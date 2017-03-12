/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

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
