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

import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.fault.UnEditableRole;
import com.clustercontrol.rest.endpoint.access.dto.SystemPrivilegeInfoRequestP1;
import com.clustercontrol.rest.endpoint.utility.dto.ImportSystemPrivilegeInfoRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationExceptionResponse;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportSystemPrivilegeInfoController  extends AbstractImportController<ImportSystemPrivilegeInfoRecordRequest, RecordRegistrationResponse> {

	public ImportSystemPrivilegeInfoController(boolean isRollbackIfAbnormal,
			List<ImportSystemPrivilegeInfoRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}

	@Override
	public RecordRegistrationResponse proccssRecord(ImportSystemPrivilegeInfoRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();

		List<SystemPrivilegeInfo> infoReqList = new ArrayList<SystemPrivilegeInfo>();
		
		for (SystemPrivilegeInfoRequestP1 dto : importRec.getImportData().getSystemPrivilegeList()) {
			SystemPrivilegeInfo info = new SystemPrivilegeInfo();
			RestBeanUtil.convertBean(dto, info);
			infoReqList.add(info);
		}

		//replaceのみなので new/update の分岐無し
		try{
			new AccessControllerBean().replaceSystemPrivilegeRole(importRec.getRoleId(), infoReqList);
			
			dtoRecRes.setResult(ImportResultEnum.NORMAL);
		} catch (UnEditableRole e) {
			//UnEditableRoleのエラーについて トランザクション制御の対象から外すために ここで制御（ロールバック不要）
			dtoRecRes.setResult(ImportResultEnum.SKIP);
			dtoRecRes.setExceptionInfo(new RecordRegistrationExceptionResponse(e));
		}
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}

}
