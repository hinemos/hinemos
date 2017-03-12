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
