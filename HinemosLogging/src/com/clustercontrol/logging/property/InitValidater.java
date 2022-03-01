/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging.property;

import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.exception.LoggingPropertyException;
import com.clustercontrol.logging.util.CommonValidater;

/**
 * ログ出力のための初期設定のバリデーションを行うクラスです。 ここでのエラーはログ出力先が無いため、ログの出力は行わない。<BR>
 */
public class InitValidater {

	// -------------------------------------------------------------------------------------------------------------------
	// 初期処理 共通項目
	// -------------------------------------------------------------------------------------------------------------------

	public static void validAppId(LoggingProperty prop, String key) throws LoggingPropertyException {
		String value = prop.getProperty(key);
		CommonValidater.validateId(key, value, 64);
	}

	public static void validCommonLogDirPath(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		if (!(prop.getProperty(PropertyConstant.CONTROL_LOG_FILE_PATH) == null)
				&& !(prop.getProperty(PropertyConstant.INTERNAL_LOG_FILE_PATH) == null)
				&& !(prop.getProperty(PropertyConstant.MON_LOG_FILE_PATH) == null)) {
			required = false;
		}
		String value = prop.getProperty(key);
		CommonValidater.validateStringLength(key, value, required, 0, 1024);
	}

	// -------------------------------------------------------------------------------------------------------------------
	// 初期処理 SDML制御ログ出力設定
	// -------------------------------------------------------------------------------------------------------------------

	public static void validControlLogDirPath(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		CommonValidater.validateStringLength(key, value, required, 0, 1024);
	}

	public static void validControlLogName(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		CommonValidater.validateStringLength(key, value, required, 0, 1024);
	}

	public static void validControlLogSize(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		CommonValidater.validateIntIsFileSize(key, value, required);
	}

	public static void validControlLogGeneration(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		CommonValidater.validateStringToInteger(key, value, required);
	}

	// -------------------------------------------------------------------------------------------------------------------
	// 初期処理 Hinemosロギング内部ログ出力設定
	// -------------------------------------------------------------------------------------------------------------------

	public static void validInternalLogDirPath(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		CommonValidater.validateString(key, value, required);
	}

	public static void validInternalLogName(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		CommonValidater.validateStringLength(key, value, required, 0, 1024);
	}

	public static void validInternalLogSize(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		CommonValidater.validateIntIsFileSize(key, value, required);
	}

	public static void validInternalLogGeneration(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		CommonValidater.validateStringToInteger(key, value, required);
	}

	public static void validInternalLogrootLogger(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = true;
		String value = prop.getProperty(key);

		CommonValidater.validateString(key, value, required);
	}

	public static void validInternalLogLogger(LoggingProperty prop, String key) throws LoggingPropertyException {
		boolean required = false;
		String value = prop.getProperty(key);

		CommonValidater.validateString(key, value, required);
	}

}
