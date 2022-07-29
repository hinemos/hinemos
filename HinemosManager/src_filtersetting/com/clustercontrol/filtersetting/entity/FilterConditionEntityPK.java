/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * "setting.cc_filter_condition" テーブルのプライマリキーを表します。
 */
@Embeddable
public class FilterConditionEntityPK implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer filterCategory;
	private String filterOwner;
	private String filterId;
	private Integer conditionIdx;

	@Deprecated // for JPA only
	public FilterConditionEntityPK() {
	}

	public FilterConditionEntityPK(Integer filterCategory, String filterOwner, String filterId, Integer conditionIdx) {
		this.filterCategory = filterCategory;
		this.filterOwner = filterOwner;
		this.filterId = filterId;
		this.conditionIdx = conditionIdx;
	}

	public FilterConditionEntityPK(FilterEntityPK parentId, Integer conditionIdx) {
		this(parentId.getFilterCategory(), parentId.getFilterOwner(), parentId.getFilterId(), conditionIdx);
	}

	@Column(name = "filter_category")
	public Integer getFilterCategory() {
		return filterCategory;
	}

	public void setFilterCategory(Integer filterCategory) {
		this.filterCategory = filterCategory;
	}

	@Column(name = "filter_owner")
	public String getFilterOwner() {
		return filterOwner;
	}

	public void setFilterOwner(String filterOwner) {
		this.filterOwner = filterOwner;
	}

	@Column(name = "filter_id")
	public String getFilterId() {
		return filterId;
	}

	public void setFilterId(String filterId) {
		this.filterId = filterId;
	}

	@Column(name = "condition_idx")
	public Integer getConditionIdx() {
		return conditionIdx;
	}

	public void setConditionIdx(Integer conditionIdx) {
		this.conditionIdx = conditionIdx;
	}

	@Override
	public String toString() {
		return "[filterCategory=" + filterCategory + ", filterOwner=" + filterOwner + ", filterId=" + filterId
				+ ", conditionIdx=" + conditionIdx + "]";
	}

	// eclipse-generated
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conditionIdx == null) ? 0 : conditionIdx.hashCode());
		result = prime * result + ((filterCategory == null) ? 0 : filterCategory.hashCode());
		result = prime * result + ((filterId == null) ? 0 : filterId.hashCode());
		result = prime * result + ((filterOwner == null) ? 0 : filterOwner.hashCode());
		return result;
	}

	// eclipse-generated
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FilterConditionEntityPK other = (FilterConditionEntityPK) obj;
		if (conditionIdx == null) {
			if (other.conditionIdx != null) return false;
		} else if (!conditionIdx.equals(other.conditionIdx)) return false;
		if (filterCategory == null) {
			if (other.filterCategory != null) return false;
		} else if (!filterCategory.equals(other.filterCategory)) return false;
		if (filterId == null) {
			if (other.filterId != null) return false;
		} else if (!filterId.equals(other.filterId)) return false;
		if (filterOwner == null) {
			if (other.filterOwner != null) return false;
		} else if (!filterOwner.equals(other.filterOwner)) return false;
		return true;
	}

}
