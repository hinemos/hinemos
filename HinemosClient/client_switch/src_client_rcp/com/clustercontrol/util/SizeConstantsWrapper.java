/**********************************************************************
 * Copyright (C) 2016 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package com.clustercontrol.util;

/**
 * RCPとRAPでサイズを切り替えるためのクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class SizeConstantsWrapper {

	/** ジョブ実行契機 */
	// ラベル １行の高さ（１行目）
	public static double JOBKICK_RUN_LABEL_LINE_INITIALHEIGHT = 14;
	// ラベル １行の高さ（２行目以降）
	public static double JOBKICK_RUN_LABEL_LINE_HEIGHT = 13;
	// ラベル １行の文字数
	public static int JOBKICK_RUN_LABEL_LINE_WORD_COUNT = 43;

	// ラジオボタン １行の高さ（１行目）
	public static double JOBKICK_RUN_RADIO_LINE_INITIALHEIGHT = 15;
	// ラジオボタン １行の高さ（２行目以降）
	public static double JOBKICK_RUN_RADIO_LINE_HEIGHT = 12;
	// ラジオボタン １行の文字数
	public static int JOBKICK_RUN_RADIO_LINE_WORD_COUNT = 41;

	// 全角文字列を１とした場合の半角文字列幅
	public static double JOBKICK_RUN_HALFWIDTH_CHARACTER_WIDTH = 0.5;

	// フォントサイズ
	public static int JOBKICK_FONT_SIZE = 9;
}
