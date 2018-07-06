/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.common;

import static com.clustercontrol.xcloud.common.CloudConstants.bundle_messages;

public interface CloudStringConstants {
	static final String strSeparator = bundle_messages.getString("caption.title_separator");
	static final String strManager = bundle_messages.getString("word.manager");
	static final String strCloudScope = bundle_messages.getString("word.cloudscope");
	static final String strNetwork = bundle_messages.getString("word.network");
	static final String strNetworkId = bundle_messages.getString("word.network.id");
	static final String strNetworkName = bundle_messages.getString("word.network.name");
	static final String strNetworkInterface = bundle_messages.getString("word.network_interface");
	
	static final String strAccount = bundle_messages.getString("word.account");
	static final String strAccountId = bundle_messages.getString("word.account.id");
	static final String strDisplayName = bundle_messages.getString("word.display.name");

	static final String strCompute = bundle_messages.getString("word.compute");
	static final String strComputeId = bundle_messages.getString("word.compute.id");
	static final String strComputeName = bundle_messages.getString("word.compute.name");
	
	static final String strStorage = bundle_messages.getString("word.storage");
	static final String strStorageId = bundle_messages.getString("word.storage.id");
	static final String strStorageName = bundle_messages.getString("word.storage.name");
	
	static final String strCloudScopeId = bundle_messages.getString("word.cloudscope.id");
	static final String strCloudScopeName = bundle_messages.getString("word.cloudscope.name");
	static final String strOwnerRole = bundle_messages.getString("word.owner_role");

	static final String strDescription = bundle_messages.getString("word.description");
	
	static final String strBrowse = bundle_messages.getString("word.browse");
	static final String strAdvance = bundle_messages.getString("word.advance");
	
	static final String strSnapshotId = bundle_messages.getString("word.snapshot.id");
	static final String strSnapshotName = bundle_messages.getString("word.snapshot.name");
	
	static final String strCreateDate = bundle_messages.getString("word.create_date");
	
	static final String strAdminAccount = bundle_messages.getString("word.adminaccount");
	
	static final String strMemo = bundle_messages.getString("word.memo");
	static final String strTag = bundle_messages.getString("word.tag");
	static final String strAttribute = bundle_messages.getString("word.attribute");
	static final String strType = bundle_messages.getString("word.type");

	static final String strAdd = bundle_messages.getString("word.add");
	static final String strDelete = bundle_messages.getString("word.delete");
	
	static final String strName = bundle_messages.getString("word.name");
	static final String strValue = bundle_messages.getString("word.value");
	
	static final String strCloudService = bundle_messages.getString("word.cloud_service");
	static final String strCloudServiceName = bundle_messages.getString("word.cloud_service.name");

	static final String strState = bundle_messages.getString("word.state");
	static final String strStateDetail = bundle_messages.getString("word.state_detail");
	static final String strCurrentState = bundle_messages.getString("word.current_state");
	
	static final String strFacilityId = bundle_messages.getString("word.facility.id");
	static final String strFacilityName = bundle_messages.getString("word.facility.name");
	static final String strFacilityPath = bundle_messages.getString("word.facility.path");
	
	static final String strAuto = bundle_messages.getString("word.auto");
	static final String strCloud = bundle_messages.getString("word.cloud");
	static final String strLocal = bundle_messages.getString("word.local");
	
	static final String strIpAddress = bundle_messages.getString("word.ip_address");
	
	static final String strArrowUp = bundle_messages.getString("word.arrow.up");
	static final String strArrowDown = bundle_messages.getString("word.arrow.down");
	
	static final String strScope = bundle_messages.getString("word.scope");
	static final String strPriority = bundle_messages.getString("word.priority");
	static final String strEvaluteItem = bundle_messages.getString("word.evaluate_item");
	static final String strMatchingCondition = bundle_messages.getString("word.matching_condition");
	
	static final String strRole = bundle_messages.getString("word.role");
	static final String strCloudPlatform = bundle_messages.getString("word.cloudplatform");
	static final String strAssignedUser = bundle_messages.getString("word.assigned_account");
	static final String strUnassgin = bundle_messages.getString("word.unassign");
	static final String strMain = bundle_messages.getString("word.main");
	
	static final String strDetail = bundle_messages.getString("word.detail");
	
