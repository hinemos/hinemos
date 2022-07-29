/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BeanUtil {
	private static final Log log = LogFactory.getLog(BeanUtil.class);

	/**
	 * JavaBeansの全プロパティの値(getClassは除く)が、
	 * nullあるいは空白のみの文字列であるかどうかを返します。
	 * 
	 * @param o 検査するJavaBeansオブジェクト。
	 * @return nullあるいは空白のみの文字列の場合はtrue、それ以外はfalse。
	 */
	public static boolean isBlank(Object o) {
		BeanInfo bi;
		try {
			bi = Introspector.getBeanInfo(o.getClass());
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
		for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
			if (pd.getName().equals("class")) continue;
			Object r;
			try {
				r = pd.getReadMethod().invoke(o);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
			if (r instanceof String && ((String) r).trim().length() == 0) continue;
			if (r != null) {
				log.debug("isBlank: '" + pd.getName() + "' is not blank. value=" + r.toString());
				return false;			
			}
		}
		return true;
	}
	
}
