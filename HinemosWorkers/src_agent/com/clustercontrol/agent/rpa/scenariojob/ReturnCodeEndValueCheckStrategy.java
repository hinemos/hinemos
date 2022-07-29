/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa.scenariojob;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRpaJobEndValueConditionInfoResponse;

import com.clustercontrol.jobmanagement.rpa.bean.RpaJobEndValueConditionTypeConstant;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobReturnCodeConditionConstant;
import com.clustercontrol.jobmanagement.rpa.util.ReturnCodeConditionChecker;

/**
 * リターンコードから終了値判定条件を満たしているかを確認するクラス<br>
 */
public class ReturnCodeEndValueCheckStrategy implements EndValueCheckStrategy {

	/** ロガー */
	static private Log m_log = LogFactory.getLog(ReturnCodeEndValueCheckStrategy.class);
	/** RPAシナリオ実行のリターンコード */
	private int returnCode;

	/**
	 * コンストラクタ
	 * 
	 * @param returnCode
	 *            判定対象のリターンコード
	 */
	public ReturnCodeEndValueCheckStrategy(int returnCode) {
		this.returnCode = returnCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.agent.rpa.EndValueCheckMethod#isSatisfied(org.
	 * openapitools.client.model.AgtRpaJobEndValueConditionInfoResponse)
	 */
	@Override
	public boolean isSatisfied(AgtRpaJobEndValueConditionInfoResponse endValueCondition) {
		boolean result = ReturnCodeConditionChecker.check(returnCode, endValueCondition.getReturnCode(),
				endValueCondition.getReturnCodeCondition());
		m_log.info("isSatisfied() : " + result + ", orderNo=" + endValueCondition.getOrderNo());
		return result;
	}

	/* (non-Javadoc)
	 * @see com.clustercontrol.agent.rpa.EndValueCheckStrategy#getConditionType()
	 */
	@Override
	public int getConditionType() {
		return RpaJobEndValueConditionTypeConstant.RETURN_CODE;
	}

	public static void main(String argv[]) {
		int returnCode = 0;
		Map<Integer, String> map = new HashMap<>();
		map.put(RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC, "=");
		map.put(RpaJobReturnCodeConditionConstant.NOT_EQUAL_NUMERIC, "!=");
		map.put(RpaJobReturnCodeConditionConstant.GREATER_THAN, ">");
		map.put(RpaJobReturnCodeConditionConstant.GREATER_THAN_OR_EQUAL_TO, ">=");
		map.put(RpaJobReturnCodeConditionConstant.LESS_THAN, "<");
		map.put(RpaJobReturnCodeConditionConstant.LESS_THAN_OR_EQUAL_TO, "<=");
		AgtRpaJobEndValueConditionInfoResponse dto = new AgtRpaJobEndValueConditionInfoResponse();
		// 正常系
		String returnCodeStr = "0";
		Integer condition = RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		ReturnCodeEndValueCheckStrategy check = new ReturnCodeEndValueCheckStrategy(returnCode);
		boolean expected = true;
		boolean actual = check.isSatisfied(dto);
		System.out.println(String.format("Case1: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		returnCodeStr = "0";
		condition = RpaJobReturnCodeConditionConstant.NOT_EQUAL_NUMERIC;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = false;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case2: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		returnCodeStr = "+9";
		condition = RpaJobReturnCodeConditionConstant.LESS_THAN;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = true;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case3: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		returnCodeStr = "0";
		condition = RpaJobReturnCodeConditionConstant.LESS_THAN_OR_EQUAL_TO;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = true;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case4: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		returnCodeStr = "-1";
		condition = RpaJobReturnCodeConditionConstant.GREATER_THAN;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = true;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case5: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		returnCodeStr = "0";
		condition = RpaJobReturnCodeConditionConstant.GREATER_THAN_OR_EQUAL_TO;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = true;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case6: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		returnCodeStr = "0,1,2";
		condition = RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = true;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case7: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		returnCodeStr = "0,1,2";
		condition = RpaJobReturnCodeConditionConstant.NOT_EQUAL_NUMERIC;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = false;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case8: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		returnCodeStr = "-1:+1";
		condition = RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = true;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case9: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		returnCodeStr = "-1:+1,1:2";
		condition = RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = true;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case10: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		// 異常系
		// 範囲指定で=/!=以外が指定されている
		returnCodeStr = "0,1";
		condition = RpaJobReturnCodeConditionConstant.GREATER_THAN;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = false;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case11: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		// 数値以外の文字
		returnCodeStr = "x";
		condition = RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = false;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case12: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		// 数値以外の文字 (不正な個所は無視して判定)
		returnCodeStr = "0,1-x,y";
		condition = RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = true;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case13: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		// スペースが含まれる (スペースは無視して判定)
		returnCodeStr = " 0 ";
		condition = RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = true;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case14: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		// スペースが含まれる (スペースは無視して判定)
		returnCodeStr = " 0, 1 ";
		condition = RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = true;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case15: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;

		// スペースが含まれる (スペースは無視して判定)
		returnCodeStr = " 0 : 1 ";
		condition = RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC;
		dto.setReturnCode(returnCodeStr);
		dto.setReturnCodeCondition(condition);
		check = new ReturnCodeEndValueCheckStrategy(returnCode);
		expected = true;
		actual = check.isSatisfied(dto);
		System.out.println(String.format("Case16: returnCode %s %s", map.get(condition), returnCodeStr));
		System.out.println(String.format("(Expected, Actual) : (%s, %s)%n", expected, actual));
		assert expected == actual;
	}
}
