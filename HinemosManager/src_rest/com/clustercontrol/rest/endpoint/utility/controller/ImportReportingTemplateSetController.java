/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.reporting.bean.TemplateSetInfo;
import com.clustercontrol.reporting.session.ReportingControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportReportingTemplateSetRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportReportingTemplateSetController extends AbstractImportController<ImportReportingTemplateSetRecordRequest, RecordRegistrationResponse> {
	public ImportReportingTemplateSetController(boolean isRollbackIfAbnormal, List<ImportReportingTemplateSetRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	@Override
	public  RecordRegistrationResponse proccssRecord( ImportReportingTemplateSetRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		TemplateSetInfo infoReq = new TemplateSetInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);

		if(importRec.getIsNewRecord()){
			//新規登録
			new ReportingControllerBean().addTemplateSet(infoReq);
		}else{
			//変更
			new ReportingControllerBean().modifyTemplateSet(infoReq);
		}

		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}


}
