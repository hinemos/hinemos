/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.composite;

import java.text.SimpleDateFormat;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.action.CommentEvent;
import com.clustercontrol.monitor.action.GetEventListTableDefine;
import com.clustercontrol.monitor.dialog.EventInfoDialog;
import com.clustercontrol.monitor.preference.MonitorPreferencePage;
import com.clustercontrol.monitor.util.ConvertListUtil;
import com.clustercontrol.monitor.util.EventFilterPropertyUtil;
import com.clustercontrol.monitor.util.EventSearchRunUtil;
import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.monitor.EventDataInfo;
import com.clustercontrol.ws.monitor.EventFilterInfo;
import com.clustercontrol.ws.monitor.ViewListInfo;

/**
 * イベント情報一覧のコンポジットクラス<BR>
 *
 * イベント情報一覧部分のテーブルのコンポジット
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class EventListComposite extends Composite {
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
	
	/** 最新受信日付 */
	// private static Long m_latestOutputDate = 0l;

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
	public EventListComposite(Composite parent, int style) {
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
		this.tableViewer.createTableColumn(GetEventListTableDefine.getEventListTableDefine(),
				GetEventListTableDefine.SORT_COLUMN_INDEX1,
				GetEventListTableDefine.SORT_COLUMN_INDEX2,
				GetEventListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}
		// ダブルクリックした場合、イベントログの詳細情報ダイアログを表示する

		this.tableViewer.addDoubleClickListener(
				new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {

						// 選択アイテムを取得する
						List<?> list = (List<?>) ((StructuredSelection)event.getSelection()).getFirstElement();

						EventInfoDialog dialog = new EventInfoDialog(m_shell, list);
						if (dialog.open() == IDialogConstants.OK_ID) {
							Property eventdetail = dialog.getInputData();
							CommentEvent comment = new CommentEvent();
							String managerName = (String) list.get(GetEventListTableDefine.MANAGER_NAME);
							comment.updateComment(managerName, eventdetail);
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
	
	private Map<String, ViewListInfo> dispDataMap;
	public void resetDisp() {
		dispDataMap = new ConcurrentHashMap<>();
	}
	
	/**
	 * ビューを更新します。<BR>
	 * 引数で指定されたファシリティの配下全てのファシリティのイベント一覧情報を取得し、
	 * 共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>監視管理のプレファレンスページより、監視[イベント]ビューの表示イベント数を取得します。</li>
	 * <li>引数で指定されたファシリティに属するイベント一覧情報を、表示イベント数分取得します。</li>
	 * <li>表示イベント数を超える場合、メッセージダイアログを表示します。</li>
	 * <li>共通テーブルビューアーにイベント情報一覧をセットします。</li>
	 * </ol>
	 *
	 * @param facilityId 表示対象の親ファシリティID
	 * @param condition 検索条件（条件なしの場合はnull）
	 * @param managerList マネージャ名リスト
	 * @param refreshFlag リフレッシュフラグ
	 * @see #updateStatus(ViewListInfo)
	 */
	public void setDisp(String facilityId, Property condition, List<String> managerList) {
		/** 表示用リスト */

		if(facilityId == null) {
			facilityId = ReservedFacilityIdConstant.ROOT_SCOPE;
		}
		Map<String, ViewListInfo> map = null;
		if(condition == null) {
			map = getEventList(facilityId, managerList);
		} else {
			map = getEventListByCondition(facilityId, condition, managerList);
		}
		
		for (Map.Entry<String, ViewListInfo> entry : map.entrySet()) {
			ViewListInfo viewListInfo = dispDataMap.get(entry.getKey());
			if (viewListInfo == null) {
				dispDataMap.put(entry.getKey(), entry.getValue());
				continue;
			}
			ViewListInfo value = entry.getValue();
			for (EventDataInfo info : value.getEventList()) {
				boolean flag = true;
				for (EventDataInfo info2 : viewListInfo.getEventList()) {
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
	
	public void updateDisp(boolean refreshFlag) {
		super.update();

		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		sdf.setTimeZone(TimezoneUtil.getTimeZone());
		label = Messages.getString("search.from") + ":";
		boolean flag = false;
		for (Entry<String, ViewListInfo> entry : dispDataMap.entrySet()) {
			if (flag) {
				label += ", ";
			}
			ViewListInfo info = entry.getValue();
			if (info.getFromOutputDate() == null) {
				label += "ALL";
			} else {
				Date fromDate = new Date(info.getFromOutputDate());
				label += sdf.format(fromDate);
			}
			label += "(" + entry.getKey() + ")";
			flag = true;
		}
		
		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		List<EventDataInfo> eventListRaw = ConvertListUtil.eventLogDataMap2SortedList(dispDataMap);
		ArrayList<ArrayList<Object>> eventList = ConvertListUtil.eventLogList2Input(eventListRaw);
		int total = 0;
		for(Map.Entry<String, ViewListInfo> entrySet : dispDataMap.entrySet()) {
			total += entrySet.getValue().getTotal();
		}
		
		boolean newEventFlg = false;
		// イベント履歴の最新を取得する
		if (refreshFlag) {
			Date tableOutputDate = null;
			Date takenOutputDate = null;
			if (!eventListRaw.isEmpty()) {
				tableOutputDate = new Date(eventListRaw.get(0).getOutputDate());
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

	private Map<String, ViewListInfo> getEventList(String facilityId, List<String> managerList) {
		int messages = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(
				MonitorPreferencePage.P_EVENT_MAX);
		EventSearchRunUtil bean = new EventSearchRunUtil();
		Map<String, ViewListInfo> map = bean.searchInfo(managerList, facilityId, null, messages);
		return map;
	}

	private Map<String, ViewListInfo> getEventListByCondition(String facilityId,
			Property condition, List<String> managerList) {
		int messages = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(
				MonitorPreferencePage.P_EVENT_MAX);

		PropertyUtil.deletePropertyDefine(condition);
		EventFilterInfo filter = EventFilterPropertyUtil.property2dto(condition);
		EventSearchRunUtil bean = new EventSearchRunUtil();
		Map<String, ViewListInfo> map = bean.searchInfo(managerList, facilityId, filter, messages);
		return map;
	}

	/**
	 * ステータスラベルを更新します。<BR>
	 * 引数で指定されたビュー一覧情報より、重要度ごとの件数，全件数を取得し、
	 * ステータスラベルを更新します。
	 *
	 * @param map ビュー一覧情報
	 */
	private void updateStatus(List<EventDataInfo> list) {
		// 表示最大件数の取得
		int critical = 0, warning = 0, info = 0, unknown = 0, total = 0;
		for( EventDataInfo eventInfo : list ){
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
}
