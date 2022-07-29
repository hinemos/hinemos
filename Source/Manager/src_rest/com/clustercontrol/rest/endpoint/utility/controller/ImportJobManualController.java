/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.jobmanagement.bean.JobKick;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJobManualRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportJobManualController extends AbstractImportController<ImportJobManualRecordRequest, RecordRegistrationResponse>{
	public ImportJobManualController (boolean isRollbackIfAbnormal, List<ImportJobManualRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	
	@Override
	public RecordRegistrationResponse proccssRecord( ImportJobManualRecordRequest importRec ) throws Exception {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		
		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		
		// DTOからINFOへ変換
		JobKick infoReq = new JobKick();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		infoReq.setType(JobKickConstant.TYPE_MANUAL);
		
		// ControllerBean呼び出し
		if(importRec.getIsNewRecord()){
			//新規登録
			new JobControllerBean().addJobManual(infoReq);
		}else{
			//変更
			new JobControllerBean().modifyJobManual(infoReq);
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}

}
