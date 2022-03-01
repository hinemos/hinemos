/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.mbean;

import java.beans.ConstructorProperties;

public class AsyncTaskQueueCounts {
	private int notifyStatusTaskFactory;
	private int notifyEventTaskFactory;
	private int notifyMailTaskFactory;
	private int notifyCommandTaskFactory;
	private int notifyLogEscalationTaskFactory;
	private int notifyJobTaskFactory;
	private int createJobSessionTaskFactory;
	private int notifyInfraTaskFactory;
	private int notifyRestTaskFactory;
	private int notifyCloudTaskFactory;
	private int agentRestartTaskFactory;
	private int agentUpdateTaskFactory;
	private int agentBroadcastAwakeTaskFactory;
	private int notifyMessageTaskFactory;
	
	@ConstructorProperties({
			"notifyStatusTaskFactory",
			"notifyEventTaskFactory",
			"notifyMailTaskFactory",
			"notifyCommandTaskFactory",
			"notifyLogEscalationTaskFactory",
			"notifyJobTaskFactory",
			"createJobSessionTaskFactory",
			"notifyInfraTaskFactory",
			"notifyRestTaskFactory",
			"notifyCloudTaskFactory",
			"agentRestartTaskFactory",
			"agentUpdateTaskFactory",
			"agentBroadcastAwakeTaskFactory",
			"notifyMessageTaskFactory"
			})
	public AsyncTaskQueueCounts(
			int notifyStatusTaskFactory,
			int notifyEventTaskFactory,
			int notifyMailTaskFactory,
			int notifyCommandTaskFactory,
			int notifyLogEscalationTaskFactory,
			int notifyJobTaskFactory,
			int createJobSessionTaskFactory,
			int notifyInfraTaskFactory,
			int notifyRestTaskFactory,
			int notifyCloudTaskFactory,
			int agentRestartTaskFactory,
			int agentUpdateTaskFactory,
			int agentBroadcastAwakeTaskFactory,
			int notifyMessageTaskFactory
			) {
		this.notifyStatusTaskFactory = notifyStatusTaskFactory;
		this.notifyEventTaskFactory = notifyEventTaskFactory;
		this.notifyMailTaskFactory = notifyMailTaskFactory;
		this.notifyCommandTaskFactory = notifyCommandTaskFactory;
		this.notifyLogEscalationTaskFactory = notifyLogEscalationTaskFactory;
		this.notifyJobTaskFactory = notifyJobTaskFactory;
		this.createJobSessionTaskFactory = createJobSessionTaskFactory;
		this.notifyInfraTaskFactory = notifyInfraTaskFactory;
		this.notifyRestTaskFactory = notifyRestTaskFactory;
		this.notifyCloudTaskFactory = notifyCloudTaskFactory;
		this.agentRestartTaskFactory = agentRestartTaskFactory;
		this.agentUpdateTaskFactory = agentUpdateTaskFactory;
		this.agentBroadcastAwakeTaskFactory = agentBroadcastAwakeTaskFactory;
		this.notifyMessageTaskFactory = notifyMessageTaskFactory;
	}

	/**
	 * @return the notifyStatusTaskFactory
	 */
	public int getNotifyStatusTaskFactory() {
		return notifyStatusTaskFactory;
	}

	/**
	 * @return the notifyEventTaskFactory
	 */
	public int getNotifyEventTaskFactory() {
		return notifyEventTaskFactory;
	}

	/**
	 * @return the notifyMailTaskFactory
	 */
	public int getNotifyMailTaskFactory() {
		return notifyMailTaskFactory;
	}

	/**
	 * @return the notifyCommandTaskFactory
	 */
	public int getNotifyCommandTaskFactory() {
		return notifyCommandTaskFactory;
	}

	/**
	 * @return the notifyLogEscalationTaskFactory
	 */
	public int getNotifyLogEscalationTaskFactory() {
		return notifyLogEscalationTaskFactory;
	}

	/**
	 * @return the notifyJobTaskFactory
	 */
	public int getNotifyJobTaskFactory() {
		return notifyJobTaskFactory;
	}

	/**
	 * @return the createJobSessionTaskFactory
	 */
	public int getCreateJobSessionTaskFactory() {
		return createJobSessionTaskFactory;
	}

	/**
	 * @return the notifyInfraTaskFactory
	 */
	public int getNotifyInfraTaskFactory() {
		return notifyInfraTaskFactory;
	}

	/**
	 * @return the notifyRestTaskFactory
	 */
	public int getNotifyRestTaskFactory() {
		return notifyRestTaskFactory;
	}

	/**
	 * @return the notifyCloudTaskFactory
	 */
	public int getNotifyCloudTaskFactory() {
		return notifyCloudTaskFactory;
	}

	/**
	 * @return the agentRestartTaskFactory
	 */
	public int getAgentRestartTaskFactory() {
		return agentRestartTaskFactory;
	}

	/**
	 * @return the agentUpdateTaskFactory
	 */
	public int getAgentUpdateTaskFactory() {
		return agentUpdateTaskFactory;
	}
	
	/**
	 * @return the agentBroadcastAwakeTaskFactory
	 */
	public int getAgentBroadcastAwakeTaskFactory() {
		return agentBroadcastAwakeTaskFactory;
	}

	/**
	 * @return the notifyMessageTaskFactory
	 */
	public int getNotifyMessageTaskFactory() {
		return notifyMessageTaskFactory;
	}
}