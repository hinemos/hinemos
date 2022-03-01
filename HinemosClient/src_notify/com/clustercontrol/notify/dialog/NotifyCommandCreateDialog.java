/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.openapitools.client.model.CommandNotifyDetailInfoResponse;
import org.openapitools.client.model.CommandTemplateResponse;

import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.TextWithParameterComposite;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.action.AddNotify;
import com.clustercontrol.notify.action.GetCommandTemplate;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.ModifyNotify;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 通知（コマンド）作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 3.0.0
 */
public class NotifyCommandCreateDialog extends NotifyBasicCreateDialog {
	// これらのカラム数の合計は、15でないといけない。

	/** カラム数（重要度）。 */
	private static final int WIDTH_PRIORITY = 2;

	/** カラム数（チェックボックス）。 */
	private static final int WIDTH_CHECK = 2;

	/** カラム数（ユーザ入力欄）。 */
	private static final int WIDTH_COMMAND_USER = 2;

	/** カラム数（コマンド入力欄）。 */
	private static final int WIDTH_COMMAND_TEXT = 9;
	
	/** カラム数（タイムアウト入力欄）。 */
	private static final int WIDTH_TIME_OUT = 2;

	// ----- instance フィールド ----- //

	/**
	 * 通知タイプ
	 *
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	private static final int TYPE_COMMAND = 5;
	
	/** command用タイムアウト時間（ミリ秒）*/
	public static final int TIMEOUT_SEC_COMMAND = 15000;

	/** 入力値の正当性を保持するオブジェクト。 */
	protected ValidateResult validateResult = null;

	/** 実行（重要度：通知） チェックボックス。 */
	private Button m_checkExecInfo = null;
	/** 実行（重要度：警告） チェックボックス。 */
	private Button m_checkExecWarning = null;
	/** 実行（重要度：危険） チェックボックス。 */
	private Button m_checkExecCritical = null;
	/** 実行（重要度：不明） チェックボックス。 */
	private Button m_checkExecUnknown = null;

	/** 実効ユーザ（重要度：通知） テキスト。 */
	private Text m_textUserInfo = null;
	/** 実効ユーザ（重要度：警告） テキスト。 */
	private Text m_textUserWarning = null;
	/** 実効ユーザ（重要度：危険） テキスト。 */
	private Text m_textUserCritical = null;
	/** 実効ユーザ（重要度：不明） テキスト。 */
	private Text m_textUserUnknown = null;

	/** 実行コマンド（重要度：通知） テキスト。 */
	private TextWithParameterComposite m_textCommandInfo = null;
	/** 実行コマンド（重要度：警告） テキスト。 */
	private TextWithParameterComposite m_textCommandWarning = null;
	/** 実行コマンド（重要度：危険） テキスト。 */
	private TextWithParameterComposite m_textCommandCritical = null;
	/** 実行コマンド（重要度：不明） テキスト。 */
	private TextWithParameterComposite m_textCommandUnknown = null;

	/** 実行コマンド（重要度：通知）*/
	private Button m_buttonDirect = null;
	/** 実行コマンド（重要度：通知）*/
	private Button m_buttonChoice = null;
	
	private boolean init = true;
	/** 実行（重要度：通知） チェックボックス。 */
	private Button m_checkExecInfo_Choice = null;
	/** 実行（重要度：警告） チェックボックス。 */
	private Button m_checkExecWarning_Choice = null;
	/** 実行（重要度：危険） チェックボックス。 */
	private Button m_checkExecCritical_Choice = null;
	/** 実行（重要度：不明） チェックボックス。 */
	private Button m_checkExecUnknown_Choice = null;

	/** 実効ユーザ（重要度：通知） テキスト。 */
	private Text m_textUserInfo_Choice = null;
	/** 実効ユーザ（重要度：警告） テキスト。 */
	private Text m_textUserWarning_Choice = null;
	/** 実効ユーザ（重要度：危険） テキスト。 */
	private Text m_textUserCritical_Choice = null;
	/** 実効ユーザ（重要度：不明） テキスト。 */
	private Text m_textUserUnknown_Choice = null;
	
	/** 実行コマンド（重要度：情報） コンボボックス*/
	private Combo m_comboCommandInfo = null;
	/** 実行コマンド（重要度：警告） コンボボックス */
	private Combo m_comboCommandWarning = null;
	/** 実行コマンド（重要度：危険）  コンボボックス*/
	private Combo m_comboCommandCritical = null;
	/** 実行コマンド（重要度：不明） コンボボックス */
	private Combo m_comboCommandUnknown = null;

