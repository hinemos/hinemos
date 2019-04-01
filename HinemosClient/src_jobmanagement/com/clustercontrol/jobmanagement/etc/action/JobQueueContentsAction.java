/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.etc.action;

import com.clustercontrol.etc.action.ShowViewAction;
import com.clustercontrol.jobmanagement.view.JobQueueContentsView;

/**
 * ジョブ履歴[同時実行制御状況]ビューを表示するクライアント側アクションクラス
 * 
 * @since 6.2.0
 */
public class JobQueueContentsAction extends ShowViewAction {

	public JobQueueContentsAction() {
		super(JobQueueContentsView.class, JobQueueContentsView.ID);
	}
}
