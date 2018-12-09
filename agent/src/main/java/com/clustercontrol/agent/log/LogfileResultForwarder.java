/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.AgentHubEndPointWrapper;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.ws.agenthub.MessageInfo;
import com.clustercontrol.ws.jobmanagement.RunInstructionInfo;
import com.clustercontrol.ws.monitor.LogfileResultDTO;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorStringValueInfo;

public class LogfileResultForwarder {
	
	private static Log log = LogFactory.getLog(LogfileResultForwarder.class);
	
	private static final LogfileResultForwarder _instance = new LogfileResultForwarder();
	
	private final ScheduledExecutorService _scheduler;
	
	public final int _queueMaxSize;
	
	public final int _transportMaxTries;
	public final int _transportMaxSize;
	public final int _transportIntervalSize;
	public final long _transportIntervalMSec;
	
	private AtomicInteger transportTries = new AtomicInteger(0);
	
	private List<Result> forwardList = new ArrayList<Result>();
	
	private LogfileResultForwarder() {
		{
			String key = "monitor.logfile.forwarding.queue.maxsize";
			int valueDefault = 5000;
			String str = AgentProperties.getProperty(key);
			int value = valueDefault;
			try {
				value = Integer.parseInt(str);
				if (value != -1 && value < 1) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				value = valueDefault;
			} finally {
				log.info(key + " uses value \"" + value + "\". (configuration = \"" + str + "\")");
			}
			_queueMaxSize = value;
		}
		
		{
			String key = "monitor.logfile.forwarding.transport.maxsize";
			int valueDefault = 100;
			String str = AgentProperties.getProperty(key);
			int value = valueDefault;
			try {
				value = Integer.parseInt(str);
				if (value != -1 && value < 1) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				value = valueDefault;
			} finally {
				log.info(key + " uses value \"" + value + "\". (configuration = \"" + str + "\")");
			}
			_transportMaxSize = value;
		}
		
		{
			String key = "monitor.logfile.forwarding.transport.maxtries";
			int valueDefault = 900;
			String str = AgentProperties.getProperty(key);
			int value = valueDefault;
			try {
				value = Integer.parseInt(str);
				if (value != -1 && value < 1) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				value = valueDefault;
			} finally {
				log.info(key + " uses value \"" + value + "\". (configuration = \"" + str + "\")");
			}
			_transportMaxTries = value;
		}
		
		{
			String key = "monitor.logfile.forwarding.transport.interval.size";
			int valueDefault = 15;
			String str = AgentProperties.getProperty(key);
			int value = valueDefault;
			try {
				value = Integer.parseInt(str);
				if (value != -1 && value < 1) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				value = valueDefault;
			} finally {
				log.info(key + " uses value \"" + value + "\". (configuration = \"" + str + "\")");
			}
			_transportIntervalSize = value;
		}
		
		{
			String key = "monitor.logfile.forwarding.transport.interval.msec";
			long valueDefault = 1000L;
			String str = AgentProperties.getProperty(key);
			long value = valueDefault;
			try {
				value = Long.parseLong(str);
				if (value != -1 && value < 1) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				value = valueDefault;
			} finally {
				log.info(key + " uses value \"" + value + "\". (configuration = \"" + str + "\")");
			}
			_transportIntervalMSec = value;
		}
		
		_scheduler = Executors.newSingleThreadScheduledExecutor(
				new ThreadFactory() {
					private volatile int _count = 0;
					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r, LogfileResultForwarder.class.getSimpleName() + _count++);
						t.setDaemon(true);
						return t;
					}
				});
		
		if (_transportIntervalMSec != -1) {
			_scheduler.scheduleWithFixedDelay(new ScheduledTask(), 0, _transportIntervalMSec, TimeUnit.MILLISECONDS);
		}
	}
	
	public static LogfileResultForwarder getInstance() {
		return _instance;
	}
	
	public void add(String message, MessageInfo msgInfo, MonitorInfo monitorInfo, MonitorStringValueInfo monitorStrValueInfo, RunInstructionInfo runInstructionInfo) {
		try {
			ForwardListLock.writeLock();
			
			if (_queueMaxSize != -1 && forwardList.size() >= _queueMaxSize) {
				log.warn("rejected new logfile monitor's result. queue is full : " + message);
				return;
			}
			
			forwardList.add(new Result(message, msgInfo, monitorInfo, monitorStrValueInfo, runInstructionInfo));
			
			if (forwardList.size() != 0) {
				if (_transportIntervalSize != -1 && forwardList.size() % _transportIntervalSize == 0) {
					_scheduler.submit(new ScheduledTask());
				}
			}
		} finally {
			ForwardListLock.writeUnlock();
		}
	}
	
	private void forward() {
		try {
			ForwardListLock.writeLock();
			
			while (forwardList.size() > 0) {
				// JAX-WSの一時ファイル肥大化(/tmp/jaxwsXXX)へのワークアラウンド実装(リクエストサイズに上限を設ける)
				int transportSize = _transportMaxSize != -1 && forwardList.size() > _transportMaxSize ? _transportMaxSize : forwardList.size();
				// 送信失敗直後は1メッセージずつ送信(SOAPのアーキテクチャ上、timeoutなどでメッセージの重複受信は回避できないが、その重複数を最小化する）
				transportSize = transportTries.get() == 0 ? transportSize : 1;
				
				List<Result> forwardListPart = Collections.unmodifiableList(forwardList.subList(0, transportSize));
				if (forwardListPart.size() > 0) {
					try {
						List<LogfileResultDTO> dtoList = new ArrayList<LogfileResultDTO>(forwardListPart.size());
						for (Result result : forwardListPart) {
							LogfileResultDTO dto = new LogfileResultDTO();
							dto.setMessage(result._message);
							dto.setMsgInfo(result._msgInfo);
							dto.setMonitorInfo(result._monitorInfo);
							dto.setMonitorStrValueInfo(result._monitorStrValueInfo);
							dto.setRunInstructionInfo(result._runInstructionInfo);
							dtoList.add(dto);
						}
						AgentHubEndPointWrapper.forwardLogfileResult(dtoList);
						
						log.debug(String.format("forward() : sended %d lines.", forwardListPart.size()));
					} catch (Throwable t) {
						String msg = String.format("[%d/%d] failed forwarding logfile monitor's result (%d of %d) : %s ...", 
								transportTries.get(), _transportMaxTries, forwardListPart.size(), forwardList.size(), 
								forwardListPart.get(0)._message);
						if (log.isDebugEnabled()) {
							log.warn(msg, t);
						} else {
							log.warn(msg);
						}
						if (transportTries.incrementAndGet() >= _transportMaxTries && _transportMaxTries != -1) {
							msg = String.format("[%d/%d] give up forwarding logfile monitor's result (%d of %d) : %s ...", 
									transportTries.get(), _transportMaxTries, forwardListPart.size(), forwardList.size(), 
									forwardListPart.get(0)._message);
							log.warn(msg, t);
						} else {
							// retry
							return;
						}
					}
					
					forwardList.removeAll(forwardListPart);
					transportTries.set(0);
				}
			}
		} catch (RuntimeException e) {
			log.warn("failed forwarding result.", e);
		} finally {
			ForwardListLock.writeUnlock();
		}
	}
	
	private static class Result {
		public final String _message;
		public final MessageInfo _msgInfo;
		public final MonitorInfo _monitorInfo;
		public final MonitorStringValueInfo _monitorStrValueInfo;
		public final RunInstructionInfo _runInstructionInfo;
		
		public Result(String message, MessageInfo msgInfo, MonitorInfo monitorInfo, MonitorStringValueInfo monitorStrValueInfo, RunInstructionInfo runInstructionInfo) {
			this._message = message;
			this._msgInfo = msgInfo;
			this._monitorInfo = monitorInfo;
			this._monitorStrValueInfo = monitorStrValueInfo;
			this._runInstructionInfo = runInstructionInfo;
		}
	}
	
	private static class ScheduledTask implements Runnable {
		
		@Override
		public void run() {
			_instance.forward();
		}
		
	}
	
	private static class ForwardListLock {
		
		private static final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
		
		public static void writeLock() {
			_lock.writeLock().lock();
		}
		
		public static void writeUnlock() {
			_lock.writeLock().unlock();
		}
	}
	
}
