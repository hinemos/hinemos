/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.JobApprovalResultConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.JobApprovalInfo;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.apllog.AplLogger;
import com.sun.mail.smtp.SMTPAddressFailedException;

/**
 * 承認処理に関するメールを送信するクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class SendApprovalMail {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(SendApprovalMail.class);

	/** アドレス文字コード */
	public static final String _charsetAddressDefault = "UTF-8";

	/** メール件名文字コード */
	public static final String _charsetSubjectDefault = "UTF-8";

	/** メール本文文字コード */
	public static final String _charsetContentDefault = "UTF-8";

	
	/**
	 * 承認依頼メールの送信を行います。
	 *
	 * @param JobInfoEntity jobInfo ジョブ情報
	 */
	public synchronized void sendRequest(JobInfoEntity jobInfo, String approvalRequestUser) throws HinemosUnknown {
		
		// メール件名と本文を取得
		String subject = jobInfo.getApprovalReqMailTitle();
		String request = null;
		
		if(jobInfo.isUseApprovalReqSentence()){
			request = jobInfo.getApprovalReqSentence();
		} else{
			request = jobInfo.getApprovalReqMailBody();
		}
		
		// 改行コードをLFからCRLFに変更する。
		// 本来はJavaMailが変換するはずだが、変換されないこともあるので、
		// 予め変更しておく。
		String content = request.replaceAll("\r\n", "\n").replaceAll("\n", "\r\n");
		
		sendMail(jobInfo, subject, content, approvalRequestUser);
	}

	/**
	 * 承認結果メールの送信を行います。
	 *
	 * @param JobInfoEntity jobInfo ジョブ情報
	 * @param JobApprovalInfo approvalInfo 承認情報
	 */
	public synchronized void sendResult(JobInfoEntity jobInfo, JobApprovalInfo approvalInfo) throws HinemosUnknown {
		
		// メール件名と本文と承認結果を取得
		String subject = jobInfo.getApprovalReqMailTitle();
		
		String result = null;
		String request = null;
		
		if(approvalInfo.getResult() == JobApprovalResultConstant.TYPE_APPROVAL){
			result = Messages.getString("APPROVAL_RESULT_APPROVE");
		}else{
			result = Messages.getString("APPROVAL_RESULT_DENY");
		}
		if(jobInfo.isUseApprovalReqSentence()){
			request = jobInfo.getApprovalReqSentence();
		} else{
			request = jobInfo.getApprovalReqMailBody();
		}
		
		// メール本文の文面を生成
		StringBuffer buf = new StringBuffer();
		buf.append(Messages.getString("MESSAGE_APPROVAL_CONFIRMED") + "\n" + "\n");
		buf.append(Messages.getString("APPROVAL_RESULT") + "："+ result + "\n" + "\n");
		buf.append("--------------------------------------------------------------------------------" + "\n" + "\n");
		buf.append(request);
		
		// 改行コードをLFからCRLFに変更する。
		// 本来はJavaMailが変換するはずだが、変換されないこともあるので、
		// 予め変更しておく。
		String content = buf.toString().replaceAll("\r\n", "\n").replaceAll("\n", "\r\n");
		
		sendMail(jobInfo, subject, content, approvalInfo.getRequestUser());
	}

	/**
	 * メールの送信を行います。
	 * 
	 * @param jobInfo
	 *            ジョブ情報
	 * @param subject
	 *            件名
	 * @param content
	 *            本文
	 */
	private void sendMail(JobInfoEntity jobInfo, String subject, String content, String approvalRequestUser) throws HinemosUnknown {
		m_log.debug("sendMail()");
		
		m_log.debug("sendMail() subject:" + subject);
		m_log.debug("sendMail() content:" + content);
		
		try {
			ArrayList<String> toAddressList = new ArrayList<String>();
			
			String userId = null;
			List<String> userIdList = null;
			String addr;
			
			userId = jobInfo.getApprovalReqUserId();
			if (userId != null && !userId.equals("*")) {
				addr = getUserMailAdress(userId);
				if(addr != null){
					toAddressList.add(addr);
				}
			}else{
				userIdList = UserRoleCache.getUserIdList(jobInfo.getApprovalReqRoleId());
				if(userIdList != null && !userIdList.isEmpty()){
					for(String user : userIdList){
						addr = null;
						addr = getUserMailAdress(user);
						if(addr != null){
							toAddressList.add(addr);
						}
					}
				}
			}
			
			if(approvalRequestUser != null && !approvalRequestUser.equals("")){
				addr = null;
				addr = getUserMailAdress(approvalRequestUser);
				if(addr != null){
					toAddressList.add(addr);
				}
			}
			// メールアドレス設定されたユーザが無かった場合は内部イベント通知
			if(toAddressList.size() == 0){
				m_log.debug("sendMail() : mail address is empty");
				internalEventNotify(PriorityConstant.TYPE_INFO, jobInfo.getId().getSessionId(), jobInfo.getId().getJobId(),
						MessageConstant.MESSAGE_SYS_019_JOB, null);
				return;
			}
			String[] toAddress = toAddressList.toArray(new String[0]);
			
			try {
				this.sendMail(toAddress, null, subject, content);
			} catch (AuthenticationFailedException e) {
				String detailMsg = "cannot connect to the mail server due to an Authentication Failure";
				m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				internalEventNotify(PriorityConstant.TYPE_CRITICAL, jobInfo.getId().getSessionId(), jobInfo.getId().getJobId(),
						MessageConstant.MESSAGE_SYS_020_JOB, null);
			} catch (SMTPAddressFailedException e) {
				String detailMsg = e.getMessage() + "(SMTPAddressFailedException)";
				m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				internalEventNotify(PriorityConstant.TYPE_CRITICAL, jobInfo.getId().getSessionId(), jobInfo.getId().getJobId(),
						MessageConstant.MESSAGE_SYS_020_JOB, null);
			} catch (MessagingException e) {
				String detailMsg = e.getCause() != null ? e.getMessage() + " Cause : " + e.getCause().getMessage() : e.getMessage();
				m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				internalEventNotify(PriorityConstant.TYPE_CRITICAL, jobInfo.getId().getSessionId(), jobInfo.getId().getJobId(),
						MessageConstant.MESSAGE_SYS_020_JOB, null);
			} catch (UnsupportedEncodingException e) {
				String detailMsg = e.getCause() != null ? e.getMessage() + " Cause : " + e.getCause().getMessage() : e.getMessage();
				m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + detailMsg + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				internalEventNotify(PriorityConstant.TYPE_CRITICAL, jobInfo.getId().getSessionId(), jobInfo.getId().getJobId(),
						MessageConstant.MESSAGE_SYS_020_JOB, null);
			}
		} catch (RuntimeException e1) {
			String detailMsg = e1.getCause() != null ? e1.getMessage() + " Cause : " + e1.getCause().getMessage() : e1.getMessage();
			m_log.warn("sendMail() " + e1.getMessage() + " : " + detailMsg + detailMsg + " : "
					+ e1.getClass().getSimpleName() + ", " + e1.getMessage(), e1);
			internalEventNotify(PriorityConstant.TYPE_CRITICAL, jobInfo.getId().getSessionId(), jobInfo.getId().getJobId(),
					MessageConstant.MESSAGE_SYS_020_JOB, null);
		}
	}

	/**
	 * メールを送信します。
	 * @param toAddressStr
	 *            送信先Toアドレス
	 * @param ccAddressStr
	 *            送信先Ccアドレス
	 * @param subject
	 *            件名
	 * @param content
	 *            本文
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	
	public void sendMail(String[] toAddressStr, String[] ccAddressStr, String subject, String content)
			throws MessagingException, UnsupportedEncodingException {

		if (toAddressStr == null || toAddressStr.length <= 0) {
			// 何もせず終了
			return;
		}
		/*
		 *  https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html
		 */
		Properties _properties = new Properties();
		_properties.setProperty("mail.debug", Boolean.toString(HinemosPropertyUtil.getHinemosPropertyBool("mail.debug", false)));
		_properties.setProperty("mail.store.protocol", HinemosPropertyUtil.getHinemosPropertyStr("mail.store.protocol", "pop3"));
		String protocol = HinemosPropertyUtil.getHinemosPropertyStr("mail.transport.protocol", "smtp");
		_properties.setProperty("mail.transport.protocol", protocol);
		_properties.put("mail.smtp.socketFactory", javax.net.SocketFactory.getDefault());
		_properties.put("mail.smtp.ssl.socketFactory", javax.net.ssl.SSLSocketFactory.getDefault());
		
		setProperties(_properties, "mail." + protocol + ".user", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".host", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".port", HinemosPropertyTypeConstant.TYPE_NUMERIC);
		setProperties(_properties, "mail." + protocol + ".connectiontimeout", HinemosPropertyTypeConstant.TYPE_NUMERIC);
		setProperties(_properties, "mail." + protocol + ".timeout", HinemosPropertyTypeConstant.TYPE_NUMERIC);
		setProperties(_properties, "mail." + protocol + ".writetimeout", HinemosPropertyTypeConstant.TYPE_NUMERIC);
		setProperties(_properties, "mail." + protocol + ".from", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".localhost", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".localaddress", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".localport", HinemosPropertyTypeConstant.TYPE_NUMERIC);
		setProperties(_properties, "mail." + protocol + ".ehlo", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".auth", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".auth.mechanisms", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".auth.login.disable", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".auth.plain.disable", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".auth.digest-md5.disable", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".auth.ntlm.disable", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".auth.ntlm.domain", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".auth.ntlm.flags", HinemosPropertyTypeConstant.TYPE_NUMERIC);
		setProperties(_properties, "mail." + protocol + ".submitter", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".dsn.notify", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".dsn.ret", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".allow8bitmime", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".sendpartial", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".sasl.enable", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".sasl.mechanisms", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".sasl.authorizationid", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".sasl.realm", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".sasl.usecanonicalhostname", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".quitwait", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".reportsuccess", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".socketFactory.class", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".socketFactory.fallback", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".socketFactory.port", HinemosPropertyTypeConstant.TYPE_NUMERIC);
		setProperties(_properties, "mail." + protocol + ".starttls.enable", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".starttls.required", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".socks.host", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".socks.port", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".mailextension", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail." + protocol + ".userset", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail." + protocol + ".noop.strict", HinemosPropertyTypeConstant.TYPE_TRUTH);
		
		setProperties(_properties, "mail.smtp.ssl.enable", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail.smtp.ssl.checkserveridentity", HinemosPropertyTypeConstant.TYPE_TRUTH);
		setProperties(_properties, "mail.smtp.ssl.trust", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail.smtp.ssl.socketFactory.class", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail.smtp.ssl.socketFactory.port", HinemosPropertyTypeConstant.TYPE_NUMERIC);
		setProperties(_properties, "mail.smtp.ssl.protocols", HinemosPropertyTypeConstant.TYPE_STRING);
		setProperties(_properties, "mail.smtp.ssl.ciphersuites", HinemosPropertyTypeConstant.TYPE_STRING);

		/**
		 * メールの設定をDBから取得
		 */
		String _loginUser = HinemosPropertyUtil.getHinemosPropertyStr("mail.transport.user", "nobody");
		String _loginPassword = HinemosPropertyUtil.getHinemosPropertyStr("mail.transport.password", "password");
		String _fromAddress = HinemosPropertyUtil.getHinemosPropertyStr("mail.from.address", "admin@hinemos.com");
		String _fromPersonalName = HinemosPropertyUtil.getHinemosPropertyStr("mail.from.personal.name", "Hinemos Admin");
		_fromPersonalName = convertNativeToAscii(_fromPersonalName);
		
		String _replyToAddress = HinemosPropertyUtil.getHinemosPropertyStr("mail.reply.to.address", "admin@hinemos.com");
		String _replyToPersonalName =HinemosPropertyUtil.getHinemosPropertyStr("mail.reply.personal.name", "Hinemos Admin");
		_replyToPersonalName = convertNativeToAscii(_replyToPersonalName);
		
		String _errorsToAddress = HinemosPropertyUtil.getHinemosPropertyStr("mail.errors.to.address", "admin@hinemos.com");

		int _transportTries = HinemosPropertyUtil.getHinemosPropertyNum("mail.transport.tries", Long.valueOf(1)).intValue();
		int _transportTriesInterval = HinemosPropertyUtil.getHinemosPropertyNum("mail.transport.tries.interval", Long.valueOf(10000)).intValue();

		String _charsetAddress = HinemosPropertyUtil.getHinemosPropertyStr("mail.charset.address", _charsetAddressDefault);
		String _charsetSubject = HinemosPropertyUtil.getHinemosPropertyStr("mail.charset.subject", _charsetSubjectDefault);
		String _charsetContent = HinemosPropertyUtil.getHinemosPropertyStr("mail.charset.content", _charsetContentDefault);

		m_log.debug("initialized mail sender : from_address = " + _fromAddress
				+ ", From = " + _fromPersonalName + " <" + _replyToAddress + ">"
				+ ", Reply-To = " + _replyToPersonalName + " <" + _replyToAddress + ">"
				+ ", Errors-To = " + _errorsToAddress
				+ ", tries = " + _transportTries
				+ ", tries-interval = " + _transportTriesInterval
				+ ", Charset [address:subject:content] = [" + _charsetAddress + ":" + _charsetSubject + ":" + _charsetContent + "]");

		// JavaMail Sessionリソース検索
		Session session = Session.getInstance(_properties);

		Message mineMsg = new MimeMessage(session);

		// 送信元メールアドレスと送信者名を指定
		if (_fromAddress != null && _fromPersonalName != null) {
			mineMsg.setFrom(new InternetAddress(_fromAddress, _fromPersonalName, _charsetAddress));
		} else if (_fromAddress != null && _fromPersonalName == null) {
			mineMsg.setFrom(new InternetAddress(_fromAddress));
		}

		// REPLY-TOを指定
		if (_replyToAddress != null && _replyToPersonalName != null) {
			InternetAddress reply[] = { new InternetAddress(_replyToAddress, _replyToPersonalName, _charsetAddress) };
			mineMsg.setReplyTo(reply);
			mineMsg.reply(true);
		} else if (_replyToAddress != null && _replyToPersonalName == null) {
			InternetAddress reply[] = { new InternetAddress(_replyToAddress) };
			mineMsg.setReplyTo(reply);
			mineMsg.reply(true);
		}

		// ERRORS-TOを指定
		if (_errorsToAddress != null) {
			mineMsg.setHeader("Errors-To", _errorsToAddress);
		}

		// 送信先メールアドレスを指定
		// TO
		InternetAddress[] toAddress = this.getAddress(toAddressStr);
		if (toAddress != null && toAddress.length > 0) {
			mineMsg.setRecipients(javax.mail.Message.RecipientType.TO, toAddress);
		} else {
			return; // TOは必須
		}
		
		// CC
		if (ccAddressStr != null) {
			InternetAddress[] ccAddress = this.getAddress(ccAddressStr);
			if (ccAddress != null && ccAddress.length > 0) {
				mineMsg.setRecipients(javax.mail.Message.RecipientType.CC, ccAddress);
			}
		}
		String message = "TO=" + Arrays.asList(toAddressStr);
		
		if (ccAddressStr != null) {
			message += ", CC=" + Arrays.asList(ccAddressStr);
		}
		m_log.debug(message);
		
		// メールの件名を指定
		mineMsg.setSubject(MimeUtility.encodeText(subject, _charsetSubject, "B"));

		// メールの内容を指定
		mineMsg.setContent(content, "text/plain; charset=" + _charsetContent);

		// 送信日付を指定
		mineMsg.setSentDate(HinemosTime.getDateInstance());

		// 再送信フラグがtrueかつ再送回数以内の場合
		for (int i = 0; i < _transportTries; i++) {
			Transport transport = null;
			try {
				// メール送信
				transport = session.getTransport();
				boolean flag = HinemosPropertyUtil.getHinemosPropertyBool("mail." + protocol + ".auth", false);
				if(flag) {
					transport.connect(_loginUser, _loginPassword);
				} else {
					transport.connect();
				}
				transport.sendMessage(mineMsg, mineMsg.getAllRecipients());
				break;
			} catch (AuthenticationFailedException e) {
				throw e;
			} catch (SMTPAddressFailedException e) {
				throw e;
			} catch (MessagingException me) {
				//_transportTries中はsleep待ちのみ 
				if (i < (_transportTries - 1)) { 
					m_log.info("sendMail() : retry sendmail. " + me.getMessage());
					try {
						Thread.sleep(_transportTriesInterval);
					} catch (InterruptedException e) { }
				//_transportTriesの最後はINTERNALイベントの通知のためExceptionをthrow 
				} else {
					throw me;
				}
			} finally {
				if (transport != null) {
					transport.close();
				}
			}
		}
	}
	
	private String convertNativeToAscii(String nativeStr) {
		if (HinemosPropertyUtil.getHinemosPropertyBool("mail.native.to.ascii", false)){
			final CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
			final StringBuilder asciiStr = new StringBuilder();
			for (final Character character : nativeStr.toCharArray()) {
				if (asciiEncoder.canEncode(character)) {
					asciiStr.append(character);
				} else {
					asciiStr.append("\\u");
					asciiStr.append(Integer.toHexString(0x10000 | character).substring(1));
				}
			}
			
			return asciiStr.toString();
		} else {
			return nativeStr;
		}
	}

	/**
	 * 引数で指定された送信先アドレスの<code> InternetAddress </code>オブジェクトを返します。
	 *
	 * @param addressList
	 *            送信先アドレスの文字列配列
	 * @return <code> InternetAddress </code>オブジェクトの配列
	 */
	private InternetAddress[] getAddress(String[] addressList) {
		InternetAddress toAddress[] = null;
		Vector<InternetAddress> list = new Vector<InternetAddress>();
		if (addressList != null) {
			for (String address : addressList) {
				try {
					list.add(new InternetAddress(address));
				} catch (AddressException e) {
					m_log.info("getAddress() : "
							+ e.getClass().getSimpleName() + ", "
							+ address + ", "
							+ e.getMessage());
				}
			}
			if (list.size() > 0) {
				toAddress = new InternetAddress[list.size()];
				list.copyInto(toAddress);
			}
		}
		return toAddress;
	}

	private void setProperties(Properties prop, String key, int type) {
		switch (type) {
		case HinemosPropertyTypeConstant.TYPE_STRING:
			String strVal = HinemosPropertyUtil.getHinemosPropertyStr(key, null);
			if (strVal != null) {
				prop.setProperty(key, strVal);
			}
			break;
		case HinemosPropertyTypeConstant.TYPE_NUMERIC:
			Long longVal = HinemosPropertyUtil.getHinemosPropertyNum(key, null);
			if (longVal != null) {
				prop.setProperty(key, longVal.toString());
			}
			break;
		case HinemosPropertyTypeConstant.TYPE_TRUTH:
			Boolean boolVal = HinemosPropertyUtil.getHinemosPropertyBool(key, null);
			if (boolVal != null) {
				prop.setProperty(key, boolVal.toString());
			}
			break;
		default:
			//上記以外はなにもしない
			break;
		}
	}
	
	/**
	 * メール送信失敗時の内部エラー通知を定義します
	 */
	public void internalEventNotify(int priority, String sessionId, String jobId, MessageConstant msgCode, String detailMsg) {
		String[] args = {sessionId, jobId};
		// メール送信失敗メッセージを出力
		AplLogger.put(priority, HinemosModuleConstant.JOB,  msgCode, args, detailMsg);
	}
	
	/**
	 * ユーザIDに対応したメールアドレスを取得する<BR>
	 *
	 * @param userId ユーザID
	 * @return ユーザ名
	 * @throws HinemosUnknown
	 */
	private String getUserMailAdress(String userId) throws HinemosUnknown {
		String address = null;
		UserInfo info = new AccessControllerBean().getUserInfo(userId);
		if(info != null){
			address = info.getMailAddress();
		}
		return address;
	}
	
}
