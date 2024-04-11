/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.openapitools.client.model.AgentInfoRequest;
import org.openapitools.client.model.AgentInfoRequest.DhcpUpdateModeEnum;
import org.openapitools.client.model.AgentJavaInfoRequest;
import org.openapitools.client.model.AgtOutputBasicInfoRequest;

import com.clustercontrol.agent.SendQueue.MessageSendableObject;
import com.clustercontrol.agent.bean.DhcpUpdateMode;
import com.clustercontrol.agent.binary.BinaryMonitorManager;
import com.clustercontrol.agent.cloud.log.CloudLogMonitorManager;
import com.clustercontrol.agent.filecheck.FileCheckManager;
import com.clustercontrol.agent.job.RunHistoryUtil;
import com.clustercontrol.agent.log.LogfileMonitorManager;
import com.clustercontrol.agent.rpa.RpaLogfileMonitorManager;
import com.clustercontrol.agent.rpa.scenariojob.ScreenshotThread;
import com.clustercontrol.agent.sdml.SdmlFileMonitorManager;
import com.clustercontrol.agent.selfcheck.SelfCheckManager;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.PropertiesFileUtil;
import com.clustercontrol.agent.winevent.WinEventMonitorManager;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.repository.bean.AgentCommandConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
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

	private static final Object shutdownLock = new Object();
	private static boolean shutdown = false;

	/** log4j設定 */
	public String m_log4jFileName = null;

	/** AgentInfoの更新判定時間 [msec] */
	private static long m_agentInfoUpdateTime = 10000;
	private static Object m_agentInfoUpdateTimeLock = new Object();

	/** AgentInfoの前回更新時間 */
	private static long lastAgentInfoUpdateTime = 0;
	
	/** シャットダウン待ち時間 [sec] */
	private static int m_shutdownWaitTime = 120;
	private static Object m_shutdownWaitTimeLock = new Object();
	
	private static AgentInfoRequest agentInfo = new AgentInfoRequest();

	private static AgentJavaInfoRequest javaInfo = null;

	public static final Integer DEFAULT_CONNECT_TIMEOUT = 10000;
	public static final Integer DEFAULT_REQUEST_TIMEOUT = 60000;

	public static final String DEFAULT_PROXY_HOST = "";
	public static final int DEFAULT_PROXY_PORT = 8081;
	public static final String DEFAULT_PROXY_USER = "";
	public static final String DEFAULT_PROXY_PASSWORD = "";
	
	private static final String REPLACE_VALUE_MANAGER_IP = "${ManagerIP}";
	
	private static int awakePort = 24005;
	
	private static Agent agent;
	
	private static boolean terminated = false;
	
	private static String rest_agent_id = null;
	
	/** 自身がマネージャに認識されているか */
	private static AtomicBoolean registered = new AtomicBoolean(false);
	
	/*
	 * AgentHome
	 * /opt/hinemos_agentやC:\Program Files(x86)\Hinemos\Agent4.0.0
	 * など
	 */
	private static String agentHome = null;

	// Runtime.execのシリアル化実行用Lock Object(他にRuntime.exec用のLock Objectを設けず、必ずJVM内で共有すること)
	public static final Object runtimeExecLock = new Object();

	/** マネージャとの接続チェック用のフラグ */
	private static boolean awakePortFlg = false;
	
	/**
	 * メイン処理
	 * 
	 * @param args プロパティファイル名
	 */
	public static void main(String[] args) {

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
			agentHome = file.getParentFile().getParent() + File.separator;
			m_log.info("agentHome=" + agentHome);
			
			// AgentInfo の初期化
			agentInfo.setFacilityId("");
			agentInfo.setHostname("");
			agentInfo.setIpAddressList(new ArrayList<String>());
			agentInfo.setInterval(0);
			agentInfo.setInstanceId("");
			agentInfo.setVersion(AgentVersion.VERSION);

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
			
			// GCログの削除
			String gclogFileName = System.getProperty("jvm.gclog.filename");
			int period = 31;
			try {
				period = Integer.parseInt(System.getProperty("jvm.gclog.date.rentention.period"));
			} catch (NumberFormatException e) {
				m_log.warn("System environment value \"jvm.gclog.date.rentention.period\" is not correct.");
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(startDate);
			calendar.add(Calendar.DAY_OF_MONTH, -period);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date deleteTargetDate = new Date(calendar.getTime().getTime());
			
			if(gclogFileName != null){
				File gclogTempFile = new File(gclogFileName);
				String logFileName = gclogTempFile.getName();
				String logDirName = gclogTempFile.getParent();
				if(logDirName != null){
					File[] list = new File(logDirName).listFiles();
					if(list != null && list.length > 0){
						for(File targetFile : list){
							String targetFileName = targetFile.getName();
							int index = targetFileName.indexOf(logFileName + ".");
							if(index != -1){
								Date targetLogDate = new Date(targetFile.lastModified());
								if(targetLogDate.before(deleteTargetDate)){
									//削除実行
									boolean ret = targetFile.delete();
									if(!ret){
										m_log.warn("Delete File Failure : " + targetFile);
									}
								}
							}
						}
					}
				}
			}
			
			// Agentインスタンス作成
			agent = new Agent(args[0]);
			
			// RPAシナリオジョブのスクリーンショットの削除
			ScreenshotThread.deleteScreenshotFiles();
			
			// queue生成
			m_sendQueue = new SendQueue();

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

	/**
	 * リポジトリ[ノード]で設定した即時反映ポートの設定をエージェント側に反映する。
	 * 
	 * @param awakePort
	 */
	public static void setAwakePort(int awakePort) {
		if (0 < awakePort && Agent.awakePort != awakePort) {
			m_log.info("awakePort=" + awakePort);
			Agent.awakePort = awakePort;
		}
		// マネージャとの接続に成功し、即時反映ポートの反映処理が行われたのでフラグをたたせる。
		awakePortFlg = true;
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
		
		boolean awakeListenCheck = Boolean.valueOf(AgentProperties.getProperty("awake.listen.check", "true"));
		boolean isBindFail = false;

		try {
			String awakeDelayStr = AgentProperties.getProperty("awake.delay", Integer.toString(1000));
			awakeDelay = Integer.parseInt(awakeDelayStr);
			m_log.info("awake.delay = " + awakeDelay + " msec");
		} catch (NumberFormatException e) {
			m_log.error("awake.delay", e);
		}
		
		String awakeListenStr = AgentProperties.getProperty("awake.listen", "true");
		boolean awakeListen = Boolean.parseBoolean(awakeListenStr);
		if (awakeListen) {
			// エージェント起動時はスレッド待機しないようにラッチを解放する
			m_receiveTopic.releaseLatch();
			// マネージャと接続しない限り待ち続けるループ
			while (!awakePortFlg) {
				try {
					Thread.sleep(awakeDelay);
					if (m_log.isDebugEnabled()) {
						m_log.debug("manager connect waiting...");
					}

				} catch (InterruptedException e1) {
					m_log.warn(e1, e1);
				}
			}
			m_log.info("waitAwakeAgent port Listen Start");

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
						// 即時反映ポートのリッスン失敗から回復した際に通知する
						if(awakeListenCheck && isBindFail) {
							String msg = MessageConstant.MESSAGE_AGENT_AWAKE_LISTEN_CHECK_SUCCESS.getMessage(Integer.toString(awakePort));
							sendMessageCheckAwakePort(PriorityConstant.TYPE_INFO, msg, msg);
							isBindFail = false;
						}
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
					String msgOrg = "waitAwakeAgent port=" + awakePort + ", " + e.getClass().getSimpleName() + ", " + e.getMessage();
					if (e instanceof BindException) {
						m_log.warn(msgOrg);
						// 即時反映ポートのリッスンに失敗した場合は通知する
						if(awakeListenCheck && !isBindFail) {
							String msg = MessageConstant.MESSAGE_AGENT_AWAKE_LISTEN_CHECK_FAIL.getMessage(Integer.toString(awakePort));
							sendMessageCheckAwakePort(PriorityConstant.TYPE_WARNING, msg, msgOrg);
							isBindFail = true;
						}
					} else {
						m_log.warn(msgOrg, e);
					}
					try {
						Thread.sleep(60*1000);
					} catch (InterruptedException e1) {
						m_log.warn(e1,e1);
					}
				}
			}
		} else {
			m_log.info("awake.listen = " + awakeListen);
			
			synchronized (shutdownLock) {
				while (!shutdown) {
					try {
						shutdownLock.wait();
					} catch (InterruptedException e) {
						m_log.warn("shutdown lock interrupted.", e);
						try {
							Thread.sleep(10*60*1000);
						} catch (InterruptedException sleepE) { };
					}
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

	private void sendMessageCheckAwakePort(int priority, String msg, String msgOrg) {
		MessageSendableObject sendme = new MessageSendableObject();
		sendme.body = new AgtOutputBasicInfoRequest();
		sendme.body.setPluginId(HinemosModuleConstant.SYSYTEM_SELFCHECK);
		sendme.body.setPriority(priority);
		sendme.body.setApplication(MessageConstant.AGENT.getMessage());
		sendme.body.setMessage(msg);
		sendme.body.setMessageOrg(msgOrg);
		sendme.body.setGenerationDate(HinemosTime.getDateInstance().getTime());
		sendme.body.setMonitorId(HinemosModuleConstant.SYSYTEM);
		sendme.body.setFacilityId(""); // マネージャがセットする。
		sendme.body.setScopeText(""); // マネージャがセットする。
		
		m_sendQueue.put(sendme);
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
		getAgentInfoRequest();
		m_log.info(getAgentStr());

		// log4j設定ファイル再読み込み設定
		String log4jFileName = System.getProperty("hinemos.agent.conf.dir") + File.separator + "log4j2.properties";
		m_log.info("log4j2.properties = " + log4jFileName);
		m_log4jFileName = log4jFileName;

		int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		int requestTimeout = DEFAULT_REQUEST_TIMEOUT;

		// プロキシ設定
		String proxyHost = DEFAULT_PROXY_HOST;
		int proxyPort = DEFAULT_PROXY_PORT;
		String proxyUser = DEFAULT_PROXY_USER;
		String proxyPassword = DEFAULT_PROXY_PASSWORD;
		
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
			AgentRestConnectManager.setProxy(proxyHost,proxyPort);
			AgentRestConnectManager.setProxyAuchenticator(proxyUser, proxyPassword);
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
					String[] managerIps = discoveryInfoMap.get("managerIp").split("\\|");
					String key = "managerAddress";
					String value = url.getProtocol() + "://" + managerIps[0] + ":"
							+ url.getPort() + "/HinemosWeb/";
					// HAオプションでFIPを利用できない場合の対応
					// managerIpが「|」区切りで2つ以上連携された場合は接続先マネージャを2件設定
					// 3つ目以降は切り捨てる
					if (managerIps.length >= 2) {
						if (managerIps.length >= 3) {
							m_log.info("More than two managerIP is specified. Set the first and second value : " + managerIps[0]
									+ "," + managerIps[1] + " to '" + key + "'.");
						}
						value = value + "," + url.getProtocol() + "://" + managerIps[1] + ":"
								+ url.getPort() + "/HinemosWeb/";
					}
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
				
				// log4j2.propertiesファイルの書き換え（Windows版エージェントのみ）
				{
					String[] managerIps = discoveryInfoMap.get("managerIp").split("\\|");
					String key = "log4j.appender.syslog.SyslogHost";
					// HAオプションでFIPを利用できない場合の対応
					// managerIpが「|」区切りで2つ以上連携された場合は1つ目の値を設定
					// 2つ目以降は切り捨てる
					if (managerIps.length >= 2) {
						m_log.info("More than one managerIP is specified. Set the first value : " + managerIps[0] + " to 'log4j.appender.syslog.SyslogHost'.");
					}
					m_log.info("Rewrite property. key : " + key + ", value : " + managerIps[0]);
					PropertiesFileUtil.replacePropertyFile(log4jFileName,
							"log4j.appender.syslog.SyslogHost",
							REPLACE_VALUE_MANAGER_IP, managerIps[0]);
					if (REPLACE_VALUE_MANAGER_IP.equals(AgentProperties.getProperty(key))) {
						m_log.info("Rewrite property. key : " + key + ", value : " + managerIps[0]);
						PropertiesFileUtil.replacePropertyFile(log4jFileName, key, REPLACE_VALUE_MANAGER_IP, managerIps[0]);
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
			AgentRestConnectManager.init(AgentProperties.getProperty("user"),
					AgentProperties.getProperty("password"),
					AgentProperties.getProperty("managerAddress"),
					connectTimeout, requestTimeout);
		} catch (Exception e) {
			m_log.error("AgentRestConnectManager.init error : " + e.getMessage(), e);
			m_log.error("current-dir=" + (new File(".")).getAbsoluteFile().getParent());
			throw e;
		}
		

		if (!replacePropFileSuccess) {
			MessageSendableObject sendme = new MessageSendableObject();
			sendme.body = new AgtOutputBasicInfoRequest();
			sendme.body.setPluginId("AGT_UPDATE_CONFFILE");
			sendme.body.setPriority(PriorityConstant.TYPE_WARNING);
			sendme.body.setApplication(MessageConstant.AGENT.getMessage());
			sendme.body.setMessage(MessageConstant.MESSAGE_AGENT_REPLACE_FILE_FAULURE_NOTIFY_MSG.getMessage());
			String[] args = { errMsg };
			sendme.body.setMessageOrg(MessageConstant.MESSAGE_AGENT_REPLACE_FILE_FAULURE_NOTIFY_ORIGMSG.getMessage(args));
			sendme.body.setGenerationDate(HinemosTime.getDateInstance().getTime());
			sendme.body.setMonitorId("SYS");
			sendme.body.setFacilityId(""); // マネージャがセットする。
			sendme.body.setScopeText(""); // マネージャがセットする。
			m_sendQueue.put(sendme);
		}

		// AgentInfoの更新判定時間の更新
		synchronized (m_agentInfoUpdateTimeLock) {
			String time = AgentProperties.getProperty("agent.info.update.time");
			if (time != null) {
				try {
					m_agentInfoUpdateTime = Long.parseLong(time);
					m_log.info("agent.info.update.time = " + m_agentInfoUpdateTime + " ms");
				} catch (NumberFormatException e) {
					m_log.error("agent.info.update.time",e);
				}
			}
		}

		// シャットダウン待ち時間の更新
		synchronized (m_shutdownWaitTimeLock) {
			String shutdownWaitTime = AgentProperties.getProperty("agent.shutdown.wait.time");
			if (shutdownWaitTime != null) {
				try {
					m_shutdownWaitTime = Integer.parseInt(shutdownWaitTime);
					m_log.info("agent.shutdown.wait.time = " + m_shutdownWaitTime + " sec");
				} catch (NumberFormatException e) {
					m_log.error("agent.shutdown.wait.time", e);
				}
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				terminate();
				// ログ出力を停止
				LogManager.shutdown();
			}
		});
	}

	/**
	 * エージェントを終了します。
	 * 自動テストからのエージェント停止に使用
	 */
	public static void teminateAgent() {
		agent.terminate();
	}
	
	/**
	 * エージェント処理実行実行します。<BR>
	 * 
	 * メインメソッドから呼び出されるメソッドで
	 * トピックへの接続を行います。
	 */
	public void exec() {
		// 定期的にリロードする処理を開始する
		m_log.info("log4j2.properties=" + m_log4jFileName);
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		context.setConfigLocation(Paths.get(m_log4jFileName).toUri());

		// ログファイル読み込みスレッド開始
		LogfileMonitorManager.getInstance().start();

		// バイナリ監視スレッド開始
		BinaryMonitorManager.start();

		// ファイルチェック(ジョブ)読み込みスレッド開始
		FileCheckManager fileCheckManager = new FileCheckManager();
		fileCheckManager.start();
		
		// Windowsイベント監視スレッド開始
		WinEventMonitorManager.start();
		
		// RPAログファイル監視スレッド開始
		RpaLogfileMonitorManager.getInstance().start();

		// SDML制御ログ読み込みスレッド開始
		SdmlFileMonitorManager.getInstance().start();

		// セルフチェックの開始
		SelfCheckManager.start();
	}

	/**
	 * 終了処理を行います。<BR>
	 */
	public void terminate() {
		synchronized(Agent.class) {
			if (terminated) {
				return;
			}
			terminated = true;
		}
		m_log.info("terminate() start");
		
		// ログファイル監視の実行中は停止を待つ
		// SDML制御ログ監視の実行中は停止を待つ
		// RPAログファイル監視の実行中は停止を待つ
		LogfileMonitorManager.getInstance().terminate();
		SdmlFileMonitorManager.getInstance().terminate();
		RpaLogfileMonitorManager.getInstance().terminate();
		for(int i = 0 ; i < m_shutdownWaitTime / 2; i++) {
			try {
				if(LogfileMonitorManager.getInstance().isRunning()) {
					m_log.info("Logfile monitor is running.");
					Thread.sleep(2 * 1000);
				} else if(SdmlFileMonitorManager.getInstance().isRunning()) {
					m_log.info("Sdml controllog monitor is running.");
					Thread.sleep(2 * 1000);
				} else if (RpaLogfileMonitorManager.getInstance().isRunning()) {
					m_log.info("RpaLogfile monitor is running.");
					Thread.sleep(2 * 1000);
				} else {
					m_log.info("Logfile monitor and Sdml controllog monitor and RPA logfile monitor is stopping.");
					break;
				}
			} catch (InterruptedException e) {
				m_log.warn(e.getMessage(), e);
			}
		}
		
		// クラウドログ監視の停止を待つ
		CloudLogMonitorManager.shutdownAllCloudLogTask();

		// エージェント内部メッセージの送信を停止
		SendQueue.termMessageSendExecuter(m_shutdownWaitTime);
		
		RunHistoryUtil.logHistory();

		SelfCheckManager.shutdown();
		
		try {
			// deleteAgentを行った直後に別スレッドのgetTopicと競合するとエージェントが重複されたと認識されるため、
			// deleteAgentとgetTopicを排他する
			synchronized(ReceiveTopic.lockTopicReceiveTiming) {
				AgentRestClientWrapper.deleteAgent(Agent.getAgentInfoRequest());
				ReceiveTopic.terminate();
			}
		} catch (InvalidSetting | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
			m_log.info("Failed to delete agent: " + e.getClass().getSimpleName() + " " + e.getMessage());
		}
		
		m_log.info("terminate() end");
		m_log.info("Hinemos agent stopped");
		
		synchronized (shutdownLock) {
			shutdown = true;
			shutdownLock.notify();
		}
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

	public static AgentInfoRequest getAgentInfoRequest() {
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
			if (agentInfo.getIpAddressList().size() != newIpAddressList.size()) {
				m_log.info("ipAddress change : " + agentInfo.getIpAddressList().size() +
						"," + newIpAddressList.size());
				agentInfo.getIpAddressList().clear();
				agentInfo.getIpAddressList().addAll(newIpAddressList);
				ReceiveTopic.setReloadFlg(true);
			} else {
				if (!agentInfo.getIpAddressList().containsAll(newIpAddressList)) {
					m_log.info("ipAddress change");
					agentInfo.getIpAddressList().clear();
					agentInfo.getIpAddressList().addAll(newIpAddressList);
					ReceiveTopic.setReloadFlg(true);
				}
			}
		} catch (SocketException e) {
			m_log.error(e,e);
		}
		agentInfo.setInterval(ReceiveTopic.getTopicInterval());

		m_log.debug(getAgentStr());

		lastAgentInfoUpdateTime = HinemosTime.currentTimeMillis();

		if(agentInfo.getFacilityId() == null || agentInfo.getFacilityId().equals("")){
			rest_agent_id = agentInfo.getHostname() + "." + agentInfo.getIpAddressList().toString();
		}else{
			rest_agent_id =agentInfo.getFacilityId()+"."+agentInfo.getInstanceId();
		}
		if(rest_agent_id.length() > 512){
			rest_agent_id= rest_agent_id.substring(0,512);
		}
		
		agentInfo.setDhcpUpdateMode(DhcpUpdateModeEnum.fromValue(
				DhcpUpdateMode.fromValue(AgentProperties.getProperty("dhcp.update.mode")).name()));
		
		String autoAssignScopeIds = AgentProperties.getProperty("repository.autoassign.scopeids", "");
		if (!autoAssignScopeIds.isEmpty()) {
			agentInfo.setAssignScopeList(Arrays.asList(AgentProperties.getProperty("repository.autoassign.scopeids", "").split(",")));
		}
		
		return agentInfo;
	}

	/**
	 * Java環境情報をクリアします。
	 */
	public static void clearJavaInfo() {
		synchronized (Agent.class) {
			javaInfo = null;
		}
	}
	
	/**
	 * システムから、Java環境情報を取得します。
	 * @return Java環境情報。
	 */
	public static AgentJavaInfoRequest getJavaInfo() {
		synchronized (Agent.class) {
			if (javaInfo == null) {
				javaInfo = new AgentJavaInfoRequest();
				javaInfo.setOsVersion(System.getProperty("os.version"));
				javaInfo.setOsArch(System.getProperty("os.arch"));
				javaInfo.setSunArchDataModel(System.getProperty("sun.arch.data.model"));
				javaInfo.setJavaVendor(System.getProperty("java.vendor"));
				javaInfo.setJavaVersion(System.getProperty("java.version"));
				javaInfo.setJavaSpecificationVersion(System.getProperty("java.specification.version"));
				javaInfo.setJavaClassVersion(System.getProperty("java.class.version"));
				javaInfo.setJavaVmInfo(System.getProperty("java.vm.info"));
				javaInfo.setJavaVmVersion(System.getProperty("java.vm.version"));
				javaInfo.setJavaVmName(System.getProperty("java.vm.name"));
				javaInfo.setOsName(System.getProperty("os.name"));
				// ログへ記録しておく
				m_log.info("getJavaInfo: osName=" + javaInfo.getOsName() + ", osVersion=" + javaInfo.getOsVersion()
						+ ", osArch=" + javaInfo.getOsArch() + ", sunArchDataModel=" + javaInfo.getSunArchDataModel()
						+ ", javaVendor=" + javaInfo.getJavaVendor() + ", javaVersion=" + javaInfo.getJavaVersion()
						+ ", javaSpecificationVersion=" + javaInfo.getJavaSpecificationVersion() + ", javaClassVersion="
						+ javaInfo.getJavaClassVersion() + ", javaVmInfo=" + javaInfo.getJavaVmInfo()
						+ ", javaVmVersion=" + javaInfo.getJavaVmVersion() + ", javaVmName="
						+ javaInfo.getJavaVmName());
			}
		}
		return javaInfo;
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
		for (String ipAddress : agentInfo.getIpAddressList()) {
			str.append(", " +ipAddress);
		}
		return str.toString();
	}

	public static String getAgentHome() {
		return agentHome;
	}
	
	public static String getRestAgentId() {
		return rest_agent_id;
	}

	public static boolean isRegistered() {
		return registered.get();
	}

	public static void setRegistered(boolean bool) {
		registered.set(bool);
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