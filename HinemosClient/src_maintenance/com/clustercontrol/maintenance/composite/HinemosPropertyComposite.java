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

package com.clustercontrol.maintenance.composite;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.maintenance.HinemosPropertyTypeMessage;
import com.clustercontrol.maintenance.action.GetHinemosPropertyTableDefine;
import com.clustercontrol.maintenance.composite.action.HinemosPropertyDoubleClickListener;
import com.clustercontrol.maintenance.util.HinemosPropertyEndpointWrapper;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.maintenance.HinemosPropertyInfo;
import com.clustercontrol.ws.maintenance.InvalidRole_Exception;

/**
 * 共通設定コンポジットクラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class HinemosPropertyComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( HinemosPropertyComposite.class );

	// ----- instance フィールド ----- //

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 */
	public HinemosPropertyComposite(Composite parent, int style) {
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

		Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(GetHinemosPropertyTableDefine.get(),
				GetHinemosPropertyTableDefine.SORT_COLUMN_INDEX1,
				GetHinemosPropertyTableDefine.SORT_COLUMN_INDEX2,
				GetHinemosPropertyTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}

		// ダブルクリックリスナの追加
		this.tableViewer.addDoubleClickListener(new HinemosPropertyDoubleClickListener(this));
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
	 */
	@Override
	public void update() {
		/*
		 * 共通設定情報とスケジュール情報が別テーブルにあるため、それぞれ取得する。
		 */
		// 共通設定情報取得
		List<HinemosPropertyInfo> HinemosPropertyList = null;
		Map<String, List<HinemosPropertyInfo>> dispDataMap= new ConcurrentHashMap<String, List<HinemosPropertyInfo>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		for(String managerName : EndpointManager.getActiveManagerSet()) {
			HinemosPropertyEndpointWrapper wrapper = HinemosPropertyEndpointWrapper.getWrapper(managerName);
			try {
				HinemosPropertyList = wrapper.getHinemosPropertyList();
			} catch (InvalidRole_Exception e) {
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				m_log.warn("update() getHinemosPropertyList, " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}

			if(HinemosPropertyList == null){
				HinemosPropertyList = new ArrayList<HinemosPropertyInfo>();
			}

			dispDataMap.put(managerName, HinemosPropertyList);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		// tableViewer にセットするための詰め替え
		ArrayList<Object> listInput = new ArrayList<Object>();
		Calendar createCal = Calendar.getInstance(TimezoneUtil.getTimeZone());
		Calendar modifyCal = Calendar.getInstance(TimezoneUtil.getTimeZone());

		for (Map.Entry<String, List<HinemosPropertyInfo>> map : dispDataMap.entrySet()) {
			for (HinemosPropertyInfo info : map.getValue()) {
				ArrayList<Object> list = new ArrayList<Object>();

				list.add(map.getKey());
				list.add(info.getKey());
				if (info.getValueType() == 1) {
					list.add(info.getValueString());
					list.add(HinemosPropertyTypeMessage.STRING_STRING);
				} else if (info.getValueType() == 2) {
					list.add(info.getValueNumeric());
					list.add(HinemosPropertyTypeMessage.STRING_NUMERIC);
				} else {
					list.add(info.isValueBoolean().toString());
					list.add(HinemosPropertyTypeMessage	.STRING_TRUTH);
				}

				list.add(info.getDescription());
				list.add(info.getOwnerRoleId());
				list.add(info.getCreateUserId());
				if (info.getCreateDatetime() != null) {
					createCal.setTimeInMillis(info.getCreateDatetime());
					list.add(createCal.getTime());
				} else {
					list.add(null);
				}
				list.add(info.getModifyUserId());
				if (info.getModifyDatetime() != null) {
					modifyCal.setTimeInMillis(info.getModifyDatetime());
					list.add(modifyCal.getTime());
				} else {
					list.add(null);
				}
				list.add(null);

				listInput.add(list);
			}
		}

		// テーブル更新
		this.tableViewer.setInput(listInput);
	}
}
