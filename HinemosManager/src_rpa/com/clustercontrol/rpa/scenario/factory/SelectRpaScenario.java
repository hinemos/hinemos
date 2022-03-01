/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RpaScenarioNotFound;
import com.clustercontrol.fault.RpaToolMasterNotFound;
import com.clustercontrol.rpa.model.RpaToolMst;
import com.clustercontrol.rpa.scenario.bean.RpaScenarioFilterInfo;
import com.clustercontrol.rpa.scenario.model.RpaScenario;
import com.clustercontrol.rpa.util.QueryUtil;

/**
 * RPAシナリオ情報を検索するクラス
 *
 */
public class SelectRpaScenario {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectRpaScenario.class );

	/**
	 * RPAシナリオ情報を返します。
	 */
	public RpaScenario getRpaScenario(String scenarioId) throws RpaScenarioNotFound, InvalidRole {
		// RPAシナリオ情報を取得
		RpaScenario entity = null;
		entity = QueryUtil.getRpaScenarioPK(scenarioId);
		
		return entity;
	}
	
	/**
	 * RPAシナリオ一覧を取得する(権限チェックなし)
	 */
	public List<RpaScenario> getRpaScenarioList() {
		return QueryUtil.getRpaScenarioList_NONE();
	}

	/**
	 * 指定したフィルタにマッチするRPAシナリオ情報一覧を返します。
	 */
	public ArrayList<RpaScenario> getRpaScenarioList(RpaScenarioFilterInfo condition) {
		m_log.debug("getRpaScenarioList() condition ");
		if(m_log.isDebugEnabled()){
			if(condition != null){
				m_log.debug("getRpaScenarioList() " +
						"RpaToolId = " + condition.getRpaToolId() +
						", scenarioId = " + condition.getScenarioId() +
						", scenarioName = " + condition.getScenarioName() +
						", scenarioIdentifyString = " + condition.getScenarioIdentifyString() +
						", scenarioOperationResultCreateSettingId = " + condition.getScenarioOperationResultCreateSettingId() +
						", ownerRoleId = " + condition.getOwnerRoleId());
			}
		}

		ArrayList<RpaScenario> filterList = new ArrayList<RpaScenario>();
		// 条件未設定の場合は空のリストを返却する
		if(condition == null){
			m_log.debug("getRpaScenarioList() condition is null");
			return filterList;
		}

		// RpaScenario情報を取得
		List<RpaScenario> entityList = QueryUtil.getRpaScenarioByFilter(
				condition.getScenarioOperationResultCreateSettingId(),
				condition.getRpaToolId(),
				condition.getScenarioId(),
				condition.getScenarioName(),
				condition.getScenarioIdentifyString(),
				condition.getOwnerRoleId());

		for(RpaScenario entity : entityList){
			m_log.debug("getRpaScenarioList() add display list : target = " + entity.getScenarioId());
			filterList.add(entity);
		}
		return filterList;
	}
	
	/**
	 * RPAシナリオのシナリオ名情報を返します。
	 */
	public Map<String,String> getRpaScenarioNameMap() {
		Map<String,String> scenarioMap = new HashMap<>();
		List<RpaScenario> entityList = QueryUtil.getRpaScenarioByFilter(null,null,null,null,null,null);
		
		for (RpaScenario entity : entityList){
			scenarioMap.put(entity.getScenarioId(), entity.getScenarioName());
		}
		
		return scenarioMap;
	}
	
	/**
	 * RPAシナリオに紐づくRPAツール名情報を返します。
	 */
	public Map<String,String> getRpaToolNameMap() throws RpaToolMasterNotFound, InvalidRole {
		Map<String,String> toolMap = new HashMap<>();
		List<RpaToolMst> toolEntityList = QueryUtil.getRpaToolMstList();
		
		for (RpaToolMst entity : toolEntityList){
			toolMap.put(entity.getRpaToolId(), entity.getRpaToolName());
		}
		
		Map<String,String> scenarioToolMap = new HashMap<>();
		List<RpaScenario> scenarioEntityList = QueryUtil.getRpaScenarioByFilter(null,null,null,null,null,null);
		
		for (RpaScenario entity : scenarioEntityList){
			if (entity.getRpaToolId() != null || "".equals(entity.getRpaToolId())){
				scenarioToolMap.put(entity.getScenarioId(), toolMap.get(entity.getRpaToolId()));
			}
		}
		
		return scenarioToolMap;
	}
	
	/**
	 * シナリオ実績作成設定IDとシナリオ識別子が一致する条件で取得したRPAシナリオのうち、
	 * 共通のシナリオ項目がtrueであるRPAシナリオが存在する場合、trueを返します。
	 */
	public Boolean checkCommonNodeScenario(String scenarioId, String scenarioOperationResultCreateSettingId, String scenarioIdentifyString) throws HinemosUnknown, InvalidRole {
		try {
			RpaScenario commonScenario = 
					QueryUtil.getCommonScenario(scenarioOperationResultCreateSettingId, scenarioIdentifyString);
			
			if (scenarioId != null){
				return !commonScenario.getScenarioId().equals(scenarioId);
			} else {
				return true;
			}
		} catch (RpaScenarioNotFound e) {
			return false;
		}
	}
}
