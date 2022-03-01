/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.report.action;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openapitools.client.model.AddTemplateSetRequest;
import org.openapitools.client.model.ImportReportingTemplateSetResponse;
import org.openapitools.client.model.ImportReportingTemplateSetRecordRequest;
import org.openapitools.client.model.ImportReportingTemplateSetRequest;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.TemplateSetInfoResponse;
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
import com.clustercontrol.utility.settings.report.conv.ReportTemplateConv;
import com.clustercontrol.utility.settings.report.xml.ReportTemplate;
import com.clustercontrol.utility.settings.report.xml.ReportTemplateType;
import com.clustercontrol.utility.settings.report.xml.TemplateInfo;
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
 * @param XMLファイルパス（ユーザ情報定義の入力元）
 * 
 * @version 6.1.0
 * @since 6.0.0
 * 
 * 
 */
public class ReportTemplateAction {

	/* ロガー */
	protected static Logger log = Logger.getLogger(ReportTemplateAction.class);

	public ReportTemplateAction() throws ConvertorException {
		super();
	}
	
	/**
	 * レポーティングテンプレート定義情報を全て削除します。<BR>
	 * 
	 * @since 6.0
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearReportTemplate(){
		
		log.debug("Start Clear ReportTemplate ");
		int ret = 0;

		List<TemplateSetInfoResponse> templateSetInfoList =null;
		ReportingRestClientWrapper wrapper = ReportingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		try {
			templateSetInfoList = wrapper.getTemplateSetList(null);
		} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass | ReportingNotFound e) {
			log.error(Messages.getString("ReportTemplate.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		for (TemplateSetInfoResponse templateSetInfo:templateSetInfoList){
			try {
				wrapper.deleteTemplateSet(templateSetInfo.getTemplateSetId());
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + templateSetInfo.getTemplateSetId());
			} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass | ReportingNotFound e) {
				log.error(Messages.getString("ReportTemplate.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// 処理の終了
		log.info(Messages.getString("ReportTemplate.ClearCompleted"));
		
		log.debug("End Clear ReportTemplate");
		return ret;
	}
	/**
	 *	定義情報をマネージャから読み出します。
	 * @return
	 */
	@ExportMethod
	public int exportReportTemplate(String xmlFile) {

		log.debug("Start Export Reporting Template ");

		int ret = 0;
		ReportingRestClientWrapper wrapper =
				ReportingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		
		List<TemplateSetInfoResponse> templateSetInfoList =null;
		
		try {
			templateSetInfoList = wrapper.getTemplateSetList(null);
		} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass | ReportingNotFound e) {
			log.error(Messages.getString("ReportTemplate.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		ReportTemplate reportTemplate = new ReportTemplate();
		
		for (TemplateSetInfoResponse info : templateSetInfoList) {
			try{
				info = wrapper.getTemplateSetInfo(info.getTemplateSetId());
				reportTemplate.addTemplateInfo(ReportTemplateConv.getTemplate(info));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + info.getTemplateSetId());
			} catch (RestConnectFailed e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// XMLファイルに出力
		try {
			reportTemplate.setCommon(ReportTemplateConv.versionReportDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			reportTemplate.setSchemaInfo(ReportTemplateConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				reportTemplate.marshal(osw);
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
		log.debug("End Export Report Template ");
		return ret;
	}
	
	/**
	 * 収集項目定義情報をマネージャに投入します。
	 * 
	 * @return
	 */
	@ImportMethod
	public int importReportTemplate(String xmlFile){
		log.debug("Start Import Report Template ");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import Report.Template (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret = 0;
		
		// XMLファイルからの読み込み
		ReportTemplateType template = null;
		try {
			template = XmlMarshallUtil.unmarshall(ReportTemplateType.class,new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import Report.Template (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(template.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		// 定義の登録
		ImportRecordConfirmer<TemplateInfo, ImportReportingTemplateSetRecordRequest, String> confirmer = new ImportRecordConfirmer<TemplateInfo, ImportReportingTemplateSetRecordRequest, String>(
				log, template.getTemplateInfo() ) {
			@Override
			protected ImportReportingTemplateSetRecordRequest convertDtoXmlToRestReq(TemplateInfo xmlDto) throws InvalidSetting, HinemosUnknown {
				TemplateSetInfoResponse reportingInfo = ReportTemplateConv.getTemplateInfoDto(xmlDto);
				ImportReportingTemplateSetRecordRequest dtoRec = new ImportReportingTemplateSetRecordRequest();
				dtoRec.setImportData(new AddTemplateSetRequest());
				RestClientBeanUtil.convertBean(reportingInfo, dtoRec.getImportData());
				dtoRec.setImportKeyValue(dtoRec.getImportData().getTemplateSetId());
				return dtoRec;
			}
			@Override
			protected Set<String> getExistIdSet() throws Exception {
				Set<String> retSet = new HashSet<String>();
				List<TemplateSetInfoResponse> infoList = ReportingRestClientWrapper
						.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getTemplateSetList(null);
				for (TemplateSetInfoResponse rec : infoList) {
					retSet.add(rec.getTemplateSetId());
				}
				return retSet;
			}
			@Override
			protected boolean isLackRestReq(ImportReportingTemplateSetRecordRequest restDto) {
				return (restDto == null || restDto.getImportData().getTemplateSetId()  == null);
			}
			@Override
			protected String getKeyValueXmlDto(TemplateInfo xmlDto) {
				return xmlDto.getTemplateSetId();
			}
			@Override
			protected String getId(TemplateInfo xmlDto) {
				return xmlDto.getTemplateSetId();
			}
			@Override
			protected void setNewRecordFlg(ImportReportingTemplateSetRecordRequest restDto, boolean flag) {
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
		ImportClientController<ImportReportingTemplateSetRecordRequest, ImportReportingTemplateSetResponse, RecordRegistrationResponse> importController = new ImportClientController<ImportReportingTemplateSetRecordRequest, ImportReportingTemplateSetResponse, RecordRegistrationResponse>(
				log, Messages.getString("report.template"), confirmer.getImportRecDtoList(),true) {
			@Override
			protected List<RecordRegistrationResponse> getResRecList(ImportReportingTemplateSetResponse importResponse) {
				return importResponse.getResultList();
			};
			@Override
			protected Boolean getOccurException(ImportReportingTemplateSetResponse importResponse) {
				return importResponse.getIsOccurException();
			};
			@Override
			protected String getReqKeyValue(ImportReportingTemplateSetRecordRequest importRec) {
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
			protected ImportReportingTemplateSetResponse callImportWrapper(List<ImportReportingTemplateSetRecordRequest> importRecList)
					throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
				ImportReportingTemplateSetRequest reqDto = new ImportReportingTemplateSetRequest();
				reqDto.setRecordList(importRecList);
				reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
				return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importReportingTemplateSet(reqDto) ;
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

		checkDelete(template);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import Report.Template ");
		
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
		int res = ReportTemplateConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.report.xml.SchemaInfo sci = ReportTemplateConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(log, res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	protected void checkDelete(ReportTemplateType xmlElements){
		List<TemplateSetInfoResponse> subList =null;

		try {
			subList = ReportingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getTemplateSetList(null);
		} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass | ReportingNotFound e) {
			log.error(Messages.getString("ReportTemplate.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			return ;
		}

		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<TemplateInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getTemplateInfo()));
		for(TemplateSetInfoResponse mgrInfo: new ArrayList<>(subList)){
			for(TemplateInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getTemplateSetId().equals(xmlElement.getTemplateSetId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}

		if(subList.size() > 0){
			for(TemplateSetInfoResponse info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getTemplateSetId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						ReportingRestClientWrapper
							.getWrapper(UtilityManagerUtil.getCurrentManagerName())
							.deleteTemplateSet(info.getTemplateSetId());
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getTemplateSetId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getTemplateSetId());
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
		log.debug("Start Differrence Report Template ");

		int ret = 0;
		// XMLファイルからの読み込み
		ReportTemplateType report1 = null;
		ReportTemplateType report2 = null;
		try {
			report1 = XmlMarshallUtil.unmarshall(ReportTemplateType.class,new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			report2 = XmlMarshallUtil.unmarshall(ReportTemplateType.class,new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			sort(report1);
			sort(report2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence Report Template (Error)");
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
			boolean diff = DiffUtil.diffCheck2(report1, report2, ReportTemplateType.class, resultA);
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
		getLogger().debug("End Differrence Report Template");

		return ret;
	}
	
	private void sort(ReportTemplateType template) {
		TemplateInfo[] infoList = template.getTemplateInfo();
		Arrays.sort(infoList,
			new Comparator<TemplateInfo>() {
				@Override
				public int compare(TemplateInfo info1, TemplateInfo info2) {
					return info1.getTemplateSetId().compareTo(info2.getTemplateSetId());
				}
			});
		template.setTemplateInfo(infoList);
	}
	
	public Logger getLogger() {
		return log;
	}
}
