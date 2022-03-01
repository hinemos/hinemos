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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.AddRpaScenarioOperationResultCreateSettingRequest;
import org.openapitools.client.model.AddRpaScenarioOperationResultCreateSettingRequest.IntervalEnum;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.ModifyRpaScenarioOperationResultCreateSettingRequest;
import org.openapitools.client.model.NotifyRelationInfoRequest;
import org.openapitools.client.model.NotifyRelationInfoResponse;
import org.openapitools.client.model.RpaScenarioOperationResultCreateSettingResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.calendar.composite.CalendarIdListComposite;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.DateTimeDialog;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.notify.composite.NotifyInfoComposite;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.rpa.action.AddRpaScenarioCreateSetting;
import com.clustercontrol.rpa.action.GetRpaScenarioCreateSetting;
import com.clustercontrol.rpa.action.ModifyRpaScenarioCreateSetting;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;

/**
 * シナリオ実績作成設定作成・変更ダイアログクラス
 */
public class RpaScenarioOperationResultCreateSettingDialog extends CommonDialog {

	// ログ
	private static Log log = LogFactory.getLog( RpaScenarioOperationResultCreateSettingDialog.class );
	/** 実績作成設定ID */
	private String settingId = "";
	/** 変更用ダイアログ判別フラグ */
	private int mode;
	/** 実績作成設定ID */
	private Text settingIdText = null;
	/** オーナーロールIDコンボボックス用コンポジット */
	private RoleIdListComposite ownerRoleIdListComposite = null;
	/** マネージャ名 */
	private String managerName = null;
	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;	
	/** スコープテキスト */
	private Text m_textScope;
	/** 対象スコープのファシリティID */
	private String m_facilityId;
	/** 作成間隔 コンボボックス */
	private Combo m_comboRunInterval = null;
	/** カレンダID コンポジット */
	private CalendarIdListComposite m_calendarId = null;
	/** 作成対象日(from) */
	private Text createFromDateText = null;
	/** 通知情報 */
	private NotifyInfoComposite m_notifyIdList = null;
	/** この設定を有効にする */
	private Button m_validFlg = null;

