/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.entity;

/**
 * Data object for CollectorCalcMethodMst.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class CollectorCalcMethodMstData extends java.lang.Object implements java.io.Serializable {
	private static final long serialVersionUID = 6610514225717809547L;
	private java.lang.String calcMethod;
	private java.lang.String className;
	private java.lang.String expression;

	/* begin value object */

	/* end value object */

	public CollectorCalcMethodMstData() {
	}

	public CollectorCalcMethodMstData(java.lang.String calcMethod, java.lang.String className,
			java.lang.String expression) {
		setCalcMethod(calcMethod);
		setClassName(className);
		setExpression(expression);
	}

	public CollectorCalcMethodMstData(CollectorCalcMethodMstData otherData) {
		setCalcMethod(otherData.getCalcMethod());
		setClassName(otherData.getClassName());
		setExpression(otherData.getExpression());

	}

	public java.lang.String getPrimaryKey() {
		return getCalcMethod();
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

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("{");

		str.append("calcMethod=" + getCalcMethod() + " " + "className=" + getClassName() + " " + "expression="
				+ getExpression());
		str.append('}');

		return (str.toString());
	}

	@Override
	public boolean equals(Object pOther) {
		if (pOther instanceof CollectorCalcMethodMstData) {
			CollectorCalcMethodMstData lTest = (CollectorCalcMethodMstData) pOther;
			boolean lEquals = true;

			if (this.calcMethod == null) {
				lEquals = lEquals && (lTest.calcMethod == null);
			} else {
				lEquals = lEquals && this.calcMethod.equals(lTest.calcMethod);
			}
			if (this.className == null) {
				lEquals = lEquals && (lTest.className == null);
			} else {
				lEquals = lEquals && this.className.equals(lTest.className);
			}
			if (this.expression == null) {
				lEquals = lEquals && (lTest.expression == null);
			} else {
				lEquals = lEquals && this.expression.equals(lTest.expression);
			}

			return lEquals;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + ((this.calcMethod != null) ? this.calcMethod.hashCode() : 0);

		result = 37 * result + ((this.className != null) ? this.className.hashCode() : 0);

		result = 37 * result + ((this.expression != null) ? this.expression.hashCode() : 0);

		return result;
	}

}
