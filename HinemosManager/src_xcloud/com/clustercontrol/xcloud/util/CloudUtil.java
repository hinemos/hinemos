/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.ObjectPrivilegeFilterInfo;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.PrivilegeDuplicate;
import com.clustercontrol.fault.UsedObjectPrivilege;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.ConditionTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobCommandInfo;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobEndStatusInfo;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeOsInfo;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.MultiTenantSupport;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.InternalIdAbstract;
import com.clustercontrol.util.Singletons;
import com.clustercontrol.util.apllog.AplLogger;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.common.InternalIdCloud;

public class CloudUtil {
	private static final Logger logger = Logger.getLogger(CloudUtil.class);
	/** DB上の最大値 */
	public static final int INSTANCE_NAME_MAX_BYTE = 128;
	public static final int ENTITY_NAME_MAX_BYTE = 128;

	/** SQLに渡すListパラメータのサイズ上限(SQL Serverの上限2100に抵触させないため) */
	public static final int SQL_PARAM_NUMBER_THRESHOLD = 2000;

	protected CloudUtil() {
	}

	public static ScopeInfo createScope(String facilityId, String facilityName, String roleId) {
		
		long now = HinemosTime.currentTimeMillis();
		
		ScopeInfo scope = new ScopeInfo();

		scope.setFacilityName(facilityName);
		scope.setFacilityId(facilityId);

		scope.setFacilityType(FacilityConstant.TYPE_SCOPE);
		scope.setDisplaySortOrder(100);
		scope.setValid(true);
		scope.setCreateDatetime(now);
		scope.setModifyDatetime(now);
		scope.setOwnerRoleId(roleId);

		return scope;
	}

	public static com.clustercontrol.repository.model.NodeInfo createNodeInfo(
			String facilityId,
			String facilityName,
			String platform,
			String subPlatform,
			String nodeName,
			String description,
			String serviceId,
			String cloudScopeId,
			String resourceName,
			String resourceType,
			String instanceId,
			String location,
			String roleId,
			NodeVariableInfo...variables
			) {
		NodeInfo nodeInfo = new NodeInfo();

		nodeInfo.setOwnerRoleId(roleId); // setDefaultValue()内で参照するので先に設定する
		setDefaultValue(nodeInfo);

		nodeInfo.setFacilityId(facilityId);
		// DBの桁数に合わせてカットする
		nodeInfo.setFacilityName(truncateString(facilityName, com.clustercontrol.repository.util.RepositoryUtil.NODE_FACILITY_NAME_MAX_BYTE));
		nodeInfo.setPlatformFamily(platform);
		nodeInfo.setSubPlatformFamily(subPlatform);
		nodeInfo.setIpAddressVersion(4);
		nodeInfo.setIpAddressV4(HinemosPropertyCommon.xcloud_ipaddress_notavailable.getStringValue());
		// DBの桁数に合わせてカットする
		nodeInfo.setNodeName(truncateString(nodeName, com.clustercontrol.repository.util.RepositoryUtil.NODE_NODE_NAME_MAX_BYTE));
		nodeInfo.setDescription(description);

		// ノード変数
		String key = HinemosPropertyCommon.xcloud_node_property_node_variablename.getStringValue();
		String[] keyArr = key.split(",");
		String value = HinemosPropertyCommon.xcloud_node_property_node_variablevalue.getStringValue();
		String[] valueArr = value.split(",");
		if (keyArr.length == valueArr.length && keyArr.length > 0) {
			ArrayList<NodeVariableInfo> vars = new ArrayList<NodeVariableInfo>();
			for (int i = 0; i < keyArr.length; i++) {
				NodeVariableInfo var = new NodeVariableInfo(facilityId, keyArr[i]);
				var.setNodeVariableValue(valueArr[i]);
				vars.add(var);
			}
			nodeInfo.setNodeVariableInfo(vars);
		}
		
		ArrayList<NodeHostnameInfo> hostnameList = new ArrayList<NodeHostnameInfo>();
		hostnameList.add(new NodeHostnameInfo(facilityId, nodeInfo.getNodeName()));
		nodeInfo.setNodeHostnameInfo(hostnameList);
		
		nodeInfo.setCloudService(serviceId);
		nodeInfo.setCloudScope(cloudScopeId);
		// DBの桁数に合わせてカットする
		nodeInfo.setCloudResourceName(truncateString(resourceName, com.clustercontrol.repository.util.RepositoryUtil.NODE_CLOUD_RESOURCE_NAME_MAX_BYTE));
		nodeInfo.setCloudResourceType(resourceType);
		nodeInfo.setCloudResourceId(instanceId);
		nodeInfo.setCloudLocation(location);

		return nodeInfo;
	}

