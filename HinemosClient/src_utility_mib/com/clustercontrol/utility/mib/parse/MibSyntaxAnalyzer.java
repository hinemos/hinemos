/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib.parse;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.utility.mib.MibLoaderException;
import com.clustercontrol.utility.mib.MibLoaderLog;
import com.clustercontrol.utility.mib.parse.MibTokenUtil;

/**
 * MIBのパース処理(Trap関連情報取得)における
 * 構文解析クラスです。
 * 
 * 指定されたMIBファイルから字句解析クラスを経由して 
 * 内容を読み込み、構文に問題がなければ各種構文の表現クラスを生成します。
 * 
 * 主たる処理としてはDEFINITONS表現の取得ですが
 * MIB名称やIMPORTS表現の部分的な抜き出しにも対応しています。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */

public class MibSyntaxAnalyzer	{
	
	private static Log log = LogFactory.getLog(MibSyntaxAnalyzer.class);

	/**
	 * MIBファイル
	 */
	private File mibFile;

	/**
	 * 字句解析インスタンス
	 */
	private MibTokenizer mibTokenizer;

	/**
	 *ログ管理インスタンス
	 */
	private MibLoaderLog mibLoaderLog;

	/**
	 *字句のバックトラッキングバッファ
	 */
	private MibToken[] tokenBufPre = new MibToken[3] ;

	/**
	 *字句のカレントバッファ
	 */
	private MibToken tokenBufCur;
	
	/**
	 * 対象MIBのSMIv2マクロ定義の有無フラグ
	 */
	private boolean isSmiV2 = false;

	/**
	 * DEFINITONS表現インスタンス
	 */
	private MibExpressionDefinitions expDefinitions = null;

	/**
	 * IMPORT表現インスタンス
	 */
	private MibExpressionImport expImport = null;

	/**
	 * MIB名称インスタンス
	 */
	private MibToken mibName = null;

	/**
	 * ヘッダーコメント
	 */
	private String mibHeaderComment = "";
	
	/**
	 * 文中コメント
	 */
	private String mibInnerComment = "";
	
	/**
	 * フッターコメント
	 */
	private String mibFooterComment = "";
	
	/**
	 * コンストラクタ
	 */
	public MibSyntaxAnalyzer (	MibLoaderLog mibLoaderLog, File mibFile ) {
		this.mibLoaderLog = mibLoaderLog;
		this.mibFile = mibFile;
	}
	
	/**
	 * DEFINITONS表現 取得
	 * 
	 * @return DEFINITONS表現
	 * 
	 */
	public MibExpressionDefinitions getDefinitions() throws MibLoaderException {
		//構文解析がまだなら実行しておく
		if( null == expDefinitions){
			expDefinitions = createDefinitions();
		}
		return expDefinitions;
	}
	
	/**
	 * SMI Version 取得
	 * 
	 * @return バージョン番号
	 */
	public int getSmiVersion()	throws MibLoaderException {
		//構文解析がまだなら実行しておく
		if( null == expDefinitions){
			expDefinitions = createDefinitions();
		}
		if(isSmiV2){
			return 2;
		}else{
			return 1;
		}
	}
	
	/**
	 * ヘッダーコメント取得
	 * 
	 * @return ヘッダーコメント
	 */
	public String getHeader()  throws MibLoaderException {
		//構文解析がまだなら実行しておく
		if( null == expDefinitions){
			expDefinitions = createDefinitions();
		}
		String retBuf;
		if(mibInnerComment.equals("")){
			retBuf = mibHeaderComment;
		}else{
			retBuf = mibHeaderComment + mibInnerComment;
		}
		if ( retBuf.equals("") ){
			return null;
		}
		return retBuf;		
	}
	
	/**
	 * フッターコメント取得
	 * 
	 * @return フッターコメント
	 */
	public String getFooter()  throws MibLoaderException {
		//構文解析がまだなら実行しておく
		if( null == expDefinitions){
			expDefinitions = createDefinitions();
		}
		if ( mibFooterComment.equals("") ){
			return null;
		}
		return mibFooterComment;
	}

	/**
	 * インポート表現の取得
	 * 
	 * @return インポート表現
	 */
	public MibExpressionImport getImport()	throws MibLoaderException {
		//構文解析がまだならインポート部分のみを解析して取得する
		if(this.expImport == null){
			mibTokenizer  =	 MibTokenizer.factoryMethod(mibLoaderLog,mibFile);
			try{
				this.mibName =createMibName().getMibName().getToken();
				this.expImport = createImport();
			}finally{
				//字句解析インスタンスの後始末。
				mibTokenizer.close();
			}
		}
		return this.expImport;
	}

