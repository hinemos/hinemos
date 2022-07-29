/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.dialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.openapitools.client.model.AddRpaManagementToolAccountRequest;
import org.openapitools.client.model.ModifyRpaManagementToolAccountRequest;
import org.openapitools.client.model.RpaManagementToolAccountResponse;
import org.openapitools.client.model.RpaManagementToolResponse;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.rpa.action.AddAccount;
import com.clustercontrol.rpa.action.GetAccount;
import com.clustercontrol.rpa.action.ModifyAccount;
import com.clustercontrol.rpa.util.RpaManagementToolEnum;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;

/**
 * シナリオタグ設定作成・変更ダイアログクラス
 *
 */
public class RpaManagementToolAccountDialog extends CommonDialog{
	// ----- static フィールド ----- //
	private static final String HTTP_PREFIX = "http://";
	private static final String HTTPS_PREFIX = "https://";

	// ログ
	private static Log m_log = LogFactory.getLog( RpaManagementToolAccountDialog.class );
	/** RPAスコープID */
	private String rpaScopeId = "";
	/** 変更用ダイアログ判別フラグ */
	private int mode;
	/** RPAスコープID */
	private Text rpaScopeIdText = null;
	/** RPAスコープ名 */
	private Text rpaScopeNameText = null;
	/** 説明 */
	private Text description = null;
	/** オーナーロールIDコンボボックス用コンポジット */
	private RoleIdListComposite roleIdListComposite = null;
	/** マネージャ名 */
	private String managerName = null;
	/** RPA管理ツール種別(コンボボックス) */
	private Combo m_comboRpaManagementToolType = null;
	/** マネージャ毎のRPA管理ツール種別 */
	private Map<String, List<RpaManagementToolResponse>> rpaManagementToolMap = new HashMap<>();
	/** URL(ラベル) */
	private Label urlLabel = null;
	/** URL */
	private Text urlText = null;
	/** アカウントID(ラベル) */
	private Label accountIdLabel = null;
	/** アカウントID */
	private Text accountIdText = null;
	/** パスワード(ラベル) */
	private Label passwordLabel = null;
	/** パスワード */
	private Text passwordText = null;
	/** テナント名(ラベル) */
	private Label tenantNameLabel = null;
	/** テナント名 */
	private Text tenantNameText = null;
	/** 表示名 */
	private Text displayNameText = null;
	/** プロキシ：有効無効 */
	private Button m_chkbxProxy = null;
	/** プロキシ：URL */
	private Text m_textProxyUrl = null;
	/** プロキシ：URL */
	private Text m_textProxyPort = null;
	/** プロキシ：ユーザ */
	private Text m_textProxyUser = null;

	/** プロキシ：パスワード */
	private Text m_textProxyPassword = null;

	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	// ----- 共通メンバ変数 ----- //
	private Shell shell = null;

	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public RpaManagementToolAccountDialog(Shell parent, String managerName, String rpaScopeId, int mode) {
		super(parent);
		this.managerName = managerName;
		this.rpaScopeId = rpaScopeId;
		this.mode = mode;
	}
	// ----- instance メソッド ----- //
	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.rpa.management.tool.account.create.modify"));
		GridData gridData = new GridData();
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 6;
		parent.setLayout(layout);
		
