/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.Date;

import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.reporting.bean.ReportingTypeConstant;
import com.clustercontrol.reporting.bean.ReportingInfo;
import com.clustercontrol.reporting.fault.ReportingNotFound;
import com.clustercontrol.reporting.model.ReportingInfoEntity;
import com.clustercontrol.reporting.util.QueryUtil;

/**
 * レポーティング情報を変更するためのクラスです。
 * 
 * @version 5.0.a
 *
 */
public class ModifyReporting {

	/**
	 * @param info
	 * @param name
	 * @return
	 * @throws ReportingNotFound
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean modifyReporting(ReportingInfo info, String name)
			throws ReportingNotFound, NotifyNotFound, InvalidRole, HinemosUnknown {

		//レポーティング情報を取得
		ReportingInfoEntity entity = QueryUtil.getReportingInfoPK(info.getReportScheduleId());

		//レポーティング情報を更新
		entity.setDescription(info.getDescription());
		entity.setFacilityId(info.getFacilityId());
		entity.setTemplateSetId(info.getTemplateSetId());
		entity.setOutputPeriodType(info.getOutputPeriodType());
		entity.setOutputPeriodBefore(info.getOutputPeriodBefore());
		entity.setOutputPeriodFor(info.getOutputPeriodFor());
		entity.setCalendarId(info.getCalendarId());
		if (info.getSchedule().getType() == ScheduleConstant.TYPE_WEEK) {
			entity.setScheduleType(ReportingTypeConstant.SCHEDULE_TYPE_WEEK);
			entity.setWeek(info.getSchedule().getWeek());
		} else {
			if (info.getSchedule().getDay() == null) {
				entity.setScheduleType(ReportingTypeConstant.SCHEDULE_TYPE_DAY);
			} else {
				entity.setScheduleType(ReportingTypeConstant.SCHEDULE_TYPE_MONTH);
			}
			entity.setDay(info.getSchedule().getDay());
		}
		entity.setHour(info.getSchedule().getHour());
		entity.setMinute(info.getSchedule().getMinute());
		entity.setNotifyGroupId(info.getNotifyGroupId());
		entity.setLogoValidFlg(info.getLogoValidFlg());
		entity.setLogoFilename(info.getLogoFilename());
		entity.setPageValidFlg(info.getPageValidFlg());
		entity.setReportTitle(info.getReportTitle());
		entity.setValidFlg(info.getValidFlg());
		entity.setOutputType(info.getOutputType());
		entity.setOwnerRoleId(info.getOwnerRoleId());
		entity.setRegUser(info.getRegUser());
		entity.setRegDate(info.getRegDate());
		entity.setUpdateUser(name);
		entity.setUpdateDate(new Date().getTime());

		new NotifyControllerBean().modifyNotifyRelation(info.getNotifyId(), info.getNotifyGroupId());

		return true;
	}

}
