/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.sdml.model.SdmlInitializeData;
import com.clustercontrol.sdml.util.QueryUtil;
import com.clustercontrol.sdml.v1.constant.InitializeKeyEnum;

public class InitializeDataManager {
	private static Log logger = LogFactory.getLog(InitializeDataManager.class);

	private String applicationId;
	private String facilityId;

	private static final String DELIMETER = ",";
	private static final String SURROUND = "\"";

	public InitializeDataManager(String applicationId, String facilityId) {
		this.applicationId = applicationId;
		this.facilityId = facilityId;
	}

	/**
	 * 該当のアプリケーションIDとファシリティIDに紐づく初期化情報を全て取得する
	 * 
	 * @return
	 */
	public Map<String, SdmlInitializeData> getInitializeDataMap() {
		Map<String, SdmlInitializeData> rtn = new HashMap<>();
		for (SdmlInitializeData data : QueryUtil.getSdmlInitializeDataByApplicationIdAndFacilityId(this.applicationId,
				this.facilityId)) {
			rtn.put(data.getKey(), data);
		}
		return rtn;
	}

	/**
	 * SDML制御ログのメッセージ部から初期化情報を作成する
	 * 
	 * @param message
	 * @return
	 */
	public SdmlInitializeData createInitializeData(String message) throws HinemosUnknown {
		if (message == null || message.isEmpty()) {
			throw new HinemosUnknown("message is empty.");
		}
		String[] results = parseMessage(message);
		SdmlInitializeData data = new SdmlInitializeData(this.applicationId, this.facilityId, results[0]);
		data.setValue(results[1]);
		return data;
	}

	// メッセージ部の文字列解析
	private String[] parseMessage(final String message) throws HinemosUnknown {
		String key;
		String value;
		if (!message.contains(DELIMETER)) {
			throw new HinemosUnknown("parse failed. message is not contatins delimeter.");
		}
		try {
			// KeyとValueに分ける
			key = message.substring(0, message.indexOf(DELIMETER));
			value = message.substring(message.indexOf(DELIMETER) + 1);
			// 文字列はダブルクォーテーションで囲われているため取り除く
			StringBuilder sb = new StringBuilder(value);
			if (value.startsWith(SURROUND)) {
				sb.deleteCharAt(0);
			}
			if (value.endsWith(SURROUND)) {
				sb.deleteCharAt(sb.length() - 1);
			}
			value = sb.toString();
		} catch (Exception e) {
			throw new HinemosUnknown("parse failed. " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		return new String[] { key, value };
	}

	/**
	 * 初期化情報のKeyが全て揃っているか確認する
	 * 
	 * @param dataMap
	 * @return
	 */
	public boolean checkInitializeDataAvailable(Map<String, SdmlInitializeData> dataMap) {
		logger.debug("checkInitializeDataAvailable() : start.");
		List<InitializeKeyEnum> logAppFilterKeys = InitializeKeyEnum.getLogAppFilterKeys();
		List<InitializeKeyEnum> intGccKeys = InitializeKeyEnum.getIntGccKeys();
		int logAppFilterCount = 0;
		int intGccCount = 0;
		try {
			logAppFilterCount = Integer.parseInt(dataMap.get(InitializeKeyEnum.LogAppFilterNCount.name()).getValue());
			intGccCount = Integer.parseInt(dataMap.get(InitializeKeyEnum.IntGccNCount.name()).getValue());
		} catch (NullPointerException e) {
			// Keyがない場合はその時点でNG
			logger.debug("checkInitializeDataAvailable() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			return false;
		} catch (NumberFormatException e) {
			// parseIntで失敗した場合もKeyの確認ができないのでNG
			logger.debug("checkInitializeDataAvailable() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			return false;
		}
		for (int i = 1; i <= logAppFilterCount; i++) {
			for (InitializeKeyEnum key : logAppFilterKeys) {
				if (dataMap.get(key.name(i)) == null) {
					// 該当のKeyがなければNG
					logger.debug("checkInitializeDataAvailable() : not enough " + key.name(i));
					return false;
				}
			}
		}
		for (int i = 1; i <= intGccCount; i++) {
			for (InitializeKeyEnum key : intGccKeys) {
				if (dataMap.get(key.name(i)) == null) {
					// 該当のKeyがなければNG
					logger.debug("checkInitializeDataAvailable() : not enough " + key.name(i));
					return false;
				}
			}
		}
		// 単一のKeyをチェック
		for (InitializeKeyEnum key : InitializeKeyEnum.values()) {
			if (logAppFilterKeys.contains(key) || intGccKeys.contains(key)) {
				continue;
			} else {
				if (dataMap.get(key.name()) == null) {
					// 該当のKeyがなければNG
					logger.debug("checkInitializeDataAvailable() : not enough " + key.name());
					return false;
				}
			}
		}
		return true;
	}
}
