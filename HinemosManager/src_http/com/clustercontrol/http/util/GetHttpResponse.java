/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * HTTPアクセスを実施するクライアントクラス<BR>
 *
 * @version 5.0.0
 * @since 2.1.0
 */
public class GetHttpResponse implements Closeable {
	public static class GetHttpResponseBuilder {
		/** 認証種別 */
		private AuthType authType;

		/** 認証ユーザ */
		private String authUser;

		/** 認証パスワード */
		private String authPassword;

		/** プロキシサーバ　スキーム */
		private String proxyScheme;

		/** プロキシサーバ　ホスト名 */
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
		private int connectTimeout = 1000;

		/** リクエストタイムアウト  */
		private int requestTimeout = 1000;

		/** ユーザーエージェント */
		private String userAgent;

		/** キープアライブ */
		private boolean keepAlive = true;

		/** 認証種別 */
		public GetHttpResponseBuilder setAuthType(AuthType type) {
			this.authType = type;
			return this;
		}

		/** 認証ユーザ */
		public GetHttpResponseBuilder setAuthUser(String authUser) {
			this.authUser = authUser;
			return this;
		}

		/** 認証パスワード */
		public GetHttpResponseBuilder setAuthPassword(String authPassword) {
			this.authPassword = authPassword;
			return this;
		}

		/** プロキシURL */
		public GetHttpResponseBuilder setProxyURL(String proxyURL) throws URISyntaxException {
			if (proxyURL != null) {
				URI uri = new URI(proxyURL);
				setProxyScheme(uri.getScheme());
				setProxyHost(uri.getHost());
			}
			return this;
		}

		/** プロキシスキーム */
		public GetHttpResponseBuilder setProxyScheme(String proxyScheme) {
			this.proxyScheme = proxyScheme;
			return this;
		}

		/** プロキシサーバ　ホスト名 */
		public GetHttpResponseBuilder setProxyHost(String proxyHost) {
			this.proxyHost = proxyHost;
			return this;
		}

		/** プロキシサーバ ポート */
		public GetHttpResponseBuilder setProxyPort(int proxyPort) {
			this.proxyPort = proxyPort;
			return this;
		}

		/** プロキシサーバ ポート */
		public GetHttpResponseBuilder setProxyUser(String proxyUser) {
			this.proxyUser = proxyUser;
			return this;
		}

		/** プロキシパスワード */
		public GetHttpResponseBuilder setProxyPassword(String proxyPassword) {
			this.proxyPassword = proxyPassword;
			return this;
		}

		/** サーバー側 SSL 証明書認証 */
		public GetHttpResponseBuilder setNeedAuthSSLCert(boolean needAuthSSLCert) {
			this.needAuthSSLCert = needAuthSSLCert;
			return this;
		}

		/** 接続タイムアウト */
		public GetHttpResponseBuilder setConnectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
			return this;
		}

		/** リクエストタイムアウト  */
		public GetHttpResponseBuilder setRequestTimeout(int requestTimeout) {
			this.requestTimeout = requestTimeout;
			return this;
		}

		/** ユーザーエージェント */
		public GetHttpResponseBuilder setUserAgent(String userAgent) {
			this.userAgent = userAgent;
			return this;
		}

		public GetHttpResponseBuilder setKeepAlive(boolean keepAlive) {
			this.keepAlive = keepAlive;
			return this;
		}

		public GetHttpResponseBuilder setCancelProxyCache(boolean cancelProxyCache) {
			this.cancelProxyCache = cancelProxyCache;
			return this;
		}

