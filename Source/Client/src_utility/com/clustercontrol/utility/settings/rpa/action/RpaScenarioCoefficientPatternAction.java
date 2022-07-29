/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.rpa.action;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openapitools.client.model.AddRpaScenarioCoefficientPatternRequest;
import org.openapitools.client.model.ImportRpaScenarioCoefficientPatternRecordRequest;
import org.openapitools.client.model.ImportRpaScenarioCoefficientPatternRequest;
import org.openapitools.client.model.ImportRpaScenarioCoefficientPatternResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;
import org.openapitools.client.model.RpaScenarioCoefficientPatternResponse;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.RpaManagementToolAccountNotFound;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.settings.ClearMethod;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.ExportMethod;
import com.clustercontrol.utility.settings.ImportMethod;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.rpa.conv.RpaScenarioCoefficientPatternConv;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioCoefficientPattern;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioCoefficientPatterns;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.ImportRecordConfirmer;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;


/**
 * 自動化効果計算のマスタ情報を取得、設定します。<br>
 * XMLファイルに定義された自動化効果計算マスタ情報を反映させるクラス<br>
 * ただし、すでに登録されている自動化効果計算マスタ情報と重複する場合はスキップされる。
 * 
 */
public class RpaScenarioCoefficientPatternAction {

	/* ロガー */
	protected static Logger log = Logger.getLogger(RpaScenarioCoefficientPatternAction.class);

	public RpaScenarioCoefficientPatternAction() throws ConvertorException {
		super();
	}
	
	/**
	 * 自動化効果計算マスタ情報を全て削除します。
	 * 
	 */
	@ClearMethod
	public int clearRpaScenarioCoefficientPattern(){
		
		log.debug("Start Clear RPA Scenario Coefficient Pattern");
		int ret = 0;

		List<RpaScenarioCoefficientPatternResponse> patternList = null;
		
		RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());

