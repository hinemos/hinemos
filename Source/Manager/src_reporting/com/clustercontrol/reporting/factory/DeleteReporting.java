/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ReportingNotFound;
import com.clustercontrol.reporting.model.ReportingInfoEntity;
import com.clustercontrol.reporting.util.QueryUtil;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * レポーティング情報を削除するためのクラスです。
 * 
 * @version 4.1.2
 *
 */
public class DeleteReporting {

	/**
	 * @param reportId
	 * @return
	 * @throws ReportNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean deleteReporting(String reportId)
			throws ReportingNotFound, InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 削除対象を検索
			ReportingInfoEntity entity = QueryUtil.getReportingInfoPK(reportId);

			//通知情報の削除
			new NotifyControllerBean().deleteNotifyRelation(entity.getNotifyGroupId());

			//レポーティング情報の削除
			em.remove(entity);

			// 通知履歴情報を削除する
			new NotifyControllerBean().deleteNotifyHistory(HinemosModuleConstant.REPORTING, reportId);

			return true;
		}
	}

}
