/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.view;

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
import com.clustercontrol.reporting.action.GetTemplateSetListTableDefine;
import com.clustercontrol.reporting.composite.TemplateSetListComposite;
import com.clustercontrol.reporting.util.ReportingUtil;
import com.clustercontrol.reporting.view.action.TemplateSetCopyAction;
import com.clustercontrol.reporting.view.action.TemplateSetDeleteAction;
import com.clustercontrol.reporting.view.action.TemplateSetModifyAction;
import com.clustercontrol.reporting.view.action.TemplateSetRefreshAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * レポーティング[テンプレートセット]ビュークラス<BR>
 * 
 * @version 5.0.a
 * @since 5.0.a
 */
public class ReportingTemplateSetView extends CommonViewPart implements ObjectPrivilegeTargetListView {

	/** レポーティング[テンプレートセット]ビューID */
	public static final String ID = ReportingTemplateSetView.class.getName();

	/** レポーティング[テンプレートセット]コンポジット */
	private TemplateSetListComposite composite = null;
	/**
	 * Number of selected items
	 */
	private int selectedNum;
	
	/**
	 * コンストラクタ
	 */
	public ReportingTemplateSetView() {
		super();
	}

	/**
	 * ViewPartへのコントロール作成処理<BR>
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		composite = new TemplateSetListComposite(parent, SWT.NONE);
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
						// レポーティング[テンプレートセット]ビューのインスタンスを取得
						IWorkbenchPage page = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage();
						IViewPart viewPart = page
								.findView(ReportingTemplateSetView.ID);
						// 選択アイテムを取得
						StructuredSelection selection = (StructuredSelection) event
								.getSelection();

						if (viewPart != null && selection != null) {
							ReportingTemplateSetView view = (ReportingTemplateSetView) viewPart
									.getAdapter(ReportingTemplateSetView.class);
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
			service.refreshElements(TemplateSetCopyAction.ID, null);
			service.refreshElements(TemplateSetDeleteAction.ID, null);
			service.refreshElements(TemplateSetModifyAction.ID, null);
			service.refreshElements(TemplateSetRefreshAction.ID, null);

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
				id = objList.get(GetTemplateSetListTableDefine.TEMPLATE_SET_ID);
				idList.add(id);
			}
		}
		return idList;
	}
	
	public Map<String, List<String>> getSelectedItem() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String templateSetId = null;
		String managerName = null;
		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		if (list != null) {
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				managerName = (String)objList.get(GetTemplateSetListTableDefine.MANAGER_NAME);
				if(map.get(managerName) == null){
					map.put(managerName, new ArrayList<String>());
				}
			}
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				templateSetId = (String)objList.get(GetTemplateSetListTableDefine.TEMPLATE_SET_ID);
				managerName = (String)objList.get(GetTemplateSetListTableDefine.MANAGER_NAME);

				map.get(managerName).add(templateSetId);
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
		String objectType = ReportingUtil.MODULE_CONSTANT_REPORTING;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>)obj).get(GetTemplateSetListTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>) obj)
					.get(GetTemplateSetListTableDefine.TEMPLATE_SET_ID);
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
			id = (String) list.get(GetTemplateSetListTableDefine.OWNER_ROLE);
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
