/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.log.control;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.constant.MessageConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.exception.LoggingPropertyException;
import com.clustercontrol.logging.monitor.GcCountMonitorTask;
import com.clustercontrol.logging.property.LoggingProperty;
import com.clustercontrol.logging.property.LoggingPropertyValidater;
import com.clustercontrol.logging.util.PriorityType;
import com.clustercontrol.logging.util.ProcessInfo;
import com.clustercontrol.logging.util.SeparationType;
import com.clustercontrol.logging.util.StringEscaper;

/**
 * 制御ログのヘッダー情報をList型で作成するクラス
 * ヘッダー情報は制御コード[Initialize]～[Start]を指す
 *
 */
public class ControlLogHeader extends ArrayList<String> {
	private static final long serialVersionUID = 1L;
	private static final InternalLogManager.Logger log = InternalLogManager.getLogger(ControlLogHeader.class);
	private static final String VERSION = "SDMLControlLog Version:";
	private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
	private static final String VERSION_KEY = "Hinemos-Logging-Version";

	/**
	 * 制御ログのヘッダー情報を整形し、文字列で返す
	 * 
	 * @param controlLogHeader
	 * @param pattern
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws LoggingPropertyException
	 */
	public String toFormatString(String pattern, String lineSeparator) {
		List<String> addPatternList = new ArrayList<String>();

		String ver = "";
		try {
			// versionを取得
			Enumeration<URL> resources = ControlLogHeader.class.getClassLoader().getResources(MANIFEST_PATH);
			
			while (resources.hasMoreElements()) {
				Manifest manifest = new Manifest(resources.nextElement().openStream());
				Attributes attrs = manifest.getMainAttributes();
				if (attrs.getValue(VERSION_KEY) != null) {
					ver = attrs.getValue(VERSION_KEY);
					break;
				}
			}
			if (ver.length() == 5) {
				// [メジャーバージョン].[マイナーバージョン]のみ抽出
				ver = ver.substring(0, 3);
			}
		} catch (IOException e) {
			log.error("toFormatString : Getting version is failed.", e);
		}
		String version = VERSION + ver;

		// ヘッダー情報にパターンを追加
		for (String initSet : this) {
			addPatternList.add(pattern + initSet);
		}
		String initSetHeader = String.join(lineSeparator, addPatternList);
		// versionを1行目に出力
		return version + lineSeparator + initSetHeader;
	}

