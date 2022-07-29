/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib.parse;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.io.FileNotFoundException;
import java.io.BufferedReader;

import com.clustercontrol.utility.mib.MibLoaderLog;
import com.clustercontrol.utility.mib.MibLoader;
import com.clustercontrol.utility.mib.MibLoaderException;
import com.clustercontrol.utility.mib.parse.MibLineScaner;
import com.clustercontrol.utility.mib.parse.MibToken; 
import com.clustercontrol.utility.mib.parse.MibTokenUtil;

/**
 * MIBのパース処理(Trap関連情報取得)における
 * 定義文に対して字句解析を行い字句を切り出す処理を行うクラスです。
 * 
 * 特殊処理にて '"' による囲い込みを 字句の一種として扱います 。
 * 
 * デフォルトMIBファイルのjar内（ソースの一部扱い）からの
 * 取得についてもここで実装しています。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class MibTokenizer {
	
	/**
	 * ログ管理
	 * 
	*/
	private MibLoaderLog mibLoaderLog ;

	/**
	 * MIBファイル
	 */
	private File mibFile ;

	/**
	 * ファイルリーダー
	 */
	private BufferedReader mibReader ;	
	
	/**
	 * ファイル読み込み行バッファ
	 */
	private String readLineBuff = null;

	/**
	 * ファイル読み込み行カウンタ（語句の開始位置用)
	 */
	private int readLineCount = 0 ;

	/**
	 * ファイル読み込み列カウンタ（語句の開始位置用)
	 */
	private int readColsCount = 0 ;

	/**
	 * ファイル読み込み行スキャナー
	 */
	private MibLineScaner lineScaner = null;

	/**
	 * スキャンバッファ（読み取りした語句のバッファ）
	 */
	private StringBuilder scanBuf ;

	/**
	 * 語句種別判定バッファ（読み取りした語句のバッファ）
	 */
	private  MibTokenUtil.Kind tokenKindBuf ;

	/**
	 * 読み飛ばし コメントバッファ（最終の語句からENDまで間のコメント取得に対応するためのバッファ）
	 */
	private StringBuilder skipLineComment ;

	/**
	 * コンストラクタ(非公開)
	 * 
	 * @param mibLoaderLog ログ管理
	 * @param mibFile MIBファイル
	 * @param mibFileReader MIBファイルリーダー
	*/
	private MibTokenizer(MibLoaderLog mibLoaderLog ,File mibFile, BufferedReader mibFileReader ){
		this.mibLoaderLog = mibLoaderLog;
		this.mibFile = mibFile;
		this.mibReader = mibFileReader;
	}

	/**
	 * ファクトリメソッド
	 * 
	 * @param mibLoaderLog ログ管理
	 * @param mibFile MIBファイル
	*/
	public static MibTokenizer factoryMethod(MibLoaderLog mibLoaderLog ,File mibFile ) throws MibLoaderException{
		try {
			//ファイルが通常パスか確認
			if(mibFile.exists()){
				//通常パスならインスタンス生成
				FileReader mibFileReader = new FileReader(mibFile);
				BufferedReader mibReader = new BufferedReader(mibFileReader);
				return new MibTokenizer(mibLoaderLog,mibFile,mibReader);
			}
			
			//リソースパス(jar内 もしくは src内）か確認
			String parentPath = mibFile.getParent();
			if( parentPath.startsWith(MibLoader.resourceDir ) ){
				String path =parentPath.replace("\\", "/")  +"/"+ mibFile.getName();//リソースパス向けに区切り文字の補正
				InputStream is = MibLoader.class.getClassLoader().getResourceAsStream(path);
				if(is != null){
					//ファイルがリソースパスに存在する場合、インスタンス生成
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					return new MibTokenizer(mibLoaderLog,mibFile,br);
				}
			}
			//どちらのパスでもない場合、パス不正として扱う
			String message = "Io error . On file Opening . Path is NotFound:"+ mibFile.getPath();
			mibLoaderLog.addEntry(mibFile, 0, 0, message);
			throw new MibLoaderException(mibLoaderLog);
		}catch (FileNotFoundException e){
			String message = "Io error . On file Opening . FileNotFoundException:"+ e.getMessage();
			mibLoaderLog.addEntry(mibFile, 0, 0, message);
			throw new MibLoaderException(mibLoaderLog);
		}
	 } 

	/**
	 * MIBファイル取得
	 * 
	 * @return MIBファイル
	*/
	public File getFile() {
		return this.mibFile;
	}

	/**
	 * 字句取得
	 * 
	 * @return 字句インスタンス
	 * @throws MibLoaderException 
	*/
	public MibToken getNextToken() throws MibLoaderException{


		tokenKindBuf =  MibTokenUtil.Kind.STRING;
		scanBuf = new StringBuilder();

		//語句の頭だし
		if(getTokenHead()==false){
			return null;
		}

		//先頭がダブルクォートなら次のダブルクォートまで改行含めて一括読み込み
		if(lineScaner.peek() == MibTokenUtil.D_QUOTE ){
			lineScaner.next();
			int startLine = readLineCount;
			int startColsCount =lineScaner.getIndex();
			if(getDoubleQuoteEnclose()==false){
				//失敗した場合は構文エラーを投げる
				String message = "Syntax error. There is problem in corresponding quote('\"').";
				mibLoaderLog.addEntry(mibFile, startLine, startColsCount, message);
				throw new MibLoaderException(mibLoaderLog);
			}
		//それ以外の場合は語句の切れ目まで連続読み出し
		}else{
			if(getNormalToken()==false){
				//失敗した場合はロードエラーを投げる
				throw new MibLoaderException(mibLoaderLog);
			}
		}
		//結果を元に語句インスタンスを生成
		MibToken result = new MibToken(scanBuf.toString(), readLineCount, readColsCount, tokenKindBuf);
		
		return result;
	}

	/**
	 * クローズ処理
	 * @throws IOException 
	 * 
	*/
	public void close() throws MibLoaderException {
		try{
			mibReader.close();
		}catch(IOException e){
			String message = "Io error. On file closing .IOExcepiton:"+ e.getMessage();
			mibLoaderLog.addEntry(mibFile, readLineCount, 0, message);
			throw new MibLoaderException(mibLoaderLog);
		}
	}

	/**
	 * ファイルの最初から有効字句先頭までの間にあるコメントを取得する。（これをヘッダーコメントという）
	 * 
	 * ファイル先頭かどうかについては別途に判定されてから本メソッドを呼ばれる前提
	 * 
	 * @return ヘッダーコメント文字
	 * @throws MibLoaderException 
	*/
	public String getHeader() throws MibLoaderException{

		StringBuilder headerBuffer = new StringBuilder();
		//読み飛ばし対象となる空行から コメント部分のみを抽出
		while( lineScaner == null  || ( ! lineScaner.hasNext() )){
			if(getNextLine(true)==false){
				return null;
			}
			lineScaner.cutWhitespace();
			if( ( ! lineScaner.hasNext() ) && readLineBuff.contains(  MibTokenUtil.COMMENT_START ) ){
				//改行
				//if( headerBuffer.length() > 0 ){
				//	headerBuffer.append("\n");
				//}
				headerBuffer.append(MibLineScaner.getComment(readLineBuff));
			}
			
		}
		readColsCount = lineScaner.getIndex();

		if( headerBuffer.length() > 0 ){
			headerBuffer.append("\n");
			return headerBuffer.toString();
		}else{
			return "";
		}
		
	}

	/**
	 * 字句を取得するまでに読み飛ばされたコメントを取得する
	 * 
	 * @return コメント
	 * @throws MibLoaderException 
	*/
	public String getSkipComment() throws MibLoaderException{
		//読み飛ばし対象となる空行から コメント部分のみを抽出
		if( skipLineComment!= null ){
			return skipLineComment.toString();
		}else{
			return "";
		}
	}

	/**
	 * 定義末尾（END）ファイル終端までの間にあるコメントを取得する。（これをフッターコメントという）
	 * 
	 * 定義末尾の到達については別途に判定されから本メソッドが呼ばれる前提である。
	 * 
	 * @return フッターコメント文字
	 * @throws MibLoaderException 
	*/
	public String getFooter() throws MibLoaderException{

		StringBuilder footerBuffer = new StringBuilder();
		//読み飛ばし対象となる空行から コメント部分のみを抽出
		while( lineScaner == null  || ( ! lineScaner.hasNext() )){
			if(getNextLine(true)==false){
				return  footerBuffer.toString();
			}
			lineScaner.cutWhitespace();
			if( ( ! lineScaner.hasNext() ) && readLineBuff.contains(MibTokenUtil.COMMENT_START ) ){
				footerBuffer.append(MibLineScaner.getComment(readLineBuff));
				footerBuffer.append("\n");
			}
		}
		return footerBuffer.toString();
	}

	
	/**
	 * 語句の種別を取得
	 *  
	 * @return 語句の種別
	 */
	private MibTokenUtil.Kind getTokenKind(String target)throws MibLoaderException {
		try{
			new BigInteger(target);
			return MibTokenUtil.Kind.NUMBER;
		}catch(NumberFormatException e){
			return MibTokenUtil.Kind.STRING;
		}
	}

	/**
	 * 語句の頭だし
	 *  
	 * @return 取得の成否
	 */
	private boolean getTokenHead()throws MibLoaderException{

		skipLineComment = null;
		//途中処理ならスペースは削除しておく
		if(lineScaner != null ){ 
			 lineScaner.cutWhitespace();
		}

		//空行は読み飛ばし
		while( lineScaner == null  || ( ! lineScaner.hasNext() )){
			if(getNextLine(true)==false){
				return false;
			}
			//文中コメント取得向け対応（字句間にあるコメントをバッファ）
			if( readLineBuff.contains(  MibTokenUtil.COMMENT_START ) ){
				if(skipLineComment==null){
					skipLineComment = new StringBuilder();
				}
				skipLineComment.append("\n");
				skipLineComment.append(MibLineScaner.getComment(readLineBuff));
			}
			lineScaner.cutWhitespace();
		}
		readColsCount = lineScaner.getIndex();
		
		return true;
	}

	/**
	 * ダブルクォートの囲い込み取得
	 *  この語句のみ連続ブランクを許容し改行も含むため、個別対応
	 *  
	 * @return 取得の成否
	 */
	private boolean getDoubleQuoteEnclose()throws MibLoaderException{
		//コメント含めた読み込みへの対応(コメント削除とりけし)
		lineScaner = new MibLineScaner(readLineBuff,lineScaner.getIndex(),false) ;
		
		//読み取り続行フラグ
		boolean continueFlag = true;
		
		while( continueFlag){
			continueFlag = false;
			if( ! lineScaner.hasNext() ){
				if(getNextLine(false)==false){
					//最終的に'"'が取得できなければ構文エラー(エラーメッセージは呼びだし元でも設定)
					String message = "Syntax error. There is no corresponding quote('\"').";
					mibLoaderLog.addEntry(mibFile, readLineCount, 0, message);
					return false;
				}
				//改行も語句に取り込む
				scanBuf.append("\n");
				//先頭のインデントは無視
				lineScaner.cutWhitespace();
				continueFlag = true;
				continue;
			}
			// " が現れるまで どんどん読み込む
			char nextChar = lineScaner.next();
			if(nextChar != MibTokenUtil.D_QUOTE ){
				scanBuf.append(nextChar);
				//必要なら次の行を読み込み
				continueFlag = true;
			}else{
				//コメント無視の読み込みに戻す
				lineScaner = new MibLineScaner(readLineBuff,lineScaner.getIndex(),true) ;
				// '"'の直後は ホワイトスペース か 改行 か 分割記号 でないならエラー
				if( lineScaner.hasNext() == false ){
					break;
				}
				if(Character.isWhitespace(lineScaner.peek())== true ){
					break;
				}
				if(MibTokenUtil.isSingleDelimiter(lineScaner.peek())== true ){
					break;
				}
				String message = "Syntax error. It is incorrect after the corresponding quote('\"').";
				mibLoaderLog.addEntry(mibFile, readLineCount, 0, message);
				return false;
			}
		}
		tokenKindBuf =  MibTokenUtil.Kind.DQ_ENCLOSE;
		return true;
	}

	/**
	 * 語句の取得
	 *  次の改行かホワイトスペースまでの文字を取得
	 *  
	 *  読取語句はscanBufにセット
	 *  
	 * @return 取得の成否
	 * @throws MibLoaderException 
	 */
	private boolean getNormalToken() throws MibLoaderException{
		//読み取り続行フラグ
		boolean continueFlag = true;

		//分割記号（１文字）なら単発で語句化
		if(MibTokenUtil.isSingleDelimiter(lineScaner.peek())){
			scanBuf.append(lineScaner.next());
			tokenKindBuf= MibTokenUtil.Kind.MARK;
			return true;
		}
		//分割記号（３文字）チェック
		if( MibTokenUtil.isDelimiterStart(lineScaner.peek()) == true &&  MibTokenUtil.COLON_EQUALS.equals(lineScaner.peek3length()) ){
			scanBuf.append(lineScaner.next());
			scanBuf.append(lineScaner.next());
			scanBuf.append(lineScaner.next());
			tokenKindBuf= MibTokenUtil.Kind.MARK;
			return true;
		}
		
		// ホワイトスペース、分割記号（::=含む）、 のいずれかまで どんどん読み込む
		while( continueFlag){
			continueFlag = false;
			char nowtPeek= lineScaner.peek();
			//次がホワイトスペースかチェック
			if( Character.isWhitespace(nowtPeek) == true ){
					break;
			}
			//次が分割記号（１文字）かチェック
			if( MibTokenUtil.isSingleDelimiter(nowtPeek) == true ){
				break;
			}
			//次が分割記号（３文字）かチェック
			if( MibTokenUtil.isDelimiterStart(lineScaner.peek()) == true &&  MibTokenUtil.COLON_EQUALS.equals(lineScaner.peek3length()) ){
				break;
			}
			scanBuf.append(lineScaner.next());
			if( lineScaner.hasNext() ){
				//ここまでの読み込みが分割記号（３文字）かチェック	
				if( scanBuf.length() == 3 && MibTokenUtil.COLON_EQUALS.equals(scanBuf.toString()) ){
					break;
				}
				continueFlag = true;
			}
		}
		
		//語句の種別を判定
		tokenKindBuf = getTokenKind(scanBuf.toString());
		return true;
	}

	
	/**
	 * ファイル内の次行取得
	 *  
	 * @return 取得の成否
	 */
	private boolean getNextLine(boolean commentCut)throws MibLoaderException{
		try{
			readLineBuff = mibReader.readLine();
			if(readLineBuff==null){
				return false;
			}
			readLineCount = readLineCount + 1;
			lineScaner = new MibLineScaner(readLineBuff,commentCut) ;
		}catch(IOException e){
			String message = "Io error. On file reading .IOExcepiton:"+ e.getMessage();
			mibLoaderLog.addEntry(mibFile, readLineCount, 0, message);
			throw new MibLoaderException(mibLoaderLog);
		}
		return true;
	}

}
