/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.plugin;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.plugin.impl.RestServicePlugin;
import com.clustercontrol.rest.endpoint.sdml.agent.AgentSdmlRestEndpoints;
import com.clustercontrol.rest.endpoint.sdml.agent.AgentSdmlRestFilterRegistration;
import com.clustercontrol.rest.exception.HinemosRestExceptionMapper;
import com.clustercontrol.rest.filter.InitializeFilter;

public class SdmlRestServiceAgentPlugin extends RestServicePlugin implements HinemosPlugin {

	private static Log logger = LogFactory.getLog(SdmlRestServiceAgentPlugin.class);

	// スレッドプール
	private static final ThreadPoolExecutor _threadPoolForAgentSdml;

	// クリーナーはRestServiceAgentPluginで起動しているため不要
	// RestAgentRequestDataCleaner _requestCleaner;

	static {
		int _threadPoolSizeForAgent = HinemosPropertyCommon.rest_agent_threadpool_size.getIntegerValue();
		int _queueSizeForAgent = HinemosPropertyCommon.rest_queue_size.getIntegerValue();

		_threadPoolForAgentSdml = new MonitoredThreadPoolExecutor(_threadPoolSizeForAgent, _threadPoolSizeForAgent, 0L,
				TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(_queueSizeForAgent), new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "RestApiWorkerForAgentSdml-" + _count++);
					}
				}, new ThreadPoolExecutor.AbortPolicy());
	}

	@Override
	public void create() {
		logger.info("create() : creating " + getClass().getSimpleName() + "...");

		try {
			addressPrefix = HinemosPropertyCommon.rest_agent_address.getStringValue();
		} catch (Exception e) {
			logger.error("create() : failed.", e);
		}
	}

	@Override
	public void activate() {
		logger.info("activate() : activating " + getClass().getSimpleName() + "...");

		try {
			// AgentSdmlRestEndpoints
			publish(addressPrefix, BASE_URL + "/" + AgentSdmlRestEndpoints.class.getSimpleName(),
					new ResourceConfig().registerClasses(InitializeFilter.class, AgentSdmlRestEndpoints.class,
							AgentSdmlRestFilterRegistration.class, HinemosRestExceptionMapper.class),
					_threadPoolForAgentSdml);

		} catch (Exception e) {
			logger.error("acivate() : failed.", e);
		}
	}

	@Override
	public void deactivate() {
		logger.info("deactivate() : deactivating " + getClass().getSimpleName() + "...");
		long _shutdownTimeout = HinemosPropertyCommon.rest_agent_shutdown_timeout.getNumericValue();
		// 許容時間まで待ちリクエストを処理しつつスレッドプールをシャットダウンする。
		shutdownOfThreadPool(_threadPoolForAgentSdml, "agentSdml", _shutdownTimeout);

		super.deactivate();
	}
}
