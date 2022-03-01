/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AddJobLinkSendSettingRequest;
import org.openapitools.client.model.AddNodeConfigSettingInfoRequest;
import org.openapitools.client.model.AddTransferInfoRequest;
import org.openapitools.client.model.BinaryCheckInfoResponse;
import org.openapitools.client.model.BinaryPatternInfoResponse;
import org.openapitools.client.model.CalendarDetailInfoResponse;
import org.openapitools.client.model.CloudNotifyDetailInfoResponse;
import org.openapitools.client.model.CommandModuleInfoResponse;
import org.openapitools.client.model.CommandNotifyDetailInfoResponse;
import org.openapitools.client.model.CustomCheckInfoResponse;
import org.openapitools.client.model.CustomTrapCheckInfoResponse;
import org.openapitools.client.model.EventFilterSettingResponse;
import org.openapitools.client.model.EventNotifyDetailInfoResponse.CriticalEventNormalStateEnum;
import org.openapitools.client.model.EventNotifyDetailInfoResponse.InfoEventNormalStateEnum;
import org.openapitools.client.model.EventNotifyDetailInfoResponse.UnknownEventNormalStateEnum;
import org.openapitools.client.model.EventNotifyDetailInfoResponse.WarnEventNormalStateEnum;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FileTransferModuleInfoResponse;
import org.openapitools.client.model.InfraManagementInfoResponse;
import org.openapitools.client.model.InfraNotifyDetailInfoResponse.CriticalInfraFailurePriorityEnum;
import org.openapitools.client.model.InfraNotifyDetailInfoResponse.InfoInfraFailurePriorityEnum;
import org.openapitools.client.model.InfraNotifyDetailInfoResponse.InfraExecFacilityFlgEnum;
import org.openapitools.client.model.InfraNotifyDetailInfoResponse.UnknownInfraFailurePriorityEnum;
import org.openapitools.client.model.InfraNotifyDetailInfoResponse.WarnInfraFailurePriorityEnum;
import org.openapitools.client.model.IntegrationConditionInfoResponse;
import org.openapitools.client.model.JmxCheckInfoResponse;
import org.openapitools.client.model.JobCommandInfoResponse;
import org.openapitools.client.model.JobFileCheckInfoResponse;
import org.openapitools.client.model.JobFileCheckResponse;
import org.openapitools.client.model.JobFileInfoResponse;
import org.openapitools.client.model.JobHistoryFilterConditionResponse;
import org.openapitools.client.model.JobHistoryFilterSettingResponse;
import org.openapitools.client.model.JobInfoResponse;
import org.openapitools.client.model.JobLinkInheritInfoResponse;
import org.openapitools.client.model.JobLinkSendInfoResponse;
import org.openapitools.client.model.JobLinkSendSettingResponse;
import org.openapitools.client.model.JobMonitorInfoResponse;
import org.openapitools.client.model.JobNotifyDetailInfoResponse;
import org.openapitools.client.model.JobObjectGroupInfoResponse;
import org.openapitools.client.model.JobObjectInfoResponse;
import org.openapitools.client.model.JobOutputInfoResponse;
import org.openapitools.client.model.JobParameterInfoResponse;
import org.openapitools.client.model.JobResourceInfoResponse;
import org.openapitools.client.model.JobRpaEndValueConditionInfoResponse;
import org.openapitools.client.model.JobRpaInfoResponse;
import org.openapitools.client.model.JobRuntimeParamResponse;
import org.openapitools.client.model.JobScheduleResponse;
import org.openapitools.client.model.JobWaitRuleInfoResponse;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.EscalateFacilityFlgEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.InfoSyslogFacilityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.InfoSyslogPriorityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.CriticalSyslogFacilityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.CriticalSyslogPriorityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.UnknownSyslogFacilityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.UnknownSyslogPriorityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.WarnSyslogFacilityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.WarnSyslogPriorityEnum;
import org.openapitools.client.model.MaintenanceScheduleRequest;
import org.openapitools.client.model.MaintenanceScheduleResponse;
import org.openapitools.client.model.MapAssociationInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse;
import org.openapitools.client.model.MonitorStringValueInfoResponse;
import org.openapitools.client.model.MonitorTruthValueInfoResponse;
import org.openapitools.client.model.NodeConfigSettingInfoResponse;
import org.openapitools.client.model.NodeConfigSettingItemInfoResponse;
import org.openapitools.client.model.NodeInfoResponse;
import org.openapitools.client.model.NotifyInfoResponse;
import org.openapitools.client.model.NotifyRelationInfoResponse;
import org.openapitools.client.model.NotifyRequestForUtility;
import org.openapitools.client.model.PageResponse;
import org.openapitools.client.model.PortCheckInfoResponse;
import org.openapitools.client.model.ReportingScheduleInfoResponse;
import org.openapitools.client.model.ReportingScheduleResponse;
import org.openapitools.client.model.RpaScenarioOperationResultCreateSettingResponse;
import org.openapitools.client.model.SnmpCheckInfoResponse;
import org.openapitools.client.model.StatusNotifyDetailInfoResponse;
import org.openapitools.client.model.SystemPrivilegeInfoResponse;
import org.openapitools.client.model.TransferInfoResponse;
import org.openapitools.client.model.StatusFilterSettingResponse;
import org.openapitools.client.model.TrapCheckInfoResponse;
import org.openapitools.client.model.TrapValueInfoRequest;
import org.openapitools.client.model.TrapValueInfoResponse;
import org.openapitools.client.model.VarBindPatternResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.JobLinkInheritKeyInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkSendProtocol;
import com.clustercontrol.rest.endpoint.infra.dto.enumtype.AccessMethodTypeEnum;
import com.clustercontrol.rest.endpoint.infra.dto.enumtype.PrioritySelectEnum;
import com.clustercontrol.rest.endpoint.infra.dto.enumtype.SendMethodTypeEnum;
import com.clustercontrol.rest.endpoint.access.dto.enumtype.SystemFunctionEnum;
import com.clustercontrol.rest.endpoint.access.dto.enumtype.SystemPrivilegeEditTypeEnum;
import com.clustercontrol.rest.endpoint.access.dto.enumtype.SystemPrivilegeModeEnum;
import com.clustercontrol.rest.endpoint.calendar.dto.enumtype.DayTypeEnum;
import com.clustercontrol.rest.endpoint.calendar.dto.enumtype.WeekNoEnum;
import com.clustercontrol.rest.endpoint.calendar.dto.enumtype.WeekXthEnum;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.rest.dto.EnumDto;
import com.clustercontrol.rest.endpoint.hub.dto.enumtype.TransferIntervalEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.CommandRetryEndStatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ConditionTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.DecisionObjectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusSelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.FileCheckEventTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.FileCheckModifyTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobParamTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobRuntimeParamTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTriggerTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JudgmentObjectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationEndDelayEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationFailureEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationJobOutputEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationMultipleEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationStartDelayEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PriorityRequiredEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ProcessingMethodEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ReferJobSelectTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ResourceJobActionEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ResourceJobTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobEndValueConditionTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobReturnCodeConditionEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaStopTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ScheduleTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ScopeJudgmentTargetEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.SessionPremakeEveryXHourEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.SessionPremakeScheduleTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.StatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.StopTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.WaitStatusEnum;
import com.clustercontrol.rest.endpoint.maintenance.dto.enumtype.MaintenanceScheduleEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.BinaryCollectTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.BinaryCutTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.BinaryLengthTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.BinaryTimeStampTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.ConvertFlagEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.MonitorNumericTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.MonitorTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PortServiceProtocolEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PredictionMethodEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.RunIntervalEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.SnmptrapVersionEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.TruthValueEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.CommandSettingTypeEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.EventNormalStateEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.ExecFacilityFlgEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyCloudPlatformTypeEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyJobTypeEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyPriorityEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyTypeEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.RenotifyTypeEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.StatusInvalidFlgEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.SyslogFacilityEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.SyslogSeverityEnum;
import com.clustercontrol.rest.endpoint.reporting.dto.enumtype.ReportOutputPeriodTypeEnum;
import com.clustercontrol.rest.endpoint.reporting.dto.enumtype.ReportOutputTypeEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.AssociationEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.FacilityTypeEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.IpaddressVersionEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.NodeConfigRunIntervalEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.NodeConfigSettingItemEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.SnmpAuthProtocolEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.SnmpPrivProtocolEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.SnmpSecurityLevelEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.SnmpVersionEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.WbemProtocolEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.WinrmProtocolEnum;
import com.clustercontrol.rest.endpoint.rpa.dto.enumtype.ScenarioCreateIntervalEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeFailureTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeJudgmentTypeEnum;

