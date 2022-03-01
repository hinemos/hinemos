/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.dialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
import org.openapitools.client.model.CalendarDetailInfoResponse;
import org.openapitools.client.model.CalendarInfoResponse;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.calendar.action.AddCalendar;
import com.clustercontrol.calendar.action.GetCalendar;
import com.clustercontrol.calendar.action.GetCalendarDetailTableDefine;
import com.clustercontrol.calendar.action.ModifyCalendar;
import com.clustercontrol.calendar.composite.CalendarDetailInfoComposite;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.DateTimeDialog;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * カレンダ設定作成・変更ダイアログクラス<BR>
 *
 * @version 4.1.0
 * @since 2.0.0
 */
public class CalendarDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog(CalendarDialog.class);
	/** カレンダID */
	private String calendarId = "";
	/** 変更用ダイアログ判別フラグ */
	private int mode;
	/** カレンダID */
	private Text calIdText = null;
	/** カレンダ名 */
	private Text calNameText = null;
	/** 有効期間（開始） */
	private Text calTimeFromText = null;
	/** 有効期間（終了） */
	private Text calTimeToText = null;
	/** 入力値を保持するオブジェクト */
	private CalendarInfoResponse inputData = null;
	/** カレンダ詳細情報 */
	private CalendarDetailInfoComposite calDetailComposite = null;
	/** オーナーロールID用テキスト */
	private RoleIdListComposite calRoleIdListComposite = null;
	private String ownerRoleId = null;
	/** マネージャ名 */
	private String managerName = null;
	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	// ----- 共通メンバ変数 ----- //
	private Shell shell = null;
	private Group calGroup = null; // カレンダ設定グループ
	private Group calDetailGroup = null;// カレンダ詳細グループ
	private Text calDescription = null;
	private Button calTimeFromButton = null;
	private Button calTimeToButton = null;

	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public CalendarDialog(Shell parent, String managerName, String id, int mode) {
		super(parent);
		this.managerName = managerName;
		this.calendarId = id;
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
		shell.setText(Messages.getString("dialog.calendar.calendar.create.modify"));
		GridData gridData = new GridData();
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);
		/*
		 * カレンダ設定グループ
		 */
		calGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "setting", calGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		calGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calGroup.setLayoutData(gridData);
		calGroup.setText(Messages.getString("calendar.create"));

		/*
		 * マネージャ
		 */
		Label labelManager = new Label(calGroup, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", labelManager);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 4;
		labelManager.setLayoutData(gridData);
		labelManager.setText(Messages.getString("facility.manager") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
			this.m_managerComposite = new ManagerListComposite(calGroup, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(calGroup, SWT.NONE, true);
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (calDetailComposite.getDetailList() != null && calDetailComposite.getDetailList().size() > 0) {
						if (!MessageDialog.openConfirm(null, Messages.getString("confirmed"),
								Messages.getString("message.calendar.55"))) {
							m_managerComposite.setText(managerName);
							return;
						}
						String managerName = m_managerComposite.getText();
						calRoleIdListComposite.createRoleIdList(managerName);
						calDetailComposite.changeManagerName(m_managerComposite.getText(), true);
						String roleId = calRoleIdListComposite.getText();
						calDetailComposite.changeOwnerRoleId(roleId);
					} else {
						String managerName = m_managerComposite.getText();
						calRoleIdListComposite.createRoleIdList(managerName);
						calDetailComposite.changeManagerName(m_managerComposite.getText(), false);
						String roleId = calRoleIdListComposite.getText();
						calDetailComposite.changeOwnerRoleId(roleId);
					}
					managerName = m_managerComposite.getText();
				}
			});
		}
		WidgetTestUtil.setTestId(this, "managerComposite", this.m_managerComposite);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 8;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);

		if (this.managerName != null) {
			this.m_managerComposite.setText(this.managerName);
		}

		/*
		 * カレンダID
		 */
		// ラベル
		Label lblCalID = new Label(calGroup, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "calid", lblCalID);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblCalID.setLayoutData(gridData);
		lblCalID.setText(Messages.getString("calendar.id") + " : ");
		// テキスト
		calIdText = new Text(calGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "calid", calIdText);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calIdText.setLayoutData(gridData);
		calIdText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * カレンダ名
		 */
		// ラベル
		Label lblCalName = new Label(calGroup, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "calendarname", lblCalName);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblCalName.setLayoutData(gridData);
		lblCalName.setText(Messages.getString("calendar.name") + " : ");
		// テキスト
		calNameText = new Text(calGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "calName", calNameText);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calNameText.setLayoutData(gridData);
		calNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * 説明
		 */
		// ラベル
		Label lblCalDescription = new Label(calGroup, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "description", lblCalDescription);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblCalDescription.setLayoutData(gridData);
		lblCalDescription.setText(Messages.getString("description") + " : ");
		// テキスト
		calDescription = new Text(calGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "caldescription", calDescription);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDescription.setLayoutData(gridData);
		calDescription.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * オーナーロールID
		 */
		Label labelRoleId = new Label(calGroup, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "ownerroleid", labelRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_ADD || this.mode == PropertyDefineConstant.MODE_COPY) {
			this.calRoleIdListComposite = new RoleIdListComposite(calGroup, SWT.NONE, this.managerName, true,
					Mode.OWNER_ROLE);
			this.calRoleIdListComposite.getComboRoleId().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (calDetailComposite.getDetailList() != null && calDetailComposite.getDetailList().size() > 0) {
						if (!MessageDialog.openConfirm(null, Messages.getString("confirmed"),
								Messages.getString("message.calendar.50"))) {
							calRoleIdListComposite.setText(ownerRoleId);
							return;
						}
					}
					calDetailComposite.changeOwnerRoleId(calRoleIdListComposite.getText());
					ownerRoleId = calRoleIdListComposite.getText();
				}
			});
		} else {
			this.calRoleIdListComposite = new RoleIdListComposite(calGroup, SWT.NONE, this.managerName, false,
					Mode.OWNER_ROLE);
		}
		WidgetTestUtil.setTestId(this, "calroleidlist", calRoleIdListComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calRoleIdListComposite.setLayoutData(gridData);

		/*
		 * 有効期間（開始）
		 */
		// ラベル
		Label lblCalTimeFrom = new Label(calGroup, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "caltimefrom", lblCalTimeFrom);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblCalTimeFrom.setLayoutData(gridData);
		lblCalTimeFrom.setText(Messages.getString("valid.time") + "(" + Messages.getString("start") + ")" + " : ");
		// テキスト
		calTimeFromText = new Text(calGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "timefrom", calTimeFromText);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calTimeFromText.setLayoutData(gridData);
		// 日時ダイアログからの入力しか受け付けません
		calTimeFromText.setEnabled(false);
		calTimeFromText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// 追加ボタン
		calTimeFromButton = new Button(calGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "timefrom", calTimeFromButton);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calTimeFromButton.setLayoutData(gridData);
		calTimeFromButton.setText(Messages.getString("calendar.button"));
		calTimeFromButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DateTimeDialog dialog = new DateTimeDialog(shell);
				if (calTimeFromText.getText().length() > 0) {
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					try {
						Date date = sdf.parse(calTimeFromText.getText());
						dialog.setDate(date);
					} catch (ParseException e1) {
						m_log.warn("calTimeFromText : " + e1.getMessage());
						
					}
				}
				if (dialog.open() == IDialogConstants.OK_ID) {
					// ダイアログより取得した日時を"yyyy/MM/dd HH:mm:ss"の形式に変換
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					String tmp = sdf.format(dialog.getDate());
					calTimeFromText.setText(tmp);
					update();
				}
			}
		});
		/*
		 * 有効期間（終了）
		 */
		// ラベル
		Label lblCalTimeTo = new Label(calGroup, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "caltimeto", lblCalTimeTo);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblCalTimeTo.setLayoutData(gridData);
		lblCalTimeTo.setText(Messages.getString("valid.time") + "(" + Messages.getString("end") + ")" + " : ");
		// テキスト
		calTimeToText = new Text(calGroup, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "timeto", calTimeToText);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calTimeToText.setLayoutData(gridData);
		// 日時ダイアログからの入力しか受け付けません
		calTimeToText.setEnabled(false);
		calTimeToText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// 追加ボタン
		calTimeToButton = new Button(calGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "timeto", calTimeToButton);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calTimeToButton.setLayoutData(gridData);
		calTimeToButton.setText(Messages.getString("calendar.button"));
		calTimeToButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DateTimeDialog dialog = new DateTimeDialog(shell);
				if (calTimeToText.getText().length() > 0) {
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					try{
						Date date = sdf.parse(calTimeToText.getText());
						dialog.setDate(date);
					}catch (ParseException e1){
						m_log.warn("calTimeToText : " + e1.getMessage());
					}
				}
				if (dialog.open() == IDialogConstants.OK_ID) {
					// 日付ダイアログより取得した数字列を"yyyy/MM/dd HH:mm:ss"の形式に変換
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					m_log.trace("CalendarDialog getTime：" + dialog.getDate());
					String tmp = sdf.format(dialog.getDate());
					calTimeToText.setText(tmp);
				}
			}
		});

		/*
		 * カレンダ詳細グループ
		 *
		 */
		calDetailGroup = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "detail", calDetailGroup);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 1;
		calDetailGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailGroup.setLayoutData(gridData);
		calDetailGroup.setText(Messages.getString("calendar.detail"));

		/**
		 * カレンダ詳細定義情報
		 */
		// 詳細情報テーブルカラム取得
		GetCalendarDetailTableDefine.get();
		this.calDetailComposite = new CalendarDetailInfoComposite(calDetailGroup, SWT.NONE, this.managerName);
		WidgetTestUtil.setTestId(this, "detail", calDetailComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 220;
		calDetailComposite.setLayoutData(gridData);

		Display calDisplay = shell.getDisplay();
		shell.setLocation((calDisplay.getBounds().width - shell.getSize().x) / 2,
				(calDisplay.getBounds().height - shell.getSize().y) / 2);

		// ダイアログを調整
		this.adjustDialog();
		// ダイアログにカレンダ詳細情報反映
		this.reflectCalendar();
		// ダイアログのパラメータをカレンダ詳細情報に反映
		// this.createCalendarInfo();
		// 必須入力項目を可視化
		this.update();

	}

	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog() {
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
	public void update() {
		// 必須項目を明示

		// カレンダIDのインデックス：9
		if ("".equals(this.calIdText.getText())) {
			this.calIdText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.calIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// カレンダ名のインデックス：9
		if ("".equals(this.calNameText.getText())) {
			this.calNameText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.calNameText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 有効期間（開始）のインデックス：9
		if ("".equals(this.calTimeFromText.getText())) {
			this.calTimeFromText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.calTimeFromText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 有効期間（終了）のインデックス：9
		if ("".equals(this.calTimeToText.getText())) {
			this.calTimeToText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.calTimeToText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ダイアログの情報からカレンダ情報を作成します。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see
	 */
	private void createCalendarInfo() {

		this.inputData = new CalendarInfoResponse();

		// カレンダID取得
		if (calIdText.getText().length() > 0) {
			inputData.setCalendarId(calIdText.getText());
		}
		m_log.trace("createCalendarInfo cal name = " + calNameText.getText());
		// カレンダ名取得
		if (calNameText.getText().length() > 0 && !"".equals(calNameText.getText())) {
			m_log.trace("createCalendarInfo22 cal name = " + calNameText.getText());
			inputData.setCalendarName(calNameText.getText());
			m_log.trace("input cal name = " + inputData.getCalendarName());
		}
		// オーナーロールID
		if (calRoleIdListComposite.getText().length() > 0) {
			inputData.setOwnerRoleId(calRoleIdListComposite.getText());
		}
		// カレンダ説明取得
		if (calDescription.getText().length() > 0) {
			inputData.setDescription(calDescription.getText());
		} else {
			inputData.setDescription("");
		}
		// 有効期間（開始）取得
		if (calTimeFromText.getText().length() > 0) {
			inputData.setValidTimeFrom(calTimeFromText.getText());
		}
		// 有効期間（終了）取得
		if (calTimeToText.getText().length() > 0) {
			inputData.setValidTimeTo(calTimeToText.getText());
		}
		// カレンダ詳細情報取得
		if (this.calDetailComposite.getDetailList() != null) {

			m_log.debug("Add CalendarDetailInfo : " + this.calDetailComposite.getDetailList().size());

			int i = 1;
			for (CalendarDetailInfoResponse detailInfo : this.calDetailComposite.getDetailList()) {
				detailInfo.setOrderNo(i);
				this.inputData.addCalendarDetailListItem(detailInfo);
				i++;
			}
		}
	}

	/**
	 * ダイアログにカレンダ情報を反映します。
	 *
	 * @param detailList
	 * @throws HinemosUnknown
	 */
	private void reflectCalendar() {
		// 初期表示
		if (mode == PropertyDefineConstant.MODE_MODIFY || mode == PropertyDefineConstant.MODE_COPY) {
			// 変更、コピーの場合、情報取得
			inputData = new GetCalendar().getCalendar(this.m_managerComposite.getText(), this.calendarId);
		} else {
			// 作成の場合
			inputData = new CalendarInfoResponse();
		}

		if (inputData == null)
			throw new InternalError("CalendarInfo is null");

		// カレンダ情報取得
		if (inputData.getCalendarId() != null) {
			this.calendarId = inputData.getCalendarId();
			this.calIdText.setText(inputData.getCalendarId());
			// カレンダ定義変更の際にはカレンダIDは変更不可
			if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
				this.calIdText.setEnabled(false);
			}
		}
		if (inputData.getCalendarName() != null) {
			this.calNameText.setText(inputData.getCalendarName());
		}
		if (inputData.getDescription() != null) {
			this.calDescription.setText(inputData.getDescription());
		}
		if (inputData.getValidTimeFrom() != null) {
			this.calTimeFromText.setText(inputData.getValidTimeFrom());
		}
		if (inputData.getValidTimeTo() != null) {
			this.calTimeToText.setText(inputData.getValidTimeTo());
		}
		// カレンダ詳細情報取得
		calDetailComposite.setDetailList((ArrayList<CalendarDetailInfoResponse>) inputData.getCalendarDetailList());

		// オーナーロールID取得
		if (inputData.getOwnerRoleId() != null) {
			this.calRoleIdListComposite.setText(inputData.getOwnerRoleId());
		}
		ownerRoleId = this.calRoleIdListComposite.getText();
		this.calDetailComposite.setOwnerRoleId(this.calRoleIdListComposite.getText());

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
		createCalendarInfo();
		String managerName = this.m_managerComposite.getText();
		if ( this.inputData != null) {
			try{
			if (mode == PropertyDefineConstant.MODE_ADD) {
				// 作成の場合
				result = new AddCalendar().add(managerName, inputData);
			} else if (mode == PropertyDefineConstant.MODE_MODIFY) {
				// 変更の場合
				result = new ModifyCalendar().modify(managerName, inputData, calIdText.getText());
			} else if (mode == PropertyDefineConstant.MODE_COPY) {
				// コピーの場合
				result = new AddCalendar().add(managerName, inputData);
			}
			}catch(Exception e){
				m_log.error(e.getMessage());
			}
		} else {
			m_log.error("action() Calendarinfo is null");
		}
		return result;
	}
	
}
