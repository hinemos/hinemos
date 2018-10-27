/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

public class TargetPlatformUtil {
	
	/**
	 * RichClientPlatformとして実行している場合にtrueを返す。
	 * 
	 * <p>濫用するとツギハギコードになると思われるので、
	 * 別のアプローチ(例えば、ターゲットプラットフォームごとの処理を別クラスに抽出して、
	 * client_switchで切り替える、等)が適切ではないかを十分検討した上で、使用すべき。
	 * 
	 * @return RCPとして実行しているならtrue、そうでなければfalse。
	 */
	public static boolean isRCP() {
		return false;
	}

	/**
	 * RemoteApplicationPlatformとして実行している場合にtrueを返す。
	 * 
	 * <p>濫用するとツギハギコードになると思われるので、
	 * 別のアプローチ(例えば、ターゲットプラットフォームごとの処理を別クラスに抽出して、
	 * client_switchで切り替える、等)が適切ではないかを十分検討した上で、使用すべき。
	 * 
	 * @return RAPとして実行しているならtrue、そうでなければfalse。
	 */
	public static boolean isRAP() {
		return true;
	}

}