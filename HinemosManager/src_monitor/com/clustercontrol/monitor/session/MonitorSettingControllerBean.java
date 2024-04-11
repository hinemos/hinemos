/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.analytics.factory.ModifyMonitorCorrelation;
import com.clustercontrol.analytics.factory.ModifyMonitorIntegration;
import com.clustercontrol.analytics.factory.ModifyMonitorLogcount;
import com.clustercontrol.analytics.factory.SelectMonitorIntegration;
import com.clustercontrol.analytics.util.SummaryLogcountWorker;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.binary.factory.ModifyMonitorBinaryFile;
import com.clustercontrol.binary.factory.ModifyMonitorPacketCapture;
import com.clustercontrol.commons.scheduler.TriggerSchedulerException;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.custom.factory.ModifyCustom;
import com.clustercontrol.custom.factory.ModifyCustomString;
import com.clustercontrol.custom.factory.SelectCustom;
import com.clustercontrol.customtrap.factory.ModifyCustomTrap;
import com.clustercontrol.customtrap.factory.ModifyCustomTrapString;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.hinemosagent.factory.ModifyMonitorAgent;
import com.clustercontrol.http.factory.ModifyMonitorHttp;
import com.clustercontrol.http.factory.ModifyMonitorHttpScenario;
import com.clustercontrol.http.factory.ModifyMonitorHttpString;
import com.clustercontrol.hub.bean.CollectStringTag;
import com.clustercontrol.hub.model.LogFormat;
import com.clustercontrol.hub.model.LogFormatKey;
import com.clustercontrol.jmx.factory.ModifyMonitorJmx;
import com.clustercontrol.logfile.factory.ModifyMonitorLogfileString;
import com.clustercontrol.monitor.bean.MonitorFilterInfo;
import com.clustercontrol.monitor.plugin.factory.ModifyMonitorPluginNumeric;
import com.clustercontrol.monitor.plugin.factory.ModifyMonitorPluginString;
import com.clustercontrol.monitor.plugin.factory.ModifyMonitorPluginTruth;
import com.clustercontrol.monitor.run.bean.MonitorInfoBean;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil;
import com.clustercontrol.monitor.run.util.MonitorChangedNotificationCallback;
import com.clustercontrol.monitor.run.util.MonitorValidator;
import com.clustercontrol.monitor.run.util.NodeToMonitorCacheChangeCallback;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.notify.util.NotifyRelationCacheRefreshCallback;
import com.clustercontrol.performance.monitor.factory.ModifyMonitorPerformance;
import com.clustercontrol.ping.factory.ModifyMonitorPing;
import com.clustercontrol.port.factory.ModifyMonitorPort;
import com.clustercontrol.process.factory.ModifyMonitorProcess;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rpa.monitor.factory.ModifyMonitorRpaLogfile;
import com.clustercontrol.rpa.monitor.factory.ModifyMonitorRpaManagementToolService;
import com.clustercontrol.sdml.util.SdmlUtil;
import com.clustercontrol.snmp.factory.ModifyMonitorSnmp;
import com.clustercontrol.snmp.factory.ModifyMonitorSnmpString;
import com.clustercontrol.snmptrap.factory.ModifyMonitorTrap;
import com.clustercontrol.sql.factory.ModifyMonitorSql;
import com.clustercontrol.sql.factory.ModifyMonitorSqlString;
import com.clustercontrol.systemlog.factory.ModifyMonitorSystemlogString;
import com.clustercontrol.systemlog.util.SystemlogCache;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.winevent.factory.ModifyMonitorWinEvent;
import com.clustercontrol.winservice.factory.ModifyMonitorWinService;
import com.clustercontrol.xcloud.factory.monitors.ModifyMonitorCloudLog;

import jakarta.persistence.PersistenceException;

/**
 * 監視設定を制御するSesison Bean<BR>
 *
 */
public class MonitorSettingControllerBean {

	// Logger
	private static Log m_log = LogFactory.getLog( MonitorSettingControllerBean.class );

