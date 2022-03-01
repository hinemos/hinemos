/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.mail.session.MailTemplateControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportMailTemplateRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportMailTemplateController extends AbstractImportController<ImportMailTemplateRecordRequest,RecordRegistrationResponse>{

	public ImportMailTemplateController(boolean isRollbackIfAbnormal, List<ImportMailTemplateRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	
	public RecordRegistrationResponse proccssRecord( ImportMailTemplateRecordRequest importRec ) throws Exception {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		
		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		
		// DTOからINFOへ変換
		MailTemplateInfo infoReq = new MailTemplateInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		
		// ControllerBean呼び出し
		if(importRec.getIsNewRecord()){
			//新規登録
			new MailTemplateControllerBean().addMailTemplate(infoReq);
		}else{
			//変更
			new MailTemplateControllerBean().modifyMailTemplate(infoReq);
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
}
