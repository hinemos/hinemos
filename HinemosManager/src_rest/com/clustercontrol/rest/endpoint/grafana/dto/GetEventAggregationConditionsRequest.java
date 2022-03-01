/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.grafana.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.filtersetting.bean.FilterSettingConstant;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.grafana.dto.Utils;
import com.clustercontrol.rest.endpoint.monitorresult.dto.enumtype.ConfirmFlgTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;
import com.clustercontrol.util.MessageConstant;

public class GetEventAggregationConditionsRequest implements RequestDto {

	public GetEventAggregationConditionsRequest() {
	}

	@RestItemName(value = MessageConstant.DESCRIPTION)
	private String description;
	
	@RestItemName(value = MessageConstant.EVENT_AGGREGATION_NEGATIVE)
	private boolean negative;
	
	@RestItemName(value = MessageConstant.PRIORITY_CRITICAL)
	private boolean priorityCritical = Boolean.TRUE;
	
	@RestItemName(value = MessageConstant.PRIORITY_WARNING)
	private boolean priorityWarning = Boolean.TRUE;
	
	@RestItemName(value = MessageConstant.PRIORITY_INFO)
	private boolean priorityInfo = Boolean.TRUE;
	
	@RestItemName(value = MessageConstant.PRIORITY_UNKNOWN)
	private boolean priorityUnknown = Boolean.TRUE;
	
	@RestItemName(value = MessageConstant.GENERATION_DATE_FROM)
	private String generationDateFrom;
	
	@RestItemName(value = MessageConstant.GENERATION_DATE_TO)
	private String generationDateTo;
	
	@RestItemName(value = MessageConstant.OUTPUT_DATE_FROM)
	private String outputDateFrom;
	
	@RestItemName(value = MessageConstant.OUTPUT_DATE_TO)
	private String outputDateTo;
	
