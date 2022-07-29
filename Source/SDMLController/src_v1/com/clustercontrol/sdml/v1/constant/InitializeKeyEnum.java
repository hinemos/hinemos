/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.constant;

import java.util.ArrayList;
import java.util.List;

public enum InitializeKeyEnum {
	// プロセス死活監視
	PrcCommand,
	PrcArgument,
	PrcInterval,
	PrcDescription,
	PrcThresholdInfo,
	PrcThresholdWarn,
	PrcMonitor,
	PrcCollect,
	// SDML監視ログ共通
	MonLogDirectory,
	MonLogFileName,
	MonLogSeparationType,
	MonLogSeparationValue,
	MonLogMaxBytes,
	// アプリケーションログ監視
	LogAppDescription,
	LogAppFilterNCount,
	LogAppFilterDescription,
	LogAppFilterPattern,
	LogAppFilterDoProcess,
	LogAppFilterCaseSensitivity,
	LogAppFilterPriority,
	LogAppFilterMessage,
	LogAppMonitor,
	LogAppCollect,
	// デッドロック監視
	IntDlkPriority,
	IntDlkDescription,
	IntDlkMonitor,
	IntDlkCollect,
	// ヒープ未使用量監視
	IntHprPriority,
	IntHprDescription,
	IntHprMonitor,
	IntHprCollect,
	// GC発生頻度監視
	IntGccNCount,
	IntGccMethod,
	IntGccPriority,
	IntGccDescription,
	IntGccMonitor,
	IntGccCollect,
	// CPU使用率監視
	IntCpuPriority,
	IntCpuDescription,
	IntCpuMonitor,
	IntCpuCollect,
	// 機能障害検知
	InfoInterval;


	public String name(int num) {
		return name() + num;
	}

	public static List<InitializeKeyEnum> getLogAppFilterKeys() {
		List<InitializeKeyEnum> list = new ArrayList<>();
		list.add(LogAppFilterDescription);
		list.add(LogAppFilterPattern);
		list.add(LogAppFilterDoProcess);
		list.add(LogAppFilterCaseSensitivity);
		list.add(LogAppFilterPriority);
		list.add(LogAppFilterMessage);
		return list;
	}

	public static List<InitializeKeyEnum> getIntGccKeys() {
		List<InitializeKeyEnum> list = new ArrayList<>();
		list.add(IntGccMethod);
		list.add(IntGccPriority);
		list.add(IntGccDescription);
		list.add(IntGccMonitor);
		list.add(IntGccCollect);
		return list;
	}
}
