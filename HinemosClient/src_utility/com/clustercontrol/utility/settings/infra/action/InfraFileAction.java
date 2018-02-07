/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.infra.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.ws.WebServiceException;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.infra.conv.InfraFileConv;
import com.clustercontrol.utility.settings.infra.xml.InfraFile;
import com.clustercontrol.utility.settings.infra.xml.InfraFileInfo;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.ImportProcessDialog;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.settings.ui.util.BackupUtil;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
import com.clustercontrol.ws.infra.InfraFileTooLarge_Exception;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;

/**
 * 環境構築ファイル定義情報をインポート・エクスポート・削除するアクションクラス<br>
 * 
 * @version 6.0.0
 * @since 5.0.a
 */
public class InfraFileAction extends BaseAction<com.clustercontrol.ws.infra.InfraFileInfo, InfraFileInfo, InfraFile> {

	protected InfraFileConv conv;
	
	public InfraFileAction() throws ConvertorException {
		super();
		conv = new InfraFileConv();
	}

	@Override
	protected String getActionName() {return "InfraFile";}

	@Override
	protected List<com.clustercontrol.ws.infra.InfraFileInfo> getList() throws Exception {
		return InfraEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getInfraFileList();
	}

	@Override
	protected void deleteInfo(com.clustercontrol.ws.infra.InfraFileInfo info) throws WebServiceException, Exception {
		List<String> args = new ArrayList<>();
		args.add(info.getFileId());
		InfraEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteInfraFileList(args);
	}

	@Override
	protected String getKeyInfoD(com.clustercontrol.ws.infra.InfraFileInfo info) {
		return info.getFileId();
	}

	@Override
	protected InfraFile newInstance() {
		return new InfraFile();
	}

	@Override
	protected void addInfo(InfraFile xmlInfo, com.clustercontrol.ws.infra.InfraFileInfo info) throws Exception {
		xmlInfo.addInfraFileInfo(conv.getXmlInfo(info));
	}

