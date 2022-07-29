/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.rest.endpoint.rpa.dto.ModifyRpaManagementToolAccountRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRpaManagementToolAccountRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.session.RpaControllerBean;

public class ImportRpaManagementToolAccountController extends AbstractImportController<ImportRpaManagementToolAccountRecordRequest, RecordRegistrationResponse> {
	
	public ImportRpaManagementToolAccountController(boolean isRollbackIfAbnormal, List<ImportRpaManagementToolAccountRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	@Override
	public  RecordRegistrationResponse proccssRecord( ImportRpaManagementToolAccountRecordRequest importRec) throws Exception {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		
		RpaManagementToolAccount retResponse = null;
		
		if(importRec.getIsNewRecord()){
			// 新規登録
			RestCommonValitater.checkRequestDto(importRec.getImportData());
			importRec.getImportData().correlationCheck();
			
			RpaManagementToolAccount settingInfo = new RpaManagementToolAccount();
			RestBeanUtil.convertBean(importRec.getImportData(), settingInfo);
			
			retResponse = new RpaControllerBean().addRpaAccount(settingInfo);
		}else{
			// 変更
			ModifyRpaManagementToolAccountRequest requestDto = new ModifyRpaManagementToolAccountRequest();
			RestBeanUtil.convertBeanSimple(importRec.getImportData(), requestDto);
			RestCommonValitater.checkRequestDto(requestDto);
			requestDto.correlationCheck(importRec.getImportData().getRpaScopeId());
			
			RpaManagementToolAccount settingInfo = new RpaManagementToolAccount();
			RestBeanUtil.convertBean(requestDto, settingInfo);
			
			settingInfo.setRpaScopeId(importRec.getImportData().getRpaScopeId());
			
			retResponse = new RpaControllerBean().modifyRpaAccount(settingInfo);
		}

		// 処理したデータのIDを返す
		dtoRecRes.setImportKeyValue(retResponse.getRpaScopeId());
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}


}
