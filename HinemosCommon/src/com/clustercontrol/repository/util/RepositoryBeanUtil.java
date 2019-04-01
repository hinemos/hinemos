/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import com.clustercontrol.repository.bean.PlatformConstant;

/**
 * リポジトリ関連のBeanを扱うUtil
 * 
 * @version 6.2.0
 * @sice 6.2.0
 */
public class RepositoryBeanUtil {

	/**
	 * プラットフォーム名を取得.<br>
	 * 
	 * @param checkString
	 *            判定元の文字列(SNMPコマンドで取得したDescription等)
	 * @param solarisFlag
	 *            プラットフォーム一覧(QueryUtil.getCollectorPlatformMstPK("SOLARIS"))
	 *            にSOLARISが存在するか.
	 * @return 判定したプラットフォーム、以下参照.
	 * @see com.clustercontrol.repository.bean.PlatformConstant
	 * @see com.clustercontrol.repository.factory.SearchNodeBySNMP.
	 *      stractProperty()
	 */
	public static String getPlatform(String checkString, boolean solarisFlag) {

		// プラットフォーム名は、Windows, Linux, Solaris以外はOtherとする
		String platform = PlatformConstant.DEFAULT;
		if (checkString.matches(PlatformConstant.MATCHER_WINDOWS)) {
			platform = PlatformConstant.WINDOWS;
		} else if (checkString.matches(PlatformConstant.MATCHER_LINUX)) {
			platform = PlatformConstant.LINUX;
		} else if (solarisFlag && (checkString.matches(PlatformConstant.MATCHER_SOLARIS1)
				|| checkString.matches(PlatformConstant.MATCHER_SOLARIS2))) {
			// プラットフォーム一覧にSOLARISが存在するときのみ、
			// デバイスサーチやノードサーチで、プラットフォームにSOLARISが入る。
			platform = PlatformConstant.SOLARIS;
		} else {
			platform = PlatformConstant.DEFAULT;
		}

		return platform;
	}

}
