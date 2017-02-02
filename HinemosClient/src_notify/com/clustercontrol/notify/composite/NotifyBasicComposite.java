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

package com.clustercontrol.notify.composite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.calendar.composite.CalendarIdListComposite;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.notify.NotifyInfo;

/**
 * 通知基本情報コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「通知ID」 テキストボックス</dd>
 *  <dd>「説明」 テキストボックス</dd>
 * </dl>
 *
 * @version 4.0.0
 * @since 3.0.0
 */

public class NotifyBasicComposite extends Composite {

	private static Log m_log = LogFactory.getLog( NotifyBasicComposite.class );
	
	/** カラム数（タイトル）。*/
	public static final int WIDTH_TITLE = 3;

	/** カラム数（値）。*/
	public static final int WIDTH_VALUE = 8;

	/** 空白のカラム。*/
	public static final int WIDTH_BLANK = 4;

	/** カラム数（全て）。*/
	public static final int WIDTH_ALL = 15;

	/** マネージャリスト用コンポジット */
	public ManagerListComposite m_managerComposite = null;

	/** 通知ID テキストボックス。 */
	private Text m_textNotifyId = null;

	/** 説明 テキストボックス。 */
	private Text m_textDescription = null;

	/** オーナーロールID用テキスト */
	public RoleIdListComposite m_ownerRoleId = null;

	/** カレンダID コンポジット。 */
	private CalendarIdListComposite m_calendarId = null;

	/** 変更対象通知ID。 */
	private String notifyId = null;

	/** Manager */
	private String managerName = null;

	/** Enable editing */
	private boolean enableFlg;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public NotifyBasicComposite(Composite parent, int style, String notifyId) {
		super(parent, style);

		this.notifyId = notifyId;
		this.initialize();
	}


	public NotifyBasicComposite(Composite parent, int style, String managerName, String notifyId, boolean enableFlg) {
		super(parent, style);

		this.managerName = managerName;
		this.notifyId = notifyId;
		this.enableFlg = enableFlg;

		this.initialize();
	}


	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;
		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		/*
		 * マネージャ
		 */
		label = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("facility.manager") + " : ");
		this.m_managerComposite = new ManagerListComposite(this, SWT.NONE, enableFlg);
		if (enableFlg) {
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_log.debug("widgetSelected(managerComposite)");
					String managerName = m_managerComposite.getText();
					m_ownerRoleId.createRoleIdList(managerName);
					m_calendarId.createCalIdCombo(managerName, m_ownerRoleId.getText());
				}
			});
		}
		WidgetTestUtil.setTestId(this, "managerComposite", m_managerComposite);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);
		this.m_managerComposite.setText(managerName);

		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space0", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 通知ID
		 */
		// ラベル
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyid", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.id") + " : ");

		// テキスト
		this.m_textNotifyId = new Text(this, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "notifyid", m_textNotifyId);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textNotifyId.setLayoutData(gridData);
		this.m_textNotifyId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 説明
		 */
		// ラベル
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "description", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("description") + " : ");
		// テキスト
		this.m_textDescription = new Text(this, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "description", m_textDescription);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textDescription.setLayoutData(gridData);
		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space2", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * オーナーロールID
		 */
		Label labelRoleId = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "ownerroleid", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		this.m_ownerRoleId = new RoleIdListComposite(this, SWT.NONE, this.m_managerComposite.getText(), enableFlg, Mode.OWNER_ROLE);
		if (enableFlg) {
			this.m_ownerRoleId.getComboRoleId().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_log.debug("widgetSelected(roleComposite)");
					String ownerRoleId = m_ownerRoleId.getText();
					String managerName = m_managerComposite.getText();
					m_calendarId.createCalIdCombo(managerName, ownerRoleId);
				}
			});
		}
		WidgetTestUtil.setTestId(this, "ownerroleid", m_ownerRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_ownerRoleId.setLayoutData(gridData);

		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space3", label);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * カレンダID
		 */
		this.m_calendarId = new CalendarIdListComposite(this, SWT.NONE, true);
		WidgetTestUtil.setTestId(this, "calendarid", m_calendarId);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_calendarId.setLayoutData(gridData);
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 通知IDが必須項目であることを明示
		if(this.m_textNotifyId.getEnabled() && "".equals(this.m_textNotifyId.getText())){
			this.m_textNotifyId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textNotifyId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる通知情報
	 * @param udpateFlg 更新するか否か（true：更新する）
	 */
	public void setInputData(NotifyInfo info, boolean updateFlg) {
		if(info != null){
			// 通知
			if (info.getNotifyId() != null) {
				this.m_textNotifyId.setText(info.getNotifyId());
				if (updateFlg) {
					this.m_textNotifyId.setEnabled(false);
				}
			}
			// 説明
			if (info.getDescription() != null) {
				this.m_textDescription.setText(info.getDescription());
			}
			// オーナーロールID
			if (info.getOwnerRoleId() != null) {
				this.m_ownerRoleId.setText(info.getOwnerRoleId());
			}
			// カレンダID
			this.m_calendarId.createCalIdCombo(m_managerComposite.getText(), m_ownerRoleId.getText());
			if (info.getCalendarId() != null) {
				this.m_calendarId.setText(info.getCalendarId());
			}
		}

		this.update();
	}

	/**
	 * 引数で指定された通知情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は認証結果を返します。
	 * 不正ではない場合は、<code>null</code>を返します。
	 *
	 * @param info 入力値を設定する通知情報
	 * @return 検証結果
	 *
	 */
	public ValidateResult createInputData(NotifyInfo info, String notifyId) {
		if(info != null){
			// 通知
			if (this.m_textNotifyId.getText() != null
					&& !"".equals((this.m_textNotifyId.getText()).trim())) {
				info.setNotifyId(this.m_textNotifyId.getText());
			}
			// 説明
			if (this.m_textDescription.getText() != null
					&& !"".equals((this.m_textDescription.getText()).trim())) {
				info.setDescription(this.m_textDescription.getText());
			}
			// オーナーロールID
			if (this.m_ownerRoleId.getText() != null
					&& !"".equals((this.m_ownerRoleId.getText()).trim())) {
				info.setOwnerRoleId(this.m_ownerRoleId.getText());
			}
			// カレンダID
			if (this.m_calendarId.getText() != null
				 && !"".equals(this.m_calendarId.getText().trim())) {
				info.setCalendarId(this.m_calendarId.getText());
			}
		}
		return null;
	}

	/**
	 * オーナーロールIDを設定します。
	 * 継承先クラスにてオーナーロールIDに関連するオブジェクト権限を持つ入力項目をクリアします。
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		m_ownerRoleId.setText(ownerRoleId);
	}

	public String getOwnerRoleId(){
		return m_ownerRoleId.getText();
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if(this.notifyId != null && !"".equals(this.notifyId.trim())){
			this.m_textNotifyId.setEnabled(false);
		}
		else {
			this.m_textNotifyId.setEnabled(enabled);
		}
		this.m_textDescription.setEnabled(enabled);
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 * @return 認証結果
	 */
	protected ValidateResult setValidateResult(String id, String message) {

		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	public RoleIdListComposite getRoleIdList(){
		return this.m_ownerRoleId;
	}

	public ManagerListComposite getManagerListComposite() {
		return this.m_managerComposite;
	}
}
