/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

/**
 * ジョブ[一覧]ビューの「ジョブ連携待機ジョブの作成」のクライアント側アクションクラス<BR>
 * 
 */
public class CreateJobLinkRcvJobAction extends BaseCreateAction {
	public static final String ID = ActionIdBase + CreateJobLinkRcvJobAction.class.getSimpleName();

	@Override
	public JobInfoWrapper.TypeEnum getJobType() {
		return JobInfoWrapper.TypeEnum.JOBLINKRCVJOB;
	}
}