/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.util.HashSet;
import java.util.Set;

import com.clustercontrol.util.KeyCheck;

/**
 * オプション管理用ユーティリティクラス
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class OptionManager {
	/* 有効なオプション(キーチェックに成功したもの)一覧 */
	private static final Set<String> options = new HashSet<>();

	public static Set<String> getOptions() {
		synchronized (OptionManager.class) {
			return options;
		}
	}

	public static boolean has(String option) {
		synchronized (OptionManager.class) {
			return options.contains(option);
		}
	}

	public synchronized static void add(String option) {
		synchronized (OptionManager.class) {
			options.add(option);
		}
	}

	/**
	 * 以下のオプション使用可否チェック<br>
	 *  ・jobmap<br>
	 *  ・nodemap<br>
	 *  ・reporting<br>
	 *  ・utility<br>
	 * @return
	 */
	public static boolean checkEnterprise() {
		return has(KeyCheck.TYPE_ENTERPRISE);
	}
	
	/**
	 * 以下のオプション使用可否チェック<br>
	 *  ・vmcloud<br>
	 * @return
	 */
	public static boolean checkxCloud() {
		return has(KeyCheck.TYPE_XCLOUD);
	}

}
