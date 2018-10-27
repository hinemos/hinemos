/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import com.clustercontrol.util.Messages;

/**
 * HTTP監視シナリオでページの判定が正常系か異常系かを示すための定数<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class HttpStatusMessage {
	/** 正常(文字列) */
	public static final String STRING_NORMAL = Messages.getString("normal");

	/** 異常(文字列) */
	public static final String STRING_ABNORMAL = Messages.getString("abnormal");

	/** 
	 * 種別を受け取り文字列を返します。
	 * true:処理する
	 * false:処理しない
	 *  */
	public static String typeToString(boolean type){
		if (type) {
			return STRING_ABNORMAL;
		}else {
			return STRING_NORMAL;
		}
	}

	/** 
	 * 文字列を受け取り種別を返します。
	 *  */
	public static boolean stringToType(String string){
		if(string.equals(STRING_ABNORMAL)){
			return true;
		} else  {
			return false;
		}
	}
}