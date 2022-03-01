/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.bean;

import java.util.ArrayList;
import java.util.List;

import org.openapitools.client.model.ModuleResultResponse.ModuleTypeEnum;

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
	/** 参照環境構築モジュール */
	public static final String STRING_REFERMANAGEMENT = Messages.getString("infra.refer.management.module");
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
		} else if (type == ModuleTypeConstant.TYPE_REFERMANAGEMENT) {
			return STRING_REFERMANAGEMENT;
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
		} else if (string.equals(STRING_REFERMANAGEMENT)) {
			return ModuleTypeConstant.TYPE_REFERMANAGEMENT;
		}
		return -1;
	}
	
	public static List<String> getAllStrings(){
		List<String> strings = new ArrayList<String>();
		
		strings.add(STRING_COMMAND);
		strings.add(STRING_FILETRANSFER);
		strings.add(STRING_REFERMANAGEMENT);
		
		return strings;
	}
	
	public static String enumToString(ModuleTypeEnum type) {
		if (type == ModuleTypeEnum.COMMAND) {
			return STRING_COMMAND;
		} else if (type ==  ModuleTypeEnum.FILETRANSFER) {
			return STRING_FILETRANSFER;
		} else if (type == ModuleTypeEnum.REFERMANAGEMENT) {
			return STRING_REFERMANAGEMENT;
		}
		return "";
	}
}
