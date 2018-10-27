/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

/**
 * Collector Task Interface
 * 
 * @since 4.0
 */
public interface CollectorTask {

	public CollectorId getCollectorId();

	public void start();

	public void shutdown();

	public void update(CollectorTask task);

	@Override
	public String toString();

}