	/**
	 * MIB名称の取得
	 * 
	 * @return MIB名称
	 */
	public MibToken getMibName()  throws MibLoaderException {
		//構文解析がまだなら対象部分のみを解析して取得する
		if(this.mibName == null){
			mibTokenizer  =	 MibTokenizer.factoryMethod(mibLoaderLog,mibFile);
			try{
				this.mibName =createMibName().getMibName().getToken();
			}finally{
				//字句解析インスタンスの後始末。
				mibTokenizer.close();
			}
		}
		return this.mibName;
	}

	
	/**
	 * DEFINITONS表現インスタンス 生成(実質的なmain)
	 */
	private MibExpressionDefinitions createDefinitions() throws MibLoaderException{
		if(log.isDebugEnabled()){
			log.debug("createDefinitions target=" + mibFile.getPath());
		}
		
		//字句解析インスタンス生成
		mibTokenizer  =	 MibTokenizer.factoryMethod(mibLoaderLog,mibFile);
		try{
			//MIB名称の取得
			MibExpressionDefinitions expDefinition = createMibName();
			
			//IMPORT表現を取得
			this.expImport= createImport();
			expDefinition.setImport(expImport);
			
			//EXPORTS表現があれば無視
			skipExport();
			
			boolean hasToken =true;
			if( tokenBufCur == null	 ){
				hasToken = false;
			}
			//ENDキーワードが出てくるまで読込を続ける
			while( hasToken ){
				//ENDなら抜ける
				if( MibTokenUtil.END.equals(tokenBufCur.getTokenString())){
					break;
				}
				// MACRO定義なら対応するENDまで無視
				if(MibTokenUtil.MACRO.equals(tokenBufCur.getTokenString() )){
					skipToEnd();
					hasToken =getToken();//次の語句を読み込み
					continue;
				}
				//SMI-V2判定
				if( isSmiV2 == false && MibTokenUtil.isSmiv2Target(tokenBufCur.getTokenString()) ){
					isSmiV2 = true;
				}
				//取得対象となる表現のキーワードがあれば、表現インスタンスを取得して紐付けする。
				if(MibTokenUtil.isAnalyzeTarget(tokenBufCur.getTokenString())){
					// MACRO定義確認のための次の語句を読み込み
					if(getToken()==false){
						break;
					}
					// MACRO定義なら対応するENDまで無視
					if(MibTokenUtil.MACRO.equals(tokenBufCur.getTokenString() )){
						skipToEnd();
						hasToken =getToken();//次の語句を読み込み
						continue;
					}
					if(MibTokenUtil.NOTIFICATION_TYPE.equals(tokenBufPre[0].getTokenString())){
						expDefinition.addChild(createNotificationType());
					}else if(MibTokenUtil.TRAP_TYPE.equals(tokenBufPre[0].getTokenString())){
						expDefinition.addChild(createTrapType());
					}else if(MibTokenUtil.IDENTIFIER.equals(tokenBufPre[0].getTokenString())){
						if(MibTokenUtil.OBJECT.equals(tokenBufPre[1].getTokenString())
							&&MibTokenUtil.COLON_EQUALS.equals(tokenBufCur.getTokenString())){
							expDefinition.addChild(createOidAllocation());
						}else{
							continue;
						}
					}else{
						expDefinition.addChild(createOidAllocation());
					}
				}
				hasToken =getToken();//次の語句を読み込み
			}
			//ENDが取得できなければ 構文エラー
			if( tokenBufCur == null	 || MibTokenUtil.END.equals(tokenBufCur.getTokenString())== false ){
				String message = "Syntax error. Keyword of END is not found.";
				mibLoaderLog.addEntry(mibTokenizer.getFile() , mibName.getTokenLine(),mibName.getTokenLine(), message);
				throw new MibLoaderException(mibLoaderLog);
			}
			if( mibTokenizer.getSkipComment() != null){
				mibInnerComment += mibTokenizer.getSkipComment();
			}
			//フッターコメント取得
			mibFooterComment =mibTokenizer.getFooter();
			return expDefinition;
		}finally{
			//字句解析インスタンスの後始末。
			mibTokenizer.close();
		}
	}

