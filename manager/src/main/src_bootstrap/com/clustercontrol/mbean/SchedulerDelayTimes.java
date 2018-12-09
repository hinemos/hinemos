/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.mbean;

import java.beans.ConstructorProperties;

public class SchedulerDelayTimes {
	private long dbms;
	private long ram;

	@ConstructorProperties({"dbms", "ram"})
	public SchedulerDelayTimes(
			long dbms,
			long ram
			) {
		this.dbms = dbms;
		this.ram = ram;
	}

	/**
	 * @return the dbms
	 */
	public long getDbms() {
		return dbms;
	}

	/**
	 * @return the ram
	 */
	public long getRam() {
		return ram;
	}

}