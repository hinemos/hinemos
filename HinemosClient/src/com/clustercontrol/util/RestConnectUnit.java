/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.util;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.HinemosToken;
import org.openapitools.client.model.LoginRequest;
import org.openapitools.client.model.LoginResponse;
import org.openapitools.client.model.ManagerInfoResponse;

import com.clustercontrol.accesscontrol.dialog.LoginAccount;
import com.clustercontrol.accesscontrol.util.AccessRestClientWrapper;
import com.clustercontrol.bean.RestHeaderConstant;
import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidTimezone;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.ApiClient;
import com.clustercontrol.rest.ApiException;
import com.clustercontrol.rest.StringUtil;
import com.clustercontrol.version.util.VersionUtil;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * Hinemosマネージャとの接続を管理するクラス。
 * HAのようなマネージャクラスタへの対応のため、このクラスを実装する。
 *
 * 接続情報の設定時（Setメソッド）にカンマ区切りによって、複数URLの指定が可能で
 * 複数URLを合わせて一つのクラスタ単位として扱っている<Br>
 * 
 * 接続先URL毎のアクセストークンの管理を行っている。<Br>
 * 
 * 本クラス内で自動で接続先マネージャを切り替えることはしないが
 * getUrlSettingList にて取得された RestUrlSettingに対して順番に通信を行い
 * エラーなった場合  setUnreached することで、接続先切り替えを実現できる。<Br>
 * 
 * DefaultApi（Restとの通信におけるプロキシクラス）を用いる場合は
 * RestUrlSetting から getApiClientで取得したのち、RestUrlSequentialExecuterを介して
 * 呼び出すこと。 <Br>
 * 
 * 接続先の切り替えに伴うマネージャからのアクセストークンの取得のみ
 * 自動で実施される（getApiClientで実施）。<Br>
 * 
 */
public class RestConnectUnit {

	// ログ
	private static Log m_log = LogFactory.getLog( RestConnectUnit.class );

	private List<String> urlList;
	private String userId;
	private String password;
	private String managerName;
	private int status = LoginAccount.STATUS_UNCONNECTED;

	//url毎の属性情報を管理するMap。キーは urlListのレコード
	//取得したログイントークン
	private final Map<String,HinemosToken> tokenMap = new ConcurrentHashMap<String,HinemosToken>();
	//取得したログイントークンの失効日時
	private final Map<String,Long> tokenExpireTimeMap = new ConcurrentHashMap<String,Long>();
	//前回アクセス時に到達不可だった場合の日時
	private final Map<String,Long> unreachedTimeMap = new ConcurrentHashMap<String,Long>();

	//優先順リスト
	private List<String> priorUrlList = null;
	// 所持オプション
	private Set<String> options = new HashSet<>();

	// ApiClientのインスタンス
	// コネクションが都度生成しないようインスタンスをThreadLocalに保持して再利用する
	private static ThreadLocal<ApiClient> m_apiClient = new ThreadLocal<ApiClient> () {
		@Override
		protected ApiClient initialValue() {
			m_log.debug("initialValue");
			return null;
		}
	};
	// ApiClientのインスタンスの生成時間（生成後の設定変更有無判断に利用する）
	private static ThreadLocal<Long> m_apiCreateTime = new ThreadLocal<Long> () {
		@Override
		protected Long initialValue() {
			m_log.debug("initialValue m_apiCreateTime");
			return 0L;
		}
	};
	

	/**
	 * 利便性向上のため、入力を正しいURLに自動整形する
	 * @param  raw   raw input.
	 * @return a well-formed or reformed URL for manager connection.
	 */
	private String validateManagerURL(String raw){
		String newURL = raw.trim();
		boolean hasProtocol = newURL.matches("\\w+://.*");
		if(!hasProtocol){
			newURL = "http://" + newURL;
		}

		try {
			URL url = new URL(newURL);
			String formatted = url.getProtocol() + "://" + url.getHost();
			if( -1 != url.getPort()){
				formatted += ":" + url.getPort();
			}else if(!hasProtocol){ // Protocolが明記されていない場合デフォルト(8080)を代入
					formatted += ":" + 8080;
			}
			if( null != url.getPath() && !url.getPath().isEmpty()){
				formatted += url.getPath();
			}else{
				formatted += "/HinemosWeb/";
			}
			if( !formatted.endsWith("/") ){
				formatted += "/";
			}
			newURL = formatted;
		} catch (MalformedURLException e) {
			m_log.error("Invalid URL: " + newURL, e);
		}
		return newURL;
	}

