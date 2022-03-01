/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.factory;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.bean.FunctionPrefixEnum;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.monitor.run.bean.MonitorNumericType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfoPK;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCacheRefreshCallback;
import com.clustercontrol.monitor.run.util.MonitorJudgementInfoCacheRefreshCallback;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.factory.ModifyNotifyRelation;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;

import jakarta.persistence.EntityExistsException;

/**
 * 数値監視の判定情報を変更する抽象クラス<BR>
 * <p>
 * 数値監視を行う各監視管理クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
abstract public class ModifyMonitorNumericValueType extends ModifyMonitor{
	/**
	 * 数値監視の判定情報を作成し、監視情報に設定します。
	 * 
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorNumericValueInfoBean
	 */
	@Override
	protected boolean addJudgementInfo() throws MonitorNotFound, InvalidRole, HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			for(MonitorNumericValueInfo value: m_monitorInfo.getNumericValueInfo()){
				if (!MonitorNumericType.TYPE_BASIC.getType().equals(value.getMonitorNumericType())
						&& !MonitorNumericType.TYPE_CHANGE.getType().equals(value.getMonitorNumericType())) {
					// 対象外データ
					continue;
				}
				value.setMonitorId(m_monitorInfo.getMonitorId());
				em.persist(value);
				value.relateToMonitorInfo(m_monitorInfo);
			}

			// 判定キャッシュを更新
			jtm.addCallback(new MonitorJudgementInfoCacheRefreshCallback(
					m_monitorInfo.getMonitorId(), m_monitorInfo.getMonitorType(), null, null, m_monitorInfo.getNumericValueInfo()));

			// 通知情報（将来予測用）の登録
			if (m_monitorInfo.getPredictionNotifyRelationList() != null
					&& m_monitorInfo.getPredictionNotifyRelationList().size() > 0) {
				for (NotifyRelationInfo predictionNotifyRelationInfo : m_monitorInfo.getPredictionNotifyRelationList()) {
					predictionNotifyRelationInfo.setNotifyGroupId(
							CollectMonitorManagerUtil.getPredictionNotifyGroupId(m_monitorInfo.getNotifyGroupId()));
					predictionNotifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.PREDICTION.name());
				}
				// 通知情報(将来予測用)を登録
				new ModifyNotifyRelation().add(m_monitorInfo.getPredictionNotifyRelationList(), m_monitorInfo.getOwnerRoleId());
			}
			// 通知情報（変化点用）の登録
			if (m_monitorInfo.getChangeNotifyRelationList() != null
					&& m_monitorInfo.getChangeNotifyRelationList().size() > 0) {
				for (NotifyRelationInfo changeNotifyRelationInfo : m_monitorInfo.getChangeNotifyRelationList()) {
					changeNotifyRelationInfo.setNotifyGroupId(
							CollectMonitorManagerUtil.getChangeNotifyGroupId(m_monitorInfo.getNotifyGroupId()));
					changeNotifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.CHANGE.name());
				}
				// 通知情報(将来予測用)を登録
				new ModifyNotifyRelation().add(m_monitorInfo.getChangeNotifyRelationList(), m_monitorInfo.getOwnerRoleId());
			}
			
			
			// この監視で収集値を保持しなければいけない期間を更新する
			int range = m_monitorInfo.getPredictionAnalysysRange();
			if (range < m_monitorInfo.getChangeAnalysysRange()) {
				range = m_monitorInfo.getChangeAnalysysRange();
			}
			// この監視で収集値を保持しなければいけない期間を更新する
			jtm.addCallback(new MonitorCollectDataCacheRefreshCallback(
					m_monitorInfo.getMonitorId(), m_monitorInfo.getMonitorTypeId(), range));
			
			return true;
		}
	}
	
	/**
	 * 監視情報より数値監視の判定情報を取得し、変更します。
	 * <p>
	 * <ol>
	 * <li>監視情報より判定情報を削除します。</li>
	 * <li>判定情報を作成し、監視情報に設定します。</li>
	 * </ol>
	 * 
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorNumericValueInfoBean
	 */
	@Override
	protected boolean modifyJudgementInfo() throws MonitorNotFound, EntityExistsException, InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			MonitorInfo monitorInfo = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

			// 数値監視判定情報を設定
			List<MonitorNumericValueInfo> valueList = m_monitorInfo.getNumericValueInfo();

			List<MonitorNumericValueInfoPK> monitorNumericValueInfoEntityPkList = new ArrayList<MonitorNumericValueInfoPK>();

			for(MonitorNumericValueInfo value : valueList){
				if(value == null){
					continue;
				}
				if (!MonitorNumericType.TYPE_BASIC.getType().equals(value.getMonitorNumericType())
						&& !MonitorNumericType.TYPE_CHANGE.getType().equals(value.getMonitorNumericType())) {
					// 対象外データ
					continue;
				}
				MonitorNumericValueInfoPK entityPk = new MonitorNumericValueInfoPK(
						m_monitorInfo.getMonitorId(),
						value.getMonitorNumericType(),
						value.getPriority());
				MonitorNumericValueInfo entity = null;
				try {
					entity = QueryUtil.getMonitorNumericValueInfoPK(entityPk);
				} catch (MonitorNotFound e) {
					// 新規登録
					entity = value;
					entity.setMonitorId(m_monitorInfo.getMonitorId());
					em.persist(entity);
					entity.relateToMonitorInfo(monitorInfo);
				}
				entity.setMessage(value.getMessage());
				entity.setThresholdLowerLimit(value.getThresholdLowerLimit());
				entity.setThresholdUpperLimit(value.getThresholdUpperLimit());
				monitorNumericValueInfoEntityPkList.add(entityPk);
			}
			// 不要なMonitorNumericValueInfoEntityを削除
			monitorInfo.deleteMonitorNumericValueInfoEntities(monitorNumericValueInfoEntityPkList);

			// 判定キャッシュを更新
			jtm.addCallback(new MonitorJudgementInfoCacheRefreshCallback(
					m_monitorInfo.getMonitorId(), m_monitorInfo.getMonitorType(), null, null, m_monitorInfo.getNumericValueInfo()));

			// 通知情報（将来予測用）を更新
			try {
				String predictionNotifyGroupId = CollectMonitorManagerUtil.getPredictionNotifyGroupId(m_monitor.getNotifyGroupId());
				if (m_monitorInfo.getPredictionNotifyRelationList() != null
						&& m_monitorInfo.getPredictionNotifyRelationList().size() > 0) {
					for (NotifyRelationInfo predictionNotifyRelationInfo : m_monitorInfo.getPredictionNotifyRelationList()) {
						predictionNotifyRelationInfo.setNotifyGroupId(
								predictionNotifyGroupId);
						predictionNotifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.PREDICTION.name());
					}
				}
				new NotifyControllerBean().modifyNotifyRelation(
						m_monitorInfo.getPredictionNotifyRelationList(), predictionNotifyGroupId, m_monitorInfo.getOwnerRoleId());
		
				// 通知情報（変化点監視用）を更新
				String changeNotifyGroupId = CollectMonitorManagerUtil.getChangeNotifyGroupId(m_monitor.getNotifyGroupId());
				if (m_monitorInfo.getChangeNotifyRelationList() != null
						&& m_monitorInfo.getChangeNotifyRelationList().size() > 0) {
					for (NotifyRelationInfo changeNotifyRelationInfo : m_monitorInfo.getChangeNotifyRelationList()) {
						changeNotifyRelationInfo.setNotifyGroupId(changeNotifyGroupId);
						changeNotifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.CHANGE.name());
					}
				}
				new NotifyControllerBean().modifyNotifyRelation(
						m_monitorInfo.getChangeNotifyRelationList(), changeNotifyGroupId, m_monitor.getOwnerRoleId());
			} catch (NotifyNotFound e) {
				throw new MonitorNotFound(e.getMessage(), e);
			}
			
			
			// この監視で収集値を保持しなければいけない期間を更新する
			int range = m_monitorInfo.getPredictionAnalysysRange();
			if (range < m_monitorInfo.getChangeAnalysysRange()) {
				range = m_monitorInfo.getChangeAnalysysRange();
			}
			jtm.addCallback(new MonitorCollectDataCacheRefreshCallback(
					m_monitorInfo.getMonitorId(), m_monitorInfo.getMonitorTypeId(), range));

			m_monitor.setPredictionFlg(m_monitorInfo.getPredictionFlg());
			m_monitor.setPredictionMethod(m_monitorInfo.getPredictionMethod());
			m_monitor.setPredictionAnalysysRange(m_monitorInfo.getPredictionAnalysysRange());
			m_monitor.setPredictionTarget(m_monitorInfo.getPredictionTarget());
			m_monitor.setPredictionApplication(m_monitorInfo.getPredictionApplication());
			m_monitor.setChangeFlg(m_monitorInfo.getChangeFlg());
			m_monitor.setChangeAnalysysRange(m_monitorInfo.getChangeAnalysysRange());
			m_monitor.setChangeApplication(m_monitorInfo.getChangeApplication());

			return true;
		}
	}

}
