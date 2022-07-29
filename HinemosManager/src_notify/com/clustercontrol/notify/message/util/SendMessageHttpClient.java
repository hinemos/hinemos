/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.message.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;

import com.clustercontrol.http.util.Response;
import com.clustercontrol.util.HinemosTime;

/**
 * メッセージ通知のHTTPアクセスを実施するクライアントクラス
 */
public class SendMessageHttpClient implements Closeable {

	public static class SendMessageClientBuilder {

		/** プロキシサーバ スキーム */
		private String proxyScheme;

		/** プロキシサーバ ホスト名 */
		private String proxyHost;

		/** プロキシサーバ ポート */
		private int proxyPort;

		/** プロキシユーザ */
		private String proxyUser;

		/** プロキシパスワード */
		private String proxyPassword;

		/** プロキシキャッシュ回避 */
		private boolean cancelProxyCache;

		/** サーバー側 SSL 証明書認証 */
		private boolean needAuthSSLCert;

		/** 接続タイムアウト */
		private int connectTimeout;

		/** リクエストタイムアウト */
		private int requestTimeout;

		/** ユーザーエージェント */
		private String userAgent;

		/** キープアライブ */
		private boolean keepAlive = true;

		/** Bearer認証用トークン（メッセージ通知で使用） */
		private String bearerToken;

		/** プロキシURL */
		public SendMessageClientBuilder setProxyURL(String proxyURL) throws URISyntaxException {
			if (proxyURL != null) {
				URI uri = new URI(proxyURL);
				setProxyScheme(uri.getScheme());
				setProxyHost(uri.getHost());
			}
			return this;
		}

		/** プロキシスキーム */
		public SendMessageClientBuilder setProxyScheme(String proxyScheme) {
			this.proxyScheme = proxyScheme;
			return this;
		}

		/** プロキシサーバ ホスト名 */
		public SendMessageClientBuilder setProxyHost(String proxyHost) {
			this.proxyHost = proxyHost;
			return this;
		}

		/** プロキシサーバ ポート */
		public SendMessageClientBuilder setProxyPort(int proxyPort) {
			this.proxyPort = proxyPort;
			return this;
		}

		/** プロキシサーバ ポート */
		public SendMessageClientBuilder setProxyUser(String proxyUser) {
			this.proxyUser = proxyUser;
			return this;
		}

		/** プロキシパスワード */
		public SendMessageClientBuilder setProxyPassword(String proxyPassword) {
			this.proxyPassword = proxyPassword;
			return this;
		}

		/** サーバー側 SSL 証明書認証 */
		public SendMessageClientBuilder setNeedAuthSSLCert(boolean needAuthSSLCert) {
			this.needAuthSSLCert = needAuthSSLCert;
			return this;
		}

		/** 接続タイムアウト */
		public SendMessageClientBuilder setConnectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
			return this;
		}

		/** リクエストタイムアウト */
		public SendMessageClientBuilder setRequestTimeout(int requestTimeout) {
			this.requestTimeout = requestTimeout;
			return this;
		}

		/** ユーザーエージェント */
		public SendMessageClientBuilder setUserAgent(String userAgent) {
			this.userAgent = userAgent;
			return this;
		}

		public SendMessageClientBuilder setKeepAlive(boolean keepAlive) {
			this.keepAlive = keepAlive;
			return this;
		}

		public SendMessageClientBuilder setCancelProxyCache(boolean cancelProxyCache) {
			this.cancelProxyCache = cancelProxyCache;
			return this;
		}

		/** Bearer認証用トークン */
		public SendMessageClientBuilder setBearerToken(String bearerToken) {
			this.bearerToken = bearerToken;
			return this;
		}