	/**
	 * プロパティを制御ログのヘッダーにセットする
	 * 
	 * @param prop
	 * @throws LoggingPropertyException
	 * @throws TimeoutException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public ControlLogHeader setProperty(LoggingProperty prop, ProcessInfo procInfo) throws LoggingPropertyException {

		this.add(ControlCode.INITIALIZE_BEGIN.getString());

		// 実行コマンド、コマンドライン引数を取得前に取得コマンドとタイムアウト値が正常かチェックする
		LoggingPropertyValidater.validPrcCommandLine(prop, PropertyConstant.PRC_GET_COMMAND_LINE);
		LoggingPropertyValidater.validPrcCommandPath(prop, PropertyConstant.PRC_GET_COMMAND_PATH);
		LoggingPropertyValidater.validPrcTimeout(prop, PropertyConstant.PRC_GET_TIMEOUT);

		HashMap<String, String> procMap = null;
		try {
			procMap = procInfo.getCommandInfo();
		} catch (TimeoutException | IOException | InterruptedException e) {
			log.error("setProperty : Getting command is failed.", e);
			throw new LoggingPropertyException(e);
		}

		String prcCmd = procMap.get("PrcCommand");
		this.addInitSet("PrcCommand", prcCmd);

		String prcArg = procMap.get("PrcArgument");
		this.addInitSet("PrcArgument", prcArg);

		// -------------------------------------------------------------------------------------------------------------------
		// 死活監視/プロセス死活監視
		// -------------------------------------------------------------------------------------------------------------------

		LoggingPropertyValidater.validPrcInterval(prop, PropertyConstant.PRC_INTERVAL);
		int prcIntervalSec = stringToSecond(prop.getProperty(PropertyConstant.PRC_INTERVAL), PropertyConstant.PRC_INTERVAL);
		this.addInitSet("PrcInterval", prcIntervalSec);

		LoggingPropertyValidater.validPrcDescription(prop, PropertyConstant.PRC_DESCRIPTION);
		this.addInitSet("PrcDescription", prop.getProperty(PropertyConstant.PRC_DESCRIPTION));

		LoggingPropertyValidater.validPrcThresholdInfo(prop, PropertyConstant.PRC_THRESHOLD_INFO);
		this.addInitSet("PrcThresholdInfo",
				new Integer[] { Integer.parseInt(prop.getProperty(PropertyConstant.PRC_THRESHOLD_INFO).split(",")[0]),
						Integer.parseInt(prop.getProperty(PropertyConstant.PRC_THRESHOLD_INFO).split(",")[1]) });

		LoggingPropertyValidater.validPrcThresholdWarn(prop, PropertyConstant.PRC_THRESHOLD_WARN);
		this.addInitSet("PrcThresholdWarn",
				new Integer[] { Integer.parseInt(prop.getProperty(PropertyConstant.PRC_THRESHOLD_WARN).split(",")[0]),
						Integer.parseInt(prop.getProperty(PropertyConstant.PRC_THRESHOLD_WARN).split(",")[1]) });

		LoggingPropertyValidater.validPrcMonitor(prop, PropertyConstant.PRC_MONITOR);
		this.addInitSet("PrcMonitor", Boolean.valueOf(prop.getProperty(PropertyConstant.PRC_MONITOR)));

		LoggingPropertyValidater.validPrcCollect(prop, PropertyConstant.PRC_COLLECT);
		this.addInitSet("PrcCollect", Boolean.valueOf(prop.getProperty(PropertyConstant.PRC_COLLECT)));

		// -------------------------------------------------------------------------------------------------------------------
		// ログ監視/プロセス内部監視共通
		// -------------------------------------------------------------------------------------------------------------------

		LoggingPropertyValidater.validMonitoringLogDirPath(prop, PropertyConstant.MON_LOG_FILE_PATH);
		// sdml.monitoring.log.directoryがない場合、sdml.log.directoryを設定する
		if(prop.getProperty(PropertyConstant.MON_LOG_FILE_PATH) == null){
			this.addInitSet("MonLogDirectory", prop.getProperty(PropertyConstant.LOG_FILE_PATH));
		} else {
			this.addInitSet("MonLogDirectory", prop.getProperty(PropertyConstant.MON_LOG_FILE_PATH));
		}

		LoggingPropertyValidater.validMonitoringLogName(prop, PropertyConstant.MON_LOG_FILE_NAME);
		this.addInitSet("MonLogFileName", prop.getProperty(PropertyConstant.MON_LOG_FILE_NAME));

		LoggingPropertyValidater.validMonitoringLogSize(prop, PropertyConstant.MON_LOG_FILE_SIZE);

		LoggingPropertyValidater.validMonitoringLogGeneration(prop, PropertyConstant.MON_LOG_FILE_GENERATION);

		LoggingPropertyValidater.validMonitoringLogSeparationType(prop, PropertyConstant.MON_LOG_SEPARATION_TYPE);
		this.addInitSet("MonLogSeparationType",
				SeparationType.stringToInt(prop.getProperty(PropertyConstant.MON_LOG_SEPARATION_TYPE)));

		LoggingPropertyValidater.validMonitoringLogSeparationValue(prop, PropertyConstant.MON_LOG_SEPARATION_VALUE);
		this.addInitSet("MonLogSeparationValue",
				prop.getProperty(PropertyConstant.MON_LOG_SEPARATION_VALUE));

		LoggingPropertyValidater.validMonitoringLogMaxBytes(prop, PropertyConstant.MON_LOG_MAX_BYTES);
		this.addInitSet("MonLogMaxBytes",
				Integer.parseInt(prop.getProperty(PropertyConstant.MON_LOG_MAX_BYTES)));

		// -------------------------------------------------------------------------------------------------------------------
		// ログ監視/アプリケーションログ監視
		// -------------------------------------------------------------------------------------------------------------------

		LoggingPropertyValidater.validLogAppLevel(prop, PropertyConstant.LOG_APP_LEVEL);

		LoggingPropertyValidater.validLogAppDescription(prop, PropertyConstant.LOG_APP_DESCRIPTION);
		this.addInitSet("LogAppDescription", prop.getProperty(PropertyConstant.LOG_APP_DESCRIPTION));

		this.addInitSet("LogAppFilterNCount", prop.getMonStrFilterNumbers().size());
		if (!prop.getMonStrFilterNumbers().isEmpty()) {
			for (Integer monStrFilNum : prop.getMonStrFilterNumbers()) {
				String logAppFilterDescKey = String.format(PropertyConstant.LOG_APP_FILTER_DESCRIPTION, monStrFilNum);
				String logAppFilterPatternKey = String.format(PropertyConstant.LOG_APP_FILTER_PATTERN, monStrFilNum);
				String logAppFilterProcessKey = String.format(PropertyConstant.LOG_APP_FILTER_DO_PROCESS, monStrFilNum);
				String logAppFilterSensitivityKey = String.format(PropertyConstant.LOG_APP_FILTER_CASE_SENSITIVITY,
						monStrFilNum);
				String logAppFilterPriorityKey = String.format(PropertyConstant.LOG_APP_FILTER_PRIORITY, monStrFilNum);
				String logAppFilterMessageKey = String.format(PropertyConstant.LOG_APP_FILTER_MESSAGE, monStrFilNum);

				LoggingPropertyValidater.validLogAppFilterDescription(prop, logAppFilterDescKey);
				this.addInitSet("LogAppFilterDescription", monStrFilNum,
						prop.getProperty(logAppFilterDescKey));

				LoggingPropertyValidater.validLogAppFilterPattern(prop, logAppFilterPatternKey);
				this.addInitSet("LogAppFilterPattern", monStrFilNum,
						prop.getProperty(logAppFilterPatternKey));

				LoggingPropertyValidater.validLogAppFilterProcess(prop, logAppFilterProcessKey);
				this.addInitSet("LogAppFilterDoProcess", monStrFilNum,
						Boolean.valueOf(prop.getProperty(logAppFilterProcessKey)));

				LoggingPropertyValidater.validLogAppFilterSensitivity(prop, logAppFilterSensitivityKey);
				this.addInitSet("LogAppFilterCaseSensitivity", monStrFilNum,
						Boolean.valueOf(prop.getProperty(logAppFilterSensitivityKey)));

				LoggingPropertyValidater.validLogAppFilterPriority(prop, logAppFilterPriorityKey);
				this.addInitSet("LogAppFilterPriority", monStrFilNum,
						PriorityType.stringToInt(prop.getProperty(logAppFilterPriorityKey)));

				LoggingPropertyValidater.validLogAppFilterMessage(prop, logAppFilterMessageKey);
				this.addInitSet("LogAppFilterMessage", monStrFilNum,
						prop.getProperty(logAppFilterMessageKey));
			}
		}

		LoggingPropertyValidater.validLogAppMonitor(prop, PropertyConstant.LOG_APP_MONITOR);
		this.addInitSet("LogAppMonitor",
				Boolean.valueOf(prop.getProperty(PropertyConstant.LOG_APP_MONITOR)));

		LoggingPropertyValidater.validLogAppCollect(prop, PropertyConstant.LOG_APP_COLLECT);
		this.addInitSet("LogAppCollect",
				Boolean.valueOf(prop.getProperty(PropertyConstant.LOG_APP_COLLECT)));

		// -------------------------------------------------------------------------------------------------------------------
		// プロセス内部監視/デッドロック監視
		// -------------------------------------------------------------------------------------------------------------------

		LoggingPropertyValidater.validIntDlkInterval(prop, PropertyConstant.INT_DLK_INTERVAL);

		LoggingPropertyValidater.validIntDlkPriority(prop, PropertyConstant.INT_DLK_PRIORITY);
		if (prop.getProperty(PropertyConstant.INT_DLK_PRIORITY) == null) {
			this.addInitSet("IntDlkPriority", null);
		} else {
			this.addInitSet("IntDlkPriority",
					PriorityType.stringToInt(prop.getProperty(PropertyConstant.INT_DLK_PRIORITY)));
		}

		LoggingPropertyValidater.validIntDlkDescription(prop, PropertyConstant.INT_DLK_DESCRIPTION);
		this.addInitSet("IntDlkDescription", prop.getProperty(PropertyConstant.INT_DLK_DESCRIPTION));

		LoggingPropertyValidater.validIntDlkTimeout(prop, PropertyConstant.INT_DLK_TIMEOUT);

		LoggingPropertyValidater.validIntDlkMonitor(prop, PropertyConstant.INT_DLK_MONITOR);
		this.addInitSet("IntDlkMonitor",
				Boolean.valueOf(prop.getProperty(PropertyConstant.INT_DLK_MONITOR)));

		LoggingPropertyValidater.validIntDlkCollect(prop, PropertyConstant.INT_DLK_COLLECT);
		this.addInitSet("IntDlkCollect",
				Boolean.valueOf(prop.getProperty(PropertyConstant.INT_DLK_COLLECT)));

		// -------------------------------------------------------------------------------------------------------------------
		// プロセス内部監視/ヒープ未使用量監視
		// -------------------------------------------------------------------------------------------------------------------

		LoggingPropertyValidater.validIntHprInterval(prop, PropertyConstant.INT_HPR_INTERVAL);

		LoggingPropertyValidater.validIntHprPriority(prop, PropertyConstant.INT_HPR_PRIORITY);
		if (prop.getProperty(PropertyConstant.INT_HPR_PRIORITY) == null) {
			this.addInitSet("IntHprPriority", null);
		} else {
			this.addInitSet("IntHprPriority",
					PriorityType.stringToInt(prop.getProperty(PropertyConstant.INT_HPR_PRIORITY)));
		}

		LoggingPropertyValidater.validIntHprThreshold(prop, PropertyConstant.INT_HPR_THRESHOLD);

		LoggingPropertyValidater.validIntHprDescription(prop, PropertyConstant.INT_HPR_DESCRIPTION);
		this.addInitSet("IntHprDescription", prop.getProperty(PropertyConstant.INT_HPR_DESCRIPTION));

		LoggingPropertyValidater.validIntHprTimeout(prop, PropertyConstant.INT_HPR_TIMEOUT);

		LoggingPropertyValidater.validIntHprMonitor(prop, PropertyConstant.INT_HPR_MONITOR);
		this.addInitSet("IntHprMonitor",
				Boolean.valueOf(prop.getProperty(PropertyConstant.INT_HPR_MONITOR)));

		LoggingPropertyValidater.validIntHprCollect(prop, PropertyConstant.INT_HPR_COLLECT);
		this.addInitSet("IntHprCollect",
				Boolean.valueOf(prop.getProperty(PropertyConstant.INT_HPR_COLLECT)));

		// -------------------------------------------------------------------------------------------------------------------
		// プロセス内部監視/GC発生頻度監視
		// -------------------------------------------------------------------------------------------------------------------

		if (prop.getGccNumbers().isEmpty()) {
			this.addInitSet("IntGccNCount", 0);
		} else {
			// 番号1の設定が"all"かチェック
			String method = prop.getProperty(String.format(PropertyConstant.INT_GCC_METHOD, 1));
			if ("all".equalsIgnoreCase(method)) {
				log.info("setProperty : Garbage Collector is specified " + method);
				for (Integer num : prop.getGccNumbers()) {
					if (num != 1) {
						// allの時に1以外が設定されている場合
						throw new LoggingPropertyException(MessageConstant.VALIDATE_GCC_ALL);
					}
				}
				ExecutorService executor = Executors.newSingleThreadExecutor();
				Future<List<GarbageCollectorMXBean>> f = executor.submit(new GcCountMonitorTask());
				List<GarbageCollectorMXBean> beans;
				try {
					beans = f.get();
				} catch (Exception e) {
					throw new LoggingPropertyException(MessageConstant.VALIDATE_GCC_GET_FAILED, e);
				} finally {
					executor.shutdownNow();
				}
				if (beans == null || beans.size() == 0) {
					// 何も取得できなかった場合も失敗
					throw new LoggingPropertyException(MessageConstant.VALIDATE_GCC_GET_FAILED);
				}
				// 取得できたコレクタに置き換え、1の設定を他に反映する
				int i = 1;
				for (GarbageCollectorMXBean bean : beans) {
					log.info("setProperty : Replace GC setting " + i + " with " + bean.getName());
					prop.setProperty(String.format(PropertyConstant.INT_GCC_METHOD, i), bean.getName());
					if (i > 1) {
						prop.replaceWithSameValue(PropertyConstant.INT_GCC_INTERVAL, 1, i);
						prop.replaceWithSameValue(PropertyConstant.INT_GCC_PRIORITY, 1, i);
						prop.replaceWithSameValue(PropertyConstant.INT_GCC_THRESHOLD, 1, i);
						prop.replaceWithSameValue(PropertyConstant.INT_GCC_DESCRIPTION, 1, i);
						prop.replaceWithSameValue(PropertyConstant.INT_GCC_TIMEOUT, 1, i);
						prop.replaceWithSameValue(PropertyConstant.INT_GCC_MONITOR, 1, i);
						prop.replaceWithSameValue(PropertyConstant.INT_GCC_COLLECT, 1, i);
					}
					i++;
				}
			}

			List<Integer> gccNumbers = prop.getGccNumbers();
			this.addInitSet("IntGccNCount", gccNumbers.size());
			for (Integer gccPropNum : gccNumbers) {
				String gccIntervalKey = String.format(PropertyConstant.INT_GCC_INTERVAL, gccPropNum);
				String gccMethodKey = String.format(PropertyConstant.INT_GCC_METHOD, gccPropNum);
				String gccPriorityKey = String.format(PropertyConstant.INT_GCC_PRIORITY, gccPropNum);
				String gccThresholdKey = String.format(PropertyConstant.INT_GCC_THRESHOLD, gccPropNum);
				String gccDescKey = String.format(PropertyConstant.INT_GCC_DESCRIPTION, gccPropNum);
				String gccTimeoutKey = String.format(PropertyConstant.INT_GCC_TIMEOUT, gccPropNum);
				String gccMonitorKey = String.format(PropertyConstant.INT_GCC_MONITOR, gccPropNum);
				String gccCollectKey = String.format(PropertyConstant.INT_GCC_COLLECT, gccPropNum);

				LoggingPropertyValidater.validIntGccInterval(prop, gccIntervalKey);

				LoggingPropertyValidater.validIntGccMethod(prop, gccMethodKey);
				this.addInitSet("IntGccMethod", gccPropNum, prop.getProperty(gccMethodKey));

				LoggingPropertyValidater.validIntGccPriority(prop, gccPriorityKey);
				this.addInitSet("IntGccPriority", gccPropNum,
						PriorityType.stringToInt(prop.getProperty(gccPriorityKey)));

				LoggingPropertyValidater.validIntGccThreshold(prop, gccThresholdKey);

				LoggingPropertyValidater.validIntGccDescription(prop, gccDescKey);
				this.addInitSet("IntGccDescription", gccPropNum, prop.getProperty(gccDescKey));

				LoggingPropertyValidater.validIntGccTimeout(prop, gccTimeoutKey);

				LoggingPropertyValidater.validIntGccMonitor(prop, gccMonitorKey);
				this.addInitSet("IntGccMonitor", gccPropNum,
						Boolean.valueOf(prop.getProperty(gccMonitorKey)));

				LoggingPropertyValidater.validIntGccCollect(prop, gccCollectKey);
				this.addInitSet("IntGccCollect", gccPropNum,
						Boolean.valueOf(prop.getProperty(gccCollectKey)));
			}
		}

		// -------------------------------------------------------------------------------------------------------------------
		// プロセス内部監視/CPU使用率監視
		// -------------------------------------------------------------------------------------------------------------------

		LoggingPropertyValidater.validIntCpuInterval(prop, PropertyConstant.INT_CPU_INTERVAL);

		LoggingPropertyValidater.validIntCpuPriority(prop, PropertyConstant.INT_CPU_PRIORITY);
		if (prop.getProperty(PropertyConstant.INT_CPU_PRIORITY) == null) {
			this.addInitSet("IntCpuPriority", null);
		} else {
			this.addInitSet("IntCpuPriority",
					PriorityType.stringToInt(prop.getProperty(PropertyConstant.INT_CPU_PRIORITY)));
		}

		LoggingPropertyValidater.validIntCpuThreshold(prop, PropertyConstant.INT_CPU_THRESHOLD);

		LoggingPropertyValidater.validIntCpuDescription(prop, PropertyConstant.INT_CPU_DESCRIPTION);
		this.addInitSet("IntCpuDescription", prop.getProperty(PropertyConstant.INT_CPU_DESCRIPTION));

		LoggingPropertyValidater.validIntCpuTimeout(prop, PropertyConstant.INT_CPU_TIMEOUT);

		LoggingPropertyValidater.validIntCpuMonitor(prop, PropertyConstant.INT_CPU_MONITOR);
		this.addInitSet("IntCpuMonitor",
				Boolean.valueOf(prop.getProperty(PropertyConstant.INT_CPU_MONITOR)));

		LoggingPropertyValidater.validIntCpuCollect(prop, PropertyConstant.INT_CPU_COLLECT);
		this.addInitSet("IntCpuCollect",
				Boolean.valueOf(prop.getProperty(PropertyConstant.INT_CPU_COLLECT)));

		// -------------------------------------------------------------------------------------------------------------------
		// 監視以外
		// -------------------------------------------------------------------------------------------------------------------

		LoggingPropertyValidater.validInfoInterval(prop, PropertyConstant.INFO_INTERVAL);
		this.addInitSet("InfoInterval",
				Integer.parseInt(prop.getProperty(PropertyConstant.INFO_INTERVAL)));

		LoggingPropertyValidater.validFaildMaxCount(prop, PropertyConstant.FAILD_MAX_COUNT);

		this.add(ControlCode.INITIALIZE_END.getString());
		this.add(ControlCode.START.getString());
		return this;
	}

	/**
	 * [Initialize_Set sendKey,value]形式でListに追加する
	 * 
	 * @param sendKey
	 * @param value
	 */
	private void addInitSet(String sendKey, Object value) {
		String message = null;
		if (value instanceof String) {
			String target = "\\*+.?{}()[]^$-|\"";
			StringEscaper stringEscaper = new StringEscaper(target);
			value = stringEscaper.escapeString(value.toString());
			message = String.format("%s,\"%s\"", sendKey, value);
		} else if (value instanceof Integer[]) {
			Integer[] list = (Integer[]) value;
			message = String.format("%s,%s-%s", sendKey, list[0], list[1]);
		} else {
			message = String.format("%s,%s", sendKey, value);
		}
		this.add(ControlCode.INITIALIZE_SET.getString() + " " + message);
	}