	/**
	 * マネージャ（クラスタ）への接続情報の設定
	 * 
	 * @param urlListStr
	 *            接続先URL. カンマ区切りで複数指定可能。
	 * @param userId
	 *            ユーザID
	 * @param password
	 *            パスワード
	 * @param managerName
	 *            マネージャ名
	 */
	public void set(String urlListStr, String userId, String password, String managerName) {
		List<String> urlList = new ArrayList<String>();
		for (String urlStr : urlListStr.split(",")) {
			String url = validateManagerURL(urlStr.trim());
			urlList.add(url);
		}

		this.urlList = Collections.unmodifiableList(urlList);
		this.userId = userId;
		this.password = password;
		this.managerName = managerName;

		try {
			PriorUrlListLock.writeLock();

			priorUrlList = new ArrayList<String>(urlList);
		} finally {
			PriorUrlListLock.writeUnlock();
		}
		
	}
	
	/**
	 * URLに対して到達不可フラグを設定<Br>
	 * 
	 * 複数URL管理時のみ該当のURLの優先順位を下げ、到達不可フラグを設定する。
	 * 
	 * @param  urlSetting  接続先URL設定
	 */
	public void setUnreached(RestUrlSetting urlSetting) {
		//クラスタに複数の接続先URLがある場合
		if (urlList.size() > 1) {
			//トークン取得ずみなら到達不可フラグを設定（次回の接続試行時に導通を確認する）
			if( tokenMap.containsKey(urlSetting.getUrlPrefix())){
				unreachedTimeMap.put(urlSetting.getUrlPrefix(),System.currentTimeMillis());
			}
			//優先順を調整
			try {
				PriorUrlListLock.writeLock();
				int recCount =priorUrlList.size();
				for(int index = 0; index < recCount; index++ ){
					if(priorUrlList.get(index).equals(urlSetting.getUrlPrefix())){
						String lastPriorUrl = priorUrlList.remove(index);
						priorUrlList.add(lastPriorUrl);
					}
				}
			} finally {
				PriorUrlListLock.writeUnlock();
			}
		}
	}

	private static class PriorUrlListLock {
		private static final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
		public static void readLock() {
			_lock.readLock().lock();
		}
		public static void readUnlock() {
			_lock.readLock().unlock();
		}
		public static void writeLock() {
			_lock.writeLock().lock();
		}
		public static void writeUnlock() {
			_lock.writeLock().unlock();
		}
	}

	/**
	 * 
	 * Rest接続向URLの設定管理クラス
	 * 
	 * URLと付帯設定を元にDefaultApi（Restとの通信におけるプロキシクラス）を払い出す
	 */
	public class RestUrlSetting {
		private RestConnectUnit parentUnit;
		private final String urlPrefix;
		private RestKind restKind = null;
		private String restDatetimeFormat = "yyyy/MM/dd HH:mm:ss";

		public static final String _pathPrefix = "api/";
		private final String _clientVersion = VersionUtil.getVersionMajor();

		private RestUrlSetting(RestConnectUnit parentUnit, String urlPrefix, RestKind restKind) {
			this.parentUnit = parentUnit;
			this.urlPrefix = urlPrefix;
			this.restKind = restKind;
		}

