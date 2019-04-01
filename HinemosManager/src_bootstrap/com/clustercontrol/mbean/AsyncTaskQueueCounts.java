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
	private int agentRestartTaskFactory;
	private int agentUpdateTaskFactory;

	@ConstructorProperties({"notifyStatusTaskFactory", "notifyEventTaskFactory", "notifyMailTaskFactory", 
		"notifyCommandTaskFactory", "notifyLogEscalationTaskFactory", "notifyJobTaskFactory", "createJobSessionTaskFactory", 
		"notifyInfraTaskFactory", "agentRestartTaskFactory", "agentUpdateTaskFactory"})
	public AsyncTaskQueueCounts(
			int notifyStatusTaskFactory,
			int notifyEventTaskFactory,
			int notifyMailTaskFactory,
			int notifyCommandTaskFactory,
			int notifyLogEscalationTaskFactory,
			int notifyJobTaskFactory,
			int createJobSessionTaskFactory,
			int notifyInfraTaskFactory,
			int agentRestartTaskFactory,
			int agentUpdateTaskFactory
			) {
		this.notifyStatusTaskFactory = notifyStatusTaskFactory;
		this.notifyEventTaskFactory = notifyEventTaskFactory;
		this.notifyMailTaskFactory = notifyMailTaskFactory;
		this.notifyCommandTaskFactory = notifyCommandTaskFactory;
		this.notifyLogEscalationTaskFactory = notifyLogEscalationTaskFactory;
		this.notifyJobTaskFactory = notifyJobTaskFactory;
		this.createJobSessionTaskFactory = createJobSessionTaskFactory;
		this.notifyInfraTaskFactory = notifyInfraTaskFactory;
		this.agentRestartTaskFactory = agentRestartTaskFactory;
		this.agentUpdateTaskFactory = agentUpdateTaskFactory;
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
}