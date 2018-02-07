/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.util;

/**
 * レポーティングオプション用ユーティリティクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ReportingUtil {

	/**
	 * モジュール定数:
	 * com.clustercontrol.bean.HinemosModuleConstant.REPORTING
	 * を使いたいが、この定数は 4.1.2 以降限定のため自前で定義しておく。
	 * 
	 */
	public static final String MODULE_CONSTANT_REPORTING = "REPORTING";
	
	/**
	 * 通知グループIDを生成します。(レポーティング用)
	 * 
	 * @return 通知グループID
	 */
	public static String createNotifyGroupIdReporting(String reportingId) {

		String ret = MODULE_CONSTANT_REPORTING + "-" + reportingId + "-0";
		return ret;
	}

}
