/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.util;

import static com.clustercontrol.jobmanagement.rpa.bean.RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC;
import static com.clustercontrol.jobmanagement.rpa.bean.RpaJobReturnCodeConditionConstant.GREATER_THAN;
import static com.clustercontrol.jobmanagement.rpa.bean.RpaJobReturnCodeConditionConstant.GREATER_THAN_OR_EQUAL_TO;
import static com.clustercontrol.jobmanagement.rpa.bean.RpaJobReturnCodeConditionConstant.LESS_THAN;
import static com.clustercontrol.jobmanagement.rpa.bean.RpaJobReturnCodeConditionConstant.LESS_THAN_OR_EQUAL_TO;
import static com.clustercontrol.jobmanagement.rpa.bean.RpaJobReturnCodeConditionConstant.NOT_EQUAL_NUMERIC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * RPAシナリオジョブでリターンコードが判定条件を満たしているかどうかを判定するクラス
 */
public class ReturnCodeConditionChecker {
	/** ロガー */
	static private Log m_log = LogFactory.getLog(ReturnCodeConditionChecker.class);
	/** 複数指定の区切り文字 */
	private static final String DELIMITER = ",";
	/** 判定条件の文字列にマッチする正規表現 */
	public static final String CONDITION_REGEX = "((^|,)([+-]?\\d+)(:([+-]?\\d+))?)+$";
	/** 複数指定の文字列にマッチする正規表現 */
	public static final String MULTI_CONDITION_REGEX = "([+-]?\\d+),([+-]?\\d+)";
	/** 範囲指定の文字列にマッチする正規表現 */
	public static final String RANGE_CONDITION_REGEX = "([+-]?\\d+):([+-]?\\d+)";
	/**
	 * コロンによる範囲指定にマッチする正規表現<br>
	 * 例： 1:10, -1:+1
	 */
	private static Pattern rangePattern = Pattern.compile(RANGE_CONDITION_REGEX);

	/**
	 * リターンコードが判定条件を満たしているかどうかを判定します。
	 * 
	 * @param returnCode
	 *            判定対象のリターンコード
	 * @param returnCodeStr
	 *            判定条件のリターンコード
	 * @param returnCodeCondition
	 *            判定条件
	 * @return true: 条件を満たしている、false: 条件を満たしていない
	 */
	public static boolean check(int returnCode, String returnCodeStr, int returnCodeCondition) {
		m_log.info("check() : start");
		boolean result = false;
		returnCodeStr = returnCodeStr.replaceAll("\\s", ""); // スペースは除去しておく
		// 条件に範囲指定が含まれる場合
		if (isRangeSpecification(returnCodeStr)) {
			// 範囲指定で条件が=/!=以外の場合はfalse
			if (returnCodeCondition != EQUAL_NUMERIC && returnCodeCondition != NOT_EQUAL_NUMERIC) {
				m_log.warn("check() : invalid condition=\"" + returnCodeCondition + "\", returnCode="
						+ returnCodeStr);
			} else {
				// 範囲指定を個々の値に展開し判定する
				Set<Integer> returnCodeSet = expand(returnCodeStr);
				if (returnCodeCondition == EQUAL_NUMERIC) {
					result = returnCodeSet.contains(returnCode);
				} else {
					result = !returnCodeSet.contains(returnCode);
				}
			}
		} else {
			Integer returnCodeInt = null;
			try {
				// 数値に変換できない文字の場合はfalse
				returnCodeInt = Integer.parseInt(returnCodeStr);
			} catch (NumberFormatException e) {
				m_log.warn("check() : invalid returnCodeStr=" + returnCodeStr + ", " + e.getMessage());
			}
			if (returnCodeInt != null) {
				switch (returnCodeCondition) {
				case EQUAL_NUMERIC:
					result = returnCode == returnCodeInt;
					break;
				case NOT_EQUAL_NUMERIC:
					result = returnCode != returnCodeInt;
					break;
				case GREATER_THAN:
					result = returnCode > returnCodeInt;
					break;
				case GREATER_THAN_OR_EQUAL_TO:
					result = returnCode >= returnCodeInt;
					break;
				case LESS_THAN:
					result = returnCode < returnCodeInt;
					break;
				case LESS_THAN_OR_EQUAL_TO:
					result = returnCode <= returnCodeInt;
					break;
				default:
					m_log.warn("check() : invalid condition=\"" + returnCodeCondition + "\"");
				}
			}
		}
		m_log.info("check() : result=" + result);
		return result;
	}

	/**
	 * 条件に複数指定または範囲指定が含まれるかどうかを返す
	 * 
	 * @param returnCodeStr
	 *            判定条件のリターンコード
	 * @return true: 含まれる、false: 含まれない
	 */
	private static boolean isRangeSpecification(String returnCodeStr) {
		boolean result = returnCodeStr.contains(DELIMITER) || rangePattern.matcher(returnCodeStr).matches();
		m_log.debug("isRangeSpecification() : " + result);
		return result;
	}

	/**
	 * 複数指定と範囲指定を個々の値に展開して返す
	 * 
	 * @param returnCodeStr
	 *            判定条件のリターンコード
	 * @return 展開後のSet
	 */
	private static Set<Integer> expand(String returnCodeStr) {
		Set<Integer> ret = new HashSet<>();
		for (String str : returnCodeStr.split(DELIMITER)) {
			if (rangePattern.matcher(str).matches()) {
				ret.addAll(expandRange(str));
			} else {
				try {
					ret.add(Integer.parseInt(str));
				} catch (NumberFormatException e) {
					// 数値に変換できない場合は無視する
					m_log.warn("expand() : invalid str=" + str + ", " + e.getMessage());
				}
			}
		}
		m_log.debug("expand() : returnCodeStr=" + returnCodeStr + ", result=" + ret);
		return ret;
	}

	/**
	 * 範囲指定を個々の値に展開して返す
	 * 
	 * @param rangeStr
	 *            範囲指定の文字列
	 * @return 展開後のList
	 */
	private static List<Integer> expandRange(String rangeStr) {
		List<Integer> ret = new ArrayList<>();
		List<Integer> range = new ArrayList<>();
		Matcher matcher = rangePattern.matcher(rangeStr);

		if (!matcher.matches() || matcher.groupCount() != 2) {
			m_log.warn("expandRange() : invalid rangeStr=" + rangeStr);
		} else {
			try {
				range.add(Integer.parseInt(matcher.group(1)));
				range.add(Integer.parseInt(matcher.group(2)));
			} catch (NumberFormatException e) {
				// 数値に変換できない場合は無視する
				m_log.warn("expandRange() : invalid rangeStr=" + rangeStr + ", " + e.getMessage());
			}
			if (range.size() == 2) {
				Collections.sort(range);
				int start = range.get(0);
				int end = range.get(1);
				for (int i = start; i <= end; i++) {
					ret.add(i);
				}
			}
		}
		m_log.debug("expandRange() : rangeStr=" + rangeStr + ", result=" + ret);
		return ret;
	}

}
