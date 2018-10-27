/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.action;

import java.util.List;

/**
 * 監視設定削除クラス用インタフェース
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public interface DeleteInterface {

	// 監視設定の削除
	public boolean delete(String managerName, List<String> monitorIdList) throws Exception;

}
