/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.agent;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.PasswordAuthentication;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.clustercontrol.agent.filecheck.FileCheckManager;
import com.clustercontrol.agent.job.RunHistoryUtil;
import com.clustercontrol.agent.log.LogfileMonitorManager;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.PropertiesFileUtil;
import com.clustercontrol.agent.winevent.WinEventMonitorManager;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.bean.AgentCommandConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.ws.agent.AgentInfo;
import com.clustercontrol.ws.agent.HinemosUnknown_Exception;
import com.clustercontrol.ws.agent.InvalidRole_Exception;
import com.clustercontrol.ws.agent.InvalidUserPass_Exception;
import com.clustercontrol.ws.agent.OutputBasicInfo;

/**
 * エージェントメインクラス<BR>
 * 
 * 管理対象ノードで起動する際に呼び出されるクラスです。
 */
public class Agent {

	//ロガー
	private static Log m_log = LogFactory.getLog(Agent.class);

	private static ReceiveTopic m_receiveTopic;

	private static SendQueue m_sendQueue;

	/** log4j設定 */
	public String m_log4jFileName = null;
	/** log4j設定ファイル再読み込み間隔 */
	private long m_reconfigLog4jInterval = 60000;

	/** AgentInfoの更新判定時間 [msec] */
	private static long m_agentInfoUpdateTime = 10000;
	/** AgentInfoの前回更新時間 */
	private static long lastAgentInfoUpdateTime = 0;
	
	/** シャットダウン待ち時間 [sec] */
	private static int m_shutdownWaitTime = 120;
	
	private static AgentInfo agentInfo = new AgentInfo();

	public static final Integer DEFAULT_CONNECT_TIMEOUT = 10000;
	public static final Integer DEFAULT_REQUEST_TIMEOUT = 60000;

	public static final String DEFAULT_PROXY_HOST = "";
	public static final int DEFAULT_PROXY_PORT = 8081;
	public static final String DEFAULT_PROXY_USER = "";
	public static final String DEFAULT_PROXY_PASSWORD = "";
	
	private static final String REPLACE_VALUE_MANAGER_IP = "${ManagerIP}";
	
	private static int awakePort = 24005;

	/*
	 * AgentHome
	 * /opt/hinemos_agentやC:\Program Files(x86)\Hinemos\Agent4.0.0
	 * など
	 */
	private static String agentHome = null;

	// Runtime.execのシリアル化実行用Lock Object(他にRuntime.exec用のLock Objectを設けず、必ずJVM内で共有すること)
	public static final Object runtimeExecLock = new Object();

