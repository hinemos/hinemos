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

import javax.persistence.EntityExistsException;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfoPK;
import com.clustercontrol.monitor.run.util.QueryUtil;

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
	protected boolean addJudgementInfo() throws MonitorNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		for(MonitorNumericValueInfo value: m_monitorInfo.getNumericValueInfo()){
			em.persist(value);
			value.relateToMonitorInfo(m_monitorInfo);
		}
		return true;
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
	protected boolean modifyJudgementInfo() throws MonitorNotFound, EntityExistsException, InvalidRole {

		MonitorInfo monitorInfo = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// 数値監視判定情報を設定
		List<MonitorNumericValueInfo> valueList = m_monitorInfo.getNumericValueInfo();

		List<MonitorNumericValueInfoPK> monitorNumericValueInfoEntityPkList = new ArrayList<MonitorNumericValueInfoPK>();

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		for(MonitorNumericValueInfo value : valueList){
			if(value != null){
				MonitorNumericValueInfoPK entityPk = new MonitorNumericValueInfoPK(
						m_monitorInfo.getMonitorId(),
						value.getPriority());
				MonitorNumericValueInfo entity = null;
				try {
					entity = QueryUtil.getMonitorNumericValueInfoPK(entityPk);
				} catch (MonitorNotFound e) {
					// 新規登録
					entity = value;
					em.persist(entity);
					entity.relateToMonitorInfo(monitorInfo);
				}
				entity.setMessage(value.getMessage());
				entity.setThresholdLowerLimit(value.getThresholdLowerLimit());
				entity.setThresholdUpperLimit(value.getThresholdUpperLimit());
				monitorNumericValueInfoEntityPkList.add(entityPk);
			}
		}
		// 不要なMonitorNumericValueInfoEntityを削除
		monitorInfo.deleteMonitorNumericValueInfoEntities(monitorNumericValueInfoEntityPkList);

		return true;
	}

}
