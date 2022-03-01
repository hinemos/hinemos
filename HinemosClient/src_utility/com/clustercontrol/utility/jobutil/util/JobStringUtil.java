/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.jobutil.util;

import java.util.HashMap;
import java.util.Map;

import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.util.UtilityManagerUtil;

public class JobStringUtil {
	public static final int ERROR_PRE_CHECK = -10;
	
	public enum ItemType {
		Manager(JobConstant.TYPE_MANAGER, Messages.getString("facility.manager")),
		JobUnit(JobConstant.TYPE_JOBUNIT, Messages.getString("jobunit")),
		JobNet(JobConstant.TYPE_JOBNET, Messages.getString("jobnet")),
		Job(JobConstant.TYPE_JOB, Messages.getString("job"));
		
		private Integer itemType;
		private String itemTypeString;
		ItemType(Integer jobType, String jobTypeString) {
			this.itemType = jobType;
			this.itemTypeString = jobTypeString;
		}
	}
	
	public static String toJobTypeString(Integer jobType) {
		for (ItemType type : ItemType.values()) {
			if (type.itemType.equals(jobType))
				return type.itemTypeString;
		}
		return String.valueOf(jobType);
	}

	public enum ItemTypeForEnum {
		Manager(JobInfoWrapper.TypeEnum.MANAGER, Messages.getString("facility.manager")),
		JobUnit(JobInfoWrapper.TypeEnum.JOBUNIT, Messages.getString("jobunit")),
		JobNet(JobInfoWrapper.TypeEnum.JOBNET, Messages.getString("jobnet")),
		Job(JobInfoWrapper.TypeEnum.JOB, Messages.getString("job"));
		
		private JobInfoWrapper.TypeEnum itemType;
		private String itemTypeString;
		ItemTypeForEnum(JobInfoWrapper.TypeEnum jobType, String jobTypeString) {
			this.itemType = jobType;
			this.itemTypeString = jobTypeString;
		}
	}
	

	public static String toJobTypeStringForEnum(JobInfoWrapper.TypeEnum jobTypeEnum) {
		for (ItemTypeForEnum type : ItemTypeForEnum.values()) {
			if (type.itemType.equals(jobTypeEnum))
				return type.itemTypeString;
		}
		return String.valueOf(jobTypeEnum);
	}

	public static Map<String, String> getScopeMap() {
		Map<String, String> map = new HashMap<>();
		FacilityTreeItemResponse tree = FacilityTreeCache.getTreeItem(UtilityManagerUtil.getCurrentManagerName());
		FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());

		for (FacilityTreeItemResponse item : tree.getChildren()) {
			map.put(item.getData().getFacilityId(), path.getPath(item));
			childScopeMap(map, path, item);
		}
		return map;
	}
	
	private static void childScopeMap(Map<String, String> map, FacilityPath path, FacilityTreeItemResponse treeItem) {
		for (FacilityTreeItemResponse item : treeItem.getChildren()) {
			map.put(item.getData().getFacilityId(), path.getPath(item));
			childScopeMap(map, path, item);
		}
	}
}
