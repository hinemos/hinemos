/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference.anno;


/**
 * 属性名に名前空間を付加する。
 * プロパティ、クラスに付加できる。
 * クラスに指定して、"nameType" へ prop を指定した場合、
 * propName に格納された名称に一致するプロパティから、名前空間に利用する文字列を取得する。
 * プロパティに指定して、"nameType" へ prop を指定した場合、指定したプロパティが所属するクラスの
 * propName に格納された名称に一致するプロパティから、名前空間に利用する文字列を取得する。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class Namespace extends AnnoSubstitute {
	/**
	 * 名前空間として使用する文字列の引用先種別。
	 * 
	 */
	public enum NameType {
		/**
		 * 文字列。
		 */
		name,
		/**
		 * プロパティ。
		 */
		prop
	}
	
	/**
	 * 名前空間で利用する文字列の指定方法。
	 */
	public NameType nameType;
	
	/**
	 * "nameType" に "name" が指定されている場合に有効。
	 */
	public String propName;
}
