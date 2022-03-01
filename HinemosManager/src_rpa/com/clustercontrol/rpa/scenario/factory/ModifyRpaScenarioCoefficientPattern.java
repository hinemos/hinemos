/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RpaScenarioCoefficientPatternDuplicate;
import com.clustercontrol.fault.RpaToolMasterNotFound;
import com.clustercontrol.rpa.model.RpaScenarioCoefficientPattern;
import com.clustercontrol.rpa.model.RpaToolEnvMst;
import com.clustercontrol.rpa.util.QueryUtil;

import jakarta.persistence.EntityExistsException;

/**
 * 自動化効果計算マスタ情報を更新するクラス
 */
public class ModifyRpaScenarioCoefficientPattern {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ModifyRpaScenarioCoefficientPattern.class );

	/**
	 * 自動化効果計算マスタ情報を作成します。
	 * @throws RpaToolMasterNotFound 
	 */
	public void add(RpaScenarioCoefficientPattern data) throws RpaScenarioCoefficientPatternDuplicate, RpaToolMasterNotFound {

		
		//エンティティBeanを作る
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			
			// 重複チェック
			jtm.checkEntityExists(RpaScenarioCoefficientPattern.class, data.getId());
			
			RpaToolEnvMst mst = QueryUtil.getRpaToolEnvMstPK(data.getId().getRpaToolEnvId());
			mst.getScenarioCoefficientPattern().add(data);
			
			jtm.flush();
		} catch (EntityExistsException e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new RpaScenarioCoefficientPatternDuplicate(e.getMessage(),e);
		}
	}
	
	/**
	 * 自動化効果計算マスタ情報を削除します。
	 */
	public void delete(String rpaToolEnvId, int orderNo) throws InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 自動化効果計算マスタ情報を検索し取得
			RpaScenarioCoefficientPattern entity = null;
			try {
				entity = QueryUtil.getRpaScenarioCoefficientPattern(rpaToolEnvId, orderNo);
			} catch (Exception e) {
				throw new HinemosUnknown(e.getMessage(), e);
			}

			//自動化効果計算マスタ情報を削除
			m_log.debug("delete() delete RPA Scenario Coefficient Pattern. rpaToolEnvId=" + entity.getId().getRpaToolEnvId()); 
			em.remove(entity);
		}
	}
}