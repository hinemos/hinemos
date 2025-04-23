/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.cloud.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.openapitools.client.model.AddCloudLoginUserRequest;
import org.openapitools.client.model.AddCloudScopeRequest;
import org.openapitools.client.model.ImportCloudScopeRecordRequest;
import org.openapitools.client.model.ImportCloudScopeRequest;
import org.openapitools.client.model.ImportCloudScopeResponse;
import org.openapitools.client.model.ModifyBillingSettingRequest;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.difference.CSVUtil;
import com.clustercontrol.utility.difference.DiffUtil;
import com.clustercontrol.utility.difference.ResultA;
import com.clustercontrol.utility.settings.ClearMethod;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.DiffMethod;
import com.clustercontrol.utility.settings.ExportMethod;
import com.clustercontrol.utility.settings.ImportMethod;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.cloud.conv.CloudUserConv;
import com.clustercontrol.utility.settings.cloud.xml.CloudScope;
import com.clustercontrol.utility.settings.cloud.xml.CloudScopeType;
import com.clustercontrol.utility.settings.cloud.xml.ICloudScope;
import com.clustercontrol.utility.settings.model.BaseAction;
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
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.CloudConstant;
import com.clustercontrol.xcloud.model.cloud.ILoginUser;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @param action 動作
 * @param XMLファイルパス
 * 
 * @version 6.0.0
 * @since 6.0.0
 * 
 */
public class CloudUserAction {

	/* ロガー */
	protected static Logger log = Logger.getLogger(CloudUserAction.class);

	public CloudUserAction() throws ConvertorException {
		super();
	}
	
