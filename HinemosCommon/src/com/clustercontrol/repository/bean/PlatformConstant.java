/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

/**
 * OS別スコープ判別用のプラットフォーム定数
 * 
 * @version 6.2.0
 * @sice 6.2.0
 * @see com.clustercontrol.repository.factory.SearchNodeBySNMP.stractProperty()
 */
public class PlatformConstant {

	/** デフォルトOS **/
	public final static String DEFAULT = "OTHER";

	/** WINDOWS-OS **/
	public final static String WINDOWS = "WINDOWS";

	/** LINUX-OS **/
	public final static String LINUX = "LINUX";

	/** SOLARIS-OS **/
	public final static String SOLARIS = "SOLARIS";

	/** WINDOWS判定用文字列 **/
	public final static String MATCHER_WINDOWS = ".*indows.*";

	/** LINUX判定用文字列 **/
	public final static String MATCHER_LINUX = ".*inux.*";

	/** SOLARIS判定用文字列 **/
	public final static String MATCHER_SOLARIS1 = ".*SunOS.*";

	/** SOLARIS判定用文字列 **/
	public final static String MATCHER_SOLARIS2 = "Solaris";
}
