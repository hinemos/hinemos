/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.util;

import java.util.List;

import org.openapitools.client.model.AddCloudNotifyRequest;
import org.openapitools.client.model.AddCommandNotifyRequest;
import org.openapitools.client.model.AddEventNotifyRequest;
import org.openapitools.client.model.AddInfraNotifyRequest;
import org.openapitools.client.model.AddJobNotifyRequest;
import org.openapitools.client.model.AddLogEscalateNotifyRequest;
import org.openapitools.client.model.AddMailNotifyRequest;
import org.openapitools.client.model.AddMessageNotifyRequest;
import org.openapitools.client.model.AddRestNotifyRequest;
import org.openapitools.client.model.AddStatusNotifyRequest;
import org.openapitools.client.model.CloudNotifyInfoResponse;
import org.openapitools.client.model.CommandNotifyInfoResponse;
import org.openapitools.client.model.EventDataInfoResponse;
import org.openapitools.client.model.EventNotifyInfoResponse;
import org.openapitools.client.model.InfraNotifyInfoResponse;
import org.openapitools.client.model.JobNotifyInfoResponse;
import org.openapitools.client.model.LogEscalateNotifyInfoResponse;
import org.openapitools.client.model.MailNotifyInfoResponse;
import org.openapitools.client.model.MessageNotifyInfoResponse;
import org.openapitools.client.model.ModifyCloudNotifyRequest;
import org.openapitools.client.model.ModifyCommandNotifyRequest;
import org.openapitools.client.model.ModifyEventNotifyRequest;
import org.openapitools.client.model.ModifyInfraNotifyRequest;
import org.openapitools.client.model.ModifyJobNotifyRequest;
import org.openapitools.client.model.ModifyLogEscalateNotifyRequest;
import org.openapitools.client.model.ModifyMailNotifyRequest;
import org.openapitools.client.model.ModifyMessageNotifyRequest;
import org.openapitools.client.model.ModifyRestNotifyRequest;
import org.openapitools.client.model.ModifyStatusNotifyRequest;
import org.openapitools.client.model.NotifyAsMonitorRequest;
import org.openapitools.client.model.NotifyCheckIdResultInfoResponse;
import org.openapitools.client.model.NotifyEventRequest;
import org.openapitools.client.model.NotifyInfoResponse;
import org.openapitools.client.model.RestNotifyInfoResponse;
import org.openapitools.client.model.SetNotifyValidRequest;
import org.openapitools.client.model.StatusNotifyInfoResponse;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class NotifyRestClientWrapper {

	private RestConnectUnit connectUnit;

	private final RestKind restKind = RestKind.NotifyRestEndpoints;

	public static NotifyRestClientWrapper getWrapper(String managerName) {
		return new NotifyRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public NotifyRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}

	public StatusNotifyInfoResponse addStatusNotify(AddStatusNotifyRequest addStatusNotifyRequest) throws RestConnectFailed, HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<StatusNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<StatusNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public StatusNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				StatusNotifyInfoResponse result =  apiClient.notifyAddStatusNotify(addStatusNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public EventNotifyInfoResponse addEventNotify(AddEventNotifyRequest addEventNotifyRequest) throws RestConnectFailed, HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<EventNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<EventNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public EventNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				EventNotifyInfoResponse result =  apiClient.notifyAddEventNotify(addEventNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public MailNotifyInfoResponse addMailNotify(AddMailNotifyRequest addMailNotifyRequest) throws RestConnectFailed, HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<MailNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<MailNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public MailNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				MailNotifyInfoResponse result =  apiClient.notifyAddMailNotify(addMailNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobNotifyInfoResponse addJobNotify(AddJobNotifyRequest addJobNotifyRequest) throws RestConnectFailed, HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<JobNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<JobNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public JobNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobNotifyInfoResponse result =  apiClient.notifyAddJobNotify(addJobNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public LogEscalateNotifyInfoResponse addLogEscalateNotify(AddLogEscalateNotifyRequest addLogEscalateNotifyRequest) throws RestConnectFailed, HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<LogEscalateNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<LogEscalateNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public LogEscalateNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				LogEscalateNotifyInfoResponse result =  apiClient.notifyAddLogEscalateNotify(addLogEscalateNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CommandNotifyInfoResponse addCommandNotify(AddCommandNotifyRequest addCommandNotifyRequest) throws RestConnectFailed, HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CommandNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<CommandNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CommandNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CommandNotifyInfoResponse result =  apiClient.notifyAddCommandNotify(addCommandNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public InfraNotifyInfoResponse addInfraNotify(AddInfraNotifyRequest addInfraNotifyRequest) throws RestConnectFailed, HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<InfraNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<InfraNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public InfraNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				InfraNotifyInfoResponse result =  apiClient.notifyAddInfraNotify(addInfraNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CloudNotifyInfoResponse addCloudNotify(AddCloudNotifyRequest addCloudNotifyRequest) throws RestConnectFailed, HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CloudNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<CloudNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CloudNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CloudNotifyInfoResponse result =  apiClient.notifyAddCloudNotify(addCloudNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public RestNotifyInfoResponse addRestNotify(AddRestNotifyRequest addRestNotifyRequest) throws RestConnectFailed, HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<RestNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<RestNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public RestNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				RestNotifyInfoResponse result =  apiClient.notifyAddRestNotify(addRestNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public MessageNotifyInfoResponse addMessageNotify(AddMessageNotifyRequest addMessageNotifyRequest) throws RestConnectFailed, HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<MessageNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<MessageNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public MessageNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				MessageNotifyInfoResponse result = apiClient.notifyAddMessageNotify(addMessageNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public StatusNotifyInfoResponse modifyStatusNotify(String notifyId, ModifyStatusNotifyRequest modifyStatusNotifyRequest) throws RestConnectFailed, NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<StatusNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<StatusNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public StatusNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				StatusNotifyInfoResponse result =  apiClient.notifyModifyStatusNotify(notifyId, modifyStatusNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyDuplicate | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public EventNotifyInfoResponse modifyEventNotify(String notifyId, ModifyEventNotifyRequest modifyEventNotifyRequest) throws RestConnectFailed, NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<EventNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<EventNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public EventNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				EventNotifyInfoResponse result =  apiClient.notifyModifyEventNotify(notifyId, modifyEventNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyDuplicate | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public MailNotifyInfoResponse modifyMailNotify(String notifyId, ModifyMailNotifyRequest modifyMailNotifyRequest) throws RestConnectFailed, NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<MailNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<MailNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public MailNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				MailNotifyInfoResponse result =  apiClient.notifyModifyMailNotify(notifyId, modifyMailNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyDuplicate | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobNotifyInfoResponse modifyJobNotify(String notifyId, ModifyJobNotifyRequest modifyJobNotifyRequest) throws RestConnectFailed, NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<JobNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<JobNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public JobNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobNotifyInfoResponse result =  apiClient.notifyModifyJobNotify(notifyId, modifyJobNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyDuplicate | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public LogEscalateNotifyInfoResponse modifyLogEscalateNotify(String notifyId, ModifyLogEscalateNotifyRequest modifyLogEscalateNotifyRequest) throws RestConnectFailed, NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<LogEscalateNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<LogEscalateNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public LogEscalateNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				LogEscalateNotifyInfoResponse result =  apiClient.notifyModifyLogEscalateNotify(notifyId, modifyLogEscalateNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyDuplicate | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CommandNotifyInfoResponse modifyCommandNotify(String notifyId, ModifyCommandNotifyRequest modifyCommandNotifyRequest) throws RestConnectFailed, NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CommandNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<CommandNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CommandNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CommandNotifyInfoResponse result =  apiClient.notifyModifyCommandNotify(notifyId, modifyCommandNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyDuplicate | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public InfraNotifyInfoResponse modifyInfraNotify(String notifyId, ModifyInfraNotifyRequest modifyInfraNotifyRequest) throws RestConnectFailed, NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<InfraNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<InfraNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public InfraNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				InfraNotifyInfoResponse result =  apiClient.notifyModifyInfraNotify(notifyId, modifyInfraNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyDuplicate | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CloudNotifyInfoResponse modifyCloudNotify(String notifyId, ModifyCloudNotifyRequest modifyCloudNotifyRequest) throws RestConnectFailed, NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CloudNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<CloudNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CloudNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CloudNotifyInfoResponse result =  apiClient.notifyModifyCloudNotify(notifyId, modifyCloudNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyDuplicate | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public RestNotifyInfoResponse modifyRestNotify(String notifyId, ModifyRestNotifyRequest modifyRestNotifyRequest) throws RestConnectFailed, NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<RestNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<RestNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public RestNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				RestNotifyInfoResponse result =  apiClient.notifyModifyRestNotify(notifyId, modifyRestNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyDuplicate | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public MessageNotifyInfoResponse modifyMessageNotify(String notifyId, ModifyMessageNotifyRequest modifyMessageNotifyRequest) throws RestConnectFailed, NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<MessageNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<MessageNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public MessageNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				MessageNotifyInfoResponse result = apiClient.notifyModifyMessageNotify(notifyId, modifyMessageNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyDuplicate | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<NotifyInfoResponse> deleteNotify(String notifyIds) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<NotifyInfoResponse>> proxy = new RestUrlSequentialExecuter<List<NotifyInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<NotifyInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<NotifyInfoResponse> result =  apiClient.notifyDeleteNotify(notifyIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public StatusNotifyInfoResponse getStatusNotify(String notifyId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<StatusNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<StatusNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public StatusNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				StatusNotifyInfoResponse result =  apiClient.notifyGetStatusNotify(notifyId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public EventNotifyInfoResponse getEventNotify(String notifyId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<EventNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<EventNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public EventNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				EventNotifyInfoResponse result =  apiClient.notifyGetEventNotify(notifyId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public MailNotifyInfoResponse getMailNotify(String notifyId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<MailNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<MailNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public MailNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				MailNotifyInfoResponse result =  apiClient.notifyGetMailNotify(notifyId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobNotifyInfoResponse getJobNotify(String notifyId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<JobNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<JobNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public JobNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobNotifyInfoResponse result =  apiClient.notifyGetJobNotify(notifyId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public LogEscalateNotifyInfoResponse getLogEscalateNotify(String notifyId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<LogEscalateNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<LogEscalateNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public LogEscalateNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				LogEscalateNotifyInfoResponse result =  apiClient.notifyGetLogEscalateNotify(notifyId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CommandNotifyInfoResponse getCommandNotify(String notifyId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<CommandNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<CommandNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CommandNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CommandNotifyInfoResponse result =  apiClient.notifyGetCommandNotify(notifyId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public InfraNotifyInfoResponse getInfraNotify(String notifyId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<InfraNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<InfraNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public InfraNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				InfraNotifyInfoResponse result =  apiClient.notifyGetInfraNotify(notifyId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CloudNotifyInfoResponse getCloudNotify(String notifyId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<CloudNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<CloudNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CloudNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CloudNotifyInfoResponse result =  apiClient.notifyGetCloudNotify(notifyId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public RestNotifyInfoResponse getRestNotify(String notifyId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<RestNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<RestNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public RestNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				RestNotifyInfoResponse result =  apiClient.notifyGetRestNotify(notifyId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public MessageNotifyInfoResponse getMessageNotify(String notifyId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<MessageNotifyInfoResponse> proxy = new RestUrlSequentialExecuter<MessageNotifyInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public MessageNotifyInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				MessageNotifyInfoResponse result = apiClient.notifyGetMessageNotify(notifyId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<NotifyInfoResponse> getNotifyList(String ownerRoleId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<NotifyInfoResponse>> proxy = new RestUrlSequentialExecuter<List<NotifyInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<NotifyInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<NotifyInfoResponse> result =  apiClient.notifyGetNotifyList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<StatusNotifyInfoResponse> getStatusNotifyList(String ownerRoleId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<StatusNotifyInfoResponse>> proxy = new RestUrlSequentialExecuter<List<StatusNotifyInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<StatusNotifyInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<StatusNotifyInfoResponse> result =  apiClient.notifyGetStatusNotifyList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<EventNotifyInfoResponse> getEventNotifyList(String ownerRoleId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<EventNotifyInfoResponse>> proxy = new RestUrlSequentialExecuter<List<EventNotifyInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<EventNotifyInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<EventNotifyInfoResponse> result =  apiClient.notifyGetEventNotifyList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MailNotifyInfoResponse> getMailNotifyList(String ownerRoleId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<MailNotifyInfoResponse>> proxy = new RestUrlSequentialExecuter<List<MailNotifyInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<MailNotifyInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<MailNotifyInfoResponse> result =  apiClient.notifyGetMailNotifyList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobNotifyInfoResponse> getJobNotifyList(String ownerRoleId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<JobNotifyInfoResponse>> proxy = new RestUrlSequentialExecuter<List<JobNotifyInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobNotifyInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobNotifyInfoResponse> result =  apiClient.notifyGetJobNotifyList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<LogEscalateNotifyInfoResponse> getLogEscalateNotifyList(String ownerRoleId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<LogEscalateNotifyInfoResponse>> proxy = new RestUrlSequentialExecuter<List<LogEscalateNotifyInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<LogEscalateNotifyInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<LogEscalateNotifyInfoResponse> result =  apiClient.notifyGetLogEscalateNotifyList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CommandNotifyInfoResponse> getCommandNotifyList(String ownerRoleId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<CommandNotifyInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CommandNotifyInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<CommandNotifyInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<CommandNotifyInfoResponse> result =  apiClient.notifyGetCommandNotifyList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<InfraNotifyInfoResponse> getInfraNotifyList(String ownerRoleId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<InfraNotifyInfoResponse>> proxy = new RestUrlSequentialExecuter<List<InfraNotifyInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<InfraNotifyInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<InfraNotifyInfoResponse> result =  apiClient.notifyGetInfraNotifyList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<CloudNotifyInfoResponse> getCloudNotifyList(String ownerRoleId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<CloudNotifyInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CloudNotifyInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<CloudNotifyInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<CloudNotifyInfoResponse> result =  apiClient.notifyGetCloudNotifyList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<RestNotifyInfoResponse> getRestNotifyList(String ownerRoleId) throws RestConnectFailed, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RestNotifyInfoResponse>> proxy = new RestUrlSequentialExecuter<List<RestNotifyInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RestNotifyInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RestNotifyInfoResponse> result =  apiClient.notifyGetRestNotifyList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NotifyNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<NotifyCheckIdResultInfoResponse> checkNotifyId(String notifyIds) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<NotifyCheckIdResultInfoResponse>> proxy = new RestUrlSequentialExecuter<List<NotifyCheckIdResultInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<NotifyCheckIdResultInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<NotifyCheckIdResultInfoResponse> result =  apiClient.notifyCheckNotifyId(notifyIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<NotifyInfoResponse> setNotifyValid(SetNotifyValidRequest setNotifyValidRequest) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, NotifyNotFound, NotifyDuplicate, InvalidSetting {
		RestUrlSequentialExecuter<List<NotifyInfoResponse>> proxy = new RestUrlSequentialExecuter<List<NotifyInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<NotifyInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<NotifyInfoResponse> result =  apiClient.notifySetNotifyValid(setNotifyValidRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | NotifyNotFound | NotifyDuplicate | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public void notify(NotifyAsMonitorRequest notifyAsMonitorRequest) throws RestConnectFailed, InvalidRole, InvalidUserPass, HinemosUnknown, NotifyNotFound, FacilityNotFound, InvalidSetting {
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(this.connectUnit,this.restKind){
			@Override
			public Void executeMethod( DefaultApi apiClient) throws Exception{
				apiClient.notifyNotifyAsMonitor(notifyAsMonitorRequest);
				return null;
			}
		};
		try {
			proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidRole | InvalidUserPass | HinemosUnknown | NotifyNotFound | FacilityNotFound | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public EventDataInfoResponse notifyEvent(NotifyEventRequest notifyEventRequest) throws RestConnectFailed, InvalidRole, InvalidUserPass, InvalidSetting, HinemosUnknown, FacilityNotFound {
		RestUrlSequentialExecuter<EventDataInfoResponse> proxy = new RestUrlSequentialExecuter<EventDataInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public EventDataInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				EventDataInfoResponse result =  apiClient.notifyNotifyEvent(notifyEventRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidRole | InvalidUserPass | InvalidSetting | HinemosUnknown | FacilityNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
}
