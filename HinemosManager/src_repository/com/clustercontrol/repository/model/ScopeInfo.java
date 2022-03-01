/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.repository.bean.FacilityConstant;

@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_scope", schema="setting")
@DiscriminatorValue("0")
public class ScopeInfo extends FacilityInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ScopeInfo() {
		super();
		setFacilityType(FacilityConstant.TYPE_SCOPE);
	}

	public ScopeInfo(String facilityId) {
		super(facilityId);
		setFacilityType(FacilityConstant.TYPE_SCOPE);
	}
}
