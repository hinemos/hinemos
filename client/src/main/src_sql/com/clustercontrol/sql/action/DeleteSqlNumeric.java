/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.sql.action;

import java.util.List;

import com.clustercontrol.monitor.action.DeleteInterface;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;

/**
 * SQL監視（数値）情報を削除するクライアント側アクションクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class DeleteSqlNumeric implements DeleteInterface{

	/**
	 * SQL監視（数値）情報を削除します。
	 *
	 * @param managerName マネージャ名
	 * @param monitorIdList 監視項目IDリスト
	 * @return 削除に成功した場合、true
	 */
	@Override
	public boolean delete(String managerName, List<String> monitorIdList) throws Exception {
		MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
		return wrapper.deleteMonitor(monitorIdList);
	}
}
