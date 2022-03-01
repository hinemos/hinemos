/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

 package com.clustercontrol.rpa.model;

import com.clustercontrol.rpa.util.LogTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;


/**
 * RPAログ解析パターンを格納するEntity定義
 */
@Entity
@Table(name="cc_rpa_log_analyze_pattern", schema="setting")
public class RpaLogAnalyzePattern extends RpaAbstructPattern {
	private static final long serialVersionUID = 1L;

	/** ログ種別(開始、終了、エラー) */
	private LogTypeEnum logType;

	public RpaLogAnalyzePattern() {
	}

	/** ログ種別(開始、終了、エラー) */
	@Enumerated(EnumType.STRING)
	@Column(name="log_type")
	public LogTypeEnum getLogType() {
		return logType;
	}

	public void setLogType(LogTypeEnum logType) {
		this.logType = logType;
	}
}