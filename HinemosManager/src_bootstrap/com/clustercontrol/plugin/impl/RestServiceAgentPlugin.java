/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.rest.endpoint.agent.AgentBinaryRestEndpoints;
import com.clustercontrol.rest.endpoint.agent.AgentBinaryRestFilterRegistration;
import com.clustercontrol.rest.endpoint.agent.AgentHubRestEndpoints;
import com.clustercontrol.rest.endpoint.agent.AgentHubRestFilterRegistration;
import com.clustercontrol.rest.endpoint.agent.AgentNodeConfigRestEndpoints;
import com.clustercontrol.rest.endpoint.agent.AgentNodeConfigRestFilterRegistration;
import com.clustercontrol.rest.endpoint.agent.AgentRestEndpoints;
import com.clustercontrol.rest.endpoint.agent.AgentRestFilterRegistration;
import com.clustercontrol.rest.endpoint.agent.AgentRpaRestEndpoints;
import com.clustercontrol.rest.endpoint.agent.AgentRpaRestFilterRegistration;
import com.clustercontrol.rest.exception.HinemosRestExceptionMapper;
import com.clustercontrol.rest.filter.InitializeFilter;
import com.clustercontrol.rest.util.RestAgentRequestDataCleaner;

public class RestServiceAgentPlugin extends RestServicePlugin implements HinemosPlugin {
	private static final Log log = LogFactory.getLog(RestServiceAgentPlugin.class);

	//　各種スレッドプール
	private static final ThreadPoolExecutor _threadPoolForAgent;
	private static final ThreadPoolExecutor _threadPoolForAgentHub;
	private static final ThreadPoolExecutor _threadPoolForAgentBinary;
	private static final ThreadPoolExecutor _threadPoolForAgentNodeConfig;
	private static final ThreadPoolExecutor _threadPoolForAgentRpa;
	
	RestAgentRequestDataCleaner _requestCleaner;