		/**
		 * Restとの通信におけるプロキシクラスを取得する
		 */
		public ApiClient getApiClient() throws RestConnectFailed ,HinemosUnknown {
			if(tokenMap.containsKey(urlPrefix) && unreachedTimeMap.containsKey(urlPrefix)){
				//ログイン済み(アクセストークン保持)で前回到達不可なら接続チェックを実施する
				try{
					unreachedTimeMap.remove(urlPrefix);// ヘルスチェックより先に実施しないとgetApiClientの再帰呼び出しで無限ループするので注意
					(new AccessRestClientWrapper(parentUnit)).connectCheckByUrl(this);
				}catch(RestConnectFailed ape){
					unreachedTimeMap.put(urlPrefix, System.currentTimeMillis());
					throw ape;
				}catch(InvalidUserPass | InvalidRole iup){
					// 認証失敗（トークンが無効）なら、取得したアクセストークンを破棄する。
					m_log.warn("getApiClient : past token invalid. urlPrefix="+urlPrefix);
					tokenMap.remove(urlPrefix);
					tokenExpireTimeMap.remove(urlPrefix);
				}catch(Exception e){
					//その他 想定外エラーでも 取得したトークンを破棄する。
					m_log.error("getApiClient : unknown Exception. urlPrefix="+urlPrefix ,e);
					tokenMap.remove(urlPrefix);
					tokenExpireTimeMap.remove(urlPrefix);
				}
			}
			//未ログイン（アクセストークンなし）だった場合、先にログインを実施する
			if(!tokenMap.containsKey(urlPrefix)){
				LoginRequest req = new LoginRequest();
				req.setUserId(userId);
				req.setPassword(password);
				try{
					(new AccessRestClientWrapper(parentUnit)).loginByUrl(this ,req);
				}catch(RestConnectFailed ape){
					throw ape;
				}catch(Exception e){
					throw new HinemosUnknown(e);
				}
			}
			return getApiClient(true);
		}
		public ApiClient getApiClientForAuthorization() throws RestConnectFailed {
			return getApiClient(false);
		}

		public void setRestDatetimeFormat( String restDatetimeFormat ) {
			this.restDatetimeFormat = restDatetimeFormat;
		}
		
		private ApiClient initApiClient() {
			ApiClient apiClient = new ApiClient() {
				// application/octet-streamが付与されないための対応
				@Override
				public String selectHeaderAccept(String[] accepts) {
					if(accepts.length == 0) {
						return null;
					}
					return StringUtil.join(accepts, ",");
				}
				
				// ファイル名にマルチバイト文字を含むファイルのアップロードのために対応
				@Override
				public RequestBody buildRequestBodyMultipart(Map<String, Object> formParams) {
					MultipartBody.Builder mpBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
					for (Entry<String, Object> param : formParams.entrySet()) {
						if (param.getValue() instanceof File) {
							File file = (File) param.getValue();
							String encodeFileName = null;
							try {
								encodeFileName = URLEncoder.encode(file.getName(), "UTF-8");
							} catch (UnsupportedEncodingException e) {
								encodeFileName = file.getName();
							}
							Headers partHeaders = Headers.of("Content-Disposition", "form-data; name=\"" + param.getKey() + "\"; filename=\"" + encodeFileName + "\"");
							MediaType mediaType = MediaType.parse(guessContentTypeFromFile(file));
							mpBuilder.addPart(partHeaders, RequestBody.create(mediaType, file));
						} else {
							Headers partHeaders = Headers.of("Content-Disposition", "form-data; name=\"" + param.getKey() + "\"");
							mpBuilder.addPart(partHeaders, RequestBody.create(null, parameterToString(param.getValue())));
						}
					}
					return mpBuilder.build();
				}
			};
			apiClient.addDefaultHeader(RestHeaderConstant.CLIENT_LANG_SET, Locale.getDefault().getLanguage());
			apiClient.addDefaultHeader(RestHeaderConstant.CLIENT_VERSION, _clientVersion);

			//プロキシ設定(SSLより先に設定すること)
			if(RestConnectManager.getProxy() != null && !(RestConnectManager.getProxy().type().equals(Type.DIRECT)) ){
				OkHttpClient.Builder builder =new OkHttpClient.Builder();
				builder.proxy(RestConnectManager.getProxy());
				if(RestConnectManager.getProxyAuchenticator() != null){
					builder.addInterceptor(RestConnectManager.getCacheInterceptor());
					builder.proxyAuthenticator(RestConnectManager.getProxyAuchentticatorDecorator());
				}
				OkHttpClient client =builder.build();
				apiClient.setHttpClient(client);
			}

			// SSLにおけるホスト認証対応（実施しない場合はその旨を設定。検証に関わる設定は別途JVM向け引数にて指定される）
			boolean doHostVerify = Boolean.valueOf(System.getProperty("https.hostVerify", "false"));
			if( !(doHostVerify) ){
				apiClient.setVerifyingSsl(false);
			}

			//ユーザ指定のタイムアウト時間を反映
			apiClient.setConnectTimeout(RestConnectManager.getHttpRequestTimeout());
			apiClient.setReadTimeout(RestConnectManager.getHttpRequestTimeout());
			apiClient.setWriteTimeout(RestConnectManager.getHttpRequestTimeout());
			return apiClient;
		}

