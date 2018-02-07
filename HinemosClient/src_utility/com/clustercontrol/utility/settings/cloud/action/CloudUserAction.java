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
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import com.clustercontrol.ClusterControlPlugin;
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
import com.clustercontrol.utility.settings.ui.dialog.ImportProcessDialog;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.ws.xcloud.AddCloudLoginUserRequest;
import com.clustercontrol.ws.xcloud.AddCloudScopeRequest;
import com.clustercontrol.ws.xcloud.AddPublicCloudScopeRequest;
import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.CloudManagerException;
import com.clustercontrol.ws.xcloud.InvalidRole_Exception;
import com.clustercontrol.ws.xcloud.InvalidUserPass_Exception;
import com.clustercontrol.ws.xcloud.ModifyBillingSettingRequest;
import com.clustercontrol.ws.xcloud.ModifyCloudScopeRequest;
import com.clustercontrol.xcloud.bean.CloudConstant;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;

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
		
		com.clustercontrol.ws.xcloud.CloudEndpoint endpoint = CloudTools.getEndpoint(com.clustercontrol.ws.xcloud.CloudEndpoint.class);
		for (com.clustercontrol.xcloud.model.cloud.ICloudScope cloudScope:roots){
			try {
				endpoint.removeCloudScope(cloudScope.getId());
			} catch (com.clustercontrol.ws.xcloud.CloudManagerException e) {
				log.error("Clear Cloud.user Error " + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				continue;
			} catch (com.clustercontrol.ws.xcloud.InvalidRole_Exception e) {
				log.error("Clear Cloud.user Error " + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				continue;
			} catch (com.clustercontrol.ws.xcloud.InvalidUserPass_Exception e) {
				log.error("Clear Cloud.user Error " + " : " + HinemosMessage.replace(e.getMessage()));
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
			cloudScope.addICloudScope(CloudUserConv.getICloudScope(cloudScopeEndpoint));
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

		if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			log.debug("End Import Cloud.user (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret = 0;
		
		// XMLファイルからの読み込み
		CloudScopeType cloudScope = null;
		try {
			cloudScope = CloudScope.unmarshal(new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
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
		
		final IHinemosManager manager =
				CloudTools.getHinemosManager(ClusterControlPlugin.getDefault().getCurrentManagerName());
		List<String> platformIdList = CloudTools.getValidPlatfomIdList();
		for (com.clustercontrol.utility.settings.cloud.xml.ICloudScope cloudScopeXML : cloudScope.getICloudScope()) {
			if (!platformIdList.contains(cloudScopeXML.getCloudPlatformId())) {
				log.warn(Messages.getString("CloudOption.Invalid", new String[]{cloudScopeXML.getCloudPlatformId()}));
				log.debug("Skip importUser, cloudScope ID = " + cloudScopeXML.getCloudScopeId());
				continue;
			}
			AddCloudScopeRequest request;
			if ((cloudScopeXML.getCloudPlatformId().equals(CloudConstant.platform_ESXi)) ||
						(cloudScopeXML.getCloudPlatformId().equals(CloudConstant.platform_vCenter))){
				request = CloudUserConv.getPrivateCloudScopeRequestDto(cloudScopeXML);
			} else if (cloudScopeXML.getCloudPlatformId().equals(CloudConstant.platform_AWS)) {
				request = CloudUserConv.getPublicCloudScopeRequestDto(cloudScopeXML);
			} else if (cloudScopeXML.getCloudPlatformId().equals(CloudConstant.platform_HyperV)) {
				request = CloudUserConv.getHyperVCloudScopeRequestDto(cloudScopeXML);
			} else if (cloudScopeXML.getCloudPlatformId().equals(CloudConstant.platform_Azure)) {
				request = CloudUserConv.getAzureCloudScopeRequestDto(cloudScopeXML);
			} else {
				log.warn(Messages.getString("SettingTools.InvalidSetting") +
						" : " + cloudScopeXML.getCloudScopeId() + " ( " +cloudScopeXML.getCloudPlatformId() + " )");
				continue;
			}
			
			try {
				manager.getEndpoint(CloudEndpoint.class).addCloudScope(request);
				// Subユーザ更新
				importSubUser(manager, cloudScopeXML);
				
				// BillingSetting
				if (request instanceof AddPublicCloudScopeRequest)
					importBillingSetting(manager, cloudScopeXML);
				
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + request.getCloudScopeId());
			} catch (CloudManagerException e) {
				if ("CLOUDSCOPE_ALREADY_EXIST".equals(e.getFaultInfo().getErrorCode())){
					//重複時、インポート処理方法を確認する
					if (!ImportProcessMode.isSameprocess()) {
						ImportProcessDialog dialog = new ImportProcessDialog(
								null, Messages.getString("message.import.confirm2", new String[]{cloudScopeXML.getCloudScopeId()}));
						ImportProcessMode.setProcesstype(dialog.open());
						ImportProcessMode.setSameprocess(dialog.getToggleState());
					}
					if (ImportProcessMode.getProcesstype() == ImportProcessDialog.UPDATE) {
						try {
							// スコープ更新
							ModifyCloudScopeRequest modRequest;
							if ((cloudScopeXML.getCloudPlatformId().equals(CloudConstant.platform_ESXi)) ||
									(cloudScopeXML.getCloudPlatformId().equals(CloudConstant.platform_vCenter))) {
								modRequest = CloudUserConv.getModifyPrivateCloudScopeRequestDto(cloudScopeXML);
							} else if (cloudScopeXML.getCloudPlatformId().equals(CloudConstant.platform_AWS)) {
								modRequest = CloudUserConv.getModifyPublicCloudScopeRequestDto(cloudScopeXML);
							} else if (cloudScopeXML.getCloudPlatformId().equals(CloudConstant.platform_HyperV)) {
								modRequest = CloudUserConv.getModifyHyperVCloudScopeRequestDto(cloudScopeXML);
							} else if (cloudScopeXML.getCloudPlatformId().equals(CloudConstant.platform_Azure)) {
								modRequest = CloudUserConv.getModifyPublicCloudScopeRequestDto(cloudScopeXML);
							} else {
								continue;
							}
							manager.getEndpoint(CloudEndpoint.class).modifyCloudScope(modRequest);

							// ログインユーザ更新
							manager.getEndpoint(CloudEndpoint.class).modifyCloudLoginUser(CloudUserConv.getModifyCloudLoginUserRequestDto(cloudScopeXML));

							// Subユーザ更新
							importSubUser(manager, cloudScopeXML);
							
							// BillingSetting
							if (request instanceof AddPublicCloudScopeRequest)
								importBillingSetting(manager, cloudScopeXML);

							log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + cloudScopeXML.getCloudScopeId());
						} catch (Exception e1) {
							log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
							ret = SettingConstants.ERROR_INPROCESS;
						} catch (Throwable t){
							log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(t.getMessage()));
							ret = SettingConstants.ERROR_INPROCESS;
						}
					} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.SKIP){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + cloudScopeXML.getCloudScopeId());
					} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
						return ret;
					}
				} else {
					log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
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
	
	private void importBillingSetting(IHinemosManager manager, ICloudScope info) {
		CloudEndpoint endpoint = manager.getEndpoint(CloudEndpoint.class);
		try {
			ModifyBillingSettingRequest output = CloudUserConv.createBillingSettingRequest(info);
			endpoint.modifyBillingSetting(output);
		} catch (CloudManagerException | InvalidRole_Exception | InvalidUserPass_Exception e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		}
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
					DeleteProcessDialog dialog = new DeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.DELETE){
					try {
						com.clustercontrol.ws.xcloud.CloudEndpoint endpoint = CloudTools.getEndpoint(com.clustercontrol.ws.xcloud.CloudEndpoint.class);
						endpoint.removeCloudScope(info.getId());
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.SKIP){
					log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getId());
				} else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.CANCEL){
					log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
				
			}
		}
		
	}
	
	private int importSubUser(IHinemosManager manager, ICloudScope info){

		final List<AddCloudLoginUserRequest> addRequestList = new ArrayList<>();
		final List<String> removeIdList = new ArrayList<>();
		final List<String> idList = new ArrayList<>();
		
		if ((info.getCloudPlatformId().equals(CloudConstant.platform_ESXi)) ||
				(info.getCloudPlatformId().equals(CloudConstant.platform_vCenter))){
			CloudUserConv.getModifyPrivateCloudUserRequestDto(info, addRequestList, removeIdList, idList);
		} else if (info.getCloudPlatformId().equals(CloudConstant.platform_AWS)) {
			CloudUserConv.getModifyPublicCloudUserRequestDto(info, addRequestList, removeIdList, idList);
		} else if (info.getCloudPlatformId().equals(CloudConstant.platform_HyperV)) {
			CloudUserConv.getModifyHyperVCloudUserRequestDto(info, addRequestList, removeIdList, idList);
		} else if (info.getCloudPlatformId().equals(CloudConstant.platform_Azure)) {
			CloudUserConv.getModifyAzureCloudUserRequestDto(info, addRequestList, removeIdList, idList);
		}
		
		CloudEndpoint endpoint = manager.getEndpoint(CloudEndpoint.class);
		

		try {
			for(String id: removeIdList){
				endpoint.removeCloudLoginUser(info.getCloudScopeId(), id);
			}
			
			for(AddCloudLoginUserRequest request: addRequestList){
				endpoint.addCloudLoginUser(request);
			}
			
			endpoint.modifyCloudLoginUserPriority(info.getCloudScopeId(), idList);
			
		} catch (CloudManagerException | InvalidRole_Exception | InvalidUserPass_Exception e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		}
		
		return 0;
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
			cloudScope = CloudScope.unmarshal(new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			cloudScope2 = CloudScope.unmarshal(new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
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
}
