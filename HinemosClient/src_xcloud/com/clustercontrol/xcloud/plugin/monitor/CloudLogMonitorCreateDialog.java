/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.monitor;

import java.lang.reflect.Field;
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TypedListener;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.AddCloudLogMonitorRequest;
import org.openapitools.client.model.CloudPlatformInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;
import org.openapitools.client.model.ModifyCloudLogMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorPluginStringInfoResponse;
import org.openapitools.client.model.PluginCheckInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.logfile.bean.LogfileLineSeparatorConstant;
import com.clustercontrol.monitor.run.dialog.CommonMonitorStringDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.notify.bean.PriChangeJudgeSelectTypeConstant;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.xcloud.bean.CloudConstant;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.InvalidStateException;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

/**
 * クラウドログ監視作成・変更ダイアログクラス<BR>
 * 
 */
public class CloudLogMonitorCreateDialog extends CommonMonitorStringDialog implements CloudStringConstants {
	public static final long serialVersionUID = 1L;


	private static final Log logger = LogFactory.getLog(CloudLogMonitorCreateDialog.class);
	private static final String DEFAULT_OFFSET = "10";

	private String service = CloudConstant.platform_AWS;
	private Text m_logText;
	private Text m_logSText;
	Composite aws_composite;
	Composite azure_composite;
	private Combo m_fileReturnCode;
	private Combo m_resourceGroup;
	private Label m_labelCloudService;
	private TabFolder tabFolder;
	private Label m_logLabel;
	private Label m_logSlabel;
	private Label m_resourceLabel;
	private Text txtPatternHead;
	protected String m_agentFacilityId;
	private Text txtPatternTail;
	private Text txtMaxBytes;
	private Label azt_colLabel;
	private Text azt_colSText;
	private Button confirmPrefixValid;
	private Label m_whiteLabel;
	private TabItem tabCheckRule;
	private Label m_workspace;
	private Text m_workspaceText;
	private Label m_tablelabel;
	private Text m_tableText;
	private Text m_textScope_aws;
	private Text m_textScope_azure;
	private boolean isAzureCreated = false;
	private Label m_offsetlable;
	private Text m_offsetText;
	

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 * 
	 * @param parent
	 *            親のシェルオブジェクト
	 * @wbp.parser.constructor
	 */
	public CloudLogMonitorCreateDialog(Shell parent) {
		super(parent, null);
		this.priorityChangeJudgeSelect = PriChangeJudgeSelectTypeConstant.TYPE_PATTERN;
		this.logLineFlag = true;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 * 
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param monitorId
	 *            変更する監視項目ID
	 * @param updateFlg
	 *            更新するか否か（true:変更、false:新規登録）
	 * 
	 */
	public CloudLogMonitorCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);

		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
		this.managerName = managerName;
		this.priorityChangeJudgeSelect = PriChangeJudgeSelectTypeConstant.TYPE_PATTERN;
		this.logLineFlag = true;
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
		super.customizeDialog(parent);

		getCloudScopeSelection();

