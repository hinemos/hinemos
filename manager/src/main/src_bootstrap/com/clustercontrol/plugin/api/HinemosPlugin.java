/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.api;

import java.util.Set;

/**
 * Hinemos Managerに追加するプラグインが実装すべきインタフェース<br/>
 * <br/>
 * 1. このインタフェースを実装した具象クラスを作成する。<br/>
 * 2. META-INF/services/com.clustercontrol.plugin.api.HinemosPluginというファイルを生成する。<br/>
 * 3. com.clustercontrol.plugin.api.HinemosPluginファイルには、1行1クラス名で具象クラス名を追記する。<br/>
 * 4. 具象クラスおよびMETA-INFを含むjarファイルを作成する。<br/>
 * 5. ${HINEMOS_HOME}/pluginsディレクトリ内にそのjarファイルを配置し、Hinemos Managerを再起動する。<br/>
 * 
 */
public interface HinemosPlugin {

	// Pluginの状態を示すステータス種別
	// [状態遷移]
	// NULL -(create)-> DEACTIVATED -(activate)-> ACTIVATED
	// ACTIVATED -(deactivated)-> DEACTIVATED -(destroy) -> NULL
	public static enum PluginStatus { NULL, DEACTIVATED, ACTIVATED };

	/**
	 * プラグインが依存するHinemosPlugin具象クラス名のセットを返すメソッド<br/>
	 * この依存関係に基づいて、各HinemosPluginがactivateされる。<br/>
	 * 基本的なactivate順序の考え方を以下に記す。<br/>
	 *   1. 全ての基盤となるモジュール系 (Log4jReloadPluginなど)
	 *   2. バックエンドの処理モジュール系(AsyncWorkerPlugin, SharedTablePluginなど)
	 *   3. フロントエンドの処理モジュール系（SnmpTrapPlugin, SystemLogPluginなど)
	 *   4. UIの解放モジュール系(WebServiceXXXPluginなど)
	 *   5. 自発的に動作するモジュール系(SchedulerPlugin, SelfCheckPluginなど)
	 * @return HinemosPlugin具象クラス名のセット
	 */
	Set<String> getDependency();

	/**
	 * プラグインの起動に必要にキー名のセットを返すメソッド<br/>
	 * 
	 * @return キー名のセット
	 */
	Set<String> getRequiredKeys();

	/**
	 * プラグインの生成処理を行うメソッド<br/>
	 * (他のプラグインがactivateする際に参照する可能性のあるリソース生成、など)<br/>
	 */
	void create();

	/**
	 * プラグインの活性化処理を行うメソッド<br/>
	 * (スケジューラ・スレッドの開始、など)</br>
	 */
	void activate();

	/**
	 * プラグインの非活性化処理を行うメソッド<br/>
	 */
	void deactivate();

	/**
	 * プラグインの廃棄処理を行うメソッド<br/>
	 */
	void destroy();

}
