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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
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
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.CriticalSyslogFacilityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.CriticalSyslogPriorityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.EscalateFacilityFlgEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.InfoSyslogFacilityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.InfoSyslogPriorityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.UnknownSyslogFacilityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.UnknownSyslogPriorityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.WarnSyslogFacilityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.WarnSyslogPriorityEnum;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.TextWithParameterComposite;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.action.AddNotify;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.ModifyNotify;
import com.clustercontrol.notify.bean.SyslogFacilityConstant;
import com.clustercontrol.notify.bean.SyslogSeverityConstant;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.notify.util.SyslogFacilityUtil;
import com.clustercontrol.notify.util.SyslogSeverityUtil;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 通知（ログエスカレーション）作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 3.0.0
 */
public class NotifyLogEscalateCreateDialog extends NotifyBasicCreateDialog {

	/** カラム数（重要度）。 */
	private static final int WIDTH_PRIORITY 		= 2;

	/** カラム数（チェックボックス）。 */
	private static final int WIDTH_CHECK 			= 2;

	/** カラム数（コンボボックス（Facility））。 */
	private static final int WIDTH_COMBO_FACILITY 			= 2;

	/** カラム数（コンボボックス（Severity））。 */
	private static final int WIDTH_COMBO_SEVERITY 			= 3;

	/** カラム数（メッセージ）。 */
	private static final int WIDTH_MESSAGE			= 6;


	// ----- instance フィールド ----- //

	/** 通知タイプ
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	private static final int TYPE_LOG_ESCALATE=4;

	/** 入力値の正当性を保持するオブジェクト。 */
	protected ValidateResult validateResult = null;

	/** スコープ用テキスト */
	private Text m_textScope = null;

	/** ファシリティID */
	private String m_facilityId = null;

	/** スコープ */
	private String m_facilityPath = null;

	/** 出力ノード用ラジオボタン */
	private Button m_radioGenerationNodeValue = null;

	/** 固定値用ラジオボタン */
	private Button m_radioFixedValue = null;

	/** スコープ参照用ボタン */
	private Button m_scopeSelect = null;

	/** エスカレート先ポートテキストボックス。 */
	private Text m_textEscalatePort = null;

	/** 実行（重要度：通知） チェックボックス。 */
	private Button m_checkLogEscalateRunInfo = null;
	/** 実行（重要度：警告） チェックボックス。 */
	private Button m_checkLogEscalateRunWarning = null;
	/** 実行（重要度：危険） チェックボックス。 */
	private Button m_checkLogEscalateRunCritical = null;
	/** 実行（重要度：不明） チェックボックス。 */
	private Button m_checkLogEscalateRunUnknown = null;

	/** Facility（重要度：通知） コンボボックス。 */
	private Combo m_comboLogEscalateFacilityInfo = null;
	/** Facility（重要度：警告） コンボボックス。 */
	private Combo m_comboLogEscalateFacilityWarning = null;
	/** Facility（重要度：危険） コンボボックス。 */
	private Combo m_comboLogEscalateFacilityCritical = null;
	/** Facility（重要度：不明） コンボボックス。 */
	private Combo m_comboLogEscalateFacilityUnknown = null;

	/** Severity（重要度：通知） コンボボックス。 */
	private Combo m_comboLogEscalateSeverityInfo = null;
	/** Severity（重要度：警告） コンボボックス。 */
	private Combo m_comboLogEscalateSeverityWarning = null;
	/** Severity（重要度：危険） コンボボックス。 */
	private Combo m_comboLogEscalateSeverityCritical = null;
	/** Severity（重要度：不明） コンボボックス。 */
	private Combo m_comboLogEscalateSeverityUnknown = null;

