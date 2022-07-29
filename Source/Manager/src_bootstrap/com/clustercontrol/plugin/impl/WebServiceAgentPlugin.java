/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.ws.agent.AgentEndpoint;
import com.clustercontrol.ws.agentbinary.AgentBinaryEndpoint;
import com.clustercontrol.ws.agenthub.AgentHubEndpoint;
import com.clustercontrol.ws.agentnodeconfig.AgentNodeConfigEndpoint;

public class WebServiceAgentPlugin extends WebServicePlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(WebServiceAgentPlugin.class);

	private static final ThreadPoolExecutor _threadPoolForAgent;
	
	//　収集値をマネージャへ送信するためのスレッドプール
	private static final ThreadPoolExecutor _threadPoolForAgentHub;
	private static final ThreadPoolExecutor _threadPoolForAgentBinary;
	private static final ThreadPoolExecutor _threadPoolForAgentNodeConfig;

	static {
		int _threadPoolSizeForAgent = HinemosPropertyCommon.ws_agent_threadpool_size.getIntegerValue();
		int _queueSizeForAgent = HinemosPropertyCommon.ws_agent_queue_size.getIntegerValue();

		_threadPoolForAgent = new MonitoredThreadPoolExecutor(_threadPoolSizeForAgent, _threadPoolSizeForAgent, 0L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(_queueSizeForAgent),
				new ThreadFactory() {
			private volatile int _count = 0;
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "WebServiceWorkerForAgent-" + _count++);
			}
		}, new ThreadPoolExecutor.AbortPolicy());
		
		_threadPoolForAgentHub = new MonitoredThreadPoolExecutor(_threadPoolSizeForAgent, _threadPoolSizeForAgent, 0L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(_queueSizeForAgent),
				new ThreadFactory() {
			private volatile int _count = 0;
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "WebServiceWorkerForAgentHub-" + _count++);
			}
		}, new ThreadPoolExecutor.AbortPolicy());

		_threadPoolForAgentBinary = new MonitoredThreadPoolExecutor(_threadPoolSizeForAgent, _threadPoolSizeForAgent,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(_queueSizeForAgent), new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "WebServiceWorkerForAgentBinary-" + _count++);
					}
				}, new ThreadPoolExecutor.AbortPolicy());

		_threadPoolForAgentNodeConfig = new MonitoredThreadPoolExecutor(_threadPoolSizeForAgent, _threadPoolSizeForAgent,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(_queueSizeForAgent), new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "WebServiceWorkerForAgentNodeConfig-" + _count++);
					}
				}, new ThreadPoolExecutor.AbortPolicy());
	}

	public static int getAgentQueueSize() {
		return _threadPoolForAgent.getQueue().size();
	}
	
	public static ThreadPoolExecutor getAgentPool(){
		return _threadPoolForAgent;
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

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(RestServiceClientPlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {

	}

	@Override
	public void activate() {
		// Check if key exists
		if(!checkRequiredKeys()){
			log.warn("KEY NOT FOUND! Unable to activate " + this.getClass().getName());
			return;
		}

		/** Webサービスの起動処理 */
		final String addressPrefix = HinemosPropertyCommon.ws_agent_address.getStringValue();
		publish(addressPrefix, "/HinemosWS/AgentEndpoint", new AgentEndpoint(), _threadPoolForAgent);
		publish(addressPrefix, "/HinemosWS/AgentHubEndpoint", new AgentHubEndpoint(), _threadPoolForAgentHub);
		publish(addressPrefix, "/HinemosWS/AgentBinaryEndpoint", new AgentBinaryEndpoint(), _threadPoolForAgentBinary);
		publish(addressPrefix, "/HinemosWS/AgentNodeConfigEndpoint", new AgentNodeConfigEndpoint(), _threadPoolForAgentNodeConfig);
	}

	@Override
	public void deactivate() {
		// 許容時間まで待ちリクエストを処理する
		_threadPoolForAgent.shutdown();
		try {
			long _shutdownTimeoutForAgent = HinemosPropertyCommon.ws_agent_shutdown_timeout.getNumericValue();
			if (! _threadPoolForAgent.awaitTermination(_shutdownTimeoutForAgent, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = _threadPoolForAgent.shutdownNow();
				if (remained != null) {
					log.info("shutdown timeout. runnable remained. (size = " + remained.size() + ")");
				}
			}
		} catch (InterruptedException e) {
			_threadPoolForAgent.shutdownNow();
		}

		// 許容時間まで待ちリクエストを処理する
		_threadPoolForAgentHub.shutdown();
		try {
			long _shutdownTimeoutForAgent = HinemosPropertyCommon.ws_agent_shutdown_timeout.getNumericValue();
			if (! _threadPoolForAgentHub.awaitTermination(_shutdownTimeoutForAgent, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = _threadPoolForAgentHub.shutdownNow();
				if (remained != null) {
					log.info("shutdown(AgentHub) timeout. runnable remained. (size = " + remained.size() + ")");
				}
			}
		} catch (InterruptedException e) {
			_threadPoolForAgentHub.shutdownNow();
		}

		// 許容時間まで待ちリクエストを処理する
		_threadPoolForAgentBinary.shutdown();
		try {
			long _shutdownTimeoutForAgent = HinemosPropertyCommon.ws_agent_shutdown_timeout.getNumericValue();
			if (!_threadPoolForAgentBinary.awaitTermination(_shutdownTimeoutForAgent, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = _threadPoolForAgentBinary.shutdownNow();
				if (remained != null) {
					log.info("shutdown(AgentBinary) timeout. runnable remained. (size = " + remained.size() + ")");
				}
			}
		} catch (InterruptedException e) {
			_threadPoolForAgentBinary.shutdownNow();
		}

		// 許容時間まで待ちリクエストを処理する
		_threadPoolForAgentNodeConfig.shutdown();
		try {
			long _shutdownTimeoutForAgent = HinemosPropertyCommon.ws_agent_shutdown_timeout.getNumericValue();
			if (!_threadPoolForAgentNodeConfig.awaitTermination(_shutdownTimeoutForAgent, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = _threadPoolForAgentNodeConfig.shutdownNow();
				if (remained != null) {
					log.info("shutdown(AgentNodeConfig) timeout. runnable remained. (size = " + remained.size() + ")");
				}
			}
		} catch (InterruptedException e) {
			_threadPoolForAgentNodeConfig.shutdownNow();
		}
		
		super.deactivate();
	}

}
