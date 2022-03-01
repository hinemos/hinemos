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
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * "setting.cc_filter_condition_item" テーブルのレコードを表します。
 */
// 本クラスはDBテーブルに対応したシンプルなDTOであり、原則としてビジネスロジックを持ちません。
// 本クラスへビジネスロジックを組み込むことを試みる前に、Info系クラスへ実装が可能かどうかを検討してください。
@Entity
@Table(name = "cc_Filter_condition_item", schema = "setting")
public class FilterConditionItemEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private FilterConditionItemEntityPK id;
	private String inputValue;

	@Deprecated // for JPA only
	public FilterConditionItemEntity() {
	}

	/**
	 * 全フィールドを設定するコンストラクタ。<br/>
	 * フィールド追加を行ったとしても、このコンストラクタをメンテして、使用している限り、
	 * 意図しない null 初期化をコンパイルエラーで検出することができます。
	 */
	public FilterConditionItemEntity(FilterConditionItemEntityPK id, String inputValue) {
		this.id = id;
		this.inputValue = inputValue;
	}

	public FilterConditionItemEntity(FilterConditionEntityPK parentId, Integer itemType, String inputValue) {
		this(new FilterConditionItemEntityPK(parentId, itemType), inputValue);
	}

	@EmbeddedId
	public FilterConditionItemEntityPK getId() {
		return id;
	}

	public void setId(FilterConditionItemEntityPK id) {
		this.id = id;
	}

	@Column(name = "input_value")
	public String getInputValue() {
		return inputValue;
	}

	public void setInputValue(String inputValue) {
		this.inputValue = inputValue;
	}

}
