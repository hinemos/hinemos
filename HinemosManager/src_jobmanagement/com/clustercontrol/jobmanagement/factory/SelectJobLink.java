/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.QueryDivergence;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.jobmanagement.bean.JobLinkExpInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageFilter;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageList;
import com.clustercontrol.jobmanagement.model.JobLinkMessageEntity;
import com.clustercontrol.jobmanagement.model.JobLinkMessageExpInfoEntity;
import com.clustercontrol.jobmanagement.model.JobLinkSendSettingEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.repository.bean.FacilityIdConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;

import jakarta.persistence.TypedQuery;

/**
 * ジョブ連携関連情報を検索するクラスです。
 *
 */
public class SelectJobLink {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( SelectJobLink.class );
	/** 最大表示数(ジョブ連携メッセージ一覧) */
	private final static int MAX_DISPLAY_NUMBER = 500;
	/** 最大表示数(ジョブ連携メッセージ一覧の対象ノード) */
	private final static int MAX_DISPLAY_FACILITY_ID = 1000;
	/**
	 * ジョブ連携送信設定情報を取得します。
	 * 
	 * @param joblinkSendSettingId ジョブ連携送信設定ID
	 * @return ジョブ連携送信設定情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public JobLinkSendSettingEntity getJobLinkSendSetting(String joblinkSendSettingId) throws JobMasterNotFound, InvalidRole {
		return QueryUtil.getJobLinkSendSettingPK(joblinkSendSettingId, ObjectPrivilegeMode.READ);
	}

	/**
	 * ジョブ連携送信設定情報一覧を取得します。
	 * 
	 * @return ジョブ連携送信設定情報リスト
	 */
	public List<JobLinkSendSettingEntity> getAllJobLinkSendSettingList(String ownerRoleId) {
		List<JobLinkSendSettingEntity> list = null;
		if (ownerRoleId == null || ownerRoleId.isEmpty()) {
			// 全件取得
			list = QueryUtil.getAllJobLinkSendSettingList();
		} else {
			// オーナーロールIDを条件として全件取得
			list = QueryUtil.getAllJobLinkSendSettingList_OR(ownerRoleId);
		}
		return new ArrayList<>(list);
	}

	/**
	 * 検索条件に一致するジョブ連携メッセージ一覧情報を取得します。<BR>
	 * 表示履歴数を越えた場合は、表示履歴数分の一覧を返します。
	 *
	 * @param userId ログインユーザのユーザID
	 * @param property 検索条件
	 * @param histories 表示履歴数
	 * @return ジョブ連携メッセージ一覧情報
	 * @throws HinemosUnknown
	 */
	public JobLinkMessageList getJobLinkMessageList(String userId, JobLinkMessageFilter property, int histories)
			throws HinemosUnknown {

		m_log.debug("getJobLinkMessageList() start : userId = " + userId + ", histories = " + histories);

		String joblinkMessageId = null;
		String srcFacilityId = null;
		String srcFacilityName = null;
		String monitorDetailId = null;
		String application = null;
		Integer[] priorityList = null;
		String message = null;
		Long sendFromDate = null;
		Long sendToDate = null;
		Long acceptFromDate = null;
		Long acceptToDate = null;

		if (property != null) {
			joblinkMessageId = property.getJoblinkMessageId();
			srcFacilityId = property.getSrcFacilityId();
			srcFacilityName = property.getSrcFacilityName();
			monitorDetailId = property.getMonitorDetailId();
			application = property.getApplication();
			priorityList = property.getPriorityList();
			message = property.getMessage();
			sendFromDate = property.getSendFromDate();
			sendToDate = property.getSendToDate();
			acceptFromDate = property.getAcceptFromDate();
			acceptToDate = property.getAcceptToDate();
		} else {
			m_log.debug("getJobLinkMessageList() property is null");
		}

		JobLinkMessageList list = new JobLinkMessageList();
		ArrayList<JobLinkMessageInfo> jobLinkMessageList = new ArrayList<>();
		int total = 0;

		if(histories <= 0){
			histories = MAX_DISPLAY_NUMBER;
		}
		Integer limit = histories + 1;

		//検索条件に該当するセッションを取得
		TypedQuery<?> typedQuery
			= getJobLinkMesageFilterQuery(
					joblinkMessageId,
					srcFacilityId,
					srcFacilityName,
					monitorDetailId,
					application,
					priorityList,
					message,
					sendFromDate,
					sendToDate,
					acceptFromDate,
					acceptToDate,
					false);
		if (typedQuery == null) {
			m_log.debug("getJobLinkMessageList() : target facilityId is not found.");
			list.setTotal(0);
			list.setList(jobLinkMessageList);
			return list;
		}
		if(limit != null){
			typedQuery = typedQuery.setMaxResults(limit);
		}

		@SuppressWarnings("unchecked")
		List<JobLinkMessageEntity> entityList = (List<JobLinkMessageEntity>)typedQuery.getResultList();

		if(entityList != null){

			//履歴数をカウント
			if(entityList.size() > histories){
				//最大表示件数より大きい場合
				TypedQuery<?> countTypedQuery
				= getJobLinkMesageFilterQuery(
						joblinkMessageId,
						srcFacilityId,
						srcFacilityName,
						monitorDetailId,
						application,
						priorityList,
						message,
						sendFromDate,
						sendToDate,
						acceptFromDate,
						acceptToDate,
						true);
				total = (int)((Long)countTypedQuery.getSingleResult()).longValue();
			}
			else{
				total = entityList.size();
			}
			m_log.debug("getJobLinkMessageList() total = " + total);

			for(JobLinkMessageEntity entity : entityList) {
				JobLinkMessageInfo info = new JobLinkMessageInfo();
				info.setJoblinkMessageId(entity.getId().getJoblinkMessageId());
				info.setFacilityId(entity.getId().getFacilityId());
				info.setSendDate(entity.getId().getSendDate());
				info.setAcceptDate(entity.getAcceptDate());
				info.setFacilityName(entity.getFacilityName());
				info.setIpAddress(entity.getIpAddress());
				info.setMonitorDetailId(entity.getMonitorDetailId());
				info.setApplication(entity.getApplication());
				info.setPriority(entity.getPriority());
				info.setMessage(entity.getMessage());
				info.setMessageOrg(entity.getMessageOrg());
				info.setJobLinkExpInfo(new ArrayList<>());
				if (entity.getJobLinkMessageExpInfoEntities() != null) {
					for (JobLinkMessageExpInfoEntity expEntity : entity.getJobLinkMessageExpInfoEntities()) {
						JobLinkExpInfo expInfo = new JobLinkExpInfo();
						expInfo.setKey(expEntity.getId().getKey());
						expInfo.setValue(expEntity.getValue());
						info.getJobLinkExpInfo().add(expInfo);
					}
				}
				jobLinkMessageList.add(info);

				//取得した履歴を最大表示件数まで格納したら終了
				if(jobLinkMessageList.size() >= histories)
					break;
			}
		}
		list.setTotal(total);
		list.setList(jobLinkMessageList);

		return list;
	}

