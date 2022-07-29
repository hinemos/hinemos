/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.fault.UnEditableRole;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRoleRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationExceptionResponse;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportRoleController extends AbstractImportController<ImportRoleRecordRequest, RecordRegistrationResponse> {

	public ImportRoleController(boolean isRollbackIfAbnormal, List<ImportRoleRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	@Override
	public  RecordRegistrationResponse proccssRecord( ImportRoleRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		RoleInfo infoReq = new RoleInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		//システムロールは無視（ スキップ扱い UnEditableRoleで返却）
		if("ADMINISTRATORS".equals(infoReq.getRoleId()) ||
				"ALL_USERS".equals(infoReq.getRoleId()) ||
				"INTERNAL".equals(infoReq.getRoleId())){
			dtoRecRes.setResult(ImportResultEnum.SKIP);
			dtoRecRes.setExceptionInfo(new RecordRegistrationExceptionResponse(new UnEditableRole())); 
			return dtoRecRes;
		}

		try{
			if(importRec.getIsNewRecord()){
				//新規登録
				new AccessControllerBean().addRoleInfo(infoReq);
			}else{
				//変更
				new AccessControllerBean().modifyRoleInfo(infoReq);
			}
		}catch(UnEditableRole e){
			// 編集不可なロールはスキップ扱い
			dtoRecRes.setResult(ImportResultEnum.SKIP);
			dtoRecRes.setExceptionInfo(new RecordRegistrationExceptionResponse(e)); 
			return dtoRecRes;
		}

		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}

}