	/**
	 * Not used. Will be removed in the future.
	 */
	@Deprecated
	public static com.clustercontrol.repository.model.NodeInfo resetNodeInfo(
			NodeInfo nodeInfo, 
			String facilityName,
			String platform,
			String subPlatform,
			String nodeName,
			String description,
			String serviceId,
			String cloudScopeId,
			String resourceName,
			String resourceType,
			String instanceId,
			String location
			) {
		nodeInfo.setFacilityName(facilityName);
		nodeInfo.setPlatformFamily(platform);
		nodeInfo.setSubPlatformFamily(subPlatform);
		nodeInfo.setIpAddressVersion(4);
		nodeInfo.setIpAddressV4(HinemosPropertyCommon.xcloud_ipaddress_notavailable.getStringValue());
		nodeInfo.setNodeName(nodeName);
		nodeInfo.setDescription(description);

		nodeInfo.setCloudService(serviceId);
		nodeInfo.setCloudScope(cloudScopeId);
		nodeInfo.setCloudResourceName(resourceName);
		nodeInfo.setCloudResourceType(resourceType);
		nodeInfo.setCloudResourceId(instanceId);
		nodeInfo.setCloudLocation(location);

		ArrayList<NodeDiskInfo> disklist = new ArrayList<NodeDiskInfo>();
		nodeInfo.setNodeDiskInfo(disklist);

		return nodeInfo;
	}

	public static void clearNodeInfo(NodeInfo nodeInfo) {
		nodeInfo.setNodeDiskInfo(new ArrayList<NodeDiskInfo>(nodeInfo.getNodeDiskInfo()));
		Iterator<NodeDiskInfo> iter = nodeInfo.getNodeDiskInfo().iterator();
		while (iter.hasNext()) {
			NodeDiskInfo info = iter.next();
			if (info.getDeviceDescription().startsWith("storageId=")) {
				iter.remove();
			}
		}
		
		nodeInfo.setCloudService("");
		nodeInfo.setCloudScope("");
		nodeInfo.setCloudResourceName("");
		nodeInfo.setCloudResourceType("");
		nodeInfo.setCloudResourceId("");
		nodeInfo.setCloudLocation("");
	}

