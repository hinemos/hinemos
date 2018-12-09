/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@NamedQueries({
	@NamedQuery(
		name=BillingDetailRelationEntity.selectBillingDetailRelationEntity,
		query="SELECT r FROM BillingDetailRelationEntity AS r " +
			"WHERE r.facilityId IN :facilityIds AND :start <= r.billingDetail.targetDate AND r.billingDetail.targetDate < :end " +
			"ORDER BY r.facilityId, r.billingDetail.cloudScopeId, r.billingDetail.resourceId, r.billingDetail.category, " +
			"r.billingDetail.categoryDetail, r.billingDetail.displayName, r.billingDetail.unit, r.billingDetail.targetDate"),
	@NamedQuery(
			name=BillingDetailRelationEntity.selectBillingDetailRelationEntityAfter,
			query="SELECT r FROM BillingDetailRelationEntity AS r WHERE r.facilityId IN :facilityIds AND :beginningTime <= r.billingDetail.targetDate")
})
@Entity
@Table(name="cc_cfg_xcloud_billing_detail_relation", schema="log")
public class BillingDetailRelationEntity {
	public final static String selectBillingDetailRelationEntity = "selectBillingDetailRelationEntity";
	public final static String selectBillingDetailRelationEntityAfter = "selectBillingDetailRelationEntityAfter";

	public static enum RelationType {
		node,
		additional
	}
	
	private Long relationId;
	private BillingDetailEntity billingDetail;
    private String facilityId;
    private String facilityName;
    private RelationType relationType;
    
    public BillingDetailRelationEntity() {
    }
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="relation_id")
	public Long getRelationId() {
		return relationId;
	}
	public void setRelationId(Long relationId) {
		this.relationId = relationId;
	}

	@ManyToOne(optional=false)
	@JoinColumn(name = "billing_detail_id")
	public BillingDetailEntity getBillingDetail() {
		return billingDetail;
	}
	public void setBillingDetail(BillingDetailEntity billingDetail) {
		this.billingDetail = billingDetail;
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="facility_name")
	public String getFacilityName() {
		return facilityName;
	}
	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	@Column(name="relation_type")
	@Enumerated(EnumType.STRING)
	public RelationType getRelationType() {
		return relationType;
	}
	public void setRelationType(RelationType relationType) {
		this.relationType = relationType;
	}

	@Override
	public String toString() {
		return "BillingDetailRelationEntity [relationId=" + relationId + ", billingDetail=" + billingDetail
				+ ", facilityId=" + facilityId + ", facilityName=" + facilityName + ", relationType=" + relationType
				+ "]";
	}
}
