/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.view;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.composite.InfraModuleComposite;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.infra.view.action.AddInfraModuleAction;
import com.clustercontrol.infra.view.action.CheckInfraModuleAction;
import com.clustercontrol.infra.view.action.CopyInfraModuleAction;
import com.clustercontrol.infra.view.action.DeleteInfraModuleAction;
import com.clustercontrol.infra.view.action.DisableInfraModuleAction;
import com.clustercontrol.infra.view.action.DownOrderInfraModuleAction;
import com.clustercontrol.infra.view.action.EnableInfraModuleAction;
import com.clustercontrol.infra.view.action.ReviewCheckStatusAction;
import com.clustercontrol.infra.view.action.RunInfraModuleAction;
import com.clustercontrol.infra.view.action.ModifyInfraModuleAction;
import com.clustercontrol.infra.view.action.UpOrderInfraModuleAction;
import com.clustercontrol.infra.view.action.UseNodePropModuleAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.InfraManagementDuplicate_Exception;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.infra.InfraManagementNotFound_Exception;
import com.clustercontrol.ws.infra.InfraModuleInfo;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidSetting_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;
import com.clustercontrol.ws.infra.NotifyDuplicate_Exception;
import com.clustercontrol.ws.infra.NotifyNotFound_Exception;

/**
 * 環境構築[モジュール]ビュークラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraModuleView extends CommonViewPart {
	// ログ
	private static Log m_log = LogFactory.getLog( InfraModuleView.class );

	/** ビューID */
	public static final String ID = InfraModuleView.class.getName();
	/** 環境構築[モジュール]ビュー用のコンポジット */
	private InfraModuleComposite m_module = null;
	private String orgViewName = null;

	/**
	 * コンストラクタ
	 */
	public InfraModuleView() {
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
	 * @see #update(String)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_module = new InfraModuleComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_module);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_module.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		//ビューの更新
		this.update(null, null);
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

		Menu menu = menuManager.createContextMenu(m_module.getTable());
		WidgetTestUtil.setTestId(this, null, menu);
		m_module.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, m_module.getTableViewer());
		orgViewName = this.getPartName();
	}

	/**
	 * ビューを更新します。
	 *
	 * @param managerName マネージャ名
	 * @param managementId 構築ID
	 *
	 * @see com.clustercontrol.infra.composite.InfraModuleComposite#update(String)
	 */
	public void update(String managerName, String managementId) {
		m_module.update(managerName, managementId);

		if(managerName == null || managerName.equals("")) {
			return;
		}
		String viewName = orgViewName + "(" + managerName + ")";
		setPartName(viewName);
	}

	/**
	 * 環境構築[モジュール]ビュー用のコンポジットを返します。
	 *
	 * @return 環境構築[モジュール]ビュー用のコンポジット
	 */
	public InfraModuleComposite getComposite() {
		return m_module;
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param num 選択イベント数
	 * @param selection ボタン（アクション）を有効にするための情報
	 */
	public void setEnabledAction() {
		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){

			service.refreshElements(AddInfraModuleAction.ID, null);
			service.refreshElements(ModifyInfraModuleAction.ID, null);
			service.refreshElements(DeleteInfraModuleAction.ID, null);
			service.refreshElements(CopyInfraModuleAction.ID, null);
			service.refreshElements(RunInfraModuleAction.ID, null);
			service.refreshElements(CheckInfraModuleAction.ID, null);
			service.refreshElements(EnableInfraModuleAction.ID, null);
			service.refreshElements(DisableInfraModuleAction.ID, null);
			service.refreshElements(UpOrderInfraModuleAction.ID, null);
			service.refreshElements(DownOrderInfraModuleAction.ID, null);
			service.refreshElements(ReviewCheckStatusAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}

	public boolean isUseNodeProp() {
		return UseNodePropModuleAction.isChecked();
	}

	private void selectItem( int selectionIndex ){
		getComposite().getTable().setFocus();
		getComposite().getTable().select( selectionIndex );
		// Refresh the toolbar
		setEnabledAction();
	}

	/**
	 * Move an module order up
	 */
	public void moveUp(){
		TableViewer tableViewer = getComposite().getTableViewer();

		StructuredSelection selection = null;
		if(tableViewer.getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) tableViewer.getSelection();
		}

		String moduleId = null;
		if(selection != null){
			moduleId = (String) ((ArrayList<?>)selection.getFirstElement()).get(GetInfraModuleTableDefine.MODULE_ID);
		}

		int selectionIndex = getComposite().getTable().getSelectionIndex();
		String managerName = getComposite().getManagerName();
		String managementId= getComposite().getManagementId();

		InfraManagementInfo management = null;
		InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
		try {
			management = wrapper.getInfraManagement(managementId);
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception | InfraManagementNotFound_Exception e) {
			m_log.debug("execute getInfraManagement, " + e.getMessage());
		}

		InfraModuleInfo module = null;
		if(management != null && management.getModuleList() != null){
			for(InfraModuleInfo tmpModule: management.getModuleList()){
				if(tmpModule.getModuleId().equals(moduleId)){
					module = tmpModule;
					break;
				}
			}

			if(module != null){
				int index = management.getModuleList().indexOf(module);
				if( index > 0 ){
					InfraModuleInfo tmpModule = management.getModuleList().get(index - 1);
					management.getModuleList().set(index, tmpModule);
					management.getModuleList().set(index - 1, module);

					try {
						wrapper.modifyInfraManagement(management);
					} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | InvalidSetting_Exception | NotifyDuplicate_Exception | NotifyNotFound_Exception | InfraManagementNotFound_Exception | InfraManagementDuplicate_Exception e) {
						m_log.debug("execute modifyInfraManagement, " + e.getMessage());
					}
				}
			}

			update(managerName, managementId);

			// 更新後に再度選択項目にフォーカスをあてる
			selectItem(selectionIndex - 1);
		}
	}

	/**
	 * Move an module order down
	 */
	public void moveDown(){
		TableViewer tableViewer = getComposite().getTableViewer();

		StructuredSelection selection = null;
		if(tableViewer.getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) tableViewer.getSelection();
		}

		String moduleId = null;
		if(selection != null){
			moduleId = (String) ((ArrayList<?>)selection.getFirstElement()).get(GetInfraModuleTableDefine.MODULE_ID);
		}

		int selectionIndex = getComposite().getTable().getSelectionIndex();
		String managerName = getComposite().getManagerName();
		String managementId= getComposite().getManagementId();

		InfraManagementInfo management = null;
		InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(managerName);
		try {
			management = wrapper.getInfraManagement(managementId);
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception | InfraManagementNotFound_Exception e) {
			m_log.debug("execute getInfraManagement, " + e.getMessage());
		}

		InfraModuleInfo module = null;
		if(management != null && management.getModuleList() != null){
			for(InfraModuleInfo tmpModule: management.getModuleList()){
				if(tmpModule.getModuleId().equals(moduleId)){
					module = tmpModule;
					break;
				}
			}

			if(module != null){
				int index = management.getModuleList().indexOf(module);
				if(index < management.getModuleList().size() - 1){
					InfraModuleInfo tmpModule = management.getModuleList().get(index + 1);
					management.getModuleList().set(index, tmpModule);
					management.getModuleList().set(index + 1, module);

					try {
						wrapper.modifyInfraManagement(management);
					} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | InvalidSetting_Exception | NotifyDuplicate_Exception | NotifyNotFound_Exception | InfraManagementNotFound_Exception | InfraManagementDuplicate_Exception e) {
						m_log.debug("execute modifyInfraManagement, " + e.getMessage());
					}
				}
			}

			update(managerName, managementId);

			// 更新後に再度選択項目にフォーカスをあてる
			selectItem(selectionIndex + 1);
		}
	}
}
