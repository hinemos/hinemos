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
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.bean.FacilityImageConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.Messages;

/**
 * スコープツリー用のラベルプロバイダクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class FacilityTreeLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		FacilityInfoResponse info = ((FacilityTreeItemResponse) element).getData();

		FacilityTypeEnum type = info.getFacilityType();
		if (type == FacilityTypeEnum.COMPOSITE) {
			return info.getFacilityName();
		} else if (type == FacilityTypeEnum.MANAGER) {
			return Messages.getString("facility.manager") + " (" + info.getFacilityId() + ")";
		} else {
			return info.getFacilityName() + " (" + info.getFacilityId() + ")";
		}
	}

	@Override
	public Image getImage(Object element) {
		FacilityInfoResponse facilityInfo = ((FacilityTreeItemResponse) element).getData();
		boolean valid = true;

		FacilityTypeEnum facilityType = facilityInfo.getFacilityType();
		if (facilityType == FacilityTypeEnum.MANAGER) {
			return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_COMPOSITE, valid);
		} else if (facilityType == FacilityTypeEnum.COMPOSITE) {
			return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_COMPOSITE, valid);
		} else if (facilityType == FacilityTypeEnum.SCOPE) {
			if (facilityInfo.getNotReferFlg()) {
				valid = false;
			}
			return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_SCOPE, valid);
		} else if (facilityType == FacilityTypeEnum.NODE) {
			if (!facilityInfo.getValid()) {
				valid = false;
			}
			return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_NODE, valid);
		} else {
			return null;
		}
	}
}
