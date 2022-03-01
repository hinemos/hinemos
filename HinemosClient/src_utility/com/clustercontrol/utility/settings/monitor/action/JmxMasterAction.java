/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openapitools.client.model.AddJmxMasterListRequest;
import org.openapitools.client.model.ImportJmxMasterRecordRequest;
import org.openapitools.client.model.ImportJmxMasterRequest;
import org.openapitools.client.model.ImportJmxMasterResponse;
import org.openapitools.client.model.JmxMasterInfoRequest;
import org.openapitools.client.model.JmxMasterInfoResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.master.xml.JmxMaster;
import com.clustercontrol.utility.settings.master.xml.JmxMasterInfo;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.JmxMasterConv;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.ImportRecordConfirmer;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;

/**
 * JMXマスタ定義情報をインポート・エクスポート・削除するアクションクラス<br>
 * 
 * @version 6.1.0
 * @since 5.0.a
 * 
 */
public class JmxMasterAction extends BaseAction<JmxMasterInfoResponse, JmxMasterInfo, JmxMaster> {

	protected JmxMasterConv conv;
	
	public JmxMasterAction() throws ConvertorException {
		super();
		conv = new JmxMasterConv();
	}

	@Override
	protected String getActionName() {return "JmxMaster";}

	@Override
	protected List<JmxMasterInfoResponse> getList() throws Exception {
		return MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJmxMasterInfoList();
	}

	@Override
	protected void deleteInfo(JmxMasterInfoResponse info) throws Exception {
		MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteJmxMaster(info.getId());
	}

	@Override
	protected String getKeyInfoD(JmxMasterInfoResponse info) {
		return info.getId();
	}

	@Override
	protected JmxMaster newInstance() {
		return new JmxMaster();
	}

	@Override
	protected void addInfo(JmxMaster xmlInfo,JmxMasterInfoResponse info) throws Exception {
		xmlInfo.addJmxMasterInfo(conv.getXmlInfo(info));
	}

	@Override
	protected void exportXml(JmxMaster xmlInfo, String xmlFile) throws Exception {
		xmlInfo.setCommon(com.clustercontrol.utility.settings.platform.conv.CommonConv.versionMasterDto2Xml(Config.getVersion()));
		xmlInfo.setSchemaInfo(new com.clustercontrol.utility.settings.master.xml.SchemaInfo());
		try(FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
			xmlInfo.marshal(osw);
		}
	}

	@Override
	protected List<JmxMasterInfo> getElements(JmxMaster xmlInfo) {
		return Arrays.asList(xmlInfo.getJmxMasterInfo());
	}

	@Override
	protected int registElements(JmxMaster xmlInfo) throws Exception {
		int ret = 0;
		ImportJmxMasterRecordConfirmer jmxMasterConfirmer = new ImportJmxMasterRecordConfirmer( log, xmlInfo.getJmxMasterInfo());
		int jmxMasterConfirmerRet = jmxMasterConfirmer.executeConfirm();
		if (jmxMasterConfirmerRet != 0) {
			ret = jmxMasterConfirmerRet;
		}
		
		if( jmxMasterConfirmerRet != SettingConstants.SUCCESS && jmxMasterConfirmerRet != SettingConstants.ERROR_CANCEL ){
			//変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			log.warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return jmxMasterConfirmerRet;
		}
		
		// レコードの登録（JMXマスタ）
		if (!(jmxMasterConfirmer.getImportRecDtoList().isEmpty())) {
			ImportJmxMasterClientController importJmxMasterClientController = new ImportJmxMasterClientController(log,
					Messages.getString("master.jmx"), jmxMasterConfirmer.getImportRecDtoList(), true);
			int importJmxMasterClientControllerRet = importJmxMasterClientController.importExecute();
			if (importJmxMasterClientControllerRet != 0) {
				ret = importJmxMasterClientControllerRet;
			}
		}
		
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			return SettingConstants.ERROR_INPROCESS;
		}
		
