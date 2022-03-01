/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.EntityExistsException;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.LogFormatDuplicate;
import com.clustercontrol.fault.LogFormatNotFound;
import com.clustercontrol.fault.LogFormatUsed;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.hub.model.LogFormat;
import com.clustercontrol.hub.model.LogFormatKey;
import com.clustercontrol.hub.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * ログフォーマットを更新
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class ModifyLogFormat {
	private static Logger m_log = Logger.getLogger( ModifyLogFormat.class );
	
	/**
	 * ログフォーマットを追加」します。
	 * 
	 * @param entity
	 * @param userId
	 * @throws LogFormatDuplicate
	 * @throws HinemosUnknown
	 */
	public void add(LogFormat entity, String userId) throws LogFormatDuplicate, HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			long now = HinemosTime.currentTimeMillis();
			jtm.checkEntityExists(LogFormat.class, entity.getLogFormatId());
			
			entity.setRegDate(now);
			entity.setRegUser(userId);
			entity.setUpdateDate(now);
			entity.setUpdateUser(userId);
			
			em.persist(entity);
			jtm.flush();
			
		} catch (EntityExistsException e) {
			throw new LogFormatDuplicate(MessageConstant.MESSAGE_HUB_LOG_FORMAT_DUPLICATION_ID.getMessage(entity.getLogFormatId()));
		}
	}
	
	/**
	 * ログフォーマットを変更します。
	 * 
	 * @param logFormat
	 * @param userId
	 * @throws LogformatNotFound
	 * @throws InvalidRole
	 */
	public void modify(LogFormat logFormat, String userId) throws LogFormatNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			String logFormatId = logFormat.getLogFormatId();
			LogFormat entity = em.find(LogFormat.class, logFormatId, ObjectPrivilegeMode.MODIFY);
			if (entity == null) {
				LogFormatNotFound e = new LogFormatNotFound("LogFormat.findByPrimaryKey, logFormatId = " + logFormatId);
				m_log.info("modify() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			entity.setDescription(logFormat.getDescription());
			entity.setTimestampRegex(logFormat.getTimestampRegex());
			entity.setTimestampFormat(logFormat.getTimestampFormat());
			entity.setUpdateUser(userId);
			entity.setUpdateDate(HinemosTime.currentTimeMillis());
			
			List<LogFormatKey> webPatternList = new ArrayList<LogFormatKey>(logFormat.getKeyPatternList());
			List<LogFormatKey> patternList = new ArrayList<LogFormatKey>(entity.getKeyPatternList());
			
			Iterator<LogFormatKey> webPatternIter = webPatternList.iterator();
			while (webPatternIter.hasNext()) {
				LogFormatKey webPattern = webPatternIter.next();
				Iterator<LogFormatKey> patternIter = patternList.iterator();
				while (patternIter.hasNext()) {
					LogFormatKey pattern = patternIter.next();
					if (webPattern.getKey().equals(pattern.getKey())) {
						pattern.setDescription(webPattern.getDescription());
						pattern.setValueType(webPattern.getValueType());
						pattern.setKeyType(webPattern.getKeyType());
						pattern.setPattern(webPattern.getPattern());
						pattern.setValue(webPattern.getValue());
						
						webPatternIter.remove();
						patternIter.remove();
						break;
					}
				}
			}
			
			for (LogFormatKey webPattern: webPatternList) {
				entity.getKeyPatternList().add(webPattern);
			}
			
			for (LogFormatKey pattern: patternList) {
				entity.getKeyPatternList().remove(pattern);
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("modify() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
	}
	
	/**
	 * ログフォーマットを削除します。
	 * 
	 * @param logFormatId
	 * @throws LogformatNotFound
	 * @throws LogformatUsed
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void delete(String logFormatId) throws LogFormatNotFound, LogFormatUsed, InvalidRole, HinemosUnknown {
		m_log.debug(String.format("delete() : logFormatd = %s", logFormatId));

		// ファイルを取得
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			LogFormat entity = em.find(LogFormat.class, logFormatId, ObjectPrivilegeMode.MODIFY);
			if (entity == null) {
				LogFormatNotFound e = new LogFormatNotFound("LogFormat.findByPrimaryKey, logFormatId = " + logFormatId);
				m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			if (QueryUtil.isLogFormatUsed(logFormatId)) {
				LogFormatUsed e = new LogFormatUsed("LogFormat is used by MonitorInfo, logFormatId = " + logFormatId);
				m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			em.remove(entity);
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
	}
}