/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.composite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ErrorViewPart;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.composite.TreeSearchBarComposite;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.view.JobMapViewIF;
import com.clustercontrol.jobmanagement.viewer.JobTreeContentProvider;
import com.clustercontrol.jobmanagement.viewer.JobTreeLabelProvider;
import com.clustercontrol.jobmanagement.viewer.JobTreeViewer;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.util.JobMapTreeUtil;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobModuleView;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブマップ用ジョブツリーコンポジットクラスです。
 *
 * @version 6.0.a
 */
public class JobMapTreeComposite extends JobTreeComposite {

	// ログ
	private static Log m_log = LogFactory.getLog( JobMapTreeComposite.class );

	/** 選択アイテム */
	private JobTreeItem m_selectItem = null;
	private boolean isModuleOnly = false;

	/**
	 * コンストラクタ
	 * @param view ジョブツリービュー
	 * @param parent 親コンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public JobMapTreeComposite(Composite parent, int style, String ownerRoleId) {
		super(parent, style, ownerRoleId);
	}

	/**
	 * コンストラクタ
	 * @param view ジョブツリービュー
	 * @param parent 親コンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public JobMapTreeComposite(Composite parent, int style, String ownerRoleId, boolean isModuleOnly) {
		super(parent, style, ownerRoleId);
		this.isModuleOnly = isModuleOnly;
		removeModule(null);
	}
	
	/**
	 * コンポジットを構築します。
	 */
	@Override
	protected void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// Add search bar
		Composite compSearch = new TreeSearchBarComposite(this, SWT.NONE, enableKeyPress);
		WidgetTestUtil.setTestId(this, "search", compSearch);
		compSearch.setLayoutData( new GridData(GridData.FILL_HORIZONTAL) );

		Tree tree = new Tree(this, SWT.MULTI | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, tree);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		tree.setLayoutData(gridData);

		m_viewer = new JobTreeViewer(tree);
		m_viewer.setContentProvider(new JobTreeContentProvider());
		m_viewer.setLabelProvider(new JobTreeLabelProvider(m_useForView));

		// 選択アイテム取得イベント定義
		m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				IViewPart viewPart = page.findView(JobTreeView.ID);

				if (viewPart == null) {
					return;
				}
				JobTreeView view = (JobTreeView) viewPart.getAdapter(JobTreeView.class);
				
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				m_selectItemList.clear();
				m_selectItemList.addAll(selection.toList());

				//選択アイテムを取得
				m_selectItem = (JobTreeItem) selection.getFirstElement();
				List<?> list = selection.toList();
				List<JobTreeItem> itemList = new ArrayList<JobTreeItem>();
				for(Object obj : list) {
					if(obj instanceof JobTreeItem) {
						itemList.add((JobTreeItem)obj);
					}
				}

