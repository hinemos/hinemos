/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent;

import com.clustercontrol.version.util.VersionUtil;

/**
 * Hinemosエージェントのバージョンを定義するクラス
 */
public class AgentVersion {
	/**
	 * TODO ※バージョンアップ時に必ず更新すること！
	 * 
	 * Hinemosエージェントのバージョンを定義する定数。<BR>
	 * AgentInfoに格納されマネージャに送信されます。<BR>
	 * {メジャーバージョン}.{マイナーバージョン}の形式で定義してください。<BR>
	 */
	public static final String VERSION = VersionUtil.getVersionMajor();
}
