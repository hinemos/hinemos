/*

Copyright (C) 2013 NTT DATA Corporation

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
import com.clustercontrol.calendar.action.GetCalendarPatternTableDefine;
import com.clustercontrol.calendar.composite.CalendarPatternComposite;
import com.clustercontrol.calendar.view.action.CalendarPatternCopyAction;
import com.clustercontrol.calendar.view.action.CalendarPatternDeleteAction;
import com.clustercontrol.calendar.view.action.CalendarPatternModifyAction;
import com.clustercontrol.calendar.view.action.ObjectPrivilegeCalendarPatternAction;
import com.clustercontrol.calendar.view.action.RefreshAction;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * カレンダ[カレンダパターン]ビュークラス<BR>
 *
 * @version 5.0.0
 * @since 4.1.0
 */
public class CalendarPatternView extends CommonViewPart implements ObjectPrivilegeTargetListView {
	public static final String ID = CalendarPatternView.class.getName();
	private CalendarPatternComposite calPatternComposite = null;
	private Composite calPatternParentComposite = null;

	/**
	 * Number of selected items
	 */
	private int selectedNum;

	/**
	 * コンストラクタ
	 */
	public CalendarPatternView() {
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
		calPatternParentComposite = parent;

		calPatternComposite = new CalendarPatternComposite(calPatternParentComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, calPatternComposite);

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

		Menu calPatternMenu = menuManager.createContextMenu(calPatternComposite.getTable());
		WidgetTestUtil.setTestId(this, null, calPatternMenu);
		calPatternComposite.getTable().setMenu(calPatternMenu);
		getSite().registerContextMenu( menuManager, this.calPatternComposite.getTableViewer() );
	}

	/**
	 * カレンダ[カレンダパターン]ビュー更新<BR>
	 */
	@Override
	public void update() {
		calPatternComposite.update();
	}

	public List<String> getSelectedIdList() {
		StructuredSelection selection = (StructuredSelection) this.calPatternComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();
		String id = null;
		List<String> idList = new ArrayList<String>();

		if (list != null) {
			for(Object obj : list) {
				@SuppressWarnings("unchecked")
				List<String> objList = (List<String>)obj;
				id = objList.get(GetCalendarPatternTableDefine.CAL_PATTERN_ID);
				idList.add(id);
			}
		}
		return idList;
	}

	public List<String> getSelectedManagerNameList() {
		StructuredSelection selection = (StructuredSelection) this.calPatternComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();
		String id = null;
		List<String> managerList = new ArrayList<String>();

		if (list != null) {
			for(Object obj : list) {
				@SuppressWarnings("unchecked")
				List<String> objList = (List<String>)obj;
				id = objList.get(GetCalendarPatternTableDefine.MANAGER_NAME);
				managerList.add(id);
			}
		}
		return managerList;
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) this.calPatternComposite.getTableViewer().getSelection();
		Object [] objs = selection.toArray();

		String managerName = null;
		String objectType = HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>)obj).get(GetCalendarPatternTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>)obj).get(GetCalendarPatternTableDefine.CAL_PATTERN_ID);
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) this.calPatternComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetCalendarPatternTableDefine.OWNER_ROLE);
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
	public void setEnabledAction(int num, ISelection selection) {
		this.selectedNum = num;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(CalendarPatternModifyAction.ID, null);
			service.refreshElements(CalendarPatternDeleteAction.ID, null);
			service.refreshElements(CalendarPatternCopyAction.ID, null);
			service.refreshElements(ObjectPrivilegeCalendarPatternAction.ID, null);
			service.refreshElements(RefreshAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}
}
