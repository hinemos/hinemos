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

package com.clustercontrol.calendar.view;

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

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.calendar.action.GetCalendarListTableDefine;
import com.clustercontrol.calendar.composite.CalendarListComposite;
import com.clustercontrol.calendar.view.action.CalendarCopyAction;
import com.clustercontrol.calendar.view.action.CalendarDeleteAction;
import com.clustercontrol.calendar.view.action.CalendarModifyAction;
import com.clustercontrol.calendar.view.action.ObjectPrivilegeCalendarListAction;
import com.clustercontrol.calendar.view.action.RefreshAction;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * カレンダ一覧ビュークラス<BR>
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class CalendarListView extends CommonViewPart implements ObjectPrivilegeTargetListView {
	public static final String ID = CalendarListView.class.getName();
	private CalendarListComposite calListComposite = null;
	private Composite calListViewParentComposite = null;

	/**
	 * Number of selected items
	 */
	private int selectedNum;

	/**
	 * コンストラクタ
	 */
	public CalendarListView() {
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
		calListViewParentComposite = parent;
		calListComposite = new CalendarListComposite(calListViewParentComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, calListComposite);

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

		Menu menu = menuManager.createContextMenu ( this.calListComposite.getTable() );
		WidgetTestUtil.setTestId(this, null, menu);
		this.calListComposite.getTable().setMenu(menu);

		getSite().registerContextMenu( menuManager, this.calListComposite.getTableViewer() );
	}

	/**
	 * カレンダ一覧ビュー更新<BR>
	 */
	@Override
	public void update() {
		calListComposite.update();
	}

	public List<String> getSelectedIdList() {
		StructuredSelection selection = (StructuredSelection) this.calListComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String id = null;
		List<String> idList = new ArrayList<String>();
		if (list != null) {
			for(Object obj : list) {
				@SuppressWarnings("unchecked")
				List<String> objList = (List<String>)obj;
				id = objList.get(GetCalendarListTableDefine.CALENDAR_ID);
				idList.add(id);
			}
		}
		return idList;
	}

	public List<String> getSelectedManagerNameList() {
		StructuredSelection selection = (StructuredSelection) this.calListComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String managerName = null;
		List<String> managerList = new ArrayList<String>();
		if (list != null) {
			for(Object obj : list) {
				@SuppressWarnings("unchecked")
				List<String> objList = (List<String>)obj;
				managerName = objList.get(GetCalendarListTableDefine.MANAGER_NAME);
				managerList.add(managerName);
			}
		}
		return managerList;
	}

	public Map<String, List<String>> getSelectedItem() {
		StructuredSelection selection = (StructuredSelection) this.calListComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String calId = null;
		String managerName = null;
		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		if (list != null) {
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				managerName = (String)objList.get(GetCalendarListTableDefine.MANAGER_NAME);
				if(map.get(managerName) == null){
					map.put(managerName, new ArrayList<String>());
				}
			}
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				calId = (String)objList.get(GetCalendarListTableDefine.CALENDAR_ID);
				managerName = (String)objList.get(GetCalendarListTableDefine.MANAGER_NAME);

				map.get(managerName).add(calId);
			}
		}
		return map;
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) this.calListComposite.getTableViewer().getSelection();
		Object [] objs = selection.toArray();

		String managerName = null;
		String objectType = HinemosModuleConstant.PLATFORM_CALENDAR;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>)obj).get(GetCalendarListTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>)obj).get(GetCalendarListTableDefine.CALENDAR_ID);
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) this.calListComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetCalendarListTableDefine.OWNER_ROLE);
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
			service.refreshElements(CalendarModifyAction.ID, null);
			service.refreshElements(CalendarDeleteAction.ID, null);
			service.refreshElements(CalendarCopyAction.ID, null);
			service.refreshElements(ObjectPrivilegeCalendarListAction.ID, null);
			service.refreshElements(RefreshAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}
}
