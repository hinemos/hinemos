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

package com.clustercontrol.composite.action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.util.WidgetTestUtil;


/**
 * 正の実数値用VerifyListenerクラス
 *
 * @version 2.2.0
 * @since 2.0.0
 */
public class PositiveRealNumberVerifyListener extends RealNumberVerifyListener {

	/**
	 * コンストラクタ
	 */
	public PositiveRealNumberVerifyListener(){
		super();
	}

	/**
	 * コンストラクタ
	 *
	 * @param low 下限
	 * @param high 上限
	 */
	public PositiveRealNumberVerifyListener(double low, double high){
		super(low, high);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.VerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
	 */
	@Override
	public void verifyText(VerifyEvent e) {

		//テキストボックスから入力前の文字列を取得
		Text text = (Text)e.getSource();
		WidgetTestUtil.setTestId(this, null, text);
		StringBuilder input = new StringBuilder(text.getText());

		//キー入力以外は有効にする
		if (e.keyCode == 0) {
			//貼り付けの場合もここに入る

			//文字列追加
			input.replace(e.start, e.end, e.text);
		}
		else {
			//BackspaceやDeleteが押されたときは、有効にする
			if (e.character == SWT.BS || e.character == SWT.DEL) {
				input.delete(e.start, e.end);
			}
			else{
				//文字追加
				input.replace(e.start, e.end, e.text);
			}
		}

		//'.'の場合はすでに数字が入力されていない または すでに'.'が入力されていたら無効にする
		if (e.text.length() == 1 && DOT_CHAR.indexOf(e.text) == 0) {
			if (!input.toString().matches("\\d+\\.?\\d*(E?|E-?)\\d*")) {
				e.doit = false;
			}
			return;
		}
		//'-'の場合は'E'の後以外は無効
		else if (e.text.length() == 1 && MINUS_CHAR.indexOf(e.text) == 0) {
			if (!input.toString().matches("\\d*\\.?\\d*(E?|E-?)\\d*")) {
				e.doit = false;
			}
			return;
		}
		//'E'の場合はすでに数字が入力されていない または すでに'E'が入力されていたら無効にする
		else if (e.text.length() == 1 && EXP_CHAR_U.indexOf(e.text) == 0) {
			if (!input.toString().matches("\\d*\\.?\\d+(E?|E-?)\\d*")) {
				e.doit = false;
			}
			return;
		}
		else{
			if (!input.toString().matches("(\\d*|\\d+\\.\\d+)(\\d*|E\\d+|E-\\d+)")) {
				e.doit = false;
				return;
			}
		}

		//範囲チェック
		checkRange(e, input.toString());
	}
}
