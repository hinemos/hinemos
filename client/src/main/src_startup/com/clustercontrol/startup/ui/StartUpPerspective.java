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

package com.clustercontrol.startup.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.startup.view.StartUpView;

/**
 * ジョブ管理のパースペクティブを生成するクラスです。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class StartUpPerspective extends ClusterControlPerspectiveBase {
	public static final String ID = StartUpPerspective.class.getName();

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
		//エディタ領域の上部100%を占めるフォルダを作成
		IFolderLayout top = layout.createFolder( "top", IPageLayout.TOP, IPageLayout.RATIO_MAX, editorArea );
		top.addView(StartUpView.ID);
	}
}
