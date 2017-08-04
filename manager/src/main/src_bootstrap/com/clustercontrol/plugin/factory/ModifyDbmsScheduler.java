/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.plugin.factory;


import java.io.InvalidClassException;
import java.io.Serializable;

import javax.persistence.EntityExistsException;
import org.eclipse.persistence.exceptions.DatabaseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.quartz.job.ReflectionInvokerJob;
import com.clustercontrol.fault.DbmsSchedulerNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.model.DbmsSchedulerEntity;
import com.clustercontrol.plugin.model.DbmsSchedulerEntityPK;
import com.clustercontrol.plugin.util.QueryUtil;
import com.clustercontrol.plugin.util.scheduler.JobDetail;
import com.clustercontrol.plugin.util.scheduler.Trigger;
import com.clustercontrol.plugin.util.scheduler.CronTrigger;
import com.clustercontrol.plugin.util.scheduler.SimpleTrigger;
import com.clustercontrol.plugin.util.scheduler.TriggerState;



/**
 * DbmsScheduler情報を更新するためのクラスです。
 * 
 */
public class ModifyDbmsScheduler {

	private static Log m_log = LogFactory.getLog( ModifyDbmsScheduler.class );
	
	
	private void setEntityInfo(DbmsSchedulerEntity entity, JobDetail jobDetail, Trigger trigger) throws InvalidClassException {
		
		entity.setMisfireInstr(trigger.getMisfireInstruction());
		entity.setDurable(jobDetail.isDurable());
		entity.setJobClassName(jobDetail.getJobDataMap().getString(ReflectionInvokerJob.KEY_CLASS_NAME));
		entity.setJobMethodName(jobDetail.getJobDataMap().getString(ReflectionInvokerJob.KEY_METHOD_NAME));
		
		if(trigger instanceof CronTrigger){
			entity.setTriggerType(SchedulerPlugin.TriggerType.CRON.name());
			entity.setCronExpression(((CronTrigger)trigger).getCronExpression());
		} else if(trigger instanceof SimpleTrigger){
			entity.setTriggerType(SchedulerPlugin.TriggerType.SIMPLE.name());
			entity.setRepeatInterval(((SimpleTrigger)trigger).getPeriod());
		}
		
		entity.setTriggerState(TriggerState.VIRGIN.name());
		entity.setStartTime(trigger.getStartTime());
		entity.setEndTime(trigger.getEndTime());
		entity.setNextFireTime(trigger.getNextFireTime());
		entity.setPrevFireTime(trigger.getPreviousFireTime());
		
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] argsType = (Class<? extends Serializable>[])jobDetail.getJobDataMap().get(ReflectionInvokerJob.KEY_ARGS_TYPE);
		Object[] args = (Object[])jobDetail.getJobDataMap().get(ReflectionInvokerJob.KEY_ARGS);
		