/**
 *  openapi提供Enum に対して、XML向け固定値(Integer String )との互換を提供するクラス
 *  
 *  固定値は openapi提供Enumの元になったEnum定義から取得する。
 *  
 * @author yasoharah
 *
 */
public class OpenApiEnumConverter {
	private  static Log log = LogFactory.getLog(OpenApiEnumConverter.class);

	// openapi提供のEnumに対する、元になったEnumの変換MAP()
	private static Map< Class<?>, Class<?>> convertMap = new HashMap< Class<?>, Class<?> >();
	
	static {
		//repository
		convertMap.put(MapAssociationInfoResponse.TypeEnum.class, AssociationEnum.class);
		convertMap.put(FacilityInfoResponse.FacilityTypeEnum.class, FacilityTypeEnum.class);
		convertMap.put(NodeInfoResponse.IpAddressVersionEnum.class, IpaddressVersionEnum.class);
		convertMap.put(NodeConfigSettingInfoResponse.RunIntervalEnum.class, NodeConfigRunIntervalEnum.class);
		convertMap.put(NodeConfigSettingItemInfoResponse.SettingItemIdEnum.class, NodeConfigSettingItemEnum.class);
		convertMap.put(NodeInfoResponse.SnmpAuthProtocolEnum.class, SnmpAuthProtocolEnum.class);
		convertMap.put(NodeInfoResponse.SnmpPrivProtocolEnum.class, SnmpPrivProtocolEnum.class);
		convertMap.put(NodeInfoResponse.SnmpSecurityLevelEnum.class, SnmpSecurityLevelEnum.class);
		convertMap.put(NodeInfoResponse.SnmpVersionEnum.class, SnmpVersionEnum.class);
		convertMap.put(NodeInfoResponse.WbemProtocolEnum.class, WbemProtocolEnum.class);
		convertMap.put(NodeInfoResponse.WinrmProtocolEnum.class, WinrmProtocolEnum.class);
		convertMap.put(AddNodeConfigSettingInfoRequest.RunIntervalEnum.class, NodeConfigRunIntervalEnum.class);

		//Notify
		convertMap.put(NotifyInfoResponse.NotifyTypeEnum.class, NotifyTypeEnum.class);
		convertMap.put(NotifyInfoResponse.RenotifyTypeEnum.class, RenotifyTypeEnum.class);
		convertMap.put(StatusNotifyDetailInfoResponse.StatusInvalidFlgEnum.class,StatusInvalidFlgEnum.class);
		convertMap.put(StatusNotifyDetailInfoResponse.StatusUpdatePriorityEnum.class, NotifyPriorityEnum.class);
		
		convertMap.put(JobNotifyDetailInfoResponse.JobExecFacilityFlgEnum.class,ExecFacilityFlgEnum.class);
		convertMap.put(JobNotifyDetailInfoResponse.InfoJobFailurePriorityEnum.class,NotifyPriorityEnum.class);
		convertMap.put(JobNotifyDetailInfoResponse.WarnJobFailurePriorityEnum.class,NotifyPriorityEnum.class);
		convertMap.put(JobNotifyDetailInfoResponse.CriticalJobFailurePriorityEnum.class,NotifyPriorityEnum.class);
		convertMap.put(JobNotifyDetailInfoResponse.UnknownJobFailurePriorityEnum.class,NotifyPriorityEnum.class);
		convertMap.put(JobNotifyDetailInfoResponse.NotifyJobTypeEnum.class,NotifyJobTypeEnum.class);
		
		convertMap.put(InfoEventNormalStateEnum.class,EventNormalStateEnum.class);
		convertMap.put(WarnEventNormalStateEnum.class,EventNormalStateEnum.class);
		convertMap.put(CriticalEventNormalStateEnum.class,EventNormalStateEnum.class);
		convertMap.put(UnknownEventNormalStateEnum.class,EventNormalStateEnum.class);
		
		convertMap.put(InfoInfraFailurePriorityEnum.class,NotifyPriorityEnum.class);
		convertMap.put(WarnInfraFailurePriorityEnum.class,NotifyPriorityEnum.class);
		convertMap.put(CriticalInfraFailurePriorityEnum.class,NotifyPriorityEnum.class);
		convertMap.put(UnknownInfraFailurePriorityEnum.class,NotifyPriorityEnum.class);
		
		convertMap.put(InfraExecFacilityFlgEnum.class,ExecFacilityFlgEnum.class);
		convertMap.put(EscalateFacilityFlgEnum.class,ExecFacilityFlgEnum.class);
		convertMap.put(InfoSyslogFacilityEnum.class,SyslogFacilityEnum.class);
		convertMap.put(InfoSyslogPriorityEnum.class,SyslogSeverityEnum.class);
		convertMap.put(WarnSyslogFacilityEnum.class,SyslogFacilityEnum.class);
		convertMap.put(WarnSyslogPriorityEnum.class,SyslogSeverityEnum.class);
		convertMap.put(CriticalSyslogFacilityEnum.class,SyslogFacilityEnum.class);
		convertMap.put(CriticalSyslogPriorityEnum.class,SyslogSeverityEnum.class);
		convertMap.put(UnknownSyslogFacilityEnum.class,SyslogFacilityEnum.class);
		convertMap.put(UnknownSyslogPriorityEnum.class,SyslogSeverityEnum.class);
		convertMap.put(NotifyRequestForUtility.NotifyTypeEnum.class, NotifyTypeEnum.class);
		convertMap.put(NotifyRequestForUtility.RenotifyTypeEnum.class, RenotifyTypeEnum.class);
		convertMap.put(NotifyRelationInfoResponse.NotifyTypeEnum.class,NotifyTypeEnum.class);
		convertMap.put(CloudNotifyDetailInfoResponse.PlatformTypeEnum.class,NotifyCloudPlatformTypeEnum.class);
		convertMap.put(CommandNotifyDetailInfoResponse.CommandSettingTypeEnum.class,CommandSettingTypeEnum.class);
		
		//jobMaster
		convertMap.put(JobInfoResponse.TypeEnum.class, JobTypeEnum.class);
		convertMap.put(JobInfoResponse.BeginPriorityEnum.class, com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum.class);
		convertMap.put(JobInfoResponse.NormalPriorityEnum.class, com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum.class);
		convertMap.put(JobInfoResponse.WarnPriorityEnum.class, com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum.class);
		convertMap.put(JobInfoResponse.AbnormalPriorityEnum.class, com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum.class);
		convertMap.put(JobInfoResponse.ReferJobSelectTypeEnum.class, ReferJobSelectTypeEnum.class);
		convertMap.put(JobInfoResponse.ReferJobSelectTypeEnum.class, ReferJobSelectTypeEnum.class);
		
		convertMap.put(JobCommandInfoResponse.ProcessingMethodEnum.class, ProcessingMethodEnum.class);
		convertMap.put(JobCommandInfoResponse.CommandRetryEndStatusEnum.class, CommandRetryEndStatusEnum.class);
		convertMap.put(JobCommandInfoResponse.StopTypeEnum.class, StopTypeEnum.class);
		
		convertMap.put(JobFileInfoResponse.ProcessingMethodEnum.class, ProcessingMethodEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.CalendarEndStatusEnum.class, EndStatusSelectEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.ConditionEnum.class, ConditionTypeEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.EndDelayConditionTypeEnum.class, ConditionTypeEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.EndDelayNotifyPriorityEnum.class,
				com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.EndDelayOperationEndStatusEnum.class, EndStatusSelectEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.EndDelayOperationTypeEnum.class, OperationEndDelayEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.EndStatusEnum.class, EndStatusSelectEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.ExclusiveBranchEndStatusEnum.class, EndStatusSelectEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.JobRetryEndStatusEnum.class, EndStatusSelectEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.MultiplicityNotifyPriorityEnum.class,
				com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.MultiplicityOperationEnum.class, OperationMultipleEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.SkipEndStatusEnum.class, EndStatusSelectEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.StartDelayConditionTypeEnum.class, ConditionTypeEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum.class,
				com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.StartDelayOperationEndStatusEnum.class, EndStatusSelectEnum.class);
		convertMap.put(JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.class, OperationStartDelayEnum.class);
		convertMap.put(JobObjectInfoResponse.DecisionConditionEnum.class, DecisionObjectEnum.class);
		convertMap.put(JobObjectInfoResponse.TypeEnum.class, JudgmentObjectEnum.class);
		convertMap.put(JobParameterInfoResponse.TypeEnum.class, JobParamTypeEnum.class);
		convertMap.put(JobScheduleResponse.ScheduleTypeEnum.class, ScheduleTypeEnum.class);
		convertMap.put(JobScheduleResponse.SessionPremakeScheduleTypeEnum.class, SessionPremakeScheduleTypeEnum.class);
		convertMap.put(JobScheduleResponse.SessionPremakeEveryXHourEnum.class, SessionPremakeEveryXHourEnum.class);
		convertMap.put(JobFileCheckResponse.EventTypeEnum.class,FileCheckEventTypeEnum.class);
		convertMap.put(JobFileCheckResponse.ModifyTypeEnum.class,FileCheckModifyTypeEnum.class);
		convertMap.put(JobRuntimeParamResponse.ParamTypeEnum.class,JobRuntimeParamTypeEnum.class);
		convertMap.put(JobMonitorInfoResponse.ProcessingMethodEnum.class, ProcessingMethodEnum.class);
		
		convertMap.put(JobRpaInfoResponse.RpaJobTypeEnum.class, RpaJobTypeEnum.class);
		convertMap.put(JobRpaInfoResponse.ProcessingMethodEnum.class, ProcessingMethodEnum.class);
		convertMap.put(JobRpaInfoResponse.RpaScreenshotEndValueConditionEnum.class, RpaJobReturnCodeConditionEnum.class);
		convertMap.put(JobRpaInfoResponse.CommandRetryEndStatusEnum.class, CommandRetryEndStatusEnum.class);
		convertMap.put(JobRpaInfoResponse.RpaNotLoginNotifyPriorityEnum.class, com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum.class);
		convertMap.put(JobRpaInfoResponse.RpaAlreadyRunningNotifyPriorityEnum.class, com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum.class);
		convertMap.put(JobRpaInfoResponse.RpaAbnormalExitNotifyPriorityEnum.class, com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum.class);
		convertMap.put(JobRpaInfoResponse.RpaStopTypeEnum.class, RpaStopTypeEnum.class);
		convertMap.put(JobRpaEndValueConditionInfoResponse.ConditionTypeEnum.class, RpaJobEndValueConditionTypeEnum.class);
		convertMap.put(JobRpaEndValueConditionInfoResponse.ReturnCodeConditionEnum.class, RpaJobReturnCodeConditionEnum.class);
		convertMap.put(JobLinkSendSettingResponse.ProcessModeEnum.class, ProcessingMethodEnum.class);
		convertMap.put(JobLinkSendSettingResponse.ProtocolEnum.class, JobLinkSendProtocol.class);
		convertMap.put(AddJobLinkSendSettingRequest.ProcessModeEnum.class, ProcessingMethodEnum.class);
		convertMap.put(AddJobLinkSendSettingRequest.ProtocolEnum.class, JobLinkSendProtocol.class);
		convertMap.put(JobObjectGroupInfoResponse.ConditionTypeEnum.class, ConditionTypeEnum.class);
		convertMap.put(JobObjectInfoResponse.StatusEnum.class, WaitStatusEnum.class);
		convertMap.put(JobOutputInfoResponse.FailureOperationTypeEnum.class, OperationJobOutputEnum.class);
		convertMap.put(JobOutputInfoResponse.FailureOperationEndStatusEnum.class, EndStatusSelectEnum.class);
		convertMap.put(JobOutputInfoResponse.FailureNotifyPriorityEnum.class, PriorityRequiredEnum.class);
		convertMap.put(JobFileCheckInfoResponse.ProcessingMethodEnum.class, ScopeJudgmentTargetEnum.class);
		convertMap.put(JobFileCheckInfoResponse.ModifyTypeEnum.class, FileCheckModifyTypeEnum.class);
		convertMap.put(JobResourceInfoResponse.ResourceTypeEnum.class, ResourceJobTypeEnum.class);
		convertMap.put(JobResourceInfoResponse.ResourceActionEnum.class, ResourceJobActionEnum.class);
		convertMap.put(JobLinkSendInfoResponse.FailureOperationEnum.class, OperationFailureEnum.class);
		convertMap.put(JobLinkSendInfoResponse.FailureEndStatusEnum.class, EndStatusSelectEnum.class);
		convertMap.put(JobLinkSendInfoResponse.PriorityEnum.class, PriorityRequiredEnum.class);
		convertMap.put(JobLinkInheritInfoResponse.KeyInfoEnum.class, JobLinkInheritKeyInfo.class);
		
		//FilterSetting
		convertMap.put(EventFilterSettingResponse.FilterCategoryEnum.class,FilterCategoryEnum.class);
		convertMap.put(StatusFilterSettingResponse.FilterCategoryEnum.class,FilterCategoryEnum.class);
		convertMap.put(JobHistoryFilterSettingResponse.FilterCategoryEnum.class,FilterCategoryEnum.class);
		convertMap.put(JobHistoryFilterConditionResponse.StatusEnum.class,StatusEnum.class);
		convertMap.put(JobHistoryFilterConditionResponse.EndStatusEnum.class,EndStatusEnum.class);
		convertMap.put(JobHistoryFilterConditionResponse.TriggerTypeEnum.class,JobTriggerTypeEnum.class);
		
		//Monitor
		convertMap.put(MonitorInfoResponse.MonitorTypeEnum.class,MonitorTypeEnum.class );
		convertMap.put(MonitorInfoResponse.RunIntervalEnum.class, RunIntervalEnum.class);
		convertMap.put(MonitorInfoResponse.PredictionMethodEnum.class, PredictionMethodEnum.class);
		convertMap.put(MonitorTruthValueInfoResponse.TruthValueEnum.class,  TruthValueEnum.class);
		convertMap.put(MonitorTruthValueInfoResponse.PriorityEnum.class,  PriorityEnum.class);
		convertMap.put(MonitorNumericValueInfoResponse.PriorityEnum.class,  PriorityEnum.class);
		convertMap.put(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.class,MonitorNumericTypeEnum.class );
		convertMap.put(MonitorStringValueInfoResponse.PriorityEnum.class,  PriorityEnum.class);
		convertMap.put(BinaryPatternInfoResponse.PriorityEnum.class,  PriorityEnum.class);
		convertMap.put(MonitorInfoResponse.PriorityChangeJudgmentTypeEnum.class,  PriorityChangeJudgmentTypeEnum.class);
		convertMap.put(MonitorInfoResponse.PriorityChangeFailureTypeEnum.class,  PriorityChangeFailureTypeEnum.class);
		
		//SystemPrivilegeInfo 
		convertMap.put(SystemPrivilegeInfoResponse.SystemFunctionEnum.class, SystemFunctionEnum.class);
		convertMap.put(SystemPrivilegeInfoResponse.SystemPrivilegeEnum.class, SystemPrivilegeModeEnum.class);
		convertMap.put(SystemPrivilegeInfoResponse.EditTypeEnum.class, SystemPrivilegeEditTypeEnum.class);
		//InfraManagementInfo
		convertMap.put(InfraManagementInfoResponse.StartPriorityEnum.class, PrioritySelectEnum.class);
		convertMap.put(InfraManagementInfoResponse.NormalPriorityRunEnum.class, PrioritySelectEnum.class);
		convertMap.put(InfraManagementInfoResponse.AbnormalPriorityRunEnum.class, PrioritySelectEnum.class);
		convertMap.put(InfraManagementInfoResponse.NormalPriorityCheckEnum.class, PrioritySelectEnum.class);
		convertMap.put(InfraManagementInfoResponse.AbnormalPriorityCheckEnum.class, PrioritySelectEnum.class);

		convertMap.put(FileTransferModuleInfoResponse.SendMethodTypeEnum.class, SendMethodTypeEnum.class);
		convertMap.put(CommandModuleInfoResponse.AccessMethodTypeEnum.class, AccessMethodTypeEnum.class);
		// reporting
		convertMap.put(ReportingScheduleResponse.OutputPeriodTypeEnum.class, ReportOutputPeriodTypeEnum.class);
		convertMap.put(ReportingScheduleResponse.OutputTypeEnum.class, ReportOutputTypeEnum.class);
		convertMap.put(ReportingScheduleInfoResponse.ScheduleTypeEnum.class, ScheduleTypeEnum.class);
		//Calender
		convertMap.put(CalendarDetailInfoResponse.DayTypeEnum.class, DayTypeEnum.class);
		convertMap.put(CalendarDetailInfoResponse.WeekXthEnum.class, WeekXthEnum.class);
		convertMap.put(CalendarDetailInfoResponse.WeekNoEnum.class, WeekNoEnum.class);	
		//Maintenance
		convertMap.put(MaintenanceScheduleResponse.TypeEnum.class,MaintenanceScheduleEnum.class);
		convertMap.put(MaintenanceScheduleRequest.TypeEnum.class,MaintenanceScheduleEnum.class);
	
		//Hub
		convertMap.put(AddTransferInfoRequest.IntervalEnum.class, TransferIntervalEnum.class);
		convertMap.put(TransferInfoResponse.IntervalEnum.class, TransferIntervalEnum.class);
		
		//HTTPMonitor
		convertMap.put(PageResponse.PriorityEnum.class, PriorityEnum.class);
		
		//JMX
		convertMap.put(JmxCheckInfoResponse.ConvertFlgEnum.class,ConvertFlagEnum.class);
		
		//SNMP TRAP
		convertMap.put(TrapValueInfoResponse.PriorityAnyVarBindEnum.class, PriorityEnum.class);
		convertMap.put(TrapValueInfoResponse.VersionEnum.class, SnmptrapVersionEnum.class);
		convertMap.put(VarBindPatternResponse.PriorityEnum.class, PriorityEnum.class);
		convertMap.put(TrapCheckInfoResponse.PriorityUnspecifiedEnum.class, PriorityEnum.class);
		
		//SNMP
		convertMap.put(SnmpCheckInfoResponse.ConvertFlgEnum.class,ConvertFlagEnum.class);
		
		//CustomTrap
		convertMap.put(CustomTrapCheckInfoResponse .ConvertFlgEnum.class, ConvertFlagEnum.class);
		
		//Custom
		convertMap.put(CustomCheckInfoResponse.ConvertFlgEnum.class,ConvertFlagEnum.class);
		
		//Port
		convertMap.put(PortCheckInfoResponse.ServiceIdEnum.class,PortServiceProtocolEnum.class);
		
		//BinaryFile
		convertMap.put(BinaryCheckInfoResponse.CollectTypeEnum.class,BinaryCollectTypeEnum.class);
		convertMap.put(BinaryCheckInfoResponse.CutTypeEnum.class,BinaryCutTypeEnum.class);
		convertMap.put(BinaryCheckInfoResponse.LengthTypeEnum.class,BinaryLengthTypeEnum.class);
		convertMap.put(BinaryCheckInfoResponse.TsTypeEnum.class,BinaryTimeStampTypeEnum.class);
		
		//integration
		convertMap.put(IntegrationConditionInfoResponse.TargetMonitorTypeEnum .class, MonitorTypeEnum.class);
		
		//Mib
		convertMap.put(TrapValueInfoRequest.PriorityAnyVarBindEnum.class,PriorityEnum.class);

		//RPA
		convertMap.put(RpaScenarioOperationResultCreateSettingResponse.IntervalEnum.class, ScenarioCreateIntervalEnum.class);
	}
	/**	 
	 * openapi提供のEnum について、定数値(Integer)へ変換する(ただし、事前に変換マップへ登録が必要)	 * 
	 * @param enumObject 変換対象となるEnum（org.openapitools.client.model配下であること）
	 */	public static Integer enumToInteger(Object enumObject) {
		return enumToConstant(null,enumObject,Integer.class);
	}
	
