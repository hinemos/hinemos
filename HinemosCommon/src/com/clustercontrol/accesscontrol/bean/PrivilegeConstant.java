/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.bean;

import com.clustercontrol.util.Messages;

/**
 * オブジェクト権限、システム権限の定数を格納するクラス<BR>
 * 
 */
public class PrivilegeConstant {

	// システム権限
	public static enum SystemPrivilegeMode {
		ADD("system_privilege.privilege.add"), 
		READ("system_privilege.privilege.read"), 
		MODIFY("system_privilege.privilege.modify"), 
		EXEC("system_privilege.privilege.exec"), 
		APPROVAL("system_privilege.privilege.approval");

		private final String labelString;

		private SystemPrivilegeMode(String labelString) {
			this.labelString = labelString;
		}

		public String labelString() {
			return Messages.getString(labelString);
		}
	}

	// オブジェクト権限（NONE=オブジェクト権限チェックしない)
	public static enum ObjectPrivilegeMode {
		READ, MODIFY, EXEC, NONE
	}

	public enum SystemPrivilegeFunction {
		/** Hinemosエージェント(内部用) */
		HinemosAgent("system_privilege.function.agent"),
		/** Hinemos HA(内部用) */
		HinemosHA("system_privilege.function.ha"),
		/** Hinemos CLI(内部用) */
		HinemosCLI("system_privilege.function.cli"),
		/** リポジトリ */
		Repository("system_privilege.function.repository"),
		/** ユーザ管理 */
		AccessControl("system_privilege.function.accesscontrol"),
		/** ジョブ管理 */
		JobManagement("system_privilege.function.jobmanagement"),
		/** 収集管理 */
		Collect("system_privilege.function.collect"),
		/** 監視結果 */
		MonitorResult("system_privilege.function.monitorresult"),
		/** 監視設定 */
		MonitorSetting("system_privilege.function.monitorsetting"),
		/** カレンダ */
		Calendar("system_privilege.function.calendar"),
		/** 通知 */
		Notify("system_privilege.function.notify"),
		/** 環境構築機能 */
		Infra("system_privilege.function.infra"),
		/** メンテナンス(履歴情報削除, 共通設定) */
		Maintenance("system_privilege.function.maintenance"),
		/** クラウド・仮想化管理 */
		CloudManagement("system_privilege.function.cloudmanagement"),
		/** レポーティング */
		Reporting("system_privilege.function.reporting"),
		/** フィルタ設定 */
		FilterSetting("system_privilege.function.filtersetting"),
		/** 収集蓄積 */
		Hub("system_privilege.function.hub"),
		/** SDML設定 */
		SdmlSetting("system_privilege.function.sdmlsetting"),
		/** PRA */
		Rpa("system_privilege.function.rpa");

		private final String labelString;

		private SystemPrivilegeFunction(String labelString) {
			this.labelString = labelString;
		}

		public String labelString() {
			return Messages.getString(labelString);
		}
	}

	// オブジェクト権限で登録されるもの
	public static ObjectPrivilegeMode[] objectPrivilegeModes = {
		ObjectPrivilegeMode.READ,
		ObjectPrivilegeMode.MODIFY,
		ObjectPrivilegeMode.EXEC,
	};

	// システム権限設定種別
	public static final String SYSTEMPRIVILEGE_EDITTYPE_NONE = "0";
	public static final String SYSTEMPRIVILEGE_EDITTYPE_DIALOG = "1";
}