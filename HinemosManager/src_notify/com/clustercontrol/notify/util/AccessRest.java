/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
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
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HttpMethodTypeConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.bean.RestAccessAuthTypeConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.commons.util.StringUtil;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.RestAccessNotFound;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyRestInfo;
import com.clustercontrol.notify.restaccess.model.RestAccessAuthHttpHeader;
import com.clustercontrol.notify.restaccess.model.RestAccessInfo;
import com.clustercontrol.notify.restaccess.model.RestAccessSendHttpHeader;
import com.clustercontrol.notify.restaccess.util.RestAccessQueryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.InternalIdAbstract;
import com.clustercontrol.util.StringBinder;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * RESTにアクセスして通知処理を行うクラス<BR>
 *
 * 重要度に対応したRESTアクセス設定に基づいて処理する。<BR>
 *
 * URL認証の場合<BR>
 *  認証用URLのレスポンスから アクセストークンを取得し、送信時に置換変数として利用可能にしている。<BR>
 *  取得したアクセストークンは規定時間の間、キャッシュして再利用する。<BR>
 *
 */
public class AccessRest implements Notifier {

	/** アクセストークン向け置換変数 */
	private static final String _TOKEN_VARIABLE_NAME = "REST_ACCESS_TOKEN";

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( AccessRest.class );

	/**
	 * RESTアクセスID毎 の トークンキャッシュ（本クラスに内に閉じたデータなので キャッシュ向け共通機構は使わない）
	 * key:restAccessId ,value:token String
	 */
	private  static ConcurrentHashMap<String,String> m_tokenCacheValueMap = new ConcurrentHashMap<String,String>();

	/**
	 * RESTアクセスID毎 の トークンキャッシュの期限。
	 * key:restAccessId,value:expireTime(millsec)
	 */
	private  static ConcurrentHashMap<String,Long> m_tokenCacheExpireTimeMap = new ConcurrentHashMap<String,Long>();

	/** WebProxy でのキャッシュキャンセルの要否(RESTなのでキャッシュ無し) */
	private static boolean m_cancelProxyCache = true;

	/** httpのリダイレクト考慮の要否(考慮不要なのでfalse) */
	private static boolean m_redirectEnabled = false;

	/** httpのkeep-alive要否(認証リクエストと送信リクエストを連続で行う可能性があるのでtrue) */
	private static boolean m_keepAlive = true;
	
	/** httpのリトライ時の間隔（10秒） */
	private static long m_httpRequestRetryInternal =10 *1000 ;

	@Override
	public void notify(NotifyRequestMessage requestMessage)
			throws NotifyNotFound {
		if(m_log.isDebugEnabled()){
			m_log.debug("notify() " + requestMessage);
		}
		OutputBasicInfo outputInfo = requestMessage.getOutputInfo();
		String notifyId = requestMessage.getNotifyId();
		
		if(m_log.isDebugEnabled()){
			m_log.debug("sendlog() " + outputInfo);
		}

		// 該当する重要度の通知のRESTアクセスIDを取得する
		NotifyRestInfo notifyInfo = QueryUtil.getNotifyRestInfoPK(notifyId);
		String usedRestAccessId = getUseRestAccesId(outputInfo.getPriority(),notifyInfo);
		if ( usedRestAccessId == null || usedRestAccessId.isEmpty()) {
			// 通知不要な設定なら処理を終了
			return;
		}

		//通知設定に紐づくRESTアクセス情報を取得
		RestAccessInfo restAccessInfo = null;
		try {
			restAccessInfo = RestAccessQueryUtil.getRestAccessInfoPK(usedRestAccessId);
		} catch (RestAccessNotFound | InvalidRole e) {
			// 取得失敗時は内部エラーを出力して終了
			String detailMsg = e.getClass().getName() + ":" + e.getMessage() +":" + e.getStackTrace()[1].getMethodName();
			String[] args = { notifyId, usedRestAccessId };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_021,args, detailMsg);
			return;
		}
		
		//RESTアクセス情報に基づいてREST通知を行う
		AplLoggerRequest ret = execRestAccesss(requestMessage, notifyInfo, restAccessInfo);
		if( ret != null ){
			// 異常発生時は内部エラーを出力して終了
			AplLogger.put(ret.getInternalId(), ret.getMsgArgs() , ret.getDetailMsg());
		}

