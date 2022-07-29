/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmap.view.action;

import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;

public class CreateRpaJobAction extends BaseCreateAction {
	public static final String ID = ActionIdBase + CreateRpaJobAction.class.getSimpleName();

	@Override
	public JobInfoWrapper.TypeEnum getJobType() {
		return JobInfoWrapper.TypeEnum.RPAJOB;
	}

	@Override
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		// RpaRestEndpointsにはcheckPublishが存在しない
		// どのEndpointでも内容は同じなのでUtilityを使用する
		return UtilityRestClientWrapper.getWrapper(managerName);
	}
}