	/**
	 * MIB名称 生成
	 *	DEFINITIONSキーワードの存在チェックと併せて実施
	 *
	 * @return DEFINITIONS表現（初期設定のみ）
	 */
	private MibExpressionDefinitions createMibName() throws MibLoaderException{
		//ヘッダーコメント取得
		mibHeaderComment =mibTokenizer.getHeader();
		
		//MIB名称を取得
		MibToken mibName = mibTokenizer.getNextToken();
		if( mibName == null	 ){
			String message = "Syntax error. MIB name is not found.";
			mibLoaderLog.addEntry(mibTokenizer.getFile() , 0, 0 , message);
			throw new MibLoaderException(mibLoaderLog);
		}
		
		//DEFINITONSキーワードを取得（途中の語句は無視）
		while( getToken() ){
			if( MibTokenUtil.DEFINITIONS.equals(tokenBufCur.getTokenString())){
				break;
			}
		}
		if( tokenBufCur == null	 ){
			//取得できなければ構文エラー
			String message = "Syntax error. Keyword of DEFINITIONS( MIB name's next token  ) is not found.";
			mibLoaderLog.addEntry(mibTokenizer.getFile() , mibName.getTokenLine(),mibName.getTokenLine(), message);
			throw new MibLoaderException(mibLoaderLog);
		}
		
		//取得した名称を返却
		MibExpressionDefinitions expDefinition = new MibExpressionDefinitions(tokenBufCur);
		expDefinition.setMibName( new MibExpressionString(mibName));
		this.mibName = mibName;
		return expDefinition;
	}
	/**

	/**
	 * IMPORTS表現インスタンス 生成
	 * 
	 * 字句を順次取得してIMPORT構文として問題なければ
	 * 表現としてインスタンス化する
	 * 
	 * @return IMPORTS表現
	 */
	private MibExpressionImport createImport() throws MibLoaderException{
		boolean hasToken =true;
		if( tokenBufCur == null	 ){
			hasToken = false;
		}
		//BEGINキーワードが出てくるまで読込を続ける
		while( hasToken ){
			if( MibTokenUtil.BEGIN.equals(tokenBufCur.getTokenString())){
				break;
			}
			hasToken =getToken();
		}
		if( tokenBufCur == null	 ){
			//取得できなければ構文エラー
			String message = "Syntax error. Keyword of BEGIN( DEFINITIONS's next token ) is not found.";
			mibLoaderLog.addEntry(mibTokenizer.getFile() , 0 , 0, message);
			throw new MibLoaderException(mibLoaderLog);
		}

		//BEGINの次がIMPORTSでなければIMPORTS指定なしと判断する。
		if( ( getToken() == false ) || (MibTokenUtil.IMPORTS.equals(tokenBufCur.getTokenString())== false)){
			if(log.isDebugEnabled()){
				log.debug("createImport . IMPORTS is nothing . target=" + mibFile.getPath());
			}
			mibInnerComment += mibTokenizer.getSkipComment();
			return null;
		}
		mibInnerComment += mibTokenizer.getSkipComment();
		
		MibExpressionImport expImport = new MibExpressionImport(tokenBufCur);
		
		//セミコロンが出てくるまで読込を続ける
		ArrayList<MibToken> targetNameList = new ArrayList<MibToken>();
		while( getToken() ){
			if( MibTokenUtil.SEMICOLON.equals(tokenBufCur.getTokenString())){
				break;
			}
			//FROMが出てくるまで カンマ以外を IMPORT対象のシンボルとしてとして一時保留
			if( ! MibTokenUtil.FROM.equals(tokenBufCur.getTokenString())){
				if( ! MibTokenUtil.COMMA.equals(tokenBufCur.getTokenString())){
					targetNameList.add(tokenBufCur);
				}
				continue;
			}
			//FROMが出てきたら 次の語句を MIB名称としたFROM表現を生成し IMPORTに紐付け
			else{
				MibExpressionFrom expFrom = new MibExpressionFrom(tokenBufCur);
				if( getToken() && (tokenBufCur.getTokenKind() == MibTokenUtil.Kind.STRING ) ){
					expFrom.setMibName(new MibExpressionString(tokenBufCur));
					for (MibToken targetName :targetNameList){
						expFrom.addSymbolName(new MibExpressionString(targetName));
					}
					expImport.addChild(expFrom);
					targetNameList = new ArrayList<MibToken>();
				}else{
					//FROMの次の語句を取得できなければ構文エラー
					String message = "Syntax error. Import MIB name (FROM's next token ) is not found.";
					mibLoaderLog.addEntry(mibTokenizer.getFile() , expFrom.getToken().getTokenLine(),expFrom.getToken().getTokenCols(), message);
					throw new MibLoaderException(mibLoaderLog);
				}
			}
		}
		if( tokenBufCur == null	 ){
			//セミコロンが取得できなければ構文エラー
			String message = "Syntax error. IMPORTS syntax termination mark (';') is not found.";
			mibLoaderLog.addEntry(mibTokenizer.getFile() , expImport.getToken().getTokenLine(),expImport.getToken().getTokenCols(), message);
			throw new MibLoaderException(mibLoaderLog);
		}
		return expImport;
	}

