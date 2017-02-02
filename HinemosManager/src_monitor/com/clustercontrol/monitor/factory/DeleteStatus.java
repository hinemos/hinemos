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

package com.clustercontrol.monitor.factory;

import java.util.ArrayList;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.bean.StatusDataInfo;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;


/**
 * ステータス情報を削除するクラス<BR>
 *
 * @version 3.0.0
 * @since 2.0.0
 */
public class DeleteStatus {

	/**
	 * 引数で指定されたステータス情報一覧を削除します。<BR>
	 * 
	 * 引数のlistは、StatusDataInfoが格納されたArrayListとして渡されます。<BR>
	 * 
	 * @param list 削除対象のステータス情報一覧（StatusDataInfoのList）
	 * @return 削除に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.monitor.bean.StatusTabelDefine
	 * @see #delete(String, String, String)
	 */
	public boolean delete(ArrayList<StatusDataInfo> list) throws MonitorNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		String monitorId = "";
		String monitorDetailId = "";
		String pluginId = "";
		String facilityId = "";

		if (list != null && list.size()>0) {
			for(StatusDataInfo status : list){
				if (status == null)
					continue;

				// ステータス情報を取得
				monitorId = status.getMonitorId();
				monitorDetailId = status.getMonitorDetailId();
				pluginId = status.getPluginId();
				facilityId = status.getFacilityId();
				StatusInfoEntity statusInfo = null;
				try {
					statusInfo = QueryUtil.getStatusInfoPK(facilityId, monitorId, monitorDetailId, pluginId, ObjectPrivilegeMode.MODIFY);
				} catch (MonitorNotFound e) {
					String[] args = {facilityId, monitorId, pluginId};
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MONITOR, MessageConstant.MESSAGE_SYS_004_SYS_SFC, args);
					throw e;
				} catch (InvalidRole e) {
					throw e;
				}
				em.remove(statusInfo);
			}
		}
		return true;
	}
}

