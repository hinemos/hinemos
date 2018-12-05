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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.composite.action.PositiveNumberVerifyListener;
import com.clustercontrol.composite.action.TimeVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.bean.ConditionTypeConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimeStringConverter;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.JobWaitRuleInfo;

/**
 * 終了遅延タブ用のコンポジットクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class EndDelayComposite extends Composite {
	/** 終了遅延セッション開始後の時間用チェックボタン */
	private Button m_sessionCondition = null;
	/** 終了遅延セッション開始後の時間の値用テキスト*/
	private Text m_sessionValue = null;
	/** 終了遅延ジョブ開始後の時間用チェックボタン */
	private Button m_jobCondition = null;
	/** 終了遅延ジョブ開始後の時間の値用テキスト */
	private Text m_jobValue = null;
	/** 終了遅延時刻用チェックボタン */
	private Button m_timeCondition = null;
	/** 終了遅延時刻の値用テキスト */
	private Text m_timeValue = null;
	/** 終了遅延判定対象の条件関係 AND用ラジオボタン */
	private Button m_andCondition = null;
	/** 終了遅延判定対象の条件関係 OR用ラジオボタン */
	private Button m_orCondition = null;
	/**  終了遅延通知用チェックボタン */
	private Button m_notifyCondition = null;
	/**  終了遅延通知重要度用コンボボックス */
	private Combo m_notifyPriority = null;
	/** 終了遅延操作用チェックボタン */
	private Button m_operationCondition = null;
	/** 終了遅延操作種別用コンボボックス */
	private Combo m_operationType = null;
	/** 終了遅延操作終了値用テキスト */
	private Combo m_operationStatus = null;
	/** 終了遅延操作終了値用テキスト */
	private Text m_operationValue = null;
	/** 終了遅延用チェックボタン */
	private Button m_endDelayCondition = null;
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
	public EndDelayComposite(Composite parent, int style, boolean isFileJob) {
		super(parent, style);
		initialize(isFileJob);
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize(boolean isFileJob) {

		this.setLayout(JobDialogUtil.getParentLayout());

		// 終了遅延（チェック）
		this.m_endDelayCondition = new Button(this, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_endDelayCondition", this.m_endDelayCondition);
		this.m_endDelayCondition.setText(Messages.getString("end.delay"));
		this.m_endDelayCondition.setLayoutData(new RowData(200,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_endDelayCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				//終了遅延コンポジットのオブジェクトの使用不可を設定
				setEndDelayEnabled(check.getSelection());
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 終了遅延（Composite）
		Composite endDelayConditionGroup = new Composite(this, SWT.BORDER);
		endDelayConditionGroup.setLayout(new GridLayout(2, false));

		// 終了遅延：判定対象一覧（グループ）
		Group endDelayGroup = new Group(endDelayConditionGroup, SWT.NONE);
		endDelayGroup.setText(Messages.getString("object.list"));
		endDelayGroup.setLayoutData(new GridData());
		((GridData)endDelayGroup.getLayoutData()).horizontalSpan = 2;
		endDelayGroup.setLayout(new GridLayout(2, false));

		// 終了遅延：判定対象一覧：セッション開始後の時間（チェック）
		this.m_sessionCondition = new Button(endDelayGroup, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_sessionCondition", this.m_sessionCondition);
		this.m_sessionCondition.setText(Messages.getString("time.after.session.start") + " : ");
		this.m_sessionCondition.setLayoutData(new GridData(230,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_sessionCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_sessionValue.setEditable(true);
				} else {
					m_sessionValue.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		// 終了遅延：判定対象一覧：セッション開始後の時間（テキスト）
		this.m_sessionValue = new Text(endDelayGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this,"m_sessionValue", this.m_sessionValue);
		this.m_sessionValue.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_sessionValue.addVerifyListener(
				new PositiveNumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));
		this.m_sessionValue.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
				);

		// 終了遅延：判定対象一覧：ジョブ開始後の時間（チェック）
		this.m_jobCondition = new Button(endDelayGroup, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_jobCondition", this.m_jobCondition);
		this.m_jobCondition.setText(Messages.getString("time.after.job.start") + " : ");
		this.m_jobCondition.setLayoutData(new GridData(230,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_jobCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_jobValue.setEditable(true);
				} else {
					m_jobValue.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 終了遅延：判定対象一覧：ジョブ開始後の時間（テキスト）
		this.m_jobValue = new Text(endDelayGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_jobValue", this.m_jobValue);
		this.m_jobValue.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_jobValue.addVerifyListener(
				new PositiveNumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));
		this.m_jobValue.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
				);

		// 終了遅延：判定対象一覧：時刻（チェック）
		this.m_timeCondition = new Button(endDelayGroup, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_timeCondition", this.m_timeCondition);
		this.m_timeCondition.setText(Messages.getString("wait.rule.time.example") + " : ");
		this.m_timeCondition.setLayoutData(new GridData(230,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_timeCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_timeValue.setEditable(true);
				} else {
					m_timeValue.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 終了遅延：判定対象一覧：時刻（テキスト）
		this.m_timeValue = new Text(endDelayGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_timeValue", this.m_timeValue);
		this.m_timeValue.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_timeValue.addVerifyListener(new TimeVerifyListener());
		this.m_timeValue.addModifyListener(	new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 終了遅延：判定対象の条件関係（グループ）
		Group endDelayBetweenGroup = new Group(endDelayConditionGroup, SWT.NONE);
		endDelayBetweenGroup.setText(Messages.getString("condition.between.objects"));
		endDelayBetweenGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridData)endDelayBetweenGroup.getLayoutData()).horizontalSpan = 2;
		endDelayBetweenGroup.setLayout(new RowLayout());

		// 終了遅延：判定対象の条件関係：AND（ラジオ）
		this.m_andCondition = new Button(endDelayBetweenGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_andCondition", this.m_andCondition);
		this.m_andCondition.setText(Messages.getString("and"));
		this.m_andCondition.setLayoutData(new RowData(100,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		
		// 終了遅延：判定対象の条件関係：OR（ラジオ）
		this.m_orCondition = new Button(endDelayBetweenGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_orCondition", this.m_orCondition);
		this.m_orCondition.setText(Messages.getString("or"));
		this.m_orCondition.setLayoutData(new RowData(100,
				SizeConstant.SIZE_BUTTON_HEIGHT));

		// 終了遅延：通知（チェック）
		this.m_notifyCondition = new Button(endDelayConditionGroup, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_notifyCondition", this.m_notifyCondition);
		this.m_notifyCondition.setText(Messages.getString("notify.attribute") + " : ");
		this.m_notifyCondition.setLayoutData(new GridData(100,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_notifyCondition.getLayoutData()).verticalAlignment = SWT.TOP;
		this.m_notifyCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_notifyPriority.setEnabled(true);
				} else {
					m_notifyPriority.setEnabled(false);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 終了遅延：通知（コンボ）
		this.m_notifyPriority = new Combo(endDelayConditionGroup, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_notifyPriority", this.m_notifyPriority);
		this.m_notifyPriority.setLayoutData(new GridData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_notifyPriority.add(PriorityMessage.STRING_INFO);
		this.m_notifyPriority.add(PriorityMessage.STRING_WARNING);
		this.m_notifyPriority.add(PriorityMessage.STRING_CRITICAL);
		this.m_notifyPriority.add(PriorityMessage.STRING_UNKNOWN);

		// 終了遅延：操作（チェック）
		this.m_operationCondition = new Button(endDelayConditionGroup, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_operationCondition", this.m_operationCondition);
		this.m_operationCondition.setText(Messages.getString("operations"));
		this.m_operationCondition.setLayoutData(new GridData(100,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_operationCondition.getLayoutData()).verticalAlignment = SWT.TOP;
		this.m_operationCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_operationType.setEnabled(true);

					int type = getSelectOperationName(m_operationType);
					if (type == OperationConstant.TYPE_STOP_AT_ONCE) {
						m_operationStatus.setEnabled(false);
						m_operationValue.setEditable(false);
					} else if(type == OperationConstant.TYPE_STOP_SUSPEND){
						m_operationStatus.setEnabled(false);
						m_operationValue.setEditable(false);
					} else if(type == OperationConstant.TYPE_STOP_SET_END_VALUE){
						m_operationStatus.setEnabled(true);
						m_operationValue.setEditable(true);
					}
				} else {
					m_operationType.setEnabled(false);
					m_operationStatus.setEnabled(false);
					m_operationValue.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 終了遅延：操作（Composite）
		Composite operationConditionGroup = new Composite(endDelayConditionGroup, SWT.BORDER);
		operationConditionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		operationConditionGroup.setLayout(new GridLayout(2, false));

		// 終了遅延：操作：名前（ラベル）
		Label operationTypeLabel = new Label(operationConditionGroup, SWT.NONE);
		operationTypeLabel.setText(Messages.getString("name") + " : ");
		operationTypeLabel.setLayoutData(new GridData(100,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// 終了遅延：操作：名前（コンボ）
		this.m_operationType = new Combo(operationConditionGroup, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_operationType", this.m_operationType);
		this.m_operationType.setLayoutData(new GridData(120,
				SizeConstant.SIZE_COMBO_HEIGHT));
		if(!isFileJob){
			this.m_operationType.add(OperationMessage.STRING_STOP_AT_ONCE);
		}
		this.m_operationType.add(OperationMessage.STRING_STOP_SUSPEND);
		this.m_operationType.add(OperationMessage.STRING_STOP_SET_END_VALUE);
		this.m_operationType.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo check = (Combo) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				int type = getSelectOperationName(check);
				if (type == OperationConstant.TYPE_STOP_AT_ONCE) {
					m_operationStatus.setEnabled(false);
					m_operationValue.setEditable(false);
				} else if(type == OperationConstant.TYPE_STOP_SUSPEND){
					m_operationStatus.setEnabled(false);
					m_operationValue.setEditable(false);
				} else if(type == OperationConstant.TYPE_STOP_SET_END_VALUE){
					m_operationStatus.setEnabled(true);
					m_operationValue.setEditable(true);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 終了遅延：操作：終了状態（ラベル）
		Label operationStatusLabel = new Label(operationConditionGroup, SWT.NONE);
		operationStatusLabel.setText(Messages.getString("end.status") + " : ");
		operationStatusLabel.setLayoutData(new GridData(100,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// 終了遅延：操作：終了状態（コンボ）
		this.m_operationStatus = new Combo(operationConditionGroup, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_operationStatus", this.m_operationStatus);
		this.m_operationStatus.setLayoutData(new GridData(120,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_operationStatus.add(EndStatusMessage.STRING_NORMAL);
		this.m_operationStatus.add(EndStatusMessage.STRING_WARNING);
		this.m_operationStatus.add(EndStatusMessage.STRING_ABNORMAL);

		// 終了遅延：操作：終了値（ラベル）
		Label operationValueLabel = new Label(operationConditionGroup, SWT.NONE);
		operationValueLabel.setText(Messages.getString("end.value") + " : ");
		operationValueLabel.setLayoutData(new GridData(100,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 終了遅延：操作：終了値（テキスト）
		this.m_operationValue = new Text(operationConditionGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_operationValue", this.m_operationValue);
		this.m_operationValue.setLayoutData(new GridData(110,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_operationValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_operationValue.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
				);
	}

	@Override
	public void update() {
		// 必須項目を明示
		if(m_endDelayCondition.getSelection() && m_sessionCondition.getSelection() &&
				"".equals(this.m_sessionValue.getText())){
			this.m_sessionValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_sessionValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_endDelayCondition.getSelection() && m_jobCondition.getSelection() &&
				"".equals(this.m_jobValue.getText())){
			this.m_jobValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_jobValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_endDelayCondition.getSelection() && m_timeCondition.getSelection() &&
				"".equals(this.m_timeValue.getText())){
			this.m_timeValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_timeValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_endDelayCondition.getSelection() && m_operationCondition.getSelection() &&
				getSelectOperationName(m_operationType) == OperationConstant.TYPE_STOP_SET_END_VALUE &&
				"".equals(this.m_operationValue.getText())){
			this.m_operationValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_operationValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ジョブ待ち条件情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 */
	public void reflectWaitRuleInfo() {
		if (m_waitRule != null) {
			//終了遅延
			m_endDelayCondition.setSelection(m_waitRule.isEndDelay());

			//セッション開始後の時間
			m_sessionCondition.setSelection(m_waitRule.isEndDelaySession());

			//セッション開始後の時間の値
			m_sessionValue.setText(String.valueOf(m_waitRule.getEndDelaySessionValue()));

			//ジョブ開始後の時間
			m_jobCondition.setSelection(m_waitRule.isEndDelayJob());

			//ジョブ開始後の時間の値
			m_jobValue.setText(String.valueOf(m_waitRule.getEndDelayJobValue()));

			//時刻
			m_timeCondition.setSelection(m_waitRule.isEndDelayTime());

			//時刻の値
			if (m_waitRule.getEndDelayTimeValue() != null) {
				//表示形式を0時未満および24時(及び48時)超にも対応する
				m_timeValue.setText(TimeStringConverter.formatTime(new Date(m_waitRule.getEndDelayTimeValue())));
			}
			else{
				m_timeValue.setText("");
			}

			//条件関係設定
			if (m_waitRule.getEndDelayConditionType() == ConditionTypeConstant.TYPE_AND) {
				m_andCondition.setSelection(true);
				m_orCondition.setSelection(false);
			} else {
				m_andCondition.setSelection(false);
				m_orCondition.setSelection(true);
			}

			//通知
			m_notifyCondition.setSelection(m_waitRule.isEndDelayNotify());

			//通知の重要度
			setSelectPriority(m_notifyPriority,
					m_waitRule.getEndDelayNotifyPriority());

			//操作
			m_operationCondition.setSelection(m_waitRule.isEndDelayOperation());

			//操作の名前
			setSelectOperationName(m_operationType,
					m_waitRule.getEndDelayOperationType());

			//操作の終了状態
			setSelectOperationEndStatus(m_operationStatus, m_waitRule.getEndDelayOperationEndStatus());

			//操作の終了値
			m_operationValue.setText(String.valueOf(m_waitRule.getEndDelayOperationEndValue()));
		}

		//終了遅延コンポジットのオブジェクトの使用不可を設定
		setEndDelayEnabled(m_endDelayCondition.getSelection());
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

		//終了遅延
		m_waitRule.setEndDelay(m_endDelayCondition.getSelection());

		//セッション開始後の時間
		m_waitRule.setEndDelaySession(m_sessionCondition.getSelection());

		//セッション開始後の時間の値
		try {
			m_waitRule.setEndDelaySessionValue(
					Integer.parseInt(m_sessionValue.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.isEndDelaySession().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.52"));
				return result;
			}
		}

		//ジョブ開始後の時間
		m_waitRule.setEndDelayJob(m_jobCondition.getSelection());

		//ジョブ開始後の時間の値
		try {
			m_waitRule.setEndDelayJobValue(
					Integer.parseInt(m_jobValue.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.isEndDelayJob().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.53"));
				return result;
			}
		}

		//時刻
		m_waitRule.setEndDelayTime(m_timeCondition.getSelection());

		//時刻の値
		if (m_waitRule.isEndDelayTime().booleanValue()) {
			boolean check = false;
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
			formatter.setTimeZone(TimezoneUtil.getTimeZone());
			Date date = null;
			try {
				//0時未満および24時(及び48時)超の文字列指定にも対応する
				date = TimeStringConverter.parseTime(m_timeValue.getText());
				check = true;
			} catch (ParseException e) {
//カレンダと同様の変換処理とするため、追加変換は行わない
//				formatter = new SimpleDateFormat("HH:mm");
//				formatter.setTimeZone(TimezoneUtil.getTimeZone());
//				try {
//					date = formatter.parse(m_timeValue.getText());
//					check = true;
//				} catch (ParseException e1) {
//				}
			}

			if(check){
				m_waitRule.setEndDelayTimeValue(date.getTime());
			}
			else{
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.hinemos.6"));
				return result;
			}
		}

		//条件関係取得
		if (m_andCondition.getSelection()) {
			m_waitRule.setEndDelayConditionType(ConditionTypeConstant.TYPE_AND);
		} else {
			m_waitRule.setEndDelayConditionType(ConditionTypeConstant.TYPE_OR);
		}

		//通知
		m_waitRule.setEndDelayNotify(m_notifyCondition.getSelection());

		//通知の重要度
		m_waitRule.setEndDelayNotifyPriority(getSelectPriority(m_notifyPriority));

		//操作
		m_waitRule.setEndDelayOperation(m_operationCondition.getSelection());

		//操作の名前
		m_waitRule.setEndDelayOperationType(getSelectOperationName(m_operationType));

		//操作の終了値
		try {
			m_waitRule.setEndDelayOperationEndStatus(getSelectOperationEndStatus(m_operationStatus));
			m_waitRule.setEndDelayOperationEndValue(
					Integer.parseInt(m_operationValue.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.isEndDelayOperation().booleanValue()) {
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
	 * コンポジットに配置したオブジェクトの使用可・使用不可を設定します。
	 *
	 * @param enabled true：終了遅延を使用する、false：終了遅延を使用しない
	 */
	private void setEndDelayEnabled(boolean enabled) {
		if (enabled) {

			//セッション開始後の時間
			m_sessionCondition.setEnabled(true);
			if(m_sessionCondition.getSelection())
				m_sessionValue.setEditable(true);
			else
				m_sessionValue.setEditable(false);

			//ジョブ開始後の時間
			m_jobCondition.setEnabled(true);
			if(m_jobCondition.getSelection())
				m_jobValue.setEditable(true);
			else
				m_jobValue.setEditable(false);

			//時刻
			m_timeCondition.setEnabled(true);
			if(m_timeCondition.getSelection())
				m_timeValue.setEditable(true);
			else
				m_timeValue.setEditable(false);

			//判定条件
			m_andCondition.setEnabled(true);
			m_orCondition.setEnabled(true);

			//通知
			m_notifyCondition.setEnabled(true);
			if(m_notifyCondition.getSelection())
				m_notifyPriority.setEnabled(true);
			else
				m_notifyPriority.setEnabled(false);

			//操作
			m_operationCondition.setEnabled(true);
			if(m_operationCondition.getSelection()){
				m_operationType.setEnabled(true);
				m_operationStatus.setEnabled(false);
				m_operationValue.setEditable(false);

				int type = getSelectOperationName(m_operationType);
				if (type == OperationConstant.TYPE_STOP_AT_ONCE) {
					m_operationStatus.setEnabled(false);
					m_operationValue.setEditable(false);
				} else if(type == OperationConstant.TYPE_STOP_SUSPEND){
					m_operationStatus.setEnabled(false);
					m_operationValue.setEditable(false);
				} else if(type == OperationConstant.TYPE_STOP_SET_END_VALUE){
					m_operationStatus.setEnabled(true);
					m_operationValue.setEditable(true);
				}
			}
			else{
				m_operationType.setEnabled(false);
				m_operationStatus.setEnabled(false);
				m_operationValue.setEditable(false);
			}
		} else {

			//セッション開始後の時間
			m_sessionCondition.setEnabled(false);
			m_sessionValue.setEditable(false);

			//ジョブ開始後の時間
			m_jobCondition.setEnabled(false);
			m_jobValue.setEditable(false);

			//時刻
			m_timeCondition.setEnabled(false);
			m_timeValue.setEditable(false);

			//判定条件
			m_andCondition.setEnabled(false);
			m_orCondition.setEnabled(false);

			//通知
			m_notifyCondition.setEnabled(false);
			m_notifyPriority.setEnabled(false);

			//操作
			m_operationCondition.setEnabled(false);
			m_operationType.setEnabled(false);
			m_operationStatus.setEnabled(false);
			m_operationValue.setEditable(false);
		}
	}

	/**
	 * 指定した重要度に該当する終了遅延通知重要度用コンボボックスの項目を選択します。
	 *
	 * @param combo 終了遅延通知重要度用コンボボックスのインスタンス
	 * @param priority 重要度
	 *
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	private void setSelectPriority(Combo combo, int priority) {
		String select = "";

		if (priority == PriorityConstant.TYPE_CRITICAL) {
			select = PriorityMessage.STRING_CRITICAL;
		} else if (priority == PriorityConstant.TYPE_WARNING) {
			select = PriorityMessage.STRING_WARNING;
		} else if (priority == PriorityConstant.TYPE_INFO) {
			select = PriorityMessage.STRING_INFO;
		} else if (priority == PriorityConstant.TYPE_UNKNOWN) {
			select = PriorityMessage.STRING_UNKNOWN;
		} else if (priority == PriorityConstant.TYPE_NONE) {
			select = PriorityMessage.STRING_NONE;
		}

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * 終了遅延通知重要度用コンボボックスにて選択している重要度を取得します。
	 *
	 * @param combo 終了遅延通知重要度用コンボボックスのインスタンス
	 * @return 重要度
	 *
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	private int getSelectPriority(Combo combo) {
		String select = combo.getText();

		if (select.equals(PriorityMessage.STRING_CRITICAL)) {
			return PriorityConstant.TYPE_CRITICAL;
		} else if (select.equals(PriorityMessage.STRING_WARNING)) {
			return PriorityConstant.TYPE_WARNING;
		} else if (select.equals(PriorityMessage.STRING_INFO)) {
			return PriorityConstant.TYPE_INFO;
		} else if (select.equals(PriorityMessage.STRING_UNKNOWN)) {
			return PriorityConstant.TYPE_UNKNOWN;
		} else if (select.equals(PriorityMessage.STRING_NONE)) {
			return PriorityConstant.TYPE_NONE;
		}

		return -1;
	}

	/**
	 * 指定したジョブ操作種別に該当する終了遅延操作用コンボボックスの項目を選択しまう。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @param operation ジョブ操作種別
	 *
	 * @see com.clustercontrol.jobmanagement.bean.OperationConstant
	 */
	private void setSelectOperationName(Combo combo, int operation) {
		String select = "";

		if (operation == OperationConstant.TYPE_STOP_AT_ONCE) {
			select = OperationMessage.STRING_START_AT_ONCE;
		} else if (operation == OperationConstant.TYPE_STOP_SUSPEND) {
			select = OperationMessage.STRING_STOP_SUSPEND;
		} else if (operation == OperationConstant.TYPE_STOP_SET_END_VALUE) {
			select = OperationMessage.STRING_STOP_SET_END_VALUE;
		}

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * 終了遅延操作用コンボボックスにて選択しているジョブ操作種別を取得します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @return ジョブ操作種別
	 *
	 * @see com.clustercontrol.jobmanagement.bean.OperationConstant
	 */
	private int getSelectOperationName(Combo combo) {
		String select = combo.getText();

		if (select.equals(OperationMessage.STRING_STOP_AT_ONCE)) {
			return OperationConstant.TYPE_STOP_AT_ONCE;
		} else if (select.equals(OperationMessage.STRING_STOP_SUSPEND)) {
			return OperationConstant.TYPE_STOP_SUSPEND;
		} else if (select.equals(OperationMessage.STRING_STOP_SET_END_VALUE)) {
			return OperationConstant.TYPE_STOP_SET_END_VALUE;
		}

		return -1;
	}

	/**
	 * 指定した重要度に該当する終了遅延終了状態用コンボボックスの項目を選択します。
	 *
	 */
	private void setSelectOperationEndStatus(Combo combo, int status) {
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

	/**
	 * 終了遅延通知終了状態用コンボボックスにて選択している項目を取得します。
	 *
	 */
	private int getSelectOperationEndStatus(Combo combo) {
		String select = combo.getText();
		return EndStatusMessage.stringToType(select);
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_endDelayCondition.setEnabled(enabled);
		if (!m_endDelayCondition.getSelection()) {
			enabled = false;
		}
		m_sessionCondition.setEnabled(enabled);
		m_sessionValue.setEditable(m_sessionCondition.getSelection() && enabled);
		m_jobCondition.setEnabled(enabled);
		m_jobValue.setEditable(m_jobCondition.getSelection() && enabled);
		m_timeCondition.setEnabled(enabled);
		m_timeValue.setEditable(m_timeCondition.getSelection() && enabled);
		m_andCondition.setEnabled(enabled);
		m_orCondition.setEnabled(enabled);
		m_notifyCondition.setEnabled(enabled);
		m_notifyPriority.setEnabled(m_notifyCondition.getSelection() && enabled);
		m_operationCondition.setEnabled(enabled);
		m_operationType.setEnabled(m_operationCondition.getSelection() && enabled);
		m_operationStatus.setEnabled(m_operationCondition.getSelection() && enabled);
		m_operationValue.setEditable(m_operationCondition.getSelection() && enabled);
	}
}
