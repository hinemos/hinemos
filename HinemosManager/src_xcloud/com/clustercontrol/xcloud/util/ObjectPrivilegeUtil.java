/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

public class ObjectPrivilegeUtil {
//	@SuppressWarnings("unchecked")
//	public static <T> T getObject(Class<T> objectClass, String objectIdName, String objectId, String userId, ObjectPrivilegeMode mode, IMessagesHolder messages) throws CloudManagerException {
//		EntityManagerEx em = Session.current().getEntityManagerEx();
//		Query query = em.createQuery(
//			"SELECT DISTINCT t, " +
//				"CASE " +
//					"WHEN t.ownerRoleId = r.roleId THEN r.roleId " +
//					"WHEN r.roleId = :ADMINISTRATORS THEN r.roleId " +
//					"ELSE null END, " +
//				"CASE " +
//					"WHEN (r.roleId <> :ADMINISTRATORS AND r.roleId = o.id.roleId AND o.id.objectPrivilege = :privilege) THEN o.id.roleId " +
//					"ELSE null END " +
//			"FROM " + objectClass.getSimpleName() + " AS t LEFT OUTER JOIN t.objectPrivileges AS o, UserInfo as h JOIN h.roleList r " +
//			"WHERE t." + objectIdName + " = :objectId AND h.userId = :userId");
//		query.setParameter("objectId", objectId);
//		query.setParameter("ADMINISTRATORS", "ADMINISTRATORS");
//		query.setParameter("userId", userId);
//		query.setParameter("privilege", mode.name());
//		
//		List<Object[]> results = (List<Object[]>)query.getResultList();
//		
//		// アクセス権のあるロールが格納されているか？
//		for (Object[] result: results) {
//			if (result[1] != null || result[2] != null) return (T)result[0];
//		}
//		
//		// 少なくとも該当オブジェクトが存在するか？
//		if (results.size() == 1) throw ErrorCode.OBJECTPRIVILEGE_UNAUTHORIZED_TO_TARGET_OBJECT.cloudManagerFault(messages.getString(objectClass.getSimpleName()), objectId, mode.name());
//		
//		// オブジェクトが存在しない場合のエラー。
//		throw ErrorCode.OBJECTPRIVILEGE_NOT_FOUND_TARGET_OBJECT.cloudManagerFault(messages.getString(objectClass.getSimpleName()), objectId);
//	}
//
//	@SuppressWarnings("unchecked")
//	public static <T> List<T> getObjects(Class<T> objectClass, String objectIdName, String userId, ObjectPrivilegeMode mode) throws CloudManagerException {
//		EntityManagerEx em = Session.current().getEntityManagerEx();
//		Query query = em.createQuery(
//				"SELECT DISTINCT t " +
//				"FROM " + objectClass.getSimpleName() + " AS t LEFT OUTER JOIN t.objectPrivileges AS o, UserEntity as h JOIN h.roleEntities r " +
//				"WHERE h.userId = :userId AND (r.roleId = t.ownerRoleId OR r.roleId = :ADMINISTRATORS OR (r.roleId = o.id.roleId AND o.id.objectPrivilege = :privilege))");
//		query.setParameter("ADMINISTRATORS", "ADMINISTRATORS");
//		query.setParameter("userId", userId);
//		query.setParameter("privilege", mode.name());
//		
//		return (List<T>)query.getResultList();
//	}
//
//	@SuppressWarnings("unchecked")
//	public static <T> List<T> getObjects(Class<T> objectClass, String objectIdName, List<String> objectIds, String userId, ObjectPrivilegeMode mode) throws CloudManagerException {
//		EntityManagerEx em = Session.current().getEntityManagerEx();
//		Query query = em.createQuery(
//				"SELECT DISTINCT t " +
//				"FROM " + objectClass.getSimpleName() + " AS t LEFT OUTER JOIN t.objectPrivileges AS o, UserEntity as h JOIN h.roleEntities r " +
//				"WHERE t." + objectIdName + " IN :objectIds AND h.userId = :userId AND (r.roleId = t.ownerRoleId OR r.roleId = :ADMINISTRATORS OR (r.roleId = o.id.roleId AND o.id.objectPrivilege = :privilege))");
//		query.setParameter("ADMINISTRATORS", "ADMINISTRATORS");
//		query.setParameter("userId", userId);
//		query.setParameter("objectIds", objectIds);
//		query.setParameter("privilege", mode.name());
//		
//		return (List<T>)query.getResultList();
//	}
}
