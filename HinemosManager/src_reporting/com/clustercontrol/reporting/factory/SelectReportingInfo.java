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
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.commons.bean.Schedule;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.reporting.bean.ReportingInfo;
import com.clustercontrol.reporting.bean.ReportingTypeConstant;
import com.clustercontrol.reporting.model.ReportingInfoEntity;
import com.clustercontrol.reporting.util.QueryUtil;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * 
 * レポーティング情報検索クラスです。
 * 
 * @version	5.0.a
 *
 */
public class SelectReportingInfo {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectReportingInfo.class );

	/**
	 * @param reportId
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws ReportingNotFound
	 */
	public ReportingInfo getReportingInfo(String reportScheduleId)
			throws HinemosUnknown, InvalidRole, ReportingNotFound {

		ReportingInfo info = null;
		// レポーティング情報を取得
		ReportingInfoEntity entity = QueryUtil.getReportingInfoPK(reportScheduleId);
		info = getReportingInfoBean(entity);
		return info;
	}


	/**
	 * 
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<ReportingInfo> getReportingList() throws InvalidRole, HinemosUnknown {
		m_log.debug("getReportingList() : start");

		// レポーティング情報一覧を取得
		ArrayList<ReportingInfo> list = new ArrayList<ReportingInfo>();

		List<ReportingInfoEntity> ct = QueryUtil.getAllReportingInfoOrderByReportId();
		for(ReportingInfoEntity entity : ct){
			list.add(getReportingInfoBean(entity));
			// for debug
			if(m_log.isDebugEnabled()){
				m_log.debug("getReportingList() : " +
						"reportScheduleId = " + entity.getReportScheduleId() +
							", description = " + entity.getDescription() +
							", facility_id = " + entity.getFacilityId() +
							", calendar_id = " + entity.getCalendarId() +
							", output_period_type = " + entity.getOutputPeriodType() +
							", output_period_before = " + entity.getOutputPeriodBefore() +
							", output_period_for = " + entity.getOutputPeriodFor() +
							", templateSetId = " + entity.getTemplateSetId() +
							", report_title = " + entity.getReportTitle() +
							", logo_valid_flg = " + entity.getLogoValidFlg() +
							", logo_filename = " + entity.getLogoFilename() +
							", page_valid_flg = " + entity.getPageValidFlg() +
							", output_type = " + entity.getOutputType() +
							", schedule_type = " + entity.getScheduleType() +
							", day = " + entity.getDay() +
							", week = " + entity.getWeek() +
							", hour = " + entity.getHour() +
							", minute = " + entity.getMinute() +
							", notifyGroupId = " + entity.getNotifyGroupId() +
							", valid_flg = " + entity.getValidFlg() +
							", regDate = " + entity.getRegDate() +
							", updateDate = " + entity.getUpdateDate() +
							", regUser = " + entity.getRegUser() +
							", updateUser = " + entity.getUpdateUser());
			}
		}

		return list;
	}

	/**
	 * ReportingInfoEntityからReportingInfoBeanへ変換
	 */
	private ReportingInfo getReportingInfoBean(ReportingInfoEntity entity)
			throws InvalidRole, HinemosUnknown {

		ReportingInfo info = new ReportingInfo();
		info.setReportScheduleId(entity.getReportScheduleId());
		info.setDescription(entity.getDescription());
		info.setFacilityId(entity.getFacilityId());

		//表示用スコープテキストの取得
		String facilityPath = new RepositoryControllerBean().getFacilityPath(entity.getFacilityId(), null);
		info.setScope(facilityPath);

		info.setCalendarId(entity.getCalendarId());
		info.setOutputPeriodType(entity.getOutputPeriodType());
		info.setOutputPeriodBefore(entity.getOutputPeriodBefore());
		info.setOutputPeriodFor(entity.getOutputPeriodFor());
		info.setTemplateSetId(entity.getTemplateSetId());
		info.setReportTitle(entity.getReportTitle());
		info.setLogoValidFlg(entity.getLogoValidFlg());
		info.setLogoFilename(entity.getLogoFilename());
		info.setPageValidFlg(entity.getPageValidFlg());
		info.setOutputType(entity.getOutputType());

		int hinemosScheduleType = ScheduleConstant.TYPE_DAY;
		Integer day = null;
		Integer week = null;
		switch (entity.getScheduleType()) {
		case ReportingTypeConstant.SCHEDULE_TYPE_DAY:
			hinemosScheduleType = ScheduleConstant.TYPE_DAY;
			break;
		case ReportingTypeConstant.SCHEDULE_TYPE_WEEK:
			hinemosScheduleType = ScheduleConstant.TYPE_WEEK;
			week = entity.getWeek();
			break;
		case ReportingTypeConstant.SCHEDULE_TYPE_MONTH:
			hinemosScheduleType = ScheduleConstant.TYPE_DAY;
			day = entity.getDay();
			break;
		default:
			break;
		}
		info.setSchedule(new Schedule(hinemosScheduleType,
				null, day, week, entity.getHour(), entity.getMinute()));
		info.setNotifyGroupId(entity.getNotifyGroupId());
		info.setValidFlg(entity.getValidFlg());
		info.setOwnerRoleId(entity.getOwnerRoleId());
		if(entity.getRegDate() == null){
			info.setRegDate(null);
		}
		else{
			info.setRegDate(entity.getRegDate());
		}
		if(entity.getUpdateDate() == null){
			info.setUpdateDate(null);
		}
		else{
			info.setUpdateDate(entity.getUpdateDate());
		}
		info.setRegUser(entity.getRegUser());
		info.setUpdateUser(entity.getUpdateUser());

		//通知情報の取得
		info.setNotifyRelationList(NotifyRelationCache.getNotifyList(info.getNotifyGroupId()));

		return info;
	}
}
