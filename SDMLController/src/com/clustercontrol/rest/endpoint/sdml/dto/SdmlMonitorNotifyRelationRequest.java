/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.dto;

import java.util.List;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoRequest;
import com.clustercontrol.sdml.model.SdmlMonitorNotifyRelation;
import com.clustercontrol.sdml.util.SdmlUtil;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertIdClassSet(infoClass = SdmlMonitorNotifyRelation.class, idName = "id")
public class SdmlMonitorNotifyRelationRequest implements RequestDto {

	@RestItemName(value = MessageConstant.SDML_MONITOR_TYPE)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64)
	private String sdmlMonitorTypeId;

	private List<NotifyRelationInfoRequest> notifyRelationList;

	public SdmlMonitorNotifyRelationRequest() {
	}

	public String getSdmlMonitorTypeId() {
		return sdmlMonitorTypeId;
	}

	public void setSdmlMonitorTypeId(String sdmlMonitorTypeId) {
		this.sdmlMonitorTypeId = sdmlMonitorTypeId;
	}

	public List<NotifyRelationInfoRequest> getNotifyRelationList() {
		return notifyRelationList;
	}

	public void setNotifyRelationList(List<NotifyRelationInfoRequest> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	// ownerRoleIdが必要なため親から呼び出すときはこちらを使用すること
	public void correlationCheck(String ownerRoleId) throws InvalidSetting {
		// sdmlMonitorTypeId
		if (!SdmlUtil.isValidSdmlMonitorTypeId(sdmlMonitorTypeId)) {
			InvalidSetting e = new InvalidSetting("SdmlMonitorTypeId(" + sdmlMonitorTypeId + ") is invalid");
			throw e;
		}

		// notifyId
		if (getNotifyRelationList() != null && getNotifyRelationList().size() > 0) {
			for (NotifyRelationInfoRequest notifyRelation : getNotifyRelationList()) {
				try {
					CommonValidator.validateNotifyId(notifyRelation.getNotifyId(), true, ownerRoleId);
				} catch (Exception e) {
					throw new InvalidSetting(e.getMessage(), e);
				}
			}
		}
	}
}
