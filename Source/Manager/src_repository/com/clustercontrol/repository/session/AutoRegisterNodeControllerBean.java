/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.session;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.AutoRegisterNodeSettingNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.repository.bean.AutoRegisterResult;
import com.clustercontrol.repository.bean.AutoRegisterStatus;
import com.clustercontrol.repository.factory.NodeAutoRegister;
import com.clustercontrol.repository.factory.AutoRegisterSettingModifier;
import com.clustercontrol.repository.model.AutoRegisterNodeInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 自動ノード登録の管理を行う Session Bean <BR>
 * 他機能からの Entity Bean へのアクセスは、Session Bean を介して行う.
 * 
 * @version 6.2.0
 * @since 6.2.0
 * 
 */
public class AutoRegisterNodeControllerBean {

	// ログ出力用インスタンス
	private static Log m_log = LogFactory.getLog(AutoRegisterNodeControllerBean.class);
	// ログ出力区切り文字
	private static final String DELIMITER = "() : ";

	/**
	 * 自動ノード登録.
	 * 
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws HinemosDbTimeout
	 */
	public static AutoRegisterResult autoRegister(String platform, List<NodeNetworkInterfaceInfo> nodeNifList,
			String forLogAddress, InetAddress sourceIpAddress) throws InvalidSetting, HinemosDbTimeout, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		AutoRegisterResult result = null;
		// 自動登録が有効かチェック.
		if (!HinemosPropertyCommon.auto_register_node_valid.getBooleanValue()) {
			result = new AutoRegisterResult(AutoRegisterStatus.INVALID, null);
			m_log.info(methodName + DELIMITER + "invalid to retister automatically on hinemos property");
			return result;
		}

		// ソースIPアドレス存在チェック.
		if (sourceIpAddress == null
				|| (!(sourceIpAddress instanceof Inet4Address) && !(sourceIpAddress instanceof Inet6Address))) {
			String message = String.format("failed to get the IP address on source. MAC addresses=[%s]", forLogAddress);
			m_log.warn(methodName + DELIMITER + message);
			String[] args = { MessageConstant.MAC_ADDRESS.getMessage(), forLogAddress };
			AplLogger.put(InternalIdCommon.PLT_REP_AREG_SYS_002, args);
			throw new HinemosUnknown(message);
		}

		// 自動登録処理.
		NodeAutoRegister register = new NodeAutoRegister(platform, nodeNifList, forLogAddress, sourceIpAddress);
		result = register.autoRegister();

		return result;
	}

	/**
	 * 自動ノード登録設定の更新.
	 * 
	 * @param updateSetting
	 *            更新内容が設定されているオブジェクト(空は空で上書き、nullはデフォルト値で上書き).
	 * @throws HinemosUnknown
	 * @throws AutoRegisterNodeSettingNotFound
	 * 
	 */
	public static void modifyAutoRegisterSetting(AutoRegisterNodeInfo updateSetting)
			throws HinemosUnknown, AutoRegisterNodeSettingNotFound {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		JpaTransactionManager jtm = null;

		// nullの項目にデフォルト値をセット.
		updateSetting.setDefaultValue();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 更新処理
			AutoRegisterSettingModifier.modifyAutoRegisterNodeInfo(updateSetting,
					(String) HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));
			jtm.commit();

		} catch (AutoRegisterNodeSettingNotFound e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

}
