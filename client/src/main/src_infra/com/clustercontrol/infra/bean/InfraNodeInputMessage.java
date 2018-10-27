/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.bean;

import com.clustercontrol.util.Messages;

/**
 * 環境構築のログイン情報設定のTooltip用メッセージを定数として格納するクラス<BR>
 *
 * @version 6.1.0
 */
public class InfraNodeInputMessage {
	/** ノードプロパティの認証情報を利用する */
	public static final String STRING_NODE_PARAM = Messages.getString("infra.node.input.node.param");
	/** 環境構築変数を認証情報として利用する */
	public static final String STRING_INFRA_PARAM = Messages.getString("infra.node.input.infra.param");
	/** ログイン情報を入力する */
	public static final String STRING_DIALOG = Messages.getString("infra.node.input.dialog");

	/**
	 * 種別からTooltip用メッセージに変換します。<BR>
	 * 
	 * @param type
	 * @return Tooltip用メッセージ
	 */
	public static String typeToString(int type) {
		if (type == InfraNodeInputConstant.TYPE_NODE_PARAM) {
			return STRING_NODE_PARAM;
		} else if (type == InfraNodeInputConstant.TYPE_INFRA_PARAM) {
			return STRING_INFRA_PARAM;
		} else if (type == InfraNodeInputConstant.TYPE_DIALOG) {
			return STRING_DIALOG;
		} else {
			return "";
		}
	}
}
