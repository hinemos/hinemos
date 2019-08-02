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
	private long dbmsJob;
	private long dbmsDel;
	private long dbmsTrans;
	private long ramMon;
	private long ramJob;

	@ConstructorProperties({"dbmsJob", "dbmsDel", "dbmsTran", "ramMon", "ramJob"})
	public SchedulerDelayTimes(
			long dbmsJob,
			long dbmsDel,
			long dbmsTrans,
			long ramMon,
			long ramJob
			) {
		this.dbmsJob = dbmsJob;
		this.dbmsDel = dbmsDel;
		this.dbmsTrans = dbmsTrans;
		this.ramMon = ramMon;
		this.ramJob = ramJob;
	}

	/**
	 * @return the dbmsJob
	 */
	public long getDbmsJob() {
		return dbmsJob;
	}
	
	/**
	 * @return the dbmsDel
	 */
	public long getDbmsDel() {
		return dbmsDel;
	}
	
	/**
	 * @return the dbmsTrans
	 */
	public long getDbmsTrans() {
		return dbmsTrans;
	}

	/**
	 * @return the ramMon
	 */
	public long getRamMon() {
		return ramMon;
	}
	
	/**
	 * @return the ramJob
	 */
	public long getRamJob() {
		return ramJob;
	}

}