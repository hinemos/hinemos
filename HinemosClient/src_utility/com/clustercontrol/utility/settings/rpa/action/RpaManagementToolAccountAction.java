/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.rpa.action;


import java.io.File;
import java.io.FileInputStream;
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
import org.openapitools.client.model.AddRpaManagementToolAccountRequest;
import org.openapitools.client.model.ImportRpaManagementToolAccountRecordRequest;
import org.openapitools.client.model.ImportRpaManagementToolAccountRequest;
import org.openapitools.client.model.ImportRpaManagementToolAccountResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;
import org.openapitools.client.model.RpaManagementToolAccountResponse;
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
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.rpa.conv.RpaManagementToolAccountConv;
import com.clustercontrol.utility.settings.rpa.xml.RpaManagementToolAccount;
import com.clustercontrol.utility.settings.rpa.xml.RpaManagementToolAccounts;
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
 */
public class RpaManagementToolAccountAction {

	/* ロガー */
	protected static Logger log = Logger.getLogger(RpaManagementToolAccountAction.class);

	public RpaManagementToolAccountAction() throws ConvertorException {
		super();
	}
	
	/**
	 * RPA管理ツールアカウント定義情報を全て削除します。<BR>
	 * 
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearRpaManagementToolAccount(){
		
		log.debug("Start Clear RPA Management Tool Account");
		int ret = 0;

		List<RpaManagementToolAccountResponse> accountList = null;
		
		RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		try {
			accountList = wrapper.getRpaManagementToolAccountList();
		} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass e) {
			log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		List<String> accountIdList = new ArrayList<String>();
		for (RpaManagementToolAccountResponse account : accountList){
			accountIdList.add(account.getRpaScopeId());
		}
		if (!accountIdList.isEmpty()){
			for(String targetId : accountIdList){
				try {
					wrapper.deleteRpaManagementToolAccount(targetId);
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " id:" + targetId);
				} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass | RpaManagementToolAccountNotFound e) {
					log.error(Messages.getString("SettingTools.ClearFailed")  + " id:" + targetId+ " , " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			}
		}
		
		// 処理の終了
		log.info(Messages.getString("SettingTools.ClearCompleted"));
		
		log.debug("End Clear RPA Management Tool Account");
		return ret;
	}
	
	/**
	 *	定義情報をマネージャから読み出します。
	 * @return
	 */
	@ExportMethod
	public int exportRpaManagementToolAccount(String xmlFile) {

		log.debug("Start Export RPA Management Tool Account");

		int ret = 0;
		RpaRestClientWrapper wrapper =
				RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		
		List<RpaManagementToolAccountResponse> accountList =null;
		
		try {
			accountList = wrapper.getRpaManagementToolAccountList();
			Collections.sort(
					accountList,
					new Comparator<RpaManagementToolAccountResponse>() {
						@Override
						public int compare(RpaManagementToolAccountResponse accountInfo1, RpaManagementToolAccountResponse accountInfo2) {
							return accountInfo1.getRpaScopeId().compareTo(accountInfo2.getRpaScopeId());
						}
					});
		} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass e) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		RpaManagementToolAccounts  accounts = new RpaManagementToolAccounts();
		RpaManagementToolAccount account = new RpaManagementToolAccount();
		
		for (RpaManagementToolAccountResponse info : accountList) {
			try{
				account = RpaManagementToolAccountConv.getRpaManagementToolAccount(info);
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + info.getRpaScopeId());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
			
			accounts.addRpaManagementToolAccount(account);
		}
		
