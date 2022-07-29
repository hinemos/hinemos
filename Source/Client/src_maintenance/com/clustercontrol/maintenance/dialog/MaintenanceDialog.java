/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.dialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.openapitools.client.model.AddMaintenanceRequest;
import org.openapitools.client.model.MaintenanceInfoResponse;
import org.openapitools.client.model.MaintenanceInfoResponse.TypeIdEnum;
import org.openapitools.client.model.MaintenanceScheduleRequest;
import org.openapitools.client.model.MaintenanceScheduleResponse;
import org.openapitools.client.model.NotifyRelationInfoResponse;

import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.calendar.composite.CalendarIdListComposite;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.composite.action.ComboModifyListener;
import com.clustercontrol.composite.action.NumberKeyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceDuplicate;
import com.clustercontrol.maintenance.action.GetMaintenance;
import com.clustercontrol.maintenance.action.ModifyMaintenance;
import com.clustercontrol.maintenance.composite.MaintenanceTypeListComposite;
import com.clustercontrol.maintenance.util.MaintenanceRestClientWrapper;
import com.clustercontrol.notify.composite.NotifyInfoComposite;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * メンテナンス[メンテナンスの作成・変更]ダイアログクラスです。
 *
 * @version 4.0.0
 * @since 2.2.0
 */
public class MaintenanceDialog extends CommonDialog {

	public static final int WIDTH_TITLE = 5;
	public static final int WIDTH_TEXT = 9;

	/** メンテナンスID用テキスト */
	private Text m_textMaintenanceId = null;
	/** ダイアログ表示時の処理タイプ */
	private int mode;
	/** 説明 */
	private Text m_textDescription = null;
	/** メンテナンス種別 */
	private MaintenanceTypeListComposite m_maintenance_type = null;
	/** 保存期間(日)用テキスト*/
	private Text m_textDataRetentionPeriod = null;
	/** カレンダID用テキスト */
	private CalendarIdListComposite m_calendarId = null;
	/** スケジュール日時指定用ラジオボタン */
	private Button m_type1 = null;
	/** スケジュール曜日指定用ラジオボタン */
	private Button m_type2 = null;
	/** 月指定用コンボボックス */
	private Combo m_comboMonth = null;
	/** 日指定用コンボボックス */
	private Combo m_comboDay = null;
	/** 時指定用コンボボックス */
	private Combo m_comboHours1 = null;
	/** 分指定用コンボボックス */
	private Combo m_comboMinutes1 = null;
	/** 曜日指定用コンボボックス */
	private Combo m_comboDayOfWeek = null;
	/** 時指定用コンボボックス */
	private Combo m_comboHours2 = null;
	/** 分指定用コンボボックス */
	private Combo m_comboMinutes2 = null;
	/** メンテナンスID */
	private String maintenanceId = null;
	/** 通知情報 */
	private NotifyInfoComposite notifyInfo = null;
	/** オーナーロールID用テキスト */
	private RoleIdListComposite m_ownerRoleId = null;
	/** この設定を有効にする */
	private Button confirmValid = null;

	private MaintenanceInfoResponse maintenanceInfo = null;

	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	/** マネージャ名 */
	private String managerName = null;

