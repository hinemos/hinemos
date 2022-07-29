/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;

public class MailServerSettings {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(MailServerSettings.class);

	/** リフレッシュトークンファイルの固定名 */
	private static final String REFRESH_TOKEN_FILE_NAME = "mail_refresh_token.txt";
	/** 複数のリフレッシュトークンファイルの固定名 */
	private static final String REFRESH_TOKEN_FILES_NAME = "mail_$_refresh_token.txt";
	
	/*
	 * https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-
	 * summary.html
	 */
	private Properties properties;
	private String loginUser;
	private String loginPassword;
	private String fromAddress;
	private String fromPersonalName;
	private String replyToAddress;
	private String replyToPersonalName;
	private String errorsToAddress;
	private int transportTries;
	private int transportTriesInterval;
	private String charsetAddress;
	private String charsetSubject;
	private String charsetContent;
	private String protocol;

	private Boolean authFlag;
	private String authMechanisms;
	private String mailHost;
	
	// OAuth認証
	private String refreshToken;
	private String oauthResponseKey;
	private String oauthUrl;
	private String oauthParamKey1;
	private String oauthParamKey2;
	private String oauthParamKey3;
	private String oauthParamKey4;
	private String oauthParamKey5;
	private String oauthParamKey6;
	private String oauthParamKey7;
	private String oauthParamKey8;
	private String oauthParamKey9;
	private String oauthParamKey10;
	private String oauthParamValue1;
	private String oauthParamValue2;
	private String oauthParamValue3;
	private String oauthParamValue4;
	private String oauthParamValue5;
	private String oauthParamValue6;
	private String oauthParamValue7;
	private String oauthParamValue8;
	private String oauthParamValue9;
	private String oauthParamValue10;


	

