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
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

/**
 * ジョブ[一覧]ビューの「リソース制御ジョブの作成」のクライアント側アクションクラス
 */
public class CreateResourceJobAction extends BaseCreateAction {

	public static final String ID = "com.clustercontrol.xcloud.jobmap.view.action.CreateResourceJobAction";

	@Override
	public JobInfoWrapper.TypeEnum getJobType() {
		return JobInfoWrapper.TypeEnum.RESOURCEJOB;
	}

	@Override
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		return CloudRestClientWrapper.getWrapper(managerName);
	}
	
}
