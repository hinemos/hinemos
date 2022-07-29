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

import com.clustercontrol.jmx.model.JmxMasterInfo;
import com.clustercontrol.jmx.session.JmxMasterControllerBean;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.JmxMasterInfoRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJmxMasterRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportJmxMasterController extends AbstractImportController<ImportJmxMasterRecordRequest, RecordRegistrationResponse> {

	public ImportJmxMasterController(boolean isRollbackIfAbnormal, List<ImportJmxMasterRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	@Override
	public  RecordRegistrationResponse proccssRecord( ImportJmxMasterRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();

		List<JmxMasterInfo> infoReqList = new ArrayList<>();
		if (importRec.getImportData().getJmxMasterInfoList() != null) {
			for (JmxMasterInfoRequest dto : importRec.getImportData().getJmxMasterInfoList()) {
				JmxMasterInfo infoReq = new JmxMasterInfo();
				RestBeanUtil.convertBean(dto, infoReq);
				infoReqList.add(infoReq);
			}
		}

		new JmxMasterControllerBean().addJmxMasterList(infoReqList);

		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}

}