		// XMLファイルに出力
		try {
			accounts.setCommon(RpaManagementToolAccountConv.versionRpaDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			accounts.setSchemaInfo(RpaManagementToolAccountConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				accounts.marshal(osw);
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
		log.debug("End Export RPA Management Tool Account");
		return ret;
	}
	
	/**
	 * 定義情報をマネージャに投入します。
	 * 
	 * @return
	 */
	@ImportMethod
	public int importRpaManagementToolAccount(String xmlFile) 
			throws ConvertorException, InvalidRole, InvalidUserPass, RpaManagementToolAccountNotFound, 
			InvalidSetting, HinemosUnknown, ParseException, RestConnectFailed {
		log.debug("Start Import RPA Management Tool Account");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import RPA Management Tool Account (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret = 0;
		
		// XMLファイルからの読み込み
		RpaManagementToolAccounts accounts = null;
		try {
			accounts = XmlMarshallUtil.unmarshall(RpaManagementToolAccounts.class,new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import RPA Management Tool Account (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(accounts.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		// castor の 情報を DTO に変換。
		List<RpaManagementToolAccountResponse> accountList = null;
		try {
			accountList = createRpaManagementToolAccountList(accounts);
		} catch (Exception e) {
			if (e instanceof ConvertorException) {
				getLogger().warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			} else {
				getLogger().warn(Messages.getString("SettingTools.ImportFailed"), e);
			}
			// DTO変換中に例外が発生した場合、RpaScenarioList は初期化状態のまま(件数0)なので、インポートを中断
			return SettingConstants.ERROR_INPROCESS;
		}

		// RpaManagementToolAccountInfo をマネージャに登録。
		ret = importRpaManagementToolAccountList(accountList);

		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		checkDelete(accountList);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import RPA Management Tool Account ");
		
		return ret;
	}
	
	public List<RpaManagementToolAccountResponse> createRpaManagementToolAccountList(RpaManagementToolAccounts accounts) 
			throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, 
			RpaManagementToolAccountNotFound, InvalidSetting, ParseException {
		return RpaManagementToolAccountConv.createRpaManagementToolAccountList(accounts);
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
		int res = RpaManagementToolAccountConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.rpa.xml.SchemaInfo sci = RpaManagementToolAccountConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	protected void checkDelete(List<RpaManagementToolAccountResponse> xmlElements){

		List<RpaManagementToolAccountResponse> subList = null;
		try {
			subList = getFilterdRpaManagementToolAccountList();
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}

		if(subList == null || subList.size() <= 0){
			return;
		}

		for(RpaManagementToolAccountResponse mgrInfo: new ArrayList<>(subList)){
			for(RpaManagementToolAccountResponse xmlElement: new ArrayList<>(xmlElements)){
				if(mgrInfo.getRpaScopeId().equals(xmlElement.getRpaScopeId())){
					subList.remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}

		if(subList.size() > 0){
			
			for(RpaManagementToolAccountResponse info: subList){
				
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getRpaScopeId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteRpaManagementToolAccount(info.getRpaScopeId());
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getRpaScopeId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getRpaScopeId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
	}
	
	/**
	 *  RpaManagementToolAccount をインポートする。
	 *  
	 */
	protected int importRpaManagementToolAccountList(List<RpaManagementToolAccountResponse> accountList) 
			throws HinemosUnknown,InvalidRole, InvalidUserPass, RpaManagementToolAccountNotFound, RestConnectFailed{
		int returnValue =0;
		ImportRecordConfirmer<RpaManagementToolAccountResponse, ImportRpaManagementToolAccountRecordRequest, String> confirmer =
				new ImportRecordConfirmer<RpaManagementToolAccountResponse, ImportRpaManagementToolAccountRecordRequest, String>(
				getLogger(), accountList.toArray(new RpaManagementToolAccountResponse[accountList.size()])) {
			@Override
			protected ImportRpaManagementToolAccountRecordRequest convertDtoXmlToRestReq(RpaManagementToolAccountResponse xmlDto)
					throws HinemosUnknown, InvalidSetting {
				ImportRpaManagementToolAccountRecordRequest dtoRec = new ImportRpaManagementToolAccountRecordRequest();
				dtoRec.setImportData(new AddRpaManagementToolAccountRequest());
				RestClientBeanUtil.convertBean(xmlDto, dtoRec.getImportData());
				dtoRec.setImportKeyValue(dtoRec.getImportData().getRpaScopeId());
				return dtoRec;
			}
			@Override
			protected Set<String> getExistIdSet() throws Exception {
				Set<String> retSet = new HashSet<String>();
				for(RpaManagementToolAccountResponse rec :getFilterdRpaManagementToolAccountList()){
					retSet.add(rec.getRpaScopeId());
				}
				return retSet;
			}
			@Override
			protected boolean isLackRestReq(ImportRpaManagementToolAccountRecordRequest restDto) {
				return false;
			}
			@Override
			protected String getKeyValueXmlDto(RpaManagementToolAccountResponse xmlDto) {
				return xmlDto.getRpaScopeId();
			}
			@Override
			protected String getId(RpaManagementToolAccountResponse xmlDto) {
				return xmlDto.getRpaScopeId();
			}
			@Override
			protected void setNewRecordFlg(ImportRpaManagementToolAccountRecordRequest restDto, boolean flag) {
				restDto.setIsNewRecord(flag);
			}
		};
		int confirmRet = confirmer.executeConfirm();
		if( confirmRet != SettingConstants.SUCCESS && confirmRet != SettingConstants.ERROR_CANCEL ){
			//変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			getLogger().warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return confirmRet;
		}

		ImportClientController<ImportRpaManagementToolAccountRecordRequest, ImportRpaManagementToolAccountResponse, RecordRegistrationResponse> importController =
				new ImportClientController<ImportRpaManagementToolAccountRecordRequest, ImportRpaManagementToolAccountResponse, RecordRegistrationResponse>(
				getLogger(), Messages.getString("rpa.management.tool.account"), confirmer.getImportRecDtoList(),true) {
			@Override
			protected List<RecordRegistrationResponse> getResRecList(ImportRpaManagementToolAccountResponse importResponse) {
				return importResponse.getResultList();
			};
			@Override
			protected Boolean getOccurException(ImportRpaManagementToolAccountResponse importResponse) {
				return importResponse.getIsOccurException();
			};
			@Override
			protected String getReqKeyValue(ImportRpaManagementToolAccountRecordRequest importRec) {
				return importRec.getImportData().getRpaScopeId();
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
			protected ImportRpaManagementToolAccountResponse callImportWrapper(List<ImportRpaManagementToolAccountRecordRequest> importRecList)
					throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
				ImportRpaManagementToolAccountRequest reqDto = new ImportRpaManagementToolAccountRequest();
				reqDto.setRecordList(importRecList);
				reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
				return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importRpaManagementToolAccount(reqDto);
			}
			@Override
			protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
				if (responseRec.getExceptionInfo() != null) {
					return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
				}
				return null;
			};
		};
		returnValue = importController.importExecute();
		
		return returnValue;
	}
	
	protected List<RpaManagementToolAccountResponse> getFilterdRpaManagementToolAccountList() 
			throws HinemosUnknown, InvalidRole, InvalidUserPass, RpaManagementToolAccountNotFound, RestConnectFailed {
		List<RpaManagementToolAccountResponse> accountList = null;
		
		accountList =
				RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRpaManagementToolAccountList();
		
		return accountList;
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
					getLogger());
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
		log.debug("Start Differrence RPA Management Tool Account ");

		int ret = 0;
		// XMLファイルからの読み込み
		RpaManagementToolAccounts accounts1 = null;
		RpaManagementToolAccounts accounts2 = null;
		try {
			accounts1 = XmlMarshallUtil.unmarshall(RpaManagementToolAccounts.class,new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			accounts2 = XmlMarshallUtil.unmarshall(RpaManagementToolAccounts.class,new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			sort(accounts1);
			sort(accounts2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence RPA Management Tool Account (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(accounts1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		if(!checkSchemaVersion(accounts2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(accounts1, accounts2, RpaManagementToolAccounts.class, resultA);
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
		getLogger().debug("End Differrence RPA Management Tool Account");

		return ret;
	}
	
	private void sort(RpaManagementToolAccounts tags) {
		RpaManagementToolAccount[] infoList = tags.getRpaManagementToolAccount();
		Arrays.sort(infoList,
			new Comparator<RpaManagementToolAccount>() {
				@Override
				public int compare(RpaManagementToolAccount info1, RpaManagementToolAccount info2) {
					return info1.getRpaManagementToolAccountInfo().getRpaScopeId().compareTo(info2.getRpaManagementToolAccountInfo().getRpaScopeId());
				}
			});
		tags.setRpaManagementToolAccount(infoList);
	}
	
	public Logger getLogger() {
		return log;
	}
}
