/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.view;
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
import com.clustercontrol.notify.restaccess.action.GetRestAccessInfoListTableDefine;
import com.clustercontrol.notify.restaccess.composite.RestAccessInfoListComposite;
import com.clustercontrol.notify.restaccess.view.action.ObjectPrivilegeRestAccessInfoListAction;
import com.clustercontrol.notify.restaccess.view.action.RestAccessInfoAddAction;
import com.clustercontrol.notify.restaccess.view.action.RestAccessInfoCopyAction;
import com.clustercontrol.notify.restaccess.view.action.RestAccessInfoDeleteAction;
import com.clustercontrol.notify.restaccess.view.action.RestAccessInfoModifyAction;
import com.clustercontrol.notify.restaccess.view.action.RestAccessInfoRefreshAction;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * RESTアクセス情報[一覧]ビュークラス<BR>
 *
 */
public class RestAccessInfoListView extends CommonViewPart implements ObjectPrivilegeTargetListView {

	/** ログ */
	private static Log m_log = LogFactory.getLog(RestAccessInfoListView.class);

	/** RESTアクセス情報[一覧]ビューID */
	public static final String ID = RestAccessInfoListView.class.getName();

	/** RESTアクセス情報一覧コンポジット */
	private RestAccessInfoListComposite composite = null;

	/** 選択レコード数 */
	private int rowNum = 0;

	/**
	 * コンストラクタ
	 */
	public RestAccessInfoListView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
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

		composite = new RestAccessInfoListComposite(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.composite.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		// ボタン（アクション）を制御するリスナーを登録
		this.composite.getTableViewer().addSelectionChangedListener(
				new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						//RESTアクセス[一覧]ビューのインスタンスを取得
						IWorkbenchPage page = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage();
						IViewPart viewPart = page.findView(RestAccessInfoListView.ID);

						//選択アイテムを取得
						StructuredSelection selection = (StructuredSelection) event.getSelection();

						if ( viewPart != null && selection != null) {
							RestAccessInfoListView view = (RestAccessInfoListView) viewPart.getAdapter(RestAccessInfoListView.class);

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
		Menu menu = menuManager.createContextMenu(composite.getTable());
		composite.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, this.composite.getTableViewer() );
	}

	/**
	 * 追加コンポジットを返します。
	 *
	 * @return 追加コンポジット
	 */
	public Composite getListComposite() {
		return this.composite;
	}

	/**
	 * 検索条件にヒットした監視設定の一覧を表示します。
	 * <p>
	 *
	 * conditionがnullの場合、全監視設定を表示します。
	 *
	 * @param condition
	 *            検索条件
	 */
	public void update(Property condition) {
		this.update();
	}

	/**
	 * ビューを更新します。
	 * <p>
	 *
	 * 検索条件が事前に設定されている場合、その条件にヒットする監視設定の一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全監視設定を表示します。
	 */
	@Override
	public void update() {
		this.composite.update();
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
			service.refreshElements(ObjectPrivilegeRestAccessInfoListAction.ID, null);
			service.refreshElements(RestAccessInfoCopyAction.ID, null);
			service.refreshElements(RestAccessInfoDeleteAction.ID, null);
			service.refreshElements(RestAccessInfoModifyAction.ID, null);
			service.refreshElements(RestAccessInfoAddAction.ID, null);
			service.refreshElements(RestAccessInfoRefreshAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);

		}
	}

	public String getSelectedId() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetRestAccessInfoListTableDefine.REST_ACCESS_ID);
		}
		return id;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetRestAccessInfoListTableDefine.OWNER_ROLE);
		}
		return id;
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();
		Object [] objs = selection.toArray();

		String managerName = null;
		String objectType = HinemosModuleConstant.PLATFORM_REST_ACCESS;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>)obj).get(GetRestAccessInfoListTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>)obj).get(GetRestAccessInfoListTableDefine.REST_ACCESS_ID);
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}
}