	static final String strCreate = bundle_messages.getString("command.create");
	static final String strClone = bundle_messages.getString("command.clone");
	static final String strAttach = bundle_messages.getString("command.attach");
	static final String strDetach = bundle_messages.getString("command.detach");
	static final String strModify = bundle_messages.getString("command.edit");
	static final String strEditConfiguration = bundle_messages.getString("command.edit_configuration");
	static final String strMigration = bundle_messages.getString("command.migration");
	static final String strSnapshot = bundle_messages.getString("command.snapshot");
	static final String strAssignScopeRule = bundle_messages.getString("command.assign_scope_rule");
	static final String strShowDetail = bundle_messages.getString("command.show_detail");
	static final String strPowerOn = bundle_messages.getString("command.poweron");
	static final String strPowerOff = bundle_messages.getString("command.poweroff");
	static final String strReboot = bundle_messages.getString("command.reboot");
	static final String strSuspend = bundle_messages.getString("command.suspend");
	static final String strSubAccountSetting = bundle_messages.getString("word.account.sub.setting");
	static final String strSubAccountAutoInput = bundle_messages.getString("word.account.sub.auto_input");
	
	static final String strResigtModify = bundle_messages.getString("word.resigt_modify");

	static final String dlgLoginUser = bundle_messages.getString("view.login_user");
	static final String dlgLoginuserRegistModify = String.format("%s - %s ({0})", dlgLoginUser, strResigtModify);
	static final String dlgLoginuserDelete = String.format("%s - %s ({0})", dlgLoginUser, strDelete);
	static final String dlgLoginuserSubAccountSetting = String.format("%s - %s ({0})", dlgLoginUser, strSubAccountSetting);
	static final String dlgLoginuserSubAccountAutoInput = String.format("%s - %s ({0})", dlgLoginUser, strSubAccountAutoInput);
	
	static final String dlgCompute = bundle_messages.getString("view.compute");
	static final String dlgComputeCreate = String.format("%s - %s ({0})", dlgCompute, strCreate);
	static final String dlgComputeClone = String.format("%s - %s ({0})", dlgCompute, strClone);
	static final String dlgComputeEditCofiguration = String.format("%s - %s ({0})", dlgCompute, strEditConfiguration);
	static final String dlgComputeMigration = String.format("%s - %s ({0})", dlgCompute, strMigration);
	static final String dlgComputeShowDetail = String.format("%s - %s ({0})", dlgCompute, strShowDetail);
	static final String dlgComputeSnapshot = String.format("%s - %s ({0})", dlgCompute, strSnapshot);
	static final String dlgComputeAssignScopeRule = String.format("%s - %s", dlgCompute, strAssignScopeRule);
	static final String dlgComputePowerOn = String.format("%s - %s", dlgCompute, strPowerOn);
	static final String dlgComputePowerOff = String.format("%s - %s", dlgCompute, strPowerOff);
	static final String dlgComputeReboot = String.format("%s - %s", dlgCompute, strReboot);
	static final String dlgComputeSuspend = String.format("%s - %s", dlgCompute, strSuspend);
	
	static final String dlgStorage = bundle_messages.getString("view.storage");
	static final String dlgStorageCreate = String.format("%s - %s ({0})", dlgStorage, strCreate);
	static final String dlgStorageClone = String.format("%s - %s ({0})", dlgStorage, strClone);
	static final String dlgStorageAttach = String.format("%s - %s ({0})", dlgStorage, strAttach);
	static final String dlgStrageDetach = String.format("%s - %s ({0})", dlgStorage, strDetach);
	static final String dlgStrageMigrate = String.format("%s - %s ({0})", dlgStorage, strMigration);
	static final String dlgStorageSnapshot = String.format("%s - %s ({0})", dlgStorage, strSnapshot);
	
	static final String dlgComputeHistry = bundle_messages.getString("view.compute.history");
	static final String dlgComputeHistryClone = String.format("%s - %s ({0})", dlgComputeHistry, strClone);

	static final String dlgStorageHistry = bundle_messages.getString("view.storage.history");
	static final String dlgStorageHistryClone = String.format("%s - %s ({0})", dlgStorageHistry, strClone);

	static final String dlgServiceState = bundle_messages.getString("view.service.state");
	
	static final String dlgRole = bundle_messages.getString("view.role");
	static final String dlgRoleAddModify = String.format("%s - %s", dlgRole, strResigtModify);

	static final String dlgNetwork = bundle_messages.getString("view.network");
	static final String dlgNetworkAttach = String.format("%s - %s ({0})", dlgNetwork, strAttach);
	static final String dlgNetworkDetach = String.format("%s - %s ({0})", dlgNetwork, strDetach);
	
