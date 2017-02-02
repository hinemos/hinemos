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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.Property;
import com.clustercontrol.jobmanagement.composite.HistoryComposite;
import com.clustercontrol.jobmanagement.composite.action.JobHistorySelectionChangedListener;
import com.clustercontrol.jobmanagement.preference.JobManagementPreferencePage;
import com.clustercontrol.jobmanagement.view.action.HistoryFilterAction;
import com.clustercontrol.jobmanagement.view.action.HistoryRefreshAction;
import com.clustercontrol.jobmanagement.view.action.StartJobHistoryAction;
import com.clustercontrol.jobmanagement.view.action.StopJobHistoryAction;
import com.clustercontrol.view.AutoUpdateView;

/**
 * ジョブ[履歴]ビュークラスです。
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class JobHistoryView extends AutoUpdateView {

	// ログ
	private static Log m_log = LogFactory.getLog( JobHistoryView.class );

	/** ビューID */
	public static final String ID = JobHistoryView.class.getName();
	/** ジョブ[履歴]ビュー用のコンポジット */
	private HistoryComposite m_history = null;
	/** フィルタ条件 */
	private Property m_condition = null;

	/**
	 * Number of selected items
	 */
	private int selectedNum;

	/**
	 * コンストラクタ
	 */
	public JobHistoryView() {
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
	 * @see com.clustercontrol.view.AutoUpdateView#setInterval(int)
	 * @see com.clustercontrol.view.AutoUpdateView#startAutoReload()
	 * @see #createContextMenu()
	 * @see #update()
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_history = new HistoryComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_history);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_history.setLayoutData(gridData);
		m_history.setView(this);

		//ポップアップメニュー作成
		createContextMenu();

		// ボタン（アクション）を制御するリスナーを登録
		this.m_history.getTableViewer().addSelectionChangedListener(
				new JobHistorySelectionChangedListener());

		//ビューを更新
		this.update(false);

		// 設定情報反映
		IPreferenceStore store = ClusterControlPlugin.getDefault()
				.getPreferenceStore();

		this.setInterval(store
				.getInt(JobManagementPreferencePage.P_HISTORY_UPDATE_CYCLE));

		if (store.getBoolean(JobManagementPreferencePage.P_HISTORY_UPDATE_FLG)) {
			this.startAutoReload();
		}
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

		Menu menu = menuManager.createContextMenu(m_history.getTable());
		WidgetTestUtil.setTestId(this, null, menu);
		m_history.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, this.m_history.getTableViewer() );
	}

	/**
	 * フィルタ条件を返します。
	 *
	 * @return フィルタ条件
	 */
	public Property getFilterCondition() {
		return m_condition;
	}

	/**
	 * フィルタ条件を設定します。
	 *
	 * @param condition フィルタ条件
	 */
	public void setFilterCondition(Property condition) {
		m_condition = condition;
	}

	/**
	 * ビューを更新します。
	 *
	 * @see com.clustercontrol.jobmanagement.composite.HistoryComposite#update()
	 * @see com.clustercontrol.jobmanagement.composite.HistoryComposite#update(Property)
	 */
	@Override
	public void update(boolean refreshFlag) {
		try {
			if (m_condition == null) {
				m_history.update();
			} else {
				m_history.update(m_condition);
			}
		} catch (Exception e) {
			m_log.warn("update(), " + e.getMessage(), e);
		}
	}

	/**
	 * ジョブ[履歴]ビュー用のコンポジットを返します。
	 *
	 * @return ジョブ[履歴]ビュー用のコンポジット
	 */
	public HistoryComposite getComposite() {
		return m_history;
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
			service.refreshElements(StartJobHistoryAction.ID, null);
			service.refreshElements(StopJobHistoryAction.ID, null);
			service.refreshElements(HistoryFilterAction.ID, null);
			service.refreshElements(HistoryRefreshAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}
}