	public MaintenanceDialog(Shell parent, String managerName, String maintenanceId, int mode) {
		super(parent);
		this.managerName = managerName;
		this.maintenanceId = maintenanceId;
		this.mode = mode;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.history.delete.settings.modify"));

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
		 * マネージャ
		 */
		Label labelManager = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", labelManager);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelManager.setLayoutData(gridData);
		labelManager.setText(Messages.getString("facility.manager") + " : ");
		if(this.mode == PropertyDefineConstant.MODE_MODIFY){
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, true);
		}
		WidgetTestUtil.setTestId(this, "managerComposite", this.m_managerComposite);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);

		if(this.managerName != null) {
			this.m_managerComposite.setText(this.managerName);
		}
		if(this.mode != PropertyDefineConstant.MODE_MODIFY) {
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					m_ownerRoleId.createRoleIdList(managerName);
					m_calendarId.createCalIdCombo(managerName, m_ownerRoleId.getText());
					notifyInfo.setManagerName(m_managerComposite.getText());
					notifyInfo.setOwnerRoleId(m_ownerRoleId.getText(), true);
					m_maintenance_type.setManagerName(m_managerComposite.getText());
					m_maintenance_type.update();
				}
			});
		}

		/*
		 * メンテナンスID
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "maintenanceid", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("maintenance.id") + " : ");
		// テキスト
		this.m_textMaintenanceId = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "id", m_textMaintenanceId);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textMaintenanceId.setLayoutData(gridData);
		if(this.mode == PropertyDefineConstant.MODE_MODIFY
				|| this.mode == PropertyDefineConstant.MODE_SHOW){
			this.m_textMaintenanceId.setEnabled(false);
		}
		this.m_textMaintenanceId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 説明
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "description", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("description") + " : ");
		// テキスト
		this.m_textDescription = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "description", m_textDescription);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textDescription.setLayoutData(gridData);
		this.m_textDescription.addModifyListener(new ModifyListener(){
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
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_ADD
				|| this.mode == PropertyDefineConstant.MODE_COPY) {
			this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE, this.managerName, true, Mode.OWNER_ROLE);
			this.m_ownerRoleId.getComboRoleId().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_calendarId.createCalIdCombo(managerName, m_ownerRoleId.getText());
					notifyInfo.setOwnerRoleId(m_ownerRoleId.getText(), true);
				}
			});
		} else {
			this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE, this.managerName, false, Mode.OWNER_ROLE);
		}
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_ownerRoleId.setLayoutData(gridData);

		/*
		 * 種別
		 */
		this.m_maintenance_type = new MaintenanceTypeListComposite(parent, SWT.NONE, this.m_managerComposite.getText(), true);
		WidgetTestUtil.setTestId(this, "typelist", m_maintenance_type);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_maintenance_type.setLayoutData(gridData);
		this.m_maintenance_type.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 保存期間(日)
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "retentionperiod", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("maintenance.retention.period") + " : ");
		// テキスト
		this.m_textDataRetentionPeriod = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "dataretentionperiod", m_textDataRetentionPeriod);
		this.m_textDataRetentionPeriod.setToolTipText(Messages.getString("message.maintenance.23"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textDataRetentionPeriod.setLayoutData(gridData);
		this.m_textDataRetentionPeriod.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * カレンダ
		 */
		this.m_calendarId = new CalendarIdListComposite(parent, SWT.NONE, true, 
				WIDTH_TITLE, WIDTH_TEXT);
		WidgetTestUtil.setTestId(this, "calendaridlist", m_calendarId);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_calendarId.setLayoutData(gridData);

		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "schedule", label);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * スケジュール
		 */
		Group group1 = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "schedule", group1);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 20;
		group1.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		group1.setLayoutData(gridData);
		group1.setText(Messages.getString("schedule"));

		// スケジュール日時指定、曜日指定
		createType1(group1);
		createType2(group1);

		/*
		 * 通知情報の属性グループ
		 */
		// グループ
		Group groupNotifyAttribute = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyattribute", groupNotifyAttribute);
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

		// 通知情報（通知ID，アプリケーションID）
		this.notifyInfo = new NotifyInfoComposite(groupNotifyAttribute, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyinfo", notifyInfo);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		notifyInfo.setLayoutData(gridData);
		notifyInfo.setManagerName(this.m_managerComposite.getText());

		/*
		 * 有効／無効
		 */
		this.confirmValid = new Button(parent, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "confirmvalid", confirmValid);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.confirmValid.setLayoutData(gridData);
		this.confirmValid.setText(Messages.getString("setting.valid.confirmed"));
		this.confirmValid.setSelection(true);

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 15;
		line.setLayoutData(gridData);

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(550, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);


		//最後に変更であれば情報を表示する
		MaintenanceInfoResponse info = null;
		if(this.maintenanceId != null){
			info = new GetMaintenance().getMaintenanceInfo(this.m_managerComposite.getText(), this.maintenanceId);
		}
		this.setInputData(info);
		this.reflectMaintenanceSchedule();
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	private void update(){
		// 各項目が必須項目であることを明示
		if(this.m_textMaintenanceId.getEnabled() && "".equals(this.m_textMaintenanceId.getText())){
			this.m_textMaintenanceId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textMaintenanceId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_maintenance_type.getText())){
			this.m_maintenance_type.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_maintenance_type.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_textDataRetentionPeriod.getText())){
			this.m_textDataRetentionPeriod.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textDataRetentionPeriod.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_comboHours1.getEnabled() && "".equals(this.m_comboHours1.getText())){
			this.m_comboHours1.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboHours1.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_comboMinutes1.getEnabled() && "".equals(this.m_comboMinutes1.getText())){
			this.m_comboMinutes1.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboMinutes1.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_comboDayOfWeek.getEnabled() && "".equals(this.m_comboDayOfWeek.getText())){
			this.m_comboDayOfWeek.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboDayOfWeek.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_comboHours2.getEnabled() && "".equals(this.m_comboHours2.getText())){
			this.m_comboHours2.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboHours2.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_comboMinutes2.getEnabled() && "".equals(this.m_comboMinutes2.getText())){
			this.m_comboMinutes2.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboMinutes2.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}


	/**
	 * スケジュール日時指定を生成します。
	 *
	 * @param parent 親グループ
	 */
	private void createType1(Group parent) {
		m_type1 = new Button(parent, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "type1", m_type1);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_type1.setLayoutData(gridData);
		m_type1.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_type2.setSelection(false);
					m_comboMonth.setEnabled(true);
					m_comboDay.setEnabled(true);
					m_comboHours1.setEnabled(true);
					m_comboMinutes1.setEnabled(true);
					m_comboDayOfWeek.setEnabled(false);
					m_comboHours2.setEnabled(false);
					m_comboMinutes2.setEnabled(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		m_comboMonth = new Combo(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "month", m_comboMonth);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboMonth.setLayoutData(gridData);
		m_comboMonth.setTextLimit(2);
		m_comboMonth.setVisibleItemCount(10);
		m_comboMonth.addKeyListener(new NumberKeyListener());
		m_comboMonth.addModifyListener(new ComboModifyListener());
		DecimalFormat format = new DecimalFormat("00");
		m_comboMonth.add("");
		for (int month = 1; month <= 12; month++) {
			m_comboMonth.add(format.format(month));
		}

		Label labelMonth = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "month", labelMonth);
		labelMonth.setText(Messages.getString("month"));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelMonth.setLayoutData(gridData);

		m_comboDay = new Combo(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "day", m_comboDay);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboDay.setLayoutData(gridData);
		m_comboDay.setTextLimit(2);
		m_comboDay.setVisibleItemCount(10);
		m_comboDay.addKeyListener(new NumberKeyListener());
		m_comboDay.addModifyListener(new ComboModifyListener());
		format = new DecimalFormat("00");
		m_comboDay.add("");
		for (int day = 1; day <= 31; day++) {
			m_comboDay.add(format.format(day));
		}

		Label labelDay = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "day", labelDay);
		labelDay.setText(Messages.getString("monthday"));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelDay.setLayoutData(gridData);

		m_comboHours1 = new Combo(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "hours1", m_comboHours1);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboHours1.setLayoutData(gridData);
		m_comboHours1.setTextLimit(2);
		m_comboHours1.setVisibleItemCount(10);
		m_comboHours1.addKeyListener(new NumberKeyListener());
		m_comboHours1.addModifyListener(new ComboModifyListener());
		format = new DecimalFormat("00");
		m_comboHours1.add("");
		for (int hour = 0; hour < 24; hour++) {
			m_comboHours1.add(format.format(hour));
		}
		this.m_comboHours1.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelHours = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "hours", labelHours);
		labelHours.setText(Messages.getString("hour"));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelHours.setLayoutData(gridData);

		m_comboMinutes1 = new Combo(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "minutes1", m_comboMinutes1);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboMinutes1.setLayoutData(gridData);
		m_comboMinutes1.setTextLimit(2);
		m_comboMinutes1.setVisibleItemCount(10);
		m_comboMinutes1.addKeyListener(new NumberKeyListener());
		m_comboMinutes1.addModifyListener(new ComboModifyListener());
		format = new DecimalFormat("00");
		m_comboMinutes1.add("");
		for (int minutes = 0; minutes < 60; minutes++) {
			m_comboMinutes1.add(format.format(minutes));
		}
		this.m_comboMinutes1.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelMinutes = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "minutes", labelMinutes);
		labelMinutes.setText(Messages.getString("minute"));
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelMinutes.setLayoutData(gridData);

		// 空白
		Label label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
	}

	/**
	 * スケジュール曜日指定を生成します。
	 *
	 * @param parent 親グループ
	 */
	private void createType2(Group parent) {
		m_type2 = new Button(parent, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "type2", m_type2);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_type2.setLayoutData(gridData);
		m_type2.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_type1.setSelection(false);
					m_comboMonth.setEnabled(false);
					m_comboDay.setEnabled(false);
					m_comboHours1.setEnabled(false);
					m_comboMinutes1.setEnabled(false);
					m_comboDayOfWeek.setEnabled(true);
					m_comboHours2.setEnabled(true);
					m_comboMinutes2.setEnabled(true);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Label labelDummy = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "dummy", labelDummy);
		labelDummy.setText("");
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelDummy.setLayoutData(gridData);

		m_comboDayOfWeek = new Combo(parent, SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "dayofweek", m_comboDayOfWeek);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 5;
		m_comboDayOfWeek.setLayoutData(gridData);
		m_comboDayOfWeek.setTextLimit(3);
		m_comboDayOfWeek.setVisibleItemCount(10);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_SUNDAY);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_MONDAY);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_TUESDAY);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_WEDNESDAY);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_THURSDAY);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_FRIDAY);
		m_comboDayOfWeek.add(DayOfWeekConstant.STRING_SATURDAY);
		this.m_comboDayOfWeek.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, label);
		labelDummy.setText("");
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelDummy.setLayoutData(gridData);

		m_comboHours2 = new Combo(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "hours2", m_comboHours2);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboHours2.setLayoutData(gridData);
		m_comboHours2.setTextLimit(2);
		m_comboHours2.setVisibleItemCount(10);
		m_comboHours2.addKeyListener(new NumberKeyListener());
		m_comboHours2.addModifyListener(new ComboModifyListener());
		DecimalFormat format = new DecimalFormat("00");
		m_comboHours2.add("");
		for (int hour = 0; hour < 24; hour++) {
			m_comboHours2.add(format.format(hour));
		}
		this.m_comboHours2.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelHours = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "hours", labelHours);
		labelHours.setText(Messages.getString("hour"));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelHours.setLayoutData(gridData);

		m_comboMinutes2 = new Combo(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "minutes2", m_comboMinutes2);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_comboMinutes2.setLayoutData(gridData);
		m_comboMinutes2.setTextLimit(2);
		m_comboMinutes2.setVisibleItemCount(10);
		m_comboMinutes2.addKeyListener(new NumberKeyListener());
		m_comboMinutes2.addModifyListener(new ComboModifyListener());
		format = new DecimalFormat("00");
		m_comboMinutes2.add("");
		for (int minutes = 0; minutes < 60; minutes++) {
			m_comboMinutes2.add(format.format(minutes));
		}
		this.m_comboMinutes2.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		Label labelMinutes = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "minutes", labelMinutes);
		labelMinutes.setText(Messages.getString("minute"));
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelMinutes.setLayoutData(gridData);

		// 空白
		Label labelSpace = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space", labelSpace);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelSpace.setLayoutData(gridData);
	}

	/**
	 * ダイアログにスケジュール情報を反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.ScheduleTableDefine
	 */
	private void reflectMaintenanceSchedule() {

		// 新規作成の場合はこちらのルートを通る。
		if (maintenanceId == null) {

			m_type1.setSelection(true);
			m_comboMonth.setEnabled(true);
			m_comboDay.setEnabled(true);
			m_comboHours1.setEnabled(true);
			m_comboMinutes1.setEnabled(true);
			m_comboDayOfWeek.setEnabled(false);
			m_comboHours2.setEnabled(false);
			m_comboMinutes2.setEnabled(false);
			this.update();
			return;
		}else{
			maintenanceInfo = new GetMaintenance().getMaintenanceInfo(this.m_managerComposite.getText(), maintenanceId);
			//スケジュール設定
			MaintenanceScheduleResponse schedule = maintenanceInfo.getSchedule();

			DecimalFormat format = new DecimalFormat("00");
			//日時を設定
			if (MaintenanceScheduleResponse.TypeEnum.DAY.equals(schedule.getType())) {
				m_comboMonth.select(0);
				//月を設定
				for (int i = 0; i < m_comboMonth.getItemCount(); i++) {
					if (schedule.getMonth() == null) {
						break;
					}
					String month = format.format(schedule.getMonth());
					if (month.equals(m_comboMonth.getItem(i))) {
						m_comboMonth.select(i);
						break;
					}
				}
				//日を設定
				m_comboDay.select(0);
				for (int i = 0; i < m_comboDay.getItemCount(); i++) {
					if (schedule.getDay() == null) {
						break;
					}
					String day = format.format(schedule.getDay());
					if (day.equals(m_comboDay.getItem(i))) {
						m_comboDay.select(i);
						break;
					}
				}
				//時を設定
				m_comboHours1.select(0);
				m_comboHours2.select(0);
				for (int i = 0; i < m_comboHours1.getItemCount(); i++) {
					if (schedule.getHour() == null) {
						break;
					}
					String hours = format.format(schedule.getHour());
					if (hours.equals(m_comboHours1.getItem(i))) {
						m_comboHours1.select(i);
						break;
					}
				}
				//分を設定
				m_comboMinutes1.select(0);
				m_comboMinutes2.select(0);
				for (int i = 0; i < m_comboMinutes1.getItemCount(); i++) {
					if (schedule.getMinute() == null) {
						break;
					}
					String minutes = format.format(schedule.getMinute());
					if (minutes.equals(m_comboMinutes1.getItem(i))) {
						m_comboMinutes1.select(i);
						break;
					}
				}
			} else {
				//曜日を設定
				m_comboDayOfWeek.select(0);
				String dayOfWeek = DayOfWeekConstant.typeToString(schedule.getWeek());
				for (int i = 0; i < m_comboDayOfWeek.getItemCount(); i++) {
					if (dayOfWeek.equals(m_comboDayOfWeek.getItem(i))) {
						m_comboDayOfWeek.select(i);
						break;
					}
				}
				//時を設定
				m_comboHours1.select(0);
				m_comboHours2.select(0);
				for (int i = 0; i < m_comboHours2.getItemCount(); i++) {
					if (schedule.getHour() == null) {
						break;
					}
					String hours = format.format(schedule.getHour());
					if (hours.equals(m_comboHours2.getItem(i))) {
						m_comboHours2.select(i);
						break;
					}
				}
				//分を設定
				m_comboMinutes1.select(0);
				m_comboMinutes2.select(0);
				for (int i = 0; i < m_comboMinutes2.getItemCount(); i++) {
					if (schedule.getMinute() == null) {
						break;
					}
					String minutes = format.format(schedule.getMinute());
					if (minutes.equals(m_comboMinutes2.getItem(i))) {
						m_comboMinutes2.select(i);
						break;
					}
				}

			}

			//指定方式を設定
			if (MaintenanceScheduleResponse.TypeEnum.DAY.equals(schedule.getType())) {
				m_type1.setSelection(true);
				m_comboMonth.setEnabled(true);
				m_comboDay.setEnabled(true);
				m_comboHours1.setEnabled(true);
				m_comboMinutes1.setEnabled(true);
				m_comboDayOfWeek.setEnabled(false);
				m_comboHours2.setEnabled(false);
				m_comboMinutes2.setEnabled(false);
			} else if (MaintenanceScheduleResponse.TypeEnum.WEEK.equals(schedule.getType())) {
				m_type2.setSelection(true);
				m_comboMonth.setEnabled(false);
				m_comboDay.setEnabled(false);
				m_comboHours1.setEnabled(false);
				m_comboMinutes1.setEnabled(false);
				m_comboDayOfWeek.setEnabled(true);
				m_comboHours2.setEnabled(true);
				m_comboMinutes2.setEnabled(true);
			}
			this.update();
		}
	}

	/**
	 * ダイアログの情報からスケジュール情報を作成します。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.ScheduleTableDefine
	 */
	private ValidateResult createMaintenanceInfo() {
		ValidateResult result = null;

		if (maintenanceInfo == null) {
			maintenanceInfo = new MaintenanceInfoResponse();
		}

		//メンテナンスID取得
		if (m_textMaintenanceId.getText().length() > 0) {
			maintenanceInfo.setMaintenanceId(m_textMaintenanceId.getText());
		}

		//オーナーロールID
		if (m_ownerRoleId.getText().length() > 0) {
			maintenanceInfo.setOwnerRoleId(m_ownerRoleId.getText());
		}

		//説明取得
		maintenanceInfo.setDescription(m_textDescription.getText());

		//種別
		if(m_maintenance_type.getSelectionIndex() >= 0){
			maintenanceInfo.setTypeId(TypeIdEnum.fromValue(m_maintenance_type.getSelectionTypeId()));
		}
		//保存期間(日)となる期間を指定
		if(m_textDataRetentionPeriod.getText().length() > 0){
			try{
				int period = Integer.parseInt(m_textDataRetentionPeriod.getText());
				maintenanceInfo.setDataRetentionPeriod(period);
			} catch (NumberFormatException e) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.maintenance.17"));
				return result;
			}
		} else {
			maintenanceInfo.setDataRetentionPeriod(null);
		}

		//カレンダID
		if (m_calendarId.getText().length() > 0) {
			maintenanceInfo.setCalendarId(m_calendarId.getText());
		} else {
			maintenanceInfo.setCalendarId(null);
		}

		//条件設定
		MaintenanceScheduleResponse schedule = new MaintenanceScheduleResponse();
		Integer month = null;
		Integer day = null;
		Integer hours = null;
		Integer minutes = null;
		Integer week = null;
		if (m_type1.getSelection()) {
			//月を取得
			if (m_comboMonth.getText().length() > 0) {
				month = Integer.valueOf(m_comboMonth.getText());
			}
			//日を取得
			if (m_comboDay.getText().length() > 0) {
				day = Integer.valueOf(m_comboDay.getText());
			}
			//時を取得
			if (m_comboHours1.getText().length() > 0) {
				hours = Integer.valueOf(m_comboHours1.getText());
			}
			//分を取得
			if (m_comboMinutes1.getText().length() > 0) {
				minutes = Integer.valueOf(m_comboMinutes1.getText());
			}

			//スケジュール種別
			schedule.setType(MaintenanceScheduleResponse.TypeEnum.DAY);

		} else if (m_type2.getSelection()) {
			schedule.setType(MaintenanceScheduleResponse.TypeEnum.WEEK);

			//曜日を取得
			if (m_comboDayOfWeek.getText().length() > 0) {
				week = Integer.valueOf(DayOfWeekConstant
						.stringToType(m_comboDayOfWeek.getText()));
			}
			//時を取得
			if (m_comboHours2.getText().length() > 0) {
				hours = Integer.valueOf(m_comboHours2.getText());
			}
			//分を取得
			if (m_comboMinutes2.getText().length() > 0) {
				minutes = Integer.valueOf(m_comboMinutes2.getText());
			}
		}

		//日時を設定
		schedule.setMonth(month);
		schedule.setDay(day);
		schedule.setWeek(week);
		schedule.setHour(hours);
		schedule.setMinute(minutes);
		maintenanceInfo.setSchedule(schedule);

		//通知関連情報の設定
		//コンポジットから通知情報を取得します。
		List<NotifyRelationInfoResponse> notifyRelationInfoList = maintenanceInfo.getNotifyId();
		if (notifyRelationInfoList != null) {
			notifyRelationInfoList.clear();
		}
		if (this.notifyInfo.getNotify() != null) {
			for (NotifyRelationInfoResponse notify : this.notifyInfo.getNotify()) {
				NotifyRelationInfoResponse nottifyDto = new NotifyRelationInfoResponse();
				nottifyDto.setNotifyId(notify.getNotifyId());
				nottifyDto.setNotifyType(notify.getNotifyType());
				notifyRelationInfoList.add(nottifyDto);
			}
		}

		//アプリケーションを取得
		maintenanceInfo.setApplication(notifyInfo.getApplication());

		//有効/無効取得
		if (confirmValid.getSelection()) {
			maintenanceInfo.setValidFlg(true);
		} else {
			maintenanceInfo.setValidFlg(false);
		}

		return result;
	}

	protected void setInputData(MaintenanceInfoResponse info) {

		// オーナーロールID設定
		if (info != null && info.getOwnerRoleId() != null) {
			this.m_ownerRoleId.setText(info.getOwnerRoleId());
		}

		// 他CompositeへのオーナーロールIDの設定
		this.m_calendarId.createCalIdCombo(this.m_managerComposite.getText(), m_ownerRoleId.getText());
		this.notifyInfo.setOwnerRoleId(m_ownerRoleId.getText(), true);


		if(info != null){
			if (info.getMaintenanceId() != null) {
				this.maintenanceId = info.getMaintenanceId();
				this.m_textMaintenanceId.setText(this.maintenanceId);
			}

			if (info.getDescription() != null) {
				this.m_textDescription.setText(info.getDescription());
			}

			if (info.getDataRetentionPeriod() != null) {
				this.m_textDataRetentionPeriod.setText(info.getDataRetentionPeriod().toString());
			}

			if (info.getCalendarId() != null) {
				this.m_calendarId.setText(info.getCalendarId());
			}

			if (info.getTypeId() != null) {
				this.m_maintenance_type.setText(m_maintenance_type
						.getMaintenanceTypeName(
								this.m_managerComposite.getText(),
								info.getTypeId().getValue()));
			}

			//通知情報の設定
			if(info.getNotifyId() != null) {
				List<NotifyRelationInfoResponse> notifyIdList = new ArrayList<>();
				for (NotifyRelationInfoResponse notify : info.getNotifyId()) {
					NotifyRelationInfoResponse notifyInfo = new NotifyRelationInfoResponse();
					notifyInfo.setNotifyId(notify.getNotifyId());
					notifyInfo.setNotifyType(notify.getNotifyType());
					notifyIdList.add(notifyInfo);
				}
				this.notifyInfo.setNotify(notifyIdList);
			}


			if (info.getApplication() != null) {
				this.notifyInfo.setApplication(info.getApplication());
				this.notifyInfo.update();
			}

			if (info.getValidFlg() != null) {
				this.confirmValid.setSelection(info.getValidFlg());
			}
		}

		// 各項目が必須項目であることを明示
		this.update();

	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 入力値を保持したプロパティを返します。<BR>
	 * プロパティオブジェクトのコピーを返します。
	 *
	 * @return プロパティ
	 *
	 * @see com.clustercontrol.util.PropertyUtil#copy(Property)
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		result = createMaintenanceInfo();
		if (result != null) {
			return result;
		}

		return null;
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

		if(this.maintenanceInfo != null){
			if(this.mode == PropertyDefineConstant.MODE_ADD){
				// 作成の場合
				String managerName = this.m_managerComposite.getText();
				String[] args = { maintenanceInfo.getMaintenanceId(), managerName };
				try {
					MaintenanceRestClientWrapper wrapper = MaintenanceRestClientWrapper
							.getWrapper(managerName);
					AddMaintenanceRequest add = new AddMaintenanceRequest();
					RestClientBeanUtil.convertBean(maintenanceInfo, add);
					add.setTypeId(AddMaintenanceRequest.TypeIdEnum.fromValue(maintenanceInfo.getTypeId().getValue()));
					add.getSchedule().setType(MaintenanceScheduleRequest.TypeEnum.fromValue(maintenanceInfo.getSchedule().getType().getValue()));
					wrapper.addMaintenance(add);

					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.maintenance.1", args));

					result = true;

				} catch (MaintenanceDuplicate e) {
					// メンテナンスIDが重複している場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.maintenance.11", args));
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						MessageDialog.openInformation(null, Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.maintenance.2", args) + errMessage);
				}

			} else if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
				// 変更の場合
				result = new ModifyMaintenance().modify(this.m_managerComposite.getText(), maintenanceInfo);
			}
		}

		return result;
	}
}
