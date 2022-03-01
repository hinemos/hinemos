/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.repository.action.GetScopeListTableDefine;
import com.clustercontrol.repository.composite.action.ScopeDoubleClickListener;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * スコープ登録一覧コンポジットクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ScopeListComposite extends Composite {

	// ----- instance フィールド ----- //

	private FacilityTreeItemResponse m_facilityTreeItem = null;
	private FacilityTreeItemResponse m_selectFacilityTreeItem = null;

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
	public void update(FacilityTreeItemResponse item) {
		ArrayList<Object> listInput = new ArrayList<Object>();
		if (item instanceof FacilityTreeItemResponse) {
			m_facilityTreeItem = item;
			FacilityTypeEnum type = item.getData().getFacilityType();
			FacilityTreeItemResponse manager = ScopePropertyUtil.getManager(item);
			if (type != FacilityTypeEnum.NODE
					&& item.getChildren() != null) {
				
				List<FacilityInfoResponse> facilityList = new ArrayList<FacilityInfoResponse>();
				
				for (FacilityTreeItemResponse child : item.getChildren()) {
					FacilityInfoResponse scope = child.getData();
					facilityList.add(scope);
				}
				// リストの作成し直し時にソート
				if (facilityList != null) {
					Collections.sort(facilityList, new Comparator<FacilityInfoResponse>() {
						@Override
						public int compare(FacilityInfoResponse info1, FacilityInfoResponse info2) {
							int order1 =  info1.getDisplaySortOrder();
							int order2 =  info2.getDisplaySortOrder();
							if(order1 == order2 ){
								String object1 = info1.getFacilityId();
								String object2 = info2.getFacilityId();
								return object1.compareTo(object2);
							}
							else {
								return (order1 - order2);
							}
						}
					});
					
					for (FacilityInfoResponse facilityInfo : facilityList) {
						ArrayList<Object> a = new ArrayList<Object>();
						if(manager == null) {
							a.add(facilityInfo.getFacilityId());
							a.add("");
							a.add("");
							a.add("");
							a.add("");
							a.add("");
						} else {
							a.add(manager.getData().getFacilityId());
							a.add(facilityInfo.getFacilityId());
							a.add(facilityInfo.getFacilityName());
							a.add(facilityInfo.getDescription());
							a.add(facilityInfo.getOwnerRoleId());
							a.add("");
						}
						a.add(null);
						listInput.add(a);
					}
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
	public void setSelectFacilityTreeItem(FacilityTreeItemResponse facilityTreeItem) {
		m_selectFacilityTreeItem = facilityTreeItem;
	}
	/**
	 * @return 選択されているアイテムを返します。
	 */
	public FacilityTreeItemResponse getSelectItem() {
		return this.m_selectFacilityTreeItem;
	}
	/**
	 * @return m_facilityTreeItemを返します。
	 */
	public FacilityTreeItemResponse getFacilityTreeItem() {
		return m_facilityTreeItem;
	}
}
