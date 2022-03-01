/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.CommandModuleInfoResponse;
import org.openapitools.client.model.CommandModuleInfoResponse.AccessMethodTypeEnum;
import org.openapitools.client.model.InfraManagementInfoResponse;
import org.openapitools.client.model.ModifyInfraManagementRequest;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementDuplicate;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.infra.bean.AccessMethodMessage;
import com.clustercontrol.infra.util.InfraDtoConverter;
import com.clustercontrol.infra.util.InfraRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

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
	private InfraManagementInfoResponse infraInfo ;
	private CommandModuleInfoResponse moduleInfo;
	
	/**
	 * ダイアログの最背面レイヤのカラム数
	 * 最背面のレイヤのカラム数のみを変更するとレイアウトがくずれるため、
	 * グループ化されているレイヤは全てこれにあわせる
	 */
	private final int DIALOG_WIDTH = 20;
	/** タイトルラベルのカラム数 */
	private final int TITLE_WIDTH = 4;
	/** テキストフォームのカラム数 */
	private final int FORM_WIDTH = 16;
	/*
	 * 基本情報設定
	 */
	/** モジュールID用テキスト */
	private Text m_moduleId = null;
	/** モジュール名用テキスト */
	private Text m_moduleName = null;
	/** 実行コマンド用テキスト */
	private Text m_commandExec = null;
	/** 戻り値の変数名用テキスト */
	private Text m_execReturnParamName = null;
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
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 15;
		m_methodSSH.setText(AccessMethodMessage.STRING_SSH);
		m_methodSSH.setLayoutData(gridData);
		//WinRMボタン
		m_methodWinRM = new Button(commandModuleComposite, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = SWT.BEGINNING;
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
		gridData.horizontalAlignment = SWT.BEGINNING;
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
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_proceedIfFailFlg.setText(Messages.getString("infra.module.inexec.after.exec.error"));
		m_proceedIfFailFlg.setLayoutData(gridData);


		/*
		 * 実行コマンドグループ
		 */
		Group groupCommandModuleComposite = new Group(commandModuleComposite, SWT.NONE);
		groupCommandModuleComposite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCommandModuleComposite.setLayoutData(gridData);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = DIALOG_WIDTH;
		commandModuleComposite.setLayout(layout);

		/*
		 * 実行コマンド
		 */
		Label labelCommandExec = new Label(groupCommandModuleComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelCommandExec.setText(Messages.getString("infra.module.exec.command") + " : ");
		labelCommandExec.setLayoutData(gridData);
		m_commandExec = new Text(groupCommandModuleComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		gridData = new GridData(500, 100);
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalIndent = 25;
		m_commandExec.setLayoutData(gridData);
		String tooltipText = Messages.getString("infra.command.tooltip") + Messages.getString("replace.parameter.node");
		m_commandExec.setToolTipText(tooltipText);
		m_commandExec.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 戻り値変数
		 */
		Label label = new Label(groupCommandModuleComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setText(Messages.getString("infra.module.exec.return.param.name") + " : ");
		label.setLayoutData(gridData);
		m_execReturnParamName = new Text(groupCommandModuleComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 13;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 25;
		m_execReturnParamName.setLayoutData(gridData);
		m_execReturnParamName.addModifyListener(new ModifyListener(){
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
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelCommandCheck.setText(Messages.getString("infra.module.check.command") + " : ");
		labelCommandCheck.setLayoutData(gridData);
		m_commandCheck = new Text(commandModuleComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		gridData = new GridData(500, 100);
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalIndent = 25;
		m_commandCheck.setLayoutData(gridData);
		m_commandCheck.setToolTipText(tooltipText);
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
		gridData.horizontalAlignment = SWT.BEGINNING;
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
		m_shell.setSize(new Point(760, m_shell.getSize().y));

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
	}

	/**
	 * ダイアログにモジュール情報を反映します。
	 *
	 * @see com.clustercontrol.infra.bean.InfraManagementInfo
	 */
	private void setInputData() {
		InfraManagementInfoResponse info = null;
		CommandModuleInfoResponse module = null;
		try {
			InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(this.managerName);
			info = wrapper.getInfraManagement(this.managementId);
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InfraManagementNotFound | InvalidSetting e) {
			m_log.error(managementId + " InfraManagerInfo is null");
		}

		if (mode == PropertyDefineConstant.MODE_ADD | mode == PropertyDefineConstant.MODE_COPY) {
			// 作成・コピーの場合新規モジュールの orderNo を計算して設定しておく
			moduleInfo = new CommandModuleInfoResponse();
			int orderNo = getOrderNoWhenAddModule(info);
			moduleInfo.setOrderNo(orderNo);
			List<CommandModuleInfoResponse> modules = info.getCommandModuleInfoList();
			modules.add(moduleInfo);
		} else if (mode == PropertyDefineConstant.MODE_MODIFY){
			// 変更の場合モジュールを取得
			for(CommandModuleInfoResponse tmpModule: info.getCommandModuleInfoList()){
				if(tmpModule.getModuleId().equals(moduleId)){
					moduleInfo = tmpModule;
				}
			}
		}
		
		// 変更、コピーの場合、情報取得
		if (moduleId != null) {
			for (CommandModuleInfoResponse tmpModule : info.getCommandModuleInfoList()) {
				if (tmpModule.getModuleId().equals(moduleId)) {
					module = tmpModule;
					break;
				}
			}
			//	モジュールID
			//	findbugs対応 nullチェック追加
			if (module != null) {
				m_moduleId.setText(module.getModuleId());
			}
			if (mode == PropertyDefineConstant.MODE_MODIFY) {
				m_moduleId.setEnabled(false);
			}
			//	モジュール名
			//	findbugs対応 nullチェック追加
			if (module != null) {
				m_moduleName.setText(module.getName());
			}
			//	実行方法
			//	findbugs対応 nullチェック追加
			if (module != null && module.getAccessMethodType() == AccessMethodTypeEnum.WINRM) {
				m_methodWinRM.setSelection(true);
			}
			else {
				m_methodSSH.setSelection(true);
			}
			//	findbugs対応 nullチェック追加
			if (module != null) {
				//	実行前に確認
				m_precheckFlg.setSelection(module.getPrecheckFlg());
				//	エラーが起こったら後続モジュールを実行しない
				m_proceedIfFailFlg.setSelection(module.getStopIfFailFlg());
				//	チェックコマンドの取得
				if (module.getCheckCommand() != null) {
					m_commandCheck.setText(module.getCheckCommand());
				}
				//	実行コマンドの取得
				m_commandExec.setText(module.getExecCommand());
				//	戻り値の変数の取得
				if (module.getExecReturnParamName() != null) {
					m_execReturnParamName.setText(module.getExecReturnParamName());
				}
				//	設定の有効･無効
				m_validFlg.setSelection(module.getValidFlg());
			}
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
			moduleInfo.setAccessMethodType(AccessMethodTypeEnum.SSH);
		} else {
			moduleInfo.setAccessMethodType(AccessMethodTypeEnum.WINRM);
		}

		//チェックコマンドで確認するかどうかのフラグ取得
		moduleInfo.setPrecheckFlg(m_precheckFlg.getSelection());

		//実行コマンドが正常に行われなかった場合後続モジュールを実行しないかのフラグ取得
		moduleInfo.setStopIfFailFlg(m_proceedIfFailFlg.getSelection());

		//実行コマンド取得
		moduleInfo.setExecCommand(m_commandExec.getText());

		//戻り値の変数取得
		if (m_execReturnParamName.getText() != null) {
			moduleInfo.setExecReturnParamName(m_execReturnParamName.getText().trim());
		}

		//チェックコマンド取得
		if (m_commandCheck.getText() != null) {
			moduleInfo.setCheckCommand(m_commandCheck.getText().trim());
		}

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
				InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(this.managerName);
				String managementId = infraInfo.getManagementId();
				ModifyInfraManagementRequest dtoReq = new ModifyInfraManagementRequest();
				RestClientBeanUtil.convertBean(infraInfo, dtoReq);
				InfraDtoConverter.convertInfoToDto(infraInfo, dtoReq);
				wrapper.modifyInfraManagement(managementId, dtoReq);
				action += "(" + this.managerName + ")";
				result = true;
				MessageDialog.openInformation(null, Messages
						.getString("successful"), Messages.getString(
						"message.infra.action.result",
						new Object[] { Messages.getString("infra.module"),
								action, Messages.getString("successful"),
								m_moduleId.getText() }));
			} catch (InfraManagementDuplicate e) {
				// ID重複
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.infra.module.duplicate", new String[]{m_moduleId.getText()}));
			} catch (InvalidRole e) {
				// 権限なし
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (InfraManagementNotFound | NotifyDuplicate | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidSetting e) {
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
	
	private int getOrderNoWhenAddModule(InfraManagementInfoResponse info) {
		return info.getCommandModuleInfoList().size() + info.getFileTransferModuleInfoList().size()
				+ info.getReferManagementModuleInfoList().size() + 1;
	}
}
