/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.openapitools.client.model.JobTreeItem;

import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmap.composite.JobMapTreeComposite;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.action.CopyJobAction;
import com.clustercontrol.jobmap.view.action.CreateApprovalJobAction;
import com.clustercontrol.jobmap.view.action.CreateFileCheckJobAction;
import com.clustercontrol.jobmap.view.action.CreateFileJobAction;
import com.clustercontrol.jobmap.view.action.CreateJobAction;
import com.clustercontrol.jobmap.view.action.CreateJobLinkRcvJobAction;
import com.clustercontrol.jobmap.view.action.CreateJobLinkSendJobAction;
import com.clustercontrol.jobmap.view.action.CreateJobNetAction;
import com.clustercontrol.jobmap.view.action.CreateJobUnitAction;
import com.clustercontrol.jobmap.view.action.CreateMonitorJobAction;
import com.clustercontrol.jobmap.view.action.CreateReferJobAction;
import com.clustercontrol.jobmap.view.action.CreateResourceJobAction;
import com.clustercontrol.jobmap.view.action.CreateRpaJobAction;
import com.clustercontrol.jobmap.view.action.DeleteJobAction;
import com.clustercontrol.jobmap.view.action.EditModeAction;
import com.clustercontrol.jobmap.view.action.JobObjectPrivilegeAction;
import com.clustercontrol.jobmap.view.action.ModifyJobAction;
import com.clustercontrol.jobmap.view.action.OpenJobMapAction;
import com.clustercontrol.jobmap.view.action.PasteJobAction;
import com.clustercontrol.jobmap.view.action.RunJobAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * ジョブ[一覧]ビュークラスです。
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
public class JobTreeView extends CommonViewPart  implements ObjectPrivilegeTargetListView {

	/** ビューID */
	public static final String ID = JobTreeView.class.getName();
	/** ジョブツリー用コンポジット */
	protected JobMapTreeComposite m_jobTree = null;
	/** ジョブツリーアイテム */
	protected JobTreeItemWrapper m_copyJobTreeItem = null;
	/** 更新フラグ */
	protected boolean m_update = false;

	private JobInfoWrapper.TypeEnum dataType;
	private boolean editEnable;
	
	/**
	 * コンストラクタ
	 */
	public JobTreeView() {
		super();
	}

	@Override
	public void dispose() {
		m_jobTree.removeFromTreeViewerList();
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
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		//ジョブ階層ツリー作成
		m_jobTree = new JobMapTreeComposite(parent, SWT.NONE, null);

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
		m_jobTree.getTreeViewer().expandToLevel(3);
	}

	/**
	 * コンテキストメニューを作成します。
	 * 
	 * @see org.eclipse.jface.action.MenuManager
	 * @see org.eclipse.swt.widgets.Menu
	 */
	protected void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		Menu treeMenu = menuManager.createContextMenu(m_jobTree.getTree());
		WidgetTestUtil.setTestId(this, null, treeMenu);
		m_jobTree.getTree().setMenu(treeMenu);
		getSite().registerContextMenu( menuManager, this.m_jobTree.getTreeViewer() );

