/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.composite;

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
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.calendar.composite.CalendarIdListComposite;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.JobWaitRuleInfo;

/**
 * 制御タブ用のコンポジットクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class ControlComposite extends Composite {
	/** 保留用チェックボタン */
	private Button m_waitCondition = null;
	/** スキップ用チェックボタン */
	private Button m_skipCondition = null;
	/** スキップ終了状態用テキスト */
	private Combo m_skipEndStatus = null;
	/** スキップ終了値用テキスト */
	private Text m_skipEndValue = null;
	/** カレンダ用チェックボタン */
	private Button m_calendarCondition = null;
	/** カレンダID用コンボボックス */
	private CalendarIdListComposite m_calendarId = null;
	/** カレンダ終了状態用テキスト */
	private Combo m_calendarEndStatus = null;
	/** カレンダ終了値用テキスト */
	private Text m_calendarEndValue = null;
	/** ジョブ待ち条件情報 */
	private JobWaitRuleInfo m_waitRule = null;

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
	public ControlComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		this.setLayout(JobDialogUtil.getParentLayout());

		// カレンダ（ラジオ）
		this.m_calendarCondition = new Button(this, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_calendarCondition", this.m_calendarCondition);
		this.m_calendarCondition.setText(Messages.getString("calendar"));
		this.m_calendarCondition.setLayoutData(new RowData(220,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_calendarCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_calendarId.setEnabled(true);
					m_calendarEndStatus.setEnabled(true);
					m_calendarEndValue.setEditable(true);
				} else {
					m_calendarId.setEnabled(false);
					m_calendarEndStatus.setEnabled(false);
					m_calendarEndValue.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// カレンダ（Composite）
		Composite calendarConditionGroup = new Composite(this, SWT.BORDER);
		calendarConditionGroup.setLayout(new GridLayout(2, false));

		// カレンダ：カレンダID（ラベル）
		Label calendarIdTitle = new Label(calendarConditionGroup, SWT.LEFT);
		calendarIdTitle.setText(Messages.getString("calendar.id") + " : ");
		calendarIdTitle.setLayoutData(new GridData(80,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// カレンダ：カレンダID（コンボ）
		this.m_calendarId = new CalendarIdListComposite(calendarConditionGroup, SWT.NONE, false);
		WidgetTestUtil.setTestId(this, "m_calendarId", this.m_calendarId);
		m_calendarId.setLayoutData(new GridData());
		this.m_calendarId.setEnabled(false);
		((GridData)this.m_calendarId.getLayoutData()).widthHint = 350;

		// カレンダ：終了状態（ラベル）
		Label calendarEndStatusTitle = new Label(calendarConditionGroup, SWT.LEFT);
		calendarEndStatusTitle.setText(Messages.getString("end.status") + " : ");
		calendarEndStatusTitle.setLayoutData(new GridData(80,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// カレンダ：終了状態（コンボ）
		this.m_calendarEndStatus = new Combo(calendarConditionGroup, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_calendarEndStatus", this.m_calendarEndStatus);
		this.m_calendarEndStatus.setLayoutData(new GridData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_calendarEndStatus.add(EndStatusMessage.STRING_NORMAL);
		this.m_calendarEndStatus.add(EndStatusMessage.STRING_WARNING);
		this.m_calendarEndStatus.add(EndStatusMessage.STRING_ABNORMAL);

		// カレンダ：終了値（ラベル）
		Label calendarEndValueTitle = new Label(calendarConditionGroup, SWT.LEFT);
		calendarEndValueTitle.setText(Messages.getString("end.value") + " : ");
		calendarEndValueTitle.setLayoutData(new GridData(80,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// カレンダ：終了値（テキスト）
		this.m_calendarEndValue = new Text(calendarConditionGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_calendarEndValue", this.m_calendarEndValue);
		this.m_calendarEndValue.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_calendarEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_calendarEndValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// separator
		JobDialogUtil.getSeparator(this);

		// 保留（ラジオ）
		this.m_waitCondition = new Button(this, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_waitCondition", this.m_waitCondition);
		this.m_waitCondition.setText(Messages.getString("reserve"));
		this.m_waitCondition.setLayoutData(new RowData(200,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_waitCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_skipCondition.setSelection(false);
					m_skipEndStatus.setEnabled(false);
					m_skipEndValue.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// separator
		JobDialogUtil.getSeparator(this);

		// スキップ（ラジオ）
		this.m_skipCondition = new Button(this, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_skipCondition", this.m_skipCondition);
		this.m_skipCondition.setText(Messages.getString("skip"));
		this.m_skipCondition.setLayoutData(new RowData(200,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_skipCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_skipEndStatus.setEnabled(true);
					m_skipEndValue.setEditable(true);
					m_waitCondition.setSelection(false);
				} else {
					m_skipEndStatus.setEnabled(false);
					m_skipEndValue.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// スキップ（Composite）
		Composite skipEndConditionGroup = new Composite(this, SWT.BORDER);
		skipEndConditionGroup.setLayout(new GridLayout(2, false));

		// スキップ：終了状態（ラベル）
		Label skipEndStatusTitle = new Label(skipEndConditionGroup, SWT.LEFT);
		skipEndStatusTitle.setText(Messages.getString("end.status") + " : ");
		skipEndStatusTitle.setLayoutData(new GridData(80,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// スキップ：終了状態（コンボ）
		this.m_skipEndStatus = new Combo(skipEndConditionGroup, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_skipEndStatus", this.m_skipEndStatus);
		this.m_skipEndStatus.setLayoutData(new GridData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_skipEndStatus.add(EndStatusMessage.STRING_NORMAL);
		this.m_skipEndStatus.add(EndStatusMessage.STRING_WARNING);
		this.m_skipEndStatus.add(EndStatusMessage.STRING_ABNORMAL);

		// スキップ：終了値（ラベル）
		Label skipEndValueTitle = new Label(skipEndConditionGroup, SWT.LEFT);
		skipEndValueTitle.setText(Messages.getString("end.value") + " : ");
		skipEndValueTitle.setLayoutData(new GridData(80,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// スキップ：終了値（テキスト）
		this.m_skipEndValue = new Text(skipEndConditionGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_skipEndValue", this.m_skipEndValue);
		this.m_skipEndValue.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_skipEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_skipEndValue.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
				);
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目を明示
		if (m_calendarCondition.getSelection() && "".equals(this.m_calendarEndValue.getText())){
			this.m_calendarEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_calendarEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_skipCondition.getSelection() && "".equals(this.m_skipEndValue.getText())){
			this.m_skipEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_skipEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ジョブ待ち条件情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 */
	public void reflectWaitRuleInfo() {
		if (m_waitRule == null)
			throw new InternalError("JobWaitRuleInfo is null");

		//カレンダ
		m_calendarCondition.setSelection(m_waitRule.isCalendar());

		//カレンダID
		if (m_waitRule.getCalendarId() != null &&
				m_waitRule.getCalendarId().length() > 0) {
			m_calendarId.setText(m_waitRule.getCalendarId());
		}

		//カレンダの終了状態
		setSelectEndStatus(m_calendarEndStatus, m_waitRule.getCalendarEndStatus());
		//カレンダ未実行時の終了値
		m_calendarEndValue.setText(String.valueOf(m_waitRule.getCalendarEndValue()));
		//保留
		m_waitCondition.setSelection(m_waitRule.isSuspend());
		//スキップ
		m_skipCondition.setSelection(m_waitRule.isSkip());
		//スキップ終了値
		m_skipEndValue.setText(String.valueOf(m_waitRule.getSkipEndValue()));

		//カレンダ
		if (m_calendarCondition.getSelection()) {
			m_calendarId.setEnabled(true);
			m_calendarEndStatus.setEnabled(true);
			m_calendarEndValue.setEditable(true);
		} else {
			m_calendarId.setEnabled(false);
			m_calendarEndStatus.setEnabled(false);
			m_calendarEndValue.setEditable(false);
		}

		//保留
		if (m_waitCondition.getSelection()) {
			m_skipCondition.setSelection(false);
		}

		//スキップ
		if (m_skipCondition.getSelection()) {
			m_skipEndStatus.setEnabled(true);
			m_skipEndValue.setEditable(true);
			m_waitCondition.setSelection(false);
		} else {
			m_skipEndStatus.setEnabled(false);
			m_skipEndValue.setEditable(false);
		}

		//スキップの終了状態
		setSelectEndStatus(m_skipEndStatus, m_waitRule.getSkipEndStatus());
	}

	/**
	 * ジョブ待ち条件情報を設定します。
	 *
	 * @param start ジョブ待ち条件情報
	 */
	public void setWaitRuleInfo(JobWaitRuleInfo start) {
		m_waitRule = start;
	}

	/**
	 * ジョブ待ち条件情報を返します。
	 *
	 * @return ジョブ待ち条件情報
	 */
	public JobWaitRuleInfo getWaitRuleInfo() {
		return m_waitRule;
	}

	/**
	 * コンポジットの情報から、ジョブ待ち条件情報を作成する。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 */
	public ValidateResult createWaitRuleInfo() {
		ValidateResult result = null;

		//カレンダ
		m_waitRule.setCalendar(m_calendarCondition.getSelection());

		//カレンダID
		if (m_calendarId.getText().length() == 0){
			m_waitRule.setCalendarId(null);
		}
		if (m_calendarId.getText().length() > 0) {
			m_waitRule.setCalendarId(m_calendarId.getText());
		}
		else{
			if (m_waitRule.isCalendar().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.calendar.22"));
				return result;
			}
		}

		//カレンダ未実行時の終了値取得
		try {
			m_waitRule.setCalendarEndStatus(getSelectEndStatus(m_calendarEndStatus));
			m_waitRule.setCalendarEndValue(Integer.parseInt(m_calendarEndValue.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.isCalendar().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		}

		//保留
		m_waitRule.setSuspend(m_waitCondition.getSelection());

		//スキップ
		m_waitRule.setSkip(m_skipCondition.getSelection());

		//スキップ終了値取得
		try {
			m_waitRule.setSkipEndStatus(getSelectEndStatus(m_skipEndStatus));
			m_waitRule.setSkipEndValue(Integer.parseInt(m_skipEndValue.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.isSkip().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		}

		return null;
	}


	/**
	 *終了状態用コンボボックスにて選択している項目を取得します。
	 *
	 */
	private int getSelectEndStatus(Combo combo) {
		String select = combo.getText();
		return EndStatusMessage.stringToType(select);
	}

	/**
	 * 指定した重要度に該当する終了状態用コンボボックスの項目を選択します。
	 *
	 */
	private void setSelectEndStatus(Combo combo, int status) {
		String select = "";

		select = EndStatusMessage.typeToString(status);

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	public CalendarIdListComposite getCalendarId() {
		return m_calendarId;
	}

	public void setCalendarId(CalendarIdListComposite calendarId) {
		this.m_calendarId = calendarId;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_waitCondition.setEnabled(enabled);
		m_skipCondition.setEnabled(enabled);
		m_skipEndStatus.setEnabled(m_skipCondition.getSelection() && enabled);
		m_skipEndValue.setEditable(m_skipCondition.getSelection() && enabled);
		m_calendarCondition.setEnabled(enabled);
		m_calendarId.setEnabled(m_calendarCondition.getSelection() && enabled);
		m_calendarEndStatus.setEnabled(m_calendarCondition.getSelection() && enabled);
		m_calendarEndValue.setEditable(m_calendarCondition.getSelection() && enabled);
	}


}
