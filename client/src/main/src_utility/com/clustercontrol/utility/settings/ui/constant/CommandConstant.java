/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.constant;


public class CommandConstant {
	public static final String ACTION_MASTER_PLATFORM = "com.clustercontrol.utility.settings.collect.action.PlatformMasterAction";
	public static final String ACTION_MASTER_COLLECT = "com.clustercontrol.utility.settings.collect.action.CollectMasterAction";
	public static final String ACTION_MASTER_JMX = "com.clustercontrol.utility.settings.monitor.action.JmxMasterAction";
	public static final String ACTION_PLATFORM_ACCESS_USER_ROLE = "com.clustercontrol.utility.settings.platform.action.UserRoleAction";
	public static final String ACTION_PLATFORM_ACCESS_SYSTEM_PRIVILEGE = "com.clustercontrol.utility.settings.platform.action.SystemPrivilegeAction";
	public static final String ACTION_PLATFORM_ACCESS_OBJECT_PRIVILEGE = "com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction";
	public static final String ACTION_PLATFORM_REPOSITORY_NODE = "com.clustercontrol.utility.settings.platform.action.RepositoryNodeAction";
	public static final String ACTION_PLATFORM_REPOSITORY_SCOPE = "com.clustercontrol.utility.settings.platform.action.RepositoryScopeAction";
	public static final String ACTION_PLATFORM_CALENDAR = "com.clustercontrol.utility.settings.platform.action.CalendarAction";
	public static final String ACTION_PLATFORM_MAINTENANCE = "com.clustercontrol.utility.settings.system.action.MaintenanceAction";
	public static final String ACTION_PLATFORM_NOTIFY = "com.clustercontrol.utility.settings.platform.action.NotifyAction";
	public static final String ACTION_PLATFORM_MAIL_TEMPLATE = "com.clustercontrol.utility.settings.platform.action.MailTemplateAction";
	public static final String ACTION_PLATFORM_LOG_FORMAT = "com.clustercontrol.utility.settings.platform.action.LogFormatAction";
	public static final String ACTION_PLATFORM_HINEMOS_PROPERTY = "com.clustercontrol.utility.settings.system.action.HinemosPropertyAction";
	public static final String ACTION_MONITOR_AGENT = "com.clustercontrol.utility.settings.monitor.action.AgentAction";
	public static final String ACTION_MONITOR_HTTP = "com.clustercontrol.utility.settings.monitor.action.HttpAction";
	public static final String ACTION_MONITOR_PERFORMANCE = "com.clustercontrol.utility.settings.monitor.action.PerfAction";
	public static final String ACTION_MONITOR_PING = "com.clustercontrol.utility.settings.monitor.action.PingAction";
	public static final String ACTION_MONITOR_PORT = "com.clustercontrol.utility.settings.monitor.action.PortAction";
	public static final String ACTION_MONITOR_PROCESS = "com.clustercontrol.utility.settings.monitor.action.ProcessAction";
	public static final String ACTION_MONITOR_SNMP = "com.clustercontrol.utility.settings.monitor.action.SnmpAction";
	public static final String ACTION_MONITOR_SNMPTRAP = "com.clustercontrol.utility.settings.monitor.action.SnmpTrapAction";
	public static final String ACTION_MONITOR_SQL = "com.clustercontrol.utility.settings.monitor.action.SqlAction";
	public static final String ACTION_MONITOR_SYSTEMLOG = "com.clustercontrol.utility.settings.monitor.action.SystemlogAction";
	public static final String ACTION_MONITOR_LOGFILE = "com.clustercontrol.utility.settings.monitor.action.LogfileAction";
	public static final String ACTION_MONITOR_CUSTOM = "com.clustercontrol.utility.settings.monitor.action.CustomAction";
	public static final String ACTION_MONITOR_WINSERVICE = "com.clustercontrol.utility.settings.monitor.action.WinServiceAction";
	public static final String ACTION_MONITOR_WINEVENT = "com.clustercontrol.utility.settings.monitor.action.WinEventAction";
	public static final String ACTION_MONITOR_HTTP_SCENARIO = "com.clustercontrol.utility.settings.monitor.action.HttpScenarioAction";
	public static final String ACTION_MONITOR_JMX = "com.clustercontrol.utility.settings.monitor.action.JmxAction";
	public static final String ACTION_MONITOR_CUSTOMTRAP = "com.clustercontrol.utility.settings.monitor.action.CustomTrapAction";
	public static final String ACTION_MONITOR_LOGCOUNT = "com.clustercontrol.utility.settings.monitor.action.LogcountAction";
	public static final String ACTION_MONITOR_BINARYFILE = "com.clustercontrol.utility.settings.monitor.action.BinaryfileAction";
	public static final String ACTION_MONITOR_PCAP = "com.clustercontrol.utility.settings.monitor.action.PacketCapAction";
	public static final String ACTION_MONITOR_INTEGRATION = "com.clustercontrol.utility.settings.monitor.action.IntegrationAction";
	public static final String ACTION_MONITOR_CORRELATION = "com.clustercontrol.utility.settings.monitor.action.CorrelationAction";
	public static final String ACTION_HUB_TRANSFER = "com.clustercontrol.utility.settings.hub.action.HubTransferAction";
	public static final String ACTION_INFRA_SETTING = "com.clustercontrol.utility.settings.infra.action.InfraSettingAction";
	public static final String ACTION_INFRA_FILE = "com.clustercontrol.utility.settings.infra.action.InfraFileAction";
	public static final String ACTION_JOB_MST = "com.clustercontrol.utility.settings.job.action.JobMasterAction";
	public static final String ACTION_JOB_KICK = "com.clustercontrol.utility.settings.job.action.JobKickAction";
	public static final String ACTION_NODE_MAP_SETTING = "com.clustercontrol.utility.settings.nodemap.action.NodeMapAction";
	public static final String ACTION_NODE_MAP_IMAGE = "com.clustercontrol.utility.settings.nodemap.action.NodeMapImageAction";
	public static final String ACTION_REPORT_SCHEDULE = "com.clustercontrol.utility.settings.report.action.ReportScheduleAction";
	public static final String ACTION_REPORT_TEMPLATE = "com.clustercontrol.utility.settings.report.action.ReportTemplateAction";
	public static final String ACTION_JOBMAP_IMAGE = "com.clustercontrol.utility.settings.jobmap.action.JobmapImageAction";
	public static final String ACTION_CLOUD_USER = "com.clustercontrol.utility.settings.cloud.action.CloudUserAction";
	public static final String ACTION_CLOUD_MON_SERVICE = "com.clustercontrol.utility.settings.monitor.action.CloudServiceAction";
	public static final String ACTION_CLOUD_MON_BILLING = "com.clustercontrol.utility.settings.monitor.action.BillingAction";
	
