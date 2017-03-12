/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.dialog;

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
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.calendar.composite.CalendarIdListComposite;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetJobKick;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.composite.JobKickFileCheckComposite;
import com.clustercontrol.jobmanagement.composite.JobKickParamComposite;
import com.clustercontrol.jobmanagement.composite.JobKickScheduleComposite;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidSetting_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidUserPass_Exception;
import com.clustercontrol.ws.jobmanagement.JobFileCheck;
import com.clustercontrol.ws.jobmanagement.JobKick;
import com.clustercontrol.ws.jobmanagement.JobKickDuplicate_Exception;
import com.clustercontrol.ws.jobmanagement.JobSchedule;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブ[ジョブ実行契機の作成・変更]ダイアログクラスです。
 *
 * @version 5.1.0
 */
public class JobKickDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( JobKickDialog.class );

	/** ジョブ実行契機情報 */
	private JobKick m_jobKick;

	/** 
	 * ジョブ実行契機共通
	 */
	/** 実行契機ID用テキスト */
	private Text m_txtJobKickId = null;
	/** 実行契機名用テキスト */
	private Text m_txtJobKickName = null;
	/** ジョブID用テキスト */
	private Text m_txtJobId = null;
	/** ジョブ名用テキスト */
	private Text m_txtJobName = null;
	/** カレンダID用コンボボックス */
	private CalendarIdListComposite m_cmpCalendarId = null;
	/** ジョブ参照用ボタン */
	private Button m_btnJobSelect = null;
	/** 有効用ラジオボタン */
	private Button m_btnValid = null;
	/** 無効用ラジオボタン */
	private Button m_btnInvalid = null;
	/** オーナーロールID用テキスト */
	private RoleIdListComposite m_cmpOwnerRoleId = null;
	/** シェル */
	private Shell m_shell = null;
	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;
	/**
	 * 作成：MODE_ADD = 0;
	 * 変更：MODE_MODIFY = 1;
	 * 複製：MODE_COPY = 3;
	 * */
	private int m_mode = PropertyDefineConstant.MODE_ADD;

	/** 所属ジョブユニットのジョブID */
	private String m_jobunitId = null;

	/** 実行契機ID */
	private String m_jobkickId;

	/** マネージャ名 */
	private String m_managerName;

	/** 実行契機種別 */
	private int m_jobkickType = JobKickConstant.TYPE_FILECHECK;
	
	/** タブフォルダ */
	private TabFolder m_tabFolder = null;

	/** 詳細情報（Composite） */
	private Composite m_detailComposite = null;

	/** ランタイムジョブ変数情報（Composite） */
	private JobKickParamComposite m_jobKickParamComposite = null;

	/**
	 * コンストラクタ
	 * 変更時、コピー時
	 * @param parent
	 * @param jobkickId
	 */
	public JobKickDialog(Shell parent, String managerName, String jobkickId, int jobkickType, int mode){
		super(parent);
		this.m_managerName = managerName;
		this.m_jobkickId = jobkickId;
		this.m_jobkickType = jobkickType;
		this.m_mode = mode;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		this.m_shell = this.getShell();

		Label label = null;

		String title = "";
		if (this.m_jobkickType == JobKickConstant.TYPE_SCHEDULE) {
			title = Messages.getString("dialog.job.add.modify.schedule");
		} else if (this.m_jobkickType == JobKickConstant.TYPE_FILECHECK) {
			title = Messages.getString("dialog.job.add.modify.filecheck");
		} else if (this.m_jobkickType == JobKickConstant.TYPE_MANUAL) {
			title = Messages.getString("dialog.job.add.modify.manual");
		}
		parent.getShell().setText(title);

		/**
		 * レイアウト設定
		 * ダイアログ内のベースとなるレイアウトが全てを変更
		 */
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);

		// Composite
		Composite jobKickComposite = new Composite(parent, SWT.NONE);
		jobKickComposite.setLayout(new GridLayout(3, false));

		// マネージャ（ラベル）
		label = new Label(jobKickComposite, SWT.LEFT);
		label.setText(Messages.getString("facility.manager") + " : ");
		label.setLayoutData(new GridData(140, SizeConstant.SIZE_LABEL_HEIGHT));
		
		// マネージャ（テキスト）
		if(this.m_mode == PropertyDefineConstant.MODE_MODIFY){
			this.m_managerComposite = new ManagerListComposite(jobKickComposite, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(jobKickComposite, SWT.NONE, true);
		}
		WidgetTestUtil.setTestId(this, "m_managerComposite", this.m_managerComposite);
		this.m_managerComposite.setLayoutData(new GridData());
		((GridData)this.m_managerComposite.getLayoutData()).widthHint = 227;

		if(this.m_managerName != null) {
			this.m_managerComposite.setText(this.m_managerName);
		}
		if(this.m_mode != PropertyDefineConstant.MODE_MODIFY) {
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					m_cmpOwnerRoleId.createRoleIdList(managerName);
					if (m_jobkickType != JobKickConstant.TYPE_MANUAL) {
						updateCalendarId();
						if (m_jobkickType == JobKickConstant.TYPE_FILECHECK) {
							((JobKickFileCheckComposite)m_detailComposite).setOwnerRoleId(
									m_managerComposite.getText(), m_cmpOwnerRoleId.getText());
						}
					}
				}
			});
		}

		// dummy
		new Label(jobKickComposite, SWT.NONE);

		// 実行契機ID（ラベル）
		label = new Label(jobKickComposite, SWT.NONE);
		label.setText(Messages.getString("jobkick.id") + " : ");
		label.setLayoutData(new GridData(140, SizeConstant.SIZE_LABEL_HEIGHT));

		// 実行契機ID（テキスト）
		this.m_txtJobKickId = new Text(jobKickComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtJobKickId", this.m_txtJobKickId);
		this.m_txtJobKickId.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtJobKickId.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		this.m_txtJobKickId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// dummy
		new Label(jobKickComposite, SWT.NONE);

		// 実行契機名（ラベル）
		label = new Label(jobKickComposite, SWT.NONE);
		label.setText(Messages.getString("jobkick.name") + " : ");
		label.setLayoutData(new GridData(140, SizeConstant.SIZE_LABEL_HEIGHT));

		// 実行契機名（テキスト）
		this.m_txtJobKickName = new Text(jobKickComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtJobKickName", this.m_txtJobKickName);
		this.m_txtJobKickName.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtJobKickName.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		this.m_txtJobKickName.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// dummy
		new Label(jobKickComposite, SWT.NONE);

		// オーナーロール（ラベル）
		label = new Label(jobKickComposite, SWT.NONE);
		label.setText(Messages.getString("owner.role.id") + " : ");
		label.setLayoutData(new GridData(140,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// オーナーロールID（テキスト）
		if (this.m_mode == PropertyDefineConstant.MODE_ADD
				|| this.m_mode == PropertyDefineConstant.MODE_COPY) {
			this.m_cmpOwnerRoleId = new RoleIdListComposite(jobKickComposite
					, SWT.NONE, this.m_managerName, true, Mode.OWNER_ROLE);
			this.m_cmpOwnerRoleId.getComboRoleId().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (m_jobkickType != JobKickConstant.TYPE_MANUAL) {
						m_cmpCalendarId.createCalIdCombo(m_managerName, m_cmpOwnerRoleId.getText());
						m_txtJobId.setText("");
						m_txtJobName.setText("");
						setJobunitId(null);
						// ファイルチェック設定への反映
						if (m_jobkickType == JobKickConstant.TYPE_FILECHECK) {
							((JobKickFileCheckComposite)m_detailComposite).setOwnerRoleId(
									m_managerComposite.getText(), m_cmpOwnerRoleId.getText());
						}
					}
				}
			});
		} else {
			this.m_cmpOwnerRoleId = new RoleIdListComposite(jobKickComposite
					, SWT.NONE,this.m_managerName, false, Mode.OWNER_ROLE);
		}
		WidgetTestUtil.setTestId(this, "m_cmpOwnerRoleId", this.m_cmpOwnerRoleId);
		this.m_cmpOwnerRoleId.setLayoutData(new GridData());
		((GridData)this.m_cmpOwnerRoleId.getLayoutData()).widthHint = 227;

		// dummy
		new Label(jobKickComposite, SWT.NONE);

		// ジョブID（ラベル）
		label = new Label(jobKickComposite, SWT.LEFT);
		label.setText(Messages.getString("job.id") + " : ");
		label.setLayoutData(new GridData(140, SizeConstant.SIZE_LABEL_HEIGHT));

		// ジョブID（テキスト）
		this.m_txtJobId = new Text(jobKickComposite, SWT.READ_ONLY | SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtJobId", this.m_txtJobId);
		this.m_txtJobId.setLayoutData(new GridData(220,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtJobId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//ボタン
		this.m_btnJobSelect = new Button(jobKickComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnJobSelect", this.m_btnJobSelect);
		this.m_btnJobSelect.setText(Messages.getString("refer"));
		this.m_btnJobSelect.setLayoutData(new GridData(40,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnJobSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JobTreeDialog dialog = new JobTreeDialog(m_shell, m_managerComposite.getText(), m_cmpOwnerRoleId.getText(), true);
				if (dialog.open() == IDialogConstants.OK_ID) {
					JobTreeItem selectItem = dialog.getSelectItem().isEmpty() ? null : dialog.getSelectItem().get(0);
					if (selectItem != null && selectItem.getData().getType() != JobConstant.TYPE_COMPOSITE) {
						m_txtJobId.setText(selectItem.getData().getId());
						m_txtJobName.setText(selectItem.getData().getName());
						setJobunitId(selectItem.getData().getJobunitId());
					} else {
						m_txtJobId.setText("");
						m_txtJobName.setText("");
						setJobunitId(null);
					}
				}
			}
		});

		// ジョブ名（ラベル）
		label = new Label(jobKickComposite, SWT.LEFT);
		label.setText(Messages.getString("job.name") + " : ");
		label.setLayoutData(new GridData(140,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// ジョブ名（テキスト）
		this.m_txtJobName = new Text(jobKickComposite, SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "txtJobName", this.m_txtJobName);
		this.m_txtJobName.setLayoutData(new GridData(220,
				SizeConstant.SIZE_TEXT_HEIGHT));

		// dummy
		new Label(jobKickComposite, SWT.NONE);

		if (m_jobkickType != JobKickConstant.TYPE_MANUAL) {
			// カレンダID（ラベル）
			label = new Label(jobKickComposite, SWT.LEFT);
			label.setText(Messages.getString("calendar.id") + " : ");
			label.setLayoutData(new GridData(140,
					SizeConstant.SIZE_LABEL_HEIGHT));
	
			// カレンダID（コンボボックス）
			this.m_cmpCalendarId = new CalendarIdListComposite(jobKickComposite, SWT.NONE, false);
			WidgetTestUtil.setTestId(this, "cmpCalendarId", this.m_cmpCalendarId);
			this.m_cmpCalendarId.setLayoutData(new GridData());
			((GridData)this.m_cmpCalendarId.getLayoutData()).widthHint = 227;
	
			// dummy
			new Label(jobKickComposite, SWT.NONE);
		}

		// separator
		JobDialogUtil.getSeparator(parent);

		// タブ
		this.m_tabFolder = new TabFolder(parent, SWT.NONE);

		if (this.m_jobkickType == JobKickConstant.TYPE_SCHEDULE) {
			// ジョブスケジュール
			this.m_detailComposite = new JobKickScheduleComposite(this.m_tabFolder, SWT.NONE);
			TabItem scheduleTabItem = new TabItem(this.m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "scheduleTabItem", scheduleTabItem);
			scheduleTabItem.setText(Messages.getString("schedule.setting"));
			scheduleTabItem.setControl(this.m_detailComposite);
		} else if (this.m_jobkickType == JobKickConstant.TYPE_FILECHECK) {
			// ファイルチェック
			this.m_detailComposite = new JobKickFileCheckComposite(this.m_tabFolder, SWT.NONE);
			TabItem fileTabItem = new TabItem(this.m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "fileTabItem", fileTabItem);
			fileTabItem.setText(Messages.getString("file.check.setting"));
			fileTabItem.setControl(this.m_detailComposite);
		} else if (this.m_jobkickType == JobKickConstant.TYPE_MANUAL) {
			// マニュアル実行契機
		} else {
			// 該当なし
		}
		// ランタイムジョブ変数
		this.m_jobKickParamComposite = new JobKickParamComposite(this.m_tabFolder, SWT.NONE);
		TabItem paramTabItem = new TabItem(this.m_tabFolder, SWT.NONE);
		WidgetTestUtil.setTestId(this, "paramTabItem", paramTabItem);
		paramTabItem.setText(Messages.getString("job.parameter"));
		paramTabItem.setControl(this.m_jobKickParamComposite);

		if (this.m_jobkickType != JobKickConstant.TYPE_MANUAL) {
			// separator
			JobDialogUtil.getSeparator(parent);
	
			Group groupValidOrInvalid = new Group(parent, SWT.NONE);
			groupValidOrInvalid.setLayout(new RowLayout());
			groupValidOrInvalid.setText(Messages.getString("valid") + "/"
					+ Messages.getString("invalid"));
	
			//有効ボタン
			this.m_btnValid = new Button(groupValidOrInvalid, SWT.RADIO);
			WidgetTestUtil.setTestId(this, "m_btnValid", this.m_btnValid);
			this.m_btnValid.setText(ValidMessage.STRING_VALID);
			this.m_btnValid.setLayoutData(new RowData(200, SizeConstant.SIZE_BUTTON_HEIGHT));
			this.m_btnValid.setSelection(true);
	
			//無効ボタン
			this.m_btnInvalid = new Button(groupValidOrInvalid, SWT.RADIO);
			WidgetTestUtil.setTestId(this, "m_btnInvalid", this.m_btnInvalid);
			this.m_btnInvalid.setText(ValidMessage.STRING_INVALID);
			this.m_btnInvalid.setLayoutData(new RowData(200, SizeConstant.SIZE_BUTTON_HEIGHT));
		}

		// ダイアログを調整
		this.adjustDialog();

		//スケジュール情報反映
		if (this.m_jobkickType == JobKickConstant.TYPE_SCHEDULE) {
			reflectJobSchedule();
		} else if (this.m_jobkickType == JobKickConstant.TYPE_FILECHECK) {
			reflectJobFileCheck();
		} else if (this.m_jobkickType == JobKickConstant.TYPE_MANUAL) {
			reflectJobManual();
		}

		// 更新処理
		update();
	}

	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		this.m_shell.pack();
		this.m_shell.setSize(new Point(590, m_shell.getSize().y));

		// 画面中央に配置
		Display display = this.m_shell.getDisplay();
		this.m_shell.setLocation((display.getBounds().width - this.m_shell.getSize().x) / 2,
				(display.getBounds().height - this.m_shell.getSize().y) / 2);
	}

	/**
	 * ジョブ実行契機情報 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示
		if("".equals(this.m_txtJobKickId.getText())){
			this.m_txtJobKickId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtJobKickId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_txtJobKickName.getText())){
			this.m_txtJobKickName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtJobKickName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_txtJobId.getText())){
			this.m_txtJobId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtJobId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 詳細情報更新
		if (this.m_detailComposite != null) { 
			this.m_detailComposite.update();
		}
	}

	/**
	 * ダイアログにジョブ実行契機情報を反映します。
	 *
	 */
	private void reflectJobKick() {

		// オーナーロールID設定
		if (this.m_jobKick.getOwnerRoleId() != null) {
			this.m_cmpOwnerRoleId.setText(this.m_jobKick.getOwnerRoleId());
		}
		// 他CompositeへのオーナーロールIDの設定
		if (m_jobkickType != JobKickConstant.TYPE_MANUAL) {
			this.m_cmpCalendarId.createCalIdCombo(this.m_managerComposite.getText(), this.m_cmpOwnerRoleId.getText());
		}

		//実行契機IDを設定
		if(this.m_jobKick.getId() != null){
			this.m_txtJobKickId.setText(this.m_jobKick.getId());
			this.m_jobkickId = this.m_jobKick.getId();
			if(this.m_mode == PropertyDefineConstant.MODE_MODIFY){
				//ファイルチェック変更時、ファイルチェックIDは変更不可とする
				this.m_txtJobKickId.setEditable(false);
			}
		}
		//実行契機名を設定
		if(this.m_jobKick.getName() != null){
			this.m_txtJobKickName.setText(this.m_jobKick.getName());
		}
		//ジョブIDを設定
		if(this.m_jobKick.getJobId() != null){
			this.m_txtJobId.setText(this.m_jobKick.getJobId());
		}
		//ジョブ名を設定
		if(this.m_jobKick.getJobName() != null){
			this.m_txtJobName.setText(this.m_jobKick.getJobName());
		}
		//ジョブユニットIDを設定
		if(this.m_jobKick.getJobunitId() != null){
			String jobunitId = this.m_jobKick.getJobunitId();
			this.setJobunitId(jobunitId);
		}
		if (this.m_jobkickType != JobKickConstant.TYPE_MANUAL) {
			//カレンダIDを設定
			if (this.m_jobKick.getCalendarId() != null) {
				this.m_cmpCalendarId.setText(this.m_jobKick.getCalendarId());
			}
			
			if(this.m_jobKick.isValid() != null){
				//有効/無効設定
				Boolean effective = this.m_jobKick.isValid();
				this.m_btnValid.setSelection(effective.booleanValue());
				this.m_btnInvalid.setSelection(!effective.booleanValue());
			}
		}
		// ランタイムジョブ変数情報を設定
		this.m_jobKickParamComposite.setJobRuntimeParamList(
				this.m_jobKick.getJobRuntimeParamList());
	}

	/**
	 * ダイアログにスケジュール情報を反映します。
	 *
	 */
	private void reflectJobSchedule() {

		//マネージャより実行契機情報を取得する
		if(this.m_mode == PropertyDefineConstant.MODE_MODIFY
				|| this.m_mode == PropertyDefineConstant.MODE_COPY){
			this.m_jobKick = GetJobKick.getJobSchedule(this.m_managerName, this.m_jobkickId);
		}else {
			this.m_jobKick = new JobSchedule();
		}
		if (this.m_jobKick == null) {
			throw new InternalError("JobSchedule is null");
		}

		// 実行契機情報の反映
		reflectJobKick();

		// スケジュール情報の反映
		JobKickScheduleComposite jobKickScheduleComposite 
			= (JobKickScheduleComposite)this.m_detailComposite;
		JobSchedule jobSchedule = (JobSchedule)this.m_jobKick;
		jobKickScheduleComposite.setJobSchedule(
				jobSchedule.getScheduleType(),
				jobSchedule.getWeek(),
				jobSchedule.getHour(),
				jobSchedule.getMinute(),
				jobSchedule.getFromXminutes(),
				jobSchedule.getEveryXminutes());
	}

	/**
	 * ダイアログにファイルチェック情報を反映します。
	 *
	 */
	private void reflectJobFileCheck() {

		//マネージャより実行契機情報を取得する
		if(this.m_mode == PropertyDefineConstant.MODE_MODIFY
				|| this.m_mode == PropertyDefineConstant.MODE_COPY){
			this.m_jobKick = GetJobKick.getJobFileCheck(this.m_managerName, this.m_jobkickId);
		}else {
			this.m_jobKick = new JobFileCheck();
		}
		if (this.m_jobKick == null) {
			throw new InternalError("JobFileCheck is null");
		}

		// 実行契機情報の反映
		reflectJobKick();

		// ファイルチェック情報の反映
		JobKickFileCheckComposite jobKickFileCheckComposite
			= (JobKickFileCheckComposite)this.m_detailComposite;
		JobFileCheck jobFileCheck = (JobFileCheck)this.m_jobKick;
		jobKickFileCheckComposite.setJobFileCheck(
				this.m_managerName, 
				jobFileCheck.getOwnerRoleId(), 
				jobFileCheck.getFacilityId(), 
				jobFileCheck.getScope(), 
				jobFileCheck.getDirectory(), 
				jobFileCheck.getFileName(), 
				jobFileCheck.getEventType(), 
				jobFileCheck.getModifyType());
	}

	/**
	 * ダイアログにマニュアル実行契機情報を反映します。
	 *
	 */
	private void reflectJobManual() {

		//マネージャより実行契機情報を取得する
		if(this.m_mode == PropertyDefineConstant.MODE_MODIFY
				|| this.m_mode == PropertyDefineConstant.MODE_COPY){
			this.m_jobKick = GetJobKick.getJobManual(this.m_managerName, this.m_jobkickId);
		}else {
			this.m_jobKick = new JobKick();
		}
		if (this.m_jobKick == null) {
			throw new InternalError("JobManual is null");
		}

		// 実行契機情報の反映
		reflectJobKick();
	}

	/**
	 * ダイアログの情報からジョブ実行契機情報を作成します。
	 *
	 * @return 入力値の検証結果
	 */
	private ValidateResult createJobKick() {
		ValidateResult result = null;

		//ジョブユニットID取得
		if (getJobunitId() != null) {
			this.m_jobKick.setJobunitId(getJobunitId());
		}

		//オーナーロールID
		if (this.m_cmpOwnerRoleId.getText().length() > 0) {
			this.m_jobKick.setOwnerRoleId(this.m_cmpOwnerRoleId.getText());
		}

		//実行契機ID取得
		if (this.m_txtJobKickId.getText().length() > 0) {
			this.m_jobKick.setId(this.m_txtJobKickId.getText());
			this.m_jobkickId = this.m_txtJobKickId.getText();
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.88"));
			return result;
		}

		//実行契機名取得
		if (this.m_txtJobKickName.getText().length() > 0) {
			this.m_jobKick.setName(this.m_txtJobKickName.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.89"));
			return result;
		}

		//ジョブID取得
		if (this.m_txtJobId.getText().length() > 0) {
			this.m_jobKick.setJobId(this.m_txtJobId.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.22"));
			return result;
		}

		//ジョブ名取得
		if (this.m_txtJobName.getText().length() > 0) {
			this.m_jobKick.setJobName(this.m_txtJobName.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.23"));
			return result;
		}

		//カレンダID
		if (this.m_jobkickType != JobKickConstant.TYPE_MANUAL 
				&& this.m_cmpCalendarId.getText().length() > 0) {
			this.m_jobKick.setCalendarId(this.m_cmpCalendarId.getText());
		} else{
			this.m_jobKick.setCalendarId(null);
		}

		//有効/無効取得
		if (this.m_jobkickType != JobKickConstant.TYPE_MANUAL) {
			this.m_jobKick.setValid(this.m_btnValid.getSelection());
		} else{
			this.m_jobKick.setValid(true);
		}

		// ランタイムジョブ変数情報
		if (this.m_jobKickParamComposite.getJobRuntimeParamList() != null) {
			this.m_jobKick.getJobRuntimeParamList().addAll(
					this.m_jobKickParamComposite.getJobRuntimeParamList());
		}
		
		return result;
	}


	/**
	 * ダイアログの情報からスケジュール情報を作成します。
	 *
	 * @return 入力値の検証結果
	 */
	private ValidateResult createJobSchedule() {
		ValidateResult result = null;

		this.m_jobKick = new JobSchedule();

		// ジョブ実行契機共通処理
		result = createJobKick();
		if (result != null) {
			return result;
		}

		// スケジュール情報
		JobSchedule jobSchedule = (JobSchedule)this.m_jobKick;
		// Composite（スケジュール）
		JobKickScheduleComposite jobKickScheduleComposite 
			= (JobKickScheduleComposite)this.m_detailComposite;
		
		// スケジュール種別
		jobSchedule.setScheduleType(jobKickScheduleComposite.getScheduleType());

		if (jobKickScheduleComposite.getScheduleType() == ScheduleConstant.TYPE_DAY) {
			// スケジュール設定「毎日」
			// 時
			jobSchedule.setHour(jobKickScheduleComposite.getHour());
			// 分
			jobSchedule.setMinute(jobKickScheduleComposite.getMinute());

		} else if (jobKickScheduleComposite.getScheduleType() == ScheduleConstant.TYPE_WEEK) {
			//スケジュール設定「曜日」
			// 曜日
			if (jobKickScheduleComposite.getWeek() != null) {
				jobSchedule.setWeek(jobKickScheduleComposite.getWeek());
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.37"));
				return result;
			}
			// 時
			jobSchedule.setHour(jobKickScheduleComposite.getHour());
			// 分
			jobSchedule.setMinute(jobKickScheduleComposite.getMinute());

		} else if (jobKickScheduleComposite.getScheduleType() == ScheduleConstant.TYPE_REPEAT) {
			// 開始（分）
			jobSchedule.setFromXminutes(jobKickScheduleComposite.getFromXminutes());
			// 間隔（分）
			jobSchedule.setEveryXminutes(jobKickScheduleComposite.getEveryXminutes());
		}

		return result;
	}

	/**
	 * ダイアログの情報からファイルチェック情報を作成します。
	 *
	 * @return 入力値の検証結果
	 */
	private ValidateResult createFileCheck() {
		ValidateResult result = null;

		this.m_jobKick = new JobFileCheck();

		// ジョブ実行契機共通処理
		result = createJobKick();
		if (result != null) {
			return result;
		}

		// ファイルチェック情報
		JobFileCheck jobFileCheck = (JobFileCheck)this.m_jobKick;
		// Composite（スケジュール）
		JobKickFileCheckComposite jobKickFileCheckComposite 
			= (JobKickFileCheckComposite)this.m_detailComposite;

		// ファシリティID・スコープ
		if(jobKickFileCheckComposite.getFacilityId() != null){
			jobFileCheck.setFacilityId(jobKickFileCheckComposite.getFacilityId());
			jobFileCheck.setScope(jobKickFileCheckComposite.getScope());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.hinemos.3"));
			return result;
		}

		// ディレクトリ
		if(jobKickFileCheckComposite.getDirectory() != null){
			jobFileCheck.setDirectory(jobKickFileCheckComposite.getDirectory());
		}else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.92"));
			return result;
		}

		// ファイル名
		if(jobKickFileCheckComposite.getFileName() != null){
			jobFileCheck.setFileName(jobKickFileCheckComposite.getFileName());
		}else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.90"));
			return result;
		}

		// ファイルチェック種別
		jobFileCheck.setEventType(jobKickFileCheckComposite.getEventType());
		// 変更種別
		jobFileCheck.setModifyType(jobKickFileCheckComposite.getModifyType());

		return result;
	}

	/**
	 * ダイアログの情報からマニュアル実行契機情報を作成します。
	 *
	 * @return 入力値の検証結果
	 */
	private ValidateResult createManual() {
		ValidateResult result = null;

		this.m_jobKick = new JobFileCheck();

		// ジョブ実行契機共通処理
		result = createJobKick();

		return result;
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("register");
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

		// ジョブ実行契機情報
		if (this.m_jobkickType == JobKickConstant.TYPE_SCHEDULE) {
			// スケジュール
			result = createJobSchedule();
			if (result != null) {
				return result;
			}
		} else if (this.m_jobkickType == JobKickConstant.TYPE_FILECHECK) {
			// ファイルチェック
			result = createFileCheck();
			if (result != null) {
				return result;
			}
		} else if (this.m_jobkickType == JobKickConstant.TYPE_MANUAL) {
			// マニュアル実行契機
			result = createManual();
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	/**
	 * 所属ジョブユニットのジョブIDを返します。<BR>
	 * @return 所属ジョブユニットのジョブID
	 */
	public String getJobunitId() {
		return this.m_jobunitId;
	}

	/**
	 * 所属ジョブユニットのジョブIDを設定します。<BR>
	 * @param jobunitId 所属ジョブユニットのジョブID
	 */
	public void setJobunitId(String jobunitId) {
		this.m_jobunitId = jobunitId;
	}

	@Override
	protected boolean action() {
		boolean result = false;
		try {
			String managerName = this.m_managerComposite.getText();

			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
			if(this.m_mode == PropertyDefineConstant.MODE_MODIFY){
				if (this.m_jobkickType == JobKickConstant.TYPE_SCHEDULE) {
					wrapper.modifySchedule((JobSchedule)this.m_jobKick);
				} else if (this.m_jobkickType == JobKickConstant.TYPE_FILECHECK) {
					wrapper.modifyFileCheck((JobFileCheck)this.m_jobKick);
				} else if (this.m_jobkickType == JobKickConstant.TYPE_MANUAL) {
					wrapper.modifyJobManual(this.m_jobKick);
				}
				Object[] arg = {managerName};
				MessageDialog.openInformation(null, Messages.getString("successful"),
						Messages.getString("message.job.77", arg));
			}
			else {//
				if (this.m_jobkickType == JobKickConstant.TYPE_SCHEDULE) {
					wrapper.addSchedule((JobSchedule)this.m_jobKick);
				} else if (this.m_jobkickType == JobKickConstant.TYPE_FILECHECK) {
					wrapper.addFileCheck((JobFileCheck)this.m_jobKick);
				} else if (this.m_jobkickType == JobKickConstant.TYPE_MANUAL) {
					wrapper.addJobManual(this.m_jobKick);
				}

				Object[] arg = {managerName};
				MessageDialog.openInformation(null, Messages.getString("successful"),
						Messages.getString("message.job.79", arg));
			}
			result = true;
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (JobKickDuplicate_Exception e) {
			String[] args = {this.m_jobKick.getId()};
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.job.83",args) + " " + HinemosMessage.replace(e.getMessage()));
		} catch (InvalidUserPass_Exception e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.job.74") + " " + HinemosMessage.replace(e.getMessage()));
		} catch (InvalidSetting_Exception e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.job.74") + " " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("action(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return result;
	}

	private void updateCalendarId() {
		this.m_cmpCalendarId.createCalIdCombo(this.m_managerComposite.getText(), this.m_cmpOwnerRoleId.getText());
		this.m_txtJobId.setText("");
		this.m_txtJobName.setText("");
		setJobunitId(null);
	}
}
