/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.plugin.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.UserIdConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.maintenance.factory.ModifySchedule;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.model.MaintenanceTypeMst;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.repository.model.CollectorSubPlatformMstEntity;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.rpa.model.RpaManagementToolTypeMst;
import com.clustercontrol.rpa.scenario.session.ScenarioOperationResultUpdater;
import com.clustercontrol.rpa.session.RpaResourceMonitor;
import com.clustercontrol.rpa.util.QueryUtil;
import com.clustercontrol.rpa.util.RpaConstants;
import com.clustercontrol.rpa.util.RpaUtil;
import com.clustercontrol.util.HinemosTime;

public class RpaPlugin implements HinemosPlugin {
	public static final Log m_log = LogFactory.getLog(RpaPlugin.class);

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(SchedulerPlugin.class.getName());
		dependency.add(Log4jReloadPlugin.class.getName());
		dependency.add(CacheInitializerPlugin.class.getName());
		return dependency;
	}

	@Override
	public Set<String> getRequiredKeys() {
		return null;
	}

	@Override
	public void create() {}

	@Override
	public void activate() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();
			long now = HinemosTime.currentTimeMillis();

			// RPA管理ツールのミドルウェア監視を登録する。
			List<MonitorInfo> middlewareMonitorList = RpaUtil.createBuiltInMiddlewareMonitor();
			for (MonitorInfo middlewareMonitor : middlewareMonitorList) {
				try {
					com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_NONE(middlewareMonitor.getMonitorId());
				} catch (MonitorNotFound e) {
					// ミドルウェア監視が登録されていない場合、登録する。
					middlewareMonitor.setRegDate(now);
					middlewareMonitor.setUpdateDate(now);
					em.persist(middlewareMonitor);
					em.flush();
					// オブジェクト権限を登録
					RpaUtil.createBuiltInMiddlewareMonitorObjectPrivilege(middlewareMonitor.getMonitorId()).stream().forEach(info -> {
						info.setCreateDate(now);
						info.setModifyDate(now);
						em.persist(info);
					});					
				}
			}
			
			// RPAシナリオ実績の履歴削除設定を登録する。
			MaintenanceTypeMst maintenanceTypeMst = RpaUtil.createBuiltInRpaMaintenanceTypeMst();
			try {
				com.clustercontrol.maintenance.util.QueryUtil.getMaintenanceTypeMstPK(maintenanceTypeMst.getType_id());
			} catch (MaintenanceNotFound e) {
				// 履歴削除種別が登録されていない場合、登録する。
				em.persist(maintenanceTypeMst);
				em.flush();
			}
			
			
			List<MaintenanceInfo> maintenanceInfoList = RpaUtil.createBuiltInRpaMaintenance();
			for (MaintenanceInfo maintenanceInfo : maintenanceInfoList) {
				try {
					com.clustercontrol.maintenance.util.QueryUtil.getMaintenanceInfoPK(maintenanceInfo.getMaintenanceId(), ObjectPrivilegeMode.NONE);
				} catch (MaintenanceNotFound e) {
					// 履歴削除設定が登録されていない場合、登録する。
					MaintenanceTypeMst typeMst = com.clustercontrol.maintenance.util.QueryUtil.getMaintenanceTypeMstPK(maintenanceInfo.getTypeId());
					maintenanceInfo.relateToMaintenanceTypeMstEntity(typeMst);
					maintenanceInfo.setRegDate(now);
					maintenanceInfo.setUpdateDate(now);
					em.persist(maintenanceInfo);
					
					// スケジューラ登録
					new ModifySchedule().addSchedule(maintenanceInfo, UserIdConstant.HINEMOS);
				}
			}

			synchronized( FacilityTreeCache.class ){ //FacilityTreeCache更新との排他制御
				// RPA組み込みスコープが存在しない場合、作成する。
				try { 
					com.clustercontrol.repository.util.QueryUtil.getScopePK(RpaConstants.RPA); 
				} catch (FacilityNotFound e) {
					RpaUtil.createBuiltInRpaScopes().stream().forEach(info -> {
						info.setCreateDatetime(now);
						info.setModifyDatetime(now);
						em.persist(info);
					});
					em.flush();
					RpaUtil.createBuiltInRpaRelation().stream().forEach(em::persist);
					RpaUtil.createBuiltInRpaScopeObjectPrivilege().stream().forEach(info -> {
						info.setCreateDate(now);
						info.setModifyDate(now);
						em.persist(info);
					});
				}
			}
			
			jtm.commit();
			FacilityTreeCache.refresh();
		} catch (HinemosUnknown | InvalidRole | MaintenanceNotFound e) {
			m_log.error(e);
		}

		// RPAリソースの自動検知開始
		 RpaResourceMonitor.start();
		 
		 // シナリオ実績更新の非同期実行開始
		 ScenarioOperationResultUpdater.start();
	}

	@Override
	public void deactivate() {}

	@Override
	public void destroy() {}
	
}
