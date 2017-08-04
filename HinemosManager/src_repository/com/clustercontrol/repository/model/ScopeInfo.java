package com.clustercontrol.repository.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
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
