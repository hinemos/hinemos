/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.factory;

import java.util.List;

import com.clustercontrol.hub.bean.StringSampleTag;

/**
 * SDMLのバージョンごとのオプションを管理するためのインターフェイス<br>
 * マネージャ側から特定のバージョンに応じた処理を呼び出す必要がある場合、<br>
 * 本インターフェイスで定義し各オプションで実装する
 */
public interface ISdmlOption {

	String getVersion();

	List<StringSampleTag> extractTagsFromMonitoringLog(String message);

	List<String> getSampleTagList(String sdmlMonitorTypeId);
}
