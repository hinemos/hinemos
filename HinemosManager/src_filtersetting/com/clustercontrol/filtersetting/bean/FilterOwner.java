/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

import com.clustercontrol.filtersetting.entity.FilterEntity;
import com.clustercontrol.filtersetting.entity.FilterEntityPK;

/**
 * フィルタ設定の所有者データを操作するためのユーティリティです。
 */
// value object(VO) にしたかったが、REST DTO や JPA entity のフィールドを VO にするのは
// プロジェクトの規約・慣習的に容易ではなく、結果として String 値として扱うケースが多くなってしまう。
// プログラム全般で VO として扱わない場合は、利便性が落ちるだけなので、ユーティリティにした。
// (String 値として扱うケースが多いにも関わらず VO にしたケースとしては、
// FilterSettingObjectId クラスがそれで、一時的に new するだけの使い方になってしまっている。)
public class FilterOwner {

	/** 共通フィルタ設定の所有者を表すコード */
	private static final String COMMON_FILTER_OWNER_VALUE = "TARGET:ALL_USERS";

	// utiltiy class
	private FilterOwner() {
		// NOP
	}

	/**
	 * パラメータに対応する文字列値を返します。
	 */
	public static String resolve(boolean common, String ownerUserId) {
		if (common) {
			return COMMON_FILTER_OWNER_VALUE;
		} else {
			return ownerUserId;
		}
	}

	/**
	 * 指定された文字列値が、共通フィルタ設定を表す場合は true を、ユーザフィルタ設定の場合は false を返します。
	 */
	public static boolean isCommon(String dbValue) {
		return COMMON_FILTER_OWNER_VALUE.equals(dbValue);
	}

	/**
	 * 指定されたエンティティが、共通フィルタ設定である場合は true を、ユーザフィルタ設定である場合は false を返します。
	 */
	public static boolean isCommon(FilterEntity entity) {
		return COMMON_FILTER_OWNER_VALUE.equals(entity.getId().getFilterOwner());
	}

	/**
	 * 指定されたエンティティPKが、共通フィルタ設定である場合は true を、ユーザフィルタ設定である場合は false を返します。
	 */
	public static boolean isCommon(FilterEntityPK entityPK) {
		return COMMON_FILTER_OWNER_VALUE.equals(entityPK.getFilterOwner());
	}

	/**
	 * 共通フィルタ設定を表す文字列値を返します。
	 */
	public static String ofCommon() {
		return COMMON_FILTER_OWNER_VALUE;
	}

	/**
	 * 指定された文字列値がユーザフィルタ設定を表す場合はユーザIDを、共通フィルタ設定の場合は null を返します。
	 */
	public static String toOwnerUserId(String dbValue) {
		if (isCommon(dbValue)) {
			return null;
		} else {
			return dbValue;
		}
	}

}
