/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.rpa.composite.RpaScenarioSummaryGraphComposite;
import com.clustercontrol.view.CommonViewPart;

/**
 * 集計グラフビュー
 *
 */
public class RpaScenarioSummaryGraphView extends CommonViewPart {
	private static final Log m_log = LogFactory.getLog(RpaScenarioSummaryGraphView.class);

	public static final String ID = RpaScenarioSummaryGraphView.class.getName();

	// コンポジット
	private FacilityTreeComposite scopeTreeComposite = null;
	private RpaScenarioSummaryGraphComposite summaryGraphComposite;
	
	private List<String> selectScopeList;

	/** サッシュフォーム */
	private SashForm treeSash = null;

	/** 追加コンポジットのベース */
	private Composite baseComposite = null;

	/** スコープツリーのコンポジットと右側のコンポジットの割合。 */
	private int sashPer = 25;

	
	/**
	 * デフォルトコンストラクタ
	 */
	public RpaScenarioSummaryGraphView() {
	}
	
	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ビューを生成します。
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		// レイアウト設定
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		// サッシュフォーム作成及び設定
		treeSash = new SashForm(parent, SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		treeSash.setLayoutData(gridData);

		//// サッシュの左側
		baseComposite = new Composite(treeSash, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		baseComposite.setLayoutData(gridData);
		FillLayout flayout = new FillLayout(SWT.VERTICAL);
		flayout.marginHeight = 0;
		flayout.marginWidth = 0;
		baseComposite.setLayout(flayout);
		
		SashForm baseCompositeSash = new SashForm(baseComposite, SWT.VERTICAL);
		Composite treeBaseComposite = new Composite(baseCompositeSash, SWT.NONE);
		treeBaseComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

		// ファシリティツリーコンポジット
		scopeTreeComposite = new FacilityTreeComposite(treeBaseComposite, SWT.NONE, null,
				false, // scope only
				false, // unregistered
				false, // internal
				true, // topic refresh
				true // checkflg
				);

		//// サッシュの右側
		// ビューに張るコンポジットの初期化
		summaryGraphComposite = new RpaScenarioSummaryGraphComposite(treeSash, SWT.NONE, this);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 5;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		summaryGraphComposite.setLayoutData(gridData);
		
		// Sashの境界を調整 左部20% 右部80%
		treeSash.setWeights(new int[] { sashPer, 100 - sashPer });

		// ツリーアイテム選択時のリスナー追加
		this.scopeTreeComposite.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// 選択アイテム取得(ツリー自体でも行っているが、念のため)
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				FacilityTreeItemResponse selectItem = (FacilityTreeItemResponse) selection
						.getFirstElement();
				if (selectItem != null) {
					baseComposite.layout(true, true);
				}
			}
		});
		((CheckboxTreeViewer)this.scopeTreeComposite.getTreeViewer()).addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				setScopeListCheckedTreeItems();
			}
		});
	}

	/**
	 * ビュー更新
	 */
	public void update() {
		long start = System.currentTimeMillis();
		m_log.debug("update()");

		m_log.debug(String.format("update: %dms", System.currentTimeMillis() - start));
	}

	/**
	 * 選択されたファシリティからマネージャ名のリストを作成し、保持します
	 */
	public void setScopeListCheckedTreeItems() {
		List<String> selectStrList = scopeTreeComposite.getCheckedTreeInfo();
		if (selectStrList != null && selectStrList.size() != 0) {
			selectScopeList = selectStrList;
		} else {
			selectScopeList = new ArrayList<>();
		}
	}

	@Override
	public void setFocus() {
	}
	
	public List<String> getSelectScopeList(){
		// 画面の最新情報から取得し直す
		setScopeListCheckedTreeItems();
		
		return selectScopeList;
	}

	/**
	 * チェックツリーから選択済みのObjectリストを取得し、
	 * FacilityTreeItemに変換してからFacilityTreeItemのリストを返します。
	 * @return
	 */
	public List<FacilityTreeItemResponse> getCheckedTreeItemList() {
		Object[] objs = ((CheckboxTreeViewer)this.scopeTreeComposite.getTreeViewer()).getCheckedElements();
		List<FacilityTreeItemResponse> itemList = new ArrayList<>();
		for (Object obj : objs) {
			if (obj instanceof FacilityTreeItemResponse) {
				itemList.add((FacilityTreeItemResponse)obj);
			}
		}
		return itemList;
	}
	
	public RpaScenarioSummaryGraphComposite getSummaryGraphComposite() {
		return summaryGraphComposite;
	}

	/**
	 * 表示します。
	 */
	public void show() {
		m_log.debug("show");
		treeSash.setMaximizedControl(null);
	}

	/**
	 * 隠します。
	 */
	public void hide() {
		m_log.debug("hide");
		treeSash.setMaximizedControl(summaryGraphComposite);
	}
	
	/**
	 * メンバで保持しているFacilityTreeCompositeを返します。
	 * @return
	 */
	public FacilityTreeComposite getFacilityTreeComposite() {
		return this.scopeTreeComposite;
	}
	

}
