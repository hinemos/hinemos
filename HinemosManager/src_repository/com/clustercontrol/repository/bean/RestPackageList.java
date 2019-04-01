/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.repository.bean;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.repository.model.NodePackageInfo;
import com.clustercontrol.util.DateUtil;
import com.clustercontrol.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Rest-API返却用のノード構成情報のパッケージ情報クラス.<br>
 * <br>
 * JSON形式で返却する想定.<br>
 * 特に@JsonPropertyで指定がなければフィールド物理名がJSON項目名となる.<br>
 * 
 * @since 6.2.0
 * @version 6.2.0
 */
public class RestPackageList extends RestNodeConfigBase {

	// ログ出力用.
	/** ロガー */
	private static Log m_log = LogFactory.getLog(RestPackageList.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// JSON項目
	/** パッケージ一覧 */
	@JsonProperty("package")
	private List<RestPackageInfo> packageList = null;

	// getter setter
	/** パッケージ一覧 */
	public List<RestPackageInfo> getPackageList() {
		return packageList;
	}

	/** パッケージ一覧 */
	public void setPackageList(List<RestPackageInfo> packageList) {
		this.packageList = packageList;
	}

	// コンストラクタ.
	/**
	 * 変換元データ指定コンストラクタ.
	 * 
	 * @param nodeList
	 *            DBから取得したノード情報.
	 */
	public RestPackageList(LatestNodeConfigWrapper<NodePackageInfo> latestPackage) {
		String constructor = this.getClass().getSimpleName();
		m_log.debug(constructor + DELIMITER + "start to construct.");
		if (latestPackage == null) {
			// データ0件の場合、空のJSON返却.
			super.collected = null;
			super.lastUpdated = null;
			this.packageList = null;
			m_log.debug(constructor + DELIMITER + "constructed null into fields.");
			return;
		}

		// JSON変換用のbeanに詰め替え.
		String dateFormat = "yyyyMMddHHmmss";
		try {
			super.collected = DateUtil.millisToString(latestPackage.getCollected(), dateFormat);
		} catch (InvalidSetting e) {
			super.collected = null;
		}
		try {
			super.lastUpdated = DateUtil.millisToString(latestPackage.getLastUpdated(), dateFormat);
		} catch (InvalidSetting e) {
			super.lastUpdated = null;
		}
		m_log.debug(constructor + DELIMITER + "constructed date.");

		// パッケージ情報の詰め替え.
		if (latestPackage.getConfigList() == null || latestPackage.getConfigList().isEmpty()) {
			this.packageList = null;
			m_log.debug(constructor + DELIMITER + "constructed null into packageList.");

		}
		this.packageList = new LinkedList<RestPackageInfo>();
		for (NodePackageInfo packageInfo : latestPackage.getConfigList()) {
			RestPackageInfo restPackage = new RestPackageInfo(packageInfo);
			this.packageList.add(restPackage);
		}
		m_log.debug(constructor + DELIMITER + "constructed packageList.");

	}

	// その他メソッド.
	/**
	 * JSON文字列に変換(ログ出力用).
	 */
	@Override
	public String toString() {
		StringBuilder jsonSb = new StringBuilder();
		jsonSb.append("{");
		jsonSb.append(JsonUtil.simpleToString("collected", super.collected));
		jsonSb.append(",");
		jsonSb.append(JsonUtil.simpleToString("lastUpdated", super.lastUpdated));
		jsonSb.append(",");
		jsonSb.append(JsonUtil.listToString("package", this.packageList, false));
		jsonSb.append("}");

		return jsonSb.toString();
	}
}
