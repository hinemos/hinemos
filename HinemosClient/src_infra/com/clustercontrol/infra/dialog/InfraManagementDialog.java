/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.dialog;

import java.util.ArrayList;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.infra.composite.InfraNoticeComposite;
import com.clustercontrol.infra.composite.InfraParameterComposite;
import com.clustercontrol.infra.composite.InfraScopeComposite;
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
import com.clustercontrol.ws.notify.NotifyRelationInfo;

public class InfraManagementDialog extends CommonDialog {
	// ログ
	private static Log m_log = LogFactory.getLog( InfraManagementDialog.class );
	/**
	 * ダイアログの最背面レイヤのカラム数
	 * 最背面のレイヤのカラム数のみを変更するとレイアウトがくずれるため、
	 * グループ化されているレイヤは全てこれにあわせる
	 */
	private final int DIALOG_WIDTH = 12;
	/** タイトルラベルのカラム数 */
	private final int TITLE_WIDTH = 4;
	/** テキストフォームのカラム数 */
	private final int FORM_WIDTH = 8;

	/** 構築ID用テキスト */
	private Text m_managementId = null;
	/** 構築名用テキスト */
	private Text m_managementName = null;
	/** 説明用テキスト*/
	private Text m_description = null;
	/** オーナーロールID用コンポジット（コンポ） */
	private RoleIdListComposite m_ownerRoleId = null;
	/** スコープ用テキスト*/
	private InfraScopeComposite m_scope = null;
	
	/** タブフォルダ */
	private TabFolder m_tabFolder = null;

	/** 通知用コンポジット */
	private InfraNoticeComposite m_noticeComp = null;

	/** 変数用コンポジット */
	private InfraParameterComposite m_parameterComp = null;

	/** 有効に変更用ボタン*/
	private Button m_validFlg = null;

	/** アクションの種別 (default: MODE_ADD) */
	private int mode = PropertyDefineConstant.MODE_ADD;
	/** マネージャ名 */
	private String managerName = null;
	/** 構築ID*/
	private String managementId = null;

	/** モジュール情報 */
	private List<InfraModuleInfo> m_modules = null;

	/** マネージャリスト用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親シェル
	 * @wbp.parser.constructor
	 */
	public InfraManagementDialog(Shell parent) {
		super(parent);
	}

	public InfraManagementDialog(Shell parent, String managerName, String managementId, int mode) {
		super(parent);
		this.managerName = managerName;
		this.managementId = managementId;
		this.mode = mode;
	}

