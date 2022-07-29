/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.infra.util;

import java.util.ArrayList;
import java.util.List;

import org.openapitools.client.model.AccessInfoRequest;
import org.openapitools.client.model.AddInfraManagementRequest;
import org.openapitools.client.model.CommandModuleInfoRequest;
import org.openapitools.client.model.CommandModuleInfoResponse;
import org.openapitools.client.model.CreateAccessInfoListForDialogResponse;
import org.openapitools.client.model.CreateSessionRequest;
import org.openapitools.client.model.FileTransferModuleInfoRequest;
import org.openapitools.client.model.FileTransferModuleInfoResponse;
import org.openapitools.client.model.InfraManagementInfoResponse;
import org.openapitools.client.model.ModifyInfraManagementRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.infra.bean.InfraNodeInputConstant;
import com.clustercontrol.util.RestClientBeanUtil;

public class InfraDtoConverter {

	public static void convertInfoToDto(InfraManagementInfoResponse info, AddInfraManagementRequest dto) {
		if (info.getCommandModuleInfoList() != null) {
			for (CommandModuleInfoResponse res : info.getCommandModuleInfoList()) {
				for (CommandModuleInfoRequest req : dto.getCommandModuleInfoList()) {
					if (res.getModuleId().equals(req.getModuleId())) {
						req.setAccessMethodType(CommandModuleInfoRequest.AccessMethodTypeEnum
								.fromValue(res.getAccessMethodType().getValue()));
					}
				}
			}
		}
		if (info.getFileTransferModuleInfoList() != null) {
			for (FileTransferModuleInfoResponse res : info.getFileTransferModuleInfoList()) {
				for (FileTransferModuleInfoRequest req : dto.getFileTransferModuleInfoList()) {
					if (res.getModuleId().equals(req.getModuleId())) {
						req.setSendMethodType(FileTransferModuleInfoRequest.SendMethodTypeEnum
								.fromValue(res.getSendMethodType().getValue()));
					}
				}
			}
		}
	}

	public static void convertInfoToDto(InfraManagementInfoResponse info, ModifyInfraManagementRequest dto) {
		if (info.getCommandModuleInfoList() != null) {
			for (CommandModuleInfoResponse res : info.getCommandModuleInfoList()) {
				for (CommandModuleInfoRequest req : dto.getCommandModuleInfoList()) {
					if (res.getModuleId().equals(req.getModuleId())) {
						req.setAccessMethodType(CommandModuleInfoRequest.AccessMethodTypeEnum
								.fromValue(res.getAccessMethodType().getValue()));
					}
				}
			}
		}
		if (info.getFileTransferModuleInfoList() != null) {
			for (FileTransferModuleInfoResponse res : info.getFileTransferModuleInfoList()) {
				for (FileTransferModuleInfoRequest req : dto.getFileTransferModuleInfoList()) {
					if (res.getModuleId().equals(req.getModuleId())) {
						req.setSendMethodType(FileTransferModuleInfoRequest.SendMethodTypeEnum
								.fromValue(res.getSendMethodType().getValue()));
					}
				}
			}
		}
	}
	
	public static CreateSessionRequest getCreateSessionRequest(String managementId, List<String> moduleIdList, 
			int nodeInputType, List<CreateAccessInfoListForDialogResponse> accessList) throws HinemosUnknown {
		CreateSessionRequest ret = new CreateSessionRequest();
		ret.setManagementId(managementId);
		ret.setModuleIdList(moduleIdList);
		ret.setNodeInputType(convertIntToEnum(nodeInputType));
		if(accessList != null && !accessList.isEmpty()) {
			List<AccessInfoRequest> accessInfoDtoReqList = new ArrayList<>();
			for(CreateAccessInfoListForDialogResponse dtoRes : accessList) {
				AccessInfoRequest accessInfoDtoReq = new AccessInfoRequest();
				RestClientBeanUtil.convertBean(dtoRes, accessInfoDtoReq);
				accessInfoDtoReqList.add(accessInfoDtoReq);
			}
			ret.setAccessList(accessInfoDtoReqList);
		}
		return ret;
	}
	
	private static CreateSessionRequest.NodeInputTypeEnum convertIntToEnum(int i) {
		CreateSessionRequest.NodeInputTypeEnum ret = null;
		switch (i) {
		case InfraNodeInputConstant.TYPE_NODE_PARAM:
			ret = CreateSessionRequest.NodeInputTypeEnum.NODE_PARAM; 
			break;
		case InfraNodeInputConstant.TYPE_INFRA_PARAM:
			ret = CreateSessionRequest.NodeInputTypeEnum.INFRA_PARAM;
			break;
		case InfraNodeInputConstant.TYPE_DIALOG:
			ret = CreateSessionRequest.NodeInputTypeEnum.DIALOG;
			break;
		default:
			break;
		}
		return ret;
	}
}
