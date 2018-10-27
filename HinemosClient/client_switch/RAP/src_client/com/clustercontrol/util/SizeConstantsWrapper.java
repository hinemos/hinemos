/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

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
	public static double JOBKICK_RUN_LABEL_LINE_INITIALHEIGHT = 16;
	// ラベル １行の高さ（２行目以降）
	public static double JOBKICK_RUN_LABEL_LINE_HEIGHT = 12.25;
	// ラベル １行の文字数
	public static int JOBKICK_RUN_LABEL_LINE_WORD_COUNT = 42;

	// ラジオボタン １行の高さ（１行目）
	public static double JOBKICK_RUN_RADIO_LINE_INITIALHEIGHT = 19;
	// ラジオボタン １行の高さ（２行目以降）
	public static double JOBKICK_RUN_RADIO_LINE_HEIGHT = 12;
	// ラジオボタン １行の文字数
	public static int JOBKICK_RUN_RADIO_LINE_WORD_COUNT = 40;

	// 全角文字列を１とした場合の半角文字列幅
	public static double JOBKICK_RUN_HALFWIDTH_CHARACTER_WIDTH = 0.5;

	// フォントサイズ
	public static int JOBKICK_FONT_SIZE = 12;
}
