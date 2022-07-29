/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NodeMapException;
import com.clustercontrol.nodemap.bean.NodeMapModel;
import com.clustercontrol.nodemap.session.NodeMapControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportNodeMapModelRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportNodeMapModelController extends AbstractImportController<ImportNodeMapModelRecordRequest, RecordRegistrationResponse>{

	public ImportNodeMapModelController(boolean isRollbackIfAbnormal, List<ImportNodeMapModelRecordRequest> importList ) {
		super(isRollbackIfAbnormal, importList);
	}
	@Override
	public RecordRegistrationResponse proccssRecord( ImportNodeMapModelRecordRequest importRec ) throws InvalidSetting, InvalidRole, NodeMapException, HinemosUnknown {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		
		NodeMapModel infoReq = new NodeMapModel();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		
		// ControllerBean呼び出し
		new NodeMapControllerBean().registerNodeMapModel(infoReq);
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}

}
