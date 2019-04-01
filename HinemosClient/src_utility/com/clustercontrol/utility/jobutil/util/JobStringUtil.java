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

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.repository.FacilityTreeItem;

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
	
	public static Map<String, String> getScopeMap() {
		Map<String, String> map = new HashMap<>();
		FacilityTreeItem tree = FacilityTreeCache.getTreeItem(UtilityManagerUtil.getCurrentManagerName());
		FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());

		for (FacilityTreeItem item : tree.getChildren()) {
			map.put(item.getData().getFacilityId(), path.getPath(item));
			childScopeMap(map, path, item);
		}
		return map;
	}
	
	private static void childScopeMap(Map<String, String> map, FacilityPath path, FacilityTreeItem treeItem) {
		for (FacilityTreeItem item : treeItem.getChildren()) {
			map.put(item.getData().getFacilityId(), path.getPath(item));
			childScopeMap(map, path, item);
		}
	}
}
