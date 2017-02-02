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

package com.clustercontrol.http.util;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.model.HttpCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;

/**
 * HTTP監視 判定情報を管理するクラス<BR>
 *
 * @version 5.0.0
 * @since 2.1.0
 */
public class ControlHttpInfo {

	/**
	 * HTTP監視情報を変更します。<BR>
	 * 
	 * @param monitorId 監視項目ID
	 * @param http HTTP監視情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public boolean modify(String monitorId, HttpCheckInfo http) throws MonitorNotFound, InvalidRole {

		MonitorInfo monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(monitorId);

		// HTTP監視情報を取得
		HttpCheckInfo entity = QueryUtil.getMonitorHttpInfoPK(monitorId);

		// HTTP監視情報を設定
		entity.setRequestUrl(http.getRequestUrl());
		entity.setUrlReplace(http.getUrlReplace());
		entity.setTimeout(http.getTimeout());
		entity.setProxySet(http.getProxySet());
		entity.setProxyHost(http.getProxyHost());
		entity.setProxyPort(http.getProxyPort());
		monitorEntity.setHttpCheckInfo(entity);

		return true;
	}
}
