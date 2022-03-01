/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;

/**
 * REST向けのリソースメソッドに関する、ログの出力用アノテーション。
 * <br>
 * 操作ログ上の 処理動作と処理対象の名称、ログレベルのタイプを指定します。
 */
@Retention(RetentionPolicy.RUNTIME)  
@Target({ 
	ElementType.TYPE,
	ElementType.METHOD
})
public @interface RestLog {

	public enum LogAction {
		Get,
		Add,
		Modify,
		Delete,
		Login,
		Logout,
		// 以下はPOSTメソッドのアクション
		Create,
		Download,
		LogCount,
		Exec,
		CheckExec,
		Operation,
		Collect,
		Ping,
		AutoConnect,
		Make,
		PowerOn,
		PowerOff,
		Suspend,
		Reboot,
		Attach,
		Detach,
		Register,
		Deregister,
		Migrate,
		Clone,
	}

	public enum LogTarget {
		Null,// 無指定ならNullを選択
		/** AccessRestEndpoints */
		ObjectPrivilege,
		Role,
		SystemPrivilegeRole,
		SystemPrivilege,
		User,
		Version,
		/** CalendarRestEndpoints */
		Calendar,
		CalendarDetail,
		Pattern,
		/** CollectRestEndpoints */
		Key,
		Data,
		ItemCodeMst,
		DataCache,
		DataRaw,
		DataString,
		DataBinary,
		Master,
		/** CommonRestEndpoints */
		MailTemplate,
		HinemosProperty,
		HinemosTime,
		RestAccessInfo,
		CommandTemplate,
		/** HubRestEndpoints */
		LogFormat,
		Transfer,
		/** InfraRestEndpoints */
		Management,
		Session,
		AccessInfo,
		CheckResult,
		File,
		/** JobRestEndpoints */
		Job,
		Jobunit,
		Lock,
		// Session,
		History,
		SessionJob,
		SessionNode,
		SessionFile,
		Jobnet,
		CommandJob,
		FileJob,
		ReferJob,
		MonitorJob,
		ApprovalJob,
		JobLinkSendJob,
		JobLinkRcvJob,
		FileCheckJob,
		Schedule,
		Filecheck,
		JobLinkRcv,
		RpaJob,
		Manual,
		Kick,
		OperationProp,
		Queue,
		QueueActivity,
		IconImage,
		JoblinkMessage,
		Joblinksend,
		RpaScreenshot,
		RpaLoginResolution,
		/** JobMapRestEndpoints */
		CheckPublish,
		// IconImage,
		JobMapIconIdDefault,
		/** MaintenanceRestEndpoints */
		Maintenance,
		MaintenanceType,
		/** MonitorResultRestEndpoints */
		Event,
		Scope,
		Status,
		EventCustomCommand,
		/** MonitorsettingRestEndpoints */
		Monitor,
		HttpScenario,
		HttpNumeric,
		HttpString,
		Agent,
		Jmx,
		Ping,
		Snmptrap,
		SnmpNumeric,
		SnmpString,
		SqlNumeric,
		SqlString,
		Winevent,
		Winservice,
		CustomtrapNumeric,
		CustomtrapString,
		CustomNumeric,
		CustomString,
		Cloudservice,
		Cloudservicebilling,
		Cloudservicebillingdetail,
		CloudLog,
		Serviceport,
		Systemlog,
		Binaryfile,
		PacketCapture,
		Process,
		Performance,
		Logfile,
		Logcount,
		Correlation,
		Integration,
		Sql,
		Jmxmaster,
		BinaryCheckInfo,
		RpaLogfile,
		RpaToolService,
		/** NodeMapRestEndpoints */
		// CheckPublish,
		Nodemap,
		Node,
		BackgroundImage,
		// IconImage,
		/** NotifyRestEndpoints */
		// Status,
		// Event,
		Mail,
		// Job
		LogEscalate,
		Command,
		Infra,
		Rest,
		Cloud,
		Message,
		Notify,
		NotifyRelation,
		/** ReportingRestEndpoints */
		// CheckPublish,
		// Schedule,
		TemplateSet,
		TemplateSetDetail,
		Template,
		/** RepositoryRestEndpoints */
		Facility,
		// Node,
		Snmp,
		// Scope,
		FacilityRelation,
		Platform,
		SubPlatform,
		LastUpdateTime,
		AgentStatus,
		// Agent,
		NodeConfig,
		ScopeAndFacilityRelation,
		PlatformMaster,
		SubPlatformMaster,
		/** UtlityRestEdnpoints */
		// CheckPublish,
		// Node,
		// Scope,
		Jobmaster,
		// Role,
		// User,
		RoleUser,
		// Monitor,
		// ObjectPrivilege,
		// SystemPrivilege,
		InfraManagement,
		// Notify,
		Reporting,
		ReportingTemplateSet,
		// CloudScope,
		// LogFormat,
		NodeConfigSetting,
		// MailTemplate,
		// Calendar,
		CalendarPattern,
		// Maintenance,
		// HinemosProperty,
		// FileCheck,
		// Schedule,
		JobManual,
		JobQueue,
		// Transfer,
		// PlatformMaster,
		NodeMapModel,
		// Jmxmaster,
		ImportUnitNumber,
		/** CloudRestEdnpoints */
		// CheckPublish,
		// Command,
		ScopeAssignRule,
		CloudPlatform,
		CloudScope,
		CloudLoginUser,
		Instance,
		InstanceBackup,
		Storage,
		StorageBackup,
		XcloudRepository,
		ServiceCondition,
		Network,
		Billing,
		BillingDetail,
		CloudService,
		/** AWSOptionRestEndpoints */
		// Instance,
		AvailabilityZones,
		Vpc,
		InstanceType,
		SecurityGroup,
		KeyPair,
		InstanceInitiatedShutdownBehaviorTypes,
		VolumeTypes,
		WindowsPassword,
		Image,
		Snapshot,
		InstanceAttribute,
		NetworkInterface,
		Elb,
		/** VMwareOptionRestEndpoints */
		// Instance,
		ResoucePool,
		DiskType,
		// Storage,
		VirtualMachine,
		Datastores,
		/** FilterSettingRestEndpoints */
		FilterSetting_User,
		FilterSetting_Common,
		FilterSetting,		// User,
		/** SdmlRestEndpoints */
		controlSetting,
		SdmlMonitorTypeMaster,
		/** GrafanaRestEndpoints */
		EventAggregation,
		StatusAggregation,
		JobHistoryAggregation,
		JobLastRunTime,
		/** RpaRestEndpoints */
		RpaManagementTool,
		RpaScenario,
		RpaScenarioTag,
		RpaScenarioResult,
		RpaScenarioResultCreate,
	}

	public enum LogType {
		REFERENCE,
		LOGIN,
		UPDATE
	}
	
	LogAction action();
	LogTarget target() default LogTarget.Null;
	LogType type();
	LogFuncName funcName() default LogFuncName.Default;

}
