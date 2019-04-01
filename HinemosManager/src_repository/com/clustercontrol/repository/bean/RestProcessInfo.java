/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.repository.bean;

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
public class RestProcessInfo {

	// ログ出力用.
	/** ロガー */
	private static Log m_log = LogFactory.getLog(RestProcessInfo.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// JSON項目
	/** プロセス名 */
	private String name = null;

	/** 実行ファイルパス(コマンド) */
	private String path = null;

	/** 実行ユーザー */
	private String user = null;

	/** プロセスID */
	private String pid = null;

	/** 起動日時(YYYYMMDDhhmmss) */
	private String startup = null;

	// コンストラクタ.
	/**
	 * 変換元データ指定コンストラクタ.
	 * 
	 * @param nodeList
	 *            DBから取得したノード情報.
	 */
	public RestProcessInfo(NodeProcessInfo processInfo) {
		String constructor = this.getClass().getSimpleName();
		m_log.debug(constructor + DELIMITER + "start to construct.");

		if (processInfo == null) {
			// データ0件の場合、空のJSON返却.
			this.name = null;
			this.path = null;
			this.user = null;
			this.pid = null;
			this.startup = null;
			m_log.debug(constructor + DELIMITER + "constructed null into fields.");
			return;
		}

		// JSON変換用のbeanに詰め替え.
		this.name = processInfo.getProcessName();
		this.path = processInfo.getPath();
		this.user = processInfo.getExecUser();
		this.pid = processInfo.getPid().toString();
		m_log.debug(constructor + DELIMITER + "constructed JSON into fields.");

		// 日付型の文字列変換.
		String dateFormat = "yyyyMMddHHmmss";
		try {
			if (processInfo.getStartupDateTime() != null && processInfo.getStartupDateTime() > 0) {
				this.startup = DateUtil.millisToString(processInfo.getStartupDateTime(), dateFormat);
			}
		} catch (InvalidSetting e) {
			this.startup = null;
		}
		m_log.debug(constructor + DELIMITER + "constructed date.");
	}

	// getter setter
	/** プロセス名 */
	public String getName() {
		return name;
	}

	/** プロセス名 */
	public void setName(String name) {
		this.name = name;
	}

	/** 実行ファイルパス(コマンド) */
	public String getPath() {
		return path;
	}

	/** 実行ファイルパス(コマンド) */
	public void setPath(String path) {
		this.path = path;
	}

	/** 実行ユーザー */
	public String getUser() {
		return user;
	}

	/** 実行ユーザー */
	public void setUser(String user) {
		this.user = user;
	}

	/** プロセスID */
	public String getPid() {
		return pid;
	}

	/** プロセスID */
	public void setPid(String pid) {
		this.pid = pid;
	}

	/** 起動日時(YYYYMMDDhhmmss) */
	public String getStartup() {
		return startup;
	}

	/** 起動日時(YYYYMMDDhhmmss) */
	public void setStartup(String startup) {
		this.startup = startup;
	}

	// その他メソッド.
	/**
	 * JSON文字列に変換(ログ出力用).
	 */
	@Override
	public String toString() {
		StringBuilder jsonSb = new StringBuilder();

		jsonSb.append("{");
		jsonSb.append(JsonUtil.simpleToString("name", this.name));
		jsonSb.append(",");
		jsonSb.append(JsonUtil.simpleToString("path", this.path));
		jsonSb.append(",");
		jsonSb.append(JsonUtil.simpleToString("user", this.user));
		jsonSb.append(",");
		jsonSb.append(JsonUtil.simpleToString("pid", this.pid));
		jsonSb.append(",");
		jsonSb.append(JsonUtil.simpleToString("startup", this.startup));
		jsonSb.append("}");

		return jsonSb.toString();
	}
}
