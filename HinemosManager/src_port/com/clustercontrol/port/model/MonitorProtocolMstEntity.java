package com.clustercontrol.port.model;

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
 * The persistent class for the cc_monitor_protocol_mst database table.
 * 
 */
@Entity
@Table(name="cc_monitor_protocol_mst", schema="setting")
@Cacheable(true)
public class MonitorProtocolMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String serviceId;
	private String className;
	private Integer defaultPortNumber;
	private String description;
	private String serviceName;
	private List<PortCheckInfo> monitorPortInfoEntities;

	@Deprecated
	public MonitorProtocolMstEntity() {
	}


	public MonitorProtocolMstEntity(String serviceId) {
		this.setServiceId(serviceId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	@Id
	@Column(name="service_id")
	public String getServiceId() {
		return this.serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}


	@Column(name="class_name")
	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}


	@Column(name="default_port_number")
	public Integer getDefaultPortNumber() {
		return this.defaultPortNumber;
	}

	public void setDefaultPortNumber(Integer defaultPortNumber) {
		this.defaultPortNumber = defaultPortNumber;
	}


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="service_name")
	public String getServiceName() {
		return this.serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}


	//bi-directional many-to-one association to MonitorPortInfoEntity
	@OneToMany(mappedBy="monitorProtocolMstEntity", fetch=FetchType.LAZY)
	public List<PortCheckInfo> getMonitorPortInfoEntities() {
		return this.monitorPortInfoEntities;
	}

	public void setMonitorPortInfoEntities(List<PortCheckInfo> monitorPortInfoEntities) {
		this.monitorPortInfoEntities = monitorPortInfoEntities;
	}

}