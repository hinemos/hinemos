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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.util.MessageConstant;

public class ImportCalendarController extends AbstractImportController<ImportCalendarRecordRequest, RecordRegistrationResponse>{

	private static Log m_log = LogFactory.getLog(ImportCalendarController.class);

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
		for (CalendarDetailInfoRequest tmp : importRec.getImportData().getCalendarDetailList()) {
			CalendarDetailInfo calendarDetailInfo = new CalendarDetailInfo();
			RestBeanUtil.convertBean(tmp,calendarDetailInfo);

			// カレンダ詳細定義 開始時刻
			Date parseDate = null;
			try {
				parseDate = TimeStringConverter.parseTime(tmp.getStartTime());
			} catch (ParseException e) {
				m_log.warn(e);
				// 変換エラー時の InvalidSetting メッセージ作成
				String message = MessageConstant.MESSAGE_INVALID_VALUE.getMessage(
						MessageConstant.CALENDAR_DETAIL.getMessage() + ", " + MessageConstant.START_TIME.getMessage(),
						tmp.getStartTime());
				throw  new InvalidSetting(message);
			}
			Long startTimeLong = parseDate.getTime();
			calendarDetailInfo.setTimeFrom(startTimeLong);

			// カレンダ詳細定義 終了時刻
			try {
				parseDate = TimeStringConverter.parseTime(tmp.getEndTime());
			} catch (ParseException e) {
				m_log.warn(e);
				// 変換エラー時の InvalidSetting メッセージ作成
				String message = MessageConstant.MESSAGE_INVALID_VALUE.getMessage(
						MessageConstant.CALENDAR_DETAIL.getMessage() + ", " + MessageConstant.END_TIME.getMessage(),
						tmp.getEndTime());
				throw  new InvalidSetting(message);
			}
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
