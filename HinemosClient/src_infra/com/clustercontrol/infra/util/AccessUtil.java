/*

Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.infra.dialog.AccessInfoDialog;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.ws.infra.AccessInfo;
import com.clustercontrol.ws.repository.HinemosUnknown_Exception;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.InvalidUserPass_Exception;

public class AccessUtil {
	// ログ
	private static Log m_log = LogFactory.getLog( AccessUtil.class );

	public static List<AccessInfo> getAccessInfoList(Shell shell, String facilityId, String ownerRoleId, String managerName, boolean useNodeProp) {
		List<AccessInfo> accessInfoList = new ArrayList<AccessInfo>();
		List<String> nodeInfoList = null;
		try {
			RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
			nodeInfoList = wrapper.getExecTargetFacilityIdList(facilityId, ownerRoleId);
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
			m_log.error("getAccessInfoList() getNodeList, " + e.getMessage());
		}
		if(nodeInfoList != null){
			for(String nodeId: nodeInfoList){
				AccessInfo info = new AccessInfo();
				info.setFacilityId(nodeId);
				accessInfoList.add(info);
			}
		}
		
		if (useNodeProp) {
			return accessInfoList;
		}
		
		boolean isAfterSameAll = false;
		AccessInfo tmpAccessInfo = null;
		for(AccessInfo accessInfo: accessInfoList){
			if (isAfterSameAll){
				accessInfo.setSshUser(tmpAccessInfo.getSshUser());
				accessInfo.setSshPassword(tmpAccessInfo.getSshPassword());
				accessInfo.setSshPrivateKeyFilepath(tmpAccessInfo.getSshPrivateKeyFilepath());
				accessInfo.setSshPrivateKeyPassphrase(tmpAccessInfo.getSshPrivateKeyPassphrase());
				accessInfo.setWinRmUser(tmpAccessInfo.getWinRmUser());
				accessInfo.setWinRmPassword(tmpAccessInfo.getWinRmPassword());
			} else {
				AccessInfoDialog dialog = new AccessInfoDialog(shell, accessInfo);
				tmpAccessInfo = accessInfo;
				if(dialog.open() == AccessInfoDialog.OK){
					isAfterSameAll = dialog.isAfterSameAll();
				} else {
					// 入力を中断した場合、アクションを中止する
					return null;
				}
			}
		}
		
		return accessInfoList;
	}
}
