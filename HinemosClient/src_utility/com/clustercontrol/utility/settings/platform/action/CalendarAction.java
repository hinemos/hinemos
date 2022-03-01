/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openapitools.client.model.AddCalendarPatternRequest;
import org.openapitools.client.model.AddCalendarRequest;
import org.openapitools.client.model.CalendarDetailInfoRequest;
import org.openapitools.client.model.CalendarDetailInfoResponse;
import org.openapitools.client.model.CalendarInfoResponse;
import org.openapitools.client.model.CalendarPatternInfoResponse;
import org.openapitools.client.model.ImportCalendarPatternRecordRequest;
import org.openapitools.client.model.ImportCalendarPatternRequest;
import org.openapitools.client.model.ImportCalendarPatternResponse;
import org.openapitools.client.model.ImportCalendarRecordRequest;
import org.openapitools.client.model.ImportCalendarRequest;
import org.openapitools.client.model.ImportCalendarResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.calendar.util.CalendarRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.difference.CSVUtil;
import com.clustercontrol.utility.difference.DiffUtil;
import com.clustercontrol.utility.difference.ResultA;
import com.clustercontrol.utility.settings.ClearMethod;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.DiffMethod;
import com.clustercontrol.utility.settings.ExportMethod;
import com.clustercontrol.utility.settings.ImportMethod;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.conv.CalendarConv;
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.platform.xml.Calendar;
import com.clustercontrol.utility.settings.platform.xml.CalendarInfo;
import com.clustercontrol.utility.settings.platform.xml.CalendarPattern;
import com.clustercontrol.utility.settings.platform.xml.CalendarPatternInfo;
import com.clustercontrol.utility.settings.platform.xml.CalendarPatternType;
import com.clustercontrol.utility.settings.platform.xml.CalendarType;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.AccountUtil;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.ImportRecordConfirmer;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;

/**
 * カレンダー定義情報をインポート・エクスポート・削除するアクションクラス<br>
 *
 * @version 6.1.0
 * @since 1.0.0
 */
public class CalendarAction {

	protected static Logger log = Logger.getLogger(CalendarAction.class);

	public CalendarAction() throws ConvertorException {
		super();
	}

