/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.inquiry.bean;

/**
 * 
 * 対象情報のステータス。
 * 
 */
public enum TargetStatus {
	downloadable,
	creating,
	failed,
	empty;
}