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

package com.clustercontrol.repository.view;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.bean.Property;
import com.clustercontrol.repository.composite.NodeListComposite;
import com.clustercontrol.repository.composite.action.NodeListSelectionChangedListener;
import com.clustercontrol.repository.view.action.NodeCopyAction;
import com.clustercontrol.repository.view.action.NodeDeleteAction;
import com.clustercontrol.repository.view.action.NodeFilterAction;
import com.clustercontrol.repository.view.action.NodeModifyAction;
import com.clustercontrol.repository.view.action.ProgramExecutionAction;
import com.clustercontrol.repository.view.action.RefreshAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;

/**
 * ノート一覧ビュークラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class NodeListView extends CommonViewPart {
	public static final String ID = NodeListView.class.getName();

	// ----- instance フィールド ----- //

	/** ノード一覧コンポジット */
	private NodeListComposite composite = null;

	/** 検索条件 */
	private Property condition = null;

	/** 選択レコード数 */
	private int rowNum = 0;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 */
	public NodeListView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
	}

	// ----- instance メソッド ----- //
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		this.composite = new NodeListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId( this, null, this.composite );

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.composite.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		// ボタン（アクション）を制御するリスナーを登録
		this.composite.getTableViewer().addSelectionChangedListener(
				new NodeListSelectionChangedListener());

		this.update();
	}


	/**
	 * ポップアップメニュー作成
	 */
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(this.composite.getTable());
		WidgetTestUtil.setTestId( this, null, menu );

		this.composite.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, this.composite.getTableViewer() );
	}

	/**
	 * compositeを返します。
	 *
	 * @return composite
	 */
	public NodeListComposite getComposite() {
		return this.composite;
	}

	/**
	 * 検索条件にヒットしたノードの一覧を表示します。
	 * <p>
	 *
	 * conditionがnullの場合、全ノードを表示します。
	 *
	 * @param condition
	 *            検索条件
	 */
	public void update(Property condition) {
		this.condition = condition;

		this.update();
	}

	/**
	 * ビューを更新します。
	 * <p>
	 *
	 * 検索条件が事前に設定されている場合、その条件にヒットするノードの一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全ノードを表示します。
	 */
	public void update() {
		this.composite.update(this.condition);
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
			service.refreshElements(NodeCopyAction.ID, null);
			service.refreshElements(NodeDeleteAction.ID, null);
			service.refreshElements(NodeModifyAction.ID, null);
			service.refreshElements(ProgramExecutionAction.ID, null);
			service.refreshElements(RefreshAction.ID, null);
			service.refreshElements(NodeFilterAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);

		}
	}
}
