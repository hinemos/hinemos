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
import com.clustercontrol.repository.model.NodeProcessInfo;
import com.clustercontrol.util.DateUtil;
import com.clustercontrol.util.JsonUtil;

/**
 * Rest-API返却用のノード構成情報のパッケージ情報クラス.<br>
 * <br>
 * JSON形式で返却する想定.<br>
 * 特に@JsonPropertyで指定がなければフィールド物理名がJSON項目名となる.<br>
 * 
 * @since 6.2.0
 * @version 6.2.0
 */
public class RestProcessList extends RestNodeConfigBase {

	// ログ出力用.
	/** ロガー */
	private static Log m_log = LogFactory.getLog(RestNodeConfigBase.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// JSON項目
	/** プロセス一覧 */
	private List<RestProcessInfo> process = null;

	// getter setter
	/** プロセス一覧 */
	public List<RestProcessInfo> getProcess() {
		return process;
	}

	/** プロセス一覧 */
	public void setProcess(List<RestProcessInfo> process) {
		this.process = process;
	}

	// コンストラクタ.
	/**
	 * 変換元データ指定コンストラクタ.
	 * 
	 * @param nodeList
	 *            DBから取得したノード情報.
	 */
	public RestProcessList(List<NodeProcessInfo> processList) {
		String constructor = this.getClass().getSimpleName();
		m_log.debug(constructor + DELIMITER + "start to construct.");

		if (processList == null || processList.isEmpty()) {
			// データ0件の場合、空のJSON返却.
			super.collected = null;
			super.lastUpdated = null;
			this.process = null;
			m_log.debug(constructor + DELIMITER + "constructed null into fields.");
			return;
		}

		// JSON変換用のbeanに詰め替え.
		NodeProcessInfo topProcess = processList.get(0);
		Long regDateMillis = topProcess.getRegDate();
		String dateFormat = "yyyyMMddHHmmss";
		try {
			super.collected = DateUtil.millisToString(regDateMillis, dateFormat);
		} catch (InvalidSetting e) {
			super.collected = null;
		}
		super.lastUpdated = super.collected;
		m_log.debug(constructor + DELIMITER + "constructed JSON into fields.");

		// プロセス情報の詰め替え.
		this.process = new LinkedList<RestProcessInfo>();
		for (NodeProcessInfo processInfo : processList) {
			RestProcessInfo restProcess = new RestProcessInfo(processInfo);
			this.process.add(restProcess);
		}
		m_log.debug(constructor + DELIMITER + "constructed process.");

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
		jsonSb.append(JsonUtil.listToString("process", this.process, false));
		jsonSb.append("}");

		return jsonSb.toString();
	}
}
