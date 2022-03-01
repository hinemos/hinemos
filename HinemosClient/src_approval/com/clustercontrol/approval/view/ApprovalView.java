/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.approval.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.approval.bean.ApprovalFilterPropertyConstant;
import com.clustercontrol.approval.composite.ApprovalComposite;
import com.clustercontrol.approval.view.action.ApprovalDetailAction;
import com.clustercontrol.approval.view.action.ApprovalFilterAction;
import com.clustercontrol.approval.view.action.ApprovalRefreshAction;
import com.clustercontrol.bean.Property;
import com.clustercontrol.client.swt.SWT;
import com.clustercontrol.util.FilterPropertyUpdater;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;

/**
 * 承認ビュークラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class ApprovalView extends CommonViewPart {

	// ログ
	private static Log m_log = LogFactory.getLog( ApprovalView.class );
	
	/** 承認ビューID */
	public static final String ID = ApprovalView.class.getName();

	/** 承認一覧コンポジット */
	private ApprovalComposite composite = null;
	/** フィルタ条件 */
	private Property condition = null;
	/** 選択レコード数 */
	private int rowNum = 0;

	/**
	 * コンストラクタ
	 */
	public ApprovalView() {
		super();
	}

	@Override
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

		composite = new ApprovalComposite(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		composite.setLayoutData(gridData);

		//ポップアップメニュー作成
		createContextMenu();

		// ボタン（アクション）を制御するリスナーを登録
		this.composite.getTableViewer().addSelectionChangedListener(
			new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					//承認[一覧]ビューのインスタンスを取得
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					IViewPart viewPart = page.findView(ApprovalView.ID);
					//選択アイテムを取得
					StructuredSelection selection = (StructuredSelection) event.getSelection();
					if ( viewPart != null && selection != null) {
						ApprovalView view = (ApprovalView) viewPart.getAdapter(ApprovalView.class);
						if (view == null) {
							m_log.info("selection changed: view is null");
							return;
						}
						//ビューのボタン（アクション）の使用可/不可を設定する
						view.setEnabledAction(selection.size(), event.getSelection());
					}
				}
			});
		//ビューの更新
		this.update();
	}

	/**
	 * フィルタ条件を返します。
	 *
	 * @return フィルタ条件
	 */
	public Property getFilterCondition() {
		return condition;
	}

	/**
	 * フィルタ条件を設定します。
	 *
	 * @param condition フィルタ条件
	 */
	public void setFilterCondition(Property condition) {
		FilterPropertyUpdater.getInstance().addFilterProperty(getClass(), condition,
				ApprovalFilterPropertyConstant.MANAGER);
		
		this.condition = condition;
	}

	
	/**
	 * ビューを更新します。
	 * <p>
	 *
	 * 検索条件が事前に設定されている場合、その条件にヒットする承認ジョブの一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全承認ジョブを表示します。
	 */
	public void update() {
		try {
			if (condition == null) {
				composite.update();
			} else {
				composite.update(condition);
			}
		} catch (Exception e) {
			m_log.warn("update(), " + e.getMessage(), e);
		}
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
		WidgetTestUtil.setTestId(this, null, menu);
		composite.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, this.composite.getTableViewer() );
	}

	/**
	 * 承認ビュー用のコンポジットを返します。
	 *
	 * @return 承認ビュー用のコンポジット
	 */
	public ApprovalComposite getComposite() {
		return composite;
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
			// 詳細、更新、フィルタボタンを作成する
			service.refreshElements(ApprovalDetailAction.ID, null);
			service.refreshElements(ApprovalRefreshAction.ID, null);
			service.refreshElements(ApprovalFilterAction.ID, null);
			
			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);

		}
	}
}
