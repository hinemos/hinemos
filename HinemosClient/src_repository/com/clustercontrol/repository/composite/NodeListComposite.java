/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.openapitools.client.model.NodeInfoResponseP2;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.Property;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.repository.action.GetNodeList;
import com.clustercontrol.repository.action.GetNodeListTableDefine;
import com.clustercontrol.repository.bean.IpAddr;
import com.clustercontrol.repository.composite.action.NodeDoubleClickListener;
import com.clustercontrol.repository.view.NodeAttributeView;
import com.clustercontrol.repository.view.NodeScopeView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ノード一覧コンポジットクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class NodeListComposite extends Composite {

	// ----- instance フィールド ----- //

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	/** 表示内容ラベル */
	private Label statuslabel = null;

	/** 合計ラベル */
	private Label totalLabel = null;

	/** 検索条件 */
	private Property condition = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public NodeListComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize();
	}

	// ----- instance メソッド ----- //

	/**
	 * コンポジットを生成・構築します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		this.statuslabel = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "statuslabel", statuslabel);
		this.statuslabel.setText("");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.statuslabel.setLayoutData(gridData);

		Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId( this, null, table );
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(GetNodeListTableDefine.get(),
				GetNodeListTableDefine.SORT_COLUMN_INDEX1,
				GetNodeListTableDefine.SORT_COLUMN_INDEX2,
				GetNodeListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}
		this.tableViewer
		.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// 各ビューの更新

				IWorkbench workbench = ClusterControlPlugin.getDefault()
						.getWorkbench();
				IWorkbenchPage page = workbench
						.getActiveWorkbenchWindow().getActivePage();

				NodeScopeView scopeView = (NodeScopeView) page
						.findView(NodeScopeView.ID);
				if (scopeView != null) {
					scopeView.update();
				}
				NodeAttributeView attributeView = (NodeAttributeView) page
						.findView(NodeAttributeView.ID);
				if (attributeView != null) {
					attributeView.update();
				}
			}
		});

		// ダブルクリックリスナの追加
		this.tableViewer.addDoubleClickListener(new NodeDoubleClickListener(this));

		this.totalLabel = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "totallabel", totalLabel);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.totalLabel.setLayoutData(gridData);

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
	 * コンポジットを更新します。
	 * <p>
	 *
	 * 検索条件が事前に設定されている場合、その条件にヒットするノードの一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全ノードを表示します。
	 */
	@Override
	public void update() {
		// データ取得
		Map<String, List<NodeInfoResponseP2>> dispDataMap = null;
		ArrayList<Object> listInput = new ArrayList<Object>();

		if (this.condition == null) {
			this.statuslabel.setText("");
			dispDataMap = new GetNodeList().getAll();
		} else {
			this.statuslabel.setText(Messages.getString("filtered.list"));

			String conditionManager = null;
			conditionManager = JobPropertyUtil.getManagerName(this.condition);

			if(conditionManager == null || conditionManager.equals("")) {
				dispDataMap = new GetNodeList().get(this.condition);
			} else {
				List<NodeInfoResponseP2> list = new GetNodeList().get(conditionManager, this.condition);
				dispDataMap = new ConcurrentHashMap<String, List<NodeInfoResponseP2>>();
				dispDataMap.put(conditionManager, list);
			}
		}

		int cnt = 0;
		for(Map.Entry<String, List<NodeInfoResponseP2>> entrySet : dispDataMap.entrySet()) {
			List<NodeInfoResponseP2> list = entrySet.getValue();

			if(list == null){
				list = new ArrayList<NodeInfoResponseP2>();
			}

			for (NodeInfoResponseP2 node : list) {
				ArrayList<Object> a = new ArrayList<Object>();
				a.add(entrySet.getKey());
				a.add(node.getFacilityId());
				a.add(node.getFacilityName());
				a.add(node.getPlatformFamily());
				if (node.getIpAddressVersion() == NodeInfoResponseP2.IpAddressVersionEnum.IPV6) {
					a.add(new IpAddr(node.getIpAddressV6(), 6));
				} else {
					a.add(new IpAddr(node.getIpAddressV4(), 4));
				}
				a.add(node.getDescription());
				a.add(node.getOwnerRoleId());
				a.add(null);
				listInput.add(a);
				cnt++;
			}
		}
		// テーブル更新
		this.tableViewer.setInput(listInput);

		// 合計欄更新
		String[] args = { Integer.toString(cnt) };
		String message = null;
		if (this.condition == null) {
			message = Messages.getString("records", args);
		} else {
			message = Messages.getString("filtered.records", args);
		}
		this.totalLabel.setText(message);
	}

	/**
	 * 検索条件にヒットしたノードの一覧を表示します。
	 * <p>
	 *
	 * conditionがnullの場合、全ノードを表示します。
	 *
	 * @param condition
	 *            検索条件
	 */
	public void update(Property condition) {
		this.condition = condition;

		this.update();
	}
}