	/**
	 * メイン処理
	 * 
	 * @param args プロパティファイル名
	 */
	public static void main(String[] args) throws Exception{

		// 引数チェック
		if(args.length != 1){
			System.out.println("Usage : java Agent [Agent.properties File Path]");
			System.exit(1);
		}

		try {
			// Systemプロパティ
			m_log.info("starting Hinemos Agent...");
			m_log.info("java.vm.version = "
					+ System.getProperty("java.vm.version"));
			m_log.info("java.vm.vendor = "
					+ System.getProperty("java.vm.vendor"));
			m_log.info("java.home = " + System.getProperty("java.home"));
			m_log.info("os.name = " + System.getProperty("os.name"));
			m_log.info("os.arch = " + System.getProperty("os.arch"));
			m_log.info("os.version = " + System.getProperty("os.version"));
			m_log.info("user.name = " + System.getProperty("user.name"));
			m_log.info("user.dir = " + System.getProperty("user.dir"));
			m_log.info("user.country = " + System.getProperty("user.country"));
			m_log.info("user.language = " + System.getProperty("user.language"));
			m_log.info("file.encoding = " + System.getProperty("file.encoding"));

			// Systemプロパティ(SET)
			String limitKey = "jdk.xml.entityExpansionLimit"; // TODO JREが変更された場合は、この変数が変更されていないかチェックすること。
			System.setProperty(limitKey, "0");
			m_log.info(limitKey + " = " + System.getProperty(limitKey));
			
			// TODO プロパティファイルのフォルダの親がagentHome。
			// あまり良くない実装なので、修正予定。
			File file = new File(args[0]);
			agentHome = file.getParentFile().getParent() + "/";
			m_log.info("agentHome=" + agentHome);

			// 起動時刻
			long startDate = HinemosTime.currentTimeMillis();
			m_log.info("start date = " + new Date(startDate) + "(" + startDate
					+ ")");
			agentInfo.setStartupTime(startDate);

			// Agent設定の初期化
			m_log.info("Agent.properties = " + args[0]);

			// スクリプトの削除
			File scriptDir = new File(agentHome + "script/");
			if(scriptDir.exists()) {
				File[] listFiles = scriptDir.listFiles();
				if (listFiles != null) {
					for(File f : listFiles) {
						boolean ret = f.delete();
						if (ret) {
							m_log.debug("delete script : " + f.getName());
						} else {
							m_log.warn("delete script error : " + f.getName());
						}
					}
				} else {
					m_log.warn("listFiles is null");
				}
			} else {
				//スクリプトフォルダが存在しない場合は作成する
				boolean ret = scriptDir.mkdir();
				if (!ret) {
					m_log.warn("mkdir error " + scriptDir.getPath());
				}
			}
			
			// queue生成
			m_sendQueue = new SendQueue();

			// Agentインスタンス作成
			Agent agent = new Agent(args[0]);

			//-----------------
			//-- トピック接続
			//-----------------
			m_log.debug("exec() : create topic ");

			m_receiveTopic = new ReceiveTopic(m_sendQueue);
			m_receiveTopic.setName("ReceiveTopicThread");
			m_log.info("receiveTopic start 1");
			m_receiveTopic.start();
			m_log.info("receiveTopic start 2");

			// エージェント処理開始
			agent.exec();

			m_log.info("Hinemos Agent started");

			// 終了待ち
			agent.waitAwakeAgent();
		} catch (Throwable e) {
			m_log.error("Agent.java: Runtime Exception Occurred. "
					+ e.getClass().getName() + ", " + e.getMessage(), e);
		}
	}

	public static void setAwakePort(int awakePort) {
		if (0 < awakePort && Agent.awakePort != awakePort) {
			m_log.info("awakePort=" + awakePort);
			Agent.awakePort = awakePort;
		}
	}
	
	/**
	 * マネージャのawakeAgentを待つメソッド。
	 * Agent.propertiesに指定されたUDPポート（デフォルト：24005）にパケットが送られてきたら、ラッチを開放する(releaseLatch)。
	 * ラッチが開放されたReceiveTopicは、マネージャからTopicを取りに行く。
	 */
	public void waitAwakeAgent () {
		final int BUFSIZE = 1;

		byte[] buf = new byte[BUFSIZE];
		InetAddress cAddr;		// マネージャのIPアドレス
		int cPort;				// マネージャのポート
		DatagramSocket sock = null;
		boolean flag = true;
		int port = 24005;

		int awakeDelay = 1000;

		try {
			String awakeDelayStr = AgentProperties.getProperty("awake.delay", Integer.toString(1000));
			awakeDelay = Integer.parseInt(awakeDelayStr);
			m_log.info("awake.delay = " + awakeDelay + " msec");
		} catch (NumberFormatException e) {
			m_log.error("awake.delay", e);
		}

		while (true) {
			/*
			 * UDPパケットを受信したらflagがtrueになる。
			 * その後に、flagがfalseになったら、getTopicを実行(releaseLatch)する。
			 * 
			 * UDPパケットを大量に受け取ってもgetTopicが大量発行されないように、
			 * このような実装とする。
			 */
			try {
				if (sock != null && port != awakePort) {
					sock.close();
					sock = null;
				}
				if (sock == null || !sock.isBound()) {
					port = awakePort;
					sock = new DatagramSocket(port);
					sock.setSoTimeout(awakeDelay);
				}
				DatagramPacket recvPacket = new DatagramPacket(buf, BUFSIZE);
				sock.receive(recvPacket);
				cAddr = recvPacket.getAddress();
				cPort = recvPacket.getPort();
				flag = true;
				m_log.info("waitAwakeAgent (" + cAddr.getHostAddress() +
						" onPort=" + cPort + ") buf.length=" + buf.length);
			} catch (SocketTimeoutException e) {
				if (flag) {
					m_log.info("waitAwakeAgent packet end");
					m_receiveTopic.releaseLatch();
					flag = false;
				}
			} catch (Exception e) {
				String msg = "waitAwakeAgent port=" + awakePort + ", " + e.getClass().getSimpleName() + ", " + e.getMessage();
				if (e instanceof BindException) {
					m_log.warn(msg);
				} else {
					m_log.warn(msg, e);
				}
				try {
					Thread.sleep(60*1000);
				} catch (InterruptedException e1) {
					m_log.warn(e1,e1);
				}
			}
		}
	}


