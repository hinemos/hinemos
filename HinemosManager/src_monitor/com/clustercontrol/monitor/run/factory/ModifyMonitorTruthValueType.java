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
		List<MonitorTruthValueInfo> valueList = m_monitorInfo.getTruthValueInfo();

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		// 真偽値監視判定情報を設定
		MonitorTruthValueInfo value = null;
		for(int index=0; index<valueList.size(); index++){
			value = valueList.get(index);
			if(value != null){
				em.persist(value);
				value.relateToMonitorInfo(m_monitorInfo);
			}
		}
		return true;
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

		// 真偽値監視判定情報を設定
		List<MonitorTruthValueInfo> valueList = m_monitorInfo.getTruthValueInfo();
		MonitorInfo monitorInfo = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());
		List<MonitorTruthValueInfoPK> monitorTruthValueInfoEntityPkList = new ArrayList<MonitorTruthValueInfoPK>();

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
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

		return true;
	}

}
