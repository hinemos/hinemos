/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.app.observer;

import java.io.InputStream;

import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.LoggingConfigurator;
import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.property.LoggingProperty;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * アプリケーションの起動、終了を監視するクラスです<BR>
 */

public class LoggingServerListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		// Servletの場合は直接終了処理呼び出し
		LoggingConfigurator.stop();
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext context = servletContextEvent.getServletContext();
		// hinemoslogging.properitesのパスを設定
		String filePath = context.getInitParameter(LoggingConstant.SERVLET_CONTEXT_PARAM_PROPERTIES_PATH);
		// web.xmlで指定した変数から取得
		// なければデフォルト
		if (filePath == null || filePath.equals("")) {
			filePath = LoggingConstant.CONFIG_FILE_PATH;
		}
		try (InputStream in = context.getResourceAsStream(filePath)) {
			LoggingProperty.getInstance().loadProperty(in);
		} catch (Exception e) {
			// 設定ファイルの読み込みでエラーが出たら吐き出し先がないので終了
			throw new RuntimeException(e);
		}
		// Hinemos内部ログの初期設定
		InternalLogManager.init();
		InternalLogManager.Logger internalLog = InternalLogManager.getLogger(LoggingServerListener.class);
		internalLog.info("contextInitialized :" + LoggingConstant.CONFIG_FILE_NAME + " path=" + filePath);
		try {
			LoggingConfigurator.init(true);
		} catch (Exception e) {
			internalLog.error(e.getMessage(), e);
			LoggingConfigurator.stop();
		}
	}
}
