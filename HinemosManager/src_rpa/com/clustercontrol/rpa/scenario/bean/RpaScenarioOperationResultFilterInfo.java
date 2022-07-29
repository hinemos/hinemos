/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResult.OperationResultStatus;
import com.clustercontrol.rpa.scenario.model.RpaScenarioTag;
import com.clustercontrol.rpa.session.RpaControllerBean;


/**
 * RPAシナリオ実績ビューのフィルタ設定を格納するクラス
 */
@XmlType(namespace = "http://rpa.ws.clustercontrol.com")
public class RpaScenarioOperationResultFilterInfo implements Serializable {

	private static final long serialVersionUID = -6352391799441935693L;
	
	/** 実行時刻検索開始時刻 */
	private Long startDateFrom;
	
	/** 実行時刻検索終了時刻 */
	private Long startDateTo;
	
	/** シナリオID */
	private String scenarioId;
	
	/** シナリオタグID */
	private List<String> tagIdList;
	
	/** ステータス */
	private List<OperationResultStatus> statusList;
	
	/** ファシリティID */
	private String facilityId;
	
	/** 検索結果取得開始位置 */
	private Integer offset;

	/** 検索結果取得数 */
	private Integer size;

	/** 検索結果最大数取得フラグ */
	private Boolean needCount;

	/** 実行時刻検索開始時刻 */
	public Long getStartDateFrom() {
		return startDateFrom;
	}
	public void setStartDateFrom(Long startDateFrom) {
		this.startDateFrom = startDateFrom;
	}

	/** 実行時刻検索終了時刻 */
	public Long getStartDateTo() {
		return startDateTo;
	}
	public void setStartDateTo(Long startDateTo) {
		this.startDateTo = startDateTo;
	}

	/** シナリオID */
	public String getScenarioId() {
		return scenarioId;
	}
	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}
	
	/** シナリオタグID */
	public List<String> getTagIdList() {
		return tagIdList;
	}
	public void setTagIdList(List<String> tagIdList) {
		this.tagIdList = tagIdList;
	}
	
	/** 子タグを含めた全てのシナリオタグIDを返す */
	public List<String> getAllTagIdList() throws InvalidRole, HinemosUnknown {
		List<String> allChildrenTagIdList = new ArrayList<>();
		allChildrenTagIdList.addAll(tagIdList);
		for (String tagId: tagIdList) {
			List<RpaScenarioTag> childrenTagList = new RpaControllerBean().getChildrenScenarioTagList(tagId);
			List<String> childrenTagIdList = childrenTagList.stream().map(RpaScenarioTag::getTagId).collect(Collectors.toList());
			allChildrenTagIdList.addAll(childrenTagIdList);
		}
		// 重複を削除して返す。
		return allChildrenTagIdList.stream().distinct().collect(Collectors.toList());
	}

	/** ステータス */
	public List<OperationResultStatus> getStatusList() {
		return this.statusList;
	}
	public void setStatusList(List<OperationResultStatus> statusList) {
		this.statusList = statusList;
	}
	
	/**　ファシリティID取得 */
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	
	/** 検索結果取得開始位置 */
	public Integer getOffset() {
		return offset;
	}
	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	
	/** 検索結果取得数 */
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	
	/** 検索結果最大数取得フラグ */
	public Boolean isNeedCount() {
		return needCount;
	}
	public void setNeedCount(Boolean needCount) {
		this.needCount = needCount;
	}

}
