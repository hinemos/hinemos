/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.composite;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.maintenance.action.GetMaintenanceListTableDefine;
import com.clustercontrol.maintenance.composite.action.MaintenanceDoubleClickListener;
import com.clustercontrol.maintenance.util.MaintenanceEndpointWrapper;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.maintenance.InvalidRole_Exception;
import com.clustercontrol.ws.maintenance.MaintenanceInfo;
import com.clustercontrol.ws.maintenance.MaintenanceTypeMst;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * メンテナンス情報一覧コンポジットクラスです。
 *
 * @version 2.2.0
 * @since 2.2.0
 */
public class MaintenanceListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( MaintenanceListComposite.class );

	// ----- instance フィールド ----- //

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public MaintenanceListComposite(Composite parent, int style) {
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
		this.tableViewer.createTableColumn(GetMaintenanceListTableDefine.get(),
				GetMaintenanceListTableDefine.SORT_COLUMN_INDEX1,
				GetMaintenanceListTableDefine.SORT_COLUMN_INDEX2,
				GetMaintenanceListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}

		// ダブルクリックリスナの追加
		this.tableViewer.addDoubleClickListener(new MaintenanceDoubleClickListener(this));
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
		// データ取得
		/*
		 * メンテナンス情報とスケジュール情報が別テーブルにあるため、それぞれ取得する。
		 */
		// メンテナンス情報取得
		Map<String, List<MaintenanceInfo>> dispDataMap= new ConcurrentHashMap<String, List<MaintenanceInfo>>();
		Map<String, HashMap<String, String>> dispDataTypeMap= new ConcurrentHashMap<String, HashMap<String, String>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for(String managerName : EndpointManager.getActiveManagerSet()) {
			List<MaintenanceInfo> maintenanceList = null;
			MaintenanceEndpointWrapper wrapper = MaintenanceEndpointWrapper.getWrapper(managerName);
			try {
				maintenanceList = wrapper.getMaintenanceList();
			} catch (InvalidRole_Exception e) {
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				m_log.warn("update() getMaintenanceList, " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}

			if(maintenanceList == null){
				maintenanceList = new ArrayList<MaintenanceInfo>();
			}

			dispDataMap.put(managerName, maintenanceList);

			// メンテナンスタイプの取得
			List<MaintenanceTypeMst> maintenanceTypeList = null;
			try {
				maintenanceTypeList = wrapper.getMaintenanceTypeList();

			} catch (InvalidRole_Exception e) {
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				m_log.warn("update() getMaintenanceTypeList, " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}

			if(maintenanceTypeList == null){
				maintenanceTypeList = new ArrayList<MaintenanceTypeMst>();
			}

			HashMap<String, String> typeMessageMap = new HashMap<String, String>();
			for (MaintenanceTypeMst type : maintenanceTypeList) {
				typeMessageMap.put(type.getTypeId(), Messages.getString(type.getNameId()));
			}

			dispDataTypeMap.put(managerName, typeMessageMap);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		// tableViewer にセットするための詰め替え
		ArrayList<Object> listInput = new ArrayList<Object>();

		for(Map.Entry<String, List<MaintenanceInfo>> map : dispDataMap.entrySet()) {
			for (MaintenanceInfo maintenanceInfo : map.getValue()) {
				ArrayList<Object> a = new ArrayList<Object>();

				a.add(map.getKey());
				a.add(maintenanceInfo.getMaintenanceId());
				a.add(maintenanceInfo.getDescription());
				HashMap<String, String> typeMap = dispDataTypeMap.get(map.getKey());
				a.add(typeMap.get(maintenanceInfo.getTypeId()));
				a.add(maintenanceInfo.getDataRetentionPeriod());
				a.add(maintenanceInfo.getCalendarId());
				a.add(maintenanceInfo.getSchedule());
				a.add(maintenanceInfo.isValidFlg());
				a.add(maintenanceInfo.getOwnerRoleId());
				a.add(maintenanceInfo.getRegUser());
				a.add(new Date(maintenanceInfo.getRegDate()));
				a.add(maintenanceInfo.getUpdateUser());
				a.add(new Date(maintenanceInfo.getUpdateDate()));
				a.add(null);

				listInput.add(a);
			}
		}

		// テーブル更新
		this.tableViewer.setInput(listInput);
	}

	/**
	 * 選択された行のメンテナンスIDを取得する
	 *
	 * @return
	 */
	public ArrayList<String> getSelectionData() {

		ArrayList<String> data = new ArrayList<String>();

		//選択されたアイテムを取得
		StructuredSelection selection =
				(StructuredSelection)tableViewer.getSelection();
		List<?> list = selection.toList();

		if (list != null) {
			for(int index = 0; index < list.size(); index++){

				ArrayList<?> info = (ArrayList<?>)list.get(index);
				if (info != null && info.size() > 0) {
					String maintenanceId = (String)info.get(GetMaintenanceListTableDefine.MAINTENANCE_ID);
					data.add(maintenanceId);
				}
			}
		}

		return data;
	}
}
