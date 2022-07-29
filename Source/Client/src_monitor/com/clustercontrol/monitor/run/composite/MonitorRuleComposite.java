/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.calendar.composite.CalendarIdListComposite;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 監視条件コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「実行間隔」 コンボボックス</dd>
 *  <dd>「カレンダID」 コンボボックス</dd>
 * </dl>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class MonitorRuleComposite extends Composite {

	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = CommonMonitorDialog.WIDTH_TITLE;

	/** カラム数（値）。*/
	public static final int WIDTH_VALUE = CommonMonitorDialog.SHORT_UNIT;

	/** 空白のカラム数。 */
	public static final int WIDTH_WHITE_SPACE = CommonMonitorDialog.MIN_UNIT;

	/** 実行間隔 コンボボックス。 */
	private Combo m_comboRunInterval = null;

	/** カレンダID コンポジット。 */
	private CalendarIdListComposite m_calendarId = null;


	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public MonitorRuleComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 *
	 * @see com.clustercontrol.calendar.composite.CalendarIdListComposite#CalendarIdListComposite(Composite, int, boolean)
	 */
	private void initialize() {

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = CommonMonitorDialog.BASIC_UNIT;
		this.setLayout(layout);

		/*
		 * 実行間隔（分）
		 */
		// ラベル
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "runinterval", label);
		gridData = new GridData();
		gridData.horizontalSpan = CommonMonitorDialog.WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("run.interval") + " : ");
		// コンボボックス
		this.m_comboRunInterval = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, m_comboRunInterval);
		gridData = new GridData();
		gridData.horizontalSpan = CommonMonitorDialog.WIDTH_VALUE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboRunInterval.setLayoutData(gridData);
		this.m_comboRunInterval.add(RunInterval.TYPE_SEC_30.toString());
		this.m_comboRunInterval.add(RunInterval.TYPE_MIN_01.toString());
		this.m_comboRunInterval.add(RunInterval.TYPE_MIN_05.toString());
		this.m_comboRunInterval.add(RunInterval.TYPE_MIN_10.toString());
		this.m_comboRunInterval.add(RunInterval.TYPE_MIN_30.toString());
		this.m_comboRunInterval.add(RunInterval.TYPE_MIN_60.toString());

		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = CommonMonitorDialog.MIN_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * カレンダ
		 */
		this.m_calendarId = new CalendarIdListComposite(this, SWT.NONE, true);
		WidgetTestUtil.setTestId(this, null, m_calendarId);
		gridData = new GridData();
		gridData.horizontalSpan = CommonMonitorDialog.HALF_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_calendarId.setLayoutData(gridData);
	}

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる監視情報
	 *
	 * @see com.clustercontrol.calendar.composite.CalendarIdListComposite#setText(String)
	 */
	public void setInputData(MonitorInfoResponse info) {

		if(info != null){
			// 監視間隔
			if(this.m_comboRunInterval.isEnabled()){
				this.m_comboRunInterval.setText(
						RunInterval.enumToString(info.getRunInterval(), MonitorInfoResponse.RunIntervalEnum.class));
			}else{
				this.m_comboRunInterval.add("0");
				this.m_comboRunInterval.setText("0");
			}

			// カレンダー
			if (info.getCalendarId() != null) {
				this.m_calendarId.setText(info.getCalendarId());
			}
		}
	}

	/**
	 * 引数で指定された監視情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は認証結果を返します。
	 * 不正ではない場合は、<code>null</code>を返します。
	 *
	 * @param info 入力値を設定する監視情報
	 * @return 検証結果
	 *
	 * @see #setValidateResult(String, String)
	 * @see com.clustercontrol.calendar.composite.CalendarIdListComposite#getText()
	 */
	public ValidateResult createInputData(MonitorInfoResponse info) {

		if(info != null){
			if (this.m_comboRunInterval.getText() != null
					&& !"".equals((this.m_comboRunInterval.getText()).trim())) {
				if("0".equals(this.m_comboRunInterval.getText())){
					info.setRunInterval(MonitorInfoResponse.RunIntervalEnum.NONE);
				} else {
					switch (RunInterval.stringToType(this.m_comboRunInterval.getText())) {
					case TYPE_SEC_30:
						info.setRunInterval(MonitorInfoResponse.RunIntervalEnum.SEC_30);
						break;
					case TYPE_MIN_01:
						info.setRunInterval(MonitorInfoResponse.RunIntervalEnum.MIN_01);
						break;
					case TYPE_MIN_05:
						info.setRunInterval(MonitorInfoResponse.RunIntervalEnum.MIN_05);
						break;
					case TYPE_MIN_10:
						info.setRunInterval(MonitorInfoResponse.RunIntervalEnum.MIN_10);
						break;
					case TYPE_MIN_30:
						info.setRunInterval(MonitorInfoResponse.RunIntervalEnum.MIN_30);
						break;
					case TYPE_MIN_60:
						info.setRunInterval(MonitorInfoResponse.RunIntervalEnum.MIN_60);
						break;
					}
				}
			}

			if (this.m_calendarId.getText() != null
					&& !"".equals((this.m_calendarId.getText()).trim())) {
				info.setCalendarId(this.m_calendarId.getText());
			}
		}
		return null;
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_comboRunInterval.setEnabled(enabled);
		this.m_calendarId.setEnabled(enabled);
	}

	/**
	 * 監視間隔のメニュ選択可否の設定
	 * @param enabled
	 */
	public void setRunIntervalEnabled(boolean enabled) {
		this.m_comboRunInterval.setEnabled(enabled);
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 * @return 認証結果
	 */
	protected ValidateResult setValidateResult(String id, String message) {

		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	public CalendarIdListComposite getCalendarId() {
		return m_calendarId;
	}
}
