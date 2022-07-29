/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging.property;

import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.constant.MessageConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.exception.LoggingPropertyException;
import com.clustercontrol.logging.util.CommonValidater;
import com.clustercontrol.logging.util.PriorityType;

public class LoggingPropertyValidater {

	private static InternalLogManager.Logger internalLog = InternalLogManager.getLogger(CommonValidater.class);

	// -------------------------------------------------------------------------------------------------------------------
	// 死活監視/プロセス死活監視
	// -------------------------------------------------------------------------------------------------------------------

	public static void validPrcInterval(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);
		String[] prcIntSelect = { "30sec", "1min", "5min", "10min", "30min", "60min" };

		try {
			CommonValidater.validateStringSelect(key, value, required, prcIntSelect);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPrcInterval", value, e);
			throw e;
		}
	}

	public static void validPrcDescription(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringLength(key, value, required, 0, 256);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPrcDescription", value, e);
			throw e;
		}
	}

	public static void validPrcThresholdInfo(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateSplitComma(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPrcThresholdInfo", value, e);
			throw e;
		}
		String[] list = value.split(",");

		try {
			CommonValidater.validateStringToInteger(key, list[0], required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPrcThresholdInfo", list[0], e);
			throw e;
		}

		try {
			CommonValidater.validateStringToInteger(key, list[1], required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPrcThresholdInfo", list[1], e);
			throw e;
		}
	}

	public static void validPrcThresholdWarn(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateSplitComma(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPrcThresholdWarn", value, e);
			throw e;
		}
		String[] list = value.split(",");

		try {
			CommonValidater.validateStringToInteger(key, list[0], required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPrcThresholdWarn", list[0], e);
			throw e;
		}

		try {
			CommonValidater.validateStringToInteger(key, list[1], required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPrcThresholdWarn", list[1], e);
			throw e;
		}
	}

	public static void validPrcCommandLine(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateString(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPrcCommandLine", value, e);
			throw e;
		}
	}

	public static void validPrcCommandPath(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		if (value == null) {
			return;
		}
		try {
			CommonValidater.validateString(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPrcCommandPath", value, e);
			throw e;
		}
	}

	public static void validPrcTimeout(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		if (value == null) {
			return;
		}
		try {
			CommonValidater.validateStringToInteger(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPrcTimeout", value, e);
			throw e;
		}
	}

	public static void validPrcMonitor(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPrcMonitor", value, e);
			throw e;
		}
	}

	public static void validPrcCollect(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPrcCollect", value, e);
			throw e;
		}
	}

	// -------------------------------------------------------------------------------------------------------------------
	// ログ監視/プロセス内部監視共通
	// -------------------------------------------------------------------------------------------------------------------

	public static void validMonitoringLogDirPath(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringLength(key, value, required, 0, 1024);
		} catch (LoggingPropertyException e) {
			internalLog.info("validMonitoringLogDirPath", value, e);
			throw e;
		}
	}

	public static void validMonitoringLogName(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringLength(key, value, required, 0, 1024);
		} catch (LoggingPropertyException e) {
			internalLog.info("validMonitoringLogName", value, e);
			throw e;
		}
	}

	public static void validMonitoringLogSize(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateIntIsFileSize(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validMonitoringLogSize", value, e);
			throw e;
		}
	}

	public static void validMonitoringLogGeneration(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToInteger(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validMonitoringLogGeneration", value, e);
			throw e;
		}
	}

	public static void validMonitoringLogSeparationType(LoggingProperty prop, String key)
			throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);
		String[] sepTypeSelect = { "HeadPattern", "TailPattern", "FileReturnCode" };

		try {
			CommonValidater.validateStringSelect(key, value, required, sepTypeSelect);
		} catch (LoggingPropertyException e) {
			internalLog.info("validMonitoringLogSeparationType", value, e);
			throw e;
		}
	}

	public static void validMonitoringLogSeparationValue(LoggingProperty prop, String key)
			throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		if (prop.getProperty(PropertyConstant.MON_LOG_SEPARATION_TYPE).equals("FileReturnCode")) {
			String[] newLineCode = { "LF", "CR", "CRLF" };
			try {
				if (!(value == null)) {
					CommonValidater.validateStringSelect(key, value, required, newLineCode);
				}
			} catch (LoggingPropertyException e) {
				internalLog.info("validMonitoringLogSeparationValue", value, e);
				throw e;
			}
		} else {
			try {
				CommonValidater.validateStringLength(key, value, required, 0, 1024);
			} catch (LoggingPropertyException e) {
				internalLog.info("validMonitoringLogSeparationValue", value, e);
				throw e;
			}
		}
	}

	public static void validMonitoringLogMaxBytes(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToInteger(key, value, required);
			CommonValidater.validateInt(key, Integer.parseInt(value), 0, 2147483647);
		} catch (LoggingPropertyException e) {
			internalLog.info("validMonitoringLogMaxBytes", value, e);
			throw e;
		}
	}

	// -------------------------------------------------------------------------------------------------------------------
	// ログ監視/アプリケーションログ監視
	// -------------------------------------------------------------------------------------------------------------------

	public static void validLogAppLevel(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);
		String[] appLevSelect = { "OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL" };

		try {
			CommonValidater.validateStringSelect(key, value, required, appLevSelect);
		} catch (LoggingPropertyException e) {
			internalLog.info("validLogAppLevel", value, e);
			throw e;
		}
	}

	public static void validLogAppDescription(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringLength(key, value, required, 0, 256);
		} catch (LoggingPropertyException e) {
			internalLog.info("validLogAppDescription", value, e);
			throw e;
		}
	}

	public static void validLogAppFilterDescription(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringLength(key, value, required, 0, 256);
		} catch (LoggingPropertyException e) {
			internalLog.info("validDescription", value, e);
			throw e;
		}
	}

	public static void validLogAppFilterPattern(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringLength(key, value, required, 0, 1024);
		} catch (LoggingPropertyException e) {
			internalLog.info("validPattern", value, e);
			throw e;
		}
	}

	public static void validLogAppFilterProcess(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validProcess", value, e);
			throw e;
		}
	}

	public static void validLogAppFilterSensitivity(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validSensitivity", value, e);
			throw e;
		}
	}

	public static void validLogAppFilterPriority(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringSelect(key, value, required, PriorityType.getStringValues());
		} catch (LoggingPropertyException e) {
			internalLog.info("validPriority", value, e);
			throw e;
		}
	}

	public static void validLogAppFilterMessage(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringLength(key, value, required, 0, 256);
		} catch (LoggingPropertyException e) {
			internalLog.info("validMessage", value, e);
			throw e;
		}
	}

	public static void validLogAppMonitor(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validLogAppMonitor", value, e);
			throw e;
		}
	}

	public static void validLogAppCollect(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validLogAppCollect", value, e);
			throw e;
		}
	}

	// -------------------------------------------------------------------------------------------------------------------
	// プロセス内部監視/デッドロック監視
	// -------------------------------------------------------------------------------------------------------------------

	public static void validIntDlkInterval(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToInteger(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntDlkInterval", value, e);
			throw e;
		}
	}

	public static void validIntDlkPriority(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringSelect(key, value, required, PriorityType.getStringValues());
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntDlkPriority", value, e);
			throw e;
		}
	}

	public static void validIntDlkDescription(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringLength(key, value, required, 0, 256);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntDlkDescription", value, e);
			throw e;
		}
	}

	public static void validIntDlkTimeout(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		if (value == null) {
			return;
		}
		try {
			CommonValidater.validateStringToInteger(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntDlkTimeout", value, e);
			throw e;
		}
	}

	public static void validIntDlkMonitor(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntDlkMonitor", value, e);
			throw e;
		}
	}

	public static void validIntDlkCollect(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntDlkCollect", value, e);
			throw e;
		}
	}

	// -------------------------------------------------------------------------------------------------------------------
	// プロセス内部監視/ヒープ未使用量監視
	// -------------------------------------------------------------------------------------------------------------------

	public static void validIntHprInterval(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToInteger(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntHprInterval", value, e);
			throw e;
		}
	}

	public static void validIntHprPriority(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringSelect(key, value, required, PriorityType.getStringValues());
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntHprPriority", value, e);
			throw e;
		}
	}

	public static void validIntHprThreshold(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		if (value == null) {
			return;
		}
		try {
			CommonValidater.validateStringToInteger(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntHprThreshold", value, e);
			throw e;
		}
	}

	public static void validIntHprDescription(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringLength(key, value, required, 0, 256);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntHprDescription", value, e);
			throw e;
		}
	}

	public static void validIntHprTimeout(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		if (value == null) {
			return;
		}
		try {
			CommonValidater.validateStringToInteger(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntHprTimeout", value, e);
			throw e;
		}
	}

	public static void validIntHprMonitor(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntHprMonitor", value, e);
			throw e;
		}
	}

	public static void validIntHprCollect(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntHprCollect", value, e);
			throw e;
		}
	}

	// -------------------------------------------------------------------------------------------------------------------
	// プロセス内部監視/GC発生頻度監視
	// -------------------------------------------------------------------------------------------------------------------

	public static void validIntGccInterval(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToInteger(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validInterval", value, e);
			throw e;
		}
	}

	public static void validIntGccMethod(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		if ("all".equalsIgnoreCase(value)) {
			throw new LoggingPropertyException(MessageConstant.VALIDATE_GCC_ALL);
		}

		try {
			CommonValidater.validateString(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validMethod", value, e);
			throw e;
		}
	}

	public static void validIntGccPriority(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringSelect(key, value, required, PriorityType.getStringValues());
		} catch (LoggingPropertyException e) {
			internalLog.info("validPriority", value, e);
			throw e;
		}
	}

	public static void validIntGccThreshold(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		if (value == null) {
			return;
		}
		try {
			CommonValidater.validateStringToInteger(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validThreshold", value, e);
			throw e;
		}
	}

	public static void validIntGccDescription(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringLength(key, value, required, 0, 256);
		} catch (LoggingPropertyException e) {
			internalLog.info("validDescription", value, e);
			throw e;
		}
	}

	public static void validIntGccTimeout(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		if (value == null) {
			return;
		}
		try {
			CommonValidater.validateStringToInteger(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntGccTimeout", value, e);
			throw e;
		}
	}

	public static void validIntGccMonitor(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validMonitor", value, e);
			throw e;
		}
	}

	public static void validIntGccCollect(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validCollect", value, e);
			throw e;
		}
	}

	// -------------------------------------------------------------------------------------------------------------------
	// プロセス内部監視/CPU使用率監視
	// -------------------------------------------------------------------------------------------------------------------

	public static void validIntCpuInterval(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToInteger(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntCpuInterval", value, e);
			throw e;
		}
	}

	public static void validIntCpuPriority(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringSelect(key, value, required, PriorityType.getStringValues());
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntCpuPriority", value, e);
			throw e;
		}
	}

	public static void validIntCpuThreshold(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		if (value == null) {
			return;
		}
		try {
			CommonValidater.validateStringToInteger(key, value, required);
			CommonValidater.validateInt(key, Integer.parseInt(value), 0, 100);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntCpuThreshold", value, e);
			throw e;
		}
	}

	public static void validIntCpuDescription(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringLength(key, value, required, 0, 256);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntCpuDescription", value, e);
			throw e;
		}
	}

	public static void validIntCpuTimeout(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		if (value == null) {
			return;
		}
		try {
			CommonValidater.validateStringToInteger(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntCpuTimeout", value, e);
			throw e;
		}
	}

	public static void validIntCpuMonitor(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntCpuMonitor", value, e);
			throw e;
		}
	}

	public static void validIntCpuCollect(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToBool(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validIntCpuCollect", value, e);
			throw e;
		}
	}

	// -------------------------------------------------------------------------------------------------------------------
	// 監視以外
	// -------------------------------------------------------------------------------------------------------------------

	public static void validInfoInterval(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		if (value == null) {
			return;
		}
		try {
			CommonValidater.validateStringToInteger(key, value, required);
			CommonValidater.validateInt(key, Integer.parseInt(value), 0, 2147483647);
		} catch (LoggingPropertyException e) {
			internalLog.info("validInfoInterval", value, e);
			throw e;
		}
	}

	public static void validFaildMaxCount(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		try {
			CommonValidater.validateStringToInteger(key, value, required);
		} catch (LoggingPropertyException e) {
			internalLog.info("validFaildMaxCount", value, e);
			throw e;
		}
	}
}