	/**
	 * マネージャのsendManagerDiscoveryInfoを待つメソッド。
	 * TCP 24005にパケットが送られてきたら、パケット内のメッセージ（IPアドレス）を返す。
	 * 
	 * @throws Exception
	 */
	private String receiveManagerDiscoveryInfo() throws Exception {
		int default_port = 24005;
		String portStr = AgentProperties.getProperty("discovery.pingport", Integer.toString(default_port));
		int port = Integer.parseInt(portStr);
		if (port < 1 || port > 65535) {
			port = default_port;
		}
		ServerSocket servSock = null;
		Socket clntSock = null;

		final int BUFSIZE = 256;
		int tmpRecvMsgSize = 0;
		int recvMsgSize = 0;
		byte[] receiveBuf = new byte[BUFSIZE];
		String recvMsg = "";

		try {
			servSock = new ServerSocket(port);

			// マネージャからの接続を待つ
			clntSock = servSock.accept();
			m_log.info("connecting to： "
					+ clntSock.getRemoteSocketAddress().toString());

			InputStream in = clntSock.getInputStream();
			OutputStream out = clntSock.getOutputStream();

			while ((tmpRecvMsgSize = in.read(receiveBuf)) != -1) {
				out.write(receiveBuf, 0, tmpRecvMsgSize);
				recvMsgSize = tmpRecvMsgSize;
			}

			recvMsg = new String(receiveBuf, 0, recvMsgSize);
			m_log.info("receive message : " + recvMsg);
		} catch (Exception e) {
			m_log.warn("receiveManagerIp " + e.getClass().getSimpleName()
					+ ", " + e.getMessage());
			throw e;
		} finally {
			try {
				if (clntSock != null) {
					clntSock.close();
				}
			} catch (Exception e) {
				m_log.warn("receiveManagerIp: " + e);
			}
			try {
				if (servSock != null) {
					servSock.close();
				}
			} catch (Exception e) {
				m_log.warn("receiveManagerIp: " + e);
			}
		}
		return recvMsg;
	}

	/**
	 * コンストラクタ
	 */
	public Agent(String propFileName) throws Exception{

		//------------
		//-- 初期処理
		//------------

		//プロパティファイル読み込み初期化
		AgentProperties.init(propFileName);

		// エージェントのIPアドレス、ホスト名をログに出力。
		getAgentInfo();
		m_log.info(getAgentStr());

		// log4j設定ファイル再読み込み設定
		String log4jFileName = System.getProperty("hinemos.agent.conf.dir") + File.separator + "log4j.properties";
		m_log.info("log4j.properties = " + log4jFileName);
		m_log4jFileName = log4jFileName;

		int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		int requestTimeout = DEFAULT_REQUEST_TIMEOUT;

		// プロキシ設定
		String proxyHost = DEFAULT_PROXY_HOST;
		int proxyPort = DEFAULT_PROXY_PORT;
		String proxyUser = DEFAULT_PROXY_USER;
		String proxyPassword = DEFAULT_PROXY_PASSWORD;
		
		// ホスト認証回避のhostnameVerifierを登録する（HTTPS接続時以外は特に効果は無い）
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

		} catch (Throwable e) {
			m_log.warn("hostname verifier (all trust) disable : " + e.getMessage(), e);
		}

