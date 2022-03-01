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
 * ジョブ[一覧]ビューの「ジョブ連携送信ジョブの作成」のクライアント側アクションクラス<BR>
 * 
 */
public class CreateJobLinkSendJobAction extends BaseCreateAction {
	public static final String ID = ActionIdBase + CreateJobLinkSendJobAction.class.getSimpleName();

	@Override
	public JobInfoWrapper.TypeEnum getJobType() {
		return JobInfoWrapper.TypeEnum.JOBLINKSENDJOB;
	}
}