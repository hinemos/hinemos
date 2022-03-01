/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.openapitools.client.model.EventCustomCommandInfoResponse;

import com.clustercontrol.monitor.action.GetEventCustomCommandListTableDefine;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * イベントカスタムコマンドのコンポジットクラス<BR>
 *
 * イベントカスタムコマンド選択テーブルのコンポジット
 *
 */
public class EventCustomCommandComposite extends Composite {

	// ----- instance フィールド ----- //

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;


	// ----- コンストラクタ ----- //

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public EventCustomCommandComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	// ----- instance メソッド ----- //

	/**
	 * コンポジットの初期化
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(5, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		Table table = new Table(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		table.setLayoutData(gridData);
		// 線を表示する
		table.setLinesVisible(true);
		// ヘッダを可視にする
		table.setHeaderVisible(true);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(
				GetEventCustomCommandListTableDefine.getEventCustomCommandListTableDefine(),
				GetEventCustomCommandListTableDefine.SORT_COLUMN_INDEX1,
				GetEventCustomCommandListTableDefine.SORT_COLUMN_INDEX2,
				GetEventCustomCommandListTableDefine.SORT_ORDER);
		
		/**テーブルのレコードを選択するリスナー*/
		table.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				//選択されたラジオボタンをONにして、それ以外をOFFにする
				
				if (e.item == null || e.item.getData() == null) {
					return;
				}
				
				Integer commandNo = (Integer) ((List<?>) e.item.getData()).get(GetEventCustomCommandListTableDefine.COMMAND_NO);
				
				//選択されたTableColumnを取得します。
				TableItem[] tableItems = table.getItems();
				for (int i = 0; i < tableItems.length; i++) {
					@SuppressWarnings("unchecked")
					ArrayList<Object> row = (ArrayList<Object>)tableItems[i].getData();
					
					if((commandNo.equals(row.get(GetEventCustomCommandListTableDefine.COMMAND_NO)))){
						//選択されたコマンドNOはON
						row.set(GetEventCustomCommandListTableDefine.SELECT, true);
					}else{
						//それ以外はOFF
						row.set(GetEventCustomCommandListTableDefine.SELECT, false);

					}
				}
				//ラジオボタンを再描写
				tableViewer.refresh();
			}
		});
		
	}
	
	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return this.tableViewer.getTable();
	}
	
	public void updateDisp(Map<Integer, EventCustomCommandInfoResponse> customCommandInfoMap) {
		super.update();

		ArrayList<ArrayList<Object>> input = new ArrayList<ArrayList<Object>>();
		for (Entry<Integer, EventCustomCommandInfoResponse> info : customCommandInfoMap.entrySet()) {
			if (!info.getValue().getEnable()) {
				continue;
			}
			
			ArrayList<Object> list = new ArrayList<Object>();
			
			list.add(GetEventCustomCommandListTableDefine.COMMAND_NO, info.getKey());
			list.add(GetEventCustomCommandListTableDefine.SELECT, Boolean.FALSE);
			list.add(GetEventCustomCommandListTableDefine.COMMAND_NAME, info.getValue().getDisplayName());
			list.add(GetEventCustomCommandListTableDefine.DESCRIPTION, info.getValue().getDescription());
			
			input.add(list);
		}
		tableViewer.setInput(input);
	}
}
