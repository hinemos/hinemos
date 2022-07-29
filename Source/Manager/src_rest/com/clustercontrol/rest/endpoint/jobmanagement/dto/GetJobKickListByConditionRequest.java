/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class GetJobKickListByConditionRequest implements RequestDto {
	/** ランタイムジョブ変数情報 */
	private JobKickFilterInfoRequest jobKickFilterInfo;

	// 取得されるレコードの上限数
	@RestItemName(value=MessageConstant.SIZE)
	@RestValidateInteger(notNull = false , minVal = 1)
	private Integer size ;

	public GetJobKickListByConditionRequest() {
	}

	public JobKickFilterInfoRequest getJobKickFilterInfo() {
		return jobKickFilterInfo;
	}

	public void setJobKickFilterInfo(JobKickFilterInfoRequest jobKickFilterInfo) {
		this.jobKickFilterInfo = jobKickFilterInfo;
	}


	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
