/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.FunctionPrefixEnum;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ReportingNotFound;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.reporting.bean.ReportingInfo;
import com.clustercontrol.reporting.bean.ReportingTypeConstant;
import com.clustercontrol.reporting.model.ReportingInfoEntity;

import jakarta.persistence.EntityExistsException;

/**
 * メンテナンス情報を登録するためのクラスです。
 * 
 * @version 4.1.2
 *
 */
public class AddReporting {

	private static Log m_log = LogFactory.getLog( AddReporting.class );

	/**
	 * @param data
	 * @param name
	 * @return
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	public ReportingInfo addReporting(ReportingInfo data, String name)
			throws EntityExistsException, InvalidRole, HinemosUnknown {
		ReportingInfo ret = null;
		data.setNotifyGroupId(NotifyGroupIdGenerator.generate(data));

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// インスタンス生成
			ReportingInfoEntity entity = new ReportingInfoEntity(data.getReportScheduleId());
			// 重複チェック
			jtm.checkEntityExists(ReportingInfoEntity.class, entity.getReportScheduleId());
			entity.setDescription(data.getDescription());
			entity.setFacilityId(data.getFacilityId());
			entity.setTemplateSetId(data.getTemplateSetId());
			entity.setOutputPeriodType(data.getOutputPeriodType());
			entity.setOutputPeriodBefore(data.getOutputPeriodBefore());
			entity.setOutputPeriodFor(data.getOutputPeriodFor());
			entity.setCalendarId(data.getCalendarId());
			if (data.getSchedule().getType() == ScheduleConstant.TYPE_WEEK) {
				entity.setScheduleType(ReportingTypeConstant.SCHEDULE_TYPE_WEEK);
				entity.setWeek(data.getSchedule().getWeek());
			} else {
				if (data.getSchedule().getDay() == null) {
					entity.setScheduleType(ReportingTypeConstant.SCHEDULE_TYPE_DAY);
				} else {
					entity.setScheduleType(ReportingTypeConstant.SCHEDULE_TYPE_MONTH);
				}
				entity.setDay(data.getSchedule().getDay());
			}
			entity.setHour(data.getSchedule().getHour());
			entity.setMinute(data.getSchedule().getMinute());
			entity.setNotifyGroupId(data.getNotifyGroupId());
			entity.setLogoValidFlg(data.getLogoValidFlg());
			entity.setLogoFilename(data.getLogoFilename());
			entity.setPageValidFlg(data.getPageValidFlg());
			entity.setOutputType(data.getOutputType());
			entity.setReportTitle(data.getReportTitle());
			entity.setValidFlg(data.getValidFlg());
			entity.setRegDate(new Date().getTime());
			entity.setUpdateDate(new Date().getTime());
			entity.setOwnerRoleId(data.getOwnerRoleId());
			entity.setRegUser(name);
			entity.setUpdateUser(name);
			em.persist(entity);
			
			ret = new SelectReportingInfo().getReportingInfo(entity.getReportScheduleId());
		} catch (EntityExistsException e){
			m_log.info("addReporting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;
		} catch (ReportingNotFound e) {
			throw new HinemosUnknown(e.getMessage(), e);
		}

		if (data.getNotifyRelationList() != null && data.getNotifyRelationList().size() > 0) {
			for (NotifyRelationInfo notifyRelationInfo : data.getNotifyRelationList()) {
				notifyRelationInfo.setNotifyGroupId(data.getNotifyGroupId());
				notifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.REPORTING.name());
			}
			new NotifyControllerBean().addNotifyRelation(data.getNotifyRelationList(), data.getOwnerRoleId());
			ret.setNotifyRelationList(data.getNotifyRelationList());
		}

		return ret;

	}

}
