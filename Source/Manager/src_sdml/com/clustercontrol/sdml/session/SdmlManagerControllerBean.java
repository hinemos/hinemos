/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.session;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.session.CheckFacility;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.util.QueryUtil;
import com.clustercontrol.util.MessageConstant;

/**
 * SDML機能の管理を行う Session Bean です。<BR>
 * 本体側に必要なSessionはこのクラスに実装します。<BR>
 * クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。
 */
public class SdmlManagerControllerBean implements CheckFacility {
	private static Log logger = LogFactory.getLog(SdmlManagerControllerBean.class);

	/**
	 * ファシリティが利用されているか確認する。
	 * 
	 * @throws UsedFacility
	 * @throws InvalidRole
	 */
	@Override
	public void isUseFacilityId(String facilityId) throws UsedFacility {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			List<SdmlControlSettingInfo> infoCollection = QueryUtil
					.getSdmlControlSettingInfoFindByFacilityId_NONE(facilityId);
			if (infoCollection != null && infoCollection.size() > 0) {
				// ID名を取得する
				StringBuilder sb = new StringBuilder();
				sb.append(MessageConstant.SDML_CONTROL_SETTING.getMessage() + " : ");
				for (SdmlControlSettingInfo entity : infoCollection) {
					sb.append(entity.getApplicationId());
					sb.append(", ");
				}
				UsedFacility e = new UsedFacility(sb.toString());
				logger.info("isUseFacilityId() : " + e.getClass().getSimpleName() + ", " + facilityId + ", "
						+ e.getMessage());
				throw e;
			}
			jtm.commit();
		} catch (UsedFacility e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			logger.warn("isUseFacilityId() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
}
