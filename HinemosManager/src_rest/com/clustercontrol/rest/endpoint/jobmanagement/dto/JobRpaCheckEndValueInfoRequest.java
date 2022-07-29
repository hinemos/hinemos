/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.RpaJobCheckEndValueInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.dto.RequestDto;

/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Infoクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
@RestBeanConvertAssertion(to = RpaJobCheckEndValueInfo.class)
public class JobRpaCheckEndValueInfoRequest implements RequestDto {
	/** RPA管理ツール 終了状態ID */
	private Integer endStatusId;
	/** 終了値 */
	private Integer endValue;

	public JobRpaCheckEndValueInfoRequest() {
	}

	/**
	 * @return the endStatusId
	 */
	public Integer getEndStatusId() {
		return endStatusId;
	}

	/**
	 * @param endStatusId
	 *            the endStatusId to set
	 */
	public void setEndStatusId(Integer endStatusId) {
		this.endStatusId = endStatusId;
	}

	/**
	 * @return the endValue
	 */
	public Integer getEndValue() {
		return endValue;
	}

	/**
	 * @param endValue
	 *            the endValue to set
	 */
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
