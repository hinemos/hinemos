/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.startup.ui;

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

		//エディタ領域全体をStartUpViewとする
		layout.addStandaloneView(StartUpView.ID, false, IPageLayout.TOP, IPageLayout.RATIO_MAX, editorArea);
	}
}
