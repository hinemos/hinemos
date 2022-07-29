/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.filtersetting.bean;

import com.clustercontrol.commons.util.QueryCriteria;
import com.clustercontrol.monitor.run.util.EventUtil;
import com.clustercontrol.notify.monitor.model.EventLogEntity;

/**
 * {@link EventFilterConditionInfo} に対応する {@link QueryCriteria} です。
 */
public class EventFilterConditionCriteria extends QueryCriteria {
	public In<Integer> priority;
	public Period outputDate;
	public Period generationDate;
	public Like monitorId;
	public Like monitorDetail;
	public Like application;
	public Like message;
	public Like comment;
	public Like commentUser;
	public Equal<Boolean> graphFlag;
	public In<Integer> confirmFlag;
	public Like confirmUser;
	public Like ownerRoleId;
	public NumberedLike userItems;
	public Range<Long> position;
	public Equal<String> notifyUUID;

	public EventFilterConditionCriteria(String uniqueId, String eventLogAlias) {
		super(uniqueId);

		// 短い名前の変数へ
		String a = eventLogAlias;

		priority = new In<>(a + ".priority", EventFilterConditionInfo.PRIORITY_VARIATION);
		outputDate = new Period(a + ".id.outputDate");
		generationDate = new Period(a + ".generationDate");
		monitorId = new Like(a + ".id.monitorId");
		monitorDetail = new Like(a + ".id.monitorDetailId");
		application = new Like(a + ".application");
		message = new Like(a + ".message");
		comment = new Like(a + ".comment");
		commentUser = new Like(a + ".commentUser");
		graphFlag = new Equal<>(a + ".collectGraphFlg");
		confirmFlag = new In<>(a + ".confirmFlg", EventFilterConditionInfo.CONFIRM_VARIATION);
		confirmUser = new Like(a + ".confirmUser");
		ownerRoleId = new Like(a + ".ownerRoleId");
		userItems = new NumberedLike(a + ".userItem%02d");
		position = new Range<>(a + ".position");
		notifyUUID = new Equal<>(a + ".notifyUUID");
	}

	/**
	 * イベント履歴のエンティティがこの条件を満たす場合は true を、そうでなければ false を返します。
	 */
	public boolean matches(EventLogEntity event) {
		boolean r = priority.contains(event.getPriority())
				&& outputDate.contains(event.getId().getOutputDate())
				&& generationDate.contains(event.getGenerationDate())
				&& monitorId.matches(event.getId().getMonitorId())
				&& monitorDetail.matches(event.getId().getMonitorDetailId())
				&& application.matches(event.getApplication())
				&& message.matches(event.getMessage())
				&& comment.matches(event.getComment())
				&& commentUser.matches(event.getCommentUser())
				&& graphFlag.isEqualTo(event.getCollectGraphFlg())
				&& confirmFlag.contains(event.getConfirmFlg())
				&& confirmUser.matches(event.getConfirmUser())
				&& ownerRoleId.matches(event.getOwnerRoleId())
				&& position.contains(event.getPosition())
				&& notifyUUID.isEqualTo(event.getNotifyUUID());

		// ユーザ項目の比較はこの時点で true の場合のみでよい (短絡評価)
		if (r) {
			for (int n : userItems.getNumbers()) {
				// 不一致が見つかり次第、ループ中止
				if (!userItems.matches(n, EventUtil.getUserItemValue(event, n))) {
					r = false;
					break;
				}
			}
		}
		return isNegative() ? !r : r;
	}
}