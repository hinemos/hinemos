/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.rpa.conv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.openapitools.client.model.RpaScenarioTagResponse;

import com.clustercontrol.bean.PatternConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RpaScenarioTagNotFound;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioTag;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioTagInfo;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioTags;

public class RpaScenarioTagConv {
	
	static private final String schemaType="K";
	static private final String schemaVersion="1";
	static private final String schemaRevision="1" ;
	
	/* ロガー */
	private static Logger log = Logger.getLogger(RpaScenarioTagConv.class);
	/**
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static com.clustercontrol.utility.settings.rpa.xml.Common
			versionRpaDto2Xml(Hashtable<String,String> ver){
	
		com.clustercontrol.utility.settings.rpa.xml.Common com =
				new com.clustercontrol.utility.settings.rpa.xml.Common();
				
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
	static public com.clustercontrol.utility.settings.rpa.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.rpa.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.rpa.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	public static RpaScenarioTag getScenarioTag(RpaScenarioTagResponse scenarioTagRes)
			throws IndexOutOfBoundsException, ParseException {

		RpaScenarioTag scenarioTag = new RpaScenarioTag();
		
		RpaScenarioTagInfo scenarioTagInfo = new RpaScenarioTagInfo();
		scenarioTagInfo.setTagId(scenarioTagRes.getTagId());
		scenarioTagInfo.setTagName(scenarioTagRes.getTagName());
		scenarioTagInfo.setTagPath(scenarioTagRes.getTagPath());
		scenarioTagInfo.setDescription(scenarioTagRes.getDescription());
		scenarioTagInfo.setOwnerRoleId(scenarioTagRes.getOwnerRoleId());
		
		scenarioTag.setRpaScenarioTagInfo(scenarioTagInfo);

		return scenarioTag;
	}
	
	public static String getParentTagId(String tagPath) {
		String ret = "";
		if (tagPath != null && !tagPath.isEmpty()){
			int index = tagPath.lastIndexOf("\\");
			ret = tagPath.substring(index + 1);
		}
		return ret;
	}
	
	/**
	 * Castor で作成した形式の RPAシナリオ設定情報を DTO へ変換する<BR>
	 *
	 */
	public static List<RpaScenarioTagResponse> createRpaScenarioTagList(RpaScenarioTags scenarioTags, List<RpaScenarioTagResponse> managerTagList) 
			throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, RpaScenarioTagNotFound, InvalidSetting, ParseException {
		List<RpaScenarioTagResponse> sceanrioTagList = new LinkedList<RpaScenarioTagResponse>();

		for (RpaScenarioTag tag : scenarioTags.getRpaScenarioTag()){
			
			List<RpaScenarioTag> xmlTagList = Arrays.asList(scenarioTags.getRpaScenarioTag());
			if(tag.getRpaScenarioTagInfo().getTagPath() == null){
				tag.getRpaScenarioTagInfo().setTagPath("");
			}
			
			String tagPathString = tag.getRpaScenarioTagInfo().getTagPath();
			int separatorIndex = tagPathString.indexOf("\\");
			if(-1 < separatorIndex){
				tagPathString = tagPathString.substring(separatorIndex + 1);
			}
			
			String[] tagPathArray = tagPathString.split("\\\\");
			
			// タグ階層バリデーションチェック
			for(String tagPath : tagPathArray){
				
				if(separatorIndex == -1){
					// タグ階層がセットされていない場合はスキップする
					if(tagPath.isEmpty()){
						continue;
					}
					
					// タグ階層がセットされていない場合以外で行頭に\が存在しない場合はエラーとする
					throw new ConvertorException(
							tag.getRpaScenarioTagInfo().getTagId()
							+ " " + Messages.getString("SettingTools.RpaScenarioTagPathNotFoundPathSeparator"));
				} else if(separatorIndex == 0){
					// タグ階層がセットされている、かつ空白の階層が存在する場合はエラーとする
					int lastSeparatorIndex = tagPathString.lastIndexOf("\\");
					if(tagPath.isEmpty() || lastSeparatorIndex+1 == tagPathString.length()){
						throw new ConvertorException(
								tag.getRpaScenarioTagInfo().getTagId()
								+ " " + Messages.getString("SettingTools.RpaScenarioTagPathContainsEmptyTadId"));
					}
					
					// タグ階層にIDに許容されていない文字列が存在する場合はエラーとする
					if(!tagPath.matches(PatternConstant.HINEMOS_ID_PATTERN)){
						throw new ConvertorException(
								tag.getRpaScenarioTagInfo().getTagId()
								+ " " + Messages.getString("SettingTools.RpaScenarioTagPathContainsInvalidId")
								+ " errorPath : " + tagPath);
					}
					
					// タグ階層に自身が含まれている場合はエラーとする
					if(tag.getRpaScenarioTagInfo().getTagId().equals(tagPath)){
						throw new ConvertorException(
								tag.getRpaScenarioTagInfo().getTagId()
								+ " " + Messages.getString("SettingTools.RpaScenarioTagPathContainsTadId"));
					}
					
					// xml上とDB上に存在しないタグを指定していた場合はエラーとする
					Optional<RpaScenarioTag> tagStream = 
							xmlTagList.stream().filter(t -> t.getRpaScenarioTagInfo().getTagId().equals(tagPath)).findFirst();
					Optional<RpaScenarioTagResponse> tagOnManagerStream = 
							managerTagList.stream().filter(t -> t.getTagId().equals(tagPath)).findFirst();
					if(!tagStream.isPresent() && !tagOnManagerStream.isPresent()){
						throw new ConvertorException(
								tag.getRpaScenarioTagInfo().getTagId()
								+ " " + Messages.getString("SettingTools.RpaScenarioTagPathContainsNonExistentTag")
								+ " errorPath : " + tagPath);
					}
				} else {
					// 行頭に\が存在せず階層途中に\が存在する場合はエラーとする
					throw new ConvertorException(
							tag.getRpaScenarioTagInfo().getTagId()
							+ " " + Messages.getString("SettingTools.RpaScenarioTagPathNotFoundPathSeparator"));
				}
			}
			
			RpaScenarioTagResponse scenarioTagInfo = 
					RpaScenarioTagConv.createRpaScenarioTag(tag.getRpaScenarioTagInfo());
			sceanrioTagList.add(scenarioTagInfo);
		}
		
		return sceanrioTagList;
	}
	
