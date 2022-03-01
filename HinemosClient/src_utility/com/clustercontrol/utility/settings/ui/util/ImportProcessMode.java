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
public class ImportProcessMode {
	private static ImportProcessMode instance = SingletonUtil.getSessionInstance(ImportProcessMode.class);
	
	private boolean sameProcess = false;
	private boolean sameObjectPrivilege = false;
	private String xmlObjectPrivilege = "";
	private int processType = 0;
	private Integer importUnitNum = null;
	private boolean isRollbackIfAbnormal = false;
	private boolean sameRollbackIfAbnormal = false;
	private boolean isCancelForAbend = false;
	private boolean sameCancelForAbend = false;
	
	private ImportProcessMode() {

	}

	public static synchronized ImportProcessMode getInstance() {    
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
	
	public static boolean isSameObjectPrivilege() {
		return getInstance().sameObjectPrivilege;
	}

	public static void setObjectPrivilege(boolean sameObjectPrivilege) {
		getInstance().sameObjectPrivilege = sameObjectPrivilege;
	}

	public static String getXmlObjectPrivilege() {
		return getInstance().xmlObjectPrivilege;
	}

	public static void setXmlObjectPrivilege(String xmlObjectPrivilege) {
		getInstance().xmlObjectPrivilege = xmlObjectPrivilege;
	}
	
	public static Integer getImportUnitNum() {
		return getInstance().importUnitNum;
	}

	public static void setImportUnitNum(Integer importUnitNum) {
		getInstance().importUnitNum = importUnitNum;
	}
	
	public static boolean isRollbackIfAbnormal() {
//		return getInstance().isRollbackIfAbnormal;
		// TODO 6612対応 7.0.1リリース時には一時変更を戻す
		return false;
	}

	public static void setRollbackIfAbnormal(boolean isRollbackIfAbnormal) {
		getInstance().isRollbackIfAbnormal = isRollbackIfAbnormal;
	}
	
	public static boolean isSameRollbackIfAbnormal() {
		return getInstance().sameRollbackIfAbnormal;
	}

	public static void setSameRollbackIfAbnormal(boolean sameRollbackIfAbnormal) {
		getInstance().sameRollbackIfAbnormal = sameRollbackIfAbnormal;
	}

	public static boolean isCancelForAbend() {
		return getInstance().isCancelForAbend;
	}

	public static void setCancelForAbend(boolean isCancelForAbend) {
		getInstance().isCancelForAbend = isCancelForAbend;
	}
	
	public static boolean isSameCancelForAbend() {
		return getInstance().sameCancelForAbend;
	}

	public static void setSameCancelForAbend(boolean sameCancelForAbend) {
		getInstance().sameCancelForAbend = sameCancelForAbend;
	}

	
}