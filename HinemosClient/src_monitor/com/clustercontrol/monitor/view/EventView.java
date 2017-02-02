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
import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.composite.EventListComposite;
import com.clustercontrol.monitor.composite.action.EventListSelectionChangedListener;
import com.clustercontrol.monitor.preference.MonitorPreferencePage;
import com.clustercontrol.monitor.view.action.EventCollectGraphOffAction;
import com.clustercontrol.monitor.view.action.EventCollectGraphOnAction;
import com.clustercontrol.monitor.view.action.EventConfirmAction;
import com.clustercontrol.monitor.view.action.EventDetailAction;
import com.clustercontrol.monitor.view.action.EventModifyMonitorSettingAction;
import com.clustercontrol.monitor.view.action.EventOpenJobHistoryAction;
import com.clustercontrol.monitor.view.action.EventUnconfirmAction;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.ScopeListBaseView;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * 監視[イベント]ビュークラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class EventView extends ScopeListBaseView {

	private static Log m_log = LogFactory.getLog( EventView.class );
	
	/** 監視[イベント]ビューID */
	public static final String ID = EventView.class.getName();

	/** イベント情報一覧コンポジット */
	private EventListComposite tableComposite = null;

	/** 検索条件 */
	private Property condition = null;

	/** 確認/未確認の種別 */
	private int confirmType = ConfirmConstant.TYPE_UNCONFIRMED;

	/** プラグインID */
	private String pluginId;

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * イベント情報一覧テーブルを作成・追加します。
	 * <p>
	 * <ol>
	 * <li>イベント情報一覧テーブルを取得します。</li>
	 * <li>イベント情報一覧テーブルに、ポップアップメニューを追加します。</li>
	 * <li>ビューを更新します。</li>
	 * <li>プレファレンスページの設定より、監視[イベント]ビューの自動更新周期を取得し、このビューにセットします。</li>
	 * <li>プレファレンスページの設定より、監視[イベント]ビューの自動更新フラグを取得し、このビューにセットします。</li>
	 * <li>イベント情報一覧テーブルを返します。</li>
	 * </ol>
	 *
	 * @param parent 親のコンポジット
	 * @return イベント情報一覧テーブルコンポジット
	 *
	 * @see com.clustercontrol.monitor.composite.EventListComposite#EventListComposite(Composite, int)
	 * @see #createContextMenu()
	 * @see #update()
	 */
	@Override
	protected Composite createListContents(Composite parent) {
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		this.tableComposite = new EventListComposite(parent, SWT.NONE);
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

		this.setInterval(store.getInt(MonitorPreferencePage.P_EVENT_UPDATE_CYCLE));

		if (store.getBoolean(MonitorPreferencePage.P_EVENT_UPDATE_FLG)) {
			this.startAutoReload();
		}
		// ボタン（アクション）を制御するリスナーを登録
		this.tableComposite.getTableViewer().addSelectionChangedListener(
				new EventListSelectionChangedListener());

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
	 * <li>ファシリティIDをキーに、イベント情報を更新します。
	 *     検索条件が<code>null</code>ではない場合は、検索条件も指定します。</li>
	 * </ol>
	 *
	 * @see com.clustercontrol.composite.FacilityTreeComposite#getSelectItem()
	 * @see com.clustercontrol.bean.FacilityInfo#getFacilityId()
	 * @see com.clustercontrol.monitor.composite.EventListComposite#update(String)
	 * @see com.clustercontrol.monitor.composite.EventListComposite#update(String, Property)
	 */
	@Override
	public void update(boolean refreshFlag) {
		@SuppressWarnings("unchecked")
		List<FacilityTreeItem> itemList = (List<FacilityTreeItem>) getScopeTreeComposite().getSelectionList();
		
		FacilityTreeItem top = (FacilityTreeItem) getScopeTreeComposite().getTree().getItem(0).getData();
		
		// 何も選択していない場合は、最上位を選択したこととする。
		if (itemList == null || itemList.size() == 0) {
			itemList = new ArrayList<FacilityTreeItem>();
			itemList.add(top);
		}
		
		int type = 0;

		String label = null;

		// Select root if nothing selected
		tableComposite.resetDisp();
		for (FacilityTreeItem item : itemList) {
			List<String> managerList = new ArrayList<String>();
			String facilityId = null;
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
			
			m_log.debug("update " + facilityId);
			
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
					tableComposite.setDisp(null, null, managerList);
				} else if (type == FacilityConstant.TYPE_MANAGER) {
					//マネージャが選択された
					tableComposite.setDisp(null, null, managerList);
				} else {
					//それ以外
					tableComposite.setDisp(facilityId, null, managerList);
				}
			} else {
				if (label == null){
					label = Messages.getString("filtered.list") + ": ";
				}
				if (facilityId != null) {
					FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
					label += path.getPath(item) + " ";
				}
				tableComposite.setDisp(facilityId, this.condition, managerList);
			}
		}
		tableComposite.updateDisp(refreshFlag);
		m_log.debug("update() : label=" + label + ",   " + tableComposite.getLabel());
		if (label == null) {
			setPathLabel("");
		} else {
			setPathLabel(label + ",   " + tableComposite.getLabel());
		}
	}

	/**
	 * 検索条件を設定します。
	 * <p>設定後の<code>update</code>は、この検索条件の結果が表示されます。
	 *
	 * @param condition 検索条件
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

	public void initButton(){
		setEnabledAction(ConfirmConstant.TYPE_ALL, null, null);
	}

	public int getConfirmType() {
		return this.confirmType;
	}

	public String getPluginId() {
		return this.pluginId;
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param confirmType 確認/未確認の種別
	 * @param pluginId プラグインID
	 * @param selection ボタン（アクション）を有効にするための情報
	 *
	 * @see com.clustercontrol.bean.FacilityConstant
	 */
	public void setEnabledAction(int confirmType, String pluginId, ISelection selection) {
		this.confirmType = confirmType;
		this.pluginId = pluginId;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(EventConfirmAction.ID, null);
			service.refreshElements(EventUnconfirmAction.ID, null);
			service.refreshElements(EventDetailAction.ID, null);
			service.refreshElements(EventModifyMonitorSettingAction.ID, null);
			service.refreshElements(EventOpenJobHistoryAction.ID, null);
			service.refreshElements(EventCollectGraphOnAction.ID, null);
			service.refreshElements(EventCollectGraphOffAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);

		}
	}
}