	/**
	 * 監視設定情報をマネージャに登録します。<BR>
	 *
	 * @param info 登録情報
	 * @return 監視設定情報
	 * @throws MonitorIdInvalid 数値監視の場合のみ
	 * @throws MonitorDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public MonitorInfo addMonitor(MonitorInfo info)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidSetting, InvalidRole, HinemosUnknown{
		return addMonitor(info, false);
	}

	/**
	 * 監視設定情報をマネージャに登録します。<BR>
	 *
	 * @param info 登録情報
	 * @param isImport true:設定インポートエクスポートから実行、false:それ以外
	 * @return 監視設定情報
	 * @throws MonitorIdInvalid 数値監視の場合のみ
	 * @throws MonitorDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public MonitorInfo addMonitor(MonitorInfo info, boolean isImport)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidSetting, InvalidRole, HinemosUnknown{
		m_log.debug("addMonitor()");

		JpaTransactionManager jtm = null;
		boolean flag = false;
		MonitorInfo ret = null;

		try {
			jtm = new JpaTransactionManager();

			//入力チェック
			try{
				// 将来予測・変化量のアプリケーションが設定されていない場合は、監視のアプリケーションを設定
				if (info.getPredictionApplication() == null || info.getPredictionApplication().isEmpty()) {
					info.setPredictionApplication(info.getApplication());
				}
				if (info.getChangeApplication() == null || info.getChangeApplication().isEmpty()) {
					info.setChangeApplication(info.getApplication());
				}
				MonitorValidator.validateMonitorInfo(info);
				
				//ユーザがオーナーロールIDに所属しているかチェック
				RoleValidator.validateUserBelongRole(info.getOwnerRoleId(),
						(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
						(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));
			} catch (InvalidRole | InvalidSetting e) {
				throw e;
			} catch (Exception e) {
				m_log.warn("addMonitor() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}

			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

			ModifyMonitor addMonitor = null;
			String monitorTypeId = info.getMonitorTypeId();
			int monitorType = info.getMonitorType();
			m_log.debug("addMonitor() monitorTypeId = " + monitorTypeId + ", monitorType = " + monitorType);

			// Hinemos エージェント監視
			if(HinemosModuleConstant.MONITOR_AGENT.equals(monitorTypeId)){
				addMonitor = new ModifyMonitorAgent();
			}
			// HTTP 監視
			else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_HTTP_S.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					addMonitor = new ModifyMonitorHttp();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					addMonitor = new ModifyMonitorHttpString();
				}
				// シナリオ
				else if(MonitorTypeConstant.TYPE_SCENARIO == monitorType){
					addMonitor = new ModifyMonitorHttpScenario();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("addMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// ログファイル 監視
			else if(HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorTypeId)){
				addMonitor =  new ModifyMonitorLogfileString();
			}
			// リソース 監視
			else if(HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)){
				addMonitor = new ModifyMonitorPerformance();
			}
			// ping監視
			else if(HinemosModuleConstant.MONITOR_PING.equals(monitorTypeId)){
				addMonitor = new ModifyMonitorPing();
			}
			// ポート監視
			else if(HinemosModuleConstant.MONITOR_PORT.equals(monitorTypeId)){
				addMonitor = new ModifyMonitorPort();
			}
			// プロセス監視
			else if(HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)){
				addMonitor = new ModifyMonitorProcess();
			}
			// SNMPTRAP監視
			else if(HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorTypeId)){
				addMonitor = new ModifyMonitorTrap();
			}
			// SNMP監視
			else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_SNMP_S.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					addMonitor = new ModifyMonitorSnmp();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					addMonitor = new ModifyMonitorSnmpString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("addMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// システムログ監視
			else if(HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)){
				addMonitor = new ModifyMonitorSystemlogString();
			}
			// SQL監視
			else if (HinemosModuleConstant.MONITOR_SQL_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_SQL_S.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					addMonitor = new ModifyMonitorSql();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					addMonitor = new ModifyMonitorSqlString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("addMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// カスタム監視
			else if(HinemosModuleConstant.MONITOR_CUSTOM_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_CUSTOM_S.equals(monitorTypeId)){
				// 数値監視
				if (MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					addMonitor = new ModifyCustom();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){	
					addMonitor = new ModifyCustomString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("addMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// Windowsサービス監視
			else if(HinemosModuleConstant.MONITOR_WINSERVICE.equals(monitorTypeId)){
				addMonitor = new ModifyMonitorWinService();
			}
			// Windowsイベント監視
			else if(HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorTypeId)){
				addMonitor = new ModifyMonitorWinEvent();
			}
			// JMX 監視
			else if(HinemosModuleConstant.MONITOR_JMX.equals(monitorTypeId)){
				addMonitor = new ModifyMonitorJmx();
			}
			// カスタムトラップ監視
			else if(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_CUSTOMTRAP_S.equals(monitorTypeId)){
				// 数値監視
				if (MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					addMonitor = new ModifyCustomTrap();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){	
					addMonitor = new ModifyCustomTrapString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("addMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// ログ件数 監視
			else if(HinemosModuleConstant.MONITOR_LOGCOUNT.equals(monitorTypeId)){
				addMonitor = new ModifyMonitorLogcount();
			}
			// 相関係数監視
			else if(HinemosModuleConstant.MONITOR_CORRELATION.equals(monitorTypeId)){
				addMonitor = new ModifyMonitorCorrelation();
			}
			// 収集値統合監視
			else if(HinemosModuleConstant.MONITOR_INTEGRATION.equals(monitorTypeId)){
				addMonitor = new ModifyMonitorIntegration();
			}
			// バイナリファイル監視
			else if (HinemosModuleConstant.MONITOR_BINARYFILE_BIN.equals(monitorTypeId)) {
				addMonitor = new ModifyMonitorBinaryFile();
			}
			// パケットキャプチャ監視
			else if (HinemosModuleConstant.MONITOR_PCAP_BIN.equals(monitorTypeId)) {
				addMonitor = new ModifyMonitorPacketCapture();
			}
			// RPAログファイル監視
			else if (HinemosModuleConstant.MONITOR_RPA_LOGFILE.equals(monitorTypeId)) {
				addMonitor = new ModifyMonitorRpaLogfile();
			}
			else if (HinemosModuleConstant.MONITOR_RPA_MGMT_TOOL_SERVICE.equals(monitorTypeId)) {
				addMonitor = new ModifyMonitorRpaManagementToolService();
			}
			// クラウドログ監視
			else if (HinemosModuleConstant.MONITOR_CLOUD_LOG.equals(monitorTypeId)) {
				addMonitor = new ModifyMonitorCloudLog();
			}
			// Other(Pluginで追加する汎用的な監視)
			else{

				// 数値
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					addMonitor = new ModifyMonitorPluginNumeric();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					addMonitor = new ModifyMonitorPluginString();
				}
				// 真偽値
				else if(MonitorTypeConstant.TYPE_TRUTH == monitorType){
					addMonitor = new ModifyMonitorPluginTruth();
				}

				if(addMonitor == null){
					HinemosUnknown e = new HinemosUnknown("Unknown monitorTypeId = " + monitorTypeId + ", monitorType = " + monitorType);
					m_log.info("addMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			try {
				jtm.begin();

				flag = addMonitor.add(info, loginUser);

				jtm.addCallback(new MonitorChangedNotificationCallback(monitorTypeId,flag));
				// コールバックメソッド設定
				if (!isImport) {
					addImportMonitorCallback(jtm);
				}
				if (HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)
						|| HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)) {
					jtm.addCallback(new NodeToMonitorCacheChangeCallback(monitorTypeId));
				}
				jtm.commit();
			} catch (MonitorIdInvalid | MonitorDuplicate | HinemosUnknown | InvalidRole e) {
				jtm.rollback();
				throw e;
			} catch (MonitorNotFound | TriggerSchedulerException e) {
				m_log.info("addMonitor " + e.getClass().getName() + ", " + e.getMessage());
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} catch (ObjectPrivilege_InvalidRole e) {
				jtm.rollback();
				throw new InvalidRole(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("addMonitor() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
			if (flag) {
				try {
					// 実行結果取得
					ret = getMonitor(info.getMonitorId());
				} catch (Exception e) {
					// トランザクションが本メソッドで完結していない場合は例外発生の可能性があるが、本処理上問題なし
					m_log.debug("addMonitor get monitor failure: "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
			}
		}
		return ret;
	}

	/**
	 * 監視設定情報を更新します。<BR>
	 *
	 * @param info 登録情報
	 * @return 監視設定情報
	 * @throws MonitorNotFound
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public MonitorInfo modifyMonitor(MonitorInfo info)
			throws MonitorNotFound, InvalidSetting, InvalidRole, HinemosUnknown {
		return modifyMonitor(info, false);
	}

	/**
	 * 監視設定情報を更新します。<BR>
	 *
	 * @param info 登録情報
	 * @param isImport true:設定インポートエクスポートから実行、false:それ以外
	 * @return 監視設定情報
	 * @throws MonitorNotFound
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public MonitorInfo modifyMonitor(MonitorInfo info, boolean isImport)
			throws MonitorNotFound, InvalidSetting, InvalidRole, HinemosUnknown {
		m_log.debug("modifyMonitor()");

		JpaTransactionManager jtm = null;
		boolean flag = false;
		MonitorInfo ret = null;

		try {
			jtm = new JpaTransactionManager();

			//入力チェック
			try{
				// 将来予測・変化量のアプリケーションが設定されていない場合は、監視のアプリケーションを設定
				if (info.getPredictionApplication() == null || info.getPredictionApplication().isEmpty()) {
					info.setPredictionApplication(info.getApplication());
				}
				if (info.getChangeApplication() == null || info.getChangeApplication().isEmpty()) {
					info.setChangeApplication(info.getApplication());
				}
				// オーナーロールID取得
				MonitorInfo entity = getMonitor(info.getMonitorId());
				info.setOwnerRoleId(entity.getOwnerRoleId());
				MonitorValidator.validateMonitorInfo(info);
			} catch (InvalidSetting | InvalidRole | MonitorNotFound e) { 
				throw e;
			} catch (Exception e) {
				m_log.warn("modifyMonitor() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}

			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

			ModifyMonitor modMonitor = null;
			String monitorTypeId = info.getMonitorTypeId();
			int monitorType = info.getMonitorType();
			m_log.debug("modifyMonitor() monitorTypeId = " + monitorTypeId + ", monitorType = " + monitorType);


			// Hinemos エージェント監視
			if(HinemosModuleConstant.MONITOR_AGENT.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorAgent();
			}
			// HTTP 監視
			else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_HTTP_S.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					modMonitor = new ModifyMonitorHttp();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					modMonitor = new ModifyMonitorHttpString();
				}
				// シナリオ
				else if(MonitorTypeConstant.TYPE_SCENARIO == monitorType){
					modMonitor = new ModifyMonitorHttpScenario();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("modifyMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// ログファイル 監視
			else if(HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorTypeId)){
				modMonitor =  new ModifyMonitorLogfileString();
			}
			// リソース 監視
			else if(HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorPerformance();
			}
			// ping監視
			else if(HinemosModuleConstant.MONITOR_PING.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorPing();
			}
			// ポート監視
			else if(HinemosModuleConstant.MONITOR_PORT.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorPort();
			}
			// プロセス監視
			else if(HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorProcess();
			}
			// SNMPTRAP監視
			else if(HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorTrap();
			}
			// SNMP監視
			else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_SNMP_S.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					modMonitor = new ModifyMonitorSnmp();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					modMonitor = new ModifyMonitorSnmpString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("modifyMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// システムログ監視
			else if(HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorSystemlogString();
			}
			// SQL監視
			else if (HinemosModuleConstant.MONITOR_SQL_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_SQL_S.equals(monitorTypeId)) {
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					modMonitor = new ModifyMonitorSql();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					modMonitor = new ModifyMonitorSqlString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("modifyMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// コマンド監視
			else if(HinemosModuleConstant.MONITOR_CUSTOM_N.equals(monitorTypeId)			
				|| HinemosModuleConstant.MONITOR_CUSTOM_S.equals(monitorTypeId)){
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					modMonitor = new ModifyCustom();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					modMonitor = new ModifyCustomString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("modifyMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				
			}
			// Windowsサービス監視
			else if(HinemosModuleConstant.MONITOR_WINSERVICE.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorWinService();
			}
			// Windowsイベント監視
			else if(HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorWinEvent();
			}
			// JMX 監視
			else if(HinemosModuleConstant.MONITOR_JMX.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorJmx();
			}
			// カスタムトラップ監視
			else if(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N.equals(monitorTypeId)			
				|| HinemosModuleConstant.MONITOR_CUSTOMTRAP_S.equals(monitorTypeId)){
				// 数値監視
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					modMonitor = new ModifyCustomTrap();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					modMonitor = new ModifyCustomTrapString();
				}
				// それ以外
				else{
					HinemosUnknown e = new HinemosUnknown("This monitorTypeId = " + monitorTypeId + ", but unknown monitorType = " + monitorType);
					m_log.info("modifyMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}				
			}
			// ログ件数 監視
			else if(HinemosModuleConstant.MONITOR_LOGCOUNT.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorLogcount();
			}
			// 相関係数 監視
			else if(HinemosModuleConstant.MONITOR_CORRELATION.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorCorrelation();
			}
			// 収集値統合 監視
			else if(HinemosModuleConstant.MONITOR_INTEGRATION.equals(monitorTypeId)){
				modMonitor = new ModifyMonitorIntegration();
			}
			// バイナリファイル監視
			else if (HinemosModuleConstant.MONITOR_BINARYFILE_BIN.equals(monitorTypeId)) {
				modMonitor = new ModifyMonitorBinaryFile();
			}
			// パケットキャプチャ監視
			else if (HinemosModuleConstant.MONITOR_PCAP_BIN.equals(monitorTypeId)) {
				modMonitor = new ModifyMonitorPacketCapture();
			}
			// RPAログファイル監視
			else if (HinemosModuleConstant.MONITOR_RPA_LOGFILE.equals(monitorTypeId)) {
				modMonitor = new ModifyMonitorRpaLogfile();
			}
			// RPA管理ツール監視
			else if (HinemosModuleConstant.MONITOR_RPA_MGMT_TOOL_SERVICE.equals(monitorTypeId)) {
				modMonitor = new ModifyMonitorRpaManagementToolService();
			}
			// クラウドログ監視
			else if (HinemosModuleConstant.MONITOR_CLOUD_LOG.equals(monitorTypeId)) {
				modMonitor = new ModifyMonitorCloudLog();
			}
			
			// Other
			else{

				// 数値
				if(MonitorTypeConstant.TYPE_NUMERIC == monitorType){
					modMonitor = new ModifyMonitorPluginNumeric();
				}
				// 文字列
				else if(MonitorTypeConstant.TYPE_STRING == monitorType){
					modMonitor = new ModifyMonitorPluginString();
				}
				// 真偽値
				else if(MonitorTypeConstant.TYPE_TRUTH == monitorType){
					modMonitor = new ModifyMonitorPluginTruth();
				}

				if(modMonitor == null){
					HinemosUnknown e = new HinemosUnknown("Unknown monitorTypeId = " + monitorTypeId + ", monitorType = " + monitorType);
					m_log.info("modifyMonitor() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			// 監視設定情報を更新
			try {
				jtm.begin();

				flag = modMonitor.modify(info, loginUser);
				
				jtm.addCallback(new MonitorChangedNotificationCallback(monitorTypeId,flag));
				// コールバックメソッド設定
				if (!isImport) {
					addImportMonitorCallback(jtm);
				}
				if (HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)
						|| HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)) {
					jtm.addCallback(new NodeToMonitorCacheChangeCallback(monitorTypeId));
				}
				jtm.commit();
			} catch (HinemosUnknown e) {
				jtm.rollback();
				if (e.getCause() instanceof PersistenceException) {
					// PersistenceExceptionの場合はSQLエラーがメッセージに含まれてしまうため、
					// メッセージを置き換えて再度例外を投げる
					throw new HinemosUnknown("MonitorSettingControllerBean.modifyMonitor, monitorId = " + info.getMonitorId(), e.getCause());
				} else {
					throw e;
				}
			} catch (MonitorNotFound | InvalidRole e) {
				jtm.rollback();
				throw e;
			} catch (TriggerSchedulerException e) {
				m_log.info("modifyMonitor " + e.getClass().getName() + ", " + e.getMessage());
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} catch (ObjectPrivilege_InvalidRole e) {
				jtm.rollback();
				throw new InvalidRole(e.getMessage(), e);
			} catch (PersistenceException e) {
				m_log.warn("modifyMonitor() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				jtm.rollback();
				throw new HinemosUnknown("MonitorSettingControllerBean.modifyMonitor, monitorId = " + info.getMonitorId(), e);
			} catch (Exception e) {
				m_log.warn("modifyMonitor() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
			if (flag) {
				try {
					// 実行結果取得
					ret = getMonitor(info.getMonitorId());
				} catch (Exception e) {
					// トランザクションが本メソッドで完結していない場合は例外発生の可能性があるが、本処理上問題なし
					m_log.debug("modifyMonitor get monitor failure: "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
			}
		}
		return ret;
	}

	/**
	 * 監視設定情報の新規登録／変更時に呼び出すコールバックメソッドを設定
	 * 
	 * 設定インポートエクスポートでCommit後に呼び出すものだけ定義
	 * 
	 * @param jtm JpaTransactionManager
	 */
	public void addImportMonitorCallback(JpaTransactionManager jtm) {
		// 通知リレーション情報のキャッシュ更新
		jtm.addCallback(new NotifyRelationCacheRefreshCallback());
	}

