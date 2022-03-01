/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.infra.util;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AddInfraFileRequest;
import org.openapitools.client.model.AddInfraManagementRequest;
import org.openapitools.client.model.CheckInfraModuleRequest;
import org.openapitools.client.model.CreateAccessInfoListForDialogResponse;
import org.openapitools.client.model.CreateSessionRequest;
import org.openapitools.client.model.InfraCheckResultResponse;
import org.openapitools.client.model.InfraFileInfoResponse;
import org.openapitools.client.model.InfraManagementInfoResponse;
import org.openapitools.client.model.InfraManagementInfoResponseP1;
import org.openapitools.client.model.InfraSessionResponse;
import org.openapitools.client.model.ModifyInfraFileRequest;
import org.openapitools.client.model.ModifyInfraManagementRequest;
import org.openapitools.client.model.ModuleResultResponse;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileBeingUsed;
import com.clustercontrol.fault.InfraFileNotFound;
import com.clustercontrol.fault.InfraFileTooLarge;
import com.clustercontrol.fault.InfraManagementDuplicate;
import com.clustercontrol.fault.InfraManagementInvalid;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InfraModuleNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.SessionNotFound;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class InfraRestClientWrapper {
	private static Log m_log = LogFactory.getLog(InfraRestClientWrapper.class);
	private RestConnectUnit connectUnit;

	private final RestKind restKind = RestKind.InfraRestEndpoints;

	public static InfraRestClientWrapper getWrapper(String managerName) {
		return new InfraRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public InfraRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}

	public InfraManagementInfoResponse addInfraManagement(AddInfraManagementRequest addInfraManagementRequest)
			throws RestConnectFailed, InfraManagementDuplicate, InfraManagementNotFound, NotifyDuplicate,
			InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		RestUrlSequentialExecuter<InfraManagementInfoResponse> proxy = new RestUrlSequentialExecuter<InfraManagementInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public InfraManagementInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				InfraManagementInfoResponse result = apiClient.infraAddInfraManagement(addInfraManagementRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InfraManagementDuplicate | InfraManagementNotFound | NotifyDuplicate
				| InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public InfraManagementInfoResponse modifyInfraManagement(String managementId,
			ModifyInfraManagementRequest modifyInfraManagementRequest)
			throws RestConnectFailed, NotifyDuplicate, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole,
			InvalidSetting, InfraManagementNotFound, InfraManagementDuplicate {
		RestUrlSequentialExecuter<InfraManagementInfoResponse> proxy = new RestUrlSequentialExecuter<InfraManagementInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public InfraManagementInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				InfraManagementInfoResponse result = apiClient.infraModifyInfraManagement(managementId,
						modifyInfraManagementRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyDuplicate | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole
				| InvalidSetting | InfraManagementNotFound | InfraManagementDuplicate def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<InfraManagementInfoResponse> deleteInfraManagement(String managementIds) throws RestConnectFailed,
			HinemosUnknown, InvalidUserPass, InvalidSetting, InvalidRole, InfraManagementNotFound {
		RestUrlSequentialExecuter<List<InfraManagementInfoResponse>> proxy = new RestUrlSequentialExecuter<List<InfraManagementInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<InfraManagementInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<InfraManagementInfoResponse> result = apiClient.infraDeleteInfraManagement(managementIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidSetting | InvalidRole
				| InfraManagementNotFound def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public InfraManagementInfoResponse getInfraManagement(String managementId) throws RestConnectFailed, HinemosUnknown,
			InvalidUserPass, InvalidRole, InfraManagementNotFound, InvalidSetting {
		RestUrlSequentialExecuter<InfraManagementInfoResponse> proxy = new RestUrlSequentialExecuter<InfraManagementInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public InfraManagementInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				InfraManagementInfoResponse result = apiClient.infraGetInfraManagement(managementId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InfraManagementNotFound
				| InvalidSetting def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<InfraManagementInfoResponse> getInfraManagementList(String ownerRoleId)
			throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<InfraManagementInfoResponse>> proxy = new RestUrlSequentialExecuter<List<InfraManagementInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<InfraManagementInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<InfraManagementInfoResponse> result = apiClient.infraGetInfraManagementList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole
				| InvalidSetting def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<InfraManagementInfoResponseP1> getReferManagementList(String ownerRoleId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<InfraManagementInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<InfraManagementInfoResponseP1>>(this.connectUnit, this.restKind) {
			@Override
			public List<InfraManagementInfoResponseP1> executeMethod(DefaultApi apiClient) throws Exception {
				List<InfraManagementInfoResponseP1> result = apiClient.infraGetReferManagementList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public InfraSessionResponse createSession(CreateSessionRequest createSessionRequest)
			throws RestConnectFailed, InfraManagementNotFound, InfraModuleNotFound, FacilityNotFound,
			InfraManagementInvalid, InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<InfraSessionResponse> proxy = new RestUrlSequentialExecuter<InfraSessionResponse>(this.connectUnit, this.restKind) {
			@Override
			public InfraSessionResponse executeMethod(DefaultApi apiClient) throws Exception {
				InfraSessionResponse result = apiClient.infraCreateSession(createSessionRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InfraManagementNotFound | InfraModuleNotFound | FacilityNotFound
				| InfraManagementInvalid | InvalidSetting | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public InfraSessionResponse deleteSession(String sessionId) throws RestConnectFailed, SessionNotFound,
			InfraManagementNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<InfraSessionResponse> proxy = new RestUrlSequentialExecuter<InfraSessionResponse>(this.connectUnit, this.restKind) {
			@Override
			public InfraSessionResponse executeMethod(DefaultApi apiClient) throws Exception {
				InfraSessionResponse result = apiClient.infraDeleteSession(sessionId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | SessionNotFound | InfraManagementNotFound | InvalidUserPass | InvalidRole
				| HinemosUnknown def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CreateAccessInfoListForDialogResponse> createAccessInfoListForDialog(String managementId,
			String moduleIds) throws RestConnectFailed, InfraManagementNotFound, InvalidUserPass, InvalidRole,
			HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<List<CreateAccessInfoListForDialogResponse>> proxy = new RestUrlSequentialExecuter<List<CreateAccessInfoListForDialogResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<CreateAccessInfoListForDialogResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CreateAccessInfoListForDialogResponse> result = apiClient
						.infraCreateAccessInfoListForDialog(managementId, moduleIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InfraManagementNotFound | InvalidUserPass | InvalidRole | HinemosUnknown
				| InvalidSetting def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ModuleResultResponse runInfraModule(String sessionId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InfraManagementNotFound,
			InfraModuleNotFound, SessionNotFound, InvalidSetting {
		RestUrlSequentialExecuter<ModuleResultResponse> proxy = new RestUrlSequentialExecuter<ModuleResultResponse>(this.connectUnit, this.restKind) {
			@Override
			public ModuleResultResponse executeMethod(DefaultApi apiClient) throws Exception {
				ModuleResultResponse result = apiClient.infraRunInfraModule(sessionId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InfraManagementNotFound
				| InfraModuleNotFound | SessionNotFound | InvalidSetting def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ModuleResultResponse checkInfraModule(String sessionId, boolean verbose)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InfraManagementNotFound,
			InfraModuleNotFound, SessionNotFound, InvalidSetting {
		RestUrlSequentialExecuter<ModuleResultResponse> proxy = new RestUrlSequentialExecuter<ModuleResultResponse>(this.connectUnit, this.restKind) {
			@Override
			public ModuleResultResponse executeMethod(DefaultApi apiClient) throws Exception {
				CheckInfraModuleRequest reqDto = new CheckInfraModuleRequest();
				reqDto.setVerbose(verbose);
				ModuleResultResponse result = apiClient.infraCheckInfraModule(sessionId, reqDto);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InfraManagementNotFound
				| InfraModuleNotFound | SessionNotFound | InvalidSetting def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<InfraCheckResultResponse> getCheckResultList(String managementId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<InfraCheckResultResponse>> proxy = new RestUrlSequentialExecuter<List<InfraCheckResultResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<InfraCheckResultResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<InfraCheckResultResponse> result = apiClient.infraGetCheckResultList(managementId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public InfraFileInfoResponse addInfraFile(File file, AddInfraFileRequest infraFileInfo) throws RestConnectFailed,
			InvalidSetting, InfraFileTooLarge, InvalidUserPass, InvalidRole, InfraManagementDuplicate, HinemosUnknown {
		RestUrlSequentialExecuter<InfraFileInfoResponse> proxy = new RestUrlSequentialExecuter<InfraFileInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public InfraFileInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				InfraFileInfoResponse result = apiClient.infraAddInfraFile(file, infraFileInfo);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidSetting | InfraFileTooLarge | InvalidUserPass | InvalidRole
				| InfraManagementDuplicate | HinemosUnknown def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public InfraFileInfoResponse modifyInfraFile(String fileId, File file, ModifyInfraFileRequest infraFileInfo)
			throws RestConnectFailed, InvalidRole, HinemosUnknown, InfraFileTooLarge, InvalidUserPass, InvalidSetting {
		RestUrlSequentialExecuter<InfraFileInfoResponse> proxy = new RestUrlSequentialExecuter<InfraFileInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public InfraFileInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				InfraFileInfoResponse result = apiClient.infraModifyInfraFile(fileId, file, infraFileInfo);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidRole | HinemosUnknown | InfraFileTooLarge | InvalidUserPass
				| InvalidSetting def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public File downloadInfraFile(String fileId)
			throws RestConnectFailed, InvalidSetting, InvalidUserPass, InvalidRole, InfraFileNotFound, HinemosUnknown {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(this.connectUnit, this.restKind) {
			@Override
			public File executeMethod(DefaultApi apiClient) throws Exception {
				File result = apiClient.infraDownloadInfraFile(fileId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidSetting | InvalidUserPass | InvalidRole | InfraFileNotFound
				| HinemosUnknown def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<InfraFileInfoResponse> deleteInfraFileList(String fileIds)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InfraFileNotFound,
			InfraFileBeingUsed, InfraManagementNotFound, InvalidSetting {
		RestUrlSequentialExecuter<List<InfraFileInfoResponse>> proxy = new RestUrlSequentialExecuter<List<InfraFileInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<InfraFileInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<InfraFileInfoResponse> result = apiClient.infraDeleteInfraFileList(fileIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (InvalidUserPass | InvalidRole | HinemosUnknown | InfraFileNotFound | InfraFileBeingUsed
				| InfraManagementNotFound | InvalidSetting def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<InfraFileInfoResponse> getInfraFileList(String ownerRoleId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<List<InfraFileInfoResponse>> proxy = new RestUrlSequentialExecuter<List<InfraFileInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<InfraFileInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<InfraFileInfoResponse> result = apiClient.infraGetInfraFileList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting def) {// 想定内例外API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
}
