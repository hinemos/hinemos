/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.session;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.scheduler.TriggerSchedulerException;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.custom.factory.ModifyCustom;
import com.clustercontrol.custom.factory.ModifyCustomString;
import com.clustercontrol.custom.factory.SelectCustom;
import com.clustercontrol.customtrap.factory.ModifyCustomTrap;
import com.clustercontrol.customtrap.factory.ModifyCustomTrapString;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.hinemosagent.factory.ModifyMonitorAgent;
import com.clustercontrol.http.factory.ModifyMonitorHttp;
import com.clustercontrol.http.factory.ModifyMonitorHttpScenario;
import com.clustercontrol.http.factory.ModifyMonitorHttpString;
import com.clustercontrol.jmx.factory.ModifyMonitorJmx;
import com.clustercontrol.logfile.factory.ModifyMonitorLogfileString;
import com.clustercontrol.monitor.bean.MonitorFilterInfo;
import com.clustercontrol.monitor.plugin.factory.ModifyMonitorPluginNumeric;
import com.clustercontrol.monitor.plugin.factory.ModifyMonitorPluginString;
import com.clustercontrol.monitor.plugin.factory.ModifyMonitorPluginTruth;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.MonitorChangedNotificationCallback;
import com.clustercontrol.monitor.run.util.MonitorValidator;
import com.clustercontrol.monitor.run.util.NodeToMonitorCacheChangeCallback;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.performance.monitor.factory.ModifyMonitorPerformance;
import com.clustercontrol.performance.monitor.factory.SelectMonitorPerformance;
import com.clustercontrol.ping.factory.ModifyMonitorPing;
import com.clustercontrol.port.factory.ModifyMonitorPort;
import com.clustercontrol.process.factory.ModifyMonitorProcess;
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
	 * @param info
	 * @return
	 * @throws MonitorDuplicate
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public boolean addMonitor(MonitorInfo info) throws MonitorDuplicate, InvalidSetting, InvalidRole, HinemosUnknown{
		m_log.debug("addMonitor()");

		JpaTransactionManager jtm = null;
		boolean flag = false;

		try {
			jtm = new JpaTransactionManager();

			//入力チェック
			try{
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

				jtm.addCallback(new MonitorChangedNotificationCallback(monitorTypeId));
				if (HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)
						|| HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)) {
					jtm.addCallback(new NodeToMonitorCacheChangeCallback(monitorTypeId));
				}
				jtm.commit();
			} catch (MonitorDuplicate | HinemosUnknown | InvalidRole e) {
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
			} finally {
				if (flag) {
					try {
						// コミット後にキャッシュクリア
						NotifyRelationCache.refresh();

						if (HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)) {
							SystemlogCache.refresh();
						}
					} catch (Exception e) {
						m_log.warn("addMonitor() transaction failure. : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown(e.getMessage(), e);
					}
				}
			}
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return flag;
	}


	/**
	 * 監視設定情報を更新します。<BR>
	 *
	 * @param info
	 * @return
	 * @throws MonitorNotFound
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public boolean modifyMonitor(MonitorInfo info) throws MonitorNotFound, InvalidSetting, InvalidRole, HinemosUnknown {
		m_log.debug("modifyMonitor()");

		JpaTransactionManager jtm = null;
		boolean flag = false;

		try {
			jtm = new JpaTransactionManager();

			//入力チェック
			try{
				MonitorValidator.validateMonitorInfo(info);
			} catch (InvalidSetting | InvalidRole e) {
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
				
				jtm.addCallback(new MonitorChangedNotificationCallback(monitorTypeId));
				if (HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)
						|| HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)) {
					jtm.addCallback(new NodeToMonitorCacheChangeCallback(monitorTypeId));
				}
				jtm.commit();
			} catch (MonitorNotFound | HinemosUnknown | InvalidRole e) {
				jtm.rollback();
				throw e;
			} catch (TriggerSchedulerException e) {
				m_log.info("modifyMonitor " + e.getClass().getName() + ", " + e.getMessage());
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} catch (ObjectPrivilege_InvalidRole e) {
				jtm.rollback();
				throw new InvalidRole(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("modifyMonitor() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				if (flag) {
					try {
						// コミット後にキャッシュクリア
						NotifyRelationCache.refresh();

						if (HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)) {
							SystemlogCache.refresh();
						}
					} catch (Exception e) {
						m_log.warn("modifyMonitor() transaction failure. : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown(e.getMessage(), e);
					}
				}
			}
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return flag;
	}


	/**
	 *
	 * 監視設定情報をマネージャから削除します。<BR>
	 *
	 * @param monitorIdList
	 * @param monitorTypeId
	 * @return
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public boolean deleteMonitor(List<String> monitorIdList, String monitorTypeId)
			throws MonitorNotFound, HinemosUnknown, InvalidSetting, InvalidRole {
		m_log.debug("deleteMonitor() monitorId = " + monitorIdList + ", monitorTypeId = " + monitorTypeId);

		JpaTransactionManager jtm = null;

		// null チェック
		if(monitorIdList == null || monitorIdList.isEmpty()){
			HinemosUnknown e = new HinemosUnknown("monitorIdList is null or empty.");
			m_log.info("deleteMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if(monitorTypeId == null || "".equals(monitorTypeId)){
			HinemosUnknown e = new HinemosUnknown("monitorTypeId is null or empty.");
			m_log.info("deleteMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

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
		// Other
		else{
			deleteMonitor = new ModifyMonitorPluginString();
		}

		// 監視設定情報を削除
		boolean flag = false;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for (String monitorId : monitorIdList) {
				// 他機能で使用されている場合はエラーとする
				MonitorValidator.valideDeleteMonitor(monitorId);
				flag = deleteMonitor.delete(monitorTypeId, monitorId);
			}

			jtm.addCallback(new MonitorChangedNotificationCallback(monitorTypeId));
			if (HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)) {
				jtm.addCallback(new NodeToMonitorCacheChangeCallback(monitorTypeId));
			}

			jtm.commit();
		} catch (MonitorNotFound | HinemosUnknown | InvalidRole | InvalidSetting e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (TriggerSchedulerException e) {
			m_log.info("deleteMonitor " + e.getClass().getName() + ", " + e.getMessage());
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
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
			if (flag) {
				try {
					// コミット後にキャッシュクリア
					NotifyRelationCache.refresh();

					if (HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)) {
						SystemlogCache.refresh();
					}
				} catch (Exception e) {
					m_log.warn("deleteMonitor() transaction failure. : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				} finally {
					jtm.close();
				}
			}
			if (jtm != null)
				jtm.close();
		}
		return flag;
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
					new NotifyControllerBean().getNotifyRelation(monitorInfo.getNotifyGroupId()));
			String monitorTypeId = monitorInfo.getMonitorTypeId();
			
			SelectMonitor selectMonitor = null;
			// リソース 監視
			if(HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)){
				selectMonitor = new SelectMonitorPerformance();
			}
			// コマンド監視
			else if(HinemosModuleConstant.MONITOR_CUSTOM_N.equals(monitorTypeId)
					|| HinemosModuleConstant.MONITOR_CUSTOM_S.equals(monitorTypeId)){
				selectMonitor = new SelectCustom();
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
	 * Hinemos Agent監視一覧リストを返します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getAgentList() throws MonitorNotFound, InvalidRole, HinemosUnknown {
		return getMonitorList(HinemosModuleConstant.MONITOR_AGENT);
	}

	/**
	 * HTTP監視一覧リストを取得します。<BR>
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getHttpList() throws MonitorNotFound, InvalidRole, HinemosUnknown {
		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		list.addAll(getMonitorList(HinemosModuleConstant.MONITOR_HTTP_N));
		list.addAll(getMonitorList(HinemosModuleConstant.MONITOR_HTTP_S));
		list.addAll(getMonitorList(HinemosModuleConstant.MONITOR_HTTP_SCENARIO));
		return list;
	}
	/**
	 * JMX監視一覧リストを取得します。<BR>
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getJmxList() throws MonitorNotFound, InvalidRole, HinemosUnknown {
		return getMonitorList(HinemosModuleConstant.MONITOR_JMX);
	}

	/**
	 * ログファイル監視一覧リストを返します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getLogfileList() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		return getMonitorList(HinemosModuleConstant.MONITOR_LOGFILE);
	}

	/**
	 * リソース監視一覧リストを返します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getPerformanceList() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		return getMonitorList(HinemosModuleConstant.MONITOR_PERFORMANCE);
	}

	/**
	 * ping監視一覧リストを取得します。<BR>
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getPingList() throws MonitorNotFound, InvalidRole, HinemosUnknown {
		return getMonitorList(HinemosModuleConstant.MONITOR_PING);
	}

	/**
	 * port監視一覧リストを返します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getPortList() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		return getMonitorList(HinemosModuleConstant.MONITOR_PORT);
	}

	/**
	 * プロセス監視一覧リストを取得します。<BR>
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getProcessList() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		return getMonitorList(HinemosModuleConstant.MONITOR_PROCESS);
	}

	/**
	 * SNMPTRAP監視一覧リストを取得します。<BR>
	 *
	 * @return rrayList<MonitorInfo>
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getTrapList() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		return getMonitorList(HinemosModuleConstant.MONITOR_SNMPTRAP);
	}

	/**
	 * SNMP監視一覧リストを取得します。<BR>
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getSnmpList() throws MonitorNotFound, InvalidRole, HinemosUnknown {
		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		list.addAll(getMonitorList(HinemosModuleConstant.MONITOR_SNMP_N));
		list.addAll(getMonitorList(HinemosModuleConstant.MONITOR_SNMP_S));
		return list;
	}

	/**
	 * SQL監視一覧リストを取得します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getSqlList() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		list.addAll(getMonitorList(HinemosModuleConstant.MONITOR_SQL_N));
		list.addAll(getMonitorList(HinemosModuleConstant.MONITOR_SQL_S));
		return list;
	}

	/**
	 * システムログ監視一覧リストを返します。
	 *
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getSystemlogList() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		return getMonitorList(HinemosModuleConstant.MONITOR_SYSTEMLOG);
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
	 * 既存のコマンド監視の一覧を返す
	 * @return コマンド監視の設定情報一覧
	 * @throws MonitorNotFound 一覧にIDが存在するが、詳細情報が存在しなかった場合
	 * @throws HinemosUnknown 予期せぬエラーが発生した場合
	 */
	public ArrayList<MonitorInfo> getCustomList() throws MonitorNotFound, InvalidRole, HinemosUnknown {
		ArrayList<MonitorInfo> list = new ArrayList<>();
		list.addAll(getMonitorList(HinemosModuleConstant.MONITOR_CUSTOM_N));
		list.addAll(getMonitorList(HinemosModuleConstant.MONITOR_CUSTOM_S));
		return list;
	}


	/**
	 * Windowsサービス監視一覧リストを取得します。<BR>
	 *
	 * @return MonitorInfoのリスト
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getWinServiceList() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		return getMonitorList(HinemosModuleConstant.MONITOR_WINSERVICE);
	}

	/**
	 * Windowsイベント監視一覧リストを取得します。<BR>
	 *
	 * @return MonitorInfoのリスト
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getWinEventList() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		return getMonitorList(HinemosModuleConstant.MONITOR_WINEVENT);
	}
	/**
	 * カスタムトラップ監視一覧リストを取得します。<BR>
	 *
	 * @return MonitorInfoのリスト
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getCustomTrapList() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		ArrayList<MonitorInfo> list = new ArrayList<>();
		list.addAll(getMonitorList(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N));
		list.addAll(getMonitorList(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S));
		return list;
	}

	/**
	 * 監視一覧リストを返します。
	 *
	 * @param monitorTypeId 監視種別ID
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	private ArrayList<MonitorInfo> getMonitorList(String monitorTypeId) throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;

		// 監視一覧を取得
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = new SelectMonitor().getMonitorList(monitorTypeId);
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
	 * @param monitorId
	 * @param monitorTypeId
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 */
	public void setStatusMonitor(String monitorId, String monitorTypeId, boolean validFlag) throws HinemosUnknown, MonitorNotFound, InvalidRole {
		// null check
		if(monitorId == null || "".equals(monitorId)){
			HinemosUnknown e = new HinemosUnknown("target monitorId is null or empty.");
			m_log.info("setStatusMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if(monitorTypeId == null || "".equals(monitorTypeId)){
			HinemosUnknown e = new HinemosUnknown("target monitorTypeId is null or empty.");
			m_log.info("setStatusMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		MonitorInfo info = getMonitor(monitorId);
		try{
			// オブジェクト権限チェック
			QueryUtil.getMonitorInfoPK(monitorId, ObjectPrivilegeMode.MODIFY);

			if (validFlag) {
				if(!info.getMonitorFlg()){
					info.setMonitorFlg(true);
					modifyMonitor(info);
				}
			} else {
				if(info.getMonitorFlg()){
					info.setMonitorFlg(false);
					modifyMonitor(info);
				}
			}
		} catch (MonitorNotFound | InvalidRole | HinemosUnknown e) {
			throw e;
		} catch (InvalidSetting e) {
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		}
	}

	/**
	 *
	 * 監視設定の収集を有効化/無効化します。<BR>
	 *
	 * @param monitorId
	 * @param monitorTypeId
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 */
	public void setStatusCollector(String monitorId, String monitorTypeId, boolean validFlag) throws HinemosUnknown, MonitorNotFound, InvalidRole {
		// null check
		if(monitorId == null || "".equals(monitorId)){
			HinemosUnknown e = new HinemosUnknown("target monitorId is null or empty.");
			m_log.info("setStatusCollector() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if(monitorTypeId == null || "".equals(monitorTypeId)){
			HinemosUnknown e = new HinemosUnknown("target monitorTypeId is null or empty.");
			m_log.info("setStatusCollector() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		MonitorInfo info = getMonitor(monitorId);
		try{
			// オブジェクト権限チェック
			QueryUtil.getMonitorInfoPK(monitorId, ObjectPrivilegeMode.MODIFY);
			// 収集蓄積機能実装に伴い、文字列監視やトラップ監視も収集可能にする
			if(validFlag) {
				if(info.getMonitorType() == MonitorTypeConstant.TYPE_TRUTH) {
					m_log.debug("setStatusMonitor() : monitorId = " + monitorId + " is truth.");
					return;
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
		} catch (MonitorNotFound | InvalidRole | HinemosUnknown e) {
			throw e;
		} catch (InvalidSetting e) {
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		}
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
		m_log.debug("getMonitorListWithoutCheckInfo(condition) : start");
		
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
			
			for(MonitorInfo info : list) {
				jtm.getEntityManager().detach(info);
				info.setCustomCheckInfo(null);
				info.setCustomTrapCheckInfo(null);
				info.setHttpCheckInfo(null);
				info.setHttpScenarioCheckInfo(null);
				info.setJmxCheckInfo(null);
				info.setLogfileCheckInfo(null);
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
			
			jtm.commit();
		} catch (HinemosUnknown | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (MonitorNotFound e) {
			m_log.info("getMonitorListWithoutCheckInfo(condition) " + e.getClass().getName() + ", " + e.getMessage());
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMonitorListWithoutCheckInfo(condition) : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		m_log.debug("getMonitorListWithoutCheckInfo(condition) : end");
		return list;
	}
}
