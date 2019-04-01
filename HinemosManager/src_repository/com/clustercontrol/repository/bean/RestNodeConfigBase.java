/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.repository.bean;

import com.clustercontrol.util.JsonUtil;

/**
 * Rest-API返却用のノード構成情報の基本クラス.<br>
 * <br>
 * JSON形式で返却する想定.<br>
 * フィールド物理名がJSON項目名となる.<br>
 * 継承して利用する.
 * 
 * @since 6.2.0
 * @version 6.2.0
 */
public class RestNodeConfigBase {

	// JSON項目
	/** 収集日時(YYYYMMDDhhmmss) */
	protected String collected = null;

	/** 最終更新日時(YYYYMMDDhhmmss) */
	protected String lastUpdated = null;

	// getter setter
	/** 収集日時(YYYYMMDDhhmmss) */
	public String getCollected() {
		return collected;
	}

	/** 収集日時(YYYYMMDDhhmmss) */
	public void setCollected(String collected) {
		this.collected = collected;
	}

	/** 最終更新日時(YYYYMMDDhhmmss) */
	public String getLastUpdated() {
		return lastUpdated;
	}

	/** 最終更新日時(YYYYMMDDhhmmss) */
	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	// その他メソッド.
	/**
	 * JSON文字列に変換(ログ出力用).
	 */
	@Override
	public String toString() {
		StringBuilder jsonSb = new StringBuilder();
		jsonSb.append("{");
		jsonSb.append(JsonUtil.simpleToString("collected", this.collected));
		jsonSb.append("\",");
		jsonSb.append(JsonUtil.simpleToString("lastUpdated", this.lastUpdated));
		jsonSb.append("}");

		return jsonSb.toString();
	}
}
