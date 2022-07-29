/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobEndStatusInfoResponse;
import org.openapitools.client.model.JobNextJobOrderInfoResponse;
import org.openapitools.client.model.JobObjectGroupInfoResponse;
import org.openapitools.client.model.JobObjectInfoResponse;
import org.openapitools.client.model.JobParameterInfoResponse;
import org.openapitools.client.model.JobWaitRuleInfoResponse;
import org.openapitools.client.model.JobmapIconImageInfoResponseP1;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PatternConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.fault.OtherUserGetLock;
import com.clustercontrol.jobmanagement.composite.ApprovalComposite;
import com.clustercontrol.jobmanagement.composite.CommandComposite;
import com.clustercontrol.jobmanagement.composite.ControlComposite;
import com.clustercontrol.jobmanagement.composite.ControlNodeComposite;
import com.clustercontrol.jobmanagement.composite.EndDelayComposite;
import com.clustercontrol.jobmanagement.composite.EndStatusComposite;
import com.clustercontrol.jobmanagement.composite.FileCheckComposite;
import com.clustercontrol.jobmanagement.composite.FileComposite;
import com.clustercontrol.jobmanagement.composite.FileOutputComposite;
import com.clustercontrol.jobmanagement.composite.JobLinkRcvComposite;
import com.clustercontrol.jobmanagement.composite.JobLinkSendComposite;
import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.composite.MonitorComposite;
import com.clustercontrol.jobmanagement.composite.NotificationsComposite;
import com.clustercontrol.jobmanagement.composite.ParameterComposite;
import com.clustercontrol.jobmanagement.composite.ReferComposite;
import com.clustercontrol.jobmanagement.composite.ResourceComposite;
import com.clustercontrol.jobmanagement.composite.RpaComposite;
import com.clustercontrol.jobmanagement.composite.StartDelayComposite;
import com.clustercontrol.jobmanagement.composite.WaitRuleComposite;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmanagement.util.JobWaitRuleUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ジョブ[ジョブの作成・変更]ダイアログクラスです。
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class JobDialog extends CommonDialog {
	// ログ
	private static Log m_log = LogFactory.getLog( JobDialog.class );

	/** ジョブID用テキスト */
	private Text m_jobIdText = null;
	/** ジョブ名用テキスト */
	private Text m_jobNameText = null;
	/** 説明テキスト */
	private Text m_jobAnnotationText = null;
	/** アイコンIDコンボボックス */
	private Combo m_iconIdCombo = null;
	/** ジョブ開始時に実行対象ノードを決定するチェックボックス */
	private Button m_expNodeRuntimeFlgCheckbox = null;
	/** 待ち条件タブ用コンポジット */
	private WaitRuleComposite m_startComposite = null;
	/** 制御タブ用コンポジット */
	private ControlComposite m_controlComposite = null;
	/** 終了状態タブ用コンポジット */
	private EndStatusComposite m_endComposite = null;
	/** コマンドタブ用コンポジット */
	private CommandComposite m_executeComposite = null;
	/** ファイル出力タブ用コンポジット */
	private FileOutputComposite m_fileOutputComposite = null;
	/** ファイル転送タブ用コンポジット */
	private FileComposite m_fileComposite = null;
	/** 通知先の指定タブ用コンポジット */
	private NotificationsComposite m_messageComposite = null;
	/** 開始遅延タブ用コンポジット */
	private StartDelayComposite m_startDelayComposite = null;
	/** 終了遅延タブ用コンポジット */
	private EndDelayComposite m_endDelayComposite = null;
	/** 制御(ノード)用コンポジット */
	private ControlNodeComposite m_controlNodeComposite = null;
	/** ジョブ変数タブ用コンポジット */
	private ParameterComposite m_parameterComposite = null;
	/** 参照タブ用コンポジット */
	private ReferComposite m_referComposite = null;
	/** 承認タブ用コンポジット */
	private ApprovalComposite m_approvalComposite = null;
	/** 監視タブ用コンポジット */
	private MonitorComposite m_monitorComposite = null;
	/** ファイルチェックタブ用コンポジット */
	private FileCheckComposite m_filecheckComposite = null;
	/** ジョブ連携受信タブ用コンポジット */
	private JobLinkSendComposite m_joblinkSendComposite = null;
	/** ジョブ連携待機タブ用コンポジット */
	private JobLinkRcvComposite m_joblinkRcvComposite = null;
	/** リソース制御タブ用コンポジット */
	private ResourceComposite m_resourceComposite = null;
	/** RPAシナリオタブ用コンポジット */
	private RpaComposite m_rpaComposite = null;

	/** ジョブツリーアイテム */
	private JobTreeItemWrapper m_jobTreeItem = null;
	/** タブフォルダー */
	private TabFolder m_tabFolder = null;
	/** シェル */
	private Shell m_shell = null;
	/** 読み取り専用フラグ */
	private boolean m_readOnly = false;
	/** ジョブ履歴呼出フラグ */
	private boolean m_isCallJobHistory = false;

	/** オーナーロールID用テキスト */
	private RoleIdListComposite m_ownerRoleId = null;

	/** マネージャ名 */
	private String m_managerName = null;

	private Button m_editButton;

	private JobTreeComposite m_jobTreeComposite = null;

	/** モジュール登録済フラグ*/
	private Button m_moduleRegisteredCondition = null;
	
	/**
	 * コンストラクタ
	 *
	 * @param parent 親シェル
	 * @param readOnly 読み取り専用フラグ true：変更不可、false：変更可
	 */
	public JobDialog(Shell parent, String managerName, boolean readOnly) {
		super(parent);
		this.m_managerName = managerName;
		m_readOnly = readOnly;
		this.m_jobTreeComposite = null;
	}

	public JobDialog(JobTreeComposite jobTreeComposite, Shell parent, String managerName, boolean readOnly) {
		this(parent, managerName, readOnly);
		this.m_jobTreeComposite = jobTreeComposite;
	}

	public JobDialog(Shell parent, String managerName, boolean readOnly, boolean isCallJobHistory) {
		this(parent, managerName, readOnly);
		this.m_isCallJobHistory = isCallJobHistory;
	}
	
	/**
	 * ダイアログエリアを生成します。
	 * <P>
	 * ジョブ種別により、表示するタブを切り替えます。
	 *
	 * @param parent 親コンポジット
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobInfo
	 * @see com.clustercontrol.bean.JobConstant
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_shell = this.getShell();

		Label label = null;

		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);

		JobInfoWrapper info = m_jobTreeItem.getData();
		if (info == null)
			throw new InternalError("info is null.");
		
		// 本メソッドで詳細情報を取得するため、setJobFull実行
		JobPropertyUtil.setJobFull(m_managerName, info);

		if (info.getType() ==  JobInfoWrapper.TypeEnum.JOBUNIT) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.jobunit"));
		} else if (info.getType() == JobInfoWrapper.TypeEnum.JOBNET) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.jobnet"));
		} else if (info.getType() == JobInfoWrapper.TypeEnum.JOB) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.job"));
		} else if (info.getType() == JobInfoWrapper.TypeEnum.FILEJOB) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.forward.file.job"));
		} else if (info.getType() == JobInfoWrapper.TypeEnum.REFERJOB || info.getType() == JobInfoWrapper.TypeEnum.REFERJOBNET){
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.refer.job"));
		} else if (info.getType() == JobInfoWrapper.TypeEnum.APPROVALJOB) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.approval.job"));
		} else if (info.getType() == JobInfoWrapper.TypeEnum.MONITORJOB) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.monitor.job"));
		} else if (info.getType() == JobInfoWrapper.TypeEnum.FILECHECKJOB) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.filecheck.job"));
		} else if (info.getType() == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.joblinksend.job"));
		} else if (info.getType() == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.joblinkrcv.job"));
		} else if (info.getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.resource.control.job"));
		} else if (info.getType() == JobInfoWrapper.TypeEnum.RPAJOB) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.rpa.job"));
		}
		
		boolean initFlag = true;
		if (info.getId() != null && info.getId().length() > 0) {
			initFlag = false;
		}

		// Composite
		Composite jobInfoComposite = new Composite(parent, SWT.NONE);
		GridLayout jobInfoGridLayout = new GridLayout(4, false);
		jobInfoComposite.setLayout(jobInfoGridLayout);

		// ジョブID（ラベル）
		label = new Label(jobInfoComposite, SWT.NONE);
		label.setText(Messages.getString("job.id") + " : ");
		label
			.setLayoutData(new GridData(120, SizeConstant.SIZE_LABEL_HEIGHT));
		
		// ジョブID（テキスト）
		this.m_jobIdText = new Text(jobInfoComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_jobIdText", m_jobIdText);
		this.m_jobIdText.setLayoutData(new GridData(200,
				SizeConstant.SIZE_TEXT_HEIGHT));
		
		if(m_isCallJobHistory){
			this.m_jobIdText.addVerifyListener(
					new StringVerifyListener(DataRangeConstant.VARCHAR_1024));
		}else{
			this.m_jobIdText.addVerifyListener(
					new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		}
		
		this.m_jobIdText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 編集（ボタン）
		this.m_editButton = new Button(jobInfoComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_editButton", m_editButton);
		m_editButton.setText(Messages.getString("edit"));
		m_editButton.setEnabled(false);
		this.m_editButton.setLayoutData(new GridData(40,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_editButton.getLayoutData()).horizontalSpan = 2;
		m_editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				super.widgetSelected(event);

				JobTreeItemWrapper jobunitItem = JobUtil.getTopJobUnitTreeItem(m_jobTreeItem);
				String jobunitId = jobunitItem.getData().getJobunitId();

				JobEditState JobEditState = JobEditStateUtil.getJobEditState( m_managerName );
				// 編集モードに入る
				Long updateTime = JobEditState.getJobunitUpdateTime(jobunitId);
				Integer result = null;
				try {
					result =JobUtil.getEditLock(m_managerName, jobunitId, updateTime, false);
				} catch (OtherUserGetLock e) {
					// 他のユーザがロックを取得している
					String message = e.getMessage() + "\n"
							+ HinemosMessage.replace(MessageConstant.MESSAGE_WANT_TO_GET_LOCK.getMessage());
					if (MessageDialog.openQuestion(
							null,
							Messages.getString("confirmed"),
							message)) {
						try {
							result = JobUtil.getEditLock(m_managerName, jobunitId, updateTime, true);
						} catch (Exception e1) {
							// ここには絶対にこないはず
							m_log.error("run() : logical error");
						}
					}
				}

				if (result != null) {
					// ロックを取得した
					m_log.debug("run() : get editLock(jobunitId="+jobunitId+")");
					JobEditState.addLockedJobunit(jobunitItem.getData(), JobTreeItemUtil.clone(jobunitItem, null), result);
					if (m_jobTreeComposite != null) {
						m_jobTreeComposite.refresh(jobunitItem.getParent());
					}

					//ダイアログの更新
					m_readOnly = false;
					updateWidgets();
				} else {
					// ロックの取得に失敗した
					m_log.debug("run() : cannot get editLock(jobunitId="+jobunitId+")");
				}
				// RPAシナリオジョブの場合、制御(ノード)タブの活性を制御する
				updateControlNodeComposite();
			}
		});

		// ジョブ名（ラベル）
		label = new Label(jobInfoComposite, SWT.NONE);
		label.setText(Messages.getString("job.name") + " : ");
		label.setLayoutData(new GridData(120,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// ジョブ名（テキスト）
		this.m_jobNameText = new Text(jobInfoComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_jobNameText", m_jobNameText);
		this.m_jobNameText.setLayoutData(new GridData(200,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_jobNameText.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		this.m_jobNameText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// モジュール登録済フラグ
		this.m_moduleRegisteredCondition = new Button(jobInfoComposite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_moduleRegistCondition", this.m_moduleRegisteredCondition);
		this.m_moduleRegisteredCondition.setText(Messages.getString("job.module.registration"));
		this.m_moduleRegisteredCondition.setLayoutData(new GridData(150,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_moduleRegisteredCondition.getLayoutData()).horizontalSpan = 2;
		this.m_moduleRegisteredCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		if (info.getType() == JobInfoWrapper.TypeEnum.JOBNET ||
				info.getType() == JobInfoWrapper.TypeEnum.APPROVALJOB ||
				info.getType() == JobInfoWrapper.TypeEnum.JOB ||
				info.getType() == JobInfoWrapper.TypeEnum.FILEJOB ||
				info.getType() == JobInfoWrapper.TypeEnum.MONITORJOB ||
				info.getType() == JobInfoWrapper.TypeEnum.FILECHECKJOB ||
				info.getType() == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB ||
				info.getType() == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB ||
				info.getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB) {
			m_moduleRegisteredCondition.setEnabled(!m_readOnly);
		} else {
			m_moduleRegisteredCondition.setEnabled(false);
		}

		// 説明（ラベル）
		label = new Label(jobInfoComposite, SWT.NONE);
		label.setText(Messages.getString("description") + " : ");
		label.setLayoutData(new GridData(120,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// 説明（テキスト）
		m_jobAnnotationText = new Text(jobInfoComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_jobAnnotationText", m_jobAnnotationText);
		m_jobAnnotationText.setLayoutData(new GridData(200,
				SizeConstant.SIZE_TEXT_HEIGHT));
		m_jobAnnotationText.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_256));
		// dummy
		new Label(jobInfoComposite, SWT.NONE);
		// dummy
		new Label(jobInfoComposite, SWT.NONE);

		// オーナーロールID（ラベル）
		label = new Label(jobInfoComposite, SWT.NONE);
		label.setText(Messages.getString("owner.role.id") + " : ");
		label.setLayoutData(new GridData(120,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// オーナーロールID（テキスト）
		// 新規登録、コピー時のみ変更可能
		// 新規登録、コピーの判定はJobInfo.createTimeで行う。
		if (info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT && info.getCreateTime() == null) {
			this.m_ownerRoleId = new RoleIdListComposite(jobInfoComposite,
					SWT.NONE, this.m_managerName, true, Mode.OWNER_ROLE);
			this.m_ownerRoleId.getComboRoleId().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_messageComposite.getNotifyId().setOwnerRoleId(m_ownerRoleId.getText(), false);
				}
			});
		} else {
			this.m_ownerRoleId = new RoleIdListComposite(jobInfoComposite,
					SWT.NONE, this.m_managerName, false, Mode.OWNER_ROLE);
		}
		GridData ownerRoleIdGridData = new GridData();
		ownerRoleIdGridData.widthHint = 207;
		this.m_ownerRoleId.setLayoutData(ownerRoleIdGridData);

		// dummy
		new Label(jobInfoComposite, SWT.NONE);
		// dummy
		new Label(jobInfoComposite, SWT.NONE);

		// ジョブ開始時に実行対象ノードを決定する
		m_expNodeRuntimeFlgCheckbox = new Button(jobInfoComposite, SWT.CHECK);
		GridData expNodeRuntimeFlgGridData = new GridData();
		expNodeRuntimeFlgGridData.heightHint = SizeConstant.SIZE_BUTTON_HEIGHT;
		expNodeRuntimeFlgGridData.horizontalSpan = 2;
		m_expNodeRuntimeFlgCheckbox.setLayoutData(expNodeRuntimeFlgGridData);
		m_expNodeRuntimeFlgCheckbox.setText(Messages.getString("job.expnoderuntime.description"));

		// dummy
		new Label(jobInfoComposite, SWT.NONE);
		// dummy
		new Label(jobInfoComposite, SWT.NONE);

		// アイコンID
		if (info.getType() != JobInfoWrapper.TypeEnum.JOBUNIT) {

			// アイコンID（ラベル）
			label = new Label(jobInfoComposite, SWT.NONE);
			label.setText(Messages.getString("icon.id") + " : ");
			label.setLayoutData(new GridData(70,
					SizeConstant.SIZE_LABEL_HEIGHT));
			
			// アイコンID（コンボボックス）
			m_iconIdCombo = new Combo(jobInfoComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
			WidgetTestUtil.setTestId(this, "m_iconIdCombo", m_iconIdCombo);
			m_iconIdCombo.setLayoutData(new GridData(120,
					SizeConstant.SIZE_COMBO_HEIGHT));
		} else {
			// dummy
			new Label(jobInfoComposite, SWT.NONE);
			// dummy
			new Label(jobInfoComposite, SWT.NONE);
		}

		// タブ
		m_tabFolder = new TabFolder(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_tabFolder);

		if (info.getType() == JobInfoWrapper.TypeEnum.JOBNET) {
			//待ち条件
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//制御
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlComposite", m_controlComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem2", tabItem2);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);

			//開始遅延
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_startDelayComposite", m_startDelayComposite);
			TabItem tabItem3 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem3", tabItem3);
			tabItem3.setText(Messages.getString("start.delay"));
			tabItem3.setControl(m_startDelayComposite);

			//終了遅延
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, false);
			WidgetTestUtil.setTestId(this, "m_endDelayComposite", m_endDelayComposite);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem4", tabItem4);
			tabItem4.setText(Messages.getString("end.delay"));
			tabItem4.setControl(m_endDelayComposite);
		}
		else if (info.getType() == JobInfoWrapper.TypeEnum.JOB) {
			//待ち条件
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//制御(ジョブ)
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlComposite", m_controlComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);

			//制御(ノード)
			m_controlNodeComposite = new ControlNodeComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlNodeComposite", m_controlNodeComposite);
			TabItem tabItem3 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem3", tabItem3);
			tabItem3.setText(Messages.getString("control.node"));
			tabItem3.setControl(m_controlNodeComposite);

			//コマンド
			m_executeComposite = new CommandComposite(m_tabFolder, SWT.NONE);
			m_executeComposite.setManagerName(m_managerName);
			WidgetTestUtil.setTestId(this, "m_executeComposite", m_executeComposite);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem4", tabItem4);
			tabItem4.setText(Messages.getString("command"));
			tabItem4.setControl(m_executeComposite);

			//ファイル出力
			m_fileOutputComposite = new FileOutputComposite(m_tabFolder, SWT.NONE);
			TabItem tabItem5 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem5.setText(Messages.getString("job.output.file"));
			tabItem5.setControl(m_fileOutputComposite);

			//開始遅延
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_startDelayComposite", m_startDelayComposite);
			TabItem tabItem6 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem6", tabItem6);
			tabItem6.setText(Messages.getString("start.delay"));
			tabItem6.setControl(m_startDelayComposite);

			//終了遅延
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, false);
			WidgetTestUtil.setTestId(this, "m_endDelayComposite", m_endDelayComposite);
			TabItem tabItem7 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem7", tabItem7);
			tabItem7.setText(Messages.getString("end.delay"));
			tabItem7.setControl(m_endDelayComposite);

		}
		else if (info.getType() == JobInfoWrapper.TypeEnum.FILEJOB) {
			//待ち条件
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//制御（ジョブ）
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlComposite", m_controlComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);
			
			//制御(ノード)
			m_controlNodeComposite = new ControlNodeComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlNodeComposite", m_controlNodeComposite);
			TabItem tabItem6 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem6.setText(Messages.getString("control.node"));
			tabItem6.setControl(m_controlNodeComposite);

			//ファイル転送
			m_fileComposite = new FileComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_fileComposite", m_fileComposite);
			TabItem tabItem3 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem3.setText(Messages.getString("forward.file"));
			tabItem3.setControl(m_fileComposite);

			//開始遅延
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_startDelayComposite", m_startDelayComposite);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem4.setText(Messages.getString("start.delay"));
			tabItem4.setControl(m_startDelayComposite);

			//終了遅延
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, true);
			WidgetTestUtil.setTestId(this, "m_endDelayComposite", m_endDelayComposite);
			TabItem tabItem5 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem5.setText(Messages.getString("end.delay"));
			tabItem5.setControl(m_endDelayComposite);
		}
		//参照ジョブ/参照ジョブネット
		else if(info.getType() == JobInfoWrapper.TypeEnum.REFERJOB || info.getType() == JobInfoWrapper.TypeEnum.REFERJOBNET){
			//待ち条件
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//参照
			m_referComposite = new ReferComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_referComposite", m_referComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabyitem2", tabItem2);
			tabItem2.setText(Messages.getString("refer"));
			tabItem2.setControl(m_referComposite);
		}
		else if (info.getType() == JobInfoWrapper.TypeEnum.APPROVALJOB) {
			//待ち条件
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//制御(ジョブ)
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlComposite", m_controlComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);

			//承認
			m_approvalComposite = new ApprovalComposite(m_tabFolder, SWT.NONE, m_managerName);
			WidgetTestUtil.setTestId(this, "m_approvalComposite", m_approvalComposite);
			TabItem tabItem3 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem3", tabItem3);
			tabItem3.setText(Messages.getString("approval"));
			tabItem3.setControl(m_approvalComposite);

			//開始遅延
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_startDelayComposite", m_startDelayComposite);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem4", tabItem4);
			tabItem4.setText(Messages.getString("start.delay"));
			tabItem4.setControl(m_startDelayComposite);

			//終了遅延
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, false);
			WidgetTestUtil.setTestId(this, "m_endDelayComposite", m_endDelayComposite);
			TabItem tabItem5 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem5", tabItem5);
			tabItem5.setText(Messages.getString("end.delay"));
			tabItem5.setControl(m_endDelayComposite);
			
		}
		else if (info.getType() == JobInfoWrapper.TypeEnum.MONITORJOB) {
			//待ち条件
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//制御(ジョブ)
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlComposite", m_controlComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);

			//監視
			m_monitorComposite = new MonitorComposite(m_tabFolder, SWT.NONE);
			m_monitorComposite.setManagerName(m_managerName);
			WidgetTestUtil.setTestId(this, "m_monitorComposite", m_monitorComposite);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem4", tabItem4);
			tabItem4.setText(Messages.getString("monitor"));
			tabItem4.setControl(m_monitorComposite);

			//開始遅延
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_startDelayComposite", m_startDelayComposite);
			TabItem tabItem5 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem5", tabItem5);
			tabItem5.setText(Messages.getString("start.delay"));
			tabItem5.setControl(m_startDelayComposite);

			//終了遅延
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, false);
			WidgetTestUtil.setTestId(this, "m_endDelayComposite", m_endDelayComposite);
			TabItem tabItem6 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem6", tabItem6);
			tabItem6.setText(Messages.getString("end.delay"));
			tabItem6.setControl(m_endDelayComposite);
		}
		else if (info.getType() == JobInfoWrapper.TypeEnum.FILECHECKJOB) {
			//待ち条件
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//制御(ジョブ)
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);

			//制御(ノード)
			m_controlNodeComposite = new ControlNodeComposite(m_tabFolder, SWT.NONE, info.getType());
			TabItem tabItem3 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem3.setText(Messages.getString("control.node"));
			tabItem3.setControl(m_controlNodeComposite);

			//ファイルチェック
			m_filecheckComposite = new FileCheckComposite(m_tabFolder, SWT.NONE);
			m_filecheckComposite.setManagerName(m_managerName);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem4.setText(Messages.getString("file.check"));
			tabItem4.setControl(m_filecheckComposite);

			//開始遅延
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			TabItem tabItem5 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem5.setText(Messages.getString("start.delay"));
			tabItem5.setControl(m_startDelayComposite);

			//終了遅延
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, false);
			TabItem tabItem6 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem6.setText(Messages.getString("end.delay"));
			tabItem6.setControl(m_endDelayComposite);

		}
		else if (info.getType() == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB) {
			//待ち条件
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//制御(ジョブ)
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);

			//ジョブ連携送信
			m_joblinkSendComposite = new JobLinkSendComposite(m_tabFolder, SWT.NONE);
			m_joblinkSendComposite.setManagerName(m_managerName);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem4.setText(Messages.getString("joblink.send"));
			tabItem4.setControl(m_joblinkSendComposite);

			//開始遅延
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			TabItem tabItem5 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem5.setText(Messages.getString("start.delay"));
			tabItem5.setControl(m_startDelayComposite);

			//終了遅延
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, false);
			TabItem tabItem6 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem6.setText(Messages.getString("end.delay"));
			tabItem6.setControl(m_endDelayComposite);

		}
		else if (info.getType() == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB) {
			//待ち条件
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//制御(ジョブ)
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);

			//ジョブ連携待機
			m_joblinkRcvComposite = new JobLinkRcvComposite(m_tabFolder, SWT.NONE);
			m_joblinkRcvComposite.setManagerName(m_managerName);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem4.setText(Messages.getString("joblink.rcv"));
			tabItem4.setControl(m_joblinkRcvComposite);

			//開始遅延
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			TabItem tabItem5 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem5.setText(Messages.getString("start.delay"));
			tabItem5.setControl(m_startDelayComposite);

			//終了遅延
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, false);
			TabItem tabItem6 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem6.setText(Messages.getString("end.delay"));
			tabItem6.setControl(m_endDelayComposite);

		}
		else if (info.getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB) {

			//待ち条件
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//制御(ジョブ)
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlComposite", m_controlComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);

			//リソース制御
			m_resourceComposite = new ResourceComposite(m_tabFolder, SWT.NONE, m_managerName);
			WidgetTestUtil.setTestId(this, "m_resourceComposite", m_resourceComposite);
			TabItem tabItem3 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem3", tabItem3);
			tabItem3.setText(Messages.getString("resource.control"));
			tabItem3.setControl(m_resourceComposite);

			//開始遅延
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_startDelayComposite", m_startDelayComposite);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem4", tabItem4);
			tabItem4.setText(Messages.getString("start.delay"));
			tabItem4.setControl(m_startDelayComposite);

			//終了遅延
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, false);
			WidgetTestUtil.setTestId(this, "m_endDelayComposite", m_endDelayComposite);
			TabItem tabItem5 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem5", tabItem5);
			tabItem5.setText(Messages.getString("end.delay"));
			tabItem5.setControl(m_endDelayComposite);
		}
		else if (info.getType() == JobInfoWrapper.TypeEnum.RPAJOB) {
			//待ち条件
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//制御(ジョブ)
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlComposite", m_controlComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);

			//制御(ノード)
			m_controlNodeComposite = new ControlNodeComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlNodeComposite", m_controlNodeComposite);
			TabItem tabItem3 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem3", tabItem3);
			tabItem3.setText(Messages.getString("control.node"));
			tabItem3.setControl(m_controlNodeComposite);

			//RPAシナリオ
			//初期化時に使用するためマネージャ名も渡す
			m_rpaComposite = new RpaComposite(m_tabFolder, SWT.NONE, this.m_managerName);
			WidgetTestUtil.setTestId(this, "m_rpaComposite", m_rpaComposite);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem4", tabItem4);
			tabItem4.setText(Messages.getString("rpa.scenario"));
			tabItem4.setControl(m_rpaComposite);

			//開始遅延
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_startDelayComposite", m_startDelayComposite);
			TabItem tabItem5 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem5", tabItem5);
			tabItem5.setText(Messages.getString("start.delay"));
			tabItem5.setControl(m_startDelayComposite);

			//終了遅延
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, false);
			WidgetTestUtil.setTestId(this, "m_endDelayComposite", m_endDelayComposite);
			TabItem tabItem6 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem6", tabItem6);
			tabItem6.setText(Messages.getString("end.delay"));
			tabItem6.setControl(m_endDelayComposite);
		}
		//参照ジョブ/参照ジョブネット以外では使用する
		if (info.getType() != JobInfoWrapper.TypeEnum.REFERJOB && info.getType() != JobInfoWrapper.TypeEnum.REFERJOBNET) {
			//終了状態
			m_endComposite = new EndStatusComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_endComposite", m_endComposite);
			TabItem tabItem8 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem8", tabItem8);
			tabItem8.setText(Messages.getString("end.status"));
			tabItem8.setControl(m_endComposite);

			//通知先の指定
			m_messageComposite = new NotificationsComposite(m_tabFolder, SWT.NONE, m_managerName);
			WidgetTestUtil.setTestId(this, "m_messageComposite", m_messageComposite);
			TabItem tabItem9 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem9", tabItem9);
			tabItem9.setText(Messages.getString("notifications"));
			tabItem9.setControl(m_messageComposite);
		}

		if (info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT ) {
			//ジョブパラメータ
			m_parameterComposite = new ParameterComposite(m_tabFolder, SWT.NONE, initFlag);
			WidgetTestUtil.setTestId(this, "m_parameterComposite", m_parameterComposite);
			TabItem tabItem10 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem9", tabItem10);
			tabItem10.setText(Messages.getString("job.parameter"));
			tabItem10.setControl(m_parameterComposite);
		}

		m_tabFolder.setSelection(0);
		m_tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// RPAシナリオジョブの場合、制御(ノード)タブの活性を制御する
				updateControlNodeComposite();
			}
		});
		// 画面中央に
		Display display = m_shell.getDisplay();
		m_shell.setLocation(
				(display.getBounds().width - m_shell.getSize().x) / 2, (display
						.getBounds().height - m_shell.getSize().y) / 2);
		m_shell.pack();
		m_shell.setSize(new Point(m_shell.getSize().x, 700));

		//ジョブ情報反映
		reflectJobInfo(info);

		updateWidgets();
	}

	private void updateWidgets() {
		JobInfoWrapper info = m_jobTreeItem.getData();
		if (m_jobTreeItem.getParent() == null) {
			// ジョブ履歴の場合はparentがnullになるので、編集モードにできないようにする
			m_editButton.setEnabled(false);
		} else {
			m_editButton.setEnabled(m_readOnly);
		}
		
		if (info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT && info.getCreateTime() != null) {
			// すでにマネージャに登録してあるジョブユニットはIDを変更できない
			m_jobIdText.setEditable(false);
		} else {
			// それ以外の場合は編集モードの有無で判定する
			m_jobIdText.setEditable(!m_readOnly);
		}

		if (info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
			// ジョブユニット以外は編集不可
			m_expNodeRuntimeFlgCheckbox.setEnabled(!m_readOnly);
		} else {
			m_expNodeRuntimeFlgCheckbox.setEnabled(false);
		}
		
		m_jobNameText.setEditable(!m_readOnly);
		
		if (info.getType() == JobInfoWrapper.TypeEnum.JOBNET ||
				info.getType() == JobInfoWrapper.TypeEnum.APPROVALJOB ||
				info.getType() == JobInfoWrapper.TypeEnum.JOB ||
				info.getType() == JobInfoWrapper.TypeEnum.FILEJOB ||
				info.getType() == JobInfoWrapper.TypeEnum.MONITORJOB ||
				info.getType() == JobInfoWrapper.TypeEnum.FILECHECKJOB ||
				info.getType() == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB ||
				info.getType() == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB ||
				info.getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB) {
			m_moduleRegisteredCondition.setEnabled(!m_readOnly);
		}
		m_jobAnnotationText.setEditable(!m_readOnly);
		if (info.getType() != JobInfoWrapper.TypeEnum.JOBUNIT) {
			m_iconIdCombo.setEnabled(!m_readOnly);
		}
		if (m_startComposite != null) {
			m_startComposite.setEnabled(!m_readOnly);
		}
		if (m_controlComposite != null) {
			m_controlComposite.setEnabled(!m_readOnly);
		}
		if (m_executeComposite != null) {
			m_executeComposite.setEnabled(!m_readOnly);
		}
		if (m_fileOutputComposite != null) {
			m_fileOutputComposite.setEnabled(!m_readOnly);
		}
		if (m_fileComposite != null) {
			m_fileComposite.setEnabled(!m_readOnly);
		}
		if (m_startDelayComposite != null) {
			m_startDelayComposite.setEnabled(!m_readOnly);
		}
		if (m_endDelayComposite != null) {
			m_endDelayComposite.setEnabled(!m_readOnly);
		}
		if (m_controlNodeComposite != null) {
			m_controlNodeComposite.setEnabled(!m_readOnly);
		}
		if (m_approvalComposite != null) {
			m_approvalComposite.setEnabled(!m_readOnly);
		}
		if (m_monitorComposite != null) {
			m_monitorComposite.setEnabled(!m_readOnly);
		}
		if (m_filecheckComposite != null) {
			m_filecheckComposite.setEnabled(!m_readOnly);
		}
		if (m_joblinkSendComposite != null) {
			m_joblinkSendComposite.setEnabled(!m_readOnly);
		}
		if (m_joblinkRcvComposite != null) {
			m_joblinkRcvComposite.setEnabled(!m_readOnly);
		}
		if (m_resourceComposite != null) {
			m_resourceComposite.setEnabled(!m_readOnly);
		}
		if (m_rpaComposite != null)
			m_rpaComposite.setEnabled(!m_readOnly);

		if (info.getType() != JobInfoWrapper.TypeEnum.REFERJOB && info.getType() != JobInfoWrapper.TypeEnum.REFERJOBNET) {
			m_endComposite.setEnabled(!m_readOnly);
			m_messageComposite.setEnabled(!m_readOnly);
		} else {
			if (m_referComposite != null) {
				m_referComposite.setEnabled(!m_readOnly);
			}
		}

		if (m_parameterComposite != null) {
			m_parameterComposite.setEnabled(!m_readOnly);
		}

	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示
		if("".equals(this.m_jobIdText.getText())){
			this.m_jobIdText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_jobIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_jobNameText.getText())){
			this.m_jobNameText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_jobNameText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 * @since 1.0.0
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 * @since 1.0.0
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * ジョブ情報をダイアログ及び各タブのコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobInfo
	 * @see com.clustercontrol.bean.JobConstant
	 */
	private void reflectJobInfo(JobInfoWrapper info) {
		if (info != null) {
			//ジョブID設定
			String jobId = info.getId();
			if (jobId != null) {
				m_jobIdText.setText(jobId);
			} else {
				m_jobIdText.setText("");
			}

			//ジョブ名設定
			if (info.getName() != null) {
				m_jobNameText.setText(info.getName());
			} else {
				m_jobNameText.setText("");
			}
			// モジュール登録済フラグ設定
			m_moduleRegisteredCondition.setSelection(info.getRegistered());
			
			//注釈設定
			if (info.getDescription() != null) {
				m_jobAnnotationText.setText(info.getDescription());
			} else {
				m_jobAnnotationText.setText("");
			}

			// オーナーロール取得
			if (info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
				if (info.getOwnerRoleId() != null) {
					this.m_ownerRoleId.setText(info.getOwnerRoleId());
				} else {
					this.m_ownerRoleId.setText(RoleIdConstant.ALL_USERS);
				}
			} else {
				JobTreeItemWrapper parentItem = m_jobTreeItem.getParent();
				if (parentItem != null) {
					// ジョブツリーより、ジョブユニットのオーナーロールIDを取得する
					//FullJob APIが呼ばれる前でも親JobTreeItemのJobInfoとOwnerRoleIdは取得できるためこの実装で問題ない
					while(parentItem.getData().getType() != JobInfoWrapper.TypeEnum.JOBUNIT) {
						parentItem = parentItem.getParent();
					}
					JobInfoWrapper parentInfo = parentItem.getData();
					this.m_ownerRoleId.setText(parentInfo.getOwnerRoleId());
				} else {
					// ツリーではなく単体のジョブ情報を渡された場合(ジョブ履歴、同時実行制御ジョブ一覧)は、
					// ジョブ情報が持っているオーナーロールIDを設定する。
					m_ownerRoleId.setText(info.getOwnerRoleId() == null ? "": info.getOwnerRoleId());
				}
			}

			// アイコンID
			if (info.getType() != JobInfoWrapper.TypeEnum.JOBUNIT) {
				setIconIdComboItem(this.m_iconIdCombo, 
						this.m_managerName, 
						this.m_ownerRoleId.getText());
				if (info.getIconId() != null) {
					this.m_iconIdCombo.setText(info.getIconId());
				}
			}

			// ジョブ開始時に実行対象ノードを決定する
			if (info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
				if (info.getExpNodeRuntimeFlg() != null) {
					this.m_expNodeRuntimeFlgCheckbox.setSelection(info.getExpNodeRuntimeFlg());
				} else {
					this.m_expNodeRuntimeFlgCheckbox.setSelection(false);
				}
			} else {
				JobTreeItemWrapper parentItem = m_jobTreeItem.getParent();
				if (parentItem != null) {
					// ジョブツリーより、ジョブユニットの情報を取得する
					while(parentItem.getData().getType() != JobInfoWrapper.TypeEnum.JOBUNIT) {
						parentItem = parentItem.getParent();
					}
					JobInfoWrapper parentInfo = parentItem.getData();
					this.m_expNodeRuntimeFlgCheckbox.setSelection(parentInfo.getExpNodeRuntimeFlg());
				} else {
					// ツリーではなく単体のジョブ情報を渡された場合(ジョブ履歴、同時実行制御ジョブ一覧)は、
					// ジョブ情報が持っている情報を設定する
					if (info.getExpNodeRuntimeFlg() != null) {
						this.m_expNodeRuntimeFlgCheckbox.setSelection(info.getExpNodeRuntimeFlg());
					}
				}
			}

			//参照タブ以外
			if( info.getType() != JobInfoWrapper.TypeEnum.REFERJOB && info.getType() != JobInfoWrapper.TypeEnum.REFERJOBNET ){
				this.m_messageComposite.getNotifyId().setOwnerRoleId(m_ownerRoleId.getText(), false);
			}

			JobWaitRuleInfoResponse jobWaitRuleInfo = info.getWaitRule();
			if (jobWaitRuleInfo == null) {
				jobWaitRuleInfo = JobTreeItemUtil.getNewJobWaitRuleInfo();
			}

			//タブ内のコンポジットにジョブ情報を反映
			if (info.getType() == JobInfoWrapper.TypeEnum.JOBNET) {
				//開始待ち条件
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//制御
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//開始遅延
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//終了遅延
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();
			}
			else if (info.getType() == JobInfoWrapper.TypeEnum.JOB) {
				//開始待ち条件
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//制御
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//実行内容
				m_executeComposite.setCommandInfo(info.getCommand());
				m_executeComposite.setOwnerRoleId(this.m_ownerRoleId.getText());
				m_executeComposite.reflectCommandInfo();

				//ファイル出力
				if (info.getCommand() != null) {
					m_fileOutputComposite.setOutputInfo(info.getCommand().getNormalJobOutputInfo());
					m_fileOutputComposite.setErrOutputInfo(info.getCommand().getErrorJobOutputInfo());
				}
				m_fileOutputComposite.reflectOutputInfo();

				//開始遅延
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//終了遅延
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();

				//制御(ノード）
				m_controlNodeComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlNodeComposite.setCommandInfo(info.getCommand());
				m_controlNodeComposite.reflectControlNodeInfo();
			}
			else if (info.getType() == JobInfoWrapper.TypeEnum.FILEJOB) {
				//開始待ち条件
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//制御
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//ファイル転送
				m_fileComposite.setFileInfo(info.getFile());
				m_fileComposite.setOwnerRoleId(this.m_ownerRoleId.getText());
				m_fileComposite.setManagerName(this.m_managerName);
				m_fileComposite.reflectFileInfo();

				//開始遅延
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//終了遅延
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();

				//制御(ノード)
				m_controlNodeComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlNodeComposite.setFileInfo(info.getFile());
				m_controlNodeComposite.reflectControlNodeInfo();
			}
			//参照ジョブ
			else if(info.getType() == JobInfoWrapper.TypeEnum.REFERJOB || info.getType() == JobInfoWrapper.TypeEnum.REFERJOBNET){
				//開始待ち条件
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();
				//参照ジョブ
				m_referComposite.setReferJobUnitId(info.getReferJobUnitId());
				m_referComposite.setReferJobId(info.getReferJobId());
				m_referComposite.setReferJobSelectType(info.getReferJobSelectType());
				m_referComposite.setReferJobType(info.getType());
				m_referComposite.setJobTreeItem(m_jobTreeItem);
				m_referComposite.setJobTreeComposite(m_jobTreeComposite);
				m_referComposite.reflectReferInfo();
			}
			//承認ジョブ
			else if (info.getType() == JobInfoWrapper.TypeEnum.APPROVALJOB) {
				//開始待ち条件
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//制御
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//承認
				m_approvalComposite.setApprovalReqRoleId(info.getApprovalReqRoleId());
				m_approvalComposite.setApprovalReqUserId(info.getApprovalReqUserId());
				m_approvalComposite.setApprovalReqSentence(info.getApprovalReqSentence());
				m_approvalComposite.setApprovalReqMailTitle(info.getApprovalReqMailTitle());
				m_approvalComposite.setApprovalReqMailBody(info.getApprovalReqMailBody());
				m_approvalComposite.setUseApprovalReqSentence(info.getIsUseApprovalReqSentence());
				m_approvalComposite.setJobTreeItem(m_jobTreeItem);
				m_approvalComposite.reflectApprovalInfo();

				//開始遅延
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//終了遅延
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();
			}
			else if (info.getType() == JobInfoWrapper.TypeEnum.MONITORJOB) {
				//開始待ち条件
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//制御
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//監視
				m_monitorComposite.setMonitorJobInfo(info.getMonitor());
				m_monitorComposite.setOwnerRoleId(this.m_ownerRoleId.getText());
				m_monitorComposite.reflectMonitorJobInfo();

				//開始遅延
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//終了遅延
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();
			}
			else if (info.getType() == JobInfoWrapper.TypeEnum.FILECHECKJOB) {
				//開始待ち条件
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//制御
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//ファイルチェック
				m_filecheckComposite.setFileCheckInfo(info.getJobFileCheck());
				m_filecheckComposite.setOwnerRoleId(this.m_ownerRoleId.getText());
				m_filecheckComposite.reflectFileCheckInfo();

				//開始遅延
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//終了遅延
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();

				//制御(ノード)
				m_controlNodeComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlNodeComposite.setFileCheckInfo(info.getJobFileCheck());
				m_controlNodeComposite.reflectControlNodeInfo();
			}
			else if (info.getType() == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB) {
				//開始待ち条件
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//制御
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//ジョブ連携送信
				m_joblinkSendComposite.setJobLinkSendInfo(info.getJobLinkSend());
				m_joblinkSendComposite.setOwnerRoleId(this.m_ownerRoleId.getText());
				m_joblinkSendComposite.reflectJobLinkSendInfo(m_isCallJobHistory);

				//開始遅延
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//終了遅延
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();
			}
			else if (info.getType() == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB) {
				//開始待ち条件
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//制御
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//ジョブ連携待機
				m_joblinkRcvComposite.setJobLinkRcvInfo(info.getJobLinkRcv());
				m_joblinkRcvComposite.setOwnerRoleId(this.m_ownerRoleId.getText());
				m_joblinkRcvComposite.reflectJobLinkRcvInfo();

				//開始遅延
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//終了遅延
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();
			}
			else if (info.getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB) {
				//開始待ち条件
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//制御
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//リソース制御
				m_resourceComposite.setResourceJobInfo(info.getResource());
				m_resourceComposite.setOwnerRoleId(this.m_ownerRoleId.getText());
				m_resourceComposite.reflectResourceJobInfo();

				//開始遅延
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//終了遅延
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();
			}
			else if (info.getType() == JobInfoWrapper.TypeEnum.RPAJOB) {
				//開始待ち条件
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//制御
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//RPAシナリオ
				m_rpaComposite.setRpaJobInfo(info.getRpa());
				m_rpaComposite.setOwnerRoleId(this.m_ownerRoleId.getText());
				m_rpaComposite.reflectRpaJobInfo();

				//開始遅延
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//終了遅延
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();

				//制御(ノード）
				m_controlNodeComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlNodeComposite.setRpaInfo(info.getRpa());
				m_controlNodeComposite.reflectControlNodeInfo();
			}
			//参照タブ以外で使用する
			if (info.getType() != JobInfoWrapper.TypeEnum.REFERJOB && info.getType() != JobInfoWrapper.TypeEnum.REFERJOBNET) {
				//終了状態の定義
				m_endComposite.setEndInfo(info.getEndStatus());
				m_endComposite.reflectEndInfo();

				//メッセージの指定
				m_messageComposite.setJobInfo(info);
				m_messageComposite.getNotifyId().setOwnerRoleId(this.m_ownerRoleId.getText(), false);
				m_messageComposite.reflectNotificationsInfo();
			}

			if (info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {

				//ジョブパラメータ
				m_parameterComposite.setParamInfo(info.getParam());
				m_parameterComposite.reflectParamInfo(m_isCallJobHistory);
			}
		}
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		// 読み取り専用の場合は処理を抜ける
		if (this.m_readOnly) {
			return result;
		}

		// 更新前のジョブID
		JobInfoWrapper info = m_jobTreeItem.getData();
		String oldJobId = info.getId();

		result = createJobInfo();
		if (result != null) {
			return result;
		}

		info = m_jobTreeItem.getData();
		if (info != null) {
			if (info.getType() == JobInfoWrapper.TypeEnum.JOBNET) {
				//開始待ち条件
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//制御
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//開始遅延
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//終了遅延
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
			} else if (info.getType() == JobInfoWrapper.TypeEnum.JOB) {
				//開始待ち条件
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//制御
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//実行内容
				result = m_executeComposite.createCommandInfo();
				if (result != null) {
					return result;
				}

				//ファイル出力
				result = m_fileOutputComposite.createOutputInfo();
				if (result != null) {
					return result;
				}

				//開始遅延
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//終了遅延
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//多重度
				result = m_controlNodeComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//リトライ情報
				result = m_controlNodeComposite.createCommandInfo();
				if (result != null) {
					return result;
				}
			} else if (info.getType() == JobInfoWrapper.TypeEnum.FILEJOB) {
				//開始待ち条件
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//制御
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//ファイル転送
				result = m_fileComposite.createFileInfo();
				if (result != null) {
					return result;
				}

				//開始遅延
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//終了遅延
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//多重度
				result = m_controlNodeComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
				
				//リトライ回数
				result = m_controlNodeComposite.createFileInfo();
				if (result != null) {
					return result;
				}
			}
			//参照ジョブ/参照ジョブネット
			else if(info.getType() == JobInfoWrapper.TypeEnum.REFERJOB || info.getType() == JobInfoWrapper.TypeEnum.REFERJOBNET){
				//開始待ち条件
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
				//参照ジョブ
				result = m_referComposite.createReferInfo();
				if(result != null){
					return result;
				}
			}
			//承認ジョブ
			else if (info.getType() == JobInfoWrapper.TypeEnum.APPROVALJOB) {
				//開始待ち条件
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
	
				//制御
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
	
				//承認
				result = m_approvalComposite.createApprovalInfo();
				if (result != null) {
					return result;
				}
	
				//開始遅延
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
	
				//終了遅延
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
			} else if (info.getType() == JobInfoWrapper.TypeEnum.MONITORJOB) {
				//開始待ち条件
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//制御
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//監視
				result = m_monitorComposite.createMonitorJobInfo();
				if (result != null) {
					return result;
				}

				//開始遅延
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//終了遅延
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
			} else if (info.getType() == JobInfoWrapper.TypeEnum.FILECHECKJOB) {
				//開始待ち条件
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//制御
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//ファイルチェック
				result = m_filecheckComposite.createFileCheckInfo();
				if (result != null) {
					return result;
				}

				//開始遅延
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//終了遅延
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//多重度
				result = m_controlNodeComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//リトライ情報
				result = m_controlNodeComposite.createFileCheckInfo();
				if (result != null) {
					return result;
				}
			} else if (info.getType() == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB) {
				//開始待ち条件
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//制御
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//ジョブ連携送信
				result = m_joblinkSendComposite.createJobLinkSendInfo();
				if (result != null) {
					return result;
				}

				//開始遅延
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//終了遅延
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
			} else if (info.getType() == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB) {
				//開始待ち条件
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//制御
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//ジョブ連携待機
				result = m_joblinkRcvComposite.createJobLinkRcvInfo();
				if (result != null) {
					return result;
				}

				//開始遅延
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//終了遅延
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
			} else if (info.getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB) {
				//開始待ち条件
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//制御
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//リソース制御
				result = m_resourceComposite.createResourceJobInfo();
				if (result != null) {
					return result;
				}

				//開始遅延
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//終了遅延
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
			} else if (info.getType() == JobInfoWrapper.TypeEnum.RPAJOB) {
				//開始待ち条件
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//制御
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//RPAシナリオ
				result = m_rpaComposite.createRpaJobInfo();
				if (result != null) {
					return result;
				}

				//開始遅延
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//終了遅延
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//多重度
				result = m_controlNodeComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//リトライ情報
				result = m_controlNodeComposite.createRpaInfo();
				if (result != null) {
					return result;
				}
			}
			//参照ジョブ/参照ジョブネット以外で使用する
			if(info.getType() != JobInfoWrapper.TypeEnum.REFERJOB && info.getType() != JobInfoWrapper.TypeEnum.REFERJOBNET){
				//終了状態の定義
				result = m_endComposite.createEndInfo();
				if (result != null) {
					return result;
				}

				//メッセージの指定
				result = m_messageComposite.createNotificationsInfo();
				if (result != null) {
					return result;
				}
			}

			if (info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
				//ジョブパラメータ
				result = m_parameterComposite.createParamInfo();
				if (result != null) {
					return result;
				}
			}

			if (m_startComposite != null) {
				info.setWaitRule(
						m_startComposite.getWaitRuleInfo());
			}
			if (m_controlComposite != null) {
				info.setWaitRule(
						m_controlComposite.getWaitRuleInfo());
			}
			if (m_executeComposite != null) {
				info.setCommand(
						m_executeComposite.getCommandInfo());
			}
			if (m_fileOutputComposite != null) {
				info.getCommand().setNormalJobOutputInfo(
						m_fileOutputComposite.getOutputInfo());
				info.getCommand().setErrorJobOutputInfo(
						m_fileOutputComposite.getErrOutputInfo());
			}
			if (m_monitorComposite != null) {
				info.setMonitor(
						m_monitorComposite.getMonitorJobInfo());
			}
			if (m_filecheckComposite != null) {
				info.setJobFileCheck(
						m_filecheckComposite.getFileCheckInfo());
			}
			if (m_rpaComposite != null) {
				info.setRpa(m_rpaComposite.getRpaJobInfo());
			}
			if (m_fileComposite != null) {
				info.setFile(
						m_fileComposite.getFileInfo());
			}
			if (m_joblinkSendComposite != null) {
				info.setJobLinkSend(
						m_joblinkSendComposite.getJobLinkSendInfo());
			}
			if (m_joblinkRcvComposite != null) {
				info.setJobLinkRcv(
						m_joblinkRcvComposite.getJobLinkRcvInfo());
			}
			if (m_resourceComposite != null) {
				info.setResource(
						m_resourceComposite.getResourceJobInfo());
			}
			if (m_endComposite != null) {
				List<JobEndStatusInfoResponse> jobEndStatusInfoList = info.getEndStatus();
				jobEndStatusInfoList.clear();
				if (m_endComposite.getEndInfo() != null) {
					jobEndStatusInfoList.addAll(m_endComposite.getEndInfo());
				}
			} if (m_startDelayComposite != null)
				info.setWaitRule(
						m_startDelayComposite.getWaitRuleInfo());
			if (m_endDelayComposite != null) {
				info.setWaitRule(
						m_endDelayComposite.getWaitRuleInfo());
			}
			if (m_controlNodeComposite != null) {
				info.setWaitRule(
						m_controlNodeComposite.getWaitRuleInfo());
				//リトライ情報のセット
				if (m_controlNodeComposite.getCommandInfo() != null) {
					info.getCommand().setMessageRetryEndFlg(m_controlNodeComposite.getCommandInfo().getMessageRetryEndFlg());
					info.getCommand().setMessageRetryEndValue(m_controlNodeComposite.getCommandInfo().getMessageRetryEndValue());
					info.getCommand().setMessageRetry(m_controlNodeComposite.getCommandInfo().getMessageRetry());
					info.getCommand().setCommandRetryFlg(m_controlNodeComposite.getCommandInfo().getCommandRetryFlg());
					info.getCommand().setCommandRetry(m_controlNodeComposite.getCommandInfo().getCommandRetry());
					info.getCommand().setCommandRetryEndStatus(m_controlNodeComposite.getCommandInfo().getCommandRetryEndStatus());
				}
				if (m_controlNodeComposite.getFileInfo() != null) {
					info.getFile().setMessageRetryEndFlg(m_controlNodeComposite.getFileInfo().getMessageRetryEndFlg());
					info.getFile().setMessageRetryEndValue(m_controlNodeComposite.getFileInfo().getMessageRetryEndValue());
					info.getFile().setMessageRetry(m_controlNodeComposite.getFileInfo().getMessageRetry());
				}
				if (m_controlNodeComposite.getJobFileCheckInfo() != null) {
					info.getJobFileCheck().setMessageRetryEndFlg(m_controlNodeComposite.getJobFileCheckInfo().getMessageRetryEndFlg());
					info.getJobFileCheck().setMessageRetryEndValue(m_controlNodeComposite.getJobFileCheckInfo().getMessageRetryEndValue());
					info.getJobFileCheck().setMessageRetry(m_controlNodeComposite.getJobFileCheckInfo().getMessageRetry());
				}
				if (m_controlNodeComposite.getRpaInfo() != null) {
					info.getRpa().setMessageRetryEndFlg(m_controlNodeComposite.getRpaInfo().getMessageRetryEndFlg());
					info.getRpa().setMessageRetryEndValue(m_controlNodeComposite.getRpaInfo().getMessageRetryEndValue());
					info.getRpa().setMessageRetry(m_controlNodeComposite.getRpaInfo().getMessageRetry());
					info.getRpa().setCommandRetryFlg(m_controlNodeComposite.getRpaInfo().getCommandRetryFlg());
					info.getRpa().setCommandRetry(m_controlNodeComposite.getRpaInfo().getCommandRetry());
					info.getRpa().setCommandRetryEndStatus(m_controlNodeComposite.getRpaInfo().getCommandRetryEndStatus());
				}
			}
			if (m_messageComposite != null){
				JobInfoWrapper messageJobInfo = m_messageComposite.getJobInfo();
				info.setBeginPriority(messageJobInfo.getBeginPriority());
				info.setNormalPriority(messageJobInfo.getNormalPriority());
				info.setWarnPriority(messageJobInfo.getWarnPriority());
				info.setAbnormalPriority(messageJobInfo.getAbnormalPriority());

				if (messageJobInfo.getNotifyRelationInfos() != null) {
					info.getNotifyRelationInfos().clear();
					info.getNotifyRelationInfos().addAll(messageJobInfo.getNotifyRelationInfos());
				}
			}

			if (m_parameterComposite != null){
				List<JobParameterInfoResponse> jobParameterInfoinfoList = info.getParam();
				jobParameterInfoinfoList.clear();
				if (m_parameterComposite.getParamInfo() != null) {
					jobParameterInfoinfoList.addAll(m_parameterComposite.getParamInfo());
				}
			}

			//参照ジョブ
			if(m_referComposite != null){
				if(m_referComposite.getReferJobUnitId() != null){
					info.setReferJobUnitId(m_referComposite.getReferJobUnitId());
				}
				if(m_referComposite.getReferJobId() != null){
					info.setReferJobId(m_referComposite.getReferJobId());
				}
				if(m_referComposite.getReferJobSelectType() != null){
					info.setReferJobSelectType(m_referComposite.getReferJobSelectType());
				}
				info.setType(m_referComposite.getReferJobType());
			}
			
			//承認ジョブ
			if(m_approvalComposite != null){
				if(m_approvalComposite.getApprovalReqRoleId() != null){
					info.setApprovalReqRoleId(m_approvalComposite.getApprovalReqRoleId());
				}
				if(m_approvalComposite.getApprovalReqUserId() != null){
					info.setApprovalReqUserId(m_approvalComposite.getApprovalReqUserId());
				}
				if(m_approvalComposite.getApprovalReqSentence() != null){
					info.setApprovalReqSentence(m_approvalComposite.getApprovalReqSentence());
				}
				if(m_approvalComposite.getApprovalReqMailTitle() != null){
					info.setApprovalReqMailTitle(m_approvalComposite.getApprovalReqMailTitle());
				}
				if(m_approvalComposite.getApprovalReqMailBody() != null){
					info.setApprovalReqMailBody(m_approvalComposite.getApprovalReqMailBody());
				}
				info.setIsUseApprovalReqSentence(m_approvalComposite.isUseApprovalReqSentence());
			}

			//このジョブを参照するほかのジョブの待ち条件を更新
			//但しセッション横断待ち条件のジョブIDは更新しない(バリデーションで気づくのでユーザに更新してもらう)
			//※同一ジョブユニット内のジョブ全ての待ち条件をチェックする必要が生じるため
			if (!oldJobId.equals(info.getId()) && info.getType() != JobInfoWrapper.TypeEnum.JOBUNIT) {
				List<JobTreeItemWrapper> siblings = m_jobTreeItem.getParent().getChildren();
				for (JobTreeItemWrapper sibling : siblings) {
					if (sibling == m_jobTreeItem) {
						continue;
					}

					JobInfoWrapper siblingJobInfo = sibling.getData();
					if (siblingJobInfo.getWaitRule() == null) {
						continue;
					}

					for (JobObjectGroupInfoResponse siblingWaitJobObjectGroupInfo : siblingJobInfo.getWaitRule().getObjectGroup()) {
						if (siblingWaitJobObjectGroupInfo.getJobObjectList() == null) {
							continue;
						}
						for (JobObjectInfoResponse objectInfo : siblingWaitJobObjectGroupInfo.getJobObjectList()) {
							if (oldJobId.equals(objectInfo.getJobId())) {
								objectInfo.setJobId(info.getId());
								siblingJobInfo.setWaitRuleChanged(true);
							}
						}
					}
				}
			}

			//この参照ジョブのジョブIDを更新
			if (!oldJobId.equals(info.getId()) && info.getType() != JobInfoWrapper.TypeEnum.JOBUNIT) {
				//所属するjobunitを探す
				JobTreeItemWrapper treeItem = m_jobTreeItem;
				while (treeItem.getData().getType() != JobInfoWrapper.TypeEnum.JOBUNIT) {
					treeItem = treeItem.getParent();
				}
				
				//すべての参照ジョブに対して、ループさせる
				updateReferJob(treeItem, oldJobId, info.getId());
			}
			
			// このジョブを参照する全ての後続ジョブ実行設定のジョブIDを更新
			if (!oldJobId.equals(info.getId())) {
				JobTreeItemWrapper treeItem = m_jobTreeItem;
				if (info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
					// ジョブユニットのジョブID変更の場合は配下のジョブ全ての設定を更新
					updateIdNextJobOrderInfo(treeItem.getChildren(), oldJobId, info.getId(), true);
				} else {
				// ジョブユニット以外は同じ階層のジョブの設定を更新
					updateIdNextJobOrderInfo(treeItem.getParent().getChildren(), oldJobId, info.getId(), false);
				}
			}

			// 後続ジョブ実行設定の更新
			if (info.getType() != JobInfoWrapper.TypeEnum.JOBUNIT
					&& m_jobTreeItem.getParent() != null) {
				//所属するjobunitを探す
				JobTreeItemWrapper treeItem = m_jobTreeItem;
				while (treeItem.getData().getType() != JobInfoWrapper.TypeEnum.JOBUNIT) {
					treeItem = treeItem.getParent();
				}
				
				//すべてのジョブに対して、ループさせる
				updateNextJobOrderInfo(treeItem.getChildren());
			}

			info.setPropertyFull(true);
		}

		return null;
	}

	/**
	 * ダイアログの情報から、ジョブ情報を作成します。
	 *
	 * @return 入力値の検証結果
	 */
	private ValidateResult createJobInfo() {
		ValidateResult result = null;

		JobInfoWrapper info = m_jobTreeItem.getData();
		String oldJobunitId;

		// ジョブユニットIDの重複チェック(ジョブを編集しているときだけチェックする）
		if (!m_readOnly && info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
			// ジョブユニットIDの重複チェック
			oldJobunitId = info.getJobunitId();
			info.setJobunitId(m_jobIdText.getText());
			try {
				JobUtil.findDuplicateJobunitId(m_jobTreeItem.getParent().getParent());
			} catch (JobInvalid e) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				String[] args1 = { m_jobIdText.getText() };
				result.setMessage(Messages.getString("message.job.64", args1));
				return result;
			} finally {
				info.setJobunitId(oldJobunitId);
			}
			// ジョブユニットIDの文字制約のチェック
			if(!m_jobIdText.getText().matches(PatternConstant.HINEMOS_ID_PATTERN)){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				String[] args1 = { m_jobIdText.getText(), Messages.getString("job.id")};
				result.setMessage(Messages.getString("message.common.6", args1));

				info.setJobunitId(oldJobunitId);
				return result;
			}

			JobEditState JobEditState = JobEditStateUtil.getJobEditState( m_managerName );
			if( JobEditState.getEditSession(m_jobTreeItem.getData()) == null ){
				// 新規ジョブユニット作成の場合
				Integer editSession = null;

				try {
					editSession =JobUtil.getEditLock(m_managerName, m_jobIdText.getText(), null, false);
				} catch (OtherUserGetLock e) {
					// 他のユーザがロックを取得している
					String message = e.getMessage() + "\n"
							+ HinemosMessage.replace(MessageConstant.MESSAGE_WANT_TO_GET_LOCK.getMessage());
					if (MessageDialog.openQuestion(
							null,
							Messages.getString("confirmed"),
							message)) {
						try {
							editSession = JobUtil.getEditLock(m_managerName, m_jobIdText.getText(), null, true);
						} catch (Exception e1) {
							// ここには絶対にこないはず
							m_log.error("run() : logical error");
						}
					}
				}
				if (editSession == null) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					String[] args1 = { m_jobIdText.getText() };
					result.setMessage(Messages.getString("message.job.105", args1));
					return result;
				}
				JobEditState.addLockedJobunit(info, null, editSession);
			} else if (!m_jobIdText.getText().equals(oldJobunitId)) {
				// ジョブユニットID変更の場合
				Integer oldEditSession = JobEditState.getEditSession(info);
				Integer editSession = null;
				try {
					editSession =JobUtil.getEditLock(m_managerName, m_jobIdText.getText(), null, false);
				} catch (OtherUserGetLock e) {
					// 他のユーザがロックを取得している
					String message = e.getMessage() + "\n"
							+ HinemosMessage.replace(MessageConstant.MESSAGE_WANT_TO_GET_LOCK.getMessage());
					if (MessageDialog.openQuestion(
							null,
							Messages.getString("confirmed"),
							message)) {
						try {
							editSession = JobUtil.getEditLock(m_managerName, m_jobIdText.getText(), null, true);
						} catch (Exception e1) {
							// ここには絶対にこないはず
							m_log.error("run() : logical error");
						}
					}
				}
				if (editSession == null) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					String[] args1 = { m_jobIdText.getText() };
					result.setMessage(Messages.getString("message.job.105", args1));
					return result;
				}
				JobEditState.addLockedJobunit(info, null, editSession);
				try {
					JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(m_managerName);
					wrapper.releaseEditLock(info.getJobunitId(),oldEditSession);
				} catch (Exception e) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					String[] args1 = { m_jobIdText.getText() };
					result.setMessage(Messages.getString("message.job.105", args1));
					return result;
				}
			}
		}

		//ジョブID取得
		if (m_jobIdText.getText().length() > 0) {
			String oldId = info.getId();
			info.setId("");
			//ジョブIDの重複チェック（所属するジョブユニット配下のみ）
			JobTreeItemWrapper unit = JobUtil.getTopJobUnitTreeItem(m_jobTreeItem);
			if(unit != null && JobUtil.findJobId(m_jobIdText.getText(), unit)){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				String[] args1 = { m_jobIdText.getText() };
				result.setMessage(Messages.getString("message.job.42", args1));

				info.setId(oldId);
				return result;
			}
			// ジョブIDの文字制約のチェック
			if(!m_jobIdText.getText().matches(PatternConstant.HINEMOS_ID_PATTERN)){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				String[] args1 = { m_jobIdText.getText(), Messages.getString("job.id")};
				result.setMessage(Messages.getString("message.common.6", args1));

				info.setId(oldId);
				return result;
			}
			info.setId(m_jobIdText.getText());

			// ジョブユニットの場合はジョブユニットIDをセットする。
			if (info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
				info.setJobunitId(m_jobIdText.getText());
			}


		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.22"));
			return result;
		}

		//ジョブ名取得
		if (m_jobNameText.getText().length() > 0) {
			info.setName(m_jobNameText.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.23"));
			return result;
		}

		//モジュール登録済フラグ取得
		info.setRegistered(m_moduleRegisteredCondition.getSelection());

		//注釈取得
		if (m_jobAnnotationText.getText().length() > 0) {
			info.setDescription(m_jobAnnotationText.getText());
		} else {
			info.setDescription("");
		}

		// アイコンID
		if (info.getType() != JobInfoWrapper.TypeEnum.JOBUNIT) {
			if (this.m_iconIdCombo.getText() == null) {
				info.setIconId("");
			} else {
				info.setIconId(this.m_iconIdCombo.getText());
			}
		}

		//オーナーロールID取得
		//ジョブユニットのみJobInfoにOwnerRoleIdを設定する
		//配下のジョブにはマネージャ側の登録処理でOwnerRoleIdが設定される
		if (info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
			String newOwnerRoleId = m_ownerRoleId.getText();
			if (newOwnerRoleId.length() > 0) {
				if (!newOwnerRoleId.equals(info.getOwnerRoleId())) {
					info.setOwnerRoleId(newOwnerRoleId);
				}
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("owner.role.id"));
				return result;
			}
		} else {
			info.setOwnerRoleId(null);
		}

		// ジョブ開始時に実行対象ノードを決定する
		if (info.getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
			info.setExpNodeRuntimeFlg(m_expNodeRuntimeFlgCheckbox.getSelection());
		} else {
			info.setExpNodeRuntimeFlg(null);
		}
		
		
		return null;
	}

	private void updateReferJob(JobTreeItemWrapper treeItem, String oldJobId, String newJobId) {
		JobInfoWrapper info = treeItem.getData();
		if (info.getType() == JobInfoWrapper.TypeEnum.REFERJOB || info.getType() == JobInfoWrapper.TypeEnum.REFERJOBNET) {
			if (oldJobId.equals(info.getReferJobId())) {
				info.setReferJobId(newJobId);
				info.setReferJobChanged(true);
			}
		}
		
		for (JobTreeItemWrapper childTreeItem : treeItem.getChildren()) {
			updateReferJob(childTreeItem, oldJobId, newJobId);
		}
	}

	/**
	 * ジョブIDが変更された際、対応する後続ジョブ実行設定のジョブIDを更新します
	 * @param treeItemList
	 * @param oldJobId
	 * @param newJobId
	 * @param recursiveFlag 再帰処理フラグ
	 */
	private void updateIdNextJobOrderInfo(List<JobTreeItemWrapper> treeItemList, String oldJobId, String newJobId, boolean recursiveFlag) {
		if (treeItemList == null || treeItemList.size() == 0) {
			return;
		}
		for (JobTreeItemWrapper treeItem : treeItemList) {
			JobInfoWrapper jobInfo = treeItem.getData();

			if (jobInfo.getWaitRule() != null && jobInfo.getWaitRule().getExclusiveBranch().booleanValue()) {
				// 古いジョブIDを更新する
				for (JobNextJobOrderInfoResponse jobNextJobOrderInfo : jobInfo.getWaitRule().getExclusiveBranchNextJobOrderList()) {
					if (oldJobId.equals(jobNextJobOrderInfo.getJobunitId())) {
						jobNextJobOrderInfo.setJobunitId(newJobId);
						jobInfo.setWaitRuleChanged(true);
					}
					if (oldJobId.equals(jobNextJobOrderInfo.getJobId())) {
						jobNextJobOrderInfo.setJobId(newJobId);
						jobInfo.setWaitRuleChanged(true);
					}
					if (oldJobId.equals(jobNextJobOrderInfo.getNextJobId())) {
						jobNextJobOrderInfo.setNextJobId(newJobId);
						jobInfo.setWaitRuleChanged(true);
					}
				}
			}
			// 再帰処理
			if (recursiveFlag && treeItem.getChildren() != null && treeItem.getChildren().size() > 0) {
				updateIdNextJobOrderInfo(treeItem.getChildren(), oldJobId, newJobId, true);
			}
		}
	}


	/**
	 * ジョブの待ち条件等の変更に従い、他ジョブの後続ジョブ実行設定を更新します
	 * @param treeItemList
	 */
	private void updateNextJobOrderInfo(List<JobTreeItemWrapper> treeItemList) {
		if (treeItemList == null || treeItemList.size() == 0) {
			return;
		}
		for (JobTreeItemWrapper treeItem : treeItemList) {
			JobInfoWrapper jobInfo = treeItem.getData();
			if (jobInfo.getWaitRule() != null && jobInfo.getWaitRule().getExclusiveBranch().booleanValue()) {
				JobWaitRuleUtil.updateNextJobOrderInfo(treeItem, null);
			}
			// 再帰処理
			if (treeItem.getChildren() != null && treeItem.getChildren().size() > 0) {
				updateNextJobOrderInfo(treeItem.getChildren());
			}
		}
	}


	/**
	 * ジョブツリーアイテムを返します。
	 *
	 * @return ジョブツリーアイテム
	 */
	public JobTreeItemWrapper getJobTreeItem() {
		return m_jobTreeItem;
	}

	/**
	 * ジョブツリーアイテムを設定します。
	 *
	 * @param jobTreeItem ジョブツリーアイテム
	 */
	public void setJobTreeItem(JobTreeItemWrapper jobTreeItem) {
		this.m_jobTreeItem = jobTreeItem;
	}

	/**
	 * アイコンIDのコンボボックスの項目を設定します。
	 *
	 * @param iconIdCombo アイコンID用コンボボックス
	 * @param managerName マネージャ名
	 * @param ownerRoleId オーダーロールID
	 */
	private void setIconIdComboItem(Combo iconIdCombo, String managerName, String ownerRoleId){

		// 初期化
		iconIdCombo.removeAll();
		// デフォルト用
		iconIdCombo.add("");

		List<String> iconIdList = null;
		// データ取得
		try {
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
				JobmapIconImageInfoResponseP1 infoRes = wrapper.getJobmapIconImageIdListForSelect(ownerRoleId);
				iconIdList = infoRes.getIconIdList();
			}
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		if(iconIdList != null){
			for(String iconId : iconIdList){
				iconIdCombo.add(iconId);
			}
		}
	}

	private void updateControlNodeComposite(){
		// 間接実行の場合はノードを指定・エージェント指定しないため非活性
		if(m_rpaComposite != null && !m_readOnly){
			if(m_rpaComposite.isDirectExecButtonSelected()){
				m_controlNodeComposite.setEnabled(true);
			} else{
				m_controlNodeComposite.setEnabled(false);
			}
		}
	}
}