	/**
	 * ダイアログエリアを生成します。
	 * <P>
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell m_shell = this.getShell();
		parent.getShell().setText(Messages.getString("dialog.infra.create.modify"));

		// レイアウト設定
		// ダイアログ内のベースとなるレイアウトが全てを変更
		GridLayout baseLayout = new GridLayout(DIALOG_WIDTH, true);
		baseLayout.marginWidth = 10;
		baseLayout.marginHeight = 10;
		//一番下のレイヤー
		parent.setLayout(baseLayout);

		GridLayout layout = new GridLayout(DIALOG_WIDTH, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;

		GridData gridData = null;

		Composite infraInfoComposite = new Composite(parent, SWT.NONE);
		infraInfoComposite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		infraInfoComposite.setLayoutData(gridData);

		//マネージャ
		Label label = new Label(infraInfoComposite, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", label);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("facility.manager") + " : ");
		if( this.mode == PropertyDefineConstant.MODE_MODIFY ){
			this.m_managerComposite = new ManagerListComposite(infraInfoComposite, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(infraInfoComposite, SWT.NONE, true);
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// Update 
					String managerName = m_managerComposite.getText();
					m_ownerRoleId.createRoleIdList(managerName);

					String ownerRoleId = m_ownerRoleId.getComboRoleId().getText();
					m_scope.setOwnerRoleId(managerName, ownerRoleId );

					m_noticeComp.getNotifyId().setNotify(new ArrayList<NotifyRelationInfo>());
					m_noticeComp.setManagerName(managerName);
					m_noticeComp.setOwnerRoleId(ownerRoleId);
				}
			});
		}
		WidgetTestUtil.setTestId(this, "managerComposite", m_managerComposite);
		gridData = new GridData();
		gridData.horizontalSpan = FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);
		if( null != this.managerName ){
			this.m_managerComposite.setText(this.managerName);
		}

		// 構築ID
		Label constructIdTitle = new Label(infraInfoComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		constructIdTitle.setLayoutData(gridData);
		constructIdTitle.setText(Messages.getString("infra.management.id") + " : ");

		m_managementId = new Text(infraInfoComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_managementId.setLayoutData(gridData);
		m_managementId.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		// 構築名
		Label constructNameTitle = new Label(infraInfoComposite, SWT.LEFT);
		constructNameTitle.setText(Messages.getString("infra.management.name") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		constructNameTitle.setLayoutData(gridData);
		m_managementName = new Text(infraInfoComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_managementName.setLayoutData(gridData);
		m_managementName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		// 説明
		Label descriptionTitle = new Label(infraInfoComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		descriptionTitle.setText(Messages.getString("description") + " : ");
		descriptionTitle.setLayoutData(gridData);
		m_description = new Text(infraInfoComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_description.setLayoutData(gridData);
		m_description.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		// オーナーロールID
		Label labelRoleId = new Label(infraInfoComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		if( this.mode == PropertyDefineConstant.MODE_MODIFY ){
			m_ownerRoleId = new RoleIdListComposite(infraInfoComposite, SWT.NONE, this.m_managerComposite.getText(), false, Mode.OWNER_ROLE);
		}else{
			m_ownerRoleId = new RoleIdListComposite(infraInfoComposite, SWT.NONE, this.m_managerComposite.getText(), true, Mode.OWNER_ROLE);
			this.m_ownerRoleId.getComboRoleId().addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					String ownerRoleId = m_ownerRoleId.getText();
					m_noticeComp.setOwnerRoleId( ownerRoleId );
					m_scope.setOwnerRoleId( ownerRoleId );
					update();
				}
			});
		}
		gridData = new GridData();
		gridData.horizontalSpan = FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_ownerRoleId.setLayoutData(gridData);

		// スコープ
		Label scopeTitle = new Label(infraInfoComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		scopeTitle.setText(Messages.getString("scope") + " : ");
		scopeTitle.setLayoutData(gridData);

		m_scope = new InfraScopeComposite( infraInfoComposite, SWT.NONE );
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_scope.setLayoutData(gridData);

		// タブ
		this.m_tabFolder = new TabFolder(infraInfoComposite, SWT.NONE);
		GridLayout groupLayout = new GridLayout(1, true);
		groupLayout.marginWidth = 5;
		groupLayout.marginHeight = 5;
		groupLayout.numColumns = DIALOG_WIDTH;
		m_tabFolder.setLayout(groupLayout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_tabFolder.setLayoutData(gridData);

		//通知設定
		m_noticeComp = new InfraNoticeComposite(this.m_tabFolder, SWT.NONE, this.m_managerComposite.getText());
		TabItem notificationTabItem = new TabItem(this.m_tabFolder, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notificationTabItem", notificationTabItem);
		notificationTabItem.setText(Messages.getString("notifications"));
		notificationTabItem.setControl(m_noticeComp);
		m_noticeComp.setLayoutData(new GridData());

		// 変数設定
		m_parameterComp = new InfraParameterComposite(this.m_tabFolder, SWT.NONE);
		TabItem parameterTabItem = new TabItem(this.m_tabFolder, SWT.NONE);
		WidgetTestUtil.setTestId(this, "parameterTabItem", parameterTabItem);
		parameterTabItem.setText(Messages.getString("infra.parameter"));
		parameterTabItem.setControl(m_parameterComp);
		m_parameterComp.setLayoutData(new GridData());

		// 設定の（有効／無効）
		m_validFlg = new Button(infraInfoComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_validFlg.setText(Messages.getString("setting.valid.confirmed"));
		m_validFlg.setLayoutData(gridData);


		m_shell.pack();
		m_shell.setSize(new Point(540, m_shell.getSize().y));

		// 画面中央に
		Display display = m_shell.getDisplay();
		m_shell.setLocation(
				(display.getBounds().width - m_shell.getSize().x) / 2, (display
						.getBounds().height - m_shell.getSize().y) / 2);

		setInputData();
		update();
	}


	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示
		if("".equals((this.m_managementId.getText()).trim())){
			this.m_managementId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_managementId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals((this.m_managementName.getText()).trim())){
			this.m_managementName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_managementName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 * @since 5.0.0
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 * @since 5.0.0
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 環境構築設定情報をダイアログ及び各タブのコンポジットに反映します。
	 *
	 * @see com.clustercontrol.infra.bean.InfraManagementInfo
	 */
	private void setInputData() {
		// 初期表示
		InfraManagementInfo info = null;
		if(managementId != null){
			// 変更、コピーの場合、情報取得
			try {
				InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(this.m_managerComposite.getText());
				info = wrapper.getInfraManagement(managementId);
			} catch (InfraManagementNotFound_Exception | HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception e) {
				m_log.error("setInputData() getInfraManagement, " + e.getMessage());
			}
		}

		if(info != null){
			if (info.getManagementId() != null) {
				m_managementId.setText(info.getManagementId());
				//インフラ構築設定の変更の場合は構築ＩＤおよびオーナーロールＩＤが変更不可
				if (mode == PropertyDefineConstant.MODE_MODIFY) {
					m_managementId.setEnabled(false);
					m_ownerRoleId.setEnabled(false);
				}
			}
			if(info.getName() != null){
				m_managementName.setText(info.getName());
			}
			if(info.getDescription() != null){
				m_description.setText(info.getDescription());
			}
			//	オーナーロールID
			m_ownerRoleId.setText(info.getOwnerRoleId());

			//	スコープ
			m_scope.setInputData(m_managerComposite.getText(), info);

			//	重要度と通知ID
			m_noticeComp.setOwnerRoleId(info.getOwnerRoleId());
			m_noticeComp.setNotificationsInfo(info);
			//	設定の有効化
			m_validFlg.setSelection(info.isValidFlg());
			// モジュール情報の引継ぎ用
			m_modules = info.getModuleList();

			// 環境構築変数情報を設定
			this.m_parameterComp.setInfraManagementParamList(
					info.getInfraManagementParamList());

		} else {
			// 作成の場合（デフォルト設定）
			m_scope.setOwnerRoleId( m_managerComposite.getText(), m_ownerRoleId.getText() );
			m_scope.setScopeParam(true);
			m_validFlg.setSelection(true);
			m_noticeComp.setOwnerRoleId(m_ownerRoleId.getText());
		}
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
		return super.validate();
	}


