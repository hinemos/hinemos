/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import java.util.HashSet;
import java.util.Set;

/**
 * ライセンスキーの定数クラス
 */
public class ActivationKeyConstant {

	/**
	 * ライセンスキーのファイル名(YYYY_MM_enterprise)
	 */
	public static final String TYPE_ENTERPRISE = "enterprise";
	
	/**
	 * ライセンスキーのファイル名(YYYY_MM_xcloud)
	 */
	public static final String TYPE_XCLOUD = "xcloud";

	/**
	 * 正式版のライセンスキーの先頭6桁の数値
	 */
	public static final int ACTIVATION_KEY_YYYYMM = 300012;
		
	/**
	 * 評価版だった場合のキーの末尾文字列
	 */
	public static final String EVALUATION_SUFFIX = "_evaluation";
	
	/**
	 * 期限切れの評価版だった場合のキーの末尾文字列
	 */
	public static final String EVALUATION_EXPIRED_SUFFIX = "_evaluation_expired";
	
	/**
	 * 利用できるオプション一覧取得
	 * 
	 * @return 利用できるオプションの一覧
	 */
	public static Set<String> getOptions(){
		Set<String> options = new HashSet<>();
		options.add(TYPE_ENTERPRISE);
		options.add(TYPE_XCLOUD);
		return options;
	}

}
