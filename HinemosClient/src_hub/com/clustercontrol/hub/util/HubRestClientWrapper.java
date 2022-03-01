/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.hub.util;

import java.util.List;

import org.openapitools.client.model.AddLogFormatRequest;
import org.openapitools.client.model.AddTransferInfoRequest;
import org.openapitools.client.model.LogFormatResponse;
import org.openapitools.client.model.LogFormatResponseP1;
import org.openapitools.client.model.ModifyLogFormatRequest;
import org.openapitools.client.model.ModifyTransferInfoRequest;
import org.openapitools.client.model.SetTransferValidRequest;
import org.openapitools.client.model.TransferInfoDestTypeMstResponse;
import org.openapitools.client.model.TransferInfoResponse;
import org.openapitools.client.model.TransferInfoResponseP1;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.LogFormatDuplicate;
import com.clustercontrol.fault.LogFormatKeyPatternDuplicate;
import com.clustercontrol.fault.LogFormatNotFound;
import com.clustercontrol.fault.LogFormatUsed;
import com.clustercontrol.fault.LogTransferDuplicate;
import com.clustercontrol.fault.LogTransferNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class HubRestClientWrapper {

	private RestConnectUnit connectUnit;
	
	private final RestKind restKind = RestKind.HubRestEndpoints;

	public static HubRestClientWrapper getWrapper(String managerName) {
		return new HubRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public HubRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}
	
	public LogFormatResponse getLogFormat(String logFormatId) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<LogFormatResponse> proxy = new RestUrlSequentialExecuter<LogFormatResponse>(
				this.connectUnit,
				this.restKind) {
			@Override
			public LogFormatResponse executeMethod(DefaultApi apiClient) throws Exception {
				LogFormatResponse result =  apiClient.hubGetLogFormat(logFormatId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}


	public List<LogFormatResponseP1> getLogFormatIdList(String ownerRoleId) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<List<LogFormatResponseP1>> proxy = new RestUrlSequentialExecuter<List<LogFormatResponseP1>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<LogFormatResponseP1> executeMethod(DefaultApi apiClient) throws Exception {
				List<LogFormatResponseP1> result =  apiClient.hubGetLogFormatIdList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<LogFormatResponse> getLogFormatListByOwnerRole(String ownerRoleId) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<List<LogFormatResponse>> proxy = new RestUrlSequentialExecuter<List<LogFormatResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<LogFormatResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<LogFormatResponse> result =  apiClient.hubGetLogFormatListByOwnerRole(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public LogFormatResponse addLogFormat(AddLogFormatRequest addLogFormatRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, LogFormatDuplicate, LogFormatKeyPatternDuplicate, InvalidSetting {
		RestUrlSequentialExecuter<LogFormatResponse> proxy = new RestUrlSequentialExecuter<LogFormatResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public LogFormatResponse executeMethod(DefaultApi apiClient) throws Exception {
				LogFormatResponse result =  apiClient.hubAddLogFormat(addLogFormatRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | LogFormatDuplicate | LogFormatKeyPatternDuplicate | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public LogFormatResponse modifyLogFormat(String logFormatId, ModifyLogFormatRequest modifyLogFormatRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, LogFormatNotFound, LogFormatKeyPatternDuplicate, InvalidSetting {
		RestUrlSequentialExecuter<LogFormatResponse> proxy = new RestUrlSequentialExecuter<LogFormatResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public LogFormatResponse executeMethod(DefaultApi apiClient) throws Exception {
				LogFormatResponse result =  apiClient.hubModifyLogFormat(logFormatId, modifyLogFormatRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | LogFormatNotFound | LogFormatKeyPatternDuplicate | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<LogFormatResponse> deleteLogFormat(String logFormatIds)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, LogFormatNotFound, LogFormatUsed {
		RestUrlSequentialExecuter<List<LogFormatResponse>> proxy = new RestUrlSequentialExecuter<List<LogFormatResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<LogFormatResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<LogFormatResponse> result =  apiClient.hubDeleteLogFormat(logFormatIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | LogFormatNotFound | LogFormatUsed def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public TransferInfoResponse getTransferInfo(String transferId) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<TransferInfoResponse> proxy = new RestUrlSequentialExecuter<TransferInfoResponse>(
				this.connectUnit,
				this.restKind) {
			@Override
			public TransferInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				TransferInfoResponse result =  apiClient.hubGetTransferInfo(transferId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<TransferInfoResponseP1> getTransferInfoIdList(String ownerRoleId) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<List<TransferInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<TransferInfoResponseP1>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<TransferInfoResponseP1> executeMethod(DefaultApi apiClient) throws Exception {
				List<TransferInfoResponseP1> result =  apiClient.hubGetTransferInfoIdList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<TransferInfoResponse> getTransferListByOwnerRole(String ownerRoleId) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<List<TransferInfoResponse>> proxy = new RestUrlSequentialExecuter<List<TransferInfoResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<TransferInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<TransferInfoResponse> result =  apiClient.hubGetTransferInfoListByOwnerRole(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public TransferInfoResponse addTransferInfo(AddTransferInfoRequest addTransferInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, LogTransferDuplicate, InvalidSetting {
		RestUrlSequentialExecuter<TransferInfoResponse> proxy = new RestUrlSequentialExecuter<TransferInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public TransferInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				TransferInfoResponse result =  apiClient.hubAddTransferInfo(addTransferInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | LogTransferDuplicate | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public TransferInfoResponse modifyTransferInfo(String transferId, ModifyTransferInfoRequest modifyTransferInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, LogTransferNotFound, InvalidSetting {
		RestUrlSequentialExecuter<TransferInfoResponse> proxy = new RestUrlSequentialExecuter<TransferInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public TransferInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				TransferInfoResponse result =  apiClient.hubModifyTransferInfo(transferId, modifyTransferInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | LogTransferNotFound | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<TransferInfoResponse> deleteTransferInfo(String transferIds)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, LogTransferNotFound {
		RestUrlSequentialExecuter<List<TransferInfoResponse>> proxy = new RestUrlSequentialExecuter<List<TransferInfoResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<TransferInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<TransferInfoResponse> result =  apiClient.hubDeleteTransferInfo(transferIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | LogTransferNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<TransferInfoDestTypeMstResponse> getTransferInfoDestTypeMstList() throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<List<TransferInfoDestTypeMstResponse>> proxy = new RestUrlSequentialExecuter<List<TransferInfoDestTypeMstResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<TransferInfoDestTypeMstResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<TransferInfoDestTypeMstResponse> result =  apiClient.hubgetTransferInfoDestTypeMstList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<TransferInfoResponse> setTransferValid(SetTransferValidRequest setTransferValidRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<List<TransferInfoResponse>> proxy = new RestUrlSequentialExecuter<List<TransferInfoResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<TransferInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<TransferInfoResponse> result = apiClient.hubSetTransferValid(setTransferValidRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

}
