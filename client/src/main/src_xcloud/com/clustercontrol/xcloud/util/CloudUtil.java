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
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.ws.repository.FacilityTreeItem;

public class CloudUtil {
	
	public static Integer jobIdMaxLength = 64;
	
	private CloudUtil() {
	}
	
	public static String getFacilityPath(String managerName, String facilityId) {
		FacilityTreeItem treeItem = FacilityTreeCache.getTreeItem(managerName);
		List<FacilityTreeItem> treeItems = collectScopes(treeItem, facilityId);
		if (!treeItems.isEmpty()) {
			FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
			return path.getPath(treeItems.get(0));
		}
		return facilityId;
	}

	public static List<FacilityTreeItem> collectScopes(String managerName, String...targetIds) {
		return recursiveCollectScopes(FacilityTreeCache.getTreeItem(managerName), new ArrayList<String>(Arrays.asList(targetIds)), new ArrayList<FacilityTreeItem>());
	}
	
	public static List<FacilityTreeItem> collectScopes(FacilityTreeItem treeItem, String...targetIds) {
		return recursiveCollectScopes(treeItem, new ArrayList<String>(Arrays.asList(targetIds)), new ArrayList<FacilityTreeItem>());
	}
	
	private static List<FacilityTreeItem> recursiveCollectScopes(FacilityTreeItem treeItem, List<String> targetIds, List<FacilityTreeItem> buf) {
		if (targetIds.contains(treeItem.getData().getFacilityId()) /*&& treeItem.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE*/) {
			targetIds.remove(treeItem.getData().getFacilityId());
			buf.add(treeItem);
		} else {
			for (FacilityTreeItem fti: treeItem.getChildren()) {
				recursiveCollectScopes(fti, targetIds, buf);
				if (targetIds.isEmpty())
					break;
			}
		}
		return buf;
	}
	
	
	public interface IFacilityTreeVisitor {
		void visitTreeItem(FacilityTreeItem item);
	}
	
	
	public static void walkFacilityTree(FacilityTreeItem treeItem, IFacilityTreeVisitor visitor) {
		visitor.visitTreeItem(treeItem);
		for (FacilityTreeItem child: treeItem.getChildren()) {
			walkFacilityTree(child, visitor);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> emptyList(Class<T> clazz) {
		return (List<T>)Collections.EMPTY_LIST;
	}
}
