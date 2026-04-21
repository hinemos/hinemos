/*
 * Copyright (c) 2025 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.rest.model.RestAgentRequestEntity;

public class RestAPIRequestLock implements AutoCloseable {
	private static Log log = LogFactory.getLog(RestAPIRequestLock.class);

	private static final Map<String, ReentrantLock> requestIdLockMap = new ConcurrentHashMap<>();

	/**
	 * 同一リクエストが処理済みの場合に出力される
	 */
	public static class RequestDuplicateException extends HinemosException {
		private static final long serialVersionUID = 1L;

		public RequestDuplicateException(String message) {
			super(message);
		}
	}

	/**
	 * 同一リクエストが処理中の場合に出力される
	 */
	public static class AlreadyLockedException extends HinemosException {
		private static final long serialVersionUID = 1L;

		public AlreadyLockedException(String message) {
			super(message);
		}
	}

	private String systemFunction;
	private String resourceMethod;
	private String requestId;
	private String agentId;
	private RestControllerBean restControllerBean;

	private boolean autoCommit;

	public RestAPIRequestLock(String systemFunction, String resourceMethod) throws AlreadyLockedException, RequestDuplicateException, HinemosUnknown {
		this(systemFunction, resourceMethod, false);
	}

	public RestAPIRequestLock(String systemFunction, String resourceMethod, boolean autoCommit) throws AlreadyLockedException, RequestDuplicateException, HinemosUnknown {
		this.systemFunction = systemFunction;
		this.resourceMethod = resourceMethod;
		this.autoCommit = autoCommit;

		this.restControllerBean = new RestControllerBean();
		this.requestId = restControllerBean.getAgentRequestId();
		this.agentId = restControllerBean.getAgentIdentifier();

		//ロック
		if (!lock()) {
			String message = "This request from the agent has been received, but it is currently being processed by another thread. "
					+ "requestId = " + requestId + ", agentId = " + agentId
					+ ", systemFunction = " + systemFunction + ", resourceMethod = " + resourceMethod;
			log.info(message);
			//エージェントに返す
			throw new AlreadyLockedException(message);
		}

		// 重複チェック
		RestAgentRequestEntity entity = null;
		try {
			entity = restControllerBean.findRestAgentRequest(requestId);
		} catch (Exception e) {
			log.warn("An unexpected error has occurred. " + e.getClass().getSimpleName() + ", " + e.getMessage()+ " requestId = " + requestId + ", agentId = " + agentId
					+ ", systemFunction = " + systemFunction + ", resourceMethod = " + resourceMethod, e);
			unlock();
			throw new HinemosUnknown(e);
		}
		if (entity != null) {
			// 既に登録済みのため、何も処理をせず結果を返す
			String message = "Duplicate. requestId = " + requestId + ", agentId = " + agentId
					+ ", systemFunction = " + systemFunction + ", resourceMethod = " + resourceMethod + ", regDate = " + entity.getRegDate();
			log.info(message);
			unlock();
			throw new RequestDuplicateException(message);
		}
	}

	protected boolean lock() {
		// MapからLockオブジェクトを取得してロック
		return requestIdLockMap.computeIfAbsent(requestId, k -> new ReentrantLock()).tryLock();
	}

	protected void unlock() {
		requestIdLockMap.computeIfPresent(requestId, (k, v) -> {
			// ロックを解放
			v.unlock();
			// 処理後にnullを返すことでMapから削除する
			return null;
		});
	}

	public void commitRequestId() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();

			restControllerBean.addRestAgentRequest(requestId, agentId, systemFunction, resourceMethod);
			jtm.commit();
		}
	}

	@Override
	public void close() {
		try {
			if (autoCommit) {
				commitRequestId();
			}
		} finally {
			unlock();
		}
	}
}