	static {
		int _threadPoolSizeForAgent = HinemosPropertyCommon.rest_agent_threadpool_size.getIntegerValue();
		int _queueSizeForAgent = HinemosPropertyCommon.rest_agent_queue_size.getIntegerValue();

		_threadPoolForAgent = new MonitoredThreadPoolExecutor(_threadPoolSizeForAgent, _threadPoolSizeForAgent, 0L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(_queueSizeForAgent),
				new ThreadFactory() {
			private volatile int _count = 0;
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "RestApiWorkerForAgent-" + _count++);
			}
		}, new ThreadPoolExecutor.AbortPolicy());
		
		_threadPoolForAgentHub = new MonitoredThreadPoolExecutor(_threadPoolSizeForAgent, _threadPoolSizeForAgent, 0L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(_queueSizeForAgent),
				new ThreadFactory() {
			private volatile int _count = 0;
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "RestApiWorkerForAgentHub-" + _count++);
			}
		}, new ThreadPoolExecutor.AbortPolicy());

		_threadPoolForAgentBinary = new MonitoredThreadPoolExecutor(_threadPoolSizeForAgent, _threadPoolSizeForAgent,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(_queueSizeForAgent), new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "RestApiWorkerForAgentBinary-" + _count++);
					}
				}, new ThreadPoolExecutor.AbortPolicy());

		_threadPoolForAgentNodeConfig = new MonitoredThreadPoolExecutor(_threadPoolSizeForAgent, _threadPoolSizeForAgent,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(_queueSizeForAgent), new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "RestApiWorkerForAgentNodeConfig-" + _count++);
					}
				}, new ThreadPoolExecutor.AbortPolicy());

		_threadPoolForAgentRpa = new MonitoredThreadPoolExecutor(_threadPoolSizeForAgent, _threadPoolSizeForAgent,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(_queueSizeForAgent), new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "RestApiWorkerForAgentRpa-" + _count++);
					}
				}, new ThreadPoolExecutor.AbortPolicy());
	}
	
	
	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(RestServiceClientPlugin.class.getName());
		return dependency;
	}
	
	@Override
	public void create() {
		log.info("create() start.");
		try {
			addressPrefix = HinemosPropertyCommon.rest_agent_address.getStringValue();
			_requestCleaner = new RestAgentRequestDataCleaner();
		} catch (Exception e) {
			log.error("create() failed.", e);
		}
	}

	@Override
	public void activate() {
		log.info("activate() start.");

		// Check if key exists
		if (!this.checkRequiredKeys()) {
			log.warn("KEY NOT FOUND! Unable to activate " + this.getClass().getName());
			return;
		}
		
		try {
            //AgentRestEndpoints
			publish(addressPrefix, BASE_URL + "/" + AgentRestEndpoints.class.getSimpleName(),
					new ResourceConfig().registerClasses(InitializeFilter.class, AgentRestEndpoints.class,
							AgentRestFilterRegistration.class, HinemosRestExceptionMapper.class),
					_threadPoolForAgent);
            //AgentHubRestEndpoints
            publish(addressPrefix, BASE_URL + "/" + AgentHubRestEndpoints.class.getSimpleName(),
                    new ResourceConfig().registerClasses(InitializeFilter.class, AgentHubRestEndpoints.class,
                            AgentHubRestFilterRegistration.class, HinemosRestExceptionMapper.class),
                    _threadPoolForAgentHub);
            //AgentBinaryRestEndpoints
            publish(addressPrefix, BASE_URL + "/" + AgentBinaryRestEndpoints.class.getSimpleName(),
                    new ResourceConfig().registerClasses(InitializeFilter.class, AgentBinaryRestEndpoints.class,
                            AgentBinaryRestFilterRegistration.class, HinemosRestExceptionMapper.class),
                    _threadPoolForAgentBinary);
            //AgentNodeConfigRestEndpoints
            publish(addressPrefix, BASE_URL + "/" + AgentNodeConfigRestEndpoints.class.getSimpleName(),
                    new ResourceConfig().registerClasses(InitializeFilter.class, AgentNodeConfigRestEndpoints.class,
                            AgentNodeConfigRestFilterRegistration.class, HinemosRestExceptionMapper.class),
                    _threadPoolForAgentNodeConfig);
            //AgentRpaRestEndpoints
            publish(addressPrefix, BASE_URL + "/" + AgentRpaRestEndpoints.class.getSimpleName(),
                    new ResourceConfig().registerClasses(InitializeFilter.class, AgentRpaRestEndpoints.class,
                            AgentRpaRestFilterRegistration.class, HinemosRestExceptionMapper.class, MultiPartFeature.class),
                    _threadPoolForAgentRpa);
			//リクエスト重複管理用テーブル削除処理開始
			_requestCleaner.start();
		} catch (Exception e) {
			log.error("acivate failed.", e);
		}
	}
	
	@Override
	public void deactivate() {
		long _shutdownTimeout = HinemosPropertyCommon.rest_agent_shutdown_timeout.getNumericValue();
		// 許容時間まで待ちリクエストを処理しつつスレッドプールをシャットダウンする。
		shutdownOfThreadPool(_threadPoolForAgent,"agent", _shutdownTimeout);
		shutdownOfThreadPool(_threadPoolForAgentHub,"agentHub", _shutdownTimeout);
		shutdownOfThreadPool(_threadPoolForAgentBinary,"agentBinary", _shutdownTimeout);
		shutdownOfThreadPool(_threadPoolForAgentNodeConfig,"agentNodeConfig", _shutdownTimeout);
		shutdownOfThreadPool(_threadPoolForAgentRpa,"agentRpa", _shutdownTimeout);
		
		//リクエスト重複管理用テーブル削除処理停止
		_requestCleaner.shutdown();

		super.deactivate();
	}
	
	public static ThreadPoolExecutor getAgentPool(){
		return _threadPoolForAgent;
	}
	
	public static int getAgentQueueSize() {
		return _threadPoolForAgent.getQueue().size();
	}

	public static int getAgentHubQueueSize() {
		return _threadPoolForAgentHub.getQueue().size();
	}
	
	public static int getAgentBinaryQueueSize() {
		return _threadPoolForAgentBinary.getQueue().size();
	}
	
	public static int getAgentNodeConfigQueueSize() {
		return _threadPoolForAgentNodeConfig.getQueue().size();
	}
}