	public static void checkTagPath(RpaScenarioTag tag, List<RpaScenarioTag> xmlTagList, 
			List<RpaScenarioTagResponse> managerTagList) throws ConvertorException{
		// 直近の親タグを取得
		String parentId = getParentTagId(tag.getRpaScenarioTagInfo().getTagPath());
		String checkedTagPath = tag.getRpaScenarioTagInfo().getTagPath();
		
		if ("".equals(checkedTagPath)) {
			//最上位のため、チェック不要
			return;
		}
		
		try{
			String expectedTagPath = getXmlPathReplaced(parentId, tag.getRpaScenarioTagInfo().getTagPath(), "", xmlTagList, managerTagList);
			if (expectedTagPath == null) {
				expectedTagPath = getManagerPath(parentId, managerTagList);
				
				if (expectedTagPath == null) {
					//XMLにもDBにもタグが存在していない状態。事前にID存在チェックをしている為、本来はありえない。
					throw new ConvertorException();
				}
			}
			
			if (!checkedTagPath.equals(expectedTagPath)) {
				//XMLに記載されている階層と本来の階層が一致していない場合、エラー
				throw new ConvertorException();
			}
		} catch (ConvertorException e){
			throw new ConvertorException(
					tag.getRpaScenarioTagInfo().getTagId() 
					+ " " + Messages.getString("SettingTools.RpaScenarioTagPathDifferentFromParent"));
		}
	}
	
	private static String getParentId(String path) {
		if (path.isEmpty()){
			return "";
		} else {
			int index = path.lastIndexOf("\\");
			return path.substring(index + 1);
		}
	}
	
	private static String getXmlPathReplaced(String checkedId, String checkedPath, String childPath, List<RpaScenarioTag> xmlTagList, List<RpaScenarioTagResponse> managerTagList) throws ConvertorException{
		String id = checkedId;
		if (checkedPath != null && checkedPath.isEmpty()) {
			//最上位の場合
			return null;
		}
		
		for (RpaScenarioTag tag : xmlTagList){
			if(tag.getRpaScenarioTagInfo().getTagId().equals(id)){
				//XMLに存在していた場合
				return tag.getRpaScenarioTagInfo().getTagPath() + "\\" + tag.getRpaScenarioTagInfo().getTagId() + childPath;
			}
		}
		
		RpaScenarioTagResponse managerTag = null;
		//DBに定義されている親をチェック
		for (RpaScenarioTagResponse tag : managerTagList){
			if(tag.getTagId().equals(id)){
				//XMLに存在していた場合
				managerTag = tag;
			}
		}
		if (managerTag == null) {
			//XMLにもDBにもタグが存在していない状態。事前にID存在チェックをしている為、本来はありえない。
			throw new ConvertorException();
		}
		
		
		//XML上の親の親を編集
		int xmlIndex = checkedPath.lastIndexOf("\\");
		int xmlIndex2 = checkedPath.lastIndexOf("\\", xmlIndex - 1);
		
		// 親の親が存在しない場合は上位階層のチェックを終了し、DBでの階層をチェック
		if (xmlIndex2 == -1){
			return null;
		}
		
		String xmlCheckPath = checkedPath.substring(0, xmlIndex);
		String xmlChildPath = checkedPath.substring(xmlIndex) + childPath;
		String xmlParentId = checkedPath.substring(xmlIndex2 + 1, xmlIndex);
		//DB上の親を取得
		String managerParentId = getParentId(managerTag.getTagPath());
		
		if (!xmlParentId.equals(managerParentId)) {
			//XMLに書かれた階層とDB上の階層が一致していないのでエラー
			throw new ConvertorException();
		}
		
		return getXmlPathReplaced(xmlParentId, xmlCheckPath, xmlChildPath, xmlTagList, managerTagList);
	}
	
	private static String getManagerPath(String id, List<RpaScenarioTagResponse> managerTagList){
		for (RpaScenarioTagResponse tag : managerTagList){
			if(tag.getTagId().equals(id)){
				return tag.getTagPath() + "\\" + tag.getTagId();
			}
		}
		
		return null;
	}
	
	public static RpaScenarioTagResponse createRpaScenarioTag(RpaScenarioTagInfo info) {
	
		RpaScenarioTagResponse ret =new RpaScenarioTagResponse();
		
		try {
			ret.setTagId(info.getTagId());
			ret.setTagName(info.getTagName());
			ret.setTagPath(info.getTagPath());
			ret.setDescription(info.getDescription());
			ret.setOwnerRoleId(info.getOwnerRoleId());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	
		return ret;
	}
}
