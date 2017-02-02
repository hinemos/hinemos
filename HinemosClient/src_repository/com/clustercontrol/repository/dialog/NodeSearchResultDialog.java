/*

Copyright (C) 2007 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.repository.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.repository.bean.IpAddr;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.repository.NodeInfo;
import com.clustercontrol.ws.repository.NodeInfoDeviceSearch;

/**
 * リポジトリ[ノードサーチ]のノードサーチエラー実行結果ダイアログクラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class NodeSearchResultDialog extends Dialog {

	// ログ
	private static Log m_log = LogFactory.getLog(NodeSearchResultDialog.class);

	private List<NodeInfoDeviceSearch> nodeinfoList;
	private Shell shell;
	private boolean success;
	
	/**
	 * コンストラクタ
	 *
	 * @param parent 親シェル
	 */
	public NodeSearchResultDialog(Shell parent, List<NodeInfoDeviceSearch> list, boolean success) {
		super(parent);
		this.nodeinfoList = list;
		this.success = success;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected Control createContents(Composite parent) {
		this.shell = this.getShell();
		GridData gridData;

		// タイトル
		shell.setText(Messages.getString("message"));

		// レイアウト
		GridLayout layout = new GridLayout(1, false);
		shell.setLayout(layout);

		//ベースコンポジット（タイトル部 + データ部）
		Composite baseCmp = new Composite(shell, SWT.NONE);
		WidgetTestUtil.setTestId(this, "base", baseCmp);
		baseCmp.setLayout(new FillLayout());
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		baseCmp.setLayoutData(gridData);

		//登録ノード情報のグループ
		Group group = new Group(baseCmp, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, null, group);
		group.setLayout(new GridLayout(1, true));
		int count = 0;
		for(NodeInfoDeviceSearch info : this.nodeinfoList) {
			if (info.getErrorMessage() != null ^ success) {
				count++;
			}
		}
		Object[] arg = {count};
		if (success) {
			group.setText(Messages.getString("message.repository.nodesearch.8", arg));
		} else {
			group.setText(Messages.getString("message.repository.nodesearch.9", arg));
		}

		//データ部
		Composite cntCmp = new Composite(group, SWT.NONE);
		WidgetTestUtil.setTestId(this, "cnt", cntCmp);
		cntCmp.setLayout(new FillLayout());
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.horizontalSpan = 1;
		cntCmp.setLayoutData(gridData);

		Table table = new Table(cntCmp, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		CommonTableViewer tableViewer = new CommonTableViewer(table);
		tableViewer.createTableColumn(GetNodeSearchResultTableDefine.get(),
				GetNodeSearchResultTableDefine.SORT_COLUMN_INDEX1,
				GetNodeSearchResultTableDefine.SORT_COLUMN_INDEX2,
				GetNodeSearchResultTableDefine.SORT_ORDER);

		//取得した情報
		ArrayList<ArrayList<Object>> listInfo = new ArrayList<>();
		for (NodeInfoDeviceSearch info : nodeinfoList) {
			if (info.getErrorMessage() == null ^ success) {
				continue;
			}
			ArrayList<Object> list = new ArrayList<>();
			NodeInfo nodeInfo = info.getNodeInfo();
			
			list.add(nodeInfo.getFacilityId());
			IpAddr ipAddress = null;
			if (nodeInfo.getIpAddressVersion() == 6) {
				ipAddress = new IpAddr(nodeInfo.getIpAddressV6(), 6);
			} else {
				ipAddress = new IpAddr(nodeInfo.getIpAddressV4(), 4);
			}
			list.add(ipAddress);
			list.add(HinemosMessage.replace(info.getErrorMessage()));
			list.add("");
			listInfo.add(list);
		}
		tableViewer.setInput(listInfo);

		//ボタン部
		Composite btnCmp = new Composite(shell, SWT.NONE);
		WidgetTestUtil.setTestId(this, "btn", btnCmp);
		layout = new GridLayout(1, false);
		btnCmp.setLayout(layout);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		btnCmp.setLayoutData(gridData);

		Button b = new Button(btnCmp, SWT.PUSH);
		WidgetTestUtil.setTestId(this, null, b);
		b.setText(Messages.getString("ok"));

		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;

		b.setLayoutData(gridData);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				shell.close();
			}
		});

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになる
		shell.pack();
		shell.setSize(new Point(600, 300));
		m_log.debug("shell getSize():" + shell.getSize());

		// 画面中央に配置
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		return cntCmp;
	}
}
