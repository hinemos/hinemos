/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import com.clustercontrol.filtersetting.entity.FilterConditionEntity;
import com.clustercontrol.filtersetting.entity.FilterConditionEntityPK;
import com.clustercontrol.filtersetting.entity.FilterEntity;
import com.clustercontrol.notify.monitor.model.EventLogEntity;

/**
 * イベント履歴フィルタ基本情報。
 */
public class EventFilterBaseInfo {

	/** スコープ */
	private String facilityId = null;

	/** ファシリティのターゲット */
	private FacilityTarget facilityTarget = FacilityTarget.ALL;

	/** true なら全範囲検索、false ならキャッシュ検索 */
	private Boolean entire = Boolean.FALSE;

	/** フィルタ詳細情報リスト */
	private List<EventFilterConditionInfo> conditions = new ArrayList<>();

	/**
	 * 全件検索設定を行ったインスタンスを生成します。
	 */
	public static EventFilterBaseInfo ofAllEvents() {
		EventFilterBaseInfo o = new EventFilterBaseInfo();
		o.facilityId = null;
		o.facilityTarget = FacilityTarget.ALL;
		o.entire = Boolean.TRUE;
		o.conditions.add(EventFilterConditionInfo.ofAllEvents());
		return o;
	}

	/**
	 * クライアントビュー表示用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static EventFilterBaseInfo ofClientViewDefault() {
		EventFilterBaseInfo o = new EventFilterBaseInfo();
		o.facilityId = null;
		o.facilityTarget = FacilityTarget.ALL;
		o.entire = Boolean.FALSE;
		o.conditions.add(EventFilterConditionInfo.ofClientViewDefault());
		return o;
	}

	/**
	 * ファイルダウンロード用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static EventFilterBaseInfo ofDownloadDefault() {
		EventFilterBaseInfo o = new EventFilterBaseInfo();
		o.facilityId = null;
		o.facilityTarget = FacilityTarget.ONE_LEVEL;
		o.entire = Boolean.TRUE;
		o.conditions.add(EventFilterConditionInfo.ofDownloadDefault());
		return o;
	}

	/**
	 * 一括確認用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static EventFilterBaseInfo ofBatchConfirmingDefault() {
		EventFilterBaseInfo o = new EventFilterBaseInfo();
		o.facilityId = null;
		o.facilityTarget = FacilityTarget.ALL;
		o.entire = Boolean.TRUE;
		o.conditions.add(EventFilterConditionInfo.ofBatchConfirmingDefault());
		return o;
	}

	public EventFilterBaseInfo() {
		// NOP
	}

	public EventFilterBaseInfo(FilterEntity entity) {
		Objects.requireNonNull(entity, "entity");

		this.facilityId = entity.getFacilityId();
		this.facilityTarget = FacilityTarget.fromCode(entity.getFacilityTarget());
		this.entire = EventFilterRange.fromCode(entity.getFilterRange()) == EventFilterRange.ENTIRE;

		for (FilterConditionEntity condEntity : entity.getConditions()) {
			EventFilterConditionInfo cond = new EventFilterConditionInfo(condEntity);
			this.conditions.add(cond);
		}
	}

	/**
	 * 保持内容を指定されたエンティティへ書き込みます。
	 */
	public void writeTo(FilterEntity entity) {
		Objects.requireNonNull(entity, "entity");

		entity.setFacilityId(facilityId);
		entity.setFacilityTarget(facilityTarget.getCode());
		entity.setFilterRange((entire.booleanValue() ? EventFilterRange.ENTIRE : EventFilterRange.CACHED).getCode());

		entity.setConditions(new ArrayList<>());
		for (EventFilterConditionInfo cond : conditions) {
			FilterConditionEntityPK condPK = new FilterConditionEntityPK(entity.getId(), entity.getConditions().size());
			FilterConditionEntity condEntity = cond.toEntity(condPK);
			entity.getConditions().add(condEntity);
		}
	}

	/**
	 * SQL条件式構築オブジェクトを生成します。
	 * 
	 * @param facilityIdsLimit 1クエリ内での最大ファシリティID数。
	 * @param eventLogAlias JPQL内での{@link EventLogEntity}のエイリアス。
	 * @return 戻り値のリストは必ず1要素以上を含みます。
	 */
	public List<EventFilterBaseCriteria> createCriteria(int facilityIdsLimit, String eventLogAlias) {
		List<EventFilterBaseCriteria> rtn = new ArrayList<>();

		List<String> facilityIds = facilityTarget.expandFacilityIds(facilityId);
		if (facilityIds == null) {
			EventFilterBaseCriteria crt = new EventFilterBaseCriteria("B", eventLogAlias);
			crt.facilityIds.setValues(null);
			rtn.add(crt);
		} else if (facilityIds.size() == 0) {
			EventFilterBaseCriteria crt = new EventFilterBaseCriteria("B", eventLogAlias);
			crt.facilityIds.setValues(facilityIds);
			rtn.add(crt);
		} else {
			for (int from = 0; from < facilityIds.size(); from += facilityIdsLimit) {
				int to = from + facilityIdsLimit;
				if (to > facilityIds.size()) {
					to = facilityIds.size();
				}

				EventFilterBaseCriteria crt = new EventFilterBaseCriteria("B", eventLogAlias);
				// イベントキャッシュでの検索処理(Javaでの比較処理)の速度を上げるため、HashSetを使う
				crt.facilityIds.setValues(new HashSet<>(facilityIds.subList(from, to)));
				rtn.add(crt);
			}
		}

		return rtn;
	}

	/**
	 * 紐づいている {@link EventFilterConditionInfo} のリストに対応した SQL条件式構築オブジェクトを生成します。
	 * 
	 * @param eventLogAlias JPQL内での {@link EventLogEntity} のエイリアス。
	 */
	public List<EventFilterConditionCriteria> createConditionsCriteria(String eventLogAlias) {
		List<EventFilterConditionCriteria> rtn = new ArrayList<>();
		for (EventFilterConditionInfo cnd : conditions) {
			rtn.add(cnd.createCriteria("C" + rtn.size() + "_", eventLogAlias));
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
		Objects.requireNonNull(facilityTarget, "facilityTarget");
		this.facilityTarget = facilityTarget;
	}

	public Boolean getEntire() {
		return entire;
	}

	public void setEntire(Boolean entire) {
		Objects.requireNonNull(entire, "entire");
		this.entire = entire;
	}

	public List<EventFilterConditionInfo> getConditions() {
		return conditions;
	}

	public void setConditions(List<EventFilterConditionInfo> conditions) {
		Objects.requireNonNull(conditions, "conditions");
		this.conditions = conditions;
	}

}
