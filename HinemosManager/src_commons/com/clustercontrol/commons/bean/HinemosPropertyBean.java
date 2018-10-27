/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.bean;

import java.io.Serializable;

import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;

/**
 * Hinemosプロパティの情報を格納するクラス<br/>
 */
public class HinemosPropertyBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer type = null;					// データ型
	private String defaultStringValue = null;		// デフォルト値（文字列）
	private Long defaultNumericValue = null;		// デフォルト値（数値）
	private Boolean defaultBooleanValue = null;		// デフォルト値（真偽値）

	/**
	 * コンストラクタ
	 * 
	 * @param type 					データ型
	 * @param defaultStringValue	デフォルト値（文字列）
	 * @param defaultNumericValue	デフォルト値（数値）
	 * @param defaultBooleanValue	デフォルト値（真偽値）
	 */
	private HinemosPropertyBean(int type, String defaultStringValue, 
			Long defaultNumericValue, Boolean defaultBooleanValue) {
		this.type = type;
		this.defaultStringValue = defaultStringValue;
		this.defaultNumericValue = defaultNumericValue;
		this.defaultBooleanValue = defaultBooleanValue;
	}
	/**
	 * データ型を返す
	 * @return type データ型
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * デフォルト値（文字列）を返す
	 * @return stringValue デフォルト値（文字列）
	 */
	public String getDefaultStringValue() {
		return this.defaultStringValue;
	}

	/**
	 * デフォルト値（数値）を返す
	 * @return defaultNumericValue デフォルト値（数値）
	 */
	public Long getDefaultNumericValue() {
		return this.defaultNumericValue;
	}

	/**
	 * デフォルト値（真偽値）を返す
	 * @return booleanValue デフォルト値（真偽値）
	 */
	public Boolean getDefaultBooleanValue() {
		return this.defaultBooleanValue;
	}

	/**
	 * 文字列のBeanを返す
	 * 
	 * @param defaultValue デフォルト値
	 * @return 文字列のBean
	 */
	public static HinemosPropertyBean string(String defaultValue) {
		return new HinemosPropertyBean(HinemosPropertyTypeConstant.TYPE_STRING, defaultValue, null, null);
	}

	/**
	 * 文字列のBeanを返す
	 * 
	 * @return 文字列のBean
	 */
	public static HinemosPropertyBean string() {
		return new HinemosPropertyBean(HinemosPropertyTypeConstant.TYPE_STRING, null, null, null);
	}

	/**
	 * 数値のBeanを返す
	 * 
	 * @param defaultValue デフォルト値
	 * @return 数値のBean
	 */
	public static HinemosPropertyBean numeric(Long defaultValue) {
		return new HinemosPropertyBean(HinemosPropertyTypeConstant.TYPE_NUMERIC, null, defaultValue, null);
	}

	/**
	 * 数値のBeanを返す
	 * 
	 * @return 数値のBean
	 */
	public static HinemosPropertyBean numeric() {
		return new HinemosPropertyBean(HinemosPropertyTypeConstant.TYPE_NUMERIC, null, null, null);
	}

	/**
	 * 真偽値のBeanを返す
	 * 
	 * @param defaultValue デフォルト値
	 * @return 真偽値のBean
	 */
	public static HinemosPropertyBean bool(Boolean defaultValue) {
		return new HinemosPropertyBean(HinemosPropertyTypeConstant.TYPE_TRUTH, null, null, defaultValue);
	}

	/**
	 * 真偽値のBeanを返す
	 * 
	 * @return 真偽値のBean
	 */
	public static HinemosPropertyBean bool() {
		return new HinemosPropertyBean(HinemosPropertyTypeConstant.TYPE_TRUTH, null, null, null);
	}
}
