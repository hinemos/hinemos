/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;

import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.basic.BasicAuthenticator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * REST接続に関する情報を管理するためのクラス<BR>
 * 
 * 以下の情報を管理している。<BR>
 * ・REST接続するHinemosマネージャ（ユニット）<BR>
 * ・REST接続時のWebProxyに関わる情報<BR>
 * ・REST接続時の通信タイムアウトに関わる情報<BR> 
 */
public class RestConnectManager {
	// ログ
	private static Log m_log = LogFactory.getLog(RestConnectManager.class);

	/** Managers 
	 * ログイン時の順番を保持するためにLinkedHashMapを利用する。
	 * */
	private Map<String, RestConnectUnit> connectUnitMap = new LinkedHashMap<>();

	/** Common settings */
	private int httpRequestTimeout = RestLoginManager.VALUE_HTTP_REQUEST_TIMEOUT;

	//プロキシ
	private Proxy proxy = Proxy.NO_PROXY;

	//プロキシ向けの認証設定
	private Authenticator proxyAuchenticator = null;
	private Credentials proxyCredentials = null;
	private CachingAuthenticatorDecorator proxyAuchentticatorDecorator = null;
	private AuthenticationCacheInterceptor proxyAuthenticationCacheInterceptor =null;
	private Map<String, CachingAuthenticator> proxyAuthCache = null;
	
	// 設定情報変更日時（エポックミリ秒）
	private long setupExecuteTime = 0;
	
	/** Private constructor */
	private RestConnectManager() {}

	/** Singleton */
	private static RestConnectManager getInstance() {
		return SingletonUtil.getSessionInstance(RestConnectManager.class);
	}

	/**
	 * DefaultApi向けのhttpリクエストのタイムアウト時間（ミリ秒）を取得する
	 */
	public static int getHttpRequestTimeout() {
		return getInstance().httpRequestTimeout;
	}

	/**
	 * DefaultApi向けのhttpリクエストのタイムアウト時間（ミリ秒）を設定する
	 */
	public static void setHttpRequestTimeout(int httpRequestTimeout) {
		getInstance().httpRequestTimeout = httpRequestTimeout;
	}
	
	/**
	 * DefaultApi向けのWebプロキシを設定する<BR>
	 * 
	 * @param proxyHost
	 *            Webプロキシホスト（IPorホスト名）
	 * @param proxyPort
	 *            Webプロキシポート（番号）
	 */
	public static void setProxy(String proxyHost, int proxyPort) {
		Proxy proxy = null;
		if (proxyHost == null || proxyHost.length() == 0) {
			proxy = Proxy.NO_PROXY;
		} else {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
		}
		getInstance().proxy = proxy;
	}

	/**
	 * DefaultApi向けのWebプロキシの設定を取得する<BR>
	 */
	public static Proxy getProxy() {
		return getInstance().proxy;
	}

	/**
	 * DefaultApi向けのWebプロキシ認証用Auchenticatorを設定する<BR>
	 * 
	 * AuchenticatorはBasic、Digest、NTLM の３種の認証に対応している。<BR>
	 * 
	 * Auchenticatorと併せてキャッシュ処理向けの各種インスタンス（インターセプターとディテクター）も設定している。<BR>
	 * 
	 * @param proxyUser
	 *            プロキシの認証用ユーザID
	 * @param proxyPass
	 *            プロキシの認証用パスワード
	 */
	public static void setProxyAuchenticator(String proxyUser, String proxyPass) {
		if (proxyUser != null && proxyUser.length() > 0) {
			getInstance().proxyCredentials = new Credentials(proxyUser, proxyPass);
		}else{
			getInstance().proxyAuchenticator = null;
			getInstance().proxyCredentials = null;
			getInstance().proxyAuthCache = null;
			getInstance().proxyAuthenticationCacheInterceptor = null;
			getInstance().proxyAuchentticatorDecorator = null;
			return ;
		}
		final DigestAuthenticator digestAuthenticator = new DigestAuthenticator( getInstance().proxyCredentials );
		final BasicAuthenticator basicAuthenticator = new BasicAuthenticator( getInstance().proxyCredentials );
		final RestProxyNTLMAuthenticator ntlmAuthenticator = new RestProxyNTLMAuthenticator(proxyUser, proxyPass,"" );
		Authenticator proxyAuthenticator = new Authenticator (){
			@Override
			public Request authenticate(Route route, Response response) throws IOException
			{
				for (String header : response.headers().values(DigestAuthenticator.PROXY_AUTH) ) {
					//Digest認証要求
					if (header.startsWith("Digest")) {
						return digestAuthenticator.authenticate(route, response);
					}
					//Basic認証要求
					if (header.startsWith("Basic")) {
						return basicAuthenticator.authenticate(route, response);
					}
					//NTLM認証要求
					if (header.startsWith("NTLM")) {
						return ntlmAuthenticator.authenticate(route, response);
					}
				}
				return null;
			}
		};
		getInstance().proxyAuchenticator = proxyAuthenticator;
		getInstance().proxyAuthCache = new ConcurrentHashMap<String, CachingAuthenticator>();
		getInstance().proxyAuthenticationCacheInterceptor = new AuthenticationCacheInterceptor(getInstance().proxyAuthCache);
		getInstance().proxyAuchentticatorDecorator = new CachingAuthenticatorDecorator(proxyAuthenticator,
				getInstance().proxyAuthCache);
	}