	/**
	 * 定義情報を全て削除します。<BR>
	 * 
	 * @since 6.0
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearUser(){
		
		log.debug("Start Clear Cloud.user ");
		int ret = 0;
		
		List<com.clustercontrol.xcloud.model.cloud.ICloudScope> roots = CloudTools.getCloudScopeList();
		
		CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		for (com.clustercontrol.xcloud.model.cloud.ICloudScope cloudScope:roots){
			try {
				endpoint.removeCloudScope(cloudScope.getId());
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " id:" + cloudScope.getId());
			} catch (RestConnectFailed | HinemosUnknown | CloudManagerException e ) {
				log.error("Clear Cloud.user Error " +" id:" + cloudScope.getId() + " , " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				continue;
			} catch (InvalidRole e) {
				log.error("Clear Cloud.user Error " +" id:" + cloudScope.getId() + " , " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				continue;
			} catch (InvalidUserPass e) {
				log.error("Clear Cloud.user Error " +" id:" + cloudScope.getId() + " , " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				continue;
			}
		}
		
		// 処理の終了
		log.info(Messages.getString("Cloud.user.ClearCompleted"));
		
		log.debug("End Clear User");
		return ret;
	}

	/**
	 *	定義情報をマネージャから読み出します。
	 * @return
	 */
	@ExportMethod
	public int exportUser(String xmlFile) {

		log.debug("Start Export Cloud.user ");

		int ret = 0;
		List<String> platformIdList = CloudTools.getValidPlatfomIdList();
		List<com.clustercontrol.xcloud.model.cloud.ICloudScope> roots = CloudTools.getCloudScopeList();
		
		
		CloudScope cloudScope = new CloudScope();
		for (com.clustercontrol.xcloud.model.cloud.ICloudScope cloudScopeEndpoint : roots) {
			if (!platformIdList.contains(cloudScopeEndpoint.getPlatformId())) {
				log.warn(Messages.getString("CloudOption.Invalid", new String[]{cloudScopeEndpoint.getPlatformId()}));
				log.debug("Skip importUser, cloudScope ID = " + cloudScopeEndpoint.getId());
				continue;
			}
			try {
				cloudScope.addICloudScope(CloudUserConv.getICloudScope(cloudScopeEndpoint));
				if (cloudScopeEndpoint.getPlatformId().equals(CloudConstant.platform_GCP) || cloudScopeEndpoint.getPlatformId().equals(CloudConstant.platform_OCI)) {
					CloudUserConv.getPrivateKeyFileContent(cloudScopeEndpoint, xmlFile, cloudScopeEndpoint.getPlatformId());
				}
			} catch (RuntimeException e) {
				// キー情報保護の有効確認でエラーが出た場合は終了する
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()), e);
				ret = SettingConstants.ERROR_INPROCESS;
				return ret;
			}
			log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + cloudScopeEndpoint.getId());
		}
		
		cloudScope.setCommon(CloudUserConv.versioncloudDto2Xml(Config.getVersion()));
		// スキーマ情報のセット
		cloudScope.setSchemaInfo(CloudUserConv.getSchemaVersion());
		
		try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(xmlFile), "UTF-8")) {
			cloudScope.marshal(osw);
		} catch (UnsupportedEncodingException | FileNotFoundException | MarshalException | ValidationException e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnexpectedError"), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
		
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Export Cloud.user ");
		return ret;
	}
	
	/**
	 * 定義情報をマネージャに投入します。
	 * 
	 * @return
	 */
	@ImportMethod
	public int importUser(String xmlFile){
		log.debug("Start Import Cloud.user ");
		
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			log.debug("End Import Cloud.user (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret = 0;
		
		// XMLファイルからの読み込み
		CloudScopeType cloudScope = null;
		try {
			cloudScope = XmlMarshallUtil.unmarshall(CloudScopeType.class,new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import Cloud.user (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(cloudScope.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		List<String> platformIdList = CloudTools.getValidPlatfomIdList();
		// 定義の登録向けにDtoを生成
		ImportRecordConfirmer<com.clustercontrol.utility.settings.cloud.xml.ICloudScope, ImportCloudScopeRecordRequest, String> confirmer = new ImportRecordConfirmer<com.clustercontrol.utility.settings.cloud.xml.ICloudScope, ImportCloudScopeRecordRequest, String>(
				log, cloudScope.getICloudScope()) {
			@Override
			protected ImportCloudScopeRecordRequest convertDtoXmlToRestReq(com.clustercontrol.utility.settings.cloud.xml.ICloudScope xmlDto) throws InvalidSetting, HinemosUnknown {
				if (!platformIdList.contains(xmlDto.getCloudPlatformId())) {
					log.warn(Messages.getString("CloudOption.Invalid", new String[]{xmlDto.getCloudPlatformId()}));
					log.debug("Skip importUser, cloudScope ID = " +xmlDto.getCloudScopeId());
					return null;
				}
				try {
					// キー情報保護が有効なら、空欄になっているものを補完する
					CloudUserConv.restoreProtectedKeys(xmlDto);
				} catch (RuntimeException e) {
					// キー情報保護の有効確認でエラーが出た場合は終了する
					log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					throw new InvalidSetting(e.getMessage());
				}
				// クラウドスコープ情報
				AddCloudScopeRequest requestData;
				if ((xmlDto.getCloudPlatformId().equals(CloudConstant.platform_ESXi)) ||
							(xmlDto.getCloudPlatformId().equals(CloudConstant.platform_vCenter))){
					requestData = CloudUserConv.getPrivateCloudScopeRequestDto(xmlDto);
				} else if (xmlDto.getCloudPlatformId().equals(CloudConstant.platform_AWS)) {
					requestData = CloudUserConv.getPublicCloudScopeRequestDto(xmlDto);
				} else if (xmlDto.getCloudPlatformId().equals(CloudConstant.platform_HyperV)) {
					requestData = CloudUserConv.getHyperVCloudScopeRequestDto(xmlDto);
				} else if (xmlDto.getCloudPlatformId().equals(CloudConstant.platform_Azure)) {
					requestData = CloudUserConv.getAzureCloudScopeRequestDto(xmlDto);
				} else if (xmlDto.getCloudPlatformId().equals(CloudConstant.platform_GCP)) {
					requestData = CloudUserConv.getGCPCloudScopeRequestDto(xmlDto, xmlFile);
				} else if (xmlDto.getCloudPlatformId().equals(CloudConstant.platform_OCI)) {
					requestData = CloudUserConv.getOCICloudScopeRequestDto(xmlDto, xmlFile);
				} else {
					log.warn(Messages.getString("SettingTools.InvalidSetting") +
							" : " + xmlDto.getCloudScopeId() + " ( " +xmlDto.getCloudPlatformId() + " )");
					return null;
				}
				ImportCloudScopeRecordRequest dtoRec = new ImportCloudScopeRecordRequest();
				dtoRec.setImportData(requestData);
				dtoRec.setImportKeyValue(xmlDto.getCloudScopeId());

				//サブユーザ
				final List<AddCloudLoginUserRequest> addRequestList = new ArrayList<>();
				final List<String> idList = new ArrayList<>();
				
				if ((xmlDto.getCloudPlatformId().equals(CloudConstant.platform_ESXi)) ||
						(xmlDto.getCloudPlatformId().equals(CloudConstant.platform_vCenter))){
					CloudUserConv.convertPrivateCloudUserRequestDto(xmlDto, requestData, addRequestList, idList);
				} else if (xmlDto.getCloudPlatformId().equals(CloudConstant.platform_AWS)) {
					CloudUserConv.convertPublicCloudUserRequestDto(xmlDto, requestData, addRequestList, idList);
				} else if (xmlDto.getCloudPlatformId().equals(CloudConstant.platform_HyperV)) {
					CloudUserConv.convertHyperVCloudUserRequestDto(xmlDto, requestData, addRequestList, idList);
				} else if (xmlDto.getCloudPlatformId().equals(CloudConstant.platform_Azure)) {
					CloudUserConv.convertAzureCloudUserRequestDto(xmlDto, requestData, addRequestList, idList);
				} else if (xmlDto.getCloudPlatformId().equals(CloudConstant.platform_GCP)) {
					CloudUserConv.convertGCPCloudUserRequestDto(xmlDto, requestData, addRequestList, idList, xmlFile);
				} else if (xmlDto.getCloudPlatformId().equals(CloudConstant.platform_OCI)) {
					CloudUserConv.convertOCICloudUserRequestDto(xmlDto, requestData, addRequestList, idList, xmlFile);
				}
		
				dtoRec.setSubUserList(addRequestList);
				dtoRec.setPriorityArrayList(idList);

				// 課金情報
				ModifyBillingSettingRequest billingSetting = CloudUserConv.createBillingSettingRequest(xmlDto);
				dtoRec.setBillingSetting(billingSetting);
				return dtoRec;
			}

			@Override
			protected Set<String> getExistIdSet() throws Exception {
				Set<String> retSet = new HashSet<String>();
				for (com.clustercontrol.xcloud.model.cloud.ICloudScope root : CloudTools.getCloudScopeList()) {
					retSet.add(root.getId());
				}
				return retSet;
			}
			@Override
			protected boolean isLackRestReq(ImportCloudScopeRecordRequest restDto) {
				return (restDto == null || restDto.getImportData().getCloudScopeId() == null);
			}
			@Override
			protected String getKeyValueXmlDto(com.clustercontrol.utility.settings.cloud.xml.ICloudScope xmlDto) {
				return xmlDto.getCloudScopeId();
			}
			@Override
			protected String getId(com.clustercontrol.utility.settings.cloud.xml.ICloudScope xmlDto) {
				return xmlDto.getCloudScopeId();
			}
			@Override
			protected void setNewRecordFlg(ImportCloudScopeRecordRequest restDto, boolean flag) {
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
		ImportClientController<ImportCloudScopeRecordRequest, ImportCloudScopeResponse, RecordRegistrationResponse> importController = new ImportClientController<ImportCloudScopeRecordRequest, ImportCloudScopeResponse, RecordRegistrationResponse>(
				log, Messages.getString("cloud.scope"), confirmer.getImportRecDtoList(),true) {
			@Override
			protected List<RecordRegistrationResponse> getResRecList(ImportCloudScopeResponse importResponse) {
				return importResponse.getResultList();
			};
			@Override
			protected Boolean getOccurException(ImportCloudScopeResponse importResponse) {
				return importResponse.getIsOccurException();
			};
			@Override
			protected String getReqKeyValue(ImportCloudScopeRecordRequest importRec) {
				return importRec.getImportData().getCloudScopeId();
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
			protected ImportCloudScopeResponse callImportWrapper(List<ImportCloudScopeRecordRequest> importRecList)
					throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
				ImportCloudScopeRequest reqDto = new ImportCloudScopeRequest();
				reqDto.setRecordList(importRecList);
				reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
				return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importCloudScope(reqDto) ;
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
		checkDelete(cloudScope);

		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import Cloud.user ");

		return ret;
	}
	
	protected boolean checkSchemaVersion(com.clustercontrol.utility.settings.cloud.xml.SchemaInfo checkSchemaVersion) {
		/*スキーマのバージョンチェック*/
		int res = CloudUserConv.checkSchemaVersion(checkSchemaVersion.getSchemaType(),
					checkSchemaVersion.getSchemaVersion(),
					checkSchemaVersion.getSchemaRevision());
		com.clustercontrol.utility.settings.cloud.xml.SchemaInfo sci = CloudUserConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(log, res,
				sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	protected void checkDelete(CloudScopeType xmlElements){
		List<com.clustercontrol.xcloud.model.cloud.ICloudScope> subList = CloudTools.getCloudScopeList();
		List<ICloudScope> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getICloudScope()));
		
		for(com.clustercontrol.xcloud.model.cloud.ICloudScope mgrInfo: new ArrayList<>(subList)){
			for(ICloudScope xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getId().equals(xmlElement.getCloudScopeId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
			
		}
		
		if(subList.size() > 0){
			for(com.clustercontrol.xcloud.model.cloud.ICloudScope info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
						endpoint.removeCloudScope(info.getId());
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
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
		log.debug("Start Differrence User ");

		int ret = 0;
		// XMLファイルからの読み込み
		CloudScopeType cloudScope = null;
		CloudScopeType cloudScope2 = null;
		try {
			cloudScope = XmlMarshallUtil.unmarshall(CloudScopeType.class,new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			cloudScope2 = XmlMarshallUtil.unmarshall(CloudScopeType.class,new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			sort(cloudScope);
			sort(cloudScope2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence Cloud.user (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(cloudScope.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		if(!checkSchemaVersion(cloudScope2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(cloudScope, cloudScope2, CloudScopeType.class, resultA);
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
						log.warn(String.format("Fail to delete File. %s", f.getAbsolutePath()));
				}
			}
		}
		catch (Exception e) {
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
		log.debug("End Differrence Cloud.user");

		return ret;
	}
	
	private void sort(CloudScopeType cloudScope) {
		ICloudScope[] infoList = cloudScope.getICloudScope();
		Arrays.sort(infoList,
				new Comparator<ICloudScope>() {
					@Override
					public int compare(ICloudScope info1, ICloudScope info2) {
						return info1.getCloudScopeId().compareTo(info2.getCloudScopeId());
					}
				});
		cloudScope.setICloudScope(infoList);
	}
	
	public Logger getLogger() {
		return log;
	}

	/**
	 * Invoked for import functionality Purpose: This method will check if the
	 * existing cloud scope contains service account details.
	 * 
	 * @param cloudScopeId
	 * @param account
	 * @return boolean value (true/false)
	 */
	public static boolean checkServiceAccountDetailsExistinDB(String cloudScopeId, AddCloudLoginUserRequest account,
			boolean isSub) {

		List<com.clustercontrol.xcloud.model.cloud.ICloudScope> roots = CloudTools.getCloudScopeList();

		ObjectMapper objMapper = new ObjectMapper();
		JsonNode serviceAccountJsonNode = null;

		for (com.clustercontrol.xcloud.model.cloud.ICloudScope cloudScopeEndpoint : roots) {
			ILoginUser accountUser = cloudScopeEndpoint.getLoginUsers().getLoginUser(account.getLoginUserId());
			if (cloudScopeEndpoint.getPlatformId().equals(CloudConstant.platform_GCP)) {
				if (accountUser != null && cloudScopeId.equals(accountUser.getCloudScopeId())) {
					try {
						serviceAccountJsonNode = objMapper
								.readTree(accountUser.getCredential().getJsonCredentialInfo());
						if (serviceAccountJsonNode.get(CloudConstant.AuthenticationType) != null) {
							String authType = serviceAccountJsonNode.get(CloudConstant.AuthenticationType).asText();
							if (authType.equals(CloudConstant.ServiceAccountKey)) {
								account.setJsonCredentialInfo(accountUser.getCredential().getJsonCredentialInfo());
								return true;
							}
						} else if (isSub) {
							// sub account is always ServiceAccountKey
							account.setJsonCredentialInfo(accountUser.getCredential().getJsonCredentialInfo());
							return true;
						}
					} catch (JsonProcessingException e) {
						log.warn("Error while parsing json" + e.getMessage());
					}
				}
			} else if (cloudScopeEndpoint.getPlatformId().equals(CloudConstant.platform_OCI)) {
				accountUser = cloudScopeEndpoint.getLoginUsers().getLoginUser(account.getLoginUserId());
				if (accountUser != null && cloudScopeId.equals(accountUser.getCloudScopeId())) {
					try {
						serviceAccountJsonNode = objMapper
								.readTree(accountUser.getCredential().getJsonCredentialInfo());
						if (serviceAccountJsonNode.get(CloudConstant.authenticationType) != null) {
							String authType = serviceAccountJsonNode.get(CloudConstant.authenticationType).asText();
							if (authType.equals(CloudConstant.apiKeyBasedAuthentication)) {
								account.setJsonCredentialInfo(accountUser.getCredential().getJsonCredentialInfo());
								return true;
							}
						} else if (isSub) {
							// sub account is always API keyfile
							account.setJsonCredentialInfo(accountUser.getCredential().getJsonCredentialInfo());
							return true;
						}

					} catch (JsonProcessingException e) {
						log.warn("Error while parsing json" + e.getMessage());
					}
				}
			}
		}
		return false;
	}

}
