/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.composite;

import java.util.ArrayList;
import java.util.Date;
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

import com.clustercontrol.calendar.action.GetCalendarListTableDefine;
import com.clustercontrol.calendar.composite.action.CalendarDoubleClickListener;
import com.clustercontrol.calendar.composite.action.CalendarSelectionChangedListener;
import com.clustercontrol.calendar.composite.action.VerticalBarSelectionListener;
import com.clustercontrol.calendar.util.CalendarEndpointWrapper;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.calendar.CalendarInfo;
import com.clustercontrol.ws.calendar.InvalidRole_Exception;

/**
 * カレンダ一覧コンポジットクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class CalendarListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( CalendarListComposite.class );
	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** テーブル */
	private Table calListTable = null;
	/** ラベル */
	private Label m_labelCount = null;
	/** カレンダID */
	private String m_calendarId = null;

	/**
	 * このコンポジットが利用するテーブルビューアを取得します。<BR>
	 *
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return m_viewer;
	}
	/**
	 * このコンポジットが利用するテーブルを取得します。<BR>
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return m_viewer.getTable();
	}
	/**
	 * カレンダID
	 * @return m_calendarId
	 */
	public String getCalendarId() {
		return m_calendarId;
	}

	/**
	 * カレンダID
	 * @param calendarId
	 */
	public void setCalendarId(String calendarId) {
		m_calendarId = calendarId;
	}
	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @param style
	 * @since 2.0.0
	 */
	public CalendarListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * 初期化処理<BR>
	 *
	 * @since 2.0.0
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		//カレンダ一覧テーブル作成
		calListTable = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, calListTable);
		calListTable.setHeaderVisible(true);
		calListTable.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		calListTable.setLayoutData(gridData);

		m_labelCount = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "count", m_labelCount);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelCount.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(calListTable);
		m_viewer.createTableColumn(GetCalendarListTableDefine.get(),
				GetCalendarListTableDefine.SORT_COLUMN_INDEX1,
				GetCalendarListTableDefine.SORT_COLUMN_INDEX2,
				GetCalendarListTableDefine.SORT_ORDER);

		for (int i = 0; i < calListTable.getColumnCount(); i++){
			calListTable.getColumn(i).setMoveable(true);
		}
		m_viewer.addSelectionChangedListener(new CalendarSelectionChangedListener(this));
		// ダブルクリックリスナの追加
		m_viewer.addDoubleClickListener(new CalendarDoubleClickListener(this));
		m_viewer.getTable().getVerticalBar().addSelectionListener(
				new VerticalBarSelectionListener(this));
	}

	/**
	 * 更新処理<BR>
	 *
	 * @since 2.0.0
	 */
	@Override
	public void update() {
		List<CalendarInfo> list = null;

		//カレンダ一覧情報取得
		Map<String, List<CalendarInfo>> dispDataMap= new ConcurrentHashMap<String, List<CalendarInfo>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for(String managerName : EndpointManager.getActiveManagerSet()) {
			CalendarEndpointWrapper wrapper = CalendarEndpointWrapper.getWrapper(managerName);
			try {
				list = wrapper.getAllCalendarList();
			} catch (InvalidRole_Exception e) {
				// 権限なし
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
			if(list == null){
				list = new ArrayList<CalendarInfo>();
			}

			dispDataMap.put(managerName, list);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		for(Map.Entry<String, List<CalendarInfo>> map: dispDataMap.entrySet()) {
			for (CalendarInfo info : map.getValue()) {
				ArrayList<Object> obj = new ArrayList<Object>();
				obj.add(map.getKey());
				obj.add(info.getCalendarId());
				obj.add(info.getCalendarName());
				obj.add(new Date(info.getValidTimeFrom()));
				obj.add(new Date(info.getValidTimeTo()));
				obj.add(info.getDescription());
				obj.add(info.getOwnerRoleId());
				obj.add(info.getRegUser());
				obj.add(new Date(info.getRegDate()));
				obj.add(info.getUpdateUser());
				obj.add(new Date(info.getUpdateDate()));
				obj.add(null);
				listInput.add(obj);
			}
		}
		m_viewer.setInput(listInput);

		Object[] args = { listInput.size() };
		m_labelCount.setText(Messages.getString("records", args));
	}
}
