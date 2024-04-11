/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.session.InfraControllerBean;
import com.clustercontrol.rest.endpoint.infra.InfraRestEndpoints;
import com.clustercontrol.rest.endpoint.utility.dto.ImportInfraManagementInfoRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportInfraManagementInfoController extends AbstractImportController<ImportInfraManagementInfoRecordRequest, RecordRegistrationResponse> {

	public ImportInfraManagementInfoController(boolean isRollbackIfAbnormal, List<ImportInfraManagementInfoRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	@Override
	public  RecordRegistrationResponse proccssRecord( ImportInfraManagementInfoRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();

		InfraManagementInfo infoReq = new InfraManagementInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		InfraRestEndpoints.convertInfraDtoToInfo(importRec.getImportData(), infoReq);

		if(importRec.getIsNewRecord()){
			new InfraControllerBean().addInfraManagement(infoReq, true);
		}else{
			new InfraControllerBean().modifyInfraManagement(infoReq, true);
		}

		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}

	@Override
	protected void addCallback(JpaTransactionManager jtm) {
		new InfraControllerBean().addImportInfraManagementCallback(jtm);
	}
}
