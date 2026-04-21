/*
 * Copyright (c) 2025 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.util;

import com.clustercontrol.commons.util.HinemosPropertyCommon;

/**
 * レポーティングのユーティリティクラス
 * 
 */
public class ReportingUtil {

	/**
	 * レポート作成のJVMヒープサイズのオプション生成
	 * 
	 */
	public static String buildJvmOptions() {
		return String.format("-Xms%dm -Xmx%dm -Xss%dk",
				HinemosPropertyCommon.reporting_jvm_option_heap_initial_size.getNumericValue(),
				HinemosPropertyCommon.reporting_jvm_option_heap_max_size.getNumericValue(),
				HinemosPropertyCommon.reporting_jvm_option_stack_size.getNumericValue());
	}
}
