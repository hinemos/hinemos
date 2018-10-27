/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmp.util;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.snmp.model.SnmpCheckInfo;

/**
 * SNMP監視 判定情報を管理するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.1.0
 */
public class ControlSnmpInfo {

	/**
	 * SNMP監視情報を変更
	 * 
	 * @param monitorId 監視項目ID
	 * @param snmp SNMP監視情報
	 * @param deleteValueFlg 前回値情報削除フラグ
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public boolean modify(String monitorId, SnmpCheckInfo snmp, boolean deleteValueFlg) throws MonitorNotFound, InvalidRole {

		MonitorInfo monitorEntity = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(monitorId);

		// SNMP監視情報を取得
		SnmpCheckInfo entity = QueryUtil.getMonitorSnmpInfoPK(monitorId);

		entity.setSnmpOid(snmp.getSnmpOid());
		entity.setConvertFlg(snmp.getConvertFlg());

		monitorEntity.setSnmpCheckInfo(entity);

		return true;
	}

}
