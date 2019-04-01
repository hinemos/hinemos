/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.repository;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.AgentNodeConfigEndPointWrapper;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.ws.agent.AgentInfo;
import com.clustercontrol.ws.repository.NodeInfo;

public class NodeConfigResultForwarder {
	
	
	private static Log log = LogFactory.getLog(NodeConfigResultForwarder.class);

	private static final NodeConfigResultForwarder _instance = new NodeConfigResultForwarder();
	
	private final ScheduledExecutorService _scheduler;
	
	public final int _queueMaxSize;
	
	public final int _transportMaxSize;
	public final int _transportMaxTries;
	public final int _transportIntervalSize;
	public final long _transportIntervalMSec;
	
	private AtomicInteger transportTries = new AtomicInteger(0);
	
	private List<NodeConfigResult> forwardList = new ArrayList<NodeConfigResult>();
	
	private NodeConfigResultForwarder() {
		{
			String key = "repository.cmdb.forwarding.queue.maxsize";
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
			String key = "repository.cmdb.forwarding.transport.maxsize";
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
			String key = "repository.cmdb.forwarding.transport.maxtries";
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
			String key = "repository.cmdb.forwarding.transport.interval.size";
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
			String key = "repository.cmdb.forwarding.transport.interval.msec";
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
						Thread t = new Thread(r, NodeConfigResultForwarder.class.getSimpleName() + _count++);
						t.setDaemon(true);
						return t;
					}
				});
		
		if (_transportIntervalMSec != -1) {
			_scheduler.scheduleWithFixedDelay(new ScheduledTask(), 0, _transportIntervalMSec, TimeUnit.MILLISECONDS);
		}
	}
	
	public static NodeConfigResultForwarder getInstance() {
		return _instance;
	}
	
	public void add(NodeConfigResult result) {
		try {
			ForwardListLock.writeLock();
			
			// 送信キューサイズが最大値を上回ったため、送信をあきらめる.
			if (_queueMaxSize != -1 && forwardList.size() >= _queueMaxSize) {
				String msg = "rejected new node config's result. queue is full : " + toShortString(result);
				log.warn(msg);
				// Manager通知.
				String message = MessageConstant.MESSAGE_FAILED_TO_SEND_NODE_CONFIG_OVER_QUEUE_SIZE.getMessage();
				String settingId = "null";
				if(result.getNodeInfo() != null && result.getNodeInfo().getNodeConfigSettingId() != null){
					settingId = result.getNodeInfo().getNodeConfigSettingId();
				}
				AgentInfo info = Agent.getAgentInfo();
				String[] args = new String[] { info.getFacilityId(), info.getHostname(), settingId, msg};
				String originMsg = MessageConstant.MESSAGE_FAILED_TO_SEND_NODE_CONFIG_DETAIL.getMessage(args);
				NodeConfigCollector.sendMessage(PriorityConstant.TYPE_WARNING, message, originMsg, settingId);
				return;
			}
			
			forwardList.add(result);
			
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
				int transportSize = 0;
				if(_transportMaxSize != -1 && forwardList.size() > _transportMaxSize ){
					transportSize = _transportMaxSize;
				} else {
					transportSize = forwardList.size();
				}
				// 送信失敗直後は1メッセージずつ送信(SOAPのアーキテクチャ上、timeoutなどでメッセージの重複受信は回避できないが、その重複数を最小化する）
				transportSize = transportTries.get() == 0 ? transportSize : 1;	

				List<NodeConfigResult> forwardListPart = Collections.unmodifiableList(forwardList.subList(0, transportSize));
				
				List<NodeConfigResult> failedList = null;
				try {
					failedList = AgentNodeConfigEndPointWrapper.forwardNodeConfigSettingResult(forwardListPart);
				} catch (Throwable t) {
					String msg = String.format("[%d/%d] failed forwarding node config's result (%d of %d) : %s ...", 
							transportTries.get(), _transportMaxTries, forwardListPart.size(), forwardList.size(), 
							toShortString(forwardListPart.get(0)));
					if (log.isDebugEnabled()) {
						log.warn(msg, t);
					} else {
						log.warn(msg);
					}
					if (transportTries.incrementAndGet() >= _transportMaxTries && _transportMaxTries != -1) {
						// 最大トライ回数を上回ったため送信あきらめる.
						msg = String.format("[%d/%d] give up forwarding node config's result (%d of %d) : %s ...", 
								transportTries.get(), _transportMaxTries, forwardListPart.size(), forwardList.size(), 
								toShortString(forwardListPart.get(0)));
						log.warn(msg, t);
						// Manager通知.
						String message = MessageConstant.MESSAGE_FAILED_TO_SEND_NODE_CONFIG_OVER_RETRY_COUNT.getMessage();
						String settingIdList = createSettingIdStr(forwardListPart);
						AgentInfo info = Agent.getAgentInfo();
						String stackTrace = Arrays.toString(t.getStackTrace());
						msg = msg + "\n" + t.getMessage() + "\n" + stackTrace;
						String[] args = new String[] { info.getFacilityId(), info.getHostname(), settingIdList, msg};
						String originMsg = MessageConstant.MESSAGE_FAILED_TO_SEND_NODE_CONFIG_DETAIL.getMessage(args);
						NodeConfigCollector.sendMessage(PriorityConstant.TYPE_WARNING, message, originMsg, "SYS");
					} else {
						// retry
						return;
					}
				}
				// 一部失敗した場合もリトライさせる.
				if(failedList != null && !failedList.isEmpty()){
					// 成功分を次回送信対象から除外する.
					List<NodeConfigResult> successList = new ArrayList<NodeConfigResult>();
					for(NodeConfigResult sendInfo : forwardListPart){
						if(!failedList.contains(sendInfo)){
							successList.add(sendInfo);
						}
					}
					forwardList.removeAll(successList);
					if (transportTries.incrementAndGet() >= _transportMaxTries && _transportMaxTries != -1) {
						// 最大トライ回数を上回ったため送信あきらめる.
						String msg = String.format("[%d/%d] give up forwarding node config's result (%d of %d)."
								+ " for more information, see 'hinemos_manager.log'. : %s ...", 
								transportTries.get(), _transportMaxTries, failedList.size(), forwardList.size(), 
								toShortString(forwardListPart.get(0)));
						log.warn(msg);
						// Manager通知.
						String message = MessageConstant.MESSAGE_FAILED_TO_SEND_NODE_CONFIG_OVER_RETRY_COUNT.getMessage();
						String settingIdList = createSettingIdStr(failedList);
						AgentInfo info = Agent.getAgentInfo();
						String[] args = new String[] { info.getFacilityId(), info.getHostname(), settingIdList, msg};
						String originMsg = MessageConstant.MESSAGE_FAILED_TO_SEND_NODE_CONFIG_DETAIL.getMessage(args);
						NodeConfigCollector.sendMessage(PriorityConstant.TYPE_WARNING, message, originMsg, "SYS");
					} else {
						// retry
						return;
					}
				}

				forwardList.removeAll(forwardListPart);
				transportTries.set(0);
			}
		} catch (RuntimeException e) {
			log.warn("failed forwarding result.", e);
		} finally {
			ForwardListLock.writeUnlock();
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
	
	private static String toShortString(NodeConfigResult nInfo) {
		// Local Variables
		String ret = null;

		// MAIN
		if (nInfo != null && nInfo.getNodeInfo() != null) {
			int packageSize = 0;
			if( nInfo.getNodeInfo().getNodePackageInfo() != null){
				packageSize = nInfo.getNodeInfo().getNodePackageInfo().size();
			}
			int processSize = 0;
			if( nInfo.getNodeInfo().getNodeProcessInfo() != null){
				processSize = nInfo.getNodeInfo().getNodeProcessInfo().size();
			}
			int nicSize = 0;
			if( nInfo.getNodeInfo().getNodeNetworkInterfaceInfo() != null){
				nicSize = nInfo.getNodeInfo().getNodeNetworkInterfaceInfo().size();
			}
			int customSize = 0;
			if( nInfo.getNodeInfo().getNodeCustomInfo() != null){
				customSize = nInfo.getNodeInfo().getNodeCustomInfo().size();
			}
			
			ret = "NodeInfo [" 
					+ "facilityId = " + nInfo.getNodeInfo().getFacilityId()
					+ ", nodeOsRegisterFlag = " + nInfo.getNodeInfo().getNodeOsRegisterFlag()
					+ ", nodePackageRegisterFlag = " + nInfo.getNodeInfo().getNodePackageRegisterFlag()
					+ ", nodeProcessRegisterFlag = " + nInfo.getNodeInfo().getNodeProcessRegisterFlag()
					+ ", nodeNetworkInterfaceInfo = " + nInfo.getNodeInfo().getNodeNetworkInterfaceRegisterFlag()
					+ ", nodePackageInfo.size() = " + packageSize
					+ ", nodeProcessInfo.size() = " + processSize
					+ ", nodeNetworkInterfaceInfo.size() = " + nicSize
					+ ", nodeCustomInfo.size() = " + customSize
					+ "]";
		}
		
		return ret;
	}
	
	private static String createSettingIdStr(List<NodeConfigResult> resultList){
		if(resultList == null || resultList.isEmpty()){
			return "null";
		}
		
		StringBuilder sb = new StringBuilder();
		boolean overNext = false;
		for(NodeConfigResult result : resultList){
			if(result == null){
				continue;
			}
			
			NodeInfo nodeInfo = result.getNodeInfo();
			if(nodeInfo.getNodeConfigSettingId() == null){
				continue;
			}
			
			if(overNext){
				sb.append(", ");
			}
			sb.append("[" + nodeInfo.getNodeConfigSettingId() + "]");
			overNext = true;
		}
		return sb.toString();
	}
	
}
