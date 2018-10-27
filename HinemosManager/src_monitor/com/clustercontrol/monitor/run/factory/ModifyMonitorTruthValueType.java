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

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfo;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfoPK;
import com.clustercontrol.monitor.run.util.MonitorJudgementInfoCacheRefreshCallback;
import com.clustercontrol.monitor.run.util.QueryUtil;

/**
 * 真偽値監視の判定情報を変更する抽象クラス<BR>
 * <p>
 * 真偽値監視を行う各監視管理クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
abstract public class ModifyMonitorTruthValueType extends ModifyMonitor{
	/**
	 * 真偽値監視の判定情報を作成し、監視情報に設定します。
	 * 
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorTruthValueInfoBean
	 */
	@Override
	protected boolean addJudgementInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorTruthValueInfo> valueList = m_monitorInfo.getTruthValueInfo();

			// 真偽値監視判定情報を設定
			MonitorTruthValueInfo value = null;
			for(int index=0; index<valueList.size(); index++){
				value = valueList.get(index);
				if(value != null){
					em.persist(value);
					value.relateToMonitorInfo(m_monitorInfo);
				}
			}

			// 判定キャッシュを更新
			jtm.addCallback(new MonitorJudgementInfoCacheRefreshCallback(
					m_monitorInfo.getMonitorId(), m_monitorInfo.getMonitorType(), null, m_monitorInfo.getTruthValueInfo(), null));

			return true;
		}
	}
	
	/**
	 * 監視情報より真偽値監視の判定情報を取得し、変更します。
	 * <p>
	 * <ol>
	 * <li>監視情報より判定情報を削除します。</li>
	 * <li>判定情報を作成し、監視情報に設定します。</li>
	 * </ol>
	 * 
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorTruthValueInfoBean
	 */
	@Override
	protected boolean modifyJudgementInfo() throws MonitorNotFound, InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 真偽値監視判定情報を設定
			List<MonitorTruthValueInfo> valueList = m_monitorInfo.getTruthValueInfo();
			MonitorInfo monitorInfo = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());
			List<MonitorTruthValueInfoPK> monitorTruthValueInfoEntityPkList = new ArrayList<MonitorTruthValueInfoPK>();

			for(MonitorTruthValueInfo value : valueList){
				if(value != null){
					MonitorTruthValueInfo entity = null;
					MonitorTruthValueInfoPK entityPk = new MonitorTruthValueInfoPK(
							m_monitorInfo.getMonitorId(),
							value.getPriority(),
							value.getTruthValue());
					try {
						entity = QueryUtil.getMonitorTruthValueInfoPK(entityPk);
					} catch (MonitorNotFound e) {
						// 新規登録
						entity = new MonitorTruthValueInfo(entityPk);
						em.persist(entity);
						entity.relateToMonitorInfo(monitorInfo);
					}
					entity.setMessage(value.getMessage());
					monitorTruthValueInfoEntityPkList.add(entityPk);
				}
			}
			// 不要なMonitorTruthValueInfoEntityを削除
			monitorInfo.deleteMonitorTruthValueInfoEntities(monitorTruthValueInfoEntityPkList);

			// 判定キャッシュを更新
			jtm.addCallback(new MonitorJudgementInfoCacheRefreshCallback(
					m_monitorInfo.getMonitorId(), m_monitorInfo.getMonitorType(), null, m_monitorInfo.getTruthValueInfo(), null));

			return true;
		}
	}

}
