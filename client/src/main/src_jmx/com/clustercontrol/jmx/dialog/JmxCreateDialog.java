/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jmx.dialog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.bean.ConvertValueMessage;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.dialog.CommonMonitorNumericDialog;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jmxmaster.JmxMasterEndpoint;
import com.clustercontrol.ws.jmxmaster.JmxMasterEndpointService;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.JmxCheckInfo;
import com.clustercontrol.ws.monitor.JmxMasterInfo;
import com.clustercontrol.ws.monitor.MonitorDuplicate_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * JMX監視作成・変更ダイアログクラス
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class JmxCreateDialog extends CommonMonitorNumericDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( JmxCreateDialog.class );

	// ----- instance フィールド ----- //

	/** 収集項目 */
	private Combo m_comboCollectorItem = null;

	/** JMXマスタリスト */
	private List<JmxMasterInfo> m_master = null;

	/** ポート */
	private Text m_textPort = null;

	/** ユーザ */
	private Text m_textUser = null;

	/** パスワード */
	private Text m_textPassword = null;

	/** 取得値の加工 */
	private Combo m_comboConvertValue = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public JmxCreateDialog(Shell parent) {
		super(parent, null);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 * @param notifyId
	 *            変更する通知ID
	 * @param updateFlg
	 *            更新するか否か（true:変更、false:新規登録）
	 */
	public JmxCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);
		this.managerName = managerName;
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
		// 項目名に「取得値」を設定
		item1 = Messages.getString("select.value");
		item2 = Messages.getString("select.value");

		super.customizeDialog(parent);
		itemName.setEditable(false);
		measure.setEditable(false);

		// タイトル
		shell.setText(Messages.getString("dialog.monitor.jmx.create.modify"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		/*
		 * チェック設定グループ（条件グループの子グループ）
		 */
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "checkrule", groupCheckRule);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = BASIC_UNIT;
		groupCheckRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCheckRule.setLayoutData(gridData);
		groupCheckRule.setText(Messages.getString("check.rule"));

		/*
		 * 監視項目
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "monitoritem", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.item") + " : ");
		// コンボボックス
		this.m_comboCollectorItem =	new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "collectoritem", m_comboCollectorItem);
		gridData = new GridData();
		gridData.horizontalSpan = 22;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboCollectorItem.setLayoutData(gridData);

		createComboCollectorItem();

		// 収集値表示名と収集値単位を設定
		m_comboCollectorItem.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0){
				if (m_comboCollectorItem.getSelectionIndex() != -1) {
					itemName.setText(HinemosMessage.replace(((JmxMasterInfo)m_comboCollectorItem.getData(m_comboCollectorItem.getText())).getName()));
					measure.setText(HinemosMessage.replace(((JmxMasterInfo)m_comboCollectorItem.getData(m_comboCollectorItem.getText())).getMeasure()));
					update();
				}
			}
		});

		//マネージャを変更した場合
		if(!updateFlg) {
			this.getMonitorBasicScope().getManagerListComposite()
			.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					itemName.setText(Messages.getString("select.value"));
					measure.setText(Messages.getString("collection.unit"));
					createComboCollectorItem();
				}
			});
		}

		/*
		 * 監視項目
		 */
		// プロキシ：ポート
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "port", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("port") + " : ");

		// テキスト
		this.m_textPort = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, null, m_textPort);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textPort.setLayoutData(gridData);
		this.m_textPort.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 16;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// プロキシ：ユーザ名
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "user", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("user") + " : ");

		// テキスト
		this.m_textUser = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, null, m_textUser);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textUser.setLayoutData(gridData);
		this.m_textUser.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// プロキシ：パスワード

		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "password", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("password") + " : ");

		// テキスト
		this.m_textPassword = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.PASSWORD);
		WidgetTestUtil.setTestId(this, null, m_textPassword);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textPassword.setLayoutData(gridData);
		this.m_textPassword.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank3", label);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 値取得の加工
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("convert.value") + " : ");
		// コンボボックス
		this.m_comboConvertValue = new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "convertvalue", m_comboConvertValue);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboConvertValue.setLayoutData(gridData);
		this.m_comboConvertValue.add(ConvertValueMessage.STRING_NO);
		this.m_comboConvertValue.add(ConvertValueMessage.STRING_DELTA);

		// 空白
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank4", label);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		// ダイアログを調整
		this.adjustDialog();

		// 初期表示
		MonitorInfo info = null;
		if(this.monitorId == null){
			// 作成の場合
			info = new MonitorInfo();
			this.setInfoInitialValue(info);
			this.setInputData(info);
		} else {
			// 変更の場合、情報取得
			try {
				MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
				info = wrapper.getMonitor(this.monitorId);
				this.setInputData(info);
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));

			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("customizeDialog() getMonitor, " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	protected void update() {
		super.update();

		if("".equals(this.m_textPort.getText().trim())){
			this.m_textPort.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textPort.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		try{
			// 項目名の「取得値（単位）」のうち、単位部分を更新
			item1 = Messages.getString("select.value") 
					+ "(" + HinemosMessage.replace(((JmxMasterInfo)m_comboCollectorItem.getData(m_comboCollectorItem.getText())).getMeasure()) 
					+ ")";
			item2 = item1;

			this.m_numericValueInfo.setTextItem1(item1);
			this.m_numericValueInfo.setTextItem2(item2);

		}catch(NullPointerException e){
			// スコープが選択されていない場合
		}

	}
	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor
	 *            設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(MonitorInfo monitor) {
		super.setInputData(monitor);

		this.inputData = monitor;

		JmxCheckInfo info = monitor.getJmxCheckInfo();
		if(info == null){
			info = new JmxCheckInfo();
		}

		if(info.getMasterId() != null){
			for(JmxMasterInfo master :this.m_master){
				if(master.getId().equals(info.getMasterId())){
					this.m_comboCollectorItem.select(this.m_comboCollectorItem.indexOf(HinemosMessage.replace(master.getName())));
				}
			}
		}

		if(info.getPort() != null){
			this.m_textPort.setText(String.valueOf(info.getPort()));
		}

		if(info.getAuthUser() != null){
			this.m_textUser.setText(info.getAuthUser());
		}

		if(info.getAuthPassword() != null){
			this.m_textPassword.setText(info.getAuthPassword());
		}

		if (info.getConvertFlg() != null) {
			this.m_comboConvertValue.setText(ConvertValueMessage.typeToString(info.getConvertFlg()));
		} else {
			this.m_comboConvertValue.setText(ConvertValueMessage.typeToString(ConvertValueConstant.TYPE_NO));
		}
		m_numericValueInfo.setInputData(monitor);

		this.update();
	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfo createInputData() {
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		// JMX監視固有情報を設定
		monitorInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_JMX);
		monitorInfo.setMonitorType(MonitorTypeConstant.TYPE_NUMERIC);

		// JMX監視情報を生成
		JmxCheckInfo jmxCheckInfo = new JmxCheckInfo();
		jmxCheckInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_JMX);
		jmxCheckInfo.setMonitorId(monitorInfo.getMonitorId());
		jmxCheckInfo.setMasterId(((JmxMasterInfo)m_comboCollectorItem.getData(m_comboCollectorItem.getText())).getId());
		jmxCheckInfo.setPort(Integer.valueOf(this.m_textPort.getText()));
		jmxCheckInfo.setConvertFlg(ConvertValueConstant.TYPE_NO);
		if(!"".equals(this.m_textUser.getText().trim())){
			jmxCheckInfo.setAuthUser(this.m_textUser.getText());
		}
		if(!"".equals(this.m_textPassword.getText().trim())){
			jmxCheckInfo.setAuthPassword(this.m_textPassword.getText());
		}
		if (!"".equals(this.m_comboConvertValue.getText().trim())) {
			jmxCheckInfo.setConvertFlg(Integer.valueOf(ConvertValueMessage.stringToType(this.m_comboConvertValue.getText())));
		}

		monitorInfo.setJmxCheckInfo(jmxCheckInfo);

		// 結果判定の定義
		validateResult = m_numericValueInfo.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}

		// 通知関連情報とアプリケーションの設定
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

		MonitorInfo info = this.inputData;
		JmxMasterInfo selectJmxMasterInfo = (JmxMasterInfo)m_comboCollectorItem.getData(m_comboCollectorItem.getText());
		info.setItemName(selectJmxMasterInfo.getName());
		info.setMeasure(selectJmxMasterInfo.getMeasure());
		String managerName = this.getManagerName();
		MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
		String[] args = { info.getMonitorId(), managerName };
		if(!this.updateFlg){
			// 作成の場合
			try {
				result = wrapper.addMonitor(info);

				if(result){
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.33", args));
				} else {
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.34", args));
				}
			} catch (MonitorDuplicate_Exception e) {
				// 監視項目IDが重複している場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.monitor.53", args));

			} catch (Exception e) {
				String errMessage = "";
				if (e instanceof InvalidRole_Exception) {
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
						Messages.getString("message.monitor.34", args) + errMessage);
			}
		} else {
			// 変更の場合
			String errMessage = "";
			try {
				result = wrapper.modifyMonitor(info);
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}

			if(result){
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						Messages.getString("message.monitor.35", args));
			} else {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.monitor.36", args) + errMessage);
			}
		}
		return result;
	}


	/**
	 * ダイアログの入力値チェックを行います。
	 * <p>
	 *
	 * @return ValidateResultオブジェクト
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;


		if ("".equals((this.m_comboCollectorItem.getText()).trim())) {
			this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("monitor.item")}));
			return this.validateResult;
		}
		if ("".equals((this.m_textPort.getText()).trim())) {
			this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required.numeric", new Object[]{Messages.getString("port")}));
			return this.validateResult;
		} else {
			try{
				Integer.valueOf(this.m_textPort.getText().trim());
			}
			catch(NumberFormatException e){
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required.numeric", new Object[]{Messages.getString("port")}));
				return this.validateResult;
			}
		}

		result = super.validate();
		return result;
	}

	private List<JmxMasterInfo> getJmxMasterInfoList(String managerName) throws com.clustercontrol.ws.jmxmaster.HinemosUnknown_Exception, com.clustercontrol.ws.jmxmaster.InvalidRole_Exception, com.clustercontrol.ws.jmxmaster.InvalidUserPass_Exception {
		WebServiceException wse = null;
		EndpointUnit endpointUnit = EndpointManager.get(managerName);
		for (EndpointSetting<JmxMasterEndpoint> endpointSetting : endpointUnit.getEndpoint(JmxMasterEndpointService.class, JmxMasterEndpoint.class)) {
			try {
				JmxMasterEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJmxMasterInfoList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJmxMasterInfoList(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	private void createComboCollectorItem() {
		try {
			if (this.m_master != null) {
				this.m_master.clear();
			}
			this.m_master = getJmxMasterInfoList(this.getManagerName());
		} catch (com.clustercontrol.ws.jmxmaster.HinemosUnknown_Exception
				| com.clustercontrol.ws.jmxmaster.InvalidRole_Exception
				| com.clustercontrol.ws.jmxmaster.InvalidUserPass_Exception e1) {
			m_log.warn(e1.getMessage(), e1);
		}
		if(this.m_master != null){
			this.m_comboCollectorItem.removeAll();
			Collections.sort(this.m_master, new Comparator<JmxMasterInfo>(){

				@Override
				public int compare(JmxMasterInfo o1, JmxMasterInfo o2) {
					String name1 = o1.getName();
					String name2 = o2.getName();
					
					if (!HinemosMessage.replace(name1).startsWith("[Hinemos]") && !HinemosMessage.replace(name2).startsWith("[Hinemos]")) {
						return 0;
					} else {
						if (HinemosMessage.replace(name1).compareTo(HinemosMessage.replace(name2)) < 0) {
							return 1;
						} else if (HinemosMessage.replace(name1).compareTo(HinemosMessage.replace(name2)) == 0) {
							return 0;
						} else {
							return -1;
						}
					}
				}
			});
			for(JmxMasterInfo info: this.m_master){
				this.m_comboCollectorItem.add(HinemosMessage.replace(info.getName()));
				this.m_comboCollectorItem.setData(HinemosMessage.replace(info.getName()), info);
			}
		}
	}
}
