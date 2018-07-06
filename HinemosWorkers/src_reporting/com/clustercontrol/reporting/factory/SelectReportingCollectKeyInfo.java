/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.fault.CollectKeyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.reporting.util.ReportingQueryUtil;

public class SelectReportingCollectKeyInfo {
	
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(SelectReportingCollectKeyInfo.class);
	
	/**
	 * 収集項目IDを取得します。
	 */
	public Integer getCollectId(String itemname, String displayname, String monitorid, String facilityid)throws ObjectPrivilege_InvalidRole{
		CollectKeyInfoPK pk = new CollectKeyInfoPK(itemname, displayname, monitorid, facilityid);
		try {
			CollectKeyInfo key = ReportingQueryUtil.getCollectKeyPK(pk, ObjectPrivilegeMode.NONE);
			if(key == null){
				throw new CollectKeyNotFound(pk.toString());
			}
			return key.getCollectorid();
		} catch (CollectKeyNotFound e) {
			m_log.warn("getCollectId() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * 収集項目を取得します。
	 */
	public CollectKeyInfo getReportingCollectKeyInfo(String itemname, String displayname, String monitorid, String facilityid)throws ObjectPrivilege_InvalidRole{
		try {
			CollectKeyInfoPK pk = new CollectKeyInfoPK(itemname, displayname, monitorid, facilityid);
			CollectKeyInfo key = ReportingQueryUtil.getCollectKeyPK(pk, ObjectPrivilegeMode.NONE);
			if(key == null){
				throw new CollectKeyNotFound(pk.toString());
			}
			return key;
		} catch (CollectKeyNotFound e) {
			m_log.warn("getReportingCollectKeyInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * 収集項目のリストを取得します。
	 * @param itemname
	 * @param displayName
	 * @param monitorid
	 * @param facilityid
	 */
	public List<CollectKeyInfo> getReportingCollectKeyInfoList(String itemname, String displayName, String monitorid, String facilityid) {
		List<CollectKeyInfo> keyList = ReportingQueryUtil.getReportingCollectKeyInfoList(itemname, displayName, monitorid, facilityid);
		return keyList;
	}
	
	/**
	 * CollectKeyInfoの一覧をmonitorIdとfacilityIdを条件に取得します。<BR>
	 * 
	 * @param monitorIdList
	 * @param facilityId
	 * @return
	 */
	public List<CollectKeyInfo> getReportCollectKeyList(String monitorId, String facilityId) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getReportCollectKeyList(monitorId, facilityId);
	}
}
