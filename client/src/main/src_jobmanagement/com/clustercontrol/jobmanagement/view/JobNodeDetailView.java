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

package com.clustercontrol.jobmanagement.view;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.jobmanagement.composite.NodeDetailComposite;
import com.clustercontrol.jobmanagement.view.action.StartJobNodeDetailAction;
import com.clustercontrol.jobmanagement.view.action.StopJobNodeDetailAction;
import com.clustercontrol.view.CommonViewPart;

/**
 * ジョブ[ノード詳細]ビュークラスです。
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class JobNodeDetailView extends CommonViewPart {
	/** ビューID */
	public static final String ID = JobNodeDetailView.class.getName();
	/** ジョブ[ノード詳細]ビュー用のコンポジット */
	private NodeDetailComposite m_nodeDetail = null;

	/**
	 * Number of selected items
	 */
	private int selectedNum;

	private String orgViewName = null;

	/**
	 * コンストラクタ
	 */
	public JobNodeDetailView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ビューを構築します。
	 *
	 * @param parent 親コンポジット
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 * @see #createContextMenu()
	 * @see #update(String, String)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_nodeDetail = new NodeDetailComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_nodeDetail);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_nodeDetail.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		//ビューの更新
		this.update(null, null, null, null);
		orgViewName = this.getPartName();
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

		Menu menu = menuManager.createContextMenu(m_nodeDetail.getTable());
		WidgetTestUtil.setTestId(this, null, menu);
		m_nodeDetail.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, this.m_nodeDetail.getTableViewer() );
	}

	/**
	 * ビューを更新します。
	 *
	 * @param managerName マネージャ名
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobuId ジョブID
	 *
	 * @see com.clustercontrol.jobmanagement.composite.NodeDetailComposite#update(String, String)
	 */
	public void update(String managerName, String sessionId, String jobunitId, String jobId) {
		if(managerName == null || managerName.equals("")) {
			return;
		}
		m_nodeDetail.update(managerName, sessionId, jobunitId, jobId);
		String viewName = orgViewName + "(" + managerName + ")";
		setPartName(viewName);
	}

	/**
	 * ジョブ[ノード詳細]ビュー用のコンポジットを返します。
	 *
	 * @return ジョブ[ノード詳細]ビュー用のコンポジット
	 */
	public NodeDetailComposite getComposite() {
		return m_nodeDetail;
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
			service.refreshElements(StartJobNodeDetailAction.ID, null);
			service.refreshElements(StopJobNodeDetailAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}
}
