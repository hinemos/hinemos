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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
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
import com.clustercontrol.ws.infra.ReferManagementModuleInfo;

/**
 * 環境構築[参照環境構築モジュールの作成・変更]ダイアログクラスです。
 *
 * @version 6.1.0
 */
public class ReferManagementModuleDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( ReferManagementModuleDialog.class );
	/** 環境構築[構築]情報*/
	private InfraManagementInfo infraInfo ;
	private ReferManagementModuleInfo moduleInfo;
	
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
	/** 参照環境構築ID用コンボボックス */
	private Combo m_comboReferManagementId = null;
	/** モジュールの有効用ボタン*/
	private Button m_validFlg = null;
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
	public ReferManagementModuleDialog(Shell parent, String managerName, String managementId) {
		super(parent);
		this.managerName = managerName;
		this.managementId = managementId;
	}
	public ReferManagementModuleDialog(Shell parent, String managerName, String managementId, String moduleId, int mode) {
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
				Messages.getString("dialog.infra.module.refer.management"));
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
		Label label = null;

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = DIALOG_WIDTH;
		composite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);

		/*
		 * モジュールID
		 */
		label = new Label(composite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setText(Messages.getString("infra.module.id") + " : ");
		label.setLayoutData(gridData);
		m_moduleId = new Text(composite, SWT.BORDER);
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
		label = new Label(composite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setText(Messages.getString("infra.module.name") + " : ");
		label.setLayoutData(gridData);
		this.m_moduleName = new Text(composite, SWT.BORDER);
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
		 * 参照環境構築ID
		 */
		label = new Label(composite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setText(Messages.getString("infra.module.refer.management") + " : ");
		label.setLayoutData(gridData);
		m_comboReferManagementId = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_comboReferManagementId", m_comboReferManagementId);
		gridData = new GridData();
		gridData.horizontalSpan = FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboReferManagementId.setLayoutData(gridData);

		// 空白
		label = new Label(composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * ラインを引く
		 */
		Label line = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = DIALOG_WIDTH;
		line.setLayoutData(gridData);

		/*
		 * 有効ボタン
		 */
		m_validFlg = new Button(composite, SWT.CHECK);
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
		//参照環境構築ID
		if("".equals(this.m_comboReferManagementId.getText())){
			this.m_comboReferManagementId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboReferManagementId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ダイアログにモジュール情報を反映します。
	 *
	 * @see com.clustercontrol.infra.bean.InfraManagementInfo
	 */
	private void setInputData() {
		InfraManagementInfo info = null;
		ReferManagementModuleInfo module = null;
		this.m_comboReferManagementId.removeAll();
		try {
			InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(this.managerName);
			// 環境構築情報取得
			info = wrapper.getInfraManagement(this.managementId);
			// 参照環境設定IDコンボボックス設定
			List<String> referManagementIdList = wrapper.getReferManagementIdList(info.getOwnerRoleId());
			if (referManagementIdList != null) {
				for (String referManagementId : referManagementIdList) {
					m_comboReferManagementId.add(referManagementId);
				}
			}
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
			throw new InternalError(e.getMessage());
		} catch (Exception e) {
			// Managerとの通信で予期せぬ内部エラーが発生したことを通知する
			m_log.warn("customizeDialog(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			throw new InternalError(e.getMessage());
		}

		if (mode == PropertyDefineConstant.MODE_ADD | mode == PropertyDefineConstant.MODE_COPY) {
			// 作成・コピーの場合新規モジュールを末尾に追加
			moduleInfo = new ReferManagementModuleInfo();
			List<InfraModuleInfo> modules = info.getModuleList();
			modules.add(moduleInfo);
		} else if (mode == PropertyDefineConstant.MODE_MODIFY){
			// 変更の場合モジュールを取得
			for(InfraModuleInfo tmpModule: info.getModuleList()){
				if(tmpModule.getModuleId().equals(moduleId)){
					moduleInfo = (ReferManagementModuleInfo) tmpModule;
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
			module = (ReferManagementModuleInfo)info.getModuleList().get(index);
			//	モジュールID
			m_moduleId.setText(module.getModuleId());
			if (mode == PropertyDefineConstant.MODE_MODIFY) {
				m_moduleId.setEnabled(false);
			}
			//	モジュール名
			m_moduleName.setText(module.getName());
			//	参照環境設定ID
			if (module.getReferManagementId() != null) {
				m_comboReferManagementId.setText(module.getReferManagementId());
			}
			//	設定の有効･無効
			m_validFlg.setSelection(module.isValidFlg());

		} else {
			// 作成の場合(default設定)
			m_validFlg.setSelection(true);
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

		//参照環境構築IDの取得
		if (m_comboReferManagementId.getText() != null || !m_comboReferManagementId.getText().isEmpty()) {
			moduleInfo.setReferManagementId(m_comboReferManagementId.getText());
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
		if (m_comboReferManagementId.getText() == null || "".equals(m_comboReferManagementId.getText())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.infra.specify.item",
							new Object[]{Messages.getString("infra.module.refer.management")}));
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
