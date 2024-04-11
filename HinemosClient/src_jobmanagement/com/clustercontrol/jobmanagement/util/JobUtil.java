/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.EditLockResponse;
import org.openapitools.client.model.GetEditLockRequest;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import org.openapitools.client.model.JobNextJobOrderInfoResponse;
import org.openapitools.client.model.JobObjectGroupInfoResponse;
import org.openapitools.client.model.JobObjectInfoResponse;
import org.openapitools.client.model.JobWaitRuleInfoResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.fault.OtherUserGetLock;
import com.clustercontrol.fault.UpdateTimeNotLatest;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;

/**
 * ジョブユーティリティクラス
 *
 * 以下を提供します。<BR>
 * <li>ジョブツリーアイテムに関するユーティリティ
 * <li>ログインユーザが参照可能なジョブユニットかどうかをチェックするユーティリティ
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class JobUtil {
	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( JobUtil.class );

	/** コピーしたジョブIDの接頭語 */
	private static final String COPY_OF = "Copy_Of_";

	/** 生成する参照ジョブIDの接頭語(ジョブマップのドラッグで使用) */
	private static final String REFER_TO = "Refer_To_";

	/**
	 * 引数で指定されたジョブツリーアイテムのコピーを作成する
	 *
	 * @param original コピー元ジョブツリーアイテム
	 * @return コピーとして作成されたジョブツリーアイテム
	 */
	private static JobTreeItemWrapper copy(JobTreeItemWrapper original) {
		JobTreeItemWrapper clone = null;
		if(original != null){
			clone = JobTreeItemUtil.clone(original, null);
			// 新規登録の判定のため、createTimeをクリアする
			clone.getData().setCreateTime(null);
		}

		return clone;
	}

	/**
	 * @param original コピー元ジョブツリーアイテム
	 * @param top コピー元ジョブツリーアイテム
	 * @return コピーとして作成されたジョブツリーアイテム
	 */
	public static JobTreeItemWrapper copy(JobTreeItemWrapper original, JobTreeItemWrapper top, String jobunitId, String ownerRoleId) {
		JobTreeItemWrapper clone = copy(original);
		clone.getData().setJobunitId(jobunitId);
		clone.getData().setOwnerRoleId(ownerRoleId);

		//オーナーロールIDをコピー先のものに変更
		clone = changeOwnerRoleId(clone, ownerRoleId);

		//ジョブIDの変更
		HashMap<String, String> jobIdMap = changeJobId(clone, top, clone);

		//待ち条件を変更する
		modifyWaitRule(clone, jobIdMap);
		
		//ジョブユニットのコピー　または　ジョブユニットの異なる参照ジョブのコピーの場合は参照を消す
		if (original.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
				!jobunitId.equals(original.getData().getJobunitId())) {
			modifyReferJob(clone);
		}

		return clone;
	}

	/**
	 * ジョブIDを元に新たなジョブIDを作成する
	 * 
	 * キーワードが"COPY_OF_"の場合
	 * 例1：COPY_OF_(13)_job0001をjob0001に変換させる。
	 * 例2：COPY_OF_job0001をjob0001に変換させる。
	 * 
	 * @param jobId 参照元のジョブID
	 * @param keyword キーワード
	 * @return
	 */
	private static String getBaseJobId(String jobId, String keyword) {
		// COPY_OF_(13)_job0001 にマッチさせる。
		Pattern p_number = Pattern.compile(keyword + "\\[1-9][0-9]*\\_");
		Matcher m_number = p_number.matcher(jobId);
		if (m_number.find()) {
			return jobId.substring(m_number.end());
		}

		// COPY_OF_job0001 にマッチさせる。
		Pattern p_org = Pattern.compile(keyword);
		Matcher m_org = p_org.matcher(jobId);
		if (m_org.find()) {
			return jobId.substring(m_org.end());
		}

		// マッチしなかったら、そのまま返す。
		return jobId;
	}

	/**
	 * オーナーロールIDを一括変更する<BR>
	 * ジョブツリーアイテムのツリー階層の全てに対して一括変更する。<BR>
	 * @param clone
	 * @param ownerRoleId
	 * @return
	 */
	private static JobTreeItemWrapper changeOwnerRoleId(JobTreeItemWrapper clone, String ownerRoleId){
		//子JobTreeItemを取得
		List<JobTreeItemWrapper> childrens = clone.getChildren();
		for (JobTreeItemWrapper children : new ArrayList<JobTreeItemWrapper>(childrens)) {
			children.getData().setOwnerRoleId(ownerRoleId);
			children = changeOwnerRoleId(children,ownerRoleId);
		}
		return clone;
	}

	/**
	 * ジョブIDを一括変更する<BR>
	 * ジョブツリーアイテムのツリー階層の全てに対して一括変更する。<BR>
	 * topとcloneに指定されたジョブツリーアイテムを、ジョブIDの重複チェック対象とする。<BR>
	 * ジョブIDが重複した場合、コピーしたジョブIDの接頭語とカウンタを付加してジョブIDを決定する。
	 *
	 * @param item ジョブID変更対象のジョブツリーアイテム
	 * @param top ジョブIDの重複チェック対象のジョブツリーアイテム
	 * @param clone ジョブIDの重複チェック対象のジョブツリーアイテム
	 */
	private static HashMap<String, String> changeJobId(JobTreeItemWrapper item, JobTreeItemWrapper top, JobTreeItemWrapper clone) {
		if(item == null || top == null)
			return null;

		HashMap<String, String> jobIdMap = new HashMap<String, String>();

		//ジョブIDを変更
		JobInfoWrapper info = item.getData();
		if(info != null){
			int count = 0;
			String jobId = "";
			jobId = info.getId();
			String baseJobId = getBaseJobId(jobId, COPY_OF);

			while(true){
				if(!findJobId(jobId, top) && !findJobId(jobId, clone)){
					break;
				}
				if(count == 0){
					jobId = COPY_OF;
				} else {
					jobId = COPY_OF + count + "_";
				}
				jobId += baseJobId;
				count++;

				// 最大で999とする。
				if (count == 1000) {
					break;
				}
			}
			//コピー前後のジョブIDの対応を記録
			//key:コピー前のジョブID、value:コピー後のジョブID
			jobIdMap.put(info.getId(), jobId);

			info.setId(jobId);

			// ジョブユニットの場合のみジョブユニットIDを上書き
			if (info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
				info.setJobunitId(jobId.toString());
				m_log.trace("changeJobId() setJobunitId = " + jobId.toString());
			}
			//else {
			//	info.setJobunitId(item.getParent().getData().getJobunitId());
			//}
		}

		//子JobTreeItemを取得
		List<JobTreeItemWrapper> childrens = item.getChildren();
		for(int i = 0; i < childrens.size(); i++){
			childrens.get(i).getData().setJobunitId(info.getJobunitId());
			m_log.trace("changeJobId() set childrens[i] " + info.getJobunitId());
			jobIdMap.putAll(changeJobId(childrens.get(i), top, clone));
		}

		return jobIdMap;
	}


	/**
	 * ジョブツリーアイテムのジョブ待ち条件情報を修正する<BR>
	 * ジョブツリーアイテムのツリー階層の全てが修正対象
	 *
	 * @param item ジョブ待ち条件情報を修正するジョブツリーアイテム
	 */
	private static void modifyWaitRule(JobTreeItemWrapper item, HashMap<String, String>jobIdMap) {
		if(item == null)
			return;

		JobInfoWrapper info = item.getData();
		if(info != null){
			//待ち条件となるジョブがコピー元に存在する場合には、コピー先のジョブIDに修正する
			//存在しない場合には、削除する
			//時刻待ち条件の場合には、そのままコピー先にコピーする
			JobWaitRuleInfoResponse waitRule = info.getWaitRule();
			if(waitRule != null && waitRule.getObjectGroup() != null){
				ArrayList<JobObjectGroupInfoResponse> groupList = new ArrayList<>();
				for (JobObjectGroupInfoResponse jobObjectGroupInfo : waitRule.getObjectGroup()) {
					if(jobObjectGroupInfo == null || jobObjectGroupInfo.getJobObjectList() == null){
						continue;
					}
					boolean isDelete = false;
					ArrayList<JobObjectInfoResponse> list = new ArrayList<JobObjectInfoResponse>();
					for (JobObjectInfoResponse jobObjectInfo : jobObjectGroupInfo.getJobObjectList()) {
						if (jobObjectInfo.getType() == JobObjectInfoResponse.TypeEnum.JOB_END_STATUS ||
							jobObjectInfo.getType() == JobObjectInfoResponse.TypeEnum.JOB_END_VALUE ||
							jobObjectInfo.getType() == JobObjectInfoResponse.TypeEnum.CROSS_SESSION_JOB_END_STATUS ||
							jobObjectInfo.getType() == JobObjectInfoResponse.TypeEnum.CROSS_SESSION_JOB_END_VALUE ||
							jobObjectInfo.getType() == JobObjectInfoResponse.TypeEnum.JOB_RETURN_VALUE) {
							String jobId = jobIdMap.get(jobObjectInfo.getJobId());
							if (jobId == null) {
								isDelete = true;
								break;
							}
							jobObjectInfo.setJobId(jobId);
						}
						list.add(jobObjectInfo);
					}
					if (!isDelete) {
						jobObjectGroupInfo.setJobObjectList(list);
						groupList.add(jobObjectGroupInfo);
					}
				}
				waitRule.getObjectGroup().clear();
				waitRule.getObjectGroup().addAll(groupList);
			}
			//後続ジョブ優先度設定がある場合、合わせてコピーする
			if (waitRule != null) {
				if (waitRule.getExclusiveBranch() != null && waitRule.getExclusiveBranch()) {
					List<JobNextJobOrderInfoResponse> nextJobOrderList = waitRule.getExclusiveBranchNextJobOrderList();
					List<JobNextJobOrderInfoResponse> list = new ArrayList<JobNextJobOrderInfoResponse>();
					for (JobNextJobOrderInfoResponse nextJobOrder: nextJobOrderList) {
						String jobId = jobIdMap.get(nextJobOrder.getJobId());
						String nextJobId = jobIdMap.get(nextJobOrder.getNextJobId());
						if (jobId != null && nextJobId != null) {
							JobNextJobOrderInfoResponse newNextJobOrder = new JobNextJobOrderInfoResponse();
							newNextJobOrder.setJobunitId(item.getData().getJobunitId());
							newNextJobOrder.setJobId(jobId);
							newNextJobOrder.setNextJobId(nextJobId);
							list.add(newNextJobOrder);
						}
					}
					waitRule.getExclusiveBranchNextJobOrderList().clear();
					waitRule.getExclusiveBranchNextJobOrderList().addAll(list);
				}
			}
			
			// ジョブキューの設定はクリアする
			if (waitRule != null) {
				waitRule.setQueueFlg(false);
				waitRule.setQueueId(null);
			}
		}

		//子JobTreeItemを取得
		List<JobTreeItemWrapper> childrens = item.getChildren();
		for(int i = 0; i < childrens.size(); i++){
			modifyWaitRule(childrens.get(i), jobIdMap);
		}
	}

	/**
	 * ジョブツリーアイテムの参照ジョブの参照先を修正する<BR>
	 * ジョブツリーアイテムのツリー階層の全ての参照ジョブが修正対象
	 *
	 * @param item ジョブ待ち条件情報を修正するジョブツリーアイテム
	 */
	private static void modifyReferJob(JobTreeItemWrapper item) {
		JobInfoWrapper cloneInfo = item.getData();
		if (cloneInfo.getType() == JobInfoWrapper.TypeEnum.REFERJOB || cloneInfo.getType() == JobInfoWrapper.TypeEnum.REFERJOBNET) {
			cloneInfo.setReferJobId("");
			cloneInfo.setReferJobUnitId("");
		}
		
		//子JobTreeItemを取得
		List<JobTreeItemWrapper> childrens = item.getChildren();
		for (int i = 0; i < childrens.size(); i++) {
			modifyReferJob(childrens.get(i));
		}
	}
	
	/**
	 * ジョブツリーアイテムからジョブIDが一致するインスタンスの有無を返す<BR>
	 * ジョブツリーアイテムのツリー階層の全てが検索対象
	 *
	 * @param jobId ジョブID
	 * @param item ジョブツリーアイテム
	 * @return ジョブIDが一致するジョブツリーアイテムがあればtrue、なければfalse。
	 */
	public static boolean findJobId(String jobId, JobTreeItemWrapper item) {
		boolean find = false;

		//ジョブIDをチェック
		JobInfoWrapper info = item.getData();
		if(info != null){
			if(jobId.compareTo(info.getId()) == 0){
				find = true;
				return find;
			}
		}

		//子JobTreeItemを取得
		List<JobTreeItemWrapper> childrens = item.getChildren();
		for(int i = 0; i < childrens.size(); i++){
			find = findJobId(jobId, childrens.get(i));
			if(find){
				break;
			}
		}

		return find;
	}

	/**
	 * ジョブツリーアイテムからジョブユニットIDが一致するインスタンスの有無を返す<BR>
	 * ジョブユニットIDがnullもしくは空文字の場合はfalseにする
	 *
	 * @param jobunitId ジョブユニットID
	 * @param item ジョブツリーアイテム(マネージャ)
	 * @return ジョブユニットIDが一致するジョブユニットが存在すればtrue、なければfalse
	 */
	public static boolean findJobunitId(String jobunitId, JobTreeItemWrapper managerItem) {
		boolean find = false;

		if (jobunitId == null || jobunitId.isEmpty()) {
			return find;
		}

		List<JobTreeItemWrapper> jobunits = managerItem.getChildren();

		for (JobTreeItemWrapper jobunit : jobunits) {
			if (jobunitId.equals(jobunit.getData().getJobunitId())) {
				find = true;
				break;
			}
		}
		return find;
	}

	/**
	 * インポート先のジョブツリーアイテムと重複するジョブIDを検索する<BR>
	 *
	 * @param sourceItem インポート元のジョブツリーアイテム
	 * @param destItem インポート先のジョブツリーアイテム
	 * @return 重複しているジョブID
	 */
	public static HashSet<String> findDuplicateJobId(
			JobTreeItemWrapper sourceItem, JobTreeItemWrapper destItem) {
		JobTreeItemWrapper destTopItem = null;
		if (destItem.getData().getType().equals(JobInfoWrapper.TypeEnum.JOBUNIT)) {
			destTopItem = destItem;
		} else {
			destTopItem = getTopJobUnitTreeItem(destItem);
		}
		return findDuplicateJobId(sourceItem, destTopItem, new HashSet<String>());
	}

	/**
	 * インポート先のジョブツリーアイテムと重複するジョブIDを検索する<BR>
	 *
	 * @param sourceItem インポート元のジョブツリーアイテム
	 * @param destItem インポート先のジョブツリーアイテム
	 * @return 重複しているジョブID
	 */
	private static HashSet<String> findDuplicateJobId(
			JobTreeItemWrapper sourceItem, JobTreeItemWrapper destItem, HashSet<String> duplicateJobIds) {

		if(sourceItem.getData() != null){
			String jobId = sourceItem.getData().getId();
			if(findJobId(jobId, destItem)){
				if (duplicateJobIds == null) {
					duplicateJobIds = new HashSet<>();
				}
				duplicateJobIds.add(jobId);
			}
			for (JobTreeItemWrapper child : sourceItem.getChildren()) {
				duplicateJobIds = findDuplicateJobId(child, destItem, duplicateJobIds);
			}
		}
		return duplicateJobIds;
	}

	/**
	 * ジョブツリーアイテム内で重複するジョブIDを検索する<BR>
	 *
	 * @param item ジョブツリーアイテム
	 * @return 重複しているジョブID
	 */
	public static HashSet<String> findDuplicateJobId(JobTreeItemWrapper item) {
		return findDuplicateJobId(item, new HashSet<String>(), new HashSet<String>());
	}

	/**
	 * ジョブツリーアイテム内で重複するジョブIDを検索する<BR>
	 *
	 * @param item ジョブツリーアイテム
	 * @param jobIds ジョブツリーアイテム内のジョブID
	 * @return 重複しているジョブID
	 */
	private static HashSet<String> findDuplicateJobId(
			JobTreeItemWrapper item, HashSet<String> jobIds, HashSet<String> duplicateJobIds) {

		if(item.getData() != null){
			String jobId = item.getData().getId();

			if(jobIds != null && jobIds.contains(jobId)){
				if (duplicateJobIds == null) {
					duplicateJobIds = new HashSet<>();
				}
				duplicateJobIds.add(jobId);
			} else {
				if (jobIds == null) {
					jobIds = new HashSet<String>();
				}
				jobIds.add(jobId);
				for (JobTreeItemWrapper child : item.getChildren()) {
					duplicateJobIds = findDuplicateJobId(child, jobIds, duplicateJobIds);
				}
			}
		}
		return duplicateJobIds;
	}

	/**
	 * ジョブツリーアイテム内のジョブIDが一致するインスタンスの有無を返す<BR>
	 *
	 * @param item ジョブツリーアイテム
	 * @param isTop ツリーのトップか否か
	 * @return ジョブIDが一致するジョブ/ネット/ユニットが存在すればtrue、なければfalse。
	 * @throws JobInvalid
	 */
	public static void findDuplicateJobId(JobTreeItemWrapper item, boolean isTop) throws JobInvalid{
		m_log.debug("findDuplicateJobId() start : isTop = " + isTop);

		// 自身がtopの場合は何もしない
		if(!isTop){

			// 自身がジョブユニット/ジョブネット/ジョブの場合
			if(item.getData() != null){
				String jobId = item.getData().getId();
				m_log.debug("findDuplicateJobId() jobId = " + jobId);

				List<JobTreeItemWrapper> children = item.getChildren();
				for (JobTreeItemWrapper child : children) {
					m_log.debug("findDuplicateJobId() child = " + child.getData().getId());

					// 配下のツリーにトップのジョブIDが含まれるか？
					if(findJobId(jobId, child)){

						// jobunitid内にjobidが重複している
						m_log.debug("findDuplicateJobId() jobId is in child " + JobTreeItemUtil.getPath(child));
						Object[] args = {jobId, child.getData().getJobunitId()};
						throw new JobInvalid(Messages.getString("message.job.65",args));
					} else {
						m_log.debug("findDuplicateJobId() jobId is not in child " + JobTreeItemUtil.getPath(child));
					}
				}
			}
		}

		List<JobTreeItemWrapper> children = item.getChildren();
		for (JobTreeItemWrapper child : children) {
			m_log.debug("findDuplicateJobId() call child " + child.getData().getId());
			findDuplicateJobId(child, false);
		}

		m_log.debug("findDuplicateJobId() success!!");
	}

	/**
	 * ジョブツリーアイテム内のジョブユニットIDが一致するインスタンスの有無を返す<BR>
	 *
	 * @param item ジョブツリーアイテム
	 * @return ジョブユニットIDが一致するユニットが存在すればtrue、なければfalse。
	 * @throws JobInvalid
	 */
	public static void findDuplicateJobunitId(JobTreeItemWrapper item) throws JobInvalid{
		m_log.debug("findDuplicateJobunitId() start " + JobTreeItemUtil.getPath(item));

		JobTreeItemWrapper top = item.getChildren().get(0);
		List<JobTreeItemWrapper> jobunits = top.getChildren();

		HashSet<String> set = new HashSet<String>();
		for (JobTreeItemWrapper jobunit : jobunits) {
			// ジョブユニットID
			String jobunitId = jobunit.getData().getJobunitId();
			m_log.debug("findDuplicateJobunitId() jobunitId = " + jobunitId);

			if(set.contains(jobunitId)){
				m_log.debug("findDuplicateJobunitId() hit " + jobunitId);
				Object[] args = {jobunitId};
				throw new JobInvalid(Messages.getString("message.job.64",args));
			} else {
				m_log.debug("findDuplicateJobunitId() add " + jobunitId + " to set");
				set.add(jobunitId);
			}
		}

		m_log.debug("findDuplicateJobunitId() success!!");
	}

	/**
	 * ジョブツリーアイテムの最上位のインスタンスを取得する
	 *
	 * @param item ジョブツリーアイテム
	 * @return 最上位のジョブツリーアイテム
	 */
	public static JobTreeItemWrapper getTopJobTreeItem(JobTreeItemWrapper item) {
		if(item == null)
			return null;

		while (item.getParent() != null) {
			if(item.getParent().getData().getType() == JobInfoWrapper.TypeEnum.COMPOSITE){
				item = item.getParent();
				break;
			}
			else{
				item = item.getParent();
			}
		}

		return item;
	}

	/**
	 * ジョブツリーアイテムの付属するジョブユニットのインスタンスを取得する
	 *
	 * @param item ジョブツリーアイテム
	 * @return 付属するジョブユニットのジョブツリーアイテム
	 */
	public static JobTreeItemWrapper getTopJobUnitTreeItem(JobTreeItemWrapper item) {
		if(item == null)
			return null;

		while (item.getParent() != null) {
			m_log.trace("getTopJobUnitTreeItem() " + item.getParent().getData().getJobunitId() + "." + item.getParent().getData().getId());
			if(item.getParent().getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT){
				item = item.getParent();
				break;
			}
			else if(item.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT){
				break;
			}
			else{
				item = item.getParent();
			}
		}

		return item;
	}

	/**
	 * ジョブツリーアイテムから指定されたジョブIDのジョブ情報を取得する
	 *
	 * @param item ジョブツリーアイテム
	 * @param jobId ジョブID
	 * @return ジョブIDに一致するジョブ情報（JobTreeItem）
	 */
	public static JobTreeItemWrapper getJobTreeItem(JobTreeItemWrapper jobTreeItem, String jobId) {

		if(jobTreeItem == null 
				|| jobId == null
				|| jobId.equals("")) {
			return null;
		}

		if (jobId.equals(jobTreeItem.getData().getId())) {
			return jobTreeItem;
		}

		// 子TreeItemも検索する
		for (JobTreeItemWrapper childJobTreeItem : jobTreeItem.getChildren()) {
			JobTreeItemWrapper resultJobTreeItem = getJobTreeItem(childJobTreeItem, jobId);
			if (resultJobTreeItem != null) {
				return resultJobTreeItem;
			}
		}

		return null;
	}

	/**
	 * 編集ロックを取得します。
	 * @param managerName
	 * @param jobunitId
	 * @param updateTime
	 * @param forceFlag
	 * @return
	 * @throws OtherUserGetLock_Exception
	 */
	public static Integer getEditLock(String managerName, String jobunitId, Long updateTime, boolean forceFlag) throws OtherUserGetLock {
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			GetEditLockRequest req = new GetEditLockRequest(); 
			req.setForceFlag(forceFlag);
			if(updateTime != null){
				req.setUpdateTime(TimezoneUtil.getSimpleDateFormat().format(new Date( updateTime)));
			}
			EditLockResponse ret = wrapper.getEditLock(jobunitId, req);
			return ret.getEditSession();
		} catch (OtherUserGetLock e) {
			throw e;
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (UpdateTimeNotLatest e) {
			// 保持しているジョブツリーが最新でない
			MessageDialog.openInformation(null, Messages.getString("message"),
					HinemosMessage.replace(e.getMessage()));
		} catch (JobInvalid e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("run(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return null;
	}

	/**
	 * ジョブユニットIDをすべて更新する。<BR>
	 *
	 * @param item ジョブツリーアイテム
	 * @param jobunitID ジョブユニットID
	 */
	public static void setJobunitIdAll(JobTreeItemWrapper item, String jobunitId) {
		if (item == null)
			return;
		
		JobInfoWrapper jobInfo = item.getData();
		if (jobInfo == null)
			return;

		if (jobInfo.getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
			jobunitId = jobInfo.getJobunitId();
		}

		m_log.trace("setJobunitIdAll() : jobId = " + jobInfo.getId() +
				", old jobunitId = " + jobInfo.getJobunitId() +
				", new jobunitId = " + jobunitId);

		// ジョブユニットIDを変更する
		jobInfo.setJobunitId(jobunitId);

		// 参照ジョブの場合は参照先のジョブユニットIDを変更する
		if (jobInfo.getType() == JobInfoWrapper.TypeEnum.REFERJOB || jobInfo.getType() == JobInfoWrapper.TypeEnum.REFERJOBNET) {
			jobInfo.setReferJobUnitId(jobunitId);
		}

		// 子のジョブツリーアイテムを再帰的に呼び出す
		for (JobTreeItemWrapper child : item.getChildren()) {
			setJobunitIdAll(child, jobunitId);
		}
	}

	/**
	 * ジョブIDを作成する<BR>
	 * ジョブIDが重複した場合、コピーしたジョブIDの接頭語とカウンタを付加してジョブIDを決定する。
	 * ジョブネットで参照ジョブネット、参照ジョブをドラッグする際に使用する。
	 *
	 * @param jobInfo ジョブID変更対象のジョブ
	 * @param top ジョブIDの重複チェック対象のジョブツリーアイテム
	 */
	public static void setReferJobId(JobInfoWrapper jobInfo, JobTreeItemWrapper top) {
		if(jobInfo == null || top == null)
			return;

		//ジョブIDを変更
		int count = 0;
		String jobId = "";
		jobId = jobInfo.getId();
		String baseJobId = getBaseJobId(jobId, REFER_TO);

		while(true){
			if(!JobUtil.findJobId(jobId, top)){
				break;
			}
			if(count == 0){
				jobId = REFER_TO;
			} else {
				jobId = REFER_TO + count + "_";
			}
			jobId += baseJobId;
			count++;

			// 最大で999とする。
			if (count == 1000) {
				break;
			}
		}

		jobInfo.setId(jobId);

		return;
	}
	
	/**
	 * モジュール登録済みのジョブ情報を取得する
	 *
	 * @param item テーブルツリーアイテム
	 * @param jobList ジョブ情報一覧
	 */
	public static void getRegisteredJob(JobTreeItemWrapper item, List<JobInfoWrapper> jobList) {
		
		JobInfoWrapper info = item.getData();
		if (info != null) {
			if (info.getRegistered() == true) {
				jobList.add(info);
			}
			
			for (int i = 0; i < item.getChildren().size(); i++) {
				JobTreeItemWrapper children = item.getChildren().get(i);
				getRegisteredJob(children, jobList);
			}
		}
		return;
	}
}
