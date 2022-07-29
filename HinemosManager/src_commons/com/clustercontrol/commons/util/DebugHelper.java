/*
 * Copyright (c) 2021 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.clustercontrol.HinemosManagerMain;

/**
 * 「デバッグモード」の実装を補助することが目的のユーティリティです。
 * <p>
 * 通常操作では確率的に再現の難しい事象などを意図的に起こすため、
 * 通常は実行されない sleep や throw exception をプログラム内へ組み込みたい場合などに利用できます。
 */
public class DebugHelper {
	private static final Logger logger = Logger.getLogger(DebugHelper.class);

	private static class PropertiesCache {
		File file;
		long timestamp;
		Properties properties;
	}

	private static Path etcDir;
	private static Map<String, PropertiesCache> cache = new ConcurrentHashMap<>();

	public static void setEtcDir(Path path) {
		etcDir = path;
	}

	static Path getEtcDir() {
		synchronized (DebugHelper.class) {
			if (etcDir == null) {
				etcDir = HinemosManagerMain._etcDir;
			}
		}
		return etcDir;
	}

	public static Properties check(String propertiesFileName) {
		if (cache.containsKey(propertiesFileName)) {
			PropertiesCache pc = cache.get(propertiesFileName);
			if (!pc.file.canRead()) return null;
			if (pc.file.lastModified() == pc.timestamp) {
				return pc.properties;
			}
		}

		Path propPath = getEtcDir().resolve(propertiesFileName);
		if (!Files.isReadable(propPath)) {
			return null;
		}

		Properties prop = new Properties();
		try (FileReader fr = new FileReader(propPath.toFile())) {
			prop.load(fr);
		} catch (IOException e) {
			logger.warn("check: Properties file wasn't found. path=" + propPath.toString());
			return null;
		}

		PropertiesCache pc = new PropertiesCache();
		pc.file = propPath.toFile();
		pc.timestamp = pc.file.lastModified();
		pc.properties = prop;
		cache.put(propertiesFileName, pc);

		return prop;
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			logger.warn("sleep: Interrupted. message=" + e.getMessage());
		}
	}

}
