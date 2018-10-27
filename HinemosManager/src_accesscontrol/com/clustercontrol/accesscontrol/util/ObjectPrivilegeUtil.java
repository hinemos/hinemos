/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;

/**
 * オブジェクト権限チェックのUtilクラス
 *
 */
public class ObjectPrivilegeUtil {
	private static Log m_log = LogFactory.getLog(ObjectPrivilegeUtil.class);
	
	/** オブジェクト権限マップ（オブジェクト種別、Entityクラス） */
	private static Map<String, Class<?>> m_objectPrivilegeMap
	= new ConcurrentHashMap<String, Class<?>>();

	/** オブジェクト種別に対応したクラスの取得 */
	private static Class<?> getObjectPrivilegeClass(String objectType) {
		// オブジェクト権限マップの作成
		createObjectPrivilegeMap();
		return m_objectPrivilegeMap.get(objectType);
	}

	/** オブジェクト権限マップの作成 */
	private static void createObjectPrivilegeMap() {
		if (m_objectPrivilegeMap != null && m_objectPrivilegeMap.size() > 0) {
			return;
		}

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			EntityManagerFactory emf = jtm.getEntityManager().getEntityManagerFactory();
			Set<EntityType<?>> entityTypes = emf.getMetamodel().getEntities();
			String str = "";
			for (EntityType<?> entityType : entityTypes) {
				Class<?> clazz = entityType.getBindableJavaType();
				if (ObjectPrivilegeTargetInfo.class.isAssignableFrom(clazz)) {
					try {
						HinemosObjectPrivilege hinemosObjectPrivilege
						= clazz.getAnnotation(HinemosObjectPrivilege.class);
						String objectType = hinemosObjectPrivilege.objectType();
						if (hinemosObjectPrivilege.isModifyCheck()) {
							str += "[" + objectType + "," + clazz + "] ";
							
							if (m_objectPrivilegeMap.get(objectType) != null) {
								String message = "duplicate objectType=" + objectType +", clazz=" + clazz;
								m_log.info(message);
								throw new HinemosUnknown(message);
							}
							m_objectPrivilegeMap.put(objectType, clazz);
						}
					} catch (Exception e) {
						continue;
					}
				}
			}
			m_log.info("objectMap=" + str);
		}
	}

	/** オブジェクト権限の更新可否チェック */
	public static Object getObjectPrivilegeObject(String objectType, String objectId, ObjectPrivilegeMode mode)
			throws JobMasterNotFound, ObjectPrivilege_InvalidRole  {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Class<?> objectPrivilegeClass = getObjectPrivilegeClass(objectType);
			m_log.debug("class=" + objectPrivilegeClass + ", objectType=" + objectType + ", objectId=" + objectId);
			if (HinemosModuleConstant.JOB.equals(objectType)) {
				// JobMstEntityの場合は、objectId　!= PK
				return em.find(objectPrivilegeClass, new JobMstEntityPK(objectId, objectId), mode);
			} else {
				return em.find(objectPrivilegeClass, objectId, mode);
			}
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
}
