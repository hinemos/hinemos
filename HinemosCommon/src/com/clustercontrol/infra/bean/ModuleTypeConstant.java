/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

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
	
	public static int classToType(String className){
		if(className.equals("")){
			
		} else if (className.equals("")){
			
		}
		return -1;
	}
}