	static final String dlgBillingDetailCollectSetting = bundle_messages.getString("caption.billing_detail_collect_setting_dialog");
	
	static final String dlgCloudServiceCreateModify = bundle_messages.getString("caption.cloud_service_create_modify");
	
	static final String strDetailTypeScope = bundle_messages.getString("word.billing.detail.type.scope");
	static final String strDetailTypeNode = bundle_messages.getString("word.billing.detail.type.node");
	static final String strDetailKindSum = bundle_messages.getString("word.billing.detail.kind.sum");
	static final String strDetailKindDelta = bundle_messages.getString("word.billing.detail.kind.delta");

	static final String strKeyword = bundle_messages.getString("word.keyword");
	
	static final String strRetentionPeriod = bundle_messages.getString("word.retention_period");
	static final String strDays = bundle_messages.getString("word.days");
	static final String strEnableBillingDetailCollection = bundle_messages.getString("caption.enable_billing_detail_collection");
	
	static final String strRegUser = bundle_messages.getString("word.reg_user");
	static final String strRegDate = bundle_messages.getString("word.reg_date");
	static final String strUpdateUser = bundle_messages.getString("word.update_user");
	static final String strUpdateDate = bundle_messages.getString("word.update_date");
	static final String strViewItemCount = bundle_messages.getString("word.view_item_count");
	
	static final String strNormal = bundle_messages.getString("word.normal");
	static final String strWarn = bundle_messages.getString("word.warn");
	static final String strError = bundle_messages.getString("word.error");
	static final String strUnknown = bundle_messages.getString("word.unknown");
	
	static final String strMonitorCloudServiceCondition = bundle_messages.getString("monitor.cloudservice.condition.monitor");
	static final String strMonitorCloudServiceBilling = bundle_messages.getString("monitor.cloudservice.billing.monitor");
	static final String strMonitorCloudServiceBillingDetail = bundle_messages.getString("monitor.cloudservice.billing.detail.monitor");
	
	static final String msgTagKeyFromOneChar = bundle_messages.getString("message.tag.key.from_one_char");
	static final String msgTagKeyDuplicate = bundle_messages.getString("message.tag.key.duplicate");
	static final String msgFucNotAvailable = bundle_messages.getString("message.community_edition.func.not_available");
	static final String msgFucUnSupported = bundle_messages.getString("message.unsupported.func.not_avaiable");
	
	static final String msgConfirmCreateCloudScope = bundle_messages.getString("message.confirm.cloudscope.create");
	static final String msgFinishCreateCloudScope = bundle_messages.getString("message.finish.cloudscope.create");
	static final String msgErrorFinishCreateCloudScope = bundle_messages.getString("message.error.finish.cloudscope.create");

	static final String msgConfirmDeleteCloudScope = bundle_messages.getString("message.confirm.cloudscope.delete");
	static final String msgFinishDeleteCloudScope = bundle_messages.getString("message.finish.cloudscope.delete");
	static final String msgErrorFinishDeleteCloudScope = bundle_messages.getString("message.error.finish.cloudscope.delete");
	
	static final String msgConfirmCreateComputeNode = bundle_messages.getString("message.confirm.compute.create");
	static final String msgFinishCreateComputeNode = bundle_messages.getString("message.finish.compute.create");
	static final String msgErrorFinishCreateComputeNode = bundle_messages.getString("message.error.finish.compute.create");
	
	static final String msgConfirmPowerOnComputeNode = bundle_messages.getString("message.confirm.compute.poweron");
	static final String msgConfirmPowerOnComputeNodeMulti = bundle_messages.getString("message.confirm.compute.poweron.multi");
	static final String msgFinishPowerOnComputeNode = bundle_messages.getString("message.finish.compute.poweron");
	static final String msgErrorFinishPowerOnComputeNode = bundle_messages.getString("message.error.finish.compute.poweron");

	static final String msgConfirmPowerOffComputeNode = bundle_messages.getString("message.confirm.compute.poweroff");
	static final String msgConfirmPowerOffComputeNodeMulti = bundle_messages.getString("message.confirm.compute.poweroff.multi");
	static final String msgFinishPowerOffComputeNode = bundle_messages.getString("message.finish.compute.poweroff");
	static final String msgErrorFinishPowerOffComputeNode = bundle_messages.getString("message.error.finish.compute.poweroff");
	
