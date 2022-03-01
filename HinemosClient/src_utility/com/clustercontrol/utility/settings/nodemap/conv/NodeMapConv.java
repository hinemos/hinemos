/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.nodemap.conv;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openapitools.client.model.FacilityElementResponse;
import org.openapitools.client.model.MapAssociationInfoResponse;
import org.openapitools.client.model.NodeMapModelResponse;
import org.openapitools.client.model.RegisterNodeMapModelRequest;

import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.nodemap.xml.Association;
import com.clustercontrol.utility.settings.nodemap.xml.Contents;
import com.clustercontrol.utility.settings.nodemap.xml.NodeMapModel;

public class NodeMapConv {
	
	private static final String schemaType="I";
	private static final String schemaVersion="1";
	private static final String schemaRevision="1";
	
	/**
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static com.clustercontrol.utility.settings.nodemap.xml.Common versionNodeMapDto2Xml(Hashtable<String,String> ver){
	
		com.clustercontrol.utility.settings.nodemap.xml.Common com = new com.clustercontrol.utility.settings.nodemap.xml.Common();
				
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
	static public com.clustercontrol.utility.settings.nodemap.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.nodemap.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.nodemap.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	/**
	 * 計算定義情報を、Hinemos Bean(DTO)からXML Beanへ変換する。
	 * 
	 * @param Hinemos Bean(DTO)
	 * @return 計算定義 XML Bean
	 */
	public static NodeMapModel dto2Xml(NodeMapModelResponse nodeMapModelWs) {

		NodeMapModel ret = new NodeMapModel();
		ret.setBgName(nodeMapModelWs.getBgName());
		ret.setMapId(nodeMapModelWs.getMapId());
		List<MapAssociationInfoResponse> associationWsList = nodeMapModelWs.getAssociations();
		for (MapAssociationInfoResponse associationWs :associationWsList){
			Association association = new Association();
			association.setSource(associationWs.getSource());
			association.setTarget(associationWs.getTarget());
			ret.addAssociation(association);
		}
		
		Set<Entry<String, FacilityElementResponse>> entryList = nodeMapModelWs.getContents().entrySet();
		
		for (Entry<String, FacilityElementResponse> entry  :entryList){
			FacilityElementResponse facilityElement = entry.getValue();
			if(!facilityElement.getNewcomer()){
				Contents content = new Contents();
				content.setFacilityId(facilityElement.getFacilityId());
				content.setIconImage(facilityElement.getIconImage());
				content.setX(facilityElement.getX());
				content.setY(facilityElement.getY());
				ret.addContents(content);
			}
		}

		return ret;
	}
	
	public static RegisterNodeMapModelRequest xml2Dto(NodeMapModel nodeMapModel) {
		
		RegisterNodeMapModelRequest nodeMapModelDto = new RegisterNodeMapModelRequest();
		nodeMapModelDto.setBgName(nodeMapModel.getBgName());
		nodeMapModelDto.setMapId(nodeMapModel.getMapId());
		Map<String, FacilityElementResponse> contentsDto = new HashMap<String, FacilityElementResponse>();
		Contents[] contentXmls = nodeMapModel.getContents();
		for (Contents contentXml :contentXmls){
			FacilityElementResponse facilityElement = new FacilityElementResponse();
			facilityElement.setFacilityId(contentXml.getFacilityId());
			facilityElement.setIconImage(contentXml.getIconImage());
			facilityElement.setX((int)contentXml.getX());
			facilityElement.setY((int)contentXml.getY());
			contentsDto.put(contentXml.getFacilityId(), facilityElement);
		}
		
		nodeMapModelDto.setContents(contentsDto);
		
		List<MapAssociationInfoResponse>  associations = nodeMapModelDto.getAssociations();
		
		Association[] associationXmls = nodeMapModel.getAssociation();
		MapAssociationInfoResponse association = null;
		for (Association associationXml :associationXmls){
			association = new MapAssociationInfoResponse();
			association.setSource(associationXml.getSource());
			association.setTarget(associationXml.getTarget());
			associations.add(association);
		}

		return nodeMapModelDto;
	}
}
