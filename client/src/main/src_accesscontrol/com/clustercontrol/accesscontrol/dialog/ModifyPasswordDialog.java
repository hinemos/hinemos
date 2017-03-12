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

package com.clustercontrol.accesscontrol.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * アクセス[パスワード変更]ダイアログクラスです。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class ModifyPasswordDialog extends CommonDialog {
	/** ユーザID用テキスト */
	private Text uidText = null;
	/** パスワード用テキスト */
	private Text passwordText1 = null;
	/** パスワード確認用テキスト */
	private Text passwordText2 = null;
	/** ユーザID */
	private String uid = null;
	/** パスワード */
	private String password = null;
	/** シェル */
	private Shell shell = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 */
	public ModifyPasswordDialog(Shell parent) {
		super(parent);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#customizeDialog(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		shell = this.getShell();

		shell.setText(Messages
				.getString("dialog.accesscontrol.modify.user.password"));

		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 10;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.fill = true;
		parent.setLayout(layout);

		RowLayout rowLayout = null;

		Composite uidComposite = new Composite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "uid", uidComposite);
		rowLayout = new RowLayout();
		rowLayout.type = SWT.HORIZONTAL;
		rowLayout.spacing = 10;
		uidComposite.setLayout(rowLayout);
		Label uidTitle = new Label(uidComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "uidtytle", uidTitle);
		uidTitle.setText(Messages.getString("user.id") + " : ");
		uidTitle.setLayoutData(new RowData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		uidText = new Text(uidComposite, SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "uid", uidText);
		uidText.setLayoutData(new RowData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		uidText.setText(uid);
		uidText.setEnabled(false);

		Composite passwordComposite1 = new Composite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "password1", passwordComposite1);
		rowLayout = new RowLayout();
		rowLayout.type = SWT.HORIZONTAL;
		rowLayout.spacing = 10;
		passwordComposite1.setLayout(rowLayout);
		Label passwordTitle1 = new Label(passwordComposite1, SWT.NONE);
		WidgetTestUtil.setTestId(this, "passwordtitle1", passwordTitle1);
		passwordTitle1.setText(Messages.getString("password") + " : ");
		passwordTitle1.setLayoutData(new RowData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		passwordText1 = new Text(passwordComposite1, SWT.BORDER | SWT.PASSWORD);
		WidgetTestUtil.setTestId(this, "password1", passwordText1);
		passwordText1.setLayoutData(new RowData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		passwordText1.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));

		passwordText1.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		Composite passwordComposite2 = new Composite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "password2", passwordComposite2);
		rowLayout = new RowLayout();
		rowLayout.type = SWT.HORIZONTAL;
		rowLayout.spacing = 10;
		passwordComposite2.setLayout(rowLayout);
		Label passwordTitle2 = new Label(passwordComposite2, SWT.NONE);
		WidgetTestUtil.setTestId(this, "passwordtitle2", passwordTitle2);
		passwordTitle2.setText(Messages.getString("confirm.password") + " : ");
		passwordTitle2.setLayoutData(new RowData(150, SizeConstant.SIZE_LABEL_HEIGHT));
		passwordText2 = new Text(passwordComposite2, SWT.BORDER | SWT.PASSWORD);
		WidgetTestUtil.setTestId(this, "password2", passwordText2);
		passwordText2.setLayoutData(new RowData(150, SizeConstant.SIZE_TEXT_HEIGHT));
		passwordText2.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));

		passwordText2.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, null, label);

		// 画面中央に
		Display modifyPasswordDisplay = shell.getDisplay();
		shell.setLocation(
				(modifyPasswordDisplay.getBounds().width - shell.getSize().x) / 2, (modifyPasswordDisplay
						.getBounds().height - shell.getSize().y) / 2);

		update();
	}


	private void update(){
		if("".equals(this.passwordText1.getText())){
			this.passwordText1.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.passwordText1.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.passwordText2.getText())){
			this.passwordText2.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.passwordText2.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
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

		//パスワード取得
		if (passwordText1.getText().length() > 0) {
			setPassword(passwordText1.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.accesscontrol.2"));
			return result;
		}

		//パスワード取得
		if (passwordText2.getText().length() > 0) {
			setPassword(passwordText2.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.accesscontrol.3"));
			return result;
		}

		//パスワード確認
		if (passwordText1.getText().compareTo(passwordText2.getText()) != 0) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.accesscontrol.4"));
			return result;
		}

		//パスワード取得
		this.password = passwordText1.getText();

		return null;
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * パスワードを返します。
	 *
	 * @return パスワード
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * パスワードを設定します。
	 *
	 * @param password パスワード
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * ユーザIDを返します。
	 *
	 * @return ユーザID
	 */
	public String getUserid() {
		return uid;
	}

	/**
	 * ユーザIDを設定します。
	 *
	 * @param uid ユーザID
	 */
	public void setUserid(String uid) {
		this.uid = uid;
	}
}
