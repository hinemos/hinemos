/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.ws.rs.core.Application;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.RequestExecutorProvider;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;

/**
 * スレッドプールを指定できるHttpContainer
 */
public class RestApiHttpContainer extends GrizzlyHttpContainer {

	private RequestExecutorProvider requestExecutorProvider;

	RestApiHttpContainer(Application application) {
		super(application);
	}

	/**
	 * Create a new Grizzly HTTP container.
	 *
	 * @param application
	 *            JAX-RS / Jersey application to be deployed on Grizzly HTTP
	 *            container.
	 * @param parentContext
	 *            DI provider specific context with application's registered
	 *            bindings.
	 * @param threadPool
	 *            Thread Pool
	 */
	public RestApiHttpContainer(Application application, ThreadPoolExecutor threadPool) {
		super(application);
		this.requestExecutorProvider = new RestApiRequestExecutorProvider(threadPool);
	}

	@Override
	public RequestExecutorProvider getRequestExecutorProvider() {
		return requestExecutorProvider;
	}

	/**
	 * スレッドプールを指定できるRequestExecutorProvider
	 */
	private static class RestApiRequestExecutorProvider implements RequestExecutorProvider {

		private ThreadPoolExecutor threadPool;

		public RestApiRequestExecutorProvider(ThreadPoolExecutor threadPool) {
			this.threadPool = threadPool;
		}

		@Override
		public Executor getExecutor(Request request) {
			return threadPool;
		}
	}
}
