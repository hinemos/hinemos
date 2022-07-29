/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.MailServerSettings;
import com.clustercontrol.commons.util.MultiSmtpServerUtil;
import com.clustercontrol.commons.util.OAuthException;
import com.clustercontrol.commons.util.SendOAuthMail;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.mail.session.MailTemplateControllerBean;
import com.clustercontrol.notify.model.NotifyMailInfo;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.StringBinder;
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
 * メールを送信するクラス<BR>
 *
 * @version 3.0.0
 * @since 3.0.0
 */
public class SendMail implements Notifier {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(SendMail.class);

	/** 日時フォーマット。 */
	private static final String SUBJECT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/** 監視のオリジナルメッセージキー。 */
	private static final String _KEY_ORG_MESSAGE = "ORG_MESSAGE";

	static {
		m_log.debug(" send all mode : " + MultiSmtpServerUtil.isSendAll());
	}

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

		if (m_log.isDebugEnabled()) {
			m_log.debug("sendMail() " + outputInfo);
		}

		try {
			NotifyMailInfo mailInfo = QueryUtil.getNotifyMailInfoPK(notifyId);

			// メールの件名を指定
			String subject = getSubject(outputInfo, mailInfo);

			// メールの内容を指定
			String content = getContent(outputInfo, mailInfo);

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
						m_log.debug("sendMail() : Server " + i + " lastFailreTime is set as "
								+ MultiSmtpServerUtil.getLastFailureTime(i));

						continue;
					}

					// 前回の送信失敗から一定期間経っていない場合は送信をスキップする
					if (MultiSmtpServerUtil.isInCooltime(i)) {
						String detailMsg = "Server " + i + " is skipped because cooltime( "
								+ MultiSmtpServerUtil.getFailureCooltime(i)
								+ " [msec]) has not passed from last failure. "
								+ MultiSmtpServerUtil.getLastFailureTime(i);
						m_log.info("sendMail() : " + detailMsg);

						continue;
					}

					try {
						this.sendMail(toAddress, ccAddressList.toArray(new String[0]),
								bccAddressList.toArray(new String[0]), subject, content, i);
						successCount++;

						// クールタイム明けで送信に成功した場合は最終失敗時刻をクリアし、インターナルイベントを通知する
						if (MultiSmtpServerUtil.getLastFailureTime(i) != null) {
							Date failureDate = MultiSmtpServerUtil.getLastFailureTime(i);
							String detailMsg = "reopened Server " + i + " and succeed to send mail. (Last Failure "
									+ failureDate + " )";
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

		} catch (RuntimeException | NotifyNotFound | OAuthException e1) {
			String detailMsg = e1.getCause() != null ? e1.getMessage() + " Cause : " + e1.getCause().getMessage() : e1.getMessage();
			m_log.warn("sendMail() " + e1.getMessage() + " : " + detailMsg + detailMsg + " : "
					+ e1.getClass().getSimpleName() + ", " + e1.getMessage(), e1);
			internalErrorNotifyMailSendFailed(notifyId, outputInfo, detailMsg);
		}
	}