	/**
	 * EXPORTS表現 読み飛ばし
	 * 
	 */
	private void skipExport() throws MibLoaderException{
		//BEGINの次がEXPORTS指定なら ; まで読み飛ばし
		if( (MibTokenUtil.EXPORTS.equals(tokenBufCur.getTokenString())== false)){
			return;
		}
		if(log.isDebugEnabled()){
			log.debug("skipExport . start search ';' . target=" + mibFile.getPath());
		}
		MibToken exports = tokenBufCur;
		while( getToken() ){
			if(  MibTokenUtil.SEMICOLON.equals(tokenBufCur.getTokenString())){
				break;
			}
		}
		if( tokenBufCur == null	 ){
			// ; が取得できなければ 構文エラー
			String message = "Syntax error. EXPORTS syntax termination mark (';') is not found.";
			mibLoaderLog.addEntry(mibTokenizer.getFile() , exports.getTokenLine(),exports.getTokenCols(), message);
			throw new MibLoaderException(mibLoaderLog);
		}
	}

	/**
	 * NOTIFICATION-TYPE表現インスタンス 生成
	 * 
	 * 予約語の次の語句がMACROでないことを確認してから呼び出されるので注意
	 * 
	 * @return NOTIFICATION-TYPE表現
	 */
	private MibExpressionNotificationType createNotificationType() throws MibLoaderException{

		MibExpressionNotificationType expNotify =  new MibExpressionNotificationType(tokenBufPre[0]);
		
		//予約語前の語句をシンボル名として取得
		expNotify.setSymbolName(new MibExpressionString(tokenBufPre[1]));

		//OBJECTSがあれば対応
		if( tokenBufCur != null &&  MibTokenUtil.OBJECTS.equals(tokenBufCur.getTokenString())){
			expNotify.setObjects(createObjects());
			getToken();
		}
		//STATUSがあればスキップ
		if( tokenBufCur != null &&  MibTokenUtil.STATUS.equals(tokenBufCur.getTokenString())){
			getToken(); 
			getToken();
		}
		
		//DESCRIPTIONがあれば対応
		if( tokenBufCur != null &&   MibTokenUtil.DESCRIPTION.equals(tokenBufCur.getTokenString())){
			expNotify.setDescription(createDescription());
			getToken(); 
		}
		
		//REFERENCEがあればスキップ
		if( tokenBufCur != null &&  MibTokenUtil.REFERENCE.equals(tokenBufCur.getTokenString())){
			getToken(); 
			getToken();
		}
		
		//値割付識別子が取得できなければ 構文エラー
		if( tokenBufCur == null	 || ( MibTokenUtil.COLON_EQUALS.equals(tokenBufCur.getTokenString()) == false ) ){
			//値割付識別子が取得できなければ 構文エラー
			String message = "Syntax error. Parts of NOTIFICATION-TYPE syntax (':==') is not found .";
			mibLoaderLog.addEntry(mibTokenizer.getFile() , expNotify.getToken().getTokenLine(),expNotify.getToken().getTokenCols(), message);
			throw new MibLoaderException(mibLoaderLog);
		}
		
		//値割付がOIDの構文なら表現として取得
		MibExpressionObjectIdentifer expOid =  createObjectIdentifer();
		expNotify.setExpOid(expOid);
		
		return expNotify;
	}

