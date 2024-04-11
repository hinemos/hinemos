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

import jakarta.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.jmx.model.JmxCheckInfo;
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
	 * @return 登録したJMX監視項目マスタ一覧
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	public List<JmxMasterInfo> addJmxMasterList(List<JmxMasterInfo> datas) throws HinemosUnknown, InvalidSetting {
		List<JmxMasterInfo> retList = new ArrayList<>();

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

			// 結果取得
			List<String> ids = new ArrayList<>();
			for (JmxMasterInfo data: datas) {
				ids.add(data.getId());
			}
			List<JmxMasterInfo> entities = em.createNamedQuery("MonitorJmxMstEntity.findList", JmxMasterInfo.class).setParameter("ids", ids).getResultList();
			for (JmxMasterInfo entity : entities) {
				retList.add(entity);
			}
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

		return retList;
	}

	/**
	 * JMX 監視項目マスタを削除します。
	 * 
	 * @return 削除したJMX監視項目マスタ一覧
	 * @throws HinemosUnknown
	 */
	public List<JmxMasterInfo> deleteJmxMasterList(List<String> ids) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<JmxMasterInfo> retList = new ArrayList<>();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			HinemosEntityManager em = jtm.getEntityManager();
			List<JmxMasterInfo> entities = em.createNamedQuery("MonitorJmxMstEntity.findList", JmxMasterInfo.class).setParameter("ids", ids).getResultList();
			for (JmxMasterInfo entity : entities) {
				List<JmxCheckInfo> jmxCheckInfoList = em
						.createNamedQuery("JmxCheckInfo.findByMasterId", JmxCheckInfo.class, ObjectPrivilegeMode.NONE)
						.setParameter("masterId", entity.getId()).getResultList();
				// 他の機能にて、JMXMaster情報が参照状態であるか調査する。
				if (jmxCheckInfoList.isEmpty()==false )  {
					StringBuilder sb = new StringBuilder();
					sb.append(MessageConstant.MONITOR_SETTING.getMessage() + " : ");
					for (JmxCheckInfo jmxinfo : jmxCheckInfoList) {
						sb.append(jmxinfo.getMonitorId());
						sb.append(", ");
					}
					sb.append(MessageConstant.MASTER_ID.getMessage() + " : ");
					sb.append( entity.getId());
					sb.append("\n");
					sb.append(MessageConstant.JMX_NOTIFY_DEPENDENCY.getMessage());
					//sb.append(Messages.getString("messages.notify.jmx.dependency"));
					UsedFacility e = new UsedFacility(sb.toString());									
					throw e;																							
					}
				else {
					retList.add(entity);
					// 削除処理
					em.remove(entity);
				}			
				jtm.commit();
			}		
		}
		catch (UsedFacility e) {
			m_log.warn("deleteJmxMasterList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage()+"", e);		
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
		return retList;

	}

	/**
	 * JMX 監視項目マスタを全て削除します。
	 * 
	 * @return 削除したJMX監視項目マスタ一覧
	 * @throws HinemosUnknown
	 */
	public List<JmxMasterInfo> deleteJmxMasterAll() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<JmxMasterInfo> retList = new ArrayList<>();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			HinemosEntityManager em = jtm.getEntityManager();

			List<JmxMasterInfo> entities = em.createNamedQuery("MonitorJmxMstEntity.findAll", JmxMasterInfo.class).getResultList();
			for (JmxMasterInfo entity : entities) {
				// 他の機能にて、JMXMaster情報が参照されていないかチェック
				List<JmxCheckInfo> jmxCheckInfoList = em
						.createNamedQuery("JmxCheckInfo.findByMasterId", JmxCheckInfo.class, ObjectPrivilegeMode.NONE)
						.setParameter("masterId", entity.getId()).getResultList();
				if (jmxCheckInfoList.isEmpty() == false) {
					m_log.warn("deleteJmxMasterAll() : Jmx master is used. master id:" + entity.getId() );
					StringBuilder sb = new StringBuilder();
					sb.append(MessageConstant.MONITOR_SETTING.getMessage() + " : ");
					for (JmxCheckInfo jmxinfo : jmxCheckInfoList) {
						sb.append(jmxinfo.getMonitorId());
						sb.append(", ");
					}
					sb.append(MessageConstant.MASTER_ID.getMessage() + " : ");
					sb.append(entity.getId());
					sb.append("\n");
					sb.append(MessageConstant.JMX_NOTIFY_DEPENDENCY.getMessage());
					UsedFacility e = new UsedFacility(sb.toString());
					throw e;
				}
			}

			for (JmxMasterInfo entity : entities) {
				retList.add(entity);
				// 削除処理
				em.remove(entity);
			}

			jtm.commit();

		} catch (UsedFacility e) {
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
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
		return retList;

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
