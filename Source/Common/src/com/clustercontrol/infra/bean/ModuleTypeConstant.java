/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.bean;


/**
 * 環境構築機能で使用するモジュールのタイプを定数として格納するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ModuleTypeConstant {
	/** コマンドモジュール */
	public static final int TYPE_COMMAND = 0;
	/** ファイル配布モジュール */
	public static final int TYPE_FILETRANSFER = 1;
	/** 参照環境構築モジュール */
	public static final int TYPE_REFERMANAGEMENT = 2;
	
	public static int classToType(String className){
		if(className.equals("")){
			
		} else if (className.equals("")){
			
		}
		return -1;
	}
}
