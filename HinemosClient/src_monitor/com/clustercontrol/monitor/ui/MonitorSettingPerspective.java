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
import com.clustercontrol.hub.view.LogFormatView;
import com.clustercontrol.monitor.view.MonitorListView;
import com.clustercontrol.notify.mail.view.MailTemplateListView;
import com.clustercontrol.notify.view.CommandTemplateListView;
import com.clustercontrol.notify.restaccess.view.RestAccessInfoListView;
import com.clustercontrol.notify.view.NotifyListView;

/**
 * 監視設定のパースペクティブを生成するクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class MonitorSettingPerspective extends ClusterControlPerspectiveBase {

	/**
	 * 画面レイアウトを実装します。
	 * 
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);

		// エディタ領域のIDを取得
		String editorArea = layout.getEditorArea();

		float percent = 0.30f;
		// エディタ領域の上部percent%を占めるフォルダを作成
		IFolderLayout top = layout.createFolder( "top", IPageLayout.TOP, percent, editorArea );
		// エディタ領域の下部(1-percent)%を占めるフォルダを作成
		IFolderLayout bottom = layout.createFolder( "bottom", IPageLayout.BOTTOM, (IPageLayout.RATIO_MAX - percent), editorArea );

		// 上部 通知[一覧]ビューの表示
		// 上部 メールテンプレート[一覧]ビューの表示
		top.addView(NotifyListView.ID);
		top.addView(MailTemplateListView.ID);
		top.addView(LogFormatView.ID);
		top.addView(CommandTemplateListView.ID);
		top.addView(RestAccessInfoListView.ID);

		// 下部 監視[一覧]ビューの表示
		bottom.addView(MonitorListView.ID);
	}
}
