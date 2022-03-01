/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.bean.ExecFacilityConstant;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.bean.SyslogFacilityConstant;
import com.clustercontrol.notify.bean.SyslogSeverityConstant;
import com.clustercontrol.notify.entity.NotifyLogEscalateInfoData;
import com.clustercontrol.notify.model.NotifyLogEscalateInfo;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.FacilityUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.StringBinder;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * syslogに転送するクラス<BR>
 *
 * @version 3.0.0
 * @since 3.0.0
 */
public class SendSyslog implements Notifier {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SendSyslog.class );

	/** Syslogヘッダー部分時刻フォーマット。 */
	public static final String HEADER_DATE_FORMAT = "MMM dd HH:mm:ss";

	/** Syslogヘッダー部分のHOSTNAME。 */
	private static int MODE_HOSTNAME;
	private static String HOSTNAME_DEFINED = null;
	private static String HOSTNAME_SERVER = "unknownhost";

	private static final int MODE_HOSTNAME_DEFAULT = 0;
	private static final int MODE_HOSTNAME_DEFINED = 1;
	private static final int MODE_HOSTNAME_FACILITYID = 2;
	private static final int MODE_HOSTNAME_NODENAME = 3;

	private static int LIMIT_SIZE = 1024;

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
		// 該当する重要度の通知情報を取得する
		NotifyLogEscalateInfo logEscalateInfo
		= QueryUtil.getNotifyLogEscalateInfoPK(notifyId);

		NotifyLogEscalateInfoData escalateInfoData = new NotifyLogEscalateInfoData();
		escalateInfoData.setNotifyId(logEscalateInfo.getNotifyId());

		switch (outputInfo.getPriority()) {
		case PriorityConstant.TYPE_INFO:
			escalateInfoData.setValidFlg(logEscalateInfo.getInfoValidFlg());
			escalateInfoData.setEscalateMessage(logEscalateInfo.getInfoEscalateMessage());
			escalateInfoData.setSyslogPriority(logEscalateInfo.getInfoSyslogPriority());
			escalateInfoData.setSyslogFacility(logEscalateInfo.getInfoSyslogFacility());
			break;
		case PriorityConstant.TYPE_WARNING:
			escalateInfoData.setValidFlg(logEscalateInfo.getWarnValidFlg());
			escalateInfoData.setEscalateMessage(logEscalateInfo.getWarnEscalateMessage());
			escalateInfoData.setSyslogPriority(logEscalateInfo.getWarnSyslogPriority());
			escalateInfoData.setSyslogFacility(logEscalateInfo.getWarnSyslogFacility());
			break;
		case PriorityConstant.TYPE_CRITICAL:
			escalateInfoData.setValidFlg(logEscalateInfo.getCriticalValidFlg());
			escalateInfoData.setEscalateMessage(logEscalateInfo.getCriticalEscalateMessage());
			escalateInfoData.setSyslogPriority(logEscalateInfo.getCriticalSyslogPriority());
			escalateInfoData.setSyslogFacility(logEscalateInfo.getCriticalSyslogFacility());
			break;
		case PriorityConstant.TYPE_UNKNOWN:
			escalateInfoData.setValidFlg(logEscalateInfo.getUnknownValidFlg());
			escalateInfoData.setEscalateMessage(logEscalateInfo.getUnknownEscalateMessage());
			escalateInfoData.setSyslogPriority(logEscalateInfo.getUnknownSyslogPriority());
			escalateInfoData.setSyslogFacility(logEscalateInfo.getUnknownSyslogFacility());
			break;
		default:
			break;
		}

		escalateInfoData.setEscalateFacilityFlg(logEscalateInfo.getEscalateFacilityFlg());
		escalateInfoData.setEscalateFacility(logEscalateInfo.getEscalateFacility());
		escalateInfoData.setEscalatePort(logEscalateInfo.getEscalatePort() );
		escalateInfoData.setOwnerRoleId(logEscalateInfo.getNotifyInfoEntity().getOwnerRoleId());

		// syslogの本文を作成
		String message = getMessage(outputInfo, logEscalateInfo);

		/**
		 * 実行
		 */
		List<InetAddress> ipAddresses = getIpAddresses(outputInfo, escalateInfoData);
		if(ipAddresses == null){
			String detailMsg = "IP Address is empty.";
			m_log.info(detailMsg);
			String[] args = { notifyId };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_007, args, detailMsg);

		}

		// ヘッダー部分のTIMESTAMPを生成
		SimpleDateFormat sdf = new SimpleDateFormat(HEADER_DATE_FORMAT, Locale.US);
		sdf.setTimeZone(HinemosTime.getTimeZone());
		String headerTimeStamp = sdf.format(HinemosTime.getDateInstance());
		if(m_log.isDebugEnabled()){
			m_log.debug("sendlog() target message. notifyId = " + escalateInfoData.getNotifyId() +
					", headerTimeStamp = " + headerTimeStamp +
					", facilityId = " + outputInfo.getFacilityId() +
					", message = " + message);
		}

		if(ipAddresses == null){
			//findbugs対応 nulpo防止
			return;
		}
		for(InetAddress address : ipAddresses){
			try {
				sendMsgWithRetry(address, 
						escalateInfoData.getEscalatePort(),
						escalateInfoData.getSyslogPriority() + escalateInfoData.getSyslogFacility(),
						headerTimeStamp,
						getSyslogHeaderHost(outputInfo.getFacilityId()),
						message
						);
			} catch (IOException e) {
				String detailMsg = e.getMessage() + " IP Address = " + address;
				m_log.info("sendlog() " + detailMsg + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				String[] args = { notifyId };
				AplLogger.put(InternalIdCommon.PLT_NTF_SYS_007, args, detailMsg);

				// 続けて次のIPを処理する
			}
		}
	}

	/**
	 * 指定の通知情報をもとにsyslog送信先のノードを特定します。
	 * NotifyLogEscalateInfoData の EscalateFacilityFlg が「ExecFacilityConstant.TYPE_GENERATION」にも
	 * かかわらず、outputInfoがnullの場合は、要素のない空のリストを返す。
	 *
	 * @param outputInfo 通知グループ情報
	 * @param logEscalateInfo ログエスカレーション設定
	 * @return 送信先ノードのIPアドレス
	 */
	private List<InetAddress> getIpAddresses(OutputBasicInfo outputInfo, NotifyLogEscalateInfoData logEscalateInfo){

		// 送信先を特定
		ArrayList<InetAddress> ipAddresses = new ArrayList<InetAddress>();
		//リポジトリ(RepositoryControllerLocal)を取得
		try {
			String facilityId = null;

			// 固定スコープ
			if(logEscalateInfo.getEscalateFacilityFlg() == ExecFacilityConstant.TYPE_FIX) {
				// 以下の実装は、入力されているファシリティIDがノードのモノである場合
				facilityId = logEscalateInfo.getEscalateFacility();
			} else {
				// イベントが発生したノード
				if(outputInfo == null){
					// NotifyLogEscalateInfoData の EscalateFacilityFlg が「ExecFacilityConstant.TYPE_GENERATION」にも
					// かかわらず、outputInfoがnullの場合は、要素のない空のリストを返す。
					return ipAddresses;
				} else {
					facilityId = outputInfo.getFacilityId();
				}
			}

			ArrayList<String> facilityIdList = new RepositoryControllerBean().getExecTargetFacilityIdList(facilityId, logEscalateInfo.getOwnerRoleId());

			for(String targetFacilityId : facilityIdList){
				FacilityInfo facility
				= new RepositoryControllerBean().getFacilityEntityByPK(targetFacilityId);
				try {
					ipAddresses.add(getInetAdress(facility));
				} catch (UnknownHostException e) {
					m_log.info("getIpAddresses() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					// 続けて次のノードを処理する
				}
			}

			return ipAddresses;
		} catch (Exception e) {
			m_log.warn("getIpAddresses() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// 例外が発生した場合は、空のリストを返す
			return null;
		}
	}

	// 文字列を置換する
	private String getMessage(OutputBasicInfo outputInfo, NotifyLogEscalateInfo logEscalateInfo){
		// 文字列を置換する
		String message = "";
		switch (outputInfo.getPriority()) {
		case PriorityConstant.TYPE_INFO:
			message = logEscalateInfo.getInfoEscalateMessage();
			break;
		case PriorityConstant.TYPE_WARNING:
			message = logEscalateInfo.getWarnEscalateMessage();
			break;
		case PriorityConstant.TYPE_CRITICAL:
			message = logEscalateInfo.getCriticalEscalateMessage();
			break;
		case PriorityConstant.TYPE_UNKNOWN:
			message = logEscalateInfo.getUnknownEscalateMessage();
			break;
		default:
			break;
		}

		try {
			int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
			ArrayList<String> inKeyList = StringBinder.getKeyList(message, maxReplaceWord);
			Map<String, String> param = NotifyUtil.createParameter(outputInfo,
					logEscalateInfo.getNotifyInfoEntity(), inKeyList);
			StringBinder binder = new StringBinder(param);

			return binder.replace(message);
		} catch (Exception e) {
			m_log.warn("getMessage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// 例外が発生した場合は、置換前の文字列を返す
			return message;
		}
	}

	private InetAddress getInetAdress(FacilityInfo facility) throws UnknownHostException {
		if (!(facility instanceof NodeInfo))
			return InetAddress.getByName(null);
		
		NodeInfo node = (NodeInfo)facility;
		
		InetAddress ret = null;
		
		// IPアドレスの取得
		int version;
		if(node.getIpAddressVersion() != null){
			version = ((NodeInfo)facility).getIpAddressVersion();
		}else{
			version = 4;
		}
		// 入力されているバージョンを比較し、対応するIPアドレスを取得する
		if(version == 4) {
			ret = InetAddress.getByName(node.getIpAddressV4());
		}
		else {
			ret = InetAddress.getByName(node.getIpAddressV6());
		}

		return ret;
	}

	/**
	 * 汎用syslog送信処理(ShelfCheckTask、AplLogger)：
	 * 設定ファイルに従ってホスト名をヘッダに埋め込み、syslogパケットを送信する。<br>
	 *
	 * @param ipAddress
	 * @param port
	 * @param facility
	 * @param severity
	 * @param facilityId
	 * @param message
	 * @param timeStamp
	 */
	public void sendAfterConvertHostname(String ipAddress, int port, String facility, String severity, String facilityId, String message, String timeStamp) {
		// ローカル変数
		String hostname = "";

		// メイン処理
		hostname = getSyslogHeaderHost(facilityId);
		InetAddress inetAddress = null;

		int facilityInt = -1;
		int severityInt = -1;
		int priority = -1;

		// メイン処理
		try {
			// generate InetAddress
			inetAddress = InetAddress.getByName(ipAddress);

			// generate priority of header
			facilityInt = SyslogFacilityConstant.stringToType(facility);
			severityInt = SyslogSeverityConstant.stringToType(severity);
			if (facilityInt == -1 || severityInt == -1) {
				m_log.info("send() sending syslog failure. facility(" + facility + ") or severity(" + severity + ") is invalid.");
				return;
			}
			priority = facilityInt + severityInt;

			sendMsgWithRetry(inetAddress, 
					port,
					priority,
					timeStamp,
					hostname,
					message);
			
		} catch (UnknownHostException e) {
			m_log.info("sending syslog failure. syslog's host(" + ipAddress + ") is invalid."
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		} catch (SocketException e) {
			m_log.info("sending syslog failure."
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		} catch (IOException e) {
			m_log.info("sending syslog failure."
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
	}

	/**
	 * 設定ファイルに従って、syslogヘッダのホスト名として埋め込む文字列を生成する
	 * @param facilityId 通知情報の対象ファシリティID
	 * @return
	 */
	private static String getSyslogHeaderHost(String facilityId) {
		JpaTransactionManager jtm = null;
		// ローカル変数
		String hostname = HinemosPropertyCommon.notify_log_escalate_manager_hostname.getStringValue();

		if (hostname == null) {
			// undef hostname of syglog header
			MODE_HOSTNAME = MODE_HOSTNAME_DEFAULT;
		} else if ("".equals(hostname)) {
			// invalid hostname of syslog header
			MODE_HOSTNAME = MODE_HOSTNAME_DEFAULT;
		} else if ("#[FACILITY_ID]".equals(hostname)) {
			// use facility_id as hostname of syslog header
			MODE_HOSTNAME = MODE_HOSTNAME_FACILITYID;
		} else if ("#[NODE]".equals(hostname)) {
			// use nodename as hostname of syslog header
			MODE_HOSTNAME = MODE_HOSTNAME_NODENAME;
		} else {
			MODE_HOSTNAME = MODE_HOSTNAME_DEFINED;
			HOSTNAME_DEFINED = hostname;
		}
		try {
			HOSTNAME_SERVER = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			m_log.warn("static() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		m_log.debug("syslog sender. (mode = " + MODE_HOSTNAME + ", hostname=" + HOSTNAME_DEFINED + ")");

		int mode = MODE_HOSTNAME;
		boolean isBuildinScope = false;

		// メイン処理
		if (m_log.isDebugEnabled()) m_log.debug("converting syslog hostname for '" + facilityId + "'. (mode = " + MODE_HOSTNAME + ")");

		if (FacilityTreeAttributeConstant.isBuiltinScope(facilityId)) {
			// 組み込みスコープの場合は、マネージャのホスト名を埋め込む
			if (m_log.isDebugEnabled()) m_log.debug("facility '" + facilityId + "' is buildin scope.");
			isBuildinScope = true;
		}

		switch (mode) {
		case MODE_HOSTNAME_DEFINED :
			hostname = HOSTNAME_DEFINED;
			break;
		case MODE_HOSTNAME_FACILITYID :
			hostname = isBuildinScope ? HOSTNAME_SERVER : facilityId;
			break;
		case MODE_HOSTNAME_NODENAME :
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				FacilityInfo facility
				= new RepositoryControllerBean().getFacilityEntityByPK(facilityId);
				if (FacilityUtil.isNode(facility)) {
					hostname = isBuildinScope ? HOSTNAME_SERVER :
						(facility instanceof NodeInfo ? ((NodeInfo)facility).getNodeName() : null);
				} else {
					if (m_log.isDebugEnabled()) m_log.debug("facility '" + facilityId + "' is not node.");
					hostname = HOSTNAME_SERVER;
				}
				jtm.commit();
			} catch (Exception e) {
				hostname = HOSTNAME_SERVER;
				m_log.warn("getSyslogHeaderHost() use '" + hostname + "' for hostname of syslog header. (facility not found : " + facilityId + ") : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				if (jtm != null)
					jtm.rollback();
			} finally {
				if (jtm != null)
					jtm.close();
			}
			break;
		default :
			hostname = HOSTNAME_SERVER;
		}

		if (m_log.isDebugEnabled()) m_log.debug("use syslog hostname '" + hostname + "' for '" + facilityId + "'.");
		return hostname;
	}

	private void sendMsgWithRetry(InetAddress ipAddress, int port, int syslogPriority, String headerTimestamp,
			String hostname, String message) throws IOException {

		String protocol = HinemosPropertyCommon.notify_log_escalate_manager_protocol.getStringValue();
		
		String sendMessage = "<"+ syslogPriority + ">" + headerTimestamp + " " + hostname + " " + message;

		// 1024バイトを超える場合は、1024バイトまでを送信する（文字化けは考慮しない）
		if(sendMessage.getBytes().length > LIMIT_SIZE){
			byte[] buf = sendMessage.getBytes();
			sendMessage = new String(buf, 0, LIMIT_SIZE);
		}

		m_log.debug("sendMsgWithRetry. (ipAddresss=" + ipAddress + ", port=" + port +
				", sendMessage=" + sendMessage + ")");

		int retryCount = HinemosPropertyCommon.notify_log_escalate_manager_retry_count.getIntegerValue();
		int retryInterval = HinemosPropertyCommon.notify_log_escalate_manager_retry_interval.getIntegerValue();
		IOException lastException = null;
		int retrytime;
		for (retrytime = 0; retrytime < retryCount; retrytime++) {
			try {
				if ("udp".equals(protocol)) {
					sendUdpMsg(ipAddress, port, sendMessage);
				} else {
					sendTcpMsg(ipAddress, port, sendMessage);
				}

				break;
			} catch (IOException e) {
				m_log.warn("sendMsgWithRetry() : " + e.getClass().getSimpleName() + 
						", retried time=" + (retrytime + 1) +
						", protocol=" + protocol +
						", message=" + e.getMessage());

				lastException = e;

				try {
					Thread.sleep(retryInterval);
				} catch (InterruptedException e1) {
				}
			}
		}

		if (retrytime == retryCount && lastException != null) {
			throw lastException;
		}
	}

	private void sendTcpMsg(InetAddress ipAddress, int port, String msg)
			throws IOException {
		
		Socket socket = null;
		OutputStream os = null;
		PrintWriter writer = null;
		try {
			InetSocketAddress endpoint= new InetSocketAddress(ipAddress, port); 
			socket = new Socket() ; 
			socket.connect(endpoint, HinemosPropertyCommon.notify_log_escalate_manager_tcp_timeout.getIntegerValue());
			
			os = socket.getOutputStream();
			writer = new PrintWriter(socket.getOutputStream(), true);
			msg = msg + "\n"; 
			writer.print(msg);
			writer.flush();
		} finally {
			if (writer != null) {
				writer.close();
			}
			if (os != null) {
				os.close();
			}
			if (socket != null) {
				socket.close();
			}
		}
	}

	private void sendUdpMsg(InetAddress ipAddress, int port, String msg)
			throws IOException {
		DatagramSocket soc = null;
		try {
			// ソケットを作成してサーバに接続する。
			soc = new DatagramSocket(); // データグラムソケットを開く
			DatagramPacket sendPacket = null; // データグラムパケット設定

			sendPacket = new DatagramPacket(msg.getBytes(),
					msg.getBytes().length, ipAddress, port);
			soc.send(sendPacket);
		} finally {
			if (soc != null) {
				soc.close();
			}
		}
	}
}
