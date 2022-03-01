/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.factory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.fault.CommandTemplateNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.notify.model.CommandTemplateInfo;
import com.clustercontrol.notify.util.QueryUtil;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * コマンド通知テンプレート情報を検索するクラスです。
 *
 */
public class SelectCommandTemplate {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectCommandTemplate.class );

	/**
	 * コマンド通知テンプレート情報を返します。
	 *
	 * @param commandTemplateId 取得対象のテンプレートID
	 * @return コマンド通知テンプレート情報
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws CommandTemplateNotFound 
	 *
	 * @see com.clustercontrol.notify.model.CommandTemplateInfo
	 */
	public CommandTemplateInfo getCommandTemplate(String commandTemplateId) throws CommandTemplateNotFound, InvalidRole, HinemosUnknown {
		// コマンド通知テンプレート情報を取得
		CommandTemplateInfo info = null;

		try {
			info = QueryUtil.getCommandTemplateInfoPK(commandTemplateId);
		} catch (CommandTemplateNotFound e) {
			String[] args = { commandTemplateId };
			AplLogger.put(InternalIdCommon.PLT_CMD_TMP_SYS_004, args);
			throw e;
		} catch (InvalidRole e) {
			throw e;
		}
		return info;
	}

	/**
	 * コマンド通知テンプレート情報一覧を返します
	 *
	 * @return コマンド通知テンプレート情報一覧（Objectの2次元配列）
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.model.CommandTemplateInfo
	 */
	public List<CommandTemplateInfo> getCommandTemplateList() {
		m_log.debug("getCommandTemplateInfoList() : start");

		// コマンド通知テンプレート情報一覧を取得
		List<CommandTemplateInfo> list = QueryUtil.getAllCommandTemplateOrderByCommandTemplateId();
		return list;
	}
	
	/**
	 * オーナーロールIDを条件としてコマンド通知テンプレート情報一覧を返します。
	 *
	 * @param ownerRoleId
	 * @return コマンド通知テンプレート情報一覧（Objectの2次元配列）
	 *
	 * @see com.clustercontrol.notify.model.CommandTemplateInfo
	 */
	public List<CommandTemplateInfo> getCommandTemplateListByOwnerRole(String ownerRoleId) {
		m_log.debug("getCommandTemplateListByOwnerRole() : start, ownerRoleId=" + ownerRoleId);

		// コマンド通知テンプレート情報一覧を取得
		List<CommandTemplateInfo> ct = QueryUtil.getAllCommandTemplateOrderByCommandTemplateId_OR(ownerRoleId);
		return ct;
	}
}
