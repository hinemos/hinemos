/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * 基底パースペクティブ<BR>
 * 
 * パースペクティブはこのクラスを継承すること。
 * 
 * 
 * @version 4.1.0
 * @since 1.0.0
 */
public class ClusterControlPerspectiveBase implements IPerspectiveFactory{

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {

		//エディタを非表示
		layout.setEditorAreaVisible(false);

	}
}
