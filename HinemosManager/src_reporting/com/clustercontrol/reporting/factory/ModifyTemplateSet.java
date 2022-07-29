/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ReportingNotFound;
import com.clustercontrol.reporting.bean.TemplateSetDetailInfo;
import com.clustercontrol.reporting.bean.TemplateSetInfo;
import com.clustercontrol.reporting.model.TemplateSetDetailInfoEntity;
import com.clustercontrol.reporting.model.TemplateSetDetailInfoEntityPK;
import com.clustercontrol.reporting.model.TemplateSetInfoEntity;
import com.clustercontrol.reporting.util.QueryUtil;

/**
 * テンプレートセット情報を変更するためのクラスです。
 * 
 * @version 5.0.a
 *
 */
public class ModifyTemplateSet {

	/**
	 * @param info
	 * @param name
	 * @return
	 * @throws ReportingNotFound
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public TemplateSetInfo modifyTemplateSet(TemplateSetInfo info, String name)
			throws ReportingNotFound, NotifyNotFound, InvalidRole, HinemosUnknown {
		TemplateSetInfo ret = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			String templateSetId = info.getTemplateSetId();
			
			//テンプレートセット情報を取得
			TemplateSetInfoEntity entity = 
					QueryUtil.getTemplateSetInfoPK(templateSetId, ObjectPrivilegeMode.MODIFY);

			//テンプレートセット情報を更新
			entity.setTemplateSetName(info.getTemplateSetName());
			entity.setDescription(info.getDescription());
			entity.setOwnerRoleId(info.getOwnerRoleId());
			entity.setUpdateDate(new Date().getTime());
			entity.setUpdateUser(name);
			
			// テンプレートセット詳細を登録
			TemplateSetDetailInfoEntity detailEntity = null;
			List<TemplateSetDetailInfoEntityPK> templateSetDetailInfoEntityPKList = 
					new ArrayList<TemplateSetDetailInfoEntityPK>();
			int num = 1;
			for (TemplateSetDetailInfo detailInfo: info.getTemplateSetDetailInfoList()) {
				
				try {
					detailEntity = QueryUtil.getTemplateSetDetailInfoPK(
							new TemplateSetDetailInfoEntityPK(templateSetId, num));
				} catch (ReportingNotFound e) {
					detailEntity = new TemplateSetDetailInfoEntity(entity.getTemplateSetId(), num);
					em.persist(detailEntity);
					detailEntity.relateToTemplateSetInfoEntity(entity);
				}
				templateSetDetailInfoEntityPKList.add(
						new TemplateSetDetailInfoEntityPK(templateSetId, num));
				copyProperties(detailEntity, detailInfo);
				num++;
			}
			//不要なTemplateSetDetailInfoEntityを削除
			entity.deleteTemplateSetDetailInfoEntities(templateSetDetailInfoEntityPKList);
			
			ret = new SelectTemplateSetInfo().getTemplateSetInfo(entity.getTemplateSetId());
			
		}
		return ret;
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
		entity.setTitleName(info.getTitleName());
		
	}
}
