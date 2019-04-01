/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.AutoRegisterNodeSettingNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.model.AutoRegisterNodeInfo;
import com.clustercontrol.repository.session.AutoRegisterNodeControllerBean;
import com.clustercontrol.repository.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

public class AutoRegisterSettingModifier {

	// ログ出力用インスタンス
	private static Log m_log = LogFactory.getLog(AutoRegisterNodeControllerBean.class);
	// ログ出力区切り文字
	private static final String DELIMITER = "() : ";

	/**
	 * 自動登録の設定を変更する<BR>
	 * 
	 * @throws InvalidRole
	 */
	public static void modifyAutoRegisterNodeInfo(AutoRegisterNodeInfo updateSetting, String modifyUserId)
			throws AutoRegisterNodeSettingNotFound, InvalidRole {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "modifing a setting to register node automatically...");

		Integer orderNo = updateSetting.getOrderNo();
		AutoRegisterNodeInfo newSetting = QueryUtil.getAutoRegisterNodeInfo(orderNo, ObjectPrivilegeMode.MODIFY);

		// 各項目を設定内容で上書き.
		newSetting.setSourceNetwork(updateSetting.getSourceNetwork());
		newSetting.setDescription(updateSetting.getDescription());
		newSetting.setOwnerRoleId(updateSetting.getOwnerRoleId());
		newSetting.setPrefix(updateSetting.getPrefix());
		newSetting.setValid(updateSetting.getValid());
		newSetting.setLastSerialNumber(updateSetting.getLastSerialNumber());
		newSetting.setUpdateUser(modifyUserId);
		newSetting.setUpdateDate(HinemosTime.currentTimeMillis());

		m_log.info(methodName + DELIMITER + "successful in  a setting to register node automatically. (orderNo = "
				+ orderNo + ")");
	}
}
