/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.calendar.view.CalendarPatternView;
import com.clustercontrol.calendar.view.CalendarListView;
import com.clustercontrol.calendar.view.CalendarMonthView;
import com.clustercontrol.calendar.view.CalendarWeekView;

/**
 * カレンダパースペクティブクラス<BR>
 * 
 * @version 4.1.0
 * @since 2.0.0
 */
public class CalendarPerspective extends ClusterControlPerspectiveBase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);

		//エディタ領域のIDを取得
		String editorArea = layout.getEditorArea();

		//エディタ領域の上部45%を占めるFolderを作成
		IFolderLayout top = layout.createFolder( "top",IPageLayout.TOP, 0.45f, editorArea );
		top.addView( CalendarListView.ID );
		top.addView( CalendarPatternView.ID );
		//エディタ領域の上部左45%(1-55%)を占めるViewを作成
		layout.addView( CalendarMonthView.ID, IPageLayout.RIGHT, 0.55f, "top" );

		//エディタ領域の下部55%を占めるフォルダの作成
		layout.addView( CalendarWeekView.ID, IPageLayout.TOP, IPageLayout.RATIO_MAX, editorArea );
	}
}
