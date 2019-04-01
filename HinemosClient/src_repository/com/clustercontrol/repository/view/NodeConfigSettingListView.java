/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import com.clustercontrol.repository.composite.NodeConfigSettingInfoListComposite;
import com.clustercontrol.repository.view.action.NodeConfigDisableAction;
import com.clustercontrol.repository.view.action.NodeConfigEnableAction;
import com.clustercontrol.repository.view.action.NodeConfigRunAction;
import com.clustercontrol.repository.view.action.NodeConfigSettingCopyAction;
import com.clustercontrol.repository.view.action.NodeConfigSettingDeleteAction;
import com.clustercontrol.repository.view.action.NodeConfigSettingModifyAction;
import com.clustercontrol.repository.view.action.ProgramExecutionAction;
import com.clustercontrol.repository.view.action.RefreshAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;

/**
 * 構成情報収集一覧ビュークラス<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeConfigSettingListView extends CommonViewPart {
	public static final String ID = NodeConfigSettingListView.class.getName();

	// ----- instance フィールド ----- //

	/** ノード一覧コンポジット */
	private NodeConfigSettingInfoListComposite composite = null;

	/** 検索条件 */
	private Property condition = null;

	/** 選択レコード数 */
	private int rowNum = 0;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 */
	public NodeConfigSettingListView() {
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

		this.composite = new NodeConfigSettingInfoListComposite(parent, SWT.NONE);
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
		//this.composite.getTableViewer().addSelectionChangedListener(
		//		new NodeListSelectionChangedListener());

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
	public NodeConfigSettingInfoListComposite getComposite() {
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
			service.refreshElements(NodeConfigSettingCopyAction.ID, null);
			service.refreshElements(NodeConfigSettingDeleteAction.ID, null);
			service.refreshElements(NodeConfigSettingModifyAction.ID, null);
			service.refreshElements(NodeConfigEnableAction.ID, null);
			service.refreshElements(NodeConfigDisableAction.ID, null);
			service.refreshElements(NodeConfigRunAction.ID, null);
			service.refreshElements(ProgramExecutionAction.ID, null);
			service.refreshElements(RefreshAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);

		}
	}
}
