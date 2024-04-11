/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.util;

import java.io.File;
import java.util.List;

import org.openapitools.client.model.AddBgImageRequest;
import org.openapitools.client.model.AddIconImageRequest;
import org.openapitools.client.model.CheckPublishResponse;
import org.openapitools.client.model.DownloadNodeConfigFileRequest;
import org.openapitools.client.model.ExistBgImageResponse;
import org.openapitools.client.model.ExistIconImageResponse;
import org.openapitools.client.model.MapBgImageInfoResponse;
import org.openapitools.client.model.MapIconImageInfoResponse;
import org.openapitools.client.model.NodeMapModelResponse;
import org.openapitools.client.model.RegisterNodeMapModelRequest;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.BgFileNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NodeMapException;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class NodeMapRestClientWrapper implements ICheckPublishRestClientWrapper {

	private RestConnectUnit connectUnit;
	
	private final RestKind restKind = RestKind.NodeMapRestEndpoints;

	public static NodeMapRestClientWrapper getWrapper(String managerName) {
		return new NodeMapRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public NodeMapRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}
	
	public NodeMapModelResponse registerNodeMapModel(RegisterNodeMapModelRequest registerNodeMapModelRequest) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, NodeMapException {
		RestUrlSequentialExecuter<NodeMapModelResponse> proxy = new RestUrlSequentialExecuter<NodeMapModelResponse>(this.connectUnit,this.restKind){
			@Override
			public NodeMapModelResponse executeMethod( DefaultApi apiClient) throws Exception{
				NodeMapModelResponse result =  apiClient.nodemapRegisterNodeMapModel(registerNodeMapModelRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting | NodeMapException def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public NodeMapModelResponse getNodeMapModel(String facilityId) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, NodeMapException {
		RestUrlSequentialExecuter<NodeMapModelResponse> proxy = new RestUrlSequentialExecuter<NodeMapModelResponse>(this.connectUnit,this.restKind){
			@Override
			public NodeMapModelResponse executeMethod( DefaultApi apiClient) throws Exception{
				NodeMapModelResponse result =  apiClient.nodemapGetNodeMapModel(facilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | NodeMapException def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public File downloadNodeConfigFile(DownloadNodeConfigFileRequest req) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(this.connectUnit,this.restKind){
			@Override
			public File executeMethod( DefaultApi apiClient) throws Exception{
				File result =  apiClient.nodemapDownloadNodeConfigFile(req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting  def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public File downloadBgImage(String filename) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, NodeMapException, BgFileNotFound {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(this.connectUnit,this.restKind){
			@Override
			public File executeMethod( DefaultApi apiClient) throws Exception{
				File result =  apiClient.nodemapDownloadBgImage(filename);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting | NodeMapException | BgFileNotFound def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public MapBgImageInfoResponse addBgImage(File file, AddBgImageRequest addBgImageRequest) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, NodeMapException {
		RestUrlSequentialExecuter<MapBgImageInfoResponse> proxy = new RestUrlSequentialExecuter<MapBgImageInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public MapBgImageInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				AddBgImageRequest req = new com.clustercontrol.nodemap.util.AddBgImageRequestEx();
				req.setFilename(addBgImageRequest.getFilename());
				MapBgImageInfoResponse result =  apiClient.nodemapAddBgImage(file, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting | NodeMapException def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MapBgImageInfoResponse> getBgImageFilename() throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<MapBgImageInfoResponse>> proxy = new RestUrlSequentialExecuter<List<MapBgImageInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<MapBgImageInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<MapBgImageInfoResponse> result =  apiClient.nodemapGetBgImageFilename();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown  def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public ExistBgImageResponse existBgImage(String filename) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ExistBgImageResponse> proxy = new RestUrlSequentialExecuter<ExistBgImageResponse>(this.connectUnit,this.restKind){
			@Override
			public ExistBgImageResponse executeMethod( DefaultApi apiClient) throws Exception{
				ExistBgImageResponse result =  apiClient.nodemapExistBgImage(filename);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public File downloadIconImage(String filename) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, NodeMapException, IconFileNotFound {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(this.connectUnit,this.restKind){
			@Override
			public File executeMethod( DefaultApi apiClient) throws Exception{
				File result =  apiClient.nodemapDownloadIconImage(filename);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting | NodeMapException | IconFileNotFound def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public MapIconImageInfoResponse addIconImage(File file, AddIconImageRequest addIconImageRequest) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, NodeMapException {
		RestUrlSequentialExecuter<MapIconImageInfoResponse> proxy = new RestUrlSequentialExecuter<MapIconImageInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public MapIconImageInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				AddIconImageRequest req = new com.clustercontrol.nodemap.util.AddIconImageRequestEx();
				req.setFilename(addIconImageRequest.getFilename());
				MapIconImageInfoResponse result =  apiClient.nodemapAddIconImage(file, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting | NodeMapException def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MapIconImageInfoResponse> getIconImageFilename() throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<MapIconImageInfoResponse>> proxy = new RestUrlSequentialExecuter<List<MapIconImageInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<MapIconImageInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<MapIconImageInfoResponse> result =  apiClient.nodemapGetIconImageFilename();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown  def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public ExistIconImageResponse existIconImage(String filename) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ExistIconImageResponse> proxy = new RestUrlSequentialExecuter<ExistIconImageResponse>(this.connectUnit,this.restKind){
			@Override
			public ExistIconImageResponse executeMethod( DefaultApi apiClient) throws Exception{
				ExistIconImageResponse result =  apiClient.nodemapExistIconImage(filename);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	@Override
	public CheckPublishResponse checkPublish()
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<CheckPublishResponse> proxy = new RestUrlSequentialExecuter<CheckPublishResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public CheckPublishResponse executeMethod(DefaultApi apiClient) throws Exception {
				CheckPublishResponse result = apiClient.nodemapCheckPublish();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed def) {//通信異常
			throw new RestConnectFailed(Messages.getString("message.hinemos.failure.transfer") + ", " + HinemosMessage.replace(def.getMessage()), def);
		} catch (InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (UrlNotFound e) {
			// UrlNotFoundが返された場合エンドポイントがPublishされていないためメッセージを設定する
			throw new HinemosUnknown(Messages.getString("message.expiration.term"), e);
		} catch (Exception unknown) {
			throw new HinemosUnknown(Messages.getString("message.unexpected_error") + "," + HinemosMessage.replace(unknown.getMessage()), unknown);
		}
	}

	
	
}
