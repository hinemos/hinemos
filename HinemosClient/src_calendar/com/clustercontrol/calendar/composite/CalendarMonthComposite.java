/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.composite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.CalendarMonthResponse;

import com.clustercontrol.calendar.action.GetCalendarMonthTableDefine;
import com.clustercontrol.calendar.composite.action.VerticalBarSelectionListener;
import com.clustercontrol.calendar.util.CalendarRestClientWrapper;
import com.clustercontrol.calendar.view.CalendarWeekView;
import com.clustercontrol.calendar.viewer.CalendarMonthTableViewer;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * カレンダ[月間予定]ビューコンポジットクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class CalendarMonthComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( CalendarMonthComposite.class );
	/** テーブルビューワ */
	private CalendarMonthTableViewer m_viewer = null;
	/** 現在の月ラベルID */
	private Label m_labelId = null;
	/** 月カレンダテーブル */
	private Table calTable = null;
	/** カレンダID */
	private String m_calendarId = null;
	/** 月間カレンダにて現在表示している年 */
	private int nowYear;
	/** 現在表示している月 */
	private int nowMonth;
	/** 年月ラベル */
	private Label yearMonth = null;
	/** 選択されている日 */
	private int selectedDay = 32;
	/** カレンダサマリ */
	private List<Integer> m_summaryInfo = null;
	/** マネージャ名 */
	private String m_managerName = null;

	private final static String fontStr = "MS UI Gothic";

	/**
	 * フォントは何度もnewするとリークするので、複数定義しない。
	 */
	private final static Font yearMonthFont =  new Font(Display.getCurrent(), fontStr, 15, 300);;
	private final static Font normalFont = new Font(Display.getCurrent(), fontStr, 10, 0);
	private final static Font boldFont = new Font(Display.getCurrent(), fontStr, 10, SWT.BOLD);

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
	 * @since 4.1.0
	 */
	public CalendarMonthComposite(Composite parent, int style) {
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
		layout.numColumns = 15;
		//カレンダID
		m_labelId = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "id", m_labelId);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 15;
		m_labelId.setText(Messages.getString("calendar.id") + " : ");
		m_labelId.setLayoutData(gridData);

		//前年戻るボタン[ << ]
		Button prevYear = new Button(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, prevYear);

		//prevYear.setData(ClusterControlPlugin.CUSTOM_WIDGET_ID, "calendarMonthCompositePrevYearButton");
		prevYear.setText(Messages.getString("view.calendar.month.previous.year"));
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		prevYear.setLayoutData(gridData);
		prevYear.setToolTipText(Messages.getString("view.calendar.month.previous.year.text"));
		prevYear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(nowYear <= 0){
					return;
				}
				nowYear = nowYear - 1;
				updateMonth();
			}
		});
		//前月戻るボタン[ < ]
		Button prevMonth = new Button(this, SWT.NONE);
		//prevMonth.setData(ClusterControlPlugin.CUSTOM_WIDGET_ID, "calendarMonthCompositePrevMonthButton");
		prevMonth.setText(Messages.getString("view.calendar.month.previous.month"));
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		prevMonth.setLayoutData(gridData);
		prevMonth.setLayoutData(gridData);
		prevMonth.setToolTipText(Messages.getString("view.calendar.month.previous.month.text"));
		prevMonth.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(nowMonth <= 0 || nowYear <= 0){
					return;
				}
				if(nowMonth == 1){
					nowMonth = 12;
					nowYear--;
				}
				else {
					nowMonth = nowMonth - 1;
				}
				updateMonth();
			}
		});
		//年月ラベル
		yearMonth = new Label(this, SWT.CENTER);
		WidgetTestUtil.setTestId(this, "yearmonth", yearMonth);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		yearMonth.setText(Messages.getString("----/--"));
		yearMonth.setFont(yearMonthFont);
		yearMonth.setLayoutData(gridData);
		//翌月進むボタン[ > ]
		Button nextMonth = new Button(this, SWT.NONE);
		//nextMonth.setData(ClusterControlPlugin.CUSTOM_WIDGET_ID, "calendarMonthCompositeNextMonthButton");
		nextMonth.setText(Messages.getString("view.calendar.month.next.month"));
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		nextMonth.setLayoutData(gridData);
		nextMonth.setToolTipText(Messages.getString("view.calendar.month.next.month.text"));
		nextMonth.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(nowMonth <= 0 || nowYear <= 0){
					return;
				}
				if(nowMonth == 12){
					nowMonth = 1;
					nowYear++;
				}else {
					nowMonth = nowMonth + 1;
				}
				updateMonth();
			}
		});
		//翌年進むボタン[ >> ]
		Button nextYear = new Button(this, SWT.NONE);
		//nextYear.setData(ClusterControlPlugin.CUSTOM_WIDGET_ID, "calendarMonthCompositeNextYearButton");
		nextYear.setText(Messages.getString("view.calendar.month.next.year"));
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		nextYear.setLayoutData(gridData);
		nextYear.setToolTipText(Messages.getString("view.calendar.month.next.year.text"));
		nextYear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(nowYear <= 0){
					return;
				}
				nowYear = nowYear + 1;
				updateMonth();
			}
		});
		//カレンダテーブル
		calTable = new Table(this, SWT.SINGLE | SWT.BORDER | SWT.HIDE_SELECTION);
		WidgetTestUtil.setTestId(this, "cal", calTable);
		//calTable.setData(ClusterControlPlugin.CUSTOM_WIDGET_ID, "calendarMonthCompositeCalTable");
		calTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				calTable.setSelection(-1);
			}
		});
		createTableColAndContents(calTable);
		calTable.setHeaderVisible(true);
		calTable.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 15;
		calTable.setLayoutData(gridData);
		m_viewer = new CalendarMonthTableViewer(calTable);
		m_viewer.createTableColumn(GetCalendarMonthTableDefine.get());
		m_viewer.getTable().getVerticalBar().addSelectionListener(
				new VerticalBarSelectionListener(this));
	}

	/**
	 * 月間カレンダビューの
	 * セルが選択された際の処理
	 * @param table
	 */
	private void createTableColAndContents(final Table table){
		/** カレンダテーブルのカーソル */
		final TableCursor cursor = new TableCursor(table, SWT.NONE);
		final ControlEditor editor = new ControlEditor(cursor);
		editor.grabHorizontal = true;
		editor.grabVertical = true;
		cursor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem row = cursor.getRow();
				WidgetTestUtil.setTestId(this, null, row);
				int column = cursor.getColumn();
				String strDay = row.getText(column);
				if(!strDay.isEmpty()){
					//取得日より数字列のみ抽出
					selectedDay = Integer.parseInt(strDay.replaceAll("[^0-9]", ""));
				}
				update();
				if(0 < selectedDay && selectedDay < 32) {
					//ビュー更新
					//アクティブページを手に入れる
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IViewPart viewPart = page.findView(CalendarWeekView.ID);
					if (viewPart != null) {
						CalendarWeekView view = (CalendarWeekView) viewPart
								.getAdapter(CalendarWeekView.class);
						if (view == null) {
							m_log.info("widget selected: view is null"); 
							return;
						}
						view.update(m_managerName, m_calendarId,nowYear,nowMonth,selectedDay);
					}
				}
			}
		});
	}

	/**
	 * 更新処理<BR>
	 *
	 * @since 2.0.0
	 */
	public void init(String managerName, String calendarId) {
		if (calendarId == null) {
			return;
		}

		m_managerName = managerName;

		//カレンダラベルの更新
		if(calendarId.length() > 0){
			m_calendarId = calendarId;
			m_labelId.setText(Messages.getString("calendar.id") + " : " + calendarId);
		}else{
			m_labelId.setText(Messages.getString("calendar.id") + " : ");
		}
		//カレンダ月ラベルの更新
		Date date = new Date();
		SimpleDateFormat sdf = null;
		//月までの表示データでもマネージャのタイムゾーン反映させる
		sdf = new SimpleDateFormat("yyyy");
		sdf.setTimeZone(TimezoneUtil.getTimeZone());
		nowYear = Integer.parseInt(sdf.format(date));
		sdf = new SimpleDateFormat("MM");
		sdf.setTimeZone(TimezoneUtil.getTimeZone());
		nowMonth = Integer.parseInt(sdf.format(date));
		updateMonth();
	}

	/**
	 * 月カレンダ作成
	 * カレンダ一覧ビューの開始期間の月をはじめに表示する
	 * @param table
	 * @param year
	 * @param month
	 */
	private void updateMonth(){
		// 選択日をリセット
		selectedDay = 32;

		//サマリ情報取得
		try {
			CalendarRestClientWrapper wrapper = CalendarRestClientWrapper.getWrapper(m_managerName);
			List<CalendarMonthResponse> resDtoList = wrapper.getCalendarMonth(m_calendarId, nowYear, nowMonth);
			List<Integer> operationStatusList = new ArrayList<>();
			for(CalendarMonthResponse res : resDtoList) {
				switch (res.getOperationStatus()) {
				case ALL_OPERATION:
					operationStatusList.add(0);
					break;
				case PARTIAL_OPERATION:
					operationStatusList.add(1);
					break;
				case NOT_OPERATION:
					operationStatusList.add(2);
					break;
				}
			}
			m_summaryInfo = operationStatusList;
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					e.getMessage());
		} catch (CalendarNotFound e) {
			// カレンダを削除した際などは、ここを通る。
			m_log.info("update(), " + e.getMessage());
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("update(), " +e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					e.getMessage());
			return;
		}

		update();
	}

	@Override
	public void update() {
		if (m_calendarId == null || m_summaryInfo == null) {
			return;
		}

		yearMonth.setText(String.format("%04d/%02d", nowYear, nowMonth));
		//カレンダ取得
		/** カレンダ */
		Calendar calendar = null;
		calendar = Calendar.getInstance(TimezoneUtil.getTimeZone());
		calendar.clear();
		calendar.set(nowYear, nowMonth-1,1);
		//月のはじめの日にちを求めます
		/** 月の最初の日にち */
		int startDate = calendar.get(Calendar.DAY_OF_WEEK);
		/** 月の最後の日にち */
		int lastDate = (m_summaryInfo.size()+1);

		/***************************************************
		 * 例
		 * 	       j=0  j=1  j=2  j=3  j=4  j=5  j=6
		 *       | Sun| Mon| Thu| Wed| Thu| Fri| Sat|
		 *       ------------------------------------
		 *   i=1                               | 1  |
		 *   i=2 | 2  | 3  | 4  | 5  | 6  | 7  | 8  |
		 *   i=3 | 9  | 10 | 11 | 12 | 13 | 14 | 15 |
		 *   i=4 | 16 | 17 | 18 | 19 | 20 | 21 | 22 |
		 *   i=5 | 23 | 24 | 25 | 26 | 27 | 28 | 29 |
		 *   i=6 | 30 | 31 |
		 *
		 ***************************************************/
		calTable.removeAll();
		//日にち
		int day = 1;

		//月の週を設定する
		for (int i = 0; i < 6; i++){ // 6週間(1日が土曜日の場合は6週間存在する)
			//月の初めの曜日を元に日にちの最初の配置を決定
			int j = startDate;
			//週の2週目からは日曜日から配置
			startDate = GetCalendarMonthTableDefine.SUNDAY;

			//日にちを設定
			String[] days = new String[8];
			//1週間の日にちを設定する
			TableItem weekTableItem = new TableItem(calTable, SWT.NONE);
			WidgetTestUtil.setTestId(this, null, weekTableItem);
			//weekTableItem.setData(ClusterControlPlugin.CUSTOM_WIDGET_ID, "calendarMonthCompositeWeekTableItem");

			//月の終わりの日まで繰り返す
			while(j < days.length && day < lastDate){
				//記号と色取得
				String sign = "";
				boolean selectedFlag = false;
				if (selectedDay <= day && day < selectedDay + 7) {
					selectedFlag = true;
				}
				switch(m_summaryInfo.get(day - 1)){
				case 0:
					//○
					sign = Messages.getString("view.calendar.month.all");
					weekTableItem.setBackground(j, ColorConstantsWrapper.green());
					break;
				case 1:
					//△
					sign = Messages.getString("view.calendar.month.part");
					weekTableItem.setBackground(j, ColorConstantsWrapper.yellow());
					break;
				case 2:
					//×
					sign = Messages.getString("view.calendar.month.none");
					weekTableItem.setBackground(j, ColorConstantsWrapper.red());
					break;
				default: // 対応しない。
					break;
				}
				if (selectedFlag) {
					weekTableItem.setFont(j, boldFont);
				} else {
					weekTableItem.setFont(j, normalFont);
				}
				weekTableItem.setForeground(j, ColorConstantsWrapper.black());
				//日にちと記号を格納
				days[j] = String.format("%02d", day) + sign;
				j++;
				day++;

				WidgetTestUtil.setTestId(this, "weektableitem" + j, weekTableItem);
			}
			weekTableItem.setText(days);
		}
	}
}
