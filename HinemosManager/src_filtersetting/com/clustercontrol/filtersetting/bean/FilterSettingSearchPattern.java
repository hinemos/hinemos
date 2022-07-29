/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

import com.clustercontrol.util.FilterConstant;

/**
 * フィルタ設定の検索パターン文字列です。
 */
public class FilterSettingSearchPattern {
	private final boolean negative;
	private final String likePattern;

	public FilterSettingSearchPattern(String pattern) {
		if (pattern == null || pattern.trim().length() == 0) {
			negative = false;
			likePattern = "%";
			return;
		}

		if (pattern.startsWith(FilterConstant.NEGATION_PREFIX)) {
			negative = true;
			pattern = pattern.substring(FilterConstant.NEGATION_PREFIX.length());
			if (pattern.length() == 0) {
				likePattern = "%";
			} else {
				likePattern = "%" + pattern + "%";
			}
		} else {
			negative = false;
			likePattern = "%" + pattern + "%";
		}
	}

	/**
	 * 条件の判定指定がある場合は true を、そうでない場合は false を返します。
	 */
	public boolean isNegative() {
		return negative;
	}

	/**
	 * SQLのLIKE式で使用するパターン文字列を返します。
	 */
	public String getLikePattern() {
		return likePattern;
	}

}
