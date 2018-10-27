/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib.parse;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.utility.mib.MibLoaderException;
import com.clustercontrol.utility.mib.MibLoaderLog;
import com.clustercontrol.utility.mib.value.ObjectIdentifierValue;

/**
 * MIBのパース処理(Trap関連情報取得)における
 * 指定された名称に対応するOIDを解決するためのキャッシュクラスです。
 * 
 * OID値の定義にて、指定している親名称が参照している定義の以後に登場するケースや
 * 最終的に解決できないケースがあるため、即座に親名称を解決できない登録を
 * 一旦予約として保留し、適時参照して後から解決しています。
 * 
 * 解決できない場合は予約情報を用いて 参照関係も含めた エラーメッセージを構築します。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class OidNameResolveCache {
	
	private static Log log = LogFactory.getLog(OidNameResolveCache.class);

	/**
	 * キャッシュの対象となる名称を保持するMIBファイル
	 */
	private File mibFile ;
	
	/**
	 * 既定の名称 iso
	 */
	private static final String iso = "iso";

	/**
	 * 既定の名称 iso
	 */
	private static final BigInteger iso_value = new BigInteger("1");

	/**
	 * 処理ログ管理クラス
	 */
	private MibLoaderLog loaderLog ;

	/**
	 * 処理予約中のOID登録情報。親の値名称の解決ができていない登録を保管しておく。
	 */
	private ConcurrentHashMap<String,OidParseInfoEntry> oidParseInfoEntrys = new ConcurrentHashMap<String,OidParseInfoEntry>();
	
	/**
	 * IMPORTS取込OID値の名称MAP。IMPORTSにて取り込まれた名称と設置値のMAP
	 */
	private ConcurrentHashMap <String,ObjectIdentifierValue> importOidNameMap =	 new ConcurrentHashMap <String,ObjectIdentifierValue>();

	/**
	 * IMPORTS取込失敗値の名称MAP。IMPORTSにて取得できなかった値名称と対応する語句(MIB名)のMap
	 */
	private ConcurrentHashMap <String,MibToken> importFailNameMap =	 new ConcurrentHashMap <String,MibToken>();

	/**
	 * 登録完了OID値の名称MAP。親の値名称の解決ができた登録を保管しておく。
	 */
	private ConcurrentHashMap <String,ObjectIdentifierValue> registOidNameMap =	 new ConcurrentHashMap <String,ObjectIdentifierValue>();
	
	/**
	 * 処理予約の親名称関連MAP（ 親値の名称 , 予約登録なってる値の名称の一覧 ）
	 */
	private ConcurrentHashMap <String,ArrayList<String>> reservOidNameMap =	 new ConcurrentHashMap <String,ArrayList<String>> ();
	
	
	/**
	 * コンストラクタ
	 * 
	 * 
	 * @param loaderLog ローダーログ
	 * @param mibFile パース対象のMIBファイル
	 * @param importOidList MIBにインポートされたOIDのリスト
	 * @param importFailNameMap インポートに失敗した名称のリスト（参照対象のMIB名付き）
	*/
	public OidNameResolveCache( MibLoaderLog loaderLog, File mibFile , ArrayList<ObjectIdentifierValue> importOidList ,ConcurrentHashMap<String,MibToken> importFailNameMap ) {

		//対象MIBファイルを取得
		this.mibFile= mibFile;

		//ログを引継
		this.loaderLog= loaderLog;

		//isoを既定値として設定
		importOidNameMap.put(iso, new ObjectIdentifierValue(null,iso , iso_value ));
		
		//IMPORT失敗値を設定
		this.importFailNameMap = importFailNameMap;
		
		//importされたOIDのリストについて検索用Mapを編集
		Iterator<ObjectIdentifierValue> it= importOidList.iterator();
		while (it.hasNext()) {
			ObjectIdentifierValue entry =  it.next();
			importOidNameMap.put(entry.getName(), entry);
		}
	}
	

	/**
	 * 名称に対応したOID値表現の登録
	 * 
	 * @param valueName 値の名称
	 * @param parentName 親となる値(OID)の名称
	 * @param value ID値
	 * @param valueNameToken  値名の字句インスタンス
	 * @param parentNameToken 親名の字句インスタンス 
	 * @param chainIdList     連携ID表現のリスト
	 * @return 処理の成否
	*/
	public boolean registParseOid(BigInteger value , MibToken valueNameToken ,MibToken parentNameToken ,ArrayList<MibExpressionChainId> chainIdList  ) throws MibLoaderException{
		boolean resolveParent = true;
		 String valueName = valueNameToken.getTokenString();
		
		//値の名称がインポートと重複した場合、インポートを優先する（mibble動作に準拠、参照エラーにはしない）
		if( importOidNameMap.get(valueName) != null ){
			if(log.isDebugEnabled()){
				log.debug("registParseOid . This name overlaps the import .name=[" + valueName + "]");
			}
			//String message = "Reference error . This name overlaps the import .name=[" + valueName + "]" ;
			//loaderLog.addEntry( mibFile, valueNameToken.getTokenLine(), valueNameToken.getTokenLine(), message);
			//throw new MibLoaderException(loaderLog);
			return true;
		} 
		
		//値の名称がここまでの登録と重複なら、異常終了
		if( registOidNameMap.containsKey(valueName) || oidParseInfoEntrys.containsKey(valueName) ){
			String message = "Reference error . This name overlaps the other syntax . name=[" + valueName + "]" ;
			loaderLog.addEntry( mibFile, valueNameToken.getTokenLine(), valueNameToken.getTokenLine(), message);
			throw new MibLoaderException(loaderLog);
		}
		
		//親として引用する値名称がある場合
		ObjectIdentifierValue parentOid = null;
		if(parentNameToken !=null){
			//親名称の参照について名前解決（importされた名称、 ここまでの処理で登録できた名称、 両方を対象とする）
			String parentName = parentNameToken.getTokenString();
			parentOid = importOidNameMap.get(parentName);
			if( parentOid == null ){
				parentOid = registOidNameMap.get(parentName);
			}
			if( parentOid == null ){
				resolveParent = false;
			}
			
			//名前解決できない場合、処理予約して終了
			if( resolveParent==false){
				oidParseInfoEntrys.put(valueName, new OidParseInfoEntry(  valueName , parentName , value ,	valueNameToken , parentNameToken ,chainIdList)) ;
				if( reservOidNameMap.containsKey(parentName) ){
					 reservOidNameMap.get( parentName).add(valueName);
				}else{
					ArrayList<String> reservList = new ArrayList<String>();
					reservList.add(valueName);
					reservOidNameMap.put( parentName, reservList);
				}
				return true;
			}
		}
		
		//親名称引用が問題なければ登録
		updateCache(parentOid,valueName,value,chainIdList );

		return true;
	}

	/**
	 * 名称に対応したOID値表現の登録
	 * 
	 * @param value ID値
	 * @param valueNameToken  値名の字句インスタンス
	 * @param parentNameToken 親名の字句インスタンス 
	 * @return 処理の成否
	*/
	public boolean registParseOid( BigInteger value , MibToken valueNameToken ,MibToken parentNameToken ) throws MibLoaderException{
		return registParseOid( value , valueNameToken , parentNameToken ,null); 
	}

	/**
	 * OIDキャッシュ更新
	 * 登録された構文表現を元にOIDを生成し、名称に割付
	 * 
	 * @param parentOid 親となる値(OID)
	 * @param valueName 値の名称
	 * @param value ID値
	 * @param chainIdList 連携ID表現のリスト
	 * @return 処理の成否
	*/
	public void updateCache( ObjectIdentifierValue parentOid, String valueName , BigInteger value , ArrayList<MibExpressionChainId> chainIdList ) throws MibLoaderException{
		//連結IDの指定があれば連結分のインスタンスを作成
		ObjectIdentifierValue setLastParent =parentOid;
		if(chainIdList != null){
			for ( MibExpressionChainId target: chainIdList){
				String setValueName ;
				BigInteger setValue ;
				//指定された名称とIDを元にインスタンスを作成
				try{
					if( target.getIdNumber() != null ){
						setValueName = target.getToken().getTokenString();
						setValue = new BigInteger(target.getIdNumber().getToken().getTokenString());
					}else{
						//()による名称(値)指定でなく 数値のみの場合 名称なしとする（mibble準拠）
						setValueName = "";
							setValue = new BigInteger(target.getToken().getTokenString());
					}
				}catch(NumberFormatException e ){
					String message = "Syntax error. Set value of object identifer syntax ( { any(number) } or { number } ) is incorrect .";
					loaderLog.addEntry( mibFile, target.getToken().getTokenLine(), target.getToken().getTokenCols(), message);
					throw new MibLoaderException(loaderLog);
				}
				ObjectIdentifierValue setOid = new ObjectIdentifierValue (setLastParent,setValueName,setValue );
				setLastParent = setOid;
			}
		}
		ObjectIdentifierValue registOid; 
		if( value != null){
			//ID値の設定があれば 連結IDにつなげてOIDを作成し、名称に対する設定値とする。（値名称を末尾OIDの名称に流用）
			registOid = new ObjectIdentifierValue (setLastParent,valueName,value );
		}else{
			//ID値の設定がなければ 連結IDによるOIDを、名称に対する設定値とする。
			registOid = setLastParent;
		}
		
		//値名称に対応するOIDインスタンスを登録
		registOidNameMap.put(valueName, registOid);

		//親OID名にて処理予約されたエントリがあれば合わせて処理
		registReserv(valueName, registOid);
		
	}
	
	/**
	 * 親OIDの未解決にて処理予約していたエントリの処理
	 * 
	*/
	private void registReserv(String parentName, ObjectIdentifierValue parentOid ) throws MibLoaderException{
		
		//指定された名称の予約があれば登録を実施
		if( reservOidNameMap.containsKey(parentName) ){
			Iterator<String> it = reservOidNameMap.get( parentName).iterator();
			while(it.hasNext()){
				String registName = it.next();
				OidParseInfoEntry entry = oidParseInfoEntrys.get(registName);
				
				//問題なければ登録MAPへ設定
				updateCache(parentOid, entry.getValueName(),entry.getValue(),entry.getChainIdList() );
				//予約情報を削除
				oidParseInfoEntrys.remove(registName);

			}
			
		}
	}

	/**
	 * 親OIDの未解決にて処理予約していたエントリの取得
	 * 
	*/
	private OidParseInfoEntry getOrgReserv(String valueName)  throws MibLoaderException{
		//予約がなければ 無しで返す。
		OidParseInfoEntry entry = oidParseInfoEntrys.get(valueName);
		if( entry == null){
			return null;
		} 
				
		//指定された名称の予約を親子関係も考慮して遡り、原点となる未解決の予約を取得（再帰処理）
		if(( entry.getParentName().equals(valueName) ==false ) && oidParseInfoEntrys.containsKey(entry.getParentName()) ){
			StringBuilder retBuf =new StringBuilder();
			retBuf.append("Infomation . The value is reference other name in this MIB.");
			retBuf.append("reference name=[");
			retBuf.append(entry.getParentName());
			retBuf.append("]. value name=[");
			retBuf.append(entry.getValueName());
			retBuf.append("].");
			String message = retBuf.toString();
			loaderLog.addEntry( mibFile, entry.getparentNameToken().getTokenLine(), entry.getparentNameToken().getTokenCols(), message);
			return getOrgReserv(entry.getParentName());
		}else{
			return entry;
		}
	}
	/**
	 * IMPORTに失敗してる名称かチェック
	 * 
	*/
	private void importFailNameCheck(String targetName){
		
		//失敗名ならその旨をログに設定
		if( this.importFailNameMap != null && this.importFailNameMap.containsKey(targetName) ){
			MibToken mibToken =importFailNameMap.get(targetName);
			String message = "Reference error . The value name  cannot be import .  Value name is " + targetName + " . Reference MIB is " + mibToken .getTokenString();
			loaderLog.addEntry( mibFile, mibToken.getTokenLine(), mibToken.getTokenCols(), message);
		}
	}

	
	/**
	 * 解決済みキャッシュの名称一覧を取得
	 *	
	 * @return 名称一覧 
	 * 
	*/
	public ArrayList<String>  getResovleOidNameList( ) throws MibLoaderException{
		return Collections.list(registOidNameMap.keys()) ;
	}

	/**
	 * 解決済みキャッシュから名称に対応したOID値を取得
	 *	
	 * @return OID値オブジェクト 
	 *	取得できなかった場合は エラーログを登録して例外を投げる
	 * 
	*/
	public ObjectIdentifierValue  getResovleOid( String valueName ) throws MibLoaderException{
		//登録成功一覧を確認
		if( registOidNameMap.containsKey(valueName) ){
			return registOidNameMap.get(valueName);
		//インポート一覧を確認
		}else if( importOidNameMap.containsKey(valueName) ){
			return importOidNameMap.get(valueName);
		}else{
			//取得にできなった場合
			OidParseInfoEntry entry = getOrgReserv(valueName);
			StringBuilder retBuf =new StringBuilder();
			if(entry != null){
				//親の解決予約があれば予約を遡って取得してエラーを出力
				retBuf.append("Reference error . The name of parent cannot be referenced in this MIB.");
				retBuf.append(" parent name=[");
				retBuf.append(entry.getParentName());
				retBuf.append("]. value name=[");
				retBuf.append(entry.getValueName());
				retBuf.append("].");
				String message = retBuf.toString();
				loaderLog.addEntry( mibFile, entry.getparentNameToken().getTokenLine(), entry.getparentNameToken().getTokenCols(), message);
				//解決に失敗している名称についてimportができてない場合はその旨を追記
				importFailNameCheck(entry.getParentName());
				throw new MibLoaderException(loaderLog);
			}else{
				//特に予約もなければ 存在しない旨を返す
				retBuf.append("Reference error . The name cannot be referenced in this MIB.");
				retBuf.append(" value name=[");
				retBuf.append(valueName);
				retBuf.append("].");
				String message = retBuf.toString();
				loaderLog.addEntry( mibFile, 0, 0, message);
				//解決に失敗している名称についてimportができてない場合はその旨を追記
				importFailNameCheck(valueName);
				throw new MibLoaderException(loaderLog);
			}
		}
	}
	
	/**
	 * OIDの構文解析情報エントリー
	 * 
	*/
	private static class OidParseInfoEntry {
		private String valueName ;
		private String parentName ;
		private BigInteger value ;
		private MibToken parentNameToken ;
		private ArrayList<MibExpressionChainId> chainIdList ;

		/**
		 *	OIDの構文解析情報 インスタンス作成
		 * 
		 * @param valueName パースしたOIDの名称
		 * @param parentName 親となるOIDの名称
		 * @param value OID値
		 * @param valueNameToken  値名の字句インスタンス
		 * @param parentNameToken 親名の字句インスタンス
		 * @param chainIdList 連結IDの表現 
		*/
		public OidParseInfoEntry( String valueName ,String parentName ,	 BigInteger value , MibToken valueNameToken ,MibToken parentNameToken,ArrayList<MibExpressionChainId> chainIdList){
			this.valueName = valueName;
			this.parentName = parentName;
			this.value = value;
			this.parentNameToken = parentNameToken;
			this.chainIdList = chainIdList;
		}
		
		/**
		 * 親OIDの名称を返します
		 * 
		 * @return 親OIDの名称
		*/
		public String getParentName(){
			return parentName;
		}
		/**
		 * 値の名称を返します
		 * 
		 * @return 値の名称
		*/
		public String getValueName(){
			return valueName;
		}

		/**
		 * OIDの値を返します
		 * 
		 * @return OIDの値
		*/
		public BigInteger getValue(){
			return value;
		}
		
		/**
		 * 親名称の字句インスタンスを返します。
		 * 
		 * @return 字句インスタンス
		*/
		public MibToken getparentNameToken(){
			return this.parentNameToken;
		}

		/**
		 * 連結IDの表現を返します
		 * 
		 * @return 連結IDの表現
		*/
		public ArrayList<MibExpressionChainId> getChainIdList(){
			return this.chainIdList;
		}
	}

}
