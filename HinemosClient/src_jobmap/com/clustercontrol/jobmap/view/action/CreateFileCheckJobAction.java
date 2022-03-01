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
 * ジョブ[一覧]ビューの「ファイルチェックジョブの作成」のクライアント側アクションクラス<BR>
 * 
 * @version 6.0.a
 */
public class CreateFileCheckJobAction extends BaseCreateAction {
	public static final String ID = ActionIdBase + CreateFileCheckJobAction.class.getSimpleName();

	@Override
	public JobInfoWrapper.TypeEnum getJobType() {
		return JobInfoWrapper.TypeEnum.FILECHECKJOB;
	}
}