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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.utility.mib.MibLoaderException;
import com.clustercontrol.utility.mib.MibLoaderLog;
import com.clustercontrol.utility.mib.MibSymbol;
import com.clustercontrol.utility.mib.MibType;
import com.clustercontrol.utility.mib.MibValue;
import com.clustercontrol.utility.mib.MibValueSymbol;
import com.clustercontrol.utility.mib.snmp.SnmpNotificationType;
import com.clustercontrol.utility.mib.snmp.SnmpTrapType;
import com.clustercontrol.utility.mib.value.NumberValue;
import com.clustercontrol.utility.mib.value.ObjectIdentifierValue;

/**
 * MIBのパース処理(Trap関連情報取得)における
 * 構文表現インスタンスの象徴インスタンスへの変換クラスです。
 * 
 * 指定された解析キャッシュを元に DEFINITION表現クラスをSymbolクラスの一覧に変換します。
 * 
 * 上記過程で IMPORT表現対応の他MIB定義取込、親OID名前解決、解析結果キャッシュの更新も実施します。
 * などを実施します。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class MibSymbolConverter {

	private static Log log = LogFactory.getLog(MibSymbolConverter.class);

	/**
	 * 予約で読込するシンボル名と引用元MIB
	 */
	public static final String[][] defactMibSymbol = new String[][]{
		{"snmp","RFC1158-MIB"}
	,	{"private","RFC1155-SMI"}
	,	{"enterprises","RFC1155-SMI"}
	};

	/**
	 * MIBファイル
	 */
	private File mibFile;

	/**
	 *ログ管理インスタンス
	 */
	private MibLoaderLog mibLoaderLog;
	
	/**
	 *変換対象となるDEFINITION表現
	 */
	private MibExpressionDefinitions target;

	/**
	 *これまでに解析したMIBのMIB値キャッシュ
	 */
	private MibValueCache mibValueCache;

	/**
	 * 変換処理におけるOID名解決用キャッシュ
	 */
	private OidNameResolveCache oidNameResolveCache ;


	/**
	 * 変換結果となる象徴一覧
	 */
	private ArrayList<MibSymbol> mibSymbolList  ;

	/**
	 * コンストラクタ
	 * 
	 * @param mibLoaderLog ログ管理
	 * @param mibValueCache 解析済みMIBキャッシュ
	 * @param mibFile 対象ファイル
	 * @param targetExpression 変換対象となる構文表現
	 */
	public MibSymbolConverter(MibLoaderLog mibLoaderLog, MibValueCache mibValueCache ,File mibFile, MibExpressionDefinitions targetExpression) {
		this.mibLoaderLog = mibLoaderLog;
		this.mibValueCache = mibValueCache;
		this.mibFile = mibFile;
		this.target = targetExpression;
	}
	
	
	/**
	 * MIB象徴の一覧を取得（Trap関連のみ）
	 * 
	 */
	public ArrayList<MibSymbol> getSymbols() throws MibLoaderException{
		//未実施なら変換を行う
		if( this.mibSymbolList == null ){
			expressionsToSymbols();
		}
		return this.mibSymbolList ;
	}
	
	/**
	 * 表現を象徴へ変換
	 * 
	 */
	private void expressionsToSymbols() throws MibLoaderException{

		mibSymbolList = new  ArrayList<MibSymbol>();

		//インポートの解決
		resolveImports();
		
		//OID割付の名前解決
		for(MibExpressionOidAlocation expOidAloc :  target.getOidAlocationList() ){
			processOidExpression(expOidAloc.getSymbolName(), expOidAloc.getExpOid());
		}
		for(MibExpressionNotificationType expNotification :  target.getNotificationTypeList()){
			processOidExpression(expNotification.getSymbolName(),expNotification.getExpOid());
		}
		
		//変換対象とインスタンス化
		mibSymbolList = processTargetExpression();
		
		//解析済みキャッシュの更新
		updateCache();
	}
	
	/**
	 * インポートの解決
	 * インポート表現の内容を元にOID名解決キャッシュを設定
	 */
	private void resolveImports(){
		
		
		// インポート失敗シンボルリスト
		ConcurrentHashMap<String,MibToken> importFailSymbolList = new ConcurrentHashMap<String,MibToken>() ;

		ArrayList<ObjectIdentifierValue> importOidList = new ArrayList<ObjectIdentifierValue>();

		//既定で無条件に実施するインポートの対応
		for( int counter = 0 ; counter  < defactMibSymbol.length ; counter++ ){
			MibValue mibValue =  mibValueCache.getMibValue(
				defactMibSymbol[counter][1]
				,defactMibSymbol[counter][0]);
			if( mibValue != null && mibValue instanceof ObjectIdentifierValue ){
				importOidList.add((ObjectIdentifierValue) mibValue);
			}
		}
		
		//インポートの参照
		if( target.getImport() != null){
			for(MibExpressionAbstract expImportChild :  target.getImport().getChildren() ){
				//MIB名毎に以下を実施
				if( expImportChild instanceof MibExpressionFrom){
					MibExpressionFrom expFrom = (MibExpressionFrom) expImportChild;
					String mibName =  expFrom.getMibName().getToken().getTokenString();
					//インポート対象となる値名称がなければ無視
					String[] symbolNameArray = expFrom.getNeedImportNames();
					if( symbolNameArray == null ){
						if(log.isDebugEnabled()){
							log.debug("resolveImports. skip import .  value name reference is nothing . MIB=["+mibName+"]");
						}
						continue;
					}
					for(String targetSymbol:  symbolNameArray ){
						//定義値キャッシュに存在すればOID名解決キャッシュへ登録対象とする
						MibValue mibValue =  mibValueCache.getMibValue(	mibName	,targetSymbol);
						if( mibValue != null && mibValue instanceof ObjectIdentifierValue ){
							importOidList.add((ObjectIdentifierValue) mibValue);
						}else{
							//なければインポート不可リストに登録しておく
							importFailSymbolList.put(targetSymbol, expFrom.getMibName().getToken());
							if(log.isDebugEnabled()){
								log.debug("resolveImports. target is not exist . MIB=["+mibName+"] value name=["+targetSymbol+"]");
							}
						}
					}
				}
			}
		}
		//IMPORTの内容を元にOID値名前解決用キャッシュを作成
		this.oidNameResolveCache = new OidNameResolveCache(mibLoaderLog,mibFile,importOidList,importFailSymbolList);
	}

	/**
	 * OID名割付表現の処理
	 * 
	 *  表現の内容を元にOID名解決キャッシュを更新
	 *  
	 *  連結ID定義の名称なしは シンボル名の複写扱いとなる 
	 *    ex { transmission 94 2 } の 94 はシンボル名と同じ名称の扱い
	 * @param expSymbolName シンボル名
	 * @param expOid OID構文表現
	 */
	private void processOidExpression( MibExpressionString expSymbolName, MibExpressionObjectIdentifer expOid) throws MibLoaderException{
		
		//OID値名前解決用キャッシュに内容を登録
		MibToken symbolNameToken = expSymbolName.getToken();
		MibToken parentNameToken =null; 
		if( expOid.getParentName() != null ){
			parentNameToken = expOid.getParentName().getToken();
		}
		BigInteger symbolValue = null;
		if( expOid.getIdNumber() != null){
			symbolValue =new BigInteger(expOid.getIdNumber().getToken().getTokenString());
		}
		ArrayList<MibExpressionChainId> chainIdList = expOid.getChainIdList();
		try{
			oidNameResolveCache.registParseOid(
					  symbolValue, symbolNameToken, parentNameToken ,chainIdList);
		}catch(MibLoaderException e){
			String message = "Process error . Target token is "+symbolNameToken.getTokenString()+" .";
			e.getLog().addEntry(mibFile, symbolNameToken.getTokenLine(), symbolNameToken.getTokenCols(), message);
			throw e;
		}
	}

	/**
	 * 象徴化の対象である表現の処理
	 * 表現内容を象徴インスタンスにマッピングして返却
	 * 
	 * @return 象徴インスタンス一覧
	 */
	private  ArrayList<MibSymbol> processTargetExpression() throws MibLoaderException{
			ArrayList<MibSymbol> ret = new ArrayList<MibSymbol>();
			//NOTIFICATION表現の一覧参照
			if(target.getNotificationTypeList() != null ){
				for(MibExpressionNotificationType expNoti :  target.getNotificationTypeList()){
					try{
						//MibTypeの生成
						ArrayList<ObjectIdentifierValue> objects = new ArrayList<ObjectIdentifierValue>();
						String description=""; 
						if(expNoti.getObjects() != null ){
							for( MibExpressionAbstract expStr : expNoti.getObjects().getChildren()){
								String objName = ((MibExpressionString) expStr).getToken().getTokenString();
								try{
									objects.add(oidNameResolveCache.getResovleOid(objName));
								}catch(MibLoaderException e){
									String message = "Reference error . OBJECTS name is not found . name is "+expStr.getToken().getTokenString()+"";
									e.getLog().addEntry(mibFile, expStr.getToken().getTokenLine(), expStr.getToken().getTokenCols(), message);
									throw e;
								}
							}
						}
						if(expNoti.getDescription() != null ){
							for( MibExpressionAbstract expStr : expNoti.getDescription().getChildren()){
								description = adjustDescription(expStr.getToken().getTokenString());
								break;
							}
						}
						MibType type = ( MibType) new SnmpNotificationType(description,objects);
			
						//MibValue(OID定義)の生成
						MibValue oid =oidNameResolveCache.getResovleOid(expNoti.getSymbolName().getToken().getTokenString());
						
						//シンボル化して返却リストに格納
						MibSymbol symbol = (MibSymbol)new MibValueSymbol(expNoti.getSymbolName().getToken().getTokenString(),oid ,type);
						ret.add(symbol);
					}catch(MibLoaderException e){
						String message = "Parse error . Syntax keyword is "+expNoti.getToken().getTokenString()+"";
						e.getLog().addEntry(mibFile, expNoti.getToken().getTokenLine(), expNoti.getToken().getTokenCols(), message);
						throw e;
					}
				}
			}
			//TRAP表現の一覧参照
			if(target.getTrapTypeList() != null ){
				for(MibExpressionTrapType expTrap :  target.getTrapTypeList()){
					try{
						//MibTypeの生成
						ArrayList<ObjectIdentifierValue> variables = new ArrayList<ObjectIdentifierValue>();
						String description=""; 
						String enterpriseName =null;
						ObjectIdentifierValue oid =null;
						if(expTrap.getVariables() != null ){
							for( MibExpressionAbstract expStr : expTrap.getVariables().getChildren()){
								try{
									variables.add(oidNameResolveCache.getResovleOid(expStr.getToken().getTokenString()));
								}catch(MibLoaderException e){
									String message = "Reference error . VARIABLES name is not found . name is "+expStr.getToken().getTokenString()+"";
									e.getLog().addEntry(mibFile, expStr.getToken().getTokenLine(), expStr.getToken().getTokenCols(), message);
									throw e;
								}
							}
						}
						if(expTrap.getDescription() != null ){
							for( MibExpressionAbstract expStr : expTrap.getDescription().getChildren()){
								description = adjustDescription(expStr.getToken().getTokenString());
								break;
							}
						}
						if(expTrap.getEnterprise().getChildren().size()==1){ 
							// ENTERPIESE 参照指定
								for( MibExpressionAbstract expOidName : expTrap.getEnterprise().getChildren()){
									try{
										enterpriseName = expOidName.getToken().getTokenString();
										oid =oidNameResolveCache.getResovleOid(enterpriseName);
										break;
									}catch(MibLoaderException e){
										String message = "Reference error. ENTERPIESE name is not found . name is "+ expOidName.getToken().getTokenString()+" ";
										e.getLog().addEntry(mibFile, expTrap.getToken().getTokenLine(), expTrap.getToken().getTokenCols(), message);
										throw e;
									}
								}
						}else{
							 // ENTERPIESE OID指定
							ObjectIdentifierValue setLastParent =null;
							for( MibExpressionAbstract expChainId : expTrap.getEnterprise().getChildren()){
								MibExpressionChainId target = (MibExpressionChainId)expChainId;
								String setValueName ;
								BigInteger setValue  ;
								//指定された名称とIDを元にインスタンスを作成
								if( target.getIdNumber() == null ){
									setValueName = "" ;
									setValue = new BigInteger(target.getToken().getTokenString());
								}else{
									setValueName = target.getToken().getTokenString();
									setValue = new BigInteger(target.getIdNumber().getToken().getTokenString());
								}
								ObjectIdentifierValue setOid = new ObjectIdentifierValue (setLastParent,setValueName,setValue );
								setLastParent = setOid;
							}
							oid = setLastParent;
						}
						MibType type = ( MibType) new SnmpTrapType(description,variables,oid);
						//MibValueの生成(Number)
						MibValue id = (MibValue)new NumberValue(new BigInteger(expTrap.getIdNumber().getToken().getTokenString()) );
						
						//シンボル化して返却リストに格納
						MibSymbol symbol = (MibSymbol)new MibValueSymbol(expTrap.getSymbolName().getToken().getTokenString(),id ,type);
						ret.add(symbol);
					}catch(MibLoaderException e){
						String message = "Parse error . Syntax keyword is "+expTrap.getToken().getTokenString()+"";
						e.getLog().addEntry(mibFile, expTrap.getToken().getTokenLine(), expTrap.getToken().getTokenCols(), message);
						throw e;
					}
				}
		}
		return ret;
	}
	
	/**
	 * 解析したMIBのOID値を解析結果キャッシュに転記
	 */
	private  void updateCache() throws MibLoaderException{
		ArrayList<MibValue> setList = new ArrayList<MibValue> ();
		for ( String name :oidNameResolveCache.getResovleOidNameList()){
			setList.add(oidNameResolveCache.getResovleOid(name));
		}
		mibValueCache.setMibValues(target.getMibName().getToken().getTokenString(),setList);
	}
	
	/**
	 * DESCRIPTION の 内容調整
	 */
	private String adjustDescription(String desc){
		//tabの8スペース化
		String replace_tab =desc.replaceAll("\t", "        ");

		//mibble準拠対応 複数行 Description 特殊trim( . の後ろを除いてtrimする)実施 →差異を許容する方針となったのでコメントアウト
		/*
		StringBuilder rewriteBuf = new StringBuilder();
		String[] StrAryBuf= replace_tab.split("\n");
		if( StrAryBuf.length > 0){
			for( String str : StrAryBuf){
				if( rewriteBuf.length() > 0 ){
					rewriteBuf.append("\n");
				}
				String trimBuf = str.trim();
				if(MibTokenUtil.isLastPeriod(trimBuf)){
					rewriteBuf.append(str);
				}else{
					rewriteBuf.append(trimBuf);
				}
			}
			if(MibTokenUtil.isLastLineField(replace_tab)){
				rewriteBuf.append("\n");
			}
			return rewriteBuf.toString();
		}
		*/
		return replace_tab;		
	}
	
} 