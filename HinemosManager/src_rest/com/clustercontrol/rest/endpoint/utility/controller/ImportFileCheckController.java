/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportFileCheckRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportFileCheckController extends AbstractImportController<ImportFileCheckRecordRequest, RecordRegistrationResponse>{
	public ImportFileCheckController (boolean isRollbackIfAbnormal, List<ImportFileCheckRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	
	@Override
	public RecordRegistrationResponse proccssRecord( ImportFileCheckRecordRequest importRec ) throws Exception {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		
		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		
		// DTOからINFOへ変換
		JobFileCheck infoReq = new JobFileCheck();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		infoReq.setType(JobKickConstant.TYPE_FILECHECK);
		
		// ControllerBean呼び出し
		if(importRec.getIsNewRecord()){
			//新規登録
			new JobControllerBean().addFileCheck(infoReq);
		}else{
			//変更
			new JobControllerBean().modifyFileCheck(infoReq);
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}

}
