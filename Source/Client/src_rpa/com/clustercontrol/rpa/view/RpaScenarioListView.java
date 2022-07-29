/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.view;

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

import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.rpa.action.GetRpaScenarioListTableDefine;
import com.clustercontrol.rpa.bean.RpaScenarioFilterConstant;
import com.clustercontrol.rpa.composite.RpaScenarioListComposite;
import com.clustercontrol.rpa.composite.action.RpaScenarioListSelectionChangedListener;
import com.clustercontrol.rpa.view.action.ObjectPrivilegeRpaScenarioListAction;
import com.clustercontrol.rpa.view.action.RpaScenarioCopyAction;
import com.clustercontrol.rpa.view.action.RpaScenarioDeleteAction;
import com.clustercontrol.rpa.view.action.RpaScenarioFilterAction;
import com.clustercontrol.rpa.view.action.RpaScenarioModifyAction;
import com.clustercontrol.rpa.view.action.RpaScenarioModifyExecNodeAction;
import com.clustercontrol.rpa.view.action.RpaScenarioRefreshAction;
import com.clustercontrol.util.FilterPropertyUpdater;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * RPAシナリオ実績[シナリオ一覧]ビュークラス<BR>
 */
public class RpaScenarioListView extends CommonViewPart implements ObjectPrivilegeTargetListView {

	/** RPAシナリオ実績[シナリオ一覧]ビューID */
	public static final String ID = RpaScenarioListView.class.getName();

	/** RPAシナリオ実績[シナリオ一覧]コンポジット */
	private RpaScenarioListComposite composite = null;

	/** 検索条件 */
	private Property condition = null;

	/** 選択レコード数 */
	private int rowNum = 0;

	/**
	 * コンストラクタ
	 */
	public RpaScenarioListView() {
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

		composite = new RpaScenarioListComposite(parent, SWT.NONE);
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
				new RpaScenarioListSelectionChangedListener());

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
	 * 検索条件にヒットしたシナリオの一覧を表示します。
	 * <p>
	 *
	 * conditionがnullの場合、全シナリオを表示します。
	 *
	 * @param condition
	 *            検索条件
	 */
	public void update(Property condition) {
		FilterPropertyUpdater.getInstance().addFilterProperty(getClass(), condition,
				RpaScenarioFilterConstant.MANAGER);
		this.condition = condition;

		this.update();
	}

	/**
	 * ビューを更新します。
	 * <p>
	 *
	 * 検索条件が事前に設定されている場合、その条件にヒットするシナリオの一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全シナリオを表示します。
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
	public void setEnabledAction(int num, String selectScenarioId, ISelection selection) {
		this.rowNum = num;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(RpaScenarioModifyAction.ID, null);
			service.refreshElements(RpaScenarioCopyAction.ID, null);
			service.refreshElements(RpaScenarioDeleteAction.ID, null);
			service.refreshElements(RpaScenarioRefreshAction.ID, null);
			service.refreshElements(RpaScenarioFilterAction.ID, null);
			service.refreshElements(ObjectPrivilegeRpaScenarioListAction.ID, null);
			service.refreshElements(RpaScenarioModifyExecNodeAction.ID, null);

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
		String objectType = HinemosModuleConstant.RPA_SCENARIO;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>)obj).get(GetRpaScenarioListTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>)obj).get(GetRpaScenarioListTableDefine.SCENARIO_ID);
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
			id = (String) list.get(GetRpaScenarioListTableDefine.OWNER_ROLE_ID);
		}
		return id;
	}
}
