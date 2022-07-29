/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

 package com.clustercontrol.rpa.model;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


/**
 * RPAツールマスタを定義するEntity
 * 
 */
@Entity
@Table(name="cc_rpa_tool_mst", schema="setting")
public class RpaToolMst implements Serializable {
	private static final long serialVersionUID = 1L;
	/** RPAツールID */
	private String rpaToolId;	
	/** RPAツール名 */
	private String rpaToolName;
	/** デフォルトシナリオ係数 */
	private Double defaultCoefficient;
	/** RPAツールパッケージ名 */
	private String rpaToolPackageName;
	/** RPAノードを割り当てるスコープID(管理製品なし) */
	private String defaultScopeId;
	
	/** 環境毎のRPAツールマスタ */
	private List<RpaToolEnvMst> rpaToolEnvMstList;

	public RpaToolMst() {
	}


	/** RPAツールID */
	@Id
	@Column(name="rpa_tool_id")
	public String getRpaToolId() {
		return this.rpaToolId;
	}

	public void setRpaToolId(String rpaToolId) {
		this.rpaToolId = rpaToolId;
	}

	/** RPAツール名 */
	@Column(name="rpa_tool_name")
	public String getRpaToolName() {
		return this.rpaToolName;
	}

	public void setRpaToolName(String rpaToolName) {
		this.rpaToolName = rpaToolName;
	}

	/** デフォルトシナリオ係数 */
	@Column(name="default_coefficient")
	public Double getDefaultCoefficient() {
		return defaultCoefficient;
	}


	public void setDefaultCoefficient(Double defaultCoefficient) {
		this.defaultCoefficient = defaultCoefficient;
	}

	/** RPAツールパッケージ名 */
	@Column(name="rpa_tool_package_name")
	public String getRpaToolPackageName() {
		return rpaToolPackageName;
	}
	
	
	public void setRpaToolPackageName(String rpaToolPackageName) {
		this.rpaToolPackageName = rpaToolPackageName;
	}
	
	/** RPAノードを割り当てるスコープID(管理製品なし) */
	@Column(name="default_scope_id")
	public String getDefaultScopeId() {
		return defaultScopeId;
	}
	
	public void setDefaultScopeId(String defaultScopeId) {
		this.defaultScopeId = defaultScopeId;
	}

	//bi-directional many-to-one association to RpaToolEnvMst
	/** 環境毎のRPAツールマスタ */
	@OneToMany(mappedBy="rpaToolMst", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<RpaToolEnvMst> getRpaToolEnvMstList() {
		return rpaToolEnvMstList;
	}	
	public void setRpaToolEnvMstList(List<RpaToolEnvMst> rpaToolEnvMstList) {
		this.rpaToolEnvMstList = rpaToolEnvMstList;
	}

}