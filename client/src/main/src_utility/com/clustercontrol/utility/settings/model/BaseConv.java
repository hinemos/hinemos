/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.model;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * メンテナンス設定情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.0.0
 * @since 5.0.a
 * 
 */
public abstract class BaseConv {

	private Log log = LogFactory.getLog(this.getClass());
	
	public Log getLogger(){return log;};
	
	protected abstract String getType();
	protected abstract String getVersion();
	protected abstract String getRevision();
	
	/**
	 * XMLとツールの対応バージョンをチェック 
	 */
	public int checkSchemaVersion(String type, String version ,String revision){
		
		return checkSchemaVersion(type, version, revision,getType(), getVersion(), getRevision());
	}

	/**
	 * XMLとツールの対応バージョンをチェック 
	 */
	public static int checkSchemaVersion(String type_tool, String version_tool, String revision_tool, String type_xml, String version_xml, String revision_xml) {

		if(type_tool.equals(type_xml)){
			//スキーマタイプ一致
			if(Integer.parseInt(version_tool) == Integer.parseInt(version_xml) ){
				//バージョン一致
				return 0;
			}else if(Integer.parseInt(version_tool) > Integer.parseInt(version_xml)){
				//APの方がXMLのバージョンより新しい
				return -3;
			}else{
				//XMLの方がAPのバージョンより新しい
				return -2;
			}
		}else{
			//スキーマタイプ不一致
			return -1;
		}
	}
	
	
	/**
	 * スキーマのバージョンを返します。
	 * @return
	 * @throws Exception 
	 */
	public <T> T getSchemaVersion(Class<T> clazz) throws Exception{
		try {
			T schema = clazz.newInstance();
			Method method = clazz.getMethod("setSchemaType", String.class);
			method.invoke(schema, getType());
			method = clazz.getMethod("setSchemaVersion", String.class);
			method.invoke(schema, getVersion());
			method = clazz.getMethod("setSchemaRevision", String.class);
			method.invoke(schema, getRevision());
			return schema;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new Exception("Schema information creation fault. ", e);
		}
	}
	
	protected String ifNull2Empty(String str){
		if(str == null){
			return "";
		}
		return str;
	}
	
	protected String ifNull2EmptyAndNonNull2String(Object obj){
		if(obj == null){
			return "";
		}
		return obj.toString();
	}

	/*
	protected Integer str2Int(String str){
		try{
			return Integer.parseInt(str);
		} catch (NumberFormatException e){
			return null;
		}
	}
	*/
	
	protected Long str2Long(String str){
		try{
			return Long.parseLong(str);
		} catch (NumberFormatException e){
			return null;
		}
	}	
	
	protected Boolean str2Bool(String str){
		if(str != null && str.equalsIgnoreCase("false")){
			return Boolean.FALSE;
		} else {
			Boolean b = Boolean.parseBoolean(str);
			if(b){
				return b;
			} else {
				return Boolean.FALSE;
			}
		}
	}
}