		/*
		 * マネージャ
		 */
		Label labelManager = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		labelManager.setLayoutData(gridData);
		labelManager.setText(Messages.getString("facility.manager") + " : ");
		if(this.mode == PropertyDefineConstant.MODE_MODIFY
				|| this.mode == PropertyDefineConstant.MODE_SHOW){
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, true);
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					// オーナーロールIDを更新
					roleIdListComposite.createRoleIdList(managerName);
					// RPA管理ツール種別を更新
					refreshComboRpaTool(managerName);
				}
			});
		}
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 4;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);

		if(this.managerName != null) {
			this.m_managerComposite.setText(this.managerName);
		}
		
		/*
		 * RPAスコープID
		 */

		//ラベル
		Label lblScenarioTagID = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblScenarioTagID.setLayoutData(gridData);
		lblScenarioTagID.setText(Messages.getString("RPA_SCOPE_ID") + " : ");
		//テキスト
		rpaScopeIdText = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		rpaScopeIdText.setLayoutData(gridData);
		rpaScopeIdText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * RPAスコープ名
		 */
		//ラベル
		Label lblScenarioTagName = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblScenarioTagName.setLayoutData(gridData);
		lblScenarioTagName.setText(Messages.getString("RPA_SCOPE_NAME") + " : ");
		//テキスト
		rpaScopeNameText = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		rpaScopeNameText.setLayoutData(gridData);
		rpaScopeNameText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * 説明
		 */
		//ラベル
		Label lblScenarioTagDescription = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblScenarioTagDescription.setLayoutData(gridData);
		lblScenarioTagDescription.setText(Messages.getString("description") + " : ");
		//テキスト
		description = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		description.setLayoutData(gridData);
		description.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * オーナーロールID
		 */
		Label labelRoleId = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_ADD
				|| this.mode == PropertyDefineConstant.MODE_COPY) {
			this.roleIdListComposite = new RoleIdListComposite(parent,
					SWT.NONE, this.managerName, true, Mode.OWNER_ROLE);
		} else {
			this.roleIdListComposite = new RoleIdListComposite(parent, SWT.NONE, this.managerName, false, Mode.OWNER_ROLE);
		}
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		roleIdListComposite.setLayoutData(gridData);

		/*
		 * RPA管理ツール種別
		 */
		// ラベル
		Label labelRpaManagementToolType = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRpaManagementToolType.setLayoutData(gridData);
		labelRpaManagementToolType.setText(Messages.getString("RPA_MANAGEMENT_TOOL_TYPE") + " : ");
		
		// コンボボックス
		this.m_comboRpaManagementToolType = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboRpaManagementToolType.setLayoutData(gridData);
		m_comboRpaManagementToolType.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				RpaManagementToolResponse rpaManagementTool = (RpaManagementToolResponse)m_comboRpaManagementToolType.getData(m_comboRpaManagementToolType.getText());
				if (rpaManagementTool != null) {
					refreshLabelDisplay(rpaManagementTool.getRpaManagementToolId());
				}
			}
		});
		refreshComboRpaTool(this.managerName);

		// ----- 接続情報 ----- //
		/*
		 * 接続情報グループ
		 */
		Group groupConnectionInfo = new Group(parent, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 6;
		groupConnectionInfo.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupConnectionInfo.setLayoutData(gridData);
		groupConnectionInfo.setText(Messages.getString("connection.info"));
		
		/*
		 * URL
		 */
		//ラベル
		urlLabel = new Label(groupConnectionInfo, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		urlLabel.setLayoutData(gridData);
		urlLabel.setText(Messages.getString("URL") + " : ");
		//テキスト
		urlText = new Text(groupConnectionInfo, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		urlText.setLayoutData(gridData);
		urlText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * アカウントID
		 */
		//ラベル
		this.accountIdLabel = new Label(groupConnectionInfo, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.accountIdLabel.setLayoutData(gridData);
		//テキスト
		this.accountIdText = new Text(groupConnectionInfo, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		accountIdText.setLayoutData(gridData);
		accountIdText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * パスワード
		 */
		//ラベル
		this.passwordLabel = new Label(groupConnectionInfo, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.passwordLabel.setLayoutData(gridData);
		//テキスト
		this.passwordText = new Text(groupConnectionInfo, SWT.BORDER | SWT.LEFT | SWT.PASSWORD);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		passwordText.setLayoutData(gridData);
		passwordText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});


		/*
		 * 表示名
		 */
		//ラベル
		Label dieplayNameLabel = new Label(groupConnectionInfo, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		dieplayNameLabel.setLayoutData(gridData);
		dieplayNameLabel.setText(Messages.getString("DISPLAY_NAME") + " : ");
		//テキスト
		this.displayNameText = new Text(groupConnectionInfo, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		displayNameText.setLayoutData(gridData);
		displayNameText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		/*
		 * テナント名
		 */
		// ラベル
		this.tenantNameLabel = new Label(groupConnectionInfo, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.tenantNameLabel.setLayoutData(gridData);
		this.tenantNameLabel.setText(Messages.getString("TENANT_NAME") + " : ");
		// テキスト
		this.tenantNameText = new Text(groupConnectionInfo, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.tenantNameText.setLayoutData(gridData);
		this.tenantNameText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ----- プロキシ情報 ----- //
		/*
		 * プロキシグループ
		 */
		Group groupProxy = new Group(groupConnectionInfo, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 6;
		groupProxy.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupProxy.setLayoutData(gridData);
		groupProxy.setText(Messages.getString("PROXY"));

		/*
		 * プロキシ:チェック
		 */
		// チェック
		m_chkbxProxy = new Button(groupProxy, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_chkbxProxy.setLayoutData(gridData);
		m_chkbxProxy.setText(Messages.getString("PROXY"));
		this.m_chkbxProxy.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e){
				update();
			}
		});

		// プロキシ：URL
		// ラベル
		
		Label label = new Label(groupProxy, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("URL") + " : ");

		// テキスト
		this.m_textProxyUrl = new Text(groupProxy, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textProxyUrl.setLayoutData(gridData);
		this.m_textProxyUrl.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});


		// プロキシ：ポート
		// ラベル
		label = new Label(groupProxy, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 20;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("port") + " : ");

		// テキスト
		this.m_textProxyPort = new Text(groupProxy, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textProxyPort.setLayoutData(gridData);
		this.m_textProxyPort.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		this.m_textProxyPort.addVerifyListener(new NumberVerifyListener(0, 65535));

		// 空白
		label = new Label(groupProxy, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blankuser", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// プロキシ：ユーザ名
		// ラベル
		label = new Label(groupProxy, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("user") + " : ");

		// テキスト
		this.m_textProxyUser = new Text(groupProxy, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textProxyUser.setLayoutData(gridData);
		this.m_textProxyUser.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupProxy, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// プロキシ：パスワード

		// ラベル
		label = new Label(groupProxy, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("password") + " : ");

		// テキスト
		this.m_textProxyPassword = new Text(groupProxy, SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.PASSWORD);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textProxyPassword.setLayoutData(gridData);
		this.m_textProxyPassword.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});


		Display calDisplay = shell.getDisplay();
		shell.setLocation((calDisplay.getBounds().width - shell.getSize().x) / 2,
				(calDisplay.getBounds().height - shell.getSize().y) / 2);
		
		// ダイアログを調整
		this.adjustDialog();
		//ダイアログにテンプレートセット詳細情報反映
		this.reflectAccount();
		// 必須入力項目を可視化
		this.update();

	}


	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(600, shell.getSize().y));

		// 画面中央に配置
		Display calAdjustDisplay = shell.getDisplay();
		shell.setLocation((calAdjustDisplay.getBounds().width - shell.getSize().x) / 2,
				(calAdjustDisplay.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示

		// RPAスコープID
		if("".equals(this.rpaScopeIdText.getText())){
			this.rpaScopeIdText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.rpaScopeIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// RPAスコープ名
		if("".equals(this.rpaScopeNameText.getText())){
			this.rpaScopeNameText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.rpaScopeNameText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// URL
		if(
			(!this.urlText.getText().startsWith(HTTP_PREFIX) || HTTP_PREFIX.equals(this.urlText.getText())) && 
			(!this.urlText.getText().startsWith(HTTPS_PREFIX) || HTTPS_PREFIX.equals(this.urlText.getText()))
		) {
			this.urlText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.urlText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// アカウントID
		if("".equals(this.accountIdText.getText())){
			this.accountIdText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.accountIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// パスワード
		if("".equals(this.passwordText.getText())){
			this.passwordText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.passwordText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 表示名
		if("".equals(this.displayNameText.getText())){
			this.displayNameText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.displayNameText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// テナント名
		if("".equals(this.tenantNameText.getText())){
			this.tenantNameText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.tenantNameText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// プロキシ情報
		this.m_textProxyUrl.setEnabled(this.m_chkbxProxy.getSelection());
		this.m_textProxyPort.setEnabled(this.m_chkbxProxy.getSelection());
		this.m_textProxyUser.setEnabled(this.m_chkbxProxy.getSelection());
		this.m_textProxyPassword.setEnabled(this.m_chkbxProxy.getSelection());

		if(this.m_textProxyUrl.isEnabled() && (
			(!this.m_textProxyUrl.getText().startsWith(HTTP_PREFIX) || HTTP_PREFIX.equals(this.m_textProxyUrl.getText())) && 
			(!this.m_textProxyUrl.getText().startsWith(HTTPS_PREFIX) || HTTPS_PREFIX.equals(this.m_textProxyUrl.getText()))
			)
		) {
			this.m_textProxyUrl.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textProxyUrl.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_textProxyPort.isEnabled() && "".equals(this.m_textProxyPort.getText().trim())){
			this.m_textProxyPort.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textProxyPort.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}


	}
	/**
	 * ダイアログの情報からアカウント情報を作成します。
	 *
	 */
	private AddRpaManagementToolAccountRequest createAccount() {
		AddRpaManagementToolAccountRequest inputData = new AddRpaManagementToolAccountRequest();
		
		inputData.setRpaScopeId(this.rpaScopeIdText.getText());
		inputData.setRpaScopeName(this.rpaScopeNameText.getText());
		inputData.setDescription(this.description.getText());
		inputData.setOwnerRoleId(this.roleIdListComposite.getText());
		inputData.setRpaManagementToolId(
				((RpaManagementToolResponse)this.m_comboRpaManagementToolType.getData(this.m_comboRpaManagementToolType.getText()))
				.getRpaManagementToolId());
		inputData.setUrl(this.urlText.getText());
		inputData.setAccountId(this.accountIdText.getText());
		inputData.setPassword(this.passwordText.getText());
		if (this.tenantNameText.isVisible()) {
			inputData.setTenantName(this.tenantNameText.getText());
		}
		inputData.setDisplayName(this.displayNameText.getText());
		inputData.setProxyFlg(this.m_chkbxProxy.getSelection());
		if (inputData.getProxyFlg()) {
			if (!this.m_textProxyUrl.getText().isEmpty()) {
				inputData.setProxyUrl(this.m_textProxyUrl.getText());
			}
			if (!this.m_textProxyPort.getText().isEmpty()) {
				inputData.setProxyPort(Integer.valueOf(this.m_textProxyPort.getText()));
			}
			if (!this.m_textProxyUser.getText().isEmpty()) {
				inputData.setProxyUser(this.m_textProxyUser.getText());
			}
			if (!this.m_textProxyPassword.getText().isEmpty()) {
				inputData.setProxyPassword(this.m_textProxyPassword.getText());
			}
		}
		return inputData;
	}

	/**
	 * ダイアログにアカウント情報を反映します。
	 *
	 */
	private void reflectAccount() {
		// 初期値を設定
		setInitialValue();

		// 初期表示
		AddRpaManagementToolAccountRequest accountInfo = null;
		if(mode == PropertyDefineConstant.MODE_MODIFY
				|| mode == PropertyDefineConstant.MODE_COPY
				|| mode == PropertyDefineConstant.MODE_SHOW){
			// 変更、コピーの場合、情報取得
			RpaManagementToolAccountResponse getAccountInfo = new GetAccount().getRpaManagementToolAccount(this.managerName, this.rpaScopeId);
			try {
				// DTO変換
				if (getAccountInfo != null) {
					accountInfo = new AddRpaManagementToolAccountRequest();
					RestClientBeanUtil.convertBean(getAccountInfo, accountInfo);
				}
			} catch (HinemosUnknown e) {
				m_log.warn("getRpaManagementToolAccount(), " + e.getMessage(), e);
			}
		}
		
		// 変更、コピーの場合情報セット
		if (accountInfo != null && (this.mode == PropertyDefineConstant.MODE_MODIFY || mode == PropertyDefineConstant.MODE_COPY)) {
			// RPAスコープID
			this.rpaScopeId = accountInfo.getRpaScopeId();
			this.rpaScopeIdText.setText(accountInfo.getRpaScopeId());
			if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
				//RPAスコープIDは変更不可
				this.rpaScopeIdText.setEnabled(false);
			}

			//RPAスコープ名
			this.rpaScopeNameText.setText(accountInfo.getRpaScopeName());

			if(accountInfo.getDescription() != null){
				this.description.setText((HinemosMessage.replace(accountInfo.getDescription())));
			}
	
			// オーナーロールID
			this.roleIdListComposite.setText(accountInfo.getOwnerRoleId());
			if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
				//オーナーロールIDは変更不可
				this.roleIdListComposite.setEnabled(false);
			}

			// RPA管理ツール種別
			String toolId = accountInfo.getRpaManagementToolId(); 
			List<RpaManagementToolResponse> toolList = rpaManagementToolMap.get(this.managerName);
			this.m_comboRpaManagementToolType.select(
				this.m_comboRpaManagementToolType.indexOf(
					toolList.stream()
					.filter(tool -> tool.getRpaManagementToolId().equals(toolId))
					.map(RpaManagementToolResponse::getRpaManagementToolName)
					.findAny()
					.orElse("")
				)
			);
			// RPA管理ツール種別は変更不可
			if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
				this.m_comboRpaManagementToolType.setEnabled(false);
			}
			
			// URL
			this.urlText.setText(accountInfo.getUrl());			
			// アカウントID
			this.accountIdText.setText(accountInfo.getAccountId());
			// パスワード
			this.passwordText.setText(accountInfo.getPassword());
			// テナント名
			if (accountInfo.getTenantName() != null) {
				this.tenantNameText.setText(accountInfo.getTenantName());
			}
			// 表示名
			this.displayNameText.setText(accountInfo.getDisplayName());
			// プロキシ
			this.m_chkbxProxy.setSelection(accountInfo.getProxyFlg());
			if (accountInfo.getProxyUrl() != null) {
				this.m_textProxyUrl.setText(accountInfo.getProxyUrl());
			}
			if (accountInfo.getProxyPort() != null) {
				this.m_textProxyPort.setText(String.valueOf(accountInfo.getProxyPort()));
			}
			if (accountInfo.getProxyUser() != null) {
				this.m_textProxyUser.setText(accountInfo.getProxyUser());
			}
			if (accountInfo.getProxyPassword() != null) {
				this.m_textProxyPassword.setText(accountInfo.getProxyPassword());
			}
		}
	}
	
	/**
	 * 初期値を設定します。
	 */
	private void setInitialValue() {
		this.m_comboRpaManagementToolType.select(0);
		this.m_chkbxProxy.setSelection(false);
		this.m_textProxyUrl.setText(HTTP_PREFIX);
		this.m_textProxyPort.setText("8080");
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
		AddRpaManagementToolAccountRequest info = createAccount(); 
		String managerName = this.m_managerComposite.getText();
		try {
			if(mode == PropertyDefineConstant.MODE_ADD || mode == PropertyDefineConstant.MODE_COPY){
				// 作成、コピーの場合
				result = new AddAccount().add(managerName, info);
			} else if (mode == PropertyDefineConstant.MODE_MODIFY){
				// 変更の場合
				ModifyRpaManagementToolAccountRequest modifyInfoReq = new ModifyRpaManagementToolAccountRequest();
				RestClientBeanUtil.convertBean(info, modifyInfoReq);
				result = new ModifyAccount().modify(managerName, info.getRpaScopeId(), modifyInfoReq);
			}
		}  catch (HinemosUnknown e) {
			m_log.error("action() Failed to convert RpaManagementToolAccount");
		}
		return result;
	}
	
	/**
	 * マネージャ名に応じてRPA管理ツールコンボボックスを更新
	 */
	private void refreshComboRpaTool(String managerName) {
		List<RpaManagementToolResponse> toolList;
		// 格納済マップ、またはAPIからRPA管理ツール種別を取得
		if (!rpaManagementToolMap.containsKey(managerName)) {
			try {
				RpaRestClientWrapper rpaWrapper = RpaRestClientWrapper.getWrapper(managerName);
				rpaManagementToolMap.put(managerName, rpaWrapper.getRpaManagementTool());
			} catch (HinemosException e) {
				// エンタープライズ機能が無効の場合は無視する
				if(UrlNotFound.class.equals(e.getCause().getClass())) {
					return;
				}
				m_log.warn(e.getMessage(), e);
			}
		}
		toolList = rpaManagementToolMap.get(managerName);
		this.m_comboRpaManagementToolType.removeAll();
		if (toolList != null) {
			for (RpaManagementToolResponse tool : toolList) {
				this.m_comboRpaManagementToolType.add(tool.getRpaManagementToolName());
				this.m_comboRpaManagementToolType.setData(tool.getRpaManagementToolName(), tool);
			}
		}
	}
	
	/**
	 * RPA管理ツール種別に応じて各項目表示を更新
	 */
	private void refreshLabelDisplay(String rpaManagementToolId) {
		if (accountIdLabel == null || passwordLabel == null
				|| this.tenantNameLabel == null || this.tenantNameText == null 
				|| this.urlLabel == null || this.urlText == null) {
			return;
		}
		
		RpaManagementToolEnum toolEnum = RpaManagementToolEnum.valueOf(rpaManagementToolId);
		
		// アカウント、パスワード(ラベル)
		this.accountIdLabel.setText(Messages.getString(toolEnum.getAccountIdProperty()) + " : ");
		this.passwordLabel.setText(Messages.getString(toolEnum.getPasswordProperty())+ " : ");
		
		// テナント名
		this.tenantNameLabel.setVisible(toolEnum.isTenantValid());
		this.tenantNameText.setVisible(toolEnum.isTenantValid());
	}

	@Override
	protected ValidateResult validate() {
		return validateEndpoint(this.m_managerComposite.getText());
	}

	@Override
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		// RpaRestEndpointsにはcheckPublishが存在しない
		// どのEndpointでも内容は同じなのでUtilityを使用する
		return UtilityRestClientWrapper.getWrapper(managerName);
	}
}