	// ----- 共通メンバ変数 ----- //
	private Shell shell = null;
	private Text descriptionText = null;

	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public RpaScenarioOperationResultCreateSettingDialog(Shell parent, String managerName, String id, int mode) {
		super(parent);
		this.managerName = managerName;
		this.settingId = id;
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
		shell.setText(Messages.getString("dialog.rpa.scenario.create.setting.create.modify"));
		GridData gridData = new GridData();
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 13;
		parent.setLayout(layout);
		
		/*
		 * マネージャ
		 */
		Label labelManager = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 7;
		labelManager.setLayoutData(gridData);
		labelManager.setText(Messages.getString("facility.manager") + " : ");
		if(this.mode == PropertyDefineConstant.MODE_MODIFY
				|| this.mode == PropertyDefineConstant.MODE_SHOW){
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, true);
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					// オーナーロールIDの対処
					ownerRoleIdListComposite.createRoleIdList(managerName);
				}
			});
		}
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 5;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);

		if(this.managerName != null) {
			this.m_managerComposite.setText(this.managerName);
		}
		
		if(this.mode != PropertyDefineConstant.MODE_MODIFY
				&& this.mode != PropertyDefineConstant.MODE_SHOW) {
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					ownerRoleIdListComposite.createRoleIdList(managerName);
					m_calendarId.createCalIdCombo(m_managerComposite.getText(), ownerRoleIdListComposite.getText());
					m_notifyIdList.setManagerName(m_managerComposite.getText());
					m_notifyIdList.setOwnerRoleId(ownerRoleIdListComposite.getText(), true);
				}
			});
		}

		
		/*
		 * 実績作成設定ID
		 */
		//ラベル
		Label lblsettingId = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblsettingId.setLayoutData(gridData);
		lblsettingId.setText(Messages.getString("RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID") + " : ");
		//テキスト
		settingIdText = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		settingIdText.setLayoutData(gridData);
		settingIdText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 説明
		 */
		//ラベル
		Label lblScenarioTagDescription = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblScenarioTagDescription.setLayoutData(gridData);
		lblScenarioTagDescription.setText(Messages.getString("DESCRIPTION") + " : ");
		//テキスト
		descriptionText = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
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
		Label labelRoleId = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("OWNER_ROLE_ID") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_ADD
				|| this.mode == PropertyDefineConstant.MODE_COPY) {
			this.ownerRoleIdListComposite = new RoleIdListComposite(parent,
					SWT.NONE, this.managerName, true, Mode.OWNER_ROLE);
		} else {
			this.ownerRoleIdListComposite = new RoleIdListComposite(parent, SWT.NONE, this.managerName, false, Mode.OWNER_ROLE);
		}
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		ownerRoleIdListComposite.setLayoutData(gridData);
		ownerRoleIdListComposite.addComboSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 参照可能なカレンダID, 通知IDを更新
				m_calendarId.createCalIdCombo(m_managerComposite.getText(), ownerRoleIdListComposite.getText());
				m_notifyIdList.setOwnerRoleId(ownerRoleIdListComposite.getText(), true);
				// スコープをリセット
				m_textScope.setText("");
			}

		});

		/*
		 * スコープ
		 */
		// ラベル
		Label labelScope = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelScope.setLayoutData(gridData);
		labelScope.setText(Messages.getString("SCOPE") + " : ");
		// テキスト
		this.m_textScope = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textScope.setLayoutData(gridData);
		this.m_textScope.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 参照ボタン
		Button m_buttonScope = new Button(parent, SWT.NONE);
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

				String managerName = m_managerComposite.getText();
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell, managerName, ownerRoleIdListComposite.getText());
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse item = dialog.getSelectItem();
					FacilityInfoResponse info = item.getData();
					m_facilityId = info.getFacilityId();
					if (info.getFacilityType() == FacilityInfoResponse.FacilityTypeEnum.NODE) {
						m_textScope.setText(info.getFacilityName());
					} else {
						FacilityPath path = new FacilityPath(
								ClusterControlPlugin.getDefault()
								.getSeparator());
						m_textScope.setText(path.getPath(item));
					}
				}
			}
		});
		
		/*
		 * 実行間隔（分）
		 */
		// ラベル
		Label label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("CREATE_INTERVAL") + " : ");
		// コンボボックス
		this.m_comboRunInterval = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboRunInterval.setLayoutData(gridData);
		this.m_comboRunInterval.add(RunInterval.TYPE_SEC_30.toString());
		this.m_comboRunInterval.add(RunInterval.TYPE_MIN_01.toString());
		this.m_comboRunInterval.add(RunInterval.TYPE_MIN_05.toString());
		this.m_comboRunInterval.add(RunInterval.TYPE_MIN_10.toString());
		this.m_comboRunInterval.add(RunInterval.TYPE_MIN_30.toString());
		this.m_comboRunInterval.add(RunInterval.TYPE_MIN_60.toString());
		this.m_comboRunInterval.setData(RunInterval.TYPE_SEC_30.toString(), IntervalEnum.SEC_30);
		this.m_comboRunInterval.setData(RunInterval.TYPE_MIN_01.toString(), IntervalEnum.MIN_01);
		this.m_comboRunInterval.setData(RunInterval.TYPE_MIN_05.toString(), IntervalEnum.MIN_05);
		this.m_comboRunInterval.setData(RunInterval.TYPE_MIN_10.toString(), IntervalEnum.MIN_10);
		this.m_comboRunInterval.setData(RunInterval.TYPE_MIN_30.toString(), IntervalEnum.MIN_30);
		this.m_comboRunInterval.setData(RunInterval.TYPE_MIN_60.toString(), IntervalEnum.MIN_60);

		// 余白
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * カレンダ
		 */
		this.m_calendarId = new CalendarIdListComposite(parent, SWT.NONE, true, 8, 6);
		gridData = new GridData();
		gridData.horizontalSpan = 13;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_calendarId.setLayoutData(gridData);
		
		/*
		 * 作成対象日(from)
		 */
		// ラベル
		Label lblcreateTimeFrom = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblcreateTimeFrom.setLayoutData(gridData);
		lblcreateTimeFrom.setText(Messages.getString("CREATE_FROM_DATE") + " : ");
		// テキスト
		createFromDateText = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		createFromDateText.setLayoutData(gridData);
		// 日時ダイアログからの入力しか受け付けません
		createFromDateText.setEnabled(false);
		createFromDateText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// 追加ボタン
		Button createTimeFromButton = new Button(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		createTimeFromButton.setLayoutData(gridData);
		createTimeFromButton.setText(Messages.getString("calendar.button"));
		createTimeFromButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DateTimeDialog dialog = new DateTimeDialog(shell);
				if (createFromDateText.getText().length() > 0) {
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					try {
						Date date = sdf.parse(createFromDateText.getText());
						dialog.setDate(date);
					} catch (ParseException e1) {
						log.warn("createTimeFromText : " + e1.getMessage());
						
					}
				}
				if (dialog.open() == IDialogConstants.OK_ID) {
					// ダイアログより取得した日時を"yyyy/MM/dd HH:mm:ss"の形式に変換
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					String tmp = sdf.format(dialog.getDate());
					createFromDateText.setText(tmp);
					update();
				}
			}
		});
		
		/*
		 * 通知情報の属性グループ
		 */
		// グループ
		Group groupNotifyAttribute = new Group(parent, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 1;
		groupNotifyAttribute.setLayout(layout);
		groupNotifyAttribute.setText(Messages.getString("notify.attribute"));
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupNotifyAttribute.setLayoutData(gridData);

		// 通知
		this.m_notifyIdList = new NotifyInfoComposite(groupNotifyAttribute, SWT.NONE);
		this.m_notifyIdList.setManagerName(this.m_managerComposite.getText());
		this.m_notifyIdList.setMinApplicationLen(0);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_notifyIdList.setLayoutData(gridData);
	

		// 設定の（有効／無効）
		m_validFlg = new Button(parent, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_validFlg.setText(Messages.getString("setting.valid.confirmed"));
		m_validFlg.setLayoutData(gridData);

		
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
		
		
		// ダイアログを調整
		this.adjustDialog();
		//ダイアログにテンプレートセット詳細情報反映
		this.reflectCreateSetting();
		// 必須入力項目を可視化
		this.update();

	}


	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(600, shell.getSize().y));

		// 画面中央に配置
		Display calAdjustDisplay = shell.getDisplay();
		shell.setLocation((calAdjustDisplay.getBounds().width - shell.getSize().x) / 2,
				(calAdjustDisplay.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示
		// スコープ
		if("".equals(this.m_textScope.getText())){
			this.m_textScope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 実績作成設定ID
		if("".equals(this.settingIdText.getText())){
			this.settingIdText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.settingIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 作成対象日
		if("".equals(this.createFromDateText.getText())){
			this.createFromDateText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.createFromDateText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
	/**
	 * ダイアログの情報からシナリオ実績作成設定情報を作成します。
	 *
	 * @return リクエストDTO
	 *
	 */
	private AddRpaScenarioOperationResultCreateSettingRequest createSettingRequest() {
		AddRpaScenarioOperationResultCreateSettingRequest inputData = new AddRpaScenarioOperationResultCreateSettingRequest();

		inputData.setScenarioOperationResultCreateSettingId(settingIdText.getText());
		inputData.setOwnerRoleId(this.ownerRoleIdListComposite.getText());
		inputData.setDescription(this.descriptionText.getText());
		inputData.setFacilityId(this.m_facilityId);
		inputData.setInterval((IntervalEnum)this.m_comboRunInterval.getData(this.m_comboRunInterval.getText()));
		inputData.setCreateFromDate(this.createFromDateText.getText());
		if (!m_calendarId.getText().isEmpty()) {
			inputData.setCalendarId(m_calendarId.getText());
		}
		
		//通知関連情報の設定
		//コンポジットから通知情報を取得します。
		List<NotifyRelationInfoRequest> notifyId = new ArrayList<>();
		if (this.m_notifyIdList.getNotify() != null) {
			for (NotifyRelationInfoResponse notify : this.m_notifyIdList.getNotify()) {
				NotifyRelationInfoRequest nottifyDto = new NotifyRelationInfoRequest();
				nottifyDto.setNotifyId(notify.getNotifyId());
				notifyId.add(nottifyDto);
			}
		}
		inputData.setNotifyId(notifyId);

		inputData.setApplication(m_notifyIdList.getApplication());
		inputData.setValidFlg(m_validFlg.getSelection());
		return inputData;
	}

	/**
	 * ダイアログにシナリオ実績作成設定情報を反映します。
	 *
	 * @param detailList
	 */
	private void reflectCreateSetting() {
		// 初期表示
		RpaScenarioOperationResultCreateSettingResponse settingInfo = null;
		if(mode == PropertyDefineConstant.MODE_MODIFY
				|| mode == PropertyDefineConstant.MODE_COPY){
			// 変更、コピーの場合、情報取得、セット
			settingInfo = new GetRpaScenarioCreateSetting().getSetting(this.managerName, this.settingId);
			if (settingInfo != null) {
				// ID
				this.settingIdText.setText(settingInfo.getScenarioOperationResultCreateSettingId());
				// オーナーロールID
				this.ownerRoleIdListComposite.setText(settingInfo.getOwnerRoleId());
				//変更の場合、マネージャ、実績作成設定ID及びオーナーロールIDは変更不可
				if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
					this.m_managerComposite.setEnabled(false);
					this.settingIdText.setEnabled(false);
					this.ownerRoleIdListComposite.setEnabled(false);
				}
				// 説明
				this.descriptionText.setText((HinemosMessage.replace(settingInfo.getDescription())));
				// スコープ
				this.m_textScope.setText(settingInfo.getScope());
				this.m_facilityId = settingInfo.getFacilityId();
				// 作成間隔
				this.m_comboRunInterval.setText(RunInterval.enumToString(settingInfo.getInterval(), org.openapitools.client.model.RpaScenarioOperationResultCreateSettingResponse.IntervalEnum.class));
	
				// カレンダ
				this.m_calendarId.createCalIdCombo(m_managerComposite.getText(), ownerRoleIdListComposite.getText());
				this.m_calendarId.setText(settingInfo.getCalendarId());
	
				// 作成対象日
				this.createFromDateText.setText(settingInfo.getCreateFromDate());
				
				// 通知ID
				this.m_notifyIdList.setManagerName(this.m_managerComposite.getText());
				this.m_notifyIdList.setOwnerRoleId(settingInfo.getOwnerRoleId(), true);
				this.m_notifyIdList.setNotify(settingInfo.getNotifyId());
				
				this.m_notifyIdList.setApplication(settingInfo.getApplication());
				// 有効フラグ
				this.m_validFlg.setSelection(settingInfo.getValidFlg());
			}
		} else {
			// 作成の場合
			// 初期値として当日00:00:00を作成対象日にセット
			this.createFromDateText.setText(TimezoneUtil.getSimpleDateFormat().format(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH)));
			this.m_comboRunInterval.select(1);
			this.m_calendarId.createCalIdCombo(m_managerComposite.getText(), ownerRoleIdListComposite.getText());
			this.m_notifyIdList.setManagerName(this.m_managerComposite.getText());
			this.m_notifyIdList.setOwnerRoleId(this.ownerRoleIdListComposite.getText(), true);
			this.m_validFlg.setSelection(true);
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
		AddRpaScenarioOperationResultCreateSettingRequest info = createSettingRequest(); 
		String managerName = this.m_managerComposite.getText();
		try {
			if(mode == PropertyDefineConstant.MODE_ADD || mode == PropertyDefineConstant.MODE_COPY){
				// 作成の場合
				result = new AddRpaScenarioCreateSetting().add(managerName, info);
			} else if (mode == PropertyDefineConstant.MODE_MODIFY){
				// 変更の場合
				ModifyRpaScenarioOperationResultCreateSettingRequest modifyInfoReq = new ModifyRpaScenarioOperationResultCreateSettingRequest();
				RestClientBeanUtil.convertBean(info, modifyInfoReq);
				result = new ModifyRpaScenarioCreateSetting().modify(managerName, settingIdText.getText(), modifyInfoReq);
			}
		}  catch (HinemosUnknown e) {
			log.error("action() Failed to convert RpaScenarioOperationResultCreateSetting");
		}
		return result;
	}

	@Override
	protected ValidateResult validate() {
		return validateEndpoint(this.m_managerComposite.getText());
	}

	@Override
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		// RpaRestEndpointsにはcheckPublishが存在しない
		// どのEndpointでも内容は同じなのでUtilityを使用する
		return UtilityRestClientWrapper.getWrapper(managerName);
	}
	
}
