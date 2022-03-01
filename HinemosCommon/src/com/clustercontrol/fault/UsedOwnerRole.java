/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ロールがオーナーロールとして使用されている場合に利用するException
 * @version 4.1.0
 */
public class UsedOwnerRole extends HinemosUsed {

	private static final long serialVersionUID = 1L;

	private String m_roleId = null;
	private int plugin = 0;

	/**
	 * UsedOwnerRoleコンストラクタ
	 */
	public UsedOwnerRole() {
		super();
	}

	/**
	 * UsedOwnerRoleコンストラクタ
	 * @param messages
	 * @param e
	 */
	public UsedOwnerRole(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * UsedOwnerRoleコンストラクタ
	 * @param messages
	 */
	public UsedOwnerRole(String messages) {
		super(messages);
	}

	/**
	 * UsedOwnerRoleコンストラクタ
	 * @param e
	 */
	public UsedOwnerRole(Throwable e) {
		super(e);
	}

	/**
	 * UsedOwnerRoleコンストラクタ
	 * 
	 * @param plugin プラグイン(機能)
	 * 
	 * @see com.clustercontrol.bean.PluginConstant
	 */
	public UsedOwnerRole(int plugin, Throwable e) {
		super(e);
		this.plugin = plugin;
	}

	/**
	 * UsedOwnerRoleコンストラクタ
	 * 
	 * @param plugin プラグイン(機能)
	 * @param message メッセージ
	 * 
	 * @see com.clustercontrol.bean.PluginConstant
	 */
	public UsedOwnerRole(int plugin, String message) {
		super(message);
		this.plugin = plugin;
	}

	/**
	 * UsedOwnerRoleコンストラクタ
	 * 
	 * @param plugin プラグイン(機能)
	 * 
	 * @see com.clustercontrol.bean.PluginConstant
	 */
	public UsedOwnerRole(int plugin) {
		this.plugin = plugin;
	}


	/**
	 * プラグイン(機能)を返します。
	 * 
	 * @return プラグイン(機能)
	 * 
	 * @see com.clustercontrol.bean.PluginConstant
	 */
	public int getPlugin() {
		return plugin;
	}
	
	/**
	 * ロールIDを返します。
	 * @return ロールID
	 */
	public String getRoleId() {
		return m_roleId;
	}

	/**
	 * ロールIDを設定します。
	 * @param roleId ロールID
	 */
	public void setRoleId(String roleId) {
		m_roleId = roleId;
	}

}
