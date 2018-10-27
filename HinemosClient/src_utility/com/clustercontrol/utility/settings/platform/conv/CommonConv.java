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

import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.notify.NotifyRelationInfo;


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
	 */
	public static Collection<NotifyRelationInfo> notifyXml2Dto(String notifyGroupId,
			com.clustercontrol.utility.settings.maintenance.xml.NotifyId[] notifies) {

		if (notifies != null) {

			Collection<NotifyRelationInfo> ret = new ArrayList<NotifyRelationInfo>();
			NotifyRelationInfo info = null;
			com.clustercontrol.utility.settings.maintenance.xml.NotifyId infoXml = null;
			for (int i = 0; i < notifies.length; i++) {

				info = new NotifyRelationInfo();
				infoXml = notifies[i];

				if(infoXml != null){
					info.setNotifyGroupId(notifyGroupId);
				}else{
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

				info.setNotifyType(infoXml.getNotifyType());

				ret.add(info);
			}
			return ret;
		} else {

			return null;
		}
	}

	/**
	 * 通知関係情報（監視設定）のXMLのBeanからDTOに変換します。
	 * 
	 * 
	 * @param notifies
	 * @return
	 */
	public static Collection<NotifyRelationInfo> notifyXml2Dto(String notifyGroupId,
			com.clustercontrol.utility.settings.monitor.xml.NotifyId[] notifies) {

		if (notifies != null) {

			Collection<NotifyRelationInfo> ret = new ArrayList<NotifyRelationInfo>();
			NotifyRelationInfo info = null;
			com.clustercontrol.utility.settings.monitor.xml.NotifyId infoXml = null;
			for (int i = 0; i < notifies.length; i++) {

				info = new NotifyRelationInfo();
				infoXml = notifies[i];

				if(infoXml != null){
					info.setNotifyGroupId(notifyGroupId);
				}else{
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

				info.setNotifyType(infoXml.getNotifyType());

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
	public static com.clustercontrol.utility.settings.maintenance.xml.NotifyId[] notifyMaintennceDto2Xml(Collection<NotifyRelationInfo> notifyDto) {

		if (notifyDto != null) {

			com.clustercontrol.utility.settings.maintenance.xml.NotifyId[] ret = new com.clustercontrol.utility.settings.maintenance.xml.NotifyId[notifyDto
			                                                                                                                            .size()];
			NotifyRelationInfo notify = null;

			Iterator<NotifyRelationInfo> it = notifyDto.iterator();
			int i = 0;

			while (it.hasNext()) {
				ret[i] = new com.clustercontrol.utility.settings.maintenance.xml.NotifyId();

				notify = it.next();

				ret[i].setNotifyGroupId(notify.getNotifyGroupId());
				ret[i].setNotifyId(notify.getNotifyId());
				ret[i].setNotifyType(notify.getNotifyType());

				i++;
			}

			return ret;
		} else {
			return null;
		}
	}

	/**
	 * 通知関連情報からXML用のBeanを生成します。
	 * 
	 * @param notifyDto
	 *            通知関連情報のCollection
	 * @return 監視設定用のBeanの配列
	 */
	/*
	public static com.clustercontrol.utility.settings.monitor.xml.NotifyId[] notifyMonitorDto2Xml(Collection<NotifyRelationInfo> notifyDto) {

		if (notifyDto != null) {

			com.clustercontrol.utility.settings.monitor.xml.NotifyId[] ret = new com.clustercontrol.utility.settings.monitor.xml.NotifyId[notifyDto
			                                                                                                                              .size()];
			NotifyRelationInfo notify = null;

			Iterator<NotifyRelationInfo> it = notifyDto.iterator();
			int i = 0;

			while (it.hasNext()) {
				ret[i] = new com.clustercontrol.utility.settings.monitor.xml.NotifyId();

				notify = it.next();

				ret[i].setNotifyGroupId(notify.getNotifyGroupId());
				ret[i].setNotifyId(notify.getNotifyId());
				ret[i].setNotifyType(notify.getNotifyType());

				i++;
			}
			return ret;
		} else {
			return null;
		}
	}
	*/
	
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
	/*
	public static  com.clustercontrol.utility.settings.monitor.xml.Common versionMonitorDto2Xml(Hashtable<String,String> ver){
	
		
		com.clustercontrol.utility.settings.monitor.xml.Common com = new com.clustercontrol.utility.settings.monitor.xml.Common();
		
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
	*/
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
