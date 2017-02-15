package com.clustercontrol.vm.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_vm_protocol_mst database table.
 * 
 */
@Entity
@Table(name="cc_vm_protocol_mst", schema="setting")
@Cacheable(true)
public class VmProtocolMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private VmProtocolMstEntityPK id;
	private Integer orderNo;

	@Deprecated
	public VmProtocolMstEntity() {
	}

	public VmProtocolMstEntity(VmProtocolMstEntityPK pk) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	public VmProtocolMstEntity(String subPlatformId, String protocol) {
		this(new VmProtocolMstEntityPK(subPlatformId, protocol));
	}

	@EmbeddedId
	public VmProtocolMstEntityPK getId() {
		return this.id;
	}

	public void setId(VmProtocolMstEntityPK id) {
		this.id = id;
	}


	@Column(name="order_no")
	public Integer getOrderNo() {
		return this.orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

}