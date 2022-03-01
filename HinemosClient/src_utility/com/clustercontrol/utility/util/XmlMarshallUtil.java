/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.util;

import java.io.Reader;

import org.castor.xml.XMLProperties;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLContext;

public class XmlMarshallUtil {
	
	/**
	 * XmlのUnmarshaller を取得
	 * 
	 * @param 変換対象XmlDtoクラス 
	 * @return Unmarshaller
	 */
	private static Unmarshaller getUnmarshaller(Class<?> clazz){
		XMLContext xmlContext = new XMLContext();
		// 下位互換向けにXMLの内容確認（順番チェック）を緩くしておく
		xmlContext.setProperty(XMLProperties.LENIENT_SEQUENCE_ORDER, true);
		Unmarshaller unmarshaller = xmlContext.createUnmarshaller();
		unmarshaller.setClass(clazz);
		return unmarshaller;
	}
	
	@SuppressWarnings("unchecked")
	public static  <T> T unmarshall( Class<T> clazz ,Reader reader) throws MarshalException, ValidationException{
		T obj = null;
		Unmarshaller unmarshaller =getUnmarshaller(clazz);
		obj = (T) unmarshaller.unmarshal(reader);
		return obj;
	}
	
}
