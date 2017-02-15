/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

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
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
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