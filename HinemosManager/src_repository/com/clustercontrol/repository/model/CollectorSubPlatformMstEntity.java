package com.clustercontrol.repository.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

/**
 * The persistent class for the cc_collector_platform_mst database table.
 * 
 */
@Entity
@Table(name="cc_collector_sub_platform_mst", schema="setting")
@Cacheable(true)
public class CollectorSubPlatformMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String subPlatformId;
	private String subPlatformName;
	private String type;
	private Integer orderNo;

	@Deprecated
	public CollectorSubPlatformMstEntity() {
	}

	public CollectorSubPlatformMstEntity(String subPlatformId) {
		this.setSubPlatformId(subPlatformId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	@Id
	@Column(name="sub_platform_id")
	public String getSubPlatformId() {
		return subPlatformId;
	}

	public void setSubPlatformId(String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	@Column(name="sub_platform_name")
	public String getSubPlatformName() {
		return subPlatformName;
	}

	public void setSubPlatformName(String subPlatformName) {
		this.subPlatformName = subPlatformName;
	}

	@Column(name="type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(name="order_no")
	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

}
