/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.view;

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
import com.clustercontrol.bean.Property;
import com.clustercontrol.sdml.action.GetSdmlControlSettingListTableDefine;
import com.clustercontrol.sdml.composite.SdmlControlSettingListComposite;
import com.clustercontrol.sdml.util.SdmlControlSettingFilterConstant;
import com.clustercontrol.sdml.view.action.ObjectPrivilegeSdmlControlSettingAction;
import com.clustercontrol.sdml.view.action.SdmlControlLogCollectorDisableAction;
import com.clustercontrol.sdml.view.action.SdmlControlLogCollectorEnableAction;
import com.clustercontrol.sdml.view.action.SdmlControlSettingCopyAction;
import com.clustercontrol.sdml.view.action.SdmlControlSettingDeleteAction;
import com.clustercontrol.sdml.view.action.SdmlControlSettingDisableAction;
import com.clustercontrol.sdml.view.action.SdmlControlSettingEnableAction;
import com.clustercontrol.sdml.view.action.SdmlControlSettingModifyAction;
import com.clustercontrol.sdml.view.action.SdmlControlSettingRefreshAction;
import com.clustercontrol.util.FilterPropertyUpdater;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * SDML設定[制御設定]ビュー
 */
public class SdmlControlSettingListView extends CommonViewPart implements ObjectPrivilegeTargetListView {
	private static Log logger = LogFactory.getLog(SdmlControlSettingListView.class);

	public static final String ID = SdmlControlSettingListView.class.getName();

	/** コンポジット */
	private SdmlControlSettingListComposite composite = null;

	/** 検索条件 */
	protected Property condition = null;

	/** 選択レコード数 */
	private int rowNum = 0;

	/**
	 * コンストラクタ
	 */
	public SdmlControlSettingListView() {
		super();
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		this.composite = new SdmlControlSettingListComposite(parent, SWT.NONE);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.composite.setLayoutData(gridData);

		// ポップアップメニュー作成
		createContextMenu();

		// ボタン（アクション）を制御するリスナーを追加
		this.composite.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart viewPart = page.findView(SdmlControlSettingListView.ID);

				// 選択アイテムを取得
				StructuredSelection selection = (StructuredSelection) event.getSelection();

				if (viewPart != null && selection != null) {
					SdmlControlSettingListView view = (SdmlControlSettingListView) viewPart
							.getAdapter(SdmlControlSettingListView.class);
					if (view == null) {
						logger.info("selectionChanged() : view is null");
						return;
					}

					// ビューのボタン（アクション）の使用可/不可を設定する
					view.setEnabledAction(selection.size(), event.getSelection());
				}
			}
		});

		this.update();
	}

	/**
	 * ポップアップメニュー作成
	 */
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(this.composite.getTable());
		WidgetTestUtil.setTestId(this, null, menu);

		this.composite.getTable().setMenu(menu);
		getSite().registerContextMenu(menuManager, this.composite.getTableViewer());
	}

	/**
	 * compositeを返します。
	 *
	 * @return composite
	 */
	public SdmlControlSettingListComposite getComposite() {
		return this.composite;
	}

	/**
	 * 検索条件にヒットした一覧を表示します。
	 * <p>
	 *
	 * conditionがnullの場合、全設定を表示します。
	 *
	 * @param condition
	 *            検索条件
	 */
	public void update(Property condition) {
		FilterPropertyUpdater.getInstance().addFilterProperty(getClass(), condition,
				SdmlControlSettingFilterConstant.MANAGER);
		this.condition = condition;

		this.update();
	}

	/**
	 * ビューを更新します。
	 * <p>
	 *
	 * 検索条件が事前に設定されている場合、その条件にヒットする設定の一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全設定を表示します。
	 */
	@Override
	public void update() {
		this.composite.update(this.condition);
	}

	/**
	 * 選択レコード数を返します。
	 * 
	 * @return rowNum
	 */
	public int getSelectedNum() {
		return this.rowNum;
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param num
	 *            選択イベント数
	 * @param selection
	 *            ボタン（アクション）を有効にするための情報
	 */
	public void setEnabledAction(int num, ISelection selection) {
		this.rowNum = num;

		// ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		if (null != service) {
			service.refreshElements(SdmlControlSettingModifyAction.ID, null);
			service.refreshElements(SdmlControlSettingDeleteAction.ID, null);
			service.refreshElements(SdmlControlSettingCopyAction.ID, null);
			service.refreshElements(SdmlControlSettingEnableAction.ID, null);
			service.refreshElements(SdmlControlSettingDisableAction.ID, null);
			service.refreshElements(SdmlControlLogCollectorEnableAction.ID, null);
			service.refreshElements(SdmlControlLogCollectorDisableAction.ID, null);
			service.refreshElements(ObjectPrivilegeSdmlControlSettingAction.ID, null);
			service.refreshElements(SdmlControlSettingRefreshAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after
			// updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);

		}
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();
		Object[] objs = selection.toArray();

		String managerName = null;
		String objectType = HinemosModuleConstant.SDML_CONTROL;
		String objectId = null;
		String objectIdLavelForDisplay = Messages.getString("application.id");
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>) obj).get(GetSdmlControlSettingListTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>) obj).get(GetSdmlControlSettingListTableDefine.APPLICATION_ID);
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId, objectId, objectIdLavelForDisplay);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetSdmlControlSettingListTableDefine.OWNER_ROLE);
		}
		return id;
	}
}
