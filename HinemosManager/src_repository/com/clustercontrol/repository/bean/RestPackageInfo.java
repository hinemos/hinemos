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
import com.clustercontrol.repository.model.NodePackageInfo;
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
public class RestPackageInfo {

	// ログ出力用.
	/** ロガー */
	private static Log m_log = LogFactory.getLog(RestPackageInfo.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// JSON項目
	/** パッケージID */
	private String id = null;

	/** パッケージ名 */
	private String name = null;

	/** バージョン */
	private String version = null;

	/** リリース数 */
	private String release = null;

	/** ベンダー */
	private String vendor = null;

	/** インストール日時(YYYYMMDDhhmmss) */
	private String installed = null;

	/** 対応CPU(x86/x64) */
	private String arch = null;

	// コンストラクタ.
	/**
	 * 変換元データ指定コンストラクタ.
	 * 
	 * @param nodeList
	 *            DBから取得したノード情報.
	 */
	public RestPackageInfo(NodePackageInfo packageInfo) {
		String constructor = this.getClass().getSimpleName();
		m_log.debug(constructor + DELIMITER + "start to construct.");
		if (packageInfo == null) {
			// データ0件の場合、空のJSON返却.
			this.id = null;
			this.name = null;
			this.version = null;
			this.release = null;
			this.vendor = null;
			this.installed = null;
			this.arch = null;
			m_log.debug(constructor + DELIMITER + "constructed null into fields.");
			return;
		}

		// JSON変換用のbeanに詰め替え.
		this.id = packageInfo.getPackageId();
		this.name = packageInfo.getPackageName();
		this.version = packageInfo.getVersion();
		this.release = packageInfo.getRelease();
		this.vendor = packageInfo.getVendor();
		this.arch = packageInfo.getArchitecture();
		m_log.debug(constructor + DELIMITER + "constructed JSON into fields.");

		// 日付型の文字列変換.
		String dateFormat = "yyyyMMddHHmmss";
		try {
			if (packageInfo.getInstallDate() != null && packageInfo.getInstallDate() > 0) {
				this.installed = DateUtil.millisToString(packageInfo.getInstallDate(), dateFormat);
			}
		} catch (InvalidSetting e) {
			this.installed = null;
		}
		m_log.debug(constructor + DELIMITER + "constructed date.");

	}

	// getter setter
	/** パッケージID */
	public String getId() {
		return id;
	}

	/** パッケージID */
	public void setId(String id) {
		this.id = id;
	}

	/** パッケージ名 */
	public String getName() {
		return name;
	}

	/** パッケージ名 */
	public void setName(String name) {
		this.name = name;
	}

	/** バージョン */
	public String getVersion() {
		return version;
	}

	/** バージョン */
	public void setVersion(String version) {
		this.version = version;
	}

	/** リリース数 */
	public String getRelease() {
		return release;
	}

	/** リリース数 */
	public void setRelease(String release) {
		this.release = release;
	}

	/** ベンダー */
	public String getVendor() {
		return vendor;
	}

	/** ベンダー */
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	/** インストール日時(YYYYMMDDhhmmss) */
	public String getInstalled() {
		return installed;
	}

	/** インストール日時(YYYYMMDDhhmmss) */
	public void setInstalled(String installed) {
		this.installed = installed;
	}

	/** 対応CPU(x86/x64) */
	public String getArch() {
		return arch;
	}

	/** 対応CPU(x86/x64) */
	public void setArch(String arch) {
		this.arch = arch;
	}

	// その他メソッド.
	/**
	 * JSON文字列に変換(ログ出力用).
	 */
	@Override
	public String toString() {
		StringBuilder jsonSb = new StringBuilder();

		jsonSb.append("{");
		jsonSb.append(JsonUtil.simpleToString("id", this.id));
		jsonSb.append(",");
		jsonSb.append(JsonUtil.simpleToString("name", this.name));
		jsonSb.append(",");
		jsonSb.append(JsonUtil.simpleToString("version", this.version));
		jsonSb.append(",");
		jsonSb.append(JsonUtil.simpleToString("release", this.release));
		jsonSb.append(",");
		jsonSb.append(JsonUtil.simpleToString("vendor", this.vendor));
		jsonSb.append(",");
		jsonSb.append(JsonUtil.simpleToString("installed", this.installed));
		jsonSb.append(",");
		jsonSb.append(JsonUtil.simpleToString("arch", this.arch));
		jsonSb.append("}");

		return jsonSb.toString();
	}
}
