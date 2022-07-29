/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The persistent class for the cc_hub_transfer_position database table.
 * 
 */
@Entity
@Table(name="cc_hub_transfer_position", schema="setting")
@Cacheable(true)
public class TransferInfoPosition implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String transferId;
	private Long lastPosition;
	private Long lastDate;
	
	@Deprecated
	public TransferInfoPosition(){
	}
	
	public TransferInfoPosition(String transferId) {
		this.setTransferId(transferId);
	}
	
	@Id
	@Column(name="transfer_id")
	public String getTransferId() {
		return transferId;
	}
	public void setTransferId(String transferId) {
		this.transferId = transferId;
	}
	
	@Column(name="last_position")
	public Long getLastPosition() {
		return lastPosition;
	}
	public void setLastPosition(Long lastPosition) {
		this.lastPosition = lastPosition;
	}
	
	@Column(name="last_date")
	public Long getLastDate() {
		return lastDate;
	}
	public void setLastDate(Long lastDate) {
		this.lastDate = lastDate;
	}
}