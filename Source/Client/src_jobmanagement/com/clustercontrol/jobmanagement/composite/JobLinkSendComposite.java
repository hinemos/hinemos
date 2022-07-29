/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobLinkExpInfoResponse;
import org.openapitools.client.model.JobLinkSendInfoResponse;
import org.openapitools.client.model.JobLinkSendSettingResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.action.GetJobLinkExpTableDefine;
import com.clustercontrol.jobmanagement.bean.JobLinkConstant;
import com.clustercontrol.jobmanagement.dialog.JobLinkExpAddDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.view.action.ModifyJobLinkSendSettingAction;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ジョブ連携送信タブ用のコンポジットクラスです。
 *
 */
public class JobLinkSendComposite extends Composite {

	/** ジョブ連携送信設定ID用コンボボックス */
	private Combo m_joblinkSendSettingIdCombo = null;
	/** ジョブ連携送信設定参照用ボタン */
	private Button m_joblinkSendSettingReferBtn = null;

	/** 送信に失敗した場合再送する(チェックボックス) */
	private Button m_retry = null;

	/** 再送回数(テキスト) */
	private Text m_retryCount;

	/** 終了値（送信成功）(テキスト) */
	private Text m_successEndValue;

	/** 送信失敗時の操作(コンボボックス) */
	private Combo m_failureOperation;
	/** 標準出力 出力失敗時 終了状態(コンボボックス) */
	private Combo m_failureEndStatus = null;
	/** 標準出力 出力失敗時 終了値(テキスト) */
	private Text m_failureEndValue = null;

	/** ジョブ連携メッセージID(テキスト) */
	private Text m_joblinkMessageId;
	/** 重要度(コンボボックス) */
	private Combo m_priority;
	/** メッセージ(テキスト) */
	private Text m_message;

	/** 拡張情報 */
	private CommonTableViewer m_expTableViewer = null;
	/** 拡張情報：追加(ボタン) */
	private Button m_expCreateButton = null;
	/** 拡張情報：削除(ボタン) */
	private Button m_expDeleteButton = null;

	/** シェル */
	private Shell m_shell = null;
	/** オーナーロールID */
	private String m_ownerRoleId = null;
	/** マネージャ名 */
	private String m_managerName = null;

	/** ジョブ連携送信情報 */
	private JobLinkSendInfoResponse m_info;

	/** 選択アイテム(拡張情報) */
	private ArrayList<Object> m_expSelectItem = null;

	/** ジョブ履歴からの遷移か否か */
	private boolean m_callJobHistory = false;

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
	public JobLinkSendComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
		m_shell = this.getShell();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		// 変数として利用されるラベル
		Label label = null;

		this.setLayout(JobDialogUtil.getParentLayout());

		// ジョブ連携送信（グループ）
		Group joblinkSendGroup = new Group(this, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		joblinkSendGroup.setLayout(layout);
		joblinkSendGroup.setText(Messages.getString("scope"));

		// ジョブ連携送信設定ID(ラベル)
		label = new Label(joblinkSendGroup, SWT.NONE);
		label.setText(Messages.getString("joblink.send.setting.id") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// ジョブ連携送信設定ID(コンボボックス)
		this.m_joblinkSendSettingIdCombo = new Combo(joblinkSendGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.m_joblinkSendSettingIdCombo.setLayoutData(new GridData(250,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_joblinkSendSettingIdCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// ジョブ連携送信設定ID：参照（ボタン）
		this.m_joblinkSendSettingReferBtn = new Button(joblinkSendGroup, SWT.NONE);
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
				if (m_callJobHistory) {
					// ジョブ履歴からの遷移
					action.dialogOpen(m_shell, m_managerName, m_info); 
				} else {
					if (action.dialogOpen(m_shell, m_managerName, m_joblinkSendSettingIdCombo.getText()) 
							== IDialogConstants.OK_ID) {
						update();
					}
				}
			}
		});

		// 送信に失敗した場合再送する
		Composite retryComposite = new Composite(this, SWT.NONE);
		retryComposite.setLayout(new GridLayout(4, false));

		// 送信に失敗した場合再送する（チェックボックス）
		this.m_retry = new Button(retryComposite, SWT.CHECK);
		this.m_retry.setText(Messages.getString("joblink.send.failure.retry"));
		this.m_retry.setLayoutData(new GridData(300, SizeConstant.SIZE_BUTTON_HEIGHT));
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

		// 再送回数（ラベル）
		label = new Label(retryComposite, SWT.NONE);
		label.setText(Messages.getString("resend.count") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)label.getLayoutData()).horizontalAlignment = SWT.END;

		// 再送回数（テキスト）
		this.m_retryCount = new Text(retryComposite, SWT.BORDER);
		this.m_retryCount.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_retryCount.addVerifyListener(
				new NumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));
		this.m_retryCount.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ジョブ連携メッセージ情報（グループ）
		Group messageGroup = new Group(this, SWT.NONE);
		messageGroup.setText(Messages.getString("joblink.message.info"));
		messageGroup.setLayout(new GridLayout(4, false));

