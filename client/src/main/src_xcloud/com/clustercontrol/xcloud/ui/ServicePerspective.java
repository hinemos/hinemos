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
import com.clustercontrol.xcloud.ui.views.LoginUsersView;
import com.clustercontrol.xcloud.ui.views.RoleManagementView;
import com.clustercontrol.xcloud.ui.views.ServiceStateView;

public class ServicePerspective extends ClusterControlPerspectiveBase{
	public static final String Id = "com.clustercontrol.xcloud.ui.ServicePerspective";
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);

		//エディタ領域のIDを取得
		String editorArea = layout.getEditorArea();
		//エディタ領域の上部35%を占めるフォルダを作成
		IFolderLayout top = layout.createFolder("top", IPageLayout.TOP,
				0.35f, editorArea);
		//ID=topのフォルダの下部65%を占めるフォルダの作成
		IFolderLayout bottom = layout.createFolder("bottom",
				IPageLayout.BOTTOM, 0.65f, editorArea);
		
		top.addView(LoginUsersView.Id);
		top.addView(RoleManagementView.Id);
		bottom.addView(ServiceStateView.Id);
	}
}
