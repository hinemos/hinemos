/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory.task;

import com.clustercontrol.xcloud.persistence.Transactional;

public interface ICloudTaskStore {
	String getData();
	void save(String data);
	void save(String data, Transactional.TransactionOption transactionType);
}
