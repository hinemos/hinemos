/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.log;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.AppenderControl;

/**
 * 各ログ設定用の基底クラス
 */
public abstract class CustomLogConfig {

	abstract protected String getDirectory() throws FileNotFoundException;

	abstract protected String getFilename();

	protected String getFilePath() throws FileNotFoundException {
		return new File(new File(getDirectory()), getFilename()).getAbsolutePath();
	}

	abstract protected String getLogPattern();

	abstract protected String getFileGeneration();

	abstract protected String getFileSize();

	abstract public AppenderControl createAppenderControl(Level level, Appender appender);

	abstract public Appender createCustomAppender() throws Exception;

}
