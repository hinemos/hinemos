/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.view.action;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.SetSdmlControlSettingStatusRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.sdml.util.SdmlRestClientWrapper;
import com.clustercontrol.util.Messages;

/**
 * SDML制御設定のSDML制御ログ収集を有効化するビューアクション
 *
 */
public class SdmlControlLogCollectorEnableAction extends SdmlSettingEnableBaseAction {
	private static Log logger = LogFactory.getLog(SdmlControlLogCollectorEnableAction.class);

	/** アクションID */
	public static final String ID = SdmlControlLogCollectorEnableAction.class.getName();

	@Override
	protected String getAction() {
		return Messages.getString("enable");
	}

	@Override
	protected String getTarget() {
		return Messages.getString("sdml.control.log.collect");
	}

	@Override
	protected void action(String managerName, List<String> applicationIds) throws RestConnectFailed, HinemosUnknown,
			SdmlControlSettingNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		logger.debug("action() : " + SdmlControlLogCollectorEnableAction.class.getSimpleName());
		SdmlRestClientWrapper wrapper = SdmlRestClientWrapper.getWrapper(managerName);

		SetSdmlControlSettingStatusRequest request = new SetSdmlControlSettingStatusRequest();
		request.setApplicationIds(applicationIds);
		request.setValidFlg(true);
		wrapper.setSdmlControlSettingLogCollectorV1(request);
	}

}