	@RestItemName(value = MessageConstant.MONITOR_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String monitorId;
	
	@RestItemName(value = MessageConstant.MONITOR_DETAIL_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String monitorDetail;
	
	@RestItemName(value = MessageConstant.APPLICATION)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String application;
	
	@RestItemName(value = MessageConstant.MESSAGE)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String message;
	
	@RestItemName(value = MessageConstant.TYPE_UNCONFIRMED)
	private boolean confirmYet = Boolean.TRUE;
	
	@RestItemName(value = MessageConstant.TYPE_CONFIRMING)
	private boolean confirmDoing = Boolean.TRUE;
	
	@RestItemName(value = MessageConstant.TYPE_CONFIRMED)
	private boolean confirmDone = Boolean.TRUE;
	
	@RestItemName(value = MessageConstant.CONFIRM_USER)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String confirmUser;
	
	@RestItemName(value = MessageConstant.COMMENT)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String comment;
	
	@RestItemName(value = MessageConstant.COMMENT_USER)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String commentUser;
	
	@RestItemName(value = MessageConstant.COLLECT_GRAPH_FLG)
	private boolean graphFlag;
	
	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String ownerRoleId;
	
	@RestItemName(value = MessageConstant.EVENT_USER_ITEM)
	private Map<String, String> userItems;
	
	@RestItemName(value = MessageConstant.EVENT_POSITION_FROM)
	private int positionFrom;
	
	@RestItemName(value = MessageConstant.EVENT_POSITION_TO)
	private int positionTo;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isNegative() {
		return negative;
	}

	public void setNegative(boolean negative) {
		this.negative = negative;
	}

	public boolean isPriorityCritical() {
		return priorityCritical;
	}

	public void setPriorityCritical(boolean priorityCritical) {
		this.priorityCritical = priorityCritical;
	}

	public boolean isPriorityWarning() {
		return priorityWarning;
	}

	public void setPriorityWarning(boolean priorityWarning) {
		this.priorityWarning = priorityWarning;
	}

	public boolean isPriorityInfo() {
		return priorityInfo;
	}

	public void setPriorityInfo(boolean priorityInfo) {
		this.priorityInfo = priorityInfo;
	}

	public boolean isPriorityUnknown() {
		return priorityUnknown;
	}

	public void setPriorityUnknown(boolean priorityUnknown) {
		this.priorityUnknown = priorityUnknown;
	}

	public String getGenerationDateFrom() {
		return generationDateFrom;
	}

	public void setGenerationDateFrom(String generationDateFrom) {
		this.generationDateFrom = generationDateFrom;
	}

	public String getGenerationDateTo() {
		return generationDateTo;
	}

	public void setGenerationDateTo(String generationDateTo) {
		this.generationDateTo = generationDateTo;
	}

	public String getOutputDateFrom() {
		return outputDateFrom;
	}

	public void setOutputDateFrom(String outputDateFrom) {
		this.outputDateFrom = outputDateFrom;
	}

	public String getOutputDateTo() {
		return outputDateTo;
	}

	public void setOutputDateTo(String outputDateTo) {
		this.outputDateTo = outputDateTo;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMonitorDetail() {
		return monitorDetail;
	}

	public void setMonitorDetail(String monitorDetail) {
		this.monitorDetail = monitorDetail;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isConfirmYet() {
		return confirmYet;
	}

	public void setConfirmYet(boolean confirmYet) {
		this.confirmYet = confirmYet;
	}

	public boolean isConfirmDoing() {
		return confirmDoing;
	}

	public void setConfirmDoing(boolean confirmDoing) {
		this.confirmDoing = confirmDoing;
	}

	public boolean isConfirmDone() {
		return confirmDone;
	}

	public void setConfirmDone(boolean confirmDone) {
		this.confirmDone = confirmDone;
	}

	public String getConfirmUser() {
		return confirmUser;
	}

	public void setConfirmUser(String confirmUser) {
		this.confirmUser = confirmUser;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCommentUser() {
		return commentUser;
	}

	public void setCommentUser(String commentUser) {
		this.commentUser = commentUser;
	}

	public boolean isGraphFlag() {
		return graphFlag;
	}

	public void setGraphFlag(boolean graphFlag) {
		this.graphFlag = graphFlag;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public Map<String, String> getUserItems() {
		return userItems;
	}

	public void setUserItems(Map<String, String> userItems) {
		this.userItems = userItems;
	}

	public int getPositionFrom() {
		return positionFrom;
	}

	public void setPositionFrom(int positionFrom) {
		this.positionFrom = positionFrom;
	}

	public int getPositionTo() {
		return positionTo;
	}

	public void setPositionTo(int positionTo) {
		this.positionTo = positionTo;
	}

	/**
	 * true になっている確認フラグのコードリストを返します。
	 */
	public List<Integer> getConfirmFlagCodes() {
		List<Integer> confirmFlgTypeList = new ArrayList<Integer>();
		if (Boolean.TRUE.equals(confirmYet)) {
			confirmFlgTypeList.add(ConfirmFlgTypeEnum.TYPE_UNCONFIRMED.getCode());
		}
		if (Boolean.TRUE.equals(confirmDoing)) {
			confirmFlgTypeList.add(ConfirmFlgTypeEnum.TYPE_CONFIRMING.getCode());
		}
		if (Boolean.TRUE.equals(confirmDone)) {
			confirmFlgTypeList.add(ConfirmFlgTypeEnum.TYPE_CONFIRMED.getCode());
		}
		return confirmFlgTypeList;
	}

	/**
	 * true になっている重要度のコードリストを返します。
	 */
	public List<Integer> getPriorityCodes() {
		List<Integer> priorityList = new ArrayList<Integer>();
		if (Boolean.TRUE.equals(priorityInfo)) {
			priorityList.add(PriorityEnum.INFO.getCode());
		}
		if (Boolean.TRUE.equals(priorityWarning)) {
			priorityList.add(PriorityEnum.WARNING.getCode());
		}
		if (Boolean.TRUE.equals(priorityCritical)) {
			priorityList.add(PriorityEnum.CRITICAL.getCode());
		}
		if (Boolean.TRUE.equals(priorityUnknown)) {
			priorityList.add(PriorityEnum.UNKNOWN.getCode());
		}
		return priorityList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// AND結合数
		Utils.validateLAndConjunction(MessageConstant.MONITOR_ID, monitorId);
		Utils.validateLAndConjunction(MessageConstant.MONITOR_DETAIL_ID, monitorDetail);
		Utils.validateLAndConjunction(MessageConstant.APPLICATION, application);
		Utils.validateLAndConjunction(MessageConstant.MESSAGE, message);
		Utils.validateLAndConjunction(MessageConstant.CONFIRM_USER, confirmUser);
		Utils.validateLAndConjunction(MessageConstant.COMMENT, comment);
		Utils.validateLAndConjunction(MessageConstant.COMMENT_USER, commentUser);
		Utils.validateLAndConjunction(MessageConstant.OWNER_ROLE_ID, ownerRoleId);
	}

}
