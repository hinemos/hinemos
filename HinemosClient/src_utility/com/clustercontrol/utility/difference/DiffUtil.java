/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.utility.difference.anno.AnnoSubstitute;
import com.clustercontrol.utility.difference.anno.Element;
import com.clustercontrol.utility.difference.anno.PrimaryKey;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.util.MultiManagerPathUtil;

/**
 * 差分検出処理のための、ユーティリティクラス。
 * 
 * @version 2.2.0
 * @since 2.0.0
 * 
 * 
 */
public class DiffUtil {
	private static Log logger = LogFactory.getLog(DiffUtil.class);

	private static AnnotationManager am = new AnnotationManagerImpl();

	/**
	 * クラスに付加されたアノテーションを取得。
	 * 
	 * @param <T>
	 * @param method
	 * @param anno
	 * @return
	 */
	public static <T extends AnnoSubstitute> T getAnnotationByAll(Class<?> targetClass, Class<T> anno) {
		if (targetClass.isPrimitive()) {
			return null;
		}

		for (Class<?> clazz = targetClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
			AnnoSubstitute[] annos = am.getClassAnno(clazz);
			if (annos == null) {
				continue;
			}

			for (AnnoSubstitute a : annos) {
				if (a.getClass() == anno) {
					return anno.cast(a);
				}
			}
		}

		return null;
	}
	
	/**
	 * 関数に付加されたアノテーションを取得。
	 * 
	 * @param <T>
	 * @param method
	 * @param anno
	 * @return
	 */
	private static Map<String, Method> getPropGetMethodByAnno(Class<?> targetClass, Class<? extends AnnoSubstitute> anno) {
		if (targetClass.isPrimitive()) {
			return null;
		}

		Map<String, Method> methodMap = new HashMap<String, Method>();
		for (Class<?> clazz = targetClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
			for (Method method : clazz.getMethods()) {
				if (isPropGet(method) && getAnnotation(method, anno) != null) {
					methodMap.put(method.getName(), method);
				}
			}
		}

		return methodMap;
	}
	
	public static String getPK(Object obj, Class<?> targetClass) {
		String pk = null;
		Map<String, Method> methodMap = getPropGetMethodByAnno(targetClass, PrimaryKey.class);
		if (methodMap != null) {
			for (Method method : methodMap.values()) {
				Object pkObj = getProperty(obj, method);
				if (pkObj != null) {
					Element elementAnno = getAnnotationByAll(method.getReturnType(), Element.class);
					if (elementAnno != null) {
						if(pk != null)
							pk = pk + "-" + getPK(pkObj, method.getReturnType());
						else
							pk = getPK(pkObj, method.getReturnType());
					} else {
						if(pk != null)
							pk = pk + "-" + pkObj.toString();
						else
							pk = pkObj.toString();
					}
				}	
			}
		}

		return pk;
	}

	public static Object getProperty(Object obj, String propName, Class<?> targetClass) {
		Object prop = null;
		Method method = getPropGetMethod(propName, targetClass);
		if (method != null) {
			prop = getProperty(obj, method);
		}

		return prop;
	}

	public static Method getPropGetMethod(String propName, Class<?> targetClass) {
		if (targetClass.isPrimitive()) {
			return null;
		}

		for (Class<?> clazz = targetClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
			for (Method method : clazz.getMethods()) {
				if (isPropGet(method) && method.getName().substring("get".length()).compareToIgnoreCase(propName) == 0) {
					return method;
				}
			}
		}

		return null;
	}

	/**
	 * クラスに付加されたアノテーションを取得。
	 * 
	 * @param <T>
	 * @param method
	 * @param anno
	 * @return
	 */
	public static <T extends AnnoSubstitute> T getAnnotation(Class<?> clazz, Class<T> anno) {
		AnnoSubstitute[] annos = am.getClassAnno(clazz);
		if (annos == null) {
			return null;
		}

		for (AnnoSubstitute a : annos) {
			if (a.getClass() == anno) {
				return anno.cast(a);
			}
		}

		return null;
	}

	/**
	 * 関数に付加されたアノテーションを取得。
	 * 
	 * @param <T>
	 * @param method
	 * @param anno
	 * @return
	 */
	public static <T extends AnnoSubstitute> T getAnnotation(Method method, Class<T> anno) {
		AnnoSubstitute[] annos = am.getPropAnno(method);
		if (annos == null) {
			return null;
		}

		for (AnnoSubstitute a : annos) {
			if (a.getClass() == anno) {
				return anno.cast(a);
			}
		}

		return null;
	}

