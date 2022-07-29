/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;


/**
 * RPAシナリオ係数パターンを格納するEntity定義
 */
@Entity
@Table(name="cc_rpa_scenario_coefficient_pattern", schema="setting")
public class RpaScenarioCoefficientPattern extends RpaAbstructPattern {
	private static final long serialVersionUID = 1L;

	/** シナリオ係数 */
	private Double coefficient;

	public RpaScenarioCoefficientPattern() {
	}

	/** シナリオ係数 */
	@Column(name="coefficient")
	public Double getCoefficient() {
		return coefficient;
	}

	public void setCoefficient(Double coefficient) {
		this.coefficient = coefficient;
	}
}