	/**
	 * TRAP-TYPE表現インスタンス 生成
	 * 
	 * 予約語の次の語句がMACROでないことを確認してから呼び出されるので注意
	 * 
	 * @return TRAP-TYPE表現
	 */
	private MibExpressionTrapType createTrapType() throws MibLoaderException{

		MibExpressionTrapType expTrap =  new MibExpressionTrapType(tokenBufPre[0]);
		try {
			
			//予約語前の語句をシンボル名として取得
			expTrap.setSymbolName(new MibExpressionString(tokenBufPre[1]));

			//ENTERPRISE があれば対応
			if(  MibTokenUtil.ENTERPRISE.equals(tokenBufCur.getTokenString())){
				expTrap.setEnterprise(createEnterprise());
				getToken();
			}
			//VARIABLESSがあれば対応
			if( tokenBufCur != null && MibTokenUtil.VARIABLES.equals(tokenBufCur.getTokenString())){
				expTrap.setVariables(createVariables());
				getToken();
			}
			//DESCRIPTIONがあれば対応
			if( tokenBufCur != null &&  MibTokenUtil.DESCRIPTION.equals(tokenBufCur.getTokenString())){
				expTrap.setDescription(createDescription());
				getToken();
			}
			//REFERENCEがあればスキップ
			if( tokenBufCur != null &&  MibTokenUtil.REFERENCE.equals(tokenBufCur.getTokenString())){
				getToken(); 
				getToken();
			}
			//値割付識別子が取得できなければ 構文エラー
			if( tokenBufCur == null	 || ( MibTokenUtil.COLON_EQUALS.equals(tokenBufCur.getTokenString()) == false ) ){
				String message = "Syntax error. Parts of TRAP-TYPE syntax (':==') is not found .";
				mibLoaderLog.addEntry(mibTokenizer.getFile() , expTrap.getToken().getTokenLine(),expTrap.getToken().getTokenCols(), message);
				throw new MibLoaderException(mibLoaderLog);
			}
			
			//値割付が数値(INTEGER)でなければエラー
			if(  ( getToken() == false ) || MibTokenUtil.Kind.NUMBER != tokenBufCur.getTokenKind() ){
				String message = "Syntax error. Set value of TRAP-TYPE syntax ( :== any) is only number .";
				mibLoaderLog.addEntry(mibTokenizer.getFile() , tokenBufCur.getTokenLine(),tokenBufCur.getTokenCols(), message);
				throw new MibLoaderException(mibLoaderLog);
			}
			try{
				Integer.parseInt(tokenBufCur.getTokenString());
			}catch(NumberFormatException e){
				String message = "Syntax error. Set value of TRAP-TYPE syntax ( :== any) is only integer .";
				mibLoaderLog.addEntry(mibTokenizer.getFile() , tokenBufCur.getTokenLine(),tokenBufCur.getTokenCols(), message);
				throw new MibLoaderException(mibLoaderLog);
			}
			expTrap.setIdNumber(new MibExpressionNumber(tokenBufCur));

			//ENTERPRISEがなければ構文エラー
			if( expTrap.getEnterprise() == null ){
				String message = "Syntax error. ENTERPRISE syntax ('ENTERPRISE ～ ') is nothing .";
				mibLoaderLog.addEntry(mibTokenizer.getFile() , expTrap.getToken().getTokenLine(), expTrap.getToken().getTokenCols(), message);
				throw new MibLoaderException(mibLoaderLog);
			}
			
			return expTrap;
		}catch(MibLoaderException e){
			String message = "Process error. There is a problem with the TRAP-TYPE syntax parse.";
			mibLoaderLog.addEntry(mibTokenizer.getFile() , expTrap.getToken().getTokenLine(), expTrap.getToken().getTokenCols(), message);
			throw e;
		}
	}

	/**
	 * 次のENDキーワードまでスキップ
	 * 
	 */
	private void skipToEnd() throws MibLoaderException{

		//ENDキーワードが出てくるまで読込を続ける
		while( getToken() ){
			//ENDなら抜ける
			if( MibTokenUtil.END.equals(tokenBufCur.getTokenString())){
				break;
			}
		}

	}

	/**
	 * OBJECTS表現インスタンス 生成
	 * 
	 * @return OBJECTS表現
	 */
	private MibExpressionObjects createObjects() throws MibLoaderException{

		MibExpressionObjects expObjs =  new MibExpressionObjects(tokenBufCur);
		getToken();

		//波カッコ内の字句を一覧取得して 文字表現として紐付け（カンマは除く）
		MibToken[] tokenArray = createCurlyBracketEnclose();
		for (int counter = 0 ; counter <tokenArray.length ; counter++){
			if(MibTokenUtil.COMMA.equals(tokenArray[counter].getTokenString()) == false){
				expObjs.addChild(new MibExpressionString(tokenArray[counter]) );
			}
		}

		return expObjs;
	}

	/**
	 * VARIABLES表現インスタンス 生成
	 * 
	 * @return VARIABLES表現
	 */
	private MibExpressionVariables createVariables() throws MibLoaderException{

		MibExpressionVariables expVaris =  new MibExpressionVariables(tokenBufCur);
		getToken();

		//波カッコ内の字句を一覧取得して 文字表現として紐付け（カンマは除く）
		MibToken[] tokenArray = createCurlyBracketEnclose();
		for (int counter = 0 ; counter <tokenArray.length ; counter++){
			if(MibTokenUtil.COMMA.equals(tokenArray[counter].getTokenString()) == false){
				expVaris.addChild(new MibExpressionString(tokenArray[counter]));
			}
		}

		return expVaris;
	}


