/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.MaintenanceInfoResponse;
import org.openapitools.client.model.MaintenanceScheduleRequest;
import org.openapitools.client.model.ModifyMaintenanceRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.maintenance.util.MaintenanceRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 *
 * メンテナンス情報を修正するクライアント側アクションクラス<BR>
 *
 * @version 3.0.0
 * @since 2.2.0
 *
 */
public class ModifyMaintenance {

	public boolean modify(String managerName, MaintenanceInfoResponse info) {
		boolean ret = false;

		String[] args = { info.getMaintenanceId(), managerName };
		try {
			MaintenanceRestClientWrapper wrapper = MaintenanceRestClientWrapper.getWrapper(managerName);

			ModifyMaintenanceRequest mod = new ModifyMaintenanceRequest();
			RestClientBeanUtil.convertBean(info, mod);
			mod.setTypeId(ModifyMaintenanceRequest.TypeIdEnum.fromValue(info.getTypeId().getValue()));
			mod.getSchedule().setType(MaintenanceScheduleRequest.TypeEnum.fromValue(info.getSchedule().getType().getValue()));
			wrapper.modifyMaintenance(info.getMaintenanceId(), mod);

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.maintenance.3", args));

			ret = true;

		} catch (HinemosUnknown e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.maintenance.2", args) + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole) {
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
