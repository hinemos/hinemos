/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.infra.action.GetInfraManagementTableDefine;
import com.clustercontrol.infra.composite.InfraManagementComposite;
import com.clustercontrol.infra.view.action.AddInfraManagementAction;
import com.clustercontrol.infra.view.action.CheckInfraManagementAction;
import com.clustercontrol.infra.view.action.CopyInfraManagementAction;
import com.clustercontrol.infra.view.action.DeleteInfraManagementAction;
import com.clustercontrol.infra.view.action.DisableInfraManagementAction;
import com.clustercontrol.infra.view.action.EnableInfraManagementAction;
import com.clustercontrol.infra.view.action.RunInfraManagementAction;
import com.clustercontrol.infra.view.action.InfraObjectPrivilegeAction;
import com.clustercontrol.infra.view.action.ModifyInfraManagementAction;
import com.clustercontrol.infra.view.action.RefreshInfraManagementAction;
import com.clustercontrol.infra.view.action.UseNodePropManagementAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * 環境構築[構築・チェック]ビュークラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraManagementView extends CommonViewPart implements ObjectPrivilegeTargetListView {

	// ログ
	private static Log m_log = LogFactory.getLog( InfraManagementView.class );

	/** ビューID */
	public static final String ID = InfraManagementView.class.getName();
	/** 環境構築[構築・チェック]ビュー用のコンポジット */
	private InfraManagementComposite m_infraManagement = null;

	/** 選択レコード数 */
	private int rowNum = 0;
	
	/**
	 * コンストラクタ
	 */
	public InfraManagementView() {
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
	 * @see com.clustercontrol.view.AutoUpdateView#setInterval(int)
	 * @see com.clustercontrol.view.AutoUpdateView#startAutoReload()
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

		m_infraManagement = new InfraManagementComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_infraManagement);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_infraManagement.setLayoutData(gridData);
		m_infraManagement.setView(this);

		//ポップアップメニュー作成
		createContextMenu();

		//ビューを更新
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

		Menu menu = menuManager.createContextMenu(m_infraManagement.getTable());
		WidgetTestUtil.setTestId(this, null, menu);
		m_infraManagement.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, m_infraManagement.getTableViewer());
	}

	/**
	 * ビューを更新します。
	 *
	 * @see com.clustercontrol.infra.composite.InfraManagementComposite#update()
	 * @see com.clustercontrol.infra.composite.InfraManagementComposite#update(Property)
	 */
	@Override
	public void update() {
		try {
			m_infraManagement.update();
		} catch (Exception e) {
			m_log.warn("update(), " + e.getMessage(), e);
		}
	}

	/**
	 * 環境構築[構築・チェック]ビュー用のコンポジットを返します。
	 *
	 * @return 環境構築[構築・チェック]ビュー用のコンポジット
	 */
	public InfraManagementComposite getComposite() {
		return m_infraManagement;
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

			service.refreshElements(AddInfraManagementAction.ID, null);
			service.refreshElements(ModifyInfraManagementAction.ID, null);
			service.refreshElements(DeleteInfraManagementAction.ID, null);
			service.refreshElements(CopyInfraManagementAction.ID, null);
			service.refreshElements(RunInfraManagementAction.ID, null);
			service.refreshElements(CheckInfraManagementAction.ID, null);
			service.refreshElements(RefreshInfraManagementAction.ID, null);
			service.refreshElements(EnableInfraManagementAction.ID, null);
			service.refreshElements(DisableInfraManagementAction.ID, null);;
			service.refreshElements(InfraObjectPrivilegeAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}

	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		// アクションでテーブルアイテム一つだけが選択されていることを前提としている。
		String managerName = (String)((List<?>)((StructuredSelection)getComposite().getTableViewer().getSelection()).getFirstElement()).get(GetInfraManagementTableDefine.MANAGER_NAME);
		String objectId = (String)((List<?>)((StructuredSelection)getComposite().getTableViewer().getSelection()).getFirstElement()).get(GetInfraManagementTableDefine.MANAGEMENT_ID);
		String objectType = HinemosModuleConstant.INFRA;
		ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
		objectBeans.add(objectBean);
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		// アクションでテーブルアイテム一つだけが選択されていることを前提としている。
		return (String)((List<?>)((StructuredSelection)getComposite().getTableViewer().getSelection()).getFirstElement()).get(GetInfraManagementTableDefine.OWNER_ROLE);
	}

	public Integer getNodeInputType() {
		return UseNodePropManagementAction.getNodeInputType();
	}
}
