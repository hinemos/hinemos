/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.nodemap.action;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.nodemap.util.NodeMapEndpointWrapper;
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
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.nodemap.conv.NodeMapConv;
import com.clustercontrol.utility.settings.nodemap.xml.NodeMapBgImage;
import com.clustercontrol.utility.settings.nodemap.xml.NodeMapBgImageType;
import com.clustercontrol.utility.settings.nodemap.xml.NodeMapIconImageType;
import com.clustercontrol.utility.settings.nodemap.xml.NodeMapBgImageInfo;
import com.clustercontrol.utility.settings.nodemap.xml.NodemapIconImage;
import com.clustercontrol.utility.settings.nodemap.xml.NodemapIconImageInfo;
import com.clustercontrol.utility.settings.ui.dialog.ImportProcessDialog;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.settings.ui.util.BackupUtil;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
import com.clustercontrol.ws.nodemap.BgFileNotFound_Exception;
import com.clustercontrol.ws.nodemap.HinemosUnknown_Exception;
import com.clustercontrol.ws.nodemap.IconFileNotFound_Exception;
import com.clustercontrol.ws.nodemap.InvalidRole_Exception;
import com.clustercontrol.ws.nodemap.InvalidUserPass_Exception;

/**
 * 
 * @param action 動作
 * @param XMLファイルパス（ユーザ情報定義の入力元）
 * 
 * @version 6.1.0
 * @since 6.0.0
 * 
 */
public class NodeMapImageAction {

	private static final int MAX_FILESIZE = 256*1024;
	/* ロガー */
	protected static Logger log = Logger.getLogger(NodeMapImageAction.class);

	public NodeMapImageAction() throws ConvertorException {
		super();
	}

	/**
	 * 項目定義情報を全て削除します。<BR>
	 * 
	 * @since 1.0
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearNodeMap(){

		log.debug("Start Clear clearNodeMapImage ");
		int ret = 0;

		return ret;
	}


	/**
	 * 定義情報をマネージャから読み出します。
	 * @return
	 */
	@ExportMethod
	public int exportNodeMap(String imageFile, String iconFile){
		log.debug("Start Import exportNodeMap Image:" + imageFile);
		int ret =0;
		boolean backup = imageFile.contains(BackupUtil.getBackupFolder());
		log.debug("imageFile : " + new File(imageFile).getAbsolutePath());
		log.debug("iconFile : " + new File(iconFile).getAbsolutePath());

		NodeMapEndpointWrapper wrapper = NodeMapEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName());

		List<String> bigImageNames = null;
		List<String> iconNames = null;
		try {
			bigImageNames = wrapper.getBgImagePK();
			iconNames = wrapper.getIconImagePK();
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}

