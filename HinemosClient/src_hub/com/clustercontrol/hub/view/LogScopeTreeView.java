/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.view;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.hub.composite.ScopeComposite;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.AutoUpdateView;

/**
 * 収集蓄積[スコープツリー]ビュークラス<BR>
 * リポジトリ[スコープ]ビューのスコープツリーのみ切り取って表示させる
 * 
 */
public class LogScopeTreeView  extends AutoUpdateView {

	public static final String ID = LogScopeTreeView.class.getName();
	// ログ
	private static Logger m_log = Logger.getLogger( LogScopeTreeView.class );

	// ----- instance フィールド ----- //

	/** スコープツリーのコンポジット */
	private ScopeComposite scopeTreeComposite = null;

	/** パス文字列を表示するラベル */
	private Label pathLabel = null;

	/** ノードをツリーに含めるか？ */
	private boolean scopeOnly = false;

	/** 未登録ノード　スコープをツリーに含めるか？**/
	private boolean unregistered = true;

	/** 内部イベント　スコープをツリーに含めるか？**/
	private boolean internal = true;

	
	
	// ----- コンストラクタ ----- //

	/**
	 * 親のコンストラクタを呼び出します。
	 * @wbp.parser.constructor
	 */
	public LogScopeTreeView() {
		super();
		this.scopeOnly = false;
		this.unregistered = true;
		this.internal = true;
	}

	/**
	 * 親のコンストラクタを呼び出します。
	 *
	 * @param scopeOnly スコープのみのスコープツリーとするかどうか
	 * @param unregistered UNREGISTEREDをスコープツリーに含めるかどうか
	 * @param internal INTERNALをスコープツリーに含めるかどうか
	 * @param topicRefresh リポジトリ情報が更新された際に画面リフレッシュするかどうか
	 */
	public LogScopeTreeView(boolean scopeOnly ,boolean unregistered, boolean internal, boolean topicRefresh) {
		super();
		this.scopeOnly = scopeOnly;
		this.unregistered = unregistered;
		this.internal = internal;
	}

	// ----- instance メソッド ----- //

	/**
	 * ビューを生成します。
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {

		// レイアウト設定
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// スコープツリー作成
		this.scopeTreeComposite = new ScopeComposite(parent, SWT.NONE, this.scopeOnly, this.unregistered, this.internal);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		this.scopeTreeComposite.setLayoutData(gridData);

		this.scopeTreeComposite.getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				// 選択アイテム取得(ツリー自体でも行っているが、念のため)
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				FacilityTreeItemResponse selectItem = (FacilityTreeItemResponse) selection.getFirstElement();
				if (selectItem != null) {
					// ビューの作成
					IWorkbenchWindow window = LogScopeTreeView.this.getSite().getWorkbenchWindow();
					IWorkbenchPage page = window.getActivePage();
					FacilityTreeItemResponse managerTreeItem = ScopePropertyUtil.getManager(selectItem);
					//スコープツリーのTOPをダブルクリックした場合はなにもせずにreturn
					if (managerTreeItem == null) {
						return;
					}
					String manager = managerTreeItem.getData().getFacilityId();
					//マネージャをダブルクリックした場合はなにもせずにreturn
					if (manager.equals(selectItem.getData().getFacilityId())) {
						return;
					}
					try {
						LogSearchView view = LogSearchView.createSearchView(
								page, manager, selectItem.getData().getFacilityId());
						m_log.debug("doubleClick(), open " + view.getTitle());

						view.setFocus();
						view.update();
//
//						AbstractSearcher searcher
//							= AbstractSearcher.simpleSearchBuilder(view.getManager()).setFacilityId(view.getFacilityId()).build();
//
//						Integer fromPos = 0;
//						Integer sizePos = LogPluginService.getDefault().getPreferenceStore().getInt(LogPreferencePage.P_SIZE_POS);
//						
//						LogSearchResponse res = searcher.search(fromPos, sizePos);
//
//						view.setSearcher(searcher);
//						view.getLogSearchComposite().getSearchResultComposite().reflectResult(res, fromPos, sizePos);
//						String esQuery = searcher.convertToESQuery(null, null, false, null, null);
//						view.getLogSearchComposite().getLogSearchConditionComposite().reflectAdvancedSearchQuery(esQuery);
					} catch (PartInitException e) {
						m_log.warn("doubleClick(), " + e.getMessage(), e);
//					} catch(LogSearchException e) {
//						m_log.warn("LogSearch is Failed " + e.getMessage());
//						MessageDialog.openWarning(
//								null,
//								Messages.getString("word.warn"),
//								Messages.getString("message.search.failed") + "\n" + e.toString());
					} catch(Exception e) {
						m_log.warn("LogSearch is Failed " + e.getMessage());
						MessageDialog.openWarning(
								null,
								Messages.getString("word.warn"),
								Messages.getString("message.search.failed") + "\n" + e.toString());
					}
				}
			}
		});
		
		createContextMenu();
	}
	
	/**
	 * ポップアップメニュー作成
	 */
	protected void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		Menu treeMenu = menuManager.createContextMenu(scopeTreeComposite.getTree());
		WidgetTestUtil.setTestId(this, null, treeMenu);
		scopeTreeComposite.getTree().setMenu(treeMenu);
		getSite().registerContextMenu( menuManager, this.scopeTreeComposite.getTreeViewer() );
	}
	
	/**
	 * 追加コンテンツを作成します。
	 * <p>
	 *
	 * オーバーライドして、追加コンポジットを生成して下さい。
	 *
	 * @param parent
	 *            追加コンテンツのベースコンポジット
	 */
	protected Composite createListContents(Composite parent) {
		return null;
	}

	/**
	 * スコープツリーのアイテムが選択された場合に呼び出されるメソッドです。
	 * <p>
	 *
	 * 必要に応じてオーバーライドし、アイテム選択時のイベント処理を実装して下さい。
	 *
	 * @param item
	 *            スコープツリーアイテム
	 */
	protected void doSelectTreeItem(FacilityTreeItemResponse item) {

	}

	/**
	 * スコープツリーのコンポジットを返します。
	 *
	 * @return スコープツリーのコンポジット
	 */
	public FacilityTreeComposite getScopeTreeComposite() {
		return this.scopeTreeComposite;
	}

	/**
	 * パス文字列を表示するラベルを返します。
	 *
	 * @return パス文字列を表示するラベル
	 */
	public Label getPathLabel() {
		return this.pathLabel;
	}

	public void update() {
		this.scopeTreeComposite.update();
		ClientSession.doCheck();
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}

	@Override
	public void update(boolean refreshFlag) {
	}
	
	/**
	 * ビューのアクションの有効/無効を設定します。
	 * 
	 * @param builtin 組み込みスコープかどうかのフラグ
	 * @param type スコープとノードの種別
	 * @param selection ボタン（アクション）を有効にするための情報
	 * 
	 * @see com.clustercontrol.bean.FacilityConstant
	 */
	public void setEnabledAction() {
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}
}
