/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.rest.endpoint.utility.dto.ImportRpaScenarioCoefficientPatternRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;
import com.clustercontrol.rpa.model.RpaScenarioCoefficientPattern;
import com.clustercontrol.rpa.session.RpaControllerBean;

public class ImportRpaScenarioCoefficientPatternController extends AbstractImportController<ImportRpaScenarioCoefficientPatternRecordRequest, RecordRegistrationResponse> {
	
	public ImportRpaScenarioCoefficientPatternController(boolean isRollbackIfAbnormal, List<ImportRpaScenarioCoefficientPatternRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	@Override
	public  RecordRegistrationResponse proccssRecord( ImportRpaScenarioCoefficientPatternRecordRequest importRec) throws Exception {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		
		RpaScenarioCoefficientPattern retResponse = null;
		
		if(importRec.getIsNewRecord()){
			// 新規登録
			RestCommonValitater.checkRequestDto(importRec.getImportData());
			importRec.getImportData().correlationCheck();
			
			RpaScenarioCoefficientPattern patternInfo = new RpaScenarioCoefficientPattern();
			RestBeanUtil.convertBean(importRec.getImportData(), patternInfo);
			
			retResponse = new RpaControllerBean().addRpaScenarioCoefficientPattern(patternInfo);
		}

		// 処理したデータのIDを返す
		if (retResponse != null) {
			dtoRecRes.setImportKeyValue(retResponse.getRpaToolEnvId());
		}else{
			dtoRecRes.setImportKeyValue(importRec.getImportData().getRpaToolEnvId());
		}
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}


}