	/** メッセージ（重要度：通知） テキスト。 */
	private TextWithParameterComposite m_textLogEscalateMessageInfo = null;
	/** メッセージ（重要度：警告） テキスト。 */
	private TextWithParameterComposite m_textLogEscalateMessageWarning = null;
	/** メッセージ（重要度：危険） テキスト。 */
	private TextWithParameterComposite m_textLogEscalateMessageCritical = null;
	/** メッセージ（重要度：不明） テキスト。 */
	private TextWithParameterComposite m_textLogEscalateMessageUnknown = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public NotifyLogEscalateCreateDialog(Shell parent) {
		super(parent);
		parentDialog = this;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param managerName マネージャ名
	 * @param parent 親のシェルオブジェクト
	 * @param notifyId 変更する通知情報の通知ID
	 * @param updateFlg 更新フラグ（true:更新する）
	 */
	public NotifyLogEscalateCreateDialog(Shell parent, String managerName, String notifyId, boolean updateFlg) {
		super(parent, managerName, notifyId, updateFlg);
		parentDialog = this;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 * @see com.clustercontrol.notify.action.GetNotify#getNotify(String)
	 * @see #setInputData(NotifyInfoInputData)
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// 通知IDが指定されている場合、その情報を初期表示する。
		NotifyInfoInputData inputData;
		if(this.notifyId != null){
			inputData = new GetNotify().getLogEscalateNotify(this.managerName, this.notifyId);
		} else {
			inputData = new NotifyInfoInputData();
		}
		this.setInputData(inputData);

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
		shell.setText(Messages.getString("dialog.notify.log.escalate.create.modify"));

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
		 * ログエスカレーション
		 */

		// ログエスカレーショングループ
		Group groupLogEscalate = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "logescalate", groupLogEscalate);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupLogEscalate.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupLogEscalate.setLayoutData(gridData);
		groupLogEscalate.setText(Messages.getString("notifies.log.escalate") + " : ");

		/*
		 * スコープグループ
		 */
		Group groupScope = new Group(groupLogEscalate, SWT.NONE);
		WidgetTestUtil.setTestId(this, "scope", groupScope);
		groupScope.setText(Messages.getString("notify.log.escalate.scope"));
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupScope.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupScope.setLayoutData(gridData);

		// イベント発生ノード ラジオボタン
		this.m_radioGenerationNodeValue = new Button(groupScope, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "generationnode", m_radioGenerationNodeValue);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioGenerationNodeValue.setLayoutData(gridData);
		this.m_radioGenerationNodeValue.setText(Messages.getString("notify.node.generation") + " : ");
		this.m_radioGenerationNodeValue.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_radioFixedValue.setSelection(false);
					m_scopeSelect.setEnabled(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

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
		this.m_radioFixedValue.addSelectionListener(new SelectionListener() {
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

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

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
		this.m_textScope.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		this.m_scopeSelect = new Button(groupScope, SWT.NONE);
		WidgetTestUtil.setTestId(this, "scope", m_scopeSelect);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_scopeSelect.setLayoutData(gridData);
		this.m_scopeSelect.setText(Messages.getString("refer"));
		this.m_scopeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell, managerName, ownerRoleId);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse selectItem = dialog.getSelectItem();
					FacilityInfoResponse info = selectItem.getData();
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
		// 空白
		label = new Label(groupScope, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// エスカレート先ポート
		label = new Label(groupScope, SWT.NONE);
		WidgetTestUtil.setTestId(this, "portnumber", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("port.number") + " : ");

		this.m_textEscalatePort = new Text(groupScope, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "escalateport", m_textEscalatePort);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textEscalatePort.setLayoutData(gridData);
		this.m_textEscalatePort.setText("514");
		this.m_textEscalatePort.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupScope, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 空行
		label = new Label(groupLogEscalate, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space3", label);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 重要度 ごとの設定
		 */
		// ラベル（重要度）
		label = new Label(groupLogEscalate, SWT.NONE);
		WidgetTestUtil.setTestId(this, "priority", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// ラベル（実行する）
		label = new Label(groupLogEscalate, SWT.NONE);
		WidgetTestUtil.setTestId(this, "attiribute", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.attribute"));

		// ラベル（Facility）
		label = new Label(groupLogEscalate, SWT.NONE);
		WidgetTestUtil.setTestId(this, "facility", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMBO_FACILITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText("Syslog Facility");

		// ラベル（Severity）
		label = new Label(groupLogEscalate, SWT.NONE);
		WidgetTestUtil.setTestId(this, "severity", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMBO_SEVERITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText("Syslog Severity");

		// ラベル（メッセージ）
		label = new Label(groupLogEscalate, SWT.NONE);
		WidgetTestUtil.setTestId(this, "message", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_MESSAGE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("message"));

		//　ログエスカレーション　重要度：通知
		label = this.getLabelPriority(groupLogEscalate, Messages.getString("info"),PriorityColorConstant.COLOR_INFO);
		this.m_checkLogEscalateRunInfo = this.getCheckLogEscalateRun(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "escalaterun", m_checkLogEscalateRunInfo);
		this.m_comboLogEscalateFacilityInfo = this.getComboLogEscalateFacility(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "escalatefacility", m_comboLogEscalateFacilityInfo);
		this.m_comboLogEscalateSeverityInfo = this.getComboLogEscalateSeverity(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "escalateseverity", m_comboLogEscalateSeverityInfo);
		this.m_textLogEscalateMessageInfo = this.getStyledTextMessage(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "escalatemessage", m_textLogEscalateMessageInfo);
		this.m_checkLogEscalateRunInfo
		.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkLogEscalateRunInfo.getSelection(),
						m_comboLogEscalateFacilityInfo,
						m_comboLogEscalateSeverityInfo,
						m_textLogEscalateMessageInfo);
				update();
			}
		});
		setEnabled(false,
				m_comboLogEscalateFacilityInfo,
				m_comboLogEscalateSeverityInfo,
				m_textLogEscalateMessageInfo);

		//　ログエスカレーション　重要度：警告
		label = this.getLabelPriority(groupLogEscalate, Messages.getString("warning"),PriorityColorConstant.COLOR_WARNING);
		this.m_checkLogEscalateRunWarning = this.getCheckLogEscalateRun(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "runwarning", m_checkLogEscalateRunWarning);
		this.m_comboLogEscalateFacilityWarning = this.getComboLogEscalateFacility(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "facilitywarning", m_comboLogEscalateFacilityWarning);
		this.m_comboLogEscalateSeverityWarning = this.getComboLogEscalateSeverity(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "serveritywarning", m_comboLogEscalateSeverityWarning);
		this.m_textLogEscalateMessageWarning = this.getStyledTextMessage(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "messagewarning", m_textLogEscalateMessageWarning);
		this.m_checkLogEscalateRunWarning
		.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkLogEscalateRunWarning.getSelection(),
						m_comboLogEscalateFacilityWarning,
						m_comboLogEscalateSeverityWarning,
						m_textLogEscalateMessageWarning);
				update();
			}
		});
		setEnabled(false,
				m_comboLogEscalateFacilityWarning,
				m_comboLogEscalateSeverityWarning,
				m_textLogEscalateMessageWarning);

		//　ログエスカレーション　重要度：危険
		label = this.getLabelPriority(groupLogEscalate, Messages.getString("critical"),PriorityColorConstant.COLOR_CRITICAL);
		this.m_checkLogEscalateRunCritical = this.getCheckLogEscalateRun(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "runcritical", m_checkLogEscalateRunCritical);
		this.m_comboLogEscalateFacilityCritical = this.getComboLogEscalateFacility(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "facilitycritical", m_comboLogEscalateFacilityCritical);
		this.m_comboLogEscalateSeverityCritical = this.getComboLogEscalateSeverity(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "severritycritical", m_comboLogEscalateSeverityCritical);
		this.m_textLogEscalateMessageCritical = this.getStyledTextMessage(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "messagecritical", m_textLogEscalateMessageCritical);
		this.m_checkLogEscalateRunCritical
		.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkLogEscalateRunCritical.getSelection(),
						m_comboLogEscalateFacilityCritical,
						m_comboLogEscalateSeverityCritical,
						m_textLogEscalateMessageCritical);
				update();
			}
		});
		setEnabled(false,
				m_comboLogEscalateFacilityCritical,
				m_comboLogEscalateSeverityCritical,
				m_textLogEscalateMessageCritical);


		//　ログエスカレーション　重要度：不明
		label = this.getLabelPriority(groupLogEscalate, Messages.getString("unknown"),PriorityColorConstant.COLOR_UNKNOWN);
		this.m_checkLogEscalateRunUnknown = this.getCheckLogEscalateRun(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "rununknown", m_checkLogEscalateRunUnknown);
		this.m_comboLogEscalateFacilityUnknown = this.getComboLogEscalateFacility(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "facilityunknown", m_comboLogEscalateFacilityUnknown);
		this.m_comboLogEscalateSeverityUnknown = this.getComboLogEscalateSeverity(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "severityunknown", m_comboLogEscalateSeverityUnknown);
		this.m_textLogEscalateMessageUnknown = this.getStyledTextMessage(groupLogEscalate);
		WidgetTestUtil.setTestId(this, "messageunknown", m_textLogEscalateMessageUnknown);
		this.m_checkLogEscalateRunUnknown
		.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkLogEscalateRunUnknown.getSelection(),
						m_comboLogEscalateFacilityUnknown,
						m_comboLogEscalateSeverityUnknown,
						m_textLogEscalateMessageUnknown);
				update();
			}
		});
		setEnabled(false,
				m_comboLogEscalateFacilityUnknown,
				m_comboLogEscalateSeverityUnknown,
				m_textLogEscalateMessageUnknown);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示

		// 固定スコープ
		if(this.m_radioFixedValue.getSelection() && "".equals(this.m_textScope.getText())){
			this.m_textScope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// ポート番号
		if("".equals(this.m_textEscalatePort.getText())){
			this.m_textEscalatePort.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textEscalatePort.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_checkLogEscalateRunInfo.getSelection()) {
			if("".equals(this.m_textLogEscalateMessageInfo.getText())) {
				this.m_textLogEscalateMessageInfo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_textLogEscalateMessageInfo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}
		if(this.m_checkLogEscalateRunWarning.getSelection()) {
			if("".equals(this.m_textLogEscalateMessageWarning.getText())) {
				this.m_textLogEscalateMessageWarning.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_textLogEscalateMessageWarning.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}
		if(this.m_checkLogEscalateRunCritical.getSelection()) {
			if("".equals(this.m_textLogEscalateMessageCritical.getText())) {
				this.m_textLogEscalateMessageCritical.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_textLogEscalateMessageCritical.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}
		if (this.m_checkLogEscalateRunUnknown.getSelection()) {
			if("".equals(this.m_textLogEscalateMessageUnknown.getText())) {
				this.m_textLogEscalateMessageUnknown.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				this.m_textLogEscalateMessageUnknown.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}
	}

	/**
	 * 入力値を保持した通知情報を返します。
	 *
	 * @return 通知情報
	 */
	@Override
	public NotifyInfoInputData getInputData() {
		return this.inputData;
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param notify 設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(NotifyInfoInputData notify) {
		super.setInputData(notify);

		// コマンド情報
		LogEscalateNotifyDetailInfoResponse info = notify.getNotifyLogEscalateInfo();
		if (info != null) {
			this.setInputDatal(info);
		} else {
			// 新規追加の場合
			this.m_radioGenerationNodeValue.setSelection(true);
			this.m_scopeSelect.setEnabled(false);
		}
	}

	private void setInputDatal(LogEscalateNotifyDetailInfoResponse log) {
		if (log.getEscalateFacilityId() != null) {
			this.m_facilityId = log.getEscalateFacilityId();
			this.m_textScope.setText(HinemosMessage.replace(log.getEscalateScope()));
		}
		if (log.getEscalatePort() != null) {
			this.m_textEscalatePort.setText(log.getEscalatePort().toString());
		}
		if (log.getEscalateFacilityFlg() != null && log.getEscalateFacilityFlg() == EscalateFacilityFlgEnum.GENERATION) {
			this.m_radioGenerationNodeValue.setSelection(true);
			this.m_scopeSelect.setEnabled(false);
		}
		else {
			this.m_radioFixedValue.setSelection(true);
			this.m_scopeSelect.setEnabled(true);
		}

		Button[] checkLogEscalateRuns = new Button[] {
				this.m_checkLogEscalateRunInfo,
				this.m_checkLogEscalateRunWarning,
				this.m_checkLogEscalateRunCritical,
				this.m_checkLogEscalateRunUnknown
		};
		Combo[] comboLogEscalateFacilities = new Combo[] {
				this.m_comboLogEscalateFacilityInfo,
				this.m_comboLogEscalateFacilityWarning,
				this.m_comboLogEscalateFacilityCritical,
				this.m_comboLogEscalateFacilityUnknown
		};
		Combo[] comboLogEscalateServerities = new Combo[] {
				this.m_comboLogEscalateSeverityInfo,
				this.m_comboLogEscalateSeverityWarning,
				this.m_comboLogEscalateSeverityCritical,
				this.m_comboLogEscalateSeverityUnknown
		};
		TextWithParameterComposite[] textLogEscalateMessages = new TextWithParameterComposite[] {
				this.m_textLogEscalateMessageInfo,
				this.m_textLogEscalateMessageWarning,
				this.m_textLogEscalateMessageCritical,
				this.m_textLogEscalateMessageUnknown
		};
		String[] syslogFacilities = new String[] {
				SyslogFacilityUtil.enumToString(log.getInfoSyslogFacility(), InfoSyslogFacilityEnum.class),
				SyslogFacilityUtil.enumToString(log.getWarnSyslogFacility(), WarnSyslogFacilityEnum.class),
				SyslogFacilityUtil.enumToString(log.getCriticalSyslogFacility(), CriticalSyslogFacilityEnum.class),
				SyslogFacilityUtil.enumToString(log.getUnknownSyslogFacility(), UnknownSyslogFacilityEnum.class)
		};
		String[] syslogPriorities = new String[] {
				SyslogSeverityUtil.enumToString(log.getInfoSyslogPriority(), InfoSyslogPriorityEnum.class),
				SyslogSeverityUtil.enumToString(log.getWarnSyslogPriority(), WarnSyslogPriorityEnum.class),
				SyslogSeverityUtil.enumToString(log.getCriticalSyslogPriority(), CriticalSyslogPriorityEnum.class),
				SyslogSeverityUtil.enumToString(log.getUnknownSyslogPriority(), UnknownSyslogPriorityEnum.class)
		};
		String[] escalateMessages = new String[] {
				log.getInfoEscalateMessage(),
				log.getWarnEscalateMessage(),
				log.getCriticalEscalateMessage(),
				log.getUnknownEscalateMessage()
		};

		Boolean[] validFlgs = this.getValidFlgs(log);
		for (int i = 0; i < validFlgs.length; i++) {
			// 通知
			boolean valid = validFlgs[i].booleanValue();
			checkLogEscalateRuns[i].setSelection(valid);
			WidgetTestUtil.setTestId(this, "checkLogEscalateRuns" + i, checkLogEscalateRuns[i]);
			comboLogEscalateFacilities[i].setEnabled(valid);
			WidgetTestUtil.setTestId(this, "comboLogEscalateFacilities" + i, comboLogEscalateFacilities[i]);
			comboLogEscalateServerities[i].setEnabled(valid);
			WidgetTestUtil.setTestId(this, "comboLogEscalateServerities" + i, comboLogEscalateServerities[i]);
			textLogEscalateMessages[i].setEnabled(valid);
			WidgetTestUtil.setTestId(this, "textLogEscalateMessages" + i, textLogEscalateMessages[i]);

			// ファシリティ
			if (syslogFacilities[i] != null) {
				comboLogEscalateFacilities[i].setText(syslogFacilities[i]);
			}
			// プライオリティ
			if (syslogPriorities[i] != null) {
				comboLogEscalateServerities[i].setText(syslogPriorities[i]);
			}
			// ジョブID
			if (escalateMessages[i] != null) {
				textLogEscalateMessages[i].setText(escalateMessages[i]);
			}
		}
	}

	/**
	 * 入力値を設定した通知情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知情報
	 *
	 * @see #createInputDataForLogEscalate(ArrayList, int, Button, Combo, Button, Combo, Button, Text)
	 */
	@Override
	protected NotifyInfoInputData createInputData() {
		NotifyInfoInputData info = super.createInputData();

		// 通知タイプの設定
		info.setNotifyType(TYPE_LOG_ESCALATE);

		// 通知タイプの設定
		LogEscalateNotifyDetailInfoResponse log = createNotifyInfoDetail();
		info.setNotifyLogEscalateInfo(log);

		return info;
	}

	private LogEscalateNotifyDetailInfoResponse createNotifyInfoDetail() {
		LogEscalateNotifyDetailInfoResponse log = new LogEscalateNotifyDetailInfoResponse();

		// 共通部分登録
		// 実行ファシリティID
		if (this.m_textScope.getText() != null
				&& !"".equals(this.m_textScope.getText())) {
			log.setEscalateFacilityId(this.m_facilityId);
			log.setEscalateScope(this.m_textScope.getText());
		}
		// 実行ファシリティ
		if (this.m_radioGenerationNodeValue.getSelection()) {
			log.setEscalateFacilityFlg(EscalateFacilityFlgEnum.GENERATION);
		}
		else if (this.m_radioFixedValue.getSelection()){
			log.setEscalateFacilityFlg(EscalateFacilityFlgEnum.FIX);
		}
		// ポート番号
		if (this.m_textEscalatePort.getText() != null
				&& !"".equals(this.m_textEscalatePort.getText())) {
			try {
				log.setEscalatePort(Integer.parseInt(this.m_textEscalatePort.getText()));
			} catch(NumberFormatException e) {
				log.setEscalatePort(null);  // 数値以外が入力された場合（マネージャ側でバリデーション）
			}
		}

		// 実行
		log.setInfoValidFlg(m_checkLogEscalateRunInfo.getSelection());
		log.setWarnValidFlg(m_checkLogEscalateRunWarning.getSelection());
		log.setCriticalValidFlg(m_checkLogEscalateRunCritical.getSelection());
		log.setUnknownValidFlg(m_checkLogEscalateRunUnknown.getSelection());

		// ファシリティ
		if (isNotNullAndBlank(m_comboLogEscalateFacilityInfo.getText())) {
			log.setInfoSyslogFacility(SyslogFacilityUtil.stringToEnum(m_comboLogEscalateFacilityInfo.getText(), InfoSyslogFacilityEnum.class));
		}
		if (isNotNullAndBlank(m_comboLogEscalateFacilityWarning.getText())) {
			log.setWarnSyslogFacility(SyslogFacilityUtil.stringToEnum(m_comboLogEscalateFacilityWarning.getText(), WarnSyslogFacilityEnum.class));
		}
		if (isNotNullAndBlank(m_comboLogEscalateFacilityCritical.getText())) {
			log.setCriticalSyslogFacility(SyslogFacilityUtil.stringToEnum(m_comboLogEscalateFacilityCritical.getText(), CriticalSyslogFacilityEnum.class));
		}
		if (isNotNullAndBlank(m_comboLogEscalateFacilityUnknown.getText())) {
			log.setUnknownSyslogFacility(SyslogFacilityUtil.stringToEnum(m_comboLogEscalateFacilityUnknown.getText(), UnknownSyslogFacilityEnum.class));
		}

		// プライオリティ
		if (isNotNullAndBlank(m_comboLogEscalateSeverityInfo.getText())) {
			log.setInfoSyslogPriority(SyslogSeverityUtil.stringToEnum(m_comboLogEscalateSeverityInfo.getText(), InfoSyslogPriorityEnum.class));
		}
		if (isNotNullAndBlank(m_comboLogEscalateSeverityWarning.getText())) {
			log.setWarnSyslogPriority(SyslogSeverityUtil.stringToEnum(m_comboLogEscalateSeverityWarning.getText(), WarnSyslogPriorityEnum.class));
		}
		if (isNotNullAndBlank(m_comboLogEscalateSeverityCritical.getText())) {
			log.setCriticalSyslogPriority(SyslogSeverityUtil.stringToEnum(m_comboLogEscalateSeverityCritical.getText(), CriticalSyslogPriorityEnum.class));
		}
		if (isNotNullAndBlank(m_comboLogEscalateSeverityUnknown.getText())) {
			log.setUnknownSyslogPriority(SyslogSeverityUtil.stringToEnum(m_comboLogEscalateSeverityUnknown.getText(), UnknownSyslogPriorityEnum.class));
		}

		// メッセージ
		if (isNotNullAndBlank(m_textLogEscalateMessageInfo.getText())) {
			log.setInfoEscalateMessage(m_textLogEscalateMessageInfo.getText());
		}
		if (isNotNullAndBlank(m_textLogEscalateMessageWarning.getText())) {
			log.setWarnEscalateMessage(m_textLogEscalateMessageWarning.getText());
		}
		if (isNotNullAndBlank(m_textLogEscalateMessageCritical.getText())) {
			log.setCriticalEscalateMessage(m_textLogEscalateMessageCritical.getText());
		}
		if (isNotNullAndBlank(m_textLogEscalateMessageUnknown.getText())) {
			log.setUnknownEscalateMessage(m_textLogEscalateMessageUnknown.getText());
		}

		return log;
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

		NotifyInfoInputData info = this.getInputData();
		if(info != null){
			if (!this.updateFlg) {
				// 作成の場合
				result = new AddNotify().addLogEscalateNotify(managerName, info);
			}
			else{
				// 変更の場合
				result = new ModifyNotify().modifyLogEscalateNotify(managerName, info);
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
	 * 引数で指定されたコンポジットの選択可/不可を設定します。
	 *
	 * @param enabled
	 *            選択可の場合、</code> true </code>
	 * @param comboLogEscalateFacility
	 *            設定対象のファシリティコンボボックス
	 * @param comboLogEscalatePriority
	 *            設定対象のプライオリティコンボボックス
	 * @param textLogEscalateMessage
	 *            設定対象のメッセージ
	 */
	private void setEnabled(boolean enabled,
			Combo comboLogEscalateFacility,
			Combo comboLogEscalatePriority,
			TextWithParameterComposite textLogEscalateMessage) {
		comboLogEscalateFacility.setEnabled(enabled);
		comboLogEscalatePriority.setEnabled(enabled);
		textLogEscalateMessage.setEnabled(enabled);
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
	 * ログエスカレーションの実行チェックボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Button getCheckLogEscalateRun(Composite parent) {
		// チェックボックス（実行）
		Button button = new Button(parent, SWT.CHECK);
		WidgetTestUtil.setTestId(this, null, button);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		return button;
	}

	/**
	 * ログエスカレーションのFacilityコンボボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたコンボボックス
	 */
	private Combo getComboLogEscalateFacility(Composite parent) {
		int blank = 0;

		// コンボボックス（通知状態）
		Combo notifyLogEscalateCreateFacilityStatusCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "facilitystatus", notifyLogEscalateCreateFacilityStatusCombo);

		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMBO_FACILITY - blank;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		notifyLogEscalateCreateFacilityStatusCombo.setLayoutData(gridData);

		// Facilityをコンボボックスに登録する
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_AUTH);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_AUTHPRIV);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_CRON);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_DAEMON);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_FTP);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_KERN);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_LPR);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_MAIL);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_NEWS);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_SYSLOG);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_USER);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_UUCP);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_LOCAL0);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_LOCAL1);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_LOCAL2);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_LOCAL3);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_LOCAL4);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_LOCAL5);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_LOCAL6);
		notifyLogEscalateCreateFacilityStatusCombo.add(SyslogFacilityConstant.STRING_LOCAL7);

		notifyLogEscalateCreateFacilityStatusCombo.setText(SyslogFacilityConstant.STRING_USER);

		// 空白
		if( blank > 0){
			Label label = new Label(parent, SWT.NONE);
			WidgetTestUtil.setTestId(this, "blank1", label);
			gridData = new GridData();
			gridData.horizontalSpan = blank;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			label.setLayoutData(gridData);
		}

		return notifyLogEscalateCreateFacilityStatusCombo;
	}

	/**
	 * ログエスカレーションのPriorityコンボボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたコンボボックス
	 */
	private Combo getComboLogEscalateSeverity(Composite parent) {
		int blank = 0;

		// コンボボックス（通知状態）
		Combo notifyLogEscalateCreateSeverityStatusCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "status", notifyLogEscalateCreateSeverityStatusCombo);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMBO_SEVERITY - blank;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		notifyLogEscalateCreateSeverityStatusCombo.setLayoutData(gridData);

		// Priorityをコンボボックスに登録する
		notifyLogEscalateCreateSeverityStatusCombo.add(SyslogSeverityConstant.STRING_EMERG);
		notifyLogEscalateCreateSeverityStatusCombo.add(SyslogSeverityConstant.STRING_ALERT);
		notifyLogEscalateCreateSeverityStatusCombo.add(SyslogSeverityConstant.STRING_CRIT);
		notifyLogEscalateCreateSeverityStatusCombo.add(SyslogSeverityConstant.STRING_ERR);
		notifyLogEscalateCreateSeverityStatusCombo.add(SyslogSeverityConstant.STRING_WARNING);
		notifyLogEscalateCreateSeverityStatusCombo.add(SyslogSeverityConstant.STRING_NOTICE);
		notifyLogEscalateCreateSeverityStatusCombo.add(SyslogSeverityConstant.STRING_INFO);
		notifyLogEscalateCreateSeverityStatusCombo.add(SyslogSeverityConstant.STRING_DEBUG);

		notifyLogEscalateCreateSeverityStatusCombo.setText(SyslogSeverityConstant.STRING_ERR);
		// 空白
		if( blank > 0){
			Label label = new Label(parent, SWT.NONE);
			WidgetTestUtil.setTestId(this, "blank2", label);
			gridData = new GridData();
			gridData.horizontalSpan = blank;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			label.setLayoutData(gridData);
		}

		return notifyLogEscalateCreateSeverityStatusCombo;
	}

	/**
	 * ログエスカレーションのメッセージテキストボックスを返します。
	 *
	 * @param parent 親のインスタンス
	 * @return 生成されたテキストボックス
	 */
	private TextWithParameterComposite getStyledTextMessage(Composite parent/*, VerifyListener listener*/) {
		// テキスト（送信先（セミコロン区切り））
		TextWithParameterComposite text = new TextWithParameterComposite(parent, SWT.BORDER | SWT.LEFT);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_MESSAGE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;

		String tooltipText = Messages.getString("notify.parameter.tooltip") + Messages.getString("replace.parameter.notify") + Messages.getString("replace.parameter.node");
		text.setToolTipText(tooltipText);
		text.setLayoutData(gridData);
		text.setColor(new Color(parent.getDisplay(), new RGB(0, 0, 255)));
		text.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		return text;
	}

	private Boolean[] getValidFlgs(LogEscalateNotifyDetailInfoResponse info) {
		Boolean[] validFlgs = new Boolean[] {
				info.getInfoValidFlg(),
				info.getWarnValidFlg(),
				info.getCriticalValidFlg(),
				info.getUnknownValidFlg()
		};
		return validFlgs;
	}

	@Override
	public void updateManagerName(String managerName) {
		super.updateManagerName(managerName);
	}

	@Override
	public void updateOwnerRole(String ownerRoleId) {
		super.updateOwnerRole(ownerRoleId);
		this.m_facilityPath = "";
		this.m_facilityId = "";
		this.m_textScope.setText(HinemosMessage.replace(m_facilityPath));
		update();
	}
}
