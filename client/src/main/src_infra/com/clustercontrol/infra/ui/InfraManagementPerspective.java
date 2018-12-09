/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.infra.view.InfraFileManagerView;
import com.clustercontrol.infra.view.InfraManagementView;
import com.clustercontrol.infra.view.InfraModuleView;

/**
 * パースペクティブを生成するクラスです。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class InfraManagementPerspective extends ClusterControlPerspectiveBase {

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
		IFolderLayout top = layout.createFolder("top", IPageLayout.TOP,
				0.50f, editorArea);
		IFolderLayout bottom = layout.createFolder("bottom",
				IPageLayout.BOTTOM, 0.5f, "top");

		top.addView(InfraManagementView.ID);
		top.addView(InfraFileManagerView.ID);
		bottom.addView(InfraModuleView.ID);
}
}