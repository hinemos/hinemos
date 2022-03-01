/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.hub.session.HubControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportTransferRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportTransferController extends AbstractImportController<ImportTransferRecordRequest, RecordRegistrationResponse>{
	public ImportTransferController(boolean isRollbackIfAbnormal, List<ImportTransferRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}

	@Override
	protected RecordRegistrationResponse proccssRecord(ImportTransferRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		TransferInfo infoReq = new TransferInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);

		if(importRec.getIsNewRecord()){
			//add
			new HubControllerBean().addTransferInfo(infoReq);
		}else{
			//Modify
			new HubControllerBean().modifyTransferInfo(infoReq);
		}

		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
}
