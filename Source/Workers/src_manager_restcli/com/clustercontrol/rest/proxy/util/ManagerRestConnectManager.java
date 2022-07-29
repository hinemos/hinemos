/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.proxy.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.DispatchingAuthenticator;
import com.burgstaller.okhttp.basic.BasicAuthenticator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.jobmanagement.factory.SelectJobLink;
import com.clustercontrol.jobmanagement.model.JobLinkSendSettingEntity;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

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
public class ManagerRestConnectManager {
	// ログ
	private static Log m_log = LogFactory.getLog(ManagerRestConnectManager.class);

	// プロキシ
	private Proxy m_proxy = Proxy.NO_PROXY;

	// プロキシ向けの認証設定
	private Authenticator m_proxyAuchenticator = null;
	private Credentials m_proxyCredentials = null;
	private CachingAuthenticatorDecorator m_proxyAuchentticatorDecorator = null;
	private AuthenticationCacheInterceptor m_proxyAuthenticationCacheInterceptor = null;
	private Map<String, CachingAuthenticator> m_proxyAuthCache = null;

	private String m_urlPrefix = "";
	private String m_userId;
	private String m_password;
	private final static int m_httpConnectTimeout = 10000;
	private final static int m_httpRequestTimeout = 60000;

	public ManagerRestConnectManager(String settingId, String facilityId) {
		try {
			NodeInfo nodeInfo = new RepositoryControllerBean().getNode(facilityId);
			String managerAddress = "";
			if (nodeInfo.getIpAddressVersion() == 4) {
				managerAddress = nodeInfo.getIpAddressV4();
			} else if (nodeInfo.getIpAddressVersion() == 6) {
				managerAddress = nodeInfo.getIpAddressV6();
			}
			JobLinkSendSettingEntity settingInfo = new SelectJobLink().getJobLinkSendSetting(settingId);
			m_urlPrefix = String.format("%s://%s:%s/HinemosWeb/", settingInfo.getProtocol(), managerAddress,
					Integer.toString(settingInfo.getPort()));
			m_userId = settingInfo.getHinemosUserId();
			m_password = settingInfo.getHinemosPassword();

			if (settingInfo.getProxyFlg()) {
				m_proxy = new Proxy(Proxy.Type.HTTP,
						new InetSocketAddress(settingInfo.getProxyHost(), settingInfo.getProxyPort()));

				if (settingInfo.getProxyUser() != null && settingInfo.getProxyUser().length() > 0) {
					m_proxyCredentials = new Credentials(settingInfo.getProxyUser(), settingInfo.getProxyPassword());
					final DigestAuthenticator digestAuthenticator = new DigestAuthenticator(m_proxyCredentials);
					final BasicAuthenticator basicAuthenticator = new BasicAuthenticator(m_proxyCredentials);
					final RestProxyNTLMAuthenticator ntlmAuthenticator = new RestProxyNTLMAuthenticator(
							settingInfo.getProxyUser(), settingInfo.getProxyPassword(), "");
					final DispatchingAuthenticator dispatchingAuthenticator = new DispatchingAuthenticator.Builder()
							.with("digest", digestAuthenticator).with("basic", basicAuthenticator).build();
					Authenticator proxyAuthenticator = new CachingAuthenticator() {
						Authenticator useAuth = null;

						@Override
						public Request authenticate(Route route, Response response) throws IOException {
							for (String header : response.headers().values(DigestAuthenticator.PROXY_AUTH)) {
								// Digest認証要求 もしくは Basic認証要求
								if (header.startsWith("Digest") || header.startsWith("Basic")) {
									Request rest = dispatchingAuthenticator.authenticate(route, response);
									// 認証キャッシュ機構（Inspectorでヘッダを設定）が動作するように
									// proxyAuchenticator
									// インスタンスをDispatchingAuthenticatorに切り替え
									useAuth = dispatchingAuthenticator;
									m_proxyAuchenticator = dispatchingAuthenticator;
									m_proxyAuchentticatorDecorator = new CachingAuthenticatorDecorator(
											dispatchingAuthenticator, m_proxyAuthCache);
									m_log.debug("new Digest =" + rest.headers());
									return rest;
								}
								// NTLM認証要求
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
							if (useAuth instanceof DispatchingAuthenticator) {
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
				} else {
					m_proxyAuchenticator = null;
					m_proxyCredentials = null;
					m_proxyAuthCache = null;
					m_proxyAuthenticationCacheInterceptor = null;
					m_proxyAuchentticatorDecorator = null;
				}
			} else {
				m_proxy = Proxy.NO_PROXY;
			}
		} catch (JobMasterNotFound | FacilityNotFound | HinemosUnknown | InvalidRole e) {
			m_log.error(e.getMessage());
		}
	}

	public Proxy getProxy() {
		return m_proxy;
	}

	/**
	 * DefaultApiによるプロキシ認証向けのAuthenticatorを取得
	 * 
	 * @return AuthenticationCacheInterceptor
	 */
	public Authenticator getProxyAuchenticator() {
		return m_proxyAuchenticator;
	}

	/**
	 * DefaultApiによるプロキシ認証向けのAuthenticationCacheInterceptorを取得
	 * 
	 * @return AuthenticationCacheInterceptor
	 */
	public AuthenticationCacheInterceptor getCacheInterceptor() {
		return m_proxyAuthenticationCacheInterceptor;
	}

	/**
	 * DefaultApiによるプロキシ認証向けのCachingAuthenticatorDecoratorを取得
	 * 
	 * @return CachingAuthenticatorDecorator
	 */

	public CachingAuthenticatorDecorator getProxyAuchentticatorDecorator() {
		return m_proxyAuchentticatorDecorator;
	}

	/**
	 * http接続タイムアウトの設定値を取得
	 */
	public int getHttpConnectTimeout() {
		return m_httpConnectTimeout;
	}

	/**
	 * http要求タイムアウトの設定値を取得
	 */
	public int getHttpRequestTimeout() {
		return m_httpRequestTimeout;
	}

	/**
	 * マネージャのUrlPrefixを取得
	 */
	public String getUrlPrefix() {
		return m_urlPrefix;
	}

	/**
	 * マネージャのHinemosユーザIDを取得
	 */
	public String getUserId() {
		return m_userId;
	}

	/**
	 * マネージャのHinemosパスワードを取得
	 */
	public String getPassword() {
		return m_password;
	}

}
