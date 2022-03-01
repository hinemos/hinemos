/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.util.QueryUtil;
import com.clustercontrol.util.Singletons;

public class SdmlVersionManager {
	private static Log logger = LogFactory.getLog(SdmlVersionManager.class);

	private Map<String, ISdmlOption> sdmlOptionMap;

	/**
	 * コンストラクタではなく、{@link Singletons#get(Class)}を使用してください。
	 */
	public SdmlVersionManager() {
		sdmlOptionMap = new ConcurrentHashMap<>();
	}

	/**
	 * オプションの追加
	 * 
	 * @param version
	 * @param option
	 */
	public void addSdmlOption(String version, ISdmlOption option) {
		logger.info("addOption() : SDML version=" + option.getVersion());
		sdmlOptionMap.put(version, option);
	}

	public List<StringSampleTag> extractTagsFromMonitoringLog(String monitorId, String message) {
		ISdmlOption option = getOptionByMonitorId(monitorId);
		if (option == null) {
			return new ArrayList<>();
		}
		return option.extractTagsFromMonitoringLog(message);
	}

	public List<String> getSampleTagList(String monitorId, String sdmlMonitorTypeId) {
		ISdmlOption option = getOptionByMonitorId(monitorId);
		if (option == null) {
			return new ArrayList<>();
		}
		return option.getSampleTagList(sdmlMonitorTypeId);
	}

	/**
	 * 監視項目IDから該当バージョンのオプションを取得
	 * 
	 * @param monitorId
	 * @return
	 */
	private ISdmlOption getOptionByMonitorId(String monitorId) {
		SdmlControlSettingInfo controlSetting = null;
		try {
			// monitorIdから制御設定を取得
			controlSetting = QueryUtil.getSdmlControlSettingInfoByMonitorId(monitorId);
		} catch (SdmlControlSettingNotFound e) {
			logger.warn("getOptionByMonitorId() : " + e.getMessage());
			return null;
		} catch (HinemosUnknown e) {
			// 重複は通常起こりえない
			logger.error("getOptionByMonitorId() : " + e.getMessage(), e);
			return null;
		}

		ISdmlOption option = sdmlOptionMap.get(controlSetting.getVersion());
		if (option == null) {
			logger.error("getOptionByMonitorId() : option is missing. version=" + controlSetting.getVersion());
		}
		return option;
	}
}