	/**
	* DefaultApiによるプロキシ認証向けのAuchenticatorを取得
	* @return Authenticator
	*/
	public static Authenticator getProxyAuchenticator() {
		return getInstance().proxyAuchenticator;
	}

	/**
	* DefaultApiによるプロキシ認証向けのAuthenticationCacheInterceptorを取得
	* @return AuthenticationCacheInterceptor
	*/
	public static AuthenticationCacheInterceptor getCacheInterceptor() {
		return getInstance().proxyAuthenticationCacheInterceptor;
	}

	/**
	* DefaultApiによるプロキシ認証向けのCachingAuthenticatorDecoratorを取得
	* @return CachingAuthenticatorDecorator
	*/
	public static CachingAuthenticatorDecorator getProxyAuchentticatorDecorator() {
		return getInstance().proxyAuchentticatorDecorator;
	}

	/**
	 * 
	 * REST接続するHinemosマネージャ（ユニット）を管理対象に追加する。<Br>
	 * 
	 * @param user
	 *            接続先Hinemosマネージャ（ユニット）のユーザID
	 * @param pass
	 *            接続先Hinemosマネージャ（ユニット）のパスワード
	 * @param managerName
	 *            接続先Hinemosマネージャ（ユニット）のマネージャ名
	 * @param managerAddressList
	 *            接続先Hinemosマネージャ（ユニット）のREST接続先アドレスリスト（カンマ区切りで複数指定）
	*/
	public static void add(String user, String pass, String managerName, String managerAddressList ) {
		RestConnectManager connectManager = getInstance();

		RestConnectUnit connectUnit = connectManager.connectUnitMap.get(managerName);
		if (connectUnit == null) {
			m_log.debug("new managerName=" + managerName);
			connectUnit = new RestConnectUnit();
			connectManager.connectUnitMap.put( managerName, connectUnit );
		}
		
		connectUnit.set(managerAddressList, user, pass, managerName);
		return;
	}

	private Map<String, RestConnectUnit> getActiveManagerMap() {
		Map<String, RestConnectUnit> activeManagerMap = new LinkedHashMap<>();
		for( Map.Entry<String, RestConnectUnit> e : getInstance().connectUnitMap.entrySet() ){
			if( e.getValue().isActive() ){
				activeManagerMap.put(e.getKey(), e.getValue());
			}
		}
		return activeManagerMap;
	}

	/**
	 * アクティブなHinemosマネージャ（ユニット）の件数を返す
	 * 
	 * @return 件数
	 */
	public static int sizeOfActive() {
		return getActiveManagerNameList().size();
	}

	/**
	 * 管理しているREST接続ユニットの件数を返す
	 * 
	 * @return 件数
	 */
	public static int sizeOfAll() {
		return getInstance().connectUnitMap.size();
	}

	/**
	 * 登録時のマネージャ名を元にHinemosマネージャ（ユニット）を取得
	 * 
	 * @return RestConnectUnit
	 */
	public static RestConnectUnit get( String managerName ){
		return getInstance().connectUnitMap.get(managerName);
	}

	/**
	 * 登録時のマネージャ名を元にアクティブなHinemosマネージャ（ユニット）を取得
	 * 
	 * @return RestConnectUnit
	 */
	public static RestConnectUnit getActive( String managerName ){
		if (getInstance().getActiveManagerMap().get(managerName) != null) {
			return getInstance().connectUnitMap.get(managerName);
		}
		else {
			m_log.warn(Messages.getString("message.accesscontrol.18"));
			throw new IllegalStateException(Messages.getString("message.accesscontrol.18"));
		}
	}