		public GetHttpResponse build() {
			return new GetHttpResponse(
					authType,
					authUser,
					authPassword,
					proxyScheme,
					proxyHost,
					proxyPort,
					proxyUser,
					proxyPassword,
					needAuthSSLCert,
					connectTimeout,
					requestTimeout,
					userAgent,
					keepAlive,
					cancelProxyCache
					);
		}
	}

	private static class CharsetParser extends ParserCallback {
		public String charset;

		public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
			parseCharset(t, a);
		}
		public void handleEndTag(HTML.Tag t, int pos) {
		}
		public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
			parseCharset(t, a);
		}
		private void parseCharset(HTML.Tag t, MutableAttributeSet a) {
			if (t.equals(HTML.Tag.META)) {
				Map<String, String> map = new HashMap<>();

				Enumeration<?> es = a.getAttributeNames();
				while (es.hasMoreElements()) {
					Object n = es.nextElement();
					Object v = a.getAttribute(n);
					map.put(n.toString().toUpperCase(), v.toString().toUpperCase());
				}

				String he = map.get("HTTP-EQUIV");
				if (he != null) {
					if ("CONTENT-TYPE".equals(he)) {
						String c = map.get("CONTENT");
						if (c != null) {
							Matcher m = chasetPattern.matcher(c);
							if (m.matches())
								charset = m.group(1);
						}
					}
				}
				else {
					if (a.getAttributeCount() == 1) {
						String cs = map.get("CHARSET");
						if (cs != null)
							charset = cs;
					}
				}
			}
		}
	}



	public static enum AuthType{
		BASIC,
		NTLM,
		DIGEST,
		NONE
	};

	private static final Log m_log = LogFactory.getLog( GetHttpResponse.class );

	private static final Pattern chasetPattern = Pattern.compile("^\\s*.+\\s*;\\s*charset\\s*=\\s*(.*)\\s*$", Pattern.CASE_INSENSITIVE);

	private static final int BUFF_SIZE = 1024 * 1024;
	private static final int BODY_MAX_SIZE = 5 * BUFF_SIZE;

	private CloseableHttpClient m_client;

	private CredentialsProvider m_cledentialProvider = new BasicCredentialsProvider();

	/** 認証種別 */
	private AuthType m_authType;

	/** 認証ユーザ */
	private String m_authUser;

	/** 認証パスワード */
	private String m_authPassword;

	/** プロキシサーバ　スキーム */
	private String m_proxyScheme;

	/** プロキシサーバ　ホスト名 */
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
	private int m_connectTimeout = 1000;

	/** リクエストタイムアウト  */
	private int m_requestTimeout = 1000;

	/** ユーザーエージェント */
	private String m_userAgent;

	/** キープアライブ */
	private boolean keepAlive = true;

	/** キープアライブ */
	private Response m_requestResult = new Response();


	/**
	 * コンストラクタ
	 */
	public GetHttpResponse() {
		super();
	}

	/**
	 * コンストラクタ
	 */
	public GetHttpResponse(
			AuthType authType,
			String authUser,
			String authPassword,
			String proxyScheme,
			String proxyHost,
			int proxyPort,
			String proxyUser,
			String proxyPassword,
			boolean needAuthSSLCert,
			int connectTimeout,
			int requestTimeout,
			String userAgent,
			boolean keepAlive,
			boolean cancelProxyCache
			) {
		super();
		this.setAuthType(authType);
		this.setAuthUser(authUser);
		this.setAuthPassword(authPassword);
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
	}

	/**
	 * リクエストURLから情報取得
	 * 
	 * @param url URL
	 * @param timeout タイムアウト（ミリ秒）
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public boolean execute(String url, String post) {
		Response result = new Response();
		try {
			CloseableHttpClient client = getHttpClient();

			result.url = url;
			if (m_authType != null && !AuthType.NONE.equals(m_authType)) {
				URI uri = new URI(url);

				Credentials credential = null;
				String authSchema = null;
				switch (m_authType) {
				case BASIC:
					credential = new UsernamePasswordCredentials(m_authUser, m_authPassword);
					authSchema = "basic";
					break;
				case NTLM:
					credential = new NTCredentials(m_authUser, m_authPassword, null, null);
					authSchema = "ntlm";
					break;
				case DIGEST:
					credential = new UsernamePasswordCredentials(m_authUser, m_authPassword);
					authSchema = "digest";
					break;
				default:
					m_log.warn("Auth type is unexpected value. AuthType = " + m_authType.name());
				}

				if (credential != null) {
					AuthScope scope = new AuthScope(uri.getHost(), uri.getPort(), AuthScope.ANY_REALM, authSchema);
					if (m_cledentialProvider.getCredentials(scope) == null) {
						m_cledentialProvider.setCredentials(scope, credential);
					}
				}
			}

			HttpRequestBase request = null;
			if (post != null && !post.isEmpty()) {
				List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
				
				for (String ss : post.split("&")) {
					int index = ss.indexOf("=");
					if (index <= 0) {
						continue;
					}
					urlParameters.add(new BasicNameValuePair(ss.substring(0, index), ss.substring(index + 1)));
				}
				if (m_log.isTraceEnabled()) {
					m_log.trace("post1=" + post + ", post2=" + urlParameters);
				}
				
				HttpPost requestPost = new HttpPost(url);
				Charset charset = Consts.UTF_8;
				try {
					charset = Charset.forName(HinemosPropertyCommon.monitor_http_post_charset.getStringValue());
				} catch (UnsupportedCharsetException e){
					m_log.warn("UnsupportedCharsetException " + e.getMessage());
				}
				requestPost.setEntity(new UrlEncodedFormEntity(urlParameters, charset));
				requestPost.addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
				request = requestPost;
			}
			else {
				request = new HttpGet(url);
			}

			// Execute the method.
			try {
				long start = HinemosTime.currentTimeMillis();
				HttpResponse response = client.execute(request);
				result.responseTime = HinemosTime.currentTimeMillis() -start;

				result.statusCode = response.getStatusLine().getStatusCode();

				// Header
				Header[] headers = response.getAllHeaders();
				if (headers != null && headers.length > 0) {
					StringBuffer header = new StringBuffer();
					for (int i = 0; i < headers.length; i++) {
						header.append((i != 0 ? "\n" : "") + headers[i]);
					}
					result.headerString = header.toString();
					result.headers = Arrays.asList(headers);
				}

				if (result.statusCode == HttpStatus.SC_OK) {
					result.success = true;

					// Content-Typeがtext文書の場合のみ、Bodyを取得
					Header header = response.getFirstHeader(HTTP.CONTENT_TYPE);
					
					boolean contentTypeFlag = false;
					String[] contentTypes = HinemosPropertyCommon.monitor_http_content_type.getStringValue().split(",");
					
					if (header != null && header.getValue() != null) {
						String value = header.getValue();
						for (String contentType : contentTypes) {
							if (value.indexOf(contentType) != -1) {
								contentTypeFlag = true;
								break;
							}
						}
					}
					
					if(contentTypeFlag){
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						try (InputStream in = response.getEntity().getContent()) {
							byte [] buffer = new byte[BUFF_SIZE];
							while(out.size() < BODY_MAX_SIZE) {
								int len = in.read(buffer);
								if(len < 0) {
									break;
								}
								out.write(buffer, 0, len);
							}
						}

						// レスポンスの文字セットは、HTTP ヘッダと meta タグに記載された文字セットを解析して決定する。
						// HTTP ヘッダの文字セットの記載例
						//
						// Content-Type: text/html; charset=euc-jp
						//
						// meta タグの記載例
						//
						// <meta http-equiv="Content-Type" content="text/html; charset=euc-jp">
						// <meta charset="euc-jp">
						//
						// HTML 的には、meta タグに指定された文字セットが優先される。
						//
						// レスポンスの解析には、一度文字列へ変換することが効率的ですが、
						// 実際には、文書を解析しないと適用できる文字セットが決められないため、
						// なにかしらダミーの文字セットを決定しないと文書を文字列に変換できない。
						//
						// この状況に対応する処理の流れとしては、以下を考えております。
						//
						// 1. HTTP ヘッダの Content-Type に charset が指定されている場合には、
						// この文字セットを利用して、レスポンスを文字列に変換する。
						//
						// 2. 指定されていない場合は、"JISAutoDetect" を使用して、
						// レスポンスを文字列に変換する。
						//
						// 3. レスポンスを変換した文字列から、meta タグを解析する。
						//
						// 4. meta タグに指定された文字セットが確認できたら、
						// 解析に使用した文字セットと異なる場合、
						// 再度、確認できた文字セットで文字列に変換する。

						String charset = "JISAutoDetect";
						Matcher m = chasetPattern.matcher(header.getValue());
						if (m.matches())
							charset = m.group(1);

						String content = new String(out.toByteArray(), charset);

						CharsetParser parser = new CharsetParser();
						ParserDelegator p = new ParserDelegator();
						p.parse(new StringReader(content), parser, true);

						if (parser.charset != null && !charset.equals(parser.charset)) {
							charset = parser.charset;
							content = new String(out.toByteArray(), charset);
						}
						result.responseBody = content;
					}
					else{
						result.errorMessage = MessageConstant.MESSAGE_FAIL_TO_CHECK_NOT_TEXT.getMessage();
					}
				}
				else{
					result.errorMessage = response.getStatusLine().toString();
				}
			}
			finally {
				request.releaseConnection();
			}
		}
		catch (UnsupportedEncodingException e) {
			m_log.info("execute(): " + e.getMessage() + " class=" + e.getClass().getName());
			result.errorMessage = "http receiving failure. (unsupported encoding)";
			result.exception = e;
		}
		catch (IOException e) {
			m_log.info("execute(): Fatal transport error. " + e.getMessage() + " class=" + e.getClass().getName());
			result.errorMessage = "http requesting failure. (I/O error : unreachable or timeout)";
			result.exception = e;
		}
		catch (Exception e) {
			m_log.info("execute(): " + e.getMessage() + " class=" + e.getClass().getName());
			result.errorMessage = "http requesting failure. " + e.getMessage() + "(" + e.getClass().getSimpleName() + ")";
			result.exception = e;
		}

		m_requestResult = result;

		return m_requestResult.success;
	}

	/**
	 * リクエストURLから情報取得
	 * 
	 * @param url URL
	 * @param timeout タイムアウト（ミリ秒）
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public boolean execute(String url) {
		return execute(url, null);
	}


	private CloseableHttpClient getHttpClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		if (m_client == null) {
			List<Header> headers = new ArrayList<>();

			HttpClientBuilder builder = HttpClients.custom()
					.setDefaultCredentialsProvider(m_cledentialProvider)
					.setDefaultHeaders(headers);

			if (!this.m_needAuthSSLCert) {
				// SSL の認証カット
				TrustStrategy trustStrategy = new TrustStrategy() {
					@Override
					public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						return true;
					}
				};
				builder.setSSLSocketFactory(
						new SSLConnectionSocketFactory(new SSLContextBuilder().loadTrustMaterial(null, trustStrategy).build(),
						new NoopHostnameVerifier()));
			}
			RequestConfig requestConfig = RequestConfig.custom()
					.setCookieSpec(CookieSpecs.DEFAULT)
					.setConnectTimeout(m_connectTimeout)
					.setSocketTimeout(m_requestTimeout).build();
			builder.setDefaultRequestConfig(requestConfig);

			if (m_proxyHost != null) {
				HttpHost proxy = new HttpHost(m_proxyHost, m_proxyPort, m_proxyScheme == null ? "https": m_proxyScheme);
				if (m_proxyUser != null && m_proxyPassword != null) {
					m_cledentialProvider.setCredentials(new AuthScope(proxy.getHostName(), proxy.getPort()), new UsernamePasswordCredentials(m_proxyUser, m_proxyPassword));
				}
				builder.setProxy(proxy);
			}

			if (m_userAgent != null) {
				headers.add(new BasicHeader(HTTP.USER_AGENT, m_userAgent));
			}

			if (m_cancelProxyCache) {
				// プロキシのキャッシュコントロール
				// https://www.ipa.go.jp/security/awareness/vendor/programmingv2/contents/405.html
				headers.add(new BasicHeader("Cache-Control", "no-cache"));
				headers.add(new BasicHeader("Pragma", "no-cache"));
			}

			if (keepAlive) {
				headers.add(new BasicHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE));
			}
			else {
				headers.add(new BasicHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE));
			}
			m_client = builder.build();
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


	private GetHttpResponse setAuthType(AuthType type) {
		this.m_authType = type;
		return this;
	}

	private void setAuthUser(String authUser) {
		this.m_authUser = authUser;
	}

	private void setAuthPassword(String authPassword) {
		this.m_authPassword = authPassword;
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

	public static GetHttpResponseBuilder custom() {
		return new GetHttpResponseBuilder();
	}

	@Override
	public void close() throws IOException {
		if (m_client != null) {
			m_client.close();
		}
	}
}
