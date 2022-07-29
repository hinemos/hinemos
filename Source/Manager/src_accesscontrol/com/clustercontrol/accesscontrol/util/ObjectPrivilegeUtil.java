/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;

/**
 * オブジェクト権限チェックのUtilクラス
 *
 */
public class ObjectPrivilegeUtil {
	private static Log m_log = LogFactory.getLog(ObjectPrivilegeUtil.class);

	/** オブジェクト権限マップへ入れる情報 */
	private static class OPMapValue {
		Class<?> entityClass;
		HinemosObjectPrivilege annotaion;
		Method idFactory = null;
	}

	// 遅延初期化
	private static class LazyHolder {
		/** オブジェクト権限マップ（K:オブジェクト種別、V:entityのクラス,アノテーションなど） */
		static final Map<String, OPMapValue> objectPrivilegeMap;

		static {
			objectPrivilegeMap = new ConcurrentHashMap<>();

			m_log.info("Gathering entity information.");
			try (JpaTransactionManager jtm = new JpaTransactionManager()) {
				EntityManagerFactory emf = jtm.getEntityManager().getEntityManagerFactory();
				Set<EntityType<?>> entityTypes = emf.getMetamodel().getEntities();
				String report = "";
				for (EntityType<?> entityType : entityTypes) {
					Class<?> clazz = entityType.getBindableJavaType();
					try {
						if (!ObjectPrivilegeTargetInfo.class.isAssignableFrom(clazz)) continue;

						HinemosObjectPrivilege annot = clazz.getAnnotation(HinemosObjectPrivilege.class);
						if (annot == null) {
							m_log.error(clazz + " doesn't have a HinemosObjectPrivilege annotation.");
							continue;
						}

						String objectType = annot.objectType();
						if (!annot.isModifyCheck()) continue;
						report += "[" + objectType + "," + clazz + "] ";

						if (objectPrivilegeMap.get(objectType) != null) {
							m_log.info("Duplicated objectType=" + objectType +", clazz=" + clazz);
							continue;
						}

						OPMapValue opv = new OPMapValue();
						opv.entityClass = clazz;
						opv.annotaion = annot;
						if (annot.idFactory().length() > 0) {
							opv.idFactory = clazz.getMethod(annot.idFactory(), String.class);
							int mdf = opv.idFactory.getModifiers();
							if (!Modifier.isPublic(mdf) || !Modifier.isStatic(mdf)) {
								m_log.error("ID factory method must be 'public static'. " + clazz);
								continue;
							}
						}
						objectPrivilegeMap.put(objectType, opv);
					} catch (Exception e) {
						m_log.error("Failed to gather information of " + clazz, e);
						continue;
					}
				}
				m_log.info("objectMap=" + report);
			}
		}
	}

	/** オブジェクト権限の更新可否チェック */
	public static Object getObjectPrivilegeObject(String objectType, String objectId, ObjectPrivilegeMode mode)
			throws JobMasterNotFound, ObjectPrivilege_InvalidRole  {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			OPMapValue opv = LazyHolder.objectPrivilegeMap.get(objectType);
			m_log.debug("class=" + opv.entityClass + ", objectType=" + objectType + ", objectId=" + objectId);
			if (HinemosModuleConstant.JOB.equals(objectType)) {
				// JobMstEntityの場合は、objectId　!= PK
				// TODO FilterEntityと同様にHinemosObjectPrivilege#idFactoryを使用してJobMstEntityへロジックを固めたほうがよいと思われる。
				return em.find(opv.entityClass, new JobMstEntityPK(objectId, objectId), mode);
			} else {
				// 基本的にはobjectIdがentityのPKだが、idFactory指定されている場合は当該メソッドにPKを生成させる
				Object pk = objectId;
				if (opv.idFactory != null) {
					pk = opv.idFactory.invoke(null, objectId);
				}
				return em.find(opv.entityClass, pk, mode);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			m_log.warn("getObjectPrivilegeObject: Error!", e);
			throw new RuntimeException(e);
		}
	}

