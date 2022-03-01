/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.RpaScenarioOperationResultNotFound;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rpa.scenario.bean.RpaScenarioOperationResultFilterInfo;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResult;
import com.clustercontrol.rpa.util.QueryUtil;
import com.clustercontrol.util.MessageConstant;

/**
 * RPAシナリオ情報を検索するクラス
 */
public class SelectRpaScenarioOperationResult {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectRpaScenarioOperationResult.class );

	/**
	 * 指定したフィルタにマッチするRPAシナリオ実績情報一覧を返します。
	 */
	public ArrayList<RpaScenarioOperationResult> getRpaScenarioOperationResultList(RpaScenarioOperationResultFilterInfo condition, List<String> facilityIds) {
		m_log.debug("getRpaScenarioOperationResultList() condition ");
		if(m_log.isDebugEnabled()){
			if(condition != null){
				m_log.debug("getRpaScenarioOperatinResultList() " +
						"startDateFrom = " + condition.getStartDateFrom() +
						", startDateTo = " + condition.getStartDateTo() +
						", scenarioId = " + condition.getScenarioId() +
						", tagIdList = " + condition.getTagIdList() +
						", status = " + condition.getStatusList());
			}
		}

		ArrayList<RpaScenarioOperationResult> filterList = new ArrayList<RpaScenarioOperationResult>();
		// 条件未設定の場合は空のリストを返却する
		if(condition == null){
			m_log.debug("getRpaScenarioOpeationResultList() condition is null");
			return filterList;
		}

		// RpaScenarioOperationResult情報を取得
		List<RpaScenarioOperationResult> entityList = QueryUtil.getRpaScenarioOperationResultByFilter(
				condition.getStartDateFrom(),
				condition.getStartDateTo(),
				condition.getScenarioId(),
				condition.getTagIdList(),
				condition.getStatusList(),
				facilityIds,
				condition.getOffset(),
				condition.getSize());
		
		for(RpaScenarioOperationResult entity : entityList){
			m_log.debug("getRpaScenarioOperationResultList() add display list : target = " + entity.getResultId());
			filterList.add(entity);
		}
		return filterList;
	}
	
	/**
	 * 指定したフィルタにマッチするRPAシナリオ実績情報の合計数を返します。
	 */
	public Long getRpaScenarioOperationResultCount(RpaScenarioOperationResultFilterInfo condition, List<String> facilityIds) 
			throws HinemosUnknown, InvalidRole {
		m_log.debug("getRpaScenarioOperationResultCount() condition ");
		if(m_log.isDebugEnabled()){
			if(condition != null){
				m_log.debug("getRpaScenarioOperatinResultCount() " +
						"startDateFrom = " + condition.getStartDateFrom() +
						", startDateTo = " + condition.getStartDateTo() +
						", scenarioId = " + condition.getScenarioId() +
						", tagIdList = " + condition.getTagIdList() +
						", statusList = " + condition.getStatusList());
			}
		}

		Long count = (long) 0;
		// 条件未設定の場合は0を返却する
		if(condition == null){
			m_log.debug("getRpaScenarioOpeationResultCount() condition is null");
			return count;
		}

		// RpaScenarioOperationResult情報を取得
		count = QueryUtil.getRpaScenarioOperationResultCountByFilter(
				condition.getStartDateFrom(),
				condition.getStartDateTo(),
				condition.getScenarioId(),
				condition.getTagIdList(),
				condition.getStatusList(),
				facilityIds);
		
		return count;
	}
	
	public List<String> checkFacilityId(String facilityId) throws HinemosUnknown, InvalidSetting{
		List<String> facilityIds = new ArrayList<>();
		if (facilityId != null) {
			if (!FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
				RepositoryControllerBean repositoryCtrl = new RepositoryControllerBean();
				try {
					if (repositoryCtrl.isNode(facilityId)){
						facilityIds.add(facilityId);
					} else {
						List<NodeInfo> nodeinfoList = repositoryCtrl.getNodeList(facilityId, 0);
						if (!nodeinfoList.isEmpty()) {
							for (NodeInfo node: nodeinfoList) {
								facilityIds.add(node.getFacilityId());
							}
						}
					}
				} catch(FacilityNotFound e) {
					m_log.warn("checkFacilityId " + e.getMessage());
					throw new IllegalStateException("preCollect() : can't get NodeInfo. facilityId = " + facilityId);
				}
			} else {
				facilityIds.add(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
			}
		}
		
		if (facilityIds.isEmpty()) {
			// 検索対象となるノードがないので終了。
			m_log.warn("checkFacilityId() : " 
					+ MessageConstant.MESSAGE_HUB_SEARCH_NO_NODE.getMessage());
			throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_NO_NODE.getMessage());
		}
		
		return facilityIds;
	}
	
	/**
	 * RPAシナリオ実績詳細情報を返します。
	 */
	public RpaScenarioOperationResult getRpaScenarioOperationResult(Long resultId) throws RpaScenarioOperationResultNotFound, InvalidRole {
		// RPAシナリオ実績詳細情報を取得
		RpaScenarioOperationResult entity = null;
		entity = QueryUtil.getRpaScenarioOperationResultPK(resultId);
		
		return entity;
	}
	
	/**
	 * RPAシナリオ実績情報の日別シナリオ実施件数をカウントして返します。
	 */
	public List<Object[]> getRpaScenarioOperationResultDailyErrorsCount(Long startDateFrom, Long startDateTo, List<String> facilityIds) 
			throws HinemosUnknown, InvalidRole {

		// RpaScenarioOperationResult情報を取得
		List<Object[]> countList = QueryUtil.getRpaScenarioOperationResultDailyErrorsCount(
				startDateFrom, startDateTo, facilityIds);
		
		return countList;
	}
	
	/**
	 * RPAシナリオ実績情報の時間帯別削減工数をカウントして返します。
	 */
	public List<Object[]> getRpaScenarioOperationResultHourlyReductionCount(Long startDateFrom, Long startDateTo, List<String> facilityIds) 
			throws HinemosUnknown, InvalidRole {

		// RpaScenarioOperationResult情報を取得
		List<Object[]> countList = QueryUtil.getRpaScenarioOperationResultHourlyReductionCount(
				startDateFrom, startDateTo, facilityIds);
		
		return countList;
	}
	
	/**
	 * RPAシナリオ実績情報のシナリオ別エラー数をカウントして返します。
	 */
	public List<Object[]> getRpaScenarioOperationResultScenarioErrorsCount(Long startDateFrom, Long startDateTo, List<String> facilityIds) 
			throws HinemosUnknown, InvalidRole {

		// RpaScenarioOperationResult情報を取得
		List<Object[]> countList = QueryUtil.getRpaScenarioOperationResultsSenarioErrorsCount(
				startDateFrom, startDateTo, facilityIds);
		
		return countList;
	}
	
	/**
	 * RPAシナリオ実績情報のノード別エラー数をカウントして返します。
	 */
	public List<Object[]> getRpaScenarioOperationResultNodeErrorsCount(Long startDateFrom, Long startDateTo, List<String> facilityIds) 
			throws HinemosUnknown, InvalidRole {

		// RpaScenarioOperationResult情報を取得
		List<Object[]> countList = QueryUtil.getRpaScenarioOperationResultsNodeErrorsCount(
				startDateFrom, startDateTo, facilityIds);
		
		return countList;
	}
	
	/**
	 * RPAシナリオ実績情報のシナリオ別削減工数をカウントして返します。
	 */
	public List<Object[]> getRpaScenarioOperationResultScenarioReductionCount(Long startDateFrom, Long startDateTo, List<String> facilityIds) 
			throws HinemosUnknown, InvalidRole {

		// RpaScenarioOperationResult情報を取得
		List<Object[]> countList = QueryUtil.getRpaScenarioOperationResultScenarioReductionCount(
				startDateFrom, startDateTo, facilityIds);
		
		return countList;
	}
	
	/**
	 * RPAシナリオ実績情報のノード別削減工数をカウントして返します。
	 */
	public List<Object[]> getRpaScenarioOperationResultNodeReductionCount(Long startDateFrom, Long startDateTo, List<String> facilityIds) 
			throws HinemosUnknown, InvalidRole {

		// RpaScenarioOperationResult情報を取得
		List<Object[]> countList = QueryUtil.getRpaScenarioOperationResultNodeReductionCount(
				startDateFrom, startDateTo, facilityIds);
		
		return countList;
	}
	
	/**
	 * RPAシナリオ実績情報のエラー数をカウントして返します。
	 */
	public List<Object[]> getRpaScenarioOperationResultErrorsCount(Long startDateFrom, Long startDateTo, List<String> facilityIds) 
			throws HinemosUnknown, InvalidRole {

		// RpaScenarioOperationResult情報を取得
		List<Object[]> countList = QueryUtil.getRpaScenarioOperationResultErrorsCount(
				startDateFrom, startDateTo, facilityIds);
		
		return countList;
	}
	
	/**
	 * RPAシナリオ実績情報の削減工数をカウントして返します。
	 */
	public List<Object[]> getRpaScenarioOperationResultReductionCount(Long startDateFrom, Long startDateTo, List<String> facilityIds) 
			throws HinemosUnknown, InvalidRole {

		// RpaScenarioOperationResult情報を取得
		List<Object[]> countList = QueryUtil.getRpaScenarioOperationResultReductionCount(
				startDateFrom, startDateTo, facilityIds);
		
		return countList;
	}
}
