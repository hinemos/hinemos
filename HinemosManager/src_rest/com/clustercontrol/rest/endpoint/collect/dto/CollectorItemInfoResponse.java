/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

public class CollectorItemInfoResponse {
	
	private String itemCode;//収集項目コード
	private String displayName;//リポジトリ表示名 (Ver3.1.0)より追加

	public CollectorItemInfoResponse(){
	}

	/**
	 * 収集項目コードを取得します。
	 * @return 収集項目コード
	 */
	public String getItemCode() {
		return itemCode;
	}
	/**
	 * 収集項目コードを設定します。
	 * @param itemCode 収集項目コード
	 */
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	/**
	 * リポジトリ表示名を取得します。
	 * @return リポジトリ表示名
	 */
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * リポジトリ表示名を設定します。
	 * @param name リポジトリ表示名
	 */
	public void setDisplayName(String name) {
		displayName = name;
	}

}
