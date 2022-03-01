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

import com.clustercontrol.rest.endpoint.utility.dto.ImportRpaScenarioTagRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;
import com.clustercontrol.rpa.scenario.model.RpaScenarioTag;
import com.clustercontrol.rpa.session.RpaControllerBean;

public class ImportRpaScenarioTagController extends AbstractImportController<ImportRpaScenarioTagRecordRequest, RecordRegistrationResponse> {
	public ImportRpaScenarioTagController(boolean isRollbackIfAbnormal, List<ImportRpaScenarioTagRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	@Override
	public  RecordRegistrationResponse proccssRecord( ImportRpaScenarioTagRecordRequest importRec) throws Exception {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		
		RpaScenarioTag retResponse = null;
		
		if(importRec.getIsNewRecord()){
			// 新規登録
			RestCommonValitater.checkRequestDto(importRec.getImportData());
			importRec.getImportData().correlationCheck();
			
			RpaScenarioTag scenarioTagInfo = new RpaScenarioTag();
			RestBeanUtil.convertBean(importRec.getImportData(), scenarioTagInfo);
			
			retResponse = new RpaControllerBean().addRpaScenarioTag(scenarioTagInfo);
		}else{
			// 変更
			RestCommonValitater.checkRequestDto(importRec.getImportData());
			importRec.getImportData().correlationCheck();
			
			RpaScenarioTag scenarioTagInfo = new RpaScenarioTag();
			RestBeanUtil.convertBean(importRec.getImportData(), scenarioTagInfo);
			
			RpaControllerBean bean = new RpaControllerBean();
			
			//タグ階層の変更の為、一度削除してから再登録する
			List<String> deleteTagIdList = new ArrayList<>();
			deleteTagIdList.add(scenarioTagInfo.getTagId());
			bean.deleteRpaScenarioTag(deleteTagIdList);
			retResponse = bean.addRpaScenarioTag(scenarioTagInfo);
			
			bean.importRpaScenarioTagPath(scenarioTagInfo);
		}

		// 処理したデータのIDを返す
		dtoRecRes.setImportKeyValue(retResponse.getTagId());
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}


}