	@Override
	protected void exportXml(InfraFile xmlInfo, String xmlFile) throws Exception {
		boolean backup = xmlFile.contains(BackupUtil.getBackupFolder());
		xmlInfo.setCommon(com.clustercontrol.utility.settings.platform.conv.CommonConv.versionInfraDto2Xml(Config.getVersion()));
		xmlInfo.setSchemaInfo(conv.getSchemaVersion(com.clustercontrol.utility.settings.infra.xml.SchemaInfo.class));
		try(FileOutputStream fos = new FileOutputStream(xmlFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
			xmlInfo.marshal(osw);
		}
		InfraEndpointWrapper endpoint = InfraEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName());
		
		FileOutputStream fos = null;
		String infraFolderPath = getFolderPath(backup);
		for(InfraFileInfo info: xmlInfo.getInfraFileInfo()){
			try {
				DataHandler handler = endpoint.downloadInfraFile(info.getFileId(), info.getFileName());

				String filePath = getFilePath(info, infraFolderPath);
				
				fos = new FileOutputStream(new File(filePath));
				handler.writeTo(fos);
				
				endpoint.deleteDownloadedInfraFile(info.getFileName());
			} catch (Exception e) {
				log.error(e);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	@Override
	protected List<InfraFileInfo> getElements(InfraFile xmlInfo) {
		return Arrays.asList(xmlInfo.getInfraFileInfo());
	}

	protected Set<String> registerdSet;
	@Override
	protected void preCheckDuplicate() {
		registerdSet = new HashSet<>();
		try {
			for(com.clustercontrol.ws.infra.InfraFileInfo info: getList()){
				registerdSet.add(getKeyInfoD(info));
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList"), e);
			log.debug("End Clear " + getActionName() + " (Error)");
		}
		
	}
	
	@Override
	protected int registElement(InfraFileInfo element) throws Exception {
		InfraEndpointWrapper endpoint = InfraEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName());
		com.clustercontrol.ws.infra.InfraFileInfo dto = conv.getDTO(element);
		String filePath = getFilePath(element, getFolderPath(false));
		
		if(! new File(filePath).exists()){
			log.warn(Messages.getString("SettingTools.InfraFileNotFound") + " : " + getKeyInfoE(element));
			return SettingConstants.ERROR_INPROCESS;
		}

		int ret = 0;
		try {
			if(!registerdSet.contains(getKeyInfoE(element))){
				endpoint.addInfraFile(dto, filePath);
			} else {
				//重複時、インポート処理方法を確認する
				if(!ImportProcessMode.isSameprocess()){
					String[] args = {getKeyInfoE(element)};
					ImportProcessDialog dialog = new ImportProcessDialog(
							null, Messages.getString("message.import.confirm2", args));
					ImportProcessMode.setProcesstype(dialog.open());
					ImportProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(ImportProcessMode.getProcesstype() == ImportProcessDialog.UPDATE){
					try {
						endpoint.modifyInfraFile(dto, filePath);
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + getKeyInfoE(element));
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.SKIP){
					log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + getKeyInfoE(element));
				} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
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
		} catch (InfraFileTooLarge_Exception e) {
			log.warn(Messages.getString("SettingTools.InfraFileTooLarge") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		}
		return ret;
	}

	@Override
	protected String getKeyInfoE(InfraFileInfo info) {
		return info.getFileId();
	}

	@Override
	protected InfraFile getXmlInfo(String filePath) throws Exception {
		return InfraFile.unmarshal(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
	}

	@Override
	protected int checkSchemaVersion(InfraFile xmlInfo) throws Exception {
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
	protected InfraFileInfo[] getArray(InfraFile info) {
		return info.getInfraFileInfo();
	}

	@Override
	protected int compare(
			com.clustercontrol.ws.infra.InfraFileInfo info1,
			com.clustercontrol.ws.infra.InfraFileInfo info2) {
		return info1.getFileId().compareTo(info2.getFileId());
	}

	@Override
	protected int sortCompare(InfraFileInfo info1,
			InfraFileInfo info2) {
		return info1.getFileId().compareTo(info2.getFileId());
	}

	@Override
	protected void setArray(InfraFile info, InfraFileInfo[] infoList) {
		info.setInfraFileInfo(infoList);
	}

	@Override
	protected void checkDelete(InfraFile xmlInfo) throws Exception{
		
		List<com.clustercontrol.ws.infra.InfraFileInfo> subList = getList();
		List<InfraFileInfo> xmlElements = new ArrayList<>(getElements(xmlInfo));
		
		for(com.clustercontrol.ws.infra.InfraFileInfo mgrInfo: new ArrayList<>(subList)){
			for(InfraFileInfo xmlElement: new ArrayList<>(xmlElements)){
				if(getKeyInfoD(mgrInfo).equals(getKeyInfoE(xmlElement))){
					subList.remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(com.clustercontrol.ws.infra.InfraFileInfo info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {getKeyInfoD(info)};
					DeleteProcessDialog dialog = new DeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.DELETE){
					try {
						deleteInfo(info);
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + getKeyInfoD(info));
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.SKIP){
					log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + getKeyInfoD(info));
				} else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.CANCEL){
					log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
				}
			}
		}
	}
	
	private String getFilePath(InfraFileInfo info, String folderPath){
		StringBuffer sb = new StringBuffer();
		sb.append(folderPath);
		sb.append(File.separator);
		sb.append(info.getFileId());
		isExsitsAndCreate(sb.toString());
		sb.append(File.separator);
		sb.append(info.getFileName());
		
		return sb.toString();
	}
	
	protected String getFolderPath(boolean backup){
		StringBuffer sb = new StringBuffer();
		
		sb.append(MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML));
		sb.append(File.separator);
		
		if (backup) {
			sb.append(BackupUtil.getBackupFolder());
			sb.append(File.separator);
			sb.append(MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_INFRA));
			sb.append("_" + BackupUtil.getTimeStampString());
		} else {
			sb.append(MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_INFRA));
		}
		isExsitsAndCreate(sb.toString());

		return sb.toString();
	}
	
	protected void isExsitsAndCreate(String directoryPath){
		File dir = new File(directoryPath);
		if(!dir.exists() && !directoryPath.endsWith("null")){
			if (!dir.mkdir())
				log.warn(String.format("Fail to create Directory. %s", dir.getAbsolutePath()));;
		}
	}
	
	/**
	 * オブジェクト権限同時インポート
	 * 
	 * @param objectType
	 * @param objectIdList
	 */
	@Override
	protected void importObjectPrivilege(List<InfraFileInfo> objectList){
		if(ImportProcessMode.isSameObjectPrivilege()){
			List<String> objectIdList = new ArrayList<String>();
			if(objectList != null){
				for(InfraFileInfo info : objectList)
					objectIdList.add(info.getFileId());
			}
			ObjectPrivilegeAction.importAccessExtraction(
					ImportProcessMode.getXmlObjectPrivilege(),
					HinemosModuleConstant.INFRA_FILE,
					objectIdList,
					getLogger());
		}
	}
}
