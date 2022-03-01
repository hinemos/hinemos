/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ReferJobSelectTypeEnum;

public class ModifyReferJobRequest extends AbstractModifyJobRequest implements RequestDto {

	/** 参照先ジョブユニットID */
	private String referJobUnitId;

	/** 参照先ジョブID */
	private String referJobId;

	/** 参照ジョブ選択種別 */
	@RestBeanConvertEnum
	private ReferJobSelectTypeEnum referJobSelectType;

	public ModifyReferJobRequest() {
	}

	public String getReferJobUnitId() {
		return referJobUnitId;
	}

	public void setReferJobUnitId(String referJobUnitId) {
		this.referJobUnitId = referJobUnitId;
	}

	public String getReferJobId() {
		return referJobId;
	}

	public void setReferJobId(String referJobId) {
		this.referJobId = referJobId;
	}

	public ReferJobSelectTypeEnum getReferJobSelectType() {
		return referJobSelectType;
	}

	public void setReferJobSelectType(ReferJobSelectTypeEnum referJobSelectType) {
		this.referJobSelectType = referJobSelectType;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
	}

}
