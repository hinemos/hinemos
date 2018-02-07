/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winevent.factory;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitorStringValueType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.winevent.model.MonitorWinEventCategoryInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventCategoryInfoEntityPK;
import com.clustercontrol.winevent.model.MonitorWinEventIdInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventIdInfoEntityPK;
import com.clustercontrol.winevent.model.MonitorWinEventKeywordInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventKeywordInfoEntityPK;
import com.clustercontrol.winevent.model.MonitorWinEventLogInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventLogInfoEntityPK;
import com.clustercontrol.winevent.model.MonitorWinEventSourceInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventSourceInfoEntityPK;
import com.clustercontrol.winevent.model.WinEventCheckInfo;

/**
 * Windowsイベント監視情報を変更するクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 * 
 */
public class ModifyMonitorWinEvent extends ModifyMonitorStringValueType{

	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// Windowsイベント監視設定を新規登録する
			WinEventCheckInfo checkInfo = m_monitorInfo.getWinEventCheckInfo();
			
			checkInfo.setMonitorId(m_monitorInfo.getMonitorId());
			em.persist(checkInfo);
			
			for(MonitorWinEventLogInfoEntity logName : checkInfo.getMonitorWinEventLogInfoEntities()){
				em.persist(logName);
				logName.relateToMonitorWinEventInfoEntity(checkInfo);
			}

			for(MonitorWinEventSourceInfoEntity sourceName : checkInfo.getMonitorWinEventSourceInfoEntities()){
				em.persist(sourceName);
				sourceName.relateToMonitorWinEventInfoEntity(checkInfo);
			}

			for(MonitorWinEventIdInfoEntity eventId : checkInfo.getMonitorWinEventIdInfoEntities()){
				em.persist(eventId);
				eventId.relateToMonitorWinEventInfoEntity(checkInfo);
			}

			for(MonitorWinEventCategoryInfoEntity categoryNumber : checkInfo.getMonitorWinEventCategoryInfoEntities()){
				em.persist(categoryNumber);
				categoryNumber.relateToMonitorWinEventInfoEntity(checkInfo);
			}

			for(MonitorWinEventKeywordInfoEntity keywordNumber : checkInfo.getMonitorWinEventKeywordInfoEntities()){
				em.persist(keywordNumber);
				keywordNumber.relateToMonitorWinEventInfoEntity(checkInfo);
			}

			return true;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			WinEventCheckInfo checkInfo = m_monitorInfo.getWinEventCheckInfo();

			// Windowsイベント監視設定を更新する
			WinEventCheckInfo entity = com.clustercontrol.winevent.util.QueryUtil.getMonitorWinEventInfoPK(m_monitorInfo.getMonitorId());
			entity.setLevelCritical(checkInfo.isLevelCritical());
			entity.setLevelWarning(checkInfo.isLevelWarning());
			entity.setLevelVerbose(checkInfo.isLevelVerbose());
			entity.setLevelError(checkInfo.isLevelError());
			entity.setLevelInformational(checkInfo.isLevelInformational());