	/** オブジェクトに紐づくオブジェクト権限を取得する。 */
	public static List<ObjectPrivilegeInfo> getObjectPrivilegeEntities(Class<?> objectPrivilegeClass, String objectId, String roleId) {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			HinemosObjectPrivilege hinemosObjectPrivilege = objectPrivilegeClass.getAnnotation(HinemosObjectPrivilege.class);
			if (hinemosObjectPrivilege == null) {
				// HinemosObjectPrivilegeアノテーションが設定されていない場合はnullを返す
				return null;
			}
			String objectType = hinemosObjectPrivilege.objectType();
			return em.createNamedQuery("ObjectPrivilegeInfo.findByObjectIdTypeRoleId", ObjectPrivilegeInfo.class)
					.setParameter("objectType", objectType)
					.setParameter("objectId", objectId)
					.setParameter("roleId", roleId)
					.getResultList();
		}
	}

	/** 指定されたオブジェクトに対応したオブジェクト権限を削除する。 */
	public static void deleteObjectPrivilege(String objectType, String objectId) {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.createNamedQuery("ObjectPrivilegeInfo.deleteByObjectTypeObjectId")
			.setParameter("objectType", objectType)
			.setParameter("objectId", objectId)
			.executeUpdate();
		}
	}

	/**
	 * 指定されたオブジェクト種別がオブジェクト権限の対象であるかを確認する。
	 * ※HinemosObjectPrivilegeアノテーションはオーナロールIDでの検索にも使用しているので、
	 *   m_objectPrivilegeMapとは別で判定する。
	 *
	 * @param objectType
	 * @return オブジェクト権限の対象ならTRUE
	 */
	public static boolean isObjectPrivilegeObject(String objectType) {
		OPMapValue opv = LazyHolder.objectPrivilegeMap.get(objectType);
		if (opv != null && opv.annotaion.objectPrivilegeAvailable()) {
			return true;
		}
		// TODO HinemosObjectPrivilege#objectPrivilegeAvailableで判定するようにしたほうがよい。
		// entityの特質(この場合はオブジェクト権限の対象であるかどうか)はentityクラスで定義されるべき。
		if (objectType.equals(HinemosModuleConstant.PLATFORM_REPOSITORY) ||
				objectType.equals(HinemosModuleConstant.MONITOR) ||
				objectType.equals(HinemosModuleConstant.HUB_LOGFORMAT) ||
				objectType.equals(HinemosModuleConstant.JOB) ||
				objectType.equals(HinemosModuleConstant.JOB_KICK) ||
				objectType.equals(HinemosModuleConstant.JOB_QUEUE) ||
				objectType.equals(HinemosModuleConstant.INFRA) ||
				objectType.equals(HinemosModuleConstant.INFRA_FILE) ||
				objectType.equals(HinemosModuleConstant.PLATFORM_CALENDAR) ||
				objectType.equals(HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN) ||
				objectType.equals(HinemosModuleConstant.PLATFORM_NOTIFY) ||
				objectType.equals(HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE) ||
				objectType.equals(HinemosModuleConstant.PLATFORM_REST_ACCESS) ||
				objectType.equals(HinemosModuleConstant.PLATFORM_COMMAND_TEMPLATE) ||
				objectType.equals(HinemosModuleConstant.SYSYTEM_MAINTENANCE) ||
				objectType.equals(HinemosModuleConstant.HUB_TRANSFER) ||
				objectType.equals(HinemosModuleConstant.SDML_CONTROL) ||
				objectType.equals(HinemosModuleConstant.JOBMAP_IMAGE_FILE) ||
				objectType.equals(HinemosModuleConstant.JOB_LINK_SEND) ||
				objectType.equals(HinemosModuleConstant.PLATFORM_REST_ACCESS ) ||
				objectType.equals(HinemosModuleConstant.PLATFORM_COMMAND_TEMPLATE) ||
				objectType.equals(HinemosModuleConstant.RPA_SCENARIO) ||
				objectType.equals(HinemosModuleConstant.RPA_SCENARIO_CREATE)) {
			return true;
		}
		return false;
	}
}
