/*

Copyright (C) 2009 NTT DATA Corporation

This program is free software;
			you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY;
			without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.accesscontrol.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.SystemPrivilegeInfo;

/**
 * システム権限の表示文字列とシステム権限テーブル値を保持するユーティリティクラスです。
 * 
 * @version 4.0.0
 */
public class SystemPrivilegePropertyUtil {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(SystemPrivilegePropertyUtil.class);

	/** システム権限マップ（key=マネージャ名、value=システム権限マップ（key=表示文字列、value=システム権限）） */
	private static Map<String, Map<String, SystemPrivilegeInfo>> m_systemPrivilegeAllMngMap
		= new ConcurrentHashMap<>();
	
	/** メッセージ情報 */
	private static Map<String, String> m_messageMap = new HashMap<>();

	/** メッセージキー */
	private static final String KEY_SYSTEM_PRIVILEGE_FUNCTION_PREFIX = "system_privilege.function.";
	private static final String KEY_SYSTEM_PRIVILEGE_PRIVILEGE_PREFIX = "system_privilege.privilege.";

	private static void createSystemPrivilegeMap(String managerName){
		if (managerName != null && managerName.length() > 0) {
			Map<String, SystemPrivilegeInfo> systemPrivilegeMap = m_systemPrivilegeAllMngMap.get(managerName);
			if (systemPrivilegeMap == null) {
				systemPrivilegeMap = new ConcurrentHashMap<>();
				List<SystemPrivilegeInfo> systemPrivilegeInfoList = new ArrayList<>();

				// システム権限取得
				try {
					AccessEndpointWrapper wrapper = AccessEndpointWrapper.getWrapper(managerName);
					systemPrivilegeInfoList = wrapper.getSystemPrivilegeInfoListByEditType(
							PrivilegeConstant.SYSTEMPRIVILEGE_EDITTYPE_DIALOG);
				}
				catch (InvalidRole_Exception e) {
					// 権限なし
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));

				} catch (Exception e) {
					// 上記以外の例外
					m_log.warn("getOwnUserList(), " + HinemosMessage.replace(e.getMessage()), e);
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				}

				// システム権限をマップに設定
				for (SystemPrivilegeInfo systemPrivilegeInfo : systemPrivilegeInfoList) {
					systemPrivilegeMap.put(getSystemPrivilegeMessage(systemPrivilegeInfo), systemPrivilegeInfo);
				}

				// マップに設定
				m_systemPrivilegeAllMngMap.put(managerName, systemPrivilegeMap);
			}
		}
		return;
	}

	/**
	 * 権限ラベルの取得
	 * @param managerName マネージャ名
	 * @return Messages.getString("repository.read", locale) の形のリスト
	 */
	public static List<String> getSystemPrivilegeNameList(String managerName) {
		createSystemPrivilegeMap(managerName);
		List<String> list = new ArrayList<>();
		if (m_systemPrivilegeAllMngMap.get(managerName) != null) {
			list = new ArrayList<String>(m_systemPrivilegeAllMngMap.get(managerName).keySet());
		}
		return list;
	}

	/**
	 * 権限ラベルの取得
	 * @param managerName マネージャ名
	 * @param systemPrivilegeInfo
	 * @return [SystemFunction Message] - [SystemPrivilege Message]
	 */
	public static String getSystemPrivilegeName(String managerName, SystemPrivilegeInfo systemPrivilegeInfo){
		createSystemPrivilegeMap(managerName);
		if (m_systemPrivilegeAllMngMap.get(managerName) != null) {
			for (Map.Entry<String, SystemPrivilegeInfo> entry : m_systemPrivilegeAllMngMap.get(managerName).entrySet()) {
				if (systemPrivilegeInfo.getSystemFunction().equals(entry.getValue().getSystemFunction()) &&
						systemPrivilegeInfo.getSystemPrivilege().equals(entry.getValue().getSystemPrivilege())) {
					return entry.getKey();
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param managerName マネージャ名
	 * @param [SystemFunction Message] - [SystemPrivilege Message]
	 * @return SyFunctionConstant.REPOSITORY + SystemPrivilegeMode.READ.name() の形
	 */
	public static SystemPrivilegeInfo getFunctionPrivilege(String managerName, String value){
		createSystemPrivilegeMap(managerName);
		if (m_systemPrivilegeAllMngMap.get(managerName) != null) {
			return m_systemPrivilegeAllMngMap.get(managerName).get(value);
		} else {
			return null;
		}
	}

	/**
	 * 
	 * マップからメッセージを取得する
	 * 
	 * @param systemPrivilegeInfo システム権限
	 * @return メッセージ
	 */
	private static String getSystemPrivilegeMessage(SystemPrivilegeInfo systemPrivilegeInfo){
		String message = "";
		if (systemPrivilegeInfo != null) {
			Locale locale = Locale.getDefault();

			String functionKey = KEY_SYSTEM_PRIVILEGE_FUNCTION_PREFIX 
					+ systemPrivilegeInfo.getSystemFunction().toLowerCase(locale);
			String functionmessage = m_messageMap.get(functionKey);

			// マップに存在しない場合のみ取得する
			if (functionmessage == null || functionmessage.length() == 0) {
				functionmessage = Messages.getString(functionKey, locale);
				m_messageMap.put(functionKey, functionmessage);
			}

			String privilegeKey = KEY_SYSTEM_PRIVILEGE_PRIVILEGE_PREFIX 
					+ systemPrivilegeInfo.getSystemPrivilege().toLowerCase(locale);
			String privilegemessage = m_messageMap.get(privilegeKey);

			// マップに存在しない場合のみ取得する
			if (privilegemessage == null || privilegemessage.length() == 0) {
				privilegemessage = Messages.getString(privilegeKey, locale);
				m_messageMap.put(privilegeKey, privilegemessage);
			}
			
			message = String.format("%s - %s", functionmessage, privilegemessage);
		}
		return message;
	}
}
