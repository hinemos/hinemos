/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

/**
 * JAX-WSによるWEBサービスの初期化(publish)/停止(stop)を制御するHinemos本体のエージェント接続用プラグイン.
 *
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

import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.ws.agent.AgentEndpoint;
import com.clustercontrol.ws.agenthub.AgentHubEndpoint;

public class WebServiceAgentPlugin extends WebServicePlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(WebServiceAgentPlugin.class);

	private static final ThreadPoolExecutor _threadPoolForAgent;
	
	//　収集値をマネージャへ送信するためのスレッドプール
	private static final ThreadPoolExecutor _threadPoolForAgentHub;

	static {
		int _threadPoolSizeForAgent = HinemosPropertyUtil.getHinemosPropertyNum("ws.agent.threadpool.size", Long.valueOf(8)).intValue();
		int _queueSizeForAgent = HinemosPropertyUtil.getHinemosPropertyNum("ws.agent.queue.size", Long.valueOf(1200)).intValue();

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
	}

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(WebServiceJobMapPlugin.class.getName());
		dependency.add(WebServiceNodeMapPlugin.class.getName());
		dependency.add(WebServiceCorePlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {

	}

	@Override
	public void activate() {
		/** Webサービスの起動処理 */
		final String addressPrefix = HinemosPropertyUtil.getHinemosPropertyStr("ws.agent.address" , "http://0.0.0.0:8081");
		publish(addressPrefix, "/HinemosWS/AgentEndpoint", new AgentEndpoint(), _threadPoolForAgent);
		publish(addressPrefix, "/HinemosWS/AgentHubEndpoint", new AgentHubEndpoint(), _threadPoolForAgentHub);
	}

	@Override
	public void deactivate() {
		// 許容時間まで待ちリクエストを処理する
		_threadPoolForAgent.shutdown();
		try {
			long _shutdownTimeoutForAgent = HinemosPropertyUtil.getHinemosPropertyNum("ws.agent.shutdown.timeout", Long.valueOf(60000));
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
			long _shutdownTimeoutForAgent = HinemosPropertyUtil.getHinemosPropertyNum("ws.agent.shutdown.timeout", Long.valueOf(60000));
			if (! _threadPoolForAgentHub.awaitTermination(_shutdownTimeoutForAgent, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = _threadPoolForAgentHub.shutdownNow();
				if (remained != null) {
					log.info("shutdown(AgentHub) timeout. runnable remained. (size = " + remained.size() + ")");
				}
			}
		} catch (InterruptedException e) {
			_threadPoolForAgentHub.shutdownNow();
		}

		super.deactivate();
	}

}
