/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.util;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.rest.annotation.RestItemName;

public class RestItemNameResolver {

	private static Log m_log = LogFactory.getLog(RestItemNameResolver.class);

	// 引数のクラスから指定のフィールドの項目名(@RestItemNameで指定されたキーの値)を解決します。
	// 実装不備に気がつけるように「存在しないフィールド」や「アノテーションが付与されていないフィールド」
	// が渡された場合は null を返します。
	public static String resolveItenName(Class<?> clazz, String item) {
		String ret = null;
		Class<?> targetClazz = clazz;
		try {
			Field f = null;
			while (targetClazz != null) {
				try {
					f = targetClazz.getDeclaredField(item);
					break;
				} catch (NoSuchFieldException e) {
					targetClazz = targetClazz.getSuperclass();
				}
			}
			RestItemName annotation =null; 
			if (f != null) {
				annotation = f.getAnnotation(RestItemName.class);
			}
			if (annotation != null) {
				ret = annotation.value().getMessage();
			}
		} catch (Exception e) {
			m_log.warn(e.getMessage());
		}

		return ret;
	}
}
