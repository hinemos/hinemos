/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.dialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.CorrectExecNodeDetailRequest;
import org.openapitools.client.model.CorrectExecNodeRequest;
import org.openapitools.client.model.GetRpaScenarioCorrectExecNodeResponse;
import org.openapitools.client.model.NotifyRelationInfoRequest;
import org.openapitools.client.model.NotifyRelationInfoResponse;
import org.openapitools.client.model.RpaScenarioExecNodeResponse;
import org.openapitools.client.model.UpdateRpaScenarioOperationResultRequest;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.DateTimeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.composite.NotifyInfoComposite;
import com.clustercontrol.rpa.action.CorrectRpaExecNode;
import com.clustercontrol.rpa.action.GetRpaScenario;
import com.clustercontrol.rpa.composite.RpaScenarioExecNodeListComposite;
import com.clustercontrol.rpa.util.RpaScenarioCorrectExecNodePropertyUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * RPAシナリオ[実行ノード訂正]ダイアログクラス
 */
public class RpaScenarioCorrectExecNodeDialog extends CommonDialog{

	// ログ
	private static Log log = LogFactory.getLog( RpaScenarioCorrectExecNodeDialog.class );
	
	/** マネージャ名 */
	private String managerName = null;
	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite managerComposite = null;
	/** シナリオ実績作成設定ID */
	private Text scenarioOperationResultCreateSettingText = null;
	/** シナリオ識別子 */
	private Text scenarioIdentifyStringText = null;
	/** 実行ノード用コンポジット */
	private RpaScenarioExecNodeListComposite scenarioExecNodeListComposite = null;
	
	/** 対象期間 */
	private Text periodTextFrom = null;
	private Button periodButtonFrom = null;
	private Text periodTextTo = null;
	private Button periodButtonTo = null;

	/** 通知情報 */
	private NotifyInfoComposite notifyIdList = null;
	
	
	/** プロパティシートから入力値を取得する為のオブジェクト */
	private List<RpaScenarioExecNodeResponse> execNodeList = null;
	
	/** シナリオID */
	private String scenarioId = "";
	
	/** オーナーロールID */
	private String ownerRoleId = "";

	// ----- 共通メンバ変数 ----- //
	private Shell shell = null;

