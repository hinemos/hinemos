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
import com.clustercontrol.jobmanagement.view.ForwardFileView;
import com.clustercontrol.jobmanagement.view.JobNodeDetailView;
import com.clustercontrol.jobmap.view.JobHistoryViewM;
import com.clustercontrol.jobmap.view.JobMapHistoryView;

/**
 * パースペクティブ構成のクラス
 * 2ビューから構成される。
 * @since 1.0.0
 */
public class JobMapHistoryPerspective extends ClusterControlPerspectiveBase {
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);

		// エディタ領域を分割
		String editorArea = layout.getEditorArea();
		IFolderLayout top = layout.createFolder("top", IPageLayout.TOP,
				0.30f, editorArea);
		IFolderLayout bottom = layout.createFolder("bottom",
				IPageLayout.BOTTOM, 0.70f, editorArea);
		top.addView(JobHistoryViewM.ID);
		bottom.addView(JobMapHistoryView.ID);
		bottom.addView(JobNodeDetailView.ID);
		bottom.addView(ForwardFileView.ID);
	}
}
