/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.dialog;

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.calendar.composite.CalendarIdListComposite;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.composite.NotifyIdListComposite;
import com.clustercontrol.reporting.action.GetReporting;
import com.clustercontrol.reporting.action.ReportingRunner;
import com.clustercontrol.reporting.composite.OutputPeriodComposite;
import com.clustercontrol.reporting.composite.ReportFormatComposite;
import com.clustercontrol.reporting.composite.TemplateSetIdListComposite;
import com.clustercontrol.reporting.util.ReportingEndpointWrapper;
import com.clustercontrol.reporting.util.ReportingUtil;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.notify.NotifyRelationInfo;
import com.clustercontrol.ws.reporting.InvalidRole_Exception;
import com.clustercontrol.ws.reporting.ReportingInfo;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * 選択したレポーティングスケジュールを即時実行するためのダイアログクラス。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ReportingRunDialog extends CommonDialog {

	public static final int WIDTH_TITLE = 4;
	public static final int WIDTH_TEXT = 8;

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
	private String m_facilityId = null;

	/** 未登録ノード スコープを表示するかフラグ */
	private boolean m_unregistered = false;

	/** 出力期間 */
	private OutputPeriodComposite m_outputPeriod;

	/** カレンダID用テキスト */
	private CalendarIdListComposite m_calendarId = null;

	/** テンプレートセット */
	private TemplateSetIdListComposite m_templateSetIdList = null;

	/** 出力設定 */
	private ReportFormatComposite m_reportFormat = null;

	/** 通知情報 */
	private NotifyIdListComposite m_notifyIdList = null;

	/** 上記の通知設定で通知する */
	private Button m_confirmNotify = null;

	/** レポーティング情報 */
	private ReportingInfo m_reportingInfo = null;

	/** ダイアログ表示時の処理タイプ */
	private int m_mode;

	private ReportingRunner m_runner;

	private Composite m_compositeOutputFile;
	private Text m_folderNameText = null;
	private String m_folderName = null;

	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	private String m_managerName = null;
	
	
	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *            親シェル
	 */
	public ReportingRunDialog(Shell parent) {
		super(parent);
		this.m_mode = PropertyDefineConstant.MODE_ADD;
	}

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *            親シェル
	 * @param schedulId
	 *            スケジュールID
	 */
	public ReportingRunDialog(Shell parent, String managerName, String scheduleId) {
		super(parent);
		this.m_managerName = managerName;
		this.m_scheduleId = scheduleId;
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
		shell.setText(Messages.getString("dialog.reporting.running"));

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
		this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, false);
		WidgetTestUtil.setTestId(this, "managerComposite", this.m_managerComposite);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);

		if(this.m_managerName != null) {
			this.m_managerComposite.setText(this.m_managerName);
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
		// 変更不可
		this.m_textScheduleId.setEnabled(false);

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
		// 変更不可
		this.m_textDescription.setEnabled(false);

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
							m_calendarId.createCalIdCombo(m_managerComposite.getText(), m_ownerRoleId.getText());
							m_notifyIdList.setOwnerRoleId(m_ownerRoleId.getText(), true);
							m_facilityId = "";
							m_textScope.setText(m_facilityId);
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

		// 変更不可
		this.m_ownerRoleId.setEnabled(false);

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
		// 変更不可
		this.m_textScope.setEnabled(false);

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
					FacilityTreeItem item = dialog.getSelectItem();
					FacilityInfo info = item.getData();
					m_facilityId = info.getFacilityId();
					if (info.getFacilityType() == FacilityConstant.TYPE_NODE) {
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
		// 変更不可
		this.m_buttonScope.setEnabled(false);

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
		// 変更不可
		this.m_calendarId.setEnabled(false);
		
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
		 * テンプレートセット
		 */
		this.m_templateSetIdList = new TemplateSetIdListComposite(parent, SWT.NONE, this.m_managerComposite.getText(), true);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_templateSetIdList.setLayoutData(gridData);
		// 変更不可
		this.m_templateSetIdList.setEnabled(false);

		/*
		 * 書式設定
		 */
		this.m_reportFormat = new ReportFormatComposite(parent, SWT.NONE, this.m_managerComposite.getText());
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_reportFormat.setLayoutData(gridData);
		// 変更不可
		this.m_reportFormat.setEnabled(false);

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
		this.m_notifyIdList = new NotifyIdListComposite(groupNotifyAttribute,
				SWT.NONE, true);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_notifyIdList.setLayoutData(gridData);
		// 変更不可
		m_notifyIdList.setEnabled(false);

		/*
		 * 通知の有無
		 */
		this.m_confirmNotify = new Button(parent, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_confirmNotify.setLayoutData(gridData);
		this.m_confirmNotify.setText(Messages
				.getString("notify.after.reporting"));
		this.m_confirmNotify.setSelection(false);

		// 出力ファイル先指定BOX
		createCompositeOutpuFile(parent);

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
					.getReportingInfo(this.m_managerComposite.getText(), m_scheduleId);
		}
	}

	/**
	 * 出力するフォルダ名を決めるためのコンポジットを生成します。
	 */
	private void createCompositeOutpuFile(Composite parent) {

		m_compositeOutputFile = new Composite(parent, SWT.NULL);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 15;
		m_compositeOutputFile.setLayoutData(gridData);
		m_compositeOutputFile.setLayout(new FormLayout());

		// ラベルフィールド
		Label label = new Label(m_compositeOutputFile, SWT.NULL);
		label.setText(Messages.getString("folder.name") + " : "); // "ファイル名"
		FormData labelData = new FormData();
		labelData.top = new FormAttachment(0, 0); // ウィンドウの上側にはりつく
		labelData.left = new FormAttachment(0, 0); // ウィンドウの左側にはりつく
		label.setLayoutData(labelData);

		// ボタン
		Button button = new Button(m_compositeOutputFile, SWT.NULL);
		button.setText(Messages.getString("refer")); // "参照"
		FormData buttonData = new FormData();
		buttonData.top = new FormAttachment(0, 0); // ウィンドウの上側にはりつく
		buttonData.right = new FormAttachment(100, 0); // ウィンドウの右側にはりつく
		button.setLayoutData(buttonData);

		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// 出力先ファイルを選択するダイアログを開く
				DirectoryDialog saveDialog = new DirectoryDialog(getShell());
				String folderName = saveDialog.open();
				if (folderName != null) {
					m_folderNameText.setText(folderName);
				}
			}
		});

		// テキストフィールド
		m_folderNameText = new Text(m_compositeOutputFile, SWT.BORDER);

		FormData textData = new FormData();
		textData.top = new FormAttachment(0, 0); // ウィンドウの上側にはりつく
		textData.left = new FormAttachment(label, 0); // ラベルの右側にはりつく
		textData.right = new FormAttachment(button, 0); // ボタンの左側にはりつく
		m_folderNameText.setLayoutData(textData);
		m_folderNameText.addVerifyListener(new StringVerifyListener(
				DataRangeConstant.VARCHAR_4096));
		m_folderNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
	}

	/**
	 * 更新処理
	 */
	private void update() {
		// 各項目が必須項目であることを明示
		if ("".equals(this.m_folderNameText.getText())) {
			this.m_folderNameText
					.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_folderNameText
					.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		m_outputPeriod.update();
	}

	/**
	 * ダイアログにスケジュール情報を反映します。
	 */
	private void reflectReportingInfo() {

		// レポーティング情報を this.reportingInfo とは別のインスタンスとして取得
		ReportingInfo info = new GetReporting().getReportingInfo(this.m_managerComposite.getText(), m_scheduleId);

		if (info != null) {

			// オーナーロールID設定
			if (info.getOwnerRoleId() != null) {
				this.m_ownerRoleId.setText(info.getOwnerRoleId());
			}

			// 他CompositeへのオーナーロールIDの設定
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
			if (info.getScopeText() != null) {
				this.m_textScope.setText(HinemosMessage.replace(info.getScopeText()));
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
			if (info.getNotifyId() != null) {
				this.m_notifyIdList.setNotify(info.getNotifyId());
			}
			
			// テンプレートセット
			if (info.getTemplateSetId() != null) {
				this.m_templateSetIdList.setText(info.getTemplateSetId());
			}
		}

		// 出力期間
		m_outputPeriod.reflectReportingInfo(info);
		// 書式
		m_reportFormat.reflectReportingInfo(info);

		this.update();
	}

	/**
	 * ダイアログの情報から即時実行用のレポーティング情報を作成します。
	 * 
	 * 出力期間はダイアログの情報を用います。通知グループは通知の有無のチェックボックスが
	 * 有効のときのみダイアログの情報を用います。その他の要素はマネージャ側で用いられない ので値の設定は行いません。
	 * 
	 * @return 入力値の検証結果
	 * 
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	private ValidateResult createReportingInfoForRunning() {
		ValidateResult result = null;

		if (m_reportingInfo == null) {
			m_reportingInfo = new ReportingInfo();
		}

		// 出力期間
		m_reportingInfo.setOutputPeriodType(m_outputPeriod.getOutputPeriodType());
		m_reportingInfo.setOutputPeriodBefore(m_outputPeriod
				.getOutputPeriodBefore());
		m_reportingInfo.setOutputPeriodFor(m_outputPeriod.getOutputPeriodFor());

		// 通知を行う場合
		if (m_confirmNotify.getSelection()) {
			m_reportingInfo.setNotifyGroupId(ReportingUtil
					.createNotifyGroupIdReporting(m_reportingInfo.getReportScheduleId()));
			// コンポジット中の通知情報に通知グループIDを対応させる
			this.m_notifyIdList
					.setNotifyGroupId(m_reportingInfo.getNotifyGroupId());
			// 通知情報のリストを更新する
			List<NotifyRelationInfo> notifyRelationInfoList = m_reportingInfo
					.getNotifyId();
			notifyRelationInfoList.clear();
			if (this.m_notifyIdList.getNotify() != null) {
				notifyRelationInfoList.addAll(this.m_notifyIdList.getNotify());
			}

			// 通知を行わない場合、通知グループIDには null を入れておく。
		} else {
			m_reportingInfo.setNotifyGroupId(null);
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

		// DataWriterへの入力
		this.m_folderName = this.m_folderNameText.getText();

		result = createReportingInfoForRunning();
		if (result != null) {
			return result;
		}

		// 入力チェック
		if (this.m_folderName == null || m_folderName.equals("")) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1")); // "入力エラー"
			result.setMessage(Messages
					.getString("message.reporting.29")); // "フォルダ名を指定してください"
			return result;
		} else if (!(new File(this.m_folderName).exists())) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1")); // "入力エラー"
			result.setMessage(Messages
					.getString("message.reporting.28")); // "指定されたフィルダが存在しません"
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
		boolean ret = false;

		// 実行準備
		m_runner = new ReportingRunner(
				this.m_scheduleId, 
				this.m_folderName,
				this.m_reportingInfo, 
				ReportingEndpointWrapper.getWrapper(this.m_managerComposite.getText()));

		try {
			// レポート作成処理を開始
			List<String> downloadFileList = m_runner.create(this.m_managerComposite.getText());

			if (downloadFileList == null || downloadFileList.size() == 0) {
				MessageDialog.openError(null, Messages.getString("failed"),
						Messages.getString("message.reporting.22"));
			} else {
				MessageDialog.openInformation(getShell(),
						Messages.getString("report.running"),			  // レポーティング即時実行
						Messages.getString("report.will.create")); // レポートファイルを作成します。
																				  // この処理には数分かかります。
				Thread thread = new Thread(m_runner);
				// Client 終了時にこのスレッドの終了を待たない
				thread.setDaemon(true);
				// ダウンロード処理の開始
				thread.start();

				ret = true;
			}
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole_Exception) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} else {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.reporting.22") + errMessage);
		}

		return ret;
	}
}