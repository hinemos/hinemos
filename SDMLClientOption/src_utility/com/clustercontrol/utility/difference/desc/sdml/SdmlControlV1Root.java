/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.difference.desc.sdml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clustercontrol.utility.difference.DiffAnnotation;

/**
 * SDML制御設定(1.x)比較用クラス
 * 本体側にJSONで定義できないためにアノテーションを付与したクラスに詰め替える
 *
 */
@DiffAnnotation(value={
		"{\"type\":\"Root\", \"funcName\":\"SdmlControlV1List_funcName\"}",
		"{\"type\":\"OrderBy\"," +
		"\"props\":[" +
			"\"ApplicationId\"," +
			"\"Description\"," +
			"\"OwnerRoleId\"," +
			"\"FacilityId\"," +
			"\"Scope\"," +
			"\"ValidFlg\"," +
			"\"ControlLogDirectory\"," +
			"\"ControlLogFilename\"," +
			"\"ControlLogCollectFlg\"," +
			"\"Application\"," +
			"\"AutoMonitorDeleteFlg\"," +
			"\"AutoMonitorCalendarId\"," +
			"\"EarlyStopThresholdSecond\"," +
			"\"EarlyStopNotifyPriority\"," +
			"\"AutoCreateSuccessPriority\"," +
			"\"AutoEnableSuccessPriority\"," +
			"\"AutoDisableSuccessPriority\"," +
			"\"AutoUpdateSuccessPriority\"," +
			"\"AutoControlFailedPriority\"" +
		"]}"
		})
public class SdmlControlV1Root {
	public Map<String, SdmlControlInfoV1> sdmlControlV1List = new HashMap<>();

	@DiffAnnotation("{\"type\":\"Comparison\"}")
	public SdmlControlInfoV1[] getSdmlControlV1List() {
		List<SdmlControlInfoV1> list = new ArrayList<>(sdmlControlV1List.values());
		Collections.sort(list, new Comparator<SdmlControlInfoV1>() {
			@Override
			public int compare(SdmlControlInfoV1 o1, SdmlControlInfoV1 o2) {
				return o1.getApplicationId().compareTo(o2.getApplicationId());
			}
		});
		return list.toArray(new SdmlControlInfoV1[0]);
	}

	public static SdmlControlV1Root getCopiedInstance(com.clustercontrol.utility.settings.sdml.xml.SdmlControlV1List xmlInfo) {
		SdmlControlV1Root root = new SdmlControlV1Root();
		for (com.clustercontrol.utility.settings.sdml.xml.SdmlControlInfoV1 src : xmlInfo.getSdmlControlInfoV1()) {
			SdmlControlInfoV1 dst = root.sdmlControlV1List.get(src.getApplicationId());
			if (dst == null) {
				dst = SdmlControlInfoV1.getCopiedInstance(src);
				root.sdmlControlV1List.put(src.getApplicationId(), dst);
			}
		}
		return root;
	}
}
