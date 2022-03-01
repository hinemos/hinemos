/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.dialog;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.CloudNotifyDetailInfoResponse;
import org.openapitools.client.model.CloudNotifyLinkInfoKeyValueObjectResponse;
import org.openapitools.client.model.FacilityInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.action.AddNotify;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.ModifyNotify;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.bean.CloudConstant;
import com.clustercontrol.xcloud.model.InvalidStateException;
import com.clustercontrol.xcloud.plugin.monitor.ScopeTreeDialogForServiceMonitors;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

/**
 * 通知（クラウド）作成・変更ダイアログクラス<BR>
 *
 */
public class NotifyCloudCreateDialog extends NotifyBasicCreateDialog {

	private static Log m_log = LogFactory.getLog(NotifyCloudCreateDialog.class);
	/** カラム数（重要度）。 */
	private static final int WIDTH_PRIORITY = 2;

	/** カラム数（チェックボックス）。 */
	private static final int WIDTH_CHECK = 2;

	/** カラム数（コンボボックス（Facility））。 */
	private static final int WIDTH_COMBO_FACILITY = 2;

	// ----- instance フィールド ----- //

	/**
	 * 通知タイプ
	 * 
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	private static final int TYPE_CLOUD = NotifyTypeConstant.TYPE_CLOUD;

	/** 入力値の正当性を保持するオブジェクト。 */
	protected ValidateResult validateResult = null;

	/** スコープ用テキスト */
	private Text m_textScope = null;

	/** ファシリティID */
	private String m_facilityId = null;

	/** スコープ */
	private String m_facilityPath = null;

	/** スコープ参照用ボタン */
	private Button m_scopeSelect = null;

	/** 実行（重要度：通知） チェックボックス。 */
	private Button m_checkCloudNotifyInfo = null;
	/** 実行（重要度：警告） チェックボックス。 */
	private Button m_checkCloudNotifyWarning = null;
	/** 実行（重要度：危険） チェックボックス。 */
	private Button m_checkCloudNotifyCritical = null;
	/** 実行（重要度：不明） チェックボックス。 */
	private Button m_checkCloudNotifyUnknown = null;

	// 重要度別「選択」ボタン
	private Button m_infoSelectButton = null;
	private Button m_warnSelectButton = null;
	private Button m_errorSelectButton = null;
	private Button m_unkSelectButton = null;

	// 重要度別コピー用コンボボックス
	private Combo m_infoCopyCombo = null;
	private Combo m_warnCopyCombo = null;
	private Combo m_errorCopyCombo = null;
	private Combo m_unkCopyCombo = null;

	// 重要度別「コピー」ボタン
	private Button m_infoCopyButton = null;
	private Button m_warnCopyButton = null;
	private Button m_errorCopyButton = null;
	private Button m_unkCopyButton = null;

	// 重要度別連携情報オブジェクト
	private LinkInfo m_InfoLinkInfo;
	private LinkInfo m_WarnLinkInfo;
	private LinkInfo m_CritLinkInfo;
	private LinkInfo m_UnkLinkInfo;

	// 重要度別連携情報ダイアログ
	protected NotifyCloudLinkInfoDialog m_InfoNotifyCloudLinkInfoDialog;
	protected NotifyCloudLinkInfoDialog m_WanrNotifyCloudLinkInfoDialog;
	protected NotifyCloudLinkInfoDialog m_CritNotifyCloudLinkInfoDialog;
	protected NotifyCloudLinkInfoDialog m_UnkNotifyCloudLinkInfoDialog;

