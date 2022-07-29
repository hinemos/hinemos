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

import com.clustercontrol.hub.bean.ValueType;

/**
 * 文字列監視の収集でHinemosが自動で抽出するタグ<br>
 * 通常のタグとは別にSDMLで独自に抽出するタグを定義する<br>
 * 
 * @see com.clustercontrol.hub.bean.CollectStringTag
 */
public enum SdmlCollectStringTag {
	TIMESTAMP_IN_LOG(ValueType.number),
	PID(ValueType.number),
	// 制御ログ用
	FileName(ValueType.string),
	Hostname(ValueType.string),
	ApplicationID(ValueType.string),
	SDMLControlCode(ValueType.string),
	// 自動作成監視設定用
	LogLevel(ValueType.string),
	ItemName(ValueType.string),
	Threshold(ValueType.number),
	Remaining(ValueType.number),
	Count(ValueType.number),
	GCName(ValueType.string),
	Usage(ValueType.number);


	private final ValueType valueType;

	private SdmlCollectStringTag(ValueType valueType) {
		this.valueType = valueType;
	}

	public ValueType valueType() {
		return valueType;
	}

	/**
	 * SDML監視種別ごとにHinemosが自動で抽出するタグのリストを返す<br>
	 * ※V1時点では既存のログファイル監視との差分のみ定義している<br>
	 * ※タグに変更がある場合は重複しないように注意する必要がある<br>
	 * 
	 * @param sdmlMonitorTypeId
	 * @return
	 */
	public static List<String> getSampleTagList(String sdmlMonitorTypeId) {
		List<String> rtn = new ArrayList<>();
		if (sdmlMonitorTypeId == null || sdmlMonitorTypeId.isEmpty()) {
			return rtn;
		}
		// 共通
		rtn.add(SdmlCollectStringTag.TIMESTAMP_IN_LOG.name());

		switch (SdmlMonitorTypeEnum.toEnum(sdmlMonitorTypeId)) {
		case LOG_APPLICATION:
			rtn.add(SdmlCollectStringTag.LogLevel.name());
			break;
		case INTERNAL_DEADLOCK:
			rtn.add(SdmlCollectStringTag.ItemName.name());
			break;
		case INTERNAL_HEAP_REMAINING:
			rtn.add(SdmlCollectStringTag.ItemName.name());
			rtn.add(SdmlCollectStringTag.Remaining.name());
			rtn.add(SdmlCollectStringTag.Threshold.name());
			break;
		case INTERNAL_GC_COUNT:
			rtn.add(SdmlCollectStringTag.ItemName.name());
			rtn.add(SdmlCollectStringTag.Count.name());
			rtn.add(SdmlCollectStringTag.Threshold.name());
			rtn.add(SdmlCollectStringTag.GCName.name());
			break;
		case INTERNAL_CPU_USAGE:
			rtn.add(SdmlCollectStringTag.ItemName.name());
			rtn.add(SdmlCollectStringTag.Usage.name());
			rtn.add(SdmlCollectStringTag.Threshold.name());
			rtn.add(SdmlCollectStringTag.PID.name());
			break;
		default:
			// 該当するものがなければ空で返す
			return new ArrayList<>();
		}
		return rtn;
	}
}
