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
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;

/**
 * ジョブ実行履歴フィルタ基本情報。
 */
public class JobHistoryFilterBaseInfo {

	/** フィルタ詳細情報リスト */
	private List<JobHistoryFilterConditionInfo> conditions = new ArrayList<>();

	/**
	 * 全件表示用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static JobHistoryFilterBaseInfo ofAllHistories() {
		JobHistoryFilterBaseInfo o = new JobHistoryFilterBaseInfo();
		o.conditions.add(JobHistoryFilterConditionInfo.ofAllHistories());
		return o;
	}

	/**
	 * クライアントビュー表示用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static JobHistoryFilterBaseInfo ofClientViewDefault() {
		JobHistoryFilterBaseInfo o = new JobHistoryFilterBaseInfo();
		o.conditions.add(JobHistoryFilterConditionInfo.ofClientViewDefault());
		return o;
	}

	public JobHistoryFilterBaseInfo() {
		// NOP
	}

	public JobHistoryFilterBaseInfo(FilterEntity entity) {
		for (FilterConditionEntity condEntity : entity.getConditions()) {
			JobHistoryFilterConditionInfo cond = new JobHistoryFilterConditionInfo(condEntity);
			this.conditions.add(cond);
		}
	}

	/**
	 * 保持内容を指定されたエンティティへ書き込みます。
	 */
	public void writeTo(FilterEntity entity) {
		entity.setConditions(new ArrayList<>());

		for (JobHistoryFilterConditionInfo cond : conditions) {
			FilterConditionEntityPK condPK = new FilterConditionEntityPK(entity.getId(), entity.getConditions().size());
			FilterConditionEntity condEntity = cond.toEntity(condPK);
			entity.getConditions().add(condEntity);
		}
	}

	/**
	 * 紐づいている {@link JobHistoryFilterConditionInfo} のリストに対応した SQL条件式構築オブジェクトを生成します。
	 * 
	 * @param jobSessiobJobAlias JPQL内での{@link JobSessionJobEntity}のエイリアス。
	 * @param jobSessionAlias JPQL内での{@link JobSessionEntity}のエイリアス。
	 */
	public List<JobHistoryFilterConditionCriteria> createConditionsCriteria(
			String jobSessiobJobAlias, String jobSessionAlias) {
		List<JobHistoryFilterConditionCriteria> rtn = new ArrayList<>();
		for (JobHistoryFilterConditionInfo cnd : conditions) {
			rtn.add(cnd.createCriteria("C" + rtn.size() + "_", jobSessiobJobAlias, jobSessionAlias));
		}
		return rtn;
	}

	public List<JobHistoryFilterConditionInfo> getConditions() {
		return conditions;
	}

	public void setConditions(List<JobHistoryFilterConditionInfo> conditions) {
		this.conditions = conditions;
	}

}
