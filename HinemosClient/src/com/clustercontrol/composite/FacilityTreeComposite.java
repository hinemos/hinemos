/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.composite.ScopeTreeSearchBarComposite;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.util.FacilityTreeItemUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.viewer.FacilityTreeContentProvider;
import com.clustercontrol.viewer.FacilityTreeLabelProvider;
import com.clustercontrol.viewer.FacilityTreeViewerSorter;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * スコープツリーを表示するコンポジットクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class FacilityTreeComposite extends Composite {
	// ログ
	private static Log m_log = LogFactory.getLog( FacilityTreeComposite.class );

	//	 ----- instance フィールド ----- //

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	/** ツリービューア */
	//ノードマップオプションで使用するためprotected
	protected TreeViewer treeViewer = null;

	/** rootFacilityId以下のスコープ情報を取得する。 */
	private String rootFacilityId = null;

	/** tableViewer登録Item */
	private FacilityTreeItem treeItem = null;

	/** リポジトリツリーの時刻 */
	private Date cacheDate = null;

	/** 選択アイテム */
	//ノードマップオプションで使用するためprotected
	protected FacilityTreeItem selectItem = null;

	private List<?> selectionList;

	/** 選択アイテム数 */
	//ノードマップオプションで使用するためprotected
	protected int subScopeNumber;

	/**ノードをツリーに含めるか？ */
	private boolean scopeOnly = false;

	/**選択対象はノードだけか？ */
	boolean selectNodeOnly = false;

	/**未登録ノード　スコープをツリーに含めるか？**/
	private boolean unregistered = true;

	/**内部イベント　スコープをツリーに含めるか？**/
	private boolean internal = true;

	/** リポジトリ情報更新により、表示をリフレッシュするかどうか **/
	//ノードマップオプションで使用するためprotected
	protected boolean topicRefresh = true;

	/** オーナーロールID */
	private String ownerRoleId = null;
	
	/** チェックボックス付きツリーにするかどうか */
	private boolean checkflg = false;

	/** parent Composite */
	private Composite parent = null;

	/** マネージャ名 */
	private String managerName = null;
	
	/** 選択中のファシリティIDリスト */
	private List<String> selectFacilityList = null;
	/** 区切り文字(#!#) */
	private static final String SEPARATOR_HASH_EX_HASH = "#!#";

	/** Enable key press on search bar */
	private boolean enableKeyPress = false;

	// ----- コンストラクタ ----- //
	public FacilityTreeComposite(Composite parent, int style,
			String managerName,
			String ownerRoleId,
			boolean selectNodeOnly) {
		super(parent, style);

		this.managerName = managerName;
		this.selectNodeOnly = selectNodeOnly;
		this.scopeOnly = false;
		this.unregistered = false;
		this.internal = false;
		this.parent = parent;
		this.ownerRoleId = ownerRoleId;
		this.createContents();
	}

	public FacilityTreeComposite(Composite parent, int style,
			String managerName,
			String ownerRoleId,
			boolean scopeOnly ,
			boolean unregistered,
			boolean internal) {
		super(parent, style);

		this.managerName = managerName;
		this.scopeOnly = scopeOnly;
		this.unregistered = unregistered;
		this.internal = internal;
		this.parent = parent;
		this.ownerRoleId = ownerRoleId;
		this.createContents();
	}

	/**
	 * 特定のスコープ配下のみを表示するコンストラクタ<br>
	 * （ver 4.1.0 現在、VM管理のみから呼び出されているコンストラクタ）
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param managerName マネージャ名
	 * @param ownerRoleId オーナーロールID
	 * @param scopeOnly スコープのみのスコープツリーとするかどうか
	 * @param unregistered UNREGISTEREDをスコープツリーに含めるかどうか
	 * @param internal INTERNALをスコープツリーに含めるかどうか
	 * @param rootFacilityId ツリーのルートとするファシリティID
	 */
	public FacilityTreeComposite(Composite parent, int style,
			String managerName,
			String ownerRoleId,
			boolean scopeOnly ,
			boolean unregistered,
			boolean internal,
			String rootFacilityId) {
		super(parent, style);

		this.managerName = managerName;
		this.scopeOnly = scopeOnly;
		this.unregistered = unregistered;
		this.internal = internal;
		this.rootFacilityId = rootFacilityId;
		this.parent = parent;
		this.ownerRoleId = ownerRoleId;
		this.createContents();
	}

	/**
	 * コンストラクタ
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param ownerRoleId オーナーロールID
	 * @param scopeOnly スコープのみのスコープツリーとするかどうか
	 * @param unregistered UNREGISTEREDをスコープツリーに含めるかどうか
	 * @param internal INTERNALをスコープツリーに含めるかどうか
	 * @param topicRefresh リポジトリ情報が更新された際に画面リフレッシュするかどうか
	 */
	public FacilityTreeComposite(Composite parent, int style,
			String ownerRoleId,
			boolean scopeOnly ,
			boolean unregistered,
			boolean internal,
			boolean topicRefresh) {
		super(parent, style);

		this.scopeOnly = scopeOnly;
		this.unregistered = unregistered;
		this.internal = internal;
		this.topicRefresh = topicRefresh;
		this.parent = parent;
		this.ownerRoleId = ownerRoleId;
		this.createContents();
	}

	/**
	 * コンストラクタ
	 * ツリーにチェックボックスをつけるか？
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param ownerRoleId オーナーロールID
	 * @param scopeOnly スコープのみのスコープツリーとするかどうか
	 * @param unregistered UNREGISTEREDをスコープツリーに含めるかどうか
	 * @param internal INTERNALをスコープツリーに含めるかどうか
	 * @param topicRefresh リポジトリ情報が更新された際に画面リフレッシュするかどうか
	 * @param checkflg ツリーにチェックボックスをつけるかどうか
	 */
	public FacilityTreeComposite(Composite parent, int style,
			String ownerRoleId,
			boolean scopeOnly ,
			boolean unregistered,
			boolean internal,
			boolean topicRefresh,
			boolean checkflg) {
		super(parent, style);

		this.scopeOnly = scopeOnly;
		this.unregistered = unregistered;
		this.internal = internal;
		this.topicRefresh = topicRefresh;
		this.parent = parent;
		this.ownerRoleId = ownerRoleId;
		this.checkflg = checkflg;
		this.enableKeyPress = true;
		this.createContents();
	}

	// ----- instance メソッド ----- //


	/**
	 * このコンポジットが利用するツリービューアを返します。
	 *
	 * @return ツリービューア
	 */
	public TreeViewer getTreeViewer() {
		return this.treeViewer;
	}

	/**
	 * このコンポジットが利用するツリーを返します。
	 *
	 * @return ツリー
	 */
	public Tree getTree() {
		return this.treeViewer.getTree();
	}

	/**
	 * 現在選択されているツリーアイテムを返します。
	 *
	 * @return ツリーアイテム
	 */
	public FacilityTreeItem getSelectItem() {
		return this.selectItem;
	}

	/*
	 * ツリーアイテムを選択します。
	 */
	public void setSelectItem(FacilityTreeItem item) {
		selectItem = item;
	}

	/** 現在選択されているツリーアイテムリストを返します。
	 *
	 * @return
	 */
	public List<?> getSelectionList() {
		return this.selectionList;
	}

	/**

	/**
	 * 現在選択されているツリーのサブスコープ数を返します。
	 *
	 * @return サブスコープ数
	 */
	public int getSubScopeNumber() {
		return subScopeNumber;
	}

	/**
	 * コンポジットを生成します。
	 * 
	 * ノードマップオプションで使用するためprotected
	 */
	protected void createContents() {

		// コンポジットのレイアウト定義
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);

		// ツリーのレイアウトデータ定義
		GridData layoutData = new GridData();
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;

		// Add search bar
		Composite compSearch = new ScopeTreeSearchBarComposite(this, SWT.NONE, enableKeyPress);
		WidgetTestUtil.setTestId(this, "search", compSearch);
		compSearch.setLayoutData( new GridData(GridData.FILL_HORIZONTAL) );

		// ツリー作成
		Tree tree = null;

		// ツリービューア作成
		if (checkflg) {
			tree = new Tree(this, SWT.MULTI | SWT.BORDER | SWT.CHECK);
			this.treeViewer = new CheckboxTreeViewer(tree);
		} else {
			tree = new Tree(this, SWT.MULTI | SWT.BORDER);
			this.treeViewer = new TreeViewer(tree);
		}
		WidgetTestUtil.setTestId(this, null, tree);
		tree.setLayoutData(layoutData);

		// ツリービューア設定
		this.treeViewer.setContentProvider(new FacilityTreeContentProvider());
		this.treeViewer.setLabelProvider(new FacilityTreeLabelProvider());
		this.treeViewer.setSorter(new FacilityTreeViewerSorter());

		// 選択アイテム取得イベント定義
		this.treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();

				selectItem = (FacilityTreeItem) selection.getFirstElement();
				selectionList = selection.toList();

				if (selectItem != null) {
					subScopeNumber = selectItem.getChildren().size();
				}
			}
		});
		if (checkflg) {
			// チェックボックス選択イベント定義
			CheckboxTreeViewer checkboxTreeViewer = (CheckboxTreeViewer) treeViewer;
			checkboxTreeViewer.addCheckStateListener(new ICheckStateListener() {
				
				public void checkStateChanged(CheckStateChangedEvent event) {
					CheckboxTreeViewer checkboxTreeViewer = (CheckboxTreeViewer) treeViewer;
					Object element = event.getElement();
					// 親が選択されたら子供も選択し、子の一部のみがチェックされている場合はグレー状態にする
					if (element instanceof FacilityTreeItem) {
						FacilityTreeItem item = (FacilityTreeItem)element;
						checkItems(checkboxTreeViewer, item, event.getChecked());
						checkPath(checkboxTreeViewer, item, event.getChecked(), false);
					}
					// チェック状態を保持する
					selectFacilityList = getCheckedTreeInfo();
				}
			});
		}
		//マネージャからのファシリティツリー更新
		final FacilityTreeComposite composite = this;
		if (topicRefresh) {
			FacilityTreeCache.addComposite(composite);
		}

		this.addDisposeListener(new DisposeListener(){
			@Override
			public void widgetDisposed(DisposeEvent e) {
				FacilityTreeCache.delComposite(composite);
			}
		});

		// 表示します。
		this.update();
	}
	
	/**
	 * ツリー上のチェックボックスに値をセットします。
	 * 子のノードがある場合はそれらもチェックします。
	 * @param tree CheckBoxTreeViewerのインスタンス
	 * @param item チェック対象のFacilityTreeItemのインスタンス
	 * @param checked チェックボックスのチェック状態
	 */
	protected void checkItems(CheckboxTreeViewer tree, FacilityTreeItem item, boolean checked) {
		tree.setGrayed(item, false);
		tree.setChecked(item, checked);
		List<FacilityTreeItem> children = item.getChildren();
		for(FacilityTreeItem child: children) {
			checkItems(tree, child, checked);
		}
	}
	
	/**
	 * チェックボックスの状態をチェックしてツリーに値をセットします。
	 * @param tree CheckBoxTreeViewerのインスタンス
	 * @param item チェック対象のFacilityTreeItemのインスタンス
	 * @param checked チェックボックスのチェック状態
	 * @param grayed チェックボックスのグレー状態（子の一部のみが選択されている状態）
	 */
	protected void checkPath(CheckboxTreeViewer tree, FacilityTreeItem item, boolean checked, boolean grayed) {
		if(item == null){
			return;
		}
		if(grayed){
			checked = true;
		} else {
			List<FacilityTreeItem> children = item.getChildren();
			for (FacilityTreeItem child: children) {
				if (tree.getGrayed(child) || checked != tree.getChecked(child)) {
					checked = grayed = true;
					break;
				}
			}
		}
		tree.setChecked(item, checked);
		tree.setGrayed(item, grayed);
		checkPath(tree, item.getParent(), checked, grayed);
	}

	/**
	 * ビューの表示内容を更新します。
	 */
	@Override
	public void update() {
		// 外部契機でファシリティツリーが更新された場合に、自分の画面もリフレッシュ
		if (this.ownerRoleId != null) {
			try {
				if (this.selectNodeOnly) {
					// ノードのみ取得
					m_log.debug("getNodeFacilityTree " + managerName);
					treeItem = addEmptyParent(RepositoryEndpointWrapper.getWrapper(managerName).getNodeFacilityTree(this.ownerRoleId));
					if (treeItem != null && treeItem.getChildren() != null && treeItem.getChildren().get(0) != null) {
						Collections.sort(treeItem.getChildren().get(0).getChildren(), new Comparator<FacilityTreeItem>() {
							@Override
							public int compare(FacilityTreeItem o1, FacilityTreeItem o2) {
								FacilityInfo info1 = ((FacilityTreeItem) o1).getData();
								FacilityInfo info2 = ((FacilityTreeItem) o2).getData();
								int order1 =  info1.getDisplaySortOrder();
								int order2 =  info2.getDisplaySortOrder();
								if(order1 == order2 ){
									String object1 = info1.getFacilityId();
									String object2 = info2.getFacilityId();
									return object1.compareTo(object2);
								}
								else {
									return (order1 - order2);
								}
							}
						});
					}
				} else {
					m_log.debug("getFacilityTree " + managerName);
					treeItem = addEmptyParent(RepositoryEndpointWrapper.getWrapper(managerName).getFacilityTree(this.ownerRoleId));
				}
			} catch (Exception e) {
				m_log.warn("getTreeItem(), " + e.getMessage(), e);
				return;
			}
		} else {
			treeItem = FacilityTreeCache.getTreeItem(managerName);
		}

		Date cacheDate = null;
		if (managerName != null) {
			cacheDate = FacilityTreeCache.getCacheDate(managerName);
		}
		if (cacheDate != null && cacheDate.equals(this.cacheDate)) {
			return;
		}
		this.cacheDate = cacheDate;

		if( null == treeItem ){
			m_log.trace("treeItem is null. Skip.");
		}else {
			FacilityTreeItem scope = (treeItem.getChildren()).get(0);
			scope.getData().setFacilityName(HinemosMessage.replace(scope.getData().getFacilityName()));

			m_log.debug("internal=" + internal + ", unregistered=" + unregistered);
			
			//ファシリティツリーから特定のスコープを取り外す。
			if(!internal){
				if (managerName == null) {
					for (FacilityTreeItem managerScope : scope.getChildren()) {
						if(!FacilityTreeItemUtil.removeChild(managerScope, FacilityTreeAttributeConstant.INTERNAL_SCOPE)){
							m_log.warn("failed removing " + FacilityTreeAttributeConstant.INTERNAL_SCOPE);
						}
					}
				} else {
					if(!FacilityTreeItemUtil.removeChild(scope, FacilityTreeAttributeConstant.INTERNAL_SCOPE)){
						m_log.warn("failed removing " + FacilityTreeAttributeConstant.INTERNAL_SCOPE + ", managerName=" + managerName);
					}
				}
			}
			if(!unregistered){
				if (managerName == null) {
					for (FacilityTreeItem managerScope : scope.getChildren()) {
						if(!FacilityTreeItemUtil.removeChild(managerScope, FacilityTreeAttributeConstant.UNREGISTERED_SCOPE)){
							m_log.warn("failed removing " + FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
						}
					}
				} else {
					if(!FacilityTreeItemUtil.removeChild(scope, FacilityTreeAttributeConstant.UNREGISTERED_SCOPE)){
						m_log.warn("failed removing " + FacilityTreeAttributeConstant.UNREGISTERED_SCOPE + ", managerName=" + managerName);
					}
				}
			}
			if (rootFacilityId != null) {
				// rootFacilityId以外を全て消す。
				FacilityTreeItemUtil.keepChild(scope, rootFacilityId);
			}
			if (scopeOnly) {
				FacilityTreeItemUtil.removeNode(scope);
			} else {
				// スコープごとのノード表示数より多いスコープを消す
				FacilityTreeItemUtil.removeOverNode(scope);
			}

			// SWTアクセスを許可するスレッドからの操作用
			checkAsyncExec(new Runnable(){
				@Override
				public void run() {
					m_log.trace("FacilityTreeComposite.checkAsyncExec() do runnnable");

					Control control = treeViewer.getControl();
					if (control == null || control.isDisposed()) {
						m_log.info("treeViewer is disposed. ");
						return;
					}
					
					FacilityTreeItem oldTreeItem = (FacilityTreeItem)treeViewer.getInput();
					m_log.debug("run() oldTreeItem=" + oldTreeItem);
					if( null != oldTreeItem ){
						if (!oldTreeItem.equals(treeItem)) {
							ArrayList<String> expandIdList = new ArrayList<String>();
							for (Object item : treeViewer.getExpandedElements()) {
								expandIdList.add(((FacilityTreeItem)item).getData().getFacilityId());
							}
							m_log.debug("expandIdList.size=" + expandIdList.size());
							treeViewer.setInput(treeItem);
							treeViewer.refresh();
							expand(treeItem, expandIdList);
						}
					}else{
						treeViewer.setInput(treeItem);
						List<FacilityTreeItem> selectItem = treeItem.getChildren();
						treeViewer.setSelection(new StructuredSelection(selectItem.get(0)), true);
						//スコープのレベルまで展開
						treeViewer.expandToLevel(3);
					}
					if (checkflg) {
						// チェックをつける
						setCheckedTreeInfo(selectFacilityList);
					}
				}

				private void expand(FacilityTreeItem item, List<String> expandIdList) {
					if (expandIdList.contains(item.getData().getFacilityId())) {
						treeViewer.expandToLevel(item, 1);
					}
					for (FacilityTreeItem child : item.getChildren()) {
						expand(child, expandIdList);
					}
				}
			});
		}
	}

	/**
	 * 同期チェック
	 * @param r
	 * @return
	 */
	private boolean checkAsyncExec(Runnable r){

		if(!this.isDisposed()){
			m_log.trace("FacilityTreeComposite.checkAsyncExec() is true");
			parent.getDisplay().asyncExec(r);
			return true;
		}
		else{
			m_log.trace("FacilityTreeComposite.checkAsyncExec() is false");
			return false;
		}
	}

	/**
	 * ツリーを展開して表示するかを指定します。
	 *
	 */
	public void setExpand(boolean isExpand) {
		if (isExpand) {
			this.treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		}
	}

	/**
	 * ツリーの表示内容を更新します。
	 *
	 * @param treeItem
	 */
	public void setScopeTree(FacilityTreeItem treeItem) {
		try {
			this.treeItem = treeItem;
			this.treeViewer.setInput(treeItem);
			this.treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		} catch (Exception e) {
			m_log.warn("setScopeTree(), " + e.getMessage(), e);
		}
	}

	/**
	 * TreeをセットしながらfacilityIDに対応する要素を選択状態にします。
	 *
	 * @param treeItem
	 * @param facilityID
	 */
	public void setScopeTreeWithSelection(FacilityTreeItem treeItem,
			String facilityID) {
		this.setScopeTree(treeItem);

		List<FacilityTreeItem> tmpItem = treeItem.getChildren();

		//引数のFaiclityIDに対応するTreeItemがあるか探します。
		for (int i = 0; i < tmpItem.size(); i++) {
			setScopeTreeWithSelectionSub(tmpItem.get(i), facilityID);
			if (facilityID.equals(tmpItem.get(i).getData().getFacilityId())) {
				this.treeViewer.setSelection(
						new StructuredSelection(tmpItem.get(i)), true);
			}
		}
	}

	/**
	 * setScopeTreeWithSelectionから呼ばれること前提とした再帰用のメソッド
	 *
	 * @param treeItem
	 * @param facilityID
	 */
	public void setScopeTreeWithSelectionSub(FacilityTreeItem treeItem,
			String facilityID) {
		List<FacilityTreeItem> tmpItem = treeItem.getChildren();

		for (int i = 0; i < tmpItem.size(); i++) {
			setScopeTreeWithSelectionSub(tmpItem.get(i), facilityID);

			if (facilityID.equals(tmpItem.get(i).getData().getFacilityId())) {
				this.treeViewer.setSelection(
						new StructuredSelection(tmpItem.get(i)), true);

			}
		}
	}

	public CommonTableViewer getTableViewer() {
		return tableViewer;
	}

	public void setTableViewer(CommonTableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return this.tableViewer.getTable();
	}
	
	/**
	 * ツリーのすべての要素を返します。
	 * 
	 * @return
	 */
	public FacilityTreeItem getAllTreeItems() {
		return this.treeItem;
	}
	
	/**
	 * ツリーの選択状態をStringのリストで以下の形式で返します。<br>
	 * ファシリティタイプが<br>
	 * 2(COMPOSITE)の場合：ファシリティタイプ<br>
	 * 0(SCOPE)または3(MANAGER)の場合：マネージャ名 + "#!#" + ファシリティID + "#!#" + ファシリティタイプ<br>
	 * 1(NODE)の場合：マネージャ名 + "#!#" + 親のファシリティID + "#!#" + ファシリティID + "#!#" + ファシリティタイプ<br>
	 * 
	 * @return
	 */
	public List<String> getCheckedTreeInfo() {
		CheckboxTreeViewer tree = (CheckboxTreeViewer)getTreeViewer();
		Object[] treeItemArray = tree.getCheckedElements();
		List<Object> treeItemList = new ArrayList<Object>();
		
		for(Object item:treeItemArray){
			boolean grayed = tree.getGrayed(item);
			boolean checked = tree.getChecked(item);
			if(!grayed && checked){
				treeItemList.add(item);
			}
		}
		List<String> selectFacilityStringList = new ArrayList<>();
		m_log.debug("SIZE:" + treeItemList.size());
		for (Object objectItem : treeItemList) {
			if (objectItem instanceof FacilityTreeItem) {
				FacilityTreeItem facilityTreeItem = (FacilityTreeItem)objectItem;
				switch (facilityTreeItem.getData().getFacilityType()) {
				case FacilityConstant.TYPE_COMPOSITE:
					selectFacilityStringList.add(String.valueOf(facilityTreeItem.getData().getFacilityType()));
					break;
				case FacilityConstant.TYPE_SCOPE:
				case FacilityConstant.TYPE_MANAGER:
					// 指定したスコープ配下に含まれる全てのノードを対象
					String managerName = ScopePropertyUtil.getManager(facilityTreeItem).getData().getFacilityId();
					String facilityId = facilityTreeItem.getData().getFacilityId();
					int facilityType = facilityTreeItem.getData().getFacilityType();
					String param = managerName +SEPARATOR_HASH_EX_HASH + facilityId + SEPARATOR_HASH_EX_HASH + facilityType;
					m_log.debug(param);
					selectFacilityStringList.add(param);
	
					break;
				case FacilityConstant.TYPE_NODE:
					managerName = ScopePropertyUtil.getManager(facilityTreeItem).getData().getFacilityId();
					facilityId = facilityTreeItem.getData().getFacilityId();
					String parentFacilityId = facilityTreeItem.getParent().getData().getFacilityId();
					facilityType = facilityTreeItem.getData().getFacilityType();
					param = managerName + SEPARATOR_HASH_EX_HASH + parentFacilityId +SEPARATOR_HASH_EX_HASH + facilityId + SEPARATOR_HASH_EX_HASH + facilityType;
					m_log.debug(param);
					selectFacilityStringList.add(param);
					break;
				default: // 既定の対処はスルー。
					break;
				}
			}
		}
		return selectFacilityStringList;
	}
	
	/**
	 * 引数で指定されたfacilityItemListを、チェック状態にします。
	 * 
	 * @param treeItem
	 */
	private void setCheckedTreeInfo(List<String> facilityItemList) {
		List<FacilityTreeItem> treeItemList = new ArrayList<>();
		List<FacilityTreeItem> parentItemList = new ArrayList<>();
		checkTreeSelect(this.treeItem, facilityItemList, treeItemList, parentItemList);
		m_log.debug("setSelectItemSize:" + treeItemList.size() + ", parentItemList.size:" + parentItemList.size());
		
		// チェック状態の復元
		FacilityTreeItem facilityArr[] = treeItemList.toArray(new FacilityTreeItem[treeItemList.size()]);
		((CheckboxTreeViewer)getTreeViewer()).setCheckedElements(facilityArr);
		
		// 選択状態の親に新たに子供がいた場合は、子供にチェックをつけ,子の一部がチェックされている場合グレー状態にする
		for (FacilityTreeItem item : treeItemList) {
			CheckboxTreeViewer tree = (CheckboxTreeViewer)getTreeViewer();
			checkItems(tree, item, true);
			checkPath(tree, item, true, false);
		}
		Object[] returns = ((CheckboxTreeViewer)getTreeViewer()).getCheckedElements();
		m_log.debug("selectItemSize:" + returns.length);
	}

	/**
	 * 引数で指定されたtreeItemの要素に対して、選択状態にするかチェックし、選択状態にするListと選択状態の子持ちのListを作成します。
	 * 
	 * @param treeItem ツリーに表示しているFacilityTreeItem
	 * @param facilityStringList 選択情報のStringのList
	 * @param facilityList 選択状態にするFacilityTreeItemのリスト(戻り値)
	 * @param parentFacilityList 選択状態の子持ちのFacilityTreeItemのリスト(戻り値)
	 */
	public void checkTreeSelect(FacilityTreeItem treeItem, List<String> facilityStringList, 
			List<FacilityTreeItem> facilityList, List<FacilityTreeItem> parentFacilityList) {
		if (facilityStringList == null || facilityStringList.size() == 0) {
			return;
		}
		List<FacilityTreeItem> treeItemList = treeItem.getChildren();
		for (FacilityTreeItem childItem : treeItemList) {
			checkTreeSelect(childItem, facilityStringList, facilityList, parentFacilityList);
		}
		for (String detail : facilityStringList) {
			String facilityId = treeItem.getData().getFacilityId();
			String managerName = "";
			String parentFacilityId = "";
			String details[] = detail.split(SEPARATOR_HASH_EX_HASH);
			String facilityType = details[details.length-1];
			if (treeItem.getData().getFacilityType() == FacilityConstant.TYPE_COMPOSITE) {
				// 「スコープ」の場合はDetailにFacilityTypeのみ入っている
				if (details.length == 1) {
					m_log.debug("selected(have sub:composite) managerName:" + managerName + ", parentFacilityId:" + parentFacilityId + ", facilityId:" + facilityId);
					facilityList.add(treeItem);
					parentFacilityList.add(treeItem);
				}
			} else {
				managerName = ScopePropertyUtil.getManager(treeItem).getData().getFacilityId();
				parentFacilityId = treeItem.getParent().getData().getFacilityId();
				if (Integer.parseInt(facilityType) == FacilityConstant.TYPE_NODE && details[0].equals(managerName) 
						&& details[1].equals(parentFacilityId) && details[2].equals(facilityId)) {
					m_log.debug("selected tree managerName:" + managerName + ", parentFacilityId:" + parentFacilityId + ", facilityId:" + facilityId);
					facilityList.add(treeItem);
				} else if ((Integer.parseInt(facilityType) == FacilityConstant.TYPE_SCOPE || Integer.parseInt(facilityType) == FacilityConstant.TYPE_MANAGER) 
								&& details[0].equals(managerName) && details[1].equals(facilityId)) {
					// nodeの場合はmanagerName、facilityId、parentFacilityIdが入っている
					// scopeまたはmanagerの場合は、managerName、facilityIdが入っている
					m_log.debug("selected(have sub) managerName:" + managerName + ", parentFacilityId:" + parentFacilityId + ", facilityId:" + facilityId);
					facilityList.add(treeItem);
					parentFacilityList.add(treeItem);
				}
			}
		}
	}
	
	/**
	 * 引数で指定されたListをメンバに設定します。
	 * 
	 * @param selectFacilityList ファシリティツリーの選択情報
	 */
	public void setSelectFacilityList(List<String> selectFacilityList) {
		this.selectFacilityList = selectFacilityList;
	}

	/**
	 * 兄弟アイテム検索（ノードスコープ検索）
	 * @param current 選択アイテム
	 * @param keyword 検索文字列（前方一致検索）
	 * @return 検索結果アイテム
	 */
	private FacilityTreeItem searchNeighbors( FacilityTreeItem current, String keyword ){
		FacilityTreeItem found;
		FacilityTreeItem parent = current.getParent();
		if( null != parent ){
			do{
				int offset = parent.getChildren().indexOf( current ) + 1;
				found = searchChildren( parent, keyword, offset );
				if( null != found ){
					return found;
				}
				current = parent;
				parent = current.getParent();
			}while( null != parent );
		}
		return null;
	}

	/**
	 * 子アイテム検索（ノードスコープ検索）
	 * @param parent 親アイテム
	 * @param keyword 検索文字列（前方一致検索）
	 * @param offset 選択アイテムのインデックス
	 * @return 検索結果アイテム
	 */
	private FacilityTreeItem searchChildren( FacilityTreeItem parent, String keyword, int offset ){
		List<FacilityTreeItem> children = parent.getChildren();
		Collections.sort(children, new Comparator<FacilityTreeItem>() {
			public int compare(FacilityTreeItem item1, FacilityTreeItem item2) {
				return item1.getData().getDisplaySortOrder() - item2.getData().getDisplaySortOrder();
			}
		});
		int len = children.size();
		for( int i = offset; i<len; i++ ){
			FacilityTreeItem child = children.get(i);

			if(child.getData().getFacilityId() != null
					&& -1 != child.getData().getFacilityId().indexOf( keyword ) ){
				return child;
			}else{
				FacilityTreeItem found = searchChildren( child, keyword, 0 );
				if( null != found ){
					return found;
				}
			}
		}
		return null;
	}

	/**
	 * ツリー内検索（ノードスコープ検索）
	 * 
	 * @param item 選択アイテム
	 * @param keyword 検索文字列（前方一致検索）
	 * @return 検索結果アイテム
	 */
	private FacilityTreeItem searchItem( FacilityTreeItem item, String keyword ){
		FacilityTreeItem found;

		// 1. Search children
		found= searchChildren(item, keyword, 0);
		if( null != found ){
			return found;
		}	

		// 2. If not found in children, search in neighbors
		found = searchNeighbors( item, keyword );
		if( null != found ){
			return found;
		}

		return null;
	}

	/**
	 * ノードスコープ検索
	 * @param keyword 検索文字列（前方一致検索）
	 */
	public void doSearch( String keyword ){
		// Check and format keyword
		if( null == keyword ){
			return;
		}
		keyword = keyword.trim();
		if( keyword.isEmpty() ){
			return;
		}

		StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
		Object targetItem = selection.getFirstElement();
		FacilityTreeItem result = searchItem( (FacilityTreeItem)( null != targetItem ? targetItem: treeViewer.getInput() ), keyword );
		if( null != result ){
			FacilityTreeItem trace = result;
			LinkedList<FacilityTreeItem> pathList = new LinkedList<>();
			do{
				pathList.addFirst( trace );
				trace = trace.getParent();
			}while( null != trace );
			TreePath path = new TreePath( pathList.toArray(new FacilityTreeItem[]{}) );
			treeViewer.setSelection( new TreeSelection(path), true );
		}else{
			MessageDialog.openInformation( this.getShell(), Messages.getString("message"), Messages.getString("search.not.found") );
			treeViewer.setSelection( new StructuredSelection(((FacilityTreeItem)treeViewer.getInput()).getChildren().get(0)), true );
		}
	}
	
	/**
	 * 画面表示のためFacilityTreeItemの親に空のItemを設定する
	 * 
	 * @param childTree 対象FacilityTreeItem
	 * @return 親に空を設定したFacilityTreeItem
	 */
	private FacilityTreeItem addEmptyParent(FacilityTreeItem childTree) {
		FacilityTreeItem rootTree = null;

		if (childTree != null) {
			// 木構造最上位インスタンスの生成
			rootTree = new FacilityTreeItem();
			FacilityInfo rootInfo = new FacilityInfo();
			rootInfo.setBuiltInFlg(true);
			rootInfo.setFacilityName(FacilityConstant.STRING_COMPOSITE);
			rootInfo.setFacilityType(FacilityConstant.TYPE_COMPOSITE);
			rootTree.setData(rootInfo);
			childTree.setParent(rootTree);
			rootTree.getChildren().add(childTree);
		}
		
		return rootTree;
	}

}
