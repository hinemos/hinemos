package com.clustercontrol.monitor.plugin.util;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.plugin.model.MonitorPluginNumericInfo;
import com.clustercontrol.monitor.plugin.model.MonitorPluginNumericInfoEntityPK;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfo;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfoEntityPK;
import com.clustercontrol.monitor.plugin.model.PluginCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;

public class PluginCheckInfoUtil {

	public boolean addCheckInfo(PluginCheckInfo info) throws MonitorNotFound, HinemosUnknown,
		InvalidRole {
		
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(info);
		
		for (MonitorPluginNumericInfo num: info.getMonitorPluginNumericInfoList()) {
			em.persist(num);
		}
		for (MonitorPluginStringInfo str: info.getMonitorPluginStringInfoList()) {
			em.persist(str);
		}
		return true;
	}

	public boolean modifyCheckInfo(PluginCheckInfo info) throws MonitorNotFound, HinemosUnknown,
		InvalidRole {
		MonitorInfo monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(info.getMonitorId());

		// 監視情報を取得
		PluginCheckInfo pluginEntity = com.clustercontrol.monitor.plugin.util.QueryUtil.getMonitorPluginInfoPK(info.getMonitorId());

		// 監視情報を設定
		PluginCheckInfo plugin = info;
		monitorEntity.setPluginCheckInfo(pluginEntity);

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		////
		// 監視情報(数値リスト)を設定
		////
		List<MonitorPluginNumericInfo> monitorPluginNumericInfoList = plugin.getMonitorPluginNumericInfoList();
		if(monitorPluginNumericInfoList != null){
			List<MonitorPluginNumericInfoEntityPK> monitorPluginNumericInfoEntityPKList = new ArrayList<MonitorPluginNumericInfoEntityPK>();
			for(MonitorPluginNumericInfo value : monitorPluginNumericInfoList){
				if(value != null){
					MonitorPluginNumericInfo nEntity = null;
					MonitorPluginNumericInfoEntityPK entityPk = new MonitorPluginNumericInfoEntityPK(
							value.getMonitorId(),
							value.getKey());
					try {
						nEntity = com.clustercontrol.monitor.plugin.util.QueryUtil.getMonitorPluginNumericInfoEntity(entityPk);
					} catch (MonitorNotFound e) {
						nEntity = new MonitorPluginNumericInfo(entityPk);
						em.persist(this);
						nEntity.relateToMonitorPluginInfoEntity(pluginEntity);
					}
					nEntity.setValue(value.getValue());
					monitorPluginNumericInfoEntityPKList.add(entityPk);
				}
			}
			// 不要なMonitorPluginNumericInfoEntityを削除
			pluginEntity.deleteMonitorPluginNumericInfoEntities(monitorPluginNumericInfoEntityPKList);
		}


		////
		// 監視情報(文字列リスト)を設定
		////
		List<MonitorPluginStringInfo> monitorPluginStringInfoList = plugin.getMonitorPluginStringInfoList();
		if(monitorPluginStringInfoList != null){
			List<MonitorPluginStringInfoEntityPK> monitorPluginStringInfoEntityPKList = new ArrayList<MonitorPluginStringInfoEntityPK>();
			for(MonitorPluginStringInfo value : monitorPluginStringInfoList){
				if(value != null){
					MonitorPluginStringInfo sEntity = null;
					MonitorPluginStringInfoEntityPK entityPk = new MonitorPluginStringInfoEntityPK(
							value.getMonitorId(),
							value.getKey());
					try {
						sEntity = com.clustercontrol.monitor.plugin.util.QueryUtil.getMonitorPluginStringInfoEntity(entityPk);
					} catch (MonitorNotFound e) {
						sEntity = new MonitorPluginStringInfo(entityPk, pluginEntity);
					}
					sEntity.setValue(value.getValue());
					monitorPluginStringInfoEntityPKList.add(entityPk);
				}
			}
			// 不要なMonitorPluginStringInfoEntityを削除
			pluginEntity.deleteMonitorPluginStringInfoEntities(monitorPluginStringInfoEntityPKList);

		}

		return true;
	}
}