		return ret;
	}

	@Override
	protected String getKeyInfoE(JmxMasterInfo info) {
		return info.getMasterId();
	}

	@Override
	protected JmxMaster getXmlInfo(String filePath) throws Exception {
		return XmlMarshallUtil.unmarshall(JmxMaster.class,new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
	}

	@Override
	protected int checkSchemaVersion(JmxMaster xmlInfo) throws Exception {
		int res = conv.checkSchemaVersion(
				xmlInfo.getSchemaInfo().getSchemaType(),
				xmlInfo.getSchemaInfo().getSchemaVersion(),
				xmlInfo.getSchemaInfo().getSchemaRevision());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				conv.getSchemaVersion(com.clustercontrol.utility.settings.monitor.xml.SchemaInfo.class);
		
		if (!BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision())) {
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}
		return 0;
	}

	@Override
	protected JmxMasterInfo[] getArray(JmxMaster info) {
		return info.getJmxMasterInfo();
	}

	@Override
	protected int compare(JmxMasterInfoResponse info1, JmxMasterInfoResponse info2) {
		return info1.getId().compareTo(info2.getId());
	}

	@Override
	protected int sortCompare(JmxMasterInfo info1, JmxMasterInfo info2) {
		return info1.getMasterId().compareTo(info2.getMasterId());
	}

	@Override
	protected void setArray(JmxMaster xmlInfo, JmxMasterInfo[] infoList) {
		xmlInfo.setJmxMasterInfo(infoList);
	}

	@Override
	protected void checkDelete(JmxMaster xmlInfo) throws Exception {
		// マスタの差分削除チェックは行わない
	}
	
	@Override
	protected void importObjectPrivilege(List<String> objectList){
		// マスタのオブジェクト権限同時インポートは行わない
	}
	
	/**
	 * JMXマスタ インポート向けのレコード確認用クラス
	 * 
	 */
	protected class ImportJmxMasterRecordConfirmer extends ImportRecordConfirmer<JmxMasterInfo, ImportJmxMasterRecordRequest, String>{
		
		public ImportJmxMasterRecordConfirmer(Logger logger, JmxMasterInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportJmxMasterRecordRequest convertDtoXmlToRestReq(JmxMasterInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			
			JmxMasterInfoResponse jmxMasterInfoResponse;
			try {
				jmxMasterInfoResponse = conv.getDTO(xmlDto);
			} catch (Exception e) {
				throw new HinemosUnknown(e);
			}
			ImportJmxMasterRecordRequest dtoRec = new ImportJmxMasterRecordRequest();
			
			List<JmxMasterInfoRequest> jmxMasterInfoRequestList = new ArrayList<JmxMasterInfoRequest>();
			JmxMasterInfoRequest jmxMasterInfoRequest = new JmxMasterInfoRequest();
			RestClientBeanUtil.convertBeanSimple(jmxMasterInfoResponse,jmxMasterInfoRequest);
			jmxMasterInfoRequestList.add(jmxMasterInfoRequest);
			
			AddJmxMasterListRequest addJmxMasterRequest = new AddJmxMasterListRequest();
			addJmxMasterRequest.setJmxMasterInfoList(jmxMasterInfoRequestList);
			
			
			dtoRec.setImportData(addJmxMasterRequest);
			dtoRec.setImportKeyValue(dtoRec.getImportData().getJmxMasterInfoList().get(0).getId());
			
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<JmxMasterInfoResponse> jmxMasterInfoList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJmxMasterInfoList();
			for (JmxMasterInfoResponse rec : jmxMasterInfoList) {
				retSet.add(rec.getId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportJmxMasterRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getJmxMasterInfoList().get(0).getId() == null || restDto.getImportData().getJmxMasterInfoList().get(0).getId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(JmxMasterInfo xmlDto) {
			return xmlDto.getMasterId();
		}
		@Override
		protected String getId(JmxMasterInfo xmlDto) {
			return xmlDto.getMasterId();
		}
		@Override
		protected void setNewRecordFlg(ImportJmxMasterRecordRequest restDto, boolean flag) {
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
				log.debug("End Import PlatformRepositoryNode (Error)");
				return ret;
			}
			
			// import用データ生成（既存レコードか確認しつつ、XMLオブジェクトをRESTAPI向けオブジェクト変換）
			for (int i = 0; i < importXmlDtoList.length; i++) {
				JmxMasterInfo xmlDto = importXmlDtoList[i];
				ImportJmxMasterRecordRequest  restDto  = null;
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
	 * JMXマスタ インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportJmxMasterClientController extends ImportClientController<ImportJmxMasterRecordRequest, ImportJmxMasterResponse, RecordRegistrationResponse>{
		
		public ImportJmxMasterClientController(Logger logger, String importInfoName, List<ImportJmxMasterRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportJmxMasterResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportJmxMasterResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportJmxMasterRecordRequest importRec) {
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
		protected ImportJmxMasterResponse callImportWrapper(List<ImportJmxMasterRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportJmxMasterRequest reqDto = new ImportJmxMasterRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importJmxMaster(reqDto);
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

	@Override
	protected List<String> getImportObjects() {
		return null;
	}
}
