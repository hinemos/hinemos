/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobKickResponse;

import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.ComboModifyListener;
import com.clustercontrol.composite.action.NumberKeyListener;
import com.clustercontrol.dialog.DateTimeDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.SessionPremakeEveryXHourEnum;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;

/**
 * ジョブ実行契機の作成・変更ダイアログのジョブセッション事前生成タブ用の
 * コンポジットクラスです。
 *
 */
public class JobKickSessionPremakeComposite extends Composite {

	/** shell */
	private Shell m_shell;

	/** ジョブセッションの事前生成を有効にする：チェックボックス */
	private Button m_btnSessionPremake = null;

	/** 事前生成スケジュール：毎日：ラジオボタン */
	private Button m_btnScheduleType_EveryDay = null;
	/** 事前生成スケジュール：毎日：時コンボボックス */
	private Combo m_cmbHour_EveryDay = null;
	/** 事前生成スケジュール：毎日：分コンボボックス */
	private Combo m_cmbMinute_EveryDay = null;

	/** 事前生成スケジュール：毎週：ラジオボタン */
	private Button m_btnScheduleType_EveryWeek = null;
	/** 事前生成スケジュール：毎週：曜日コンボボックス */
	private Combo m_cmbWeek_EveryWeek = null;
	/** 事前生成スケジュール：毎週：時コンボボックス */
	private Combo m_cmbHour_EveryWeek = null;
	/** 事前生成スケジュール：毎週：分コンボボックス */
	private Combo m_cmbMinute_EveryWeek = null;

	/** 事前生成スケジュール：時間：ラジオボタン */
	private Button m_btnScheduleType_Time = null;
	/** 事前生成スケジュール：時間：時Fromラジオボタン */
	private Combo m_cmbHour_Time = null;
	/** 事前生成スケジュール：時間：分Fromラジオボタン */
	private Combo m_cmbMinute_Time = null;
	/** 事前生成スケジュール：時間：時Everyラジオボタン */
	private Combo m_cmbEveryXHour_Time = null;
	/** 事前生成スケジュール：時間：生成時間ラベル */
	private Label m_lblMakeHour_Time = null;

	/** 事前生成スケジュール：日時：ラジオボタン */
	private Button m_btnScheduleType_Datetime = null;
	/** 事前生成スケジュール：日時：日時テキスト */
	private Text m_txtDate_Datetime = null;
	/** 事前生成スケジュール：日時：日時ボタン */
	private Button m_btnDate_Datetime = null;

	/** 事前生成スケジュール：日時：日時Toテキスト */
	private Text m_txtToDate_Datetime = null;
	/** 事前生成スケジュール：日時：日時Toボタン */
	private Button m_btnToDate_Datetime = null;

	/** 事前生成が完了したらINTERNALイベントを出力する：チェックボックス */
	private Button m_btnInternal = null;

	/** スケジュール設定Composite */
	private JobKickScheduleComposite m_scheduleComposite = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親コンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public JobKickSessionPremakeComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		m_shell = this.getShell();

		Label label = null;
		GridData gridData = null;
		DecimalFormat format = new DecimalFormat("00");

		this.setLayout(JobDialogUtil.getParentLayout());