				IViewPart viewRegist = page.findView(JobModuleView.ID);
				if (viewRegist != null && !isModuleOnly) {
					// JobTreeの選択が変わったら、JobModuleTreeの表示を変える
					JobModuleView moduleView = (JobModuleView)viewRegist.getAdapter(JobModuleView.class);
					String managerName = JobTreeItemUtil.getManagerName(m_selectItem);
					moduleView.getJobMapTreeComposite().removeModule(managerName);
				}
				if (m_selectItem != null) {
					//選択ツリーアイテムを設定
					setSelectItem(itemList);

					// ログインユーザで参照可能なジョブユニットかどうかチェックする
					if (m_selectItem.getData().getType() == JobConstant.TYPE_JOBUNIT) {
						view.setEnabledActionAll(true);
						view.setEnabledAction(m_selectItem.getData().getType(), m_selectItem.getData().getJobunitId(), selection);
					} else {
						//ビューのアクションの有効/無効を設定
						view.setEnabledAction(m_selectItem.getData().getType(), m_selectItem.getData().getJobunitId(), selection);
					}

				} else {
					//選択ツリーアイテムを設定
					setSelectItem(null);
					//ビューのアクションを全て無効に設定
					view.setEnabledAction(-9, selection);
				}
			}
		});

		// 選択アイテム取得イベント定義
		m_viewer.addDoubleClickListener(new IDoubleClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void doubleClick(DoubleClickEvent event) {
				//選択アイテムを取得
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				m_selectItemList.clear();
				m_selectItemList.addAll(selection.toList());
				
				//ジョブ[登録]ビューのインスタンスを取得
				IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
				IViewPart viewPart = page.findView(JobTreeView.ID);

				if (viewPart != null) {
					m_selectItem = (JobTreeItem) selection.getFirstElement();
					List<?> list = selection.toList();
					List<JobTreeItem> itemList = new ArrayList<JobTreeItem>();
					for(Object obj : list) {
						if(obj instanceof JobTreeItem) {
							itemList.add((JobTreeItem)obj);
						}
					}
					
					if (isModuleOnly) {
						return;
					}
					JobTreeView view = (JobTreeView) viewPart.getAdapter(JobTreeView.class);

					if (m_selectItem != null) {
						updateJobMapEditor(m_selectItem);

						//選択ツリーアイテムを設定
						setSelectItem(itemList);

						// ログインユーザで参照可能なジョブユニットかどうかチェックする
						if (m_selectItem.getData().getType() == JobConstant.TYPE_JOBUNIT) {
							view.setEnabledActionAll(true);
							view.setEnabledAction(m_selectItem.getData().getType(), m_selectItem.getData().getJobunitId(), selection);
							updateJobMapEditor(m_selectItem);
						} else {
							//ビューのアクションの有効/無効を設定
							view.setEnabledAction(m_selectItem.getData().getType(), m_selectItem.getData().getJobunitId(), selection);
							updateJobMapEditor(m_selectItem);
						}

					} else {
						//選択ツリーアイテムを設定
						setSelectItem(null);

						//ビューのアクションを全て無効に設定
						view.setEnabledAction(-9, selection);
					}
				}
			}
		});

		// Drag処理
		Transfer[] transferTypes = new Transfer[] {TextTransfer.getInstance()};
		m_viewer.addDragSupport(DND.DROP_MOVE, transferTypes, new DragSourceAdapter() {
			@Override
			public void dragStart(DragSourceEvent event) {
				JobMapEditorView editorView = JobMapActionUtil.getJobMapEditorView();
				JobTreeItem dispItem = editorView.getFocusFigure().getJobTreeItem();
				JobEditState jobEditState = JobEditStateUtil.getJobEditState(JobTreeItemUtil.getManagerName(dispItem));
				boolean readOnly = jobEditState.isLockedJobunitId(dispItem.getData().getJobunitId());
				if (m_selectItem.getData().getType() == JobConstant.TYPE_JOBUNIT
						|| !readOnly) {
					event.doit = false;
				}
			}
			@Override
			public void dragSetData(DragSourceEvent event) {
				event.data = m_selectItem.getData().getJobunitId() 
						+ "," + m_selectItem.getData().getId() 
						+ "," + JobTreeItemUtil.getManagerName(m_selectItem);
			}
		});

		updateTree(m_useForView);
		m_viewer.expandToLevel(3);
	}

	/**
	 * ジョブマップツリーをモジュール登録されたものだけにします。
	 * 
	 */
	public void removeModule(String managerName) {
		m_log.trace("removeModule leave ManagerName:" + managerName);
		if (managerName == null || managerName.equals("")) {
			m_viewer.setInput(null);
			return;
		}
		JobTreeItem treeTop = new JobTreeItem();
		JobInfo treeInfo = new JobInfo();
		treeInfo.setJobunitId("");
		treeInfo.setId("");
		treeInfo.setName(JobConstant.STRING_COMPOSITE);
		treeInfo.setType(JobConstant.TYPE_COMPOSITE);
		treeTop.setData(treeInfo);
		
		List<JobTreeItem> jobTreeItemList = new ArrayList<>();
		JobTreeView jobTree = JobMapActionUtil.getJobTreeView();
		TreeItem items[] = jobTree.getJobMapTreeComposite().getTree().getItems();
//		TreeItem items[] = m_viewer.getTree().getItems();
		for (int i = 0; i < items.length; i++) {
			JobTreeItem jobItem = (JobTreeItem)items[i].getData();
			JobTreeItem jobTreeItem = JobMapTreeUtil.deepCopy(jobItem, null);
			removeModuleTreeItem(jobTreeItem.getChildren(), jobTreeItemList, managerName);
			jobTreeItem.setParent(treeTop);
			treeTop.getChildren().add(jobTreeItem);
		}
		m_viewer.setInput(treeTop);
		m_viewer.expandToLevel(3);
	}
	
	private void removeModuleTreeItem(List<JobTreeItem> jobTreeList, List<JobTreeItem> jobList, String targetManagerName) {
		Iterator<JobTreeItem> it = jobTreeList.iterator();
		while (it.hasNext()) {
			JobTreeItem jobItem = it.next();
			if (jobItem.getChildren().size() > 0) {
				removeModuleTreeItem(jobItem.getChildren(), jobList, targetManagerName);
			}
			if (jobItem.getData().getType() != JobConstant.TYPE_COMPOSITE 
					&& !JobTreeItemUtil.getManagerName(jobItem).equals(targetManagerName)) {
				it.remove();
				continue;
			}
			if (jobItem.getData().getType() == JobConstant.TYPE_MANAGER) {
				jobItem.getChildren().clear();
				for (JobTreeItem childItem : jobList) {
					childItem.setParent(jobItem);
					jobItem.getChildren().add(childItem);
				}
				jobList.clear();
				continue;
			}
			if (jobItem.getData().isRegisteredModule()) {
				jobItem.getChildren().clear();
				jobList.add(jobItem);
			} else {
				it.remove();
			}
		}
	}
	
	public void updateJobMapEditor(JobTreeItem jobTreeItem) {

		m_log.debug("updateJobMapEditor() : " + jobTreeItem);

		//アクティブページを手に入れる
		IWorkbenchPage page = PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getActivePage();
		//ジョブマップ[設定]ビューを更新する
		IViewReference viewReference = page.findViewReference("com.clustercontrol.jobmap.view.JobMapEditorView");
		if (viewReference == null){
			return;
		}
		IViewPart viewPart = viewReference.getView(false);
		if (viewPart != null && !(viewPart instanceof ErrorViewPart)) {
			m_log.debug("updateJobMapEditor() : " + viewPart.getClass().getName());
			JobMapViewIF view = (JobMapViewIF) viewPart;
			
			String managerName = null;
			JobTreeItem mgrTree = JobTreeItemUtil.getManager(jobTreeItem);
			if(mgrTree == null) {
				if (jobTreeItem != null) {
					managerName = jobTreeItem.getChildren().get(0).getData().getId();
				}
			} else {
				managerName = mgrTree.getData().getId();
			}
			
			view.update(managerName, null, jobTreeItem);
		} else {
			m_log.debug("updateJobMapEditor() :: viewPart is null. or ErrorViewPart.");
		}
	}
	
	@Override
	public void update() {
		super.update();
		if (this.isModuleOnly) {
			removeModule(null);
		}
		m_viewer.expandToLevel(3);
	}
}
