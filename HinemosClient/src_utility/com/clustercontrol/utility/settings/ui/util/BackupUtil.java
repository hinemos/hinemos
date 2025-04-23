/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.settings.ui.action.ReadXMLAction;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.util.MultiManagerPathUtil;

/**
 * バックアップ用ユーティリティクラス<BR>
 *
 * @version 6.1.0
 * @since 6.0.0
 */

public class BackupUtil {
	private static Logger log = Logger.getLogger(BackupUtil.class);
	public static final int WHEN_IMPORT = 1;
	public static final int WHEN_EXPORT = 2;
	public static final int WHEN_CLEAR = 3;
	
	private static final String TIME_FORMAT = "yyyyMMdd_HHmmss";
	
	public static String getBackupFolder() {
		return MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_BACKUP_FOLDER);
	}

	public static boolean isBackup(int when) {
		String key = null;
		String defaultValue = null;
		switch(when){
		case WHEN_IMPORT:
			key = SettingToolsXMLPreferencePage.KEY_BACKUP_IMPORT;
			defaultValue=SettingToolsXMLPreferencePage.DEFAULT_VALUE_BACKUP_IMPORT;
			break;
		case WHEN_EXPORT:
			key = SettingToolsXMLPreferencePage.KEY_BACKUP_EXPORT;
			defaultValue=SettingToolsXMLPreferencePage.DEFAULT_VALUE_BACKUP_EXPORT;
			break;
		case WHEN_CLEAR:
			key = SettingToolsXMLPreferencePage.KEY_BACKUP_CLEAR;
			defaultValue=SettingToolsXMLPreferencePage.DEFAULT_VALUE_BACKUP_CLEAR;
			break;
		default:
			return false;
		}
		
		String value = MultiManagerPathUtil.getPreference(key);
		if (value.equals(key)) {
			return Boolean.parseBoolean(defaultValue);
		} else if (value.equals(Boolean.TRUE.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String getTimeStampString(){
		return new SimpleDateFormat(TIME_FORMAT).format(new Timestamp(System.currentTimeMillis()));
	}
	
	private static String getBackupFileName(String parent, String fileName, String timestamp){
		int index = fileName.lastIndexOf('.');
		if (index ==-1){
			return parent + fileName;
		}else{
			String backupFileName = parent + fileName.substring(0, index) + "_" + timestamp;
			String backupFileExtent = fileName.substring(index);//"."付き
			return backupFileName + backupFileExtent;
		}
	}
	
	private static boolean makeBackupFolder(String path){
		File destFolder = new File(path);
		if (destFolder.exists()){
			// Do Nothing
		}else{
			if (!destFolder.mkdirs()){
				return false;
			}
		}
		return true;
	}
	
	private static File getDestFile(File fromFile, String timestamp){
		String dstPath = getBackupPath();
		
		File destFolder = new File(dstPath);
		if (destFolder.exists()){
			// Do Nothing
		}else{
			if (!destFolder.mkdirs()){
				return null;
			}
		}
		
		if (!makeBackupFolder(dstPath)){
			return null;
		}
		
		String destFileName = getBackupFileName(dstPath, fromFile.getName(), timestamp);
		File destFile = new File(destFileName);
		return destFile;
	}
	
	public static boolean moveXml(FuncInfo info){
		List<String> defaultFileList = info.getDefaultXML();
		
		// XMLファイルのリスト作成
		List<String> fileList = ReadXMLAction.getXMLFile(defaultFileList);
		String timestamp = getTimeStampString();
		for (String file: fileList){
			File fromFile = new File(file);
			if (fromFile.exists()){
				File destFile = getDestFile(fromFile, timestamp);
				if (destFile == null){
					return false;
				}
				try {
					Files.copy(fromFile.toPath(), destFile.toPath());
				} catch (IOException e) {
					return false;
				}
			}
		}

		if (info.getId().equals(HinemosModuleConstant.INFRA_FILE)){
			moveBinaryFile(MultiManagerPathUtil.getPreference(
					SettingToolsXMLPreferencePage.VALUE_INFRA), timestamp);
		} else if (info.getId().equals(HinemosModuleConstant.JOB_MAP_IMAGE)){
			moveBinaryFile(MultiManagerPathUtil.getPreference(
					SettingToolsXMLPreferencePage.VALUE_JOBMAP_IMAGE_FOLDER), timestamp);
		} else if (info.getId().equals(HinemosModuleConstant.NODE_MAP_IMAGE)){
			moveBinaryFile(MultiManagerPathUtil.getPreference(
					SettingToolsXMLPreferencePage.VALUE_NODEMAP_BG_FOLDER), timestamp);
			moveBinaryFile(MultiManagerPathUtil.getPreference(
					SettingToolsXMLPreferencePage.VALUE_NODEMAP_ICON_FOLDER), timestamp);
		} else if (info.getId().equals(HinemosModuleConstant.CLOUD_USER)) {
			moveBinaryFile(MultiManagerPathUtil.getCloudScopeFolder(), timestamp);
		}
		
		return true;
	}
	
	/**
	 * @param folder
	 * @param timestamp
	 * @return
	 */
	public static boolean moveBinaryFile(String folder, String timestamp){
		StringBuffer fromPath = new StringBuffer();
		fromPath.append(MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML));
		fromPath.append(File.separator);
		fromPath.append(folder);
		fromPath.append(File.separator);
		Path from = Paths.get(fromPath.toString());
		
		StringBuffer destPath = new StringBuffer();
		destPath.append(getBackupPath());
		destPath.append(folder);
		destPath.append("_" + timestamp);
		destPath.append(File.separator);
		Path dest = Paths.get(destPath.toString());
		
		// ファイルツリーをたどって、ファイル／ディレクトリをコピーするFileVisitor
		FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				// ディレクトリのコピー
				Path target = dest.resolve(from.relativize(dir)).normalize();
				Files.copy(dir, target, StandardCopyOption.COPY_ATTRIBUTES);
				return FileVisitResult.CONTINUE;
			}
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				// ファイルをコピー
				Path target = dest.resolve(from.relativize(file)).normalize();
				Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES);
				return FileVisitResult.CONTINUE;
			}
		};
		
		try {
			Files.walkFileTree(from, visitor);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public static String getBackupPath(){
		StringBuffer sb = new StringBuffer();
		sb.append(MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML));
		sb.append(File.separator);
		sb.append(BackupUtil.getBackupFolder());
		sb.append(File.separator);
		return sb.toString();
	}

	public static List<String> getBackupList(List<String> fileList){
		List<String> retList = new ArrayList<String>();
		String backupPath = getBackupPath();
		makeBackupFolder(backupPath);
		for (String fileName: fileList) {
			String backupFileName = getBackupFileName(backupPath,  MultiManagerPathUtil.getXMLFileName(fileName), getTimeStampString());
			retList.add(backupFileName);
		}
		return retList;
	}
	
	public static void deleteFiles(File f){
		File[] files=f.listFiles();
		if (null == files){
			return;
		}
		for(int i=0; i<files.length; i++){
			if(f.isFile()){
				deleteFiles( files[i] );
			}else if (f.isDirectory()){
				deleteFolder(files[i]);
			}
		}
		if (!f.delete())
			log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
	}
	
	public static void deleteFolder(File f){
		if( f.exists()==false ){
			return ;
		}

		if(f.isFile()){
			if (!f.delete())
				log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
		}
			
		if(f.isDirectory()){
			File[] files=f.listFiles();
			if (null == files){
				return;
			}
			for(int i=0; i<files.length; i++){
				deleteFolder( files[i] );
			}
			if (!f.delete())
				log.warn(String.format("Fail to delete Directory. %s", f.getAbsolutePath()));
		}
	}
}
