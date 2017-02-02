/*

Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.composite;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * 共通設定種別を一覧表示するコンポジットクラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class HinemosPropertyTypeListComposite extends Composite {

	//	 ----- instance フィールド ----- //
	/** 選択アイテム */
	private String selectItem = null;

	/** リストビューア */
	private ListViewer HinemosPropertyTypeList = null;

	// ----- コンストラクタ ----- //


	public HinemosPropertyTypeListComposite(Composite parent, int style) {
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

		// 共通設定種別リストのリストビューアの定義
		this.HinemosPropertyTypeList = new ListViewer(this, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE |SWT.BORDER);
		GridData gdata = new GridData(GridData.FILL_BOTH);
		this.HinemosPropertyTypeList.getList().setLayoutData(gdata);

		// 共通設定種別リストの選択されたアイテムの取得
		this.HinemosPropertyTypeList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				selectItem = (String)selection.getFirstElement();
			}
		});
	}

	/**
	 * 現在選択されているリストアイテムを返します。
	 * 
	 * @return ツリーアイテム
	 */
	public String getSelectItem() {
		return this.selectItem;
	}
	/**
	 * 共通設定種別リストを更新します。（未実装）
	 */
	@Override
	public void update() {
	}

	/**
	 * 共通設定種別リスト
	 */
	public void clear() {
		HinemosPropertyTypeList.refresh();
	}

	/**
	 * 共通設定種別リストの取得
	 * @return
	 */
	public ListViewer getHinemosPropertyTypeList() {
		return HinemosPropertyTypeList;
	}

	/**
	 * 共通設定種別リストの設定
	 * @param monitorTypeList 共通設定種別リスト
	 */
	public void setHinemosPropertyTypeList(ListViewer monitorTypeList) {
		this.HinemosPropertyTypeList = monitorTypeList;
	}
}
