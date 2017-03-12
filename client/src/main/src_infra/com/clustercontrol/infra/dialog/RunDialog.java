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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 実行確認ダイアログクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class RunDialog extends CommonDialog {

	private Button allRunButton = null;
	private boolean allRun = false;
	private String message = null;
	
	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public RunDialog(Shell parent, String message) {
		super(parent);
		this.message = message;
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
		shell.setText(Messages.getString("confirmed"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 1;
		parent.setLayout(layout);
		
		// ラベル
		Label label = new Label(parent, SWT.NONE);
		label.setText(message);
		
		// ダミーラベル
		new Label(parent, SWT.NONE);

		/*
		 * 以降のログイン情報を同じ設定にする
		 */
		allRunButton = new Button(parent, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "allRunButton", allRunButton);
		allRunButton.setText(Messages.getString("message.infra.all.run"));
		
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(600, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("run");
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
	 * ＯＫボタンが押された場合に呼ばれるメソッドで、入力値チェックを実施します。
	 * <p>
	 *
	 * エラーの場合、ダイアログを閉じずにエラー内容を通知します。
	 */
	@Override
	protected void okPressed() {
		allRun = allRunButton.getSelection();
		super.okPressed();
	}
	
	public boolean isAllRun() {
		return allRun;
	}
}
