/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.dialog;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.AddReportingScheduleRequest;
import org.openapitools.client.model.AddReportingScheduleRequest.OutputPeriodTypeEnum;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;
import org.openapitools.client.model.ModifyReportingScheduleRequest;
import org.openapitools.client.model.NotifyRelationInfoResponse;
import org.openapitools.client.model.ReportingScheduleInfoResponse;
import org.openapitools.client.model.ReportingScheduleResponse;
import org.openapitools.client.model.ReportingScheduleResponse.OutputTypeEnum;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.calendar.composite.CalendarIdListComposite;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.notify.composite.NotifyIdListComposite;
import com.clustercontrol.reporting.action.AddReporting;
import com.clustercontrol.reporting.action.GetReporting;
import com.clustercontrol.reporting.action.ModifyReporting;
import com.clustercontrol.reporting.composite.OutputPeriodComposite;
import com.clustercontrol.reporting.composite.ReportFormatComposite;
import com.clustercontrol.reporting.composite.ScheduleComposite;
import com.clustercontrol.reporting.composite.TemplateSetIdListComposite;
import com.clustercontrol.reporting.util.ReportingRestClientWrapper;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * レポーティング[スケジュールの作成・変更]ダイアログクラスです。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ReportingScheduleDialog extends CommonDialog {

	public static final int WIDTH_TITLE = 4;
	public static final int WIDTH_TEXT = 8;

	// ログ
	private static Log m_log = LogFactory.getLog( ReportingScheduleDialog.class );
	
	/** スケジュールID */
	private String m_scheduleId = null;

	/** スケジュールID用テキスト */
	private Text m_textScheduleId = null;

	/** 説明 */
	private Text m_textDescription = null;

	/** オーナーロールID用テキスト */
	private RoleIdListComposite m_ownerRoleId = null;

	/** スコープ テキストボックス。 */
	private Text m_textScope = null;

	/** 参照 ボタン。 */
	private Button m_buttonScope = null;

	/** 選択されたスコープのファシリティID。 */
	private String m_facilityId = "";

	/** 未登録ノード スコープを表示するかフラグ */
	private boolean m_unregistered = false;

	/** 出力期間 */
	private OutputPeriodComposite m_outputPeriod;

	/** カレンダID用テキスト */
	private CalendarIdListComposite m_calendarId = null;

	/** テンプレートセットID */
	private TemplateSetIdListComposite m_templateSetIdList = null;

	/** 書式設定 */
	private ReportFormatComposite m_reportFormat = null;

	/** スケジュール */
	private ScheduleComposite m_schedule;

	/** 通知情報 */
	private NotifyIdListComposite m_notifyIdList = null;

	/** この設定を有効にする */
	private Button m_confirmValid = null;

	/** レポーティング情報 */
	private ReportingScheduleResponse m_reportingInfo = null;

	/** ダイアログ表示時の処理タイプ */
	private int m_mode;

	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	private String m_managerName = null;
	
	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *            親シェル
	 */
	public ReportingScheduleDialog(Shell parent) {
		super(parent);
		this.m_mode = PropertyDefineConstant.MODE_ADD;
	}

	public ReportingScheduleDialog(Shell parent, String managerName, String scheduleId, int mode) {
		super(parent);
		this.m_managerName = managerName;
		this.m_scheduleId = scheduleId;
		this.m_mode = mode;
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

		// タイトル
		shell.setText(Messages
				.getString("dialog.reporting.schedule.create.modify"));

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
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = WIDTH_TITLE;
		labelManager.setLayoutData(gridData);
		labelManager.setText(Messages.getString("facility.manager") + " : ");
		if(this.m_mode == PropertyDefineConstant.MODE_MODIFY
				|| this.m_mode == PropertyDefineConstant.MODE_SHOW){
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, true);
		}
		WidgetTestUtil.setTestId(this, "managerComposite", this.m_managerComposite);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);

		if(this.m_managerName != null) {
			this.m_managerComposite.setText(this.m_managerName);
		}
		if(this.m_mode != PropertyDefineConstant.MODE_MODIFY) {
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					m_facilityId = "";
					m_textScope.setText(m_facilityId);
					m_ownerRoleId.createRoleIdList(managerName);
					m_calendarId.createCalIdCombo(managerName, m_ownerRoleId.getText());
					m_notifyIdList.setManagerName(managerName);
					m_templateSetIdList.setManagerName(managerName);
					m_templateSetIdList.update(m_ownerRoleId.getText());
					m_reportFormat.setManagerName(managerName);
				}
			});
		}
		
		/*
		 * スケジュールID
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("schedule.id") + " : ");
		// テキスト
		this.m_textScheduleId = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textScheduleId.setLayoutData(gridData);
		if (this.m_mode == PropertyDefineConstant.MODE_MODIFY
				|| this.m_mode == PropertyDefineConstant.MODE_SHOW) {
			this.m_textScheduleId.setEnabled(false);
		}
		this.m_textScheduleId.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 説明
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("description") + " : ");
		// テキスト
		this.m_textDescription = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textDescription.setLayoutData(gridData);
		this.m_textDescription.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

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
		if (this.m_mode == PropertyDefineConstant.MODE_ADD
				|| this.m_mode == PropertyDefineConstant.MODE_COPY) {
			this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE,
					this.m_managerComposite.getText(), true, Mode.OWNER_ROLE);
			this.m_ownerRoleId.getComboRoleId().addSelectionListener(
					new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							// オーナーロール変更時の動作
							m_calendarId.createCalIdCombo(m_managerComposite.getText(), m_ownerRoleId.getText());
							m_notifyIdList.setOwnerRoleId(m_ownerRoleId.getText(), true);
							m_facilityId = "";
							m_textScope.setText(m_facilityId);
							
							m_templateSetIdList.update(m_ownerRoleId.getText());
						}
					});
		} else {
			this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE,
					this.m_managerComposite.getText(), false, Mode.OWNER_ROLE);
		}
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_ownerRoleId.setLayoutData(gridData);

		// 空白
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * スコープ
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("scope") + " : ");
		// テキスト
		this.m_textScope = new Text(parent, SWT.BORDER | SWT.LEFT
				| SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textScope.setLayoutData(gridData);
		this.m_textScope.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 参照ボタン
		m_buttonScope = new Button(parent, SWT.NONE);
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
				Shell shell = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell();

				ScopeTreeDialog dialog = new ScopeTreeDialog(shell,
						m_managerComposite.getText(), m_ownerRoleId.getText(), false, m_unregistered);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse item = dialog.getSelectItem();
					FacilityInfoResponse info = item.getData();
					m_facilityId = info.getFacilityId();
					if (info.getFacilityType() == FacilityTypeEnum.NODE) {
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

		// 空白
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * カレンダ
		 */
		this.m_calendarId = new CalendarIdListComposite(parent, SWT.NONE, true);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_calendarId.setLayoutData(gridData);

		/*
		 * 出力期間
		 */
		this.m_outputPeriod = new OutputPeriodComposite(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_outputPeriod.setLayoutData(gridData);

		/*
		 * 出力内容
		 */
		this.m_templateSetIdList = new TemplateSetIdListComposite(parent, SWT.NONE, this.m_managerComposite.getText(), true);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_templateSetIdList.setLayoutData(gridData);

		/*
		 * 出力設定
		 */
		this.m_reportFormat = new ReportFormatComposite(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_reportFormat.setLayoutData(gridData);

		/*
		 * スケジュール
		 */
		this.m_schedule = new ScheduleComposite(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_schedule.setLayoutData(gridData);

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
		this.m_notifyIdList = new NotifyIdListComposite(groupNotifyAttribute, SWT.NONE, true);
		this.m_notifyIdList.setManagerName(this.m_managerComposite.getText());
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_notifyIdList.setLayoutData(gridData);

		/*
		 * 有効／無効
		 */
		this.m_confirmValid = new Button(parent, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.m_confirmValid.setLayoutData(gridData);
		this.m_confirmValid
				.setText(Messages.getString("setting.valid.confirmed"));
		this.m_confirmValid.setSelection(true);

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(550, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		// レポーティング情報の反映
		this.reflectReportingInfo();

		// レポーティング情報の生成
		if (m_scheduleId != null) {
			this.m_reportingInfo = new GetReporting()
					.getReportingSchedule(this.m_managerComposite.getText(), m_scheduleId);
		}

	}

	/**
	 * 更新処理
	 */
	private void update() {
		// 各項目が必須項目であることを明示
		if (this.m_textScheduleId.getEnabled()
				&& "".equals(this.m_textScheduleId.getText())) {
			this.m_textScheduleId
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_textScheduleId
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if ("".equals(this.m_textScope.getText())) {
			this.m_textScope
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_textScope
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		m_schedule.update();
		m_outputPeriod.update();
		m_reportFormat.update();
		m_templateSetIdList.update();
	}

	/**
	 * ダイアログにスケジュール情報を反映します。
	 */
	private void reflectReportingInfo() {

		// 新規作成の場合はこちらのルートを通る。
		if (m_scheduleId == null) {

			m_outputPeriod.setInitialValue();
			m_schedule.setInitialValue();

			// 他Compositeへのマネージャ名、オーナーロールIDの設定
			this.m_calendarId.createCalIdCombo(this.m_managerComposite.getText(), this.m_ownerRoleId.getText());
			this.m_notifyIdList.setOwnerRoleId(this.m_ownerRoleId.getText(), true);
			this.m_templateSetIdList.setManagerName(this.m_managerComposite.getText());
			this.m_templateSetIdList.update(this.m_ownerRoleId.getText());
			this.m_reportFormat.createOutputTypeStrList(this.m_managerComposite.getText());
		} else {
			// レポーティング情報を this.reportingInfo とは別のインスタンスとして取得
			ReportingScheduleResponse info = new GetReporting()
					.getReportingSchedule(this.m_managerComposite.getText(), this.m_scheduleId);

			if (info != null) {

				// オーナーロールID設定
				if (info.getOwnerRoleId() != null) {
					this.m_ownerRoleId.setText(info.getOwnerRoleId());
				}
				// 他Compositeへのマネージャ名、オーナーロールIDの設定
				this.m_calendarId.createCalIdCombo(this.m_managerComposite.getText(), this.m_ownerRoleId.getText());
				this.m_notifyIdList.setOwnerRoleId(this.m_ownerRoleId.getText(), true);
				this.m_templateSetIdList.setManagerName(this.m_managerComposite.getText());
				this.m_templateSetIdList.update(this.m_ownerRoleId.getText());
				this.m_reportFormat.createOutputTypeStrList(this.m_managerComposite.getText());

				// スケジュールID
				if (info.getReportScheduleId() != null) {
					this.m_scheduleId = info.getReportScheduleId();
					this.m_textScheduleId.setText(this.m_scheduleId);
				}

				// 説明
				if (info.getDescription() != null) {
					this.m_textDescription.setText(info.getDescription());
				}

				// スコープ
				if (info.getScope() != null) {
					this.m_textScope.setText(HinemosMessage.replace(info.getScope()));
				}

				// ファシリティ
				if (info.getFacilityId() != null) {
					this.m_facilityId = info.getFacilityId();
				}

				// カレンダ
				if (info.getCalendarId() != null) {
					this.m_calendarId.setText(info.getCalendarId());
				}

				// 通知情報の設定
				if (info.getNotifyRelationList() != null) {
					List<NotifyRelationInfoResponse> relationInfoList = new ArrayList<>();
					if (info.getNotifyRelationList() != null && info.getNotifyRelationList().size() > 0) {
						for (NotifyRelationInfoResponse infoRes : info.getNotifyRelationList()){
							relationInfoList.add(infoRes);
						}
					}
					this.m_notifyIdList.setNotify(relationInfoList);
				}

				// 有効/無効
				if (info.getValidFlg() != null) {
					this.m_confirmValid.setSelection(info.getValidFlg().booleanValue());
				}
				
				// テンプレートセットID
				if (info.getTemplateSetId() != null) {
					this.m_templateSetIdList.setText(info.getTemplateSetId());
				}
			}

			// 出力期間
			m_outputPeriod.reflectReportingSchedule(info);
			// 出力設定
			m_reportFormat.reflectReportingSchedule(info);
			// スケジュール
			m_schedule.reflectReportingInfo(info);
		}

		this.update();
	}

	/**
	 * ダイアログの情報からレポーティング情報を作成します。
	 * 
	 * @return 入力値の検証結果
	 * 
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	private ValidateResult createReportingInfo() {
		ValidateResult result = null;

		// レポーティング情報が既に存在する場合は、作成者と作成日を取得する
		String regDate = null;
		String regUser = null;
		if (m_reportingInfo != null) {
			regDate = m_reportingInfo.getRegDate();
			regUser = m_reportingInfo.getRegUser();
		}
		m_reportingInfo = new ReportingScheduleResponse();

		// スケジュールID取得
		if (m_textScheduleId.getText().length() > 0) {
			m_reportingInfo.setReportScheduleId(m_textScheduleId.getText());
		}

		// オーナーロールID
		if (m_ownerRoleId.getText().length() > 0) {
			m_reportingInfo.setOwnerRoleId(m_ownerRoleId.getText());
		}

		// スコープ
		if (m_facilityId.length() > 0) {
			m_reportingInfo.setFacilityId(m_facilityId);
		}

		// 説明
		if (m_textDescription.getText().length() > 0) {
			m_reportingInfo.setDescription(m_textDescription.getText());
		} else {
			m_reportingInfo.setDescription("");
		}

		// 出力期間
		m_reportingInfo.setOutputPeriodType(m_outputPeriod.getOutputPeriodType());
		m_reportingInfo.setOutputPeriodBefore(m_outputPeriod
				.getOutputPeriodBefore());
		m_reportingInfo.setOutputPeriodFor(m_outputPeriod.getOutputPeriodFor());

		// カレンダID
		if (m_calendarId.getText().length() > 0) {
			m_reportingInfo.setCalendarId(m_calendarId.getText());
		} else {
			m_reportingInfo.setCalendarId(null);
		}

		// テンプレートセットID
		m_reportingInfo.setTemplateSetId(m_templateSetIdList.getText());

		// 出力設定
		m_reportingInfo.setReportTitle(m_reportFormat.getReportTitle());
		m_reportingInfo.setLogoValidFlg(m_reportFormat.getLogoValidFlg());
		m_reportingInfo.setLogoFilename(m_reportFormat.getLogoFilename());
		m_reportingInfo.setPageValidFlg(m_reportFormat.getPageValidFlg());
		
		try 
		{
			m_reportingInfo.setOutputType(OutputTypeEnum.fromValue(m_reportFormat.getOutputTypeStr()));
			
		} catch (Exception e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			m_log.warn("update() createReportingInfo, " + errMessage, e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
		}

		// スケジュール設定

		ReportingScheduleInfoResponse schedule = new ReportingScheduleInfoResponse();
		schedule.setScheduleType(m_schedule.getType());
		schedule.setDay(m_schedule.getDay());
		schedule.setWeek(m_schedule.getWeek());
		schedule.setHour(m_schedule.getHour());
		schedule.setMinute(m_schedule.getMinute());
		m_reportingInfo.setSchedule(schedule);

		// 通知関連情報の設定
		// 通知情報のリストを更新する
		if (m_reportingInfo.getNotifyRelationList() == null) {
			m_reportingInfo.setNotifyRelationList(new ArrayList<NotifyRelationInfoResponse>());
		}
		List<NotifyRelationInfoResponse> notifyRelationInfoList = m_reportingInfo.getNotifyRelationList();
		
		notifyRelationInfoList.clear();
		if (this.m_notifyIdList.getNotify() != null) {
			List<NotifyRelationInfoResponse> relationInfoResList = new ArrayList<>();
			try {
				if (this.m_notifyIdList.getNotify() != null && this.m_notifyIdList.getNotify().size() > 0) {
					for (NotifyRelationInfoResponse info : this.m_notifyIdList.getNotify()){
						NotifyRelationInfoResponse relationInfoRes = new NotifyRelationInfoResponse();
						RestClientBeanUtil.convertBean(info, relationInfoRes);
						relationInfoResList.add(relationInfoRes);
					}
				}
			} catch (HinemosUnknown e) {
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("createReportingInfo(), " + errMessage, e);
				MessageDialog.openError(null,
						Messages.getString("failed"),
						Messages.getString("message.accesscontrol.23")
						+ ", " + errMessage);
			}
			notifyRelationInfoList.addAll(relationInfoResList);
		}

		// 有効/無効取得
		if (m_confirmValid.getSelection()) {
			m_reportingInfo.setValidFlg(true);
		} else {
			m_reportingInfo.setValidFlg(false);
		}

		// 既存の作成日を設定する。
		if (regDate != null) {
			m_reportingInfo.setRegDate(regDate);
		}
		// 既存の作成者を取得する
		if (regUser != null) {
			m_reportingInfo.setRegUser(regUser);
		}
		return result;
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
	 * 入力値チェックをします。
	 * 
	 * @return 検証結果
	 * 
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		result = validateEndpoint(this.m_managerComposite.getText());
		if( result != null ){
			return result; 
		}

		result = createReportingInfo();
		if (result != null) {
			return result;
		}

		// 入力チェック
		if (this.m_facilityId == null || m_facilityId.equals("")) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1")); // "入力エラー"
			result.setMessage(Messages.getString("message.hinemos.3")); // "スコープを指定してください"
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
	@SuppressWarnings("unused")
	@Override
	protected boolean action() {
		boolean result = false;
		createReportingInfo();
		
		AddReportingScheduleRequest addInfoReq = new AddReportingScheduleRequest();
		try {
			RestClientBeanUtil.convertBean(this.m_reportingInfo, addInfoReq);
			addInfoReq.setOutputPeriodType(OutputPeriodTypeEnum.fromValue(this.m_reportingInfo.getOutputPeriodType().getValue()));
			addInfoReq.setOutputType(org.openapitools.client.model.AddReportingScheduleRequest.OutputTypeEnum.fromValue(this.m_reportingInfo.getOutputType().getValue()));
			addInfoReq.getSchedule().setScheduleType(org.openapitools.client.model.ReportingScheduleInfoRequest.ScheduleTypeEnum.fromValue(this.m_reportingInfo.getSchedule().getScheduleType().getValue()));
			String managerName = this.m_managerComposite.getText();
			// findbugs対応 不要なaddInfoReqのnullチェックを削除
			if (this.m_mode == PropertyDefineConstant.MODE_ADD) {
				// 作成の場合
				result = new AddReporting().add(managerName, addInfoReq);
			} else if (this.m_mode == PropertyDefineConstant.MODE_MODIFY) {
				// 変更の場合
				ModifyReportingScheduleRequest modifyInfoReq = new ModifyReportingScheduleRequest();
				RestClientBeanUtil.convertBean(this.m_reportingInfo, modifyInfoReq);
				modifyInfoReq.setOutputPeriodType(org.openapitools.client.model.ModifyReportingScheduleRequest.OutputPeriodTypeEnum.fromValue(this.m_reportingInfo.getOutputPeriodType().getValue()));
				modifyInfoReq.setOutputType(org.openapitools.client.model.ModifyReportingScheduleRequest.OutputTypeEnum.fromValue(this.m_reportingInfo.getOutputType().getValue()));
				modifyInfoReq.getSchedule().setScheduleType(org.openapitools.client.model.ReportingScheduleInfoRequest.ScheduleTypeEnum.fromValue(this.m_reportingInfo.getSchedule().getScheduleType().getValue()));
				result = new ModifyReporting().modify(managerName, m_textScheduleId.getText(), modifyInfoReq);
			} else if (this.m_mode == PropertyDefineConstant.MODE_COPY) {
				addInfoReq.setReportScheduleId(m_textScheduleId.getText());
				result = new AddReporting().add(managerName, addInfoReq);
			}
		} catch (HinemosUnknown e) {
			m_log.error("action() Failed to convert ReportingInfo");
		}

		return result;
	}

	@Override
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		return ReportingRestClientWrapper.getWrapper(managerName);
	}
	
}