		return;
	}
	
	// 該当する重要度の通知のRESTアクセスIDを取得する
	private String getUseRestAccesId(int priority, NotifyRestInfo notifyInfo){

		Boolean validFlg = false;
		String usedRestAccessId = null;
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			validFlg = notifyInfo.getInfoValidFlg();
			usedRestAccessId = notifyInfo.getInfoRestAccessId();
			break;
		case PriorityConstant.TYPE_WARNING:
			validFlg = notifyInfo.getWarnValidFlg();
			usedRestAccessId = notifyInfo.getWarnRestAccessId();
			break;
		case PriorityConstant.TYPE_CRITICAL:
			validFlg = notifyInfo.getCriticalValidFlg();
			usedRestAccessId = notifyInfo.getCriticalRestAccessId();
			break;
		case PriorityConstant.TYPE_UNKNOWN:
			validFlg = notifyInfo.getUnknownValidFlg();
			usedRestAccessId = notifyInfo.getUnknownRestAccessId();
			break;
		default:
			break;
		}
		if (!(validFlg) || usedRestAccessId == null || usedRestAccessId.isEmpty()) {
			return null;
		}
		return usedRestAccessId;
		
	}
	
	/**
	 * REST通知を行う<br>
	 * 異常により内部エラーを通知する必要があれば エラー内容を戻り値として返す<br>
	 * 
	 * 通知としての処理とは別途に、画面からのテスト実行より呼出の可能性を考慮し<br>
	 * public メソッド化＋エラー通知は呼出元にて制御可能 としている。<br>
	 * 
	 * @param requestMessage
	 * @param notifyInfo
	 * @param restAccessInfo
	 * 
	 */
	public static AplLoggerRequest execRestAccesss(NotifyRequestMessage requestMessage, NotifyRestInfo notifyInfo, RestAccessInfo restAccessInfo) {

		// httpクライアントを構築（必要ならWebプロキシの利用設定も構築）
		CloseableHttpClient httpClient = null;
		try{
			try {
				httpClient = getHttpClient(restAccessInfo);
			} catch ( Exception e) {
				//異常発生時は内部エラーを出力して終了
				String detailMsg = e.getClass().getName() + " : " + e.getMessage() +" : " + e.getStackTrace()[0].getMethodName();
				String[] args = { requestMessage.getNotifyId(), restAccessInfo.getRestAccessId() };
				return new AplLoggerRequest(InternalIdCommon.PLT_NTF_SYS_023,args, detailMsg);
			}
			String tokenId = null;
			// 必要なら、設定に基づいて認証RESTアクセスを実施し、結果（認証トークン）を取得
			if (restAccessInfo.getAuthType() == RestAccessAuthTypeConstant.TYPE_URL) {
				try {
					tokenId = authRestAccesss(httpClient, restAccessInfo);
				} catch (IOException | HinemosUnknown | ParseException  e) {
					//異常発生時は内部エラーを出力して終了
					String detailMsg = e.getClass().getName() + " : " + e.getMessage() +" : " + e.getStackTrace()[0].getMethodName();
					String[] args = { requestMessage.getNotifyId(), restAccessInfo.getRestAccessId(), restAccessInfo.getAuthUrlString() };
					return new AplLoggerRequest(InternalIdCommon.PLT_NTF_SYS_022,args, detailMsg);
				}
			}
			// 設定に基づいて送信アクセスを実施
			try {
				sendRestAccesss(httpClient, requestMessage, notifyInfo, restAccessInfo, tokenId);
			} catch (IOException | HinemosUnknown e) {
				//異常発生時は内部エラーを出力して終了
				String detailMsg = e.getClass().getName() + ":" + e.getMessage() +":" + e.getStackTrace()[0].getMethodName();
				String[] args = { requestMessage.getNotifyId(), restAccessInfo.getRestAccessId(), restAccessInfo.getSendUrlString() };
				return new AplLoggerRequest(InternalIdCommon.PLT_NTF_SYS_022,args, detailMsg);
			}
			return null;
		}finally{
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					m_log.error("execRestAccesss() :  Unable to close httpClient due to IOException. Message="
							+ e.getMessage());
				}
			}
		}
	}

	// URL認証設定に戻づく認証向けRESTアクセスを行う（戻り値は認証用トークン）
	private static String authRestAccesss( CloseableHttpClient httpClient,  RestAccessInfo restAccessInfo ) throws HinemosUnknown,IOException,ParseException {
		if(m_log.isDebugEnabled()){
			m_log.debug("authRestAccesss() : start :  restAccessId = " + getRestAccessId(restAccessInfo));
		}

		//トークンキャッシュ アクセス用ロックオブジェクト（ID 毎で排他）
		ILock tokenCacheAccesslock = getLock(getRestAccessId(restAccessInfo));
		try{
			// トークンキャッシュを参照するので readロック
			tokenCacheAccesslock.readLock();

			// RestアクセスIDのキャッシュをチェックして有効期間内なら再利用
			String accessToken  = m_tokenCacheValueMap.get(restAccessInfo.getRestAccessId());
			if( accessToken != null ){
				Long expireTime = m_tokenCacheExpireTimeMap.get(restAccessInfo.getRestAccessId());
				if( expireTime != null && expireTime > HinemosTime.currentTimeMillis()){
					return accessToken;
				}
			}
		} finally {
			tokenCacheAccesslock.readUnlock();
		}
	
		try{
			// キャッシュにトークンを登録するので writeロックに切り替える 
			// readからwiteに切り替える隙間によって、タイムアウト前に連続変更の可能性があるが、動作に支障はないので目をつむる。
			tokenCacheAccesslock.writeLock();
			// httpリクエストを構築（メソッド別）
			HttpUriRequestBase request = getUrlRequestBase(restAccessInfo.getAuthUrlMethodType(),restAccessInfo.getAuthUrlString());
	
			// 認証アクセス向けのhttpヘッダー情報を取得
			if( restAccessInfo.getAuthHttpHeaders() != null){
				for( RestAccessAuthHttpHeader rec : restAccessInfo.getAuthHttpHeaders() ){
					request.addHeader(rec.getKey(), rec.getValue());
				}
			}
	
			// 必要(POST or PUT)なら 認証アクセス向けのhttpボディ情報 取得
			if (restAccessInfo.getAuthUrlMethodType() == HttpMethodTypeConstant.TYPE_POST
					|| restAccessInfo.getAuthUrlMethodType() == HttpMethodTypeConstant.TYPE_PUT) {
				StringEntity requestEntity = new StringEntity(restAccessInfo.getAuthUrlBody(),StandardCharsets.UTF_8);
				request.setEntity(requestEntity);
			}
			
			// 制御設定に基づいて認証アクセス（リトライ付き） 
			Long accessStartTime = HinemosTime.currentTimeMillis();
			CloseableHttpResponse response = null;
			IOException resultException= null;
			for (int execCount = 0; execCount <= restAccessInfo.getHttpRetryNum(); execCount++) {
				if (execCount > 0) {
					// リトライ時は規定時間待ってから実行する
					try {
						Thread.sleep(m_httpRequestRetryInternal);
					} catch (InterruptedException e) {
						// 割り込みが入っても無視
						m_log.info("authRestAccesss() : Sleep was interrupted. cause=" + e.getMessage());
					}
				}
				try{
					resultException = null;
					if(m_log.isDebugEnabled()){
						m_log.debug("authRestAccesss() : httpClient.execute :  restAccessId = "
								+ getRestAccessId(restAccessInfo) + " request=" + request.toString());
					}
					accessStartTime = HinemosTime.currentTimeMillis();
					response = httpClient.execute(request);
					// レスポンスコードが200OKでなければリトライ
					if (response == null || !(isOkStatus(response.getCode()))) {
						continue;
					}
					break;
				}catch( IOException e){
					m_log.warn("authRestAccesss() : httpClient.execute :"  + e.getClass().getSimpleName() + ", " + e.getMessage() + "  : restAccessId=" + restAccessInfo.getRestAccessId());
					resultException =  e;
				}
			}
			// リトライしても送信失敗なら 異常をエスカレーション
			if( resultException != null  ){
				throw resultException;
			}
			// レスポンスコードが200OKでなければエラー扱いとする。
			if (response == null || !(isOkStatus(response.getCode()))) {
				String detailMessage =null; 
				if(response == null ){
					detailMessage = "http response code is null.  url= " + restAccessInfo.getAuthUrlString()
					+ " : restAccessId=" + getRestAccessId(restAccessInfo);
				}else{
					detailMessage = "http response code is not 2xx . url= " + restAccessInfo.getAuthUrlString()
							+ " : restAccessId=" + getRestAccessId(restAccessInfo) + " : code=" + response.getCode()
							+ " : body=" + translateHttpEntityToString(response);
				}
				m_log.warn("authRestAccesss() : " + detailMessage);
				throw new HinemosUnknown(detailMessage);
			}
			// トークン取得設定が不完全ならトークンの取得は行わない
			if (StringUtil.isNullOrEmpty(restAccessInfo.getAuthUrlGetRegex())) {
				return null;
			}
			if (restAccessInfo.getAuthUrlValidTerm() == null || restAccessInfo.getAuthUrlValidTerm() < 0) {
				return null;
			}
			
			// httpレスポンス(body)から設定に基づいて アクセストークンを切り出して キャッシュへ登録
			Long expireTime = accessStartTime + restAccessInfo.getAuthUrlValidTerm() ;
			try{
				String authResponseData = EntityUtils.toString(response.getEntity(),StandardCharsets.UTF_8);
				String accessToken = getAccessToken(authResponseData ,restAccessInfo.getAuthUrlGetRegex());
				// トークンが取得できなかった場合、キャッシュに登録しないが エラーにはしない（あり得るか不明だがトークンなしで認証するケースを考慮 ）
				if( accessToken != null &&  !(accessToken.isEmpty()) ){
					m_tokenCacheValueMap.put(restAccessInfo.getRestAccessId(),accessToken);
					m_tokenCacheExpireTimeMap.put(restAccessInfo.getRestAccessId(),expireTime);
				}
				if(m_log.isDebugEnabled()){
					m_log.debug("authRestAccesss() : end :  restAccessId = " + getRestAccessId(restAccessInfo)
							+ " : accessToken=" + accessToken + " : body=" + authResponseData);
				}
				return accessToken;
			}catch(IOException | ParseException e){
				m_log.warn("authRestAccesss() : get access token :"  + e.getClass().getSimpleName() + ", " + e.getMessage() + "  : restAccessId=" + restAccessInfo.getRestAccessId());
				throw e ;
			}
		} finally {
			tokenCacheAccesslock.writeUnlock();
		}
	}

	// 送信設定に基づいて、送信向けRESTアクセスを行う
	private static void sendRestAccesss(CloseableHttpClient httpClient, NotifyRequestMessage requestMessage,
			NotifyRestInfo notifyInfo, RestAccessInfo restAccessInfo, String tokenId)
			throws IOException, HinemosUnknown {
		if(m_log.isDebugEnabled()){
			m_log.debug("sendRestAccesss() : start :  restAccessId = " + getRestAccessId(restAccessInfo));
		}
		// 機能固有の置換変数としてアクセストークンを追加
		Map<String,String> localReplaceVariableParam =  new HashMap<String,String>();
		if( tokenId != null ){
			localReplaceVariableParam.put(_TOKEN_VARIABLE_NAME, tokenId);
		}
		// httpリクエストを構築（メソッド別）
		// URLに対する変数置換も実施する。
		String urlString = replaceVariable(requestMessage.getOutputInfo(), notifyInfo, localReplaceVariableParam, restAccessInfo.getSendUrlString());
		HttpUriRequestBase request = getUrlRequestBase(restAccessInfo.getSendHttpMethodType(),urlString);
		
		// 送信アクセス向けのhttpヘッダー情報を生成
		if( restAccessInfo.getSendHttpHeaders() != null){
			for( RestAccessSendHttpHeader rec : restAccessInfo.getSendHttpHeaders() ){
				//変数置換も実施する。
				String value = replaceVariable(requestMessage.getOutputInfo(), notifyInfo, localReplaceVariableParam, rec.getValue());
				if (m_log.isTraceEnabled()) {
					m_log.trace("sendRestAccesss() : addHeader : key=" + rec.getKey() + " ,value=" + value);
				}
				request.addHeader(rec.getKey(), value);
			}
		}
		// Basic認証なら認証ヘッダーを追加
		if (restAccessInfo.getAuthType() == RestAccessAuthTypeConstant.TYPE_BASIC ){
			String basicAuthorization = "Basic" + " " + Base64.encodeBase64String(
					(restAccessInfo.getAuthBasicUser().trim() + ":" + restAccessInfo.getAuthBasicPassword().trim())
							.getBytes(StandardCharsets.UTF_8));
			request.addHeader("Authorization", basicAuthorization);
		}

		// 送信アクセス向けのhttpボディ情報生成
		if (restAccessInfo.getSendHttpMethodType() == HttpMethodTypeConstant.TYPE_POST
				|| restAccessInfo.getSendHttpMethodType() == HttpMethodTypeConstant.TYPE_PUT) {
			//変数置換も実施する。
			String body = replaceVariable(requestMessage.getOutputInfo(), notifyInfo, localReplaceVariableParam,
					restAccessInfo.getSendHttpBody());
			if(m_log.isTraceEnabled() ){
				m_log.trace("sendRestAccesss() : setEntity : body=" + body);
			}
			StringEntity requestEntity = new StringEntity(body,StandardCharsets.UTF_8);
			request.setEntity(requestEntity);
		}
		
		// 制御設定に基づいて送信アクセス（リトライ付き）
		CloseableHttpResponse response = null;
		IOException resultException= null;
		for (int execCount = 0; execCount <= restAccessInfo.getHttpRetryNum(); execCount++) {
			if (execCount > 0) {
				// リトライ時は規定時間待ってから実行する
				try {
					Thread.sleep(m_httpRequestRetryInternal);
				} catch (InterruptedException e) {
					// 割り込みが入っても無視
					m_log.info("sendRestAccesss() : Sleep was interrupted. cause=" + e.getMessage());
				}
			}
			try{
				resultException = null;
				if(m_log.isDebugEnabled()){
					m_log.debug("sendRestAccesss() : httpClient.execute :  restAccessId = "
							+ getRestAccessId(restAccessInfo) + " : request=" + request.toString());
				}
				response = httpClient.execute(request);
				if (response == null || !(isOkStatus(response.getCode()))) {
					continue;
				}
				break;
			}catch( IOException e){
				m_log.warn("sendRestAccesss() : httpClient.execute.  url= " + restAccessInfo.getAuthUrlString() + " : restAccessId=" + getRestAccessId(restAccessInfo));
				resultException =  e;
			}
		}
		// リトライしても送信失敗なら 異常をエスカレーション
		if( resultException != null  ){
			throw resultException;
		}
		// レスポンスコードがOKでなければエラー扱いとする。
		if (!(isOkStatus(response.getCode()))) {
			String detailMessage = "http response code is not 2xx .  url= " + restAccessInfo.getSendUrlString()
					+ " : restAccessId=" + getRestAccessId(restAccessInfo) + " accessToken=" + tokenId + " code="
					+ response.getCode() + " body=" + translateHttpEntityToString(response);
			m_log.warn("sendRestAccesss() : " + detailMessage);
			throw new HinemosUnknown(detailMessage);
		}
	}

	
	
	// httpの制御設定に基づいてhttpクライアントを生成
	private static CloseableHttpClient getHttpClient(RestAccessInfo restAccessInfo)
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, URISyntaxException, HinemosUnknown {
		if(m_log.isDebugEnabled()){
			m_log.debug("getHttpClient() : start :  restAccessId = " + getRestAccessId(restAccessInfo));
		}

		boolean ssl_trustall = HinemosPropertyCommon.notify_rest_ssl_trustall.getBooleanValue();
		
		CloseableHttpClient m_client = null;
		CredentialsStore m_cledentialProvider = new BasicCredentialsProvider();
				
		PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();

		List<Header> headers = new ArrayList<>();

		HttpClientBuilder builder = HttpClients.custom()
				.setDefaultCredentialsProvider(m_cledentialProvider)
				.setDefaultHeaders(headers);

		// trustall =true なら SSL の認証カット
		if (ssl_trustall) {
			TrustStrategy trustStrategy = new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			};
			try {
				connectionManagerBuilder.setSSLSocketFactory(
						new SSLConnectionSocketFactory(new SSLContextBuilder().loadTrustMaterial(null, trustStrategy).build(),
						new NoopHostnameVerifier()));
			} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
				m_log.error("CloseableHttpClient() : setSSLSocketFactory : message= " + e.getMessage(), e);
				throw e;
			}
		}
		RequestConfig requestConfig = RequestConfig.custom()
				.setCookieSpec(StandardCookieSpec.RELAXED)
				.setConnectTimeout(Timeout.ofMilliseconds(restAccessInfo.getHttpConnectTimeout()))
				.setResponseTimeout(Timeout.ofMilliseconds(restAccessInfo.getHttpRequestTimeout()))
				.setRedirectsEnabled(m_redirectEnabled) 
				.build();
		builder.setDefaultRequestConfig(requestConfig);
	
		//デフォルトだと無限待ちの場合があるので、ソケットのブロッキング待ちタイムアウト時間をリクエスト待ちと合わせる
		SocketConfig socketConfig = SocketConfig.custom()
				.setSoTimeout(Timeout.ofMilliseconds(restAccessInfo.getHttpRequestTimeout())).build();
		connectionManagerBuilder.setDefaultSocketConfig(socketConfig);

		// プロキシを利用するなら、必要な設定を実施
		if( restAccessInfo.getUseWebProxy() ){
			URI uri;
			try {
				uri = new URI(restAccessInfo.getWebProxyUrlString());
			} catch (URISyntaxException e) {
				m_log.warn("CloseableHttpClient() : URISyntaxException . message=" + e.getMessage() + " url= "
						+ restAccessInfo.getWebProxyUrlString());
				throw e;
			}

			try {
				HttpHost proxy = new HttpHost( uri.getScheme() == null ? "https": uri.getScheme(), uri.getHost(),(int)restAccessInfo.getWebProxyPort().longValue());
				if (restAccessInfo.getWebProxyAuthUser() != null && restAccessInfo.getWebProxyAuthPassword() != null) {
					m_cledentialProvider.setCredentials(new AuthScope(proxy.getHostName(), proxy.getPort()), new UsernamePasswordCredentials(restAccessInfo.getWebProxyAuthUser() , restAccessInfo.getWebProxyAuthPassword().toCharArray()));
				}
				builder.setProxy(proxy);
			} catch (Exception e) {
				m_log.warn("CloseableHttpClient() : Exception at HttpHost . class="+ e.getClass().getName() + ", message=" + e.getMessage() + " proxy url= "
						+ restAccessInfo.getWebProxyUrlString() + ", proxy port=" + restAccessInfo.getWebProxyPort());
				throw new HinemosUnknown(e.getMessage(),e);
			}
			// プロキシのキャッシュコントロール（キャッシュ無し）を追加
			if (m_cancelProxyCache) {
				// https://www.ipa.go.jp/security/awareness/vendor/programmingv2/contents/405.html
				headers.add(new BasicHeader("Cache-Control", "no-cache"));
				headers.add(new BasicHeader("Pragma", "no-cache"));
			}
		}

		// httpキープアライブを必要なら追加
		if (m_keepAlive) {
			headers.add(new BasicHeader(HttpHeaders.CONNECTION, HttpHeaders.KEEP_ALIVE));
		}
		else {
			headers.add(new BasicHeader(HttpHeaders.CONNECTION, "Close"));
		}
		m_client = builder.setConnectionManager(connectionManagerBuilder.build()).build();

		return m_client;
	}

	// 引数を元にリクエストの基盤インスタンスを生成
	private static HttpUriRequestBase getUrlRequestBase( Integer methodType , String url ){
		
		HttpUriRequestBase request = null;
		switch (methodType) {
			case HttpMethodTypeConstant.TYPE_GET:
				request = new HttpGet(url);
				break;
			case HttpMethodTypeConstant.TYPE_POST:
				request = new HttpPost(url);
				break;
			case HttpMethodTypeConstant.TYPE_PUT:
				request = new HttpPut(url);
				break;
			case HttpMethodTypeConstant.TYPE_DELETE:
				request = new HttpDelete(url);
				break;
			default:
				request = new HttpGet(url);
				break;
		}
		return request;
	}
	
	// 正規表現を元にレスポンスからアクセストークンを取得
	private static String getAccessToken(String responseEntity , String patternRegex ){
		String ret = null;
		Pattern p = Pattern.compile(patternRegex);

		Matcher m = p.matcher(responseEntity);
		int groupCount = m.groupCount();
		
		// 正規表現に対し複数マッチなら最初の１つが優先
		// 正規表現の中に複数グループありなら最初の１つが優先 ※ group(0)だと全体を表してしまうので group(1)が最初
		while (m.find()) {
			if (groupCount >= 1) {
				ret = m.group(1);
			}
			break;
		}
		if( ret != null && ret.isEmpty() ){
			if(m_log.isTraceEnabled()){
				m_log.trace("getAccessToken() : ret is empty.  it is converted to null." );
			}
			// 取得したトークンが空ならnullにする
			ret = null;
		}
		return ret;
	}
	
	// 文字列の中の置換用変数を置換する
	private static String replaceVariable(OutputBasicInfo outputInfo, NotifyRestInfo notifyInfo, Map<String, String> localParam , String targetString ){
		if(m_log.isTraceEnabled()){
			m_log.trace("replaceVariable() : targetString =" + targetString);
		}
		
		// 文字列を置換する
		String message = targetString;

		try {
			int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
			ArrayList<String> inKeyList = StringBinder.getKeyList(message, maxReplaceWord);
			Map<String, String> param = NotifyUtil.createParameter(outputInfo,
					notifyInfo.getNotifyInfoEntity(), inKeyList);
			// 機能固有の置換文字列への対応
			for (Entry<String, String> entry : localParam.entrySet()) {
				if (!(param.containsKey(entry.getKey()))) {
					param.put(entry.getKey(), entry.getValue());
				}
			}
			
			StringBinder binder = new StringBinder(param);
			String ret = binder.replace(message);
			if(m_log.isTraceEnabled()){
				m_log.trace("replaceVariable() : nonmal end. ret =" + ret);
			}
			return ret;
		} catch (Exception e) {
			m_log.warn("getMessage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// 例外が発生した場合は、置換前の文字列を返す
			if(m_log.isTraceEnabled()){
				m_log.trace("replaceVariable() : exception occured. ret =" + message);
			}
			return message;
		}
	}

	private static String getRestAccessId( RestAccessInfo restAccessInfo ){
		if(restAccessInfo != null){
			return restAccessInfo.getRestAccessId();
		}
		return null;
	}
	
	// http response のEntity（Body）用のオブジェクトについて UTF-8エンコードでStringに変換する
	// エラー発生時は、decode error を結果として返す。
	private static String translateHttpEntityToString(CloseableHttpResponse target) {
		String ret;
		if( target == null || target.getEntity() ==null ){
			return null;
		}
		try {
			ret = EntityUtils.toString(target.getEntity() ,StandardCharsets.UTF_8);
		} catch (ParseException | IOException e) {
			m_log.warn("translateHttpEntityToString() : exception occured. message =" + e.getMessage());
			ret = "decode error";
		}
		return ret;
	}
	// レスポンスのステータスコードが問題ないか判断する。
	private static boolean isOkStatus( int statusCode ){
		if( 200 <= statusCode && statusCode <= 299 ){
			return true;
		}
		return false;
	}

	public static ILock getLock (String restAccessId) {
		ILockManager lm = LockManagerFactory.instance().create();
		return lm.create(AccessRest.class.getName() + "-" + restAccessId);
	}
	
	// INTERNAL通知リクエスト用クラス
	private static class AplLoggerRequest{
		private InternalIdAbstract internalId;
		private String[] msgArgs;
		private String detailMsg;

		public AplLoggerRequest(InternalIdAbstract internalId, String[] msgArgs, String detailMsg) {
			this.internalId= internalId;
			this.msgArgs= msgArgs;
			this.detailMsg= detailMsg;
		}
		public InternalIdAbstract getInternalId() {
			return internalId;
		}
		public String[] getMsgArgs() {
			return msgArgs;
		}
		public String getDetailMsg() {
			return detailMsg;
		}
		
	}

	/**
	 * 指定されたRESTアクセスIDのトークンキャッシュを削除します。（設定更新時向け）
	 */
	public static void clearRestAccessIdCache( String restAccessId ){
		
		//トークンキャッシュ アクセス用ロックオブジェクト（ID 毎で排他）
		ILock tokenCacheAccesslock = getLock(restAccessId);
		try{
			tokenCacheAccesslock.writeLock();
			m_tokenCacheValueMap.remove(restAccessId);
			m_tokenCacheExpireTimeMap.remove(restAccessId);
		} finally {
			tokenCacheAccesslock.writeUnlock();
		}
	}
}