		try {
			String strConnect = AgentProperties.getProperty("connect.timeout");
			if (strConnect != null) {
				connectTimeout = Integer.parseInt(strConnect);
			}
			String strRequest = AgentProperties.getProperty("request.timeout");
			if (strRequest != null) {
				requestTimeout = Integer.parseInt(strRequest);
			}
			String strProxyHost = AgentProperties.getProperty("http.proxy.host");
			if (strProxyHost != null) {
				proxyHost = strProxyHost;
			}
			String strProxyPort = AgentProperties.getProperty("http.proxy.port");
			if (strProxyPort != null) {
				proxyPort = Integer.parseInt(strProxyPort);
			}
			String strProxyUser = AgentProperties.getProperty("http.proxy.user");
			if (strProxyUser != null) {
				proxyUser = strProxyUser;
			}
			String strProxyPassword = AgentProperties.getProperty("http.proxy.password");
			if (strProxyPassword != null) {
				proxyPassword = strProxyPassword;
			}
		} catch (Exception e) {
			m_log.warn(e.getMessage());
		}

		if(!"".equals(proxyHost)){
			System.setProperty("http.proxyHost", proxyHost);
			System.setProperty("http.proxyPort", Integer.toString(proxyPort));
			BasicAuth basicAuth = new BasicAuth(proxyUser, proxyPassword);
			Authenticator.setDefault(basicAuth);
			m_log.info("proxy.host=" + System.getProperty("http.proxyHost") + ", proxy.port=" + System.getProperty("http.proxyPort") + ", proxy.user=" + proxyUser);
		}


		// 設定ファイルの接続先アドレスに ${ManagerIP} の指定があった場合に限りマネージャからの接続を待ち、
		// マネージャのPINGを受け取って、接続先マネージャのIPアドレスの設定反映や自身のFacilityIDの書き換えを行う
		String managerAddress = AgentProperties.getProperty("managerAddress");
		URL url = new URL(managerAddress);
		boolean replacePropFileSuccess = true;
		String errMsg = "";
		if (REPLACE_VALUE_MANAGER_IP.equals((url.getHost()))) {
			try {
				
				// マネージャからのPINGを待ちうけ、正常なPINGが来るまで繰り返し待ち続ける
				Map<String, String> discoveryInfoMap = new HashMap<String, String>();
				while (true) {
					m_log.info("waiting for manager connection...");
					String recvMsg = receiveManagerDiscoveryInfo();
					
					// 受信したメッセージを、key=value,key=value の形式で解析し、Mapにする
					try {
						discoveryInfoMap.clear();
						String[] commaSplittedRecvMsgArray = recvMsg.split(",");
						for (String keyvalueset : commaSplittedRecvMsgArray) {
							String key = keyvalueset.split("=")[0];
							String value = keyvalueset.split("=")[1];
							discoveryInfoMap.put(key, value);
						}
					} catch (Exception e) {
						m_log.error("can't parse receive message : " + e.toString());
						continue;
					}
					if (discoveryInfoMap.containsKey("agentFacilityId") && discoveryInfoMap.containsKey("managerIp")) {
						break;
					} else {
						m_log.error("receive message is invalid");
					}
				}
				// Agent.propertiesファイルの接続先マネージャの書き換え＋動作中のプロパティの書き換え
				{
					String managerIp = discoveryInfoMap.get("managerIp");
					String key = "managerAddress";
					String value = url.getProtocol() + "://" + managerIp + ":"
							+ url.getPort() + "/HinemosWS/";
					m_log.info("Rewrite property. key : " + key + ", value : " + value);
					PropertiesFileUtil.replacePropertyFile(propFileName, key, managerAddress, value);
					AgentProperties.setProperty(key, value);
				}
				
				// Agent.propertiesファイルのエージェントファシリティID書き換え＋動作中のプロパティの書き換え
				{
					String key = "facilityId";
					String value = discoveryInfoMap.get("agentFacilityId");
					m_log.info("Rewrite property. key : " + key + ", value : " + value);
					PropertiesFileUtil.replacePropertyFile(propFileName, key, "", value);
					AgentProperties.setProperty(key, value);
				}
				
				// log4j.propertiesファイルの書き換え（Windows版エージェントのみ）
				{
					String managerIp = discoveryInfoMap.get("managerIp");
					String key = "log4j.appender.syslog.SyslogHost";
					PropertiesFileUtil.replacePropertyFile(log4jFileName,
							"log4j.appender.syslog.SyslogHost",
							REPLACE_VALUE_MANAGER_IP, managerIp);
					if (REPLACE_VALUE_MANAGER_IP.equals(AgentProperties.getProperty(key))) {
						m_log.info("Rewrite property. key : " + key + ", value : " + managerIp);
						PropertiesFileUtil.replacePropertyFile(log4jFileName, key, REPLACE_VALUE_MANAGER_IP, managerIp);
					}
				}
			} catch (HinemosUnknown e) {
				// プロパティファイル書き換えに失敗した場合、マネージャに通知する
				errMsg = e.getMessage();
				m_log.warn(errMsg, e);
				replacePropFileSuccess = false;
			} catch (Exception e) {
				m_log.warn(e.getMessage(), e);
				throw e;
			}
		}
		
