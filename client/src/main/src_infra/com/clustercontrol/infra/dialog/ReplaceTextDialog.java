/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 環境構築[ログイン情報入力]ダイアログクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ReplaceTextDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog(ReplaceTextDialog.class); 
	
	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = 4;

	/** 入力値を保持するオブジェクト。 */
	private List<Object> m_inputData = null;

	/** 入力値の正当性を保持するオブジェクト。 */
	private ValidateResult m_validateResult = null;

	/** ユーザ */
	private Text m_txtSearch = null;

	/** パスワード */
	private Text m_txtReplace = null;

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param identifier 変更する文字列監視の判定情報の識別キー
	 */
	public ReplaceTextDialog(Shell parent) {
		super(parent);
	}
	
	public ReplaceTextDialog(Shell parent, List<Object> inputData) {
		super(parent);
		this.m_inputData = inputData;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 * @see #setInputData(Pattern)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("infra.management.replacement.words"));

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
		 * 検索用文字列
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("infra.management.search.words") + " : ");
		// テキスト
		this.m_txtSearch = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "search", m_txtSearch);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_txtSearch.setLayoutData(gridData);
		this.m_txtSearch.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 置換用文字列
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("infra.management.replacement.words") + " : ");
		// テキスト
		this.m_txtReplace = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "replace", m_txtReplace);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_txtReplace.setLayoutData(gridData);
		this.m_txtReplace.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
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

		m_log.debug("m_inputData=" + m_inputData);
		this.setInputData(m_inputData);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を可視化
		if("".equals(this.m_txtSearch.getText())){
			this.m_txtSearch.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtSearch.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_txtReplace.getText())){
			this.m_txtReplace.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtReplace.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}


	/**
	 * 入力値を保持した文字列監視の判定情報を返します。
	 *
	 * @return 判定情報
	 */
	public List<Object> getInputData() {
		return this.m_inputData;
	}

	/**
	 * 引数で指定された判定情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる判定情報
	 */
	public void setInputData(List<Object> inputData) {
		if (inputData == null || inputData.size() < 2) {
			return;
		}
		m_txtSearch.setText((String)inputData.get(0));
		m_txtReplace.setText((String)inputData.get(1));
		m_inputData = inputData;
	}

	/**
	 * 引数で指定された判定情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 判定情報
	 *
	 * @see #setValidateResult(String, String)
	 */
	private List<Object> createInputData() {
		List<Object> data = new ArrayList<Object>();

		data.add(m_txtSearch.getText());
		data.add(m_txtReplace.getText());
		data.add(""); // dummy
		
		return data;
	}

	/**
	 * 無効な入力値をチェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see #createInputData()
	 */
	@Override
	protected ValidateResult validate() {
		m_validateResult = null;

		// ユーザ
		if ("".equals((this.m_txtSearch.getText()).trim())){
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.infra.specify.item", new Object[]{Messages.getString("infra.management.search.words")}));
		}

		// パスワード
		if ("".equals((this.m_txtReplace.getText()).trim())){
			this.setValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.infra.specify.item", new Object[]{Messages.getString("infra.management.replacement.words")}));
		}
		
		if (this.m_validateResult == null) {
			return super.validate();
		} else {
			return m_validateResult;
		}
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
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 */
	protected void setValidateResult(String id, String message) {

		this.m_validateResult = new ValidateResult();
		this.m_validateResult.setValid(false);
		this.m_validateResult.setID(id);
		this.m_validateResult.setMessage(message);
	}

	/**
	 * 入力値の判定を行います。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		this.m_inputData = createInputData();
		if(this.m_inputData != null){
			result = true;
		}

		return result;
	}
}
