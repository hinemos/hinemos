/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.annotation.validation.RestValidateDouble;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateLong;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.util.MessageConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Restの入力チェックで使用する共通メソッド
 */
public class RestCommonValitater {
	
	private static Log m_log = LogFactory.getLog( RestCommonValitater.class );

	/**
	 * Request向けのDTOに対して、専用アノテーションに基づいて入力チェックを行う。<BR>
	 * 
	 * @param targetDto 対象DTO
	 * @throws InvalidSetting
	 */
	public static void checkRequestDto( Object targetDto ) throws InvalidSetting {
		
		if( targetDto == null ){
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(MessageConstant.PARAM.getMessage()));
		}

		// 現実装では一回のチェックでの複数項目エラー検出には対応してない。
		// 必要なら checkValue系メソッドで、発生したInvalidSettingをスレッドローカルメンバに保存するように変更し
		// ここでリターン前に保存したInvalidSettingのメッセージを まとめてException化すれば可能な想定

		//通常のクラスなら自身を、集合系のクラスなら保持レコードを、チェック対象として取得
		List<Object> checkList = new ArrayList<Object>();
		if(targetDto.getClass().isArray() ){
			for (Object tagAryRec:  (Object[])targetDto){
				checkList.add(tagAryRec);
			}
		}else if(targetDto instanceof List ){
			for (Object tagListRec: (List<?>) targetDto){
				checkList.add(tagListRec);
			}
		}else if(targetDto instanceof Map ){
			for (Object tagValueRec: ((Map<?,?>) targetDto).values()){
				checkList.add(tagValueRec);
			}
		}else{
			checkList.add(targetDto);
		}

