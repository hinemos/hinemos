/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.AddRpaScenarioRequest;
import org.openapitools.client.model.GetRpaScenarioResponse;
import org.openapitools.client.model.ModifyRpaScenarioRequest;
import org.openapitools.client.model.RpaScenarioTagResponse;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.rpa.action.AddRpaScenario;
import com.clustercontrol.rpa.action.GetRpaScenario;
import com.clustercontrol.rpa.action.ModifyRpaScenario;
import com.clustercontrol.rpa.composite.RpaScenarioExecNodeComposite;
import com.clustercontrol.rpa.composite.RpaScenarioInformationForAnalysisComposite;
import com.clustercontrol.rpa.composite.RpaScenarioOperationResultCreateSettingIdListComposite;
import com.clustercontrol.rpa.composite.RpaToolListComposite;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;

/**
 * RPAシナリオ作成・変更ダイアログクラス
 */
public class RpaScenarioDialog extends CommonDialog{

	// ログ
	private static Log log = LogFactory.getLog( RpaScenarioDialog.class );
	/** シナリオID */
	private String scenarioId = "";
	/** 変更用ダイアログ判別フラグ */
	private int mode;
	/** シナリオID */
	private Text scenarioIdText = null;
	/** RPAツール名 */
	private RpaToolListComposite rpaToolComposite = null;
	/** 作成ダイアログに表示するRPAツールIDの初期値 */
	private String initialRpaToolId = null;
	/** シナリオ名 */
	private Text scenarioNameText = null;
	/** シナリオ識別子 */
	private Text scenarioIdentifyStringText = null;
	/** 作成ダイアログに表示するシナリオ識別子の初期値 */
	private String initialIdentifyString = null;
	/** 入力値を保持するオブジェクト */
	private GetRpaScenarioResponse inputData = null;
	/** オーナーロールID */
	private Text roleIdText = null;
	/** マネージャ名 */
	private String managerName = null;
	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;
	/** 実績作成設定IDコンボボックス用コンポジット */
	private RpaScenarioOperationResultCreateSettingIdListComposite scenarioOperationResultCreateSettingListComposite = null;
	/** 作成ダイアログに表示する実績作成設定IDの初期値 */
	private String initialCreateSettingId = null;
	/** タブフォルダ */
	private TabFolder m_tabFolder = null;
	/** 実行ノードタブ用コンポジット */
	private RpaScenarioExecNodeComposite scenarioExecNodeComposite = null;
	/** 分析用情報タブ用コンポジット */
	private RpaScenarioInformationForAnalysisComposite scenarioInformationComposite = null;

	// ----- 共通メンバ変数 ----- //
	private Shell shell = null;
	private Text descriptionText = null;

	/** 入力値の正当性を保持するオブジェクト */
	protected ValidateResult validateResult = null;
	
	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public RpaScenarioDialog(Shell parent, String managerName, String id, int mode) {
		super(parent);
		this.managerName = managerName;
		this.scenarioId = id;
		this.mode = mode;
	}

