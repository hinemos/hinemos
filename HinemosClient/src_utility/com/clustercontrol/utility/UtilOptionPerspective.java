/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


/**
 * 初期起動用パースペクティブ<BR>
 * 
 * Hinemosクライアントの初期表示にビューなどを
 * 追加する場合にはこのクラスを変更します。
 * 
 * 
 * @version 2.0.0
 * @since 1.2.0
 * 
 */
public class UtilOptionPerspective implements IPerspectiveFactory {

	/*
 	* (non-Javadoc)
	* 
	* @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	*/
	@Override
	public void createInitialLayout(IPageLayout layout) {
		
		  //エディタ領域を非表示にする
		layout.setEditorAreaVisible(false);
		
		/*
		 * アクションセットを追加
		 * メニューなどの追加はこのあたりで行う。
		 */
		layout.addActionSet("com.clustercontrol.utility.settings.ui.ActionSet");
		layout.addActionSet("com.clustercontrol.utility.traputil.ActionSet");
		
	}

}