		try {
			EndpointManager.init(AgentProperties.getProperty("user"),
					AgentProperties.getProperty("password"),
					AgentProperties.getProperty("managerAddress"),
					connectTimeout, requestTimeout);
		} catch (Exception e) {
			m_log.error("EndpointManager.init error : " + e.getMessage(), e);
			m_log.error("current-dir=" + (new File(".")).getAbsoluteFile().getParent());
			throw e;
		}

		if (!replacePropFileSuccess) {
			OutputBasicInfo output = new OutputBasicInfo();
			output.setPluginId("AGT_UPDATE_CONFFILE");
			output.setPriority(PriorityConstant.TYPE_WARNING);
			output.setApplication(MessageConstant.AGENT.getMessage());
			String[] args = { errMsg };
			output.setMessage(MessageConstant.MESSAGE_AGENT_REPLACE_FILE_FAULURE_NOTIFY_MSG.getMessage());
			output.setMessageOrg(MessageConstant.MESSAGE_AGENT_REPLACE_FILE_FAULURE_NOTIFY_ORIGMSG.getMessage(args));
			output.setGenerationDate(HinemosTime.getDateInstance().getTime());
			output.setMonitorId("SYS");
			output.setFacilityId(""); // マネージャがセットする。
			output.setScopeText(""); // マネージャがセットする。
			m_sendQueue.put(output);
		}

		// AgentInfoの更新判定時間の更新
		String time = AgentProperties.getProperty("agent.info.update.time");
		if (time != null) {
			try {
				m_agentInfoUpdateTime = Long.parseLong(time);
				m_log.info("agent.info.update.time = " + m_agentInfoUpdateTime + " ms");
			} catch (NumberFormatException e) {
				m_log.error("agent.info.update.time",e);
			}
		}
		