		public SendMessageHttpClient build() {
			return new SendMessageHttpClient(proxyScheme, proxyHost, proxyPort, proxyUser, proxyPassword,
					needAuthSSLCert, connectTimeout, requestTimeout, userAgent, keepAlive, cancelProxyCache,
					bearerToken);
		}
	}

	public static enum AuthType {
		BASIC, NTLM, DIGEST, NONE
	};

	private static final Log m_log = LogFactory.getLog(SendMessageHttpClient.class);

	private static final int BUFF_SIZE = 1024 * 1024;

	private static final int BODY_MAX_SIZE = 5 * BUFF_SIZE;

	private CloseableHttpClient m_client;

	private CredentialsStore m_cledentialProvider = new BasicCredentialsProvider();

	/** プロキシサーバ スキーム */
	private String m_proxyScheme;

	/** プロキシサーバ ホスト名 */
	private String m_proxyHost;

	/** プロキシサーバ ポート */
	private int m_proxyPort;

	/** プロキシユーザ */
	private String m_proxyUser;

	/** プロキシパスワード */
	private String m_proxyPassword;

	/** プロキシキャッシュ回避 */
	private boolean m_cancelProxyCache;

	/** サーバー側 SSL 証明書認証 */
	private boolean m_needAuthSSLCert;

	/** 接続タイムアウト */
	private int m_connectTimeout;

	/** リクエストタイムアウト */
	private int m_requestTimeout;

	/** ユーザーエージェント */
	private String m_userAgent;

	/** キープアライブ */
	private boolean keepAlive = true;

	/** Bearer認証用トークン（メッセージ通知で使用） */
	private String m_bearerToken;

	/** レスポンス */
	private Response m_requestResult = new Response();

	/**
	 * コンストラクタ
	 */
	public SendMessageHttpClient() {
		super();
	}

	/**
	 * コンストラクタ
	 */
	public SendMessageHttpClient(String proxyScheme, String proxyHost, int proxyPort, String proxyUser,
			String proxyPassword, boolean needAuthSSLCert, int connectTimeout, int requestTimeout, String userAgent,
			boolean keepAlive, boolean cancelProxyCache, String bearerToken) {
		super();
		this.setProxyScheme(proxyScheme);
		this.setProxyUser(proxyUser);
		this.setProxyHost(proxyHost);
		this.setProxyPort(proxyPort);
		this.setProxyUser(proxyUser);
		this.setProxyPassword(proxyPassword);
		this.setNeedAuthSSLCert(needAuthSSLCert);
		this.setConnectTimeout(connectTimeout);
		this.setRequestTimeout(requestTimeout);
		this.setUserAgent(userAgent);
		this.setKeepAlive(keepAlive);
		this.setCancelProxyCache(cancelProxyCache);
		this.setBearerToken(bearerToken);
	}

	/**
	 * リクエストURLから情報取得
	 * 
	 * @param url URL
	 * @param hinemosMessage JSON文字列
	 * @return
	 */
	public boolean execute(String url, String hinemosMessage)
			throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		Response result = new Response();
		result.url = url;
		CloseableHttpClient client = getHttpClient();
		HttpUriRequestBase request = null;

		if (m_log.isTraceEnabled()) {
			m_log.trace("url=" + url + ", hinemosMessage=" + hinemosMessage);
		}

		HttpPost requestPost = new HttpPost(url);
		requestPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		requestPost.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + m_bearerToken);
		requestPost.setEntity(new StringEntity(hinemosMessage, StandardCharsets.UTF_8));
		request = requestPost;

		// Execute the method.
		try {
			long start = HinemosTime.currentTimeMillis();
			CloseableHttpResponse response = client.execute(request);
			result.responseTime = HinemosTime.currentTimeMillis() - start;

			result.statusCode = response.getCode();

			if (result.statusCode == HttpStatus.SC_OK) {
				result.success = true;
			} else {
				// 失敗した場合はレスポンスボディを取得しておく
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try (InputStream in = response.getEntity().getContent()) {
					byte[] buffer = new byte[BUFF_SIZE];
					while (out.size() < BODY_MAX_SIZE) {
						int len = in.read(buffer);
						if (len < 0) {
							break;
						}
						out.write(buffer, 0, len);
					}
				}
				result.responseBody = new String(out.toByteArray());
			}
		} finally {
			request.reset();
		}

		m_requestResult = result;
		return m_requestResult.success;
	}

	private CloseableHttpClient getHttpClient()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		if (m_client == null) {
			List<Header> headers = new ArrayList<>();
			PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder
					.create();

			HttpClientBuilder builder = HttpClients.custom().setDefaultCredentialsProvider(m_cledentialProvider)
					.setDefaultHeaders(headers);

			if (!this.m_needAuthSSLCert) {
				// SSL の認証カット
				TrustStrategy trustStrategy = new TrustStrategy() {
					@Override
					public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						return true;
					}
				};
				connectionManagerBuilder.setSSLSocketFactory(new SSLConnectionSocketFactory(
						new SSLContextBuilder().loadTrustMaterial(null, trustStrategy).build(),
						new NoopHostnameVerifier()));
			}
			RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(StandardCookieSpec.RELAXED)
					.setConnectTimeout(Timeout.ofMilliseconds(m_connectTimeout))
					.setResponseTimeout(Timeout.ofMilliseconds(m_requestTimeout)).build();
			builder.setDefaultRequestConfig(requestConfig);

			// デフォルトだと無限待ちの場合があるので、ソケットのブロッキング待ちタイムアウト時間をリクエスト待ちと合わせる
			SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(Timeout.ofMilliseconds(m_requestTimeout))
					.build();
			connectionManagerBuilder.setDefaultSocketConfig(socketConfig);

			// プロキシ情報がある場合のみ、プロキシ使用する
			if (m_proxyHost != null && !m_proxyHost.isEmpty() && m_proxyPort != 0) {
				HttpHost proxy;
				if (m_proxyScheme == null) {
					proxy = new HttpHost("https");
				} else {
					proxy = new HttpHost(m_proxyScheme, m_proxyHost, m_proxyPort);
				}

				if (m_proxyUser != null && m_proxyPassword != null) {
					m_cledentialProvider.setCredentials(new AuthScope(proxy.getHostName(), proxy.getPort()),
							new UsernamePasswordCredentials(m_proxyUser, m_proxyPassword.toCharArray()));
				}
				builder.setProxy(proxy);
			}

			if (m_userAgent != null) {
				headers.add(new BasicHeader(HttpHeaders.USER_AGENT, m_userAgent));
			}

			if (m_cancelProxyCache) {
				// プロキシのキャッシュコントロール
				// https://www.ipa.go.jp/security/awareness/vendor/programmingv2/contents/405.html
				headers.add(new BasicHeader("Cache-Control", "no-cache"));
				headers.add(new BasicHeader("Pragma", "no-cache"));
			}

			if (keepAlive) {
				headers.add(new BasicHeader(HttpHeaders.CONNECTION, HttpHeaders.KEEP_ALIVE));
			} else {
				headers.add(new BasicHeader(HttpHeaders.CONNECTION, "Close"));
			}
			m_client = builder.setConnectionManager(connectionManagerBuilder.build()).build();
		}

		return m_client;
	}

	/**
	 * @return m_header を戻します。
	 */
	public String getHeaderString() {
		return m_requestResult.headerString;
	}

	/**
	 * @return m_header を戻します。
	 */
	public List<Header> getHeaders() {
		return m_requestResult.headers;
	}

	/**
	 * @return m_responseBody を戻します。
	 */
	public String getResponseBody() {
		return m_requestResult.responseBody;
	}

	/**
	 * @return m_responseTime を戻します。
	 */
	public long getResponseTime() {
		return m_requestResult.responseTime;
	}

	/**
	 * @return m_statusCode を戻します。
	 */
	public int getStatusCode() {
		return m_requestResult.statusCode;
	}

	/**
	 * @return m_errorMessage を戻します。
	 */
	public String getErrorMessage() {
		return m_requestResult.errorMessage;
	}

	public Response getResult() {
		return m_requestResult;
	}

	private void setProxyHost(String proxyHost) {
		this.m_proxyHost = proxyHost;
	}

	private void setProxyPort(int proxyPort) {
		this.m_proxyPort = proxyPort;
	}

	private void setProxyUser(String proxyUser) {
		this.m_proxyUser = proxyUser;
	}

	private void setProxyPassword(String proxyPassword) {
		this.m_proxyPassword = proxyPassword;
	}

	private void setNeedAuthSSLCert(boolean needAuthSSLCert) {
		this.m_needAuthSSLCert = needAuthSSLCert;
	}

	private void setConnectTimeout(int connectTimeout) {
		this.m_connectTimeout = connectTimeout;
	}

	private void setRequestTimeout(int requestTimeout) {
		this.m_requestTimeout = requestTimeout;
	}

	private void setUserAgent(String userAgent) {
		this.m_userAgent = userAgent;
	}

	private void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	private void setProxyScheme(String proxyScheme) {
		this.m_proxyScheme = proxyScheme;
	}

	private void setCancelProxyCache(boolean cancelProxyCache) {
		this.m_cancelProxyCache = cancelProxyCache;
	}

	private void setBearerToken(String bearerToken) {
		this.m_bearerToken = bearerToken;
	}

	public static SendMessageClientBuilder custom() {
		return new SendMessageClientBuilder();
	}

	@Override
	public void close() throws IOException {
		if (m_client != null) {
			m_client.close();
		}
	}
}
