/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.composite;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.util.Messages;

/**
 * テキストハイライトコンポジットクラス<BR>
 * 
 * @version 4.0.0
 * @since 2.4.0
 */
public class StyledTextComposite extends Composite {
	private StyledText textCanvas;
	private String[] keywords;
	private Color color;
	private Color okColor;
	private Color ngColor;

	public StyledTextComposite(Composite parent, int style) {
		super(parent, SWT.NONE);
		this.setLayout(new FillLayout());
		textCanvas = new StyledText(this, style);

		// 選択可/不可時の色を指定する
		okColor = new Color(parent.getDisplay(), new RGB(255,255,255));
		ngColor = parent.getBackground();

		// 設定されている文字列がハイライトされるようにイベントリスナを設定
		textCanvas.addLineStyleListener(new LineStyleListener(){

			@Override
			public void lineGetStyle(LineStyleEvent event) {
				if(keywords == null){
					return;
				}

				String lineText = event.lineText;
				if (lineText == null) {
					return;
				}
				ArrayList<StyleRange> styleRanges = new ArrayList<StyleRange>();

				TreeMap<Integer, Integer> paramPtr = new TreeMap<Integer, Integer>();

				for(int i=0; i<keywords.length; i++){
					int index = 0;
					int fromIndex = 0;

					String keyword = keywords[i];

					// キーワードに一致する文字列が含まれる場合はその位置を取得する
					while ((index = lineText.indexOf(keyword, fromIndex)) != -1) {
						paramPtr.put(event.lineOffset + index, keyword.length());
						fromIndex = index + keyword.length();
					}
				}

				// workaround for SWT bug 212851 StyledText - getStyleRanges(int start, int length) returns wrong style range
				// http://mail.eclipse.org/viewcvs/index.cgi/org.eclipse.swt/Eclipse%20SWT%20Custom%20Widgets/common/org/eclipse/swt/custom/StyledTextRenderer.java?annotate=1.86
				for (Map.Entry<Integer, Integer> key : paramPtr.entrySet()) {
					StyleRange styleRange = new StyleRange();
					styleRange.start = key.getKey();
					styleRange.length = key.getValue();
					styleRange.foreground = color;
					styleRanges.add(styleRange);
				}

				event.styles = styleRanges.toArray(new StyleRange[styleRanges.size()]);
			}
		});
	}

	/**
	 * ハイライトする文字列の配列を返します。
	 * @return ハイライトする文字列の配列
	 */
	public String[] getKeywords() {
		return keywords;
	}

	/**
	 * ハイライトする文字列を設定します。
	 * @param keywords ハイライトする文字列の配列
	 */
	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	/**
	 * 入力エリアに入力されているテキストを返します。
	 * @return 入力エリアに入力されているテキスト
	 */
	public String getText() {
		return  textCanvas.getText();
	}

	/**
	 * 入力エリアに文字列を設定します。
	 * @param text 入力エリアに設定する文字列
	 */
	public void setText(String text) {
		this.textCanvas.setText(text);
	}

	/**
	 * ハイライトされる文字の色を返します。
	 * @return ハイライトされる文字の色
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * ハイライトされる文字の色を設定します。
	 * @param color ハイライトされる文字の色
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * リスナークラスを追加します。
	 * @param listener 追加するリスナー
	 */
	public void addVerifyListener(VerifyListener listener) {
		this.textCanvas.addVerifyListener(listener);
	}

	/**
	 * リスナークラスを追加します。
	 * @param listener 追加するリスナー
	 */
	public void addModifyListener(ModifyListener listener) {
		this.textCanvas.addModifyListener(listener);
	}

	/**
	 * 入力値の上限を設定します。
	 * @param upper 入力値の上限
	 */
	public void setInputUpper(int upper) {
		this.textCanvas.addVerifyListener(new StringVerifyListener(upper));
	}

	/**
	 * 選択可/不可を設定します。
	 * @param enable 入力可/不可
	 */
	@Override
	public void setEnabled(boolean enable) {
		this.textCanvas.setEditable(enable);
		if(enable){
			this.textCanvas.setBackground(okColor);
		}
		else {
			this.textCanvas.setBackground(ngColor);
		}
	}

	/**
	 * 背景色を設定します。
	 */
	@Override
	public void setBackground(Color color) {
		this.textCanvas.setBackground(color);
	}


	/**
	 * 文字列用VerifyListenerクラス<BR>
	 * 
	 * 入力された文字列が指定した文字数より多い場合、エラーを出力する(StyledTextComposite用)
	 * 
	 * @version 2.4.0
	 * @since 2.4.0
	 */
	private static class StringVerifyListener implements VerifyListener {
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
			// テキストボックスから入力前の文字列を取得
			StyledText text = (StyledText)e.getSource();
			StringBuilder input = new StringBuilder(text.getText());

			// キー入力以外は有効にする
			if (e.keyCode == 0) {
				//貼り付けの場合もここに入る

				//文字列追加saf
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
					input.replace(e.start, e.end, e.text);
				}
			}

			//入力文字が改行だった場合は、入力を無効とする
			if (input.toString().indexOf('\n') != -1) {
				e.doit = false;
				return;
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
			//	try {
			//if(inputText.getBytes("UTF-8").length > length){
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



}
