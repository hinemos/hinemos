/*

Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.composite;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * モジュール種別を一覧表示するコンポジットクラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class ModuleTypeListComposite extends Composite {

	//	 ----- instance フィールド ----- //
	/** 選択アイテム */
	private String selectItem = null;

	/** リストビューア */
	private ListViewer moduleTypeList = null;

	// ----- コンストラクタ ----- //


	public ModuleTypeListComposite(Composite parent, int style) {
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

		// モジュール種別リストのリストビューアの定義
		this.moduleTypeList = new ListViewer(this, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE |SWT.BORDER);
		GridData gdata = new GridData(GridData.FILL_BOTH);
		this.moduleTypeList.getList().setLayoutData(gdata);

		// モジュール種別リストの選択されたアイテムの取得
		this.moduleTypeList.addSelectionChangedListener(new ISelectionChangedListener() {
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
	 * モジュール種別リスト
	 */
	public void clear() {
		moduleTypeList.refresh();
	}

	/**
	 * モジュール種別リストの取得
	 * @return
	 */
	public ListViewer getMonitorTypeList() {
		return moduleTypeList;
	}

	/**
	 * モジュール種別リストの設定
	 * @param monitorTypeList モジュール種別リスト
	 */
	public void setMonitorTypeList(ListViewer monitorTypeList) {
		this.moduleTypeList = monitorTypeList;
	}
}
