/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.bean;

/**
 * 数値監視種別の定義を定数として格納するクラス<BR>
 * 
 * @version 6.1.0
 */
public enum MonitorNumericType {
	/** 通常 */
	TYPE_BASIC(""),
	/** 将来予測監視 */
	TYPE_PREDICTION("PREDICTION"),
	/** 変化点監視 */
	TYPE_CHANGE("CHANGE");

	/** タイプ */
	private final String type;

	private MonitorNumericType(String type) {
		this.type = type;
	}

	/**
	 * タイプを返す
	 * @return タイプ
	 */
	public String getType () {
		return this.type;
	}
}