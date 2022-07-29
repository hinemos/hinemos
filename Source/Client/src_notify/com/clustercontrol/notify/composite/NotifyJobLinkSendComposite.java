/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobLinkSendSettingResponse;
import org.openapitools.client.model.JobNotifyDetailInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.view.action.ModifyJobLinkSendSettingAction;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * ジョブ通知 ジョブ連携メッセージ送信用のコンポジットクラスです。
 *
 */
public class NotifyJobLinkSendComposite extends Composite {

	/** ジョブ連携送信設定ID用コンボボックス */
	private Combo m_joblinkSendSettingIdCombo = null;
	/** ジョブ連携送信設定参照用ボタン */
	private Button m_joblinkSendSettingReferBtn = null;

	/** 送信に失敗した場合再送する(チェックボックス) */
	private Button m_retry = null;

	/** 再送回数(テキスト) */
	private Text m_retryCount = null;

	/** 重要度（情報）有効/無効 */
	private Button m_infoValid = null;
	/** 重要度（警告）有効/無効 */
	private Button m_warnValid = null;
	/** 重要度（危険）有効/無効 */
	private Button m_criticalValid = null;
	/** 重要度（不明）有効/無効 */
	private Button m_unknownValid = null;

	/** 送信成功時にINTERNALイベントを出力する チェックボックス。 */
	private Button m_successInternal = null;
	/** 送信失敗時にINTERNALイベントを出力する チェックボックス。 */
	private Button m_failureInternal = null;

	/** マネージャ名 */
	private String m_managerName = null;

	/** オーナーロールID */
	private String m_ownerRoleId = null;