	/**
	 * 登録時のマネージャ名を元にHinemosマネージャ（ユニット）の表示順を取得
	 * 
	 * @return 順番号
	 */
	public static int getOrder(String managerName) {
		int n = 0;
		for (String key : getInstance().connectUnitMap.keySet()) {
			if (key.equals(managerName)) {
				return n;
			}
			n ++;
		}
		return 99;
	}

	public static Set<String> getActiveManagerSet() {
		return getInstance().getActiveManagerMap().keySet();
	}

	/**
	 * 全Hinemosマネージャ（ユニット）の一覧を取得
	 * 
	 * @return RestConnectUnit
	 */
	public static List<RestConnectUnit> getAllManagerList() {
		return ( 0 < sizeOfAll() ) ? new ArrayList<RestConnectUnit>( getInstance().connectUnitMap.values() ) : new ArrayList<RestConnectUnit>();
	}

	/**
	 * アクティブなHinemosマネージャ（ユニット）の一覧を取得
	 * 
	 * @return RestConnectUnit
	 */

	public static List<RestConnectUnit> getActiveManagerList() {
		Map<String, RestConnectUnit> activeManagerMap = getInstance().getActiveManagerMap();
		return ( 0 < activeManagerMap.size() ) ? new ArrayList<RestConnectUnit>( activeManagerMap.values() ) : new ArrayList<RestConnectUnit>();
	}

	public static List<String> getActiveManagerNameList() {
		List<String> activeList = new ArrayList<>();
		for( Map.Entry<String, RestConnectUnit> e : getInstance().connectUnitMap.entrySet() ){
			if( e.getValue().isActive() ){
				activeList.add(e.getKey());
			}
		}
		return activeList;
	}

	/**
	 * 
	 * フィルタ用にログイン中のマネージャリストを作成します
	 * 
	 * @return ログイン中のマネージャリスト
	 */
	public static String[] createManagerSelectValues() {
		List<String> list = new ArrayList<>();
		list.add(""); // blank
		list.addAll(getInstance().getActiveManagerMap().keySet());
		return list.toArray(new String[list.size()]);
	}

	public static Set<String> getAllOptions() {
		Set<String> allOptions = new HashSet<>();
		for( Map.Entry<String, RestConnectUnit> e : getInstance().connectUnitMap.entrySet() ){
			allOptions.addAll(e.getValue().getOptions());
		}
		return allOptions;
	}

	/**
	 * ログインに用いられているユーザーIDかをチェック
	 * 
	 * @param managerName
	 *            対象となるマネージャ名
	 * @param userId
	 *            チェック対象ユーザーID
	 * @return
	 */
	public static boolean hasLoginUser( String managerName, String userId ){
		return getLoginUserId(managerName).equals(userId);
	}

	/**
	 * 指定されたマネージャのログインに用いているユーザIDを返します。
	 * 
	 * @param managerName マネージャ名
	 * @return ユーザID
	 */
	public static String getLoginUserId(String managerName) {
		return getInstance().getActiveManagerMap().get(managerName).getUserId();
	}

	/**
	* Hinemosマネージャからログアウト
	*/
	public static void logout( String managerName ) {
		RestConnectManager connectMgr = getInstance();
		m_log.info( "Logout " + managerName );
		synchronized( connectMgr.connectUnitMap ){
			connectMgr.connectUnitMap.get(managerName).disconnect();
		}
	}

	/**
	 *  Hinemosマネージャを管理対象から削除
	 */
	public static void delete( String managerName ) {
		RestConnectManager connectMgr = getInstance();
		m_log.debug( "Delete " + managerName );
		synchronized( connectMgr.connectUnitMap ){
			connectMgr.connectUnitMap.remove(managerName);
		}
		
		RestLoginManager.saveLoginState();
	}

	/**
	*  設定情報変更日時（エポックミリ秒）を取得
	*/
	public static long getSetupExecuteTime() {
		return getInstance().setupExecuteTime;
	}

	/**
	*  設定情報変更日時（エポックミリ秒）を設定
	*/
	public static void setSetupExecuteTime(long setupExecuteTime) {
		getInstance().setupExecuteTime = setupExecuteTime;
	}


}
