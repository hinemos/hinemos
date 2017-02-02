/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.monitor.util;

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
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.model.EventLogEntityPK;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;
import com.clustercontrol.notify.monitor.model.StatusInfoEntityPK;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static StatusInfoEntity getStatusInfoPK(StatusInfoEntityPK pk) throws MonitorNotFound, InvalidRole {
		return getStatusInfoPK(pk, ObjectPrivilegeMode.READ);
	}

	public static StatusInfoEntity getStatusInfoPK(StatusInfoEntityPK pk, ObjectPrivilegeMode mode) throws MonitorNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		StatusInfoEntity entity = null;
		try {
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
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<StatusInfoEntity> list
		= em.createNamedQuery("StatusInfoEntity.findExpirationStatus", StatusInfoEntity.class)
		.setParameter("expirationDate", expirationDate).getResultList();
		return list;
	}

	public static EventLogEntity getEventLogPK(EventLogEntityPK pk) throws EventLogNotFound, InvalidRole {
		return getEventLogPK(pk, ObjectPrivilegeMode.READ);
	}

	public static EventLogEntity getEventLogPK(EventLogEntityPK pk, ObjectPrivilegeMode mode) throws EventLogNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		EventLogEntity entity = null;
		try {
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

	public static int updateEventLogFlgByFilter(String[] facilityIds,
			Integer[] priorityList,
			Long outputFromDate,
			Long outputToDate,
			Long generationFromDate,
			Long generationToDate,
			String monitorId,
			String monitorDetailId,
			String application,
			String message,
			Integer confirmFlg,
			String confirmUser,
			String comment,
			String commentUser,
			Integer confirmType,
			Long confirmDate,
			Boolean collectGraphFlg) {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		// 「含まない」検索を行うかの判断に使う値
		String notInclude = "NOT:";

		// 引数と反対の確認状態を取得する
		Integer selectConfirmFlg = null;
		if(confirmType == ConfirmConstant.TYPE_CONFIRMED){
			selectConfirmFlg = ConfirmConstant.TYPE_UNCONFIRMED;
		}
		else if(confirmType == ConfirmConstant.TYPE_UNCONFIRMED){
			selectConfirmFlg = ConfirmConstant.TYPE_CONFIRMED;
		}

		// SQL作成
		StringBuffer sbJpql = new StringBuffer();
		sbJpql.append("UPDATE EventLogEntity a SET a.confirmFlg = :confirmFlg, a.confirmDate = :confirmDate, a.confirmUser = :confirmUser");
		sbJpql.append(" WHERE true = true");

		// ファシリティID設定
		if(facilityIds != null && facilityIds.length>0) {
			sbJpql.append(" AND a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", facilityIds) + ")");
		}
		// 重要度設定
		if (priorityList != null && priorityList.length>0 && priorityList.length != PriorityConstant.PRIORITY_LIST.length) {
			sbJpql.append(" AND a.priority IN (" + HinemosEntityManager.getParamNameString("priority", new String[priorityList.length]) + ")");
		}
		// 受信日時（自）設定
		if(outputFromDate != null) {
			sbJpql.append(" AND a.id.outputDate >= :outputFromDate");
		}
		// 受信日時（至）設定
		if(outputToDate != null) {
			sbJpql.append(" AND a.id.outputDate <= :outputToDate ");
		}
		// 出力日時（自）設定
		if(generationFromDate != null) {
			sbJpql.append(" AND a.generationDate >= :generationFromDate ");
		}
		// 出力日時（至）設定
		if(generationToDate != null) {
			sbJpql.append(" AND a.generationDate <= :generationToDate ");
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
		// コメント
		if(comment != null && !"".equals(comment)){
			if(!comment.startsWith(notInclude)) {
				sbJpql.append(" AND a.comment like :comment");
			}
			else {
				sbJpql.append(" AND a.comment not like :comment");
			}
		}
		//コメントユーザ
		if(commentUser != null && !"".equals(commentUser)){
			if(!commentUser.startsWith(notInclude)) {
				sbJpql.append(" AND a.commentUser like :commentUser");
			}
			else {
				sbJpql.append(" AND a.commentUser not like :commentUser");
			}
		}
		// 確認有無
		if(selectConfirmFlg != null) {
			sbJpql.append(" AND a.confirmFlg = :selectConfirmFlg ");
		}

		TypedQuery<Integer> query = em.createQuery(sbJpql.toString(), Integer.class, EventLogEntity.class, ObjectPrivilegeMode.MODIFY)
				.setParameter("confirmFlg", confirmFlg)
				.setParameter("confirmDate", confirmDate)
				.setParameter("confirmUser", confirmUser);
		// ファシリティID設定
		if(facilityIds != null && facilityIds.length>0) {
			query = HinemosEntityManager.appendParam(query, "facilityId", facilityIds);
		}
		// 重要度設定
		if (priorityList != null && priorityList.length>0 && priorityList.length != PriorityConstant.PRIORITY_LIST.length){
			int count = priorityList.length;
			if (count > 0) {
				for (int i = 0 ; i < count ; i++) {
					query = query.setParameter("priority" + i, priorityList[i]);
				}
			}
		}
		// 受信日時（自）設定
		if(outputFromDate != null) {
			query.setParameter("outputFromDate", outputFromDate);
		}
		// 受信日時（至）設定
		if(outputToDate != null) {
			query.setParameter("outputToDate", outputToDate);
		}
		// 出力日時（自）設定
		if(generationFromDate != null) {
			query.setParameter("generationFromDate", generationFromDate);
		}
		// 出力日時（至）設定
		if(generationToDate != null) {
			query.setParameter("generationToDate", generationToDate);
		}
		// 監視項目ID設定
		if(monitorId != null && !"".equals(monitorId)) {
			if(!monitorId.startsWith(notInclude)) {
				query = query.setParameter("monitorId", monitorId);
			}
			else {
				query = query.setParameter("monitorId", monitorId.substring(notInclude.length()));
			}
		}
		// 監視詳細設定
		if(monitorDetailId != null && !"".equals(monitorDetailId)) {
			if(!monitorDetailId.startsWith(notInclude)) {
				query = query.setParameter("monitorDetailId", monitorDetailId);
			}
			else {
				query = query.setParameter("monitorDetailId", monitorDetailId.substring(notInclude.length()));
			}
		}
		// アプリケーション設定
		if(application != null && !"".equals(application)) {
			if(!application.startsWith(notInclude)) {
				query = query.setParameter("application", application);
			}
			else {
				query = query.setParameter("application", application.substring(notInclude.length()));
			}
		}
		// メッセージ設定
		if(message != null && !"".equals(message)) {
			if(!message.startsWith(notInclude)) {
				query = query.setParameter("message", message);
			}
			else {
				query = query.setParameter("message", message.substring(notInclude.length()));
			}
		}
		//コメント
		if(comment != null && !"".equals(comment)){
			if(!comment.startsWith(notInclude)) {
				query = query.setParameter("comment", comment);
			}
			else {
				query = query.setParameter("comment", comment.substring(notInclude.length()));
			}
		}
		//コメントユーザ
		if(commentUser != null && !"".equals(commentUser)){
			if(!commentUser.startsWith(notInclude)) {
				query = query.setParameter("commentUser", commentUser);
			}
			else {
				query = query.setParameter("commentUser", commentUser.substring(notInclude.length()));
			}
		}
		// 確認有無
		if(selectConfirmFlg != null) {
			query.setParameter("selectConfirmFlg", selectConfirmFlg);
		}
		// 性能グラフ用フラグ
		if (collectGraphFlg != null) {
			query.setParameter("collectGraphFlg", collectGraphFlg);
		}

		return query.executeUpdate();
	}

	// TODO
	// updateEventLogFlgByFilterとgetEventLogByFilterが同じ処理が多い。統合すること。
	public static List<EventLogEntity> getEventLogByFilter(String[] facilityIds,
			Integer[] priorityList,
			Long outputFromDate,
			Long outputToDate,
			Long generationFromDate,
			Long generationToDate,
			String monitorId,
			String monitorDetailId,
			String application,
			String message,
			Integer confirmFlg,
			String confirmUser,
			String comment,
			String commentUser,
			Boolean collectGraphFlg,
			String ownerRoleId,
			Boolean orderByFlg,
			Integer limit) {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		
		// 「含まない」検索を行うかの判断に使う値
		String notInclude = "NOT:";
		
		StringBuffer sbJpql = new StringBuffer();
		sbJpql.append("SELECT a FROM EventLogEntity a WHERE true = true");
		// ファシリティID設定
		if(facilityIds != null && facilityIds.length>0) {
			sbJpql.append(" AND a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", facilityIds) + ")");
		}
		// 重要度設定
		if (priorityList != null && priorityList.length>0 && priorityList.length != PriorityConstant.PRIORITY_LIST.length) {
			sbJpql.append(" AND a.priority IN (" + HinemosEntityManager.getParamNameString("priority", new String[priorityList.length]) + ")");
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
		// 確認有無
		if(confirmFlg != null) {
			sbJpql.append(" AND a.confirmFlg = :confirmFlg");
		}
		// 確認ユーザ
		if(confirmUser != null &&  !"".equals(confirmUser)) {
			if(!confirmUser.startsWith(notInclude)) {
				sbJpql.append(" AND a.confirmUser like :confirmUser");
			}
			else {
				sbJpql.append(" AND a.confirmUser not like :confirmUser");
			}
		}
		//コメント
		if(comment != null && !"".equals(comment)){
			if(!comment.startsWith(notInclude)) {
				sbJpql.append(" AND a.comment like :comment");
			}
			else {
				sbJpql.append(" AND a.comment not like :comment");
			}
		}
		//コメントユーザ
		if(commentUser != null && !"".equals(commentUser)){
			if(!commentUser.startsWith(notInclude)) {
				sbJpql.append(" AND a.commentUser like :commentUser");
			}
			else {
				sbJpql.append(" AND a.commentUser not like :commentUser");
			}
		}
		//性能グラフ用フラグ
		if(collectGraphFlg != null) {
			sbJpql.append(" AND a.collectGraphFlg = :collectGraphFlg");
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
		// ソート
		if (orderByFlg) {
			sbJpql.append(" ORDER BY a.id.outputDate");
		} else {
			sbJpql.append(" ORDER BY a.id.outputDate DESC");
		}
		TypedQuery<EventLogEntity> typedQuery
		= em.createQuery(sbJpql.toString(), EventLogEntity.class);
		// ファシリティID設定
		if(facilityIds != null && facilityIds.length>0) {
			typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", facilityIds);
		}
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
		// 確認有無
		if(confirmFlg != null) {
			typedQuery = typedQuery.setParameter("confirmFlg", confirmFlg);
		}
		// 確認ユーザ
		if(confirmUser != null && !"".equals(confirmUser)) {
			if(!confirmUser.startsWith(notInclude)) {
				typedQuery = typedQuery.setParameter("confirmUser", confirmUser);
			}
			else {
				typedQuery = typedQuery.setParameter("confirmUser", confirmUser.substring(notInclude.length()));
			}
		}
		//コメント
		if(comment != null && !"".equals(comment)){
			if(!comment.startsWith(notInclude)) {
				typedQuery = typedQuery.setParameter("comment", comment);
			}
			else {
				typedQuery = typedQuery.setParameter("comment", comment.substring(notInclude.length()));
			}
		}
		//コメントユーザ
		if(commentUser != null && !"".equals(commentUser)){
			if(!commentUser.startsWith(notInclude)) {
				typedQuery = typedQuery.setParameter("commentUser", commentUser);
			}
			else {
				typedQuery = typedQuery.setParameter("commentUser", commentUser.substring(notInclude.length()));
			}
		}
		//性能グラフ用フラグ
		if(collectGraphFlg != null){
			typedQuery = typedQuery.setParameter("collectGraphFlg", collectGraphFlg);
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
		if (limit != null) {
			typedQuery = typedQuery.setMaxResults(limit);
		}

		return typedQuery.getResultList();
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

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		StringBuffer sbJpql = new StringBuffer();
		sbJpql.append("SELECT a FROM EventLogEntity a WHERE true = true");
		// ファシリティID設定
		if(facilityIds != null && facilityIds.length>0) {
			sbJpql.append(" AND a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", facilityIds) + ")");
		}
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
		TypedQuery<EventLogEntity> typedQuery
		= em.createQuery(sbJpql.toString(), EventLogEntity.class);
		// ファシリティID設定
		if(facilityIds != null && facilityIds.length>0) {
			typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", facilityIds);
		}
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

		return typedQuery.getResultList();
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

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		
		// 「含まない」検索を行うかの判断に使う値
		String notInclude = "NOT:";
		
		StringBuffer sbJpql = new StringBuffer();
		sbJpql.append("SELECT a FROM StatusInfoEntity a WHERE true = true");
		// ファシリティID設定
		if(facilityIds != null && facilityIds.length>0) {
			sbJpql.append(" AND a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", facilityIds) + ")");
		}
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

		TypedQuery<StatusInfoEntity> typedQuery
		= em.createQuery(sbJpql.toString(), StatusInfoEntity.class);
		// ファシリティID設定
		if(facilityIds != null && facilityIds.length>0) {
			typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", facilityIds);
		}
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

		return typedQuery.getResultList();
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

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		StringBuffer sbJpql = new StringBuffer();
		sbJpql.append("SELECT a FROM StatusInfoEntity a WHERE true = true");
		// ファシリティID設定
		if(facilityIds != null && facilityIds.length>0) {
			sbJpql.append(" AND a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", facilityIds) + ")");
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
		TypedQuery<StatusInfoEntity> typedQuery
		= em.createQuery(sbJpql.toString(), StatusInfoEntity.class);
		// ファシリティID設定
		if(facilityIds != null && facilityIds.length>0) {
			typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", facilityIds);
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
		// オーナーロールID設定
		if(ownerRoleId != null && !"".equals(ownerRoleId)) {
			typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId);
		}
		typedQuery = typedQuery.setMaxResults(1);
		return typedQuery.getResultList();
	}
}
