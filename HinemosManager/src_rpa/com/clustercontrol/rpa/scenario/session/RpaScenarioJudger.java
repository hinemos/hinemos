/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.RpaScenarioDuplicate;
import com.clustercontrol.fault.RpaScenarioNotFound;
import com.clustercontrol.rpa.scenario.factory.RpaLogParseResult;
import com.clustercontrol.rpa.scenario.model.RpaScenario;
import com.clustercontrol.rpa.scenario.model.RpaScenario.CulcType;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.rpa.session.RpaControllerBean;
import com.clustercontrol.rpa.util.QueryUtil;

/**
 * RPAのシナリオ判定・自動登録を行うクラス
 *
 */
public class RpaScenarioJudger {
	private static Log m_log = LogFactory.getLog( RpaScenarioJudger.class);
	
	/**
	 * いずれかの処理を行う。
	 * <ol>
	 * <li>同一作成設定ID、シナリオ識別かつファシリティIDがシナリオ実行ノードとして登録されている場合は、そのシナリオを返す。</li>
	 * <li>同一作成設定ID、シナリオ識別のシナリオで、ノード間で共通のシナリオがある場合は、そのシナリオの実行ノードとしてファシリティIDを登録し、そのシナリオを返す。</li>
	 * <li>上記条件に一致するシナリオが無い場合は、新たにシナリオを登録し、そのシナリオを返す。</li>
	 * </ol>
	 * 
	 * @param facilityId シナリオ実行ノードのファシリティID
	 * @param createSetting シナリオ実績作成設定情報
	 * @param result RPAログパース結果
	 * @param rpaToolId RPAツールID
	 * @return シナリオ情報(引数の情報にマッチ、または新たに自動登録された)
	 * @throws HinemosUnknown
	 */
	public static RpaScenario scenarioJudge(String facilityId, RpaScenarioOperationResultCreateSetting createSetting, RpaLogParseResult result, String rpaToolId)
			throws HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			RpaScenario scenario;
			// シナリオ識別子
			String identifyString = result.getIdentifyString();
			m_log.debug(String.format("scenarioMatch() start. scenarioCreateSetting=%s, facilityId = %s, identifyString = %s"
					, createSetting, facilityId, identifyString));
			
			// 1. 同一作成設定ID、シナリオ識別かつファシリティIDがシナリオ実行ノードとして登録されている場合は、そのシナリオを返す。
			try {
				scenario = QueryUtil.getScenario(createSetting.getScenarioOperationResultCreateSettingId(), facilityId, identifyString, createSetting.getOwnerRoleId());
			} catch (RpaScenarioNotFound e) {
				// 一致するシナリオが無い場合、既存のシナリオに対し新たな実行ノード、または新たなシナリオの登録を行う。
				// Hinemosプロパティによって判定方法を選択
				if (!HinemosPropertyCommon.rpa_collect_scenario_classify_node.getBooleanValue()){
					// シナリオ識別が同じ場合、別のノードでも同じシナリオと判定する。
					// 2. 同一作成設定ID、シナリオ識別のシナリオで、ノード間で共通のシナリオがある場合は、そのシナリオの実行ノードとしてファシリティIDを登録し、そのシナリオを返す。
					try {
						RpaScenario commonScenario = QueryUtil.getCommonScenario(createSetting.getScenarioOperationResultCreateSettingId(), identifyString);
						
						// 実行ノード登録
						m_log.debug(String.format("scenarioRegist() find common scenario. scenarioId = %s, facilityId = %s, identifyString = %s"
								, commonScenario.getScenarioId(), facilityId, identifyString));
						commonScenario.addExecNode(facilityId);
						scenario = commonScenario;
					} catch (RpaScenarioNotFound e1) {
						// 該当するシナリオが無い場合、新たなシナリオを登録する。
						scenario = scenarioRegist(facilityId, createSetting, result, rpaToolId);
					}
				} else {
					// 1,2に該当するシナリオが無いため、新たにシナリオを登録する。
					scenario = scenarioRegist(facilityId, createSetting, result, rpaToolId);
				}
			}
			return scenario;
		}
	}
	
	/**
	 * シナリオを自動登録する。<BR>
	 */
	private static RpaScenario scenarioRegist(String facilityId, RpaScenarioOperationResultCreateSetting createSetting, RpaLogParseResult result, String rpaToolId)
			throws HinemosUnknown {
		m_log.debug("scenarioRegist(): start. identify=" + result.getIdentifyString());
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			RpaScenario newScenario = new RpaScenario();
			newScenario.setScenarioName(result.getIdentifyString());
			newScenario.setScenarioIdentifyString(result.getIdentifyString());
			newScenario.setRpaToolId(rpaToolId);
			newScenario.setScenarioOperationResultCreateSettingId(createSetting.getScenarioOperationResultCreateSettingId());
			newScenario.setOpeStartDate(0L);
			// 操作時間計算方法:自動算出する(デフォルト)
			newScenario.setManualTimeCulcType(CulcType.AUTO);
			// 共通のシナリオ(チェックボックス)
			newScenario.setCommonNodeScenario(!HinemosPropertyCommon.rpa_collect_scenario_classify_node.getBooleanValue());
			// 実行ノード登録
			newScenario.addExecNode(facilityId);
			
			// シナリオ登録
			// シナリオID、オーナーロールIDは自動設定される
			RpaScenario ret = new RpaControllerBean().addRpaScenario(newScenario);

			return ret;
		} catch (RpaScenarioDuplicate | InvalidSetting | InvalidRole e) {
			// 想定外エラー
			m_log.warn("scenarioRegist(): Failed to auto scenario regist:" + e.getMessage(), e);
			throw new HinemosUnknown(e);
		}
	}
}
