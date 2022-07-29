/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.report.conv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.openapitools.client.model.TemplateSetDetailInfoResponse;
import org.openapitools.client.model.TemplateSetInfoResponse;

import com.clustercontrol.util.DateTimeStringConverter;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.report.xml.TemplateDetailInfo;
import com.clustercontrol.utility.settings.report.xml.TemplateInfo;
import com.clustercontrol.utility.util.Config;

public class ReportTemplateConv {
	
	static private final String schemaType="H";
	static private final String schemaVersion="1";
	static private final String schemaRevision="1" ;
	
	/* ロガー */
	private static Logger log = Logger.getLogger(ReportTemplateConv.class);
	/**
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static com.clustercontrol.utility.settings.report.xml.Common
			versionReportDto2Xml(Hashtable<String,String> ver){
	
		com.clustercontrol.utility.settings.report.xml.Common com =
				new com.clustercontrol.utility.settings.report.xml.Common();
				
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
	static public com.clustercontrol.utility.settings.report.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.report.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.report.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	public static TemplateInfo getTemplate(TemplateSetInfoResponse template)
			throws IndexOutOfBoundsException, ParseException {

		TemplateInfo ret = new TemplateInfo();
		ret.setTemplateSetId(template.getTemplateSetId());
		ret.setTemplateSetName(template.getTemplateSetName());
		ret.setDescription(template.getDescription());
		ret.setOwnerRoleId(template.getOwnerRoleId());

		TemplateDetailInfo[] templateDetailInfoList = new TemplateDetailInfo[template.getTemplateSetDetailInfoList().size()];
		List<TemplateSetDetailInfoResponse> details = template.getTemplateSetDetailInfoList();
		int i=0;
		for (TemplateSetDetailInfoResponse detail : details) {
			templateDetailInfoList[i] = new TemplateDetailInfo();
			templateDetailInfoList[i].setOrderNo(detail.getOrderNo());
			templateDetailInfoList[i].setDescription(detail.getDescription());
			templateDetailInfoList[i].setTemplateId(detail.getTemplateId());
			templateDetailInfoList[i].setTitleName(detail.getTitleName());
			i++;
		}
		ret.setTemplateDetailInfo(templateDetailInfoList);

		return ret;
	}
	
	public static TemplateSetInfoResponse
		getTemplateInfoDto(TemplateInfo info) {
	
		TemplateSetInfoResponse ret =new TemplateSetInfoResponse();
		
		try {
			// 登録日時、更新日時に利用する日時（実行日時とする）
			String now = DateTimeStringConverter.formatLongDate(new Date().getTime());
			
			ret.setTemplateSetId(info.getTemplateSetId());
			ret.setTemplateSetName(info.getTemplateSetName());
			ret.setDescription(info.getDescription());
			ret.setOwnerRoleId(info.getOwnerRoleId());
			
			TemplateSetDetailInfoResponse detail = null;
			
			int orderNo = 0;
			for (TemplateDetailInfo detailxml :sort(info.getTemplateDetailInfo())){
				detail= new TemplateSetDetailInfoResponse();
				detail.setTemplateSetId(detailxml.getTemplateId());
				if (detailxml.getOrderNo() > 0) {
					detail.setOrderNo(++orderNo);
				}
				detail.setDescription(detailxml.getDescription());
				detail.setTemplateId(detailxml.getTemplateId());
				detail.setTitleName(detailxml.getTitleName());
				
				ret.getTemplateSetDetailInfoList().add(detail);
			}
			
			ret.setRegDate(now);;
			ret.setRegUser(Config.getConfig("Login.USER"));
			ret.setUpdateDate(now);
			ret.setUpdateUser(Config.getConfig("Login.USER"));
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	
		return ret;
	}
	
	private static TemplateDetailInfo[] sort(TemplateDetailInfo[] templateDetailInfoList) {
		Arrays.sort(templateDetailInfoList,
			new Comparator<TemplateDetailInfo>() {
				@Override
				public int compare(TemplateDetailInfo info1, TemplateDetailInfo info2) {
					return Integer.compare(info1.getOrderNo(), info2.getOrderNo());
				}
			});
		return templateDetailInfoList;
	}
}