	/**
	 * メールを送信します。
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
		sendMail(toAddressStr, null, null, subject, content, -1);
	}

	public void sendMail(String[] toAddressStr, String subject, String content, int slot)
			throws MessagingException, UnsupportedEncodingException, OAuthException {
		sendMail(toAddressStr, null, null, subject, content, slot);
	}

	public void sendMail(String[] toAddressStr, String[] ccAddressStr, String[] bccAddressStr, String subject,
			String content) throws MessagingException, UnsupportedEncodingException, OAuthException {
		sendMail(toAddressStr, ccAddressStr, bccAddressStr, subject, content, -1);
	}

	public void sendMail(String[] toAddressStr, String[] ccAddressStr, String[] bccAddressStr, String subject,
			String content, int slot) throws MessagingException, UnsupportedEncodingException, OAuthException {

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
	 * メール件名を返します。
	 *
	 * @param source
	 *            出力内容
	 * @param mailInfo
	 *            通知内容
	 * @return メール件名
	 */
	public String getSubject(OutputBasicInfo source, NotifyMailInfo mailInfo) {

		String subject = null;
		try {
			if (mailInfo != null
					&& mailInfo.getMailTemplateInfoEntity() != null
					&& mailInfo.getMailTemplateInfoEntity().getMailTemplateId() != null) {
				MailTemplateInfo templateData = new MailTemplateControllerBean()
						.getMailTemplateInfo(mailInfo.getMailTemplateInfoEntity().getMailTemplateId());
				int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
				String origin = templateData.getSubject();
				ArrayList<String> inKeyList = StringBinder.getKeyList(origin, maxReplaceWord);
				Map<String, String> param = NotifyUtil.createParameter(source, mailInfo.getNotifyInfoEntity(), inKeyList);
				StringBinder binder = new StringBinder(param);
				subject = binder.replace(origin);
			} else if (source.getFacilityId().equals(FacilityTreeAttributeConstant.INTERNAL_SCOPE)
					&& !(HinemosPropertyCommon.internal_mail_subject.getStringValue().equals(""))) {
				// デフォルト(テンプレート指定なし)でのinternal通知で
				// internal.mail.subjectが設定済みなら、設定値を表題とする（パラメータ置換あり）
				if (m_log.isDebugEnabled()) {
					m_log.debug("getSubject() :use internal.mail.subject. value="
							+ HinemosPropertyCommon.internal_mail_subject.getStringValue());
				}
				int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
				String origin = HinemosPropertyCommon.internal_mail_subject.getStringValue();
				ArrayList<String> inKeyList = StringBinder.getKeyList(origin, maxReplaceWord);
				Map<String, String> param = NotifyUtil.createParameter(source, null, inKeyList);
				StringBinder binder = new StringBinder(param);
				subject = binder.replace(origin);
			} else {
				Locale locale = NotifyUtil.getNotifyLocale();
				subject = Messages.getString("MAIL_SUBJECT", locale) + "("
						+ Messages.getString(PriorityConstant.typeToMessageCode(source.getPriority()), locale) + ")";
			}
		} catch (Exception e) {
			m_log.warn("getSubject() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// 例外発生時のメールサブジェクト
			return "Hinemos Notification";
		}

		return subject;
	}

	/**
	 * メール本文を返します。
	 *
	 * @param source
	 *            出力内容
	 * @param mailInfo
	 *            通知内容
	 * @return メール本文
	 */
	public String getContent(OutputBasicInfo source, NotifyMailInfo mailInfo) {

		StringBuffer buf = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat(SUBJECT_DATE_FORMAT);
		sdf.setTimeZone(HinemosTime.getTimeZone());

		try {
			if (mailInfo != null
					&& mailInfo.getMailTemplateInfoEntity() != null
					&& mailInfo.getMailTemplateInfoEntity().getMailTemplateId() != null) {
				MailTemplateInfo mailData = new MailTemplateControllerBean()
						.getMailTemplateInfo(mailInfo.getMailTemplateInfoEntity().getMailTemplateId());
				int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
				String origin = mailData.getBody();
				ArrayList<String> inKeyList = StringBinder.getKeyList(origin, maxReplaceWord);
				Map<String, String> param = NotifyUtil.createParameter(source, mailInfo.getNotifyInfoEntity(), inKeyList);
				if (source.getPluginId().matches(HinemosModuleConstant.MONITOR+".*")
						&& param.get(_KEY_ORG_MESSAGE).length() >= HinemosPropertyCommon.notify_mail_messageorg_max_length.getIntegerValue()) {
					String updateMessageOrg = param.get(_KEY_ORG_MESSAGE).substring(0, HinemosPropertyCommon.notify_mail_messageorg_max_length.getIntegerValue());
					param.replace(_KEY_ORG_MESSAGE, updateMessageOrg);
				}

				StringBinder binder = new StringBinder(param);
				buf.append(binder.replace(origin + "\n"));
			} else {

				Locale locale = NotifyUtil.getNotifyLocale();

				buf.append(Messages.getString("GENERATION_TIME", locale) + " : "
						+ sdf.format(source.getGenerationDate()) + "\n");
				buf.append(Messages.getString("APPLICATION", locale) + " : "
						+ HinemosMessage.replace(source.getApplication(), locale) + "\n");
				buf.append(Messages.getString("PRIORITY", locale) + " : "
						+ Messages.getString(PriorityConstant.typeToMessageCode(source.getPriority()), locale) + "\n");
				buf.append(Messages.getString("MESSAGE", locale) + " : "
						+ HinemosMessage.replace(source.getMessage(), locale) + "\n");
				buf.append(Messages.getString("SCOPE", locale) + " : "
						+ HinemosMessage.replace(source.getScopeText(), locale) + "\n");
			}
		} catch (MailTemplateNotFound | InvalidRole | HinemosUnknown | RuntimeException e) {
			m_log.warn("getContent() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// 例外発生時のメール本文
			return "An error occurred creating message.";
		}

		// 改行コードをLFからCRLFに変更する。
		// 本来はJavaMailが変換するはずだが、変換されないこともあるので、
		// 予め変更しておく。
		String ret = buf.toString().replaceAll("\r\n", "\n").replaceAll("\n", "\r\n");

		return ret;
	}

	/**
	 * 通知失敗時の内部エラー通知を定義します
	 */
	private void internalErrorNotifyMailSendFailed(String notifyId, OutputBasicInfo source, String detailMsg) {
		// 通知元が監視の場合
		if(source.getPluginId().matches(HinemosModuleConstant.MONITOR+".*")){
			String[] args = { notifyId, source.getFacilityId(), source.getMonitorId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_013, args, detailMsg);
		}
		// 通知元がジョブの場合
		else if(source.getPluginId().matches(HinemosModuleConstant.JOB+".*")) {
			String[] args = { notifyId, source.getMonitorId(), source.getJobunitId(), source.getJobId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_014, args, detailMsg);
		}
		// 通知元がメンテナンスの場合
		else if(source.getPluginId().matches(HinemosModuleConstant.SYSYTEM_MAINTENANCE)){
			String[] args = { notifyId, source.getMonitorId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_015, args, detailMsg);
		}
		// 通知元が環境構築の場合
		else if(source.getPluginId().matches(HinemosModuleConstant.INFRA)){
			String[] args = { notifyId, source.getFacilityId(), source.getMonitorId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_016, args, detailMsg);
		}
		// 通知元が構成情報設定の場合
		else if(source.getPluginId().matches(HinemosModuleConstant.NODE_CONFIG_SETTING)){
			String[] args = { notifyId, source.getFacilityId(), source.getMonitorId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_017, args, detailMsg);
		}
		// 通知元がSDMLの場合
		else if (source.getPluginId().matches(HinemosModuleConstant.SDML_CONTROL)) {
			String[] args = { notifyId, source.getMonitorId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_032, args, detailMsg);
		}
		// 通知元がそれ以外の場合(通ることはない)
		else{
			String[] args = { notifyId };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_007, args, detailMsg);
		}
	}
}
