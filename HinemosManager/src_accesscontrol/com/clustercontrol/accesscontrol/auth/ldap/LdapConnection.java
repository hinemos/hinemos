/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.auth.ldap;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jndi.ldap.LdapCtxFactory;

/**
 * LDAPによる情報検索機能を提供します。
 */
public class LdapConnection implements Closeable {
	private static final Log log = LogFactory.getLog(LdapConnection.class);
	
	/** ログメッセージでバインド対象を識別するためのID */
	private String bindId;
	
	/** ディレクトリサービス */
	private DirContext ctx;
	
	/** 接続タイムアウト(ミリ秒) (0:ソケットのタイムアウト設定に従う) */
	private int connectTimeout;
	
	/** 読み取りタイムアウト(ミリ秒) (0:無限に待機する) */
	private int readTimeout;

	/** true: 証明書チェーンを検証しない */
	private boolean lenientSsl;
	
	/**
	 * インスタンスを生成します。
	 * まだLDAP接続は開始しません。
	 */
	public LdapConnection() {
		ctx = null;
		bindId = "";
		this.connectTimeout = 0;
		this.readTimeout = 0;
		lenientSsl = false;
	}

	/**
	 * 接続タイムアウトを設定します。
	 * デフォルト値は 0  です。
	 * <p>
	 * {@link #bind(String, String, String)}の前に呼び出す必要があります。
	 * 
	 * @param connectTimeout 接続タイムアウト(ミリ秒)。0 の場合はベースとなるネットワークプロトコルの動作に従います。
	 * @return このインスタンス自身。
	 */
	public LdapConnection setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	/**
	 * 読み取りタイムアウトを設定します。
	 * デフォルト値は 0  です。
	 * <p>
	 * {@link #bind(String, String, String)}の前に呼び出す必要があります。
	 * 
	 * @param readTimeout 読み取りタイムアウト(ミリ秒)。0の場合は無限に待機します。
	 * @return このインスタンス自身。
	 */
	public LdapConnection setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
		return this;
	}

	/**
	 * LDAPS接続時、サーバの証明書に対する証明書チェーンの検証をしないようにするかどうかを設定します。
	 * デフォルトは false(検証する) です。
	 * <p>
	 * {@link #bind(String, String, String)}の前に呼び出す必要があります。
	 * 
	 * @param lenientSsl trueの場合、証明書チェーンの検証を行いません。
	 * @return このインスタンス自身。
	 */
	public LdapConnection setLenientSsl(boolean lenientSsl) {
		this.lenientSsl = lenientSsl;
		return this;
	}

	/**
	 * ディレクトリサービスにバインド(接続)します。<br/>
	 * すでにバインド済みの場合は、そのバインドを解除してから、新たにバインドを行います。
	 * 
	 * @param url ディレクトリサービスサーバのURL。
	 * @param userDn バインドするDN。
	 * @param userPw バインドパスワード。
	 * @return バインドに成功した場合は true、資格情報に誤りがあってバインド失敗した場合は false。
	 * @throws LdapAccessException ディレクトリサービスとの通信に問題が発生した。
	 */
	public boolean bind(String url, String userDn, String userPw) {
		if (ctx != null) {
			close();
		}
		
		bindId = "{url=" + url + ",user=" + userDn + "}";  // セキュリティのためパスワードは含めない

		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.PROVIDER_URL, url);
		env.put(Context.INITIAL_CONTEXT_FACTORY, LdapCtxFactory.class.getName());
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, userDn);
		env.put(Context.SECURITY_CREDENTIALS, userPw);
		if (url.toLowerCase().startsWith("ldaps:") && lenientSsl) {
			env.put("java.naming.ldap.factory.socket", LenientSSLSocketFactory.class.getName());
		}
		env.put("com.sun.jndi.ldap.connect.pool", "false");
		env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(connectTimeout));
		env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(readTimeout));
		
		try {
			ctx = new InitialDirContext(env);
		} catch (AuthenticationException | InvalidNameException e) {
			// 資格情報エラーの場合は、例外を収めて false を返す。
			// DN が構文レベルで間違っている場合の InvalidNameException も同じ扱いとする。
			log.debug("bind: " + e.getClass().getSimpleName() + " bindId=" + bindId + " message=" + e.getMessage());
			return false;
		} catch (NamingException e) {
			// NamingExceptionのエラーメッセージだけでは原因がわかりにくいことがあるので、大本の例外のメッセージも出力する。
			log.warn("bind: Error. bindId=" + bindId + " message=" + e.getMessage()
					+ ", cause=" + getRootExceptionMessage(e));
			throw new LdapAccessException(e);
		}

		log.debug("bind: Ok. " + bindId);
		return true;
	}

	/**
	 * 指定されたThrowableの一番最初の原因となったThrowableに関するログ用メッセージを返します。
	 */
	protected static String getRootExceptionMessage(Throwable throwable) {
		Throwable t = throwable;
		if (t.getCause() == null) return "none";
		while (t.getCause() != null) {
			t = t.getCause();
		}
		return "{exception=" + t.getClass().getName() + ", message=" + t.getMessage() + "}";
	}
	
	/**
	 * baseDnで指定されたエントリ配下から、filterにマッチするエントリを検索します。
	 * filterがnullあるいは空文字列の場合、baseDn配下のすべてのエントリを返します。
	 * 
	 * @throws LdapAccessException ディレクトリサービスとの通信に問題が発生した。
	 */
	public List<LdapSearchResult> searchSubTree(String baseDn, String filter) {
		if (ctx == null) {
			// プログラムミスの可能性が高い(bindの戻り値をチェックしていない)
			throw new IllegalStateException("Not binded. ");
		}
		
		if (filter == null || filter.length() == 0) {
			filter = "(objectClass=*)";
		}
		
		SearchControls ctrl = new SearchControls();
		ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		try {
			return LdapSearchResult.createFrom(ctx.search(baseDn, filter, ctrl));
		} catch (NamingException e) {
			log.warn("searchSubTree: Error. bindId=" + bindId + ", baseDn=" + baseDn + ", filter=" + filter
					+ ", message=" + e.getMessage() + ", cause=" + getRootExceptionMessage(e));
			throw new LdapAccessException(e);
		}
	}

	/**
	 * ディレクトリサービスへのバインドを解除します。
	 */
	@Override
	public void close() {
		if (ctx != null) {
			try {
				ctx.close();
				log.debug("close: Ok. bindId=" + bindId);
			} catch (NamingException e) {
				log.warn("close: Error. bindId=" + bindId, e);
			}
			ctx = null;
			bindId = "";
		}
	}

	/**
	 * RFC2254(https://www.ietf.org/rfc/rfc2254.txt)に従って
	 * 文字列のエスケープを行って返します。
	 */
	public static String escapeRfc2254(String s) {
		return s.replace("\\", "\\5c") // 最初に"\"をエスケープする
				.replace("*", "\\2a")
				.replace("(", "\\28")
				.replace(")", "\\29")
				.replace("\u0000", "\\00");
	}
	
	/**
	 * LDAPによる情報検索の結果です。
	 */
	public static class LdapSearchResult {
		
		/** エントリのDN */
		private String name;

		/** 属性名 -> 属性値(複数可) */
		private Map<String, List<Object>> attributes;

		/**
		 * {@link SearchResult} の enumeration から、{@link LdapSearchResult} のリストを生成します。
		 * <p>
		 * 本メソッドは、引数として受け取った{@link NamingEnumeration}を必ずcloseしますので、
		 * 呼び出し元でリソース解放を意識する必要はありません。
		 */
		public static List<LdapSearchResult> createFrom(NamingEnumeration<SearchResult> searchResults)
				throws NamingException {
			try {
				List<LdapSearchResult> rtn = new ArrayList<>();
				while (searchResults.hasMore()) {
					rtn.add(new LdapSearchResult(searchResults.next()));
				}
				return rtn;
			} finally {
				// hasMore()がfalseになるまで回した場合、自動でリソースは解放されるが、
				// 例外発生で最後まで回らなかった場合に備えて。
				searchResults.close();
			}
		}

		/**
		 * {@link SearchResult} からインスタンスを生成します。
		 * 
		 */
		public LdapSearchResult(SearchResult sr) throws NamingException {
			this(sr.getNameInNamespace());

			NamingEnumeration<? extends Attribute> attrs = sr.getAttributes().getAll();
			try {
				while (attrs.hasMore()) {
					Attribute a = attrs.next();
					String attrName = a.getID();
					List<Object> attrValues = new ArrayList<>();
					NamingEnumeration<?> vals = a.getAll();
					try {
						while (vals.hasMore()) {
							attrValues.add(vals.next());
						}
					} finally {
						vals.close();
					}
					attributes.put(attrName, attrValues);
				}
			} finally {
				attrs.close();
			}
		}

		/**
		 * 指定されたDNで、属性を持たないインスタンスを生成します。
		 */
		public LdapSearchResult(String name) {
			this.name = name;
			attributes = new ConcurrentHashMap<>();
		}
		
		/**
		 * DNを返します。
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * 指定された属性を持っている場合は true を返します。
		 */
		public boolean hasAttribute(String attributeName) {
			return attributes.containsKey(attributeName);
		}

		/**
		 * 指定された属性の値を上書きでセットします。
		 * <p>
		 * valuesはコピーせずにそのまま使用します。
		 * そのため呼び出し元で変更を加えた場合、本クラスにも影響が及びます。
		 */
		public void setValues(String attributeName, List<Object> values) {
			attributes.put(attributeName, values);
		}
		
		/**
		 * 指定された属性の値(複数可のためリスト)を返します。
		 * <p>
		 * 指定された属性を持たない場合は例外を投げます。
		 * 指定された属性を持たない可能性がある場合は、先に{@link #hasAttribute(String)}でチェックを行ってください。
		 */
		public List<Object> getValues(String attributeName) {
			if (!attributes.containsKey(attributeName)) {
				throw new NoSuchElementException(attributeName);
			}
			return attributes.get(attributeName);
		}
		
		/**
		 * 指定された属性の最初の値を返します。
		 * <p>
		 * 指定された属性を持たない場合は例外を投げます。
		 * 指定された属性を持たない可能性がある場合は、先に{@link #hasAttribute(String)}でチェックを行ってください。
		 */
		public Object getFirstValue(String attributeName) {
			if (!attributes.containsKey(attributeName)) {
				throw new NoSuchElementException(attributeName);
			}
			return attributes.get(attributeName).get(0);
		}

		/**
		 * 指定された属性の最初の値を文字列化して返します。
		 * nullの場合は空文字列にします。
		 * <p>
		 * 指定された属性を持たない場合は例外を投げます。
		 * 指定された属性を持たない可能性がある場合は、先に{@link #hasAttribute(String)}でチェックを行ってください。
		 */
		public String getFirstValueAsString(String attributeName) {
			if (!attributes.containsKey(attributeName)) {
				throw new NoSuchElementException(attributeName);
			}
			Object v = attributes.get(attributeName).get(0);
			return Objects.toString(v, "");
		}

		/**
		 * すべての属性値を human readable な形式で文字列化します。
		 */
		public String toPrettyString() {
			StringBuilder sb = new StringBuilder();
			sb.append("[").append(name).append("]\n");
			attributes.keySet().stream().sorted().forEach(k -> {
				sb.append(k).append(": ");
				sb.append(attributes.get(k).stream()
						.map(v -> Objects.toString(v, "")).collect(Collectors.joining(", ")));
				sb.append("\n");
			});
			return sb.toString();
		}
	}
	
	/**
	 * 証明書チェーンを検証しない(自己署名証明書を通す)SSLSocketFactory。
	 */
	public static class LenientSSLSocketFactory extends SSLSocketFactory {
		private static Log log = LogFactory.getLog(LenientSSLSocketFactory.class);

		public static final String SSL_PROTOCOL = "TLS";

		private SSLContext sslContext;
		private SSLSocketFactory delegate;

		public LenientSSLSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
			sslContext = SSLContext.getInstance(SSL_PROTOCOL);
			sslContext.init(null, new X509TrustManager[] { new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType)
						throws CertificateException {
					// NOP
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType)
						throws CertificateException {
					// デバッグログを出力するだけ
					if (!log.isDebugEnabled()) return;

					for (int i = 0; i < chain.length; ++i) {
						X509Certificate cert = chain[i];
						StringBuilder msg = new StringBuilder();
						msg.append("chain").append(i).append("={")
								.append("issuer=").append(cert.getIssuerX500Principal())
								.append(",sans=").append(Objects.toString(cert.getSubjectAlternativeNames()))
								.append("},");
						log.debug("checkServerTrusted: " + msg);
					}
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			} }, new SecureRandom());

			delegate = sslContext.getSocketFactory();
		}
		
		// "com.sun.jndi.ldap.connect.timeout" != 0 の場合に呼ばれる。
		// abstract ではないが、overrideしないと SocketFactory クラスの「無条件に例外を投げる実装」が使われてしまう。
		@Override
		public Socket createSocket() throws IOException {
			return delegate.createSocket();
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			return delegate.createSocket(host, port);
		}

		@Override
		public Socket createSocket(InetAddress host, int port) throws IOException {
			return delegate.createSocket(host, port);
		}

		@Override
		public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
				throws IOException, UnknownHostException {
			return delegate.createSocket(host, port, localHost, localPort);
		}

		@Override
		public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
				throws IOException {
			return delegate.createSocket(address, port, localAddress, localPort);
		}

		@Override
		public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
			return delegate.createSocket(s, host, port, autoClose);
		}

		@Override
		public String[] getDefaultCipherSuites() {
			return delegate.getDefaultCipherSuites();
		}

		@Override
		public String[] getSupportedCipherSuites() {
			return delegate.getSupportedCipherSuites();
		}
		
		// com.sun.jndi.ldap.Connectionは、このstaticメソッドを呼んでSocketFactroyのインスタンスを生成する。
		// リフレクション経由で呼び出されるため、SSLSocketFactroy#getDefaultを実質オーバーライドしている形となる。
		public static SocketFactory getDefault() {
			try {
				return new LenientSSLSocketFactory();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
