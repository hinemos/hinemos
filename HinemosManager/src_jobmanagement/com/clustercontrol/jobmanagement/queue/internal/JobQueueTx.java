/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.queue.internal;

import java.util.List;

import javax.persistence.TypedQuery;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.Transaction;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueReferrerViewInfo.JobQueueReferrerViewInfoListItem;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSettingViewFilter;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSettingViewInfo.JobQueueSettingViewInfoListItem;
import com.clustercontrol.jobmanagement.queue.internal.CriteriaBuilder.OperationType;

/**
 * ジョブキュー特有の永続化データアクセスを提供します。
 *
 * @since 6.2.0
 */
public class JobQueueTx extends Transaction {

	public JobQueueEntity findJobQueueEntitiy(String queueId) {
		return getEm().find(JobQueueEntity.class, queueId, ObjectPrivilegeMode.NONE);
	}

	public JobQueueEntity findJobQueueEntitiyForRead(String queueId) {
		return getEm().find(JobQueueEntity.class, queueId, ObjectPrivilegeMode.READ);
	}

	public JobQueueEntity findJobQueueEntitiyForModify(String queueId) {
		return getEm().find(JobQueueEntity.class, queueId, ObjectPrivilegeMode.MODIFY);
	}

	public JobQueueItemEntity findJobQueueItemEntity(JobQueueItemEntityPK id) {
		return getEm().find(JobQueueItemEntity.class, id, ObjectPrivilegeMode.NONE);
	}
	
	public List<JobQueueEntity> findJobQueueEntities() {
		return getEm().createNamedQuery("JobQueueEntity.findAll", JobQueueEntity.class).getResultList();
	}

	public List<JobQueueEntity> findJobQueueEntitiesWithRoleForRead(String roleId) {
		return getEm().createNamedQuery_OR("JobQueueEntity.findAll", JobQueueEntity.class, ObjectPrivilegeMode.READ,
				roleId).getResultList();
	}
	
	public List<JobQueueItemEntity> findJobQueueItemEntities(String queueId) {
		return getEm().createNamedQuery("JobQueueItemEntity.findByQueueId", JobQueueItemEntity.class)
				.setParameter("queueId", queueId).getResultList();
	}

	public List<JobQueueItemEntity> findJobQueueItemEntitiesByStatus(String queueId, int statusId) {
		return getEm().createNamedQuery("JobQueueItemEntity.findByQueueIdAndStatusId", JobQueueItemEntity.class)
				.setParameter("queueId", queueId).setParameter("statusId", statusId).getResultList();
	}
	
	public long countJobQueueItem(String queueId) {
		return getEm().createNamedQuery("JobQueueItemEntity.countByQueueId", Long.class)
				.setParameter("queueId", queueId).getSingleResult().longValue();
	}
	
	public long countJobQueueItemByStatus(String queueId, int statusId) {
		return getEm().createNamedQuery("JobQueueItemEntity.countByQueueIdAndStatusId", Long.class)
				.setParameter("queueId", queueId).setParameter("statusId", statusId).getSingleResult().longValue();
	}

	public List<Object[]> countJobQueueItemPerQueueId(int statusId) {
		return getEm().createNamedQuery("JobQueueItemEntity.countPerQueueId", Object[].class)
				.setParameter("statusId", statusId).getResultList();
	}
	
	public List<String> findJobIdByQueueId(String queueId) {
		return getEm().createNamedQuery("JobMstEntity.findJobIdByQueueId", String.class)
				.setParameter("queueId", queueId).getResultList();
	}
	
	public List<JobQueueSettingViewInfoListItem> findSettingViewInfoListItemForRead(JobQueueSettingViewFilter filter) {
		String entityName = JobQueueEntity.class.getSimpleName();
		CriteriaBuilder criteria = new CriteriaBuilder()
				.add(entityName, "queueId", filter.getQueueId(), OperationType.LIKE)
				.add(entityName, "name", filter.getQueueName(), OperationType.LIKE)
				.add(entityName, "concurrency", filter.getConcurrencyFrom(), OperationType.FROM)
				.add(entityName, "concurrency", filter.getConcurrencyTo(), OperationType.TO)
				.add(entityName, "ownerRoleId", filter.getOwnerRoleId(), OperationType.LIKE)
				.add(entityName, "regUser", filter.getRegUser(), OperationType.LIKE)
				.add(entityName, "regDate", filter.getRegDateFrom(), OperationType.FROM)
				.add(entityName, "regDate", filter.getRegDateTo(), OperationType.TO)
				.add(entityName, "updateUser", filter.getUpdateUser(), OperationType.LIKE)
				.add(entityName, "updateDate", filter.getUpdateDateFrom(), OperationType.FROM)
				.add(entityName, "updateDate", filter.getUpdateDateTo(), OperationType.TO);		
		
		TypedQuery<JobQueueSettingViewInfoListItem> query = getEm().createQuery(
				"SELECT NEW " + JobQueueSettingViewInfoListItem.class.getName() + "("
						+ "a.queueId, a.name, a.concurrency, a.ownerRoleId, "
						+ "a.regDate, a.regUser, a.updateDate, a.updateUser"
						+ ") FROM " + entityName + " a "
						+ "WHERE " + criteria.build(entityName + ":a"),
						JobQueueSettingViewInfoListItem.class,
						JobQueueEntity.class, // オブジェクト権限チェックのため別にEntityクラスを渡す
						ObjectPrivilegeMode.READ);
		criteria.fillParameters(query);
		return query.getResultList();
	}
	
	public List<JobQueueReferrerViewInfoListItem> findReferrerViewInfoListItemForRead(String queueId) {
		String entityName = JobMstEntity.class.getSimpleName();
		TypedQuery<JobQueueReferrerViewInfoListItem> query = getEm().createQuery(
				"SELECT NEW " + JobQueueReferrerViewInfoListItem.class.getName() + "("
						+ "a.id.jobunitId, a.id.jobId, a.ownerRoleId"
						+ ") FROM " + entityName + " a "
						+ "WHERE a.queueFlg = TRUE AND a.queueId = :queueId",
						JobQueueReferrerViewInfoListItem.class,
						JobMstEntity.class, // オブジェクト権限チェックのため別にEntityクラスを渡す
						ObjectPrivilegeMode.READ);
		query.setParameter("queueId", queueId);
		return query.getResultList();
	}
}
