/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.util;

import org.eclipse.rap.rwt.SingletonUtil;

/**
 * インポート時の処理方法を保持するシングルトンクラス
 * 
 * @version 6.1.0
 * @since 2.2.0
 * 
 */
public class DeleteProcessMode {
	private static DeleteProcessMode instance = SingletonUtil.getSessionInstance(DeleteProcessMode.class);
	
	private boolean sameProcess = false;
	private int processType = 0;
	
	private DeleteProcessMode() {

	}

	public static synchronized DeleteProcessMode getInstance() {    
		return instance;
	}

	public static boolean isSameprocess() {
		return getInstance().sameProcess;
	}

	public static void setSameprocess(boolean sameprocess) {
		getInstance().sameProcess = sameprocess;
	}

	public static int getProcesstype() {
		return getInstance().processType;
	}

	public static void setProcesstype(int processtype) {
		getInstance().processType = processtype;
	}
}