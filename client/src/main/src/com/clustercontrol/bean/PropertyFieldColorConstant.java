/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import org.eclipse.swt.graphics.Color;

/**
 * 未入力のカラムを知らせる文字色を規定する定数クラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class PropertyFieldColorConstant {
	/** 未入力の項目 **/
	public static final Color COLOR_EMPTY= new Color(null, 128, 128, 128);

	/** 入力済みの項目 **/
	public static final Color COLOR_FILLED = null;

}
