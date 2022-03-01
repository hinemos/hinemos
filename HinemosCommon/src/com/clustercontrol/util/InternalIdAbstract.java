/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

/**
 * INTERNALイベントの以下の情報を取得できるインタフェース<BR>
 *   INTERNAL_ID
 *   重要度
 *   プラグインID
 *   メッセージ
 * (アプリケーションはプラグインIDを基に付与するため不要)
 * 
 * 実装先はEnumを想定
 * 本インターフェースを実装するクラス間でINTERNAL_IDは重複しない値を設定すること。
 * 
 */
public interface InternalIdAbstract {

	Integer getPriority();
	String getPluginId();
	String getMessage(String... args);
	String getInternalId();
}

