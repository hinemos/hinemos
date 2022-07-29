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

import com.clustercontrol.logging.LoggingConfigurator;
import com.clustercontrol.logging.constant.LoggingConstant;

/**
 * Premain Class.
 */
public class LoggingPreMain {

	public static void premain(String agentArgs) {
		try {

			// 設定ファイルの読み込み
			String filePath = null;
			if (agentArgs == null || agentArgs.equals("")) {
				// 指定がない場合は同一ディレクトリ
				Path path = getJarDir(LoggingPreMain.class);
				filePath = path.resolve(LoggingConstant.CONFIG_FILE_NAME).toString();
			} else {
				filePath = agentArgs;
			}
			FileInputStream in = new FileInputStream(filePath);

			// HinemosLoggingの起動
			LoggingConfigurator.start(in);

		} catch (Throwable t) {
			// 起動時に異常が起きた場合は標準エラー出力に書き込み、終了する
			t.printStackTrace();
			return;
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
