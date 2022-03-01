/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui;

import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;


/**
 * 初期起動用パースペクティブ<BR>
 * 
 * Hinemosクライアントの初期表示にビューなどを
 * 追加する場合にはこのクラスを変更します。
 * 
 * 
 * @version 6.1.0
 * @since 1.2.0
 * 
 */
public class SettingToolsPerspective extends ClusterControlPerspectiveBase {

	public static final String ID = "com.clustercontrol.enterprise.utility.SettingToolsPerspective";

	/*
 	* (non-Javadoc)
 	* 
 	* @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
 	*/
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);
		layout.setEditorAreaVisible(false);
	}
}