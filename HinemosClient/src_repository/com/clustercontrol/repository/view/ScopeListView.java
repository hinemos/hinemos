/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.composite.ScopeListComposite;
import com.clustercontrol.repository.composite.action.FacilityTreeSelectionChangedListener;
import com.clustercontrol.repository.composite.action.ScopeListSelectionChangedListener;
import com.clustercontrol.repository.dialog.ScopeCreateDialog;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.repository.view.action.NodeAssignAction;
import com.clustercontrol.repository.view.action.NodeReleaseAction;
import com.clustercontrol.repository.view.action.RefreshAction;
import com.clustercontrol.repository.view.action.ScopeAddAction;
import com.clustercontrol.repository.view.action.ScopeDeleteAction;
import com.clustercontrol.repository.view.action.ScopeModifyAction;
import com.clustercontrol.repository.view.action.ScopeObjectPrivilegeAction;
import com.clustercontrol.repository.view.action.ScopeShowAction;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;
import com.clustercontrol.view.ScopeListBaseView;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * スコープ登録ビュークラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class ScopeListView extends ScopeListBaseView implements ObjectPrivilegeTargetListView {
	public static final String ID = ScopeListView.class.getName();

	// ----- instance フィールド ----- //

	/** リポジトリ[スコープ]ビュー用コンポジット */
	private ScopeListComposite composite = null;

	/** Last focus composite (Tree/List) */
	private Composite lastFocusComposite = null;

	/** 組み込みスコープかどうかのフラグ */
	private boolean builtin;

	/** スコープとノードの種別 */
	private int type;

	/** ボタン（アクション）を有効にするための情報 */
	private boolean notReferFlg;

	// ----- コンストラクタ ----- //

	// ----- instance メソッド ----- //
	protected String getViewName() {
		return this.getClass().getName();
	}
	/**
	 * @see com.clustercontrol.view.ScopeListBaseView#createListContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Composite createListContents(Composite parent) {
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		this.composite = new ScopeListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, composite);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.composite.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		//Listenerの追加
		super.getScopeTreeComposite().getTreeViewer().addSelectionChangedListener(
				new FacilityTreeSelectionChangedListener());

		this.composite.getTableViewer().addSelectionChangedListener(
				new ScopeListSelectionChangedListener(composite));

		this.update();
		setLastFocusComposite(this.getScopeTreeComposite());
		
		this.getScopeTreeComposite().getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				FacilityTreeItem item = (FacilityTreeItem) selection.getFirstElement();
				
				// 未選択の場合は、処理終了
				if( null == item ){
					return;
				}

				// スコープかつビルトインでない場合のみ処理
				FacilityInfo info = item.getData();
				if (info.getFacilityType() == FacilityConstant.TYPE_SCOPE && !info.isBuiltInFlg()) {
					FacilityTreeItem manager = ScopePropertyUtil.getManager(item);
					String managerName = manager.getData().getFacilityId();
	
					// ダイアログを生成
					String facilityId = info.getFacilityId();
					ScopeCreateDialog dialog = new ScopeCreateDialog(composite.getShell(), managerName, facilityId, true);
	
					// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
					if (dialog.open() == IDialogConstants.OK_ID) {
						composite.update();
					}
				}
			}
		});

		return this.composite;
	}

	/**
	 * ポップアップメニュー作成
	 *
	 *
	 */
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu treeMenu = menuManager.createContextMenu(this.getScopeTreeComposite().getTree());
		this.getScopeTreeComposite().getTree().setMenu(treeMenu);

		Menu listMenu = menuManager.createContextMenu(this.composite.getTable());
		WidgetTestUtil.setTestId(this, null, listMenu);
		this.composite.getTable().setMenu(listMenu);
		getSite().registerContextMenu(menuManager, this.composite.getTableViewer());
	}

	/**
	 * 選択されたスコープ(ノード)の情報を表示します。
	 *
	 * @param item
	 *            ツリーアイテム
	 */
	@Override
	protected void doSelectTreeItem(FacilityTreeItem selectItem) {
		FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
		setPathLabel(Messages.getString("scope") + " : "  + path.getPath(selectItem));

		this.composite.update(selectItem);
	}

	/**
	 * ビューを更新します。
	 *
	 * @see com.clustercontrol.view.AutoUpdateView#update()
	 */
	@Override
	public void update() {
		ClientSession.doCheck();
	}

	/**
	 * アダプターとして要求された場合、自身のインスタンスを渡します。
	 *
	 * @param cls
	 *            クラスのインスタンス
	 * @return 自身のインスタンス
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class cls) {
		if (cls.isInstance(this)) {
			return this;
		} else {
			return super.getAdapter(cls);
		}
	}

	public ScopeListComposite getComposite() {
		return this.composite;
	}

	public boolean getBuiltin() {
		return this.builtin;
	}

	public int getType() {
		return this.type;
	}

	public boolean getNotReferFlg() {
		return this.notReferFlg;
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param notReferFlg 参照不可フラグ
	 *
	 * @see com.clustercontrol.bean.FacilityConstant
	 */
	public void setEnabledAction(boolean builtin,int type, ISelection selection, boolean notReferFlg) {
		this.builtin = builtin;
		this.type = type;
		this.notReferFlg = notReferFlg;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(ScopeAddAction.ID, null);
			service.refreshElements(ScopeModifyAction.ID, null);
			service.refreshElements(ScopeDeleteAction.ID, null);
			service.refreshElements(NodeAssignAction.ID, null);
			service.refreshElements(NodeReleaseAction.ID, null);
			service.refreshElements(ScopeObjectPrivilegeAction.ID, null);
			service.refreshElements(RefreshAction.ID, null);
			service.refreshElements(ScopeShowAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {

		// 選択されているスコープを取得する
		FacilityTreeItem item = getSelectedScopeItem();

		// 選択されており、スコープの場合は値を返す
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		if (item != null) {
			FacilityTreeItem manager = ScopePropertyUtil.getManager(item);
			String managerName = manager == null ? item.getData().getFacilityId() : manager.getData().getFacilityId();
			String objectId = item.getData().getFacilityId();
			String objectType = HinemosModuleConstant.PLATFORM_REPOSITORY;
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {

		// 選択されているスコープを取得する
		FacilityTreeItem item = getSelectedScopeItem();

		// 選択されており、スコープの場合は値を返す
		String ownerRoleId = null;
		if (item != null) {
			ownerRoleId = item.getData().getOwnerRoleId();
		}
		return ownerRoleId;
	}

	public FacilityTreeItem getSelectedScopeItem() {
		FacilityTreeComposite tree = this.getScopeTreeComposite();
		ScopeListComposite list = (ScopeListComposite) this.getListComposite();

		// tree.getTree().isFocusControl() is not working under RAP because of Toolbar focus
		FacilityTreeItem item = null;
		if( this.lastFocusComposite instanceof FacilityTreeComposite ){
			item = tree.getSelectItem();
		}else if( this.lastFocusComposite instanceof ScopeListComposite ){
			item = list.getSelectItem();
		}

		// 選択されており、スコープの場合は値を返す
		if( null != item ){
			return item;
		} else {
			return null;
		}
	}

	public List<?> getSelectedScopeItems() {
		FacilityTreeComposite tree = this.getScopeTreeComposite();
		ScopeListComposite list = (ScopeListComposite) this.getListComposite();

		// tree.getTree().isFocusControl() is not working under RAP because of Toolbar focus
		List<?> items = null;
		if( this.lastFocusComposite instanceof FacilityTreeComposite ){
			items = tree.getSelectionList();
		}else if( this.lastFocusComposite instanceof ScopeListComposite ){
			StructuredSelection selection = (StructuredSelection)list.getTableViewer().getSelection();
			items = selection.toList();
		}

		// 選択されており、スコープの場合は値を返す
		if( !( null == items || items.isEmpty() ) ){
			return items;
		} else {
			return null;
		}
	}

	/**
	 * Set last focus composite(Tree/List)
	 * @param composite
	 */
	public void setLastFocusComposite( Composite composite ){
		if( ! composite.equals(this.lastFocusComposite) ){
			this.lastFocusComposite = composite;
		}
	}
	@Override
	public void update(boolean refreshFlag) {
		// 実装なし
		
	}
}
