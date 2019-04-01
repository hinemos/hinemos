/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.utility.jobutil.ui.views.commands.ExportJobCommand;
import com.clustercontrol.utility.jobutil.ui.views.commands.ImportJobCommand;
import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.composite.JobListComposite;
import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.composite.action.JobListSelectionChangedListener;
import com.clustercontrol.jobmanagement.composite.action.JobTreeSelectionChangedListener;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.view.action.CreateApprovalJobAction;
import com.clustercontrol.jobmanagement.view.action.CreateFileJobAction;
import com.clustercontrol.jobmanagement.view.action.CreateJobAction;
import com.clustercontrol.jobmanagement.view.action.CreateJobNetAction;
import com.clustercontrol.jobmanagement.view.action.CreateJobUnitAction;
import com.clustercontrol.jobmanagement.view.action.CreateMonitorJobAction;
import com.clustercontrol.jobmanagement.view.action.CreateReferJobAction;
import com.clustercontrol.jobmanagement.view.action.DeleteJobAction;
import com.clustercontrol.jobmanagement.view.action.EditModeAction;
import com.clustercontrol.jobmanagement.view.action.JobObjectPrivilegeAction;
import com.clustercontrol.jobmanagement.view.action.ModifyJobAction;
import com.clustercontrol.jobmanagement.view.action.RunJobAction;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブ[一覧]ビュークラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class JobListView extends CommonViewPart implements ObjectPrivilegeTargetListView {
	// ログ
	private static Log m_log = LogFactory.getLog( JobListView.class );

	/** ビューID */
	public static final String ID = JobListView.class.getName();
	/** サッシュ */
	private SashForm m_sash = null;
	/** ジョブツリー用コンポジット */
	private JobTreeComposite m_jobTree = null;
	/** ジョブ[一覧]ビュー用のコンポジット */
	private JobListComposite m_jobList = null;
	/** ジョブツリーアイテム */
	private JobTreeItem m_copyJobTreeItem = null;

	/** Last focus composite */
	private Composite lastFocusComposite = null;

	/**
	 * Number of selected items
	 */
	private int selectedNum;
	private int dataType;
	private boolean editEnable;

	/**
	 * コンストラクタ
	 */
	public JobListView() {
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

		m_sash = new SashForm(parent, SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "sashform", m_sash);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_sash.setLayoutData(gridData);

		Long start = System.currentTimeMillis();
		//ジョブ階層ツリー作成
		m_jobTree = new JobTreeComposite(m_sash, SWT.NONE, null);
		WidgetTestUtil.setTestId(this, "jobtree", m_jobTree);

		//ジョブ一覧作成
		m_jobList = new JobListComposite(m_sash, SWT.NONE);
		WidgetTestUtil.setTestId(this, "joblist", m_jobList);

		//Sashの境界を調整 左部30% 右部70%
		m_sash.setWeights(new int[] { 30, 70 });

		m_jobTree.getTreeViewer().addSelectionChangedListener(new JobTreeSelectionChangedListener());
		m_jobList.getTableViewer().addSelectionChangedListener(new JobListSelectionChangedListener(m_jobList));

		//ポップアップメニュー作成
		createContextMenu();

		// Initialize dataType
		JobTreeItem select = getSelectJobTreeItemList().get(0);
		dataType = select == null ? JobConstant.TYPE_COMPOSITE : select.getData().getType();

		Long end = System.currentTimeMillis();

		m_log.info("init() : " + (end - start) + "ms");

		m_jobTree.addToTreeViewerList();
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
		Menu treeMenu = menuManager.createContextMenu(m_jobTree.getTree());
		WidgetTestUtil.setTestId(this, null, treeMenu);
		m_jobTree.getTree().setMenu(treeMenu);
		menuManager.addMenuListener( new IMenuListener(){
			@Override
			public void menuAboutToShow( IMenuManager manager ){
				StructuredSelection selection = (StructuredSelection) m_jobTree.getTreeViewer().getSelection();
				if( selection != null) {
					// Do the same as JobTreeSelectionChangedListenser
					Object selectObject = selection.getFirstElement();
					List<?> list = selection.toList();
					List<JobTreeItem> itemList = new ArrayList<JobTreeItem>();
					for(Object obj : list) {
						if(obj instanceof JobTreeItem) {
							itemList.add((JobTreeItem)obj);
						}
					}

					// Set last focus
					JobTreeComposite composite = getJobTreeComposite();
					if( composite != null && composite.getTree().isFocusControl() ){
						setLastFocusComposite( composite );
					}
					setEnabledAction( selectObject, itemList, true );
				}
			}
		} );

		getSite().registerContextMenu( menuManager, this.m_jobTree.getTreeViewer() );

		menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		Menu listMenu = menuManager.createContextMenu(m_jobList.getTable());
		WidgetTestUtil.setTestId(this, null, listMenu);
		m_jobList.getTable().setMenu(listMenu);
		getSite().registerContextMenu( menuManager, this.m_jobList.getTableViewer() );
	}

	/**
	 * データタイプを返します。
	 * @return
	 */
	public int getDataType() {
		return this.dataType;
	}

	/**
	 * ビューを更新します。(Hinemosマネージャから再取得します。)
	 *
	 * @see com.clustercontrol.jobmanagement.composite.JobTreeComposite#update()
	 * @see com.clustercontrol.jobmanagement.composite.JobTreeComposite#getSelectItemList()
	 * @see com.clustercontrol.jobmanagement.composite.JobListComposite#update(JobTreeItem)
	 */
	@Override
	public void update() {
		Long start = System.currentTimeMillis();
		JobEditStateUtil.releaseAll();

		m_jobTree.update();
		List<JobTreeItem> itemList = this.getJobTreeComposite().getSelectItemList();
		for(JobTreeItem item : itemList) {
			m_jobList.update(item);
		}
		Long end = System.currentTimeMillis();
		m_log.info("update () : " + (end - start) + "ms");
	}

	/**
	 * 指定されたジョブIDに該当するジョブツリーをフォーカスする。(Hinemosマネージャから再取得せず、キャッシュを利用します。)
	 * @param jobId
	 */
	public void setFocus(String managerName, String jobunitId, String jobId){
		m_jobTree.setFocus(managerName, jobunitId, jobId);
	}

	/**
	 * Get the number of selected items
	 * @return
	 */
	public int getSelectedNum(){
		return this.selectedNum;
	}

	public boolean getEditEnable(){
		return this.editEnable;
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param type ジョブ種別
	 * @param selection ボタン（アクション）を有効にするための情報
	 *
	 * @see com.clustercontrol.bean.JobConstant
	 */
	public void setEnabledAction(Object selectObject, List<JobTreeItem> itemList, boolean updateList ){
		if (selectObject instanceof JobTreeItem) {
			JobTreeItem selectJobTreeItem = (JobTreeItem)selectObject;
			// 選択ツリーアイテムを設定
			getJobListComposite().setSelectJobTreeItemList( itemList );

			int type = selectJobTreeItem.getData().getType();
			String jobunitId = selectJobTreeItem.getData().getJobunitId();

			// ログインユーザで参照可能なジョブユニットかどうかチェックする
			if( type == JobConstant.TYPE_JOBUNIT ){
				setEnabledActionAll( true );
			}

			// 選択ツリーアイテムを設定し、ジョブ一覧を更新
			if(updateList){
				getJobListComposite().update(selectJobTreeItem);
			}

			// ビューのアクションの有効/無効を設定
			setEnabledAction( type, jobunitId);
		} else {
			// ジョブ一覧をクリア
			if(updateList){
				getJobListComposite().update(null);
			}

			//ビューのアクションを全て無効に設定
			setEnabledAction(-9, "");
		}
	}

	private void setEnabledAction( int type, String jobunitId ){
		this.dataType = type;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			List<JobTreeItem> tree = getSelectJobTreeItemList();
			boolean isEditable = true;
			for(JobTreeItem item : tree) {
				if(item == null) {
					isEditable = false;
					break;
				}
				String managerName = JobTreeItemUtil.getManagerName(item);
				if( null != managerName ){
					isEditable = isEditable && JobEditStateUtil.getJobEditState(managerName).isLockedJobunitId(item.getData().getJobunitId());
				}else{
					isEditable = false;
					break;
				}
			}
			editEnable = isEditable;

			service.refreshElements(CreateJobUnitAction.ID, null);
			service.refreshElements(CreateJobNetAction.ID, null);
			service.refreshElements(CreateJobAction.ID, null);
			service.refreshElements(CreateFileJobAction.ID, null);
			service.refreshElements(CreateReferJobAction.ID, null);
			service.refreshElements(CreateApprovalJobAction.ID, null);
			service.refreshElements(CreateMonitorJobAction.ID, null);
			service.refreshElements(DeleteJobAction.ID, null);
			service.refreshElements(ModifyJobAction.ID, null);
			service.refreshElements(JobObjectPrivilegeAction.ID, null);
			service.refreshElements(RunJobAction.ID, null);
			service.refreshElements(EditModeAction.ID, null);
			service.refreshElements(ImportJobCommand.ID, null);
			service.refreshElements(ExportJobCommand.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);

		}

	}

	/**
	 * ジョブツリー用のコンポジットを返します。
	 *
	 * @return ジョブツリー用のコンポジット
	 */
	public JobTreeComposite getJobTreeComposite() {
		return m_jobTree;
	}

	/**
	 * ジョブ[一覧]ビュー用のコンポジットを返します。
	 *
	 * @return ジョブ[一覧]ビュー用のコンポジット
	 */
	public JobListComposite getJobListComposite() {
		return m_jobList;
	}

	/**
	 * ジョブツリーを表示します。
	 */
	public void show() {
		m_sash.setMaximizedControl(null);
	}

	/**
	 * ジョブツリーを非表示にします。
	 */
	public void hide() {
		m_sash.setMaximizedControl(m_jobList);
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

	/**
	 *  最後にフォーカスしたコンポジットを返します。
	 * @return composite
	 */
	public Composite getLastFocusComposite() {
		return this.lastFocusComposite;
	}

	/**
	 * 選択ジョブツリーアイテムを返します。
	 *
	 * @return JobTreeItem 選択されたジョブツリーアイテム
	 */
	public List<JobTreeItem> getSelectJobTreeItemList() {
		List<JobTreeItem> items = new ArrayList<JobTreeItem>();

		if(this.lastFocusComposite instanceof JobTreeComposite) {
			items = m_jobTree.getSelectItemList();
		} else if( this.lastFocusComposite instanceof JobListComposite ) {
			items = m_jobList.getSelectItemList();
		}

		if(items.isEmpty()) {
			items = new ArrayList<JobTreeItem>();
			items.add(null);
		}

		return items;
	}

	/**
	 * コピー元ジョブツリーアイテムを取得します。
	 *
	 * @return コピー元ジョブツリーアイテム
	 */
	public JobTreeItem getCopyJobTreeItem() {
		return m_copyJobTreeItem;
	}

	/**
	 * コピー元ジョブツリーアイテムを設定します。
	 *
	 * @param copy コピー元ジョブツリーアイテム
	 */
	public void setCopyJobTreeItem(JobTreeItem copy) {
		m_copyJobTreeItem = copy;
	}

	@Override
	public void dispose() {
		m_jobTree.removeFromTreeViewerList();
		super.dispose();
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {

		// 選択されているスコープを取得する
		JobTreeItem item = getSelectedJobunitItem();

		// 選択されており、スコープの場合は値を返す
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		if (item != null) {
			JobTreeItem manager = JobTreeItemUtil.getManager(item);
			String managerName = manager.getData().getId();
			String objectId = item.getData().getJobunitId();
			String objectType = HinemosModuleConstant.JOB;
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {

		// 選択されているスコープを取得する
		JobTreeItem item = getSelectedJobunitItem();

		// 選択されており、スコープの場合は値を返す
		String ownerRoleId = null;
		if (item != null) {
			ownerRoleId = item.getData().getOwnerRoleId();
		}
		return ownerRoleId;
	}

	private JobTreeItem getSelectedJobunitItem() {
		JobTreeComposite tree = getJobTreeComposite();
		WidgetTestUtil.setTestId(this, "tree", tree);
		JobListComposite list = getJobListComposite();
		WidgetTestUtil.setTestId(this, "list", list);

		JobTreeItem item = null;
		if(this.lastFocusComposite instanceof JobTreeComposite){
			item = tree.getSelectItemList().get(0);
		}else if(this.lastFocusComposite instanceof JobListComposite){
			item = list.getSelectItemList().get(0);
		}else{
			// 該当項目なし
		}

		// 選択されており、ジョブユニットの場合は値を返す
		if (item != null && item.getData().getType() == JobConstant.TYPE_JOBUNIT) {
			return item;
		} else {
			return null;
		}
	}
}
