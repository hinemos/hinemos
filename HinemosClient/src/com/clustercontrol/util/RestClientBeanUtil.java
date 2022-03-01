/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.util;

import com.clustercontrol.rest.util.RestBeanUtilBase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;

public class RestClientBeanUtil extends RestBeanUtilBase {

	public static void convertBean(Object srcBean, Object destBean) throws HinemosUnknown {
		//将来的に一部の処理をオーバーライドで変更できるように、本クラスを経由して共通処理を呼び出しとする
		try{
			new RestClientBeanUtil().convertBeanRecursive(srcBean, destBean);
		} catch (InvalidSetting e) {
			// convertBeanRecursiveでは InvalidSettingは発生しない想定（RestBeanUtilとの互換性のためにthrowsがある）
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}
	/**
	 * src側メンバの値をクラス型に合わせて変換した後、copyTargetへセットする。 <BR>
	 */
	@Override
	protected void convertMember(Field srcField, Object srcObject, Field copyTargetField, Object copyTargetObject) throws Exception{
		Class<?> srcType = srcField.getType();
		Class<?> tagType = copyTargetField.getType();
		Object srcValObj = srcField.get(srcObject);

		//Response向けEmunの Request向け変換対応 
		if (srcType.isEnum() && tagType.isEnum()
				&& srcValObj != null && srcType.getSimpleName().equals(tagType.getSimpleName())) {
			try{
				//srcのEnumから値を取得
				Method srcMethod = srcType.getMethod("getValue");
				String srcVaule = (String) srcMethod.invoke(srcValObj);
				
				//tagのEnumの値に変換
				Method tagMethod = tagType.getDeclaredMethod("fromValue",String.class);
				Object tagVaule = tagMethod.invoke(null,srcVaule);

				//型が一致してれば設定
				if (tagType.isAssignableFrom(tagVaule.getClass())) {
					setTargetMemberValue(copyTargetField,copyTargetObject,tagVaule);
				}
			}catch( RuntimeException e ){
				//findebugs対応 RuntimeException のcatchを明示化
			}catch( Exception e ){
				//変換エラー(getDeclaredMethod や invokeによるExcepiton )の場合、型がミスマッチとみなして変換しない
			} 
			return;
		}

		//既設処理
		super.convertMember(srcField, srcObject, copyTargetField, copyTargetObject);
	}
}
