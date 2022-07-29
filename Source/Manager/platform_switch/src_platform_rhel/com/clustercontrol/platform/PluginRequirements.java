/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

/**
 * OSごとに異なるPlug-in要件を定義するクラス<br/>
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
package com.clustercontrol.platform;

import java.util.Set;

/**
 * プラットフォームごとにプラグインの活性化要件を定義するクラス
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class PluginRequirements {
	/**
	 * コア機能(WebServiceCorePlugin)を機能するのに必要なキー名のセット
	 * 
	 * Linux版の場合は特にキーが不要
	 * Windows版の場合、全機能enterpriseキーが必要
	 */
	public static final Set<String> CORE_REQUIRED_KEYS = null;
}