		JobTreeItemWrapper select = getSelectJobTreeItem();
		dataType = select == null ? JobInfoWrapper.TypeEnum.COMPOSITE : select.getData().getType();
	}


	/**
	 * ビューを更新します。
	 * 
	 * @see com.clustercontrol.jobmanagement.composite.JobMapTreeComposite#update()
	 * @see com.clustercontrol.jobmanagement.composite.JobMapTreeComposite#getSelectItem()
	 * @see com.clustercontrol.jobmanagement.composite.JobMapListComposite#update(JobTreeItem)
	 */
	public void update() {
		JobEditStateUtil.releaseAll();

		m_jobTree.update();
		JobMapEditorView view = JobMapActionUtil.getJobMapEditorView();
		if (view != null) {
			view.clear();
		}

		m_update = false;
	}
	
	/**
	 * ビューのアクションの有効/無効を設定します。
	 * 
	 * @param type ジョブ種別
	 * @param selection ボタン（アクション）を有効にするための情報
	 * 
	 * @see com.clustercontrol.bean.JobConstant
	 */
	public void setEnabledAction(JobInfoWrapper.TypeEnum type, ISelection selection) {
		setEnabledAction(type, "", selection);
	}
	/**
	 * ビューのアクション全無効を設定します。
	 * 
	 * @param selection ボタン（アクション）を有効にするための情報
	 * 
	 * @see com.clustercontrol.bean.JobConstant
	 */
	public void setDisabledAction( ISelection selection) {
		setEnabledAction(null, "", selection);
	}
	
	/**
	 * ビューのアクションの有効/無効を設定します。
	 * 
	 * @param type ジョブ種別
	 * @param selection ボタン（アクション）を有効にするための情報
	 * 
	 * @see com.clustercontrol.bean.JobConstant
	 */
	public void setEnabledAction(JobInfoWrapper.TypeEnum type, String jobunitId, ISelection selection) {
		this.dataType = type;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			JobTreeItemWrapper item = getSelectJobTreeItem();
			if (item == null) {
				return;
			}
			
			String managerName = JobTreeItemUtil.getManagerName(item);
			if (managerName == null) {
				editEnable = false;
			} else {
				JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
				editEnable = editState.isLockedJobunitId(item.getData().getJobunitId());
			}

			service.refreshElements(CreateJobUnitAction.ID, null);
			service.refreshElements(CreateJobNetAction.ID, null);
			service.refreshElements(CreateJobAction.ID, null);
			service.refreshElements(CreateFileJobAction.ID, null);
			service.refreshElements(CreateReferJobAction.ID, null);
			service.refreshElements(CreateApprovalJobAction.ID, null);
			service.refreshElements(CreateMonitorJobAction.ID, null);
			service.refreshElements(CreateFileCheckJobAction.ID, null);
			service.refreshElements(CreateJobLinkSendJobAction.ID, null);
			service.refreshElements(CreateJobLinkRcvJobAction.ID, null);
			service.refreshElements(CreateResourceJobAction.ID, null);
			service.refreshElements(CreateRpaJobAction.ID, null);
			service.refreshElements(DeleteJobAction.ID, null);
			service.refreshElements(ModifyJobAction.ID, null);
			service.refreshElements(JobObjectPrivilegeAction.ID, null);
			service.refreshElements(RunJobAction.ID, null);
			service.refreshElements(EditModeAction.ID, null);
			service.refreshElements(CopyJobAction.ID, null);
			service.refreshElements(PasteJobAction.ID, null);
			service.refreshElements(OpenJobMapAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}

	/**
	 * データタイプを返します。
	 * @return
	 */
	public JobInfoWrapper.TypeEnum getDataType() {
		return this.dataType;
	}	
	
	public boolean getEditEnable(){
		return this.editEnable;
	}
	
	
	/**
	 * ジョブツリー用のコンポジットを返します。
	 * 
	 * @return ジョブツリー用のコンポジット
	 */
	public JobMapTreeComposite getJobMapTreeComposite() {
		return m_jobTree;
	}

	/**
	 * ジョブツリーを表示します。
	 */
	public void show() {
	}

	/**
	 * 選択ジョブツリーアイテムを返します。
	 * 
	 * @return JobTreeItem 選択されたジョブツリーアイテム
	 */
	public JobTreeItemWrapper getSelectJobTreeItem() {
		JobTreeItemWrapper select = null;
		List<JobTreeItemWrapper> selectItemList = m_jobTree.getSelectItemList();
		if (selectItemList != null && !selectItemList.isEmpty()) {
			select = selectItemList.get(0);
		}
		
		return select;
	}
	
	public List<JobTreeItemWrapper> getSelectJobTreeItemList() {
		List<JobTreeItemWrapper> selectItemList = m_jobTree.getSelectItemList();
		return selectItemList;
	}

	/**
	 * コピー元ジョブツリーアイテムを取得します。
	 * 
	 * @return コピー元ジョブツリーアイテム
	 */
	public JobTreeItemWrapper getCopyJobTreeItem() {
		return m_copyJobTreeItem;
	}

	/**
	 * コピー元ジョブツリーアイテムを設定します。
	 * 
	 * @param copy コピー元ジョブツリーアイテム
	 */
	public void setCopyJobTreeItem(JobTreeItemWrapper copy) {
		m_copyJobTreeItem = copy;
	}

	/**
	 * ジョブツリーアイテムの更新フラグを返します。
	 * 
	 * @return 更新フラグ
	 */
	public boolean isUpdate() {
		return m_update;
	}

	/**
	 * ジョブツリーアイテムの更新フラグを設定します。
	 * 
	 * @param update 更新フラグ
	 */
	public void setUpdate(boolean update) {
		m_update = update;
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {

		// 選択されているスコープを取得する
		JobTreeItemWrapper item = getSelectedJobunitItem();
		
		// 選択されており、スコープの場合は値を返す
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		if (item != null) {
			String objectId = item.getData().getJobunitId();
			String objectType = HinemosModuleConstant.JOB;
			
			JobTreeItemWrapper manager = JobTreeItemUtil.getManager(item);
			String managerName = manager.getData().getId();
			
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {

		// 選択されているスコープを取得する
		JobTreeItemWrapper item = getSelectedJobunitItem();

		// 選択されており、スコープの場合は値を返す
		String ownerRoleId = null;
		if (item != null) {
			ownerRoleId = item.getData().getOwnerRoleId();
		}
		return ownerRoleId;
	}
	
	private JobTreeItemWrapper getSelectedJobunitItem() {
		JobMapTreeComposite tree = getJobMapTreeComposite();
		JobMapEditorView view = JobMapActionUtil.getJobMapEditorView();

		JobTreeItemWrapper item = null;
		if(tree.getTree().isFocusControl()){
			List<JobTreeItemWrapper> selectItemList = tree.getSelectItemList();
			if (selectItemList != null && !selectItemList.isEmpty()) {
				item = selectItemList.get(0);
			}
		} else{
			item = view.getFocusFigure().getJobTreeItem();
		}

		// 選択されており、ジョブユニットの場合は値を返す
		if (item != null && item.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
			return item;
		} else {
			return null;
		}
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}
}