/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

public class CollectorCalcMethodMstDataResponse {
	private String calcMethod;
	private String className;
	private String expression;

	public CollectorCalcMethodMstDataResponse() {
	}

	public java.lang.String getCalcMethod() {
		return this.calcMethod;
	}

	public void setCalcMethod(java.lang.String calcMethod) {
		this.calcMethod = calcMethod;
	}

	public java.lang.String getClassName() {
		return this.className;
	}

	public void setClassName(java.lang.String className) {
		this.className = className;
	}

	public java.lang.String getExpression() {
		return this.expression;
	}

	public void setExpression(java.lang.String expression) {
		this.expression = expression;
	}

}
