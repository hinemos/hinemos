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
 * ジョブ[一覧]ビューの「監視ジョブの作成」のクライアント側アクションクラス<BR>
 * 
 * @version 6.0.a
 */
public class CreateApprovalJobAction extends BaseCreateAction {
	public static final String ID = ActionIdBase  + CreateApprovalJobAction.class.getSimpleName();

	@Override
	public JobInfoWrapper.TypeEnum getJobType() {
		return JobInfoWrapper.TypeEnum.APPROVALJOB;
	}
}