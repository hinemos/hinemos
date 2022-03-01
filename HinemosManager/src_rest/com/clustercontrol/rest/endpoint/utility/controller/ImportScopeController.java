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

import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityIdConstant;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rest.endpoint.utility.dto.ImportScopeRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportScopeController extends AbstractImportController<ImportScopeRecordRequest, RecordRegistrationResponse> {

	public ImportScopeController(boolean isRollbackIfAbnormal, List<ImportScopeRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}

	@Override
	public  RecordRegistrationResponse proccssRecord( ImportScopeRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		//Scope情報変更
		ScopeInfo infoReq = new ScopeInfo();
		RestBeanUtil.convertBean(importRec.getImportData().getScopeInfo(), infoReq);
		infoReq.setFacilityType(FacilityConstant.TYPE_SCOPE);
		if(importRec.getImportData().getParentFacilityId().equals(FacilityIdConstant.ROOT)){
			importRec.getImportData().setParentFacilityId(null);
		}
		if(importRec.getIsNewRecord()){
			//新規登録
			new RepositoryControllerBean().addScope(importRec.getImportData().getParentFacilityId(), infoReq);
		}else{
			//変更
			new RepositoryControllerBean().modifyScopeWithParent(infoReq,importRec.getImportData().getParentFacilityId());
		}
		//割り当てノード変更（差分反映）
		List<NodeInfo> setupNodes = new RepositoryControllerBean().getNodeList(infoReq.getFacilityId(),1);
		if(!(setupNodes.isEmpty())){
			//なくなった割り当てがあれば削除
			List<String> releaseNodeIdStrings = new ArrayList<String>();
			for(NodeInfo rec :setupNodes){
				if(!(importRec.getAssignFacilityIdList().contains(rec.getFacilityId()))){
					releaseNodeIdStrings.add(rec.getFacilityId());
				}
			}
			if(!(releaseNodeIdStrings.isEmpty())){
				new RepositoryControllerBean().releaseNodeScope(infoReq.getFacilityId(), releaseNodeIdStrings.toArray(new String[0]));
			}
		}
		//新しい割り当てを反映
		if(importRec.getAssignFacilityIdList() !=null && !(importRec.getAssignFacilityIdList().isEmpty())){
			new RepositoryControllerBean().assignNodeScope(infoReq.getFacilityId(), importRec.getAssignFacilityIdList().toArray(new String[0]));
		}
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
}
