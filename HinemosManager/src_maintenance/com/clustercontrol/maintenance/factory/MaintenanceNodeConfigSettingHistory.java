/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.repository.model.NodeCpuHistoryDetail;
import com.clustercontrol.repository.model.NodeCustomHistoryDetail;
import com.clustercontrol.repository.model.NodeDiskHistoryDetail;
import com.clustercontrol.repository.model.NodeFilesystemHistoryDetail;
import com.clustercontrol.repository.model.NodeHostnameHistoryDetail;
import com.clustercontrol.repository.model.NodeLicenseHistoryDetail;
import com.clustercontrol.repository.model.NodeMemoryHistoryDetail;
import com.clustercontrol.repository.model.NodeProductHistoryDetail;
import com.clustercontrol.repository.model.NodeNetstatHistoryDetail;
import com.clustercontrol.repository.model.NodeNetworkInterfaceHistoryDetail;
import com.clustercontrol.repository.model.NodeOsHistoryDetail;
import com.clustercontrol.repository.model.NodePackageHistoryDetail;
import com.clustercontrol.repository.model.NodeVariableHistoryDetail;
import com.clustercontrol.util.HinemosTime;

/**
 * 構成情報収集の削除処理
 *
 * @version 6.2.0
 * @since 6.2.0
 *
 */
public class MaintenanceNodeConfigSettingHistory extends MaintenanceObject {

	private static Log m_log = LogFactory.getLog( MaintenanceNodeConfigSettingHistory.class );

	/**
	 * 削除処理
	 */
	@Override
	protected int _delete(Long boundary, boolean status, String ownerRoleId){
		int ret = 0;
		long start = HinemosTime.currentTimeMillis();

		String roleId = null;

		JpaTransactionManager jtm = null;

		try{

			// AdminRoleの場合はファシリティIDを条件にせず、全て削除
			if(!RoleIdConstant.isAdministratorRole(ownerRoleId)){
				roleId = ownerRoleId;
			}

			// 履歴削除処理
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 履歴削除
			int nodeCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryByRegDate(roleId, boundary);
			ret += nodeCount;

			/** CPU情報 */
			// 履歴詳細削除
			int cpuCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeCpuHistoryDetail.class, roleId, boundary);
			ret += cpuCount;

			/** Disk情報 */
			// 履歴詳細削除
			int diskCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeDiskHistoryDetail.class, roleId, boundary);
			ret += diskCount;

			/** Filesystem情報 */
			// 履歴詳細削除
			int filesystemCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeFilesystemHistoryDetail.class, roleId, boundary);
			ret += filesystemCount;

			/** ノード変数情報 */
			// 履歴詳細削除
			int variableCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeVariableHistoryDetail.class, roleId, boundary);
			ret += variableCount;

			/** ホスト名情報 */
			// 履歴詳細削除
			int hostnameCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeHostnameHistoryDetail.class, roleId, boundary);
			ret += hostnameCount;

			/** メモリ情報 */
			// 履歴詳細削除
			int memoryCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeMemoryHistoryDetail.class, roleId, boundary);
			ret += memoryCount;

			/** NIC情報 */
			// 履歴詳細削除
			int nicCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeNetworkInterfaceHistoryDetail.class, roleId, boundary);
			ret += nicCount;

			/** OS情報 */
			// 履歴詳細削除
			int osCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeOsHistoryDetail.class, roleId, boundary);
			ret += osCount;

			/** ネットワーク接続 */
			// 履歴詳細削除
			int netstatCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeNetstatHistoryDetail.class, roleId, boundary);
			ret += netstatCount;

			/** パッケージ情報 */
			// 履歴詳細削除
			int packageCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodePackageHistoryDetail.class, roleId, boundary);
			ret += packageCount;

			/** ユーザ任意情報 */
			// 履歴詳細削除
			int customCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeCustomHistoryDetail.class, roleId, boundary);
			ret += customCount;

			/** 個別導入製品情報 */
			// 履歴詳細削除
			int productCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeProductHistoryDetail.class, roleId, boundary);
			ret += productCount;

			/** ライセンス情報 */
			// 履歴詳細削除
			int licenseCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeLicenseHistoryDetail.class, roleId, boundary);
			ret += licenseCount;

			jtm.commit();

			long deleteTime = HinemosTime.currentTimeMillis() - start;
			m_log.info("_delete() "
					+ "delete count=" + ret
					+ ", nodeCount=" + nodeCount
					+ ", cpuCount=" + cpuCount 
					+ ", diskCount=" + diskCount 
					+ ", filesystemCount=" + filesystemCount 
					+ ", variableCount=" + variableCount
					+ ", hostnameCount=" + hostnameCount 
					+ ", memoryCount=" + memoryCount 
					+ ", nicCount=" + nicCount 
					+ ", osCount=" + osCount 
					+ ", netstatCount=" + netstatCount
					+ ", packageCount=" + packageCount
					+ ", customCount=" + customCount
					+ ", productCount=" + productCount
					+ ", licenseCount=" + licenseCount
					+ ", deleteTime=" + deleteTime  +"ms");

		} catch(Exception e){
			m_log.warn("deleteNodeConfigSettingHistory() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}
}
