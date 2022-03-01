/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.AddNodeConfigSettingInfoRequest;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;
import org.openapitools.client.model.ModifyNodeConfigSettingInfoRequest;
import org.openapitools.client.model.NodeConfigCustomInfoResponse;
import org.openapitools.client.model.NodeConfigSettingInfoResponse;
import org.openapitools.client.model.NodeConfigSettingItemInfoRequest;
import org.openapitools.client.model.NodeConfigSettingItemInfoRequest.SettingItemIdEnum;
import org.openapitools.client.model.NotifyRelationInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.PropertyFieldColorConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NodeConfigSettingDuplicate;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.notify.composite.NotifyIdListComposite;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.composite.NodeConfigCollectRuleComposite;
import com.clustercontrol.repository.composite.NodeConfigTargetComposite;
import com.clustercontrol.repository.composite.NodeCustomComposite;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.repository.bean.NodeConfigRunInterval;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * 構成情報取得設定の作成・変更ダイアログクラス<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeConfigSettingCreateDialog extends CommonDialog {
	// ログ
	private static Log m_log = LogFactory.getLog( NodeConfigSettingCreateDialog.class );

	// ----- instance フィールド ----- //

	/** 初期表示構成情報収集ID */
	private String m_configInfoSettingId = "";
	
	/** 構成情報収集対象スコープのファシリティID */
	private String m_facilityId = "";

	/** 変更用ダイアログ判別フラグ */
	private boolean m_isModifyDialog = false;

	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	/** オーナーロールID用テキスト */
	private RoleIdListComposite m_ownerRoleId = null;
	
	private Text		m_configSettingIdText = null;
	private Text		m_configSettingNameText = null;
	private Text		m_configSettingDescriptionText = null;
	private Text		m_configSettingScopeText = null;
	private Button		m_configSettingValidCheck = null;
	
	private NodeConfigCollectRuleComposite m_collectRule;

	private String m_managerName = null;

	private NotifyIdListComposite m_notify;

	private NodeConfigTargetComposite m_configTarget;

	/** ユーザ任意情報一覧 */
	private NodeCustomComposite m_customList = null;

	/** シェル */
	private Shell m_shell = null;

	// ----- コンストラクタ ----- //

	/**
	 * 指定した形式のダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param configInfoSettingId
	 *            初期表示するノードのファシリティID
	 * @param isModifyDialog
	 *            変更用ダイアログとして利用する場合は、true
	 */
	public NodeConfigSettingCreateDialog(Shell parent, String managerName, String configInfoSettingId,
			boolean isModifyDialog) {
		super(parent);

		this.m_managerName = managerName;
		this.m_configInfoSettingId = configInfoSettingId;
		this.m_isModifyDialog = isModifyDialog;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_shell = this.getShell();
		Label label = null;

		// タイトル
		m_shell.setText(Messages
				.getString("dialog.repository.nodeinfosetting.create.modify"));

		// レイアウト
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.fill = true;
		parent.setLayout(layout);

		// Composite
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		// マネージャ(ラベル)
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("facility.manager") + " : ");
		label.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));

		// マネージャ(Composite)
		if(!m_isModifyDialog){
			this.m_managerComposite = new ManagerListComposite(composite, SWT.NONE, true);
		} else {
			this.m_managerComposite = new ManagerListComposite(composite, SWT.NONE, false);
		}
		GridData gridData = new GridData();
		gridData.widthHint = 350;
		gridData.horizontalSpan = 2;
		this.m_managerComposite.setLayoutData(gridData);

		if(this.m_managerName != null) {
			this.m_managerComposite.setText(this.m_managerName);
		}
		if (!m_isModifyDialog) {
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_managerName = m_managerComposite.getText();
					m_ownerRoleId.createRoleIdList(m_managerName);
					m_configSettingScopeText.setText("");
					m_facilityId = "";
					m_collectRule.getCalendarId().createCalIdCombo(m_managerName, m_ownerRoleId.getText());
					m_notify.setManagerName(m_managerName);
				}
			});
		}

		// 構成情報収集ID(ラベル)
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("getconfig.id") + " : ");
		label.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));

		// 構成情報収集ID(テキスト)
		this.m_configSettingIdText = new Text(composite, SWT.BORDER);
		gridData = new GridData(350, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalSpan = 2;
		this.m_configSettingIdText.setLayoutData(gridData);
		m_configSettingIdText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		// 構成情報収集名(ラベル)
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("getconfig.name") + " : ");
		label.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));

		// 構成情報収集名(テキスト)
		this.m_configSettingNameText = new Text(composite, SWT.BORDER);
		gridData = new GridData(350, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalSpan = 2;
		this.m_configSettingNameText.setLayoutData(gridData);
		m_configSettingNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
			
		});

		// 説明(ラベル)
		label = new Label(composite, SWT.LEFT);
		label.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));
		label.setText(Messages.getString("getconfig.desc") + " : ");

		// 説明(テキスト)
		this.m_configSettingDescriptionText = new Text(composite, SWT.BORDER | SWT.LEFT);
		gridData = new GridData(350, SizeConstant.SIZE_TEXT_HEIGHT);
		gridData.horizontalSpan = 2;
		this.m_configSettingDescriptionText.setLayoutData(gridData);

		// オーナーロールID(ラベル)
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("owner.role.id") + " : ");
		label.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));

		// オーナーロールID(Composite)
		if(!m_isModifyDialog) {
			m_ownerRoleId = new RoleIdListComposite(composite, SWT.NONE, this.m_managerName, true, Mode.OWNER_ROLE);

			m_ownerRoleId.getComboRoleId().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_configSettingScopeText.setText("");
					m_facilityId = "";
					m_collectRule.getCalendarId().createCalIdCombo(m_managerName, m_ownerRoleId.getText());
					m_notify.setOwnerRoleId(m_ownerRoleId.getText(), true);
				}
			});
		} else {
			m_ownerRoleId = new RoleIdListComposite(composite, SWT.NONE, this.m_managerName, false, Mode.OWNER_ROLE);
		}
		gridData = new GridData();
		gridData.widthHint = 350;
		gridData.horizontalSpan = 2;
		m_ownerRoleId.setLayoutData(gridData);

		// スコープ(ラベル)
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("scope") + " : ");
		label.setLayoutData(new GridData(170, SizeConstant.SIZE_LABEL_HEIGHT));

		// スコープ(テキスト)
		m_configSettingScopeText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		m_configSettingScopeText.setLayoutData(new GridData(250, SizeConstant.SIZE_TEXT_HEIGHT));
		m_configSettingScopeText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// スコープ(参照ボタン)
		Button configSettingScopeButton = new Button(composite, SWT.NONE);
		configSettingScopeButton.setText(Messages.getString("refer"));
		configSettingScopeButton.setLayoutData(new GridData(90, SizeConstant.SIZE_BUTTON_HEIGHT));
		configSettingScopeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				String managerName = m_managerComposite.getText();
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell, managerName, m_ownerRoleId.getText(), false, false);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse item = dialog.getSelectItem();
					FacilityInfoResponse info = item.getData();
					m_facilityId = info.getFacilityId();
					if (info.getFacilityType() == FacilityTypeEnum.NODE) {
						m_configSettingScopeText.setText(info.getFacilityName());
					} else {
						FacilityPath path = new FacilityPath(
								ClusterControlPlugin.getDefault()
								.getSeparator());
						m_configSettingScopeText.setText(path.getPath(item));
					}
				}
			}
		});

		/*
		 * 収集間隔とカレンダ
		 */
		Group groupRule = new Group(composite, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.marginWidth = 2;
		gridLayout.marginHeight = 2;
		groupRule.setLayout(gridLayout);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.widthHint = 650;
		groupRule.setLayoutData(gridData);
		groupRule.setText(Messages.getString("monitor.rule"));

		m_collectRule = new NodeConfigCollectRuleComposite(groupRule, SWT.NONE);
		m_collectRule.getCalendarId().createCalIdCombo(this.m_managerName, m_ownerRoleId.getText());
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_collectRule.setLayoutData(gridData);

		/*
		 * 構成情報収集対象
		 */
		Group groupTarget = new Group(composite, SWT.NONE);
		gridLayout = new GridLayout(1, true);
		gridLayout.marginWidth = 2;
		gridLayout.marginHeight = 2;
		groupTarget.setLayout(gridLayout);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.widthHint = 650;
		groupTarget.setLayoutData(gridData);
		groupTarget.setText(Messages.getString("getconfig.target"));

		// タブ.
		TabFolder tabFolder = new TabFolder(groupTarget, SWT.NONE);

		// 基本情報
		this.m_configTarget = new NodeConfigTargetComposite(tabFolder, SWT.NONE);
		gridData = new GridData();
		this.m_configTarget.setLayoutData(gridData);
		TabItem tabBasic = new TabItem(tabFolder, SWT.NONE);
		tabBasic.setText(Messages.getString("node.config.basic"));
		tabBasic.setControl(m_configTarget);

		// ユーザ任意情報
		this.m_customList = new NodeCustomComposite(tabFolder, SWT.NONE);
		gridData = new GridData();
		this.m_customList.setLayoutData(gridData);
		TabItem tabCustom = new TabItem(tabFolder, SWT.NONE);
		tabCustom.setText(Messages.getString("node.config.setting.custom"));
		tabCustom.setControl(this.m_customList);

		/*
		 * 通知グループ（監視グループの子グループ）
		 */
		Group groupNotifyAttribute = new Group(composite, SWT.NONE);
		gridLayout = new GridLayout(1, true);
		gridLayout.marginWidth = 2;
		gridLayout.marginHeight = 2;
		groupNotifyAttribute.setLayout(gridLayout);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupNotifyAttribute.setLayoutData(gridData);
		groupNotifyAttribute.setText(Messages.getString("notify.attribute"));
		this.m_notify = new NotifyIdListComposite(groupNotifyAttribute, SWT.NONE, true);
		this.m_notify.setManagerName(m_managerName);
		this.m_notify.setOwnerRoleId(this.m_ownerRoleId.getText(), true);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_notify.setLayoutData(gridData);
		
		this.m_notify.update();
		
		
		// 有効フラグ
		m_configSettingValidCheck = new Button(composite, SWT.CHECK);
		gridData = new GridData(200, SizeConstant.SIZE_BUTTON_HEIGHT);
		gridData.horizontalSpan = 3;
		m_configSettingValidCheck.setLayoutData(gridData);
		m_configSettingValidCheck.setText(Messages.getString("setting.valid.confirmed"));
		

		// ダイアログを調整
		this.adjustDialog();

		this.setInputData();
		this.update();
	}

	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		this.m_shell.pack();
		this.m_shell.setSize(new Point(700, m_shell.getSize().y));

		// 画面中央に配置
		Display display = this.m_shell.getDisplay();
		this.m_shell.setLocation((display.getBounds().width - this.m_shell.getSize().x) / 2,
				(display.getBounds().height - this.m_shell.getSize().y) / 2);
	}

	/**
	 * 構成情報取得設定情報をダイアログ及び各タブのコンポジットに反映します。
	 *
	 * @see com.clustercontrol.infra.bean.InfraManagementInfo
	 */
	private void setInputData() {
		// 初期表示
		NodeConfigSettingInfoResponse info = null;
		if(m_configInfoSettingId != null){
			// 変更、コピーの場合、情報取得
			try {
				RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(this.m_managerComposite.getText());
				info = wrapper.getNodeConfigSettingInfo(m_configInfoSettingId);
			} catch (HinemosUnknown | InvalidRole | InvalidUserPass | NodeConfigSettingNotFound | RestConnectFailed e) {
				m_log.error("setInputData() getNodeConfigInfoSetting, " + e.getMessage());
			}
		}

		// オーナーロールID設定
		if (info != null && info.getOwnerRoleId() != null) {
			m_ownerRoleId.setText(info.getOwnerRoleId());
		}

		// 他CompositeへのオーナーロールIDの設定
		m_collectRule.getCalendarId().createCalIdCombo(this.m_managerName, m_ownerRoleId.getText());
		m_notify.setOwnerRoleId(m_ownerRoleId.getText(), true);

		// 各種情報設定
		if(info != null){
			if (info.getSettingId() != null) {
				m_configInfoSettingId = info.getSettingId();
				m_configSettingIdText.setText(info.getSettingId());
				if (m_isModifyDialog) {
					m_configSettingIdText.setEditable(false);
					m_ownerRoleId.setEnabled(false);
				}
			}
			if(info.getSettingName() != null){
				m_configSettingNameText.setText(info.getSettingName());
			}
			if(info.getDescription() != null){
				m_configSettingDescriptionText.setText(info.getDescription());
			}

			//	スコープ
			m_configSettingScopeText.setText(HinemosMessage.replace(info.getScope()));
			m_facilityId = info.getFacilityId();

			//	重要度と通知ID
			List<NotifyRelationInfoResponse> dtoList = info.getNotifyRelationList();
			m_notify.setNotify(dtoList);

			//	設定の有効化
			m_configSettingValidCheck.setSelection(info.getValidFlg());

		} else {
			// 作成の場合（デフォルト設定）
			m_configInfoSettingId = "";
			m_configSettingIdText.setText("");
			m_configSettingNameText.setText("");
			m_configSettingDescriptionText.setText("");
			m_configSettingScopeText.setText("");
			m_facilityId = "";
			m_configSettingValidCheck.setSelection(true);
		}
		// 他Compositeへの情報設定
		m_collectRule.setInputData(info);
		m_configTarget.setInputData(info);

		this.m_customList.setInputData(info);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){

		m_log.debug("update");

		/*必須項目を色別表示します。*/
		setColor(m_configSettingIdText);
		setForegroundColor(m_configSettingIdText);
		setColor(m_configSettingNameText);
		setForegroundColor(m_configSettingNameText);
		setColor(m_configSettingScopeText);
		setForegroundColor(m_configSettingScopeText);

	}

	/**
	 * 空文字だったらピンク色にする。
	 * @param item
	 */
	private void setColor(Text text) {
		if (text == null) {
			return ;
		}
		if ("".equals(text.getText())) {
			text.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			text.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 入力項目です、の色を薄くする。
	 * @param item
	 */
	private void setForegroundColor(Text text) {
		if (text == null) {
			return;
		}

		if ("".equals(text.getText())) {
			text.setForeground(PropertyFieldColorConstant.COLOR_EMPTY);
		} else {
			text.setForeground(PropertyFieldColorConstant.COLOR_FILLED);
		}

	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		return result;
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

		//ID取得
		String args[] = { this.m_configSettingIdText.getText() };
		
		// Manager登録.
		ArrayList<NodeConfigCustomInfoResponse> customList = this.m_customList.getNodeConfigCustomInfoList();
		String errMessage = "";
		RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(this.m_managerComposite.getText());

		NodeConfigSettingInfoResponse configInfo = new NodeConfigSettingInfoResponse();
		configInfo.setSettingId(this.m_configSettingIdText.getText());
		configInfo.setFacilityId(this.m_facilityId);
		configInfo.setSettingName(this.m_configSettingNameText.getText());
		configInfo.setDescription(this.m_configSettingDescriptionText.getText());
		configInfo.setCalendarId(this.m_collectRule.getCalendarId().getText());
		configInfo.setValidFlg(this.m_configSettingValidCheck.getSelection());

		if(customList != null && !customList.isEmpty()){
			configInfo.getNodeConfigCustomList().addAll(customList);
		}

		if (m_ownerRoleId.getText().length() > 0) {
			configInfo.setOwnerRoleId(m_ownerRoleId.getText());
		}

		if (m_notify.getNotify() != null){
			configInfo.setNotifyRelationList(m_notify.getNotify());
		}
		
		if(!m_isModifyDialog){

			// 作成の場合
			try {
				AddNodeConfigSettingInfoRequest requestDto = new AddNodeConfigSettingInfoRequest();
				RestClientBeanUtil.convertBean(configInfo, requestDto);
				if (this.m_collectRule.getRunInterval() == NodeConfigRunInterval.TYPE_HOUR_6) {
					requestDto.setRunInterval(org.openapitools.client.model.AddNodeConfigSettingInfoRequest.RunIntervalEnum._6);
				} else if (this.m_collectRule.getRunInterval() == NodeConfigRunInterval.TYPE_HOUR_12) {
					requestDto.setRunInterval(org.openapitools.client.model.AddNodeConfigSettingInfoRequest.RunIntervalEnum._12);
				} else if (this.m_collectRule.getRunInterval() == NodeConfigRunInterval.TYPE_HOUR_24) {
					requestDto.setRunInterval(org.openapitools.client.model.AddNodeConfigSettingInfoRequest.RunIntervalEnum._24);
				}
				requestDto.setNodeConfigSettingItemList(new ArrayList<>());
				for (SettingItemIdEnum settingItemId : this.m_configTarget.getTarget()) {
					NodeConfigSettingItemInfoRequest tmp = new NodeConfigSettingItemInfoRequest();
					tmp.setSettingItemId(settingItemId);
					requestDto.getNodeConfigSettingItemList().add(tmp);
				}
				if(this.m_customList.isValid()){
					NodeConfigSettingItemInfoRequest tmp = new NodeConfigSettingItemInfoRequest();
					tmp.setSettingItemId(SettingItemIdEnum.CUSTOM);
					requestDto.getNodeConfigSettingItemList().add(tmp);
				}
				wrapper.addNodeConfigSettingInfo(requestDto);

				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						Messages.getString("message.repository.57", args));

				result = true;

			} catch (NodeConfigSettingDuplicate e) {
				// 構成情報収集設定IDが重複している場合、エラーダイアログを表示する

				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.repository.55", args));


			} catch (Exception e) {
				if (e instanceof InvalidRole) {
					// アクセス権なしの場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} else {
					errMessage = ", " + HinemosMessage.replace(e.getMessage());
					if (!(e instanceof InvalidSetting)) {
						m_log.warn("action()", e);
					} else {
						m_log.info("action()" + errMessage);
					}
				}
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.repository.58", args) + errMessage);
			}
		} else {
			// 変更の場合
			try {
				ModifyNodeConfigSettingInfoRequest requestDto = new ModifyNodeConfigSettingInfoRequest();
				RestClientBeanUtil.convertBean(configInfo, requestDto);
				if (this.m_collectRule.getRunInterval() == NodeConfigRunInterval.TYPE_HOUR_6) {
					requestDto.setRunInterval(org.openapitools.client.model.ModifyNodeConfigSettingInfoRequest.RunIntervalEnum._6);
				} else if (this.m_collectRule.getRunInterval() == NodeConfigRunInterval.TYPE_HOUR_12) {
					requestDto.setRunInterval(org.openapitools.client.model.ModifyNodeConfigSettingInfoRequest.RunIntervalEnum._12);
				} else if (this.m_collectRule.getRunInterval() == NodeConfigRunInterval.TYPE_HOUR_24) {
					requestDto.setRunInterval(org.openapitools.client.model.ModifyNodeConfigSettingInfoRequest.RunIntervalEnum._24);
				}
				requestDto.setNodeConfigSettingItemList(new ArrayList<>());
				for (SettingItemIdEnum settingItemId : this.m_configTarget.getTarget()) {
					NodeConfigSettingItemInfoRequest tmp = new NodeConfigSettingItemInfoRequest();
					tmp.setSettingItemId(settingItemId);
					requestDto.getNodeConfigSettingItemList().add(tmp);
				}
				if(this.m_customList.isValid()){
					NodeConfigSettingItemInfoRequest tmp = new NodeConfigSettingItemInfoRequest();
					tmp.setSettingItemId(SettingItemIdEnum.CUSTOM);
					requestDto.getNodeConfigSettingItemList().add(tmp);
				}
				wrapper.modifyNodeConfigSettingInfo(configInfo.getSettingId(), requestDto);
					MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						Messages.getString("message.repository.54", args));

					result = true;

			} catch (Exception e) {
				if (e instanceof InvalidRole) {
					// アクセス権なしの場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} else {
					errMessage = ", " + HinemosMessage.replace(e.getMessage());
				}

				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.repository.56", args) + errMessage);
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
		if (m_isModifyDialog) {
			return Messages.getString("modify");
		} else {
			return Messages.getString("register");
		}
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
}