	/**
	 * openapi提供のEnum について、定数値(String)へ変換する(ただし、事前に変換マップへ登録が必要)
	 * 
	 * @param enumObject 変換対象となるEnum（org.openapitools.client.model配下であること）
	 * */
	public static String enumToString(Object enumObject) {
		return enumToConstant(null,enumObject,String.class);
	}

	/**
	 * 定数値(Intger)について、openapi提供のEnum へ変換する(ただし、事前に変換マップへ登録が必要)
	 * 
	 * @param srcValue 変換元になる定数
	 * @param convertType 変換先となるEnumの型（org.openapitools.client.model配下であること）
	 * */
	public static <T extends Enum<T>> T integerToEnum(Integer srcValue, Class<T> convertType ) throws HinemosUnknown, InvalidSetting {
		return constantToEnum(null,srcValue,convertType );
	}

	/**
	 * 定数値(String)について、openapi提供のEnum へ変換する(ただし、事前に変換マップへ登録が必要)
	 * 
	 * @param itemName 項目名（エラー発生時に利用）
	 * @param srcValue 変換元になる定数
	 * @param convertType 変換先となるEnumの型（org.openapitools.client.model配下であること）
	 * */
	public static <T extends Enum<T>> T stringToEnum(String srcValue, Class<T> convertType ) throws HinemosUnknown, InvalidSetting {
		return constantToEnum(null,srcValue,convertType );
	}
	
