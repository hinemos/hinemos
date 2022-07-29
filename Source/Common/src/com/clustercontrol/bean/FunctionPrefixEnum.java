/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.bean;

/**
 * cc_notify_relation_infoのfunction_prefixに登録する値の定義。
 * 
 * @version 7.0.0
 * @since 7.0.0
 */
public enum FunctionPrefixEnum {
	MONITOR,
	PREDICTION,
	CHANGE,
	INFRA,
	NODE_CONFIG_SETTING,
	MAINTENANCE,
	REPORTING,
	RPA_SCENARIO_CREATE,
	RPA_SCENARIO_CORRECT,
	JOB_MASTER,
	JOB_SESSION,
	SDML_CONTROL;

	private FunctionPrefixEnum() {
	}

}
