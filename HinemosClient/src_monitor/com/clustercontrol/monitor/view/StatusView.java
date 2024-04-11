/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view;

import java.util.ArrayList;
import java.util.List;

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
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;
import org.openapitools.client.model.StatusFilterBaseRequest;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DefaultLayoutSettingManager.ListLayout;
import com.clustercontrol.filtersetting.util.StatusFilterHelper;
import com.clustercontrol.monitor.composite.StatusListComposite;
import com.clustercontrol.monitor.composite.action.StatusListSelectionChangedListener;
import com.clustercontrol.monitor.preference.MonitorPreferencePage;
import com.clustercontrol.monitor.view.action.RefreshAction;
import com.clustercontrol.monitor.view.action.ScopeShowActionStatus;
import com.clustercontrol.monitor.view.action.StatusDeleteAction;
import com.clustercontrol.monitor.view.action.StatusModifyMonitorSettingAction;
import com.clustercontrol.monitor.view.action.StatusOpenJobHistoryAction;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.ScopeListBaseView;

/**
 * 監視[ステータス]ビュークラス<BR>
 *
 */
public class StatusView extends ScopeListBaseView {
	private static Log m_log = LogFactory.getLog(StatusView.class);

	/** 監視[ステータス]ビューID */
	public static final String ID = StatusView.class.getName();

	/** ステータス情報一覧コンポジット */
	private StatusListComposite tableComposite = null;

	/** フィルタ条件 */
	private StatusFilterBaseRequest filter = StatusFilterHelper.createDefaultFilter(null);
	
	/** フィルタが有効な場合は true */
	private boolean filterEnabled = false;

	/** 選択レコード数 */
	private int rowNum = 0;

	/** プラグインID */
	private String pluginId;

	private String selectedScopeLabel;

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ステータス情報一覧テーブルを作成・追加します。
	 * <p>
	 * <ol>
	 * <li>ステータス情報一覧テーブルを取得します。</li>
	 * <li>ステータス情報一覧テーブルに、ポップアップメニューを追加します。</li>
	 * <li>ビューを更新します。</li>
	 * <li>プレファレンスページの設定より、監視[ステータス]ビューの自動更新周期を取得し、このビューにセットします。</li>
	 * <li>プレファレンスページの設定より、監視[ステータス]ビューの自動更新フラグを取得し、このビューにセットします。</li>
	 * <li>ステータス情報一覧テーブルを返します。</li>
	 * </ol>
	 *
	 * @param parent 親のコンポジット
	 * @return ステータス情報一覧テーブルコンポジット
	 *
	 * @see com.clustercontrol.monitor.composite.StatusListComposite#StatusListComposite(Composite, int)
	 * @see #createContextMenu()
	 * @see #update()
	 */
	@Override
	protected Composite createListContents(Composite parent) {
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		ListLayout tableCompositeLayout = null;
		if (this.getDefaultViewLayout() != null) {
			tableCompositeLayout = this.getDefaultViewLayout().getViewItem(StatusListComposite.class.getSimpleName(), ListLayout.class);
		}
		this.tableComposite = new StatusListComposite(parent, SWT.NONE, tableCompositeLayout);
		WidgetTestUtil.setTestId(this, null, tableComposite);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.tableComposite.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		// 設定情報反映
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();

		this.setInterval(store.getInt(MonitorPreferencePage.P_STATUS_UPDATE_CYCLE));

		if (store.getBoolean(MonitorPreferencePage.P_STATUS_UPDATE_FLG)) {
			this.startAutoReload();
		}

		// ボタン（アクション）を制御するリスナーを登録
		this.tableComposite.getTableViewer().addSelectionChangedListener( new StatusListSelectionChangedListener() );

		return this.tableComposite;
	}

	/**
	 * スコープツリーでアイテムを選択した際に呼ばれ、ビューを更新します。
	 *
	 * @param item スコープツリーアイテム
	 *
	 * @see #update()
	 */
	@Override
	protected void doSelectTreeItem(FacilityTreeItemResponse item) {
		this.update(false);

		// 更新に失敗している場合は自動更新を停止する
		if (!this.tableComposite.isUpdateSuccess()) {
			this.stopAutoReload();
		}
	}

