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
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ResourceJobActionEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ResourceJobTypeEnum;

public class JobResourceInfoRequest implements RequestDto {

	/** クラウドスコープID */
	private String resourceCloudScopeId;

	/** ロケーションID */
	private String resourceLocationId;

	/** リソース種別 */
	@RestBeanConvertEnum
	private ResourceJobTypeEnum resourceType;

	/** アクション */
	@RestBeanConvertEnum
	private ResourceJobActionEnum resourceAction;

	/** 対象ID（スコープID、インスタンスID、ストレージID） */
	private String resourceTargetId;

	/** 状態確認期間（秒） */
	private Integer resourceStatusConfirmTime;

	/** 状態確認間隔（秒） */
	private Integer resourceStatusConfirmInterval;

	/** アタッチ先コンピュートノード（リソースID） */
	private String resourceAttachNode;

	/** アタッチ先デバイス */
	private String resourceAttachDevice;

	/** 通知先スコープ（ファシリティID） */
	private String resourceNotifyScope;

	/** 通知先スコープパス */
	private String resourceNotifyScopePath;

	/** 終了値（成功） */
	private Integer resourceSuccessValue;

	/** 終了値（失敗） */
	private Integer resourceFailureValue;

	public JobResourceInfoRequest() {
	}

	public String getResourceCloudScopeId() {
		return resourceCloudScopeId;
	}

	public void setResourceCloudScopeId(String resourceCloudScopeId) {
		this.resourceCloudScopeId = resourceCloudScopeId;
	}

	public String getResourceLocationId() {
		return resourceLocationId;
	}

	public void setResourceLocationId(String resourceLocationId) {
		this.resourceLocationId = resourceLocationId;
	}

	public ResourceJobTypeEnum getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceJobTypeEnum resourceType) {
		this.resourceType = resourceType;
	}

	public ResourceJobActionEnum getResourceAction() {
		return resourceAction;
	}

	public void setResourceAction(ResourceJobActionEnum resourceAction) {
		this.resourceAction = resourceAction;
	}

	public String getResourceTargetId() {
		return resourceTargetId;
	}

	public void setResourceTargetId(String resourceTargetId) {
		this.resourceTargetId = resourceTargetId;
	}

	public Integer getResourceStatusConfirmTime() {
		return resourceStatusConfirmTime;
	}

	public void setResourceStatusConfirmTime(Integer resourceStatusConfirmTime) {
		this.resourceStatusConfirmTime = resourceStatusConfirmTime;
	}

	public Integer getResourceStatusConfirmInterval() {
		return resourceStatusConfirmInterval;
	}

	public void setResourceStatusConfirmInterval(Integer resourceStatusConfirmInterval) {
		this.resourceStatusConfirmInterval = resourceStatusConfirmInterval;
	}

	public String getResourceAttachNode() {
		return resourceAttachNode;
	}

	public void setResourceAttachNode(String resourceAttachNode) {
		this.resourceAttachNode = resourceAttachNode;
	}

	public String getResourceAttachDevice() {
		return resourceAttachDevice;
	}

	public void setResourceAttachDevice(String resourceAttachDevice) {
		this.resourceAttachDevice = resourceAttachDevice;
	}

	public String getResourceNotifyScope() {
		return resourceNotifyScope;
	}

	public void setResourceNotifyScope(String resourceNotifyScope) {
		this.resourceNotifyScope = resourceNotifyScope;
	}

	public String getResourceNotifyScopePath() {
		return resourceNotifyScopePath;
	}

	public void setResourceNotifyScopePath(String resourceNotifyScopePath) {
		this.resourceNotifyScopePath = resourceNotifyScopePath;
	}

	public Integer getResourceSuccessValue() {
		return resourceSuccessValue;
	}

	public void setResourceSuccessValue(Integer resourceSuccessValue) {
		this.resourceSuccessValue = resourceSuccessValue;
	}

	public Integer getResourceFailureValue() {
		return resourceFailureValue;
	}

	public void setResourceFailureValue(Integer resourceFailureValue) {
		this.resourceFailureValue = resourceFailureValue;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