	/**
	 * 作成設定ID、シナリオ識別子、RPAツールIDを含めた作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public RpaScenarioDialog(Shell parent, String managerName, String createSettingId, String identifyString, String rpaToolId, int mode) {
		super(parent);
		this.managerName = managerName;
		this.initialCreateSettingId = createSettingId;
		this.initialIdentifyString = identifyString;
		this.initialRpaToolId = rpaToolId;
		this.mode = mode;
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
		shell.setText(Messages.getString("dialog.rpa.scenario.create.modify"));
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
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.heightHint = SizeConstant.SIZE_LABEL_HEIGHT;
		labelManager.setLayoutData(gridData);
		labelManager.setText(Messages.getString("facility.manager") + " : ");
		
		//コンボ
		if(this.mode == PropertyDefineConstant.MODE_MODIFY
				|| this.mode == PropertyDefineConstant.MODE_SHOW){
			this.m_managerComposite = new ManagerListComposite(composite, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(composite, SWT.NONE, true);
		}
		gridData = new GridData();
		gridData.widthHint = 207;
		this.m_managerComposite.setLayoutData(gridData);

		if(this.managerName != null) {
			this.m_managerComposite.setText(this.managerName);
		}
		
		this.m_managerComposite.addComboSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					// シナリオ実績作成設定一覧を更新
					scenarioOperationResultCreateSettingListComposite.setManagerName(m_managerComposite.getText());
				}
		});
		
		/*
		 * シナリオ実績作成設定ID
		 */
		//ラベル
		Label labelScenarioOperationResultCreateSettingId = new Label(composite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.heightHint = SizeConstant.SIZE_LABEL_HEIGHT;
		labelScenarioOperationResultCreateSettingId.setLayoutData(gridData);
		labelScenarioOperationResultCreateSettingId.setText(Messages.getString("rpa.scenario.operation.result.create.setting.id") + " : ");
		
		//コンボ
		if (this.mode == PropertyDefineConstant.MODE_ADD
				|| this.mode == PropertyDefineConstant.MODE_COPY) {
			this.scenarioOperationResultCreateSettingListComposite = new RpaScenarioOperationResultCreateSettingIdListComposite(composite, SWT.DROP_DOWN, this.managerName, true);
		} else {
			this.scenarioOperationResultCreateSettingListComposite = new RpaScenarioOperationResultCreateSettingIdListComposite(composite, SWT.DROP_DOWN, this.managerName, false);
		}
		gridData = new GridData();
		gridData.widthHint = 207;
		scenarioOperationResultCreateSettingListComposite.setLayoutData(gridData);
		scenarioOperationResultCreateSettingListComposite.addSelectionListenerToComboBox(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// オーナーロールIDを更新
				refreshRoleId();
			}
		});

		/*
		 * シナリオID
		 */
		//ラベル
		Label labelScenarioId = new Label(composite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.heightHint = SizeConstant.SIZE_LABEL_HEIGHT;
		labelScenarioId.setLayoutData(gridData);
		labelScenarioId.setText(Messages.getString("rpa.scenario.id") + " : ");
		
		//テキスト
		scenarioIdText = new Text(composite, SWT.BORDER);
		gridData = new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT);
		scenarioIdText.setLayoutData(gridData);
		scenarioIdText.setEnabled(false);

		/*
		 * RPAツール名
		 */
		//ラベル
		Label labelRpaToolName = new Label(composite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.heightHint = SizeConstant.SIZE_LABEL_HEIGHT;
		labelRpaToolName.setLayoutData(gridData);
		labelRpaToolName.setText(Messages.getString("rpa.tool") + " : ");
		
		//テキスト
		if (this.mode == PropertyDefineConstant.MODE_ADD
				|| this.mode == PropertyDefineConstant.MODE_COPY) {
			this.rpaToolComposite = new RpaToolListComposite(composite, SWT.DROP_DOWN, this.managerName, true);
		} else {
			this.rpaToolComposite = new RpaToolListComposite(composite, SWT.DROP_DOWN, this.managerName, false);
		}
		gridData = new GridData();
		gridData.widthHint = 207;
		this.rpaToolComposite.setLayoutData(gridData);

		/*
		 * シナリオ識別子
		 */
		//ラベル
		Label labelScenarioIdentifyString = new Label(composite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.heightHint = SizeConstant.SIZE_LABEL_HEIGHT;
		labelScenarioIdentifyString.setLayoutData(gridData);
		labelScenarioIdentifyString.setText(Messages.getString("rpa.scenario.identify.string") + " : ");
		
		//テキスト
		scenarioIdentifyStringText = new Text(composite, SWT.BORDER);
		gridData = new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT);
		scenarioIdentifyStringText.setLayoutData(gridData);
		scenarioIdentifyStringText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
			this.scenarioIdentifyStringText.setEnabled(false);
		}

		/*
		 * シナリオ名
		 */
		//ラベル
		Label labelScenarioName = new Label(composite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.heightHint = SizeConstant.SIZE_LABEL_HEIGHT;
		labelScenarioName.setLayoutData(gridData);
		labelScenarioName.setText(Messages.getString("rpa.scenario.name") + " : ");
		
		//テキスト
		scenarioNameText = new Text(composite, SWT.BORDER);
		gridData = new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT);
		scenarioNameText.setLayoutData(gridData);
		scenarioNameText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 説明
		 */
		//ラベル
		Label labelDescription = new Label(composite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.heightHint = SizeConstant.SIZE_LABEL_HEIGHT;
		labelDescription.setLayoutData(gridData);
		labelDescription.setText(Messages.getString("description") + " : ");
		
		//テキスト
		descriptionText = new Text(composite, SWT.BORDER);
		gridData = new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT);
		descriptionText.setLayoutData(gridData);
		descriptionText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * オーナーロールID
		 */
		//ラベル
		Label labelRoleId = new Label(composite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.heightHint = SizeConstant.SIZE_LABEL_HEIGHT;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		
		// テキスト
		roleIdText = new Text(composite, SWT.BORDER);
		gridData = new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT);
		roleIdText.setLayoutData(gridData);
		// オーナーロールIDは手動設定不可
		roleIdText.setEnabled(false);

		// ラインを引く
		Composite lineComposite = new Composite(parent, SWT.NONE);
		GridLayout lineGridLayout = new GridLayout(4, false);
		lineComposite.setLayout(lineGridLayout);
		
		Label line = new Label(lineComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 12;
		gridData.verticalIndent = 10;
		line.setLayoutData(gridData);

		// タブ
		m_tabFolder = new TabFolder(parent, SWT.NONE);
		
		//実行ノード
		scenarioExecNodeComposite = new RpaScenarioExecNodeComposite(m_tabFolder, SWT.NONE);
		TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
		tabItem1.setText(Messages.getString("rpa.scenario.exec.node"));
		tabItem1.setControl(scenarioExecNodeComposite);

		//分析用情報
		scenarioInformationComposite = new RpaScenarioInformationForAnalysisComposite(m_tabFolder, SWT.NONE, this.managerName);
		TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
		tabItem2.setText(Messages.getString("dialog.rpa.scenario.create.modify.analysis.information"));
		tabItem2.setControl(scenarioInformationComposite);

		m_tabFolder.setSelection(0);
		
		// ダイアログを調整
		this.adjustDialog();
		// ダイアログに情報反映
		this.reflectScenario();
		// 必須入力項目を可視化
		this.update();
	}

	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// 画面中央に配置
		Display calDisplay = shell.getDisplay();
		shell.setLocation((calDisplay.getBounds().width - shell.getSize().x) / 2,
				(calDisplay.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示
		
		// シナリオ識別子
		if("".equals(this.scenarioIdentifyStringText.getText())){
			this.scenarioIdentifyStringText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.scenarioIdentifyStringText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// シナリオ名
		if("".equals(this.scenarioNameText.getText())){
			this.scenarioNameText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.scenarioNameText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
	
	/**
	 * ダイアログの情報からシナリオ情報を作成します。
	 *
	 * @return 入力値の検証結果
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;
		result = validateEndpoint(this.m_managerComposite.getText());
		if (result != null) {
			return result;
		}

		this.inputData = new GetRpaScenarioResponse();

		//シナリオ実績作成設定ID取得
		if(scenarioOperationResultCreateSettingListComposite.getText().length() > 0){
			inputData.setScenarioOperationResultCreateSettingId(scenarioOperationResultCreateSettingListComposite.getText());
		} else {
			inputData.setScenarioOperationResultCreateSettingId("");
		}
		
		//RPAツールID取得
		if(rpaToolComposite.getText().length() > 0) {
			inputData.setRpaToolId((rpaToolComposite.getText()));
		}
		
		
		//シナリオ識別子取得
		if(scenarioIdentifyStringText.getText().length() > 0){
			inputData.setScenarioIdentifyString(scenarioIdentifyStringText.getText());
		}
		
		//シナリオ名取得
		if(scenarioNameText.getText().length() > 0){
			inputData.setScenarioName(scenarioNameText.getText());
		}
		
		//説明取得
		if(descriptionText.getText().length() > 0){
			inputData.setDescription(descriptionText.getText());
		} else {
			inputData.setDescription("");
		}

		/*
		 * 実行ノードタブ
		 */
		//共通のシナリオ取得
		inputData.setCommonNodeScenario(this.scenarioExecNodeComposite.getCommonNodeScenario());
		
		/*
		 * 分析用情報
		 */
		//シナリオタグ取得
		inputData.setTagList(this.scenarioInformationComposite.getScenarioTagList());
		
		//運用開始日時取得
		inputData.setOpeStartDate(this.scenarioInformationComposite.getStartDate());
		
		//手動操作時間タイプ取得
		inputData.setManualTimeCulcType(this.scenarioInformationComposite.getManualTimeCulcType());
		//手動操作時間取得
		String manualTimeText = this.scenarioInformationComposite.getManualTime();
		//自動算出する
		if (manualTimeText == null){
			inputData.setManualTime(null);
		} else if ("".equals(manualTimeText)) {
			//手動操作時間設定かつテキストなし
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.rpa.scenario.14"));
			return result;
		} else {
			//手動操作時間設定かつテキストあり
			try {
				Long manualTime = Long.valueOf(manualTimeText);
				//負の値またはLong型の最大/1000以上の値はエラーとする
				if (manualTime < 0 || 9223372036854775L < manualTime) {
					throw new NumberFormatException();
				}
				//秒をミリ秒に変換して設定
				inputData.setManualTime(manualTime * 1000);
			} catch (NumberFormatException e) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.rpa.scenario.15"));
				return result;
			}
		}
		
		return result;
	}

	/**
	 * ダイアログにシナリオ情報を反映します。
	 */
	private void reflectScenario() {
		// 初期表示
		GetRpaScenarioResponse scenarioInfo = null;
		if(mode == PropertyDefineConstant.MODE_MODIFY
				|| mode == PropertyDefineConstant.MODE_COPY
				|| mode == PropertyDefineConstant.MODE_SHOW){
			// 変更、コピーの場合、情報取得
			scenarioInfo = new GetRpaScenario().getRpaScenario(this.managerName, this.scenarioId);
		}else{
			// 作成の場合
			scenarioInfo = new GetRpaScenarioResponse();
			scenarioInfo.setScenarioOperationResultCreateSettingId(initialCreateSettingId);
			scenarioInfo.setScenarioIdentifyString(initialIdentifyString);
			scenarioInfo.setRpaToolId(initialRpaToolId);
		}
		this.inputData = scenarioInfo;
		refreshRoleId();
		this.scenarioInformationComposite.reflectInfomation();
		this.scenarioInformationComposite.setOpeStartDate(0L);
		
		//シナリオ情報取得
		if(scenarioInfo != null){
			if (scenarioInfo.getScenarioOperationResultCreateSettingId() != null) {
				this.scenarioOperationResultCreateSettingListComposite.setText(scenarioInfo.getScenarioOperationResultCreateSettingId());
				//シナリオ情報変更、または初期値が指定された場合、シナリオ実績作成設定IDは変更不可
				if (this.mode == PropertyDefineConstant.MODE_MODIFY || initialCreateSettingId != null) {
					this.scenarioOperationResultCreateSettingListComposite.setEnabled(false);
				}
			}
			if (scenarioInfo.getScenarioId() != null && mode == PropertyDefineConstant.MODE_MODIFY) {
				this.scenarioIdText.setText(scenarioInfo.getScenarioId());
			}
			if(scenarioInfo.getRpaToolId() != null){
				this.rpaToolComposite.setRpaToolId(scenarioInfo.getRpaToolId());
				if (initialRpaToolId != null) {
					// 初期値が指定された場合、RPAツールは変更不可
					this.rpaToolComposite.setEnabled(false);
				}
			}
			if(scenarioInfo.getScenarioIdentifyString() != null){
				this.scenarioIdentifyStringText.setText(scenarioInfo.getScenarioIdentifyString());
				if (initialIdentifyString != null) {
					// 初期値が指定された場合、シナリオ識別子は変更不可
					this.scenarioIdentifyStringText.setEnabled(false);
				}
			}
			if(scenarioInfo.getScenarioName() != null){
				this.scenarioNameText.setText(scenarioInfo.getScenarioName());
			}
			if(scenarioInfo.getDescription() != null){
				this.descriptionText.setText(scenarioInfo.getDescription());
			}
			if (scenarioInfo.getOwnerRoleId() != null) {
				this.roleIdText.setText(scenarioInfo.getOwnerRoleId());
			}
			
			if (scenarioInfo.getCommonNodeScenario() != null) {
				this.scenarioExecNodeComposite.setCommonNodeScenario(scenarioInfo.getCommonNodeScenario());
			}
			if (scenarioInfo.getExecNodeList() != null && !scenarioInfo.getExecNodeList().isEmpty() 
					&& mode == PropertyDefineConstant.MODE_MODIFY) {
				this.scenarioExecNodeComposite.setExecNodeList(scenarioInfo.getExecNodeList());
				this.scenarioExecNodeComposite.reflectExecNodeInfo();
			}
			
			if (scenarioInfo.getTagList() != null && !scenarioInfo.getTagList().isEmpty()){
				this.scenarioInformationComposite.setScenarioTagList(scenarioInfo.getTagList());
			}
			if (scenarioInfo.getOpeStartDate() != null){
				this.scenarioInformationComposite.setOpeStartDate(scenarioInfo.getOpeStartDate());
			}
			if (scenarioInfo.getManualTimeCulcType() != null ){
				this.scenarioInformationComposite.setManualTime(scenarioInfo.getManualTimeCulcType(), scenarioInfo.getManualTime());
			}
		}

		this.update();
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
		
		GetRpaScenarioResponse info = this.inputData;
		String managerName = this.m_managerComposite.getText();
		if(info != null){
			try {
				if(mode == PropertyDefineConstant.MODE_ADD || mode == PropertyDefineConstant.MODE_COPY){
					// 作成の場合
					AddRpaScenarioRequest addInfoReq = new AddRpaScenarioRequest();
					RestClientBeanUtil.convertBean(info, addInfoReq);
					
					// タグは個別にリレーション用のメンバに変換
					for (RpaScenarioTagResponse infoTag : info.getTagList()){
						addInfoReq.getTagRelationList().add(infoTag.getTagId());
					}
					
					result = new AddRpaScenario().add(managerName, addInfoReq);
				} else if (mode == PropertyDefineConstant.MODE_MODIFY){
					// 変更の場合
					ModifyRpaScenarioRequest modifyInfoReq = new ModifyRpaScenarioRequest();
					RestClientBeanUtil.convertBean(info, modifyInfoReq);
					
					// タグは個別にリレーション用のメンバに変換
					for (RpaScenarioTagResponse infoTag : info.getTagList()){
						modifyInfoReq.getTagRelationList().add(infoTag.getTagId());
					}
					
					result = new ModifyRpaScenario().modify(managerName, scenarioIdText.getText(), modifyInfoReq);
				}
			}  catch (HinemosUnknown e) {
				log.error("action() Failed to convert RpaScenario");
			}
		} else {
			log.error("action() RpaScenario is null");
		}
		return result;
	}
	
	/**
	 * オーナーロールIDを更新
	 */
	private void refreshRoleId() {
		String ownerRoleId = scenarioOperationResultCreateSettingListComposite.getOwnerRoleId();
		// シナリオ実績作成設定のオーナーロールを表示
		roleIdText.setText(ownerRoleId);
	}

	@Override
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		// RpaRestEndpointsにはcheckPublishが存在しない
		// どのEndpointでも内容は同じなのでUtilityを使用する
		return UtilityRestClientWrapper.getWrapper(managerName);
	}
	
}
