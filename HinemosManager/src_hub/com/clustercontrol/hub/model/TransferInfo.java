/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.rest.dto.EnumDto;

/**
 * The persistent class for the cc_hub_transfer database table.
 * 
 */
@Entity
@Table(name="cc_hub_transfer", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.HUB_TRANSFER,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="transfer_id", insertable=false, updatable=false))
public class TransferInfo extends ObjectPrivilegeTargetInfo {
	
	public static enum TransferType implements EnumDto<Integer> {
		realtime,
		batch,
		delay;

		@Override
		public Integer getCode() {
			return this.ordinal();
		}
	}
	
	
	public static enum DataType implements EnumDto<Integer> {
		job,
		event,
		numeric,
		string;

		@Override
		public Integer getCode() {
			return this.ordinal();
		}
	}
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String transferId;
	private String description;
	private DataType dataType;
	private String destTypeId;
	private TransferType transType;
	private Integer interval;
	private String calendarId;
	private Boolean validFlg;
	private Long regDate;
	private Long updateDate;
	private String regUser;
	private String updateUser;
	
	private List<TransferDestProp> destProps = new ArrayList<>();
	
	private TransferInfoPosition position;
	
	public TransferInfo(){
	}
	
	@Id
	@Column(name="transfer_id")
	public String getTransferId() {
		return transferId;
	}
	public void setTransferId(String transferId) {
		this.transferId = transferId;
		setObjectId(this.transferId);
	}
	
	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="data_type")
	public DataType getDataType() {
		return dataType;
	}
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	@Column(name="dest_type_id")
	public String getDestTypeId() {
		return destTypeId;
	}
	public void setDestTypeId(String destTypeId) {
		this.destTypeId = destTypeId;
	}

	@Enumerated(EnumType.ORDINAL)
	@Column(name="trans_type")
	public TransferType getTransType() {
		return transType;
	}
	public void setTransType(TransferType transType) {
		this.transType = transType;
	}

	@Column(name="interval")
	public Integer getInterval() {
		return interval;
	}
	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	@Column(name="calendar_id")
	public String getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	@Column(name="valid_flg")
	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	@Column(name="reg_date")
	public Long getRegDate() {
		return regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}
	
	@Column(name="update_date")
	public Long getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}
	
	@Column(name="reg_user")
	public String getRegUser() {
		return regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
	
	@Column(name="update_user")
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	@OneToMany(mappedBy="logTransfer", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumn(name="transfer_id", referencedColumnName="transfer_id", insertable=false, updatable=false)
	
	@ElementCollection
	@CollectionTable(
		name="cc_hub_transfer_dest_prop", schema="setting",
		joinColumns={@JoinColumn(name="transfer_id", referencedColumnName="transfer_id")}
	)
	public List<TransferDestProp> getDestProps() {
		return destProps;
	}
	public void setDestProps(List<TransferDestProp> destProps) {
		this.destProps = destProps;
	}
	
	@XmlTransient
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="transfer_id", referencedColumnName="transfer_id", insertable=false, updatable=false)
	public TransferInfoPosition getPosition() {
		return position;
	}
	public void setPosition(TransferInfoPosition position) {
		this.position = position;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TransferInfo [transferId=" + transferId + ", description=" + description + ", dataType=" + dataType
				+ ", destTypeId=" + destTypeId + ", transType=" + transType + ", interval=" + interval + ", calendarId="
				+ calendarId + ", validFlg=" + validFlg + ", regDate=" + regDate + ", updateDate=" + updateDate
				+ ", regUser=" + regUser + ", updateUser=" + updateUser + ", destProps=" + destProps + ", position="
				+ position + "]";
	}
}