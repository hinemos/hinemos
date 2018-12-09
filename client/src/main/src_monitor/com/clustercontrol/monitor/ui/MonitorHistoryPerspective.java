/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.monitor.view.StatusView;

/**
 * 監視管理のパースペクティブを生成するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class MonitorHistoryPerspective extends ClusterControlPerspectiveBase {

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

		float percent = 0.5f;
		// エディタ領域の上部percent%を占めるフォルダを作成
		IFolderLayout top = layout.createFolder( "top", IPageLayout.TOP, percent, editorArea );
		top.addView(StatusView.ID);

		// 下部50%を占めるフォルダを作成
		IFolderLayout bottom = layout.createFolder( "bottom", IPageLayout.TOP, IPageLayout.RATIO_MAX - percent, editorArea);
		bottom.addView(EventView.ID);
	}
}
