/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */


package com.clustercontrol.monitor.run.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeUtil;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.ViewListInfo;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

public class EventCache {
	private static Log m_log = LogFactory.getLog( EventCache.class );

	/*
	 * ConcurrentSkipListSetの特徴
	 * ・ソートしてくれる
	 * ・synchronized は不要
	 * ・size() は重い処理
	 */
	private static ConcurrentSkipListSet<EventLogEntity> eventCache = null;
	private static boolean eventCacheFull = false;
	
	/** 「含まない」検索を行うかの判断に使う値 */
	private static final String SEARCH_PARAM_NOT_INCLUDE = "NOT:";
	
	/** 任意の文字列(何も無くても良い) */
	private static final String SEARCH_PARAM_PART_MATCH = "%";
	
	/** 任意の一文字 */
	private static final String SEARCH_PARAM_ONCE_MATCH = "_";

	
	public static void initEventCache() {
		eventCache = new ConcurrentSkipListSet<EventLogEntity>(new Comparator<EventLogEntity>() {
			@Override
			public int compare(EventLogEntity o1, EventLogEntity o2) {
				return (int) (- o1.getId().getOutputDate() + o2.getId().getOutputDate());
			}
		});
		for (EventLogEntity e : QueryUtil.getEventLogByFilter(null, null, null, null,
				null, null, null, null, null, null, null, null, null, null, null, null,
				false, getEventCacheLimit())) {
			addEventCache(e);
		}
		if (eventCache.size() == getEventCacheLimit()) {
			eventCacheFull = true;
		} else {
			eventCacheFull = false;
		}
		m_log.info("eventCacheFull=" + eventCacheFull);
	}
	
	private static int getEventCacheLimit() {
		return HinemosPropertyUtil.getHinemosPropertyNum("notify.event.cache.size", Long.valueOf(10000)).intValue();
	}

	public static EventLogEntity cloneWithoutOrg (EventLogEntity entity) {
		EventLogEntity ret = entity.clone();
		ret.setMessageOrg(null);
		return ret;
	}
	public static void addEventCache(EventLogEntity e) {
		eventCache.add(cloneWithoutOrg(e));
		m_log.trace("add=" + e.getId());
		while (eventCache.size() > getEventCacheLimit()) { // .size()はそこそこ重い処理
			EventLogEntity last = eventCache.last();
			m_log.trace("last=" + last.getId());
			eventCache.remove(last);
			eventCacheFull = true;
		}
		if (m_log.isTraceEnabled()) {
			long now = HinemosTime.currentTimeMillis();
			String str = "";
			for (EventLogEntity ee : eventCache) {
				str += "outputDate=" + (now - ee.getId().getOutputDate()) + "\n";
			}
			m_log.trace(str);
		}
	}
	
	public static void modifyEventCache(EventLogEntity e) {
		EventLogEntity newEntity = cloneWithoutOrg(e);
		for (EventLogEntity entity : eventCache) {
			if (entity.getId().equals(newEntity.getId())) {
				eventCache.remove(entity);
				eventCache.add(newEntity);
				return;
			}
		}
		if (eventCacheFull) {
			m_log.info("modifyEvenCache : out of cache");
		} else {
			m_log.warn("modifyEvenCache : failed");
		}
	}
	
	/**
	 * 
	 * @param generationDate
	 * @param removeAllFlg confirmFlgを意識しない全削除:true、confirmFlgが1(確認)のものを削除:false
	 * @param ownerRoleId
	 */
	public static void removeEventCache(long generationDate, boolean removeAllFlg, String ownerRoleId) {
		for (EventLogEntity entity : eventCache) {
			if (entity.getGenerationDate() < generationDate) {
				if (!removeAllFlg && entity.getConfirmFlg().intValue() != ConfirmConstant.TYPE_CONFIRMED) {
					// removeAllFlgがfalse、かつ、イベント履歴の承認フラグが1(確認)以外の場合は消さない
					continue;
				}
				if (ownerRoleId != null && !entity.getOwnerRoleId().equals(ownerRoleId)) {
					// ownerRoleIdがチェック対象、かつ、ownerRoleIdが異なる場合は消さない
					continue;
				}
				eventCache.remove(entity);
				eventCacheFull = false;
			}
		}
		m_log.debug("removeEventCache end");
	}
	
