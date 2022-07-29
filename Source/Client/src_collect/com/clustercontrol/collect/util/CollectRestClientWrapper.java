/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.collect.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AddCollectMasterRequest;
import org.openapitools.client.model.BinaryQueryResultResponse;
import org.openapitools.client.model.CollectDataResponse;
import org.openapitools.client.model.CollectKeyInfoResponseP1;
import org.openapitools.client.model.CollectKeyMapForAnalyticsResponse;
import org.openapitools.client.model.CollectKeyResponseP1;
import org.openapitools.client.model.CollectMasterInfoResponse;
import org.openapitools.client.model.CollectorItemCodeMstInfoResponse;
import org.openapitools.client.model.CollectorItemCodeMstMapResponse;
import org.openapitools.client.model.CollectorItemCodeMstResponseP1;
import org.openapitools.client.model.CreatePerfFileRequest;
import org.openapitools.client.model.CreatePerfFileResponse;
import org.openapitools.client.model.DownloadBinaryRecordRequest;
import org.openapitools.client.model.DownloadBinaryRecordsRequest;
import org.openapitools.client.model.GetCoefficientsRequest;
import org.openapitools.client.model.GetCoefficientsResponse;
import org.openapitools.client.model.GetCollectDataResponse;
import org.openapitools.client.model.GetCollectIdResponse;
import org.openapitools.client.model.GetItemCodeListResponse;
import org.openapitools.client.model.QueryCollectBinaryDataRequest;
import org.openapitools.client.model.QueryCollectStringDataRequest;
import org.openapitools.client.model.RunSummaryLogcountRequest;
import org.openapitools.client.model.StringQueryResultResponse;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.BinaryRecordNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.PerfFileNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.rest.endpoint.collect.dto.emuntype.SummaryTypeEnum;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class CollectRestClientWrapper {

	private static Log m_log = LogFactory.getLog(CollectRestClientWrapper.class);
	private RestConnectUnit connectUnit;
	
	private final RestKind restKind = RestKind.CollectRestEndpoints;

	public static CollectRestClientWrapper getWrapper(String managerName){
		return new CollectRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public CollectRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}
	
	public List<CollectKeyResponseP1> getCollectId(String itemName,String displayName, String monitorId, String facilityIdList )
			throws RestConnectFailed, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		RestUrlSequentialExecuter<List<CollectKeyResponseP1>> proxy = new RestUrlSequentialExecuter<List<CollectKeyResponseP1>>(this.connectUnit,this.restKind){
			@Override
			public List<CollectKeyResponseP1> executeMethod( DefaultApi apiClient) throws Exception{
				List<CollectKeyResponseP1>  result = null;
				GetCollectIdResponse dtoRes = apiClient.collectGetCollectId(monitorId, itemName, displayName, null, facilityIdList);
				if (dtoRes != null) {
					result = dtoRes.getKeyList();
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole| InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CollectDataResponse> getCollectData(String idList, SummaryTypeEnum summaryType, String fromTime, String toTime)
			throws RestConnectFailed, HinemosDbTimeout, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		RestUrlSequentialExecuter<List<CollectDataResponse>> proxy = new RestUrlSequentialExecuter<List<CollectDataResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<CollectDataResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<CollectDataResponse> result = null;
				GetCollectDataResponse dtoRes = apiClient.collectGetCollectData(null ,idList, summaryType.name(), fromTime, toTime);
				if (dtoRes != null) {
					result = dtoRes.getCollectDataList();
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosDbTimeout | InvalidUserPass | InvalidRole| InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CollectKeyInfoResponseP1> getItemCodeList(String facilityIds)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<CollectKeyInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<CollectKeyInfoResponseP1>>(this.connectUnit,this.restKind){
			@Override
			public List<CollectKeyInfoResponseP1> executeMethod( DefaultApi apiClient) throws Exception{
				List<CollectKeyInfoResponseP1> result = null;
				GetItemCodeListResponse	dtoRes = apiClient.collectGetItemCodeList(facilityIds, null);
				if (dtoRes != null) {
					result = dtoRes.getKeyInfoList();
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CreatePerfFileResponse createPerfFile(CreatePerfFileRequest createPerfFileRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		RestUrlSequentialExecuter<CreatePerfFileResponse> proxy = new RestUrlSequentialExecuter<CreatePerfFileResponse>(this.connectUnit,this.restKind){
			@Override
			public CreatePerfFileResponse executeMethod( DefaultApi apiClient) throws Exception{
				CreatePerfFileResponse result =  apiClient.collectCreatePerfFile(createPerfFileRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public File downloadPerfFile(String fileName) throws RestConnectFailed, InvalidUserPass, InvalidRole, PerfFileNotFound, HinemosUnknown {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(this.connectUnit,this.restKind){
			@Override
			public File executeMethod( DefaultApi apiClient) throws Exception{
				File result =  apiClient.collectDownloadPerfFile(fileName);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | PerfFileNotFound | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<CollectorItemCodeMstInfoResponse> getCollectItemCodeMasterList()throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<CollectorItemCodeMstInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CollectorItemCodeMstInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<CollectorItemCodeMstInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<CollectorItemCodeMstInfoResponse> result =  apiClient.collectGetCollectItemCodeMasterList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CollectKeyMapForAnalyticsResponse getCollectKeyMapForAnalytics(String facilityId,String ownerRoleId )
			throws RestConnectFailed, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		RestUrlSequentialExecuter<CollectKeyMapForAnalyticsResponse> proxy = new RestUrlSequentialExecuter<CollectKeyMapForAnalyticsResponse>(this.connectUnit,this.restKind){
			@Override
			public CollectKeyMapForAnalyticsResponse executeMethod( DefaultApi apiClient) throws Exception{
				CollectKeyMapForAnalyticsResponse result =  apiClient.collectGetCollectKeyMapForAnalytics(facilityId, ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole| InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public GetCoefficientsResponse getCoefficients(GetCoefficientsRequest getCoefficientsRequest)
			throws RestConnectFailed, InvalidSetting, HinemosUnknown {
		RestUrlSequentialExecuter<GetCoefficientsResponse> proxy = new RestUrlSequentialExecuter<GetCoefficientsResponse>(this.connectUnit,this.restKind){
			@Override
			public GetCoefficientsResponse executeMethod( DefaultApi apiClient) throws Exception{
				GetCoefficientsResponse result =  apiClient.collectGetCoefficients(getCoefficientsRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CollectorItemCodeMstMapResponse getItemCodeMap()throws RestConnectFailed ,InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<CollectorItemCodeMstMapResponse> proxy = new RestUrlSequentialExecuter<CollectorItemCodeMstMapResponse>(this.connectUnit,this.restKind){
			@Override
			public CollectorItemCodeMstMapResponse executeMethod( DefaultApi apiClient) throws Exception{
				CollectorItemCodeMstMapResponse result =  apiClient.collectGetItemCodeMap();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CollectorItemCodeMstResponseP1 getAvailableCollectorItemList(String facilityId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<CollectorItemCodeMstResponseP1> proxy = new RestUrlSequentialExecuter<CollectorItemCodeMstResponseP1>(this.connectUnit,this.restKind){
			@Override
			public CollectorItemCodeMstResponseP1 executeMethod( DefaultApi apiClient) throws Exception{
				CollectorItemCodeMstResponseP1 result =  apiClient.collectGetAvailableCollectorItemList(facilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public Void runSummaryLogcount(RunSummaryLogcountRequest reqDto)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(this.connectUnit,this.restKind){
			@Override
			public Void executeMethod( DefaultApi apiClient) throws Exception{
				apiClient.collectRunSummaryLogcount(reqDto);
				return null;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole| InvalidSetting| HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public StringQueryResultResponse queryCollectStringData(QueryCollectStringDataRequest queryCollectStringDataRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosDbTimeout, InvalidSetting,HinemosUnknown {
		RestUrlSequentialExecuter<StringQueryResultResponse> proxy = new RestUrlSequentialExecuter<StringQueryResultResponse>(this.connectUnit,this.restKind){
			@Override
			public StringQueryResultResponse executeMethod( DefaultApi apiClient) throws Exception{
				StringQueryResultResponse result =  apiClient.collectQueryCollectStringData(queryCollectStringDataRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosDbTimeout | InvalidSetting| HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public BinaryQueryResultResponse queryCollectBinaryData(QueryCollectBinaryDataRequest queryCollectBinaryDataRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosDbTimeout, InvalidSetting ,HinemosUnknown {
		RestUrlSequentialExecuter<BinaryQueryResultResponse> proxy = new RestUrlSequentialExecuter<BinaryQueryResultResponse>(this.connectUnit,this.restKind){
			@Override
			public BinaryQueryResultResponse executeMethod( DefaultApi apiClient) throws Exception{
				BinaryQueryResultResponse result =  apiClient.collectQueryCollectBinaryData(queryCollectBinaryDataRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosDbTimeout | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CollectMasterInfoResponse addCollectMaster(AddCollectMasterRequest addCollectMasterRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, InvalidSetting , HinemosUnknown {
		RestUrlSequentialExecuter<CollectMasterInfoResponse> proxy = new RestUrlSequentialExecuter<CollectMasterInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CollectMasterInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CollectMasterInfoResponse result =  apiClient.collectAddCollectMaster(addCollectMasterRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CollectMasterInfoResponse deleteCollectMasterAll()
			throws RestConnectFailed, InvalidUserPass, InvalidRole,HinemosUnknown {
		RestUrlSequentialExecuter<CollectMasterInfoResponse> proxy = new RestUrlSequentialExecuter<CollectMasterInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CollectMasterInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CollectMasterInfoResponse result =  apiClient.collectDeleteCollectMasterAll();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CollectMasterInfoResponse getCollectMasterInfo()
			throws RestConnectFailed, InvalidUserPass, InvalidRole , HinemosUnknown {
		RestUrlSequentialExecuter<CollectMasterInfoResponse> proxy = new RestUrlSequentialExecuter<CollectMasterInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CollectMasterInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CollectMasterInfoResponse result =  apiClient.collectGetCollectMasterInfo();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	

	public File downloadBinaryRecord(DownloadBinaryRecordRequest downloadBinaryRecordRequest)
			throws RestConnectFailed,BinaryRecordNotFound, HinemosDbTimeout, IOException,
			InvalidRole, InvalidSetting, InvalidUserPass, HinemosUnknown {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(this.connectUnit,this.restKind){
			@Override
			public File executeMethod( DefaultApi apiClient) throws Exception{
				File result =  apiClient.collectDownloadBinaryRecord(downloadBinaryRecordRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | BinaryRecordNotFound | HinemosDbTimeout | IOException
				| InvalidRole | InvalidSetting | InvalidUserPass | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public File downloadBinaryRecords(DownloadBinaryRecordsRequest downloadBinaryRecordsRequest)
			throws RestConnectFailed, BinaryRecordNotFound, HinemosDbTimeout, IOException, 
			InvalidRole, InvalidSetting, InvalidUserPass, HinemosUnknown {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(this.connectUnit,this.restKind){
			@Override
			public File executeMethod( DefaultApi apiClient) throws Exception{
				File result =  apiClient.collectDownloadBinaryRecords(downloadBinaryRecordsRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | BinaryRecordNotFound | HinemosDbTimeout | IOException
				| InvalidRole | InvalidSetting | InvalidUserPass | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

}