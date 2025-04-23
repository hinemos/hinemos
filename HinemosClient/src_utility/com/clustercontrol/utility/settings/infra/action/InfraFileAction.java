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
import javax.activation.FileDataSource;

import org.openapitools.client.model.AddInfraFileRequest;
import org.openapitools.client.model.InfraFileInfoResponse;
import org.openapitools.client.model.ModifyInfraFileRequest;

import com.clustercontrol.fault.InfraFileTooLarge;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.infra.util.InfraRestClientWrapper;
import com.clustercontrol.rest.JSON;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.infra.conv.InfraFileConv;
import com.clustercontrol.utility.settings.infra.xml.InfraFile;
import com.clustercontrol.utility.settings.infra.xml.InfraFileInfo;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.dialog.UtilityProcessDialog;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.settings.ui.util.BackupUtil;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.XmlMarshallUtil;

/**
 * 環境構築ファイル定義情報をインポート・エクスポート・削除するアクションクラス<br>
 * 
 * @version 6.0.0
 * @since 5.0.a
 */
public class InfraFileAction extends BaseAction<InfraFileInfoResponse, InfraFileInfo, InfraFile> {

	protected InfraFileConv conv;
	protected List<String> objectList = new ArrayList<String>();
	public InfraFileAction() throws ConvertorException {
		super();
		conv = new InfraFileConv();
	}

	@Override
	protected String getActionName() {return "InfraFile";}

	@Override
	protected List<InfraFileInfoResponse> getList() throws Exception {
		return InfraRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getInfraFileList(null);
	}

	@Override
	protected void deleteInfo(InfraFileInfoResponse info) throws Exception {
		List<String> args = new ArrayList<>();
		args.add(info.getFileId());
		InfraRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteInfraFileList(String.join(",",args));
	}

	@Override
	protected String getKeyInfoD(InfraFileInfoResponse info) {
		return info.getFileId();
	}

	@Override
	protected InfraFile newInstance() {
		return new InfraFile();
	}

	@Override
	protected void addInfo(InfraFile xmlInfo, InfraFileInfoResponse info) throws Exception {
		xmlInfo.addInfraFileInfo(conv.getXmlInfo(info));
	}

