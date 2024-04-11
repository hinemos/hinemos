/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.reporting.util;

import java.io.File;
import java.util.List;

import org.openapitools.client.model.AddReportingScheduleRequest;
import org.openapitools.client.model.AddTemplateSetRequest;
import org.openapitools.client.model.CheckPublishResponse;
import org.openapitools.client.model.CreateReportingFileRequest;
import org.openapitools.client.model.CreateReportingFileResponse;
import org.openapitools.client.model.ModifyReportingScheduleRequest;
import org.openapitools.client.model.ModifyTemplateSetRequest;
import org.openapitools.client.model.ReportingScheduleResponse;
import org.openapitools.client.model.SetReportingStatusRequest;
import org.openapitools.client.model.TemplateIdListResponse;
import org.openapitools.client.model.TemplateSetDetailInfoResponse;
import org.openapitools.client.model.TemplateSetInfoResponse;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ReportFileCreateFailed;
import com.clustercontrol.fault.ReportFileNotFound;
import com.clustercontrol.fault.ReportingDuplicate;
import com.clustercontrol.fault.ReportingNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class ReportingRestClientWrapper implements ICheckPublishRestClientWrapper {

	private RestConnectUnit connectUnit;
	
	private final RestKind restKind = RestKind.ReportingRestEndpoints;

	public static ReportingRestClientWrapper getWrapper(String managerName) {
		return new ReportingRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public ReportingRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}
	
	public ReportingScheduleResponse addReportingSchedule(AddReportingScheduleRequest addReportingRequest) throws RestConnectFailed, HinemosUnknown, ReportingDuplicate, InvalidUserPass, InvalidRole,InvalidSetting {
		RestUrlSequentialExecuter<ReportingScheduleResponse> proxy = new RestUrlSequentialExecuter<ReportingScheduleResponse>(this.connectUnit,this.restKind){
			@Override
			public ReportingScheduleResponse executeMethod( DefaultApi apiClient) throws Exception{
				ReportingScheduleResponse result =  apiClient.reportingAddReportingSchedule(addReportingRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | ReportingDuplicate| InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ReportingScheduleResponse modifyReportingSchedule(String scheduleId, ModifyReportingScheduleRequest modifyReportingRequest) throws RestConnectFailed, HinemosUnknown, NotifyNotFound, ReportingNotFound, InvalidUserPass, InvalidRole,InvalidSetting {
		RestUrlSequentialExecuter<ReportingScheduleResponse> proxy = new RestUrlSequentialExecuter<ReportingScheduleResponse>(this.connectUnit,this.restKind){
			@Override
			public ReportingScheduleResponse executeMethod( DefaultApi apiClient) throws Exception{
				ReportingScheduleResponse result =  apiClient.reportingModifyReportingSchedule(scheduleId, modifyReportingRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyNotFound | ReportingNotFound | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<ReportingScheduleResponse> deleteReportingSchedule(String scheduleIds) throws RestConnectFailed, HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<ReportingScheduleResponse>> proxy = new RestUrlSequentialExecuter<List<ReportingScheduleResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<ReportingScheduleResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<ReportingScheduleResponse> result =  apiClient.reportingDeleteReportingSchedule(scheduleIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | ReportingNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ReportingScheduleResponse getReportingSchedule(String scheduleId) throws RestConnectFailed, ReportingNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<ReportingScheduleResponse> proxy = new RestUrlSequentialExecuter<ReportingScheduleResponse>(this.connectUnit,this.restKind){
			@Override
			public ReportingScheduleResponse executeMethod( DefaultApi apiClient) throws Exception{
				ReportingScheduleResponse result =  apiClient.reportingGetReportingSchedule(scheduleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | ReportingNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<ReportingScheduleResponse> getReportingScheduleList() throws RestConnectFailed, HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<ReportingScheduleResponse>> proxy = new RestUrlSequentialExecuter<List<ReportingScheduleResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<ReportingScheduleResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<ReportingScheduleResponse> result =  apiClient.reportingGetReportingScheduleList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | ReportingNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<ReportingScheduleResponse> setReportingScheduleStatus(SetReportingStatusRequest setReportingStatusRequest) throws RestConnectFailed, NotifyNotFound, ReportingNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<ReportingScheduleResponse>> proxy = new RestUrlSequentialExecuter<List<ReportingScheduleResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<ReportingScheduleResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<ReportingScheduleResponse> result =  apiClient.reportingSetReportingScheduleStatus(setReportingStatusRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | ReportingNotFound | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CreateReportingFileResponse createReportingFileManual(String reportId, CreateReportingFileRequest createReportingFileRequest) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CreateReportingFileResponse> proxy = new RestUrlSequentialExecuter<CreateReportingFileResponse>(this.connectUnit,this.restKind){
			@Override
			public CreateReportingFileResponse executeMethod( DefaultApi apiClient) throws Exception{
				CreateReportingFileResponse result =  apiClient.reportingCreateReportingFileManual(reportId, createReportingFileRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public TemplateSetInfoResponse addTemplateSet(AddTemplateSetRequest addTemplateSetRequest) throws RestConnectFailed, HinemosUnknown, ReportingDuplicate, InvalidUserPass, InvalidRole,InvalidSetting {
		RestUrlSequentialExecuter<TemplateSetInfoResponse> proxy = new RestUrlSequentialExecuter<TemplateSetInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public TemplateSetInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				TemplateSetInfoResponse result =  apiClient.reportingAddTemplateSet(addTemplateSetRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | ReportingDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public TemplateSetInfoResponse modifyTemplateSet(String templateSetId, ModifyTemplateSetRequest modifyTemplateSetRequest) throws RestConnectFailed,HinemosUnknown, NotifyNotFound, ReportingNotFound, InvalidUserPass, InvalidRole,InvalidSetting {
		RestUrlSequentialExecuter<TemplateSetInfoResponse> proxy = new RestUrlSequentialExecuter<TemplateSetInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public TemplateSetInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				TemplateSetInfoResponse result =  apiClient.reportingModifyTemplateSet(templateSetId, modifyTemplateSetRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyNotFound | ReportingNotFound | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<TemplateSetInfoResponse> deleteTemplateSet(String templateSetIdList) throws RestConnectFailed,HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<TemplateSetInfoResponse>> proxy = new RestUrlSequentialExecuter<List<TemplateSetInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<TemplateSetInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<TemplateSetInfoResponse> result =  apiClient.reportingDeleteTemplateSet(templateSetIdList);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | ReportingNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<TemplateSetInfoResponse> getTemplateSetList(String ownerRoleId) throws RestConnectFailed, HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<TemplateSetInfoResponse>> proxy = new RestUrlSequentialExecuter<List<TemplateSetInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<TemplateSetInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<TemplateSetInfoResponse> result =  apiClient.reportingGetTemplateSetList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | ReportingNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public TemplateSetInfoResponse getTemplateSetInfo(String templateSetId) throws RestConnectFailed, HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<TemplateSetInfoResponse> proxy = new RestUrlSequentialExecuter<TemplateSetInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public TemplateSetInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				TemplateSetInfoResponse result =  apiClient.reportingGetTemplateSetInfo(templateSetId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | ReportingNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<TemplateSetDetailInfoResponse> getTemplateSetDetailInfoList(String templateSetId) throws RestConnectFailed, InvalidRole, HinemosUnknown, InvalidUserPass {
		RestUrlSequentialExecuter<List<TemplateSetDetailInfoResponse>> proxy = new RestUrlSequentialExecuter<List<TemplateSetDetailInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<TemplateSetDetailInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<TemplateSetDetailInfoResponse> result =  apiClient.reportingGetTemplateSetDetailInfoList(templateSetId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidRole | HinemosUnknown | InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public TemplateIdListResponse getTemplateIdList(String ownerRoleId) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<TemplateIdListResponse> proxy = new RestUrlSequentialExecuter<TemplateIdListResponse>(this.connectUnit,this.restKind){
			@Override
			public TemplateIdListResponse executeMethod( DefaultApi apiClient) throws Exception{
				TemplateIdListResponse result =  apiClient.reportingGetTemplateIdList(ownerRoleId);
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
	
	@Override
	public CheckPublishResponse checkPublish() throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<CheckPublishResponse> proxy = new RestUrlSequentialExecuter<CheckPublishResponse>(this.connectUnit,this.restKind){
			@Override
			public CheckPublishResponse executeMethod( DefaultApi apiClient) throws Exception{
				CheckPublishResponse result =  apiClient.reportingCheckPublish();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed def) {//通信異常
			throw new RestConnectFailed(Messages.getString("message.hinemos.failure.transfer") + ", " + HinemosMessage.replace(def.getMessage()), def);
		} catch (InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (UrlNotFound e) {
			// UrlNotFoundが返された場合エンドポイントがPublishされていないためメッセージを設定する
			throw new HinemosUnknown(Messages.getString("message.expiration.term"), e);
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(Messages.getString("message.unexpected_error") + "," + HinemosMessage.replace(unknown.getMessage()), unknown);
		}
	}
	
	public File downloadReportingFile(String fileName) throws RestConnectFailed, InvalidUserPass, InvalidRole, ReportFileNotFound, ReportFileCreateFailed, HinemosUnknown {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(this.connectUnit,this.restKind){
			@Override
			public File executeMethod( DefaultApi apiClient) throws Exception{
				File result =  apiClient.reportingDownloadReportingFile(fileName);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | ReportFileNotFound | ReportFileCreateFailed | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
}