	/**
	 * ENTERPRISE表現インスタンス 生成
	 */
	private MibExpressionEnterprise createEnterprise() throws MibLoaderException{

		MibExpressionEnterprise expEnter =  new MibExpressionEnterprise(tokenBufCur);
		
		boolean getRes =getToken();
		//次の字句が { の場合 波カッコ内の字句を一覧取得して 連結ID表現として紐付け
		if( getRes == true &&  MibTokenUtil.LEFT_CURLY_PART.equals(tokenBufCur.getTokenString())){
			MibToken[] tokenArray = createCurlyBracketEnclose();
			ArrayList<MibExpressionChainId> chainIdList	= createChainIdList(tokenArray);
			int chainIdNum = chainIdList.size();
			for (int counter = 0 ; counter <chainIdNum ; counter++){
				expEnter.addChild(chainIdList.get(counter));
			}
		}else
		//次の字句が文字列ならそのまま紐付け
		if( getRes == true && tokenBufCur.getTokenKind() == MibTokenUtil.Kind.STRING ){
			expEnter.addChild(new MibExpressionString(tokenBufCur));
		}
		//それ以外は構文エラー
		else{
			String message = "Syntax error. ENTERPRISE syntax ('ENTERPRISE [problem string] ') is incorrect .";
			mibLoaderLog.addEntry(mibTokenizer.getFile() , expEnter.getToken().getTokenLine(),expEnter.getToken().getTokenCols(), message);
			throw new MibLoaderException(mibLoaderLog);
		}
		
		return expEnter;
	}

	/**
	 * DESCRIPTION表現インスタンス 生成
	 * 
	 * @return DESCRIPTION表現
	 */
	private MibExpressionDescription createDescription() throws MibLoaderException{
		
		MibExpressionDescription expDesc =  new MibExpressionDescription(tokenBufCur);
		
		//次の字句は "での囲い込みであること。
		if( (getToken() == false ) || tokenBufCur.getTokenKind() != MibTokenUtil.Kind.DQ_ENCLOSE ){
			String message = "Syntax error. DESCRIPTION syntax ('DESCRIPTION \"...\" ') is incorrect .";
			mibLoaderLog.addEntry(mibTokenizer.getFile() , expDesc.getToken().getTokenLine(),expDesc.getToken().getTokenCols(), message);
			throw new MibLoaderException(mibLoaderLog);
		}

		expDesc.addChild(new MibExpressionString(tokenBufCur));

		return expDesc;
	}

	/**
	 * OID割付表現インスタンス 生成
	 * 
	 * 予約語の次の語句がMACROでないことを確認してから呼び出されるので注意
	 * 
	 * @return OID割付する各種表現
	 */
	private MibExpressionOidAlocation createOidAllocation() throws MibLoaderException{
		MibExpressionOidAlocation expOidAloc =  new MibExpressionOidAlocation(tokenBufPre[0]);
		
		//予約後前の語句をシンボル名として取得（OBJECT IDENTIFERのみ構文の関係で個別対応）
		if(MibTokenUtil.IDENTIFIER.equals(tokenBufPre[0].getTokenString())){
			expOidAloc.setSymbolName(new MibExpressionString(tokenBufPre[2]));
		}else{
			expOidAloc.setSymbolName(new MibExpressionString(tokenBufPre[1]));
		}
		
		try{
			//値割付識別子(:==)が出てくるまで読込処理を続ける(途中は読み飛ばす)
			boolean hasToken = true;
			while( hasToken ){
				if(  MibTokenUtil.COLON_EQUALS.equals(tokenBufCur.getTokenString())){
					break;
				}
				//途中で構文解釈対象となる予約語が見つかった場合は  構文くずれとしてエラー
				if( MibTokenUtil.isAnalyzeTarget(tokenBufCur.getTokenString()) && MibTokenUtil.IDENTIFIER.equals(tokenBufCur.getTokenString()) ==false ){
					break;
				}
				hasToken=getToken();
			}
			//値割付識別子が取得できなければ 構文エラー
			if( tokenBufCur == null	 || MibTokenUtil.COLON_EQUALS.equals(tokenBufCur.getTokenString()) ==false ){
				String message = "Syntax error. Parts of "+ expOidAloc.getToken().getTokenString() + " syntax ( :== ) is not found .";
				mibLoaderLog.addEntry(mibTokenizer.getFile() , expOidAloc.getToken().getTokenLine(),expOidAloc.getToken().getTokenCols(), message);
				throw new MibLoaderException(mibLoaderLog);
			}
			
			//値割付がOIDの構文なら表現として取得
			MibExpressionObjectIdentifer expOid =  createObjectIdentifer();
			expOidAloc.setExpOid(expOid);
			return expOidAloc;
		}catch(MibLoaderException e){
			MibToken org = expOidAloc.getToken();
			String message = "Process error. There is a problem with the " + org.getTokenString() +" syntax parse.";
			mibLoaderLog.addEntry(mibTokenizer.getFile() , org.getTokenLine(),org.getTokenCols(), message);
			throw e;
		}
	}

