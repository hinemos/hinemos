/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logging.property;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.exception.LoggingPropertyException;

public class LoggingProperty extends Properties {

	private static final long serialVersionUID = 1L;

	public LoggingProperty() {
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
		defProp.setProperty(PropertyConstant.INFO_INTERVAL, "60");
		defProp.setProperty(PropertyConstant.FAILD_MAX_COUNT, "10");
		return defProp;
	}

	public void loadProperty(InputStream in) throws IOException, LoggingPropertyException {
		this.defaults = new Properties(getDefaultProperties());
		// 文字コードを指定して読み込む
		try (InputStreamReader isr = new InputStreamReader(in, LoggingConstant.CONFIG_CHARSET);
				BufferedReader reader = new BufferedReader(isr)) {
			this.load(reader);
		}

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

		for (String key : getInternalLogLoggers()) {
			InitValidater.validInternalLogLogger(this, key);
		}
	}

	public String getProperty(String key, int num) {
		String rtn;
		try {
			key = String.format(key, num);
			rtn = getProperty(key);
		} catch (IllegalFormatException e) {
			return null;
		}
		return rtn;
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

	public Integer getIntProperty(String key, int num) {
		Integer rtn;
		try {
			key = String.format(key, num);
			rtn = Integer.parseInt(getProperty(key));
		} catch (NumberFormatException | IllegalFormatException e) {
			return null;
		}
		return rtn;
	}

	/**
	 * 複数設定の場合に別の番号の設定にも同じ設定を反映する
	 * 
	 * @param key
	 * @param srcNum
	 * @param dstNum
	 */
	public void replaceWithSameValue(String key, int srcNum, int dstNum) {
		String srcVal = getProperty(key, srcNum);
		if (srcVal == null) {
			return;
		}
		String dstKey = String.format(key, dstNum);
		setProperty(dstKey, srcVal);
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
	 * @return 複数設定した番号のリスト（昇順）
	 */
	public List<Integer> getGccNumbers() {
		LinkedHashSet<Integer> set = new LinkedHashSet<Integer>();
		for (Object key : this.keySet()) {
			String k = key.toString();
			if (k.matches(".*gc.count.\\d..*")) {
				set.add(Integer.valueOf(k.replaceAll("[^0-9]", "")));
			}
		}
		List<Integer> list = new ArrayList<>(set);
		Collections.sort(list);
		return list;
	}

	public List<String> getInternalLogLoggers() {
		ArrayList<String> loggers = new ArrayList<String>();
		for (Object key : keySet()) {
			String k = key.toString();
			if (k.matches(PropertyConstant.INTERNAL_LOG_ROOT_LOGGER)) {
				continue;
			}
			if (k.matches(PropertyConstant.INTERNAL_LOG_LOGGER_PREFIX)) {
				loggers.add(k);
			}
		}
		return loggers;
	}
}
