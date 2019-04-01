/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.jobmanagement.view.JobKickListView;
import com.clustercontrol.jobmanagement.view.JobQueueSettingView;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobMapImageListView;
import com.clustercontrol.jobmap.view.JobModuleView;
import com.clustercontrol.jobmap.view.JobTreeView;

/**
 * パースペクティブ構成のクラス
 * 2ビューから構成される。
 * @since 1.0.0
 */
public class JobMapEditorPerspective extends ClusterControlPerspectiveBase {
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);

		// エディタ領域を分割
		String editorArea = layout.getEditorArea();
		
		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.25f, editorArea);
		left.addView(JobTreeView.ID);

		layout.addView(JobModuleView.ID, IPageLayout.BOTTOM, 0.6f, "left");
		
		IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, 0.80f, editorArea);
		right.addView(JobMapEditorView.ID);
		right.addView(JobKickListView.ID);
		right.addView(JobMapImageListView.ID);
		right.addView(JobQueueSettingView.ID);
	}
}
