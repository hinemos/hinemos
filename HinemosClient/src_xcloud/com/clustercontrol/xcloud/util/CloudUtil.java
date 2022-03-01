/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.FacilityTreeCache;

public class CloudUtil {
	
	public static Integer jobIdMaxLength = 64;
	
	private CloudUtil() {
	}
	
	public static String getFacilityPath(String managerName, String facilityId) {
		FacilityTreeItemResponse treeItem = FacilityTreeCache.getTreeItem(managerName);
		List<FacilityTreeItemResponse> treeItems = collectScopes(treeItem, facilityId);
		if (!treeItems.isEmpty()) {
			FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
			return path.getPath(treeItems.get(0));
		}
		return facilityId;
	}

	public static List<FacilityTreeItemResponse> collectScopes(String managerName, String...targetIds) {
		return recursiveCollectScopes(FacilityTreeCache.getTreeItem(managerName), new ArrayList<String>(Arrays.asList(targetIds)), new ArrayList<FacilityTreeItemResponse>());
	}
	
	public static List<FacilityTreeItemResponse> collectScopes(FacilityTreeItemResponse treeItem, String...targetIds) {
		return recursiveCollectScopes(treeItem, new ArrayList<String>(Arrays.asList(targetIds)), new ArrayList<FacilityTreeItemResponse>());
	}
	
	private static List<FacilityTreeItemResponse> recursiveCollectScopes(FacilityTreeItemResponse treeItem, List<String> targetIds, List<FacilityTreeItemResponse> buf) {
		if (targetIds.contains(treeItem.getData().getFacilityId()) /*&& treeItem.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE*/) {
			targetIds.remove(treeItem.getData().getFacilityId());
			buf.add(treeItem);
		} else {
			for (FacilityTreeItemResponse fti: treeItem.getChildren()) {
				recursiveCollectScopes(fti, targetIds, buf);
				if (targetIds.isEmpty())
					break;
			}
		}
		return buf;
	}
	
	
	public interface IFacilityTreeVisitor {
		void visitTreeItem(FacilityTreeItemResponse item);
	}
	
	
	public static void walkFacilityTree(FacilityTreeItemResponse treeItem, IFacilityTreeVisitor visitor) {
		visitor.visitTreeItem(treeItem);
		for (FacilityTreeItemResponse child: treeItem.getChildren()) {
			walkFacilityTree(child, visitor);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> emptyList(Class<T> clazz) {
		return (List<T>)Collections.EMPTY_LIST;
	}
}
