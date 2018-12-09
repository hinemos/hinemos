/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.utility.mib.Mib;
import com.clustercontrol.utility.mib.parse.MibExpressionAbstract;
import com.clustercontrol.utility.mib.parse.MibExpressionDefinitions;
import com.clustercontrol.utility.mib.parse.MibExpressionFrom;
import com.clustercontrol.utility.mib.parse.MibSymbolConverter;
import com.clustercontrol.utility.mib.parse.MibSyntaxAnalyzer;
import com.clustercontrol.utility.mib.parse.MibToken;
import com.clustercontrol.utility.mib.parse.MibValueCache;
/**
 * MIBのパース処理(Trap関連情報取得)におけるMIBファイルのローダーです。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class MibLoader {

	private static Log log = LogFactory.getLog(MibLoader.class);
	
	/**
	 * 既知としてあらかじめ準備しているMIBファイルへのリソースパス(libディレクトリ内の想定)
	 */
	public static String resourceDir= "mibs";

	/**
	 * 予約で読込するMIB
	 */
	public static final String[] foreseeingMib = new String[]{"RFC1155-SMI","RFC1158-MIB"};
	
	
	/**
	 * 各MIB内に保持された値のキャッシュ（IMPORT用）
	 * 
	*/
	private MibValueCache mibValueCache ;

	/**
	 * ログ管理
	 * 
	*/
	private MibLoaderLog mibLoaderLog ;

	/**
	 * 検索対象パスに設置されてるMIBの一覧（定義ファイルとの紐付け付き）
	 * 
	*/
	private ConcurrentHashMap<String,File> searchTargetMap ;

	/**
	 * 検索対象パス一覧
	 * 
	*/
	private ArrayList<File> searchTargetList ;

	/**
	 * 依存関係解決待ちMIB一覧（循環参照チェック用）
	 * 
	*/
	private HashSet<String> reosolveWaitMibList = new HashSet<String>();

	
	/**
	 * コンストラクタ
	 * 
	*/
	public MibLoader() {
		reset();
	}

	
	/**
	 * 各種設定をリセットします。
	 * 
	*/
	public void reset() {
		searchTargetList = new ArrayList<File>();
		searchTargetMap = null;
		mibValueCache = new MibValueCache();
	}

	/**
	 * MIBファイルの検索対象となるディレクトリパス（サブのディレクトリすべてを含む）を追加します
	 * 
	 * @param tagdir 対象ディレクトリ
	*/
	public void addAllDirs(File tagdir) {
		if (tagdir == null) {
			return;
		}
		addDir(tagdir);
		File[] childrenArray = tagdir.listFiles();
		if(childrenArray == null){
			return;
		}
		for( File childFile: childrenArray){
			if(childFile == null){
				continue;
			}
			if (childFile.isDirectory()) {
				addAllDirs(childFile);
			}
		} 
	}

	
	/**
	 * MIBファイルの検索対象となるディレクトリパスを追加します
	 * 
	 * @param tagdir 対象ディレクトリ
	*/
	public void addDir(File target) {
		if (target == null) {
			return;
		}
		File setDir = target.getParentFile() ;
		if (target.isDirectory()) {
			setDir =target ;
		}
		//同じパスが登録済みなら 重複登録はしない。
		for( File listElement :searchTargetList){
			if( setDir.getPath().equals(listElement.getPath())   ){
				return;
			}
		}
		searchTargetList.add(setDir);
		//検索対象リスト変更したので 各種キャッシュを初期化
		searchTargetMap =null;
		mibValueCache = new MibValueCache();
	}
	
	/**
	 * 指定のMIBファイルを読みこみ、定義されたMIBを表すクラスを返します。
	 * 
	 * @param tagFile MIBファイル
	 * @return MIB
	*/
	public Mib load(File tagFile)  throws MibLoaderException {
		if( tagFile == null){
			return null;
		}
		if(log.isDebugEnabled()){
			log.debug("load start targetFile=" +tagFile.getPath() );
		}
		this.mibLoaderLog = new MibLoaderLog();
		Mib ret= getMib(tagFile);
		if(log.isDebugEnabled()){
			log.debug("load end targetFile=" +tagFile.getPath() );
		}
		return ret;
	}

	/**
	 * 指定のMIBファイルを読みこみ、定義されたMIBを表すクラスを返します。
	 *   内部向け
	 * @param tagFile MIBファイル
	 * @return MIB
	*/
	private Mib getMib(File tagFile)  throws MibLoaderException {
		if(log.isDebugEnabled()){
			log.debug("getMib targetFile=" +tagFile.getPath() );
		}
		//IMPORT構文における他のMIBへの依存を解決（このメソッドへの再帰処理）
		try{
			resolveImportsDependent(tagFile);
		}catch(MibLoaderException e){
			String message = "parse error. IMPORTS of keyword is not resolved . MIB file is " + tagFile.getPath() ;
			e.getLog().addEntry(tagFile, 0, 0, message);
			throw e;
		}
		//全体構文解析を実施
		MibSyntaxAnalyzer syntaxAnalyzer = new MibSyntaxAnalyzer(this.mibLoaderLog,tagFile);
		MibExpressionDefinitions expDefinition = syntaxAnalyzer.getDefinitions();
		
		//MIB属性情報を取得してMIBインスタンス作成
		Mib retMib = new Mib(expDefinition.getMibName().getToken().getTokenString());
		retMib.setHeaderComment(syntaxAnalyzer.getHeader());
		retMib.setFooterComment(syntaxAnalyzer.getFooter());
		retMib.setSmiVersion(syntaxAnalyzer.getSmiVersion());

		//構文表現を象徴クラスに変換
		MibSymbolConverter converter = new MibSymbolConverter(this.mibLoaderLog,mibValueCache,tagFile,expDefinition);
		for( MibSymbol symbol : converter.getSymbols()){
			retMib.addMibSymbol(symbol);
		}
		
		return retMib;
	}

	/**
	 * 既設のMIBファイルの検索対象となるディレクトリパスを除去します。
	 * 
	*/
	public void removeAllDirs() {
		searchTargetList = new ArrayList<File>();
		//検索対象リスト変更したので 各種キャッシュを初期化
		searchTargetMap = null;
		mibValueCache = new MibValueCache();
	}

	/**
	 * MIB定義におけるIMPORTSの依存関係解決（MIB参照の解決）
	 * 
	 * 本クラスにて管理している検索対象パスを利用するため本クラスにて実装
	 * 
	 * @param tagFile MIBファイル
	*/
	private void resolveImportsDependent (File tagFile) throws MibLoaderException {
		//先読み
		loadForeseeingMib();
		
		//対象ファイルがIMPORTしているMIBの一覧取得（SyntaxAnalyzer経由）
		ArrayList<MibToken> importMibNameList = new ArrayList<MibToken>();
		MibSyntaxAnalyzer syntaxAnalyzer = new MibSyntaxAnalyzer(this.mibLoaderLog,tagFile);
		if( syntaxAnalyzer.getImport() != null ){
			for(MibExpressionAbstract expImportChild :	syntaxAnalyzer.getImport().getChildren() ){
				if( expImportChild instanceof MibExpressionFrom){
					MibExpressionFrom expFrom = (MibExpressionFrom) expImportChild;
					//インポート処理の要否判定
					if(expFrom.isNeedImport()){
						importMibNameList.add(expFrom.getMibName().getToken());
					}else{
						if(log.isDebugEnabled()){
							log.debug("resolveImportsDependent. skip import . value name reference is nothing . MIB=["+expFrom.getMibName().getToken().getTokenString()+"]");
						}
					}
				}
			}
		}
		//IMPORTしている各MIBの解決チェック
		for(MibToken importMibName : importMibNameList ){
			//解析済みキャッシュにある場合は解決済み
			if(this.mibValueCache.getMibValueMap(importMibName.getTokenString()) != null ){
				continue;
			}
			
			if(log.isDebugEnabled()){
				log.debug("resolveImports . MIB file=" +tagFile.getName()+ " . Target MIB=" +importMibName.getTokenString() );
			}
			//キャッシュが存在しない場合、まず既定MIBでの有無をチェック
			File defaultMib = getDefaultMibFile(importMibName.getTokenString() );
			if( defaultMib != null ){
				//該当があればそちらを先に読み込み
				if(log.isDebugEnabled()){
					log.debug("resolveImports by default MIB. targetFile=" +defaultMib.getPath() );
				}
				getMib(defaultMib);
				continue;
			}
			
			//全検索パス内のMIB名称一覧を取得（未取得の場合のみ）
			if( this.searchTargetMap ==null ){
				searchTargetMap = getMibNameFromTarget(searchTargetList);
			}
			
			//検索パス内のMIBファイル内での有無をチェック
			if( this.searchTargetMap.containsKey(importMibName.getTokenString())){
				//該当があればそちらを先に読み込み
				if(log.isDebugEnabled()){
					log.debug("resolveImports by search target directory . targetFile=" +this.searchTargetMap.get(importMibName.getTokenString()).getPath() );
				}
				//循環参照防止チェック
				if( reosolveWaitMibList.contains(importMibName.getTokenString()) ){
					//循環参照（MIB-A から MIB-Bが 参照されている 一方 MIB-Bから MIB-Aが参照されている ）なら 無視
					if(log.isDebugEnabled()){
						log.debug("resolveImports. Circular reference is detected . import mib =" + importMibName.getTokenString() + ". targetFile=" +this.searchTargetMap.get(importMibName.getTokenString()).getPath() );
					}
					continue;
				}
				reosolveWaitMibList.add(importMibName.getTokenString());
				getMib(this.searchTargetMap.get(importMibName.getTokenString()));
				reosolveWaitMibList.remove(importMibName.getTokenString());
				continue;
			}
			
			//既定MIBと検索パス内のMIB、どちらにも無ければIMPORTSの依存関係エラー(対象MIBなし)
			String message = "Reference error. MIB to request import is not found . MIB name is " + importMibName.getTokenString() ;
			mibLoaderLog.addEntry(tagFile, importMibName.getTokenLine(), importMibName.getTokenCols(), message);
			throw new MibLoaderException(mibLoaderLog);
		}

	}

	/**
	 * 先読MIBのロード
	 * 
	*/
	private void loadForeseeingMib () throws MibLoaderException {
		
		for( int counter = 0 ; counter  < foreseeingMib.length ; counter++ ){
			//解析済みキャッシュにある場合は解決済み
			if(this.mibValueCache.getMibValueMap(foreseeingMib[counter]) != null ){
				continue;
			}
			//キャッシュが存在しない場合
			File defaultMib = getDefaultMibFile(foreseeingMib[counter] );
			if( defaultMib != null){
				//該当があれば読み込み(IMPORTの依存解決はしない)
				MibSyntaxAnalyzer syntaxAnalyzer = new MibSyntaxAnalyzer(this.mibLoaderLog,defaultMib);
				MibExpressionDefinitions expDefinition = syntaxAnalyzer.getDefinitions();
				MibSymbolConverter converter = new MibSymbolConverter(this.mibLoaderLog,mibValueCache,defaultMib,expDefinition);
				converter.getSymbols();
			}
		}
	}
	/**
	 * 検索対象となるファイルとディレクトリから各ファイルが保持するMIB名MAPを取得
	 * 
	 * @param searchtList 検索対象一覧
	 * @return 各ファイルが保持するMIB名MAP
	*/
	private ConcurrentHashMap<String,File> getMibNameFromTarget( ArrayList<File> searchtList) throws MibLoaderException {
		ConcurrentHashMap<String,File> retMap = new ConcurrentHashMap<String,File>();
		MibLoaderLog trushLog = new MibLoaderLog();
		ArrayList<File> reverseList = new ArrayList<File>(searchtList);
		Collections.reverse(reverseList); 

		//対象一覧を逆順検索
		for ( File target :reverseList){
			//対象の中のファイルからMIB名取得
			File[] childrenArray = target.listFiles();
			if(childrenArray == null){
				continue;
			}
			for ( File targetChild :childrenArray){
				if(targetChild.isFile()){
					try{
						//名称がかぶった場合、後勝ちの上書きで設定
						MibSyntaxAnalyzer syntaxAnalyzer = new MibSyntaxAnalyzer(trushLog,targetChild);
						retMap.put(syntaxAnalyzer.getMibName().getTokenString(),targetChild);
					}catch( MibLoaderException e ){
						//異常発生時は該当のファイルは無視
					}
				}
			}
		}
		return retMap;
	}
	/**
	 * デフォルトMIBファイルの取得
	 * 
	 * @param mibName 対象MIB名
	 * @return ファイルオブジェクト（jar内に存在するのでexistsしてもfalseが帰るので注意）
	*/
	private File getDefaultMibFile ( String mibName ){
		String targetPath = resourceDir	+ "/" + mibName;
		File defaultMib;
		//通常パス検索（MIBデータパッチファイル向け）
		defaultMib = new File(targetPath );
		if( defaultMib.exists() ){
			return defaultMib;
		}
		//リソースパス（jar内 もしくは src内）検索
		ClassLoader classLoader = MibLoader.class.getClassLoader();
		try {
			URL  url = classLoader.getResource(targetPath);
			if( url != null ){
				File file = new File(targetPath);
				return file;
			}
		}catch(Exception e){
			//異常が発生した場合は存在しないものとする。
		}
		return null;
	}
	
}


