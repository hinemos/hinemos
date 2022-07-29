/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.util;

import java.io.Writer;
import java.nio.charset.Charset;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.WriterAppender;

public class Log4J2Util {

	//log4j2 のAPIを用いてappenderを追加
	public static void addWriteAppenderToLogger(Writer writer, String appenderName, String loggerName,  Level level) {
		final LoggerContext context = LoggerContext.getContext(false);
		final Configuration config = context.getConfiguration();
		final PatternLayout layout = PatternLayout.newBuilder().withPattern("%d %-5p [%t] [%c] %m%n")
				.withCharset(Charset.forName("UTF-8")).build();

		Appender appender = WriterAppender.createAppender(layout, null, writer, appenderName, false, true);
		appender.start();
		LoggerConfig tagLogConf = config.getLoggerConfig(loggerName);
		tagLogConf.addAppender(appender, level, null);
	}

	//log4j2 のAPIを用いてappenderを削除
	public static void removeAppenderFromLogger(String appenderName, String loggerName) {
		final LoggerContext context = LoggerContext.getContext(false);
		final Configuration config = context.getConfiguration();

		LoggerConfig tagLogConf = config.getLoggerConfig(loggerName);
		Appender appender = tagLogConf.getAppenders().get(appenderName);
		if( appender != null ){
			appender.stop();
		} 
		tagLogConf.removeAppender(appenderName);
		
	}

}