	/**
	 * 情報をマネージャから削除します。<BR>
	 *
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearCalendar() {

		log.debug("Start Clear PlatformCalendar ");
		int ret = 0;
		// カレンダ定義一覧の取得
		List<CalendarInfoResponse> calendarInfoList = null;
		try {
			calendarInfoList = CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCalendarList(null);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformCalendar (Error)", e);
			return ret;
		}

		List<String> ids = new ArrayList<>();
		for (CalendarInfoResponse calendarInfo : calendarInfoList) {
			ids.add(calendarInfo.getCalendarId());
		}

		if (AccountUtil.isAdministrator(UtilityManagerUtil.getCurrentManagerName())) {
			// ADMINISTRATORS権限がある場合
			try {
				CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteCalendar(String.join(",", ids));
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + ids.toString());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		} else {
			// ADMINISTRATORS権限がない場合
			for (String id : ids) {
				try {
					CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteCalendar(id);
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + id);
				} catch (Exception e) {
					log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			}
		}
		
		// カレンダパターン定義一覧の取得
		List<CalendarPatternInfoResponse> calendarPatternInfoList = null;
		try {
			calendarPatternInfoList = CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCalendarPatternList(null);
			Collections.sort(calendarPatternInfoList, new Comparator<CalendarPatternInfoResponse>() {
				@Override
				public int compare(
						CalendarPatternInfoResponse info1,
						CalendarPatternInfoResponse info2) {
					return info1.getCalendarPatternId().compareTo(info2.getCalendarPatternId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformCalendar (Error)", e);
			return ret;
		}

		ids = new ArrayList<>();
		for (CalendarPatternInfoResponse calendarPatternInfo : calendarPatternInfoList) {
			ids.add(calendarPatternInfo.getCalendarPatternId());
		}
		
		if (AccountUtil.isAdministrator(UtilityManagerUtil.getCurrentManagerName())) {
			// ADMINISTRATORS権限がある場合
			try {
				CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteCalendarPattern(String.join(",", ids));
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + ids.toString());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		} else {
			// ADMINISTRATORS権限がない場合
			for (String id : ids) {
				try {
					CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteCalendarPattern(id);
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + id);
				} catch (Exception e) {
					log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			}
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Clear PlatformCalendar ");
		return ret;
	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 *
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportCalendar(String xmlFile, String xmlPattern) {

		log.debug("Start Export PlatformCalendar ");

		int ret = 0;
		// カレンダ定義一覧の取得
		List<CalendarInfoResponse> calendarInfoList = null;
		try {
			calendarInfoList = CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCalendarList(null);
			Collections.sort(calendarInfoList, new Comparator<CalendarInfoResponse>() {
				@Override
				public int compare(
						CalendarInfoResponse info1,
						CalendarInfoResponse info2) {
					return info1.getCalendarId().compareTo(info2.getCalendarId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformCalendar (Error)", e);
			return ret;
		}
		// カレンダ定義の取得
		Calendar calendar = new Calendar();
		for (CalendarInfoResponse info : calendarInfoList) {
			try {
				CalendarInfoResponse calendarInfo = CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCalendarInfo(info.getCalendarId());
				calendar.addCalendarInfo(CalendarConv.getCalendarInfo(calendarInfo));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + info.getCalendarId());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// カレンダパターン定義一覧の取得
		List<CalendarPatternInfoResponse> calendarPatternInfoList = null;
		try {
			calendarPatternInfoList = CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCalendarPatternList(null);
			Collections.sort(calendarPatternInfoList, new Comparator<CalendarPatternInfoResponse>() {
				@Override
				public int compare(
						CalendarPatternInfoResponse info1,
						CalendarPatternInfoResponse info2) {
					return info1.getCalendarPatternId().compareTo(info2.getCalendarPatternId());
				}
			});
			
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformCalendar (Error)", e);
			return ret;
		}
		// カレンダパターン定義の取得
		CalendarPattern calendarPattern = new CalendarPattern();
		for (CalendarPatternInfoResponse calendarPatterInfo : calendarPatternInfoList) {
			try {
				calendarPattern.addCalendarPatternInfo(
						CalendarConv.getCalendarPatternInfo(calendarPatterInfo));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + calendarPatterInfo.getCalendarPatternId());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}

		// XMLファイルに出力
		try {

			calendar.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			calendar.setSchemaInfo(CalendarConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				calendar.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
		// XMLファイルに出力
		try {

			calendarPattern.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			calendarPattern.setSchemaInfo(CalendarConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlPattern);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				calendarPattern.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}

		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Export PlatformCalendar ");
		return ret;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 *
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 */
	@ImportMethod
	public int importCalendar(String xmlFile, String xmlPattern) {
		log.debug("Start Import PlatformCalendar ");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
	    	log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
	    	log.debug("End Import PlatformCalendar (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
	    }
		
		int ret = 0;
		
		// XMLファイルからの読み込み
		CalendarType calendar = null;
		try {
			calendar = XmlMarshallUtil.unmarshall(CalendarType.class,new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformCalendar (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(calendar.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		// XMLファイルからの読み込み
		CalendarPatternType calendarPattern = null;
		try {
			calendarPattern = XmlMarshallUtil.unmarshall(CalendarPatternType.class,new InputStreamReader(new FileInputStream(xmlPattern), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformCalendar (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(calendarPattern.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		// カレンダパターン定義の登録
		List<String> objectIdList = new ArrayList<String>();
		ImportCalendarPatternRecordConfirmer calendarPatternConfirmer = new ImportCalendarPatternRecordConfirmer( log, calendarPattern.getCalendarPatternInfo() );
		int calendarPatternConfirmerRet = calendarPatternConfirmer.executeConfirm();
		if (calendarPatternConfirmerRet != 0) {
			ret = calendarPatternConfirmerRet;
		}
		// レコードの登録（カレンダパターン）
		if (!(calendarPatternConfirmer.getImportRecDtoList().isEmpty())) {
			ImportCalendarPatternClientController calendarPatternController = new ImportCalendarPatternClientController(log,
					Messages.getString("calendar.pattern"), calendarPatternConfirmer.getImportRecDtoList(), true);
			int notifyControllerRet = calendarPatternController.importExecute();
			for (RecordRegistrationResponse rec: calendarPatternController.getImportSuccessList() ){
				objectIdList.add(rec.getImportKeyValue());
			}
			if (notifyControllerRet != 0) {
				ret = notifyControllerRet;
			}
		}
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN, objectIdList);
		
		// カレンダ定義の登録
		objectIdList = new ArrayList<String>();
		
		ImportCalendarRecordConfirmer calendarConfirmer = new ImportCalendarRecordConfirmer( log, calendar.getCalendarInfo() );
		int calendarConfirmerRet = calendarConfirmer.executeConfirm();
		if (calendarConfirmerRet != 0) {
			ret = calendarConfirmerRet;
		}
		// レコードの登録（カレンダ）
		if (!(calendarConfirmer.getImportRecDtoList().isEmpty())) {
			ImportCalendarClientController calendarController = new ImportCalendarClientController(log,
					Messages.getString("calendar"), calendarConfirmer.getImportRecDtoList(), true);
			int notifyControllerRet = calendarController.importExecute();
			for (RecordRegistrationResponse rec: calendarController.getImportSuccessList() ){
				objectIdList.add(rec.getImportKeyValue());
			}
			if (notifyControllerRet != 0) {
				ret = notifyControllerRet;
			}
		}
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.PLATFORM_CALENDAR, objectIdList);
		
		//差分削除
		checkDelete(calendar);
		checkDelete(calendarPattern);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import PlatformCalendar ");
		return ret;
	}
	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.platform.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = CalendarConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo sci = CalendarConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	/**
	 * 差分比較処理を行います。
	 * XMLファイル２つ（filePath1,filePath2）を比較する。
	 * 差分がある場合：差分をＣＳＶファイルで出力する。
	 * 差分がない場合：出力しない。
	 * 				   または、すでに存在している同一名のＣＳＶファイルを削除する。
	 *
	 * @param xmlFile1 XMLファイル名
	 * @param xmlFile2 XMLファイル名
	 * @return 終了コード
	 * @throws ConvertorException
	 */
	@DiffMethod
	public int diffXml(String xmlFile1, String xmlPattern1, String xmlFile2, String xmlPattern2) throws ConvertorException {
		log.debug("Start Differrence PlatformCalendar ");

		int ret = 0;
		// XMLファイルからの読み込み
		Calendar calendar1 = null;
		Calendar calendar2 = null;
		CalendarPattern calendarPattern1 = null;
		CalendarPattern calendarPattern2 = null;
		try {
			calendar1 = XmlMarshallUtil.unmarshall(Calendar.class,new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			calendarPattern1 = XmlMarshallUtil.unmarshall(CalendarPattern.class,new InputStreamReader(new FileInputStream(xmlPattern1), "UTF-8"));
			calendar2 = XmlMarshallUtil.unmarshall(Calendar.class,new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			calendarPattern2 = XmlMarshallUtil.unmarshall(CalendarPattern.class,new InputStreamReader(new FileInputStream(xmlPattern2), "UTF-8"));
			sort(calendar1);
			sort(calendarPattern1);
			sort(calendar2);
			sort(calendarPattern2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformCalendar (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(calendar1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(calendarPattern1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		if(!checkSchemaVersion(calendar2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(calendarPattern2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(calendar1, calendar2, Calendar.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_1;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlFile2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlFile2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}
			
			resultA = new ResultA();
			//比較処理に渡す
			diff = DiffUtil.diffCheck2(calendarPattern1, calendarPattern2, CalendarPattern.class, resultA);
			assert resultA.getResultBs().size() == 1;
			if (diff){
				ret += SettingConstants.SUCCESS_DIFF_2;
			}
			
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlPattern2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlPattern2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}
		} catch (FileNotFoundException e) {
			log.warn(e.getMessage());
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			log.error("unexpected: ", e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
		finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
		
		// 処理の終了
		if ((ret >= SettingConstants.SUCCESS) && (ret<=SettingConstants.SUCCESS_MAX)){
			log.info(Messages.getString("SettingTools.DiffCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}
		log.debug("End Differrence PlatformCalendar");

		return ret;
	}
	
	private void sort(Calendar calender) {
		CalendarInfo[] infoList = calender.getCalendarInfo();
		Arrays.sort(infoList,
			new Comparator<CalendarInfo>() {
				@Override
				public int compare(CalendarInfo info1, CalendarInfo info2) {
					return info1.getCalendarId().compareTo(info2.getCalendarId());
				}
			});
		 calender.setCalendarInfo(infoList);
	}
	
	private void sort(CalendarPattern calenderPattern) {
		CalendarPatternInfo[] infoList = calenderPattern.getCalendarPatternInfo();
		Arrays.sort(
			infoList,
			new Comparator<CalendarPatternInfo>() {
				@Override
				public int compare(CalendarPatternInfo obj1,CalendarPatternInfo obj2) {
					return obj1.getCalendarPatternId().compareTo(obj2.getCalendarPatternId());
				}
			});
		 calenderPattern.setCalendarPatternInfo(infoList);
	}

	protected void checkDelete(CalendarType xmlElements){
		
		List<CalendarInfoResponse> subList = null;
		try {
			subList = CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCalendarList(null);
		}
		catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " " + e);
			log.debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<CalendarInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getCalendarInfo()));
		for(CalendarInfoResponse mgrInfo: new ArrayList<>(subList)){
			for(CalendarInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getCalendarId().equals(xmlElement.getCalendarId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(CalendarInfoResponse info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getCalendarId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
			    	try {
			    		List<String> args = new ArrayList<>();
			    		args.add(info.getCalendarId());
			    		CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteCalendar(String.join(",", args));
			    		log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getCalendarId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + info.getCalendarId(), e1);
					}
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
			    	log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getCalendarId());
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			    	log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
			    	return;
			    }
			}
		}
	}

	protected void checkDelete(CalendarPatternType xmlElements){
		
		List<CalendarPatternInfoResponse> subList = null;
		try {
			subList = CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCalendarPatternList(null);
		}
		catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			log.debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<CalendarPatternInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getCalendarPatternInfo()));
		for(CalendarPatternInfoResponse mgrInfo: new ArrayList<>(subList)){
			for(CalendarPatternInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getCalendarPatternId().equals(xmlElement.getCalendarPatternId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(CalendarPatternInfoResponse info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getCalendarPatternId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
			    	try {
			    		List<String> args = new ArrayList<>();
			    		args.add(info.getCalendarPatternId());
			    		CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteCalendarPattern(String.join(",", args));
			    		log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getCalendarPatternId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
			    	log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getCalendarPatternId());
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			    	log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
			    	return;
			    }
			}
		}
	}
	
	/**
	 * オブジェクト権限同時インポート
	 * 
	 * @param objectType
	 * @param objectIdList
	 */
	protected void importObjectPrivilege(String objectType, List<String> objectIdList){
		if(ImportProcessMode.isSameObjectPrivilege()){
			ObjectPrivilegeAction.importAccessExtraction(
					ImportProcessMode.getXmlObjectPrivilege(),
					objectType,
					objectIdList,
					log);
		}
	}
	
	public Logger getLogger() {
		return log;
	}
	
	/**
	 * カレンダ インポート向けのレコード確認用クラス
	 * 
	 */
	protected static class ImportCalendarRecordConfirmer extends ImportRecordConfirmer<CalendarInfo, ImportCalendarRecordRequest, String>{
		
		public ImportCalendarRecordConfirmer(Logger logger, CalendarInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportCalendarRecordRequest convertDtoXmlToRestReq(CalendarInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			
			CalendarInfoResponse dto;
			try {
				dto = CalendarConv.getCalendarInfoDto(xmlDto);
			} catch (ParseException e) {
				throw new HinemosUnknown(e.getMessage());
			}
			ImportCalendarRecordRequest dtoRec = new ImportCalendarRecordRequest();
			dtoRec.setImportData(new AddCalendarRequest());
			RestClientBeanUtil.convertBeanSimple(dto, dtoRec.getImportData());
			
			List<CalendarDetailInfoRequest> calendarDetailInfoRequestList = new ArrayList<CalendarDetailInfoRequest>();
			for(CalendarDetailInfoResponse tmp:dto.getCalendarDetailList()){
				CalendarDetailInfoRequest calendarDetailInfoRequest = new CalendarDetailInfoRequest();
				RestClientBeanUtil.convertBeanSimple(tmp,calendarDetailInfoRequest);
				
				//Enum個別セット
				calendarDetailInfoRequest.setDayType(CalendarDetailInfoRequest.DayTypeEnum.valueOf(tmp.getDayType().name()));
				if(tmp.getWeekNo()!=null){
					calendarDetailInfoRequest.setWeekNo(CalendarDetailInfoRequest.WeekNoEnum.valueOf(tmp.getWeekNo().name()));
				} else {
					calendarDetailInfoRequest.setWeekNo(null);
				}
				calendarDetailInfoRequest.setWeekXth(CalendarDetailInfoRequest.WeekXthEnum.valueOf(tmp.getWeekXth().name()));
				
				calendarDetailInfoRequestList.add(calendarDetailInfoRequest);
			}
			dtoRec.getImportData().setCalendarDetailList(calendarDetailInfoRequestList);
			
			dtoRec.setImportKeyValue(dtoRec.getImportData().getCalendarId());
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<CalendarInfoResponse> calendarInfoList = CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCalendarList(null);
			for (CalendarInfoResponse rec : calendarInfoList) {
				retSet.add(rec.getCalendarId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportCalendarRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getCalendarId() == null || restDto.getImportData().getCalendarId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(CalendarInfo xmlDto) {
			return xmlDto.getCalendarId();
		}
		@Override
		protected String getId(CalendarInfo xmlDto) {
			return xmlDto.getCalendarId();
		}
		@Override
		protected void setNewRecordFlg(ImportCalendarRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}
	
	/**
	 * カレンダパターン インポート向けのレコード確認用クラス
	 * 
	 */
	protected static class ImportCalendarPatternRecordConfirmer extends ImportRecordConfirmer<CalendarPatternInfo, ImportCalendarPatternRecordRequest, String>{
		
		public ImportCalendarPatternRecordConfirmer(Logger logger, CalendarPatternInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportCalendarPatternRecordRequest convertDtoXmlToRestReq(CalendarPatternInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			
			CalendarPatternInfoResponse dto;
			try {
				dto = CalendarConv.getCalendarPatternInfoDto(xmlDto);
			} catch (ParseException e) {
				throw new HinemosUnknown(e.getMessage());
			}
			ImportCalendarPatternRecordRequest dtoRec = new ImportCalendarPatternRecordRequest();
			dtoRec.setImportData(new AddCalendarPatternRequest());
			RestClientBeanUtil.convertBeanSimple(dto, dtoRec.getImportData());
			
			dtoRec.setImportKeyValue(dtoRec.getImportData().getCalendarPatternId());
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<CalendarPatternInfoResponse> calendarInfoList = CalendarRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCalendarPatternList(null);
			for (CalendarPatternInfoResponse rec : calendarInfoList) {
				retSet.add(rec.getCalendarPatternId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportCalendarPatternRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getCalendarPatternId() == null || restDto.getImportData().getCalendarPatternId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(CalendarPatternInfo xmlDto) {
			return xmlDto.getCalendarPatternId();
		}
		@Override
		protected String getId(CalendarPatternInfo xmlDto) {
			return xmlDto.getCalendarPatternId();
		}
		@Override
		protected void setNewRecordFlg(ImportCalendarPatternRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}
	
	/**
	 * カレンダー インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportCalendarClientController extends ImportClientController<ImportCalendarRecordRequest, ImportCalendarResponse, RecordRegistrationResponse>{
		
		public ImportCalendarClientController(Logger logger, String importInfoName, List<ImportCalendarRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportCalendarResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportCalendarResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportCalendarRecordRequest importRec) {
			return importRec.getImportKeyValue();
		};

		@Override
		protected String getResKeyValue(RecordRegistrationResponse responseRec) {
			return responseRec.getImportKeyValue();
		};

		@Override
		protected boolean isResNormal(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.NORMAL) ;
		};

		@Override
		protected boolean isResSkip(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.SKIP) ;
		};

		@Override
		protected ImportCalendarResponse callImportWrapper(List<ImportCalendarRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportCalendarRequest reqDto = new ImportCalendarRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importCalendar(reqDto);
		}

		@Override
		protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
			if (responseRec.getExceptionInfo() != null) {
				return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
			}
			return null;
		};

		@Override
		protected void setResultLog( RecordRegistrationResponse responseRec ){
			String keyValue = getResKeyValue(responseRec);
			if ( isResNormal(responseRec) ) {
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : "+ this.importInfoName + ":" + keyValue);
			} else if(isResSkip(responseRec)){
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : " + this.importInfoName + ":" + keyValue);
			} else {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : "+ this.importInfoName + ":" + keyValue + " : "
						+ HinemosMessage.replace(getRestExceptionMessage(responseRec)));
			}
		}
	}
	
	/**
	 * カレンダーパターン インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportCalendarPatternClientController extends ImportClientController<ImportCalendarPatternRecordRequest, ImportCalendarPatternResponse, RecordRegistrationResponse>{
		
		public ImportCalendarPatternClientController(Logger logger, String importInfoName, List<ImportCalendarPatternRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportCalendarPatternResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportCalendarPatternResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportCalendarPatternRecordRequest importRec) {
			return importRec.getImportKeyValue();
		};

		@Override
		protected String getResKeyValue(RecordRegistrationResponse responseRec) {
			return responseRec.getImportKeyValue();
		};

		@Override
		protected boolean isResNormal(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.NORMAL) ;
		};

		@Override
		protected boolean isResSkip(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.SKIP) ;
		};

		@Override
		protected ImportCalendarPatternResponse callImportWrapper(List<ImportCalendarPatternRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportCalendarPatternRequest reqDto = new ImportCalendarPatternRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importCalendarPattern(reqDto);
		}

		@Override
		protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
			if (responseRec.getExceptionInfo() != null) {
				return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
			}
			return null;
		};

		@Override
		protected void setResultLog( RecordRegistrationResponse responseRec ){
			String keyValue = getResKeyValue(responseRec);
			if ( isResNormal(responseRec) ) {
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : "+ this.importInfoName + ":" + keyValue);
			} else if(isResSkip(responseRec)){
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : " + this.importInfoName + ":" + keyValue);
			} else {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : "+ this.importInfoName + ":" + keyValue + " : "
						+ HinemosMessage.replace(getRestExceptionMessage(responseRec)));
			}
		}
	}

}
