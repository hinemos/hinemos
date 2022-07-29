/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * RPA管理ツール実行種別マスタのEntity定義
 * 
 */
@Entity
@Table(name = "cc_rpa_management_tool_run_type_mst", schema = "setting")
public class RpaManagementToolRunTypeMst implements Serializable {
	private static final long serialVersionUID = 1L;

	private RpaManagementToolRunTypeMstPK id;
	/** 実行種別名 */
	private String runTypeName;

	@EmbeddedId
	public RpaManagementToolRunTypeMstPK getId() {
		return id;
	}

	public void setId(RpaManagementToolRunTypeMstPK id) {
		this.id = id;
	}

	@Column(name = "run_type_name")
	public String getRunTypeName() {
		return runTypeName;
	}

	public void setRunTypeName(String runTypeName) {
		this.runTypeName = runTypeName;
	}

}
