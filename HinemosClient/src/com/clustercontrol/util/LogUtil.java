/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.ws.WebFault;

/**
 * ログ出力に関するユーティリティです。
 *
 * @since 6.2.0
 */
public class LogUtil {
	
	/**
	 * ログ出力に使用できるように、例外のスタックトレースを文字列化して返します。
	 * ただし、例外の種類がWebFault(HinemosマネージャのAPIから返されるもの)である場合には
	 * スタックトレースではなく、例外クラス名と例外メッセージのみを返します。
	 * (WebFaultに関しては、スタックトレースがほとんど意味を持たないため。)
	 * 
	 * @param leader 先頭に付与するメッセージ文字列。
	 * @param throwable 発生した例外。
	 */
	public static String filterWebFault(String leader, Throwable throwable) {
		Class<?> cls = throwable.getClass();
		WebFault annt = cls.getAnnotation(WebFault.class);
		if (annt == null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			throwable.printStackTrace(pw);
			return leader + sw.toString();
		} else {
			return leader + cls.getName() + ", " + throwable.getMessage();
		}
	}
}