	public static void confirmEventCache(
			List<String> facilityIdList,
			List<Integer> priorityList,
			Long outputFromDate,
			Long outputToDate,
			Long generationFromDate,
			Long generationToDate,
			String monitorId,
			String monitorDetailId,
			String application,
			String message,
			Integer confirmFlg,
			String confirmUser,
			String comment,
			String commentUser,
			Integer cofirmType,
			Long confirmDate,
			Boolean collectGraphFlg,
			String ownerRoleId) {
		for (EventLogEntity entity : eventCache) {
			
			// 承認フラグと承認ユーザは指定しない
			if (!filterCheck(entity, facilityIdList, priorityList, outputFromDate, outputToDate,
					generationFromDate, generationToDate, monitorId, monitorDetailId, application,
					message, null, null, comment, commentUser, collectGraphFlg, ownerRoleId)) {
				continue;
			}

			// 更新権限がない場合は処理をしない
			try {
				ObjectPrivilegeUtil.getObjectPrivilegeObject("MON", entity.getId().getMonitorId(), ObjectPrivilegeMode.MODIFY);
			} catch (JobMasterNotFound | ObjectPrivilege_InvalidRole e) {
				continue;
			}
			entity.setConfirmFlg(confirmFlg);
			entity.setConfirmDate(confirmDate);
			entity.setConfirmUser(confirmUser);
		}
	}
	
	public static List<EventLogEntity> getEventListByCache(
			List<String> facilityIdList,
			List<Integer> priorityList,
			Long outputFromDate,
			Long outputToDate,
			Long generationFromDate,
			Long generationToDate,
			String monitorId,
			String monitorDetailId,
			String application,
			String message,
			Integer confirmFlg,
			String confirmUser,
			String comment,
			String commentUser,
			Boolean collectGraphFlg,
			String ownerRoleId,
			Boolean orderByFlg,
			Integer limit) {
		ArrayList<EventLogEntity> ret = new ArrayList<>();
		for (EventLogEntity entity : eventCache) {
			
			if (!filterCheck(entity, facilityIdList, priorityList, outputFromDate, outputToDate,
					generationFromDate, generationToDate, monitorId, monitorDetailId, application,
					message, confirmFlg, confirmUser, comment, commentUser, collectGraphFlg, ownerRoleId)) {
				continue;
			}
			
			
			ret.add(entity);
			if (ret.size() == limit) {
				break;
			}
		}
		return ret;
	}
	
