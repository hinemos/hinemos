/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jmx.action;

import java.util.List;

import com.clustercontrol.monitor.action.DeleteInterface;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;

/**
 * リソース監視情報を削除するクラス
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class DeleteJmx implements DeleteInterface{

	/**
	 * リソース監視情報を削除します。
	 *
	 * @param monitorIdList 状態監視IDリスト
	 * @return 削除に成功した場合、true
	 */
	@Override
	public boolean delete(String managerName, List<String> monitorIdList) throws Exception{
		MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
		return wrapper.deleteMonitor(monitorIdList);
	}
}
