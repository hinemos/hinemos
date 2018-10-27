/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;



/**
 * 属性毎比較結果。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class ResultD {
	public enum ResultType {
		diff,
		equal,
		warning
	}

	public enum ValueType {
		simple,
		array
	}

	/**
	 * カラム ID。 ソートするのに使用。
	 */
	private String[] columnId;

	/**
	 * 差分結果種別。
	 */
	private ResultType resultType;
	
	/**
	 * 属性の種別。
	 */
	private ValueType valueType;

	/**
	 * ネームスペース。
	 */
	private String[] nameSpaces = new String[0];

	/**
	 * 属性名。
	 */
	private String propName;

	/**
	 * 対象 1 の属性値。
	 */
	private String[] value1;
	
	/**
	 * 対象 2 の属性値。
	 */
	private String[] value2;

	public void setPropName(String propName) {
		this.propName = propName;
	}

	public String getPropName() {
		return propName;
	}

	public void setValue1(String[] value1) {
		this.value1 = value1;
	}

	public String[] getValue1() {
		return value1;
	}

	public void setValue2(String[] value2) {
		this.value2 = value2;
	}

	public String[] getValue2() {
		return value2;
	}

	public void setResultType(ResultType resultType) {
		this.resultType = resultType;
	}

	public ResultType getResultType() {
		return resultType;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

	public ValueType getValueType() {
		return valueType;
	}

	public void setNameSpaces(String[] nameSpaces) {
		this.nameSpaces = nameSpaces;
	}

	public String[] getNameSpaces() {
		return nameSpaces;
	}

	public void setColumnId(String[] columnId) {
		this.columnId = columnId;
	}

	public String[] getColumnId() {
		return columnId;
	}
}
