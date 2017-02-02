/*

Copyright (C) 2007 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.maintenance.util.MaintenanceEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.maintenance.InvalidRole_Exception;
import com.clustercontrol.ws.maintenance.MaintenanceInfo;

/**
 * メンテナンス情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 4.0.0
 * @since 2.2.0
 */
public class GetMaintenance {

	// ログ
	private static Log m_log = LogFactory.getLog( GetMaintenance.class );


	/**
	 * メンテナンス情報を返します。
	 *
	 * @param maintenanceId
	 * @return
	 */
	public MaintenanceInfo getMaintenanceInfo(String managerName, String maintenanceId) {

		MaintenanceInfo info = null;
		try {
			MaintenanceEndpointWrapper wrapper = MaintenanceEndpointWrapper.getWrapper(managerName);
			info = wrapper.getMaintenanceInfo(maintenanceId);
		} catch (InvalidRole_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("getMaintenanceInfo(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return info;
	}
}
