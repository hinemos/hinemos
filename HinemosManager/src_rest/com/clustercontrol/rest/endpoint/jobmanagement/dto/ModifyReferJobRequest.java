/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;
import java.util.Collections;
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
		//ReferjobではendStatusは不要のため、あらかじめダミーデータを登録する
		JobEndStatusInfoRequest request1 = new JobEndStatusInfoRequest();
		JobEndStatusInfoRequest request2 = new JobEndStatusInfoRequest();
		JobEndStatusInfoRequest request3 = new JobEndStatusInfoRequest();
		
		ArrayList<JobEndStatusInfoRequest> referEndStatus = new ArrayList<JobEndStatusInfoRequest>();
		Collections.addAll(referEndStatus, request1,request2,request3);
		
		super.setEndStatus(referEndStatus);
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

	//ダミーデータを登録しているため、ここでは何もしない
	@Override
	public void setEndStatus(ArrayList<JobEndStatusInfoRequest> endStatus) {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
	}

}
