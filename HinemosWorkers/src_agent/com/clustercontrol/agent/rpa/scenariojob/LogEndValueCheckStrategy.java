/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa.scenariojob;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRpaJobEndValueConditionInfoResponse;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;

import com.clustercontrol.jobmanagement.rpa.bean.RpaJobEndValueConditionTypeConstant;

/**
 * ログの内容から終了値判定条件を満たしているかを確認するクラス<br>
 */
public class LogEndValueCheckStrategy implements EndValueCheckStrategy {

	/** ロガー */
	static private Log m_log = LogFactory.getLog(LogEndValueCheckStrategy.class);
	/**
	 * ジョブ実行指示情報<br>
	 * RPAの実行ログの取得のために使用します。
	 */
	private AgtRunInstructionInfoResponse runInstructionInfo;

	/**
	 * コンストラクタ
	 * 
	 * @param runInstructionInfo
	 *            ジョブ実行指示情報
	 */
	public LogEndValueCheckStrategy(AgtRunInstructionInfoResponse runInstructionInfo) {
		this.runInstructionInfo = runInstructionInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.agent.rpa.EndValueCheckMethod#isSatisfied(org.
	 * openapitools.client.model.AgtRpaJobEndValueConditionInfoResponse)
	 */
	@Override
	public boolean isSatisfied(AgtRpaJobEndValueConditionInfoResponse endValueCondition) {
		// 判定条件のorderNoが出力されたログに基づくorderNoと一致すればtrueを返す
		boolean result = false;
		ScenarioLogCache scenarioLogCache = ScenarioLogCache.get(runInstructionInfo);
		if (scenarioLogCache.getOrderNo() != null) {
			result = endValueCondition.getOrderNo().equals(scenarioLogCache.getOrderNo());
			m_log.debug("isSatisfied() : orderNo=" + endValueCondition.getOrderNo() + ", logOrderNo="
					+ scenarioLogCache.getOrderNo() + ", logMessage=" + scenarioLogCache.getLogMessage());
		}
		m_log.info("isSatisfied() : " + result + ", orderNo=" + endValueCondition.getOrderNo());
		return result;
	}

	/* (non-Javadoc)
	 * @see com.clustercontrol.agent.rpa.EndValueCheckStrategy#getConditionType()
	 */
	@Override
	public int getConditionType() {
		return RpaJobEndValueConditionTypeConstant.LOG;
	}
}
