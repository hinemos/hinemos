/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportUserRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportUserController extends AbstractImportController<ImportUserRecordRequest, RecordRegistrationResponse> {

	private static Log m_log = LogFactory.getLog(ImportUserController.class);

	public ImportUserController(boolean isRollbackIfAbnormal, List<ImportUserRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	@Override
	public RecordRegistrationResponse proccssRecord( ImportUserRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		UserInfo infoReq = new UserInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);

		if(importRec.getIsNewRecord()){
			//新規登録
			new AccessControllerBean().addUserInfo(infoReq);
		}else{
			//変更
			new AccessControllerBean().modifyUserInfo(infoReq);
		}
		if( importRec.getPassword() != null && !(importRec.getPassword().isEmpty()) ){
			m_log.debug("proccssRecord () : changePassword " );
			new AccessControllerBean().changePassword(infoReq.getUserId(), importRec.getPassword());
		}
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}

}