	static final String msgConfirmSuspendComputeNode = bundle_messages.getString("message.confirm.compute.suspend");
	static final String msgConfirmSuspendComputeNodeMulti = bundle_messages.getString("message.confirm.compute.suspend.multi");
	static final String msgFinishSuspendComputeNode = bundle_messages.getString("message.finish.compute.suspend");
	static final String msgErrorFinishSuspendComputeNode = bundle_messages.getString("message.error.finish.compute.suspend");

	static final String msgConfirmRebootComputeNode = bundle_messages.getString("message.confirm.compute.reboot");
	static final String msgConfirmRebootComputeNodeMulti = bundle_messages.getString("message.confirm.compute.reboot.multi");
	static final String msgFinishRebootComputeNode = bundle_messages.getString("message.finish.compute.reboot");
	static final String msgErrorFinishRebootComputeNode = bundle_messages.getString("message.error.finish.compute.reboot");
	
	static final String msgConfirmSnapshotCreateComputeNode = bundle_messages.getString("message.confirm.compute.snapshot.create");
	static final String msgFinishSnapshotCreateComputeNode = bundle_messages.getString("message.finish.compute.snapshot.create");
	static final String msgErrorFinishSnapshotCreateComputeNode = bundle_messages.getString("message.error.finish.compute.snapshot.create");
	
	static final String msgSelectJobnetSummary = bundle_messages.getString("message.select.jobnet.summary");
	static final String msgSelectJobnet = bundle_messages.getString("message.select.jobnet");
	
	static final String msgSetingJob = bundle_messages.getString("message.setting.job");
	static final String msgInputJobDetail = bundle_messages.getString("message.input.job.detail");
	
	static final String msgSelectScopeSummary = bundle_messages.getString("message.select.scope.summay");
	static final String msgSelectScope = bundle_messages.getString("message.select.scope");
	
	static final String msgErrorFinishCreateDetachStorageJob = bundle_messages.getString("message.error.finish.detach.strage.job.create");
	
	static final String msgConfirmCloneComputeNode = bundle_messages.getString("message.confirm.compute.clone");
	static final String msgFinishCloneComputeNode = bundle_messages.getString("message.finish.compute.clone");
	static final String msgErrorFinishCloneComputeNode = bundle_messages.getString("message.error.finish.compute.clone");

	static final String msgConfirmMigrateComputeNode = bundle_messages.getString("message.confirm.compute.migrate");
	static final String msgFinishMigrateComputeNode = bundle_messages.getString("message.finish.compute.migrate");
	static final String msgErrorFinishMigrateComputeNode = bundle_messages.getString("message.error.finish.compute.migrate");
	
	static final String strJobId = bundle_messages.getString("word.job.id");
	static final String strJobName = bundle_messages.getString("word.job.name");
	
	static final String msgConfirmDeleteComputeNode = bundle_messages.getString("message.confirm.compute.delete");
	static final String msgConfirmDeleteComputeNodeMulti = bundle_messages.getString("message.confirm.compute.delete.multi");
	static final String msgFinishDeleteComputeNode = bundle_messages.getString("message.finish.compute.delete");
	static final String msgErrorFinishDeleteComputeNode = bundle_messages.getString("message.error.finish.compute.delete");
	
	static final String msgConfirmModifyComputeNode = bundle_messages.getString("message.confirm.compute.modify");
	static final String msgFinishModifyComputeNode = bundle_messages.getString("message.finish.compute.modify");
	static final String msgErrorFinishModifyComputeNode = bundle_messages.getString("message.error.finish.compute.modify");
	
	static final String msgConfirmModifyAutoAssignNodepattern = bundle_messages.getString("message.confirm.auto_assign_nodepattern.modify");
	static final String msgFinishModifyAutoAssignNodepattern = bundle_messages.getString("message.finish.auto_assign_nodepattern.modify");
	static final String msgErrorFinishModifyAutoAssignNodepattern = bundle_messages.getString("message.error.finish.auto_assign_nodepattern.modify");

	static final String msgConfirmManualRegistNodeModify = bundle_messages.getString("message.confirm.manual_regist_node.modify");
	static final String msgConfirmManualRegistNodeModifyMulti = bundle_messages.getString("message.confirm.manual_regist_node.modify.multi");
	static final String msgFinishManualRegistNodeModify = bundle_messages.getString("message.finish.manual_regist_node.modify");
	static final String msgErrorFinishManualRegistNodeModify = bundle_messages.getString("message.error.finish.manual_regist_node.modify");
	
