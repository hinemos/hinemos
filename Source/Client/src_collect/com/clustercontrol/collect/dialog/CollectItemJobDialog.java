/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.collect.action.GetCollectItemJobTableDefine;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.CheckBoxSelectionAdapter;
import com.clustercontrol.util.Messages;

/**
 * ジョブ収集値表示名[一覧]ダイアログクラス<BR>
 *
 * @version 6.1.0
 */
public class CollectItemJobDialog extends CommonDialog {
	
	/** ジョブ収集項目テーブルビューアー */
	private CommonTableViewer m_tableViewer = null;

	/** 収集値表示名（ジョブ）のマップ（収集値表示名、収集値表示名の情報） */
	private Map<String, String> m_collectorItemJobMap = new HashMap<>();

	/** 収集値表示名（ジョブ）選択項目のリスト(収集値表示名) */
	private List<String> m_collectorItemJobCheckList = new ArrayList<>();

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public CollectItemJobDialog(Shell parent, 
			Map<String, String> collectorItemJobMap, 
			List<String> collectorItemJobCheckList) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		this.m_collectorItemJobMap = collectorItemJobMap;
		this.m_collectorItemJobCheckList = collectorItemJobCheckList;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.collection.display.jobs"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		final Table table = new Table(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData(210, 135);
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// 収集値表示名一覧
		this.m_tableViewer = new CommonTableViewer(table);

		this.m_tableViewer.createTableColumn(GetCollectItemJobTableDefine.get(),
				GetCollectItemJobTableDefine.SORT_COLUMN_INDEX,
				GetCollectItemJobTableDefine.SORT_ORDER);
		
		/** チェックボックスの選択を制御するリスナー */
		SelectionAdapter adapter =
				new CheckBoxSelectionAdapter(this.getParentShell(), this.m_tableViewer, GetCollectItemJobTableDefine.SELECTION);
		table.addSelectionListener(adapter);

		// 情報反映
		reflectInfo();
		// 更新処理
		update();
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 400);
	}

	/**
	 * ランタイムジョブ変数情報をコンポジットに反映します。
	 *
	 */
	public void reflectInfo() {
		ArrayList<ArrayList<Object>> tableData = new ArrayList<ArrayList<Object>>();
		if (m_collectorItemJobMap != null && m_collectorItemJobCheckList != null) {
			for (Map.Entry<String, String> collectItemJob : m_collectorItemJobMap.entrySet()) {
				ArrayList<Object> tableLineData = new ArrayList<Object>();
				tableLineData.add(m_collectorItemJobCheckList.contains(collectItemJob.getKey()));
				tableLineData.add(collectItemJob.getKey());
				tableData.add(tableLineData);
			}
		}
		this.m_tableViewer.setInput(tableData);
	}
	/**
	 * 更新処理
	 *
	 */
	public void update(){
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;
		m_collectorItemJobCheckList.clear();
		if (m_tableViewer.getTable() != null) {
			ArrayList<?> tableData = null;
			for (TableItem tableItem : m_tableViewer.getTable().getItems()) {
				tableData = (ArrayList<?>)tableItem.getData();
				if ((Boolean)tableData.get(GetCollectItemJobTableDefine.SELECTION)) {
					m_collectorItemJobCheckList.add((String)tableData.get(GetCollectItemJobTableDefine.COLLECT_ITEM_NAME));
				}
			}
		}
		return result;
	}
}
