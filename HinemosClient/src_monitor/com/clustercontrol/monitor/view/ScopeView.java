/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DefaultLayoutSettingManager.ListLayout;
import com.clustercontrol.monitor.composite.ScopeListComposite;
import com.clustercontrol.monitor.preference.MonitorPreferencePage;
import com.clustercontrol.view.ScopeListBaseView;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 監視[スコープ]ビュークラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class ScopeView extends ScopeListBaseView {
	public static final String ID = ScopeView.class.getName();

	/** スコープ情報一覧コンポジット */
	private ScopeListComposite tableComposite = null;

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * スコープ情報一覧テーブルを作成・追加します。
	 * <p>
	 * <ol>
	 * <li>スコープ情報一覧テーブルを取得します。</li>
	 * <li>ビューを更新します。</li>
	 * <li>プレファレンスページの設定より、監視[スコープ]ビューの自動更新周期を取得し、このビューにセットします。</li>
	 * <li>プレファレンスページの設定より、監視[スコープ]ビューの自動更新フラグを取得し、このビューにセットします。</li>
	 * <li>スコープ情報一覧テーブルを返します。</li>
	 * </ol>
	 *
	 * @param parent 親のコンポジット
	 * @return スコープ情報一覧テーブルコンポジット
	 *
	 * @see com.clustercontrol.monitor.composite.ScopeListComposite#ScopeListComposite(Composite, int)
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
			tableCompositeLayout = this.getDefaultViewLayout().getViewItem(ScopeListComposite.class.getSimpleName(), ListLayout.class);
		}
		this.tableComposite = new ScopeListComposite(parent, SWT.NONE, tableCompositeLayout);
		WidgetTestUtil.setTestId(this, null, tableComposite);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.tableComposite.setLayoutData(gridData);

		this.update(false);

		// 設定情報反映
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();

		this.setInterval(store.getInt(MonitorPreferencePage.P_SCOPE_UPDATE_CYCLE));

		if (store.getBoolean(MonitorPreferencePage.P_SCOPE_UPDATE_FLG)) {
			this.startAutoReload();
		}

		return this.tableComposite;
	}

	/**
	 * スコープツリーでアイテムを選択した際に呼ばれ、ビューを更新します。
	 *
	 * @param item スコープツリーアイテム
	 *
	 * @see com.clustercontrol.monitor.composite.ScopeListComposite#update(String)
	 */
	@Override
	protected void doSelectTreeItem( FacilityTreeItem selectItem ){
		FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
		setPathLabel(Messages.getString("scope") + " : "  + path.getPath(selectItem));
		
		// Select root if nothing selected
		if( selectItem == null && 0 < getScopeTreeComposite().getTree().getItemCount() ){
			selectItem = (FacilityTreeItem) getScopeTreeComposite().getTree().getItem(0).getData();
		}

		if( selectItem != null ){
			FacilityInfo itemData = selectItem.getData();

			String managerName;
			String facilityId;
			if(itemData.getFacilityType() == FacilityConstant.TYPE_COMPOSITE){
				this.tableComposite.update( EndpointManager.getActiveManagerNameList() );
			}else if(itemData.getFacilityType() == FacilityConstant.TYPE_MANAGER) {
				managerName = selectItem.getData().getFacilityId();
				this.tableComposite.update( managerName, "" );
			}else{
				FacilityTreeItem manager = ScopePropertyUtil.getManager(selectItem);
				managerName = manager.getData().getFacilityId();
				facilityId = selectItem.getData().getFacilityId();
				this.tableComposite.update( managerName, facilityId );
			}
		}
	}

	/**
	 * ビューを更新します。<BR>
	 * <p>
	 * <ol>
	 * <li>スコープツリーで選択されているアイテムを取得します。</li>
	 * <li>アイテムより、ファシリティIDを取得します。</li>
	 * <li>ファシリティIDをキーに、イベント情報を更新します。</li>
	 * </ol>
	 *
	 * @see com.clustercontrol.monitor.composite.ScopeListComposite#update(String)
	 */
	@Override
	public void update(boolean refreshFlag) {
		FacilityTreeItem item = this.getScopeTreeComposite().getSelectItem();
		doSelectTreeItem( item );
	}
	
	@Override
	public boolean isDefaultLayoutView() {
		return true;
	}
}