	@Override
	protected void exportXml(InfraFile xmlInfo, String xmlFile) throws Exception {
		boolean backup = false;
		String directoryPath = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
		if (directoryPath != null && (xmlFile.length() > directoryPath.length())) {
			backup = xmlFile.substring(directoryPath.length()).contains(BackupUtil.getBackupFolder());
		}

		xmlInfo.setCommon(com.clustercontrol.utility.settings.platform.conv.CommonConv.versionInfraDto2Xml(Config.getVersion()));
		xmlInfo.setSchemaInfo(conv.getSchemaVersion(com.clustercontrol.utility.settings.infra.xml.SchemaInfo.class));
		try(FileOutputStream fos = new FileOutputStream(xmlFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
			xmlInfo.marshal(osw);
		}
		InfraRestClientWrapper endpoint = InfraRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		
		FileOutputStream fos = null;
		String infraFolderPath = getFolderPath(backup);
		for(InfraFileInfo info: xmlInfo.getInfraFileInfo()){
			File downloadFile = null;
			try {
				downloadFile =  endpoint.downloadInfraFile(info.getFileId());
				FileDataSource source = new FileDataSource(downloadFile);
				DataHandler handler = new DataHandler(source);
				
				String filePath = getFilePath(info, infraFolderPath);
				fos = new FileOutputStream(new File(filePath));
				
				handler.writeTo(fos);
			} catch (Exception e) {
				log.error(e);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}
				}
				if (downloadFile != null && downloadFile.exists()) {
					if (!downloadFile.delete()) {
						log.warn("Failed temporary infra file."+ " FilePath=" + downloadFile.getAbsolutePath());
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
			for(InfraFileInfoResponse info: getList()){
				registerdSet.add(getKeyInfoD(info));
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList"), e);
			log.debug("End Clear " + getActionName() + " (Error)");
		}
		
	}
	
	@Override
	protected int registElements(InfraFile element) throws Exception {
		InfraRestClientWrapper endpoint = InfraRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		int ret = 0;
		
		for(InfraFileInfo infraFileInfo:element.getInfraFileInfo()){
			AddInfraFileRequest dto = conv.getDTO(infraFileInfo);
			String filePath = getFilePath(infraFileInfo, getFolderPath(false));
			File file = new File(filePath);
			if(! file.exists()){
				log.warn(Messages.getString("SettingTools.InfraFileNotFound") + " : " + getKeyInfoE(infraFileInfo));
				return SettingConstants.ERROR_INPROCESS;
			}
			
			try {
				if(!registerdSet.contains(getKeyInfoE(infraFileInfo))){
					AddInfraFileRequestEx dtoEx = new AddInfraFileRequestEx();
					RestClientBeanUtil.convertBeanSimple(dto, dtoEx);
					endpoint.addInfraFile(file, dtoEx);
					log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + getKeyInfoE(infraFileInfo));
					objectList.add(dto.getFileId());
				} else {
					//重複時、インポート処理方法を確認する
					if(!ImportProcessMode.isSameprocess()){
						String[] args = {getKeyInfoE(infraFileInfo)};
						UtilityProcessDialog dialog = UtilityDialogInjector.createImportProcessDialog(
								null, Messages.getString("message.import.confirm2", args));
						ImportProcessMode.setProcesstype(dialog.open());
						ImportProcessMode.setSameprocess(dialog.getToggleState());
					}
					
					if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
						try {
							ModifyInfraFileRequestEx modifyDtoEx = new ModifyInfraFileRequestEx();
							RestClientBeanUtil.convertBeanSimple(dto, modifyDtoEx);
							endpoint.modifyInfraFile(dto.getFileId(), file, modifyDtoEx);
							log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + getKeyInfoE(infraFileInfo));
							objectList.add(dto.getFileId());
						} catch (Exception e1) {
							log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
							ret = SettingConstants.ERROR_INPROCESS;
						}
					} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + getKeyInfoE(infraFileInfo));
					} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
						ret = -1;
					}
				}
			} catch (InvalidRole e) {
				log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()) + " : " + getKeyInfoE(infraFileInfo) );
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass e) {
				log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()) + " : " + getKeyInfoE(infraFileInfo));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InfraFileTooLarge e) {
				log.warn(Messages.getString("SettingTools.InfraFileTooLarge") + " : " + HinemosMessage.replace(e.getMessage()) + " : " + getKeyInfoE(infraFileInfo));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()) + " : " + getKeyInfoE(infraFileInfo));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		return ret;
	}

	@Override
	protected String getKeyInfoE(InfraFileInfo info) {
		return info.getFileId();
	}

	@Override
	protected InfraFile getXmlInfo(String filePath) throws Exception {
		return XmlMarshallUtil.unmarshall(InfraFile.class,new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
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
			InfraFileInfoResponse info1,
			InfraFileInfoResponse info2) {
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
		
		List<InfraFileInfoResponse> subList = getList();
		List<InfraFileInfo> xmlElements = new ArrayList<>(getElements(xmlInfo));
		
		for(InfraFileInfoResponse mgrInfo: new ArrayList<>(subList)){
			for(InfraFileInfo xmlElement: new ArrayList<>(xmlElements)){
				if(getKeyInfoD(mgrInfo).equals(getKeyInfoE(xmlElement))){
					subList.remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(InfraFileInfoResponse info: subList){
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
	protected void importObjectPrivilege(List<String> objectList){
		if(ImportProcessMode.isSameObjectPrivilege()){
			ObjectPrivilegeAction.importAccessExtraction(
					ImportProcessMode.getXmlObjectPrivilege(),
					HinemosModuleConstant.INFRA_FILE,
					objectList,
					getLogger());
		}
	}

	@Override
	protected List<String> getImportObjects() {
		return objectList;
	}
	
	// ファイルアップロード用のDTO
	public static class AddInfraFileRequestEx extends AddInfraFileRequest {
		@Override
		public String toString() {
			return new JSON().serialize(this);
		}
	}
	
	public static class ModifyInfraFileRequestEx extends ModifyInfraFileRequest {
		@Override
		public String toString() {
			return new JSON().serialize(this);
		}
	}
}
