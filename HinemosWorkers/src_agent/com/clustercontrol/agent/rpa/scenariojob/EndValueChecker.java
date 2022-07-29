/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa.scenariojob;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRpaJobEndValueConditionInfoResponse;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;

/**
 * RPAのリターンコードとログからジョブの終了値を判定するクラス
 */
public class EndValueChecker {

	/** ロガー */
	static private Log m_log = LogFactory.getLog(EndValueChecker.class);
	/** ジョブ実行指示情報 */
	private AgtRunInstructionInfoResponse runInstructionInfo;
	/** RPAプログラムのリターンコード */
	private int returnCode;
	/** 最初に条件を満たした判定条件 */
	private AgtRpaJobEndValueConditionInfoResponse satisfiedCondition;
	/** 終了値判定方法リスト */
	private List<EndValueCheckStrategy> endValueCheckStrategies = new ArrayList<>();

	/**
	 * コンストラクタ
	 * 
	 * @param runInstructionInfo
	 *            ジョブ実行指示情報
	 * @param returnCode
	 *            判定対象のリターンコード
	 */
	public EndValueChecker(AgtRunInstructionInfoResponse runInstructionInfo, int returnCode) {
		this.runInstructionInfo = runInstructionInfo;
		this.returnCode = returnCode;
	}

	/**
	 * 終了値判定条件からジョブの終了値を判定します。
	 * 
	 * @return ジョブの終了値
	 */
	public int check() {
		m_log.info("check() : sessionId=" + runInstructionInfo.getSessionId() + ", jobunitId="
				+ runInstructionInfo.getJobunitId() + ", jobId=" + runInstructionInfo.getJobId());
		// 優先度順に条件をチェックし、最初に満たされている条件の終了値を返す
		for (AgtRpaJobEndValueConditionInfoResponse endValueCondition : runInstructionInfo
				.getRpaEndValueConditionInfoList()) {
			if (evaluate(endValueCondition)) {
				satisfiedCondition = endValueCondition;
				int endValue;
				// RPAのリターンコードをそのまま終了値とする場合
				if (endValueCondition.getUseCommandReturnCodeFlg() != null
						&& endValueCondition.getUseCommandReturnCodeFlg()) {
					endValue = returnCode;
				} else {
					endValue = endValueCondition.getEndValue();
				}
				m_log.info("check() : condition is satisfied, orderNo=" + endValueCondition.getOrderNo() + ", endValue="
						+ endValue);
				return endValue;
			}
		}
		m_log.info("check() : no conditions are satisfied, default endValue="
				+ runInstructionInfo.getRpaDefaultEndValue());
		return runInstructionInfo.getRpaDefaultEndValue();
	}

	/**
	 * 終了値判定条件が満たされているか評価します。
	 * @param endValueCondition
	 * @return true: 条件を満たしている / false: 条件を満たしていない
	 */
	private boolean evaluate(AgtRpaJobEndValueConditionInfoResponse endValueCondition) {
		for (EndValueCheckStrategy strategy : endValueCheckStrategies) {
			if (strategy.getConditionType() == endValueCondition.getConditionType()) {
				return strategy.isSatisfied(endValueCondition);
			}
		}
		m_log.warn("evaluate() : unsupported condition type=" + endValueCondition.getConditionType() + ", orderNo="
				+ endValueCondition.getOrderNo());
		return false;
	}

	/**
	 * 終了値判定方法を追加します。
	 * @param strategy
	 */
	public void addStrategy(EndValueCheckStrategy strategy) {
		endValueCheckStrategies.add(strategy);
	}

	/**
	 * 最初に満たした判定条件を返します。
	 * 
	 * @return 最初に満たした判定条件
	 */
	public AgtRpaJobEndValueConditionInfoResponse getSatisfiedCondition() {
		return satisfiedCondition;
	}
}
