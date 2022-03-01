/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.conv;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.NotifyRelationInfoRequest;
import org.openapitools.client.model.NotifyRelationInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.util.OpenApiEnumConverter;

/**
 * 共通情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 * 
 */
public class CommonConv {
	
	private static Log log = LogFactory.getLog(CommonConv.class);
	
	/**
	 * 通知関係情報（メンテナンス）のXMLのBeanからDTOに変換します。
	 * 
	 * 
	 * @param notifies
	 * @return
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static Collection<NotifyRelationInfoRequest> notifyXml2Dto(String notifyGroupId,
			com.clustercontrol.utility.settings.maintenance.xml.NotifyId[] notifies) throws InvalidSetting, HinemosUnknown {

		if (notifies != null) {

			Collection<NotifyRelationInfoRequest> ret = new ArrayList<NotifyRelationInfoRequest>();
			NotifyRelationInfoRequest info = null;
			com.clustercontrol.utility.settings.maintenance.xml.NotifyId infoXml = null;
			for (int i = 0; i < notifies.length; i++) {

				info = new NotifyRelationInfoRequest();
				infoXml = notifies[i];
				
				if(infoXml == null){
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid") 
							+ "(NotifyRelation-NotifyRelationId) : " + notifyGroupId);
					return null;
				}
				
				if (infoXml.getNotifyId() != null && !infoXml.getNotifyId().equals("")) {
					info.setNotifyId(infoXml.getNotifyId());
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid") 
							+ "(NotifyRelation-NotifyId) : " + notifyGroupId);
					continue;
				}

				ret.add(info);
			}
			return ret;
		} else {

			return null;
		}
	}

	/**
	 * 通知関連情報からXML用のBeanを生成します。
	 * 
	 * 
	 * @param notifyDto
	 *            通知関連情報のCollection
	 * @return Hinemosシステム用のBeanの配列
	 */
	public static com.clustercontrol.utility.settings.maintenance.xml.NotifyId[] notifyMaintennceDto2Xml(Collection<NotifyRelationInfoResponse> notifyDto) {

		if (notifyDto != null) {

			com.clustercontrol.utility.settings.maintenance.xml.NotifyId[] ret = new com.clustercontrol.utility.settings.maintenance.xml.NotifyId[notifyDto
			                                                                                                                            .size()];
			NotifyRelationInfoResponse notify = null;

			Iterator<NotifyRelationInfoResponse> it = notifyDto.iterator();
			int i = 0;

			while (it.hasNext()) {
				ret[i] = new com.clustercontrol.utility.settings.maintenance.xml.NotifyId();

				notify = it.next();
				ret[i].setNotifyId(notify.getNotifyId());
				ret[i].setNotifyType(OpenApiEnumConverter.enumToInteger(notify.getNotifyType()) );

				i++;
			}

			return ret;
		} else {
			return null;
		}
	}
	
	/**
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static  com.clustercontrol.utility.settings.maintenance.xml.Common versionMaintenanceDto2Xml(Hashtable<String,String> ver){
	
		
		com.clustercontrol.utility.settings.maintenance.xml.Common com = new com.clustercontrol.utility.settings.maintenance.xml.Common();
		
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
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static  com.clustercontrol.utility.settings.platform.xml.Common versionPlatformDto2Xml(Hashtable<String,String> ver){
	
		
		com.clustercontrol.utility.settings.platform.xml.Common com = new com.clustercontrol.utility.settings.platform.xml.Common();
		
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
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static  com.clustercontrol.utility.settings.job.xml.Common versionJobDto2Xml(Hashtable<String,String> ver){
	
		
		com.clustercontrol.utility.settings.job.xml.Common com = new com.clustercontrol.utility.settings.job.xml.Common();
		
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
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static  com.clustercontrol.utility.settings.master.xml.Common versionMasterDto2Xml(Hashtable<String,String> ver){
	
		
		com.clustercontrol.utility.settings.master.xml.Common com = new com.clustercontrol.utility.settings.master.xml.Common();
		
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
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static  com.clustercontrol.utility.settings.infra.xml.Common versionInfraDto2Xml(Hashtable<String,String> ver){
	
		
		com.clustercontrol.utility.settings.infra.xml.Common com = new com.clustercontrol.utility.settings.infra.xml.Common();
		
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
}
