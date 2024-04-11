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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssignGetter;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssignSetter;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIgnore;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertNullCopy;
import com.clustercontrol.rest.dto.EnumDto;

/**
 * Restに関連するデータオブジェクトの変換用ユーティリティクラス<BR>
 * 
 * 以下の用途を想定<BR>
 *・ Rest向けDTO <=> INFOクラス(もしくはEntity)<BR>
 * 
 */

public class RestBeanUtil extends RestBeanUtilBase {

	private static Log m_log = LogFactory.getLog(RestBeanUtil.class);

	private RestBeanUtil(){
	}
	
	// リフレクションしたメソッドをキャッシュするか
	private boolean useMethodCacheFlg = false;
	
	// スレッド毎に共有されるメソッドのキャッシュ
	private static final ThreadLocal<Map<Field, Method>> threadLocalGetterMap = new ThreadLocal<Map<Field, Method>>() {
		@Override
		protected Map<Field, Method> initialValue() {
			return new HashMap<>();
		}
	};
	private static final ThreadLocal<Map<Field, Method>> threadLocalSetterMap = new ThreadLocal<Map<Field, Method>>() {
		@Override
		protected Map<Field, Method> initialValue() {
			return new HashMap<>();
		}
	};

	/**
	 * キャッシュまたはリフレクションからGetterメソッドを取得<BR>
	 * メソッドが存在しない場合はnullを返す。
	 */
	private Method getGetter(Class<?> objectClass, Field srcField, String methodName) {
		// キャッシュを取得
		Map<Field, Method> methodMap = threadLocalGetterMap.get();
		if (!methodMap.containsKey(srcField)) {
			try {
				// キャッシュに未登録の場合はリフレクションで取得
				m_log.debug(String.format("put %s#%s on cache.",objectClass.getSimpleName(), methodName));
				methodMap.put(srcField, objectClass.getMethod(methodName));
			} catch (NoSuchMethodException | SecurityException e) {
				m_log.debug(String.format("%s#%s is not found.",objectClass.getSimpleName(), methodName));
				methodMap.put(srcField, null);
			}
		}
		return methodMap.get(srcField);
	}

	/**
	 * キャッシュまたはリフレクションからSetterメソッドを取得<BR>
	 * メソッドが存在しない場合はnullを返す。
	 */
	private Method getSetter(Class<?> objectClass, Field targetField, String methodName, Class<?> valueClass) {
		// キャッシュを取得
		Map<Field, Method> methodMap = threadLocalSetterMap.get();
		if (!methodMap.containsKey(targetField)) {
			try {
				// キャッシュに未登録の場合はリフレクションで取得
				m_log.debug(String.format("put %s#%s on cache.",objectClass.getSimpleName(), methodName));
				methodMap.put(targetField, objectClass.getMethod(methodName, valueClass));
			} catch (NoSuchMethodException | SecurityException e) {
				m_log.debug(String.format("%s#%s is not found.",objectClass.getSimpleName(), methodName));
				methodMap.put(targetField, null);
			}
		}
		return methodMap.get(targetField);
	}

	/**
	 * Rest向けDTO <=> INFOクラス間のデータ変換を行う <BR>
	 * <BR>
	 * 処理の内容はconvertBeanと同等だが、日時文字列によるInvalidSettingがありえない場合(INFO->DTO変換など)は
	 * こちらのメソッド利用することで InvalidSetting の throw対応を抑止できる。
	 */
	public static void convertBeanNoInvalid(Object srcBean, Object destBean) throws HinemosUnknown {
		try {
			convertBean(srcBean, destBean);
		} catch (InvalidSetting e) {
			throw new HinemosUnknown(e);
		}
	}

	
	/**
	 * Rest向けDTO <=> INFOクラス(もしくはEntity) 間のデータ変換を行う <BR>
	 * <BR>
	 * クラス内メンバの名称が一致し、かつデータ型が一致する場合は、変換元のメンバの値を変換先のメンバに複写する。<BR>
	 * クラス内メンバがユーザー定義のデータクラスであれば、その内部のメンバについても同様に対応する<BR>
	 * クラス内メンバに型変換向けアノテーションがあれば、型変換して複写する。<BR>
	 * <BR>
	 * DTOクラスにID変換向けアノテーションがあれば、内部メンバのID変換を行う<BR>
	 * DTO->INFOのID変換：src内id向けメンバをid用クラスに変換してdest側idにセット。
	 * INFO->DTOのID変換：src内id用クラス内メンバから値を取得してdest側対応メンバにセット。 <BR>
	 * メソッドパラメータとしては 配列やコレクションには対応していない<BR>
	 * <BR>
	 * クラス内メンバについてはListとMapのみ対応している。配列、Set等はスキップする。 <BR>
	 * ただし 以下の制約がある。 <BR>
	 * 変換先側では無条件に ListはArrayList、MapはHashMapとなる <BR>
	 * Mapのkeyはsrcとdestで型が一致していなければ スキップ。<BR>
	 * List<Object>や Map<<key,Object>> について Object が 配列やCollectionの場合は正常に動作しない。
	 * <BR>
	 * <BR>
	 * InvalidSettingは DTO->INFOにおける日時文字のHinemosTime変換時にのみ発生する。 <BR>
	 * 
	 * @param srcBean
	 *            変換元Bean
	 * @param destBean
	 *            変換先Bean
	 * @throws HinemosUnknown,InvalidSetting
	 * 
	 */
	public static void  convertBean(Object srcBean, Object destBean) throws HinemosUnknown, InvalidSetting {
		new RestBeanUtil().convertBeanRecursive(srcBean, destBean);
	}
	
