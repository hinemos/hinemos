/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobLinkExpInfoRequest;
import org.openapitools.client.model.JobLinkExpInfoResponse;
import org.openapitools.client.model.JobLinkSendInfoResponse;
import org.openapitools.client.model.SendJobLinkMessageManualRequest;

import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.jobmanagement.action.GetJobLinkExpTableDefine;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ジョブ連携メッセージの手動送信ダイアログです。
 *
 */
public class JobLinkMessageManualSendDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( JobLinkMessageManualSendDialog.class );

	/** ジョブ連携メッセージ手動送信情報 */
	private SendJobLinkMessageManualRequest m_request;

	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	/** ジョブ連携送信設定ID用テキスト */
	private Text m_txtJoblinkSendSettingId = null;

	/** ジョブ連携メッセージID(テキスト) */
	private Text m_joblinkMessageId;
	/** 重要度(コンボボックス) */
	private Combo m_priority;
	/** アプリケーション */
	private Text m_application = null;
	/** 監視詳細 */
	private Text m_monitorDetailId = null;
	/** メッセージ(テキスト) */
	private Text m_message;

	/** 拡張情報 */
	private CommonTableViewer m_expTableViewer = null;
	/** 拡張情報：追加(ボタン) */
	private Button m_expCreateButton = null;
	/** 拡張情報：削除(ボタン) */
	private Button m_expDeleteButton = null;

	/** 選択アイテム(拡張情報) */
	private ArrayList<Object> m_expSelectItem = null;

	/** シェル */
	private Shell m_shell = null;

	/** ジョブ連携送信設定ID */
	private String m_joblinkSendSettingId;

	/** マネージャ名 */
	private String m_managerName;

	/**
	 * コンストラクタ
	 * 変更時、コピー時
	 */
	public JobLinkMessageManualSendDialog(Shell parent, String managerName, String joblinkSendSettingId){
		super(parent);
		this.m_managerName = managerName;
		this.m_joblinkSendSettingId = joblinkSendSettingId;
	}

	@Override
	protected void customizeDialog(Composite parent) {
		Label label = null;
		int labelWidth = 140;

		// タイトル
		parent.getShell().setText(Messages.get("dialog.joblinkmessage.send.manual"));

		// ベースレイアウト
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);

		// Composite
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		// マネージャ（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.get("facility.manager") + " : ");
		label.setLayoutData(new GridData(labelWidth, SizeConstant.SIZE_LABEL_HEIGHT));

		// マネージャ（テキスト）
		this.m_managerComposite = new ManagerListComposite(composite, SWT.NONE, false);
		this.m_managerComposite.setLayoutData(new GridData());
		((GridData)this.m_managerComposite.getLayoutData()).widthHint = 227;
		if(this.m_managerName != null) {
			this.m_managerComposite.setText(this.m_managerName);
		}

		// ジョブ連携送信設定ID（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.get("joblink.send.setting.id") + " : ");
		label.setLayoutData(new GridData(labelWidth, SizeConstant.SIZE_LABEL_HEIGHT));

		// ジョブ連携送信設定ID（テキスト）
		m_txtJoblinkSendSettingId = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		m_txtJoblinkSendSettingId.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		m_txtJoblinkSendSettingId.setText(this.m_joblinkSendSettingId);

		// ジョブ連携メッセージ情報（グループ）
		Group messageGroup = new Group(composite, SWT.NONE);
		messageGroup.setText(Messages.getString("joblink.message.info"));
		messageGroup.setLayoutData(new GridData());
		((GridData)messageGroup.getLayoutData()).horizontalSpan = 2;
		messageGroup.setLayout(new GridLayout(3, false));

		// ジョブ連携メッセージID(ラベル)
		label = new Label(messageGroup, SWT.NONE);
		label.setText(Messages.getString("joblink.message.id") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));

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
		((GridData)this.m_priority.getLayoutData()).horizontalSpan = 2;
		this.m_priority.add(PriorityMessage.STRING_INFO);
		this.m_priority.add(PriorityMessage.STRING_WARNING);
		this.m_priority.add(PriorityMessage.STRING_CRITICAL);
		this.m_priority.add(PriorityMessage.STRING_UNKNOWN);
		setSelectPriority(m_priority, JobLinkSendInfoResponse.PriorityEnum.INFO);

		// アプリケーション(ラベル)
		label = new Label(messageGroup, SWT.NONE);
		label.setText(Messages.getString("application") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// アプリケーション(テキスト)
		this.m_application = new Text(messageGroup, SWT.BORDER);
		this.m_application.setLayoutData(new GridData(260, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_application.getLayoutData()).horizontalSpan = 2;
		this.m_application.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 監視詳細(ラベル)
		label = new Label(messageGroup, SWT.NONE);
		label.setText(Messages.getString("monitor.detail.id") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// 監視詳細(テキスト)
		this.m_monitorDetailId = new Text(messageGroup, SWT.BORDER);
		this.m_monitorDetailId.setLayoutData(new GridData(260, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_monitorDetailId.getLayoutData()).horizontalSpan = 2;
		this.m_monitorDetailId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// メッセージ(ラベル)
		label = new Label(messageGroup, SWT.NONE);
		label.setText(Messages.getString("message") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// メッセージ(テキスト)
		this.m_message = new Text(messageGroup, SWT.BORDER);
		this.m_message.setLayoutData(new GridData(260, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_message.getLayoutData()).horizontalSpan = 2;
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
		expTable.setLayoutData(new GridData(260, 50));
		((GridData)expTable.getLayoutData()).verticalSpan = 3;
		((GridData)expTable.getLayoutData()).verticalAlignment = SWT.TOP;

		// ボタン：追加（ボタン）
		m_expCreateButton = new Button(messageGroup, SWT.NONE);
		this.m_expCreateButton.setText(Messages.getString("add"));
		this.m_expCreateButton.setLayoutData(new GridData(60,
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
				}
			}
		});

		// ボタン：削除（ボタン）
		this.m_expDeleteButton = new Button(messageGroup, SWT.NONE);
		this.m_expDeleteButton.setText(Messages.getString("delete"));
		this.m_expDeleteButton.setLayoutData(new GridData(60,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_expDeleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ArrayList<?> list = (ArrayList<?>) m_expTableViewer.getInput();
				list.remove(m_expSelectItem);
				m_expSelectItem = null;
				m_expTableViewer.setInput(list);
			}
		});

		// dummy
		label = new Label(messageGroup, SWT.NONE);
		label.setLayoutData(new GridData(80, SizeConstant.SIZE_BUTTON_HEIGHT));

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

		// 表示調整
		adjustPosition(600);

		update();
	}

	@Override
	protected String getOkButtonText() {
		return Messages.get("send");
	}

	@Override
	protected String getCancelButtonText() {
		return Messages.get("cancel");
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
		// 入力値生成
		m_request = new SendJobLinkMessageManualRequest();
		m_request.setJoblinkSendSettingId(m_txtJoblinkSendSettingId.getText());

		// ジョブ連携メッセージID
		m_request.setJoblinkMessageId(m_joblinkMessageId.getText());

		// 重要度
		m_request.setPriority(getSelectPriority(m_priority));

		// アプリケーション
		m_request.setApplication(m_application.getText());

		// 監視詳細
		m_request.setMonitorDetailId(m_monitorDetailId.getText());

		// メッセージ
		m_request.setMessage(m_message.getText());

		// 拡張情報
		ArrayList<JobLinkExpInfoRequest> expList = new ArrayList<>();
		ArrayList<?> tableData = (ArrayList<?>) m_expTableViewer.getInput();
		if (tableData != null) {
			for (int i = 0; i < tableData.size(); i++) {
				ArrayList<?> tableLineData = (ArrayList<?>) tableData.get(i);
				JobLinkExpInfoRequest expInfo = new JobLinkExpInfoRequest();
				expInfo.setKey((String) tableLineData.get(GetJobLinkExpTableDefine.KEY));
				expInfo.setValue((String) tableLineData.get(GetJobLinkExpTableDefine.VALUE));
				expList.add(expInfo);
			}
		}
		m_request.getJobLinkExpList().clear();
		m_request.getJobLinkExpList().addAll(expList);
		return null;
	}

	@Override
	protected boolean action() {
		boolean result = false;
		Object[] arg = {m_request.getJoblinkSendSettingId(), m_request.getJoblinkMessageId()};
		try {
			String managerName = this.m_managerComposite.getText();
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			wrapper.sendJobLinkMessageManual(m_request);
			MessageDialog.openInformation(null, Messages.getString("successful"),
					Messages.getString("message.joblinkmessage.send.success", arg));
			result = true;
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (InvalidUserPass | InvalidSetting e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.joblinkmessage.send.failure", arg) + " " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("action(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return result;
	}

	private void update() {
		if (m_joblinkMessageId.getEditable() && "".equals(m_joblinkMessageId.getText())) {
			this.m_joblinkMessageId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_joblinkMessageId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 拡張情報の選択アイテムを設定します。
	 *
	 * @param expSelectItem 選択アイテム
	 */
	public void setExpSelectItem(ArrayList<Object> expSelectItem) {
		m_expSelectItem = expSelectItem;
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
	private SendJobLinkMessageManualRequest.PriorityEnum getSelectPriority(Combo combo) {
		String select = combo.getText();

		if (select.equals(PriorityMessage.STRING_CRITICAL)) {
			return SendJobLinkMessageManualRequest.PriorityEnum.CRITICAL;
		} else if (select.equals(PriorityMessage.STRING_WARNING)) {
			return SendJobLinkMessageManualRequest.PriorityEnum.WARNING;
		} else if (select.equals(PriorityMessage.STRING_INFO)) {
			return SendJobLinkMessageManualRequest.PriorityEnum.INFO;
		} else if (select.equals(PriorityMessage.STRING_UNKNOWN)) {
			return SendJobLinkMessageManualRequest.PriorityEnum.UNKNOWN;
		}

		return null;
	}
}
