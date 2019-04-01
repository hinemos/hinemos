/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.binary.model.BinaryCheckInfo;
import com.clustercontrol.binary.model.BinaryPatternInfo;
import com.clustercontrol.binary.model.PacketCheckInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfoPK;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfoPK;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfo;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfoPK;

/**
 * DB照会Util<BR>
 *
 * @version 6.1.0
 */
public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static MonitorInfo getMonitorInfoPK(String monitorId) throws MonitorNotFound, InvalidRole {
		return getMonitorInfoPK(monitorId, ObjectPrivilegeMode.READ);
	}

	public static MonitorInfo getMonitorInfoPK(String monitorId, ObjectPrivilegeMode mode) throws MonitorNotFound, InvalidRole {
		MonitorInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(MonitorInfo.class, monitorId, mode);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorInfo.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getMonitorInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static MonitorInfo getMonitorInfoPK_OR(String monitorId, String ownerRoleId) throws MonitorNotFound, InvalidRole {
		MonitorInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find_OR(MonitorInfo.class, monitorId, ownerRoleId);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorInfo.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorInfoPK_OR() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getMonitorInfoPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static MonitorInfo getMonitorInfoPK_NONE(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			MonitorInfo entity
			= em.find(MonitorInfo.class, monitorId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorInfo.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static List<MonitorInfo> getAllMonitorInfo() throws HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorInfo> list
			= em.createNamedQuery("MonitorInfo.findAll", MonitorInfo.class)
			.getResultList();
			return list;
		}
	}

	public static List<MonitorInfo> getMonitorInfoByOwnerRoleId_NONE(String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorInfo> list
			= em.createNamedQuery("MonitorInfo.findByOwnerRoleId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("ownerRoleId", roleId)
			.getResultList();
			return list;
		}
	}

	public static List<MonitorInfo> getMonitorInfoByFacilityId_NONE(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorInfo> list
			= em.createNamedQuery("MonitorInfo.findByFacilityId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("facilityId", facilityId)
			.getResultList();
			return list;
		}
	}

	public static List<MonitorInfo> getMonitorInfoByMonitorTypeId(String monitorTypeId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorInfo> list
			= em.createNamedQuery("MonitorInfo.findByMonitorTypeId", MonitorInfo.class)
			.setParameter("monitorTypeId", monitorTypeId)
			.getResultList();
			return list;
		}
	}

	public static List<MonitorInfo> getMonitorInfoByMonitorTypeId_NONE(String monitorTypeId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorInfo> list
			= em.createNamedQuery("MonitorInfo.findByMonitorTypeId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("monitorTypeId", monitorTypeId)
			.getResultList();
			return list;
		}
	}

	public static List<MonitorNumericValueInfo> getMonitorNumericValueInfoByIdNumericType_OR(
			String monitorId, String monitorNumericType, String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorNumericValueInfo> list
			= em.createNamedQuery_OR("MonitorInfo.findByIdNumericType", MonitorNumericValueInfo.class, ownerRoleId)
			.setParameter("monitorId", monitorId)
			.setParameter("monitorNumericType", monitorNumericType)
			.getResultList();
			return list;
		}
	}

	public static List<MonitorNumericValueInfo> getMonitorNumericValueInfoByIdNumericType(
			String monitorId, String monitorNumericType, ObjectPrivilegeMode mode) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorNumericValueInfo> list
			= em.createNamedQuery("MonitorNumericValueInfo.findByIdNumericType", MonitorNumericValueInfo.class, mode)
			.setParameter("monitorId", monitorId)
			.setParameter("monitorNumericType", monitorNumericType)
			.getResultList();
			return list;
		}
	}

	public static MonitorNumericValueInfo getMonitorNumericValueInfoPK(MonitorNumericValueInfoPK pk) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			MonitorNumericValueInfo entity = em.find(MonitorNumericValueInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorNumericValueInfoEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getMonitorNumericValueInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static MonitorNumericValueInfo getMonitorNumericValueInfoPK(
			String monitorId, String monitorNumericType, Integer priority) throws MonitorNotFound {
		return getMonitorNumericValueInfoPK(new MonitorNumericValueInfoPK(monitorId, monitorNumericType, priority));
	}

	public static MonitorStringValueInfo getMonitorStringValueInfoPK(MonitorStringValueInfoPK pk) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			MonitorStringValueInfo entity = em.find(MonitorStringValueInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorStringValueInfoEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getMonitorStringValueInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static MonitorStringValueInfo getMonitorStringValueInfoPK(String monitorId, Integer orderNo) throws MonitorNotFound {
		return getMonitorStringValueInfoPK(new MonitorStringValueInfoPK(monitorId, orderNo));
	}

	public static MonitorTruthValueInfo getMonitorTruthValueInfoPK(MonitorTruthValueInfoPK pk) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			MonitorTruthValueInfo entity = em.find(MonitorTruthValueInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorTruthValueInfoEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getMonitorTruthValueInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
	public static List<MonitorInfo> getMonitorInfoFindByCalendarId_NONE(String calendarId){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorInfo> monitorInfoList
			= em.createNamedQuery("MonitorInfo.findByCalendarId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("calendarId", calendarId).getResultList();
			return monitorInfoList;
		}
	}

	public static MonitorTruthValueInfo getMonitorTruthValueInfoPK(String monitorId, Integer priority, Integer truthValue) throws MonitorNotFound {
		return getMonitorTruthValueInfoPK(new MonitorTruthValueInfoPK(monitorId, priority, truthValue));
	}

	public static List<MonitorTruthValueInfo> getMonitorTruthValueInfoFindByMonitorId(String monitorId, ObjectPrivilegeMode mode){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorTruthValueInfo> monitorTruthValueInfoList
			= em.createNamedQuery("MonitorTruthValueInfo.findByMonitorId", MonitorTruthValueInfo.class, mode)
			.setParameter("monitorId", monitorId).getResultList();
			return monitorTruthValueInfoList;
		}
	}

	public static List<MonitorStringValueInfo> getMonitorStringValueInfoFindByMonitorId(String monitorId, ObjectPrivilegeMode mode){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorStringValueInfo> monitorStringValueInfoList
			= em.createNamedQuery("MonitorStringValueInfo.findByMonitorId", MonitorStringValueInfo.class, mode)
			.setParameter("monitorId", monitorId).getResultList();

			// sort
			if (monitorStringValueInfoList != null && monitorStringValueInfoList.size() > 0) {
				Collections.sort(monitorStringValueInfoList, new Comparator<MonitorStringValueInfo>() {
					@Override
					public int compare(MonitorStringValueInfo o1, MonitorStringValueInfo o2) {
						return o1.getId().getOrderNo().compareTo(o2.getId().getOrderNo());
					}
				});
				
			}

			return monitorStringValueInfoList;
		}
	}

	public static List<MonitorNumericValueInfo> getMonitorNumericValueInfoFindByMonitorId(String monitorId, ObjectPrivilegeMode mode){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorNumericValueInfo> monitorNumericValueInfoList
			= em.createNamedQuery("MonitorNumericValueInfo.findByMonitorId", MonitorNumericValueInfo.class, mode)
			.setParameter("monitorId", monitorId).getResultList();
			return monitorNumericValueInfoList;
		}
	}

	public static List<MonitorInfo> getMonitorInfoByFilter(
			String monitorId,
			String monitorTypeId,
			String description,
			String calendarId,
			String regUser,
			Long regFromDate,
			Long regToDate,
			String updateUser,
			Long updateFromDate,
			Long updateToDate,
			Boolean monitorFlg,
			Boolean collectorFlg,
			String ownerRoleId) {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 「含まない」検索を行うかの判断に使う値
			String notInclude = "NOT:";
		
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT a FROM MonitorInfo a WHERE true = true");
			// monitorId
			if(monitorId != null && !"".equals(monitorId)) {
				if(!monitorId.startsWith(notInclude)) {
					sbJpql.append(" AND a.monitorId like :monitorId");
				}else{
					sbJpql.append(" AND a.monitorId not like :monitorId");
				}
			}
			// monitorTypeId
			if(monitorTypeId != null && !"".equals(monitorTypeId)) {
				if(!monitorTypeId.startsWith(notInclude)) {
					sbJpql.append(" AND a.monitorTypeId like :monitorTypeId");
				}else{
					sbJpql.append(" AND a.monitorTypeId not like :monitorTypeId");
				}
			}
			// description
			if(description != null && !"".equals(description)) {
				if(!description.startsWith(notInclude)) {
					sbJpql.append(" AND a.description like :description");
				}else{
					sbJpql.append(" AND a.description not like :description");
				}
			}
			// calendarId
			if(calendarId != null && !"".equals(calendarId)) {
				sbJpql.append(" AND a.calendarId like :calendarId");
			}
			// regUser
			if(regUser != null && !"".equals(regUser)) {
				if(!regUser.startsWith(notInclude)) {
					sbJpql.append(" AND a.regUser like :regUser");
				}else{
					sbJpql.append(" AND a.regUser not like :regUser");
				}
			}
			// regFromDate
			if (regFromDate > 0) {
				sbJpql.append(" AND a.regDate >= :regFromDate");
			}
			// regToDate
			if (regToDate > 0){
				sbJpql.append(" AND a.regDate <= :regToDate");
			}
			// updateUser
			if(updateUser != null && !"".equals(updateUser)) {
				if (!updateUser.startsWith(notInclude)) {
					sbJpql.append(" AND a.updateUser like :updateUser");
				}else{
					sbJpql.append(" AND a.updateUser not like :updateUser");
				}
			}
			// updateFromDate
			if(updateFromDate > 0) {
				sbJpql.append(" AND a.updateDate >= :updateFromDate");
			}
			// updateToDate
			if(updateToDate > 0) {
				sbJpql.append(" AND a.updateDate <= :updateToDate");
			}
			// monitorFlg
			if(monitorFlg != null) {
				sbJpql.append(" AND a.monitorFlg = :monitorFlg");
			}
			// collectorFlg
			if(collectorFlg != null) {
				sbJpql.append(" AND a.collectorFlg = :collectorFlg");
			}
			// ownerRoleId
			if(ownerRoleId != null && !"".equals(ownerRoleId)) {
				if (!ownerRoleId.startsWith(notInclude)) {
					sbJpql.append(" AND a.ownerRoleId like :ownerRoleId");
				}else{
					sbJpql.append(" AND a.ownerRoleId not like :ownerRoleId");
				}
			}
			TypedQuery<MonitorInfo> typedQuery = em.createQuery(sbJpql.toString(), MonitorInfo.class);

			// monitorId
			if(monitorId != null && !"".equals(monitorId)) {
				if(!monitorId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("monitorId", monitorId);
				}else{
					typedQuery = typedQuery.setParameter("monitorId", monitorId.substring(notInclude.length()));
				}
			}
			// monitorTypeId
			if(monitorTypeId != null && !"".equals(monitorTypeId)) {
				if(!monitorTypeId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("monitorTypeId", monitorTypeId);
				}else{
					typedQuery = typedQuery.setParameter("monitorTypeId", monitorTypeId.substring(notInclude.length()));
				}
			}
			// description
			if(description != null && !"".equals(description)) {
				if(!description.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("description", description);
				}else{
					typedQuery = typedQuery.setParameter("description", description.substring(notInclude.length()));
				}
			}
			// calendarId
			if(calendarId != null && !"".equals(calendarId)) {
				typedQuery = typedQuery.setParameter("calendarId", calendarId);
			}
			// regUser
			if(regUser != null && !"".equals(regUser)) {
				if(!regUser.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("regUser", regUser);
				}else{
					typedQuery = typedQuery.setParameter("regUser", regUser.substring(notInclude.length()));
				}
			}
			// regFromDate
			if (regFromDate > 0) {
				typedQuery = typedQuery.setParameter("regFromDate", regFromDate);
			}
			// regToDate
			if (regToDate > 0){
				typedQuery = typedQuery.setParameter("regToDate", regToDate);
			}
			// updateUser
			if(updateUser != null && !"".equals(updateUser)) {
				if(!updateUser.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("updateUser", updateUser);
				}else{
					typedQuery = typedQuery.setParameter("updateUser", updateUser.substring(notInclude.length()));
				}
			}
			// updateFromDate
			if(updateFromDate > 0) {
				typedQuery = typedQuery.setParameter("updateFromDate", updateFromDate);
			}
			// updateToDate
			if(updateToDate > 0) {
				typedQuery = typedQuery.setParameter("updateToDate", updateToDate);
			}
			// monitorFlg
			if(monitorFlg != null) {
				typedQuery = typedQuery.setParameter("monitorFlg", monitorFlg);
			}
			// collectorFlg
			if(collectorFlg != null) {
				typedQuery = typedQuery.setParameter("collectorFlg", collectorFlg);
			}
			// ownerRoleId
			if(ownerRoleId != null && !"".equals(ownerRoleId)) {
				if(!ownerRoleId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId);
				}else{
					typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId.substring(notInclude.length()));
				}
			}
			return typedQuery.getResultList();
		}
	}

	public static List<MonitorInfo> getMonitorInfoByMonitorType_OR(List<Integer> monitorTypes, String ownerRoleId) {
		List<MonitorInfo> list = new ArrayList<>();
		if (monitorTypes == null || monitorTypes.size() == 0) {
			return list;
		}
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder sbJpql = new StringBuilder();
			sbJpql.append("SELECT a FROM MonitorInfo a");
			sbJpql.append(" WHERE a.monitorType IN (" + HinemosEntityManager.getParamNameString("monitorType", monitorTypes.toArray()) + ")");
			sbJpql.append(" ORDER BY a.monitorId");
			TypedQuery<MonitorInfo> typedQuery = em.createQuery_OR(sbJpql.toString(), MonitorInfo.class, ownerRoleId);
			typedQuery = HinemosEntityManager.appendParam(typedQuery, "monitorType", monitorTypes.toArray());

			return typedQuery.getResultList();
		}
	}

	/**
	 * バイナリ検索条件テーブルの主キー取得
	 */
	public static BinaryPatternInfo getBinaryPatternInfoPK(MonitorStringValueInfoPK pk) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			BinaryPatternInfo entity = em.find(BinaryPatternInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("BinaryPatternInfo.findByPrimaryKey" + pk.toString());
				m_log.info("getBinaryPatternInfoPK() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	/**
	 * バイナリ検索条件テーブルの主キー(ID指定)取得
	 */
	public static BinaryPatternInfo getBinaryPatternInfoPK(String monitorId, Integer orderNo) throws MonitorNotFound {
		return getBinaryPatternInfoPK(new MonitorStringValueInfoPK(monitorId, orderNo));
	}

	/**
	 * バイナリ監視設定の主キー取得
	 */
	public static BinaryCheckInfo getBinaryCheckInfoPK(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			BinaryCheckInfo entity = em.find(BinaryCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound(
						"BinaryCheckInfoEntity.findByPrimaryKey" + ", monitorId = " + monitorId);
				m_log.info("getBinaryCheckInfoPK() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	/**
	 * パケットキャプチャ監視設定の主キー取得
	 */
	public static PacketCheckInfo getPacketCheckInfoPK(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			PacketCheckInfo entity = em.find(PacketCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound(
						"PacketCheckInfoEntity.findByPrimaryKey" + ", monitorId = " + monitorId);
				m_log.info("getPacketCheckInfoPK() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		return entity;
		}
	}
}
