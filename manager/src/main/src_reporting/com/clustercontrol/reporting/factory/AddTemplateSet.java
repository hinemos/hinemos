/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.Date;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.reporting.bean.TemplateSetDetailInfo;
import com.clustercontrol.reporting.bean.TemplateSetInfo;
import com.clustercontrol.reporting.fault.ReportingNotFound;
import com.clustercontrol.reporting.model.TemplateSetDetailInfoEntity;
import com.clustercontrol.reporting.model.TemplateSetDetailInfoEntityPK;
import com.clustercontrol.reporting.model.TemplateSetInfoEntity;
import com.clustercontrol.reporting.util.QueryUtil;

/**
 * テンプレートセット情報を登録するためのクラスです。
 * 
 * @version 5.0.a
 *
 */
public class AddTemplateSet {

	private static Log m_log = LogFactory.getLog( AddTemplateSet.class );

	/**
	 * @param data
	 * @param name
	 * @return
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	public boolean addTemplateSet(TemplateSetInfo data, String name)
			throws EntityExistsException, InvalidRole, HinemosUnknown {
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// インスタンス生成
			TemplateSetInfoEntity entity = new TemplateSetInfoEntity(data.getTemplateSetId());
			// 重複チェック
			jtm.checkEntityExists(TemplateSetInfoEntity.class, entity.getTemplateSetId());
			entity.setTemplateSetName(data.getTemplateSetName());
			entity.setDescription(data.getDescription());
			entity.setOwnerRoleId(data.getOwnerRoleId());
			entity.setRegDate(new Date().getTime());
			entity.setUpdateDate(new Date().getTime());
			entity.setRegUser(name);
			entity.setUpdateUser(name);
			em.persist(entity);
			
			// テンプレートセット詳細を追加
			TemplateSetDetailInfoEntity detailEntity = null;
			int num = 1;
			for (TemplateSetDetailInfo detailInfo: data.getTemplateSetDetailInfoList()) {
				try {
					detailEntity = QueryUtil.getTemplateSetDetailInfoPK(
							new TemplateSetDetailInfoEntityPK(detailInfo.getTemplateSetId(), num));
				} catch (ReportingNotFound e) {
					detailEntity = new TemplateSetDetailInfoEntity(entity.getTemplateSetId(), num);
					em.persist(detailEntity);
					detailEntity.relateToTemplateSetInfoEntity(entity);
				}
				copyProperties(detailEntity, detailInfo);
				num++;
			}
		} catch (EntityExistsException e){
			m_log.info("addTemplateSet() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;
		}

		return true;
	}
	
	/**
	 * テンプレートセット詳細情報を追加します。<BR>
	 * 
	 * @param calDetailInfoEntity
	 * @param CalendarDetailInfo
	 * @return
	 */
	public void copyProperties(TemplateSetDetailInfoEntity entity, TemplateSetDetailInfo info) {

		// 説明
		entity.setDescription(info.getDescription());
		// テンプレートID
		entity.setTemplateId(info.getTemplateId());
		// タイトル
		if(info.getTitleName() != null && !info.getTitleName().isEmpty()) {
			entity.setTitleName(info.getTitleName());
		}
	}
	
	
}
