/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.composite.action.ComboModifyListener;
import com.clustercontrol.composite.action.NumberKeyListener;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ジョブ実行契機の作成・変更ダイアログのスケジュールタブ用の
 * コンポジットクラスです。
 *
 * @version 5.1.0
 */
public class JobKickScheduleComposite extends Composite {

	/** スケジュール設定：毎日：ラジオボタン */
	private Button m_btnScheduleType_Day = null;
	/** スケジュール設定：毎日：時コンボボックス */
	private Combo m_cmbHour_Day = null;
	/** スケジュール設定：毎日：分コンボボックス */
	private Combo m_cmbMinute_Day = null;

	/** スケジュール設定：毎週：ラジオボタン */
	private Button m_btnScheduleType_Week = null;
	/** スケジュール設定：毎週：曜日コンボボックス */
	private Combo m_cmbWeek_Week = null;
	/** スケジュール設定：毎週：時コンボボックス */
	private Combo m_cmbHour_Week = null;
	/** スケジュール設定：毎週：分コンボボックス */
	private Combo m_cmbMinute_Week = null;

	/** スケジュール設定：毎時：ラジオボタン */
	private Button m_btnScheduleType_Hour = null;
	/** スケジュール設定：毎時：分Fromラジオボタン */
	private Combo m_cmbFromXminutes_Hour = null;
	/** スケジュール設定：毎時：分Everyラジオボタン */
	private Combo m_cmbEveryXminutes_Hour = null;


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
	public JobKickScheduleComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		Label label = null;
		GridData gridData = null;
		DecimalFormat format = new DecimalFormat("00");

		this.setLayout(JobDialogUtil.getParentLayout());

