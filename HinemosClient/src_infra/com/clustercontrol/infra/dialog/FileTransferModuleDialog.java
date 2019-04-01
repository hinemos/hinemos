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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.infra.bean.SendMethodConstant;
import com.clustercontrol.infra.composite.FileReplaceSettingComposite;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.infra.FileTransferModuleInfo;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.InfraFileInfo;
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
 * 環境構築[ファイル配布モジュールの作成・変更]ダイアログクラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class FileTransferModuleDialog extends CommonDialog {
	// ログ
	private static Log m_log = LogFactory.getLog( FileTransferModuleDialog.class );

	// CONSTANT
	private static String DEFAULT_SCP_OWNER = Messages.getString("infra.module.transfer.default.owner");
	private static String DEFAULT_SCP_ATTRIBUTE = Messages.getString("infra.module.transfer.default.file.attibute");

	/** 環境構築[構築]情報*/
	private InfraManagementInfo infraInfo ;

	private FileTransferModuleInfo moduleInfo;

	/**
	 * ダイアログの最背面レイヤのカラム数8ijm
	 * 最背面のレイヤのカラム数のみを変更するとレイアウトがくずれるため、
	 * グループ化されているレイヤは全てこれにあわせる
	 */
	private final int DIALOG_WIDTH = 12;
	/*
	 * 基本情報設定
	 */
	/** マネージャ名 */
	private String m_managerName = null;
	/** 環境構築ID用文字列 */
	private String m_managementId = null;
	/** モジュールID用文字列 */
	private String m_strModuleId = null;
	/** モジュールID用テキスト */
	private Text m_moduleId = null;
	/** モジュール名用テキスト */
	private Text m_moduleName = null;
	/** 配置ファイルIDコンボボックス */
	private Combo m_comboFileId = null;
	/** 配置パス用テキスト */
	private Text m_placementPath = null;
	/** SCPによるファイル転送用ボタン */
	private Button m_scp = null;
	/** WinRMによるファイル転送用ボタン*/
	private Button m_winRm = null;

	/** 設定の有効ボタン*/
	private Button m_valid = null;

	/** SCPのオーナー用テキスト*/
	private Text m_scpOwner = null;
	/** SCPのファイル属性用テキスト*/
	private Text m_scpFileAttribute = null;
	/** ダミー用ラベル*/
	private Label dumyLabel = null;

	/** チェックコマンドが正常に行われた用ボタン */
	private Button m_rename = null;
	/** チェックコマンド*/
	private Button m_check = null;
	/** 戻り値の変数名用テキスト */
	private Text m_execReturnParamName = null;

	private FileReplaceSettingComposite m_chenge;
	/** シェル */
	private Shell m_shell = null;

	/**
	 * 作成：MODE_ADD = 0;
	 * 変更：MODE_MODIFY = 1;
	 * 複製：MODE_COPY = 3;
	 * */
	private int mode;

	private Button m_md5Check = null;


	public FileTransferModuleDialog(Shell parent, String managerName, String managementId) {
		super(parent);
		this.mode = PropertyDefineConstant.MODE_ADD;
		this.m_managerName = managerName;
		this.m_managementId = managementId;
	}

	public FileTransferModuleDialog(Shell parent, String managerName, String managementId, String moduleId, int mode) {
		super(parent);
		this.mode = mode;
		this.m_managerName = managerName;
		this.m_managementId = managementId;
		this.m_strModuleId = moduleId;
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
				Messages.getString("dialog.infra.module.transfer"));
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
		/*
		 * モジュールID
		 */
		Composite fileCheckComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 12;
		fileCheckComposite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		fileCheckComposite.setLayoutData(gridData);
		//ラベル
		Label labelModuleId = new Label(fileCheckComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelModuleId.setText(Messages.getString("infra.module.id") + " : ");
		labelModuleId.setLayoutData(gridData);

		//テキスト
		m_moduleId = new Text(fileCheckComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
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
		//ラベル
		Label labelModuleName = new Label(fileCheckComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelModuleName.setText(Messages.getString("infra.module.name") + " : ");
		labelModuleName.setLayoutData(gridData);
		//テキスト
		this.m_moduleName = new Text(fileCheckComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
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
		 * 配置ファイルID
		 */
		//ラベル
		Label labelFileId = new Label(fileCheckComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelFileId.setText(Messages.getString("infra.module.placement.file") + " : ");
		labelFileId.setLayoutData(gridData);

		//コンボボックス
		// 変更可能な場合コンボボックス
		this.m_comboFileId = new Combo(fileCheckComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboFileId.setLayoutData(gridData);
		this.m_comboFileId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 配置パス
		 */
		//ラベル
		Label labelPlacementPath = new Label(fileCheckComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelPlacementPath.setText(Messages.getString("infra.module.placement.path") + " : ");
		labelPlacementPath.setLayoutData(gridData);
		//テキスト
		this.m_placementPath = new Text(fileCheckComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_placementPath.setLayoutData(gridData);
		this.m_placementPath.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		dumyLabel = new Label(fileCheckComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		dumyLabel.setLayoutData(gridData);


		/*
		 * ファイルの転送方法
		 */
		Group fileDistributeMethod = new Group(fileCheckComposite, SWT.NONE);
		fileDistributeMethod.setText(Messages.getString("infra.module.transfer.method"));
		fileDistributeMethod.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		fileDistributeMethod.setLayoutData(gridData);


		//SCPによるファイル転送ボタン
		m_scp = new Button(fileDistributeMethod, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_scp.setText(Messages.getString("infra.module.transfer.method.scp"));
		m_scp.setLayoutData(gridData);
		m_scp.setSelection(true);
		m_scp.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});


		//ラベル
		Label labelSelectMethod = new Label(fileDistributeMethod, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 20;
		labelSelectMethod.setText(Messages.getString("infra.module.transfer.method.owner") + " : ");
		labelSelectMethod.setLayoutData(gridData);
		//テキスト
		this.m_scpOwner = new Text(fileDistributeMethod, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 20;
		this.m_scpOwner.setLayoutData(gridData);
		this.m_scpOwner.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//ラベル
		Label labelSelectMetho = new Label(fileDistributeMethod, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 20;
		labelSelectMetho.setText(Messages.getString("infra.module.transfer.method.scp.file.attribute") + " : ");
		labelSelectMetho.setLayoutData(gridData);

		//テキスト
		m_scpFileAttribute = new Text(fileDistributeMethod, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 20;
		m_scpFileAttribute.setLayoutData(gridData);
		this.m_scpFileAttribute.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});


		//WinRMによるファイル転送ボタン
		m_winRm = new Button(fileDistributeMethod, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_winRm.setText(Messages.getString("infra.module.transfer.method.winrm"));
		m_winRm.setLayoutData(gridData);
		m_winRm.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// 空白
		dumyLabel = new Label(fileCheckComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		dumyLabel.setLayoutData(gridData);
		//後続
		m_check = new Button(fileCheckComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_check.setText(Messages.getString("infra.module.inexec.after.transfer.error"));
		m_check.setLayoutData(gridData);


		//リネーム
		m_rename = new Button(fileCheckComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_rename.setText(Messages.getString("infra.module.same.file.maintain"));
		m_rename.setLayoutData(gridData);


		//MD5チェック
		m_md5Check = new Button(fileCheckComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_md5Check.setText(Messages.getString("infra.module.md5.check"));
		m_md5Check.setLayoutData(gridData);
		m_md5Check.setSelection(true);

		/*
		 * 戻り値変数
		 */
		Label label = new Label(fileCheckComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setText(Messages.getString("infra.module.exec.return.param.name") + " : ");
		label.setLayoutData(gridData);
		m_execReturnParamName = new Text(fileCheckComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
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

		// 空白
		dumyLabel = new Label(fileCheckComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		dumyLabel.setLayoutData(gridData);
		/*
		 * ファイル内の変数を置換
		 */

		//ラベル
		Label labelCommandCheck = new Label(fileCheckComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelCommandCheck.setText(Messages.getString("infra.module.transfer.chenge.variable") + " : ");
		labelCommandCheck.setLayoutData(gridData);

		//変数置換用コンポジット
		m_chenge = new FileReplaceSettingComposite(fileCheckComposite, SWT.NONE);
		m_chenge.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_chenge.setLayoutData(gridData);

		// 空白
		dumyLabel = new Label(fileCheckComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		dumyLabel.setLayoutData(gridData);

		// ラインを引く
		Label line = new Label(fileCheckComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 12;
		line.setLayoutData(gridData);

		//有効ボタン
		m_valid = new Button(fileCheckComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_valid.setText(Messages.getString("setting.valid.confirmed"));
		m_valid.setLayoutData(gridData);



		// ダイアログを調整
		this.adjustDialog();
		//スケジュール情報反映
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
		m_shell.setSize(new Point(550, m_shell.getSize().y));

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
		m_scpOwner.setEnabled(m_scp.getSelection());
		m_scpFileAttribute.setEnabled(m_scp.getSelection());

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
		//配置ファイル
		if("".equals(this.m_comboFileId.getText())){
			this.m_comboFileId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboFileId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//配置パス
		if("".equals(this.m_placementPath.getText())){
			this.m_placementPath.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_placementPath.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//オーナー
		if(m_scpOwner.isEnabled() && "".equals(this.m_scpOwner.getText())){
			this.m_scpOwner.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scpOwner.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//ファイル属性
		if(m_scpFileAttribute.isEnabled() && "".equals(this.m_scpFileAttribute.getText())){
			this.m_scpFileAttribute.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scpFileAttribute.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
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


	/**
	 * ダイアログにモジュール情報を反映します。
	 *
	 * @see com.clustercontrol.infra.bean.InfraManagementInfo
	 */
	private void setInputData() {
		InfraManagementInfo info = null;
		FileTransferModuleInfo module = null;
		try {
			InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(this.m_managerName);
			info = wrapper.getInfraManagement(m_managementId);
		} catch (InfraManagementNotFound_Exception | HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception e) {
			m_log.error(m_managementId + " InfraManagerInfo is null");
			return;
		}

		//環境構築ファイル一覧取得
		List<InfraFileInfo> infraFileInfoList = null;
		InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(m_managerName);
		try {
			infraFileInfoList = wrapper.getInfraFileListByOwnerRoleId(info.getOwnerRoleId());
		} catch (Exception e) {
			m_log.warn("setInputData() getInfraFileList, " + e.getMessage());
		}
		if (infraFileInfoList != null) {
			for (InfraFileInfo infraFileInfo : infraFileInfoList) {
				m_comboFileId.add(infraFileInfo.getFileId());
			}
		}
		
		if (mode == PropertyDefineConstant.MODE_ADD | mode == PropertyDefineConstant.MODE_COPY) {
			// 作成・コピーの場合新規モジュールを末尾に追加
			moduleInfo = new FileTransferModuleInfo();
			List<InfraModuleInfo> modules = info.getModuleList();
			modules.add(moduleInfo);
		} else if (mode == PropertyDefineConstant.MODE_MODIFY){
			// 変更の場合モジュールを取得
			for(InfraModuleInfo tmpModule: info.getModuleList()){
				if(tmpModule.getModuleId().equals(m_strModuleId)){
					moduleInfo = (FileTransferModuleInfo) tmpModule;
				}
			}
		}
		
		// 変更、コピーの場合、情報取得
		if (m_strModuleId != null && info != null) {
			for (InfraModuleInfo tmpModule : info.getModuleList()) {
				if (tmpModule.getModuleId().equals(m_strModuleId)) {
					module = (FileTransferModuleInfo) tmpModule;
					break;
				}
			}
			if (module == null) {
				m_log.error("setInputData() module does not find, " + m_strModuleId);
				return;
			}
			//	モジュールID
			m_moduleId.setText(module.getModuleId());
			if (mode == PropertyDefineConstant.MODE_MODIFY) {
				m_moduleId.setEnabled(false);
			}
			//	モジュール名
			m_moduleName.setText(module.getName());

			m_comboFileId.setText(module.getFileId());

			m_placementPath.setText(module.getDestPath());

			//	実行方法
			if (module.getSendMethodType() == SendMethodConstant.TYPE_WINRM) {
				m_scp.setSelection(false);
				m_winRm.setSelection(true);
			}
			else {
				m_scp.setSelection(true);
				m_winRm.setSelection(false);
				m_scpOwner.setText(module.getDestOwner());
				m_scpFileAttribute.setText(module.getDestAttribute());
			}

			//	後続
			m_check.setSelection(module.isStopIfFailFlg());
			//	リネーム
			m_rename.setSelection(module.isBackupIfExistFlg());
			// MD5
			m_md5Check.setSelection(module.isPrecheckFlg());
			// 戻り値の変数名
			if (module.getExecReturnParamName() != null) {
				m_execReturnParamName.setText(module.getExecReturnParamName());
			}
			//	設定の有効･無効
			m_valid.setSelection(module.isValidFlg());

			m_chenge.setInputData(module.getFileTransferVariableList());

		} else {
			// 作成の場合(default設定)
			m_valid.setSelection(true);
			m_scp.setSelection(true);
			m_scpOwner.setText(DEFAULT_SCP_OWNER);
			m_scpFileAttribute.setText(DEFAULT_SCP_ATTRIBUTE);
		}
		//	取得した構築設定の情報
		infraInfo = info;
	}

	/**
	 * ダイアログの情報からファイル配布モジュール情報を作成します。
	 *
	 * @return ファイル配布モジュールの情報
	 *
	 *
	 */
	private void createInputData() {
		//モジュールID取得
		moduleInfo.setModuleId(m_moduleId.getText());

		//モジュール名取得
		moduleInfo.setName(m_moduleName.getText());

		moduleInfo.setFileId(m_comboFileId.getText());

		moduleInfo.setDestPath(m_placementPath.getText());

		//実行方法の取得
		if (m_scp.getSelection()) {
			moduleInfo.setSendMethodType(SendMethodConstant.TYPE_SCP);
			moduleInfo.setDestOwner(m_scpOwner.getText());
			moduleInfo.setDestAttribute(m_scpFileAttribute.getText());
		} else {
			moduleInfo.setSendMethodType(SendMethodConstant.TYPE_WINRM);
		}

		//正常に行われなかった場合後続モジュールを実行しないかのフラグ取得
		moduleInfo.setStopIfFailFlg(m_check.getSelection());

		//正常に行われなかった場合後続モジュールを実行しないかのフラグ取得
		moduleInfo.setBackupIfExistFlg(m_rename.getSelection());

		moduleInfo.setPrecheckFlg(m_md5Check.getSelection());

		// 戻り値の変数名
		if (m_execReturnParamName.getText() != null) {
			moduleInfo.setExecReturnParamName(m_execReturnParamName.getText());
		}
		moduleInfo.getFileTransferVariableList().clear();
		moduleInfo.getFileTransferVariableList().addAll(m_chenge.getInputData());

		//設定の有効
		moduleInfo.setValidFlg(m_valid.getSelection());
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
				InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(this.m_managerName);
				wrapper.modifyInfraManagement(infraInfo);
				action += "(" + this.m_managerName + ")";
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
						Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module"), 
								action, Messages.getString("failed"), m_moduleId.getText() + "\n" + HinemosMessage.replace(e.getMessage())}));
			} catch (Exception e) {
				m_log.info("action() modifyInfraManagement : " + e.getMessage() + " (" + e.getClass().getName() + ")");
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module"), 
								action, Messages.getString("failed"), m_moduleId.getText() + "\n" + HinemosMessage.replace(e.getMessage())}));
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
