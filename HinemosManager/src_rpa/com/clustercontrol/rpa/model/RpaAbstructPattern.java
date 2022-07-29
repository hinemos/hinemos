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
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

/**
 * RPAログパターン判定定義データEntity定義のMappedSuperClass
 *
 */
@MappedSuperclass
public class RpaAbstructPattern implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Embeddable
	public static class RpaAbstructPatternPK implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public RpaAbstructPatternPK() {
		}

		public RpaAbstructPatternPK(String rpaToolEnvId, Integer orderNo) {
			this.rpaToolEnvId = rpaToolEnvId;
			this.orderNo = orderNo;
		}

		/** 環境毎のRPAツールID */
		private String rpaToolEnvId;
		
		/** 順序 */
		private Integer orderNo;

		/** 環境毎のRPAツールID */
		@Column(name="rpa_tool_env_id")
		public String getRpaToolEnvId() {
			return rpaToolEnvId;
		}

		public void setRpaToolEnvId(String rpaToolEnvId) {
			this.rpaToolEnvId = rpaToolEnvId;
		}

		/** 環境毎のRPAツールID */
		@Column(name="order_no")
		public Integer getOrderNo() {
			return orderNo;
		}

		public void setOrderNo(Integer orderNo) {
			this.orderNo = orderNo;
		}
	}
	
	public RpaAbstructPattern() {
	}

	/** PK(環境毎のRPAツールID、順序) */
	private RpaAbstructPatternPK id;
	
	/** 大文字小文字を区別する */
	private Boolean caseSensitivityFlg;
	/** パターン */
	private String pattern;
	
	/** 環境毎のRPAツールマスタ */
	private RpaToolEnvMst rpaToolEnvMst;
	

	/** 大文字小文字を区別する */
	@Column(name="case_sensitivity_flg")
	public Boolean getCaseSensitivityFlg() {
		return this.caseSensitivityFlg;
	}

	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}

	/** パターン */
	@Column(name="pattern")
	public String getPattern() {
		return this.pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/** 環境毎のRPAツールマスタ */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="rpa_tool_env_id", insertable=false, updatable=false)
	public RpaToolEnvMst getRpaToolEnvMst() {
		return rpaToolEnvMst;
	}

	@Deprecated
	public void setRpaToolEnvMst(RpaToolEnvMst rpaToolEnvMst) {
		this.rpaToolEnvMst = rpaToolEnvMst;
	}

	/** 環境毎のRPAツールID */
	@Transient
	public String getRpaToolEnvId() {
		return this.getId().getRpaToolEnvId();
	}

	public void setRpaToolEnvId(String rpaToolEnvId) {
		this.getId().setRpaToolEnvId(rpaToolEnvId);
	}

	/** 順序 */
	@Transient
	public Integer getOrderNo() {
		return this.getId().getOrderNo();
	}

	public void setOrderNo(Integer orderNo) {
		this.getId().setOrderNo(orderNo);
	}

	@EmbeddedId
	public RpaAbstructPatternPK getId() {
		if (id == null) {
			id = new RpaAbstructPatternPK();
		}
		return id;
	}

	public void setId(RpaAbstructPatternPK id) {
		this.id = id;
	}
}
