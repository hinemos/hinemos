/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

/**
 * 構成情報で使用する定数
 *   
 */
public class NodeConfigSettingConstant {

	// 構成情報検索 登録先スコープ名
	public static final String NODE_CONFIG_SCOPE = FacilityTreeAttributeConstant.NODE_CONFIGURATION_SCOPE;

	// 構成情報検索 登録先スコープ名
	public static final String NODE_CONFIG_NODE_PREFIX = "NodeConfiguration_";

	// 構成情報検索 ダウンロードファイル名
	public static final String NODE_CONFIG_FILE_PREFIX = "NodeConfiguration_";

	// 構成情報履歴詳細 登録日時(TO) デフォルト値
	public static final Long REG_DATE_TO_DEFAULT_VALUE = 9999999999999L;
}
