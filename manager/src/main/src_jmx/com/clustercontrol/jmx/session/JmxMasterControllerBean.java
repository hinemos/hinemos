/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jmx.session;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jmx.model.JmxMasterInfo;
import com.clustercontrol.util.MessageConstant;

/**
 * JMX 監視項目マスタ情報を制御するSession Bean <BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class JmxMasterControllerBean {

	private static Log m_log = LogFactory.getLog( JmxMasterControllerBean.class );


	/**
	 * JMX 監視項目マスタを登録します。
	 * 
	 * @param datas JMX 監視項目マスタ
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	public boolean addJmxMasterList(List<JmxMasterInfo> datas) throws HinemosUnknown, InvalidSetting {
		boolean ret = false;

		for (JmxMasterInfo m: datas) {
			validateJmxMasterInfo(m);
		}

		JpaTransactionManager jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();
		try {
			jtm.begin();

			// インスタンス生成
			for (JmxMasterInfo data: datas) {
				// 重複チェック
				jtm.checkEntityExists(JmxMasterInfo.class, data.getId());
				em.persist(data);
			}

			jtm.commit();

			ret = true;
		} catch (EntityExistsException e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("addJmxMasterList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

		return ret;
	}

	/**
	 * JMX 監視項目マスタを削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean deleteJmxMasterList(List<String> ids) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		boolean ret = false;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			HinemosEntityManager em = jtm.getEntityManager();
			List<JmxMasterInfo> entities = em.createNamedQuery("MonitorJmxMstEntity.findList", JmxMasterInfo.class).setParameter("ids", ids).getResultList();
			for (JmxMasterInfo entity : entities) {
				// 削除処理
				em.remove(entity);
			}

			jtm.commit();

			ret = true;
		} catch (Exception e) {
			m_log.warn("deleteJmxMasterList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return ret;

	}

	/**
	 * JMX 監視項目マスタを全て削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean deleteJmxMasterAll() throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		boolean ret = false;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			HinemosEntityManager em = jtm.getEntityManager();
			List<JmxMasterInfo> entities = em.createNamedQuery("MonitorJmxMstEntity.findAll", JmxMasterInfo.class).getResultList();
			for (JmxMasterInfo entity : entities) {
				// 削除処理
				em.remove(entity);
			}

			jtm.commit();

			ret = true;
		} catch (Exception e) {
			m_log.warn("deleteJmxMasterAll() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return ret;

	}

	/**
	 * JMX 監視項目マスタを取得します。
	 * 
	 * @return JMX 監視項目マスタ
	 * @throws HinemosUnknown
	 */
	public List<JmxMasterInfo> getJmxMasterList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<JmxMasterInfo> ret = new ArrayList<>();

		try {
			jtm = new JpaTransactionManager();
			HinemosEntityManager em = jtm.getEntityManager();
			List<JmxMasterInfo> entities = em.createNamedQuery("MonitorJmxMstEntity.findAll", JmxMasterInfo.class).getResultList();
			ret.addAll(entities);
		} catch (Exception e) {
			m_log.warn("getJmxMasterList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return ret;
	}

	private void validateJmxMasterInfo(JmxMasterInfo info) throws InvalidSetting {
		CommonValidator.validateString(MessageConstant.MONITOR_JMX_MASTER_ID.getMessage(), info.getId(), true, 1, 64);
		CommonValidator.validateString(MessageConstant.MONITOR_JMX_MASTER_OBJECTNAME.getMessage(), info.getObjectName(), true, 1, 512);
		CommonValidator.validateString(MessageConstant.MONITOR_JMX_MASTER_ATTRIBUTENAME.getMessage(), info.getAttributeName(), true, 1, 256);
		CommonValidator.validateString(MessageConstant.MONITOR_JMX_MASTER_KEYS.getMessage(), info.getKeys(), false, 0, 512);
		CommonValidator.validateString(MessageConstant.MONITOR_JMX_MASTER_NAME.getMessage(), info.getName(), true, 1, 256);
		CommonValidator.validateString(MessageConstant.MONITOR_JMX_MASTER_MEASURE.getMessage(), info.getMeasure(), true, 1, 64);
	}
}
