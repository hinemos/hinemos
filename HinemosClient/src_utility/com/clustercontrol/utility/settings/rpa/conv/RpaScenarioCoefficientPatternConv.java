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
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openapitools.client.model.RpaScenarioCoefficientPatternResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RpaManagementToolAccountNotFound;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioCoefficientPattern;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioCoefficientPatternInfo;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioCoefficientPatterns;

/**
 * 自動化効果計算マスタ情報のJavaBeanとXML(Bean)のbindingとのやりとりを行うクラス
 * 
 */
public class RpaScenarioCoefficientPatternConv {
	
	static private final String schemaType="K";
	static private final String schemaVersion="1";
	static private String schemaRevision ="1";
	
	/* ロガー */
	private static Logger log = Logger.getLogger(RpaScenarioCoefficientPatternConv.class);
	
	/**
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
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
	 * XMLとツールの対応バージョンをチェック
	 * 
	 */
	static public int checkSchemaVersion(String type, String version ,String revision){
		return BaseConv.checkSchemaVersion(
				schemaType,schemaVersion,schemaRevision,
				type,version,revision);
	}
	
	/**
	 * スキーマのバージョンを返します。
	 * 
	 */
	static public com.clustercontrol.utility.settings.rpa.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.rpa.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.rpa.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	/**
	 * Castor で作成した形式の RPAシナリオ設定情報を DTO へ変換する<BR>
	 *
	 */
	public static List<RpaScenarioCoefficientPatternResponse> createRpaScenarioCoefficientPatternList(RpaScenarioCoefficientPatterns patterns) 
			throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, RpaManagementToolAccountNotFound, InvalidSetting, ParseException {
		List<RpaScenarioCoefficientPatternResponse> patternList = new LinkedList<RpaScenarioCoefficientPatternResponse>();

		for (RpaScenarioCoefficientPattern pattern : patterns.getRpaScenarioCoefficientPattern()) {
			log.debug("RpaToolEnvId : " + pattern.getRpaScenarioCoefficientPatternInfo().getRpaToolEnvId() 
						+ " , " + pattern.getRpaScenarioCoefficientPatternInfo().getOrderNo());
			RpaScenarioCoefficientPatternResponse patternInfo = 
					createRpaScenarioCoefficientPattern(pattern.getRpaScenarioCoefficientPatternInfo());

			patternList.add(patternInfo);
		}

		return patternList;
	}
	
	public static RpaScenarioCoefficientPatternResponse createRpaScenarioCoefficientPattern(RpaScenarioCoefficientPatternInfo info) {
		
		RpaScenarioCoefficientPatternResponse ret =new RpaScenarioCoefficientPatternResponse();
		
		try {
			ret.setRpaToolEnvId(info.getRpaToolEnvId());
			ret.setOrderNo(info.getOrderNo());
			ret.setCoefficient(info.getCoefficient());
			ret.setPattern(info.getPattern());
			ret.caseSensitivityFlg(info.getCaseSensitivity());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	
		return ret;
	}
	
	public static RpaScenarioCoefficientPattern getRpaScenarioCoefficientPattern(RpaScenarioCoefficientPatternResponse patternRes)
			throws IndexOutOfBoundsException, ParseException {

		RpaScenarioCoefficientPattern pattern = new RpaScenarioCoefficientPattern();
		
		RpaScenarioCoefficientPatternInfo patternInfo = new RpaScenarioCoefficientPatternInfo();
		patternInfo.setRpaToolEnvId(patternRes.getRpaToolEnvId());
		patternInfo.setOrderNo(patternRes.getOrderNo());
		patternInfo.setCoefficient(patternRes.getCoefficient());
		patternInfo.setPattern(patternRes.getPattern());
		patternInfo.setCaseSensitivity(patternRes.getCaseSensitivityFlg());
		
		pattern.setRpaScenarioCoefficientPatternInfo(patternInfo);

		return pattern;
	}
	
}
