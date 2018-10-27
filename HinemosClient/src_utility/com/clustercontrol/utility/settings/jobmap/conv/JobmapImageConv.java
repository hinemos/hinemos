/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.jobmap.conv;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.clustercontrol.jobmanagement.util.JobmapIconImageUtil;
import com.clustercontrol.utility.settings.jobmap.xml.JobmapInfo;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.MultiManagerPathUtil;

public class JobmapImageConv {
	
	static private final String schemaType="H";
	static private final String schemaVersion="1";
	static private final String schemaRevision="1" ;
	
	/* ロガー */
	private static Logger log = Logger.getLogger(JobmapImageConv.class);

	
	/**
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static com.clustercontrol.utility.settings.jobmap.xml.Common
		versionJobmapDto2Xml(Hashtable<String,String> ver){
	
		com.clustercontrol.utility.settings.jobmap.xml.Common com =
				new com.clustercontrol.utility.settings.jobmap.xml.Common();

		com.setHinemosVersion(ver.get("hinemosVersion"));
		com.setToolVersion(ver.get("toolVersion"));
		com.setGenerator(ver.get("generator"));
		com.setAuthor(System.getProperty("user.name"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		com.setGenerateDate(dateFormat.format(new Date()));
		com.setRuntimeHost(ver.get("runtimeHost"));
		com.setConnectedManager(ver.get("connectedManager"));
		
		return com;
	}
	
	/**
	 * XMLとツールの対応バージョンをチェック */
	static public int checkSchemaVersion(String type, String version ,String revision){

		return BaseConv.checkSchemaVersion(
				schemaType,schemaVersion,schemaRevision,
				type,version,revision);
	}
	
	
	/**
	 * スキーマのバージョンを返します。
	 * @return
	 */
	static public com.clustercontrol.utility.settings.jobmap.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.jobmap.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.jobmap.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	
	public static JobmapInfo getJobmap(com.clustercontrol.ws.jobmanagement.JobmapIconImage jobmapImage) {
		
		JobmapInfo ret = new JobmapInfo();
		ret.setIconId(jobmapImage.getIconId());
		ret.setDescription(jobmapImage.getDescription());
		ret.setOwnerRoleId(jobmapImage.getOwnerRoleId());
		
		return ret;
	}
	
	public static com.clustercontrol.ws.jobmanagement.JobmapIconImage
		getJobmapInfoDto(JobmapInfo info) throws ParseException {
	
		com.clustercontrol.ws.jobmanagement.JobmapIconImage ret =
			new com.clustercontrol.ws.jobmanagement.JobmapIconImage();
	

		// 登録日時、更新日時に利用する日時（実行日時とする）
		long now = new Date().getTime();
		ret.setIconId(info.getIconId());
		ret.setDescription(info.getDescription());
		ret.setOwnerRoleId(info.getOwnerRoleId());
		String path = getFilePath(info.getIconId());
		if (path==null) {
			return null;
		}
		byte[] fileData =null;
		try {
			fileData = JobmapIconImageUtil.getImageFileData(path);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
		ret.setFiledata(fileData);
		
		ret.setCreateTime(now);
		ret.setCreateUser(Config.getConfig("Login.USER"));
		ret.setUpdateTime(now);
		ret.setUpdateUser(Config.getConfig("Login.USER"));

		return ret;
	}
	
	private static String getFilePath(String iconId){
		StringBuffer sb = new StringBuffer();
		
		sb.append(MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML));
		sb.append(File.separator);
		sb.append(MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_JOBMAP_IMAGE_FOLDER));
		sb.append(File.separator);
		String path = sb.toString();
		
		File dir = new File(path);
		File[] files = dir.listFiles();
		String fileName = null;
		if(files == null)
			return null;
		for (File file : files){
			fileName = file.getName();
			if (fileName.equals(iconId)){
				return file.getAbsolutePath();
			}
		}

		return null;
	}

}
