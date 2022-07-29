/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.util;

import com.clustercontrol.platform.HinemosPropertyDefault;

/**
 * Hinemosで扱う一時ファイルの種類一覧
 * ・仕様上、ファイル名が固定な場合は fileName を指定する
 * ・ファイル名が変動する場合(HogeInfo.getName() が使用される場合)は
 *   空文字を指定し、各実装でファイル名を補完する
 * Hinemosで扱う一時ファイルの種類一覧を管理するクラス。
 * prefixで一時ファイルのプレフィックスを指定する。
 */
public enum RestTempFileType {

	// 構成情報検索
	REPOSITORY_NODECONFIG("repository_nodeconfig-",
			HinemosPropertyDefault.node_config_export_dir.getStringValue()),
	// 監視履歴 - イベント通知結果レポート出力
	MONITORRESULT_EVENT("monitorresult_event-",
			HinemosPropertyDefault.performance_export_dir.getStringValue()),
	// 収集蓄積 - 収集データ(バイナリ)ダウンロード
	HUB_BINARY("hub_binary-",
			HinemosPropertyDefault.binary_export_dir.getStringValue()),
	// 環境構築 - 環境構築ファイルダウンロード
	INFRA_FILE("infra_file-",
			HinemosPropertyDefault.infra_export_dir.getStringValue()),
	// ジョブマップ - ジョブマップアイコン画像ダウンロード
	JOBMAP_ICONIMAGE("jobmap_iconimage-",
			HinemosPropertyDefault.jobmap_export_dir.getStringValue()),
	// RPAシナリオジョブ - スクリーンショットダウンロード
	JOB_RPA_SCREENSHOT("job_rpa_screenshot-",
			HinemosPropertyDefault.job_rpa_screenshot_export_dir.getStringValue()),
	// ノードマップ- 
	NODEMAP_BGIMAGE("nodemap_bgimage-",
			HinemosPropertyDefault.nodemap_export_dir.getStringValue()),
	// ノードマップ- 
	NODEMAP_ICONIMAGE("nodemap_iconimage-",
			HinemosPropertyDefault.nodemap_export_dir.getStringValue()),
	// ノードマップ- 
	NODEMAP_NODECONFIG("nodemap_nodeconfig-",
			HinemosPropertyDefault.nodemap_export_dir.getStringValue()),
	// クラウド - 課金詳細情報エクスポート
	BINARY("binary-",
				HinemosPropertyDefault.binary_export_dir.getStringValue()),
	// クラウド - 課金詳細情報エクスポート
	CLOUD("hinemos_cloud_billing_detail_",
			HinemosPropertyDefault.cloud_export_dir.getStringValue());

	private final String prefix;
	private final String dir;
	
	private RestTempFileType(final String prefix, final String dir) {
		this.prefix = prefix;
		this.dir = dir;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public String getDir() {
		return dir;
	}
}
