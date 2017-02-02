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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.notify.NotifyInfo;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * 初回通知条件コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「初回通知」 ラベル</dd>
 *  <dd>「初回通知のタイミング」 テキストボックス</dd>
 * </dl>
 *
 * @version 3.0.0
 * @since 3.0.0
 */
public class NotifyInitialComposite extends Composite {

	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = 4;

	/** カラム数（値）。*/
	public static final int WIDTH_VALUE = 2;

	/** 空白のカラム数。 */
	public static final int WIDTH_BLANK = 9;

	/** カラム数（全て）。 */
	public static final int WIDTH_ALL = 16;

	/** 初回通知 テキストボックス。 */
	public Text m_textInitialCount = null;

	/** 初回も通知しない チェックボックス。 */
	public Button m_checkNotRenotifyFirst = null;

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
	public NotifyInitialComposite(Composite parent, int style) {
		super(parent, style);

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
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		// 初回通知のタイミング
		/*
		 * グループ
		 */
		Group groupInitial = new Group(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, groupInitial);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 16;
		groupInitial.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupInitial.setLayoutData(gridData);
		groupInitial.setText(Messages.getString("notify.initial"));


		// 「同じ重要度の監視結果が」
		label = new Label(groupInitial, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyinitial1", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.initial.1"));

		// テキストボックス（初回通知）
		this.m_textInitialCount = new Text(groupInitial, SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, m_textInitialCount);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textInitialCount.setLayoutData(gridData);
		this.m_textInitialCount.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 「回以上連続した場合に通知する」
		label = new Label(groupInitial, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyinitial2", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.initial.2"));
		
		// チェックボックス（「初回も通知しない」）
		this.m_checkNotRenotifyFirst = new Button(groupInitial, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "renotifyfirst", m_checkNotRenotifyFirst);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.m_checkNotRenotifyFirst.setText(Messages.getString("suppress.always.include.first"));
		this.m_checkNotRenotifyFirst.setLayoutData(gridData);
		this.m_checkNotRenotifyFirst.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目を明示
		if("".equals(this.m_textInitialCount.getText())){
			this.m_textInitialCount.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textInitialCount.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param notify 設定値として用いる通知情報
	 */
	public void setInputData(NotifyInfo notify) {
		if(notify != null){
			if (notify.getInitialCount() != null) {
				this.m_textInitialCount.setText(notify.getInitialCount().toString());
			}
			if (notify.isNotFirstNotify() != null && notify.isNotFirstNotify().booleanValue()) {
				this.m_checkNotRenotifyFirst.setSelection(true);
			} else {
				this.m_checkNotRenotifyFirst.setSelection(false);
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
	public ValidateResult createInputData(NotifyInfo info) {

		if(info != null){

			try {
				info.setInitialCount(Integer.parseInt(this.m_textInitialCount.getText()));
				if (this.m_checkNotRenotifyFirst.getSelection())
					info.setNotFirstNotify(true);
				else {
					info.setNotFirstNotify(false);
				}
			} catch (NumberFormatException e) {
				return this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.notify.33"));
			}
		}
		return null;
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
}
