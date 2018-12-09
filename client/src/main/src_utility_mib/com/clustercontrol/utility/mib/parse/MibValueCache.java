/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib.parse;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.clustercontrol.utility.mib.MibValue;

/**
 * MIBのパース処理(Trap関連情報取得)における
 * 各MIB内に保持された値のキャッシュ管理クラスです。
 * 
 * MIB内でのIMPORT構文による他MIBの値の参照を解決するために利用します。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class MibValueCache {

	/**
	 * 名称（MIBの名称,値の名称）をキーとしたMIB値キャッシュ
	 */
	private ConcurrentHashMap <String,ConcurrentHashMap <String,MibValue>> mibValueMap =	 new ConcurrentHashMap <String,ConcurrentHashMap <String,MibValue>>();
	
	
	/**
	 * コンストラクタ
	 * 
	*/
	public MibValueCache() {
		//初期化処理特になし
	}

	/**
	 * MIB内の値MAPをキャッシュから一括取得
	 * 
	 * @param mibName MIBの名称
	 * @return 指定MIB内のMIB値MAP
	*/
	public ConcurrentHashMap <String,MibValue> getMibValueMap( String mibName){
		
		//キャッシュからMAPを取得して返す
		return mibValueMap.get(mibName);
	}

	/**
	 * MIB値をキャッシュから取得
	 * 
	 * @param mibName MIBの名称
	 * @param valueName 値の名称
	 * @param tagMib パース済みMIBオブジェクト
	 * @return 指定MIB内のMIB値
	*/
	public MibValue getMibValue( String mibName,String valueName){
		
		//キャッシュからMAPを取得
		 ConcurrentHashMap <String,MibValue> valueMap = mibValueMap.get(mibName) ;
		 if( valueMap == null){
			 return null;
		 }
		//MAPからMIB値を取得
		 MibValue mibValue = valueMap.get(valueName) ;
		 if( mibValue == null){
			 return null;
		 }
		 return mibValue;
	}

	/**
	 * MIB内の値のキャッシュへの登録
	 * 
	 * @param tagMib パース済みMIBオブジェクト
	*/
	public void setMibValues( String mibName ,Collection<MibValue> values ){
		
		//MIBからシンボルの一覧を取得
		Iterator<MibValue> itr_values = values.iterator();
		
		//値の定義をキャッシュへセット
		ConcurrentHashMap <String,MibValue> setMap = new ConcurrentHashMap <String,MibValue>();
		while(itr_values.hasNext()) {
			MibValue set =itr_values.next();
			if( set != null ) {
				setMap.put(set.getName(),set);
			}
		}
		mibValueMap.put(mibName,setMap);
	}
}
