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
import jakarta.persistence.MappedSuperclass;

/**
 * "setting.cc_filter" テーブルのプライマリキーを表します。
 */
@Embeddable
@MappedSuperclass
public class FilterEntityPK implements Serializable, Comparable<FilterEntityPK> {

	private static final long serialVersionUID = 1L;

	private Integer filterCategory;
	private String filterOwner;
	private String filterId;

	@Deprecated() // for JPA only
	public FilterEntityPK() {
	}

	public FilterEntityPK(Integer filterCategory, String filterOwner, String filterId) {
		this.filterCategory = filterCategory;
		this.filterOwner = filterOwner;
		this.filterId = filterId;
	}

	@Override
	public int compareTo(FilterEntityPK o) {
		int i = filterCategory.compareTo(o.filterCategory);
		if (i == 0) {
			i = filterOwner.compareTo(o.filterOwner);
			if (i == 0) {
				i = filterId.compareTo(o.filterId);
			}
		}
		return i;
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

	@Override
	public String toString() {
		return "[filterCategory=" + filterCategory + ", filterOwner=" + filterOwner + ", filterId=" + filterId + "]";
	}

	// eclipse-generated
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		FilterEntityPK other = (FilterEntityPK) obj;
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
