/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.repository.view.AgentListView;
import com.clustercontrol.repository.view.NodeAttributeView;
import com.clustercontrol.repository.view.NodeListView;
import com.clustercontrol.repository.view.NodeScopeView;
import com.clustercontrol.repository.view.ScopeListView;

/**
 * リポジトリ機能のパースペクティブを生成するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class RepositoryPerspective extends ClusterControlPerspectiveBase {

	/**
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);

		//エディタ領域のIDを取得
		String editorArea = layout.getEditorArea();

		float topLeftRatio = 0.65f;
		float bottomRatio = 0.6f;

		//エディタ領域の左部65%を占めるフォルダを作成
		IFolderLayout left = layout.createFolder( "left", IPageLayout.LEFT, topLeftRatio, editorArea );
		//エディタ領域の右部35%を占めるフォルダを作成
		IFolderLayout right = layout.createFolder( "right", IPageLayout.RIGHT, IPageLayout.RATIO_MAX-topLeftRatio, editorArea );
		//エディタ左領域の下部40%を占めるフォルダを作成
		IFolderLayout bottom = layout.createFolder( "bottom", IPageLayout.BOTTOM, bottomRatio, "left" );

		left.addView(NodeListView.ID);
		left.addView(AgentListView.ID);
		right.addView(NodeAttributeView.ID);
		bottom.addView(ScopeListView.ID);
		bottom.addView(NodeScopeView.ID);
	}
}
