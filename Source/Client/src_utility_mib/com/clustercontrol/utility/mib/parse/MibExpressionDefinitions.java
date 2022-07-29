/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib.parse;

import java.util.ArrayList;

/**
 * MIBのパース処理(Trap関連情報取得)における
 * 構文によるMIB定義表現クラスです。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */

public class MibExpressionDefinitions extends MibExpressionAbstract {
	
	private MibExpressionImport expImport;
	private MibExpressionString expMibName;

	private ArrayList<MibExpressionTrapType> expTrapTypeList = new ArrayList<MibExpressionTrapType>();
	private ArrayList<MibExpressionNotificationType> expNotificatonList = new ArrayList<MibExpressionNotificationType>();
	private ArrayList<MibExpressionOidAlocation> expOidAlocationList = new ArrayList<MibExpressionOidAlocation>();
	
	/**
	 * コンストラクタ
	 * @param tokenValue 字句インスタンス
	 */
	public MibExpressionDefinitions ( MibToken tokenValue ) {
		super.setToken(tokenValue); 
		
	}

	/**
	 * MIB名称表現の取得
	 */
	public MibExpressionString getMibName( ){
		return this.expMibName;
	}
	
	/**
	 * インポート表現の取得
	 */
	public MibExpressionImport getImport( ){
		return this.expImport;
	}
	
	/**
	 * TRAP-TYPE表現一覧の取得
	 */
	public ArrayList<MibExpressionTrapType> getTrapTypeList( ){
		return this.expTrapTypeList;
	}

	/**
	 * NOTIFICATION-TYPE表現一覧の取得
	 */
	public ArrayList<MibExpressionNotificationType> getNotificationTypeList( ){
		return this.expNotificatonList;
	}
	
	/**
	 * 各種OID割付表現一覧の取得
	 */
	public ArrayList<MibExpressionOidAlocation> getOidAlocationList( ){
		return this.expOidAlocationList;
	}
	/**
	 * MIB名称の紐付け設定
	 * @param expMibName MIB名
	 */
	public void setMibName(MibExpressionString expMibName ){
		//重複した設定は無視
		if( this.expMibName != null ) {
			return ;
		} 
		this.expMibName = expMibName;
		super.addChild(expMibName);
	}
	
	/**
	 * インポート表現の紐付け設定
	 * @param expImport IMPORT表現
	 */
	public void setImport(MibExpressionImport expImport ){
		//重複した設定は無視
		if( this.expImport != null ) {
			return ;
		} 
		this.expImport = expImport;
		super.addChild(expImport);
	}
	
	/**
	 * 各種表現の紐付け設定
	 * @param expTarget 紐付けたい表現
	 */
	@Override
	public void addChild(MibExpressionAbstract expTarget ){
		super.addChild(expTarget);
		if( expTarget instanceof MibExpressionTrapType ){
			this.expTrapTypeList.add((MibExpressionTrapType)expTarget);
		}
		if( expTarget instanceof MibExpressionNotificationType ){
			this.expNotificatonList.add((MibExpressionNotificationType)expTarget);
		}
		if( expTarget instanceof MibExpressionOidAlocation ){
			this.expOidAlocationList.add((MibExpressionOidAlocation)expTarget);
		}
	}
	
}