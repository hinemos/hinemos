/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib.parse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MIBのパース処理(Trap関連情報取得)における
 * 定義文から字句解析により切り出された字句の扱いに関するUtilityクラス。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class MibTokenUtil {
	
	
	/**
	 * OIDにおける 連結用ID定義の表現パターン. 該当例: name(999) , chain1(1)
	 */
	private static final Pattern chainIdExpressionPattern = Pattern.compile("[0-9a-zA-Z]+\\([0-9]+\\)");
	
	/**
	 * 字句種別設定値
	 */
	public enum Kind {
			NUMBER //数値
		,	STRING //文字
		,	MARK //文法記号
		,	DQ_ENCLOSE // '"'での囲い込まれた文字列
	}

	/**
	 * 予約語 DEFINITIONS. MIB定義を全体を表すキーワード
	 */
	public static final String DEFINITIONS ="DEFINITIONS";

	/**
	 * 予約語 MACRO
	 */
	public static final String MACRO ="MACRO";

	/**
	 * 予約語 BEGIN. MIB定義の開始キーワード
	 */
	public static final String BEGIN ="BEGIN";

	/**
	 * 予約語 END. MIB定義の終了
	 */
	public static final String END ="END";

	/**
	 * 予約語 IMPORT. MIB定義内で他の定義を引用する際のキーワード. FROMとの併用必須。
	 */
	public static final String IMPORTS ="IMPORTS";

	/**
	 * 予約語 EXPORT. MIB定義内で他の定義への参照向けを表すキーワード. 本件では基本的に考慮外 
	 */
	public static final String EXPORTS ="EXPORTS";

	/**
	 * 予約語 OBJECT-TYPE. 名称に対するOIDの割付と併用されるキーワード。
	 */
	public static final String OBJECT_TYPE ="OBJECT-TYPE";

	/**
	 * 予約語 TRAP-TYPE
	 */
	public static final String TRAP_TYPE ="TRAP-TYPE";

	/**
	 * 予約語 NOTIFICATION-TYPE
	 */
	public static final String NOTIFICATION_TYPE ="NOTIFICATION-TYPE";

	/**
	 * 予約語 OBJECT. 名称に対するOIDの割付と併用されるキーワード。
	 */
	public static final String OBJECT ="OBJECT";

	/**
	 * 予約語 IDENTIFIER. 名称に対するOIDの割付と併用されるキーワード。
	 */
	public static final String IDENTIFIER ="IDENTIFIER";

	/**
	 * 予約語 OBJECT-GROUP（値割付）. 名称に対するOIDの割付と併用されるキーワード。
	 */
	public static final String OBJECT_GROUP ="OBJECT-GROUP";

	/**
	 * 予約語 NOTIFICATION-GROUP（値割付）. 名称に対するOIDの割付と併用されるキーワード。
	 */
	public static final String NOTIFICATION_GROUP ="NOTIFICATION-GROUP";
	
	/**
	 * 予約語 MODULE-COMPLIANCE （型割付）.SMI-v2判断に利用
	 */
	public static final String MODULE_COMPLIANCE ="MODULE-COMPLIANCE";
	
	/**
	 * 予約語 AGENT-CAPABILITIES（値割付）. 名称に対するOIDの割付と併用されるキーワード。
	 */
	public static final String AGENT_CAPABILITIES ="AGENT-CAPABILITIES";
	
	/**
	 * 予約語 MODULE-IDENTITY（値割付）. 名称に対するOIDの割付と併用されるキーワード。
	 */
	public static final String MODULE_IDENTITY ="MODULE-IDENTITY";
	
	/**
	 * 予約語 OBJECT-IDENTITY（値割付）. 名称に対するOIDの割付と併用されるキーワード。
	 */
	public static final String OBJECT_IDENTITY ="OBJECT-IDENTITY";
	
	/**
	 * 予約語 TEXTUAL-CONVENTION（型割付）.SMI-v2判断に利用
	 */
	public static final String TEXTUAL_CONVENTION ="TEXTUAL-CONVENTION";

	/**
	 * 予約語 FROM . MIB定義内で他の定義を引用する際のキーワード. 対象のMIBとその中の値や型の名称を指定
	 */
	public static final String FROM ="FROM";

	/**
	 * 予約語 OBJECTS
	 */
	public static final String OBJECTS ="OBJECTS";

	/**
	 * 予約語 VARIABLES
	 */
	public static final String VARIABLES ="VARIABLES";
	
	/**
	 * 予約語 DESCRIPTION
	 */
	public static final String DESCRIPTION ="DESCRIPTION";

	/**
	 * 予約語 REFERENCE
	 */
	public static final String REFERENCE ="REFERENCE";

	/**
	 * 予約語 STATUS
	 */
	public static final String STATUS ="STATUS";


	/**
	 * 予約語 ENTERPRISE
	 */
	public static final String ENTERPRISE ="ENTERPRISE";
	
	
	/**
	 * 識別子 セミコロン
	 */
	public static final String SEMICOLON =";";

	/**
	 * 識別子 カンマ
	 */
	public static final String COMMA =",";

	/**
	 * 識別子 割付
	 */
	public static final String COLON_EQUALS ="::=";

	/**
	 * 識別子 左波カッコ
	 */
	public static final String LEFT_CURLY_PART ="{";

	/**
	 * 識別子 右波カッコ
	 */
	public static final String RIGHT_CURLY_PART ="}";

	/**
	 * 識別子 左波カッコ
	 */
	public static final String LEFT_ROUND_PART ="(";

	/**
	 * 識別子 右丸カッコ
	 */
	public static final String RIGHT_ROUND_PART =")";

	/**
	 * 識別子 コメント開始
	 */
	public static final String COMMENT_START ="--";

	/**
	 * 文字 ダブルクォート
	 */
	public static final Character D_QUOTE ='"';


	/**
	 * 構文解析対象となる予約語
	 */
	private static final HashSet<String> analyzeTarget = new HashSet<>(Arrays.asList(
			OBJECT_TYPE
		,	TRAP_TYPE
		,	NOTIFICATION_TYPE
		,	IDENTIFIER
		,	MODULE_IDENTITY
		,	OBJECT_GROUP
		,	OBJECT_IDENTITY
		,	AGENT_CAPABILITIES
	));

	/**
	 * SMI-V2判定対象となる予約語
	 */
	private static final HashSet<String> smiv2Target = new HashSet<>(Arrays.asList(
			NOTIFICATION_TYPE
		,	MODULE_COMPLIANCE
		,	MODULE_IDENTITY
		,	OBJECT_GROUP
		,	OBJECT_IDENTITY
		,	AGENT_CAPABILITIES
		,	TEXTUAL_CONVENTION
	));

	/**
	 * コンストラクタ
	 * 
	*/
	public MibTokenUtil() {
		
	}
	
	/**
	 * 分割記号（１文字）かどうかを確認
	 * 
	 * @param target 文字
	 * @return 確認結果（真偽）
	*/
	public static boolean isSingleDelimiter( char target){
		if( target == ',' ){
			return true;
		}
		if( target == ';' ){
			return true;
		}
		if( target == '{' ){
			return true;
		}
		if( target == '}' ){
			return true;
		}
		if( target == '(' ){
			return true;
		}
		if( target == ')' ){
			return true;
		}
		return false;
	}

	/**
	 * 分割記号（複数字記号の先頭）かどうかを確認
	 * 
	 * @param target 文字
	 * @return 確認結果（真偽）
	*/
	public static boolean isDelimiterStart( char target){
		if( target == ':' ){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 連結ID表現(名称式つき)か確認
	 *
	 * 該当字句が連結ID表現[ name(1)の形式 ]か確認
	 * 
	 * @return 連結ID
	 */
	public static boolean isChainIdExpression( MibToken token ){
    	Matcher matcher = chainIdExpressionPattern.matcher(token.getTokenString());
    	return matcher.matches();
	}
	
	/**
	 * 構文解析の対象となる予約語句かどうかを確認
	 * 
	 * @param target 対象語句
	 * @return 確認結果（真偽）
	*/
	public static boolean isAnalyzeTarget( String target){
		return analyzeTarget.contains(target);
	}
	
	/**
	 * SMI-V2の対象となる予約語句かどうかを確認
	 * 
	 * @param target 対象語句
	 * @return 確認結果（真偽）
	*/
	public static boolean isSmiv2Target( String target){
		return smiv2Target.contains(target);
	}
	
	/**
	 * 文字列の末尾が . かどうかチェック
	 * 
	 * @param target 対象語句
	 * @return 確認結果（真偽）
	*/
	public static boolean isLastPeriod( String target){
		if(target.length() > 0 ){
			if(target.charAt(target.length()-1) == '.'){
				return true;
			}
		}
    	return false;
	}
	/**
	 * 文字列の末尾が 改行 かどうかチェック
	 * 
	 * @param target 対象語句
	 * @return 確認結果（真偽）
	*/
	public static boolean isLastLineField( String target){
		if(target.length() > 0 ){
			if(target.charAt(target.length()-1) == '\n'){
				return true;
			}
		}
    	return false;
	}
}
