/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.composite.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 時刻用VerifyListenerクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class TimeVerifyListener implements VerifyListener {
	private static final String NUMBER_CHAR = "0123456789:-";
	private static final String COLON_CHAR = ":";
	private static final Integer LENGTH = Integer.valueOf(9);

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
			//数字以外は無効にする
			else if (NUMBER_CHAR.indexOf(Character.toString(e.character)) == -1) {
				e.doit = false;

				return;
			}
			//':'の場合はすでに数字が入力されていたら無効にする
			else if (COLON_CHAR.indexOf(Character.toString(e.character)) == 0) {
				if (text.getText().length() == 0) {
					e.doit = false;
				}

				return;
			}
			else{
				//文字追加
				input.replace(e.start, e.end, e.text);
			}
		}

		//範囲チェック
		checkRange(e, input.toString());
	}

	/**
	 * 範囲チェック
	 *
	 * @param e イベント
	 * @param inputText 入力文字列
	 */
	private void checkRange(VerifyEvent e, String inputText){

		//入力文字列の文字列長をチェック
		//try {
		//if(inputText.getBytes("UTF-8").length > LENGTH){
		if(inputText.length() > LENGTH){
			//入力は無効
			e.doit = false;

			String[] args = { LENGTH.toString() };

			//エラーメッセージ
			MessageDialog.openWarning(
					null,
					Messages.getString("message.hinemos.1"),
					Messages.getString("message.hinemos.7", args ));
		}
		/*} catch (UnsupportedEncodingException e1) {
			//入力は無効
			e.doit = false;
		}*/
	}
}