	// クラウドプラットフォームの定数
	private final String AWS_PLT_ID = "_" + CloudConstant.platform_AWS + "_";
	private final String AZURE_PLT_ID = "_" + CloudConstant.platform_Azure + "_";

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public NotifyCloudCreateDialog(Shell parent) {
		super(parent);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param managerName
	 *            マネージャ名
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param notifyId
	 *            変更する通知情報の通知ID
	 * @param updateFlg
	 *            更新フラグ（true:更新する）
	 */
	public NotifyCloudCreateDialog(Shell parent, String managerName, String notifyId, boolean updateFlg) {
		super(parent, managerName, notifyId, updateFlg);
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 * @see com.clustercontrol.notify.action.GetNotify#getNotify(String)
	 * @see #setInputData(NotifyInfoInputData)
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// 通知IDが指定されている場合、その情報を初期表示する。
		NotifyInfoInputData inputData;
		if (this.notifyId != null) {
			inputData = new GetNotify().getCloudNotify(this.managerName, this.notifyId);
		} else {
			inputData = new NotifyInfoInputData();
		}
		this.setInputData(inputData);

		update();

	}

	/**
	 * 親のクラスから呼ばれ、各通知用のダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 */
	@Override
	protected void customizeSettingDialog(Composite parent) {
		final Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.notify.cloud.create.modify"));

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
		 * クラウド通知
		 */

		// クラウドグループ
		Group groupCloud = new Group(parent, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupCloud.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCloud.setLayoutData(gridData);
		groupCloud.setText(Messages.getString("notifies.cloud") + " : ");

		/*
		 * スコープ
		 */
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("scope"));

		this.m_textScope = new Text(groupCloud, SWT.BORDER | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textScope.setLayoutData(gridData);
		this.m_textScope.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		this.m_scopeSelect = new Button(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_scopeSelect.setLayoutData(gridData);
		this.m_scopeSelect.setText(Messages.getString("refer"));
		this.m_scopeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialogForServiceMonitors dialog = null;
				int dialogPressType = -1;
				try {
					// エンドポイントがPublishされていることを確認
					ValidateResult result = validateEndpoint(managerName);
					if (result != null) {
						displayError(result);
						return;
					}
					dialog = new ScopeTreeDialogForServiceMonitors(getShell(), managerName,
							m_notifyBasic.m_ownerRoleId.getText());
					
					dialogPressType = dialog.open();
				} catch (InvalidStateException e1) {
					// クラウド仮想化の参照権限がない場合等に到達
					// ダイアログを出力して処理を終了
					MessageDialog.openInformation(null, Messages.getString("message"), e1.getMessage());
					return;
				} catch (Exception e2){
					// 想定外例外はログに出力するのみ(ここには到達しない想定)
					m_log.warn("customizeSettingDialog(): ", e2);
					return;
				}

				if (dialogPressType == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse selectItem = dialog.getSelectItem();
					FacilityInfoResponse info = selectItem.getData();
					FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
					m_facilityPath = path.getPath(selectItem);
					m_facilityId = info.getFacilityId();
					m_textScope.setText(HinemosMessage.replace(m_facilityPath));
					update();
				}
			}
		});
		// 空白
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 空行
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 重要度 ごとの設定
		 */
		// ラベル（重要度）
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// ラベル（実行する）
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.attribute"));

		// ラベル（連携情報）
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMBO_FACILITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("dialog.notify.cloud.link.info"));

