/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.dto.RequestDto;

public class QueryCollectStringDataRequest implements RequestDto {

	public QueryCollectStringDataRequest(){
	}
	public enum OperatorEnum {
		AND,OR
	}

	@RestBeanConvertDatetime
	private String from;

	@RestBeanConvertDatetime
	private String to;

	private String monitorId;

	private String facilityId;

	private String keywords;

	private String tag;

	private OperatorEnum operator;

	private Integer offset;

	private Integer size;

	private Boolean needCount;
	
	/**
	 * 時間範囲の開始時間取得。
	 * 
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}
	/**
	 * 時間範囲の開始時間設定。
	 *
	 * @param from the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}
	/**
	 * 時間範囲の終了時間取得。
	 * 
	 * @return the to
	 */
	public String getTo() {
		return to;
	}
	/**
	 * 時間範囲の終了時間設定。
	 * 
	 * @param to the to to set
	 */
	public void setTo(String to) {
		this.to = to;
	}
	/**
	 * 監視ID取得。
	 * 
	 * @return the monitorId
	 */
	public String getMonitorId() {
		return monitorId;
	}
	/**
	 * 監視ID設定。
	 * 
	 * @param monitorId the monitorId to set
	 */
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	/**
	 * ファシリティID取得。
	 * 
	 * @return the facilityId
	 */
	public String getFacilityId() {
		return facilityId;
	}
	/**
	 * ファシリティID設定。
	 * 
	 * @param facilityId the facilityId to set
	 */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	/**
	 * キーワード取得。
	 * 
	 * @return the keywords
	 */
	public String getKeywords() {
		return keywords;
	}
	/**
	 * キーワード設定。
	 * 
	 * キーワードのルールは以下。
	 * 1. 各ワードは、複数指定可。空白区切り。
	 * 2. キー指定検索は、キーとワードを : でつなぐ。
	 * 3. 空白や : をワードに含めるには、' で囲む。
	 * 4. ' をワードに含めるには、'' としてエスケープする。
	 * 5. 前方一致、中間一致、後方一致は、適宜 SQL 準拠の % を指定する。
	 * 
	 * @param keywords the keywords to set
	 */
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	/**
	 * タグ取得。
	 * 
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}
	/**
	 * タグ設定。
	 * 
	 * 指定されたタグごとに集計を行う。
	 * ログ件数監視で使用する。
	 * 
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}
	/**
	 * キーワードの論理演算種別設定
	 * 
	 * @return the operator
	 */
	public OperatorEnum getOperator() {
		return operator;
	}
	/**
	 * キーワードの論理演算種別設定
	 * 
	 * @param operator the operator to set
	 */
	public void setOperator(OperatorEnum operator) {
		this.operator = operator;
	}
	/**
	 * 検索結果取得開始位置取得。
	 * 
	 * @return the offset
	 */
	public Integer getOffset() {
		return offset;
	}
	/**
	 * 検索結果取得開始位置設定。
	 * 
	 * @param offset the offset to set
	 */
	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	/**
	 * 検索結果取得数取得。
	 * 
	 * @return the size
	 */
	public Integer getSize() {
		return size;
	}
	/**
	 * 検索結果取得数設定。
	 * 
	 * @param size the size to set
	 */
	public void setSize(Integer size) {
		this.size = size;
	}
	/**
	 * 検索結果最大数取得フラグ取得。
	 * 
	 * @return the needCount
	 */
	public Boolean isNeedCount() {
		return needCount;
	}
	/**
	 * 検索結果最大数取得フラグ設定。
	 * 
	 * @param needCount the needCount to set
	 */
	public void setNeedCount(Boolean needCount) {
		this.needCount = needCount;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
