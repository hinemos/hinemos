/*

Copyright (C) 2008 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */

package com.clustercontrol.performance.operator;

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.poller.util.DataTable;


/**
 * 	逆ポーランド記法による演算クラス
 */
public class RevercePorlishNotation extends Operator {
	private static Log log = LogFactory.getLog( RevercePorlishNotation.class );

	private String expression;		// 計算式の文字列（逆ポーランド記法で格納）
	private Object[] expArray;		// 計算式の配列（逆ポーランド記法で格納）

	public enum OPERATOR { ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION };

	private final String regexSplitter = "[ \t]+";
	private final String regexDeltaFunction = "^delta\\((.+?)\\)$";
	private final Pattern regexDeltaFunctionPattern = Pattern.compile(regexDeltaFunction, Pattern.CASE_INSENSITIVE);

	@Override
	public void setExpression(String expression) {
		if (log.isDebugEnabled()) log.debug("expression : " + expression);

		// 計算式がnullまたは空文字列の場合はIllegalArgumentExceptionとする
		if (expression == null || "".equals(expression)) {
			throw new IllegalArgumentException("setExpression() illegal argument : " + expression);
		}

		String[] vars = expression.split(regexSplitter);
		this.expArray = new Object[vars.length];

		for(int i = 0 ; i < vars.length ; i++) {
			if ("+".equals(vars[i])) {
				this.expArray[i] = OPERATOR.ADDITION;
			} else if ("-".equals(vars[i])) {
				this.expArray[i] = OPERATOR.SUBTRACTION;
			} else if ("*".equals(vars[i])) {
				this.expArray[i] = OPERATOR.MULTIPLICATION;
			} else if ("/".equals(vars[i])) {
				this.expArray[i] = OPERATOR.DIVISION;
			} else {
				try  {
					// 定数指定
					this.expArray[i] = Double.parseDouble(vars[i]);
				} catch (NumberFormatException e) {
					// 変数指定
					this.expArray[i] = vars[i];
				}
			}
		}
		this.expression = expression;
		log.debug("RevercePorlishNotation#setExpression() expression = " + this.expression);
		if (log.isDebugEnabled()) {
			for (int i = 0 ; i < this.expArray.length ; i++) {
				log.debug("RevercePorlishNotation#setExpression() expArray[" + i + "] = " + this.expArray[i]);
			}
		}
	}

	/**
	 * 定数・変数判定
	 */
	private double getVal(Object obj, DataTable currentTable, DataTable previousTable, String deviceName) throws CollectedDataNotFoundException, InvalidValueException {
		double result = 0D;

		if (obj instanceof Double) {
			// 数字の場合
			result = (Double)obj;
		} else if (obj instanceof String) {
			// 文字列の場合
			String str = (String)obj;
			Matcher matcher = regexDeltaFunctionPattern.matcher(str);
			if (matcher.find()) {
				// delta(key)の場合
				result = getDifferenceValue(matcher.group(1), currentTable, previousTable, deviceName);
			} else {
				// keyの場合
				result = getCurrentMibValue(str, currentTable, deviceName);
			}
		}
		return result;
	}

	/**
	 * 計算処理
	 */
	@Override
	public double calc(DataTable currentTable, DataTable previousTable, String deviceName) throws CollectedDataNotFoundException, InvalidValueException{
		double right = 0D;
		double left = 0D;
		double result = 0D;
		Stack<Double> _stack = new Stack<Double>();

		if (this.expArray.length == 1) {
			result = getVal(expArray[0], currentTable, previousTable, deviceName);
		} else {
			for (int i = 0 ; i < this.expArray.length; i++) {

				try {
					if (expArray[i] instanceof OPERATOR) {
						right = _stack.pop();
						left = _stack.pop();
						switch ((OPERATOR)expArray[i]) {
						case ADDITION :
							result = left + right;
							break;
						case SUBTRACTION :
							result =  left - right;
							break;
						case MULTIPLICATION :
							result =  left * right;
							break;
						case DIVISION :
							if (right == 0) {
								log.warn("0-devided, expression=" + expression);
								// 0-devideの場合は計算不能としてNaNを返す
								return Double.NaN;
							}
							result =  left / right;
							break;
						}
						_stack.push(new Double(result));
					} else {
						_stack.push(getVal(expArray[i], currentTable, previousTable, deviceName));
					}
				} catch (CollectedDataNotFoundWithNoPollingException e) {
					throw e;
				} catch (CollectedDataNotFoundException | IllegalStateException | EmptyStackException e) {
					log.warn("calc [" + expression + "], " + e.getClass().getName() + ", " + e.getMessage());
					throw new InvalidValueException(e.getMessage());
				} catch (Exception e) {
					log.warn("calc [" + expression + "], " + e.getClass().getName() + ", " + e.getMessage(), e);
					throw new InvalidValueException(e.getMessage());
				}
			}
			if (_stack.size() > 1) {
				String messages = "expression is invalid, expression-" + expression;
				log.warn("calc : " + messages);
				throw new InvalidValueException(messages);
			}
		}
		return result;
	}

	public static void main(String[] args) {
		try {
			// result : 17.333333333333333
			//String expr = "5.0 4.0 3.0 * 2.0 6.0 / + +";

			// result : 1.2272727272727273
			String expr = "1.0 2.0 + 3.0 * 4.5 + 11.0 /";

			// result : 0.24
			//String expr = "0.24";

			RevercePorlishNotation rev = new RevercePorlishNotation();
			rev.setExpression(expr);
			System.out.println("result : " + rev.calc(null, null, null));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
