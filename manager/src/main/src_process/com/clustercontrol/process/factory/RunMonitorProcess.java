/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.process.factory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.monitor.run.util.NodeToMonitorCache;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.poller.bean.PollerProtocolConstant;
import com.clustercontrol.poller.impl.Snmp4jPollerImpl;
import com.clustercontrol.poller.impl.WbemPollerImpl;
import com.clustercontrol.poller.util.DataTable;
import com.clustercontrol.poller.util.TableEntry;
import com.clustercontrol.process.entity.MonitorProcessPollingMstData;
import com.clustercontrol.process.entity.MonitorProcessPollingMstPK;
import com.clustercontrol.process.model.ProcessCheckInfo;
import com.clustercontrol.process.util.PollingDataManager;
import com.clustercontrol.process.util.ProcessProperties;
import com.clustercontrol.process.util.QueryUtil;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.MessageConstant;

/**
 * プロセス監視を実行するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class RunMonitorProcess extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorProcess.class );

	/** プロセス監視情報 */
	private ProcessCheckInfo m_process = null;

	/** コマンド */
	private String m_command = "";

	/** 引数 */
	private String m_param = "";

	/** メッセージ */
	private String m_message = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/**
	 * コンストラクタ
	 * @throws HinemosUnknown
	 */
	public RunMonitorProcess() throws HinemosUnknown {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.MonitorExecuteTask
	 */
	@Override
	protected RunMonitor createMonitorInstance() throws HinemosUnknown {
		// エラー時のメッセージを受け渡す
		RunMonitor monitor = new RunMonitorProcess();
		monitor.setMessage(m_message);
		return monitor;
	}
	
	/**
	 * @see runMonitorAggregateByNode
	 */
	protected List<OutputBasicInfo> runMonitorInfo() throws FacilityNotFound, MonitorNotFound, InvalidRole, HinemosUnknown {
		// リソース監視は、通常の他の監視の「監視項目単位」の監視実行ではなく、「ノード単位」で実行するため、
		// runMonitorInfo ではなく runMonitorInfoAggregatedByNode が監視の実態となる
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 各種プロトコル（SNMP等）を使用して対象からプロセス情報を取得し、そのデータを返す。
	 * 本関数は、基底クラスRunMonitorから呼び出される
	 * @throws HinemosUnknown 
	 */
	@Override
	protected Object preCollect(Set<Integer> execMonitorIntervals) throws HinemosUnknown {
		
		m_log.debug("preCollect() : start preCollect");
		
		if (!m_isMonitorJob) {
			// このノードで1つもプロセス監視設定が無い場合はスキップ
			if (NodeToMonitorCache.getInstance(HinemosModuleConstant.MONITOR_PROCESS).getMonitorsWithCalendar(m_facilityId, execMonitorIntervals).size() == 0) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("preCollect(); skip process polling because of no monitor setting. facilityId = " + m_facilityId);
				}
				return null;
			}
		}
		
		// リポジトリ,DBから設定情報を取得する(プラットフォームID, サブプラットフォームID, 収集方法など)
		PollingDataManager dataManager = new PollingDataManager(m_facilityId);
		String collectMethod = dataManager.getCollectMethod();

		if (collectMethod == null) {
			throw new IllegalStateException("preCollect() : collectMethod is not defined. facilityId = " + m_facilityId);
		}
		
		switch (collectMethod) {
			case PollerProtocolConstant.PROTOCOL_SNMP: {
				
				NodeInfo node = null;
				try {
					node = new RepositoryControllerBean().getNode(m_facilityId);
				} catch (FacilityNotFound | HinemosUnknown e) {
					throw new IllegalStateException("preCollect() : can't get NodeInfo. facilityId = " + m_facilityId);
				}
				
				// SNMPを使って問い合わせる
				DataTable snmpResponse = Snmp4jPollerImpl.getInstance().polling(
						node.getAvailableIpAddress(),
						node.getSnmpPort(),
						node.getSnmpVersion(),
						node.getSnmpCommunity(),
						node.getSnmpRetryCount(),
						node.getSnmpTimeout(),
						dataManager.getPollingTargets(collectMethod),
						node.getSnmpSecurityLevel(),
						node.getSnmpUser(),
						node.getSnmpAuthPassword(),
						node.getSnmpPrivPassword(),
						node.getSnmpAuthProtocol(),
						node.getSnmpPrivProtocol());
				
				// SNMPの問い合わせ結果をもとに、プロセスのリストを構築する
				List<ProcessInfo> procList = buildSnmpProcessList(snmpResponse, m_facilityId);
				
				if (m_log.isDebugEnabled()) {
					if (procList == null) {
						m_log.debug("preCollect() : facilityId = " + m_facilityId + ", procList = null");
					} else {
						m_log.debug("preCollect() : facilityId = " + m_facilityId + ", procList size = " + procList.size());
					}
				}
				
				return procList;
			}
			case PollerProtocolConstant.PROTOCOL_WBEM:
				NodeInfo node = null;
				try {
					node = new RepositoryControllerBean().getNode(m_facilityId);
				} catch (FacilityNotFound | HinemosUnknown e) {
					throw new IllegalStateException("preCollect() : can't get NodeInfo. facilityId = " + m_facilityId);
				}
				
				// WBEMを使って問い合わせる
				WbemPollerImpl poller = new WbemPollerImpl();
				DataTable wbemResponse = poller.polling(
						node.getAvailableIpAddress(),
						node.getWbemPort(),
						node.getWbemProtocol(),
						node.getWbemUser(),
						node.getWbemUserPassword(),
						node.getWbemNameSpace(),
						node.getWbemRetryCount(),
						node.getWbemTimeout(),
						dataManager.getPollingTargets(collectMethod));
				
				// WBEMの問い合わせ結果をもとに、プロセスのリストを構築する
				List<ProcessInfo> procList = buildWbemProcessList(wbemResponse, m_facilityId);
				
				if (m_log.isDebugEnabled()) {
					if (procList == null) {
						m_log.debug("preCollect() : facilityId = " + m_facilityId + ", procList = null");
					} else {
						m_log.debug("preCollect() : facilityId = " + m_facilityId + ", procList size = " + procList.size());
					}
				}
				
				return procList;
				
		}
		// FIXME nagatsumas 仮実装
		return null;
	}
	
	/**
	 * 収集済みのデータから、当該監視項目におけるプロセス情報を抽出する<BR>
	 * プロセス監視の場合、実際のデータ収集は preCollect にて実施している。
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 */
	@Override
	public boolean collect(String facilityId) {
		
		if (m_log.isDebugEnabled())
			m_log.debug("collect() : start." +
					" facilityId = " + m_facilityId +
					", monitorId = " + m_monitorId + 
					", monitorType = " + m_monitorTypeId);
		
		// 引数のコマンド,パラメータに一致したプロセス数
		int count = 0;

		// 監視開始時刻を設定
		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}

		// メッセージを設定
		m_messageOrg = MessageConstant.COMMAND.getMessage() + " : " + m_command + ", "
				+ MessageConstant.PARAM.getMessage() + " : " + m_param;

		// 監視設定のコマンド・パラメタのパターンマッチ文字列
		Pattern pCommand = null;
		Pattern pParam = null;
		try {
			// 大文字・小文字を区別しない場合
			if(m_process.getCaseSensitivityFlg()){
				pCommand = Pattern.compile(m_command, Pattern.CASE_INSENSITIVE);
				pParam = Pattern.compile(m_param, Pattern.CASE_INSENSITIVE);
			}
			// 大文字・小文字を区別する場合
			else{
				pCommand = Pattern.compile(m_command);
				pParam = Pattern.compile(m_param);
			}
		} catch (PatternSyntaxException e) {
			m_log.info("collect() command, parameter PatternSyntax error : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			m_message = MessageConstant.MESSAGE_PLEASE_SET_VALUE_WITH_REGEX.getMessage();
			return false;
		}
		
		@SuppressWarnings("unchecked")
		List<ProcessInfo> procList = (List<ProcessInfo>) preCollectData;
		
		if (procList == null) {
			// TODO nagatsumas この対処でOK？
			// 何らかの理由でプロセスのリストが取れない場合は不明とする
			return false;
		} else {
			// 全プロセスをなめて、コマンドと引数がマッチするものをカウントする
			for (ProcessInfo procInfo : procList) {
				if (pCommand.matcher(procInfo.command).matches()) {
					if (pParam.matcher(procInfo.param).matches()) {
						count++;
						// ノードの値取得時刻を設定
						m_nodeDate = procInfo.time;
							// オリジナルメッセージにコマンド名＋引数を与える設定
						if(ProcessProperties.getProperties().isDetailedDisplay()) {
							m_messageOrg = m_messageOrg + "\n";
						
							if (procInfo.pid != null) {
								// PIDが取得できた場合（SNMPの場合のみ）
								m_messageOrg = m_messageOrg + procInfo.pid + " : ";
							}
							
							m_messageOrg = m_messageOrg + procInfo.command + " " + procInfo.param;
						}
					}
				}
			}
		}
			// 正常終了
		m_value = (double) count;
		m_message = MessageConstant.PROCESS_NUMBER.getMessage() + " : "
				+ NumberFormat.getNumberInstance().format(m_value);
		
		if (m_log.isDebugEnabled())
			m_log.debug("collect() : end." +
					" facilityId = " + m_facilityId +
					", monitorId = " + m_monitorId + 
					", monitorType = " + m_monitorTypeId +
					", count = " + count);

		return true;
		
//		// 最大リトライ回数超過。「タイムアウトしました」メッセージでイベント通知
//		m_message = MessageConstant.MESSAGE_TIME_OUT.getMessage();
//		return false;
	}

	/**
	 * プロセス監視情報を取得します。<BR>
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {

		// プロセス監視情報を取得
		if (!m_isMonitorJob) {
			m_process = QueryUtil.getMonitorProcessInfoPK(m_monitorId);
		} else {
			m_process = QueryUtil.getMonitorProcessInfoPK(m_monitor.getMonitorId());
		}

		// プロセス監視情報を設定
		m_command = m_process.getCommand();
		if(m_process.getParam() != null){
			m_param = m_process.getParam();
		}
	}

	/**
	 * ノード用メッセージを取得します。<BR>
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {
		return m_message;
	}

	@Override
	public void setMessage(String message) {
		m_message = message;
	}
	
	
	/**
	 * ノード用オリジナルメッセージを取得します。<BR>
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		return m_messageOrg;
	}

	/**
	 * SNMPで取得したレスポンスから、各プロセスごとに、コマンド・引数・情報取得時刻・PIDを紐付けてプロセスのリストとして返す
	 * @param snmpResponse
	 * @param dataManager
	 * @return Listの各要素が1つ1つのプロセスをあらわす。レスポンスの異常などにより構築ができなかった場合、nullを返す。
	 */
	private List<ProcessInfo> buildSnmpProcessList(DataTable snmpResponse, String facilityId) {
		PollingDataManager dataManager = new PollingDataManager(facilityId);
		String runName = "";
		String runParam = "";
		String runPath = "";
		
		// SNMPで問い合わせるOIDを決める（DBから取得）
		{
			// cc_monitor_process_polling_mst から variable_id = "name" を取得
			MonitorProcessPollingMstData pollingBean;
			pollingBean = ProcessMasterCache.getMonitorProcessPollingMst(
					(new MonitorProcessPollingMstPK(
							PollerProtocolConstant.PROTOCOL_SNMP,
							dataManager.getPlatformId(),
							dataManager.getSubPlatformId(),
							"name")));
			if (pollingBean == null) {
				m_log.info("collect() pollingBean (name) is null");
				return null;
			}
			runName = PollerProtocolConstant.getEntryKey(PollerProtocolConstant.PROTOCOL_SNMP, pollingBean.getPollingTarget());
			m_log.debug("collect() runName : " + runName);

			// cc_monitor_process_polling_mst から variable_id = "param" を取得
			pollingBean = ProcessMasterCache.getMonitorProcessPollingMst(new MonitorProcessPollingMstPK(
					PollerProtocolConstant.PROTOCOL_SNMP,
					dataManager.getPlatformId(),
					dataManager.getSubPlatformId(),
					"param"));
			if (pollingBean == null) {
				m_log.info("collect() pollingBean (param) is null");
				return null;
			}
			runParam = PollerProtocolConstant.getEntryKey(PollerProtocolConstant.PROTOCOL_SNMP, pollingBean.getPollingTarget());
			m_log.debug("collect() runParam : " + runParam);

			// cc_monitor_process_polling_mst から variable_id = "path" を取得
			pollingBean = ProcessMasterCache.getMonitorProcessPollingMst(
					new MonitorProcessPollingMstPK(
							PollerProtocolConstant.PROTOCOL_SNMP,
							dataManager.getPlatformId(),
							dataManager.getSubPlatformId(),
							"path")
					);
			if (pollingBean == null){
				m_log.info("collect() pollingBean (path) is null");
				return null;
			}
			runPath = PollerProtocolConstant.getEntryKey(PollerProtocolConstant.PROTOCOL_SNMP, pollingBean.getPollingTarget());
			m_log.debug("collect() runPath : " + runPath);
		}
		
		// SNMPのレスポンスから、名前、パラメタ、コマンド部分を抽出する
		Set<TableEntry> nameEntrySet = snmpResponse.getValueSetStartWith(runName);
		Set<TableEntry> paramEntrySet = snmpResponse.getValueSetStartWith(runParam);
		Set<TableEntry> commandEntrySet = snmpResponse.getValueSetStartWith(runPath);
		if (nameEntrySet == null || paramEntrySet == null || commandEntrySet == null) {
			// 「値を取得できませんでした」メッセージでイベント通知
			// ポーリングに失敗して値が取得できていない等
			m_log.info("collect()  FacilityID : "
					+ facilityId
					+ ", "
					+ "valueSetName(Set) or valueSetParam(Set) or valueSetPath(Set) is null , SNMP Polling failed");
			return null;
		}
		
		if (m_log.isDebugEnabled()) {
			m_log.debug("process list (Name)    size = " + nameEntrySet.size());
			m_log.debug("process list (Param)   size = " + paramEntrySet.size());
			m_log.debug("process list (Command) size = " + commandEntrySet.size());
		}
		
		
		List<ProcessInfo> processList = new ArrayList<>();
		
		for (TableEntry nameEntry : nameEntrySet) {
			String pid;
			
			// 名前とコマンドの情報から、実行コマンドのパス（引数を除く）を構築する
			String command = null;
			long time = 0L;
			{
				String name = null;
				if (nameEntry != null) {
					if (!nameEntry.isValid()) {
						// pollingでエラーがあれば終了する
						m_message = nameEntry.getErrorDetail().getMessage();
						return null;
					}
					
					name = (String) nameEntry.getValue();
					if (name == null) {
						m_log.info("collect()  FacilityID : "
								+ facilityId
								+ ", "
								+ "NameEntry(value) is null. What snmp happened?");
						m_message = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_PROCESS.getMessage();
						return null;
					}
				} else {
					// エントリそのものがnullなら飛ばす
					m_log.debug("collect()  FacilityID : "
							+ facilityId
							+ ", "
							+ "NameEntry is null. What snmp happened?");
					continue;
				}
	
				// このエントリのOIDの末尾（つまりプロセス番号）を取得する
				String key = nameEntry.getKey();
				pid = key.substring(key.lastIndexOf("."));
	
				// 起動パスOIDを指定して値を取得
				TableEntry commandEntry = snmpResponse.getValue(runPath + pid);
				if (commandEntry != null) {
					command = (String) commandEntry.getValue();
					time = commandEntry.getDate();
					if (command == null) {
						m_log.info("collect()  FacilityID : "
								+ facilityId
								+ ", "
								+ "commandEntry(value) is null. What snmp happened?");
						m_message = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_PROCESS.getMessage();
						break;  // while文を抜ける
					}
				} else {
					// エントリそのものがnullなら飛ばす
					m_log.debug("collect()  FacilityID : "
							+ facilityId
							+ ", "
							+ "commandEntry is null. What snmp happened?");
					continue; 
				}
	
				// Windowsの場合を考慮して、valueCommandとvalueNameを連結する
				if (command.length() == 0) {
					// パスが取得できない場合はコマンド名
					// パスがnull OR 空文字
					command = name;
				} else if (!command.startsWith("/")
						&& command.endsWith("\\")
						&& !command.equals(name)) {
					// 条件・・・・
					// パスが'/'以外の文字で始まり
					// パスが'\'で終わっていて
					// パスとコマンド名が違う
	
					command = command + name;
				}
			}
			
			// 2. パラメタを探す
			String param = null;

			// 起動パラメータOIDを指定して値を取得
			TableEntry paramEntry = snmpResponse.getValue(runParam + pid);
			m_log.debug("collect()  FacilityID : " + facilityId
					+ ", " + " paramEntry: " + paramEntry);

			if (paramEntry != null) {
				param = (String) paramEntry.getValue();
				if (param == null) {
					m_log.info("collect()  FacilityID : "
							+ facilityId
							+ ", "
							+ "paramEntry(value) is null. What snmp happened?");
					m_message = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_PROCESS.getMessage();
					return null;
				}
			} else {
				// エントリそのものがnullなら飛ばす
				m_log.debug("collect()  FacilityID : "
						+ facilityId
						+ ", "
						+ "paramEntry is null. What snmp happened?");
				continue;
			}
			
			processList.add(new ProcessInfo(pid, command, param, time));
		}
		
		return processList;
	}
	
	/**
	 * WBEMで取得したレスポンスから、各プロセスごとに、コマンド・引数・情報取得時刻・PIDを紐付けてプロセスのリストとして返す
	 * @param wbemResponse
	 * @param dataManager
	 * @return Listの各要素が1つ1つのプロセスをあらわす。レスポンスの異常などにより構築ができなかった場合、nullを返す。
	 */
	private List<ProcessInfo> buildWbemProcessList(DataTable wbemResponse, String facilityId) {
		PollingDataManager dataManager = new PollingDataManager(facilityId);
		String runParam = "";	// WBEMで利用するのはparamのみ
		
		// WBEMで問い合わせるclassを決める（DBから取得）
		
		// cc_monitor_process_polling_mst から variable_id = "param" を取得
		MonitorProcessPollingMstData pollingBean;
		pollingBean = ProcessMasterCache.getMonitorProcessPollingMst(new MonitorProcessPollingMstPK(
				PollerProtocolConstant.PROTOCOL_WBEM,
				dataManager.getPlatformId(),
				dataManager.getSubPlatformId(),
				"param"));
		if (pollingBean == null) {
			m_log.info("collect() pollingBean (param) is null");
			return null;
		}
		runParam = PollerProtocolConstant.getEntryKey(PollerProtocolConstant.PROTOCOL_WBEM, pollingBean.getPollingTarget());
		m_log.debug("collect() runParam : " + runParam);

		
		// 起動名を指定してサブツリーの値を一度に取得
		Set<TableEntry> paramEntrySet = wbemResponse.getValueSetStartWith(runParam);

		if (paramEntrySet == null) {
			// 「値を取得できませんでした」メッセージでイベント通知
			// ポーリングに失敗して値が取得できていない等
			m_log.info("collect()  FacilityID : "
					+ facilityId
					+ ", "
					+ "paramEntrySet(Set) is null , WBEM Polling failed");
			return null;
		}
		
		if (m_log.isDebugEnabled()) {
			m_log.debug("process list (Param)   size = " + paramEntrySet.size());
		}
		
		List<ProcessInfo> processList = new ArrayList<>();
		
		for (TableEntry paramEntry : paramEntrySet) {
			String pid = null;
			String command = null;
			long time = 0L;
			String param = "";

			if (paramEntry != null) {
				
				if (!paramEntry.isValid()) {
					m_message = paramEntry.getErrorDetail().getMessage();
					return null;
				}
				
				@SuppressWarnings("unchecked")
				Vector<String> valueParam = (Vector<String>)paramEntry.getValue();
				
				if (valueParam == null) {
					m_log.info("collect()  FacilityID : "
							+ facilityId
							+ ", "
							+ "ParamEntry(value) is null. What wbem happened?");
					m_message = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_PROCESS.getMessage();
					return null;
				}
				
				command = valueParam.get(0).toString();
				m_log.debug("command : " + command);

				for(int i=1; i < valueParam.size(); i++) {
					param = param + valueParam.get(i);

					if(i+1 < valueParam.size()) {
						param = param + " ";
					}

					m_log.debug("param : " + param);
				}
			} else {
				// エントリそのものがnullなら飛ばす
				m_log.debug("collect()  FacilityID : "
						+ facilityId
						+ ", "
						+ "ParamEntry is null. What wbem happened?");
				continue;
			}
			
			time = paramEntry.getDate();
			
			processList.add(new ProcessInfo(pid, command, param, time));
		}

		return processList;
	}
	
	private static final class ProcessInfo {
		public final String command;
		public final String param;
		public final long time;
		public final String pid;
		public ProcessInfo(String pid, String command, String param, long time) {
			this.pid = pid;
			this.command = command;
			this.param = param;
			this.time = time;
		}
	}


	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		if (m_monitor == null || m_monitor.getProcessCheckInfo() == null) {
			return "";
		}
		String[] args = {
				m_monitor.getProcessCheckInfo().getCommand(),
				m_monitor.getProcessCheckInfo().getParam(),
				String.valueOf(m_monitor.getProcessCheckInfo().getCaseSensitivityFlg())};
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_PROCESS.getMessage(args)
				+ "\n" + orgMsg;
	}
}
