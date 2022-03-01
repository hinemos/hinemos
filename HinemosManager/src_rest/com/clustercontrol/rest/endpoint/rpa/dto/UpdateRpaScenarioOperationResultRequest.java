/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.rpa.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoRequest;
import com.clustercontrol.util.MessageConstant;

public class UpdateRpaScenarioOperationResultRequest implements RequestDto {

	/** シナリオ実績作成設定ID */
	@RestValidateString(notNull=true, minLen=1, maxLen=64)
	@RestItemName(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID)
	private String scenarioOperationResultCreateSettingId;

	/** シナリオ識別文字列 */
	@RestValidateString(notNull=true, minLen=1, maxLen=512)
	@RestItemName(MessageConstant.RPA_SCENARIO_SCENARIO_IDENTIFY_STRING)
	private String scenarioIdentifyString;

	/** 対象日時(From) */
	@RestValidateString(notNull=true, minLen=1)
	@RestItemName(MessageConstant.TARGET_PERIOD_FROM)
	@RestBeanConvertDatetime
	private String fromDate;

	/** 対象日時(To) */
	@RestValidateString(notNull=true, minLen=1)
	@RestItemName(MessageConstant.TARGET_PERIOD_TO)
	@RestBeanConvertDatetime
	private String toDate;

	/** 通知ID */
	@RestItemName(value = MessageConstant.NOTIFY_ID)
	private List<NotifyRelationInfoRequest> notifyId;

	/** アプリケーション */
	@RestItemName(MessageConstant.APPLICATION)
	@RestValidateString(minLen=0, maxLen=64)
	private String application;

	/** シナリオ実績作成設定ID */
	public String getScenarioOperationResultCreateSettingId() {
		return this.scenarioOperationResultCreateSettingId;
	}

	public void setScenarioOperationResultCreateSettingId(String scenarioOperationResultCreateSettingId) {
		this.scenarioOperationResultCreateSettingId = scenarioOperationResultCreateSettingId;
	}

	/** シナリオ識別文字列 */
	public String getScenarioIdentifyString() {
		return scenarioIdentifyString;
	}
	
	public void setScenarioIdentifyString(String scenarioIdentifyString) {
		this.scenarioIdentifyString = scenarioIdentifyString;
	}
	
	/** 対象日時(From) */
	public String getFromDate() {
		return fromDate;
	}
	
	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}
	
	/** 対象日時(To) */
	public String getToDate() {
		return toDate;
	}
	
	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
	

	/** 通知ID　*/
	public List<NotifyRelationInfoRequest> getNotifyId() {
		return notifyId;
	}
	public void setNotifyId(List<NotifyRelationInfoRequest> notifyId) {
		this.notifyId = notifyId;
	}

	/** アプリケーション */
	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}


	@Override
	public void correlationCheck() throws InvalidSetting {

	}

}
