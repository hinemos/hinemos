/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportNodeRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportNodeController extends AbstractImportController<ImportNodeRecordRequest, RecordRegistrationResponse> {

	public ImportNodeController(boolean isRollbackIfAbnormal, List<ImportNodeRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}

	@Override
	protected RecordRegistrationResponse proccssRecord(ImportNodeRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		NodeInfo infoReq = new NodeInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		infoReq.setFacilityType(FacilityConstant.TYPE_NODE);
		if(importRec.getIsNewRecord()){
			//add
			new RepositoryControllerBean().addNode(infoReq);
		}else{
			//Modify
			new RepositoryControllerBean().modifyNode(infoReq);
		}

		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
}
