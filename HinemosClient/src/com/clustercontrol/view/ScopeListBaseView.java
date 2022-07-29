/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.view;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.FacilityTreeItemUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * スコープツリーと合わせて利用するビューを作成するための基本的な実装を持つクラス<BR>
 * <p>
 *
 * 基本的には、createContentsをオーバーライドし、追加コンテンツを生成して下さい。 <br>
 * また、必要に応じてdoSelectTreeItemをオーバーライドし、ツリーアイテム選択時の イベント処理を実装して下さい。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class ScopeListBaseView extends AutoUpdateView {
	private static Log m_log = LogFactory.getLog(ScopeListBaseView.class);

	// ----- instance フィールド ----- //

	/** サッシュフォーム */
	private SashForm treeSash = null;

	/** スコープツリーのコンポジット */
	private FacilityTreeComposite scopeTreeComposite = null;

	/** 追加コンポジットのベース */
	private Composite baseComposite = null;

	/** 追加コンポジット */
	private Composite listComposite = null;

	/** パス文字列を表示するラベル */
	private Label pathLabel = null;

	/** ノードをツリーに含めるか？ */
	private boolean scopeOnly = false;

	/** 未登録ノード　スコープをツリーに含めるか？**/
	private boolean unregistered = true;

	/** 内部イベント　スコープをツリーに含めるか？**/
	private boolean internal = true;

	/** リポジトリ情報更新により、表示をリフレッシュするかどうか **/
	private boolean topicRefresh = true;
	
	/** スコープツリーのコンポジットと右側のコンポジットの割合。 */
	private int sashPer = 30;

	// ----- コンストラクタ ----- //

	/**
	 * 親のコンストラクタを呼び出します。
	 */
	public ScopeListBaseView() {
		super();
		this.scopeOnly = false;
		this.unregistered = true;
		this.internal = true;
		this.topicRefresh = true;
	}

	/**
	 * 親のコンストラクタを呼び出します。
	 *
	 * @param scopeOnly スコープのみのスコープツリーとするかどうか
	 * @param unregistered UNREGISTEREDをスコープツリーに含めるかどうか
	 * @param internal INTERNALをスコープツリーに含めるかどうか
	 * @param topicRefresh リポジトリ情報が更新された際に画面リフレッシュするかどうか
	 */
	public ScopeListBaseView(boolean scopeOnly ,boolean unregistered, boolean internal, boolean topicRefresh) {
		super();

		this.scopeOnly = scopeOnly;
		this.unregistered = unregistered;
		this.internal = internal;
		this.topicRefresh = topicRefresh;
	}

	// ----- instance メソッド ----- //

	/**
	 * ビューを生成します。
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		m_log.trace("createPartControl");
		super.createPartControl(parent);

		// レイアウト設定
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// サッシュフォーム作成及び設定
		this.treeSash = new SashForm(parent, SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		this.treeSash.setLayoutData(gridData);

		// スコープツリー作成
		this.scopeTreeComposite = new FacilityTreeComposite(treeSash, SWT.NONE, null, this.scopeOnly, this.unregistered, this.internal, this.topicRefresh, false);

		// 追加コンポジットのベース作成
		this.baseComposite = new Composite(this.treeSash, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, baseComposite);

		// パス文字列表示ラベル作成
		this.pathLabel = new Label(this.baseComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "path", pathLabel);
		this.pathLabel.setText(Messages.getString("scope") + " : ");
		//		this.pathLabel.pack();

		// 追加コンテンツ作成
		this.listComposite = this.createListContents(this.baseComposite);

		// Sashの境界を調整 左部30% 右部70%
		treeSash.setWeights(new int[] { sashPer, 100 - sashPer });

		// ツリーアイテム選択時のリスナー追加
		this.scopeTreeComposite.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// 選択アイテム取得(ツリー自体でも行っているが、念のため)
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				FacilityTreeItemResponse selectItem = (FacilityTreeItemResponse) selection.getFirstElement();
				if (selectItem != null) {
					// パスラベルの更新
					baseComposite.layout(true, true);
					// イベントメソッド呼び出し
					doSelectTreeItem(selectItem);
				}
			}
		});
	}
	
	/**
	 * Sashの割合を変更したいときに呼ぶ。
	 * コンストラクタ内部で呼ぶこと。
	 * @param per
	 */
	protected void setSash(int per) {
		sashPer = per;
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
	protected abstract Composite createListContents(Composite parent);

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
	 * スコープツリーのアイテムがチェックされた場合に呼び出されるメソッド
	 * 
	 * 必要に応じてオーバーライドし、アイテムチェック時のイベント処理を実装してください。
	 * @param item スコープツリーアイテム
	 */
	protected void doSelectTreeItems(FacilityTreeItemResponse[] item) {
		
	}

	/**
	 * このビューのレイアウトを構築するサッシュフォームを返します。
	 *
	 * @return サッシュフォーム
	 */
	public SashForm getTreeSash() {
		return this.treeSash;
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
	 * 追加コンポジットのベースを返します。
	 *
	 * @return 追加コンポジットのベース
	 */
	public Composite getBaseComposite() {
		return this.baseComposite;
	}

	/**
	 * 追加コンポジットを返します。
	 *
	 * @return 追加コンポジット
	 */
	public Composite getListComposite() {
		return this.listComposite;
	}

	/**
	 * パス文字列を表示するラベルを返します。
	 *
	 * @return パス文字列を表示するラベル
	 */
	public void setPathLabel(String str) {
		if (str != null) {
			pathLabel.setText(str);
			pathLabel.pack();
		}
	}

	/**
	 * 表示します。
	 */
	public void show() {
		this.treeSash.setMaximizedControl(null);
	}

	/**
	 * 隠します。
	 */
	public void hide() {
		this.treeSash.setMaximizedControl(this.baseComposite);
	}

	/**
	 * 指定されたマネージャ配下のスコープを選択します。
	 */
	public void selectScope(String managerName, String facilityId) {
		AtomicBoolean managerFound = new AtomicBoolean(false);
		FacilityTreeItemResponse abort = new FacilityTreeItemResponse();
		FacilityTreeItemResponse found = FacilityTreeItemUtil.visitTreeItems(this.scopeTreeComposite.getAllTreeItems(), item -> {
			if (!managerFound.get()) {
				// まずはマネージャを探す
				if (item.getData().getFacilityType() == FacilityTypeEnum.MANAGER
						&& Objects.equals(managerName, item.getData().getFacilityId())) {
					managerFound.set(true);
				}
			} else {
				// マネージャが見つかったらスコープを探す
				if (item.getData().getFacilityType() == FacilityTypeEnum.MANAGER) {
					return abort;	// 別のマネージャに移ってしまった
				}
				if (Objects.equals(facilityId, item.getData().getFacilityId())) {
					return item;
				}
			}
			return null;
		});
		if (found == null || found == abort) {
			m_log.warn("selectScope: Not found. manager=" + managerName + ", facilityId=" + facilityId);
		} else {
			scopeTreeComposite.getTreeViewer().setSelection(new StructuredSelection(found), true);
		}
	}

	/**
	 * スコープツリーで選択されているマネージャ名を返します。
	 * 
	 * @return 選択されているマネージャ名。単一のマネージャが選択されていない場合はnull。
	 */
	public String getSingleSelectedManagerName() {
		String managerName = null;
		for (Object obj : getScopeTreeComposite().getSelectionList()) {
			FacilityTreeItemResponse item = (FacilityTreeItemResponse) obj;
			if (null == item || item.getData().getFacilityType() == FacilityTypeEnum.COMPOSITE) {
				// ルートを選択中 = 複数マネージャ選択中
				m_log.debug("getSingleSelectedManagerName: null (root)");
				return null;
			}
			String mn;
			if (item.getData().getFacilityType() == FacilityTypeEnum.MANAGER) {
				// マネージャを選択中
				mn = item.getData().getFacilityId();
			} else {
				// マネージャ配下のスコープを選択中
				FacilityTreeItemResponse manager = ScopePropertyUtil.getManager(item);
				mn = manager.getData().getFacilityId();
			}
			if (managerName != null && !managerName.equals(mn)) {
				// 複数マネージャ選択中
				m_log.debug("getSingleSelectedManagerName: null (" + managerName + ", " + mn + ")");
				return null;
			}
			managerName = mn;
		}
		m_log.debug("getSingleSelectedManagerName: " + managerName);
		return managerName;
	}

}
