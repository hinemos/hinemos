/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 */

package com.clustercontrol.performance.monitor.factory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.commons.util.ObjectSharingService;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.NodeToMonitorCache;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.performance.bean.CollectorItemInfo;
import com.clustercontrol.performance.monitor.model.PerfCheckInfo;
import com.clustercontrol.performance.monitor.util.PerfDataQueue;
import com.clustercontrol.performance.monitor.util.QueryUtil;
import com.clustercontrol.performance.operator.Operator;
import com.clustercontrol.performance.operator.Operator.CollectedDataNotFoundException;
import com.clustercontrol.performance.operator.Operator.CollectedDataNotFoundWithNoPollingException;
import com.clustercontrol.performance.util.CalculationMethod;
import com.clustercontrol.performance.util.PollingDataManager;
import com.clustercontrol.performance.util.code.CollectorItemCodeTable;
import com.clustercontrol.poller.IPoller;
import com.clustercontrol.poller.bean.PollerProtocolConstant;
import com.clustercontrol.poller.impl.Snmp4jPollerImpl;
import com.clustercontrol.poller.impl.WbemPollerImpl;
import com.clustercontrol.poller.util.DataTable;
import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * リソース監視の閾値判定クラス
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class RunMonitorPerformance extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorPerformance.class );

	/** 閾値情報 */
	private PerfCheckInfo m_perf = null;

	/** 収集項目名 */
	private String m_itemName = null;

	/** デバイス情報 */
	private NodeDeviceInfo m_deviceData = null;

	/** エラーメッセージ */
	private String m_errorMessage = null;
	
	/**
	 * コンストラクタ
	 *
	 */
	public RunMonitorPerformance() {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 *
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.MonitorExecuteTask
	 */
	@Override
	protected RunMonitor createMonitorInstance() {
		return new RunMonitorPerformance();
	}

	/**
	 * @see runMonitorAggregateByNode
	 */
	protected boolean runMonitorInfo() throws FacilityNotFound, MonitorNotFound, InvalidRole, HinemosUnknown {
		// リソース監視は、通常の他の監視の「監視項目単位」の監視実行ではなく、「ノード単位」で実行するため、
		// runMonitorInfo ではなく runMonitorInfoAggregatedByNode が監視の実態となる
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 各種プロトコル（SNMP等）を使用して対象ノードからリソース情報を取得し、そのデータを返す。
	 * 本関数は、基底クラスRunMonitorから呼び出される
	 * @throws HinemosUnknown 
	 * @throws FacilityNotFound 
	 */
	@Override
	protected Object preCollect(Set<Integer> execMonitorIntervals) throws HinemosUnknown, FacilityNotFound {
		
		final RepositoryControllerBean repository = new RepositoryControllerBean();
		NodeInfo node = null;
		try {
			node = repository.getNode(m_facilityId);
		} catch (FacilityNotFound | HinemosUnknown e) {
			m_log.warn("preCollect() : failed to execute repository.getNode(). facilityId = " + m_facilityId, e);
			return null;
		}
		if (!m_isMonitorJob) {
			// 引数で与えられた監視間隔において必要となる全キー情報（SNMPならOID）を取り出す
			final Map<String, Set<String>> target = getPollingTarget(execMonitorIntervals);
			
			// 収集方法ごとにポーリング処理を行ない、全てのデータをallResponseに格納していく
			final Map<String, DataTable> allResponse = new HashMap<>();
			for (final Map.Entry<String, Set<String>> targetEntry : target.entrySet()) {
				final String collectMethod = targetEntry.getKey();
				switch (collectMethod) {
					case PollerProtocolConstant.PROTOCOL_SNMP: {
						final DataTable snmpResponse = Snmp4jPollerImpl.getInstance().polling(
								node.getAvailableIpAddress(),
								node.getSnmpPort(),
								node.getSnmpVersion(),
								node.getSnmpCommunity(),
								node.getSnmpRetryCount(),
								node.getSnmpTimeout(),
								targetEntry.getValue(),
								node.getSnmpSecurityLevel(),
								node.getSnmpUser(),
								node.getSnmpAuthPassword(),
								node.getSnmpPrivPassword(),
								node.getSnmpAuthProtocol(),
								node.getSnmpPrivProtocol());
						allResponse.put(PollerProtocolConstant.PROTOCOL_SNMP, snmpResponse);
						break;
					}
					case PollerProtocolConstant.PROTOCOL_WBEM: {
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
								targetEntry.getValue());
						allResponse.put(PollerProtocolConstant.PROTOCOL_WBEM, wbemResponse);
						break;
					}
					default: {
						// *****************************
						// オプションのpollerの設定
						//	pollingTargetMapのKEYにより識別する
						//	SNMP	SNMPポーラ(非オプション機能)
						//	WBEM	WBEMポーラ(非オプション機能)
						//	VM.		VM管理オプション(VmPollerImplInterface)	(ex. VM.XEN)
						//	CLOUD.	クラウド管理オプション(ICloudPoller)		(ex. CLOUD.AWS)
						// *****************************
						
						////
						// 各種オプションでの監視用ポーラ取得箇所
						// IPollerインターフェースを継承したポーラが、ポーラーのプロトコル名をキーにして事前にObjectSharingServiceに
						// 登録されている場合、そのインスタンスを取り出して監視を実行する。
						// 各種オプションのJARファイル内で、Plugin機構により登録されている前提。
						// 2013年8月末時点で、VM・クラウドの2オプションで本機構を使用している
						////
						IPoller poller;
						try {
							poller = ObjectSharingService.objectRegistry().get(IPoller.class, collectMethod);
						} catch (InstantiationException | IllegalAccessException e) {
							m_log.warn(String.format("polling() : %s, %s", e.getClass().getSimpleName(), e.getMessage()), e);
							continue;
						}
						
						if (poller == null) {
							m_log.warn("polling : unknown pollerProtocol. facilityId = " + node.getFacilityId() + ", protocol = " + collectMethod);
							continue;
						}
						
						// ポーラーの実体のメソッドを呼び出して、実際にポーリングを行う
						// もし独自のオプション製品で、nodeinfoとpollingTarget以外のパラメタが必要な場合、
						// 第3引数のObjectに突っ込むことで対応する。
						// その場合は独自のオプション用にpollerProtocolの内容で分岐する必要がある。
						// （できれば本体側に手を入れずにすむよう、第三引数は使わず、ポーラ内で情報を取ることが望ましいが・・・）
						final DataTable pluginResponse = poller.polling(node, targetEntry.getValue(), null);
						allResponse.put(collectMethod, pluginResponse);
					}
				}
			}
			
			// 取得したデータを登録する
			final Map<Integer, Map<String, Set<String>>> pollingTarget = new HashMap<>();
			for (final int interval : execMonitorIntervals) {
				// TODO ポーリングターゲットを再計算しているが、本来的には上で実際に値取得に使ったポーリングターゲットを使うべき
				// TODO 上とここで齟齬が出るのは監視設定が変わったときなどのみなので、この実装でもあまり問題は無いはず
				pollingTarget.put(interval, getPollingTarget(Collections.singleton(interval)));
			}
			PerfDataQueue.getInstance(m_facilityId).pushNewData(pollingTarget, allResponse);
			return null;
		} else {
			// 監視情報で必要な全キー情報（SNMPならOID）を取り出す
			final Map<String, Set<String>> target = getPollingTarget(null);

			// 収集方法ごとにポーリング処理を行なう
			if (target.containsKey(PollerProtocolConstant.PROTOCOL_SNMP)) {
				m_curData = Snmp4jPollerImpl.getInstance().polling(
						node.getAvailableIpAddress(),
						node.getSnmpPort(),
						node.getSnmpVersion(),
						node.getSnmpCommunity(),
						node.getSnmpRetryCount(),
						node.getSnmpTimeout(),
						target.get(PollerProtocolConstant.PROTOCOL_SNMP),
						node.getSnmpSecurityLevel(),
						node.getSnmpUser(),
						node.getSnmpAuthPassword(),
						node.getSnmpPrivPassword(),
						node.getSnmpAuthProtocol(),
						node.getSnmpPrivProtocol());
			} else if (target.containsKey(PollerProtocolConstant.PROTOCOL_WBEM)) {
				// WBEMを使って問い合わせる
				WbemPollerImpl poller = new WbemPollerImpl();
				m_curData = poller.polling(
						node.getAvailableIpAddress(),
						node.getWbemPort(),
						node.getWbemProtocol(),
						node.getWbemUser(),
						node.getWbemUserPassword(),
						node.getWbemNameSpace(),
						node.getWbemRetryCount(),
						node.getWbemTimeout(),
						target.get(PollerProtocolConstant.PROTOCOL_WBEM));
			} else {
				// *****************************
				// オプションのpollerの設定
				//	pollingTargetMapのKEYにより識別する
				//	SNMP	SNMPポーラ(非オプション機能)
				//	WBEM	WBEMポーラ(非オプション機能)
				//	VM.		VM管理オプション(VmPollerImplInterface)	(ex. VM.XEN)
				//	CLOUD.	クラウド管理オプション(ICloudPoller)		(ex. CLOUD.AWS)
				// *****************************
				
				////
				// 各種オプションでの監視用ポーラ取得箇所
				// IPollerインターフェースを継承したポーラが、ポーラーのプロトコル名をキーにして事前にObjectSharingServiceに
				// 登録されている場合、そのインスタンスを取り出して監視を実行する。
				// 各種オプションのJARファイル内で、Plugin機構により登録されている前提。
				// 2013年8月末時点で、VM・クラウドの2オプションで本機構を使用している
				////
				IPoller poller = null;
				Map.Entry<String, Set<String>> matchedEntry = null;
				for (final Map.Entry<String, Set<String>> targetEntry : target.entrySet()) {
					try {
						poller = ObjectSharingService.objectRegistry().get(IPoller.class, targetEntry.getKey());
						if (poller != null) {
							matchedEntry = targetEntry;
							break;
						}
					} catch (InstantiationException | IllegalAccessException e) {
						m_log.warn(String.format("polling() : %s, %s", e.getClass().getSimpleName(), e.getMessage()), e);
					}
				}
				
				if (poller != null) {
					// ポーラーの実体のメソッドを呼び出して、実際にポーリングを行う
					// もし独自のオプション製品で、nodeinfoとpollingTarget以外のパラメタが必要な場合、
					// 第3引数のObjectに突っ込むことで対応する。
					// その場合は独自のオプション用にpollerProtocolの内容で分岐する必要がある。
					// （できれば本体側に手を入れずにすむよう、第三引数は使わず、ポーラ内で情報を取ることが望ましいが・・・）
					m_curData = poller.polling(node, matchedEntry.getValue(), null);
				} else {
					m_log.debug("polling() : not found plugin poller.");
				}
			}
			return null;
		}
	}


	@Override
	protected void checkMultiMonitorInfoData(final Object preCollectData, final List<RunMonitor> runMonitorList) {
		
		List<Sample> sampleList= new ArrayList<Sample>();
		
		/**
		 * 以降、判定処理を行う
		 * pre処理にてデータは収集済みなので、閾値判定処理のみを監視項目それぞれに対して直列に実施する
		 * （並行で実施するメリットはないため、無駄にスレッドを使用しない意味で直列実行する）
		 */
		for (final RunMonitor runMonitor : runMonitorList) {
			final List<MonitorRunResultInfo> resultNotifyList = new ArrayList<>();
			final RunMonitorPerformance runMonitorPerf = (RunMonitorPerformance) runMonitor;
			List<MonitorRunResultInfo> resultList;
			try {
				resultList = runMonitorPerf.collectList();
			} catch (FacilityNotFound e) {
				// TODO nagatsumas ログ出力必須、INTERNAL通知も必要か？
				return;
			}
			
			Sample sample = null;
			if (runMonitorPerf.getMonitorInfo().getCollectorFlg().booleanValue()) {
				sample = new Sample(HinemosTime.getDateInstance(), runMonitor.getMonitorInfo().getMonitorId());
			}
			
			// 1つの監視項目から複数の結果が上がる（デバイス等）ため、その処理を行なう
			for(MonitorRunResultInfo result : resultList){
				m_nodeDate = result.getNodeDate();
				
				// 通知（実際の通知は監視項目単位で実施）
				if(m_isMonitorJob || result.getMonitorFlg()){
					resultNotifyList.add(result);
				}
				
				// 収集値（実際の収集値登録は監視項目単位で実施）
				if (sample != null) {
					if(result.getCollectorFlg()){
						int errorCode = 0;
						if(result.isCollectorResult()){
							errorCode = CollectedDataErrorTypeConstant.NOT_ERROR;
						}else{
							errorCode = CollectedDataErrorTypeConstant.UNKNOWN;
						}
						sample.set(m_facilityId, result.getItemName(), result.getValue(), errorCode, result.getDisplayName());
					}
				}
			}
			// 監視項目ごとに（監視項目内の詳細・内訳を）まとめて通知・収集値登録を行なう
			if (!m_isMonitorJob) {
				notify(runMonitor.getMonitorInfo(), m_facilityId, resultNotifyList);
			} else {
					m_monitorRunResultInfo = new MonitorRunResultInfo();
					m_monitorRunResultInfo.setNodeDate(m_nodeDate);
					m_monitorRunResultInfo.setCurData(m_curData);
				if (resultNotifyList != null && resultNotifyList.size() > 0) {
					m_monitorRunResultInfo.setPriority(resultNotifyList.get(0).getPriority());
					m_monitorRunResultInfo.setMessageOrg(makeJobOrgMessage(resultNotifyList.get(0).getMessageOrg(), 
							resultNotifyList.get(0).getMessage()));
				}
			}
			if (sample != null) {
				sampleList.add(sample);
			}
		}
		if(!sampleList.isEmpty()){
			CollectDataUtil.put(sampleList);
		}
	}

	/**
	 * リソース監視の場合はcollectを呼ばず、collectListで処理を行なう
	 */
	@Override
	public boolean collect(String facilityId) {
		throw new UnsupportedOperationException();
	}

	/**
	 * preCollectにて収集済みのデータをもとに、当該監視項目に関するリソース情報を抽出し、後続の通知処理で必要となる情報を返す
	 * @throws FacilityNotFound 
	 */
	public List<MonitorRunResultInfo> collectList() throws FacilityNotFound {
		m_log.debug("collectList() monitorTypeId = " + m_monitorTypeId + ",monitorId = " + m_monitorId  + ", facilityId = " + m_facilityId);

		////
		// ターゲットのItemCodeList/deviceType/deviceListを取得
		////
		boolean breakdownFlg = m_perf.getBreakdownFlg();
		final PollingDataManager dataManager = new PollingDataManager(m_facilityId,m_perf.getItemCode(),breakdownFlg);
		final List<String> itemCodeList = dataManager.getItemCodeList();
		final List<? extends NodeDeviceInfo> deviceList = dataManager.getDeviceList();
		final List<MonitorRunResultInfo> resultList = new ArrayList<>();
		final String facilityName = dataManager.getFacilityName();
		final String platform = dataManager.getPlatformId();
		final String subPlatform = dataManager.getSubPlatformId();

		for(String itemCode : itemCodeList){
			
			// ターゲットのdisplayNameがALLの場合、全てのデバイスに対して処理する
			if(m_perf.getDeviceDisplayName() != null && (PollingDataManager.ALL_DEVICE_NAME).equals(m_perf.getDeviceDisplayName())){
				for (final NodeDeviceInfo deviceInfo : deviceList) {
					m_deviceData = deviceInfo;
					final MonitorRunResultInfo result = calcValue(facilityName, platform, subPlatform, itemCode, deviceInfo.getDeviceName());
					if (result != null) {
						resultList.add(result);
					}
				}
			}
			// 特定のdisplayNameが指定されている場合、デバイスを特定して処理する
			else if(m_perf.getDeviceDisplayName().length() > 0) {
				for(NodeDeviceInfo deviceInfo : deviceList){
					if(m_perf.getDeviceDisplayName().equals(deviceInfo.getDeviceDisplayName())){
						m_deviceData = deviceInfo;
						final MonitorRunResultInfo result = calcValue(facilityName, platform, subPlatform, itemCode, deviceInfo.getDeviceName());
						if (result != null) {
							resultList.add(result);
						}
						break;
					}
				}
			}
			// デバイスがない場合
			else{
				m_deviceData = null;
				final MonitorRunResultInfo result = calcValue(facilityName, platform, subPlatform, itemCode, "");
				if (result != null) {
					resultList.add(result);
				}
			}
		}
		
		return resultList;
	}


	/**
	 * 
	 * @param facilityName
	 * @param platform
	 * @param subPlatform
	 * @param itemCode
	 * @param deviceName
	 * @return 計算したデータを含むMonitorRunResultInfo（但し、Queue内にデータが無く、通知する必要が無い場合はnullが返る）
	 */
	private MonitorRunResultInfo calcValue(String facilityName, String platform, String subPlatform, String itemCode, String deviceName) {
		m_log.debug("calcEachValue() monitorId = " + m_monitorId + ", itemCode = " + itemCode + " , displayName = " + deviceName);

		final MonitorRunResultInfo result = new MonitorRunResultInfo();
		result.setFacilityId(m_facilityId);
		final CollectorItemInfo itemInfo = new CollectorItemInfo(m_monitorId, itemCode, deviceName);
		m_itemName = CollectorItemCodeTable.getFullItemName(itemInfo.getItemCode(), itemInfo.getDisplayName());
		DataTable curTable = null;
		DataTable prvTable = null;
		if (!m_isMonitorJob) {
			curTable = PerfDataQueue.getInstance(m_facilityId).getCurrentData(getMonitorInfo().getRunInterval());
			prvTable = PerfDataQueue.getInstance(m_facilityId).getPrevData(getMonitorInfo().getRunInterval());
		} else {
			curTable = (DataTable)m_curData;
			prvTable = (DataTable)m_prvData;
		}
		
		if (curTable == null || curTable.keySet().size() == 0 || prvTable == null || prvTable.keySet().size() == 0) {
			// DataTalbeを2回分取得できなかった場合にはnullを返す（何も通知しない）
			m_log.info("calcValue() : polling have not done enough count." + facilityName + ", " + itemCode + ", " + deviceName);
			return null;
		}
		boolean ret;
		try {
			m_value = CalculationMethod.getPerformance(platform, subPlatform, itemInfo, deviceName, curTable, prvTable);
			ret = true;
		} catch (CollectedDataNotFoundWithNoPollingException e) {
			// DataTalbeを2回分取得できなかった場合にはnullを返す（何も通知しない）
			m_log.info("calcValue() : previous polling have not done." + facilityName + ", " + itemCode + ", " + deviceName);
			return null;
		} catch (CollectedDataNotFoundException | IllegalStateException | Operator.InvalidValueException e) {
			m_value = Double.NaN;
			m_errorMessage = e.getMessage();
		} catch (Exception e){
			m_log.warn("getPerformance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// 例外発生時はNaNとする
			m_value = Double.NaN;
			m_errorMessage = e.getMessage();
		}
		
		if(!Double.isNaN(m_value)){
			ret = true;
		} else {
			ret = false;
		}
		
		result.setValue(m_value);
		result.setCollectorResult(ret);
		int checkResult = getCheckResult(ret);

		if(m_perf.getItemCode().equals(itemInfo.getItemCode())){
			result.setMonitorFlg(true);
		}else{
			result.setMonitorFlg(false);
		}

		result.setCollectorFlg(true);
		if (m_now != null) {
			result.setNodeDate(m_now.getTime());
		}
		result.setMessage(getMessage(checkResult));
		result.setMessageOrg(getMessageOrg(checkResult));
		result.setPriority(checkResult);
		result.setNotifyGroupId(getNotifyGroupId());
		result.setItemCode(itemInfo.getItemCode());
		result.setItemName(m_itemName);
		result.setDisplayName(itemInfo.getDisplayName());
		return result;
	}

	/**
	 * 当該ノードにおいて、引数で指定した複数の監視間隔タイミングで実行するべき、全ポーリングターゲットを返す
	 * @param intervalSecSet 監視間隔のSet
	 * @return キーが収集方法（SNMPやWBEM）、値がターゲット（SNMPの場合ならOID）のSet
	 * @throws HinemosUnknown 
	 * @throws FacilityNotFound 
	 */
	public Map<String, Set<String>> getPollingTarget(final Set<Integer> intervalSecSet) throws HinemosUnknown, FacilityNotFound {
		final Map<String, Set<String>> returnMap = new HashMap<>();
		Set<MonitorInfo> currentAllMonitor = new HashSet<>();

		if (!m_isMonitorJob) {
			final NodeToMonitorCache node2monitor = NodeToMonitorCache.getInstance(HinemosModuleConstant.MONITOR_PERFORMANCE);
			final Map<Integer, Set<MonitorInfo>> currentMonitors = node2monitor.getMonitorsWithCalendar(m_facilityId, intervalSecSet);
			for (final Set<MonitorInfo> monitors : currentMonitors.values()) {
				currentAllMonitor.addAll(monitors);
			}
		} else {
			currentAllMonitor.add(m_monitor);
		}
		
		for (final MonitorInfo monitor : currentAllMonitor) {
			final PerfCheckInfo perfCheckInfo = monitor.getPerfCheckInfo();
			final PollingDataManager dataManager = new PollingDataManager(m_facilityId, perfCheckInfo.getItemCode(), perfCheckInfo.getBreakdownFlg());
			
			// 返却オブジェクトに、収集メソッドとターゲット情報を追加する
			final String collectMethod = dataManager.getCollectMethod();
			Set<String> target = returnMap.get(collectMethod);
			if (target == null) {
				target = new HashSet<String>();
				returnMap.put(collectMethod, target);
			}
			for (String pollingTarget : dataManager.getPollingTargets()) {
				if (pollingTarget != null && !pollingTarget.equals("")) {
					target.add(pollingTarget);
				}
			}
		}
		return returnMap;
	}

	private static void notify(MonitorInfo monitorInfo, String facilityId, List<MonitorRunResultInfo> notifyResultList) {
		if(monitorInfo.getMonitorFlg().booleanValue() == false){
			return;
		}
		// 通知IDが指定されていない場合、通知しない
		List<NotifyRelationInfo> notifyRelationList = monitorInfo.getNotifyRelationList();
		if(notifyRelationList == null || notifyRelationList.size() == 0){
			return;
		}

		ArrayList<OutputBasicInfo> outputBasicInfoList = new ArrayList<OutputBasicInfo>(notifyResultList.size());
		for (MonitorRunResultInfo resultInfo : notifyResultList) {
			// 通知情報を設定
			OutputBasicInfo outputBasicInfo = new OutputBasicInfo();
			outputBasicInfo.setPluginId(monitorInfo.getMonitorTypeId());
			outputBasicInfo.setMonitorId(monitorInfo.getMonitorId());
			outputBasicInfo.setApplication(monitorInfo.getApplication());

			// 通知抑制用のサブキーを設定。
			if(resultInfo.getDisplayName() != null && !"".equals(resultInfo.getDisplayName())){
				// 監視結果にデバイス名を含むものは、デバイス名をサブキーとして設定。
				outputBasicInfo.setSubKey(resultInfo.getDisplayName());
			}

			outputBasicInfo.setFacilityId(facilityId);
			outputBasicInfo.setScopeText(monitorInfo.getScope());
			outputBasicInfo.setPriority(resultInfo.getPriority());
			outputBasicInfo.setMessage(resultInfo.getMessage());
			outputBasicInfo.setMessageOrg(resultInfo.getMessageOrg());
			outputBasicInfo.setGenerationDate(resultInfo.getNodeDate());

			outputBasicInfoList.add(outputBasicInfo);
		}
		new NotifyControllerBean().notify(outputBasicInfoList, monitorInfo.getNotifyGroupId());
	}

	/**
	 *
	 */
	@Override
	protected void notify(
			boolean isNode, String facilityId,
			int result,
			Date generationDate,
			MonitorRunResultInfo resultInfo) throws HinemosUnknown {

		// for debug
		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() isNode = " + isNode + ", facilityId = " + facilityId
					+ ", result = " + result + ", generationDate = " + generationDate.toString()
					+ ", resultInfo = " + resultInfo.getMessage());
		}

		// 監視無効の場合、通知しない
		if(!m_monitor.getMonitorFlg()){
			m_log.debug("notify() isNode = " + isNode + ", facilityId = " + facilityId
					+ ", result = " + result + ", generationDate = " + generationDate.toString()
					+ ", resultInfo = " + resultInfo.getMessage()
					+ ", monitorFlg is false");
			return;
		}

		// 通知IDが指定されていない場合、通知しない
		String notifyGroupId = resultInfo.getNotifyGroupId();
		if(notifyGroupId == null || "".equals(notifyGroupId)){
			return;
		}

		// 通知情報を設定
		OutputBasicInfo notifyInfo = new OutputBasicInfo();
		notifyInfo.setPluginId(m_monitorTypeId);
		notifyInfo.setMonitorId(m_monitorId);
		notifyInfo.setApplication(m_monitor.getApplication());

		// 通知抑制用のサブキーを設定。
		if(resultInfo.getDisplayName() != null && !"".equals(resultInfo.getDisplayName())){
			// 監視結果にデバイス名を含むものは、デバイス名をサブキーとして設定。
			notifyInfo.setSubKey(resultInfo.getDisplayName());
		}

		String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
		notifyInfo.setFacilityId(facilityId);
		notifyInfo.setScopeText(facilityPath);

		int priority = resultInfo.getPriority();
		String message = resultInfo.getMessage();
		String messageOrg = resultInfo.getMessageOrg();

		notifyInfo.setPriority(priority);
		notifyInfo.setMessage(message);
		notifyInfo.setMessageOrg(messageOrg);

		if(generationDate == null){
			notifyInfo.setGenerationDate(null);
		}
		else{
			notifyInfo.setGenerationDate(generationDate.getTime());
		}

		// for debug
		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() priority = " + priority
					+ " , message = " + message
					+ " , messageOrg = " + messageOrg
					+ ", generationDate = " + generationDate);
		}

		// ログ出力情報を送信
		if (m_log.isDebugEnabled()) {
			m_log.debug("sending message"
					+ " : priority=" + notifyInfo.getPriority()
					+ " generationDate=" + notifyInfo.getGenerationDate() + " pluginId=" + notifyInfo.getPluginId()
					+ " monitorId=" + notifyInfo.getMonitorId() + " facilityId=" + notifyInfo.getFacilityId()
					+ " subKey=" + notifyInfo.getSubKey()
					+ ")");
		}
		new NotifyControllerBean().notify(notifyInfo, notifyGroupId);
	}


	/**
	 * リソース監視情報を設定します。
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {
		// 性能管理閾値監視情報を取得
		if (!m_isMonitorJob) {
			m_perf = QueryUtil.getMonitorPerfInfoPK(m_monitorId);
		} else {
			m_perf = QueryUtil.getMonitorPerfInfoPK(m_monitor.getMonitorId());
		}
	}

	/**
	 * メッセージを取得します。
	 */
	@Override
	public String getMessage(int result) {
		String valueString;
		if(Double.isNaN(m_value)){
			valueString = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_PERFORMANCE.getMessage();
		} else {
			valueString = NumberFormat.getNumberInstance().format(m_value);
		}
		return m_itemName + " : " + valueString;
	}

	/**
	 * メッセージを取得します。
	 */
	@Override
	protected String getMessageForScope(int result){
		String valueString;
		if(Double.isNaN(m_value)){
			valueString = MessageConstant.TIME_OUT.getMessage();
		} else {
			valueString = NumberFormat.getNumberInstance().format(m_value);
		}
		return m_itemName + " : " + valueString;
	}

	/**
	 * オリジナルメッセージを取得します。
	 */
	@Override
	public String getMessageOrg(int result) {
		String valueString;
		if(Double.isNaN(m_value)){
			valueString = "NaN";
			
			if (m_errorMessage != null) {
				valueString = valueString + "\n" + m_errorMessage;
			}
		} else {
			valueString = NumberFormat.getNumberInstance().format(m_value);

			m_log.debug("RunMonitorPerf messageOrg : " + valueString);

			// デバイス情報を付加
			if(m_deviceData != null) {

				valueString = valueString + "\n" +
						MessageConstant.DEVICE_NAME.getMessage() + " : " + m_deviceData.getDeviceName() + "\n" +
						MessageConstant.DEVICE_INDEX.getMessage() + " : " + m_deviceData.getDeviceIndex();

				m_log.debug("RunMonitorPerf add DeviceInfo : " + valueString);
			}
		}
		return m_itemName + " : " + valueString;
	}

	/**
	 * オリジナルメッセージを取得します。
	 */
	@Override
	protected String getMessageOrgForScope(int result){
		String valueString;
		if(Double.isNaN(m_value)){
			valueString = "NaN";
		} else {
			valueString = NumberFormat.getNumberInstance().format(m_value);
		}
		return m_itemName + " : " + valueString;
	}


	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		String[] args = {m_itemName};
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_PERFORMANCE.getMessage(args)
				+ "\n" + msg;
	}

