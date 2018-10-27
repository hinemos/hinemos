/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.bean;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.hub.bean.ValueType;

/**
 * 文字列監視の収集でHinemosが自動で抽出するタグ
 * 
 */
public enum CollectStringTag {
	TIMESTAMP_IN_LOG(ValueType.number),
	TIMESTAMP_RECIEVED(ValueType.number),
	facility(ValueType.string),
	severity(ValueType.string),
	hostname(ValueType.string),
	message(ValueType.string),
	filename(ValueType.string),
	TYPE(ValueType.string),
	KEY(ValueType.string),
	MSG(ValueType.string),
	FacilityID(ValueType.string);
	
	private final ValueType valueType;

	private CollectStringTag(ValueType valueType) {
		this.valueType = valueType;
	}

	public ValueType valueType() {
		return valueType;
	}

	/**
	 * 監視設定ごとにHinemosが自動で抽出するタグのリストを返す
	 * 
	 * @param monitorTypeId 監視種別ID
	 * @return タグリスト
	 */
	public static List<String> getSampleTagList(String monitorTypeId) {
		List<String> rtn = new ArrayList<>();
		if (monitorTypeId == null || monitorTypeId.isEmpty()) {
			return rtn;
		}
		// 文字列監視、バイナリ監視
		rtn.add(CollectStringTag.TIMESTAMP_RECIEVED.name());
		if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)) {
			// システムログ監視
			rtn.add(CollectStringTag.TIMESTAMP_IN_LOG.name());
			rtn.add(CollectStringTag.facility.name());
			rtn.add(CollectStringTag.severity.name());
			rtn.add(CollectStringTag.hostname.name());
			rtn.add(CollectStringTag.message.name());
		} else if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_LOGFILE)) {
			// ログファイル監視
			rtn.add(CollectStringTag.filename.name());
		} else if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)) {
			// カスタムトラップ監視（文字列）
			rtn.add(CollectStringTag.TIMESTAMP_IN_LOG.name());
			rtn.add(CollectStringTag.TYPE.name());
			rtn.add(CollectStringTag.KEY.name());
			rtn.add(CollectStringTag.MSG.name());
			rtn.add(CollectStringTag.FacilityID.name());
		}
		return rtn;
	}
}
