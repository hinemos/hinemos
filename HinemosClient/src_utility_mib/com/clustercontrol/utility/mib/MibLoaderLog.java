/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * MIBのパース処理(Trap関連情報取得)におけるMibLoaderでの処理のログ管理クラスです。
 * MIBファイルに構文解析に関連するログを管理します。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class MibLoaderLog {
	
	private static Log log = LogFactory.getLog(MibLoaderLog.class);
	/**
	 * 登録ログの一覧です。
	 * 
	*/
	private ArrayList<LogEntry> logEntryAry	 = new ArrayList<LogEntry>();
	
	/**
	 * コンストラクタ
	 * 
	*/
	public MibLoaderLog() {
		//処理なし
	}
	
	/**
	 * ログ追加
	 * 
		 * @param mibFile	 MIBファイル
		 * @param lineNumber 行番号
		 * @param colsNumber 行先頭から文字数
		 * @param message	 メッセージ
	*/
	public void addEntry( File mibFile , int lineNumber , int colsNumber , String message ) {
		logEntryAry.add(new LogEntry(mibFile , lineNumber, colsNumber ,message ));
		if(log.isDebugEnabled()){
			log.debug(mibFile.getName() +" at "+ lineNumber + " : " + message);
		}
	}
	
	/**
	 * 登録されたログのイテレータを返します。
	 * 
	 * @return ログのイテレータ
	*/
	public Iterator<LogEntry> entries() {
		return logEntryAry.iterator();
	}
	
	/**
	 * ログ登録を表すクラスです。
	 *
	 * @version 6.1.a
	 * @since 6.1.a
	 */
	public static class LogEntry {
		
		/**
		 * MIBファイル
		 * 
		*/
		private File mibFile ;
		
		/**
		 * 行番号
		 * 
		*/
		private int lineNumber ;
		
		/**
		 * 行先頭から文字数
		 * 
		*/
		private int colsNumber ;
		/**
		 * メッセージ
		 * 
		*/
		private String message = "";
		
		/**
		 * コンストラクタ
		 * 
		 * @param mibFile	 MIBファイル
		 * @param lineNumber 行番号
		 * @param colsNumber 行先頭から文字数
		 * @param message	 メッセージ
		*/
		public LogEntry( File mibFile , int lineNumber , int colsNumber , String message ) {
			this.mibFile = mibFile;
			this.lineNumber = lineNumber;
			this.colsNumber = colsNumber;
			this.message = message;
		}

		/**
		 * 行番号取得
		 * 
		 * @return 行番号
		*/
		public int getLineNumber() {
			return lineNumber;
		}

		/**
		 * 行先頭から文字数取得
		 * 
		 * @return 行先頭から文字数
		*/
		public int getColsNumber() {
			return colsNumber;
		}

		/**
		 * メッセージ取得
		 * 
		 * @return メッセージ
		*/
		public String getMessage() {
			return message;
		}
		
		/**
		 *	MIBファイル取得
		 * 
		 * @return	MIBファイル
		*/
		public File getFile() {
			return mibFile;
		}
		
	}
}


