/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.composite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.action.GetStatusListTableDefine;
import com.clustercontrol.monitor.dialog.StatusInfoDialog;
import com.clustercontrol.monitor.preference.MonitorPreferencePage;
import com.clustercontrol.monitor.util.ConvertListUtil;
import com.clustercontrol.monitor.util.StatusFilterPropertyUtil;
import com.clustercontrol.monitor.util.StatusSearchRunUtil;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.repository.bean.FacilityTargetConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.monitor.StatusFilterInfo;

/**
 * ステータス情報一覧のコンポジットクラス<BR>
 *
 *ステータス情報一覧部分のテーブルのコンポジット
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class StatusListComposite extends Composite {
	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	/** 危険ラベル */
	private Label criticalLabel = null;

	/** 警告ラベル */
	private Label warningLabel = null;

	/** 通知ラベル */
	private Label infoLabel = null;

	/** 不明ラベル */
	private Label unknownLabel = null;

	/** 合計ラベル */
	private Label totalLabel = null;

	private Shell m_shell = null;

	/**
	 * インスタンスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public StatusListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
		m_shell = this.getShell();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(5, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI);
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

		// ステータス作成
		// 危険
		this.criticalLabel = new Label(this, SWT.CENTER);
		WidgetTestUtil.setTestId(this, "criticallabel", criticalLabel);
		this.criticalLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		this.criticalLabel.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_RED));

		// 警告
		this.warningLabel = new Label(this, SWT.CENTER);
		WidgetTestUtil.setTestId(this, "warninglabel", warningLabel);
		this.warningLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		this.warningLabel.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_YELLOW));

		// 通知
		this.infoLabel = new Label(this, SWT.CENTER);
		WidgetTestUtil.setTestId(this, "infolabel", infoLabel);
		this.infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		this.infoLabel.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_GREEN));

		// 不明
		this.unknownLabel = new Label(this, SWT.CENTER);
		WidgetTestUtil.setTestId(this, "unknownlabel", unknownLabel);
		this.unknownLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		this.unknownLabel.setBackground(new Color(null, 128, 192, 255));

		// 合計
		this.totalLabel = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "totallabel", totalLabel);
		this.totalLabel.setLayoutData( new GridData(SWT.FILL, SWT.NONE, true, false) );

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(GetStatusListTableDefine.getStatusListTableDefine(),
				GetStatusListTableDefine.SORT_COLUMN_INDEX1,
				GetStatusListTableDefine.SORT_COLUMN_INDEX2,
				GetStatusListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}

		this.tableViewer.addDoubleClickListener(new IDoubleClickListener(){
			@Override
			public void doubleClick(DoubleClickEvent event) {
				// 選択アイテムを取得する
				List<?> list = (List<?>) ((StructuredSelection)event.getSelection()).getFirstElement();
				StatusInfoDialog dialog = new StatusInfoDialog(m_shell, list);
				dialog.open();
			}
		});
	}

	/**
	 * このコンポジットが利用する共通テーブルビューアーを返します。
	 *
	 * @return 共通テーブルビューアー
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

	/** 表示用リスト */
	private Map<String, ArrayList<ArrayList<Object>>> dispDataMap;
	public void resetDisp() {
		dispDataMap = new ConcurrentHashMap<>();
	}
	
	/**
	 * ビューを更新します。<BR>
	 * 引数で指定された条件に一致するステータス情報一覧を取得し、共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>引数で指定された条件に一致するステータス情報一覧を取得します。</li>
	 * <li>共通テーブルビューアーにステータス情報一覧をセットします。</li>
	 * </ol>
	 *
	 * @param facilityId 表示対象の親ファシリティID
	 * @param condition 検索条件
	 * @param managerList マネージャ名リスト
	 * @param refreshFlg リフレッシュフラグ
	 * @see #updateStatus(ArrayList)
	 */
	public void setDisp(String facilityId, Property condition, List<String> managerList) {
		if(facilityId == null) {
			facilityId = ReservedFacilityIdConstant.ROOT_SCOPE;
		}
		Map<String, ArrayList<ArrayList<Object>>> map = null;
		if(condition == null) {
			map = getStatusList(facilityId, managerList);
		} else {
			map = getStatusListByCondition(facilityId, condition, managerList);
		}
		for (Map.Entry<String, ArrayList<ArrayList<Object>>> entry : map.entrySet()) {
			ArrayList<ArrayList<Object>> viewListInfo = dispDataMap.get(entry.getKey());
			if (viewListInfo == null) {
				dispDataMap.put(entry.getKey(), entry.getValue());
				continue;
			}
			ArrayList<ArrayList<Object>> value = entry.getValue();
			for (ArrayList<Object> info : value) {
				boolean flag = true;
				for (ArrayList<Object> info2 : viewListInfo) {
					if (info2.get(GetStatusListTableDefine.FACILITY_ID).equals(info.get(GetStatusListTableDefine.FACILITY_ID)) &&
							info2.get(GetStatusListTableDefine.MONITOR_ID).equals(info.get(GetStatusListTableDefine.MONITOR_ID)) &&
							info2.get(GetStatusListTableDefine.MONITOR_DETAIL_ID).equals(info.get(GetStatusListTableDefine.MONITOR_DETAIL_ID)) &&
							info2.get(GetStatusListTableDefine.PLUGIN_ID).equals(info.get(GetStatusListTableDefine.PLUGIN_ID))) {
						flag = false;
						break;
					}
				}
				
				// monitor_id, monitor_detail_id, plugin_id, output_date, facility_id
				if (flag){
					viewListInfo.add(info);
				}
			}
		}

	}
	
	public void updateDisp(boolean refreshFlg) {
		super.update();

		ArrayList<ArrayList<Object>> statusList = ConvertListUtil.statusInfoData2List(dispDataMap);
		
		boolean statusChengedFlg = false;
		if (refreshFlg) {
			// 最新のステータスを取得する
			Map<StatusKey, Integer> newStatusMap = new HashMap<>();
			for (ArrayList<Object> newStatus : statusList) {
				StatusKey key = new StatusKey(
						newStatus.get(GetStatusListTableDefine.FACILITY_ID).toString(),
						newStatus.get(GetStatusListTableDefine.MONITOR_ID).toString(),
						newStatus.get(GetStatusListTableDefine.MONITOR_DETAIL_ID).toString(),
						(newStatus.get(GetStatusListTableDefine.PLUGIN_ID).toString()));
				newStatusMap.put(key, (Integer)newStatus.get(GetStatusListTableDefine.PRIORITY));
			}
			
			// 画面に表示されているステータス履歴を取得する
			Object obj = this.getTableViewer().getInput();
			Map<StatusKey, Integer> oldStatusMap = new HashMap<>();
			if (obj != null) {
				@SuppressWarnings("unchecked")
				List<ArrayList<?>> oldStatusList = (ArrayList<ArrayList<?>>)obj;
				for (ArrayList<?> oldStatus : oldStatusList) {
					StatusKey oldKey = new StatusKey(
							oldStatus.get(GetStatusListTableDefine.FACILITY_ID).toString(),
							oldStatus.get(GetStatusListTableDefine.MONITOR_ID).toString(),
							oldStatus.get(GetStatusListTableDefine.MONITOR_DETAIL_ID).toString(),
							oldStatus.get(GetStatusListTableDefine.PLUGIN_ID).toString());
					oldStatusMap.put(oldKey, (Integer)oldStatus.get(GetStatusListTableDefine.PRIORITY));
				}
			}
			
			// 最新のステータス一覧と画面に表示されているステータス一覧の比較を行う			
			for (Map.Entry<StatusKey, Integer> newStatusKey : newStatusMap.entrySet()) {
				Integer tableStatus = oldStatusMap.get(newStatusKey.getKey());
				if (!newStatusKey.getValue().equals(tableStatus)) {
					statusChengedFlg = true;
					break;
				}
			}
		}
		
		if(ClusterControlPlugin.getDefault().getPreferenceStore().getBoolean(MonitorPreferencePage.P_STATUS_NEW_STATE_FLG) &&
			refreshFlg && statusChengedFlg){
			if(ClientSession.isDialogFree()){
				ClientSession.occupyDialog();
				// 新しいステータスが発生した場合、エラーダイアログを表示する
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				Shell shell = null;
				if (window != null) {
					shell = window.getShell();
					shell.forceActive();
				}
				// 別ウインドウを表示していたときは、点滅して知らせる
				MessageDialogWithToggle.openInformation(
						shell,
						Messages.getString("message"),
						Messages.getString("message.monitor.85"),
						Messages.getString("message.will.not.be.displayed"),
						false,
						ClusterControlPlugin.getDefault().getPreferenceStore(),
						MonitorPreferencePage.P_STATUS_NEW_STATE_FLG);
				ClientSession.freeDialog();
			}
		}
		this.updateStatus(statusList);
		tableViewer.setInput(statusList);
	}

	private Map<String, ArrayList<ArrayList<Object>>> getStatusList(String facilityId, List<String> managerList) {
		StatusSearchRunUtil util = new StatusSearchRunUtil();
		StatusFilterInfo filter = new StatusFilterInfo();
		filter.setFacilityType(FacilityTargetConstant.TYPE_ALL);
		Map<String, ArrayList<ArrayList<Object>>> map = util.searchInfo(managerList, facilityId, filter);
		return map;
	}

	private Map<String, ArrayList<ArrayList<Object>>> getStatusListByCondition(String facilityId,
			Property condition, List<String> managerList) {

		PropertyUtil.deletePropertyDefine(condition);
		StatusFilterInfo filter = StatusFilterPropertyUtil.property2dto(condition);
		StatusSearchRunUtil util = new StatusSearchRunUtil();
		Map<String, ArrayList<ArrayList<Object>>> map = util.searchInfo(managerList, facilityId, filter);

		return map;
	}

	/**
	 * ステータスラベルを更新します。<BR>
	 * 引数で指定されたステータス情報一覧より、重要度ごとの件数，全件数を取得し、
	 * ステータスラベルを更新します。
	 *
	 * @param map ステータス情報一覧
	 */
	private void updateStatus( ArrayList<ArrayList<Object>> list ){
		int[] status = new int[4];
		for(ArrayList<Object> data : list ){
			
			// メッセージの置き換えを実施
			String mes = ((String)data.get(GetStatusListTableDefine.MESSAGE));
			data.set(GetStatusListTableDefine.MESSAGE, HinemosMessage.replace(mes));
			
			int value = ((Integer) data.get(GetStatusListTableDefine.PRIORITY)).intValue();
			switch (value) {
			case PriorityConstant.TYPE_CRITICAL:
				status[0]++;
				break;
			case PriorityConstant.TYPE_WARNING:
				status[1]++;
				break;
			case PriorityConstant.TYPE_INFO:
				status[2]++;
				break;
			case PriorityConstant.TYPE_UNKNOWN:
				status[3]++;
				break;
			default: // 既定の対処はスルー。
				break;
			}
		}

		// ラベル更新
		this.criticalLabel.setText(String.valueOf(status[0]));
		this.warningLabel.setText(String.valueOf(status[1]));
		this.infoLabel.setText(String.valueOf(status[2]));
		this.unknownLabel.setText(String.valueOf(status[3]));
		int total = status[0] + status[1] + status[2] + status[3];
		this.totalLabel.setText(Messages.getString("filtered.records", new Object[]{total}));
	}
	
	/**
	 * ステータス一覧マップのキークラス<BR>
	 *
	 */
	private static class StatusKey {
		/** ファシリティID */
		private String facilityId;
		
		/** 監視項目ID */
		private String monitorId;
		
		/** 監視詳細ID*/
		private String monitorDetailId;
		
		/** プラグインID */
		private String pluginId;
		
		private StatusKey (String facilityId, String monitorId, String monitorDetailId, String pluginId) {
			this.facilityId = facilityId;
			this.monitorId = monitorId;
			this.monitorDetailId = monitorDetailId;
			this.pluginId = pluginId;
		}

		/**
		 * @return the facilityId
		 */
		private String getFacilityId() {
			return facilityId;
		}

		/**
		 * @return the monitorId
		 */
		private String getMonitorId() {
			return monitorId;
		}

		/**
		 * @return the monitorDetailId
		 */
		private String getMonitorDetailId() {
			return monitorDetailId;
		}

		/**
		 * @return the pluginId
		 */
		private String getPluginId() {
			return pluginId;
		}

		@Override
		public boolean equals(Object obj) {
			StatusKey statusKey1 = null;
			if (obj instanceof StatusKey) {
				statusKey1 = (StatusKey)obj;
				// キーの値を比較する。
				return this.facilityId.equals(statusKey1.getFacilityId()) && 
						this.monitorId.equals(statusKey1.getMonitorId()) && 
						this.monitorDetailId.equals(statusKey1.getMonitorDetailId()) && 
						this.pluginId.equals(statusKey1.getPluginId());
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(this.facilityId, this.monitorId, this.monitorDetailId, this.pluginId);
		}
		
	}

}
