/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * リソース制御ジョブに関する情報を保持するクラス
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class ResourceJobInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Log m_log = LogFactory.getLog(ResourceJobInfo.class);

	/** クラウドスコープID */
	private String resourceCloudScopeId;

	/** ロケーションID */
	private String resourceLocationId;

	/** リソース種別 */
	private Integer resourceType;

	/** アクション */
	private Integer resourceAction;

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

	public Integer getResourceType() {
		return resourceType;
	}

	public void setResourceType(Integer resourceType) {
		this.resourceType = resourceType;
	}

	public Integer getResourceAction() {
		return resourceAction;
	}

	public void setResourceAction(Integer resourceAction) {
		this.resourceAction = resourceAction;
	}

	public String getResourceTargetId() {
		return resourceTargetId;
	}

	public void setResourceTargetId(String resourceTargetID) {
		this.resourceTargetId = resourceTargetID;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resourceCloudScopeId == null) ? 0 : resourceCloudScopeId.hashCode());
		result = prime * result + ((resourceLocationId == null) ? 0 : resourceLocationId.hashCode());
		result = prime * result + ((resourceType == null) ? 0 : resourceType.hashCode());
		result = prime * result + ((resourceAction == null) ? 0 : resourceAction.hashCode());
		result = prime * result + ((resourceTargetId == null) ? 0 : resourceTargetId.hashCode());
		result = prime * result + ((resourceStatusConfirmTime == null) ? 0 : resourceStatusConfirmTime.hashCode());
		result = prime * result + ((resourceStatusConfirmInterval == null) ? 0 : resourceStatusConfirmInterval.hashCode());
		result = prime * result + ((resourceAttachNode == null) ? 0 : resourceAttachNode.hashCode());
		result = prime * result + ((resourceAttachDevice == null) ? 0 : resourceAttachDevice.hashCode());
		result = prime * result + ((resourceNotifyScope == null) ? 0 : resourceNotifyScope.hashCode());
		result = prime * result + ((resourceNotifyScopePath == null) ? 0 : resourceNotifyScopePath.hashCode());
		result = prime * result + ((resourceSuccessValue == null) ? 0 : resourceSuccessValue.hashCode());
		result = prime * result + ((resourceFailureValue == null) ? 0 : resourceFailureValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ResourceJobInfo)) {
			return false;
		}
		ResourceJobInfo o1 = this;
		ResourceJobInfo o2 = (ResourceJobInfo) o;

		boolean ret = equalsSub(o1.getResourceCloudScopeId(), o2.getResourceCloudScopeId())
				&& equalsSub(o1.getResourceLocationId(), o2.getResourceLocationId())
				&& equalsSub(o1.getResourceType(), o2.getResourceType())
				&& equalsSub(o1.getResourceAction(), o2.getResourceAction())
				&& equalsSub(o1.getResourceTargetId(), o2.getResourceTargetId())
				&& equalsSub(o1.getResourceStatusConfirmTime(), o2.getResourceStatusConfirmTime())
				&& equalsSub(o1.getResourceStatusConfirmInterval(), o2.getResourceStatusConfirmInterval())
				&& equalsSub(o1.getResourceAttachNode(), o2.getResourceAttachNode())
				&& equalsSub(o1.getResourceAttachDevice(), o2.getResourceAttachDevice())
				&& equalsSub(o1.getResourceNotifyScope(), o2.getResourceNotifyScope())
				&& equalsSub(o1.getResourceNotifyScopePath(), o2.getResourceNotifyScopePath())
				&& equalsSub(o1.getResourceSuccessValue(), o2.getResourceSuccessValue())
				&& equalsSub(o1.getResourceFailureValue(), o2.getResourceFailureValue());
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null) {
			return false;
		}
		boolean ret = o1.equals(o2);
		if (!ret) {
			if (m_log.isTraceEnabled()) {
				m_log.trace("equalsSub : " + o1 + "!=" + o2);
			}
		}
		return ret;
	}
}
