/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.commons.util.ObjectValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.maintenance.model.HinemosPropertyInfo;
import com.clustercontrol.maintenance.session.HinemosPropertyControllerBean;
import com.clustercontrol.rest.endpoint.common.dto.enumtype.HinemosPropertyTypeEnum;
import com.clustercontrol.rest.endpoint.utility.dto.ImportHinemosPropertyRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.util.MessageConstant;

public class ImportHinemosPropertyController extends AbstractImportController<ImportHinemosPropertyRecordRequest, RecordRegistrationResponse> {

	public ImportHinemosPropertyController (boolean isRollbackIfAbnormal, List<ImportHinemosPropertyRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	
	@Override
	public RecordRegistrationResponse proccssRecord( ImportHinemosPropertyRecordRequest importRec ) throws Exception {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		
		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		
		// DTOからINFOへ変換
		HinemosPropertyInfo infoReq = new HinemosPropertyInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		
		if (HinemosPropertyTypeEnum.STRING.equals(importRec.getImportData().getType())) {
			infoReq.setValueType(HinemosPropertyTypeEnum.STRING.getCode());
			infoReq.setValueString(importRec.getImportData().getValue());
		} else if (HinemosPropertyTypeEnum.NUMERIC.equals(importRec.getImportData().getType())) {
			infoReq.setValueType(HinemosPropertyTypeEnum.NUMERIC.getCode());
			if (!ObjectValidator.isEmptyString(importRec.getImportData().getValue())) {
				try {
					infoReq.setValueNumeric(Long.valueOf(importRec.getImportData().getValue()));
				} catch (NumberFormatException e) {
					throw new InvalidSetting(
							MessageConstant.MESSAGE_INPUT_BETWEEN.getMessage(MessageConstant.VALUE.getMessage(),
									String.valueOf(Long.MIN_VALUE), String.valueOf(Long.MAX_VALUE)));
				}
			}
		} else if (HinemosPropertyTypeEnum.BOOLEAN.equals(importRec.getImportData().getType())) {
			infoReq.setValueType(HinemosPropertyTypeEnum.BOOLEAN.getCode());
			infoReq.setValueBoolean(Boolean.valueOf(importRec.getImportData().getValue()));
		}
		
		// ControllerBean呼び出し
		if(importRec.getIsNewRecord()){
			//新規登録
			new HinemosPropertyControllerBean().addHinemosProperty(infoReq);
		}else{
			//変更
			new HinemosPropertyControllerBean().modifyHinemosProperty(infoReq);
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
}
