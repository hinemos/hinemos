/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.dialog.LoginAccount;
import com.clustercontrol.accesscontrol.util.AccessEndpointWrapper;
import com.clustercontrol.fault.InvalidTimezone;
import com.clustercontrol.ws.access.HinemosUnknown_Exception;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.InvalidUserPass_Exception;
import com.clustercontrol.ws.access.ManagerInfo;
import com.sun.xml.internal.ws.client.BindingProviderProperties;
import com.sun.xml.internal.ws.developer.JAXWSProperties;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class EndpointUnit {

	// ログ
	private static Log m_log = LogFactory.getLog( EndpointUnit.class );

	private List<String> urlList;
	private String userId;
	private String password;
	private String managerName;
	private int status = LoginAccount.STATUS_UNCONNECTED;

	private List<String> priorUrlList = null;
	// 所持オプション
	private Set<String> options = new HashSet<>();
	
	private ConcurrentHashMap<String, Object> endpointMap = new ConcurrentHashMap<String, Object>();

	static {
		// HTTPSの証明書の取り扱いについて、環境変数の値を元にセットアップを行う
		// ホスト認証を行うか否か（Boolean.valueOfは、true以外の場合には全てFalseとするため、
		// 基本的にはホスト認証は行わない
		boolean doHostVerify = Boolean.valueOf(System.getProperty("https.hostVerify", "false"));
		
		if (doHostVerify == false) {
			// HostNameVerifierとして、ホスト認証を行わないように、
			// SocketFactoryとして、証明書の読み込みを行わない、という設定をシステムワイドに施す
			try {
				HostnameVerifier hv = new HostnameVerifier() {
					public boolean verify(String urlHostName,
							javax.net.ssl.SSLSession session) {
						return true;
					}
				};
				
				// Create the trust manager.
				javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
				class AllTrustManager implements
						javax.net.ssl.TrustManager,
						javax.net.ssl.X509TrustManager {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] certs,
							String authType)
							throws java.security.cert.CertificateException {
						return;
					}
					
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] certs,
							String authType)
							throws java.security.cert.CertificateException {
						return;
					}
				}
				javax.net.ssl.TrustManager tm = new AllTrustManager();
				trustAllCerts[0] = tm;
				// Create the SSL context
				javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
				// Create the session context
				javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
				// Initialize the contexts; the session context takes the
				// trust manager.
				sslsc.setSessionTimeout(0);
				sc.init(null, trustAllCerts, null);
				// Use the default socket factory to create the socket for
				// the secure
				// connection
				javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				// Set the default host name verifier to enable the connection.
				HttpsURLConnection.setDefaultHostnameVerifier(hv);
				m_log.info("EndpointUnit.static : setup hostverifier completed.");
			} catch (Exception e) {
				m_log.error("EndpointUnit.static : host authentication setup failed.", e);
			}
		}
	}

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
				formatted += "/HinemosWS/";
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
	
	public void changeEndpoint() {
		if (urlList.size() > 1) {
			try {
				PriorUrlListLock.writeLock();

				String lastPriorUrl = priorUrlList.remove(0);
				priorUrlList.add(lastPriorUrl);
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

	private void setBindingProvider(Object o, String url) {
		BindingProvider bp = (BindingProvider)o;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
		bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, userId);
		bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
		bp.getRequestContext().put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

		// Maximum time between establishing a connection and receiving data from the connection (ms)
 		int httpRequestTimeout = EndpointManager.getHttpRequestTimeout();
		bp.getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, httpRequestTimeout );
		if (m_log.isTraceEnabled()) {
			m_log.trace("ws timeout updated : requestTimeout = " + httpRequestTimeout + "");
		}

		((SOAPBinding)bp.getBinding()).setMTOMEnabled(true);
	}

	public class EndpointSetting<T> {
		public final EndpointKey<T> _key;

		public static final String _wsdlSuffix = "?wsdl";

		private EndpointSetting(Class<? extends Service> endpintServiceClass, Class<T> endpointClass, String urlPrefix) {
			_key = new EndpointKey<T>(endpintServiceClass, endpointClass, urlPrefix);
		}

		@SuppressWarnings("unchecked")
		public T getEndpoint() {
			try {
				T endpoint = null;

				if (! Service.class.isAssignableFrom(_key._endpointServiceClass)) {
					return null;
				}

				WebServiceClient wsc = _key._endpointServiceClass.getAnnotation(WebServiceClient.class);
				WebEndpoint we = null;
				for (Method method : _key._endpointServiceClass.getMethods()) {
					if ((we = method.getAnnotation(WebEndpoint.class)) != null) {
						break;
					}
				}
				
				if (we == null)
					throw new InternalError("we is null.");

				String urlStr = _key._urlPrefix + wsc.name() + _wsdlSuffix;

				if (endpointMap.containsKey(urlStr)) {
					endpoint = (T)endpointMap.get(urlStr);
				} else {
					Class<?>[] types = {URL.class};
					Constructor<? extends Service> constructor = _key._endpointServiceClass.getConstructor(types);
					Service service = constructor.newInstance(new URL(urlStr));
					endpoint = service.getPort(new QName(wsc.targetNamespace(), we.name()), _key._endpointClass);
					endpointMap.put(urlStr, endpoint);
				}
				setBindingProvider(endpoint, urlStr);

				return endpoint;
			} catch (InvocationTargetException e) {
				m_log.warn("failed creating endpoint. " + e.getMessage() + " (" + e.getClass().getName() + ")");
				throw new WebServiceException(e.getTargetException().getMessage(), e);
			} catch (Exception e) {
				m_log.warn("failed creating endpoint.", e);
				throw new WebServiceException(e.getMessage(), e);
			}
		}
	}

	private class EndpointKey<T> {
		public final Class<? extends Service> _endpointServiceClass;
		public final Class<T> _endpointClass;
		public final String _urlPrefix;

		private EndpointKey(Class<? extends Service> endpointServiceClass, Class<T> endpointClass, String urlPrefix) {
			this._endpointServiceClass = endpointServiceClass;
			this._endpointClass = endpointClass;
			this._urlPrefix = urlPrefix;
		};

		@Override
		public int hashCode() {
			int h = 1;
			h = h * 31 + (_endpointServiceClass == null ? 0 : _endpointServiceClass.hashCode());
			h = h * 31 + (_endpointClass == null ? 0 : _endpointClass.hashCode());
			h = h * 31 + (_urlPrefix == null ? 0 : _urlPrefix.hashCode());
			return h;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}

			if (obj instanceof EndpointKey<?>) {
				EndpointKey<?> cast = (EndpointKey<?>)obj;
				if (_endpointServiceClass != null && _endpointServiceClass.equals(cast._endpointServiceClass)) {
					if (_endpointClass != null && _endpointClass.equals(cast._endpointClass)) {
						if (_urlPrefix != null && _urlPrefix.equals(cast._urlPrefix)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return String.format("%s : _endpointServiceClass = %s, _endpointClass = %s, _urlStr = %s",
					EndpointKey.class.getSimpleName(), _endpointServiceClass.getSimpleName(), _endpointClass.getSimpleName(), _urlPrefix);
		}
	}

	/**
	 *  使用可能な順番でServiceに対応するEndpointSettingリストを返す
	 * @param <T>
	 * @return
	 */
	public <T> List<EndpointSetting<T>> getEndpoint(Class<? extends Service> endpointServiceClass, Class<T> endpointClass) {
		try {
			PriorUrlListLock.readLock();

			List<EndpointSetting<T>> list = new ArrayList<EndpointSetting<T>>(priorUrlList.size());
			for (String url : priorUrlList) {
				list.add(new EndpointSetting<T>(endpointServiceClass, endpointClass, url));
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

	public ManagerInfo connect() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidTimezone{
		m_log.debug("connect : " + urlList);

		// Check login result at first
		ManagerInfo managerInfo = AccessEndpointWrapper.getWrapperLoginCheck(managerName).checkLogin( this );
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
	 * 切断
	 */
	public void disconnect(){
		m_log.debug("disconnect : " + urlList);

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
}
