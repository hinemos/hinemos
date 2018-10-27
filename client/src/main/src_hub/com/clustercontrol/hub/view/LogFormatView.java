/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.hub.action.GetLogFormatTableDefine;
import com.clustercontrol.hub.composite.LogFormatComposite;
import com.clustercontrol.hub.view.action.LogFormatCopyAction;
import com.clustercontrol.hub.view.action.LogFormatDeleteAction;
import com.clustercontrol.hub.view.action.LogFormatModifyAction;
import com.clustercontrol.hub.view.action.ObjectPrivilegeLogFormatAction;
import com.clustercontrol.hub.view.action.RefreshHubAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * ログフォーマットビュークラス<BR>
 *
 */
public class LogFormatView extends CommonViewPart implements ObjectPrivilegeTargetListView {
	public static final String ID = LogFormatView.class.getName();
	private LogFormatComposite logFormatComposite = null;
	private Composite logFormatViewParentComposite = null;

	/**
	 * Number of selected items
	 */
	private int selectedNum;

	/**
	 * コンストラクタ
	 */
	public LogFormatView() {
		super();
	}

	/**
	 * ViewPartへのコントロール作成処理<BR>
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		logFormatViewParentComposite = parent;

		logFormatComposite = new LogFormatComposite(logFormatViewParentComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, logFormatComposite);

		//ポップアップメニュー作成
		createContextMenu();

		//ビューを更新
		this.update();
	}

	/**
	 * ポップアップメニュー作成<BR>
	 *
	 *
	 */
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu ( this.logFormatComposite.getTable() );
		WidgetTestUtil.setTestId(this, null, menu);
		this.logFormatComposite.getTable().setMenu(menu);

		getSite().registerContextMenu( menuManager, this.logFormatComposite.getTableViewer() );
	}

	/**
	 * ログフォーマットビュー更新<BR>
	 */
	@Override
	public void update() {
		logFormatComposite.update();
	}

	public List<String> getSelectedIdList() {
		StructuredSelection selection = (StructuredSelection) this.logFormatComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String id = null;
		List<String> idList = new ArrayList<String>();
		if (list != null) {
			for(Object obj : list) {
				@SuppressWarnings("unchecked")
				List<String> objList = (List<String>)obj;
				id = objList.get(GetLogFormatTableDefine.FORMAT_ID);
				idList.add(id);
			}
		}
		return idList;
	}

	public List<String> getSelectedManagerNameList() {
		StructuredSelection selection = (StructuredSelection) this.logFormatComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String managerName = null;
		List<String> managerList = new ArrayList<String>();
		if (list != null) {
			for(Object obj : list) {
				@SuppressWarnings("unchecked")
				List<String> objList = (List<String>)obj;
				managerName = objList.get(GetLogFormatTableDefine.MANAGER_NAME);
				managerList.add(managerName);
			}
		}
		return managerList;
	}

	public Map<String, List<String>> getSelectedItem() {
		StructuredSelection selection = (StructuredSelection) this.logFormatComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String formatId = null;
		String managerName = null;
		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		if (list != null) {
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				managerName = (String)objList.get(GetLogFormatTableDefine.MANAGER_NAME);
				if(map.get(managerName) == null){
					map.put(managerName, new ArrayList<String>());
				}
			}
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				formatId = (String)objList.get(GetLogFormatTableDefine.FORMAT_ID);
				managerName = (String)objList.get(GetLogFormatTableDefine.MANAGER_NAME);

				map.get(managerName).add(formatId);
			}
		}
		return map;
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) this.logFormatComposite.getTableViewer().getSelection();
		Object [] objs = selection.toArray();

		String managerName = null;
		String objectType = HinemosModuleConstant.HUB_LOGFORMAT;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>)obj).get(GetLogFormatTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>)obj).get(GetLogFormatTableDefine.FORMAT_ID);
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) this.logFormatComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetLogFormatTableDefine.OWNER_ROLE);
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

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param num 選択イベント数
	 * @param selection ボタン（アクション）を有効にするための情報
	 */
	public void setEnabledAction( int num, ISelection selection ){
		this.selectedNum = num;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(LogFormatModifyAction.ID, null);
			service.refreshElements(LogFormatDeleteAction.ID, null);
			service.refreshElements(LogFormatCopyAction.ID, null);
			service.refreshElements(ObjectPrivilegeLogFormatAction.ID, null);
			service.refreshElements(RefreshHubAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}
}
