/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.grafana.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

public class QueryBuildHelper {
	private static Logger logger = Logger.getLogger(StatusQueryBuilder.class);

	private static String regex = "^(NOT:)(.+)$";
	private static Pattern pattern = Pattern.compile(regex);

	public static String buildCondition(String property, String operator, Integer value) {
		StringBuilder sb = new StringBuilder();

		sb.append(property);
		sb.append(" ");
		sb.append(operator);
		sb.append(" ");
		sb.append(value);

		return sb.toString();
	}

	/**
	 * 日時の条件を構築します
	 * 
	 * @param property
	 * @param operator
	 * @param value
	 * @return
	 */
	public static String buildDateCondition(String property, String operator, Long value) {
		StringBuilder sb = new StringBuilder();

		sb.append(property);
		sb.append(" / 1000 ");
		sb.append(operator);
		sb.append(" ");
		sb.append(value);
		sb.append(" / 1000");

		return sb.toString();
	}
	
	/**
	 * List<Integer> に対して IN 句を構築します
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public static String buildInClause(String property, List<Integer> value) {
		StringBuilder sb = new StringBuilder();

		sb.append(property);
		sb.append(" IN ");
		sb.append(value.stream().map(Object::toString).collect(Collectors.joining(",", "(", ")")));

		return sb.toString();
	}

	/**
	 * List<String> に対して IN 句を構築します
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public static String buildInClauseWithString(String property, List<String> value) {
		StringBuilder sb = new StringBuilder();

		sb.append(property);
		sb.append(" IN ");
		sb.append(value.stream().map(Object::toString).collect(Collectors.joining("','", "('", "')")));

		return sb.toString();
	}

	/**
	 * LIKE 句を構築します
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public static String buildLikeClause(String property, String value) {
		StringBuilder sb = new StringBuilder();
		Matcher m = pattern.matcher(value);

		sb.append(property);
		if (m.find()) {
			sb.append(" NOT LIKE '");
			sb.append(m.group(2));
		} else {
			sb.append(" LIKE '");
			sb.append(value);
		}
		sb.append("'");

		return sb.toString();
	}
	
	/**
	 * シングルクォーテーションをエスケープします
	 * 
	 * @param str
	 * @return
	 */
	public static String escapeQuote(String str) {
		return str.replace("'", "''");
	}

	/**
	 * 文字列をシングルクォーテーションで囲んで返します
	 * 
	 * @param value
	 * @return
	 */
	public static String quoteString(String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("'");
		sb.append(value);
		sb.append("'");

		return sb.toString();
	}

}
