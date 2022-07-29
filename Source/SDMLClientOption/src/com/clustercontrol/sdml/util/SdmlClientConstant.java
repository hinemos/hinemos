/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

public class SdmlClientConstant {
	/** ダイアログの最背面レイヤのカラム数 */
	public static final int DIALOG_WIDTH = 7;
	/** タイトルラベルのカラム数 */
	public static final int TITLE_WIDTH = 2;
	/** テキストフォームのカラム数 */
	public static final int FORM_WIDTH = 5;
	/** フォーム横ボタンのカラム数 */
	public static final int BUTTON_WIDTH = 1;
	/** フォーム横にボタンを設置する場合のカラム数 */
	public static final int FORM_WIDTH_WITH_BTN = FORM_WIDTH - BUTTON_WIDTH;

	/** テキストフォームのカラム数（横幅いっぱい広げない場合の個別設定） */
	public static final int FORM_WIDTH_1 = 2;
	public static final int FORM_WIDTH_1_SPACE = FORM_WIDTH - FORM_WIDTH_1;
}