	/**
	 * フィルタ条件にマッチしていたら、trueを返す
	 */
	private static boolean filterCheck (
			EventLogEntity entity,
			List<String> facilityIdList,
			List<Integer> priorityList,
			Long outputFromDate,
			Long outputToDate,
			Long generationFromDate,
			Long generationToDate,
			String monitorId,
			String monitorDetailId,
			String application,
			String message,
			Integer confirmFlg,
			String confirmUser,
			String comment,
			String commentUser,
			Boolean collectGraphFlg,
			String ownerRoleId) {
		
		// ファシリティID
		if (facilityIdList != null && !facilityIdList.contains(entity.getId().getFacilityId())) {
			return false;
		}
		// 重要度
		if (priorityList != null && !priorityList.contains(entity.getPriority())) {
			return false;
		}
		// 受信日時（自）
		if (outputFromDate != null && entity.getId().getOutputDate() < outputFromDate) {
			return false;
		}
		// 受信日時（至）
		if (outputToDate != null && outputToDate < entity.getId().getOutputDate()) {
			return false;
		}
		// 出力日時（自）
		if (generationFromDate != null && entity.getGenerationDate() < generationFromDate) {
			return false;
		}
		// 出力日時（至）
		if (generationToDate != null && generationToDate < entity.getGenerationDate()) {
			return false;
		}
		
		// TODO QueryUtil.getEventLogByFilterと同じ実装にすること
		// TODO LIKEと同じ処理を実装すること
		
		// 監視項目ID
		if (monitorId != null && !matchLike(monitorId, entity.getId().getMonitorId())) {
			return false;
		}
		// 監視詳細
		if (monitorDetailId != null && !matchLike(monitorDetailId, entity.getId().getMonitorDetailId())) {
			return false;
		}
		// アプリケーション
		if (application != null && !matchLike(application, entity.getApplication())) {
			return false;
		}
		// メッセージ
		if (message != null && !matchLike(message, entity.getMessage())) {
			return false;
		}
		// 確認有無(confirmFlg=nullはすべて)
		if (confirmFlg != null && !entity.getConfirmFlg().equals(confirmFlg)) {
			return false;
		}
		// 確認ユーザ
		if (confirmUser != null && !matchLike(confirmUser, entity.getConfirmUser())) {
			return false;
		}
		//コメント
		if (comment != null && !matchLike(comment, entity.getComment())) {
			return false;
		}
		//コメントユーザ
		if (commentUser != null && !matchLike(commentUser, entity.getCommentUser())) {
			return false;
		}
		//オーナーロールID
		if (ownerRoleId != null && !matchLike(ownerRoleId, entity.getOwnerRoleId())) {
			return false;
		}
		// 性能グラフ用フラグ(nullはすべて)
		if (collectGraphFlg != null && !entity.getCollectGraphFlg().equals(collectGraphFlg)) {
			return false;
		}
		try {
			ObjectPrivilegeUtil.getObjectPrivilegeObject("MON", entity.getId().getMonitorId(), ObjectPrivilegeMode.READ);
		} catch (JobMasterNotFound | ObjectPrivilege_InvalidRole e) {
			m_log.debug("getEventList : " + e.getClass().getName() + ", "+ e.getMessage());
			return false;
		}
		return true;
	}
	
	public static void setEventRange(ViewListInfo ret) {
		if (eventCacheFull) {
			ret.setFromOutputDate(eventCache.last().getId().getOutputDate());
		} else {
			ret.setFromOutputDate(null);
		}
		ret.setToOutputDate(eventCache.first().getId().getOutputDate());
	}

	/**
	 * フィルターで指定された文言が一致するかどうかを返します。<br>
	 * (SQLのlikeと同じ。[%」と「_」と「:NOT」に対応。)
	 * 
	 * @param filter
	 * @param check
	 * @return
	 */
	private static boolean matchLike(String filter, String check) {
		String filter2 = filter;
		boolean notInc = false;
		if (filter2.startsWith(SEARCH_PARAM_NOT_INCLUDE)) {
			notInc = true;
			filter2 = filter2.substring(SEARCH_PARAM_NOT_INCLUDE.length(), filter2.length());
		}
		if (filter2.contains(SEARCH_PARAM_PART_MATCH) || filter2.contains(SEARCH_PARAM_ONCE_MATCH)) {
			filter2 = Pattern.quote(filter2);
			filter2 = filter2.replace(SEARCH_PARAM_PART_MATCH, "\\E" + SEARCH_PARAM_PART_MATCH + "\\Q");
			filter2 = filter2.replace(SEARCH_PARAM_PART_MATCH, ".*");
			filter2 = filter2.replace(SEARCH_PARAM_ONCE_MATCH, "\\E" + SEARCH_PARAM_ONCE_MATCH + "\\Q");
			filter2 = filter2.replace(SEARCH_PARAM_ONCE_MATCH, ".");
			if (check.matches(filter2)) {
				if (notInc) {
					return false;
				}
			} else {
				if (!notInc) {
					return false;
				}
			}
		} else {
			if (!filter2.equals(check)) {
				if (!notInc) {
					return false;
				}
			} else {
				if (notInc) {
					return false;
				}
			}
		}
		return true;
	}
}
