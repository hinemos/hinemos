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
 * RPA管理ツール停止方法マスタのEntity定義
 * 
 */
@Entity
@Table(name = "cc_rpa_management_tool_stop_mode_mst", schema = "setting")
public class RpaManagementToolStopModeMst implements Serializable {
	private static final long serialVersionUID = 1L;

	private RpaManagementToolStopModeMstPK id;
	/** 停止方法名 */
	private String stopModeName;

	@EmbeddedId
	public RpaManagementToolStopModeMstPK getId() {
		return id;
	}

	public void setId(RpaManagementToolStopModeMstPK id) {
		this.id = id;
	}

	@Column(name = "stop_mode_name")
	public String getStopModeName() {
		return stopModeName;
	}

	public void setStopModeName(String stopModeName) {
		this.stopModeName = stopModeName;
	}

}
