/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect;

import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.collect.view.CollectGraphView;

/**
 * 性能のパースペクティブを生成するクラス
 * 
 */
public class CollectPerspective extends ClusterControlPerspectiveBase {

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
		// エディタ領域の下部(1-percent)％を占めるフォルダの作成
		layout.addView(CollectGraphView.ID, IPageLayout.TOP, 0.5f, editorArea);
	}
}
