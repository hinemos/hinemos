/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.jobutil.ui.views.commands;

/**
 * ジョブをエクスポートするダイアログを開くためのクライアント側アクションクラス<BR>
 * ジョブマップ エディタパースペクティブ ジョブ[ツリー]ビュー用
 * （同一IDの場合、ジョブマップ[登録]ビューのビューアクションボタンの活性、非活性も同時に制御されてしまうため
 * IDを分割するために作成)
 */
public class ExportJobCommandJobTree extends ExportJobCommand {

	/** アクションID */
	public static final String ID = ExportJobCommandJobTree.class.getName();
}
