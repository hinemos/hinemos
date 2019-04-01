/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.session;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.analytics.factory.RunMonitorCorrelation;
import com.clustercontrol.analytics.factory.RunMonitorIntegration;
import com.clustercontrol.analytics.factory.RunMonitorLogcount;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.ObjectSharingService;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hinemosagent.factory.RunMonitorAgent;
import com.clustercontrol.http.factory.RunMonitorHttp;
import com.clustercontrol.http.factory.RunMonitorHttpScenario;
import com.clustercontrol.http.factory.RunMonitorHttpString;
import com.clustercontrol.jmx.factory.RunMonitorJmx;
import com.clustercontrol.monitor.plugin.factory.RunMonitorPluginSample;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.performance.monitor.factory.RunMonitorPerformance;
import com.clustercontrol.ping.factory.RunMonitorPing;
import com.clustercontrol.port.factory.RunMonitorPort;
import com.clustercontrol.process.factory.RunMonitorProcess;
import com.clustercontrol.snmp.factory.RunMonitorSnmp;
import com.clustercontrol.snmp.factory.RunMonitorSnmpString;
import com.clustercontrol.sql.factory.RunMonitorSql;
import com.clustercontrol.sql.factory.RunMonitorSqlString;
import com.clustercontrol.winservice.factory.RunMonitorWinService;

/**
 * Quartzから呼び出して監視を実行する
 */
public class MonitorRunManagementBean {

	// Logger
	private static Log m_log = LogFactory.getLog( MonitorRunManagementBean.class );

