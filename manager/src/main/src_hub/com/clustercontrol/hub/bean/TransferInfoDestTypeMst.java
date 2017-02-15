/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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