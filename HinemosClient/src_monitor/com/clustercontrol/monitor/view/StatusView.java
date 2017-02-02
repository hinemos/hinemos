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

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.composite.StatusListComposite;
import com.clustercontrol.monitor.composite.action.StatusListSelectionChangedListener;
import com.clustercontrol.monitor.preference.MonitorPreferencePage;
import com.clustercontrol.monitor.view.action.RefreshAction;
import com.clustercontrol.monitor.view.action.ScopeShowActionStatus;
import com.clustercontrol.monitor.view.action.StatusDeleteAction;
import com.clustercontrol.monitor.view.action.StatusModifyMonitorSettingAction;
import com.clustercontrol.monitor.view.action.StatusOpenJobHistoryAction;
import com.clustercontrol.util.Messages;
import com.clustercontrol.view.ScopeListBaseView;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * 監視[ステータス]ビュークラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class StatusView extends ScopeListBaseView {
	private static Log m_log = LogFactory.getLog(StatusView.class);

	/** 監視[ステータス]ビューID */
	public static final String ID = StatusView.class.getName();

	/** ステータス情報一覧コンポジット */
	private StatusListComposite tableComposite = null;

	/** 検索条件 */
	private Property condition = null;

	/** 選択レコード数 */
	private int rowNum = 0;

	/** プラグインID */
	private String pluginId;

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

		this.tableComposite = new StatusListComposite(parent, SWT.NONE);
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
	protected void doSelectTreeItem(FacilityTreeItem item) {
		this.update(false);
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
	 * <p>
	 * <ol>
	 * <li>スコープツリーで選択されているアイテムを取得します。</li>
	 * <li>アイテムより、ファシリティIDを取得します。</li>
	 * <li>ファシリティIDをキーに、ステータス情報を更新します。
	 *     検索条件が<code>null</code>ではない場合は、検索条件も指定します。</li>
	 * </ol>
	 *
	 * @see com.clustercontrol.composite.FacilityTreeComposite#getSelectItem()
	 * @see com.clustercontrol.bean.FacilityInfo#getFacilityId()
	 * @see com.clustercontrol.monitor.composite.StatusListComposite#update(String)
	 * @see com.clustercontrol.monitor.composite.StatusListComposite#update(String, Property)
	 */
	@Override
	public void update(boolean refreshFlag) {
		@SuppressWarnings("unchecked")
		List<FacilityTreeItem> itemList = (List<FacilityTreeItem>) getScopeTreeComposite().getSelectionList();
		String facilityId = null;
		int type = 0;
		String label = null;
		
		FacilityTreeItem top = (FacilityTreeItem) getScopeTreeComposite().getTree().getItem(0).getData();
		
		// 何も選択していない場合は、最上位を選択したこととする。
		if (itemList == null || itemList.size() == 0) {
			itemList = new ArrayList<FacilityTreeItem>();
			itemList.add(top);
		}
		
		tableComposite.resetDisp();
		for (FacilityTreeItem item : itemList) {
			List<String> managerList = new ArrayList<String>();
			// Select root if nothing selected
			if( item == null && 0 < getScopeTreeComposite().getTree().getItemCount() ){
				item = top;
			}
	
			if( item != null ){
				FacilityInfo itemData = item.getData();
	
				if(itemData.getFacilityType() == FacilityConstant.TYPE_COMPOSITE){
					type = FacilityConstant.TYPE_COMPOSITE;
					managerList = EndpointManager.getActiveManagerNameList();
				} else if(itemData.getFacilityType() == FacilityConstant.TYPE_MANAGER) {
					String managerName = itemData.getFacilityId();
					type = FacilityConstant.TYPE_MANAGER;
					managerList.add(managerName);
				} else {
					type = FacilityConstant.TYPE_SCOPE;
					facilityId = item.getData().getFacilityId();
					FacilityTreeItem manager = ScopePropertyUtil.getManager(item);
					String managerName = manager.getData().getFacilityId();
					managerList.add(managerName);
				}
			}
	
			m_log.debug("facilityId=" + facilityId);
			
			if (this.condition == null) {
				// スコープツリーでアイテムが選択されていない場合
				if (label == null){
					label = Messages.getString("scope") + ": ";
				}
				if (facilityId != null) {
					FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
					label += path.getPath(item) + " ";
				}
				if(type == FacilityConstant.TYPE_COMPOSITE){
					//スコープが選択された
					this.tableComposite.setDisp(null, null, managerList);
				} else if (type == FacilityConstant.TYPE_MANAGER) {
					//マネージャが選択された
					this.tableComposite.setDisp(null, null, managerList);
				} else {
					//それ以外
					this.tableComposite.setDisp(facilityId, null, managerList);
				}
			} else {
				if (label == null){
					label = Messages.getString("filtered.list") + ": ";
				}
				if (facilityId != null) {
					FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
					label += path.getPath(item) + " ";
				}
				this.tableComposite.setDisp(facilityId, this.condition, managerList);
			}
		}
		tableComposite.updateDisp(refreshFlag);
		setPathLabel(label);
	}

	/**
	 * 検索条件を設定します。
	 * <p>
	 *
	 * 設定後のupdateは、この検索条件の結果が表示されます。
	 *
	 * @param condition
	 *            検索条件
	 */
	public void setCondition(Property condition) {
		this.condition = condition;
	}

	/**
	 * 検索条件とビューアクションを初期化します。
	 */
	@Override
	public void dispose() {
		super.dispose();

		this.setCondition(null);
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
}
