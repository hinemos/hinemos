/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.approval.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.approval.view.ApprovalView;

/**
 * 承認のパースペクティブを生成するクラスです。
 * 
 * @version 5.1.0
 * @since 5.1.0
 */
public class ApprovalPerspective extends ClusterControlPerspectiveBase {

	public static final String ID = ApprovalPerspective.class.getName();

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
		top.addView(ApprovalView.ID);
	}
}