		private ApiClient getApiClient(boolean setAuthorization) throws RestConnectFailed {
			try {
				//Pathの補完
				String urlStr = urlPrefix +_pathPrefix + restKind.getName();

				ApiClient apiClient = m_apiClient.get();
				Long apiCreateTime = m_apiCreateTime.get();
				long setupExecuteTime = RestConnectManager.getSetupExecuteTime();
				if (apiClient == null || (apiCreateTime < setupExecuteTime)) {
					// 初回呼び出し時、もしくは接続設定変更発生時に ApiClientを初期化（設定変更内容を動作に反映するため）
					m_apiClient.set(initApiClient());
					m_apiCreateTime.set(System.currentTimeMillis());
					apiClient = m_apiClient.get();
					if( m_log.isDebugEnabled() ){
						m_log.debug("getApiClient() : m_apiClient is stored .m_apiCreateTime=" + m_apiCreateTime.get());
					}
				}
				// URLと各種ヘッダを設定
				apiClient.setBasePath(urlStr);
				if(setAuthorization){
					apiClient.addDefaultHeader(RestHeaderConstant.AUTHORIZATION,
							RestHeaderConstant.AUTH_BEARER + " " + tokenMap.get(urlPrefix).getTokenId());
				}
				apiClient.addDefaultHeader(RestHeaderConstant.CLIENT_DT_FORMAT, restDatetimeFormat);

				return apiClient;
			} catch (Exception e) {
				m_log.warn("failed creating ApiClient.", e);
				throw new RestConnectFailed(e.getMessage(),e.getCause());
				
			}
		}
		public String  getUrlPrefix() {
			return urlPrefix;
		}
	}


	/**
	 *  使用可能な順番でRestUrlSettingのリストを返す
	 * @param <T>
	 * @return
	 */
	public List<RestUrlSetting> getUrlSettingList(RestKind restKind ) {
		try {
			PriorUrlListLock.readLock();

			List<RestUrlSetting> list = new ArrayList<RestUrlSetting>(priorUrlList.size());
			for (String url : priorUrlList) {
				list.add(new RestUrlSetting(this, url,restKind));
			}
			return Collections.unmodifiableList(list);
		} finally {
			PriorUrlListLock.readUnlock();
		}
	}

	public boolean isActive(){
		return LoginAccount.STATUS_CONNECTED == status;
	}

	public int getStatus(){
		return status;
	}
	
	/**
	 * 設定情報を元にマネージャに接続する。
	 * 
	 * 接続に成功した際、マネージャのオプション情報を保存する。
	 * 
	 * @return ログイン時に取得したマネージャ環境情報
	 */
	public ManagerInfoResponse connect() throws HinemosException, ApiException {
		m_log.debug("connect : " + urlList);
		ManagerInfoResponse managerInfo = null;	
		
		LoginRequest req = new LoginRequest();
		req.setUserId(userId);
		req.setPassword(password);
		//ログインメソッドの呼び出し＋ 取得したトークンの保存
		LoginResponse info = (new AccessRestClientWrapper(this)).login(req);
		managerInfo = info.getManagerInfo();
		// Check login result at first
		int managerTZOffset = managerInfo.getTimeZoneOffset();
		this.options = new HashSet<>(managerInfo.getOptions());

		// タイムゾーンの取得
		Integer clientTZOffset = TimezoneUtil.getTimeZoneOffset();
		m_log.debug("connect : client's timezone = " + clientTZOffset + " , " + managerName + "'s timezone = " + managerTZOffset + ", options = " + options);

		// クライアントが保持するタイムゾーンと接続先マネージャより取得したタイムゾーンが一致しない場合は
		// ログインを失敗させる。
		if( clientTZOffset != null && clientTZOffset != managerTZOffset ){
			m_log.info("connect : " + managerName + "'s timezone(" + managerTZOffset + ") does not match client's(" + clientTZOffset + ")"); 
			throw new InvalidTimezone();
		}

		// クライアントがタイムゾーンを持たない場合、マネージャから取得したタイムゾーンを保持する。
		if( clientTZOffset == null ){
			TimezoneUtil.setTimeZoneOffset(managerTZOffset);
		}

		// Add to active manager list after login success(no exception occurred)
		status = LoginAccount.STATUS_CONNECTED;

		return managerInfo;
	}