		// 空白
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 重要度：通知
		label = this.getLabelPriority(groupCloud, Messages.getString("info"), PriorityColorConstant.COLOR_INFO);
		this.m_checkCloudNotifyInfo = this.getCheckCloudNotify(groupCloud);
		this.m_checkCloudNotifyInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkCloudNotifyInfo.getSelection(), m_infoSelectButton, m_infoCopyCombo,
						m_infoCopyButton);
				update();
			}
		});

		// 選択ボタン
		m_infoSelectButton = getSelectButton(groupCloud, shell, PriorityConstant.TYPE_INFO);
		// 空白
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 重要度取得用
		m_infoCopyCombo = getComboCopyPriority(groupCloud);

		// コピーボタン
		m_infoCopyButton = getCopyButton(groupCloud, shell, PriorityConstant.TYPE_INFO);

		// 空白
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 初期表示は非活性
		setEnabled(false, m_infoSelectButton, m_infoCopyCombo, m_infoCopyButton);

		// クラウド通知 重要度：警告
		label = this.getLabelPriority(groupCloud, Messages.getString("warning"), PriorityColorConstant.COLOR_WARNING);
		this.m_checkCloudNotifyWarning = this.getCheckCloudNotify(groupCloud);
		this.m_checkCloudNotifyWarning.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkCloudNotifyWarning.getSelection(), m_warnSelectButton, m_warnCopyCombo,
						m_warnCopyButton);
				update();
			}
		});
		// 選択ボタン
		m_warnSelectButton = getSelectButton(groupCloud, shell, PriorityConstant.TYPE_WARNING);
		// 空白
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 重要度取得用
		m_warnCopyCombo = getComboCopyPriority(groupCloud);

		// コピーボタン
		m_warnCopyButton = getCopyButton(groupCloud, shell, PriorityConstant.TYPE_WARNING);

		// 空白
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		setEnabled(false, m_warnSelectButton, m_warnCopyCombo, m_warnCopyButton);

		// クラウド通知 重要度：危険
		label = this.getLabelPriority(groupCloud, Messages.getString("critical"), PriorityColorConstant.COLOR_CRITICAL);
		this.m_checkCloudNotifyCritical = this.getCheckCloudNotify(groupCloud);
		this.m_checkCloudNotifyCritical.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkCloudNotifyCritical.getSelection(), m_errorSelectButton, m_errorCopyCombo,
						m_errorCopyButton);
				update();
			}
		});

		// 選択ボタン
		m_errorSelectButton = getSelectButton(groupCloud, shell, PriorityConstant.TYPE_CRITICAL);
		// 空白
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 重要度取得用
		m_errorCopyCombo = getComboCopyPriority(groupCloud);

		// コピーボタン
		m_errorCopyButton = getCopyButton(groupCloud, shell, PriorityConstant.TYPE_CRITICAL);

		// 空白
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		setEnabled(false, m_errorSelectButton, m_errorCopyCombo, m_errorCopyButton);

		// クラウド通知 重要度：不明
		label = this.getLabelPriority(groupCloud, Messages.getString("unknown"), PriorityColorConstant.COLOR_UNKNOWN);
		this.m_checkCloudNotifyUnknown = this.getCheckCloudNotify(groupCloud);

		this.m_checkCloudNotifyUnknown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(m_checkCloudNotifyUnknown.getSelection(), m_unkSelectButton, m_unkCopyCombo,
						m_unkCopyButton);
				update();
			}
		});
		// 選択ボタン
		m_unkSelectButton = getSelectButton(groupCloud, shell, PriorityConstant.TYPE_UNKNOWN);
		// 空白
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 重要度取得用
		m_unkCopyCombo = getComboCopyPriority(groupCloud);

		// コピーボタン
		m_unkCopyButton = getCopyButton(groupCloud, shell, PriorityConstant.TYPE_UNKNOWN);

		// 空白
		label = new Label(groupCloud, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		setEnabled(false, m_unkSelectButton, m_unkCopyCombo, m_unkCopyButton);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update() {
		// 必須項目を明示
		if (this.m_textScope.getText().isEmpty()) {
			this.m_textScope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_textScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);

			m_checkCloudNotifyInfo.setEnabled(true);
			m_checkCloudNotifyWarning.setEnabled(true);
			m_checkCloudNotifyCritical.setEnabled(true);
			m_checkCloudNotifyUnknown.setEnabled(true);
		}
		
		// 連携情報の参照ボタンの必須有無
		// 重要度のチェックが無い場合は、灰色に戻す
		if (m_checkCloudNotifyInfo.getSelection()) {
			if (m_InfoLinkInfo == null || m_InfoLinkInfo.isEmpty()) {
				m_infoSelectButton.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				m_infoSelectButton.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		} else {
			m_infoSelectButton.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (m_checkCloudNotifyWarning.getSelection()) {
			if (m_WarnLinkInfo == null || m_WarnLinkInfo.isEmpty()) {
				m_warnSelectButton.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				m_warnSelectButton.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		} else {
			m_warnSelectButton.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (m_checkCloudNotifyCritical.getSelection()) {
			if (m_CritLinkInfo == null || m_CritLinkInfo.isEmpty()) {
				m_errorSelectButton.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				m_errorSelectButton.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		} else {
			m_errorSelectButton.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if (m_checkCloudNotifyUnknown.getSelection()) {
			if (m_UnkLinkInfo == null || m_UnkLinkInfo.isEmpty()) {
				m_unkSelectButton.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				m_unkSelectButton.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		} else {
			m_unkSelectButton.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

	}
	
	@Override
	/**
	 * オーナーロール変更時にスコープをクリアする
	 * @param ownerRoleId
	 */
	public void updateOwnerRole(String ownerRoleId) {
		super.updateOwnerRole(ownerRoleId);
		
		// 選択済みスコープをクリア
		m_facilityPath = "";
		m_facilityId = "";
		m_textScope.setText("");
		update();
	}

	/**
	 * 入力値を保持した通知情報を返します。
	 *
	 * @return 通知情報
	 */
	@Override
	public NotifyInfoInputData getInputData() {
		return this.inputData;
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param notify
	 *            設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(NotifyInfoInputData notify) {
		super.setInputData(notify);

		// コマンド情報
		CloudNotifyDetailInfoResponse info = notify.getNotifyCloudInfo();

		if (info != null) {
			this.setInputDatal(info);
		} else {
			m_checkCloudNotifyInfo.setEnabled(false);
			m_checkCloudNotifyWarning.setEnabled(false);
			m_checkCloudNotifyCritical.setEnabled(false);
			m_checkCloudNotifyUnknown.setEnabled(false);
		}
	}

	private void setInputDatal(CloudNotifyDetailInfoResponse notifyInfo) {
		// スコープ情報
		if (notifyInfo.getFacilityId() != null) {
			m_facilityId = notifyInfo.getFacilityId();
		}
		if (notifyInfo.getTextScope() != null) {
			m_textScope.setText(HinemosMessage.replace(notifyInfo.getTextScope()));
		}
		// 有効無効
		if (notifyInfo.getInfoValidFlg() != null) {
			m_checkCloudNotifyInfo.setSelection(notifyInfo.getInfoValidFlg());
			setEnabled(m_checkCloudNotifyInfo.getSelection(), m_infoSelectButton, m_infoCopyCombo, m_infoCopyButton);
		}
		if (notifyInfo.getWarnValidFlg() != null) {
			m_checkCloudNotifyWarning.setSelection(notifyInfo.getWarnValidFlg());
			setEnabled(m_checkCloudNotifyWarning.getSelection(), m_warnSelectButton, m_warnCopyCombo, m_warnCopyButton);
		}
		if (notifyInfo.getCriticalValidFlg() != null) {
			m_checkCloudNotifyCritical.setSelection(notifyInfo.getCriticalValidFlg());
			setEnabled(m_checkCloudNotifyCritical.getSelection(), m_errorSelectButton, m_errorCopyCombo,
					m_errorCopyButton);
		}
		if (notifyInfo.getUnknownValidFlg() != null) {
			m_checkCloudNotifyUnknown.setSelection(notifyInfo.getUnknownValidFlg());
			setEnabled(m_checkCloudNotifyUnknown.getSelection(), m_unkSelectButton, m_unkCopyCombo, m_unkCopyButton);
		}

		// 連携情報をデータオブジェクトに格納
		// 連携情報 重要
		m_InfoLinkInfo = new LinkInfo();
		m_InfoLinkInfo.setEventBus(notifyInfo.getInfoEventBus());
		m_InfoLinkInfo.setAccessKey(notifyInfo.getInfoAccessKey());
		m_InfoLinkInfo.setDataVersion(notifyInfo.getInfoDataVersion());
		m_InfoLinkInfo.setDetailType(notifyInfo.getInfoDetailType());
		m_InfoLinkInfo.setDataList(notifyInfo.getInfoKeyValueDataList());
		m_InfoLinkInfo.setSource(notifyInfo.getInfoSource());
		// 連携情報 警告
		m_WarnLinkInfo = new LinkInfo();
		m_WarnLinkInfo.setEventBus(notifyInfo.getWarnEventBus());
		m_WarnLinkInfo.setAccessKey(notifyInfo.getWarnAccessKey());
		m_WarnLinkInfo.setDataVersion(notifyInfo.getWarnDataVersion());
		m_WarnLinkInfo.setDetailType(notifyInfo.getWarnDetailType());
		m_WarnLinkInfo.setDataList(notifyInfo.getWarnKeyValueDataList());
		m_WarnLinkInfo.setSource(notifyInfo.getWarnSource());
		// 連携情報 危険
		m_CritLinkInfo = new LinkInfo();
		m_CritLinkInfo.setEventBus(notifyInfo.getCritEventBus());
		m_CritLinkInfo.setAccessKey(notifyInfo.getCritAccessKey());
		m_CritLinkInfo.setDataVersion(notifyInfo.getCritDataVersion());
		m_CritLinkInfo.setDetailType(notifyInfo.getCritDetailType());
		m_CritLinkInfo.setDataList(notifyInfo.getCritKeyValueDataList());
		m_CritLinkInfo.setSource(notifyInfo.getCritSource());
		// 連携情報 不明
		m_UnkLinkInfo = new LinkInfo();
		m_UnkLinkInfo.setEventBus(notifyInfo.getUnkEventBus());
		m_UnkLinkInfo.setAccessKey(notifyInfo.getUnkAccessKey());
		m_UnkLinkInfo.setDataVersion(notifyInfo.getUnkDataVersion());
		m_UnkLinkInfo.setDetailType(notifyInfo.getUnkDetailType());
		m_UnkLinkInfo.setDataList(notifyInfo.getUnkKeyValueDataList());
		m_UnkLinkInfo.setSource(notifyInfo.getUnkSource());
	}

	/**
	 * 入力値を設定した通知情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知情報
	 *
	 * @see #createInputDataForLogEscalate(ArrayList, int, Button, Combo,
	 *      Button, Combo, Button, Text)
	 */
	@Override
	protected NotifyInfoInputData createInputData() {
		NotifyInfoInputData info = super.createInputData();

		// 通知タイプの設定
		info.setNotifyType(TYPE_CLOUD);

		// 通知タイプの設定
		CloudNotifyDetailInfoResponse notify = createNotifyInfoDetail();
		info.setNotifyCloudInfo(notify);

		return info;
	}

	private CloudNotifyDetailInfoResponse createNotifyInfoDetail() {
		CloudNotifyDetailInfoResponse notify = new CloudNotifyDetailInfoResponse();

		// 共通部分登録
		// 実行ファシリティID
		if (this.m_textScope.getText() != null && !"".equals(this.m_textScope.getText())) {
			notify.setFacilityId(this.m_facilityId);
			notify.setTextScope(this.m_textScope.getText());
		}

		// 実行
		notify.setInfoValidFlg(m_checkCloudNotifyInfo.getSelection());
		notify.setWarnValidFlg(m_checkCloudNotifyWarning.getSelection());
		notify.setCriticalValidFlg(m_checkCloudNotifyCritical.getSelection());
		notify.setUnknownValidFlg(m_checkCloudNotifyUnknown.getSelection());

		// platform
		if (this.m_facilityId == null) {
			// facilityidが選択されていない場合は、バリデーションで弾く
			notify.setPlatformType(CloudNotifyDetailInfoResponse.PlatformTypeEnum.OTHER);
		} else {
			if (this.m_facilityId.contains(AWS_PLT_ID)) {
				notify.setPlatformType(CloudNotifyDetailInfoResponse.PlatformTypeEnum.AWS);
			} else if (this.m_facilityId.contains(AZURE_PLT_ID)) {
				notify.setPlatformType(CloudNotifyDetailInfoResponse.PlatformTypeEnum.AZURE);
			} else {
				// AWS, Azure以外はバリデーションで弾く
				notify.setPlatformType(CloudNotifyDetailInfoResponse.PlatformTypeEnum.OTHER);
			}
		}

		// 情報詳細
		if (m_InfoLinkInfo != null) {
			notify.setInfoAccessKey(m_InfoLinkInfo.getAccessKey());
			notify.setInfoDataVersion(m_InfoLinkInfo.getDataVersion());
			notify.setInfoDetailType(m_InfoLinkInfo.getDetailType());
			notify.setInfoEventBus(m_InfoLinkInfo.getEventBus());
			notify.setInfoKeyValueDataList(m_InfoLinkInfo.getDataList());
			notify.setInfoSource(m_InfoLinkInfo.getSource());
		}
		// 警告詳細
		if (m_WarnLinkInfo != null) {
			notify.setWarnAccessKey(m_WarnLinkInfo.getAccessKey());
			notify.setWarnDataVersion(m_WarnLinkInfo.getDataVersion());
			notify.setWarnDetailType(m_WarnLinkInfo.getDetailType());
			notify.setWarnEventBus(m_WarnLinkInfo.getEventBus());
			notify.setWarnKeyValueDataList(m_WarnLinkInfo.getDataList());
			notify.setWarnSource(m_WarnLinkInfo.getSource());
		}
		// 危険詳細
		if (m_CritLinkInfo != null) {
			notify.setCritAccessKey(m_CritLinkInfo.getAccessKey());
			notify.setCritDataVersion(m_CritLinkInfo.getDataVersion());
			notify.setCritDetailType(m_CritLinkInfo.getDetailType());
			notify.setCritEventBus(m_CritLinkInfo.getEventBus());
			notify.setCritKeyValueDataList(m_CritLinkInfo.getDataList());
			notify.setCritSource(m_CritLinkInfo.getSource());
		}
		// 不明詳細
		if (m_UnkLinkInfo != null) {
			notify.setUnkAccessKey(m_UnkLinkInfo.getAccessKey());
			notify.setUnkDataVersion(m_UnkLinkInfo.getDataVersion());
			notify.setUnkDetailType(m_UnkLinkInfo.getDetailType());
			notify.setUnkEventBus(m_UnkLinkInfo.getEventBus());
			notify.setUnkKeyValueDataList(m_UnkLinkInfo.getDataList());
			notify.setUnkSource(m_UnkLinkInfo.getSource());
		}
		return notify;
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		// 入力値生成
		this.inputData = this.createInputData();

		return super.validate();
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

		NotifyInfoInputData info = this.getInputData();
		if (info != null) {
			if (!this.updateFlg) {
				// 作成の場合
				result = new AddNotify().addCloudNotify(this.managerName, info);
			} else {
				// 変更の場合
				result = new ModifyNotify().modifyCloudNotify(this.managerName, info);
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
	 * @param id
	 *            ID
	 * @param message
	 *            メッセージ
	 */
	@Override
	protected void setValidateResult(String id, String message) {

		this.validateResult = new ValidateResult();
		this.validateResult.setValid(false);
		this.validateResult.setID(id);
		this.validateResult.setMessage(message);
	}

	/**
	 * ボタンを生成します。<BR>
	 * 参照フラグが<code> true </code>の場合は閉じるボタンを生成し、<code> false </code>
	 * の場合は、デフォルトのボタンを生成します。
	 *
	 * @param parent
	 *            ボタンバーコンポジット
	 *
	 * @see #createButtonsForButtonBar(Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		if (!this.referenceFlg) {
			super.createButtonsForButtonBar(parent);
		} else {
			// 閉じるボタン
			this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), false);
		}
	}

	/**
	 * コンポジットの選択可/不可を設定します。
	 *
	 * @param enable
	 *            選択可の場合、<code> true </code>
	 */
	@Override
	protected void setEnabled(boolean enable) {
		super.m_notifyBasic.setEnabled(enable);
		super.m_notifyInhibition.setEnabled(enable);
	}

	/**
	 * 引数で指定されたコンポジットの選択可/不可を設定します。
	 *
	 */
	private void setEnabled(boolean enabled, Button buttonSelect, Combo comboCopy, Button buttonCopy) {
		buttonSelect.setEnabled(enabled);
		comboCopy.setEnabled(enabled);
		buttonCopy.setEnabled(enabled);
	}

	/**
	 * 重要度のラベルを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param text
	 *            ラベルに表示するテキスト
	 * @param background
	 *            ラベルの背景色
	 * @return 生成されたラベル
	 */
	private Label getLabelPriority(Composite parent, String text, Color background) {

		// ラベル（重要度）
		Label label = new Label(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(text + " : ");
		label.setBackground(background);

		return label;
	}

	/**
	 * クラウド通知の実行チェックボックスを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Button getCheckCloudNotify(Composite parent) {
		// チェックボックス（実行）
		Button button = new Button(parent, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		return button;
	}

	/**
	 * コピー用コンボボックスを返します
	 *
	 * @param parent
	 *            親のコンポジット
	 * @return 生成されたコンボボックス
	 */
	private Combo getComboCopyPriority(Composite parent) {
		int blank = 0;

		// コンボボックス（通知状態）
		Combo copyPriorityCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);

		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_COMBO_FACILITY - blank;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		copyPriorityCombo.setLayoutData(gridData);

		// Facilityをコンボボックスに登録する
		copyPriorityCombo.add(Messages.getString("info"));
		copyPriorityCombo.add(Messages.getString("warning"));
		copyPriorityCombo.add(Messages.getString("critical"));
		copyPriorityCombo.add(Messages.getString("unknown"));

		// 空白
		if (blank > 0) {
			Label label = new Label(parent, SWT.NONE);
			gridData = new GridData();
			gridData.horizontalSpan = blank;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			label.setLayoutData(gridData);
		}

		return copyPriorityCombo;
	}

	private Button getSelectButton(Composite parent, Shell shell, int priority) {
		Button infoB = new Button(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		infoB.setLayoutData(gridData);
		infoB.setText(Messages.getString("refer"));
		infoB.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (priority == PriorityConstant.TYPE_INFO) {
					m_InfoNotifyCloudLinkInfoDialog = new NotifyCloudLinkInfoDialog(shell, isAWSFacility(m_facilityId));
					if (m_InfoLinkInfo != null) {
						m_InfoNotifyCloudLinkInfoDialog.setLinkInfoData(priority, m_InfoLinkInfo.getEventBus(),
								m_InfoLinkInfo.getAccessKey(), m_InfoLinkInfo.getDataVersion(),
								m_InfoLinkInfo.getDetailType(), m_InfoLinkInfo.getDataList(),
								m_InfoLinkInfo.getSource());
					}

					if (m_InfoNotifyCloudLinkInfoDialog.open() == IDialogConstants.OK_ID) {
						createLinkInfo(priority, m_InfoNotifyCloudLinkInfoDialog);
						update();
					}

				} else if (priority == PriorityConstant.TYPE_WARNING) {
					m_WanrNotifyCloudLinkInfoDialog = new NotifyCloudLinkInfoDialog(shell, isAWSFacility(m_facilityId));
					if (m_WarnLinkInfo != null) {
						m_WanrNotifyCloudLinkInfoDialog.setLinkInfoData(priority, m_WarnLinkInfo.getEventBus(),
								m_WarnLinkInfo.getAccessKey(), m_WarnLinkInfo.getDataVersion(),
								m_WarnLinkInfo.getDetailType(), m_WarnLinkInfo.getDataList(),
								m_WarnLinkInfo.getSource());
					}

					if (m_WanrNotifyCloudLinkInfoDialog.open() == IDialogConstants.OK_ID) {
						createLinkInfo(priority, m_WanrNotifyCloudLinkInfoDialog);
						update();
					}
				} else if (priority == PriorityConstant.TYPE_CRITICAL) {
					m_CritNotifyCloudLinkInfoDialog = new NotifyCloudLinkInfoDialog(shell, isAWSFacility(m_facilityId));
					if (m_CritLinkInfo != null) {
						m_CritNotifyCloudLinkInfoDialog.setLinkInfoData(priority, m_CritLinkInfo.getEventBus(),
								m_CritLinkInfo.getAccessKey(), m_CritLinkInfo.getDataVersion(),
								m_CritLinkInfo.getDetailType(), m_CritLinkInfo.getDataList(),
								m_CritLinkInfo.getSource());
					}

					if (m_CritNotifyCloudLinkInfoDialog.open() == IDialogConstants.OK_ID) {
						createLinkInfo(priority, m_CritNotifyCloudLinkInfoDialog);
						update();
					}
				} else if (priority == PriorityConstant.TYPE_UNKNOWN) {
					m_UnkNotifyCloudLinkInfoDialog = new NotifyCloudLinkInfoDialog(shell, isAWSFacility(m_facilityId));
					if (m_UnkLinkInfo != null) {
						m_UnkNotifyCloudLinkInfoDialog.setLinkInfoData(priority, m_UnkLinkInfo.getEventBus(),
								m_UnkLinkInfo.getAccessKey(), m_UnkLinkInfo.getDataVersion(),
								m_UnkLinkInfo.getDetailType(), m_UnkLinkInfo.getDataList(), m_UnkLinkInfo.getSource());
					}

					if (m_UnkNotifyCloudLinkInfoDialog.open() == IDialogConstants.OK_ID) {
						createLinkInfo(priority, m_UnkNotifyCloudLinkInfoDialog);
						update();
					}
				}

			}
		});

		return infoB;
	}

	private void createLinkInfo(int priority, NotifyCloudLinkInfoDialog dialog) {

		LinkInfo linkInfo = new LinkInfo();

		linkInfo.setEventBus(dialog.getComposite().getEventBus());
		linkInfo.setDetailType(dialog.getComposite().getDetailType());
		linkInfo.setSource(dialog.getComposite().getSource());

		if (!dialog.isAWS()) {
			linkInfo.setAccessKey(dialog.getComposite().getAccessKey());
			linkInfo.setDataVersion(dialog.getComposite().getDataVersion());
		}
	
		linkInfo.setDataList(dialog.getComposite().getDataList());
	

		if (priority == PriorityConstant.TYPE_INFO) {
			m_InfoLinkInfo = linkInfo;
		} else if (priority == PriorityConstant.TYPE_WARNING) {
			m_WarnLinkInfo = linkInfo;

		} else if (priority == PriorityConstant.TYPE_CRITICAL) {
			m_CritLinkInfo = linkInfo;
		} else if (priority == PriorityConstant.TYPE_UNKNOWN) {
			m_UnkLinkInfo = linkInfo;
		}

	}

	private Button getCopyButton(Composite parent, Shell shell, int priority) {
		Button copyB = new Button(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		copyB.setLayoutData(gridData);
		copyB.setText(Messages.getString("copy"));
		copyB.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (priority == PriorityConstant.TYPE_INFO) {
					m_InfoNotifyCloudLinkInfoDialog = new NotifyCloudLinkInfoDialog(shell, isAWSFacility(m_facilityId));

					copyDialog(priority, m_InfoNotifyCloudLinkInfoDialog, m_infoCopyCombo);

				} else if (priority == PriorityConstant.TYPE_WARNING) {
					m_WanrNotifyCloudLinkInfoDialog = new NotifyCloudLinkInfoDialog(shell, isAWSFacility(m_facilityId));
					copyDialog(priority, m_WanrNotifyCloudLinkInfoDialog, m_warnCopyCombo);

				} else if (priority == PriorityConstant.TYPE_CRITICAL) {
					m_CritNotifyCloudLinkInfoDialog = new NotifyCloudLinkInfoDialog(shell, isAWSFacility(m_facilityId));
					copyDialog(priority, m_CritNotifyCloudLinkInfoDialog, m_errorCopyCombo);

				} else if (priority == PriorityConstant.TYPE_UNKNOWN) {
					m_UnkNotifyCloudLinkInfoDialog = new NotifyCloudLinkInfoDialog(shell, isAWSFacility(m_facilityId));
					copyDialog(priority, m_UnkNotifyCloudLinkInfoDialog, m_unkCopyCombo);
				}

			}
		});

		return copyB;
	}

	private void copyDialog(int priority, NotifyCloudLinkInfoDialog dialog, Combo combo) {
		LinkInfo linkInfo = null;
		if (combo.getText().equals(PriorityMessage.STRING_INFO)) {
			linkInfo = m_InfoLinkInfo;
		} else if (combo.getText().equals(PriorityMessage.STRING_WARNING)) {
			linkInfo = m_WarnLinkInfo;
		} else if (combo.getText().equals(PriorityMessage.STRING_CRITICAL)) {
			linkInfo = m_CritLinkInfo;
		} else {
			linkInfo = m_UnkLinkInfo;
		}

		if (linkInfo != null && !linkInfo.isEmpty()) {
			dialog.setLinkInfoData(priority, linkInfo.getEventBus(), linkInfo.getAccessKey(), linkInfo.getDataVersion(),
					linkInfo.getDetailType(), linkInfo.getDataList(), linkInfo.getSource());
		} else {
			MessageDialog.openWarning(null, Messages.getString("warning"),
					Messages.getString("message.notify.cloud.3"));
			return;
		}

		if (dialog.open() == IDialogConstants.OK_ID) {
			createLinkInfo(priority, dialog);
			update();
		}
	}

	// facilityIDがAWSの物であるかを確認
	private boolean isAWSFacility(String facilityID) {
		return facilityID.contains(AWS_PLT_ID);
	}

	/**
	 * クラウド通知の連携情報を一時的に保持するプライベートクラス
	 *
	 */
	private static class LinkInfo {
		// findbugs対応 不要な変数 priority を除去
		
		// Azureの場合エンドポイント
		private String eventBus = "";
		// Azureの場合サブジェクト
		private String detailType = "";

		// Azureの場合イベントタイプ
		private String source = "";
		// 以下Azureのみ
		private String dataVersion = "";

		private String accessKey = "";
		// jsonデータでディテールおよびデータを保存
		private List<CloudNotifyLinkInfoKeyValueObjectResponse> dataList;

		public String getEventBus() {
			return eventBus;
		}

		public void setEventBus(String eventBus) {
			if (eventBus == null) {
				eventBus = "";
			}
			this.eventBus = eventBus;
		}

		public String getDetailType() {
			return detailType;
		}

		public void setDetailType(String detailType) {
			if (detailType == null) {
				detailType = "";
			}
			this.detailType = detailType;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			if (source == null) {
				source = "";
			}
			this.source = source;
		}

		public String getDataVersion() {
			return dataVersion;
		}

		public void setDataVersion(String dataVersion) {
			if (dataVersion == null) {
				dataVersion = "";
			}
			this.dataVersion = dataVersion;
		}

		public String getAccessKey() {
			return accessKey;
		}

		public void setAccessKey(String accessKey) {
			if (accessKey == null) {
				accessKey = "";
			}
			this.accessKey = accessKey;
		}

		public List<CloudNotifyLinkInfoKeyValueObjectResponse> getDataList() {
			return dataList;
		}

		public void setDataList(List<CloudNotifyLinkInfoKeyValueObjectResponse> list) {
			if (list == null) {
				this.dataList = new ArrayList<CloudNotifyLinkInfoKeyValueObjectResponse>();
				
				return;
			}
			// DeepCopy
			this.dataList = new ArrayList<CloudNotifyLinkInfoKeyValueObjectResponse>(list);
		}

		public boolean isEmpty() {
			return eventBus.isEmpty() && detailType.isEmpty() && source.isEmpty() && dataVersion.isEmpty()
					&& accessKey.isEmpty() && (dataList == null || dataList.isEmpty());
		}

	}

	@Override
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		return CloudRestClientWrapper.getWrapper(managerName);
	}
	

}
