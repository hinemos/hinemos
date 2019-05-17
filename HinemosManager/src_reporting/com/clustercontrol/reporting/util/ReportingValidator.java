/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.reporting.bean.ReportingInfo;
import com.clustercontrol.reporting.bean.TemplateSetDetailInfo;
import com.clustercontrol.reporting.bean.TemplateSetInfo;
import com.clustercontrol.reporting.factory.SelectReportingInfo;
import com.clustercontrol.reporting.session.ReportingControllerBean;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;

/**
 * レポーティングの入力チェッククラス
 * 
 * @since 4.1.2
 */
public class ReportingValidator {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ReportingValidator.class );

	/**
	 * レポーティング情報の妥当性チェック
	 * 
	 * @param reportingInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateReportingInfo(ReportingInfo reportingInfo) throws InvalidSetting, InvalidRole {

		// reportId
		if (reportingInfo.getReportScheduleId() == null ||
				"".equals(reportingInfo.getReportScheduleId())) {
			InvalidSetting e = new InvalidSetting(Messages.getString("MESSAGE_REPORTING_9"));
			m_log.info("validateReportingInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateId(Messages.getString("SCHEDULE_ID"), reportingInfo.getReportScheduleId(), 64);

		// ownerRoleId
		CommonValidator.validateString(Messages.getString("OWNER_ROLE_ID"), reportingInfo.getOwnerRoleId(), true, 1, 64);

		// templateSetId
		CommonValidator.validateString(Messages.getString("TEMPLATE_SET_ID"), reportingInfo.getTemplateSetId(), true, 1, 64);
		
		// schedule
		CommonValidator.validateScheduleHour(reportingInfo.getSchedule());

		// calendarId
		CommonValidator.validateCalenderId(reportingInfo.getCalendarId(), false, reportingInfo.getOwnerRoleId());
		
		// notifyId
		if(reportingInfo.getNotifyId() != null){
			for(NotifyRelationInfo notifyRelationInfo : reportingInfo.getNotifyId()){
				CommonValidator.validateNotifyId(notifyRelationInfo.getNotifyId(), true, reportingInfo.getOwnerRoleId());
			}
		}
		
		// outputPeriodBefore
		if(reportingInfo.getOutputPeriodBefore() == null
				|| reportingInfo.getOutputPeriodBefore().intValue() < 0){
			InvalidSetting e = new InvalidSetting(Messages.getString("MESSAGE_REPORTING_17"));
			m_log.info("validateReportingInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		else {
			m_log.info("validateReportingInfo() : getOutputPeriodBefore = " + reportingInfo.getOutputPeriodBefore());
		}

		// outputPeriodFor
		if(reportingInfo.getOutputPeriodFor() == null
				|| reportingInfo.getOutputPeriodFor().intValue() < 0){
			InvalidSetting e = new InvalidSetting(Messages.getString("MESSAGE_REPORTING_17"));
			m_log.info("validateReportingInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		else {
			m_log.info("validateReportingInfo() : getOutputPeriodFor = " + reportingInfo.getOutputPeriodFor());
		}
		
		// outputType
		if(reportingInfo.getOutputType() == null
				|| reportingInfo.getOutputType().intValue() < 0){
			InvalidSetting e = new InvalidSetting(Messages.getString("MESSAGE_REPORTING_44"));
			m_log.info("validateReportingInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// description
		CommonValidator.validateString(Messages.getString("DESCRIPTION"),
				reportingInfo.getDescription(), false, 0, 256);

		// reportTitle
		CommonValidator.validateString(Messages.getString("REPORT_TITLE"),
				reportingInfo.getReportTitle(), false, 1, 256);
	}
	
	
	public static void validateTemplateSetInfo(TemplateSetInfo templateSetInfo) throws InvalidSetting, InvalidRole {

		// templateSetId
		if (templateSetInfo.getTemplateSetId() == null ||
				"".equals(templateSetInfo.getTemplateSetId())) {
			InvalidSetting e = new InvalidSetting(Messages.getString("MESSAGE_REPORTING_31"));
			m_log.info("validateTemplateSetInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateId(Messages.getString("TEMPLATE_SET_ID"), templateSetInfo.getTemplateSetId(), 64);
		
		// templateSetName
		CommonValidator.validateString(Messages.getString("TEMPLATE_SET_NAME"), templateSetInfo.getTemplateSetName(), true, 1, 256);
		
		// description
		CommonValidator.validateString(Messages.getString("DESCRIPTION"), templateSetInfo.getDescription(), false, 0, 256);
		
		// ownerRoleId
		CommonValidator.validateString(Messages.getString("OWNER_ROLE_ID"), templateSetInfo.getOwnerRoleId(), true, 1, 64);
		
		// templateSetDetail
		// NULL・空チェック
		if (templateSetInfo.getTemplateSetDetailInfoList() ==null ||
				templateSetInfo.getTemplateSetDetailInfoList().size() == 0) {
			InvalidSetting e = new InvalidSetting(Messages.getString("MESSAGE_REPORTING_32"));
			m_log.info("validateTemplateSetInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		//テンプレートIDの一覧を取得
		Set<String> templateIdSet = new HashSet<String>(new ReportingControllerBean().getTemplateIdList(templateSetInfo.getOwnerRoleId()));
		// テンプレートID、NULL・空チェック
		for (TemplateSetDetailInfo detailInfo : templateSetInfo.getTemplateSetDetailInfoList()) {

			if(detailInfo == null) {
				InvalidSetting e = new InvalidSetting(Messages.getString("MESSAGE_REPORTING_45"));
				m_log.info("validateTemplateSetInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			if(detailInfo.getTemplateId() == null || detailInfo.getTemplateId().isEmpty()) {
				InvalidSetting e = new InvalidSetting(
						Messages.getString("MESSAGE_REPORTING_33", String.valueOf(detailInfo.getOrderNo())));
				m_log.info("validateTemplateSetInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			// description
			CommonValidator.validateString(
					Messages.getString("TEMPLATE_ID") + " : " + detailInfo.getTemplateId() + " - " + Messages.getString("DESCRIPTION"), 
					detailInfo.getDescription(), 
					false, 0, 256);
			
			// title
			CommonValidator.validateString(
					Messages.getString("TEMPLATE_ID") + " : " + detailInfo.getTemplateId() + " - " + Messages.getString("TITLE_NAME"), 
					detailInfo.getTitleName(), 
					false, 0, 256);
			
			// template id
			if( templateIdSet.contains(detailInfo.getTemplateId()) == false ){
				String[] argStrings ={ detailInfo.getTemplateId() };
				InvalidSetting e = new InvalidSetting(
						Messages.getString("MESSAGE_REPORTING_46", argStrings ));
				throw e;
			}
		}
	}
	
	public static void validateDeleteTemplateSetInfo(String templateSetId) throws InvalidSetting, InvalidRole, HinemosUnknown {

		// レポーティングスケジュールのチェック
		m_log.debug("validateDeleteTemplateSetInfo() schedule check start");

		ArrayList<ReportingInfo> list;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			list = new SelectReportingInfo().getReportingList();
			
			for(ReportingInfo info : list){
				if(templateSetId.equals(info.getTemplateSetId())){
					// 削除対象のテンプレートセットにスケジュールからの参照がある
					String[] args = {info.getReportScheduleId(), templateSetId};
					m_log.info("validateDeleteTemplateSetInfo() : Could not delete. It is being used. "
							+ "ScheduleID=" + info.getReportScheduleId() 
							+ ",TemplateSetID=" + templateSetId);
					throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_REPORTING_TEMPLATESET_REFERENCE.getMessage(args));
				}
			}
		} catch (InvalidRole | ObjectPrivilege_InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("validateDeleteTemplateSetInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
}