	public static final int WEIGHT_OTHER=-1;
	public static final int WEIGHT_MASTER_PLATFORM = 0;
	public static final int WEIGHT_MASTER_COLLECT = 1;
	public static final int WEIGHT_MASTER_JMX = 2;
	public static final int WEIGHT_PLATFORM_ACCESS_USER_ROLE = 10;
	public static final int WEIGHT_PLATFORM_ACCESS_SYSTEM_PRIVILEGE = 11;
	public static final int WEIGHT_PLATFORM_ACCESS_OBJECT_PRIVILEGE = 12;
	public static final int WEIGHT_PLATFORM_REPOSITORY_NODE = 100;
	public static final int WEIGHT_PLATFORM_REPOSITORY_SCOPE = 101;
	public static final int WEIGHT_PLATFORM_CALENDAR =  200;
	public static final int WEIGHT_PLATFORM_MAINTENANCE = 500;
	public static final int WEIGHT_PLATFORM_HINEMOS_PROPERTY = 501;
	public static final int WEIGHT_PLATFORM_NOTIFY = 400;
	public static final int WEIGHT_PLATFORM_MAIL_TEMPLATE = 300;
	public static final int WEIGHT_PLATFORM_LOG_FORMAT = 301;
	public static final int WEIGHT_MONITOR_AGENT = 700;
	public static final int WEIGHT_MONITOR_HTTP = 701;
	public static final int WEIGHT_MONITOR_PERFORMANCE = 702;
	public static final int WEIGHT_MONITOR_PING = 703;
	public static final int WEIGHT_MONITOR_PORT = 704;
	public static final int WEIGHT_MONITOR_PROCESS = 705;
	public static final int WEIGHT_MONITOR_SNMP = 706;
	public static final int WEIGHT_MONITOR_SNMPTRAP = 707;
	public static final int WEIGHT_MONITOR_SQL = 708;
	public static final int WEIGHT_MONITOR_SYSTEMLOG = 709;
	public static final int WEIGHT_MONITOR_LOGFILE = 710;
	public static final int WEIGHT_MONITOR_CUSTOM = 711;
	public static final int WEIGHT_MONITOR_WINSERVICE = 712;
	public static final int WEIGHT_MONITOR_WINEVENT = 713;
	public static final int WEIGHT_MONITOR_HTTP_SCENARIO = 714;
	public static final int WEIGHT_MONITOR_JMX = 715;
	public static final int WEIGHT_MONITOR_CUSTOMTRAP = 716;
	public static final int WEIGHT_MONITOR_LOGCOUNT = 717;
	public static final int WEIGHT_MONITOR_BINARYFILE = 718;
	public static final int WEIGHT_MONITOR_PCAP = 719;
	public static final int WEIGHT_MONITOR_INTEGRATION = 720;
	public static final int WEIGHT_MONITOR_CORRELATION = 721;
	public static final int WEIGHT_HUB_TRANSFER = 800;
	public static final int WEIGHT_INFRA_FILE = 850;
	public static final int WEIGHT_INFRA_SETTING = 851;
	public static final int WEIGHT_JOBMAP_IMAGE = 900;
	public static final int WEIGHT_JOB_MST = 901;
	public static final int WEIGHT_JOB_KICK = 950;
	public static final int WEIGHT_NODE_MAP_SETTING = 1001;
	public static final int WEIGHT_NODE_MAP_IMAGE = 1000;
	public static final int WEIGHT_REPORT_SETTING = 1101;
	public static final int WEIGHT_REPORT_TEMPLATE = 1100;
	public static final int WEIGHT_CLOUD_USER = 1200;
	public static final int WEIGHT_CLOUD_MON_SERVICE = 1201;
	public static final int WEIGHT_CLOUD_MON_BILLING = 1202;
}