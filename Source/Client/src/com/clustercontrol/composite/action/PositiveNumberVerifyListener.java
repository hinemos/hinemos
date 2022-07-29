/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.composite.action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.util.WidgetTestUtil;

/**
 * 正の数値用VerifyListenerクラス<BR>
 *
 * @version 2.2.0
 * @since 2.0.0
 */
public class PositiveNumberVerifyListener extends NumberVerifyListener {

	/**
	 * コンストラクタ
	 */
	public PositiveNumberVerifyListener(){
		super();
	}

	/**
	 * コンストラクタ
	 *
	 * @param low 下限
	 * @param high 上限
	 */
	public PositiveNumberVerifyListener(int low, int high){
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

		//数字以外が入力されたら無効にする
		if (!input.toString().matches("\\d*")) {
			e.doit = false;
			return;
		}

		//範囲チェック
		checkRange(e, input.toString());
	}
}
