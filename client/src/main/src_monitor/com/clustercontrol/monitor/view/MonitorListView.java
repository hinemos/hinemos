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

package com.clustercontrol.monitor.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.composite.MonitorListComposite;
import com.clustercontrol.monitor.composite.action.MonitorListSelectionChangedListener;
import com.clustercontrol.monitor.run.action.GetMonitorListTableDefine;
import com.clustercontrol.monitor.view.action.CollectorDisableAction;
import com.clustercontrol.monitor.view.action.CollectorEnableAction;
import com.clustercontrol.monitor.view.action.MonitorCopyAction;
import com.clustercontrol.monitor.view.action.MonitorDeleteAction;
import com.clustercontrol.monitor.view.action.MonitorDisableAction;
import com.clustercontrol.monitor.view.action.MonitorEnableAction;
import com.clustercontrol.monitor.view.action.MonitorFilterAction;
import com.clustercontrol.monitor.view.action.MonitorModifyAction;
import com.clustercontrol.monitor.view.action.MonitorRefreshAction;
import com.clustercontrol.monitor.view.action.ObjectPrivilegeMonitorListAction;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * 監視[一覧]ビュークラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class MonitorListView extends CommonViewPart implements ObjectPrivilegeTargetListView {

	/** 監視[一覧]ビューID */
	public static final String ID = MonitorListView.class.getName();

	/** 監視設定一覧コンポジット */
	private MonitorListComposite composite = null;

	/** 検索条件 */
	private Property condition = null;

	/** 選択レコード数 */
	private int rowNum = 0;

	/**
	 * コンストラクタ
	 */
	public MonitorListView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ViewPartへのコントロール作成処理<BR>
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		composite = new MonitorListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, composite);
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
				new MonitorListSelectionChangedListener());

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

		Menu menu = menuManager.createContextMenu(composite.getTable());
		WidgetTestUtil.setTestId(this, null, menu);
		composite.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, this.composite.getTableViewer() );
	}

	/**
	 * 追加コンポジットを返します。
	 *
	 * @return 追加コンポジット
	 */
	public Composite getListComposite() {
		return this.composite;
	}

	/**
	 * 検索条件にヒットした監視設定の一覧を表示します。
	 * <p>
	 *
	 * conditionがnullの場合、全監視設定を表示します。
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
	 * 検索条件が事前に設定されている場合、その条件にヒットする監視設定の一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全監視設定を表示します。
	 */
	@Override
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
			service.refreshElements(ObjectPrivilegeMonitorListAction.ID, null);
			service.refreshElements(MonitorCopyAction.ID, null);
			service.refreshElements(MonitorDeleteAction.ID, null);
			service.refreshElements(MonitorModifyAction.ID, null);
			service.refreshElements(MonitorDisableAction.ID, null);
			service.refreshElements(MonitorEnableAction.ID, null);
			service.refreshElements(CollectorDisableAction.ID, null);
			service.refreshElements(CollectorEnableAction.ID, null);
			service.refreshElements(MonitorRefreshAction.ID, null);
			service.refreshElements(MonitorFilterAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();
		Object [] objs = selection.toArray();

		String managerName = null;
		String objectType = HinemosModuleConstant.MONITOR;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>)obj).get(GetMonitorListTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>)obj).get(GetMonitorListTableDefine.MONITOR_ID);
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetMonitorListTableDefine.OWNER_ROLE);
		}
		return id;
	}
}
