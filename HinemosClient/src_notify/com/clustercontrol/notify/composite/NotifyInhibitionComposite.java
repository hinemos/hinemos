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

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.bean.RenotifyTypeConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.notify.NotifyInfo;


/**
 * 抑制条件コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「イベント重複時の通知回数の抑制」 ラベル</dd>
 *  <dd>「抑制しないする」 ラジオボタン</dd>
 *  <dd>「期間で抑制する」 ラジオボタン</dd>
 *  <dd>「回数で抑制する」 ラジオボタン</dd>
 *  <dd>「重要度で抑制する」 ラジオボタン</dd>
 * </dl>
 *
 * @version 4.0.0
 * @since 3.0.0
 */
public class NotifyInhibitionComposite extends Composite {

	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = 3;

	/** カラム数（値）。*/
	public static final int WIDTH_VALUE = 2;

	/** 空白のカラム数。 */
	public static final int WIDTH_BLANK = 8;

	/** カラム数（全て）。 */
	public static final int WIDTH_ALL = 15;

	/**
	 * 抑制間隔（抑制しない） ラジオボタン。
	 * @see com.clustercontrol.bean.ExclusionConstant
	 */
	public Button m_radioRenotifyAlways = null;
	/**
	 * 抑制間隔（期間で抑制する） ラジオボタン。
	 * @see com.clustercontrol.bean.ExclusionConstant
	 */
	public Button m_radioRenotifyPeriod = null;

	/**
	 * 抑制間隔（重要度で抑制する） ラジオボタン。
	 * @see com.clustercontrol.bean.ExclusionConstant
	 */
	public Button m_radioRenotifyNo = null;

	/** 抑制期間 テキストボックス。 */
	public Text m_textRenotifyPeriod = null;

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
	public NotifyInhibitionComposite(Composite parent, int style) {
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

		// イベント重複時の通知回数
		/*
		 * グループ
		 */
		Group groupInhibition = new Group(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, groupInhibition);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupInhibition.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupInhibition.setLayoutData(gridData);
		groupInhibition.setText(Messages.getString("notify.inhibition"));

		// ラジオボタン（「常に通知する」）
		this.m_radioRenotifyAlways = new Button(groupInhibition, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "renotifyalways", m_radioRenotifyAlways);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_ALL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioRenotifyAlways.setLayoutData(gridData);
		this.m_radioRenotifyAlways.setText(Messages.getString("suppress.no"));

		// ラジオボタン（「前回通知から」）
		this.m_radioRenotifyPeriod = new Button(groupInhibition, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "renotifyperiod", m_radioRenotifyPeriod);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioRenotifyPeriod.setLayoutData(gridData);
		this.m_radioRenotifyPeriod.setText(Messages.getString("suppress.by.time.interval.1"));
		this.m_radioRenotifyPeriod.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// テキストボックス（期間で抑制する）
		this.m_textRenotifyPeriod = new Text(groupInhibition, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "renotifyperiod", m_textRenotifyPeriod);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textRenotifyPeriod.setLayoutData(gridData);
		this.m_textRenotifyPeriod.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// ラベル（「分間は同一重要度の通知はしない」）
		label = new Label(groupInhibition, SWT.NONE);
		WidgetTestUtil.setTestId(this, "timeinterval", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("suppress.by.time.interval.2"));

		// ラジオボタン（「通知しない」）
		this.m_radioRenotifyNo = new Button(groupInhibition, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "renotifyno", m_radioRenotifyNo);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioRenotifyNo.setLayoutData(gridData);
		this.m_radioRenotifyNo.setText(Messages.getString("suppress.always"));
		this.m_radioRenotifyNo.addSelectionListener(new SelectionAdapter() {
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
		if(this.m_radioRenotifyPeriod.getSelection() && "".equals(this.m_textRenotifyPeriod.getText())){
			this.m_textRenotifyPeriod.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textRenotifyPeriod.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		m_textRenotifyPeriod.setEnabled(m_radioRenotifyPeriod.getSelection());
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param notify 設定値として用いる通知情報
	 */
	public void setInputData(NotifyInfo notify) {

		if(notify != null){

			// イベントの抑制
			if (notify.getRenotifyType() != null && notify.getRenotifyType().intValue() == RenotifyTypeConstant.TYPE_PERIOD) {
				this.m_radioRenotifyPeriod.setSelection(true);
			} else if (notify.getRenotifyType() != null && notify.getRenotifyType().intValue() == RenotifyTypeConstant.TYPE_NO_NOTIFY) {
				this.m_radioRenotifyNo.setSelection(true);
			} else {
				this.m_radioRenotifyAlways.setSelection(true);
			}
			if (notify.getRenotifyPeriod() != null) {
				this.m_textRenotifyPeriod.setText(notify.getRenotifyPeriod().toString());
			}
		}
		update();
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
			// 抑制
			if (this.m_radioRenotifyPeriod.getSelection()) {
				info.setRenotifyType(RenotifyTypeConstant.TYPE_PERIOD);
			}
			else if(this.m_radioRenotifyNo.getSelection()) {
				info.setRenotifyType(RenotifyTypeConstant.TYPE_NO_NOTIFY);
			}
			else {
				info.setRenotifyType(RenotifyTypeConstant.TYPE_ALWAYS_NOTIFY);
			}

			try {
				info.setRenotifyPeriod(Integer.parseInt(this.m_textRenotifyPeriod.getText()));
			} catch (NumberFormatException e) {
				if (this.m_radioRenotifyPeriod.getSelection()) {
					return this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.notify.14"));
				}
			}
		}
		return null;
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enable) {
		this.m_radioRenotifyAlways.setEnabled(enable);
		this.m_radioRenotifyPeriod.setEnabled(enable);
		this.m_radioRenotifyNo.setEnabled(enable);
		if (enable) {
			this.m_textRenotifyPeriod.setEnabled(this.m_radioRenotifyPeriod.getSelection());
		} else {
			this.m_textRenotifyPeriod.setEnabled(false);
		}
		// 必須入力項目を可視化
		//this.update();
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
