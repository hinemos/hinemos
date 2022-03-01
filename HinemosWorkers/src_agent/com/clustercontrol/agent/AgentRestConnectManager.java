/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.DispatchingAuthenticator;
import com.burgstaller.okhttp.basic.BasicAuthenticator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import com.clustercontrol.agent.util.RestProxyNTLMAuthenticator;
import com.clustercontrol.bean.RestHeaderConstant;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.ApiClient;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 * 
 * Hinemosマネージャと通信できない場合は、RestConnectFailedがthrowされる。
 * RestConnectFailedが出力された場合は、もう一台のマネージャと通信する。
 */
public class AgentRestConnectManager {
	// ログ
	private static Log m_log = LogFactory.getLog(AgentRestConnectManager.class);

	private static Proxy m_proxy = Proxy.NO_PROXY;
	private static Authenticator m_proxyAuchenticator = null;
	private static CachingAuthenticatorDecorator m_proxyAuchentticatorDecorator = null;
	private static AuthenticationCacheInterceptor m_proxyAuthenticationCacheInterceptor =null;
	private static Map<String, CachingAuthenticator> m_proxyAuthCache = null;
	private static ThreadLocal<ApiClient> m_apiClient = new ThreadLocal<ApiClient> () {
		@Override
		protected ApiClient initialValue() {
			m_log.debug("initialValue");
			return null;
		}
	};
	
	private static RestUrlSettingList urlSettingList ;
	
	//Restの接続種別を定義（接続種別ごとに既定となるURLが変わるため
	public enum RestKind {
		AgentRestEndpoints("AgentRestEndpoints"),
		AgentHubRestEndpoints("AgentHubRestEndpoints"),
		AgentBinaryRestEndpoints("AgentBinaryRestEndpoints"),
		AgentNodeConfigRestEndpoints("AgentNodeConfigRestEndpoints"),
		AgentSdmlRestEndpoints("AgentSdmlRestEndpoints"),
		AgentRpaRestEndpoints("AgentRpaRestEndpoints");

		private final String name;

