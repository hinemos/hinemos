/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.ui;

import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.jobmanagement.view.JobPlanListView;
import com.clustercontrol.jobmanagement.view.JobKickListView;

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
		layout.addView( JobKickListView.ID, IPageLayout.TOP, 0.5f, editorArea );
		//残りのViewの作成
		layout.addView( JobPlanListView.ID, IPageLayout.TOP, IPageLayout.RATIO_MAX, editorArea );
	}
}