	/**
	 *
	 * 監視設定情報をマネージャから削除します。<BR>
	 *
	 * @param monitorIdList
	 * @return 削除した監視設定List
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public List<MonitorInfo> deleteMonitor(List<String> monitorIdList)
			throws MonitorNotFound, HinemosUnknown, InvalidSetting, InvalidRole {
		m_log.debug("deleteMonitor() monitorId = " + monitorIdList );

		JpaTransactionManager jtm = null;
		List<MonitorInfo> retList = new ArrayList<>();

		// null チェック
		if(monitorIdList == null || monitorIdList.isEmpty()){
			HinemosUnknown e = new HinemosUnknown("monitorIdList is null or empty.");
			m_log.info("deleteMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		// 監視設定情報を削除
		boolean flag = false;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for(String monitorId : monitorIdList) {
				retList.add(getMonitor(monitorId));
			}

			for (String monitorId : monitorIdList) {
				
				MonitorInfo monitorInfo = QueryUtil.getMonitorInfoPK(monitorId);
				
				String monitorTypeId = monitorInfo.getMonitorTypeId();
				
				ModifyMonitor deleteMonitor = null;
				// Hinemos エージェント監視
				if(HinemosModuleConstant.MONITOR_AGENT.equals(monitorTypeId)){
					deleteMonitor = new ModifyMonitorAgent();
				}
				// HTTP 監視
				else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(monitorTypeId)
						|| HinemosModuleConstant.MONITOR_HTTP_S.equals(monitorTypeId)) {
					deleteMonitor = new ModifyMonitorHttp();
				}
				// HTTP シナリオ監視
				else if (HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(monitorTypeId)) {
					deleteMonitor = new ModifyMonitorHttpScenario();
				}
				// ログファイル 監視
				else if(HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorTypeId)){
					deleteMonitor =  new ModifyMonitorLogfileString();
				}
				// リソース 監視
				else if(HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)){
					deleteMonitor = new ModifyMonitorPerformance();
				}
				// ping監視
				else if(HinemosModuleConstant.MONITOR_PING.equals(monitorTypeId)){
					deleteMonitor = new ModifyMonitorPing();
				}
				// ポート監視
				else if(HinemosModuleConstant.MONITOR_PORT.equals(monitorTypeId)){
					deleteMonitor = new ModifyMonitorPort();
				}
				// プロセス監視
				else if(HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)){
					deleteMonitor = new ModifyMonitorProcess();
				}
				// SNMPTRAP監視
				else if(HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorTypeId)){
					deleteMonitor = new ModifyMonitorTrap();
				}
				// SNMP監視
				else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(monitorTypeId)
						|| HinemosModuleConstant.MONITOR_SNMP_S.equals(monitorTypeId)) {
					deleteMonitor = new ModifyMonitorSnmp();
				}
				// システムログ監視
				else if(HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)){
					deleteMonitor = new ModifyMonitorSystemlogString();
				}
				// SQL監視
				else if (HinemosModuleConstant.MONITOR_SQL_N.equals(monitorTypeId)
						|| HinemosModuleConstant.MONITOR_SQL_S.equals(monitorTypeId)) {
					deleteMonitor = new ModifyMonitorSql();
				}
				// コマンド監視
				else if(HinemosModuleConstant.MONITOR_CUSTOM_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_CUSTOM_S.equals(monitorTypeId)) {
					deleteMonitor = new ModifyCustom();
				}
				// Windowsサービス監視
				else if(HinemosModuleConstant.MONITOR_WINSERVICE.equals(monitorTypeId)){
					deleteMonitor = new ModifyMonitorWinService();
				}
				// Windowsイベント監視
				else if(HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorTypeId)){
					deleteMonitor = new ModifyMonitorWinEvent();
				}
				// JMX 監視
				else if(HinemosModuleConstant.MONITOR_JMX.equals(monitorTypeId)){
					deleteMonitor = new ModifyMonitorJmx();
				}
				// カスタムトラップ監視
				else if(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_CUSTOMTRAP_S.equals(monitorTypeId)) {
					deleteMonitor = new ModifyCustomTrap();
				}
				// ログ件数 監視
				else if(HinemosModuleConstant.MONITOR_LOGCOUNT.equals(monitorTypeId)){
					deleteMonitor = new ModifyMonitorLogcount();
				}
				// 相関係数 監視
				else if(HinemosModuleConstant.MONITOR_CORRELATION.equals(monitorTypeId)){
					deleteMonitor = new ModifyMonitorCorrelation();
				}
				// 収集値統合 監視
				else if(HinemosModuleConstant.MONITOR_INTEGRATION.equals(monitorTypeId)){
					deleteMonitor = new ModifyMonitorIntegration();
				}
				// バイナリファイル監視
				else if (HinemosModuleConstant.MONITOR_BINARYFILE_BIN.equals(monitorTypeId)) {
					deleteMonitor = new ModifyMonitorBinaryFile();
				}
				// パケットキャプチャ監視
				else if (HinemosModuleConstant.MONITOR_PCAP_BIN.equals(monitorTypeId)) {
					deleteMonitor = new ModifyMonitorPacketCapture();
				}
				// RPAログファイル監視
				else if (HinemosModuleConstant.MONITOR_RPA_LOGFILE.equals(monitorTypeId)) {
					deleteMonitor = new ModifyMonitorRpaLogfile();
				}
				else if (HinemosModuleConstant.MONITOR_RPA_MGMT_TOOL_SERVICE.equals(monitorTypeId)) {
					deleteMonitor = new ModifyMonitorRpaManagementToolService();
				}
				// Other
				else{
					deleteMonitor = new ModifyMonitorPluginString();
				}
				
				// 他機能で使用されている場合はエラーとする
				MonitorValidator.valideDeleteMonitor(monitorId);
				flag = deleteMonitor.delete(monitorId);
				
				jtm.addCallback(new MonitorChangedNotificationCallback(monitorTypeId,flag));
				if (HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)
						|| HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)) {
					jtm.addCallback(new NodeToMonitorCacheChangeCallback(monitorTypeId));
				}
				
			}
			jtm.commit();
		} catch ( HinemosUnknown  | InvalidSetting | MonitorNotFound e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return retList;
	}


	/**
	 * 監視情報を取得します。<BR>
	 *
	 * @param monitorId
	 * @return
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public MonitorInfo getMonitor(String monitorId) throws MonitorNotFound, HinemosUnknown, InvalidRole {
		m_log.debug("getMonitor() monitorId = " + monitorId);

		JpaTransactionManager jtm = null;

		// null チェック
		if(monitorId == null || "".equals(monitorId)){
			HinemosUnknown e = new HinemosUnknown("monitorId is null or empty.");
			m_log.info("getMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// 監視設定情報を取得
		MonitorInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			MonitorInfo monitorInfo = QueryUtil.getMonitorInfoPK(monitorId);
			// 通知情報の取得
			monitorInfo.setNotifyRelationList(
					NotifyRelationCache.getNotifyList(monitorInfo.getNotifyGroupId()));
			if (monitorInfo.getMonitorType().equals(MonitorTypeConstant.TYPE_NUMERIC)) {
				// 通知情報(将来予測)の取得
				monitorInfo.setPredictionNotifyRelationList(
						NotifyRelationCache.getNotifyList(
						CollectMonitorManagerUtil.getPredictionNotifyGroupId(monitorInfo.getNotifyGroupId())));
				// 通知情報(変化点監視)の取得
				monitorInfo.setChangeNotifyRelationList(
						NotifyRelationCache.getNotifyList(
						CollectMonitorManagerUtil.getChangeNotifyGroupId(monitorInfo.getNotifyGroupId())));
			}

			String monitorTypeId = monitorInfo.getMonitorTypeId();
			
			SelectMonitor selectMonitor = null;
			// コマンド監視
			if(HinemosModuleConstant.MONITOR_CUSTOM_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_CUSTOM_S.equals(monitorTypeId)){
				selectMonitor = new SelectCustom();
			}
			// 収集値統合監視
			else if(HinemosModuleConstant.MONITOR_INTEGRATION.equals(monitorTypeId)){
				selectMonitor = new SelectMonitorIntegration();
			}
			// Other
			else{
				selectMonitor = new SelectMonitor();
			}

			info = selectMonitor.getMonitor(monitorTypeId, monitorId);
			jtm.commit();
		} catch (MonitorNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMonitor() "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return info;
	}

	/**
	 * 監視設定一覧を取得する
	 *
	 * @return
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getMonitorList() throws InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;
		ArrayList<MonitorInfo> list = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			long start = HinemosTime.currentTimeMillis();
			list = new SelectMonitor().getMonitorList();
			long end = HinemosTime.currentTimeMillis();
			long time = end - start;
			if (1000 < time) {
				m_log.info("getMonitorList " + (end-start) + "ms");
			} else {
				m_log.debug("getMonitorList " + (end-start) + "ms");
			}

			jtm.commit();
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMonitorList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * 以下の条件に一致する監視設定一覧を取得します。
	 *　　オーナーロールIDが参照可能
	 *　　文字列監視
	 *　　指定されたファシリティIDもしくはその配下のノードに一致する
	 *　※サイレント監視で使用する。
	 *
	 * @param facilityId　ファシリティID
	 * @param ownerRoleId　オーナーロールID
	 * @return List 監視設定リスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<MonitorInfo> getStringMonitoInfoListForAnalytics(String facilityId, String ownerRoleId) 
			throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<MonitorInfo> list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = new SelectMonitor().getStringMonitoInfoListForAnalytics(facilityId, ownerRoleId); 
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (Exception e){
			m_log.warn("getStringMonitoInfoListForAnalytics() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * システムログ監視一覧リストを返します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getSystemlogMonitorCache() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		ArrayList<MonitorInfo> list = null;
		try {
			list = SystemlogCache.getSystemlogList();
		} catch (MonitorNotFound e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("getSystemlogList " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		// null check
		if(list == null){
			list = new ArrayList<MonitorInfo>();
		}
		return list;
	}

	/**
	 * 監視種別ID、監視IDに合致する監視を返します。
	 *
	 * @param monitorTypeId 監視種別ID
	 * @param monitorId 監視ID
	 * @return MonitorInfo 監視設定情報
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public MonitorInfo getMonitor(String monitorTypeId, String monitorId) 
			throws MonitorNotFound, InvalidRole, HinemosUnknown{

		// null チェック
		if(monitorId == null || "".equals(monitorId)){
			HinemosUnknown e = new HinemosUnknown("monitorId is null or empty.");
			m_log.info("getMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		JpaTransactionManager jtm = null;
		MonitorInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			info = QueryUtil.getMonitorInfoPK(monitorId);
			
			// monitorTypeIdが一致しない場合、MonitorNotFoundをスローする
			if(!info.getMonitorTypeId().equals(monitorTypeId)) {
				String msg = "monitorId = " + monitorId + ", monitorTypeId = " + monitorTypeId;
				MonitorNotFound e = new MonitorNotFound(msg);
				m_log.info("getMonitor() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			// 通知情報の取得
			info.setNotifyRelationList(
					NotifyRelationCache.getNotifyList(info.getNotifyGroupId()));
			if (info.getMonitorType().equals(MonitorTypeConstant.TYPE_NUMERIC)) {
				// 通知情報(将来予測)の取得
				info.setPredictionNotifyRelationList(
						NotifyRelationCache.getNotifyList(
						CollectMonitorManagerUtil.getPredictionNotifyGroupId(info.getNotifyGroupId())));
				// 通知情報(変化点監視)の取得
				info.setChangeNotifyRelationList(
						NotifyRelationCache.getNotifyList(
						CollectMonitorManagerUtil.getChangeNotifyGroupId(info.getNotifyGroupId())));
			}
			
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (MonitorNotFound e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getMonitor() : monitorId = " + monitorId + ", monitorTypeId = " + monitorTypeId
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return info;
	}
	
	/**
	 * 監視一覧リストを返します。
	 *
	 * @param monitorTypeId 監視種別ID
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getMonitorList(String monitorTypeId) throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		// 監視一覧を取得
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = new SelectMonitor().getMonitorList(monitorTypeId);
			
			// 通知情報の設定
			for(MonitorInfo monitorInfo : list) {
				monitorInfo.setNotifyRelationList(
						NotifyRelationCache.getNotifyList(monitorInfo.getNotifyGroupId()));
				if (monitorInfo.getMonitorType().equals(MonitorTypeConstant.TYPE_NUMERIC)) {
					// 通知情報(将来予測)の取得
					monitorInfo.setPredictionNotifyRelationList(
							NotifyRelationCache.getNotifyList(
								CollectMonitorManagerUtil.getPredictionNotifyGroupId(monitorInfo.getNotifyGroupId())));
					// 通知情報(変化点監視)の取得
					monitorInfo.setChangeNotifyRelationList(
							NotifyRelationCache.getNotifyList(
								CollectMonitorManagerUtil.getChangeNotifyGroupId(monitorInfo.getNotifyGroupId())));
				}
			}
			
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMonitorList() : monitorTypeId = " + monitorTypeId
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 *
	 * 監視設定の監視を有効化/無効化します。<BR>
	 *
	 * @param monitorIdList
	 * @param validFlag
	 * @return 変更した監視設定情報一覧
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 */
	// TODO: クライアントに対して、エラーの情報が渡せるように改修する必要がある
	public List<MonitorInfo> setStatusMonitor(List<String> monitorIdList, boolean validFlag) throws HinemosUnknown, MonitorNotFound, InvalidRole {
		List<MonitorInfo> retList = new ArrayList<>();

		// null check
		if(monitorIdList == null || monitorIdList.size() == 0){
			HinemosUnknown e = new HinemosUnknown("target monitorId is null or empty.");
			m_log.info("setStatusMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		for (String monitorId : monitorIdList) {
			if(monitorId == null || monitorId.isEmpty()){
				continue;
			}
			MonitorInfo info = getMonitor(monitorId);
			try{
				// オブジェクト権限チェック
				QueryUtil.getMonitorInfoPK(monitorId, ObjectPrivilegeMode.MODIFY);
	
				if (isChangeNecessary(info, validFlag)) {
					info.setMonitorFlg(validFlag);
					
					if (info.getMonitorType() == MonitorTypeConstant.TYPE_NUMERIC
							&& !(validFlag && !info.getCollectorFlg())) {
						// 数値監視
						// 有効への変更は収集フラグがONの場合のみ変更
						// 無効への変更は収集フラグを判断せずに変更
						// ※不具合で発生した下記の状態を変更できるようにするため
						// 収集フラグ=OFF、変更量=ON、将来予測=ON

						// 変化量、将来予測フラグを変更する
						m_log.debug("Hinemos property：monitor_valid_link_change:"
								+ String.valueOf(HinemosPropertyCommon.monitor_valid_link_change.getBooleanValue()));
						// 変化量フラグの設定
						// Hinemosプロパティで変化量フラグの変更可否を判断
						if (HinemosPropertyCommon.monitor_valid_link_change.getBooleanValue()) {
							m_log.debug("change amount flag：" + String.valueOf(validFlag));
							info.setChangeFlg(validFlag);
						}
						m_log.debug("Hinemos property：monitor_valid_link_prediction:" + String
								.valueOf(HinemosPropertyCommon.monitor_valid_link_prediction.getBooleanValue()));
						// 将来予測フラグの設定
						// Hinemosプロパティで将来予測フラグの変更可否を判断
						if (HinemosPropertyCommon.monitor_valid_link_prediction.getBooleanValue()) {
							m_log.debug("prediction flag：" + String.valueOf(validFlag));
							info.setPredictionFlg(validFlag);
						}
					}
					modifyMonitor(info);
				}
				retList.add(getMonitor(monitorId));
			} catch (MonitorNotFound | InvalidRole | HinemosUnknown e) {
				throw e;
			} catch (InvalidSetting e) {
				throw new HinemosUnknown(e.getMessage(), e);
			} catch (ObjectPrivilege_InvalidRole e) {
				throw new InvalidRole(e.getMessage(), e);
			}
		}
		return retList;
	}

	/**
	 * 監視フラグ、変化量フラグ、将来予測フラグの変更要否のチェック
	 * 
	 * @param info 監視情報
	 * @param valid 有効・無効
	 * @return
	 */
	private boolean isChangeNecessary(MonitorInfo info, boolean validFlag) {
		if (info.getMonitorFlg() != validFlag) {
			//監視フラグが変更したい状態でない場合、変更が必要
			return true;
		}
		
		if (info.getMonitorType() != MonitorTypeConstant.TYPE_NUMERIC ||
				(validFlag && !info.getCollectorFlg())) {
			//数値監視でない場合
			//有効への変更 かつ 収集フラグがONでない場合は変更不要
			return false;
		}
		
		if ((info.getChangeFlg() == validFlag) &&
				(info.getPredictionFlg() == validFlag)) {
			//変更量フラグ、将来予測フラグが既に変更したい状態の場合、変更不要
			return false;
		}
		return true;
	}
	
	/**
	 *
	 * 監視設定の収集を有効化/無効化します。<BR>
	 *
	 * @param monitorIdList
	 * @param validFlag
	 * @return 変更した監視設定情報一覧
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 */
	public List<MonitorInfo> setStatusCollector(List<String> monitorIdList, boolean validFlag) throws HinemosUnknown, MonitorNotFound, InvalidRole {
		List<MonitorInfo> retList = new ArrayList<>();

		// null check
		if(monitorIdList == null || monitorIdList.size() == 0){
			HinemosUnknown e = new HinemosUnknown("target monitorId is null or empty.");
			m_log.info("setStatusCollector() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		for (String monitorId : monitorIdList) {
			if(monitorId == null || monitorId.isEmpty()){
				continue;
			}
			MonitorInfo info = getMonitor(monitorId);
			try{
				// オブジェクト権限チェック
				QueryUtil.getMonitorInfoPK(monitorId, ObjectPrivilegeMode.MODIFY);
				// 収集蓄積機能実装に伴い、文字列監視やトラップ監視も収集可能にする
				if(validFlag) {
					if(info.getMonitorType() == MonitorTypeConstant.TYPE_TRUTH) {
						m_log.debug("setStatusMonitor() : monitorId = " + monitorId + " is truth.");
						continue;
					}
	
					if(!info.getCollectorFlg()){
						info.setCollectorFlg(true);
						modifyMonitor(info);
					}
				} else {
					if(info.getCollectorFlg()){
						info.setCollectorFlg(false);
						modifyMonitor(info);
					}
				}
				retList.add(getMonitor(monitorId));
			} catch (MonitorNotFound | InvalidRole | HinemosUnknown e) {
				throw e;
			} catch (InvalidSetting e) {
				throw new HinemosUnknown(e.getMessage(), e);
			} catch (ObjectPrivilege_InvalidRole e) {
				throw new InvalidRole(e.getMessage(), e);
			}
		}
		return retList;
	}

	/**
	 * 監視設定一覧の取得
	 *
	 * @param condition フィルタ条件
	 * @return 監視設定一覧
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getMonitorList(MonitorFilterInfo condition) throws InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorList(MonitorFilterInfo) : start");

		JpaTransactionManager jtm = null;

		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = new SelectMonitor().getMonitorList(condition);
			jtm.commit();
		} catch (HinemosUnknown | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (MonitorNotFound e) {
			m_log.info("getMonitorList " + e.getClass().getName() + ", " + e.getMessage());
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMonitorList(condition) : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		m_log.debug("getMonitorList(condition) : end");
		return list;
	}

	/**
	 * チェック設定を含まない監視設定一覧の取得
	 *
	 * @param condition フィルタ条件
	 * @return チェック設定を含まない監視設定一覧
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getMonitorListWithoutCheckInfo(MonitorFilterInfo condition) throws InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorListWithoutCheckInfo(conditionList) : start");

		long time1 = System.currentTimeMillis();
		long time2 = 0L; 
		long time3 = 0L; 
		JpaTransactionManager jtm = null;

		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			if(condition != null) {
				list = new SelectMonitor().getMonitorList(condition);
			} else {
				list = new SelectMonitor().getMonitorList();
			}
			jtm.commit();
			time2 = System.currentTimeMillis();
		} catch (HinemosUnknown | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (MonitorNotFound e) {
			m_log.info("getMonitorListWithoutCheckInfo(conditionList) " + e.getClass().getName() + ", " + e.getMessage());
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMonitorListWithoutCheckInfo(conditionList) : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
			// MonitorInfoからCheckInfoをはずす
			if (list != null) {
				for(MonitorInfo info : list) {
					info.setCustomCheckInfo(null);
					info.setCustomTrapCheckInfo(null);
					info.setHttpCheckInfo(null);
					info.setHttpScenarioCheckInfo(null);
					info.setJmxCheckInfo(null);
					info.setLogfileCheckInfo(null);
					info.setBinaryCheckInfo(null);
					info.setPerfCheckInfo(null);
					info.setPingCheckInfo(null);
					info.setPluginCheckInfo(null);
					info.setPortCheckInfo(null);
					info.setProcessCheckInfo(null);
					info.setSnmpCheckInfo(null);
					info.setSqlCheckInfo(null);
					info.setTrapCheckInfo(null);
					info.setWinEventCheckInfo(null);
					info.setWinServiceCheckInfo(null);
				}
			}
			time3 = System.currentTimeMillis();
			long term = time3 - time1;
			String msg = String.format("getMonitorListWithoutCheckInfo() end : term1=%s[msec], term2=%s[msec], all_term=%s[msec], ",
					(time2 - time1), (time3 - time2), term);
			if (term >= 10 * 1000) {
				m_log.info(msg);
			} else {
				m_log.debug(msg);
			}
		}

		m_log.debug("getMonitorListWithoutCheckInfo(conditionList) : end");
		return list;
	}

	/**
	 * チェック設定を含まない監視設定一覧の取得
	 *
	 * @param condition フィルタ条件
	 * @return チェック設定を含まない監視設定一覧
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfoBean> getMonitorBeanListWithoutCheckInfo(MonitorFilterInfo condition) throws InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorListWithoutCheckInfo(conditionList) : start");

		long time1 = System.currentTimeMillis();
		long time2 = 0L; 
		long time3 = 0L; 
		JpaTransactionManager jtm = null;
		ArrayList<MonitorInfoBean> rtnList = new ArrayList<>();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			ArrayList<MonitorInfo> list = null;
			if(condition != null) {
				list = new SelectMonitor().getMonitorList(condition);
			} else {
				list = new SelectMonitor().getMonitorList();
			}

			time2 = System.currentTimeMillis();

			// 戻り値に設定
			for(MonitorInfo info : list) {
				rtnList.add(createMonitorInfoBean(info));
			}
	
			jtm.commit();
		} catch (HinemosUnknown | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (MonitorNotFound e) {
			m_log.info("getMonitorListWithoutCheckInfo(conditionList) " + e.getClass().getName() + ", " + e.getMessage());
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMonitorListWithoutCheckInfo(conditionList) : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
			time3 = System.currentTimeMillis();
			long term = time3 - time1;
			String msg = String.format("getMonitorListWithoutCheckInfo() end : term1=%s[msec], term2=%s[msec], all_term=%s[msec], ",
					(time2 - time1), (time3 - time2), term);
			if (term >= 10 * 1000) {
				m_log.info(msg);
			} else {
				m_log.debug(msg);
			}
		}
		m_log.debug("getMonitorListWithoutCheckInfo(conditionList) : end");
		return rtnList;
	}

	/**
	 * 監視設定IDの取得
	 *
	 * @param condition フィルタ条件
	 * @return 監視設定IDリスト
	 * @throws HinemosUnknown
	 */
	public List<String> getMonitorIdList(MonitorFilterInfo condition) throws InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorIdList(condition) : start");
		
		JpaTransactionManager jtm = null;

		List<MonitorInfo> monitorInfoList = null;
		List<String> list = new ArrayList<>();
		
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			if(condition != null) {
				monitorInfoList = new SelectMonitor().getMonitorList(condition);
			} else {
				monitorInfoList = new SelectMonitor().getMonitorList();
			}
			
			for(MonitorInfo info : monitorInfoList) {
				list.add(info.getMonitorId());
			}
			
			jtm.commit();
		} catch (HinemosUnknown | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (MonitorNotFound e) {
			m_log.info("getMonitorIdList(condition) " + e.getClass().getName() + ", " + e.getMessage());
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMonitorIdList(condition) : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		m_log.debug("getMonitorIdList(condition) : end");
		return list;
	}

	/**
	 * 指定した文字列監視設定のタグ一覧を取得します。
	 * 
	 * @param monitorId		監視項目ID
	 * @param ownerRoleId	オーナーロールID
	 * @return タグ一覧 
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<String> getMonitorStringTagList(String monitorId, String ownerRoleId) 
			throws MonitorNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<String> ret = null;

		try {
			// トランザクション開始
			jtm = new JpaTransactionManager();
			jtm.begin();

			HashSet<String> retSet = new HashSet<>();
			MonitorInfo monitorInfo = QueryUtil.getMonitorInfoPK_OR(monitorId, ownerRoleId);
			if (monitorInfo.getLogFormatId() != null && !monitorInfo.getLogFormatId().isEmpty()) {
				LogFormat logFormat = com.clustercontrol.hub.util.QueryUtil.getLogFormatPK_OR(
					monitorInfo.getLogFormatId(), monitorInfo.getOwnerRoleId());
				if (logFormat != null && logFormat.getKeyPatternList() != null) {
					for (LogFormatKey logFormatKey : logFormat.getKeyPatternList()) {
						retSet.add(logFormatKey.getKey());
					}
				}
			}
			// Hinemosが自動で抽出するタグを追加
			retSet.addAll(CollectStringTag.getSampleTagList(monitorInfo.getMonitorTypeId()));
			if (SdmlUtil.isCreatedBySdml(monitorInfo)) {
				// SDMLで自動作成された監視の場合、SDMLで自動抽出するタグを追加
				retSet.addAll(SdmlUtil.getSampleTagList(monitorInfo.getMonitorId(), monitorInfo.getSdmlMonitorTypeId()));
			}
			ret = new ArrayList<String>(retSet);
			Collections.sort(ret);
			jtm.commit();
		} catch (MonitorNotFound | InvalidRole e) {
			m_log.warn("getMonitorStringTagList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getMonitorStringTagList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return ret;
	}

	/**
	 * 指定された監視種別の監視設定一覧を取得する
	 *
	 * @param monitorType 監視種別
	 * @param ownerRoleId オーナーロールID
	 * @return
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getMonitorListByMonitorType(List<Integer> monitorTypes, String ownerRoleId)
			throws HinemosUnknown{

		JpaTransactionManager jtm = null;
		ArrayList<MonitorInfo> list = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = new SelectMonitor().getMonitorListByMonitorType_OR(monitorTypes, ownerRoleId);

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getMonitorListByMonitorType() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * 以下の条件に一致する監視情報を取得します。
	 *　　オーナーロールIDが参照可能
	 *　　文字列監視もしくはSNMPTRAP監視
	 *　　指定されたファシリティIDもしくはその直下のノードに一致する　
	 *　　※ログ件数監視で使用する。
	 *
	 * @param facilityId　ファシリティID
	 * @param ownerRoleId　オーナーロールID
	 * @return　監視設定リスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getMonitorListForLogcount(String facilityId, String ownerRoleId)
			throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<MonitorInfo> list = new ArrayList<>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<Integer> typeList = new ArrayList<>();
			typeList.add(MonitorTypeConstant.TYPE_STRING);
			typeList.add(MonitorTypeConstant.TYPE_TRAP);
			List<MonitorInfo> monitorInfoList = new SelectMonitor().getMonitorListByMonitorType_OR(typeList, ownerRoleId);
			for (MonitorInfo monitorInfo : monitorInfoList) {
				// 指定したファシリティIDをスコープ、もしくはノードに含む場合のみ対象とする
				if (monitorInfo.getFacilityId().equals(facilityId)
						|| new RepositoryControllerBean().getFacilityIdList(
								monitorInfo.getFacilityId(), 0).contains(facilityId)) {
					// 個別の情報は不要なので削除（文字列監視のみ含まれるはずなのですべて消す必要はないがまとめて消している）
					jtm.getEntityManager().detach(monitorInfo);
					monitorInfo.setCustomCheckInfo(null);
					monitorInfo.setCustomTrapCheckInfo(null);
					monitorInfo.setHttpCheckInfo(null);
					monitorInfo.setHttpScenarioCheckInfo(null);
					monitorInfo.setJmxCheckInfo(null);
					monitorInfo.setLogfileCheckInfo(null);
					monitorInfo.setBinaryCheckInfo(null);
					monitorInfo.setPerfCheckInfo(null);
					monitorInfo.setPingCheckInfo(null);
					monitorInfo.setPluginCheckInfo(null);
					monitorInfo.setPortCheckInfo(null);
					monitorInfo.setProcessCheckInfo(null);
					monitorInfo.setSnmpCheckInfo(null);
					monitorInfo.setSqlCheckInfo(null);
					monitorInfo.setTrapCheckInfo(null);
					monitorInfo.setWinEventCheckInfo(null);
					monitorInfo.setWinServiceCheckInfo(null);

					list.add(monitorInfo);
				}
			}
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (Exception e){
			m_log.warn("getMonitorListForLogcount() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * ログ件数監視の過去分集計を行う。
	 *
	 * @param monitorId 監視設定ID
	 * @param startDate 収集開始日時
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void runSummaryLogcount(String monitorId, Long startDate) throws MonitorNotFound, InvalidSetting, InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();

			// 収集終了日を設定
			Long endDate = HinemosTime.currentTimeMillis();

			// 入力チェック
			MonitorValidator.validateSummaryLogcount(monitorId, startDate, endDate);

			// 収集処理開始
			SummaryLogcountWorker.runLogcount(monitorId, startDate, endDate);

			jtm.commit();
		} catch (InvalidRole | InvalidSetting | MonitorNotFound | HinemosUnknown e) {
			m_log.info("runSummaryLogcount " + e.getClass().getName() + ", " + e.getMessage());
			throw e;
		} catch (Exception e) {
			m_log.warn("runSummaryLogcount() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * Hinemosクライアントに引き渡す監視設定情報を作成
	 * 
	 * @param monitorInfo DBより取得した監視設定情報
	 * @return チェック設定、監視種別ごとの情報を含まない監視設定情報
	 */
	private MonitorInfoBean createMonitorInfoBean(MonitorInfo monitorInfo) {
		if (monitorInfo == null) {
			return null;
		}
		MonitorInfoBean bean = new MonitorInfoBean();
		bean.setMonitorId(monitorInfo.getMonitorId());
		bean.setApplication(monitorInfo.getApplication());
		bean.setCollectorFlg(monitorInfo.getCollectorFlg());
		bean.setDelayTime(monitorInfo.getDelayTime());
		bean.setDescription(monitorInfo.getDescription());
		bean.setFailurePriority(monitorInfo.getFailurePriority());
		bean.setItemName(monitorInfo.getItemName());
		bean.setMeasure(monitorInfo.getMeasure());
		bean.setMonitorFlg(monitorInfo.getMonitorFlg());
		bean.setMonitorType(monitorInfo.getMonitorType());
		bean.setMonitorTypeId(monitorInfo.getMonitorTypeId());
		bean.setNotifyGroupId(monitorInfo.getNotifyGroupId());
		bean.setRegDate(monitorInfo.getRegDate());
		bean.setRegUser(monitorInfo.getRegUser());
		bean.setRunInterval(monitorInfo.getRunInterval());
		bean.setTriggerType(monitorInfo.getTriggerType());
		bean.setUpdateDate(monitorInfo.getUpdateDate());
		bean.setUpdateUser(monitorInfo.getUpdateUser());
		bean.setCalendarId(monitorInfo.getCalendarId());
		bean.setFacilityId(monitorInfo.getFacilityId());
		bean.setLogFormatId(monitorInfo.getLogFormatId());
		bean.setPredictionFlg(monitorInfo.getPredictionFlg());
		bean.setPredictionMethod(monitorInfo.getPredictionMethod());
		bean.setPredictionAnalysysRange(monitorInfo.getPredictionAnalysysRange());
		bean.setPredictionTarget(monitorInfo.getPredictionTarget());
		bean.setPredictionApplication(monitorInfo.getPredictionApplication());
		bean.setChangeFlg(monitorInfo.getChangeFlg());
		bean.setChangeAnalysysRange(monitorInfo.getChangeAnalysysRange());
		bean.setChangeApplication(monitorInfo.getChangeApplication());
		bean.setSdmlMonitorTypeId(monitorInfo.getSdmlMonitorTypeId());
		bean.setScope(monitorInfo.getScope());
		bean.setOwnerRoleId(monitorInfo.getOwnerRoleId());
		return bean;
	}
}
