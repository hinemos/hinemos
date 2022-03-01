/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;

/**
 * Restに関連するデータオブジェクトの変換用ユーティリティクラス<BR>
 * 
 * 以下の用途を想定<BR>
 *・ Rest向けDTO 同士での オブジェクト変換（Request系DTO <=> Response系DTOを想定）<BR>
 * 
 */

public class RestBeanUtilBase {

	private static Log m_log = LogFactory.getLog(RestBeanUtilBase.class);

	/**
	 *  コンストラクタ <BR>
	 * 
	 */
	protected RestBeanUtilBase(){
	}
	
	/**
	 *Rest向けDTO 同士 、Rest向けDTO <=> INFOクラス(もしくはEntity) 、でのオブジェクト変換を行う <BR>
	 * <BR>
	 * クラス内メンバの名称が一致し、かつデータ型が一致する場合は、変換元のメンバの値を変換先のメンバに複写する。<BR>
	 * クラス内メンバがユーザー定義のデータクラスであれば、その内部のメンバについても同様に対応する<BR>
	 * <BR>
	 * クラス内メンバについてはListとMapのみ対応している。配列、Set等はスキップする。 <BR>
	 * また 以下の制約がある。 <BR>
	 * ・変換先側では無条件に ListはArrayList、MapはHashMapとなる <BR>
	 * ・Mapのkeyはsrcとdestで型が一致していなければ スキップ。<BR>
	 * ・List<Object>や Map<<key,Object>> について Object が 配列やCollectionの場合は正常に動作しない。
	 * <BR>
	 * @param srcBean
	 *            変換元Bean
	 * @param destBean
	 *            変換先Bean
	 * @throws HinemosUnknown,InvalidSetting
	 */
	public static void convertBeanSimple(Object srcBean, Object destBean) throws HinemosUnknown {
		try{
			new RestBeanUtilBase().convertBeanRecursive(srcBean, destBean);
		} catch (InvalidSetting e) {
			// convertBeanSubでは InvalidSettingは発生しない想定（RestBeanUtilとの互換性のためにthrowsを指定してる）
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 *  オブジェクト間のデータ変換を行う <BR>
	 * 
	 * throws の InvalidSettingは RestBeanUtilBeanを継承するRestBeanUtilでのみ利用される想定だが
	 * convertBeanRecursive メソッドのOverrideの都合上、設定している<BR>
	 * 
	 */
	protected void convertBeanRecursive(Object srcBean, Object destBean) throws HinemosUnknown,InvalidSetting {

		long start = System.currentTimeMillis();

		// srcがnull もしくは クラス内メンバ毎変換が不要な場合なら終了
		if (srcBean == null || !(isRecursiveTarget(srcBean.getClass()))) {
			return;
		}

		// src と destのメンバの一覧を取得（destは名前検索向けに、名前をキーにしてMap化）
		List<Field> srcFieldList = getClassFields(srcBean.getClass());
		Map<String, Field> destFieldMap = new HashMap<String, Field>();
		for (Field rec : getClassFields(destBean.getClass())) {
			destFieldMap.put(rec.getName(), rec);
		}
		
		try {

			for (Field srcField : srcFieldList) {
				srcField.setAccessible(true);
				
				// src側と名称が一致するdest側のメンバーを取得(見つからなければスキップ)
				Field destTargetField = destFieldMap.get(srcField.getName());
				if (destTargetField == null) {
					continue;
				}
				destTargetField.setAccessible(true);
				
				//srcメンバをdestメンバにコンバート行う
				convertMember(srcField,srcBean,destTargetField,destBean);
			}

			if (m_log.isTraceEnabled()) {
				m_log.trace(String.format("convertBean() : %s ms", (System.currentTimeMillis() - start)));
			}

		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * src側メンバの値をクラス型に合わせて変換した後、copyTargetへセットする。 <BR>
	 */
	protected void convertMember(Field srcField, Object srcObject, Field copyTargetField, Object copyTargetObject) throws Exception{		

		//コピー先が static finalなら処理対象外
		int mod = copyTargetField.getModifiers();
		if(Modifier.isStatic(mod) && (Modifier.isFinal(mod))){
			return;
		}

		// srcメンバのクラス型に基づいて変換
		Class<?> srcType = srcField.getType();
		if (srcType.isArray()) {
			// 配列なら処理対象外
			return;
		}

		if (List.class.isAssignableFrom(srcType)) {
			// List の変換(List<AAA> <-> List<AAA'>)
			convertList(srcField, srcObject, copyTargetField, copyTargetObject);
			return;
		}
		if (Map.class.isAssignableFrom(srcType)) {
			// Map の変換(Map<AAA,BBB> <-> Map<AAA,BBB'>)
			convertMap(srcField, srcObject, copyTargetField, copyTargetObject);
			return;
		}
		if (Collection.class.isAssignableFrom(srcType)) {
			// ここまでに対応が無かった集合型（Set Queueなど）なら処理対象外
			return;
		}
		Object srcValue = getSrcMemberValue(srcField, srcObject);
		if (isRecursiveTarget(srcType)) {
			// ユーザー定義データクラスならクラス内メンバーに対して再帰処理
			Object destValue = null;
			if (srcValue != null) {
				// destメンバが抽象クラス、もしくは、インタフェースの場合、コピー元インスタンスを設定してコピーする
				if(Modifier.isAbstract(copyTargetField.getType().getModifiers())
						|| Modifier.isInterface(copyTargetField.getType().getModifiers())) {
					destValue = srcValue.getClass().getDeclaredConstructor().newInstance();
				} else {
					destValue = copyTargetField.getType().getDeclaredConstructor().newInstance();
				}
				convertBeanRecursive(srcValue, destValue);
			}
			setTargetMemberValue(copyTargetField,copyTargetObject,destValue);
			return;
		} else {
			// クラス内メンバ毎の変換が必要ではない場合、型が一致していればそのまま複写
			if (srcType == copyTargetField.getType()) {
				setTargetMemberValue(copyTargetField,copyTargetObject,srcValue);
			} else {
				// プリミティブ型 -> そのラッパークラス の場合は変換してコピー
				if (srcType.isPrimitive() && !(copyTargetField.getType().isPrimitive())) {
					convertPrimitiveToWrapper(srcField, srcValue, copyTargetField, copyTargetObject);
				}
				// プリミティブラッパークラス ->プリミティブ型 の場合も変換してコピー
				if (!(srcType.isPrimitive()) && copyTargetField.getType().isPrimitive()
						&& srcValue != null) {
					convertWrapperToPrimitive(srcField, srcValue, copyTargetField, copyTargetObject);
				}
			}
			return;
		}
		
	}

	/**
	 * src側メンバの値を取得する<BR>
	 */
	protected Object getSrcMemberValue(Field srcField, Object srcObject) throws IllegalAccessException {
		Object srcValue = srcField.get(srcObject);
		return srcValue;
	}
	
	/**
	 * copyTarget側メンバの値を設定する<BR>
	 * @throws IllegalAccessException 
	 */
	protected void setTargetMemberValue(Field copyTargetField, Object copyTargetObject , Object destValue) throws IllegalAccessException  {
		copyTargetField.set(copyTargetObject, destValue);
		return ;
	}
	
	/**
	 * List<Object>間の変換を行う。 対象フィールドは setAccessible(true) となっている前提<BR>
	 */
	protected void convertList(Field srcField, Object srcObject, Field copyTargetField, Object copyTargetObject)
			throws HinemosUnknown,InvalidSetting {
		try {
			List<?> srcLists = (List<?>) getSrcMemberValue(srcField, srcObject);
			if (srcLists == null) {
				setTargetMemberValue(copyTargetField, copyTargetObject, null);
				return;
			}
			ParameterizedType tagPt = (ParameterizedType) copyTargetField.getGenericType();
			Class<?> destClazz = (Class<?>) tagPt.getActualTypeArguments()[0];
			ParameterizedType srcPt = (ParameterizedType) srcField.getGenericType();
			Class<?> srcClazz = (Class<?>) srcPt.getActualTypeArguments()[0];
			List<Object> destLists = new ArrayList<>();
			if (destClazz == srcClazz) {
				destLists.addAll(srcLists);
			} else {
				for (Object srcListRec : srcLists) {
					Object destListRec = destClazz.getDeclaredConstructor().newInstance();
					convertBeanRecursive(srcListRec, destListRec);
					destLists.add(destListRec);
				}
			}
			setTargetMemberValue(copyTargetField, copyTargetObject, destLists);
		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException e) {
			// destClazz.getDeclaredConstructor() で不正なクラスを扱った場合に発生
			m_log.error("convertList(): exception=" + e.getClass().getSimpleName() + ", message=" + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			// Field.setにてアクセスが不可能な場合や設定する値が不正な場合に入ってくる
			m_log.error("convertList(): exception=" + e.getClass().getSimpleName() + ", message=" + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * Map<Object,Object>間の変換を行う。 対象フィールドは setAccessible(true) となっている前提<BR>
	 */
	protected void convertMap(Field srcField, Object srcObject, Field copyTargetField, Object copyTargetObject)
			throws HinemosUnknown,InvalidSetting {
		try {
			if (!(Map.class.isAssignableFrom(copyTargetField.getType()))) {
				// copyTarget側がMapでなければ処理打ち切り
				return;
			}

			Map<?, ?> srcMap = (Map<?, ?>) getSrcMemberValue(srcField,srcObject);
			if (srcMap == null) {
				// src側がnullならnullにて複写する
				setTargetMemberValue(copyTargetField, copyTargetObject, null);
				return;
			}

			// src側の型情報を取得
			ParameterizedType srcRecParamType = (ParameterizedType) srcField.getGenericType();
			Class<?> srcKeyClass = (Class<?>) srcRecParamType.getActualTypeArguments()[0];
			Class<?> srcValClass = (Class<?>) srcRecParamType.getActualTypeArguments()[1];

			// copyTarget側の型情報を取得
			ParameterizedType tagRecParamType = (ParameterizedType) copyTargetField.getGenericType();
			Class<?> copyTargetKeyClass = (Class<?>) tagRecParamType.getActualTypeArguments()[0];
			Class<?> copyTargetValClass = (Class<?>) tagRecParamType.getActualTypeArguments()[1];

			// key情報の型が一致しないなら処理しない
			if (srcKeyClass != copyTargetKeyClass) {
				return;
			}

			Map<Object, Object> destMap = new HashMap<>();
			if (srcValClass == copyTargetValClass) {
				// value情報の型が完全一致ならオールイン
				destMap.putAll(srcMap);
			} else {
				// 型が不一致なら srcのvalueレコード毎に変換を試行してdestへ設定
				for (Entry<?, ?> srcMapRec : srcMap.entrySet()) {
					Object destMapValue = copyTargetValClass.getDeclaredConstructor().newInstance();
					convertBeanRecursive(srcMapRec.getValue(), destMapValue);
					destMap.put(srcMapRec.getKey(), destMapValue);
				}
			}
			copyTargetField.setAccessible(true);
			setTargetMemberValue(copyTargetField,copyTargetObject,destMap);
		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException e) {
			// getDeclaredConstructor() で不正なクラスを扱った場合に発生
			m_log.error("convertMap(): exception=" + e.getClass().getSimpleName() + ", message=" + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			// Field.setにてアクセスが不可能な場合や設定する値が不正な場合に入ってくる
			m_log.error("convertMap(): exception=" + e.getClass().getSimpleName() + ", message=" + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	
	/**
	 * プリミティブ型からラッパークラスへの変換を行う。 対象フィールドは setAccessible(true)
	 * となっている前提<BR>
	 * 
	 */
	protected void convertPrimitiveToWrapper(Field srcField, Object srcValue, Field copyTargetField,
			Object copyTargetObject) throws HinemosUnknown {
		try {
			Class<?> srcType = srcField.getType();
			Class<?> tagType = copyTargetField.getType();
			Object setValue = null;
			if (srcType == Integer.TYPE && tagType == Integer.class) {
				int srcInt = (int) srcValue;
				Integer setInteger = srcInt;
				setValue = setInteger;
			}
			if (srcType == Long.TYPE && tagType == Long.class) {
				long srcLong = (long) srcValue;
				Long setLong = srcLong;
				setValue = setLong;
			}
			if (srcType == Boolean.TYPE && tagType == Boolean.class) {
				boolean srcBoolean = (boolean) srcValue;
				Boolean setBoolean = srcBoolean;
				setValue = setBoolean;
			}
			if (srcType == Double.TYPE && tagType == Double.class) {
				setValue = new Double((double) srcValue );
			}
			if (srcType == Float.TYPE && tagType == Float.class) {
				setValue = new Float((float) srcValue);
			}
			if (srcType == Short.TYPE && tagType == Short.class) {
				short srcShort = (short) srcValue;
				Short setShort = srcShort;
				setValue = setShort;
			}
			if (srcType == Byte.TYPE && tagType == Byte.class) {
				byte srcByte = (byte) srcValue;
				Byte setByte = srcByte;
				setValue = setByte;
			}
			if (srcType == Character.TYPE && copyTargetField.getType() == Character.class) {
				char srcChar = (char) srcValue;
				Character setCharacter = srcChar;
				setValue = setCharacter;
			}
			if (setValue != null) {
				// 変換結果を設定
				setTargetMemberValue(copyTargetField, copyTargetObject, setValue);
			}
		} catch (IllegalAccessException e) {
			// Field.setにてアクセスが不可能な場合や設定する値が不正な場合に入ってくる
			m_log.error("convertPrimitiveToWrapper(): exception=" + e.getClass().getSimpleName() + ", message=" + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * ラッパークラスからプリミティブ型への変換を行う。 対象フィールドは setAccessible(true)
	 * となっている前提<BR>
	 * 
	 */
	protected void convertWrapperToPrimitive(Field srcField, Object srcValue, Field copyTargetField,
			Object copyTargetObject) throws HinemosUnknown {
		try {
			Class<?> srcType = srcField.getType();
			Class<?> tagType = copyTargetField.getType();
			Object setValue = null;
			if (srcType == Integer.class && tagType == Integer.TYPE) {
				int setInt =(Integer) srcValue;
				setValue = setInt;
			}
			if (srcType == Long.class && tagType == Long.TYPE) {
				long setLong =(Long) srcValue;
				setValue = setLong;
			}
			if (srcType == Boolean.class && tagType == Boolean.TYPE) {
				boolean setBoolean = (Boolean) srcValue;
				setValue = setBoolean;
			}
			if (srcType == Double.class && tagType == Double.TYPE) {
				double setDouble = (Double) srcValue;
				setValue = setDouble;
			}
			if (srcType == Float.class && tagType == Float.TYPE) {
				float setFloat = (Float) srcValue;
				setValue = setFloat;
			}
			if (srcType == Short.class && tagType == Short.TYPE) {
				short setShort = (Short) srcValue;
				setValue = setShort;
			}
			if (srcType == Byte.class && tagType == Byte.TYPE) {
				byte setByte = (Byte) srcValue;
				setValue = setByte;
			}
			if (srcType == Character.class && tagType == Character.TYPE) {
				char setChar = (Character) srcValue;
				setValue = setChar;
			}
			if (setValue != null) {
				// 変換結果を設定
				setTargetMemberValue(copyTargetField, copyTargetObject, setValue);
			}
		} catch (IllegalAccessException e) {
			// Field.setにてアクセスが不可能な場合や設定する値が不正な場合に入ってくる
			m_log.error("convertWrapperToPrimitive(): exception=" + e.getClass().getSimpleName() + ", message=" + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
	
	/**
	 * クラス内のメンバ一覧を取得（スーパークラスのメンバ含む）<BR>
	 */
	protected static List<Field> getClassFields(Class<?> targetClass) {
		Field[] tatgetFields = targetClass.getDeclaredFields();
		Class<?> superClass = targetClass.getSuperclass();
		if (superClass != null) {
			List<Field> superFields = getClassFields(superClass);
			superFields.addAll(Arrays.asList(tatgetFields));
			return superFields;
		} else {
			List<Field> ret = new ArrayList<Field>();
			ret.addAll(Arrays.asList(tatgetFields));
			return ret;
		}
	}

	/**
	 *  再帰処理対象のクラスかどうか判定<BR>
	 * <BR>
	 */
	protected static boolean isRecursiveTarget(Class<?> type) {
		// プリミティブ型とそのラッパー、Enum、集合系クラスは再帰処理 対象外とする
		if (type == String.class || type == Integer.class || type == Double.class || type == Long.class
				|| type == Boolean.class || type == Float.class || type == Short.class || type == Byte.class
				|| type == Character.class || type.isEnum() || type.isPrimitive()
				|| Collection.class.isAssignableFrom(type)) {
			return false;
		}
		return true;
	}

}
