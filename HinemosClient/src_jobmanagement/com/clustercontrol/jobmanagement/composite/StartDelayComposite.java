/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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
import org.openapitools.client.model.JobWaitRuleInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.composite.action.PositiveNumberVerifyListener;
import com.clustercontrol.composite.action.TimeVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimeStringConverter;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 開始遅延タブ用のコンポジットクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class StartDelayComposite extends Composite {
	/** 開始遅延セッション開始後の時間用チェックボタン */
	private Button m_sessionCondition = null;
	/** 開始遅延セッション開始後の時間の値用テキスト*/
	private Text m_sessionValue = null;
	/** 開始遅延時刻用チェックボタン */
	private Button m_timeCondition = null;
	/** 開始遅延時刻の値用テキスト */
	private Text m_timeValue = null;
	/** 開始遅延判定対象の条件関係 AND用ラジオボタン */
	private Button m_andCondition = null;
	/** 開始遅延判定対象の条件関係 OR用ラジオボタン */
	private Button m_orCondition = null;
	/**  開始遅延通知用チェックボタン */
	private Button m_notifyCondition = null;
	/**  開始遅延通知重要度用コンボボックス */
	private Combo m_notifyPriority = null;
	/** 開始遅延操作用チェックボタン */
	private Button m_operationCondition = null;
	/** 開始遅延操作種別用コンボボックス */
	private Combo m_operationType = null;
	/** 開始遅延操作終了状態用テキスト */
	private Combo m_operationStatus = null;
	/** 開始遅延操作終了値用テキスト */
	private Text m_operationValue = null;
	/** 開始遅延用チェックボタン */
	private Button m_startDelayCondition = null;
	/** ジョブ待ち条件情報 */
	private JobWaitRuleInfoResponse m_waitRule = null;

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
	public StartDelayComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを構築します。
	 */
	private void initialize() {

		this.setLayout(JobDialogUtil.getParentLayout());

		// 開始遅延（チェック）
		this.m_startDelayCondition = new Button(JobDialogUtil.getComposite_MarginZero(this), SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_startDelayCondition", this.m_startDelayCondition);
		this.m_startDelayCondition.setText(Messages.getString("start.delay"));
		this.m_startDelayCondition.setLayoutData(new RowData(100,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_startDelayCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				//開始遅延コンポジットのオブジェクトの使用不可を設定
				setStartDelayEnabled(check.getSelection());
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 開始遅延（Composite）
		Composite startDelayConditionGroup = new Composite(this, SWT.BORDER);
		startDelayConditionGroup.setLayout(new GridLayout(2, false));

		// 開始遅延：判定対象一覧（グループ）
		Group startDelayGroup = new Group(startDelayConditionGroup, SWT.NONE);
		startDelayGroup.setText(Messages.getString("object.list"));
		startDelayGroup.setLayoutData(new GridData());
		((GridData)startDelayGroup.getLayoutData()).horizontalSpan = 2;
		startDelayGroup.setLayout(new GridLayout(2, false));
		
		// 開始遅延：判定対象一覧：セッション開始後の時間（チェック）
		this.m_sessionCondition = new Button(startDelayGroup, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_sessionCondition", this.m_sessionCondition);
		this.m_sessionCondition.setText(Messages.getString("time.after.session.start") + " : ");
		this.m_sessionCondition.setLayoutData(new GridData(250,
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

		// 開始遅延：判定対象一覧：セッション開始後の時間（テキスト）
		this.m_sessionValue = new Text(startDelayGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_sessionValue", this.m_sessionValue);
		this.m_sessionValue.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_sessionValue.addVerifyListener(
				new PositiveNumberVerifyListener(1, DataRangeConstant.SMALLINT_HIGH));
		this.m_sessionValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 開始遅延：判定対象一覧：時刻（チェック）
		this.m_timeCondition = new Button(startDelayGroup, SWT.CHECK);
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

		// 開始遅延：判定対象一覧：時刻（チェック）
		this.m_timeValue = new Text(startDelayGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_timeValue", this.m_timeValue);
		this.m_timeValue.setLayoutData(new GridData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_timeValue.addVerifyListener(new TimeVerifyListener());
		this.m_timeValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 開始遅延：判定対象の条件関係（グループ）
		Group startDelayBetweenGroup = new Group(startDelayConditionGroup, SWT.NONE);
		startDelayBetweenGroup.setText(Messages.getString("condition.between.objects"));
		startDelayBetweenGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridData)startDelayBetweenGroup.getLayoutData()).horizontalSpan = 2;
		startDelayBetweenGroup.setLayout(new RowLayout());

		// 開始遅延：判定対象の条件関係：AND（ラジオ）
		this.m_andCondition = new Button(startDelayBetweenGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_andCondition", this.m_andCondition);
		this.m_andCondition.setText(Messages.getString("and"));
		this.m_andCondition.setLayoutData(new RowData(60,
				SizeConstant.SIZE_BUTTON_HEIGHT));

		// ラジオボタン配置調整用の空Composite
		JobDialogUtil.getComposite_Space(startDelayBetweenGroup, 40, SizeConstant.SIZE_BUTTON_HEIGHT);

		// 開始遅延：判定対象の条件関係：OR（ラジオ）
		this.m_orCondition = new Button(startDelayBetweenGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_orCondition", this.m_orCondition);
		this.m_orCondition.setText(Messages.getString("or"));
		this.m_orCondition.setLayoutData(new RowData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));

		// 開始遅延：通知（チェック）
		this.m_notifyCondition = new Button(startDelayConditionGroup, SWT.CHECK);
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

		// 開始遅延：通知（コンボ）
		this.m_notifyPriority = new Combo(startDelayConditionGroup, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_notifyPriority", this.m_notifyPriority);
		this.m_notifyPriority.setLayoutData(new GridData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_notifyPriority.add(PriorityMessage.STRING_INFO);
		this.m_notifyPriority.add(PriorityMessage.STRING_WARNING);
		this.m_notifyPriority.add(PriorityMessage.STRING_CRITICAL);
		this.m_notifyPriority.add(PriorityMessage.STRING_UNKNOWN);

		// 開始遅延：操作（チェック）
		this.m_operationCondition = new Button(startDelayConditionGroup, SWT.CHECK);
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

					JobWaitRuleInfoResponse.StartDelayOperationTypeEnum type = getSelectOperationName(m_operationType);
					if (type == JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.SKIP) {
						m_operationStatus.setEnabled(true);
						m_operationValue.setEditable(true);
					} else if(type == JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.WAIT){
						m_operationStatus.setEnabled(false);
						m_operationValue.setEditable(false);
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

		// 開始遅延：操作（グループ）
		Composite operationConditionGroup = new Composite(startDelayConditionGroup, SWT.BORDER);
		operationConditionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		operationConditionGroup.setLayout(new GridLayout(2, false));

		// 開始遅延：操作：名前（ラベル）
		Label operationTypeLabel = new Label(operationConditionGroup, SWT.NONE);
		operationTypeLabel.setText(Messages.getString("name") + " : ");
		operationTypeLabel.setLayoutData(new GridData(100,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 開始遅延：操作：名前（コンボ）
		this.m_operationType = new Combo(operationConditionGroup, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_operationType", this.m_operationType);
		this.m_operationType.setLayoutData(new GridData(120,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_operationType.add(OperationMessage.STRING_STOP_SKIP);
		this.m_operationType.add(OperationMessage.STRING_STOP_WAIT);
		this.m_operationType.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo check = (Combo) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				JobWaitRuleInfoResponse.StartDelayOperationTypeEnum type = getSelectOperationName(check);
				if (type == JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.SKIP) {
					m_operationStatus.setEnabled(true);
					m_operationValue.setEditable(true);
				} else if(type == JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.WAIT){
					m_operationStatus.setEnabled(false);
					m_operationValue.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 開始遅延：操作：終了状態（ラベル）
		Label operationStatusLabel = new Label(operationConditionGroup, SWT.NONE);
		operationStatusLabel.setText(Messages.getString("end.status") + " : ");
		operationStatusLabel.setLayoutData(new GridData(100,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 開始遅延：操作：終了状態（コンボ）
		this.m_operationStatus = new Combo(operationConditionGroup, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_operationStatus", this.m_operationStatus);
		this.m_operationStatus.setLayoutData(new GridData(120,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_operationStatus.add(EndStatusMessage.STRING_NORMAL);
		this.m_operationStatus.add(EndStatusMessage.STRING_WARNING);
		this.m_operationStatus.add(EndStatusMessage.STRING_ABNORMAL);

		// 開始地点：操作：終了値（ラベル）
		Label operationValueLabel = new Label(operationConditionGroup, SWT.NONE);
		operationValueLabel.setText(Messages.getString("end.value") + " : ");
		operationValueLabel.setLayoutData(new GridData(100,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 開始地点：操作：終了値（テキスト）
		this.m_operationValue = new Text(operationConditionGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_operationValue", this.m_operationValue);
		this.m_operationValue.setLayoutData(new GridData(110,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_operationValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_operationValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
	}

	@Override
	public void update() {
		// 必須項目を明示
		if(m_startDelayCondition.getSelection() && m_sessionCondition.getSelection() &&
				"".equals(this.m_sessionValue.getText())){
			this.m_sessionValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_sessionValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_startDelayCondition.getSelection() && m_timeCondition.getSelection() &&
				"".equals(this.m_timeValue.getText())){
			this.m_timeValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_timeValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_startDelayCondition.getSelection() && m_operationCondition.getSelection() &&
				getSelectOperationName(m_operationType) == JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.SKIP &&
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
			//開始遅延
			m_startDelayCondition.setSelection(m_waitRule.getStartDelay());

			//セッション開始後の時間
			m_sessionCondition.setSelection(m_waitRule.getStartDelaySession());

			//セッション開始後の時間の値
			m_sessionValue.setText(String.valueOf(m_waitRule.getStartDelaySessionValue()));

			//時刻
			m_timeCondition.setSelection(m_waitRule.getStartDelayTime());

			//時刻の値
			if (m_waitRule.getStartDelayTimeValue() != null) {
				//表示形式を0時未満および24時(及び48時)超にも対応する
				m_timeValue.setText(m_waitRule.getStartDelayTimeValue());
			}
			else{
				m_timeValue.setText("");
			}

			//条件関係設定
			if (m_waitRule.getStartDelayConditionType() == JobWaitRuleInfoResponse.StartDelayConditionTypeEnum.AND) {
				m_andCondition.setSelection(true);
				m_orCondition.setSelection(false);
			} else {
				m_andCondition.setSelection(false);
				m_orCondition.setSelection(true);
			}

			//通知
			m_notifyCondition.setSelection(m_waitRule.getStartDelayNotify());

			//通知の重要度
			setSelectPriority(m_notifyPriority,
					m_waitRule.getStartDelayNotifyPriority());

			//操作
			m_operationCondition.setSelection(m_waitRule.getStartDelayOperation());

			//操作の名前
			setSelectOperationName(m_operationType,
					m_waitRule.getStartDelayOperationType());

			//操作の終了状態
			setSelectOperationEndStatus(m_operationStatus,
					m_waitRule.getStartDelayOperationEndStatus());

			//操作の終了値
			m_operationValue.setText(String.valueOf(m_waitRule.getStartDelayOperationEndValue()));
		}

		//開始遅延コンポジットのオブジェクトの使用不可を設定
		setStartDelayEnabled(m_startDelayCondition.getSelection());
	}

	/**
	 * ジョブ待ち条件情報を設定します。
	 *
	 * @param start ジョブ待ち条件情報
	 */
	public void setWaitRuleInfo(JobWaitRuleInfoResponse start) {
		m_waitRule = start;
	}

	/**
	 * ジョブ待ち条件情報を返します。
	 *
	 * @return ジョブ待ち条件情報
	 */
	public JobWaitRuleInfoResponse getWaitRuleInfo() {
		return m_waitRule;
	}

	/**
	 * コンポジットの情報から、ジョブ待ち条件情報を作成します。
	 *
	 * @return 入力値の検証結果
	 */
	public ValidateResult createWaitRuleInfo() {
		ValidateResult result = null;

		//開始遅延
		m_waitRule.setStartDelay(m_startDelayCondition.getSelection());

		//セッション開始後の時間
		m_waitRule.setStartDelaySession(m_sessionCondition.getSelection());

		//セッション開始後の時間の値
		try {
			m_waitRule.setStartDelaySessionValue(
					Integer.parseInt(m_sessionValue.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.getStartDelaySession().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.52"));
				return result;
			}
		}

		//時刻
		m_waitRule.setStartDelayTime(m_timeCondition.getSelection());

		//時刻の値
		if (m_waitRule.getStartDelayTime().booleanValue()) {
			boolean check = false;
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
			formatter.setTimeZone(TimezoneUtil.getTimeZone());
			try {
				//0時未満および24時(及び48時)超の文字列指定にも対応する
				TimeStringConverter.parseTime(m_timeValue.getText());
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
				m_waitRule.setStartDelayTimeValue(m_timeValue.getText());
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
			m_waitRule.setStartDelayConditionType(JobWaitRuleInfoResponse.StartDelayConditionTypeEnum.AND);
		} else {
			m_waitRule.setStartDelayConditionType(JobWaitRuleInfoResponse.StartDelayConditionTypeEnum.OR);
		}

		//通知
		m_waitRule.setStartDelayNotify(m_notifyCondition.getSelection());

		//通知の重要度
		m_waitRule.setStartDelayNotifyPriority(getSelectPriority(m_notifyPriority));

		//操作
		m_waitRule.setStartDelayOperation(m_operationCondition.getSelection());

		//操作の名前
		m_waitRule.setStartDelayOperationType(getSelectOperationName(m_operationType));

		//操作の終了値
		try {
			// 開始遅延が有効であり、操作がスキップの場合のみ、入力されている終了値を確認する
			m_waitRule.setStartDelayOperationEndStatus(getSelectOperationEndStatus(m_operationStatus));
			m_waitRule.setStartDelayOperationEndValue(Integer.parseInt(m_operationValue.getText()));
		} catch (NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.21"));
			return result;
		}

		return null;
	}
	
	/**
	 * コンポジットに配置したオブジェクトの使用可・使用不可を設定します。
	 *
	 * @param enabled true：開始遅延を使用する、false：開始遅延を使用しない
	 */
	private void setStartDelayEnabled(boolean enabled) {
		if (enabled) {

			//セッション開始後の時間
			m_sessionCondition.setEnabled(true);
			if(m_sessionCondition.getSelection())
				m_sessionValue.setEditable(true);
			else
				m_sessionValue.setEditable(false);

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

				JobWaitRuleInfoResponse.StartDelayOperationTypeEnum type = getSelectOperationName(m_operationType);
				if (type == JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.SKIP) {
					m_operationStatus.setEnabled(true);
					m_operationValue.setEditable(true);
				} else if(type == JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.WAIT){
					m_operationStatus.setEnabled(false);
					m_operationValue.setEditable(false);
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
	 * 指定した重要度に該当する開始遅延通知重要度用コンボボックスの項目を選択します。
	 *
	 * @param combo 開始遅延通知重要度用コンボボックスのインスタンス
	 * @param priority 重要度
	 *
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	private void setSelectPriority(Combo combo, JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum enumValue) {
		String select = "";
		if (enumValue == JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum.CRITICAL) {
			select = PriorityMessage.STRING_CRITICAL;
		} else if (enumValue == JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum.WARNING) {
			select = PriorityMessage.STRING_WARNING;
		} else if (enumValue == JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum.INFO) {
			select = PriorityMessage.STRING_INFO;
		} else if (enumValue == JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum.UNKNOWN) {
			select = PriorityMessage.STRING_UNKNOWN;
//		} else if (enumValue == JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum.NONE) {
//			select = PriorityMessage.STRING_NONE;
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
	 * 開始遅延通知重要度用コンボボックスにて選択している重要度を取得します。
	 *
	 * @param combo 開始遅延通知重要度用コンボボックスのインスタンス
	 * @return 重要度
	 *
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	private JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum getSelectPriority(Combo combo) {
		String select = combo.getText();

		if (select.equals(PriorityMessage.STRING_CRITICAL)) {
			return JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum.CRITICAL;
		} else if (select.equals(PriorityMessage.STRING_WARNING)) {
			return JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum.WARNING;
		} else if (select.equals(PriorityMessage.STRING_INFO)) {
			return JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum.INFO;
		} else if (select.equals(PriorityMessage.STRING_UNKNOWN)) {
			return JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum.UNKNOWN;
//		} else if (select.equals(PriorityMessage.STRING_NONE)) {
//			return JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum.NONE;
		}

		return null;
	}

	/**
	 * 指定したジョブ操作種別に該当する開始遅延操作用コンボボックスの項目を選択します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @param operation ジョブ操作種別
	 *
	 * @see com.clustercontrol.jobmanagement.bean.OperationConstant
	 */
	private void setSelectOperationName(Combo combo, JobWaitRuleInfoResponse.StartDelayOperationTypeEnum operation) {
		String select = "";

		if (operation == JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.SKIP) {
			select = OperationMessage.STRING_STOP_SKIP;
		} else if (operation == JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.WAIT) {
			select = OperationMessage.STRING_STOP_WAIT;
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
	 * 開始遅延操作用コンボボックスにて選択しているジョブ操作種別を取得します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @return ジョブ操作種別
	 *
	 * @see com.clustercontrol.jobmanagement.bean.OperationConstant
	 */
	private JobWaitRuleInfoResponse.StartDelayOperationTypeEnum getSelectOperationName(Combo combo) {
		String select = combo.getText();

		if (select.equals(OperationMessage.STRING_STOP_SKIP)) {
			return JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.SKIP;
		} else if (select.equals(OperationMessage.STRING_STOP_WAIT)) {
			return JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.WAIT;
		}

		return null;
	}

	/**
	 * 指定した重要度に該当する開始遅延終了状態用コンボボックスの項目を選択します。
	 *
	 */
	private void setSelectOperationEndStatus(Combo combo, JobWaitRuleInfoResponse.StartDelayOperationEndStatusEnum status) {
		String select = "";

		select = EndStatusMessage.typeEnumValueToString(status.getValue());

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * 開始遅延通知終了状態用コンボボックスにて選択している項目を取得します。
	 *
	 */
	private JobWaitRuleInfoResponse.StartDelayOperationEndStatusEnum getSelectOperationEndStatus(Combo combo) {
		String select = combo.getText();
		String enmuValue = EndStatusMessage.stringTotypeEnumValue(select);
		return JobWaitRuleInfoResponse.StartDelayOperationEndStatusEnum.fromValue(enmuValue) ;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_startDelayCondition.setEnabled(enabled);
		if (!m_startDelayCondition.getSelection()) {
			enabled = false;
		}
		m_sessionCondition.setEnabled(enabled);
		m_sessionValue.setEditable(m_sessionCondition.getSelection() && enabled);
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