	/**
	 * 無効な入力値の情報を設定します
	 *
	 */
	protected ValidateResult createValidateResult(String id, String message) {

		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	/**
	 * ダイアログの情報から、環境構築情報を作成します。
	 *
	 */
	protected InfraManagementInfo createInputData() {

		InfraManagementInfo info = new InfraManagementInfo();

		//構築ID取得
		info.setManagementId(m_managementId.getText());
		//構築名取得
		info.setName(m_managementName.getText());

		//説明 nullは許容するが空文字を許容しないため
		if(!"".equals(m_description.getText().trim())){
			info.setDescription(m_description.getText());
		}

		//オーナーロールID取得
		info.setOwnerRoleId(m_ownerRoleId.getText());

		//スコープ取得
		info.setFacilityId(m_scope.getFacilityId());
		info.setScope(m_scope.getScope());

		//重要度取得
		info.setStartPriority(m_noticeComp.getStartPriority());
		info.setNormalPriorityRun(m_noticeComp.getNormalPriorityRun());
		info.setAbnormalPriorityRun(m_noticeComp.getAbnormalPriorityRun());
		info.setNormalPriorityCheck(m_noticeComp.getNormalPriorityCheck());
		info.setAbnormalPriorityCheck(m_noticeComp.getAbnormalPriorityCheck());

		//通知ID取得
		if (m_noticeComp.getNotifyId().getNotify() != null) {
			List<NotifyRelationInfo> notifyList = info.getNotifyRelationList();
			notifyList.addAll(m_noticeComp.getNotifyId().getNotify());
		}

		// 変数情報取得
		info.getInfraManagementParamList().clear();
		info.getInfraManagementParamList().addAll(this.m_parameterComp.getInfraManagementParamList());

		//オーナーロールID
		info.setOwnerRoleId(m_ownerRoleId.getText());
		//有効・無効判定取得
		info.setValidFlg(m_validFlg.getSelection());

		if(m_modules != null){
			info.getModuleList().addAll(m_modules);
		}

		return info;
	}

	@Override
	protected boolean action() {
		boolean result = false;
		InfraManagementInfo info = createInputData();
		String action = null;
		String errMsg = null;
		InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(this.m_managerComposite.getText());

		if (mode == PropertyDefineConstant.MODE_MODIFY){
			// 変更の場合
			action = Messages.getString("modify");
			try {
				wrapper.modifyInfraManagement(info);
				result = true;
			} catch (InvalidRole_Exception e) {
				m_log.info("action() modifyInfraManagement, " + e.getMessage());
				errMsg = Messages.getString("message.accesscontrol.16");
			} catch (InfraManagementDuplicate_Exception e) {
				// ID重複
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.infra.module.duplicate", new String[]{m_managementId.getText()}));
			} catch (InfraManagementNotFound_Exception | HinemosUnknown_Exception | InvalidUserPass_Exception | InvalidSetting_Exception | NotifyDuplicate_Exception | NotifyNotFound_Exception e) {
				m_log.info("action() modifyInfraManagement, " + e.getMessage());
				errMsg = HinemosMessage.replace(e.getMessage());
			} catch (Exception e) {
				m_log.info("action() modifyInfraManagement, " + e.getMessage());
				errMsg = HinemosMessage.replace(e.getMessage());
			}
		} else if (mode == PropertyDefineConstant.MODE_ADD | mode == PropertyDefineConstant.MODE_COPY){
			// コピー,作成の場合
			action = Messages.getString("add");
			try {
				wrapper.addInfraManagement(info);
				result = true;
			} catch (InvalidRole_Exception e) {
				m_log.info("action() addInfraManagement, " + e.getMessage());
				errMsg = Messages.getString("message.accesscontrol.16");
			} catch (InfraManagementDuplicate_Exception e) {
				m_log.info("action(); addInfraManagement, " + e.getMessage());
				errMsg = Messages.getString("message.infra.module.duplicate", new String[]{m_managementId.getText()});
			} catch (InfraManagementNotFound_Exception e) {
				// コピーの場合の参照環境構築モジュールの環境構築設定未存在
				m_log.info("action(); addInfraManagement, " + e.getMessage());
				errMsg = HinemosMessage.replace(e.getMessage());
			} catch (NotifyDuplicate_Exception | HinemosUnknown_Exception | InvalidUserPass_Exception | InvalidSetting_Exception e) {
				m_log.info("action() addInfraManagement, " + e.getMessage());
				errMsg = HinemosMessage.replace(e.getMessage());
			} catch (Exception e) {
				m_log.info("action() addInfraManagement, " + e.getMessage());
				errMsg = HinemosMessage.replace(e.getMessage());
			}
		}

		if(result){
			action += "(" + this.m_managerComposite.getText() + ")";
			MessageDialog.openInformation(null, Messages
					.getString("successful"), Messages.getString(
					"message.infra.action.result",
					new Object[] { Messages.getString("infra.management.id"),
							action, Messages.getString("successful"),
							m_managementId.getText() }));
		} else {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.management.id"), action, Messages.getString("failed"), m_managementId.getText() + "\n" + (errMsg != null? errMsg: "")}));
		}

		return result;
	}
}