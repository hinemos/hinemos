/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ReportingNotFound;
import com.clustercontrol.reporting.bean.TemplateSetDetailInfo;
import com.clustercontrol.reporting.bean.TemplateSetInfo;
import com.clustercontrol.reporting.model.TemplateSetDetailInfoEntity;
import com.clustercontrol.reporting.model.TemplateSetInfoEntity;
import com.clustercontrol.reporting.util.QueryUtil;

/**
 * 
 * テンプレートセット情報の検索クラスです。
 * 
 * @version	5.0.a
 *
 */
public class SelectTemplateSetInfo {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectTemplateSetInfo.class );

	/**
	 * テンプレートセット情報を取得します
	 * 
	 * @param templateSetId
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws ReportingNotFound
	 */
	public TemplateSetInfo getTemplateSetInfo(String templateSetId)
			throws HinemosUnknown, InvalidRole, ReportingNotFound {
		
		m_log.debug("getTemplateSetInfo() : start, templateSetId = " + templateSetId);
		
		TemplateSetInfo info = null;
		
		if(templateSetId != null && !templateSetId.isEmpty()) {
		
			m_log.debug("templateSetId : " + templateSetId);
			
			// テンプレートセット情報を取得
			TemplateSetInfoEntity entity = QueryUtil.getTemplateSetInfoPK(templateSetId);
			info = getTemplateSetInfoBean(entity);
			
			// テンプレートセット詳細情報を追加
			ArrayList<TemplateSetDetailInfo> detailList = getTemplateSetDetailList(templateSetId);
			info.getTemplateSetDetailInfoList().clear();
			info.getTemplateSetDetailInfoList().addAll(detailList);
		}
		
		return info;
	}
	
	/**
	 * テンプレートセットIDを基にテンプレートセット詳細情報を取得します。
	 * 
	 * @param templateSetId
	 * @return
	 */
	public ArrayList<TemplateSetDetailInfo> getTemplateSetDetailList(String templateSetId) {
		ArrayList<TemplateSetDetailInfo> list = new ArrayList<TemplateSetDetailInfo>();
		
		m_log.debug("getTemplateSetDetailList() : start");
		
		//テンプレートセットIDでテンプレートセット詳細を取得
		List<TemplateSetDetailInfoEntity> entityList = QueryUtil.getTemplateSetDetailByTemplateSetId(templateSetId);
		for(TemplateSetDetailInfoEntity entity : entityList) {
			
			m_log.debug("getTemplateSetDetailList() : orderNo = " + entity.getId().getOrderNo() + ", templateId = " + entity.getTemplateId());
			
			TemplateSetDetailInfo info = new TemplateSetDetailInfo();
			info.setTemplateSetId(entity.getId().getTemplateSetId());
			info.setOrderNo(entity.getId().getOrderNo());
			info.setTemplateId(entity.getTemplateId());
			
			if(entity.getDescription() != null) {
				info.setDescription(entity.getDescription());
			}
			if(entity.getTitleName() != null) {
				info.setTitleName(entity.getTitleName());
			}
			
			list.add(info);
		}
		return list;
	}
	
	/**
	 * テンプレート情報の一覧を取得します。
	 * 
	 * @param ownerRoleId
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws ReportingNotFound
	 */
	
	public ArrayList<TemplateSetInfo> getAllTemplateSetList(String ownerRoleId) {
		
		m_log.debug("getAllTemplateSetList() : start");
		
		List<TemplateSetInfoEntity> entityList = null;
		
		if(ownerRoleId == null || ownerRoleId.isEmpty()) {
			// ログインユーザが参照権限がある全テンプレートセットを取得
			entityList = QueryUtil.getAllTemplateSetInfo();
		} else {
			// オーナーロールIDを条件として全テンプレートセットを取得
			entityList = QueryUtil.getAllTemplateSetInfo_OR(ownerRoleId);
		}
		
		ArrayList<TemplateSetInfo> infoList = new ArrayList<>();
		for (TemplateSetInfoEntity entity: entityList) {
			infoList.add(getTemplateSetInfoBean(entity));
		}
		
		return infoList;
	}
	
	/**
	 * TemplateSetInfoEntityからTemplateSetInfoBeanへ変換
	 */
	private TemplateSetInfo getTemplateSetInfoBean(TemplateSetInfoEntity entity) {

		m_log.debug("getTemplateSetInfoBean() : start");
		
		TemplateSetInfo info = new TemplateSetInfo();
		info.setTemplateSetId(entity.getTemplateSetId());
		info.setTemplateSetName(entity.getTemplateSetName());
		info.setOwnerRoleId(entity.getOwnerRoleId());
		info.setDescription(entity.getDescription());
		info.setRegUser(entity.getRegUser());
		info.setRegDate(entity.getRegDate());
		info.setUpdateUser(entity.getUpdateUser());
		info.setUpdateDate(entity.getUpdateDate());
				
		return info;
	}
	
}
