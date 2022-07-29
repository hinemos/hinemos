/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.composite;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.EventFilterBaseRequest;
import org.openapitools.client.model.EventLogInfoResponse;
import org.openapitools.client.model.GetEventListResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.DefaultLayoutSettingManager.ListLayout;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.composite.CustomizableListComposite;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.monitor.action.GetEventListTableDefine;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.dialog.EventInfoDialog;
import com.clustercontrol.monitor.preference.MonitorPreferencePage;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo.UserItemDisplayInfo;
import com.clustercontrol.monitor.util.ConvertListUtil;
import com.clustercontrol.monitor.util.EventDisplaySettingGetUtil;
import com.clustercontrol.monitor.util.EventSearchRunUtil;
import com.clustercontrol.monitor.util.MonitorResultRestClientWrapper;
import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.util.DateTimeStringConverter;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;


/**
 * イベント情報一覧のコンポジットクラス<BR>
 *
 * イベント情報一覧部分のテーブルのコンポジット
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class EventListComposite extends CustomizableListComposite {
	
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
	
	private MultiManagerEventDisplaySettingInfo eventDspSetting = null; 

	/** 更新成功可否フラグ */
	private boolean m_updateSuccess = true;

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
	public EventListComposite(Composite parent, int style, ListLayout listLayout) {
		super(parent, style, listLayout);
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
		this.tableViewer.createTableColumn(GetEventListTableDefine.getEventListTableDefine(),
				GetEventListTableDefine.SORT_COLUMN_INDEX1,
				GetEventListTableDefine.SORT_COLUMN_INDEX2,
				GetEventListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}
		
		//デフォルトレイアウトのよる列順の変更
		updateColumnOrder(table);
		
		//イベント表示設定情報の取得
		eventDspSetting = getEventDisplaySettingGetUtil(RestConnectManager.getActiveManagerNameList());
		
		//ユーザ拡張項目、イベント番号の表示／非表示、列名の切替
		updateCustomizableColumn(table, null);
		
		//デフォルトレイアウトによる列幅の変更
		updateColumnWidth(table);
		
		// ダブルクリックした場合、イベントログの詳細情報ダイアログを表示する

		this.tableViewer.addDoubleClickListener(
				new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {

						// 選択アイテムを取得する
						List<?> list = (List<?>) ((StructuredSelection)event.getSelection()).getFirstElement();

						EventInfoDialog dialog = new EventInfoDialog(m_shell, list, getEventDspSetting());
						if (dialog.open() == IDialogConstants.OK_ID) {
							//ダイアログがOKボタンで閉じられた場合の動作
							
							String managerName = (String) list.get(GetEventListTableDefine.MANAGER_NAME);
							dialog.okButtonPress(managerName);
							IWorkbench workbench = ClusterControlPlugin.getDefault().getWorkbench();
							IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();

							EventView eventView = (EventView) page.findView(EventView.ID);
							if (eventView != null){
								eventView.update(false);
							}
						}
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
	
	private String label = "";

	public String getLabel () {
		return label;
	}
	
	private Map<String, GetEventListResponse> dispDataMap;
	public void resetDisp() {
		dispDataMap = new ConcurrentHashMap<>();
	}
	
	/**
	 * ビューを更新します。
	 *
	 * @param filter 検索条件
	 * @param managerList マネージャ名リスト
	 */
	public void setDisp(EventFilterBaseRequest filter, List<String> managerList) {
		
		//イベント表示設定情報の取得
		eventDspSetting = getEventDisplaySettingGetUtil(RestConnectManager.getActiveManagerNameList());
		
		updateCustomizableColumn(this.tableViewer.getTable(), managerList);
		updateColumnWidth(this.tableViewer.getTable(), GetEventListTableDefine.getEventListTableDefine());

		int messages = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(MonitorPreferencePage.P_EVENT_MAX);
		EventSearchRunUtil bean = new EventSearchRunUtil();
		Map<String, GetEventListResponse> map = bean.searchInfo(managerList, filter, messages);
		m_updateSuccess = bean.isSearchSuccess();

		for (Map.Entry<String, GetEventListResponse> entry : map.entrySet()) {
			GetEventListResponse viewListInfo = dispDataMap.get(entry.getKey());
			if (viewListInfo == null) {
				dispDataMap.put(entry.getKey(), entry.getValue());
				continue;
			}
			GetEventListResponse value = entry.getValue();
			for (EventLogInfoResponse info : value.getEventList()) {
				boolean flag = true;
				for (EventLogInfoResponse info2 : viewListInfo.getEventList()) {
					if (info2.getFacilityId().equals(info.getFacilityId()) &&
						info2.getMonitorId().equals(info.getMonitorId()) &&
						info2.getMonitorDetailId().equals(info.getMonitorDetailId()) &&
						info2.getPluginId().equals(info.getPluginId()) &&
						info2.getOutputDate().equals(info.getOutputDate())) {
						flag = false;
						break;
					}
				}
				
				// monitor_id, monitor_detail_id, plugin_id, output_date, facility_id
				if (flag){
					viewListInfo.getEventList().add(info);
				}
			}
			viewListInfo.setCritical(viewListInfo.getCritical() + value.getCritical());
			viewListInfo.setInfo(viewListInfo.getInfo() + value.getInfo());
			viewListInfo.setTotal(viewListInfo.getTotal() + value.getTotal());
			viewListInfo.setUnKnown(viewListInfo.getUnKnown() + value.getUnKnown());
			viewListInfo.setWarning(viewListInfo.getWarning() + value.getWarning());
		}
	}
	
	public void updateDisp(boolean refreshFlag) throws ParseException, HinemosUnknown {
		super.update();

		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		label = Messages.getString("search.from") + ":";
		boolean flag = false;
		for (Entry<String, GetEventListResponse> entry : dispDataMap.entrySet()) {
			if (flag) {
				label += ", ";
			}
			GetEventListResponse info = entry.getValue();
			if (info.getFromOutputDate() == null) {
				label += "ALL";
			} else {
				Long fromDateLong = null;
				Date tmpDate = MonitorResultRestClientWrapper.parseDate(info.getFromOutputDate());
				fromDateLong = tmpDate.getTime();
				Date fromDate = new Date(fromDateLong);
				label += DateTimeStringConverter.formatDate(fromDate);
			}
			label += "(" + entry.getKey() + ")";
			flag = true;
		}
		
		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		List<EventLogInfoResponse> eventListRaw = ConvertListUtil.eventLogDataMap2SortedList(dispDataMap);
		ArrayList<ArrayList<Object>> eventList = ConvertListUtil.eventLogList2Input(eventListRaw);
		int total = 0;
		for(Map.Entry<String, GetEventListResponse> entrySet : dispDataMap.entrySet()) {
			total += entrySet.getValue().getTotal();
		}
		
		boolean newEventFlg = false;
		// イベント履歴の最新を取得する
		if (refreshFlag) {
			Date tableOutputDate = null;
			Date takenOutputDate = null;
			if (!eventListRaw.isEmpty()) {
				Long tableOutputDateLong = MonitorResultRestClientWrapper.parseDate(eventListRaw.get(0).getOutputDate()).getTime();
				tableOutputDate = new Date(tableOutputDateLong);
			}

			// 画面に表示されているイベント履歴の最新を取得する
			Table table = this.getTable();
			Date tempOutputDate = null;
			if (table.getItems().length != 0) {
				TableItem[] takenOutputTableItems = table.getItems();
				for (int i = 0; i < takenOutputTableItems.length; i++) {
					@SuppressWarnings("unchecked")
					List<Object> list = (ArrayList<Object>) table.getItems()[i].getData();
					tempOutputDate = (Date) list.get(GetEventListTableDefine.RECEIVE_TIME);
					if (takenOutputDate == null || tempOutputDate.after(takenOutputDate)) {
						takenOutputDate = tempOutputDate;
					}
				}
				// イベント履歴の最新と画面に表示されているイベント履歴の最新を比較
				if (tableOutputDate != null && takenOutputDate != null && 
						tableOutputDate.after(takenOutputDate)) {
					newEventFlg = true;
				}
			}
		}
		if(ClusterControlPlugin.getDefault().getPreferenceStore().getBoolean(MonitorPreferencePage.P_EVENT_MESSAGE_FLG) &&
				total > eventList.size() &&
				ClientSession.isDialogFree()){
			ClientSession.occupyDialog();
			// 最大表示件数を超える場合、エラーダイアログを表示する
			MessageDialogWithToggle.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.monitor.12"),
					Messages.getString("message.will.not.be.displayed"),
					false,
					ClusterControlPlugin.getDefault().getPreferenceStore(),
					MonitorPreferencePage.P_EVENT_MESSAGE_FLG);
			ClientSession.freeDialog();
		}
		
		if(ClusterControlPlugin.getDefault().getPreferenceStore().getBoolean(MonitorPreferencePage.P_EVENT_NEW_EVENT_FLG) &&
			refreshFlag && newEventFlg &&
			ClientSession.isDialogFree()){
			ClientSession.occupyDialog();
			// 新しいイベントが発生した場合、エラーダイアログを表示する
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
					Messages.getString("message.monitor.84"),
					Messages.getString("message.will.not.be.displayed"),
					false,
					ClusterControlPlugin.getDefault().getPreferenceStore(),
					MonitorPreferencePage.P_EVENT_NEW_EVENT_FLG);
			ClientSession.freeDialog();
		}

		this.updateStatus(eventListRaw);
		tableViewer.setInput(eventList);
	}

	private MultiManagerEventDisplaySettingInfo getEventDisplaySettingGetUtil(List<String> managerList) {
		EventDisplaySettingGetUtil bean = new EventDisplaySettingGetUtil();
		MultiManagerEventDisplaySettingInfo info = bean.getEventDisplaySettingInfo(managerList);
		m_updateSuccess = bean.isUpdateSuccess();
		
		return info;
	}

	/**
	 * ステータスラベルを更新します。<BR>
	 * 引数で指定されたビュー一覧情報より、重要度ごとの件数，全件数を取得し、
	 * ステータスラベルを更新します。
	 *
	 * @param map ビュー一覧情報
	 */
	private void updateStatus(List<EventLogInfoResponse> list) {
		// 表示最大件数の取得
		int critical = 0, warning = 0, info = 0, unknown = 0, total = 0;
		for( EventLogInfoResponse eventInfo : list ){
			// ラベル更新
			switch( eventInfo.getPriority() ){
			case( PriorityConstant.TYPE_INFO ):
				info++;
				break;
			case( PriorityConstant.TYPE_WARNING ):
				warning++;
				break;
			case( PriorityConstant.TYPE_CRITICAL ):
				critical++;
				break;
			case( PriorityConstant.TYPE_UNKNOWN ):
				unknown++;
				break;
			default: // 既定の対象は、スルー。
				break;
			}
			total++;
		}
		this.criticalLabel.setText(String.valueOf(critical));
		this.warningLabel.setText(String.valueOf(warning));
		this.infoLabel.setText(String.valueOf(info));
		this.unknownLabel.setText(String.valueOf(unknown));
		this.totalLabel.setText(Messages.getString("records", new Object[]{ total }));
	}
	

	/**
	 * ユーザ拡張イベント項目、イベント番号の表示更新
	 * 
	 * @param table
	 * @param managerList
	 */
	private void updateCustomizableColumn(Table table, List<String> managerList) {
		
		String managerName = null;
		if (managerList != null && managerList.size() == 1) {
			managerName = managerList.get(0);
		}
		
		TableColumn column = null;
		column = table.getColumn(GetEventListTableDefine.EVENT_NO);
		if (!eventDspSetting.isEventNoDisplay(managerName)) {
			column.setWidth(0);
			column.setMoveable(false);
			column.setResizable(false);
		} else {
			if (column.getWidth() == 0) {
				//非表示　→　表示に切り替わった場合
				column.setWidth(GetEventListTableDefine.EVENT_NO_WIDTH);
				column.setMoveable(true);
				column.setResizable(true);
			}
		}
		
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			column = table.getColumn(GetEventListTableDefine.getUserItemIndex(i));
			UserItemDisplayInfo itemDspInfo = eventDspSetting.getUserItemDisplayInfo(managerName, i);
			if (!itemDspInfo.getDisplayEnable()) {
				column.setWidth(0);
				column.setMoveable(false);
				column.setResizable(false);
			} else {
				if (column.getWidth() == 0) {
					//非表示　→　表示に切り替わった場合
					column.setWidth(GetEventListTableDefine.USERITEM_WIDTH);
					column.setMoveable(true);
					column.setResizable(true);	
				}
			}
			
			column.setText(itemDspInfo.getDisplayName());
			if (itemDspInfo.getHasMultiDisplayName()) {
				column.setToolTipText(itemDspInfo.getToolTipName());
			} else {
				column.setToolTipText("");
			}
		}
		
	}

	@Override
	public Map<String, Integer> getColumnIndexMap() {
		return GetEventListTableDefine.COLNAME_INDEX_MAP;
	}
	
	public MultiManagerEventDisplaySettingInfo getEventDspSetting() {
		if (this.eventDspSetting == null) {
			return new MultiManagerEventDisplaySettingInfo();
		}
		return this.eventDspSetting;
	}

	/**
	 * 更新成功可否を返します。
	 * @return 更新成功可否
	 */
	public boolean isUpdateSuccess() {
		return this.m_updateSuccess;
	}
}
