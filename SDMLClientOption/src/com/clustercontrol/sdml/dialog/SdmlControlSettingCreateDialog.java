/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.dialog;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.AddSdmlControlSettingRequest;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;
import org.openapitools.client.model.ModifySdmlControlSettingRequest;
import org.openapitools.client.model.SdmlControlSettingInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.SdmlControlSettingDuplicate;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.sdml.composite.AutoCreatedMonitorSettingComposite;
import com.clustercontrol.sdml.composite.SdmlAutoControlNoticeComposite;
import com.clustercontrol.sdml.composite.SdmlBasicSettingComposite;
import com.clustercontrol.sdml.composite.SdmlRunningTimeComposite;
import com.clustercontrol.sdml.util.SdmlClientConstant;
import com.clustercontrol.sdml.util.SdmlRestClientWrapper;
import com.clustercontrol.sdml.util.SdmlUiUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

public class SdmlControlSettingCreateDialog extends CommonDialog {
	private static Log logger = LogFactory.getLog(SdmlControlSettingCreateDialog.class);

	/** マネージャ */
	private String managerName = null;
	/** アプリケーションID */
	private String applicationId = null;
	/** ファシリティID */
	private String facilityId = "";

	/** アクションの種別 (default: MODE_ADD) */
	private int mode = PropertyDefineConstant.MODE_ADD;

	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite managerComposite = null;
	/** アプリケーションID用テキスト */
	private Text applicationIdText = null;
	/** オーナーロールID用コンポジット（コンポ） */
	private RoleIdListComposite ownerRoleIdComposite = null;
	/** スコープ用テキスト */
	private Text scopeText = null;
	/** 説明用テキスト */
	private Text descriptionText = null;
	/** 基本設定タブ */
	private SdmlBasicSettingComposite basicSettingComposite = null;
	/** 自動作成監視用設定タブ */
	private AutoCreatedMonitorSettingComposite autoCreatedMonitorSettingComposite = null;
	/** 起動時間タブ */
	private SdmlRunningTimeComposite runningTimeComposite = null;
	/** 監視設定制御時の通知タブ */
	private SdmlAutoControlNoticeComposite autoControlNoticeComposite = null;
	/** 有効 */
	private Button validButton = null;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 * @param managerName
	 * @param applicationId
	 * @param mode
	 */
	public SdmlControlSettingCreateDialog(Shell parent, String managerName, String applicationId, int mode) {
		super(parent);

		this.managerName = managerName;
		this.applicationId = applicationId;
		this.mode = mode;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();
		Label label = null;
		GridLayout layout = null;
		GridData gridData = null;

		// タイトル
		shell.setText(Messages.getString("dialog.sdml.control.setting.create.modify"));

		// レイアウト
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = SdmlClientConstant.DIALOG_WIDTH;
		parent.setLayout(layout);

		// Composite
		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout(SdmlClientConstant.DIALOG_WIDTH, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		composite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);

		// マネージャ(ラベル)
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("facility.manager") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		// マネージャ(Composite)
		if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
			this.managerComposite = new ManagerListComposite(composite, SWT.NONE, false);
		} else {
			this.managerComposite = new ManagerListComposite(composite, SWT.NONE, true);
		}
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.managerComposite.setLayoutData(gridData);
		if (this.managerName != null) {
			this.managerComposite.setText(this.managerName);
		}
		if (this.mode != PropertyDefineConstant.MODE_MODIFY) {
			this.managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					managerName = managerComposite.getText();
					ownerRoleIdComposite.createRoleIdList(managerName);
					scopeText.setText("");
					facilityId = "";

					basicSettingComposite.reflect(managerName, ownerRoleIdComposite.getText());
					autoCreatedMonitorSettingComposite.reflect(managerName, ownerRoleIdComposite.getText());
				}
			});
		}

		// アプリケーションID(ラベル)
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("application.id") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		// アプリケーションID(テキスト)
		this.applicationIdText = new Text(composite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.applicationIdText.setLayoutData(gridData);
		applicationIdText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		// 説明(ラベル)
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("getconfig.desc") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		// 説明(テキスト)
		this.descriptionText = new Text(composite, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.descriptionText.setLayoutData(gridData);

		// オーナーロールID(ラベル)
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("owner.role.id") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		// オーナーロールID(Composite)
		if (this.mode != PropertyDefineConstant.MODE_MODIFY) {
			this.ownerRoleIdComposite = new RoleIdListComposite(composite, SWT.NONE, this.managerName, true,
					Mode.OWNER_ROLE);
			this.ownerRoleIdComposite.getComboRoleId().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					scopeText.setText("");
					facilityId = "";

					basicSettingComposite.reflect(managerName, ownerRoleIdComposite.getText());
					autoCreatedMonitorSettingComposite.reflect(managerName, ownerRoleIdComposite.getText());
				}
			});
		} else {
			this.ownerRoleIdComposite = new RoleIdListComposite(composite, SWT.NONE, this.managerName, false,
					Mode.OWNER_ROLE);
		}
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.ownerRoleIdComposite.setLayoutData(gridData);

		// スコープ(ラベル)
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("scope") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		// スコープ(テキスト)
		this.scopeText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH_WITH_BTN;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.scopeText.setLayoutData(gridData);
		this.scopeText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// スコープ(参照ボタン)
		Button configSettingScopeButton = new Button(composite, SWT.NONE);
		configSettingScopeButton.setText(Messages.getString("refer"));
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.BUTTON_WIDTH;
		gridData.widthHint = 90;
		configSettingScopeButton.setLayoutData(gridData);
		configSettingScopeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				String managerName = managerComposite.getText();
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell, managerName, ownerRoleIdComposite.getText(), false,
						false);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse item = dialog.getSelectItem();
					FacilityInfoResponse info = item.getData();
					facilityId = info.getFacilityId();
					if (info.getFacilityType() == FacilityTypeEnum.NODE) {
						scopeText.setText(info.getFacilityName());
					} else {
						FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
						scopeText.setText(path.getPath(item));
					}
				}
			}
		});

		// タブ
		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		tabFolder.setLayoutData(gridData);

		// 基本設定タブ
		this.basicSettingComposite = new SdmlBasicSettingComposite(tabFolder, SWT.NONE, this.managerName, this.mode);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		this.basicSettingComposite.setLayoutData(gridData);
		TabItem tabBasic = new TabItem(tabFolder, SWT.NONE);
		tabBasic.setText(Messages.getString("sdml.basic.setting"));
		tabBasic.setControl(this.basicSettingComposite);

		// 自動作成監視用設定タブ
		this.autoCreatedMonitorSettingComposite = new AutoCreatedMonitorSettingComposite(tabFolder, SWT.NONE,
				this.managerName, this.ownerRoleIdComposite.getText());
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		this.autoCreatedMonitorSettingComposite.setLayoutData(gridData);
		TabItem tabAutoMonitor = new TabItem(tabFolder, SWT.NONE);
		tabAutoMonitor.setText(Messages.getString("sdml.auto.created.monitor.setting"));
		tabAutoMonitor.setControl(this.autoCreatedMonitorSettingComposite);

		// 起動時間タブ
		this.runningTimeComposite = new SdmlRunningTimeComposite(tabFolder, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		this.runningTimeComposite.setLayoutData(gridData);
		TabItem tabRunningTime = new TabItem(tabFolder, SWT.NONE);
		tabRunningTime.setText(Messages.getString("sdml.running.time"));
		tabRunningTime.setControl(this.runningTimeComposite);

		// 監視設定制御時の通知タブ
		this.autoControlNoticeComposite = new SdmlAutoControlNoticeComposite(tabFolder, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		this.autoControlNoticeComposite.setLayoutData(gridData);
		TabItem tabAutoControl = new TabItem(tabFolder, SWT.NONE);
		tabAutoControl.setText(Messages.getString("sdml.auto.control.notify"));
		tabAutoControl.setControl(this.autoControlNoticeComposite);

		// 有効フラグ
		validButton = new Button(composite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		validButton.setLayoutData(gridData);
		validButton.setText(Messages.getString("setting.valid.confirmed"));

		// ダイアログを調整
		this.adjustDialog(shell);

		this.setInputData();
		this.update();
	}

	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(Shell shell) {
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(750, shell.getSize().y));

		// 画面中央に配置
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}

	private void setInputData() {
		// 初期表示
		SdmlControlSettingInfoResponse info = null;
		if (applicationId != null) {
			// 変更、コピーの場合、情報取得
			try {
				SdmlRestClientWrapper wrapper = SdmlRestClientWrapper.getWrapper(this.managerComposite.getText());
				info = wrapper.getSdmlControlSettingV1(applicationId);
			} catch (HinemosUnknown | SdmlControlSettingNotFound | InvalidRole | InvalidUserPass
					| RestConnectFailed e) {
				logger.error("setInputData() getSdmlControlSetting, " + e.getMessage());
			}
		}

		if (info != null) {
			if (info.getApplicationId() != null) {
				applicationId = info.getApplicationId();
				applicationIdText.setText(info.getApplicationId());
			}
			if (info.getDescription() != null) {
				descriptionText.setText(info.getDescription());
			}
			// オーナーロールID
			if (info.getOwnerRoleId() != null) {
				ownerRoleIdComposite.setText(info.getOwnerRoleId());
			}

			// スコープ
			facilityId = info.getFacilityId();
			scopeText.setText(HinemosMessage.replace(info.getScope()));

			// 設定の有効化
			validButton.setSelection(info.getValidFlg());

			// 設定変更時は変更不可
			if (mode == PropertyDefineConstant.MODE_MODIFY) {
				applicationIdText.setEnabled(false);
				ownerRoleIdComposite.setEnabled(false);
			}
		} else {
			// 作成の場合（デフォルト設定）
			validButton.setSelection(true);
		}
		// オーナーロールIDを反映
		basicSettingComposite.reflect(managerName, ownerRoleIdComposite.getText());
		autoCreatedMonitorSettingComposite.reflect(managerName, ownerRoleIdComposite.getText());

		// 子Compositeへの情報設定
		basicSettingComposite.setInputData(info);
		autoCreatedMonitorSettingComposite.setInputData(info);
		runningTimeComposite.setInputData(info);
		autoControlNoticeComposite.setInputData(info);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update() {
		// 必須表示
		SdmlUiUtil.setColorRequired(applicationIdText);
		SdmlUiUtil.setColorRequired(scopeText);
	}

	/**
	 * ダイアログの情報から、登録用の情報を作成します。
	 *
	 */
	protected SdmlControlSettingInfoResponse createInputData() {
		SdmlControlSettingInfoResponse info = new SdmlControlSettingInfoResponse();

		info.setApplicationId(this.applicationIdText.getText());
		info.setDescription(this.descriptionText.getText());
		info.setOwnerRoleId(this.ownerRoleIdComposite.getText());
		info.setFacilityId(this.facilityId);
		info.setValidFlg(this.validButton.getSelection());

		// 子Compositeの情報反映
		basicSettingComposite.createInputData(info);
		autoCreatedMonitorSettingComposite.createInputData(info);
		runningTimeComposite.createInputData(info);
		autoControlNoticeComposite.createInputData(info);

		return info;
	}

	@Override
	protected boolean action() {
		boolean result = false;
		SdmlControlSettingInfoResponse info = createInputData();
		SdmlRestClientWrapper wrapper = SdmlRestClientWrapper.getWrapper(this.managerComposite.getText());

		String action = null;
		if (mode == PropertyDefineConstant.MODE_ADD | mode == PropertyDefineConstant.MODE_COPY) {
			action = Messages.getString("add");
		} else if (mode == PropertyDefineConstant.MODE_MODIFY) {
			action = Messages.getString("modify");
		}
		try {
			if (mode == PropertyDefineConstant.MODE_ADD | mode == PropertyDefineConstant.MODE_COPY) {
				// 作成、コピー
				AddSdmlControlSettingRequest dtoReq = new AddSdmlControlSettingRequest();
				RestClientBeanUtil.convertBean(info, dtoReq);
				wrapper.addSdmlControlSettingV1(dtoReq);
			} else if (mode == PropertyDefineConstant.MODE_MODIFY) {
				// 変更
				ModifySdmlControlSettingRequest dtoReq = new ModifySdmlControlSettingRequest();
				RestClientBeanUtil.convertBean(info, dtoReq);
				wrapper.modifySdmlControlSettingV1(info.getApplicationId(), dtoReq);
			}
			// 成功時のメッセージ
			MessageDialog.openInformation(null, Messages.getString("successful"), Messages.getString(
					"message.sdml.control.action.finished",
					new String[] { this.applicationIdText.getText(), action, this.managerComposite.getText() }));
			result = true;

		} catch (InvalidRole e) {
			// アクセス権なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (SdmlControlSettingDuplicate e) {
			// ID重複
			MessageDialog.openInformation(null, Messages.getString("message"), Messages
					.getString("message.sdml.control.duplicate", new String[] { this.applicationIdText.getText() }));
		} catch (Exception e) {
			logger.warn("action(), " + e.getMessage(), e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.sdml.control.action.failed",
							new String[] { this.applicationIdText.getText(), action, e.getMessage() }));
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
}
