/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import java.util.ArrayList;

public class CollectorItemTreeItemResponse {

	
	/** 情報オブジェクト */
	private CollectorItemCodeMstDataResponse itemCodeData = null;

	/** 子の格納リスト */
	private ArrayList<CollectorItemTreeItemResponse> children = null;

	public CollectorItemTreeItemResponse(){
		super();
	}

	/**
	 * children を取得します。
	 * 
	 * @return children
	 */
	public ArrayList<CollectorItemTreeItemResponse> getChildren() {
		return children;
	}

	/**
	 * children を設定します。
	 * webサービス(jaxb)のためpublicにしておく。
	 * 
	 * @param children
	 */
	public void setChildren(ArrayList<CollectorItemTreeItemResponse> children) {
		this.children = children;
	}

	public CollectorItemCodeMstDataResponse getItemCodeData() {
		return itemCodeData;
	}

	public void setItemCodeData(CollectorItemCodeMstDataResponse itemCodeData) {
		this.itemCodeData = itemCodeData;
	}
}
