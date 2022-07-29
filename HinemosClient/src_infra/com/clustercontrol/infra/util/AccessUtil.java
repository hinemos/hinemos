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
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.CreateAccessInfoListForDialogResponse;
import org.openapitools.client.model.InfraManagementInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.infra.dialog.AccessInfoDialog;

public class AccessUtil {
	// ログ
	private static Log m_log = LogFactory.getLog( AccessUtil.class );

	public static List<CreateAccessInfoListForDialogResponse> getAccessInfoList(Shell shell, InfraManagementInfoResponse management, List<String> moduleIdList, String managerName) {
		List<CreateAccessInfoListForDialogResponse> accessInfoList = new ArrayList<CreateAccessInfoListForDialogResponse>();
		try {
			InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(managerName);
			accessInfoList = wrapper.createAccessInfoListForDialog(management.getManagementId(), moduleIdList.stream().collect(Collectors.joining(",")));
		} catch (RestConnectFailed | InfraManagementNotFound | InvalidUserPass | InvalidRole | HinemosUnknown
				| InvalidSetting e) {
			m_log.error("getAccessInfoList() getNodeList, " + e.getMessage());
		}

		boolean isAfterSameAll = false;
		CreateAccessInfoListForDialogResponse tmpAccessInfo = null;
		for(CreateAccessInfoListForDialogResponse accessInfo: accessInfoList){
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