	/**
	 * ObjectIdentifer割付表現インスタンス 生成
	 * 
	 * 予約語のOBJECT IDENTIFIERではなく
	 * :== { ... } であらわす構文についての表現なので注意
	 * 
	 * @return ObjectIdentifer表現
	 */
	private MibExpressionObjectIdentifer createObjectIdentifer() throws MibLoaderException{

		MibExpressionObjectIdentifer expOid =  new MibExpressionObjectIdentifer(tokenBufCur);
		
		//左カッコ'{'を確認
		if ( ( getToken() == false ) || ! MibTokenUtil.LEFT_CURLY_PART.equals(tokenBufCur.getTokenString()) ){
			String message = "Syntax error. Start mark ('{') of object identifer syntax ( :== { ... }) is not found .";
			mibLoaderLog.addEntry(mibTokenizer.getFile() , expOid.getToken().getTokenLine(),expOid.getToken().getTokenCols(), message);
			throw new MibLoaderException(mibLoaderLog);
		} 

		//波カッコ内の字句を一覧取得
		MibToken[] tokenArray = createCurlyBracketEnclose();
		//{}内の字句が1つで数値なら親なしOIDを設定して返却
		if ( tokenArray.length == 1 ){
			if( tokenArray[0].getTokenKind() !=  MibTokenUtil.Kind.NUMBER ){
				String message = "Syntax error. Set value of object identifer syntax ( :== { any }) is incorrect .";
				mibLoaderLog.addEntry(mibTokenizer.getFile() , expOid.getToken().getTokenLine(),expOid.getToken().getTokenCols(), message);
				throw new MibLoaderException(mibLoaderLog);
			}else{
				expOid.setIdNumber(new MibExpressionNumber(tokenArray[0]));
				return expOid;
			}
		} 
		ArrayList<MibExpressionChainId> chainIdList	= createChainIdList(tokenArray);
		int chainIdNum = chainIdList.size();
		for (int counter = 0 ; counter <chainIdNum ; counter++){
			MibExpressionChainId targetChainId = chainIdList.get(counter);
			if( counter == 0 ){
				//先頭なら親OIDの名称か連結IDとして紐付け
				if( targetChainId.getIdNumber() != null ){
					expOid.addChainId( targetChainId);
				}else{
					expOid.setParentName(new MibExpressionString(targetChainId.getToken()));
				}
			}else if( counter == ( chainIdNum - 1 )){
				//末尾ならID番号か連結IDとして紐付け
				if( targetChainId.getIdNumber() != null ){
					expOid.addChainId( targetChainId );
				}else{
					expOid.setIdNumber(new MibExpressionNumber(targetChainId.getToken()));
				}
			}else{
				//途中なら連結IDとして紐付け
				expOid.addChainId( targetChainId);
			}
			
		}

		return expOid;
	}

	/**
	 * 連結ID表現インスタンスのList 生成
	 *
	 * ObjectIdentifer表現内における { 名称(数値) [繰り返し] }の構文表現を表している。
	 * 
	 * 名称のみ や 数値のみ の場合もあるので注意
	 * 
	 * @return 連結ID
	 */

