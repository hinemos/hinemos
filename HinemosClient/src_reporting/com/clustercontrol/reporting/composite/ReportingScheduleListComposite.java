/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.ReportingScheduleResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.reporting.action.GetReportingScheduleTableDefine;
import com.clustercontrol.reporting.composite.action.ReportingDoubleClickListener;
import com.clustercontrol.reporting.util.ReportingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * レポーティングスケジュール一覧コンポジットクラスです。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ReportingScheduleListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory
			.getLog(ReportingScheduleListComposite.class);

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
	public ReportingScheduleListComposite(Composite parent, int style) {
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
		this.tableViewer.createTableColumn(
				GetReportingScheduleTableDefine.get(),
				GetReportingScheduleTableDefine.SORT_COLUMN_INDEX1,
				GetReportingScheduleTableDefine.SORT_COLUMN_INDEX2,
				GetReportingScheduleTableDefine.SORT_ORDER);

		// ダブルクリックリスナの追加
		this.tableViewer
				.addDoubleClickListener(new ReportingDoubleClickListener(this));
		
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
		List<ReportingScheduleResponse> reportingList = null;
		
		Map<String, List<ReportingScheduleResponse>> dispDataMap = new ConcurrentHashMap<String, List<ReportingScheduleResponse>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<String, String>();
		
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			ReportingRestClientWrapper wrapper = ReportingRestClientWrapper.getWrapper(managerName);
			try {
				boolean isPublish = wrapper.checkPublish().getPublish();
				if (!isPublish) {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.expiration.term.invalid"));
				}
			} catch (HinemosUnknown e) {
				// エンタープライズ機能が無効の場合は HinemosUnknownでラップしたUrlNotFoundとなる。
				// この場合は無視する
				if(UrlNotFound.class.equals(e.getCause().getClass())) {
					continue;
				}
				errorMsgs.put(managerName, Messages.getString("message.expiration.term"));
			} catch (InvalidRole | InvalidUserPass | RestConnectFailed e) {
				errorMsgs.put(managerName, Messages.getString("message.expiration.term"));
			}

			try {
				reportingList = wrapper.getReportingScheduleList();
			} catch (InvalidRole e) {
				// 権限なし
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				// 上記以外の例外
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("update(), " + errMessage, e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
			}
			if (reportingList == null) {
				reportingList = new ArrayList<ReportingScheduleResponse>();
			}
			
			dispDataMap.put(managerName, reportingList);
		}
		
		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}
		
		// tableViewer にセットするための詰め替え
		ArrayList<Object> listInput = new ArrayList<Object>();
		for(Map.Entry<String, List<ReportingScheduleResponse>> map: dispDataMap.entrySet()) {
			for (ReportingScheduleResponse reportingInfo : map.getValue()) {
				ArrayList<Object> a = new ArrayList<Object>();
				a.add(map.getKey());
				a.add(reportingInfo.getReportScheduleId());
				a.add(reportingInfo.getDescription());
				a.add(reportingInfo.getTemplateSetId());
				a.add(reportingInfo.getFacilityId());
				a.add(HinemosMessage.replace(reportingInfo.getScope()));
				a.add(reportingInfo.getCalendarId());
				a.add(reportingInfo.getValidFlg());
				a.add(reportingInfo.getOwnerRoleId());
				a.add(reportingInfo.getRegUser());
				a.add(reportingInfo.getRegDate());
				a.add(reportingInfo.getUpdateUser());
				a.add(reportingInfo.getUpdateDate());
				listInput.add(a);
			}
		}
		// テーブル更新
		this.tableViewer.setInput(listInput);
	}

	/**
	 * 選択された行のスケジュールIDを取得する
	 * 
	 * @return
	 */
	public ArrayList<String> getSelectionData() {

		ArrayList<String> data = new ArrayList<String>();

		// 選択されたアイテムを取得
		StructuredSelection selection = (StructuredSelection) tableViewer
				.getSelection();
		List<?> list = selection.toList();

		if (list != null) {
			for (int index = 0; index < list.size(); index++) {

				ArrayList<?> info = (ArrayList<?>) list.get(index);
				if (info != null && info.size() > 0) {
					String scheduleId = (String) info
							.get(GetReportingScheduleTableDefine.REPORT_SCHEDULE_ID);
					data.add(scheduleId);
				}
			}
		}

		return data;
	}
}