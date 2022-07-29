/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.clustercontrol.filtersetting.entity.FilterConditionEntity;
import com.clustercontrol.filtersetting.entity.FilterConditionEntityPK;
import com.clustercontrol.filtersetting.entity.FilterConditionItemEntity;
import com.clustercontrol.rest.dto.EnumDto;

/**
 * フィルタ基本情報のベースクラス。
 */
public abstract class FilterConditionInfo<T extends FilterItemType> {

	/** 説明 */
	private String description = "";

	/** 条件に一致しない結果を表示 */
	private Boolean negative = Boolean.FALSE;

	public FilterConditionInfo() {
		// NOP
	}

	public FilterConditionInfo(FilterConditionEntity entity) {
		Objects.requireNonNull(entity, "entity");
		this.description = entity.getDescription();
		this.negative = entity.getNegative();
	}

	/**
	 * サブクラスの初期化処理です。
	 * {@link #setItemValue(FilterItemType, String)} を使用して、entity の内容を設定します。
	 */
	// コンストラクタでやってしまえそうな処理だが、その場合は以下のフローになるため不可。
	// 1. サブクラスが super(entity) を呼び出す。
	// 2. このクラスのコンストラクタで entity からサブクラスのフィールドへ値を代入する。
	// 3. super(entity) 終了。
	// 4. サブクラスのフィールド初期化が実行される。  (2.で代入した値が上書きされてしまう)
	protected void initializeItems(FilterConditionEntity entity) {
		for (FilterConditionItemEntity it : entity.getItems()) {
			T type = convertFilterItemType(it.getId().getItemType());
			String value = it.getInputValue();
			setItemValue(type, value);
		}
	}

	/**
	 * 保持している内容を{@link FilterConditionEntity}へ変換します。
	 */
	public FilterConditionEntity toEntity(FilterConditionEntityPK condPK) {
		List<FilterConditionItemEntity> items = new ArrayList<>();
		for (T type : getAllFilterItemTypes()) {
			Object v = getItemValue(type);
			if (v != null) {
				if (v instanceof EnumDto) {
					v = ((EnumDto<?>) v).getCode();
				}
				items.add(new FilterConditionItemEntity(condPK, type.getCode(), v.toString()));
			}
		}

		return new FilterConditionEntity(condPK, description, negative, items);
	}

	/**
	 * [オーバーライド用] フィルタ条件項目種別コードを、enumへ変換します。
	 */
	public abstract T convertFilterItemType(Integer dbValue);

	/**
	 * [オーバーライド用] 全てのフィルタ条件項目種別の配列を返します。
	 */
	public abstract T[] getAllFilterItemTypes();

	/**
	 * [オーバーライド用] 指定されたフィルタ条件項目種別の値を設定します。
	 */
	public abstract void setItemValue(T type, String value);

	/**
	 * [オーバーライド用] 指定されたフィルタ条件項目種別の値を返します。
	 */
	public abstract Object getItemValue(T type);

	/**
	 * フィルタ条件の説明文を返します。
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * フィルタ条件の説明文を設定します。
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * フィルタ条件の全反転を返します。
	 */
	public Boolean getNegative() {
		return negative;
	}

	/**
	 * フィルタ条件の全反転を設定します。
	 */
	public void setNegative(Boolean negative) {
		this.negative = negative;
	}

}
