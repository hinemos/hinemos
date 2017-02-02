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

package com.clustercontrol.notify.composite;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


/**
 * 通知をタイプ別にリスト表示するコンポジットクラス<BR>
 * 
 * @version 3.0.0
 * @since 3.0.0
 */
public class NotifyTypeListComposite extends Composite {

	//	 ----- instance フィールド ----- //

	/** 選択アイテム */
	private Integer selectItem = null;

	/** リストビューア */
	private ListViewer notifyTypeList = null;

	// ----- コンストラクタ ----- //

	public NotifyTypeListComposite(Composite parent, int style) {
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
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);

		this.notifyTypeList = new ListViewer(this, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI |SWT.BORDER);
		GridData gdata = new GridData(GridData.FILL_BOTH);
		this.notifyTypeList.getList().setLayoutData(gdata);

		this.notifyTypeList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				selectItem = (Integer)selection.getFirstElement();
			}
		});

	}

	//	/**
	//	 * このコンポジットが利用するリストビューアを返します。
	//	 *
	//	 * @return リストビューア
	//	 */
	//	public ListViewer getListViewer() {
	//		return this.notifyTypeList;
	//	}

	/**
	 * 現在選択されているリストアイテムを返します。
	 * 
	 * @return ツリーアイテム
	 */
	public Integer getSelectItem() {
		return this.selectItem;
	}

	/**
	 * ビューの表示内容を更新します。
	 */
	@Override
	public void update() {

	}

	public void clear() {
		notifyTypeList.refresh();

	}

	public ListViewer getNotifyTypeList() {
		return notifyTypeList;
	}

	public void setNotifyTypeList(ListViewer notifyTypeList) {
		this.notifyTypeList = notifyTypeList;
	}

}
