/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.view;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.collect.composite.CollectGraphComposite;
import com.clustercontrol.collect.composite.CollectSettingComposite;
import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * 性能[グラフ]ビュー
 *
 * @version 5.0.0
 * @since 4.0.0
 *
 */
public class CollectGraphView extends CommonViewPart {
	private static final Log m_log = LogFactory.getLog(CollectGraphView.class);

	public static final String ID = CollectGraphView.class.getName();

	// コンポジット
	private FacilityTreeComposite scopeTreeComposite = null;
	private CollectSettingComposite collectSettingComposite = null; 
	private CollectGraphComposite collectGraphComposite;

	/** サッシュフォーム */
	private SashForm treeSash = null;


	/** 追加コンポジットのベース */
	private Composite baseComposite = null;

	/** スコープツリーのコンポジットと右側のコンポジットの割合。 */
	private int sashPer = 25;
	/** 区切り文字(##@##) */
	protected static final String SEPARATOR_HASH_HASH_AT_HASH_HASH = "##@##";

	
	/**
	 * デフォルトコンストラクタ
	 */
	public CollectGraphView() {
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
		WidgetTestUtil.setTestId(this, null, baseComposite);
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
		
		// 収集種別コンポジット
		collectSettingComposite = new CollectSettingComposite(baseCompositeSash, SWT.NONE, this);

		//// サッシュの右側
		// ビューに張るコンポジットの初期化
		collectGraphComposite = new CollectGraphComposite(treeSash, SWT.NONE, this);
		WidgetTestUtil.setTestId(this, null, collectGraphComposite);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 5;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		collectGraphComposite.setLayoutData(gridData);
		
		// Sashの境界を調整 左部20% 右部80%
		treeSash.setWeights(new int[] { sashPer, 100 - sashPer });
		baseCompositeSash.setWeights(new int[] { 40, 60 });

		// ツリーアイテム選択時のリスナー追加
		this.scopeTreeComposite.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// 選択アイテム取得(ツリー自体でも行っているが、念のため)
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				FacilityTreeItem selectItem = (FacilityTreeItem) selection
						.getFirstElement();
				if (selectItem != null) {
					baseComposite.layout(true, true);
					// イベントメソッド呼び出し
					// doSelectTreeItem(selectItem);
				}
			}
		});
		((CheckboxTreeViewer)this.scopeTreeComposite.getTreeViewer()).addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				// itemCodeListを取得して画面に反映する
				setItemCodeCheckedTreeItems();
			}
		});

		// チェック状態を復元する
		setSelectTreeItem(null);
		
	}

	/**
	 * ビュー更新
	 */
	public void update() {
		long start = System.currentTimeMillis();
		m_log.debug("update()");
//		graphComposite.update();

		m_log.debug(String.format("update: %dms", System.currentTimeMillis() - start));
	}

	/**
	 * 選択されたファシリティからマネージャ名のリストを作成し、
	 * Manager側からitemCodeリストを取得して画面に表示します。<br>
	 * ファシリティ未選択の場合はitemCodeリストを空にします。
	 * 
	 */
	public void setItemCodeCheckedTreeItems() {
		m_log.debug("setItemCodeCheckedTreeItems()");
		// 収集値表示名リスト、サマリータイプ、グラフ種別、折り返しチェックなどをクリアする
		collectSettingComposite.clearItem();
		
		List<String> selectStrList = scopeTreeComposite.getCheckedTreeInfo();
		if (selectStrList != null && selectStrList.size() != 0) {
			
			m_log.debug("setItemCodeCheckedTreeItems() itemCodeリストをManager側に取りにいきます");
			// マネージャ名からitemNameを取得し、画面に反映します
			collectSettingComposite.setCollectorItemCombo();
			collectSettingComposite.setSummaryItemCombo();
			collectSettingComposite.setGraphTypeItemCombo();
		}
	}

	@Override
	public void setFocus() {
	}

	/**
	 * チェックツリーから選択済みのObjectリストを取得し、
	 * FacilityTreeItemに変換してからFacilityTreeItemのリストを返します。
	 * @return
	 */
	public List<FacilityTreeItem> getCheckedTreeItemList() {
		Object[] objs = ((CheckboxTreeViewer)this.scopeTreeComposite.getTreeViewer()).getCheckedElements();
		List<FacilityTreeItem> itemList = new ArrayList<>();
		for (Object obj : objs) {
			if (obj instanceof FacilityTreeItem) {
				itemList.add((FacilityTreeItem)obj);
			}
		}
		return itemList;
	}
	
	public CollectGraphComposite getCollectGraphComposite() {
		return collectGraphComposite;
	}
	
	public CollectSettingComposite getCollectSettingComposite() {
		return collectSettingComposite;
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
		treeSash.setMaximizedControl(collectGraphComposite);
	}
	
	/**
	 * 画面起動時にチェック状態にします。
	 * 
	 * @param treeItem
	 */
	private void setSelectTreeItem(List<String> selectNodeMapList) {
		
		m_log.debug("setSelectTreeItem ファシリティツリーと収集値表示名をPreference情報を元に復元します");
		
		// Preferenceから情報取得
		List<String> selectList = selectNodeMapList;
		if (selectList == null) {
			selectList = getSelectedInfoList();
		}

		// ツリーの選択状態の復元
		this.scopeTreeComposite.setSelectFacilityList(selectList);
		
		// ツリーのチェックに応じてitemCodeを取得し画面に反映する
		if (getCheckedTreeItemList().size() == 0) return;
		setItemCodeCheckedTreeItems();
		collectSettingComposite.setDefaultItemInfo();
		
	}
	
	/**
	 * Preferenceで保持しているP_COLLECT_GRAPH_SELECT_NODE_INFO(ツリー選択状態)を、"##@##"でsplitして、Stringのリストで返します。
	 * 
	 */
	private List<String> getSelectedInfoList() {
		String treeSelect = ClusterControlPlugin.getDefault().getPreferenceStore().getString(
				CollectSettingComposite.P_COLLECT_GRAPH_SELECT_NODE_INFO);
		if (treeSelect == null || treeSelect.equals("")) {
			return null;
		}
		String array[] = treeSelect.split(SEPARATOR_HASH_HASH_AT_HASH_HASH);
		List<String> selectList = Arrays.asList(array);
		return selectList;
	}
	
	/**
	 * メンバで保持しているFacilityTreeCompositeを返します。
	 * @return
	 */
	public FacilityTreeComposite getFacilityTreeComposite() {
		return this.scopeTreeComposite;
	}
	
	public void setSelectFacilityListFromNodemap(List<String> selectNodeMapList) {
		setSelectTreeItem(selectNodeMapList);
		this.scopeTreeComposite.update();
	}

}
