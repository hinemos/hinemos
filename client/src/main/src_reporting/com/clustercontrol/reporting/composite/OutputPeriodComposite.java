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

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.action.ComboModifyListener;
import com.clustercontrol.composite.action.NumberKeyListener;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.reporting.ReportingInfo;

/**
 * レポーティング出力期間コンポジットクラス<BR>
 * <p>
 * <dl>
 * <dt>コンポジット</dt>
 * <dd>「日単位」ラジオボタン</dd>
 * <dd>「開始日」 選択ボックス</dd>
 * <dd>「日数」 選択ボックス</dd>
 * <dd>「月単位」ラジオボタン</dd>
 * 　*
 * <dd>「開始月」 選択ボックス</dd>
 * <dd>「月数」 選択ボックス</dd>
 * <dd>「年単位」ラジオボタン</dd>
 * <dd>「開始年」 選択ボックス</dd>
 * <dd>「月数」 選択ボックス</dd>
 * </dl>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class OutputPeriodComposite extends Composite {

	/** 出力期間タイプ（日単位） */
	public static final int OUTPUT_PERIOD_TYPE_DAY = 0;

	/** 出力期間タイプ（月単位） */
	public static final int OUTPUT_PERIOD_TYPE_MONTH = 1;
	
	/** 出力期間タイプ（年単位） */
	public static final int OUTPUT_PERIOD_TYPE_YEAR = 2;

	/** 出力期間「～日前から」の範囲 */
	public static final int[] RANGE_DAY_BEFORE = {0, 90};

	/** 出力期間「～日分」の範囲 */
	public static final int[] RANGE_DAY_FOR = {1, 90};

	/** 出力期間「～か月前から」の範囲 */
	public static final int[] RANGE_MONTH_BEFORE = {0, 12};

	/** 出力期間「～か月分」の範囲 */
	public static final int[] RANGE_MONTH_FOR = {1, 12};
	
	/** 出力期間「～年前から」の範囲 */
	public static final int[] RANGE_YEAR_BEFORE = {0, 5};

	/** 出力期間「～年分」の範囲 */
	public static final int[] RANGE_YEAR_FOR = {1, 5};


	// 出力期間用ラジオボタン
	private Button m_typePeriodDay = null;
	private Combo m_comboPeriodDayBefore = null;
	private Combo m_comboPeriodDayFor = null;
	private Button m_typePeriodMonth = null;
	private Combo m_comboPeriodMonthBefore = null;
	private Combo m_comboPeriodMonthFor = null;
	private Button m_typePeriodYear = null;
	private Combo m_comboPeriodYearBefore = null;
	private Combo m_comboPeriodYearFor = null;

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
	public OutputPeriodComposite(Composite parent, int style) {
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

		Label label = new Label(this, SWT.NONE);
		label.setText(Messages.getString("output.period") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		Group groupOutputPeriod = new Group(this, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupOutputPeriod.setLayout(layout);

		gridData = new GridData();
		gridData.horizontalSpan = 11;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupOutputPeriod.setLayoutData(gridData);

		createOutputPeriodDay(groupOutputPeriod);
		createOutputPeriodMonth(groupOutputPeriod);
		createOutputPeriodYear(groupOutputPeriod);
	}

	/**
	 * 更新処理
	 * 
	 */
	@Override
	public void update() {

		// キーボードから範囲外の値が入力されたらリセットする
		Integer periodBefore = getOutputPeriodBefore();
		Integer periodFor = getOutputPeriodFor();

		if (m_typePeriodDay.getSelection()) {
			if (periodBefore != null &&
					(periodBefore < RANGE_DAY_BEFORE[0] || periodBefore > RANGE_DAY_BEFORE[1])) {
				m_comboPeriodDayBefore.setText("");
			}
			if (periodFor != null &&
					(periodFor < RANGE_DAY_FOR[0] || periodFor > RANGE_DAY_FOR[1])) {
				m_comboPeriodDayFor.setText("");
			}
		} else if (m_typePeriodMonth.getSelection()) {
			if (periodBefore != null &&
					(periodBefore < RANGE_MONTH_BEFORE[0] || periodBefore > RANGE_MONTH_BEFORE[1])) {
				m_comboPeriodMonthBefore.setText("");
			}
			if (periodFor != null &&
					(periodFor < RANGE_MONTH_FOR[0] || periodFor > RANGE_MONTH_FOR[1])) {
				m_comboPeriodMonthFor.setText("");
			}
		} else if (m_typePeriodYear.getSelection()) {
			if (periodBefore != null &&
					(periodBefore < RANGE_YEAR_BEFORE[0] || periodBefore > RANGE_YEAR_BEFORE[1])) {
				m_comboPeriodYearBefore.setText("");
			}
			if (periodFor != null &&
					(periodFor < RANGE_YEAR_FOR[0] || periodFor > RANGE_YEAR_FOR[1])) {
				m_comboPeriodYearFor.setText("");
			}
		}

		// 各項目が必須項目であることを明示
		if (this.m_comboPeriodDayBefore.getEnabled()
				&& "".equals(this.m_comboPeriodDayBefore.getText())) {
			this.m_comboPeriodDayBefore
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboPeriodDayBefore
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_comboPeriodDayFor.getEnabled()
				&& "".equals(this.m_comboPeriodDayFor.getText())) {
			this.m_comboPeriodDayFor
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboPeriodDayFor
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_comboPeriodMonthBefore.getEnabled()
				&& "".equals(this.m_comboPeriodMonthBefore.getText())) {
			this.m_comboPeriodMonthBefore
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboPeriodMonthBefore
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_comboPeriodMonthFor.getEnabled()
				&& "".equals(this.m_comboPeriodMonthFor.getText())) {
			this.m_comboPeriodMonthFor
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboPeriodMonthFor
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_comboPeriodYearBefore.getEnabled()
				&& "".equals(this.m_comboPeriodYearBefore.getText())) {
			this.m_comboPeriodYearBefore
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboPeriodYearBefore
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_comboPeriodYearFor.getEnabled()
				&& "".equals(this.m_comboPeriodYearFor.getText())) {
			this.m_comboPeriodYearFor
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboPeriodYearFor
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (m_typePeriodDay.getSelection()) {
			m_typePeriodMonth.setSelection(false);
			m_comboPeriodDayBefore.setEnabled(true);
			m_comboPeriodDayFor.setEnabled(true);
			m_comboPeriodMonthBefore.setEnabled(false);
			m_comboPeriodMonthFor.setEnabled(false);
			m_comboPeriodYearBefore.setEnabled(false);
			m_comboPeriodYearFor.setEnabled(false);

		} else if (m_typePeriodMonth.getSelection()) {
			m_typePeriodDay.setSelection(false);
			m_comboPeriodDayBefore.setEnabled(false);
			m_comboPeriodDayFor.setEnabled(false);
			m_comboPeriodMonthBefore.setEnabled(true);
			m_comboPeriodMonthFor.setEnabled(true);
			m_comboPeriodYearBefore.setEnabled(false);
			m_comboPeriodYearFor.setEnabled(false);
		} else if (m_typePeriodYear.getSelection()) {
			m_typePeriodDay.setSelection(false);
			m_comboPeriodDayBefore.setEnabled(false);
			m_comboPeriodDayFor.setEnabled(false);
			m_comboPeriodMonthBefore.setEnabled(false);
			m_comboPeriodMonthFor.setEnabled(false);
			m_comboPeriodYearBefore.setEnabled(true);
			m_comboPeriodYearFor.setEnabled(true);
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
	 * 出力期間（日単位）を生成します。
	 * 
	 * @param parent
	 *            親グループ
	 */
	private void createOutputPeriodDay(Group parent) {
		m_typePeriodDay = new Button(parent, SWT.RADIO);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_typePeriodDay.setLayoutData(gridData);
		m_typePeriodDay.setText(Messages
				.getString("output.period.from"));
		m_typePeriodDay.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		m_comboPeriodDayBefore = new Combo(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboPeriodDayBefore.setLayoutData(gridData);
		m_comboPeriodDayBefore.setTextLimit(2);
		m_comboPeriodDayBefore.setVisibleItemCount(10);
		m_comboPeriodDayBefore.addKeyListener(new NumberKeyListener());
		m_comboPeriodDayBefore.addModifyListener(new ComboModifyListener());
		DecimalFormat format = new DecimalFormat("0");
		m_comboPeriodDayBefore.add("");
		for (int day = RANGE_DAY_BEFORE[0]; day <= RANGE_DAY_BEFORE[1]; day++) {
			m_comboPeriodDayBefore.add(format.format(day));
		}
		this.m_comboPeriodDayBefore.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelDay = new Label(parent, SWT.NONE);
		labelDay.setText(Messages.getString("output.period.days.ago"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelDay.setLayoutData(gridData);

		m_comboPeriodDayFor = new Combo(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboPeriodDayFor.setLayoutData(gridData);
		m_comboPeriodDayFor.setTextLimit(2);
		m_comboPeriodDayFor.setVisibleItemCount(10);
		m_comboPeriodDayFor.addKeyListener(new NumberKeyListener());
		m_comboPeriodDayFor.addModifyListener(new ComboModifyListener());
		format = new DecimalFormat("0");
		m_comboPeriodDayFor.add("");
		for (int day = RANGE_DAY_FOR[0]; day <= RANGE_DAY_FOR[1]; day++) {
			m_comboPeriodDayFor.add(format.format(day));
		}
		this.m_comboPeriodDayFor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelHours = new Label(parent, SWT.NONE);
		labelHours.setText(Messages
				.getString("output.period.for.days"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelHours.setLayoutData(gridData);
	}

	/**
	 * 出力期間（月単位）を生成します。
	 * 
	 * @param parent
	 *            親グループ
	 */
	private void createOutputPeriodMonth(Group parent) {
		m_typePeriodMonth = new Button(parent, SWT.RADIO);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_typePeriodMonth.setLayoutData(gridData);
		m_typePeriodMonth.setText(Messages
				.getString("output.period.from"));
		m_typePeriodMonth.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		m_comboPeriodMonthBefore = new Combo(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboPeriodMonthBefore.setLayoutData(gridData);
		m_comboPeriodMonthBefore.setTextLimit(2);
		m_comboPeriodMonthBefore.setVisibleItemCount(10);
		m_comboPeriodMonthBefore.addKeyListener(new NumberKeyListener());
		m_comboPeriodMonthBefore.addModifyListener(new ComboModifyListener());
		DecimalFormat format = new DecimalFormat("0");
		m_comboPeriodMonthBefore.add("");
		for (int month = RANGE_MONTH_BEFORE[0]; month <= RANGE_MONTH_BEFORE[1]; month++) {
			m_comboPeriodMonthBefore.add(format.format(month));
		}
		this.m_comboPeriodMonthBefore.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelDay = new Label(parent, SWT.NONE);
		labelDay.setText(Messages
				.getString("output.period.months.ago"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelDay.setLayoutData(gridData);

		m_comboPeriodMonthFor = new Combo(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboPeriodMonthFor.setLayoutData(gridData);
		m_comboPeriodMonthFor.setTextLimit(2);
		m_comboPeriodMonthFor.setVisibleItemCount(10);
		m_comboPeriodMonthFor.addKeyListener(new NumberKeyListener());
		m_comboPeriodMonthFor.addModifyListener(new ComboModifyListener());
		format = new DecimalFormat("0");
		m_comboPeriodMonthFor.add("");
		for (int month = RANGE_MONTH_FOR[0]; month <= RANGE_MONTH_FOR[1]; month++) {
			m_comboPeriodMonthFor.add(format.format(month));
		}
		this.m_comboPeriodMonthFor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelHours = new Label(parent, SWT.NONE);
		labelHours.setText(Messages
				.getString("output.period.for.months"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelHours.setLayoutData(gridData);
	}
	
	/**
	 * 出力期間（年単位）を生成します。
	 * 
	 * @param parent
	 *            親グループ
	 */
	private void createOutputPeriodYear(Group parent) {
		m_typePeriodYear = new Button(parent, SWT.RADIO);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_typePeriodYear.setLayoutData(gridData);
		m_typePeriodYear.setText(Messages
				.getString("output.period.from"));
		m_typePeriodYear.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		m_comboPeriodYearBefore = new Combo(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboPeriodYearBefore.setLayoutData(gridData);
		m_comboPeriodYearBefore.setTextLimit(2);
		m_comboPeriodYearBefore.setVisibleItemCount(10);
		m_comboPeriodYearBefore.addKeyListener(new NumberKeyListener());
		m_comboPeriodYearBefore.addModifyListener(new ComboModifyListener());
		DecimalFormat format = new DecimalFormat("0");
		m_comboPeriodYearBefore.add("");
		for (int years = RANGE_YEAR_BEFORE[0]; years <= RANGE_YEAR_BEFORE[1]; years++) {
			m_comboPeriodYearBefore.add(format.format(years));
		}
		this.m_comboPeriodYearBefore.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelDay = new Label(parent, SWT.NONE);
		labelDay.setText(Messages
				.getString("output.period.years.ago"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelDay.setLayoutData(gridData);

		m_comboPeriodYearFor = new Combo(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboPeriodYearFor.setLayoutData(gridData);
		m_comboPeriodYearFor.setTextLimit(2);
		m_comboPeriodYearFor.setVisibleItemCount(10);
		m_comboPeriodYearFor.addKeyListener(new NumberKeyListener());
		m_comboPeriodYearFor.addModifyListener(new ComboModifyListener());
		format = new DecimalFormat("0");
		m_comboPeriodYearFor.add("");
		for (int year = RANGE_YEAR_FOR[0]; year <= RANGE_YEAR_FOR[1]; year++) {
			m_comboPeriodYearFor.add(format.format(year));
		}
		this.m_comboPeriodYearFor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelHours = new Label(parent, SWT.NONE);
		labelHours.setText(Messages
				.getString("output.period.for.years"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelHours.setLayoutData(gridData);
	}

	/**
	 * レポーティング情報を反映させます。
	 * 
	 * @param info
	 *            レポーティング情報
	 */
	public void reflectReportingInfo(ReportingInfo info) {
		DecimalFormat format = new DecimalFormat("0");

		if (info != null) {
			if (info.getOutputPeriodType() == OUTPUT_PERIOD_TYPE_DAY) {
				m_comboPeriodDayBefore.select(0);
				for (int i = 0; i < m_comboPeriodDayBefore.getItemCount(); i++) {
					if (info.getOutputPeriodBefore() == null) {
						break;
					}
					String periodBefore = format.format(info
							.getOutputPeriodBefore());
					if (periodBefore.equals(m_comboPeriodDayBefore.getItem(i))) {
						m_comboPeriodDayBefore.select(i);
						break;
					}
				}

				m_comboPeriodDayFor.select(0);
				for (int i = 0; i < m_comboPeriodDayFor.getItemCount(); i++) {
					if (info.getOutputPeriodFor() == null) {
						break;
					}
					String periodFor = format.format(info.getOutputPeriodFor());
					if (periodFor.equals(m_comboPeriodDayFor.getItem(i))) {
						m_comboPeriodDayFor.select(i);
						break;
					}
				}

				m_typePeriodDay.setSelection(true);
				m_comboPeriodDayBefore.setEnabled(true);
				m_comboPeriodDayFor.setEnabled(true);
				m_comboPeriodMonthBefore.setEnabled(false);
				m_comboPeriodMonthFor.setEnabled(false);
				m_comboPeriodYearBefore.setEnabled(false);
				m_comboPeriodYearFor.setEnabled(false);

			} else if (info.getOutputPeriodType() == OUTPUT_PERIOD_TYPE_MONTH) {
				m_comboPeriodMonthBefore.select(0);
				for (int i = 0; i < m_comboPeriodMonthBefore.getItemCount(); i++) {
					if (info.getOutputPeriodBefore() == null) {
						break;
					}
					String periodBefore = format.format(info
							.getOutputPeriodBefore());
					if (periodBefore
							.equals(m_comboPeriodMonthBefore.getItem(i))) {
						m_comboPeriodMonthBefore.select(i);
						break;
					}
				}

				m_comboPeriodMonthFor.select(0);
				for (int i = 0; i < m_comboPeriodMonthFor.getItemCount(); i++) {
					if (info.getOutputPeriodFor() == null) {
						break;
					}
					String periodFor = format.format(info.getOutputPeriodFor());
					if (periodFor.equals(m_comboPeriodMonthFor.getItem(i))) {
						m_comboPeriodMonthFor.select(i);
						break;
					}
				}

				m_typePeriodMonth.setSelection(true);
				m_comboPeriodDayBefore.setEnabled(false);
				m_comboPeriodDayFor.setEnabled(false);
				m_comboPeriodMonthBefore.setEnabled(true);
				m_comboPeriodMonthFor.setEnabled(true);
				m_comboPeriodYearBefore.setEnabled(false);
				m_comboPeriodYearFor.setEnabled(false);

			} else if (info.getOutputPeriodType() == OUTPUT_PERIOD_TYPE_YEAR) {
				m_comboPeriodYearBefore.select(0);
				for (int i = 0; i < m_comboPeriodYearBefore.getItemCount(); i++) {
					if (info.getOutputPeriodBefore() == null) {
						break;
					}
					String periodBefore = format.format(info
							.getOutputPeriodBefore());
					if (periodBefore
							.equals(m_comboPeriodYearBefore.getItem(i))) {
						m_comboPeriodYearBefore.select(i);
						break;
					}
				}

				m_comboPeriodYearFor.select(0);
				for (int i = 0; i < m_comboPeriodYearFor.getItemCount(); i++) {
					if (info.getOutputPeriodFor() == null) {
						break;
					}
					String periodFor = format.format(info.getOutputPeriodFor());
					if (periodFor.equals(m_comboPeriodYearFor.getItem(i))) {
						m_comboPeriodYearFor.select(i);
						break;
					}
				}

				m_typePeriodYear.setSelection(true);
				m_comboPeriodDayBefore.setEnabled(false);
				m_comboPeriodDayFor.setEnabled(false);
				m_comboPeriodMonthBefore.setEnabled(false);
				m_comboPeriodMonthFor.setEnabled(false);
				m_comboPeriodYearBefore.setEnabled(true);
				m_comboPeriodYearFor.setEnabled(true);

			} else {
				// unknown
			}
		}
	}

	/**
	 * 初期状態設定
	 */
	public void setInitialValue() {
		m_typePeriodDay.setSelection(true);
		m_comboPeriodDayBefore.setEnabled(true);
		m_comboPeriodDayFor.setEnabled(true);
		m_comboPeriodMonthBefore.setEnabled(false);
		m_comboPeriodMonthFor.setEnabled(false);
		m_comboPeriodYearBefore.setEnabled(false);
		m_comboPeriodYearFor.setEnabled(false);
	}

	/**
	 * 出力期間のタイプを返します。
	 * 
	 * @return 出力期間タイプ
	 */
	public Integer getOutputPeriodType() {
		if (m_typePeriodDay.getSelection()) {

			return OUTPUT_PERIOD_TYPE_DAY;
		} else if (m_typePeriodMonth.getSelection()) {
			return OUTPUT_PERIOD_TYPE_MONTH;
		} else if (m_typePeriodYear.getSelection()) {
			return OUTPUT_PERIOD_TYPE_YEAR;
		} else {
			return null;
		}
	}

	/**
	 * 出力期間（～日前 or ～か月前から or ～年前）を返します。
	 * 
	 * @return
	 */
	public Integer getOutputPeriodBefore() {
		if (m_typePeriodDay.getSelection()
				&& m_comboPeriodDayBefore.getText().length() > 0) {
			return Integer.valueOf(m_comboPeriodDayBefore.getText());
		} else if (m_typePeriodMonth.getSelection()
				&& m_comboPeriodMonthBefore.getText().length() > 0) {
			return Integer.valueOf(m_comboPeriodMonthBefore.getText());
		} else if (m_typePeriodYear.getSelection()
				&& m_comboPeriodYearBefore.getText().length() > 0) {
			return Integer.valueOf(m_comboPeriodYearBefore.getText());
		} else {
			return null;
		}
	}

	/**
	 * 出力期間（日数 or 月数 or 年数）を返します。
	 * 
	 * @return
	 */
	public Integer getOutputPeriodFor() {
		if (m_typePeriodDay.getSelection()
				&& m_comboPeriodDayFor.getText().length() > 0) {
			return Integer.valueOf(m_comboPeriodDayFor.getText());
		} else if (m_typePeriodMonth.getSelection()
				&& m_comboPeriodMonthFor.getText().length() > 0) {
			return Integer.valueOf(m_comboPeriodMonthFor.getText());
		} else if (m_typePeriodYear.getSelection()
				&& m_comboPeriodYearFor.getText().length() > 0) {
			return Integer.valueOf(m_comboPeriodYearFor.getText());
		} else {
			return null;
		}
	}

}
