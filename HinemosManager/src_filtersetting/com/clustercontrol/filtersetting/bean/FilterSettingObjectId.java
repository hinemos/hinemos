/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

import java.util.Objects;

import com.clustercontrol.filtersetting.entity.FilterEntityPK;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;

/**
 * フィルタ設定のオブジェクト権限用IDを表します。
 */
public class FilterSettingObjectId {

	private final FilterCategoryEnum filterCategory;
	private final boolean common;
	private final String filterId;
	private final String dbValue;

	public FilterSettingObjectId(FilterCategoryEnum filterCategory, boolean common, String filterId) {
		Objects.requireNonNull(filterCategory, "filterCategory");
		Objects.requireNonNull(filterId, "filterId");

		this.filterCategory = filterCategory;
		this.common = common;
		this.filterId = filterId;
		if (common) {
			String lzCat = "000" + String.valueOf(filterCategory.getCode());
			dbValue = lzCat.substring(lzCat.length() - 4) + filterId;
		} else {
			dbValue = null;
		}
	}

	public FilterSettingObjectId(String dbValue) {
		this.dbValue = dbValue;
		if (dbValue == null) {
			filterCategory = null;
			common = false;
			filterId = null;
		} else {
			filterCategory = FilterCategoryEnum.fromCode(Integer.parseInt(dbValue.substring(0, 4), 10));
			common = true;
			filterId = dbValue.substring(4);
		}
	}

	/**
	 * DBへ格納する文字列値を返します。
	 */
	public String toDbValue() {
		return dbValue;
	}

	/**
	 * ユーザ向けの表現を返します。
	 */
	public String toMessage() {
		if (common) {
			return filterId + " (" + filterCategory.getLabel() + ")";
		} else {
			return "User Filter Setting";
		}
	}

	public FilterEntityPK toEntityPK() {
		if (!common) {
			// logic error
			throw new RuntimeException("User Filter Setting can't have object privilege.");
		}
		return new FilterEntityPK(filterCategory.getCode(), FilterOwner.ofCommon(), filterId);
	}

}