	/**
	 * TypedQuery作成
	 *
	 * @param joblinkMessageId ジョブ連携メッセージID
	 * @param srcFacilityId ファシリティID
	 * @param srcFacilityName ファシリティ名
	 * @param monitorDetailId 監視詳細
	 * @param application = アプリケーション
	 * @param priorityList 重要度リスト
	 * @param message メッセージ
	 * @param sendFromDate 送信日時（From）
	 * @param sendToDate = 送信日時（To）
	 * @param acceptFromDate = 受信日時（From）
	 * @param acceptToDate = 受信日時（To）
	 * @param isCount true:件数を取得する
	 * @return typedQuery
	 * @throws HinemosUnknown
	 */
	private TypedQuery<?> getJobLinkMesageFilterQuery(
			String joblinkMessageId,
			String srcFacilityId,
			String srcFacilityName,
			String monitorDetailId,
			String application,
			Integer[] priorityList,
			String message,
			Long sendFromDate,
			Long sendToDate,
			Long acceptFromDate,
			Long acceptToDate,
			boolean isCount) throws HinemosUnknown{

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 対象ノードのファシリティID取得
			Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
			boolean facilityIdCheck = false;
			String[] facilityIds = null;
			if (!isAdministrator && (srcFacilityId == null || srcFacilityId.isEmpty())) {
				srcFacilityId = FacilityIdConstant.ROOT;
			}
			if (srcFacilityId != null && !srcFacilityId.isEmpty()) {
				facilityIdCheck = true;
				List<String> facilityIdList = new RepositoryControllerBean().getNodeFacilityIdList(
						srcFacilityId, null, RepositoryControllerBean.ALL);
				if (facilityIdList == null || facilityIdList.isEmpty()) {
					m_log.info("getJobLinkMesageFilterQuery() target facilityId is not found. : facilityId=" + srcFacilityId);
					return null;
				}
				if (facilityIdList.size() > MAX_DISPLAY_FACILITY_ID) {
					facilityIds = facilityIdList.subList(0, MAX_DISPLAY_FACILITY_ID).toArray(new String[MAX_DISPLAY_FACILITY_ID]);
				} else {
					facilityIds = facilityIdList.toArray(new String[facilityIdList.size()]);
				}
			}

			// 「含まない」検索を行うかの判断に使う値
			String notInclude = "NOT:";

			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT");
			if (isCount) {
				sbJpql.append(" COUNT(a)");
			} else {
				sbJpql.append(" a");
			}
			sbJpql.append(" FROM JobLinkMessageEntity a");
			sbJpql.append(" WHERE true = true");
			if(joblinkMessageId != null && joblinkMessageId.length() > 0) {
				if(!joblinkMessageId.startsWith(notInclude)) {
					sbJpql.append(" AND a.id.joblinkMessageId like :joblinkMessageId");
				}else {
					sbJpql.append(" AND a.id.joblinkMessageId not like :joblinkMessageId");
				}
			}
			if(facilityIdCheck) {
				sbJpql.append(" AND a.id.facilityId IN (" + 
						HinemosEntityManager.getParamNameString("facilityId", facilityIds) + ")");
			}
			if(srcFacilityName != null && srcFacilityName.length() > 0) {
				if(!srcFacilityName.startsWith(notInclude)) {
					sbJpql.append(" AND a.facilityName like :facilityName");
				}else {
					sbJpql.append(" AND a.facilityName not like :facilityName");
				}
			}
			if(monitorDetailId != null && monitorDetailId.length() > 0) {
				if(!monitorDetailId.startsWith(notInclude)) {
					sbJpql.append(" AND a.monitorDetailId like :monitorDetailId");
				}else {
					sbJpql.append(" AND a.monitorDetailId not like :monitorDetailId");
				}
			}
			if(application != null && application.length() > 0) {
				if(!application.startsWith(notInclude)) {
					sbJpql.append(" AND a.application like :application");
				}else {
					sbJpql.append(" AND a.application not like :application");
				}
			}
			boolean notPriorityCheck = true;
			if (priorityList != null && priorityList.length != 0) {
				for (int defaultPriority : PriorityConstant.PRIORITY_LIST) {
					if (!Arrays.asList(priorityList).contains(defaultPriority)) {
						notPriorityCheck = false;
						break;
					}
				}
			}
			if (!notPriorityCheck) {
				sbJpql.append(" AND a.priority IN (" + 
						HinemosEntityManager.getParamNameString("priority", priorityList) + ")");
			}
			if(message != null && message.length() > 0) {
				if(!message.startsWith(notInclude)) {
					sbJpql.append(" AND a.message like :message");
				}else {
					sbJpql.append(" AND a.message not like :message");
				}
			}
			if(sendFromDate != null) {
				sbJpql.append(" AND a.id.sendDate >= :sendFromDate");
			}
			if(sendToDate != null) {
				sbJpql.append(" AND a.id.sendDate <= :sendToDate");
			}
			if(acceptFromDate != null) {
				sbJpql.append(" AND a.acceptDate >= :acceptFromDate");
			}
			if(acceptToDate != null) {
				sbJpql.append(" AND a.acceptDate <= :acceptToDate");
			}

			TypedQuery<?> typedQuery = null;
			m_log.debug("getJobLinkMesageFilterQuery() jpql = " + sbJpql.toString());
			if (isCount) {
				typedQuery = em.createQuery(sbJpql.toString(), Long.class, JobLinkMessageEntity.class);
			} else {
				sbJpql.append(" ORDER BY a.acceptDate DESC");
				typedQuery = em.createQuery(sbJpql.toString(), JobLinkMessageEntity.class);
			}
			if(joblinkMessageId != null && joblinkMessageId.length() > 0) {
				if(!joblinkMessageId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("joblinkMessageId",
							QueryDivergence.escapeLikeCondition(joblinkMessageId));
				}else {
					typedQuery = typedQuery.setParameter("joblinkMessageId",
							QueryDivergence.escapeLikeCondition(joblinkMessageId.substring(notInclude.length())));
				}
			}
			if(facilityIdCheck) {
				typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", facilityIds);
			}
			if(srcFacilityName != null && srcFacilityName.length() > 0) {
				if(!srcFacilityName.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("facilityName",
							QueryDivergence.escapeLikeCondition(srcFacilityName));
				}else {
					typedQuery = typedQuery.setParameter("facilityName",
							QueryDivergence.escapeLikeCondition(srcFacilityName.substring(notInclude.length())));
				}
			}
			if(monitorDetailId != null && monitorDetailId.length() > 0) {
				if(!monitorDetailId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("monitorDetailId",
							QueryDivergence.escapeLikeCondition(monitorDetailId));
				}else {
					typedQuery = typedQuery.setParameter("monitorDetailId",
							QueryDivergence.escapeLikeCondition(monitorDetailId.substring(notInclude.length())));
				}
			}
			if(application != null && application.length() > 0) {
				if(!application.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("application",
							QueryDivergence.escapeLikeCondition(application));
				}else {
					typedQuery = typedQuery.setParameter("application",
							QueryDivergence.escapeLikeCondition(application.substring(notInclude.length())));
				}
			}
			if (!notPriorityCheck) {
				for (int i = 0 ; i < priorityList.length ; i++) {
					typedQuery.setParameter("priority" + i, priorityList[i]);
				}
			}
			if(message != null && message.length() > 0) {
				if(!message.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("message",
							QueryDivergence.escapeLikeCondition(message));
				}else {
					typedQuery = typedQuery.setParameter("message",
							QueryDivergence.escapeLikeCondition(message.substring(notInclude.length())));
				}
			}
			if(sendFromDate != null) {
				typedQuery = typedQuery.setParameter("sendFromDate", sendFromDate);
			}
			if(sendToDate != null) {
				typedQuery = typedQuery.setParameter("sendToDate", sendToDate);
			}
			if(acceptFromDate != null) {
				typedQuery = typedQuery.setParameter("acceptFromDate", acceptFromDate);
			}
			if(acceptToDate != null) {
				typedQuery = typedQuery.setParameter("acceptToDate", acceptToDate);
			}

			return typedQuery;
		}
	}
}
