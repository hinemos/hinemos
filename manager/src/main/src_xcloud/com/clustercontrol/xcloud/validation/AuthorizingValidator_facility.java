/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation;

import java.lang.reflect.Method;
import java.util.Locale;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;
import com.clustercontrol.xcloud.validation.MethodValidator.MethodValidationContext;

public class AuthorizingValidator_facility implements CustomMethodValidator {
	@Override
	public void validate(Method method, ParamHolder params, String group, MethodValidationContext context) throws PluginException {
		String facilityId = params.getParam("XCLOUD_CORE_FACILITY_ID", String.class);

		FacilityTreeItem treeItem;
		try {
			treeItem = RepositoryControllerBeanWrapper.bean().getFacilityTree(null, Locale.getDefault());
		}
		catch (HinemosUnknown e1) {
			throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e1);
		}

		if (serarchFacility(treeItem, facilityId) == null)
			throw ErrorCode.FACILITY_NOT_FOUND.cloudManagerFault(facilityId);
	}

	private FacilityTreeItem serarchFacility(FacilityTreeItem treeItem, String facilityId) {
		if (!treeItem.getData().isNotReferFlg() && treeItem.getData().getFacilityId().equals(facilityId)) return treeItem;
		
		for (FacilityTreeItem child: treeItem.getChildrenArray()) {
			FacilityTreeItem result = serarchFacility(child, facilityId);
			if (result != null)
				return result;
		}
		return null;
	}
}
