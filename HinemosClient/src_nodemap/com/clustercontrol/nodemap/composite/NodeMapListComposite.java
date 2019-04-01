/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.nodemap.editpart.MapViewController;
import com.clustercontrol.nodemap.etc.action.NodeMapListTableDefine;
import com.clustercontrol.nodemap.util.FacilityElementComparator;
import com.clustercontrol.nodemap.util.RelationViewController;
import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.nodemap.FacilityElement;

public class NodeMapListComposite extends Composite{

	// ログ
	private static Log m_log = LogFactory.getLog( NodeMapListComposite.class );

	private NodeMapView _view;

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	/** 全件ラベル */
	private Label totalLabel = null;

	private MapViewController m_controller;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 * 
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public NodeMapListComposite(Composite parent, int style, NodeMapView view) {
		super(parent, style);
		_view = view;
		initialize();
	}

	// ----- instance メソッド ----- //

	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(NodeMapListTableDefine.get(),
				NodeMapListTableDefine.SORT_COLUMN_INDEX,
				NodeMapListTableDefine.SORT_COLUMN_INDEX_SECOND,
				NodeMapListTableDefine.SORT_ORDER);

		// ダブルクリックした場合、スコープであれば遷移する。
		this.tableViewer.addDoubleClickListener(
				new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						// 選択アイテムを取得する
						List<?> list = (List<?>) ((StructuredSelection)event.getSelection()).getFirstElement();
						if (list == null) {
							return;
						}
						String type = (String) list.get(0);
						String facilityId = (String)list.get(1);
						if (!Messages.getString("scope").equals(type)) {
							m_log.debug(facilityId + " is node");
							return ;
						}
						m_log.debug("0=" + list.get(0) + ", 1=" + list.get(1));
						// 同じビュー内で画面遷移
						_view.updateView(facilityId);
					}
				});

		// クリックした場合
		this.tableViewer.addSelectionChangedListener(
				new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						// 選択アイテムを取得する
						List<?> list = (List<?>) ((StructuredSelection)event.getSelection()).getFirstElement();
						if (list == null) {
							return;
						}
						String facilityId = (String)list.get(1);
						String parentId = m_controller.getCurrentScope();
						m_log.debug("selection facilityId=" + facilityId +
								", parentId=" + parentId);
						/*
						 * イベントビューとステータスビューの表示を変更する。
						 */
						RelationViewController.updateScopeTreeView(parentId, facilityId);
						RelationViewController.updateStatusEventView(parentId, facilityId);
					}
				});


		// 件数
		this.totalLabel = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.totalLabel.setLayoutData(gridData);
	}

	public void setTableList(ArrayList<FacilityElement> list) {
		if(list == null) {
			list = new ArrayList<FacilityElement>();
		}

		Collections.sort(list, new FacilityElementComparator());

		ArrayList<Object> listInput = new ArrayList<Object>();
		int scopeN = 0;
		int nodeN = 0;

		// scope
		for (FacilityElement element : list) {
			if (FacilityConstant.TYPE_NODE_STRING.equals(element.getTypeName())) {
				continue;
			}
			ArrayList<Object> a = new ArrayList<Object>();
			a.add(Messages.getString("scope"));
			a.add(element.getFacilityId());
			a.add(HinemosMessage.replace(element.getFacilityName()));
			a.add("");
			a.add("");
			listInput.add(a);
			scopeN ++;
		}

		// node
		for (FacilityElement element : list) {
			if (!(FacilityConstant.TYPE_NODE_STRING.equals(element.getTypeName()))) {
				continue;
			}

			ArrayList<Object> a = new ArrayList<Object>();
			a.add(Messages.getString("node"));
			a.add(element.getFacilityId());
			a.add(element.getFacilityName());
			a.add(getFacilityElementProperty(element, "IpNetworkNumber"));
			a.add(getFacilityElementProperty(element, "IpNetworkNumberV6"));
			listInput.add(a);
			nodeN ++;
		}
		this.tableViewer.setInput(listInput);

		// 件数表示
		Object[] args = { String.valueOf(scopeN), String.valueOf(nodeN)};
		this.totalLabel.setText(com.clustercontrol.nodemap.messages.Messages.getString(
				"scope.node.records", args));

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
	 * 指定されたNodeElementからkeyに該当する属性値を返す
	 * 
	 * @param node
	 * @param key
	 * @return 属性値
	 */
	private static String getFacilityElementProperty(FacilityElement node, String key) {
		String resultStr = "";

		List<FacilityElement.Attributes.Entry> entries = node.getAttributes().getEntry();
		for (FacilityElement.Attributes.Entry entry : entries) {
			if (key.equals(entry.getKey())) {
				resultStr = (String)entry.getValue();
				break;
			}
		}
		return resultStr;
	}


	public void setController(MapViewController m_controller) {
		this.m_controller = m_controller;
	}
}
