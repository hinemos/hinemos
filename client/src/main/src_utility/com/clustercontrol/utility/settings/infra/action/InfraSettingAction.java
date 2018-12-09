/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.infra.action;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.ws.WebServiceException;

import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.infra.conv.InfraSettingConv;
import com.clustercontrol.utility.settings.infra.xml.InfraManagement;
import com.clustercontrol.utility.settings.infra.xml.InfraManagementInfo;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.ImportProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidSetting_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;

/**
 * 環境構築設定定義情報をインポート・エクスポート・削除するアクションクラス<br>
 * 
 * @version 6.0.0
 * @since 5.0.a
 */
public class InfraSettingAction extends BaseAction<com.clustercontrol.ws.infra.InfraManagementInfo, InfraManagementInfo, InfraManagement> {

	protected InfraSettingConv conv;
	
	public InfraSettingAction() throws ConvertorException {
		super();
		conv = new InfraSettingConv();
	}

	@Override
	protected String getActionName() {return "InfraManagement";}

	@Override
	protected List<com.clustercontrol.ws.infra.InfraManagementInfo> getList()	throws Exception {
		return InfraEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getInfraManagementList();
	}

	@Override
	protected void deleteInfo(com.clustercontrol.ws.infra.InfraManagementInfo info) throws WebServiceException, Exception {
		List<String> ids = new ArrayList<>();
		ids.add(info.getManagementId());
		InfraEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteInfraManagement(ids);
	}

	@Override
	protected String getKeyInfoD(com.clustercontrol.ws.infra.InfraManagementInfo info) {
		return info.getManagementId();
	}

	@Override
	protected InfraManagement newInstance() {
		return new InfraManagement();
	}

	@Override
	protected void addInfo(InfraManagement xmlInfo,	com.clustercontrol.ws.infra.InfraManagementInfo info)	throws Exception {
		xmlInfo.addInfraManagementInfo(conv.getXmlInfo(info));
	}

	@Override
	protected void exportXml(InfraManagement xmlInfo, String xmlFile) throws Exception {
		xmlInfo.setCommon(com.clustercontrol.utility.settings.platform.conv.CommonConv.versionInfraDto2Xml(Config.getVersion()));
		xmlInfo.setSchemaInfo(conv.getSchemaVersion(com.clustercontrol.utility.settings.infra.xml.SchemaInfo.class));
		try(FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
			xmlInfo.marshal(osw);
		}
	}

	@Override
	protected List<InfraManagementInfo> getElements(InfraManagement xmlInfo) {
		return Arrays.asList(xmlInfo.getInfraManagementInfo());
	}

	protected Set<String> registerdSet;
	@Override
	protected void preCheckDuplicate() {
		registerdSet = new HashSet<>();
		try {
			for(com.clustercontrol.ws.infra.InfraManagementInfo info: getList()){
				registerdSet.add(getKeyInfoD(info));
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList"), e);
			log.debug("End Clear " + getActionName() + " (Error)");
		}
		
	}
	
	@Override
	protected int registElement(InfraManagementInfo element) throws Exception {
		InfraEndpointWrapper endpoint = InfraEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		com.clustercontrol.ws.infra.InfraManagementInfo dto = conv.getDTO(element);
		int ret = 0;
		try {
			if(!registerdSet.contains(getKeyInfoE(element))){
				endpoint.addInfraManagement(dto);
			} else {
				//重複時、インポート処理方法を確認する
				if(!ImportProcessMode.isSameprocess()){
					String[] args = {getKeyInfoE(element)};
					ImportProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.import.confirm2", args));
				    ImportProcessMode.setProcesstype(dialog.open());
				    ImportProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
			    	try {
			    		endpoint.modifyInfraManagement(dto);
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + getKeyInfoE(element));
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
			    } else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
			    	log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + getKeyInfoE(element));
			    } else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			    	log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			    	ret = -1;
			    }
			}
		} catch (InvalidRole_Exception e) {
			log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (InvalidUserPass_Exception e) {
			log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (InvalidSetting_Exception e) {
			log.warn(Messages.getString("SettingTools.InvalidSetting") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		}
		return ret;
	}

	@Override
	protected String getKeyInfoE(InfraManagementInfo info) {
		return info.getManagementId();
	}

	@Override
	protected InfraManagement getXmlInfo(String filePath) throws Exception {
		return (InfraManagement) InfraManagement.unmarshal(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
	}

	@Override
	protected int checkSchemaVersion(InfraManagement xmlInfo) throws Exception {
		/*スキーマのバージョンチェック*/
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
	protected InfraManagementInfo[] getArray(InfraManagement info) {
		return info.getInfraManagementInfo();
	}

	@Override
	protected int compare(
			com.clustercontrol.ws.infra.InfraManagementInfo info1,
			com.clustercontrol.ws.infra.InfraManagementInfo info2) {
		return info1.getManagementId().compareTo(info2.getManagementId());
	}

	@Override
	protected int sortCompare(InfraManagementInfo info1,
			InfraManagementInfo info2) {
		return info1.getManagementId().compareTo(info2.getManagementId());
	}

	@Override
	protected void setArray(InfraManagement info, InfraManagementInfo[] infoList) {
		info.setInfraManagementInfo(infoList);
	}

	@Override
	protected void checkDelete(InfraManagement xmlInfo) throws Exception{
		
		List<com.clustercontrol.ws.infra.InfraManagementInfo> subList = getList();
		List<InfraManagementInfo> xmlElements = new ArrayList<>(getElements(xmlInfo));
		
		for(com.clustercontrol.ws.infra.InfraManagementInfo mgrInfo: new ArrayList<>(subList)){
			for(InfraManagementInfo xmlElement: new ArrayList<>(xmlElements)){
				if(getKeyInfoD(mgrInfo).equals(getKeyInfoE(xmlElement))){
					subList.remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(com.clustercontrol.ws.infra.InfraManagementInfo info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {getKeyInfoD(info)};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
			    	try {
			    		deleteInfo(info);
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + getKeyInfoD(info));
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
			    	log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + getKeyInfoD(info));
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			    	log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
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
	@Override
	protected void importObjectPrivilege(List<InfraManagementInfo> objectList){
		if(ImportProcessMode.isSameObjectPrivilege()){
			List<String> objectIdList = new ArrayList<String>();
			if(objectList != null){
				for(InfraManagementInfo info : objectList)
					objectIdList.add(info.getManagementId());
			}
			ObjectPrivilegeAction.importAccessExtraction(
					ImportProcessMode.getXmlObjectPrivilege(),
					HinemosModuleConstant.INFRA,
					objectIdList,
					getLogger());
		}
	}
}
