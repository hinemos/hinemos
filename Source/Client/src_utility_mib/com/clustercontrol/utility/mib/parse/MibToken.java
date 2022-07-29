/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib.parse;

/**
 * MIBのパース処理(Trap関連情報取得)における
 * 定義文から字句解析により切り出された字句を表すクラスです。
 * 
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class MibToken {
	
	/**
	 * 字句
	 */
	private String tokenString ;

	/**
	 * 字句のMIBファイル内における行
	 */
	private int tokenLine;

	/**
	 * 字句のMIBファイル内における列(先頭から文字数）
	 */
	private int tokenCols;

	/**
	 * 字句種別( MibTokenUtil.Kind にて定義 )
	 */
	private MibTokenUtil.Kind tokenKind;

	/**
	 * コンストラクタ
	 * 
	 * @param tokenString 字句の文字列
	 * @param tokenLine 字句の出現開始行
	 * @param tokenCols 字句の出現開始列
	 * @param tokenKind 字句の種別
	*/
	public MibToken( String tokenString , int tokenLine ,int tokenCols , MibTokenUtil.Kind tokenKind) {
		this.tokenString = tokenString;
		this.tokenLine = tokenLine;
		this.tokenCols = tokenCols;
		this.tokenKind = tokenKind;
	}

	/**
	 * 字句の文字列取得
	 * 
	 * @return 文字列
	*/
	public String getTokenString(){
		return this.tokenString;
	}

	/**
	 * 字句の出現開始行取得
	 * 
	 * @return 行数
	*/
	public int getTokenLine(){
		return this.tokenLine;
	}
	/**
	 * 字句の出現開始列取得
	 * 
	 * @return 列数
	*/
	public int getTokenCols(){
		return this.tokenCols;
	}
	/**
	 * 字句の文字種別取得
	 * 
	 * @return 種別
	*/
	public  MibTokenUtil.Kind getTokenKind(){
		return this.tokenKind;
	}

	/**
	 * 設定値の一覧表示
	 * 
	 * @return 文字列
	*/
	public String getFormatString(){
		StringBuilder formatString = new StringBuilder(); 
		formatString.append("String:["+tokenString );
		formatString.append("]," );
		formatString.append("Kind:["+tokenKind );
		formatString.append("]," );
		formatString.append("Line:["+tokenLine );
		formatString.append("]," );
		formatString.append("Cols:["+tokenCols );
		formatString.append("]" );
		return formatString.toString();
	}

}