	public static boolean isPropGet(Method method) {
		// パブリックでないプロパティは、対象外。
		if (!Modifier.isPublic(method.getModifiers())) {
			return false;
		}

		// static 定義されているのは、対象外。
		if (Modifier.isStatic(method.getModifiers())) {
			return false;
		}

		// 共変は、対象外。
		if (method.isSynthetic()) {
			return false;
		}

		// 引数が 0 個。
		if (method.getParameterTypes().length != 0) {
			return false;
		}

		// 関数名の先頭は、get がつく。
		if (!method.getName().startsWith("get")) {
			return false;
		}

		// 戻り値は、void でない。
		if (Void.class.isAssignableFrom(method.getReturnType())
				|| void.class.isAssignableFrom(method.getReturnType())) {
			return false;
		}

		return true;
	}

	public static Object getProperty(Object obj, Method propGet) {
		Object prop = null;
		try {
			prop = propGet.invoke(obj);
		} catch (Exception e) {
			throw new IllegalStateException("unexpected", e);
		}
		
		if (prop == null) {
			return null;
		}

		return prop;
	}

	public static AnnotationManager getAnnotationManager() {
		return am;
	}

	public static ResultA diffCheck(Object dto1, Object dto2, Class<?> targetClass) {
		DiffChecker dc = new DiffChecker(dto1, dto2, targetClass);
		dc.check();
		return dc.getResultA();
	}

	public static ResultA diffCheck(Object dto1, Object dto2, Class<?> targetClass, ResultA resultA) {
		DiffChecker dc = new DiffChecker(dto1, dto2, targetClass, resultA);
		dc.check();
		return dc.getResultA();
	}

	public static boolean diffCheck2(Object dto1, Object dto2, Class<?> targetClass, ResultA resultA) {
		logger.info("output all difference flag : " + Boolean.toString(isAll()));

		DiffChecker dc = new DiffChecker(dto1, dto2, targetClass, resultA);
		return dc.check();
	}
	
	public static boolean isAll() {
		String value = MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.KEY_DIFF_MODE);
		if (value == null) {
			return false;
		} else if (value.equals("true")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * xmlタグ上に該当の要素があったか検証する
	 * @param obj
	 * @param method
	 * @param targetClass
	 * @return
	 */
	public static boolean getHasProp(Object obj, Method method) {
		try {
			String testhas = method.getName().replaceFirst("get", "has");
			boolean result = true;
			StringBuilder typeName = new StringBuilder();
			typeName.append(obj.getClass().toString(), 6, obj.getClass().toString().length());
			typeName.append("Type");
			Class<?> targetClass = Class.forName(typeName.toString());
			Object flg = getHasProperty(obj, testhas, targetClass);
			if (flg != null) {
				result = ((Boolean) flg).booleanValue();
			}
			return result;
		} catch (ClassNotFoundException e) {
			// 対象のhasメソッドが見つからない場合は元々定義されていないので、trueを返却する。
			// 返却先で要素の比較を行わせるため。
			return true;
		}
	}

	/**
	 * hasプロパティを実際に取得するメソッド
	 * @param obj
	 * @param propGet
	 * @return
	 */
	public static Object getHasProperty(Object obj, Method propGet) {
		Object prop = null;
		try {
			prop = propGet.invoke(obj);
		} catch (Exception e) {
			throw new IllegalStateException("unexpected", e);
		}
		
		if (prop == null) {
			return null;
		}

		return prop;
	}

	/**
	 * hasメソッドを取得するクラスとメソッド名
	 * @param obj
	 * @param propName
	 * @param targetClass
	 * @return
	 */
	public static Object getHasProperty(Object obj, String propName, Class<?> targetClass) {
		Object prop = null;
		Method method = getHasPropGetMethod(propName, targetClass);
		if (method != null) {
			prop = getHasProperty(obj, method);
		}

		return prop;
	}

	/**
	 * クラスより、hasメソッドを抽出する
	 * @param propName
	 * @param targetClass
	 * @return
	 */
	public static Method getHasPropGetMethod(String propName, Class<?> targetClass) {
		if (targetClass.isPrimitive()) {
			return null;
		}
		for (Class<?> clazz = targetClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
			for (Method method : clazz.getMethods()) {
				if (isPropHas(method) && method.getName().compareToIgnoreCase(propName) == 0) {
					return method;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * メソッドがhasが先頭につき、.*type.javaで定義されているものかを検証する
	 * @param method
	 * @return
	 */
	public static boolean isPropHas(Method method) {
		// パブリックでないプロパティは、対象外。
		if (!Modifier.isPublic(method.getModifiers())) {
			return false;
		}
		
		// static 定義されているのは、対象外。
		if (Modifier.isStatic(method.getModifiers())) {
			return false;
		}
		
		// 共変は、対象外。
		if (method.isSynthetic()) {
			return false;
		}
		
		// 引数が 0 個。
		if (method.getParameterTypes().length != 0) {
			return false;
		}
		
		// 関数名の先頭は、has がつく。
		if (!method.getName().startsWith("has")) {
			return false;
		}
		
		// 戻り値は、void でない。
		if (Void.class.isAssignableFrom(method.getReturnType())
				|| void.class.isAssignableFrom(method.getReturnType())) {
			return false;
		}
		
		return true;
	}
}