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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.openapitools.client.model.GetMonitorBeanListRequest;
import org.openapitools.client.model.MonitorFilterInfoRequest;
import org.openapitools.client.model.MonitorInfoBeanResponse;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.monitor.run.action.GetMonitorListTableDefine;
import com.clustercontrol.monitor.run.bean.MonitorTypeMessage;
import com.clustercontrol.monitor.util.MonitorFilterPropertyUtil;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.monitor.view.action.MonitorModifyAction;
import com.clustercontrol.sdml.util.SdmlClientUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

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
		Map<String, List<MonitorInfoBeanResponse>> dispDataMap= new ConcurrentHashMap<>();

		String conditionManager = null;
		if(this.condition == null) {
			this.statuslabel.setText("");
			
			for (String managerName : RestConnectManager.getActiveManagerSet()) {
				getMonitorList(managerName, dispDataMap, errMsgs);
			}
		} else {
			this.statuslabel.setText(Messages.getString("filtered.list"));
			
			conditionManager = JobPropertyUtil.getManagerName(this.condition);
			PropertyUtil.deletePropertyDefine(this.condition);
			MonitorFilterInfoRequest filter = MonitorFilterPropertyUtil.property2dto(this.condition);
			Set<String> managerSet = null;
			if (conditionManager == null || conditionManager.equals("")) {
				managerSet = RestConnectManager.getActiveManagerSet();
			
			} else {
				managerSet = new HashSet<String>() ;
				managerSet.add(conditionManager);
			}
			
			for (String managerName : managerSet) {
				getMonitorListWithCondition(managerName, filter, dispDataMap, errMsgs);
			}
		}

		// Show message box
		if( 0 < errMsgs.size() ){
			UIManager.showMessageBox(errMsgs, true);
		}

		// MonitorInfo を tableViewer にセットするための詰め替え
		ArrayList<Object> listInput = new ArrayList<Object>();

		for( Map.Entry<String, List<MonitorInfoBeanResponse>> e: dispDataMap.entrySet() ){
			for (MonitorInfoBeanResponse monitorBean : e.getValue()) {
				ArrayList<Object> a = new ArrayList<Object>();
				a.add(e.getKey());
				a.add(monitorBean.getMonitorId());
				if (SdmlClientUtil.isCreatedBySdml(monitorBean)) {
					// SDMLによって自動作成された監視の場合
					String pluginId = SdmlClientUtil.getPluginId(e.getKey(), monitorBean);
					if (pluginId != null && !pluginId.isEmpty()) {
						// プラグインIDにSDML監視種別のプラグインIDを表示する
						a.add(pluginId);
					} else {
						// 取得できなかった場合は通常通り
						a.add(monitorBean.getMonitorTypeId());
					}
				} else {
					a.add(monitorBean.getMonitorTypeId());
				}
				a.add(MonitorTypeMessage.codeToString(monitorBean.getMonitorType().toString()));
				a.add(monitorBean.getDescription());
				a.add(monitorBean.getFacilityId());
				a.add(HinemosMessage.replace(monitorBean.getScope()));
				a.add(monitorBean.getCalendarId());
				Integer runInterval = 0;
				switch (monitorBean.getRunInterval()) {
				case SEC_30:
					runInterval = RunInterval.TYPE_SEC_30.toSec();
					break;
				case MIN_01:
					runInterval = RunInterval.TYPE_MIN_01.toSec();
					break;
				case MIN_05:
					runInterval = RunInterval.TYPE_MIN_05.toSec();
					break;
				case MIN_10:
					runInterval = RunInterval.TYPE_MIN_10.toSec();
					break;
				case MIN_30:
					runInterval = RunInterval.TYPE_MIN_30.toSec();
					break;
				case MIN_60:
					runInterval = RunInterval.TYPE_MIN_60.toSec();
					break;
				case NONE:
					runInterval = 0;
					break;
				}
				a.add(runInterval);
				a.add(monitorBean.getMonitorFlg());
				a.add(monitorBean.getCollectorFlg());
				a.add(monitorBean.getOwnerRoleId());
				a.add(monitorBean.getRegUser());
				Date regDate = null;
				try {
					regDate = TimezoneUtil.getSimpleDateFormat().parse(monitorBean.getRegDate());
				} catch (ParseException ex) {
					// 何もしない
				}
				a.add(regDate);
				a.add(monitorBean.getUpdateUser());
				Date updateDate = null;
				try {
					updateDate = TimezoneUtil.getSimpleDateFormat().parse(monitorBean.getUpdateDate());
				} catch (ParseException ex) {
					// 何もしない
				}
				a.add(updateDate);
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
			Map<String, List<MonitorInfoBeanResponse>> dispDataMap,
			Map<String, String> errorMsgs) {
		try {
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(managerName);
			List<MonitorInfoBeanResponse> list = wrapper.getMonitorBeanList();
			
			if( null != list ){
				dispDataMap.put(managerName, list);
			}
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (HinemosUnknown e) {			errorMsgs.put( managerName, Messages.getString("message.monitor.67") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("update() getMonitorList, " + HinemosMessage.replace(e.getMessage()), e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
	}

	private void getMonitorListWithCondition(String managerName, MonitorFilterInfoRequest filter,
			Map<String, List<MonitorInfoBeanResponse>> dispDataMap,
			Map<String, String> errorMsgs) {
		try {
			// マネージャにアクセス
			GetMonitorBeanListRequest info = new GetMonitorBeanListRequest();
			info.setMonitorFilterInfo(filter);
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(managerName);
			List<MonitorInfoBeanResponse> list = wrapper.getMonitorBeanListByCondition(info);
			if( null != list ){
				dispDataMap.put(managerName, list);
			}
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (HinemosUnknown e) {			errorMsgs.put( managerName, Messages.getString("message.monitor.67") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("update() getMonitorListByCondition, " + e.getMessage(), e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
	}
}