		try {
			patternList = wrapper.getRpaScenarioCoefficientPatternList();
		} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass e) {
			log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		if (patternList != null && !patternList.isEmpty()){
			for (RpaScenarioCoefficientPatternResponse pattern : patternList){
				try {
					wrapper.deleteRpaScenarioCoefficientPattern(pattern.getRpaToolEnvId(), pattern.getOrderNo());
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " id:[" + pattern.getRpaToolEnvId()+"," +pattern.getOrderNo()+ "]");
				} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass e) {
					log.error(Messages.getString("SettingTools.ClearFailed") + " id:[" + pattern.getRpaToolEnvId() + ","
							+ pattern.getOrderNo() + "]" + " , " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			}
		}
		
		// 処理の終了
		log.info(Messages.getString("SettingTools.ClearCompleted"));
		log.debug("End Clear RPA Scenario Coefficient Pattern");
		return ret;
	}
	
	/**
	 * 自動化効果計算マスタ情報をマネージャに投入します。
	 * 
	 */
	@ImportMethod
	public int importRpaScenarioCoefficientPattern(String xmlFile) throws HinemosUnknown{
		
		log.debug("Start Import RpaScenarioCoefficientPattern");
		
		int ret = 0;
		
		// XMLファイルからの読み込み
		RpaScenarioCoefficientPatterns patterns = null;
		try {
			patterns = XmlMarshallUtil.unmarshall(RpaScenarioCoefficientPatterns.class,new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import RPA Scenario Coefficient Pattern (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(patterns.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		// castor の 情報を DTO に変換。
		List<RpaScenarioCoefficientPatternResponse> patternList = null;
		try {
			patternList = createRpaScenarioCoefficientPatternList(patterns);
		} catch (Exception e) {
			if (e instanceof ConvertorException) {
				getLogger().warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			} else {
				getLogger().warn(Messages.getString("SettingTools.ImportFailed"), e);
			}
			// DTO変換中に例外が発生した場合、RpaScenarioList は初期化状態のまま(件数0)なので、インポートを中断
			return SettingConstants.ERROR_INPROCESS;
		}
		
		// 定義の登録
		try {
			ret = importRpaScenarioCoefficientPatternList(patternList);
		} catch (InvalidRole | InvalidUserPass | RestConnectFailed e) {
			throw new HinemosUnknown(e);
		}
		if(ret != SettingConstants.SUCCESS){
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
			return ret;
		}

		// 処理の終了
		log.info(Messages.getString("SettingTools.ImportCompleted"));
		log.debug("End Import RPA Scenario Coefficient Pattern");
		return ret;
	}
	
	protected int importRpaScenarioCoefficientPatternList(List<RpaScenarioCoefficientPatternResponse> patternList) 
			throws HinemosUnknown, InvalidRole, InvalidUserPass, RestConnectFailed{
		int returnValue =0;
		ImportRpaScenarioCoefficientPatternRecordConfirmer confirmer =
				new ImportRpaScenarioCoefficientPatternRecordConfirmer(
						getLogger(), patternList.toArray(new RpaScenarioCoefficientPatternResponse[patternList.size()]));
		int confirmRet = confirmer.executeConfirm();
		if( confirmRet != SettingConstants.SUCCESS && confirmRet != SettingConstants.ERROR_CANCEL ){
			//変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			getLogger().warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return confirmRet;
		}

		ImportRpaScenarioCoefficientPatternClientController importController =
				new ImportRpaScenarioCoefficientPatternClientController(
						getLogger(), Messages.getString("master.rpa.scenario.coefficient.pattern"), confirmer.getImportRecDtoList(),true);
		returnValue = importController.importExecute();
		
		return returnValue;
	}

	public List<RpaScenarioCoefficientPatternResponse> createRpaScenarioCoefficientPatternList(RpaScenarioCoefficientPatterns patterns) 
			throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, 
			RpaManagementToolAccountNotFound, InvalidSetting, ParseException {
		return RpaScenarioCoefficientPatternConv.createRpaScenarioCoefficientPatternList(patterns);
	}
	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.rpa.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = RpaScenarioCoefficientPatternConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.rpa.xml.SchemaInfo sci = RpaScenarioCoefficientPatternConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	/**
	 * 自動化効果計算マスタ情報をマネージャから読み出します。
	 * 
	 */
	@ExportMethod
	public int exportRpaScenarioCoefficientPattern(String xmlFile){
		
		log.debug("Start Export RpaScenarioCoefficientPattern");
		
		int ret = 0;
		RpaRestClientWrapper wrapper =
				RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		
		List<RpaScenarioCoefficientPatternResponse> patternList =null;
		
		try {
			patternList = wrapper.getRpaScenarioCoefficientPatternList();
			Collections.sort(
					patternList,
					new Comparator<RpaScenarioCoefficientPatternResponse>() {
						@Override
						public int compare(RpaScenarioCoefficientPatternResponse info1, RpaScenarioCoefficientPatternResponse info2) {
							return info1.getRpaToolEnvId().compareTo(info2.getRpaToolEnvId());
						}
					});
		} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass e) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		RpaScenarioCoefficientPatterns patterns = new RpaScenarioCoefficientPatterns();
		RpaScenarioCoefficientPattern pattern = new RpaScenarioCoefficientPattern();
		
		for (RpaScenarioCoefficientPatternResponse info : patternList) {
			try{
				pattern = RpaScenarioCoefficientPatternConv.getRpaScenarioCoefficientPattern(info);
				log.info(Messages.getString("SettingTools.ExportSucceeded") 
						+ " : " + info.getRpaToolEnvId() 
						+ " , " + info.getOrderNo());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
			
			patterns.addRpaScenarioCoefficientPattern(pattern);
		}
		
		// XMLファイルに出力
		try {
			patterns.setCommon(RpaScenarioCoefficientPatternConv.versionRpaDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			patterns.setSchemaInfo(RpaScenarioCoefficientPatternConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				patterns.marshal(osw);
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
		
		log.debug("End Export RpaScenarioCoefficientPattern");
		return ret;
	}

	public Logger getLogger() {
		return log;
	}
	
	/**
	 * 自動化効果計算マスタ インポート向けのレコード確認用クラス
	 * 
	 */
	private static class ImportRpaScenarioCoefficientPatternRecordConfirmer extends ImportRecordConfirmer<RpaScenarioCoefficientPatternResponse, ImportRpaScenarioCoefficientPatternRecordRequest, RpaScenarioCoefficientPatternResponse>{
		
		public ImportRpaScenarioCoefficientPatternRecordConfirmer(Logger logger, RpaScenarioCoefficientPatternResponse[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportRpaScenarioCoefficientPatternRecordRequest convertDtoXmlToRestReq(RpaScenarioCoefficientPatternResponse xmlDto)
				throws HinemosUnknown, InvalidSetting {
			ImportRpaScenarioCoefficientPatternRecordRequest dtoRec = new ImportRpaScenarioCoefficientPatternRecordRequest();
			dtoRec.setImportData(new AddRpaScenarioCoefficientPatternRequest());
			RestClientBeanUtil.convertBean(xmlDto, dtoRec.getImportData());
			dtoRec.setImportKeyValue(dtoRec.getImportData().getRpaToolEnvId());
			
			return dtoRec;
		}

		@Override
		protected boolean isExistRecord(RpaScenarioCoefficientPatternResponse xmlDto){
			// 複合キーでチェックする
			Optional<RpaScenarioCoefficientPatternResponse> existRecord = 
					existIdSet.stream().filter(res -> res.getRpaToolEnvId().equals(xmlDto.getRpaToolEnvId()) && res.getOrderNo() == xmlDto.getOrderNo()).findFirst();

			return existRecord.isPresent();
		};
		
		@Override
		protected Set<RpaScenarioCoefficientPatternResponse> getExistIdSet() throws Exception {
			Set<RpaScenarioCoefficientPatternResponse> retSet = new HashSet<RpaScenarioCoefficientPatternResponse>();
			List<RpaScenarioCoefficientPatternResponse> patternInfoList = RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRpaScenarioCoefficientPatternList();
			for (RpaScenarioCoefficientPatternResponse rec : patternInfoList) {
				retSet.add(rec);
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportRpaScenarioCoefficientPatternRecordRequest restDto) {
			return false;
		}
		@Override
		protected String getKeyValueXmlDto(RpaScenarioCoefficientPatternResponse xmlDto) {
			return xmlDto.getRpaToolEnvId();
		}
		@Override
		protected RpaScenarioCoefficientPatternResponse getId(RpaScenarioCoefficientPatternResponse xmlDto) {
			return xmlDto;
		}
		@Override
		protected void setNewRecordFlg(ImportRpaScenarioCoefficientPatternRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
		@Override
		//既存レコードの場合スキップのためオーバーライド
		public int  executeConfirm(){
			//存在チェック向けに現状のID一覧を取得
			try {
				existIdSet = getExistIdSet();
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				log.debug("End Import RpaScenarioCoefficientPattern (Error)");
				return ret;
			}
			
			// import用データ生成（既存レコードか確認しつつ、XMLオブジェクトをRESTAPI向けオブジェクト変換）
			for (int i = 0; i < importXmlDtoList.length; i++) {
				RpaScenarioCoefficientPatternResponse xmlDto = importXmlDtoList[i];
				ImportRpaScenarioCoefficientPatternRecordRequest  restDto  = null;
				//持ち回り用データに変換して型チェック
				try{
					restDto = convertDtoXmlToRestReq(xmlDto);
					// 登録不要なデータなら NULLで戻ってくるので以後の処理はスキップ
					if(restDto == null){
						continue;
					} 
					//必要な項目が入っていない場合エラーとする
					if ( isLackRestReq(restDto)) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + getKeyValueXmlDto(xmlDto));
						ret = SettingConstants.ERROR_INPROCESS;
						continue;
					}
				} catch (HinemosUnknown |InvalidSetting e) {
					log.info(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
					continue;
				}

				//既存レコードの場合、スキップしログを出力する。
				try{
					boolean isNewRecord =true; 
					if ( isExistRecord(xmlDto) ){
						String targetId = getKeyValueXmlDto(xmlDto);
						//インポート対象外
						log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + targetId);
						continue;
					}
					setNewRecordFlg(restDto,isNewRecord);
					if (!(additionalCheck(restDto))) {
						continue;
					}
					
					importRecDtoList.add(restDto);
					
				} catch (Exception e) {
					log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()),e);
					ret = SettingConstants.ERROR_INPROCESS;
					break;
				}
				
			}
			return ret;
		}
	}
	
	/**
	 * 自動化効果計算マスタ インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportRpaScenarioCoefficientPatternClientController 
		extends ImportClientController<ImportRpaScenarioCoefficientPatternRecordRequest, ImportRpaScenarioCoefficientPatternResponse, RecordRegistrationResponse>{
		
		public ImportRpaScenarioCoefficientPatternClientController(Logger logger, String importInfoName, 
				List<ImportRpaScenarioCoefficientPatternRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportRpaScenarioCoefficientPatternResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportRpaScenarioCoefficientPatternResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportRpaScenarioCoefficientPatternRecordRequest importRec) {
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
		protected ImportRpaScenarioCoefficientPatternResponse callImportWrapper(List<ImportRpaScenarioCoefficientPatternRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportRpaScenarioCoefficientPatternRequest reqDto = new ImportRpaScenarioCoefficientPatternRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importRpaScenarioCoefficientPattern(reqDto);
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
