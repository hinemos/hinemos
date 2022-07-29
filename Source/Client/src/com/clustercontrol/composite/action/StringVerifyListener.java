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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.util.Messages;

/**
 * 文字列用VerifyListenerクラス<BR>
 *
 * 入力された文字列が指定した文字数より多い場合、エラーを出力する
 *
 * @version 2.2.0
 * @since 2.2.0
 */
public class StringVerifyListener implements VerifyListener {
	private Integer length;

	/**
	 * コンストラクタ
	 *
	 * @param length 文字列長
	 */
	public StringVerifyListener(int length){
		this.length = Integer.valueOf(length);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.VerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
	 */
	@Override
	public void verifyText(VerifyEvent e) {

		//テキストボックスから入力前の文字列を取得
		String textString = null;
		if(e.getSource() instanceof Text){
			Text text = (Text)e.getSource();
			textString = text.getText();
		}else if(e.getSource() instanceof Combo){
			Combo combo = (Combo)e.getSource();
			textString = combo.getText();
		}else{
			throw new InternalError("textString is null.");
		}
		StringBuilder input = new StringBuilder(textString);

		//キー入力以外は有効にする
		if (e.keyCode == 0) {
			//貼り付けの場合もここに入る

			//文字列追加
			input.replace(e.start, e.end, e.text);
		}
		else{
			//BackspaceやDeleteが押されたときは、有効にする
			if (e.character == SWT.BS || e.character == SWT.DEL) {
				//文字削除
				input.delete(e.start, e.end);
			}
			else{
				//文字追加
				//	        	input.insert(e.start, e.character);
				input.replace(e.start, e.end, e.text);
			}
		}

		//文字列長チェック
		checkLength(e, input.toString());
	}

	/**
	 * 文字列長チェック
	 *
	 * @param e イベント
	 * @param inputText 入力文字列
	 */
	private void checkLength(VerifyEvent e, String inputText){

		//入力文字列の文字列長をチェック
		//try {
		//	if(inputText.getBytes("UTF-8").length > length){
		/*
		 * DBMSがSQL-ASCII～UTF-8に変更になったので、
		 * UTF8のバイト数を換算する必要はなくなりました。
		 * */
		if(inputText.length() > length){
			//入力は無効
			e.doit = false;

			String[] args = { this.length.toString() };

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
