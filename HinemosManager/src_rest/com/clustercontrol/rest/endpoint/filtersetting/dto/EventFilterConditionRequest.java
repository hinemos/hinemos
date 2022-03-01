/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.filtersetting.bean.EventFilterConditionInfo;
import com.clustercontrol.filtersetting.bean.FilterSettingConstant;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.validation.RestValidateLong;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertAssertion(to = EventFilterConditionInfo.class)
public class EventFilterConditionRequest implements RequestDto {

	private static final Pattern userItemKeyPattern = Pattern.compile("0*(\\d{1,5})"); // "5"は適当(Integerに収まる桁数で)

	@RestItemName(MessageConstant.FILTER_COND_DESCRIPTION)
	@RestValidateString(maxLen = FilterSettingConstant.CONDITION_DESC_LEN_MAX)
	private String description;

	@RestItemName(MessageConstant.FILTER_COND_NEGATIVE)
	private Boolean negative;

	@RestItemName(MessageConstant.PRIORITY_CRITICAL)
	private Boolean priorityCritical;

	@RestItemName(MessageConstant.PRIORITY_WARNING)
	private Boolean priorityWarning;

	@RestItemName(MessageConstant.PRIORITY_INFO)
	private Boolean priorityInfo;

	@RestItemName(MessageConstant.PRIORITY_UNKNOWN)
	private Boolean priorityUnknown;

	@RestItemName(MessageConstant.GENERATION_DATE_FROM)
	@RestBeanConvertDatetime
	private String generationDateFrom;

	@RestItemName(MessageConstant.GENERATION_DATE_TO)
	@RestBeanConvertDatetime
	private String generationDateTo;

	@RestItemName(MessageConstant.OUTPUT_DATE_FROM)
	@RestBeanConvertDatetime
	private String outputDateFrom;

	@RestItemName(MessageConstant.OUTPUT_DATE_TO)
	@RestBeanConvertDatetime
	private String outputDateTo;

	@RestItemName(MessageConstant.MONITOR_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String monitorId;

	@RestItemName(MessageConstant.MONITOR_DETAIL_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String monitorDetail;

	@RestItemName(MessageConstant.APPLICATION)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String application;

	@RestItemName(MessageConstant.MESSAGE)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String message;

	@RestItemName(MessageConstant.TYPE_UNCONFIRMED)
	private Boolean confirmYet;

	@RestItemName(MessageConstant.TYPE_CONFIRMING)
	private Boolean confirmDoing;

	@RestItemName(MessageConstant.TYPE_CONFIRMED)
	private Boolean confirmDone;

	@RestItemName(MessageConstant.CONFIRM_USER)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String confirmUser;

	@RestItemName(MessageConstant.COMMENT)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String comment;

	@RestItemName(MessageConstant.COMMENT_USER)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String commentUser;

	@RestItemName(MessageConstant.COLLECT_GRAPH_FLG)
	private Boolean graphFlag;

	@RestItemName(MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(maxLen = FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String ownerRoleId;

	@RestItemName(MessageConstant.NOTIFY_UUID)
	@RestValidateString(maxLen=FilterSettingConstant.ITEM_VALUE_LEN_MAX)
	private String notifyUUID;

	@RestItemName(MessageConstant.EVENT_USER_ITEM)
	private Map<String, String> userItems;

	@RestItemName(MessageConstant.EVENT_POSITION_FROM)
	@RestValidateLong(minVal = 0, maxVal = Long.MAX_VALUE)
	private Long positionFrom;

	@RestItemName(MessageConstant.EVENT_POSITION_TO)
	@RestValidateLong(minVal = 0, maxVal = Long.MAX_VALUE)
	private Long positionTo;

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

		Utils.validateLAndConjunction(MessageConstant.NOTIFY_UUID, notifyUUID);
		// userItems
		Map<String, String> normalized = new HashMap<>();
		if (userItems != null) {
			for (Entry<String, String> entry : userItems.entrySet()) {
				// key：0-leadingな10進数文字列(1～40の範囲)
				String key = entry.getKey();
				Matcher mt = userItemKeyPattern.matcher(key);
				if (!mt.matches()) {
					throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_RANGE.getMessage(
							MessageConstant.EVENT_USER_ITEM_NUMBER.getMessage(),
							"1",
							String.valueOf(EventHinemosPropertyConstant.USER_ITEM_SIZE)));
				}
				int num = Integer.parseInt(mt.group(1)); // パターン判定で保証済みのため例外は発生しない
				CommonValidator.validateInt(MessageConstant.EVENT_USER_ITEM_NUMBER.getMessage(),
						num, 1, EventHinemosPropertyConstant.USER_ITEM_SIZE);

				// value
				String numStr = String.valueOf(num);
				String value = entry.getValue();
				CommonValidator.validateString(MessageConstant.EVENT_USER_ITEM_X.getMessage(numStr),
						value, false, 0, FilterSettingConstant.ITEM_VALUE_LEN_MAX);
				Utils.validateLAndConjunction(MessageConstant.EVENT_USER_ITEM_X.getMessage(numStr), value);

				// キーをノーマライズした新しいMapを作成する
				normalized.put(String.valueOf(num), value);
			}
		}
		userItems = normalized;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getNegative() {
		return negative;
	}

	public void setNegative(Boolean negative) {
		this.negative = negative;
	}

	public Boolean getPriorityCritical() {
		return priorityCritical;
	}

	public void setPriorityCritical(Boolean priorityCritical) {
		this.priorityCritical = priorityCritical;
	}

	public Boolean getPriorityWarning() {
		return priorityWarning;
	}

	public void setPriorityWarning(Boolean priorityWarning) {
		this.priorityWarning = priorityWarning;
	}

	public Boolean getPriorityInfo() {
		return priorityInfo;
	}

	public void setPriorityInfo(Boolean priorityInfo) {
		this.priorityInfo = priorityInfo;
	}

	public Boolean getPriorityUnknown() {
		return priorityUnknown;
	}

	public void setPriorityUnknown(Boolean priorityUnknown) {
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

	public Boolean getConfirmYet() {
		return confirmYet;
	}

	public void setConfirmYet(Boolean confirmYet) {
		this.confirmYet = confirmYet;
	}

	public Boolean getConfirmDoing() {
		return confirmDoing;
	}

	public void setConfirmDoing(Boolean confirmDoing) {
		this.confirmDoing = confirmDoing;
	}

	public Boolean getConfirmDone() {
		return confirmDone;
	}

	public void setConfirmDone(Boolean confirmDone) {
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

	public Boolean getGraphFlag() {
		return graphFlag;
	}

	public void setGraphFlag(Boolean graphFlag) {
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

	public Long getPositionFrom() {
		return positionFrom;
	}

	public void setPositionFrom(Long positionFrom) {
		this.positionFrom = positionFrom;
	}

	public Long getPositionTo() {
		return positionTo;
	}

	public void setPositionTo(Long positionTo) {
		this.positionTo = positionTo;
	}

	public String getNotifyUUID() {
		return notifyUUID;
	}

	public void setNotifyUUID(String notifyUUID) {
		this.notifyUUID = notifyUUID;
	}
}
