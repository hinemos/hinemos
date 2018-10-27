/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.nodemap.composite.ScopeComposite;
import com.clustercontrol.nodemap.view.action.OpenNodeMapAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;

/**
 * スコープビュー
 * @since 1.0.0
 */
public class ScopeTreeView extends CommonViewPart {
	public static final String ID = "com.clustercontrol.nodemap.view.ScopeTreeView";
	ScopeComposite m_composite;

	public ScopeTreeView(){
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);

		m_composite = new ScopeComposite(parent, SWT.NONE, false, true, true);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		m_composite.setLayoutData(gridData);

		createContextMenu();
	}

	public ScopeComposite getScopeComposite() {
		return m_composite;
	}

	/**
	 * ポップアップメニュー作成
	 * 
	 * 
	 */
	protected void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		Menu treeMenu = menuManager.createContextMenu(m_composite.getTree());
		WidgetTestUtil.setTestId(this, null, treeMenu);
		m_composite.getTree().setMenu(treeMenu);
		getSite().registerContextMenu( menuManager, this.m_composite.getTreeViewer() );
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 * 
	 * @param builtin 組み込みスコープかどうかのフラグ
	 * @param type スコープとノードの種別
	 * @param selection ボタン（アクション）を有効にするための情報
	 * 
	 * @see com.clustercontrol.bean.FacilityConstant
	 */
	public void setEnabledAction() {
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		service.refreshElements(OpenNodeMapAction.ID, null);
		
		// Update ToolBar after elements refreshed
		// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
		getViewSite().getActionBars().updateActionBars();
		getViewSite().getActionBars().getToolBarManager().update(false);
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}
}