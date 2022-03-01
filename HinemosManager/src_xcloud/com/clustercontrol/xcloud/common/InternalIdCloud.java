/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.xcloud.common;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.InternalIdAbstract;

/**
 * INTERNALイベントの情報を格納するクラス
 * 
 * クラウド管理オプションでは、「メッセージIDに対応したメッセージ＋α」のメッセージが設定されていることもある
 * 
 * ※各項目のコメント欄は最新でない可能性があります。参考程度にして、詳細はプロパティファイルを参照してください。
 * ※項目名は[プラグインID]_[監視設定ID(固定"SYS")]_[連番]にすること。
 *   連番は同プラグインID内の最大値にし、間の空いている数値は使用しないこと。
 * ※InternalIdAbstractを実装するクラス間でINTERNAL_IDは重複しない値を設定すること。
 * 
 */
public enum InternalIdCloud implements InternalIdAbstract {
	// 更新中に例外がスローされました。
	CLOUD_SYS_001(PriorityConstant.TYPE_WARNING, CloudConstants.PLUGIN_ID, CloudMessageConstant.AUTOUPDATE_ERROR),
	// hinemosAssignScopeIdタグでご指定のスコープが存在しないため、該当のノードを割り当てられませんでした。(ScopeId={1}, InstanceId={0})
	CLOUD_SYS_002(PriorityConstant.TYPE_WARNING, CloudConstants.PLUGIN_ID, CloudMessageConstant.VALIDATION_SCOPE_NOT_FOUND),
	// 自動検知を実行しました。
	CLOUD_SYS_003(PriorityConstant.TYPE_INFO, CloudConstants.PLUGIN_ID, CloudMessageConstant.EXECUTED_AUTO_SEARCH),
	// 自動検知に失敗しました。
	CLOUD_SYS_004(PriorityConstant.TYPE_WARNING, CloudConstants.PLUGIN_ID, CloudMessageConstant.EXECUTED_AUTO_SEARCH_FAILED),
	// 予期せぬエラーが発生しました, {0}
	CLOUD_SYS_099(PriorityConstant.TYPE_WARNING, CloudConstants.PLUGIN_ID, CloudMessageConstant.FAILURE_UNEXPECTED),
	;

	// 重要度
	private Integer priority;
	// プラグインID
	private String pluginId;
	// メッセージID
	private CloudMessageConstant messageConstant;

	/**
	 * コンストラクタ
	 * 
	 * @param priority 重要度
	 * @param pluginId プラグインID
	 * @param errorCode メッセージ定数
	 */
	private InternalIdCloud(Integer priority, String pluginId, CloudMessageConstant messageConstant) {
		this.priority = priority;
		this.pluginId = pluginId;
		this.messageConstant = messageConstant;
	}

	@Override
	public Integer getPriority() {
		return priority;
	}

	@Override
	public String getPluginId() {
		return pluginId;
	}

	@Override
	public String getMessage(String... args) {
		return messageConstant.getMessage(args);
	}

	@Override
	public String getInternalId() {
		return name();
	}
}
