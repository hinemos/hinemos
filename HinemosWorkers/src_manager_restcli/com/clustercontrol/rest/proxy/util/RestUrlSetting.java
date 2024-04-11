/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.proxy.util;

import java.net.Proxy.Type;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.HinemosToken;
import org.openapitools.client.model.LoginRequest;
import org.openapitools.client.model.LoginResponse;

import com.burgstaller.okhttp.DispatchingAuthenticator;
import com.clustercontrol.bean.RestHeaderConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.ApiClient;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.version.util.VersionUtil;

import okhttp3.OkHttpClient;

/**
 * Restの基底URL毎での接続に関する設定とロジックを保持する
 * 
 */
public class RestUrlSetting {
	// ログ
	private static Log m_log = LogFactory.getLog(RestUrlSetting.class);

	private String apiUrlStr = null;
	private String loginUrlStr = null;
	private ManagerRestConnectManager restConnectManager = null;
	private HinemosToken token = null;

	public static final String _pathPrefix = "api/";
	private static final String _clientVersion = VersionUtil.getVersionMajor();

	// Restの接続種別を定義（接続種別ごとに既定となるURLが変わるため
	public enum RestKind {
		AccessRestEndpoints("AccessRestEndpoints"), JobRestEndpoints("JobRestEndpoints");

		private final String name;

		private RestKind(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public RestUrlSetting(ManagerRestConnectManager restConnectManager, RestKind restKind) {
		this.restConnectManager = restConnectManager;
		// Pathの補完
		apiUrlStr = restConnectManager.getUrlPrefix() + _pathPrefix + restKind.getName();
		loginUrlStr = restConnectManager.getUrlPrefix() + _pathPrefix + RestKind.AccessRestEndpoints.getName();
	}

	/**
	 * マネージャへの接続切断
	 */
	public void connect() throws RestConnectFailed, HinemosUnknown {
		// ログイン
		LoginRequest req = new LoginRequest();
		req.setUserId(restConnectManager.getUserId());
		req.setPassword(restConnectManager.getPassword());
		try {
			DefaultApi defaultApi = new DefaultApi(getApiClient(loginUrlStr));
			LoginResponse res = new AccessRestClientWrapper(defaultApi).loginByUrl(req);
			token = res.getToken();
		} catch (RestConnectFailed ape) {
			throw ape;
		} catch (Exception e) {
			throw new HinemosUnknown(e);
		}
	}

	public ApiClient getApiClient() throws RestConnectFailed {
		return getApiClient(apiUrlStr);
	}

	private ApiClient getApiClient(String urlStr) throws RestConnectFailed {
		try {
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

			// URL Basic認証 その他Httpヘッダを設定
			apiClient.setBasePath(urlStr);
			if (token != null) {
				apiClient.addDefaultHeader(RestHeaderConstant.AUTHORIZATION,
						RestHeaderConstant.AUTH_BEARER + " " + token.getTokenId());
			}
			apiClient.addDefaultHeader(RestHeaderConstant.CLIENT_DT_FORMAT,
					RestCommonConverter.getRestDateTimeFormat().toPattern());
			apiClient.addDefaultHeader(RestHeaderConstant.CLIENT_LANG_SET, Locale.getDefault().getLanguage());
			apiClient.addDefaultHeader(RestHeaderConstant.CLIENT_VERSION, _clientVersion);

			// プロキシ設定(SSLより先に設定すること)
			if (restConnectManager.getProxy() != null && !(restConnectManager.getProxy().type().equals(Type.DIRECT))) {
				OkHttpClient.Builder builder = new OkHttpClient.Builder();
				builder.proxy(restConnectManager.getProxy());
				if (restConnectManager.getProxyAuchenticator() != null) {
					if (restConnectManager.getProxyAuchenticator() instanceof DispatchingAuthenticator) {
						builder.addInterceptor(restConnectManager.getCacheInterceptor());
						builder.proxyAuthenticator(restConnectManager.getProxyAuchentticatorDecorator());
					} else {
						builder.proxyAuthenticator(restConnectManager.getProxyAuchenticator());
					}
				}
				OkHttpClient client = builder.build();
				apiClient.setHttpClient(client);
			}
			// SSLにおけるホスト認証対応（実施しない場合はその旨を設定。検証に関わる設定は別途JVM向け引数にて指定される）
			boolean doHostVerify = Boolean.valueOf(System.getProperty("https.hostVerify", "false"));
			if (!(doHostVerify)) {
				apiClient.setVerifyingSsl(false);
			}
			apiClient.setConnectTimeout(restConnectManager.getHttpConnectTimeout());
			apiClient.setReadTimeout(restConnectManager.getHttpRequestTimeout());
			apiClient.setWriteTimeout(restConnectManager.getHttpRequestTimeout());

			return apiClient;

		} catch (Exception e) {
			m_log.warn("failed creating DefaultApi.", e);
			throw new RestConnectFailed(e.getMessage(), e.getCause());

		}
	}

	/**
	 * マネージャとの接続を切断
	 */
	public void disconnect() {
		try {
			DefaultApi defaultApi = new DefaultApi(getApiClient(loginUrlStr));
			new AccessRestClientWrapper(defaultApi).logout();
		} catch (RestConnectFailed ape) {
			// 通信失敗
			m_log.warn("disconnect : RestConnectFailed. urlPrrefix=" + restConnectManager.getUrlPrefix());
		} catch (InvalidUserPass | InvalidRole iup) {
			// 認証失敗（トークンが無効）
			m_log.warn("disconnect : past token invalid. urlPrrefix=" + restConnectManager.getUrlPrefix());
		} catch (Exception e) {
			// その他 想定外エラー
			m_log.error("disconnect : unknown Exception. urlPrrefix=" + restConnectManager.getUrlPrefix());
		}
	}
}