	/**
	 * Quartzからのコールバックメソッド
	 * 
	 * 監視項目ID単位で複数のノードにポーリングを行うタイプの監視
	 * 
	 * @param monitorTypeId 監視監視対象ID
	 * @param monitorId 監視項目ID
	 * @param monitorType 監視判定タイプ
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 * @since 4.0.0
	 */
	public void runMonitorAggregatedMonitorId(String monitorTypeId, String monitorId, Integer monitorType) throws FacilityNotFound, MonitorNotFound, HinemosUnknown {
		Long starttime = 0L;
		if (m_log.isDebugEnabled()) {
			starttime = new Date().getTime();
		}
		m_log.debug("runMonitorAggregatedMonitorId() monitorTypeId = " + monitorTypeId + ", monitorId = " + monitorId + ", monitorType = " + monitorType);

		JpaTransactionManager jtm = null;

		// null チェック
		if(monitorId == null || "".equals(monitorId)){
			HinemosUnknown e = new HinemosUnknown("monitorId is null or empty.");
			m_log.warn("runMonitorAggregatedMonitorId() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;
		}
		if(monitorTypeId == null || "".equals(monitorTypeId)){
			HinemosUnknown e = new HinemosUnknown("monitorTypeId is null or empty.");
			m_log.warn("runMonitorAggregatedMonitorId() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			throw e;
		}

		RunMonitor runMonitor = null;
		try {
			
			switch (monitorTypeId) {
			case HinemosModuleConstant.MONITOR_AGENT:
				runMonitor = new RunMonitorAgent();
				break;
				
			case HinemosModuleConstant.MONITOR_HTTP_N:
			case HinemosModuleConstant.MONITOR_HTTP_S:
			case HinemosModuleConstant.MONITOR_HTTP_SCENARIO:
				if (monitorType != null) {
					switch (monitorType) {
					case MonitorTypeConstant.TYPE_NUMERIC:
						runMonitor = new RunMonitorHttp();
						break;
					case MonitorTypeConstant.TYPE_STRING:
						runMonitor = new RunMonitorHttpString();
						break;
					case MonitorTypeConstant.TYPE_SCENARIO:
						runMonitor = new RunMonitorHttpScenario();
						break;
					default:
						break;
					}
				}
				if (runMonitor == null) {
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.warn("runMonitorAggregatedMonitorId() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw e;
				}
				break;
				
			case HinemosModuleConstant.MONITOR_PING:
				runMonitor = new RunMonitorPing();
				break;
				
			case HinemosModuleConstant.MONITOR_PORT:
				runMonitor = new RunMonitorPort();
				break;
				
			case HinemosModuleConstant.MONITOR_SNMP_N:
			case HinemosModuleConstant.MONITOR_SNMP_S:
				if (monitorType != null) {
					switch (monitorType) {
					case MonitorTypeConstant.TYPE_NUMERIC:
						runMonitor = new RunMonitorSnmp();
						break;
					case MonitorTypeConstant.TYPE_STRING:
						runMonitor = new RunMonitorSnmpString();
						break;
					default:
						break;
					}
				}
				if (runMonitor == null) {
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.warn("runMonitorAggregatedMonitorId() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw e;
				}
				break;
				
			case HinemosModuleConstant.MONITOR_SQL_N:
			case HinemosModuleConstant.MONITOR_SQL_S:
				if (monitorType != null) {
					switch (monitorType) {
					case MonitorTypeConstant.TYPE_NUMERIC:
						runMonitor = new RunMonitorSql();
						break;
					case MonitorTypeConstant.TYPE_STRING:
						runMonitor = new RunMonitorSqlString();
						break;
					default:
						break;
					}
				}
				if (runMonitor == null) {
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.warn("runMonitorAggregatedMonitorId() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw e;
				}
				break;
				
			case HinemosModuleConstant.MONITOR_WINSERVICE:
				runMonitor = new RunMonitorWinService();
				break;
				
			case HinemosModuleConstant.MONITOR_JMX:
				runMonitor = new RunMonitorJmx();
				break;
				
			case HinemosModuleConstant.MONITOR_LOGCOUNT:
				runMonitor = new RunMonitorLogcount();
				break;
				
			case HinemosModuleConstant.MONITOR_CORRELATION:
				runMonitor = new RunMonitorCorrelation();
				break;
				
			case HinemosModuleConstant.MONITOR_INTEGRATION:
				runMonitor = new RunMonitorIntegration();
				break;
				
			case HinemosModuleConstant.MONITOR_LOGFILE:
			case HinemosModuleConstant.MONITOR_BINARYFILE_BIN:
			case HinemosModuleConstant.MONITOR_PCAP_BIN:
			case HinemosModuleConstant.MONITOR_PROCESS:
			case HinemosModuleConstant.MONITOR_PERFORMANCE:
			case HinemosModuleConstant.MONITOR_SNMPTRAP:
			case HinemosModuleConstant.MONITOR_SYSTEMLOG:
			case HinemosModuleConstant.MONITOR_CUSTOM_N:
			case HinemosModuleConstant.MONITOR_CUSTOM_S:
			case HinemosModuleConstant.MONITOR_WINEVENT:
			case HinemosModuleConstant.MONITOR_CUSTOMTRAP_N:
			case HinemosModuleConstant.MONITOR_CUSTOMTRAP_S:
				// 本来呼び出すべきではない
				break;
				
			default:
				try {
					runMonitor = ObjectSharingService.objectRegistry().get(RunMonitor.class, monitorTypeId + "." + monitorType);
				} catch (Exception e) {
					m_log.warn("runMonitorAggregatedMonitorId() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw e;
				}

				if(runMonitor == null){
					//監視のサンプル実装
					runMonitor = new RunMonitorPluginSample();

					/*
					HinemosUnknown e = new HinemosUnknown("Unknown monitorTypeId = " + monitorTypeId + ", monitorType = " + monitorType);
					m_log.warn("runMonitorAggregatedMonitorId() : " + e.getClass().getSimpleName() +
							", " + e.getMessage(), e);
					throw e;
					*/
				}
			}
		} catch (Exception e) {
			m_log.warn("runMonitorAggregatedMonitorId() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		List<OutputBasicInfo> notifyInfoList = null;
		try{
			// トランザクションがすでに開始されている場合は処理終了
			jtm = new JpaTransactionManager();
			jtm.begin(true);

			if (runMonitor != null) {
				notifyInfoList = runMonitor.runMonitor(monitorTypeId, monitorId);
			} else {
				throw new NullPointerException("runMonitor is null");
			}

			// 通知設定
			jtm.addCallback(new NotifyCallback(notifyInfoList));

			jtm.commit();
		}catch(FacilityNotFound | MonitorNotFound | HinemosUnknown e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		}catch(Exception e){
			m_log.warn("runMonitorAggregatedMonitorId() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		if (m_log.isDebugEnabled()) {
			Long endtime = new Date().getTime();
			m_log.debug("runMonitorAggregatedMonitorId() total time=" + (endtime - starttime) + "ms");
		}
	}
	
	/**
	 * Quartzからのコールバックメソッド
	 * 
	 * ノード単位で集約してデータを取得したほうが効率的に監視できるタイプの監視をキックする。
	 * （リソース監視・プロセス監視）
	 * 
	 * @param monitorTypeId 監視監視対象ID
	 * @param facilityId 監視対象ノード（FacilityId）
	 * @param monitorType 監視判定タイプ
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 * @since 5.1.0
	 */
	public void runMonitorAggregatedFacilityId(String monitorTypeId, String facilityId, Integer monitorType) throws FacilityNotFound, MonitorNotFound, HinemosUnknown {
		Long starttime = 0L;
		if (m_log.isDebugEnabled()) {
			starttime = new Date().getTime();
		}
		m_log.debug("runMonitorAggregatedFacilityId() monitorTypeId = " + monitorTypeId + ", facilityId = " + facilityId + ", monitorType = " + monitorType);

		JpaTransactionManager jtm = null;

		// null チェック
		if(facilityId == null || facilityId.length() == 0){
			HinemosUnknown e = new HinemosUnknown("facilityId is null or empty.");
			m_log.warn("runMonitorAggregatedFacilityId() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;

		}
		if(monitorTypeId == null || monitorTypeId.length() == 0) {
			HinemosUnknown e = new HinemosUnknown("monitorTypeId is null or empty.");
			m_log.warn("runMonitorAggregatedFacilityId() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;
		}

		RunMonitor runMonitor = null;
		try {
			
			switch (monitorTypeId) {
			case HinemosModuleConstant.MONITOR_PROCESS:
				runMonitor = new RunMonitorProcess();
				break;
				
			case HinemosModuleConstant.MONITOR_PERFORMANCE:
				runMonitor = new RunMonitorPerformance();
				break;
				
			case HinemosModuleConstant.MONITOR_AGENT:
			case HinemosModuleConstant.MONITOR_HTTP_N:
			case HinemosModuleConstant.MONITOR_HTTP_S:
			case HinemosModuleConstant.MONITOR_HTTP_SCENARIO:
			case HinemosModuleConstant.MONITOR_PING:
			case HinemosModuleConstant.MONITOR_PORT:
			case HinemosModuleConstant.MONITOR_SNMP_N:
			case HinemosModuleConstant.MONITOR_SNMP_S:
			case HinemosModuleConstant.MONITOR_SQL_N:
			case HinemosModuleConstant.MONITOR_SQL_S:
			case HinemosModuleConstant.MONITOR_WINSERVICE:
			case HinemosModuleConstant.MONITOR_JMX:
			case HinemosModuleConstant.MONITOR_LOGFILE:
			case HinemosModuleConstant.MONITOR_BINARYFILE_BIN:
			case HinemosModuleConstant.MONITOR_PCAP_BIN:
			case HinemosModuleConstant.MONITOR_LOGCOUNT:
			case HinemosModuleConstant.MONITOR_SNMPTRAP:
			case HinemosModuleConstant.MONITOR_SYSTEMLOG:
			case HinemosModuleConstant.MONITOR_CUSTOM_N:
			case HinemosModuleConstant.MONITOR_CUSTOM_S:
			case HinemosModuleConstant.MONITOR_WINEVENT:
			case HinemosModuleConstant.MONITOR_CORRELATION:
				// 本来呼び出すべきではない
				break;
				
			default:
				try {
					runMonitor = ObjectSharingService.objectRegistry().get(RunMonitor.class, monitorTypeId + "." + monitorType);
				} catch (Exception e) {
					m_log.warn("runMonitorAggregatedFacilityId() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw e;
				}

				if(runMonitor == null){
					//監視のサンプル実装
					runMonitor = new RunMonitorPluginSample();

					/*
					HinemosUnknown e = new HinemosUnknown("Unknown monitorTypeId = " + monitorTypeId + ", monitorType = " + monitorType);
					m_log.warn("runMonitorAggregatedFacilityId() : " + e.getClass().getSimpleName() +
							", " + e.getMessage(), e);
					throw e;
					*/
				}
			}
		} catch (Exception e) {
			m_log.warn("runMonitorAggregatedFacilityId() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		List<OutputBasicInfo> notifyInfoList = null;
		try{
			// トランザクションがすでに開始されている場合は処理終了
			jtm = new JpaTransactionManager();
			jtm.begin(true);

			if (runMonitor != null) {
				notifyInfoList = runMonitor.runMonitorAggregateByNode(monitorTypeId, facilityId);
			} else {
				throw new NullPointerException("runMonitor is null");
			}

			// 通知設定
			jtm.addCallback(new NotifyCallback(notifyInfoList));

			jtm.commit();
		}catch(FacilityNotFound | MonitorNotFound | HinemosUnknown e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		}catch(Exception e){
			m_log.warn("runMonitorAggregatedFacilityId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		if (m_log.isDebugEnabled()) {
			Long endtime = new Date().getTime();
			m_log.debug("runMonitorAggregatedFacilityId() total time=" + (endtime - starttime) + "ms");
		}
	}
}
