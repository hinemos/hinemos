/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.viewer;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * FacilityTreeCompositeクラス用のViewerSorterクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class FacilityTreeViewerSorter extends ViewerSorter {

	/**
	 * 比較処理
	 * 
	 * @param viewer
	 * @param e1
	 * @param e2
	 * @return 比較結果。ソート順位・ファシリティIDの値でソートする
	 * @since 1.0.0
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {

		if (e1 instanceof FacilityTreeItem && e2 instanceof FacilityTreeItem) {

			FacilityInfo info1 = ((FacilityTreeItem) e1).getData();
			FacilityInfo info2 = ((FacilityTreeItem) e2).getData();
			int order1 =  info1.getDisplaySortOrder();
			int order2 =  info2.getDisplaySortOrder();

			if(order1 == order2 ){

				String object1 = info1.getFacilityId();
				String object2 = info2.getFacilityId();

				return object1.compareTo(object2);
			}
			else {
				return (order1 - order2);
			}
		}
		return 0;
	}
}
