/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.performance.session.PerformanceCollectMasterControllerBean;
import com.clustercontrol.repository.entity.CollectorPlatformMstData;
import com.clustercontrol.rest.endpoint.utility.dto.ImportPlatformMasterRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportPlatformMasterController extends AbstractImportController<ImportPlatformMasterRecordRequest, RecordRegistrationResponse> {
	public ImportPlatformMasterController(boolean isRollbackIfAbnormal, List<ImportPlatformMasterRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	
	@Override
	public RecordRegistrationResponse proccssRecord( ImportPlatformMasterRecordRequest importRec ) throws Exception {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		
		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		
		// DTOからINFOへ変換
		CollectorPlatformMstData infoReq = new CollectorPlatformMstData();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		
		// ControllerBean呼び出し
		if(importRec.getIsNewRecord()){
			//新規登録
			new PerformanceCollectMasterControllerBean().addCollectPlatformMaster(infoReq);
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}

}
