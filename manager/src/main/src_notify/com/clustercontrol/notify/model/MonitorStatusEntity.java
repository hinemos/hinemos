package com.clustercontrol.notify.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;



/**
 * The persistent class for the cc_monitor_status database table.
 * 
 */
@Entity
@Table(name="cc_monitor_status", schema="setting")
@Cacheable(true)
public class MonitorStatusEntity implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private MonitorStatusEntityPK id;
	private Long counter;
	private Long lastUpdate;
	private Integer priority;

	@Deprecated
	public MonitorStatusEntity() {
	}

	public MonitorStatusEntity(MonitorStatusEntityPK pk) {
		this.setId(pk);
	}

	public MonitorStatusEntity(String facilityId, String pluginId, String monitorId, String subKey) {
		this(new MonitorStatusEntityPK(facilityId, pluginId, monitorId, subKey));
	}


	@EmbeddedId
	public MonitorStatusEntityPK getId() {
		return this.id;
	}

	public void setId(MonitorStatusEntityPK id) {
		this.id = id;
	}


	@Column(name="counter")
	public Long getCounter() {
		return this.counter;
	}

	public void setCounter(Long counter) {
		this.counter = counter;
	}


	@Column(name="last_update")
	public Long getLastUpdate() {
		return this.lastUpdate;
	}

	public void setLastUpdate(Long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}


	@Column(name="priority")
	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String toString() {
		return String.format("%s [id = %s, counter = %d, lastUpdate = %s, priority = %d]", 
				MonitorStatusEntity.class.getSimpleName(), id, counter, lastUpdate, priority);
	};
	
	public MonitorStatusEntity clone() {
		try {
			MonitorStatusEntity entity = (MonitorStatusEntity)super.clone();
			entity.counter = this.counter;
			entity.lastUpdate = this.lastUpdate;
			entity.priority = this.priority;
			return entity;
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
	
	public static void main (String args[]) {
		MonitorStatusEntity entity = new MonitorStatusEntity("1", "2", "3", "4");
		System.out.println("hoge1 " + entity);
		System.out.println("hoge2 " + entity.clone());
		System.out.println("hoge3 " + entity.equals(entity.clone()));
	}
}