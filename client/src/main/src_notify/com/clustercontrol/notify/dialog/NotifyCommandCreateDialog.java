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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.TextWithParameterComposite;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.action.AddNotify;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.ModifyNotify;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.notify.NotifyCommandInfo;
import com.clustercontrol.ws.notify.NotifyInfo;

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
	 * @see #setInputData(NotifyInfo)
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// 通知IDが指定されている場合、その情報を初期表示する。
		NotifyInfo info = null;
		if (this.notifyId != null) {
			info = new GetNotify().getNotify(this.managerName, this.notifyId);
		} else {
			info = new NotifyInfo();
		}
		this.setInputData(info);

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

		/*
		 * 重要度 ごとの設定
		 */
		// ラベル（重要度）
		label = new Label(groupComamnd, SWT.NONE);
		WidgetTestUtil.setTestId(this, "priority", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// ラベル（実行）
		label = new Label(groupComamnd, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyattribute", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.attribute"));

		// ラベル（ユーザ入力欄）
		label = new Label(groupComamnd, SWT.NONE);
		WidgetTestUtil.setTestId(this, "effectiveuser", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMMAND_USER;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("effective.user"));

		// ラベル（コマンド入力欄）
		label = new Label(groupComamnd, SWT.NONE);
		WidgetTestUtil.setTestId(this, "command", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMMAND_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("command"));

		// 重要度：通知
		label = this.getLabelPriority(groupComamnd, Messages.getString("info"),
				PriorityColorConstant.COLOR_INFO);
		this.m_checkExecInfo = this.getCheckBox(groupComamnd);
		WidgetTestUtil.setTestId(this, "execinfo", m_checkExecInfo);
		this.m_textUserInfo = this.getTextUser(groupComamnd);
		WidgetTestUtil.setTestId(this, "userinfo", m_textUserInfo);
		this.m_textCommandInfo = this.getTextCommand(groupComamnd);
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
		label = this.getLabelPriority(groupComamnd, Messages
				.getString("warning"), PriorityColorConstant.COLOR_WARNING);
		this.m_checkExecWarning = this.getCheckBox(groupComamnd);
		WidgetTestUtil.setTestId(this, "execwarning", m_checkExecWarning);
		this.m_textUserWarning = this.getTextUser(groupComamnd);
		WidgetTestUtil.setTestId(this, "userwarning", m_textUserWarning);
		this.m_textCommandWarning = this.getTextCommand(groupComamnd);
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
		label = this.getLabelPriority(groupComamnd, Messages
				.getString("critical"), PriorityColorConstant.COLOR_CRITICAL);
		this.m_checkExecCritical = this.getCheckBox(groupComamnd);
		WidgetTestUtil.setTestId(this, "execcritical", m_checkExecCritical);
		this.m_textUserCritical = this.getTextUser(groupComamnd);
		WidgetTestUtil.setTestId(this, "usercritical", m_textUserCritical);
		this.m_textCommandCritical = this.getTextCommand(groupComamnd);
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
		label = this.getLabelPriority(groupComamnd, Messages
				.getString("unknown"), PriorityColorConstant.COLOR_UNKNOWN);
		this.m_checkExecUnknown = this.getCheckBox(groupComamnd);
		WidgetTestUtil.setTestId(this, "execunknown", m_checkExecUnknown);
		this.m_textUserUnknown = this.getTextUser(groupComamnd);
		WidgetTestUtil.setTestId(this, "userunknown", m_textUserUnknown);
		this.m_textCommandUnknown = this.getTextCommand(groupComamnd);
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
	public NotifyInfo getInputData() {
		return this.inputData;
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param notify
	 *            設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(NotifyInfo notify) {
		super.setInputData(notify);

		// コマンド情報
		NotifyCommandInfo info = notify.getNotifyCommandInfo();
		if (info != null) {
			this.setInputData(info);
		} else {
			// タイムアウト値（デフォルト）を指定
			this.m_textTimeout.setText(Integer.toString(TIMEOUT_SEC_COMMAND));
		}

		// 必須項目を可視化
		this.update();
	}

	/**
	 * 引数で指定された通知ステータス情報の値を、各項目に設定します。
	 *
	 * @param info
	 *            設定値として用いる通知コマンド情報
	 */
	private void setInputData(NotifyCommandInfo info) {
		Button[] checkExecs = getCheckExecs();
		Text[] textUsers = getTextUsers();
		TextWithParameterComposite[] textCommands = getTextCommands();

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
				textCommands[i].setText(commands[i]);
			}
		}
		this.m_textTimeout.setText(Integer.toString(info.getTimeout()));
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

	private Text[] getTextUsers() {
		Text[] textUsers = new Text[] {
				this.m_textUserInfo,
				this.m_textUserWarning,
				this.m_textUserCritical,
				this.m_textUserUnknown
		};
		return textUsers;
	}

	private Button[] getCheckExecs() {
		Button[] checkExecs = new Button[] {
				this.m_checkExecInfo,
				this.m_checkExecWarning,
				this.m_checkExecCritical,
				this.m_checkExecUnknown
		};
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
	protected NotifyInfo createInputData() {
		NotifyInfo info = super.createInputData();

		// 通知タイプの設定
		info.setNotifyType(TYPE_COMMAND);

		// コマンド情報
		NotifyCommandInfo notifyCommandInfo = this.createNotifyInfoDetail();
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
	private NotifyCommandInfo createNotifyInfoDetail() {
		NotifyCommandInfo command = new NotifyCommandInfo();
		// 環境変数を読み込むか否かのフラグ。デフォルトは有効とする。
		command.setSetEnvironment(true);

		// 実行チェックボックス
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

		// タイムアウト値を設定
		try {
			command.setTimeout(Integer.parseInt(m_textTimeout.getText()));
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

		NotifyInfo info = this.getInputData();
		if (info != null) {
			if (!this.updateFlg) {
				// 作成の場合
				result = new AddNotify().add(this.getInputManagerName(), info);
			} else {
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
		this.setEnabledDetail(enable,this.m_checkExecInfo,
				this.m_textUserInfo, this.m_textCommandInfo);
		this.setEnabledDetail(enable, this.m_checkExecWarning,
				this.m_textUserWarning, this.m_textCommandWarning);
		this.setEnabledDetail(enable, this.m_checkExecCritical,
				this.m_textUserCritical, this.m_textCommandCritical);
		this.setEnabledDetail(enable, this.m_checkExecUnknown,
				this.m_textUserUnknown, this.m_textCommandUnknown);
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
			Text textUser, TextWithParameterComposite textCommand) {

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
	private void setEnabled(boolean enabled, Text textUser,
			TextWithParameterComposite textCommand) {
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
			// TODO Remove the following hard-code. IDialogConstants.*_LABEL will causes IncompatibleClassChangeError on RAP
			this.createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
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

		notifyCmdCreateTooltipTextWithParamComposite.setToolTipText(Messages.getString("notify.parameter.tooltip"));
		notifyCmdCreateTooltipTextWithParamComposite.setColor(new Color(parent.getDisplay(), new RGB(0, 0, 255)));
		notifyCmdCreateTooltipTextWithParamComposite.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		return notifyCmdCreateTooltipTextWithParamComposite;
	}
}
