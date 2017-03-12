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

package com.clustercontrol.repository.composite;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.repository.action.GetScopeListTableDefine;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.composite.action.ScopeDoubleClickListener;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * スコープ登録一覧コンポジットクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ScopeListComposite extends Composite {

	// ----- instance フィールド ----- //

	private FacilityTreeItem m_facilityTreeItem = null;
	private FacilityTreeItem m_selectFacilityTreeItem = null;

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	/** 全件ラベル */
	private Label totalLabel = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public ScopeListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	// ----- instance メソッド ----- //

	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		this.totalLabel = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "totallabel", totalLabel);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.totalLabel.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(GetScopeListTableDefine.get(),
				GetScopeListTableDefine.SORT_COLUMN_INDEX1,
				GetScopeListTableDefine.SORT_COLUMN_INDEX2,
				GetScopeListTableDefine.SORT_ORDER);

		// ダブルクリックリスナの追加
		this.tableViewer.addDoubleClickListener(new ScopeDoubleClickListener(this));

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}
	}

	/**
	 * スコープ一覧を表示します。
	 *
	 * @param item
	 *            選択されたFacilityTreeItem
	 */
	public void update(FacilityTreeItem item) {
		ArrayList<Object> listInput = new ArrayList<Object>();
		if (item instanceof FacilityTreeItem) {
			m_facilityTreeItem = item;

			int type = item.getData().getFacilityType();
			FacilityTreeItem manager = ScopePropertyUtil.getManager(item);
			if (type != FacilityConstant.TYPE_NODE
					&& item.getChildren() != null) {
				for (FacilityTreeItem child : item.getChildren()) {
					FacilityInfo scope = child.getData();
					ArrayList<Object> a = new ArrayList<Object>();
					if(manager == null) {
						a.add(scope.getFacilityId());
						a.add("");
						a.add("");
						a.add("");
						a.add("");
						a.add("");
					} else {
						a.add(manager.getData().getFacilityId());
						a.add(scope.getFacilityId());
						a.add(scope.getFacilityName());
						a.add(scope.getDescription());
						a.add(scope.getOwnerRoleId());
						a.add("");
					}
					a.add(null);
					listInput.add(a);
				}
			}
		}

		this.tableViewer.setInput(listInput);

		// 件数表示
		Object[] args = { String.valueOf(listInput.size()) };
		this.totalLabel.setText(Messages.getString("records", args));
	}

	/**
	 * tableViewerを返します。
	 *
	 * @return tableViewer
	 */
	public CommonTableViewer getTableViewer() {
		return this.tableViewer;
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
	 * 選択ファシリティツリーアイテムを設定します。
	 *
	 * @param facilityTreeItem ファシリティツリーアイテム
	 */
	public void setSelectFacilityTreeItem(FacilityTreeItem facilityTreeItem) {
		m_selectFacilityTreeItem = facilityTreeItem;
	}
	/**
	 * @return 選択されているアイテムを返します。
	 */
	public FacilityTreeItem getSelectItem() {
		return this.m_selectFacilityTreeItem;
	}
	/**
	 * @return m_facilityTreeItemを返します。
	 */
	public FacilityTreeItem getFacilityTreeItem() {
		return m_facilityTreeItem;
	}
}
