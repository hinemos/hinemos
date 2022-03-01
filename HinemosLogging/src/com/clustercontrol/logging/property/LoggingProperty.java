/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logging.property;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Properties;

import com.clustercontrol.logging.constant.PropertyConstant;

public class LoggingProperty extends Properties {

	private static final long serialVersionUID = 1L;

	private static final LoggingProperty INSTANCE = new LoggingProperty();

	private LoggingProperty() {
	}

	public static LoggingProperty getInstance() {
		return INSTANCE;
	}

	/**
	 * hinemoslogging.cfgの必須でない項目は省略可能、 省略した場合のデフォルト値を設定する<BR>
	 */
	private Properties getDefaultProperties() {
		Properties defProp = new Properties();
		defProp.setProperty(PropertyConstant.PRC_DESCRIPTION, "Auto registered by SDML Controller.");
		defProp.setProperty(PropertyConstant.PRC_THRESHOLD_INFO, "1,99");
		defProp.setProperty(PropertyConstant.PRC_THRESHOLD_WARN, "99,99");
		defProp.setProperty(PropertyConstant.PRC_GET_COMMAND_PATH, "");
		defProp.setProperty(PropertyConstant.PRC_GET_TIMEOUT, "60");
		defProp.setProperty(PropertyConstant.MON_LOG_SEPARATION_TYPE, "HeadPattern");
		defProp.setProperty(PropertyConstant.MON_LOG_SEPARATION_VALUE,
				"\\d{4}-\\d{2}-\\d{2}T\\d{2}\\:\\d{2}\\:\\d{2},\\d{3}[+-]\\d{2}\\:\\d{2}");
		defProp.setProperty(PropertyConstant.MON_LOG_MAX_BYTES, "5000");
		defProp.setProperty(PropertyConstant.LOG_APP_DESCRIPTION, "Auto registered by SDML Controller.");
		defProp.setProperty(PropertyConstant.INT_DLK_PRIORITY, "critical");
		defProp.setProperty(PropertyConstant.INT_DLK_DESCRIPTION, "Auto registered by SDML Controller.");
		defProp.setProperty(PropertyConstant.INT_DLK_TIMEOUT, "3");
		defProp.setProperty(PropertyConstant.INT_HPR_PRIORITY, "warning");
		defProp.setProperty(PropertyConstant.INT_HPR_DESCRIPTION, "Auto registered by SDML Controller.");
		defProp.setProperty(PropertyConstant.INT_HPR_TIMEOUT, "3");
		defProp.setProperty(PropertyConstant.INT_CPU_PRIORITY, "warning");
		defProp.setProperty(PropertyConstant.INT_CPU_DESCRIPTION, "Auto registered by SDML Controller.");
		defProp.setProperty(PropertyConstant.INT_CPU_TIMEOUT, "3");
		defProp.setProperty(PropertyConstant.INFORM_INTERVAL, "60");
		defProp.setProperty(PropertyConstant.FAILD_MAX_COUNT, "10");
		return defProp;
	}

	public void loadProperty(InputStream in) throws Exception {
		this.defaults = new Properties(getDefaultProperties());
		this.load(in);

		// 複数項目の要素数ごとにデフォルト値を設定する
		if (!getMonStrFilterNumbers().isEmpty()) {
			for (Integer monStrFilNum : getMonStrFilterNumbers()) {
				String logAppFilterDescKey = String.format(PropertyConstant.LOG_APP_FILTER_DESCRIPTION, monStrFilNum);
				if (this.getProperty(logAppFilterDescKey) == null) {
					this.setProperty(logAppFilterDescKey, "Auto registered by SDML Controller.");
				}
			}
		}
		if (!getGccNumbers().isEmpty()) {
			for (Integer gccPropNum : getGccNumbers()) {
				String gccDescKey = String.format(PropertyConstant.INT_GCC_DESCRIPTION, gccPropNum);
				String gccTimeoutKey = String.format(PropertyConstant.INT_GCC_TIMEOUT, gccPropNum);
				if (this.getProperty(gccDescKey) == null) {
					this.setProperty(gccDescKey, "Auto registered by SDML Controller.");
				}
				if (this.getProperty(gccTimeoutKey) == null) {
					this.setProperty(gccTimeoutKey, "3");
				}
			}
		}

		// 設定ファイル読み込み後、ログ出力に必要な値のバリデーションを行う
		InitValidater.validAppId(this, PropertyConstant.APP_ID);

		InitValidater.validCommonLogDirPath(this, PropertyConstant.LOG_FILE_PATH);

		InitValidater.validControlLogDirPath(this, PropertyConstant.CONTROL_LOG_FILE_PATH);

		InitValidater.validControlLogName(this, PropertyConstant.CONTROL_LOG_FILE_NAME);

		InitValidater.validControlLogSize(this, PropertyConstant.CONTROL_LOG_FILE_SIZE);

		InitValidater.validControlLogGeneration(this, PropertyConstant.CONTROL_LOG_FILE_GENERATION);

		InitValidater.validInternalLogDirPath(this, PropertyConstant.INTERNAL_LOG_FILE_PATH);

		InitValidater.validInternalLogName(this, PropertyConstant.INTERNAL_LOG_FILE_NAME);

		InitValidater.validInternalLogSize(this, PropertyConstant.INTERNAL_LOG_FILE_SIZE);

		InitValidater.validInternalLogGeneration(this, PropertyConstant.INTERNAL_LOG_FILE_GENERATION);

		InitValidater.validInternalLogrootLogger(this, PropertyConstant.INTERNAL_LOG_ROOT_LOGGER);

		for (String key : PropertyConstant.INTERNAL_LOG_LOGGERS) {
			InitValidater.validInternalLogLogger(this, key);
		}
	}

	public Integer getIntProperty(String key) {
		Integer rtn;
		try {
			rtn = Integer.parseInt(getProperty(key));
		} catch (NumberFormatException e) {
			return null;
		}
		return rtn;
	}

	/**
	 * 設定ファイルで複数設定したフィルタ設定の「～.application.filter.N」のNを取得する
	 * 
	 * @return 複数設定した番号のリスト
	 */
	public LinkedHashSet<Integer> getMonStrFilterNumbers() {
		LinkedHashSet<Integer> list = new LinkedHashSet<Integer>();
		for (Object key : this.keySet()) {
			String k = key.toString();
			if (k.matches(".*application.filter.\\d..*")) {
				list.add(Integer.valueOf(k.replaceAll("[^0-9]", "")));
			}
		}
		return list;
	}

	/**
	 * 設定ファイルで複数設定したGC発生頻度監視の「～gc.count.N」のNを取得する
	 * 
	 * @return 複数設定した番号のリスト
	 */
	public LinkedHashSet<Integer> getGccNumbers() {
		LinkedHashSet<Integer> list = new LinkedHashSet<Integer>();
		for (Object key : this.keySet()) {
			String k = key.toString();
			if (k.matches(".*gc.count.\\d..*")) {
				list.add(Integer.valueOf(k.replaceAll("[^0-9]", "")));
			}
		}
		return list;
	}
}
