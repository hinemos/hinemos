/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import com.burgstaller.okhttp.digest.fromhttpclient.HTTP;
import com.clustercontrol.commons.util.AccessTokenCache.AccessTokenValue;
import com.clustercontrol.util.HinemosTime;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OAuth認証処理でメールを送信するクラス<BR>
 *
 * @version 6.2.2
 * @since 6.2.2
 */
public class SendOAuthMail {
	
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(SendOAuthMail.class);

	/** 認証メカニズム_XOAUTH2 */
	public static final String STRING_AOUTH_MECHANISMS_XOAUTH2 = "XOAUTH2";
	/** リフレッシュトークン値_key項目と紐づけるための設定値 */
	private static final String STRING_REFRESH_TOKEN_VALUE = "refresh_token_value";

	/**
	 * OAuth認証によりメールを送信します。
	 * 
	 * @param transport メール設定
	 * @param mailServerSettings メール設定情報
	 * @param mineMsg 送信内容
	 */
	public void sendOAuthMail(Transport transport, MailServerSettings mailServerSettings, Message mineMsg) throws MessagingException, OAuthException {
		m_log.debug("sendOAuthMail() : start");
		
		boolean authFlag = mailServerSettings.getAuthFlag();
		String authMechanisms = mailServerSettings.getAuthMechanisms();
		String refreshToken = mailServerSettings.getRefreshToken();
		// mail.smtp.auth（SMTP AUTHの利用有無）がtrue 
		// かつ、mail_oauth_mechanismsがXOAUTH2の際にOauth2.0認証をする
		if (authFlag && STRING_AOUTH_MECHANISMS_XOAUTH2.equals(authMechanisms)) {
			if (isEmpty(refreshToken)) {
				// リフレッシュトークンが設定されていない場合は通知
				throw new OAuthException("Refresh token is not set.");
			}
			// アクセストークン取得処理
			String accessToken = "";
			try {
				accessToken = getAccessToken(refreshToken, mailServerSettings);
			} catch (OAuthException e) {
				throw e;
			} catch (Exception e) {
				// エラー時はアクセスコードの再取得
				try {
					Thread.sleep(10000L);
					// キャッシュに登録されているアクセスコードを削除
					AccessTokenCache.remove(refreshToken);
					accessToken = getAccessToken(refreshToken, mailServerSettings);
				} catch(Exception ae)  {
					m_log.warn("getAccessToken() : Unable to obtain a access token: " + e.getMessage());
					throw new OAuthException("Could not get access token.");
				}
			}
			if (isEmpty(accessToken)){
				// アクセストークンが取得できない場合は通知
				throw new OAuthException("Could not get access token.");
			}

			// メール送信処理
			try {
				sendMail(transport, mailServerSettings, mineMsg, accessToken);
			} catch (AuthenticationFailedException e) {
				m_log.debug("sendOAuthMail() : send error. " + e.getMessage());
				// キャッシュに登録されているアクセスコードを削除し、アクセストークンの再取得
				AccessTokenCache.remove(refreshToken);
				try {
					accessToken = getAccessToken(refreshToken, mailServerSettings);
				} catch (OAuthException ae) {
					throw ae;
				} catch (Exception ae) {
					// エラー時はアクセスコードの再取得
					try {
						Thread.sleep(10000L);
						accessToken = getAccessToken(refreshToken, mailServerSettings);
					} catch(Exception ace)  {
						m_log.warn("getAccessToken() : Unable to obtain a access token: " + ace.getMessage());
						throw new OAuthException("Could not get access token.");
					}
				}

				if (isEmpty(accessToken)){
					// アクセストークンが取得できない場合は通知
					throw new OAuthException("Could not get access token.");
				}
				sendMail(transport, mailServerSettings, mineMsg, accessToken);
			} catch (MessagingException me) {
				throw me;
			}
		}
		m_log.debug("sendOAuthMail() : end");
	}

