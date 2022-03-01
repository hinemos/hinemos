/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.composite;

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
import org.openapitools.client.model.CalendarPatternInfoResponse;
import org.openapitools.client.model.YMDResponse;

import com.clustercontrol.calendar.action.GetCalendarPatternTableDefine;
import com.clustercontrol.calendar.composite.action.CalendarPatternDoubleClickListener;
import com.clustercontrol.calendar.composite.action.CalendarPatternSelectionChangedListener;
import com.clustercontrol.calendar.composite.action.VerticalBarSelectionListener;
import com.clustercontrol.calendar.util.CalendarRestClientWrapper;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * カレンダ[カレンダパターン]ビューコンポジットクラス<BR>
 * @version 4.1.0
 * @since 4.1.0
 */
public class CalendarPatternComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( CalendarPatternComposite.class );
	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** テーブル */
	private Table calPatternInfoTable = null;
	/** ラベル */
	private Label m_labelCount = null;
	/** カレンダパターンID */
	private String m_calendarPatternId = null;

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
	 * カレンダパターンID
	 * @return m_holidayEtcId
	 */
	public String getCalendarPatternId() {
		return m_calendarPatternId;
	}

	/**
	 * カレンダパターンID
	 * @param id
	 */
	public void setCalendarPatternId(String id) {
		m_calendarPatternId = id;
	}
	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @param style
	 * @since 4.1.0
	 */
	public CalendarPatternComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * 初期化処理<BR>
	 *
	 * @since 4.1.0
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		//カレンダ[カレンダパターン]情報テーブル作成
		calPatternInfoTable = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		WidgetTestUtil.setTestId(this, "info", calPatternInfoTable);
		calPatternInfoTable.setHeaderVisible(true);
		calPatternInfoTable.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		calPatternInfoTable.setLayoutData(gridData);

		m_labelCount = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "count", m_labelCount);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelCount.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(calPatternInfoTable);
		m_viewer.createTableColumn(GetCalendarPatternTableDefine.get(),
				GetCalendarPatternTableDefine.SORT_COLUMN_INDEX1,
				GetCalendarPatternTableDefine.SORT_COLUMN_INDEX2,
				GetCalendarPatternTableDefine.SORT_ORDER);
		for (int i = 0; i < calPatternInfoTable.getColumnCount(); i++){
			calPatternInfoTable.getColumn(i).setMoveable(true);
		}

		m_viewer.addSelectionChangedListener(new CalendarPatternSelectionChangedListener(this));
		// ダブルクリックリスナの追加
		m_viewer.addDoubleClickListener(new CalendarPatternDoubleClickListener(this));
		m_viewer.getTable().getVerticalBar().addSelectionListener(
				new VerticalBarSelectionListener(this));
	}

	/**
	 * 更新処理<BR>
	 *
	 * @since 4.1.0
	 */
	@Override
	public void update() {
		List<CalendarPatternInfoResponse> list = null;

		//カレンダ[カレンダパターン]情報取得
		Map<String, List<CalendarPatternInfoResponse>> dispDataMap= new ConcurrentHashMap<String, List<CalendarPatternInfoResponse>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			try {
				CalendarRestClientWrapper wrapper = CalendarRestClientWrapper.getWrapper(managerName);
				list = wrapper.getCalendarPatternList(null);
			} catch (InvalidRole e) {
				// 権限なし
				errorMsgs.put( managerName, e.getMessage() );
			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
				errorMsgs.put( managerName, e.getMessage());
			}
			if(list == null){
				list = new ArrayList<CalendarPatternInfoResponse>();
			}
			dispDataMap.put(managerName, list);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		for(Map.Entry<String, List<CalendarPatternInfoResponse>> map : dispDataMap.entrySet()) {
			for (CalendarPatternInfoResponse info : map.getValue()) {
				ArrayList<Object> obj = new ArrayList<Object>();
				obj.add(map.getKey());
				obj.add(info.getCalendarPatternId());
				obj.add(info.getCalendarPatternName());
				obj.add(info.getCalPatternDetailInfoEntities().size());
				if(info.getCalPatternDetailInfoEntities() != null){
					StringBuilder set = new StringBuilder();
					for(int i = 0; i < info.getCalPatternDetailInfoEntities().size(); i++){
						//設定値を超えたら省略...
						if(i >= 5){
							set.append("...");
							break;
						}
						YMDResponse ymd = info.getCalPatternDetailInfoEntities().get(i);
						set.append(
								ymd.getYearNo() + "/"
								+ ymd.getMonthNo() + "/"
								+ ymd.getDayNo() + " , ");
					}
					obj.add(set);
				}
				else {
					obj.add("");
				}
				obj.add(info.getOwnerRoleId());
				obj.add(info.getRegUser());
				obj.add(info.getRegDate());
				obj.add(info.getUpdateUser());
				obj.add(info.getUpdateDate());
				obj.add(null);
				listInput.add(obj);
			}
		}

		m_viewer.setInput(listInput);

		Object[] args = { listInput.size() };
		m_labelCount.setText(Messages.getString("records", args));
	}
}
