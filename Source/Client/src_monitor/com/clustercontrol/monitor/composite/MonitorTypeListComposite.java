/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.composite;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * 監視種別を一覧表示するコンポジットクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class MonitorTypeListComposite extends Composite {

	//	 ----- instance フィールド ----- //
	/** 選択アイテム */
	private ArrayList<?> selectItem = null;

	/** リストビューア */
	private ListViewer monitorTypeList = null;

	// ----- コンストラクタ ----- //


	public MonitorTypeListComposite(Composite parent, int style) {
		super(parent, style);
		this.createContents();
	}

	// ----- instance メソッド ----- //

	/**
	 * コンポジットを生成します。
	 */
	private void createContents() {

		// コンポジットのレイアウト定義
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);

		// 監視種別リストのリストビューアの定義
		this.monitorTypeList = new ListViewer(this, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE |SWT.BORDER);
		GridData gdata = new GridData(GridData.FILL_BOTH);
		this.monitorTypeList.getList().setLayoutData(gdata);

		// 監視種別リストの選択されたアイテムの取得
		this.monitorTypeList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				selectItem = (ArrayList<?>)selection.getFirstElement();
			}
		});
	}

	/**
	 * 現在選択されているリストアイテムを返します。
	 * 
	 * @return ツリーアイテム
	 */
	public ArrayList<?> getSelectItem() {
		return this.selectItem;
	}
	/**
	 * 監視種別リストを更新します。（未実装）
	 */
	@Override
	public void update() {
	}

	/**
	 * 監視種別リスト
	 */
	public void clear() {
		monitorTypeList.refresh();
	}

	/**
	 * 監視種別リストの取得
	 * @return
	 */
	public ListViewer getMonitorTypeList() {
		return monitorTypeList;
	}

	/**
	 * 監視種別リストの設定
	 * @param monitorTypeList 監視種別リスト
	 */
	public void setMonitorTypeList(ListViewer monitorTypeList) {
		this.monitorTypeList = monitorTypeList;
	}
}
