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
import com.clustercontrol.filtersetting.bean.EventFilterBaseCriteria;
import com.clustercontrol.filtersetting.bean.EventFilterBaseInfo;
import com.clustercontrol.filtersetting.bean.EventFilterConditionCriteria;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
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

	public static EventLogEntity cloneEntity (EventLogEntity entity) {
		EventLogEntity ret = entity.clone();
		//キャッシュからオリジナルメッセージを除外する
		ret.setMessageOrg(null);
		//ユーザー拡張イベントのキャッシュサイズを調整する
		adjustUserItemSize(ret);
		
		return ret;
	}
	
	/**
	 * Hinemosプロパティ"monitor.event.useritem.itemXX.cache.size"の設定値をもとに、
	 * 各ユーザー拡張イベント(40項目)のキャッシュサイズを調整する
	 * 
	 * @param entity
	 */
	private static void adjustUserItemSize(EventLogEntity entity) {
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			String userItemValue = EventUtil.getUserItemValue(entity, i);
			int cacheLength = HinemosPropertyCommon.monitor_event_useritem_item$_cache_size.getIntegerValue(String.format("%02d", i));
			
			if (userItemValue != null && userItemValue.length() > cacheLength) {
				//ユーザ拡張イベントの文字列の長さがHinemosプロパティの設定値を超えている場合、
				//Hinemosプロパティの設定値までをキャッシュとして保持する
				EventUtil.setUserItemValue(entity, i, userItemValue.substring(0, cacheLength));
			}
		}
	}

	public static void addEventCache(EventLogEntity e) {
		eventCache.add(cloneEntity(e));
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
		EventLogEntity newEntity = cloneEntity(e);
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
			EventFilterBaseInfo filter, Integer confirmFlg, Long confirmDate, String confirmUser) {

		EventFilterBaseCriteria base = filter.createCriteria(Integer.MAX_VALUE, "a").get(0);
		List<EventFilterConditionCriteria> conds = filter.createConditionsCriteria("a");

		for (EventLogEntity entity : eventCache) {
			if (!checkEntity(entity, base, conds)) continue;
			
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
			EventFilterBaseInfo filter, Boolean orderByFlg, Integer limit) {
		ArrayList<EventLogEntity> ret = new ArrayList<>();

		// クエリではなくJavaでの処理なので、ファシリティID分割は不要
		EventFilterBaseCriteria base = filter.createCriteria(Integer.MAX_VALUE, "a").get(0);
		List<EventFilterConditionCriteria> conds = filter.createConditionsCriteria("a");

		for (EventLogEntity entity : eventCache) {
			if (!checkEntity(entity, base, conds)) continue;

			ret.add(entity);
			if (ret.size() == limit) {
				break;
			}
		}
		return ret;
	}

	private static boolean checkEntity(EventLogEntity entity, EventFilterBaseCriteria base, List<EventFilterConditionCriteria> conds) {
		if (!base.matches(entity)) return false;
		// ConditionCriteria はOR結合なので、どれか一つとマッチすればOK
		for (EventFilterConditionCriteria cnd : conds) {
			if (cnd.matches(entity) && checkAuthority(entity)) return true;
		}
		return false;
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
}
