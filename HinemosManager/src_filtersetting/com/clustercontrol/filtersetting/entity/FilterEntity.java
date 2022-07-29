/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.entity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.filtersetting.bean.FilterOwner;
import com.clustercontrol.filtersetting.bean.FilterSettingObjectId;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * "setting.cc_filter" テーブルのレコードを表します。
 */
// 本クラスはDBテーブルに対応したシンプルなDTOであり、原則としてビジネスロジックを持ちません。
// 本クラスへビジネスロジックを組み込むことを試みる前に、Info系クラスへ実装が可能かどうかを検討してください。
@Entity
@Table(name = "cc_Filter", schema = "setting")
@HinemosObjectPrivilege(objectType = HinemosModuleConstant.FILTER_SETTING, isModifyCheck = true,
		idFactory = "createIdFromObjectId", objectPrivilegeAvailable = true)
@AttributeOverride(name = "objectId", column = @Column(name = "object_id", insertable = true, updatable = true))
public class FilterEntity extends ObjectPrivilegeTargetInfo {

	private static final long serialVersionUID = 1L;

	private FilterEntityPK id;
	private String filterName;
	// private String ownerRoleId; // ObjectPrivilegeTargetInfoが扱う
	// private String objectId;    // ObjectPrivilegeTargetInfoが扱う
	private String facilityId;
	private Integer facilityTarget;
	private Integer filterRange;
	private String regUser;
	private Long regDate;
	private String updateUser;
	private Long updateDate;
	private List<FilterConditionEntity> conditions;

	/**
	 * オブジェクト権限IDから、このエンティティのPKを生成して返します。
	 */
	public static FilterEntityPK createIdFromObjectId(String objectId) {
		return new FilterSettingObjectId(objectId).toEntityPK();
	}
	
	/**
	 * 渡されたエンティティのリストを、連続更新・削除するための順番に並べ替えます。
	 * <p>
	 * flushしながら複数のエンティティを連続して更新する場合、本メソッドにより一貫性のある順序にします。
	 * そうしない場合、デッドロックを起こす恐れがあります。
	 */
	public static void sortForSequentialUpdate(List<FilterEntity> entities) {
		Collections.sort(entities, new Comparator<FilterEntity>() {
			@Override
			public int compare(FilterEntity o1, FilterEntity o2) {
				return o1.id.compareTo(o2.id);
			}
		});
	}

	@Deprecated // for JPA only
	public FilterEntity() {
	}

	/**
	 * 全フィールドを設定するコンストラクタ。<br/>
	 * フィールド追加を行ったとしても、このコンストラクタをメンテして、使用している限り、
	 * 意図しない null 初期化をコンパイルエラーで検出することができます。
	 */
	public FilterEntity(FilterEntityPK id, String filterName, String obejctId, String ownerRoleId, String facilityId,
			Integer facilityTarget, Integer filterRange, String regUser, Long regDate, String updateUser, Long updateDate,
			List<FilterConditionEntity> conditions) {
		this.id = id;
		this.filterName = filterName;
		this.setObjectId(obejctId);
		this.setOwnerRoleId(ownerRoleId);
		this.facilityId = facilityId;
		this.facilityTarget = facilityTarget;
		this.filterRange = filterRange;
		this.regUser = regUser;
		this.regDate = regDate;
		this.updateUser = updateUser;
		this.updateDate = updateDate;
		this.conditions = conditions;
	}

	@Override
	public boolean tranGetUncheckFlg() {
		// ユーザフィルタであるならオブジェクト権限チェックはしない
		if (id != null && !FilterOwner.isCommon(id)) {
			return true;
		}
		// 共通フィルタ設定 or 所有者未設定なら、標準の動作をする
		return super.tranGetUncheckFlg();
	}

	@EmbeddedId
	public FilterEntityPK getId() {
		return id;
	}

	public void setId(FilterEntityPK id) {
		this.id = id;
	}

	@Column(name = "filter_name")
	public String getFilterName() {
		return filterName;
	}

	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	@Column(name = "facility_id")
	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name = "facility_target")
	public Integer getFacilityTarget() {
		return facilityTarget;
	}

	public void setFacilityTarget(Integer facilityTarget) {
		this.facilityTarget = facilityTarget;
	}

	@Column(name = "filter_range")
	public Integer getFilterRange() {
		return filterRange;
	}

	public void setFilterRange(Integer filterRange) {
		this.filterRange = filterRange;
	}

	@Column(name = "reg_user")
	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	@Column(name = "reg_date")
	public Long getRegDate() {
		return regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	@Column(name = "update_user")
	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	@Column(name = "update_date")
	public Long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	@Transient // JPAリレーションの挙動は複雑で不具合の温床となる恐れがあるので、自力で関連付ける
	public List<FilterConditionEntity> getConditions() {
		return conditions;
	}

	public void setConditions(List<FilterConditionEntity> conditions) {
		this.conditions = conditions;
	}
}
