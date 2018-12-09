/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.action;

import java.util.List;

import com.clustercontrol.monitor.action.DeleteInterface;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;

/**
 * Hinemos Agent監視情報をマネージャから削除するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class DeleteAgent implements DeleteInterface{

	/**
	 * Hinemos Agent監視情報を削除します。<BR>
	 *
	 * @param managerName マネージャ名
	 * @param monitorIdList 状態監視IDリスト
	 * @return 削除に成功した場合、true
	 */
	@Override
	public boolean delete(String managerName, List<String> monitorIdList) throws Exception {
		MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
		return wrapper.deleteMonitor(monitorIdList);
	}
}
