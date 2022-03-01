/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.session;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RpaScenarioNotFound;
import com.clustercontrol.hub.model.CollectStringData;
import com.clustercontrol.rpa.model.RpaLogAnalyzePattern;
import com.clustercontrol.rpa.model.RpaScenarioCoefficientPattern;
import com.clustercontrol.rpa.model.RpaToolEnvMst;
import com.clustercontrol.rpa.scenario.factory.RpaLogParseException;
import com.clustercontrol.rpa.scenario.factory.RpaLogParseResult;
import com.clustercontrol.rpa.scenario.factory.RpaLogParser;
import com.clustercontrol.rpa.scenario.model.RpaScenario;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResult;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResult.OperationResultStatus;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultDetail;
import com.clustercontrol.rpa.util.LogTypeEnum;
import com.clustercontrol.rpa.util.QueryUtil;
import com.clustercontrol.rpa.util.RpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

/**
 * RPAログの解析、シナリオ実績更新を行うクラス
 *
 */
public class RpaLogAnalyzer {
	private static Log m_log = LogFactory.getLog(RpaLogAnalyzer.class);
	

	public static void analyzeLog(String facilityId, CollectStringData rpaLogData, RpaScenarioOperationResultCreateSetting createSetting, RpaToolEnvMst rpaToolEnvMst) throws HinemosUnknown, RpaLogParseException {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			// 解析クラスを取得
			RpaLogParser parser = getParser(rpaToolEnvMst);
			
			// 収集ログをRPAログとしてパース
			RpaLogParseResult result = parser.parse(rpaLogData.getValue());
			// ログ解析パターンを用いてログ種別を判定
			LogTypeEnum logType = matchLogType(result.getLogMessage(), rpaToolEnvMst);
		
			// シナリオ識別からシナリオIDを特定する(またはシナリオを登録)
			// 解析結果にシナリオ識別が含まれていない場合、nullとなる。
			RpaScenario scenario = findScenarioByResult(facilityId, result, logType, rpaToolEnvMst, createSetting, rpaLogData);
			
			// 実行中のシナリオ実績を検索する。
			// 実行中のシナリオ実績が無い場合、nullとなる。
			RpaScenarioOperationResult runningOperationResult = findRunningOperationResult(facilityId, createSetting);
			
			if (scenario == null && runningOperationResult != null) {
				try {
					// 実行中のシナリオ実績からシナリオを特定
					scenario = QueryUtil.getRpaScenarioPK(runningOperationResult.getScenarioId());
				} catch (RpaScenarioNotFound | InvalidRole e) {
					// 想定外エラー
					m_log.warn("analyzeLog(): unexpected Error.", e);
					throw new HinemosUnknown(e);
				}
			} else if (scenario == null && runningOperationResult == null) {
				// シナリオ、シナリオ実績共にnullの場合、シナリオを特定できないためパース失敗
				m_log.warn("analyzeLog(): unknown message."
						+ " rpaToolEnvId = " + rpaToolEnvMst.getRpaToolEnvId()
						+ ", scenarioCreateSettingId = " + createSetting.getScenarioOperationResultCreateSettingId()
						+ ", logText = " + rpaLogData.getValue());
				throw new RpaLogParseException();
			}
			
			// 解析結果からシナリオ実績の更新or登録を行う。
			RpaScenarioOperationResult targetOperationResult = getTargetOperationResult(facilityId, logType, scenario, runningOperationResult, result, createSetting, rpaToolEnvMst, rpaLogData);
			
			// 解析結果をシナリオ実績明細として登録
			while (targetOperationResult.detailsContainsLogTime(result.getLogTime())) {
				// 最新のログ時刻がログ実績明細に含まれていた場合、時刻を1ms増やす。
				// (ログ時刻がms単位で出力されておらず、同時刻のログが連続で出力された場合を考慮)
				result.setLogTime(result.getLogTime() + 1);				
			}
			
			m_log.debug(String.format("create operation result detail. resultId=%d, latestTime=%d logTime=%d, log=%s",targetOperationResult.getResultId(), targetOperationResult.getLatestTime(), result.getLogTime(), result.getLogMessage()));
			RpaScenarioOperationResultDetail operationResultDetail = new RpaScenarioOperationResultDetail();
			operationResultDetail.setLogTime(result.getLogTime());
			operationResultDetail.setLog(result.getLogMessage());
			operationResultDetail.setLogType(logType);
			operationResultDetail.setPriority(result.getPriority());
			targetOperationResult.addResultDetail(operationResultDetail);
			targetOperationResult.incrementStep();
			
			if (logType == LogTypeEnum.END) {
				// 終了時刻をセット
				targetOperationResult.setEndDate(result.getLogTime());
				
				if (targetOperationResult.getStatus() == OperationResultStatus.NORMAL_END) {
					// 終了ログの場合は実行時間、手動操作時間、手動操作コスト、削減時間、削減率を0にセット
					updateOperationReusltDetail(operationResultDetail, 0, 0);
					// 正常終了時は、実績明細から実行時間、手動操作時間、手動操作コスト、削減時間、削減率を計算
					List<RpaScenarioOperationResultDetail> resultDetails = targetOperationResult.getOperationResultDetail();
					// 実行時間:各明細の実行時間を合計
					long runTime = resultDetails.stream().mapToLong(RpaScenarioOperationResultDetail::getRunTime).sum();
					
					long manualTime;
					int coefficientCost;
					long reductionTime;
					// 実績明細からシナリオの手動操作時間を算出する。
					
					// 手動操作コスト[%] = 各明細の手動操作コストの合計[%] 
					// 実行時間[ms] = 各明細の実行時間合計[ms] × (100 + 手動操作コスト)[%] / 100
					// ※v.6.2で提供している自動化効果計算ツールと同様の計算式
					coefficientCost = resultDetails.stream().mapToInt(RpaScenarioOperationResultDetail::getCoefficientCost).sum();
					manualTime = Math.floorDiv(
							runTime * (100 + coefficientCost),
							100);
					
					// 削減時間[ms] = 手動操作時間[ms] - 実行時間[ms]
					reductionTime = manualTime - runTime;
					
					// 削減率 = 削減時間[ms] / 実行時間[ms] * 100
					int reductionRate = (int)(reductionTime * 100 / manualTime);
					
					targetOperationResult.setRunTime(runTime);
					targetOperationResult.setManualTime(manualTime);
					targetOperationResult.setReductionTime(reductionTime);
					targetOperationResult.setCoefficientCost(coefficientCost);
					targetOperationResult.setReductionRate(reductionRate);
				}
			}
		}
	}
	
	/**
	 * RPAツールマスタから解析クラスをロードする。
	 */
	private static RpaLogParser getParser(RpaToolEnvMst rpaToolEnvMst) throws HinemosUnknown {
		// RPAツールマスタから解析クラスをロード
		try {
			return (RpaLogParser)Class.forName(rpaToolEnvMst.getRpaParserClass()).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// ロードできない場合は想定外例外
			m_log.warn("getParser(): RPA Parser Class Not Found. rpaToolEnvId = " + rpaToolEnvMst.getRpaToolEnvId());
			throw new HinemosUnknown(e);
		}
	}
	
	/**
	 * ログパターンからログ種別を特定する。
	 */
	private static LogTypeEnum matchLogType(String log, RpaToolEnvMst rpaToolEnvMst) {
		for (RpaLogAnalyzePattern entry : rpaToolEnvMst.getLogAnalyzePattern()) {
			if(RpaUtil.patternMatch(log, entry)) {
				return entry.getLogType();
			}
		}
		
		// どのパターンにもマッチしない場合は状態なし
		return LogTypeEnum.NONE;
	}
	
	/**
	 * ログパターンからシナリオ係数を特定する。
	 */
	private static double matchCoefficient(String log, RpaToolEnvMst rpaToolEnvMst) {
		for (RpaScenarioCoefficientPattern entry : rpaToolEnvMst.getScenarioCoefficientPattern()) {
			if(RpaUtil.patternMatch(log, entry)) {
				return entry.getCoefficient();
			}
		}
		// 係数パターンとマッチしない場合、RPAツールマスタのデフォルト係数を用いる
		return rpaToolEnvMst.getRpaToolMst().getDefaultCoefficient();
	}
	
	/**
	 * ログ解析結果からシナリオを特定する。
	 * ログ解析結果にシナリオ識別子が含まれていない場合は、nullを返す。
	 * 新しいシナリオの場合は新たにシナリオを登録する。
	 */
	private static RpaScenario findScenarioByResult(
			String facilityId
			, RpaLogParseResult result
			, LogTypeEnum logType
			, RpaToolEnvMst rpaToolEnvMst
			, RpaScenarioOperationResultCreateSetting createSetting
			, CollectStringData rpaLogData
			) throws RpaLogParseException, HinemosUnknown {
		if (logType == LogTypeEnum.START) {
			// 開始ログの場合、新たにシナリオを登録するか判定を行う
			// 開始の場合は必ずシナリオ識別が入っている前提(そうでないとシナリオ特定できない)
			if (result.getIdentifyString() == null || result.getIdentifyString().isEmpty()) {
				// シナリオ識別が無い場合RpaLogParseExceptionをthrow
				String message = "invalid RPA start message."
						+ "rpaToolEnvId = " + rpaToolEnvMst.getRpaToolEnvId()
						+ ", scenarioOperationResultCreateId = " + createSetting.getScenarioOperationResultCreateSettingId()
						+ ", logText = " + rpaLogData.getValue();
				throw new RpaLogParseException(message);
			}
			
			// シナリオ判定
			// 新しいシナリオの場合は自動登録が実施される。
			return RpaScenarioJudger.scenarioJudge(facilityId, createSetting, result, rpaToolEnvMst.getRpaToolMst().getRpaToolId());
		} else if (!result.getIdentifyString().isEmpty()) {
			// 開始ログ以外で、シナリオ識別子が含まれている場合
			// 作成設定ID、ファシリティIDを用いてシナリオを特定
			try {
				return QueryUtil.getScenario(
						createSetting.getScenarioOperationResultCreateSettingId() 
						, facilityId 
						, result.getIdentifyString()
						, createSetting.getOwnerRoleId()
						);
			} catch (RpaScenarioNotFound e) {
				// 登録済のシナリオが無い場合、シナリオが特定できない不明なログのため、パース失敗例外
				throw new RpaLogParseException(e);
			}
		} else {
			// シナリオ識別子が無い場合、nullを返す(実行中のシナリオからシナリオを特定する)
			return null;
		}
	}
	
	
	/**
	 * ファシリティIDとシナリオ実績作成設定から実行中のシナリオ実績を取得する。
	 */
	private static RpaScenarioOperationResult findRunningOperationResult(String facilityId, RpaScenarioOperationResultCreateSetting createSetting) {
		// 引数がnullの場合、ファシリティと監視項目IDで特定
		// (1ノードで同時に実行されるのは1シナリオのみ)				
		return findRunningOperationResultByFaclityAndCreateSetting(facilityId, createSetting.getScenarioOperationResultCreateSettingId());
	}
	
	/**
	 * ファシリティIDとシナリオ実績作成IDで実行中のシナリオ実績を検索する。
	 * 実行中のシナリオが無い場合nullを返す。
	 */
	private static RpaScenarioOperationResult findRunningOperationResultByFaclityAndCreateSetting(String facilityId, String scenarioOperationResultCreateSettingId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			EntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("RpaScenarioOperationResult.findRunningByFacilityAndScenarioOperationResultCreateSettingId",
					RpaScenarioOperationResult.class)
					.setParameter("NORMAL_RUNNING", OperationResultStatus.NORMAL_RUNNING)
					.setParameter("ERROR_RUNNING", OperationResultStatus.ERROR_RUNNING)
					.setParameter("scenarioOperationResultCreateSettingId", scenarioOperationResultCreateSettingId)
					.setParameter("facilityId", facilityId)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
		
	}
	
	/**
	 * シナリオ実績の更新or登録を行う。
	 * 更新or登録したシナリオ実績を返す。
	 * また、最新のシナリオ実績明細の登録は、このメソッドの後で実施する。
	 */
	private static RpaScenarioOperationResult getTargetOperationResult(
			String facilityId
			, LogTypeEnum logType
			, RpaScenario scenario
			, RpaScenarioOperationResult runningOperationResult
			, RpaLogParseResult result
			, RpaScenarioOperationResultCreateSetting createSetting
			, RpaToolEnvMst rpaToolEnvMst
			, CollectStringData rpaLogData
			) throws RpaLogParseException {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			RpaScenarioOperationResult targetOperationResult;
			switch (logType) {
			case START:
				// すでに実行中のシナリオがある場合は前回実行の実績とし、状態「不明」に変更
				if (runningOperationResult != null) {
					runningOperationResult.setStatus(OperationResultStatus.UNKNOWN);					
					m_log.info(String.format("analyzeLog() unknown operation result. scenarioId=%s, facilityId = %s, startDate = %s"
							, scenario.getScenarioId(), facilityId, runningOperationResult.getStartDate()));
				}
				m_log.debug(String.format("analyzeLog() operation start. scenarioId=%s, facilityId = %s, startDate = %s"
						, scenario.getScenarioId(), facilityId, result.getLogTime()));

				// 新たなシナリオ実績を登録し、更新対象とする。
				targetOperationResult = new RpaScenarioOperationResult(
						scenario.getScenarioId(),
						facilityId,
						result.getLogTime(),
						OperationResultStatus.NORMAL_RUNNING,
						createSetting.getScenarioOperationResultCreateSettingId(),
						rpaToolEnvMst.getRpaToolEnvId());
				em.persist(targetOperationResult);
				break;
			case ERROR:
			case END:
			case NONE:
			default:
				// 開始ログ以外で実行中シナリオがnullの場合、パース失敗として例外を投げる。
				if (runningOperationResult == null) {
					m_log.warn("analyzeLog(): unknown scenario message."
							+ "rpaToolEnv = " + rpaToolEnvMst.getRpaToolEnvId()
							+ ", scenarioCreateSettingId = " + createSetting.getScenarioOperationResultCreateSettingId()
							+ ", logText = " + rpaLogData.getValue());
					throw new RpaLogParseException();					
				} else {
					targetOperationResult = runningOperationResult;
				}
				OperationResultStatus previousStatus = targetOperationResult.getStatus();
				switch (logType) {
				case ERROR:
					// ログ種別がエラーの場合、シナリオ実績のステータスを「実行中(エラー)」に変更
					targetOperationResult.setStatus(OperationResultStatus.ERROR_RUNNING);
					break;
				case END:
					// 直前のステータスに伴い、シナリオ実績のステータスを「終了(正常)」または「終了(エラー)」に変更
					if (previousStatus == OperationResultStatus.NORMAL_RUNNING) {
						targetOperationResult.setStatus(OperationResultStatus.NORMAL_END);
					} else {
						targetOperationResult.setStatus(OperationResultStatus.ERROR_END);
					}
					m_log.debug(String.format("analyzeLog() operation end. scenarioId=%s, facilityId = %s, endDate = %s, status = %s"
							, scenario.getScenarioId(), facilityId, result.getLogTime(), targetOperationResult.getStatus().name()));
					break;
				default:
					break;
				}
				
				// ステータスが実行中(エラー)、または終了(エラー)以外の場合、解析結果を用いてシナリオ実績明細を更新する。
				if (targetOperationResult.getStatus() != OperationResultStatus.ERROR_RUNNING
						&& targetOperationResult.getStatus() != OperationResultStatus.ERROR_END) {
					updateOperationResult(targetOperationResult, result, rpaToolEnvMst);
				}
				break;
			}
			return targetOperationResult;
		}
	}
	
	/**
	 * 解析結果を用いて実績明細を更新する。
	 */
	private static void updateOperationResult(RpaScenarioOperationResult targetOperationResult, RpaLogParseResult result, RpaToolEnvMst rpaToolEnvMst) {

		// -- 手動操作時間を計算 --
		// シナリオ係数パターンで直前の実績明細ログのシナリオ係数=手動操作コストを特定する。
		RpaScenarioOperationResultDetail latestDetail = targetOperationResult.getLatestDetail();

		double coefficient;
		switch (latestDetail.getLogType()) {
		case START:
		case END:
			// 開始ログ、終了ログはシナリオ係数を0とする。
			coefficient = 0;
			break;
		default:
			// 自動化効果計算パターンよりシナリオ係数をマッチする。
			coefficient = matchCoefficient(latestDetail.getLog(), rpaToolEnvMst);
			break;
		}
		
		// 直前の実績明細の実行時間を計算する。
		long prevRunTime = result.getLogTime() - targetOperationResult.getLatestTime();
		
		// 直前の実績明細を更新
		updateOperationReusltDetail(latestDetail, prevRunTime, coefficient);
	}
	
	
	/**
	 * シナリオ実績明細に実行時間を反映する。
	 */
	private static void updateOperationReusltDetail(RpaScenarioOperationResultDetail operationResultDetail, long runTime, double coefficient) {
		// 手動操作時間、手動操作コスト、削減時間、削減率を計算
		
		// 手動操作時間[ms] = 実行時間[ms] * (1 + 手動操作コスト)
		long manualTime = Math.round(runTime * coefficient) + runTime;
		// 手動操作コスト=シナリオ係数[%]
		int coefficientCost = Math.round((float)coefficient * 100);
		// 削減時間[ms] = 手動操作時間[ms] - 実行時間[ms]
		long reductionTime = manualTime - runTime;
		// 削減率[%] = 削減時間[ms] / 手動操作時間[ms] * 100
		int reductionRate;
		if (manualTime != 0) {
			reductionRate = (int)(reductionTime * 100 / manualTime);
		} else {
			// 手動操作時間が(実行時間が)0の場合は、手動操作コストを0にする。
			reductionRate = 0;
		}
		
		operationResultDetail.setRunTime(runTime);
		operationResultDetail.setManualTime(manualTime);
		operationResultDetail.setCoefficientCost(coefficientCost);
		operationResultDetail.setReductionTime(reductionTime);
		operationResultDetail.setReductionRate(reductionRate);
	}
}
