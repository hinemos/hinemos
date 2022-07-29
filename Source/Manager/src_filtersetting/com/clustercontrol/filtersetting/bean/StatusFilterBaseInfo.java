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

import com.clustercontrol.filtersetting.entity.FilterConditionEntity;
import com.clustercontrol.filtersetting.entity.FilterConditionEntityPK;
import com.clustercontrol.filtersetting.entity.FilterEntity;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;

/**
 * ステータス通知結果フィルタ基本情報。
 */
public class StatusFilterBaseInfo {

	/** スコープ */
	private String facilityId = null;

	/** ファシリティのターゲット */
	private FacilityTarget facilityTarget = FacilityTarget.ALL;

	/** フィルタ詳細情報リスト */
	private List<StatusFilterConditionInfo> conditions = new ArrayList<>();

	/**
	 * 全件用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static StatusFilterBaseInfo ofAllStatus() {
		StatusFilterBaseInfo o = new StatusFilterBaseInfo();
		o.facilityId = null;
		o.facilityTarget = FacilityTarget.ALL;
		o.conditions.add(StatusFilterConditionInfo.ofAllStatus());
		return o;
	}

	/**
	 * クライアントビュー表示用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static StatusFilterBaseInfo ofClientViewDefault() {
		StatusFilterBaseInfo o = new StatusFilterBaseInfo();
		o.facilityId = null;
		o.facilityTarget = FacilityTarget.ALL;
		o.conditions.add(StatusFilterConditionInfo.ofClientViewDefault());
		return o;
	}

	public StatusFilterBaseInfo() {
		// NOP
	}

	public StatusFilterBaseInfo(FilterEntity entity) {
		this.facilityId = entity.getFacilityId();
		this.facilityTarget = FacilityTarget.fromCode(entity.getFacilityTarget());

		for (FilterConditionEntity condEntity : entity.getConditions()) {
			StatusFilterConditionInfo cond = new StatusFilterConditionInfo(condEntity);
			this.conditions.add(cond);
		}
	}

	/**
	 * 保持内容を指定されたエンティティへ書き込みます。
	 */
	public void writeTo(FilterEntity entity) {
		entity.setFacilityId(facilityId);
		entity.setFacilityTarget(facilityTarget.getCode());
		entity.setConditions(new ArrayList<>());

		for (StatusFilterConditionInfo cond : conditions) {
			FilterConditionEntityPK condPK = new FilterConditionEntityPK(entity.getId(), entity.getConditions().size());
			FilterConditionEntity condEntity = cond.toEntity(condPK);
			entity.getConditions().add(condEntity);
		}
	}

	/**
	 * SQL条件式構築オブジェクトを生成します。
	 * 
	 * @param facilityIdsLimit 1クエリ内での最大ファシリティID数。
	 * @param statusInfoAlias JPQL内での{@link StatusInfoEntity}のエイリアス。
	 * @return 戻り値のリストは必ず1要素以上を含みます。
	 */
	public List<StatusFilterBaseCriteria> createCriteria(int facilityIdsLimit, String statusInfoAlias) {
		List<StatusFilterBaseCriteria> rtn = new ArrayList<>();

		List<String> facilityIds = facilityTarget.expandFacilityIds(facilityId);
		if (facilityIds == null) {
			StatusFilterBaseCriteria crt = new StatusFilterBaseCriteria("B" + rtn.size(), statusInfoAlias);
			crt.facilityIds.setValues(null);
			rtn.add(crt);
		} else if (facilityIds.size() == 0) {
			StatusFilterBaseCriteria crt = new StatusFilterBaseCriteria("B" + rtn.size(), statusInfoAlias);
			crt.facilityIds.setValues(facilityIds);
			rtn.add(crt);
		} else {
			for (int from = 0; from < facilityIds.size(); from += facilityIdsLimit) {
				int to = from + facilityIdsLimit;
				if (to > facilityIds.size()) {
					to = facilityIds.size();
				}

				StatusFilterBaseCriteria crt = new StatusFilterBaseCriteria("B" + rtn.size(), statusInfoAlias);
				crt.facilityIds.setValues(new ArrayList<>(facilityIds.subList(from, to)));
				rtn.add(crt);
			}
		}

		return rtn;
	}

	/**
	 * 紐づいている {@link StatusFilterConditionInfo} のリストに対応した SQL条件式構築オブジェクトを生成します。
	 * 
	 * @param statusInfoAlias JPQL内での {@link StatusInfoEntity} のエイリアス。
	 */
	public List<StatusFilterConditionCriteria> createConditionsCriteria(String statusInfoAlias) {
		List<StatusFilterConditionCriteria> rtn = new ArrayList<>();
		for (StatusFilterConditionInfo cnd : conditions) {
			rtn.add(cnd.createCriteria("C" + rtn.size() + "_", statusInfoAlias));
		}
		return rtn;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public FacilityTarget getFacilityTarget() {
		return facilityTarget;
	}

	public void setFacilityTarget(FacilityTarget facilityTarget) {
		this.facilityTarget = facilityTarget;
	}

	public List<StatusFilterConditionInfo> getConditions() {
		return conditions;
	}

	public void setConditions(List<StatusFilterConditionInfo> conditions) {
		this.conditions = conditions;
	}

}