		// タイトル
		shell.setText(dlgCloudLogCreateModify);

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// クラウドスコープ変更時にタブの情報を更新する
		getMonitorBasicScope().getButtonScope().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				cloudUpdate(false);
			}
		});

		/*
		 * 監視対象クラウド
		 */
		// ラベル
		Label m_labelCloud = new Label(groupRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_labelCloud.setLayoutData(gridData);
		m_labelCloud.setText(Messages.getString("xcloud.service") + " : ");

		m_labelCloudService = new Label(groupRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_labelCloudService.setLayoutData(gridData);
		String services = CloudConstant.platform_AWS;
		m_labelCloudService.setText(services);

		// クラウドログ監視設定用タブ作成
		createTab();

		// ダイアログを調整
		this.adjustDialog();

		// 初期表示
		MonitorInfoResponse info = null;
		if (this.monitorId == null) {
			// 作成の場合
			info = new MonitorInfoResponse();
			this.setInfoInitialValue(info);
			this.setInputData(info);
		} else {
			// 変更の場合、情報取得
			try {
				info = MonitorsettingRestClientWrapper.getWrapper(managerName).getMonitor(this.monitorId);
				this.setInputData(info);
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));

			} catch (Exception e) {
				// 上記以外の例外
				logger.warn("customizeDialog() getMonitor, ", e);
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.hinemos.failure.unexpected") + ", "
								+ HinemosMessage.replace(e.getMessage()));

			}
		}

	}

	/**
	 * 各項目に入力値を設定します。
	 * 
	 * @param monitor
	 *            設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(MonitorInfoResponse monitor) {
		
		// 設定値のある状態（変更かコピー）で呼ばれたのかを判定
		boolean isModifyOrCopy = monitor.getPluginCheckInfo() != null
				&& monitor.getPluginCheckInfo().getMonitorPluginStringInfoList() != null
				&& !monitor.getPluginCheckInfo().getMonitorPluginStringInfoList().isEmpty();
		
		// アクセス権限のチェックとデフォルトレイアウトの決定のため、
		// インストール済みのクラウド管理機能の一覧を取得
		IHinemosManager manager = null;
		boolean azureFlg = false;
		try {
			if (isModifyOrCopy) {
				manager = ClusterControlPlugin.getDefault().getHinemosManager(managerName);
			} else {
				// 新規作成時は先頭のマネージャを取得
				manager = ClusterControlPlugin.getDefault().getHinemosManagers().get(0);
			}
			// エンドポイントがPublishされていることを確認
			boolean isPublish = manager.getWrapper().checkPublish().getPublish();
			if (!isPublish) {
				// エンドポイントはPublishされているがキーファイルが期限切れの場合
				throw new HinemosUnknown(Messages.getString("message.expiration.term.invalid"));
			}
			List<CloudPlatformInfoResponse> platformList = manager.getWrapper().getAllCloudPlatforms();
			int pCount = 0;
			for (CloudPlatformInfoResponse platform : platformList) {
				if (platform.getCloudSpec().getPublicCloud()) {
					pCount++;
					if (platform.getEntity().getPlatformId().equals(CloudConstant.platform_Azure)) {
						azureFlg = true;
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("setInputData(); avaliable platform: " + platform.getEntity().getPlatformId());
				}
			}
			if (pCount != 1) {
				azureFlg = false;
			}

		} catch (InvalidRole e) {
			// 他のクラウド系監視に合わせて、新規作成時はエラーダイアログを表示しない
			if (isModifyOrCopy) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"), e.getMessage());
				return;
			}
		} catch (Exception e) {
			// 上記以外の例外
			logger.warn("setInputData(): " + e.getMessage(), e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());
		}

		super.setInputData(monitor);
		this.inputData = monitor;
		m_stringValueInfo.setInputData(monitor);

		if (isModifyOrCopy) {
			// ダイアログに反映
			service = getPluginInfoValue(CloudConstant.cloudLog_platform, monitor);
			cloudUpdate(false);

			if (service.equals(CloudConstant.platform_Azure)) {
				m_resourceGroup.setText(getPluginInfoValue(CloudConstant.cloudLog_ResourceGroup, monitor));
				m_textScope_azure.setText(getPluginInfoValue(CloudConstant.cloudLog_targetScope, monitor));
				m_workspaceText.setText(getPluginInfoValue(CloudConstant.cloudLog_LogGroup, monitor));
				azt_colSText.setText(getPluginInfoValue(CloudConstant.cloudLog_Col, monitor));
				m_tableText.setText(getPluginInfoValue(CloudConstant.cloudLog_LogStream, monitor));
			}

			m_textScope_aws.setText(getPluginInfoValue(CloudConstant.cloudLog_targetScope, monitor));
			m_agentFacilityId = getPluginInfoValue(CloudConstant.cloudLog_targetScopeFacilityId, monitor);
			m_logText.setText(getPluginInfoValue(CloudConstant.cloudLog_LogGroup, monitor));
			m_logSText.setText(getPluginInfoValue(CloudConstant.cloudLog_LogStream, monitor));
			confirmPrefixValid
					.setSelection(Boolean.parseBoolean(getPluginInfoValue(CloudConstant.cloudLog_isPrefix, monitor)));
			m_offsetText.setText(getPluginInfoValue(CloudConstant.cloudLog_Offset, monitor));

			m_fileReturnCode.setText(getPluginInfoValue(CloudConstant.cloudLog_ReturnCode, monitor));
			txtPatternHead.setText(getPluginInfoValue(CloudConstant.cloudLog_patternHead, monitor));
			txtPatternTail.setText(getPluginInfoValue(CloudConstant.cloudLog_patternTail, monitor));
			txtMaxBytes.setText(getPluginInfoValue(CloudConstant.cloudLog_maxBytes, monitor));
		} else {
			// クラウド管理Azureのみ導入済みの場合はデフォルト画面をAzureにする
			cloudUpdate(azureFlg);
		}
	}

	// キーに対応するMonitorPluginStringInfoを取得
	private String getPluginInfoValue(String key, MonitorInfoResponse monitor) {
		for (MonitorPluginStringInfoResponse s : monitor.getPluginCheckInfo().getMonitorPluginStringInfoList()) {
			if (key.equals(s.getKey())) {
				if (s.getValue() == null) {
					logger.debug("getPluginInfoValue(): value empty:" + key);
					return "";
				} else {
					logger.debug("getPluginInfoValue(): found key:" + key + " value:" + s.getValue());
					return s.getValue();
				}
			}
		}

		logger.debug("getPluginInfoValue(): key not found:" + key);

		return "";
	}

	/**
	 * 入力値を用いてクラウドログ監視情報を生成します。
	 * 
	 * @return 入力値を保持した監視
	 */
	@Override
	protected MonitorInfoResponse createInputData() {
		super.createInputData();
		if (validateResult != null) {
			return null;
		}

		// 監視条件 クラウドログ監視
		PluginCheckInfoResponse pluginCheckInfo = new PluginCheckInfoResponse();
		List<MonitorPluginStringInfoResponse> stringInfos = pluginCheckInfo.getMonitorPluginStringInfoList();

		// 対象サービス
		MonitorPluginStringInfoResponse platform = new MonitorPluginStringInfoResponse();
		platform.setKey(CloudConstant.cloudLog_platform);
		platform.setValue(service);
		stringInfos.add(platform);

		// 監視実行スコープ（ファシリティ名）
		MonitorPluginStringInfoResponse scope = new MonitorPluginStringInfoResponse();
		scope.setKey(CloudConstant.cloudLog_targetScope);

		// 監視実行スコープ
		MonitorPluginStringInfoResponse scopeFacilityId = new MonitorPluginStringInfoResponse();
		scopeFacilityId.setKey(CloudConstant.cloudLog_targetScopeFacilityId);

		// ログ取得遅延
		MonitorPluginStringInfoResponse offset = new MonitorPluginStringInfoResponse();
		offset.setKey(CloudConstant.cloudLog_Offset);
				
		// ロググループ/ワークスペース名
		MonitorPluginStringInfoResponse logG = new MonitorPluginStringInfoResponse();
		logG.setKey(CloudConstant.cloudLog_LogGroup);

		// ログストリーム/テーブル名
		MonitorPluginStringInfoResponse logS = new MonitorPluginStringInfoResponse();
		logS.setKey(CloudConstant.cloudLog_LogStream);

		// プリフィックス（AWSのみ）
		MonitorPluginStringInfoResponse prefix = new MonitorPluginStringInfoResponse();
		prefix.setKey(CloudConstant.cloudLog_isPrefix);

		// カラム名（Azureのみ）
		MonitorPluginStringInfoResponse col = new MonitorPluginStringInfoResponse();
		col.setKey(CloudConstant.cloudLog_Col);

		// リソースグループ名（Azureのみ）
		MonitorPluginStringInfoResponse rg = new MonitorPluginStringInfoResponse();
		rg.setKey(CloudConstant.cloudLog_ResourceGroup);

		// テキストボックスから文字列を取得
		if (this.m_agentFacilityId != null && !"".equals(m_agentFacilityId)) {
			scopeFacilityId.setValue(this.m_agentFacilityId);
		}
		stringInfos.add(scopeFacilityId);

		if (service.equals(CloudConstant.platform_AWS)) {
			if (this.m_textScope_aws.getText() != null && !"".equals(m_textScope_aws.getText())) {
				scope.setValue(this.m_textScope_aws.getText());
			}
			if (this.m_logText.getText() != null && !"".equals(this.m_logText.getText())) {
				logG.setValue(this.m_logText.getText());
			}
			if (this.m_logSText.getText() != null && !"".equals(this.m_logSText.getText())) {
				logS.setValue(this.m_logSText.getText());
			}
			if (this.confirmPrefixValid.getSelection()) {
				prefix.setValue(Boolean.toString(true));
			} else {
				prefix.setValue(Boolean.toString(false));
			}
			if (this.m_offsetText.getText() != null && !"".equals(this.m_offsetText.getText())) {
				offset.setValue(this.m_offsetText.getText());
			}
			stringInfos.add(prefix);
			stringInfos.add(offset);
			
		} else {
			if (this.m_textScope_azure.getText() != null && !"".equals(m_textScope_azure.getText())) {
				scope.setValue(this.m_textScope_azure.getText());
			}
			if (this.m_workspaceText.getText() != null && !"".equals(this.m_workspaceText.getText())) {
				logG.setValue(this.m_workspaceText.getText());
			}
			if (this.m_tableText.getText() != null && !"".equals(this.m_tableText.getText())) {
				logS.setValue(this.m_tableText.getText());
			}
			if (this.m_resourceGroup != null && this.m_resourceGroup.getText() != null
					&& !"".equals(this.m_resourceGroup.getText())) {
				rg.setValue(this.m_resourceGroup.getText());
			}
			stringInfos.add(rg);

			if (this.azt_colSText != null && this.azt_colSText.getText() != null
					&& !"".equals(this.azt_colSText.getText())) {
				col.setValue(this.azt_colSText.getText());
			}
			stringInfos.add(col);
		}
		stringInfos.add(scope);
		stringInfos.add(logG);
		stringInfos.add(logS);


		// 区切り条件タブ
		// 改行コード
		MonitorPluginStringInfoResponse retCode = new MonitorPluginStringInfoResponse();
		retCode.setKey(CloudConstant.cloudLog_ReturnCode);
		// 先頭文字列
		MonitorPluginStringInfoResponse patternHead = new MonitorPluginStringInfoResponse();
		patternHead.setKey(CloudConstant.cloudLog_patternHead);
		// 終端文字列
		MonitorPluginStringInfoResponse petternTail = new MonitorPluginStringInfoResponse();
		petternTail.setKey(CloudConstant.cloudLog_patternTail);
		// 最大読み取り文字数
		MonitorPluginStringInfoResponse maxByte = new MonitorPluginStringInfoResponse();
		maxByte.setKey(CloudConstant.cloudLog_maxBytes);

		if (this.m_fileReturnCode.getText() != null && !"".equals(this.m_fileReturnCode.getText())) {
			retCode.setValue(this.m_fileReturnCode.getText());
		}
		stringInfos.add(retCode);

		patternHead.setValue(this.txtPatternHead.getText());
		stringInfos.add(patternHead);
		petternTail.setValue(this.txtPatternTail.getText());
		stringInfos.add(petternTail);

		if (this.txtMaxBytes.getText() != null && !"".equals(this.txtMaxBytes.getText())) {
			maxByte.setValue(this.txtMaxBytes.getText());
		}
		stringInfos.add(maxByte);

		monitorInfo.setPluginCheckInfo(pluginCheckInfo);

		// 結果判定の定義
		validateResult = m_stringValueInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			return null;
		}

		// 通知関連情報とアプリケーションの設定
		validateResult = m_notifyInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			if (validateResult.getID() == null) { // 通知ID警告用出力
				if (!displayQuestion(validateResult)) {
					validateResult = null;
					return null;
				}
			} else { // アプリケーション未入力チェック
				return null;
			}
		}

		return monitorInfo;
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

		String[] args = { this.inputData.getMonitorId(), getManagerName() };
		MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(getManagerName());
		if (!this.updateFlg) {
			// 作成の場合
			try {
				AddCloudLogMonitorRequest info = new AddCloudLogMonitorRequest();
				RestClientBeanUtil.convertBean(this.inputData, info);
				info.setRunInterval(AddCloudLogMonitorRequest.RunIntervalEnum
						.fromValue(this.inputData.getRunInterval().getValue()));
				wrapper.addCloudLogMonitor(info);
				MessageDialog.openInformation(null, Messages.getString("successful"),
						Messages.getString("message.monitor.33", args));
				result = true;
			} catch (MonitorDuplicate e) {
				// 監視項目IDが重複している場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.monitor.53", args));
			} catch (Exception e) {
				String errMessage = "";
				if (e instanceof InvalidRole) {
					// アクセス権なしの場合、エラーダイアログを表示する
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} else {
					errMessage = ", " + e.getMessage();
				}

				MessageDialog.openError(null, Messages.getString("failed"),
						Messages.getString("message.monitor.34", args) + errMessage);
			}
		} else {
			// 変更の場合
			try {
				ModifyCloudLogMonitorRequest info = new ModifyCloudLogMonitorRequest();
				RestClientBeanUtil.convertBean(this.inputData, info);
				info.setRunInterval(ModifyCloudLogMonitorRequest.RunIntervalEnum
						.fromValue(this.inputData.getRunInterval().getValue()));

				wrapper.modifyCloudLogMonitor(this.inputData.getMonitorId(), info);
				MessageDialog.openInformation(null, Messages.getString("successful"),
						Messages.getString("message.monitor.35", args));
				result = true;
			} catch (Exception e) {
				String errMessage = "";
				if (e instanceof InvalidRole) {
					// アクセス権なしの場合、エラーダイアログを表示する
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} else {
					errMessage = ", " + e.getMessage();
				}
				MessageDialog.openError(null, Messages.getString("failed"),
						Messages.getString("message.monitor.36", args) + errMessage);
			}
		}

		return result;

	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update() {
		// 監視対象実行エージェントが必須項目であることを明示
		if (this.m_textScope_aws != null && this.m_textScope_aws.getEnabled()
				&& "".equals(this.m_textScope_aws.getText())) {
			this.m_textScope_aws.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else if (this.m_textScope_aws != null) {
			this.m_textScope_aws.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (this.m_textScope_azure != null && this.m_textScope_azure.getEnabled()
				&& "".equals(this.m_textScope_azure.getText())) {
			this.m_textScope_azure.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else if (this.m_textScope_azure != null) {
			this.m_textScope_azure.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// ログ取得遅延
		if (this.m_offsetText != null && this.m_offsetText.getEnabled() && "".equals(this.m_offsetText.getText())) {
			this.m_offsetText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else if (this.m_offsetText != null) {
			this.m_offsetText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// リソースグループ
		if (this.m_resourceGroup != null && this.m_resourceGroup.getEnabled()
				&& "".equals(this.m_resourceGroup.getText())) {
			this.m_resourceGroup.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else if (this.m_resourceGroup != null) {
			this.m_resourceGroup.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// ロググループ
		if (this.m_logText != null && this.m_logText.getEnabled() && "".equals(this.m_logText.getText())) {
			this.m_logText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else if (this.m_logText != null) {
			this.m_logText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// カラム
		if (this.azt_colSText != null && this.azt_colSText.getEnabled() && "".equals(this.azt_colSText.getText())) {
			this.azt_colSText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else if (this.azt_colSText != null) {
			this.azt_colSText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// ワークスペース
		if (this.m_workspaceText != null && this.m_workspaceText.getEnabled()
				&& "".equals(this.m_workspaceText.getText())) {
			this.m_workspaceText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else if (this.m_workspaceText != null) {
			this.m_workspaceText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// テーブル
		if (this.m_tableText != null && this.m_tableText.getEnabled() && "".equals(this.m_tableText.getText())) {
			this.m_tableText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else if (this.m_tableText != null) {
			this.m_tableText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 先頭パターンに入力項目がある場合は終端パターンを無効化
		if (this.txtPatternHead != null && this.txtPatternHead.getEnabled()
				&& !this.txtPatternHead.getText().isEmpty()) {
			this.txtPatternTail.setEditable(false);
		} else if (this.txtPatternHead != null) {
			this.txtPatternTail.setEditable(true);
		}

		// 終端パターンに入力項目がある場合は先頭パターンを無効化
		if (this.txtPatternTail != null && this.txtPatternTail.getEnabled()
				&& !this.txtPatternTail.getText().isEmpty()) {
			this.txtPatternHead.setEditable(false);
		} else if (this.txtPatternTail != null) {
			this.txtPatternHead.setEditable(true);
		}

	}
	
	
	/**
	 * オーナーロール変更時にクラウドログタブの内容をクリアする
	 * 
	 * @param ownerRoleId
	 */
	@Override
	public void updateOwnerRole(String ownerRoleId) {
		super.updateOwnerRole(ownerRoleId);
		// azure固有項目のクリア
		if (isAzureCreated && service.equals(CloudConstant.platform_Azure)) {
			m_resourceGroup.clearSelection();
			m_resourceGroup.deselectAll();
			m_resourceGroup.removeAll();
			m_textScope_azure.setText("");
			azt_colSText.setText("");
			m_workspaceText.setText("");
			m_tableText.setText("");
		} else {
			// AWS固有項目
			m_logText.setText("");
			m_logSText.setText("");
			m_textScope_aws.setText("");
			m_offsetText.setText(DEFAULT_OFFSET);
		}
		update();
	}

	/**
	 * クラウドスコープのプラットフォーム毎に設定項目を切り替えるメソッド
	 */
	private void cloudUpdate(boolean forceAzure) {
		// 取得できない場合は強制的にAWS
		if ((m_monitorBasic.getFacilityId() != null && !m_monitorBasic.getFacilityId().isEmpty()
				&& m_monitorBasic.getFacilityId().contains(CloudConstant.platform_Azure)) || forceAzure) {
			service = CloudConstant.platform_Azure;
		} else {
			service = CloudConstant.platform_AWS;
		}

		if (service.equals(CloudConstant.platform_Azure)) {
			// composite切り替え
			if (!isAzureCreated) {
				createAzureComposite();
			}
			if (!tabCheckRule.getControl().equals(azure_composite)) {
				tabCheckRule.setControl(azure_composite);
			}

			// resourceGroupセレクションの初期化
			m_resourceGroup.clearSelection();
			m_resourceGroup.deselectAll();
			m_resourceGroup.removeAll();

			m_labelCloudService.setText(CloudConstant.platform_Azure);
			// 選択したファシリティIDからリソースグループ名を抽出
			if (!forceAzure) {
				FacilityTreeItemResponse treeItem;
				try {
					treeItem = RepositoryRestClientWrapper.getWrapper(managerName)
							.getFacilityTree(m_monitorBasic.getOwnerRoleId());
				} catch (HinemosUnknown | InvalidRole | InvalidUserPass | RestConnectFailed e) {
					logger.warn(e.getMessage(), e);
					throw new InvalidStateException(e.getMessage(), e);
				}
				ArrayList<String> resourceGroupList = getResourceGroupRecursive(treeItem,
						m_monitorBasic.getFacilityId());
				if (resourceGroupList != null) {
					for (String s : resourceGroupList) {
						m_resourceGroup.add(s);
					}
				}
			}
			// azureのみ有効な設定項目を表示
			azt_colSText.setToolTipText(Messages.getString("xcloud.log.col.tooltip"));
		} else {
			// AWS用コンポジットの作成
			m_labelCloudService.setText(CloudConstant.platform_AWS);
			m_logLabel.setText(Messages.getString("xcloud.loggroup") + " : ");
			m_logSlabel.setText(Messages.getString("xcloud.logstream") + ": ");

			if (!tabCheckRule.getControl().equals(aws_composite)) {
				tabCheckRule.setControl(aws_composite);
			}
		}
		tabFolder.setSelection(new TabItem[] { tabCheckRule });

	}

	/**
	 * Azureのクラウドスコープツリーからリソースグループ名を取得するメソッド
	 * 
	 * @param tree
	 * @param faciltiyId
	 * @return
	 */
	private ArrayList<String> getResourceGroupRecursive(FacilityTreeItemResponse tree, String faciltiyId) {
		ArrayList<String> workspaceList = new ArrayList<String>();

		if (tree.getChildren() == null || tree.getChildren().isEmpty()) {
			return null;
		}

		// ツリーを探索し、選択したファシリティIDのツリーを見つける
		// 見つかった場合その子がリソースグループ名を保持している
		if (tree.getData().getFacilityId().equals(faciltiyId)) {
			logger.info("found target: " + tree.getData().getFacilityId());
			for (FacilityTreeItemResponse t : tree.getChildren()) {
				workspaceList.add(t.getData().getFacilityName());
			}
			return workspaceList;
		}

		// ファシリティIDが一致しなかった場合は子を探索
		for (FacilityTreeItemResponse t : tree.getChildren()) {
			ArrayList<String> tmplist = getResourceGroupRecursive(t, faciltiyId);
			// 一致するファシリティIDがあった場合は、そのリソースグループ名を返す
			if (tmplist != null && !tmplist.isEmpty()) {
				return tmplist;
			}
		}

		return null;
	}

	/**
	 * クラウドログ監視設定用のタブを作成
	 */
	private void createTab() {

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// ファイル情報および区切り条件をタブにまとめる
		tabFolder = new TabFolder(groupRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		tabFolder.setLayoutData(gridData);
		tabCheckRule = new TabItem(tabFolder, SWT.NONE);
		tabCheckRule.setText(Messages.getString("xcloud.log"));
		TabItem tabDelimiter = new TabItem(tabFolder, SWT.NONE);
		tabDelimiter.setText(Messages.getString("file.delimiter"));

		/*
		 * チェック設定グループ（条件グループの子グループ）
		 */
		azure_composite = new Composite(tabFolder, SWT.NONE);
		aws_composite = new Composite(tabFolder, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = BASIC_UNIT;
		azure_composite.setLayout(layout);
		aws_composite.setLayout(layout);

		/*
		 * スコープ
		 */
		// ラベル
		Label m_labelScope = new Label(aws_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_labelScope.setLayoutData(gridData);
		m_labelScope.setText(Messages.getString("xcloud.agent") + " : ");
		// テキスト
		this.m_textScope_aws = new Text(aws_composite, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textScope_aws.setLayoutData(gridData);
		this.m_textScope_aws.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 参照ボタン
		Button m_buttonScope = new Button(aws_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_buttonScope.setLayoutData(gridData);
		m_buttonScope.setText(Messages.getString("refer"));
		m_buttonScope.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				String managerName = m_monitorBasic.getManagerListComposite().getText();
				// エンドポイントがPublishされていることを確認
				ValidateResult result = validateEndpoint(managerName);
				if (result != null) {
					displayError(result);
					return;
				}
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell, managerName, m_monitorBasic.getOwnerRoleId(), false,
						m_unregistered);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse item = dialog.getSelectItem();
					FacilityInfoResponse info = item.getData();
					m_agentFacilityId = info.getFacilityId();
					if (info.getFacilityType() == FacilityTypeEnum.NODE) {
						m_textScope_aws.setText(info.getFacilityName());
					} else {
						FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
						m_textScope_aws.setText(path.getPath(item));
					}
				}
			}
		});

		// 空白
		label = new Label(aws_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		// ログ取得遅延（秒）
		// ラベル
		m_offsetlable = new Label(aws_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_offsetlable.setLayoutData(gridData);
		m_offsetlable.setText(Messages.getString("xcloud.logoffset") + " : ");

		// テキスト
		this.m_offsetText = new Text(aws_composite, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_offsetText.setLayoutData(gridData);
		this.m_offsetText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// デフォルト10秒
		this.m_offsetText.setText(DEFAULT_OFFSET);
		
		// ロググループ
		// ラベル
		m_logLabel = new Label(aws_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_logLabel.setLayoutData(gridData);
		m_logLabel.setText(Messages.getString("xcloud.loggroup") + " : ");

		// テキスト
		this.m_logText = new Text(aws_composite, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_logText.setLayoutData(gridData);
		this.m_logText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// ログストリーム
		// ラベル
		m_logSlabel = new Label(aws_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_logSlabel.setLayoutData(gridData);
		m_logSlabel.setText(Messages.getString("xcloud.logstream") + ": ");

		// テキスト
		this.m_logSText = new Text(aws_composite, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_logSText.setLayoutData(gridData);
		this.m_logSText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		this.m_logSText.setToolTipText(Messages.getString("xcloud.log.stream.tooltip"));

		// 空白
		m_whiteLabel = new Label(aws_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_whiteLabel.setLayoutData(gridData);

		// プリフィックス（有効／無効）
		this.confirmPrefixValid = new Button(aws_composite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.confirmPrefixValid.setLayoutData(gridData);
		this.confirmPrefixValid.setText(Messages.getString("xcloud.prefix"));

		// 空白
		label = new Label(aws_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		
		
		// AWS用コンポジットの配置
		tabCheckRule.setControl(aws_composite);

		// 区切り条件
		Composite delimiter = new Composite(tabFolder, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = BASIC_UNIT;
		delimiter.setLayout(layout);

		// 先頭パターン（正規表現）
		// ラベル
		Label lblPrePattern = new Label(delimiter, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG + WIDTH_TITLE_MIDDLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblPrePattern.setLayoutData(gridData);
		lblPrePattern.setText(Messages.getString("file.delimiter.pattern.head") + " : ");
		// テキスト
		txtPatternHead = new Text(delimiter, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT - WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		txtPatternHead.setLayoutData(gridData);
		txtPatternHead.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 終端パターン（正規表現）
		// ラベル
		Label lblSufPattern = new Label(delimiter, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG + WIDTH_TITLE_MIDDLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblSufPattern.setLayoutData(gridData);
		lblSufPattern.setText(Messages.getString("file.delimiter.pattern.tail") + " : ");
		// テキスト
		txtPatternTail = new Text(delimiter, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT - WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		txtPatternTail.setLayoutData(gridData);
		txtPatternTail.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		VerifyListener verifier = new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				String text = e.text;
				if (e.character == SWT.BS || e.character == SWT.DEL) {
					return;
				}
				if (e.text.equals("")) {
					return;
				}
				if (!text.matches("^[0-9]+$")) {
					e.doit = false;
				}
			}
		};

		// 改行コード
		// ラベル
		label = new Label(delimiter, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG + WIDTH_TITLE_MIDDLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("returncode") + " : ");
		// コンボボックス
		this.m_fileReturnCode = new Combo(delimiter, SWT.DROP_DOWN | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT - WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_fileReturnCode.setLayoutData(gridData);

		m_fileReturnCode.add(LogfileLineSeparatorConstant.LF);
		m_fileReturnCode.add(LogfileLineSeparatorConstant.CR);
		m_fileReturnCode.add(LogfileLineSeparatorConstant.CRLF);
		m_fileReturnCode.setText(LogfileLineSeparatorConstant.LF);// デフォルト

		tabDelimiter.setControl(delimiter);

		// 最大読み取りバイト長（Byte)
		// ラベル
		Label lblReadByte = new Label(delimiter, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG + WIDTH_TITLE_MIDDLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblReadByte.setLayoutData(gridData);
		lblReadByte.setText(Messages.getString("file.delimiter.chars") + " : ");
		// テキスト
		txtMaxBytes = new Text(delimiter, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT - WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		txtMaxBytes.setLayoutData(gridData);
		txtMaxBytes.addVerifyListener(verifier);
		txtMaxBytes.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		tabFolder.setSelection(new TabItem[] { tabCheckRule });

		// dummy
		label = new Label(delimiter, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

	}

	/**
	 * Azure用のコンポジットを作成するメソッド
	 */
	private void createAzureComposite() {
		/*
		 * スコープ
		 */
		// ラベル
		GridData gridData;

		Label m_labelScope = new Label(azure_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_labelScope.setLayoutData(gridData);
		m_labelScope.setText(Messages.getString("xcloud.agent") + " : ");
		// テキスト
		this.m_textScope_azure = new Text(azure_composite, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textScope_azure.setLayoutData(gridData);
		this.m_textScope_azure.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 参照ボタン
		Button m_buttonScope = new Button(azure_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_buttonScope.setLayoutData(gridData);
		m_buttonScope.setText(Messages.getString("refer"));
		m_buttonScope.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				String managerName = m_monitorBasic.getManagerListComposite().getText();
				// エンドポイントがPublishされていることを確認
				ValidateResult result = validateEndpoint(managerName);
				if (result != null) {
					displayError(result);
					return;
				}
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell, managerName, m_monitorBasic.getOwnerRoleId(), false,
						m_unregistered);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse item = dialog.getSelectItem();
					FacilityInfoResponse info = item.getData();
					m_agentFacilityId = info.getFacilityId();
					if (info.getFacilityType() == FacilityTypeEnum.NODE) {
						m_textScope_azure.setText(info.getFacilityName());
					} else {
						FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
						m_textScope_azure.setText(path.getPath(item));
					}
				}
			}
		});

		// 空白
		Label label = new Label(azure_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// リソースグループ名（Azureのみ）
		// ラベル
		m_resourceLabel = new Label(azure_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_resourceLabel.setLayoutData(gridData);
		m_resourceLabel.setText(Messages.getString("xcloud.resourceGroup") + " : ");
		// コンボボックス
		m_resourceGroup = new Combo(azure_composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT - WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_resourceGroup.setLayoutData(gridData);
		m_resourceGroup.add("");
		m_resourceGroup.setText("");
		m_resourceGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ワークスペース
		// ラベル
		m_workspace = new Label(azure_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_workspace.setLayoutData(gridData);
		m_workspace.setText(Messages.getString("xcloud.workspace") + " : ");

		// テキスト
		this.m_workspaceText = new Text(azure_composite, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_workspaceText.setLayoutData(gridData);
		this.m_workspaceText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// テーブル
		// ラベル
		m_tablelabel = new Label(azure_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_tablelabel.setLayoutData(gridData);
		m_tablelabel.setText(Messages.getString("xcloud.table") + ": ");

		// テキスト
		this.m_tableText = new Text(azure_composite, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_tableText.setLayoutData(gridData);
		this.m_tableText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// カラム（Azureのみ）
		// ラベル
		azt_colLabel = new Label(azure_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		azt_colLabel.setLayoutData(gridData);
		azt_colLabel.setText(Messages.getString("xcloud.col") + ": ");

		// テキスト
		this.azt_colSText = new Text(azure_composite, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.azt_colSText.setLayoutData(gridData);
		this.azt_colSText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		// 空白
		label = new Label(azure_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		isAzureCreated = true;
	}

	/**
	 * スコープツリーからクラウドスコープのみを抽出して スコープツリーダイアログを表示するメソッド
	 */
	private void getCloudScopeSelection() {
		for (Listener l : m_monitorBasic.getButtonScope().getListeners(SWT.Selection)) {
			m_monitorBasic.getButtonScope()
					.removeSelectionListener((SelectionListener) ((TypedListener) l).getEventListener());
		}

		m_monitorBasic.getButtonScope().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialogForServiceMonitors dialog = null;
				int dialogPressType = -1;
				try {
					CloudLogMonitorCreateDialog.this.managerName = m_monitorBasic.getManagerListComposite().getText();
					// エンドポイントがPublishされていることを確認
					ValidateResult result = validateEndpoint(CloudLogMonitorCreateDialog.this.managerName);
					if (result != null) {
						displayError(result);
						return;
					}
					dialog = new ScopeTreeDialogForServiceMonitors(getShell(), managerName,
							m_monitorBasic.getOwnerRoleId());
					
					dialogPressType = dialog.open();
				} catch (InvalidStateException e1) {
					// クラウド仮想化の参照権限がない場合等に到達
					// ダイアログを出力して処理を終了
					MessageDialog.openInformation(null, Messages.getString("message"), e1.getMessage());
					return;
				} catch (Exception e2){
					// 想定外例外はログに出力するのみ(ここには到達しない想定)
					logger.warn("getCloudScopeSelection(): ", e2);
					return;
				}

				if (dialogPressType == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse selectItem = dialog.getSelectItem();
					FacilityInfoResponse info = selectItem.getData();
					Field m_facilityId = null;
					boolean m_facilityIdAccesible = false;
					Field m_textScope = null;
					boolean m_textScopeAccesible = false;
					
					try {
						m_facilityId = m_monitorBasic.getClass().getDeclaredField("m_facilityId");
						m_facilityIdAccesible = m_facilityId.isAccessible();
						m_facilityId.setAccessible(true);

						m_textScope = m_monitorBasic.getClass().getDeclaredField("m_textScope");
						m_textScopeAccesible = m_textScope.isAccessible();
						m_textScope.setAccessible(true);

						m_facilityId.set(m_monitorBasic, info.getFacilityId());

						FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
						((Text) m_textScope.get(m_monitorBasic)).setText(path.getPath(selectItem));
					} catch (Exception e2) {
						logger.error(e2);
					} finally {
						if (m_facilityId != null)
							m_facilityId.setAccessible(m_facilityIdAccesible);

						if (m_textScope != null)
							m_textScope.setAccessible(m_textScopeAccesible);
					}

					update();
				}
			}
		});
	}

	@Override
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		return CloudRestClientWrapper.getWrapper(managerName);
	}
	

}
