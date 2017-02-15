/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jmx.util;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jmx.model.JmxCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;

/**
 * JMX 監視 判定情報を管理するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ControlJmxInfo {

	/**
	 * JMX 監視情報を変更します。<BR>
	 * 
	 * @param monitorId 監視項目ID
	 * @param jmx JMX 監視情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 */
	public boolean modify(String monitorId, JmxCheckInfo jmx) throws MonitorNotFound, InvalidRole {

		MonitorInfo monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(monitorId);

		// JMX 監視情報を取得
		JmxCheckInfo entity = QueryUtil.getMonitorJmxInfoPK(monitorId);

		// JMX 監視情報を設定
		entity.setAuthUser(jmx.getAuthUser());
		entity.setAuthPassword(jmx.getAuthPassword());
		entity.setPort(jmx.getPort());
		entity.setMasterId(jmx.getMasterId());
		entity.setConvertFlg(jmx.getConvertFlg());
		monitorEntity.setJmxCheckInfo(entity);

		return true;
	}
}
