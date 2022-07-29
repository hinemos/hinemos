/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logging.constant;

public class PropertyConstant {

	// -------------------------------------------------------------------------------------------------------------------
	// 共通項目
	// -------------------------------------------------------------------------------------------------------------------

	public final static String APP_ID = "sdml.application.id";

	public static final String LOG_FILE_PATH = "sdml.log.directory";

	// -------------------------------------------------------------------------------------------------------------------
	// SDML制御ログ出力設定
	// -------------------------------------------------------------------------------------------------------------------

	public static final String CONTROL_LOG_FILE_PATH = "sdml.control.log.directory";

	public static final String CONTROL_LOG_FILE_NAME = "sdml.control.log.file.name";

	public static final String CONTROL_LOG_FILE_SIZE = "sdml.control.log.file.size";

	public static final String CONTROL_LOG_FILE_GENERATION = "sdml.control.log.file.generation";

	// -------------------------------------------------------------------------------------------------------------------
	// SDML監視ログ出力設定
	// -------------------------------------------------------------------------------------------------------------------

	public static final String MON_LOG_FILE_PATH = "sdml.monitoring.log.directory";

	public static final String MON_LOG_FILE_NAME = "sdml.monitoring.log.file.name";

	public static final String MON_LOG_FILE_SIZE = "sdml.monitoring.log.file.size";

	public static final String MON_LOG_FILE_GENERATION = "sdml.monitoring.log.file.generation";

	// -------------------------------------------------------------------------------------------------------------------
	// 監視要否
	// -------------------------------------------------------------------------------------------------------------------

	public static final String PRC_MONITOR = "monitor.enable.process";

	public static final String LOG_APP_MONITOR = "monitor.enable.log.application";

	public static final String INT_DLK_MONITOR = "monitor.enable.internal.deadlock";

	public static final String INT_HPR_MONITOR = "monitor.enable.internal.heap.remaining";

	public static final String INT_GCC_MONITOR = "monitor.enable.internal.gc.count.%d";

	public static final String INT_CPU_MONITOR = "monitor.enable.internal.cpu.usage";

	// -------------------------------------------------------------------------------------------------------------------
	// 収集要否
	// -------------------------------------------------------------------------------------------------------------------

	public static final String PRC_COLLECT = "collect.enable.process";

	public static final String LOG_APP_COLLECT = "collect.enable.log.application";

	public static final String INT_DLK_COLLECT = "collect.enable.internal.deadlock";

	public static final String INT_HPR_COLLECT = "collect.enable.internal.heap.remaining";

	public static final String INT_GCC_COLLECT = "collect.enable.internal.gc.count.%d";

	public static final String INT_CPU_COLLECT = "collect.enable.internal.cpu.usage";

	// -------------------------------------------------------------------------------------------------------------------
	// プロセス死活監視
	// -------------------------------------------------------------------------------------------------------------------

	public static final String PRC_INTERVAL = "process.interval";

	public static final String PRC_DESCRIPTION = "process.description";

	public static final String PRC_THRESHOLD_INFO = "process.threshold.info";

	public static final String PRC_THRESHOLD_WARN = "process.threshold.warn";

	public static final String PRC_GET_COMMAND_LINE = "process.get.command.line";

	public static final String PRC_GET_COMMAND_PATH = "process.get.command.path";

	public static final String PRC_GET_TIMEOUT = "process.get.command.timeout";

	// -------------------------------------------------------------------------------------------------------------------
	// アプリケーションログ監視・プロセス内部監視共通
	// -------------------------------------------------------------------------------------------------------------------

	public static final String MON_LOG_SEPARATION_TYPE = "sdml.monitoring.log.separation.condition.type";

	public static final String MON_LOG_SEPARATION_VALUE = "sdml.monitoring.log.separation.condition.value";

	public static final String MON_LOG_MAX_BYTES = "sdml.monitoring.log.separation.condition.max.bytes";

	// -------------------------------------------------------------------------------------------------------------------
	// アプリケーションログ監視
	// -------------------------------------------------------------------------------------------------------------------

	public static final String LOG_APP_LEVEL = "log.application.level";

	public static final String LOG_APP_DESCRIPTION = "log.application.description";

