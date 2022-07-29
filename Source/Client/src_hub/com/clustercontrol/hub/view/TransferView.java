/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.hub.action.GetTransferTableDefine;
import com.clustercontrol.hub.composite.TransferComposite;
import com.clustercontrol.hub.view.action.LogTransferCopyAction;
import com.clustercontrol.hub.view.action.LogTransferDeleteAction;
import com.clustercontrol.hub.view.action.LogTransferDisableAction;
import com.clustercontrol.hub.view.action.LogTransferEnableAction;
import com.clustercontrol.hub.view.action.LogTransferModifyAction;
import com.clustercontrol.hub.view.action.ObjectPrivilegeLogTransferAction;
import com.clustercontrol.hub.view.action.RefreshHubAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * 収集蓄積[転送]ビュークラス<BR>
 *
 */
public class TransferView extends CommonViewPart implements ObjectPrivilegeTargetListView {
	public static final String ID = TransferView.class.getName();
	private TransferComposite transferComposite = null;
	private Composite transferViewParentComposite = null;

	/**
	 * Number of selected items
	 */
	private int selectedNum;

	/**
	 * コンストラクタ
	 */
	public TransferView() {
		super();
	}

	/**
	 * ViewPartへのコントロール作成処理<BR>
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		transferViewParentComposite = parent;

		transferComposite = new TransferComposite(transferViewParentComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, transferComposite);

		//ポップアップメニュー作成
		createContextMenu();

		//ビューを更新
		this.update();
	}

	/**
	 * ポップアップメニュー作成<BR>
	 *
	 *
	 */
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu ( this.transferComposite.getTable() );
		WidgetTestUtil.setTestId(this, null, menu);
		this.transferComposite.getTable().setMenu(menu);

		getSite().registerContextMenu( menuManager, this.transferComposite.getTableViewer() );
	}

	/**
	 * 収集蓄積[転送]ビュー更新<BR>
	 */
	@Override
	public void update() {
		transferComposite.update();
	}

	
	
	public TransferComposite getLogTransferComposite() {
		return transferComposite;
	}

	public List<String> getSelectedIdList() {
		StructuredSelection selection = (StructuredSelection) this.transferComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String id = null;
		List<String> idList = new ArrayList<String>();
		if (list != null) {
			for(Object obj : list) {
				@SuppressWarnings("unchecked")
				List<String> objList = (List<String>)obj;
				id = objList.get(GetTransferTableDefine.TRANSFER_ID);
				idList.add(id);
			}
		}
		return idList;
	}

	public List<String> getSelectedManagerNameList() {
		StructuredSelection selection = (StructuredSelection) this.transferComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String managerName = null;
		List<String> managerList = new ArrayList<String>();
		if (list != null) {
			for(Object obj : list) {
				@SuppressWarnings("unchecked")
				List<String> objList = (List<String>)obj;
				managerName = objList.get(GetTransferTableDefine.MANAGER_NAME);
				managerList.add(managerName);
			}
		}
		return managerList;
	}

	public Map<String, List<String>> getSelectedItem() {
		StructuredSelection selection = (StructuredSelection) this.transferComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		String transferId = null;
		String managerName = null;
		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		if (list != null) {
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				managerName = (String)objList.get(GetTransferTableDefine.MANAGER_NAME);
				if(map.get(managerName) == null){
					map.put(managerName, new ArrayList<String>());
				}
			}
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				transferId = (String)objList.get(GetTransferTableDefine.TRANSFER_ID);
				managerName = (String)objList.get(GetTransferTableDefine.MANAGER_NAME);

				map.get(managerName).add(transferId);
			}
		}
		return map;
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) this.transferComposite.getTableViewer().getSelection();
		Object [] objs = selection.toArray();

		String managerName = null;
		String objectType = HinemosModuleConstant.HUB_TRANSFER;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>)obj).get(GetTransferTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>)obj).get(GetTransferTableDefine.TRANSFER_ID);
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) this.transferComposite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetTransferTableDefine.OWNER_ROLE);
		}
		return id;
	}

	/**
	 * Get the number of selected items
	 * @return
	 */
	public int getSelectedNum(){
		return this.selectedNum;
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param num 選択イベント数
	 * @param selection ボタン（アクション）を有効にするための情報
	 */
	public void setEnabledAction( int num, ISelection selection ){
		this.selectedNum = num;

		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(LogTransferModifyAction.ID, null);
			service.refreshElements(LogTransferDeleteAction.ID, null);
			service.refreshElements(LogTransferCopyAction.ID, null);
			service.refreshElements(LogTransferEnableAction.ID, null);
			service.refreshElements(LogTransferDisableAction.ID, null);
			service.refreshElements(ObjectPrivilegeLogTransferAction.ID, null);
			service.refreshElements(RefreshHubAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}
}
