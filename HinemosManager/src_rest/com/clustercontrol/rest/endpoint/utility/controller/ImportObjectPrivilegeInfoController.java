/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.rest.endpoint.access.dto.ObjectPrivilegeInfoRequestP1;
import com.clustercontrol.rest.endpoint.utility.dto.ImportObjectPrivilegeInfoRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportObjectPrivilegeInfoController extends AbstractImportController<ImportObjectPrivilegeInfoRecordRequest, RecordRegistrationResponse> {
	public ImportObjectPrivilegeInfoController(boolean isRollbackIfAbnormal, List<ImportObjectPrivilegeInfoRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}

	@Override
	protected RecordRegistrationResponse proccssRecord(ImportObjectPrivilegeInfoRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();

		//AccessControllerBean() の実装都合上、replace処理のみ
		String objectType = importRec.getImportData().getObjectType();
		String objectId = importRec.getImportData().getObjectId();
		List<ObjectPrivilegeInfo> infoReqList = new ArrayList<>();
		for (ObjectPrivilegeInfoRequestP1 dto : importRec.getImportData().getObjectPrigilegeInfoList()) {
			ObjectPrivilegeInfo info = new ObjectPrivilegeInfo();
			info.setRoleId(dto.getRoleId());
			info.setObjectPrivilege(dto.getObjectPrivilege());
			infoReqList.add(info);
		}
		new AccessControllerBean().replaceObjectPrivilegeInfo(objectType,objectId, infoReqList);
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}

}
