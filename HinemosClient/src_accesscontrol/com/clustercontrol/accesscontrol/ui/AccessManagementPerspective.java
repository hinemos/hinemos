package com.clustercontrol.accesscontrol.ui;

import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.accesscontrol.view.RoleListView;
import com.clustercontrol.accesscontrol.view.RoleSettingTreeView;
import com.clustercontrol.accesscontrol.view.SystemPrivilegeListView;
import com.clustercontrol.accesscontrol.view.UserListView;

/**
 * アカウントのパースペクティブを生成するクラスです。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class AccessManagementPerspective extends ClusterControlPerspectiveBase {

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

		// エディタ領域の上部5割を占めるViewを作成
		layout.addView( RoleListView.ID, IPageLayout.TOP, 0.5f, editorArea );
		// 上部右側5割を占めるViewを作成
		layout.addView( UserListView.ID, IPageLayout.RIGHT, 0.5f, RoleListView.ID );

		// エディタ領域の下部残りのスペース(5割)を占めるViewを作成
		layout.addView( RoleSettingTreeView.ID, IPageLayout.TOP, IPageLayout.RATIO_MAX, editorArea );
		// 下部右側7割(1-0.3)を占めるViewを作成
		layout.addView( SystemPrivilegeListView.ID, IPageLayout.RIGHT, 0.3f, RoleSettingTreeView.ID );
	}
}
