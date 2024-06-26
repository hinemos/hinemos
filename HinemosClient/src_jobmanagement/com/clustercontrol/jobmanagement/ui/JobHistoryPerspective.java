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
import com.clustercontrol.jobmanagement.view.ForwardFileView;
import com.clustercontrol.jobmanagement.view.JobDetailView;
import com.clustercontrol.jobmanagement.view.JobHistoryView;
import com.clustercontrol.jobmanagement.view.JobLinkMessageView;
import com.clustercontrol.jobmanagement.view.JobNodeDetailView;
import com.clustercontrol.jobmanagement.view.JobQueueActivityView;
import com.clustercontrol.jobmanagement.view.JobQueueContentsView;

/**
 * ジョブ管理のパースペクティブを生成するクラスです。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobHistoryPerspective extends ClusterControlPerspectiveBase {

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
		IFolderLayout top = layout.createFolder( "top", IPageLayout.TOP, 0.33f, editorArea );
		top.addView(JobHistoryView.ID);
		top.addView(JobQueueActivityView.ID);
		top.addView(JobLinkMessageView.ID);
		//残りの50%を占めるViewの作成
		IFolderLayout middle = layout.createFolder( "middle", IPageLayout.TOP, 0.5f, editorArea );
		middle.addView(JobDetailView.ID);
		middle.addView(JobQueueContentsView.ID);
		//残りのViewの作成
		IFolderLayout bottom = layout.createFolder( "buttom", IPageLayout.TOP, IPageLayout.RATIO_MAX, editorArea );
		bottom.addView(JobNodeDetailView.ID);
		bottom.addView(ForwardFileView.ID);
	}
}
