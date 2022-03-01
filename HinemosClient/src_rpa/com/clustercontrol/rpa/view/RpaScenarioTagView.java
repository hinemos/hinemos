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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.rpa.action.GetRpaScenarioTagListTableDefine;
import com.clustercontrol.rpa.composite.RpaScenarioTagListComposite;
import com.clustercontrol.rpa.view.action.RpaScenarioTagCopyAction;
import com.clustercontrol.rpa.view.action.RpaScenarioTagDeleteAction;
import com.clustercontrol.rpa.view.action.RpaScenarioTagModifyAction;
import com.clustercontrol.rpa.view.action.RpaScenarioTagRefreshAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * RPA設定[シナリオタグ]ビュークラス
 */
public class RpaScenarioTagView extends CommonViewPart implements ObjectPrivilegeTargetListView {

	/** RPA設定[シナリオタグ]ビューID */
	public static final String ID = RpaScenarioTagView.class.getName();

	/** RPA設定[シナリオタグ]コンポジット */
	private RpaScenarioTagListComposite composite = null;
	/**
	 * Number of selected items
	 */
	private int selectedNum;
	
	/**
	 * コンストラクタ
	 */
	public RpaScenarioTagView() {
		super();
	}

	/**
	 * ViewPartへのコントロール作成処理
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		composite = new RpaScenarioTagListComposite(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.composite.setLayoutData(gridData);

		// ポップアップメニュー作成
		createContextMenu();

		this.composite.getTableViewer().addSelectionChangedListener(
				new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						// RPA設定[シナリオタグ]ビューのインスタンスを取得
						IWorkbenchPage page = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage();
						IViewPart viewPart = page
								.findView(RpaScenarioTagView.ID);
						// 選択アイテムを取得
						StructuredSelection selection = (StructuredSelection) event
								.getSelection();

						if (viewPart != null && selection != null) {
							RpaScenarioTagView view = (RpaScenarioTagView) viewPart
									.getAdapter(RpaScenarioTagView.class);
							// ビューのボタン（アクション）の使用可/不可を設定する
							view.setEnabledAction(selection.size(),
									event.getSelection());
						}
					}
				});

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
		getSite().registerContextMenu(menuManager, this.composite.getTableViewer() );
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
	 * ビューを更新します。
	 */
	@Override
	public void update() {
		this.composite.update();
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 * 
	 * @param num
	 *            選択イベント数
	 * @param selection
	 *            ボタン（アクション）を有効にするための情報
	 */
	public void setEnabledAction(int num, ISelection selection) {
		
		this.selectedNum = num;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(RpaScenarioTagCopyAction.ID, null);
			service.refreshElements(RpaScenarioTagDeleteAction.ID, null);
			service.refreshElements(RpaScenarioTagModifyAction.ID, null);
			service.refreshElements(RpaScenarioTagRefreshAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}

	public List<String> getSelectedIdList() {
		StructuredSelection selection = (StructuredSelection) this.composite
				.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();
		
		
		String id = null;
		List<String> idList = new ArrayList<String>();
		if (list != null) {
			for(Object obj : list) {
				@SuppressWarnings("unchecked")
				List<String> objList = (List<String>)obj;
				id = objList.get(GetRpaScenarioTagListTableDefine.TAG_ID);
				idList.add(id);
			}
		}
		return idList;
	}
	
	public Map<String, List<String>> getSelectedItem() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String tagId = null;
		String managerName = null;
		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		if (list != null) {
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				managerName = (String)objList.get(GetRpaScenarioTagListTableDefine.MANAGER_NAME);
				if(map.get(managerName) == null){
					map.put(managerName, new ArrayList<String>());
				}
				tagId = (String)objList.get(GetRpaScenarioTagListTableDefine.TAG_ID);
				map.get(managerName).add(tagId);
			}
		}
		return map;
	}
	
	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) this.composite
				.getTableViewer().getSelection();
		Object[] objs = selection.toArray();

		String managerName = null;
		String objectType = HinemosModuleConstant.RPA_SCENARIO_TAG;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>)obj).get(GetRpaScenarioTagListTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>) obj)
					.get(GetRpaScenarioTagListTableDefine.TAG_ID);
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) this.composite
				.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetRpaScenarioTagListTableDefine.OWNER_ROLE);
		}
		return id;
	}
	
	/**
	 * Get the number of selected items
	 * @return
	 */
	public int getSelectedNum(){
		return this.selectedNum;
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}

}
