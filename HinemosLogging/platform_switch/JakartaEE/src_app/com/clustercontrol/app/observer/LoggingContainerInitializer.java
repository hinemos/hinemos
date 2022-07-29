/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.app.observer;

import java.util.Set;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public class LoggingContainerInitializer implements ServletContainerInitializer {

	@Override
	public void onStartup(Set<Class<?>> arg0, ServletContext arg1) throws ServletException {
		LoggingServerListener logLis = new LoggingServerListener();
		arg1.addListener(logLis);
	}
}
