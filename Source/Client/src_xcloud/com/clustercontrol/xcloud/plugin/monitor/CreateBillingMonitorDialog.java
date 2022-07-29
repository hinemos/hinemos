/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.monitor;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.AddCloudserviceBillingMonitorRequest;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;
import org.openapitools.client.model.GetPlatformServicesResponse;
import org.openapitools.client.model.ModifyCloudserviceBillingMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoRequest;
import org.openapitools.client.model.MonitorPluginStringInfoResponse;
import org.openapitools.client.model.PlatformServicesResponse;
import org.openapitools.client.model.PluginCheckInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.run.composite.MonitorBasicScopeComposite;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.monitor.run.dialog.CommonMonitorNumericDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.MessageManager;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.InvalidStateException;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;
import com.clustercontrol.xcloud.util.ControlUtil;

/**
 * クラウド課金監視作成・変更ダイアログクラス<BR>
 * 
 * @version 6.0.0
 * @since 2.0.0
 */
public class CreateBillingMonitorDialog extends CommonMonitorNumericDialog {
	MessageManager messages = CloudConstants.bundle_messages;
	
	String strFee = messages.getString("word.fee");
	String strTarget = messages.getString("word.target");
	String strSeparator = messages.getString("caption.title_separator");
	String msgSelectTarget = messages.getString("message.select_subject", new Object[]{"word.target"});
	
	String strBillingMonitorDialog = messages.getString("caption.create_billing_monitor_dialog");
	
	private static final String targetformat = "(%s) %s";
	private static final String key_platform = "platform";
	private static final String key_service = "service";
	
	MonitorBasicScopeComposite m_monitorBasic_;

	// ログ
	private static final Log logger = LogFactory.getLog(CreateBillingMonitorDialog.class);

	/** ターゲット */
	private Combo cmbTarget = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 * 
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public CreateBillingMonitorDialog(Shell parent, String managerName) {
		super(parent, managerName);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 * 
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param monitorId
	 *            監視ID
	 * @param updateFlg
	 *            更新するか否か（true:変更、false:新規登録）
	 * @wbp.parser.constructor
	 */
	public CreateBillingMonitorDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);

		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
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
		item1 = Messages.getString("select.value");
		item2 = Messages.getString("select.value");

		super.customizeDialog(parent);

