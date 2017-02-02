/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.accesscontrol.view;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.accesscontrol.composite.UserListComposite;
import com.clustercontrol.accesscontrol.composite.action.UserListSelectionChangedListener;
import com.clustercontrol.accesscontrol.view.action.ModifyPasswordAction;
import com.clustercontrol.accesscontrol.view.action.UserAddAction;
import com.clustercontrol.accesscontrol.view.action.UserDeleteAction;
import com.clustercontrol.accesscontrol.view.action.UserModifyAction;
import com.clustercontrol.accesscontrol.view.action.UserRefreshAction;
import com.clustercontrol.bean.Property;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;

/**
 * アクセス[ユーザ]ビュークラス<BR>
 *
 * クライアントの画面を構成します。
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class UserListView extends CommonViewPart {
	/** ビューID */
	public static final String ID = UserListView.class.getName();

	/** アクセス[ユーザ]ビュー用のコンポジット */
	private UserListComposite m_userList = null;

	/** 選択レコード数 */
	private int rowNum = 0;

	/**
	 * コンストラクタ
	 */
	public UserListView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ビューを構築します。
	 *
	 * @param parent 親コンポジット
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 * @see #createContextMenu()
	 * @see #update()
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_userList = new UserListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_userList);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_userList.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		// ボタン（アクション）を制御するリスナーを登録
		this.m_userList.getTableViewer().addSelectionChangedListener(
				new UserListSelectionChangedListener());

		//ビューを更新
		this.update();
	}

	/**
	 * コンテキストメニューを作成します。
	 *
	 * @see org.eclipse.jface.action.MenuManager
	 * @see org.eclipse.swt.widgets.Menu
	 */
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(m_userList.getTable());
		WidgetTestUtil.setTestId(this, null, menu);
		m_userList.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, m_userList.getTableViewer() );
	}

	/**
	 * ビューを更新します。
	 *
	 * @see com.clustercontrol.accesscontrol.composite.UserListComposite#update()
	 * @see com.clustercontrol.accesscontrol.composite.UserListComposite#update(Property)
	 */
	public void update() {
		m_userList.update();
	}

	/**
	 * アクセス[ユーザ]ビュー用のコンポジットを返します。
	 *
	 * @return アクセス[ユーザ]ビュー用のコンポジット
	 */
	public UserListComposite getComposite() {
		return m_userList;
	}

	/**
	 * 選択レコード数を返します。
	 * @return rowNum
	 */
	public int getSelectedNum(){
		return this.rowNum;
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param num 選択イベント数
	 * @param selection ボタン（アクション）を有効にするための情報
	 */
	public void setEnabledAction(int num, ISelection selection) {
		this.rowNum = num;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(UserModifyAction.ID, null);
			service.refreshElements(UserAddAction.ID, null);
			service.refreshElements(UserDeleteAction.ID, null);
			service.refreshElements(UserRefreshAction.ID, null);
			service.refreshElements(ModifyPasswordAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}
}
