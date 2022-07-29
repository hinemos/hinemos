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
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoRequest;

public abstract class AbstractAddJobRequest implements RequestDto {

	/** ジョブID */
	private String id;

	/** 親ジョブID */
	private String parentId;

	/** ジョブ名 */
	private String name;

	/** ジョブ待ち条件情報 */
	@RestValidateObject(notNull= true)
	private JobWaitRuleInfoRequest waitRule;

	/** ジョブ終了状態情報 */
	@RestValidateCollection(notNull = true, minSize = 3, maxSize = 3)
	private ArrayList<JobEndStatusInfoRequest> endStatus = new ArrayList<>();

	/** アイコンID */
	private String iconId;

	/** 説明 */
	private String description;

	/** モジュール登録済フラグ */
	private Boolean registered;

	// ジョブ通知関連
	@RestValidateObject(notNull= true)
	@RestBeanConvertEnum
	private PrioritySelectEnum beginPriority;
	@RestValidateObject(notNull= true)
	@RestBeanConvertEnum
	private PrioritySelectEnum normalPriority;
	@RestValidateObject(notNull= true)
	@RestBeanConvertEnum
	private PrioritySelectEnum warnPriority;
	@RestValidateObject(notNull= true)
	@RestBeanConvertEnum
	private PrioritySelectEnum abnormalPriority;
	/** 通知ID **/
	private ArrayList<NotifyRelationInfoRequest> notifyRelationInfos = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JobWaitRuleInfoRequest getWaitRule() {
		return waitRule;
	}

	public void setWaitRule(JobWaitRuleInfoRequest waitRule) {
		this.waitRule = waitRule;
	}

	public ArrayList<JobEndStatusInfoRequest> getEndStatus() {
		return endStatus;
	}

	public void setEndStatus(ArrayList<JobEndStatusInfoRequest> endStatus) {
		this.endStatus = endStatus;
	}

	public String getIconId() {
		return iconId;
	}

	public void setIconId(String iconId) {
		this.iconId = iconId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getRegistered() {
		return registered;
	}

	public void setRegistered(Boolean registered) {
		this.registered = registered;
	}

	public PrioritySelectEnum getBeginPriority() {
		return beginPriority;
	}

	public void setBeginPriority(PrioritySelectEnum beginPriority) {
		this.beginPriority = beginPriority;
	}

	public PrioritySelectEnum getNormalPriority() {
		return normalPriority;
	}

	public void setNormalPriority(PrioritySelectEnum normalPriority) {
		this.normalPriority = normalPriority;
	}

	public PrioritySelectEnum getWarnPriority() {
		return warnPriority;
	}

	public void setWarnPriority(PrioritySelectEnum warnPriority) {
		this.warnPriority = warnPriority;
	}

	public PrioritySelectEnum getAbnormalPriority() {
		return abnormalPriority;
	}

	public void setAbnormalPriority(PrioritySelectEnum abnormalPriority) {
		this.abnormalPriority = abnormalPriority;
	}

	public ArrayList<NotifyRelationInfoRequest> getNotifyRelationInfos() {
		return notifyRelationInfos;
	}

	public void setNotifyRelationInfos(ArrayList<NotifyRelationInfoRequest> notifyRelationInfos) {
		this.notifyRelationInfos = notifyRelationInfos;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		waitRule.correlationCheck();
		for (JobEndStatusInfoRequest req : endStatus) {
			req.correlationCheck();
		}
		if (notifyRelationInfos != null) {
			for (NotifyRelationInfoRequest req : notifyRelationInfos) {
				req.correlationCheck();
			}
		}
	}
}
