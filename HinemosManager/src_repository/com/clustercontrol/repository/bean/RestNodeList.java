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

import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.util.JsonUtil;

/**
 * Rest-API返却用のノード一覧.<br>
 * <br>
 * JSON形式で返却する想定.<br>
 * フィールド物理名がJSON項目名となる.
 * 
 * @since 6.2.0
 * @version 6.2.0
 */
public class RestNodeList {

	// JSON項目
	/** ノード一覧 */
	private List<RestNodeInfo> node = null;

	// コンストラクタ.
	/**
	 * 変換元データ指定コンストラクタ.
	 * 
	 * @param nodeList
	 *            DBから取得したノード情報.
	 */
	public RestNodeList(List<NodeInfo> nodeList) {
		if (nodeList == null || nodeList.isEmpty()) {
			// データ0件の場合、空のJSON返却
			this.node = null;
			return;
		}

		// JSON変換用のbeanに詰め替え.
		this.node = new LinkedList<RestNodeInfo>();
		for (NodeInfo node : nodeList) {
			RestNodeInfo restNode = new RestNodeInfo(node);
			this.node.add(restNode);
		}
	}

	// setter getter
	/** ノード一覧 */
	public List<RestNodeInfo> getNode() {
		return node;
	}

	/** ノード一覧 */
	public void setNode(List<RestNodeInfo> node) {
		this.node = node;
	}

	// その他メソッド.
	/**
	 * JSON文字列に変換(ログ出力用).
	 */
	@Override
	public String toString() {
		StringBuilder jsonSb = new StringBuilder();
		jsonSb.append("{");
		jsonSb.append(JsonUtil.listToString("node", this.node, false));
		jsonSb.append("}");

		return jsonSb.toString();
	}

}
