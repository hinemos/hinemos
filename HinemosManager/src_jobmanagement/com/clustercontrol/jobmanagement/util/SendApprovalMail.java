/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.bean.JobApprovalResultConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.MailServerSettings;
import com.clustercontrol.commons.util.MultiSmtpServerUtil;
import com.clustercontrol.commons.util.OAuthException;
import com.clustercontrol.commons.util.SendOAuthMail;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.JobApprovalInfo;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.apllog.AplLogger;
import com.sun.mail.smtp.SMTPAddressFailedException;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;

/**
 * 承認処理に関するメールを送信するクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class SendApprovalMail {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(SendApprovalMail.class);

	/**
	 * 承認依頼メールの送信を行います。
	 *
	 * @param JobInfoEntity
	 *            jobInfo ジョブ情報
	 */
	public synchronized void sendRequest(JobInfoEntity jobInfo, String approvalRequestUser) throws HinemosUnknown {

		// メール件名と本文を取得
		String subject = jobInfo.getApprovalReqMailTitle();
		String request = null;

		if (jobInfo.isUseApprovalReqSentence()) {
			request = jobInfo.getApprovalReqSentence();
			// 承認依頼文を利用する場合リンクアドレスを付与する。取得に失敗しても処理は継続する。
			try {
				request += "\r\n\r\n" + new JobControllerBean().getApprovalPageLink();
			} catch (HinemosUnknown e) {
				m_log.warn("failed to get job.approval.page.link");
			}
		} else {
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
	 * @param JobInfoEntity
	 *            jobInfo ジョブ情報
	 * @param JobApprovalInfo
	 *            approvalInfo 承認情報
	 */
	public synchronized void sendResult(JobInfoEntity jobInfo, JobApprovalInfo approvalInfo) throws HinemosUnknown {

		// メール件名と本文と承認結果を取得
		String subject = jobInfo.getApprovalReqMailTitle();

		String result = null;
		String request = null;

		if (approvalInfo.getResult() == JobApprovalResultConstant.TYPE_APPROVAL) {
			result = Messages.getString("APPROVAL_RESULT_APPROVE");
		} else {
			result = Messages.getString("APPROVAL_RESULT_DENY");
		}
		if (jobInfo.isUseApprovalReqSentence()) {
			request = jobInfo.getApprovalReqSentence();
			// 承認依頼文を利用する場合リンクアドレスを付与する。取得に失敗しても処理は継続する。
			try {
				request += "\r\n\r\n" + new JobControllerBean().getApprovalPageLink();
			} catch (HinemosUnknown e) {
				// 処理しない
			}
		} else {
			request = jobInfo.getApprovalReqMailBody();
		}

		// メール本文の文面を生成
		StringBuffer buf = new StringBuffer();
		buf.append(Messages.getString("MESSAGE_APPROVAL_CONFIRMED") + "\n" + "\n");
		buf.append(Messages.getString("APPROVAL_RESULT") + "：" + result + "\n" + "\n");
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
	private void sendMail(JobInfoEntity jobInfo, String subject, String content, String approvalRequestUser)
			throws HinemosUnknown {
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
				if (addr != null) {
					toAddressList.add(addr);
				}
			} else {
				userIdList = UserRoleCache.getUserIdList(jobInfo.getApprovalReqRoleId());
				if (userIdList != null && !userIdList.isEmpty()) {
					for (String user : userIdList) {
						addr = null;
						addr = getUserMailAdress(user);
						if (addr != null) {
							toAddressList.add(addr);
						}
					}
				}
			}

			if (approvalRequestUser != null && !approvalRequestUser.equals("")) {
				addr = null;
				addr = getUserMailAdress(approvalRequestUser);
				if (addr != null) {
					toAddressList.add(addr);
				}
			}
			// メールアドレス設定されたユーザが無かった場合は内部イベント通知
			if (toAddressList.size() == 0) {
				m_log.debug("sendMail() : mail address is empty");
				// メール送信失敗メッセージを出力
				String[] args = { jobInfo.getId().getSessionId(), jobInfo.getId().getJobId() };
				AplLogger.put(InternalIdCommon.JOB_SYS_019, args);
				return;
			}
			String[] toAddress = toAddressList.toArray(new String[0]);

			// オーナーロールIDを取得
			JobSessionJobEntity sessionJob = jobInfo.getJobSessionJobEntity();
			if (sessionJob == null) {
				// 本メソッド実行元でJobSessionJobEntityからJobInfoEntityを取得しているため不要と思われるが念のため。
				sessionJob = QueryUtil.getJobSessionJobPK(
						jobInfo.getId().getSessionId(), jobInfo.getId().getJobunitId(), jobInfo.getId().getJobId());
			}
			String ownerRoleId = sessionJob.getOwnerRoleId();

			// 承認ジョブのオーナーロールIDから送信先SMTPサーバを決定する
			List<Integer> serverList = MultiSmtpServerUtil.getRoleServerList(ownerRoleId);
			if (serverList == null) {
				serverList = new ArrayList<Integer>();
			}

			/*
			 * オーナーロールIDがどのmail.X.ownerRoleID.listにも含まれない場合、デフォルトSMTPサーバに送る
			 */
			if (serverList.isEmpty() || "".equals(ownerRoleId) || ownerRoleId == null) {
				serverList.add(0);
			}
			m_log.debug("sendMail() : using server is " + serverList);

			int successCount = 0;

			for (int i : serverList) {
				try {
					boolean isThisServerEnabled = true;

					if (i == 0) {
						// デフォルトのSMTPサーバの場合、常に使用する
						isThisServerEnabled = true;
					} else {
						// マルチSMTPサーバで設定されたSMTPサーバの場合、
						// Hinemosプロパティから有効・無効を判断する
						isThisServerEnabled = HinemosPropertyCommon.mail_$_server_enable
								.getBooleanValue(Integer.toString(i));
					}

					if (isThisServerEnabled == false) {
						m_log.info("sendMail() : Server " + i
								+ " is not used because it is disabled by Hinemos property.");
						// 無効であった場合は最終失敗時刻をクリアしておく
						MultiSmtpServerUtil.clearLastFailureTime(i);
						m_log.debug("sendMail() : Server " + i + " lastFailreTime is set as"
								+ MultiSmtpServerUtil.getLastFailureTime(i));

						continue;
					}

					// 前回の送信失敗から一定期間経っていない場合は送信をスキップする
					if (MultiSmtpServerUtil.isInCooltime(i)) {
						String detailMsg = "Server " + i + " is skipped because cooltime("
								+ MultiSmtpServerUtil.getFailureCooltime(i)
								+ "[msec]) has not passed from last failure. "
								+ MultiSmtpServerUtil.getLastFailureTime(i);
						m_log.info("sendMail() : " + detailMsg);

						continue;
					}

					try {
						this.sendMail(toAddress, null, subject, content, i);
						successCount++;

						// クールタイム明けで送信に成功した場合は最終失敗時刻をクリアし、インターナルイベントを通知する
						if (MultiSmtpServerUtil.getLastFailureTime(i) != null) {
							Date failureDate = MultiSmtpServerUtil.getLastFailureTime(i);
							String detailMsg = "reopened Server " + i + " and succeed to send mail. (Last Failure "
									+ failureDate + ")";
							MultiSmtpServerUtil.clearLastFailureTime(i);
							String[] args = { failureDate.toString(), String.valueOf(i) };
							AplLogger.put(InternalIdCommon.JOB_SYS_027, args, detailMsg);
						}

						/*
						 * 全てのサーバで送信を実施する設定でない場合は、 どれか一つのSMTPサーバで送信成功した時点で終了する
						 */
						if (!MultiSmtpServerUtil.isSendAll()) {
							m_log.info("sendMail() : Send mode is any one. Ignore the rest of serverList");
							break;
						}

					} catch (Exception e) {
						m_log.warn("sendMail() : Server " + i + " encountered errors while sending mail. ");
						MultiSmtpServerUtil.setLastFailureTime(i, HinemosTime.getDateInstance());
						throw e;
					}

				} catch (AuthenticationFailedException e) {
					String detailMsg = "cannot connect to the mail server due to an Authentication Failure";
					m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + " : " + e.getClass().getSimpleName()
							+ ", " + e.getMessage());
					String[] args = { jobInfo.getId().getSessionId(), jobInfo.getId().getJobId() };
					AplLogger.put(InternalIdCommon.JOB_SYS_020, args);
				} catch (SMTPAddressFailedException e) {
					String detailMsg = e.getMessage() + "(SMTPAddressFailedException)";
					m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + " : " + e.getClass().getSimpleName()
							+ ", " + e.getMessage());
					String[] args = { jobInfo.getId().getSessionId(), jobInfo.getId().getJobId() };
					AplLogger.put(InternalIdCommon.JOB_SYS_020, args);
				} catch (MessagingException e) {
					String detailMsg = e.getCause() != null ? e.getMessage() + " Cause : " + e.getCause().getMessage()
							: e.getMessage();
					m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + " : " + e.getClass().getSimpleName()
							+ ", " + e.getMessage());
					String[] args = { jobInfo.getId().getSessionId(), jobInfo.getId().getJobId() };
					AplLogger.put(InternalIdCommon.JOB_SYS_020, args);
				} catch (UnsupportedEncodingException e) {
					String detailMsg = e.getCause() != null ? e.getMessage() + " Cause : " + e.getCause().getMessage()
							: e.getMessage();
					m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + detailMsg + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					String[] args = { jobInfo.getId().getSessionId(), jobInfo.getId().getJobId() };
					AplLogger.put(InternalIdCommon.JOB_SYS_020, args);
				}
			}

			if (successCount == 0) {
				String detailMsg = "failed to send mail on all servers available for " + ownerRoleId;
				m_log.warn("sendMail() " + detailMsg);
				String[] args = { jobInfo.getId().getSessionId() };
				AplLogger.put(InternalIdCommon.JOB_SYS_026, args, detailMsg);

			}

		} catch (RuntimeException | JobInfoNotFound | InvalidRole | OAuthException e1) {
			String detailMsg = e1.getCause() != null ? e1.getMessage() + " Cause : " + e1.getCause().getMessage()
					: e1.getMessage();
			m_log.warn("sendMail() " + e1.getMessage() + " : " + detailMsg + detailMsg + " : "
					+ e1.getClass().getSimpleName() + ", " + e1.getMessage(), e1);
			String[] args = { jobInfo.getId().getSessionId(), jobInfo.getId().getJobId() };
			AplLogger.put(InternalIdCommon.JOB_SYS_020, args);
		}
	}

	/**
	 * メールを送信します。
	 * 
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
			throws MessagingException, UnsupportedEncodingException, OAuthException {
		sendMail(toAddressStr, ccAddressStr, subject, content, -1);
	}

	public void sendMail(String[] toAddressStr, String[] ccAddressStr, String subject, String content, int slot)
			throws MessagingException, UnsupportedEncodingException, OAuthException {

		if (toAddressStr == null || toAddressStr.length <= 0) {
			// 何もせず終了
			return;
		}

		MailServerSettings mailServerSettings;
		if (slot > 0 && slot < 11) {
			mailServerSettings = MultiSmtpServerUtil.getMailServerSettings(slot);
		} else {
			mailServerSettings = new MailServerSettings();
		}

		Properties _properties = mailServerSettings.getProperties();
		String _loginUser = mailServerSettings.getLoginUser();
		String _loginPassword = mailServerSettings.getLoginPassword();
		String _fromAddress = mailServerSettings.getFromAddress();
		String _fromPersonalName = mailServerSettings.getFromPersonalName();
		String _replyToAddress = mailServerSettings.getReplyToAddress();
		String _replyToPersonalName = mailServerSettings.getReplyToPersonalName();
		String _errorsToAddress = mailServerSettings.getErrorsToAddress();
		int _transportTries = mailServerSettings.getTransportTries();
		int _transportTriesInterval = mailServerSettings.getTransportTriesInterval();
		String _charsetAddress = mailServerSettings.getCharsetAddress();
		String _charsetSubject = mailServerSettings.getCharsetSubject();
		String _charsetContent = mailServerSettings.getCharsetContent();
		String _authMechanisms = mailServerSettings.getAuthMechanisms();

		m_log.debug("initialized mail sender : from_address = " + _fromAddress + ", From = " + _fromPersonalName + " <"
				+ _replyToAddress + ">" + ", Reply-To = " + _replyToPersonalName + " <" + _replyToAddress + ">"
				+ ", Errors-To = " + _errorsToAddress + ", tries = " + _transportTries + ", tries-interval = "
				+ _transportTriesInterval + ", Charset [address:subject:content] = [" + _charsetAddress + ":"
				+ _charsetSubject + ":" + _charsetContent + "]");

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
			mineMsg.setRecipients(Message.RecipientType.TO, toAddress);
		} else {
			return; // TOは必須
		}

		// CC
		if (ccAddressStr != null) {
			InternetAddress[] ccAddress = this.getAddress(ccAddressStr);
			if (ccAddress != null && ccAddress.length > 0) {
				mineMsg.setRecipients(Message.RecipientType.CC, ccAddress);
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

		// OAuth認証で送信するためのクラス
		SendOAuthMail sendOAuthMail = null;

		// 再送信フラグがtrueかつ再送回数以内の場合
		for (int i = 0; i < _transportTries; i++) {
			Transport transport = null;
			try {
				// メール送信
				transport = session.getTransport();
				boolean flag = mailServerSettings.getAuthFlag();
				if (SendOAuthMail.STRING_AOUTH_MECHANISMS_XOAUTH2.equals(_authMechanisms) && flag) {
					if (sendOAuthMail == null) {
						sendOAuthMail = new SendOAuthMail();
					}
					sendOAuthMail.sendOAuthMail(transport, mailServerSettings, mineMsg);
				} else {
					if (flag) {
						transport.connect(_loginUser, _loginPassword);
					} else {
						transport.connect();
					}
					transport.sendMessage(mineMsg, mineMsg.getAllRecipients());
				}
				break;
			} catch (MessagingException me) {
				if (i < (_transportTries - 1)) {
					// _transportTries中はsleep待ちのみ
					m_log.info("sendMail() : retry sendmail. " + me.getMessage());
					try {
						Thread.sleep(_transportTriesInterval);
					} catch (InterruptedException e) {
					}
				} else {
					// _transportTriesの最後はINTERNALイベントの通知のためExceptionをthrow
					throw me;
				}
			} finally {
				if (transport != null) {
					transport.close();
				}
			}
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
					m_log.info(
							"getAddress() : " + e.getClass().getSimpleName() + ", " + address + ", " + e.getMessage());
				}
			}
			if (list.size() > 0) {
				toAddress = new InternetAddress[list.size()];
				list.copyInto(toAddress);
			}
		}
		return toAddress;
	}

	/**
	 * ユーザIDに対応したメールアドレスを取得する<BR>
	 *
	 * @param userId
	 *            ユーザID
	 * @return ユーザ名
	 * @throws HinemosUnknown
	 */
	private String getUserMailAdress(String userId) throws HinemosUnknown {
		String address = null;
		UserInfo info = new AccessControllerBean().getUserInfo(userId);
		if (info != null) {
			address = info.getMailAddress();
		}
		return address;
	}

}
