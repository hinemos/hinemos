/*
 * Copyright (c) 2020 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logfile.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ログファイル監視の改行コードを定数として格納するクラス<BR>
 * 
 */
//FIXME 改善要望 http://172.16.54.255/redmine/issues/1146
public class LogfileLineSeparatorConstant {

	/** LF */
	public static final String LF = "LF";

	/** CR */
	public static final String CR = "CR";

	/** CRLF */
	public static final String CRLF = "CRLF";

	/** リスト **/
	public static final List<String> LIST
		= Collections.unmodifiableList(new ArrayList<>(Arrays.asList(LF, CR, CRLF)));
}