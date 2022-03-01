/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.view;

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
import com.clustercontrol.notify.action.GetCommandTemplateTableDefine;
import com.clustercontrol.notify.composite.CommandTemplateListComposite;
import com.clustercontrol.notify.view.action.CommandTemplateShowAction;
import com.clustercontrol.notify.view.action.ObjectPrivilegeCommandTemplateAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * 監視設定[コマンド通知テンプレート]ビュークラス<BR>
 *
 */
public class CommandTemplateListView extends CommonViewPart implements ObjectPrivilegeTargetListView {
	/** ログ */
	private static Log m_log = LogFactory.getLog(CommandTemplateListView.class);

	/** 監視設定[コマンド通知テンプレート]ビューID */
	public static final String ID = CommandTemplateListView.class.getName();

	/** コマンド通知テンプレート一覧コンポジット */
	private CommandTemplateListComposite composite = null;

	/** 選択アイテム数 */
	private int rowNum = 0;

	/**
	 * コンストラクタ
	 */
	public CommandTemplateListView() {
		super();
	}

	protected String getViewName() {
		return getClass().getName();
	}

	/**
	 * ViewPartへのコントロール作成処理<BR>
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

		composite = new CommandTemplateListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, composite);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		composite.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		// ボタン（アクション）を制御するリスナーを登録
		composite.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				IViewPart viewPart = page.findView(CommandTemplateListView.ID);

				//選択アイテムを取得
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				if ( viewPart != null && selection != null) {
					rowNum = selection.size();
					CommandTemplateListView view = (CommandTemplateListView) viewPart
							.getAdapter(CommandTemplateListView.class);
					if (view == null) {
						m_log.info("selection changed: view is null"); 
						return;
					}

					//ビューのボタン（アクション）の使用可/不可を設定する
					view.setEnabledAction(rowNum, event.getSelection());
				}
			}
		});
		update();
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
		Menu menu = menuManager.createContextMenu(composite.getTableViewer().getTable());
		composite.getTableViewer().getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, composite.getTableViewer() );
	}

	/**
	 * 追加コンポジットを返します。
	 *
	 * @return 追加コンポジット
	 */
	public Composite getListComposite() {
		return composite;
	}

	/**
	 * ビューを更新します。
	 */
	@Override
	public void update() {
		composite.update();
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param num 選択イベント数
	 * @param selection ボタン（アクション）を有効にするための情報
	 */
	public void setEnabledAction(int num, ISelection selection) {
		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(ObjectPrivilegeCommandTemplateAction.ID, null);
			service.refreshElements(CommandTemplateShowAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}

	public int getSelectedNum(){
		return rowNum;
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();
		String objectType = HinemosModuleConstant.PLATFORM_COMMAND_TEMPLATE;
		List<ObjectBean> objectBeans = new ArrayList<>();
		for (Object obj : selection.toArray()) {
			String managerName = (String) ((List<?>) obj).get(GetCommandTemplateTableDefine.MANAGER_NAME);
			String objectId = (String) ((List<?>) obj).get(GetCommandTemplateTableDefine.COMMAND_TEMPLATE_ID);
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();
		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetCommandTemplateTableDefine.OWNER_ROLE);
		}
		return id;
	}
}
