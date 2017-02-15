package com.clustercontrol.performance.monitor.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_collector_category_mst database table.
 * 
 */
@Entity
@Table(name="cc_collector_category_mst", schema="setting")
@Cacheable(true)
public class CollectorCategoryMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String categoryCode;
	private String categoryName;
	private List<CollectorCategoryCollectMstEntity> collectorCategoryCollectMstEntities;
	private List<CollectorItemCodeMstEntity> collectorItemCodeMstEntities;

	@Deprecated
	public CollectorCategoryMstEntity() {
	}

	public CollectorCategoryMstEntity(String categoryCode) {
		this.setCategoryCode(categoryCode);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}


	@Id
	@Column(name="category_code")
	public String getCategoryCode() {
		return this.categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}


	@Column(name="category_name")
	public String getCategoryName() {
		return this.categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}


	//bi-directional many-to-one association to CollectorCategoryCollectMstEntity
	@OneToMany(mappedBy="collectorCategoryMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<CollectorCategoryCollectMstEntity> getCollectorCategoryCollectMstEntities() {
		return this.collectorCategoryCollectMstEntities;
	}

	public void setCollectorCategoryCollectMstEntities(List<CollectorCategoryCollectMstEntity> collectorCategoryCollectMstEntities) {
		this.collectorCategoryCollectMstEntities = collectorCategoryCollectMstEntities;
	}


	//bi-directional many-to-one association to CollectorItemCodeMstEntity
	@OneToMany(mappedBy="collectorCategoryMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<CollectorItemCodeMstEntity> getCollectorItemCodeMstEntities() {
		return this.collectorItemCodeMstEntities;
	}

	public void setCollectorItemCodeMstEntities(List<CollectorItemCodeMstEntity> collectorItemCodeMstEntities) {
		this.collectorItemCodeMstEntities = collectorItemCodeMstEntities;
	}

}