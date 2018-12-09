/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