		// タイトル
		shell.setText(strBillingMonitorDialog);

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		/*
		 * チェック設定グループ（条件グループの子グループ）
		 */
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupCheckRule.setLayout(layout);
		groupCheckRule.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 40, 1));
		groupCheckRule.setText(Messages.getString("check.rule"));

		/*
		 * ターゲット
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(strTarget + strSeparator);
		// コンボボックス
		this.cmbTarget = new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.cmbTarget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 10, 1));
		ControlUtil.setRequired(this.cmbTarget);

		// 収集値表示名のデフォルト値を設定
		this.itemName.setText(strFee);

		// 収集値単位のデフォルト値を設定
//		this.measure.setText(Messages.getString("time.msec"));

		// ダイアログを調整
		this.adjustDialog();

		// 初期表示
		MonitorInfoResponse info = null;
		if(this.monitorId == null){
			// 作成の場合
			info = new MonitorInfoResponse();
			this.setInfoInitialValue(info);
		} else {
			// 変更の場合、情報取得
			try {
				info = MonitorsettingRestClientWrapper.getWrapper(getManagerName()).getMonitor(this.monitorId);
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));

			} catch (Exception e) {
				// 上記以外の例外
				logger.warn("customizeDialog(), " + e.getMessage(), e);
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());

			}
		}
		
		{
			Field field = null;
			Boolean accesible = null;
			try {
				field = CommonMonitorDialog.class.getDeclaredField("m_monitorBasic");
				accesible = field.isAccessible();
				field.setAccessible(true);
				m_monitorBasic_ = (MonitorBasicScopeComposite)field.get(this);
			} catch(Exception e) {
				throw new IllegalStateException(e);
			} finally {
				if (accesible != null) {
					field.setAccessible(accesible);
				}
			}
		}
		
		RoleIdListComposite m_ownerRoleId;
		{
			Field field = null;
			Boolean accesible = null;
			try {
				field = com.clustercontrol.monitor.run.composite.MonitorBasicComposite.class.getDeclaredField("m_ownerRoleId");
				accesible = field.isAccessible();
				field.setAccessible(true);
				m_ownerRoleId = (RoleIdListComposite)field.get(m_monitorBasic_);
			} catch(Exception e) {
				throw new IllegalStateException(e);
			} finally {
				if (accesible != null) {
					field.setAccessible(accesible);
				}
			}
		}
		
		Combo comboRoleId;
		{
			Field field = null;
			Boolean accesible = null;
			try {
				field = com.clustercontrol.composite.RoleIdListComposite.class.getDeclaredField("comboRoleId");
				accesible = field.isAccessible();
				field.setAccessible(true);
				comboRoleId = (Combo)field.get(m_ownerRoleId);
				
				if (comboRoleId != null) {
					comboRoleId.addModifyListener(new ModifyListener() {
						@Override
						public void modifyText(ModifyEvent e) {
							cmbTarget.removeAll();
						}
					});
				}
			} catch(Exception e) {
				throw new IllegalStateException(e);
			} finally {
				if (accesible != null) {
					field.setAccessible(accesible);
				}
			}
		}
		
		final Text m_textScope;
		{
			Field field = null;
			Boolean accesible = null;
			try {
				field = MonitorBasicScopeComposite.class.getDeclaredField("m_textScope");
				accesible = field.isAccessible();
				field.setAccessible(true);
				m_textScope = (Text)field.get(m_monitorBasic_);
			} catch(Exception e) {
				throw new IllegalStateException(e);
			} finally {
				if (accesible != null) {
					field.setAccessible(accesible);
				}
			}
		}
		
		m_textScope.addModifyListener(new ModifyListener() {
			private String lastFacilityId = null;

			@Override
			public void modifyText(ModifyEvent arg0) {
				if(lastFacilityId == null){
					if(m_monitorBasic_.getFacilityId() == null){
						return;
					} else {
						setupCmbTarget(m_monitorBasic_.getFacilityId());
					}
				} else {
					if(m_monitorBasic_.getFacilityId() == null){
						cmbTarget.clearSelection();
						cmbTarget.removeAll();
					} else if(!lastFacilityId.equals(m_monitorBasic_.getFacilityId())){
						setupCmbTarget(m_monitorBasic_.getFacilityId());
					}
				}
				lastFacilityId = m_monitorBasic_.getFacilityId();
			}
		});

		Button m_buttonScope = null;
		{
			Field field = null;
			Boolean accesible = null;
			try {
				field = com.clustercontrol.monitor.run.composite.MonitorBasicScopeComposite.class.getDeclaredField("m_buttonScope");
				accesible = field.isAccessible();
				field.setAccessible(true);
				m_buttonScope = (Button)field.get(m_monitorBasic_);
			} catch(Exception e) {
				throw new IllegalStateException(e);
			} finally {
				if (accesible != null) {
					field.setAccessible(accesible);
				}
			}
		}
		
		for (Listener listener: m_buttonScope.getListeners(SWT.Selection)) {
			m_buttonScope.removeListener(SWT.Selection, listener);
			m_buttonScope.removeListener(SWT.DefaultSelection,listener);	
		}
		
		m_buttonScope.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				
				IHinemosManager manager = null;
				ScopeTreeDialog dialog = null;
				int dialogPressType = -1;
				try {
					// エンドポイントがPublishされていることを確認
					ValidateResult result = validateEndpoint(getManagerName());
					if (result != null) {
						displayError(result);
						return;
					}
					manager = ClusterControlPlugin.getDefault().getHinemosManager(getManagerName());
					manager.update();
					dialog = new ScopeTreeDialog(shell, manager, m_monitorBasic_.getOwnerRoleId());
					dialogPressType = dialog.open();
				} catch (CloudModelException | InvalidStateException e1) {
					// クラウド仮想化の参照権限がない場合等に到達
					// ダイアログを出力して処理を終了
					MessageDialog.openInformation(null, Messages.getString("message"), e1.getMessage());
					return;
				} catch (Exception e2) {
					// 想定外例外はログに出力するのみ(ここには到達しない想定)
					logger.warn("customizeDialog(): ", e2);
					return;
				}
				
				if (dialogPressType == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse item = dialog.getSelectItem();
					FacilityInfoResponse info = item.getData();
					
					{
						Field field = null;
						Boolean accesible = null;
						try {
							field = MonitorBasicScopeComposite.class.getDeclaredField("m_facilityId");
							accesible = field.isAccessible();
							field.setAccessible(true);
							field.set(m_monitorBasic_, info.getFacilityId());
						} catch(Exception ex) {
							throw new IllegalStateException(ex);
						} finally {
							if (accesible != null) {
								field.setAccessible(accesible);
							}
						}
					}
					
					if (info.getFacilityType() == FacilityTypeEnum.NODE) {
						m_textScope.setText(info.getFacilityName());
					} else {
						FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
						m_textScope.setText(path.getPath(item));
					}
				}
			}
		});
		
		if (info != null)
			this.setInputData(info);
	}


	/**
	 * 更新処理
	 * 
	 */
	@Override
	public void update(){
		super.update();

//		// 必須項目を明示
//		if(this.m_textTimeout.getEnabled() && "".equals(this.m_textTimeout.getText())){
//			this.m_textTimeout.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
//		}else{
//			this.m_textTimeout.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
//		}
	}


	/**
	 * 各項目に入力値を設定します。
	 * 
	 * @param monitor
	 *            設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(MonitorInfoResponse monitor) {
		super.setInputData(monitor);

		this.inputData = monitor;

		if(inputData.getFacilityId() != null && !inputData.getFacilityId().isEmpty())
			setupCmbTarget(inputData.getFacilityId());
		
		// 監視条件クラウド課金監視情報
		PluginCheckInfoResponse info = monitor.getPluginCheckInfo();
		if (info != null && info.getMonitorPluginStringInfoList() != null && !info.getMonitorPluginStringInfoList().isEmpty()) {
			String platform = null;
			String service = null;
			for (MonitorPluginStringInfoResponse stringInfo : info.getMonitorPluginStringInfoList()) {
				switch(stringInfo.getKey()){
					case key_platform:
						platform = stringInfo.getValue();
						break;
					case key_service:
						service = stringInfo.getValue();
						break;
					default:
						break;
				}
			}
			//予期せぬデータだった場合は、warnログを出力し、先頭のデータを指定する
			if (platform == null || service == null) {
				logger.warn("Unexpected internal failure occurred in Hinemos Manager.");
				this.cmbTarget.select(0);
			} else {
				int selectService = cmbTarget.indexOf(String.format(targetformat, platform,service));
				if(selectService != -1){
					this.cmbTarget.select(selectService);
				}
			}
		}
		
		m_numericValueInfo.setInputData(monitor);
	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 * 
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfoResponse createInputData() {
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		if(cmbTarget.getText() == null || cmbTarget.getText().isEmpty()){
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					msgSelectTarget);
			return null;
		}
		
		// 監視条件 クラウド課金監視情報
		PluginCheckInfoResponse pluginInfo = new PluginCheckInfoResponse();
		pluginInfo.setMonitorPluginStringInfoList(new ArrayList<>());

		PlatformServicesResponse services = (PlatformServicesResponse)cmbTarget.getData(cmbTarget.getText());
		
		MonitorPluginStringInfoResponse platformInfo = new MonitorPluginStringInfoResponse();
		platformInfo.setKey(key_platform);
		platformInfo.setValue(services.getPlatformId());
		pluginInfo.getMonitorPluginStringInfoList().add(platformInfo);
		
		MonitorPluginStringInfoResponse serviceInfo = new MonitorPluginStringInfoResponse();
		serviceInfo.setKey(key_service);
		serviceInfo.setValue(services.getServiceId());
		pluginInfo.getMonitorPluginStringInfoList().add(serviceInfo);
		
		monitorInfo.setPluginCheckInfo(pluginInfo);

		// 結果判定の定義
		validateResult = m_numericValueInfo.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}

		// 通知関連情報とアプリケーションの設定
		// 通知グループIDの設定
		validateResult = m_notifyInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			if(validateResult.getID() == null){	// 通知ID警告用出力
				if(!displayQuestion(validateResult)){
					validateResult = null;
					return null;
				}
			}
			else{	// アプリケーション未入力チェック
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
		
		if(this.inputData != null){
			String[] args = { this.inputData.getMonitorId(), getManagerName() };
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(getManagerName());
			if(!this.updateFlg){
				// 作成の場合
				try {
					AddCloudserviceBillingMonitorRequest info = new AddCloudserviceBillingMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddCloudserviceBillingMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(AddCloudserviceBillingMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.addCloudservicebillingMonitor(info);
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.33", args));
					result = true;
				} catch (MonitorIdInvalid e) {
					// 監視項目IDが不適切な場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.monitor.97", args));
				} catch (MonitorDuplicate e) {
					// 監視項目IDが重複している場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.monitor.53", args));
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						// アクセス権なしの場合、エラーダイアログを表示する
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + e.getMessage();
					}
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.34", args) + errMessage);
				}
			} else{
				// 変更の場合
				try {
					ModifyCloudserviceBillingMonitorRequest info = new ModifyCloudserviceBillingMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifyCloudserviceBillingMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getNumericValueInfo() != null
							&& this.inputData.getNumericValueInfo() != null) {
						for (int i = 0; i < info.getNumericValueInfo().size(); i++) {
							info.getNumericValueInfo().get(i).setPriority(MonitorNumericValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getNumericValueInfo().get(i).getPriority().getValue()));
						}
					}
					info.setPredictionMethod(ModifyCloudserviceBillingMonitorRequest.PredictionMethodEnum.fromValue(
							this.inputData.getPredictionMethod().getValue()));
					wrapper.modifyCloudservicebillingMonitor(this.inputData.getMonitorId(), info);
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
					result = true;
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						// アクセス権なしの場合、エラーダイアログを表示する
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + e.getMessage();
					}
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.36", args) + errMessage);
				}
			}
		}

		return result;
	}

	/**
	 * MonitorInfoに初期値を設定します
	 * 
	 * @see com.clustercontrol.dialog.CommonMonitorDialog#setInfoInitialValue()
	 */
	@Override
	protected void setInfoInitialValue(MonitorInfoResponse monitor) {
		super.setInfoInitialValue(monitor);
	}
	/**
	 * スコープを指定した場合、配下に属するノードが選択できるサービスをすべて追加する。
	 * @param facilityId
	 */
	private void setupCmbTarget(final String facilityId){
		cmbTarget.removeAll();
		final IHinemosManager manager = ClusterControlPlugin.getDefault().getHinemosManager(getManagerName());
		manager.update();

		try {
			GetPlatformServicesResponse billingServices = manager.getWrapper().getPlatformServices(facilityId,super.getMonitorBasicScope().getOwnerRoleId());
			if(billingServices != null){
				for(PlatformServicesResponse service: billingServices.getPlatiformServices()){
					CreateBillingMonitorDialog.this.cmbTarget.add(String.format(targetformat, service.getPlatformId(),service.getServiceId()));
					CreateBillingMonitorDialog.this.cmbTarget.setData(String.format(targetformat, service.getPlatformId(),service.getServiceId()), service);
				}
			}
		} catch (CloudManagerException | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown  e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		return CloudRestClientWrapper.getWrapper(managerName);
	}
	
}
