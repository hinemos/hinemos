/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import org.openapitools.client.model.JobHistoryFilterBaseRequest;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.filtersetting.util.FilterSettingManagerNameUpdater;
import com.clustercontrol.filtersetting.util.JobHistoryFilterHelper;
import com.clustercontrol.jobmanagement.composite.HistoryComposite;
import com.clustercontrol.jobmanagement.composite.action.JobHistorySelectionChangedListener;
import com.clustercontrol.jobmanagement.preference.JobManagementPreferencePage;
import com.clustercontrol.jobmanagement.view.action.HistoryFilterAction;
import com.clustercontrol.jobmanagement.view.action.HistoryRefreshAction;
import com.clustercontrol.jobmanagement.view.action.StartJobHistoryAction;
import com.clustercontrol.jobmanagement.view.action.StopJobHistoryAction;
import com.clustercontrol.util.ManagerTag;
import com.clustercontrol.util.WidgetTestUtil;
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
	private JobHistoryFilterBaseRequest filter = JobHistoryFilterHelper.createDefaultFilter();
	/** フィルタ true:On/false:Off */
	private boolean filterEnabled = false;

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

	@Override
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

		if (store.getBoolean(JobManagementPreferencePage.P_HISTORY_UPDATE_FLG) && m_history.isUpdateSuccess()) {
			this.startAutoReload();
		} else {
			this.stopAutoReload();
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
	 * フィルタ設定を返します。
	 * 
	 * @return
	 * 		nullを返すことはありませんが、フィールド {@link ManagerTag#managerName}
	 * 		は「全接続マネージャ」を意味するnullである可能性があります。
	 */
	public ManagerTag<JobHistoryFilterBaseRequest> getFilter() {
		return new ManagerTag<JobHistoryFilterBaseRequest>(
				FilterSettingManagerNameUpdater.getInstance().getFilterManagerName(ID), filter);
	}

	/**
	 * フィルタ条件を設定します。
	 *
	 * @param managerName 検索対象のマネージャ名、nullなら全接続マネージャ 
	 * @param filter フィルタ条件
	 */
	public void setFilter(String managerName, JobHistoryFilterBaseRequest filter) {
		FilterSettingManagerNameUpdater.getInstance().setFilterManagerName(ID, managerName);
		this.filter = filter;
	}

	public boolean isFilterEnabled() {
		return filterEnabled;
	}

	public void setFilterEnabled(boolean filterEnabled) {
		this.filterEnabled = filterEnabled;

		if (!filterEnabled) {
			// フィルタを無効にするため、ジョブ[履歴]ビューをリセットする
			m_history.reset();
		}
	}

	/**
	 * ビューを更新します。
	 *
	 */
	@Override
	public void update(boolean refreshFlag) {
		try {
			if (!filterEnabled) {
				m_history.update();
			} else {
				m_history.update(FilterSettingManagerNameUpdater.getInstance().getFilterManagerName(ID), filter);
			}
		} catch (Exception e) {
			m_log.warn("update(), " + e.getMessage(), e);
		}

		// 更新に失敗している場合は自動更新を停止する
		if (!m_history.isUpdateSuccess()) {
			this.stopAutoReload();
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
	
	/**
	 * 更新成功可否を返します。
	 * @return 更新成功可否
	 */
	public boolean isUpdateSuccess() {
		return this.m_history.isUpdateSuccess();
	}
}