	/**
	 * [Initialize_Set sendKey{i},value]形式でListに追加する
	 * 
	 * @param sendKey
	 * @param i
	 * @param value
	 */
	private void addInitSet(String sendKey, int i, Object value) {
		String message = null;
		if (value instanceof String) {
			String target = "\\*+.?{}()[]^$-|\"";
			StringEscaper stringEscaper = new StringEscaper(target);
			value = stringEscaper.escapeString(value.toString());
			message = String.format("%s%s,\"%s\"", sendKey, i, value);
		} else {
			message = String.format("%s%s,%s", sendKey, i, value);
		}
		this.add(ControlCode.INITIALIZE_SET.getString() + " " + message);
	}

	/*
	 * 設定ファイルの文字列から秒数の数値に変換する
	 */
	private int stringToSecond(String str, String name) throws LoggingPropertyException {
		try {
			if (str.endsWith("sec")) {
				return Integer.parseInt(str.substring(0, str.length() - "sec".length()));
			} else if (str.endsWith("min")) {
				return Integer.parseInt(str.substring(0, str.length() - "min".length())) * 60;
			} else {
				throw new LoggingPropertyException(MessageConstant.getValidateStrSerect(name));
			}
		} catch (NumberFormatException e) {
			throw new LoggingPropertyException(MessageConstant.getValidateStrSerect(name));
		}
	}
}