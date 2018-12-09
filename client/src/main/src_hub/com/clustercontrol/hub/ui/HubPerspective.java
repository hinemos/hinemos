/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.hub.view.TransferView;
import com.clustercontrol.hub.view.LogScopeTreeView;

public class HubPerspective extends ClusterControlPerspectiveBase {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);

		//エディタ領域のIDを取得
		String editorArea = layout.getEditorArea();

		IFolderLayout left = layout.createFolder( "left",IPageLayout.LEFT, 0.20f, editorArea );
		IFolderLayout right = layout.createFolder( "right",IPageLayout.RIGHT, 0.95f, editorArea );
		
		left.addView(LogScopeTreeView.ID);
		//right.addView(LogSummaryView.ID);
		//right.addView(LogSearchView.ID);
		right.addView(TransferView.ID);
	}
}
