package com.clustercontrol.performance.monitor.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_snmp_value_type_mst database table.
 * 
 */
@Entity
@Table(name="cc_snmp_value_type_mst", schema="setting")
@Cacheable(true)
public class SnmpValueTypeMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String valueType;
	private List<CollectorPollingMstEntity> collectorPollingMstEntities;

	@Deprecated
	public SnmpValueTypeMstEntity() {
	}

	public SnmpValueTypeMstEntity(String valueType) {
		this.setValueType(valueType);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}


	@Id
	@Column(name="value_type")
	public String getValueType() {
		return this.valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}


	//bi-directional many-to-one association to CollectorPollingMstEntity
	@OneToMany(mappedBy="snmpValueTypeMstEntity", fetch=FetchType.LAZY)
	public List<CollectorPollingMstEntity> getCollectorPollingMstEntities() {
		return this.collectorPollingMstEntities;
	}

	public void setCollectorPollingMstEntities(List<CollectorPollingMstEntity> collectorPollingMstEntities) {
		this.collectorPollingMstEntities = collectorPollingMstEntities;
	}

}