/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 転送先種別を格納し、また関連するプロパティも保持する。
 *
 */
public class TransferInfoDestTypeMst {

	private String destTypeId;
	private String name;
	private String description;
	
	private List<TransferDestTypePropMst> destTypePropMsts = new ArrayList<>();
	
	public TransferInfoDestTypeMst(){
	}

	public String getDestTypeId() {
		return destTypeId;
	}
	public void setDestTypeId(String destTypeId) {
		this.destTypeId = destTypeId;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<TransferDestTypePropMst> getDestTypePropMsts() {
		return destTypePropMsts;
	}
	public void setDestTypePropMsts(List<TransferDestTypePropMst> destTypePropMsts) {
		this.destTypePropMsts = destTypePropMsts;
	}
}