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
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.JobKickResponse;
import org.openapitools.client.model.JobLinkExpInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetJobLinkExpTableDefine;
import com.clustercontrol.jobmanagement.dialog.JobLinkExpAddDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ジョブ実行契機の作成・変更ダイアログのジョブ連携受信設定タブ用の
 * コンポジットクラスです。
 *
 */
public class JobKickJobLinkRcvComposite extends Composite {

	/** シェル */
	private Shell m_shell = null;

	/** マネージャ名 */
	private String m_managerName = null;
	/** オーナーロールID */
	private String m_ownerRoleId = null;
	/** 選択されたスコープのファシリティID。 */
	private String m_facilityId = null;

	/** スコープ用テキスト */
	private Text m_txtScope = null;
	/** スコープ参照用ボタン */
	private Button m_btnScopeSelect = null;

	/** ジョブ連携メッセージID */
	private Text m_joblinkMessageId = null;

	/** 重要度（情報）有効/無効 */
	private Button m_infoValid = null;
	/** 重要度（警告）有効/無効 */
	private Button m_warnValid = null;
	/** 重要度（危険）有効/無効 */
	private Button m_criticalValid = null;
	/** 重要度（不明）有効/無効 */
	private Button m_unknownValid = null;

	/** アプリケーションフラグ */
	private Button m_application = null;
	/** アプリケーション */
	private Text m_applicationText = null;

	/** 監視詳細フラグ */
	private Button m_monitorDetailId = null;
	/** 監視詳細 */
	private Text m_monitorDetailIdText = null;

	/** メッセージフラグ */
	private Button m_message = null;
	/** メッセージ */
	private Text m_messageText = null;

	/** 拡張情報フラグ */
	private Button m_exp = null;
	/** 拡張情報 */
	private CommonTableViewer m_expTableViewer = null;
	/** 拡張情報：追加用ボタン */
	private Button m_expCreateButton = null;
	/** 拡張情報：削除用ボタン */
	private Button m_expDeleteButton = null;

	/** 選択アイテム(拡張情報) */
	private ArrayList<Object> m_expSelectItem = null;

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
	public JobKickJobLinkRcvComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		this.m_shell = this.getShell();
		Label label = null;

		this.setLayout(JobDialogUtil.getParentLayout());