	/** 入力値の正当性を保持するオブジェクト */
	protected ValidateResult validateResult = null;
	
	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public RpaScenarioCorrectExecNodeDialog(Shell parent, String managerName, String scenarioId, String ownerRoleId) {
		super(parent);
		this.managerName = managerName;
		this.scenarioId = scenarioId;
		this.ownerRoleId = ownerRoleId;
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
		shell.setText(Messages.getString("dialog.rpa.scenario.execnode.modify"));
		GridData gridData = new GridData();
		
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		composite.setLayout(gridLayout);
		
		/*
		 * マネージャ
		 */
		//ラベル
		Label labelManager = new Label(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "manager", labelManager);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.heightHint = SizeConstant.SIZE_LABEL_HEIGHT;
		labelManager.setLayoutData(gridData);
		labelManager.setText(Messages.getString("facility.manager") + " : ");
		
		//コンボ
		this.managerComposite = new ManagerListComposite(composite, SWT.NONE, false);
		WidgetTestUtil.setTestId(this, "managerComposite", this.managerComposite);
		gridData = new GridData();
		gridData.widthHint = 207;
		this.managerComposite.setLayoutData(gridData);

		if(this.managerName != null) {
			this.managerComposite.setText(this.managerName);
		}

		/*
		 * シナリオ実績作成設定ID
		 */
		//ラベル
		Label labelScenarioOperationResultCreateSettingId = new Label(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "scenarioOperationResultCreateSettingId", labelScenarioOperationResultCreateSettingId);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.heightHint = SizeConstant.SIZE_LABEL_HEIGHT;
		labelScenarioOperationResultCreateSettingId.setLayoutData(gridData);
		labelScenarioOperationResultCreateSettingId.setText(Messages.getString("rpa.scenario.operation.result.create.setting.id") + " : ");
		
		//テキスト
		scenarioOperationResultCreateSettingText = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "scenarioOperationResultCreateSettingText", scenarioOperationResultCreateSettingText);
		gridData = new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT);
		scenarioOperationResultCreateSettingText.setLayoutData(gridData);
		scenarioOperationResultCreateSettingText.setEnabled(false);


		/*
		 * シナリオ識別子
		 */
		//ラベル
		Label labelScenarioIdentifyString = new Label(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "description", labelScenarioIdentifyString);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.heightHint = SizeConstant.SIZE_LABEL_HEIGHT;
		labelScenarioIdentifyString.setLayoutData(gridData);
		labelScenarioIdentifyString.setText(Messages.getString("rpa.scenario.identify.string") + " : ");
		
		//テキスト
		scenarioIdentifyStringText = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "scenarioIdentifyString", scenarioIdentifyStringText);
		gridData = new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT);
		scenarioIdentifyStringText.setLayoutData(gridData);
		scenarioIdentifyStringText.setEnabled(false);
		
		//実行ノード
		this.scenarioExecNodeListComposite = new RpaScenarioExecNodeListComposite(composite, SWT.NONE);
		this.scenarioExecNodeListComposite.setManagerName(managerName);
		this.scenarioExecNodeListComposite.setScenarioId(scenarioId);
		gridData = new GridData();
		gridData.verticalIndent = 8;
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.scenarioExecNodeListComposite.setLayoutData(gridData);
		
		/*
		 * シナリオ実績更新情報の属性グループ
		 */
		// グループ
		Group groupDateAttribute = new Group(composite, SWT.NONE);
		GridLayout dateGroupLayout = new GridLayout(1, true);
		dateGroupLayout.marginWidth = 5;
		dateGroupLayout.marginHeight = 5;
		dateGroupLayout.numColumns = 1;
		groupDateAttribute.setLayout(dateGroupLayout);
		groupDateAttribute.setText(Messages.getString("view.rpa.scenario.operation.result.modify"));
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupDateAttribute.setLayoutData(gridData);
		
		//対象期間
		Composite compositePeriod = new Composite(groupDateAttribute, SWT.NONE);
		GridLayout gl_groupPeriod = new GridLayout(12, false);
		gl_groupPeriod.verticalSpacing = 0;
		compositePeriod.setLayout(gl_groupPeriod);
		GridData gd_groupPeriod = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_groupPeriod.widthHint = 50;
		compositePeriod.setLayoutData(gd_groupPeriod);
		
		Label lblPeriod = new Label(compositePeriod, SWT.NONE);
		GridData gd_lblPeriod = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblPeriod.widthHint = 98;
		lblPeriod.setLayoutData(gd_lblPeriod);
		lblPeriod.setText(Messages.getString("view.rpa.scenario.operation.result.modify.period"));
		
		// テキスト
		periodTextFrom = new Text(compositePeriod, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "opeStartDateText", periodTextFrom);
		periodTextFrom.setLayoutData(new GridData(140, SizeConstant.SIZE_TEXT_HEIGHT));
		// 日時ダイアログからの入力しか受け付けません
		periodTextFrom.setEnabled(false);
		periodTextFrom.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// 追加ボタン
		periodButtonFrom = new Button(compositePeriod, SWT.NONE);
		WidgetTestUtil.setTestId(this, "opeStartDateButton", periodButtonFrom);
		periodButtonFrom.setLayoutData(new GridData(40, SizeConstant.SIZE_BUTTON_HEIGHT + 5));
		periodButtonFrom.setText(Messages.getString("calendar.button"));
		periodButtonFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DateTimeDialog dialog = new DateTimeDialog(shell);
				if (periodTextFrom.getText().length() > 0) {
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					try {
						Date date = sdf.parse(periodTextFrom.getText());
						dialog.setDate(date);
					} catch (ParseException e1) {
						log.warn("opeStartDateText : " + e1.getMessage());
						
					}
				}
				if (dialog.open() == IDialogConstants.OK_ID) {
					// ダイアログより取得した日時を"yyyy/MM/dd HH:mm:ss"の形式に変換
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					String tmp = sdf.format(dialog.getDate());
					periodTextFrom.setText(tmp);
					update();
				}
			}
		});

		Label lblFromTo = new Label(compositePeriod, SWT.NONE);
		lblFromTo.setAlignment(SWT.CENTER);
		GridData gd_lblFromTo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblFromTo.widthHint = 39;
		lblFromTo.setLayoutData(gd_lblFromTo);
		lblFromTo.setText(Messages.getString("wave"));

		// テキスト
		periodTextTo = new Text(compositePeriod, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "opeStartDateText", periodTextTo);
		periodTextTo.setLayoutData(new GridData(140, SizeConstant.SIZE_TEXT_HEIGHT));
		// 日時ダイアログからの入力しか受け付けません
		periodTextTo.setEnabled(false);
		periodTextTo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// 追加ボタン
		periodButtonTo = new Button(compositePeriod, SWT.NONE);
		WidgetTestUtil.setTestId(this, "opeStartDateButton", periodButtonTo);
		periodButtonTo.setLayoutData(new GridData(40, SizeConstant.SIZE_BUTTON_HEIGHT + 5));
		periodButtonTo.setText(Messages.getString("calendar.button"));
		periodButtonTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DateTimeDialog dialog = new DateTimeDialog(shell);
				if (periodTextTo.getText().length() > 0) {
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					try {
						Date date = sdf.parse(periodTextTo.getText());
						dialog.setDate(date);
					} catch (ParseException e1) {
						log.warn("opeStartDateText : " + e1.getMessage());
						
					}
				}
				if (dialog.open() == IDialogConstants.OK_ID) {
					// ダイアログより取得した日時を"yyyy/MM/dd HH:mm:ss"の形式に変換
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					String tmp = sdf.format(dialog.getDate());
					periodTextTo.setText(tmp);
					update();
				}
			}
		});
		
		/*
		 * 通知情報の属性グループ
		 */
		// グループ
		Group groupNotifyAttribute = new Group(groupDateAttribute, SWT.NONE);
		GridLayout notifyGroupLayout = new GridLayout(1, true);
		notifyGroupLayout.marginWidth = 5;
		notifyGroupLayout.marginHeight = 5;
		notifyGroupLayout.numColumns = 1;
		groupNotifyAttribute.setLayout(notifyGroupLayout);
		groupNotifyAttribute.setText(Messages.getString("notify.attribute"));
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupNotifyAttribute.setLayoutData(gridData);
		
		// 通知
		this.notifyIdList = new NotifyInfoComposite(groupNotifyAttribute, SWT.NONE);
		this.notifyIdList.setManagerName(this.managerComposite.getText());
		this.notifyIdList.setMinApplicationLen(0);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		notifyIdList.setLayoutData(gridData);
		
		// ダイアログを調整
		this.adjustDialog();
		// ダイアログに情報反映
		this.reflectScenario();
		// 必須入力項目を可視化
		this.update();
	}

	/**
	 * ダイアログエリアを調整します。
	 */
	private void adjustDialog(){
		// 画面中央に配置
		Display calDisplay = shell.getDisplay();
		shell.setLocation((calDisplay.getBounds().width - shell.getSize().x) / 2,
				(calDisplay.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * 更新処理
	 */
	public void update(){
		// 現状何もしない。
		// 必須項目が追加された時等はここでテキストボックスの色変更等すること。
	}
	
	/**
	 * ダイアログにシナリオ情報を反映します。
	 */
	private void reflectScenario() {
		// 初期表示
		GetRpaScenarioCorrectExecNodeResponse scenarioInfo = null;
		
		scenarioInfo = new GetRpaScenario().getRpaScenarioCorrectExecNode(this.managerName, this.scenarioId);
		
		//シナリオ情報取得
		if(scenarioInfo != null){
			this.execNodeList = scenarioInfo.getExecNodeList();
			
			if(scenarioInfo.getScenarioOperationResultCreateSettingId() != null){
				this.scenarioOperationResultCreateSettingText.setText(scenarioInfo.getScenarioOperationResultCreateSettingId());
				this.scenarioExecNodeListComposite.setScenarioOperationResultCreateSetting(scenarioInfo.getScenarioOperationResultCreateSettingId());
			}
			
			if(scenarioInfo.getScenarioIdentifyString() != null){
				this.scenarioIdentifyStringText.setText(scenarioInfo.getScenarioIdentifyString());
				this.scenarioExecNodeListComposite.setScenarioIdentifyString(scenarioInfo.getScenarioIdentifyString());
			}
			
			this.scenarioExecNodeListComposite.setRpaToolId(scenarioInfo.getRpaToolId());
			this.scenarioExecNodeListComposite.setExecNode(scenarioInfo.getExecNodeList(), scenarioInfo.getScenarioList());
			
			this.notifyIdList.setOwnerRoleId(this.ownerRoleId, true);
		}

		this.update();
	}
	
	
	/**
	 * ダイアログに「実行ノード訂正」、「シナリオ実績更新」、「閉じる」ボタンを作成する。
	 *
	 * @param parent
	 *            ボタンバーコンポジット
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// 実行ノード訂正ボタン
		this.createButton(parent, IDialogConstants.PROCEED_ID, Messages.getString("rpa.scenario.execution.node.correct"), false);
		this.getButton(IDialogConstants.PROCEED_ID).addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						correctExecutionNodeAction();
					}
				});

		// シナリオ実績更新ボタン
		this.createButton(parent, IDialogConstants.OPEN_ID, Messages.getString("view.rpa.scenario.operation.result.modify"), false);
		this.getButton(IDialogConstants.OPEN_ID).addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						updateOperationResultAction();
					}
				});

		// 閉じるボタン
		this.createButton( parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), false );
	}

	/**
	 * 実行ノード訂正
	 */
	private void correctExecutionNodeAction() {
		CorrectExecNodeRequest request = new CorrectExecNodeRequest();
		//実績作成設定ID
		request.setScenarioOperationResultCreateSettingId(scenarioOperationResultCreateSettingText.getText());
		// シナリオ識別子
		request.setScenarioIdentifyString(scenarioIdentifyStringText.getText());

		// 実行ノード
		Property property = this.scenarioExecNodeListComposite.getInputData();
		List<CorrectExecNodeDetailRequest> execNodes = RpaScenarioCorrectExecNodePropertyUtil.property2dto(property, this.execNodeList);
		request.setExecNodes(execNodes);
		
		new CorrectRpaExecNode().correctRpaExecNode(managerName, request);
		this.scenarioExecNodeListComposite.update();
	}
	
	/**
	 * シナリオ実績更新
	 */
	private void updateOperationResultAction() {
		// 対象期間(from,to)が空の場合、メッセージを出して終了
		if (!checkupdateOperationResultAction()) {
			return;
		}
		UpdateRpaScenarioOperationResultRequest request = new UpdateRpaScenarioOperationResultRequest();
		//実績作成設定ID
		request.setScenarioOperationResultCreateSettingId(scenarioOperationResultCreateSettingText.getText());
		// シナリオ識別子
		request.setScenarioIdentifyString(scenarioIdentifyStringText.getText());
		// 対象期間(from)
		request.setFromDate(this.periodTextFrom.getText());
		// 対象期間(To)
		request.setToDate(this.periodTextTo.getText());
		// 通知ID
		if (this.notifyIdList.getNotify() != null) {
			for (NotifyRelationInfoResponse notify: this.notifyIdList.getNotify()){
				NotifyRelationInfoRequest notifyReq = new NotifyRelationInfoRequest();
				notifyReq.setNotifyId(notify.getNotifyId());
				request.getNotifyId().add(notifyReq);
			}
		}
		// アプリケーション
		request.setApplication(this.notifyIdList.getApplication());

		new CorrectRpaExecNode().updateExecNode(managerName, request);
	}
	
	private boolean checkupdateOperationResultAction() {
		// 対象期間(from,to)が空の場合、メッセージを出して終了。
		if ("".equals(periodTextFrom.getText()) || "".equals(this.periodTextTo.getText())) {
			MessageDialog.openError(
				null,
				Messages.getString("failed"),
				Messages.getString("MESSAGE_PLEASE_INPUT", new String[] {Messages.getString("TARGET_PERIOD")})); 
		return false;
		}
		return true;
	}
}
