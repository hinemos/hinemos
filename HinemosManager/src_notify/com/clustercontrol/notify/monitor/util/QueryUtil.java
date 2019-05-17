/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.monitor.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.EventFilterInternal;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.GetEventFilterInternal;
import com.clustercontrol.monitor.bean.UpdateEventFilterInternal;
import com.clustercontrol.monitor.factory.ModifyEventInfo;
import com.clustercontrol.monitor.run.util.EventUtil;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.model.EventLogEntityPK;
import com.clustercontrol.notify.monitor.model.EventLogOperationHistoryEntity;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;
import com.clustercontrol.notify.monitor.model.StatusInfoEntityPK;
import com.clustercontrol.util.HinemosTime;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	private final static int MONITOR_HISTORY_FACILITY_ID_MAX_COUNT = 1000;
	
	public static StatusInfoEntity getStatusInfoPK(StatusInfoEntityPK pk) throws MonitorNotFound, InvalidRole {
		return getStatusInfoPK(pk, ObjectPrivilegeMode.READ);
	}

	public static StatusInfoEntity getStatusInfoPK(StatusInfoEntityPK pk, ObjectPrivilegeMode mode) throws MonitorNotFound, InvalidRole {
		StatusInfoEntity entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(StatusInfoEntity.class, pk, mode);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("StatusInfoEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getStatusInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getStatusInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static StatusInfoEntity getStatusInfoPK(String facilityId,
			String monitorId,
			String monitorDetailId,
			String pluginId) throws MonitorNotFound, InvalidRole {
		return getStatusInfoPK(new StatusInfoEntityPK(facilityId, monitorId, monitorDetailId, pluginId));
	}

	public static StatusInfoEntity getStatusInfoPK(String facilityId,
			String monitorId,
			String monitorDetailId,
			String pluginId,
			ObjectPrivilegeMode mode) throws MonitorNotFound, InvalidRole {
		return getStatusInfoPK(new StatusInfoEntityPK(facilityId, monitorId, monitorDetailId, pluginId), mode);
	}

	public static List<StatusInfoEntity> getStatusInfoByExpirationStatus(Long expirationDate) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<StatusInfoEntity> list
			= em.createNamedQuery("StatusInfoEntity.findExpirationStatus", StatusInfoEntity.class)
			.setParameter("expirationDate", expirationDate).getResultList();
			return list;
		}
	}

	public static EventLogEntity getEventLogPK(EventLogEntityPK pk) throws EventLogNotFound, InvalidRole {
		return getEventLogPK(pk, ObjectPrivilegeMode.READ);
	}

	public static EventLogEntity getEventLogPK(EventLogEntityPK pk, ObjectPrivilegeMode mode) throws EventLogNotFound, InvalidRole {
		EventLogEntity entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(EventLogEntity.class, pk, mode);
			if (entity == null) {
				EventLogNotFound e = new EventLogNotFound("EventLogEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getEventLogPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getEventLogPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}
	
	public static EventLogEntity getEventLogPK(String monitorId,
			String monitorDetailId,
			String pluginId,
			Long outputDate,
			String facilityId) throws EventLogNotFound, InvalidRole {
		return getEventLogPK(new EventLogEntityPK(monitorId, monitorDetailId, pluginId, outputDate, facilityId));
	}

	public static EventLogEntity getEventLogPK(String monitorId,
			String monitorDetailId,
			String pluginId,
			Long outputDate,
			String facilityId,
			ObjectPrivilegeMode mode) throws EventLogNotFound, InvalidRole {
		return getEventLogPK(new EventLogEntityPK(monitorId, monitorDetailId, pluginId, outputDate, facilityId), mode);
	}
	
	public static int updateEventLogFlgByFilter(
			UpdateEventFilterInternal filter,
			Integer confirmType,
			String confirmUser,
			Long confirmDate) {

		int rtn = 0;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			// SQL作成
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT a FROM EventLogEntity a WHERE true = true");
			
			// 共通項目のappend
			appendJpqlStringForEventLogByFilter(sbJpql, filter);
			
			// 確認有無
			sbJpql.append(" AND a.confirmFlg <> :confirmFlg ");
			
			sbJpql.append(" ORDER BY a.id.outputDate");
			
			String[] facilityIds = null;
			if (filter.getFacilityIdList() != null) {
				facilityIds = filter.getFacilityIdList().toArray(new String[0]);
			}
				
			if (facilityIds == null || facilityIds.length <= 0) {
				
				String facilityIdJpqlStr =  "";
				
				rtn += updateEventLogFlgByFilterImpl(
						jtm, em, sbJpql.toString(), facilityIdJpqlStr, facilityIds, filter,
						confirmType, confirmUser, confirmDate);
				
			} else {
				
				for (int i = 0; i < facilityIds.length; i += MONITOR_HISTORY_FACILITY_ID_MAX_COUNT) {
					int length = i + MONITOR_HISTORY_FACILITY_ID_MAX_COUNT;
					if (length > facilityIds.length) {
						length = facilityIds.length;
					}
					
					String [] tmpFacilityIds = Arrays.copyOfRange(facilityIds, i, length);
					
					String facilityIdJpqlStr = " AND a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", tmpFacilityIds) + ")";
					
					rtn += updateEventLogFlgByFilterImpl(
							jtm, em, sbJpql.toString(), facilityIdJpqlStr,  tmpFacilityIds, filter,
							confirmType, confirmUser, confirmDate);
				}
			}
			return rtn;
		}
	}
	
	private static int updateEventLogFlgByFilterImpl(
			JpaTransactionManager jtm, HinemosEntityManager em, String jpSql, String facilityIdJpqlStr, String[] facilityIds,
			UpdateEventFilterInternal filter, Integer confirmType, String confirmUser, Long confirmDate) {
		
		int updateVal = 0;
		
		TypedQuery<EventLogEntity> query = em.createQuery(String.format(jpSql, facilityIdJpqlStr), EventLogEntity.class, EventLogEntity.class, ObjectPrivilegeMode.MODIFY)
				.setParameter("confirmFlg", confirmType);
		
		// 共通項目のappend
		appendJpqlValueForEventLogByFilter(query, facilityIds, filter);
		
		List<EventLogEntity> eventLogList = null;
		
		int offset = 0;
		final int max = 1000;
		long startTime = 0;
		
		eventLogList = query.setFirstResult(offset).setMaxResults(max).getResultList();
		
		while (eventLogList.size() > 0) {
			startTime = HinemosTime.currentTimeMillis();
			for (EventLogEntity event : eventLogList) {
				ModifyEventInfo.setConfirmFlgChange(jtm, event, confirmType, confirmDate, confirmUser);
				event.setConfirmFlg(confirmType);
				if (confirmType == ConfirmConstant.TYPE_CONFIRMED || 
					confirmType == ConfirmConstant.TYPE_CONFIRMING){
					event.setConfirmDate(confirmDate);
				}
				event.setConfirmUser(confirmUser);
				em.merge(event);
				updateVal++;
			}
			em.flush(); // DBへの書き出し
			em.clear(); // 取得したEventLogEntityのキャッシュをクリア
			m_log.debug(String.format("updateEventLogFlgByFilterImpl() : from %d to %d rows completed in %d ms",
						offset, offset + eventLogList.size(), HinemosTime.currentTimeMillis() - startTime));
			offset += eventLogList.size();
			eventLogList = query.setFirstResult(offset).setMaxResults(max).getResultList();
		}
		return updateVal;
	}
	
	/**
	 * 
	 * @param filter 全件取得の場合はnull
	 * @param orderByFlg tureの時、outputDateの昇順
	 * @param limit
	 * @return
	 */
	public static List<EventLogEntity> getEventLogByFilter(
			GetEventFilterInternal filter, Boolean orderByFlg, Integer limit) {
		
		List<EventLogEntity> rtnList = new ArrayList<>();
		if (filter == null) {
			filter = new GetEventFilterInternal();
		}
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT a FROM EventLogEntity a WHERE true = true");

			// 共通項目のappend
			appendJpqlStringForEventLogByFilter(sbJpql, filter);
			appendJpqlStringForGetEventLogByFilter(sbJpql, filter);
			
			// ソート
			if (orderByFlg) {
				sbJpql.append(" ORDER BY a.id.outputDate");
			} else {
				sbJpql.append(" ORDER BY a.id.outputDate DESC");
			}
			
			String[] facilityIds = null;
			if (filter.getFacilityIdList() != null) {
				facilityIds = filter.getFacilityIdList().toArray(new String[0]);
			}
			
			if (facilityIds == null || facilityIds.length <= 0) {
				
				rtnList = getEventLogByFilterImpl(
						em, sbJpql.toString(), "", facilityIds, filter, limit);
				
			} else {
				List<EventLogEntity> tmpList = new ArrayList<>();
				for (int i = 0; i < facilityIds.length; i+= MONITOR_HISTORY_FACILITY_ID_MAX_COUNT) {
					int length = i + MONITOR_HISTORY_FACILITY_ID_MAX_COUNT;
					if (length > facilityIds.length) {
						length = facilityIds.length;
					}
					
					String [] tmpFacilityIds = Arrays.copyOfRange(facilityIds, i, length);
					
					String facilityIdJpqlStr = " AND a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", tmpFacilityIds) + ")";
					
					List<EventLogEntity> res = getEventLogByFilterImpl(
								em, sbJpql.toString(), facilityIdJpqlStr, tmpFacilityIds, filter, limit);
					
					tmpList.addAll(res);
				}
				// ソート処理
				Collections.sort(tmpList, new Comparator<EventLogEntity>() {
					@Override
					public int compare(EventLogEntity o1, EventLogEntity o2) {
						if (orderByFlg) {
							// outputDateの昇順
							return (o1.getId().getOutputDate().compareTo(o2.getId().getOutputDate()));
						} else {
							// outputDateの降順
							return (o2.getId().getOutputDate().compareTo(o1.getId().getOutputDate()));
						}
					}
				});
				// 最大件数にする
				if (limit != null && tmpList.size() > limit) {
					rtnList.addAll(tmpList.subList(0, limit));
				} else {
					rtnList = tmpList;
				}
			}
			return rtnList;
		}
	} 
	
	private static List<EventLogEntity> getEventLogByFilterImpl(
			HinemosEntityManager em, String jpSql, String facilityIdJpqlStr, String[] facilityIds,
			GetEventFilterInternal filter,
			Integer limit) {
		
		TypedQuery<EventLogEntity> typedQuery
			= em.createQuery(String.format(jpSql, facilityIdJpqlStr), EventLogEntity.class);
		
		// 共通項目のappend
		appendJpqlValueForEventLogByFilter(typedQuery, facilityIds, filter);
		appendJpqlValueForGetEventLogByFilter(typedQuery, filter);
		if (limit != null) {
			typedQuery = typedQuery.setMaxResults(limit);
		}
		
		return typedQuery.getResultList();
	}
	
	/**
	 * updateEventLogFlgByFilter()メソッド、getEventLogByFilter()メソッド共通のJpql作成処理
	 * 
	 * @param sbJpql
	 * @param filter
	 */
	private static void appendJpqlStringForEventLogByFilter(StringBuffer sbJpql, EventFilterInternal<?> filter) {

		// ファシリティID設定
		sbJpql.append("%s");
		
		if (filter == null) {
			return;
		}
		
		//重要度 設定
		appendInCondition(sbJpql, filter.getPriorityList(), PriorityConstant.PRIORITY_LIST.length, "priority");
		//受信日時（自） 設定
		appendWhereCondition(sbJpql, filter.getOutputFromDate(), "id.outputDate", "outputFromDate", " >= ");
		//受信日時（至） 設定
		appendWhereCondition(sbJpql, filter.getOutputToDate(), "id.outputDate", "outputToDate", " <= ");
		//出力日時（自） 設定
		appendWhereCondition(sbJpql, filter.getGenerationFromDate(), "generationDate", "generationFromDate", " >= ");
		//出力日時（至） 設定
		appendWhereCondition(sbJpql, filter.getGenerationToDate(), "generationDate", "generationToDate", " <= ");
		//監視項目ID 設定
		appendStringWhereCondition(sbJpql, filter.getMonitorId(), "id.monitorId", "monitorId");
		//監視詳細 設定
		appendStringWhereCondition(sbJpql, filter.getMonitorDetailId(), "id.monitorDetailId", "monitorDetailId");
		//アプリケーション 設定
		appendStringWhereCondition(sbJpql, filter.getApplication(), "application", "application");
		//メッセージ 設定
		appendStringWhereCondition(sbJpql, filter.getMessage(), "message", "message");
		//コメント 設定
		appendStringWhereCondition(sbJpql, filter.getComment(), "comment", "comment");
		//コメントユーザ 設定
		appendStringWhereCondition(sbJpql, filter.getCommentUser(), "commentUser", "commentUser");
		//性能グラフ用フラグ 設定
		appendWhereCondition(sbJpql, filter.getCollectGraphFlg(), "collectGraphFlg");
	}
	
	/**
	 * getEventLogByFilter()メソッド用のJpql作成処理
	 * 
	 * @param sbJpql
	 * @param filter
	 */
	private static void appendJpqlStringForGetEventLogByFilter(StringBuffer sbJpql, GetEventFilterInternal filter) {
		if (filter == null) {
			return;
		}
		
		final String userItemFormat = "userItem%02d";
		
		// 確認状態
		appendInCondition(sbJpql, filter.getConfirmFlgList(), ConfirmConstant.CONFIRM_LIST.length, "confirmFlg");
		// 確認ユーザ
		appendStringWhereCondition(sbJpql, filter.getConfirmUser(), "confirmUser", "confirmUser");
		//オーナーロールID
		appendStringWhereCondition(sbJpql, filter.getOwnerRoleId(), "ownerRoleId", "ownerRoleId");
		
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			String colName = String.format(userItemFormat, i);
			//ユーザ項目設定
			appendStringWhereCondition(sbJpql, EventUtil.getUserItemValue(filter, i), colName, colName);
		}
		//イベント番号（自） 設定
		appendWhereCondition(sbJpql, filter.getPositionFrom(), "position", "positionFrom", " >= ");
		//イベント番号（至） 設定
		appendWhereCondition(sbJpql, filter.getPositionTo(), "position", "positionTo", " <= ");
	}
	
	
	/**
	 * updateEventLogFlgByFilter()メソッド、getEventLogByFilter()メソッド共通のJpqlへの値設定処理
	 * 
	 * @param typedQuery
	 * @param facilityIds
	 * @param filter
	 */
	private static void appendJpqlValueForEventLogByFilter(
			TypedQuery<?> typedQuery, String[] facilityIds, EventFilterInternal<?> filter ) {
			
		// ファシリティID設定
		if(facilityIds != null && facilityIds.length > 0) {
			typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", filter.getFacilityIdList().toArray());
		}
		// 重要度設定
		appendTypedQueryParamList(typedQuery, filter.getPriorityList(), PriorityConstant.PRIORITY_LIST.length, "priority");
		// 受信日時（自）設定
		appendTypedQueryParam(typedQuery, filter.getOutputFromDate(), "outputFromDate");
		// 受信日時（至）設定
		appendTypedQueryParam(typedQuery, filter.getOutputToDate(), "outputToDate");
		// 出力日時（自）設定
		appendTypedQueryParam(typedQuery, filter.getGenerationFromDate(), "generationFromDate");
		// 出力日時（至）設定
		appendTypedQueryParam(typedQuery, filter.getGenerationToDate(), "generationToDate");
		// 監視項目ID設定
		appendTypedQueryParamString(typedQuery, filter.getMonitorId(), "monitorId");
		// 監視詳細設定
		appendTypedQueryParamString(typedQuery, filter.getMonitorDetailId(), "monitorDetailId");
		// アプリケーション設定
		appendTypedQueryParamString(typedQuery, filter.getApplication(), "application");
		// メッセージ設定
		appendTypedQueryParamString(typedQuery, filter.getMessage(), "message");
		// コメント
		appendTypedQueryParamString(typedQuery, filter.getComment(), "comment");
		// コメントユーザ
		appendTypedQueryParamString(typedQuery, filter.getCommentUser(), "commentUser");
		// 性能グラフ用フラグ
		appendTypedQueryParam(typedQuery, filter.getCollectGraphFlg(), "collectGraphFlg");
	}
	
	
	/**
	 * getEventLogByFilter()メソッド共通のJpqlへの値設定処理
	 * 
	 * @param typedQuery
	 * @param filter
	 */
	private static void appendJpqlValueForGetEventLogByFilter(TypedQuery<?> typedQuery, GetEventFilterInternal filter) {
		// 確認有無
		appendTypedQueryParamList(typedQuery, filter.getConfirmFlgList(), ConfirmConstant.CONFIRM_LIST.length, "confirmFlg");
		// 確認ユーザ
		appendTypedQueryParamString(typedQuery, filter.getConfirmUser(), "confirmUser");
		//オーナーロールID
		appendTypedQueryParamString(typedQuery, filter.getOwnerRoleId(), "ownerRoleId");
		//ユーザ項目
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			final String userItemFormat = "userItem%02d";
			appendTypedQueryParamString(typedQuery, EventUtil.getUserItemValue(filter, i), String.format(userItemFormat, i));
		}
		// イベント番号（自）設定
		appendTypedQueryParam(typedQuery, filter.getPositionFrom(), "positionFrom");
		// イベント番号（至）設定
		appendTypedQueryParam(typedQuery, filter.getPositionTo(), "positionTo");
	}
	
	private static void appendInCondition(StringBuffer sbJpql, List<?> value, int allParamsize, String colId) {
		if (value == null || value.size() == 0 || value.size() >= allParamsize) {
			return;
		}
		
		sbJpql.append(" AND a." + colId + " IN (" + 
				HinemosEntityManager.getParamNameString(colId, new String[value.size()]) + ")");
	}
	
	private static void appendWhereCondition(StringBuffer sbJpql, Object value, String colId, String paramName, String operator) {
		if (value == null) {
			return;
		}
		sbJpql.append(String.format(" AND a.%s %s :%s", colId, operator, paramName));
	}
	private static void appendWhereCondition(StringBuffer sbJpql, Object value, String colId) {
		appendWhereCondition(sbJpql, value, colId, colId, " = ");
	}
	
	private static void appendStringWhereCondition(StringBuffer sbJpql, String value, String colId, String paramName) {
		final String notInclude = "NOT:";
		
		if (value == null || "".equals(value)) {
			return;
		}
		if (!value.startsWith(notInclude)) {
			sbJpql.append(String.format(" AND a.%s like :%s", colId, paramName));
		} else {
			sbJpql.append(String.format(" AND a.%s not like :%s", colId, paramName));
		}
	}
	
	private static void appendTypedQueryParamList(TypedQuery<?> typedQuery, List<?> value, int allParamsize,String colId) {
		if (value == null || value.size() == 0 || value.size() >= allParamsize) {
			return;
		}
		
		for (int i = 0 ; i < value.size() ; i++) {
			typedQuery.setParameter(colId + i, value.get(i));
		}
	}
	
	private static void appendTypedQueryParam(TypedQuery<?> typedQuery, Object value, String colId) {
		if(value != null) {
			typedQuery.setParameter(colId, value);
		}
	}
	
	private static void appendTypedQueryParamString(TypedQuery<?> typedQuery, String value, String colId) {
		final String notInclude = "NOT:";
		if(value != null && !"".equals(value)) {
			if(!value.startsWith(notInclude)) {
				typedQuery.setParameter(colId, value);
			}
			else {
				typedQuery.setParameter(colId, value.substring(notInclude.length()));
			}
		}
	}
	
	public static List<EventLogOperationHistoryEntity> getEventLogOperationHistoryListByEventLogPK(
			String monitorId,
			String monitorDetailId,
			String pluginId,
			Long outputDate,
			String facilityId) {
		List<EventLogOperationHistoryEntity> rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			rtn = em.createNamedQuery("EventLogOperationHistoryEntity.findByEventLogPK", EventLogOperationHistoryEntity.class)
				.setParameter("monitorId", monitorId)
				.setParameter("monitorDetailId", monitorDetailId)
				.setParameter("pluginId", pluginId)
				.setParameter("outputDate", outputDate)
				.setParameter("facilityId", facilityId)
				.getResultList();
			
			return rtn;
		}
	}
	
	public static List<EventLogEntity> getEventLogByHighPriorityFilter(
			String[] facilityIds,
			Integer priority,
			Long outputFromDate,
			Long outputToDate,
			Long generationFromDate,
			Long generationToDate,
			String application,
			String message,
			Integer confirmFlg,
			String confirmUser,
			Boolean orderByFlg) {

		List<EventLogEntity> rtnList = new ArrayList<>();

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT a FROM EventLogEntity a WHERE true = true");
			// ファシリティID設定
			sbJpql.append("%s");
			// 重要度設定
			if (priority != null){
				sbJpql.append(" AND a.priority = :priority");
			}
			// 受信日時（自）設定
			if (outputFromDate != null) {
				sbJpql.append(" AND a.id.outputDate >= :outputFromDate");
			}
			// 受信日時（至）設定
			if (outputToDate != null){
				sbJpql.append(" AND a.id.outputDate <= :outputToDate");
			}
			// 出力日時（自）設定
			if(generationFromDate != null) {
				sbJpql.append(" AND a.generationDate >= :generationFromDate");
			}
			// 出力日時（至）設定
			if(generationToDate != null) {
				sbJpql.append(" AND a.generationDate <= :generationToDate");
			}
			// アプリケーション設定
			if(application != null && !"".equals(application)) {
				sbJpql.append(" AND a.application like :application");
			}
			// メッセージ設定
			if(message != null && !"".equals(message)) {
				sbJpql.append(" AND a.message like :message");
			}
			// 確認有無
			if(confirmFlg != null) {
				sbJpql.append(" AND a.confirmFlg = :confirmFlg");
			}
			// 確認ユーザ
			if(confirmUser != null) {
				sbJpql.append(" AND a.confirmUser = :confirmUser");
			}
			// ソート
			if (orderByFlg) {
				sbJpql.append(" ORDER BY a.id.outputDate DESC");
			}

			if (facilityIds == null || facilityIds.length <= 0) {

				TypedQuery<EventLogEntity> typedQuery
				= em.createQuery(String.format(sbJpql.toString(), ""), EventLogEntity.class);

				// 重要度設定
				if (priority != null){
					typedQuery = typedQuery.setParameter("priority", priority);
				}
				// 受信日時（自）設定
				if (outputFromDate != null) {
					typedQuery = typedQuery.setParameter("outputFromDate", outputFromDate);
				}
				// 受信日時（至）設定
				if (outputToDate != null){
					typedQuery = typedQuery.setParameter("outputToDate", outputToDate);
				}
				// 出力日時（自）設定
				if(generationFromDate != null) {
					typedQuery = typedQuery.setParameter("generationFromDate", generationFromDate);
				}
				// 出力日時（至）設定
				if(generationToDate != null) {
					typedQuery = typedQuery.setParameter("generationToDate", generationToDate);
				}
				// アプリケーション設定
				if(application != null && !"".equals(application)) {
					typedQuery = typedQuery.setParameter("application", application);
				}
				// メッセージ設定
				if(message != null && !"".equals(message)) {
					typedQuery = typedQuery.setParameter("message", message);
				}
				// 確認有無
				if(confirmFlg != null) {
					typedQuery = typedQuery.setParameter("confirmFlg", confirmFlg);
				}
				// 確認ユーザ
				if(confirmUser != null) {
					typedQuery = typedQuery.setParameter("confirmUser", confirmUser);
				}
				typedQuery = typedQuery.setMaxResults(1);
				rtnList = typedQuery.getResultList();
			} else {
				List<EventLogEntity> tmpList = new ArrayList<>();
				for (int i = 0; i < facilityIds.length; i+=MONITOR_HISTORY_FACILITY_ID_MAX_COUNT) {
					int length = i + MONITOR_HISTORY_FACILITY_ID_MAX_COUNT;
					if (length > facilityIds.length) {
						length = facilityIds.length;
					}
					
					String facilityIdJpqlStr = " AND a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", Arrays.copyOfRange(facilityIds, i, length)) + ")";

					TypedQuery<EventLogEntity> typedQuery
					= em.createQuery(String.format(sbJpql.toString(), facilityIdJpqlStr), EventLogEntity.class);

					// ファシリティID設定
					typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", Arrays.copyOfRange(facilityIds, i, length));
					// 重要度設定
					if (priority != null){
						typedQuery = typedQuery.setParameter("priority", priority);
					}
					// 受信日時（自）設定
					if (outputFromDate != null) {
						typedQuery = typedQuery.setParameter("outputFromDate", outputFromDate);
					}
					// 受信日時（至）設定
					if (outputToDate != null){
						typedQuery = typedQuery.setParameter("outputToDate", outputToDate);
					}
					// 出力日時（自）設定
					if(generationFromDate != null) {
						typedQuery = typedQuery.setParameter("generationFromDate", generationFromDate);
					}
					// 出力日時（至）設定
					if(generationToDate != null) {
						typedQuery = typedQuery.setParameter("generationToDate", generationToDate);
					}
					// アプリケーション設定
					if(application != null && !"".equals(application)) {
						typedQuery = typedQuery.setParameter("application", application);
					}
					// メッセージ設定
					if(message != null && !"".equals(message)) {
						typedQuery = typedQuery.setParameter("message", message);
					}
					// 確認有無
					if(confirmFlg != null) {
						typedQuery = typedQuery.setParameter("confirmFlg", confirmFlg);
					}
					// 確認ユーザ
					if(confirmUser != null) {
						typedQuery = typedQuery.setParameter("confirmUser", confirmUser);
					}
					typedQuery = typedQuery.setMaxResults(1);
					tmpList.addAll(typedQuery.getResultList());
				}
				// ソート処理
				if (orderByFlg) {
					Collections.sort(tmpList, new Comparator<EventLogEntity>() {
						@Override
						public int compare(EventLogEntity o1, EventLogEntity o2) {
							// outputDateの降順
							return (o2.getId().getOutputDate().compareTo(o1.getId().getOutputDate()));
						}
					});
				}
				// 最大件数にする
				if (tmpList.size() > 1) {
					rtnList.addAll(tmpList.subList(0, 1));
				} else {
					rtnList = tmpList;
				}
			}
			return rtnList;
		}
	}

	public static List<StatusInfoEntity> getStatusInfoByFilter(
			String[] facilityIds,
			Integer[] priorityList,
			Long outputFromDate,
			Long outputToDate,
			Long generationFromDate,
			Long generationToDate,
			String monitorId,
			String monitorDetailId,
			String application,
			String message,
			String ownerRoleId) {

		List<StatusInfoEntity> rtnList = new ArrayList<>();

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 「含まない」検索を行うかの判断に使う値
			String notInclude = "NOT:";
			
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT a FROM StatusInfoEntity a WHERE true = true");
			// ファシリティID設定
			sbJpql.append("%s");
			// 重要度設定
			if (priorityList != null && priorityList.length>0 && priorityList.length != PriorityConstant.PRIORITY_LIST.length) {
				sbJpql.append(" AND a.priority IN (" + HinemosEntityManager.getParamNameString("priority", new String[priorityList.length]) + ")"); 
			}
			// 受信日時（自）設定
			if (outputFromDate != null) {
				sbJpql.append(" AND a.outputDate >= :outputFromDate");
			}
			// 受信日時（至）設定
			if (outputToDate != null){
				sbJpql.append(" AND a.outputDate <= :outputToDate");
			}
			// 出力日時（自）設定
			if(generationFromDate != null) {
				sbJpql.append(" AND a.generationDate >= :generationFromDate");
			}
			// 出力日時（至）設定
			if(generationToDate != null) {
				sbJpql.append(" AND a.generationDate <= :generationToDate");
			}
			// 監視項目ID設定
			if(monitorId != null && !"".equals(monitorId)) {
				if(!monitorId.startsWith(notInclude)) {
					sbJpql.append(" AND a.id.monitorId like :monitorId");
				}
				else {
					sbJpql.append(" AND a.id.monitorId not like :monitorId");
				}
			}
			// 監視詳細設定
			if(monitorDetailId != null && !"".equals(monitorDetailId)) {
				if(!monitorDetailId.startsWith(notInclude)) {
					sbJpql.append(" AND a.id.monitorDetailId like :monitorDetailId");
				}
				else {
					sbJpql.append(" AND a.id.monitorDetailId not like :monitorDetailId");
				}
			}
			// アプリケーション設定
			if(application != null && !"".equals(application)) {
				if(!application.startsWith(notInclude)) {
					sbJpql.append(" AND a.application like :application");
				}
				else {
					sbJpql.append(" AND a.application not like :application");
				}
			}
			// メッセージ設定
			if(message != null && !"".equals(message)) {
				if(!message.startsWith(notInclude)) {
					sbJpql.append(" AND a.message like :message");
				}
				else {
					sbJpql.append(" AND a.message not like :message");
				}
			}
			//オーナーロールID
			if(ownerRoleId != null && !"".equals(ownerRoleId)){
				if(!ownerRoleId.startsWith(notInclude)) {
					sbJpql.append(" AND a.ownerRoleId like :ownerRoleId");
				}
				else {
					sbJpql.append(" AND a.ownerRoleId not like :ownerRoleId");
				}
			}

			if (facilityIds == null || facilityIds.length <= 0) {
				
				TypedQuery<StatusInfoEntity> typedQuery
				= em.createQuery(String.format(sbJpql.toString(), ""), StatusInfoEntity.class);

				// 重要度設定
				if (priorityList != null && priorityList.length>0 && priorityList.length != PriorityConstant.PRIORITY_LIST.length){
					int count = priorityList.length;
					if (count > 0) {
						for (int i = 0 ; i < count ; i++) {
							typedQuery = typedQuery.setParameter("priority" + i, priorityList[i]);
						}
					}
				}
				// 受信日時（自）設定
				if (outputFromDate != null) {
					typedQuery = typedQuery.setParameter("outputFromDate", outputFromDate);
				}
				// 受信日時（至）設定
				if (outputToDate != null){
					typedQuery = typedQuery.setParameter("outputToDate", outputToDate);
				}
				// 出力日時（自）設定
				if(generationFromDate != null) {
					typedQuery = typedQuery.setParameter("generationFromDate", generationFromDate);
				}
				// 出力日時（至）設定
				if(generationToDate != null) {
					typedQuery = typedQuery.setParameter("generationToDate", generationToDate);
				}
				// 監視項目ID設定
				if(monitorId != null && !"".equals(monitorId)) {
					if(!monitorId.startsWith(notInclude)) {
						typedQuery = typedQuery.setParameter("monitorId", monitorId);
					}
					else {
						typedQuery = typedQuery.setParameter("monitorId", monitorId.substring(notInclude.length()));
					}
				}
				// 監視詳細設定
				if(monitorDetailId != null && !"".equals(monitorDetailId)) {
					if(!monitorDetailId.startsWith(notInclude)) {
						typedQuery = typedQuery.setParameter("monitorDetailId", monitorDetailId);
					}
					else {
						typedQuery = typedQuery.setParameter("monitorDetailId", monitorDetailId.substring(notInclude.length()));
					}
				}
				// アプリケーション設定
				if(application != null && !"".equals(application)) {
					if(!application.startsWith(notInclude)) {
						typedQuery = typedQuery.setParameter("application", application);
					}
					else {
						typedQuery = typedQuery.setParameter("application", application.substring(notInclude.length()));
					}
				}
				// メッセージ設定
				if(message != null && !"".equals(message)) {
					if(!message.startsWith(notInclude)) {
						typedQuery = typedQuery.setParameter("message", message);
					}
					else {
						typedQuery = typedQuery.setParameter("message", message.substring(notInclude.length()));
					}
				}
				//オーナーロールID
				if(ownerRoleId != null && !"".equals(ownerRoleId)){
					if(!ownerRoleId.startsWith(notInclude)) {
						typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId);
					}
					else {
						typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId.substring(notInclude.length()));
					}
				}
				rtnList = typedQuery.getResultList();
			} else {
				for (int i = 0; i < facilityIds.length; i+=MONITOR_HISTORY_FACILITY_ID_MAX_COUNT) {
					int length = i + MONITOR_HISTORY_FACILITY_ID_MAX_COUNT;
					if (length > facilityIds.length) {
						length = facilityIds.length;
					}
					
					String facilityIdJpqlStr = " AND a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", Arrays.copyOfRange(facilityIds, i, length)) + ")";
					
					TypedQuery<StatusInfoEntity> typedQuery
					= em.createQuery(String.format(sbJpql.toString(), facilityIdJpqlStr), StatusInfoEntity.class);

					// ファシリティID設定
					typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", Arrays.copyOfRange(facilityIds, i, length));

					// 重要度設定
					if (priorityList != null && priorityList.length>0 && priorityList.length != PriorityConstant.PRIORITY_LIST.length){
						int count = priorityList.length;
						if (count > 0) {
							for (int j = 0 ; j < count ; j++) {
								typedQuery = typedQuery.setParameter("priority" + j, priorityList[j]);
							}
						}
					}
					// 受信日時（自）設定
					if (outputFromDate != null) {
						typedQuery = typedQuery.setParameter("outputFromDate", outputFromDate);
					}
					// 受信日時（至）設定
					if (outputToDate != null){
						typedQuery = typedQuery.setParameter("outputToDate", outputToDate);
					}
					// 出力日時（自）設定
					if(generationFromDate != null) {
						typedQuery = typedQuery.setParameter("generationFromDate", generationFromDate);
					}
					// 出力日時（至）設定
					if(generationToDate != null) {
						typedQuery = typedQuery.setParameter("generationToDate", generationToDate);
					}
					// 監視項目ID設定
					if(monitorId != null && !"".equals(monitorId)) {
						if(!monitorId.startsWith(notInclude)) {
							typedQuery = typedQuery.setParameter("monitorId", monitorId);
						}
						else {
							typedQuery = typedQuery.setParameter("monitorId", monitorId.substring(notInclude.length()));
						}
					}
					// 監視詳細設定
					if(monitorDetailId != null && !"".equals(monitorDetailId)) {
						if(!monitorDetailId.startsWith(notInclude)) {
							typedQuery = typedQuery.setParameter("monitorDetailId", monitorDetailId);
						}
						else {
							typedQuery = typedQuery.setParameter("monitorDetailId", monitorDetailId.substring(notInclude.length()));
						}
					}
					// アプリケーション設定
					if(application != null && !"".equals(application)) {
						if(!application.startsWith(notInclude)) {
							typedQuery = typedQuery.setParameter("application", application);
						}
						else {
							typedQuery = typedQuery.setParameter("application", application.substring(notInclude.length()));
						}
					}
					// メッセージ設定
					if(message != null && !"".equals(message)) {
						if(!message.startsWith(notInclude)) {
							typedQuery = typedQuery.setParameter("message", message);
						}
						else {
							typedQuery = typedQuery.setParameter("message", message.substring(notInclude.length()));
						}
					}
					//オーナーロールID
					if(ownerRoleId != null && !"".equals(ownerRoleId)){
						if(!ownerRoleId.startsWith(notInclude)) {
							typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId);
						}
						else {
							typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId.substring(notInclude.length()));
						}
					}
					rtnList.addAll(typedQuery.getResultList());
				}
			}
			return rtnList;
		}
	}

	public static List<StatusInfoEntity> getStatusInfoByHighPriorityFilter(
			String[] facilityIds,
			Long outputFromDate,
			Long outputToDate,
			Long generationFromDate,
			Long generationToDate,
			String application,
			String message,
			String ownerRoleId,
			boolean orderFlg) {

		List<StatusInfoEntity> rtnList = new ArrayList<>();

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT a FROM StatusInfoEntity a WHERE true = true");
			// ファシリティID設定
			sbJpql.append("%s");
			// 受信日時（自）設定
			if (outputFromDate != null) {
				sbJpql.append(" AND a.outputDate >= :outputFromDate");
			}
			// 受信日時（至）設定
			if (outputToDate != null){
				sbJpql.append(" AND a.outputDate <= :outputToDate");
			}
			// 出力日時（自）設定
			if(generationFromDate != null) {
				sbJpql.append(" AND a.generationDate >= :generationFromDate");
			}
			// 出力日時（至）設定
			if(generationToDate != null) {
				sbJpql.append(" AND a.generationDate <= :generationToDate");
			}
			// アプリケーション設定
			if(application != null && !"".equals(application)) {
				sbJpql.append(" AND a.application like :application");
			}
			// メッセージ設定
			if(message != null && !"".equals(message)) {
				sbJpql.append(" AND a.message like :message");
			}
			// オーナーロールID設定
			if(ownerRoleId != null && !"".equals(ownerRoleId)) {
				sbJpql.append(" AND a.ownerRoleId = :ownerRoleId");
			}
			if (orderFlg) {
				sbJpql.append(" ORDER BY a.priority, a.outputDate DESC");
			} else {
				sbJpql.append(" ORDER BY a.priority");
			}

			if (facilityIds == null || facilityIds.length <= 0) {

				TypedQuery<StatusInfoEntity> typedQuery
				= em.createQuery(String.format(sbJpql.toString(), ""), StatusInfoEntity.class);

				// 受信日時（自）設定
				if (outputFromDate != null) {
					typedQuery = typedQuery.setParameter("outputFromDate", outputFromDate);
				}
				// 受信日時（至）設定
				if (outputToDate != null){
					typedQuery = typedQuery.setParameter("outputToDate", outputToDate);
				}
				// 出力日時（自）設定
				if(generationFromDate != null) {
					typedQuery = typedQuery.setParameter("generationFromDate", generationFromDate);
				}
				// 出力日時（至）設定
				if(generationToDate != null) {
					typedQuery = typedQuery.setParameter("generationToDate", generationToDate);
				}
				// アプリケーション設定
				if(application != null && !"".equals(application)) {
					typedQuery = typedQuery.setParameter("application", application);
				}
				// メッセージ設定
				if(message != null && !"".equals(message)) {
					typedQuery = typedQuery.setParameter("message", message);
				}
				// オーナーロールID設定
				if(ownerRoleId != null && !"".equals(ownerRoleId)) {
					typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId);
				}
				typedQuery = typedQuery.setMaxResults(1);
				rtnList = typedQuery.getResultList();
			} else {
				List<String> facilityIdList = Arrays.asList(facilityIds);
				List<StatusInfoEntity> tmpList = new ArrayList<>();
				for (int i = 0; i < facilityIdList.size(); i+=MONITOR_HISTORY_FACILITY_ID_MAX_COUNT) {
					int length = i + MONITOR_HISTORY_FACILITY_ID_MAX_COUNT;
					if (length > facilityIdList.size()) {
						length = facilityIdList.size();
					}

					String facilityIdJpqlStr = " AND a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", Arrays.copyOfRange(facilityIds, i, length)) + ")";

					TypedQuery<StatusInfoEntity> typedQuery
					= em.createQuery(String.format(sbJpql.toString(), facilityIdJpqlStr), StatusInfoEntity.class);

					// ファシリティID設定
					typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", Arrays.copyOfRange(facilityIds, i, length));

					// 受信日時（自）設定
					if (outputFromDate != null) {
						typedQuery = typedQuery.setParameter("outputFromDate", outputFromDate);
					}
					// 受信日時（至）設定
					if (outputToDate != null){
						typedQuery = typedQuery.setParameter("outputToDate", outputToDate);
					}
					// 出力日時（自）設定
					if(generationFromDate != null) {
						typedQuery = typedQuery.setParameter("generationFromDate", generationFromDate);
					}
					// 出力日時（至）設定
					if(generationToDate != null) {
						typedQuery = typedQuery.setParameter("generationToDate", generationToDate);
					}
					// アプリケーション設定
					if(application != null && !"".equals(application)) {
						typedQuery = typedQuery.setParameter("application", application);
					}
					// メッセージ設定
					if(message != null && !"".equals(message)) {
						typedQuery = typedQuery.setParameter("message", message);
					}
					// オーナーロールID設定
					if(ownerRoleId != null && !"".equals(ownerRoleId)) {
						typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId);
					}
					typedQuery = typedQuery.setMaxResults(1);
					tmpList.addAll(typedQuery.getResultList());
				}
				// 最大件数にする
				if (tmpList.size() > 1) {
					rtnList.addAll(tmpList.subList(0, 1));
				} else {
					rtnList = tmpList;
				}
			}
			return rtnList;
		}
	}
}
