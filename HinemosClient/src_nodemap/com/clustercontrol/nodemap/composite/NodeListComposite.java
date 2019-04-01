/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.composite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.nodemap.etc.action.NodeListTableDefine;
import com.clustercontrol.nodemap.view.NodeListView;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.repository.NodeInfo;

/**
 * ノード一覧コンポジットクラス<BR>
 *
 * @version 6.2.0
 */
public class NodeListComposite extends Composite{

	// ログ
	private static Log m_log = LogFactory.getLog( NodeListComposite.class );

	/** ビュー */
	private NodeListView _view;

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	/** 全件ラベル */
	private Label totalLabel = null;

	/** 対象日時ラベル */
	private Label m_targetDatetimeLabel = null;

	/** 対象日時 */
	private Long m_targetDatetime = 0L;

	/** マネージャ名 */
	private String m_managerName;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 * 
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public NodeListComposite(Composite parent, int style, NodeListView view) {
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

		// パス文字列表示コンポジットを作成
		Composite pathComposite = new Composite(this, SWT.NONE);

		// パス文字列表示コンポジットがビューの大きさにあわせて可変になるように設定
		GridData pathGridData = new GridData();
		pathGridData.horizontalAlignment = GridData.FILL;
		pathGridData.verticalAlignment = GridData.FILL;
		pathComposite.setLayoutData(pathGridData);

		// コンポジット内のレイアウトを設定
		pathComposite.setLayout(new FormLayout());

		// 対象日時ラベル
		m_targetDatetimeLabel = new Label(pathComposite, SWT.NONE);
		FormData labelData = new FormData();  // ラベルの位置設定
		labelData.top  = new FormAttachment(0, 0); // コンポジット上側に詰める
		labelData.left = new FormAttachment(0, 0);  // コンポジットの左側に詰める
		labelData.width = 300;
		m_targetDatetimeLabel.setLayoutData(labelData);

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
		this.tableViewer.createTableColumn(NodeListTableDefine.get(),
			NodeListTableDefine.SORT_COLUMN_INDEX,
			NodeListTableDefine.SORT_COLUMN_INDEX_SECOND,
			NodeListTableDefine.SORT_ORDER);

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
					String managerName = (String)list.get(0);
					String facilityId = (String)list.get(1);
					m_log.debug("selection managerName=" + managerName
							+ ", facilityId=" + facilityId);
					/*
					 * 構成情報の表示を変更する。
					 */
					_view.m_attributeComposite.update(managerName, facilityId, m_targetDatetime, _view.getNodeFilterInfo());
				}
			}
		);

		// 件数
		this.totalLabel = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.totalLabel.setLayoutData(gridData);
	}

	/**
	 * テーブルに値を表示
	 * 
	 * @param _nodeListMap ノード一覧
	 * @param targetDatetime 対象日時
	 */
	public void setTableList(ConcurrentHashMap<String, List<NodeInfo>> _nodeListMap, Long targetDatetime) {
		if(_nodeListMap == null) {
			_nodeListMap = new ConcurrentHashMap<>();
		}

		if (targetDatetime != null) {
			m_targetDatetime = targetDatetime;
		}
		ArrayList<Object> listInput = new ArrayList<Object>();
		int nodeN = 0;

		// 一覧表示
		for (Map.Entry<String, List<NodeInfo>> entry : _nodeListMap.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			for (NodeInfo nodeInfo : entry.getValue()) {
				ArrayList<Object> a = new ArrayList<Object>();
				a.add(entry.getKey());
				a.add(nodeInfo.getFacilityId());
				a.add(nodeInfo.getFacilityName());
				a.add(nodeInfo.getPlatformFamily());
				a.add(nodeInfo.getOwnerRoleId());
				if (nodeInfo.getCreateDatetime() == null) {
					a.add(null);
				} else {
					a.add(new Date(nodeInfo.getCreateDatetime()));
				}
				if (nodeInfo.getModifyDatetime() == null) {
					a.add(null);
				} else {
					a.add(new Date(nodeInfo.getModifyDatetime()));
				}
				listInput.add(a);
				nodeN++;
			}
		}
		this.tableViewer.setInput(listInput);

		// 対象日時表示
		String targetDatetimeStr = "";
		if (m_targetDatetime != null && m_targetDatetime != 0L) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
			sdf.setTimeZone(HinemosTime.getTimeZone());
			targetDatetimeStr = sdf.format(new Date(m_targetDatetime));
		} else {
			targetDatetimeStr = Messages.getString("latest");
		}
		this.m_targetDatetimeLabel.setText(Messages.getString("target.datetime") + " : " + targetDatetimeStr);

		// 件数表示
		Object[] args = { String.valueOf(nodeN)};
		this.totalLabel.setText(com.clustercontrol.nodemap.messages.Messages.getString(	"node.records", args));

		// 構成情報表示
		_view.m_attributeComposite.update(null, null, null, null);
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
	 * マネージャ名取得
	 * 
	 * @return マネージャ名
	 */
	public String getManagerName() {
		return m_managerName;
	}

	/**
	 * マネージャ名設定
	 * 
	 * @param managerName マネージャ名
	 */
	public void setManagerName(String managerName) {
		m_managerName = managerName;
	}
}
