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
 * ジョブ終了状態の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class EndStatusColorConstant {
	/** 正常 */
	public static final Color COLOR_NORMAL = new Color(null, 0, 255, 0);

	/** 警告 */
	public static final Color COLOR_WARNING = new Color(null, 255, 255, 0);

	/** 異常 */
	public static final Color COLOR_ABNORMAL = new Color(null, 255, 0, 0);
}
