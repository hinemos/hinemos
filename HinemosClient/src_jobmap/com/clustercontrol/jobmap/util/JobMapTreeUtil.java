/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.swt.widgets.TreeItem;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.viewer.JobTreeViewer;

public class JobMapTreeUtil {

	/**
	 * Session Singleton
	 */
	private static JobMapTreeUtil getInstance(){
		return SingletonUtil.getSessionInstance(JobMapTreeUtil.class);
	}

	/**
	 * ツリーのディープコピーを生成して返します
	 * 
	 * @return ディープコピー
	 */
	public static JobTreeItemWrapper deepCopy(JobTreeItemWrapper item, JobTreeItemWrapper parentItem) {
		synchronized(getInstance()) {
			JobTreeItemWrapper resultItem = new JobTreeItemWrapper();
			if (item.getData() != null) {
				JobInfoWrapper resultData = JobTreeItemUtil.createJobInfoWrapper();
				
				if (item.getData().getAbnormalPriority() != null) {
					resultData.setAbnormalPriority(item.getData().getAbnormalPriority());
				}
				
				resultData.setApprovalReqMailBody(item.getData().getApprovalReqMailBody());
				resultData.setApprovalReqMailTitle(item.getData().getApprovalReqMailTitle());
				resultData.setApprovalReqRoleId(item.getData().getApprovalReqRoleId());
				resultData.setApprovalReqSentence(item.getData().getApprovalReqSentence());
				resultData.setApprovalReqUserId(item.getData().getApprovalReqUserId());
				
				if (item.getData().getBeginPriority() != null) {
					resultData.setBeginPriority(item.getData().getBeginPriority());
				}
				
				// JobCommandInfo skip
				
				if (item.getData().getCreateTime() != null) {
					resultData.setCreateTime(item.getData().getCreateTime());
				}
				
				resultData.setCreateUser(item.getData().getCreateUser());
				resultData.setDescription(item.getData().getDescription());
				
				// endStatus skip
				
				// JobFileInfo skip
				
				resultData.setIconId(item.getData().getIconId());
				
				resultData.setId(item.getData().getId());
				resultData.setJobunitId(item.getData().getJobunitId());
				
				// MonitorJobInfo skip
				
				resultData.setName(item.getData().getName());
				if (item.getData().getNormalPriority() != null) {
					resultData.setNormalPriority(item.getData().getNormalPriority());
				}
				
				// List<NotifyRelationInfo> skip
				
				resultData.setOwnerRoleId(item.getData().getOwnerRoleId());
				
				// List<JobParameterInfo> skip
				
				resultData.setParentId(item.getData().getParentId());
				resultData.setPropertyFull(item.getData().getPropertyFull());
				resultData.setReferJobId(item.getData().getReferJobId());
				
				if (item.getData().getReferJobSelectType() != null) {
					resultData.setReferJobSelectType(item.getData().getReferJobSelectType());
				}

				resultData.setReferJobUnitId(item.getData().getReferJobUnitId());
				resultData.setRegistered(item.getData().getRegistered());
				if (item.getData().getType() != null) {
					resultData.setType(item.getData().getType());
				}
				
				if (item.getData().getUpdateTime() != null) {
					resultData.setUpdateTime(item.getData().getUpdateTime());
				}
				
				resultData.setUpdateUser(item.getData().getUpdateUser());
				resultData.setIsUseApprovalReqSentence(item.getData().getIsUseApprovalReqSentence());
				
				// JobWaitRuleInfo skip
				
				if (item.getData().getWarnPriority() != null) {
					resultData.setWarnPriority(item.getData().getWarnPriority());
				}
				resultData.setExpNodeRuntimeFlg(item.getData().getExpNodeRuntimeFlg());
				resultItem.setData(resultData);
			}
	
			if (item.getChildren() != null) {
				List<JobTreeItemWrapper> resultChildren = new ArrayList<JobTreeItemWrapper>();
				for(JobTreeItemWrapper child : item.getChildren()) {
					if (child != null) {
						JobTreeItemWrapper resultChild = deepCopy(child, resultItem);
						resultChildren.add(resultChild);
					} else {
						resultChildren.add(null);
					}
				}
				resultItem.getChildren().clear();
				resultItem.getChildren().addAll(resultChildren);
			} else {
				resultItem.getChildren().clear();
			}
			resultItem.setParent(parentItem);
			return resultItem;
		}
	}

	/**
	 * 引数で指定されたツリー情報から、引数で指定されたマネージャ名・ジョブユニットID、ジョブIDと
	 * 一致するJobTreeItemを返します。
	 * 
	 * @param treeViewer 検索対象ツリー
	 * @param managerName 検索するマネージャ名
	 * @param jobunitId 検索するジョブユニットID
	 * @param jobId 検索するジョブID
	 * @return
	 */
	public static JobTreeItemWrapper getTargetJobTreeItem(JobTreeViewer treeViewer, String managerName, String jobunitId, String jobId) {
		TreeItem items[] = treeViewer.getTree().getItems();
		for (int i = 0; i < items.length; i++) {
			JobTreeItemWrapper jobItem = (JobTreeItemWrapper)items[i].getData();
			JobTreeItemWrapper ret = searchTreeItem(jobItem.getChildren(), managerName, jobunitId, jobId);
			if (ret != null) {
				return ret;
			}
		}
		return null;
	}
	private static JobTreeItemWrapper searchTreeItem(List<JobTreeItemWrapper> jobTreeList, String managerName, String jobunitId, String jobId) {
		Iterator<JobTreeItemWrapper> it = jobTreeList.iterator();
		while (it.hasNext()) {
			JobTreeItemWrapper jobItem = it.next();
			JobTreeItemWrapper ret = searchTreeItem(jobItem.getChildren(), managerName, jobunitId, jobId);
			if (ret != null) {
				return ret;
			}
			if (managerName.equals(JobTreeItemUtil.getManagerName(jobItem)) 
					&& jobunitId.equals(jobItem.getData().getJobunitId()) 
					&& jobId.equals(jobItem.getData().getId())) {
				return jobItem;
			}
		}
		return null;
	}

}
