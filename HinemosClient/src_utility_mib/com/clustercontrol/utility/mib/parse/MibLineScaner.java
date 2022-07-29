/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MIBのパース処理(Trap関連情報取得)における
 * 定義文から字句解析時における行文字列の１文字読出用クラスです
 * 
 * 連続するスペースの削除やコメント定義の払い落としの実装も扱います。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class MibLineScaner {

	
	/**
	 * コメント以前の削除用パターン 
	 */
	private static final Pattern commetBeforePattern = Pattern.compile("[^-]*--\\s*");
	
	
	/**
	 * 読み取る行文字列。必要に応じてコメント部分を削除しておく。 
	 */
	private String line = null;

	/**
	 * 現在位置（文字数）
	 */
	private int current;

	/**
	 * 最大文字数
	 */
	private int maxIndex;

	/**
	 * コンストラクタ
	 * 
	 * @param line 行文字列
	 * @param isCommentPurge コメント削除をあらかじめ行うか？
	 */
	public MibLineScaner(String line , boolean isCommentPurge ) {
		init(line,0,isCommentPurge);
	}

	/**
	 * コンストラクタ（作り直し用）
	 * 
	 * @param line 行文字列
	 * @param current 読み込み済みの文字位置
	 * @param isCommentPurge コメント削除をあらかじめ行うか？
	 */
	public MibLineScaner(String line , int current , boolean isCommentPurge ) {
		init(line,current,isCommentPurge);
	}
	
	private void init(String line ,int current, boolean isCommentPurge ){
		this.line = line;
		this.current = current;
		if (line != null){
			if( isCommentPurge ){
				purgeComment(current);
			}
			maxIndex = this.line.length();
		}
	}

	/**
	 * 現在文字の取得して次へ
	 * @return １文字
	 */
	public char next() {
		if (!hasNext()) { return (char)-1; }
		return line.charAt(current++);
	}

	/**
	 * 現在文字の取得
	 * @return １文字
	 */
	public char peek() {
		if (!hasNext()) { return (char)-1; }
		return line.charAt(current);
	}

	/**
	 * 現在地点含めた３文字を先読み取得
	 * 
	 * @return ３文字(３文字に満たない場合 ブランク)
	 */
	public String peek3length() {
		if( (maxIndex - current < 3) ){
			return "";
		}
		return line.substring(current, current + 3 ) ;
	}

	/**
	 * 現在位置の取得
	 * @return 現在位置
	 */
	public int getIndex() {
		return current;
	}
	
	/**
	 * 次の文字の有無
	 * @return 次の文字の有無
	 */
	public boolean hasNext() {
		return (line != null) && (maxIndex != current);
	}

	/**
	 * 連続するブランクの削除
	 */
	public void cutWhitespace() {
		char ch = peek();
		while (Character.isWhitespace(ch)) {
			next();
			ch = peek();
		}
	}

	/**
	 * コメントの除去
	 */
	private void purgeComment(int startIndex) {
		//Matcher commentMatcher = commentAfterPattern.matcher(line);
		//this.line = commentMatcher.replaceFirst("");
		boolean doubleQouteEncFlag = false;
		for( int cnt = startIndex ; cnt < line.length()  ; cnt++ ){
			//  " にて囲われているかの状態判定
			if(line.charAt(cnt) == MibTokenUtil.D_QUOTE){
				if(doubleQouteEncFlag){
					doubleQouteEncFlag=false;
				}else{
					doubleQouteEncFlag=true;
				}
			}
			// " にて囲われた -- は無視
			if(doubleQouteEncFlag==true){
				continue;
			}
			// --がでたらそこで打ち切り
			if( cnt >= 1 && line.charAt(cnt) == '-' && line.charAt(cnt-1) == '-'){
				if( cnt == 1 ){
					this.line = "";
				}else{
					this.line = line.substring( 0 , cnt - 1 );
				}
				return;
			}
		}
		return;
	}
	/**
	 * コメント内容取得
	 */
	public static String getComment(String targetIine) {
		Matcher commentMatcher = commetBeforePattern.matcher(targetIine);
		return commentMatcher.replaceFirst("");
	}
} 