	private ArrayList<MibExpressionChainId> createChainIdList( MibToken[] tokenArray ) throws MibLoaderException{
		ArrayList<MibExpressionChainId> retList = new ArrayList<MibExpressionChainId>();
		MibExpressionChainId lastChain =null ;
		for (int counter = 0 ; counter < tokenArray.length ; counter++){
				// カッコの開始の場合
				// 直前が名称で かつ" ( 数値 )" の形式かチェックしてOKなら 連結IDの数値として紐付け
				if( tokenArray[counter].getTokenString().equals(MibTokenUtil.LEFT_ROUND_PART) && lastChain != null && (counter + 2  <tokenArray.length ) ){
					if(tokenArray[counter+ 2].getTokenString().equals(MibTokenUtil.RIGHT_ROUND_PART) && tokenArray[counter+ 1].getTokenKind() == MibTokenUtil.Kind.NUMBER && lastChain.getToken().getTokenKind() == MibTokenUtil.Kind.STRING  ){
						lastChain.setIdNumber(new MibExpressionNumber(new MibToken(tokenArray[counter + 1].getTokenString(), tokenArray[counter + 1].getTokenLine(),tokenArray[counter + 1].getTokenCols(), tokenArray[counter + 1].getTokenKind())));
						counter = counter + 2;
					}else{
						//フォーマットがおかしいならエラー
						String message = "Syntax error. Set value of object identifer syntax ( :== { any(number) ...  }) is incorrect . pattern 1";
						mibLoaderLog.addEntry(mibTokenizer.getFile() ,tokenArray[counter].getTokenLine(),tokenArray[counter].getTokenCols(), message);
						throw new MibLoaderException(mibLoaderLog);
					}
				}
				//数値 
				else if( tokenArray[counter].getTokenKind() == MibTokenUtil.Kind.NUMBER ){
					MibExpressionChainId chainId = new MibExpressionChainId(new MibToken(tokenArray[counter].getTokenString(), tokenArray[counter].getTokenLine(),tokenArray[counter].getTokenCols(), tokenArray[counter].getTokenKind()));
					retList.add(chainId);
					lastChain = chainId;
				}
				//名称
				else if( tokenArray[counter].getTokenKind() == MibTokenUtil.Kind.STRING ){
					MibExpressionChainId chainId = new MibExpressionChainId(new MibToken(tokenArray[counter].getTokenString(), tokenArray[counter].getTokenLine(),tokenArray[counter].getTokenCols(), tokenArray[counter].getTokenKind()));
					retList.add(chainId);
					lastChain = chainId;
				}else{
					//上記以外はエラー
					String message = "Syntax error. Set value of object identifer syntax ( :== { any(number) ...  }) is incorrect . pattern 2";
					mibLoaderLog.addEntry(mibTokenizer.getFile() ,tokenArray[counter].getTokenLine(),tokenArray[counter].getTokenCols(), message);
					throw new MibLoaderException(mibLoaderLog);
				}
			
		}
		return retList;

	}
	/**
	 * 波カッコ内字句一覧取得
	 * 
	 * カレントに左カッコ'{'が出てきたらカッコ内の字句の配列を返す。
	 *
	 * 波カッコになっていない場合、カレントの字句のみの配列を返す
	 * 
	 */
	private MibToken[] createCurlyBracketEnclose() throws MibLoaderException{
		//左カッコ'{'を確認 
		if ( MibTokenUtil.LEFT_CURLY_PART.equals(tokenBufCur.getTokenString())==false ){
			//違う場合 カレントの字句のみで配列を返す
			MibToken[] ret = new MibToken[1];
			ret[0] =tokenBufCur;
			return ret ;
		}
		MibToken leftPart = tokenBufCur;
		
		//右カッコ'}'が出てくるまで字句をバッファする
		ArrayList<MibToken> tokenBufList = new ArrayList<MibToken>();
		while( getToken() ){
			if(  MibTokenUtil.RIGHT_CURLY_PART.equals(tokenBufCur.getTokenString())){
				break;
			}
			tokenBufList.add(tokenBufCur);
		}
		if( tokenBufCur == null	 ){
			//右カッコ'}'が取得できない場合は構文エラー
			String message = "Syntax error. End of curly bracket enclose  ('{ ... }') is not found .";
			mibLoaderLog.addEntry(mibTokenizer.getFile() , leftPart.getTokenLine(),leftPart.getTokenCols(), message);
			throw new MibLoaderException(mibLoaderLog);
		}
		return tokenBufList.toArray( new MibToken[0]);
	}

	/**
	 * 語句取得
	 * 
	 * 直近３件のバッファリングあり
	 * 
	 * @return 取得の成否
	 */
	private boolean getToken() throws MibLoaderException{
		//直近トークンのバックトラック（3回分まで）を管理しつつ読み込み
		tokenBufPre[2] = tokenBufPre[1];
		tokenBufPre[1] = tokenBufPre[0];
		tokenBufPre[0] = tokenBufCur;
		tokenBufCur =mibTokenizer.getNextToken();
		if(tokenBufCur == null){
			return false;
		}
		return true;
	}
	
}