/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfoPK;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeUtil;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.EventFilterInternal;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.GetEventFilterInternal;
import com.clustercontrol.monitor.bean.UpdateEventFilterInternal;
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
	private static Object lock = new Object();
	
	/** 「含まない」検索を行うかの判断に使う値 */
	private static final String SEARCH_PARAM_NOT_INCLUDE = "NOT:";
	
	/** 任意の文字列(何も無くても良い) */
	private static final String SEARCH_PARAM_PART_MATCH = "%";
	
	/** 任意の一文字 */
	private static final String SEARCH_PARAM_ONCE_MATCH = "_";

	
	public static void initEventCache() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			eventCache = new ConcurrentSkipListSet<EventLogEntity>(new Comparator<EventLogEntity>() {
				@Override
				public int compare(EventLogEntity o1, EventLogEntity o2) {
					int result = 0;
					Long compareValue = - o1.getId().getOutputDate() + o2.getId().getOutputDate();
					
					if (compareValue > 0) {
						result = 1;
					} else if (compareValue < 0){
						result = -1;
					}
					return result;
				}
			});
			
			em.clear();
			for (EventLogEntity e : QueryUtil.getEventLogByFilter(null, false, getEventCacheLimit())) {
				addEventCache(e);
			}
			if (eventCache.size() == getEventCacheLimit()) {
				eventCacheFull = true;
			} else {
				eventCacheFull = false;
			}
			m_log.info("eventCacheFull=" + eventCacheFull);
		}
	}
	
	private static int getEventCacheLimit() {
		return HinemosPropertyCommon.notify_event_cache_size.getIntegerValue();
	}

	public static EventLogEntity cloneWithoutOrg (EventLogEntity entity) {
		EventLogEntity ret = entity.clone();
		ret.setMessageOrg(null);
		return ret;
	}
	public static void addEventCache(EventLogEntity e) {
		
		eventCache.add(cloneWithoutOrg(e));
		m_log.trace("add=" + e.getId());
		
		synchronized (lock) {
			int size = eventCache.size();
			for (int i = 0; i < size - getEventCacheLimit() && i < 100; i ++) {
				EventLogEntity last = eventCache.pollLast();
				m_log.trace("last=" + last.getId());
				eventCacheFull = true;
			}
		}
		if (m_log.isTraceEnabled()) {
			long now = HinemosTime.currentTimeMillis();
			StringBuilder sb = new StringBuilder();
			for (EventLogEntity ee : eventCache) {
				sb.append("outputDate=");
				sb.append(now - ee.getId().getOutputDate());
				sb.append("\n");
			}
			m_log.trace(sb.toString());
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
	
	/**
	 * 確認状態の一括変更の結果を、キャッシュに反映する
	 * 
	 * @param filter
	 * @param confirmFlg
	 * @param confirmDate
	 * @param confirmUser
	 */
	public static void confirmEventCache(
			UpdateEventFilterInternal filter, Integer confirmFlg, Long confirmDate, String confirmUser) {
		for (EventLogEntity entity : eventCache) {
			
			if (!filterCheck(entity, filter)) {
				//更新条件に一致したキャッシュであることをチェックする
				continue;
			}
			
			// 更新権限がない場合は処理をしない
			try {
				ObjectPrivilegeUtil.getObjectPrivilegeObject("MON", entity.getId().getMonitorId(), ObjectPrivilegeMode.MODIFY);
			} catch (JobMasterNotFound | ObjectPrivilege_InvalidRole e) {
				continue;
			}
			entity.setConfirmFlg(confirmFlg);
			if (confirmFlg == ConfirmConstant.TYPE_CONFIRMED || 
					confirmFlg == ConfirmConstant.TYPE_CONFIRMING){
				entity.setConfirmDate(confirmDate);
			}
			entity.setConfirmUser(confirmUser);
		}
	}
	
	public static List<EventLogEntity> getEventListByCache(
			GetEventFilterInternal filter, Boolean orderByFlg,Integer limit) {
		ArrayList<EventLogEntity> ret = new ArrayList<>();
		for (EventLogEntity entity : eventCache) {
			
			if (!filterCheck(entity, filter)) {
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
			EventFilterInternal<?> filter
			) {
		
		if (!filterCheckCommon(entity, filter)) {
			//Get/UPDATE共通のフィルタ内容をチェックする
			return false;
		}
		
		if ((filter instanceof GetEventFilterInternal)) {
			//Get固有のフィルタをチェックする
			if (!filterCheckGet(entity, (GetEventFilterInternal) filter)) { 
				return false;
			}
		}
		
		return checkAuthority(entity);
	}
	
	private static boolean filterCheckCommon(EventLogEntity entity, EventFilterInternal<?> filter){
		// ファシリティID
		if (filter.getFacilityIdList() != null 
				&& !filter.getFacilityIdList().contains(entity.getId().getFacilityId())) {
			return false;
		}
		// 重要度
		if (filter.getPriorityList() != null 
				&& !filter.getPriorityList().contains(entity.getPriority())) {
			return false;
		}
		// 受信日時（自）
		if (isGreater(filter.getOutputFromDate(), entity.getId().getOutputDate())) {
			return false;
		}
		// 受信日時（至）
		if (isLess(filter.getOutputToDate() ,entity.getId().getOutputDate())) {
			return false;
		}
		// 出力日時（自）
		if (isGreater(filter.getGenerationFromDate(), entity.getGenerationDate())) {
			return false;
		}
		// 出力日時（至）
		if (isLess(filter.getGenerationToDate() , entity.getGenerationDate())) {
			return false;
		}
		
		// TODO QueryUtil.getEventLogByFilterと同じ実装にすること
		// TODO LIKEと同じ処理を実装すること
		
		// 監視項目ID
		if (!matchLike(filter.getMonitorId(), entity.getId().getMonitorId())) {
			return false;
		}
		// 監視詳細
		if (!matchLike(filter.getMonitorDetailId(), entity.getId().getMonitorDetailId())) {
			return false;
		}
		// アプリケーション
		if (!matchLike(filter.getApplication(), entity.getApplication())) {
			return false;
		}
		// メッセージ
		if (!matchLike(filter.getMessage(), entity.getMessage())) {
			return false;
		}
		
		// コメント
		if (!matchLike(filter.getComment(), entity.getComment())) {
			return false;
		}
		// コメントユーザ
		if (!matchLike(filter.getCommentUser(), entity.getCommentUser())) {
			return false;
		}
		
		// 性能グラフ用フラグ(nullはすべて)
		if (!matchBoolean(filter.getCollectGraphFlg(), entity.getCollectGraphFlg())) {
			return false;
		}
		
		return true;
	}
	
	private static boolean filterCheckGet(EventLogEntity entity, GetEventFilterInternal filter){
		
		// イベント番号（自）
		if (isGreater(filter.getPositionFrom(), entity.getPosition())) {
			return false;
		}
		// イベント番号（至）
		if (isLess(filter.getPositionTo() , entity.getPosition())) {
			return false;
		}
		// 確認リスト
		if (!matchList(filter.getConfirmFlgList(), entity.getConfirmFlg())) {
			return false;
		}
		// 確認ユーザ
		if (!matchLike(filter.getConfirmUser(), entity.getConfirmUser())) {
			return false;
		}
		// オーナーロールID
		if (!matchLike(filter.getOwnerRoleId(), entity.getOwnerRoleId())) {
			return false;
		}
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			//ユーザ項目
			String filterValue = EventUtil.getUserItemValue(filter, i);
			String entityValue = EventUtil.getUserItemValue(entity, i);
			if (entityValue == null) {
				entityValue = "";
			}
			
			if (!matchLike(filterValue, entityValue)) {
				return false;
			}
		}
		
		return true;
	}
	
	//権限のチェック
	private static boolean checkAuthority(EventLogEntity entity) {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			// ADMINISTRATOR 所属のユーザの場合、オーナーロール、オブジェクト権限のチェックは行わず表示する
			Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
			if (isAdministrator != null && isAdministrator) {
				return true;
			}
			
			// イベント履歴のオーナーロールに所属していれば表示する
			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			List<String> roleIdList = UserRoleCache.getRoleIdList(loginUser);
			if (roleIdList.contains(entity.getOwnerRoleId())) {
				return true;
			}
			
			// 通知元が監視設定の場合、オブジェクト権限のチェックまで実施する
			String pluginId = entity.getId().getPluginId();
			if(pluginId.startsWith(HinemosModuleConstant.MONITOR)) {
				for (String roleId : roleIdList) {
					ObjectPrivilegeInfoPK objectPrivilegeEntityPK = 
							new ObjectPrivilegeInfoPK(HinemosModuleConstant.MONITOR, entity.getId().getMonitorId(), 
									roleId, ObjectPrivilegeMode.READ.name());
					ObjectPrivilegeInfo objectPrivilegeEntity = em.find(
							ObjectPrivilegeInfo.class, objectPrivilegeEntityPK, ObjectPrivilegeMode.READ);
					if (objectPrivilegeEntity != null) {
						m_log.debug("filterCheck() ObjectPrivilegeInfo=" + objectPrivilegeEntity.getId().toString());
						return true;
					}
				}
			}
		} catch (Exception e) {
			m_log.debug("getEventList : " + e.getClass().getName() + ", "+ e.getMessage());
			return false;
		}
		return false;
	}
	
	public static void setEventRange(ViewListInfo ret) {
		if (eventCacheFull) {
			ret.setFromOutputDate(eventCache.last().getId().getOutputDate());
		} else {
			ret.setFromOutputDate(null);
		}
		if (eventCache.size() > 0) {
			ret.setToOutputDate(eventCache.first().getId().getOutputDate());
		} else {
			ret.setToOutputDate(null);
		}
		
	}

	private static boolean isLess(Long filter, Long check) {
		if (filter == null) {
			return false;
		}
		if (filter < check) {
			return true;
		}
		return false;
	}
	
	private static boolean isGreater(Long filter, Long check) {
		if (filter == null) {
			return false;
		}
		if (filter > check) {
			return true;
		}
		return false;
	}
	

	
	private static <T> boolean matchList(List<T> filter, T check) {
		if (filter == null) {
			return true;
		}
		return filter.contains(check);
	}
	
	private static boolean matchBoolean(Boolean filter, Boolean check) {
		if (filter == null) {
			return true;
		}
		
		return filter.equals(check);
		
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
		if (filter == null){
			//フィルタで指定がない場合、true
			return true;
		}
		
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
