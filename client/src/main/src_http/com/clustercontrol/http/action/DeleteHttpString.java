/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.action;

import java.util.List;

import com.clustercontrol.monitor.action.DeleteInterface;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;

/**
 * HTTP監視（文字列）情報を削除するクライアント側アクションクラス<BR>
 *
 * @version 4.0.0
 * @since 2.1.0
 */
public class DeleteHttpString implements DeleteInterface{

	/**
	 * HTTP監視（文字列）情報をマネージャから削除します。<BR>
	 *
	 * @param monitorId 監視項目ID
	 * @return 削除に成功した場合、true
	 */
	@Override
	public boolean delete(String managerName, List<String> monitorId) throws Exception {
		MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
		return wrapper.deleteMonitor(monitorId);
	}
}
