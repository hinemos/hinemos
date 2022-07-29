/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.notify.restaccess.model.RestAccessInfo;
import com.clustercontrol.notify.restaccess.session.RestAccessInfoControllerBean;
import com.clustercontrol.rest.endpoint.common.CommonRestEndpoints;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRestAccessInfoRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportRestAccessInfoController extends AbstractImportController<ImportRestAccessInfoRecordRequest,RecordRegistrationResponse>{

	public ImportRestAccessInfoController(boolean isRollbackIfAbnormal, List<ImportRestAccessInfoRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	
	public RecordRegistrationResponse proccssRecord( ImportRestAccessInfoRecordRequest importRec ) throws Exception {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		
		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		
		// DTOからINFOへ変換
		RestAccessInfo infoReq = new RestAccessInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		CommonRestEndpoints.paddingRestAccessInfo(infoReq);
		// ControllerBean呼び出し
		if(importRec.getIsNewRecord()){
			//新規登録
			new RestAccessInfoControllerBean().addRestAccess(infoReq);
		}else{
			//変更
			new RestAccessInfoControllerBean().modifyRestAccess(infoReq);
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
}
