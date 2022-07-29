/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.xcloud.ui.views.NetworksView;
import com.clustercontrol.xcloud.ui.views.RepositoryView;

public class NetworkPerspective extends ClusterControlPerspectiveBase{
	public static final String Id = "com.clustercontrol.xcloud.ui.NetworkPerspective";
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);
		
		//エディタ領域のIDを取得
		String editorArea = layout.getEditorArea();
		//エディタ領域の左部33%を占めるフォルダを作成
		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT,
				0.33f, editorArea);
		//エディタ領域の上部50%を占めるフォルダを作成
		IFolderLayout top = layout.createFolder("top", IPageLayout.TOP,
				0.50f, editorArea);
		//ID=topのフォルダの下部50%を占めるフォルダの作成
//		IFolderLayout bottom = layout.createFolder("bottom",
//				IPageLayout.BOTTOM, 0.5f, "top");
		left.addView(RepositoryView.Id);
		top.addView(NetworksView.Id);
//		bottom.addView(InstanceBackupsView.Id);
	}
}