	private static void setDefaultValue(NodeInfo nodeInfo) {
		nodeInfo.setFacilityType(FacilityConstant.TYPE_NODE);
		nodeInfo.setDisplaySortOrder(100);
		if (nodeInfo.getFacilityId() == null) {
			nodeInfo.setFacilityId("");
		}
		if (nodeInfo.getFacilityName() == null) {
			nodeInfo.setFacilityName("");
		}
		if (nodeInfo.getDescription() == null) {
			nodeInfo.setDescription("");
		}
		if (nodeInfo.getValid() == null) {
			nodeInfo.setValid(Boolean.TRUE);
		}
		if (nodeInfo.getCreateUserId() == null) {
			nodeInfo.setCreateUserId("");
		}
		if (nodeInfo.getCreateDatetime() == null) {
			nodeInfo.setCreateDatetime(null);
		}
		if (nodeInfo.getModifyUserId() == null) {
			nodeInfo.setModifyUserId("");
		}
		if (nodeInfo.getModifyDatetime() == null) {
			nodeInfo.setModifyDatetime(null);
		}

		// HW
		if (nodeInfo.getPlatformFamily() == null) {
			nodeInfo.setPlatformFamily("");
		}
		if (nodeInfo.getHardwareType() == null) {
			nodeInfo.setHardwareType("");
		}
		if (nodeInfo.getIconImage() == null) {
			nodeInfo.setIconImage("");
		}

		// IPアドレス
		if (nodeInfo.getIpAddressVersion() == null) {
			nodeInfo.setIpAddressVersion(-1);
		}
		if (nodeInfo.getIpAddressV4() == null) {
			nodeInfo.setIpAddressV4("");
		}
		if (nodeInfo.getIpAddressV6() == null) {
			nodeInfo.setIpAddressV6("");
		}

		// OS
		if (nodeInfo.getNodeName() == null) {
			nodeInfo.setNodeName("");
		}
		if (nodeInfo.getNodeOsInfo() == null) {
			nodeInfo.setNodeOsInfo(new NodeOsInfo(nodeInfo.getFacilityId()));
		}
		if (nodeInfo.getNodeOsInfo().getOsName() == null) {
			nodeInfo.getNodeOsInfo().setOsName("");
		}
		if (nodeInfo.getNodeOsInfo().getOsRelease() == null) {
			nodeInfo.getNodeOsInfo().setOsRelease("");
		}
		if (nodeInfo.getNodeOsInfo().getOsVersion() == null) {
			nodeInfo.getNodeOsInfo().setOsVersion("");
		}
		if (nodeInfo.getNodeOsInfo().getCharacterSet() == null) {
			nodeInfo.getNodeOsInfo().setCharacterSet("");
		}
		if (nodeInfo.getNodeOsInfo().getStartupDateTime() == null) {
			nodeInfo.getNodeOsInfo().setStartupDateTime(0L);
		}

		// エージェント
		nodeInfo.setAgentAwakePort(HinemosPropertyCommon.xcloud_node_property_agent_awakeport.getIntegerValue());
		nodeInfo.setJobPriority(HinemosPropertyCommon.xcloud_node_property_job_priority.getIntegerValue());
		nodeInfo.setJobMultiplicity(HinemosPropertyCommon.xcloud_node_property_job_multiplicity.getIntegerValue());
		
		// RPA
		nodeInfo.setRpaLogDir(HinemosPropertyCommon.xcloud_node_property_rpa_log_directory.getStringValue());
		nodeInfo.setRpaManagementToolType(HinemosPropertyCommon.xcloud_node_property_rpa_management_tool_type.getStringValue());
		nodeInfo.setRpaResourceId(HinemosPropertyCommon.xcloud_node_property_rpa_resource_id.getStringValue());
		nodeInfo.setRpaUser(HinemosPropertyCommon.xcloud_node_property_rpa_user.getStringValue());
		nodeInfo.setRpaExecEnvId(HinemosPropertyCommon.xcloud_node_property_rpa_execution_environment_id.getStringValue());

		// SNMP
		nodeInfo.setSnmpUser(HinemosPropertyCommon.xcloud_node_property_snmp_user.getStringValue());
		nodeInfo.setSnmpPort(HinemosPropertyCommon.xcloud_node_property_snmp_port.getIntegerValue());
		nodeInfo.setSnmpCommunity(HinemosPropertyCommon.xcloud_node_property_snmp_community.getStringValue());
		nodeInfo.setSnmpVersion(SnmpVersionConstant.stringToType(HinemosPropertyCommon.xcloud_node_property_snmp_version.getStringValue()));
		nodeInfo.setSnmpSecurityLevel(HinemosPropertyCommon.xcloud_node_property_snmp_securitylevel.getStringValue());
		nodeInfo.setSnmpAuthPassword(HinemosPropertyCommon.xcloud_node_property_snmp_auth_password.getStringValue());
		nodeInfo.setSnmpPrivPassword(HinemosPropertyCommon.xcloud_node_property_snmp_priv_password.getStringValue());
		nodeInfo.setSnmpAuthProtocol(HinemosPropertyCommon.xcloud_node_property_snmp_auth_protocol.getStringValue());
		nodeInfo.setSnmpPrivProtocol(HinemosPropertyCommon.xcloud_node_property_snmp_priv_protocol.getStringValue());
		nodeInfo.setSnmpTimeout(HinemosPropertyCommon.xcloud_node_property_snmp_timeout.getIntegerValue());
		nodeInfo.setSnmpRetryCount(HinemosPropertyCommon.xcloud_node_property_snmp_retries.getIntegerValue());

		// WBEM
		nodeInfo.setWbemUser(HinemosPropertyCommon.xcloud_node_property_wbem_user.getStringValue());
		nodeInfo.setWbemUserPassword(HinemosPropertyCommon.xcloud_node_property_wbem_userpassword.getStringValue());
		nodeInfo.setWbemPort(HinemosPropertyCommon.xcloud_node_property_wbem_port.getIntegerValue());
		nodeInfo.setWbemProtocol(HinemosPropertyCommon.xcloud_node_property_wbem_protocol.getStringValue());		
		nodeInfo.setWbemTimeout(HinemosPropertyCommon.xcloud_node_property_wbem_timeout.getIntegerValue());
		nodeInfo.setWbemRetryCount(HinemosPropertyCommon.xcloud_node_property_wbem_retries.getIntegerValue());

		// IPMI
		nodeInfo.setIpmiIpAddress(HinemosPropertyCommon.xcloud_node_property_ipmi_ipaddress.getStringValue());
		nodeInfo.setIpmiPort(HinemosPropertyCommon.xcloud_node_property_ipmi_port.getIntegerValue());
		nodeInfo.setIpmiUser(HinemosPropertyCommon.xcloud_node_property_ipmi_user.getStringValue());
		nodeInfo.setIpmiUserPassword(HinemosPropertyCommon.xcloud_node_property_ipmi_userpassword.getStringValue());
		nodeInfo.setIpmiTimeout(HinemosPropertyCommon.xcloud_node_property_ipmi_timeout.getIntegerValue());
		nodeInfo.setIpmiRetries(HinemosPropertyCommon.xcloud_node_property_ipmi_retries.getIntegerValue());
		nodeInfo.setIpmiProtocol(HinemosPropertyCommon.xcloud_node_property_ipmi_protocol.getStringValue());
		nodeInfo.setIpmiLevel(HinemosPropertyCommon.xcloud_node_property_ipmi_level.getStringValue());
		
		// WinRM
		nodeInfo.setWinrmUser(HinemosPropertyCommon.xcloud_node_property_winrm_user.getStringValue());
		nodeInfo.setWinrmUserPassword(HinemosPropertyCommon.xcloud_node_property_winrm_userpassword.getStringValue());
		nodeInfo.setWinrmVersion(HinemosPropertyCommon.xcloud_node_property_winrm_version.getStringValue());		
		nodeInfo.setWinrmPort(HinemosPropertyCommon.xcloud_node_property_winrm_port.getIntegerValue());
		nodeInfo.setWinrmProtocol(HinemosPropertyCommon.xcloud_node_property_winrm_protocol.getStringValue());
		nodeInfo.setWinrmTimeout(HinemosPropertyCommon.xcloud_node_property_winrm_timeout.getIntegerValue());
		nodeInfo.setWinrmRetries(HinemosPropertyCommon.xcloud_node_property_winrm_retries.getIntegerValue());
		
		// SSH
		nodeInfo.setSshUser(HinemosPropertyCommon.xcloud_node_property_ssh_user.getStringValue());
		nodeInfo.setSshUserPassword(HinemosPropertyCommon.xcloud_node_property_ssh_userpassword.getStringValue());
		nodeInfo.setSshPrivateKeyFilepath(HinemosPropertyCommon.xcloud_node_property_ssh_privkey_path.getStringValue());
		nodeInfo.setSshPrivateKeyPassphrase(HinemosPropertyCommon.xcloud_node_property_ssh_privkey_passphrase.getStringValue());
		nodeInfo.setSshPort(HinemosPropertyCommon.xcloud_node_property_ssh_port.getIntegerValue());
		nodeInfo.setSshTimeout(HinemosPropertyCommon.xcloud_node_property_ssh_timeout.getIntegerValue());

		// xcloud
		nodeInfo.setCloudLogPriority(HinemosPropertyCommon.xcloud_node_property_cloudlog_priority.getIntegerValue());
		
		// 保守
		nodeInfo.setAdministrator(HinemosPropertyCommon.xcloud_node_property_administrator.getStringValue());
		nodeInfo.setContact(HinemosPropertyCommon.xcloud_node_property_contact.getStringValue());

		// マルチテナント設定
		Singletons.get(MultiTenantSupport.class).replaceNodeProperties(nodeInfo);
	}

