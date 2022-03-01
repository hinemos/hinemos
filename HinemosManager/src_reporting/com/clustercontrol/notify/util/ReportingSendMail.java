/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.MailServerSettings;
import com.clustercontrol.commons.util.MultiSmtpServerUtil;
import com.clustercontrol.commons.util.OAuthException;
import com.clustercontrol.commons.util.SendOAuthMail;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyMailInfo;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.StringBinder;
import com.clustercontrol.util.apllog.AplLogger;
import com.sun.mail.smtp.SMTPAddressFailedException;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;

/**
 * メールを送信するクラス(レポーティングオプション用)<BR>
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class ReportingSendMail extends SendMail {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(ReportingSendMail.class);

	// メール通知で添付有無を決めるマジックワード
	private static final String NOT_ATTACHED = "\\[NOT_ATTACHED\\]";

	/**
	 * メールの送信を行います。
	 *
	 * @param outputInfo 出力・通知情報
	 */
	@Override
	public synchronized void notify(NotifyRequestMessage message) {

		sendMail(message.getOutputInfo(), message.getNotifyId());
	}

	/**
	 * メールの送信を行います。
	 *
	 */
	private void sendMail(OutputBasicInfo outputInfo, String notifyId) {

		boolean attachedFlg = true;

		m_log.info("SendMail() " + outputInfo);

		try {

			NotifyInfo notifyInfo = QueryUtil.getNotifyInfoPK(notifyId);
			NotifyMailInfo mailInfo = QueryUtil.getNotifyMailInfoPK(notifyId);

			// メールの件名を指定
			String subject = getSubject(outputInfo, mailInfo);

			// メールの内容を指定
			String content = getContent(outputInfo, mailInfo);

			// 添付有無の確認
			if (notifyInfo.getDescription() != null) {
				if (notifyInfo.getDescription().matches(".*" + NOT_ATTACHED + ".*")) {
					attachedFlg = false;
				}
			}

			// オーナーロールIDを取得
			String ownerRoleId = mailInfo.getNotifyInfoEntity().getOwnerRoleId();

			// オーナーロールIDから送信先SMTPサーバを決定する
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

			/**
			 * メール送信
			 */
			String address = null;
			switch (outputInfo.getPriority()) {
			case PriorityConstant.TYPE_INFO:
				address = mailInfo.getInfoMailAddress();
				break;
			case PriorityConstant.TYPE_WARNING:
				address = mailInfo.getWarnMailAddress();
				break;
			case PriorityConstant.TYPE_CRITICAL:
				address = mailInfo.getCriticalMailAddress();
				break;
			case PriorityConstant.TYPE_UNKNOWN:
				address = mailInfo.getUnknownMailAddress();
				break;
			default:
				break;
			}
			if (address == null) {
				m_log.info("address is null");
				return;
			}
			if (address.length() == 0) {
				m_log.info("address.length()==0");
				return;
			}

			String changeAddress = null;
			try {
				int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
				ArrayList<String> inKeyList = StringBinder.getKeyList(address, maxReplaceWord);
				Map<String, String> param = NotifyUtil.createParameter(outputInfo, inKeyList);
				StringBinder binder = new StringBinder(param);
				changeAddress = binder.bindParam(address);
			} catch (Exception e) {
				m_log.warn("sendMail() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				changeAddress = address;
			}
			StringTokenizer t = new StringTokenizer(changeAddress, ";");
			ArrayList<String> toAddressList = new ArrayList<String>();
			ArrayList<String> ccAddressList = new ArrayList<String>();
			ArrayList<String> bccAddressList = new ArrayList<String>();
			String separator = ":";
			String ccPrefix = "CC" + separator;
			String bccPrefix = "BCC" + separator;
			while (t.hasMoreTokens()) {
				String addr = t.nextToken();
				if (addr.startsWith(ccPrefix)) {
					ccAddressList.add(addr.substring(ccPrefix.length()));
				} else if (addr.startsWith(bccPrefix)) {
					bccAddressList.add(addr.substring(bccPrefix.length()));
				} else {
					toAddressList.add(addr);
				}
			}
			String[] toAddress = toAddressList.toArray(new String[0]);

			if (toAddress == null || toAddress.length <= 0) {
				m_log.debug("sendMail() : mail address is empty");
				return;
			}

			String attachedFilePath = null;
			// 添付ファイルの情報を確認
			// メール通知の説明欄に添付しない（[NOT_ATTACHED]）が含まれていない、かつ
			// 添付ファイルに関する情報が空でない
			if (attachedFlg && outputInfo.getSubKey() != null && !outputInfo.getSubKey().isEmpty()) {
				attachedFilePath = outputInfo.getSubKey();
			}

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
						this.sendMailAttach(toAddress, ccAddressList.toArray(new String[0]),
								bccAddressList.toArray(new String[0]), subject, content, attachedFilePath, i);
						successCount++;

						// クールタイム明けで送信に成功した場合は最終失敗時刻をクリアし、インターナルイベントを通知する
						if (MultiSmtpServerUtil.getLastFailureTime(i) != null) {
							Date failureDate = MultiSmtpServerUtil.getLastFailureTime(i);
							String detailMsg = "reopened Server " + i + " and succeed to send mail. (Last Failure "
									+ failureDate + ")";
							MultiSmtpServerUtil.clearLastFailureTime(i);
							String[] args = { failureDate.toString(), String.valueOf(i) };
							AplLogger.put(InternalIdCommon.PLT_NTF_SYS_012, args, detailMsg);
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
					internalErrorNotifyMailSendFailed(notifyId, outputInfo, detailMsg);
				} catch (SMTPAddressFailedException e) {
					String detailMsg = e.getMessage() + "(SMTPAddressFailedException)";
					m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + " : " + e.getClass().getSimpleName()
							+ ", " + e.getMessage());
					internalErrorNotifyMailSendFailed(notifyId, outputInfo, detailMsg);
				} catch (MessagingException e) {
					String detailMsg = e.getCause() != null ? e.getMessage() + " Cause : " + e.getCause().getMessage()
							: e.getMessage();
					m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + " : " + e.getClass().getSimpleName()
							+ ", " + e.getMessage());
					internalErrorNotifyMailSendFailed(notifyId, outputInfo, detailMsg);
				} catch (UnsupportedEncodingException e) {
					String detailMsg = e.getCause() != null ? e.getMessage() + " Cause : " + e.getCause().getMessage()
							: e.getMessage();
					m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + detailMsg + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					internalErrorNotifyMailSendFailed(notifyId, outputInfo, detailMsg);
				}
			}

			if (successCount == 0) {
				String detailMsg = "failed to send mail on all servers available for " + ownerRoleId;
				m_log.warn("sendMail() " + detailMsg);
				String[] args = { notifyId };
				AplLogger.put(InternalIdCommon.PLT_NTF_SYS_011, args, detailMsg);
			}

		} catch (RuntimeException | NotifyNotFound | InvalidRole | OAuthException e1) {
			String detailMsg = e1.getCause() != null ? e1.getMessage() + " Cause : " + e1.getCause().getMessage()
					: e1.getMessage();
			m_log.warn("sendMail() " + e1.getMessage() + " : " + detailMsg + detailMsg + " : "
					+ e1.getClass().getSimpleName() + ", " + e1.getMessage(), e1);
			internalErrorNotifyMailSendFailed(notifyId, outputInfo, detailMsg);
		}
	}

	/**
	 * メールを送信します。(このルートは通らない)
	 *
	 * <p>
	 * 下記の情報は、ファイルより取得します。
	 * <p>
	 * <ul>
	 * <li>差出人アドレス</li>
	 * <li>差出人個人名</li>
	 * <li>返信の送信先アドレス</li>
	 * <li>返信の送信先個人名</li>
	 * <li>エラー送信先アドレス</li>
	 * </ul>
	 *
	 * @param addressTo
	 *            送信先アドレス
	 * @param source
	 *            出力内容
	 * @return 送信に成功した場合、<code> true </code>
	 * @throws MessagingException
	 * @throws NamingException
	 * @throws UnsupportedEncodingException
	 */
	public void sendMail(String[] toAddressStr, String subject, String content)
			throws MessagingException, UnsupportedEncodingException, OAuthException {
		sendMail(toAddressStr, null, null, subject, content);
	}

	public void sendMailAttach(String[] toAddressStr, String[] ccAddressStr, String[] bccAddressStr, String subject,
			String content, String attachedFilePath) throws MessagingException, UnsupportedEncodingException, OAuthException {
		sendMailAttach(toAddressStr, ccAddressStr, bccAddressStr, subject, content, attachedFilePath, -1);
	}

	public void sendMailAttach(String[] toAddressStr, String[] ccAddressStr, String[] bccAddressStr, String subject,
			String content, String attachedFilePath, int slot) throws MessagingException, UnsupportedEncodingException, OAuthException {

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
		if (_fromAddress != null) {
			mineMsg.setFrom(new InternetAddress(_fromAddress, _fromPersonalName, _charsetAddress));
		}
		// REPLY-TOを指定
		if (_replyToAddress != null) {
			InternetAddress[] reply = { new InternetAddress(_replyToAddress, _replyToPersonalName, _charsetAddress) };
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
		// BCC
		if (bccAddressStr != null) {
			InternetAddress[] bccAddress = this.getAddress(bccAddressStr);
			if (bccAddress != null && bccAddress.length > 0) {
				mineMsg.setRecipients(Message.RecipientType.BCC, bccAddress);
			}
		}

		String message = "TO=" + Arrays.asList(toAddressStr);
		if (ccAddressStr != null) {
			message += ", CC=" + Arrays.asList(ccAddressStr);
		}
		if (bccAddressStr != null) {
			message += ", BCC=" + Arrays.asList(bccAddressStr);
		}
		m_log.debug(message);

		// メールの件名を指定
		mineMsg.setSubject(MimeUtility.encodeText(subject, _charsetSubject, "B"));

		// メールの内容を指定(添付ファイルの処理を追加)
		if (attachedFilePath == null || attachedFilePath.equals("")) {
			mineMsg.setContent(content, "text/plain; charset=" + _charsetContent);
		} else {
			MimeMultipart multipart = new MimeMultipart();

			// パート1に本文を設定
			MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.setContent(content, "text/plain; charset=" + _charsetContent);
			multipart.addBodyPart(bodyPart); // マルチパートに本文を追加

			// パート2に添付ファイルを設定
			MimeBodyPart attachPart = new MimeBodyPart();
			FileDataSource fds = new FileDataSource(attachedFilePath);
			attachPart.setDataHandler(new DataHandler(fds));
			attachPart.setFileName(fds.getName());

			multipart.addBodyPart(attachPart); // マルチパートに添付ファイルを追加

			// メールにマルチパートを設定
			mineMsg.setContent(multipart);
		}

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
	 * 通知失敗時の内部エラー通知を定義します
	 */
	private void internalErrorNotifyMailSendFailed(String notifyId, OutputBasicInfo source, String detailMsg) {
		String[] args = { notifyId, source.getMonitorId() };
		AplLogger.put(InternalIdCommon.PLT_NTF_SYS_018, args, detailMsg);
	}
}
