/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

/**
 * ジョブ[一覧]ビューの「ジョブネットの作成」のクライアント側アクションクラス<BR>
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
public class CreateJobNetAction extends BaseCreateAction {
	public static final String ID = ActionIdBase + CreateJobNetAction.class.getSimpleName();

	@Override
	public JobInfoWrapper.TypeEnum getJobType() {
		return JobInfoWrapper.TypeEnum.JOBNET;
	}
}