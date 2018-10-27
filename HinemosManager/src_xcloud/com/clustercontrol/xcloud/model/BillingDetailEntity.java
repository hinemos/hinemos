/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * 本当は、エンタープライズ側にあるべきクラス<BR>
 * eclipselink の weaving の問題で、CloudManagerBase へ移動。DB のスキーマも CloudManagerBase に含まれる。
 * 
 */
@NamedQueries({
	@NamedQuery(
			name=BillingDetailEntity.selectBillingDetailEntity,
			query="SELECT d FROM BillingDetailEntity AS d WHERE d.cloudScopeId = :cloudScopeId AND :start <= d.targetDate AND d.targetDate < :end " +
					 "ORDER BY d.category, d.resourceId, d.categoryDetail, d.displayName, d.targetDate"),
	@NamedQuery(
			name=BillingDetailEntity.selectBillingDetailEntityRange,
			query="SELECT d FROM BillingDetailEntity d JOIN d.billingDetailRelations r WHERE r.facilityId IN :facilityIds AND :start <= d.targetDate AND d.targetDate < :end"),
	@NamedQuery(
			name=BillingDetailEntity.deleteBillingDetailEntity,
			query="DELETE FROM BillingDetailEntity d WHERE d.cloudScopeId = :cloudScopeId"),
	@NamedQuery(
			name=BillingDetailEntity.deleteBillingDetailEntityOutOfRange,
			query="DELETE FROM BillingDetailEntity d WHERE d.cloudScopeId = :cloudScopeId AND d.targetDate < :retentionDay")
})
@Entity
@Table(name="cc_cfg_xcloud_billing_detail", schema="log")
public class BillingDetailEntity {
	public final static String deleteBillingDetailEntity = "deleteBillingDetailEntity";
	public final static String selectBillingDetailEntity = "selectBillingDetailEntity";
	public final static String selectBillingDetailEntityRange = "selectBillingDetailEntityRange";
	public final static String deleteBillingDetailEntityOutOfRange = "deleteBillingDetailEntityOutOfRange";
	
	private Long billingDetailId;
	private String cloudScopeId;
	private String cloudScopeName;
	private Long targetDate;
	private String category;
	private String categoryDetail;
	private String displayName;
	private Double cost;
	private String unit;
	private List<BillingDetailRelationEntity> relations = new ArrayList<>();
	private String resourceId;
	
	public BillingDetailEntity() {
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="billing_detail_id")
	public Long getBillingDetailId() {
		return billingDetailId;
	}
	public void setBillingDetailId(Long billingDetailId) {
		this.billingDetailId = billingDetailId;
	}
	@Column(name="cloud_scope_id")
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}
	@Column(name="cloud_scope_name")
	public String getCloudScopeName() {
		return cloudScopeName;
	}
	public void setCloudScopeName(String cloudScopeName) {
		this.cloudScopeName = cloudScopeName;
	}
	@Column(name="target_date")
	public Long getTargetDate() {
		return targetDate;
	}
	public void setTargetDate(Long targetDate) {
		this.targetDate = targetDate;
	}
	@Column(name="category")
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	@Column(name="category_detail")
	public String getCategoryDetail() {
		return categoryDetail;
	}
	public void setCategoryDetail(String categoryDetail) {
		this.categoryDetail = categoryDetail;
	}
	@Column(name="display_name")
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	@Column(name="cost")
	public Double getCost() {
		return cost;
	}
	public void setCost(Double cost) {
		this.cost = cost;
	}
	@Column(name="unit")
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	@OneToMany(fetch=FetchType.EAGER, mappedBy="billingDetail", cascade={CascadeType.ALL})
	public List<BillingDetailRelationEntity> getBillingDetailRelations() {
		return relations;
	}
	public void setBillingDetailRelations(List<BillingDetailRelationEntity> relations) {
		this.relations = relations;
	}
	@Column(name="resource_id")
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
}
