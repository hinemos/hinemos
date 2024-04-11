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
import com.clustercontrol.reporting.bean.ReportingInfo;
import com.clustercontrol.reporting.session.ReportingControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportReportingInfoRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportReportingInfoController extends AbstractImportController<ImportReportingInfoRecordRequest, RecordRegistrationResponse> {
	public ImportReportingInfoController(boolean isRollbackIfAbnormal, List<ImportReportingInfoRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	@Override
	public  RecordRegistrationResponse proccssRecord( ImportReportingInfoRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		ReportingInfo infoReq = new ReportingInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);

		if(importRec.getIsNewRecord()){
			//新規登録
			new ReportingControllerBean().addReporting(infoReq, true);
		}else{
			//変更
			new ReportingControllerBean().modifyReporting(infoReq, true);
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
		new ReportingControllerBean().addImportReportingCallback(jtm);
	}
}
