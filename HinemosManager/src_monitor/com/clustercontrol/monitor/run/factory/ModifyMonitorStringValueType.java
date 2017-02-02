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
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfoPK;
import com.clustercontrol.monitor.run.util.QueryUtil;

/**
 * 文字列監視の判定情報を変更する抽象クラス<BR>
 * <p>
 * 文字列監視を行う各監視管理クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.1.0
 */
abstract public class ModifyMonitorStringValueType extends ModifyMonitor{

	/**
	 * 文字列監視の判定情報を作成し、監視情報に設定します。
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorStringValueInfoBean
	 */
	@Override
	protected boolean addJudgementInfo() throws MonitorNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		// 文字列監視判定情報を設定
		List<MonitorStringValueInfo> list = m_monitorInfo.getStringValueInfo();
		if (list != null) {
			for(int index = 0; index < list.size(); index++){
				MonitorStringValueInfo value = list.get(index);
				value.setOrderNo(index + 1);
				em.persist(value);
				value.relateToMonitorInfo(m_monitorInfo);
			}
		}
		return true;
	}
	
	/**
	 * 監視情報より文字列監視の判定情報を取得し、変更します。
	 * <p>
	 * <ol>
	 * <li>監視情報より判定情報を削除します。</li>
	 * <li>判定情報を作成し、監視情報に設定します。</li>
	 * </ol>
	 * @throws MonitorNotFound
	 * @throws EntityExistsException
	 * @throws InvalidRole
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorStringValueInfoBean
	 */
	@Override
	protected boolean modifyJudgementInfo() throws MonitorNotFound, EntityExistsException, InvalidRole {

		MonitorInfo monitorInfo = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// 文字列監視判定情報を設定
		List<MonitorStringValueInfo> valueList = m_monitorInfo.getStringValueInfo();
		if(valueList == null){
			return true;
		}

		List<MonitorStringValueInfoPK> monitorStringValueInfoEntityPkList = new ArrayList<MonitorStringValueInfoPK>();

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		int orderNo = 0;
		for(MonitorStringValueInfo value : valueList){
			if(value != null){
				MonitorStringValueInfo entity = null;
				MonitorStringValueInfoPK entityPk = new MonitorStringValueInfoPK(
						m_monitorInfo.getMonitorId(),
						Integer.valueOf(++orderNo));
				try {
					entity = QueryUtil.getMonitorStringValueInfoPK(entityPk);
				} catch (MonitorNotFound e) {
					entity = new MonitorStringValueInfo(entityPk);
					em.persist(entity);
					entity.relateToMonitorInfo(monitorInfo);
				}
				entity.setCaseSensitivityFlg(value.getCaseSensitivityFlg());
				entity.setDescription(value.getDescription());
				entity.setMessage(value.getMessage());
				entity.setPattern(value.getPattern());
				entity.setPriority(value.getPriority());
				entity.setProcessType(value.getProcessType());
				entity.setValidFlg(value.getValidFlg());
				monitorStringValueInfoEntityPkList.add(entityPk);
			}
		}
		// 不要なMonitorStringValueInfoEntityを削除
		monitorInfo.deleteMonitorStringValueInfoEntities(monitorStringValueInfoEntityPkList);

		return true;
	}

}
