/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.report.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openapitools.client.model.AddReportingScheduleRequest;
import org.openapitools.client.model.ImportReportingInfoRecordRequest;
import org.openapitools.client.model.ImportReportingInfoRequest;
import org.openapitools.client.model.ImportReportingInfoResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.ReportingScheduleResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.ReportingNotFound;
import com.clustercontrol.reporting.util.ReportingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
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
import com.clustercontrol.utility.settings.report.conv.ReportScheduleConv;
import com.clustercontrol.utility.settings.report.xml.Reporting;
import com.clustercontrol.utility.settings.report.xml.ReportingInfo;
import com.clustercontrol.utility.settings.report.xml.ReportingType;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.ImportRecordConfirmer;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;

/**
 * 
 * @param action 動作
 * @param XMLファイルパス
 * 
 * @version 6.1.0
 * @since 6.0.0
 * 
 */
public class ReportScheduleAction {

	/* ロガー */
	protected static Logger log = Logger.getLogger(ReportScheduleAction.class);

	public ReportScheduleAction() throws ConvertorException {
		super();
	}

	/**
	 * レポーティングスケジュール定義情報を全て削除します。<BR>
	 * 
	 * @since 6.0
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearReportSchedule(){
		
		log.debug("Start Clear ReportSchedule ");

		int ret = 0;

		List<ReportingScheduleResponse> reportingInfoList =null;

		try {
			reportingInfoList = ReportingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getReportingScheduleList();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | ReportingNotFound e) {
			log.error(Messages.getString("ReportSchedule.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		for (ReportingScheduleResponse reportingInfo:reportingInfoList){
			try {
				ReportingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.deleteReportingSchedule(reportingInfo.getReportScheduleId());
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " id:" +reportingInfo.getReportScheduleId());
			} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | ReportingNotFound e) {
				log.error(Messages.getString("ReportSchedule.ClearFailed")
						+ " cheduleId = " + reportingInfo.getReportScheduleId() + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// 処理の終了
		log.info(Messages.getString("ReportSchedule.ClearCompleted"));
		
		log.debug("End Clear ReportSchedule");
		return ret;
	}

	/**
	 *	定義情報をマネージャから読み出します。
	 * @return
	 */
	@ExportMethod
	public int exportReportSchedule(String xmlFile) {

		log.debug("Start Export Reporting Schedule ");

		int ret = 0;
		List<ReportingScheduleResponse> reportingInfoList =null;

		try {
			reportingInfoList = ReportingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getReportingScheduleList();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | ReportingNotFound e) {
			log.error(Messages.getString("ReportSchedule.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}

		Reporting reporting = new Reporting();

		for (ReportingScheduleResponse info : reportingInfoList) {
			try{
				reporting.addReportingInfo(ReportScheduleConv.getReporting(info));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + info.getReportScheduleId());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// XMLファイルに出力
		try {
			reporting.setCommon(ReportScheduleConv.versionReportDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			reporting.setSchemaInfo(ReportScheduleConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				reporting.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Export Report Schedule ");
		return ret;
	}
	
	/**
	 * 定義情報をマネージャに投入します。
	 * 
	 * @return
	 */
	@ImportMethod
	public int importReportSchedule(String xmlFile){
		log.debug("Start Import Report Schedule ");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import Report.Schedule (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret = 0;
		
		// FIXME
		// #5672 attibute である reportTitle に含まれる改行が半角スペースに変更されてしまう事象への暫定対処
		// 本質的には reportTitle を attibute ではなく element として定義する必要があるが、
		// マイナーバージョンでは修正前の XML が取り込めなくなってしまうため対応できない
		// 次期メジャーバージョンでは element への変換を検討すること
		StringBuffer sb = new StringBuffer();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"))) {
			String line = in.readLine();
			while ((line = in.readLine()) != null) {
				sb.append("&#xA;");
				sb.append(line);
			}
		} catch (IOException e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import Report.Schedule (Error)");
			return ret;
		}
		// XML宣言後の改行は元に戻す
		String xml = sb.toString().replaceFirst("&#xA;", System.getProperty("line.separator"));
		
		// XMLファイルからの読み込み
		ReportingType report = null;
		try {
			report = XmlMarshallUtil.unmarshall(Reporting.class,new StringReader(xml));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import Report.Schedule (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(report.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		// 定義の登録
		ImportRecordConfirmer<ReportingInfo, ImportReportingInfoRecordRequest, String> confirmer = new ImportRecordConfirmer<ReportingInfo, ImportReportingInfoRecordRequest, String>(
				log, report.getReportingInfo() ) {
			@Override
			protected ImportReportingInfoRecordRequest convertDtoXmlToRestReq(ReportingInfo xmlDto) throws InvalidSetting, HinemosUnknown {
				ReportingScheduleResponse reportingInfo = ReportScheduleConv.getReportingInfoDto(xmlDto);
				ImportReportingInfoRecordRequest dtoRec = new ImportReportingInfoRecordRequest();
				dtoRec.setImportData(new AddReportingScheduleRequest());
				RestClientBeanUtil.convertBean(reportingInfo, dtoRec.getImportData());
				dtoRec.setImportKeyValue(dtoRec.getImportData().getReportScheduleId());
				return dtoRec;
			}
			@Override
			protected Set<String> getExistIdSet() throws Exception {
				Set<String> retSet = new HashSet<String>();
				List<ReportingScheduleResponse> infoList = ReportingRestClientWrapper
						.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getReportingScheduleList();
				for (ReportingScheduleResponse rec : infoList) {
					retSet.add(rec.getReportScheduleId() );
				}
				return retSet;
			}
			@Override
			protected boolean isLackRestReq(ImportReportingInfoRecordRequest restDto) {
				return (restDto == null || restDto.getImportData().getReportScheduleId()  == null);
			}
			@Override
			protected String getKeyValueXmlDto(ReportingInfo xmlDto) {
				return xmlDto.getReportScheduleId();
			}
			@Override
			protected String getId(ReportingInfo xmlDto) {
				return xmlDto.getReportScheduleId();
			}
			@Override
			protected void setNewRecordFlg(ImportReportingInfoRecordRequest restDto, boolean flag) {
				restDto.setIsNewRecord(flag);
			}
		};
		int confirmRet = confirmer.executeConfirm();
		if( confirmRet != SettingConstants.SUCCESS && confirmRet != SettingConstants.ERROR_CANCEL ){
			//変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			log.warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return confirmRet;
		}
		
		// 更新単位の件数毎にインポートメソッドを呼び出し、結果をログ出力
		// API異常発生時はそこで中断、レコード個別の異常発生時はユーザ選択次第で続行
		ImportClientController<ImportReportingInfoRecordRequest, ImportReportingInfoResponse, RecordRegistrationResponse> importController = new ImportClientController<ImportReportingInfoRecordRequest, ImportReportingInfoResponse, RecordRegistrationResponse>(
				log, Messages.getString("report.schedule"), confirmer.getImportRecDtoList(),true) {
			@Override
			protected List<RecordRegistrationResponse> getResRecList(ImportReportingInfoResponse importResponse) {
				return importResponse.getResultList();
			};
			@Override
			protected Boolean getOccurException(ImportReportingInfoResponse importResponse) {
				return importResponse.getIsOccurException();
			};
			@Override
			protected String getReqKeyValue(ImportReportingInfoRecordRequest importRec) {
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
			protected ImportReportingInfoResponse callImportWrapper(List<ImportReportingInfoRecordRequest> importRecList)
					throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
				ImportReportingInfoRequest reqDto = new ImportReportingInfoRequest();
				reqDto.setRecordList(importRecList);
				reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
				return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importReportingInfo(reqDto) ;
			}
			@Override
			protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
				if (responseRec.getExceptionInfo() != null) {
					return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
				}
				return null;
			};
		};
		ret = importController.importExecute();

		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}

		checkDelete(report);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import Report.Schedule ");
		
		return ret;
	}
	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.report.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = ReportScheduleConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.report.xml.SchemaInfo sci = ReportScheduleConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(log, res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	protected void checkDelete(ReportingType xmlElements){
		
		List<ReportingScheduleResponse> subList =null;

		try {
			subList = ReportingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getReportingScheduleList();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | ReportingNotFound e) {
			log.error(Messages.getString("ReportSchedule.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			return ;
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		List<ReportingInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getReportingInfo()));
		for(ReportingScheduleResponse mgrInfo: new ArrayList<>(subList)){
			for(ReportingInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getReportScheduleId().equals(xmlElement.getReportScheduleId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}

		if(subList.size() > 0){
			for(ReportingScheduleResponse info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getReportScheduleId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						ReportingRestClientWrapper
							.getWrapper(UtilityManagerUtil.getCurrentManagerName())
							.deleteReportingSchedule(info.getReportScheduleId());
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getReportScheduleId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getReportScheduleId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
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
	public int diffXml(String xmlFile1, String xmlFile2) throws ConvertorException {
		log.debug("Start Differrence Report Schedule ");

		int ret = 0;
		// XMLファイルからの読み込み
		ReportingType report1 = null;
		ReportingType report2 = null;
		try {
			report1 = XmlMarshallUtil.unmarshall(ReportingType.class,new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			report2 = XmlMarshallUtil.unmarshall(ReportingType.class,new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			sort(report1);
			sort(report2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence Report Schedule (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(report1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		if(!checkSchemaVersion(report2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(report1, report2, ReportingType.class, resultA);
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_1;
			}
			assert resultA.getResultBs().size() == 1;
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
		}
		catch (Exception e) {
			getLogger().error("unexpected: ", e);
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
		getLogger().debug("End Differrence Report Schedule");

		return ret;
	}

	private void sort(ReportingType report) {
		ReportingInfo[] infoList = report.getReportingInfo();
		Arrays.sort(infoList,
			new Comparator<ReportingInfo>() {
				@Override
				public int compare(ReportingInfo info1, ReportingInfo info2) {
					return info1.getReportScheduleId().compareTo(info2.getReportScheduleId());
				}
			});
		report.setReportingInfo(infoList);
	}
	
	public Logger getLogger(){
		return log;
	}
}
