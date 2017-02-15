package com.clustercontrol.port.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;


/**
 * The persistent class for the cc_monitor_port_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_port_info", schema="setting")
@Cacheable(true)
public class PortCheckInfo extends MonitorCheckInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer portNumber;
	private Integer runCount;
	private Integer runInterval;
	private Integer timeout;
	private MonitorInfo monitorInfo;
	private MonitorProtocolMstEntity monitorProtocolMstEntity;
	
	private String serviceId;

	public PortCheckInfo() {
	}

	@Column(name="port_number")
	public Integer getPortNo() {
		return this.portNumber;
	}

	public void setPortNo(Integer portNumber) {
		this.portNumber = portNumber;
	}


	@Column(name="run_count")
	public Integer getRunCount() {
		return this.runCount;
	}

	public void setRunCount(Integer runCount) {
		this.runCount = runCount;
	}


	@Column(name="run_interval")
	public Integer getRunInterval() {
		return this.runInterval;
	}

	public void setRunInterval(Integer runInterval) {
		this.runInterval = runInterval;
	}


	public Integer getTimeout() {
		return this.timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}


	//bi-directional one-to-one association to MonitorInfo
	@XmlTransient
	@OneToOne(fetch=FetchType.LAZY, optional=false)
	@PrimaryKeyJoinColumn(name="monitor_id")
	public MonitorInfo getMonitorInfo() {
		return this.monitorInfo;
	}

	@Deprecated
	public void setMonitorInfo(MonitorInfo monitorInfo) {
		this.monitorInfo = monitorInfo;
	}

	/**
	 * MonitorInfoオブジェクト参照設定<BR>
	 * 
	 * MonitorInfo設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToMonitorInfo(MonitorInfo monitorInfo) {
		this.setMonitorInfo(monitorInfo);
		if (monitorInfo != null) {
			monitorInfo.setPortCheckInfo(this);
		}
	}

	@Transient
	public String getServiceId() {
		if (serviceId == null && this.monitorProtocolMstEntity != null)
			serviceId = this.monitorProtocolMstEntity.getServiceId();
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	//bi-directional many-to-one association to MonitorProtocolMstEntity
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="service_id")
	public MonitorProtocolMstEntity getMonitorProtocolMstEntity() {
		return this.monitorProtocolMstEntity;
	}

	@Deprecated
	public void setMonitorProtocolMstEntity(MonitorProtocolMstEntity monitorProtocolMstEntity) {
		this.monitorProtocolMstEntity = monitorProtocolMstEntity;
	}

	/**
	 * MonitorProtocolMstEntityオブジェクト参照設定<BR>
	 * 
	 * MonitorProtocolMstEntity設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToMonitorProtocolMstEntity(MonitorProtocolMstEntity monitorProtocolMstEntity) {
		this.setMonitorProtocolMstEntity(monitorProtocolMstEntity);
		if (monitorProtocolMstEntity != null) {
			List<PortCheckInfo> list = monitorProtocolMstEntity.getMonitorPortInfoEntities();
			if (list == null) {
				list = new ArrayList<PortCheckInfo>();
			} else {
				for(PortCheckInfo entity : list){
					if (entity.getMonitorId().equals(getMonitorId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorProtocolMstEntity.setMonitorPortInfoEntities(list);
		}
	}

	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// MonitorInfo
		if (this.monitorInfo != null) {
			this.monitorInfo.setPortCheckInfo(null);
		}

		// MonitorProtocolMstEntity
		if (this.monitorProtocolMstEntity != null) {
			List<PortCheckInfo> list = this.monitorProtocolMstEntity.getMonitorPortInfoEntities();
			if (list != null) {
				Iterator<PortCheckInfo> iter = list.iterator();
				while(iter.hasNext()) {
					PortCheckInfo entity = iter.next();
					if (entity.getMonitorId().equals(this.getMonitorId())){
						iter.remove();
						break;
					}
				}
			}
		}

	}

}