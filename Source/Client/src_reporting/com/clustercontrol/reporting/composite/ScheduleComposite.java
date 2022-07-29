/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.composite;

import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.openapitools.client.model.ReportingScheduleInfoResponse;
import org.openapitools.client.model.ReportingScheduleResponse;
import org.openapitools.client.model.ReportingScheduleInfoResponse.ScheduleTypeEnum;

import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.action.ComboModifyListener;
import com.clustercontrol.composite.action.NumberKeyListener;
import com.clustercontrol.util.Messages;

/**
 * レポーティング出力期間コンポジットクラス<BR>
 * <p>
 * <dl>
 * <dt>コンポジット</dt>
 * <dd>時指定用コンボボックス（毎日）</dd>
 * <dd>分指定用コンボボックス（毎日）</dd>
 * <dd>曜日指定用コンボボックス（毎週）</dd>
 * <dd>時指定用コンボボックス（毎週）</dd>
 * <dd>分指定用コンボボックス（毎週）</dd>
 * <dd>日指定コンボボックス（毎月）</dd>
 * <dd>時指定コンボボックス（毎月）</dd>
 * <dd>分指定コンボボックス（毎月）</dd>
 * <dd>「毎日」ラジオボタン</dd>
 * <dd>「毎週」ラジオボタン</dd>
 * <dd>「毎月」ラジオボタン</dd>
 * </dl>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ScheduleComposite extends Composite {

	/** 時指定用コンボボックス（毎日） */
	private Combo m_comboHoursDaily = null;
	/** 分指定用コンボボックス（毎日） */
	private Combo m_comboMinutesDaily = null;

	/** 曜日指定用コンボボックス（毎週） */
	private Combo m_comboDayOfWeek = null;
	/** 時指定用コンボボックス（毎週） */
	private Combo m_comboHoursWeekly = null;
	/** 分指定用コンボボックス（毎週） */
	private Combo m_comboMinutesWeekly = null;

	/** 日指定用コンボボックス（毎月） */
	private Combo m_comboDay = null;
	/** 時指定用コンボボックス（毎月） */
	private Combo m_comboHoursMonthly = null;
	/** 分指定用コンボボックス（毎月） */
	private Combo m_comboMinutesMonthly = null;

	/** スケジュール毎日指定用ラジオボタン */
	private Button m_typeDaily = null;
	/** スケジュール毎週指定用ラジオボタン */
	private Button m_typeWeekly = null;
	/** スケジュール毎月指定用ラジオボタン　 */
	private Button m_typeMonthly = null;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 * 
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 * 
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *      style)
	 * @see #initialize()
	 */
	public ScheduleComposite(Composite parent, int style) {
		super(parent, style);
		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridData gridData;

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		Group group = new Group(this, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 20;
		group.setLayout(layout);

		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		group.setLayoutData(gridData);
		group.setText(Messages.getString("schedule"));

		// スケジュール日時指定、曜日指定
		createTypeDaily(group);
		createTypeWeekly(group);
		createTypeMonthly(group);
	}

	/**
	 * 更新処理
	 * 
	 */
	@Override
	public void update() {

		// 各項目が必須項目であることを明示
		if (this.m_comboHoursDaily.getEnabled()
				&& "".equals(this.m_comboHoursDaily.getText())) {
			this.m_comboHoursDaily
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboHoursDaily
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (this.m_comboMinutesDaily.getEnabled()
				&& "".equals(this.m_comboMinutesDaily.getText())) {
			this.m_comboMinutesDaily
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboMinutesDaily
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (this.m_comboDayOfWeek.getEnabled()
				&& "".equals(this.m_comboDayOfWeek.getText())) {
			this.m_comboDayOfWeek
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboDayOfWeek
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (this.m_comboHoursWeekly.getEnabled()
				&& "".equals(this.m_comboHoursWeekly.getText())) {
			this.m_comboHoursWeekly
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboHoursWeekly
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (this.m_comboMinutesWeekly.getEnabled()
				&& "".equals(this.m_comboMinutesWeekly.getText())) {
			this.m_comboMinutesWeekly
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboMinutesWeekly
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (this.m_comboDay.getEnabled()
				&& "".equals(this.m_comboDay.getText())) {
			this.m_comboDay
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboDay
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (this.m_comboHoursMonthly.getEnabled()
				&& "".equals(this.m_comboHoursMonthly.getText())) {
			this.m_comboHoursMonthly
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboHoursMonthly
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (this.m_comboMinutesMonthly.getEnabled()
				&& "".equals(this.m_comboMinutesMonthly.getText())) {
			this.m_comboMinutesMonthly
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboMinutesMonthly
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (m_typeDaily.getSelection()) {
			m_typeWeekly.setSelection(false);
			m_typeMonthly.setSelection(false);
			m_comboHoursDaily.setEnabled(true);
			m_comboMinutesDaily.setEnabled(true);
			m_comboDayOfWeek.setEnabled(false);
			m_comboHoursWeekly.setEnabled(false);
			m_comboMinutesWeekly.setEnabled(false);
			m_comboDay.setEnabled(false);
			m_comboHoursMonthly.setEnabled(false);
			m_comboMinutesMonthly.setEnabled(false);

		} else if (m_typeWeekly.getSelection()) {
			m_typeDaily.setSelection(false);
			m_typeMonthly.setSelection(false);
			m_comboHoursDaily.setEnabled(false);
			m_comboMinutesDaily.setEnabled(false);
			m_comboDayOfWeek.setEnabled(true);
			m_comboHoursWeekly.setEnabled(true);
			m_comboMinutesWeekly.setEnabled(true);
			m_comboDay.setEnabled(false);
			m_comboHoursMonthly.setEnabled(false);
			m_comboMinutesMonthly.setEnabled(false);

		} else if (m_typeMonthly.getSelection()) {
			m_typeDaily.setSelection(false);
			m_typeWeekly.setSelection(false);
			m_comboHoursDaily.setEnabled(false);
			m_comboMinutesDaily.setEnabled(false);
			m_comboDayOfWeek.setEnabled(false);
			m_comboHoursWeekly.setEnabled(false);
			m_comboMinutesWeekly.setEnabled(false);
			m_comboDay.setEnabled(true);
			m_comboHoursMonthly.setEnabled(true);
			m_comboMinutesMonthly.setEnabled(true);
		}
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {

	}

	/**
	 * スケジュール「毎日」時刻指定を生成します。
	 * 
	 * @param parent
	 *            親グループ
	 */
	private void createTypeDaily(Group parent) {
		m_typeDaily = new Button(parent, SWT.RADIO);
		m_typeDaily.setText(Messages.getString("everyday"));
		GridData gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_typeDaily.setLayoutData(gridData);
		m_typeDaily.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Label labelDummy = new Label(parent, SWT.NONE);
		labelDummy.setText("");
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelDummy.setLayoutData(gridData);

		m_comboHoursDaily = new Combo(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboHoursDaily.setLayoutData(gridData);
		m_comboHoursDaily.setTextLimit(2);
		m_comboHoursDaily.setVisibleItemCount(10);
		m_comboHoursDaily.addKeyListener(new NumberKeyListener());
		m_comboHoursDaily.addModifyListener(new ComboModifyListener());
		DecimalFormat format = new DecimalFormat("00");
		m_comboHoursDaily.add("");
		for (int hour = 0; hour < 24; hour++) {
			m_comboHoursDaily.add(format.format(hour));
		}
		this.m_comboHoursDaily.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelHours = new Label(parent, SWT.NONE);
		labelHours.setText(Messages.getString("hr"));
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelHours.setLayoutData(gridData);

		m_comboMinutesDaily = new Combo(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboMinutesDaily.setLayoutData(gridData);
		m_comboMinutesDaily.setTextLimit(2);
		m_comboMinutesDaily.setVisibleItemCount(10);
		m_comboMinutesDaily.addKeyListener(new NumberKeyListener());
		m_comboMinutesDaily.addModifyListener(new ComboModifyListener());
		format = new DecimalFormat("00");
		m_comboMinutesDaily.add("");
		for (int minutes = 0; minutes < 60; minutes++) {
			m_comboMinutesDaily.add(format.format(minutes));
		}
		this.m_comboMinutesDaily.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelMinutes = new Label(parent, SWT.NONE);
		labelMinutes.setText(Messages.getString("min"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelMinutes.setLayoutData(gridData);
	}

	/**
	 * スケジュール「毎週」曜日時刻指定を生成します。
	 * 
	 * @param parent
	 *            親グループ
	 */
	private void createTypeWeekly(Group parent) {
		m_typeWeekly = new Button(parent, SWT.RADIO);
		m_typeWeekly.setText(Messages.getString("schedule.everyweek"));
		GridData gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_typeWeekly.setLayoutData(gridData);
		m_typeWeekly.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		m_comboDayOfWeek = new Combo(parent, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 4;
		m_comboDayOfWeek.setLayoutData(gridData);
		m_comboDayOfWeek.setTextLimit(3);
		m_comboDayOfWeek.setVisibleItemCount(10);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_SUNDAY);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_MONDAY);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_TUESDAY);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_WEDNESDAY);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_THURSDAY);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_FRIDAY);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_SATURDAY);
		this.m_comboDayOfWeek.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		m_comboHoursWeekly = new Combo(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboHoursWeekly.setLayoutData(gridData);
		m_comboHoursWeekly.setTextLimit(2);
		m_comboHoursWeekly.setVisibleItemCount(10);
		m_comboHoursWeekly.addKeyListener(new NumberKeyListener());
		m_comboHoursWeekly.addModifyListener(new ComboModifyListener());
		DecimalFormat format = new DecimalFormat("00");
		m_comboHoursWeekly.add("");
		for (int hour = 0; hour < 24; hour++) {
			m_comboHoursWeekly.add(format.format(hour));
		}
		this.m_comboHoursWeekly.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelHours = new Label(parent, SWT.NONE);
		labelHours.setText(Messages.getString("hr"));
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelHours.setLayoutData(gridData);

		m_comboMinutesWeekly = new Combo(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboMinutesWeekly.setLayoutData(gridData);
		m_comboMinutesWeekly.setTextLimit(2);
		m_comboMinutesWeekly.setVisibleItemCount(10);
		m_comboMinutesWeekly.addKeyListener(new NumberKeyListener());
		m_comboMinutesWeekly.addModifyListener(new ComboModifyListener());
		format = new DecimalFormat("00");
		m_comboMinutesWeekly.add("");
		for (int minutes = 0; minutes < 60; minutes++) {
			m_comboMinutesWeekly.add(format.format(minutes));
		}
		this.m_comboMinutesWeekly.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelMinutes = new Label(parent, SWT.NONE);
		labelMinutes.setText(Messages.getString("min"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelMinutes.setLayoutData(gridData);
	}

	/**
	 * スケジュール「毎月」日時指定を生成します。
	 * 
	 * @param parent
	 *            親グループ
	 */
	private void createTypeMonthly(Group parent) {
		m_typeMonthly = new Button(parent, SWT.RADIO);
		m_typeMonthly.setText(Messages.getString("all.month"));
		GridData gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_typeMonthly.setLayoutData(gridData);
		m_typeMonthly.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		m_comboDay = new Combo(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboDay.setLayoutData(gridData);
		m_comboDay.setTextLimit(2);
		m_comboDay.setVisibleItemCount(10);
		m_comboDay.addKeyListener(new NumberKeyListener());
		m_comboDay.addModifyListener(new ComboModifyListener());
		DecimalFormat format = new DecimalFormat("0");
		m_comboDay.add("");
		for (int day = 1; day <= 31; day++) {
			m_comboDay.add(format.format(day));
		}
		this.m_comboDay.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelDays = new Label(parent, SWT.NONE);
		labelDays.setText(Messages.getString("day"));
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelDays.setLayoutData(gridData);

		m_comboHoursMonthly = new Combo(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboHoursMonthly.setLayoutData(gridData);
		m_comboHoursMonthly.setTextLimit(2);
		m_comboHoursMonthly.setVisibleItemCount(10);
		m_comboHoursMonthly.addKeyListener(new NumberKeyListener());
		m_comboHoursMonthly.addModifyListener(new ComboModifyListener());
		format = new DecimalFormat("00");
		m_comboHoursMonthly.add("");
		for (int hour = 0; hour < 24; hour++) {
			m_comboHoursMonthly.add(format.format(hour));
		}
		this.m_comboHoursMonthly.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelHours = new Label(parent, SWT.NONE);
		labelHours.setText(Messages.getString("hr"));
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelHours.setLayoutData(gridData);

		m_comboMinutesMonthly = new Combo(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboMinutesMonthly.setLayoutData(gridData);
		m_comboMinutesMonthly.setTextLimit(2);
		m_comboMinutesMonthly.setVisibleItemCount(10);
		m_comboMinutesMonthly.addKeyListener(new NumberKeyListener());
		m_comboMinutesMonthly.addModifyListener(new ComboModifyListener());
		format = new DecimalFormat("00");
		m_comboMinutesMonthly.add("");
		for (int minutes = 0; minutes < 60; minutes++) {
			m_comboMinutesMonthly.add(format.format(minutes));
		}
		this.m_comboMinutesMonthly.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelMinutes = new Label(parent, SWT.NONE);
		labelMinutes.setText(Messages.getString("min"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelMinutes.setLayoutData(gridData);
	}

	/**
	 * レポーティング情報を反映させます。
	 * 
	 * @param info
	 *            レポーティング情報
	 */
	public void reflectReportingInfo(ReportingScheduleResponse info) {
		// スケジュール設定
		if (info != null) {
			ReportingScheduleInfoResponse schedule = info.getSchedule();
			DecimalFormat format = new DecimalFormat("00");
			if (schedule.getScheduleType() == ScheduleTypeEnum.DAY
					&& schedule.getDay() == null) {
				// 時を設定
				m_comboHoursDaily.select(0);
				for (int i = 0; i < m_comboHoursDaily.getItemCount(); i++) {
					if (schedule.getHour() == null) {
						break;
					}
					String hours = format.format(schedule.getHour());
					if (hours.equals(m_comboHoursDaily.getItem(i))) {
						m_comboHoursDaily.select(i);
						break;
					}
				}
				// 分を設定
				m_comboMinutesDaily.select(0);
				for (int i = 0; i < m_comboMinutesDaily.getItemCount(); i++) {
					if (schedule.getMinute() == null) {
						break;
					}
					String minutes = format.format(schedule.getMinute());
					if (minutes.equals(m_comboMinutesDaily.getItem(i))) {
						m_comboMinutesDaily.select(i);
						break;
					}
				}

			} else if (schedule.getScheduleType() == ScheduleTypeEnum.WEEK) {
				// 曜日を設定
				m_comboDayOfWeek.select(0);
				String dayOfWeek = DayOfWeekConstant.typeToString(schedule
						.getWeek());
				for (int i = 0; i < m_comboDayOfWeek.getItemCount(); i++) {
					if (dayOfWeek.equals(m_comboDayOfWeek.getItem(i))) {
						m_comboDayOfWeek.select(i);
						break;
					}
				}
				// 時を設定
				m_comboHoursWeekly.select(0);
				for (int i = 0; i < m_comboHoursWeekly.getItemCount(); i++) {
					if (schedule.getHour() == null) {
						break;
					}
					String hours = format.format(schedule.getHour());
					if (hours.equals(m_comboHoursWeekly.getItem(i))) {
						m_comboHoursWeekly.select(i);
						break;
					}
				}
				// 分を設定
				m_comboMinutesWeekly.select(0);
				for (int i = 0; i < m_comboMinutesWeekly.getItemCount(); i++) {
					if (schedule.getMinute() == null) {
						break;
					}
					String minutes = format.format(schedule.getMinute());
					if (minutes.equals(m_comboMinutesWeekly.getItem(i))) {
						m_comboMinutesWeekly.select(i);
						break;
					}
				}

			} else if (schedule.getScheduleType() == ScheduleTypeEnum.DAY
					&& schedule.getDay() != null) {
				// 日を設定
				format = new DecimalFormat("");
				m_comboDay.select(0);
				for (int i = 0; i < m_comboDay.getItemCount(); i++) {
					if (schedule.getDay() == null) {
						break;
					}
					String day = format.format(schedule.getDay());
					if (day.equals(m_comboDay.getItem(i))) {
						m_comboDay.select(i);
						break;
					}
				}

				format = new DecimalFormat("00");
				// 時を設定
				m_comboHoursMonthly.select(0);
				for (int i = 0; i < m_comboHoursMonthly.getItemCount(); i++) {
					if (schedule.getHour() == null) {
						break;
					}
					String hours = format.format(schedule.getHour());
					if (hours.equals(m_comboHoursMonthly.getItem(i))) {
						m_comboHoursMonthly.select(i);
						break;
					}
				}
				// 分を設定
				m_comboMinutesMonthly.select(0);
				for (int i = 0; i < m_comboMinutesMonthly.getItemCount(); i++) {
					if (schedule.getMinute() == null) {
						break;
					}
					String minutes = format.format(schedule.getMinute());
					if (minutes.equals(m_comboMinutesMonthly.getItem(i))) {
						m_comboMinutesMonthly.select(i);
						break;
					}
				}

			} else {
				// unknown
			}

			// 指定方式を設定
			if (schedule.getScheduleType() == ScheduleTypeEnum.DAY
					&& schedule.getDay() == null) {
				m_typeDaily.setSelection(true);
				m_comboHoursDaily.setEnabled(true);
				m_comboMinutesDaily.setEnabled(true);
				m_comboDayOfWeek.setEnabled(false);
				m_comboHoursWeekly.setEnabled(false);
				m_comboMinutesWeekly.setEnabled(false);
				m_comboDay.setEnabled(false);
				m_comboHoursMonthly.setEnabled(false);
				m_comboMinutesMonthly.setEnabled(false);
			} else if (schedule.getScheduleType() == ScheduleTypeEnum.WEEK) {
				m_typeWeekly.setSelection(true);
				m_comboHoursDaily.setEnabled(false);
				m_comboMinutesDaily.setEnabled(false);
				m_comboDayOfWeek.setEnabled(true);
				m_comboHoursWeekly.setEnabled(true);
				m_comboMinutesWeekly.setEnabled(true);
				m_comboDay.setEnabled(false);
				m_comboHoursMonthly.setEnabled(false);
				m_comboMinutesMonthly.setEnabled(false);
			} else if (schedule.getScheduleType() == ScheduleTypeEnum.DAY
					&& schedule.getDay() != null) {
				m_typeMonthly.setSelection(true);
				m_comboHoursDaily.setEnabled(false);
				m_comboMinutesDaily.setEnabled(false);
				m_comboDayOfWeek.setEnabled(false);
				m_comboHoursWeekly.setEnabled(false);
				m_comboMinutesWeekly.setEnabled(false);
				m_comboDay.setEnabled(true);
				m_comboHoursMonthly.setEnabled(true);
				m_comboMinutesMonthly.setEnabled(true);
			} else {
				// unknown
			}
		}
	}

	/**
	 * 初期状態の設定。
	 */
	public void setInitialValue() {
		m_typeDaily.setSelection(true);
		m_comboHoursDaily.setEnabled(true);
		m_comboMinutesDaily.setEnabled(true);
		m_comboDayOfWeek.setEnabled(false);
		m_comboHoursWeekly.setEnabled(false);
		m_comboMinutesWeekly.setEnabled(false);
		m_comboHoursMonthly.setEnabled(false);
		m_comboMinutesMonthly.setEnabled(false);
		m_comboDay.setEnabled(false);
	}

	/**
	 * スケジュールタイプを返します。
	 * 
	 * @return スケジュールタイプ
	 */
	public ScheduleTypeEnum getType() {
		if (m_typeDaily.getSelection() || m_typeMonthly.getSelection()) {
			return ScheduleTypeEnum.DAY;
		} else if (m_typeWeekly.getSelection()) {
			return ScheduleTypeEnum.WEEK;
		} else {
			return null;
		}
	}

	/**
	 * スケジュールの日を返します。
	 * 
	 * @return 日
	 */
	public Integer getDay() {
		if (m_typeMonthly.getSelection() && m_comboDay.getText().length() > 0) {
			return Integer.valueOf(m_comboDay.getText());
		} else {
			return null;
		}
	}

	/**
	 * スケジュールの週を返します。
	 * 
	 * @return 週
	 */
	public Integer getWeek() {
		if (m_typeWeekly.getSelection()
				&& m_comboDayOfWeek.getText().length() > 0) {
			return Integer.valueOf(DayOfWeekConstant.stringToType(m_comboDayOfWeek
					.getText()));
		} else {
			return null;
		}
	}

	/**
	 * スケジュールの時を返します。
	 * 
	 * @return 時
	 */
	public Integer getHour() {
		if (m_typeDaily.getSelection()
				&& m_comboHoursDaily.getText().length() > 0) {
			return Integer.valueOf(m_comboHoursDaily.getText());
		} else if (m_typeWeekly.getSelection()
				&& m_comboHoursWeekly.getText().length() > 0) {
			return Integer.valueOf(m_comboHoursWeekly.getText());
		} else if (m_typeMonthly.getSelection()
				&& m_comboHoursMonthly.getText().length() > 0) {
			return Integer.valueOf(m_comboHoursMonthly.getText());
		} else {
			return null;
		}
	}

	/**
	 * スケジュールの分を返します。
	 * 
	 * @return 分
	 */
	public Integer getMinute() {
		if (m_typeDaily.getSelection()
				&& m_comboMinutesDaily.getText().length() > 0) {
			return Integer.valueOf(m_comboMinutesDaily.getText());
		} else if (m_typeWeekly.getSelection()
				&& m_comboMinutesWeekly.getText().length() > 0) {
			return Integer.valueOf(m_comboMinutesWeekly.getText());
		} else if (m_typeMonthly.getSelection()
				&& m_comboMinutesMonthly.getText().length() > 0) {
			return Integer.valueOf(m_comboMinutesMonthly.getText());
		} else {
			return null;
		}
	}

}