		entity.setJobArgNum(args.length);
		for (int i=0; i < args.length; i++) {
			
			String arg = "";
			if (m_log.isDebugEnabled()) m_log.debug("arg[" + i + "]:" + args[i]);
			
			if (argsType[i] == String.class){
				arg = (String)args[i];
			} else if (argsType[i] == Boolean.class){
				arg = Boolean.toString((Boolean)args[i]);
			} else if (argsType[i] == Integer.class){
				arg = Integer.toString((Integer)args[i]);
			} else if (argsType[i] == Long.class){
				arg = Long.toString((Long)args[i]);
			} else if (argsType[i] == Short.class){
				arg = Short.toString((Short)args[i]);
			} else if (argsType[i] == Float.class){
				arg = Float.toString((Float)args[i]);
			} else if (argsType[i] == Double.class){
				arg = Double.toString((Double)args[i]);
			} else {
				m_log.error("not support class");
				throw new InvalidClassException(argsType[i].getName());
			}
			String typeArg = argsType[i].getSimpleName() + ":" + arg;
			if (arg == null) {
				typeArg = "nullString";
			}
			if (m_log.isDebugEnabled()) m_log.debug("typeArg[" + i + "]:" + typeArg);
			
			switch (i) {
			case 0  : entity.setJobArg00(typeArg); break;
			case 1  : entity.setJobArg01(typeArg); break;
			case 2  : entity.setJobArg02(typeArg); break;
			case 3  : entity.setJobArg03(typeArg); break;
			case 4  : entity.setJobArg04(typeArg); break;
			case 5  : entity.setJobArg05(typeArg); break;
			case 6  : entity.setJobArg06(typeArg); break;
			case 7  : entity.setJobArg07(typeArg); break;
			case 8  : entity.setJobArg08(typeArg); break;
			case 9  : entity.setJobArg09(typeArg); break;
			case 10 : entity.setJobArg10(typeArg); break;
			case 11 : entity.setJobArg11(typeArg); break;
			case 12 : entity.setJobArg12(typeArg); break;
			case 13 : entity.setJobArg13(typeArg); break;
			case 14 : entity.setJobArg14(typeArg); break;
			default: m_log.error("arg count ng.");
			}
		}
	}
	
	/**
	 * @param jobDetail
	 * @param trigger
	 * @return
	 * @throws EntityExistsException
	 * @throws InvalidClassException 
	 */
	public void addDbmsScheduler(JobDetail jobDetail, Trigger trigger)
			throws EntityExistsException, InvalidClassException {
		
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// 重複チェック
			jtm.checkEntityExists(DbmsSchedulerEntity.class, new DbmsSchedulerEntityPK(jobDetail.getName(), jobDetail.getGroup()));
			
			DbmsSchedulerEntity entity = new DbmsSchedulerEntity(jobDetail.getName(), jobDetail.getGroup());
			setEntityInfo(entity, jobDetail, trigger);
			
			HinemosEntityManager em = jtm.getEntityManager();
			em.persist(entity);
			
			jtm.commit();
		} catch (EntityExistsException e){
			m_log.error("DbmsSchedulerEntity entity is already exists. (name = " + jobDetail.getName() + ", group = " + jobDetail.getGroup() + ")");
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (DatabaseException e){
			m_log.error("addDbmsScheduler() DatabaseException: (name = " + jobDetail.getName() + ", group = " + jobDetail.getGroup() + ")");
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.error("modifyDbmsScheduler() Exception. (name = " + jobDetail.getName() + ", group = " + jobDetail.getGroup() + ")");
			m_log.error("modifyDbmsScheduler(): " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw e;
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}
	
	/**
	 * @param jobDetail
	 * @param trigger
	 * @throws DbmsSchedulerNotFound
	 * @throws InvalidRole
	 * @throws InvalidClassException 
	 */
	public void modifyDbmsScheduler(JobDetail jobDetail, Trigger trigger)
			throws DbmsSchedulerNotFound, InvalidRole, HinemosUnknown, InvalidClassException {
		
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			DbmsSchedulerEntity entity = QueryUtil.getDbmsSchedulerPK(jobDetail.getName(), jobDetail.getGroup(), ObjectPrivilegeMode.MODIFY);
			setEntityInfo(entity, jobDetail, trigger);
			
			jtm.commit();
		} catch (DbmsSchedulerNotFound e){
			m_log.error("DbmsSchedulerEntity entity is not found. (name = " + jobDetail.getName() + ", group = " + jobDetail.getGroup() + ")");
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (DatabaseException e){
			m_log.error("modifyDbmsScheduler() DatabaseException: (name = " + jobDetail.getName() + ", group = " + jobDetail.getGroup() + ")");
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.error("modifyDbmsScheduler() Exception. (name = " + jobDetail.getName() + ", group = " + jobDetail.getGroup() + ")");
			m_log.error("modifyDbmsScheduler(): " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw e;
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * @param jobDetail
	 * @param trigger
	 * @param state
	 * @throws DbmsSchedulerNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public void modifyDbmsSchedulerInternal(JobDetail jobDetail, Trigger trigger, String state)
			throws DbmsSchedulerNotFound, InvalidRole, HinemosUnknown {
		
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			DbmsSchedulerEntity entity = QueryUtil.getDbmsSchedulerPK(jobDetail.getName(), jobDetail.getGroup(), ObjectPrivilegeMode.MODIFY);
			entity.setTriggerState(state);
			entity.setNextFireTime(trigger.getNextFireTime());
			entity.setPrevFireTime(trigger.getPreviousFireTime());
			entity.setEndTime(trigger.getEndTime());
			
			if (m_log.isDebugEnabled()) {
				m_log.debug("JOBID=" + jobDetail.getName() + ", GROUP=" + jobDetail.getGroup() + ", state=" + state);
				m_log.debug("getNextFireTime=" + trigger.getNextFireTime() + ", getPreviousFireTime=" + trigger.getPreviousFireTime());
			}
			
			jtm.commit();
		} catch (Exception e) {
			m_log.error("modifyDbmsSchedulerInternal() Exception. (name = " + jobDetail.getName() + ", group = " + jobDetail.getGroup() + ")");
			m_log.error("modifyDbmsSchedulerInternal(): " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw e;
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}
	
	/**
	 * @param name
	 * @param group
	 * @throws InvalidRole
	 */
	public void deleteDbmsScheduler(String name, String group)
			throws InvalidRole {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// 削除対象を検索
			DbmsSchedulerEntity entity = QueryUtil.getDbmsSchedulerPK(name, group, ObjectPrivilegeMode.MODIFY);
			// DB情報の削除
			HinemosEntityManager em = jtm.getEntityManager();
			em.remove(entity);
			
			jtm.commit();
		} catch(DbmsSchedulerNotFound e) {
			// 未登録の場合は何もしない
			m_log.debug("DbmsSchedulerEntity entity is not found. (name = " + name + ", group = " + group + ")");
		} catch (Exception e) {
			m_log.error("deleteDbmsScheduler() Exception. (name = " + name + ", group = " + group + ")");
			m_log.error("deleteDbmsScheduler(): " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw e;
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}
}