//	@Override
//	protected boolean setMonitorInfo(String monitorTypeId, String monitorId) throws MonitorNotFound, HinemosUnknown{
//		boolean ret = super.setMonitorInfo(monitorTypeId, monitorId);
//
//		// 次回が稼働日の場合はスケジュールを再作成する
//		if(!m_isInCalendarTerm && m_isInNextCalendarTerm){
//			try{
//				RepositoryControllerBean repository = new RepositoryControllerBean();
//				if(m_perf == null){
//					setCheckInfo();
//				}
//
//				ModifyPollingSchedule schedule = new ModifyPollingSchedule();
//				if(repository.isNode(m_facilityId)){
//					// ノードの場合
//					m_log.info("pre-schedule : monitorId = " + m_monitorId + ", facilityId = " + m_facilityId);
//					schedule.addNodeSchedule(m_facilityId, m_monitorId, m_monitor.getMonitorTypeId(), m_monitor.getRunInterval(), m_perf);
//				}
//				else{
//					// スコープの場合
//					ArrayList<String> facilityList = repository.getExecTargetFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId());
//					if (facilityList.size() == 0) {
//						m_log.info("pre-schedule : monitorId = " + m_monitorId + ", facilityId is null");
//						return true;
//					}
//
//					for (String facilityId : facilityList) {
//						m_log.info("pre-schedule : monitorId = " + m_monitorId + ", facilityId = " + facilityId);
//						schedule.addNodeSchedule(facilityId, m_monitorId, m_monitor.getMonitorTypeId(), m_monitor.getRunInterval(), m_perf);
//					}
//				}
//
//			} catch (FacilityNotFound e) {
//				m_log.warn("setMonitorInfo() : fail to addSchedule . m_monitorId = " + m_monitorId, e);
//			} catch (InvalidRole e) {
//				m_log.warn("setMonitorInfo() : fail to addSchedule . m_monitorId = " + m_monitorId, e);
//			}
//		}
//
//		return ret;
//	}

}
