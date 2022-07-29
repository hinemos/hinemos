/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ConditionTypeEnum;
import com.clustercontrol.util.MessageConstant;

/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Infoクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
public class JobObjectGroupInfoRequest implements RequestDto {

	/** AND/OR */
	@RestItemName(value = MessageConstant.CONDITION_TYPE)
	@RestValidateObject(notNull = true)
	@RestBeanConvertEnum
	private ConditionTypeEnum conditionType = ConditionTypeEnum.AND;
	
	/** 待ち条件 */
	private ArrayList<JobObjectInfoRequest> jobObjectList;

	public JobObjectGroupInfoRequest() {
	}

	public ConditionTypeEnum getConditionType() {
		return conditionType;
	}

	public void setConditionType(ConditionTypeEnum conditionType) {
		this.conditionType = conditionType;
	}

	public ArrayList<JobObjectInfoRequest> getJobObjectList() {
		return jobObjectList;
	}

	public void setJobObjectList(ArrayList<JobObjectInfoRequest> jobObjectList) {
		this.jobObjectList = jobObjectList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		if (jobObjectList != null) {
			for (JobObjectInfoRequest req : jobObjectList) {
				req.correlationCheck();
			}
		}
	}
}
