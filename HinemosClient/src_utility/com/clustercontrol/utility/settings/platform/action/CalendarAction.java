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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.calendar.util.CalendarEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
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
import com.clustercontrol.utility.settings.ui.dialog.ImportProcessDialog;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.ws.calendar.CalendarDuplicate_Exception;
import com.clustercontrol.ws.calendar.HinemosUnknown_Exception;
import com.clustercontrol.ws.calendar.InvalidRole_Exception;
import com.clustercontrol.ws.calendar.InvalidSetting_Exception;
import com.clustercontrol.ws.calendar.InvalidUserPass_Exception;

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
		List<com.clustercontrol.ws.calendar.CalendarInfo> calendarInfoList = null;
		try {
			calendarInfoList = CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getCalendarList(null);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformCalendar (Error)", e);
			return ret;
		}

		List<String> ids = new ArrayList<>();
		for (com.clustercontrol.ws.calendar.CalendarInfo calendarInfo : calendarInfoList) {
			ids.add(calendarInfo.getCalendarId());
		}

		try {
			CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteCalendar(ids);
			log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + ids.toString());
		} catch (WebServiceException e) {
			log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		}
		
		// カレンダパターン定義一覧の取得
		List<com.clustercontrol.ws.calendar.CalendarPatternInfo> calendarPatternInfoList = null;
		try {
			calendarPatternInfoList = CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getCalendarPatternList(null);
			Collections.sort(calendarPatternInfoList, new Comparator<com.clustercontrol.ws.calendar.CalendarPatternInfo>() {
				@Override
				public int compare(
						com.clustercontrol.ws.calendar.CalendarPatternInfo info1,
						com.clustercontrol.ws.calendar.CalendarPatternInfo info2) {
					return info1.getCalPatternId().compareTo(info2.getCalPatternId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformCalendar (Error)", e);
			return ret;
		}

		ids = new ArrayList<>();
		for (com.clustercontrol.ws.calendar.CalendarPatternInfo calendarPatternInfo : calendarPatternInfoList) {
			ids.add(calendarPatternInfo.getCalPatternId());
		}
		
		try {
			CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteCalendarPattern(ids);
			log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + ids.toString());
		} catch (WebServiceException e) {
			log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
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
		List<com.clustercontrol.ws.calendar.CalendarInfo> calendarInfoList = null;
		try {
			calendarInfoList = CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getCalendarList(null);
			Collections.sort(calendarInfoList, new Comparator<com.clustercontrol.ws.calendar.CalendarInfo>() {
				@Override
				public int compare(
						com.clustercontrol.ws.calendar.CalendarInfo info1,
						com.clustercontrol.ws.calendar.CalendarInfo info2) {
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
		for (com.clustercontrol.ws.calendar.CalendarInfo info : calendarInfoList) {
			try {
				com.clustercontrol.ws.calendar.CalendarInfo calendarInfo = CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getCalendar(info.getCalendarId());
				calendar.addCalendarInfo(CalendarConv.getCalendarInfo(calendarInfo));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + info.getCalendarId());
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// カレンダパターン定義一覧の取得
		List<com.clustercontrol.ws.calendar.CalendarPatternInfo> calendarPatternInfoList = null;
		try {
			calendarPatternInfoList = CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getCalendarPatternList(null);
			Collections.sort(calendarPatternInfoList, new Comparator<com.clustercontrol.ws.calendar.CalendarPatternInfo>() {
				@Override
				public int compare(
						com.clustercontrol.ws.calendar.CalendarPatternInfo info1,
						com.clustercontrol.ws.calendar.CalendarPatternInfo info2) {
					return info1.getCalPatternId().compareTo(info2.getCalPatternId());
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
		for (com.clustercontrol.ws.calendar.CalendarPatternInfo calendarPatterInfo : calendarPatternInfoList) {
			try {
				calendarPattern.addCalendarPatternInfo(
						CalendarConv.getCalendarPatternInfo(calendarPatterInfo));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + calendarPatterInfo.getCalPatternId());
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

		if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
	    	log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
	    	log.debug("End Import PlatformCalendar (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
	    }
		
		int ret = 0;
		
		// XMLファイルからの読み込み
		CalendarType calendar = null;
		try {
			calendar = Calendar.unmarshal(new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
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
			calendarPattern = CalendarPattern.unmarshal(new InputStreamReader(new FileInputStream(xmlPattern), "UTF-8"));
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
		for (CalendarPatternInfo info : calendarPattern.getCalendarPatternInfo()) {
			com.clustercontrol.ws.calendar.CalendarPatternInfo calendarPatternInfo = null;
			try {
				calendarPatternInfo = CalendarConv.getCalendarPatternInfoDto(info);
				CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).addCalendarPattern(calendarPatternInfo);
				objectIdList.add(info.getCalendarPatternId());
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + info.getCalendarPatternId());
			} catch (CalendarDuplicate_Exception e) {
				//重複時、インポート処理方法を確認する
				if(!ImportProcessMode.isSameprocess()){
					String[] args = {info.getCalendarPatternId()};
					ImportProcessDialog dialog = new ImportProcessDialog(
							null, Messages.getString("message.import.confirm2", args));
				    ImportProcessMode.setProcesstype(dialog.open());
				    ImportProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(ImportProcessMode.getProcesstype() == ImportProcessDialog.UPDATE){
			    	try {
			    		CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).modifyCalendarPattern(calendarPatternInfo);
			    		objectIdList.add(info.getCalendarPatternId());
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + info.getCalendarPatternId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
			    } else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.SKIP){
			    	log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + info.getCalendarPatternId());
			    } else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
			    	log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			    	ret = SettingConstants.ERROR_INPROCESS;
			    	return ret;
			    }
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidSetting_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidSetting") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN, objectIdList);
		
		// カレンダ定義の登録
		objectIdList = new ArrayList<String>();
		for (CalendarInfo info : calendar.getCalendarInfo()) {
			com.clustercontrol.ws.calendar.CalendarInfo calendarInfo = null;
			try {
				calendarInfo = CalendarConv.getCalendarInfoDto(info);
				CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).addCalendar(calendarInfo);
				objectIdList.add(info.getCalendarId());
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + info.getCalendarId());
			} catch (CalendarDuplicate_Exception e) {
				//重複時、インポート処理方法を確認する
				if(!ImportProcessMode.isSameprocess()){
					String[] args = {info.getCalendarId()};
					ImportProcessDialog dialog = new ImportProcessDialog(
							null, Messages.getString("message.import.confirm2", args));
				    ImportProcessMode.setProcesstype(dialog.open());
				    ImportProcessMode.setSameprocess(dialog.getToggleState());
				}
				
			    if(ImportProcessMode.getProcesstype() == ImportProcessDialog.UPDATE){
			    	try {
			    		CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).modifyCalendar(calendarInfo);
			    		objectIdList.add(info.getCalendarId());
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + info.getCalendarId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
			    } else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.SKIP){
			    	log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + info.getCalendarId());
			    } else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
			    	log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			    	ret = SettingConstants.ERROR_INPROCESS;
			    	return ret;
			    }
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (InvalidSetting_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidSetting") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				continue;
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				continue;
			}
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
			calendar1 = (Calendar) Calendar.unmarshal(new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			calendarPattern1 = (CalendarPattern) CalendarPattern.unmarshal(new InputStreamReader(new FileInputStream(xmlPattern1), "UTF-8"));
			calendar2 = (Calendar) Calendar.unmarshal(new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			calendarPattern2 = (CalendarPattern) CalendarPattern.unmarshal(new InputStreamReader(new FileInputStream(xmlPattern2), "UTF-8"));
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
		
		List<com.clustercontrol.ws.calendar.CalendarInfo> subList = null;
		try {
			subList = CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getCalendarList(null);
		}
		catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " " + e);
			log.debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<CalendarInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getCalendarInfo()));
		for(com.clustercontrol.ws.calendar.CalendarInfo mgrInfo: new ArrayList<>(subList)){
			for(CalendarInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getCalendarId().equals(xmlElement.getCalendarId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(com.clustercontrol.ws.calendar.CalendarInfo info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getCalendarId()};
					DeleteProcessDialog dialog = new DeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.DELETE){
			    	try {
			    		List<String> args = new ArrayList<>();
			    		args.add(info.getCalendarId());
			    		CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteCalendar(args);
			    		log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getCalendarId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + info.getCalendarId(), e1);
					}
			    } else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.SKIP){
			    	log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getCalendarId());
			    } else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.CANCEL){
			    	log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
			    	return;
			    }
			}
		}
	}

	protected void checkDelete(CalendarPatternType xmlElements){
		
		List<com.clustercontrol.ws.calendar.CalendarPatternInfo> subList = null;
		try {
			subList = CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getCalendarPatternList(null);
		}
		catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			log.debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<CalendarPatternInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getCalendarPatternInfo()));
		for(com.clustercontrol.ws.calendar.CalendarPatternInfo mgrInfo: new ArrayList<>(subList)){
			for(CalendarPatternInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getCalPatternId().equals(xmlElement.getCalendarPatternId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(com.clustercontrol.ws.calendar.CalendarPatternInfo info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getCalPatternId()};
					DeleteProcessDialog dialog = new DeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.DELETE){
			    	try {
			    		List<String> args = new ArrayList<>();
			    		args.add(info.getCalPatternId());
			    		CalendarEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteCalendarPattern(args);
			    		log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getCalPatternId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
			    } else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.SKIP){
			    	log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getCalPatternId());
			    } else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.CANCEL){
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
}
