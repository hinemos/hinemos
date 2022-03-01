/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.composite;

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
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.JobNotifyDetailInfoResponse;
import org.openapitools.client.model.JobNotifyDetailInfoResponse.CriticalJobFailurePriorityEnum;
import org.openapitools.client.model.JobNotifyDetailInfoResponse.InfoJobFailurePriorityEnum;
import org.openapitools.client.model.JobNotifyDetailInfoResponse.JobExecFacilityFlgEnum;
import org.openapitools.client.model.JobNotifyDetailInfoResponse.UnknownJobFailurePriorityEnum;
import org.openapitools.client.model.JobNotifyDetailInfoResponse.WarnJobFailurePriorityEnum;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.dialog.JobTreeDialog;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * ジョブ通知 直接実行用のコンポジットクラスです。
 *
 */
public class NotifyJobDirectComposite extends Composite {

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

	/** シェル */
	private Shell m_shell = null;
	/** オーナーロールID */
	private String m_ownerRoleId = null;
	/** マネージャ名 */
	private String m_managerName = null;

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
	public NotifyJobDirectComposite(Composite parent, int style) {
		super(parent, style);
		m_shell = this.getShell();
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

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
		layout.numColumns = 16;
		groupJob.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupJob.setLayoutData(gridData);
		groupJob.setText(Messages.getString("notifies.job.direct"));

		/*
		 * スコープグループ
		 */
		Group groupScope = new Group(groupJob, SWT.NONE);
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
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioGenerationNodeValue.setLayoutData(gridData);
		this.m_radioGenerationNodeValue.setText(Messages.getString("notify.node.generation") + " : ");
		this.m_radioGenerationNodeValue.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					m_radioFixedValue.setSelection(false);
					m_scopeSelect.setEnabled(false);
				}
			}
		});
		this.m_radioGenerationNodeValue.setSelection(true);

		// 固定値 ラジオボタン
		this.m_radioFixedValue = new Button(groupScope, SWT.RADIO);
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
				if (check.getSelection()) {
					m_radioGenerationNodeValue.setSelection(false);
					m_scopeSelect.setEnabled(true);
				}
				update();
			}
		});
		this.m_radioFixedValue.setSelection(false);

		this.m_textScope = new Text(groupScope, SWT.BORDER | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textScope.setLayoutData(gridData);
		this.m_textScope.setText("");

		this.m_scopeSelect = new Button(groupScope, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_scopeSelect.setLayoutData(gridData);
		this.m_scopeSelect.setText(Messages.getString("refer"));
		this.m_scopeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(m_shell, m_managerName, m_ownerRoleId);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse selectItem = dialog.getSelectItem();
					FacilityInfoResponse info = selectItem.getData();
					FacilityPath path = new FacilityPath( ClusterControlPlugin.getDefault().getSeparator());
					m_facilityPath = path.getPath(selectItem);
					m_facilityId = info.getFacilityId();
					m_textScope.setText(HinemosMessage.replace(m_facilityPath));
					update();
				}
			}
		});
		m_scopeSelect.setEnabled(false);

		/*
		 * 重要度 ごとの設定
		 */
		// ラベル（重要度）
		label = new Label(groupJob, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// ラベル（実行する）
		label = new Label(groupJob, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.attribute"));

		// ラベル（ジョブユニットID）
		label = new Label(groupJob, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("jobunit.id"));

		// ラベル（ジョブID）
		label = new Label(groupJob, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("job.id"));

		// ラベル（参照）
		label = new Label(groupJob, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// ラベル（呼出失敗時）
		label = new Label(groupJob, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("failure.call.value"));

		// ジョブ 重要度：情報
		label = createPriorityLabelWidget(groupJob, Messages.getString("info"),PriorityColorConstant.COLOR_INFO);
		this.m_checkJobRunInfo = createJobRunCheckWidget(groupJob);
		this.m_textJobunitIdInfo = createJobunitIdTextWidget(groupJob);
		this.m_textJobIdInfo = createJobIdTextWidget(groupJob);
		this.m_buttonReferInfo = createJobReferButtonWidget(groupJob);
		this.m_comboFailurePriorityInfo = createPriorityComboWidget(groupJob);
		this.m_buttonReferInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// ジョブツリーダイアログ表示
				JobTreeDialog dialog = new JobTreeDialog(m_shell, m_managerName, m_ownerRoleId, false);
				if (dialog.open() == IDialogConstants.OK_ID) {
					JobTreeItemWrapper selectItem = dialog.getSelectItem().get(0);
					if (selectItem.getData().getType() != JobInfoWrapper.TypeEnum.COMPOSITE) {
						m_textJobunitIdInfo.setText(selectItem.getData().getJobunitId());
						m_textJobIdInfo.setText(selectItem.getData().getId());
					} else {
						m_textJobunitIdInfo.setText("");
						m_textJobIdInfo.setText("");
					}
				}
			}
		});

		// ジョブ 重要度：警告
		label = createPriorityLabelWidget(groupJob, Messages.getString("warning"),PriorityColorConstant.COLOR_WARNING);
		this.m_checkJobRunWarning = createJobRunCheckWidget(groupJob);
		this.m_textJobunitIdWarning = createJobunitIdTextWidget(groupJob);
		this.m_textJobIdWarning = createJobIdTextWidget(groupJob);
		this.m_buttonReferWarning = createJobReferButtonWidget(groupJob);
		this.m_comboFailurePriorityWarning = createPriorityComboWidget(groupJob);
		this.m_buttonReferWarning.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// ジョブツリーダイアログ表示
				JobTreeDialog dialog = new JobTreeDialog(m_shell, m_managerName, m_ownerRoleId, false);
				if (dialog.open() == IDialogConstants.OK_ID) {
					JobTreeItemWrapper selectItem = dialog.getSelectItem().get(0);
					if (selectItem.getData().getType() != JobInfoWrapper.TypeEnum.COMPOSITE) {
						m_textJobunitIdWarning.setText(selectItem.getData().getJobunitId());
						m_textJobIdWarning.setText(selectItem.getData().getId());
					} else {
						m_textJobunitIdWarning.setText("");
						m_textJobIdWarning.setText("");
					}
				}
			}
		});

		// ジョブ 重要度：危険
		label = createPriorityLabelWidget(groupJob, Messages.getString("critical"),PriorityColorConstant.COLOR_CRITICAL);
		this.m_checkJobRunCritical = createJobRunCheckWidget(groupJob);
		this.m_textJobunitIdCritical = createJobunitIdTextWidget(groupJob);
		this.m_textJobIdCritical = createJobIdTextWidget(groupJob);
		this.m_buttonReferCritical = createJobReferButtonWidget(groupJob);
		this.m_comboFailurePriorityCritical = createPriorityComboWidget(groupJob);
		this.m_buttonReferCritical.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// ジョブツリーダイアログ表示
				JobTreeDialog dialog = new JobTreeDialog(m_shell, m_managerName, m_ownerRoleId, false);
				if (dialog.open() == IDialogConstants.OK_ID) {
					JobTreeItemWrapper selectItem = dialog.getSelectItem().get(0);
					if (selectItem.getData().getType() != JobInfoWrapper.TypeEnum.COMPOSITE) {
						m_textJobunitIdCritical.setText(selectItem.getData().getJobunitId());
						m_textJobIdCritical.setText(selectItem.getData().getId());
					} else {
						m_textJobunitIdCritical.setText("");
						m_textJobIdCritical.setText("");
					}
				}
			}
		});

		// ジョブ 重要度：不明
		label = createPriorityLabelWidget(groupJob, Messages.getString("unknown"),PriorityColorConstant.COLOR_UNKNOWN);
		this.m_checkJobRunUnknown = createJobRunCheckWidget(groupJob);
		this.m_textJobunitIdUnknown = createJobunitIdTextWidget(groupJob);
		this.m_textJobIdUnknown = createJobIdTextWidget(groupJob);
		this.m_buttonReferUnknown = createJobReferButtonWidget(groupJob);
		this.m_comboFailurePriorityUnknown = createPriorityComboWidget(groupJob);
		this.m_buttonReferUnknown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// ジョブツリーダイアログ表示
				JobTreeDialog dialog = new JobTreeDialog(m_shell, m_managerName, m_ownerRoleId, false);
				if (dialog.open() == IDialogConstants.OK_ID) {
					JobTreeItemWrapper selectItem = dialog.getSelectItem().get(0);
					if (selectItem.getData().getType() != JobInfoWrapper.TypeEnum.COMPOSITE) {
						m_textJobunitIdUnknown.setText(selectItem.getData().getJobunitId());
						m_textJobIdUnknown.setText(selectItem.getData().getId());
					} else {
						m_textJobunitIdUnknown.setText("");
						m_textJobIdUnknown.setText("");
					}
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
	 * 引数で指定されたジョブ通知情報の値を、各項目に設定します。
	 *
	 * @param notifyJobInfo 設定値として用いるジョブ通知情報
	 */
	public void setInputData(JobNotifyDetailInfoResponse notifyJobInfo) {

		// 初期値設定
		m_radioGenerationNodeValue.setSelection(true);
		m_radioFixedValue.setSelection(false);
		m_scopeSelect.setEnabled(false);

		if (notifyJobInfo != null) {
			if (notifyJobInfo.getJobExecFacilityId() != null) {
				this.m_facilityId = notifyJobInfo.getJobExecFacilityId();
				this.m_textScope.setText(HinemosMessage.replace(notifyJobInfo.getJobExecScope()));
			}
			if (notifyJobInfo.getJobExecFacilityFlg() != null && notifyJobInfo.getJobExecFacilityFlg() == JobExecFacilityFlgEnum.GENERATION) {
				this.m_radioGenerationNodeValue.setSelection(true);
				this.m_radioFixedValue.setSelection(false);
				this.m_scopeSelect.setEnabled(false);
			} 
			else {
				this.m_radioGenerationNodeValue.setSelection(false);
				this.m_radioFixedValue.setSelection(true);
				this.m_scopeSelect.setEnabled(true);
			}

			Boolean[] validFlgs = new Boolean[] {
					notifyJobInfo.getInfoValidFlg(),
					notifyJobInfo.getWarnValidFlg(),
					notifyJobInfo.getCriticalValidFlg(),
					notifyJobInfo.getUnknownValidFlg()
			};
			Button[] checkJobRuns = new Button[] {
					this.m_checkJobRunInfo,
					this.m_checkJobRunWarning,
					this.m_checkJobRunCritical,
					this.m_checkJobRunUnknown
			};
			String[] jobunitIds = new String[] {
					notifyJobInfo.getInfoJobunitId(),
					notifyJobInfo.getWarnJobunitId(),
					notifyJobInfo.getCriticalJobunitId(),
					notifyJobInfo.getUnknownJobunitId()
			};
			Text[] textJobunitIds = new Text[] {
					this.m_textJobunitIdInfo,
					this.m_textJobunitIdWarning,
					this.m_textJobunitIdCritical,
					this.m_textJobunitIdUnknown
			};
			String[] jobIds = new String[] {
					notifyJobInfo.getInfoJobId(),
					notifyJobInfo.getWarnJobId(),
					notifyJobInfo.getCriticalJobId(),
					notifyJobInfo.getUnknownJobId()
			};
			Text[] textJobIds = new Text[] {
					this.m_textJobIdInfo,
					this.m_textJobIdWarning,
					this.m_textJobIdCritical,
					this.m_textJobIdUnknown
			};
			String[] jobFailurePriorities = new String[] {
					PriorityMessage.enumToString(notifyJobInfo.getInfoJobFailurePriority(), InfoJobFailurePriorityEnum.class),
					PriorityMessage.enumToString(notifyJobInfo.getWarnJobFailurePriority(), WarnJobFailurePriorityEnum.class),
					PriorityMessage.enumToString(notifyJobInfo.getCriticalJobFailurePriority(), CriticalJobFailurePriorityEnum.class),
					PriorityMessage.enumToString(notifyJobInfo.getUnknownJobFailurePriority(), UnknownJobFailurePriorityEnum.class)
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
				//ジョブユニットID
				if (jobunitIds[i] != null){
					textJobunitIds[i].setText(jobunitIds[i]);
				}
				// ジョブID
				if (jobIds[i] != null) {
					textJobIds[i].setText(jobIds[i]);
				}
				// ジョブ失敗時の重要度
				if (jobFailurePriorities[i] != null) {
					comboFailurePriorities[i].setText(jobFailurePriorities[i]);
				}
			}
		}
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

		// 実行
		notifyJobInfo.setNotifyJobType(JobNotifyDetailInfoResponse.NotifyJobTypeEnum.DIRECT);
		notifyJobInfo.setInfoValidFlg(m_checkJobRunInfo.getSelection());
		notifyJobInfo.setWarnValidFlg(m_checkJobRunWarning.getSelection());
		notifyJobInfo.setCriticalValidFlg(m_checkJobRunCritical.getSelection());
		notifyJobInfo.setUnknownValidFlg(m_checkJobRunUnknown.getSelection());

		// jobunitId
		if (isNotNullAndBlank(m_textJobunitIdInfo.getText())) {
			notifyJobInfo.setInfoJobunitId(m_textJobunitIdInfo.getText());
		}
		if (isNotNullAndBlank(m_textJobunitIdWarning.getText())) {
			notifyJobInfo.setWarnJobunitId(m_textJobunitIdWarning.getText());
		}
		if (isNotNullAndBlank(m_textJobunitIdCritical.getText())) {
			notifyJobInfo.setCriticalJobunitId(m_textJobunitIdCritical.getText());
		}
		if (isNotNullAndBlank(m_textJobunitIdUnknown.getText())) {
			notifyJobInfo.setUnknownJobunitId(m_textJobunitIdUnknown.getText());
		}

		// jobId
		if (isNotNullAndBlank(m_textJobIdInfo.getText())) {
			notifyJobInfo.setInfoJobId(m_textJobIdInfo.getText());
		}
		if (isNotNullAndBlank(m_textJobIdWarning.getText())) {
			notifyJobInfo.setWarnJobId(m_textJobIdWarning.getText());
		}
		if (isNotNullAndBlank(m_textJobIdCritical.getText())) {
			notifyJobInfo.setCriticalJobId(m_textJobIdCritical.getText());
		}
		if (isNotNullAndBlank(m_textJobIdUnknown.getText())) {
			notifyJobInfo.setUnknownJobId(m_textJobIdUnknown.getText());
		}

		// 呼出失敗時
		if (isNotNullAndBlank(m_comboFailurePriorityInfo.getText())) {
			notifyJobInfo.setInfoJobFailurePriority(PriorityMessage.stringToEnum(m_comboFailurePriorityInfo.getText(), InfoJobFailurePriorityEnum.class));
		}
		if (isNotNullAndBlank(m_comboFailurePriorityWarning.getText())) {
			notifyJobInfo.setWarnJobFailurePriority(PriorityMessage.stringToEnum(m_comboFailurePriorityWarning.getText(), WarnJobFailurePriorityEnum.class));
		}
		if (isNotNullAndBlank(m_comboFailurePriorityCritical.getText())) {
			notifyJobInfo.setCriticalJobFailurePriority(PriorityMessage.stringToEnum(m_comboFailurePriorityCritical.getText(), CriticalJobFailurePriorityEnum.class));
		}
		if (isNotNullAndBlank(m_comboFailurePriorityUnknown.getText())) {
			notifyJobInfo.setUnknownJobFailurePriority(PriorityMessage.stringToEnum(m_comboFailurePriorityUnknown.getText(), UnknownJobFailurePriorityEnum.class));
		}

		// 共通部分登録
		// 実行ファシリティID
		if (isNotNullAndBlank(this.m_textScope.getText())) {
			notifyJobInfo.setJobExecFacilityId(this.m_facilityId);
			notifyJobInfo.setJobExecScope(this.m_textScope.getText());
		}
		// 実行ファシリティ
		if (this.m_radioGenerationNodeValue.getSelection()) {
			notifyJobInfo.setJobExecFacilityFlg(JobExecFacilityFlgEnum.GENERATION);
		}
		else if (this.m_radioFixedValue.getSelection()){
			notifyJobInfo.setJobExecFacilityFlg(JobExecFacilityFlgEnum.FIX);
		}

		info.setNotifyJobInfo(notifyJobInfo);

		return result;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_radioGenerationNodeValue.setEnabled(enabled);
		m_radioFixedValue.setEnabled(enabled);
		m_textScope.setEditable(false);
		m_scopeSelect.setEnabled(enabled && m_radioFixedValue.getSelection());
		m_checkJobRunInfo.setEnabled(enabled);
		m_checkJobRunWarning.setEnabled(enabled);
		m_checkJobRunCritical.setEnabled(enabled);
		m_checkJobRunUnknown.setEnabled(enabled);
		m_textJobunitIdInfo.setEditable(false);
		m_textJobunitIdWarning.setEditable(false);
		m_textJobunitIdCritical.setEditable(false);
		m_textJobunitIdUnknown.setEditable(false);
		m_textJobIdInfo.setEditable(false);
		m_textJobIdWarning.setEditable(false);
		m_textJobIdCritical.setEditable(false);
		m_textJobIdUnknown.setEditable(false);
		m_buttonReferInfo.setEnabled(enabled);
		m_buttonReferWarning.setEnabled(enabled);
		m_buttonReferCritical.setEnabled(enabled);
		m_buttonReferUnknown.setEnabled(enabled);
		m_comboFailurePriorityInfo.setEnabled(enabled);
		m_comboFailurePriorityWarning.setEnabled(enabled);
		m_comboFailurePriorityCritical.setEnabled(enabled);
		m_comboFailurePriorityUnknown.setEnabled(enabled);
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
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

	/**
	 * @param m_managerName the m_managerName to set
	 */
	public void setManagerName(String m_managerName) {
		this.m_managerName = m_managerName;
	}

	/**
	 * 重要度のラベルを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param text ラベルに表示するテキスト
	 * @param background ラベルの背景色
	 * @return 生成されたラベル
	 */
	private Label createPriorityLabelWidget(Composite parent,
			String text,
			Color background
			) {

		// ラベル（重要度）
		Label label = new Label(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
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
	private Button createJobRunCheckWidget(Composite parent) {

		// チェックボックス（実行）
		Button button = new Button(parent, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
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
	private Text createJobunitIdTextWidget(Composite parent) {
		// テキストボックス（ジョブユニットID）
		Text notifyJobCreateJobUnitIdText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
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
	 * ジョブIDテキストボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Text createJobIdTextWidget(Composite parent) {
		// テキストボックス（ジョブID）
		Text notifyJobCreateJobIdText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 4;
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
	private Button createJobReferButtonWidget(Composite parent) {
		// チェックボックス（参照）
		Button button = new Button(parent, SWT.NONE);
		GridData gridData = new GridData();
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
	private Combo createPriorityComboWidget(Composite parent) {
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		combo.setLayoutData(gridData);
		combo.add(PriorityMessage.STRING_CRITICAL);
		combo.add(PriorityMessage.STRING_WARNING);
		combo.add(PriorityMessage.STRING_INFO);
		combo.add(PriorityMessage.STRING_UNKNOWN);
		combo.setText(PriorityMessage.getNotifyJobFailurePriorityDefault());

		return combo;
	}

	private boolean isNotNullAndBlank(String str) {
		return str != null && !str.trim().isEmpty();
	}
}