		//XML作成
		// ImageFile
		NodeMapBgImage NodeMapBgImage = new NodeMapBgImage();
		for (String  bigImageName : bigImageNames) {
			try{
				
				NodeMapBgImageInfo vNodeMapBgImageInfo = new NodeMapBgImageInfo();
				vNodeMapBgImageInfo.setFileName(bigImageName);
				NodeMapBgImage.addNodeMapBgImageInfo(vNodeMapBgImageInfo);
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + bigImageName);
			
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		try {
			NodeMapBgImage.setCommon(NodeMapConv.versionNodeMapDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			NodeMapBgImage.setSchemaInfo(NodeMapConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(imageFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				NodeMapBgImage.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
		
		// Icon
		NodemapIconImageInfo nodeMapIconImage = new NodemapIconImageInfo();
		for (String  iconName : iconNames) {
			try{
				nodeMapIconImage.setIconId(iconName);
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + iconName,e);
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + iconName,e);
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		NodemapIconImage nodemapIconImage = new NodemapIconImage();
		for (String  iconName : iconNames) {
			try{
				
				NodemapIconImageInfo vNodemapIconImageInfo = new NodemapIconImageInfo();
				vNodemapIconImageInfo.setIconId(iconName);
				nodemapIconImage.addNodemapIconImageInfo(vNodemapIconImageInfo);
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + iconName);
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + iconName,e);
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + iconName,e);
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		try {
			nodemapIconImage.setCommon(NodeMapConv.versionNodeMapDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			nodemapIconImage.setSchemaInfo(NodeMapConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(iconFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				nodemapIconImage.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
		}

		
		// データ保存
		byte[] fileData=null;
		
		try {
			String bgFolderPath = getFolderPath(SettingToolsXMLPreferencePage.VALUE_NODEMAP_BG_FOLDER,backup);
			// ImageFile保存
			for (String bigImageName : bigImageNames){
				fileData = wrapper.getBgImage(bigImageName);
				String path = getFilePath(bigImageName, bgFolderPath);
				log.debug("path = " + path);
				FileOutputStream fileOutStm = null;
				try {
					fileOutStm = new FileOutputStream(path);
					fileOutStm.write(fileData);
				} catch (IOException e) {
					log.error(Messages.getString("NodeMapImage.ExportFailed") + " FilePath=" + path,e);//処理続行
				}finally{
					try {
						fileOutStm.close();
					} catch (IOException e) {
					}
					fileOutStm = null;
				}
			}
			
			String iconFolderPath = getFolderPath(SettingToolsXMLPreferencePage.VALUE_NODEMAP_ICON_FOLDER,backup);
			// Icon保存
			for (String iconName : iconNames){
				fileData = wrapper.getIconImage(iconName);
				String path = getFilePath(iconName, iconFolderPath);
				log.debug("path = " + path);
				FileOutputStream fileOutStm = null;
				try {
					fileOutStm = new FileOutputStream(path);
					fileOutStm.write(fileData);
				} catch (IOException e) {
					log.error(Messages.getString("NodeMapImage.ExportFailed") + " FilePath=" + path,e);//処理続行
				}finally{
					try {
						fileOutStm.close();
					} catch (IOException e) {
					}
					fileOutStm = null;
				}
			}
			
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | IconFileNotFound_Exception | BgFileNotFound_Exception e) {
			log.error(e);
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		
		log.debug("End Export NodeMap Image ");
		
		
		return ret;
	}
	

	/**
	 * 項目定義情報をマネージャに投入します。
	 * 
	 * @return
	 */
	@ImportMethod
	public int importNodeMap(String imageXml, String iconXml){
		
		log.debug("Start Import NodeMapImage :" + imageXml);
		int ret=0;
		
		if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			log.debug("End Import Report.Schedule (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		// XMLファイルからの読み込み
		
		NodeMapBgImageType nodeMapBgImage = null;
		NodeMapIconImageType nodeMapIconImageInfo = null;
		// BbImage
		try {
			nodeMapBgImage =  NodeMapBgImage.unmarshal(new InputStreamReader(new FileInputStream(imageXml), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import NodeMap Image (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(nodeMapBgImage.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		// IconImage
		try {
			nodeMapIconImageInfo =  NodemapIconImage.unmarshal(new InputStreamReader(new FileInputStream(iconXml), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import NodeMap Image (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(nodeMapIconImageInfo.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		NodeMapEndpointWrapper wrapper = NodeMapEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName());
		// インポート
		// BGFile
		String bgFolderPath = getFolderPath(SettingToolsXMLPreferencePage.VALUE_NODEMAP_BG_FOLDER, false);
		for (NodeMapBgImageInfo bgImageInfo: nodeMapBgImage.getNodeMapBgImageInfo()) {
			try {
				// ファイルデータ取得
				String path = getFilePath(bgImageInfo.getFileName(), bgFolderPath);
				byte[] filedata = getFileData(path);
				if (wrapper.isBgImage(bgImageInfo.getFileName())){
					if(!ImportProcessMode.isSameprocess()){
						String[] args = {bgImageInfo.getFileName()};
						ImportProcessDialog dialog = new ImportProcessDialog(
								null, Messages.getString("message.import.confirm2", args));
						ImportProcessMode.setProcesstype(dialog.open());
						ImportProcessMode.setSameprocess(dialog.getToggleState());
					}
					if(ImportProcessMode.getProcesstype() == ImportProcessDialog.UPDATE){
						wrapper.setBgImage(bgImageInfo.getFileName(), filedata);
						log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + bgImageInfo.getFileName());
					} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.SKIP){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + bgImageInfo.getFileName());
					} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
						return ret;
					}
				}
				else{
					wrapper.setBgImage(bgImageInfo.getFileName(), filedata);
					log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + bgImageInfo.getFileName());
				}
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}

		// Icon
		String iconFolderPath = getFolderPath(SettingToolsXMLPreferencePage.VALUE_NODEMAP_ICON_FOLDER, false);
		for (NodemapIconImageInfo iconImageInfo: nodeMapIconImageInfo.getNodemapIconImageInfo()) {
			try {
				// ファイルデータ取得
				String path = getFilePath(iconImageInfo.getIconId(), iconFolderPath);
				byte[] filedata = getFileData(path);
				
				if (wrapper.isIconImage(iconImageInfo.getIconId())){
					if(!ImportProcessMode.isSameprocess()){
						String[] args = {iconImageInfo.getIconId()};
						ImportProcessDialog dialog = new ImportProcessDialog(
								null, Messages.getString("message.import.confirm2", args));
						ImportProcessMode.setProcesstype(dialog.open());
						ImportProcessMode.setSameprocess(dialog.getToggleState());
					}
					if(ImportProcessMode.getProcesstype() == ImportProcessDialog.UPDATE){
						wrapper.setIconImage(iconImageInfo.getIconId(), filedata);
						log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + iconImageInfo.getIconId());
					} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.SKIP){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + iconImageInfo.getIconId());
					} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
						return ret;
					}
				}
				else{
					wrapper.setIconImage(iconImageInfo.getIconId(), filedata);
					log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + iconImageInfo.getIconId());
				}
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		//checkDelete(NodeMapBgImage);// 削除の手段がない
		
		return ret;
		
	}
	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.nodemap.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = NodeMapConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.nodemap.xml.SchemaInfo sci = NodeMapConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	/**
	 * ファイルデータ取得
	 * @param filepath
	 * @throws Exception
	 */
	private static byte[] getFileData(String filepath) throws Exception {
		File file = new File(filepath);
		String filename = file.getName();
		int filesize = (int)file.length();
		if (filesize > MAX_FILESIZE) {
			log.warn("getFileData(), file size is too large");
			throw new Exception(Messages.getString("file.too.large"));
		}
		byte[] filedata = null;

		/*
		 * ファイルを読む
		 */
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(filepath);
			filedata = new byte[filesize];
			int readsize = stream.read(filedata, 0, filesize);
			log.debug("UploadImage readsize = " + readsize + ", filesize = " + filesize);
			log.debug("path=" + filepath + ", name=" + filename);
		} catch (FileNotFoundException e) {
			log.warn("getFileData(), " + e.getMessage(), e);
			throw new Exception(Messages.getString("file.not.found"), e);
		} catch (Exception e) {
			log.warn("getFileData(), " + e.getMessage(), e);
			throw e;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					log.warn("getFileData(), " + e.getMessage(), e);
				}
				stream = null;
			}
		}
		return filedata;
	}
	
	
	@DiffMethod
	public int diffXml(String xmlPgImage1, String xmlIcon1, String xmlPgImage2, String xmlIcon2) throws ConvertorException {
		
		log.debug("Start Differrence Nodemap　Image ");

		int ret = 0;
		// XMLファイルからの読み込み
		NodeMapBgImageType NodeMapBgImage1 = null;
		NodeMapBgImageType NodeMapBgImage2 = null;
		NodeMapIconImageType nodeMapIconImage1 = null;
		NodeMapIconImageType nodeMapIconImage2 = null;
		
		try {
			NodeMapBgImage1 = NodeMapBgImage.unmarshal(new InputStreamReader(new FileInputStream(xmlPgImage1), "UTF-8"));
			NodeMapBgImage2 = NodeMapBgImage.unmarshal(new InputStreamReader(new FileInputStream(xmlPgImage2), "UTF-8"));
			nodeMapIconImage1 = NodemapIconImage.unmarshal(new InputStreamReader(new FileInputStream(xmlIcon1), "UTF-8"));
			nodeMapIconImage2 = NodemapIconImage.unmarshal(new InputStreamReader(new FileInputStream(xmlIcon2), "UTF-8"));
			
			sort(NodeMapBgImage1);
			sort(NodeMapBgImage2);
			sort(nodeMapIconImage1);
			sort(nodeMapIconImage2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence NodeMap Image (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		// PgImage
		if(!checkSchemaVersion(NodeMapBgImage1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(NodeMapBgImage2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		// Icon
		if(!checkSchemaVersion(nodeMapIconImage1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(nodeMapIconImage1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			// PgImage
			boolean diff = DiffUtil.diffCheck2(NodeMapBgImage1, NodeMapBgImage2, NodeMapBgImageType.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_1;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlPgImage2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlPgImage2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}
			
			// Icon
			resultA = new ResultA();
			diff = DiffUtil.diffCheck2(nodeMapIconImage1, nodeMapIconImage2, NodeMapIconImageType.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_2;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlIcon2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlIcon2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}
		} catch (FileNotFoundException e) {
			log.warn(e.getMessage());
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
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
		
		log.debug("End Differrence Nodemap Image");

		return ret;
	}
	
	private void sort(NodeMapBgImageType NodeMapBgImage) {
		NodeMapBgImageInfo[] infoList = NodeMapBgImage.getNodeMapBgImageInfo();
		Arrays.sort(infoList,
				new Comparator<NodeMapBgImageInfo>() {
					@Override
					public int compare(NodeMapBgImageInfo info1, NodeMapBgImageInfo info2) {
						return info1.getFileName().compareTo(info2.getFileName());
					}
				});
		NodeMapBgImage.setNodeMapBgImageInfo(infoList);
	}
	
	private void sort(NodeMapIconImageType nodeMapIconImage) {
		NodemapIconImageInfo[] infoList = nodeMapIconImage.getNodemapIconImageInfo();
		Arrays.sort(infoList,
				new Comparator<NodemapIconImageInfo>() {
					@Override
					public int compare(NodemapIconImageInfo info1, NodemapIconImageInfo info2) {
						return info1.getIconId().compareTo(info2.getIconId());
					}
				});
		nodeMapIconImage.setNodemapIconImageInfo(infoList);
	}
	
	private String getFilePath(String iconId, String folderPath){
		StringBuffer sb = new StringBuffer();
		sb.append(folderPath);
		sb.append(File.separator);
		sb.append(iconId);
		
		return sb.toString();
	}
	
	private String getFolderPath(String fileKind, boolean backup){
		StringBuffer sb = new StringBuffer();
		
		sb.append(MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML));
		sb.append(File.separator);
		
		if (backup) {
			sb.append(BackupUtil.getBackupFolder());
			sb.append(File.separator);
			sb.append(MultiManagerPathUtil.getPreference(fileKind));
			sb.append("_" + BackupUtil.getTimeStampString());
		} else {
			sb.append(MultiManagerPathUtil.getPreference(fileKind));
		}
		
		isExsitsAndCreate(sb.toString());
		
		return sb.toString();
	}
	
	protected void isExsitsAndCreate(String directoryPath){
		File dir = new File(directoryPath);
		if(!dir.exists() && !directoryPath.endsWith("null")){
			if (!dir.mkdir())
				log.warn(String.format("Fail to create Directory. %s", dir.getAbsolutePath()));
		}
	}
	
	public Logger getLogger() {
		return log;
	}
}
