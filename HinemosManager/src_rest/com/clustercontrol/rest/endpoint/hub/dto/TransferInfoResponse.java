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

import com.clustercontrol.hub.model.TransferInfo.DataType;
import com.clustercontrol.hub.model.TransferInfo.TransferType;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.endpoint.hub.dto.enumtype.TransferIntervalEnum;

public class TransferInfoResponse {
	private String transferId;
	private String description;
	private DataType dataType;
	private String destTypeId;
	private TransferType transType;
	@RestBeanConvertEnum
	private TransferIntervalEnum interval;
	private String calendarId;
	private Boolean validFlg;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	private String regUser;
	private String updateUser;
	
	private List<TransferDestPropResponse> destProps = new ArrayList<>();

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

	public String getRegDate() {
		return regDate;
	}
	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}
	
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	
	public String getRegUser() {
		return regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
	
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public List<TransferDestPropResponse> getDestProps() {
		return destProps;
	}
	public void setDestProps(List<TransferDestPropResponse> destProps) {
		this.destProps = destProps;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

}
