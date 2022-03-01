/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.collect.action;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.openapitools.client.model.AddCollectPlatformMasterRequest;
import org.openapitools.client.model.CollectorPlatformInfoResponse;
import org.openapitools.client.model.ImportPlatformMasterRecordRequest;
import org.openapitools.client.model.ImportPlatformMasterRequest;
import org.openapitools.client.model.ImportPlatformMasterResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.ExportMethod;
import com.clustercontrol.utility.settings.ImportMethod;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.collect.conv.CollectConv;
import com.clustercontrol.utility.settings.collect.conv.PlatformMasterConv;
import com.clustercontrol.utility.settings.master.xml.CollectorMstPlatforms;
import com.clustercontrol.utility.settings.master.xml.CollectorPlatforms;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;


/**
 * プラットフォームのマスター情報を取得、設定します。<br>
 * XMLファイルに定義されたプラットフォーム情報を反映させるクラス<br>
 * ただし、すでに登録されているプラットフォーム情報と重複する場合はスキップされる。
 * 
 * @param action 動作
 * @param XMLファイルパス（ユーザ情報定義の入力元）
 * 
 * @version 6.0.0
 * @since 1.2.0
 * 
 */
public class PlatformMasterAction {

	/* ロガー */
	protected static Logger log = Logger.getLogger(PlatformMasterAction.class);

	public PlatformMasterAction() throws ConvertorException {
		super();
	}
	
	/**
	 * プラットフォーム情報をマネージャに投入します。
	 * 
	 * @return
	 * @throws HinemosUnknown 
	 */
	@ImportMethod
	public int importPlatformMaster(String fileName) throws HinemosUnknown{
		
		log.debug("Start Import PlatformMaster :" + fileName);
		
		int ret=0;
		
		//XMLからBeanに取り込みます。
		CollectorMstPlatforms list = null;
		try {
			list = XmlMarshallUtil.unmarshall(CollectorMstPlatforms.class,new InputStreamReader(
					new FileInputStream(fileName), "UTF-8"));
			
		} catch (MarshalException e1) {
			log.error(Messages.getString("CollectMaster.UnmarshalXmlFailed") + " " + e1.getMessage());
			log.debug(e1,e1);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformMaster : " + fileName +"(Error)");
			return ret;
		} catch (ValidationException e1) {
			log.error(Messages.getString("CollectMaster.UnmarshalXmlFailed") + " " + e1.getMessage());
			log.debug(e1,e1);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformMaster : " + fileName +"(Error)");
			return ret;
		} catch (UnsupportedEncodingException e1) {
			log.error(Messages.getString("CollectMaster.UnmarshalXmlFailed") + " " + e1.getMessage());
			log.debug(e1,e1);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformMaster : " + fileName +"(Error)");
			return ret;
		} catch (FileNotFoundException e1) {
			log.error(Messages.getString("CollectMaster.UnmarshalXmlFailed") + " " + e1.getMessage());
			log.debug(e1,e1);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformMaster : " + fileName +"(Error)");
			return ret;
		}
		
		/*
		 * スキーマのバージョンチェック
		 */
		if(!checkSchemaVersion(list.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		// 重複確認無しのため、ImportRecordConfirmerは利用しない
		List<ImportPlatformMasterRecordRequest> dtoRecList = new ArrayList<ImportPlatformMasterRecordRequest>();
		for(CollectorPlatforms collectorPlatforms:list.getCollectorPlatforms()){
			AddCollectPlatformMasterRequest dto = PlatformMasterConv.xml2dto(collectorPlatforms);
			ImportPlatformMasterRecordRequest dtoRec = new ImportPlatformMasterRecordRequest();
			dtoRec.setImportData(dto);
			dtoRec.setIsNewRecord(true);
			dtoRec.setImportKeyValue(dtoRec.getImportData().getPlatformId());
			dtoRecList.add(dtoRec);
		}

		// レコードの登録（プラットフォームマスタ）
		if (!(dtoRecList.isEmpty())) {
			ImportPlatformMasterClientController platformMasterController = new ImportPlatformMasterClientController(log,
					Messages.getString("master.platform"), dtoRecList, true);
			int platformMasterControllerRet = platformMasterController.importExecute();
			if (platformMasterControllerRet != 0) {
				ret = platformMasterControllerRet;
			}
		}

		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("CollectMaster.ImportCompleted"));
		}else{
			log.error(Messages.getString("CollectMaster.EndWithErrorCode") );
		}
		
		log.debug("End Import CollectCalcMaster");
		return ret;
		
	}

	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.master.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = PlatformMasterConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.master.xml.SchemaInfo sci = PlatformMasterConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	/**
	 * プラットフォーム情報をマネージャから読み出します。
	 * @return
	 */
	@ExportMethod
	public int exportPlatformMaster(String fileName){
		
		log.debug("Start Export PlatformMaster");
		
		int ret = 0;
		
		List<CollectorPlatformInfoResponse> list = null;

		try {
			list = RepositoryRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCollectPlatformMasterList();
			log.info(Messages.getString("CollectMaster.ExportSucceeded") + " : " + fileName);
		} catch (Exception e) {
			log.error(Messages.getString("CollectMaster.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformMaster (Error)");
			return ret;
		}
		
		CollectorMstPlatforms exportData = new CollectorMstPlatforms();
		CollectorPlatforms platforms = null;
		
		Iterator<CollectorPlatformInfoResponse> itr = list.iterator();
		while(itr.hasNext()) {
			platforms = PlatformMasterConv.dto2Xml(itr.next());
			exportData.addCollectorPlatforms(platforms);
		}
		
		if (exportData == null || exportData.getCollectorPlatformsCount() == 0){
			ret = SettingConstants.ERROR_INPROCESS;
		}
		else {
			// XMLファイルに出力
			try{
				exportData.setCommon(CollectConv.versionCollectDto2Xml(Config.getVersion()));
				
				// スキーマ情報のセット
				exportData.setSchemaInfo(PlatformMasterConv.getSchemaVersion());
				try(FileOutputStream fos = new FileOutputStream(fileName);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
					exportData.marshal(osw);
				}
			} catch (Exception e) {
				log.warn(Messages.getString("CollectMaster.ExportFailed") + " : " + e.getMessage());
				log.debug(e,e);
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("CollectMaster.ExportCompleted"));
		}else{
			log.error(Messages.getString("CollectMaster.EndWithErrorCode") );
		}
			
		log.debug("End Export PlatformMaster");
		return ret;
	}

	public Logger getLogger() {
		return log;
	}
	
	/**
	 * プラットフォームマスタ インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportPlatformMasterClientController extends ImportClientController<ImportPlatformMasterRecordRequest, ImportPlatformMasterResponse, RecordRegistrationResponse>{
		
		public ImportPlatformMasterClientController(Logger logger, String importInfoName, List<ImportPlatformMasterRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportPlatformMasterResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportPlatformMasterResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportPlatformMasterRecordRequest importRec) {
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
		protected ImportPlatformMasterResponse callImportWrapper(List<ImportPlatformMasterRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportPlatformMasterRequest reqDto = new ImportPlatformMasterRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importPlatformMaster(reqDto);
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
