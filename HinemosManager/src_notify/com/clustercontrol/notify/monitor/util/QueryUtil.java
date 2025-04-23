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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.QueryCriteria;
import com.clustercontrol.commons.util.QueryDivergence;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.filtersetting.bean.EventFilterBaseCriteria;
import com.clustercontrol.filtersetting.bean.EventFilterBaseInfo;
import com.clustercontrol.filtersetting.bean.EventFilterConditionCriteria;
import com.clustercontrol.filtersetting.bean.EventFilterConditionInfo;
import com.clustercontrol.filtersetting.bean.StatusFilterBaseCriteria;
import com.clustercontrol.filtersetting.bean.StatusFilterBaseInfo;
import com.clustercontrol.filtersetting.bean.StatusFilterConditionCriteria;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.factory.ModifyEventInfo;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.model.EventLogEntityPK;
import com.clustercontrol.notify.monitor.model.EventLogOperationHistoryEntity;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;
import com.clustercontrol.notify.monitor.model.StatusInfoEntityPK;
import com.clustercontrol.util.HinemosTime;

import jakarta.persistence.TypedQuery;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(QueryUtil.class);

	/** テスト時に置換可能な外部依存処理 */
	public interface External {
		int getMonitorHistoryFacilityIdMaxCount();
	}

	/** デフォルトの外部依存処理実装 */
	private static final External defaultExternal = new External() {
		@Override
		public int getMonitorHistoryFacilityIdMaxCount() {
			return HinemosPropertyCommon.monitor_history_facility_id_max_count.getIntegerValue();
		}
	};

	/** デフォルトの外部依存処理実装を返します。 */
	public static External getDefaultExternal() {
		return defaultExternal;
	}

	private static External external = getDefaultExternal();

	/** 外部依存処理実装を置き換えます。 */
	public static void setExternal(External external) {
		QueryUtil.external = external;
	}

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
			List<StatusInfoEntity> list = em.createNamedQuery("StatusInfoEntity.findExpirationStatus", StatusInfoEntity.class)
					.setParameter("expirationDate", expirationDate).getResultList();
			return list;
		}
	}

	/**
	 * ステータス通知のプライマリーキーから監視詳細IDを除いて
	 * ステータス通知の情報を取得します。
	 * 
	 * @param pluginId
	 * @param monitorId
	 * @param facilityId
	 * @return
	 */
	public static List<StatusInfoEntity> getStatusInfoByPKWithoutMonitorDetailId(String pluginId,
			String monitorId,
			String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<StatusInfoEntity> list =
					em.createNamedQuery("StatusInfoEntity.findByPKWithoutMonitorDetailId", StatusInfoEntity.class)
					.setParameter("pluginId", pluginId)
					.setParameter("monitorId", monitorId)
					.setParameter("facilityId", facilityId)
					.getResultList();
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
			EventFilterBaseInfo filter,
			int confirmType,
			String confirmUser,
			Long confirmDate) {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 引数 confirmType で指定されたフラグはoffにする
			for (EventFilterConditionInfo cnd : filter.getConditions()) {
				cnd.setConfirmFlag(confirmType, Boolean.FALSE);
			}

			AtomicInteger updateCounter = new AtomicInteger(0);
			filterEvents(filter, false, ObjectPrivilegeMode.MODIFY, 1000, events -> {
				long startTime = HinemosTime.currentTimeMillis();
				for (EventLogEntity event : events) {
					ModifyEventInfo.setConfirmFlgChange(jtm, event, confirmType, confirmDate, confirmUser);
					event.setConfirmFlg(confirmType);
					if (confirmType == ConfirmConstant.TYPE_CONFIRMED ||
							confirmType == ConfirmConstant.TYPE_CONFIRMING) {
						event.setConfirmDate(confirmDate);
					}
					event.setConfirmUser(confirmUser);
					em.merge(event);
					updateCounter.incrementAndGet();
				}
				em.flush(); // DBへの書き出し
				em.clear(); // 取得したEventLogEntityのキャッシュをクリア
				m_log.debug(String.format("updateEventLogFlgByFilter: %d rows completed in %d ms.",
						events.size(), HinemosTime.currentTimeMillis() - startTime));
				return true;
			});
			return updateCounter.get();
		}
	}

	/**
	 * 
	 * @param filters 全件取得の場合はnull
	 * @param orderAsc 結果の並び順。trueならoutputDateの昇順、falseなら降順。
	 * @param limit 取得する最大件数。
	 * @return
	 */
	public static List<EventLogEntity> getEventLogByFilter(
			EventFilterBaseInfo filter, boolean orderAsc, int limit) {

		List<EventLogEntity> rtnList = new ArrayList<>();

		// フィルタ指定がない場合は全検索
		if (filter == null) {
			filter = EventFilterBaseInfo.ofAllEvents();
		}

		filterEvents(filter, orderAsc, ObjectPrivilegeMode.READ, limit, events -> {
			rtnList.addAll(events);
			return false;
		});

		// ソート処理
		Collections.sort(rtnList, new Comparator<EventLogEntity>() {
			@Override
			public int compare(EventLogEntity o1, EventLogEntity o2) {
				if (orderAsc) {
					// outputDateの昇順
					return (o1.getId().getOutputDate().compareTo(o2.getId().getOutputDate()));
				} else {
					// outputDateの降順
					return (o2.getId().getOutputDate().compareTo(o1.getId().getOutputDate()));
				}
			}
		});

		// 最大件数にする
		if (rtnList.size() > limit) {
			return new ArrayList<>(rtnList.subList(0, limit));
		} else {
			return rtnList;
		}
	}

	/**
	 * イベント履歴のフィルタ検索 共通処理。
	 * <p>
	 * 対象ファシリティIDのリストを {@link HinemosPropertyCommon#monitor_history_facility_id_max_count} ずつのグループに分割し、
	 * それぞれのグループごとにクエリを実行します。<br/>
	 * また、それぞれのクエリでは、引数の blockSize 単位で結果を取得して、blockOperation へ渡します。
	 * 
	 * @param filter フィルタ条件。
	 * @param orderAsc クエリのORDER指定を、trueなら昇順、falseなら降順にします。
	 * @param opMode オブジェクト権限モード。
	 * @param blockSize 1度のクエリ実行で取得する最大件数。
	 * @param blockOperation
	 * 		クエリ結果を受け取る処理を指定します。
	 * 		<ul>
	 * 		<li>引数はクエリで取得したイベント履歴のリストです。
	 * 		<li>戻り値が true の場合、次のブロックを取得して再びこの処理を呼び出します。次のブロックとなるクエリ結果が空の場合は、次のクエリへ移ります。
	 * 		<li>戻り値が false の場合、次のクエリへ移ります。
	 * 		</ul>
	 */
	private static void filterEvents(EventFilterBaseInfo filter, boolean orderAsc, ObjectPrivilegeMode opMode,
			int blockSize, Function<List<EventLogEntity>, Boolean> blockOperation) {

		// 条件設定がない場合は、検索するまでもなく、一致するイベント履歴は存在しない
		if (filter.getConditions().size() == 0) return;
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// フィルタからQueryCriteriaを生成
			List<EventFilterBaseCriteria> baseCrts = filter.createCriteria(external.getMonitorHistoryFacilityIdMaxCount(), "a");
			List<EventFilterConditionCriteria> condCrts = filter.createConditionsCriteria("a");

			// 複数条件項目をOR結合
			StringBuffer exprs = new StringBuffer();
			String delim = "";
			for (QueryCriteria crt : condCrts) {
				exprs.append(delim).append("(").append(crt.buildExpressions()).append(")");
				delim = " OR ";
			}

			// ORDER指定は必ず付ける (後で改めてソートはする)
			String orderBy;
			if (orderAsc) {
				orderBy = "ORDER BY a.id.outputDate";
			} else {
				orderBy = "ORDER BY a.id.outputDate DESC";
			}

			// ファシリティIDグループのループ
			for (QueryCriteria baseCrt : baseCrts) {
				String jpql = "SELECT a FROM EventLogEntity a WHERE ("
						+ baseCrt.buildExpressions()
						+ ") AND ("
						+ exprs.toString()
						+ ") " + orderBy;
				m_log.debug("filterEvents: SQL=" + jpql);

				TypedQuery<EventLogEntity> query = em.createQuery(jpql, EventLogEntity.class, EventLogEntity.class, opMode);
				baseCrt.submitParameters(query);
				for (QueryCriteria condCrt : condCrts) {
					condCrt.submitParameters(query);
				}

				while (true) {
					query.setMaxResults(blockSize);
					List<EventLogEntity> events = query.getResultList();
					if (events.size() == 0) break;
					Boolean res = blockOperation.apply(events);
					if (res == null || !res.booleanValue()) break;
				}
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
			if (priority != null) {
				sbJpql.append(" AND a.priority = :priority");
			}
			// 受信日時（自）設定
			if (outputFromDate != null) {
				sbJpql.append(" AND a.id.outputDate >= :outputFromDate");
			}
			// 受信日時（至）設定
			if (outputToDate != null) {
				sbJpql.append(" AND a.id.outputDate <= :outputToDate");
			}
			// 出力日時（自）設定
			if (generationFromDate != null) {
				sbJpql.append(" AND a.generationDate >= :generationFromDate");
			}
			// 出力日時（至）設定
			if (generationToDate != null) {
				sbJpql.append(" AND a.generationDate <= :generationToDate");
			}
			// アプリケーション設定
			if (application != null && !"".equals(application)) {
				sbJpql.append(" AND a.application like :application");
			}
			// メッセージ設定
			if (message != null && !"".equals(message)) {
				sbJpql.append(" AND a.message like :message");
			}
			// 確認有無
			if (confirmFlg != null) {
				sbJpql.append(" AND a.confirmFlg = :confirmFlg");
			}
			// 確認ユーザ
			if (confirmUser != null) {
				sbJpql.append(" AND a.confirmUser = :confirmUser");
			}
			// ソート
			if (orderByFlg) {
				sbJpql.append(" ORDER BY a.id.outputDate DESC");
			}

			if (facilityIds == null || facilityIds.length <= 0) {

				TypedQuery<EventLogEntity> typedQuery = em.createQuery(String.format(sbJpql.toString(), ""), EventLogEntity.class);

				// 重要度設定
				if (priority != null) {
					typedQuery = typedQuery.setParameter("priority", priority);
				}
				// 受信日時（自）設定
				if (outputFromDate != null) {
					typedQuery = typedQuery.setParameter("outputFromDate", outputFromDate);
				}
				// 受信日時（至）設定
				if (outputToDate != null) {
					typedQuery = typedQuery.setParameter("outputToDate", outputToDate);
				}
				// 出力日時（自）設定
				if (generationFromDate != null) {
					typedQuery = typedQuery.setParameter("generationFromDate", generationFromDate);
				}
				// 出力日時（至）設定
				if (generationToDate != null) {
					typedQuery = typedQuery.setParameter("generationToDate", generationToDate);
				}
				// アプリケーション設定
				if (application != null && !"".equals(application)) {
					typedQuery = typedQuery.setParameter("application", application);
				}
				// メッセージ設定
				if (message != null && !"".equals(message)) {
					typedQuery = typedQuery.setParameter("message", message);
				}
				// 確認有無
				if (confirmFlg != null) {
					typedQuery = typedQuery.setParameter("confirmFlg", confirmFlg);
				}
				// 確認ユーザ
				if (confirmUser != null) {
					typedQuery = typedQuery.setParameter("confirmUser", confirmUser);
				}
				typedQuery = typedQuery.setMaxResults(1);
				rtnList = typedQuery.getResultList();
			} else {
				int MONITOR_HISTORY_FACILITY_ID_MAX_COUNT = external.getMonitorHistoryFacilityIdMaxCount();
				List<EventLogEntity> tmpList = new ArrayList<>();
				for (int i = 0; i < facilityIds.length; i += MONITOR_HISTORY_FACILITY_ID_MAX_COUNT) {
					int length = i + MONITOR_HISTORY_FACILITY_ID_MAX_COUNT;
					if (length > facilityIds.length) {
						length = facilityIds.length;
					}

					String facilityIdJpqlStr = " AND a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", Arrays.copyOfRange(facilityIds, i, length)) + ")";

					TypedQuery<EventLogEntity> typedQuery = em.createQuery(String.format(sbJpql.toString(), facilityIdJpqlStr), EventLogEntity.class);

					// ファシリティID設定
					typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", Arrays.copyOfRange(facilityIds, i, length));
					// 重要度設定
					if (priority != null) {
						typedQuery = typedQuery.setParameter("priority", priority);
					}
					// 受信日時（自）設定
					if (outputFromDate != null) {
						typedQuery = typedQuery.setParameter("outputFromDate", outputFromDate);
					}
					// 受信日時（至）設定
					if (outputToDate != null) {
						typedQuery = typedQuery.setParameter("outputToDate", outputToDate);
					}
					// 出力日時（自）設定
					if (generationFromDate != null) {
						typedQuery = typedQuery.setParameter("generationFromDate", generationFromDate);
					}
					// 出力日時（至）設定
					if (generationToDate != null) {
						typedQuery = typedQuery.setParameter("generationToDate", generationToDate);
					}
					// アプリケーション設定
					if (application != null && !"".equals(application)) {
						typedQuery = typedQuery.setParameter("application", application);
					}
					// メッセージ設定
					if (message != null && !"".equals(message)) {
						typedQuery = typedQuery.setParameter("message", message);
					}
					// 確認有無
					if (confirmFlg != null) {
						typedQuery = typedQuery.setParameter("confirmFlg", confirmFlg);
					}
					// 確認ユーザ
					if (confirmUser != null) {
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

	public static int getStatusCountByFilter(StatusFilterBaseInfo filter) {
		int countAll = 0;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			List<TypedQuery<Long>> queryList = createQueryListForGetStatus(em, filter, null, Long.class, "COUNT(DISTINCT a)");

			for (TypedQuery<Long> query : queryList) {
				countAll += QueryDivergence.countResult(query.getSingleResult());
			}
			return countAll;
		}
	}
	
	public static List<StatusInfoEntity> getStatusInfoByFilter(StatusFilterBaseInfo filter, Integer limit) {
		List<StatusInfoEntity> rtnList = new ArrayList<>();
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			List<TypedQuery<StatusInfoEntity>> queryList = createQueryListForGetStatus(em, filter, limit, StatusInfoEntity.class, "a");
			
			for (TypedQuery<StatusInfoEntity> query : queryList) {
				List<StatusInfoEntity> result = query.getResultList();
				rtnList.addAll(result);
			}
			
			if (limit != null && limit > 0) {
				// 改めて最終変更日時の降順でソート
				Collections.sort(rtnList, (o1, o2) -> o2.getOutputDate().compareTo(o1.getOutputDate()));
				
				if (rtnList.size() > limit) {
					return rtnList.subList(0, limit);
				}
			}
			
			return rtnList;
		}
	}
	
	private static <T> List<TypedQuery<T>> createQueryListForGetStatus(HinemosEntityManager em, StatusFilterBaseInfo filter, Integer limit, Class<T> clazz, String selectColumn) {
		List<TypedQuery<T>> queryList = new ArrayList<>();

		// 表示件数に上限設定があるか
		final boolean isLimited = limit != null && limit > 0;
		
		// フィルタからQueryCriteriaを生成
		List<StatusFilterBaseCriteria> baseCrts = filter.createCriteria(external.getMonitorHistoryFacilityIdMaxCount(), "a");
		List<StatusFilterConditionCriteria> condCrts = filter.createConditionsCriteria("a");
		
		// 複数条件項目をOR結合
		StringBuilder exprs = new StringBuilder();
		String delim = "";
		for (QueryCriteria crt : condCrts) {
			exprs.append(delim).append("(").append(crt.buildExpressions()).append(")");
			delim = " OR ";
		}

		StringBuilder jpql = new StringBuilder();

		// ファシリティIDグループのループ
		for (QueryCriteria baseCrt : baseCrts) {
			jpql.delete(0, jpql.length());
			
			jpql.append("SELECT ");
			jpql.append(selectColumn);
			jpql.append(" FROM StatusInfoEntity a WHERE (");
			jpql.append(baseCrt.buildExpressions());
			jpql.append(") AND (");
			jpql.append(exprs);
			jpql.append(")");
			if (isLimited) {
				jpql.append(" ORDER BY a.outputDate DESC");
			}

			if (m_log.isDebugEnabled()) {
				m_log.debug("createGetStatusQueryList: SQL=" + jpql);
			}

			TypedQuery<T> query = em.createQuery(jpql.toString(), clazz);
			
			baseCrt.submitParameters(query);
			for (QueryCriteria condCrt : condCrts) {
				condCrt.submitParameters(query);
			}

			if (isLimited) {
				query.setMaxResults(limit);
			}
			
			queryList.add(query);
		}
		
		return queryList;
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
			if (outputToDate != null) {
				sbJpql.append(" AND a.outputDate <= :outputToDate");
			}
			// 出力日時（自）設定
			if (generationFromDate != null) {
				sbJpql.append(" AND a.generationDate >= :generationFromDate");
			}
			// 出力日時（至）設定
			if (generationToDate != null) {
				sbJpql.append(" AND a.generationDate <= :generationToDate");
			}
			// アプリケーション設定
			if (application != null && !"".equals(application)) {
				sbJpql.append(" AND a.application like :application");
			}
			// メッセージ設定
			if (message != null && !"".equals(message)) {
				sbJpql.append(" AND a.message like :message");
			}
			// オーナーロールID設定
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				sbJpql.append(" AND a.ownerRoleId = :ownerRoleId");
			}
			if (orderFlg) {
				sbJpql.append(" ORDER BY a.priority, a.outputDate DESC");
			} else {
				sbJpql.append(" ORDER BY a.priority");
			}

			if (facilityIds == null || facilityIds.length <= 0) {

				TypedQuery<StatusInfoEntity> typedQuery = em.createQuery(String.format(sbJpql.toString(), ""), StatusInfoEntity.class);

				// 受信日時（自）設定
				if (outputFromDate != null) {
					typedQuery = typedQuery.setParameter("outputFromDate", outputFromDate);
				}
				// 受信日時（至）設定
				if (outputToDate != null) {
					typedQuery = typedQuery.setParameter("outputToDate", outputToDate);
				}
				// 出力日時（自）設定
				if (generationFromDate != null) {
					typedQuery = typedQuery.setParameter("generationFromDate", generationFromDate);
				}
				// 出力日時（至）設定
				if (generationToDate != null) {
					typedQuery = typedQuery.setParameter("generationToDate", generationToDate);
				}
				// アプリケーション設定
				if (application != null && !"".equals(application)) {
					typedQuery = typedQuery.setParameter("application", application);
				}
				// メッセージ設定
				if (message != null && !"".equals(message)) {
					typedQuery = typedQuery.setParameter("message", message);
				}
				// オーナーロールID設定
				if (ownerRoleId != null && !"".equals(ownerRoleId)) {
					typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId);
				}
				typedQuery = typedQuery.setMaxResults(1);
				rtnList = typedQuery.getResultList();
			} else {
				List<String> facilityIdList = Arrays.asList(facilityIds);
				List<StatusInfoEntity> tmpList = new ArrayList<>();
				int MONITOR_HISTORY_FACILITY_ID_MAX_COUNT = external.getMonitorHistoryFacilityIdMaxCount();
				for (int i = 0; i < facilityIdList.size(); i += MONITOR_HISTORY_FACILITY_ID_MAX_COUNT) {
					int length = i + MONITOR_HISTORY_FACILITY_ID_MAX_COUNT;
					if (length > facilityIdList.size()) {
						length = facilityIdList.size();
					}

					String facilityIdJpqlStr = " AND a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", Arrays.copyOfRange(facilityIds, i, length)) + ")";

					TypedQuery<StatusInfoEntity> typedQuery = em.createQuery(String.format(sbJpql.toString(), facilityIdJpqlStr), StatusInfoEntity.class);

					// ファシリティID設定
					typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", Arrays.copyOfRange(facilityIds, i, length));

					// 受信日時（自）設定
					if (outputFromDate != null) {
						typedQuery = typedQuery.setParameter("outputFromDate", outputFromDate);
					}
					// 受信日時（至）設定
					if (outputToDate != null) {
						typedQuery = typedQuery.setParameter("outputToDate", outputToDate);
					}
					// 出力日時（自）設定
					if (generationFromDate != null) {
						typedQuery = typedQuery.setParameter("generationFromDate", generationFromDate);
					}
					// 出力日時（至）設定
					if (generationToDate != null) {
						typedQuery = typedQuery.setParameter("generationToDate", generationToDate);
					}
					// アプリケーション設定
					if (application != null && !"".equals(application)) {
						typedQuery = typedQuery.setParameter("application", application);
					}
					// メッセージ設定
					if (message != null && !"".equals(message)) {
						typedQuery = typedQuery.setParameter("message", message);
					}
					// オーナーロールID設定
					if (ownerRoleId != null && !"".equals(ownerRoleId)) {
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
