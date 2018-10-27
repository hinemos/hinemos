/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib;

import com.clustercontrol.utility.mib.MibLoaderLog;

/**
 * MIBのパース処理(Trap関連情報取得)におけるMibLoaderでの処理にて発生するExceptionです。
 * 対象となるMIBファイルの定義内容に問題がある場合に発生します。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class MibLoaderException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5826812856833130587L;
	/**
	 * MILoader処理におけるログ
	 */
	private MibLoaderLog loaderLog ;
	
	
	/**
	 * コンストラクタ
	 * 
	 * @param loaderLog ログ
	*/
	public MibLoaderException(MibLoaderLog loaderLog) {
		super();
		this.loaderLog = loaderLog;
	}
	
	/**
	 * 処理ログを返します
	 * 
	 * @return 処理ログ
	*/
	public MibLoaderLog getLog() {
		return loaderLog;
	}

}