	public static final String LOG_APP_FILTER_DESCRIPTION = "log.application.filter.%d.description";

	public static final String LOG_APP_FILTER_PATTERN = "log.application.filter.%d.pattern";

	public static final String LOG_APP_FILTER_DO_PROCESS = "log.application.filter.%d.do.process";

	public static final String LOG_APP_FILTER_CASE_SENSITIVITY = "log.application.filter.%d.case.sensitivity";

	public static final String LOG_APP_FILTER_PRIORITY = "log.application.filter.%d.priority";

	public static final String LOG_APP_FILTER_MESSAGE = "log.application.filter.%d.message";

	// -------------------------------------------------------------------------------------------------------------------
	// プロセス内部監視 デッドロック監視
	// -------------------------------------------------------------------------------------------------------------------

	public static final String INT_DLK_INTERVAL = "internal.deadlock.interval";

	public static final String INT_DLK_PRIORITY = "internal.deadlock.priority";

	public static final String INT_DLK_DESCRIPTION = "internal.deadlock.description";

	public static final String INT_DLK_TIMEOUT = "internal.deadlock.timeout";

	// -------------------------------------------------------------------------------------------------------------------
	// プロセス内部監視 ヒープ未使用量監視
	// -------------------------------------------------------------------------------------------------------------------

	public static final String INT_HPR_INTERVAL = "internal.heap.remaining.interval";

	public static final String INT_HPR_PRIORITY = "internal.heap.remaining.priority";

	public static final String INT_HPR_THRESHOLD = "internal.heap.remaining.threshold";

	public static final String INT_HPR_DESCRIPTION = "internal.heap.remaining.description";

	public static final String INT_HPR_TIMEOUT = "internal.heap.remaining.timeout";

	// -------------------------------------------------------------------------------------------------------------------
	// プロセス内部監視 GC発生頻度監視
	// -------------------------------------------------------------------------------------------------------------------

	public static final String INT_GCC_INTERVAL = "internal.gc.count.%d.interval";

	public static final String INT_GCC_METHOD = "internal.gc.count.%d.method";

	public static final String INT_GCC_PRIORITY = "internal.gc.count.%d.priority";

	public static final String INT_GCC_THRESHOLD = "internal.gc.count.%d.threshold";

	public static final String INT_GCC_DESCRIPTION = "internal.gc.count.%d.description";

	public static final String INT_GCC_TIMEOUT = "internal.gc.count.%d.timeout";

	// -------------------------------------------------------------------------------------------------------------------
	// プロセス内部監視 CPU使用率監視
	// -------------------------------------------------------------------------------------------------------------------

	public static final String INT_CPU_INTERVAL = "internal.cpu.usage.interval";

	public static final String INT_CPU_PRIORITY = "internal.cpu.usage.priority";

	public static final String INT_CPU_THRESHOLD = "internal.cpu.usage.threshold";

	public static final String INT_CPU_DESCRIPTION = "internal.cpu.usage.description";

	public static final String INT_CPU_TIMEOUT = "internal.cpu.usage.timeout";

	// -------------------------------------------------------------------------------------------------------------------
	// 障害検知用設定
	// -------------------------------------------------------------------------------------------------------------------

	public static final String INFO_INTERVAL = "info.interval";

	public static final String FAILD_MAX_COUNT = "internal.monitor.failed.max.count";

	// -------------------------------------------------------------------------------------------------------------------
	// Hinemosロギング内部ログ出力設定
	// -------------------------------------------------------------------------------------------------------------------

	public static final String INTERNAL_LOG_FILE_PATH = "hinemos.logging.log.directory";

	public static final String INTERNAL_LOG_FILE_NAME = "hinemos.logging.log.file.name";

	public static final String INTERNAL_LOG_FILE_SIZE = "hinemos.logging.log.file.size";

	public static final String INTERNAL_LOG_FILE_GENERATION = "hinemos.logging.log.file.generation";

	public static final String INTERNAL_LOG_ROOT_LOGGER = "hinemos.logging.log.logger.root";

	public static final String INTERNAL_LOG_LOGGER_PREFIX = "hinemos.logging.log.logger..*";

}