	static final String msgConfirmCloneComputeSnapshot = bundle_messages.getString("message.confirm.compute.snapshot.clone");
	static final String msgFinishCloneComputeSnapshot = bundle_messages.getString("message.finish.compute.snapshot.clone");
	static final String msgErrorFinishCloneComputeSnapshot = bundle_messages.getString("message.error.finish.compute.snapshot.clone");

	static final String msgConfirmDeleteSnapshot = bundle_messages.getString("message.confirm.snapshot.delete");
	static final String msgFinishDeleteSnapshot = bundle_messages.getString("message.finish.snapshot.delete");
	static final String msgErrorFinishDeleteSnapshot = bundle_messages.getString("message.error.finish.snapshot.delete");
	
	static final String msgConfirmhStorageCreate = bundle_messages.getString("message.confirm.storage.create");
	static final String msgFinishStorageCreate = bundle_messages.getString("message.finish.storage.create");
	static final String msgErrorFinishStorageCreate = bundle_messages.getString("message.error.finish.storage.create");

	static final String msgConfirmDeleteStorage = bundle_messages.getString("message.confirm.storage.delete");
	static final String msgConfirmDeleteStorageMulti = bundle_messages.getString("message.confirm.storage.delete.multi");
	static final String msgFinishDeleteStorage = bundle_messages.getString("message.finish.storage.delete");
	static final String msgErrorFinishDeleteStorage = bundle_messages.getString("message.error.finish.storage.delete");
	
	static final String msgConfirmAttachStorage = bundle_messages.getString("message.confirm.storage.attach");
	static final String msgFinishAttachStorage = bundle_messages.getString("message.finish.storage.attach");
	static final String msgErrorFinishAttachStorage = bundle_messages.getString("message.error.finish.storage.attach");

	static final String msgConfirmDetachStorage = bundle_messages.getString("message.confirm.storage.detach");
	static final String msgConfirmDetachStorageMulti = bundle_messages.getString("message.confirm.storage.detach.multi");
	static final String msgFinishDetachStorage = bundle_messages.getString("message.finish.storage.detach");
	static final String msgErrorFinishDetachStorage = bundle_messages.getString("message.error.finish.storage.detach");
	
	static final String msgConfirmSnapshotCreateStorage = bundle_messages.getString("message.confirm.storage.snapshot.create");
	static final String msgFinishSnapshotCreateStorage = bundle_messages.getString("message.finish.storage.snapshot.create");
	static final String msgErrorFinishSnapshotCreateStorage = bundle_messages.getString("message.error.finish.storage.snapshot.create");
	
	static final String msgConfirmMigrateStorage = bundle_messages.getString("message.confirm.storage.migrate");
	static final String msgFinishMigrateStorage = bundle_messages.getString("message.finish.storage.migrate");
	static final String msgErrorFinishMigrateStorage = bundle_messages.getString("message.error.finish.storage.migrate");

	static final String msgConfirmCloneStorageSnapshot = bundle_messages.getString("message.confirm.storage.snapshot.clone");
	static final String msgFinishCloneStorageSnapshot = bundle_messages.getString("message.finish.storage.snapshot.clone");
	static final String msgErrorFinishCloneStorageSnapshot = bundle_messages.getString("message.error.finish.storage.snapshot.clone");
	
	static final String msgConfirmModifyNetworkSetting = bundle_messages.getString("message.confirm.network.setting.modify");
	static final String msgFinishModifyNetworkSetting = bundle_messages.getString("message.finish.network.setting.modify");
	static final String msgErrorFinishModifyNetworkSetting = bundle_messages.getString("message.error.finish.network.setting.modify");

	static final String msgConfirmAttachNetwork = bundle_messages.getString("message.confirm.network.attach");
	static final String msgFinishAttachNetwork = bundle_messages.getString("message.finish.network.attach");
	static final String msgErrorFinishAttachNetwork = bundle_messages.getString("message.error.finish.network.attach");

	static final String msgConfirmDetachNetwork = bundle_messages.getString("message.confirm.network.detach");
	static final String msgFinishDetachNetwork = bundle_messages.getString("message.finish.network.detach");
	static final String msgErrorFinishDetachNetwork = bundle_messages.getString("message.error.finish.network.detach");
	
