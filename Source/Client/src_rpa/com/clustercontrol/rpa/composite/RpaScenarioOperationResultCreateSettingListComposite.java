/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.RpaScenarioOperationResultCreateSettingResponse;
import org.openapitools.client.model.RpaScenarioOperationResultCreateSettingResponse.IntervalEnum;

import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.rpa.action.GetRpaScenarioOperationResultCreateSettingListTableDefine;
import com.clustercontrol.rpa.composite.action.RpaScenarioOperationResultCreateSettingDoubleClickListener;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * RPAシナリオ実績作成設定一覧コンポジットクラス
 */
public class RpaScenarioOperationResultCreateSettingListComposite extends Composite {

	// ログ
	private static Log log = LogFactory.getLog( RpaScenarioOperationResultCreateSettingListComposite.class );
	/** テーブルビューア */
	private CommonTableViewer viewer = null;
	/** テーブル */
	private Table rpaScenarioCreateSettingListTable = null;
	/** ラベル */
	private Label labelCount = null;

	/**
	 * このコンポジットが利用するテーブルビューアを取得します。
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return viewer;
	}
	/**
	 * このコンポジットが利用するテーブルを取得します。
	 * @return テーブル
	 */
	public Table getTable() {
		return viewer.getTable();
	}
	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @param style
	 */
	public RpaScenarioOperationResultCreateSettingListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * 初期化処理
	 */
	private void initialize() {
		
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		//RPAシナリオ実績作成設定一覧テーブル作成
		rpaScenarioCreateSettingListTable = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		rpaScenarioCreateSettingListTable.setHeaderVisible(true);
		rpaScenarioCreateSettingListTable.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		rpaScenarioCreateSettingListTable.setLayoutData(gridData);
		
		labelCount = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		labelCount.setLayoutData(gridData);

		viewer = new CommonTableViewer(rpaScenarioCreateSettingListTable);
		viewer.createTableColumn(GetRpaScenarioOperationResultCreateSettingListTableDefine.get(),
				GetRpaScenarioOperationResultCreateSettingListTableDefine.SORT_COLUMN_INDEX1,
				GetRpaScenarioOperationResultCreateSettingListTableDefine.SORT_COLUMN_INDEX2,
				GetRpaScenarioOperationResultCreateSettingListTableDefine.SORT_ORDER);
		
		for (int i = 0; i < rpaScenarioCreateSettingListTable.getColumnCount(); i++){
			rpaScenarioCreateSettingListTable.getColumn(i).setMoveable(true);
		}
		// ダブルクリックリスナの追加
		viewer.addDoubleClickListener(new RpaScenarioOperationResultCreateSettingDoubleClickListener(this));
		
	}

	/**
	 * 更新処理
	 */
	@Override
	public void update() {
		List<RpaScenarioOperationResultCreateSettingResponse> list = null;

		//RPAシナリオ実績作成設定一覧情報取得
		Map<String, List<RpaScenarioOperationResultCreateSettingResponse>> dispDataMap= new ConcurrentHashMap<String, List<RpaScenarioOperationResultCreateSettingResponse>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<String, String>();
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			try {
				list = wrapper.getRpaScenarioOperationResultCreateSettingList();
			} catch (Exception e) {
				// エンタープライズ機能が無効の場合は無視する
				if(UrlNotFound.class.equals(e.getCause().getClass())) {
					continue;
				}
				// 上記以外の例外
				log.warn("update(), " + e.getMessage(), e);
				errorMsgs.put( managerName, e.getMessage() );
			}
			if(list == null){
				list = new ArrayList<RpaScenarioOperationResultCreateSettingResponse>();
			}

			dispDataMap.put(managerName, list);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		for(Map.Entry<String, List<RpaScenarioOperationResultCreateSettingResponse>> map: dispDataMap.entrySet()) {
			for (RpaScenarioOperationResultCreateSettingResponse info : map.getValue()) {
				ArrayList<Object> obj = new ArrayList<Object>();
				obj.add(map.getKey());
				obj.add(info.getScenarioOperationResultCreateSettingId());
				obj.add(HinemosMessage.replace(info.getDescription()));
				obj.add(info.getScope());
				obj.add(RunInterval.enumToString(info.getInterval(), IntervalEnum.class));
				String validFlgStr;
				if (info.getValidFlg()) {
					validFlgStr = Messages.getString("valid");
				} else {
					validFlgStr = Messages.getString("invalid");
				}
				obj.add(validFlgStr);
				obj.add(info.getCalendarId());
				obj.add(info.getOwnerRoleId());
				obj.add(info.getRegUser());
				obj.add(info.getRegDate());
				obj.add(info.getUpdateUser());
				obj.add(info.getUpdateDate());
				obj.add(null);
				listInput.add(obj);
			}
		}
		viewer.setInput(listInput);

		Object[] args = { Integer.valueOf(listInput.size()) };
		labelCount.setText(Messages.getString("records", args));
	}
}