	/**
	 * ポップアップメニューを作成します。
	 */
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(tableComposite.getTable());
		WidgetTestUtil.setTestId(this, null, menu);
		tableComposite.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, this.tableComposite.getTableViewer() );
	}

	/**
	 * ビューを更新します。<BR>
	 */
	@Override
	public void update(boolean refreshFlag) {
		@SuppressWarnings("unchecked")
		List<FacilityTreeItemResponse> itemList = (List<FacilityTreeItemResponse>) getScopeTreeComposite().getSelectionList();
		FacilityTreeItemResponse top = (FacilityTreeItemResponse) getScopeTreeComposite().getTree().getItem(0).getData();

		// 何も選択していない場合は、最上位を選択したこととする。
		if (itemList == null || itemList.size() == 0) {
			itemList = new ArrayList<FacilityTreeItemResponse>();
			itemList.add(top);
		}

		tableComposite.resetDisp();
		selectedScopeLabel = "";

		for (FacilityTreeItemResponse item : itemList) {
			// Select root if nothing selected
			if (item == null && 0 < getScopeTreeComposite().getTree().getItemCount()) { // これありうる？
				item = top;
			}
			if (item == null) continue;

			// 検索対象のマネージャとファシリティIDを決定
			List<String> managerList = new ArrayList<String>();
			// ファシリティIDがnullで検索すると現在のツリーに存在しないノードも対象になってしまうため、
			// ファシリティIDに"_ROOT_"を設定する。
			String facilityId = ReservedFacilityIdConstant.ROOT_SCOPE;
			FacilityInfoResponse itemData = item.getData();
			if (itemData.getFacilityType() == FacilityTypeEnum.COMPOSITE) {
				managerList = RestConnectManager.getActiveManagerNameList();
			} else if (itemData.getFacilityType() == FacilityTypeEnum.MANAGER) {
				managerList.add(itemData.getFacilityId());
			} else {
				managerList.add(ScopePropertyUtil.getManager(item).getData().getFacilityId());
				facilityId = item.getData().getFacilityId();
			}
			m_log.debug("update: facilityId=" + facilityId + ", managerList=" + managerList);

			// パス表示
			if (facilityId != null) {
				FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
				selectedScopeLabel += path.getPath(item) + " ";
			}

			// 検索
			if (filterEnabled) {
				StatusFilterBaseRequest filter = StatusFilterHelper.duplicate(this.filter);
				filter.setFacilityId(facilityId);
				tableComposite.setDisp(filter, managerList);
			} else {
				tableComposite.setDisp(StatusFilterHelper.createDefaultFilter(facilityId), managerList);
			}
		}

		tableComposite.updateDisp(refreshFlag);

		String label = Messages.getString(filterEnabled ? "filtered.list" : "scope") + ": ";
		if (selectedScopeLabel.length() > 0) {
			label += selectedScopeLabel;
		}
		setPathLabel(label);
		m_log.debug("update() : label=" + label);
	}

	public StatusFilterBaseRequest getFilter() {
		return filter;
	}

	public void setFilter(StatusFilterBaseRequest filter) {
		this.filter = filter;
	}

	public boolean isFilterEnabled() {
		return filterEnabled;
	}

	public void setFilterEnabled(boolean filterEnabled) {
		this.filterEnabled = filterEnabled;
	}

	public String getSelectedScopeLabel() {
		return selectedScopeLabel;
	}

	/**
	 * 検索条件とビューアクションを初期化します。
	 */
	@Override
	public void dispose() {
		super.dispose();
	}

	/**
	 * 選択レコード数を返します。
	 * @return rowNum
	 */
	public int getRowNum() {
		return this.rowNum;
	}

	/** プラグインIDを返します。
	 * @return pluginId
	 */
	public String getPluginId() {
		return this.pluginId;
	}

	public void initButton() {
		setEnabledAction(0, null, null);
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param num 選択イベント数
	 * @param selection ボタン（アクション）を有効にするための情報
	 */
	public void setEnabledAction(int num, String pluginId, ISelection selection) {
		this.rowNum = num;
		this.pluginId = pluginId;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(StatusDeleteAction.ID, null);
			service.refreshElements(StatusModifyMonitorSettingAction.ID, null);
			service.refreshElements(StatusOpenJobHistoryAction.ID, null);
			service.refreshElements(StatusModifyMonitorSettingAction.ID, null);
			service.refreshElements(ScopeShowActionStatus.ID, null);
			service.refreshElements(RefreshAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}
	
	@Override
	public boolean isDefaultLayoutView() {
		return true;
	}

	/**
	 * 更新成功可否を返します。
	 * @return 更新成功可否
	 */
	public boolean isUpdateSuccess() {
		return this.tableComposite.isUpdateSuccess();
	}
}
