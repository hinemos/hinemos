/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.jobmap.composite.JobMapTreeComposite;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * ジョブ[一覧]ビュークラスです。
 * 
 * @version 6.1.0
 * @since 1.0.0
 */
public class JobModuleView extends JobTreeView implements ObjectPrivilegeTargetListView {

	/** ビューID */
	public static final String ID = JobModuleView.class.getName();
//	/** ジョブツリー用コンポジット */
//	protected JobMapTreeComposite m_jobTree = null;
//	/** ジョブツリーアイテム */
//	protected JobTreeItem m_copyJobTreeItem = null;
//	/** 更新フラグ */
//	protected boolean m_update = false;

//	private int dataType;
//	private boolean editEnable;
//	
	/**
	 * コンストラクタ
	 */
	public JobModuleView() {
		super();
	}

	@Override
	public void dispose() {
		m_jobTree.removeFromTreeViewerList();
//		m_jobTreeModuleOnly.removeFromTreeViewerList();
		
		super.dispose();
	}
	
	/**
	 * ビューを構築します。
	 * 
	 * @param parent 親コンポジット
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 * @see #createContextMenu()
	 * @see #update()
	 */
	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		//ジョブ階層ツリー作成
		m_jobTree = new JobMapTreeComposite(parent, SWT.NONE, null, true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_jobTree.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		m_jobTree.addToTreeViewerList();
		
		//ツリーを開く
//		m_jobTree.getTreeViewer().expandToLevel(3);


//		//ジョブ階層モジュール登録限定ツリー作成
//		m_jobTreeModuleOnly = new JobMapTreeComposite(parent, SWT.NONE, null, true);
//
//		GridData gridData2 = new GridData();
//		gridData2.horizontalAlignment = GridData.FILL;
//		gridData2.verticalAlignment = GridData.FILL;
//		gridData2.grabExcessHorizontalSpace = true;
//		gridData2.grabExcessVerticalSpace = true;
//		m_jobTreeModuleOnly.setLayoutData(gridData2);
//
//		//ポップアップメニュー作成
////		createContextMenu(m_jobTree2);
//
////		m_jobTree2.addToTreeViewerList();
//
//		m_jobTreeModuleOnly.getTreeViewer().expandToLevel(3);
}

//	/**
//	 * コンテキストメニューを作成します。
//	 * 
//	 * @see org.eclipse.jface.action.MenuManager
//	 * @see org.eclipse.swt.widgets.Menu
//	 */
//	protected void createContextMenu() {
//		MenuManager menuManager = new MenuManager();
//		menuManager.setRemoveAllWhenShown(true);
//		Menu treeMenu = menuManager.createContextMenu(m_jobTree.getTree());
//		WidgetTestUtil.setTestId(this, null, treeMenu);
//		m_jobTree.getTree().setMenu(treeMenu);
//		getSite().registerContextMenu( menuManager, m_jobTree.getTreeViewer() );
//
//		JobTreeItem select = getSelectJobTreeItem();
//		dataType = select == null ? JobConstant.TYPE_COMPOSITE : select.getData().getType();
//	}
//
//
//	/**
//	 * ビューを更新します。
//	 * 
//	 * @see com.clustercontrol.jobmanagement.composite.JobMapTreeComposite#update()
//	 * @see com.clustercontrol.jobmanagement.composite.JobMapTreeComposite#getSelectItem()
//	 * @see com.clustercontrol.jobmanagement.composite.JobMapListComposite#update(JobTreeItem)
//	 */
//	public void update() {
//		JobEditStateUtil.releaseAll();
//
//		m_jobTree.update();
////		m_jobTreeModuleOnly.update();
//		JobMapEditorView view = JobMapActionUtil.getJobMapEditorView();
//		if (view != null) {
//			view.clear();
//		}
//
//		m_update = false;
//	}
//	
//	/**
//	 * ビューのアクションの有効/無効を設定します。
//	 * 
//	 * @param type ジョブ種別
//	 * @param selection ボタン（アクション）を有効にするための情報
//	 * 
//	 * @see com.clustercontrol.bean.JobConstant
//	 */
//	public void setEnabledAction(int type, ISelection selection) {
//		setEnabledAction(type, "", selection);
//	}
//	
//	/**
//	 * ビューのアクションの有効/無効を設定します。
//	 * 
//	 * @param type ジョブ種別
//	 * @param selection ボタン（アクション）を有効にするための情報
//	 * 
//	 * @see com.clustercontrol.bean.JobConstant
//	 */
//	@Override
//	public void setEnabledAction(int type, String jobunitId, ISelection selection) {
//		this.dataType = type;
//
//		//ビューアクションの使用可/不可を設定
//		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
//		if( null != service ){
//			JobTreeItem item = getSelectJobTreeItem();
//			if (item == null) {
//				return;
//			}
//			
//			String managerName = JobTreeItemUtil.getManagerName(item);
//			if (managerName == null) {
//				editEnable = false;
//			} else {
//				JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
//				editEnable = editState.isLockedJobunitId(item.getData().getJobunitId());
//			}
//
//			service.refreshElements(CreateJobUnitAction.ID, null);
//			service.refreshElements(CreateJobNetAction.ID, null);
//			service.refreshElements(CreateJobAction.ID, null);
//			service.refreshElements(CreateFileJobAction.ID, null);
//			service.refreshElements(CreateReferJobAction.ID, null);
//			service.refreshElements(CreateApprovalJobAction.ID, null);
//			service.refreshElements(CreateMonitorJobAction.ID, null);
//			service.refreshElements(DeleteJobAction.ID, null);
//			service.refreshElements(ModifyJobAction.ID, null);
//			service.refreshElements(JobObjectPrivilegeAction.ID, null);
//			service.refreshElements(RunJobAction.ID, null);
//			service.refreshElements(EditModeAction.ID, null);
//			service.refreshElements(CopyJobAction.ID, null);
//			service.refreshElements(PasteJobAction.ID, null);
//
//
//			// Update ToolBar after elements refreshed
//			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
//			getViewSite().getActionBars().updateActionBars();
//			getViewSite().getActionBars().getToolBarManager().update(false);
//		}
//	}
//
//	/**
//	 * データタイプを返します。
//	 * @return
//	 */
//	public int getDataType() {
//		return this.dataType;
//	}	
//	
//	public boolean getEditEnable(){
//		return this.editEnable;
//	}
//	
//	
//	/**
//	 * ジョブツリー用のコンポジットを返します。
//	 * 
//	 * @return ジョブツリー用のコンポジット
//	 */
//	public JobMapTreeComposite getJobMapTreeComposite() {
//		return m_jobTree;
//	}
//
////	/**
////	 * ジョブツリーのモジュール登録されているものだけを表示しているコンポジットを返します。
////	 * @return
////	 */
////	public JobMapTreeComposite getJobMapTreeCompositeModuleOnly() {
////		return m_jobTreeModuleOnly;
////	}
//	/**
//	 * ジョブツリーを表示します。
//	 */
//	public void show() {
//	}
//
//	/**
//	 * 選択ジョブツリーアイテムを返します。
//	 * 
//	 * @return JobTreeItem 選択されたジョブツリーアイテム
//	 */
//	public JobTreeItem getSelectJobTreeItem() {
//		JobTreeItem select = null;
//		List<JobTreeItem> selectItemList = m_jobTree.getSelectItemList();
//		if (selectItemList != null && !selectItemList.isEmpty()) {
//			select = selectItemList.get(0);
//		}
//		
//		return select;
//	}
//	
//	public List<JobTreeItem> getSelectJobTreeItemList() {
//		List<JobTreeItem> selectItemList = m_jobTree.getSelectItemList();
//		return selectItemList;
//	}
//
//	/**
//	 * コピー元ジョブツリーアイテムを取得します。
//	 * 
//	 * @return コピー元ジョブツリーアイテム
//	 */
//	public JobTreeItem getCopyJobTreeItem() {
//		return m_copyJobTreeItem;
//	}
//
//	/**
//	 * コピー元ジョブツリーアイテムを設定します。
//	 * 
//	 * @param copy コピー元ジョブツリーアイテム
//	 */
//	public void setCopyJobTreeItem(JobTreeItem copy) {
//		m_copyJobTreeItem = copy;
//	}
//
//	/**
//	 * ジョブツリーアイテムの更新フラグを返します。
//	 * 
//	 * @return 更新フラグ
//	 */
//	public boolean isUpdate() {
//		return m_update;
//	}
//
//	/**
//	 * ジョブツリーアイテムの更新フラグを設定します。
//	 * 
//	 * @param update 更新フラグ
//	 */
//	public void setUpdate(boolean update) {
//		m_update = update;
//	}
//
//	@Override
//	public List<ObjectBean> getSelectedObjectBeans() {
//
//		// 選択されているスコープを取得する
//		JobTreeItem item = getSelectedJobunitItem();
//		
//		// 選択されており、スコープの場合は値を返す
//		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
//		if (item != null) {
//			String objectId = item.getData().getJobunitId();
//			String objectType = HinemosModuleConstant.JOB;
//			
//			JobTreeItem manager = JobTreeItemUtil.getManager(item);
//			String managerName = manager.getData().getId();
//			
//			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
//			objectBeans.add(objectBean);
//		}
//		return objectBeans;
//	}
//
//	@Override
//	public String getSelectedOwnerRoleId() {
//
//		// 選択されているスコープを取得する
//		JobTreeItem item = getSelectedJobunitItem();
//
//		// 選択されており、スコープの場合は値を返す
//		String ownerRoleId = null;
//		if (item != null) {
//			ownerRoleId = item.getData().getOwnerRoleId();
//		}
//		return ownerRoleId;
//	}
//	
//	private JobTreeItem getSelectedJobunitItem() {
//		JobMapTreeComposite tree = getJobMapTreeComposite();
//		JobMapEditorView view = JobMapActionUtil.getJobMapEditorView();
//
//		JobTreeItem item = null;
//		if(tree.getTree().isFocusControl()){
//			List<JobTreeItem> selectItemList = tree.getSelectItemList();
//			if (selectItemList != null && !selectItemList.isEmpty()) {
//				item = selectItemList.get(0);
//			}
//		} else{
//			item = view.getFocusFigure().getJobTreeItem();
//		}
//
//		// 選択されており、ジョブユニットの場合は値を返す
//		if (item != null && item.getData().getType() == JobConstant.TYPE_JOBUNIT) {
//			return item;
//		} else {
//			return null;
//		}
//	}
//
//	@Override
//	protected String getViewName() {
//		return this.getClass().getName();
//	}
}