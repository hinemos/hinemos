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

package com.clustercontrol.maintenance.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.maintenance.view.HinemosPropertyView;
import com.clustercontrol.maintenance.view.MaintenanceListView;


/**
 * メンテナンスパースペクティブクラス<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class MaintenancePerspective extends ClusterControlPerspectiveBase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);

		float percent = 0.4f;
		//エディタ領域のIDを取得
		String editorArea = layout.getEditorArea();
		//エディタ領域の上部percent%を占めるフォルダを作成
		IFolderLayout top = layout.createFolder("top", IPageLayout.TOP, percent, editorArea);
		// エディタ領域の下部(1-percent)%を占めるフォルダを作成
		IFolderLayout bottom = layout.createFolder( "bottom", IPageLayout.BOTTOM, (IPageLayout.RATIO_MAX - percent), editorArea );

		top.addView(MaintenanceListView.ID);
		bottom.addView(HinemosPropertyView.ID);
	}
}