		// Composite
		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new RowData());
		((RowData)composite.getLayoutData()).width = 545;

		// スコープ（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("scope") + " : ");

		// スコープ（テキスト）
		this.m_txtScope =  new Text(composite, SWT.READ_ONLY | SWT.BORDER);
		this.m_txtScope.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtScope.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// スコープ参照（ボタン）
		this.m_btnScopeSelect = new Button(composite, SWT.NONE);
		this.m_btnScopeSelect.setLayoutData(new GridData(40, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnScopeSelect.setText(Messages.getString("refer"));
		this.m_btnScopeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(m_shell, m_managerName, m_ownerRoleId);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse selectItem = dialog.getSelectItem();
					FacilityInfoResponse info = selectItem.getData();
					FacilityPath path = new FacilityPath(
							ClusterControlPlugin.getDefault()
							.getSeparator());
					m_facilityId = info.getFacilityId();
					m_txtScope.setText(path.getPath(selectItem));
					update();
				}
			}
		});

		// 条件（グループ）
		Group ruleGroup = new Group(composite, SWT.NONE);
		ruleGroup.setLayoutData(new GridData());
		((GridData)ruleGroup.getLayoutData()).horizontalSpan = 3;
		ruleGroup.setText(Messages.getString("monitor.rule"));
		ruleGroup.setLayout(new GridLayout(3, false));

		// ジョブ連携メッセージID(ラベル)
		label = new Label(ruleGroup, SWT.NONE);
		label.setText(Messages.getString("joblink.message.id") + " : ");
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));

		// ジョブ連携メッセージID
		this.m_joblinkMessageId = new Text(ruleGroup, SWT.BORDER);
		this.m_joblinkMessageId.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_joblinkMessageId.getLayoutData()).horizontalSpan = 2;
		this.m_joblinkMessageId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 重要度（グループ）
		Group priorityGroup = new Group(ruleGroup, SWT.NONE);
		priorityGroup.setText(Messages.getString("priority"));
		priorityGroup.setLayoutData(new GridData());
		((GridData)priorityGroup.getLayoutData()).horizontalSpan = 3;
		priorityGroup.setLayout(new GridLayout(8, false));

		// 重要度(レイアウト)
		GridData priorityLabelGridData = new GridData(60, SizeConstant.SIZE_BUTTON_HEIGHT);
		priorityLabelGridData.horizontalAlignment = GridData.FILL;
		priorityLabelGridData.grabExcessHorizontalSpace = true;
		GridData priorityCheckGridData = new GridData(40, SizeConstant.SIZE_BUTTON_HEIGHT);
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

		// アプリケーション(チェックボックス)
		m_application = new Button(ruleGroup, SWT.CHECK);
		m_application.setText(Messages.getString("application") + " : ");
		m_application.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)m_application.getLayoutData()).verticalAlignment = SWT.BEGINNING;
		m_application.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_applicationText.setEditable(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// アプリケーション(テキスト)
		this.m_applicationText = new Text(ruleGroup, SWT.BORDER);
		this.m_applicationText.setLayoutData(new GridData(250, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_applicationText.getLayoutData()).horizontalSpan = 2;
		this.m_applicationText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 監視詳細(チェックボックス)
		m_monitorDetailId = new Button(ruleGroup, SWT.CHECK);
		m_monitorDetailId.setText(Messages.getString("monitor.detail.id") + " : ");
		m_monitorDetailId.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)m_monitorDetailId.getLayoutData()).verticalAlignment = SWT.BEGINNING;
		m_monitorDetailId.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_monitorDetailIdText.setEditable(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 監視詳細(テキスト)
		this.m_monitorDetailIdText = new Text(ruleGroup, SWT.BORDER);
		this.m_monitorDetailIdText.setLayoutData(new GridData(250, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_monitorDetailIdText.getLayoutData()).horizontalSpan = 2;
		this.m_monitorDetailIdText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// メッセージ(チェックボックス)
		m_message = new Button(ruleGroup, SWT.CHECK);
		m_message.setText(Messages.getString("message") + " : ");
		m_message.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)m_message.getLayoutData()).verticalAlignment = SWT.BEGINNING;
		m_message.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_messageText.setEditable(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// メッセージ(テキスト)
		this.m_messageText = new Text(ruleGroup, SWT.BORDER);
		this.m_messageText.setLayoutData(new GridData(250, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_messageText.getLayoutData()).horizontalSpan = 2;
		this.m_messageText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 拡張情報(チェックボックス)
		m_exp = new Button(ruleGroup, SWT.CHECK);
		m_exp.setText(Messages.getString("extended.info") + " : ");
		m_exp.setLayoutData(new GridData(150, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)m_exp.getLayoutData()).verticalAlignment = SWT.BEGINNING;
		((GridData)m_exp.getLayoutData()).verticalSpan = 3;
		((GridData)m_exp.getLayoutData()).verticalAlignment = SWT.TOP;
		m_exp.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_expCreateButton.setEnabled(check.getSelection());
				m_expDeleteButton.setEnabled(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 拡張情報
		Table table = new Table(ruleGroup, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(250, 50));
		((GridData)table.getLayoutData()).verticalSpan = 3;
		((GridData)table.getLayoutData()).verticalAlignment = SWT.TOP;

		// ボタン：追加（ボタン）
		m_expCreateButton = new Button(ruleGroup, SWT.NONE);
		this.m_expCreateButton.setText(Messages.getString("add"));
		this.m_expCreateButton.setLayoutData(new GridData(50,
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
					} else {
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
					}
					m_expTableViewer.setInput(list);
				}
			}
		});

		// ボタン：削除（ボタン）
		this.m_expDeleteButton = new Button(ruleGroup, SWT.NONE);
		this.m_expDeleteButton.setText(Messages.getString("delete"));
		this.m_expDeleteButton.setLayoutData(new GridData(50,
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
		label = new Label(ruleGroup, SWT.NONE);
		label.setLayoutData(new GridData(80, SizeConstant.SIZE_BUTTON_HEIGHT));

		this.m_expTableViewer = new CommonTableViewer(table);
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
		if("".equals(this.m_txtScope.getText())){
			this.m_txtScope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_joblinkMessageId.getEditable() && "".equals(m_joblinkMessageId.getText())) {
			this.m_joblinkMessageId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_joblinkMessageId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_applicationText.getEditable() && "".equals(m_applicationText.getText())) {
			this.m_applicationText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_applicationText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_monitorDetailIdText.getEditable() && "".equals(m_monitorDetailIdText.getText())) {
			this.m_monitorDetailIdText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_monitorDetailIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (m_messageText.getEditable() && "".equals(m_messageText.getText())) {
			this.m_messageText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_messageText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ジョブ連携受信実行契機情報をコンポジットに反映します。
	 * 
	 * @param managerName マネージャ名
	 * @param jobKick ジョブ実行契機情報
	 */
	public void setJobLinkRcv(String managerName, JobKickResponse jobKick) {

		// マネージャ名
		this.m_managerName = managerName;

		// 初期値
		this.m_txtScope.setText("");
		this.m_joblinkMessageId.setText("");
		this.m_infoValid.setSelection(true);
		this.m_warnValid.setSelection(true);
		this.m_criticalValid.setSelection(true);
		this.m_unknownValid.setSelection(true);
		this.m_application.setSelection(false);
		this.m_applicationText.setText("");
		this.m_monitorDetailId.setSelection(false);
		this.m_monitorDetailIdText.setText("");
		this.m_message.setSelection(false);
		this.m_messageText.setText("");
		this.m_exp.setSelection(false);

		if (jobKick != null) {
			// オーナーロールID
			this.m_ownerRoleId = jobKick.getOwnerRoleId();

			//スコープ
			if(jobKick.getFacilityId() != null){
				this.m_facilityId = jobKick.getFacilityId();
				this.m_txtScope.setText(HinemosMessage.replace(jobKick.getScope()));
			}

			// 条件
			if (jobKick.getJoblinkMessageId() != null) {
				this.m_joblinkMessageId.setText(jobKick.getJoblinkMessageId());
			}
			if (jobKick.getInfoValidFlg() != null) {
				this.m_infoValid.setSelection(jobKick.getInfoValidFlg());
			}
			if (jobKick.getWarnValidFlg() != null) {
				this.m_warnValid.setSelection(jobKick.getWarnValidFlg());
			}
			if (jobKick.getCriticalValidFlg() != null) {
				this.m_criticalValid.setSelection(jobKick.getCriticalValidFlg());
			}
			if (jobKick.getUnknownValidFlg() != null) {
				this.m_unknownValid.setSelection(jobKick.getUnknownValidFlg());
			}
			if (jobKick.getApplicationFlg() != null) {
				this.m_application.setSelection(jobKick.getApplicationFlg());
			}
			if (jobKick.getApplication() != null) {
				this.m_applicationText.setText(jobKick.getApplication());
			}
			if (jobKick.getMonitorDetailIdFlg() != null) {
				this.m_monitorDetailId.setSelection(jobKick.getMonitorDetailIdFlg());
			}
			if (jobKick.getMonitorDetailId() != null) {
				this.m_monitorDetailIdText.setText(jobKick.getMonitorDetailId());
			}
			if (jobKick.getMessageFlg() != null) {
				this.m_message.setSelection(jobKick.getMessageFlg());
			}
			if (jobKick.getMessage() != null) {
				this.m_messageText.setText(jobKick.getMessage());
			}
			if (jobKick.getExpFlg() != null) {
				this.m_exp.setSelection(jobKick.getExpFlg());
			}

			// 拡張情報
			List<JobLinkExpInfoResponse> list = jobKick.getJobLinkExpList();
			if(list != null){
				ArrayList<Object> tableData = new ArrayList<Object>();
				for (int i = 0; i < list.size(); i++) {
					JobLinkExpInfoResponse info = list.get(i);
					ArrayList<Object> tableLineData = new ArrayList<Object>();
					tableLineData.add(info.getKey());
					tableLineData.add(info.getValue());
					tableData.add(tableLineData);
				}
				m_expTableViewer.setInput(tableData);
			}
		}
		// ウィジェットの有効/無効設定
		this.m_applicationText.setEditable(this.m_application.getSelection());
		this.m_monitorDetailIdText.setEditable(this.m_monitorDetailId.getSelection());
		this.m_messageText.setEditable(this.m_message.getSelection());
		this.m_expCreateButton.setEnabled(this.m_exp.getSelection());
		this.m_expDeleteButton.setEnabled(this.m_exp.getSelection());

		update();
	}


	/**
	 * コンポジットの情報から、ジョブ連携待機情報を作成する。
	 *
	 * @return 入力値の検証結果
	 */
	public ValidateResult createJobLinkRcvInfo(JobKickResponse jobKick) {
		ValidateResult result = null;

		if (jobKick == null) {
			return result;
		}

		// ファシリティID・スコープ
		if(this.m_facilityId != null && this.m_facilityId.length() > 0) {
			jobKick.setFacilityId(this.m_facilityId);
			jobKick.setScope(this.m_txtScope.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.hinemos.3"));
			return result;
		}

		// ジョブ連携メッセージID
		jobKick.setJoblinkMessageId(m_joblinkMessageId.getText());

		// 重要度（情報）有効/無効
		jobKick.setInfoValidFlg(m_infoValid.getSelection());
		// 重要度（警告）有効/無効
		jobKick.setWarnValidFlg(m_warnValid.getSelection());
		// 重要度（危険）有効/無効
		jobKick.setCriticalValidFlg(m_criticalValid.getSelection());
		// 重要度（不明）有効/無効
		jobKick.setUnknownValidFlg(m_unknownValid.getSelection());

		// アプリケーションフラグ
		jobKick.setApplicationFlg(m_application.getSelection());
		// アプリケーション
		jobKick.setApplication(m_applicationText.getText());

		// 監視詳細フラグ
		jobKick.setMonitorDetailIdFlg(m_monitorDetailId.getSelection());
		// 監視詳細
		jobKick.setMonitorDetailId(m_monitorDetailIdText.getText());

		// メッセージフラグ
		jobKick.setMessageFlg(m_message.getSelection());
		// メッセージ
		jobKick.setMessage(m_messageText.getText());

		// 拡張情報フラグ
		jobKick.setExpFlg(m_exp.getSelection());
		// 拡張情報
		ArrayList<JobLinkExpInfoResponse> expList = new ArrayList<>();
		ArrayList<?> tableData = (ArrayList<?>) m_expTableViewer.getInput();
		for (int i = 0; i < tableData.size(); i++) {
			ArrayList<?> tableLineData = (ArrayList<?>) tableData.get(i);
			JobLinkExpInfoResponse expInfo = new JobLinkExpInfoResponse();
			expInfo.setKey((String) tableLineData.get(GetJobLinkExpTableDefine.KEY));
			expInfo.setValue((String) tableLineData.get(GetJobLinkExpTableDefine.VALUE));
			expList.add(expInfo);
		}
		jobKick.getJobLinkExpList().clear();
		jobKick.getJobLinkExpList().addAll(expList);

		return null;
	}

	/**
	 * オーナーロールIDを設定します。
	 * @param ownerRoleId オーナーロールID
	 * @param managerName マネージャ名
	 */
	public void setOwnerRoleId(String managerName, String ownerRoleId) {
		this.m_managerName = managerName;
		this.m_ownerRoleId = ownerRoleId;
		this.m_facilityId = "";
		m_txtScope.setText("");
	}

	/**
	 * 拡張情報の選択アイテムを設定します。
	 *
	 * @param expSelectItem 選択アイテム
	 */
	public void setExpSelectItem(ArrayList<Object> expSelectItem) {
		m_expSelectItem = expSelectItem;
	}
}