	public MailServerSettings() {
		this.protocol = HinemosPropertyCommon.mail_transport_protocol.getStringValue();
		this.properties = new Properties();

		this.properties.setProperty("mail.debug", Boolean.toString(HinemosPropertyCommon.mail_debug.getBooleanValue()));
		this.properties.setProperty("mail.store.protocol", HinemosPropertyCommon.mail_store_protocol.getStringValue());
		this.properties.setProperty("mail.transport.protocol", this.protocol);
		this.properties.put("mail.smtp.socketFactory", javax.net.SocketFactory.getDefault());
		this.properties.put("mail.smtp.ssl.socketFactory", javax.net.ssl.SSLSocketFactory.getDefault());

		this.authFlag = HinemosPropertyCommon.mail_$_auth.getBooleanValue(this.protocol, false);

		setProperties(this.properties, HinemosPropertyCommon.mail_$_user, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_host, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_port, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_connectiontimeout, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_timeout, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_writetimeout, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_from, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_localhost, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_localaddress, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_localport, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_ehlo, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_auth, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_auth_mechanisms, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_auth_login_disable, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_auth_plain_disable, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_auth_digest_md5_disable, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_auth_ntlm_disable, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_auth_ntlm_domain, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_auth_ntlm_flags, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_submitter, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_dsn_notify, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_dsn_ret, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_allow8bitmime, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_sendpartial, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_sasl_enable, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_sasl_mechanisms, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_sasl_authorizationid, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_sasl_realm, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_sasl_usecanonicalhostname, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_quitwait, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_reportsuccess, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_socketFactory_class, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_socketFactory_fallback, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_socketFactory_port, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_starttls_enable, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_starttls_required, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_socks_host, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_socks_port, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_mailextension, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_userset, this.protocol);
		setProperties(this.properties, HinemosPropertyCommon.mail_$_noop_strict, this.protocol);

		setProperties(this.properties, HinemosPropertyCommon.mail_smtp_ssl_enable, "");
		setProperties(this.properties, HinemosPropertyCommon.mail_smtp_ssl_checkserveridentity, "");
		setProperties(this.properties, HinemosPropertyCommon.mail_smtp_ssl_trust, "");
		setProperties(this.properties, HinemosPropertyCommon.mail_smtp_ssl_socketFactory_class, "");
		setProperties(this.properties, HinemosPropertyCommon.mail_smtp_ssl_socketFactory_port, "");
		setProperties(this.properties, HinemosPropertyCommon.mail_smtp_ssl_protocols, "");
		setProperties(this.properties, HinemosPropertyCommon.mail_smtp_ssl_ciphersuites, "");

		this.loginUser = HinemosPropertyCommon.mail_transport_user.getStringValue();
		this.loginPassword = HinemosPropertyCommon.mail_transport_password.getStringValue();
		this.fromAddress = HinemosPropertyCommon.mail_from_address.getStringValue();
		this.fromPersonalName = convertNativeToAscii(HinemosPropertyCommon.mail_from_personal_name.getStringValue());
		this.replyToAddress = HinemosPropertyCommon.mail_reply_to_address.getStringValue();
		this.replyToPersonalName = convertNativeToAscii(
				HinemosPropertyCommon.mail_reply_personal_name.getStringValue());
		this.errorsToAddress = HinemosPropertyCommon.mail_errors_to_address.getStringValue();
		this.transportTries = HinemosPropertyCommon.mail_transport_tries.getIntegerValue();
		this.transportTriesInterval = HinemosPropertyCommon.mail_transport_tries_interval.getIntegerValue();
		this.charsetAddress = HinemosPropertyCommon.mail_charset_address.getStringValue();
		this.charsetSubject = HinemosPropertyCommon.mail_charset_subject.getStringValue();
		this.charsetContent = HinemosPropertyCommon.mail_charset_content.getStringValue();

		this.authMechanisms = HinemosPropertyCommon.mail_$_auth_mechanisms.getStringValue(this.protocol);
		this.mailHost = HinemosPropertyCommon.mail_$_host.getStringValue(this.protocol);
		this.refreshToken = getRefreshToken(HinemosPropertyCommon.mail_oauth_refresh_token_path.getStringValue(), 0);
		this.oauthResponseKey = HinemosPropertyCommon.mail_oauth_response_key.getStringValue();
		this.oauthUrl = HinemosPropertyCommon.mail_oauth_url.getStringValue();
		this.oauthParamKey1 = HinemosPropertyCommon.mail_oauth_param_key1.getStringValue();
		this.oauthParamKey2 = HinemosPropertyCommon.mail_oauth_param_key2.getStringValue();
		this.oauthParamKey3 = HinemosPropertyCommon.mail_oauth_param_key3.getStringValue();
		this.oauthParamKey4 = HinemosPropertyCommon.mail_oauth_param_key4.getStringValue();
		this.oauthParamKey5 = HinemosPropertyCommon.mail_oauth_param_key5.getStringValue();
		this.oauthParamKey6 = HinemosPropertyCommon.mail_oauth_param_key6.getStringValue();
		this.oauthParamKey7 = HinemosPropertyCommon.mail_oauth_param_key7.getStringValue();
		this.oauthParamKey8 = HinemosPropertyCommon.mail_oauth_param_key8.getStringValue();
		this.oauthParamKey9 = HinemosPropertyCommon.mail_oauth_param_key9.getStringValue();
		this.oauthParamKey10 = HinemosPropertyCommon.mail_oauth_param_key10.getStringValue();
		this.oauthParamValue1 = HinemosPropertyCommon.mail_oauth_param_value1.getStringValue();
		this.oauthParamValue2 = HinemosPropertyCommon.mail_oauth_param_value2.getStringValue();
		this.oauthParamValue3 = HinemosPropertyCommon.mail_oauth_param_value3.getStringValue();
		this.oauthParamValue4 = HinemosPropertyCommon.mail_oauth_param_value4.getStringValue();
		this.oauthParamValue5 = HinemosPropertyCommon.mail_oauth_param_value5.getStringValue();
		this.oauthParamValue6 = HinemosPropertyCommon.mail_oauth_param_value6.getStringValue();
		this.oauthParamValue7 = HinemosPropertyCommon.mail_oauth_param_value7.getStringValue();
		this.oauthParamValue8 = HinemosPropertyCommon.mail_oauth_param_value8.getStringValue();
		this.oauthParamValue9 = HinemosPropertyCommon.mail_oauth_param_value9.getStringValue();
		this.oauthParamValue10 = HinemosPropertyCommon.mail_oauth_param_value10.getStringValue();

		m_log.debug("MailSettings : settings " + this.toString());
	}