		//チェック実施
		for ( Object checkTarget : checkList ){
			checkRecursiveDTO(checkTarget);
		}
		return;
	}
	
	private static void checkRecursiveDTO( Object target ) throws InvalidSetting {

		//対象オブジェクト内の各メンバーに対し、オブジェクトの型別のチェックを実施
		List<Field> memberFields = getClassFields(target.getClass());
		for ( Field targetField : memberFields ){
			// JsonIgnoreアノテーションが付与されたフィールドはチェックしない。
			if (targetField.getAnnotation(JsonIgnore.class) != null) {
				continue;
			}
			//private なFieldでもアクセスできるようにする。
			targetField.setAccessible(true);
			Object targetInstance = null;
			try{
				targetInstance = (Object)targetField.get(target);
			}catch(Exception e){
				if( m_log.isTraceEnabled() ){
					m_log.trace( "checkRecursiveDTO() : targetField access failed .  Exception ="+e.getMessage() );
				}
			}
			if(targetField.getType().isArray() ){
				checkArray(targetField,(Object[])targetInstance);
			}else if(List.class.isAssignableFrom(targetField.getType()) ){
				checkList(targetField,(List<?>)targetInstance);
			}else if(Map.class.isAssignableFrom(targetField.getType()) ){
				checkMap(targetField,(Map<?,?>)targetInstance);
			}else{
				checkNormal(targetField,targetInstance);
			}
		}
	}
	
	private static void checkNormal(Field field, Object value ) throws InvalidSetting {
		if( m_log.isTraceEnabled() ){
			m_log.trace( "valitateUnit(): Field name="+field.getName()  );
		}

		//単体型向けチェック
		Class<?> type = field.getType();
		checkValueNonCollection(field, type , value);
		if( value == null ){
			return;
		}

		//インスタンス内部のメンバーに再帰的なチェックが必要な場合、実施
		if( isRecursiveTarget(type)){
			checkRecursiveDTO(value);
		}
	}

	private static void checkArray(Field field, Object[] target ) throws InvalidSetting {
		if( m_log.isTraceEnabled() ){
			m_log.trace( "checkArray(): Field name="+field.getName() );
		}
		//集合型向けチェック
		checkValueCollectionType( field, target );
		if( target == null ){
			return;
		}

		//配列のレコード毎のチェック
		//レコードがプリミティブラッパーの場合は検査対象にならないが想定通り(アノテーション設計的に対応していない)
		Class<?> type = field.getType().getComponentType();
		for ( Object tagAryRec: target ){
			if( isRecursiveTarget(type)){
				checkRecursiveDTO(tagAryRec);
			}
		}
	}
	

	private static void checkList(Field field, List<?> target ) throws InvalidSetting {
		if( m_log.isTraceEnabled() ){
			m_log.trace( "checkList(): Field name="+field.getName()  );
		}
		//集合型向けチェック
		checkValueCollectionType( field, target );
		if( target == null ){
			return;
		}

		//Listのレコード毎のチェック
		//レコードがプリミティブラッパーの場合は検査対象にならないが想定通り(アノテーション設計的に対応していない)
		for (Object tagAryRec:  target){
			Class<?> type = tagAryRec.getClass();
			if( isRecursiveTarget( type ) ){
				checkRecursiveDTO( tagAryRec );
			}
		}
	}

	private static void checkMap(Field field, Map<?,?> target ) throws InvalidSetting {
		if( m_log.isTraceEnabled() ){
			m_log.trace( "checkMap(): Field name="+field.getName() );
		}
		//集合型向けチェック
		checkValueCollectionType( field, target );
		if( target == null ){
			return;
		}

		//Mapのレコード毎のチェック（MapのValue値のみ対象）
		//レコードがプリミティブラッパーの場合は検査対象にならないが想定通り(アノテーション設計的に対応していない)
		for ( Object tagValueRec: target.values() ){
			Class<?> type = tagValueRec.getClass();
			if( isRecursiveTarget(type)){
				checkRecursiveDTO(tagValueRec);
			}
		}
	}

	
	// メンバー[各集合型]の値チェック（NotNull レコード数の範囲）
	private static void checkValueCollectionType(Field field, Object target) throws InvalidSetting {

		if( m_log.isTraceEnabled() ){
			m_log.trace( "checkValueCollectionType(): type=" + field.getType() );
		}

		//項目名の取得
		String itemName = getItemName(field);
		
		//CollectionType向けチェック
		RestValidateCollection valColDef = field.getAnnotation(RestValidateCollection.class);
		if( valColDef != null ){
			CommonValidator.validateCollectionType(itemName,target,valColDef.notNull(), valColDef.minSize(),valColDef.maxSize());
		}
	}
	
	// メンバー[非集合型]の値チェック（NotNull 数値/文字数の範囲 文字内容）
	private static void checkValueNonCollection( Field field,Class<?> targetType, Object targetObject)  throws InvalidSetting {
		
		if( m_log.isTraceEnabled() ){
			m_log.trace( "checkValueNonCollection(): targetType=" + targetType );
		}

		//項目名の取得
		String itemName = getItemName(field);

		//型に応じたチェックを実施
		if(targetType == String.class){
			RestValidateString valStrDef = field.getAnnotation(RestValidateString.class);
			if( valStrDef != null ){
				//文字共通チェック
				if( valStrDef.notNull() ){
					CommonValidator.validateNull(itemName, targetObject);
				}
				if( targetObject != null ){
					CommonValidator.validateStringLengthSkippable(itemName, (String)targetObject, valStrDef.minLen(), valStrDef.maxLen());
				}
				//タイプ別チェック
				if( targetObject != null && !((String)targetObject).isEmpty() && !(valStrDef.type().equals(CheckType.NULL)) ){
					if(valStrDef.type().equals(CheckType.ID)){
						CommonValidator.validateIdWithoutCheckNullAndEmpty(itemName,(String)targetObject,  Integer.MAX_VALUE );
					}else if(valStrDef.type().equals(CheckType.COLLECT)){
						CommonValidator.validateCollect(itemName,(String)targetObject, true,  Integer.MAX_VALUE);
					}
				}
			}
			return;
		}
		
		if( targetType == Long.class ){
			RestValidateLong valLngDef = field.getAnnotation(RestValidateLong.class);
			if( valLngDef != null ){
				if( valLngDef.notNull() ){
					CommonValidator.validateNull(itemName, targetObject);
				}
				if( targetObject != null ){
					CommonValidator.validateLongSkippable(itemName, (Long)targetObject, valLngDef.minVal(), valLngDef.maxVal());
				}
			}
			return;
		}
		if( targetType == Integer.class ){
			RestValidateInteger valIntDef = field.getAnnotation(RestValidateInteger.class);
			if( valIntDef != null ){
				if( valIntDef.notNull() ){
					CommonValidator.validateNull(itemName, targetObject);
				}
				if( targetObject != null ){
					CommonValidator.validateIntegerSkippable(itemName, (Integer)targetObject, valIntDef.minVal(), valIntDef.maxVal());
				}
			}
			return;
		}
		if( targetType == Double.class ){
			RestValidateDouble valDblDef = field.getAnnotation(RestValidateDouble.class);
			if( valDblDef != null ){
				if( valDblDef.notNull() ){
					CommonValidator.validateNull(itemName, targetObject);
				}
				if( targetObject != null ){
					CommonValidator.validateDoubleSkippable(itemName, (Double)targetObject, valDblDef.minVal(), valDblDef.maxVal());
				}
			}
			return;
		}
		RestValidateObject valObjDef = field.getAnnotation(RestValidateObject.class);
		if( valObjDef != null ){
			if( valObjDef.notNull() ){
				CommonValidator.validateNull(itemName, targetObject);
			}
		}
		return;
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

	/**
	 * DTOクラス内メンバの項目名を取得する<BR>
	 * @param field 対象メンバのField情報
	 * @return 項目名
	 * <BR>
	 */
	// 項目名の取得
	public static String getItemName(Field field){
		// 項目名アノテーションを取得（もしなければ変数名で代替）
		RestItemName nameDef = field.getAnnotation(RestItemName.class);
		String itemName = null;
		if(nameDef != null ){
			itemName = nameDef.value().getMessage();
		}else{
			itemName = field.getName();
		}
		return itemName;
	}

	/**
	 * クラス内のメンバ一覧を取得（スーパークラスのメンバ含む）<BR>
	 */
	private static List<Field> getClassFields(Class<?> targetClass) {
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
}
