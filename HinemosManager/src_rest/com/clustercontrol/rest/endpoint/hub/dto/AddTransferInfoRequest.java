/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.hub.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.hub.model.TransferInfo.DataType;
import com.clustercontrol.hub.model.TransferInfo.TransferType;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.hub.dto.enumtype.TransferIntervalEnum;
import com.clustercontrol.util.MessageConstant;

public class AddTransferInfoRequest implements RequestDto {
	
	@RestItemName(value = MessageConstant.HUB_TRANSFER_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String transferId;

	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;

	@RestItemName(value = MessageConstant.HUB_TRANSFER_DATA_TYPE)
	@RestValidateObject(notNull = true)
	private DataType dataType;

	@RestItemName(value = MessageConstant.HUB_TRANSFER_DEST_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String destTypeId;

	@RestItemName(value = MessageConstant.HUB_TRANSFER_TYPE)
	@RestValidateObject(notNull = true)
	private TransferType transType;
	
	@RestBeanConvertEnum
	@RestItemName(value = MessageConstant.HUB_TRANSFER_INTERVAL)
	private TransferIntervalEnum interval;

	@RestItemName(value = MessageConstant.CALENDAR_ID)
	@RestValidateString(maxLen = 64)
	private String calendarId;
	
	@RestItemName(value = MessageConstant.SETTING_VALID)
	private Boolean validFlg;

	@RestItemName(value = MessageConstant.HUB_TRANSFER_SETTING)
	private List<TransferDestPropRequest> destProps = new ArrayList<>();
	
	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(notNull = true, maxLen = 64)
	private String ownerRoleId;

	public String getTransferId() {
		return transferId;
	}
	public void setTransferId(String transferId) {
		this.transferId = transferId;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public DataType getDataType() {
		return dataType;
	}
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public String getDestTypeId() {
		return destTypeId;
	}
	public void setDestTypeId(String destTypeId) {
		this.destTypeId = destTypeId;
	}

	public TransferType getTransType() {
		return transType;
	}
	public void setTransType(TransferType transType) {
		this.transType = transType;
	}

	public TransferIntervalEnum getInterval() {
		return interval;
	}
	public void setInterval(TransferIntervalEnum interval) {
		this.interval = interval;
	}

	public String getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	public List<TransferDestPropRequest> getDestProps() {
		return destProps;
	}
	public void setDestProps(List<TransferDestPropRequest> destProps) {
		this.destProps = destProps;
	}
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		
		if (transType == TransferType.batch && !TransferIntervalEnum.getBatchValues().contains(interval)) {
			throw new InvalidSetting(MessageConstant.MESSAGE_HUB_TRANSFER_INTERVAL_HOURS_INVALID.getMessage());			
		} else if (transType == TransferType.delay && !TransferIntervalEnum.getDelayValues().contains(interval)) {
			throw new InvalidSetting(MessageConstant.MESSAGE_HUB_TRANSFER_INTERVAL_DAYS_INVALID.getMessage());
		}

		if (destProps != null) {
			for (TransferDestPropRequest destProp : destProps) {
				destProp.correlationCheck();
			}
		}
	}
}
