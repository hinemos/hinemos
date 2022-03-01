/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * 環境毎のRPAツールマスタを定義するEntity
 */
@Entity
@Table(name="cc_rpa_tool_env_mst", schema="setting")
public class RpaToolEnvMst implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/** 環境毎のRPAツールID */
	private String rpaToolEnvId;

	/** 環境毎のRPAツール名 */
	private String rpaToolEnvName;

	/** RPAツールマスタ */
	private RpaToolMst rpaToolMst;

	/**
	 * RPAツールのログを解析するクラス名<BR>
	 * {@link com.clustercontrol.rpa.scenario.factory.RpaLogParser}の実装クラスを指定
	 */
	private String rpaParserClass;

	/** RPAログ解析パターン */
	private List<RpaLogAnalyzePattern> logAnalyzePattern = new ArrayList<>();

	/** RPAシナリオ係数パターン */
	private List<RpaScenarioCoefficientPattern> scenarioCoefficientPattern = new ArrayList<>();

	public RpaToolEnvMst() {
	}

	/** 環境毎のRPAツールID */
	@Id
	@Column(name="rpa_tool_env_id")
	public String getRpaToolEnvId() {
		return this.rpaToolEnvId;
	}

	public void setRpaToolEnvId(String rpaToolEnvId) {
		this.rpaToolEnvId = rpaToolEnvId;
	}

	/** 環境毎のRPAツール名 */
	@Column(name="rpa_parser_class")
	public String getRpaParserClass() {
		return this.rpaParserClass;
	}

	public void setRpaParserClass(String rpaParserClass) {
		this.rpaParserClass = rpaParserClass;
	}

	@Column(name="rpa_tool_env_name")
	public String getRpaToolEnvName() {
		return this.rpaToolEnvName;
	}

	public void setRpaToolEnvName(String rpaToolEnvName) {
		this.rpaToolEnvName = rpaToolEnvName;
	}

	//bi-directional many-to-one association to RpaToolMst
	/** RPAツールマスタ */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="rpa_tool_id", insertable=false, updatable=false)
	public RpaToolMst getRpaToolMst() {
		return this.rpaToolMst;
	}

	@Deprecated
	public void setRpaToolMst(RpaToolMst rpaToolMst) {
		this.rpaToolMst = rpaToolMst;
	}

	/** RPAログ解析パターン */
	@OneToMany(mappedBy="rpaToolEnvMst", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    @OrderBy("id.orderNo ASC") // 取得時にordorNoで昇順ソート
	public List<RpaLogAnalyzePattern> getLogAnalyzePattern() {
		return logAnalyzePattern;
	}


	public void setLogAnalyzePattern(List<RpaLogAnalyzePattern> logAnalyzePattern) {
		this.logAnalyzePattern = logAnalyzePattern;
	}


	/** RPAシナリオ係数パターン */
	@OneToMany(mappedBy="rpaToolEnvMst", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@OrderBy("id.orderNo ASC") // 取得時にordorNoで昇順ソート
	public List<RpaScenarioCoefficientPattern> getScenarioCoefficientPattern() {
		return scenarioCoefficientPattern;
	}


	public void setScenarioCoefficientPattern(List<RpaScenarioCoefficientPattern> scenarioCoefficientPattern) {
		this.scenarioCoefficientPattern = scenarioCoefficientPattern;
	}
}
