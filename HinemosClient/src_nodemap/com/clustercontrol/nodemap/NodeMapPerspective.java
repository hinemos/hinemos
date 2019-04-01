/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.nodemap.util.SecondaryIdMap;
import com.clustercontrol.nodemap.view.EventViewM;
import com.clustercontrol.nodemap.view.NodeListView;
import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.nodemap.view.ScopeTreeView;
import com.clustercontrol.nodemap.view.StatusViewM;

/**
 * パースペクティブ構成のクラス
 * 4ビューから構成される。
 * @since 1.0.0
 */
public class NodeMapPerspective extends ClusterControlPerspectiveBase {
	private static Log m_log = LogFactory.getLog( NodeMapPerspective.class );
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);
		
		m_log.info("createInitialLayout");
		SecondaryIdMap.init();
		
		// エディタ領域を分割
		String editorArea = layout.getEditorArea();
		IFolderLayout top = layout.createFolder("top", IPageLayout.TOP,
				0.70f, editorArea);
		IFolderLayout topLeft = layout.createFolder("topLeft",
				IPageLayout.LEFT, 0.20f, "top");
		IFolderLayout bottom = layout.createFolder("bottom",
				IPageLayout.BOTTOM, 0.30f, editorArea);

		// ビューを登録
		topLeft.addView(ScopeTreeView.ID);
		top.addPlaceholder(NodeMapView.ID + ":*");
		top.addPlaceholder(NodeListView.ID + ":*");
		bottom.addView(StatusViewM.ID);
		bottom.addView(EventViewM.ID);

	}
}
