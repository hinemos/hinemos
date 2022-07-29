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
 * ジョブ[一覧]ビューの「参照ジョブの作成」のクライアント側アクションクラス<BR>
 * 
 * @version 4.1.0
 * @since 4.1.0
 */
public class CreateReferJobAction extends BaseCreateAction {
	public static final String ID = ActionIdBase + CreateReferJobAction.class.getSimpleName();
	
	@Override
	public JobInfoWrapper.TypeEnum getJobType() {
		return JobInfoWrapper.TypeEnum.REFERJOB;
	}
}