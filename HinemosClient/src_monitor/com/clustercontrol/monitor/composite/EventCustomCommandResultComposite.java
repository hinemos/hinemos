/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.composite;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.EventCustomCommandResultResponse;
import org.openapitools.client.model.EventCustomCommandResultRootResponse;
import org.openapitools.client.model.GetEventCustomCommandResultResponse;

import com.clustercontrol.monitor.action.GetEventCustomCommandResultTableDefine;
import com.clustercontrol.monitor.bean.EventCustomCommandStatusConstant;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.util.DateTimeStringConverter;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * イベントカスタムコマンド実行結果のコンポジットクラス<BR>
 *
 * イベントカスタムコマンド実行結果テーブルのコンポジット
 *
 */
public class EventCustomCommandResultComposite extends Composite {

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
	public EventCustomCommandResultComposite(Composite parent, int style) {
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
				GetEventCustomCommandResultTableDefine.getCustomCommandResultTableDefine(),
				GetEventCustomCommandResultTableDefine.SORT_COLUMN_INDEX1,
				GetEventCustomCommandResultTableDefine.SORT_COLUMN_INDEX2,
				GetEventCustomCommandResultTableDefine.SORT_ORDER);
	}
	
	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return this.tableViewer.getTable();
	}
	
	public void updateDisp(EventCustomCommandResultRootResponse resultRoot, 
			String managerName, MultiManagerEventDisplaySettingInfo eventDspSettingInfo) {
		super.update();
		
		ArrayList<ArrayList<Object>> input = new ArrayList<ArrayList<Object>>();
		
		final String lineSep = "\n";
		
		for(int i = 0; i < resultRoot.getEventResultList().size(); i++) {
			EventCustomCommandResultResponse result = resultRoot.getEventResultList().get(i);

			ArrayList<Object> list = new ArrayList<>();
			StringBuilder orderStr = new StringBuilder();

			orderStr.append(String.format("%s:%s        ", Messages.getString("event.customcommand.result.processing.order"), String.valueOf(i + 1)));
			orderStr.append(lineSep);
			orderStr.append(String.format("%s:%s", Messages.getString("receive.time"), DateTimeStringConverter.formatLongDate(result.getEvent().getOutputDate())));
			orderStr.append(lineSep);
			orderStr.append(String.format("%s:%s", Messages.getString("plugin.id"), result.getEvent().getPluginId()));
			orderStr.append(lineSep);
			orderStr.append(String.format("%s:%s", Messages.getString("monitor.id"), result.getEvent().getMonitorId()));
			orderStr.append(lineSep);
			orderStr.append(String.format("%s:%s", Messages.getString("monitor.detail.id"), result.getEvent().getMonitorDetailId()));
			orderStr.append(lineSep);
			orderStr.append(String.format("%s:%s", Messages.getString("facility.id"), result.getEvent().getFacilityId()));
			orderStr.append(lineSep);
			if (eventDspSettingInfo.isEventNoDisplay(managerName)) {
				orderStr.append(String.format("%s:%d", Messages.getString("monitor.eventno"), result.getEvent().getPosition()));
				orderStr.append(lineSep);
			}
			
			String resultStr = "";
			if (result.getStatus() != null) {
				resultStr = Messages.getString(EventCustomCommandStatusConstant.statusToMessageCode(result.getStatus()));
			}
			
			String rcStr = "";
			if (result.getReturnCode() != null) {
				rcStr = String.valueOf(result.getReturnCode());
			}
			
			list.add(GetEventCustomCommandResultTableDefine.ORDER, i);
			list.add(GetEventCustomCommandResultTableDefine.EVENTINFO, orderStr.toString());
			list.add(GetEventCustomCommandResultTableDefine.RESULT, resultStr);
			list.add(GetEventCustomCommandResultTableDefine.RETURN_CODE, rcStr);
			list.add(GetEventCustomCommandResultTableDefine.MESSAGE, HinemosMessage.replace(result.getMessage()));
			
			input.add(list);
		}
		tableViewer.setInput(input);
	}
}
