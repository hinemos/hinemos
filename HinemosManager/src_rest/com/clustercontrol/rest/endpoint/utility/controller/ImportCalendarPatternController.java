/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import com.clustercontrol.calendar.model.CalendarPatternInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.fault.CalendarDuplicate;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.endpoint.utility.dto.ImportCalendarPatternRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportCalendarPatternController extends AbstractImportController<ImportCalendarPatternRecordRequest, RecordRegistrationResponse>{

	public ImportCalendarPatternController(boolean isRollbackIfAbnormal, List<ImportCalendarPatternRecordRequest> importList ) {
		super(isRollbackIfAbnormal, importList);
	}
	@Override
	public RecordRegistrationResponse proccssRecord( ImportCalendarPatternRecordRequest importRec ) throws InvalidSetting, HinemosUnknown, CalendarDuplicate, InvalidRole, CalendarNotFound {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		
		CalendarPatternInfo infoReq = new CalendarPatternInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		// ControllerBean呼び出し
		if(importRec.getIsNewRecord()){
			//新規登録
			new CalendarControllerBean().addCalendarPattern(infoReq);
		}else{
			//変更
			new CalendarControllerBean().modifyCalendarPattern(infoReq);
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
}