		// ジョブ連携メッセージID(ラベル)
		label = new Label(messageGroup, SWT.NONE);
		label.setText(Messages.getString("joblink.message.id") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// ジョブ連携メッセージID Prefix(ラベル)
		label = new Label(messageGroup, SWT.NONE);
		label.setText(HinemosModuleConstant.JOB + "_");
		label.setLayoutData(new GridData(30, SizeConstant.SIZE_TEXT_HEIGHT));

		// ジョブ連携メッセージID
		this.m_joblinkMessageId = new Text(messageGroup, SWT.BORDER);
		this.m_joblinkMessageId.setLayoutData(new GridData(260, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_joblinkMessageId.getLayoutData()).horizontalSpan = 2;
		this.m_joblinkMessageId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 重要度(ラベル)
		label = new Label(messageGroup, SWT.NONE);
		label.setText(Messages.getString("priority") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// 重要度
		this.m_priority = new Combo(messageGroup, SWT.CENTER | SWT.READ_ONLY);
		this.m_priority.setLayoutData(new GridData(120, SizeConstant.SIZE_COMBO_HEIGHT));
		((GridData)this.m_priority.getLayoutData()).horizontalSpan = 3;
		this.m_priority.add(PriorityMessage.STRING_INFO);
		this.m_priority.add(PriorityMessage.STRING_WARNING);
		this.m_priority.add(PriorityMessage.STRING_CRITICAL);
		this.m_priority.add(PriorityMessage.STRING_UNKNOWN);

		// メッセージ(ラベル)
		label = new Label(messageGroup, SWT.NONE);
		label.setText(Messages.getString("message") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// メッセージ(テキスト)
		this.m_message = new Text(messageGroup, SWT.BORDER);
		this.m_message.setLayoutData(new GridData(300, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_message.getLayoutData()).horizontalSpan = 3;
		this.m_message.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 拡張情報(ラベル)
		label = new Label(messageGroup, SWT.NONE);
		label.setText(Messages.getString("extended.info") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)label.getLayoutData()).verticalSpan = 3;
		((GridData)label.getLayoutData()).verticalAlignment = SWT.TOP;

		// 拡張情報
		Table expTable = new Table(messageGroup, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		expTable.setHeaderVisible(true);
		expTable.setLinesVisible(true);
		expTable.setLayoutData(new GridData(300, 50));
		((GridData)expTable.getLayoutData()).verticalSpan = 3;
		((GridData)expTable.getLayoutData()).horizontalSpan = 2;
		((GridData)expTable.getLayoutData()).verticalAlignment = SWT.TOP;

		// ボタン：追加（ボタン）
		m_expCreateButton = new Button(messageGroup, SWT.NONE);
		this.m_expCreateButton.setText(Messages.getString("add"));
		this.m_expCreateButton.setLayoutData(new GridData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_expCreateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JobLinkExpAddDialog dialog = new JobLinkExpAddDialog(m_shell);
				if (dialog.open() == IDialogConstants.OK_ID) {
					JobLinkExpInfoResponse info = dialog.getInputData();
					@SuppressWarnings("unchecked")
					ArrayList<Object> list = (ArrayList<Object>) m_expTableViewer.getInput();
					
					if (list == null) {
						list = new ArrayList<>();
					}
					String infoKey = info.getKey();
					for (Object obj : list) {
						@SuppressWarnings("unchecked")
						String key = (String) ((ArrayList<Object>)obj).get(0);
						if (infoKey.equals(key)) {
							// キーの重複エラー
							MessageDialog.openWarning(null, Messages.getString("warning"),
									Messages.getString("message.common.16",
											new String[]{Messages.getString("key")}));
							return;
						}
					}
					ArrayList<Object> tableLineData = new ArrayList<Object>();
					tableLineData.add(info.getKey());
					tableLineData.add(info.getValue());
					list.add(tableLineData);
					m_expTableViewer.setInput(list);
					changeExpAddButtonEnabled();
				}
			}
		});

		// ボタン：削除（ボタン）
		this.m_expDeleteButton = new Button(messageGroup, SWT.NONE);
		this.m_expDeleteButton.setText(Messages.getString("delete"));
		this.m_expDeleteButton.setLayoutData(new GridData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_expDeleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ArrayList<?> list = (ArrayList<?>) m_expTableViewer.getInput();
				list.remove(m_expSelectItem);
				m_expSelectItem = null;
				m_expTableViewer.setInput(list);
				changeExpAddButtonEnabled();
			}
		});

		// dummy
		label = new Label(messageGroup, SWT.NONE);
		label.setLayoutData(new GridData(80, SizeConstant.SIZE_BUTTON_HEIGHT));

		// 送信に失敗した場合再送する
		Composite endComposite = new Composite(this, SWT.NONE);
		endComposite.setLayout(new GridLayout(2, true));

		// 送信成功（グループ）
		Group successGroup = new Group(endComposite, SWT.NONE);
		successGroup.setText(Messages.getString("send.success"));
		successGroup.setLayout(new GridLayout(2, false));
		successGroup.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, true));

		// 送信成功 - 終了値（ラベル）
		label = new Label(successGroup, SWT.LEFT);
		label.setText(Messages.getString("end.value") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 送信成功 - 終了値（テキスト）
		this.m_successEndValue = new Text(successGroup, SWT.BORDER);
		this.m_successEndValue.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_successEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_successEndValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 送信失敗（グループ）
		Group failureGroup = new Group(endComposite, SWT.NONE);
		failureGroup.setText(Messages.getString("send.failure"));
		failureGroup.setLayout(new GridLayout(2, false));
		failureGroup.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, true));

		// 送信失敗時 - 操作（ラベル）
		label = new Label(failureGroup, SWT.NONE);
		label.setText(Messages.getString("send.failure.operation") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_BUTTON_HEIGHT));

		// 送信失敗時 - 操作（コンボボックス）
		this.m_failureOperation = new Combo(failureGroup, SWT.CENTER | SWT.READ_ONLY);
		this.m_failureOperation.setLayoutData(new GridData(120, SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_failureOperation.add(OperationMessage.STRING_STOP_SUSPEND);
		this.m_failureOperation.add(OperationMessage.STRING_STOP_SET_END_VALUE);
		this.m_failureOperation.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo check = (Combo) e.getSource();
				JobLinkSendInfoResponse.FailureOperationEnum type = getSelectOperation(check);
				if (type == JobLinkSendInfoResponse.FailureOperationEnum.SET_END_VALUE) {
					m_failureEndStatus.setEnabled(true);
					m_failureEndValue.setEditable(true);
				} else {
					m_failureEndStatus.setEnabled(false);
					m_failureEndValue.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 送信失敗時 - 終了状態（ラベル）
		label = new Label(failureGroup, SWT.NONE);
		label.setText(Messages.getString("end.status") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 送信失敗時 - 終了状態（コンボボックス）
		this.m_failureEndStatus = new Combo(failureGroup, SWT.CENTER | SWT.READ_ONLY);
		this.m_failureEndStatus.setLayoutData(new GridData(80, SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_failureEndStatus.add(EndStatusMessage.STRING_NORMAL);
		this.m_failureEndStatus.add(EndStatusMessage.STRING_WARNING);
		this.m_failureEndStatus.add(EndStatusMessage.STRING_ABNORMAL);

		// 送信失敗時 - 終了値（ラベル）
		label = new Label(failureGroup, SWT.NONE);
		label.setText(Messages.getString("end.value") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 送信失敗時 - 終了値（テキスト）
		// 標準出力 出力失敗時 終了値
		this.m_failureEndValue = new Text(failureGroup, SWT.BORDER);
		this.m_failureEndValue.setLayoutData(new GridData(80, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_failureEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_failureEndValue.addModifyListener(
				new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) {
						update();
					}
				}
			);

		this.m_expTableViewer = new CommonTableViewer(expTable);
		this.m_expTableViewer.createTableColumn(GetJobLinkExpTableDefine.get(),
				GetJobLinkExpTableDefine.SORT_COLUMN_INDEX,
				GetJobLinkExpTableDefine.SORT_ORDER);
		this.m_expTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
					//選択行を取得
					@SuppressWarnings("unchecked")
					ArrayList<Object> info = (ArrayList<Object>) ((StructuredSelection) event.getSelection()).getFirstElement();
					setExpSelectItem(info);
				} else {
					setExpSelectItem(null);
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
		if (m_joblinkMessageId.getEditable() && "".equals(m_joblinkMessageId.getText())) {
			this.m_joblinkMessageId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_joblinkMessageId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_successEndValue.getEditable() && "".equals(m_successEndValue.getText())) {
			this.m_successEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_successEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_failureOperation.getEnabled() && "".equals(m_failureOperation.getText())) {
			this.m_failureOperation.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_failureOperation.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_failureEndStatus.getEnabled() && "".equals(m_failureEndStatus.getText())) {
			this.m_failureEndStatus.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_failureEndStatus.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_failureEndValue.getEditable() && "".equals(m_failureEndValue.getText())) {
			this.m_failureEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_failureEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}


	/**
	 * ジョブ連携送信情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobLinkSendInfo
	 */
	public void reflectJobLinkSendInfo(boolean callJobHistory) {
		// ジョブ履歴からの遷移か否か
		m_callJobHistory =  callJobHistory;

		// 初期値
		m_retry.setSelection(false);
		m_retryCount.setText(String.valueOf(0));
		m_joblinkMessageId.setText("");
		setSelectPriority(m_priority, JobLinkSendInfoResponse.PriorityEnum.INFO);
		m_message.setText("");
		m_successEndValue.setText(String.valueOf(0));
		setSelectOperation(m_failureOperation, JobLinkSendInfoResponse.FailureOperationEnum.SET_END_VALUE);
		setSelectStatus(m_failureEndStatus, JobLinkSendInfoResponse.FailureEndStatusEnum.ABNORMAL);
		m_failureEndValue.setText(String.valueOf(-1));

		// ジョブ連携送信設定ID
		if (m_callJobHistory) {
			if (m_info != null && m_info.getJoblinkSendSettingId() != null) {
				this.m_joblinkSendSettingIdCombo.add(m_info.getJoblinkSendSettingId());
				this.m_joblinkSendSettingIdCombo.setText(m_info.getJoblinkSendSettingId());
			}
		} else {
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
	
			String selectLabel = "";
			this.m_joblinkSendSettingIdCombo.removeAll();
			this.m_joblinkSendSettingIdCombo.add("");
			if (joblinkSendSettingList != null && joblinkSendSettingList.size() > 0) {
				for (JobLinkSendSettingResponse jobLinkSendSetting : joblinkSendSettingList) {
					if (m_info != null 
							&& m_info.getJoblinkSendSettingId().equals(jobLinkSendSetting.getJoblinkSendSettingId())) {
						selectLabel = jobLinkSendSetting.getJoblinkSendSettingId();
					}
					this.m_joblinkSendSettingIdCombo.add(jobLinkSendSetting.getJoblinkSendSettingId());
				}
			}
			this.m_joblinkSendSettingIdCombo.setText(selectLabel);
		}

		if (m_info != null) {

			// 送信に失敗した場合再送する
			m_retry.setSelection(m_info.getRetryFlg());

			// 再送回数
			if (m_info.getRetryCount() != null) {
				m_retryCount.setText(String.valueOf(m_info.getRetryCount()));
			}

			// ジョブ連携メッセージID
			if (m_info.getJoblinkMessageId() != null) {
				m_joblinkMessageId.setText(m_info.getJoblinkMessageId());
			}

			// 重要度
			setSelectPriority(m_priority, m_info.getPriority());

			// メッセージ
			if (m_info.getMessage() != null) {
				m_message.setText(m_info.getMessage());
			}

			// 拡張情報
			List<JobLinkExpInfoResponse> expList = m_info.getJobLinkExpList();
			if(expList != null){
				ArrayList<Object> tableData = new ArrayList<Object>();
				for (int i = 0; i < expList.size(); i++) {
					JobLinkExpInfoResponse info = expList.get(i);
					ArrayList<Object> tableLineData = new ArrayList<Object>();
					tableLineData.add(info.getKey());
					tableLineData.add(info.getValue());
					tableData.add(tableLineData);
				}
				m_expTableViewer.setInput(tableData);
			}
			changeExpAddButtonEnabled();

			// 終了値（送信成功）
			if (m_info.getSuccessEndValue() != null) {
				m_successEndValue.setText(String.valueOf(m_info.getSuccessEndValue()));
			}

			// 送信失敗時の操作
			setSelectOperation(m_failureOperation, m_info.getFailureOperation());

			// 送信失敗時の操作
			setSelectStatus(m_failureEndStatus, m_info.getFailureEndStatus());

			// 終了値（送信失敗）
			if (m_info.getFailureEndValue() != null) {
				m_failureEndValue.setText(String.valueOf(m_info.getFailureEndValue()));
			}
		}

		// オブジェクトの有効/無効設定
		m_retryCount.setEditable(m_retry.getSelection());
		JobLinkSendInfoResponse.FailureOperationEnum type = getSelectOperation(m_failureOperation);
		if (type == JobLinkSendInfoResponse.FailureOperationEnum.SET_END_VALUE) {
			m_failureEndStatus.setEnabled(true);
			m_failureEndValue.setEditable(true);
		} else {
			m_failureEndStatus.setEnabled(false);
			m_failureEndValue.setEditable(false);
		}

		update();
	}

	/**
	 * ジョブ連携送信情報を設定します。
	 *
	 * @param info ジョブ連携送信情報
	 */
	public void setJobLinkSendInfo(JobLinkSendInfoResponse info) {
		m_info = info;
	}

	/**
	 * ジョブ連携送信情報を返します。
	 *
	 * @return ジョブ連携送信情報
	 */
	public JobLinkSendInfoResponse getJobLinkSendInfo() {
		return m_info;
	}

	/**
	 * コンポジットの情報から、ジョブ連携送信情報を作成する。
	 *
	 * @return 入力値の検証結果
	 */
	public ValidateResult createJobLinkSendInfo() {
		ValidateResult result = null;

		// インスタンスを作成
		m_info = new JobLinkSendInfoResponse();

		// ジョブ連携送信設定ID
		if (m_joblinkSendSettingIdCombo.getText().isEmpty()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.common.1", 
					new String[]{Messages.getString("joblink.send.setting.id")}));
			return result;
		}
		m_info.setJoblinkSendSettingId(m_joblinkSendSettingIdCombo.getText());

		// 送信に失敗した場合再送する
		m_info.setRetryFlg(m_retry.getSelection());

		// 再送回数
		if (m_info.getRetryFlg() || !m_retryCount.getText().isEmpty()) {
			try {
				m_info.setRetryCount(
						Integer.parseInt(m_retryCount.getText()));
			} catch (NumberFormatException e) {
				if (m_info.getRetryFlg()) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.common.15", 
							new String[]{Messages.getString("resend.count")}));
					return result;
				}
			}
		}

		// ジョブ連携メッセージID
		if (m_joblinkMessageId.getText().isEmpty()) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.common.1", 
					new String[]{Messages.getString("joblink.message.id")}));
			return result;
		}
		m_info.setJoblinkMessageId(m_joblinkMessageId.getText());

		// 重要度
		m_info.setPriority(getSelectPriority(m_priority));

		// メッセージ
		m_info.setMessage(m_message.getText());

		// 拡張情報
		ArrayList<JobLinkExpInfoResponse> expList = new ArrayList<>();
		ArrayList<?> tableData = (ArrayList<?>) m_expTableViewer.getInput();
		if (tableData != null) {
			for (int i = 0; i < tableData.size(); i++) {
				ArrayList<?> tableLineData = (ArrayList<?>) tableData.get(i);
				JobLinkExpInfoResponse expInfo = new JobLinkExpInfoResponse();
				expInfo.setKey((String) tableLineData.get(GetJobLinkExpTableDefine.KEY));
				expInfo.setValue((String) tableLineData.get(GetJobLinkExpTableDefine.VALUE));
				expList.add(expInfo);
			}
		}
		m_info.getJobLinkExpList().clear();
		m_info.getJobLinkExpList().addAll(expList);

		// 送信成功 - 終了値
		try {
			m_info.setSuccessEndValue(
					Integer.parseInt(m_successEndValue.getText()));
		} catch (NumberFormatException e) {
			if (m_info.getRetryFlg()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.common.15", 
						new String[]{Messages.getString("end.value")
						+ "(" + Messages.getString("send.success") + ")"}));
				return result;
			}
		}

		// 送信失敗 - 操作
		m_info.setFailureOperation(getSelectOperation(m_failureOperation));

		// 送信失敗 - 終了状態
		m_info.setFailureEndStatus(getSelectStatus(m_failureEndStatus));

		// 送信失敗 - 終了値
		if (m_info.getFailureOperation() == JobLinkSendInfoResponse.FailureOperationEnum.SET_END_VALUE
				|| !m_failureEndValue.getText().isEmpty()) {
			try {
				m_info.setFailureEndValue(
						Integer.parseInt(m_failureEndValue.getText()));
			} catch (NumberFormatException e) {
				if (m_info.getRetryFlg()) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.common.15", 
							new String[]{Messages.getString("end.value")
							+ "(" + Messages.getString("send.failure") + ")"}));
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_joblinkSendSettingIdCombo.setEnabled(enabled);
		this.m_retry.setEnabled(enabled);
		this.m_retryCount.setEditable(this.m_retry.getSelection() && enabled);
		this.m_joblinkMessageId.setEditable(enabled);
		this.m_priority.setEnabled(enabled);
		this.m_message.setEditable(enabled);
		this.m_expDeleteButton.setEnabled(enabled);
		this.m_successEndValue.setEditable(enabled);
		this.m_failureOperation.setEnabled(enabled);
		JobLinkSendInfoResponse.FailureOperationEnum type = getSelectOperation(m_failureOperation);
		if (type == JobLinkSendInfoResponse.FailureOperationEnum.SET_END_VALUE) {
			m_failureEndStatus.setEnabled(enabled);
			m_failureEndValue.setEditable(enabled);
		} else {
			m_failureEndStatus.setEnabled(false);
			m_failureEndValue.setEditable(false);
		}
		if (enabled) {
			changeExpAddButtonEnabled();
		} else {
			this.m_expCreateButton.setEnabled(false);
		}
	}

	public void setOwnerRoleId(String ownerRoleId) {
		m_ownerRoleId = ownerRoleId;
	}

	/**
	 * @param m_managerName the m_managerName to set
	 */
	public void setManagerName(String managerName) {
		m_managerName = managerName;
	}

	/**
	 * 拡張情報の選択アイテムを設定します。
	 *
	 * @param expSelectItem 選択アイテム
	 */
	public void setExpSelectItem(ArrayList<Object> expSelectItem) {
		m_expSelectItem = expSelectItem;
	}

	private void changeExpAddButtonEnabled() {
		ArrayList<?> list = (ArrayList<?>) m_expTableViewer.getInput();
		if (list != null && list.size() >= JobLinkConstant.EXP_INFO_MAX_COUNT) {
			m_expCreateButton.setEnabled(false);
		} else {
			m_expCreateButton.setEnabled(true);
		}
	}
	/**
	 * 指定した重要度に該当するコンボボックスの項目を選択します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @param enumValue 重要度
	 */
	private void setSelectPriority(Combo combo, JobLinkSendInfoResponse.PriorityEnum enumValue) {
		String select = "";
		if (enumValue == JobLinkSendInfoResponse.PriorityEnum.CRITICAL) {
			select = PriorityMessage.STRING_CRITICAL;
		} else if (enumValue == JobLinkSendInfoResponse.PriorityEnum.WARNING) {
			select = PriorityMessage.STRING_WARNING;
		} else if (enumValue == JobLinkSendInfoResponse.PriorityEnum.INFO) {
			select = PriorityMessage.STRING_INFO;
		} else if (enumValue == JobLinkSendInfoResponse.PriorityEnum.UNKNOWN) {
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
	 * コンボボックスにて選択している重要度を取得します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @return 重要度
	 */
	private JobLinkSendInfoResponse.PriorityEnum getSelectPriority(Combo combo) {
		String select = combo.getText();

		if (select.equals(PriorityMessage.STRING_CRITICAL)) {
			return JobLinkSendInfoResponse.PriorityEnum.CRITICAL;
		} else if (select.equals(PriorityMessage.STRING_WARNING)) {
			return JobLinkSendInfoResponse.PriorityEnum.WARNING;
		} else if (select.equals(PriorityMessage.STRING_INFO)) {
			return JobLinkSendInfoResponse.PriorityEnum.INFO;
		} else if (select.equals(PriorityMessage.STRING_UNKNOWN)) {
			return JobLinkSendInfoResponse.PriorityEnum.UNKNOWN;
		}

		return null;
	}

	/**
	 * 指定した操作に該当するコンボボックスの項目を選択します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @param enumValue 操作
	 */
	private void setSelectOperation(Combo combo, JobLinkSendInfoResponse.FailureOperationEnum enumValue) {
		String select = "";
		if (enumValue == JobLinkSendInfoResponse.FailureOperationEnum.SUSPEND) {
			select = OperationMessage.STRING_STOP_SUSPEND;
		} else if (enumValue == JobLinkSendInfoResponse.FailureOperationEnum.SET_END_VALUE) {
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
	 * コンボボックスにて選択している操作を取得します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @return 操作
	 */
	private JobLinkSendInfoResponse.FailureOperationEnum getSelectOperation(Combo combo) {
		String select = combo.getText();
		if (select.equals(OperationMessage.STRING_STOP_SUSPEND)) {
			return JobLinkSendInfoResponse.FailureOperationEnum.SUSPEND;
		} else if (select.equals(OperationMessage.STRING_STOP_SET_END_VALUE)) {
			return JobLinkSendInfoResponse.FailureOperationEnum.SET_END_VALUE;
		}

		return null;
	}

	/**
	 * 指定した終了状態に該当するコンボボックスの項目を選択します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @param enumValue 終了状態
	 */
	private void setSelectStatus(Combo combo, JobLinkSendInfoResponse.FailureEndStatusEnum enumValue) {
		String select = "";
		select = EndStatusMessage.typeEnumValueToString(enumValue.getValue());

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * コンボボックスにて選択している終了状態を取得します。
	 *
	 * @param combo コンボボックスのインスタンス
	 * @return 終了状態
	 */
	private JobLinkSendInfoResponse.FailureEndStatusEnum getSelectStatus(Combo combo) {
		String select = combo.getText();
		String enmuValue = EndStatusMessage.stringTotypeEnumValue(select);
		return JobLinkSendInfoResponse.FailureEndStatusEnum.fromValue(enmuValue) ;
	}
}
