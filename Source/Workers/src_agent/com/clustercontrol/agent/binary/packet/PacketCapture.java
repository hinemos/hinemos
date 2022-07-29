/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.packet;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.Pcaps;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.binary.BinaryMonitorConfig;
import com.clustercontrol.agent.binary.BinaryMonitorManager;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.MessageConstant;

public class PacketCapture {

	// ログ出力関連
	/** ロガー */
	private static Log m_log = LogFactory.getLog(PacketCapture.class);

	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/**
	 * パケットキャプチャー制御用オブジェクト取得.<br>
	 * <br>
	 * 引数で渡した監視設定に紐づくパケットキャプチャ制御用オブジェクトを生成する.
	 * 
	 * @param m_wrapper
	 *			  監視設定
	 * @param host
	 *			  エージェントホスト名/IPv4/IPv6
	 * 
	 * @return パケットキャプチャー制御用オブジェクト.
	 */
	public PcapHandle getPcapHandle(MonitorInfoWrapper m_wrapper, String host) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + String.format("start. host=%s", host));

		String filter = null;
		String userFilter = null;
		String defaultFilter = null;
		try {
			// IPアドレスオブジェクトの生成
			InetAddress address = InetAddress.getByName(host);

			// キャプチャ対象のネットワークデバイスを取得.
			PcapNetworkInterface nif;
			nif = Pcaps.getDevByAddress(address);
			if (nif == null) {
				m_log.warn(methodName + DELIMITER + String.format("failed to get net work device. host=%s", host));
				return null;
			}

			// 監視設定等からパケットキャプチャの設定取得.
			int snaplen = BinaryMonitorConfig.getSnapLength();
			boolean promodeFlg = m_wrapper.monitorInfo.getPacketCheckInfo().getPromiscuousMode().booleanValue();
			PromiscuousMode promode = PacketCaptureUtil.getPromiscuousMode(promodeFlg);
			int timeoutMillis = BinaryMonitorConfig.getTimeoutMillis();

			// パケット取得開始時の設定処理・制御用オブジェクト作成
			// openLiveはlibpcapのnativeコードをwrapperしたメソッド.
			// 詳細は下記参照.
			// https://www.tcpdump.org/manpages/pcap_open_live.3pcap.html
			PcapHandle handle = nif.openLive(snaplen, promode, timeoutMillis);

			// 監視設定でフィルター用文字列が設定されている場合は設定.
			userFilter = m_wrapper.monitorInfo.getPacketCheckInfo().getFilterStr();

			// マネージャー⇔エージェント間の通信をパケットキャプチャ対象外とするフィルタ生成.
			defaultFilter = getDefaultBpfStr(host, address);
			if (defaultFilter == null) {
				// 生成できなかった場合はパケットキャプチャ実施しない.
				m_log.warn(methodName + DELIMITER + String.format("failed to make default filter. host=%s", host));
				return null;
			}

			// ユーザー設定フィルターとデフォルトフィルターを連結してパケットキャプチャ制御に設定.
			if (userFilter != null && !userFilter.isEmpty()) {
				filter = "(" + defaultFilter + ") and (" + userFilter + ")";
			} else {
				filter = defaultFilter;
			}
			handle.setFilter(filter, BpfCompileMode.OPTIMIZE);

			return handle;

		} catch (UnsatisfiedLinkError e) {
			// libpcap/winpcap 未インストール.
			m_log.warn(methodName + DELIMITER + "please install libpcap or winpcap : " + e.getMessage(), e);
			// 監視履歴に通知.
			BinaryMonitorManager.sendMessage(PriorityConstant.TYPE_CRITICAL, MessageConstant.AGENT.getMessage(),
					MessageConstant.MESSAGE_PCAP_INSTALL.getMessage(), e.getMessage(),
					m_wrapper.monitorInfo.getMonitorId(), m_wrapper.runInstructionInfoReq,
					m_wrapper.monitorInfo.getMonitorTypeId());
		} catch (NotOpenException e) {
			// PcapHandleがクローズされている場合.
			m_log.warn(methodName + DELIMITER + "closed the handle error : " + e.getMessage(), e);
		} catch (PcapNativeException e) {
			// パケットキャプチャnativeコードのエラー.
			if (e.getMessage() != null && !e.getMessage().isEmpty()
					&& (e.getMessage().contains("syntax") || e.getMessage().contains("expression"))) {
				// フィルタ文法エラーはフィルタの内容出力して監視履歴に通知.
				if (filter == null) {
					filter = "null";
				}
				if (defaultFilter == null) {
					defaultFilter = "null";
				}
				if (userFilter == null) {
					userFilter = "null";
				}
				String errorMessage = String.format(
						"failure in native code for packet capture."
								+ " filter(all)=[%s], filter(default)=[%s], filter(user)=[%s] : " + e.getMessage(),
						filter, defaultFilter, userFilter);
				m_log.warn(methodName + DELIMITER + errorMessage, e);
				// 監視履歴に通知.
				String[] args = { userFilter };
				BinaryMonitorManager.sendMessage(PriorityConstant.TYPE_CRITICAL, MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_PCAP_FILTER.getMessage(args), errorMessage,
						m_wrapper.monitorInfo.getMonitorId(), m_wrapper.runInstructionInfoReq,
						m_wrapper.monitorInfo.getMonitorTypeId());
			} else {
				m_log.warn(methodName + DELIMITER + "failure in native code for packet capture : " + e.getMessage(), e);
			}
		} catch (UnknownHostException e) {
			// 指定ホスト名誤り.
			m_log.warn(methodName + DELIMITER + "failure of " + host + " : " + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * デフォルトで設定するBPF文字列を取得.<br>
	 * <br>
	 * Hinemosマネージャ⇔エージェント間の通信をパケットキャプチャ対象外とするBPF文字列生成.
	 * 
	 * @return デフォルトBPF文字列(生成不可はnull).
	 */
	private String getDefaultBpfStr(String agentHost, InetAddress address) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// フィルタ設定用のAgentアドレスを取得.
		m_log.debug(methodName + DELIMITER + String.format("start. agentHost=%s", agentHost));
		final String scopeDelimiter = "%";
		if (agentHost.contains(scopeDelimiter)) {
			// IPv6でスコープID指定されているとエラーとなるため、IPv4に変換する.
			boolean changed = false;
			try {
				// NIFと紐づくアドレス全量取得.
				NetworkInterface ni = NetworkInterface.getByInetAddress(address);
				Enumeration<InetAddress> addressList = ni.getInetAddresses();
				while (addressList.hasMoreElements()) {
					InetAddress inetAddress = addressList.nextElement();
					String hostAddress = inetAddress.getHostAddress();
					if (!hostAddress.contains(scopeDelimiter)) {
						// IPv6以外の形式ならセット.
						agentHost = hostAddress;
						m_log.debug(methodName + DELIMITER
								+ String.format("change address of agent from IPv6. address=%s", agentHost));
						changed = true;
						break;
					}
				}
				m_log.debug(methodName + DELIMITER + String.format(
						"end of changing address of agent from IPv6. changed=%b, address=%s", changed, agentHost));
			} catch (SocketException e) {
				changed = false;
				m_log.info(
						methodName + DELIMITER
								+ String.format("failed to change address of agent from IPv6. address=%s", agentHost),
						e);
			}
			// IPv4に変換できなかった場合はホスト名に変換する.
			if (!changed) {
				try {
					// マシンから直接取得.
					agentHost = InetAddress.getLocalHost().getHostName();
					m_log.info(methodName + DELIMITER
							+ String.format("set host name of agent from system. name=%s", agentHost));
				} catch (UnknownHostException e) {
					// AgentInfoから取得(ダミーホスト名が設定されている場合はパケットキャプチャ不可)
					agentHost = Agent.getAgentInfoRequest().getHostname();
					m_log.info(methodName + DELIMITER
							+ String.format("set host name of agent from AgentInfo. name=%s", agentHost), e);
				}
			}
		}

		// マネージャーのホスト名/IP ＋ ポート番号毎にデフォルトのフィルタ文字列リストを生成する.
		String managerAddressList = AgentProperties.getProperty("managerAddress");
		StringBuilder defaultFilterSb = null;
		List<StringBuilder> defaultFilterSbList = new ArrayList<StringBuilder>();
		for (String managerAddress : managerAddressList.split(",")) {
			// URLを生成.
			URL url = null;
			try {
				url = new URL(managerAddress.trim());
			} catch (MalformedURLException e) {
				// Agent.Propertiesで指定されているマネージャーアドレスが不正でURL変換できなかった場合は飛ばす.
				m_log.info(methodName + DELIMITER + "failed to change URL from [" + managerAddress.trim() + "]");
				continue;
			}
			// URLからマネージャーのホスト名/IPとポート番号を取得.
			String managerHost = url.getHost();
			// 対象のマネージャー⇔エージェント間の通信を除外するフィルタ文字列を生成する.
			// !((送信元Agent and 宛先Manager) or (送信元Manager and 宛先Agent))]
			defaultFilterSb = new StringBuilder();
			defaultFilterSb.append("!((");
			defaultFilterSb.append("dst " + managerHost);
			defaultFilterSb.append(" and src " + agentHost + ")");
			defaultFilterSb.append(" or (");
			defaultFilterSb.append("src " + managerHost);
			defaultFilterSb.append(" and dst " + agentHost + "))");
			defaultFilterSbList.add(defaultFilterSb);
			m_log.debug(methodName + DELIMITER + "created default BPF string for manager [" + managerHost
					+ "]. BPF string = [" + defaultFilterSb.toString() + "]");
		}

		// マネージャー毎に生成したBPF文字列を連結する.
		String defaultFilter = "";
		if (defaultFilterSbList.size() == 1) {
			// 1件のみの場合はそのまま.
			defaultFilter = defaultFilterSbList.get(0).toString();
			m_log.debug(methodName + DELIMITER + "created default BPF string from manager count = "
					+ defaultFilterSbList.size());
			m_log.debug(methodName + DELIMITER + "created default BPF string = [" + defaultFilter + "]");
		} else if (defaultFilterSbList.size() > 1) {
			defaultFilterSb = new StringBuilder();
			for (int i = 0; i < defaultFilterSbList.size(); i++) {
				defaultFilterSb.append("(");
				defaultFilterSb.append(defaultFilterSbList.get(i));
				defaultFilterSb.append(")");
				if ((i + 1) != defaultFilterSbList.size()) {
					// 最後尾以外はandで連結
					defaultFilterSb.append(" and ");
				}
			}
			defaultFilter = defaultFilterSb.toString();
			m_log.debug(methodName + DELIMITER + "created default BPF string from manager count = "
					+ defaultFilterSbList.size());
			m_log.debug(methodName + DELIMITER + "created default BPF string = [" + defaultFilter + "]");
		} else {
			// マネージャーが設定されていないなどの理由でBPFフィルター生成不可.
			m_log.info(methodName + DELIMITER + "faile to create default BPF string.");
			return null;
		}
		return defaultFilter;
	}

	/**
	 * パケットキャプチャー出力リスナー取得.<br>
	 * <br>
	 * 引数で渡した監視設定に基づいてキャプチャしたパケットをファイル出力するリスナーを作成する.
	 * 
	 * @param host
	 *			  キャプチャ対象 Network Interface のIPアドレス(出力ファイル名の識別用)
	 * @param m_wrapper
	 *			  監視設定
	 * @param handle
	 *			  パケットキャプチャ制御オブジェクト
	 * 
	 * @return パケット出力リスナー.
	 */
	public PacketListenerImpl getListener(String host, MonitorInfoWrapper moninfo, PcapHandle handle) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "argument host=[" + host + "]");

		if (handle == null) {
			// そもそもハンドルが生成されていない場合はnull返却.
			m_log.debug(methodName + DELIMITER + String.format("packet capture handle is empty. host=%s", host));
			return null;
		}

		// 出力先のディレクトリ等初期化.
		String dirPath = moninfo.monitorInfo.getBinaryCheckInfo().getDirectory();
		File dir = new File(dirPath);
		m_log.debug(methodName + DELIMITER + "output dirPath=[" + dirPath + "]");

		if (!dir.exists()) {
			// 出力先フォルダが存在しない場合は作成する.
			if (!dir.mkdirs()) {
				// 生成失敗した場合.
				m_log.warn(methodName + " failure of making" + dirPath);
			}
		}
		// ダンプファイルの最大サイズを取得.
		long dumpSize = BinaryMonitorConfig.getMaxDumpSize();
		m_log.debug(methodName + DELIMITER + "argument dumpSize=[" + dumpSize + "]");

		// パケット出力のリスナー定義.
		PacketListenerImpl listener = new PacketListenerImpl(moninfo, handle, dirPath, dumpSize, host);

		return listener;
	}
}
