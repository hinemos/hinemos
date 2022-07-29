/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.util;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaPersistenceConfig;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosPropertyNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.maintenance.model.HinemosPropertyInfo;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.model.MaintenanceTypeMst;

import jakarta.persistence.Query;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );


	public static MaintenanceTypeMst getMaintenanceTypeMstPK(String typeId) throws MaintenanceNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			MaintenanceTypeMst entity = em.find(MaintenanceTypeMst.class, typeId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MaintenanceNotFound e = new MaintenanceNotFound("MaintenanceTypeMstEntity.findByPrimaryKey"
						+ ", typeId = " + typeId);
				m_log.info("getMaintenanceTypeMstPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static List<MaintenanceTypeMst> getAllMaintenanceTypeMst() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MaintenanceTypeMst> list = em.createNamedQuery("MaintenanceTypeMstEntity.findAll"
					, MaintenanceTypeMst.class).getResultList();
			return list;
		}
	}

	public static MaintenanceInfo getMaintenanceInfoPK(String maintenanceId) throws MaintenanceNotFound, InvalidRole {
		return getMaintenanceInfoPK(maintenanceId, ObjectPrivilegeMode.READ);
	}

	public static MaintenanceInfo getMaintenanceInfoPK(String maintenanceId, ObjectPrivilegeMode mode) throws MaintenanceNotFound, InvalidRole {
		MaintenanceInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(MaintenanceInfo.class, maintenanceId, mode);
			if (entity == null) {
				MaintenanceNotFound e = new MaintenanceNotFound("MaintenanceInfoEntity.findByPrimaryKey"
						+ ", maintenanceId = " + maintenanceId);
				m_log.info("getMaintenanceInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getMaintenanceInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static List<MaintenanceInfo> getAllMaintenanceInfoOrderByMaintenanceId() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MaintenanceInfo> list = em.createNamedQuery("MaintenanceInfoEntity.findAllOrderByMaintenanceId"
					,MaintenanceInfo.class).getResultList();
			return list;
		}
	}

	public static List<MaintenanceInfo> getMaintenanceInfoFindByCalendarId_NONE(String calendarId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MaintenanceInfo> list
			= em.createNamedQuery("MaintenanceInfoEntity.findByCalendarId"
					,MaintenanceInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("calendarId", calendarId)
					.getResultList();
			return list;
		}
	}
	
	/**
	 * Windows用
	 * 監視項目IDより収集項目IDのリストを取得する
	 * @param monitorId
	 * @return
	 */
	public static List<Integer> getCollectoridByMonitorId(String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Integer> list
			= em.createNamedQuery("CollectKeyInfo.getCollectorIdByMonitorId"
					,Integer.class, ObjectPrivilegeMode.NONE)
					.setParameter("monitorId", monitorId)
					.getResultList();
			return list;
		}
	}

	public static List<Date> selectTargetDateCollectStringDataByDateTimeAndMonitorId(Long dateTime, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("CollectStringData.selectTargetDateByDateTimeAndMonitorId",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.setParameter("monitorId", monitorId)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteCollectStringDataByDateTimeAndMonitorId(Date targetDate, int timeout, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("CollectStringData.deleteByDateTimeAndMonitorId")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate))
					.setParameter("monitorId", monitorId);
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}
	
	public static List<Date> selectTargetDateCollectStringDataByDateTime(Long dateTime) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("CollectStringData.selectTargetDateDateTime",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteCollectStringDataByDateTime(Date targetDate, int timeout) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("CollectStringData.deleteByDateTime")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate));
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}

	/**
	 * バイナリ収集テーブルから指定条件の削除対象の日付のリストを取得.
	 * @return 削除対象の日付のリスト
	 */
	public static List<Date> selectTargetDateCollectBinaryDataByDateTimeAndMonitorId(Long dateTime, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("CollectBinaryData.selectTargetDateByDateTimeAndMonitorId",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.setParameter("monitorId", monitorId)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}

	/**
	 * バイナリ収集テーブルから指定条件のデータを削除.
	 * @return 削除件数.
	 */
	public static int deleteCollectBinaryDataByDateTimeAndMonitorId(Date targetDate, int timeout, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("CollectBinaryData.deleteByDateTimeAndMonitorId")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate))
					.setParameter("monitorId", monitorId);
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}
	
	/**
	 * バイナリ収集テーブルから指定条件の削除対象の日付のリストを取得.
	 * @return 削除対象の日付のリスト
	 */
	public static List<Date> selectTargetDateCollectBinaryDataByDateTime(Long dateTime) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("CollectBinaryData.selectTargetDateByDateTime",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}

	/**
	 * バイナリ収集テーブルから指定条件のデータを削除.
	 * 
	 * @param dateTime
	 * @param timeout
	 * @return 削除件数.
	 */
	public static int deleteCollectBinaryDataByDateTime(Date targetDate, int timeout) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("CollectBinaryData.deleteByDateTime")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate));
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}

	public static List<Date> selectTargetDateCollectDataByDateTimeAndMonitorId(Long dateTime, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("CollectData.selectTargetDateByDateTimeAndMonitorId",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.setParameter("monitorId", monitorId)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteCollectDataByDateTimeAndMonitorId(Date targetDate, int timeout, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("CollectData.deleteByDateTimeAndMonitorId")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate))
					.setParameter("monitorId", monitorId);
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}

	public static List<Date> selectTargetDateCollectDataByDateTime(Long dateTime) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("CollectData.selectTargetDateByDateTime",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteCollectDataByDateTime(Date targetDate, int timeout) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("CollectData.deleteByDateTime")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate));
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}
	
	/**
	 * Windows用
	 * 数値系収集テーブルから指定条件のデータを削除.
	 * @param dateTime
	 * @param timeout
	 * @param collectId
	 * @return 削除件数.
	 */
	public static int deleteCollectDataByDateTimeAndCollectorId(Long dateTime, int timeout, int collectId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("CollectData.deleteByDateTimeAndCollectorId")
					.setParameter("dateTime", dateTime)
					.setParameter("collectorid", collectId);
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}

	public static List<Date> selectTargetDateSummaryHourByDateTimeAndMonitorId(Long dateTime, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("SummaryHour.selectTargetDateByDateTimeAndMonitorId",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.setParameter("monitorId", monitorId)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteSummaryHourByDateTimeAndMonitorId(Date targetDate, int timeout, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("SummaryHour.deleteByDateTimeAndMonitorId")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate))
					.setParameter("monitorId", monitorId);
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}
	
	public static List<Date> selectTargetDateSummaryHourByDateTime(Long dateTime) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("SummaryHour.selectTargetDateByDateTime",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteSummaryHourByDateTime(Date targetDate, int timeout) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("SummaryHour.deleteByDateTime")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate));
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}
	
	public static List<Date> selectTargetDateSummaryDayByDateTimeAndMonitorId(Long dateTime, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("SummaryDay.selectTargetDateByDateTimeAndMonitorId",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.setParameter("monitorId", monitorId)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteSummaryDayByDateTimeAndMonitorId(Date targetDate, int timeout, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("SummaryDay.deleteByDateTimeAndMonitorId")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate))
					.setParameter("monitorId", monitorId);
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}
	
	public static List<Date> selectTargetDateSummaryDayByDateTime(Long dateTime) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("SummaryDay.selectTargetDateByDateTime",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteSummaryDayByDateTime(Date targetDate, int timeout) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("SummaryDay.deleteByDateTime")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate));
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}
	
	public static List<Date> selectTargetDateSummaryMonthByDateTimeAndMonitorId(Long dateTime, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("SummaryMonth.selectTargetDateByDateTimeAndMonitorId",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.setParameter("monitorId", monitorId)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteSummaryMonthByDateTimeAndMonitorId(Date targetDate, int timeout, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("SummaryMonth.deleteByDateTimeAndMonitorId")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate))
					.setParameter("monitorId", monitorId);
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}
	
	public static List<Date> selectTargetDateSummaryMonthByDateTime(Long dateTime) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("SummaryMonth.selectTargetDateByDateTime",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteSummaryMonthByDateTime(Date targetDate, int timeout) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("SummaryMonth.deleteByDateTime")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate));
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}

	public static List<Date> selectTargetDateEventLogByGenerationDate(Long generationDate) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("EventLogEntity.selectTargetDateByGenerationDate",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("generationDate", generationDate)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteEventLogByGenerationDate(Date targetDate, int timeout) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("EventLogEntity.deleteByGenerationDate")
					.setParameter("generationDate", parseTargetDateToTargetUnixTime(targetDate));
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}

	public static List<Date> selectTargetDateEventLogByGenerationDateConfigFlg(Long generationDate) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("EventLogEntity.selectTargetDateByGenerationDateConfigFlg",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("generationDate", generationDate)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteEventLogByGenerationDateConfigFlg(Date targetDate, int timeout) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("EventLogEntity.deleteByGenerationDateConfigFlg")
					.setParameter("generationDate", parseTargetDateToTargetUnixTime(targetDate));
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}
	
	public static List<Date> selectTargetDateEventLogByGenerationDateAndOwnerRoleId(Long generationDate, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("EventLogEntity.selectTargetDateByGenerationDateAndOwnerRoleId",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("generationDate", generationDate)
					.setParameter("ownerRoleId", roleId)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}

	public static int deleteEventLogByGenerationDateAndOwnerRoleId(Date targetDate, int timeout, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			Query query = em.createNamedQuery("EventLogEntity.deleteByGenerationDateAndOwnerRoleId")
					.setParameter("generationDate", parseTargetDateToTargetUnixTime(targetDate))
					.setParameter("ownerRoleId", roleId);
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}

	public static List<Date> selectTargetDateEventLogByGenerationDateConfigFlgAndOwnerRoleId(Long generationDate, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("EventLogEntity.selectTargetDateByGenerationDateConfigFlgAndOwnerRoleId",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("generationDate", generationDate)
					.setParameter("ownerRoleId", roleId)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteEventLogByGenerationDateConfigFlgAndOwnerRoleId(Date targetDate, int timeout, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("EventLogEntity.deleteByGenerationDateConfigFlgAndOwnerRoleId")
					.setParameter("generationDate", parseTargetDateToTargetUnixTime(targetDate))
					.setParameter("ownerRoleId", roleId);
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}

	public static int deleteEventLogOperationHistoryByGenerationDate(Date targetDate, int timeout) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("EventLogOperationHistoryEntity.deleteByGenerationDate")
					.setParameter("generationDate", parseTargetDateToTargetUnixTime(targetDate));
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}

	public static int deleteEventLogOperationHistoryByGenerationDateConfigFlg(Date targetDate, int timeout) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("EventLogOperationHistoryEntity.deleteByGenerationDateConfigFlg")
					.setParameter("generationDate", parseTargetDateToTargetUnixTime(targetDate));
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}
	
	public static List<Date> selectTargetDateEventLogOperationHistoryByGenerationDateAndOwnerRoleId(Long generationDate, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Date> list =em.createNamedQuery("EventLogOperationHistoryEntity.selectTargetDateByGenerationDateAndOwnerRoleId",
					Date.class, ObjectPrivilegeMode.NONE)
					.setParameter("generationDate", generationDate)
					.setParameter("ownerRoleId", roleId)
					.getResultList();
			return list;
		}
	}

	public static int deleteEventLogOperationHistoryByGenerationDateAndOwnerRoleId(Date targetDate, int timeout, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			Query query = em.createNamedQuery("EventLogOperationHistoryEntity.deleteByGenerationDateAndOwnerRoleId")
					.setParameter("generationDate", parseTargetDateToTargetUnixTime(targetDate))
					.setParameter("ownerRoleId", roleId);
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}

	public static int deleteEventLogOperationHistoryByGenerationDateConfigFlgAndOwnerRoleId(Date targetDate, int timeout, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("EventLogOperationHistoryEntity.deleteByGenerationDateConfigFlgAndOwnerRoleId")
					.setParameter("generationDate", parseTargetDateToTargetUnixTime(targetDate))
					.setParameter("ownerRoleId", roleId);
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}
	
	public static int createJobCompletedSessionsTable() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery("JobCompletedSessionsEntity.createTable").executeUpdate();
			return ret;
		}
	}

	/**
	 * 特定の開始・再実行日時の間のデータを削除対象のテンポラリテーブルへ登録します。
	 * 
	 * @param since 開始・再実行日時(00:00:00)
	 * @param until 開始・再実行日時+1日(00:00:00)
	 * @return 削除対象レコード件数
	 */
	public static int insertJobCompletedSessionsJobSessionJob(Long since, Long until) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery(
					"JobCompletedSessionsEntity.insertJobSessionJob", Integer.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, since)
				.setParameter(2, until)
				.executeUpdate();
			return ret;
		}
	}

	/**
	 * 特定の開始・再実行日時の間のデータと開始予定日時の間のデータを削除対象としてテンポラリテーブルへ登録します。
	 * 
	 * @param startDateSince 開始・再実行日時(00:00:00)
	 * @param startDateUntil 開始・再実行日時+1日(00:00:00)
	 * @param scheduleDateSince 開始予定日時(00:00:00)
	 * @param scheduleDateUntil 開始予定日時+1日(00:00:00)
	 * @return 削除対象レコード件数
	 */
	public static int insertJobCompletedAndInterruptedSessionsJobSessionJob(Long startDateSince, Long startDateUntil, Long scheduleDateSince, Long scheduleDateUntil) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery(
					"JobCompletedSessionsEntity.insertJobCompletedAndInterruptedSessionJob", Integer.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, startDateSince)
				.setParameter(2, startDateUntil)
				.setParameter(3, scheduleDateSince)
				.setParameter(4, scheduleDateUntil)
				.executeUpdate();
			return ret;
		}
	}

	/**
	 * 特定の開始予定日時の間のデータを削除対象としてテンポラリテーブルへ登録します。
	 * 
	 * @param scheduleDateSince 開始予定日時(00:00:00)
	 * @param scheduleDateUntil 開始予定日時+1日(00:00:00)
	 * @return 削除対象レコード件数
	 */
	public static int insertJobInterruptedSessionsJobSessionJob(Long scheduleDateSince, Long scheduleDateUntil) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery(
					"JobCompletedSessionsEntity.insertJobInterruptedSessionJob", Integer.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, scheduleDateSince)
				.setParameter(2, scheduleDateUntil)
				.executeUpdate();
			return ret;
		}
	}

	/**
	 * 特定の開始・再実行日時の間のデータを削除対象のテンポラリテーブルへ登録します。（オーナーロールID指定）
	 * 
	 * @param since 開始・再実行日時(00:00:00)
	 * @param until 開始・再実行日時+1日(00:00:00)
	 * @param roleId オーナーロールID
	 * @return 削除対象レコード件数
	 */
	public static int insertJobCompletedSessionsJobSessionJobByOwnerRoleId(Long since, Long until, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery(
					"JobCompletedSessionsEntity.insertJobSessionJobByOwnerRoleId", Integer.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, since)
				.setParameter(2, until)
				.setParameter(3, roleId)
				.executeUpdate();
			return ret;
		}
	}

	/**
	 * 特定の開始・再実行日時の間のデータと開始予定日時の間のデータを削除対象としてテンポラリテーブルへ登録します。（オーナーロールID指定）
	 * 
	 * @param startDateSince 開始・再実行日時(00:00:00)
	 * @param startDateUntil 開始・再実行日時+1日(00:00:00)
	 * @param scheduleDateSince 開始予定日時(00:00:00)
	 * @param scheduleDateUntil 開始予定日時+1日(00:00:00)
	 * @param roleId オーナーロールID
	 * @return 削除対象レコード件数
	 */
	public static int insertJobCompletedAndInterruptedSessionsJobSessionJobByOwnerRoleId(Long startDateSince, Long startDateUntil, Long scheduleDateSince, Long scheduleDateUntil, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery(
					"JobCompletedSessionsEntity.insertJobCompletedAndInterruptedSessionJobByOwnerRoleId", Integer.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, startDateSince)
				.setParameter(2, startDateUntil)
				.setParameter(3, scheduleDateSince)
				.setParameter(4, scheduleDateUntil)
				.setParameter(5, roleId)
				.executeUpdate();
			return ret;
		}
	}

	/**
	 * 特定の開始予定日時の間のデータを削除対象としてテンポラリテーブルへ登録します。（オーナーロールID指定）
	 * 
	 * @param scheduleDateSince 開始予定日時(00:00:00)
	 * @param scheduleDateUntil 開始予定日時+1日(00:00:00)
	 * @param roleId オーナーロールID
	 * @return 削除対象レコード件数
	 */
	public static int insertJobInterruptedSessionsJobSessionJobByOwnerRoleId(Long scheduleDateSince, Long scheduleDateUntil, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery(
					"JobCompletedSessionsEntity.insertJobInterruptedSessionJobByOwnerRoleId", Integer.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, scheduleDateSince)
				.setParameter(2, scheduleDateUntil)
				.setParameter(3, roleId)
				.executeUpdate();
			return ret;
		}
	}

	public static int dropJobCompletedSessionsTable() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery("JobCompletedSessionsEntity.dropTable").executeUpdate();
			return ret;
		}
	}

	/**
	 * 特定の開始・再実行日時の間のデータを削除対象(終了、変更済)のテンポラリテーブルへ登録します。
	 * 
	 * @param since 開始・再実行日時(00:00:00)
	 * @param until 開始・再実行日時+1日(00:00:00)
	 * @return 削除対象レコード件数
	 */
	public static int insertJobCompletedSessionsJobSessionJobByStatus(Long since, Long until) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery(
					"JobCompletedSessionsEntity.insertJobSessionJobByStatus", Integer.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, since)
				.setParameter(2, until)
				.executeUpdate();
			return ret;
		}
	}

	/**
	 * 特定の開始・再実行日時の間のデータを削除対象(終了、変更済)のテンポラリテーブルへ登録します。
	 * 
	 * @param startDateSince 開始・再実行日時(00:00:00)
	 * @param startDateUntil 開始・再実行日時+1日(00:00:00)
	 * @param scheduleDateSince 開始予定日時(00:00:00)
	 * @param scheduleDateUntil 開始予定日時+1日(00:00:00)
	 * @return 削除対象レコード件数
	 */
	public static int insertJobCompletedAndInterruptedSessionsJobSessionJobByStatus(Long startDateSince, Long startDateUntil, Long scheduleDateSince, Long scheduleDateUntil) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery(
					"JobCompletedSessionsEntity.insertJobCompletedAndInterruptedSessionJobByStatus", Integer.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, startDateSince)
				.setParameter(2, startDateUntil)
				.setParameter(3, scheduleDateSince)
				.setParameter(4, scheduleDateUntil)
				.executeUpdate();
			return ret;
		}
	}

	/**
	 * 特定の開始予定日時の間のデータを削除対象(終了、変更済)としてテンポラリテーブルへ登録します。
	 * 
	 * @param scheduleDateSince 開始予定日時(00:00:00)
	 * @param scheduleDateUntil 開始予定日時+1日(00:00:00)
	 * @return 削除対象レコード件数
	 */
	public static int insertJobInterruptedSessionsJobSessionJobByStatus(Long scheduleDateSince, Long scheduleDateUntil) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery(
					"JobCompletedSessionsEntity.insertJobInterruptedSessionJobByStatus", Integer.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, scheduleDateSince)
				.setParameter(2, scheduleDateUntil)
				.executeUpdate();
			return ret;
		}
	}

	/**
	 * 特定の開始・再実行日時の間のデータを削除対象(終了、変更済)のテンポラリテーブルへ登録します。（オーナーロールID指定）
	 * 
	 * @param since 開始・再実行日時(00:00:00)
	 * @param until 開始・再実行日時+1日(00:00:00)
	 * @param roleId オーナーロールID
	 * @return 削除対象レコード件数
	 */
	public static int insertJobCompletedSessionsJobSessionJobByStatusAndOwnerRoleId(Long since, Long until, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery(
					"JobCompletedSessionsEntity.insertJobSessionJobByStatusAndOwnerRoleId", Integer.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, since)
				.setParameter(2, until)
				.setParameter(3, roleId)
				.executeUpdate();
			return ret;
		}
	}

	/**
	 * 特定の開始・再実行日時の間のデータと開始予定日時の間のデータを削除対象(終了、変更済)としてテンポラリテーブルへ登録します。（オーナーロールID指定）
	 * 
	 * @param startDateSince 開始・再実行日時(00:00:00)
	 * @param startDateUntil 開始・再実行日時+1日(00:00:00)
	 * @param scheduleDateSince 開始予定日時(00:00:00)
	 * @param scheduleDateUntil 開始予定日時+1日(00:00:00)
	 * @param roleId オーナーロールID
	 * @return 削除対象レコード件数
	 */
	public static int insertJobCompletedAndInterruptedSessionsJobSessionJobByStatusAndOwnerRoleId(Long startDateSince, Long startDateUntil, Long scheduleDateSince, Long scheduleDateUntil, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery(
					"JobCompletedSessionsEntity.insertJobCompletedAndInterruptedSessionJobByStatusAndOwnerRoleId", Integer.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, startDateSince)
				.setParameter(2, startDateUntil)
				.setParameter(3, scheduleDateSince)
				.setParameter(4, scheduleDateUntil)
				.setParameter(5, roleId)
				.executeUpdate();
			return ret;
		}
	}

	/**
	 * 特定の開始予定日時の間のデータを削除対象(終了、変更済)としてテンポラリテーブルへ登録します。（オーナーロールID指定）
	 * 
	 * @param scheduleDateSince 開始予定日時(00:00:00)
	 * @param scheduleDateUntil 開始予定日時+1日(00:00:00)
	 * @param roleId オーナーロールID
	 * @return 削除対象レコード件数
	 */
	public static int insertJobInterruptedSessionsJobSessionJobByStatusAndOwnerRoleId(Long scheduleDateSince, Long scheduleDateUntil, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery(
					"JobCompletedSessionsEntity.insertJobInterruptedSessionJobByStatusAndOwnerRoleId", Integer.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, scheduleDateSince)
				.setParameter(2, scheduleDateUntil)
				.setParameter(3, roleId)
				.executeUpdate();
			return ret;
		}
	}

	public static int deleteJobSessionByCompletedSessions() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery("JobSessionEntity.deleteByJobCompletedSessions")
					.executeUpdate();
			return ret;
		}
	}

	public static int deleteNotifyRelationInfoByCompletedSessions() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery("NotifyRelationInfoEntity.deleteByJobCompletedSessions")
					.executeUpdate();
			return ret;
		}
	}

	public static int deleteMonitorStatusByCompletedSessions() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery("MonitorStatusEntity.deleteByJobCompletedSessions")
					.executeUpdate();
			return ret;
		}
	}

	public static int deleteNotifyHistoryByCompletedSessions() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery("NotifyHistoryEntity.deleteByJobCompletedSessions")
					.executeUpdate();
			return ret;
		}
	}

	/**
	 * 削除対象の中で最も過去のジョブ履歴の開始・再実行日時を取得します。
	 * 
	 * @param untilDate 指定日時(削除対象外の保存期間を差し引いた日時)
	 * @return 最も過去のジョブ履歴の開始・再実行日時
	 */
	public static Long selectOldestStartDate(Long untilDate) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> results = em.createNamedQuery("JobCompletedSessionsEntity.selectOldestStartDate",
					Long.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, untilDate)
				.getResultList();
			if (results.size() <= 0) {
				return null;
			} else {
				return results.get(0);
			}
		}
	}

	/**
	 * 削除対象の中で開始・再実行日時が存在しない、最も過去のジョブ履歴の開始予定日時を取得します。
	 * 
	 * @param untilDate 指定日時(削除対象外の保存期間を差し引いた日時)
	 * @return 最も過去のジョブ履歴の開始予定日時
	 */
	public static Long selectOldestScheduleDate(Long untilDate) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> results = em.createNamedQuery("JobCompletedSessionsEntity.selectOldestScheduleDate",
					Long.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, untilDate)
				.getResultList();
			if (results.size() <= 0) {
				return null;
			} else {
				return results.get(0);
			}
		}
	}

	/**
	 * 削除対象(終了、変更済)の中で最も過去のジョブ履歴の開始・再実行日時を取得します。
	 * 
	 * @param untilDate 指定日時(削除対象外の保存期間を差し引いた日時)
	 * @return 最も過去のジョブ履歴の開始・再実行日時
	 */
	public static Long selectOldestStartDateByStatus(Long untilDate) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> results = em.createNamedQuery("JobCompletedSessionsEntity.selectOldestStartDateByStatus",
					Long.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, untilDate)
				.getResultList();
			if (results.size() <= 0) {
				return null;
			} else {
				return results.get(0);
			}
		}
	}

	/**
	 * 削除対象(終了、変更済)の中で開始・再実行日時が存在しない、最も過去のジョブ履歴の開始・再実行日時を取得します。
	 * 
	 * @param untilDate 指定日時(削除対象外の保存期間を差し引いた日時)
	 * @return 最も過去のジョブ履歴の開始・再実行日時
	 */
	public static Long selectOldestScheduleDateByStatus(Long untilDate) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> results = em.createNamedQuery("JobCompletedSessionsEntity.selectOldestScheduleDateByStatus",
					Long.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, untilDate)
				.getResultList();
			if (results.size() <= 0) {
				return null;
			} else {
				return results.get(0);
			}
		}
	}

	/**
	 * 削除対象の中で最も過去のジョブ履歴の開始・再実行日時を取得します。（オーナーロールID指定）
	 * 
	 * @param untilDate 指定日時(削除対象外の保存期間を差し引いた日時)
	 * @param roleId オーナーロールID
	 * @return 最も過去のジョブ履歴の開始・再実行日時
	 */
	public static Long selectOldestStartDateByOwnerRoleId(Long untilDate, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> results = em.createNamedQuery("JobCompletedSessionsEntity.selectOldestStartDateByOwnerRoleId",
					Long.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, untilDate)
				.setParameter(2, roleId)
				.getResultList();
			if (results.size() <= 0) {
				return null;
			} else {
				return results.get(0);
			}
		}
	}

	/**
	 * 削除対象の中で開始・再実行日時が存在しない、最も過去のジョブ履歴の開始予定日時を取得します。（オーナーロールID指定）
	 * 
	 * @param untilDate 指定日時(削除対象外の保存期間を差し引いた日時)
	 * @param roleId オーナーロールID
	 * @return 最も過去のジョブ履歴の開始予定日時
	 */
	public static Long selectOldestScheduleDateByOwnerRoleId(Long untilDate, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> results = em.createNamedQuery("JobCompletedSessionsEntity.selectOldestScheduleDateByOwnerRoleId",
					Long.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, untilDate)
				.setParameter(2, roleId)
				.getResultList();
			if (results.size() <= 0) {
				return null;
			} else {
				return results.get(0);
			}
		}
	}

	/**
	 * 削除対象(終了、変更済)の中で最も過去のジョブ履歴の開始・再実行日時を取得します。（オーナーロールID指定）
	 * 
	 * @param untilDate 指定日時(削除対象外の保存期間を差し引いた日時)
	 * @param roleId オーナーロールID
	 * @return 最も過去のジョブ履歴の開始・再実行日時
	 */
	public static Long selectOldestStartDateByStatusAndOwnerRoleId(Long untilDate, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> results = em.createNamedQuery("JobCompletedSessionsEntity.selectOldestStartDateByStatusAndOwnerRoleId",
					Long.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, untilDate)
				.setParameter(2, roleId)
				.getResultList();
			if (results.size() <= 0) {
				return null;
			} else {
				return results.get(0);
			}
		}
	}

	/**
	 * 削除対象(終了、変更済)の中で開始・再実行日時が存在しない、最も過去のジョブ履歴の開始予定日時を取得します。（オーナーロールID指定）
	 * 
	 * @param untilDate 指定日時(削除対象外の保存期間を差し引いた日時)
	 * @param roleId オーナーロールID
	 * @return 最も過去のジョブ履歴の開始予定日時
	 */
	public static Long selectOldestScheduleDateByStatusAndOwnerRoleId(Long untilDate, String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> results = em.createNamedQuery("JobCompletedSessionsEntity.selectOldestScheduleDateByStatusAndOwnerRoleId",
					Long.class, ObjectPrivilegeMode.NONE)
				.setParameter(1, untilDate)
				.setParameter(2, roleId)
				.getResultList();
			if (results.size() <= 0) {
				return null;
			} else {
				return results.get(0);
			}
		}
	}

	/**
	 * セッション一時テーブルからセッションIDを取得します。
	 * @return セッションIDリスト
	 */
	public static ArrayList<String> selectCompletedSession() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<?> list = em.createNativeQuery("select session_id from cc_job_completed_sessions").getResultList();
	
			ArrayList<String> sessionList = new ArrayList<String>();
			for(Object obj : list) {
				sessionList.add(obj.toString());
			}
			return sessionList;
		}
	}

	public static List<Date> selectTargetDateRpaScenarioOperationResultByDateTimeAndScenarioId(Long dateTime, String scenarioId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("RpaScenarioOperationResult.selectTargetDateByDateTimeAndScenarioId",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.setParameter("scenarioId", scenarioId)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteRpaScenarioOperationResultByDateTimeAndScenarioId(Date targetDate, int timeout, String scenarioId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("RpaScenarioOperationResult.deleteByDateTimeAndScenarioId")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate))
					.setParameter("scenarioId", scenarioId);
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}
	
	public static List<Date> selectTargetDateRpaScenarioOperationResultByDateTime(Long dateTime) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<Long> list = em.createNamedQuery("RpaScenarioOperationResult.selectTargetDateByDateTime",
					Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("dateTime", dateTime)
					.getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}
	
	public static int deleteRpaScenarioOperationResultDataByDateTime(Date targetDate, int timeout) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Query query = em.createNamedQuery("RpaScenarioOperationResult.deleteByDateTime")
					.setParameter("dateTime", parseTargetDateToTargetUnixTime(targetDate));
			if (timeout > 0) {
				query = query.setHint(JpaPersistenceConfig.JPA_PARAM_QUERY_TIMEOUT, timeout * 1000);
			}
			return query.executeUpdate();
		}
	}


	/**
	 * 指定されたキーの共通設定情報を取得します。<br>
	 * @param key キー
	 * @return 共通設定情報
	 * @throws HinemosPropertyNotFound
	 */
	public static HinemosPropertyInfo getHinemosPropertyInfoPK(String key) throws HinemosPropertyNotFound, InvalidRole {
		return getHinemosPropertyInfoPK(key, ObjectPrivilegeMode.READ);
	}

	/**
	 * 指定されたキーの共通設定情報をアクセス権限チェックなしで取得します。<br>
	 * セキュリティ上、Hinemosマネージャ内でのみ使用してください。
	 * @param key キー
	 * @return 共通設定情報
	 * @throws HinemosPropertyNotFound
	 */
	public static HinemosPropertyInfo getHinemosPropertyInfoPK_NONE(String key) throws HinemosPropertyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			HinemosPropertyInfo entity
			= em.createNamedQuery("HinemosPropertyEntity.findByKey"
					,HinemosPropertyInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("key", key)
					.getSingleResult();
			return entity;
		}
	}

	/**
	 * 共通設定情報一覧を取得します。
	 * @return 共通設定情報リスト
	 */
	public static List<HinemosPropertyInfo> getAllHinemosPropertyOrderByKey() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<HinemosPropertyInfo> list = em.createNamedQuery("HinemosPropertyEntity.findAll",
							HinemosPropertyInfo.class, ObjectPrivilegeMode.READ)
					.getResultList();
			return list;
		}
	}

	protected static HinemosPropertyInfo getHinemosPropertyInfoPK(String key, ObjectPrivilegeMode mode) throws HinemosPropertyNotFound, InvalidRole {
		HinemosPropertyInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(HinemosPropertyInfo.class, key, mode);
			if (entity == null) {
				HinemosPropertyNotFound e = new HinemosPropertyNotFound("HinemosPropertyEntity.findByPrimaryKey" + ", key = " + key);
					m_log.info("getHinemosPropertyInfoPK() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getHinemosPropertyInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	/**
	 * 共通情報一覧を取得します。
	 *
	 * @return 共通情報一覧
	 */
	public static List<HinemosPropertyInfo> getAllHinemosProperty_None() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<HinemosPropertyInfo> list = em.createNamedQuery("HinemosPropertyEntity.findAll",
							HinemosPropertyInfo.class, ObjectPrivilegeMode.NONE)
					.getResultList();
			return list;
		}
	}
	
	/**
	 * UnixTime(ミリ秒)のリストから、Date型の日付重複のないリストを取得する
	 * 
	 * @param unixTimeList
	 * @return 処理対象となる日付のリスト
	 */
	private static List<Date> getTargetDateListByUnixTimeLsit(List<Long> unixTimeList){
		List<Date> ret = new ArrayList<Date>();
		Calendar calendar = Calendar.getInstance();
		for(long unixTime : unixTimeList){
			calendar.setTimeInMillis(unixTime);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date date = new Date(calendar.getTime().getTime());
			if(!ret.contains(date)){
				ret.add(date);
			}
		}
		return ret;
	}

	/**
	 * 削除対象の日付(Date)をDBに合わせてUnixTime(ミリ秒)に変換する
	 * ※その際、削除は渡したUnixTime未満で行われるため、削除対象の日付を削除するために＋1日してからUnixTimeに変換する
	 * 
	 * @param targetDate 削除対象日付
	 * @return 削除対象日付＋1日のUnixTime
	 */
	public static long parseTargetDateToTargetUnixTime(Date targetDate){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(targetDate);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		return calendar.getTimeInMillis();
	}
}