	/** シェル */
	private Shell m_shell = null;

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
	public NotifyJobLinkSendComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
		m_shell = this.getShell();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		// Composite
		Composite composite = null;
		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		/*
		 * ジョブ
		 */
		// ジョブグループ
		Group groupJob = new Group(this, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		groupJob.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupJob.setLayoutData(gridData);
		groupJob.setText(Messages.getString("notifies.job.joblinksend"));

		Composite joblinkComposite = new Composite(groupJob, SWT.NONE);
		joblinkComposite.setLayout(new GridLayout(3, false));

		// ジョブ連携送信設定ID(ラベル)
		label = new Label(joblinkComposite, SWT.NONE);
		label.setText(Messages.getString("joblink.send.setting.id") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// ジョブ連携送信設定ID(コンボボックス)
		this.m_joblinkSendSettingIdCombo = new Combo(joblinkComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.m_joblinkSendSettingIdCombo.setLayoutData(new GridData(250,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_joblinkSendSettingIdCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// 監視設定：参照（ボタン）
		this.m_joblinkSendSettingReferBtn = new Button(joblinkComposite, SWT.NONE);
		this.m_joblinkSendSettingReferBtn.setText(Messages.getString("refer"));
		this.m_joblinkSendSettingReferBtn.setLayoutData(new GridData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_joblinkSendSettingReferBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_joblinkSendSettingIdCombo.getText() == null || m_joblinkSendSettingIdCombo.getText().isEmpty()) {
					return;
				}
				ModifyJobLinkSendSettingAction action = new ModifyJobLinkSendSettingAction();
				if (action.dialogOpen(m_shell, m_managerName, m_joblinkSendSettingIdCombo.getText()) 
						== IDialogConstants.OK_ID) {
					update();
				}
			}
		});

		// 送信に失敗した場合再送する
		Composite retryComposite = new Composite(groupJob, SWT.NONE);
		retryComposite.setLayout(new RowLayout());

		// 送信に失敗した場合に再送する（チェックボックス）
		this.m_retry = new Button(retryComposite, SWT.CHECK);
		this.m_retry.setText(Messages.getString("joblink.send.failure.retry"));
		this.m_retry.setLayoutData(new RowData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_retry.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_retryCount.setEditable(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		composite = new Composite(retryComposite, SWT.NONE);
		composite.setLayoutData(new RowData(100, SizeConstant.SIZE_BUTTON_HEIGHT));

		// 再送回数（ラベル）
		label = new Label(retryComposite, SWT.NONE);
		label.setText(Messages.getString("resend.count") + " : ");
		label.setLayoutData(new RowData(100, SizeConstant.SIZE_TEXT_HEIGHT));

		// 再送回数（テキスト）
		this.m_retryCount = new Text(retryComposite, SWT.BORDER);
		this.m_retryCount.setLayoutData(new RowData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_retryCount.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_retryCount.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		this.m_retryCount.setEditable(m_retry.getSelection());

		// 重要度（グループ）
		Group priorityGroup = new Group(groupJob, SWT.NONE);
		priorityGroup.setText(Messages.getString("priority"));
		priorityGroup.setLayoutData(new GridData());
		((GridData)priorityGroup.getLayoutData()).horizontalSpan = 3;
		priorityGroup.setLayout(new GridLayout(8, true));

		// 重要度(レイアウト)
		GridData priorityLabelGridData = new GridData(60, SizeConstant.SIZE_BUTTON_HEIGHT);
		priorityLabelGridData.horizontalAlignment = GridData.FILL;
		priorityLabelGridData.grabExcessHorizontalSpace = true;
		GridData priorityCheckGridData = new GridData(30, SizeConstant.SIZE_BUTTON_HEIGHT);
		priorityCheckGridData.horizontalAlignment = GridData.CENTER;
		priorityCheckGridData.grabExcessHorizontalSpace = true;

		// 重要度：情報
		label = new Label(priorityGroup, SWT.NONE);
		label.setLayoutData(priorityLabelGridData);
		label.setText(Messages.getString("info") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_INFO);
		m_infoValid = new Button(priorityGroup, SWT.CHECK);
		m_infoValid.setLayoutData(priorityCheckGridData);

		// 重要度：警告
		label = new Label(priorityGroup, SWT.NONE);
		label.setLayoutData(priorityLabelGridData);
		label.setText(Messages.getString("warning") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_WARNING);
		m_warnValid = new Button(priorityGroup, SWT.CHECK);
		m_warnValid.setLayoutData(priorityCheckGridData);

		// 重要度：危険
		label = new Label(priorityGroup, SWT.NONE);
		label.setLayoutData(priorityLabelGridData);
		label.setText(Messages.getString("critical") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_CRITICAL);
		m_criticalValid = new Button(priorityGroup, SWT.CHECK);
		m_criticalValid.setLayoutData(priorityCheckGridData);

		// 重要度：不明
		label = new Label(priorityGroup, SWT.NONE);
		label.setLayoutData(priorityLabelGridData);
		label.setText(Messages.getString("unknown") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_UNKNOWN);
		m_unknownValid = new Button(priorityGroup, SWT.CHECK);
		m_unknownValid.setLayoutData(priorityCheckGridData);

		// 通知（グループ）
		Group notifyGroup = new Group(groupJob, SWT.NONE);
		notifyGroup.setText(Messages.getString("notify.send.internal"));
		notifyGroup.setLayoutData(new GridData());
		((GridData)notifyGroup.getLayoutData()).horizontalSpan = 3;
		notifyGroup.setLayout(new RowLayout());

		// 通知成功時にINTERNALイベントを出力する（チェックボックス）
		this.m_successInternal = new Button(notifyGroup, SWT.CHECK);
		this.m_successInternal.setText(Messages.getString("notify.send.success"));
		this.m_successInternal.setLayoutData(new RowData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));
		composite = new Composite(notifyGroup, SWT.NONE);
		composite.setLayoutData(new RowData(100, SizeConstant.SIZE_BUTTON_HEIGHT));
		
		// 通知失敗時にINTERNALイベントを出力する（チェックボックス）
		this.m_failureInternal = new Button(notifyGroup, SWT.CHECK);
		this.m_failureInternal.setText(Messages.getString("notify.send.failure"));
		this.m_failureInternal.setLayoutData(new RowData(SWT.DEFAULT, SizeConstant.SIZE_BUTTON_HEIGHT));
		composite = new Composite(notifyGroup, SWT.NONE);
		composite.setLayoutData(new RowData(100, SizeConstant.SIZE_BUTTON_HEIGHT));

		// 更新処理
		update();
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目を明示
		if (m_joblinkSendSettingIdCombo.getEnabled() && "".equals(m_joblinkSendSettingIdCombo.getText())) {
			this.m_joblinkSendSettingIdCombo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_joblinkSendSettingIdCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_retryCount.getEditable() && "".equals(m_retryCount.getText())) {
			this.m_retryCount.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_retryCount.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}


	/**
	 * 引数で指定されたジョブ通知情報の値を、各項目に設定します。
	 *
	 * @param notifyJobInfo 設定値として用いるジョブ通知情報
	 */
	public void setInputData(JobNotifyDetailInfoResponse notifyJobInfo) {

		// 初期値設定
		m_retry.setSelection(false);
		m_retryCount.setText("");
		m_infoValid.setSelection(false);
		m_warnValid.setSelection(false);
		m_criticalValid.setSelection(false);
		m_unknownValid.setSelection(false);
		m_successInternal.setSelection(false);
		m_failureInternal.setSelection(false);

		// ジョブ連携送信設定ID
		updateJoblinkSendSettingIdCombo(notifyJobInfo);

		if (notifyJobInfo != null) {
			// 送信に失敗した場合再送する
			if (notifyJobInfo.getRetryFlg() != null) {
				m_retry.setSelection(notifyJobInfo.getRetryFlg());
			}

			// 再送回数
			if (notifyJobInfo.getRetryCount() != null) {
				m_retryCount.setText(String.valueOf(notifyJobInfo.getRetryCount()));
			}

			// 重要度（情報）
			if (notifyJobInfo.getInfoValidFlg() != null) {
				m_infoValid.setSelection(notifyJobInfo.getInfoValidFlg());
			}

			// 重要度（警告）
			if (notifyJobInfo.getWarnValidFlg() != null) {
				m_warnValid.setSelection(notifyJobInfo.getWarnValidFlg());
			}

			// 重要度（危険）
			if (notifyJobInfo.getCriticalValidFlg() != null) {
				m_criticalValid.setSelection(notifyJobInfo.getCriticalValidFlg());
			}

			// 重要度（不明）
			if (notifyJobInfo.getUnknownValidFlg() != null) {
				m_unknownValid.setSelection(notifyJobInfo.getUnknownValidFlg());
			}

			// 送信成功時にINTERNALイベントを出力する
			if (notifyJobInfo.getSuccessInternalFlg() != null) {
				m_successInternal.setSelection(notifyJobInfo.getSuccessInternalFlg());
			}

			// 送信失敗時にINTERNALイベントを出力する
			if (notifyJobInfo.getFailureInternalFlg() != null) {
				m_failureInternal.setSelection(notifyJobInfo.getFailureInternalFlg());
			}
		}

		// オブジェクトの有効/無効設定
		m_retryCount.setEditable(m_retry.getSelection());

		update();
	}

	/**
	 * 引数で指定されたジョブ通知情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は認証結果を返します。
	 * 不正ではない場合は、<code>null</code>を返します。
	 *
	 * @param notifyJobInfo 入力値を設定するジョブ通知情報
	 * @return 検証結果
	 *
	 */
	public ValidateResult createInputData(NotifyInfoInputData info) {
		ValidateResult result = null;

		// インスタンスを作成
		JobNotifyDetailInfoResponse notifyJobInfo = new JobNotifyDetailInfoResponse();
		notifyJobInfo.setNotifyJobType(JobNotifyDetailInfoResponse.NotifyJobTypeEnum.JOB_LINK_SEND);

		// ジョブ連携送信設定ID
		notifyJobInfo.setJoblinkSendSettingId(m_joblinkSendSettingIdCombo.getText());

		// 送信に失敗した場合再送する
		notifyJobInfo.setRetryFlg(m_retry.getSelection());

		// 再送回数
		try {
			notifyJobInfo.setRetryCount(
					Integer.parseInt(m_retryCount.getText()));
		} catch (NumberFormatException e) {
			if (notifyJobInfo.getRetryFlg()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.common.1", 
						new String[]{Messages.getString("resend.count")}));
				return result;
			}
		}

		// 重要度（情報）
		notifyJobInfo.setInfoValidFlg(m_infoValid.getSelection());
		// 重要度（警告）
		notifyJobInfo.setWarnValidFlg(m_warnValid.getSelection());
		// 重要度（危険）
		notifyJobInfo.setCriticalValidFlg(m_criticalValid.getSelection());
		// 重要度（不明）
		notifyJobInfo.setUnknownValidFlg(m_unknownValid.getSelection());
		// 送信成功時にINTERNALイベントを出力する
		notifyJobInfo.setSuccessInternalFlg(m_successInternal.getSelection());
		// 送信失敗時にINTERNALイベントを出力する
		notifyJobInfo.setFailureInternalFlg(m_failureInternal.getSelection());

		info.setNotifyJobInfo(notifyJobInfo);

		return result;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_joblinkSendSettingIdCombo.setEnabled(enabled);
		this.m_retry.setEnabled(enabled);
		this.m_retryCount.setEditable(this.m_retry.getSelection() && enabled);
		this.m_infoValid.setEnabled(enabled);
		this.m_warnValid.setEnabled(enabled);
		this.m_criticalValid.setEnabled(enabled);
		this.m_unknownValid.setEnabled(enabled);
		this.m_successInternal.setEnabled(enabled);
		this.m_failureInternal.setEnabled(enabled);
	}

	public void setOwnerRoleId(String ownerRoleId) {
		m_ownerRoleId = ownerRoleId;
		// ジョブ連携送信設定ID
		updateJoblinkSendSettingIdCombo(null);
	}

	/**
	 * @param m_managerName the m_managerName to set
	 */
	public void setManagerName(String managerName) {
		m_managerName = managerName;
	}

	/**
	 * ジョブ連携送信設定IDコンボボックス更新
	 * 
	 */
	private void updateJoblinkSendSettingIdCombo(JobNotifyDetailInfoResponse notifyJobInfo) {
		List<JobLinkSendSettingResponse> joblinkSendSettingList = new ArrayList<>();
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(this.m_managerName);
			joblinkSendSettingList = wrapper.getJobLinkSendSettingList(this.m_ownerRoleId);
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		this.m_joblinkSendSettingIdCombo.removeAll();
		String selectId = "";
		if (joblinkSendSettingList != null && joblinkSendSettingList.size() > 0) {
			for (JobLinkSendSettingResponse jobLinkSendSetting : joblinkSendSettingList) {
				if (notifyJobInfo != null
						&& notifyJobInfo.getJoblinkSendSettingId() != null
						&& !notifyJobInfo.getJoblinkSendSettingId().equals("")
						&& notifyJobInfo.getJoblinkSendSettingId().equals(jobLinkSendSetting.getJoblinkSendSettingId())) {
					selectId = jobLinkSendSetting.getJoblinkSendSettingId();
				}
				this.m_joblinkSendSettingIdCombo.add(jobLinkSendSetting.getJoblinkSendSettingId());
			}
		}
		this.m_joblinkSendSettingIdCombo.setText(selectId);
		update();
	}
}
