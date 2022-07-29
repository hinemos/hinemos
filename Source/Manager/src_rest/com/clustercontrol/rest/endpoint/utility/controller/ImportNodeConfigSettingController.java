/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.repository.session.NodeConfigSettingControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportNodeConfigSettingRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportNodeConfigSettingController extends AbstractImportController<ImportNodeConfigSettingRecordRequest, RecordRegistrationResponse>{
	public ImportNodeConfigSettingController(boolean isRollbackIfAbnormal, List<ImportNodeConfigSettingRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	
	@Override
	public RecordRegistrationResponse proccssRecord( ImportNodeConfigSettingRecordRequest importRec ) throws Exception {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		
		NodeConfigSettingInfo infoReq = new NodeConfigSettingInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		
		// ControllerBean呼び出し
		if(importRec.getIsNewRecord()){
			//新規登録
			new NodeConfigSettingControllerBean().addNodeConfigSettingInfo(infoReq);
		}else{
			//変更
			new NodeConfigSettingControllerBean().modifyNodeConfigSettingInfo(infoReq);
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
}
