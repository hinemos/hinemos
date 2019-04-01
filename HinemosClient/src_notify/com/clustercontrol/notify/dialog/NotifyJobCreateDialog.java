/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.dialog;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.composite.action.JobIdSelectionListener;
import com.clustercontrol.notify.action.AddNotify;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.ModifyNotify;
import com.clustercontrol.notify.bean.ExecFacilityConstant;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.notify.NotifyInfo;
import com.clustercontrol.ws.notify.NotifyJobInfo;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * 通知（ジョブ実行）作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 3.0.0
 */
public class NotifyJobCreateDialog extends NotifyBasicCreateDialog {

	/** カラム数（重要度）。 */
	private static final int WIDTH_PRIORITY 		= 2;

	/** カラム数（ジョブ実行）。 */
	private static final int WIDTH_JOB_RUN	 		= 2;

	/** カラム数（ジョブユニットID）。 */
	private static final int WIDTH_JOBUNIT_ID	 		= 3;

	/** カラム数（ジョブID）。 */
	private static final int WIDTH_JOB_ID	 		= 4;

	/** カラム数（参照ボタン）。 */
	private static final int WIDTH_REF_BTN 			= 1;

	/** カラム数（呼出失敗時）。 */
	private static final int WIDTH_FAILURE_PRIORITY = 4;


	// ----- instance フィールド ----- //

	/** 通知タイプ
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	private static final int TYPE_JOB = 3;

	/** 入力値の正当性を保持するオブジェクト。 */
	protected ValidateResult validateResult = null;

	/** スコープ用テキスト */
	private Text m_textScope = null;

	/** ファシリティID */
	private String m_facilityId = null;

	/** スコープ */
	private String m_facilityPath = null;

	/** ジョブ変数用ラジオボタン */
	private Button m_radioGenerationNodeValue = null;

	/** 固定値用ラジオボタン */
	private Button m_radioFixedValue = null;

	/** スコープ参照用ボタン */
	private Button m_scopeSelect = null;

	/** 実行（通知） チェックボックス。 */
	private Button m_checkJobRunInfo = null;
	/** 実行（警告） チェックボックス。 */
	private Button m_checkJobRunWarning = null;
	/** 実行（異常） チェックボックス。 */
	private Button m_checkJobRunCritical = null;
	/** 実行（不明） チェックボックス。 */
	private Button m_checkJobRunUnknown = null;

	/** ジョブユニットID（通知） */
	private Text m_textJobunitIdInfo = null;
	/** ジョブユニットID（警告） */
	private Text m_textJobunitIdWarning = null;
	/** ジョブユニットID（危険） */
	private Text m_textJobunitIdCritical = null;
	/** ジョブユニットID（不明） */
	private Text m_textJobunitIdUnknown = null;

	/** ジョブID（通知） テキストボックス。 */
	private Text m_textJobIdInfo = null;
	/** ジョブID（警告） テキストボックス。 */
	private Text m_textJobIdWarning = null;
	/** ジョブID（異常） テキストボックス。 */
	private Text m_textJobIdCritical = null;
	/** ジョブID（不明） テキストボックス。 */
	private Text m_textJobIdUnknown = null;

	/** 参照（通知） ボタン。 */
	private Button m_buttonReferInfo = null;
	/** 参照（警告） ボタン。 */
	private Button m_buttonReferWarning = null;
	/** 参照（異常） ボタン。 */
	private Button m_buttonReferCritical = null;
	/** 参照（不明） ボタン。 */
	private Button m_buttonReferUnknown = null;

