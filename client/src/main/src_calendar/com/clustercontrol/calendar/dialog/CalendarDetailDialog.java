/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.calendar.dialog;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.calendar.bean.DayOfWeekInMonthConstant;
import com.clustercontrol.calendar.bean.MonthConstant;
import com.clustercontrol.calendar.util.CalendarEndpointWrapper;
import com.clustercontrol.composite.action.NumberKeyListener;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimeStringConverter;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.calendar.CalendarDetailInfo;
import com.clustercontrol.ws.calendar.CalendarPatternInfo;
import com.clustercontrol.ws.calendar.InvalidRole_Exception;

/**
 * カレンダ詳細設定ダイアログ作成・変更ダイアログクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class CalendarDetailDialog extends CommonDialog{

	// ----- instance フィールド ----- //
	// ログ
	private static Log m_log = LogFactory.getLog( CalendarDetailDialog.class );
	/* ----- 変数 ----- */
	/**
	 * ダイアログの最背面レイヤのカラム数
	 * 最背面のレイヤのカラム数のみを変更するとレイアウトがくずれるため、
	 * グループ化されているレイヤは全てこれにあわせる
	 */
	private final int DIALOG_WIDTH = 8;
	/** 入力値を保持するオブジェクト */
	private CalendarDetailInfo inputData = null;
	/** 入力値の正当性を保持するオブジェクト。 */
	private ValidateResult m_validateResult = null;
	// ----- 共通メンバ変数 ----- //
	private Shell shell = null;
	private Group calDetailYearGroup = null; //年グループ
	private Group calDetailMonthGroup = null; //月グループ
	private Group calDetailDateGroup = null;// 日グループ
	private Group calDetailAfterDateGroup = null; //上記日程よりx日後設定グループ
	private Group calDetailTimeGroup = null; //時間グループ
	private Group calDetailOptGroup = null; //稼動非稼動設定グループ
	private Group calDetailSubstituteGroup = null; // 振り替えグループ
	/**ラジオボタン**/
	//年グループ
	private Button calDetailEveryYearRadio = null;
	private Button calDetailSpecifyYearRadio = null;

	//曜日グループ
	private Button calDetailAllDayRadio = null;
	private Button calDetailDayOfWeekRadio = null;
	private Button calDetailDateRadio = null;
	private Button calDetailCalPatternRadio = null;
	//稼動/非稼動グループ
	private Button calDetailOptOnRadio = null;
	private Button calDetailOptOffRadio = null;
	// 振り替えグループ
	private Button calDetailSubstituteCheck = null;
	private Text calDetailSubstituteTime = null;
	private Text calDetailSubstituteLimit = null;
	/**コンボボックス**/
	private Combo calDetailMonthCombo = null;
	private Combo calDetailDayOfWeekCombo = null;
	private Combo calDetailDayOfWeekInMonthCombo = null;
	private Combo calDetailDayGroup = null;
	private Combo calDetailCalPatternCombo = null;
	/**テキスト**/
	//説明
	private Text calDetailDescriptionText = null;
	//年
	private Text calDetailYearText = null;
	//曜日、日、その他 の日程からx日後
	private Text calDetailDaysLaterText = null;
	//時間 - 開始時間
	private Text calDetailTimeFromText = null;
	//時間 - 終了時間
	private Text calDetailTimeToText = null;

	// オーナーロールID
	private String ownerRoleId = null;
	/** マネージャ名 */
	private String managerName = null;

	// カレンダパターンマップ
	private Map<String, String> calPatternMap = null;

	/**
	 *
	 * @return
	 */
	public CalendarDetailInfo getInputData() {
		return this.inputData;
	}
	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param ownerRoleId オーナーロールID
	 */
	public CalendarDetailDialog(Shell parent, String managerName, String ownerRoleId) {
		super(parent);
		this.managerName = managerName;
		this.ownerRoleId = ownerRoleId;
	}
	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param identifier 変更する文字列監視の判定情報の識別キー
	 * @param ownerRoleId オーナーロールID
	 */
	public CalendarDetailDialog(Shell parent, String managerName, int order, String ownerRoleId){
		super(parent);
		this.managerName = managerName;
		this.ownerRoleId = ownerRoleId;
	}
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param detailInfo カレンダ詳細情報
	 * @param ownerRoleId オーナーロールID
	 */
	public CalendarDetailDialog(Shell parent, String managerName, CalendarDetailInfo detailInfo, String ownerRoleId){
		super(parent);
		this.managerName = managerName;
		this.inputData = detailInfo;
		this.ownerRoleId = ownerRoleId;
	}
	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		shell = this.getShell();

		// タイトル
		//カレンダ[詳細設定作成・変更]
		shell.setText(Messages.getString("dialog.calendar.detail.create.modify"));

		// ラベル
		GridData gridData = new GridData();
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = DIALOG_WIDTH;
		parent.setLayout(layout);
		/*
		 * 説明
		 */
		//ラベル
		Label label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, null, label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText("  " + Messages.getString("description"));
		//テキスト
		calDetailDescriptionText = new Text(parent, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "description", calDetailDescriptionText);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailDescriptionText.setLayoutData(gridData);
		calDetailDescriptionText.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_256));
		calDetailDescriptionText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * 「年」設定グループ
		 */
		calDetailYearGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "year", calDetailYearGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 8;
		calDetailYearGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailYearGroup.setLayoutData(gridData);
		//年
		calDetailYearGroup.setText(Messages.getString("year"));
		/*
		 * 年
		 */
		//「毎年」ラジオボタン
		this.calDetailEveryYearRadio = new Button(calDetailYearGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "year", calDetailEveryYearRadio);
		//毎年
		this.calDetailEveryYearRadio.setText(Messages.getString("calendar.detail.every.year"));
		GridData gridYearRadio = new GridData();
		gridYearRadio.horizontalSpan = 8;
		gridYearRadio.horizontalAlignment = GridData.FILL;
		gridYearRadio.grabExcessHorizontalSpace = true;
		this.calDetailEveryYearRadio.setLayoutData(gridYearRadio);
		this.calDetailEveryYearRadio.setSelection(true);
		// ラジオボタンのイベント
		this.calDetailEveryYearRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		//「毎年」ラジオボタン
		this.calDetailSpecifyYearRadio = new Button(calDetailYearGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "specifyyear", calDetailSpecifyYearRadio);
		//毎年
		this.calDetailSpecifyYearRadio.setText(Messages.getString("calendar.detail.specify.year"));
		gridYearRadio = new GridData();
		gridYearRadio.horizontalSpan = 3;
		gridYearRadio.horizontalAlignment = GridData.FILL;
		gridYearRadio.grabExcessHorizontalSpace = true;
		this.calDetailSpecifyYearRadio.setLayoutData(gridYearRadio);
		// ラジオボタンのイベント
		this.calDetailSpecifyYearRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		//テキスト
		calDetailYearText = new Text(calDetailYearGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "year", calDetailYearText);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailYearText.setLayoutData(gridData);
		calDetailYearText.setTextLimit(4);
		calDetailYearText.addKeyListener(new NumberKeyListener());
		calDetailYearText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				if ("-".equals(calDetailYearText.getText())) {
					calDetailYearText.setText("");
				}
				update();
			}
		});
		//ラベル
		label = new Label(calDetailYearGroup, SWT.LEFT);
		WidgetTestUtil.setTestId(this, null, label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("year"));
		/*
		 * 「月」設定グループ
		 */
		calDetailMonthGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "month", calDetailMonthGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 8;
		calDetailMonthGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailMonthGroup.setLayoutData(gridData);
		//年	月
		calDetailMonthGroup.setText(Messages.getString("month"));
		/*
		 * 月
		 */
		//ラベル
		Label lblCalID = new Label(calDetailMonthGroup, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "calid", lblCalID);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblCalID.setLayoutData(gridData);
		lblCalID.setText(Messages.getString("month"));
		// コンボ
		this.calDetailMonthCombo = new Combo(calDetailMonthGroup, SWT.RIGHT | SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "month", calDetailMonthCombo);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.calDetailMonthCombo.setLayoutData(gridData);
		//毎月、1月～12月までをコンボボックスで表示
		for(int i = 0; i < 13 ; i++){
			this.calDetailMonthCombo.add(MonthConstant.typeToString(i));
		}
		//初期設定は「毎月」
		this.calDetailMonthCombo.setText(MonthConstant.typeToString(0));
		/*
		 * 「日」設定グループ
		 *
		 */
		calDetailDateGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "date", calDetailDateGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 8;
		calDetailDateGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailDateGroup.setLayoutData(gridData);
		calDetailDateGroup.setText(Messages.getString("monthday"));
		//「すべての日」ラジオボタン
		this.calDetailAllDayRadio = new Button(calDetailDateGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "allday", calDetailAllDayRadio);
		//すべての日
		this.calDetailAllDayRadio.setText(Messages.getString("calendar.detail.everyday"));
		GridData gridDataRadio = new GridData();
		gridDataRadio.horizontalSpan = 3;
		gridDataRadio.horizontalAlignment = GridData.FILL;
		gridDataRadio.grabExcessHorizontalSpace = true;
		this.calDetailAllDayRadio.setLayoutData(gridDataRadio);
		this.calDetailAllDayRadio.setSelection(true);
		// ラジオボタンのイベント
		this.calDetailAllDayRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		// 空白
		label = new Label(calDetailDateGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank", label);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		//「曜日」ラジオボタン
		this.calDetailDayOfWeekRadio = new Button(calDetailDateGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "dayofweek", calDetailDayOfWeekRadio);
		this.calDetailDayOfWeekRadio.setText(Messages.getString("weekday"));
		gridDataRadio = new GridData();
		gridDataRadio.horizontalSpan = 3;
		gridDataRadio.horizontalAlignment = GridData.FILL;
		gridDataRadio.grabExcessHorizontalSpace = true;
		this.calDetailDayOfWeekRadio.setLayoutData(gridDataRadio);

		// ラジオボタンのイベント
		this.calDetailDayOfWeekRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		//「第x週選択」コンボボックス
		this.calDetailDayOfWeekInMonthCombo = new Combo(calDetailDateGroup, SWT.RIGHT | SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "dayofweekinmonth", calDetailDayOfWeekInMonthCombo);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.calDetailDayOfWeekInMonthCombo.setLayoutData(gridData);
		//毎週、第1週～第5週までの配列
		String dayOfWeekInMonth[] = new String[6];
		for(int i = 0; i < dayOfWeekInMonth.length ; i++){
			dayOfWeekInMonth[i] = DayOfWeekInMonthConstant.typeToString(i);
			this.calDetailDayOfWeekInMonthCombo.add(dayOfWeekInMonth[i]);
		}
		this.calDetailDayOfWeekInMonthCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		//「曜日選択」コンボボックス
		this.calDetailDayOfWeekCombo = new Combo(calDetailDateGroup, SWT.RIGHT | SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "dayofweek", calDetailDayOfWeekCombo);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.calDetailDayOfWeekCombo.setLayoutData(gridData);
		//日曜日から土曜日までの配列
		String dayOfWeek[] = new String[7];
		for(int i = 0; i < dayOfWeek.length ; i++){
			dayOfWeek[i] = DayOfWeekConstant.typeToString(i+1);
			this.calDetailDayOfWeekCombo.add(dayOfWeek[i]);
		}
		this.calDetailDayOfWeekCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		// 空白
		label = new Label(calDetailDateGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		//日にち
		this.calDetailDateRadio = new Button(calDetailDateGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "date", calDetailDateRadio);
		this.calDetailDateRadio.setText(Messages.getString("monthday"));
		gridDataRadio = new GridData();
		gridDataRadio.horizontalSpan = 3;
		gridDataRadio.horizontalAlignment = GridData.FILL;
		gridDataRadio.grabExcessHorizontalSpace = true;
		this.calDetailDateRadio.setLayoutData(gridDataRadio);
		// ラジオボタンのイベント
		this.calDetailDateRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		//「日にち」コンボボックス
		this.calDetailDayGroup = new Combo(calDetailDateGroup, SWT.RIGHT | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "day", calDetailDayGroup);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.calDetailDayGroup.setLayoutData(gridData);
		//1日～31日
		String date[] = new String[31];
		//		date[0] = "";
		//		this.cmbDays.add(date[0]);
		for(int i = 0; i < date.length ; i++){
			date[i] = (i+1) + "";
			this.calDetailDayGroup.add(date[i]);
		}
		this.calDetailDayGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		//ラベル
		Label day = new Label(calDetailDateGroup, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "day", day);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		day.setLayoutData(gridData);
		day.setText(Messages.getString("monthday"));
		// 空白
		label = new Label(calDetailDateGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		//その他
		this.calDetailCalPatternRadio = new Button(calDetailDateGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "calpattern", calDetailCalPatternRadio);
		this.calDetailCalPatternRadio.setText(Messages.getString("calendar.pattern"));
		gridDataRadio = new GridData();
		gridDataRadio.horizontalSpan = 3;
		gridDataRadio.horizontalAlignment = GridData.FILL;
		gridDataRadio.grabExcessHorizontalSpace = true;
		this.calDetailCalPatternRadio.setLayoutData(gridDataRadio);
		// ラジオボタンのイベント
		this.calDetailCalPatternRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		//「カレンダパターン」の選択コンボボックス
		this.calDetailCalPatternCombo = new Combo(calDetailDateGroup, SWT.RIGHT | SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "calpattern", calDetailCalPatternCombo);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = 150;
		this.calDetailCalPatternCombo.setLayoutData(gridData);
		this.calDetailCalPatternCombo.add("");
		this.calPatternMap = new ConcurrentHashMap<>();
		for(CalendarPatternInfo str : getCalendarPatternList(this.managerName, this.ownerRoleId)){
			this.calDetailCalPatternCombo.add(str.getCalPatternName());
			calPatternMap.put(str.getCalPatternId(), str.getCalPatternName());
		}
		this.calDetailCalPatternCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		//前後日グループ
		calDetailAfterDateGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "afterdate", calDetailAfterDateGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 8;
		calDetailAfterDateGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailAfterDateGroup.setLayoutData(gridData);
		calDetailAfterDateGroup.setText(Messages.getString("calendar.detail.before.after"));
		// 上記の日程からx日後
		label = new Label(calDetailAfterDateGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "afterdategroup", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		//上記の日程より
		label.setText(Messages.getString("calendar.detail.after.1") + "");
		//テキスト
		calDetailDaysLaterText = new Text(calDetailAfterDateGroup, SWT.BORDER | SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "dayslater", calDetailDaysLaterText);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailDaysLaterText.setLayoutData(gridData);
		calDetailDaysLaterText.setTextLimit(8);
		calDetailDaysLaterText.setText("0");
		calDetailDaysLaterText.setToolTipText(Messages.getString("calendar.detail.notes") + "");
		calDetailDaysLaterText.addKeyListener(new NumberKeyListener());
		calDetailDaysLaterText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// 上記の日程からx日後
		label = new Label(calDetailAfterDateGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "afterdategroup", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		//日後"
		label.setText(Messages.getString("calendar.detail.after.2") + "");

		// 振り替え
		calDetailSubstituteGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "substitute", calDetailSubstituteGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 8;
		calDetailSubstituteGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailSubstituteGroup.setLayoutData(gridData);
		calDetailSubstituteGroup.setText(Messages.getString("calendar.detail.substitute"));
		// 振り替える
		calDetailSubstituteCheck = new Button(calDetailSubstituteGroup, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "substituteCheck", calDetailSubstituteCheck);
		calDetailSubstituteCheck.setText(Messages.getString("calendar.detail.substitute.check"));
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailSubstituteCheck.setLayoutData(gridData);
		calDetailSubstituteCheck.setSelection(false);
		calDetailSubstituteCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		//振り替え間隔
		label = new Label(calDetailSubstituteGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("calendar.detail.substitute.time"));
		//振り替え間隔のテキスト
		calDetailSubstituteTime = new Text(calDetailSubstituteGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "substituteTime", calDetailSubstituteTime);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailSubstituteTime.setEnabled(false);
		calDetailSubstituteTime.setLayoutData(gridData);
		calDetailSubstituteTime.setTextLimit(5); // 24h*366day=8784、マイナス許容の5桁
		calDetailSubstituteTime.setText("24");
		calDetailSubstituteTime.addKeyListener(new NumberKeyListener());
		calDetailSubstituteTime.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// 時間ラベル
		label = new Label(calDetailSubstituteGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "substituteTimePeriod", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		// 時間
		label.setText(Messages.getString("time.period") + "");
		//振り替え上限
		label = new Label(calDetailSubstituteGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("calendar.detail.substitute.limit"));
		//振り替え間隔のテキスト
		calDetailSubstituteLimit = new Text(calDetailSubstituteGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "transferupper", calDetailSubstituteLimit);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailSubstituteLimit.setEnabled(false);
		calDetailSubstituteLimit.setLayoutData(gridData);
		calDetailSubstituteLimit.setTextLimit(2);
		calDetailSubstituteLimit.setText("10");
		calDetailSubstituteLimit.addKeyListener(new NumberKeyListener());
		calDetailSubstituteLimit.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 時間設定グループ
		 */
		calDetailTimeGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "time", calDetailTimeGroup);
		layout = new GridLayout(1,true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 8;
		calDetailTimeGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailTimeGroup.setLayoutData(gridData);
		calDetailTimeGroup.setText(Messages.getString("time.period"));

		//時間
		// 開始時刻
		label = new Label(calDetailTimeGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "calDetailTimeGroup", calDetailTimeGroup);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("start") + Messages.getString("timestamp") + "");

		//テキスト
		calDetailTimeFromText = new Text(calDetailTimeGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "timefrom", calDetailTimeFromText);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailTimeFromText.setLayoutData(gridData);
		calDetailTimeFromText.setTextLimit(9);
		calDetailTimeFromText.setText("00:00:00");
		calDetailTimeFromText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// 空白
		label = new Label(calDetailTimeGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 終了時刻
		label = new Label(calDetailTimeGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "endtimestamp", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("end") + Messages.getString("timestamp") + "");
		//テキスト
		calDetailTimeToText = new Text(calDetailTimeGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "timeto", calDetailTimeToText);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailTimeToText.setLayoutData(gridData);
		calDetailTimeToText.setTextLimit(9);
		calDetailTimeToText.setText("24:00:00");
		calDetailTimeToText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * カレンダ稼動非稼動設定グループ
		 */
		calDetailOptGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "opt", calDetailOptGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 6;
		calDetailOptGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailOptGroup.setLayoutData(gridData);
		calDetailOptGroup.setText(Messages.getString("calendar.detail.operation.3"));
		//稼動ボタン
		this.calDetailOptOnRadio = new Button(calDetailOptGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "opton", calDetailOptOnRadio);
		this.calDetailOptOnRadio.setText(Messages.getString("calendar.detail.operation.1"));
		this.calDetailOptOnRadio.setSelection(true);
		gridDataRadio = new GridData();
		gridDataRadio.horizontalSpan = 2;
		gridDataRadio.horizontalAlignment = GridData.FILL;
		gridDataRadio.grabExcessHorizontalSpace = true;
		this.calDetailOptOnRadio.setLayoutData(gridDataRadio);
		this.calDetailOptOnRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		//非稼動ボタン
		this.calDetailOptOffRadio =  new Button(calDetailOptGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "offradio", calDetailOptOffRadio);
		this.calDetailOptOffRadio.setText(Messages.getString("calendar.detail.operation.2"));
		gridDataRadio = new GridData();
		gridDataRadio.horizontalSpan = 2;
		gridDataRadio.horizontalAlignment = GridData.FILL;
		gridDataRadio.grabExcessHorizontalSpace = true;
		this.calDetailOptOffRadio.setLayoutData(gridDataRadio);
		this.calDetailOptOffRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		shell.pack();
		// 画面中央に
		Display calDetailDisplay = shell.getDisplay();
		shell.setLocation((calDetailDisplay.getBounds().width - shell.getSize().x) / 2,
				(calDetailDisplay.getBounds().height - shell.getSize().y) / 2);

		// 必須入力項目を可視化
		this.update();
		this.reflectCalendar();
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示

		//ラジオボタン「毎年」を選択
		if(calDetailEveryYearRadio.getSelection()){
			calDetailYearText.setEnabled(false);
		}else {//ラジオボタン「指定」を選択
			calDetailYearText.setEnabled(true);
		}
		//ラジオボタン「すべての日」を選択
		if(calDetailAllDayRadio.getSelection()){
			calDetailDayOfWeekInMonthCombo.setEnabled(false);
			calDetailDayOfWeekCombo.setEnabled(false);
			calDetailDayGroup.setEnabled(false);
			calDetailDaysLaterText.setEnabled(false);
		}else{
			calDetailDayOfWeekInMonthCombo.setEnabled(true);
			calDetailDayOfWeekCombo.setEnabled(true);
			calDetailDayGroup.setEnabled(true);
			calDetailCalPatternCombo.setEnabled(true);
			calDetailDaysLaterText.setEnabled(true);
		}
		//ラジオボタン「曜日」を選択
		if(calDetailDayOfWeekRadio.getSelection()){
			calDetailDayOfWeekInMonthCombo.setEnabled(true);
			calDetailDayOfWeekCombo.setEnabled(true);
			calDetailDayGroup.setEnabled(false);
			calDetailCalPatternCombo.setEnabled(false);
		}else{
			calDetailDayOfWeekInMonthCombo.setEnabled(false);
			calDetailDayOfWeekCombo.setEnabled(false);
		}
		//ラジオボタン「日」を選択
		if(calDetailDateRadio.getSelection()){
			calDetailDayOfWeekInMonthCombo.setEnabled(false);
			calDetailDayOfWeekCombo.setEnabled(false);
			calDetailDayGroup.setEnabled(true);
			calDetailCalPatternCombo.setEnabled(false);
		}else{
			calDetailDayGroup.setEnabled(false);
		}
		//ラジオボタン「カレンダパターン」を選択
		if(calDetailCalPatternRadio.getSelection()){
			calDetailDayOfWeekInMonthCombo.setEnabled(false);
			calDetailDayOfWeekCombo.setEnabled(false);
			calDetailDayGroup.setEnabled(false);
			calDetailCalPatternCombo.setEnabled(true);
		}else{
			calDetailCalPatternCombo.setEnabled(false);
		}
		//テキスト 指定年
		if(calDetailYearText.getEnabled() && ("").equals(calDetailYearText.getText())){
			calDetailYearText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			calDetailYearText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//コンボボックス 第x週
		if(calDetailDayOfWeekInMonthCombo.getEnabled() && ("").equals(calDetailDayOfWeekInMonthCombo.getText())){
			calDetailDayOfWeekInMonthCombo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			calDetailDayOfWeekInMonthCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//コンボボックス 曜日
		if(calDetailDayOfWeekCombo.getEnabled() && ("").equals(calDetailDayOfWeekCombo.getText())){
			calDetailDayOfWeekCombo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			calDetailDayOfWeekCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//コンボボックス 日
		if(calDetailDayGroup.getEnabled() && ("").equals(calDetailDayGroup.getText())){
			calDetailDayGroup.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			calDetailDayGroup.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//コンボボックス カレンダパターン
		if(calDetailCalPatternCombo.getEnabled() && ("").equals(calDetailCalPatternCombo.getText())){
			calDetailCalPatternCombo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			calDetailCalPatternCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//テキスト 開始時間
		if(("").equals(calDetailTimeFromText.getText())){
			calDetailTimeFromText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			calDetailTimeFromText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//テキスト 終了時間
		if(("").equals(calDetailTimeToText.getText())){
			calDetailTimeToText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			calDetailTimeToText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		
		// 振り替えグループ
		if (calDetailOptOnRadio.getSelection() && !calDetailOptOffRadio.getSelection()) {
			calDetailSubstituteCheck.setEnabled(true);
			// 振り替えるチェックボックス
			if (calDetailSubstituteCheck.getSelection()) {
				calDetailSubstituteTime.setEnabled(true);
				calDetailSubstituteLimit.setEnabled(true);
			} else {
				calDetailSubstituteTime.setEnabled(false);
				calDetailSubstituteLimit.setEnabled(false);
			}
		}
		if (calDetailOptOffRadio.getSelection() && !calDetailOptOnRadio.getSelection()) {
			calDetailSubstituteCheck.setEnabled(false);
			calDetailSubstituteTime.setEnabled(false);
			calDetailSubstituteLimit.setEnabled(false);
		}
		
		// テキスト 振り替え間隔
		if (("").equals(calDetailSubstituteTime.getText())) {
			calDetailSubstituteTime.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			calDetailSubstituteTime.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// テキスト 振り替え上限
		if (("").equals(calDetailSubstituteLimit.getText())) {
			calDetailSubstituteLimit.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			calDetailSubstituteLimit.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ダイアログの情報からカレンダ情報を作成します。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see
	 */
	private CalendarDetailInfo createCalendarInfo() {
		/** 開始時間*/
		Long timeFrom = null;
		/** 終了時間*/
		Long timeTo = null;

		inputData = new CalendarDetailInfo();
		/*
		 * 説明
		 */
		if(calDetailDescriptionText.getText().length() > 0){
			this.inputData.setDescription(calDetailDescriptionText.getText());
		}

		/*
		 * 年情報 取得
		 */
		if(calDetailEveryYearRadio.getSelection()){
			//0 は毎年
			this.inputData.setYear(0);
		}else {
			if(calDetailYearText.getText() != null && calDetailYearText.getText().length() > 0){
				try {
					this.inputData.setYear(Integer.parseInt(calDetailYearText.getText()));
				}catch (NumberFormatException e) {
					String[] args = {"[ " +  Messages.getString("year") + " ]"};
					this.setValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.calendar.51",args));
					return null;
				}
			}else {
				String[] args = {"[ " +  Messages.getString("year") + " ]"};
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.calendar.15",args));
				return null;
			}
		}
		/*
		 * 月グループ情報 取得
		 */
		if(calDetailMonthCombo.getText() != null && calDetailMonthCombo.getText().length() > 0){
			this.inputData.setMonth(MonthConstant.stringToType(calDetailMonthCombo.getText()));
		} else {
			String[] args = {"[ " +  Messages.getString("month") + " ]"};
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.calendar.15",args));
			return null;
		}

		/*
		 * 日グループ情報取得
		 *
		 * 曜日選択ラジオボタン
		 * 0 : 全ての日
		 * 1 : 曜日
		 * 2 : 日
		 * 3 : その他
		 */
		//すべての日
		if(calDetailAllDayRadio.getSelection()){
			this.inputData.setDayType(0);
		}
		//曜日
		else if(calDetailDayOfWeekRadio.getSelection()){
			//第x週テキスト
			if(calDetailDayOfWeekInMonthCombo.getText() != null
					&& calDetailDayOfWeekInMonthCombo.getText().length() > 0){
				this.inputData.setDayOfWeekInMonth(
						DayOfWeekInMonthConstant.stringToType(calDetailDayOfWeekInMonthCombo.getText()));
			}else {
				String[] args = {"[ " +  Messages.getString("calendar.detail.xth") + " ]"};
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.calendar.15",args));
				return null;
			}
			//曜日テキスト
			if(calDetailDayOfWeekCombo.getText() != null &&
					calDetailDayOfWeekCombo.getText().length() > 0){
				this.inputData.setDayOfWeek(DayOfWeekConstant.stringToType(calDetailDayOfWeekCombo.getText()));
			}else {
				String[] args = {"[ " +  Messages.getString("weekday") + " ]"};
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.calendar.15",args));
				return null;
			}
			this.inputData.setDayType(1);
		}
		//日
		else if(calDetailDateRadio.getSelection()){
			//日テキスト
			if(calDetailDayGroup.getText() != null
					&& calDetailDayGroup.getText().length() > 0){
				this.inputData.setDate(Integer.parseInt(calDetailDayGroup.getText()));
			}else {
				String[] args = {"[ " +  Messages.getString("monthday") + " ]"};
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.calendar.15",args));
				return null;
			}
			this.inputData.setDayType(2);
		}
		//カレンダパターン
		else if(calDetailCalPatternRadio.getSelection()){
			//カレンダパターンテキスト
			if(calDetailCalPatternCombo.getText() != null && calDetailCalPatternCombo.getText().length() > 0){
				this.inputData.setCalPatternId(getCalPatternId(calDetailCalPatternCombo.getText()));
			}else {
				String[] args = {"[ " +  Messages.getString("calendar.pattern") + " ]"};
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.calendar.15",args));
				return null;
			}
			this.inputData.setDayType(3);
		}
		//ラジオボタンで、いずれか選択状態にあるはずだが、念のため...未選択のときアラート
		else {
			String[] args = {"[ " +  Messages.getString("calendar.detail.date.type") + " ]"};
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.calendar.15",args));
			return null;
		}
		//前後日
		if(calDetailDaysLaterText.getText().length() > 0){
			int daysLater = 0;
			try {
				daysLater = Integer.parseInt(calDetailDaysLaterText.getText());
			} catch (Exception e) {
				String[] args = {"[ " +  Messages.getString("calendar.detail.before.after") + " ]"};
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.calendar.53",args));
				return null;
			}
			if (-32768 > daysLater || daysLater > 32767) {
				String[] args = {Messages.getString("calendar.detail.before.after"),
						"-32768", "32767"};
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.calendar.52",args));
				return null;
			}
			this.inputData.setAfterday(daysLater);
		}

		// 振り替え間隔
		// 稼動・非稼動の稼動の場合のみ振り替えを実施する
		Integer substituteDateInt = 0;
		int SUBSTITUTE_DATE_INT_MAX = 24 * 366; // 24h*366day(1year)
		int SUBSTITUTE_DATE_INT_MIN = SUBSTITUTE_DATE_INT_MAX * -1;
		String checkParam = "";
		try {
			this.inputData.setSubstituteFlg(calDetailSubstituteCheck.getSelection());
			// 振り替えがONの場合のみ、振り替え時間と振り替え間隔をチェックして格納する
			checkParam = Messages.getString("calendar.detail.substitute.time");
			substituteDateInt = Integer.parseInt(calDetailSubstituteTime.getText());
			if (substituteDateInt == 0 || substituteDateInt < SUBSTITUTE_DATE_INT_MIN || substituteDateInt > SUBSTITUTE_DATE_INT_MAX) {
				String[] args = {Messages.getString("calendar.detail.substitute.time"), "0",
						String.valueOf(SUBSTITUTE_DATE_INT_MIN), String.valueOf(SUBSTITUTE_DATE_INT_MAX)};
				this.setValidateResult(Messages.getString("message.hinemos.1"), Messages.getString("message.calendar.54",args));
				return null;
			}
			checkParam = Messages.getString("calendar.detail.substitute.limit");
			int limit = Integer.parseInt(calDetailSubstituteLimit.getText());
			if (limit < 1 || limit > 99) {
				String[] args = {Messages.getString("calendar.detail.substitute.limit"), "1", "99"};
				this.setValidateResult(Messages.getString("message.hinemos.1"), Messages.getString("message.calendar.52",args));
				return null;
			}
			this.inputData.setSubstituteTime(substituteDateInt);
			this.inputData.setSubstituteLimit(Integer.valueOf(calDetailSubstituteLimit.getText()));
		} catch (Exception e) {
			String[] args = {checkParam};
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.calendar.15",args));
			return null;
		}
		
		//開始時間、終了時間
		Date dateTimeFrom = null;
		Date dateTimeTo = null;
		
		try {
			//0時未満および24時(及び48時)超の文字列指定にも対応する
			dateTimeFrom = TimeStringConverter.parseTime(calDetailTimeFromText.getText());
			dateTimeTo = TimeStringConverter.parseTime(calDetailTimeToText.getText());
			
			timeFrom = dateTimeFrom.getTime();
			timeTo = dateTimeTo.getTime();
			if(timeFrom >= timeTo){
				String[] args = {"[ " +  Messages.getString("end")
						+ Messages.getString("time") + " ]",
						"[ " +  Messages.getString("start")
						+ Messages.getString("time") + " ]"};
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.calendar.31",args));
				return null;
			}
			else {
				this.inputData.setTimeFrom(timeFrom);
				this.inputData.setTimeTo(timeTo);
			}
		} catch (ParseException e) {
			//「HH:mm:ss」形式に変換できないものが入力値として与えられた場合
			String tmp = "";
			if (dateTimeFrom == null) {
				tmp = "start";
			}
			else {
				tmp = "end";
			}
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					(Messages.getString(tmp) + Messages.getString("message.hinemos.6")));
			//e.printStackTrace();
			return null;
		}
		//稼動/非稼動取得
		if (calDetailOptOnRadio.getSelection()) {
			this.inputData.setOperateFlg(true);
		} else {
			this.inputData.setOperateFlg(false);
		}
		return inputData;
	}
	
	/**
	 * ダイアログにカレンダ情報を反映します。
	 *
	 * @param calInfo
	 *            設定値として用いる監視情報
	 */
	private void reflectCalendar() {
		// 初期表示
		CalendarDetailInfo detailInfo = null;
		if(this.inputData != null){
			// 変更の場合、情報取得
			detailInfo = this.inputData;
			//ここで、getSelection() firstElementを取得する
		}
		else{
			// 作成の場合
			detailInfo = new CalendarDetailInfo();
		}
		//カレンダ詳細情報取得
		if(detailInfo != null){
			//説明
			if(detailInfo.getDescription() != null){
				this.calDetailDescriptionText.setText(detailInfo.getDescription());
			}
			//年
			if(detailInfo.getYear() != null){
				if(detailInfo.getYear() == 0){
					this.calDetailEveryYearRadio.setSelection(true);
					this.calDetailSpecifyYearRadio.setSelection(false);
				}else {
					this.calDetailSpecifyYearRadio.setSelection(true);
					this.calDetailEveryYearRadio.setSelection(false);
					this.calDetailYearText.setText(String.valueOf(detailInfo.getYear()));
				}
			}
			//月
			if (detailInfo.getMonth() != null) {
				this.calDetailMonthCombo.setText(MonthConstant.typeToString(detailInfo.getMonth()));
			}
			//日
			if(detailInfo.getDayType() != null){
				//すべての日
				if(detailInfo.getDayType() == 0){
					this.calDetailAllDayRadio.setSelection(true);
					this.calDetailDayOfWeekRadio.setSelection(false);
					this.calDetailDateRadio.setSelection(false);
					this.calDetailCalPatternRadio.setSelection(false);
				}
				//曜日
				else if (detailInfo.getDayType() == 1){
					this.calDetailAllDayRadio.setSelection(false);
					this.calDetailDayOfWeekRadio.setSelection(true);
					this.calDetailDateRadio.setSelection(false);
					this.calDetailCalPatternRadio.setSelection(false);
				}
				//日
				else if (detailInfo.getDayType() == 2){
					this.calDetailAllDayRadio.setSelection(false);
					this.calDetailDayOfWeekRadio.setSelection(false);
					this.calDetailDateRadio.setSelection(true);
					this.calDetailCalPatternRadio.setSelection(false);
				}
				//その他
				else if (detailInfo.getDayType() == 3){
					this.calDetailAllDayRadio.setSelection(false);
					this.calDetailDayOfWeekRadio.setSelection(false);
					this.calDetailDateRadio.setSelection(false);
					this.calDetailCalPatternRadio.setSelection(true);
				}else {
					//FIXME 出力メッセージ要追加
					m_log.error("Error Of DayType[" + detailInfo.getDayType() + "]");
				}
				//第x週
				if(detailInfo.getDayOfWeekInMonth() != null){
					//取得した数値を文字列に変換
					String str = DayOfWeekInMonthConstant.typeToString(detailInfo.getDayOfWeekInMonth());
					this.calDetailDayOfWeekInMonthCombo.setText(str);
				}
				//曜日
				if(detailInfo.getDayOfWeek() != null){
					String str = DayOfWeekConstant.typeToString(detailInfo.getDayOfWeek());
					this.calDetailDayOfWeekCombo.setText(str);
				}
				//日
				if(detailInfo.getDate() != null){
					this.calDetailDayGroup.setText(String.valueOf(detailInfo.getDate()));
				}
				//カレンダパターン
				if(detailInfo.getCalPatternId() != null){
					this.calDetailCalPatternCombo.setText(calPatternMap.get(detailInfo.getCalPatternId()));
				}
				//上記の日程からx日後
				if(detailInfo.getAfterday() != null){
					this.calDetailDaysLaterText.setText(String.valueOf(detailInfo.getAfterday()));
				}
			}
			// 振り替え
			if (detailInfo.isSubstituteFlg() != null) {
				this.calDetailSubstituteCheck.setSelection(detailInfo.isSubstituteFlg());
				this.calDetailSubstituteTime.setText(String.valueOf(detailInfo.getSubstituteTime()));
				this.calDetailSubstituteLimit.setText(String.valueOf(detailInfo.getSubstituteLimit()));
			}
			/**
			 * 時間
			 * 開始時間、終了時間
			 * 24:00:00は、00:00:00と表示される
			 * そのため、変換処理を行う。
			 */
			//表示形式を0時未満および24時(及び48時)超にも対応するよう変換する
			// 変換例
			// 48:00～：50:00を02:00と表示されないよう48加算する
			// 24:00～：26:00を02:00と表示されないよう24加算する
			// 00:00～：変換不要
			// 0時未満の場合
			// 前日の23:45は-00:15と表示する
			// 前々日の22:00は-26:00と表示する
			if(detailInfo.getTimeFrom() != null){
				this.calDetailTimeFromText.setText(TimeStringConverter.formatTime(new Date(detailInfo.getTimeFrom())));
			}
			if(detailInfo.getTimeTo() != null){
				this.calDetailTimeToText.setText(TimeStringConverter.formatTime(new Date(detailInfo.getTimeTo())));
			}
			//稼動/非稼動取得
			if (detailInfo.isOperateFlg() != null) {
				if(detailInfo.isOperateFlg()){
					calDetailOptOnRadio.setSelection(true);
					calDetailOptOffRadio.setSelection(false);
				} else {
					calDetailOptOnRadio.setSelection(false);
					calDetailOptOffRadio.setSelection(true);
				}
			}
		}
		// カレンダIDが必須項目であることを明示
		this.update();
	}
	/**
	 * 入力値をCalendarDetailInfoListに登録します。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		createCalendarInfo();
		return true;
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		// 入力値生成
		this.inputData = this.createCalendarInfo();
		if (this.inputData != null) {
			return super.validate();
		} else {
			return m_validateResult;
		}
	}
	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 */
	protected void setValidateResult(String id, String message) {

		this.m_validateResult = new ValidateResult();
		this.m_validateResult.setValid(false);
		this.m_validateResult.setID(id);
		this.m_validateResult.setMessage(message);
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * カレンダ詳細ダイアログ
	 * その他項目を取得する。
	 *
	 * @param managerName マネージャ名
	 * @param ownerRoleId オーナーロールID
	 * @return
	 */
	private List<CalendarPatternInfo> getCalendarPatternList(String managerNane, String ownerRoleId){
		//カレンダ詳細ダイアログカレンダパターン項目
		List<CalendarPatternInfo> calPatternList = null;
		//カレンダパターン情報取得
		try {
			CalendarEndpointWrapper wrapper = CalendarEndpointWrapper.getWrapper(managerNane);
			calPatternList = wrapper.getCalendarPatternList(ownerRoleId);
		} catch (InvalidRole_Exception e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			return null;
		}
		return calPatternList;
	}

	/**
	 * カレンダパターンのカレンダパターンIDを返す。
	 * @param name カレンダパターン名
	 * @return カレンダパターンID
	 */
	private String getCalPatternId(String name) {
		if (calPatternMap.containsValue(name)) {
			for (Map.Entry<String, String> entry : calPatternMap.entrySet()) {
				if (entry.getValue().equals(name)) {
					return entry.getKey();
				}
			}
		}
		return "";
	}
}
