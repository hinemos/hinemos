/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
