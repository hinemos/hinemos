/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import java.util.List;

import org.openapitools.client.model.AddSdmlControlSettingRequest;
import org.openapitools.client.model.GetSdmlControlSettingListRequest;
import org.openapitools.client.model.ImportSdmlControlRequest;
import org.openapitools.client.model.ImportSdmlControlResponse;
import org.openapitools.client.model.ModifySdmlControlSettingRequest;
import org.openapitools.client.model.SdmlControlSettingInfoResponse;
import org.openapitools.client.model.SdmlMonitorTypeMasterResponse;
import org.openapitools.client.model.SetSdmlControlSettingStatusRequest;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.SdmlControlSettingDuplicate;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.rest.client.SdmlApi;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;

/**
 * SDMLのREST用ラッパークラス
 *
 */
public class SdmlRestClientWrapper {

	private RestConnectUnit connectUnit;

	private final RestKind restKind = RestKind.SdmlRestEndpoints;

	public static SdmlRestClientWrapper getWrapper(String managerName) {
		return new SdmlRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public SdmlRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}

	public SdmlControlSettingInfoResponse addSdmlControlSettingV1(
			AddSdmlControlSettingRequest addSdmlControlSettingRequest) throws RestConnectFailed, HinemosUnknown,
			SdmlControlSettingDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		SdmlRestUrlSequentialExecuter<SdmlControlSettingInfoResponse> proxy = new SdmlRestUrlSequentialExecuter<SdmlControlSettingInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public SdmlControlSettingInfoResponse executeMethod(SdmlApi apiClient) throws Exception {
				SdmlControlSettingInfoResponse result = apiClient
						.sdmlAddSdmlControlSettingV1(addSdmlControlSettingRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | SdmlControlSettingDuplicate | InvalidUserPass | InvalidRole
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public SdmlControlSettingInfoResponse modifySdmlControlSettingV1(String applicationId,
			ModifySdmlControlSettingRequest modifySdmlControlSettingRequest) throws RestConnectFailed, HinemosUnknown,
			SdmlControlSettingNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		SdmlRestUrlSequentialExecuter<SdmlControlSettingInfoResponse> proxy = new SdmlRestUrlSequentialExecuter<SdmlControlSettingInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public SdmlControlSettingInfoResponse executeMethod(SdmlApi apiClient) throws Exception {
				SdmlControlSettingInfoResponse result = apiClient.sdmlModifySdmlControlSettingV1(applicationId,
						modifySdmlControlSettingRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | SdmlControlSettingNotFound | InvalidUserPass | InvalidRole
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SdmlControlSettingInfoResponse> deleteSdmlControlSettingV1(String applicationIds)
			throws RestConnectFailed, HinemosUnknown, SdmlControlSettingNotFound, InvalidUserPass, InvalidRole {
		SdmlRestUrlSequentialExecuter<List<SdmlControlSettingInfoResponse>> proxy = new SdmlRestUrlSequentialExecuter<List<SdmlControlSettingInfoResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<SdmlControlSettingInfoResponse> executeMethod(SdmlApi apiClient) throws Exception {
				List<SdmlControlSettingInfoResponse> result = apiClient.sdmlDeleteSdmlControlSettingV1(applicationIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | SdmlControlSettingNotFound | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public SdmlControlSettingInfoResponse getSdmlControlSettingV1(String applicationId)
			throws RestConnectFailed, HinemosUnknown, SdmlControlSettingNotFound, InvalidUserPass, InvalidRole {
		SdmlRestUrlSequentialExecuter<SdmlControlSettingInfoResponse> proxy = new SdmlRestUrlSequentialExecuter<SdmlControlSettingInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public SdmlControlSettingInfoResponse executeMethod(SdmlApi apiClient) throws Exception {
				SdmlControlSettingInfoResponse result = apiClient.sdmlGetSdmlControlSettingV1(applicationId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | SdmlControlSettingNotFound | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SdmlControlSettingInfoResponse> getSdmlControlSettingListV1(String ownerRoleId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		SdmlRestUrlSequentialExecuter<List<SdmlControlSettingInfoResponse>> proxy = new SdmlRestUrlSequentialExecuter<List<SdmlControlSettingInfoResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<SdmlControlSettingInfoResponse> executeMethod(SdmlApi apiClient) throws Exception {
				List<SdmlControlSettingInfoResponse> result = apiClient.sdmlGetSdmlControlSettingListV1(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SdmlControlSettingInfoResponse> getSdmlControlSettingListByConditionV1(
			GetSdmlControlSettingListRequest getSdmlControlSettingListRequest)
			throws RestConnectFailed, InvalidSetting, HinemosUnknown, InvalidUserPass, InvalidRole {
		SdmlRestUrlSequentialExecuter<List<SdmlControlSettingInfoResponse>> proxy = new SdmlRestUrlSequentialExecuter<List<SdmlControlSettingInfoResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<SdmlControlSettingInfoResponse> executeMethod(SdmlApi apiClient) throws Exception {
				List<SdmlControlSettingInfoResponse> result = apiClient
						.sdmlGetSdmlControlSettingListByConditionV1(getSdmlControlSettingListRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidSetting | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SdmlControlSettingInfoResponse> setSdmlControlSettingStatusV1(
			SetSdmlControlSettingStatusRequest setSdmlControlSettingStatusRequest) throws RestConnectFailed,
			HinemosUnknown, SdmlControlSettingNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		SdmlRestUrlSequentialExecuter<List<SdmlControlSettingInfoResponse>> proxy = new SdmlRestUrlSequentialExecuter<List<SdmlControlSettingInfoResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<SdmlControlSettingInfoResponse> executeMethod(SdmlApi apiClient) throws Exception {
				List<SdmlControlSettingInfoResponse> result = apiClient
						.sdmlSetSdmlControlSettingStatusV1(setSdmlControlSettingStatusRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | SdmlControlSettingNotFound | InvalidUserPass | InvalidRole
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SdmlControlSettingInfoResponse> setSdmlControlSettingLogCollectorV1(
			SetSdmlControlSettingStatusRequest setSdmlControlSettingStatusRequest) throws RestConnectFailed,
			HinemosUnknown, SdmlControlSettingNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		SdmlRestUrlSequentialExecuter<List<SdmlControlSettingInfoResponse>> proxy = new SdmlRestUrlSequentialExecuter<List<SdmlControlSettingInfoResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<SdmlControlSettingInfoResponse> executeMethod(SdmlApi apiClient) throws Exception {
				List<SdmlControlSettingInfoResponse> result = apiClient
						.sdmlSetSdmlControlSettingLogCollectorV1(setSdmlControlSettingStatusRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | SdmlControlSettingNotFound | InvalidUserPass | InvalidRole
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportSdmlControlResponse importSdmlControlSettingV1(ImportSdmlControlRequest importSdmlControlRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		SdmlRestUrlSequentialExecuter<ImportSdmlControlResponse> proxy = new SdmlRestUrlSequentialExecuter<ImportSdmlControlResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportSdmlControlResponse executeMethod(SdmlApi apiClient) throws Exception {
				ImportSdmlControlResponse result = apiClient.sdmlImportSdmlControlSettingV1(importSdmlControlRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SdmlMonitorTypeMasterResponse> getSdmlMonitorTypeMaster()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		SdmlRestUrlSequentialExecuter<List<SdmlMonitorTypeMasterResponse>> proxy = new SdmlRestUrlSequentialExecuter<List<SdmlMonitorTypeMasterResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<SdmlMonitorTypeMasterResponse> executeMethod(SdmlApi apiClient) throws Exception {
				List<SdmlMonitorTypeMasterResponse> result = apiClient.sdmlGetSdmlMonitorTypeMaster();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}
}
