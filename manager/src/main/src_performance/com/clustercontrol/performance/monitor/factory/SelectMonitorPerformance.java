/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */

package com.clustercontrol.performance.monitor.factory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * リソース監視判定情報検索クラス
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class SelectMonitorPerformance extends SelectMonitor{
	/**
	 * アプリケーションログにログを出力します。
	 * 
	 * @param index アプリケーションログのインデックス
	 */
	protected void outputLog(String monitorTypeId, String monitorId, int priority, MessageConstant msgCode) {
		String[] args = {monitorTypeId, monitorId };
		AplLogger.put(priority, HinemosModuleConstant.PERFORMANCE, msgCode, args);
	}

}
