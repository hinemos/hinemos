/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.clustercontrol.plugin.util.scheduler.Trigger;

/**
 * The persistent class for the cc_dbms_scheduler database table.
 * 
 */
@Entity
@Table(name="cc_dbms_scheduler", schema="setting")
@Cacheable(true)
public class DbmsSchedulerEntity implements Serializable {
	private static final long serialVersionUID	= 1L;
	private DbmsSchedulerEntityPK id	= null;
	private int misfireInstr					= Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY;
	private boolean durable					= true;
	private String jobClassName				= null;
	private String jobMethodName				= null;
	
	private String triggerType					= null;
	private String triggerState				= null;
	private long repeatInterval				= 0;
	private String cronExpression				= null;

	private long startTime						= 0;
	private long endTime						= 0;
	private long nextFireTime					= 0;
	private long prevFireTime					= 0;

	private int jobArgNum						= 0;
	private String jobArg00					= null;
	private String jobArg01					= null;
	private String jobArg02					= null;
	private String jobArg03					= null;
	private String jobArg04					= null;
	private String jobArg05					= null;
	private String jobArg06					= null;
	private String jobArg07					= null;
	private String jobArg08					= null;
	private String jobArg09					= null;
	private String jobArg10					= null;
	private String jobArg11					= null;
	private String jobArg12					= null;
	private String jobArg13					= null;
	private String jobArg14					= null;

	@Deprecated
	public DbmsSchedulerEntity() {
	}

	public DbmsSchedulerEntity(DbmsSchedulerEntityPK id) {
		this.setId(id);
	}

	public DbmsSchedulerEntity(String jobId, String jobGroup) {
		this(new DbmsSchedulerEntityPK(jobId, jobGroup));
	}

	@EmbeddedId
	public DbmsSchedulerEntityPK getId() {
		return this.id;
	}

	public void setId(DbmsSchedulerEntityPK id) {
		this.id = id;
	}
	
	@Column(name="misfire_instr")
	public int getMisfireInstr() {
		return this.misfireInstr;
	}

	public void setMisfireInstr(int misfireInstr) {
		this.misfireInstr = misfireInstr;
	}

	@Column(name="durable")
	public boolean getDurable() {
		return this.durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	@Column(name="job_class_name")
	public String getJobClassName() {
		return this.jobClassName;
	}

	public void setJobClassName(String jobClassName) {
		this.jobClassName = jobClassName;
	}

	@Column(name="job_method_name")
	public String getJobMethodName() {
		return this.jobMethodName;
	}

	public void setJobMethodName(String jobMethodName) {
		this.jobMethodName = jobMethodName;
	}

	@Column(name="trigger_type")
	public String getTriggerType() {
		return this.triggerType;
	}

	public void setTriggerType(String triggerType) {
		this.triggerType = triggerType;
	}

	@Column(name="trigger_state")
	public String getTriggerState() {
		return this.triggerState;
	}

	public void setTriggerState(String triggerState) {
		this.triggerState = triggerState;
	}

	@Column(name="repeat_interval")
	public long getRepeatInterval() {
		return this.repeatInterval;
	}

	public void setRepeatInterval(long repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

	@Column(name="cron_expression")
	public String getCronExpression() {
		return this.cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Column(name="start_time")
	public long getStartTime() {
		return this.startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	@Column(name="end_time")
	public long getEndTime() {
		return this.endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	@Column(name="next_fire_time")
	public long getNextFireTime() {
		return this.nextFireTime;
	}

	public void setNextFireTime(long nextFireTime) {
		this.nextFireTime = nextFireTime;
	}

	@Column(name="prev_fire_time")
	public long getPrevFireTime() {
		return this.prevFireTime;
	}

	public void setPrevFireTime(long prevFireTime) {
		this.prevFireTime = prevFireTime;
	}

	@Column(name="job_arg_num")
	public int getJobArgNum() {
		return this.jobArgNum;
	}

	public void setJobArgNum(int jobArgNum) {
		this.jobArgNum = jobArgNum;
	}

	@Column(name="job_arg00")
	public String getJobArg00() {
		return this.jobArg00;
	}

	public void setJobArg00(String jobArg) {
		this.jobArg00 = jobArg;
	}

	@Column(name="job_arg01")
	public String getJobArg01() {
		return this.jobArg01;
	}

	public void setJobArg01(String jobArg) {
		this.jobArg01 = jobArg;
	}
	

	@Column(name="job_arg02")
	public String getJobArg02() {
		return this.jobArg02;
	}

	public void setJobArg02(String jobArg) {
		this.jobArg02 = jobArg;
	}

	@Column(name="job_arg03")
	public String getJobArg03() {
		return this.jobArg03;
	}

	public void setJobArg03(String jobArg) {
		this.jobArg03 = jobArg;
	}

	@Column(name="job_arg04")
	public String getJobArg04() {
		return this.jobArg04;
	}

	public void setJobArg04(String jobArg) {
		this.jobArg04 = jobArg;
	}

	@Column(name="job_arg05")
	public String getJobArg05() {
		return this.jobArg05;
	}

	public void setJobArg05(String jobArg) {
		this.jobArg05 = jobArg;
	}

	@Column(name="job_arg06")
	public String getJobArg06() {
		return this.jobArg06;
	}

	public void setJobArg06(String jobArg) {
		this.jobArg06 = jobArg;
	}

	@Column(name="job_arg07")
	public String getJobArg07() {
		return this.jobArg07;
	}

	public void setJobArg07(String jobArg) {
		this.jobArg07 = jobArg;
	}

	@Column(name="job_arg08")
	public String getJobArg08() {
		return this.jobArg08;
	}

	public void setJobArg08(String jobArg) {
		this.jobArg08 = jobArg;
	}

	@Column(name="job_arg09")
	public String getJobArg09() {
		return this.jobArg09;
	}

	public void setJobArg09(String jobArg) {
		this.jobArg09 = jobArg;
	}

	@Column(name="job_arg10")
	public String getJobArg10() {
		return this.jobArg10;
	}

	public void setJobArg10(String jobArg) {
		this.jobArg10 = jobArg;
	}

	@Column(name="job_arg11")
	public String getJobArg11() {
		return this.jobArg11;
	}

	public void setJobArg11(String jobArg) {
		this.jobArg11 = jobArg;
	}

	@Column(name="job_arg12")
	public String getJobArg12() {
		return this.jobArg12;
	}

	public void setJobArg12(String jobArg) {
		this.jobArg12 = jobArg;
	}

	@Column(name="job_arg13")
	public String getJobArg13() {
		return this.jobArg13;
	}

	public void setJobArg13(String jobArg) {
		this.jobArg13 = jobArg;
	}

	@Column(name="job_arg14")
	public String getJobArg14() {
		return this.jobArg14;
	}

	public void setJobArg14(String jobArg) {
		this.jobArg14 = jobArg;
	}

}