	/** タイムアウト テキスト。*/
	private Text m_textTimeout = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public NotifyCommandCreateDialog(Shell parent) {
		super(parent);
		parentDialog = this;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 * @param notifyId
	 *            変更する通知情報の通知ID
	 * @param updateFlg
	 *            更新フラグ（true:更新する）
	 */
	public NotifyCommandCreateDialog(Shell parent, String managerName, String notifyId,
			boolean updateFlg) {
		super(parent, managerName, notifyId, updateFlg);
		parentDialog = this;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
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
			inputData = new GetNotify().getCommandNotify(this.managerName, this.notifyId);
		} else {
			inputData = new NotifyInfoInputData();
		}
		this.setInputData(inputData);

	}

	/**
	 * 親のクラスから呼ばれ、各通知用のダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 */
	@Override
	protected void customizeSettingDialog(Composite parent) {
		final Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.notify.command.create.modify"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		//ownerRoleIDに対して追加リスナー
		if (m_notifyBasic.m_ownerRoleId.getComboRoleId() != null) {
			m_notifyBasic.m_ownerRoleId.getComboRoleId().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					init = true;
					if (m_buttonChoice.getSelection()) {
						updateTemplate();
					}
				}
			});
		}
	
		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		/*
		 * コマンド実行
		 */
		// コマンド設定グループ
		Group groupComamnd = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "command", groupComamnd);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupComamnd.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupComamnd.setLayoutData(gridData);
		groupComamnd.setText(Messages.getString("notifies.command"));

		//コマンド入力グループ
		m_buttonDirect = new Button(groupComamnd, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = GridData.FILL;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_buttonDirect.setLayoutData(gridData);
		m_buttonDirect.setText(Messages.getString("notify.command.type.direct"));
		m_buttonDirect.addSelectionListener(new SelectionAdapter()  {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabledDetails(true);
				update();
			}
		});
		
		Group groupComamndDirect = new Group(groupComamnd, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupComamndDirect.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupComamndDirect.setLayoutData(gridData);

		/*
		 * 重要度 ごとの設定
		 */
		// ラベル（重要度）
		label = new Label(groupComamndDirect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "priority", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// ラベル（実行）
		label = new Label(groupComamndDirect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyattribute", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.attribute"));

		// ラベル（ユーザ入力欄）
		label = new Label(groupComamndDirect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "effectiveuser", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMMAND_USER;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("effective.user"));

		// ラベル（コマンド入力欄）
		label = new Label(groupComamndDirect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "command", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMMAND_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("command"));

		// 重要度：通知
		label = this.getLabelPriority(groupComamndDirect, Messages.getString("info"),
				PriorityColorConstant.COLOR_INFO);
		this.m_checkExecInfo = this.getCheckBox(groupComamndDirect);
		WidgetTestUtil.setTestId(this, "execinfo", m_checkExecInfo);
		this.m_textUserInfo = this.getTextUser(groupComamndDirect);
		WidgetTestUtil.setTestId(this, "userinfo", m_textUserInfo);
		this.m_textCommandInfo = this.getTextCommand(groupComamndDirect);
		WidgetTestUtil.setTestId(this, "commandinfo", m_textCommandInfo);
		this.m_checkExecInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkExecInfo.getSelection(),
						m_textUserInfo, m_textCommandInfo);
				update();
			}
		});
		setEnabled(false, m_textUserInfo, m_textCommandInfo);

		// 重要度：警告
		label = this.getLabelPriority(groupComamndDirect, Messages
				.getString("warning"), PriorityColorConstant.COLOR_WARNING);
		this.m_checkExecWarning = this.getCheckBox(groupComamndDirect);
		WidgetTestUtil.setTestId(this, "execwarning", m_checkExecWarning);
		this.m_textUserWarning = this.getTextUser(groupComamndDirect);
		WidgetTestUtil.setTestId(this, "userwarning", m_textUserWarning);
		this.m_textCommandWarning = this.getTextCommand(groupComamndDirect);
		WidgetTestUtil.setTestId(this, "commandwarning", m_textCommandWarning);
		this.m_checkExecWarning.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkExecWarning.getSelection(),
						m_textUserWarning, m_textCommandWarning);
				update();
			}
		});
		setEnabled(false, m_textUserWarning, m_textCommandWarning);

		// 重要度：危険
		label = this.getLabelPriority(groupComamndDirect, Messages
				.getString("critical"), PriorityColorConstant.COLOR_CRITICAL);
		this.m_checkExecCritical = this.getCheckBox(groupComamndDirect);
		WidgetTestUtil.setTestId(this, "execcritical", m_checkExecCritical);
		this.m_textUserCritical = this.getTextUser(groupComamndDirect);
		WidgetTestUtil.setTestId(this, "usercritical", m_textUserCritical);
		this.m_textCommandCritical = this.getTextCommand(groupComamndDirect);
		WidgetTestUtil.setTestId(this, "commandcritical", m_textCommandCritical);
		this.m_checkExecCritical
		.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkExecCritical.getSelection(),
						m_textUserCritical, m_textCommandCritical);
				update();
			}
		});
		setEnabled(false, m_textUserCritical, m_textCommandCritical);

		// 重要度：不明
		label = this.getLabelPriority(groupComamndDirect, Messages
				.getString("unknown"), PriorityColorConstant.COLOR_UNKNOWN);
		this.m_checkExecUnknown = this.getCheckBox(groupComamndDirect);
		WidgetTestUtil.setTestId(this, "execunknown", m_checkExecUnknown);
		this.m_textUserUnknown = this.getTextUser(groupComamndDirect);
		WidgetTestUtil.setTestId(this, "userunknown", m_textUserUnknown);
		this.m_textCommandUnknown = this.getTextCommand(groupComamndDirect);
		WidgetTestUtil.setTestId(this, "commandunknown", m_textCommandUnknown);
		this.m_checkExecUnknown
		.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkExecUnknown.getSelection(),
						m_textUserUnknown, m_textCommandUnknown);
				update();
			}
		});
		setEnabled(false, m_textUserUnknown, m_textCommandUnknown);
		

		//コマンドテンプレート選択グループ
		m_buttonChoice = new Button(groupComamnd, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = GridData.FILL;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_buttonChoice.setLayoutData(gridData);
		m_buttonChoice.setText(Messages.getString("notify.command.type.choice"));
		m_buttonChoice.addSelectionListener(new SelectionAdapter()  {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (init) {
					updateTemplate();
				}
				setEnabledDetails(true);
				update();
			}
		});
		
		Group groupComamndChoice = new Group(groupComamnd, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupComamndChoice.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupComamndChoice.setLayoutData(gridData);

		/*
		 * 重要度 ごとの設定
		 */
		// ラベル（重要度）
		label = new Label(groupComamndChoice, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// ラベル（実行）
		label = new Label(groupComamndChoice, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.attribute"));

		// ラベル（ユーザ入力欄）
		label = new Label(groupComamndChoice, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMMAND_USER;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("effective.user"));

		// ラベル（コマンド通知テンプレート選択欄）
		label = new Label(groupComamndChoice, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMMAND_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("command.template.id"));

		// 重要度：情報
		label = this.getLabelPriority(groupComamndChoice, Messages.getString("info"),
				PriorityColorConstant.COLOR_INFO);
		this.m_checkExecInfo_Choice = this.getCheckBox(groupComamndChoice);
		this.m_textUserInfo_Choice = this.getTextUser(groupComamndChoice);
		this.m_comboCommandInfo = this.getComboCommand(groupComamndChoice);
		this.m_checkExecInfo_Choice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkExecInfo_Choice.getSelection(),
						m_textUserInfo_Choice, m_comboCommandInfo);
				update();
			}
		});

		// 重要度：警告
		label = this.getLabelPriority(groupComamndChoice, Messages
				.getString("warning"), PriorityColorConstant.COLOR_WARNING);
		this.m_checkExecWarning_Choice = this.getCheckBox(groupComamndChoice);
		this.m_textUserWarning_Choice = this.getTextUser(groupComamndChoice);
		this.m_comboCommandWarning = this.getComboCommand(groupComamndChoice);
		this.m_checkExecWarning_Choice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkExecWarning_Choice.getSelection(),
						m_textUserWarning_Choice, m_comboCommandWarning);
				update();
			}
		});

		// 重要度：危険
		label = this.getLabelPriority(groupComamndChoice, Messages
				.getString("critical"), PriorityColorConstant.COLOR_CRITICAL);
		this.m_checkExecCritical_Choice = this.getCheckBox(groupComamndChoice);
		this.m_textUserCritical_Choice = this.getTextUser(groupComamndChoice);
		this.m_comboCommandCritical = this.getComboCommand(groupComamndChoice);
		this.m_checkExecCritical_Choice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkExecCritical_Choice.getSelection(),
						m_textUserCritical_Choice, m_comboCommandCritical);
				update();
			}
		});

		// 重要度：不明
		label = this.getLabelPriority(groupComamndChoice, Messages
				.getString("unknown"), PriorityColorConstant.COLOR_UNKNOWN);
		this.m_checkExecUnknown_Choice = this.getCheckBox(groupComamndChoice);
		this.m_textUserUnknown_Choice = this.getTextUser(groupComamndChoice);
		this.m_comboCommandUnknown = this.getComboCommand(groupComamndChoice);
		this.m_checkExecUnknown_Choice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkExecUnknown_Choice.getSelection(),
						m_textUserUnknown_Choice, m_comboCommandUnknown);
				update();
			}
		});

		/*
		 * タイムアウト
		 */
		// ラベル
		label = new Label(groupComamnd, SWT.NONE);
		WidgetTestUtil.setTestId(this, "timeout", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TIME_OUT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("time.out") + " : ");

		// テキスト
		this.m_textTimeout = new Text(groupComamnd, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "timeout", m_textTimeout);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TIME_OUT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textTimeout.setLayoutData(gridData);
		this.m_textTimeout.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		// ラベル（単位）
		label = new Label(groupComamnd, SWT.NONE);
		WidgetTestUtil.setTestId(this, "millisec", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TIME_OUT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("milli.sec"));
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示

		if (m_buttonDirect.getSelection()) {
			// 情報
			if(this.m_checkExecInfo.getSelection()){
				// コマンド
				if("".equals(this.m_textCommandInfo.getText())){
					this.m_textCommandInfo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				}else{
					this.m_textCommandInfo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}
			}else{
				// コマンドの背景色はsetEnabledで別途変更されるため、ここで変える必要はない
				this.m_textUserInfo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}

			// 警告
			if(this.m_checkExecWarning.getSelection()){
				// コマンド
				if("".equals(this.m_textCommandWarning.getText())){
					this.m_textCommandWarning.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				}else{
					this.m_textCommandWarning.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}
			}else{
				this.m_textUserWarning.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}

			// 危険
			if(this.m_checkExecCritical.getSelection()){
				// コマンド
				if("".equals(this.m_textCommandCritical.getText())){
					this.m_textCommandCritical.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				}else{
					this.m_textCommandCritical.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}
			}else{
				this.m_textUserCritical.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}

			// 不明
			if(m_checkExecUnknown.getSelection()){
				// コマンド
				if("".equals(this.m_textCommandUnknown.getText())){
					this.m_textCommandUnknown.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				}else{
					this.m_textCommandUnknown.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}
			}else{
				this.m_textUserUnknown.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		} else {

			// 情報
			if(this.m_checkExecInfo_Choice.getSelection()){
				// コマンド
				if("".equals(this.m_comboCommandInfo.getText())){
					this.m_comboCommandInfo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				}else{
					this.m_comboCommandInfo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}
			}else{
				// コマンドの背景色はsetEnabledで別途変更されるため、ここで変える必要はない
				this.m_textUserInfo_Choice.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				this.m_comboCommandInfo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}

			// 警告
			if(this.m_checkExecWarning_Choice.getSelection()){
				// コマンド
				if("".equals(this.m_comboCommandWarning.getText())){
					this.m_comboCommandWarning.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				}else{
					this.m_comboCommandWarning.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}
			}else{
				this.m_textUserWarning_Choice.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				m_comboCommandWarning.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}

			// 危険
			if(this.m_checkExecCritical_Choice.getSelection()){
				// コマンド
				if("".equals(this.m_comboCommandCritical.getText())){
					this.m_comboCommandCritical.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				}else{
					this.m_comboCommandCritical.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}
			}else{
				this.m_textUserCritical_Choice.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				m_comboCommandCritical.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}

			// 不明
			if(m_checkExecUnknown_Choice.getSelection()){
				// コマンド
				if("".equals(this.m_comboCommandUnknown.getText())){
					this.m_comboCommandUnknown.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				}else{
					this.m_comboCommandUnknown.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}
			}else{
				this.m_textUserUnknown_Choice.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				m_comboCommandUnknown.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}
		
		// タイムアウト
		if("".equals(this.m_textTimeout.getText())){
			this.m_textTimeout.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textTimeout.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
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
	 * @param notify
	 *            設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(NotifyInfoInputData notify) {
		super.setInputData(notify);

		// コマンド情報
		CommandNotifyDetailInfoResponse info = notify.getNotifyCommandInfo();
		if (info != null) {
			this.setInputData(info);
		} else {
			// タイムアウト値（デフォルト）を指定
			this.m_textTimeout.setText(Integer.toString(TIMEOUT_SEC_COMMAND));
			this.m_buttonDirect.setSelection(true);
		}

		// 必須項目を可視化
		this.setEnabledDetails(true);
		this.update();
	}

	/**
	 * 引数で指定された通知ステータス情報の値を、各項目に設定します。
	 *
	 * @param info
	 *            設定値として用いる通知コマンド情報
	 */
	private void setInputData(CommandNotifyDetailInfoResponse info) {
		if (info.getCommandSettingType().equals(CommandNotifyDetailInfoResponse.CommandSettingTypeEnum.CHOICE_TEMPLATE)) {
			m_buttonChoice.setSelection(true);
			updateTemplate();
		} else {
			m_buttonDirect.setSelection(true);
		}
		
		Button[] checkExecs = getCheckExecs();
		Text[] textUsers = getTextUsers();
		TextWithParameterComposite[] textCommands = getTextCommands();
		Combo[] comboTemplates = getComboTemplates();

		String[] effectiveUsers = new String[] {
				info.getInfoEffectiveUser(),
				info.getWarnEffectiveUser(),
				info.getCriticalEffectiveUser(),
				info.getUnknownEffectiveUser()
		};
		String[] commands = new String[] {
				info.getInfoCommand(),
				info.getWarnCommand(),
				info.getCriticalCommand(),
				info.getUnknownCommand()
		};

		Boolean[] validFlgs = getValidFlgs(info);
		for (int i = 0; i < validFlgs.length; i++) {
			boolean valid = validFlgs[i].booleanValue();
			checkExecs[i].setSelection(valid);
			setEnabled(valid, textUsers[i], textCommands[i]);
			WidgetTestUtil.setTestId(this, "textusers" + i, textUsers[i]);
			if (effectiveUsers[i] != null) {
				textUsers[i].setText(effectiveUsers[i]);
			}
			if (commands[i] != null) {
				if (m_buttonDirect.getSelection()) {
					textCommands[i].setText(commands[i]);
				} else {
					comboTemplates[i].setText(commands[i]);
				}
			}
		}
		this.m_textTimeout.setText(Integer.toString(info.getCommandTimeout()));
	}

	private TextWithParameterComposite[] getTextCommands() {
		TextWithParameterComposite[] textCommands = new TextWithParameterComposite[] {
				this.m_textCommandInfo,
				this.m_textCommandWarning,
				this.m_textCommandCritical,
				this.m_textCommandUnknown
		};
		return textCommands;
	}

	private Combo[] getComboTemplates() {
		Combo[] textCommands = new Combo[] {
				this.m_comboCommandInfo,
				this.m_comboCommandWarning,
				this.m_comboCommandCritical,
				this.m_comboCommandUnknown
		};
		return textCommands;
	}

	private Text[] getTextUsers() {
		Text[] textUsers;
		if (m_buttonDirect.getSelection()) {
			textUsers = new Text[] {
				this.m_textUserInfo,
				this.m_textUserWarning,
				this.m_textUserCritical,
				this.m_textUserUnknown
			};
		} else {
			textUsers = new Text[] {
				this.m_textUserInfo_Choice,
				this.m_textUserWarning_Choice,
				this.m_textUserCritical_Choice,
				this.m_textUserUnknown_Choice
			};
		}
		return textUsers;
	}

	private Button[] getCheckExecs() {
		Button[] checkExecs;
		if (m_buttonDirect.getSelection()) {
			checkExecs = new Button[] {
					this.m_checkExecInfo,
					this.m_checkExecWarning,
					this.m_checkExecCritical,
					this.m_checkExecUnknown
			};
		} else {
			checkExecs = new Button[] {
					this.m_checkExecInfo_Choice,
					this.m_checkExecWarning_Choice,
					this.m_checkExecCritical_Choice,
					this.m_checkExecUnknown_Choice
			};
		}
		return checkExecs;
	}

	/**
	 * 入力値を設定した通知情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知情報
	 *
	 * @see #createInputData(ArrayList, int, Button, Text, Button)
	 */
	@Override
	protected NotifyInfoInputData createInputData() {
		NotifyInfoInputData info = super.createInputData();

		// 通知タイプの設定
		info.setNotifyType(TYPE_COMMAND);

		// コマンド情報
		CommandNotifyDetailInfoResponse notifyCommandInfo = this.createNotifyInfoDetail();
		if (notifyCommandInfo != null) {
			info.setNotifyCommandInfo(notifyCommandInfo);
		} else {
			return null;
		}
		
		return info;
	}

	/**
	 * 入力値を設定した通知イベント情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知コマンド情報
	 *
	 */
	private CommandNotifyDetailInfoResponse createNotifyInfoDetail() {
		CommandNotifyDetailInfoResponse command = new CommandNotifyDetailInfoResponse();

		// 実行チェックボックス
		if (m_buttonDirect.getSelection()) {
			command.setCommandSettingType(CommandNotifyDetailInfoResponse.CommandSettingTypeEnum.DIRECT_COMMAND);
			command.setInfoValidFlg(m_checkExecInfo.getSelection());
			command.setWarnValidFlg(m_checkExecWarning.getSelection());
			command.setCriticalValidFlg(m_checkExecCritical.getSelection());
			command.setUnknownValidFlg(m_checkExecUnknown.getSelection());

			// 実効ユーザ
			if (m_textUserInfo.getText() != null) {
				command.setInfoEffectiveUser(m_textUserInfo.getText());
			}
			if (m_textUserWarning.getText() != null) {
				command.setWarnEffectiveUser(m_textUserWarning.getText());
			}
			if (m_textUserCritical.getText() != null) {
				command.setCriticalEffectiveUser(m_textUserCritical.getText());
			}
			if (m_textUserUnknown.getText() != null) {
				command.setUnknownEffectiveUser(m_textUserUnknown.getText());
			}

			// 実行コマンド
			if (isNotNullAndBlank(m_textCommandInfo.getText())) {
				command.setInfoCommand(m_textCommandInfo.getText());
			}
			if (isNotNullAndBlank(m_textCommandWarning.getText())) {
				command.setWarnCommand(m_textCommandWarning.getText());
			}
			if (isNotNullAndBlank(m_textCommandCritical.getText())) {
				command.setCriticalCommand(m_textCommandCritical.getText());
			}
			if (isNotNullAndBlank(m_textCommandUnknown.getText())) {
				command.setUnknownCommand(m_textCommandUnknown.getText());
			}
		} else {
			command.setCommandSettingType(CommandNotifyDetailInfoResponse.CommandSettingTypeEnum.CHOICE_TEMPLATE);
			command.setInfoValidFlg(m_checkExecInfo_Choice.getSelection());
			command.setWarnValidFlg(m_checkExecWarning_Choice.getSelection());
			command.setCriticalValidFlg(m_checkExecCritical_Choice.getSelection());
			command.setUnknownValidFlg(m_checkExecUnknown_Choice.getSelection());

			// 実効ユーザ
			if (m_textUserInfo_Choice.getText() != null) {
				command.setInfoEffectiveUser(m_textUserInfo_Choice.getText());
			}
			if (m_textUserWarning_Choice.getText() != null) {
				command.setWarnEffectiveUser(m_textUserWarning_Choice.getText());
			}
			if (m_textUserCritical_Choice.getText() != null) {
				command.setCriticalEffectiveUser(m_textUserCritical_Choice.getText());
			}
			if (m_textUserUnknown_Choice.getText() != null) {
				command.setUnknownEffectiveUser(m_textUserUnknown_Choice.getText());
			}

			// 実行コマンド
			if (isNotNullAndBlank(m_comboCommandInfo.getText())) {
				command.setInfoCommand(m_comboCommandInfo.getText());
			}
			if (isNotNullAndBlank(m_comboCommandWarning.getText())) {
				command.setWarnCommand(m_comboCommandWarning.getText());
			}
			if (isNotNullAndBlank(m_comboCommandCritical.getText())) {
				command.setCriticalCommand(m_comboCommandCritical.getText());
			}
			if (isNotNullAndBlank(m_comboCommandUnknown.getText())) {
				command.setUnknownCommand(m_comboCommandUnknown.getText());
			}
			
		}

		// タイムアウト値を設定
		try {
			command.setCommandTimeout(Integer.parseInt(m_textTimeout.getText()));
		} catch (NumberFormatException e) {
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.monitor.custom.msg.timeout.invalid"));
			return null;
		}

		return command;
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

		if (this.inputData != null) {
			return super.validate();
		} else {
			return validateResult;
		}
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
		if (info != null) {
			if (!this.updateFlg) {
				// 作成の場合
				result = new AddNotify().addCommandNotify(managerName, info);
			} else {
				// 変更の場合
				result = new ModifyNotify().modifyCommandNotify(managerName, info);
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
	 * コンポジットの選択可/不可を設定します。
	 *
	 * @param enable
	 *            選択可の場合、<code> true </code>
	 */
	@Override
	protected void setEnabled(boolean enable) {

		super.m_notifyBasic.setEnabled(enable);
		super.m_notifyInhibition.setEnabled(enable);

		this.setEnabledDetails(enable);
	}

	/**
	 * コマンド通知の通知関連コンポジットの選択可/不可を設定します。
	 *
	 * @param enable
	 *            選択可の場合、<code> true </code>
	 *
	 * @see #setEnabledDetail(boolean, Button, Text, Button)
	 */
	protected void setEnabledDetails(boolean enable) {

		// 通知関連
		if (m_buttonDirect.getSelection()) {
			this.setEnabledDetail(enable,this.m_checkExecInfo,
					this.m_textUserInfo, this.m_textCommandInfo);
			this.setEnabledDetail(enable, this.m_checkExecWarning,
					this.m_textUserWarning, this.m_textCommandWarning);
			this.setEnabledDetail(enable, this.m_checkExecCritical,
					this.m_textUserCritical, this.m_textCommandCritical);
			this.setEnabledDetail(enable, this.m_checkExecUnknown,
					this.m_textUserUnknown, this.m_textCommandUnknown);
			this.setEnabledDetail(!enable,this.m_checkExecInfo_Choice,
					this.m_textUserInfo_Choice, this.m_comboCommandInfo);
			this.setEnabledDetail(!enable, this.m_checkExecWarning_Choice,
					this.m_textUserWarning_Choice, this.m_comboCommandWarning);
			this.setEnabledDetail(!enable, this.m_checkExecCritical_Choice,
					this.m_textUserCritical_Choice, this.m_comboCommandCritical);
			this.setEnabledDetail(!enable, this.m_checkExecUnknown_Choice,
					this.m_textUserUnknown_Choice, this.m_comboCommandUnknown);
		} else {
			this.setEnabledDetail(!enable,this.m_checkExecInfo,
					this.m_textUserInfo, this.m_textCommandInfo);
			this.setEnabledDetail(!enable, this.m_checkExecWarning,
					this.m_textUserWarning, this.m_textCommandWarning);
			this.setEnabledDetail(!enable, this.m_checkExecCritical,
					this.m_textUserCritical, this.m_textCommandCritical);
			this.setEnabledDetail(!enable, this.m_checkExecUnknown,
					this.m_textUserUnknown, this.m_textCommandUnknown);
			this.setEnabledDetail(enable,this.m_checkExecInfo_Choice,
					this.m_textUserInfo_Choice, this.m_comboCommandInfo);
			this.setEnabledDetail(enable, this.m_checkExecWarning_Choice,
					this.m_textUserWarning_Choice, this.m_comboCommandWarning);
			this.setEnabledDetail(enable, this.m_checkExecCritical_Choice,
					this.m_textUserCritical_Choice, this.m_comboCommandCritical);
			this.setEnabledDetail(enable, this.m_checkExecUnknown_Choice,
					this.m_textUserUnknown_Choice, this.m_comboCommandUnknown);
		}
	}

	/**
	 * コマンド通知のコンポジットの選択可/不可を設定します。
	 *
	 * @param enable
	 *            選択可の場合、<code> true </code>
	 * @param checkBox
	 *            実行チェックボックス
	 * @param textCommand
	 *            実行コマンドテキストボックス
	 * @param checkExecInhibition
	 *            抑制チェックボックス
	 */
	protected void setEnabledDetail(boolean enable, Button checkBox,
			Text textUser, Composite textCommand) {

		if (enable) {
			checkBox.setEnabled(true);

			textUser.setEnabled(checkBox.getSelection());
			textCommand.setEnabled(checkBox.getSelection());

		} else {
			checkBox.setEnabled(false);

			textUser.setEnabled(false);
			textCommand.setEnabled(false);

		}
	}

	/**
	 * 引数で指定されたコンポジットの選択可/不可を設定します。
	 *
	 * @param enabled
	 *            選択可の場合、</code> true </code>
	 * @param textCommand
	 *            設定対象の実行コマンドテキストボックス
	 * @param checkInhibitionFlg
	 *            設定対象の抑制チェックボックス
	 */
	private void setEnabled(boolean enabled, Text textUser, Composite textCommand) {
		textUser.setEnabled(enabled);
		textCommand.setEnabled(enabled);
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id
	 *            ID
	 * @param message
	 *            メッセージ
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
	 * @param parent
	 *            ボタンバーコンポジット
	 *
	 * @see #createButtonsForButtonBar(Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		if (!this.referenceFlg) {
			super.createButtonsForButtonBar(parent);
		} else {
			// 閉じるボタン
			this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), false);
		}
	}

	/**
	 * 重要度のラベルを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param text
	 *            ラベルに表示するテキスト
	 * @param background
	 *            ラベルの背景色
	 * @return 生成されたラベル
	 */
	private Label getLabelPriority(Composite parent, String text,
			Color background) {

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
	 * 実行チェックボックスを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Button getCheckBox(Composite parent) {
		// チェックボックス（実行）
		Button button = new Button(parent, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		return button;
	}

	/**
	 * 実行コマンド入力欄のテキストボックスを返します。 テキストボックスはカスタマイズされたもので、置換文字列は青色で表示されます。
	 *
	 * @param parent
	 *            親のインスタンス
	 * @return 生成されたテキストボックス
	 */
	private Text getTextUser(Composite parent) {
		// テキスト
		Text text = new Text(parent, SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, text);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMMAND_USER;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		text.setToolTipText(Messages.getString("notify.command.user.tooltip"));
		text.setLayoutData(gridData);
		text.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		return text;
	}

	/**
	 * 実行コマンド入力欄のテキストボックスを返します。 テキストボックスはカスタマイズされたもので、置換文字列は青色で表示されます。
	 *
	 * @param parent
	 *            親のインスタンス
	 * @return 生成されたテキストボックス
	 */
	private TextWithParameterComposite getTextCommand(Composite parent) {

		// テキスト
		TextWithParameterComposite notifyCmdCreateTooltipTextWithParamComposite = new TextWithParameterComposite(parent, SWT.BORDER | SWT.SINGLE);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMMAND_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		notifyCmdCreateTooltipTextWithParamComposite.setLayoutData(gridData);


		String tooltipText = Messages.getString("notify.parameter.tooltip") + Messages.getString("replace.parameter.notify") + Messages.getString("replace.parameter.node");
		notifyCmdCreateTooltipTextWithParamComposite.setToolTipText(tooltipText);
		notifyCmdCreateTooltipTextWithParamComposite.setColor(new Color(parent.getDisplay(), new RGB(0, 0, 255)));
		notifyCmdCreateTooltipTextWithParamComposite.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		return notifyCmdCreateTooltipTextWithParamComposite;
	}

	private Combo getComboCommand(Composite parent) {
		// コンボボックス
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMMAND_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		combo.setLayoutData(gridData);
		combo.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		return combo;
	}

	private Boolean[] getValidFlgs(CommandNotifyDetailInfoResponse info) {
		Boolean[] validFlgs = new Boolean[] {
				info.getInfoValidFlg(),
				info.getWarnValidFlg(),
				info.getCriticalValidFlg(),
				info.getUnknownValidFlg()
		};
		return validFlgs;
	}

	private void updateTemplate() {
		List<CommandTemplateResponse> templates = new GetCommandTemplate().getCommandTemplateListByOwnerRole(managerName, ownerRoleId);
		for (Combo comboTemplate : getComboTemplates()) {
			comboTemplate.removeAll();
			comboTemplate.add(""); // 無選択
			for (CommandTemplateResponse template : templates) {
				comboTemplate.add(template.getCommandTemplateId());
			}
		}
		init = false;
	}
}
