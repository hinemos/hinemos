/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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

import com.clustercontrol.accesscontrol.composite.RoleListComposite;
import com.clustercontrol.accesscontrol.composite.action.RoleListSelectionChangedListener;
import com.clustercontrol.accesscontrol.view.action.RoleAddAction;
import com.clustercontrol.accesscontrol.view.action.RoleDeleteAction;
import com.clustercontrol.accesscontrol.view.action.RoleModifyAction;
import com.clustercontrol.accesscontrol.view.action.RoleRefreshAction;
import com.clustercontrol.bean.Property;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;

/**
 * アクセス権限[ロール]ビュークラス<BR>
 *
 * クライアントの画面を構成します。
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class RoleListView extends CommonViewPart {
	/** ビューID */
	public static final String ID = RoleListView.class.getName();

	/** アクセス[ロール]ビュー用のコンポジット */
	private RoleListComposite m_roleList = null;

	/** 選択レコード数 */
	private int rowNum = 0;

	/**
	 * コンストラクタ
	 */
	public RoleListView() {
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

		m_roleList = new RoleListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_roleList);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_roleList.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		// ボタン（アクション）を制御するリスナーを登録
		this.m_roleList.getTableViewer().addSelectionChangedListener(
				new RoleListSelectionChangedListener());

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

		Menu menu = menuManager.createContextMenu(m_roleList.getTable());
		WidgetTestUtil.setTestId(this, null, menu);
		m_roleList.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, m_roleList.getTableViewer() );
	}

	/**
	 * ビューを更新します。
	 *
	 * @see com.clustercontrol.accesscontrol.composite.RoleListComposite#update()
	 * @see com.clustercontrol.accesscontrol.composite.RoleListComposite#update(Property)
	 */
	public void update() {
		this.m_roleList.update();
	}

	/**
	 * アカウント[ロール]ビュー用のコンポジットを返します。
	 *
	 * @return アカウント[ロール]ビュー用のコンポジット
	 */
	public RoleListComposite getComposite() {
		return m_roleList;
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
			service.refreshElements(RoleAddAction.ID, null);
			service.refreshElements(RoleModifyAction.ID, null);
			service.refreshElements(RoleDeleteAction.ID, null);
			service.refreshElements(RoleRefreshAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}
}
