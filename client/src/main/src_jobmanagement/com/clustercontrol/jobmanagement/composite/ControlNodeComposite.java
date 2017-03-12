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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.composite.action.PositiveNumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.JobCommandInfo;
import com.clustercontrol.ws.jobmanagement.JobFileInfo;
import com.clustercontrol.ws.jobmanagement.JobWaitRuleInfo;

/**
 *制御(ノード)タブ用のコンポジットクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class ControlNodeComposite extends Composite {

	/** 待機用ラジオボタン */
	private Button m_waitCondition = null;
	/** 停止用ラジオボタン */
	private Button m_endCondition = null;
	/** 停止時の終了値用テキスト */
	private Text m_endValue = null;

	/**  通知用チェックボタン */
	private Button m_notifyCondition = null;
	/**  通知重要度用コンボボックス */
	private Combo m_notifyPriority = null;

	/** ジョブ待ち条件情報 */
	private JobWaitRuleInfo m_waitRule = null;
	/** ジョブコマンド情報 */
	private JobCommandInfo m_jobCommand = null;
	/** ジョブファイル転送情報 */
	private JobFileInfo m_jobFile = null;

	/** コマンド実行失敗時に終了する用チェックボタン */
	private Button m_messageRetryEndCondition = null;
	/** 終了値用テキスト */
	private Text m_messageRetryEndValue = null;
	/** リトライ回数用テキスト */
	private Text m_messageRetry;

	/** ジョブが正常終了するまで繰り返す用チェックボタン */
	private Button m_commandRetryCondition = null;
	/** エラーリトライ回数用テキスト */
	private Text m_commandRetry;

	/** ジョブ種別 */
	private int m_jobType = JobConstant.TYPE_JOB;

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
	public ControlNodeComposite(Composite parent, int style, int jobType) {
		super(parent, style);
		this.m_jobType = jobType;
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		this.setLayout(JobDialogUtil.getParentLayout());

		// 多重度が上限（グループ）
		Group multiplicityGroup = new Group(this, SWT.NONE);
		multiplicityGroup.setText(Messages.getString("job.multiplicity.action"));
		multiplicityGroup.setLayout(new RowLayout(SWT.VERTICAL));

		// 多重度が上限：通知（Composite）
		Composite notifyComposite = new Composite(multiplicityGroup, SWT.NONE);
		notifyComposite.setLayout(new RowLayout());
		
		// 多重度が条件：通知（チェック）
		this.m_notifyCondition = new Button(notifyComposite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_notifyCondition", this.m_notifyCondition);
		this.m_notifyCondition.setText(Messages.getString("notify") + " : ");
		this.m_notifyCondition.setLayoutData(new RowData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
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

		// 多重度が上限：通知（コンボ）
		this.m_notifyPriority = new Combo(notifyComposite, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_notifyPriority", this.m_notifyPriority);
		this.m_notifyPriority.setLayoutData(new RowData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_notifyPriority.add(PriorityMessage.STRING_INFO);
		this.m_notifyPriority.add(PriorityMessage.STRING_WARNING);
		this.m_notifyPriority.add(PriorityMessage.STRING_CRITICAL);
		this.m_notifyPriority.add(PriorityMessage.STRING_UNKNOWN);

		// 操作（グループ）
		Group actionGroup = new Group(multiplicityGroup, SWT.NONE);
		actionGroup.setText(Messages.getString("operations"));
		actionGroup.setLayout(new GridLayout(2, false));

		// 操作：待機（ラジオ）
		this.m_waitCondition = new Button(actionGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_waitCondition", this.m_waitCondition);
		this.m_waitCondition.setText(Messages.getString("wait"));
		this.m_waitCondition.setLayoutData(new GridData(50, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_waitCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setMultiplicityOperation(StatusConstant.TYPE_WAIT);
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		// dummy
		new Label(actionGroup, SWT.NONE);

		// 操作：終了（ラジオ）
		this.m_endCondition = new Button(actionGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_endCondition", this.m_endCondition);
		this.m_endCondition.setText(Messages.getString("end"));
		this.m_endCondition.setLayoutData(new GridData(50, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_endCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setMultiplicityOperation(StatusConstant.TYPE_END);
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		// 操作：終了（Composite）
		Composite endGroup = new Composite(actionGroup, SWT.BORDER);
		endGroup.setLayout(new RowLayout());

		// 操作：終了：終了値（ラベル）
		Label endLabel = new Label(endGroup, SWT.LEFT);
		endLabel.setText(Messages.getString("end.value") + " : ");
		endLabel.setLayoutData(new RowData(80, SizeConstant.SIZE_LABEL_HEIGHT));
		
		// 操作：終了：終了値（テキスト）
		this.m_endValue = new Text(endGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_endValue", this.m_endValue);
		this.m_endValue.setLayoutData(new RowData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_endValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_endValue.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
				);

		// separator
		JobDialogUtil.getSeparator(this);

		// エージェントに接続できない（チェック）
		this.m_messageRetryEndCondition = new Button(this, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_messageRetryEndCondition", this.m_messageRetryEndCondition);
		this.m_messageRetryEndCondition.setText(Messages.getString("command.error.ended"));
		this.m_messageRetryEndCondition.setLayoutData(new RowData(300,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_messageRetryEndCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				m_messageRetryEndValue.setEditable((m_jobType == JobConstant.TYPE_JOB) 
						&& check.getSelection());
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// エージェントに接続できない（Composite）
		Composite messageRetryEndConditionGroup = new Composite(this, SWT.BORDER);
		messageRetryEndConditionGroup.setLayout(new RowLayout());

		// エージェントに接続できない：試行回数（ラベル）
		Label messageRetryTitle = new Label(messageRetryEndConditionGroup, SWT.LEFT);
		messageRetryTitle.setText(Messages.getString("job.retries") + " : ");
		messageRetryTitle.setLayoutData(new RowData(105,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// エージェントに接続できない：試行回数（テキスト）
		this.m_messageRetry= new Text(messageRetryEndConditionGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_messageRetry", this.m_messageRetry);
		this.m_messageRetry.setLayoutData(new RowData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_messageRetry.addVerifyListener(
				new PositiveNumberVerifyListener(1, DataRangeConstant.SMALLINT_HIGH));
		this.m_messageRetry.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
				);

		// エージェントに接続できない：終了値（ラベル）
		Label skipEndValueTitle = new Label(messageRetryEndConditionGroup, SWT.RIGHT);
		skipEndValueTitle.setText(Messages.getString("end.value") + " : ");
		skipEndValueTitle.setLayoutData(new RowData(80,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// エージェントに接続できない：終了値（テキスト）
		this.m_messageRetryEndValue = new Text(messageRetryEndConditionGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_messageRetryEndValue", this.m_messageRetryEndValue);
		this.m_messageRetryEndValue.setLayoutData(new RowData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_messageRetryEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_messageRetryEndValue.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
				);

		// separator
		JobDialogUtil.getSeparator(this);

		// ジョブが終了するまで繰り返す（チェック）
		this.m_commandRetryCondition = new Button(this, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_commandRetryCondition", this.m_commandRetryCondition);
		this.m_commandRetryCondition.setText(Messages.getString("command.error.retry"));
		this.m_commandRetryCondition.setLayoutData(new RowData(300,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_commandRetryCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_commandRetry.setEditable(true);
				} else {
					m_commandRetry.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// ジョブが終了するまで繰り返す（Composite）
		Composite commandRetryConditionGroup = new Composite(this, SWT.BORDER);
		commandRetryConditionGroup.setLayout(new RowLayout());

		// ジョブが終了するまで繰り返す：試行回数（ラベル）
		messageRetryTitle = new Label(commandRetryConditionGroup, SWT.LEFT);
		messageRetryTitle.setText(Messages.getString("job.retries") + " : ");
		messageRetryTitle.setLayoutData(new RowData(105,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// ジョブが終了するまで繰り返す：試行回数（テキスト）
		this.m_commandRetry= new Text(commandRetryConditionGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_commandRetry", this.m_commandRetry);
		this.m_commandRetry.setLayoutData(new RowData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_commandRetry.addVerifyListener(
				new PositiveNumberVerifyListener(1, DataRangeConstant.SMALLINT_HIGH));
		this.m_commandRetry.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
				);

		// 有効・無効設定
		if (m_jobType == JobConstant.TYPE_FILEJOB) {
			this.m_messageRetryEndValue.setEditable(false);
		}
	}

	@Override
	public void update() {
		// 必須項目を明示
		if(m_endCondition.getSelection() && "".equals(this.m_endValue.getText())){
			this.m_endValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_endValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_messageRetryEndCondition.getSelection() && "".equals(m_messageRetryEndValue.getText())) {
			this.m_messageRetryEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_messageRetryEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_messageRetry.getText())){
			this.m_messageRetry.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_messageRetry.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_commandRetryCondition.getSelection() && "".equals(this.m_commandRetry.getText())){
			this.m_commandRetry.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_commandRetry.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 制御(ノード)情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 * @see com.clustercontrol.jobmanagement.bean.JobCommandInfo
	 */
	public void reflectControlNodeInfo() {
		if (m_waitRule != null) {
			//通知
			m_notifyCondition.setSelection(m_waitRule.isMultiplicityNotify());

			//通知の重要度
			setSelectPriority(m_notifyPriority, m_waitRule.getMultiplicityNotifyPriority());
			setMultiplicityOperation(m_waitRule.getMultiplicityOperation());

			m_endValue.setText(String.valueOf(m_waitRule.getMultiplicityEndValue()));
		}

		m_messageRetry.setText("10");
		m_messageRetryEndCondition.setSelection(true);
		m_messageRetryEndValue.setText(String
				.valueOf(EndStatusConstant.INITIAL_VALUE_ABNORMAL));
		m_commandRetry.setText("10");
		m_commandRetryCondition.setSelection(false);

		if (m_jobCommand != null) {
			//リトライ回数
			m_messageRetry.setText(String.valueOf(m_jobCommand.getMessageRetry()));

			//エラー時終了
			m_messageRetryEndCondition.setSelection(m_jobCommand.isMessageRetryEndFlg());

			//エラー時終了値
			m_messageRetryEndValue.setText(String.valueOf(m_jobCommand.getMessageRetryEndValue()));

			//エラー時リトライ
			m_commandRetryCondition.setSelection(m_jobCommand.isCommandRetryFlg());

			//エラー時のリトライ回数
			m_commandRetry.setText(String.valueOf(m_jobCommand.getCommandRetry()));
		} else if (m_jobFile != null) {
			//リトライ回数
			m_messageRetry.setText(String.valueOf(m_jobFile.getMessageRetry()));

			//エラー時終了
			m_messageRetryEndCondition.setSelection(m_jobFile.isMessageRetryEndFlg());

			//エラー時終了値
			m_messageRetryEndValue.setText(String.valueOf(m_jobFile.getMessageRetryEndValue()));

			//エラー時リトライ
			m_commandRetryCondition.setSelection(m_jobFile.isCommandRetryFlg());

			//エラー時のリトライ回数
			m_commandRetry.setText(String.valueOf(m_jobFile.getCommandRetry()));
		}
		//エラー時終了
		if (m_messageRetryEndCondition.getSelection()) {
			m_messageRetryEndValue.setEditable(false);
			if (m_jobType == JobConstant.TYPE_JOB) {
				m_messageRetryEndValue.setEditable(true);
			}
		} else {
			m_messageRetryEndValue.setEditable(false);
		}

		//正常終了するまで繰り返す
		if (m_commandRetryCondition.getSelection()) {
			m_commandRetry.setEditable(true);
		} else {
			m_commandRetry.setEditable(false);
		}

	}

	private void setMultiplicityOperation(Integer status) {
		m_waitCondition.setSelection(false);
		m_endCondition.setSelection(false);

		//wait,end,running
		if (status == StatusConstant.TYPE_END) {
			m_endCondition.setSelection(true);
			m_endValue.setEditable(true);
		} else if (status == StatusConstant.TYPE_RUNNING) {
			m_endValue.setEditable(false);
		} else {
			m_waitCondition.setSelection(true);
			m_endValue.setEditable(false);
		}
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
	 * ジョブコマンド情報を設定する。
	 *
	 * @param jobCommand ジョブコマンド情報
	 */
	public void setCommandInfo(JobCommandInfo jobCommand) {
		m_jobCommand = jobCommand;
	}
	
	/**
	 * ジョブファイル転送情報を設定する。
	 *
	 * @param jobFile ジョブファイル転送情報
	 */
	public void setFileInfo(JobFileInfo jobFile) {
		m_jobFile = jobFile;
	}

	/**
	 * ジョブコマンド情報を返す。
	 *
	 * @return ジョブコマンド情報
	 */
	public JobCommandInfo getCommandInfo() {
		return m_jobCommand;
	}
	
	/**
	 * ジョブファイル転送情報を返す。
	 * 
	 * @return ジョブファイル転送情報
	 */
	public JobFileInfo getFileInfo() {
		return m_jobFile;
	}

	/**
	 * コンポジットの情報から、ジョブ待ち条件情報を作成する。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 */
	public ValidateResult createWaitRuleInfo() {

		//条件関係取得
		if (m_waitCondition.getSelection()) {
			m_waitRule.setMultiplicityOperation(StatusConstant.TYPE_WAIT);
		} else if (m_endCondition.getSelection()){
			m_waitRule.setMultiplicityOperation(StatusConstant.TYPE_END);
		}

		//通知
		m_waitRule.setMultiplicityNotify(m_notifyCondition.getSelection());

		//通知の重要度
		m_waitRule.setMultiplicityNotifyPriority(getSelectPriority(m_notifyPriority));

		//終了値
		try {
			m_waitRule.setMultiplicityEndValue(
					Integer.parseInt(m_endValue.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.getMultiplicityOperation() == StatusConstant.TYPE_END) {
				ValidateResult result = null;
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
	 * コンポジットの情報から、ジョブコマンド情報を作成する。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobCommandInfo
	 */
	public ValidateResult createCommandInfo() {
		ValidateResult result = null;

		if (m_jobCommand == null) {
			m_jobCommand = new JobCommandInfo();
		}

		//リトライ回数
		try {
			if (m_messageRetry.getText().length() > 0) {
				m_jobCommand.setMessageRetry(Integer.parseInt(m_messageRetry.getText()));
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.87"));
				return result;
			}
		} catch(NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.87"));
			return result;
		}

		//エラー時終了
		m_jobCommand.setMessageRetryEndFlg(m_messageRetryEndCondition.getSelection());

		//エラー時終了値取得
		try {
			if (m_messageRetryEndValue.getText().length() > 0) {
				m_jobCommand.setMessageRetryEndValue(Integer.parseInt(m_messageRetryEndValue
						.getText()));
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		} catch (NumberFormatException e) {
			if (m_jobCommand.isMessageRetryEndFlg().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		}

		//エラーリトライ回数
		try {
			if (m_commandRetry.getText().length() > 0) {
				m_jobCommand.setCommandRetry(Integer.parseInt(m_commandRetry.getText()));
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.87"));
				return result;
			}
		} catch(NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.87"));
			return result;
		}

		//エラー時のリトライ
		m_jobCommand.setCommandRetryFlg(m_commandRetryCondition.getSelection());

		return null;
	}
	
	/**
	 * コンポジットの情報から、ファイル転送ジョブ情報を作成する。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobFileInfo
	 */
	public ValidateResult createFileInfo() {
		ValidateResult result = null;

		if (m_jobFile == null) {
			m_jobFile = new JobFileInfo();
		}

		//リトライ回数
		try {
			if (m_messageRetry.getText().length() > 0) {
				m_jobFile.setMessageRetry(Integer.parseInt(m_messageRetry.getText()));
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.87"));
				return result;
			}
		} catch(NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.87"));
			return result;
		}

		//エラー時終了
		m_jobFile.setMessageRetryEndFlg(m_messageRetryEndCondition.getSelection());

		//エラー時終了値取得
		try {
			if (m_messageRetryEndValue.getText().length() > 0) {
				m_jobFile.setMessageRetryEndValue(Integer.parseInt(m_messageRetryEndValue
						.getText()));
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		} catch (NumberFormatException e) {
			if (m_jobFile.isMessageRetryEndFlg().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		}

		//エラーリトライ回数
		try {
			if (m_commandRetry.getText().length() > 0) {
				m_jobFile.setCommandRetry(Integer.parseInt(m_commandRetry.getText()));
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.87"));
				return result;
			}
		} catch(NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.87"));
			return result;
		}

		//エラー時のリトライ
		m_jobFile.setCommandRetryFlg(m_commandRetryCondition.getSelection());

		return null;
	}


		/**
	 * 指定した重要度に該当する終了遅延通知重要度用コンボボックスの項目を選択します。
	 *
	 * @param combo 終了遅延通知重要度用コンボボックスのインスタンス
	 * @param priority 重要度
	 *
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public void setSelectPriority(Combo combo, int priority) {
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
	public int getSelectPriority(Combo combo) {
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
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_notifyCondition.setEnabled(enabled);
		m_notifyPriority.setEnabled(m_notifyCondition.getSelection() && enabled);
		m_waitCondition.setEnabled(enabled);
		m_endCondition.setEnabled(enabled);
		m_endValue.setEditable(m_endCondition.getSelection() && enabled);
		m_messageRetryEndCondition.setEnabled(enabled);
		m_messageRetry.setEditable(enabled);
		m_messageRetryEndValue.setEditable(m_jobType == JobConstant.TYPE_JOB 
				&& m_messageRetryEndCondition.getSelection() && enabled);

		m_commandRetryCondition.setEnabled(enabled);
		m_commandRetry.setEditable(m_commandRetryCondition.getSelection() && enabled);
	}

}
