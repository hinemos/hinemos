/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.system.action;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.ws.WebServiceException;

import com.clustercontrol.maintenance.util.HinemosPropertyEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.maintenance.xml.HinemosProperty;
import com.clustercontrol.utility.settings.maintenance.xml.HinemosPropertyInfo;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.system.conv.HinemosPropertyConv;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.maintenance.HinemosPropertyDuplicate_Exception;
import com.clustercontrol.ws.maintenance.InvalidRole_Exception;
import com.clustercontrol.ws.maintenance.InvalidSetting_Exception;
import com.clustercontrol.ws.maintenance.InvalidUserPass_Exception;

/**
 * Hinemosプロパティ定義情報をインポート・エクスポート・削除するアクションクラス<br>
 * 
 * @version 6.1.0
 * @since 5.0.a
 * 
 */
public class HinemosPropertyAction extends BaseAction<com.clustercontrol.ws.maintenance.HinemosPropertyInfo, HinemosPropertyInfo, HinemosProperty> {

	protected HinemosPropertyConv conv;
	
	public HinemosPropertyAction() throws ConvertorException {
		super();
		conv = new HinemosPropertyConv();
	}

	@Override
	protected String getActionName() {return "PlatformHinemosProperty";}

	@Override
	protected List<com.clustercontrol.ws.maintenance.HinemosPropertyInfo> getList()	throws Exception {
		return HinemosPropertyEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getHinemosPropertyList();
	}

	@Override
	protected void deleteInfo(com.clustercontrol.ws.maintenance.HinemosPropertyInfo info) throws WebServiceException, Exception {
		HinemosPropertyEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteHinemosProperty(info.getKey());
	}

	@Override
	protected String getKeyInfoD(com.clustercontrol.ws.maintenance.HinemosPropertyInfo info) {
		return info.getKey();
	}

	@Override
	protected HinemosProperty newInstance() {
		return new HinemosProperty();
	}

	@Override
	protected void addInfo(HinemosProperty xmlInfo,	com.clustercontrol.ws.maintenance.HinemosPropertyInfo info)	throws Exception {
		xmlInfo.addHinemosPropertyInfo(conv.getXmlInfo(info));
	}

	@Override
	protected void exportXml(HinemosProperty xmlInfo, String xmlFile) throws Exception {
		xmlInfo.setCommon(com.clustercontrol.utility.settings.platform.conv.CommonConv.versionMaintenanceDto2Xml(Config.getVersion()));
		xmlInfo.setSchemaInfo(conv.getSchemaVersion(com.clustercontrol.utility.settings.maintenance.xml.SchemaInfo.class));
		try(FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
			xmlInfo.marshal(osw);
		}
	}

	@Override
	protected List<HinemosPropertyInfo> getElements(HinemosProperty xmlInfo) {
		return Arrays.asList(xmlInfo.getHinemosPropertyInfo());
	}

	@Override
	protected int registElement(HinemosPropertyInfo element) throws Exception {
		HinemosPropertyEndpointWrapper endpoint = HinemosPropertyEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		com.clustercontrol.ws.maintenance.HinemosPropertyInfo dto = conv.getDTO(element);
		int ret = 0;
		try {
			endpoint.addHinemosProperty(dto);
		} catch (HinemosPropertyDuplicate_Exception e) {
			//重複時、インポート処理方法を確認する
			if(!ImportProcessMode.isSameprocess()){
				String[] args = {getKeyInfoE(element)};
				UtilityProcessDialog dialog = UtilityDialogInjector.createImportProcessDialog(
						null, Messages.getString("message.import.confirm2", args));
			    ImportProcessMode.setProcesstype(dialog.open());
			    ImportProcessMode.setSameprocess(dialog.getToggleState());
			}
		    
		    if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
		    	try {
		    		endpoint.modifyHinemosProperty(dto);
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
	protected String getKeyInfoE(HinemosPropertyInfo info) {
		return info.getKey();
	}

	@Override
	protected HinemosProperty getXmlInfo(String filePath) throws Exception {
		return HinemosProperty.unmarshal(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
	}

	@Override
	protected int checkSchemaVersion(HinemosProperty xmlInfo) throws Exception {
		/*スキーマのバージョンチェック*/
		int res = conv.checkSchemaVersion(xmlInfo.getSchemaInfo().getSchemaType(),
					xmlInfo.getSchemaInfo().getSchemaVersion(),
					xmlInfo.getSchemaInfo().getSchemaRevision());
		com.clustercontrol.utility.settings.maintenance.xml.SchemaInfo sci=  
			conv.getSchemaVersion(com.clustercontrol.utility.settings.maintenance.xml.SchemaInfo.class);
		
		boolean chkres = BaseAction.checkSchemaVersionResult(log, res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
		
		if (chkres) {
			return 0;
		} else {
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}
	}

	@Override
	protected HinemosPropertyInfo[] getArray(HinemosProperty info) {
		return info.getHinemosPropertyInfo();
	}

	@Override
	protected int compare(
			com.clustercontrol.ws.maintenance.HinemosPropertyInfo info1,
			com.clustercontrol.ws.maintenance.HinemosPropertyInfo info2) {
		return info1.getKey().compareTo(info2.getKey());
	}

	@Override
	protected int sortCompare(HinemosPropertyInfo info1,
			HinemosPropertyInfo info2) {
		return info1.getKey().compareTo(info2.getKey());
	}

	@Override
	protected void setArray(HinemosProperty info, HinemosPropertyInfo[] infoList) {
		info.setHinemosPropertyInfo(infoList);
	}

	@Override
	protected void checkDelete(HinemosProperty xmlInfo) throws Exception{
		
		List<com.clustercontrol.ws.maintenance.HinemosPropertyInfo> subList = getList();
		List<HinemosPropertyInfo> xmlElements = new ArrayList<>(getElements(xmlInfo));
		
		for(com.clustercontrol.ws.maintenance.HinemosPropertyInfo mgrInfo: new ArrayList<>(subList)){
			for(HinemosPropertyInfo xmlElement: new ArrayList<>(xmlElements)){
				if(getKeyInfoD(mgrInfo).equals(getKeyInfoE(xmlElement))){
					subList.remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(com.clustercontrol.ws.maintenance.HinemosPropertyInfo info: subList){
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
			    	return;
			    }
			}
		}
	}
	
	@Override
	protected void importObjectPrivilege(List<HinemosPropertyInfo> objectList){
		// Hinemosプロパティのオブジェクト権限同時インポートは行わない
	}
}
