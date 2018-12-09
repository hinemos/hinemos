/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.sql.util;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.sql.model.SqlCheckInfo;

/**
 * SQL監視 判定情報管理クラス
 *
 * @version 4.0.0
 * @since 2.1.0
 */
public class ControlSqlInfo {

	/**
	 * SQL監視情報を変更
	 * 
	 * @param monitorId 監視項目ID
	 * @param sql SQL監視情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public boolean modify(String monitorId, SqlCheckInfo sql) throws MonitorNotFound, InvalidRole {

		MonitorInfo monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(monitorId);

		// SQL監視情報を取得
		SqlCheckInfo entity = QueryUtil.getMonitorSqlInfoPK(monitorId);

		// SQL監視情報を設定
		entity.setConnectionUrl(sql.getConnectionUrl());
		entity.setUser(sql.getUser());
		entity.setPassword(sql.getPassword());
		entity.setQuery(sql.getQuery());
		entity.setJdbcDriver(sql.getJdbcDriver());
		monitorEntity.setSqlCheckInfo(entity);
		return true;
	}
}