	public static void convertBean(Object srcBean, Object destBean, boolean useMethodCacheFlg) throws HinemosUnknown, InvalidSetting {
		RestBeanUtil util = new RestBeanUtil();
		util.useMethodCacheFlg = useMethodCacheFlg;
		util.convertBeanRecursive(srcBean, destBean);
	}

	/**
	 * Rest向けDTO <=> INFOクラス(もしくはEntity) 間のデータ変換を行う <BR>
	 */
	@Override
	protected void convertBeanRecursive(Object srcBean, Object destBean) throws HinemosUnknown, InvalidSetting {

		// DTO->INFO でのID変換における、INFO側IDクラス内メンバに該当するDTOメンバのリスト（名称検索用にMap化）
		Map<String, Field> dtoIdFieldMap = new HashMap<String, Field>();
		// INFO->DTO でのID変換における IDを保持するINFO側メンバの名称
		String infoIdFieldName = null;
		// INFO->DTO でのID変換における INFO側IDクラスのインスタンス
		Object infoIdFieldInstance = null;

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

		// DTO->INFOのID変換設定(アノテーション)が有れば、INFO側ID内メンバとなるDTOメンバの名称を、設定を元に取得
		RestBeanConvertIdClassSet idConvDtoToInfSet = srcBean.getClass().getAnnotation(RestBeanConvertIdClassSet.class);
		if (idConvDtoToInfSet != null && idConvDtoToInfSet.infoClass().isAssignableFrom(destBean.getClass())) {
			Field idField = destFieldMap.get(idConvDtoToInfSet.idName());
			if (idField != null) {
				for (Field infoIdMember : idField.getType().getDeclaredFields()) {
					dtoIdFieldMap.put(infoIdMember.getName(), null);
				}
			}
		}

		// INFO->DTOのID変換設定(アノテーション)が有れば、IDを保持するINFO側メンバの名称を、設定から取得
		RestBeanConvertIdClassSet idConvInfToDtoSet = destBean.getClass().getAnnotation(RestBeanConvertIdClassSet.class);
		if (idConvInfToDtoSet != null && idConvInfToDtoSet.infoClass().isAssignableFrom(srcBean.getClass())) {
			infoIdFieldName = idConvInfToDtoSet.idName();
		}

		try {
			// convert src の各メンバー毎にdestメンバへの変換を行う
			for (Field srcField : srcFieldList) {
				srcField.setAccessible(true);

				// 対象外（アノテーション設定）ならスキップ
				if (srcField.getAnnotation(RestBeanConvertIgnore.class) != null) {
					continue;
				}
				// DTO->INFO ID変換向けの DTO側ID向けメンバなら、退避して別途処理（DTO->INFO ID変換 参照）
				if (dtoIdFieldMap.containsKey(srcField.getName())) {
					dtoIdFieldMap.put(srcField.getName(), srcField);
					continue;
				}
				// INFO->DTO ID変換向けの INFO側IDメンバなら、退避して別途処理（INFO->DTO ID変換 参照）
				if (infoIdFieldName != null && srcField.getName().equals(infoIdFieldName)) {
					infoIdFieldInstance = getSrcMemberValue(srcField, srcBean);
					continue;
				}

				// src側と名称が一致するdest側のメンバーを取得(見つからなければスキップ)
				Field destTargetField = destFieldMap.get(srcField.getName());
				if (destTargetField == null) {
					continue;
				}
				destTargetField.setAccessible(true);

				// srcの値がnullならスキップ
				// SOAPのunmarshalling(XML->bean)仕様に準拠
				if (getSrcMemberValue(srcField, srcBean) == null) {
					if (srcField.getAnnotation(RestBeanConvertNullCopy.class) != null
							&& !(destTargetField.getType().isPrimitive())) {
						//RestBeanConvertNullCopy指定時はNULLセット(Primitiveならできないので回避)
						setTargetMemberValue(destTargetField, destBean, null);
					}
					continue;
				}
				
				// アノテーションの指定があれば専用の変換処理を行う
				if (srcField.getAnnotation(RestBeanConvertDatetime.class) != null){
					// アノテーション指定による日時データの変換(Long -> String)
					convertDateStringToLong(srcField, srcBean, destTargetField, destBean);
					continue;
				}
				if (destTargetField.getAnnotation(RestBeanConvertDatetime.class) != null){
					// アノテーション指定による日時データの変換(String -> Long)
					convertDateLongToString(srcField, srcBean, destTargetField, destBean);
					continue;
				}
				if (srcField.getAnnotation(RestBeanConvertEnum.class) != null) {
					// アノテーション指定によるコード値データの変換(Enum -> String/Integer)
					convertEnumToCode(srcField, srcBean, destTargetField, destBean);
					continue;
				}
				if (destTargetField.getAnnotation(RestBeanConvertEnum.class) != null) {
					// アノテーション指定によるコード値データの変換(Enum <-> String/Integer)
					convertCodeToEnum(srcField, srcBean, destTargetField, destBean);
					continue;
				}

				// 特にアノテーションがなければ srcメンバのクラス型に基づいて変換
				convertMember(srcField,srcBean,destTargetField,destBean);
			}
			// DTO->INFO ID変換
			if (!dtoIdFieldMap.isEmpty()) {
				// dest(INFO)側メンバにIDがある(名前で特定)なら、src側の対応メンバをID化してdest側IDにセット
				Field destIdClassField = destFieldMap.get(idConvDtoToInfSet.idName());
				if (destIdClassField != null) {
					converDtoMemberToIdInstance(dtoIdFieldMap.values(), srcBean, destIdClassField, destBean);
				}
			}
			// INFO->DTO ID変換
			if (infoIdFieldInstance != null) {
				// src(INFO)側メンバにIDがある(名前で特定済み)なら、src側ID内メンバをdest側対応メンバに複写
				for (Field infoIdClassMember : infoIdFieldInstance.getClass().getDeclaredFields()) {
					Field copyTargetField = destFieldMap.get(infoIdClassMember.getName());
					if (copyTargetField == null) {
						continue;
					}
					infoIdClassMember.setAccessible(true);
					copyTargetField.setAccessible(true);
					if (copyTargetField.getAnnotation(RestBeanConvertEnum.class) != null) {
						convertCodeToEnum(infoIdClassMember, infoIdFieldInstance, copyTargetField, destBean);
					} else {
						setTargetMemberValue(copyTargetField, destBean, getSrcMemberValue(infoIdClassMember,infoIdFieldInstance));
					}
				}
			}

			if (m_log.isTraceEnabled()) {
				m_log.trace(
						String.format("convertBean() : %s to %s . exec time %s ms", srcBean.getClass().getSimpleName(),
								destBean.getClass().getSimpleName(), (System.currentTimeMillis() - start)));
			}

		} catch (HinemosUnknown | InvalidSetting e) {
			throw e;
		} catch (Exception e) {
			// 想定外エラー
			m_log.error(e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * src側メンバの値を取得する（可能ならGetter経由で取得する） <BR>
	 */
	@Override
	// 各フィールドは setAccessible(true) となっている前提。
	protected Object getSrcMemberValue(Field srcField, Object srcObject) throws IllegalAccessException {
		if (useMethodCacheFlg) {
			// useMethodCacheFlgがtrueの場合はメソッドのキャッシュを使用する。
			return getSrcMemberValueUsingCache(srcField, srcObject);
		}

		try {
			// 一部のInfoクラスにてGetterメソッドを経由しないと正常にメンバ値を取得できないタイミングがある事へ対応している
			// Getterの名称とメンバの名称は一般的な命名規則に従ってる前提。（アノテーション指定があればそちらを優先 ）
			Class<?> tagClass = srcObject.getClass();
			String getterName = "";
			RestBeanConvertAssignGetter assign = srcField.getAnnotation(RestBeanConvertAssignGetter.class);
			if (assign != null){
				getterName = assign.getterName();
			}else{
				String memberName = srcField.getName();
				getterName = "get" + memberName.substring(0, 1).toUpperCase() + memberName.substring(1);
			}
			Method method = tagClass.getMethod(getterName);
			return method.invoke(srcObject);
		} catch (RuntimeException e) { 
			// findbugs対応 RuntimeExceptionのcatchを明示化
			// Getterが生成できない場合は 値を直接参照
			return srcField.get(srcObject);
		} catch (Exception e) {
			// Getterが生成できない場合は 値を直接参照
			return srcField.get(srcObject);
		}

	}

	/**
	 * copyTarget側メンバの値を設定する（可能ならSetter経由で設定する） <<BR>
	 * @throws IllegalAccessException 
	 */
	protected void setTargetMemberValue(Field copyTargetField, Object copyTargetObject , Object destValue) throws IllegalAccessException  {
		if (useMethodCacheFlg) {
			// useMethodCacheFlgがtrueの場合はメソッドのキャッシュを使用する。
			setTargetMemberValueUsingCache(copyTargetField, copyTargetObject, destValue);
			return;
		}

		try {
			// 一部のInfoクラスにてSetterメソッドを経由しないと正常にメンバ値を設定できない問題へ対応している
			// Setterの名称とメンバの名称は一般的な命名規則に従ってる前提。（アノテーション指定があればそちらを優先 ）
			Class<?> valueClass = destValue.getClass();
			Class<?> tagClass = copyTargetObject.getClass();
			String memberName = copyTargetField.getName();
			String setterName = "";
			RestBeanConvertAssignSetter assign = copyTargetField.getAnnotation(RestBeanConvertAssignSetter.class);
			if (assign != null){
				setterName = assign.setterName();
			}else{
				setterName = "set" + memberName.substring(0, 1).toUpperCase() + memberName.substring(1);
			}
			Method method = tagClass.getMethod(setterName,valueClass);
			method.invoke(copyTargetObject,destValue);
		} catch (RuntimeException e) { // findbugs対応 RuntimeExceptionのcatchを明示化
			// Setterが生成できない場合は 値を直接メンバ変数に設定
			copyTargetField.set(copyTargetObject, destValue);
		} catch (Exception e) {
			// Setterが生成できない場合は 値を直接メンバ変数に設定
			copyTargetField.set(copyTargetObject, destValue);
		}
		return ;
	}

	/**
	 * src側メンバの値を取得する（可能ならGetter経由で取得する） <BR>
	 */
	// 各フィールドは setAccessible(true) となっている前提。
	private Object getSrcMemberValueUsingCache(Field srcField, Object srcObject) throws IllegalAccessException {
		try {
			// 一部のInfoクラスにてGetterメソッドを経由しないと正常にメンバ値を取得できないタイミングがある事へ対応している
			// Getterの名称とメンバの名称は一般的な命名規則に従ってる前提。（アノテーション指定があればそちらを優先 ）
			Class<?> tagClass = srcObject.getClass();
			String getterName = "";
			RestBeanConvertAssignGetter assign = srcField.getAnnotation(RestBeanConvertAssignGetter.class);
			if (assign != null){
				getterName = assign.getterName();
			}else{
				String memberName = srcField.getName();
				getterName = "get" + memberName.substring(0, 1).toUpperCase() + memberName.substring(1);
			}
			Method method = getGetter(tagClass, srcField, getterName);
			if (method != null) {
				return method.invoke(srcObject);
			} else {
				// Getterを参照できない場合は 値を直接参照
				return srcField.get(srcObject);
			}
		} catch (RuntimeException | InvocationTargetException e) { 
			// findbugs対応 RuntimeExceptionのcatchを明示化
			// Getterが生成できない場合は 値を直接参照
			return srcField.get(srcObject);
		}

	}

	/**
	 * copyTarget側メンバの値を設定する（可能ならSetter経由で設定する） <BR>
	 * @throws IllegalAccessException 
	 */
	private void setTargetMemberValueUsingCache(Field copyTargetField, Object copyTargetObject , Object destValue) throws IllegalAccessException  {
		try {
			// 一部のInfoクラスにてSetterメソッドを経由しないと正常にメンバ値を設定できない問題へ対応している
			// Setterの名称とメンバの名称は一般的な命名規則に従ってる前提。（アノテーション指定があればそちらを優先 ）
			Class<?> valueClass = destValue.getClass();
			Class<?> tagClass = copyTargetObject.getClass();
			String memberName = copyTargetField.getName();
			String setterName = "";
			RestBeanConvertAssignSetter assign = copyTargetField.getAnnotation(RestBeanConvertAssignSetter.class);
			if (assign != null){
				setterName = assign.setterName();
			}else{
				setterName = "set" + memberName.substring(0, 1).toUpperCase() + memberName.substring(1);
			}
			Method method = getSetter(tagClass, copyTargetField, setterName, valueClass);
			if (method != null) {
				method.invoke(copyTargetObject, destValue);
			} else {
				// Setterを参照できない場合は 値を直接メンバ変数に設定
				copyTargetField.set(copyTargetObject, destValue);
			}
		} catch (RuntimeException | InvocationTargetException e) {
			// findbugs対応 RuntimeExceptionのcatchを明示化
			// Setterが生成できない場合は 値を直接メンバ変数に設定
			copyTargetField.set(copyTargetObject, destValue);
		}
		return ;
	}

	// システム内時刻（HinemosTime）を表すLong から 日時を表す文字列 への変換を行う
	// 各フィールドは setAccessible(true) となっている前提。
	private void convertDateLongToString(Field srcField, Object srcObject, Field copyTargetField,
			Object copyTargetObject) throws InvalidSetting, HinemosUnknown {
		try {
			if (long.class.equals(srcField.getType()) || Long.class.equals(srcField.getType())) {
				// Long->日時文字列
				Long srcVal = (Long) getSrcMemberValue(srcField,srcObject);
				if (srcVal == null) {
					setTargetMemberValue(copyTargetField, copyTargetObject, null);
					return;
				}
				String destVal = RestCommonConverter.convertHinemosTimeToDTString(srcVal);
				setTargetMemberValue(copyTargetField, copyTargetObject, destVal);
			} else {
				String errMessage = String.format("failed Convert Date process. srcType=[%s], destType=[%s]",
						srcField.getType().getSimpleName(), copyTargetField.getType().getSimpleName());
				m_log.error("convertDateStringToLong(): " + errMessage);
				throw new HinemosUnknown(errMessage);
			}
		} catch (IllegalAccessException e) {
			// Field.setにてアクセスが不可能な場合や設定する値が不正な場合に入ってくる
			m_log.error("convertDateStringToLong(): exception =" + e.getClass().getSimpleName() + ", message="
					+ e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	// 日時を表す文字列 から システム内時刻（HinemosTime）を表すLongへの変換を行う
	// 各フィールドは setAccessible(true) となっている前提。
	// 入力値のチェックも兼ねているので注意。
	private void convertDateStringToLong(Field srcField, Object srcObject, Field copyTargetField,
			Object copyTargetObject) throws InvalidSetting, HinemosUnknown {
		try {
			if (String.class.equals(srcField.getType())) {
				// 日時文字列->Long
				String srcVal = (String) getSrcMemberValue(srcField,srcObject);
				if (srcVal == null) {
					setTargetMemberValue(copyTargetField, copyTargetObject, null);
					return;
				}
				Long destVal = RestCommonConverter.convertDTStringToHinemosTime(srcVal,
						RestCommonValitater.getItemName(srcField));
				setTargetMemberValue(copyTargetField, copyTargetObject, destVal);
			} else {
				String errMessage = String.format("failed Convert Date process. srcType=[%s], destType=[%s]",
						srcField.getType().getSimpleName(), copyTargetField.getType().getSimpleName());
				m_log.error("convertDateLongToString(): " + errMessage);
				throw new HinemosUnknown(errMessage);
			}
		} catch (IllegalAccessException e) {
			// Field.setにてアクセスが不可能な場合や設定する値が不正な場合に入ってくる
			m_log.error("convertDateLongToString(): exception =" + e.getClass().getSimpleName() + ", message="
					+ e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	// ENUM(コード値向け) から コード(int/String型) への変換を行う
	// 各フィールドは setAccessible(true) となっている前提
	private void convertEnumToCode(Field srcField, Object srcObject, Field copyTargetField,
			Object copyTargetObject) throws InvalidSetting, HinemosUnknown {
		try {
			Object srcValue = getSrcMemberValue(srcField,srcObject);
			if (srcValue == null) {
				setTargetMemberValue(copyTargetField, copyTargetObject, null);
				return;
			}
			if (Arrays.asList(srcField.getType().getInterfaces()).contains(EnumDto.class)) {
				// Enum->コード
				EnumDto<?> srcVal = (EnumDto<?>) srcValue;
				setTargetMemberValue(copyTargetField, copyTargetObject, srcVal.getCode());
			} else {
				String errMessage = String.format("failed Convert Enum process. srcType=[%s], destType=[%s]",
						srcField.getType().getSimpleName(), copyTargetField.getType().getSimpleName());
				m_log.error("convertEnumToCode(): " + errMessage);
				throw new HinemosUnknown(errMessage);
			}
		} catch (IllegalAccessException e) {
			// Field.setにてアクセスが不可能な場合や設定する値が不正な場合に入ってくる
			m_log.error("convertEnumToCode(): exception=" + e.getClass().getSimpleName() + ", message="
					+ e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage());
		}
	}

	// コード(int/String型) から ENUM(コード値向け) への変換を行う
	// 各フィールドは setAccessible(true) となっている前提
	private void convertCodeToEnum(Field srcField, Object srcObject, Field copyTargetField,
			Object copyTargetObject) throws InvalidSetting, HinemosUnknown {
		try {
			Object srcValue = getSrcMemberValue(srcField,srcObject);
			if (srcValue == null) {
				setTargetMemberValue(copyTargetField, copyTargetObject, null);
				return;
			}
			if (int.class.equals(srcField.getType()) || Integer.class.equals(srcField.getType())
					|| String.class.equals(srcField.getType())) {
				// コード->Enum
				Object srcVal = srcValue;
				EnumDto<?>[] des = (EnumDto<?>[]) copyTargetField.getType().getEnumConstants();
				for (EnumDto<?> destVal : des) {
					if (destVal.getCode().equals(srcVal)) {
						setTargetMemberValue(copyTargetField, copyTargetObject, destVal);
						break;
					}
				}
			} else {
				String errMessage = String.format("failed Convert Enum process. srcType=[%s], destType=[%s]",
						srcField.getType().getSimpleName(), copyTargetField.getType().getSimpleName());
				m_log.error("convertCodeToEnum(): " + errMessage);
				throw new HinemosUnknown(errMessage);
			}
		} catch (IllegalAccessException e) {
			// Field.setにてアクセスが不可能な場合や設定する値が不正な場合に入ってくる
			m_log.error("convertCodeToEnum(): exception=" + e.getClass().getSimpleName() + ", message="
					+ e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage());
		}
	}

	//srcDTO内のID対応メンバーを、IDクラスのインスタンスに変換して copyTarget側のメンバー設定します。
	private void converDtoMemberToIdInstance(Collection<Field> dtoIdFieldList, Object srcObject,
			Field destIdClassField, Object copyTargetObject) throws InvalidSetting, HinemosUnknown {
		try {
			Object destIdInstance = destIdClassField.getType().newInstance();
			for (Field dtoIdMember : dtoIdFieldList) {
				if (dtoIdMember == null) {
					continue;
				}
				// src項目内のId用メンバ と名前が同じID用クラスメンバを取得
				Field targetField = null;
				for (Field infoIdMember : destIdInstance.getClass().getDeclaredFields()) {
					if (dtoIdMember.getName().equals(infoIdMember.getName())) {
						targetField = infoIdMember;
					}
				}
				if (targetField == null) {
					continue;
				}
				// 値をセット
				dtoIdMember.setAccessible(true);
				targetField.setAccessible(true);
				if (dtoIdMember.getAnnotation(RestBeanConvertEnum.class) != null) {
					convertEnumToCode(dtoIdMember, srcObject, targetField, destIdInstance);
				} else {
					Object srcValue = getSrcMemberValue(dtoIdMember,srcObject);
					setTargetMemberValue(targetField, destIdInstance, srcValue);
				}
			}
			destIdClassField.setAccessible(true);
			setTargetMemberValue(destIdClassField, copyTargetObject, destIdInstance);
		} catch (IllegalAccessException e) {
			// Field.setにてアクセスが不可能な場合や設定する値が不正な場合に入ってくる
			m_log.error("converDtoMemberToIdInstance(): exception=" + e.getClass().getSimpleName() + ", message="
					+ e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (InstantiationException e) {
			// newInstanceに対象が不正な場合に入ってくる
			m_log.error("converDtoMemberToIdInstance(): exception=" + e.getClass().getSimpleName() + ", message="
					+ e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

	}
}