	/**
	 * アクセストークンを取得します。
	 * 
	 * @return アクセストークン
	 * @throws Exception
	 */
	private String getAccessToken(String refreshToken, MailServerSettings mailServerSettings) throws Exception {
		m_log.debug("getAccessToken() : start");

		AccessTokenValue valueEntity = AccessTokenCache.getAccessTokenValue(refreshToken);
		String accessToken = valueEntity.getAccessToken();
		if (isNotEmpty(accessToken)) {
			// 既にアクセストークンがキャッシュに登録されているのであれば後続処理は不要
			return accessToken;
		}

		// urlパラメータ作成
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		setUrlParameters(urlParameters, refreshToken, mailServerSettings.getOauthParamKey1(), mailServerSettings.getOauthParamValue1());
		setUrlParameters(urlParameters, refreshToken, mailServerSettings.getOauthParamKey2(), mailServerSettings.getOauthParamValue2());
		setUrlParameters(urlParameters, refreshToken, mailServerSettings.getOauthParamKey3(), mailServerSettings.getOauthParamValue3());
		setUrlParameters(urlParameters, refreshToken, mailServerSettings.getOauthParamKey4(), mailServerSettings.getOauthParamValue4());
		setUrlParameters(urlParameters, refreshToken, mailServerSettings.getOauthParamKey5(), mailServerSettings.getOauthParamValue5());
		setUrlParameters(urlParameters, refreshToken, mailServerSettings.getOauthParamKey6(), mailServerSettings.getOauthParamValue6());
		setUrlParameters(urlParameters, refreshToken, mailServerSettings.getOauthParamKey7(), mailServerSettings.getOauthParamValue7());
		setUrlParameters(urlParameters, refreshToken, mailServerSettings.getOauthParamKey8(), mailServerSettings.getOauthParamValue8());
		setUrlParameters(urlParameters, refreshToken, mailServerSettings.getOauthParamKey9(), mailServerSettings.getOauthParamValue9());
		setUrlParameters(urlParameters, refreshToken, mailServerSettings.getOauthParamKey10(), mailServerSettings.getOauthParamValue10());

		String oauthUrl = mailServerSettings.getOauthUrl();
		if (isEmpty(oauthUrl)) {
			m_log.warn("mail_oauth_url is not set. ");
			return accessToken;
		}
		HttpPost requestPost = new HttpPost(oauthUrl);
		requestPost.addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
		requestPost.setEntity(new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8));

		long start = HinemosTime.currentTimeMillis();

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(requestPost);

		long responseTime = HinemosTime.currentTimeMillis() -start;
		int statusCode = response.getCode();
		String result = EntityUtils.toString(response.getEntity(), "UTF-8");
		m_log.debug(String.format("getAccessToken() : url=%s, code=%d", oauthUrl, statusCode));

		if (statusCode == HttpStatus.SC_OK) {
			m_log.debug(String.format("getAccessToken() : url=%s, success=%s, responseTime=%d", oauthUrl, response.toString(), responseTime));

			try {
				JsonNode jsonResult = new ObjectMapper().readTree(result);
				accessToken = jsonResult.get(mailServerSettings.getOauthResponseKey()).textValue();
			} catch (Exception e) {
				m_log.warn("getAccessToken() : Unable to obtain a access token: " + e.getMessage());
			}
		} else{
			// ステータスコード200以外はエラーとし、リトライもしない
			throw new OAuthException(String.format("http status code isn't 200. code=%d, message=%s", response.getCode(), response.toString()));
		}

		// アクセスコード取得後はキャッシュに登録
		if (isNotEmpty(accessToken)) {
			valueEntity.setAccessToken(accessToken);
			AccessTokenCache.update(refreshToken, valueEntity);
		}

		m_log.debug("getAccessToken() : end");
		return accessToken;
	}

	/**
	 * メールを送信します。
	 * 
	 * @param transport
	 * @param mailServerSettings
	 * @param mineMsg
	 * @param accessToken
	 * @throws MessagingException
	 * @throws AddressException
	 * @throws NoSuchProviderException
	 */
	private void sendMail(Transport transport, MailServerSettings mailServerSettings, Message mineMsg, String accessToken) throws MessagingException, AddressException, NoSuchProviderException {

		// メール送信処理(プロパティ設定はMailServerSettingsで行っているため不要)
		String host = mailServerSettings.getMailHost();
		String user = mailServerSettings.getLoginUser();

		try {
			// メール送信
			transport.connect(host, user, accessToken);
			transport.sendMessage(mineMsg, mineMsg.getAllRecipients());
		} catch (AuthenticationFailedException me) {
			m_log.warn("sendMail() : send error. " + me.getMessage());
			throw me;
		} catch (Exception e) {
			m_log.warn("sendMail() : send error. " + e.getMessage());
			throw e;
		} finally {
			if (transport != null) {
				transport.close();
			}
		}
	}

	/**
	 * 空文字列か判定します。
	 * 
	 * @param str 文字列
	 * @return true:空(null含む)、false:空ではない
	 */
	private boolean isEmpty(String str) {
		if (str == null || str.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * 有効な文字列か判定します。
	 * 
	 * @param str 文字列
	 * @return true:空ではない、false:空(null含む)
	 */
	private boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * アクセスコードを取得する際のUrlパラメータに有効な値を設定します。
	 * 
	 * @param urlParameters Urlパラメータ
	 * @param key パラメータkey
	 * @param value パラメータvalue
	 */
	private void setUrlParameters(List<NameValuePair> urlParameters, String refreshToken, String key, String value) {
		// valueは空欄を設定する可能性を考えて空を許容する
		if (isNotEmpty(key)) {
			// valueに"refresh_token_value"が入っていた場合は、keyがリフレッシュトークンに該当するものとしてパラメータを設定する。
			// keyが"refresh_token"かはチェックしない（リフレッシュトークンに該当するものが、key=refresh_tokenとは限らないため）
			if (STRING_REFRESH_TOKEN_VALUE.equals(value)) {
				urlParameters.add(new BasicNameValuePair(key, refreshToken));
			} else {
				urlParameters.add(new BasicNameValuePair(key, value));
			}
		}
	}
}