	public static String getFacilityName(RepositoryControllerBean repositoryController, String facilityId) {
		try {
			NodeInfo nodeInfo = repositoryController.getNode(facilityId);
			return nodeInfo.getFacilityName();
		}
		catch(Exception e) {
			Logger logger = Logger.getLogger(CloudUtil.class);
			logger.error("Not got facilityId (" + facilityId + ")");
		}

		return null;
	}

	public static String getFacilityName(String facilityId) {
		try {
			return getFacilityName(new RepositoryControllerBean(), facilityId);
		}
		catch(Exception e) {
			Logger logger = Logger.getLogger(CloudUtil.class);
			logger.error("Not got facilityId (" + facilityId + ")");
		}

		return null;
	}

	public static JobTreeItem searchJobTreeItem(JobTreeItem treeItem, String jobunitId, String id) {
		if (treeItem.getData().getJobunitId().equals(jobunitId) && treeItem.getData().getId().equals(id)) {
			return treeItem;
		}
		for (JobTreeItem child: treeItem.getChildren()) {
			// Hinemos から取得した情報には、親が設定されていないので、むりやりここで代入。
			child.setParent(treeItem);

			JobTreeItem result = searchJobTreeItem(child, jobunitId, id);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public static void relateJobTreeItems(JobTreeItem parent, JobTreeItem child) {
		child.setParent(parent);
		child.getData().setJobunitId(parent.getData().getJobunitId());
		if (parent.getChildren() == null) {
			parent.setChildren(new ArrayList<JobTreeItem>());
		}
		parent.getChildren().add(child);
	}

	public static final String MESSAGE_ID_INFO = "001";
	public static final String MESSAGE_ID_WARNING = "002";
	public static final String MESSAGE_ID_CRITICAL = "003";
	public static final String MESSAGE_ID_UNKNOWN = "100";
	public static final String MESSAGE_ID_FAILURE = "200";

	public static enum Priority {
		INFO(PriorityConstant.TYPE_INFO, MESSAGE_ID_INFO),
		WARNING(PriorityConstant.TYPE_WARNING, MESSAGE_ID_WARNING),
		CRITICAL(PriorityConstant.TYPE_CRITICAL, MESSAGE_ID_CRITICAL),
		UNKNOWN(PriorityConstant.TYPE_UNKNOWN, MESSAGE_ID_UNKNOWN),
		FAILURE(PriorityConstant.TYPE_FAILURE, MESSAGE_ID_FAILURE),
		;

		private Priority(int type, String messageId) {
			this.type = type;
			this.messageId = messageId;
		}

		public final int type;
		public final String messageId;

		public static Priority priority(int type) {
			for (Priority p: values()) {
				if (p.type == type) {
					return p;
				}
			}
			return null;
		}
	}

	public static OutputBasicInfo createOutputBasicInfo(
			Priority priority,
			String pluginId,
			String monitorId,
			String subKey,
			String application,
			String facilityId,
			String facilityPath,
			String message,
			String messageOrg,
			Long generationDate) {
		OutputBasicInfo output = new OutputBasicInfo();

		// 通知情報を設定
		output.setPluginId(pluginId);
		output.setMonitorId(monitorId);
		output.setApplication(application);

		// 通知抑制用のサブキーを設定。
		output.setSubKey(subKey);

		output.setFacilityId(facilityId);

		if (facilityPath == null) {
			try {
				facilityPath = RepositoryControllerBeanWrapper.bean().getFacilityPath(facilityId, null);
			}
			catch (Exception e) {
				Logger.getLogger(CloudUtil.class).error(e.getMessage(), e);
			}
		}
		output.setScopeText(facilityPath);

		output.setPriority(priority.type);
		//output.setMessageId(priority.messageId);
		output.setMessage(message);
		output.setMessageOrg(messageOrg);
		output.setGenerationDate(generationDate);

		return output;
	}

	public static OutputBasicInfo createOutputBasicInfoEx(
			Priority priority,
			String pluginId,
			String monitorId,
			String subKey,
			String application,
			String facilityId,
			String message,
			String messageOrg,
			Long generationDate) {
		return createOutputBasicInfo(
				priority,
				pluginId,
				monitorId,
				subKey,
				application,
				facilityId,
				null,
				message,
				messageOrg,
				generationDate);
	}
	
	public static void notifyInternalMessage (
			InternalIdAbstract internalId,
			String[] args,
			String detailMsg) {
		AplLogger.put(internalId, args, detailMsg);
	}
	

	public static void notifyInternalMessage (
			InternalIdAbstract internalId,
			String detailMsg) {
		AplLogger.put(internalId, internalId.getMessage(), detailMsg);
	}

	public static void notifyInternalMessage (Exception exception) {
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		pw.flush();
		String messageOrg = sw.toString();
		
		String[] args = {exception.getMessage()};
		if (exception instanceof CloudManagerException
			&& ((CloudManagerException)exception).getInternalId() != null) {
			InternalIdAbstract internalId = ((CloudManagerException)exception).getInternalId();
			AplLogger.put(internalId, args, messageOrg);
		} else {
			AplLogger.put(InternalIdCloud.CLOUD_SYS_099, args, messageOrg);
		}
	}
	
	public static class ObjectPriviledgeOperator {
		private List<ObjectPrivilegeInfo> privileges;
		private String objectType;
		private String objectId;
		private AccessControllerBean accessBean;
		
		public ObjectPriviledgeOperator(AccessControllerBean accessBean, String objectType, String objectId) throws HinemosUnknown {
			this.accessBean = accessBean;
			this.objectType = objectType;
			this.objectId = objectId;
			
			ObjectPrivilegeFilterInfo filter = new ObjectPrivilegeFilterInfo();
			filter.setObjectId(this.objectId);
			filter.setObjectType(this.objectType);
			privileges = accessBean.getObjectPrivilegeInfoList(filter);
		}
		
		public ObjectPriviledgeOperator addFullRightToObject(String roleId) {
			return addRightToObject(roleId, ObjectPrivilegeMode.READ, ObjectPrivilegeMode.MODIFY, ObjectPrivilegeMode.EXEC);
		}

		public ObjectPriviledgeOperator removeFullRightFromObject(String roleId) {
			return removeRightFromObject(roleId, ObjectPrivilegeMode.READ, ObjectPrivilegeMode.MODIFY, ObjectPrivilegeMode.EXEC);
		}

		public ObjectPriviledgeOperator addRightToObject(String roleId, ObjectPrivilegeMode...modes) {
			Set<ObjectPrivilegeInfo> addingPrivileges = new TreeSet<>(new Comparator<ObjectPrivilegeInfo>() {
				@Override
				public int compare(ObjectPrivilegeInfo o1, ObjectPrivilegeInfo o2) {
					return o1.getObjectPrivilege().compareTo(o2.getObjectPrivilege());
				}
			});
			for (ObjectPrivilegeMode mode: modes) {
				ObjectPrivilegeInfo objectPrivilegeInfo = new ObjectPrivilegeInfo();
				objectPrivilegeInfo.setObjectId(objectId);
				objectPrivilegeInfo.setObjectType(objectType);
				objectPrivilegeInfo.setRoleId(roleId);
				objectPrivilegeInfo.setObjectPrivilege(mode.name());
				addingPrivileges.add(objectPrivilegeInfo);
			}
			for (ObjectPrivilegeInfo priviledge: privileges) {
				for (Iterator<ObjectPrivilegeInfo> iter = addingPrivileges.iterator(); iter.hasNext();) {
					ObjectPrivilegeInfo adding = iter.next();
					if (priviledge.getObjectPrivilege().equals(adding.getObjectPrivilege()) && priviledge.getRoleId().equals(adding.getRoleId())) {
						iter.remove();
						break;
					}
				}
			}
			privileges.addAll(addingPrivileges);
			return this;
		}

		public ObjectPriviledgeOperator removeRightFromObject(String roleId, ObjectPrivilegeMode...modes) {
			for (Iterator<ObjectPrivilegeInfo> iter = privileges.iterator(); iter.hasNext();) {
				ObjectPrivilegeInfo priviledge = iter.next();
				if (priviledge.getRoleId().equals(roleId) && ObjectPrivilegeMode.valueOf(priviledge.getObjectPrivilege()) != null) {
					iter.remove();
				}
			}
			return this;
		}
		
		public void commit() throws PrivilegeDuplicate, UsedObjectPrivilege, HinemosUnknown, InvalidSetting, InvalidRole, JobMasterNotFound {
			accessBean.replaceObjectPrivilegeInfo(objectType, objectId, privileges);
		}
	}
	
	public static void addFullRightToObject(AccessControllerBean accessBean, String roleId, String objectType, String objectId) throws PrivilegeDuplicate, UsedObjectPrivilege, HinemosUnknown, InvalidSetting, InvalidRole, JobMasterNotFound {
		new ObjectPriviledgeOperator(accessBean, objectType, objectId).addFullRightToObject(roleId).commit();
	}

	public static void removeFullRightFromObject(AccessControllerBean accessBean, String roleId, String objectType, String objectId) throws PrivilegeDuplicate, UsedObjectPrivilege, HinemosUnknown, InvalidSetting, InvalidRole, JobMasterNotFound {
		new ObjectPriviledgeOperator(accessBean, objectType, objectId).removeFullRightFromObject(roleId).commit();
	}

	public static void addRightToObject(AccessControllerBean accessBean, String roleId, String objectType, String objectId, ObjectPrivilegeMode...modes) throws PrivilegeDuplicate, UsedObjectPrivilege, HinemosUnknown, InvalidSetting, InvalidRole, JobMasterNotFound {
		new ObjectPriviledgeOperator(accessBean, objectType, objectId).addRightToObject(roleId, modes).commit();
	}

	public static void removeRightFromObject(AccessControllerBean accessBean, String roleId, String objectType, String objectId, ObjectPrivilegeMode...modes) throws PrivilegeDuplicate, UsedObjectPrivilege, HinemosUnknown, InvalidSetting, InvalidRole, JobMasterNotFound {
		new ObjectPriviledgeOperator(accessBean, objectType, objectId).removeRightFromObject(roleId, modes).commit();
	}
	
	public static JobInfo createJobInfo(String jobId, int type, String name, String description, String roleId) {
		JobInfo info = new JobInfo();
		info.setId(jobId);
		info.setType(type);

		info.setName(name);
		info.setDescription(description);

		ArrayList<JobEndStatusInfo> statuses = new ArrayList<>();
		JobEndStatusInfo status0 = new JobEndStatusInfo();
		status0.setType(EndStatusConstant.TYPE_NORMAL);
		status0.setValue(EndStatusConstant.INITIAL_VALUE_NORMAL);
		status0.setStartRangeValue(0);
		status0.setEndRangeValue(0);
		statuses.add(status0);
		JobEndStatusInfo status1 = new JobEndStatusInfo();
		status1.setType(EndStatusConstant.TYPE_WARNING);
		status1.setValue(EndStatusConstant.INITIAL_VALUE_WARNING);
		status1.setStartRangeValue(1);
		status1.setEndRangeValue(1);
		statuses.add(status1);
		JobEndStatusInfo status2 = new JobEndStatusInfo();
		status2.setType(EndStatusConstant.TYPE_ABNORMAL);
		status2.setValue(EndStatusConstant.INITIAL_VALUE_ABNORMAL);
		status2.setStartRangeValue(0);
		status2.setEndRangeValue(0);
		statuses.add(status2);
		info.setEndStatus(statuses);
		
		info.setBeginPriority(PriorityConstant.TYPE_CRITICAL);
		info.setNormalPriority(PriorityConstant.TYPE_CRITICAL);
		info.setWarnPriority(PriorityConstant.TYPE_CRITICAL);
		info.setAbnormalPriority(PriorityConstant.TYPE_CRITICAL);
		info.setOwnerRoleId(roleId);

		info.setPropertyFull(true);

		return info;
	}

	public static JobTreeItem createJobnet(String jobnetId, String name, String description, String roleId) {
		JobInfo info = createJobInfo(jobnetId, JobConstant.TYPE_JOBNET, name, description, roleId);
		setJobWaitRuleInfo(info);

		JobTreeItem jobnet = new JobTreeItem();
		jobnet.setData(info);

		return jobnet;
	}

	public static JobTreeItem createJob(RepositoryControllerBean repositoryBean, String jobId, String name, String description, String facilityId, String command, String roleId) throws HinemosUnknown {
		JobInfo info = createJobInfo(jobId, JobConstant.TYPE_JOB, name, description, roleId);
		setJobWaitRuleInfo(info);

		JobCommandInfo jci = new JobCommandInfo();
		info.setCommand(jci);

		jci.setFacilityID(facilityId);
		jci.setScope(repositoryBean.getFacilityPath(facilityId, null));
		jci.setStartCommand(command);
		jci.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);
		jci.setSpecifyUser(false);//YesNoConstant.TYPE_NO
		jci.setProcessingMethod(ProcessingMethodConstant.TYPE_ALL_NODE);
		jci.setMessageRetry(10);

		JobTreeItem job = new JobTreeItem();
		job.setData(info);

		return job;
	}

	public static void setJobWaitRuleInfo(JobInfo jobInfo) {
		JobWaitRuleInfo jobWaitRuleInfo = new JobWaitRuleInfo();

		// 待ち条件タブ
		jobWaitRuleInfo.setCondition(ConditionTypeConstant.TYPE_AND);
		jobWaitRuleInfo.setEndCondition(true);//YesNoConstant.TYPE_YES
		jobWaitRuleInfo.setEndStatus(EndStatusConstant.TYPE_ABNORMAL);
		jobWaitRuleInfo.setEndValue(EndStatusConstant.INITIAL_VALUE_ABNORMAL);

		// 制御タブ
		jobWaitRuleInfo.setCalendar(false);//YesNoConstant.TYPE_NO
		jobWaitRuleInfo.setCalendarEndStatus(EndStatusConstant.TYPE_ABNORMAL);
		jobWaitRuleInfo.setCalendarEndValue(EndStatusConstant.INITIAL_VALUE_NORMAL);
		jobWaitRuleInfo.setSuspend(false);//YesNoConstant.TYPE_NO
		jobWaitRuleInfo.setSkip(false);//YesNoConstant.TYPE_NO
		jobWaitRuleInfo.setSkipEndStatus(EndStatusConstant.TYPE_ABNORMAL);
		jobWaitRuleInfo.setSkipEndValue(EndStatusConstant.INITIAL_VALUE_NORMAL);

		// 開始遅延タブ
		jobWaitRuleInfo.setStart_delay(false);//YesNoConstant.TYPE_NO
		jobWaitRuleInfo.setStart_delay_condition_type(ConditionTypeConstant.TYPE_AND);
		jobWaitRuleInfo.setStart_delay_notify(false);//YesNoConstant.TYPE_NO
		jobWaitRuleInfo.setStart_delay_notify_priority(PriorityConstant.TYPE_CRITICAL);
		jobWaitRuleInfo.setStart_delay_operation_type(OperationConstant.TYPE_STOP_AT_ONCE);
		jobWaitRuleInfo.setStart_delay_operation(false);//YesNoConstant.TYPE_NO
		jobWaitRuleInfo.setStart_delay_operation_end_status(EndStatusConstant.TYPE_ABNORMAL);
		jobWaitRuleInfo.setStart_delay_operation_end_value(EndStatusConstant.INITIAL_VALUE_ABNORMAL);
		jobWaitRuleInfo.setStart_delay_session(false);//YesNoConstant.TYPE_NO
		jobWaitRuleInfo.setStart_delay_session_value(1);
		jobWaitRuleInfo.setStart_delay_time(false);//YesNoConstant.TYPE_NO

		// 終了遅延タブ
		jobWaitRuleInfo.setEnd_delay(false);//YesNoConstant.TYPE_NO
		jobWaitRuleInfo.setEnd_delay_condition_type(ConditionTypeConstant.TYPE_AND);
		jobWaitRuleInfo.setEnd_delay_job(false);//YesNoConstant.TYPE_NO
		jobWaitRuleInfo.setEnd_delay_job_value(1);
		jobWaitRuleInfo.setEnd_delay_notify(false);//YesNoConstant.TYPE_NO
		jobWaitRuleInfo.setEnd_delay_notify_priority(PriorityConstant.TYPE_CRITICAL);
		jobWaitRuleInfo.setEnd_delay_operation(false);//YesNoConstant.TYPE_NO
		jobWaitRuleInfo.setEnd_delay_operation_end_status(EndStatusConstant.TYPE_ABNORMAL);
		jobWaitRuleInfo.setEnd_delay_operation_end_value(EndStatusConstant.INITIAL_VALUE_ABNORMAL);
		jobWaitRuleInfo.setEnd_delay_operation_type(OperationConstant.TYPE_STOP_AT_ONCE);
		jobWaitRuleInfo.setEnd_delay_session(false);//YesNoConstant.TYPE_NO
		jobWaitRuleInfo.setEnd_delay_session_value(1);
		jobWaitRuleInfo.setEnd_delay_time(false);//YesNoConstant.TYPE_NO

		// 多重度タブ
		jobWaitRuleInfo.setMultiplicityEndValue(EndStatusConstant.INITIAL_VALUE_ABNORMAL);
		jobWaitRuleInfo.setMultiplicityNotify(true);//YesNoConstant.TYPE_YES
		jobWaitRuleInfo.setMultiplicityNotifyPriority(PriorityConstant.TYPE_WARNING);
		jobWaitRuleInfo.setMultiplicityOperation(StatusConstant.TYPE_WAIT);

		jobInfo.setWaitRule(jobWaitRuleInfo);
	}
	
	public interface IFacilityTreeVisitor {
		void visitScope(FacilityTreeItem parent, FacilityTreeItem item);
		void visitNode(FacilityTreeItem parent, FacilityTreeItem item);
	}
	
	public static void walkFacilityTree(FacilityTreeItem parent, FacilityTreeItem treeItem, IFacilityTreeVisitor visitor) {
		switch (treeItem.getData().getFacilityType()) {
		case FacilityConstant.TYPE_NODE:
			visitor.visitNode(parent, treeItem);
			break;
		case FacilityConstant.TYPE_SCOPE:
			visitor.visitScope(parent, treeItem);
			break;
		default:
			break;
		}
		for (FacilityTreeItem child: treeItem.getChildren()) {
			walkFacilityTree(parent, child, visitor);
		}
	}
	
	public static List<FacilityTreeItem> collectScopes(FacilityTreeItem treeItem, String...targetIds) {
		return recursiveCollectScopes(treeItem, new ArrayList<String>(Arrays.asList(targetIds)), new ArrayList<FacilityTreeItem>());
	}
	
	private static List<FacilityTreeItem> recursiveCollectScopes(FacilityTreeItem treeItem, List<String> targetIds, List<FacilityTreeItem> buf) {
		if (targetIds.contains(treeItem.getData().getFacilityId()) && treeItem.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE) {
			targetIds.remove(treeItem.getData().getFacilityId());
			buf.add(treeItem);
		} else {
			for (FacilityTreeItem fti: treeItem.getChildren()) {
				recursiveCollectScopes(fti, targetIds, buf);
				if (targetIds.isEmpty())
					break;
			}
		}
		return buf;
	}
	
	public static FacilityTreeItem searchFacility(FacilityTreeItem treeItem, String facilityId) {
		if (facilityId.equals(treeItem.getData().getFacilityId())) {
			return treeItem;
		} else {
			for (FacilityTreeItem child: treeItem.getChildren()) {
				FacilityTreeItem match = searchFacility(child, facilityId);
				if (match != null) {
					return match;
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> emptyList(Class<T> clazz) {
		return (List<T>)Collections.EMPTY_LIST;
	}
	
	public static String createAbsoluteFilePath(String relativePath) {
		String hinemosHome = System.getProperty("hinemos.manager.home.dir");
		return hinemosHome + (hinemosHome.endsWith("/") ? "": "/") + relativePath;
	}
	
	private static boolean checkInfoRange(Map<Integer, MonitorJudgementInfo> judgements, double value) {
		MonitorJudgementInfo info = judgements.get(Integer.valueOf(PriorityConstant.TYPE_INFO));
		return info.getThresholdLowerLimit() <= value && value < info.getThresholdUpperLimit();
	}
	
	private static boolean checkWarnRange(Map<Integer, MonitorJudgementInfo> judgements, double value) {
		MonitorJudgementInfo warn = judgements.get(Integer.valueOf(PriorityConstant.TYPE_WARNING));
		return warn.getThresholdLowerLimit() <= value && value < warn.getThresholdUpperLimit();
	}
	
	private static boolean checkCriticalRange(Map<Integer, MonitorJudgementInfo> judgements, double value) {
		MonitorJudgementInfo warn = judgements.get(Integer.valueOf(PriorityConstant.TYPE_WARNING));
		return warn.getThresholdUpperLimit() <= value;
	}

	public static CloudUtil.Priority checkPriorityRange(Map<Integer, MonitorJudgementInfo> judgements, double value) {
		return checkInfoRange(judgements, value) ? 
				CloudUtil.Priority.INFO: (checkWarnRange(judgements, value) ? 
						CloudUtil.Priority.WARNING: (checkCriticalRange(judgements, value) ? 
								CloudUtil.Priority.CRITICAL: CloudUtil.Priority.UNKNOWN));
	}
	
	public static String getStackTrace(Thread t){
		StackTraceElement[] eList = t.getStackTrace();
		StringBuilder trace = new StringBuilder();
		for (StackTraceElement e : eList) {
			trace.append("\n\tat ");
			trace.append(e.getClassName() + "." + e.getMethodName() + "(" + e.getFileName() + ":" + e.getLineNumber() + ")");
		}
		return trace.toString();
	}

	/**
	 * 文字列を指定された桁数にカットする
	 * 
	 * @param str
	 * @param length
	 * @return 
	 */
	public static String truncateString(String str, int length) {
		if (str != null && str.length() > length) {
			logger.debug("truncateString() : length=" + length + ", original string=" + str);
			return str.substring(0, length);
		} else {
			return str;
		}
	}
	
	/**
	 * リソース更新時に使用されるロックを取得する
	 * @param className
	 * @param cloudScopeId
	 * @param locationId
	 * @return
	 */
	public static ILock getLock(String className, String cloudScopeId, String locationId) {
		ILockManager lm = LockManagerFactory.instance().create();
		return lm.create(getLockKey(className, cloudScopeId, locationId));
	}

	private static String getLockKey(String className, String cloudScopeId, String locationId) {
		return String.format("%s [%s, %s]", className, cloudScopeId, locationId);
	}

	/**
	 * 指定したファシリティIDのノードの管理対象フラグを更新する
	 * @param facilityId
	 * @param newValue
	 */
	public static void updateValidFlg(String facilityId, Boolean newValue) {
		try {
			RepositoryControllerBean repositoryControllerBean = RepositoryControllerBeanWrapper.bean();
			NodeInfo nodeInfo = repositoryControllerBean.getNode(facilityId);
			Boolean oldValue = nodeInfo.getValid();
			if (!newValue.equals(oldValue)) {
				nodeInfo.setValid(newValue);
				repositoryControllerBean.modifyNode(nodeInfo);
				logger.info(String.format("updateValidFlg(): FacilityID=%s, ValidFlg:%s->%s", facilityId, oldValue, newValue));
			}
		} catch (InvalidSetting | InvalidRole | FacilityNotFound | HinemosUnknown e) {
			ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
		}
	}
}