		// Composite
		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new RowLayout());
		composite.setLayoutData(new RowData());
		((RowData)composite.getLayoutData()).width = 740;

		// ジョブセッションの事前生成を有効にする（チェックボックス）
		this.m_btnSessionPremake = new Button(composite, SWT.CHECK);
		this.m_btnSessionPremake.setLayoutData(new RowData(300, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnSessionPremake.setText(Messages.getString("job.sessionpremake.enable"));
		this.m_btnSessionPremake.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		this.m_btnSessionPremake.setSelection(false);

		// 事前生成（Composite）
		Composite preGenerateComposite = new Composite(composite, SWT.BORDER);
		preGenerateComposite.setLayout(new GridLayout(1, true));

		// 事前生成 (Group)
		Group scheduleGroup = new Group(preGenerateComposite, SWT.NONE);
		scheduleGroup.setText(Messages.getString("job.sessionpremake.schedule"));
		scheduleGroup.setLayout(new GridLayout(21, false));

		// 毎日（ラジオボタン）
		this.m_btnScheduleType_EveryDay = new Button(scheduleGroup, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_btnScheduleType_EveryDay.setLayoutData(gridData);
		this.m_btnScheduleType_EveryDay.setText(Messages.getString("everyday"));
		this.m_btnScheduleType_EveryDay.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		this.m_btnScheduleType_EveryDay.setSelection(true);

		// 毎日：時（コンボボックス）
		this.m_cmbHour_EveryDay = new Combo(scheduleGroup, SWT.READ_ONLY | SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbHour_EveryDay.setLayoutData(gridData);
		this.m_cmbHour_EveryDay.setTextLimit(2);
		this.m_cmbHour_EveryDay.setVisibleItemCount(10);
		this.m_cmbHour_EveryDay.addKeyListener(new NumberKeyListener());
		this.m_cmbHour_EveryDay.addModifyListener(new ComboModifyListener());
		for (int hour = 0; hour < 24; hour++) {
			this.m_cmbHour_EveryDay.add(format.format(hour));
		}
		this.m_cmbHour_EveryDay.select(0);
		this.m_cmbHour_EveryDay.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 毎日：時（ラベル）
		label = new Label(scheduleGroup, SWT.NONE);
		label.setText(Messages.getString("hr"));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 毎日：分（コンボボックス）
		this.m_cmbMinute_EveryDay = new Combo(scheduleGroup, SWT.READ_ONLY | SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbMinute_EveryDay.setLayoutData(gridData);
		this.m_cmbMinute_EveryDay.setTextLimit(2);
		this.m_cmbMinute_EveryDay.setVisibleItemCount(10);
		this.m_cmbMinute_EveryDay.addKeyListener(new NumberKeyListener());
		this.m_cmbMinute_EveryDay.addModifyListener(new ComboModifyListener());
		for (int minutes = 0; minutes < 60; minutes++) {
			this.m_cmbMinute_EveryDay.add(format.format(minutes));
		}
		this.m_cmbMinute_EveryDay.select(0);
		this.m_cmbMinute_EveryDay.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 毎日：分（ラベル）
		label = new Label(scheduleGroup, SWT.NONE);
		label.setText(Messages.getString("min"));
		gridData = new GridData();
		gridData.horizontalSpan = 13;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		new Label(scheduleGroup, SWT.NONE);
		new Label(scheduleGroup, SWT.NONE);

		// 毎日：説明（ラベル）
		label = new Label(scheduleGroup, SWT.NONE);
		label.setText(Messages.getString("job.sessionpremake.everyday.description"));
		gridData = new GridData();
		gridData.horizontalSpan = 19;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 毎週（ラジオボタン）
		this.m_btnScheduleType_EveryWeek = new Button(scheduleGroup, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_btnScheduleType_EveryWeek.setLayoutData(gridData);
		this.m_btnScheduleType_EveryWeek.setText(Messages.getString("schedule.everyweek"));
		this.m_btnScheduleType_EveryWeek.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 毎週：曜日（コンボボックス）
		this.m_cmbWeek_EveryWeek = new Combo(scheduleGroup, SWT.READ_ONLY | SWT.CENTER);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbWeek_EveryWeek.setLayoutData(gridData);
		this.m_cmbWeek_EveryWeek.setTextLimit(3);
		this.m_cmbWeek_EveryWeek.setVisibleItemCount(10);
		this.m_cmbWeek_EveryWeek.add(DayOfWeekConstant.STRING_SUNDAY);
		this.m_cmbWeek_EveryWeek.add(DayOfWeekConstant.STRING_MONDAY);
		this.m_cmbWeek_EveryWeek.add(DayOfWeekConstant.STRING_TUESDAY);
		this.m_cmbWeek_EveryWeek.add(DayOfWeekConstant.STRING_WEDNESDAY);
		this.m_cmbWeek_EveryWeek.add(DayOfWeekConstant.STRING_THURSDAY);
		this.m_cmbWeek_EveryWeek.add(DayOfWeekConstant.STRING_FRIDAY);
		this.m_cmbWeek_EveryWeek.add(DayOfWeekConstant.STRING_SATURDAY);
		this.m_cmbWeek_EveryWeek.select(0);
		this.m_cmbWeek_EveryWeek.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		new Label(scheduleGroup, SWT.NONE);

		// 毎週：時（コンボボックス）
		this.m_cmbHour_EveryWeek = new Combo(scheduleGroup, SWT.READ_ONLY | SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbHour_EveryWeek.setLayoutData(gridData);
		this.m_cmbHour_EveryWeek.setTextLimit(2);
		this.m_cmbHour_EveryWeek.setVisibleItemCount(10);
		this.m_cmbHour_EveryWeek.addKeyListener(new NumberKeyListener());
		this.m_cmbHour_EveryWeek.addModifyListener(new ComboModifyListener());
		for (int hour = 0; hour < 24; hour++) {
			this.m_cmbHour_EveryWeek.add(format.format(hour));
		}
		this.m_cmbHour_EveryWeek.select(0);
		this.m_cmbHour_EveryWeek.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 毎週：時（ラベル）
		label = new Label(scheduleGroup, SWT.NONE);
		label.setText(Messages.getString("hr"));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 毎週：分（コンボボックス）
		this.m_cmbMinute_EveryWeek = new Combo(scheduleGroup, SWT.READ_ONLY | SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbMinute_EveryWeek.setLayoutData(gridData);
		this.m_cmbMinute_EveryWeek.setTextLimit(2);
		this.m_cmbMinute_EveryWeek.setVisibleItemCount(10);
		this.m_cmbMinute_EveryWeek.addKeyListener(new NumberKeyListener());
		this.m_cmbMinute_EveryWeek.addModifyListener(new ComboModifyListener());
		for (int minutes = 0; minutes < 60; minutes++) {
			this.m_cmbMinute_EveryWeek.add(format.format(minutes));
		}
		this.m_cmbMinute_EveryWeek.select(0);
		this.m_cmbMinute_EveryWeek.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 毎週：分（ラベル）
		label = new Label(scheduleGroup, SWT.NONE);
		label.setText(Messages.getString("min"));
		gridData = new GridData();
		gridData.horizontalSpan = 9;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		new Label(scheduleGroup, SWT.NONE);
		new Label(scheduleGroup, SWT.NONE);

		// 毎週：説明（ラベル）
		label = new Label(scheduleGroup, SWT.NONE);
		label.setText(Messages.getString("job.sessionpremake.everyweek.description"));
		gridData = new GridData();
		gridData.horizontalSpan = 19;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 時間（ラジオボタン）
		this.m_btnScheduleType_Time = new Button(scheduleGroup, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_btnScheduleType_Time.setLayoutData(gridData);
		this.m_btnScheduleType_Time.setText(Messages.getString("hour.period"));
		this.m_btnScheduleType_Time.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 時間：時From（コンボボックス）
		this.m_cmbHour_Time = new Combo(scheduleGroup, SWT.READ_ONLY | SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbHour_Time.setLayoutData(gridData);
		this.m_cmbHour_Time.setTextLimit(2);
		this.m_cmbHour_Time.setVisibleItemCount(10);
		this.m_cmbHour_Time.addKeyListener(new NumberKeyListener());
		this.m_cmbHour_Time.addModifyListener(new ComboModifyListener());
		for (int hour = 0; hour < 24; hour++) {
			this.m_cmbHour_Time.add(format.format(hour));
		}
		this.m_cmbHour_Time.select(0);
		this.m_cmbHour_Time.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 時間：時From（ラベル）
		label = new Label(scheduleGroup, SWT.NONE);
		label.setText(Messages.getString("hr"));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 時間：分From（コンボボックス）
		this.m_cmbMinute_Time = new Combo(scheduleGroup, SWT.READ_ONLY | SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbMinute_Time.setLayoutData(gridData);
		this.m_cmbMinute_Time.setTextLimit(2);
		this.m_cmbMinute_Time.setVisibleItemCount(10);
		this.m_cmbMinute_Time.addKeyListener(new NumberKeyListener());
		this.m_cmbMinute_Time.addModifyListener(new ComboModifyListener());
		for (int minutes = 0; minutes < 60; minutes++) {
			this.m_cmbMinute_Time.add(format.format(minutes));
		}
		this.m_cmbMinute_Time.select(0);
		this.m_cmbHour_Time.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 時間：分From（ラベル）
		label = new Label(scheduleGroup, SWT.NONE);
		label.setText(Messages.getString("schedule.min.start.time"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 時間：時Every（コンボボックス）
		this.m_cmbEveryXHour_Time = new Combo(scheduleGroup, SWT.READ_ONLY | SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbEveryXHour_Time.setLayoutData(gridData);
		this.m_cmbEveryXHour_Time.setTextLimit(2);
		this.m_cmbEveryXHour_Time.setVisibleItemCount(10);
		this.m_cmbEveryXHour_Time.addKeyListener(new NumberKeyListener());
		this.m_cmbEveryXHour_Time.addModifyListener(new ComboModifyListener());
		for (SessionPremakeEveryXHourEnum hour : SessionPremakeEveryXHourEnum.values()) {
			this.m_cmbEveryXHour_Time.add(hour.getCode().toString());
		}
		this.m_cmbEveryXHour_Time.select(0);
		this.m_cmbEveryXHour_Time.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				m_lblMakeHour_Time.setText(m_cmbEveryXHour_Time.getText()
						+ Messages.getString("job.sessionpremake.time.description"));
				update();
			}
		});

		// 時間：時Every（ラベル）
		label = new Label(scheduleGroup, SWT.NONE);
		label.setText(Messages.getString("job.sessionpremake.every.description"));
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		new Label(scheduleGroup, SWT.NONE);
		new Label(scheduleGroup, SWT.NONE);

		// 時間：説明（ラベル）
		this.m_lblMakeHour_Time = new Label(scheduleGroup, SWT.NONE);
		this.m_lblMakeHour_Time.setText(m_cmbEveryXHour_Time.getText()
				+ Messages.getString("job.sessionpremake.time.description"));
		gridData = new GridData();
		gridData.horizontalSpan = 19;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_lblMakeHour_Time.setLayoutData(gridData);

		// 日時（ラジオボタン）
		this.m_btnScheduleType_Datetime = new Button(scheduleGroup, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_btnScheduleType_Datetime.setLayoutData(gridData);
		this.m_btnScheduleType_Datetime.setText(Messages.getString("time"));
		this.m_btnScheduleType_Datetime.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 日時：日時
		Composite compDate_Datetime = new Composite(scheduleGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		compDate_Datetime.setLayoutData(gridData);
		compDate_Datetime.setLayout(new GridLayout(2, false));

		// 日時：日時（テキストボックス）
		m_txtDate_Datetime = new Text(compDate_Datetime, SWT.BORDER);
		gridData = new GridData();
		gridData.heightHint = SizeConstant.SIZE_TEXT_HEIGHT;
		gridData.widthHint = 135;
		m_txtDate_Datetime.setLayoutData(gridData);
		// 日時データの場合は、日時ダイアログからの入力しか受け付けない
		m_txtDate_Datetime.setEditable(false);

		// 日時：日時 (Button)
		m_btnDate_Datetime = new Button(compDate_Datetime, SWT.NONE);
		m_btnDate_Datetime.setText(Messages.getString("calendar.button"));
		gridData = new GridData();
		gridData.heightHint = SizeConstant.SIZE_BUTTON_HEIGHT;
		m_btnDate_Datetime.setLayoutData(gridData);
		m_btnDate_Datetime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DateTimeDialog dialog = new DateTimeDialog(m_shell);
				if (m_txtDate_Datetime.getText().length() > 0) {
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					Date date = null;
					try {
						date = sdf.parse(m_txtDate_Datetime.getText());
					} catch (ParseException e1) {
						// 何もしない
					}
					dialog.setDate(date);
				}
				if (dialog.open() == IDialogConstants.OK_ID) {
					// 取得した日時をLong型で保持
					// calTimeFrom = dialog.getDate().getTime();
					// ダイアログより取得した日時を"yyyy/MM/dd HH:mm:ss"の形式に変換
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					String tmp = sdf.format(dialog.getDate());
					m_txtDate_Datetime.setText(tmp);
					update();
				}
			}
		});

		// 日時：日時：説明（ラベル）
		label = new Label(scheduleGroup, SWT.NONE);
		label.setText(Messages.getString("job.sessionpremake.generatetime.description"));
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 日時：日時To
		Composite compToDate_Datetime = new Composite(scheduleGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		compToDate_Datetime.setLayoutData(gridData);
		compToDate_Datetime.setLayout(new GridLayout(2, false));

		// 日時：日時To（テキストボックス）
		m_txtToDate_Datetime = new Text(compToDate_Datetime, SWT.BORDER);
		gridData = new GridData();
		gridData.heightHint = SizeConstant.SIZE_TEXT_HEIGHT;
		gridData.widthHint = 135;
		m_txtToDate_Datetime.setLayoutData(gridData);
		// 日時データの場合は、日時ダイアログからの入力しか受け付けない
		m_txtToDate_Datetime.setEditable(false);

		// 日時：日時To (Button)
		m_btnToDate_Datetime = new Button(compToDate_Datetime, SWT.NONE);
		m_btnToDate_Datetime.setText(Messages.getString("calendar.button"));
		gridData = new GridData();
		gridData.heightHint = SizeConstant.SIZE_BUTTON_HEIGHT;
		m_btnToDate_Datetime.setLayoutData(gridData);
		m_btnToDate_Datetime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DateTimeDialog dialog = new DateTimeDialog(m_shell);
				if (m_txtToDate_Datetime.getText().length() > 0) {
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					Date date = null;
					try {
						date = sdf.parse(m_txtToDate_Datetime.getText());
					} catch (ParseException e1) {
						// 何もしない
					}
					dialog.setDate(date);
				}
				if (dialog.open() == IDialogConstants.OK_ID) {
					// 取得した日時をLong型で保持
					// calTimeFrom = dialog.getDate().getTime();
					// ダイアログより取得した日時を"yyyy/MM/dd HH:mm:ss"の形式に変換
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					String tmp = sdf.format(dialog.getDate());
					m_txtToDate_Datetime.setText(tmp);
					update();
				}
			}
		});

		// 日時：日時To：説明（ラベル）
		label = new Label(scheduleGroup, SWT.NONE);
		label.setText(Messages.getString("job.sessionpremake.until.description"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		new Label(scheduleGroup, SWT.NONE);
		new Label(scheduleGroup, SWT.NONE);

		// 日時：説明（ラベル）
		label = new Label(scheduleGroup, SWT.NONE);
		label.setText(Messages.getString("job.sessionpremake.datetime.description"));
		gridData = new GridData();
		gridData.horizontalSpan = 19;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 事前生成が完了したらINTERNALイベントを出力する（チェックボックス）
		this.m_btnInternal = new Button(preGenerateComposite, SWT.CHECK);
		this.m_btnInternal.setLayoutData(new GridData(350, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnInternal.setText(Messages.getString("job.sessionpremake.internal.notice"));
		this.m_btnInternal.setSelection(true);

		// 初期設定
		update();
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目を明示
		boolean enabled = this.m_btnSessionPremake.getSelection();
		this.m_btnScheduleType_EveryDay.setEnabled(enabled);
		this.m_cmbHour_EveryDay.setEnabled(enabled && this.m_btnScheduleType_EveryDay.getSelection());
		this.m_cmbMinute_EveryDay.setEnabled(enabled && this.m_btnScheduleType_EveryDay.getSelection());
		this.m_btnScheduleType_EveryWeek.setEnabled(enabled);
		this.m_cmbWeek_EveryWeek.setEnabled(enabled && this.m_btnScheduleType_EveryWeek.getSelection());
		this.m_cmbHour_EveryWeek.setEnabled(enabled && this.m_btnScheduleType_EveryWeek.getSelection());
		this.m_cmbMinute_EveryWeek.setEnabled(enabled && this.m_btnScheduleType_EveryWeek.getSelection());
		this.m_btnScheduleType_Time.setEnabled(enabled);
		this.m_cmbHour_Time.setEnabled(enabled && this.m_btnScheduleType_Time.getSelection());
		this.m_cmbMinute_Time.setEnabled(enabled && this.m_btnScheduleType_Time.getSelection());
		this.m_cmbEveryXHour_Time.setEnabled(enabled && this.m_btnScheduleType_Time.getSelection());
		this.m_btnScheduleType_Datetime.setEnabled(enabled);
		this.m_txtDate_Datetime.setEnabled(enabled && this.m_btnScheduleType_Datetime.getSelection());
		this.m_btnDate_Datetime.setEnabled(enabled && this.m_btnScheduleType_Datetime.getSelection());
		this.m_txtToDate_Datetime.setEnabled(enabled && this.m_btnScheduleType_Datetime.getSelection());
		this.m_btnToDate_Datetime.setEnabled(enabled && this.m_btnScheduleType_Datetime.getSelection());
		this.m_btnInternal.setEnabled(enabled);

		if(this.m_cmbHour_EveryDay.getEnabled() && "".equals(this.m_cmbHour_EveryDay.getText())){
			this.m_cmbHour_EveryDay.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_cmbHour_EveryDay.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_cmbMinute_EveryDay.getEnabled() && "".equals(this.m_cmbMinute_EveryDay.getText())){
			this.m_cmbMinute_EveryDay.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_cmbMinute_EveryDay.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_cmbWeek_EveryWeek.getEnabled() && "".equals(this.m_cmbWeek_EveryWeek.getText())){
			this.m_cmbWeek_EveryWeek.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_cmbWeek_EveryWeek.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_cmbHour_EveryWeek.getEnabled() && "".equals(this.m_cmbHour_EveryWeek.getText())){
			this.m_cmbHour_EveryWeek.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_cmbHour_EveryWeek.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_cmbMinute_EveryWeek.getEnabled() && "".equals(this.m_cmbMinute_EveryWeek.getText())){
			this.m_cmbMinute_EveryWeek.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_cmbMinute_EveryWeek.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_cmbHour_Time.getEnabled() && "".equals(this.m_cmbHour_Time.getText())){
			this.m_cmbHour_Time.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_cmbHour_Time.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_cmbMinute_Time.getEnabled() && "".equals(this.m_cmbMinute_Time.getText())){
			this.m_cmbMinute_Time.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_cmbMinute_Time.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_cmbEveryXHour_Time.getEnabled() && "".equals(this.m_cmbEveryXHour_Time.getText())){
			this.m_cmbEveryXHour_Time.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_cmbEveryXHour_Time.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_txtDate_Datetime.getEnabled() && "".equals(this.m_txtDate_Datetime.getText())){
			this.m_txtDate_Datetime.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtDate_Datetime.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_txtToDate_Datetime.getEnabled() && "".equals(this.m_txtToDate_Datetime.getText())){
			this.m_txtToDate_Datetime.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtToDate_Datetime.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// スケジュール情報（一定間隔）、ジョブセッション事前生成情報の制御
		if (this.m_scheduleComposite != null) {
			this.m_scheduleComposite.setIntervalEnabled(!enabled);
		}
	}

	/**
	 * 事前生成スケジュール情報をコンポジットに反映します。
	 *
	 */
	public void setSessionPremake(
			Boolean flg,
			JobKickResponse.SessionPremakeScheduleTypeEnum type,
			Integer week,
			Integer hour,
			Integer minute,
			JobKickResponse.SessionPremakeEveryXHourEnum everyXHour,
			String date,
			String toDate,
			Boolean internalFlg) {

		//日時を設定
		DecimalFormat format = new DecimalFormat("00");

		if (flg == null) {
			flg = false;
		}
		this.m_btnSessionPremake.setSelection(flg);
		if(type == null){
			type = JobKickResponse.SessionPremakeScheduleTypeEnum.EVERY_DAY;
		}
		if(type == JobKickResponse.SessionPremakeScheduleTypeEnum.EVERY_DAY){
			// スケジュール設定「毎日」
			this.m_btnScheduleType_EveryDay.setSelection(true);
			this.m_btnScheduleType_EveryWeek.setSelection(false);
			this.m_btnScheduleType_Time.setSelection(false);
			this.m_btnScheduleType_Datetime.setSelection(false);
			if(hour != null){
				//時を設定
				this.m_cmbHour_EveryDay.select(0);
				for (int i = 0; i < this.m_cmbHour_EveryDay.getItemCount(); i++) {
					String hours = format.format(hour);
					if (hours.equals(this.m_cmbHour_EveryDay.getItem(i))) {
						this.m_cmbHour_EveryDay.select(i);
						break;
					}
				}
			}
			if(minute != null){
				//分を設定
				this.m_cmbMinute_EveryDay.select(0);
				for (int i = 0; i < this.m_cmbMinute_EveryDay.getItemCount(); i++) {
					String minutes = format.format(minute);
					if (minutes.equals(this.m_cmbMinute_EveryDay.getItem(i))) {
						this.m_cmbMinute_EveryDay.select(i);
						break;
					}
				}
			}
		} else if (type == JobKickResponse.SessionPremakeScheduleTypeEnum.EVERY_WEEK){
			// スケジュール設定「毎週」
			this.m_btnScheduleType_EveryDay.setSelection(false);
			this.m_btnScheduleType_EveryWeek.setSelection(true);
			this.m_btnScheduleType_Time.setSelection(false);
			this.m_btnScheduleType_Datetime.setSelection(false);
			//曜日を設定
			this.m_cmbWeek_EveryWeek.select(0);
			for (int i = 0; i < this.m_cmbWeek_EveryWeek.getItemCount(); i++) {
				if (week == null) {
					break;
				}
				String dayOfWeek = DayOfWeekConstant.typeToString(week);
				if (dayOfWeek.equals(this.m_cmbWeek_EveryWeek.getItem(i))) {
					this.m_cmbWeek_EveryWeek.select(i);
					break;
				}
			}
			//時を設定
			if(hour != null){
				this.m_cmbHour_EveryWeek.select(0);
				for (int i = 0; i < this.m_cmbHour_EveryWeek.getItemCount(); i++) {
					String hours = format.format(hour);
					if (hours.equals(this.m_cmbHour_EveryWeek.getItem(i))) {
						this.m_cmbHour_EveryWeek.select(i);
						break;
					}
				}
			}
			//分を設定
			if(minute != null){
				this.m_cmbMinute_EveryWeek.select(0);
				for (int i = 0; i < this.m_cmbMinute_EveryWeek.getItemCount(); i++) {
					String minutes = format.format(minute);
					if (minutes.equals(this.m_cmbMinute_EveryWeek.getItem(i))) {
						this.m_cmbMinute_EveryWeek.select(i);
						break;
					}
				}
			}
		} else if(type == JobKickResponse.SessionPremakeScheduleTypeEnum.TIME){
			// スケジュール設定「時間」
			this.m_btnScheduleType_EveryDay.setSelection(false);
			this.m_btnScheduleType_EveryWeek.setSelection(false);
			this.m_btnScheduleType_Time.setSelection(true);
			this.m_btnScheduleType_Datetime.setSelection(false);
			
			//時を設定
			if(hour != null){
				this.m_cmbHour_Time.select(0);
				for (int i = 0; i < this.m_cmbHour_Time.getItemCount(); i++) {
					String hours = format.format(hour);
					if (hours.equals(this.m_cmbHour_Time.getItem(i))) {
						this.m_cmbHour_Time.select(i);
						break;
					}
				}
			}
			//分を設定
			if(minute != null){
				this.m_cmbMinute_Time.select(0);
				for (int i = 0; i < this.m_cmbMinute_Time.getItemCount(); i++) {
					String minutes = format.format(minute);
					if (minutes.equals(this.m_cmbMinute_Time.getItem(i))) {
						this.m_cmbMinute_Time.select(i);
						break;
					}
				}
			}
			//「everyXHour分毎」を設定
			if(everyXHour != null){
				this.m_cmbEveryXHour_Time.select(0);
				for (int i = 0; i < this.m_cmbEveryXHour_Time.getItemCount(); i++) {
					String hours = SessionPremakeEveryXHourEnum.valueOf(everyXHour.getValue()).getCode().toString();
					if (hours.equals(this.m_cmbEveryXHour_Time.getItem(i))) {
						this.m_cmbEveryXHour_Time.select(i);
						break;
					}
				}
			}
			this.m_lblMakeHour_Time.setText(m_cmbEveryXHour_Time.getText()
					+ Messages.getString("job.sessionpremake.time.description"));
		} else if(type == JobKickResponse.SessionPremakeScheduleTypeEnum.DATETIME){
			// スケジュール設定「日時」
			this.m_btnScheduleType_EveryDay.setSelection(false);
			this.m_btnScheduleType_EveryWeek.setSelection(false);
			this.m_btnScheduleType_Time.setSelection(false);
			this.m_btnScheduleType_Datetime.setSelection(true);
			
			//実行時間を設定
			if(date != null){
				m_txtDate_Datetime.setText(date);
			}
			//To時間を設定
			if(toDate != null){
				m_txtToDate_Datetime.setText(toDate);
			}
		}
		if (internalFlg == null) {
			internalFlg = false;
		}
		this.m_btnInternal.setSelection(internalFlg);
		update();
	}

	/**
	 * ジョブセッションの事前生成を有効にするか否かを戻します。
	 * @return true：ジョブセッションの事前生成を有効にする、false：無効にする
	 */
	public Boolean getFlg() {
		if(this.m_btnSessionPremake.getSelection()){
			return true;
		} else {
			return false;
		}
	}

	/**
	 * スケジュール設定：種別を戻します。
	 * @return スケジュール種別
	 */
	public JobKickResponse.SessionPremakeScheduleTypeEnum getType() {
		JobKickResponse.SessionPremakeScheduleTypeEnum result = null;
		if(this.m_btnScheduleType_EveryDay.getSelection()){
			// スケジュール設定「毎日」
			result = JobKickResponse.SessionPremakeScheduleTypeEnum.EVERY_DAY;
		} else if(this.m_btnScheduleType_EveryWeek.getSelection()){
			// スケジュール設定「毎週」
			result = JobKickResponse.SessionPremakeScheduleTypeEnum.EVERY_WEEK;
		} else if (this.m_btnScheduleType_Time.getSelection()){
			// スケジュール設定「時間」
			result = JobKickResponse.SessionPremakeScheduleTypeEnum.TIME;
		} else if (this.m_btnScheduleType_Datetime.getSelection()){
			// スケジュール設定「日時」
			result = JobKickResponse.SessionPremakeScheduleTypeEnum.DATETIME;
		}
		return result;
	}

	/**
	 * スケジュール設定：曜日を戻します。
	 * @return 曜日
	 */
	public Integer getWeek() {
		Integer result = null;
		if(this.m_btnScheduleType_EveryWeek.getSelection()){
			//スケジュール設定「曜日」
			if(this.m_cmbWeek_EveryWeek.getText().length() > 0){
				result = Integer.valueOf(DayOfWeekConstant
						.stringToType(this.m_cmbWeek_EveryWeek.getText()));
			}
		}
		return result;
	}

	/**
	 * スケジュール設定：時を戻します。
	 * @return 時
	 */
	public Integer getHour() {
		Integer result = null;
		if(this.m_btnScheduleType_EveryDay.getSelection()){
			//スケジュール設定「毎日」
			if(this.m_cmbHour_EveryDay.getText().length() > 0){
				result = Integer.valueOf(this.m_cmbHour_EveryDay.getText());
			}
		} else if(this.m_btnScheduleType_EveryWeek.getSelection()){
			//スケジュール設定「毎週」
			if(this.m_cmbHour_EveryWeek.getText().length() > 0){
				result = Integer.valueOf(this.m_cmbHour_EveryWeek.getText());
			}
		} else if(this.m_btnScheduleType_Time.getSelection()){
			//スケジュール設定「時間」
			if(this.m_cmbHour_Time.getText().length() > 0){
				result = Integer.valueOf(this.m_cmbHour_Time.getText());
			}
		}
		return result;
	}

	/**
	 * スケジュール設定：分を戻します。
	 * @return 分
	 */
	public Integer getMinute() {
		Integer result = null;
		if(this.m_btnScheduleType_EveryDay.getSelection()){
			//スケジュール設定「毎日」
			if(this.m_cmbMinute_EveryDay.getText().length() > 0){
				result = Integer.valueOf(this.m_cmbMinute_EveryDay.getText());
			}
		} else if(this.m_btnScheduleType_EveryWeek.getSelection()){
			//スケジュール設定「毎週」
			if(this.m_cmbMinute_EveryWeek.getText().length() > 0){
				result = Integer.valueOf(this.m_cmbMinute_EveryWeek.getText());
			}
		} else if(this.m_btnScheduleType_Time.getSelection()){
			//スケジュール設定「時間」
			if(this.m_cmbMinute_Time.getText().length() > 0){
				result = Integer.valueOf(this.m_cmbMinute_Time.getText());
			}
		}
		return result;
	}

	/**
	 * スケジュール設定：間隔（時）を戻します。
	 * @return 間隔（時）
	 */
	public JobKickResponse.SessionPremakeEveryXHourEnum getEveryXHour() {
		JobKickResponse.SessionPremakeEveryXHourEnum result = null;
		if (this.m_btnScheduleType_Time.getSelection()){
			// スケジュール設定「時間」
			if(this.m_cmbEveryXHour_Time.getText().length() > 0){
				for (SessionPremakeEveryXHourEnum item : SessionPremakeEveryXHourEnum.values()) {
					if (item.getCode().toString().equals(this.m_cmbEveryXHour_Time.getText())) {
						result = JobKickResponse.SessionPremakeEveryXHourEnum.fromValue(item.name());
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * スケジュール設定：日時を戻します。
	 * @return 日時
	 */
	public String getDate() {
		if (m_txtDate_Datetime.getText().isEmpty()){
			return null;
		} else {
			return m_txtDate_Datetime.getText();
		}
	}

	/**
	 * スケジュール設定：日時Toを戻します。
	 * @return 日時To
	 */
	public String getDateTo() {
		if (this.m_txtToDate_Datetime.getText().isEmpty()){
			return null;
		} else {
			return m_txtToDate_Datetime.getText();
		}
	}

	/**
	 * 事前生成が完了したらINTERNALイベントを出力するか否かを戻します。
	 * @return true：事前生成が完了したらINTERNALイベントを出力する、false：しない
	 */
	public Boolean getInternalFlg() {
		if(this.m_btnInternal.getSelection()){
			return true;
		} else {
			return false;
		}
	}

	/**
	 * スケジュール設定Compositeを設定します
	 * 
	 * @param scheduleComposite スケジュール設定Composite
	 */
	public void setScheduleComposite(JobKickScheduleComposite scheduleComposite) {
		this.m_scheduleComposite = scheduleComposite;
	}

	/**
	 * ジョブセッションの事前生成を有効にするの活性／非活性を設定します
	 * 
	 * @param enabled true:活性 / false:非活性
	 */
	public void setSessionPremakeEnabled(boolean enabled) {
		if (this.m_btnSessionPremake != null) {
			this.m_btnSessionPremake.setEnabled(enabled);
		}
	}

	/**
	 * ジョブセッションの事前生成を有効にする：チェックボックスの値を戻します
	 * 
	 * @return ジョブセッションの事前生成を有効にする
	 */
	public boolean getSessionPremake() {
		if (this.m_btnSessionPremake != null) {
			return this.m_btnSessionPremake.getSelection();
		} else {
			return false;
		}
	}
}
