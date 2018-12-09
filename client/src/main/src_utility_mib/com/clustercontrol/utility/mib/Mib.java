/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib;

import java.util.ArrayList;
import java.util.Collection;

import com.clustercontrol.utility.mib.MibSymbol;

/**
 * MIBのパース処理(Trap関連情報取得)におけるMIB自体を表します。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class Mib {
	
	/**
	 * MIB名称.
	 */
	private String mibName;
	
	/**
	 * MIBのheaderComment.
	 */
	private String mibHeaderComment = null;
	
	/**
	 * MIBのfooterComment.
	 */
	private String mibFooterComment = null;
	
	/**
	 * MIBが保持する各種象徴の一覧.
	 */
	private ArrayList<MibSymbol> mibSymbolAry = new ArrayList<MibSymbol>();
	
	/**
	 * MIBの定義に用いられているSMIのバージョン番号.
	 */
	private int smiVersion = 1;
	
	/**
	 * コンストラクタ
	 * 
	 * @param name 象徴の名称
	*/
	public Mib(String mibName) {
		this.mibName = mibName;
	}
	
	/**
	 * 名称を返します
	 * 
	 * @return 象徴の名称
	*/
	public String getName() {
		return mibName;
	}
	
	/**
	 * MIBファイルのヘッダコメントを返します
	 * 
	 * @return	ヘッダコメント
	*/
	public String getHeaderComment() {
		return mibHeaderComment;
	}
	
	/**
	 * MIBファイルのフッタコメントを返します
	 * 
	 * @return フッタコメント
	*/
	public String getFooterComment() {
		return mibFooterComment;
	}
	/**
	 * 保持する各種象徴の一覧を返します
	 * 
	 * @return 保持する各種象徴の一覧
	*/
	public	Collection<MibSymbol> getAllSymbols() {
		return mibSymbolAry;
	}

	/**
	 * 保持するSMIのバージョン番号を返します。
	 * 
	 * @return 保持する各種象徴の一覧
	*/
	public	int getSmiVersion() {
		return smiVersion;
	}
	
	/**
	 * MIBに象徴を追加します。
	 * 
	 * @param addMibSymbol MIBに追加する象徴
	*/
	public void addMibSymbol(MibSymbol addMibSymbol) {
		mibSymbolAry.add(addMibSymbol);
	}

	/**
	 * MIBのヘッダコメントを設定します。
	 * 
	 * @param mibHeaderComment ヘッダコメント
	*/
	public void setHeaderComment(String mibHeaderComment) {
		this.mibHeaderComment = mibHeaderComment;
	}
	
	/**
	 * MIBのフッタコメントを設定します。
	 * 
	 * @param mibFooterComment フッタコメント
	*/
	public void setFooterComment(String mibFooterComment) {
		this.mibFooterComment = mibFooterComment;
	}

	/**
	 * MIBのSMIのバージョン番号を設定します。
	 * 
	 * @param smiVersion バージョン番号
	*/
	public void setSmiVersion(int smiVersion) {
		this.smiVersion = smiVersion;
	}

}


