/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.jobmanagement.action.GetJobLinkSendSettingTableDefine;
import com.clustercontrol.jobmanagement.composite.JobLinkSendSettingListComposite;
import com.clustercontrol.jobmanagement.view.action.CopyJobLinkSendSettingAction;
import com.clustercontrol.jobmanagement.view.action.DeleteJobLinkSendSettingAction;
import com.clustercontrol.jobmanagement.view.action.JobLinkSendSettingRefreshAction;
import com.clustercontrol.jobmanagement.view.action.ManualSendJobLinkMessageAction;
import com.clustercontrol.jobmanagement.view.action.ModifyJobLinkSendSettingAction;
import com.clustercontrol.jobmanagement.view.action.ObjectPrivilegeJobLinkSendSettingAction;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;


/**
 * ジョブ設定[ジョブ連携送信設定]ビュークラスです。
 *
 */
public class JobLinkSendSettingListView extends CommonViewPart implements ObjectPrivilegeTargetListView {

	// ログ
	private static Log m_log = LogFactory.getLog( JobHistoryView.class );

	/** ビューID */
	public static final String ID = JobLinkSendSettingListView.class.getName();

	/** ジョブ設定[ジョブ連携送信設定]ビュー用のコンポジット */
	private JobLinkSendSettingListComposite m_composite = null;

	/** 選択レコード数 */
	private int rowNum;

	/**
	 * コンストラクタ
	 */
	public JobLinkSendSettingListView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ビューを構築します。
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_composite = new JobLinkSendSettingListComposite(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_composite.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		// ボタン（アクション）を制御するリスナーを登録
		this.m_composite.getTableViewer().addSelectionChangedListener(
				new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						// ジョブ連携送信情報[一覧]ビューのインスタンスを取得
						IWorkbenchPage page = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage();
						IViewPart viewPart = page.findView(JobLinkSendSettingListView.ID);
						//選択アイテムを取得
						StructuredSelection selection = (StructuredSelection) event.getSelection();
						if (viewPart != null && selection != null) {
							JobLinkSendSettingListView view 
								= (JobLinkSendSettingListView)viewPart.getAdapter(JobLinkSendSettingListView.class);
							if (view == null) {
								m_log.info("selection changed: view is null"); 
								return;
							}
							//ビューのボタン（アクション）の使用可/不可を設定する
							view.setEnabledAction(selection.size(), event.getSelection());
						}
					}
				});
		this.update();
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
		Menu menu = menuManager.createContextMenu(m_composite.getTable());
		m_composite.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, m_composite.getTableViewer() );
	}

	/**
	 * コンポジットを返します。
	 *
	 * @return コンポジット
	 */
	public Composite getComposite() {
		return this.m_composite;
	}

	/**
	 * ビューを更新します。
	 * 
	 */
	@Override
	public void update() {
		this.m_composite.update();
	}

	/**
	 * 選択レコード数を返します。
	 * @return rowNum
	 */
	public int getSelectedNum(){
		return this.rowNum;
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param num 選択イベント数
	 * @param selection ボタン（アクション）を有効にするための情報
	 */
	public void setEnabledAction(int num, ISelection selection) {
		this.rowNum = num;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(ObjectPrivilegeJobLinkSendSettingAction.ID, null);
			service.refreshElements(CopyJobLinkSendSettingAction.ID, null);
			service.refreshElements(DeleteJobLinkSendSettingAction.ID, null);
			service.refreshElements(ModifyJobLinkSendSettingAction.ID, null);
			service.refreshElements(JobLinkSendSettingRefreshAction.ID, null);
			service.refreshElements(ManualSendJobLinkMessageAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);

		}
	}

	public String getSelectedId() {
		StructuredSelection selection = (StructuredSelection) this.m_composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetJobLinkSendSettingTableDefine.JOBLINK_SEND_SETTING_ID);
		}
		return id;
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) this.m_composite.getTableViewer().getSelection();
		Object [] objs = selection.toArray();

		String managerName = null;
		String objectType = HinemosModuleConstant.JOB_LINK_SEND;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			objectId = (String) ((List<?>)obj).get(GetJobLinkSendSettingTableDefine.JOBLINK_SEND_SETTING_ID);
			managerName = (String) ((List<?>)obj).get(GetJobLinkSendSettingTableDefine.MANAGER_NAME);
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) this.m_composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetJobLinkSendSettingTableDefine.OWNER_ROLE);
		}
		return id;
	}
}
