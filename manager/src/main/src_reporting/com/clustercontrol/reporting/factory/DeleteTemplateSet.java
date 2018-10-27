/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.reporting.fault.ReportingNotFound;
import com.clustercontrol.reporting.model.TemplateSetInfoEntity;
import com.clustercontrol.reporting.util.QueryUtil;

/**
 * テンプレートセット情報を削除するためのクラスです。
 * 
 * @version 5.0.a
 *
 */
public class DeleteTemplateSet {

	private static Log m_log = LogFactory.getLog(DeleteTemplateSet.class);

	/**
	 * テンプレートセット情報を削除します。
	 * 
	 * @param templateSetId
	 * @return
	 * @throws ReportNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean deleteTemplateSet(String templateSetId)
			throws ReportingNotFound, InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//削除対象のテンプレートセット情報取得
			TemplateSetInfoEntity entity = 
					QueryUtil.getTemplateSetInfoPK(templateSetId, ObjectPrivilegeMode.MODIFY);
			
			//テンプレートセット情報を削除
			em.remove(entity);
		} catch (ReportingNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteTemplateSet() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		
		return true;
	}

}