		// シャットダウン待ち時間の更新
		String shutdownWaitTime = AgentProperties.getProperty("agent.shutdown.wait.time");
		if (shutdownWaitTime != null) {
			try {
				m_shutdownWaitTime = Integer.parseInt(shutdownWaitTime);
				m_log.info("agent.shutdown.wait.time = " + m_shutdownWaitTime + " sec");
			} catch (NumberFormatException e) {
				m_log.error("agent.shutdown.wait.time", e);
			}
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// ログファイル監視の実行中は停止を待つ
				LogfileMonitorManager.terminate();
				for(int i = 0 ; i < m_shutdownWaitTime / 2; i++) {
					try {
						if(LogfileMonitorManager.isRunning()) {
							m_log.info("Logfile monitor is running.");
							Thread.sleep(2 * 1000);
						} else {
							m_log.info("Logfile monitor is stopping.");
							break;
						}
					} catch (InterruptedException e) {
						m_log.warn(e.getMessage(), e);
					}
				}
				
				terminate();
				m_log.info("Hinemos agent stopped");
			}
		});
	}

	/**
	 * エージェント処理実行実行します。<BR>
	 * 
	 * メインメソッドから呼び出されるメソッドで
	 * トピックへの接続を行います。
	 */
	public void exec() {

		// 定期的にリロードする処理を開始する
		m_log.info("log4j.properties=" + m_log4jFileName);
		PropertyConfigurator.configureAndWatch(m_log4jFileName, m_reconfigLog4jInterval);

		// ログファイル読み込みスレッド開始
		LogfileMonitorManager.start();

		// ファイルチェック(ジョブ)読み込みスレッド開始
		FileCheckManager fileCheckManager = new FileCheckManager();
		fileCheckManager.start();
		
		// Windowsイベント監視スレッド開始
		WinEventMonitorManager.start();

	}

	/**
	 * 終了処理を行います。<BR>
	 */
	public void terminate() {
		m_log.info("terminate() start");
		RunHistoryUtil.logHistory();

		try {
			// deleteAgentを行った直後に別スレッドのgetTopicと競合するとエージェントが重複されたと認識されるため、
			// deleteAgentとgetTopicを排他する
			synchronized(ReceiveTopic.lockTopicReceiveTiming) {
				AgentEndPointWrapper.deleteAgent();
				ReceiveTopic.terminate();
			}
		} catch (InvalidRole_Exception e) {
			m_log.info("InvalidRoleException " + e.getMessage());
		} catch (InvalidUserPass_Exception e) {
			m_log.info("InvalidUserPassException " + e.getMessage());
		} catch (HinemosUnknown_Exception e) {
			m_log.info("HinemosUnknown " + e.getMessage());
		}

		m_log.info("terminate() end");
	}

	/**
	 * エージェントを再起動します。
	 */
	public static void restart(int agentCommand) {
		String osName = System.getProperty("os.name");
		m_log.info("Hinemos agent restart : os=" + osName);

		/** OSがWindowsか否(UNIX)かを自動判別する */
		if(osName != null && osName.startsWith("Windows")){
			restartWin(agentCommand);
		} else {
			restartUnix(agentCommand);
		}
	}

	private static void restartWin(int agentCommand) {
		String[] command = null;

		if (agentCommand == AgentCommandConstant.UPDATE) {
			command = new String[5];
			command[0] = "CMD";
			command[1] = "/C";
			command[2] = "cscript";
			command[3] = getAgentHome() + "/bin/RestartAgent.vbs";
			command[4] = "copy";
			m_log.info("restartWin.command for update = " + command[0] + " " + command[1] + " " + command[2] +
					" " + command[3] + " " + command[4]);
		} else {
			command = new String[4];
			command[0] = "CMD";
			command[1] = "/C";
			command[2] = "cscript";
			command[3] = getAgentHome() + "/bin/RestartAgent.vbs";
			m_log.info("restartWin.command for create = " + command[0] + " " + command[1] + " " + command[2] +
			" " + command[3]);
		}
		try {
			Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			m_log.error("restart " + e.getMessage(), e);
		}
	}

	private static void restartUnix(int agentCommand) {
		String[] command = null;
		String script = "agent_restart.sh";
		command = new String[3];
		command[0] = "sh";
		command[1] = "-c";
		command[2] = getAgentHome() + "bin/" + script;
		command[2] += " -q";
		if (agentCommand == AgentCommandConstant.UPDATE) {
			command[2] += " copy";
		}
		m_log.info("command = " + command[0] + " " + command[1] + " " + command[2]);
		try {
			Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			m_log.error("restart " + e.getMessage(), e);
		}
	}

	public static AgentInfo getAgentInfo() {
		// 更新判定時間以内の場合agentInfoを書き換えない
		long diffTime = HinemosTime.currentTimeMillis() - lastAgentInfoUpdateTime;
		m_log.debug("diffTime : " + diffTime);
		if(m_agentInfoUpdateTime >= Math.abs(diffTime)) {
			m_log.debug("m_agentInfoUpdateTime >= diffTime(AgentInfo no update.)");
			return agentInfo;
		}
		
		// IPアドレスが変更されて設定の再取得中はagentInfoを書き換えない
		if (ReceiveTopic.isReloadFlg()) {
			return agentInfo;
		}
		
		agentInfo.setFacilityId(AgentProperties.getProperty("facilityId"));
		String instanceId = AgentProperties.getProperty("instanceId");
		if (instanceId == null) {
			instanceId = "";
		}
		agentInfo.setInstanceId(instanceId);

		// OS情報(識別したホスト名、IPアドレス)
		try {
			// ホスト名取得
			String hostname = System.getProperty("hostname");
			m_log.debug("hostname=[" + hostname + "]");
			agentInfo.setHostname(hostname);

			// IPアドレス取得
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			ArrayList<String> newIpAddressList = new ArrayList<String>();
			if (null != networkInterfaces) {
				while (networkInterfaces.hasMoreElements()) {
					NetworkInterface ni = networkInterfaces.nextElement();
					Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
					while (inetAddresses.hasMoreElements()) {
						InetAddress in = inetAddresses.nextElement();
						String hostAddress = in.getHostAddress();
						if (hostAddress != null && !hostAddress.equals("127.0.0.1") &&
								!hostAddress.startsWith("0:0:0:0:0:0:0:1") &&
								!hostAddress.equals("::1")) {
							newIpAddressList.add(hostAddress);
						}
					}
				}
			}
			if (agentInfo.getIpAddress().size() != newIpAddressList.size()) {
				m_log.info("ipAddress change : " + agentInfo.getIpAddress().size() +
						"," + newIpAddressList.size());
				agentInfo.getIpAddress().clear();
				agentInfo.getIpAddress().addAll(newIpAddressList);
				ReceiveTopic.setReloadFlg(true);
			} else {
				if (!agentInfo.getIpAddress().containsAll(newIpAddressList)) {
					m_log.info("ipAddress change");
					agentInfo.getIpAddress().clear();
					agentInfo.getIpAddress().addAll(newIpAddressList);
					ReceiveTopic.setReloadFlg(true);
				}
			}
		} catch (SocketException e) {
			m_log.error(e,e);
		}
		agentInfo.setInterval(ReceiveTopic.getTopicInterval());

		m_log.debug(getAgentStr());

		lastAgentInfoUpdateTime = HinemosTime.currentTimeMillis();
		return agentInfo;
	}

	public static String getAgentStr() {
		StringBuffer str = new StringBuffer();
		str.append("agentInfo=");
		if (agentInfo.getFacilityId() != null) {
			str.append("[facilityID=" + agentInfo.getFacilityId() + "]");
		}
		if (agentInfo.getInstanceId() != null && agentInfo.getInstanceId().length() != 0) {
			str.append("[instanceID=" + agentInfo.getInstanceId() + "]");
		}
		str.append("[hostname="+ agentInfo.getHostname() + "]");
		for (String ipAddress : agentInfo.getIpAddress()) {
			str.append(", " +ipAddress);
		}
		return str.toString();
	}

	public static String getAgentHome() {
		return agentHome;
	}
}

class BasicAuth extends Authenticator{
	private String username;
	private String password;
	public BasicAuth(String username, String password){
		this.username = username;
		this.password = password;
	}
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(username,password.toCharArray());
	}
}