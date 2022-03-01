/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.clustercontrol.calendar.model.CalendarDetailInfo;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.calendar.util.TimeStringConverter;
import com.clustercontrol.fault.CalendarDuplicate;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.endpoint.calendar.dto.CalendarDetailInfoRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportCalendarRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;
import com.clustercontrol.util.HinemosTime;

public class ImportCalendarController extends AbstractImportController<ImportCalendarRecordRequest, RecordRegistrationResponse>{

	public ImportCalendarController(boolean isRollbackIfAbnormal, List<ImportCalendarRecordRequest> importList ) {
		super(isRollbackIfAbnormal, importList);
	}
	@Override
	public RecordRegistrationResponse proccssRecord( ImportCalendarRecordRequest importRec ) throws CalendarDuplicate, InvalidSetting, InvalidRole, HinemosUnknown, CalendarNotFound, ParseException {
		
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		
		CalendarInfo infoReq = new CalendarInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		
		// 個別セット
		List<CalendarDetailInfo> calendarDetailList = new ArrayList<CalendarDetailInfo>();
		for(CalendarDetailInfoRequest tmp:importRec.getImportData().getCalendarDetailList()){
			CalendarDetailInfo calendarDetailInfo = new CalendarDetailInfo();
			RestBeanUtil.convertBean(tmp,calendarDetailInfo);
			
			Date parseDate = TimeStringConverter.parseTime(tmp.getStartTime());
			Long startTimeLong = parseDate.getTime();
			calendarDetailInfo.setTimeFrom(startTimeLong);
			
			parseDate = TimeStringConverter.parseTime(tmp.getEndTime());
			Long timeToLong = parseDate.getTime();
			calendarDetailInfo.setTimeTo(timeToLong);
			
			calendarDetailList.add(calendarDetailInfo);
		}
		infoReq.setCalendarDetailList(calendarDetailList);
		
		// ControllerBean呼び出し
		if(importRec.getIsNewRecord()){
			//新規登録
			new CalendarControllerBean().addCalendar(infoReq);
		}else{
			//変更
			new CalendarControllerBean().modifyCalendar(infoReq);
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
}
