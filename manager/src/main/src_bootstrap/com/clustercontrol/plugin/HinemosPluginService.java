/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.plugin.api.HinemosPlugin.PluginStatus;

/**
 * HinemosPlugin全体の制御を行うクラス(Singleton).
 * 
 */
public class HinemosPluginService {

	public static final Log log = LogFactory.getLog(HinemosPluginService.class);

	private static final HinemosPluginService _instance = new HinemosPluginService();

	// プラグイン名とプラグインインスタンスとのマップ情報
	private final Map<String, HinemosPlugin> pluginMap = new ConcurrentHashMap<String, HinemosPlugin>();
	// プラグイン名とステータスのマップ情報
	private final Map<String, PluginStatus> pluginStatusMap = new ConcurrentHashMap<String, PluginStatus>();

	// activateした順序を保持しておく配列
	private final List<HinemosPlugin> pluginActivatationOrder = new ArrayList<HinemosPlugin>();

	/**
	 * privateコンストラクタ(Singletonクラス).
	 */
	private HinemosPluginService() {
		String pluginDir = System.getProperty("hinemos.manager.home.dir") + File.separator + "plugins";
		try {
			// /opt/hinemos/plugins配下のjarファイルをクラスパスに追加する
			ClassUtils.addDirToClasspath(new File(pluginDir));
			log.info("initialized plugin directory : " + pluginDir);
		} catch (Exception e) {
			log.warn("classpath configure failure. (pluginDir = " + pluginDir + ")", e);
		}

		ServiceLoader<HinemosPlugin> serviceLoader = ServiceLoader.load(HinemosPlugin.class);
		Iterator<HinemosPlugin> itr = serviceLoader.iterator();
		while (itr.hasNext()) {
			HinemosPlugin plugin = itr.next();

			pluginMap.put(plugin.getClass().getName(), plugin);
			pluginStatusMap.put(plugin.getClass().getName(), PluginStatus.NULL);
		}

	}

	/**
	 * JVM内で単一のHinemosPluginServiceインスタンスを返す.
	 * 
	 * @return HinemosPluginServiceインスタンス
	 */
	public static HinemosPluginService getInstance() {
		return _instance;
	}

	/**
	 * HinemosPluginの生成処理を行うメソッド.
	 */
	public synchronized void create() {
		for (Map.Entry<String, HinemosPlugin> set : pluginMap.entrySet()) {
			try {
				log.info("creating plugin - " + set.getKey());
				pluginStatusMap.put(set.getValue().getClass().getName(), PluginStatus.DEACTIVATED);
				set.getValue().create();
			} catch (Throwable t) {
				log.warn("plugin creation failure.", t);
			}
		}
	}

	/**
	 * HinemosPluginの活性化処理を順次行うメソッド.
	 * 
	 * <p>
	 * {@link com.clustercontrol.plugin.api.HinemosPlugin.getDependency()}にて定義される
	 * プラグイン間の依存関係に基づいて、プラグインが活性化される順序が決定される。
	 * </p>
	 */
	public synchronized void activate() {
		List<HinemosPlugin> waitingPlugin = new ArrayList<HinemosPlugin>(pluginMap.values());
		pluginActivatationOrder.clear();

		while (true) {
			boolean nonActivatable = true;

			for (HinemosPlugin plugin : waitingPlugin) {
				if (pluginStatusMap.get(plugin.getClass().getName()) == PluginStatus.ACTIVATED) {
					// 既にactivateが呼ばれたプラグインは処理しない
					continue;
				}

				if (isPluginActivatable(plugin)) {
					// 依存するプラグインが全て活性化されたプラグインのactivateを呼び出す
					log.info("activating plugin - " + plugin.getClass().getName());

					nonActivatable = false;
					pluginStatusMap.put(plugin.getClass().getName(), PluginStatus.ACTIVATED);
					pluginActivatationOrder.add(plugin);

					try {
						plugin.activate();
					} catch (Throwable t) {
						log.warn("plugin activation failure.", t);
					}
				}
			}

			// 活性化したプラグインを活性化待ちプラグインのリストから除去する
			waitingPlugin.removeAll(pluginActivatationOrder);

			if (nonActivatable) {
				if (waitingPlugin.size() == 0) {
					// 全てのプラグインが活性化された場合は終了する
					break;
				}

				// 依存関係が不正に定義されており、活性化できるプラグインが存在しない場合はWARNメッセージを出力して終了する
				for (HinemosPlugin plugin : waitingPlugin) {
					log.warn(String.format("plugin %s is not started because of dependency : %s", plugin.getClass().getName(), plugin.getDependency()));
				}
				log.warn("some plugin may be not activated bevcause of dependency.");
				break;
			}
		}
	}

	/**
	 * 依存しているHinemosPlugin状態を確認し、HinemosPluginが活性化条件が満たされたかどうかを返すメソッド.
	 * 
	 * @param plugin 活性化可能かどうかを調べるHinemosPluginインスタンス
	 * @return true : 活性化可能、false : 活性化不可
	 */
	private boolean isPluginActivatable(HinemosPlugin plugin) {
		boolean activatable = true;

		log.debug("checking dependency of " + plugin.getClass().getName() + " : " + plugin.getDependency());

		for (String className : plugin.getDependency()) {
			log.debug("checking plugin status of " + className + " : " + pluginStatusMap.get(className));
			if (pluginStatusMap.get(className) != PluginStatus.ACTIVATED) {
				activatable = false;
				break;
			}
		}

		return activatable;
	}

	/**
	 * HinemosPluginの非活性化処理を順次行うメソッド.
	 * 
	 * <p>
	 * {@link com.clustercontrol.plugin.HinemosPluginService.activate()}により活性化された順序と逆の順序で、
	 * 各プラグインのdeactivateメソッドが呼び出される。
	 * </p>
	 */
	public synchronized void deactivate() {
		List<HinemosPlugin> pluginDeactivationOrder = new ArrayList<HinemosPlugin>(pluginActivatationOrder);
		Collections.reverse(pluginDeactivationOrder);

		for (HinemosPlugin plugin : pluginDeactivationOrder) {
			try {
				log.info("deactivating plugin - " + plugin.getClass().getName());
				pluginStatusMap.put(plugin.getClass().getName(), PluginStatus.DEACTIVATED);
				plugin.deactivate();
			} catch (Throwable t) {
				log.warn("plugin deactivation failure.", t);
			}
		}
	}

	/**
	 * HinemosPluginの廃棄処理を行うメソッド.
	 */
	public synchronized void destroy() {
		for (Map.Entry<String, HinemosPlugin> set : pluginMap.entrySet()) {
			try {
				log.info("destroying plugin - " + set.getKey());
				pluginStatusMap.put(set.getValue().getClass().getName(), PluginStatus.NULL);
				set.getValue().destroy();
			} catch (Throwable t) {
				log.warn("plugin destroying failure.", t);
			}
		}
	}
}
