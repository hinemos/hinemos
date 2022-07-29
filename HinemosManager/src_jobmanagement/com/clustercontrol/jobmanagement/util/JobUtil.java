/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.bean.FunctionPrefixEnum;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobTreeItemResponseP1;

/**
 * ジョブユーティリティクラス
 * 
 * 以下ィを提供します。<BR>
 * <li>ジョブツリーアイテムに関するユーティリティ
 * <li>ログインユーザが参照可能なジョブユニットかどうかをチェックするユーティリティ
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class JobUtil {
	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( JobUtil.class );

	/**
	 * 有効なジョブの存在チェック
	 * @param item
	 * @param jobId
	 * @param selectType
	 * @return -1:有効なジョブ無し、0:有効なジョブ有り、1:モジュール登録設定不一致
	 */
	public static int checkValidJob(JobTreeItem item, String jobId, Integer selectType) {
		if(item == null || item.getData() == null){
			return -1;
		}
		//ジョブIDをチェック
		JobInfo info = item.getData();
		int type = info.getType();
		if (jobId.equals(info.getId())) {
			if (type == JobConstant.TYPE_JOB 
					|| type == JobConstant.TYPE_FILEJOB
					|| type == JobConstant.TYPE_APPROVALJOB
					|| type == JobConstant.TYPE_MONITORJOB
					|| type == JobConstant.TYPE_JOBLINKSENDJOB
					|| type == JobConstant.TYPE_JOBLINKRCVJOB
					|| type == JobConstant.TYPE_FILECHECKJOB
					|| type == JobConstant.TYPE_RESOURCEJOB
					|| type == JobConstant.TYPE_RPAJOB) {
				if(selectType == 1 && !info.isRegisteredModule()){
					//存在していても、モジュール登録済みフラグが解除されていれば無効とする
					return 1;
				}else{
					return 0;
				}
			}
		}
		//子JobTreeItemを取得
		for(JobTreeItem child : item.getChildren()){
			int ret = checkValidJob(child, jobId, selectType);
			if(ret != -1) {
				return ret;
			}
		}
		return -1;
	}

	/**
	 * 有効なジョブネットの存在チェック
	 * @param item
	 * @param jobNetId
	 * @param referJobNet
	 * @return -1:有効なジョブネット無し、0:有効なジョブネット有り、1:モジュール登録設定不一致、2:配下に参照ジョブネットが含まれる
	 */
	public static int checkValidJobNet(JobTreeItem item, String jobNetId, JobInfo referJobNet) {
		if(item == null || item.getData() == null){
			return -1;
		}
		//ジョブIDをチェック
		JobInfo info = item.getData();
		int type = info.getType();
		if (jobNetId.equals(info.getId())) {
			if (type == JobConstant.TYPE_JOBNET) {
				if(referJobNet.getReferJobSelectType() == 1 && !info.isRegisteredModule()){
					//存在していても、モジュール登録済みフラグが解除されていれば無効とする
					return 1;
				}
				// 配下のジョブのジョブタイプをチェック(参照ジョブネットが含まれないこと)
				if (isExistReferJobNet(item)){
					return 2;
				}
				return 0;
			}
		}
		//子JobTreeItemを取得
		for(JobTreeItem child : item.getChildren()){
			int ret = checkValidJobNet(child, jobNetId, referJobNet);
			if(ret != -1) {
				return ret;
			}
		}
		return -1;
	}

	/**
	 * 参照ジョブネットの存在チェック
	 * @param item
	 * @param jobId
	 * @return
	 */
	public static boolean isExistReferJobNet(JobTreeItem item) {
		if(item == null || item.getData() == null){
			return false;
		}
		// ジョブタイプをチェック
		if (item.getData().getType() == JobConstant.TYPE_REFERJOBNET) {
			return true;
		}
		
		//子JobTreeItemを取得
		for(JobTreeItem child : item.getChildren()){
			if (isExistReferJobNet(child)){
				return true;
			}
		}
		return false;
	}

	/**
	 * ジョブユニット内の参照ジョブのみを取得する
	 * @param item
	 * @param list
	 * @return
	 */
	public static ArrayList<JobInfo> findReferJob(JobTreeItem item){
		ArrayList<JobInfo> ret = new ArrayList<JobInfo>();
		if(item == null || item.getData() == null){
			return ret;
		}
		//ジョブID取得
		m_log.trace("checkReferJob Id=" + item.getData().getId());
		JobInfo jobInfo = item.getData();
		if (jobInfo.getType() == JobConstant.TYPE_REFERJOB) {
			if (jobInfo.getReferJobUnitId() != null
					&& jobInfo.getReferJobId() != null) {
				ret.add(item.getData());
				m_log.trace("JobId =" + jobInfo.getId() +
					", UnitId =" + jobInfo.getReferJobUnitId() +
					", referJobId =" + jobInfo.getReferJobId());
			}
		}
		//子JobTreeItemを取得
		for(JobTreeItem child : item.getChildren()){
			ret.addAll(findReferJob(child));
		}
		return ret;
	}

	/**
	 * ジョブユニット内の参照ジョブネットのみを取得する
	 * @param item
	 * @param list
	 * @return
	 */
	public static ArrayList<JobInfo> findReferJobNet(JobTreeItem item){
		ArrayList<JobInfo> ret = new ArrayList<JobInfo>();
		if(item == null || item.getData() == null){
			return ret;
		}
		//ジョブID取得
		m_log.trace("checkReferJobNet Id=" + item.getData().getId());
		JobInfo jobInfo = item.getData();
		if (jobInfo.getType() == JobConstant.TYPE_REFERJOBNET) {
			if (jobInfo.getReferJobUnitId() != null
					&& jobInfo.getReferJobId() != null) {
				ret.add(item.getData());
				m_log.trace("JobId =" + jobInfo.getId() +
					", UnitId =" + jobInfo.getReferJobUnitId() +
					", referJobId =" + jobInfo.getReferJobId());
			}
		}
		//子JobTreeItemを取得
		for(JobTreeItem child : item.getChildren()){
			ret.addAll(findReferJobNet(child));
		}
		return ret;
	}

	public static void sort(JobTreeItemResponseP1 item) {
		ArrayList<JobTreeItemResponseP1> children = item.getChildren();
		if (children == null || children.size() == 0) {
			return;
		}
		Collections.sort(item.getChildren(), new DataComparatorResponse());
		for (JobTreeItemResponseP1 child : children) {
			sort(child);
		}
	}
	/**
	 * ジョブツリーをソートする
	 * @param item
	 */
	public static void sort(JobTreeItem item) {
		ArrayList<JobTreeItem> children = item.getChildren();
		if (children == null || children.size() == 0) {
			return;
		}
		Collections.sort(item.getChildren(), new DataComparator());
		for (JobTreeItem child : children) {
			sort(child);
		}
	}

	private static class DataComparatorResponse implements java.util.Comparator<JobTreeItemResponseP1>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(JobTreeItemResponseP1 o1, JobTreeItemResponseP1 o2){
			String s1 = o1.getData().getId();
			String s2 = o2.getData().getId();
			m_log.trace("s1=" + s1 + ", s2=" + s2);
			return s1.compareTo(s2);
		}
	}
	
	private static class DataComparator implements java.util.Comparator<JobTreeItem>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(JobTreeItem o1, JobTreeItem o2){
			String s1 = o1.getData().getId();
			String s2 = o2.getData().getId();
			m_log.trace("s1=" + s1 + ", s2=" + s2);
			return s1.compareTo(s2);
		}
	}

	/**
	 * ジョブセッションが走行中か否かをチェックする
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @return ジョブセッションが走行中ならtrueを返す。ジョブセッションが終了しているならfalseを返す
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public static boolean isRunJob(String sessionId, String jobunitId,
			String jobId) throws JobInfoNotFound, InvalidRole {
		// セッションIDとジョブIDから、セッションジョブを取得
		JpaTransactionManager jtm = new JpaTransactionManager();
		try {
			JobSessionJobEntity sessionJobEntity = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
			if (sessionJobEntity.getStatus() == StatusConstant.TYPE_END) {
				return false;
			} else
				return true;
		}
		finally {
			jtm.close();
		}
	}

	/**
	 * JobNotice関連情報を設定する
	 * 
	 * @param jobInfoEntity
	 * @param parentJobId
	 * @return
	 * @throws InvalidRole
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 */
	public static void copyJobNoticeProperties (JobInfoEntity jobInfoEntity, String parentJobId) throws HinemosUnknown {

		String sessionId = jobInfoEntity.getId().getSessionId();
		String jobunitId = jobInfoEntity.getId().getJobunitId();

		try {
			JobInfoEntity parentJobInfoEntity = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, parentJobId).getJobInfoEntity();

			String infoNotifyGroupId = NotifyGroupIdGenerator.generate(jobInfoEntity);
			jobInfoEntity.setNotifyGroupId(infoNotifyGroupId);

			jobInfoEntity.setBeginPriority(parentJobInfoEntity.getBeginPriority());
			jobInfoEntity.setNormalPriority(parentJobInfoEntity.getNormalPriority());
			jobInfoEntity.setWarnPriority(parentJobInfoEntity.getWarnPriority());
			jobInfoEntity.setAbnormalPriority(parentJobInfoEntity.getAbnormalPriority());

			// 取得したマスタ情報の通知グループIDで、通知関連情報を取得する
			List<NotifyRelationInfo> ct = new NotifyControllerBean()
					.getNotifyRelation(NotifyGroupIdGenerator
							.generate(jobInfoEntity));
			// JobNoticeInfo用の通知グループIDで、通知関連テーブルのコピーを作成する
			for (NotifyRelationInfo relation : ct) {
				relation.setNotifyGroupId(infoNotifyGroupId);
				relation.setFunctionPrefix(FunctionPrefixEnum.JOB_SESSION.name());
			}
			// JobからNotifyRelationInfoは１件のみ登録すればよい。
			new NotifyControllerBean().addNotifyRelation(ct);
		} catch (InvalidRole e) {
			throw new HinemosUnknown(e.getMessage());
		} catch (JobInfoNotFound e) {
			throw new HinemosUnknown(e.getMessage());
		}
	}

	/**
	 * ジョブユニットIDに対応したオーナーロールID
	 * 
	 * @param jobunitId ジョブユニットID
	 * @return オーナーロールID
	 */
	public static String createSessioniOwnerRoleId(String jobunitId) {
		String ownerRoleId = "";

		if (CreateJobSession.TOP_JOBUNIT_ID.equals(jobunitId)) {
			ownerRoleId = RoleIdConstant.ALL_USERS;
		} else {
			try {
				JobMstEntity jobMstEntity
				= QueryUtil.getJobMstPK_NONE(new JobMstEntityPK(jobunitId, jobunitId));
				if (jobMstEntity.getOwnerRoleId() == null) {
					ownerRoleId = RoleIdConstant.INTERNAL;
				} else {
					ownerRoleId = jobMstEntity.getOwnerRoleId();
				}
			} catch (JobMasterNotFound e) {
				ownerRoleId = RoleIdConstant.INTERNAL;
			}
		}
		return ownerRoleId;
	}
}