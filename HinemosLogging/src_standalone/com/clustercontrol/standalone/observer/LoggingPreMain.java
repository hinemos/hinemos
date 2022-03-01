/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.standalone.observer;

import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.LoggingConfigurator;
import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.property.LoggingProperty;

public class LoggingPreMain {
	public static void premain(String agentArgs) {
		Path path = null;
		String filePath = null;

		// 最初にHinemosロギングの動作ログの設定をする
		try {
			path = getJarDir(LoggingPreMain.class);
			// 設定ファイルのパスを設定 (agentArgsに指定して任意に設定可能)

			if (agentArgs == null || agentArgs.equals("")) {
				filePath = path + "/" + LoggingConstant.CONFIG_FILE_NAME;
			} else {
				filePath = agentArgs;
			}
			try (FileInputStream in = new FileInputStream(filePath)) {
				LoggingProperty.getInstance().loadProperty(in);
			} catch (Exception e1) {
				throw e1;
			}
		} catch (Exception e) {
			// 設定ファイルの読み込みでエラーが出たら吐き出し先がないので終了
			throw new RuntimeException(e);
		}

		// Hinemos内部ログの初期設定
		InternalLogManager.init();
		InternalLogManager.Logger internalLog = InternalLogManager.getLogger(LoggingPreMain.class);
		internalLog.info("premain : internalLog start");
		internalLog.info("premain : " + LoggingConstant.CONFIG_FILE_NAME + " path=" + filePath);
		try {
			LoggingConfigurator.init(false);
		} catch (Exception e) {
			internalLog.error("premain : " + e.getMessage(), e);
			LoggingConfigurator.stop();
		}
	}

	// jarの配置箇所の取得
	private static Path getJarDir(Class<?> cls) throws URISyntaxException {
		ProtectionDomain pd = cls.getProtectionDomain();
		CodeSource cs = pd.getCodeSource();
		URL location = cs.getLocation();
		URI uri = location.toURI();
		Path path = Paths.get(uri);
		return path.getParent();
	}
}
