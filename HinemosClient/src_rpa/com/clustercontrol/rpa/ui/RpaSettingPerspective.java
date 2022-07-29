/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.rpa.view.RpaManagementToolAccountView;
import com.clustercontrol.rpa.view.RpaScenarioOperationResultCreateSettingView;
import com.clustercontrol.rpa.view.RpaScenarioTagView;

/**
 * RPAシナリオパースペクティブ
 */
public class RpaSettingPerspective extends ClusterControlPerspectiveBase {

	public static final String ID = "com.clustercontrol.enterprise.rpa.ui.RpaSettingPerspective";

	/**
	 * 画面レイアウトを実装します。
	 * 
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);

		float percent = 0.5f;
		// エディタ領域のIDを取得
		String editorArea = layout.getEditorArea();
		// エディタ領域の上部percent%を占めるフォルダを作成
		IFolderLayout top = layout.createFolder("top", IPageLayout.TOP, percent, editorArea);
		// エディタ領域の下部(1-percent)%を占めるフォルダを作成
		IFolderLayout bottom = layout.createFolder( "bottom", IPageLayout.BOTTOM, (IPageLayout.RATIO_MAX - percent), editorArea );

		top.addView(RpaManagementToolAccountView.ID);
		bottom.addView(RpaScenarioOperationResultCreateSettingView.ID);
		bottom.addView(RpaScenarioTagView.ID);
	}
}