	static final String strFooterTitle = bundle_messages.getString("word.view_item_count") + bundle_messages.getString("caption.title_separator");
	
	static final String msgConfirmAgentRegist = bundle_messages.getString("message.confirm.compute.agent.regist");
	static final String msgConfirmAgentRegistMulti = bundle_messages.getString("message.confirm.compute.agent.regist.multi");
	static final String msgFinishSuccessfullAgentRegist = bundle_messages.getString("message.finish.successful.compute.agent.regist");
	static final String msgErrorFinishAgentRegist = bundle_messages.getString("message.error.finish.compute.agent.regist");
	
	static final String msgErrorFinishRefreshView = bundle_messages.getString("message.error.finish.refresh.view");
	static final String msgErrorFinishShowBillingDetail= bundle_messages.getString("message.error.finish.show.billing.detail");

	static final String msgErrorFinishExportBillingDetailForCloudScope = bundle_messages.getString("message.finish.error.billing.detail.download.cloudscope");
	static final String msgErrorFinishExportBillingDetailForFacility = bundle_messages.getString("message.finish.error.billing.detail.download.facility");
	static final String msgErrorFinishCreatePowerOffJob = bundle_messages.getString("message.error.finish.power.off.job.create");
	static final String msgErrorFinishCreatePowerOnJob = bundle_messages.getString("message.error.finish.power.on.job.create");
	static final String msgErrorFinishCreateRebootJob = bundle_messages.getString("message.error.finish.reboot.job.create");
	static final String msgErrorFinishCreateSuspendJob = bundle_messages.getString("message.error.finish.suspend.job.create");
	static final String msgErrorFinishCreateSnapshotStorageJob = bundle_messages.getString("message.error.finish.snapshot.storage.job.create");
	static final String msgErrorFinishSnapshotInstanceCreateJob = bundle_messages.getString("message.error.finish.snapshot.instance.job.create");
	
	static final String msgConfirmModifyBillingSetting = bundle_messages.getString("message.confirm.billing_setting.modify");
	static final String msgFinishModifyBillingSetting = bundle_messages.getString("message.finish.billing_setting.modify");
	static final String msgErrorFinishModifyBillingSetting = bundle_messages.getString("message.error.finish.billing_setting.modify");
	static final String msgErrorFinishCreateAttachStorageJob = bundle_messages.getString("message.error.finish.attach.storage.job.create");
	static final String msgConfirmModifyCloudScope = bundle_messages.getString("message.confirm.cloudscope.modify");
	static final String msgFinishModifyCloudScope = bundle_messages.getString("message.finish.cloudscope.modify");
	static final String msgErrorFinishModifyCloudScope = bundle_messages.getString("message.error.finish.cloudscope.modify");
	static final String msgConfirmModifySubAccount = bundle_messages.getString("message.confirm.subaccount.modify");
	static final String msgFinishModifySubAccount = bundle_messages.getString("message.finish.subaccount.modify");
	static final String msgErrorFinishModifySubAccount = bundle_messages.getString("message.error.finish.subaccount.modify");
	static final String msgErrorFinishCreateJob = bundle_messages.getString("message.error.finish.job.regist");
	
	static final String msgErrorGetPlatformServiceConditions = bundle_messages.getString("message.error.get.platformservice_condition");
	static final String msgErrorGetAvailablePlatformUsers = bundle_messages.getString("message.error.get.availableplatform_user");
	
	static final String msgErrorGetUnassignedUsers = bundle_messages.getString("message.error.get.unassigned_users");
	static final String msgErrorGetAvailableRoles = bundle_messages.getString("message.error.get.available_role");
	
	static final String msgErrorGetRepository = bundle_messages.getString("message.error.get.repository");
	static final String msgErrorGetFacilityTree = bundle_messages.getString("message.error.get.facilitytree");
	static final String msgErrorGetAllStorages = bundle_messages.getString("message.error.get.storage");
	static final String msgErrorGetAllNetworks = bundle_messages.getString("message.error.get.network");
	
	static final String msgConfirmModifyRoleRelations = bundle_messages.getString("message.confirm.role_assign.modify");
	static final String msgFinishModifyRoleRelations = bundle_messages.getString("message.finish.role_assign.modify");
	static final String msgErrorFinishModifyRoleRelations = bundle_messages.getString("message.error.finish.role_assign.modify");
	
	static final String msgValidationRequiredInputMessage = bundle_messages.getString("validation.required_input.message");
}
