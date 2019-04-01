/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import java.util.Arrays;

import com.clustercontrol.fault.HinemosUnknown;

/**
 * マネージャ内部で使用するイベントの更新用フィルタ条件
 *
 */
public class UpdateEventFilterInternal extends EventFilterInternal<EventBatchConfirmInfo> {
	
	public UpdateEventFilterInternal() {
	}
	
	@Override
	public void setFilter(String facilityId, EventBatchConfirmInfo filter) throws HinemosUnknown {
		setFilterCommon(filter);
		
		// ファシリティID 設定
		setFacilityIdList(getFacilityIds(facilityId, this.getFacilityType()));
	}
	
	private void setFilterCommon(EventBatchConfirmInfo filter) throws HinemosUnknown {
		// GetEventFilterInternalとの共通部分 - START
		// 本来は同一ロジックとすべきだが、EventFilterInfoとEventBatchConfirmInfoで
		// 一部の変数名(OutputFromDate、OutputDateFromなど)が異なるため、
		// 別ロジックとする
		
		//重要度　設定
		if (filter.getPriorityList() != null && filter.getPriorityList().length > 0) {
			setPriorityList(Arrays.asList(filter.getPriorityList()));
		} else {
			setPriorityList(null);
		}
		
		//更新日時（自）　設定
		setOutputFromDate(convFromDate(filter.getOutputFromDate()));
		
		//更新日時（至）　設定
		setOutputToDate(convToDate(filter.getOutputToDate()));

		//出力日時（自）　設定
		setGenerationFromDate(convFromDate(filter.getGenerationFromDate()));

		//出力日時（至）　設定
		setGenerationToDate(convToDate(filter.getGenerationToDate()));

		//監視項目ID　設定
		setMonitorId(convEmptyToNull(filter.getMonitorId()));
		
		//監視詳細　設定
		setMonitorDetailId(convEmptyToNull(filter.getMonitorDetailId()));
		
		//対象ファシリティ種別　設定
		setFacilityType(filter.getFacilityType());
		
		//アプリケーション　設定
		setApplication(convEmptyToNull(filter.getApplication()));
		
		//メッセージ　設定
		setMessage(convEmptyToNull(filter.getMessage()));
		
		// コメント　設定
		setComment(convEmptyToNull(filter.getComment()));
		// コメントユーザ 設定
		setCommentUser(convEmptyToNull(filter.getCommentUser()));
		
		// 性能グラフ用フラグ　設定
		setCollectGraphFlg(filter.getCollectGraphFlg());
		
		// UpdateEventFilterInternalとの共通部分 - END
	}
	
	@Override
	public int hashCode() {
		int result = super.hashCode();
		
		return result;
	}
	
	@Override
	public boolean equals(Object obj) { 
		return super.equals(obj);
	}
}