	/**
	 * マネージャとの接続を切断
	 */
	public void disconnect(){
		m_log.debug("disconnect : " + urlList);
		try{
			(new AccessRestClientWrapper(this)).logout();
		}catch(RestConnectFailed ape){
			// 通信失敗
			m_log.warn("disconnect : RestConnectFailed. urlList="+Collections.unmodifiableList(urlList));
		}catch(InvalidUserPass | InvalidRole iup){
			// 認証失敗（トークンが無効）
			m_log.warn("disconnect : past token invalid. urlList="+Collections.unmodifiableList(urlList));
		}catch(Exception e){
			//その他 想定外エラー
			m_log.error("disconnect : unknown Exception. urlList="+Collections.unmodifiableList(urlList));
		}
		//アクセストークンに関連する情報を破棄する。
		for( String urlPrefix : this.urlList ){
			tokenMap.remove(urlPrefix);
			unreachedTimeMap.remove(urlPrefix);
			tokenExpireTimeMap.remove(urlPrefix);
		}
		status = LoginAccount.STATUS_UNCONNECTED;
	}

	public String getUrlListStr() {
		if(null != urlList){
			return String.join(", ", getUrlList());
		}else{
			return null;
		}
	}

	public List<String> getUrlList() {
		return urlList;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getManagerName() {
		return managerName;
	}

	public Set<String> getOptions() {
		return options;
	}
	
	public void setHinemosToken(String url, HinemosToken info) {
		Long currentTime =  System.currentTimeMillis();
		tokenMap.put(url, info);
		//トークンの失効日時は ValidTermMinitesとシステム時刻 を元に算出する（HinemosTokenの設定値はManagerの時計を元にしているので Clientとずれてる可能性があるため）
		Long expireTime =currentTime + (info.getValidTermMinites() * 60 * 1000);
		tokenExpireTimeMap.put(url,expireTime );
		if(m_log.isDebugEnabled()){
			m_log.debug("setHinemosToken :expireTime="+expireTime);
		}
	}
	
	/**
	 * アクセストークンの更新（必要時のみ実施）。<Br>
	 * 
	 * 定周期にて実施される想定<Br>
	 * 
	 * @param checkTerm
	 *            チェック間隔(分)
	 */	
	
	public void  updateTokenIfNeeded (int checkTerm){
		for( String urlPrefix : this.urlList ){
			HinemosToken token =  tokenMap.get(urlPrefix);
			Long expireTime = tokenExpireTimeMap.get(urlPrefix);
			if( token != null &&  expireTime != null ){
				//アクセストークンの失効日時(余裕時間30秒)をチェックして、次回のチェック時までに失効しているなら reloginメソッドを用いてトークンを更新。
				Long nextCheckTime = System.currentTimeMillis() + ( checkTerm * 60L * 1000L) + 30000L;
				if(m_log.isDebugEnabled()){
					m_log.debug("updateTokenIfNeeded :nextCheckTime="+nextCheckTime);
				}
				if( nextCheckTime > expireTime ){
					try{
						(new AccessRestClientWrapper(this)).relogin();
					}catch(RestConnectFailed ape){
						// 通信失敗
						m_log.warn("disconnect : RestConnectFailed . urlList="+Collections.unmodifiableList(urlList));
					}catch(InvalidUserPass | InvalidRole iup){
						// 認証失敗（トークンが無効）
						m_log.warn("disconnect : past token invalid. urlList="+Collections.unmodifiableList(urlList));
					}catch(Exception e){
						//その他 想定外エラー
						m_log.error("disconnect : unknown Exception. urlList="+Collections.unmodifiableList(urlList));
					}
				}
				
			}
		}
	}
	
}
