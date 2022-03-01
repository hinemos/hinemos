/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.monitor.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.AddRpaLogfileMonitorRequest;
import org.openapitools.client.model.ModifyRpaLogfileMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorStringValueInfoRequest;
import org.openapitools.client.model.NotifyRelationInfoResponse;
import org.openapitools.client.model.RpaLogFileCheckInfoResponse;
import org.openapitools.client.model.RpaToolEnvResponse;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.logfile.dialog.LogfileStringCreateDialog;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.monitor.run.dialog.CommonMonitorStringDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.notify.bean.PriChangeJudgeSelectTypeConstant;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;

/**
 * RPAログファイル監視作成・変更ダイアログクラス<BR>
 * @see LogfileStringCreateDialog
 *
 */
public class RpaLogfileStringCreateDialog extends CommonMonitorStringDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( RpaLogfileStringCreateDialog.class );
	
	/** RPAツール(コンボボックス) */
	private Combo m_comboRpaTool = null;

	/** ディレクトリ */
	private Text m_directory = null;

	/** ファイル名 */
	private Text m_fileName = null;

	/** エンコード */
	private Text m_fileEncoding = null;

	/** マネージャ名 */
	private String managerName = null;
	
	/** RPAツール(ID, ツール名) */
	private List<RpaToolEnvResponse> rpaToolList = new ArrayList<>();

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public RpaLogfileStringCreateDialog(Shell parent) {
		super(parent, null);
		logLineFlag = true;
		this.priorityChangeJudgeSelect = PriChangeJudgeSelectTypeConstant.TYPE_PATTERN;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param monitorId 変更する監視項目ID
	 * @param updateFlg 更新するか否か（true:変更、false:新規登録）
	 */
	public RpaLogfileStringCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);

		logLineFlag = true;
		this.managerName = managerName;
		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
		this.priorityChangeJudgeSelect = PriChangeJudgeSelectTypeConstant.TYPE_PATTERN;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// タイトル
		shell.setText(Messages.getString("dialog.rpalogfile.create.modify"));


		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;
		
		/*
		 * ファイル情報グループ（条件グループの子グループ）
		 */
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "checkrule", groupCheckRule);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		layout.numColumns = BASIC_UNIT;
		groupCheckRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCheckRule.setLayoutData(gridData);
		groupCheckRule.setText(Messages.getString("file.info"));

		
		// RPAツール
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "runinterval", label);
		gridData = new GridData();
		gridData.horizontalSpan = CommonMonitorDialog.WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("rpa.tool") + " : ");
		// コンボボックス
		this.m_comboRpaTool = new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, m_comboRpaTool);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboRpaTool.setLayoutData(gridData);
		createComboRpaTool();

		//ディレクトリ
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "directory", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("directory") + " : ");
		// テキスト
		this.m_directory = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "directory", m_directory);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		String tooltipText = Messages.getString("monitor.logfile.directory.tool.tip") + Messages.getString("replace.parameter.node");
		this.m_directory.setToolTipText(tooltipText);
		this.m_directory.setLayoutData(gridData);
		this.m_directory.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//ファイル名
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "filename", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("file.name") + "(" + Messages.getString("regex") + ") : ");
		// テキスト
		this.m_fileName = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "filename", m_fileName);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_fileName.setLayoutData(gridData);
		this.m_fileName.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		this.m_fileName.setToolTipText(Messages.getString("dialog.logfile.pattern"));

		//ファイルエンコーディング
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "fileencoding", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("file.encoding") + " : ");
		// テキスト
		this.m_fileEncoding = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "fileencoding", m_fileEncoding);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_fileEncoding.setLayoutData(gridData);
		this.m_fileEncoding.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		// 監視間隔の設定を利用不可とする
		this.m_monitorRule.setRunIntervalEnabled(false);

		// ダイアログを調整
		this.adjustDialog();

		// 初期表示
		MonitorInfoResponse info = null;

		
		if(this.monitorId == null){
			// 作成の場合
			info = new MonitorInfoResponse();
			this.setInfoInitialValue(info);
			this.setInputData(info);
		} else {
			// 変更の場合、情報取得
			try {
				MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(managerName);
				info = wrapper.getMonitor(monitorId);
				this.setInputData(info);
			} catch (InvalidRole e) {
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
	public void update(){
		super.update();

		// 必須項目を明示
		Text[] texts = {m_directory, m_fileName, m_fileEncoding};
		for (Text text : texts) {
			if("".equals(text.getText())){
				text.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			}else{
				text.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}
	}

	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor 設定値として用いる監視情報
	 */
	@Override
	protected void setInputData(MonitorInfoResponse monitor) {

		super.setInputData(monitor);
		this.inputData = monitor;

		List<NotifyRelationInfoResponse> c = monitor.getNotifyRelationList();
		if (m_log.isDebugEnabled()) {
			if (c != null ) {
				for (NotifyRelationInfoResponse i : c) {
					m_log.debug("notifyId : " + i.getNotifyId());
				}
			}
		}

		// 監視条件 RPAログファイル監視情報
		RpaLogFileCheckInfoResponse rpaLogfileInfo = monitor.getRpaLogFileCheckInfo();
		if (rpaLogfileInfo == null) {
			rpaLogfileInfo = new RpaLogFileCheckInfoResponse();
		}
		if (rpaLogfileInfo.getRpaToolEnvId() != null){
			this.m_comboRpaTool.setText(getRpaToolName(rpaLogfileInfo.getRpaToolEnvId()));
		}
		if (rpaLogfileInfo.getDirectory() != null){
			this.m_directory.setText(rpaLogfileInfo.getDirectory());
		}
		if (rpaLogfileInfo.getFileName() != null){
			this.m_fileName.setText(rpaLogfileInfo.getFileName());
		}
		if (rpaLogfileInfo.getFileEncoding() != null){
			this.m_fileEncoding.setText(rpaLogfileInfo.getFileEncoding());
		} else {
			this.m_fileEncoding.setText("UTF-8");
		}

		// 各種設定が必須項目であることを明示
		this.update();

		m_stringValueInfo.setInputData(monitor);
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

		// 監視条件 RPAログファイル監視情報
		RpaLogFileCheckInfoResponse rpaLogfileInfo = new RpaLogFileCheckInfoResponse();

		//テキストボックスから文字列を取得
		rpaLogfileInfo.setRpaToolEnvId(rpaToolList.get(this.m_comboRpaTool.getSelectionIndex()).getRpaToolEnvId());
		if (this.m_directory.getText() != null
				&& !"".equals(this.m_directory.getText())) {
			rpaLogfileInfo.setDirectory(this.m_directory.getText());
		}
		if (this.m_fileName.getText() != null
				&& !"".equals(this.m_fileName.getText())) {
			rpaLogfileInfo.setFileName(this.m_fileName.getText());
		}
		if (this.m_fileEncoding.getText() != null
				&& !"".equals(this.m_fileEncoding.getText())) {
			rpaLogfileInfo.setFileEncoding(this.m_fileEncoding.getText());
		}
		
		monitorInfo.setRpaLogFileCheckInfo(rpaLogfileInfo);

		// 結果判定の定義
		validateResult = m_stringValueInfo.createInputData(monitorInfo);
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

		if(this.inputData != null){
			String[] args = { this.inputData.getMonitorId(), getManagerName() };
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(getManagerName());
			if(!this.updateFlg){
				// 作成の場合
				try {
					AddRpaLogfileMonitorRequest info = new AddRpaLogfileMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddRpaLogfileMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getStringValueInfo() != null
							&& this.inputData.getStringValueInfo() != null) {
						for (int i = 0; i < info.getStringValueInfo().size(); i++) {
							info.getStringValueInfo().get(i).setPriority(MonitorStringValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getStringValueInfo().get(i).getPriority().getValue()));
						}
					}
					wrapper.addRpaLogFileMonitor(info);
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.33", args));
					result = true;
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
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}

					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.34", args) + errMessage);
				}
			} else {
				// 変更の場合
				try {
					ModifyRpaLogfileMonitorRequest info = new ModifyRpaLogfileMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifyRpaLogfileMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getStringValueInfo() != null
							&& this.inputData.getStringValueInfo() != null) {
						for (int i = 0; i < info.getStringValueInfo().size(); i++) {
							info.getStringValueInfo().get(i).setPriority(MonitorStringValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getStringValueInfo().get(i).getPriority().getValue()));
						}
					}
					wrapper.modifyRpaLogfileMonitorInfo(this.inputData.getMonitorId(), info);
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
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
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

	private String getRpaToolName(String rpaToolId) {
		return rpaToolList.stream()
				.filter(tool -> tool.getRpaToolEnvId().equals(rpaToolId))
				.findFirst()
				.map(RpaToolEnvResponse::getRpaToolEnvName)
				.orElse(null);
	}
	
	/**
	 * RPAツールコンボボックスを設定
	 */
	private void createComboRpaTool() {
		List<RpaToolEnvResponse> rpaToolList = null; 
		try {
			if (this.rpaToolList != null) {
				this.rpaToolList.clear();
			}
			String managerName = this.getManagerName();
			RpaRestClientWrapper rpaWrapper = RpaRestClientWrapper.getWrapper(managerName);
			rpaToolList = rpaWrapper.getRpaToolEnv();
		} catch (HinemosException e) {
			m_log.warn(e.getMessage(), e);
		}

		if(rpaToolList != null){
			this.m_comboRpaTool.removeAll();

			for(RpaToolEnvResponse rpaTool : rpaToolList){
				this.rpaToolList.add(rpaTool);
				this.m_comboRpaTool.add(rpaTool.getRpaToolEnvName());
			}
			
			this.m_comboRpaTool.select(0);
		}
	}

	@Override
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		// RpaRestEndpointsにはcheckPublishが存在しない
		// どのEndpointでも内容は同じなのでUtilityを使用する
		return UtilityRestClientWrapper.getWrapper(managerName);
	}

}
