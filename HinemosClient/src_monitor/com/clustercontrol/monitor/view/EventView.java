/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view;

import java.text.ParseException;
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
import org.openapitools.client.model.EventFilterBaseRequest;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DefaultLayoutSettingManager.ListLayout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.filtersetting.util.EventFilterHelper;
import com.clustercontrol.monitor.composite.EventListComposite;
import com.clustercontrol.monitor.composite.action.EventListSelectionChangedListener;
import com.clustercontrol.monitor.preference.MonitorPreferencePage;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.monitor.view.action.EventCollectGraphOffAction;
import com.clustercontrol.monitor.view.action.EventCollectGraphOnAction;
import com.clustercontrol.monitor.view.action.EventConfirmAction;
import com.clustercontrol.monitor.view.action.EventConfirmingAction;
import com.clustercontrol.monitor.view.action.EventCustomCommandAction;
import com.clustercontrol.monitor.view.action.EventDetailAction;
import com.clustercontrol.monitor.view.action.EventModifyMonitorSettingAction;
import com.clustercontrol.monitor.view.action.EventOpenJobHistoryAction;
import com.clustercontrol.monitor.view.action.EventUnconfirmAction;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.ScopeListBaseView;

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

	/** フィルタ条件 */
	private EventFilterBaseRequest filter = EventFilterHelper.createDefaultFilter(null);

	/** フィルタが有効な場合は true */
	private boolean filterEnabled = false;
	
	/** 選択されているイベントの確認種別（各アクションの活性／非活性の判断に使用する） */
	private List<Integer> confirmTypeList = null;

	/** プラグインID */
	private String pluginId;
	
	private String selectedScopeLabel;
	
	@Override
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

		ListLayout tableCompositeLayout = null;
		if (this.getDefaultViewLayout() != null) {
			tableCompositeLayout = this.getDefaultViewLayout().getViewItem(EventListComposite.class.getSimpleName(), ListLayout.class);
		}
		
		this.tableComposite = new EventListComposite(parent, SWT.NONE, tableCompositeLayout);
		
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

		if (store.getBoolean(MonitorPreferencePage.P_EVENT_UPDATE_FLG) && this.tableComposite.isUpdateSuccess()) {
			this.startAutoReload();
		}else {
			this.stopAutoReload();
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
	protected void doSelectTreeItem(FacilityTreeItemResponse item) {
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
				EventFilterBaseRequest filter = EventFilterHelper.duplicate(this.filter);
				filter.setFacilityId(facilityId);
				tableComposite.setDisp(filter, managerList);
			} else {
				tableComposite.setDisp(EventFilterHelper.createDefaultFilter(facilityId), managerList);
			}
		}

		try {
			tableComposite.updateDisp(refreshFlag);
		} catch (ParseException | HinemosUnknown e) {
			m_log.warn("update() : updateDisp failed " + e.getMessage());
		}

		String label = Messages.getString(filterEnabled ? "filtered.list" : "scope") + ": ";
		if (selectedScopeLabel.length() > 0) {
			label += selectedScopeLabel;
		}
		if (tableComposite.getLabel().trim().length() > 0) {
			label += ",   " + tableComposite.getLabel();
		}
		setPathLabel(label);
		m_log.debug("update() : label=" + label);

		// 更新に失敗している場合は自動更新を停止する
		if (!tableComposite.isUpdateSuccess()) {
			this.stopAutoReload();
		}
	}

	/**
	 * 検索条件を設定します。
	 * <p>設定後の{@link #update(boolean) update}は、この検索条件の結果が表示されます。
	 */
	public void setFilter(EventFilterBaseRequest filter) {
		this.filter = filter;
	}

	public EventFilterBaseRequest getFilter() {
		return filter;
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

	public void initButton(){
		setEnabledAction(null, null, null);
	}

	public List<Integer> getConfirmTypeList() {
		return this.confirmTypeList;
	}

	public String getPluginId() {
		return this.pluginId;
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param confirmType 選択されている確認状態
	 * @param pluginId プラグインID
	 * @param selection ボタン（アクション）を有効にするための情報
	 *
	 * @see com.clustercontrol.bean.FacilityConstant
	 */
	public void setEnabledAction(List<Integer> confirmTypeList, String pluginId, ISelection selection) {
		this.confirmTypeList = confirmTypeList;
		this.pluginId = pluginId;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(EventConfirmAction.ID, null);
			service.refreshElements(EventConfirmingAction.ID, null);
			service.refreshElements(EventUnconfirmAction.ID, null);
			service.refreshElements(EventDetailAction.ID, null);
			service.refreshElements(EventModifyMonitorSettingAction.ID, null);
			service.refreshElements(EventOpenJobHistoryAction.ID, null);
			service.refreshElements(EventCollectGraphOnAction.ID, null);
			service.refreshElements(EventCollectGraphOffAction.ID, null);
			service.refreshElements(EventCustomCommandAction.ID, null);

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
	
	public MultiManagerEventDisplaySettingInfo getEventDspSetting() {
		if (this.tableComposite == null) {
			return new MultiManagerEventDisplaySettingInfo();
		}
		return this.tableComposite.getEventDspSetting();
	}

	/**
	 * 更新成功可否を返します。
	 * @return 更新成功可否
	 */
	public boolean isUpdateSuccess() {
		return this.tableComposite.isUpdateSuccess();
	}
}
