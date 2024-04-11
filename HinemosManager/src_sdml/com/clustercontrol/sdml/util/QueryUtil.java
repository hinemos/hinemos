/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.QueryDivergence;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.sdml.model.SdmlControlMonitorRelation;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.model.SdmlControlStatus;
import com.clustercontrol.sdml.model.SdmlControlStatusPK;
import com.clustercontrol.sdml.model.SdmlInitializeData;
import com.clustercontrol.sdml.model.SdmlMonitorNotifyRelation;
import com.clustercontrol.sdml.model.SdmlMonitorNotifyRelationPK;
import com.clustercontrol.sdml.model.SdmlMonitorTypeMasterInfo;

import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.TypedQuery;

public class QueryUtil {
	private static Log logger = LogFactory.getLog(QueryUtil.class);

	/**
	 * アプリケーションIDを指定してSDML制御設定を取得
	 * 
	 * @param applicationId
	 * @return
	 * @throws SdmlControlSettingNotFound
	 * @throws InvalidRole
	 */
	public static SdmlControlSettingInfo getSdmlControlSettingInfoPK(String applicationId)
			throws SdmlControlSettingNotFound, InvalidRole {
		return getSdmlControlSettingInfoPK(applicationId, ObjectPrivilegeMode.READ);
	}

	/**
	 * アプリケーションIDを指定してSDML制御設定を取得
	 * 
	 * @param applicationId
	 * @param mode
	 * @return
	 * @throws SdmlControlSettingNotFound
	 * @throws InvalidRole
	 */
	public static SdmlControlSettingInfo getSdmlControlSettingInfoPK(String applicationId, ObjectPrivilegeMode mode)
			throws SdmlControlSettingNotFound, InvalidRole {
		SdmlControlSettingInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(SdmlControlSettingInfo.class, applicationId, mode);
			if (entity == null) {
				SdmlControlSettingNotFound e = new SdmlControlSettingNotFound(
						"SdmlControlSettingInfo.findByPrimaryKey" + ", applicationId=" + applicationId);
				logger.info("getSdmlControlSettingInfoPK() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			logger.info("getSdmlControlSettingInfoPK() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}

		return entity;
	}

	/**
	 * SDML制御設定の一覧を取得
	 * 
	 * @return
	 */
	public static List<SdmlControlSettingInfo> getAllSdmlControlSettingInfo() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlControlSettingInfo> list = em
					.createNamedQuery("SdmlControlSettingInfo.findAll", SdmlControlSettingInfo.class).getResultList();
			return list;
		}
	}

	/**
	 * オーナーロールIDを指定してSDML制御設定の一覧を取得
	 * 
	 * @param ownerRoleId
	 * @return
	 */
	public static List<SdmlControlSettingInfo> getAllSdmlControlSettingInfo_OR(String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlControlSettingInfo> list = em
					.createNamedQuery_OR("SdmlControlSettingInfo.findAll", SdmlControlSettingInfo.class, ownerRoleId)
					.getResultList();
			return list;
		}
	}

	/**
	 * バージョンを指定してSDML制御設定一覧を取得
	 * 
	 * @param version
	 * @return
	 */
	public static List<SdmlControlSettingInfo> getSdmlControlSettingInfoByVersion(String version) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlControlSettingInfo> list = em
					.createNamedQuery("SdmlControlSettingInfo.findByVersion", SdmlControlSettingInfo.class)
					.setParameter("version", version).getResultList();
			return list;
		}
	}

	public static List<SdmlControlSettingInfo> getSdmlControlSettingInfoByVersion_OR(String version,
			String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlControlSettingInfo> list = em.createNamedQuery_OR("SdmlControlSettingInfo.findByVersion",
					SdmlControlSettingInfo.class, ownerRoleId).setParameter("version", version).getResultList();
			return list;
		}
	}

	/**
	 * ファシリティIDを指定してSDML制御設定一覧を取得
	 * 
	 * @param facilityId
	 * @return
	 */
	public static List<SdmlControlSettingInfo> getSdmlControlSettingInfoFindByFacilityId_NONE(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// ファシリティIDが使用されている設定を取得する。
			List<SdmlControlSettingInfo> list = em.createNamedQuery("SdmlControlSettingInfo.findByFacilityId",
					SdmlControlSettingInfo.class, ObjectPrivilegeMode.NONE).setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	/**
	 * 指定された監視項目IDから自動作成元のSDML制御設定を取得
	 * 
	 * @param monitorId
	 * @return
	 * @throws SdmlControlSettingNotFound
	 * @throws HinemosUnknown
	 */
	public static SdmlControlSettingInfo getSdmlControlSettingInfoByMonitorId(String monitorId)
			throws SdmlControlSettingNotFound, HinemosUnknown {
		SdmlControlSettingInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.createNamedQuery("SdmlControlSettingInfo.findByMonitorId", SdmlControlSettingInfo.class)
					.setParameter("monitorId", monitorId).getSingleResult();

		} catch (NoResultException e) {
			logger.info(
					"getSdmlControlSettingInfoByMonitorId() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new SdmlControlSettingNotFound("SdmlControlSettingInfo.findByMonitorId, monitorId=" + monitorId);
		} catch (NonUniqueResultException e) {
			logger.info(
					"getSdmlControlSettingInfoByMonitorId() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return entity;
	}

	/**
	 * 検索条件を指定してSDML制御設定を取得
	 * 
	 * @param applicationId
	 * @param description
	 * @param validFlg
	 * @param regUser
	 * @param regFromDate
	 * @param regToDate
	 * @param updateUser
	 * @param updateFromDate
	 * @param updateToDate
	 * @param ownerRoleId
	 * @param version
	 * @return
	 */
	public static List<SdmlControlSettingInfo> getSdmlControlSettingInfoByFilter(String applicationId,
			String description, Boolean validFlg, String regUser, Long regFromDate, Long regToDate, String updateUser,
			Long updateFromDate, Long updateToDate, String ownerRoleId, String version) {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 「含まない」検索を行うかの判断に使う値
			String notInclude = "NOT:";

			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT a FROM SdmlControlSettingInfo a WHERE true = true");
			// applicationId
			if (applicationId != null && !"".equals(applicationId)) {
				if (!applicationId.startsWith(notInclude)) {
					sbJpql.append(" AND a.applicationId like :applicationId");
				} else {
					sbJpql.append(" AND a.applicationId not like :applicationId");
				}
			}
			// description
			if (description != null && !"".equals(description)) {
				if (!description.startsWith(notInclude)) {
					sbJpql.append(" AND a.description like :description");
				} else {
					sbJpql.append(" AND a.description not like :description");
				}
			}
			// validFlg
			if (validFlg != null) {
				sbJpql.append(" AND a.validFlg = :validFlg");
			}
			// regUser
			if (regUser != null && !"".equals(regUser)) {
				if (!regUser.startsWith(notInclude)) {
					sbJpql.append(" AND a.regUser like :regUser");
				} else {
					sbJpql.append(" AND a.regUser not like :regUser");
				}
			}
			// regFromDate
			if (regFromDate > 0) {
				sbJpql.append(" AND a.regDate >= :regFromDate");
			}
			// regToDate
			if (regToDate > 0) {
				sbJpql.append(" AND a.regDate <= :regToDate");
			}
			// updateUser
			if (updateUser != null && !"".equals(updateUser)) {
				if (!updateUser.startsWith(notInclude)) {
					sbJpql.append(" AND a.updateUser like :updateUser");
				} else {
					sbJpql.append(" AND a.updateUser not like :updateUser");
				}
			}
			// updateFromDate
			if (updateFromDate > 0) {
				sbJpql.append(" AND a.updateDate >= :updateFromDate");
			}
			// updateToDate
			if (updateToDate > 0) {
				sbJpql.append(" AND a.updateDate <= :updateToDate");
			}
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				if (!ownerRoleId.startsWith(notInclude)) {
					sbJpql.append(" AND a.ownerRoleId like :ownerRoleId");
				} else {
					sbJpql.append(" AND a.ownerRoleId not like :ownerRoleId");
				}
			}
			// version
			if (version != null && !"".equals(version)) {
				sbJpql.append(" AND a.version = :version");
			}
			TypedQuery<SdmlControlSettingInfo> typedQuery = em.createQuery(sbJpql.toString(),
					SdmlControlSettingInfo.class);

			// applicationId
			if (applicationId != null && !"".equals(applicationId)) {
				if (!applicationId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("applicationId",
							QueryDivergence.escapeLikeCondition(applicationId));
				} else {
					typedQuery = typedQuery.setParameter("applicationId",
							QueryDivergence.escapeLikeCondition(applicationId.substring(notInclude.length())));
				}
			}
			// description
			if (description != null && !"".equals(description)) {
				if (!description.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("description",
							QueryDivergence.escapeLikeCondition(description));
				} else {
					typedQuery = typedQuery.setParameter("description",
							QueryDivergence.escapeLikeCondition(description.substring(notInclude.length())));
				}
			}
			// validFlg
			if (validFlg != null) {
				typedQuery = typedQuery.setParameter("validFlg", validFlg);
			}
			// regUser
			if (regUser != null && !"".equals(regUser)) {
				if (!regUser.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("regUser", QueryDivergence.escapeLikeCondition(regUser));
				} else {
					typedQuery = typedQuery.setParameter("regUser",
							QueryDivergence.escapeLikeCondition(regUser.substring(notInclude.length())));
				}
			}
			// regFromDate
			if (regFromDate > 0) {
				typedQuery = typedQuery.setParameter("regFromDate", regFromDate);
			}
			// regToDate
			if (regToDate > 0) {
				typedQuery = typedQuery.setParameter("regToDate", regToDate);
			}
			// updateUser
			if (updateUser != null && !"".equals(updateUser)) {
				if (!updateUser.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("updateUser", QueryDivergence.escapeLikeCondition(updateUser));
				} else {
					typedQuery = typedQuery.setParameter("updateUser",
							QueryDivergence.escapeLikeCondition(updateUser.substring(notInclude.length())));
				}
			}
			// updateFromDate
			if (updateFromDate > 0) {
				typedQuery = typedQuery.setParameter("updateFromDate", updateFromDate);
			}
			// updateToDate
			if (updateToDate > 0) {
				typedQuery = typedQuery.setParameter("updateToDate", updateToDate);
			}
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				if (!ownerRoleId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("ownerRoleId",
							QueryDivergence.escapeLikeCondition(ownerRoleId));
				} else {
					typedQuery = typedQuery.setParameter("ownerRoleId",
							QueryDivergence.escapeLikeCondition(ownerRoleId.substring(notInclude.length())));
				}
			}
			// version
			if (version != null && !"".equals(version)) {
				typedQuery = typedQuery.setParameter("version", version);
			}
			return typedQuery.getResultList();
		}
	}

	/**
	 * 自動作成監視設定用の通知関連情報を取得
	 * 
	 * @param pk
	 * @return
	 * @throws SdmlControlSettingNotFound
	 */
	public static SdmlMonitorNotifyRelation getSdmlMonitorNotifyRelationPK(SdmlMonitorNotifyRelationPK pk)
			throws SdmlControlSettingNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			SdmlMonitorNotifyRelation entity = em.find(SdmlMonitorNotifyRelation.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				SdmlControlSettingNotFound e = new SdmlControlSettingNotFound(
						"SdmlMonitorNotifyRelation.findByPrimaryKey, " + pk.toString());
				logger.info(
						"getSdmlMonitorNotifyRelationPK() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	/**
	 * 指定されたアプリケーションIDに紐づく自動作成監視の関連情報を取得
	 * 
	 * @param applicationid
	 * @return
	 */
	public static List<SdmlControlMonitorRelation> getSdmlControlMonitorRelationByApplicationId(String applicationId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlControlMonitorRelation> list = em
					.createNamedQuery("SdmlControlMonitorRelation.findByApplicationId",
							SdmlControlMonitorRelation.class)
					.setParameter("applicationId", applicationId).getResultList();
			return list;
		}
	}

	/**
	 * 指定されたアプリケーションIDとファシリティIDに紐づく自動作成監視の関連情報を取得
	 * 
	 * @param applicationid
	 * @param facilityId
	 * @return
	 */
	public static List<SdmlControlMonitorRelation> getSdmlControlMonitorRelationByApplicationIdAndFacilityId(
			String applicationId, String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlControlMonitorRelation> list = em
					.createNamedQuery("SdmlControlMonitorRelation.findByApplicationIdAndFacilityId",
							SdmlControlMonitorRelation.class)
					.setParameter("applicationId", applicationId).setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	/**
	 * 自動制御の状態を取得
	 * 
	 * @param pk
	 * @return
	 * @throws SdmlControlSettingNotFound
	 */
	public static SdmlControlStatus getSdmlControlStatusPK(SdmlControlStatusPK pk) throws SdmlControlSettingNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			SdmlControlStatus entity = em.find(SdmlControlStatus.class, pk, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				SdmlControlSettingNotFound e = new SdmlControlSettingNotFound(
						"SdmlControlStatus.findByPrimaryKey, " + pk.toString());
				logger.info("getSdmlControlStatusPK() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	/**
	 * 自動制御の状態を全て取得
	 * 
	 * @return
	 */
	public static List<SdmlControlStatus> getAllSdmlControlStatus() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlControlStatus> list = em.createNamedQuery("SdmlControlStatus.findAll", SdmlControlStatus.class)
					.getResultList();
			return list;
		}
	}

	/**
	 * 指定されたアプリケーションIDに紐づく自動制御の状態を削除
	 * 
	 * @param applicationId
	 * @return
	 */
	public static int deleteSdmlControlStatusByApplicationId(String applicationId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery("SdmlControlStatus.deleteByApplicationId")
					.setParameter("applicationId", applicationId).executeUpdate();
			return ret;
		}
	}

	/**
	 * 指定されたアプリケーションIDとファシリティIDに紐づく監視設定初期化情報を取得
	 * 
	 * @param applicationId
	 * @param facilityId
	 * @return
	 */
	public static List<SdmlInitializeData> getSdmlInitializeDataByApplicationIdAndFacilityId(String applicationId,
			String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlInitializeData> list = em
					.createNamedQuery("SdmlInitializeData.findByApplicationIdAndFacilityId", SdmlInitializeData.class)
					.setParameter("applicationId", applicationId).setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	/**
	 * 指定されたアプリケーションIDに紐づく監視設定初期化情報を削除
	 * 
	 * @param applicationId
	 * @return
	 */
	public static int deleteSdmlInitializeDataByApplicationId(String applicationId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery("SdmlInitializeData.deleteByApplicationId")
					.setParameter("applicationId", applicationId).executeUpdate();
			return ret;
		}
	}

	/**
	 * 指定されたアプリケーションIDとファシリティIDに紐づく監視設定初期化情報を削除
	 * 
	 * @param applicationId
	 * @param facilityId
	 * @return
	 */
	public static int deleteSdmlInitializeDataByApplicationIdAndFacilityId(String applicationId, String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery("SdmlInitializeData.deleteByApplicationIdAndFacilityId")
					.setParameter("applicationId", applicationId).setParameter("facilityId", facilityId)
					.executeUpdate();
			return ret;
		}
	}

	/**
	 * SDML監視種別の一覧を取得
	 * 
	 * @return
	 */
	public static List<SdmlMonitorTypeMasterInfo> getAllSdmlMonitorTypeMst() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlMonitorTypeMasterInfo> list = em
					.createNamedQuery("SdmlMonitorTypeMasterInfo.findAll", SdmlMonitorTypeMasterInfo.class)
					.getResultList();
			return list;
		}
	}

	/**
	 * 指定したプラグインIDに一致するSDML監視種別の一覧を取得（曖昧検索）
	 * 
	 * @return
	 */
	public static List<SdmlMonitorTypeMasterInfo> getSdmlMonitorTypeMstByPluginId(String pluginId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlMonitorTypeMasterInfo> list = em
					.createNamedQuery("SdmlMonitorTypeMasterInfo.findByPluginId", SdmlMonitorTypeMasterInfo.class)
					.setParameter("pluginId", pluginId)
					.getResultList();
			return list;
		}
	}

	/**
	 * 指定したプラグインIDに一致しないSDML監視種別の一覧を取得（曖昧検索）
	 * 
	 * @return
	 */
	public static List<SdmlMonitorTypeMasterInfo> getSdmlMonitorTypeMstByPluginIdNot(String pluginId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlMonitorTypeMasterInfo> list = em
					.createNamedQuery("SdmlMonitorTypeMasterInfo.findByPluginIdNot", SdmlMonitorTypeMasterInfo.class)
					.setParameter("pluginId", pluginId)
					.getResultList();
			return list;
		}
	}

	/**
	 * 指定したカレンダIDに一致するSDML監視種別の一覧を取得
	 * 
	 * @return
	 */
	public static List<SdmlControlSettingInfo> getSdmlControlSettingInfoFindByCalendarId_NONE(
			String autoMonitorCalendarId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlControlSettingInfo> list = em
					.createNamedQuery("SdmlControlSettingInfo.findByAutoMonitorCalendarId",
							SdmlControlSettingInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("autoMonitorCalendarId", autoMonitorCalendarId).getResultList();
			return list;
		}
	}

	/**
	 * 指定したオーナーロールIDが設定されたSDML制御設定の一覧を取得
	 * 
	 * @param ownerRoleId
	 * @return
	 */
	public static List<SdmlControlSettingInfo> getSdmlControlSettingInfoFindByOwnerRoleId_NONE(String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlControlSettingInfo> list = em.createNamedQuery("SdmlControlSettingInfo.findByOwnerRoleId",
					SdmlControlSettingInfo.class, ObjectPrivilegeMode.NONE).setParameter("ownerRoleId", ownerRoleId)
					.getResultList();
			return list;
		}
	}
}