	/// openapi提供のEnum について、定数値へ変換する（元になったEnum定義 参照）
	@SuppressWarnings("unchecked")
	private static <T> T enumToConstant(String itemName ,Object enumObject, Class<T> constType){
		
		if(enumObject !=null && enumObject.getClass().isEnum()){
			Class<?> srcType = enumObject.getClass();
			Class<?> tagType = convertMap.get(srcType);
			if( tagType ==null){
				//  convertTypのMap登録がない場合ここに来る。コーディングミス(登録漏れ)以外はあり得ない想定
				log.error("enumToConstant(): OpenApiEnumConverter does not support "+ srcType.getName() );
				return null;
			}
			try{
				//srcのEnum(OpenApi提供)から値を取得
				Method srcMethod = srcType.getMethod("getValue");
				String srcValue = (String) srcMethod.invoke(enumObject);

				//tagのEnum(元のEnum)の列挙子に変換
				Method tagMethod = tagType.getDeclaredMethod("valueOf",String.class);
				Object tagValue = tagMethod.invoke(null,srcValue);
				
				//tagのEnum の定数値を取得
				if( tagValue instanceof EnumDto ){
					@SuppressWarnings("rawtypes")
					Object ret = ((EnumDto)tagValue).getCode();
					return (T) ret;
				}
				// 該当無しならMAP設定が不正とみなす。コーディングミス(登録漏れ)以外はあり得ない想定
				log.error( "enumToConstant():OpenApiEnumConverter settings for "+srcType.getName() +" are incorrect. ItemName"+generateItemName(itemName ,srcType));
				return null;
			}catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e ){
				//変換エラー(getDeclaredMethod や invokeによるExcepiton )
				log.error( "enumToConstant(): OpenApiEnumConverter utility does not support "+ srcType.getName() );
				return null;
			}
		}
		return null;
	}

	/// 定数値について、openapi提供のEnum へ変換する（元になったEnum定義 参照）
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T extends Enum<T>> T constantToEnum(String itemName ,Object srcValue, Class<T> convertType ) throws HinemosUnknown, InvalidSetting {
		
		if (srcValue != null) {
			String enumName =null; 
			//tagのEnum(元のEnum)の列挙子に変換して名称を取得
			Class<?> tagType = convertMap.get(convertType);
			if( tagType ==null){
				//  convertTypのMap登録がない場合ここに来る。コーディングミス(登録漏れ)以外はあり得ない想定
				throw new HinemosUnknown( "OpenApiEnumConverter does not support "+ convertType.getName()+"." );
			}
			try{
				EnumDto<?>[] des = (EnumDto<?>[]) tagType.getEnumConstants();
				for (EnumDto<?> destVal : des) {
					if (destVal.getCode().equals(srcValue)) {
						enumName = ((Enum) destVal).name();
						break;
					}
				}
			}catch( Exception e ){
				// convertMap tagTypeがEnumでないとメソッドが失敗してここに来る。コーディングミス(convertMap設定間違い)以外はあり得ない想定
				throw new HinemosUnknown( "OpenApiEnumConverter settings for "+convertType.getName() +" are incorrect."+ e.getMessage(),e );
			} 
			if (enumName == null) {
				// srcValue 指定が間違っていると 該当するEnumが見つからず失敗してここに来る想定
				throw new InvalidSetting("Setting value(" + srcValue + ") of \"" + generateItemName(itemName,convertType) + "\" is invalid. ");
			}
			//名称が取得できていれば srcのEnum(OpenApi提供)の列挙子にもどす
			try{
				Method srcMethod = convertType.getDeclaredMethod("fromValue",String.class);
				T srcEnum = (T)srcMethod.invoke(null,enumName);
				return srcEnum;
			}catch( Exception e ){
				// convertMap の組み合わせが間違っていると fromValueメソッドが失敗してここに来る。コーディングミス(convertMap設定間違い)以外はあり得ない想定
				throw new HinemosUnknown( "OpenApiEnumConverter settings for "+convertType.getName() +" are incorrect."+ e.getMessage(),e );
			} 
		}
		return null;
	}
	private static String generateItemName (String itemName , Class<?> convertType ){
		// 設定がなければ OpenApi提供Enumの名称から項目名称を取得（Dto上のメンバ名称と連動するため）。
		// 命名規則は メンバ名(アッパーキャメル)＋"Enum"。アッパーはローワーに直して 末尾4文字は必ずEnumなのでカット
		if (itemName == null) {
			try{
				itemName = convertType.getSimpleName().substring(0, (convertType.getSimpleName().length() - 4));
				itemName = itemName.substring(0, 1).toLowerCase() + itemName.substring(1);
			}catch( Exception e ){
				//異常発生時はそのまま返す
				itemName = convertType.getSimpleName();
			}
		}
		return itemName;
	}

}
