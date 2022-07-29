/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.sdml.view.MonitorListViewM;
import com.clustercontrol.sdml.view.SdmlControlSettingListView;

/**
 * SDML設定パースペクティブ
 *
 */
public class SdmlSettingPerspective extends ClusterControlPerspectiveBase {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);

		// エディタ領域のIDを取得
		String editorArea = layout.getEditorArea();

		// エディタ領域を分割
		float percent = 0.35f;
		IFolderLayout top = layout.createFolder("top", IPageLayout.TOP, percent, editorArea);
		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, (IPageLayout.RATIO_MAX - percent),
				editorArea);

		// ビューを登録
		top.addView(SdmlControlSettingListView.ID);
		bottom.addView(MonitorListViewM.ID);
	}
}
