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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.bean.Property;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.monitor.run.action.GetMonitorListTableDefine;
import com.clustercontrol.monitor.run.bean.MonitorTypeMessage;
import com.clustercontrol.monitor.util.MonitorFilterPropertyUtil;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.monitor.view.action.MonitorModifyAction;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorFilterInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * 監視設定一覧のコンポジットクラス<BR>
 *
 * 監視設定一覧部分のテーブルのコンポジット
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class MonitorListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( MonitorListComposite.class );

	// ----- instance フィールド ----- //

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	/** 表示内容ラベル */
	private Label statuslabel = null;

	/** 合計ラベル */
	private Label totalLabel = null;

	/** 検索条件 */
	private Property condition = null;

	/** 自分自身のComposite */
	private Composite composite = null;

	// ----- コンストラクタ ----- //

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public MonitorListComposite(Composite parent, int style) {
		super(parent, style);

		// 初期化
		initialize();
		composite = this;
		WidgetTestUtil.setTestId(this, null, composite);
	}

	// ----- instance メソッド ----- //

	/**
	 * コンポジットの初期化
	 */
	private void initialize() {

		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// 表示内容ラベルの作成
		this.statuslabel = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "status", statuslabel);
		this.statuslabel.setText("");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.statuslabel.setLayoutData(gridData);

		// テーブルの作成
		Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, table);
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
		this.tableViewer.createTableColumn(GetMonitorListTableDefine.get(),
				GetMonitorListTableDefine.SORT_COLUMN_INDEX1,
				GetMonitorListTableDefine.SORT_COLUMN_INDEX2,
				GetMonitorListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}

		this.tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				String managerName = "";
				String pluginId = "";
				String monitorId = "";

				if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
					ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event.getSelection()).getFirstElement();

					managerName = (String)info.get(GetMonitorListTableDefine.MANAGER_NAME);
					pluginId = (String) info.get(GetMonitorListTableDefine.MONITOR_TYPE_ID);
					monitorId = (String) info.get(GetMonitorListTableDefine.MONITOR_ID);
				}

				if(pluginId != null && monitorId != null){
					// ダイアログ名を取得
					MonitorModifyAction action = new MonitorModifyAction();
					// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
					if (action.dialogOpen(composite.getShell(), managerName, pluginId, monitorId) == IDialogConstants.OK_ID) {
						composite.update();
					}
				}
			}
		});

		// 合計ラベルの作成
		this.totalLabel = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "totallabel", totalLabel);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.totalLabel.setLayoutData(gridData);

	}

	/**
	 * コンポジットを更新します。
	 * <p>
	 *
	 * 検索条件が事前に設定されている場合、その条件にヒットする監視設定の一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全監視設定を表示します。
	 */
	@Override
	public void update() {
		// Message collecting
		Map<String, String> errMsgs = new ConcurrentHashMap<>();

		// データ取得
		Map<String, List<MonitorInfo>> dispDataMap= new ConcurrentHashMap<>();

		String conditionManager = null;
		if(condition != null) {
			conditionManager = JobPropertyUtil.getManagerName(condition);
		}

		if(conditionManager == null || conditionManager.equals("")) {
			if (this.condition == null) {
				this.statuslabel.setText("");
				// TODO Fetch list concurrently with multiple threads
				for(String managerName : EndpointManager.getActiveManagerSet()) {
					getMonitorList(managerName, dispDataMap, errMsgs);
				}
			} else {
				this.statuslabel.setText(Messages.getString("filtered.list"));
				PropertyUtil.deletePropertyDefine(this.condition);
				MonitorFilterInfo filter = MonitorFilterPropertyUtil.property2dto(this.condition);
				for (String managerName : EndpointManager.getActiveManagerSet()) {
					getMonitorListWithCondition(managerName, filter, dispDataMap, errMsgs);
				}
			}
		} else {
			getMonitorList(conditionManager, dispDataMap, errMsgs);
		}

		// Show message box
		if( 0 < errMsgs.size() ){
			UIManager.showMessageBox(errMsgs, true);
		}

		// MonitorInfo を tableViewer にセットするための詰め替え
		ArrayList<Object> listInput = new ArrayList<Object>();

		for( Map.Entry<String, List<MonitorInfo>> e: dispDataMap.entrySet() ){
			for (MonitorInfo monitor : e.getValue()) {
				ArrayList<Object> a = new ArrayList<Object>();
				a.add(e.getKey());
				a.add(monitor.getMonitorId());
				a.add(monitor.getMonitorTypeId());
				a.add(MonitorTypeMessage.typeToString(monitor.getMonitorType()));
				a.add(monitor.getDescription());
				a.add(monitor.getFacilityId());
				a.add(HinemosMessage.replace(monitor.getScope()));
				a.add(monitor.getCalendarId());
				if(monitor.getRunInterval() == 0){
					a.add("-");
				}else{
					a.add(monitor.getRunInterval() / 60 + Messages.getString("minute"));
				}
				a.add(monitor.isMonitorFlg());
				a.add(monitor.isCollectorFlg());
				a.add(monitor.getOwnerRoleId());
				a.add(monitor.getRegUser());
				a.add(new Date(monitor.getRegDate()));
				a.add(monitor.getUpdateUser());
				a.add(new Date(monitor.getUpdateDate()));
				a.add(null);

				listInput.add(a);
			}
		}

		// テーブル更新
		this.tableViewer.setInput(listInput);

		// 合計欄更新
		String[] args = { String.valueOf(listInput.size()) };
		String message = null;
		if (this.condition == null) {
			message = Messages.getString("records", args);
		} else {
			message = Messages.getString("filtered.records", args);
		}
		this.totalLabel.setText(message);
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
	 * 検索条件が事前に設定されている場合、その条件にヒットする監視設定の一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全監視設定を表示します。
	 */
	public void update(Property condition) {
		this.condition = condition;

		this.update();
	}

	private void getMonitorList(String managerName,
			Map<String, List<MonitorInfo>> dispDataMap,
			Map<String, String> errorMsgs) {
		try {
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
			List<MonitorInfo> list = wrapper.getMonitorList();
			
			if( null != list ){
				// メモリが溢れてしまうので、監視設定一覧に表示しない情報は落とす。
				for (MonitorInfo info : list) {
					info.setCustomCheckInfo(null);
					info.setCustomTrapCheckInfo(null);
					info.setHttpCheckInfo(null);
					info.setHttpScenarioCheckInfo(null);
					info.setJmxCheckInfo(null);
					info.setLogfileCheckInfo(null);
					info.setPerfCheckInfo(null);
					info.setPingCheckInfo(null);
					info.setPluginCheckInfo(null);
					info.setPortCheckInfo(null);
					info.setProcessCheckInfo(null);
					info.setSnmpCheckInfo(null);
					info.setSqlCheckInfo(null);
					info.setTrapCheckInfo(null);
					info.setWinEventCheckInfo(null);
					info.setWinServiceCheckInfo(null);
				}
				dispDataMap.put(managerName, list);
			}
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (MonitorNotFound_Exception | HinemosUnknown_Exception e) {
			errorMsgs.put( managerName, Messages.getString("message.monitor.67") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("update() getMonitorList, " + HinemosMessage.replace(e.getMessage()), e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
	}

	private void getMonitorListWithCondition(String managerName, MonitorFilterInfo filter,
			Map<String, List<MonitorInfo>> dispDataMap,
			Map<String, String> errorMsgs) {
		try {
			// マネージャにアクセス
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
			List<MonitorInfo> list = wrapper.getMonitorListByCondition(filter);
			if( null != list ){
				dispDataMap.put(managerName, list);
			}
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (MonitorNotFound_Exception | HinemosUnknown_Exception e) {
			errorMsgs.put( managerName, Messages.getString("message.monitor.67") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("update() getMonitorListByCondition, " + e.getMessage(), e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
	}
}
