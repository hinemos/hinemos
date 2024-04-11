/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.session.MaintenanceControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportMaintenanceRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportMaintenanceController extends AbstractImportController<ImportMaintenanceRecordRequest, RecordRegistrationResponse>{

	public ImportMaintenanceController(boolean isRollbackIfAbnormal, List<ImportMaintenanceRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	
	public RecordRegistrationResponse proccssRecord( ImportMaintenanceRecordRequest importRec ) throws Exception {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		
		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		
		// DTOからINFOへ変換
		MaintenanceInfo infoReq = new MaintenanceInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		
		// ControllerBean呼び出し
		if(importRec.getIsNewRecord()){
			//新規登録
			new MaintenanceControllerBean().addMaintenance(infoReq, true);
		}else{
			//変更
			new MaintenanceControllerBean().modifyMaintenance(infoReq, true);
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}

	@Override
	protected void addCallback(JpaTransactionManager jtm) {
		new MaintenanceControllerBean().addImportMaintenanceCallback(jtm);
	}
}