		private RestKind(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	// 接続URL一覧管理クラス URL一覧のCRUDを実装している
	private static class RestUrlSettingList {
		private List<String> restUrlList = new ArrayList<String>();
		private String lastSuccessRestUrl =null;

		private void add(String restUrl) {
			if (lastSuccessRestUrl == null) {
				lastSuccessRestUrl = restUrl;
			}
			restUrlList.add(restUrl);
		}

		private List<RestUrlSetting> getList( RestKind restKind) {
			List<RestUrlSetting> list = new ArrayList<RestUrlSetting>(restUrlList.size());
			if(lastSuccessRestUrl != null ){
				list.add( new RestUrlSetting( lastSuccessRestUrl , restKind ) );
			}
			for (String restUrl : restUrlList) {
				if (!(restUrl.equals(lastSuccessRestUrl))) {
					list.add(new RestUrlSetting( restUrl,restKind));
				}
			}
			return list;
		}

		// lastSuccessRestUrlの設定を更新し、次回処理での接続先URLを入れ替える
		// ※ restUrlListが 1件(切り替えは発生しない) か2件(トグルする) であることが前提のロジックなので注意
		public void changeConnectUrl() {
			m_log.debug("changeEndpoint");
			boolean flag = false;
			for (String e : restUrlList) {
				if (flag) {
					lastSuccessRestUrl = e;
					flag = false;
					return;
				}
				if (lastSuccessRestUrl.equals(e)) {
					flag = true;
				}
			}
			lastSuccessRestUrl = restUrlList.get(0);
		}
		private int size() {
			return restUrlList.size();
		}
	}

	//Restの基底URL毎での接続に関する設定とロジックを保持する
	public static class RestUrlSetting {
		private final String urlPrefix;
		private RestKind restKind = null; 
		private String agentRequestId =null;
		
		public static final String _pathPrefix = "api/";

		public RestUrlSetting(String urlPrefix, RestKind restKind) {
			this.urlPrefix =urlPrefix;
			this.restKind =restKind;
		}
		
		private ApiClient initApiClient() {
			ApiClient apiClient = new ApiClient() {
				// openapi-generator 4.3.1 の不具合のワークアラウンド。
				// https://github.com/OpenAPITools/openapi-generator/issues/440
				// リクエストの accept ヘッダ候補に JSON 形式があると、それ以外の候補を除去してしまうので、
				// ダウンロード API のように JSON 以外を返すことがある API の呼び出しで
				// 409(Not Acceptable)エラーを引き起こしてしまう。
				@Override
				public String selectHeaderAccept(String[] accepts) {
					if (accepts.length == 0) {
						return null;
					}
					return com.clustercontrol.rest.StringUtil.join(accepts, ",");
				}
			};
			
			//プロキシ設定(SSLより先に設定すること)
			if(AgentRestConnectManager.getProxy() != null && !(AgentRestConnectManager.getProxy().type().equals(Type.DIRECT)) ){
				OkHttpClient.Builder builder =new OkHttpClient.Builder();
				builder.proxy(AgentRestConnectManager.getProxy());
				if(AgentRestConnectManager.getProxyAuchenticator() != null){
					if (AgentRestConnectManager.getProxyAuchenticator() instanceof DispatchingAuthenticator) {
						builder.addInterceptor(AgentRestConnectManager.getCacheInterceptor());
						builder.proxyAuthenticator(AgentRestConnectManager.getProxyAuchentticatorDecorator());
					} else {
						builder.proxyAuthenticator(AgentRestConnectManager.getProxyAuchenticator());
					}
				}
				OkHttpClient client =builder.build();
				apiClient.setHttpClient(client);
			}
			// SSLにおけるホスト認証対応（実施しない場合はその旨を設定。検証に関わる設定は別途JVM向け引数にて指定される）
			boolean doHostVerify = Boolean.valueOf(System.getProperty("https.hostVerify", "false"));
			if( !(doHostVerify) ){
				apiClient.setVerifyingSsl(false);
			}
			apiClient.setConnectTimeout(AgentRestConnectManager.getHttpConnectTimeout());
			apiClient.setReadTimeout(AgentRestConnectManager.getHttpRequestTimeout());
			apiClient.setWriteTimeout(AgentRestConnectManager.getHttpRequestTimeout());
			// Basic認証 
			apiClient.addDefaultHeader("Authorization", "Basic "+ AgentRestConnectManager.getBasicAuthorization() );
			return apiClient;
		}

		public ApiClient getApiClient() throws RestConnectFailed {
			try {
				//Pathの補完
				String urlStr = urlPrefix +_pathPrefix + restKind.getName();
				
				// DefaultApiのインスタンスを生成して返す
				// コネクションが都度生成しないようインスタンスをThreadLocalに保持して再利用する
				ApiClient apiClient = m_apiClient.get();
				if (apiClient == null) {
					m_apiClient.set(initApiClient());
					apiClient = m_apiClient.get();
				}

				// URL その他Httpヘッダを設定 
				apiClient.setBasePath(urlStr);
				if (this.agentRequestId != null) {
					apiClient.addDefaultHeader(RestHeaderConstant.AGENT_REQUEST_ID, agentRequestId);
					apiClient.addDefaultHeader(RestHeaderConstant.AGENT_IDENTIFIER, Agent.getRestAgentId());
				}
				
				return apiClient;
			} catch (Exception e) {
				m_log.warn("failed creating DefaultApi.", e);
				throw new RestConnectFailed(e.getMessage(),e.getCause());
				
			}
		}
		public String  getUrlPrefix() {
			return urlPrefix;
		}
		public void setAgentRequestId(String agentRequestId ){
			this.agentRequestId = agentRequestId;
			return ;
		}
		
	}


	private static String m_username = "";
	private static String m_password = "";
	private static String m_basicAuthorization = "";
	private static int m_httpConnectTimeout = Agent.DEFAULT_CONNECT_TIMEOUT;
	private static int m_httpRequestTimeout = Agent.DEFAULT_REQUEST_TIMEOUT;

	public static void init(String user, String pass, String managerAddressList,
			int httpConnectTimeout, int httpRequestTimeout) {
		m_username = user;
		m_password = pass;
		urlSettingList = new RestUrlSettingList();
		m_httpConnectTimeout = httpConnectTimeout;
		m_httpRequestTimeout = httpRequestTimeout;

		for (String managerAddress : managerAddressList.split(",")) {
			urlSettingList.add(managerAddress);
		}
		m_basicAuthorization =Base64.encodeBase64String((m_username+":"+ m_password).getBytes(StandardCharsets.UTF_8));
		m_log.info("manager instance = " + urlSettingList.size() );
	}

	public static void setProxy(String proxyHost, int proxyPort) {
		Proxy proxy = null;
		if (proxyHost == null || proxyHost.length() == 0) {
			proxy = Proxy.NO_PROXY;
		} else {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
		}
		m_proxy = proxy;
	}
	
	public static Proxy getProxy() {
		return m_proxy;
	}

	public static void setProxyAuchenticator(String proxyUser, String proxyPass) {

		if (proxyUser == null || proxyUser.length() == 0) {
			return;
		}

		Credentials proxyCredentials = new Credentials(proxyUser, proxyPass);
		final DigestAuthenticator digestAuthenticator = new DigestAuthenticator( proxyCredentials );
		final BasicAuthenticator basicAuthenticator = new BasicAuthenticator(proxyCredentials );
		final RestProxyNTLMAuthenticator ntlmAuthenticator = new RestProxyNTLMAuthenticator(proxyUser, proxyPass,"" );
		final DispatchingAuthenticator dispatchingAuthenticator = new DispatchingAuthenticator.Builder()
				.with("digest", digestAuthenticator).with("basic", basicAuthenticator).build();
		Authenticator proxyAuthenticator = new CachingAuthenticator	 (){
			Authenticator  useAuth = null;
			@Override
			public Request authenticate(Route route, Response response) throws IOException
			{
				for (String header : response.headers().values(DigestAuthenticator.PROXY_AUTH) ) {
					//Digest認証要求 もしくは  Basic認証要求
					if (header.startsWith("Digest") || header.startsWith("Basic")) {
						Request rest= dispatchingAuthenticator.authenticate(route, response);
						// 認証キャッシュ機構（Inspectorでヘッダを設定）が動作するように 
						// proxyAuchenticator インスタンスをDispatchingAuthenticatorに切り替え
						useAuth = dispatchingAuthenticator;
						m_proxyAuchenticator = dispatchingAuthenticator;
						m_proxyAuchentticatorDecorator = new CachingAuthenticatorDecorator(
								dispatchingAuthenticator, m_proxyAuthCache);
						m_log.debug("new Digest =" + rest.headers());
						return rest;
					}
					//NTLM認証要求
					if (header.startsWith("NTLM")) {
						// 認証キャッシュ機構がNTLM未対応のため、別途設定
						// DispatchingAuthenticator のbuild に
						// with("ntlm",ntlmAuthenticator) も設定可能だが
						// キャッシュ関連の警告が頻発するので回避。
						useAuth = ntlmAuthenticator;
						m_proxyAuchenticator = ntlmAuthenticator;
						return ntlmAuthenticator.authenticate(route, response);
					}
				}
				return null;
			}
			@Override
			public Request authenticateWithState(Route route, Request request) throws IOException {
				if( useAuth instanceof DispatchingAuthenticator ){
					return ((DigestAuthenticator) useAuth).authenticateWithState(route, request);
				}
				return null;
			}
		};

		m_proxyAuchenticator = proxyAuthenticator;
		m_proxyAuthCache = new ConcurrentHashMap<String, CachingAuthenticator>();
		m_proxyAuthenticationCacheInterceptor = new AuthenticationCacheInterceptor(m_proxyAuthCache);
		m_proxyAuchentticatorDecorator = new CachingAuthenticatorDecorator(proxyAuthenticator,
				m_proxyAuthCache);
	}

	/**
	* DefaultApiによるプロキシ認証向けのAuthenticatorを取得
	* @return AuthenticationCacheInterceptor
	*/
	public static Authenticator getProxyAuchenticator() {
		return m_proxyAuchenticator;
	}

	/**
	* DefaultApiによるプロキシ認証向けのAuthenticationCacheInterceptorを取得
	* @return AuthenticationCacheInterceptor
	*/
	public static AuthenticationCacheInterceptor getCacheInterceptor() {
		return m_proxyAuthenticationCacheInterceptor;
	}

	/**
	* DefaultApiによるプロキシ認証向けのCachingAuthenticatorDecoratorを取得
	* @return CachingAuthenticatorDecorator
	*/

	public static CachingAuthenticatorDecorator getProxyAuchentticatorDecorator() {
		return m_proxyAuchentticatorDecorator;
	}

	/**
	 *	使用可能な順番でRestUrlSettingリストを返す
	 * @return RestUrlSettingリスト
	 */
	public static List<RestUrlSetting> getUrlSettingList(RestKind restKind ) {
		return urlSettingList.getList(restKind);
	}

	/**
	 *	次回処理での接続先URLを入れ替える
	 */
	public static void changeConnectUrl() {
		urlSettingList.changeConnectUrl();
	}

	/**
	 *	http接続タイムアウトの設定値を取得
	 */
	private static int getHttpConnectTimeout(){
		return m_httpConnectTimeout;
	}

	/**
	 *	http要求タイムアウトの設定値を取得
	 */
	private static int getHttpRequestTimeout(){
		return m_httpRequestTimeout;
	}

	/**
	 *	Basic認証向けの認証値を取得
	 */
	private static String getBasicAuthorization(){
		return m_basicAuthorization;
	}


}
