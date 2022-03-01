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
 * "setting.cc_filter_condition_item" テーブルのプライマリキーを表します。
 */
@Embeddable
public class FilterConditionItemEntityPK implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer filterCategory;
	private String filterOwner;
	private String filterId;
	private Integer conditionIdx;
	private Integer itemType;

	@Deprecated // for JPA only
	public FilterConditionItemEntityPK() {
	}

	public FilterConditionItemEntityPK(Integer filterCategory, String filterOwner, String filterId, Integer conditionIdx,
			Integer itemType) {
		this.filterCategory = filterCategory;
		this.filterOwner = filterOwner;
		this.filterId = filterId;
		this.conditionIdx = conditionIdx;
		this.itemType = itemType;
	}

	public FilterConditionItemEntityPK(FilterConditionEntityPK parentId, Integer itemType) {
		this(parentId.getFilterCategory(), parentId.getFilterOwner(), parentId.getFilterId(), parentId.getConditionIdx(),
				itemType);
	}

	public FilterConditionItemEntityPK(FilterEntityPK grandId, Integer conditionIdx, Integer itemType) {
		this(grandId.getFilterCategory(), grandId.getFilterOwner(), grandId.getFilterId(), conditionIdx, itemType);
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

	@Column(name = "item_type")
	public Integer getItemType() {
		return itemType;
	}

	public void setItemType(Integer itemType) {
		this.itemType = itemType;
	}

	@Override
	public String toString() {
		return "[filterCategory=" + filterCategory + ", filterOwner=" + filterOwner + ", filterId=" + filterId
				+ ", conditionIdx=" + conditionIdx + ", itemType=" + itemType + "]";
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
		result = prime * result + ((itemType == null) ? 0 : itemType.hashCode());
		return result;
	}

	// eclipse-generated
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FilterConditionItemEntityPK other = (FilterConditionItemEntityPK) obj;
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
		if (itemType == null) {
			if (other.itemType != null) return false;
		} else if (!itemType.equals(other.itemType)) return false;
		return true;
	}

}
