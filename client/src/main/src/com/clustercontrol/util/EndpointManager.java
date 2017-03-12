/**********************************************************************
 * Copyright (C) 2014 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 *
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package com.clustercontrol.util;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;

/**
 * EndpointManagerを管理するクラス
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class EndpointManager {

	// ログ
	private static Log m_log = LogFactory.getLog(EndpointManager.class);

	/** Managers 
	 * ログイン時の順番を保持するためにLinkedHashMapを利用する。
	 * */
	private Map<String, EndpointUnit> endpointManagerMap = new LinkedHashMap<>();

	/** Common settings */
	private int httpRequestTimeout = LoginManager.VALUE_HTTP_REQUEST_TIMEOUT;

	private Proxy proxy = Proxy.NO_PROXY;
	private PasswordAuthentication authenticator = null;
	
	/** Private constructor */
	private EndpointManager() {}

	/** Singleton */
	private static EndpointManager getInstance() {
		return SingletonUtil.getSessionInstance(EndpointManager.class);
	}

	public static int getHttpRequestTimeout() {
		return getInstance().httpRequestTimeout;
	}

	public static void setHttpRequestTimeout(int httpRequestTimeout) {
		getInstance().httpRequestTimeout = httpRequestTimeout;
	}
	
	public static void setProxy(String proxyHost, int proxyPort) {
		Proxy proxy = null;
		if (proxyHost == null || proxyHost.length() == 0) {
			proxy = Proxy.NO_PROXY;
		} else {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
		}
		getInstance().proxy = proxy;
	}
	
	public static Proxy getProxy() {
		return getInstance().proxy;
	}
	
	public static void setAuthenticator(String proxyUser, String proxyPass) {
		PasswordAuthentication authenticator = null;
		if (proxyUser != null && proxyUser.length() > 0) {
			authenticator = new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
		}
		getInstance().authenticator = authenticator;
	}
	
	public static PasswordAuthentication getAuthenticator() {
		return getInstance().authenticator;
	}

	public static void add(String user, String pass, String managerName, String managerAddressList ) {
		EndpointManager endpointManager = getInstance();

		EndpointUnit endpointUnit = endpointManager.endpointManagerMap.get(managerName);
		if (endpointUnit == null) {
			m_log.debug("new managerName=" + managerName);
			endpointUnit = new EndpointUnit();
			endpointManager.endpointManagerMap.put( managerName, endpointUnit );
		}
		
		endpointUnit.set(managerAddressList, user, pass, managerName);
		return;
	}

	private Map<String, EndpointUnit> getActiveManagerMap() {
		Map<String, EndpointUnit> activeManagerMap = new LinkedHashMap<>();
		for( Map.Entry<String, EndpointUnit> e : getInstance().endpointManagerMap.entrySet() ){
			if( e.getValue().isActive() ){
				activeManagerMap.put(e.getKey(), e.getValue());
			}
		}
		return activeManagerMap;
	}

	public static int sizeOfActive() {
		return getActiveManagerNameList().size();
	}

	public static int sizeOfAll() {
		return getInstance().endpointManagerMap.size();
	}

	public static EndpointUnit get( String managerName ){
		return getInstance().endpointManagerMap.get(managerName);
	}
	
	public static int getOrder(String managerName) {
		int n = 0;
		for (String key : getInstance().endpointManagerMap.keySet()) {
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

	public static List<EndpointUnit> getAllManagerList() {
		return ( 0 < sizeOfAll() ) ? new ArrayList<EndpointUnit>( getInstance().endpointManagerMap.values() ) : new ArrayList<EndpointUnit>();
	}

	public static List<EndpointUnit> getActiveManagerList() {
		Map<String, EndpointUnit> activeManagerMap = getInstance().getActiveManagerMap();
		return ( 0 < activeManagerMap.size() ) ? new ArrayList<EndpointUnit>( activeManagerMap.values() ) : new ArrayList<EndpointUnit>();
	}

	public static List<String> getActiveManagerNameList() {
		List<String> activeList = new ArrayList<>();
		for( Map.Entry<String, EndpointUnit> e : getInstance().endpointManagerMap.entrySet() ){
			if( e.getValue().isActive() ){
				activeList.add(e.getKey());
			}
		}
		return activeList;
	}

	/**
	* Check if user is login
	* @param uid
	* @return
	*/
	public static boolean hasLoginUser( String managerName, String userId ){
		return getInstance().getActiveManagerMap().get(managerName).getUserId().equals(userId);
	}

	/**
	* ログアウト
	*/
	public static void logout( String managerName ) {
		EndpointManager endpointMgr = getInstance();
		m_log.info( "Logout " + managerName );
		synchronized( endpointMgr.endpointManagerMap ){
			endpointMgr.endpointManagerMap.get(managerName).disconnect();
		}
	}

	/**
	 * 削除
	 */
	public static void delete( String managerName ) {
		EndpointManager endpointMgr = getInstance();
		m_log.debug( "Delete " + managerName );
		synchronized( endpointMgr.endpointManagerMap ){
			endpointMgr.endpointManagerMap.remove(managerName);
		}
		
		LoginManager.saveLoginState();
	}

}
