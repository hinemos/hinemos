package com.clustercontrol.monitor.plugin.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.plugin.model.PluginCheckInfo;
import com.clustercontrol.monitor.plugin.model.MonitorPluginNumericInfo;
import com.clustercontrol.monitor.plugin.model.MonitorPluginNumericInfoEntityPK;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfo;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfoEntityPK;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static PluginCheckInfo getMonitorPluginInfoPK(String monitorId) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		PluginCheckInfo entity = em.find(PluginCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorPluginInfoEntity.findByPrimaryKey"
					+ ", monitorId = " + monitorId);
			m_log.info("getMonitorPluginInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}


	public static MonitorPluginNumericInfo getMonitorPluginNumericInfoEntity(MonitorPluginNumericInfoEntityPK pk) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorPluginNumericInfo entity = em.find(MonitorPluginNumericInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorPluginNumericInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getMonitorPluginNumericInfoEntity() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static MonitorPluginStringInfo getMonitorPluginStringInfoEntity(MonitorPluginStringInfoEntityPK pk) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorPluginStringInfo entity = em.find(MonitorPluginStringInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorPluginStringInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getMonitorPluginStringInfoEntity() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

}
