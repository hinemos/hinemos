/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.action;

import java.util.List;

import com.clustercontrol.monitor.action.DeleteInterface;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;

/**
 * バイナリファイル監視情報削除<br>
 * <br>
 * マネージャー処理を呼び出してバイナリ監視情報を削除<br>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class DeleteMonitorBinaryFile implements DeleteInterface {

	/**
	 * バイナリファイル監視情報を削除
	 *
	 * @param monitorIdList
	 *            監視項目IDリスト
	 * @return 削除に成功した場合、true
	 */
	@Override
	public boolean delete(String managerName, List<String> monitorIdList) throws Exception {
		MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
		return wrapper.deleteMonitor(monitorIdList);
	}
}
