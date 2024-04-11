/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.AddJobLinkSendSettingRequest;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.JobLinkSendInfoResponse;
import org.openapitools.client.model.JobLinkSendSettingResponse;
import org.openapitools.client.model.JobLinkSendSettingResponse.ProtocolEnum;
import org.openapitools.client.model.ModifyJobLinkSendSettingRequest;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobMasterDuplicate;
import com.clustercontrol.jobmanagement.bean.JobLinkConstant;
import com.clustercontrol.jobmanagement.bean.JobLinkSendProtocol;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * ジョブ連携送信設定ダイアログです。
 *
 */
public class JobLinkSendSettingDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( JobLinkSendSettingDialog.class );

	/** ジョブ連携送信設定情報 */
	private JobLinkSendSettingResponse m_jobLinkSendSetting;

	/** ジョブ連携送信設定ID用テキスト */
	private Text m_txtJoblinkSendSettingId = null;
	/** 説明 */
	private Text m_txtDescription = null;
	/** スコープ用テキスト */
	private Text m_txtScope = null;
	/** スコープ参照用ボタン */
	private Button m_btnScopeSelect = null;
	/** 全てのノードで用ラジオボタン */
	private Button m_allNode = null;
	/** いずれかのノードで用ラジオボタン */
	private Button m_eitherNode = null;
	/** 送信先プロトコル(HTTP) */
	private Button m_radioProtocol_http = null;
	/** 送信先プロトコル(HTTPS) */
	private Button m_radioProtocol_https = null;
	/** 送信先ポート */
	private Text m_txtPort = null;
	/** HinemosユーザID用テキスト */
	private Text m_txtUserId = null;
	/** Hinemosパスワード用テキスト */
	private Text m_txtPassword = null;
	/** HTTPプロキシを使用する */
	private Button m_checkProxy = null;
	/** HTTPプロキシホスト */
	private Text m_txtProxyHost = null;
	/** HTTPプロキシポート */
	private Text m_txtProxyPort = null;
	/** HTTPプロキシユーザ */
	private Text m_txtProxyUser = null;
	/** HTTPプロキシパスワード */
	private Text m_txtProxyPassword = null;

	/** オーナーロールID用テキスト */
	private RoleIdListComposite m_cmpOwnerRoleId = null;

	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;
	/**
	 * 作成：MODE_ADD = 0;
	 * 変更：MODE_MODIFY = 1;
	 * 複製：MODE_COPY = 3;
	 * ジョブ履歴からの参照：MODE_SHOW = 2;
	 * */
	private int m_mode = PropertyDefineConstant.MODE_ADD;

	/** 選択されたスコープのファシリティID。 */
	private String m_facilityId = null;

	/** ジョブ連携送信設定ID */
	private String m_joblinkSendSettingId;

	/** マネージャ名 */
	private String m_managerName;

	/**
	 * コンストラクタ
	 */
	public JobLinkSendSettingDialog(Shell parent, String managerName, String joblinkSendSettingId, int mode){
		super(parent);
		this.m_managerName = managerName;
		this.m_joblinkSendSettingId = joblinkSendSettingId;
		this.m_mode = mode;
	}

	/**
	 * コンストラクタ（ジョブ履歴からの遷移時）
	 */
	public JobLinkSendSettingDialog(
			Shell parent,
			String managerName,
			JobLinkSendInfoResponse jobLinkSendInfo){
		super(parent);
		this.m_managerName = managerName;
		this.m_jobLinkSendSetting = new JobLinkSendSettingResponse();
		if (jobLinkSendInfo != null) {
			this.m_joblinkSendSettingId = jobLinkSendInfo.getJoblinkSendSettingId();
			this.m_jobLinkSendSetting.setJoblinkSendSettingId(this.m_joblinkSendSettingId);
			this.m_jobLinkSendSetting.setScope(jobLinkSendInfo.getScope());
			this.m_jobLinkSendSetting.setProcessMode(
				JobLinkSendSettingResponse.ProcessModeEnum.valueOf(jobLinkSendInfo.getProcessingMethod().name()));
			this.m_jobLinkSendSetting.setProtocol(
				JobLinkSendSettingResponse.ProtocolEnum.valueOf(jobLinkSendInfo.getProtocol().name()));
			this.m_jobLinkSendSetting.setPort(jobLinkSendInfo.getPort());
			this.m_jobLinkSendSetting.setHinemosUserId(jobLinkSendInfo.getHinemosUserId());
			this.m_jobLinkSendSetting.setHinemosPassword(jobLinkSendInfo.getHinemosPassword());
			this.m_jobLinkSendSetting.setProxyFlg(jobLinkSendInfo.getProxyFlg());
			this.m_jobLinkSendSetting.setProxyHost(jobLinkSendInfo.getProxyHost());
			this.m_jobLinkSendSetting.setProxyPort(jobLinkSendInfo.getProxyPort());
			this.m_jobLinkSendSetting.setProxyUser(jobLinkSendInfo.getProxyUser());
			this.m_jobLinkSendSetting.setProxyPassword(jobLinkSendInfo.getProxyPassword());
		}
		this.m_mode = PropertyDefineConstant.MODE_SHOW;
	}

	@Override
	protected void customizeDialog(Composite parent) {
		Label label = null;
		int labelWidth = 140;

		// タイトル
		parent.getShell().setText(Messages.get("dialog.joblinksend.setting"));

		// ベースレイアウト
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);

		// Composite
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		// マネージャ（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.get("facility.manager") + " : ");
		label.setLayoutData(new GridData(labelWidth, SizeConstant.SIZE_LABEL_HEIGHT));

		// マネージャ（テキスト）
		if(this.m_mode == PropertyDefineConstant.MODE_ADD
				|| this.m_mode == PropertyDefineConstant.MODE_COPY) {
			this.m_managerComposite = new ManagerListComposite(composite, SWT.NONE, true);
		} else {
			this.m_managerComposite = new ManagerListComposite(composite, SWT.NONE, false);
		}
		this.m_managerComposite.setLayoutData(new GridData());
		((GridData)this.m_managerComposite.getLayoutData()).widthHint = 227;

		if(this.m_managerName != null) {
			this.m_managerComposite.setText(this.m_managerName);
		}
		if(this.m_mode == PropertyDefineConstant.MODE_ADD
				|| this.m_mode == PropertyDefineConstant.MODE_COPY) {
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_managerName = m_managerComposite.getText();
					m_cmpOwnerRoleId.createRoleIdList(m_managerName);
					m_txtScope.setText("");
					m_facilityId = null;
				}
			});
		}

		// ジョブ連携送信設定ID（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.get("joblink.send.setting.id") + " : ");
		label.setLayoutData(new GridData(labelWidth, SizeConstant.SIZE_LABEL_HEIGHT));

		// ジョブ連携送信設定ID（テキスト）
		m_txtJoblinkSendSettingId = new Text(composite, SWT.BORDER);
		m_txtJoblinkSendSettingId.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		m_txtJoblinkSendSettingId.addVerifyListener(new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		m_txtJoblinkSendSettingId.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		if(this.m_mode == PropertyDefineConstant.MODE_MODIFY){
			// 変更時は変更不可とする
			this.m_txtJoblinkSendSettingId.setEditable(false);
		}

		// 説明（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.get("description") + " : ");
		label.setLayoutData(new GridData(labelWidth, SizeConstant.SIZE_LABEL_HEIGHT));

		// 説明（テキスト）
		m_txtDescription = new Text(composite, SWT.BORDER);
		m_txtDescription.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		m_txtDescription.addVerifyListener(new StringVerifyListener(DataRangeConstant.VARCHAR_256));
		m_txtDescription.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// オーナーロール（ラベル）
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("owner.role.id") + " : ");
		label.setLayoutData(new GridData(140, SizeConstant.SIZE_LABEL_HEIGHT));

		// オーナーロールID（テキスト）
		if (this.m_mode == PropertyDefineConstant.MODE_ADD
				|| this.m_mode == PropertyDefineConstant.MODE_COPY) {
			this.m_cmpOwnerRoleId = new RoleIdListComposite(composite
					, SWT.NONE, this.m_managerName, true, Mode.OWNER_ROLE);
			this.m_cmpOwnerRoleId.getComboRoleId().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_txtScope.setText("");
					m_facilityId = null;
					update();
				}
			});
		} else {
			this.m_cmpOwnerRoleId = new RoleIdListComposite(composite
					, SWT.NONE, this.m_managerName, false, Mode.OWNER_ROLE);
		}
		this.m_cmpOwnerRoleId.setLayoutData(new GridData());
		((GridData)this.m_cmpOwnerRoleId.getLayoutData()).widthHint = 227;

		// 送信先設定（グループ）
		Group settingGroup = new Group(composite, SWT.NONE);
		settingGroup.setLayoutData(new GridData());
		((GridData)settingGroup.getLayoutData()).horizontalSpan = 2;
		settingGroup.setText(Messages.getString("destination.setting"));
		settingGroup.setLayout(new GridLayout(3, false));

		// スコープ（ラベル）
		label = new Label(settingGroup, SWT.NONE);
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("destination.scope") + " : ");

		// スコープ（テキスト）
		this.m_txtScope =  new Text(settingGroup, SWT.READ_ONLY | SWT.BORDER);
		this.m_txtScope.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtScope.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// スコープ参照（ボタン）
		this.m_btnScopeSelect = new Button(settingGroup, SWT.NONE);
		this.m_btnScopeSelect.setLayoutData(new GridData(40, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnScopeSelect.setText(Messages.getString("refer"));
		this.m_btnScopeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell, m_managerName, m_cmpOwnerRoleId.getText());
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

		// スコープ処理(ラベル)
		Label scopeProcLabel = new Label(settingGroup, SWT.NONE);
		scopeProcLabel.setText(Messages.getString("destination.scope.success.any.node"));
		scopeProcLabel.setLayoutData(new GridData(300, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)scopeProcLabel.getLayoutData()).horizontalSpan = 3;

		// スコープ処理：全てのノード
		this.m_allNode = new Button(settingGroup, SWT.RADIO);
		this.m_allNode.setText(Messages.getString("scope.all.nodes"));
		this.m_allNode.setLayoutData(
				new GridData(150,SizeConstant.SIZE_BUTTON_HEIGHT));
		
		// スコープ処理：いずれかのノード
		this.m_eitherNode = new Button(settingGroup, SWT.RADIO);
		this.m_eitherNode.setText(Messages.getString("scope.either.nodes"));
		this.m_eitherNode.setLayoutData(
				new GridData(250, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_eitherNode.getLayoutData()).horizontalSpan = 2;

		// 送信先詳細設定（グループ）
		Group advanceGroup = new Group(composite, SWT.NONE);
		advanceGroup.setLayoutData(new GridData());
		((GridData)advanceGroup.getLayoutData()).horizontalSpan = 2;
		advanceGroup.setText(Messages.getString("destination.setting.advance"));
		advanceGroup.setLayout(new GridLayout(2, false));

		// 送信先プロトコル（ラベル）
		label = new Label(advanceGroup, SWT.NONE);
		label.setLayoutData(new GridData(200, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("destination.protocol") + " : ");

		// 送信先プロトコル（Composite）
		Composite protocolComposite = new Composite(advanceGroup, SWT.BORDER);
		protocolComposite.setLayout(new GridLayout(2, true));

		// 送信先プロトコル(HTTP)ラジオボタン
		this.m_radioProtocol_http = new Button(protocolComposite, SWT.RADIO);
		this.m_radioProtocol_http.setText(JobLinkSendProtocol.HTTP.toString());
		this.m_radioProtocol_http.setSelection(true);

		// 送信先プロトコル(HTTPS)ラジオボタン
		this.m_radioProtocol_https = new Button(protocolComposite, SWT.RADIO);
		this.m_radioProtocol_https.setText(JobLinkSendProtocol.HTTPS.toString());
		this.m_radioProtocol_https.setSelection(false);

		// 送信先ポート（ラベル）
		label = new Label(advanceGroup, SWT.NONE);
		label.setLayoutData(new GridData(200, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("destination.port") + " : ");

		// 送信先ポート（テキスト）
		this.m_txtPort =  new Text(advanceGroup, SWT.BORDER);
		this.m_txtPort.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtPort.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// HinemosユーザID（ラベル）
		label = new Label(advanceGroup, SWT.NONE);
		label.setLayoutData(new GridData(200, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("hinemos.user") + " : ");

		// HinemosユーザID（テキスト）
		this.m_txtUserId =  new Text(advanceGroup, SWT.BORDER);
		this.m_txtUserId.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtUserId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// Hinemosパスワード（ラベル）
		label = new Label(advanceGroup, SWT.NONE);
		label.setLayoutData(new GridData(200, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("hinemos.password") + " : ");

		// Hinemosパスワード（テキスト）
		this.m_txtPassword =  new Text(advanceGroup, SWT.BORDER | SWT.PASSWORD);
		this.m_txtPassword.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtPassword.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// HTTPプロキシを使用する（チェックボックス）
		this.m_checkProxy = new Button(composite, SWT.CHECK);
		this.m_checkProxy.setText(Messages.getString("proxy.connection.enable"));
		this.m_checkProxy.setLayoutData(new GridData(300, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_checkProxy.getLayoutData()).horizontalSpan = 2;
		this.m_checkProxy.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				m_txtProxyHost.setEditable(check.getSelection());
				m_txtProxyPort.setEditable(check.getSelection());
				m_txtProxyUser.setEditable(check.getSelection());
				m_txtProxyPassword.setEditable(check.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// HTTPプロキシ（Composite）
		Composite proxyComposite = new Composite(composite, SWT.BORDER);
		proxyComposite.setLayoutData(new GridData());
		((GridData)proxyComposite.getLayoutData()).horizontalSpan = 2;
		proxyComposite.setLayout(new GridLayout(2, false));

		// HTTPプロキシホスト（ラベル）
		label = new Label(proxyComposite, SWT.NONE);
		label.setLayoutData(new GridData(200, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("proxy.connection.host") + " : ");

		// HTTPプロキシホスト（テキスト）
		this.m_txtProxyHost =  new Text(proxyComposite, SWT.BORDER);
		this.m_txtProxyHost.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtProxyHost.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// HTTPプロキシポート（ラベル）
		label = new Label(proxyComposite, SWT.NONE);
		label.setLayoutData(new GridData(200, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("proxy.connection.port") + " : ");

		// HTTPプロキシポート（テキスト）
		this.m_txtProxyPort =  new Text(proxyComposite, SWT.BORDER);
		this.m_txtProxyPort.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtProxyPort.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// HTTPプロキシユーザ（ラベル）
		label = new Label(proxyComposite, SWT.NONE);
		label.setLayoutData(new GridData(200, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("proxy.connection.user") + " : ");

		// HTTPプロキシユーザ（テキスト）
		this.m_txtProxyUser =  new Text(proxyComposite, SWT.BORDER);
		this.m_txtProxyUser.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtProxyUser.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// HTTPプロキシパスワード（ラベル）
		label = new Label(proxyComposite, SWT.NONE);
		label.setLayoutData(new GridData(200, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("proxy.connection.password") + " : ");

		// HTTPプロキシパスワード（テキスト）
		this.m_txtProxyPassword =  new Text(proxyComposite, SWT.BORDER | SWT.PASSWORD);
		this.m_txtProxyPassword.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtProxyPassword.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 表示調整
		adjustPosition(550);

		reflectJobLinkSendSetting();

		update();
	}

	/**
	 * ダイアログにジョブ連携送信設定を反映します。
	 *
	 */
	private void reflectJobLinkSendSetting() {
		// 初期値設定
		m_txtJoblinkSendSettingId.setText("");
		m_txtDescription.setText("");
		m_txtScope.setText("");
		m_allNode.setSelection(true);
		m_eitherNode.setSelection(false);
		m_radioProtocol_http.setSelection(true);
		m_radioProtocol_https.setSelection(false);
		m_txtPort.setText(String.valueOf(JobLinkConstant.SEND_SETTING_PORT_DEFAUT));
		m_txtUserId.setText("");
		m_txtPassword.setText("");
		m_checkProxy.setSelection(false);
		m_txtProxyHost.setText("");
		m_txtProxyPort.setText("");
		m_txtProxyUser.setText("");
		m_txtProxyPassword.setText("");

		//マネージャよりジョブ連携送信設定情報を取得する
		if(this.m_mode == PropertyDefineConstant.MODE_SHOW){
			// ジョブ履歴からの遷移の場合はコンストラクタで設定済み
		} else if(this.m_mode == PropertyDefineConstant.MODE_MODIFY
				|| this.m_mode == PropertyDefineConstant.MODE_COPY){
			try {
				JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(m_managerName);
				this.m_jobLinkSendSetting = wrapper.getJobLinkSendSetting(m_joblinkSendSettingId);
			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("getJobSchedule(), " + e.getMessage(), e);
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
			if (this.m_jobLinkSendSetting == null) {
				throw new InternalError("JobLinkSendSetting is null");
			}
		} else {
			this.m_jobLinkSendSetting = new JobLinkSendSettingResponse();
		}
		if (m_jobLinkSendSetting.getJoblinkSendSettingId() != null) {
			m_txtJoblinkSendSettingId.setText(m_jobLinkSendSetting.getJoblinkSendSettingId());
		}
		if (m_jobLinkSendSetting.getDescription() != null) {
			m_txtDescription.setText(m_jobLinkSendSetting.getDescription());
		}
		if (m_jobLinkSendSetting.getOwnerRoleId() != null) {
			m_cmpOwnerRoleId.setText(m_jobLinkSendSetting.getOwnerRoleId());
		}
		m_facilityId = m_jobLinkSendSetting.getFacilityId();
		if (m_jobLinkSendSetting.getScope() != null) { 
			m_txtScope.setText(HinemosMessage.replace(m_jobLinkSendSetting.getScope()));
		}
		if (m_jobLinkSendSetting.getProcessMode() != null) {
			if (m_jobLinkSendSetting.getProcessMode() == JobLinkSendSettingResponse.ProcessModeEnum.ALL_NODE) {
				m_allNode.setSelection(true);
				m_eitherNode.setSelection(false);
			} else {
				m_allNode.setSelection(false);
				m_eitherNode.setSelection(true);
			}
		}
		if (m_jobLinkSendSetting.getProtocol() != null) {
			if (m_jobLinkSendSetting.getProtocol() == ProtocolEnum.HTTP) {
				m_radioProtocol_http.setSelection(true);
				m_radioProtocol_https.setSelection(false);
			} else {
				m_radioProtocol_http.setSelection(false);
				m_radioProtocol_https.setSelection(true);
			}
		}
		if (m_jobLinkSendSetting.getPort() != null) {
			m_txtPort.setText(Integer.toString(m_jobLinkSendSetting.getPort()));
		}
		if (m_jobLinkSendSetting.getHinemosUserId() != null) {
			m_txtUserId.setText(m_jobLinkSendSetting.getHinemosUserId());
		}
		if (m_jobLinkSendSetting.getHinemosPassword() != null) {
			m_txtPassword.setText(m_jobLinkSendSetting.getHinemosPassword());
		}
		if (m_jobLinkSendSetting.getProxyFlg() != null) {
			m_checkProxy.setSelection(m_jobLinkSendSetting.getProxyFlg());
		}
		if (m_jobLinkSendSetting.getProxyHost() != null) {
			m_txtProxyHost.setText(m_jobLinkSendSetting.getProxyHost());
		}
		if (m_jobLinkSendSetting.getProxyPort() != null) {
			m_txtProxyPort.setText(Integer.toString(m_jobLinkSendSetting.getProxyPort()));
		}
		if (m_jobLinkSendSetting.getProxyUser() != null) {
			m_txtProxyUser.setText(m_jobLinkSendSetting.getProxyUser());
		}
		if (m_jobLinkSendSetting.getProxyPassword() != null) {
			m_txtProxyPassword.setText(m_jobLinkSendSetting.getProxyPassword());
		}
		// 編集可否設定
		if (this.m_mode == PropertyDefineConstant.MODE_SHOW) {
			// ジョブ履歴からの遷移の場合は編集不可
			m_txtJoblinkSendSettingId.setEditable(false);
			m_txtDescription.setEditable(false);
			m_txtScope.setEditable(false);
			m_btnScopeSelect.setEnabled(false);
			m_allNode.setEnabled(false);
			m_eitherNode.setEnabled(false);
			m_radioProtocol_http.setEnabled(false);
			m_radioProtocol_https.setEnabled(false);
			m_txtPort.setEditable(false);
			m_txtUserId.setEditable(false);
			m_txtPassword.setEditable(false);
			m_checkProxy.setEnabled(false);
			m_txtProxyHost.setEditable(false);
			m_txtProxyPort.setEditable(false);
			m_txtProxyUser.setEditable(false);
			m_txtProxyPassword.setEditable(false);
		} else {
			// 編集可否設定
			m_txtProxyHost.setEditable(m_checkProxy.getSelection());
			m_txtProxyPort.setEditable(m_checkProxy.getSelection());
			m_txtProxyUser.setEditable(m_checkProxy.getSelection());
			m_txtProxyPassword.setEditable(m_checkProxy.getSelection());
		}
	}

	@Override
	protected String getOkButtonText() {
		if(this.m_mode == PropertyDefineConstant.MODE_SHOW){
			return null;
		} else {
			return Messages.get("register");
		}
	}

	@Override
	protected String getCancelButtonText() {
		return Messages.get("cancel");
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		// 入力値生成
		m_jobLinkSendSetting.setJoblinkSendSettingId(m_txtJoblinkSendSettingId.getText());
		m_jobLinkSendSetting.setDescription(m_txtDescription.getText());
		m_jobLinkSendSetting.setFacilityId(m_facilityId);
		if (m_allNode.getSelection()) {
			m_jobLinkSendSetting.setProcessMode(JobLinkSendSettingResponse.ProcessModeEnum.ALL_NODE);
		} else {
			m_jobLinkSendSetting.setProcessMode(JobLinkSendSettingResponse.ProcessModeEnum.RETRY);
		}
		if (m_radioProtocol_http.getSelection()) {
			m_jobLinkSendSetting.setProtocol(ProtocolEnum.HTTP);
		} else {
			m_jobLinkSendSetting.setProtocol(ProtocolEnum.HTTPS);
		}
		try {
			m_jobLinkSendSetting.setPort(Integer.parseInt(m_txtPort.getText()));
		} catch (NumberFormatException e) {
			ValidateResult validateResult = new ValidateResult();
			validateResult.setValid(false);
			validateResult.setID(Messages.getString("message.hinemos.1"));
			validateResult.setMessage(Messages.getString("message.common.1", 
					new String[]{Messages.getString("destination.port")}));
			return validateResult;
		}
		m_jobLinkSendSetting.setHinemosUserId(m_txtUserId.getText());
		m_jobLinkSendSetting.setHinemosPassword(m_txtPassword.getText());
		m_jobLinkSendSetting.setProxyFlg(m_checkProxy.getSelection());
		m_jobLinkSendSetting.setProxyHost(m_txtProxyHost.getText());
		if (!m_jobLinkSendSetting.getProxyFlg() && m_txtProxyPort.getText().isEmpty()) {
			m_jobLinkSendSetting.setProxyPort(null);
		} else {
			try {
				m_jobLinkSendSetting.setProxyPort(Integer.parseInt(m_txtProxyPort.getText()));
			} catch (NumberFormatException e) {
				ValidateResult validateResult = new ValidateResult();
				validateResult.setValid(false);
				validateResult.setID(Messages.getString("message.hinemos.1"));
				validateResult.setMessage(Messages.getString("message.common.1", 
						new String[]{Messages.getString("proxy.connection.port")}));
				return validateResult;
			}
		}
		m_jobLinkSendSetting.setProxyUser(m_txtProxyUser.getText());
		m_jobLinkSendSetting.setProxyPassword(m_txtProxyPassword.getText());
		m_jobLinkSendSetting.setOwnerRoleId(m_cmpOwnerRoleId.getText());
		return null;
	}

	@Override
	protected boolean action() {
		boolean result = false;
		m_joblinkSendSettingId =m_jobLinkSendSetting.getJoblinkSendSettingId();		
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(m_managerName);
			if(this.m_mode == PropertyDefineConstant.MODE_MODIFY){
				ModifyJobLinkSendSettingRequest request = new ModifyJobLinkSendSettingRequest();
				RestClientBeanUtil.convertBean(m_jobLinkSendSetting, request);
				wrapper.modifyJobLinkSendSetting(m_joblinkSendSettingId, request);

				Object[] arg = {m_joblinkSendSettingId};
				MessageDialog.openInformation(null, Messages.getString("successful"),
						Messages.getString("message.joblinksendsetting.modified", arg));
			} else {//
				AddJobLinkSendSettingRequest request = new AddJobLinkSendSettingRequest();
				RestClientBeanUtil.convertBean(m_jobLinkSendSetting, request);
				wrapper.addJobLinkSendSetting(request);

				Object[] arg = {m_jobLinkSendSetting.getJoblinkSendSettingId()};
				MessageDialog.openInformation(null, Messages.getString("successful"),
						Messages.getString("message.joblinksendsetting.created", arg));
			}
			result = true;
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (JobMasterDuplicate e) {
			Object[] arg = {m_joblinkSendSettingId};
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.joblinksendsetting.duplicate", arg));
		} catch (InvalidUserPass | InvalidSetting e) {
			Object[] arg = {m_joblinkSendSettingId};
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.joblinksendsetting.failed", arg) + " " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("action(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return result;
	}

	private void update() {
		if("".equals(m_txtJoblinkSendSettingId.getText())){
			m_txtJoblinkSendSettingId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			m_txtJoblinkSendSettingId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(m_txtScope.getText())){
			m_txtScope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			m_txtScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(m_txtPort.getText())){
			m_txtPort.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			m_txtPort.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(m_txtUserId.getText())){
			m_txtUserId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			m_txtUserId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(m_txtPassword.getText())){
			m_txtPassword.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			m_txtPassword.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(m_txtPort.getText())){
			m_txtPort.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			m_txtPort.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_txtProxyHost.getEditable() && "".equals(m_txtProxyHost.getText())){
			m_txtProxyHost.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			m_txtProxyHost.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(m_txtProxyPort.getEditable() && "".equals(m_txtProxyPort.getText())){
			m_txtProxyPort.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else {
			m_txtProxyPort.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
}
