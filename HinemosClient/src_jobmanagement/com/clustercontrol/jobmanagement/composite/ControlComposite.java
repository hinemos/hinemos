/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobNextJobOrderInfoResponse;
import org.openapitools.client.model.JobWaitRuleInfoResponse;
import org.openapitools.client.model.JobWaitRuleInfoResponse.CalendarEndStatusEnum;
import org.openapitools.client.model.JobWaitRuleInfoResponse.JobRetryEndStatusEnum;
import org.openapitools.client.model.JobWaitRuleInfoResponse.SkipEndStatusEnum;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.calendar.composite.CalendarIdListComposite;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.composite.action.PositiveNumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.bean.JobInfoParameterConstant;
import com.clustercontrol.jobmanagement.dialog.ExclusiveBranchCompositeDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

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
	/** 繰り返し実行用チェックボタン */
	private Button m_jobRetryCondition = null;
	/** 繰り返し実行回数用テキスト */
	private Text m_jobRetryCount = null;
	/** 繰り返し実行試行間隔用テキスト */
	private Text m_jobRetryInterval = null;
	/** 繰り返し実行終了状態用テキスト */
	private Combo m_jobRetryEndStatus = null;
	/** 排他条件分岐設定ボタン */
	private Button m_exclusiveBranchButton;
	/** シェル */
	private Shell m_shell;
	/** 読み取り専用フラグ */
	private boolean m_readOnly = false;
	/** ジョブ待ち条件情報 */
	private JobWaitRuleInfoResponse m_waitRule = null;
	/** ジョブ種別 */
	private JobInfoWrapper.TypeEnum m_jobType = JobInfoWrapper.TypeEnum.JOB;
	/** 排他分岐ダイアログで後続ジョブを表示するために使用する */
	private JobTreeItemWrapper m_jobTreeItem;
	/** ジョブキュー チェックボタン */
	private Button m_jobQueueCondition = null;
	/** ジョブキュー 選択リスト */
	private JobQueueDropdown m_jobQueue = null;
	/** ジョブ待ち条件情報（後続ジョブ実行設定） */
	private Boolean m_exclusiveBranch;
	private JobWaitRuleInfoResponse.ExclusiveBranchEndStatusEnum m_exclusiveBranchEndStatus = null;
    private Integer m_exclusiveBranchEndValue = null;
    private List<JobNextJobOrderInfoResponse> m_exclusiveBranchNextJobOrderList = new ArrayList<>();

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
	public ControlComposite(Composite parent, int style, JobInfoWrapper.TypeEnum jobType) {
		super(parent, style);
		initialize();
		m_shell = this.getShell();
		m_jobType = jobType;
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		this.setLayout(JobDialogUtil.getParentLayout());

		// カレンダ（チェック）
		this.m_calendarCondition = new Button(JobDialogUtil.getComposite_MarginZero(this), SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_calendarCondition", this.m_calendarCondition);
		this.m_calendarCondition.setText(Messages.getString("calendar"));
		this.m_calendarCondition.setLayoutData(new RowData(100,
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
		((GridData)this.m_calendarId.getLayoutData()).widthHint = 600;

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

		// 保留（チェック）
		this.m_waitCondition = new Button(JobDialogUtil.getComposite_MarginZero(this), SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_waitCondition", this.m_waitCondition);
		this.m_waitCondition.setText(Messages.getString("reserve"));
		this.m_waitCondition.setLayoutData(new RowData(100,
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

		// スキップ（チェック）
		this.m_skipCondition = new Button(JobDialogUtil.getComposite_MarginZero(this), SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_skipCondition", this.m_skipCondition);
		this.m_skipCondition.setText(Messages.getString("skip"));
		this.m_skipCondition.setLayoutData(new RowData(100,
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
		skipEndConditionGroup.setLayout(new RowLayout());

		// スキップ：終了状態（ラベル）
		Label skipEndStatusTitle = new Label(skipEndConditionGroup, SWT.LEFT);
		skipEndStatusTitle.setText(Messages.getString("end.status") + " : ");
		skipEndStatusTitle.setLayoutData(new RowData(80,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// スキップ：終了状態（コンボ）
		this.m_skipEndStatus = new Combo(skipEndConditionGroup, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_skipEndStatus", this.m_skipEndStatus);
		this.m_skipEndStatus.setLayoutData(new RowData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_skipEndStatus.add(EndStatusMessage.STRING_NORMAL);
		this.m_skipEndStatus.add(EndStatusMessage.STRING_WARNING);
		this.m_skipEndStatus.add(EndStatusMessage.STRING_ABNORMAL);

		// スキップ：終了値（ラベル）
		Label skipEndValueTitle = new Label(skipEndConditionGroup, SWT.RIGHT);
		skipEndValueTitle.setText(Messages.getString("end.value") + " : ");
		skipEndValueTitle.setLayoutData(new RowData(80,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// スキップ：終了値（テキスト）
		this.m_skipEndValue = new Text(skipEndConditionGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_skipEndValue", this.m_skipEndValue);
		this.m_skipEndValue.setLayoutData(new RowData(100,
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

		// separator
		JobDialogUtil.getSeparator(this);

		// ジョブキュー(チェック)
		this.m_jobQueueCondition = new Button(JobDialogUtil.getComposite_MarginZero(this), SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_jobQueueCondition", this.m_jobQueueCondition);
		this.m_jobQueueCondition.setText(Messages.getString("job.concurrency_control"));
		this.m_jobQueueCondition.setLayoutData(new RowData(110, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_jobQueueCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_jobQueue.setEnabled(true);
				} else {
					m_jobQueue.setEnabled(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		m_jobQueueCondition.setEnabled(false);

		// ジョブキュー（Composite）
		Composite jobQueueGroup = new Composite(this, SWT.BORDER);
		jobQueueGroup.setLayout(new GridLayout(2, false));

		// ジョブキュー：ジョブキューID（ラベル）
		Label jobQueueIdTitle = new Label(jobQueueGroup, SWT.LEFT);
		jobQueueIdTitle.setText(Messages.getString("jobqueue.id") + " : ");
		jobQueueIdTitle.setLayoutData(new GridData(80, SizeConstant.SIZE_LABEL_HEIGHT));

		// ジョブキュー：ジョブキュー（コンボ）
		this.m_jobQueue = new JobQueueDropdown(jobQueueGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_jobQueue", this.m_jobQueue);
		m_jobQueue.setLayoutData(new GridData());
		((GridData) this.m_jobQueue.getLayoutData()).widthHint = 600;
		this.m_jobQueue.setEnabled(false);

		// separator
		JobDialogUtil.getSeparator(this);
		
		// 繰り返し実行（チェック）
		this.m_jobRetryCondition = new Button(JobDialogUtil.getComposite_MarginZero(this), SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_jobRetryCondition", this.m_jobRetryCondition);
		this.m_jobRetryCondition.setText(Messages.getString("job.retry"));
		this.m_jobRetryCondition.setLayoutData(new RowData(200,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_jobRetryCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					if (isRetryJob()) {
						m_jobRetryEndStatus.setEnabled(true);
						m_jobRetryCount.setEditable(true);
						m_jobRetryInterval.setEditable(true);
					}
				} else {
					m_jobRetryEndStatus.setEnabled(false);
					m_jobRetryCount.setEditable(false);
					m_jobRetryInterval.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 繰り返し実行（Composite）
		Composite jobRetryEndConditionGroup = new Composite(this, SWT.BORDER);
		jobRetryEndConditionGroup.setLayout(new RowLayout());

		// 繰り返し実行：繰り返し回数（ラベル）
		Label jobRetryEndValueTitle = new Label(jobRetryEndConditionGroup, SWT.LEFT);
		jobRetryEndValueTitle.setText(Messages.getString("job.retry.count") + " : ");
		jobRetryEndValueTitle.setLayoutData(new RowData(80,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// 繰り返し実行：繰り返し回数（テキスト）
		this.m_jobRetryCount = new Text(jobRetryEndConditionGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_jobRetryCount", this.m_jobRetryCount);
		this.m_jobRetryCount.setLayoutData(new RowData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_jobRetryCount.addVerifyListener(
				new PositiveNumberVerifyListener(1, DataRangeConstant.SMALLINT_HIGH));
		this.m_jobRetryCount.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
			);

		// 繰り返し実行：試行間隔（ラベル）
		Label jobRetryIntervalTitle = new Label(jobRetryEndConditionGroup, SWT.RIGHT);
		jobRetryIntervalTitle.setText(Messages.getString("job.retry.interval") + " : ");
		jobRetryIntervalTitle.setLayoutData(new RowData(150,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// 繰り返し実行：試行間隔（テキスト）
		this.m_jobRetryInterval = new Text(jobRetryEndConditionGroup, SWT.BORDER);
		this.m_jobRetryInterval.setLayoutData(new RowData(100,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_jobRetryInterval.addVerifyListener(
				new PositiveNumberVerifyListener(0, JobInfoParameterConstant.JOB_RETRY_INTERVAL_HIGH));
		this.m_jobRetryInterval.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
			);

		// 繰り返し実行：繰り返し完了状態（ラベル）
		Label jobRetryEndStatusTitle = new Label(jobRetryEndConditionGroup, SWT.RIGHT);
		jobRetryEndStatusTitle.setText(Messages.getString("job.retry.end.status") + " : ");
		jobRetryEndStatusTitle.setLayoutData(new RowData(120,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// 繰り返し実行：繰り返し完了状態（コンボ）
		this.m_jobRetryEndStatus = new Combo(jobRetryEndConditionGroup, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_jobRetryEndStatus", this.m_jobRetryEndStatus);
		this.m_jobRetryEndStatus.setLayoutData(new RowData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_jobRetryEndStatus.add("");
		this.m_jobRetryEndStatus.add(EndStatusMessage.STRING_NORMAL);
		this.m_jobRetryEndStatus.add(EndStatusMessage.STRING_WARNING);
		this.m_jobRetryEndStatus.add(EndStatusMessage.STRING_ABNORMAL);

		// separator
		JobDialogUtil.getSeparator(this);
		
		//排他条件分岐コンポジット
		Composite exclusiveBranchEndConditionGroup = new Composite(this, SWT.NONE);
		exclusiveBranchEndConditionGroup.setLayout(new GridLayout(2, false));

		m_exclusiveBranchButton = new Button(exclusiveBranchEndConditionGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_exclusiveBranchButton", this.m_exclusiveBranchButton);
		m_exclusiveBranchButton.setText(Messages.getString("job.exclusive.branch"));
		m_exclusiveBranchButton.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_exclusiveBranchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ExclusiveBranchCompositeDialog dialog = new ExclusiveBranchCompositeDialog(m_shell, m_readOnly);
				dialog.setExclusiveBranch(m_exclusiveBranch);
				dialog.setExclusiveBranchEndStatus(m_exclusiveBranchEndStatus);
				dialog.setExclusiveBranchEndValue(m_exclusiveBranchEndValue);
				dialog.setExclusiveBranchNextJobOrderList(m_exclusiveBranchNextJobOrderList);
				dialog.setJobTreeItem(m_jobTreeItem);
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_exclusiveBranch = dialog.isExclusiveBranch();
					m_exclusiveBranchEndStatus = dialog.getExclusiveBranchEndStatus();
					m_exclusiveBranchEndValue = dialog.getExclusiveBranchEndValue();
					m_exclusiveBranchNextJobOrderList = dialog.getExclusiveBranchNextJobOrderList();
				}
			}
		});
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目を明示
		if (m_calendarCondition.getSelection() && "".equals(this.m_calendarEndValue.getText())) {
			this.m_calendarEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_calendarEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_skipCondition.getSelection() && "".equals(this.m_skipEndValue.getText())) {
			this.m_skipEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_skipEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_jobRetryCondition.getSelection() && "".equals(this.m_jobRetryCount.getText())) {
			this.m_jobRetryCount.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_jobRetryCount.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_jobRetryCondition.getSelection() && "".equals(this.m_jobRetryInterval.getText())) {
			this.m_jobRetryInterval.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_jobRetryInterval.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
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
		m_calendarCondition.setSelection(m_waitRule.getCalendar());

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
		m_waitCondition.setSelection(m_waitRule.getSuspend());
		//スキップ
		m_skipCondition.setSelection(m_waitRule.getSkip());
		//スキップ終了値
		m_skipEndValue.setText(String.valueOf(m_waitRule.getSkipEndValue()));
		//スキップの終了状態
		setSelectEndStatus(m_skipEndStatus, m_waitRule.getSkipEndStatus());
		//繰り返し実行
		m_jobRetryCondition.setSelection(m_waitRule.getJobRetryFlg());
		//繰り返し実行回数
		m_jobRetryCount.setText(String.valueOf(m_waitRule.getJobRetry()));
		//繰り返し試行間隔
		m_jobRetryInterval.setText(String.valueOf(m_waitRule.getJobRetryInterval()));
		//繰り返し実行の終了状態
		setSelectEndStatus(m_jobRetryEndStatus, m_waitRule.getJobRetryEndStatus());
		// ジョブキュー
		m_jobQueueCondition.setSelection(BooleanUtils.isTrue(m_waitRule.getQueueFlg()));
		// ジョブキューID
		if (StringUtils.isNotEmpty(m_waitRule.getQueueId())) {
			m_jobQueue.setQueueId(m_waitRule.getQueueId());
		}
		// 後続ジョブ実行設定
		m_exclusiveBranch = m_waitRule.getExclusiveBranch();
		m_exclusiveBranchEndStatus = m_waitRule.getExclusiveBranchEndStatus();
		m_exclusiveBranchEndValue = m_waitRule.getExclusiveBranchEndValue();
		m_exclusiveBranchNextJobOrderList.addAll(m_waitRule.getExclusiveBranchNextJobOrderList());

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

		//繰り返し実行
		if (m_jobRetryCondition.getSelection()) {
			if (isRetryJob()) {
				m_jobRetryCount.setEditable(true);
				m_jobRetryInterval.setEditable(true);
				m_jobRetryEndStatus.setEnabled(true);
			}
		} else {
			m_jobRetryCount.setEditable(false);
			m_jobRetryInterval.setEditable(false);
			m_jobRetryEndStatus.setEnabled(false);
		}

		// ジョブキュー
		if (m_jobQueueCondition.getSelection()) {
			m_jobQueue.setEnabled(true);
		} else {
			m_jobQueue.setEnabled(false);
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
			if (m_waitRule.getCalendar().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.calendar.22"));
				return result;
			}
		}

		//カレンダ未実行時の終了値取得
		try {
			m_waitRule.setCalendarEndStatus(JobWaitRuleInfoResponse.CalendarEndStatusEnum.fromValue(getSelectEndStatus(m_calendarEndStatus)));
			m_waitRule.setCalendarEndValue(Integer.parseInt(m_calendarEndValue.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.getCalendar().booleanValue()) {
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
			m_waitRule.setSkipEndStatus(JobWaitRuleInfoResponse.SkipEndStatusEnum.fromValue(getSelectEndStatus(m_skipEndStatus)));
			m_waitRule.setSkipEndValue(Integer.parseInt(m_skipEndValue.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.getSkip().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		}

		//繰り返し実行
		m_waitRule.setJobRetryFlg(m_jobRetryCondition.getSelection());

		//繰り返し実行終了値取得
		// 試行回数
		try {
			m_waitRule.setJobRetry(Integer.parseInt(m_jobRetryCount.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.getJobRetryFlg().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.167"));
				return result;
			}
		}
		// 試行間隔
		try {
			m_waitRule.setJobRetryInterval(Integer.parseInt(m_jobRetryInterval.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.getJobRetryFlg().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.197"));
				return result;
			}
		}

		// 完了状態
		if (getSelectEndStatus(m_jobRetryEndStatus) != null) {
			m_waitRule.setJobRetryEndStatus(JobWaitRuleInfoResponse.JobRetryEndStatusEnum.fromValue(getSelectEndStatus(m_jobRetryEndStatus)));
		} else {
			m_waitRule.setJobRetryEndStatus(null);
		}

		// ジョブキュー
		m_waitRule.setQueueFlg(m_jobQueueCondition.getSelection());
		
		// ジョブキューID
		if (StringUtils.isEmpty(m_jobQueue.getQueueId())) {
			m_waitRule.setQueueId(null);
			if (m_waitRule.getQueueFlg().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.jobqueue.missing"));
				return result;
			}
		} else {
			m_waitRule.setQueueId(m_jobQueue.getQueueId());
		}
		
		// 後続ジョブ実行設定
		m_waitRule.setExclusiveBranch(m_exclusiveBranch);
		m_waitRule.setExclusiveBranchEndStatus(m_exclusiveBranchEndStatus);
		m_waitRule.setExclusiveBranchEndValue(m_exclusiveBranchEndValue);
		m_waitRule.getExclusiveBranchNextJobOrderList().clear();
		m_waitRule.getExclusiveBranchNextJobOrderList().addAll(m_exclusiveBranchNextJobOrderList);

		return null;
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
		return EndStatusMessage.stringTotypeEnumValue(select);
	}

	/**
	 * 指定した重要度に該当する終了状態用コンボボックスの項目を選択します。
	 *
	 */
	private void setSelectEndStatus(Combo combo, Object enumValue) {
		String select = "";
		if (enumValue == null) {
			// 何もしない
		} else if (enumValue instanceof CalendarEndStatusEnum) {
			select = EndStatusMessage.typeEnumValueToString(
					((CalendarEndStatusEnum)enumValue).getValue());
		} else if (enumValue instanceof SkipEndStatusEnum) {
			select = EndStatusMessage.typeEnumValueToString(
					((SkipEndStatusEnum)enumValue).getValue());
		} else if (enumValue instanceof JobRetryEndStatusEnum) {
			select = EndStatusMessage.typeEnumValueToString(
					((JobRetryEndStatusEnum)enumValue).getValue());
		}

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

	public void setJobTreeItem(JobTreeItemWrapper jobTreeItem) {
		m_jobTreeItem = jobTreeItem;
	}
	
	public JobQueueDropdown getJobQueueDropdown() {
		return m_jobQueue;
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
		m_jobRetryCondition.setEnabled(isRetryJob() && enabled);
		m_jobRetryEndStatus.setEnabled(isRetryJob() && m_jobRetryCondition.getSelection() && enabled);
		m_jobRetryCount.setEditable(isRetryJob() && m_jobRetryCondition.getSelection() && enabled);
		m_jobRetryInterval.setEditable(isRetryJob() && m_jobRetryCondition.getSelection() && enabled);
		m_jobQueueCondition.setEnabled(isJobQueueAvailable() && enabled);
		m_jobQueue.setEnabled(isJobQueueAvailable() && m_jobQueueCondition.getSelection() && enabled);
		m_readOnly = !enabled;
	}
	
	private boolean isRetryJob() {
		//ファイル転送ジョブ、承認ジョブの場合は繰り返し実行の設定項目は非活性にする
		if( (m_jobType != JobInfoWrapper.TypeEnum.FILEJOB)
				&& (m_jobType != JobInfoWrapper.TypeEnum.APPROVALJOB)){
			return true;
		}
		return false;
	}

	// ジョブキューが有効な種類のジョブであるか。
	private boolean isJobQueueAvailable() {
		if (m_jobType == JobInfoWrapper.TypeEnum.JOBNET
				|| m_jobType == JobInfoWrapper.TypeEnum.JOB
				|| m_jobType == JobInfoWrapper.TypeEnum.FILEJOB
				|| m_jobType == JobInfoWrapper.TypeEnum.MONITORJOB
				|| m_jobType == JobInfoWrapper.TypeEnum.APPROVALJOB
				|| m_jobType == JobInfoWrapper.TypeEnum.FILECHECKJOB
				|| m_jobType == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB
				|| m_jobType == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB
				|| m_jobType == JobInfoWrapper.TypeEnum.RESOURCEJOB
				|| m_jobType == JobInfoWrapper.TypeEnum.RPAJOB) {
			return true;
		}
		return false;
	}

}