			// 関連するlogName, source, eventId, categoryを一旦削除
			for(MonitorWinEventLogInfoEntity info : entity.getMonitorWinEventLogInfoEntities()){
				info.relateToMonitorWinEventInfoEntity(null);
			}
			for(MonitorWinEventSourceInfoEntity info : entity.getMonitorWinEventSourceInfoEntities()){
				info.relateToMonitorWinEventInfoEntity(null);
			}
			for(MonitorWinEventIdInfoEntity info : entity.getMonitorWinEventIdInfoEntities()){
				info.relateToMonitorWinEventInfoEntity(null);
			}
			for(MonitorWinEventCategoryInfoEntity info : entity.getMonitorWinEventCategoryInfoEntities()){
				info.relateToMonitorWinEventInfoEntity(null);
			}
			for(MonitorWinEventKeywordInfoEntity info : entity.getMonitorWinEventKeywordInfoEntities()){
				info.relateToMonitorWinEventInfoEntity(null);
			}
			entity.setMonitorWinEventLogInfoEntities(new ArrayList<MonitorWinEventLogInfoEntity>());
			entity.setMonitorWinEventSourceInfoEntities(new ArrayList<MonitorWinEventSourceInfoEntity>());
			entity.setMonitorWinEventIdInfoEntities(new ArrayList<MonitorWinEventIdInfoEntity>());
			entity.setMonitorWinEventCategoryInfoEntities(new ArrayList<MonitorWinEventCategoryInfoEntity>());
			entity.setMonitorWinEventKeywordInfoEntities(new ArrayList<MonitorWinEventKeywordInfoEntity>());
			com.clustercontrol.winevent.util.QueryUtil.deleteRelatedEntitiesByMonitorid(m_monitorInfo.getMonitorId());
			
			
			// 関連するlogName, source, eventId, categoryを再作成
			List<MonitorWinEventLogInfoEntity> logs = new ArrayList<MonitorWinEventLogInfoEntity>();
			for(String logName : checkInfo.getLogName()){
				MonitorWinEventLogInfoEntity log = new MonitorWinEventLogInfoEntity(new MonitorWinEventLogInfoEntityPK(m_monitorInfo.getMonitorId(), logName));
				em.persist(log);
				log.relateToMonitorWinEventInfoEntity(checkInfo);
				logs.add(log);
			}
			entity.setMonitorWinEventLogInfoEntities(logs);

			List<MonitorWinEventSourceInfoEntity> sources = new ArrayList<MonitorWinEventSourceInfoEntity>();
			for(String sourceName : checkInfo.getSource()){
				MonitorWinEventSourceInfoEntity source = new MonitorWinEventSourceInfoEntity(new MonitorWinEventSourceInfoEntityPK(m_monitorInfo.getMonitorId(), sourceName));
				em.persist(source);
				source.relateToMonitorWinEventInfoEntity(checkInfo);
				sources.add(source);
			}
			entity.setMonitorWinEventSourceInfoEntities(sources);

			List<MonitorWinEventIdInfoEntity> ids = new ArrayList<MonitorWinEventIdInfoEntity>();
			for(Integer eventId : checkInfo.getEventId()){
				MonitorWinEventIdInfoEntity id = new MonitorWinEventIdInfoEntity(new MonitorWinEventIdInfoEntityPK(m_monitorInfo.getMonitorId(), eventId));
				em.persist(id);
				id.relateToMonitorWinEventInfoEntity(checkInfo);
				ids.add(id);
			}
			entity.setMonitorWinEventIdInfoEntities(ids);

			List<MonitorWinEventCategoryInfoEntity> categories = new ArrayList<MonitorWinEventCategoryInfoEntity>();
			for(Integer categoryNumber : checkInfo.getCategory()){
				MonitorWinEventCategoryInfoEntity category = new MonitorWinEventCategoryInfoEntity(new MonitorWinEventCategoryInfoEntityPK(m_monitorInfo.getMonitorId(), categoryNumber));
				em.persist(category);
				category.relateToMonitorWinEventInfoEntity(checkInfo);
				categories.add(category);
			}
			entity.setMonitorWinEventCategoryInfoEntities(categories);

			List<MonitorWinEventKeywordInfoEntity> keywords = new ArrayList<MonitorWinEventKeywordInfoEntity>();
			for(Long keywordNumber : checkInfo.getKeywords()){
				MonitorWinEventKeywordInfoEntity keyword = new MonitorWinEventKeywordInfoEntity(new MonitorWinEventKeywordInfoEntityPK(m_monitorInfo.getMonitorId(), keywordNumber));
				em.persist(keyword);
				keyword.relateToMonitorWinEventInfoEntity(checkInfo);
				keywords.add(keyword);
			}
			entity.setMonitorWinEventKeywordInfoEntities(keywords);

			return true;
		}
	}

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		return 0;
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.NONE;
	}
}
