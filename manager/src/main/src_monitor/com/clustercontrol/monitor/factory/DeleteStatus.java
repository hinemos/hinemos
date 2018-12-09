/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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

		String monitorId = "";
		String monitorDetailId = "";
		String pluginId = "";
		String facilityId = "";

		if (list == null || list.size() == 0) {
			return true;
		}

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
			return true;
		}
	}
}

