/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

/**
 * 戻り値の定義
 * 
 * - OK
 * - NG
 * - NONE（未設定）
 *   
 */
public enum ReturnValue {
	
	TRUE(0),
	FALSE(1),
	NONE(9);
	
	private final int value;

	private ReturnValue(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static ReturnValue valueOf(int value) {
		for (ReturnValue val : values()) {
			if (val.value == value) {
				return val;
			}
		}
		return null;
	}
}
