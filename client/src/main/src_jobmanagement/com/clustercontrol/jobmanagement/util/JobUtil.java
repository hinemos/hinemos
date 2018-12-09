/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobInvalid_Exception;
import com.clustercontrol.ws.jobmanagement.JobNextJobOrderInfo;
import com.clustercontrol.ws.jobmanagement.JobObjectInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.JobWaitRuleInfo;
import com.clustercontrol.ws.jobmanagement.OtherUserGetLock_Exception;
import com.clustercontrol.ws.jobmanagement.UpdateTimeNotLatest_Exception;

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
	private static JobTreeItem copy(JobTreeItem original) {
		JobTreeItem clone = null;
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
	public static JobTreeItem copy(JobTreeItem original, JobTreeItem top, String jobunitId, String ownerRoleId) {
		JobTreeItem clone = copy(original);
		clone.getData().setJobunitId(jobunitId);
		clone.getData().setOwnerRoleId(ownerRoleId);

		//オーナーロールIDをコピー先のものに変更
		clone = changeOwnerRoleId(clone, ownerRoleId);

		//ジョブIDの変更
		HashMap<String, String> jobIdMap = changeJobId(clone, top, clone);

		//待ち条件を変更する
		modifyWaitRule(clone, jobIdMap);
		
		//ジョブユニットのコピー　または　ジョブユニットの異なる参照ジョブのコピーの場合は参照を消す
		if (original.getData().getType() == JobConstant.TYPE_JOBUNIT ||
				!jobunitId.equals(original.getData().getJobunitId())) {
			modifyReferJob(clone);
		}

		//FIXME ジョブユニットIDを変更する

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
	private static JobTreeItem changeOwnerRoleId(JobTreeItem clone, String ownerRoleId){
		//子JobTreeItemを取得
		List<JobTreeItem> childrens = clone.getChildren();
		for (JobTreeItem children : new ArrayList<JobTreeItem>(childrens)) {
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
	private static HashMap<String, String> changeJobId(JobTreeItem item, JobTreeItem top, JobTreeItem clone) {
		if(item == null || top == null)
			return null;

		HashMap<String, String> jobIdMap = new HashMap<String, String>();

		//ジョブIDを変更
		JobInfo info = item.getData();
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
			if (info.getType() == JobConstant.TYPE_JOBUNIT) {
				info.setJobunitId(jobId.toString());
				m_log.trace("changeJobId() setJobunitId = " + jobId.toString());
			}
			//else {
			//	info.setJobunitId(item.getParent().getData().getJobunitId());
			//}
		}

		//子JobTreeItemを取得
		List<JobTreeItem> childrens = item.getChildren();
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
	private static void modifyWaitRule(JobTreeItem item, HashMap<String, String>jobIdMap) {
		if(item == null)
			return;

		JobInfo info = item.getData();
		if(info != null){
			//待ち条件となるジョブがコピー元に存在する場合には、コピー先のジョブIDに修正する
			//存在しない場合には、削除する
			//時刻待ち条件の場合には、そのままコピー先にコピーする
			JobWaitRuleInfo waitRule = info.getWaitRule();
			if(waitRule != null && waitRule.getObject() != null){
				ArrayList<JobObjectInfo> list = new ArrayList<JobObjectInfo>();
				for (JobObjectInfo jobObjectInfo : waitRule.getObject()) {
					String jobId = jobIdMap.get(jobObjectInfo.getJobId());
					if (jobId != null) {
						jobObjectInfo.setJobId(jobId);
						list.add(jobObjectInfo);
					} else if (jobObjectInfo.getType() == JudgmentObjectConstant.TYPE_TIME ||
							jobObjectInfo.getType() == JudgmentObjectConstant.TYPE_START_MINUTE ||
							jobObjectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
						list.add(jobObjectInfo);
					} else if (jobObjectInfo.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS ||
							jobObjectInfo.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE) {
						//セッション横断待ち条件の場合、待ち条件のジョブIDはそのままコピーします
						list.add(jobObjectInfo);
					}
				}
				waitRule.getObject().clear();
				waitRule.getObject().addAll(list);
			}
			//後続ジョブ優先度設定がある場合、合わせてコピーする
			if (waitRule != null) {
				if (waitRule.isExclusiveBranch() != null && waitRule.isExclusiveBranch()) {
					List<JobNextJobOrderInfo> nextJobOrderList = waitRule.getExclusiveBranchNextJobOrderList();
					List<JobNextJobOrderInfo> list = new ArrayList<JobNextJobOrderInfo>();
					for (JobNextJobOrderInfo nextJobOrder: nextJobOrderList) {
						String jobId = jobIdMap.get(nextJobOrder.getJobId());
						String nextJobId = jobIdMap.get(nextJobOrder.getNextJobId());
						if (jobId != null && nextJobId != null) {
							JobNextJobOrderInfo newNextJobOrder = new JobNextJobOrderInfo();
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
		}

		//子JobTreeItemを取得
		List<JobTreeItem> childrens = item.getChildren();
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
	private static void modifyReferJob(JobTreeItem item) {
		JobInfo cloneInfo = item.getData();
		if (cloneInfo.getType() == JobConstant.TYPE_REFERJOB || cloneInfo.getType() == JobConstant.TYPE_REFERJOBNET) {
			cloneInfo.setReferJobId("");
			cloneInfo.setReferJobUnitId("");
		}
		
		//子JobTreeItemを取得
		List<JobTreeItem> childrens = item.getChildren();
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
	public static boolean findJobId(String jobId, JobTreeItem item) {
		boolean find = false;

		//ジョブIDをチェック
		JobInfo info = item.getData();
		if(info != null){
			if(jobId.compareTo(info.getId()) == 0){
				find = true;
				return find;
			}
		}

		//子JobTreeItemを取得
		List<JobTreeItem> childrens = item.getChildren();
		for(int i = 0; i < childrens.size(); i++){
			find = findJobId(jobId, childrens.get(i));
			if(find){
				break;
			}
		}

		return find;
	}

	/**
	 * ジョブツリーアイテム内のジョブIDが一致するインスタンスの有無を返す<BR>
	 *
	 * @param item ジョブツリーアイテム
	 * @param isTop ツリーのトップか否か
	 * @return ジョブIDが一致するジョブ/ネット/ユニットが存在すればtrue、なければfalse。
	 * @throws JobInvalid
	 */
	public static void findDuplicateJobId(JobTreeItem item, boolean isTop) throws JobInvalid{
		m_log.debug("findDuplicateJobId() start : isTop = " + isTop);

		// 自身がtopの場合は何もしない
		if(!isTop){

			// 自身がジョブユニット/ジョブネット/ジョブの場合
			if(item.getData() != null){
				String jobId = item.getData().getId();
				m_log.debug("findDuplicateJobId() jobId = " + jobId);

				List<JobTreeItem> children = item.getChildren();
				for (JobTreeItem child : children) {
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

		List<JobTreeItem> children = item.getChildren();
		for (JobTreeItem child : children) {
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
	public static void findDuplicateJobunitId(JobTreeItem item) throws JobInvalid{
		m_log.debug("findDuplicateJobunitId() start " + JobTreeItemUtil.getPath(item));

		JobTreeItem top = item.getChildren().get(0);
		List<JobTreeItem> jobunits = top.getChildren();

		HashSet<String> set = new HashSet<String>();
		for (JobTreeItem jobunit : jobunits) {
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
	public static JobTreeItem getTopJobTreeItem(JobTreeItem item) {
		if(item == null)
			return null;

		while (item.getParent() != null) {
			if(item.getParent().getData().getType() == JobConstant.TYPE_COMPOSITE){
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
	public static JobTreeItem getTopJobUnitTreeItem(JobTreeItem item) {
		if(item == null)
			return null;

		while (item.getParent() != null) {
			m_log.trace("getTopJobUnitTreeItem() " + item.getParent().getData().getJobunitId() + "." + item.getParent().getData().getId());
			if(item.getParent().getData().getType() == JobConstant.TYPE_JOBUNIT){
				item = item.getParent();
				break;
			}
			else if(item.getData().getType() == JobConstant.TYPE_JOBUNIT){
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
	public static JobTreeItem getJobTreeItem(JobTreeItem jobTreeItem, String jobId) {

		if(jobTreeItem == null 
				|| jobId == null
				|| jobId.equals("")) {
			return null;
		}

		if (jobId.equals(jobTreeItem.getData().getId())) {
			return jobTreeItem;
		}

		// 子TreeItemも検索する
		for (JobTreeItem childJobTreeItem : jobTreeItem.getChildren()) {
			JobTreeItem resultJobTreeItem = getJobTreeItem(childJobTreeItem, jobId);
			if (resultJobTreeItem != null) {
				return resultJobTreeItem;
			}
		}

		return null;
	}

	/**
	 * ジョブツリーアイテムのジョブ待ち条件情報をチェックする
	 *
	 * @param item ジョブ待ち条件情報をチェックするジョブツリーアイテム
	 */
	public static boolean checkWaitRule(JobTreeItem item) throws JobInvalid{
		boolean check = true;

		if(item == null)
			return check;

		if(item.getData() != null){
			//ジョブID取得
			String jobId = item.getData().getId();

			//待ち条件情報を取得する
			JobWaitRuleInfo waitRule = item.getData().getWaitRule();
			if(waitRule != null &&
					waitRule.getObject() != null && waitRule.getObject().size() > 0){

				Iterator<JobObjectInfo> itr = waitRule.getObject().iterator();
				while(itr.hasNext()) {
					//判定対象を取得
					JobObjectInfo objectInfo = itr.next();
					if(objectInfo.getType() != JudgmentObjectConstant.TYPE_TIME){
						//判定対象のジョブIDが同一階層に存在するかチェック
						boolean find = false;
						String targetJobId = objectInfo.getJobId();
						List<JobTreeItem> childrens = item.getParent().getChildren();
						for(int i = 0; i < childrens.size(); i++){
							//ジョブIDをチェック
							JobInfo childInfo = childrens.get(i).getData();
							if(childInfo != null &&
									!jobId.equals(childInfo.getId())){
								if(targetJobId.compareTo(childInfo.getId()) == 0){
									find = true;
									break;
								}
							}
						}
						if(!find){
							String args[] = {jobId, targetJobId};
							throw new JobInvalid(Messages.getString("message.job.59", args));
						}
					}
				}
			}
		}

		//子JobTreeItemを取得
		List<JobTreeItem> childrens = item.getChildren();
		for(int i = 0; i < childrens.size(); i++){
			check = checkWaitRule(childrens.get(i));
			if(!check){
				break;
			}
		}

		return check;
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
	public static Integer getEditLock(String managerName, String jobunitId, Long updateTime, boolean forceFlag) throws OtherUserGetLock_Exception {
		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
			return wrapper.getEditLock(jobunitId, updateTime, forceFlag);
		} catch (OtherUserGetLock_Exception e) {
			throw e;
		} catch (InvalidRole_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (UpdateTimeNotLatest_Exception e) {
			// 保持しているジョブツリーが最新でない
			MessageDialog.openInformation(null, Messages.getString("message"),
					HinemosMessage.replace(e.getMessage()));
		} catch (JobInvalid_Exception e) {
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
	public static void setJobunitIdAll(JobTreeItem item, String jobunitId) {
		if (item == null)
			return;
		
		JobInfo jobInfo = item.getData();
		if (jobInfo == null)
			return;

		if (jobInfo.getType() == JobConstant.TYPE_JOBUNIT) {
			jobunitId = jobInfo.getJobunitId();
		}

		m_log.trace("setJobunitIdAll() : jobId = " + jobInfo.getId() +
				", old jobunitId = " + jobInfo.getJobunitId() +
				", new jobunitId = " + jobunitId);

		// ジョブユニットIDを変更する
		jobInfo.setJobunitId(jobunitId);

		// 参照ジョブの場合は参照先のジョブユニットIDを変更する
		if (jobInfo.getType() == JobConstant.TYPE_REFERJOB || jobInfo.getType() == JobConstant.TYPE_REFERJOBNET) {
			jobInfo.setReferJobUnitId(jobunitId);
		}

		// 子のジョブツリーアイテムを再帰的に呼び出す
		for (JobTreeItem child : item.getChildren()) {
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
	public static void setReferJobId(JobInfo jobInfo, JobTreeItem top) {
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
	public static void getRegisteredJob(JobTreeItem item, List<JobInfo> jobList) {
		
		JobInfo info = item.getData();
		if (info != null) {
			if (info.isRegisteredModule() == true) {
				jobList.add(info);
			}
			
			for (int i = 0; i < item.getChildren().size(); i++) {
				JobTreeItem children = item.getChildren().get(i);
				getRegisteredJob(children, jobList);
			}
		}
		return;
	}
}
