/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.session;

import java.util.List;
import java.util.Optional;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.QueryCriteria;
import com.clustercontrol.commons.util.Transaction;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.filtersetting.bean.FilterOwner;
import com.clustercontrol.filtersetting.entity.FilterConditionEntity;
import com.clustercontrol.filtersetting.entity.FilterConditionItemEntity;
import com.clustercontrol.filtersetting.entity.FilterEntity;
import com.clustercontrol.filtersetting.entity.FilterEntityPK;

import jakarta.persistence.TypedQuery;

/**
 * フィルタ設定特有の永続化データアクセスを提供します。
 * 言い換えると、DBアクセスの薄いラッパーです。
 */
public class FilterTx extends Transaction {

	public Optional<FilterEntity> findFilter(FilterEntityPK id, ObjectPrivilegeMode priv) throws InvalidRole {
		try {
			return Optional.ofNullable(getEm().find(FilterEntity.class, id, priv));
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e);
		}
	}

	public List<FilterEntity> searchFilters(Integer filterCategory, String filterOwner, String pattern,
			ObjectPrivilegeMode priv) {
		return getEm()
				.createNamedQuery("FilterEntity.search", FilterEntity.class, priv)
				.setParameter("filterCategory", filterCategory)
				.setParameter("filterOwner", filterOwner)
				.setParameter("pattern", pattern)
				.getResultList();
	}

	public List<FilterEntity> searchFilterNot(Integer filterCategory, String filterOwner, String pattern,
			ObjectPrivilegeMode priv) {
		return getEm()
				.createNamedQuery("FilterEntity.searchNot", FilterEntity.class, priv)
				.setParameter("filterCategory", filterCategory)
				.setParameter("filterOwner", filterOwner)
				.setParameter("pattern", pattern)
				.getResultList();
	}

	public List<FilterEntity> searchFiltersOfAllUsers(Integer filterCategory, String pattern) {
		return getEm()
				.createNamedQuery("FilterEntity.searchAllUsers", FilterEntity.class)
				.setParameter("filterCategory", filterCategory)
				.setParameter("filterOwnerCommon", FilterOwner.ofCommon())
				.setParameter("pattern", pattern)
				.getResultList();
	}

	public List<FilterEntity> searchFiltersOfAllUsersNot(Integer filterCategory, String pattern) {
		return getEm()
				.createNamedQuery("FilterEntity.searchAllUsersNot", FilterEntity.class)
				.setParameter("filterCategory", filterCategory)
				.setParameter("filterOwnerCommon", FilterOwner.ofCommon())
				.setParameter("pattern", pattern)
				.getResultList();
	}

	private static class OwnerSearchCriteria extends QueryCriteria {
		In<String> owners = new In<>("a.id.filterOwner");

		@Override
		protected String[] getConditionFieldNames() {
			return new String[] { "owners" };
		}
	}

	public List<FilterEntity> searchFiltersByOwners(List<String> filterOwners, ObjectPrivilegeMode priv) {
		OwnerSearchCriteria crt = new OwnerSearchCriteria();
		crt.owners.setValues(filterOwners);

		TypedQuery<FilterEntity> query = getEm().createQuery(
				"SELECT a FROM FilterEntity a WHERE " + crt.buildExpressions()
						+ " ORDER BY a.id.filterCategory, a.id.filterId",
				FilterEntity.class, priv);

		crt.submitParameters(query);
		return query.getResultList();
	}

	public List<FilterConditionEntity> findConditions(FilterEntityPK id) {
		return getEm()
				.createNamedQuery("FilterConditionEntity.find", FilterConditionEntity.class)
				.setParameter("filterCategory", id.getFilterCategory())
				.setParameter("filterOwner", id.getFilterOwner())
				.setParameter("filterId", id.getFilterId())
				.getResultList();
	}

	public int removeConditions(FilterEntityPK id) {
		return getEm()
				.createNamedQuery("FilterConditionEntity.remove")
				.setParameter("filterCategory", id.getFilterCategory())
				.setParameter("filterOwner", id.getFilterOwner())
				.setParameter("filterId", id.getFilterId())
				.executeUpdate();
	}

	public List<FilterConditionItemEntity> findConditionItems(FilterEntityPK id) {
		return getEm()
				.createNamedQuery("FilterConditionItemEntity.find", FilterConditionItemEntity.class)
				.setParameter("filterCategory", id.getFilterCategory())
				.setParameter("filterOwner", id.getFilterOwner())
				.setParameter("filterId", id.getFilterId())
				.getResultList();
	}

	public int removeConditionItems(FilterEntityPK id) {
		return getEm()
				.createNamedQuery("FilterConditionItemEntity.remove")
				.setParameter("filterCategory", id.getFilterCategory())
				.setParameter("filterOwner", id.getFilterOwner())
				.setParameter("filterId", id.getFilterId())
				.executeUpdate();
	}

}
