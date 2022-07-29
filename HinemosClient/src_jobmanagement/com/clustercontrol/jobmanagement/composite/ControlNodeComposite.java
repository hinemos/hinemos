/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import org.openapitools.client.model.JobCommandInfoResponse;
import org.openapitools.client.model.JobFileCheckInfoResponse;
import org.openapitools.client.model.JobFileInfoResponse;
import org.openapitools.client.model.JobRpaInfoResponse;
import org.openapitools.client.model.JobWaitRuleInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.composite.action.PositiveNumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

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
	private JobWaitRuleInfoResponse m_waitRule = null;
	/** ジョブコマンド情報 */
	private JobCommandInfoResponse m_jobCommand = null;
	/** ジョブファイル転送情報 */
	private JobFileInfoResponse m_jobFile = null;
	/** ジョブファイルチェック情報 */
	private JobFileCheckInfoResponse m_jobFileCheck = null;
	/** RPAシナリオジョブ情報 */
	private JobRpaInfoResponse m_jobRpa = null;

	/** エージェントに接続できない時に終了する用チェックボタン */
	private Button m_messageRetryEndCondition = null;
	/** 終了値用テキスト */
	private Text m_messageRetryEndValue = null;
	/** リトライ回数用テキスト */
	private Text m_messageRetry;

	/** コマンド繰り返し実行用チェックボタン */
	private Button m_commandRetryCondition = null;
	/** コマンド繰り返し実行回数 */
	private Text m_commandRetry;
	/** コマンド繰り返し完了状態 */
	private Combo m_commandRetryEndStatus = null;

	/** ジョブ種別 */
	private JobInfoWrapper.TypeEnum m_jobType = JobInfoWrapper.TypeEnum.JOB;

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
	public ControlNodeComposite(Composite parent, int style, JobInfoWrapper.TypeEnum jobType) {
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
		this.m_waitCondition.setLayoutData(new GridData(70, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_waitCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setMultiplicityOperation( JobWaitRuleInfoResponse.MultiplicityOperationEnum.WAIT);
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
				setMultiplicityOperation( JobWaitRuleInfoResponse.MultiplicityOperationEnum.END);
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
		this.m_messageRetryEndCondition = new Button(JobDialogUtil.getComposite_MarginZero(this), SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_messageRetryEndCondition", this.m_messageRetryEndCondition);
		this.m_messageRetryEndCondition.setText(Messages.getString("command.error.ended"));
		this.m_messageRetryEndCondition.setLayoutData(new RowData(400,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_messageRetryEndCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				m_messageRetryEndValue.setEditable(
						(m_jobType == JobInfoWrapper.TypeEnum.JOB || m_jobType == JobInfoWrapper.TypeEnum.FILECHECKJOB
						|| m_jobType == JobInfoWrapper.TypeEnum.RPAJOB) 
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

		// 繰り返し実行（チェック）
		this.m_commandRetryCondition = new Button(JobDialogUtil.getComposite_MarginZero(this), SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_commandRetryCondition", this.m_commandRetryCondition);
		this.m_commandRetryCondition.setText(Messages.getString("command.retry"));
		this.m_commandRetryCondition.setLayoutData(new RowData(200,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_commandRetryCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_commandRetry.setEditable(true);
					//コマンドジョブの場合のみ、活性にする
					if (m_jobType == JobInfoWrapper.TypeEnum.JOB) {
						m_commandRetryEndStatus.setEnabled(true);
					}
				} else {
					m_commandRetry.setEditable(false);
					m_commandRetryEndStatus.setEnabled(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 繰り返し実行（Composite）
		Composite commandRetryConditionGroup = new Composite(this, SWT.BORDER);
		commandRetryConditionGroup.setLayout(new RowLayout());

		// 繰り返し実行：試行回数（ラベル）
		messageRetryTitle = new Label(commandRetryConditionGroup, SWT.LEFT);
		messageRetryTitle.setText(Messages.getString("job.retry.count") + " : ");
		messageRetryTitle.setLayoutData(new RowData(105,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// 繰り返し実行：試行回数（テキスト）
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

		// 繰り返し実行：繰り返し完了状態（ラベル）
		Label commandRetryEndStatusTitle = new Label(commandRetryConditionGroup, SWT.RIGHT);
		commandRetryEndStatusTitle.setText(Messages.getString("job.retry.end.status") + " : ");
		commandRetryEndStatusTitle.setLayoutData(new RowData(120,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// 繰り返し実行：繰り返し完了状態（コンボ）
		this.m_commandRetryEndStatus = new Combo(commandRetryConditionGroup, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_commandRetryEndStatus", this.m_commandRetryEndStatus);
		this.m_commandRetryEndStatus.setLayoutData(new RowData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_commandRetryEndStatus.add("");
		this.m_commandRetryEndStatus.add(EndStatusMessage.STRING_NORMAL);
		this.m_commandRetryEndStatus.add(EndStatusMessage.STRING_WARNING);
		this.m_commandRetryEndStatus.add(EndStatusMessage.STRING_ABNORMAL);

		// 有効・無効設定
		if (m_jobType == JobInfoWrapper.TypeEnum.FILEJOB) {
			this.m_messageRetryEndValue.setEditable(false);
			this.m_commandRetryCondition.setEnabled(false);
			this.m_commandRetry.setEditable(false);
			this.m_commandRetryEndStatus.setEnabled(false);
		} else if (m_jobType == JobInfoWrapper.TypeEnum.FILECHECKJOB) {
			this.m_commandRetryCondition.setEnabled(false);
			this.m_commandRetry.setEditable(false);
			this.m_commandRetryEndStatus.setEnabled(false);
		} else if (m_jobType == JobInfoWrapper.TypeEnum.JOB) {
			this.m_messageRetryEndValue.setEnabled(true);
			this.m_commandRetryCondition.setEnabled(true);
			this.m_commandRetry.setEditable(true);
			this.m_commandRetryEndStatus.setEnabled(true);
		} else if (m_jobType == JobInfoWrapper.TypeEnum.RPAJOB) {
			this.m_commandRetryCondition.setEnabled(false);
			this.m_commandRetry.setEditable(false);
			this.m_commandRetryEndStatus.setEnabled(false);
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
			m_notifyCondition.setSelection(m_waitRule.getMultiplicityNotify());

			//通知の重要度
			setSelectPriority(m_notifyPriority, m_waitRule.getMultiplicityNotifyPriority());
			setMultiplicityOperation(m_waitRule.getMultiplicityOperation());

			m_endValue.setText(String.valueOf(m_waitRule.getMultiplicityEndValue()));
		}
		//初期値
		m_messageRetry.setText("10");
		m_messageRetryEndCondition.setSelection(true);
		m_messageRetryEndValue.setText(String
				.valueOf(EndStatusConstant.INITIAL_VALUE_ABNORMAL));
		m_commandRetryCondition.setSelection(false);
		m_commandRetry.setText("10");
		//繰り返し完了状態の初期値は「正常」
		setSelectEndStatus(m_commandRetryEndStatus, JobCommandInfoResponse.CommandRetryEndStatusEnum.NORMAL);

		if (m_jobCommand != null) {
			//リトライ回数
			m_messageRetry.setText(String.valueOf(m_jobCommand.getMessageRetry()));

			//エラー時終了
			m_messageRetryEndCondition.setSelection(m_jobCommand.getMessageRetryEndFlg());

			//エラー時終了値
			m_messageRetryEndValue.setText(String.valueOf(m_jobCommand.getMessageRetryEndValue()));

			//繰り返し実行
			m_commandRetryCondition.setSelection(m_jobCommand.getCommandRetryFlg());

			//繰り返し実行回数
			m_commandRetry.setText(String.valueOf(m_jobCommand.getCommandRetry()));

			//繰り返し実行完了状態
			setSelectEndStatus(m_commandRetryEndStatus, m_jobCommand.getCommandRetryEndStatus());

		} else if (m_jobFile != null) {
			//リトライ回数
			m_messageRetry.setText(String.valueOf(m_jobFile.getMessageRetry()));

			//エラー時終了
			m_messageRetryEndCondition.setSelection(m_jobFile.getMessageRetryEndFlg());

			//エラー時終了値
			m_messageRetryEndValue.setText(String.valueOf(m_jobFile.getMessageRetryEndValue()));

		} else if (m_jobFileCheck != null) {
			//リトライ回数
			m_messageRetry.setText(String.valueOf(m_jobFileCheck.getMessageRetry()));

			//エラー時終了
			m_messageRetryEndCondition.setSelection(m_jobFileCheck.getMessageRetryEndFlg());

			//エラー時終了値
			m_messageRetryEndValue.setText(String.valueOf(m_jobFileCheck.getMessageRetryEndValue()));

		} else if (m_jobRpa != null) {
			//リトライ回数
			m_messageRetry.setText(String.valueOf(m_jobRpa.getMessageRetry()));

			//エラー時終了
			m_messageRetryEndCondition.setSelection(m_jobRpa.getMessageRetryEndFlg());

			//エラー時終了値
			m_messageRetryEndValue.setText(String.valueOf(m_jobRpa.getMessageRetryEndValue()));

			//繰り返し実行
			m_commandRetryCondition.setSelection(m_jobRpa.getCommandRetryFlg());

			//繰り返し実行回数
			m_commandRetry.setText(String.valueOf(m_jobRpa.getCommandRetry()));

			//繰り返し実行完了状態
			setSelectEndStatus(m_commandRetryEndStatus, m_jobRpa.getCommandRetryEndStatus());

		}
		//エラー時終了
		if (m_messageRetryEndCondition.getSelection()) {
			m_messageRetryEndValue.setEditable(false);
			if (m_jobType == JobInfoWrapper.TypeEnum.JOB || m_jobType == JobInfoWrapper.TypeEnum.FILECHECKJOB || m_jobType == JobInfoWrapper.TypeEnum.RPAJOB) {
				m_messageRetryEndValue.setEditable(true);
			}
		} else {
			m_messageRetryEndValue.setEditable(false);
		}

		//繰り返し実行
		if (m_commandRetryCondition.getSelection()) {
			if (m_jobType == JobInfoWrapper.TypeEnum.JOB  || m_jobType == JobInfoWrapper.TypeEnum.RPAJOB) {
				m_commandRetry.setEditable(true);
				m_commandRetryEndStatus.setEnabled(true);
			}
		} else {
			m_commandRetry.setEditable(false);
			m_commandRetryEndStatus.setEnabled(false);
		}

	}

	private void setMultiplicityOperation( JobWaitRuleInfoResponse.MultiplicityOperationEnum status) {
		m_waitCondition.setSelection(false);
		m_endCondition.setSelection(false);

		//wait,end
		if (status == JobWaitRuleInfoResponse.MultiplicityOperationEnum.END) {
			m_endCondition.setSelection(true);
			m_endValue.setEditable(true);
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
	 * ジョブコマンド情報を設定する。
	 *
	 * @param jobCommand ジョブコマンド情報
	 */
	public void setCommandInfo(JobCommandInfoResponse jobCommand) {
		m_jobCommand = jobCommand;
	}
	
	/**
	 * ジョブファイル転送情報を設定する。
	 *
	 * @param jobFile ジョブファイル転送情報
	 */
	public void setFileInfo(JobFileInfoResponse jobFile) {
		m_jobFile = jobFile;
	}
	
	/**
	 * ジョブファイルチェック情報を設定する。
	 *
	 * @param jobFileCheck ジョブファイルチェック情報
	 */
	public void setFileCheckInfo(JobFileCheckInfoResponse jobFileCheck) {
		m_jobFileCheck = jobFileCheck;
	}
	
	/**
	 * RPAシナリオジョブ転送情報を設定する。
	 *
	 * @param jobRpa RPAシナリオジョブ転送情報
	 */
	public void setRpaInfo(JobRpaInfoResponse jobRpa) {
		m_jobRpa = jobRpa;
	}

	/**
	 * ジョブコマンド情報を返す。
	 *
	 * @return ジョブコマンド情報
	 */
	public JobCommandInfoResponse getCommandInfo() {
		return m_jobCommand;
	}
	
	/**
	 * ジョブファイル転送情報を返す。
	 * 
	 * @return ジョブファイル転送情報
	 */
	public JobFileInfoResponse getFileInfo() {
		return m_jobFile;
	}
	
	/**
	 * ジョブファイルチェック情報を返す。
	 * 
	 * @return ジョブファイルチェック情報
	 */
	public JobFileCheckInfoResponse getJobFileCheckInfo() {
		return m_jobFileCheck;
	}
	
	/**
	 * RPAシナリオジョブ転送情報を返す。
	 * 
	 * @return ジョブファイル転送情報
	 */
	public JobRpaInfoResponse getRpaInfo() {
		return m_jobRpa;
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
			m_waitRule.setMultiplicityOperation(JobWaitRuleInfoResponse.MultiplicityOperationEnum.WAIT);
		} else if (m_endCondition.getSelection()){
			m_waitRule.setMultiplicityOperation(JobWaitRuleInfoResponse.MultiplicityOperationEnum.END);
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
			if (m_waitRule.getMultiplicityOperation() ==  JobWaitRuleInfoResponse.MultiplicityOperationEnum.END) {
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
			m_jobCommand =  JobTreeItemUtil.createJobCommandInfoResponse();
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
			if (m_jobCommand.getMessageRetryEndFlg().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		}

		//繰り返し実行回数
		try {
			if (m_commandRetry.getText().length() > 0) {
				m_jobCommand.setCommandRetry(Integer.parseInt(m_commandRetry.getText()));
				if (m_commandRetryEndStatus.getText().equals("")) {
					m_jobCommand.setCommandRetryEndStatus(null);
				} else {
					String enumValue = EndStatusMessage.stringTotypeEnumValue(m_commandRetryEndStatus.getText());
					m_jobCommand.setCommandRetryEndStatus(JobCommandInfoResponse.CommandRetryEndStatusEnum.fromValue(enumValue));
				}
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.167"));
				return result;
			}
		} catch(NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.167"));
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
			m_jobFile = new JobFileInfoResponse();
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
			if (m_jobFile.getMessageRetryEndFlg().booleanValue()) {
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
	 * コンポジットの情報から、ファイルチェックジョブ情報を作成する。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobFileCheckInfo
	 */
	public ValidateResult createFileCheckInfo() {
		ValidateResult result = null;

		if (m_jobFileCheck == null) {
			m_jobFileCheck = new JobFileCheckInfoResponse();
		}

		//リトライ回数
		try {
			if (m_messageRetry.getText().length() > 0) {
				m_jobFileCheck.setMessageRetry(Integer.parseInt(m_messageRetry.getText()));
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
		m_jobFileCheck.setMessageRetryEndFlg(m_messageRetryEndCondition.getSelection());

		//エラー時終了値取得
		try {
			if (m_messageRetryEndValue.getText().length() > 0) {
				m_jobFileCheck.setMessageRetryEndValue(Integer.parseInt(m_messageRetryEndValue
						.getText()));
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		} catch (NumberFormatException e) {
			if (m_jobFileCheck.getMessageRetryEndFlg().booleanValue()) {
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
	public ValidateResult createRpaInfo() {
		ValidateResult result = null;

		if (m_jobRpa == null) {
			m_jobRpa = new JobRpaInfoResponse();
		}

		//リトライ回数
		try {
			if (m_messageRetry.getText().length() > 0) {
				m_jobRpa.setMessageRetry(Integer.parseInt(m_messageRetry.getText()));
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
		m_jobRpa.setMessageRetryEndFlg(m_messageRetryEndCondition.getSelection());

		//エラー時終了値取得
		try {
			if (m_messageRetryEndValue.getText().length() > 0) {
				m_jobRpa.setMessageRetryEndValue(Integer.parseInt(m_messageRetryEndValue
						.getText()));
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		} catch (NumberFormatException e) {
			if (m_jobRpa.getMessageRetryEndFlg().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		}

		//エラー時のコマンドリトライ
		m_jobRpa.setCommandRetryFlg(false);

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
	public void setSelectPriority(Combo combo, JobWaitRuleInfoResponse.MultiplicityNotifyPriorityEnum priority) {
		String select = "";

		if (priority == JobWaitRuleInfoResponse.MultiplicityNotifyPriorityEnum.CRITICAL) {
			select = PriorityMessage.STRING_CRITICAL;
		} else if (priority == JobWaitRuleInfoResponse.MultiplicityNotifyPriorityEnum.WARNING) {
			select = PriorityMessage.STRING_WARNING;
		} else if (priority == JobWaitRuleInfoResponse.MultiplicityNotifyPriorityEnum.INFO) {
			select = PriorityMessage.STRING_INFO;
		} else if (priority == JobWaitRuleInfoResponse.MultiplicityNotifyPriorityEnum.UNKNOWN) {
			select = PriorityMessage.STRING_UNKNOWN;
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
	public JobWaitRuleInfoResponse.MultiplicityNotifyPriorityEnum getSelectPriority(Combo combo) {
		String select = combo.getText();

		if (select.equals(PriorityMessage.STRING_CRITICAL)) {
			return JobWaitRuleInfoResponse.MultiplicityNotifyPriorityEnum.CRITICAL;
		} else if (select.equals(PriorityMessage.STRING_WARNING)) {
			return JobWaitRuleInfoResponse.MultiplicityNotifyPriorityEnum.WARNING;
		} else if (select.equals(PriorityMessage.STRING_INFO)) {
			return JobWaitRuleInfoResponse.MultiplicityNotifyPriorityEnum.INFO;
		} else if (select.equals(PriorityMessage.STRING_UNKNOWN)) {
			return JobWaitRuleInfoResponse.MultiplicityNotifyPriorityEnum.UNKNOWN;
		}

		return null;
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
		m_messageRetryEndValue.setEditable(
				(m_jobType == JobInfoWrapper.TypeEnum.JOB || m_jobType == JobInfoWrapper.TypeEnum.FILECHECKJOB || m_jobType == JobInfoWrapper.TypeEnum.RPAJOB)
						&& m_messageRetryEndCondition.getSelection() && enabled);

		m_commandRetryCondition.setEnabled((m_jobType == JobInfoWrapper.TypeEnum.JOB ) 
				&& enabled);
		m_commandRetry.setEditable((m_jobType == JobInfoWrapper.TypeEnum.JOB )
				&& m_commandRetryCondition.getSelection() && enabled);
		m_commandRetryEndStatus.setEnabled((m_jobType == JobInfoWrapper.TypeEnum.JOB ) 
				&& m_commandRetryCondition.getSelection() && enabled);
	}

	/**
	 *終了状態用コンボボックスにて選択している項目を取得します。
	 *
	 */
	private String getSelectEndStatus(Combo combo) {
		String select = combo.getText();
		if (select.equals("")) {
			return null;
		}
		String enumValue = EndStatusMessage.stringTotypeEnumValue(select);
		return enumValue;
	}

	/**
	 * 指定した重要度に該当する終了状態用コンボボックスの項目を選択します。
	 *
	 */
	private void setSelectEndStatus(Combo combo, Object enumValue) {

		String select = "";
		if (enumValue == null) {
			// 何もしない
		} else if (enumValue instanceof JobCommandInfoResponse.CommandRetryEndStatusEnum) {
			select = EndStatusMessage.typeEnumValueToString(
					((JobCommandInfoResponse.CommandRetryEndStatusEnum)enumValue).getValue());
		} else if (enumValue instanceof JobRpaInfoResponse.CommandRetryEndStatusEnum) {
			select = EndStatusMessage.typeEnumValueToString(
					((JobRpaInfoResponse.CommandRetryEndStatusEnum)enumValue).getValue());
		}

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}
}
