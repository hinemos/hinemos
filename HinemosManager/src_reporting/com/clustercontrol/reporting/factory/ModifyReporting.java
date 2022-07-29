/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.Date;

import com.clustercontrol.bean.FunctionPrefixEnum;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ReportingNotFound;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.reporting.bean.ReportingTypeConstant;
import com.clustercontrol.reporting.bean.ReportingInfo;
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
	public ReportingInfo modifyReporting(ReportingInfo info, String name)
			throws ReportingNotFound, NotifyNotFound, InvalidRole, HinemosUnknown {
		ReportingInfo ret = null;
		info.setNotifyGroupId(NotifyGroupIdGenerator.generate(info));

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
		entity.setUpdateUser(name);
		entity.setUpdateDate(new Date().getTime());
		
		if (info.getNotifyRelationList() != null && info.getNotifyRelationList().size() > 0) {
			for (NotifyRelationInfo notifyRelationInfo : info.getNotifyRelationList()) {
				notifyRelationInfo.setNotifyGroupId(info.getNotifyGroupId());
				notifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.REPORTING.name());
			}
		}

		new NotifyControllerBean().modifyNotifyRelation(info.getNotifyRelationList(), info.getNotifyGroupId(), info.getOwnerRoleId());
		ret = new SelectReportingInfo().getReportingInfo(entity.getReportScheduleId());

		return ret;
	}

}
