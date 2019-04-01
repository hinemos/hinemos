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
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.util.JsonUtil;

/**
 * Rest-API返却用のノード関連情報.<br>
 * <br>
 * JSON形式で返却する想定.<br>
 * フィールド物理名がJSON項目名となる.
 * 
 * @since 6.2.0
 * @version 6.2.0
 */
public class RestNodeInfo {

	// JSON項目.
	/** ファシリティID */
	private String facilityId = null;

	/** IPアドレス一覧 */
	private List<String> ipAddress = null;

	// コンストラクタ.
	/**
	 * 変換元データ指定コンストラクタ.
	 * 
	 * @param node
	 *            DBから取得したノード情報.
	 */
	public RestNodeInfo(NodeInfo node) {
		this.facilityId = node.getFacilityId();

		if (node.getNodeNetworkInterfaceInfo() == null || node.getNodeNetworkInterfaceInfo().isEmpty()) {
			this.ipAddress = null;
			return;
		}

		// IPアドレスのセット.
		this.ipAddress = new LinkedList<String>();
		for (NodeNetworkInterfaceInfo nif : node.getNodeNetworkInterfaceInfo()) {
			this.ipAddress.add(nif.getNicIpAddress());
		}
	}

	// setter getter.
	/** ファシリティID */
	public String getFacilityId() {
		return facilityId;
	}

	/** ファシリティID */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/** IPアドレス一覧 */
	public List<String> getIpAddress() {
		return ipAddress;
	}

	/** IPアドレス一覧 */
	public void setIpAddress(List<String> ipAddress) {
		this.ipAddress = ipAddress;
	}

	// その他メソッド.
	/**
	 * JSON文字列に変換(ログ出力用).
	 */
	@Override
	public String toString() {
		StringBuilder jsonSb = new StringBuilder();
		jsonSb.append("{");
		jsonSb.append(JsonUtil.simpleToString("facilityId", this.facilityId));
		jsonSb.append(",");
		jsonSb.append(JsonUtil.listToString("ipAddress", this.ipAddress, true));
		jsonSb.append("}");

		return jsonSb.toString();
	}
}
