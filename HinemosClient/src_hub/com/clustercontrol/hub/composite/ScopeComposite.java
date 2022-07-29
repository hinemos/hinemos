/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.composite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.hub.view.LogScopeTreeView;
import com.clustercontrol.repository.composite.ScopeTreeSearchBarComposite;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.FacilityTreeContentProvider;
import com.clustercontrol.viewer.FacilityTreeLabelProvider;
import com.clustercontrol.viewer.FacilityTreeViewerSorter;

/**
 * スコープビューを構成するコンポジット
 * @since 7.0.0
 */
public class ScopeComposite extends FacilityTreeComposite {

	// ログ
	private static Log m_log = LogFactory.getLog( ScopeComposite.class );

	/**
	 * 引数5つのコンストラクタしか使用する予定はないので、
	 * 他のコンストラクタの実装は省略。
	 * 
	 * @param parent
	 * @param style
	 * @param scopeOnly
	 * @param unregistered
	 * @param internal
	 */
	public ScopeComposite(Composite parent, int style,
			boolean scopeOnly ,
			boolean unregistered,
			boolean internal) {
		super(parent,style, null, null,scopeOnly,unregistered,internal);
	}

	@Override
	/**
	 * コンポジットを生成します。
	 */
	protected void createContents() {
		FacilityTreeCache.addComposite(this);
		
		// コンポジットのレイアウト定義
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);
		
		// Add search bar
		Composite compSearch = new ScopeTreeSearchBarComposite(this, SWT.NONE, true);
		WidgetTestUtil.setTestId(this, "search", compSearch);
		compSearch.setLayoutData( new GridData(GridData.GRAB_HORIZONTAL) );
		
		// ツリー作成
		Tree tree = new Tree(this, SWT.SINGLE | SWT.BORDER);

		// ツリーのレイアウトデータ定義
		GridData layoutData = new GridData();
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		tree.setLayoutData(layoutData);

		// ツリービューア作成
		this.treeViewer = new TreeViewer(tree);

		// ツリービューア設定
		this.treeViewer.setContentProvider(new FacilityTreeContentProvider());
		this.treeViewer.setLabelProvider(new FacilityTreeLabelProvider());
		this.treeViewer.setSorter(new FacilityTreeViewerSorter());

		// 選択アイテム取得イベント定義
		this.treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();

				/*
				 * topicが飛ぶと、ぬるぽが出るので、null checkを入れる。
				 */
				selectItem = (FacilityTreeItemResponse) selection.getFirstElement();
				if (selectItem == null) {
					m_log.warn("selectionChanged(), selectionChanged selectItem is null");
					return;
				}

				subScopeNumber = selectItem.getChildren().size();

				try{
					// スコープツリービューのインスタンスを取得
					IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
					if (page == null) {
						m_log.debug("ScopeComposite page is null");
						return;
					}
					IViewReference viewReference = page.findViewReference(LogScopeTreeView.ID);
					if (viewReference == null) {
						m_log.debug("ScopeComposite viewReference is null");
						return;
					}
					IViewPart viewPart = viewReference.getView(false);
					if (viewPart == null) {
						m_log.debug("ScopeComposite viewPart is null");
						return;
					}
					if (viewPart instanceof LogScopeTreeView) {
						LogScopeTreeView view = (LogScopeTreeView) viewPart;
						// ビューのアクションの有効/無効を設定
						view.setEnabledAction();
					}
				} catch (Exception e) {
					m_log.warn("createContents(), " + e.getMessage(), e);
				}
			}
		});

		//Drag&Drop
		final ScopeComposite scopeComposite = this;
		Transfer[] transferTypes = new Transfer[] {TextTransfer.getInstance()};
		treeViewer.addDragSupport(DND.DROP_MOVE, transferTypes, new DragSourceListener() {
			@Override
			public void dragStart(DragSourceEvent event) {
				if (selectItem.getData().getFacilityType() != FacilityTypeEnum.NODE) {
					event.doit = false;
				}
			}
			
			@Override
			public void dragSetData(DragSourceEvent event) {
				event.data = selectItem.getData().getFacilityId();
			}
			
			@Override
			public void dragFinished(DragSourceEvent event) {
				m_log.debug("Drag finished.");
				m_log.debug("Drag operation: " + event.detail);
				if (event.detail == DND.DROP_MOVE) {
					scopeComposite.update();
				}
			}
		});
		
		// 表示します。
		this.update();
	}
}
