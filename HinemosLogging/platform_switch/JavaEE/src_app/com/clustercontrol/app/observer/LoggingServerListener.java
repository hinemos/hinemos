/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.app.observer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.clustercontrol.logging.LoggingConfigurator;
import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.exception.LoggingPropertyException;
import com.clustercontrol.logging.property.LoggingProperty;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * アプリケーションの起動、終了を監視するクラスです<BR>
 */
public class LoggingServerListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		try {

			// 設定ファイルの読み込み
			// ServletContextの初期化パラメータを取得
			ServletContext context = servletContextEvent.getServletContext();
			String filePath = context.getInitParameter(LoggingConstant.SERVLET_CONTEXT_PARAM_PROPERTIES_PATH);
			InputStream in = null;
			if (filePath == null || filePath.equals("")) {
				// 指定がない場合は/WEB-INF/lib
				filePath = "/WEB-INF/lib/" + LoggingConstant.CONFIG_FILE_NAME;
				in = context.getResourceAsStream(filePath);
				if (in == null) {
					throw new FileNotFoundException(filePath);
				}
			} else {
				in = new FileInputStream(filePath);
			}

			// HinemosLoggingの起動
			LoggingConfigurator.start(in);

		} catch (Throwable t) {
			// 起動時に異常が起きた場合は標準エラー出力に書き込み、終了する
			t.printStackTrace();
			return;
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		// HinemosLoggingの停止
		LoggingConfigurator.stop(true);
	}

}
