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

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.maintenance.util.MaintenanceEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.maintenance.HinemosUnknown_Exception;
import com.clustercontrol.ws.maintenance.InvalidRole_Exception;
import com.clustercontrol.ws.maintenance.MaintenanceInfo;

/**
 *
 * メンテナンス情報を修正するクライアント側アクションクラス<BR>
 *
 * @version 3.0.0
 * @since 2.2.0
 *
 */
public class ModifyMaintenance {

	public boolean modify(String managerName, MaintenanceInfo info) {
		boolean ret = false;

		String[] args = { info.getMaintenanceId(), managerName };
		try {
			MaintenanceEndpointWrapper wrapper = MaintenanceEndpointWrapper.getWrapper(managerName);
			wrapper.modifyMaintenance(info);

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.maintenance.3", args));

			ret = true;

		} catch (HinemosUnknown_Exception e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.maintenance.2", args) + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole_Exception) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} else {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}

			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.maintenance.4", args) + errMessage);
		}

		return ret;
	}

}
