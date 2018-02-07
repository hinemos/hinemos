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
import com.clustercontrol.infra.action.GetInfraFileManagerTableDefine;
import com.clustercontrol.infra.composite.InfraFileManagerComposite;
import com.clustercontrol.infra.view.action.AddInfraFileAction;
import com.clustercontrol.infra.view.action.DeleteInfraFileAction;
import com.clustercontrol.infra.view.action.DownloadInfraFileAction;
import com.clustercontrol.infra.view.action.InfraFileObjectPrivilegeAction;
import com.clustercontrol.infra.view.action.ModifyInfraFileAction;
import com.clustercontrol.infra.view.action.RefreshInfraFileManagerAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * 環境構築[ファイルマネージャ]ビュークラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraFileManagerView extends CommonViewPart implements ObjectPrivilegeTargetListView {
	/** ビューID */
	public static final String ID = InfraFileManagerView.class.getName();
	
	/** 環境構築[ファイルマネージャ]ビュー用のコンポジット */
	private InfraFileManagerComposite m_fileManager = null;

	/** 選択レコード数 */
	private int rowNum = 0;

	/**
	 * コンストラクタ
	 */
	public InfraFileManagerView() {
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

		m_fileManager = new InfraFileManagerComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_fileManager);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_fileManager.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		//ビューの更新
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

		Menu menu = menuManager.createContextMenu(m_fileManager.getTable());
		WidgetTestUtil.setTestId(this, null, menu);
		m_fileManager.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, m_fileManager.getTableViewer());
	}

	/**
	 * ビューを更新します。
	 *
	 * @see com.clustercontrol.infra.composite.InfraFileManagerComposite#update(String)
	 */
	public void update() {
		m_fileManager.update();
	}

	/**
	 * 環境構築[モジュール]ビュー用のコンポジットを返します。
	 *
	 * @return 環境構築[モジュール]ビュー用のコンポジット
	 */
	public InfraFileManagerComposite getComposite() {
		return m_fileManager;
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
			service.refreshElements(AddInfraFileAction.ID, null);
			service.refreshElements(ModifyInfraFileAction.ID, null);
			service.refreshElements(DeleteInfraFileAction.ID, null);
			service.refreshElements(DownloadInfraFileAction.ID, null);
			service.refreshElements(RefreshInfraFileManagerAction.ID, null);
			service.refreshElements(InfraFileObjectPrivilegeAction.ID, null);
			
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
		String managerName = (String)((List<?>)((StructuredSelection)getComposite().getTableViewer().getSelection()).getFirstElement()).get(GetInfraFileManagerTableDefine.MANAGER_NAME);
		String objectId = (String)((List<?>)((StructuredSelection)getComposite().getTableViewer().getSelection()).getFirstElement()).get(GetInfraFileManagerTableDefine.FILE_ID);
		String objectType = HinemosModuleConstant.INFRA_FILE;
		ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
		objectBeans.add(objectBean);
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		// アクションでテーブルアイテム一つだけが選択されていることを前提としている。
		return (String)((List<?>)((StructuredSelection)getComposite().getTableViewer().getSelection()).getFirstElement()).get(GetInfraFileManagerTableDefine.OWNER_ROLE);
	}
}