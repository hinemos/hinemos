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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.clustercontrol.bean.FacilityImageConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * スコープツリー用のラベルプロバイダクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class FacilityTreeLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		FacilityInfo info = ((FacilityTreeItem) element).getData();

		int type = info.getFacilityType();
		if (type == FacilityConstant.TYPE_COMPOSITE) {
			return info.getFacilityName();
		} else if (type == FacilityConstant.TYPE_MANAGER) {
			return Messages.getString("facility.manager") + " (" + info.getFacilityId() + ")";
		} else {
			return info.getFacilityName() + " (" + info.getFacilityId() + ")";
		}
	}

	@Override
	public Image getImage(Object element) {
		FacilityInfo facilityInfo = ((FacilityTreeItem)element).getData();
		boolean valid = true;

		switch (facilityInfo.getFacilityType()) {
		case FacilityConstant.TYPE_MANAGER:
			return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_COMPOSITE, valid);
		case FacilityConstant.TYPE_COMPOSITE:
			return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_COMPOSITE, valid);
		case FacilityConstant.TYPE_SCOPE:
			if (facilityInfo.isNotReferFlg()) {
				valid = false;
			}
			return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_SCOPE, valid);
		case FacilityConstant.TYPE_NODE:
			if (!facilityInfo.isValid()) {
				valid = false;
			}
		return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_NODE, valid);
		default:
			return null;
		}
	}
}
