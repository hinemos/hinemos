/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.util;

import java.io.IOException;
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
		unmarshaller.setWhitespacePreserve(true);
		return unmarshaller;
	}

	/**
	 * Xmlのunmarshallを実行。
	 * 本メソッド内で引数のReaderはクローズされる。
	 */
	@SuppressWarnings("unchecked")
	public static  <T> T unmarshall( Class<T> clazz ,Reader reader) throws MarshalException, ValidationException, IOException{
		T obj = null;
		Unmarshaller unmarshaller =getUnmarshaller(clazz);
		try {
			obj = (T) unmarshaller.unmarshal(reader);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				throw e;
			}
		}
		return obj;
	}
	
}
