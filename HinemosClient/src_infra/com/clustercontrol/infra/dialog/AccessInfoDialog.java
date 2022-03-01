/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.CreateAccessInfoListForDialogResponse;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 環境構築[ログイン情報入力]ダイアログクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class AccessInfoDialog extends CommonDialog {

	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = 6;
	
	/** カラム数（テキストボックス）。 */
	public static final int WIDTH_TEXT = 9;
	

	/** 入力値を保持するオブジェクト。 */
	private CreateAccessInfoListForDialogResponse m_inputData = null;
	
	private boolean afterSameAll = false;

	/** 入力値の正当性を保持するオブジェクト。 */
	private ValidateResult m_validateResult = null;

	/** ファイシリティID */
	private Text m_txtFacilityId = null;

	/** モジュールID */
	private Text m_txtModuleId = null;

	private Text m_txtSshUser = null;
	private Text m_txtSshPassword = null;
	private Text m_txtSshPrivateKeyFilepath = null;
	private Text m_txtSshPrivateKeyPassphrase = null;
	private Text m_txtWinRmUser = null;
	private Text m_txtWinRmPassword = null;
	
	/** 以降のログイン情報を同じ設定にする */
	private Button m_btnAfterSameAll = null;

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param identifier 変更する文字列監視の判定情報の識別キー
	 */
	public AccessInfoDialog(Shell parent, CreateAccessInfoListForDialogResponse info) {
		super(parent);

		this.m_inputData = info;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 * @see #setInputData(Pattern)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.infra.input.credential"));

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
		 * ファシリティID
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("facility.id") + " : ");
		// テキスト
		this.m_txtFacilityId = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "facilityId", m_txtFacilityId);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_txtFacilityId.setLayoutData(gridData);
		this.m_txtFacilityId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * モジュールID
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("infra.module.id") + " : ");
		// テキスト
		this.m_txtModuleId = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "moduleId", m_txtModuleId);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_txtModuleId.setLayoutData(gridData);
		this.m_txtModuleId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * ログイン情報グループ
		 */
		// グループ
		Group credentialGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "credential", credentialGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		credentialGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		credentialGroup.setLayoutData(gridData);
		credentialGroup.setText(Messages.getString("monitor.rule"));

		/*
		 * SSHユーザ
		 */
		// ラベル
		label = new Label(credentialGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("ssh") + Messages.getString("user") + " : ");
		// テキスト
		this.m_txtSshUser = new Text(credentialGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "sshUser", m_txtSshUser);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_txtSshUser.setLayoutData(gridData);

		/*
		 * SSHパスワード
		 */
		// ラベル
		label = new Label(credentialGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("ssh") + Messages.getString("password") + " : ");
		// テキスト
		this.m_txtSshPassword = new Text(credentialGroup, SWT.BORDER | SWT.LEFT | SWT.PASSWORD);
		WidgetTestUtil.setTestId(this, "sshPassword", m_txtSshPassword);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_txtSshPassword.setLayoutData(gridData);

		/*
		 * SSH鍵
		 */
		// ラベル
		label = new Label(credentialGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("ssh.private.key.filepath") + " : ");
		// テキスト
		this.m_txtSshPrivateKeyFilepath = new Text(credentialGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "sshPrivateKeyFilepath", m_txtSshPrivateKeyFilepath);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_txtSshPrivateKeyFilepath.setLayoutData(gridData);

		/*
		 * SSH鍵パスフレーズ
		 */
		// ラベル
		label = new Label(credentialGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("ssh.private.key.passphrase") + " : ");
		// テキスト
		this.m_txtSshPrivateKeyPassphrase = new Text(credentialGroup, SWT.BORDER | SWT.LEFT | SWT.PASSWORD);
		WidgetTestUtil.setTestId(this, "sshPrivateKeyPassphrase", m_txtSshPrivateKeyPassphrase);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_txtSshPrivateKeyPassphrase.setLayoutData(gridData);

		/*
		 * WinRmユーザ
		 */
		// ラベル
		label = new Label(credentialGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("winrm") + Messages.getString("user") +  " : ");
		// テキスト
		this.m_txtWinRmUser = new Text(credentialGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "winRmUser", m_txtSshUser);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_txtWinRmUser.setLayoutData(gridData);

		/*
		 * WinRMパスワード
		 */
		// ラベル
		label = new Label(credentialGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("winrm") + Messages.getString("password") + " : ");
		// テキスト
		this.m_txtWinRmPassword = new Text(credentialGroup, SWT.BORDER | SWT.LEFT | SWT.PASSWORD);
		WidgetTestUtil.setTestId(this, "winRmPassword", m_txtSshPassword);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_txtWinRmPassword.setLayoutData(gridData);

		/*
		 * 以降のログイン情報を同じ設定にする
		 */
		this.m_btnAfterSameAll = new Button(parent, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "sameAll", m_btnAfterSameAll);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_btnAfterSameAll.setLayoutData(gridData);
		this.m_btnAfterSameAll.setText(Messages.getString("infra.credential.after.same.setting"));

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 15;
		line.setLayoutData(gridData);

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(550, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		this.setInputData(this.m_inputData);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
	}


	/**
	 * 入力値を保持した文字列監視の判定情報を返します。
	 *
	 * @return 判定情報
	 */
	public CreateAccessInfoListForDialogResponse getInputData() {
		return this.m_inputData;
	}

	/**
	 * 引数で指定された判定情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる判定情報
	 */
	protected void setInputData(CreateAccessInfoListForDialogResponse info) {

		// ファシリティID
		this.m_txtFacilityId.setText(info.getFacilityId());
		this.m_txtFacilityId.setEnabled(false);
		
		// モジュールID
		if (info.getModuleId() != null) {
			this.m_txtModuleId.setText(info.getModuleId());
		}
		this.m_txtModuleId.setEnabled(false);

		if (info.getSshUser() != null && !info.getSshUser().isEmpty()) {
			this.m_txtSshUser.setText(info.getSshUser());
		}
		if (info.getSshPassword() != null && !info.getSshPassword().isEmpty()) {
			this.m_txtSshPassword.setText(info.getSshPassword());
		}
		if (info.getSshPrivateKeyFilepath() != null && !info.getSshPrivateKeyFilepath().isEmpty()) {
			this.m_txtSshPrivateKeyFilepath.setText(info.getSshPrivateKeyFilepath());
		}
		if (info.getSshPrivateKeyPassphrase() != null && !info.getSshPrivateKeyPassphrase().isEmpty()) {
			this.m_txtSshPrivateKeyPassphrase.setText(info.getSshPrivateKeyPassphrase());
		}
		if (info.getWinRmUser() != null && !info.getWinRmUser().isEmpty()) {
			this.m_txtWinRmUser.setText(info.getWinRmUser());
		}
		if (info.getWinRmPassword() != null && !info.getWinRmPassword().isEmpty()) {
			this.m_txtWinRmPassword.setText(info.getWinRmPassword());
		}

		// 必須項目を可視化
		this.update();

	}

	/**
	 * 引数で指定された判定情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 判定情報
	 *
	 * @see #setValidateResult(String, String)
	 */
	private CreateAccessInfoListForDialogResponse createInputData() {
		m_inputData.setFacilityId(m_inputData.getFacilityId());
		
		m_inputData.setSshUser(null2empty(m_txtSshUser.getText()));
		m_inputData.setSshPassword(null2empty(m_txtSshPassword.getText()));
		m_inputData.setSshPrivateKeyFilepath(null2empty(m_txtSshPrivateKeyFilepath.getText()));
		m_inputData.setSshPrivateKeyPassphrase(null2empty(m_txtSshPrivateKeyPassphrase.getText()));
		m_inputData.setWinRmUser(null2empty(m_txtWinRmUser.getText()));
		m_inputData.setWinRmPassword(null2empty(m_txtWinRmPassword.getText()));

		setAfterSameAll(m_btnAfterSameAll.getSelection());

		return m_inputData;
	}
	
	private String null2empty(String s) {
		if (s == null) {
			return "";
		}
		return s;
	}

	/**
	 * 無効な入力値をチェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see #createInputData()
	 */
	@Override
	protected ValidateResult validate() {
		this.m_validateResult = null;
		return super.validate();
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
	protected void setValidateResult(String id, String message) {

		this.m_validateResult = new ValidateResult();
		this.m_validateResult.setValid(false);
		this.m_validateResult.setID(id);
		this.m_validateResult.setMessage(message);
	}

	/**
	 * 入力値の判定を行います。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		this.m_inputData = createInputData();
		if(this.m_inputData != null){
			result = true;
		}

		return result;
	}

	public boolean isAfterSameAll() {
		return afterSameAll;
	}

	public void setAfterSameAll(boolean afterSameAll) {
		this.afterSameAll = afterSameAll;
	}
}
