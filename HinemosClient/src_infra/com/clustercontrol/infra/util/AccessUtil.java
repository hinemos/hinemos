/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.infra.dialog.AccessInfoDialog;
import com.clustercontrol.ws.infra.AccessInfo;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.infra.InfraManagementNotFound_Exception;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;

public class AccessUtil {
	// ログ
	private static Log m_log = LogFactory.getLog( AccessUtil.class );

	public static List<AccessInfo> getAccessInfoList(Shell shell, InfraManagementInfo management, List<String> moduleIdList, String managerName) {
		List<AccessInfo> accessInfoList = new ArrayList<AccessInfo>();
		try {
			InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
			accessInfoList = wrapper.createAccessInfoListForDialog(management.getManagementId(), moduleIdList);
		} catch (InfraManagementNotFound_Exception | HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
			m_log.error("getAccessInfoList() getNodeList, " + e.getMessage());
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
