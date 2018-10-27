/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
