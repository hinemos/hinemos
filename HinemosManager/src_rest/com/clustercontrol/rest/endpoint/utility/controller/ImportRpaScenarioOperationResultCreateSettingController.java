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

import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.rest.endpoint.rpa.dto.ModifyRpaScenarioOperationResultCreateSettingRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRpaScenarioOperationResultCreateSettingRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.rpa.session.RpaControllerBean;

public class ImportRpaScenarioOperationResultCreateSettingController extends AbstractImportController<ImportRpaScenarioOperationResultCreateSettingRecordRequest, RecordRegistrationResponse> {
	
	public ImportRpaScenarioOperationResultCreateSettingController(boolean isRollbackIfAbnormal, List<ImportRpaScenarioOperationResultCreateSettingRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	@Override
	public  RecordRegistrationResponse proccssRecord( ImportRpaScenarioOperationResultCreateSettingRecordRequest importRec) throws Exception {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		
		RpaScenarioOperationResultCreateSetting retResponse = null;
		
		if(importRec.getIsNewRecord()){
			// 新規登録
			RestCommonValitater.checkRequestDto(importRec.getImportData());
			importRec.getImportData().correlationCheck();
			
			RpaScenarioOperationResultCreateSetting settingInfo = new RpaScenarioOperationResultCreateSetting();
			RestBeanUtil.convertBean(importRec.getImportData(), settingInfo);
			
			retResponse = new RpaControllerBean().addRpaScenarioOperationResultCreateSetting(settingInfo);
		}else{
			// 変更
			ModifyRpaScenarioOperationResultCreateSettingRequest requestDto = new ModifyRpaScenarioOperationResultCreateSettingRequest();
			RestBeanUtil.convertBeanSimple(importRec.getImportData(), requestDto);
			RestCommonValitater.checkRequestDto(requestDto);
			requestDto.correlationCheck();
			
			RpaScenarioOperationResultCreateSetting settingInfo = new RpaScenarioOperationResultCreateSetting();
			RestBeanUtil.convertBean(requestDto, settingInfo);
			settingInfo.setScenarioOperationResultCreateSettingId(importRec.getImportData().getScenarioOperationResultCreateSettingId());
			// findbugs対応 RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE nullチェックの先を settingInfo.getNotifyId から 変換元につけかえ 
			if(requestDto.getNotifyId() == null || requestDto.getNotifyId().isEmpty() ){
				settingInfo.setNotifyId(new ArrayList<NotifyRelationInfo>());
			}
			
			retResponse = new RpaControllerBean().modifyRpaScenarioOperationResultCreateSetting(settingInfo);
		}

		// 処理したデータのIDを返す
		dtoRecRes.setImportKeyValue(retResponse.getScenarioOperationResultCreateSettingId());
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}


}