		// スケジュール設定（Composite）
		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(14, true));
		composite.setLayoutData(new RowData());
		((RowData)composite.getLayoutData()).width = 525;

		// 毎日（ラジオボタン）
		this.m_btnScheduleType_Day = new Button(composite, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_btnScheduleType_Day", this.m_btnScheduleType_Day);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_btnScheduleType_Day.setLayoutData(gridData);
		this.m_btnScheduleType_Day.setText(Messages.getString("everyday"));
		this.m_btnScheduleType_Day.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 毎日：時（コンボボックス）
		this.m_cmbHour_Day = new Combo(composite, SWT.READ_ONLY | SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_cmbHour_Day", this.m_cmbHour_Day);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbHour_Day.setLayoutData(gridData);
		this.m_cmbHour_Day.setTextLimit(2);
		this.m_cmbHour_Day.setVisibleItemCount(10);
		this.m_cmbHour_Day.addKeyListener(new NumberKeyListener());
		this.m_cmbHour_Day.addModifyListener(new ComboModifyListener());
		this.m_cmbHour_Day.add("*");
		for (int hour = 0; hour <= 48; hour++) {
			this.m_cmbHour_Day.add(format.format(hour));
		}
		this.m_cmbHour_Day.select(0);
		this.m_cmbHour_Day.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 毎日：時（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("hr"));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 毎日：分（コンボボックス）
		this.m_cmbMinute_Day = new Combo(composite, SWT.READ_ONLY | SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_cmbMinute_Day", this.m_cmbMinute_Day);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbMinute_Day.setLayoutData(gridData);
		this.m_cmbMinute_Day.setTextLimit(2);
		this.m_cmbMinute_Day.setVisibleItemCount(10);
		this.m_cmbMinute_Day.addKeyListener(new NumberKeyListener());
		this.m_cmbMinute_Day.addModifyListener(new ComboModifyListener());
		for (int minutes = 0; minutes < 60; minutes++) {
			this.m_cmbMinute_Day.add(format.format(minutes));
		}
		this.m_cmbMinute_Day.select(0);
		this.m_cmbMinute_Day.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 毎日：分（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("min"));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		// 毎週（ラジオボタン）
		this.m_btnScheduleType_Week = new Button(composite, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_btnScheduleType_Week", this.m_btnScheduleType_Week);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_btnScheduleType_Week.setLayoutData(gridData);
		this.m_btnScheduleType_Week.setText(Messages.getString("schedule.everyweek"));
		this.m_btnScheduleType_Week.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 毎週：曜日（コンボボックス）
		this.m_cmbWeek_Week = new Combo(composite, SWT.READ_ONLY | SWT.CENTER);
		WidgetTestUtil.setTestId(this, "m_cmbWeek_Week", this.m_cmbWeek_Week);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbWeek_Week.setLayoutData(gridData);
		this.m_cmbWeek_Week.setTextLimit(3);
		this.m_cmbWeek_Week.setVisibleItemCount(10);
		this.m_cmbWeek_Week.add(DayOfWeekConstant.STRING_SUNDAY);
		this.m_cmbWeek_Week.add(DayOfWeekConstant.STRING_MONDAY);
		this.m_cmbWeek_Week.add(DayOfWeekConstant.STRING_TUESDAY);
		this.m_cmbWeek_Week.add(DayOfWeekConstant.STRING_WEDNESDAY);
		this.m_cmbWeek_Week.add(DayOfWeekConstant.STRING_THURSDAY);
		this.m_cmbWeek_Week.add(DayOfWeekConstant.STRING_FRIDAY);
		this.m_cmbWeek_Week.add(DayOfWeekConstant.STRING_SATURDAY);
		this.m_cmbWeek_Week.select(0);
		this.m_cmbWeek_Week.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 毎週：時（コンボボックス）
		this.m_cmbHour_Week = new Combo(composite, SWT.READ_ONLY | SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_cmbHour_Week", this.m_cmbHour_Week);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbHour_Week.setLayoutData(gridData);
		this.m_cmbHour_Week.setTextLimit(2);
		this.m_cmbHour_Week.setVisibleItemCount(10);
		this.m_cmbHour_Week.addKeyListener(new NumberKeyListener());
		this.m_cmbHour_Week.addModifyListener(new ComboModifyListener());
		this.m_cmbHour_Week.add("*");
		for (int hour = 0; hour <= 48; hour++) {
			this.m_cmbHour_Week.add(format.format(hour));
		}
		this.m_cmbHour_Week.select(0);
		this.m_cmbHour_Week.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 毎週：時（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("hr"));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 毎週：分（コンボボックス）
		this.m_cmbMinute_Week = new Combo(composite, SWT.READ_ONLY | SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_cmbMinute_Week", this.m_cmbMinute_Week);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbMinute_Week.setLayoutData(gridData);
		this.m_cmbMinute_Week.setTextLimit(2);
		this.m_cmbMinute_Week.setVisibleItemCount(10);
		this.m_cmbMinute_Week.addKeyListener(new NumberKeyListener());
		this.m_cmbMinute_Week.addModifyListener(new ComboModifyListener());
		for (int minutes = 0; minutes < 60; minutes++) {
			this.m_cmbMinute_Week.add(format.format(minutes));
		}
		this.m_cmbMinute_Week.select(0);
		this.m_cmbMinute_Week.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 毎週：分（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("min"));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		// 毎時（ラジオボタン）
		this.m_btnScheduleType_Hour = new Button(composite, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_btnScheduleType_Hour", this.m_btnScheduleType_Hour);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_btnScheduleType_Hour.setLayoutData(gridData);
		this.m_btnScheduleType_Hour.setText(Messages.getString("hourly"));
		this.m_btnScheduleType_Hour.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 毎時：分From（コンボボックス）
		this.m_cmbFromXminutes_Hour = new Combo(composite, SWT.READ_ONLY | SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_cmbFromXminutes_Hour", this.m_cmbFromXminutes_Hour);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbFromXminutes_Hour.setLayoutData(gridData);
		this.m_cmbFromXminutes_Hour.setTextLimit(2);
		this.m_cmbFromXminutes_Hour.setVisibleItemCount(10);
		this.m_cmbFromXminutes_Hour.addKeyListener(new NumberKeyListener());
		this.m_cmbFromXminutes_Hour.addModifyListener(new ComboModifyListener());
		this.m_cmbFromXminutes_Hour.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 毎時：分From（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("schedule.min.start.time"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 毎時：分Every（コンボボックス）
		this.m_cmbEveryXminutes_Hour = new Combo(composite, SWT.READ_ONLY | SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_cmbEveryXminutes_Hour", this.m_cmbEveryXminutes_Hour);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_cmbEveryXminutes_Hour.setLayoutData(gridData);
		this.m_cmbEveryXminutes_Hour.setTextLimit(2);
		this.m_cmbEveryXminutes_Hour.setVisibleItemCount(10);
		this.m_cmbEveryXminutes_Hour.addKeyListener(new NumberKeyListener());
		this.m_cmbEveryXminutes_Hour.addModifyListener(new ComboModifyListener());
		this.m_cmbEveryXminutes_Hour.add(format.format(1));
		this.m_cmbEveryXminutes_Hour.add(format.format(2));
		this.m_cmbEveryXminutes_Hour.add(format.format(3));
		this.m_cmbEveryXminutes_Hour.add(format.format(5));
		this.m_cmbEveryXminutes_Hour.add(format.format(10));
		this.m_cmbEveryXminutes_Hour.add(format.format(15));
		this.m_cmbEveryXminutes_Hour.add(format.format(20));
		this.m_cmbEveryXminutes_Hour.add(format.format(30));
		this.m_cmbEveryXminutes_Hour.add(format.format(60));
		this.m_cmbEveryXminutes_Hour.select(5);
		this.m_cmbEveryXminutes_Hour.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				DecimalFormat format = new DecimalFormat("00");
				int tmp = m_cmbFromXminutes_Hour.getSelectionIndex();
				int minutesQ = 0;
				try {
					minutesQ = Integer.parseInt(m_cmbEveryXminutes_Hour.getText());
					m_cmbFromXminutes_Hour.removeAll();
					for (int minutes = 0; minutes < minutesQ; minutes++) {
						m_cmbFromXminutes_Hour.add(format.format(minutes));
					}
				}catch (NumberFormatException e){
					// 初期表示の場合にしかこの例外は発生しない
					m_cmbEveryXminutes_Hour.setText("60");
				}
				if (minutesQ > 0) {
					tmp = tmp%minutesQ;
				}
				if(m_cmbFromXminutes_Hour.getItemCount() > tmp){
					m_cmbFromXminutes_Hour.select(tmp);
				}
				update();
			}
		});
		int everyMinutes = Integer.parseInt(this.m_cmbEveryXminutes_Hour.getText());
		for (int minutes = 0; minutes < everyMinutes; minutes++) {
			this.m_cmbFromXminutes_Hour.add(format.format(minutes));
		}

		// 毎時：分Every（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("schedule.min.execution.interval"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 初期設定
		this.m_btnScheduleType_Day.setSelection(true);
		this.m_cmbHour_Day.setEnabled(true);
		this.m_cmbMinute_Day.setEnabled(true);
		this.m_cmbWeek_Week.setEnabled(false);
		this.m_cmbHour_Week.setEnabled(false);
		this.m_cmbMinute_Week.setEnabled(false);
		this.m_cmbFromXminutes_Hour.setEnabled(false);
		this.m_cmbEveryXminutes_Hour.setEnabled(false);
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目を明示
		if(this.m_cmbMinute_Day.getEnabled() && "".equals(this.m_cmbMinute_Day.getText())){
			this.m_cmbMinute_Day.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_cmbMinute_Day.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_cmbWeek_Week.getEnabled() && "".equals(this.m_cmbWeek_Week.getText())){
			this.m_cmbWeek_Week.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_cmbWeek_Week.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_cmbMinute_Week.getEnabled() && "".equals(this.m_cmbMinute_Week.getText())){
			this.m_cmbMinute_Week.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_cmbMinute_Week.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_cmbFromXminutes_Hour.getEnabled() && "".equals(this.m_cmbFromXminutes_Hour.getText())){
			this.m_cmbFromXminutes_Hour.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_cmbFromXminutes_Hour.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_cmbEveryXminutes_Hour.getEnabled() && "".equals(this.m_cmbEveryXminutes_Hour.getText())){
			this.m_cmbEveryXminutes_Hour.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_cmbEveryXminutes_Hour.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		this.m_cmbHour_Day.setEnabled(this.m_btnScheduleType_Day.getSelection());
		this.m_cmbMinute_Day.setEnabled(this.m_btnScheduleType_Day.getSelection());
		this.m_cmbWeek_Week.setEnabled(this.m_btnScheduleType_Week.getSelection());
		this.m_cmbHour_Week.setEnabled(this.m_btnScheduleType_Week.getSelection());
		this.m_cmbMinute_Week.setEnabled(this.m_btnScheduleType_Week.getSelection());
		this.m_cmbFromXminutes_Hour.setEnabled(this.m_btnScheduleType_Hour.getSelection());
		this.m_cmbEveryXminutes_Hour.setEnabled(this.m_btnScheduleType_Hour.getSelection());
	}

	/**
	 * ジョブスケジュール情報をコンポジットに反映します。
	 *
	 * @param scheduleType
	 * @param week
	 * @param hour
	 * @param minute
	 * @param fromXminutes
	 * @param everyXminutes
	 */
	public void setJobSchedule(
			Integer scheduleType,
			Integer week,
			Integer hour,
			Integer minute,
			Integer fromXminutes,
			Integer everyXminutes) {
		
		//日時を設定
		DecimalFormat format = new DecimalFormat("00");
		if(scheduleType == ScheduleConstant.TYPE_DAY){
			// スケジュール設定「毎日」
			this.m_btnScheduleType_Day.setSelection(true);
			this.m_btnScheduleType_Week.setSelection(false);
			this.m_btnScheduleType_Hour.setSelection(false);
			if(hour != null){
				//時を設定
				this.m_cmbHour_Day.select(0);
				for (int i = 0; i < this.m_cmbHour_Day.getItemCount(); i++) {
					String hours = format.format(hour);
					if (hours.equals(this.m_cmbHour_Day.getItem(i))) {
						this.m_cmbHour_Day.select(i);
						break;
					}
				}
			}
			if(minute != null){
				//分を設定
				this.m_cmbMinute_Day.select(0);
				for (int i = 0; i < this.m_cmbMinute_Day.getItemCount(); i++) {
					String minutes = format.format(minute);
					if (minutes.equals(this.m_cmbMinute_Day.getItem(i))) {
						this.m_cmbMinute_Day.select(i);
						break;
					}
				}
			}
		} else if(scheduleType == ScheduleConstant.TYPE_WEEK){
			// スケジュール設定「毎週」
			this.m_btnScheduleType_Day.setSelection(false);
			this.m_btnScheduleType_Week.setSelection(true);
			this.m_btnScheduleType_Hour.setSelection(false);
			//曜日を設定
			this.m_cmbWeek_Week.select(0);
			for (int i = 0; i < this.m_cmbWeek_Week.getItemCount(); i++) {
				if (week == null) {
					break;
				}
				String dayOfWeek = DayOfWeekConstant.typeToString(week);
				if (dayOfWeek.equals(this.m_cmbWeek_Week.getItem(i))) {
					this.m_cmbWeek_Week.select(i);
					break;
				}
			}
			//時を設定
			if(hour != null){
				this.m_cmbHour_Week.select(0);
				for (int i = 0; i < this.m_cmbHour_Week.getItemCount(); i++) {
					String hours = format.format(hour);
					if (hours.equals(this.m_cmbHour_Week.getItem(i))) {
						this.m_cmbHour_Week.select(i);
						break;
					}
				}
			}
			//分を設定
			if(minute != null){
				this.m_cmbMinute_Week.select(0);
				for (int i = 0; i < this.m_cmbMinute_Week.getItemCount(); i++) {
					String minutes = format.format(minute);
					if (minutes.equals(this.m_cmbMinute_Week.getItem(i))) {
						this.m_cmbMinute_Week.select(i);
						break;
					}
				}
			}
		} else if(scheduleType == ScheduleConstant.TYPE_REPEAT){
			// スケジュール設定「毎時」
			this.m_btnScheduleType_Day.setSelection(false);
			this.m_btnScheduleType_Week.setSelection(false);
			this.m_btnScheduleType_Hour.setSelection(true);

			/**
			 * FIXME
			 * 「everyXminutes分毎」が設定された後に一度「cmbFromXminutes_Hour」をリセットするため、
			 * 「everyXminutes分毎」を設定後に、「fromXminutes分から」を設定する必要があります
			 */
			//「everyXminutes分毎」を設定
			if(everyXminutes != null){
				this.m_cmbEveryXminutes_Hour.select(0);
				for (int i = 0; i < this.m_cmbEveryXminutes_Hour.getItemCount(); i++) {
					String minutes = format.format(everyXminutes);
					if (minutes.equals(this.m_cmbEveryXminutes_Hour.getItem(i))) {
						this.m_cmbEveryXminutes_Hour.select(i);
						break;
					}
				}
			}
			//「fromXminutes分から」を設定
			if(fromXminutes != null){
				this.m_cmbFromXminutes_Hour.select(0);
				for (int i = 0; i < this.m_cmbFromXminutes_Hour.getItemCount(); i++) {
					String minutes = format.format(fromXminutes);
					if (minutes.equals(this.m_cmbFromXminutes_Hour.getItem(i))) {
						this.m_cmbFromXminutes_Hour.select(i);
						break;
					}
				}
			}
		}
	}

	/**
	 * スケジュール設定：種別を戻します。
	 * @return スケジュール種別
	 */
	public Integer getScheduleType() {
		Integer result = null;
		if(this.m_btnScheduleType_Day.getSelection()){
			// スケジュール設定「毎日」
			result = ScheduleConstant.TYPE_DAY;
		} else if(this.m_btnScheduleType_Week.getSelection()){
			// スケジュール設定「毎週」
			result = ScheduleConstant.TYPE_WEEK;
		} else if (this.m_btnScheduleType_Hour.getSelection()){
			// スケジュール設定「毎時」
			result = ScheduleConstant.TYPE_REPEAT;
		}
		return result;
	}

	/**
	 * スケジュール設定：曜日を戻します。
	 * @return 曜日
	 */
	public Integer getWeek() {
		Integer result = null;
		if(this.m_btnScheduleType_Week.getSelection()){
			//スケジュール設定「曜日」
			if(this.m_cmbWeek_Week.getText().length() > 0){
				result = Integer.valueOf(DayOfWeekConstant
						.stringToType(this.m_cmbWeek_Week.getText()));
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
		if(this.m_btnScheduleType_Day.getSelection()){
			//スケジュール設定「毎日」
			if(this.m_cmbHour_Day.getText().length() > 0 
					&& !this.m_cmbHour_Day.getText().equals("*")){
				result = Integer.valueOf(this.m_cmbHour_Day.getText());
			}
		} else if(this.m_btnScheduleType_Week.getSelection()){
			//スケジュール設定「毎週」
			if(this.m_cmbHour_Week.getText().length() > 0
					&& !this.m_cmbHour_Week.getText().equals("*")){
				result = Integer.valueOf(this.m_cmbHour_Week.getText());
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
		if(this.m_btnScheduleType_Day.getSelection()){
			//スケジュール設定「毎日」
			if(this.m_cmbMinute_Day.getText().length() > 0){
				result = Integer.valueOf(this.m_cmbMinute_Day.getText());
			}
		} else if(this.m_btnScheduleType_Week.getSelection()){
			//スケジュール設定「曜日」
			if(this.m_cmbMinute_Week.getText().length() > 0){
				result = Integer.valueOf(this.m_cmbMinute_Week.getText());
			}
		}
		return result;
	}

	/**
	 * スケジュール設定：開始（分）を戻します。
	 * @return 開始（分）
	 */
	public Integer getFromXminutes() {
		Integer result = null;
		if (this.m_btnScheduleType_Hour.getSelection()){
			// スケジュール設定「毎時」
			if(this.m_cmbFromXminutes_Hour.getText().length() > 0){
				result = Integer.valueOf(this.m_cmbFromXminutes_Hour.getText());
			}
		}
		return result;
	}

	/**
	 * スケジュール設定：間隔（分）を戻します。
	 * @return 間隔（分）
	 */
	public Integer getEveryXminutes() {
		Integer result = null;
		if (this.m_btnScheduleType_Hour.getSelection()){
			// スケジュール設定「毎時」
			if(this.m_cmbEveryXminutes_Hour.getText().length() > 0){
				result = Integer.valueOf(this.m_cmbEveryXminutes_Hour.getText());
			}
		}
		return result;
	}
}
