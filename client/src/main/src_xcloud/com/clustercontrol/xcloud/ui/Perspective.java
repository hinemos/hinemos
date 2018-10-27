/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.xcloud.extensions.CloudViewPluginExtension;

public class Perspective extends ClusterControlPerspectiveBase{
	public static final String Id = "com.clustercontrol.xcloud.ui.Perspective";
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);

		String editorArea = layout.getEditorArea();
		
		List<CloudViewPluginExtension.PluginView> views = CloudViewPluginExtension.getPluginViews();
		
		float leftRatio = 0.25f;
		float centerRatio = 0.666f;
		float topRatio = 0.34f;
		
		Set<CloudViewPluginExtension.ViewPosition> posFlg = new HashSet<CloudViewPluginExtension.ViewPosition>();
		
		for(CloudViewPluginExtension.PluginView view: views){
			posFlg.add(view.getPosition());
		}
		if(!posFlg.contains(CloudViewPluginExtension.ViewPosition.middle) || !posFlg.contains(CloudViewPluginExtension.ViewPosition.bottom)){
			topRatio = 0.5f;
		}
		if(!posFlg.contains(CloudViewPluginExtension.ViewPosition.middle) && !posFlg.contains(CloudViewPluginExtension.ViewPosition.bottom) && !posFlg.contains(CloudViewPluginExtension.ViewPosition.top)){
			leftRatio = 0.5f;
		}
		if(!posFlg.contains(CloudViewPluginExtension.ViewPosition.left)){
			centerRatio = 0.75f;
		}
		
		Map<CloudViewPluginExtension.ViewPosition, IFolderLayout> folderLayout = new HashMap<CloudViewPluginExtension.ViewPosition, IFolderLayout>();
		
		folderLayout.put(CloudViewPluginExtension.ViewPosition.left, layout.createFolder("left", IPageLayout.LEFT, leftRatio, editorArea));
		folderLayout.put(CloudViewPluginExtension.ViewPosition.right, layout.createFolder("right", IPageLayout.LEFT, 1.0f, editorArea));
		folderLayout.put(null, layout.createFolder("center", IPageLayout.LEFT, centerRatio, "right"));
		folderLayout.put(CloudViewPluginExtension.ViewPosition.top, layout.createFolder("top", IPageLayout.TOP, topRatio, "center"));
		folderLayout.put(CloudViewPluginExtension.ViewPosition.bottom, layout.createFolder("bottom", IPageLayout.TOP, 1.0f, "center"));
		folderLayout.put(CloudViewPluginExtension.ViewPosition.middle, layout.createFolder("middle", IPageLayout.TOP, 0.5f, "bottom"));
				
		for(CloudViewPluginExtension.PluginView view: views){
			folderLayout.get(view.getPosition()).addView(view.getId());
		}
	}
}