	public MailServerSettings(int slot) {
		String str = slot + "";

		this.protocol = HinemosPropertyCommon.mail_$_transport_protocol.getStringValue(str);
		this.properties = new Properties();

		this.properties.setProperty("mail.debug",
				Boolean.toString(HinemosPropertyCommon.mail_$_debug.getBooleanValue(str)));
		this.properties.setProperty("mail.store.protocol",
				HinemosPropertyCommon.mail_$_store_protocol.getStringValue(str));
		this.properties.setProperty("mail.transport.protocol", this.protocol);
		this.properties.put("mail.smtp.socketFactory", javax.net.SocketFactory.getDefault());
		this.properties.put("mail.smtp.ssl.socketFactory", javax.net.ssl.SSLSocketFactory.getDefault());

		this.loginUser = HinemosPropertyCommon.mail_$_transport_user.getStringValue(str);
		this.loginPassword = HinemosPropertyCommon.mail_$_transport_password.getStringValue(str);
		this.fromAddress = HinemosPropertyCommon.mail_$_from_address.getStringValue(str);
		this.fromPersonalName = convertNativeToAscii(
				HinemosPropertyCommon.mail_$_from_personal_name.getStringValue(str), str);
		this.replyToAddress = HinemosPropertyCommon.mail_$_reply_to_address.getStringValue(str);
		this.replyToPersonalName = convertNativeToAscii(
				HinemosPropertyCommon.mail_$_reply_personal_name.getStringValue(str), str);
		this.errorsToAddress = HinemosPropertyCommon.mail_$_errors_to_address.getStringValue(str);
		this.transportTries = HinemosPropertyCommon.mail_$_transport_tries.getIntegerValue(str);
		this.transportTriesInterval = HinemosPropertyCommon.mail_$_transport_tries_interval.getIntegerValue(str);
		this.charsetAddress = HinemosPropertyCommon.mail_$_charset_address.getStringValue(str);
		this.charsetSubject = HinemosPropertyCommon.mail_$_charset_subject.getStringValue(str);
		this.charsetContent = HinemosPropertyCommon.mail_$_charset_content.getStringValue(str);

		this.refreshToken = getRefreshToken(HinemosPropertyCommon.mail_$_oauth_refresh_token_path.getStringValue(str), slot);
		this.oauthResponseKey = HinemosPropertyCommon.mail_$_oauth_response_key.getStringValue(str);
		this.oauthUrl = HinemosPropertyCommon.mail_$_oauth_url.getStringValue(str);
		this.oauthParamKey1 = HinemosPropertyCommon.mail_$_oauth_param_key1.getStringValue(str);
		this.oauthParamKey2 = HinemosPropertyCommon.mail_$_oauth_param_key2.getStringValue(str);
		this.oauthParamKey3 = HinemosPropertyCommon.mail_$_oauth_param_key3.getStringValue(str);
		this.oauthParamKey4 = HinemosPropertyCommon.mail_$_oauth_param_key4.getStringValue(str);
		this.oauthParamKey5 = HinemosPropertyCommon.mail_$_oauth_param_key5.getStringValue(str);
		this.oauthParamKey6 = HinemosPropertyCommon.mail_$_oauth_param_key6.getStringValue(str);
		this.oauthParamKey7 = HinemosPropertyCommon.mail_$_oauth_param_key7.getStringValue(str);
		this.oauthParamKey8 = HinemosPropertyCommon.mail_$_oauth_param_key8.getStringValue(str);
		this.oauthParamKey9 = HinemosPropertyCommon.mail_$_oauth_param_key9.getStringValue(str);
		this.oauthParamKey10 = HinemosPropertyCommon.mail_$_oauth_param_key10.getStringValue(str);
		this.oauthParamValue1 = HinemosPropertyCommon.mail_$_oauth_param_value1.getStringValue(str);
		this.oauthParamValue2 = HinemosPropertyCommon.mail_$_oauth_param_value2.getStringValue(str);
		this.oauthParamValue3 = HinemosPropertyCommon.mail_$_oauth_param_value3.getStringValue(str);
		this.oauthParamValue4 = HinemosPropertyCommon.mail_$_oauth_param_value4.getStringValue(str);
		this.oauthParamValue5 = HinemosPropertyCommon.mail_$_oauth_param_value5.getStringValue(str);
		this.oauthParamValue6 = HinemosPropertyCommon.mail_$_oauth_param_value6.getStringValue(str);
		this.oauthParamValue7 = HinemosPropertyCommon.mail_$_oauth_param_value7.getStringValue(str);
		this.oauthParamValue8 = HinemosPropertyCommon.mail_$_oauth_param_value8.getStringValue(str);
		this.oauthParamValue9 = HinemosPropertyCommon.mail_$_oauth_param_value9.getStringValue(str);
		this.oauthParamValue10 = HinemosPropertyCommon.mail_$_oauth_param_value10.getStringValue(str);

		if ("smtps".equals(this.protocol)) {
			this.authFlag = HinemosPropertyCommon.mail_$_smtps_auth.getBooleanValue(str, false);
			this.authMechanisms = HinemosPropertyCommon.mail_$_smtps_auth_mechanisms.getStringValue(str);
			this.mailHost = HinemosPropertyCommon.mail_$_smtps_host.getStringValue(str);

			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_user, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_host, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_port, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_connectiontimeout, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_timeout, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_writetimeout, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_from, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_localhost, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_localaddress, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_localport, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_ehlo, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_auth, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_auth_mechanisms, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_auth_login_disable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_auth_plain_disable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_auth_digest_md5_disable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_auth_ntlm_disable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_auth_ntlm_domain, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_auth_ntlm_flags, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_submitter, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_dsn_notify, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_dsn_ret, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_allow8bitmime, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_sendpartial, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_sasl_enable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_sasl_mechanisms, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_sasl_authorizationid, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_sasl_realm, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_sasl_usecanonicalhostname, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_quitwait, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_reportsuccess, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_socketFactory_class, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_socketFactory_fallback, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_socketFactory_port, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_starttls_enable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_starttls_required, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_socks_host, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_socks_port, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_mailextension, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_userset, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtps_noop_strict, str);

			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_enable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_checkserveridentity, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_trust, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_socketFactory_class, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_socketFactory_port, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_protocols, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_ciphersuites, str);
		} else {
			this.authFlag = HinemosPropertyCommon.mail_$_smtp_auth.getBooleanValue(str, false);
			this.authMechanisms = HinemosPropertyCommon.mail_$_smtp_auth_mechanisms.getStringValue(str);
			this.mailHost = HinemosPropertyCommon.mail_$_smtp_host.getStringValue(str);

			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_user, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_host, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_port, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_connectiontimeout, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_timeout, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_writetimeout, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_from, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_localhost, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_localaddress, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_localport, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ehlo, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_auth, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_auth_mechanisms, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_auth_login_disable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_auth_plain_disable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_auth_digest_md5_disable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_auth_ntlm_disable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_auth_ntlm_domain, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_auth_ntlm_flags, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_submitter, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_dsn_notify, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_dsn_ret, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_allow8bitmime, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_sendpartial, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_sasl_enable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_sasl_mechanisms, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_sasl_authorizationid, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_sasl_realm, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_sasl_usecanonicalhostname, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_quitwait, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_reportsuccess, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_socketFactory_class, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_socketFactory_fallback, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_socketFactory_port, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_starttls_enable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_starttls_required, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_socks_host, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_socks_port, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_mailextension, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_userset, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_noop_strict, str);

			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_enable, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_checkserveridentity, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_trust, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_socketFactory_class, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_socketFactory_port, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_protocols, str);
			setProperties(this.properties, HinemosPropertyCommon.mail_$_smtp_ssl_ciphersuites, str);
		}

		m_log.debug("MailSettings : settings with slot " + this.toString());
	}

	public Properties getProperties() {
		return properties;
	}

	public String getLoginUser() {
		return loginUser;
	}

	public String getLoginPassword() {
		return loginPassword;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public String getFromPersonalName() {
		return fromPersonalName;
	}

	public String getReplyToAddress() {
		return replyToAddress;
	}

	public String getReplyToPersonalName() {
		return replyToPersonalName;
	}

	public String getErrorsToAddress() {
		return errorsToAddress;
	}

	public int getTransportTries() {
		return transportTries;
	}

	public int getTransportTriesInterval() {
		return transportTriesInterval;
	}

	public String getCharsetAddress() {
		return charsetAddress;
	}

	public String getCharsetSubject() {
		return charsetSubject;
	}

	public String getCharsetContent() {
		return charsetContent;
	}

	public String getProtocol() {
		return protocol;
	}
	
	public Boolean getAuthFlag() {
		return authFlag;
	}

	public String getAuthMechanisms() {
		return authMechanisms;
	}

	public String getMailHost() {
		return mailHost;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public String getOauthUrl() {
		return oauthUrl;
	}

	public String getOauthResponseKey() {
		return oauthResponseKey;
	}
	
	public String getOauthParamKey1() {
		return oauthParamKey1;
	}

	public String getOauthParamKey2() {
		return oauthParamKey2;
	}

	public String getOauthParamKey3() {
		return oauthParamKey3;
	}

	public String getOauthParamKey4() {
		return oauthParamKey4;
	}

	public String getOauthParamKey5() {
		return oauthParamKey5;
	}

	public String getOauthParamKey6() {
		return oauthParamKey6;
	}

	public String getOauthParamKey7() {
		return oauthParamKey7;
	}

	public String getOauthParamKey8() {
		return oauthParamKey8;
	}

	public String getOauthParamKey9() {
		return oauthParamKey9;
	}

	public String getOauthParamKey10() {
		return oauthParamKey10;
	}

	public String getOauthParamValue1() {
		return oauthParamValue1;
	}

	public String getOauthParamValue2() {
		return oauthParamValue2;
	}

	public String getOauthParamValue3() {
		return oauthParamValue3;
	}

	public String getOauthParamValue4() {
		return oauthParamValue4;
	}

	public String getOauthParamValue5() {
		return oauthParamValue5;
	}

	public String getOauthParamValue6() {
		return oauthParamValue6;
	}

	public String getOauthParamValue7() {
		return oauthParamValue7;
	}

	public String getOauthParamValue8() {
		return oauthParamValue8;
	}

	public String getOauthParamValue9() {
		return oauthParamValue9;
	}

	public String getOauthParamValue10() {
		return oauthParamValue10;
	}

	@Override
	public String toString() {
		return "MailSettings [loginUser=" + loginUser + ", loginPassword=" + loginPassword + ", fromAddress="
				+ fromAddress + ", fromPersonalName=" + fromPersonalName + ", replyToAddress=" + replyToAddress
				+ ", replyToPersonalName=" + replyToPersonalName + ", errorsToAddress=" + errorsToAddress
				+ ", transportTries=" + transportTries + ", transportTriesInterval=" + transportTriesInterval
				+ ", charsetAddress=" + charsetAddress + ", charsetSubject=" + charsetSubject + ", charsetContent="
				+ charsetContent + ", protocol=" + protocol + ", authFlag=" + authFlag + "]";
	}

	private String convertNativeToAscii(String nativeStr) {
		if (HinemosPropertyCommon.mail_native_to_ascii.getBooleanValue()) {
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

	private String convertNativeToAscii(String nativeStr, String replaceStr) {
		if (HinemosPropertyCommon.mail_$_native_to_ascii.getBooleanValue(replaceStr)) {
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

	private void setProperties(Properties prop, HinemosPropertyCommon hinemosPropertyCommon, String replaceStr) {
		// マルチSMTPサーバ対応のための"mail.X.smtp.server"といったキーを
		// JavaMailが解釈できる"mail.smtp.server"の形式のキーに変換
		String propKeyForJavaMail = hinemosPropertyCommon.getReplaceKey(replaceStr)
				.replaceAll("(mail\\.)[0-9]{0,2}\\.?(.*)", "$1$2");

		m_log.debug("setProperties() : remove slot number from property key for MultiSnmtp, "
				+ hinemosPropertyCommon.getReplaceKey(replaceStr) + " -> " + propKeyForJavaMail);

		switch (hinemosPropertyCommon.getBean().getType()) {
		case HinemosPropertyTypeConstant.TYPE_STRING:
			String strVal = hinemosPropertyCommon.getStringValue(replaceStr);
			if (strVal != null) {
				prop.setProperty(propKeyForJavaMail, strVal);
			}
			break;
		case HinemosPropertyTypeConstant.TYPE_NUMERIC:
			Long longVal = hinemosPropertyCommon.getNumericValue(replaceStr);
			if (longVal != null) {
				prop.setProperty(propKeyForJavaMail, longVal.toString());
			}
			break;
		case HinemosPropertyTypeConstant.TYPE_TRUTH:
			Boolean boolVal = hinemosPropertyCommon.getBooleanValue(replaceStr);
			if (boolVal != null) {
				prop.setProperty(propKeyForJavaMail, boolVal.toString());
			}
			break;
		default:
			// 上記以外はなにもしない
			break;
		}
	}

	/**
	 * リフレッシュトークンを取得します。
	 * 
	 * @param refreshTokenPath リフレッシュトークンパス
	 * @param slot 指数
	 * @return リフレッシュトークン
	 */
	private String getRefreshToken(String refreshTokenPath, int slot) {
		if (refreshTokenPath ==null || refreshTokenPath.isEmpty()) {
			m_log.info("refresh_token_path is not set. " + refreshTokenPath);
			return null;
		}

		String fileName = "";
		if (slot > 0 && slot < 11) {
			String str = slot + "";
			fileName = REFRESH_TOKEN_FILES_NAME.replace("$", str);
		} else {
			fileName = REFRESH_TOKEN_FILE_NAME;
		}

		String homedir = normalize(refreshTokenPath);
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		String refreshToken = null;

		String path = homedir + File.separator + fileName;

		try {
			fis = new FileInputStream(path);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);

			// 取得するのは1行目だけ
			refreshToken = br.readLine();
		} catch (FileNotFoundException e) {
			m_log.warn("configuration file not found. [" + refreshTokenPath + "]"
					+ e.getClass().getName() + ", " + e.getMessage());
		} catch (IOException e) {
			m_log.warn("configuration read error. [" + refreshTokenPath +"]"
					+ e.getClass().getName() + ", " + e.getMessage());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
		return refreshToken;
	}

	// 
	/**
	 * ファイルセパレータをマネージャ側の形式に統一します。
	 * 
	 * @param filepath ファイルパス
	 * @return 統一したファイルパス
	 */
	private static String normalize(String filepath) {
		return filepath.replace("/", File.separator).replace("\\", File.separator);
	}
}
