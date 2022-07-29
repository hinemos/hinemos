/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.jobmanagement.view.JobPlanListView;
import com.clustercontrol.jobmanagement.view.JobQueueReferrerView;
import com.clustercontrol.jobmanagement.view.JobQueueSettingView;
import com.clustercontrol.jobmanagement.view.JobKickListView;
import com.clustercontrol.jobmanagement.view.JobLinkSendSettingListView;

/**
 * ジョブ管理のパースペクティブを生成するクラスです。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobSettingPerspective extends ClusterControlPerspectiveBase {

	/**
	 * 画面レイアウトを実装します。
	 * 
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);
		//エディタ領域のIDを取得
		String editorArea = layout.getEditorArea();

		//エディタ領域の上部30%を占めるViewを作成
		layout.addView( JobListView.ID, IPageLayout.TOP, 0.33f, editorArea );
		//残りの50%を占めるViewの作成
		IFolderLayout middle = layout.createFolder( "middle", IPageLayout.TOP, 0.5f, editorArea );
		middle.addView(JobKickListView.ID);
		middle.addView(JobQueueSettingView.ID);
		middle.addView(JobLinkSendSettingListView.ID);
		//残りのViewの作成
		IFolderLayout bottom = layout.createFolder( "bottom", IPageLayout.TOP, IPageLayout.RATIO_MAX, editorArea );
		bottom.addView(JobPlanListView.ID);
		bottom.addView(JobQueueReferrerView.ID);
	}
}
