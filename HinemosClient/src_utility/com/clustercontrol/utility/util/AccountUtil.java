/*
 * Copyright (c) 2020 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;


import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.util.AccessRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;

import java.util.HashSet;
import java.util.Set;

import org.openapitools.client.model.RoleInfoResponseP1;
import org.openapitools.client.model.UserInfoResponseP3;

public class AccountUtil {

	/**
	 * ログインユーザがASMINISTRATORS権限を持つか否か
	 * 
	 * @param managerName マネージャ名
	 * @return true:ADMINISTRATORS権限を持つ／false:ADMINISTRATORS権限を持たない
	 */
	public static boolean isAdministrator(String managerName) {
		boolean rtn = false;

		try {
			Set<String> roleSet = new HashSet<String>();
			AccessRestClientWrapper wrapper = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
			UserInfoResponseP3 resDto = wrapper.getOwnerRoleIdList();
			for( RoleInfoResponseP1 rec:  resDto.getRoleList()){
				roleSet.add(rec.getRoleId());
			} 
			if (!(roleSet.isEmpty())) {
				return roleSet.contains(RoleIdConstant.ADMINISTRATORS);
			}
		} catch ( RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass e) {
			// 何もしない
		}
		return rtn;
	}
}
