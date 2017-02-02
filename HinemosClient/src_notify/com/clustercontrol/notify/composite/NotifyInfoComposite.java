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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.notify.NotifyRelationInfo;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * アプリケーション付き通知ID一覧コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「通知ID」 ラベル（親）</dd>
 *  <dd>「通知ID一覧」 フィールド（親）</dd>
 *  <dd>「選択」 ボタン（親）</dd>
 *  <dd>「アプリケーション」 テキストボックス</dd>
 * </dl>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class NotifyInfoComposite extends NotifyIdListComposite {

	/** アプリケーション ラベル。 */
	private Label labelApplication = null;

	/** アプリケーション ラベル文字列。 */
	private Text textApplication = null;

	/** 入力値チェック用 */
	protected ValidateResult validateResult = null;


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
	 * @see #initialize(Composite)
	 */
	public NotifyInfoComposite(Composite parent, int style) {
		super(parent, style, true);

		this.initialize(parent);
	}

	public NotifyInfoComposite(Composite parent, int style, int notifyIdType) {
		super(parent, style, true, notifyIdType);

		this.initialize(parent);
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param text アプリケーション ラベル文字列
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize(Composite)
	 */
	public NotifyInfoComposite(Composite parent, int style, String text) {
		super(parent, style, text);

		this.initialize(parent);
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize(Composite parent) {

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		/*
		 * アプリケーションID
		 */
		// ラベル
		this.labelApplication = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "application", labelApplication);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.labelApplication.setLayoutData(gridData);
		this.labelApplication.setText(Messages.getString("application") + " : ");
		// テキスト
		this.textApplication = new Text(this, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, null, textApplication);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textApplication.setLayoutData(gridData);
		this.textApplication.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		update();
	}

	/**
	 * コンポジットを更新します。<BR>
	 */
	@Override
	public void update() {
		// 必須入力項目を明示する
		if(this.textApplication.getEnabled() && "".equals(this.textApplication.getText())){
			this.textApplication.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textApplication.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}


	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.textApplication.setEnabled(enabled);

		this.update();
	}

	/**
	 * アプリケーションを返します。
	 *
	 * @return アプリケーション
	 */
	public String getApplication() {

		return this.textApplication.getText();
	}

	/**
	 * アプリケーションを設定します。
	 */
	public void setApplication(String string) {
		this.textApplication.setText(string);
	}

	/**
	 * 引数で指定された監視情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は認証結果を返します。
	 * 不正ではない場合は、<code>null</code>を返します。
	 *
	 * @param info	監視情報
	 * @return	検証結果
	 */
	public ValidateResult createInputData(MonitorInfo info){

		this.validateResult = null;
		if(info != null){

			if(getNotify() != null && getNotify().size() != 0){
				//コンポジットから通知情報を取得します。
				List<NotifyRelationInfo> notifyRelationInfoList = info.getNotifyRelationList();
				notifyRelationInfoList.clear();
				if (this.getNotify() != null) {
					notifyRelationInfoList.addAll(this.getNotify());
				}
			}

			// アプリケーションの設定
			if(this.getApplication() != null && !this.getApplication().equals("")){
				info.setApplication(this.getApplication());
			}

		}
		return this.validateResult;
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 */
	protected void setValidateResult(String id, String message) {

		this.validateResult = new ValidateResult();
		this.validateResult.setValid(false);
		this.validateResult.setID(id);
		this.validateResult.setMessage(message);

	}

}
