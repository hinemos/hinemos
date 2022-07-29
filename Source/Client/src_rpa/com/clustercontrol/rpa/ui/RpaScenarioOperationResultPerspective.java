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
import com.clustercontrol.rpa.view.RpaScenarioListView;
import com.clustercontrol.rpa.view.RpaScenarioSummaryGraphView;

/**
 * RPAシナリオ実績のパースペクティブを生成するクラス<BR>
 */
public class RpaScenarioOperationResultPerspective extends ClusterControlPerspectiveBase {

	public static final String ID = "com.clustercontrol.enterprise.ui.RpaScenarioOperationResultPerspective";


	/**
	 * 画面レイアウトを実装します。
	 * 
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);

		// エディタ領域のIDを取得
		String editorArea = layout.getEditorArea();

		float percent = 0.30f;
		// エディタ領域の上部percent%を占めるフォルダを作成
		IFolderLayout top = layout.createFolder( "top", IPageLayout.TOP, percent, editorArea );

		// 上部 RPAシナリオ実績[シナリオ一覧]ビューの表示
		top.addView(RpaScenarioListView.ID);
		top.addView(RpaScenarioSummaryGraphView.ID);
	}
}
