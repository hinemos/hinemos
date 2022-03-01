/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winservice.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.plugin.impl.ProxyManagerPlugin;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

import intel.management.wsman.ManagedInstance;
import intel.management.wsman.ManagedReference;
import intel.management.wsman.WsmanConnection;
import intel.management.wsman.WsmanException;
import intel.management.wsman.WsmanUtils;

/**
 * WinRMを使用したWindowsサービス監視実行クラス
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class RequestWinRM {

	private static Log m_log = LogFactory.getLog( RequestWinRM.class ) ;

	private static final String URI_WIN32_SERVICE = "http://schemas.microsoft.com/wbem/wsman/1/wmi/root/cimv2/Win32_Service";
	private static final String STATE_RUNNING = "Running";

	// Windowsサービス名
	private String m_serviceName;

	private String m_message;
	private String m_messageOrg;
	private long m_date;

	private WsmanConnection m_con;
	private String m_url;
	private String m_state;

	/**
	 * デフォルトコンストラクタ
	 * @param serviceName
	 */
	public RequestWinRM(String serviceName) {
		this.m_serviceName = serviceName;
	}

	/**
	 * WinRMへアクセスして指定のWindowsサービスの状態がRunningであることを確認する
	 * 
	 * @param ipAddress
	 * @param user
	 * @param userPassword
	 * @param port
	 * @param protocol
	 * @param timeout
	 * @param retries
	 * @return
	 */
	public boolean polling (
			String ipAddress,
			String user,
			String userPassword,
			int port,
			String protocol,
			int timeout,
			int retries
			) throws HinemosUnknown, WsmanException {
		m_log.debug("polling() " +
				"ipAddress = " + ipAddress +
				",user = " + user +
				",userPassword = " + userPassword +
				",port = " + port +
				",protocol = " + protocol +
				",timeout = " + timeout +
				",retries = " + retries);


		// XMLのTransformerFactoryの設定
		m_log.debug("polling() javax.xml.transform.TransformerFactory = " + System.getProperty("javax.xml.transform.TransformerFactory"));
		System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

		// URLの作成
		try{
			InetAddress address = InetAddress.getByName(ipAddress);
			if (address instanceof Inet6Address){
				m_url = protocol + "://[" + ipAddress + "]:" + port + "/wsman";
			}
			else{
				m_url = protocol + "://" + ipAddress + ":" + port + "/wsman";
			}
		} catch (UnknownHostException e) {
			m_log.info("polling() ipAddress is not valid : " + ipAddress
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new HinemosUnknown("ipAddress is not valid : " + ipAddress);
		}
		m_log.debug("polling() url = " + m_url);

		// コネクションと認証の設定
		m_con = WsmanConnection.createConnection(m_url);
		String authenticationScheme = HinemosPropertyCommon.monitor_winservice_authschema.getStringValue();
		if ("NTLM".equals(authenticationScheme)) {
			authenticationScheme = "ntlm";

			// プロキシ設定
			String proxyHost = ProxyManagerPlugin.getProxyHost();
			Integer proxyPort = ProxyManagerPlugin.getProxyPort();
			if (proxyHost != null && proxyPort != null) {
				m_con.setProxyHost(proxyHost);
				m_con.setProxyPort(proxyPort);
				String proxyUser = ProxyManagerPlugin.getProxyUser();
				String proxyPassword = ProxyManagerPlugin.getProxyPassword();
				if (proxyUser != null && proxyPassword != null) {
					m_con.setProxyUsername(proxyUser);
					m_con.setProxyPassword(proxyPassword);
				}
				List<String> proxyIgnoreHostList = ProxyManagerPlugin.getProxyIgnoreHostList();
				m_con.setProxyIgnoreHostList(proxyIgnoreHostList);
			}
		} else {
			authenticationScheme = "basic";
		}
		m_con.setAuthenticationScheme(authenticationScheme);
		m_con.setUsername(user);
		m_con.setUserpassword(userPassword);
		m_con.setTimeout(timeout);

		boolean sslTrustall = HinemosPropertyCommon.monitor_winservice_ssl_trustall.getBooleanValue();
		if(sslTrustall) {
			X509TrustManager tm = new X509TrustManager() {
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			
				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				}
			
				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				}
			};

			m_con.setTrustManager(tm);
			m_con.setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
		} else {
			// HTTP監視で使用しているライブラリ common-httpclient の HostnameVerifier を使用する
			m_con.setHostnameVerifier(HttpsSupport.getDefaultHostnameVerifier());
		}

		// URIの設定
		ManagedReference ref = m_con.newReference(URI_WIN32_SERVICE);
		ref.addSelector("Name", m_serviceName);

		// 判定
		int count = 0;
		WsmanException lastException = null;
		while (count < retries) {
			try{
				// 接続
				ManagedInstance inst = ref.get();
				if(m_log.isDebugEnabled()){
					m_log.debug(WsmanUtils.getXML(inst));
				}

				// 状態の取得
				Object stateObj = inst.getProperty("State");
				if(stateObj != null){
					m_state = stateObj.toString();
				}else{
					count++;
					continue;
				}

				// 状態の判定
				if(STATE_RUNNING.equalsIgnoreCase(m_state)){

					// [OK]
					m_message = m_serviceName + " Service is " + STATE_RUNNING;
					m_messageOrg = m_serviceName + " Service is " + STATE_RUNNING;
					m_date = HinemosTime.currentTimeMillis();

					break;
				}
				else{
					// [NG]
					m_message = m_serviceName + " Service is not " + STATE_RUNNING;
					m_messageOrg = m_serviceName + " Service is another state : " + m_state;
					m_date = HinemosTime.currentTimeMillis();

					return false;
				}

			} catch (WsmanException e) {
				m_log.debug("polling() url=" + m_url + ", count=" + count + " " + e.getMessage() + ", " + e.getReason());

				lastException = e; // 最後の例外を返却
				count++;
				continue;

			} finally {

				if(m_con != null){
					m_con = null;
				}
			}
		}

		// リトライ全部失敗の場合はNG
		if(count == retries){

			// 不明
			m_message = "WinRM Access Error . ";
			m_messageOrg = "WinRM Access Error . ";
			if(lastException != null){
				m_messageOrg = m_messageOrg + " : " + lastException.getMessage();
			}
			m_date = HinemosTime.currentTimeMillis();

			if(lastException != null){
				m_log.info("winservice url=" + m_url + ", message=" + lastException.getMessage() +
						", reason=" + lastException.getReason());
				if(lastException.getMessage() == null){
					throw new HinemosUnknown(MessageConstant.MESSAGE_WINSERVICE_NAME_NOT_EXIST_OR_NOT_REFERENCE_AUTHORITY_TO_WINRM.getMessage() + " : " + lastException.getReason());
				}else{
					if(lastException.getMessage().indexOf("HTTP response code: 401") != -1){
						throw new HinemosUnknown(MessageConstant.MESSAGE_FAIL_AT_WINRM_ID_OR_PASSWORD_OR_LOGINAUTH_ERR.getMessage());
					}
				}
				throw lastException;
			}
			else{
				// ここには到達しないはず。
				throw new HinemosUnknown("winservice unknown");
			}
		}

		// [OK]の場合のみ到達
		return true;
	}

	/**
	 * @return メッセージを戻します。
	 */
	public String getMessage() {
		m_log.debug("getMessage() message = " + m_message);
		return m_message;
	}

	/**
	 * 
	 * @return
	 */
	public String getMessageOrg() {
		m_log.debug("getMessageOrg() messageOrg = " + m_messageOrg);
		return m_messageOrg;
	}

	/**
	 * @return 取得日時を戻します。
	 */
	public long getDate() {
		m_log.debug("getDate() date = " + new Date(m_date));
		return m_date;
	}


	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			RequestWinRM winRM = new RequestWinRM("SNMP");
			winRM.polling(
					"172.26.98.119",
					"Administrator",
					"Hinemos24",
					5985,
					"http",
					3000,
					5);

			System.out.println("MSG = " + winRM.getMessage());
			System.out.println("MSG_ORG = " + winRM.getMessageOrg());

			System.out.println(System.getProperty("javax.xml.transform.TransformerFactory"));

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
