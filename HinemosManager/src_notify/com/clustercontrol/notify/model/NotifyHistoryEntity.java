package com.clustercontrol.notify.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_notify_history database table.
 * 
 */
@Entity
@Table(name="cc_notify_history", schema="setting")
@Cacheable(true)
public class NotifyHistoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private NotifyHistoryEntityPK id;
	private Long lastNotify;
	private Integer priority;

	@Deprecated
	public NotifyHistoryEntity() {
	}

	public NotifyHistoryEntity(NotifyHistoryEntityPK pk) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	public NotifyHistoryEntity(
			String facilityId,
			String pluginId,
			String monitorId,
			String notifyId,
			String subKey) {
		this(new NotifyHistoryEntityPK(facilityId,
				pluginId,
				monitorId,
				notifyId,
				subKey));
	}

	@EmbeddedId
	public NotifyHistoryEntityPK getId() {
		return this.id;
	}

	public void setId(NotifyHistoryEntityPK id) {
		this.id = id;
	}


	@Column(name="last_notify")
	public Long getLastNotify() {
		return this.lastNotify;
	}

	public void setLastNotify(Long lastNotify) {
		this.lastNotify = lastNotify;
	}


	@Column(name="priority")
	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

}