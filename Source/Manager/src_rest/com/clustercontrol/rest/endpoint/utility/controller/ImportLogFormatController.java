/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.hub.model.LogFormat;
import com.clustercontrol.hub.session.HubControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportLogFormatRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportLogFormatController extends AbstractImportController<ImportLogFormatRecordRequest,RecordRegistrationResponse>{

	public ImportLogFormatController(boolean isRollbackIfAbnormal, List<ImportLogFormatRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	
	@Override
	public RecordRegistrationResponse proccssRecord( ImportLogFormatRecordRequest importRec ) throws Exception {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		
		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		
		// DTOからINFOへ変換
		LogFormat infoReq = new LogFormat();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		
		// ControllerBean呼び出し
		if(importRec.getIsNewRecord()){
			//新規登録
			new HubControllerBean().addLogFormat(infoReq);
		}else{
			//変更
			new HubControllerBean().modifyLogFormat(infoReq);
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
	
}
