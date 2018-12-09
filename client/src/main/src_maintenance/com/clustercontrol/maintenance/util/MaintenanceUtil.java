/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.util;

public class MaintenanceUtil {

	/**
	 * 通知グループIDを生成します。(ジョブ定義用)
	 * 
	 * @return 通知グループID
	 */
	public static String createNotifyGroupIdMaintenance(String maintenanceId){

		String ret = com.clustercontrol.bean.HinemosModuleConstant.SYSYTEM_MAINTENANCE
				+"-"+maintenanceId + "-0";
		return ret;
	}

}
