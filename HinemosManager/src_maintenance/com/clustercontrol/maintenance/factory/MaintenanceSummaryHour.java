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

package com.clustercontrol.maintenance.factory;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;

/**
 * 性能実績の削除処理
 *
 * @version 5.1.0
 * @since 5.1.0
 *
 */
public class MaintenanceSummaryHour extends MaintenanceObject {

	private static Log m_log = LogFactory.getLog( MaintenanceSummaryHour.class );
	
	/**
	 * 削除処理
	 */
	@Override
	protected int _delete(Long boundary, boolean status, String ownerRoleId){
		int ret = 0;
		JpaTransactionManager jtm = null;
		try{
			//AdminRoleの場合は監視IDを条件にせず、全て削除
			if(RoleIdConstant.isAdministratorRole(ownerRoleId)){
				jtm = new JpaTransactionManager();
				jtm.begin();
				ret = delete(boundary, status);
				jtm.commit();
				
			}else{
				ArrayList<MonitorInfo> monitorList = new MonitorSettingControllerBean().getMonitorList();
				
				ArrayList<String> monitorIdList = new ArrayList<>();
				
				for (MonitorInfo monitorInfo : monitorList) {
					if (RoleIdConstant.isAdministratorRole(ownerRoleId) 
							|| monitorInfo.getOwnerRoleId().equals(ownerRoleId)) {
						monitorIdList.add(monitorInfo.getMonitorId());
					}
				}
				if (monitorIdList.isEmpty()) {
					return -1;
				}
				jtm = new JpaTransactionManager();
				jtm.begin();
				
				for(int i = 0; i < monitorIdList.size(); i++){
					ret += delete(boundary, status, monitorIdList.get(i));
					jtm.commit();
				}
			}
		} catch(Exception e){
			m_log.warn("deleteCollectData() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}
	
	protected int delete(Long boundary, boolean status, String monitorId) {
		m_log.debug("_delete() start : status = " + status);

		int ret = -1;
		
		//SQL文の実行
		//for HA (縮退判定時間を延ばすため)、シングルには影響なし(0)：タイムアウト値設定
		ret  = QueryUtil.deleteSummaryHourByDateTimeAndMonitorId(
				boundary, HinemosPropertyUtil.getHinemosPropertyNum(_QUERY_TIMEOUT_KEY, Long.valueOf(0)).intValue(), monitorId);
		
		//終了
		m_log.debug("_delete() count : " + ret);
		return ret;
	}

	protected int delete(Long boundary, boolean status) {
		m_log.debug("_delete() start : status = " + status);

		int ret = -1;
		
		//SQL文の実行
		//for HA (縮退判定時間を延ばすため)、シングルには影響なし(0)：タイムアウト値設定
		ret  = QueryUtil.deleteSummaryHourByDateTime(
				boundary, HinemosPropertyUtil.getHinemosPropertyNum(_QUERY_TIMEOUT_KEY, Long.valueOf(0)).intValue());
		
		//終了
		m_log.debug("_delete() count : " + ret);
		return ret;
	}
}
