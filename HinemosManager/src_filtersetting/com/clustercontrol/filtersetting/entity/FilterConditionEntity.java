/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.entity;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * "setting.cc_filter_condition" テーブルのレコードを表します。
 */
// 本クラスはDBテーブルに対応したシンプルなDTOであり、原則としてビジネスロジックを持ちません。
// 本クラスへビジネスロジックを組み込むことを試みる前に、Info系クラスへ実装が可能かどうかを検討してください。
@Entity
@Table(name = "cc_Filter_condition", schema = "setting")
public class FilterConditionEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private FilterConditionEntityPK id;
	private String description;
	private Boolean negative;
	private List<FilterConditionItemEntity> items;

	@Deprecated // for JPA only
	public FilterConditionEntity() {
	}

	/**
	 * 全フィールドを設定するコンストラクタ。<br/>
	 * フィールド追加を行ったとしても、このコンストラクタをメンテして、使用している限り、
	 * 意図しない null 初期化をコンパイルエラーで検出することができます。
	 */
	public FilterConditionEntity(FilterConditionEntityPK id, String description, Boolean negative,
			List<FilterConditionItemEntity> items) {
		this.id = id;
		this.description = description;
		this.negative = negative;
		this.items = items;
	}

	@EmbeddedId
	public FilterConditionEntityPK getId() {
		return id;
	}

	public void setId(FilterConditionEntityPK id) {
		this.id = id;
	}

	@Column(name = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "negate_cond")
	public Boolean getNegative() {
		return negative;
	}

	public void setNegative(Boolean negative) {
		this.negative = negative;
	}

	@Transient // JPAリレーションの挙動は複雑で不具合の温床となる恐れがあるので、自力で関連付ける
	public List<FilterConditionItemEntity> getItems() {
		return items;
	}

	public void setItems(List<FilterConditionItemEntity> items) {
		this.items = items;
	}

}
