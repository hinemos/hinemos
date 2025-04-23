/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.composite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

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
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.composite.ScopeTreeSearchBarComposite;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
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
	protected FacilityTreeItemResponse treeItem = null;

	/** リポジトリツリーの時刻 */
	protected Date cacheDate = null;

	/** 選択アイテム */
	//ノードマップオプションで使用するためprotected
	protected FacilityTreeItemResponse selectItem = null;

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
	protected String ownerRoleId = null;

	/** チェックボックス付きツリーにするかどうか */
	private boolean checkflg = false;

	/** parent Composite */
	private Composite parent = null;

	/** マネージャ名 */
	protected String managerName = null;

	/**
	 * 選択中のファシリティID階層のリスト
	 * ファシリティタイプにより、以下の形式となる（「」は実際のID等に置換える。区切り文字はSEPARATOR_HASH_EX_HASH）。
	 * 0(SCOPE)の場合		：「マネージャ名」#!#「ファシリティID」#!#0
	 * 1(NODE)の場合		：「マネージャ名」#!#「親スコープのファシリティID」#!#「ファシリティID」#!#1
	 * 2(COMPOSITE)の場合	：「ファシリティタイプ」
	 * 3(MANAGER)の場合		：「マネージャ名」#!#「マネージャ名」#!#3
	 */
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
	public FacilityTreeItemResponse getSelectItem() {
		return this.selectItem;
	}

	/*
	 * ツリーアイテムを選択します。
	 */
	public void setSelectItem(FacilityTreeItemResponse item) {
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

				selectItem = (FacilityTreeItemResponse) selection.getFirstElement();
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
					if (m_log.isDebugEnabled()) {
						m_log.debug("createContents() ... checkStateChanged() start. element=" + element + ", Checked=" + event.getChecked());
					}
					// 一旦削除
					deleteSelectFacilityList();
					// 親が選択されたら子供も選択し、子の一部のみがチェックされている場合はグレー状態にする
					if (element instanceof FacilityTreeItemResponse) {
						checkPath(checkboxTreeViewer, (FacilityTreeItemResponse) element, event.getChecked());
					}
					// チェック状態を保持する
					getSelectFacilityList();
					if (m_log.isDebugEnabled()) {
						m_log.debug("createContents() ... checkStateChanged() end.");
					}
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
	 * 
	 * @param tree CheckBoxTreeViewerのインスタンス
	 * @param item チェック対象のFacilityTreeItemのインスタンス
	 * @param state チェックボックスのチェック状態
	 */
	protected void setChildrenCheckedRecursively(CheckboxTreeViewer tree, FacilityTreeItemResponse item, boolean state) {
		tree.setChecked(item, state);
		tree.setGrayed(item, false);		// グレーも外す
		List<FacilityTreeItemResponse> children = item.getChildren();
		for(FacilityTreeItemResponse child: children) {
			setChildrenCheckedRecursively(tree, child, state);
		}
	}

	/**
	 * 再帰的に、子のチェックボックスのグレーを変更する。
	 * 
	 * @param tree CheckBoxTreeViewerのインスタンス
	 * @param item チェック対象のFacilityTreeItemのインスタンス
	 * @param state チェックボックスのグレー状態
	 */
	protected void setChildrenGrayedRecursively(CheckboxTreeViewer tree, FacilityTreeItemResponse item, boolean state) {
		tree.setGrayChecked(item, state);
		List<FacilityTreeItemResponse> children = item.getChildren();
		for(FacilityTreeItemResponse child: children) {
			setChildrenGrayedRecursively(tree, child, state);
		}
	}

	/**
	 * 該当項目の親からルート階層まで再帰的に確認し、チェックボックスにチェックやグレーを設定する。
	 * 
	 * @param tree スコープ階層
	 * @param item 該当項目
	 * @param checked 該当項目のチェック状態
	 * @param grayed 該当項目のグレー状態
	 */
	private void setParentGrayCheckedRecursively(CheckboxTreeViewer tree, FacilityTreeItemResponse item, boolean checked, boolean grayed) {
		if (tree == null || item == null) {
			// 通常は到達しない
			m_log.warn("setParentGrayCheckedRecursively() tree or item is null.");
			return;
		}
		if (m_log.isDebugEnabled()) {
			m_log.debug("setParentGrayCheckedRecursively() start. item FacilityId=" + item.getData().getFacilityId() + ", checked=" + checked + ", grayed=" + grayed);
		}

		FacilityTreeItemResponse parent = item.getParent();
		if (parent == null || parent.getData() == null || parent.getData().getFacilityId() == null) {
			// 頂点の番兵はgetFacilityId()がnull
			if (m_log.isDebugEnabled()) {
				m_log.debug("setParentGrayCheckedRecursively() end. parent is null.");
			}
			return;
		}
		if (m_log.isTraceEnabled()) {
			m_log.trace("setParentGrayCheckedRecursively() parent FacilityId=" + parent.getData().getFacilityId());
		}
		// 該当項目がグレーなら親はグレー
		if (grayed) {
			if (m_log.isDebugEnabled()) {
				m_log.debug("setParentGrayCheckedRecursively() parent grayed, too. parent FacilityId=" + parent.getData().getFacilityId());
			}
			tree.setGrayChecked(parent, true);
			// 更に親を確認
			setParentGrayCheckedRecursively(tree, parent, true, true);
			if (m_log.isDebugEnabled()) {
				m_log.debug("setParentGrayCheckedRecursively() end. item FacilityId=" + item.getData().getFacilityId());
			}
			return;
		}

		// 兄弟を確認
		// チェック入りの兄弟数（該当項目はカウント除外）
		int checkedBroCnt = 0;
		// 兄弟にグレーがあるか
		boolean hasBrothersGray = false;
		List<FacilityTreeItemResponse> brothers = parent.getChildren();
		if (m_log.isTraceEnabled()) {
			m_log.trace("setParentGrayCheckedRecursively() brothers.size()=" + brothers.size());
		}
		for (FacilityTreeItemResponse brother : brothers) {
			if (brother == null || brother.getData() == null || brother.getData().getFacilityId() == null) {
				if (m_log.isTraceEnabled()) {
					m_log.trace("setParentGrayCheckedRecursively() brother is null, so skip.");
				}
				continue;
			}
			if (m_log.isTraceEnabled()) {
				m_log.trace("setParentGrayCheckedRecursively() brother FacilityId=" + brother.getData().getFacilityId());
			}
			// itemはスキップ（tree.getChecked()は高コストなのでできるだけ呼び出したくない）
			if (brother.getData().getFacilityId().equals(item.getData().getFacilityId())) {
				if (m_log.isTraceEnabled()) {
					m_log.trace("setParentGrayCheckedRecursively() brother is same item, so skip.");
				}
				continue;
			}
			// brotherがグレーの場合はループを抜ける
			if (tree.getGrayed(brother)) {
				if (m_log.isTraceEnabled()) {
					m_log.trace("setParentGrayCheckedRecursively() brother is grayed. brother FacilityId=" + brother.getData().getFacilityId());
				}
				hasBrothersGray = true;
				break;
			}
			// brotherがチェックされていればカウントアップ
			if (tree.getChecked(brother)) {
				checkedBroCnt++;
			}
		}
		// 兄弟にグレーがあれば親はグレー
		if (hasBrothersGray) {
			if (m_log.isDebugEnabled()) {
				m_log.debug("setParentGrayCheckedRecursively() brother is grayed, so parent grayed, too. parent FacilityId=" + parent.getData().getFacilityId());
			}
			tree.setGrayChecked(parent, true);
			// 更に親を確認
			setParentGrayCheckedRecursively(tree, parent, true, true);
			if (m_log.isDebugEnabled()) {
				m_log.debug("setParentGrayCheckedRecursively() end. item FacilityId=" + item.getData().getFacilityId());
			}
			return;
		}
		if (m_log.isTraceEnabled()) {
			m_log.trace("setParentGrayCheckedRecursively() checkedBroCnt=" + checkedBroCnt);
		}

		boolean parentChecked = false;
		boolean parentGrayed = false;
		if (!checked && checkedBroCnt == 0) {
			// 自身がチェックなしで、兄弟チェック数が0なら、親はチェックなし
			if (m_log.isDebugEnabled()) {
				m_log.debug("setParentGrayCheckedRecursively() parent unchecked. parent FacilityId=" + parent.getData().getFacilityId());
			}
			tree.setChecked(parent, false);
			tree.setGrayed(parent, false);
		} else if (checked && brothers.size() - 1 == checkedBroCnt) {
			// 全チェックなら親はチェック
			parentChecked = true;
			if (m_log.isDebugEnabled()) {
				m_log.debug("setParentGrayCheckedRecursively() parent checked. parent FacilityId=" + parent.getData().getFacilityId());
			}
			tree.setChecked(parent, true);
			tree.setGrayed(parent, false);
		} else {
			// そうでないなら親はグレー
			if (m_log.isDebugEnabled()) {
				m_log.debug("setParentGrayCheckedRecursively() parent grayed. parent FacilityId=" + parent.getData().getFacilityId());
			}
			parentChecked = true;
			parentGrayed = true;
			tree.setGrayChecked(parent, true);
		}

		// 更に親を確認
		setParentGrayCheckedRecursively(tree, parent, parentChecked, parentGrayed);

		if (m_log.isDebugEnabled()) {
			m_log.debug("setParentGrayCheckedRecursively() end. item FacilityId=" + item.getData().getFacilityId());
		}
	}

	/**
	 * チェックボックスの状態をチェックしてツリーに値をセットします。
	 * 
	 * @param tree CheckBoxTreeViewerのインスタンス
	 * @param item チェック対象のFacilityTreeItemのインスタンス
	 * @param checked チェックボックスのチェック状態
	 */
	protected void checkPath(CheckboxTreeViewer tree, FacilityTreeItemResponse item, boolean checked) {
		if(tree == null || item == null || item.getData() == null || item.getData().getFacilityId() == null){
			// 通常は到達しない
			m_log.warn("checkPath() tree or item is null.");
			return;
		}
		if (m_log.isDebugEnabled()) {
			m_log.debug("checkPath() item FacilityId=" + item.getData().getFacilityId() + ", checked=" + checked);
		}

		// 子全体をチェックに更新
		// 以下の方が、setChildrenCheckedRecursively()より処理時間が短い
		setChildrenGrayedRecursively(tree, item, false);	// 一旦グレーを外す、時間がかかる
		tree.setSubtreeChecked(item, checked);
		// 親のチェック、グレーを更新
		setParentGrayCheckedRecursively(tree, item, checked, false);
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
					treeItem = addEmptyParent(RepositoryRestClientWrapper.getWrapper(managerName).getNodeFacilityTree(this.ownerRoleId));
					if (treeItem != null && treeItem.getChildren() != null && treeItem.getChildren().get(0) != null) {
						Collections.sort(treeItem.getChildren().get(0).getChildren(), new FacilityTreeViewerSorter.FacilityTreeItemComparator());
					}
				} else {
					m_log.debug("getFacilityTree " + managerName);
					treeItem = addEmptyParent(RepositoryRestClientWrapper.getWrapper(managerName).getFacilityTree(this.ownerRoleId));
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
			FacilityTreeItemResponse scope = (treeItem.getChildren()).get(0);
			scope.getData().setFacilityName(HinemosMessage.replace(scope.getData().getFacilityName()));

			m_log.debug("internal=" + internal + ", unregistered=" + unregistered);
			
			//ファシリティツリーから特定のスコープを取り外す。
			if(!internal){
				if (managerName == null) {
					for (FacilityTreeItemResponse managerScope : scope.getChildren()) {
						if(!FacilityTreeItemUtil.removeChild(managerScope, FacilityTreeAttributeConstant.INTERNAL_SCOPE)){
							m_log.warn("failed removing " + FacilityTreeAttributeConstant.INTERNAL_SCOPE);
						}
					}
				} else {
					if(!FacilityTreeItemUtil.removeChild(scope, FacilityTreeAttributeConstant.INTERNAL_SCOPE)){
						m_log.debug("failed removing " + FacilityTreeAttributeConstant.INTERNAL_SCOPE + ", managerName=" + managerName);
					}
				}
			}
			if(!unregistered){
				if (managerName == null) {
					for (FacilityTreeItemResponse managerScope : scope.getChildren()) {
						if(!FacilityTreeItemUtil.removeChild(managerScope, FacilityTreeAttributeConstant.UNREGISTERED_SCOPE)){
							m_log.warn("failed removing " + FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
						}
					}
				} else {
					if(!FacilityTreeItemUtil.removeChild(scope, FacilityTreeAttributeConstant.UNREGISTERED_SCOPE)){
						m_log.debug("failed removing " + FacilityTreeAttributeConstant.UNREGISTERED_SCOPE + ", managerName=" + managerName);
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
			checkAsyncExec(new Runnable(){	// Webクライアントはリロード時にここが2回走る
				@Override
				public void run() {
					m_log.trace("FacilityTreeComposite.checkAsyncExec() do runnnable");

					Control control = treeViewer.getControl();
					if (control == null || control.isDisposed()) {
						m_log.info("treeViewer is disposed. ");
						return;
					}

					FacilityTreeItemResponse oldTreeItem = (FacilityTreeItemResponse)treeViewer.getInput();
					m_log.debug("run() oldTreeItem=" + oldTreeItem);
					if( null != oldTreeItem ){
						if (!oldTreeItem.equals(treeItem)) {
							ArrayList<String> expandIdList = new ArrayList<String>();
							for (Object item : treeViewer.getExpandedElements()) {
								expandIdList.add(((FacilityTreeItemResponse)item).getData().getFacilityId());
							}
							m_log.debug("expandIdList.size=" + expandIdList.size());
							treeViewer.setInput(treeItem);
							treeViewer.refresh();
							expand(treeItem, expandIdList);
						}
					}else{
						treeViewer.setInput(treeItem);
						List<FacilityTreeItemResponse> selectItem = treeItem.getChildren();
						treeViewer.setSelection(new StructuredSelection(selectItem.get(0)), true);
						//スコープのレベルまで展開
						treeViewer.expandToLevel(3);
					}
					if (checkflg) {
						// チェックをつける
						setCheckedTreeInfo(getSelectFacilityList());
					}
				}

				private void expand(FacilityTreeItemResponse item, List<String> expandIdList) {
					if (expandIdList.contains(item.getData().getFacilityId())) {
						treeViewer.expandToLevel(item, 1);
					}
					for (FacilityTreeItemResponse child : item.getChildren()) {
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
	protected boolean checkAsyncExec(Runnable r){

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
	public void setScopeTree(FacilityTreeItemResponse treeItem) {
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
	public void setScopeTreeWithSelection(FacilityTreeItemResponse treeItem,
			String facilityID) {
		this.setScopeTree(treeItem);

		List<FacilityTreeItemResponse> tmpItem = treeItem.getChildren();

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
	public void setScopeTreeWithSelectionSub(FacilityTreeItemResponse treeItem,
			String facilityID) {
		List<FacilityTreeItemResponse> tmpItem = treeItem.getChildren();

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
	public FacilityTreeItemResponse getAllTreeItems() {
		return this.treeItem;
	}
	
	/**
	 * ツリーの選択状態をStringのリストで以下の形式で返します。<br>
	 * このメソッドは時間がかかる
	 * 
	 * @return ファシリティID階層のリスト（メンバ変数selectFacilityListの形式）
	 */
	public List<String> getCheckedTreeInfo() {
		if (m_log.isDebugEnabled()) {
			m_log.debug("getCheckedTreeInfo() start.");
		}

		CheckboxTreeViewer tree = (CheckboxTreeViewer) getTreeViewer();
		Object[] treeItemArray = tree.getCheckedElements();
		List<FacilityTreeItemResponse> treeItemList = new ArrayList<>();
		for (Object item : treeItemArray) {
			if (!(item instanceof FacilityTreeItemResponse)) {
				continue;
			}
			if (tree.getGrayed(item)) {
				// グレーはスキップ
				continue;
			}
			if (tree.getChecked(item)) {
				treeItemList.add((FacilityTreeItemResponse) item);
			}
		}

		return getFacilityList(treeItemList);
	}

	/**
	 * ツリー項目のリストから、ファシリティID階層のリストに変換する。
	 * 
	 * @param treeItemList ツリー項目
	 * @return ファシリティID階層のリスト（メンバ変数selectFacilityListの形式）
	 */
	public List<String> getFacilityList(List<FacilityTreeItemResponse> treeItemList) {
		m_log.info("getFacilityList() start.");
		List<String> facilityList = new ArrayList<>();
		if (treeItemList == null) {
			// 通常は到達しない
			m_log.warn("getFacilityList() end. treeItemList is null.");
			return facilityList;
		}

		if (m_log.isDebugEnabled()) {
			m_log.debug("getFacilityList() treeItemList.size:" + treeItemList.size());
		}
		for (FacilityTreeItemResponse item : treeItemList) {
			switch (item.getData().getFacilityType()) {
			case COMPOSITE:
				// findbugs対応 facilityType設定値をordinaryから定数に変更
				facilityList.add(String.valueOf(FacilityConstant.TYPE_COMPOSITE));
				if (m_log.isTraceEnabled()) {
					m_log.trace("getFacilityList() COMPOSITE, FacilityId=" + item.getData().getFacilityId());
				}
				break;
			case SCOPE:
			case MANAGER:
				// 指定したスコープ配下に含まれる全てのノードを対象
				String managerName = ScopePropertyUtil.getManager(item).getData().getFacilityId();
				String facilityId = item.getData().getFacilityId();
				// findbugs対応 facilityType設定値をordinaryから定数に変更
				Integer facilityType = null;
				if (item.getData().getFacilityType() == FacilityInfoResponse.FacilityTypeEnum.SCOPE) {
					facilityType = FacilityConstant.TYPE_SCOPE;
				} else if (item.getData().getFacilityType() == FacilityInfoResponse.FacilityTypeEnum.MANAGER) {
					facilityType = FacilityConstant.TYPE_MANAGER;
				}
				String param = managerName + SEPARATOR_HASH_EX_HASH + facilityId + SEPARATOR_HASH_EX_HASH + String.valueOf(facilityType);
				if (m_log.isTraceEnabled()) {
					m_log.trace("getFacilityList() TYPE_SCOPE or TYPE_MANAGER, param=" + param);
				}
				facilityList.add(param);
				break;
			case NODE:
				managerName = ScopePropertyUtil.getManager(item).getData().getFacilityId();
				facilityId = item.getData().getFacilityId();
				String parentFacilityId = item.getParent().getData().getFacilityId();
				// findbugs対応 facilityType設定値をordinaryから定数に変更
				param = managerName + SEPARATOR_HASH_EX_HASH + parentFacilityId + SEPARATOR_HASH_EX_HASH + facilityId
						+ SEPARATOR_HASH_EX_HASH + String.valueOf(FacilityConstant.TYPE_NODE);
				if (m_log.isTraceEnabled()) {
					m_log.trace("getFacilityList() TYPE_NODE, param=" + param);
				}
				facilityList.add(param);
				break;
			default: // 既定の対処はスルー。
				if (m_log.isTraceEnabled()) {
					m_log.trace("getFacilityList() default, FacilityType=" + item.getData().getFacilityType());
				}
				break;
			}
		}

		if (m_log.isTraceEnabled()) {
			m_log.trace("getFacilityList() end. selectFacilityStringList=" + Arrays.toString(facilityList.toArray()));
		}
		return facilityList;
	}

	/**
	 * 引数で指定されたfacilityItemListにより、チェックボックのチェックやグレー状態を復元します。
	 * 
	 * @param facilityItemList 現状選択しているリスト。形式は、メンバ変数 selectFacilityList と同じ。
	 */
	private void setCheckedTreeInfo(List<String> facilityItemList) {
		if (facilityItemList == null || facilityItemList.size() == 0) {
			if (m_log.isDebugEnabled()) {
				m_log.debug("setCheckedTreeInfo() facilityItemList is empty.");
			}
			return;
		}
		if (m_log.isDebugEnabled()) {
			m_log.debug("setCheckedTreeInfo() this.treeItem FacilityId=" + this.treeItem.getData().getFacilityId()
					+ ", facilityItemList=" + Arrays.toString(facilityItemList.toArray()));
		}

		//選択してる項目、配下のスコープ、ノードも含む
		List<FacilityTreeItemResponse> treeItemList = new ArrayList<>();
		//選択してるスコープと配下のスコープ、自身も含む（ノードは含まず）
		List<FacilityTreeItemResponse> parentItemList = new ArrayList<>();
		// 選択している項目を取得
		checkTreeSelect(this.treeItem, facilityItemList, treeItemList, parentItemList);
		if (m_log.isDebugEnabled()) {
			m_log.debug("setCheckedTreeInfo() treeItemList Size:" + treeItemList.size() + ", parentItemList size:" + parentItemList.size());
		}
		// ツリーの末端でチェックの入っている項目（スコープ、ノード）のリストを作成
		List<FacilityTreeItemResponse> checkedItemList = new ArrayList<>(treeItemList);
		for (FacilityTreeItemResponse treeItem : treeItemList) {
			if (treeItem.getData() == null || treeItem.getData().getFacilityId() == null) {
				if (m_log.isTraceEnabled()) {
					m_log.trace("setCheckedTreeInfo() remove treeItem, data or FacilityId is null.");
				}
				checkedItemList.remove(treeItem);
				continue;
			}
			if (m_log.isTraceEnabled()) {
				m_log.trace("setCheckedTreeInfo() treeItem FacilityId=" + treeItem.getData().getFacilityId());
			}
			for (FacilityTreeItemResponse parentItem : parentItemList) {
				if (treeItem.getParent() == null || treeItem.getParent().getData().getFacilityId() == null
						|| parentItem.getData() == null || parentItem.getData().getFacilityId() == null) {
					if (m_log.isTraceEnabled()) {
						m_log.trace("    setCheckedTreeInfo() skip, parent(data or FacilityId) is null.");
					}
					continue;
				}
				if (treeItem.getParent().getData().getFacilityId().equals(parentItem.getData().getFacilityId())) {
					checkedItemList.remove(treeItem);
				}
			}
		}
		if (m_log.isTraceEnabled()) {
			for (FacilityTreeItemResponse parentItem : parentItemList) {
				m_log.trace("setCheckedTreeInfo() parentItem FacilityId=" + parentItem.getData().getFacilityId());
			}
			for (FacilityTreeItemResponse item : checkedItemList) {
				m_log.trace("setCheckedTreeInfo() checkedItem FacilityId=" + item.getData().getFacilityId());
			}
		}

		// チェック状態の復元（該当項目のみにチェックが入る）
		FacilityTreeItemResponse treeItemArr[] = treeItemList.toArray(new FacilityTreeItemResponse[treeItemList.size()]);
		CheckboxTreeViewer tree = (CheckboxTreeViewer) getTreeViewer();
		if (m_log.isTraceEnabled()) {
			m_log.trace("setCheckedTreeInfo() start of call tree.setCheckedElements()");
		}
		tree.setCheckedElements(treeItemArr);	// 時間がかかる
		if (m_log.isTraceEnabled()) {
			m_log.trace("setCheckedTreeInfo() end of call tree.setCheckedElements()");
		}

		// チェックとグレーを更新
		// ・選択項目に新たに子供がいた場合は、子供にチェックをつける。
		// ・子の一部がチェックされている場合は、親をグレー状態にする。
		for (FacilityTreeItemResponse item : checkedItemList) {
			checkPath(tree, item, true);
		}
	}

	/**
	 * 引数で指定されたtreeItemの要素に対して、選択状態にするかチェックし、選択状態にするListと選択状態の子持ちのListを作成します。
	 * 
	 * @param treeItem ツリーに表示しているFacilityTreeItem
	 * @param facilityStringList 選択情報のStringのList。形式は、メンバ変数 selectFacilityList と同じ。
	 * @param facilityList 選択状態にするFacilityTreeItemのリスト(戻り値)
	 * @param parentFacilityList 選択状態の子持ちのFacilityTreeItemのリスト(戻り値)
	 */
	private void checkTreeSelect(FacilityTreeItemResponse treeItem, List<String> facilityStringList, 
			List<FacilityTreeItemResponse> facilityList, List<FacilityTreeItemResponse> parentFacilityList) {
		if (treeItem == null || facilityStringList == null || facilityList == null || parentFacilityList == null) {
			m_log.warn("checkTreeSelect() treeItem or facilityStringList or facilityList or parentFacilityList is null, they must not be null.");
			return;
		}
		if (facilityStringList.size() == 0) {
			if (m_log.isDebugEnabled()) {
				m_log.debug("checkTreeSelect() facilityStringList is empty.");
			}
			return;
		}
		if (m_log.isTraceEnabled()) {
			// 再帰のため大量に出力
			m_log.trace("checkTreeSelect() treeItem FacilityId=" + treeItem.getData().getFacilityId());
		}

		List<FacilityTreeItemResponse> treeItemList = treeItem.getChildren();
		for (FacilityTreeItemResponse childItem : treeItemList) {
			checkTreeSelect(childItem, facilityStringList, facilityList, parentFacilityList);
		}

		Pattern splitter = Pattern.compile(SEPARATOR_HASH_EX_HASH);
		String facilityId = treeItem.getData().getFacilityId();
		for (String detail : facilityStringList) {
			String managerName = "";
			String parentFacilityId = "";
			String details[] = splitter.split(detail);
			String facilityType = details[details.length-1];
			if (treeItem.getData().getFacilityType() == FacilityTypeEnum.COMPOSITE) {
				// 「スコープ」の場合はDetailにFacilityTypeのみ入っている
				if (details.length == 1) {
					if (m_log.isDebugEnabled()) {
						m_log.debug("checkTreeSelect() selected(have sub:composite) managerName:" + managerName + ", parentFacilityId:" + parentFacilityId + ", facilityId:" + facilityId);
					}
					facilityList.add(treeItem);
					parentFacilityList.add(treeItem);
				}
			} else {
				managerName = ScopePropertyUtil.getManager(treeItem).getData().getFacilityId();
				parentFacilityId = treeItem.getParent().getData().getFacilityId();
				if (Integer.parseInt(facilityType) == FacilityConstant.TYPE_NODE && details[0].equals(managerName) 
						&& details[1].equals(parentFacilityId) && details[2].equals(facilityId)) {
					if (m_log.isDebugEnabled()) {
						m_log.debug("checkTreeSelect() selected tree managerName:" + managerName + ", parentFacilityId:" + parentFacilityId + ", facilityId:" + facilityId);
					}
					facilityList.add(treeItem);
				} else if ((Integer.parseInt(facilityType) == FacilityConstant.TYPE_SCOPE || Integer.parseInt(facilityType) == FacilityConstant.TYPE_MANAGER) 
								&& details[0].equals(managerName) && details[1].equals(facilityId)) {
					// nodeの場合はmanagerName、facilityId、parentFacilityIdが入っている
					// scopeまたはmanagerの場合は、managerName、facilityIdが入っている
					if (m_log.isDebugEnabled()) {
						m_log.debug("checkTreeSelect() selected(have sub) managerName:" + managerName + ", parentFacilityId:" + parentFacilityId + ", facilityId:" + facilityId);
					}
					facilityList.add(treeItem);
					parentFacilityList.add(treeItem);
				}
			}
		}
	}

	/**
	 * 選択中のファシリティID階層のリストを取得します。
	 * 形式は、selectFacilityListを参照。
	 * 
	 * @return 選択中のファシリティID階層のリスト
	 */
	public List<String> getSelectFacilityList() {
		if (this.selectFacilityList == null) {
			this.selectFacilityList = getCheckedTreeInfo();
		}

		return this.selectFacilityList;
	}

	/**
	 * 選択中のファシリティID階層のリストを設定します。
	 * 形式は、selectFacilityListを参照。
	 * 
	 * @param selectFacilityList 選択中のファシリティID階層のリスト
	 */
	public void setSelectFacilityList(List<String> selectFacilityList) {
		this.selectFacilityList = selectFacilityList;
	}

	/**
	 * 選択中のファシリティID階層のリストを削除します。
	 */
	public void deleteSelectFacilityList() {
		this.selectFacilityList = null;
	}

	/**
	 * 兄弟アイテム検索（ノードスコープ検索）
	 * @param current 選択アイテム
	 * @param keyword 検索文字列（前方一致検索）
	 * @return 検索結果アイテム
	 */
	private FacilityTreeItemResponse searchNeighbors( FacilityTreeItemResponse current, String keyword ){
		FacilityTreeItemResponse found;
		FacilityTreeItemResponse parent = current.getParent();
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
	private FacilityTreeItemResponse searchChildren( FacilityTreeItemResponse parent, String keyword, int offset ){
		List<FacilityTreeItemResponse> children = parent.getChildren();
		Collections.sort(children, new FacilityTreeViewerSorter.FacilityTreeItemComparator());
		int len = children.size();
		for( int i = offset; i<len; i++ ){
			FacilityTreeItemResponse child = children.get(i);

			if(child.getData().getFacilityId() != null
					&& -1 != child.getData().getFacilityId().indexOf( keyword ) ){
				return child;
			}else{
				FacilityTreeItemResponse found = searchChildren( child, keyword, 0 );
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
	private FacilityTreeItemResponse searchItem( FacilityTreeItemResponse item, String keyword ){
		FacilityTreeItemResponse found;

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
		FacilityTreeItemResponse result = searchItem( (FacilityTreeItemResponse)( null != targetItem ? targetItem: treeViewer.getInput() ), keyword );
		if( null != result ){
			FacilityTreeItemResponse trace = result;
			LinkedList<FacilityTreeItemResponse> pathList = new LinkedList<>();
			do{
				pathList.addFirst( trace );
				trace = trace.getParent();
			}while( null != trace );
			TreePath path = new TreePath( pathList.toArray(new FacilityTreeItemResponse[]{}) );
			treeViewer.setSelection( new TreeSelection(path), true );
		}else{
			MessageDialog.openInformation( this.getShell(), Messages.getString("message"), Messages.getString("search.not.found") );
			treeViewer.setSelection( new StructuredSelection(((FacilityTreeItemResponse)treeViewer.getInput()).getChildren().get(0)), true );
		}
	}
	
	/**
	 * 画面表示のためFacilityTreeItemの親に空のItemを設定する
	 * 
	 * @param childTree 対象FacilityTreeItem
	 * @return 親に空を設定したFacilityTreeItem
	 */
	protected FacilityTreeItemResponse addEmptyParent(FacilityTreeItemResponse childTree) {
		FacilityTreeItemResponse rootTree = null;

		if (childTree != null) {
			// 木構造最上位インスタンスの生成
			rootTree = new FacilityTreeItemResponse();
			FacilityInfoResponse rootInfo = new FacilityInfoResponse();
			rootInfo.setBuiltInFlg(true);
			rootInfo.setFacilityName(FacilityConstant.STRING_COMPOSITE);
			rootInfo.setFacilityType(FacilityTypeEnum.COMPOSITE);
			rootTree.setData(rootInfo);
			childTree.setParent(rootTree);
			rootTree.getChildren().add(childTree);
		}
		
		return rootTree;
	}

}
