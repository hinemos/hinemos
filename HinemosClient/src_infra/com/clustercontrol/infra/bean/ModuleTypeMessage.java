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

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.util.Messages;

/**
 * 環境構築機能で使用するモジュールのタイプを定数として格納するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ModuleTypeMessage {
	/** コマンドモジュール */
	public static final String STRING_COMMAND = Messages.getString("infra.command.module");
	/** ファイル配布モジュール */
	public static final String STRING_FILETRANSFER = Messages.getString("infra.file.transfer.module");
	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == ModuleTypeConstant.TYPE_COMMAND) {
			return STRING_COMMAND;
		} else if (type ==  ModuleTypeConstant.TYPE_FILETRANSFER) {
			return STRING_FILETRANSFER;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_COMMAND)) {
			return  ModuleTypeConstant.TYPE_COMMAND;
		} else if (string.equals(STRING_FILETRANSFER)) {
			return  ModuleTypeConstant.TYPE_FILETRANSFER;
		}
		return -1;
	}
	
	public static List<String> getAllStrings(){
		List<String> strings = new ArrayList<String>();
		
		strings.add(STRING_COMMAND);
		strings.add(STRING_FILETRANSFER);
		
		return strings;
	}
}
