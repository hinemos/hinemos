/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRoleUserRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportRoleUserController extends AbstractImportController<ImportRoleUserRecordRequest, RecordRegistrationResponse> {

	public ImportRoleUserController(boolean isRollbackIfAbnormal, List<ImportRoleUserRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}

	@Override
	public  RecordRegistrationResponse proccssRecord( ImportRoleUserRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();

		List<String> userIdList = importRec.getImportData().getUserIdList();
		new AccessControllerBean().assignUserRole(importRec.getRoleId(),
				userIdList.toArray(new String[userIdList.size()]));

		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}

}