	/** 呼出失敗時の重要度（通知） コンボボックス。 */
	private Combo m_comboFailurePriorityInfo = null;
	/** 呼出失敗時の重要度（警告） コンボボックス。 */
	private Combo m_comboFailurePriorityWarning = null;
	/** 呼出失敗時の重要度（異常） コンボボックス。 */
	private Combo m_comboFailurePriorityCritical = null;
	/** 呼出失敗時の重要度（不明） コンボボックス。 */
	private Combo m_comboFailurePriorityUnknown = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public NotifyJobCreateDialog(Shell parent) {
		super(parent);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param notifyId 変更する通知情報の通知ID
	 * @param updateFlg 更新フラグ（true:更新する）
	 */
	public NotifyJobCreateDialog(Shell parent, String managerName, String notifyId, boolean updateFlg) {
		super(parent, managerName, notifyId, updateFlg);
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 * @see com.clustercontrol.notify.action.GetNotify#getNotify(String)
	 * @see #setInputData(NotifyInfo)
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// 通知IDが指定されている場合、その情報を初期表示する。
		NotifyInfo info = null;
		if(this.notifyId != null){
			info = new GetNotify().getNotify(this.managerName, this.notifyId);
		}
		else{
			info = new NotifyInfo();
		}
		this.setInputData(info);
	}

	/**
	 * 親のクラスから呼ばれ、各通知用のダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 */
	@Override
	protected void customizeSettingDialog(Composite parent) {
		final Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.notify.job.create.modify"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		/*
		 * ジョブ
		 */
		// ジョブグループ
		Group groupJob = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "job", groupJob);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 16;
		groupJob.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupJob.setLayoutData(gridData);
		groupJob.setText(Messages.getString("notifies.job"));

		/*
		 * スコープグループ
		 */
		Group groupScope = new Group(groupJob, SWT.NONE);
		WidgetTestUtil.setTestId(this, "scope", groupScope);
		groupScope.setText(Messages.getString("notify.job.scope"));
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 16;
		groupScope.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 16;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupScope.setLayoutData(gridData);

		// ジョブ変数 ラジオボタン
		this.m_radioGenerationNodeValue = new Button(groupScope, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "generationnodevalue", m_radioGenerationNodeValue);
		gridData = new GridData();
		gridData.horizontalSpan = 16;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioGenerationNodeValue.setLayoutData(gridData);
		this.m_radioGenerationNodeValue.setText(Messages.getString("notify.node.generation") + " : ");
		this.m_radioGenerationNodeValue.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_radioFixedValue.setSelection(false);
					m_scopeSelect.setEnabled(false);
				}
			}
		});

		// 固定値 ラジオボタン
		this.m_radioFixedValue = new Button(groupScope, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "fixedvalue", m_radioFixedValue);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioFixedValue.setLayoutData(gridData);
		this.m_radioFixedValue.setText(Messages.getString("notify.node.fix") + " : ");
		this.m_radioFixedValue.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_radioGenerationNodeValue.setSelection(false);
					m_scopeSelect.setEnabled(true);
				}
				update();
			}
		});

		this.m_textScope = new Text(groupScope, SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "scope", m_textScope);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textScope.setLayoutData(gridData);
		this.m_textScope.setText("");

		this.m_scopeSelect = new Button(groupScope, SWT.NONE);
		WidgetTestUtil.setTestId(this, "scopeselect", m_scopeSelect);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_scopeSelect.setLayoutData(gridData);
		this.m_scopeSelect.setText(Messages.getString("refer"));
		this.m_scopeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell,
						m_notifyBasic.getManagerListComposite().getText(),
						m_notifyBasic.getRoleIdList().getText());
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItem selectItem = dialog.getSelectItem();
					FacilityInfo info = selectItem.getData();
					FacilityPath path = new FacilityPath(
							ClusterControlPlugin.getDefault()
							.getSeparator());
					m_facilityPath = path.getPath(selectItem);
					m_facilityId = info.getFacilityId();
					m_textScope.setText(HinemosMessage.replace(m_facilityPath));
					update();
				}
			}
		});

		label = new Label(groupScope, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, label);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 空行
		label = new Label(groupJob, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 16;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 重要度 ごとの設定
		 */
		// ラベル（重要度）
		label = new Label(groupJob, SWT.NONE);
		WidgetTestUtil.setTestId(this, "priority", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// ラベル（実行する）
		label = new Label(groupJob, SWT.NONE);
		WidgetTestUtil.setTestId(this, "run", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_JOB_RUN;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.attribute"));

		// ラベル（ジョブユニットID）
		label = new Label(groupJob, SWT.NONE);
		WidgetTestUtil.setTestId(this, "jobunitid", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_JOBUNIT_ID;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("jobunit.id"));

		// ラベル（ジョブID）
		label = new Label(groupJob, SWT.NONE);
		WidgetTestUtil.setTestId(this, "jobid", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_JOB_ID;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("job.id"));

		// ラベル（参照）
		label = new Label(groupJob, SWT.NONE);
		WidgetTestUtil.setTestId(this, "refer", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_REF_BTN;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// ラベル（呼出失敗時）
		label = new Label(groupJob, SWT.NONE);
		WidgetTestUtil.setTestId(this, "failurecallvalue", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_FAILURE_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("failure.call.value"));

		// ジョブ 重要度：情報
		label = this.getLabelPriority(groupJob, Messages.getString("info"),PriorityColorConstant.COLOR_INFO);
		this.m_checkJobRunInfo = this.getCheckJobRun(groupJob);
		WidgetTestUtil.setTestId(this, "jobruninfo", m_checkJobRunInfo);
		this.m_textJobunitIdInfo = this.getTextJobunitId(groupJob);
		WidgetTestUtil.setTestId(this, "jobunitidinfo", m_textJobunitIdInfo);
		this.m_textJobIdInfo = this.getTextJobId(groupJob);
		WidgetTestUtil.setTestId(this, "jobidinfo", m_textJobIdInfo);
		this.m_buttonReferInfo = this.getButtonJobRefer(groupJob);
		WidgetTestUtil.setTestId(this, "referinfo", m_buttonReferInfo);
		this.m_comboFailurePriorityInfo = this.getComboPriority(groupJob);
		WidgetTestUtil.setTestId(this, "failurepriority", m_comboFailurePriorityInfo);
		this.m_buttonReferInfo.addSelectionListener(new JobIdSelectionListener(shell, m_notifyBasic.getManagerListComposite(), m_textJobunitIdInfo, m_textJobIdInfo, m_notifyBasic.getRoleIdList()));

		// ジョブ 重要度：警告
		label = this.getLabelPriority(groupJob, Messages.getString("warning"),PriorityColorConstant.COLOR_WARNING);
		this.m_checkJobRunWarning = this.getCheckJobRun(groupJob);
		WidgetTestUtil.setTestId(this, "jobrunwarning", m_checkJobRunWarning);
		this.m_textJobunitIdWarning = this.getTextJobunitId(groupJob);
		WidgetTestUtil.setTestId(this, "jobunitidwarning", m_textJobunitIdWarning);
		this.m_textJobIdWarning = this.getTextJobId(groupJob);
		WidgetTestUtil.setTestId(this, "jobidwarnitng", m_textJobIdWarning);
		this.m_buttonReferWarning = this.getButtonJobRefer(groupJob);
		WidgetTestUtil.setTestId(this, "referwarning", m_buttonReferWarning);
		this.m_comboFailurePriorityWarning = this.getComboPriority(groupJob);
		WidgetTestUtil.setTestId(this, "failureprioritywarning", m_comboFailurePriorityWarning);
		this.m_buttonReferWarning.addSelectionListener(new JobIdSelectionListener(shell, m_notifyBasic.getManagerListComposite(), m_textJobunitIdWarning, m_textJobIdWarning, m_notifyBasic.getRoleIdList()));

		// ジョブ 重要度：危険
		label = this.getLabelPriority(groupJob, Messages.getString("critical"),PriorityColorConstant.COLOR_CRITICAL);
		this.m_checkJobRunCritical = this.getCheckJobRun(groupJob);
		WidgetTestUtil.setTestId(this, "criticalcheck", m_checkJobRunCritical);
		this.m_textJobunitIdCritical = this.getTextJobunitId(groupJob);
		WidgetTestUtil.setTestId(this, "jobunitidcritical", m_textJobunitIdCritical);
		this.m_textJobIdCritical = this.getTextJobId(groupJob);
		WidgetTestUtil.setTestId(this, "jobidcritical", m_textJobIdCritical);
		this.m_buttonReferCritical = this.getButtonJobRefer(groupJob);
		WidgetTestUtil.setTestId(this, "refercritical", m_buttonReferCritical);
		this.m_comboFailurePriorityCritical = this.getComboPriority(groupJob);
		WidgetTestUtil.setTestId(this, "failureprioritycritical", m_comboFailurePriorityCritical);
		this.m_buttonReferCritical.addSelectionListener(new JobIdSelectionListener(shell, m_notifyBasic.getManagerListComposite(), m_textJobunitIdCritical, m_textJobIdCritical, m_notifyBasic.getRoleIdList()));

		// ジョブ 重要度：不明
		label = this.getLabelPriority(groupJob, Messages.getString("unknown"),PriorityColorConstant.COLOR_UNKNOWN);
		this.m_checkJobRunUnknown = this.getCheckJobRun(groupJob);
		WidgetTestUtil.setTestId(this, "jobrununknown", m_checkJobRunUnknown);
		this.m_textJobunitIdUnknown = this.getTextJobunitId(groupJob);
		WidgetTestUtil.setTestId(this, "jobunitidunknown", m_textJobunitIdUnknown);
		this.m_textJobIdUnknown = this.getTextJobId(groupJob);
		WidgetTestUtil.setTestId(this, "jobidunknown", m_textJobIdUnknown);
		this.m_buttonReferUnknown = this.getButtonJobRefer(groupJob);
		WidgetTestUtil.setTestId(this, "referunknown", m_buttonReferUnknown);
		this.m_comboFailurePriorityUnknown = this.getComboPriority(groupJob);
		WidgetTestUtil.setTestId(this, "failurepriorityunknown", m_comboFailurePriorityUnknown);
		this.m_buttonReferUnknown.addSelectionListener(new JobIdSelectionListener(shell, m_notifyBasic.getManagerListComposite(), m_textJobunitIdUnknown, m_textJobIdUnknown, m_notifyBasic.getRoleIdList()));
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示

		// ジョブ実行スコープ
		if(this.m_radioFixedValue.getSelection() && "".equals(this.m_textScope.getText())){
			this.m_textScope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 情報
		if(this.m_checkJobRunInfo.getSelection() && "".equals(this.m_textJobunitIdInfo.getText())){
			this.m_textJobunitIdInfo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			this.m_textJobIdInfo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textJobunitIdInfo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.m_textJobIdInfo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 警告
		if(this.m_checkJobRunWarning.getSelection() && "".equals(this.m_textJobunitIdWarning.getText())){
			this.m_textJobunitIdWarning.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			this.m_textJobIdWarning.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textJobunitIdWarning.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.m_textJobIdWarning.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 危険
		if(this.m_checkJobRunCritical.getSelection() && "".equals(this.m_textJobunitIdCritical.getText())){
			this.m_textJobunitIdCritical.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			this.m_textJobIdCritical.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textJobunitIdCritical.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.m_textJobIdCritical.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 不明
		if(this.m_checkJobRunUnknown.getSelection() && "".equals(this.m_textJobunitIdUnknown.getText())){
			this.m_textJobunitIdUnknown.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			this.m_textJobIdUnknown.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textJobunitIdUnknown.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.m_textJobIdUnknown.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
	/**
	 * 入力値を保持した通知情報を返します。
	 *
	 * @return 通知情報
	 */
	@Override
	public NotifyInfo getInputData() {
		return this.inputData;
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param notify 設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(NotifyInfo notify) {
		super.setInputData(notify);

		// コマンド情報
		NotifyJobInfo info = notify.getNotifyJobInfo();
		if (info != null) {
			this.setInputDatal(info);
		} else {
			// 新規追加の場合
			this.m_radioGenerationNodeValue.setSelection(true);
			this.m_scopeSelect.setEnabled(false);
		}
	}

	private void setInputDatal(NotifyJobInfo job) {
		if (job.getJobExecFacility() != null) {
			this.m_facilityId = job.getJobExecFacility();
			this.m_textScope.setText(HinemosMessage.replace(job.getJobExecScope()));
		}
		if (job.getJobExecFacilityFlg() != null && job.getJobExecFacilityFlg() == ExecFacilityConstant.TYPE_GENERATION) {
			this.m_radioGenerationNodeValue.setSelection(true);
			this.m_scopeSelect.setEnabled(false);
		}
		else {
			this.m_radioFixedValue.setSelection(true);
			this.m_scopeSelect.setEnabled(true);
		}

		Boolean[] validFlgs = getValidFlgs(job);
		Button[] checkJobRuns = new Button[] {
				this.m_checkJobRunInfo,
				this.m_checkJobRunWarning,
				this.m_checkJobRunCritical,
				this.m_checkJobRunUnknown
		};
		String[] jobunitIds = new String[] {
				job.getInfoJobunitId(),
				job.getWarnJobunitId(),
				job.getCriticalJobunitId(),
				job.getUnknownJobunitId()
		};
		Text[] textJobunitIds = new Text[] {
				this.m_textJobunitIdInfo,
				this.m_textJobunitIdWarning,
				this.m_textJobunitIdCritical,
				this.m_textJobunitIdUnknown
		};
		String[] jobIds = new String[] {
				job.getInfoJobId(),
				job.getWarnJobId(),
				job.getCriticalJobId(),
				job.getUnknownJobId()
		};
		Text[] textJobIds = new Text[] {
				this.m_textJobIdInfo,
				this.m_textJobIdWarning,
				this.m_textJobIdCritical,
				this.m_textJobIdUnknown
		};
		Integer[] jobFailurePriorities = new Integer[] {
				job.getInfoJobFailurePriority(),
				job.getWarnJobFailurePriority(),
				job.getCriticalJobFailurePriority(),
				job.getUnknownJobFailurePriority()
		};
		Combo[] comboFailurePriorities = new Combo[] {
				this.m_comboFailurePriorityInfo,
				this.m_comboFailurePriorityWarning,
				this.m_comboFailurePriorityCritical,
				this.m_comboFailurePriorityUnknown
		};


		for (int i = 0; i < validFlgs.length; i++) {
			boolean valid = validFlgs[i].booleanValue();
			checkJobRuns[i].setSelection(valid);
			WidgetTestUtil.setTestId(this, "checkJobRuns" + i, checkJobRuns[i]);

			//ジョブユニットID
			if (jobunitIds[i] != null){
				textJobunitIds[i].setText(jobunitIds[i]);
				WidgetTestUtil.setTestId(this, "textJobunitIds" + i, textJobunitIds[i]);
			}

			// ジョブID
			if (jobIds[i] != null) {
				textJobIds[i].setText(jobIds[i]);
				WidgetTestUtil.setTestId(this, "textJobIds" + i, textJobIds[i]);
			}

			// ジョブ失敗時の重要度
			if (jobFailurePriorities[i] != null) {
				comboFailurePriorities[i].setText(PriorityMessage.typeToString(jobFailurePriorities[i]));
				WidgetTestUtil.setTestId(this, "comboFailurePriorities" + i, comboFailurePriorities[i]);
			}
		}
	}

	/**
	 * 引数で指定された通知ジョブ情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる通知ジョブ情報
	 * @param checkJobRun 通知チェックボックス
	 * @param textJobId ジョブIDテキストボックス
	 * @param checkInhibition 抑制チェックボックス
	 * @param comboFailurePriority 呼出失敗時の重要度
	 */
	protected void setInputDataForJob(NotifyJobInfo info,
			Button checkJobRun,
			Text textJobunitId,
			Text textJobId,
			Combo comboFailurePriority
			) {
	}

	/**
	 * 入力値を設定した通知情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知情報
	 *
	 * @see #createInputDataForJob(ArrayList, int, Button, Text, Button, Combo)
	 */
	@Override
	protected NotifyInfo createInputData() {
		NotifyInfo info = super.createInputData();

		// 通知タイプの設定
		info.setNotifyType(TYPE_JOB);

		// コマンド情報
		NotifyJobInfo notifyJobInfo = createNotifyInfoDetail();
		info.setNotifyJobInfo(notifyJobInfo);
		return info;
	}

	private NotifyJobInfo createNotifyInfoDetail() {
		// ジョブ情報
		NotifyJobInfo job = new NotifyJobInfo();

		//　実行
		job.setInfoValidFlg(m_checkJobRunInfo.getSelection());
		job.setWarnValidFlg(m_checkJobRunWarning.getSelection());
		job.setCriticalValidFlg(m_checkJobRunCritical.getSelection());
		job.setUnknownValidFlg(m_checkJobRunUnknown.getSelection());


		// jobunitId
		if (isNotNullAndBlank(m_textJobunitIdInfo.getText())) {
			job.setInfoJobunitId(m_textJobunitIdInfo.getText());
		}
		if (isNotNullAndBlank(m_textJobunitIdWarning.getText())) {
			job.setWarnJobunitId(m_textJobunitIdWarning.getText());
		}
		if (isNotNullAndBlank(m_textJobunitIdCritical.getText())) {
			job.setCriticalJobunitId(m_textJobunitIdCritical.getText());
		}
		if (isNotNullAndBlank(m_textJobunitIdUnknown.getText())) {
			job.setUnknownJobunitId(m_textJobunitIdUnknown.getText());
		}

		// jobId
		if (isNotNullAndBlank(m_textJobIdInfo.getText())) {
			job.setInfoJobId(m_textJobIdInfo.getText());
		}
		if (isNotNullAndBlank(m_textJobIdWarning.getText())) {
			job.setWarnJobId(m_textJobIdWarning.getText());
		}
		if (isNotNullAndBlank(m_textJobIdCritical.getText())) {
			job.setCriticalJobId(m_textJobIdCritical.getText());
		}
		if (isNotNullAndBlank(m_textJobIdUnknown.getText())) {
			job.setUnknownJobId(m_textJobIdUnknown.getText());
		}

		// 呼出失敗時
		if (isNotNullAndBlank(m_comboFailurePriorityInfo.getText())) {
			job.setInfoJobFailurePriority(PriorityMessage.stringToType(m_comboFailurePriorityInfo.getText()));
		}
		if (isNotNullAndBlank(m_comboFailurePriorityWarning.getText())) {
			job.setWarnJobFailurePriority(PriorityMessage.stringToType(m_comboFailurePriorityWarning.getText()));
		}
		if (isNotNullAndBlank(m_comboFailurePriorityCritical.getText())) {
			job.setCriticalJobFailurePriority(PriorityMessage.stringToType(m_comboFailurePriorityCritical.getText()));
		}
		if (isNotNullAndBlank(m_comboFailurePriorityUnknown.getText())) {
			job.setUnknownJobFailurePriority(PriorityMessage.stringToType(m_comboFailurePriorityUnknown.getText()));
		}

		// 共通部分登録
		// 実行ファシリティID
		if (isNotNullAndBlank(this.m_textScope.getText())) {
			job.setJobExecFacility(this.m_facilityId);
			job.setJobExecScope(this.m_textScope.getText());
		}
		// 実行ファシリティ
		if (this.m_radioGenerationNodeValue.getSelection()) {
			job.setJobExecFacilityFlg(ExecFacilityConstant.TYPE_GENERATION);
		}
		else if (this.m_radioFixedValue.getSelection()){
			job.setJobExecFacilityFlg(ExecFacilityConstant.TYPE_FIX);
		}

		return job;
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		// 入力値生成
		this.inputData = this.createInputData();

		return super.validate();
	}

	/**
	 * 入力値をマネージャに登録します。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		NotifyInfo info = this.getInputData();
		if(info != null){
			if (!this.updateFlg) {
				// 作成の場合
				result = new AddNotify().add(this.getInputManagerName(), info);
			}
			else{
				// 変更の場合
				result = new ModifyNotify().modify(this.getInputManagerName(), info);
			}
		}

		return result;
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 */
	@Override
	protected void setValidateResult(String id, String message) {

		this.validateResult = new ValidateResult();
		this.validateResult.setValid(false);
		this.validateResult.setID(id);
		this.validateResult.setMessage(message);
	}

	/**
	 * ボタンを生成します。<BR>
	 * 参照フラグが<code> true </code>の場合は閉じるボタンを生成し、<code> false </code>の場合は、デフォルトのボタンを生成します。
	 *
	 * @param parent ボタンバーコンポジット
	 *
	 * @see #createButtonsForButtonBar(Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		if(!this.referenceFlg){
			super.createButtonsForButtonBar(parent);
		}
		else{
			// 閉じるボタン
			this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), false);
		}
	}

	/**
	 * コンポジットの選択可/不可を設定します。
	 *
	 * @param enable 選択可の場合、<code> true </code>
	 */
	@Override
	protected void setEnabled(boolean enable) {
		super.m_notifyBasic.setEnabled(enable);
		super.m_notifyInhibition.setEnabled(enable);
	}

	/**
	 * 重要度のラベルを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param text ラベルに表示するテキスト
	 * @param background ラベルの背景色
	 * @return 生成されたラベル
	 */
	private Label getLabelPriority(Composite parent,
			String text,
			Color background
			) {

		// ラベル（重要度）
		Label label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "labelpriority", label);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(text + " : ");
		label.setBackground(background);

		return label;
	}

	/**
	 * ジョブの実行チェックボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Button getCheckJobRun(Composite parent) {

		// チェックボックス（実行）
		Button button = new Button(parent, SWT.CHECK);
		WidgetTestUtil.setTestId(this, null, button);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_JOB_RUN;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		return button;
	}

	/**
	 * ジョブユニットIDテキストボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Text getTextJobunitId(Composite parent) {
		// テキストボックス（ジョブユニットID）
		Text notifyJobCreateJobUnitIdText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "jobunitid", notifyJobCreateJobUnitIdText);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_JOBUNIT_ID;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		notifyJobCreateJobUnitIdText.setLayoutData(gridData);
		notifyJobCreateJobUnitIdText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		return notifyJobCreateJobUnitIdText;
	}

	/**
	 * ジョブジョブIDテキストボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Text getTextJobId(Composite parent) {
		// テキストボックス（ジョブID）
		Text notifyJobCreateJobIdText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "job", notifyJobCreateJobIdText);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_JOB_ID;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		notifyJobCreateJobIdText.setLayoutData(gridData);

		return notifyJobCreateJobIdText;
	}

	/**
	 * ジョブの参照ボタンを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Button getButtonJobRefer(Composite parent) {
		// チェックボックス（参照）
		Button button = new Button(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, button);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_REF_BTN;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);
		button.setText(Messages.getString("refer"));

		return button;
	}

	/**
	 * ジョブの重要度のコンボボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param horizontalSpan コンボボックスのカラム数
	 * @return コンボボックス
	 */
	private Combo getComboPriority(Composite parent) {
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, combo);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_FAILURE_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		combo.setLayoutData(gridData);
		combo.add(PriorityMessage.STRING_CRITICAL);
		combo.add(PriorityMessage.STRING_WARNING);
		combo.add(PriorityMessage.STRING_INFO);
		combo.add(PriorityMessage.STRING_UNKNOWN);
		combo.setText(PriorityMessage.STRING_UNKNOWN);

		return combo;
	}

	@Override
	public void setOwnerRoleId(String ownerRoleId) {
		super.setOwnerRoleId(ownerRoleId);
		this.m_facilityPath = "";
		this.m_facilityId = "";
		this.m_textScope.setText(HinemosMessage.replace(m_facilityPath));

		this.m_textJobunitIdInfo.setText("");
		this.m_textJobunitIdWarning.setText("");
		this.m_textJobunitIdCritical.setText("");
		this.m_textJobunitIdUnknown.setText("");
		this.m_textJobIdInfo.setText("");
		this.m_textJobIdWarning.setText("");
		this.m_textJobIdCritical.setText("");
		this.m_textJobIdUnknown.setText("");
	}

}
