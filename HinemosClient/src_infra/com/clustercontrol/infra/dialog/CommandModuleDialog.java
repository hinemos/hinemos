/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.dialog;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.infra.bean.AccessMethodConstant;
import com.clustercontrol.infra.bean.AccessMethodMessage;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.infra.CommandModuleInfo;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.InfraManagementDuplicate_Exception;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.infra.InfraManagementNotFound_Exception;
import com.clustercontrol.ws.infra.InfraModuleInfo;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidSetting_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;
import com.clustercontrol.ws.infra.NotifyDuplicate_Exception;
import com.clustercontrol.ws.infra.NotifyNotFound_Exception;

/**
 * 環境構築[コマンドモジュールの作成・変更]ダイアログクラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class CommandModuleDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( CommandModuleDialog.class );
	/** 環境構築[構築]情報*/
	private InfraManagementInfo infraInfo ;
	private CommandModuleInfo moduleInfo;
	
	/**
	 * ダイアログの最背面レイヤのカラム数
	 * 最背面のレイヤのカラム数のみを変更するとレイアウトがくずれるため、
	 * グループ化されているレイヤは全てこれにあわせる
	 */
	private final int DIALOG_WIDTH = 12;
	/** タイトルラベルのカラム数 */
	private final int TITLE_WIDTH = 4;
	/** テキストフォームのカラム数 */
	private final int FORM_WIDTH = 6;
	/*
	 * 基本情報設定
	 */
	/** モジュールID用テキスト */
	private Text m_moduleId = null;
	/** モジュール名用テキスト */
	private Text m_moduleName = null;
	/** 実行コマンド用テキスト */
	private Text m_commandExec = null;
	/** チェックコマンド用テキスト */
	private Text m_commandCheck = null;
	/** チェックコマンドで事前に確認する用ボタン */
	private Button m_precheckFlg = null;
	/** 実行コマンドが失敗したら後続を行わない用ボタン*/
	private Button m_proceedIfFailFlg = null;
	/** モジュールの有効用ボタン*/
	private Button m_validFlg = null;
	/** SSH用ラジオボタン */
	private Button m_methodSSH = null;
	/** WinRM用ラジオボタン */
	private Button m_methodWinRM = null;
	/** ダミー用ラベル*/
	private Label m_label = null;
	/** シェル */
	private Shell m_shell = null;

	/**
	 * 作成：MODE_ADD = 0;
	 * 変更：MODE_MODIFY = 1;
	 * 複製：MODE_COPY = 3;
	 * */
	private int mode;
	/** マネージャ名 */
	private String managerName = null;
	/** 選択モジュールID*/
	private String moduleId = null;
	/** 所属構築ID */
	private String managementId = null;
	/** インデックス（初期：-1）*/
	private int index = -1;


	/**
	 * コンストラクタ
	 * 変更時、コピー時
	 * @param parent
	 * @param id
	 */
	public CommandModuleDialog(Shell parent, String managerName, String managementId) {
		super(parent);
		this.managerName = managerName;
		this.managementId = managementId;
	}
	public CommandModuleDialog(Shell parent, String managerName, String managementId, String moduleId, int mode) {
		super(parent);
		this.managerName = managerName;
		this.managementId = managementId;
		this.moduleId = moduleId;
		this.mode = mode;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_shell = this.getShell();
		parent.getShell().setText(
				Messages.getString("dialog.infra.module.command"));
		/**
		 * レイアウト設定
		 * ダイアログ内のベースとなるレイアウトが全てを変更
		 */
		GridLayout baseLayout = new GridLayout(1, true);
		baseLayout.marginWidth = 10;
		baseLayout.marginHeight = 10;
		baseLayout.numColumns = DIALOG_WIDTH;
		//一番下のレイヤー
		parent.setLayout(baseLayout);

		GridData gridData= null;

		Composite commandModuleComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = DIALOG_WIDTH;
		commandModuleComposite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		commandModuleComposite.setLayoutData(gridData);

		/*
		 * モジュールID
		 */
		Label labelModuleId = new Label(commandModuleComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelModuleId.setText(Messages.getString("infra.module.id") + " : ");
		labelModuleId.setLayoutData(gridData);
		m_moduleId = new Text(commandModuleComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_moduleId.setLayoutData(gridData);
		this.m_moduleId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * モジュール名
		 */
		Label labelModuleName = new Label(commandModuleComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelModuleName.setText(Messages.getString("infra.module.name") + " : ");
		labelModuleName.setLayoutData(gridData);
		this.m_moduleName = new Text(commandModuleComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_moduleName.setLayoutData(gridData);
		this.m_moduleName.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 実行方法
		 */
		Label labelSelectMethod = new Label(commandModuleComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelSelectMethod.setText(Messages.getString("infra.module.exec.method") + " : ");
		labelSelectMethod.setLayoutData(gridData);
		//SSHボタン
		m_methodSSH = new Button(commandModuleComposite, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 15;
		m_methodSSH.setText(AccessMethodMessage.STRING_SSH);
		m_methodSSH.setLayoutData(gridData);
		//WinRMボタン
		m_methodWinRM = new Button(commandModuleComposite, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 15;
		m_methodWinRM.setText(AccessMethodMessage.STRING_WINRM);
		m_methodWinRM.setLayoutData(gridData);

		// 空白
		m_label = new Label(commandModuleComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_label.setLayoutData(gridData);

		/*
		 * チェックコマンドで確認を行うかどうかの確認
		 */
		m_precheckFlg = new Button(commandModuleComposite, SWT.CHECK |SWT.TOP);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_precheckFlg.setText(Messages.getString("infra.module.check.command.run.before"));
		m_precheckFlg.setLayoutData(gridData);
		//確認後どう行うかの説明
		Label labelSkipExec = new Label(commandModuleComposite, SWT.LEFT | SWT.WRAP);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 10;
		labelSkipExec.setText(Messages.getString("infra.module.check.command.run.before.supple"));
		labelSkipExec.setLayoutData(gridData);

		/*
		 * 実行が正常に終了しなかった場合、後続モジュールを実行しないかどうかの確認
		 */
		m_proceedIfFailFlg = new Button(commandModuleComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_proceedIfFailFlg.setText(Messages.getString("infra.module.inexec.after.exec.error"));
		m_proceedIfFailFlg.setLayoutData(gridData);

		// 空白
		m_label = new Label(commandModuleComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_label.setLayoutData(gridData);

		/*
		 * 実行コマンド
		 */
		Label labelCommandExec = new Label(commandModuleComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelCommandExec.setText(Messages.getString("infra.module.exec.command") + " : ");
		labelCommandExec.setLayoutData(gridData);
		m_commandExec = new Text(commandModuleComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 25;
		m_commandExec.setLayoutData(gridData);
		m_commandExec.setToolTipText(Messages.getString("infra.command.tooltip"));
		m_commandExec.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * チェックコマンド
		 */
		Label labelCommandCheck = new Label(commandModuleComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelCommandCheck.setText(Messages.getString("infra.module.check.command") + " : ");
		labelCommandCheck.setLayoutData(gridData);
		m_commandCheck = new Text(commandModuleComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 25;
		m_commandCheck.setLayoutData(gridData);
		m_commandCheck.setToolTipText(Messages.getString("infra.command.tooltip"));
		m_commandCheck.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		// 空白
		m_label = new Label(commandModuleComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_label.setLayoutData(gridData);

		/*
		 * ラインを引く
		 */
		Label line = new Label(commandModuleComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = DIALOG_WIDTH;
		line.setLayoutData(gridData);

		/*
		 * 有効ボタン
		 */
		m_validFlg = new Button(commandModuleComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_validFlg.setText(Messages.getString("setting.valid.confirmed"));
		m_validFlg.setLayoutData(gridData);

		// ダイアログを調整
		this.adjustDialog();

		//コマンドモジュール情報反映
		setInputData();
		update();

	}


	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		m_shell.pack();
		m_shell.setSize(new Point(500, m_shell.getSize().y));

		// 画面中央に配置
		Display display = m_shell.getDisplay();
		m_shell.setLocation((display.getBounds().width - m_shell.getSize().x) / 2,
				(display.getBounds().height - m_shell.getSize().y) / 2);
	}
	/**
	 * 更新処理
	 *
	 */
	public void update(){
		/*
		 *  必須項目を明示
		 */
		//モジュールID
		if("".equals(this.m_moduleId.getText())){
			this.m_moduleId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_moduleId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//モジュール名
		if("".equals(this.m_moduleName.getText())){
			this.m_moduleName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_moduleName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//実行コマンド
		if("".equals(this.m_commandExec.getText())){
			this.m_commandExec.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_commandExec.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//チェックコマンド
		if("".equals(this.m_commandCheck.getText())){
			this.m_commandCheck.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_commandCheck.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ダイアログにモジュール情報を反映します。
	 *
	 * @see com.clustercontrol.infra.bean.InfraManagementInfo
	 */
	private void setInputData() {
		InfraManagementInfo info = null;
		CommandModuleInfo module = null;
		try {
			InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(this.managerName);
			info = wrapper.getInfraManagement(this.managementId);
		} catch (InfraManagementNotFound_Exception | HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception e) {
			m_log.error(managementId + " InfraManagerInfo is null");
		}

		if (mode == PropertyDefineConstant.MODE_ADD | mode == PropertyDefineConstant.MODE_COPY) {
			// 作成・コピーの場合新規モジュールを末尾に追加
			moduleInfo = new CommandModuleInfo();
			List<InfraModuleInfo> modules = info.getModuleList();
			modules.add(moduleInfo);
		} else if (mode == PropertyDefineConstant.MODE_MODIFY){
			// 変更の場合モジュールを取得
			for(InfraModuleInfo tmpModule: info.getModuleList()){
				if(tmpModule.getModuleId().equals(moduleId)){
					moduleInfo = (CommandModuleInfo) tmpModule;
				}
			}
		}
		
		// 変更、コピーの場合、情報取得
		if (moduleId != null) {
			for (InfraModuleInfo tmpModule : info.getModuleList()) {
				if (tmpModule.getModuleId().equals(moduleId)) {
					index = info.getModuleList().indexOf(tmpModule);
					break;
				}
			}
			module = (CommandModuleInfo) info.getModuleList().get(index);
			//	モジュールID
			m_moduleId.setText(module.getModuleId());
			if (mode == PropertyDefineConstant.MODE_MODIFY) {
				m_moduleId.setEnabled(false);
			}
			//	モジュール名
			m_moduleName.setText(module.getName());
			//	実行方法
			if (module.getAccessMethodType() == AccessMethodConstant.TYPE_WINRM) {
				m_methodWinRM.setSelection(true);
			}
			else {
				m_methodSSH.setSelection(true);
			}

			//	実行前に確認
			m_precheckFlg.setSelection(module.isPrecheckFlg());
			//	エラーが起こったら後続モジュールを実行しない
			m_proceedIfFailFlg.setSelection(module.isStopIfFailFlg());
			//	チェックコマンドの取得
			m_commandCheck.setText(module.getCheckCommand());
			//	実行コマンドの取得
			m_commandExec.setText(module.getExecCommand());
			//	設定の有効･無効
			m_validFlg.setSelection(module.isValidFlg());

		} else {
			// 作成の場合(default設定)
			m_validFlg.setSelection(true);
			m_methodSSH.setSelection(true);
		}
		//	取得した構築設定の情報
		infraInfo = info;
	}

	/**
	 * ダイアログの情報からコマンドモジュール情報を作成します。
	 *
	 * @return コマンドモジュールの情報
	 *
	 * @see com.clustercontrol.infra.bean.ExecutableModuleInfo
	 *
	 */
	private void createInputData() {

		//モジュールID取得
		moduleInfo.setModuleId(m_moduleId.getText());

		//モジュール名取得
		moduleInfo.setName(m_moduleName.getText());

		//実行方法の取得
		if (m_methodSSH.getSelection()) {
			moduleInfo.setAccessMethodType(AccessMethodConstant.TYPE_SSH);
		} else {
			moduleInfo.setAccessMethodType(AccessMethodConstant.TYPE_WINRM);
		}

		//チェックコマンドで確認するかどうかのフラグ取得
		moduleInfo.setPrecheckFlg(m_precheckFlg.getSelection());

		//実行コマンドが正常に行われなかった場合後続モジュールを実行しないかのフラグ取得
		moduleInfo.setStopIfFailFlg(m_proceedIfFailFlg.getSelection());

		//実行コマンド取得
		moduleInfo.setExecCommand(m_commandExec.getText());

		//チェックコマンド取得
		moduleInfo.setCheckCommand(m_commandCheck.getText());

		//設定の有効
		moduleInfo.setValidFlg(m_validFlg.getSelection());
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("register");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}


	@Override
	protected ValidateResult validate() {
		if ("".equals((m_moduleId.getText()).trim())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.infra.specify.item",
							new Object[]{Messages.getString("infra.module.id")}));
		}
		if ("".equals((m_moduleName.getText()).trim())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.infra.specify.item",
							new Object[]{Messages.getString("infra.module.name")}));
		}
		if ("".equals((this.m_commandExec.getText()).trim())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.infra.specify.item",
							new Object[]{Messages.getString("infra.module.exec.command")}));
		}
		if ("".equals((this.m_commandCheck.getText()).trim())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.infra.specify.item",
							new Object[]{Messages.getString("infra.module.check.command")}));
		}
		return super.validate();
	}

	/**
	 * ValidateResultを作成します
	 * @id messageBoxのタイトル部分の表記
	 * @message messageBoxに表示する文字列の設定
	 */
	protected ValidateResult createValidateResult(String id, String message) {
		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	@Override
	protected boolean action() {
		boolean result = false;
		createInputData();
		String action = null;
		if(infraInfo != null){
			if(mode == PropertyDefineConstant.MODE_ADD | mode == PropertyDefineConstant.MODE_COPY){
				// 作成の場合
				action = Messages.getString("add");
			} else if (mode == PropertyDefineConstant.MODE_MODIFY){
				// 変更の場合
				action = Messages.getString("modify");
			}

			try {
				InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(this.managerName);
				wrapper.modifyInfraManagement(infraInfo);
				action += "(" + this.managerName + ")";
				result = true;
				MessageDialog.openInformation(null, Messages
						.getString("successful"), Messages.getString(
						"message.infra.action.result",
						new Object[] { Messages.getString("infra.module"),
								action, Messages.getString("successful"),
								m_moduleId.getText() }));
			} catch (InfraManagementDuplicate_Exception e) {
				// ID重複
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.infra.module.duplicate", new String[]{m_moduleId.getText()}));
			} catch (InvalidRole_Exception e) {
				// 権限なし
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (InfraManagementNotFound_Exception | NotifyDuplicate_Exception | NotifyNotFound_Exception | HinemosUnknown_Exception | InvalidUserPass_Exception | InvalidSetting_Exception e) {
				m_log.info("action() modifyInfraManagement : " + e.getMessage() + " (" + e.getClass().getName() + ")");
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module"), action, 
								Messages.getString("failed"), m_moduleId.getText() + "\n" + HinemosMessage.replace(e.getMessage())}));
			} catch (Exception e) {
				m_log.info("action() modifyInfraManagement : " + e.getMessage() + " (" + e.getClass().getName() + ")");
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module"), action, 
								Messages.getString("failed"), m_moduleId.getText() + "\n" + HinemosMessage.replace(e.getMessage())}));
			}
		} else {
			m_log.error("inputData InfraManagerInfo is null");
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module"), action, Messages.getString("failed"), m_moduleId.getText()}));
		}
		return result;
	}
}
